# Incident Response And Triage

When a production incident starts, the hardest problem is often not the final root cause.
The hardest problem is deciding what to do next while the system is still failing.

That is what incident response is about.

The goal is not to look calm or sound smart.
The goal is to reduce user impact safely and quickly.

---

## Smallest Mental Model

Incident response is not mainly about proving the root cause quickly.
It is about building the first useful picture of live impact, choosing the
safest mitigation, and confirming whether user pain is going down.

That is why symptom, blast radius, and mitigation matter earlier than perfect
explanation.

---

## Bad First Instinct vs Better First Instinct

Bad first instinct:

- explain the whole failure before acting

Better first instinct:

- identify the live symptom
- estimate scope
- choose the safest reversible mitigation
- keep diagnosing while damage is contained

During the first minutes, that order is usually faster and safer.

---

## 1. What An Incident Is

An incident is not every bug.

A practical definition is:

- a live service problem with meaningful user, business, or operational impact

Examples:

- checkout failure rate spikes
- a payment provider slowdown makes order confirmation unusable
- queue lag grows until downstream processing misses business deadlines
- a rollout introduces errors for real users

This matters because incident response is about live impact.
Not every engineering problem needs incident handling.

---

## 2. Triage Means "What Is The Problem Right Now?"

Triage is the first classification step.

At the beginning of an incident, you usually do not know the full root cause.
You still need fast answers to these questions:

- what is the user-facing symptom?
- how many users are affected?
- which services or regions are affected?
- when did it start?
- is impact still growing?
- what safe mitigation options exist right now?

Triage is about getting the first useful picture, not proving the final explanation.

Bad first question:

- "what is the exact root cause?"

Better first question:

- "what is failing right now, how wide is the impact, and what can we do safely in the next few minutes?"

That is the mindset shift that usually makes incident handling faster.

---

## 3. Symptom First, Root Cause Second

Many engineers lose time by trying to explain everything before acting.

That is risky.

Better order:

1. identify the live symptom
2. estimate blast radius
3. stop the damage if possible
4. continue diagnosis while impact is contained

Concrete example:

- canary release increases 5xx rate

Do not start with:

- "let us inspect every code path carefully first"

Start with:

- pause or rollback the canary
- confirm whether error rate improves
- then investigate the exact cause

Mitigation often matters more than perfect explanation in the first minutes.

---

## 4. Common Mitigation Options

Mitigation means reducing impact before the system is fully fixed.

Common mitigation actions:

- rollback a bad deploy
- stop a canary rollout
- disable a non-critical feature flag
- shed low-priority traffic
- fail fast instead of letting queues and thread pools saturate
- switch to a backup dependency or degraded mode
- temporarily increase capacity if that is a known safe move

Important rule:

Mitigation should be safer than the thing you are trying to fix.

Do not improvise high-risk changes under pressure unless the alternative is worse.

Best approach:

- prefer known reversible mitigations first

Examples:

- rollback before emergency refactor
- disable optional feature before changing three configs at once
- fail fast before allowing queues to grow without limit

During an incident, reversible usually beats clever.

---

## 5. Incident Roles

Larger incidents go better when people are not all doing the same thing at once.

Simple role model:

- incident lead: keeps the picture coherent and drives next actions
- operations responder: runs mitigations and checks service state
- subject matter expert: investigates the likely technical cause
- communications owner: keeps stakeholders updated

One person may cover multiple roles in a small team.
The point is still useful:

someone should drive the incident, not only participate in it.

Bad pattern:

- everyone debugs
- nobody decides
- updates become late and inconsistent

Better pattern:

- one person keeps the shared picture coherent
- others investigate or mitigate within that frame

That sounds simple, but it prevents a lot of wasted motion.

---

## 6. Communication During An Incident

Bad incident communication usually sounds like this:

- too vague
- too optimistic
- too late
- too technical for the audience

Good incident communication usually includes:

