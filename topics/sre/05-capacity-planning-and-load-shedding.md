# Capacity Planning And Load Shedding

Systems do not fail only because code is wrong.
They also fail because demand grows faster than capacity, or because one dependency slows down and the whole service starts queueing behind it.

This note is about keeping systems stable before saturation becomes an outage.

---

## Smallest Mental Model

Capacity planning is really the study of which limit fills first, how quickly it
fills under stress, and what the system should do before overload turns into a
wider failure.

That is why the topic belongs to backend and `SRE`, not only to infrastructure.

Small concrete example:

- traffic spike plus a slow payment provider keeps requests in flight longer
- thread pools and DB connections start filling even though CPU still looks acceptable
- the stronger response is not "wait for a crash" but shed non-critical work, tighten admission, or degrade gracefully before the whole service saturates

---

## 1. What Capacity Planning Means

Capacity planning means estimating how much load the system can handle safely, and deciding what needs to change before normal traffic or spikes exceed that limit.

Simple version:

- how much traffic can we serve?
- how much headroom do we have?
- what breaks first if traffic rises or dependencies slow down?

This is not only an infra topic.
Backend behavior decides capacity too.

Examples:

- one slow SQL query can cut effective throughput sharply
- one bad retry policy can multiply downstream load
- one blocking call can pin a thread pool and increase queueing latency everywhere

That is why capacity planning is not only "how many servers do we need?"
It is also "how expensive is one unit of work, and what happens when work stops finishing quickly enough?"

---

## 2. Throughput, Latency, And Saturation

These three ideas must be connected.

- throughput = how much work the system completes
- latency = how long work takes
- saturation = how close the system is to a limit

A common failure pattern is:

1. traffic rises
2. one dependency slows down
3. requests stay in flight longer
4. thread pools, DB pools, or queues fill up
5. latency rises more
6. failures spread into parts of the system that were healthy at first

This is why saturation matters so much.
Once the system is near a hard limit, small extra pressure can create much larger instability.

Bad mental model:

- if the system is still answering, it is healthy enough

Better mental model:

- if queues, pools, or in-flight requests keep growing, the system may already be on the way to visible failure

Saturation is often an early warning that raw availability misses.

---

## 3. Headroom

Headroom means spare capacity.

If a service normally runs at 95% of a critical limit, it has very little room for:

- traffic spikes
- noisy neighbors
- dependency slowdown
- failover from another region
- canary mistakes

Capacity planning without headroom is fragile planning.

This does not mean "always overprovision massively."
It means understand where you are tight, and why.

---

## 4. The Limits That Usually Matter

In backend systems, common capacity limits include:

- CPU
- memory
- thread pools
- database connection pools
- queue consumers
- cache throughput
- dependency rate limits
- network bandwidth

Different systems hit different limits first.

A CPU-light service may still fail because:

- DB pool is exhausted
- queue backlog grows
- provider rate limit is reached

That is why capacity planning must follow the actual bottleneck, not only the easiest metric to graph.

Best approach:

- ask "what fills first?"

Sometimes the right answer is:

- not CPU
- not memory
- but DB connections, queue lag, or provider limits

That one question often leads to much better operational conversations.

---

## 5. Why Averages Hide Trouble

Capacity work goes badly when teams trust only average traffic and average latency.

That hides:

- peak periods
- bursty tenant behavior
- regional events
- flash sales
- dependency jitter

Good planning uses:

- peak traffic
- percentile latency
- concurrency shape
- backlog growth
- failure-mode scenarios

The question is not only:

- what is normal on average?

The better question is:

- what happens during the busiest and worst realistic periods?

---

## 6. Capacity Planning Inputs

A useful first-pass model usually needs:

- request rate or event rate
- concurrency
- latency distribution
- CPU and memory usage
- DB and cache behavior
- dependency-call profile
- expected growth
- known burst events

Examples of growth drivers:

- product launch
- seasonal traffic
- onboarding a large tenant
- moving traffic from one region into another during failover

Capacity planning is stronger when it includes business context, not only system charts.

---

## 7. Scaling Up, Scaling Out, And Scaling Smartly

Three common responses to capacity pressure:

- scale up: bigger instances
- scale out: more replicas
- reduce per-request cost: make the work cheaper

Many teams jump to scaling before asking whether the workload became inefficient.

Concrete example:

- adding replicas may help
- but if every request does an avoidable slow query, the system is still wasting capacity

