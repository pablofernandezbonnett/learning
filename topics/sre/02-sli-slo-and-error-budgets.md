# SLI, SLO, and Error Budgets

Reliability targets help a team decide whether users are getting the service
they need and when to spend time fixing it. Start with a critical user
journey, then choose a measurement and target. A percentage without a clear
journey and time window is hard to act on.

## The Smallest Model

| Term | Meaning | Example |
| --- | --- | --- |
| Service level indicator (SLI) | What is measured | Share of checkout attempts completed successfully |
| Service level objective (SLO) | Internal target for that measure over a window | 99.9% over 30 days |
| Service level agreement (SLA) | External commitment, often contractual | Published availability promise |

An SLI should reflect user-visible behavior. CPU usage can explain a failure,
but a healthy CPU graph does not prove checkout works. Define which requests
count, what success means, and whether planned maintenance or client errors
are included. Without these rules, two teams can calculate different answers
from the same traffic.

## Choose a Useful Target

For checkout, one measure may miss important failure modes. A small set could
cover:

- **Availability:** percentage of valid checkout attempts that complete.
- **Latency:** percentage of completed checkouts under a chosen time limit.
- **Completion:** percentage of accepted orders that reach a valid final state
  within a chosen period.

Pick windows and targets from product needs and observed traffic. A target
should be strict enough to prompt action and realistic enough that the team
can operate it. Avoid copying one service's target to every system.

## Error Budget and Burn Rate

An SLO below 100% allows some failure in its window. That allowance is the
error budget. With a 99.9% success target across 1,000,000 eligible attempts,
up to 1,000 attempts may fail while still meeting the target.

Burn rate describes how fast the budget is being spent. A sharp failure over
one hour can consume much of a monthly budget before the monthly SLO is
formally missed. Use fast burn to detect urgent impact and the full window to
judge the longer trend.

The budget informs choices: slow a risky rollout, fix a recurring failure,
or continue normal delivery when the service is stable. It is not a license
to ignore failures until the allowance is exhausted.

## First Pass

1. Pick one critical user journey.
2. Define eligible attempts and successful outcomes.
3. Choose one or two indicators and a clear time window.
4. Set a target that matches business need and observed behavior.
5. Decide which alert and release decisions the target will influence.
6. Revisit the definition when it hides a real user failure.

For collecting logs, metrics, and traces, see
[observability and monitoring](../devops/03-observability-and-monitoring.md).
For turning a reliability target into a useful page, see
[alerting and on-call](./03-alerting-and-on-call.md).
