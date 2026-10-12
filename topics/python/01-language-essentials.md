# Python Language Essentials

Python makes small programs quick to write. Keep the data flow visible: use functions and modules by default, add a class when it represents useful state or behavior, and validate data as it enters the program.

## Values and Models

`str` is text; `bytes` is binary data. Encode or decode explicitly at file, network, and message boundaries. Lists and dictionaries are mutable; assigning either to another name does not copy it.

Type hints explain expected values to readers and static checkers, but Python does not enforce function annotations at runtime. Use a `dataclass` for a small internal record; use boundary validation for untrusted input.

```python
from dataclasses import dataclass

@dataclass(frozen=True)
class LineItem:
    sku: str
    price_minor: int
    quantity: int

def line_total(item: LineItem) -> int:
    if item.quantity <= 0:
        raise ValueError("quantity must be positive")
    return item.price_minor * item.quantity
```

Use integer minor currency units or a deliberate decimal policy for money. A type hint alone does not establish that `quantity` is positive.

## Common Edges

- A mutable default argument is created when the function is defined. Use `None` and make a fresh list or dictionary inside when callers should not share state.
- `is None` tests for the `None` singleton. Use `T | None` in annotations when a value may be absent.
- A comprehension is useful for a short transformation or filter. Use an ordinary loop when it makes multi-step behavior clearer.
- `with` makes resource cleanup explicit for files, locks, and clients.
- Catch the specific exception a boundary can handle; use `raise ... from error` when translating a failure so its cause remains available.

## Asynchronous Work

`async` helps when code waits for nonblocking I/O. It does not make CPU-heavy work faster or turn a blocking library into an asynchronous one. Use a consistent sync or async path through a service boundary. Limit concurrent calls to a database or provider, and decide how unfinished work is cancelled.

## Further Reading

- [Python tutorial](https://docs.python.org/3/tutorial/).
- [Type hints](https://docs.python.org/3/library/typing.html).
- [Dataclasses](https://docs.python.org/3/library/dataclasses.html).
- [Asyncio tasks](https://docs.python.org/3/library/asyncio-task.html).
