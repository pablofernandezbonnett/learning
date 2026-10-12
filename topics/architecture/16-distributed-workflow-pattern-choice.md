# Choosing a Distributed Workflow Pattern

Start with the business step and the failure it must survive. A pattern is
useful only if it closes that gap; several patterns can appear in one flow
because they solve different problems.

## Find the Coordination Gap

| Problem | Smallest useful starting point | Continue with |
| --- | --- | --- |
| The caller needs a result now | Synchronous call with timeout and an unknown-outcome plan | [Sync or async](../api/07-sync-vs-async-integration-choice.md) |
| One owned job can finish later | Queue and retryable worker | [Broker delivery](../api/02-message-brokers-and-delivery-semantics.md) |
| Several consumers need one business fact or replay | Event stream and versioned event contract | [Broker delivery](../api/02-message-brokers-and-delivery-semantics.md) |
| A database change must reliably publish an event | Outbox in the local transaction | [Workflow recovery](./03-distributed-transactions-and-events.md) |
| Several write owners can leave a business flow half-finished | Explicit state and compensation; a saga if steps need coordination | [Workflow recovery](./03-distributed-transactions-and-events.md) |
| Combined reads no longer fit the write model | A query projection; heavier CQRS only when justified | [Read model migration](./19-database-per-service-and-read-model-migration.md) |
| The event sequence itself must be the source of truth | Consider event sourcing | See below |

**CQRS** means separating the model that validates writes from the model that
serves reads. It does not require two databases or event sourcing. A simple
query projection may be enough; see [practical CQRS in Spring
Boot](../spring-boot/22-practical-cqrs-and-read-models.md) for an implementation
path.

**Event sourcing** stores an ordered history of business changes as the source
from which current state is rebuilt. Choose it only when that history and
replay are part of the requirement. It adds event versioning, replay, and
projection maintenance; ordinary state plus an audit trail is simpler when
full event history is not needed.

## One Example, Several Gaps

An order is saved, then payment and inventory must act. An outbox can ensure
the saved order produces an event. Broker consumers need duplicate-safe
processing. If payment succeeds but inventory fails, the business workflow
needs a recorded state and compensation. The outbox does not replace that
recovery plan, and a saga does not make event publication atomic.

## Decision Rule

Ask what must happen now, which writes have independent owners, what can fail
after a commit, and which effects may repeat. Add only the mechanism that
answers each concrete question.
