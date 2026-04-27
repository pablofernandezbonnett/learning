# HTTP and HTTPS for AppSec

You cannot understand AppSec well if HTTP still feels fuzzy.
Most web security problems are just normal request and response behavior used in the wrong way.

---

## 1. What This Topic Is

HTTP is the protocol browsers and clients use to talk to servers.
HTTPS is HTTP protected with TLS, which helps keep the traffic private and hard to tamper with.

Short rule:

> security features such as cookies, sessions, bearer tokens, and CORS all sit on top of HTTP

---

## 2. Why It Matters

If you do not understand requests, responses, headers, cookies, and redirects, later topics turn into memorized jargon.

Examples:

- session cookies travel in headers
- bearer tokens often travel in the `Authorization` header
- CORS is implemented with HTTP headers
- CSRF depends on how browsers automatically send cookies

---

## 3. What You Should Understand

- request line, headers, body
- response status, headers, body
- common methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`
- redirects
- cookies and `Set-Cookie`
- same-origin versus cross-origin requests
- HTTPS and TLS at a practical level

---

## 4. How It Breaks In Real Apps

Common failures:

- sensitive traffic allowed over plain HTTP
- authentication cookies sent without `Secure`
- mixed secure and insecure resources
- over-trusting headers from clients
- exposing too much information in error responses

Transport security does not solve every problem, but without it many other controls become weak.

---

## 5. How To Build It Better

- use HTTPS everywhere for authenticated traffic
- send sensitive cookies only with `Secure`
- know which headers are trusted and which come from users
- keep error output useful for clients but not overly revealing
- inspect real requests in browser DevTools or Burp instead of treating HTTP as theory

---

## 6. What To Look For In Code and Config

- redirect rules from HTTP to HTTPS
- `Set-Cookie` attributes
- auth headers and reverse proxy behavior
- CORS configuration
- error handling and response shape
- cache behavior for sensitive responses

---

## 7. Practical Exercise

Take one authenticated request from a real app and inspect:

- method
- path
- status code
- request headers
- response headers
- cookies
- whether it would still be safe if leaked over plaintext transport

---

## 8. Resources

- base: [MDN HTTP Overview](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Overview)
- defense: [OWASP Transport Layer Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Security_Cheat_Sheet.html)
- practice: [PortSwigger Web Security Academy - Getting Started](https://portswigger.net/web-security/getting-started)
