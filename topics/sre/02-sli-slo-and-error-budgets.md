# SLI, SLO, And Error Budgets

These terms sound simple, but teams often use them loosely.
That creates confusion fast.

The goal of this note is not to memorize three acronyms.
The goal is to understand how reliability targets shape real engineering decisions.

---

## Why This Matters

Without a clear reliability target, teams usually drift into one of two weak
habits:

- shipping changes without a shared idea of acceptable failure
- arguing about reliability with vague phrases such as "this feels unstable"

`SLI`, `SLO`, and error budget matter because they turn that vagueness into
something teams can operate against.

---

## 1. Smallest Mental Model

- `SLI` = what you measure
- `SLO` = the target you operate against
- `SLA` = the promise made externally

If you only remember one thing, remember this:

You cannot define a good `SLO` before you define a meaningful `SLI`.

That matters because many teams start with a target number first:

- "we want 99.99%"

But without a clear measured behavior, that number is mostly decoration.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- choose a number first
- then look for a graph that makes the number look official

Better mental model:

- start from a critical user journey
- define the behavior that actually matters
- then decide the target and what happens when you miss it

That order is what turns reliability targets into operating tools instead of
ceremony.

---

## 2. What An SLI Really Is

`SLI` means `Service Level Indicator`.
That is just a measured signal that tells you something about user experience or service behavior.

Examples:

- checkout success rate
- p99 checkout latency
- payment authorization success rate
- queue processing lag
- percentage of webhook deliveries completed within 60 seconds

Important rule:

A useful `SLI` should describe something that matters to the user or the business flow.

Weak `SLI`:

- CPU under 70%

Why weak:

- users may still be failing while CPU looks fine

Stronger `SLI`:

- 99.9% of successful checkouts finish in under 2 seconds

Why stronger:

- it speaks directly about user-visible behavior

Best approach:

- start from one critical user journey
- ask what "healthy" means for that journey
- only then decide the exact metric

That order usually produces stronger `SLIs` than starting from whichever infrastructure graph is easiest to find.

---

## 3. What An SLO Really Is

`SLO` means `Service Level Objective`.
It is the target you set for the `SLI`.

Concrete example:

- `SLI`: checkout success rate
- `SLO`: 99.9% successful checkouts over 30 days

Another example:

- `SLI`: percentage of requests served under 500ms
- `SLO`: 95% of requests under 500ms over 7 days

Plain-English version:

An `SLO` says "this is the level of reliability we are trying to maintain."

It is not just a dashboard number.
It is an operating target.

Bad use:

- define `SLOs`
- put them on slides
- never change release or paging behavior

Better use:

- connect the `SLO` to alerting, canary decisions, and reliability prioritization

That is what makes it operational instead of ceremonial.

---

## 4. What An SLA Is And Why It Is Different

`SLA` means `Service Level Agreement`.
This is usually customer-facing or contractual.

Concrete example:

- internal `SLO`: 99.95% monthly availability
- external `SLA`: 99.9% monthly availability

Why teams separate them:

- you want internal safety margin
- you do not want every small internal miss to become a contractual failure

Do not mix these up in conversation.

- `SLI` is the measurement
- `SLO` is the target
- `SLA` is the external promise

---

## 5. Error Budget

Error budget is one of the most useful `SRE` ideas.

Simple definition:

If your `SLO` is not 100%, then some amount of failure is allowed.
That allowed amount is your error budget.

Concrete example:

- `SLO`: 99.9% success over 30 days
- allowed failure: 0.1%

If you serve 1,000,000 requests in that window:

- you can fail 1,000 of them and still hit the `SLO`

Why this matters:

An error budget turns reliability from vague emotion into a usable tradeoff.

It helps answer:

- can we keep pushing features at normal speed?
- should we slow releases and focus on stability?
- are we spending reliability too fast?

---

## 6. Burn Rate

Burn rate tells you how quickly you are consuming the error budget.

Small concrete example:

- monthly `SLO`: 99.9%
- monthly error budget: 0.1%
- in one bad hour, errors spike enough that you use 20% of the monthly budget

That is a fast burn.
Even if the total outage time is short, the pace of failure may be serious.

