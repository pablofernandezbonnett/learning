# DynamoDB Cheatsheet

> Primary fit: `Platform / Growth lane`


Use this after reading [09-dynamodb.md](./09-dynamodb.md).

This is the short version to retain and reopen quickly.

---

## The Core Idea

DynamoDB is not "NoSQL therefore flexible."

It is:

- key-based
- access-pattern-first
- high-throughput
- operationally lightweight

Short version:

> Design the table around the queries, not around the entities.

---

## Use It When

- access patterns are known in advance
- you need simple key lookups or range scans
- throughput is very high
- TTL is useful
- idempotency keys, counters, sessions, or dedupe records fit naturally

---

## Do Not Use It When

- you need joins
- you need ad-hoc queries
- access patterns are still evolving
- relational integrity is the real requirement

In those cases, Postgres is often the better answer.

---

## The 5 Things To Remember

1. **PK/SK is load-bearing.**
   Bad key design hurts both querying and scaling.

2. **GSIs are useful, not free.**
   They increase write cost and complexity.

3. **Hot partitions are the main operational trap.**
   High throughput on one key causes throttling.

4. **Eventually consistent is the default.**
   Strong reads exist, but you choose them deliberately.

5. **DynamoDB and Postgres solve different problems.**
   Do not force one to replace the other.

---

## Good Fits

- idempotency key store
- payment request deduplication
- TTL-backed session state
- rate-limit counters
- high-throughput key lookups

---

## Bad Fits

- finance ledger
- complex reporting
- multi-table relational workflows
- unknown query patterns

---

## Short Answer Shape

Good short answer:

> I would consider DynamoDB when the access patterns are known up front and the workload is
> key-based, high-throughput, and operationally simple. I would avoid it when the
> system needs joins, ad-hoc querying, or strong relational integrity. The main
> design risk is bad partition-key choice, because that hurts both scalability
> and correctness expectations.
