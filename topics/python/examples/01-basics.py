"""
Python 3 Refresher — for Java/Kotlin developers
Run: python examples/01-basics.py
"""

# ─── 1. TYPE HINTS ────────────────────────────────────────────────────────────
# Python is dynamically typed but type hints (PEP 484) make code readable
# and enable IDE autocompletion. They are NOT enforced at runtime.

def calculate_discount(price: float, rate: float) -> float:
    return price * (1 - rate)

# Union types — Python 3.10+ syntax (use Optional[str] on older versions)
def find_sku(product_id: int) -> str | None:
    catalogue = {1: "UT-WHITE-M", 2: "UT-BLACK-L"}
    return catalogue.get(product_id)  # returns None if not found — no Optional.orElse()

print(calculate_discount(2990, 0.2))   # 2392.0
print(find_sku(1))                     # UT-WHITE-M
print(find_sku(99))                    # None


# ─── 2. F-STRINGS ─────────────────────────────────────────────────────────────
# Like Kotlin string templates. Always use f-strings over % formatting or .format()

sku = "UT-WHITE-M"
price = 2990
stock = 42

print(f"SKU: {sku} | Price: ¥{price:,} | Stock: {stock}")
# Expressions inside: f"{price * 1.1:.2f}" → "3289.00"
print(f"Price with tax: ¥{price * 1.1:.2f}")


# ─── 3. DATACLASSES ───────────────────────────────────────────────────────────
# Like Kotlin data class — auto-generates __init__, __repr__, __eq__

from dataclasses import dataclass, field
from datetime import datetime

@dataclass
class Product:
    sku: str
    name: str
    price: float
    category: str
    active: bool = True                          # default value
    tags: list[str] = field(default_factory=list)  # mutable default — never use []

    def discounted_price(self, rate: float) -> float:
        return self.price * (1 - rate)

    # Kotlin equivalent: fun discountedPrice(rate: Double) = price * (1 - rate)

p = Product(sku="UT-001", name="Uniqlo T-Shirt", price=2990.0, category="tops")
print(p)
print(p.discounted_price(0.2))  # 2392.0

# Frozen dataclass = immutable (like Kotlin val data class)
@dataclass(frozen=True)
class Money:
    amount: float
    currency: str = "JPY"

    def __add__(self, other: "Money") -> "Money":
        assert self.currency == other.currency, "Currency mismatch"
        return Money(self.amount + other.amount, self.currency)

total = Money(2990) + Money(1990)
print(total)  # Money(amount=4980, currency='JPY')


# ─── 4. LIST COMPREHENSIONS ───────────────────────────────────────────────────
# Kotlin: items.filter { it.active }.map { it.price }
# Python: [item.price for item in items if item.active]

products = [
    Product("SKU-1", "T-Shirt",  2990.0, "tops"),
    Product("SKU-2", "Jeans",    6990.0, "bottoms"),
    Product("SKU-3", "Jacket",   9990.0, "outerwear", active=False),
    Product("SKU-4", "Socks",     990.0, "accessories"),
]

# Filter + map in one line
active_prices = [p.price for p in products if p.active]
print(active_prices)  # [2990.0, 6990.0, 990.0]

# Dict comprehension — build a lookup map (like associateBy in Kotlin)
sku_map: dict[str, Product] = {p.sku: p for p in products}
print(sku_map["SKU-2"].name)  # Jeans

# Nested comprehension — flatten categories
all_tags = [tag for p in products for tag in p.tags]  # flat list of all tags


# ─── 5. CONTEXT MANAGERS (with) ───────────────────────────────────────────────
# Like Java try-with-resources. Guarantees cleanup even on exception.

# File I/O — always use 'with'
import json, tempfile, os

data = {"sku": "UT-001", "price": 2990}
with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
    json.dump(data, f)
    temp_path = f.name

with open(temp_path) as f:
    loaded = json.load(f)
    print(loaded)  # {'sku': 'UT-001', 'price': 2990}

os.unlink(temp_path)  # cleanup

# Custom context manager with contextlib
from contextlib import contextmanager

@contextmanager
def timed_operation(name: str):
    start = datetime.now()
    try:
        yield                          # body of the 'with' block runs here
    finally:
        elapsed = (datetime.now() - start).total_seconds()
        print(f"[{name}] took {elapsed:.3f}s")

