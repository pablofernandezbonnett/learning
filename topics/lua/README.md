# Lua and Embedded Rules

Lua is useful when an application needs a small rule that changes independently of the main release. The application still owns inputs, side effects, final state, execution limits, and rule publication. A script only returns a bounded decision.

1. [Lua essentials](./01-language-basics.md): the language features needed to read a small rule.
2. [Embedded rule design](./02-embedded-rules.md): when to use scripts, how to contain them, and a guided pricing exercise.

This pattern can live in any host application with a suitable Lua runtime. Use ordinary application code when a rule is stable, tightly coupled to transactions, or needs broad system access.
