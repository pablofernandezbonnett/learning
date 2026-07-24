# Lua for JVM Backend Engineers

Use this note when you want the smallest useful explanation of why `Lua` can
fit a `Java` or `Kotlin` backend at all.

This is not a full Lua course.
It is the bridge from normal JVM backend instincts to one specific scripting
use case: narrow hot-swappable business rules.

---

## 1. Why This Matters

The interesting part of Lua for a backend engineer is usually not "I want
another programming language."

It is this problem:

- some rules change much faster than the main service should be rebuilt and redeployed
- those rules are still too dynamic or too business-shaped for hardcoded Java or Kotlin to feel like the right release vehicle
- a full custom DSL would cost more than the rule layer is worth

That is where an embedded scripting language can help.

---

## 2. Smallest Useful Mental Model

The JVM service still owns the real backend system:

- auth
- request validation
- transactions
- source of truth
- external side effects
- rollout and rollback

Lua only owns one narrow rule evaluation.

Plain-English version:

> the host application owns correctness and safety; Lua only owns one small
> decision that needs to change quickly

Small concrete example:

- checkout service already knows the authenticated user, product, region, and base price
- Lua decides whether a seasonal pricing rule applies
- the host validates the returned discount and still owns the final order state

---

## 3. Bad Mental Model Vs Better Mental Model

Bad mental model:

- "Lua is a way to patch production logic live."

Better mental model:

- "Lua is a way to move one narrow rule layer out of the main deploy cycle while the host service still owns safety and correctness."

Bad mental model:

- "If the script works, the architecture is done."

Better mental model:

- "The script is only the small visible part. The hard part is sandboxing, input shaping, result validation, rollout, rollback, and audit."

Bad mental model:

- "We can expose a lot of the JVM to Lua so the script stays powerful."

Better mental model:

- "The script should get a tiny input context and return a tiny result. Arbitrary runtime power is exactly what makes embedded scripting dangerous."

---

## 4. What Already Transfers From Java And Kotlin

Most backend judgment transfers directly:

- business invariants
- source of truth
- side-effect boundaries
- retries and idempotency
- observability
- deployment caution

What changes is mostly the expression style:

- fewer types
- one main data structure: tables
- narrower scripts
- stronger need for host-owned safety rails

So the real shift is not "new backend fundamentals."
It is "same backend judgment, weaker local language guarantees."

---

## 5. What To Learn First In Lua

If your only goal is embedded rules, learn these first:

### Tables

Lua tables are the flexible structure behind:

- arrays
- maps
- object-like values

That is why [examples/01-tables.lua](./examples/01-tables.lua) is the first useful file.

### `local`

Variables are global by default.
That is a real footgun in rule scripts.

Strong default:

- use `local` unless you very deliberately want shared global state

### Functions returning tables

Most useful embedded rule scripts look like:

- read `ctx`
- compute a result
- return a small result table

### Simple control flow

You mostly need:

- `if`
- loops
- string functions
- math

You do not need advanced metaprogramming first.

---

## 6. What A Good Host Integration Shape Looks Like

Strong default:

1. select a script version
2. build a strict input context
3. execute in a sandbox
4. enforce timeout or instruction limit
5. validate the returned result
6. log script version, latency, and outcome
7. keep rollback easy

Small Kotlin-shaped example:

```kotlin
data class PricingContext(
    val productId: String,
    val basePrice: Int,
    val customerTier: String,
    val quantity: Int,
)

data class PricingDecision(
    val finalPrice: Int,
    val reason: String,
)

interface PricingRuleEngine {
    fun evaluate(ruleVersion: String, ctx: PricingContext): PricingDecision
}
```

Why this shape is good:

- the host contract stays typed and small
- the script version is explicit
- the script is forced into one narrow decision boundary

Small Java-shaped reminder:

- the same idea works perfectly well from Java
- Kotlin is only a nicer syntax for the host-side contract
- the important thing is the JVM-owned boundary, not the host language aesthetics

Security and permission rule:

- the script should not get ambient access to files, network, process execution, or broad JVM reflection
- the runtime identity that fetches scripts should be read-only
- the people or systems that publish scripts should be a smaller, more controlled set than the systems that merely execute them

### `Globals` Have A Lifetime

In LuaJ, a `Globals` environment holds script-visible state. If unrelated
requests share one mutable environment, a script can leave global state behind
for a later evaluation to observe.

