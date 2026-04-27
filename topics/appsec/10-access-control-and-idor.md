# Access Control and IDOR

Broken access control is one of the highest-value AppSec topics for developers because it maps directly to everyday backend features.
These bugs are often simple, severe, and easy to miss if tests only cover the happy path.

---

## 1. What This Topic Is

Access control is the logic that decides whether a user may perform an action or access a resource.
IDOR, insecure direct object reference, is a common failure mode where a user can access another resource by changing an identifier.

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

---

## 3. What You Should Understand

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

---

## 5. How To Build It Better

- validate permission on every relevant request
- combine actor, resource, and action in the authorization decision
- keep authorization logic explicit and testable
- write tests for both allowed and forbidden cases
- avoid trusting any client-supplied identifier without a server-side access check

---

## 6. What To Look For In Code

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

---

## 8. Resources

- defense: [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
- IDOR-specific defense: [OWASP Insecure Direct Object Reference Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Insecure_Direct_Object_Reference_Prevention_Cheat_Sheet.html)
- practice: [PortSwigger Access Control Vulnerabilities](https://portswigger.net/web-security/access-control.html)
