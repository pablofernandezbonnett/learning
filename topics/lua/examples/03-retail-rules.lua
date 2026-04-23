--[[
  Lab 3: Retail Business Rules Engine
  Run: lua examples/03-retail-rules.lua

  USE CASE: Lua as a business rules engine embedded in a Java/Kotlin backend.

  WHY LUA IN RETAIL?
  ─────────────────────────────────────────────────────────────────────────────
  Problem: Pricing rules change frequently (Black Friday, member tiers, regional
  promotions). Redeploying Java/Kotlin services for every rule change is slow,
  risky, and requires a full CI/CD cycle.

  Solution: Business rules written in Lua scripts, loaded and executed by the
  JVM at runtime via LuaJ (https://luaj.sourceforge.net/).

  Architecture:
    Java/Kotlin service
      ↓  loads at startup (or on-demand from DB / S3)
    Lua script (this file)
      ↓  receives context (product, customer, cart)
      ↓  returns: finalPrice, discount, eligibilityReason
    Java/Kotlin service uses the result

  Key advantage: change pricing rules without redeploying the service.
  The ops team (or a business admin UI) updates the Lua script in S3/DB,
  the service reloads it → live rule update in seconds.

  LuaJ integration (Java pseudocode — SANDBOXED, see 04-sandbox-security.lua):
    // NEVER use JsePlatform.standardGlobals() — it exposes io, os, shell access.
    // Always use SandboxedLuaLoader (loads only math, string, table, base).
    LuaTable ctx = new LuaTable();
    ctx.set("productId", LuaValue.valueOf("J001"));
    ctx.set("basePrice",  LuaValue.valueOf(14900));
    ctx.set("customerTier", LuaValue.valueOf("gold"));
    LuaValue result = SandboxedLuaLoader.execute(luaScript, ctx);
    int finalPrice = result.get("finalPrice").toint();
--]]

-- ─── Constants & Lookup Tables ─────────────────────────────────────────────────

local TIER_DISCOUNTS = {
    bronze   = 0.00,    -- no discount
    silver   = 0.05,    -- 5% off
    gold     = 0.10,    -- 10% off
    platinum = 0.15,    -- 15% off
}

local CATEGORY_MULTIPLIERS = {
    outerwear    = 1.0,   -- full price
    tops         = 1.0,
    bottoms      = 1.0,
    accessories  = 0.9,   -- accessories always 10% off (clearance strategy)
}

-- Promotion calendar: { [date_key] = { name, discount } }
local PROMOTIONS = {
    ["11-28"] = { name = "Black Friday",       discount = 0.30 },
    ["12-24"] = { name = "Christmas Eve",       discount = 0.15 },
    ["01-01"] = { name = "New Year Flash Sale", discount = 0.20 },
}

-- ─── Helper Functions ──────────────────────────────────────────────────────────

local function clamp(value, min_val, max_val)
    return math.max(min_val, math.min(max_val, value))
end

local function roundYen(price)
    -- Round to nearest 10 yen (Japanese retail convention)
    return math.floor(price / 10 + 0.5) * 10
end

local function getCurrentPromotion()
    -- In production: os.date returns real date.
    -- For testing, we simulate Black Friday.
    local date_key = os.date("%m-%d")  -- e.g., "11-28"
    return PROMOTIONS[date_key]
end

-- ─── Core Pricing Engine ───────────────────────────────────────────────────────

--[[
  calculatePrice(ctx) → result table

  ctx fields (provided by Java/Kotlin caller):
    - productId       : string
    - productName     : string
    - basePrice       : number (yen)
    - category        : string ("outerwear"|"tops"|"bottoms"|"accessories")
    - customerTier    : string ("bronze"|"silver"|"gold"|"platinum")
    - quantity        : number (items in cart)
    - isStoreEmployee : boolean
    - storeId         : number
--]]

local function calculatePrice(ctx)
    local base         = ctx.basePrice or 0
    local tier         = ctx.customerTier or "bronze"
    local category     = ctx.category or "tops"
    local quantity     = ctx.quantity or 1
    local isEmployee   = ctx.isStoreEmployee or false

    local discountReasons = {}
    local totalDiscount   = 0.0

    -- ── Rule 1: Customer tier discount ───────────────────────────────────────
    local tierDiscount = TIER_DISCOUNTS[tier] or 0
    if tierDiscount > 0 then
        totalDiscount = totalDiscount + tierDiscount
        table.insert(discountReasons,
            string.format("%s tier: -%.0f%%", tier, tierDiscount * 100))
    end

    -- ── Rule 2: Category multiplier ───────────────────────────────────────────
    local categoryMult = CATEGORY_MULTIPLIERS[category] or 1.0
    if categoryMult < 1.0 then
        local catDiscount = 1.0 - categoryMult
        totalDiscount = totalDiscount + catDiscount
        table.insert(discountReasons,
            string.format("%s category: -%.0f%%", category, catDiscount * 100))
    end

    -- ── Rule 3: Seasonal promotion (date-based) ───────────────────────────────
    local promo = getCurrentPromotion()
    if promo then
        -- Promotions stack with tier discount but are capped to avoid losses
        local promoDiscount = promo.discount
        totalDiscount = totalDiscount + promoDiscount
        table.insert(discountReasons,
            string.format("%s: -%.0f%%", promo.name, promoDiscount * 100))
    end

    -- ── Rule 4: Bulk purchase discount ────────────────────────────────────────
    if quantity >= 3 then
        local bulkDiscount = 0.05  -- 5% for 3+ items
        totalDiscount = totalDiscount + bulkDiscount
        table.insert(discountReasons,
            string.format("bulk buy (%d items): -%.0f%%", quantity, bulkDiscount * 100))
    end

    -- ── Rule 5: Employee discount (overrides everything, generous) ────────────
    if isEmployee then
        -- Employee discount replaces all other discounts
        totalDiscount = 0.30   -- 30% fixed
        discountReasons = { "employee discount: -30%" }
    end

    -- ── Cap total discount to 50% (floor margin protection) ──────────────────
    totalDiscount = clamp(totalDiscount, 0, 0.50)

    -- ── Calculate final price ─────────────────────────────────────────────────
    local finalPrice = roundYen(base * (1 - totalDiscount))

    return {
        productId      = ctx.productId,
        productName    = ctx.productName,
        basePrice      = base,
        finalPrice     = finalPrice,
        discountTotal  = math.floor(totalDiscount * 100 + 0.5),  -- as integer percent
        savedYen       = base - finalPrice,
        reasons        = discountReasons,
        eligible       = finalPrice > 0,
    }
end

-- ─── Stock Availability Rules ──────────────────────────────────────────────────

local function checkAvailability(ctx)
    local stock    = ctx.stock or 0
    local quantity = ctx.quantity or 1
    local storeId  = ctx.storeId

    -- Rules applied in priority order
    if stock <= 0 then
        return { available = false, reason = "OUT_OF_STOCK", canBackorder = false }
    end

    if quantity > stock then
        -- Partial fulfillment: allow ordering up to available stock
        return {
            available    = true,
            reason       = "PARTIAL_STOCK",
            canFulfill   = stock,
            requested    = quantity,
            message      = string.format("Only %d available (requested %d)", stock, quantity),
        }
    end

    -- Low stock warning (reorder point logic)
    if stock <= 10 then
        return {
            available = true,
            reason    = "LOW_STOCK",
            message   = string.format("Only %d left — order soon!", stock),
        }
    end

    return { available = true, reason = "IN_STOCK" }
end

-- ─── Demo: Simulate different customer scenarios ──────────────────────────────

local function printResult(label, result)
    print(string.format("\n[%s]", label))
    print(string.format("  Product   : %s (%s)", result.productName, result.productId))
    print(string.format("  Base price: ¥%d", result.basePrice))
    print(string.format("  Final price: ¥%d  (-%d%%)", result.finalPrice, result.discountTotal))
    print(string.format("  You save  : ¥%d", result.savedYen))
    if #result.reasons > 0 then
        print("  Reasons   : " .. table.concat(result.reasons, " + "))
    end
end

print("========== RETAIL PRICING ENGINE DEMO ==========")

-- Scenario A: Regular customer buying a jacket
local r1 = calculatePrice({
    productId      = "J001",
    productName    = "Ultra Light Down",
    basePrice      = 14900,
    category       = "outerwear",
    customerTier   = "bronze",
    quantity       = 1,
    isStoreEmployee = false,
})
printResult("Bronze customer, 1 jacket", r1)

-- Scenario B: Gold member buying 3 T-shirts (tier + bulk)
local r2 = calculatePrice({
    productId      = "T001",
    productName    = "AIRism T-Shirt",
    basePrice      = 2990,
    category       = "tops",
    customerTier   = "gold",
    quantity       = 3,
    isStoreEmployee = false,
})
printResult("Gold member, 3 T-shirts (bulk)", r2)

-- Scenario C: Platinum member buying accessories (tier + category)
local r3 = calculatePrice({
    productId      = "A001",
    productName    = "Canvas Tote Bag",
    basePrice      = 1990,
    category       = "accessories",
    customerTier   = "platinum",
    quantity       = 1,
    isStoreEmployee = false,
})
printResult("Platinum member, accessory", r3)

-- Scenario D: Store employee (fixed 30% override)
local r4 = calculatePrice({
    productId      = "J001",
    productName    = "Ultra Light Down",
    basePrice      = 14900,
    category       = "outerwear",
    customerTier   = "gold",
    quantity       = 1,
    isStoreEmployee = true,
})
printResult("Employee purchase (30% fixed)", r4)

print()
print("========== AVAILABILITY CHECKS ==========")

local function printAvailability(label, result)
    print(string.format("[%s] available=%s, reason=%s%s",
        label,
        tostring(result.available),
        result.reason,
        result.message and (" — " .. result.message) or ""
    ))
end

printAvailability("In stock (500 units)", checkAvailability({ stock = 500, quantity = 2 }))
printAvailability("Low stock (5 units)",  checkAvailability({ stock = 5,   quantity = 1 }))
printAvailability("Partial (10 left, want 15)", checkAvailability({ stock = 10, quantity = 15 }))
printAvailability("Out of stock",         checkAvailability({ stock = 0,   quantity = 1 }))

print()
print("========== INTEGRATION NOTE ==========")
print([[
In production (LuaJ / Java integration):

  Globals globals = JsePlatform.standardGlobals();

  // Pass context from Java to Lua
  LuaTable ctx = new LuaTable();
  ctx.set("productId",    LuaValue.valueOf("J001"));
  ctx.set("basePrice",    LuaValue.valueOf(14900));
  ctx.set("customerTier", LuaValue.valueOf("gold"));
  ctx.set("quantity",     LuaValue.valueOf(3));
  globals.set("ctx", ctx);

  // Execute the rules script (loaded from DB, S3, or classpath)
  LuaValue chunk = globals.load(luaScript);
  LuaValue result = chunk.call();

  // Read results back into Java
  int finalPrice    = result.get("finalPrice").toint();
  int discountPct   = result.get("discountTotal").toint();

Advantage: update Lua script in S3 bucket → pricing rules change live
without redeployment. Business team can modify rules independently.
]])
