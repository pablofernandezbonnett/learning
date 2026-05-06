# Zero-Downtime Deployments

> Primary fit: `Shared core`


The goal: deploy a new version of your backend (`v1` → `v2`) without telling users
"The application will be unavailable from 2AM to 4AM for maintenance." That message
means your infrastructure needs work.

Zero-downtime requires container orchestration (Kubernetes/ECS) and three deployment
strategies — plus one hard problem: the database.

---

## Why This Matters

Deployment strategy is not just an operations detail. It directly affects user
experience, rollback safety, and whether a schema or application change becomes
an incident.

This matters because many systems can deploy new containers easily but still
cause downtime through incompatible versions, bad health checks, or destructive
database changes.

## Smallest Mental Model

Zero-downtime deployment means old and new versions can overlap safely while
traffic keeps flowing.

That usually depends on two things:

- traffic moves gradually or safely between versions
- the application and database stay compatible during the rollout window

## Bad Mental Model vs Better Mental Model

Bad mental model:

- zero downtime means Kubernetes restarts pods one by one
- once the rollout strategy is configured, the problem is mostly solved
- database migration is separate from deployment strategy

Better mental model:

- zero downtime is a compatibility problem first and a rollout-tooling problem
  second
- old and new versions often coexist, so contracts and schemas must survive
  overlap
- the database is usually the sharpest edge in the whole release

Small concrete example:

- weak approach: rename a column and deploy new code in one release because the
  app rollout is "rolling"
- better approach: expand the schema first, let both app versions coexist,
  backfill safely, and only contract later

Strong default:

- use rolling updates as the normal default
- use canary or blue/green when blast radius or rollback speed matters more
- always treat destructive schema change as a multi-step rollout

Interview-ready takeaway:

> I treat zero-downtime release as a compatibility problem. The app rollout
> strategy matters, but the critical question is whether old and new code can
> safely coexist with the database during the rollout window.

---

## 1. Rolling Updates

The default Kubernetes strategy. Simple, no extra cost.

**How it works:**
Assume 4 Pods of `payment-v1` sit behind a Load Balancer.

1. K8s starts one new `payment-v2` Pod.
2. When the new Pod passes its Readiness Probe ("I am ready for traffic"), K8s adds it to
   the load balancer and immediately terminates one old `payment-v1` Pod. You now have
   3×v1 + 1×v2.
3. Repeat until 4×v2 are running.

**Pros:** Built-in to K8s, zero cost, no extra infrastructure.

**Cons:** During the rollout, v1 and v2 serve traffic simultaneously. Both versions must
be **backward-compatible** (API contracts, DB schema). Rollback is a second rolling
update — not instant.

```yaml
# Deployment — tune the rollout window
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1        # allow 1 extra Pod above desired count during rollout
    maxUnavailable: 0  # never go below desired count (no capacity loss)
```

---

## 2. Blue/Green Deployments

Full environment duplication. Maximum safety, highest cost.

**How it works:**

- **Blue** = current live environment (v1), receiving 100% of production traffic.
- **Green** = new environment (v2), deployed in isolation, receiving no traffic.

1. Bring up a full Green (v2) environment — identical server count, same config.
2. Run private E2E tests directly against Green.
3. At launch time: flip the Load Balancer / DNS / VPC router so 100% of traffic instantly
   moves to Green.
4. Leave Blue running for a few hours as a hot rollback option.
5. Once confident, decommission Blue.

**Pros:** Rollback is a single switch flip — instant, zero downtime.

**Cons:** You pay double the infrastructure cost during the transition window.

**When to use:** Major version releases, database migrations, payment path changes where
partial rollout is unacceptable.

---

## 3. Canary Releases

Progressive traffic shifting with real-user validation.

**How it works:**

1. Deploy v2 alongside v1, but route only **5% of traffic** to v2.
2. Monitor error rate (HTTP 5xx), p95 latency, and business metrics on dashboards.
3. If healthy → gradually increase: 5% → 20% → 50% → 100%.
4. If the canary emits errors → abort. Return 100% to v1. Only 5% of users were affected.

**Tooling:** Kubernetes `Ingress` weight annotations, AWS ALB weighted target groups,
Argo Rollouts (`canarySteps`), Istio traffic splitting.

