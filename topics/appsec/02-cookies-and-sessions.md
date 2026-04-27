# Cookies and Sessions

Many auth systems look different at the UI level but still depend on the same core idea:
the server needs a reliable way to recognize that later requests belong to the same user.

---

## 1. What This Topic Is

A cookie is a small piece of data the server asks the browser to store.
A session is the server-side concept of tracked user state across multiple requests.

In many applications, the cookie itself is not the important thing.
The important thing is that the cookie lets the server link the browser to a known authenticated session.
That is why session handling is really a trust and lifecycle problem, not just a storage detail.

Short rule:

> a cookie is a transport mechanism; a session is the security and state model behind it

---

## 2. Why It Matters

If you build login flows, admin panels, or browser-based APIs, you will touch sessions whether you call them that or not.
Even apps that market themselves as "token-based" often end up recreating session-like behavior in another form.

This topic matters because:

- browsers send cookies automatically
- many CSRF issues depend on cookie behavior
- many account-takeover issues are really session theft issues

Once you understand how browsers attach cookies automatically, CSRF and related topics stop feeling arbitrary.

---

## 3. What You Should Understand

The key is not to memorize every cookie flag.
It is to understand what risk each flag is trying to reduce and where it does not help.

- `Set-Cookie` and `Cookie`
- session cookie versus persistent cookie
- `HttpOnly`
- `Secure`
- `SameSite`
- session expiration
- session fixation
- why `localStorage` is not the same thing as a cookie

---

## 4. How It Breaks In Real Apps

Common failures:

- session IDs not rotated after login
- auth cookies accessible to JavaScript when they do not need to be
- missing `Secure` on sensitive cookies
- weak or missing logout behavior
- overly long session lifetime
- trusting a cookie value that users can modify directly

The common pattern behind these mistakes is misplaced trust.
Teams either trust the browser too much, or they treat session state as a convenience detail instead of a core security boundary.

---

## 5. How To Build It Better

Think about sessions as credentials with lifecycle rules.
They need safe creation, safe transport, safe storage, safe expiration, and a predictable way to be invalidated.

- regenerate session identifiers after authentication
- mark session cookies as `HttpOnly` and `Secure`
- choose `SameSite` deliberately instead of relying on vague defaults
- keep session lifetime short enough for the risk
- store an opaque session ID in the cookie, not the whole trust decision

---

## 6. What To Look For In Code and Config

The most useful review question here is simple:
"What exactly turns this browser from anonymous into authenticated, and how is that state protected over time?"

- cookie attributes in framework config
- login and logout handlers
- session rotation on privilege change
- timeout configuration
- server-side session store behavior
- any place that reads role or permission decisions from user-controlled cookie data

---

## 7. Practical Exercise

Log in to a sample app and answer:

- what cookies were created?
- which ones are auth-related?
- do they have `HttpOnly`, `Secure`, and `SameSite`?
- does the cookie value change after login or privilege elevation?

If you can answer those questions confidently in DevTools, you are already building the right mental model for later AppSec topics.

---

## 8. Resources

- base: [MDN Using HTTP Cookies](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies)
- defense: [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- practice: [PortSwigger CSRF](https://portswigger.net/web-security/csrf)

---

## 9. Internal Repo Links

- [../security/01-auth-sessions-vs-jwt.md](../security/01-auth-sessions-vs-jwt.md): deeper explanation of session-based auth, cookie behavior, and the web tradeoff versus JWT
- [../spring-boot/01-spring-boot-fast-review.md](../spring-boot/01-spring-boot-fast-review.md): practical Spring notes on secure cookies, CSRF, and browser-session behavior
