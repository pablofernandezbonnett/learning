# JPA and Hibernate: See the SQL Behind the Entities

Jakarta Persistence, often still called JPA, defines the persistence API.
Hibernate is a common implementation; Spring Data adds repository
conveniences. The object model is useful for changing business state, but a
small amount of entity navigation can produce many SQL statements or load far
more data than a request needs. Check the generated work, not only the
repository call.

## Count Queries Across a Whole Use Case

```kotlin
val orders = orderRepository.findRecentOrders()
val names = orders.map { it.customer.name }
```

If `customer` is fetched separately for each of 50 orders, one list query can
lead to many additional selects: the **N+1** pattern. A single order detail
with one extra select is a hidden query, but it is not N+1 until that access
repeats across a list. Cache contents and mapping settings can change the
exact count, so inspect the SQL for the actual use case.

Choose the fetch shape from what the caller needs:

| Need | Candidate | Check |
|---|---|---|
| One detail with a needed to-one relation | Fetch join or entity graph | Only the required relations load |
| A list of a few fields | DTO or projection query | Selected columns and row count stay bounded |
| A list with collections | Batch fetch or a two-step read | Query count, row multiplication, and page size |

Making every relation eager can move the cost to unrelated requests. A fetch
join across a collection can multiply SQL rows; combining it with pagination
needs particular care because behavior and cost depend on query shape,
database, and Hibernate version. Inspect the generated SQL and measured plan
before treating one strategy as a default.

For Spring Data projection syntax, see [Spring Data](./13-spring-data.md).
For diagnosing an expensive statement, use
[Query optimization](../databases/08-query-optimization.md).

## End Lazy Loading Before the API Boundary

A lazy association loads when accessed, if the persistence context can still
load it. Returning a JPA entity from a controller lets JSON serialization
decide which associations to traverse. That can issue unexpected queries when
the context is open or fail with `LazyInitializationException` when it is
closed.

Build the response inside an explicit service read boundary. Fetch the fields
or associations the response needs, then map them to an API DTO. Do not use
serialization annotations such as `@JsonView` as a substitute for choosing a
database read shape. A DTO also keeps persistence mappings out of the public
API contract.

## Control the Persistence Context on Writes

Hibernate tracks managed entities in a **persistence context**. Changing a
managed entity can become an SQL update during a **flush**, which synchronizes
pending changes with the database. A flush may happen before a query or at
commit; it is not itself a commit. The transaction can still roll back.

This is convenient for a small aggregate update. For a large import, one
transaction that persists thousands of entities can hold a growing context
and delay substantial work until flush. Process bounded chunks, measure JDBC
batching, and flush and clear the context when appropriate. Clearing detaches
entities, so later changes to those objects will not be tracked automatically.

Bulk JPQL or native updates act directly on database rows and can leave
already-managed entities stale in the current context. Refresh, clear, or
start a new context before relying on those objects again.

## Use the Right Read or Write Tool

| Work | Starting choice |
|---|---|
| Change one aggregate under business rules | Managed entity and short transaction |
| Return a small list or report | Projection, DTO query, or explicit SQL |
| Update many rows with the same rule | Bulk query, with context synchronization planned |
| Import many distinct rows | Chunked persistence or a bulk data path, measured on real volume |

The point is to keep the SQL and object lifecycle understandable. For joins,
aggregation, and windows, see [SQL refresh](../databases/03-sql-refresh-for-backend-engineers.md).
For local transaction scope and remote calls, see
[Transactions in Spring](./03-transactions-and-isolation.md).

## Verify One Representative Request

1. Capture the SQL and count statements for the full service request.
2. Check selected columns, rows returned, and association loads.
3. For a list, increase page size in a test and look for query-count growth.
4. For writes, inspect flush timing, managed-entity count, and batch behavior.
5. Compare latency and database work after changing one fetch or write path.

## Further Reading

- [Hibernate ORM user guide](https://docs.hibernate.org/orm/current/userguide/html_single/)
- [Hibernate fetching guidance](https://docs.hibernate.org/orm/current/introduction/html_single/)
