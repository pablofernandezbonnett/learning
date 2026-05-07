# Alerting And On-Call

Many systems have monitoring.
Far fewer have good alerting.

The difference matters because bad alerting trains people to ignore the system.

This note is about a simple practical question:

When should the system interrupt a human?

---

## Smallest Mental Model

Alerting is not "more monitoring."
It is the boundary where a signal becomes expensive enough to justify waking a
human or interrupting active work.

If the team cannot say what action the responder should take, the alert is
probably not ready to page.

---

## 1. The Goal Of Alerting

The goal is not "notify us whenever anything looks unusual."

The real goal is:

- detect meaningful user or service impact quickly
- page the right person only when human action is needed
- give enough context to start diagnosis without panic

If an alert wakes someone up at 3AM, it should be because:

- users are already harmed
- or users are very likely to be harmed soon
- and a human can do something useful about it

That last part matters a lot.

Bad alert:

- wakes someone up
- but the only action is "wait and see"

Better alert:

- wakes someone up
- and the responder has a real mitigation path such as rollback, traffic shift, failover, or feature disable

That is usually the difference between noise and operational value.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- page on any technical signal that looks suspicious

Why weak:

- it optimizes for fear of missing something and usually destroys trust in the page

Better mental model:

- page on user or business pain, or on a fast-moving signal that will require human action soon

Why stronger:

- it keeps interruption cost aligned with real urgency and real mitigation value

---

## 2. Monitoring vs Alerting

These are related but not identical.

- monitoring means collecting and observing signals
- alerting means deciding which signals justify interrupting a human

A dashboard can contain 200 metrics.
Only a small subset should page.

This distinction is one of the biggest maturity differences between beginner and mature operations.

---

## 3. What Should Page

Good paging alerts usually mean one of these:

- a user-critical journey is failing
- latency is bad enough to break real usage
- a backlog or saturation signal is heading toward real service failure
- a dependency outage requires mitigation or traffic shaping
- a rollout is causing active damage and needs human intervention

Good examples:

- checkout success rate drops below the service `SLO`
- p99 latency doubles and checkout completion drops
- queue lag keeps growing and orders are no longer processed in time
- a canary release shows elevated 5xx rate compared with baseline

Bad paging examples:

- CPU briefly crosses 70%
- one pod restarts once
- disk usage grows but is still far from a real limit
- a non-critical batch job is late by 5 minutes during business hours

Those may deserve dashboards or low-priority notifications.
They usually do not deserve a page.

Best approach:

- page on customer or business pain
- notify on important technical drift
- ticket slow-burn operational risk

That separation keeps the human interruption cost proportional to the real urgency.

---

## 4. Symptom Alerts Beat Cause Alerts

A symptom alert tells you the user-visible service is unhealthy.
A cause alert guesses why.

Symptom examples:

- checkout success rate falls below threshold
- p99 latency for payment authorization exceeds threshold

Cause examples:

- Redis CPU high
- one provider dependency timing out
- database pool almost full

Why symptom alerts are usually better for paging:

- they align with actual impact
- they reduce false positives
- they still work when the true cause is something unexpected

Cause alerts are still useful.
They are often better as:

- warning signals
- dashboard context
- ticket-generating signals

Important rule:

Page primarily on symptoms.
Use cause signals to speed diagnosis.

Common bad pattern:

- paging on every likely cause signal because teams fear missing something

That often creates the opposite outcome:

- the team sees so many pages that the truly important one becomes harder to trust

Strong alerting is selective, not nervous.

---

## 5. Noise, Fatigue, And Trust

Bad alerting creates alert fatigue.

Alert fatigue means:

- people get interrupted too often
- many alerts do not matter
- engineers stop trusting the signal
- real incidents get slower responses because noise trained the team to hesitate

A noisy alert is not harmless.
It actively damages incident response quality.

That is why mature teams prune alerts aggressively.

The question is not:

- can we detect more?

The better question is:

- can we detect the important things clearly enough that engineers still trust the page?

---

## 6. Severity Levels

Many teams use more than one alert level.

Simple model:

- page: active user impact or urgent risk that needs human action now
- high-priority notification: serious but not yet page-worthy
- ticket or backlog item: needs follow-up, not immediate action

Example:

- checkout failure rate spikes right now -> page
- one replica is unhealthy but traffic is fine -> notification
- SSL certificate expires in 20 days -> ticket