- current symptom
- affected scope
- start time
- current mitigation
- next update time

Concrete example:

> Since 14:10 JST, checkout success has dropped for a subset of users in ap-northeast-1.
> We paused the active canary rollout and are validating whether success rate recovers.
> Next update in 15 minutes.

That is much more useful than:

- "we are investigating some issues"

Best approach:

- be concrete about the symptom
- honest about uncertainty
- predictable about the next update

People handle uncertainty better than silence.

---

## 7. A Practical Diagnosis Loop

During an incident, a simple loop helps:

1. check the symptom metric
2. check blast radius by region, tenant, feature, or dependency path
3. inspect recent changes
4. use traces to find the slow or failing span
5. use logs to confirm the error pattern
6. choose the safest mitigation
7. verify whether the symptom improves

The final step matters.

Do not assume a mitigation worked.
Confirm it with the same signal that showed the incident.

---

## 8. Recent Change Is A High-Value Clue

One of the fastest useful incident questions is:

- what changed recently?

Examples:

- code deploy
- feature flag change
- traffic spike
- dependency release
- schema migration
- config change

This does not mean every incident is caused by your own deploy.
It means recent change is often the cheapest high-value lead.

---

## 9. Example: Queue Backlog Incident

Suppose webhook events are piling up.

Symptoms:

- queue lag grows
- downstream business state becomes stale
- customers see delayed updates

Possible causes:

- consumer deploy regression
- dependency timeout
- database saturation
- dead-letter loop or poison message

Possible mitigations:

- pause problematic consumer release
- route traffic away from a failing dependency if possible
- reduce retries that are amplifying pressure
- isolate poison messages into `DLQ`
- temporarily scale consumers if the bottleneck is safe and real

The right move depends on the diagnosis.
But the pattern is the same:

- contain impact
- restore flow
- only then keep digging deeper

Bad response:

- scale consumers immediately because backlog is high

Why risky:

- if the real issue is DB saturation or poison messages, scaling may increase pressure

Better response:

- confirm the bottleneck first
- then choose the mitigation that reduces pressure instead of multiplying it

---

## 10. Severity And Escalation

Not every incident has the same urgency.

Factors that raise severity:

- revenue-critical path broken
- data-loss or correctness risk
- wide regional or global impact
- fast error-budget burn
- long expected recovery time

Escalation should be clear before incidents happen.

Questions to define in advance:

- when do we pull in another team?
- when do we notify management or support?
- when do we declare a major incident?

Unclear escalation wastes time exactly when the team has the least spare attention.

---

## 11. What Not To Do

Common bad patterns:

- chasing root cause before mitigation
- making several risky changes at once
- having five people debug independently with no incident lead
- posting status updates that hide uncertainty
- forgetting to write a timeline while the incident unfolds

Another bad pattern:

- saying "it looks fixed" without checking the original symptom metrics

---

## 12. Recovery Is Not The End

Once the service stabilizes, the incident is not fully finished.

You still need:

- timeline
- customer or stakeholder summary if relevant
- root cause analysis
- follow-up actions
- alert and runbook review

The response phase protects users now.
The review phase protects users later.

---

## 13. A Simple Incident Template

For a first-response note, capture:

- start time
- symptom
- affected systems or users
- likely scope
- suspected recent changes
- mitigation attempts
- current status
- next update time

This is enough to keep the response coherent without turning the first minutes into paperwork.

---

## 14. 20-Second Answer

> Incident response is about reducing live user impact quickly and safely. Triage starts with symptom,
> scope, and blast radius, not perfect root-cause proof. The usual flow is: confirm the symptom, inspect recent changes,
> choose the safest mitigation, verify recovery, then continue with deeper analysis and follow-up.

---

## 15. What To Internalize

- triage is about the first useful picture, not the final theory
- mitigation often matters more than root-cause depth in the first minutes
- verify recovery with the same signals that showed the incident
- clear roles and updates make incidents shorter and less chaotic
