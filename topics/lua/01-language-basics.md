# Lua Essentials for Small Rules

A Lua rule usually needs local variables, functions, conditionals, and tables. Learn these before exploring metatables or advanced language features.

## Tables

A table stores key-value pairs. Numeric keys can represent a list; named keys can represent a small record. Lua list indexes normally start at 1.

```lua
local tiers = { bronze = 0, gold = 0.10 }
local items = { "shirt", "jacket" }

local discount = tiers.gold
local first = items[1]
```

`nil` means a key has no value; assigning `nil` removes it. Use `ipairs` for a contiguous list and `pairs` when iterating all present keys without relying on order. The length operator `#` is intended for sequences; a table with holes can have more than one valid boundary, so do not use `#` as a reliable item count there.

## Local State and Functions

Use `local` for variables and helper functions. A global name can be visible to later code in the same environment, which is especially risky if a host reuses that environment across evaluations.

```lua
local function eligible(ctx)
    return ctx.quantity >= 3 and ctx.customer_tier == "gold"
end

local function evaluate(ctx)
    if eligible(ctx) then
        return { discount_percent = 10, reason = "gold bulk" }
    end
    return { discount_percent = 0, reason = "none" }
end
```

The host should supply validated input and validate the returned table. The snippet shows the shape of a decision, not a complete pricing policy.

## What to Leave for Later

Metatables can change table behavior, including lookup through `__index`. They are useful in some Lua programs but add little to a narrow, data-in/decision-out rule. Start without them.

## Further Reading

- [Lua reference manual](https://www.lua.org/manual/) for tables, scope, and functions.
- [Programming in Lua](https://www.lua.org/pil/) for worked language examples; check its edition against the Lua version you use.
