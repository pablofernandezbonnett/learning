--[[
  Lab 1: Tables — Lua's Only Data Structure
  Run: lua examples/01-tables.lua

  Lua has ONE data structure: the Table.
  It is simultaneously:
    - An array (sequential integer keys: t[1], t[2], ...)
    - A map/dictionary (arbitrary keys: t["name"], t.name)
    - An object (fields + methods)

  Java analogy: HashMap<Object, Object> with syntactic sugar.
  Kotlin analogy: listOf(), mapOf(), and data class combined into one type.
--]]

print("=== 1. Table as an Array ===")

local products = {
    "Ultra Light Down Jacket",  -- index 1 (Lua arrays start at 1!)
    "AIRism T-Shirt",           -- index 2
    "Heattech Inner",           -- index 3
    "Slim Fit Chinos",          -- index 4
}

-- Access by index
print("First product:", products[1])          -- Ultra Light Down Jacket
print("Last product:", products[#products])   -- #table = length operator

-- Iterate with ipairs (stops at first nil)
print("All products:")
for i, name in ipairs(products) do
    print(string.format("  [%d] %s", i, name))
end

-- Add to array
table.insert(products, "Fleece Jacket")       -- append at end
table.insert(products, 2, "Oxford Shirt")     -- insert at position 2 (shifts others)
print("After inserts:", #products, "items")

-- Remove
local removed = table.remove(products, 2)     -- remove at position 2, returns the value
print("Removed:", removed)

print()
print("=== 2. Table as a Map (Dictionary) ===")

-- Two equivalent syntaxes for string keys
local product = {
    id       = "J001",            -- shorthand for ["id"] = "J001"
    name     = "Ultra Light Down",
    priceYen = 14900,
    category = "outerwear",
    inStock  = true,
}

-- Access
print("Name:", product.name)          -- dot notation (sugar for product["name"])
print("Price:", product["priceYen"])  -- bracket notation

-- Add / modify / delete fields
product.color = "Navy"                -- add new field
product.priceYen = 13900              -- modify (sale price)
product.inStock = nil                 -- delete field (nil = absence of value)

-- Iterate with pairs (all key-value pairs, unordered)
print("Product fields:")
for key, value in pairs(product) do
    print(string.format("  %s = %s", tostring(key), tostring(value)))
end

-- Check if field exists
if product.color ~= nil then
    print("Color is set:", product.color)
end

print()
print("=== 3. Table as a Nested Structure ===")

local store = {
    name     = "UNIQLO Ginza",
    storeId  = 1001,
    location = {
        city    = "Tokyo",
        district = "Ginza",
        country = "Japan",
    },
    departments = {
        { name = "Outerwear",   manager = "Taro" },
        { name = "Tops",        manager = "Hanako" },
        { name = "Accessories", manager = "Kenji" },
    },
}

print("Store:", store.name)
print("City:", store.location.city)
print("Departments:")
for i, dept in ipairs(store.departments) do
    print(string.format("  %d. %s (manager: %s)", i, dept.name, dept.manager))
end

print()
print("=== 4. Table as a Function Container (Module pattern) ===")

-- Tables can hold functions — this is Lua's module system
local MathUtils = {}

function MathUtils.sum(t)
    local total = 0
    for _, v in ipairs(t) do total = total + v end
    return total
end

function MathUtils.average(t)
    return MathUtils.sum(t) / #t
end

function MathUtils.max(t)
    local m = t[1]
    for _, v in ipairs(t) do if v > m then m = v end end
    return m
end

local prices = { 14900, 2990, 1990, 4990, 3490 }
print(string.format("Sum:     ¥%d", MathUtils.sum(prices)))
print(string.format("Average: ¥%.0f", MathUtils.average(prices)))
print(string.format("Max:     ¥%d", MathUtils.max(prices)))

print()
print("=== 5. Common Table Operations ===")

-- table.concat: join array elements as string
local tags = { "outerwear", "winter", "bestseller" }
print("Tags:", table.concat(tags, ", "))

-- Sort (in-place)
local prices2 = { 14900, 2990, 1990, 4990 }
table.sort(prices2)                              -- ascending
print("Sorted asc:", table.concat(prices2, ", "))

table.sort(prices2, function(a, b) return a > b end)  -- descending (custom comparator)
print("Sorted desc:", table.concat(prices2, ", "))

-- Deep copy (tables are passed by reference — not copied by assignment)
local function deepCopy(original)
    local copy = {}
    for k, v in pairs(original) do
        copy[k] = type(v) == "table" and deepCopy(v) or v
    end
    return copy
end

local original = { name = "Jacket", tags = { "winter", "warm" } }
local copy = deepCopy(original)
copy.name = "Copy"
print("Original name:", original.name)  -- still "Jacket"
print("Copy name:", copy.name)          -- "Copy"

print()
print("=== 6. Sparse Tables and nil behavior ===")

local sparse = {}
sparse[1] = "first"
sparse[5] = "fifth"   -- indices 2-4 are nil
sparse[10] = "tenth"

-- #sparse is undefined for sparse tables (Lua spec says "any boundary")
-- Use pairs() to safely iterate all non-nil values
print("Sparse table values:")
for k, v in pairs(sparse) do
    print(string.format("  [%s] = %s", tostring(k), tostring(v)))
end
