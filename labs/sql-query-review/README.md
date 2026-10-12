# SQL Query Review Lab

Use this after [query optimization](../../topics/databases/08-query-optimization.md). Create your own small PostgreSQL dataset: customers, orders, and order items across several tenants. Include enough rows that a plan choice is visible. The exercise is to explain the plan before and after each change, not to guess which index is best.

## Tasks

1. Query the newest 50 paid orders for one tenant. Capture `EXPLAIN (ANALYZE, BUFFERS)` and identify filtering, sorting, row estimates, and actual work.
2. Add an index matching the filter and sort path. Run the same query and compare plans, time, and write cost.
3. Join those orders to customers. Check whether the join changes the bottleneck.
4. Compare a deep offset page with a page starting after the last seen `(created_at, id)` tuple. Keep the sort order stable.

## Guide

```text
baseline plan -> name expensive operation -> propose one change
             -> rerun same workload -> compare actual evidence
             -> keep or revert the change
```

For the list query, a candidate index starts with equality-filtered columns and then the sort columns, including a tie-breaker. Seek pagination carries the last seen tuple from the prior page; it does not first calculate that tuple with a deep offset query. Indexes speed selected reads but add storage and write work.

Record the query, row counts, plan before and after, and one reason the result might differ with other data distributions. Never infer a production win from a tiny synthetic dataset alone.
