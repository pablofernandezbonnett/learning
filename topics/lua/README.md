# Lua for Embedded Business Rules

Lua is background material for one specific design idea:

> when frequent business-rule changes should be moved out of the main JVM deployment cycle

This makes it useful for retail, pricing, promotion, or rules-heavy systems.

---

## 1. What Lua Is Doing Here

Lua is a lightweight embeddable scripting language.

In this repo, the interesting question is not "learn Lua as a language for its own sake".
The interesting question is:

- when would a backend system benefit from an embedded rule layer?
- what does that buy you?
- what does it cost?

---

## 2. What Problem It Solves

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

---

## 3. Smallest Mental Model

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

---

## 4. Why Lua Can Be Attractive

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

---

## 5. What To Learn First

### Tables

Lua's main data structure.
Think of them as the flexible core structure behind arrays, maps, and object-like data.

### Local vs global

Variables are global by default.
That is dangerous.
Use `local` unless there is a very deliberate reason not to.

### Functions and closures

Useful because rule evaluation often passes small functions and context.

### Metatables

Interesting, but secondary for the business-rules use case.
You do not need to go deep here before understanding the host-integration model.

---

## 6. Real Backend Use Case

### Retail-style pricing and promotions

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

---

## 7. Minimal Host Integration Shape

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

---

## 8. Security And Operational Caution

If you embed a scripting language, the hard part is not syntax.
The hard part is controlling what the script is allowed to do.

Questions that matter:

- can the script touch files or the network?
- can it import dangerous libraries?
- can one bad script hang the host runtime?
- who can publish or edit scripts?
- how are scripts versioned and rolled back?

This is why embedded scripting is a real architecture choice, not a toy.

---

## 9. When I Would Use It And When I Would Not

Use it when:

- business rules change frequently
- the host runtime should remain stable
- a sandboxed rule layer gives real agility value

Avoid it when:

- rules are stable
- the team has no appetite for script governance
- strong typing and normal code review are more valuable than hot-swappable rule changes

---

## 10. Practical Summary

Good short answer:

> I would consider embedded scripting, such as Lua, only for narrow rule-heavy domains
> like promotions or pricing where the rule layer changes frequently. The benefit is
> faster rule iteration without full redeployment. The cost is weaker typing, more
> sandboxing work, and the need for strong governance around script publication.

---

## 11. Code Examples

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
