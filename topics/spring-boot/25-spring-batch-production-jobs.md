# Spring Batch Jobs That Can Fail Safely

> Primary fit: `Growth / Backend platforms with imports, settlement, reporting, or scheduled work`

Spring Batch is useful when a backend must process a bounded set of records
reliably: a daily settlement file, a supplier import, a backfill, or a report.
It is not a reason to turn every asynchronous action into a batch job.

The important skill is not knowing every `ItemReader` implementation. It is
being able to answer these operational questions before the first run:

- which business input does this run own?
- what is safe to retry, skip, or restart?
- how do we prove what happened to one record?
- how do we stop a duplicate run from producing duplicate business effects?

---

## Why This Matters

Batch failures are often discovered late:

- a settlement file was only half imported
- a scheduled job ran twice after a deployment
- one malformed record stopped a six-hour process
- an operator restarts a job and repeats an external side effect

Spring Batch gives useful runtime machinery for transactions, execution
metadata, restartability, and item counts. It does not decide which records are
safe to repeat or whether a missing payment is acceptable. Those are application
and business decisions.

## Smallest Useful Mental Model

A production batch job is a durable workflow over a known input.

```text
business input -> reader -> processor -> writer -> durable business result
                         \-> JobRepository records progress and outcome
```

The common shape is *chunk processing*:

1. read a small group of records
2. validate or transform them
3. write that group in one transaction
4. save progress before moving on

A `chunk` is therefore a transaction and recovery boundary, not merely a
performance setting.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- a batch job is a `for` loop that happens to run at night
- restarting means running the same code again
- `skip` means the framework has dealt with a bad record
- Batch metadata makes downstream writes idempotent automatically

Better mental model:

- a job is an explicitly identified business run with durable execution state
- a restart continues a failed run only when its reader, writer, and data model
  can support that safely
- every skipped record needs a business disposition: reject, quarantine, or
  reconcile
- the business writer still needs uniqueness and idempotency rules

Strong default:

> Start with one single-threaded, restartable job, a JDBC-backed
> `JobRepository`, stable business parameters, and an idempotent writer. Scale
> only after measuring the slowest real step.

---

## 1. When Spring Batch Fits

Good fits:

- nightly settlement, invoicing, reconciliation, or report generation
- a supplier CSV import with a defined file and a reconciliation result
- a controlled backfill over existing database records
- a scheduled aggregation where one run has a clear business date or cutoff

Poor fits:

- a user waiting for one request response
- an unbounded stream where Kafka or another broker is the real runtime model
- one small delayed action that a queue worker can handle more simply
- work with no clear input boundary, owner, or completion condition

| Need | Better first shape |
| --- | --- |
| User must receive the result now | Normal request/response service |
| Work happens per event and should scale continuously | Queue or stream consumer |
| Process a known file, date range, or backfill safely | Spring Batch job |
| One short periodic cleanup | Scheduled task; promote to Batch only when recovery and audit matter |

Do not call a `@Scheduled` loop a batch system just because it runs nightly.
Use Spring Batch when durable execution state, restart, counts, and controlled
failure handling provide real value.

---

## 2. The Vocabulary You Need in an Incident

| Term | Practical meaning |
| --- | --- |
| `Job` | The reusable workflow definition, such as `importSettlements`. |
| `JobInstance` | One logical business run, identified by the job name and identifying parameters. |
| `JobExecution` | One attempt to run that instance. A failed instance can have another execution on restart. |
| `Step` | One named stage inside the job, such as parse, validate, or write. |
| `StepExecution` | The recorded attempt for one step, including read, write, filter, and skip counts. |
| `ExecutionContext` | Persisted state used to continue safely, for example the last processed position. |
| `JobRepository` | The storage for Batch execution metadata; normally JDBC-backed in production. |

The default JDBC metadata tables start with `BATCH_`. They are operational data,
not application business truth. Do not edit them manually to make a dashboard
look green or to force a restart.

---

## 3. A Small, Realistic Example: Daily Settlement Import