**Pros:** Real user validation. Blast radius is small if v2 has a bug.

**Cons:** v1 and v2 serve traffic simultaneously — same backward-compatibility requirement
as Rolling Updates.

---

## 4. The Hard Problem: Database Migrations

None of the above strategies survive a destructive schema change.

**The failure scenario:**
A developer renames a column: `user_email` → `primary_email`. The migration runs.
All v1 Pods (still in rotation during a Rolling Update, or the live Blue in Blue/Green)
immediately crash — they query `user_email` which no longer exists.
You just caused downtime despite using a "zero-downtime" deployment strategy.

### The Expand-and-Contract Pattern

All destructive schema changes must be split into at least 3 separate deployments.

**Phase 1 — Expand (DDL only):**
Add the new column. Leave the old column untouched.
```sql
ALTER TABLE users ADD COLUMN primary_email VARCHAR(255);
```
No code change. Both v1 (reading `user_email`) and v2 (reading `primary_email`) can
coexist with this schema.

**Phase 2 — Dual-Write (code change):**
Deploy v2. v2 writes to **both** columns on every save. This ensures v1 Pods still
in rotation continue to read valid data from `user_email`.
```kotlin
user.userEmail = email       // legacy — keeps v1 happy
user.primaryEmail = email    // new — v2 reads this
```

**Phase 3 — Backfill (data migration):**
Run a background job (batch or cron) to copy historical values:
```sql
UPDATE users SET primary_email = user_email WHERE primary_email IS NULL;
```
Run in small batches with `LIMIT` + `OFFSET` or keyset pagination to avoid lock contention.

**Phase 4 — Contract (cleanup):**
Once all Pods run v2 and the backfill is complete, drop the legacy column:
```sql
ALTER TABLE users DROP COLUMN user_email;
```

### Flyway and Liquibase — Automated Migration Tooling

Running `psql -f migration.sql` manually is error-prone and not reproducible. Production
databases need versioned, audited, and automated migrations.

**Flyway (simpler, SQL-first):**
- Migrations are plain `.sql` files named `V1__initial_schema.sql`, `V2__add_primary_email.sql`.
- Flyway tracks applied versions in a `flyway_schema_history` table.
- Spring Boot auto-runs pending migrations on startup (`spring.flyway.enabled=true`).

```
src/main/resources/db/migration/
  V1__create_users.sql
  V2__add_primary_email.sql      ← Phase 1 expand
  V3__backfill_primary_email.sql ← Phase 3 data backfill
  V4__drop_user_email.sql        ← Phase 4 contract
```

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false    # fail fast if schema history is missing
```

**Liquibase (more powerful, XML/YAML/JSON/SQL):**
- Uses a `changelog` file describing changesets.
- Supports rollback definitions (`<rollback>` tag), tagging releases, diff generation.
- Preferred when you need cross-DB portability or complex rollback scripts.

```yaml
# db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - changeSet:
      id: 2-add-primary-email
      author: pablo
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: primary_email
                  type: varchar(255)
      rollback:
        - dropColumn:
            tableName: users
            columnName: primary_email
```

**Rule:** Use one tool per project, never both. Flyway for greenfield Spring Boot projects
(less configuration). Liquibase when you need auditable rollback plans or multi-DB support.

---

## 5. Practical Summary

*"For zero-downtime deployments, I use Kubernetes **Rolling Updates** as the default
strategy — it is free and built-in. For high-risk releases (payment flow, major API
changes), I apply **Canary releases**, routing 5% of traffic to v2 and monitoring error
rate and p95 latency before fully promoting.*

*The database is the real challenge. Any destructive schema change goes through the
**Expand-and-Contract** pattern: expand the schema, dual-write in v2, backfill historical
data, then drop the old column in a fourth deployment — days or weeks later. This guarantees
v1 and v2 Pods can coexist without schema conflicts at any point during the rollout.*

*Migrations are automated with **Flyway** (or Liquibase for complex rollbacks) — versioned
SQL files applied automatically at startup, tracked in `flyway_schema_history`. No manual
`ALTER TABLE` runs in production."*
