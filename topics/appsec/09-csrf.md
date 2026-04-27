# Cross-Site Request Forgery (CSRF)

CSRF makes much more sense once cookies and browser behavior are clear.
The key idea is simple: the browser can send a valid authenticated request that the user never meant to make.

---

## 1. What This Topic Is

CSRF is a vulnerability where an attacker causes a victim's browser to perform an unintended action on a site where the victim is already authenticated.

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

---

## 3. What You Should Understand

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

---

## 5. How To Build It Better

- use anti-CSRF tokens for sensitive browser-originating actions
- validate them strictly on the server
- use `SameSite` deliberately
- avoid mutating state through unsafe `GET` behavior
- understand when your app is cookie-backed and when it is not

---

## 6. What To Look For In Code

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

---

## 8. Resources

- base and practice: [PortSwigger CSRF](https://portswigger.net/web-security/csrf)
- prevention guidance: [PortSwigger Preventing CSRF Vulnerabilities](https://portswigger.net/web-security/csrf/preventing)
- defense: [OWASP Cross-Site Request Forgery Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)
