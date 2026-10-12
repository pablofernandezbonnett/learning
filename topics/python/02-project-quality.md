# Python Project Quality

A one-off script can start in one file. Add structure when it gains a second input format, a regular schedule, another maintainer, or a business-critical output. The aim is to make behavior easy to check and rerun.

## Small Project Shape

```text
project/
  pyproject.toml
  src/<package>/
    __init__.py
    main.py
    rules.py
  tests/
    test_rules.py
```

Keep parsing and external I/O near the edges. Put deterministic decisions in functions that can be tested without network access. A small service may add an HTTP adapter; it does not need a layer for every file.

`pyproject.toml` records package metadata and tool configuration. Use an isolated environment and record dependencies so another machine can recreate the job. Choose one formatter/linter, one test runner, and a static type checker for important code. For example, Ruff, pytest, and Pyright are options; the quality rule does not depend on those exact tools.

## Check the Risks

- Type public functions and important internal contracts.
- Validate file columns, JSON, environment variables, and provider responses before using them.
- Test empty input, duplicates, missing fields, invalid values, and partial failures.
- Make outputs repeatable: fix or inject time, random values, and external responses in tests.
- Review generated files and side effects before scheduling a script.

For data jobs, check row counts and keys before and after joins; a plausible-looking report can still duplicate or lose records. For APIs, test the HTTP contract and failure mapping, not only a service function. The [exercises](./04-exercises.md) apply both cases.

## Further Reading

- [Python packaging guide](https://packaging.python.org/en/latest/).
- [Python `venv`](https://docs.python.org/3/library/venv.html).
- [pytest documentation](https://docs.pytest.org/en/stable/).