The best answer is often a combination:

- fix the expensive path
- add replicas where needed
- tune limits and requests sensibly

Bad approach:

- add capacity everywhere without understanding the cost per request

That may buy time, but it can also hide the real bottleneck until the next spike.

---

## 8. Load Shedding

Load shedding means intentionally rejecting, delaying, or degrading some work so the system can protect the most important work.

This sounds harsh, but it is often healthier than letting everything collapse together.

Examples:

- reject recommendation traffic so checkout stays alive
- drop non-critical background work during a spike
- fail fast on a slow provider instead of letting all request threads block
- enforce tenant or client rate limits so one caller cannot consume shared capacity

Plain-English version:

Load shedding is controlled unfairness in service of keeping the important path alive.

Best approach:

- decide ahead of time which traffic is most important

If the team has not already decided that:

- checkout beats recommendations
- core writes beat analytics
- paid tenant traffic may beat free-tier background work

then overload decisions become much harder in the worst possible moment.

---

## 9. Graceful Degradation

Graceful degradation is related to load shedding, but not identical.

- load shedding removes or limits work
- graceful degradation serves a reduced but still useful experience

Examples:

- hide recommendations but keep cart and checkout working
- disable a slow enrichment call and return a simpler response
- pause low-priority sync jobs while core ordering continues

This is usually better than total failure.

---

## 10. Backpressure

Backpressure means the system signals that it cannot safely accept unlimited work at the current rate.

Examples:

- queue consumers slow intake
- API returns `429 Too Many Requests`
- worker pool stops accepting more concurrent jobs

Without backpressure, overload often becomes:

- bigger queues
- longer waits
- more retries
- more contention
- wider failure

Backpressure is how the system says "slow down before we make this worse."

Good vs bad pattern:

- bad: accept everything, let queues grow, then time out much later
- better: reject earlier with a clear signal such as `429` or bounded queue admission

Earlier controlled rejection is often kinder than slower hidden failure.

---

## 11. Retry Storms

Retries are useful, but they can destroy capacity when used badly.

Concrete example:

1. payment provider gets slow
2. your service retries aggressively
3. callers retry your service too
4. thread pools and queues fill
5. the original dependency issue becomes a full-system overload problem

This is why capacity planning must include retry behavior.

A system under stress does not need unlimited optimism.
It needs controlled failure.

Best approach:

- retries should be bounded
- they should include backoff
- and they should stop when retrying will only amplify pressure

Retry policy is part of capacity design, not only error handling style.

---

## 12. Example: Flash Sale Checkout

Suppose a sale campaign will multiply checkout traffic by 4 for two hours.

Useful planning questions:

- which component hits a limit first?
- do we have enough headroom in app replicas and DB connections?
- can the payment provider absorb the increased rate?
- should we pre-scale instead of waiting for autoscaling?
- which features can degrade if pressure grows?
- what should alert first during the event?

Possible protective actions:

- pre-scale critical services
- tighten rate limits on non-essential APIs
- disable expensive optional features
- set clear rollback thresholds for new releases
- watch queue lag and error-budget burn, not only CPU

Capacity planning is strongest when it becomes a plan for a known event, not only a spreadsheet.

---

## 13. Where Teams Usually Fail

Common mistakes:

- assuming autoscaling solves everything
- planning for average traffic instead of realistic peaks
- ignoring connection pools and queue lag
- allowing non-critical work to share critical capacity with checkout or payment
- forgetting dependency rate limits
- adding retries that amplify overload

Another common mistake:

- scaling the service but not the database or provider behind it

---

## 14. A Practical First Pass

For one critical service, do this:

1. identify the main user journey
2. measure normal and peak traffic
3. identify the first likely bottlenecks
4. define the protection rules for overload
5. define which work can degrade or shed
6. rehearse one spike scenario before you need it

That gives more value than pretending exact long-range forecasts are always possible.

---

## 15. 20-Second Answer

> Capacity planning is about understanding how much real load the system can handle safely,
> where it will saturate first, and what protections exist before overload becomes an outage.
> Load shedding and graceful degradation are deliberate ways to protect critical paths when demand or dependency failure would otherwise collapse the whole system.

---

## 16. What To Internalize

- capacity is shaped by software behavior, not only infrastructure size
- saturation in pools, queues, or dependencies often matters more than raw CPU
- headroom is what gives the system room to survive spikes and partial failure
- controlled rejection or degradation is often safer than total overload
