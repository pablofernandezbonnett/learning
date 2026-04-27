# Authentication vs Authorization

This is one of the most important distinctions in AppSec.
Many real security bugs happen because a team implemented authentication and assumed that was enough.

---

## 1. What This Topic Is

Authentication answers:

> who is this user or system?

Authorization answers:

> what is this user or system allowed to do?

Short rule:

> authn gets you into the building; authz decides which rooms you may enter

---

## 2. Why It Matters

A user can be fully authenticated and still be forbidden from:

- reading another user's record
- calling an admin endpoint
- refunding an order
- downloading another tenant's invoice

If your mental model stops at login success, you will miss the most dangerous application bugs.

---

## 3. What You Should Understand

- authn versus authz
- roles, permissions, and ownership
- horizontal versus vertical access control
- least privilege
- deny by default
- server-side enforcement

---

## 4. How It Breaks In Real Apps

Common failures:

- checking only that the user is logged in
- hiding UI links but not protecting endpoints
- trusting a client-supplied role or user ID
- missing object-level checks on resource access
- assuming middleware alone covers every business rule

---

## 5. How To Build It Better

- perform authorization checks server-side on every relevant request
- check resource ownership and role together where needed
- default to deny unless access is explicitly allowed
- make permission logic visible and testable
- treat frontend controls as convenience, not security

---

## 6. What To Look For In Code

- controller methods that accept IDs from the client
- service methods that fetch resources without checking ownership
- role checks implemented only in the UI
- places where a role or permission is read from user-controlled data
- tests that cover happy paths but not forbidden paths

---

## 7. Practical Exercise

Take a resource endpoint such as `GET /orders/{id}` and ask:

- can any authenticated user call it?
- where is ownership checked?
- what happens if I change the ID?
- do tests prove the failure path?

---

## 8. Resources

- defense: [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- defense: [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
- practice: [PortSwigger Access Control](https://portswigger.net/web-security/access-control.html)
