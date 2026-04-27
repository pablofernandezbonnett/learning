# Access Control and IDOR

Broken access control is one of the highest-value AppSec topics for developers because it maps directly to everyday backend features.
These bugs are often simple, severe, and easy to miss if tests only cover the happy path.

---

## 1. What This Topic Is

Access control is the logic that decides whether a user may perform an action or access a resource.
IDOR, insecure direct object reference, is a common failure mode where a user can access another resource by changing an identifier.

In practice, this often looks disappointingly simple.
The route is protected, the user is logged in, and the app still leaks or mutates another user's data because ownership was never checked on that specific object.

Short rule:

> route protection is not enough; you must authorize access to the specific object and action

---

## 2. Why It Matters

This is where real business damage happens:

- reading another user's invoice
- editing another tenant's data
- calling admin-only operations
- refunding, cancelling, or deleting things without permission

These are not theoretical edge cases.
They are normal CRUD mistakes with security impact.
That is exactly why they matter so much for software engineers.
They hide inside routine feature work, not only inside obviously "security-related" code.

---

## 3. What You Should Understand

The core question is always the same:
why is this actor allowed to perform this action on this resource right now?
If the code cannot answer that cleanly, the authorization model is probably weak.

- horizontal access control
- vertical access control
- object-level authorization
- ownership checks
- deny by default
- least privilege

---

## 4. How It Breaks In Real Apps

Common failures:

- endpoint checks only "is authenticated"
- resource fetched by client-supplied ID with no ownership check
- role checks happen in UI only
- hidden admin URLs mistaken for protection
- state transition rules missing on sensitive actions

These failures often survive because teams test the success case thoroughly and the failure case barely at all.
Broken access control is as much a testing discipline problem as an implementation problem.

---

## 5. How To Build It Better

Make authorization a first-class part of business logic.
The code should make it obvious which combinations of user, role, ownership, and object state permit the action.

- validate permission on every relevant request
- combine actor, resource, and action in the authorization decision
- keep authorization logic explicit and testable
- write tests for both allowed and forbidden cases
- avoid trusting any client-supplied identifier without a server-side access check

---

## 6. What To Look For In Code

When you see a client-controlled ID, slow down.
That is one of the clearest signals that an object-level authorization check should exist nearby, either in the service layer or in a well-defined policy component.

- methods like `findById(id)` followed by immediate return
- controller paths containing user- or object-controlled IDs
- service methods with no `principal`, actor, or permission context
- only-success authorization tests
- admin features hidden in the UI but callable directly

---

## 7. Practical Exercise

Take a route like `GET /users/{id}` or `GET /orders/{id}` and answer:

- who is allowed to access this object?
- where is that rule enforced?
- what happens if I change the ID?
- do tests prove both `200` and `403` or `404` behavior?

If you cannot point to the exact line or function that enforces the rule, the implementation is probably relying on assumptions instead of clear authorization.

---

## 8. Resources

- defense: [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
- IDOR-specific defense: [OWASP Insecure Direct Object Reference Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Insecure_Direct_Object_Reference_Prevention_Cheat_Sheet.html)
- practice: [PortSwigger Access Control Vulnerabilities](https://portswigger.net/web-security/access-control.html)

---

## 9. Internal Repo Links

- [../security/02-web-and-api-security.md](../security/02-web-and-api-security.md): broader repo treatment of broken access control, BOLA, and workflow abuse in product systems
- [../spring-boot/16-appsec-authz-lab.md](../spring-boot/16-appsec-authz-lab.md): concrete lab on object-level authorization in a Spring API
- [../security/03-spring-and-jvm-appsec.md](../security/03-spring-and-jvm-appsec.md): framework-oriented follow-up on request security, method security, and permission checks in Spring
