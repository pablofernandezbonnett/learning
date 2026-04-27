# OAuth 2.0 and OpenID Connect

This topic gets confusing because teams often use the words "OAuth", "login", and "JWT" as if they were interchangeable.
They are not.

---

## 1. What This Topic Is

OAuth 2.0 is an authorization framework for delegated access to protected resources.
OpenID Connect, usually called OIDC, is an identity layer on top of OAuth 2.0.

In simple terms, OAuth helps one system obtain limited access to another system on a user's behalf.
OIDC adds a standard way to know who the user is, which is why it is used for modern login and single sign-on flows.

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
You may end up storing the wrong token in the wrong place or asking one token to do a job it was never meant to do.

---

## 3. What You Should Understand

Do not try to memorize the whole standards stack on day one.
Focus first on who issues each token, who consumes it, and what decision that token is supposed to support.

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

The deeper problem is usually a weak system diagram.
If the team cannot explain the trust boundaries and token flows clearly, the implementation often ends up wrong in subtle but important ways.

---

## 5. How To Build It Better

Start with a clear flow and a clear purpose for each token.
That is more valuable than copying a library example without understanding who is trusting whom.

- use OIDC when the goal is user login and identity
- prefer authorization code flow with PKCE for modern public clients
- validate ID tokens properly
- keep scopes and token privileges narrow
- do not let "we use OAuth" substitute for an actual sequence diagram and threat model

---

## 6. What To Look For In Code and Design

This topic is partly code and partly architecture.
Review not only the implementation, but also whether the design can explain redirect handling, token storage, token validation, and the boundary between identity and API access.

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
That is not a criticism.
It is a good diagnostic: this topic becomes much easier once the sequence is visible.

---

## 8. Resources

- formal reference: [RFC 6749](https://www.rfc-editor.org/rfc/rfc6749)
- OIDC reference: [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- defense: [OWASP OAuth2 Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/OAuth2_Cheat_Sheet.html)

---

## 9. Internal Repo Links

- [../security/04-advanced-auth-and-sso.md](../security/04-advanced-auth-and-sso.md): deeper repo coverage of OIDC login flows, BFF patterns, API keys, and enterprise SSO
- [../security/01-auth-sessions-vs-jwt.md](../security/01-auth-sessions-vs-jwt.md): practical comparison of token and session models before you go deeper into OAuth and OIDC
