# Advanced Authentication, B2B APIs, and SSO

Standard user login is only part of the auth surface.
Backend systems also need clear patterns for service-to-service trust, partner APIs,
and enterprise SSO with identity providers such as Okta or Active Directory.

Here is the breakdown of advanced authentication mechanisms.

---

## Why This Matters

Authentication design gets messy when the caller is no longer just "a user in a
browser." Real systems also need machine-to-machine access, partner APIs,
enterprise identity providers, and token handling choices that change the risk
profile of the whole system.

This matters because many weak auth answers name OAuth or SSO correctly but do
not explain where trust lives, where tokens live, or which pattern fits which
caller.

## Smallest Mental Model

Start by asking who the caller is:

- machine or script
- internal service
- browser user
- mobile app
- enterprise user coming from a customer identity provider

Then choose the auth pattern that matches that caller and the trust boundary.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- one auth mechanism should fit all callers
- OAuth, OIDC, SAML, API keys, and sessions are mostly interchangeable names
- saying "we use JWT" is enough explanation

Better mental model:

- different callers need different trust shapes
- the key decision is where credentials or tokens live and who can safely hold
  secrets
- enterprise SSO is often a platform integration decision, not just a login UI
  feature

Small concrete example:

- weak approach: a browser SPA stores long-lived tokens and the design stops at
  "OIDC login"
- better approach: the team explains whether the app is a confidential web app,
  BFF, SPA, or mobile client, then chooses server-side exchange or PKCE
  accordingly

Strong default:

- API keys for external machine clients when that is enough
- client credentials for service-to-service trust
- authorization code flow with PKCE for public clients
- server-side code exchange plus secure session for confidential web clients or
  BFFs

Interview-ready takeaway:

> I choose auth by caller type and token location: API keys for external machine
> access, client credentials for service-to-service trust, and OIDC code flow
> with either server-side exchange or PKCE depending on whether the client can
> safely hold a secret.

---

## 1. Basic Auth vs. API Keys (Machine-to-Machine)

When a script, a cron job, or an external partner system needs to talk to your API, you do not use a web login screen.

### A. HTTP Basic Auth
*   **How it works:** The client sends the HTTP Header `Authorization: Basic <base64(username:password)>`.
*   **The Problem:** The "encryption" is just Base64 encoding, which is trivial to decode. The *only* thing protecting the credentials is the TLS/HTTPS layer. If those credentials belong to a real user, sending their password on every request is incredibly risky.
*   **Acceptable Use Cases:** Very rare. Sometimes used for internal infrastructure scraping (e.g., Prometheus scraping a `/metrics` endpoint inside a trusted private VPC network).

### B. API Keys (The B2B Standard)
*   **How it works:** You generate a long, cryptographically strong random string (e.g., `sk_live_51H...`) and give it to the external developer. They send it in a header like `Authorization: Bearer <ApiKey>` or `X-API-Key: <ApiKey>`.
*   **Storage:** Similar to passwords, API Keys should be **hashed** (using bcrypt/argon2) in your database. You only show the plaintext key to the user *once* during creation.
*   **Rolling/Revocation:** If a key is leaked, the user can instantly revoke it in their dashboard and generate a new one.
*   *Practical tip:* For a public B2B API, validate API keys close to the edge and cache the validation result briefly if the lookup is expensive.

---

## 2. Server-to-Server (S2S): OAuth2 Client Credentials Flow

If you have two internal microservices (e.g., `OrderService` calling `BillingService`), how does `BillingService` know it's *really* `OrderService` calling and not a hacker who got inside the network?

You _could_ use API Keys, but managing hundreds of static keys across microservices is a nightmare.

