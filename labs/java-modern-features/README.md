# Java Modern Features Lab

Use this after [modern Java](../../topics/java/03-modern-java-for-backend-engineers.md) and [concurrency in production](../../topics/java/05-concurrency-in-production.md). The task is to use language features where they clarify a real backend rule.

## Exercise 1: finite payment outcomes

Model `Succeeded(transactionId)`, `Failed(reason)`, and `Pending(nextAction)` as a closed set. Write a function that turns each outcome into an API response. Add a fourth outcome and check whether the compiler points to handling you missed.

**Guide:** a sealed type defines the allowed variants; records carry the data; an exhaustive pattern switch forces a decision for each variant. Keep payment side effects outside the formatting function.

```text
responseFor(result):
  Succeeded -> success body
  Failed    -> mapped error body
  Pending   -> accepted/pending body
```

## Exercise 2: concurrent downstream calls

A request calls stock, price, and fraud services. Give the whole request a deadline and each dependency a concurrency limit. Compare a small platform-thread pool with virtual threads under blocking waits. Record latency, timeouts, and downstream saturation.

**Guide:** start the independent calls, wait only until the shared deadline, cancel or abandon unfinished work according to the client contract, and release permits. Virtual threads reduce the cost of waiting; they do not create more downstream capacity or extend the deadline.

Check one slow dependency, a saturated dependency, cancellation, and a response that arrives after the deadline. Avoid treating a synthetic thread count as proof of production capacity.
