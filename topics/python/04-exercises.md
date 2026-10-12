# Guided Python Exercises

These exercises ask for a design and checks, not a supplied implementation. Use a few lines of code only when they clarify a language or library operation.

## 1. Reconcile Inventory Exports

You receive `system.csv` with `sku,system_qty` and `count.csv` with `sku,counted_qty`. Produce a report of differences. A missing SKU and a duplicate SKU must be visible rather than silently dropped or multiplied by a join.

```text
read both files with explicit column types
validate required columns, unique SKU keys, nonnegative quantities
compare the complete key sets, including rows present on only one side
for matched SKUs: difference = counted_qty - system_qty
mark differences above a chosen threshold for investigation
write a report with input versions, unmatched rows, and totals
```

If using pandas, `merge(..., validate="one_to_one", indicator=True)` makes join cardinality and unmatched rows visible. Check output row counts and totals before trusting the report. Test empty files, duplicate keys, missing quantities, unknown SKUs, and a rerun with the same inputs. Do not mutate inventory from this report without a separate reviewed action.

## 2. Small Product API

Design `POST /products` and `GET /products/{id}` for a product with SKU, name, and price in integer minor units. Define response and error shapes before choosing FastAPI or another HTTP framework.

```text
POST:
  validate request shape and domain rules
  enforce unique SKU in the store
  create product and return 201 with stable identifier
GET:
  validate identifier
  fetch product or return documented 404
```

Keep request parsing in the route and uniqueness in the service/store boundary. An in-memory dictionary is enough to explore behavior, but it loses data on restart and cannot enforce uniqueness across instances. Use a durable unique constraint for a real service.

Check valid creation and retrieval, malformed input, nonpositive price, duplicate SKU, unknown ID, and simultaneous creations with the same SKU. Add authorization, request limits, and operational endpoints according to the actual deployment, using the [API block](../api/README.md) for the general design method.

## Further Reading

- [pandas merge](https://pandas.pydata.org/docs/reference/api/pandas.DataFrame.merge.html) for cardinality validation and unmatched rows.
- [FastAPI tutorial](https://fastapi.tiangolo.com/tutorial/) if you choose that framework.
