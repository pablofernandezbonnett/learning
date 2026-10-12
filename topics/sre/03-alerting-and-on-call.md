# Alerting and On-Call

An alert should interrupt someone when users are harmed, or likely to be
harmed soon, and a responder can act. Monitoring collects signals; paging
chooses which signals justify an immediate human response.

## What Should Page?

Start with user symptoms: checkout success falls, payment confirmation is
too slow, or orders are missing a processing deadline. An infrastructure
signal such as high CPU or one restarted process is often diagnostic context,
unless it predicts urgent impact and has a clear mitigation.

| Signal | Likely route | Reason |
| --- | --- | --- |
| Checkout failures rise sharply | Page | Users are affected; rollback or traffic change may help. |
| Queue lag will miss an order deadline soon | Page | Delayed business work needs action now. |
| CPU briefly rises with no user impact | Dashboard | No immediate response is clear. |
| Disk growth will become a problem next week | Ticket | Needs planned work, not an interruption. |

An SLO burn-rate alert can detect rapid consumption of the allowed failure
budget. Use the [SLI and SLO note](./02-sli-slo-and-error-budgets.md) to define
that measurement. Do not page just because a graph looks unusual.

## Make a Page Actionable

Every page should state:

- affected user journey and current impact
- threshold, start time, and link to the relevant signals
- responsible service or team
- first safe checks and mitigation options
- escalation path when the first responder cannot contain it

A runbook is a short guide for these first actions. Keep it current through
incident use. Group duplicate alerts from one failure so the responder can
see one incident instead of a flood of pages.

## Operate the On-Call System

On-call needs a reachable owner, backup, handoff, access to the relevant
systems, and authority to take safe mitigating action. Define severity and
escalation from user impact, not from how frightening a metric looks.

Review noisy pages, missed incidents, and alerts that had no action. Remove
or reroute them. Too many low-value interruptions make urgent signals harder
to trust.

The page begins response; it does not contain the full diagnosis. Continue
with [incident response and triage](./04-incident-response-and-triage.md).
