# Cross-Site Request Forgery (CSRF)

CSRF makes much more sense once cookies and browser behavior are clear.
The key idea is simple: the browser can send a valid authenticated request that the user never meant to make.

---

## 1. What This Topic Is

CSRF is a vulnerability where an attacker causes a victim's browser to perform an unintended action on a site where the victim is already authenticated.

The important detail is that the browser may send the victim's cookies automatically.
So the request can look authenticated to the server even though the user never intended to trigger it from that attacker-controlled page.

Short rule:

> if the browser automatically sends the victim's session and the server cannot tell intent from background submission, CSRF becomes possible

---

## 2. Why It Matters

CSRF affects state-changing actions such as:

- changing email or password
- making a purchase
- triggering an admin action
- changing payout details

It is mainly a browser and session problem, not a generic API problem.
That distinction matters because teams sometimes apply CSRF advice to APIs that do not rely on browser-sent cookies, while missing it in the parts of the app that do.

---

## 3. What You Should Understand

The useful distinction here is identity versus intent.
The session cookie may correctly identify the user, but the server still needs a way to know the request came from a legitimate flow in the app.

- why cookie-backed auth is relevant
- request intent versus request identity
- CSRF tokens
- `SameSite` cookies
- why `GET` should not mutate state
- why referer-only checks are weak

---

## 4. How It Breaks In Real Apps

Common failures:

- no CSRF token on sensitive actions
- token present but not properly validated
- relying only on request method
- relying only on referer checks
- assuming `SameSite` alone solves every case

The common theme is partial defenses.
A team may know one CSRF mitigation exists and then overestimate what that one control covers.

---

## 5. How To Build It Better

Treat state-changing browser actions as needing both authentication and request integrity.
That is why anti-CSRF tokens and sound cookie settings work well together instead of competing with each other.

- use anti-CSRF tokens for sensitive browser-originating actions
- validate them strictly on the server
- use `SameSite` deliberately
- avoid mutating state through unsafe `GET` behavior
- understand when your app is cookie-backed and when it is not

---

## 6. What To Look For In Code

You are looking for actions that matter and that a browser can trigger automatically.
Password changes, payment actions, admin operations, and profile updates are the usual places to start.

- forms or state-changing endpoints with no request integrity token
- auth that depends only on browser-sent cookies
- change endpoints reachable by `GET`
- framework CSRF protections disabled without a clear replacement
- admin actions exposed through browser forms or links

---

## 7. Practical Exercise

Take one browser-based update action and ask:

- does the browser send auth automatically?
- what protects intent?
- is there a CSRF token?
- what is the `SameSite` setting?
- could the action be triggered cross-site?

If the answer is unclear, the flow probably needs to be drawn and reviewed, not just tested casually.

---

## 8. Resources

- base and practice: [PortSwigger CSRF](https://portswigger.net/web-security/csrf)
- prevention guidance: [PortSwigger Preventing CSRF Vulnerabilities](https://portswigger.net/web-security/csrf/preventing)
- defense: [OWASP Cross-Site Request Forgery Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)

---

## 9. Internal Repo Links

- [../security/01-auth-sessions-vs-jwt.md](../security/01-auth-sessions-vs-jwt.md): extends the discussion of cookie-based browser auth and why CSRF appears in that model
- [../spring-boot/01-spring-boot-fast-review.md](../spring-boot/01-spring-boot-fast-review.md): practical Spring-specific notes on CSRF tokens, CORS, and secure browser-session setup
