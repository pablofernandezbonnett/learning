# Cross-Site Scripting (XSS)

XSS is the main reminder that browser-facing applications have a second execution environment: the user's browser.
If your app turns attacker-controlled input into executable browser code, you have lost control of that boundary.

---

## 1. What This Topic Is

Cross-site scripting happens when untrusted data is rendered in the browser in a way that lets it run as script or otherwise change the page's behavior.

That can happen because the data lands in HTML, in an attribute, in a URL, or inside JavaScript itself.
The browser does not care that the source was "just user content" if the page inserts it into an executable context.

Short rule:

> if the browser treats attacker input as code instead of text, you have an XSS problem

---

## 2. Why It Matters

XSS can lead to:

- session theft
- account takeover
- UI manipulation
- malicious actions performed as the user
- data exfiltration from the page

It is a frontend issue with backend causes very often.
The backend stores or returns the dangerous data, and the frontend renders it in the wrong place or with the wrong API.

---

## 3. What You Should Understand

This topic becomes easier once you stop asking "is there escaping?" in the abstract and start asking "escaping for which context?"
HTML text, attributes, JavaScript strings, and URLs do not all need the same treatment.

- reflected XSS
- stored XSS
- DOM XSS
- output encoding
- sanitization
- safe versus unsafe DOM sinks

---

## 4. How It Breaks In Real Apps

Common failures:

- inserting user data into HTML without escaping
- using `innerHTML` unsafely
- trusting rich text without proper sanitization
- putting untrusted data into JavaScript, URLs, or HTML attributes without context-aware handling

The repeated mistake is treating all output as if it were plain text.
In reality, where the data lands is what determines the risk.

---

## 5. How To Build It Better

Use safe rendering defaults first, then add exceptions very carefully.
Every time a codebase reaches for an escape hatch like raw HTML rendering, the review bar should go up.

- prefer framework defaults that escape output
- encode output for the correct context
- sanitize HTML only when you truly need to allow HTML
- avoid unsafe DOM APIs where possible
- use CSP as a defense-in-depth layer, not your first fix

---

## 6. What To Look For In Code

One good review habit is to search for sinks, not only sources.
If you know which APIs can create executable browser behavior, you can trace back to whether untrusted data can reach them.

- template rendering of user-controlled values
- `innerHTML`, `dangerouslySetInnerHTML`, or equivalent escape hatches
- rich text or markdown renderers
- direct DOM writes from query parameters, fragments, or API responses
- places where tokens or sensitive data could be exposed to injected JavaScript

---

## 7. Practical Exercise

Take one UI page that renders user data and ask:

- where does the data come from?
- which context is it rendered in: HTML, attribute, URL, JS?
- which escaping or sanitization layer protects it?
- what is the first unsafe sink on the page?

That exercise teaches you to reason about the real rendering path, not just about whether some helper function exists somewhere in the stack.

---

## 8. Resources

- defense: [OWASP Cross Site Scripting Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- DOM-specific defense: [OWASP DOM Based XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/DOM_based_XSS_Prevention_Cheat_Sheet.html)
- practice: [PortSwigger Cross-Site Scripting](https://portswigger.net/web-security/cross-site-scripting)

---

## 9. Internal Repo Links

- [../security/01-auth-sessions-vs-jwt.md](../security/01-auth-sessions-vs-jwt.md): useful repo extension for the XSS tradeoff around browser token storage and cookie-based sessions
- [../security/02-web-and-api-security.md](../security/02-web-and-api-security.md): broader backend security framing for web abuse and trust-boundary mistakes
