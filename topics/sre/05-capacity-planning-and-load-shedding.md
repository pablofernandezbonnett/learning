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
3. requests that already started take longer to finish
4. the limited application workers, database connections, or waiting lines fill up
5. latency rises more
6. failures spread into parts of the system that were healthy at first

This is why saturation matters so much.
Once the system is near a hard limit, small extra pressure can create much larger instability.

Bad mental model:

- if the system is still answering, it is healthy enough

Better mental model:

- if the waiting lines, used database connections, or unfinished requests keep
  growing, the system may already be on the way to visible failure

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

### How An API Refuses Work Before It Overloads

An API response that has already completed cannot be taken back. Protection
happens when each new request tries to start expensive work.

Three controls address different failure shapes:

- a rate limit is a maximum number of requests a caller may start in a time
  window; it can allow a small short burst without accepting unlimited traffic
- a simultaneous-work limit is a maximum number of expensive requests from a
  caller that may be running right now; this matters when each one becomes slow
- a cost limit treats a broad date-range search or a request that calls many
  suppliers as more expensive than a narrow search, instead of pretending both
  cost the same

For a shared API, apply these limits by authenticated client or tenant. If the
application runs in several copies, they need one shared count; otherwise each
copy lets the caller consume its own separate allowance while all of them still
hit the same database or supplier.

On rejection, return `429 Too Many Requests` and, when useful, `Retry-After`.
This tells a well-behaved caller to back off rather than retry immediately.
The permitted values are capacity and product-tier decisions, so choose them
from load tests and observed dependency limits rather than quoting a universal
number.

Strong default:

> A rate limit controls requests over time. A simultaneous-work limit controls
> how many slow operations can occupy the system now. A cost limit stops one
> broad request from being treated as cheap.

### Many Identical Requests: Run The Work Once

Browser controls such as debounce, disabling a button, or cancelling an old
search improve the browser experience. They do not protect an API: another
client can call the endpoint directly.

Suppose one authenticated client sends the *same* expensive read 10,000 times
at once: a hotel search, streaming catalogue query, product search, or account
history. The important distinction is:

- the 10,000 HTTP requests still reached the API, so they still use network,
  connection, and some application memory
- but the expensive SQL search does **not** need to run 10,000 times

The simple server-side fix is to make a safe key from the caller's access scope
and normalized read fields (for example query, filters, sort, and page). The
first request starts the expensive work. Requests with the same key wait for,
or reuse, that one result. People sometimes call this *request coalescing*; it
just means “do the identical work once.”

Do not make the key from only the visible search text. Tenant permissions,
subscription rights, region, or contracted prices can differ, so two callers
must not share a result unless they are allowed to see exactly the same data.

After that result is ready, keep it briefly in a cache when a slightly old
answer is acceptable. Then a repeat request can return the saved answer without
waiting for the database at all.

The interviewer may be pointing to one of these simpler controls:

| Control | What it does in the 10,000-identical-request case | What it does **not** solve |
| --- | --- | --- |
| Short cache | A repeat arriving after the first result is ready gets the saved result. | It cannot help the first 10,000 requests if they all arrive before the result exists. |
| Run identical work once | While the first read is running, later matching requests share that one expensive operation. | Each HTTP request can still occupy a connection while waiting. |
| Per-client simultaneous-request limit | Allows, for example, only a small number of one client's requests to be active now; rejects the rest. | It does not reuse a result and does not limit how many requests the client can make over an hour. |
| Request and connection limits at the API entry point | Stops huge bodies, too many connections, or requests that wait too long from reaching workers. | It cannot tell whether two valid searches mean the same business work. |
| Rate limit | Limits how many requests a client starts during a period such as a minute. | It is broader than needed when the only problem is repeated identical work. |

So a good low-cost order for this exact case is: short cache first, share the
unfinished identical search on a cache miss, then cap waiting and simultaneous
requests. Use a rate limit as an additional control when the client may also
send many *different* searches.

### Repeated Work Is Not The Same As A Denial-of-Service Attack

There are two real cases, and a good answer names both:

1. **Repeated identical reads.** A legitimate or buggy client repeats the same
   request. A cache and one shared expensive operation make this cheap.
2. **Denial-of-service attack.** Someone tries to exhaust network connections,
   CPU, memory, or database capacity. A cache helps only one small part of that
   problem: it cannot make 10,000 incoming connections harmless.

For the second case, protect the API before normal application work starts:

- put a reverse proxy, load balancer, or API gateway in front of the service;
  it is the public front door and can reject traffic without consuming an
  application worker
- set maximum connection counts, request/header/body sizes, and short idle and
  request timeouts there
- authenticate agencies early; apply quotas or rate limits by agency identity,
  with an IP-based fallback for unauthenticated traffic
- keep an application-level simultaneous-work limit as a second line of
  defence, so a valid but expensive request cannot fill all database workers

For a large network-level flood, the public front door needs provider or
network-level denial-of-service protection; an application process sees the
attack too late. A `429` with `Retry-After` is useful for a known client that
exceeds its allowance. A `503` with `Retry-After` is appropriate when the
service has no capacity, regardless of which client caused it.

This is useful, but it is not a complete denial-of-service defence. Ten
thousand waiting requests can still exhaust connections or memory. Bound that
too:

1. authenticate the calling agency or client, then reject invalid or oversized
   request shapes before database or supplier work starts
2. set a maximum time for the whole request; if the caller disconnects, stop
   work when the application and database can do so
3. put a small maximum on simultaneous requests and on how many may wait for
   the same unfinished result; reject the excess quickly with `429` or `503`
   and, if useful, `Retry-After`
4. enforce request-body, header, and connection limits at the API entry point,
   before requests reach application workers

This works for direct API callers, not only a UI. It has a narrow purpose:
sharing work makes repeated *identical* reads cheap. A caller can still send
many distinct valid requests. For that wider abuse case, a caller quota or rate
limit is still needed; there is no server-side trick that makes unlimited
different work free.

Strong interview answer:

> If one client sends 10,000 identical expensive reads, I build a key from its
> access scope and normalized input. One request does the expensive work and
> the rest reuse its result or a very short cache. This is different from rate
> limiting: I am not counting requests per minute; I am avoiding the same work
> many times. I also cap concurrent requests and waiting callers, because
> deduplicating work does not make 10,000 open connections harmless.

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
