# Java Modern Features Lab

This lab is a small Java `21` companion for the Java refresh path.

Use it after:

- [../../topics/java/03-modern-java-for-backend-engineers.md](../../topics/java/03-modern-java-for-backend-engineers.md)
- [../../topics/java/02-java-concurrency-and-jmm.md](../../topics/java/02-java-concurrency-and-jmm.md)
- [../../topics/java/05-concurrency-in-production.md](../../topics/java/05-concurrency-in-production.md)

It demonstrates:

- records
- sealed types
- pattern matching
- virtual threads
- virtual threads with a request budget mindset

## How To Run

From this folder:

```bash
mvn compile
java -cp target/classes modernjava.ModernJavaFeatures
java -cp target/classes modernjava.VirtualThreadsRequestBudgetLab
```

What to observe:

- `ModernJavaFeatures` shows the language and runtime features
- `VirtualThreadsRequestBudgetLab` shows the backend point more clearly:
  virtual threads make blocking waits cheaper, but request budgets still matter

Smallest mental model:

- modern Java features help model data and control flow more clearly
- virtual threads help concurrency shape, not database, network, or timeout reality

Use this lab as a supporting example, not as a full project template.
