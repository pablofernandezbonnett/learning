--[[
  Lab 2: Metatables — OOP in a Classless World
  Run: lua examples/02-metatables.lua

  Lua doesn't have classes. It has metatables — a mechanism to define behavior
  for tables (what happens when you index them, call them, add them, etc.).

  Prototypal inheritance: one table can "inherit" from another via __index.
  This is how JavaScript's prototype chain works, but more explicit.

  Java/Kotlin analogy:
    - __index    ≈ method lookup (checks parent class if method not found)
    - __tostring ≈ toString()
    - __add      ≈ operator overloading (operator fun plus())
    - __call     ≈ making an object callable (invoke())
--]]

print("=== 1. Basic Metatable — __index (method lookup) ===")

-- Define a "class" as a table
local Product = {}
Product.__index = Product   -- when a field isn't found in an instance, look in Product

-- Constructor
function Product.new(id, name, priceYen)
    local self = setmetatable({}, Product)  -- create a table, set Product as its metatable
    self.id       = id
    self.name     = name
    self.priceYen = priceYen
    self.stock    = 0
    return self
end

-- Methods defined on the "class" table
function Product:describe()
    -- ':' syntax passes 'self' automatically (sugar for Product.describe(self))
    return string.format("Product[%s] %s @ ¥%d (stock: %d)",
                         self.id, self.name, self.priceYen, self.stock)
end

function Product:addStock(qty)
    assert(qty > 0, "Quantity must be positive")
    self.stock = self.stock + qty
    return self   -- enable method chaining
end

function Product:applyDiscount(percent)
    assert(percent > 0 and percent < 100, "Discount must be 1-99%")
    self.priceYen = math.floor(self.priceYen * (1 - percent / 100))
    return self
end

-- Create instances
local jacket  = Product.new("J001", "Ultra Light Down", 14900)
local tshirt  = Product.new("T001", "AIRism T-Shirt", 2990)

jacket:addStock(250):applyDiscount(20)   -- method chaining
tshirt:addStock(1500)

print(jacket:describe())   -- J001, discounted price
print(tshirt:describe())

-- The 'jacket' table does NOT have a 'describe' field — but its metatable does.
-- When jacket.describe is accessed, Lua finds __index = Product and looks there.
print("jacket.describe == Product.describe:", jacket.describe == Product.describe)  -- true

print()
print("=== 2. __tostring — custom string conversion ===")

-- Override how print() displays a Product
Product.__tostring = function(self)
    return string.format("<%s: %s>", self.id, self.name)
end

print(tostring(jacket))   -- <J001: Ultra Light Down>
-- Note: print() doesn't call __tostring automatically in Lua 5.1/5.2
-- In Lua 5.3+: print(jacket) calls __tostring

print()
print("=== 3. Inheritance — extend a class ===")

-- PremiumProduct inherits from Product and adds loyalty points
local PremiumProduct = setmetatable({}, { __index = Product })  -- inherit from Product
PremiumProduct.__index = PremiumProduct

function PremiumProduct.new(id, name, priceYen, loyaltyPoints)
    local self = Product.new(id, name, priceYen)   -- call parent constructor
    setmetatable(self, PremiumProduct)               -- re-set metatable to PremiumProduct
    self.loyaltyPoints = loyaltyPoints
    return self
end

-- Override describe() — adds loyalty points info
function PremiumProduct:describe()
    local base = Product.describe(self)   -- call parent method explicitly
    return base .. string.format(" [+%d points]", self.loyaltyPoints)
end

function PremiumProduct:calculateReward(purchaseAmountYen)
    return math.floor(purchaseAmountYen / 100) * self.loyaltyPoints
end

local cashmere = PremiumProduct.new("K001", "Cashmere Crewneck", 19900, 5)
cashmere:addStock(100)
print(cashmere:describe())                                -- uses overridden describe
print("Reward points for ¥19900:", cashmere:calculateReward(19900))

-- Inheritance chain: cashmere → PremiumProduct → Product
print("Is PremiumProduct:", getmetatable(cashmere) == PremiumProduct)  -- true
print("Has addStock method:", cashmere.addStock == Product.addStock)    -- true (inherited)

print()
print("=== 4. __newindex — intercept field writes (validation / change tracking) ===")

local function makeValidatedProduct(id, name, price)
    local data = { id = id, name = name, priceYen = price }
    local changed = {}

    local mt = {
        __index = data,
        __newindex = function(t, key, value)
            -- Intercept writes to validate and track changes
            if key == "priceYen" then
                assert(type(value) == "number" and value > 0, "Price must be a positive number")
                if data[key] ~= value then
                    changed[key] = { from = data[key], to = value }
                end
            end
            data[key] = value   -- write to the underlying data table
        end,
        __tostring = function(t)
            return string.format("Product[%s] ¥%d", data.id, data.priceYen)
        end,
    }

    return setmetatable({}, mt), changed
end

local p, changes = makeValidatedProduct("T002", "Heattech", 1990)
p.priceYen = 1790     -- 10% off — triggers __newindex

print("After price change:")
if changes.priceYen then
    print(string.format("  priceYen: ¥%d → ¥%d", changes.priceYen.from, changes.priceYen.to))
end

-- Validation
local ok, err = pcall(function() p.priceYen = -100 end)  -- negative price
if not ok then print("Validation error caught:", err) end

print()
print("=== 5. __add — operator overloading (combine carts) ===")

local Cart = {}
Cart.__index = Cart

function Cart.new()
    return setmetatable({ items = {} }, Cart)
end

function Cart:add(productId, qty)
    self.items[productId] = (self.items[productId] or 0) + qty
    return self
end

function Cart:total(products)
    local sum = 0
    for pid, qty in pairs(self.items) do
        local p = products[pid]
        if p then sum = sum + p.priceYen * qty end
    end
    return sum
end

-- __add: cart1 + cart2 merges both carts
Cart.__add = function(a, b)
    local merged = Cart.new()
    for pid, qty in pairs(a.items) do merged.items[pid] = qty end
    for pid, qty in pairs(b.items) do
        merged.items[pid] = (merged.items[pid] or 0) + qty
    end
    return merged
end

local cart1 = Cart.new():add("J001", 1)
local cart2 = Cart.new():add("T001", 2):add("T002", 1)
local combined = cart1 + cart2

print("Combined cart items:")
for pid, qty in pairs(combined.items) do
    print(string.format("  %s × %d", pid, qty))
end
