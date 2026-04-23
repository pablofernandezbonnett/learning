# Clean Code and Code Review

> Primary fit: `Shared core`


Use this when the work includes:

- coding tests
- pair programming
- code review discussion
- "what does good backend code look like?" questions

This is not a book-summary version of "clean code."
It is the practical backend version.

Use the same reusable shape as the rest of the repo:

- definition
- minimal example
- real implementation
- practical summary

---

## 1. What Clean Code Actually Means

Clean code is code that is:

- easy to read
- easy to change
- easy to test
- hard to misuse

In backend work, clean code usually means:

- names explain intent
- state transitions are explicit
- side effects are visible
- boundaries are testable
- error handling is deliberate

Two quick terms used in this note:

- `state transition` means an important business state change, for example `PENDING -> PAID`
- `side effect` means the code changes something outside the current method, for example a database write, an HTTP call, or publishing an event

It does **not** mean:

- tiny functions everywhere
- abstracting everything into interfaces
- obsessing over style while correctness is broken

Core sentence:

> For backend code, clean usually means the next engineer can understand the
> intent, change it safely, and see where the side effects happen.

---

## 2. Minimal Example

### Bad shape

```kotlin
fun process(order: Order?) {
    if (order != null && !order.items.isNullOrEmpty()) {
        if (!order.isPaid) {
            charge(order)
            order.isPaid = true
            repo.save(order)
            mail(order)
        }
    }
}
```

<details>
<summary>Java version</summary>

```java
public void p(Order o) {
    if (o != null && o.getItems() != null && !o.getItems().isEmpty()) {
        if (!o.isPaid()) {
            charge(o);
            o.setPaid(true);
            repo.save(o);
            mail(o);
        }
    }
}
```

</details>

Why this is weak:

- unclear naming
- validation, payment, persistence, and notification are mixed together
- side effects are hidden in one block
- failure handling is unclear

### Better shape

```kotlin
fun processOrderPayment(order: Order) {
    validatePayableOrder(order)

    val payment = paymentGateway.charge(order)
    order.markPaid(payment.transactionId)
    orderRepository.save(order)

    notificationService.sendPaymentConfirmation(order.id)
}
```

<details>
<summary>Java version</summary>

```java
public void processOrderPayment(Order order) {
    validatePayableOrder(order);

    PaymentResult payment = paymentGateway.charge(order);
    order.markPaid(payment.transactionId());
    orderRepository.save(order);

    notificationService.sendPaymentConfirmation(order.id());
}
```

</details>

Why this is better:

- intent is obvious
- the main flow reads top to bottom
- the state transition is explicit
- dependencies and side effects are visible

---

## 3. What Good Review Usually Wants To See

### 1. Naming that carries intent

Good names explain:

- what the code is deciding
- what state is changing
- what the side effect is

Weak names:

- `process`
- `handle`
- `doStuff`
- `data`
- `flag`

Better names:

- `reserveInventory`
- `markPaymentAuthorized`
- `publishOrderCreatedEvent`
- `isRetryableFailure`

### 2. Explicit side effects

Backend code becomes risky when hidden effects are mixed together.

Make it obvious when code:

- writes to the database
- calls another service
- publishes an event
- changes important business state in memory

Example:

- `order.markPaid(...)` changes local business state
- `orderRepository.save(order)` writes to the database
- `paymentGateway.charge(order)` calls an external dependency
- `eventPublisher.publish(...)` triggers later work elsewhere

If all four happen inside one vague helper like `process(order)`, the code becomes hard to reason about and hard to review safely.

### 3. Structure that keeps business logic separate from external calls

Here, a `boundary` means a place where your code crosses into something external, such as:

- the database
- another service
- a queue or event bus
- a third-party API

Good backend code usually separates:

- validation
- business decision
- persistence
- external calls

Not because "layers are pretty," but because testability and change-safety improve.

Example:

- validation decides whether the request is even allowed
- business logic decides what business state should change
- persistence stores that state
- external calls notify or coordinate with other systems

If those concerns are mixed together, even a small change becomes risky because nobody can tell which part is business logic and which part is database, network, or queue work.

### 4. Deliberate error handling

Strong code shows:

- which failures are expected
- which failures are retryable
- which failures should stop the flow

Weak code throws generic exceptions everywhere or swallows them silently.

Example:

- invalid input -> return `400` or a business validation error
- payment provider timeout -> maybe retry or return a temporary failure
- duplicate webhook -> acknowledge safely, do not process twice