Assume that a payment provider delivers one file per business date. Every row
has a stable provider settlement ID. The job must create a local settlement
record exactly once, report malformed rows, and be restartable after a database
or process failure.

The input identity should be stable and meaningful:

```text
job name:       importSettlements
businessDate:   2026-08-07
sourceFile:     provider-settlements-2026-08-07.csv
fileChecksum:   4d2c...
```

Do not use the current timestamp as the only parameter just to make every
launch succeed. That creates a different `JobInstance`, which defeats the
normal restart path and can hide duplicate work.

### Minimal Spring Batch configuration

For a Spring Boot application, add the Batch starter and use a real relational
database for the job repository and business writes. Keep automatic job launch
off for a normal HTTP service unless startup is deliberately the scheduler.

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")
    runtimeOnly("org.postgresql:postgresql")
}
```

```yaml
spring:
  batch:
    job:
      enabled: false # an API instance should not accidentally start a job on deploy
```

With Spring Boot auto-configuration, a single `Job` bean can otherwise be run
at application startup. Choose one explicit trigger: a scheduler, an operator
endpoint with proper authorization, or a platform job runner.

### Job and step shape

This example uses the Spring Batch 5-style builders used with Spring Boot 3.x.
The reader and writer are intentionally left behind interfaces: their failure
and idempotency rules matter more than a long configuration listing.

```java
@Configuration
class SettlementBatchConfiguration {

    @Bean
    Job importSettlementsJob(
            JobRepository jobRepository,
            Step importSettlementsStep) {
        return new JobBuilder("importSettlements", jobRepository)
                .start(importSettlementsStep)
                .build();
    }