The exact labels vary by team.
The important part is the behavior attached to each level.

---

## 7. Good Alert Structure

A good alert should answer these quickly:

- what is broken?
- how bad is it?
- since when?
- which service or region is affected?
- what should I check first?

Weak alert:

- "payment latency high"

Stronger alert:

- "payment authorization p99 latency above 2s for 10 minutes in ap-northeast-1; checkout completion down 8%; recent canary release active"

The second one gives the responder a starting point.

Best approach:

- include the user-facing symptom
- include the affected scope
- include one or two high-value context clues
- avoid turning the alert text into a giant paragraph nobody can scan at 3AM

The alert should start the investigation, not try to replace it.

---

## 8. On-Call Is A System, Not Just A Rotation

People often talk about on-call as if it means:

- "someone has the phone this week"

That is too narrow.

Good on-call requires:

- clear service ownership
- known escalation path
- dashboards that support diagnosis
- runbooks for common failures
- alert thresholds people trust
- handoff between shifts when needed

If those pieces are weak, the rotation alone will not save you.

---

## 9. Runbooks

A runbook is a short operational guide for a known failure pattern.

It should help the responder answer:

- what does this alert usually mean?
- what should I check first?
- what safe mitigation options exist?
- when should I escalate?

Example runbook topics:

- payment provider latency spike
- webhook queue backlog
- database connection pool saturation
- bad canary rollout

Runbooks do not replace judgment.
They reduce wasted time during stress.

Good runbook shape:

- what this alert usually means
- what to check first
- safe first mitigations
- escalation point

If the runbook is longer than the responder can use under pressure, it is probably trying to be a full manual instead of a first-response guide.

---

## 10. Deduplication And Correlation

During one incident, many alerts may fire.

Example:

- checkout errors spike
- payment provider timeouts spike
- order queue lag rises
- pod CPU rises

If each one pages separately, the team gets spammed.

Good alerting systems try to:

- group related alerts
- suppress duplicates
- show parent symptom before child signals

This matters because operators need one clear incident picture, not 40 disconnected alarms.

---

## 11. Burn-Rate Alerting

One of the strongest alerting patterns is burn-rate alerting.

Simple idea:

- do not wait until the monthly `SLO` is already lost
- alert when the error budget is being consumed too fast

Why this is useful:

- it catches serious incidents earlier
- it ties paging directly to reliability objectives

Practical meaning:

- if errors are high enough that you will burn a large part of the budget quickly, page now

This is more useful than many raw infrastructure alerts.

Simple bad vs better framing:

- bad: page when CPU is high
- better: page when checkout is burning error budget fast

CPU might matter.
Error-budget burn tells you the service is spending reliability too quickly right now.

---

## 12. Example: Checkout Service

A reasonable first set of alerts might be:

- page when checkout success rate drops below the `SLO` threshold for a sustained window
- page when error-budget burn rate is high in both short and longer windows
- notify when PSP latency rises but checkout is still healthy
- notify when queue lag grows but is still below customer-impact threshold
- open a ticket when disk or certificate risk is real but not urgent

That is already much stronger than:

- page on CPU
- page on memory
- page on every pod restart

The system should care first about user and business impact.

---

## 13. Where Teams Usually Fail

Common mistakes:

- paging on infrastructure noise instead of service symptoms
- paging on thresholds that are crossed all the time
- sending alerts without ownership or runbooks
- not reviewing alerts after incidents
- keeping useless alerts forever because nobody wants to delete them

Another common mistake:

- building a beautiful dashboard but never deciding which signal should wake a human

---

## 14. Practical Review Questions

For every paging alert, ask:

1. does this represent real or imminent user impact?
2. can a human do something useful now?
3. does this alert fire rarely enough that people will trust it?
4. does the alert text help the responder start?
5. is there a dashboard or runbook linked from it?

If the answer is mostly no, that signal probably should not page.

---

## 15. 20-Second Answer

> Monitoring collects signals. Alerting decides which signals justify interrupting a human.
> Good paging alerts are usually symptom-based, tied to user impact or fast error-budget burn,
> and supported by runbooks and clear ownership. Bad alerting creates noise, fatigue, and slower incident response.

---

## 16. What To Internalize

- page only when human action is needed
- symptom alerts are usually better paging signals than cause alerts
- alert noise damages trust and response quality
- on-call quality depends on ownership, runbooks, dashboards, and escalation, not only the rota