Those are different failures and should not all collapse into one generic `RuntimeException`.

### 5. Simplicity over pattern theater

A clean service method with a few obvious dependencies is stronger than a maze of:

- factories
- handlers
- strategies
- abstract base classes

unless those abstractions solve a real change problem.

That does **not** mean patterns are bad.
It means patterns should solve a real problem, not decorate the code.

Useful practical patterns worth recognizing:

- `Adapter`: hide external provider details behind your own interface
- `Strategy`: switch between a few real business rules or policies cleanly
- `Factory`: useful when object creation is complex or variant-dependent
- `Repository`: keep persistence access behind a clear boundary

Small examples and use cases:

Minimal Kotlin sketches:

These are intentionally tiny.
The point is to make the dependency shape obvious, not to show production-ready code.

- `Adapter`
  - use when: you call a third-party API and do not want the rest of your codebase to depend on that provider's request and response shapes or error conventions
  - example:
    - `StripePaymentClientAdapter` translates your internal `PaymentRequest` into Stripe's request shape
    - later, if you add another provider, the rest of your service still speaks your own internal interface

```kotlin
data class PaymentRequest(val orderId: String, val amount: BigDecimal, val currency: String)
data class PaymentResult(val authorized: Boolean, val providerRef: String?)

interface PaymentClient {
    fun authorize(request: PaymentRequest): PaymentResult
}

class StripePaymentClientAdapter(
    private val stripeClient: StripeClient,
) : PaymentClient {
    override fun authorize(request: PaymentRequest): PaymentResult {
        val stripeRequest = StripeChargeRequest(
            amountInMinorUnits = request.amount.movePointRight(2).toLong(),
            currency = request.currency.lowercase(),
            metadata = mapOf("orderId" to request.orderId),
        )

        val response = stripeClient.createCharge(stripeRequest)
        return PaymentResult(
            authorized = response.status == "succeeded",
            providerRef = response.chargeId,
        )
    }
}
```

- `Strategy`
  - use when: you have a few real variants of one decision and the rule may change by market, tenant, or product type
  - example:
    - `PricingStrategy` for regular pricing vs campaign pricing
    - `RetryStrategy` for different partner integrations

```kotlin
interface PricingStrategy {
    fun calculate(basePrice: BigDecimal): BigDecimal
}

class RegularPricingStrategy : PricingStrategy {
    override fun calculate(basePrice: BigDecimal): BigDecimal = basePrice
}

class CampaignPricingStrategy(
    private val discountRate: BigDecimal,
) : PricingStrategy {
    override fun calculate(basePrice: BigDecimal): BigDecimal =
        basePrice.multiply(BigDecimal.ONE - discountRate)
}

class PricingService(
    private val strategyByMarket: Map<String, PricingStrategy>,
) {
    fun priceFor(market: String, basePrice: BigDecimal): BigDecimal =
        strategyByMarket.getValue(market).calculate(basePrice)
}
```

- `Factory`
  - use when: object creation itself contains branching or setup that would otherwise leak everywhere
  - example:
    - `NotificationClientFactory` creates the right client for email, SMS, or push depending on channel
    - `PaymentCommandFactory` creates the right request object for auth vs capture

```kotlin
enum class NotificationChannel { EMAIL, SMS, PUSH }

interface NotificationClient {
    fun send(recipient: String, message: String)
}

class NotificationClientFactory(
    private val emailClient: NotificationClient,
    private val smsClient: NotificationClient,
    private val pushClient: NotificationClient,
) {
    fun create(channel: NotificationChannel): NotificationClient =
        when (channel) {
            NotificationChannel.EMAIL -> emailClient
            NotificationChannel.SMS -> smsClient
            NotificationChannel.PUSH -> pushClient
        }
}
```

- `Repository`
  - use when: business or service code should not care about JPA (Java Persistence API) query details, SQL, or persistence framework specifics
  - example:
    - `OrderRepository.findByCheckoutId(...)`
    - the service asks for an order, not for a SQL query string

```kotlin
interface OrderRepository {
    fun findByCheckoutId(checkoutId: String): Order?
    fun save(order: Order): Order
}

class CheckoutService(
    private val orderRepository: OrderRepository,
) {
    fun loadOrder(checkoutId: String): Order =
        orderRepository.findByCheckoutId(checkoutId)
            ?: throw OrderNotFoundException(checkoutId)
}
```

Short rule:

> the pattern should remove real coupling, real branching, or real framework leakage
> if it only adds ceremony, it is probably not helping

