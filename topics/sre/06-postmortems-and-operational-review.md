# Postmortems And Operational Review

An incident is expensive.
Wasting that incident by learning nothing from it is worse.

That is why mature teams do not stop at "service is back."
They also ask:

- what actually happened?
- why did the system and team allow it?
- what should change now?

This note is about how to do that without turning review into blame theater.

---

## Smallest Mental Model

A postmortem is not a story about who was wrong.
It is a structured way to explain why the incident was possible, why it lasted
as long as it did, and what changes would make the next version of the system
or team safer.

If the review does not improve future decisions, it was mostly paperwork.

---

## Bad Postmortem vs Better Postmortem

Bad postmortem:

- one-line root cause
- one person implicitly blamed
- vague action such as "improve monitoring"

Better postmortem:

- clear timeline
- root cause separated from contributing factors
- explicit review of detection and mitigation quality
- concrete owned actions tied to the actual failure

That difference is what makes postmortems operationally useful.

---

## 1. What A Postmortem Is

A postmortem is a structured review after an incident or major operational failure.

Its purpose is to:

- reconstruct what happened
- explain impact and contributing factors
- identify where detection, mitigation, or design were weak
- define concrete follow-up actions

It is not meant to prove who to blame.

---

## 2. Blameless Does Not Mean Toothless

People sometimes misunderstand "blameless postmortem."

It does not mean:

- pretend no mistakes were made
- avoid naming bad decisions
- write vague summaries so nobody feels uncomfortable

It means:

- focus on system conditions, decisions, context, and safeguards
- do not reduce the incident to "one person messed up"

Good question:

- why was this mistake easy to make and hard to catch?

Weaker question:

- who pressed the wrong button?

The first question improves systems.
The second mainly improves fear.

Best approach:

- describe the decision and the conditions around it clearly
- avoid vague blame language
- still say exactly what was weak in the system or process

Blameless works best when it stays specific.

---

## 3. The Basic Structure

A useful postmortem usually includes:

- summary
- impact
- timeline
- root cause
- contributing factors
- detection and response review
- follow-up actions

Simple definitions:

- summary: what happened in one short paragraph
- impact: who or what was affected
- timeline: key events in order
- root cause: the main technical or process failure
- contributing factors: conditions that made the incident worse or easier to trigger
- follow-up actions: specific work that reduces recurrence or impact

Good postmortem shape:

- short enough that people will read it
- specific enough that people can act on it

That balance matters.
A very long document can still be weak if it hides the real lessons.

---

## 4. Timeline Matters A Lot

The timeline is not filler.
It often reveals the real story.

Example timeline value:

- 14:02 deploy starts
- 14:07 canary error rate rises
- 14:10 alert fires
- 14:18 team acknowledges
- 14:24 rollback begins
- 14:31 recovery confirmed

Without the timeline, people argue abstractly.
With the timeline, you can ask concrete questions:

- was detection fast enough?
- was the alert good enough?
- was rollback delayed?
- did communication lag behind reality?

---

## 5. Root Cause vs Contributing Factors

Teams often oversimplify incidents into one sentence.

Example oversimplification:

- "the root cause was a bad query"

That may be true but incomplete.

A better review often separates:

- root cause
- contributing factors

Example:

- root cause: new release introduced an unindexed query on checkout read path
- contributing factor: alerting focused on CPU instead of checkout latency
- contributing factor: canary threshold was too loose
- contributing factor: rollback runbook was unclear

This gives you more than one place to improve.

Bad version:

- "bad query caused outage"

Better version:

- query regression caused the slowdown
- weak alerting delayed detection
- loose canary threshold increased blast radius

The second version is more useful because it exposes both the trigger and the weak defenses.

---

## 6. Detection And Response Review

Do not review only the code failure.
Review the operational response too.

Questions worth asking:

- which signal detected the incident?
- was that signal fast enough?
- did the page have enough context?
- was the blast radius clear early?
- was the mitigation chosen quickly enough?
- did the runbook help?

This matters because two teams can have the same technical failure and very different customer impact depending on response quality.

---

## 7. Good Follow-Up Actions

The postmortem becomes real only if it leads to useful actions.

Good actions are:

- specific
- owned by someone
- prioritized
- tied to the observed failure

Weak action:

- "improve monitoring"

Stronger actions:

- add burn-rate alert for checkout success `SLO`
- tighten canary abort threshold for payment service
- add index to `orders(created_at, tenant_id)` query path
- create runbook for PSP latency incidents

Concrete action beats vague intention.

Best approach:

- every follow-up action should answer "how would this have reduced this incident or its impact?"

If the answer is unclear, the action may be too generic to be valuable.

---

## 8. Operational Review Beyond Incidents

Operational review should not happen only after outages.

Useful regular review topics:

- alert quality
- noisy pages
- recurring near-misses
- change-failure rate
- rollback frequency
- capacity headroom before planned events
- unresolved action items from older incidents

This is how teams improve before the next painful event, not only after it.

That is why operational review should include near-misses too.

A near-miss means:

- the system almost failed badly
- but luck, low traffic, or one manual action prevented a larger incident

Those are often some of the cheapest lessons to learn from.

---

## 9. Example: Bad Canary Release

Suppose a canary introduced a schema-read mismatch.

A useful postmortem might show:

- impact: 12% of checkout attempts failed for 21 minutes
- root cause: v2 read new column before expand-and-contract rollout was complete
- contributing factor: canary validation checked pod health, not checkout success
- contributing factor: rollback ownership was unclear for the first 8 minutes
- follow-up: add schema compatibility checklist to release review
- follow-up: gate canary promotion on checkout business metrics

That is stronger than:

- "engineer forgot compatibility"

The second version may be emotionally satisfying for blame, but weak for prevention.

Best approach:

- write the postmortem so a new engineer joining next month could understand both the failure and the better future behavior

If the document only says who was wrong, it will age badly and teach little.

---

## 10. Where Teams Usually Fail

Common mistakes:

- writing the postmortem too late
- skipping timeline detail
- treating root cause as one sentence and stopping there
- creating vague actions with no owner
- never checking whether old actions were completed
- protecting feelings by removing useful specificity

Another common mistake:

- writing a document only because process requires it, then never changing the system

That is paperwork, not learning.

---

## 11. A Simple Postmortem Template

Use this minimal structure:

1. Summary
2. Impact
3. Timeline
4. Root cause
5. Contributing factors
6. Detection and response review
7. Follow-up actions with owners

This is enough for most first-pass postmortems.

---

## 12. 20-Second Answer

> A postmortem is a structured review of an incident so the team can understand impact,
> timeline, root cause, contributing factors, and what needs to change. Blameless does not mean vague:
> it means focus on system conditions and decisions, then create concrete owned follow-up actions that reduce recurrence or impact.

---

## 13. What To Internalize

- postmortems are for system learning, not personal blame
- timeline and response quality matter as much as root cause detail
- vague action items are almost as bad as no action items
- operational review should also happen before the next major incident
