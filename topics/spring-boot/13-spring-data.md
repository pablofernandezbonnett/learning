# Spring Data: Use Repositories Without Hiding Data Access

Spring Data creates repository implementations from interfaces. It removes
routine data-access code, but each store still has its own query and
consistency behavior. Use a repository while the operation remains clear;
inspect or write the query when its cost or behavior matters.

This note uses Spring Data JPA examples. For database choice, start with
[Choose a data store](../databases/04-sql-vs-nosql.md).

## Start With Simple Repository Methods

```java
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findFirstByOrderByCreatedAtDesc();
}
```

Spring Data derives these queries from the method names. Use this for simple
lookups and existence checks. When a method name grows into a description of
several joins and filters, make the query explicit so its shape is visible.
`existsByEmail` expresses an existence check directly; verify the generated
query and index if the path is important.

## Return the Shape the Caller Needs

A closed interface projection can return a small read shape without loading
an entity for every row:

```java
interface UserSummary {
    String getName();
    String getEmail();
}

interface UserRepository extends JpaRepository<User, Long> {
    List<UserSummary> findByRole(UserRole role);
}
```

For a derived JPA query, Spring Data can limit selection to the projection's
top-level properties. A nested property may require a join that materializes
more of the related object. Inspect the SQL instead of assuming every
projection reads only the declared fields. For fetch plans, lazy loading, and
N+1, see [JPA and Hibernate](./04-jpa-hibernate-performance-traps.md).

## Compose Optional Filters

A JPA `Specification` represents a reusable query predicate. It is useful
when a search screen has optional filters that would create many derived
method variants. Add `JpaSpecificationExecutor<User>` to the repository,
then compose only the filters supplied by the caller:

```java
Specification<User> filters = isActive();
if (role != null) {
    filters = filters.and(hasRole(role));
}
Page<User> users = userRepository.findAll(filters, PageRequest.of(0, 20));
```

Here, `isActive()` and `hasRole(role)` are small methods returning
`Specification<User>`. Keep pagination and the resulting joins under review:
composition makes filters easier to maintain, but it does not make a costly
query cheap.

## Record Who and When

Spring Data JPA auditing can fill creation and modification timestamps with
`@CreatedDate` and `@LastModifiedDate`. Add an auditing listener to the
entity and enable JPA auditing in configuration. If you also record the actor
with `@CreatedBy` or `@LastModifiedBy`, provide an `AuditorAware` that
resolves the current principal. Decide how background jobs represent their
actor; a hard-coded user in sample configuration is not a production policy.

Auditing is metadata, not a substitute for an application event or immutable
history when changes must be reconstructed.

## Leave the Repository Abstraction When Needed

| Work | Starting choice |
|---|---|
| Simple entity lookup or update | Repository method |
| Small read response | Projection or explicit DTO query |
| Optional JPA filters | Specifications with query review |
| Reporting, bulk change, or database-specific SQL | Explicit JPQL, SQL, or a lower-level data-access tool |

The repository API does not make JPA, MongoDB, and Redis interchangeable.
For MongoDB, use repositories for straightforward document access and
`MongoTemplate` when the operation needs explicit updates or aggregation.
For Redis key operations, use `RedisTemplate`; Spring Cache serves simple
method-level caching. Choose the store from its data and failure model first.
For their storage models, see [MongoDB](../databases/07-mongodb-in-depth.md)
and [Redis](../databases/06-redis-in-depth.md).

## Further Reading

- [Spring Data JPA projections](https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html)
- [Spring Data JPA specifications](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html)
- [Spring Data JPA auditing](https://docs.spring.io/spring-data/jpa/reference/auditing.html)
