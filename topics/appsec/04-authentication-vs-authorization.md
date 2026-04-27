# Authentication vs Authorization

This is one of the most important distinctions in AppSec.
Many real security bugs happen because a team implemented authentication and assumed that was enough.

---

## 1. What This Topic Is

Authentication answers:

> who is this user or system?

Authorization answers:

> what is this user or system allowed to do?

The distinction sounds simple, but many production bugs come from collapsing both ideas into one.
A successful login proves identity.
It does not prove the user should access every record or every action behind the login wall.

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
This is why broken authorization often causes more direct business damage than weak login UX.

---

## 3. What You Should Understand

The useful mental model here is actor, resource, and action.
You are not only asking "who is calling?" but also "what are they trying to do?" and "to which object?"

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

Most of these are not framework failures.
They are design failures where the application never clearly expressed the permission rule in the first place.

---

## 5. How To Build It Better

Good authorization logic is explicit, boring, and easy to test.
If the rule is important, a reviewer should be able to find it and reason about it without reverse-engineering half the framework.

- perform authorization checks server-side on every relevant request
- check resource ownership and role together where needed
- default to deny unless access is explicitly allowed
- make permission logic visible and testable
- treat frontend controls as convenience, not security

---

## 6. What To Look For In Code

One of the best review habits in AppSec is to trace a client-supplied ID from the route to the database call and ask where permission is actually enforced.

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

If the answer depends on "the frontend would never show that button", the design is not secure enough.

---

## 8. Resources

- defense: [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- defense: [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
- practice: [PortSwigger Access Control](https://portswigger.net/web-security/access-control.html)

---

## 9. Internal Repo Links

- [../security/02-web-and-api-security.md](../security/02-web-and-api-security.md): broader backend view of broken access control, workflow abuse, and API trust boundaries
- [../spring-boot/16-appsec-authz-lab.md](../spring-boot/16-appsec-authz-lab.md): hands-on lab focused on object-level authorization and BOLA prevention