with timed_operation("product lookup"):
    result = sku_map.get("SKU-1")


# ─── 6. ERROR HANDLING ────────────────────────────────────────────────────────
# No checked exceptions. Catch specific exception types.

class ProductNotFoundError(Exception):
    """Raised when a SKU cannot be found in the catalogue."""
    def __init__(self, sku: str):
        super().__init__(f"Product not found: {sku}")
        self.sku = sku

def get_product(sku: str) -> Product:
    product = sku_map.get(sku)
    if product is None:
        raise ProductNotFoundError(sku)
    return product

try:
    p = get_product("INVALID")
except ProductNotFoundError as e:
    print(f"Error: {e}")             # Error: Product not found: INVALID
except Exception as e:
    print(f"Unexpected error: {e}")  # catch-all, like Exception in Java
finally:
    print("Lookup attempted")        # always runs


# ─── 7. GENERATORS ────────────────────────────────────────────────────────────
# Lazy sequences — like Kotlin Sequences or Java Streams.
# Use when processing large datasets you don't want to load fully into memory.

def active_products(source: list[Product]):
    """Yields products one at a time — memory efficient for large catalogues."""
    for product in source:
        if product.active:
            yield product  # pauses here, resumes on next()

# The generator is lazy — nothing runs until you iterate
gen = active_products(products)
for p in gen:
    print(f"  Active: {p.sku}")

# Generator expression (inline) — like a lazy list comprehension
prices_gen = (p.price for p in products if p.active)
total = sum(prices_gen)  # consumes the generator
print(f"Total active inventory value: ¥{total:,.0f}")


# ─── 8. MATCH / CASE (Python 3.10+) ──────────────────────────────────────────
# Like Kotlin when — exhaustive pattern matching

@dataclass
class OrderEvent:
    type: str
    order_id: str
    payload: dict

def handle_event(event: OrderEvent) -> str:
    match event.type:
        case "ORDER_PLACED":
            return f"New order {event.order_id} — reserve stock"
        case "ORDER_PAID":
            return f"Order {event.order_id} paid — trigger fulfilment"
        case "ORDER_CANCELLED":
            return f"Order {event.order_id} cancelled — release stock"
        case _:                         # default (like else in when)
            return f"Unknown event: {event.type}"

e = OrderEvent("ORDER_PAID", "ORD-001", {"amount": 2990})
print(handle_event(e))  # Order ORD-001 paid — trigger fulfilment


# ─── 9. DECORATORS ────────────────────────────────────────────────────────────
# Like Java/Kotlin annotations but more powerful — they are plain functions
# that wrap other functions. FastAPI, Flask, and many frameworks use them.

import functools
import time

def retry(max_attempts: int = 3, delay: float = 0.1):
    """Decorator that retries a function on exception."""
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            last_error = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except Exception as e:
                    last_error = e
                    print(f"Attempt {attempt} failed: {e}")
                    if attempt < max_attempts:
                        time.sleep(delay)
            raise last_error
        return wrapper
    return decorator

@retry(max_attempts=2, delay=0.0)
def unreliable_api_call(fail: bool) -> str:
    if fail:
        raise ConnectionError("Timeout")
    return "OK"

try:
    print(unreliable_api_call(fail=True))
except ConnectionError:
    print("All retries exhausted")


# ─── 10. ASYNC / AWAIT ────────────────────────────────────────────────────────
# Like Kotlin coroutines. asyncio is the standard library event loop.
# Useful for I/O-bound work: HTTP calls, DB queries, file I/O.

import asyncio

async def fetch_stock(sku: str) -> int:
    """Simulates an async DB/API call."""
    await asyncio.sleep(0.01)      # non-blocking wait (like delay() in Kotlin)
    return {"UT-001": 42, "UT-002": 0}.get(sku, -1)

async def check_multiple_skus(skus: list[str]) -> dict[str, int]:
    # Run all fetches concurrently — like Kotlin's async { } + awaitAll()
    tasks = [fetch_stock(sku) for sku in skus]
    results = await asyncio.gather(*tasks)
    return dict(zip(skus, results))

stock = asyncio.run(check_multiple_skus(["UT-001", "UT-002", "UT-999"]))
print(stock)  # {'UT-001': 42, 'UT-002': 0, 'UT-999': -1}

print("\n✓ All basics examples completed")
