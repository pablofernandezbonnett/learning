# Web and API Security Decisions

An API is a set of allowed state changes and reads. Secure it by defining the caller, object, action, data, and resource budget at each boundary. A valid login or well-formed JSON body is only the start.

## Review One Request End to End

```text
caller → authenticate → authorize action and object → validate input
       → enforce workflow state → bound work and external calls
       → return only permitted data → log outcome safely
```

- **Object and field access:** check ownership or tenant scope on every object lookup and update. Limit which fields a caller may read or write; a hidden UI field is not a server rule.
- **Business sequence:** reject actions that skip required states, such as confirming an unpaid order. Apply quotas or friction where an otherwise valid flow can be abused at scale.
- **Resource use:** cap body size, pagination, expensive searches, concurrency, and paid downstream calls. Rate limits are one control, not a substitute for bounded work.
- **Outbound calls:** constrain destinations for user-influenced URLs to prevent server-side request forgery (SSRF). Validate third-party responses before using them in business decisions.
- **Uploads and exposure:** limit size and type, store files outside executable paths, and inventory public endpoints, old versions, and debug surfaces.

For an order lookup, authenticate the caller and verify they may read that exact order before returning any record. Test a valid owner, another user, another tenant, and a privileged support role. The detailed failure mechanics live in [object-level access control](../appsec/10-access-control-and-idor.md).

Use [threat modeling](./06-threat-modeling-and-business-abuse.md) to find workflow abuse before implementation. For request design, also use the [API block](../api/README.md).

## Further Reading

- [OWASP API Security Top 10](https://api-security.owasp.org/editions/2023/en/0x11-t10/) for risk categories and examples.
- [OWASP REST security cheat sheet](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html).
