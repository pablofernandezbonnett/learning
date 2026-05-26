# Lua for Embedded Business Rules

Use this folder when you want the smallest useful mental model for `Lua` from a
`Java` or `Kotlin` backend point of view.

This topic is narrow on purpose.
It is not about learning Lua as your next general backend language.
It is about one specific architecture move:
using a small embedded scripting layer for rule-heavy domains that change
faster than the main JVM deployment cycle should.

Typical fit:

- pricing
- promotions
- discount eligibility
- region-specific rule logic
- narrowly scoped hot-swappable product rules

## Smallest Mental Model

Lua is a lightweight embeddable scripting language.
The host application stays responsible for security, context, execution limits,
and the final business flow.
The script only evaluates a narrow rule and returns a result.

Some rules change faster than the host application should be redeployed.

Typical examples:

- pricing adjustments
- promotion logic
- regional discount rules
- staff or VIP eligibility rules

If every rule change requires:

- code change
- PR
- CI
- deployment

then the deployment cycle becomes the bottleneck for business rule changes.

Short rule:

> embedded scripting is useful when the rule layer changes faster than the service runtime should

Plain-English version:

> the JVM service still owns the product flow; Lua only owns one narrow rule
> decision inside that flow

The host application stays responsible for:

- loading trusted script content
- building the input context
- enforcing sandbox and execution limits
- interpreting the result

The script stays responsible for:

- evaluating the rule
- returning a decision or calculated output

Smallest example:

```text
Spring Boot service
  -> loads Lua rule
  -> passes ctx { basePrice, customerTier, quantity }
  -> executes rule
  -> gets { finalPrice, discountReason }
```

## Why Lua Can Be Attractive

Pros:

- lightweight and embeddable
- good fit for compact rule logic
- rules can change without rebuilding the whole service
- simpler than inventing a custom rule DSL from scratch

Tradeoffs / Cons:

- weaker typing and tooling than Java or Kotlin
- sandboxing and governance matter a lot
- debugging and version control of scripts need discipline
- not a good default if rules are stable and developer-owned anyway

## Stable Practical Baseline

If your goal is only to understand the useful part of Lua for JVM backend work,
keep this baseline warm first:

- tables, meaning Lua's flexible core structure for arrays, maps, and object-like values
- `local`, because globals by default are a real footgun
- functions returning simple tables as rule results
- host-owned sandboxing, execution limits, input validation, and result validation
- script versioning and rollback, because hot updates without rollback are reckless

That is already enough to understand most narrow rule-engine uses of Lua in a
backend system.

## Recommended Order

1. [lua-for-jvm-backend-engineers.md](./lua-for-jvm-backend-engineers.md): why Lua can fit a JVM backend at all, what transfers from Java/Kotlin, and how to think about hot rule updates safely
2. [examples/01-tables.lua](./examples/01-tables.lua): the one Lua data structure that matters first
3. [examples/03-retail-rules.lua](./examples/03-retail-rules.lua): a realistic rule-engine style example
4. [examples/04-sandbox-security.lua](./examples/04-sandbox-security.lua): why embedded scripting is a security and governance decision, not a syntax toy
5. [examples/02-metatables.lua](./examples/02-metatables.lua): useful later, but secondary for the embedded-rules use case

## If You Want To Get Productive Fast

1. read [lua-for-jvm-backend-engineers.md](./lua-for-jvm-backend-engineers.md)
2. run [examples/01-tables.lua](./examples/01-tables.lua)
3. run [examples/03-retail-rules.lua](./examples/03-retail-rules.lua)
4. read [examples/04-sandbox-security.lua](./examples/04-sandbox-security.lua) before you trust the architecture idea

This keeps the learning path focused on the architecture value first, then the
minimum syntax, then the safety model.

## What To Learn First

- `Tables`: Lua's main data structure. Think of them as the flexible core
  structure behind arrays, maps, and object-like data.
- `Local` vs global: variables are global by default. That is dangerous. Use
  `local` unless there is a very deliberate reason not to.
- functions and closures: useful because rule evaluation often passes small
  functions and context.
