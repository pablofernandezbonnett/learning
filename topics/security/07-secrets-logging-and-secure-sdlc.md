# Secrets, Logging, and Secure Delivery

Security controls need an operating path: credentials must be replaced, incidents must be investigated, and risky changes must be checked before release.

## Secrets

Keep credentials out of source, images, and logs. Give workloads only the secrets they need, use the platform's protected secret source, and define how each secret is rotated or revoked. Separate human and workload access. A secret manager helps with access and audit, but the application must still avoid copying secrets into exceptions or telemetry.

## Security Events

Log enough to explain who attempted which sensitive action, on which object, and with what outcome. High-value events include authentication failures, authorization denials, privilege changes, webhook verification failures, and unusual payment transitions. Correlate them with request or trace IDs. Do not log passwords, bearer tokens, session IDs, raw card data, or unnecessary personal data.

Decide retention and access for security logs. An audit trail for an admin action needs an actor and target; an application debug message does not replace it.

## Delivery Checks

For a new public endpoint, payment path, upload, admin action, or external integration:

1. Review the trust boundary and abuse cases.
2. Test denied access and malformed input, not only success.
3. Review dependency and configuration changes.
4. Confirm sensitive failures are observable and rollback is possible.

Keep dependency scanning and patching in normal delivery. A scanner finding still needs triage: reachability, available fix, compensating controls, and deployment timing.

See [threat modeling](./06-threat-modeling-and-business-abuse.md) for choosing controls before code review.

## Further Reading

- [OWASP Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html).
- [OWASP Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html).