    @Bean
    Step importSettlementsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<SettlementRow> settlementReader,
            ItemProcessor<SettlementRow, Settlement> settlementProcessor,
            ItemWriter<Settlement> settlementWriter) {
        return new StepBuilder("importSettlementsStep", jobRepository)
                .<SettlementRow, Settlement>chunk(250, transactionManager)
                .reader(settlementReader)
                .processor(settlementProcessor)
                .writer(settlementWriter)
                .faultTolerant()
                .retry(DeadlockLoserDataAccessException.class)
                .retryLimit(3)
                .skip(FlatFileParseException.class)
                .skipLimit(10)
                .build();
    }
}
```

The code means:

- at most 250 items are committed together
- a database deadlock may be transient, so it has a bounded retry
- a syntactically malformed file row is handled separately from infrastructure
  failure
- the eleventh skipped item fails this step because the configured total skip
  limit is ten

Do **not** copy the skip rule into a money-moving job blindly. A malformed
settlement usually needs the whole run to fail or to enter an explicit manual
reconciliation flow. Losing one vendor marketing row may be acceptable; losing
one financial record generally is not.

---

## 4. Reader, Writer, and Transaction Choices

### Choose a stable reader before optimizing it

| Input | Strong default | Main caution |
| --- | --- | --- |
| One uploaded or provider file | File reader with file name and checksum in job parameters | A replaced file with the same name is a different input. |
| Database range | `JdbcPagingItemReader` with a unique, stable sort key | Offset-like thinking and non-unique ordering lose or repeat rows. |
| Long read-only database scan | Cursor reader only after checking connection, timeout, and restart behaviour | A cursor can hold database resources for a long time. |
| External API pages | Reader with explicit page/cursor checkpoint and rate-limit handling | The remote API may change between pages. |

For a JDBC paging reader, Spring Batch can restart from the last sort key. That
only works safely when the sort key is unique and stable. `ORDER BY updated_at`
is not enough if multiple rows share the same timestamp; pair it with a unique
tie-breaker or use an immutable primary key range.

### Chunk size is a tradeoff

Small chunks:

- lose less work on failure
- keep transactions and locks shorter
- create more commits and metadata updates

Large chunks:

- reduce commit overhead
- may improve throughput
- repeat more work after failure and hold resources longer

Start with a boring value such as `100` or `250`, then measure database time,
lock waits, memory, commits, and recovery cost. Do not choose `10_000` because
the file is large.

### Keep the business writer idempotent

Batch metadata tracks the Batch execution. It does not prove that an external
provider, ledger, email service, or another database received an effect once.

For the settlement example, the writer should enforce a business invariant:

```sql
create table imported_settlement (
    provider_name varchar(40) not null,
    provider_settlement_id varchar(100) not null,
    business_date date not null,
    amount_yen bigint not null,
    primary key (provider_name, provider_settlement_id)
);
```

Then a retry or restart can attempt the same write without inventing a second
settlement. Decide explicitly whether a duplicate means:

- already processed successfully: no-op and count it
- same key but conflicting amount: fail and investigate
- correction: a new, versioned business action, not a silent overwrite

If the Batch repository and the business database are not in the same
transaction, a crash between business write and Batch metadata update can cause
the chunk to be attempted again. Idempotent writes are still required.

---

## 5. Restart Is Not Rerun

This distinction prevents many operational mistakes.

### Restart a failed business run

Use the same identifying parameters when the input is the same business run:

```text
importSettlements + businessDate=2026-08-07 + fileChecksum=4d2c...
```

Spring Batch records the failed execution and, when the job and step are
restartable, can continue from persisted state. Completed steps are normally
skipped on a restart.

Before restarting, check:

- the source file or database snapshot is still the same logical input
- the reader persists usable state
- the writer is idempotent
- the original cause is fixed or the job will simply fail again

### Run a different business action

Use a new identifying parameter only when it really represents a new run:

- a correction version approved by finance
- a newly delivered source file with a different checksum
- a deliberate reprocessing policy with separate audit semantics

Adding a random `run.id` to escape an already-completed instance is usually a
warning sign. A completed instance cannot be restarted; that protects you from
casually repeating the same business run.

### A safe restart conversation

Before pressing restart, an operator should be able to say:

> This is the same provider file and business date. The job failed in the
> database write step after 42,000 committed rows. The lock issue is gone, the
> settlement table has a unique provider ID, and we will verify the final
> expected count against the source manifest.

That is operational control. “It failed, try it again” is not.

---

## 6. Retry, Skip, Filter, and Fail Mean Different Things

| Outcome | Use when | Example | Required follow-up |
| --- | --- | --- | --- |
| Retry | The same action may succeed after a short wait | transient deadlock or temporary network reset | Bound attempts and make the effect safe to repeat. |
| Skip | This one item is invalid but policy permits the rest to continue | malformed optional supplier row | Record the item and reconcile it. |
| Filter | The item is valid but irrelevant to this run | already-cancelled export candidate | Count it separately; do not disguise it as an error. |
| Fail | Continuing could make business truth incomplete or unsafe | missing settlement amount or unknown money currency | Stop, preserve evidence, and resolve deliberately. |

### Retry rules

Retry only known transient failures. Use a small limit and backoff where the
dependency needs recovery time. Never retry:

- validation errors
- authorization failures
- a business rejection
- a non-idempotent external side effect without an idempotency key or durable
  outbox-style handoff

### Skip rules

`skipLimit` is not a data-quality policy. It is a guardrail that stops a run
from quietly discarding an unlimited number of records. Send skipped records to
a durable reject table or a reviewed file with:

- job execution ID
- source identity and row number or record ID
- failure class and safe error summary
- time and disposition owner

Never put full payment data, tokens, or personal data into the exception log
just to make a bad row easier to find.

---

## 7. Observability: Know the Outcome, Not Only That the Process Is Alive

A green process health endpoint does not mean last night’s batch completed.
Observe three layers.

### A. Batch execution layer

From `JobRepository` / `JobExplorer`, inspect:

- job name, instance parameters, execution ID, status, start and end time
- the failed step and exit description
- read, write, filter, commit, rollback, retry, and skip counts
- whether another execution for the same logical run is already running

Use the execution ID in structured logs and support communication. It is much
more useful than saying “the 02:00 import failed”.

### B. Business reconciliation layer

For every consequential job, define expected evidence outside Batch metadata:

- source manifest says `50,000` records
- `49,995` were committed
- `3` were filtered because they were already reversed
- `2` are in the reject table with owners and ticket IDs
- total money amount matches the provider control total

The exact fields depend on the domain. The rule does not: Batch status and
business correctness are separate checks.

### C. Service and dependency layer

Monitor:

- duration compared with the normal window
- failure count by job and step
- items processed per minute and backlog age
- database pool saturation, lock waits, slow queries, and disk space
- external API rate limits and error rate
- missed schedule or overlapping execution

Alert on a missed business deadline or failed reconciliation, not only on a
JVM crash.

---

## 8. Debugging Playbook

When a job fails, start with the execution record rather than source code.

1. **Identify the exact run.** Job name, business parameters, execution ID,
   start time, and whether a second attempt is running.
2. **Find the failed step.** Inspect its status, exit information, and counts.
   A high read count with zero writes points to a processor or writer issue;
   growing rollback counts point to transaction or dependency trouble.
3. **Correlate logs and dependency signals.** Search with execution ID, source
   ID, and step name. Check DB locks, pool exhaustion, remote error rate, and
   recent deploy/config changes.
4. **Classify before acting.** Is it bad input, a transient dependency failure,
   a code/config regression, or an unsafe business mismatch?
5. **Choose the safe action.** Restart only the same failed run after fixing a
   transient cause; quarantine input; roll back a release; or run a formally
   versioned correction.
6. **Reconcile after completion.** Verify source count, output count, rejects,
   and business totals. `COMPLETED` is necessary, not sufficient.

### Fast symptom map

| Symptom | Likely first check | Unsafe reflex to avoid |
| --- | --- | --- |
| `JobInstanceAlreadyCompleteException` | Are these parameters an already completed business run? | Add a timestamp and rerun blindly. |
| Job starts on every API deploy | `spring.batch.job.enabled` and trigger ownership | Treat startup as a harmless scheduler. |
| Repeated rows after restart | Writer unique key, input identity, transaction boundary | Delete Batch metadata. |
| Job is slow with low CPU | DB query plan, locks, API latency, connection pool | Add threads first. |
| Many skipped rows | Reject evidence, source-format change, skip policy | Raise `skipLimit` until it turns green. |
| Restart reprocesses from the beginning | Reader `ExecutionContext`, stable sort key, `saveState` | Assume every reader restarts automatically. |

---

## 9. Testing the Failure Shape

Test the business and operational behavior, not only that the job returns
`COMPLETED`.

### Unit tests

Test the processor’s pure decisions:

- valid input becomes the expected output
- invalid amount fails clearly
- an irrelevant but valid row is filtered deliberately
- duplicate business identity is not silently transformed into a second effect

### Integration tests

Use the real database engine with Testcontainers when the job relies on SQL,
migrations, uniqueness, locking, or paging behavior. H2 cannot prove
PostgreSQL query, locking, or migration behaviour.

### End-to-end job tests

`spring-batch-test` provides `@SpringBatchTest` and test utilities for launching
a job and inspecting the execution. Test at least:

- normal run: output and reconciliation counts match the source
- one transient writer failure: bounded retry succeeds without duplicate output
- malformed input: expected reject or intentional failure
- crash/failure after committed chunks: restart reaches the correct final state
- same completed parameters: launch is rejected rather than repeated

Use unique test parameters per test unless the test is specifically checking
restart behavior. Otherwise old Batch metadata makes tests order-dependent.

---

## 10. Scale Only After the Single-Threaded Job Is Trustworthy

The first scaling question is not “how do we add threads?” It is “which input
range can be processed independently without changing correctness?”

Strong progression:

1. improve the query, indexes, page size, and chunk size
2. split independent steps when they really have separate responsibilities
3. partition by immutable, non-overlapping ranges such as primary-key ranges
4. add multi-threaded or remote execution only when workers, DB pools,
   idempotency, and monitoring can support it

Partitioning works well when each worker receives an explicit range:

```text
partition-0: settlement_id 1 to 1,000,000
partition-1: settlement_id 1,000,001 to 2,000,000
```

It works badly when workers all query “records updated in the last hour” and
race over a moving target. Measure first: Spring Batch itself recommends
checking whether a single-process job already meets the real requirement before
introducing parallel complexity.

---

## 11. Common Traps

1. **Using the job’s start time as the business cutoff.**
   Define a business date, timezone, and input watermark explicitly. A job that
   starts late after a DST or deployment event must not silently select a
   different population.

2. **Reading a moving table without a boundary.**
   Capture a high-water mark, immutable ID range, or source snapshot. Otherwise
   rows can move into or out of the query while the job runs.

3. **Making one giant transaction.**
   It increases lock duration, rollback cost, and blast radius. Use chunks
   unless the operation is truly one small atomic business action.

4. **Treating `RunIdIncrementer` as an idempotency solution.**
   It creates new instances; it does not make writes safe or identify the same
   business input.

5. **Launching from multiple replicas without ownership.**
   Deployment replicas, schedulers, and manual operator tools need one clear
   launch policy. A persistent repository helps coordinate state, but it does
   not replace a deliberate scheduler and business idempotency.

6. **Skipping without a reconciliation path.**
   A skipped row is unfinished work until someone can find, correct, and replay
   or formally close it.

7. **Logging source rows wholesale.**
   Batch imports often contain PII, account data, or provider references. Log
   correlation identifiers and protected summaries, not raw payloads.

8. **Assuming a completed job means the business result is correct.**
   Reconcile counts and totals against the source before declaring success.

---

## Practical Checklist

Before shipping a job, can the team answer:

- What exact input does one run own, and how is it identified?
- Which parameter values mean restart versus a new business run?
- What prevents duplicate business effects after retry or restart?
- Which errors retry, skip, filter, or fail, and why?
- Where do rejected records go and who reconciles them?
- How can an operator locate one failed execution and its failed step?
- What source count, output count, and business total prove completion?
- What is the safe action if the job crashes halfway through?
- Which measured bottleneck would justify partitioning?

## Interview Framing

> I use Spring Batch for bounded, auditable work such as imports and
> settlements. I model the business input with stable job parameters, use a
> persistent job repository for execution state, and make the writer idempotent
> with business-level uniqueness. I distinguish retryable transient failures
> from rejected data, and I only restart after checking the failed step,
> committed counts, source identity, and reconciliation result.

## Related Reading

- [03-transactions-and-isolation.md](./03-transactions-and-isolation.md): the
  transaction and locking rules beneath chunk writes
- [19-flyway-and-schema-migrations.md](./19-flyway-and-schema-migrations.md):
  versioned schema changes for Batch metadata and business tables
- [17-webhook-idempotency-lab.md](./17-webhook-idempotency-lab.md): the same
  business-level duplicate-protection principle in an event flow
- [../testing/01-testing-strategies.md](../testing/01-testing-strategies.md):
  choosing real dependency tests for risky boundaries
- [../sre/04-incident-response-and-triage.md](../sre/04-incident-response-and-triage.md):
  live mitigation and communication discipline
- [../sre/05-capacity-planning-and-load-shedding.md](../sre/05-capacity-planning-and-load-shedding.md):
  bottlenecks, saturation, and measuring before scaling

## Further Reading

- [Spring Boot: Spring Batch](https://docs.spring.io/spring-boot/reference/io/spring-batch.html)
- [Spring Batch: chunk-oriented processing](https://docs.spring.io/spring-batch/reference/step/chunk-oriented-processing.html)
- [Spring Batch: restart configuration](https://docs.spring.io/spring-batch/reference/step/chunk-oriented-processing/restart.html)
- [Spring Batch: retry and skip](https://docs.spring.io/spring-batch/reference/5.2/step/chunk-oriented-processing/retry-logic.html)
- [Spring Batch: testing](https://docs.spring.io/spring-batch/reference/testing.html)
- [Spring Batch: scaling and partitioning](https://docs.spring.io/spring-batch/reference/scalability.html)
