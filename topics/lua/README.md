# Lua for Embedded Business Rules

Use this folder for one narrow architecture move: a small Lua rule layer inside
a Java or Kotlin service. It is not a general Lua or alternative-backend path.

Typical fit:

- pricing and promotions
- discount eligibility
- regional rule variations
- narrowly scoped rules that change more often than the host service should be redeployed

## Smallest Useful Mental Model

The JVM service owns authentication, transactions, input validation, execution
limits, publication, rollback, and the final business state. Lua evaluates one
narrow rule and returns a simple result.

Weak approach:

> Lua lets us patch production business logic freely.

Better approach:

> A sandboxed script evaluates a bounded rule; the host service keeps control of
> every sensitive capability and operational decision.

## Recommended Order

1. [lua-for-jvm-backend-engineers.md](./lua-for-jvm-backend-engineers.md): the architecture decision, safety model, and JVM integration shape
2. [examples/01-tables.lua](./examples/01-tables.lua): Lua's one core data structure
3. [examples/03-retail-rules.lua](./examples/03-retail-rules.lua): a pricing-rule example
4. [examples/04-sandbox-security.lua](./examples/04-sandbox-security.lua): sandboxing, governance, and rollback before trusting the approach
5. [examples/02-metatables.lua](./examples/02-metatables.lua): useful later, not required for the rule-engine use case

Run examples with `lua examples/<file>.lua` from this folder.

## Core Rule

> Use embedded scripting only when faster rule iteration has more value than the
> lost typing and added governance cost. The host owns the trust boundary.