`MVC` (`Model-View-Controller`), `DDD` (`Domain-Driven Design`), and `ACL` (`Anti-Corruption Layer`) are related, but they live at a different level:

- `MVC` is an application-structure pattern
- `DDD` is a domain-modeling approach
- `ACL` is a boundary-protection pattern when integrating with external systems

They are not alternatives.
A backend service can use them together:

- `MVC` for HTTP entry points such as controllers and request handling
- `DDD` for the internal domain model and business rules
- `Adapter` objects inside an `ACL` to translate external request and response shapes or error models into your own internal language

In that sense, an `ACL` often helps protect a `DDD` model from external concepts that
would otherwise leak into the domain.

So no, classic patterns did not "disappear".
They are still useful when they solve a concrete local problem.
They just should not be forced everywhere.

Short rule:

> use patterns to remove real coupling or duplication, not to make the code look senior

---

## 4. Real Backend Implementation Rules

When reviewing or writing backend code, check this order:

1. correctness
2. state transition clarity
3. failure handling
4. testability
5. readability and naming

Useful practical rules:

- prefer one obvious main success path
- keep early validation and early return simple
- avoid boolean flags that hide mode changes
  example: `processPayment(order, true)` is much weaker than `authorizePayment(order)` or
  `capturePayment(order)` because the boolean hides that the method has two different behaviours
- make transaction boundaries visible
- keep database, queue, and network calls explicit
- prefer small business types over passing raw values everywhere
- remove dead branches and speculative abstractions

For payment or checkout code, add one more rule:

- make operations that could duplicate a real effect on retry painfully obvious

Example:

- good: `paymentGateway.charge(...)` sits in one obvious place with duplicate-protection, such as an idempotency key, or a clear retry note nearby
- weak: charge call is hidden inside a mapper, helper, or callback and reviewers have to hunt for it

---

## 5. What To Say In Code Review Questions

If they ask what you look for in a pull request (`PR`), a strong order is:

1. Is it correct?
2. Are the state changes and side effects obvious?
3. Is the failure behavior sane?
4. Is the code easy to test and evolve?
5. Are names, boundaries, and duplication acceptable?

Small concrete example:

- if a PR changes checkout flow, first check whether duplicate payment or duplicate order creation became easier by mistake
- then check whether the side effects are still visible
- then check whether tests cover the failure case
- only after that worry about naming polish or refactoring style

Good senior line:

> I review correctness first, then clarity of state changes and side effects,
> then testability and maintainability. I care much more about whether the code
> is safe to evolve than whether it is stylistically clever.

---

## 6. Common Mistakes

- names that do not tell you what the code is doing
- one method trying to do the whole job at once
- state changes happening in places that look harmless
- too many raw strings, numbers, and booleans instead of meaningful business-specific types
- too much nesting, so the main path is hard to read
- extra abstraction before there is a real second use case
- code that looks tidy but ignores real backend failure behaviour

Small examples:

- vague name:
  `handle(data)` tells you almost nothing; `authorizePayment(request)` tells you the business action
- one method doing everything:
  one method validates the request, charges the provider, saves the row, sends email, and maps the response
- hidden state change:
  helper method quietly changes `order.status` without its name making that obvious
- too many raw values:
  `createOrder("U-123", "sku-9", 2, true, "JPY")` is harder to read and easier to misuse than using clearer types such as `UserId`, `Sku`, `Quantity`, or a request object
- deep nesting:
  five layers of `if` blocks make the main success path hard to see; early validation and early return usually make the flow clearer
- premature abstraction:
  `PaymentHandlerFactory -> PaymentStrategyResolver -> AbstractPaymentProcessor` exists even though there is still only one provider
- tidy but unsafe code:
  code reads nicely, but a timeout, retry, or partial transaction can still duplicate a charge or leave state inconsistent

Important reminder:

> Clean code is not separate from backend correctness. Code is not clean if it
> hides retries, transactions, or dangerous side effects behind pretty names.

---

## 7. Practical Summary

Good short answer:

> In backend work, clean code means the intent, state transitions, and side
> effects are obvious enough that another engineer can change the code safely.
> I look first at correctness and failure handling, then at naming, structure,
> and testability. I prefer simple flows with explicit boundaries over abstract
> designs that hide what the system is really doing.

Good follow-up answer for a take-home:

Here, `take-home` means a coding exercise you complete outside the live session.

> I optimize for code that is easy to review under time pressure: obvious names,
> explicit validation, visible database and network boundaries, and tests around the risky
> branches rather than trying to impress with patterns.
