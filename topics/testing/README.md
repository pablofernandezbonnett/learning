# Testing for Backend Engineers

Use this folder for practical testing strategy, clean code, and review judgment.
The goal is not to maximize test count.
The goal is to choose the smallest test surface that gives real confidence for
the risk you are carrying.

Focus:

- test levels by risk
- real confidence versus mock confidence
- code shape that is easy to read, change, and test

Working style:

- describe the smallest useful meaning of testing terms before adding taxonomy
- tie each test choice back to the failure you are trying to catch
- keep the discussion grounded in backend change risk, not abstract purity

## Recommended Order

1. [01-testing-strategies.md](./01-testing-strategies.md): how to choose unit, integration, and end-to-end tests by failure risk rather than by habit
2. [02-clean-code-and-code-review.md](./02-clean-code-and-code-review.md): how code shape affects testability, review quality, and long-term change cost

## Core Rule

- choose the test level by the failure that matters most
- boundary realism matters more than test count alone
- clean backend code makes state changes and side effects obvious

That is why this folder treats test strategy and code shape as one discussion:
code that hides side effects usually produces weaker tests and noisier reviews.
