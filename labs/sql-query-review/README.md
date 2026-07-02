# SQL Query Review Lab

This lab is the runnable companion for fast SQL performance review.

Why this lab matters:

- query tuning is easier to retain when you can run the query, inspect the
  plan, add the index, and compare the result
- this lab stays small enough to reopen in one sitting
- it supports the repo goal of stronger backend SQL judgment without turning
  the repo into a DBA playground

Use it after:

- [../../topics/databases/13-explain-indexes-and-query-review-baseline.md](../../topics/databases/13-explain-indexes-and-query-review-baseline.md)
- [../../topics/databases/08-query-optimization.md](../../topics/databases/08-query-optimization.md)
- [../../topics/databases/10-postgres-in-depth.md](../../topics/databases/10-postgres-in-depth.md)

## What This Lab Covers

- a filtered-and-sorted list query with no supporting composite index
- `EXPLAIN (ANALYZE, BUFFERS)` before and after the index
- offset pagination versus seek pagination
- a small join query worth checking in a plan

## Files

- [01-schema-and-seed.sql](./01-schema-and-seed.sql): schema plus sample data
- [02-explain-baseline.sql](./02-explain-baseline.sql): first plan checks before tuning
- [03-index-and-recheck.sql](./03-index-and-recheck.sql): composite index plus the same plan again
- [04-pagination-comparison.sql](./04-pagination-comparison.sql): offset versus seek pagination

## How To Run

Strong default:

1. use any local PostgreSQL instance you already have
2. create a disposable database
3. run the files in order with `psql`

Example shape:

```bash
createdb learning_sql_lab
psql learning_sql_lab -f 01-schema-and-seed.sql
psql learning_sql_lab -f 02-explain-baseline.sql
psql learning_sql_lab -f 03-index-and-recheck.sql
psql learning_sql_lab -f 04-pagination-comparison.sql
```

If you prefer Docker and already use it, a temporary local database is also
fine. The lab does not require any repo-local wrapper or build tool.

## What To Look For

- did the first list query sort too many rows or scan too widely?
- does the composite index match the real filter and sort path?
- does deep offset pagination keep doing work that seek pagination avoids?
- does the join plan look reasonable for the row counts involved?

## Strong Default

- run one file at a time
- read the plan before changing the schema
- after adding the index, ask what write cost you just accepted
- keep the mental model on evidence, not on optimizer mythology
