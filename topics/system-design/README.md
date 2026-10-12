# System Design

Use this folder to learn how to turn a problem into a system that meets its
important promises under normal use, load, failure, and change.

## Start Here

1. [General design method](./00-general-system-design-method.md): the reusable sequence from outcome and constraints to recovery and proof.
2. [Decision reference](./system-design-decision-cheatsheet.md): choose a boundary or component after the need is clear.

## Practice and Examples

- [Design drills](./system-design-drills.md): practice decisions under timeouts, duplicate delivery, contention, and read load.
- [Checkout to fulfillment](./checkout-to-fulfillment-end-to-end.md): one connected case from purchase intent to physical work.
- [Worked diagrams](./worked-diagrams.md): inspect atomic request claims, outbox replay, and stock contention.

If a question says "lifecycle," first identify what changes state. For Spring
object hooks, see [bean lifecycle](../spring-boot/06-bean-lifecycle.md); for
local database work, see [transactions](../spring-boot/03-transactions-and-isolation.md);
for business states and cross-system recovery, use the checkout case above.

The examples show the method in use. They are not separate templates to copy
into every system. For requirements discovery, use
[solution decisions](../solutions-architecture/README.md); for API boundaries,
use [API design](../api/README.md).
