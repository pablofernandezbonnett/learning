# HTTP and HTTPS for AppSec

You cannot understand AppSec well if HTTP still feels fuzzy.
Most web security problems are just normal request and response behavior used in the wrong way.

---

## 1. What This Topic Is

HTTP is the protocol browsers and clients use to talk to servers.
HTTPS is HTTP protected with TLS, which helps keep the traffic private and hard to tamper with.

At a practical level, HTTP gives you the structure of the conversation:
method, path, headers, body, and status code.
HTTPS does not change that structure.
It protects that conversation while it moves across the network.

Short rule:

> security features such as cookies, sessions, bearer tokens, and CORS all sit on top of HTTP

---

## 2. Why It Matters

If you do not understand requests, responses, headers, cookies, and redirects, later topics turn into memorized jargon.
You may recognize the words, but you will not be able to reason about what the browser is actually doing or where trust really comes from.

That matters because AppSec is rarely about exotic crypto mistakes.
It is more often about ordinary web behavior that was misunderstood, over-trusted, or combined badly.

Examples:

- session cookies travel in headers
- bearer tokens often travel in the `Authorization` header
- CORS is implemented with HTTP headers
- CSRF depends on how browsers automatically send cookies

---

## 3. What You Should Understand

You do not need protocol-level expertise yet.
You do need a stable mental model of what the client sends, what the server returns, and which parts of that exchange carry trust-sensitive data.

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
For example, a well-designed session cookie is still a poor defense if an attacker can observe or tamper with the traffic that carries it.

---

## 5. How To Build It Better

The first improvement is not complicated: make the transport predictable and safe by default.
Then make sure developers can inspect real requests and understand which headers, cookies, and responses are security-relevant.

- use HTTPS everywhere for authenticated traffic
- send sensitive cookies only with `Secure`
- know which headers are trusted and which come from users
- keep error output useful for clients but not overly revealing
- inspect real requests in browser DevTools or Burp instead of treating HTTP as theory

---

## 6. What To Look For In Code and Config

When you review a web app, do not stop at controller logic.
A lot of security posture sits one layer lower in proxy settings, cookie attributes, caching, redirects, and error handling.

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

The goal is not just to spot a bug.
The goal is to get used to seeing web security as real traffic with concrete fields, not as abstract concepts.

---

## 8. Resources

- base: [MDN HTTP Overview](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Overview)
- defense: [OWASP Transport Layer Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Security_Cheat_Sheet.html)
- practice: [PortSwigger Web Security Academy - Getting Started](https://portswigger.net/web-security/getting-started)

---

## 9. Internal Repo Links

- [../architecture/04-networking-fundamentals.md](../architecture/04-networking-fundamentals.md): longer refresher on HTTP structure, headers, cookies, and TLS at a backend level