- metatables: interesting, but secondary for the business-rules use case. You
  do not need to go deep here before understanding the host-integration model.

For this repo, a stronger practical order is:

- tables
- local variables
- simple functions
- returning a result table
- host integration model
- sandbox and governance

Do not start with metaprogramming.

## Real Backend Use Case

Retail-style pricing and promotions

Example:

- base price comes from the catalog service
- region and customer tier come from the request context
- Lua rule determines whether a seasonal promotion or tier discount applies

This can be attractive when:

- the rule logic changes often
- the host service boundary should remain stable
- business wants controlled rule changes without redeploying the JVM service every time

This is not a replacement for core transactional correctness.
The source of truth and final order/payment state still belong to the main backend system.

## Minimal Host Integration Shape

The host service should do the safe work:

1. load a vetted script
2. build a strict input context
3. execute inside a sandbox
4. read back a simple result

Example idea with LuaJ:

```kotlin
fun executePricingScript(luaScript: String, productId: String, basePrice: Int, tier: String): Int {
    val globals = createSandbox()
    val ctx = globals.tableOf()
    ctx["productId"] = productId
    ctx["basePrice"] = basePrice
    ctx["customerTier"] = tier
    globals["ctx"] = ctx
    val result = globals.load(luaScript).call()
    return result["finalPrice"].toint()
}
```

Important rule:

> the host application must own the sandbox, boundaries, and validation; the script should not get arbitrary runtime power

The host should also own:

- script version selection
- rollout and rollback
- audit logging
- metrics such as script version, execution time, and failure count
- publication permissions and approval flow

Small but important engineer details:

- do not let the same broad application identity both edit scripts and execute them
- treat script publication like a controlled release, not like editing a feature flag in production
- keep a kill switch so one bad script version can be disabled fast
- validate both the input context and the returned result shape, not only the script text
- prefer allow lists over deny lists for exposed globals and host capabilities

## Security And Operational Caution

If you embed a scripting language, the hard part is not syntax.
The hard part is controlling what the script is allowed to do.

Questions that matter:

- can the script touch files or the network?
- can it import dangerous libraries?
- can one bad script hang the host runtime?
- who can publish or edit scripts?
- how are scripts versioned and rolled back?
- which runtime identity is allowed to fetch scripts, and is it read-only?
- can one script version be enabled gradually instead of all at once?

This is why embedded scripting is a real architecture choice, not a toy.

Strong default:

- one identity writes approved script artifacts
- one runtime identity reads approved script artifacts
- normal service identities do not get script-publishing rights
- script changes leave an audit trail with approver, version, and timestamp
- rollback is a normal operation, not an incident-only improvisation

## When I Would Use It And When I Would Not

Use it when:

- business rules change frequently
- the host runtime should remain stable
- a sandboxed rule layer gives real agility value

Avoid it when:

- rules are stable
- the team has no appetite for script governance
- strong typing and normal code review are more valuable than hot-swappable rule changes

Weak approach:

- "Lua lets us patch business logic live, so we can move faster everywhere"

Better approach:

- "Lua is useful for a narrow rule layer that changes frequently, while the JVM service still owns auth, transactions, state transitions, and final correctness"

## Practical Summary

Practical summary:

> I would consider embedded scripting, such as Lua, only for narrow rule-heavy domains
> like promotions or pricing where the rule layer changes frequently. The benefit is
> faster rule iteration without full redeployment. The cost is weaker typing, more
> sandboxing work, and the need for strong governance around script publication.

## Code Examples

| File | What it teaches |
|---|---|
| `examples/01-tables.lua` | tables as arrays, maps, objects, and module containers |
| `examples/02-metatables.lua` | prototypal behaviour and metatable basics |
| `examples/03-retail-rules.lua` | pricing and discount rule example |
| `examples/04-sandbox-security.lua` | sandbox and safety rationale |

Run them with:

```bash
lua examples/01-tables.lua
lua examples/02-metatables.lua
lua examples/03-retail-rules.lua
lua examples/04-sandbox-security.lua
```