This matters because:

- a short intense incident can be more dangerous than a slow mild degradation

That is why mature teams do not alert only on "did we already miss the monthly `SLO`?"
They also alert on "are we burning the budget dangerously fast right now?"

Best approach:

- use burn rate for fast detection
- use the full `SLO` window for longer-term judgment

One catches urgent incidents.
The other keeps the broader reliability picture honest.

---

## 7. Good SLOs vs Weak SLOs

### Good SLO traits

- tied to real user experience
- clearly measurable
- based on a known time window
- realistic enough to operate
- strict enough to matter

### Weak SLO traits

- based only on infrastructure health
- easy to measure but unrelated to user experience
- so strict that every small issue becomes noise
- so loose that nobody changes behavior when it is missed

Bad example:

- "all services should be fast"

Still vague:

- "p95 latency should be low"

Better:

- "99% of payment authorization requests complete under 800ms over 7 days"

Why this one is better:

- it says what is being measured
- it says how strict the target is
- it says the time window

That may sound simple, but many weak `SLOs` fail on one of those three points.

---

## 8. Availability vs Latency vs Correctness

Many systems need more than one `SLI`.

Examples:

- availability: did the request succeed?
- latency: did it succeed quickly enough?
- correctness: did it produce the right durable result?

For some flows, correctness matters more than raw latency.

Concrete example:

- inventory reservation that responds in 200ms but double-reserves stock is not healthy

This is why `SLO` design must match the system.
Not every service should use the same template.

---

## 9. Backend Example: Checkout Flow

Suppose you own checkout.

You might define:

- `SLI 1`: percentage of checkout requests that return success
- `SLO 1`: 99.9% successful checkouts over 30 days
- `SLI 2`: percentage of successful checkouts completed in under 2 seconds
- `SLO 2`: 95% under 2 seconds over 7 days
- `SLI 3`: percentage of orders that reach a consistent final state within 5 minutes
- `SLO 3`: 99.95% within 5 minutes over 30 days

Why three?

Because checkout can fail in different ways:

- requests can error immediately
- requests can become too slow
- async downstream work can get stuck later

One `SLO` rarely explains the whole reliability story.

Best approach:

- keep the set small
- but allow more than one `SLI` when the failure modes are meaningfully different

That avoids both extremes:

- one oversimplified metric
- or an unreadable wall of reliability numbers

---

## 10. Where Teams Usually Get It Wrong

Common mistakes:

- choosing infrastructure metrics instead of user-visible service behavior
- defining `SLOs` nobody uses for decisions
- creating too many `SLIs` and drowning in measurement noise
- picking targets before understanding normal traffic shape
- treating every internal tool like a customer-critical product

Another common mistake:

- defining `SLOs` but not changing release behavior when the budget is burning

If the `SLO` never affects decisions, it is mostly decoration.

---

## 11. How Error Budgets Change Decisions

Suppose a service is burning budget fast.

Practical responses may include:

- pause risky releases
- reduce canary speed
- prioritize reliability fixes over feature work
- tighten rollback thresholds
- review alerts and dashboards for missed signals

This is the point of the whole idea.

`SLO` is not only a measurement system.
It is a way to decide when reliability work should win over change velocity.

---

## 12. A Good First Pass For Most Teams

If you are starting from scratch, do not build an elaborate `SLO` framework on day one.

A good first pass is:

1. pick one critical user journey
2. define one availability `SLI`
3. define one latency `SLI`
4. choose a clear time window
5. confirm the numbers match real traffic and business expectations
6. connect the `SLO` to alerting and release decisions

That is much better than inventing 20 dashboard metrics nobody trusts.

---

## 13. 20-Second Answer

> `SLI` is the measurement, `SLO` is the target, and `SLA` is the external promise.
> Error budget is the amount of failure allowed by the `SLO`. The practical value is
> that it lets teams trade feature velocity against reliability using real signals instead of guesswork.

---

## 14. What To Internalize

- choose `SLIs` that describe user or business impact
- `SLOs` should influence operations and release decisions
- error budget is the allowed failure space, not a theory term
- burn rate matters because fast failure can be dangerous even in a short window