**The Solution: OAuth2 Client Credentials Flow.**
1.  **Identity Provider (IdP):** You have an internal Auth server (like Keycloak).
2.  **Requesting a Token:** `OrderService` starts up. It sends its hardcoded `client_id` and `client_secret` to the IdP.
3.  **The JWT:** The IdP verifies them and replies with a short-lived (e.g., 60 minutes) JWT Access Token.
4.  **The Call:** `OrderService` attaches that JWT to its HTTP call to `BillingService`.
5.  **Validation:** `BillingService` statically verifies the JWT signature (without needing to contact the IdP).

*Why this is Senior level:* It eliminates long-lived static keys flying around your network. If a token is intercepted, it becomes useless in 60 minutes.

---

## 3. Web Login: OIDC Authorization Code Flow

When a human user clicks "Log in with Google" or "Log in with Apple", the core
goal is simple:

- let the identity provider authenticate the user
- let your app learn who the user is
- avoid handling the user's Google or Microsoft password yourself

Smallest safe flow:

1. your app redirects the user to the identity provider
2. the user authenticates there
3. the provider redirects back with a short-lived authorization code
4. that code is exchanged for tokens
5. your app verifies identity and establishes its own login state

The important nuance is where step 4 happens.

### A. Confidential Web App or BFF

If your backend is the OAuth client:

- the backend performs the code exchange
- the backend can use a `client_secret`
- the browser may only receive a secure session cookie

This is a strong fit when you want tighter server-side control and less token
exposure in the browser.

### B. SPA or Mobile Public Client

If the client is an SPA or a mobile app:

- it is a public client
- it should use authorization code flow with PKCE
- it cannot safely hold a long-term `client_secret`

In this model, tokens may live in the client runtime, so the security discussion
must include browser XSS and storage tradeoffs for SPAs, or secure OS storage
for mobile apps.

Short rule:

- backend-rendered web app or BFF -> server-side code exchange plus secure cookie/session
- SPA or mobile app -> authorization code flow plus PKCE
- do not answer only "OIDC" without explaining where tokens live

Short rule:

> If the app is a confidential web client or BFF, I keep the code exchange and
> token handling server-side and give the browser a secure session. If it is an
> SPA or mobile app, I use authorization code flow with PKCE and then discuss
> token storage and XSS/runtime tradeoffs explicitly.

---

## 4. SSO (Single Sign-On) for Corporate B2B Clients

If you build SaaS software (like Slack or Jira), large companies will say: *"We have 10,000 employees. We are not creating 10,000 accounts with passwords in your system. We want them to log in using our internal Microsoft Active Directory or Okta."*

This is **Enterprise SSO**.

### A. SAML (Security Assertion Markup Language)
*   **The Old Standard:** Built in the early 2000s. Uses bulky XML documents.
*   **How it works (High Level):** The user goes to your app. Your app redirects them to their corporate Okta portal. They log in, Okta generates a cryptographically signed XML document (the "SAML Assertion") saying "This is Bob from Sales", and bounces the user back to your app. Your app parses the XML, verifies the signature, and logs Bob in.
*   *Pros/Cons:* Mandatory for older enterprise networks, but notoriously painful to implement and debug.

### B. OIDC (OpenID Connect)
*   **The Modern Standard:** Built on top of OAuth2. Uses JSON (JWTs) instead of XML.
*   *Pros:* Much lighter, easier to parse, native to modern web and mobile apps. Replaces SAML in almost all new enterprise architectures.

Practical rule:

> For enterprise B2B customers, SSO support is often a platform requirement. OIDC is
> the cleaner modern default, but SAML 2.0 still appears in older enterprise identity
> environments, so an identity broker is often the pragmatic choice.

---

## Further Reading

- OAuth 2.0 (RFC 6749): https://datatracker.ietf.org/doc/html/rfc6749
- OpenID Connect Core 1.0: https://openid.net/specs/openid-connect-core-1_0-18.html
- Spring Authorization Server Reference: https://docs.spring.io/spring-authorization-server/reference/index.html
- Keycloak OIDC Layers: https://www.keycloak.org/securing-apps/oidc-layers
- OWASP REST Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html
