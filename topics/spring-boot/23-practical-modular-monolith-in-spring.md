# Practical Modular Monolith in Spring Boot

A modular monolith is often the strongest default before microservices.

The problem is that many teams say "modular monolith" but still build:

- one giant codebase
- one giant package tree
- one shared service layer with weak ownership

This note keeps the shape practical.

---

## Why This Matters

If you skip the modular-monolith step, teams often split too early into
services before the boundaries are mature.

If you keep one codebase but never make the boundaries explicit, you get:

- accidental coupling
- harder extraction later
- architecture discussions that sound cleaner than the code really is

This matters because a good modular monolith lets you learn the domain before
paying full distributed-systems cost.

---

## Smallest Useful Mental Model

A modular monolith means:

- one deployable application
- one main database
- explicit internal module boundaries
- business capabilities separated inside the codebase

Short rule:

> A modular monolith keeps local simplicity while still forcing you to name real boundaries.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- monolith means everything can call everything
- package structure is just code organization style
- extraction later will be easy automatically

Better mental model:

- modular monolith means one app with real internal ownership boundaries
- package boundaries, dependency direction, and database access rules still matter
- later extraction becomes easier only if the internal boundaries are already real

Small concrete example:

- weak approach: `order`, `payment`, and `inventory` code all share services,
  entities, and repositories freely
- better approach: each module owns its application and domain logic, and other
  modules interact through explicit module-facing interfaces

---

## 1. What A Good Modular Monolith Protects

It protects:

- domain clarity
- local transaction simplicity
- easier debugging
- lower operational cost

Without pretending:

- all boundaries are solved forever

This is a learning and delivery shape, not a final ideology.

---

## 2. Package By Module First

Weak shape:

```text
controller/
service/
repository/
entity/
```

This usually hides domain ownership.

Stronger shape:

```text
com.example.app
  order/
    application/
    domain/
    infrastructure/
  payment/
    application/
    domain/
    infrastructure/
  inventory/
    application/
    domain/
    infrastructure/
```

Why this matters:

- each module becomes visible
- cross-module access becomes easier to review
- extraction later starts from something real

---

## 3. Module Interaction Rules

Good default:

- module internals are not everyone else's playground
- cross-module calls go through explicit interfaces or use-case boundaries
- shared utility code stays small and truly generic

Bad sign:

- one giant shared service package
- direct repository access across modules
- module A updates module B's tables or entities directly

Short rule:

> If one module needs another module's internals all the time, the boundary is weak or misplaced.

---

## 4. Database Rules Inside The Monolith

Even with one database, ownership still matters.

Good default:

- one module owns the write rules for its tables
- other modules do not mutate those tables casually
- cross-module flows still go through application logic, not random repository access

Why:

- one database does not mean one undifferentiated domain

This is the internal version of service-owned data.

---

## 5. Concrete Example

Imagine one Spring Boot commerce app with:

- `order`
- `payment`
- `inventory`
- `notification`

Good shape:

- `OrderApplicationService` asks `InventoryFacade` to reserve stock
- `PaymentApplicationService` handles payment rules
- `Notification` reacts to events or explicit application calls

Weak shape:

- `OrderService` loads `PaymentEntity` directly
- `InventoryRepository` is injected anywhere
- notification logic joins order and payment tables directly

That is still one deployable app, but the second shape is much harder to split
or even reason about.

---

## 6. Events Inside A Modular Monolith

You do not need microservices to benefit from events.

Inside one modular monolith, events can help when:

- one module should react later
- coupling between modules is getting noisy
- you want to keep side effects off the critical path

Examples:

- `OrderPlaced`
- `PaymentAuthorized`
- `InventoryReserved`

This can stay simple:

- local domain event
- application event
- outbox later if needed

The point is not ceremony.
The point is reducing direct coupling where it helps.

---

## 7. What Makes Later Extraction Easier

A module is easier to extract later when:

- its package boundary is already clean
- its write ownership is already clear
- its APIs to other modules are explicit
- its data access is not spread everywhere
- its side effects are already observable

This is why modular monolith is not a delay tactic.
It is preparation for good boundaries.

---

## 8. Strong Default

For many backend teams:

- start with a modular monolith
- package by business module
- keep write ownership explicit even with one DB
- add module-facing interfaces before service extraction pressure arrives

That is usually stronger than:

- split to microservices because the codebase is getting bigger

---

## 9. Big Traps

1. **Package by layer only**
   Example: module ownership stays invisible.

2. **Cross-module repository access**
   Example: boundary looks clean in diagrams but not in code.

3. **One giant shared service layer**
   Example: all business rules mix together.

4. **Calling it modular without dependency discipline**
   Example: names changed, coupling stayed.

5. **Jumping to microservices before boundaries are stable**
   Example: distributed complexity arrives before domain clarity.

---

## 10. Practical Summary

Good short answer:

> A modular monolith in Spring Boot is one deployable app with explicit internal business-module boundaries. I package by capability, keep write ownership clear even with one database, and avoid cross-module repository access so the codebase stays understandable now and easier to split later if real service boundaries emerge.
