# OAuth 2.0 and OpenID Connect

This topic gets confusing because teams often use the words "OAuth", "login", and "JWT" as if they were interchangeable.
They are not.

---

## 1. What This Topic Is

OAuth 2.0 is an authorization framework for delegated access to protected resources.
OpenID Connect, usually called OIDC, is an identity layer on top of OAuth 2.0.

Short rule:

> OAuth is mainly about access to APIs; OIDC is what you add when you need login and identity

---

## 2. Why It Matters

This affects how you design:

- social login
- enterprise SSO
- third-party integrations
- mobile and SPA login flows
- API access with short-lived tokens

If you confuse authentication and delegated authorization here, your design gets messy quickly.

---

## 3. What You Should Understand

- authorization server
- resource server
- client
- access token
- refresh token
- ID token
- scopes
- authorization code flow
- PKCE

---

## 4. How It Breaks In Real Apps

Common failures:

- using OAuth terminology without a clear trust model
- weak redirect URI handling
- not using PKCE where it should be used
- treating an access token as an identity token
- storing long-lived tokens carelessly

---

## 5. How To Build It Better

- use OIDC when the goal is user login and identity
- prefer authorization code flow with PKCE for modern public clients
- validate ID tokens properly
- keep scopes and token privileges narrow
- do not let "we use OAuth" substitute for an actual sequence diagram and threat model

---

## 6. What To Look For In Code and Design

- redirect URI validation
- PKCE support
- token storage and refresh behavior
- issuer and audience validation for ID tokens
- whether the app can clearly explain which token is for API access and which token represents identity

---

## 7. Practical Exercise

Draw a simple auth flow with these boxes:

- browser or mobile app
- your app
- authorization server
- resource server

Then label:

- where login happens
- where consent happens
- where code exchange happens
- where API access is enforced

If you cannot draw it, you do not understand it yet.

---

## 8. Resources

- formal reference: [RFC 6749](https://www.rfc-editor.org/rfc/rfc6749)
- OIDC reference: [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- defense: [OWASP OAuth2 Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/OAuth2_Cheat_Sheet.html)
