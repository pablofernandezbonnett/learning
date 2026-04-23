# Secrets, Logging, and Secure SDLC

AppSec is not only endpoint design.

A backend engineer also needs to handle three operational realities well:

- secrets
- security logging and detection
- dependency and release hygiene

This is where many "good codebases" still fail.

---

## 1. Secrets Management Basics

Secrets include:

- API keys
- database credentials
- webhook signing secrets
- JWT signing keys
- encryption keys
- cloud credentials

Good rules:

- never commit secrets to source control
- never treat `application.yml` as a safe long-term vault
- prefer a proper secrets manager
- rotate secrets with intent
- use least privilege for secret access
- keep human access and workload access separate

What this means in practice:

- apps fetch secrets at runtime
- CI/CD should not spray secrets across logs and job output
- production secrets should be auditable and revocable

---

## 2. Security Logging

Logs are part of the control surface.

For backend/product systems, log at least:

- authentication failures
- authorization denials
- password or credential changes
- role or privilege changes
- suspicious rate-limit hits
- invalid workflow transitions
- webhook verification failures
- payment/refund anomalies

Do not log:

- passwords
- bearer tokens
- session IDs
- raw card data
- secrets
- unnecessary personal data

Good logging is:

- structured
- correlated with request and actor IDs
- useful for detection, not only debugging

---

## 3. Secure SDLC for a Small Team

You do not need a huge security program to improve.

For a backend team, a practical secure SDLC usually means:

- threat model risky features before implementation
- review authz and state transitions in code review
- run dependency scanning in CI
- patch known vulnerable dependencies on a regular cadence
- keep a small checklist for dangerous changes
- verify logging and alerting for high-risk flows

Examples of "dangerous changes":

- new public endpoint
- new file upload
- new payment or webhook flow
- new admin action
- new outbound integration

---

## 4. Cloud and AWS Basics

You do not need to become an infra specialist to improve security.

For an application engineer, the highest-value AWS ideas are:

- prefer IAM roles over long-lived credentials
- use Secrets Manager or an equivalent secrets system
- understand least privilege
- use CloudTrail and CloudWatch for traceability
- keep sensitive services on private networks where possible

If you only remember one AWS AppSec rule, remember this:

> Long-lived static credentials should feel suspicious by default.

---

## 5. Practical Checklist

- Are secrets absent from source, images, and logs?
- Can the application rotate or reload secrets safely?
- Are sensitive events logged without leaking sensitive data?
- Is dependency scanning part of CI?
- Are security-relevant changes reviewed with a checklist?
- Are privilege changes and admin actions auditable?
- Are webhook and payment failures visible in monitoring?

---

## 6. Interview Framing

Good short answer:

> I treat AppSec as part of the delivery lifecycle, not just endpoint design.
> That means handling secrets properly, logging the events that matter,
> scanning dependencies, and adding lightweight security checks to risky
> features like payments, uploads, auth flows, and admin operations.

---

## 7. Further Reading

- OWASP Secrets Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- OWASP Dependency-Check: https://owasp.org/www-project-dependency-check/
- AWS Well-Architected Security Pillar: https://docs.aws.amazon.com/wellarchitected/latest/framework/security.html
- AWS Well-Architected Application Security: https://docs.aws.amazon.com/wellarchitected/2024-06-27/framework/sec-appsec.html
- AWS Secrets Manager Best Practices: https://docs.aws.amazon.com/secretsmanager/latest/userguide/best-practices.html
- AWS IAM Best Practices: https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html
