# 16. Flyway and Schema Migrations

> Primary fit: `Shared core`


If a Spring Boot service owns a relational schema, schema evolution is part of
the backend job. Flyway is one of the most common ways to make those changes
versioned, repeatable, and reviewable.

---

## 1. What Flyway Is

Flyway is a database migration tool.

In practice, it gives you:

- versioned schema changes in Git
- a clear order of execution
- startup-time migration support
- a history table that records what ran

The basic model is simple:

- you create migration files
- Flyway checks what already ran
- Flyway applies only the pending migrations

That is much safer than manual `ALTER TABLE` runs in production.

---

## 2. Why It Matters in Spring Boot

For backend work, Flyway usually means:

- JPA (Java Persistence API) / Hibernate should not own schema evolution in production
- schema changes should be explicit and reviewed
- every environment should move forward the same way

Good rule:

> Use Hibernate for mapping. Use Flyway for schema changes.

If you let Hibernate "update" schemas automatically in production, you lose too
much control over ordering, rollbacks, and operational safety.

---

## 3. Default Spring Boot Shape

Typical setup:

- dependency on `flyway-core`
- database-specific module when needed, for example PostgreSQL
- migrations under `src/main/resources/db/migration`

Typical naming:

- `V1__create_orders_table.sql`
- `V2__add_sync_status_to_products.sql`
- `V3__backfill_order_source.sql`

Typical config:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

Flyway records applied migrations in `flyway_schema_history`.

---

## 4. What a Good Migration Looks Like

A good migration is:

- small
- explicit
- safe to review
- easy to reason about under load

Good examples:

- add a nullable column
- add an index
- create a new table
- backfill data in controlled steps

Riskier examples:

- large blocking table rewrites
- destructive column drops in the same release that introduces the replacement
- application and schema changes that cannot coexist during rollout

---

## 5. Expand and Contract

This is the most important production pattern to pair with Flyway.

Example:

1. expand: add the new column or table
2. deploy app code that can read/write both shapes
3. backfill old data
4. switch reads fully to the new shape
5. contract: drop the old column later

Why it matters:

- old and new app versions can coexist
- rolling deployments stay safe
- database changes stop being the sharpest edge in the release

For commerce systems, this matters a lot around:

- orders
- payments
- inventory
- customer identity

---

## 6. Flyway vs Liquibase

Flyway:

- simpler
- SQL-first
- easier to explain and review
- usually enough for typical Spring Boot services

Liquibase:

- more configurable
- richer change-log model
- better if you truly need rollback metadata or heavier DB-process tooling

Practical rule:

> Prefer Flyway unless the project has a clear reason to need Liquibase.

---

## 7. Common Mistakes

1. using Flyway and `schema.sql`/`data.sql` together for the same purpose

`schema.sql` and `data.sql` are simple initialization mechanisms.
Flyway is a versioned migration mechanism.

If both are trying to create or mutate the same tables, you lose one clear source of truth.
That creates drift, duplicate DDL (schema-changing SQL such as `CREATE TABLE` or `ALTER TABLE`), and startup-order confusion.

Practical rule:

- use Flyway for schema evolution
- use `data.sql` only for small local/dev seed data if the team really needs it
- do not let two mechanisms compete to define the same production schema

2. relying on `ddl-auto=update` in production

`ddl-auto=update` sounds convenient, but it means Hibernate decides schema changes at app startup.
That removes reviewable migration files and makes production changes harder to reason about.

Why this is risky:

- the change is not explicit in Git as a migration step
- rollout ordering is less clear
- destructive or surprising changes are harder to audit
- different environments can drift more easily

Short rule:

- `ddl-auto=update` can be acceptable for local experiments
- production schema changes should be explicit Flyway migrations

3. putting large data migrations into startup scripts with no operational plan

Schema migrations and data migrations are not always the same operational problem.
Adding a column is usually cheap compared with backfilling 100 million rows.

If you put a huge backfill into app startup with no plan, you risk:

- slow startup or deployment timeouts
- lock contention
- overloaded databases during release
- half-finished operational work with poor visibility

Better pattern:

- use Flyway for the schema step
- run large data backfills in controlled batches or dedicated jobs
- decide in advance how you will monitor progress and failure

4. writing destructive migrations before the app is compatible with the new shape

This is the classic mistake that breaks rolling deployments.

Bad sequence:

1. drop old column
2. deploy code that still reads it

That fails as soon as old code and new schema overlap.

Safer sequence:

1. add the new shape first
2. deploy code that supports both old and new
3. backfill and switch reads
4. drop old shape later

That is exactly why expand-and-contract matters.

5. skipping index review when adding new query paths

A schema change is not finished when the table or column exists.
If the release also adds a new lookup path, filter, sort, or join pattern, index impact must be reviewed too.

Otherwise the feature may be "correct" but slow in production.

Typical example:

- you add `status` and `created_at` filters to an orders query
- the query works in dev
- production traffic turns it into a scan problem because no supporting index exists

Short rule:

- every new important read path should trigger an index review
- migrations are about data shape and query performance, not only DDL correctness

---

## 8. Interview Lines

These lines are stronger if you know what they actually mean:

1. "I prefer Flyway for Spring Boot services because it keeps schema evolution explicit, versioned, and reviewable."

What you mean:

- schema changes live in Git as ordered migration files
- reviewers can inspect exactly what will hit the database
- every environment can move forward using the same steps

2. "For production changes, the key idea is not just the migration tool. It is pairing Flyway with expand-and-contract so old and new versions can coexist safely."

What you mean:

- the migration tool alone does not make deployments safe
- safety comes from sequencing schema and app changes so multiple versions can run at the same time
- this matters in rolling deploys, blue-green deploys, and any environment where not all instances switch at once

3. "Hibernate maps the model, but I do not want Hibernate inventing production schema changes for me."

What you mean:

- object-relational mapping (ORM) and production schema evolution are different responsibilities
- Hibernate is good at mapping entities to tables
- Flyway is better for explicit, reviewable, operationally safe database changes

Interview-safe combined answer:

> I use Flyway so schema changes are explicit, versioned, and reviewable. For production releases, I pair it with expand-and-contract so old and new app versions can coexist safely. Hibernate can map the model, but I do not want automatic ORM-driven schema changes deciding production rollout behavior for me.

---

## 9. Further Reading

- Spring Boot database initialization:
  https://docs.spring.io/spring-boot/how-to/data-initialization.html
- Flyway getting started:
  https://documentation.red-gate.com/flyway/getting-started-with-flyway/getting-started
- Flyway API quickstart:
  https://documentation.red-gate.com/flyway/getting-started-with-flyway/quickstart-guides/quickstart-api
