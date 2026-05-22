# Kotlin Basics Lab

This folder contains small standalone Kotlin files for quick refresh.

Topics included:

- language basics
- data classes
- null safety
- functional collection operations
- coroutines
- advanced coroutines and flows
- backend coroutine timeout and blocking-boundary practice

## How To Run

Run any file directly with `kotlinc`:

```bash
kotlinc 00-basics.kt -include-runtime -d out.jar && java -jar out.jar
```

Use one file at a time.
The goal is to reopen the language quickly, not to build a large project.

Recommended coroutine order:

1. `04-coroutines.kt`
2. `05-coroutines-advanced.kt`
3. `06-backend-coroutine-boundaries.kt`