Strong default:

- create a fresh, restricted environment for each evaluation or isolate state
  per request and script version
- pass all needed values through `ctx` and return the decision explicitly
- if you pool environments for performance, reset or recreate them and prove
  isolation with tests before trusting the pool

This is why `local` is more than style in a rule script: it reduces accidental
state that can survive beyond the decision you intended to make.

---

## 7. What Hot Updates Actually Mean

When people say "update rules in hot," the safe version is usually this:

- scripts are stored with versioning
- one approved version is active
- the service reloads or re-fetches that version
- rollback means switching back to the previous known-good version

Weak approach:

- editing one live script blob with no version or audit trail

Better approach:

- treating scripts like controlled deployable artifacts with approval, version, metrics, and rollback

Practical rule:

> hot-swappable rules still need release discipline; they just use a smaller
> release unit than the whole service

Good engineer details that matter here:

- separate `draft`, `approved`, and `active` script states
- allow one-click rollback to the previous approved version
- avoid direct in-place mutation of the active script blob
- keep a kill switch so the host can fall back to a safe built-in rule or reject the optional rule path
- if the rule affects money or ordering, prefer staged rollout over instant global enable

Weak approach:

- one admin edits the live script and the service immediately uses it everywhere

Better approach:

- the service activates a specific approved version and can roll back or disable it quickly if latency, error rate, or business output looks wrong

---

## 8. Script Security And Governance

This is where many average implementations go wrong.

The script is only one artifact.
The real safety comes from the surrounding controls.

Strong default:

- keep the input context small and typed on the host side
- validate the returned result before it affects pricing, eligibility, or any downstream write
- expose only the minimum globals the script truly needs
- sign or otherwise integrity-protect stored script artifacts
- log script version, caller, latency, and decision outcome
- alert on script failures, timeout spikes, and unusual result distributions

Permission model worth keeping:

- publishers can submit or approve scripts
- runtime can only read approved scripts
- ordinary service instances cannot publish or modify scripts
- rollback authority should be explicit and easy to use during incidents

Short rule:

> the safest Lua script is a tiny, sandboxed rule with tiny inputs, tiny outputs,
> read-only runtime access, and an easy rollback path

---

## 9. Where Lua Fits Well

Good fit:

- promotions
- pricing tweaks
- merchant-specific rules
- feature eligibility
- narrow scoring or classification rules

Bad fit:

- authentication or authorization core logic
- transaction coordination
- database write orchestration
- arbitrary integration workflows
- any logic that needs broad host or network access

Short rule:

> if the rule is narrow and changes often, Lua can help; if the logic is core
> backend correctness, keep it in the main service

---

## 10. Main Tradeoff

The benefit is agility.
The cost is governance.

You gain:

- faster rule iteration
- smaller release surface for one class of change
- less pressure to rebuild and redeploy the whole service for every pricing tweak

You pay with:

- weaker typing
- weaker local tooling
- sandbox work
- audit and rollout discipline
- more ways to confuse ownership if boundaries are sloppy

---

## 11. Strong Default

If you only want the sensible first version of this pattern:

- keep scripts tiny
- pass a tiny `ctx`
- return a tiny result
- expose no file, network, or host reflection access
- keep scripts versioned and reviewable
- make rollback trivial

That is enough to learn the architecture idea without overbuilding it.

---

## 12. Suggested Study Order

1. read this note first
2. run [examples/01-tables.lua](./examples/01-tables.lua)
3. run [examples/03-retail-rules.lua](./examples/03-retail-rules.lua)
4. read [examples/04-sandbox-security.lua](./examples/04-sandbox-security.lua)
5. treat [examples/02-metatables.lua](./examples/02-metatables.lua) as secondary unless you want deeper Lua language knowledge

---

## 13. Takeaway

The useful Lua idea for a JVM backend engineer is not "replace backend code
with scripts."

It is:

> keep the main service responsible for correctness and safety, and use Lua only
> for one narrow rule layer that changes faster than the main deployment cycle
> should.

## Further Reading

- [Lua official overview](https://www.lua.org/work/doc/)
- [LuaJ project](https://github.com/luaj/luaj)
- [LuaJ `Globals` API](https://luaj.org/luaj/3.0/api/org/luaj/vm2/Globals.html)
