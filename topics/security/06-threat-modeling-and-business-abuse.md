# Threat Modeling and Business Abuse

A threat model asks what valuable state or capability a flow exposes, how a caller could misuse it, and which control will detect or stop that misuse. It is useful before the implementation fixes the wrong boundary in place.

## A Small Workflow

1. Draw the critical flow and name its assets: identities, money, private data, inventory, or admin powers.
2. Mark each trust boundary: client to API, API to provider, job to store, support tool to production.
3. Ask who can pretend to be another actor, change data, skip steps, read too much, exhaust capacity, or gain more privilege.
4. For each credible abuse case, choose a control and evidence that it works.
5. Revisit the model when a new actor, integration, or state transition appears.

The output can be a short table:

| Flow | Abuse | Control | Evidence |
|---|---|---|---|
| Read order | Change ID to another tenant's order | Object and tenant authorization | Cross-tenant test |
| Reserve stock | Hold inventory repeatedly | Per-actor limits and expiry | Load/abuse test and alert |
| Payment webhook | Replay a valid event | Signature check and idempotent transition | Replay test |

Rate limits do not prevent every business abuse case. A caller may stay below a limit while exploiting a discount, skipping checkout states, or using an internal support action too broadly. Put rules at the business state boundary and log unusual outcomes without exposing sensitive data.

Use [web and API decisions](./02-web-and-api-security.md) for common API controls and [payments](./05-payment-integration-patterns.md) for uncertain financial states.

## Further Reading

- [OWASP Threat Modeling Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Threat_Modeling_Cheat_Sheet.html).
- [OWASP API Security Top 10](https://api-security.owasp.org/editions/2023/en/0x11-t10/) for API abuse categories.
