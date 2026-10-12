# Postmortems and Operational Review

After recovery, review how the failure reached users, how it was detected,
and why mitigation took as long as it did. The goal is a safer system and
better response next time. Describe decisions and conditions precisely
without reducing the event to personal blame.

## A Useful Postmortem

Keep the document short enough to read and specific enough to change work:

1. **Summary and impact:** what failed, for whom, and for how long?
2. **Timeline:** key changes, first symptom, alert, response, mitigation,
   and confirmed recovery.
3. **Trigger and contributing factors:** what started the failure, and which
   design or operating conditions widened or prolonged it?
4. **Detection and response:** did signals, paging, runbooks, and ownership
   help or delay mitigation?
5. **Follow-up:** a small number of owned actions tied to a finding, with
   priority and a way to check completion.

Avoid an action such as "improve monitoring." Name the missing signal and the
decision it should support, such as "page on checkout completion burn rate
before promoting the next canary."

## Example: Failed Canary

A new release reads a database column before every instance can safely
serve it. Checkout failures rise, but the canary checks process health rather
than checkout success. The review should record the compatibility mistake,
the missing user-facing signal, and how long rollback took. Follow-ups might
test mixed-version reads and gate canary promotion on checkout completion.
One sentence about an engineer being more careful would not address either
weak defense.

## Review Between Incidents

Use a short recurring review for noisy alerts, near misses, capacity
headroom, unfinished incident actions, and recurring manual mitigations.
A near miss deserves attention when low traffic or luck kept a real failure
from reaching more users.

Check whether past actions actually reduced recurrence or detection time.
Closing a ticket is not evidence that the operating risk changed.

For handling the live event, see
[incident response and triage](./04-incident-response-and-triage.md).
