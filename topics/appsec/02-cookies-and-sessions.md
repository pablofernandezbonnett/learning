# Cookies and Sessions

Many auth systems look different at the UI level but still depend on the same core idea:
the server needs a reliable way to recognize that later requests belong to the same user.

---

## 1. What This Topic Is

A cookie is a small piece of data the server asks the browser to store.
A session is the server-side concept of tracked user state across multiple requests.

Short rule:

> a cookie is a transport mechanism; a session is the security and state model behind it

---

## 2. Why It Matters

If you build login flows, admin panels, or browser-based APIs, you will touch sessions whether you call them that or not.

This topic matters because:

- browsers send cookies automatically
- many CSRF issues depend on cookie behavior
- many account-takeover issues are really session theft issues

---

## 3. What You Should Understand

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

---

## 5. How To Build It Better

- regenerate session identifiers after authentication
- mark session cookies as `HttpOnly` and `Secure`
- choose `SameSite` deliberately instead of relying on vague defaults
- keep session lifetime short enough for the risk
- store an opaque session ID in the cookie, not the whole trust decision

---

## 6. What To Look For In Code and Config

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

---

## 8. Resources

- base: [MDN Using HTTP Cookies](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies)
- defense: [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- practice: [PortSwigger CSRF](https://portswigger.net/web-security/csrf)
