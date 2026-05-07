# Authentication: Sessions, JWT, Web, and Mobile

> Primary fit: `Shared core`

You do not need a fashionable auth answer.

Authentication questions are dangerous because it is easy to give a fashionable answer
that is still wrong for the client type or threat model.

This note keeps the topic practical:

- what the authentication choice really is
- the smallest mental model for sessions and JWTs
- how web and mobile differ
- how to explain the tradeoff clearly

If you need the shortest safe default:

- for web, think secure cookies, server control, XSS/CSRF tradeoffs
- for mobile or API clients, think bearer access token plus server-controlled refresh flow
- mention PKCE for public clients when OAuth/OIDC enters the picture

Why that last bullet matters:

- a `public client` means a browser SPA or mobile app that cannot safely hide a long-term secret
- PKCE is the extra proof step that stops a stolen authorization code from being enough on its own

---

## 1. What The Auth Decision Actually Is

The main choice is usually not:

> sessions or JWT forever

The real questions are:

- where does authentication state live?
- how is it sent on each request?
- how is it renewed?
- how is it revoked?
- what changes between web and mobile clients?

Short rule:

> auth design is a tradeoff between ergonomics, revocation, client type, and attack surface

### The most common auth shapes to recognize

The most common request authentication shapes are:

- **Cookie-based session auth**: browser sends a session cookie, server looks up session state
- **Basic auth**: client sends username and password on each request
- **Bearer token auth**: client sends an access token, usually in the `Authorization` header
- **Signed URL or scoped temporary link**: request is authorized by a short-lived signature in the URL

Short rule:

- browser login flows often use cookies
- APIs and mobile clients often use bearer tokens
- Basic auth exists, but it is rarely the best answer for user-facing modern product auth

### The `Authorization` header in plain language

Two common header forms are:

```http
Authorization: Basic dXNlcjpwYXNz
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

What they mean:

- `Basic`: the client sends credentials directly on each request, usually base64-encoded `username:password`
- `Bearer`: the client sends a token; whoever holds that token can usually use it until it expires or is revoked

Important nuance:

- Base64 is not encryption
- `Basic` over plain HTTP would expose credentials immediately
- if `Basic` is used at all, it must be over HTTPS

Practical rule:

- `Basic` is acceptable for simple internal scripts, admin tooling, or very limited machine usage
- `Bearer` is the more normal answer for modern APIs, OAuth2, and mobile clients

---

## 2. Session-Based Auth: The Smallest Mental Model

Smallest session model:

1. user logs in
2. server creates a random session ID
3. server stores session state
4. browser sends the session cookie on each request

That means:

- auth state is server-side
- the cookie only carries an opaque identifier

Why it is good:

- easy revocation
- very natural for web apps
- `HttpOnly` cookies reduce XSS token theft risk

Why it is not free:

- server-side session storage is required
- cookie-based auth introduces CSRF risk
- less natural for native mobile clients

Clean web rule:

- `HttpOnly`
- `Secure`
- `SameSite`
- explicit CSRF protection when needed

### What the cookie actually looks like

The cookie itself is just a small browser value plus some delivery rules.

Typical shape:

```http
Set-Cookie: SESSION=abc123xyz; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=1800
```

What the important parts mean:

- `SESSION=abc123xyz`: the cookie name and value; in session auth this value is usually just an opaque random session ID
- `HttpOnly`: JavaScript cannot read it directly
- `Secure`: browser only sends it over HTTPS
- `SameSite`: helps restrict cross-site sending behavior
- `Path`: where the cookie should be sent
- `Max-Age` or `Expires`: how long it should live

What the server normally does:

1. user logs in successfully
2. server creates a random unpredictable session ID
3. server stores session state on the server side
4. server returns `Set-Cookie`
5. browser stores it and sends it automatically on later matching requests

Good session hygiene to remember:

- session IDs should be random and hard to guess
- rotate the session after login to reduce session fixation risk
- clear or expire the cookie on logout

---

## 3. JWT-Based Auth: The Smallest Mental Model

Smallest JWT model:

1. user authenticates
2. auth service issues a signed token
3. client sends `Authorization: Bearer ...`
4. services validate signature and claims

That means:

- auth state is mostly in the token
- services can validate locally

Why it is good:

- stateless access token validation
- convenient for service-to-service verification
- natural for mobile and API clients

Why it is dangerous if explained badly:

- JWT is not automatically better than sessions
- revocation is harder
- putting JWTs in `localStorage` for web apps creates XSS risk

Important nuance:

> JWT helps with stateless verification, not with instant revocation

### What a JWT actually looks like

A JWT has three parts:

```text
header.payload.signature
```

Each part is `Base64URL`-encoded.
That means it is encoded for transport, not encrypted by default.

Smallest mental model:

- `header`: which signing algorithm and token type are used
- `payload`: the claims, meaning the data inside the token
- `signature`: proof that the token was signed by the trusted issuer and was not modified

Smallest example:

```json
// header
{ "alg": "RS256", "typ": "JWT" }

// payload
{
  "sub": "user-123",
  "iss": "https://auth.example.com",
  "aud": "orders-api",
  "role": "customer",
  "exp": 1770000000
}
```

Claims worth recognizing:

- `sub`: subject, usually the user or client ID
- `iss`: issuer, who created the token
- `aud`: audience, which API or system the token is meant for
- `exp`: expiry time

Important rule:

- signed JWT does **not** mean secret JWT
- clients and developers can often decode the header and payload
- do not put sensitive data inside just because it is signed

### How access tokens are usually issued

The auth server normally does something like this:

1. verify the user login or OAuth grant
2. decide which client or API the token is for
3. build the claims
4. set a short expiry
5. sign the token with a server-controlled key

In practice, that signature is often:

- `HMAC` with one shared secret for simpler internal setups, or
- `RSA` or `EC` with a private key so APIs can verify using a public key

Short rule:

- access token = short-lived signed proof
- refresh token = longer-lived handle for getting a new access token

### How access tokens are actually used on requests

The normal API call shape is:

```http
GET /api/orders/123 HTTP/1.1
Host: api.example.com
Authorization: Bearer <access-token>
```

The API then usually does three things:

1. validate the token signature
2. check basic claims such as issuer, audience, and expiry
3. authorize the action based on subject, roles, scopes, or claims

That last step matters:

- authentication = "who is this?"
- authorization = "is this identity allowed to do this action?"

Good line:

> the access token proves identity or delegated access, but the API still has to apply authorization rules for the specific action.

---

## 4. The Standard Practical Pattern: Access Token Plus Refresh Token

This is the normal modern answer.

- short-lived access token
- longer-lived refresh token
- refresh token stored and controlled server-side

Why this pattern exists:

- access token stays cheap to verify
- refresh token gives you a revocation point

How refresh tokens are normally used:

1. client receives an access token and a refresh token
2. client uses the access token on normal API calls
3. when the access token expires, client sends the refresh token to the auth server
4. auth server validates the refresh token
5. auth server issues a new access token
6. often, the auth server also rotates the refresh token and invalidates the old one

Smallest refresh request shape:

```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token&refresh_token=<refresh-token>
```

Or in a custom API style:

```http
POST /auth/refresh
Cookie: refresh_token=...
```

The important idea is the same:

- the refresh token is not sent to every business API call
- it is sent only to the auth server or refresh endpoint
- its job is to obtain a new short-lived access token

Why rotation is useful:

- if a refresh token is stolen and replayed later, reuse detection becomes possible
- the auth server gets tighter control over long-lived sessions

Practical storage rule:

- access token is short-lived and used often
- refresh token is longer-lived and must be protected more carefully
- on web, refresh is often kept in a secure cookie
- on mobile, refresh is often kept in secure OS storage

Why not use the refresh token everywhere?

- it lives longer
- if stolen, it is more dangerous than one short-lived access token
- keeping it away from normal API traffic reduces exposure

Typical flow:

1. access token expires in minutes
2. client calls refresh endpoint
3. auth service validates refresh token
4. auth service issues a new access token

What this gives you:

- better security than long-lived access tokens
- practical forced logout

---

## 5. Web vs Mobile Changes The Answer

This is one of the highest-value distinctions in practice.

### Web

Best default for many web apps:

- session cookie, or
- short-lived access token plus refresh token in secure cookie-based flow

What matters most:

- XSS exposure
- CSRF protection
- browser cookie behavior

Bad default:

- long-lived JWT in `localStorage`

### Mobile

Mobile has a different threat and runtime model.

Common fit:

- bearer access token
- refresh token stored in secure OS storage

Why:

- no browser cookie model
- headers are more natural
- platform secure storage exists

Short rule:

- web answer should mention XSS and CSRF
- mobile answer should mention secure storage and PKCE

---

## 6. OAuth2, OIDC, And PKCE

You do not need a huge identity lecture in most backend discussions.
You do need a clean mental model.

- **OAuth2**: delegated authorization. It is the framework for letting one system obtain tokens to call another system.
- **OIDC**: identity layer on top of OAuth2. It adds standard login and user-identity information on top of the OAuth flow.

Authorization code flow is the standard secure interactive flow.

### OAuth2 in one minute

The most useful OAuth2 roles to remember are:

- **Resource Owner**: the user
- **Client**: the app trying to act for the user, such as a web frontend or mobile app
- **Authorization Server**: the system that logs the user in and issues tokens
- **Resource Server**: the API that receives the token and protects data

Practical Spring or Hybris mental model:

- user is redirected to the authorization server
- user logs in there
- the client gets an authorization code
- the client exchanges that code for tokens
- later the API checks the access token on each request

That is why OAuth2 appears so often in enterprise Java stacks:

- the login and token issuing concern is separated cleanly from the business API
- one authorization server can support several clients and APIs

If you know Okta, the practical framing is:

- Okta is an implementation and product
- OAuth2 and OIDC are the protocol models underneath

### The authorization code flow in plain language

1. client redirects user to the authorization server
2. user authenticates there
3. authorization server redirects back with an authorization code
4. client exchanges that code for tokens
5. client uses the access token when calling APIs

For backend work, this is the highest-value OAuth2 flow to remember.

PKCE stands for `Proof Key for Code Exchange`.

What problem PKCE solves:

- in the authorization code flow, the client first gets an authorization code
- if an attacker steals that code before it is exchanged for tokens, they may try to redeem it
- public clients such as SPAs and mobile apps cannot rely on a hidden client secret to stop that

What PKCE changes:

1. the client creates a one-time random secret called the `code verifier`
2. the client sends only a transformed version of it, called the `code challenge`, in the first step
3. when exchanging the authorization code for tokens, the client must prove it still has the original `code verifier`

That means a stolen authorization code is not enough by itself.
The attacker would also need the one-time verifier.

PKCE matters because:

- SPAs and mobile apps are public clients
- they cannot safely keep a client secret
- PKCE protects the authorization code exchange from interception abuse

Smallest practical rule:

- if OAuth login is happening in a browser SPA or a mobile app, say `authorization code flow with PKCE`
- if you only say `OAuth` and stop there, the answer sounds incomplete

Short line:

> for SPAs and mobile clients I use authorization code flow with PKCE because public
> clients cannot safely hold a client secret

Short distinction worth knowing:

- OAuth2 answers: "how does this client get delegated token-based access?"
- OIDC answers: "how do we standardize user login identity on top of that?"

If the discussion is backend-focused and not identity-heavy, `OAuth2 authorization code flow with PKCE` is usually the main phrase that matters.

If the discussion moves into Spring APIs, reopen:

- [03-spring-and-jvm-appsec.md](./03-spring-and-jvm-appsec.md)

That note covers the practical difference between OAuth2 scopes, Spring roles, and authorities.

---

## 7. CSRF And XSS: The Web Auth Trap

When the client is a browser, you must separate two different risks.

- **XSS**: attacker script steals readable tokens or abuses the page
- **CSRF**: browser sends authenticated cookies automatically on a forged request

These two are often mixed up, but the danger is different:

- `XSS` is "bad JavaScript runs inside your page context"
- `CSRF` is "the browser sends your cookie to the server even though the request came from a malicious page"

That is why one defense does not automatically solve the other.

### XSS in plain language

If an attacker manages to inject JavaScript into your page, that script may:

- read tokens from `localStorage` or other readable browser storage
- call your own backend as the logged-in user
- alter the page and trick the user

That is why `HttpOnly` cookies help against token theft:

- the browser can still send the cookie
- but injected JavaScript cannot read the cookie value directly

Important limit:

- `HttpOnly` helps against token theft
- it does **not** by itself stop the browser from sending the cookie on a forged request
- so it does not solve CSRF alone

### CSRF in plain language

CSRF matters mainly when authentication depends on cookies that the browser sends automatically.

Smallest example:

1. user is logged into `shop.example.com`
2. browser has a valid session cookie for that site
3. user visits a malicious page
4. malicious page submits a hidden form or triggers a request to `shop.example.com/change-email`
5. browser attaches the session cookie automatically
6. server may think the request is legitimate unless it checks for CSRF protection

That is the classic form case you are remembering:

- the server renders a form
- the form includes a hidden CSRF token
- when the form is submitted, the backend checks that token
- a third-party malicious site cannot read the real token value, so it cannot forge the request correctly

Smallest form example:

```html
<form action="/transfer" method="post">
  <input type="hidden" name="_csrf" value="abc123" />
  <input type="text" name="amount" />
</form>
```

The important idea is not the exact field name.
It is that the request carries a secret value the attacker site cannot know.

That is why cookie-based auth needs CSRF mitigation:

- `SameSite`
- CSRF token pattern
- careful state-mutating endpoint protection

What those mitigations mean:

- `SameSite`: tells the browser to restrict when cookies are sent on cross-site requests
- `CSRF token pattern`: server expects a per-session or per-request token that a third-party site cannot guess
- careful state-mutating endpoint protection: be stricter on `POST`, `PUT`, `PATCH`, and `DELETE` than on harmless reads

### Why bearer tokens behave differently in the browser

If the frontend stores an access token and manually sends it in the `Authorization` header:

- the browser does **not** automatically attach that header to a forged cross-site form submit
- that reduces the classic CSRF shape

But the tradeoff is:

- if the token is stored in readable browser storage, `XSS` can steal it

So the usual web tradeoff is:

- cookie flow -> better protection against token theft, but you must handle CSRF carefully
- readable browser token flow -> less classic CSRF exposure, but much more XSS/token theft risk

And why readable browser storage is dangerous for tokens:

- `localStorage` is easy for injected JS to read

Short rule:

> cookie-based auth reduces token theft risk but introduces CSRF considerations

---

## 8. Decision Rules

- web app with classic browser constraints -> sessions or secure cookie-based token flow
- native mobile app -> bearer tokens in secure OS storage
- microservice verification -> signed access tokens can be useful
- fast revocation requirement -> keep a server-controlled refresh boundary
- "just use JWT everywhere" -> weak answer unless the client model is explained

---

## 9. Choice By Use Case

### Browser web app

- sessions: good default
- cookie-based access + refresh flow: also good
- long-lived JWT in `localStorage`: no
  Why: browser XSS and CSRF tradeoffs matter more than token fashion.

### Native mobile app

- bearer access token: yes
- refresh token in secure OS storage: yes
- PKCE when OAuth/OIDC is involved: yes
  Why: mobile has no normal browser-cookie model and does have platform secure storage.

### Internal service or machine-to-machine call

- signed access token: often useful
- full browser-style session flow: usually no
  Why: the main need is service identity and claim verification, not browser ergonomics.

### Need strong server-side revocation control

- session or server-controlled refresh token: yes
- pure long-lived JWT: weak fit
  Why: instant or near-instant revocation needs some server-controlled state.

---

## 10. Signed URLs: Related, But Not The Same As Login Auth

A signed URL is not a normal user login session.
It is a temporary link that carries enough signed information to authorize one narrow action for a short time.

Typical use:

- temporary file download
- direct upload to object storage
- one short-lived access link to a private resource

Smallest example shape:

```text
https://files.example.com/download/report.pdf?expires=1770000000&signature=abc123...
```

What makes it work:

- the URL includes the resource path, expiry time, and often some extra constraints
- the server signs that data
- when the request arrives, the server recomputes and verifies the signature

Why teams use signed URLs:

- you can grant very narrow temporary access
- you do not need to expose the full login token to another system just to fetch one file

Short rule:

- signed URL = temporary scoped access to one resource
- access token = broader API authorization

---

## 11. The Big Traps

1. **"JWT is always modern, so it is always better"**
   Example: browser app ends up with long-lived tokens in `localStorage`.

2. **Forgetting CSRF when cookies are used**
   Example: the cookie is secure and `HttpOnly`, but state-changing requests still lack CSRF protection.

3. **Talking about stateless JWT validation as if revocation were solved**
   Example: token validates cryptographically, but there is still no strong forced-logout story.

4. **Ignoring client type**
   Example: giving the same answer for browser, mobile, and internal service calls.

5. **Forgetting PKCE for public clients**
   Example: an SPA or mobile OAuth explanation sounds fine until someone asks how the authorization code exchange is protected.

---

## 12. 20-Second Answer

Use this as a reasoning answer, not as a claim that you have implemented every variant personally:

> I do not choose sessions or JWT as ideology. I choose based on where auth state should
> live, how the client sends it, and how revocation works. For web I usually prefer secure
> cookie-based flows because browser security matters a lot; for mobile I prefer bearer
> access tokens with refresh tokens in secure OS storage. My standard modern answer is
> short-lived access tokens plus server-controlled refresh tokens, and for public clients I
> use OAuth authorization code flow with PKCE.

---

## 13. 1-Minute Answer

> The real auth decision is about state, renewal, revocation, and client context. Sessions
> keep state on the server and work naturally for web apps, especially with `HttpOnly`,
> `Secure`, and `SameSite` cookies, but they require CSRF thinking because browsers send
> cookies automatically. JWT access tokens are useful when services need stateless signature
> verification or when mobile/API clients send bearer tokens, but JWT alone does not solve
> revocation well. That is why I usually use short-lived access tokens plus longer-lived
> refresh tokens controlled server-side. For web I avoid long-lived tokens in `localStorage`
> because of XSS exposure. For mobile I store tokens in OS secure storage. If OAuth login is
> involved, I use authorization code flow with PKCE for SPAs and mobile because public clients
> cannot safely keep a client secret.

---

## 14. What To Internalize

- the real question is auth state design, not token fashion
- sessions and JWTs solve different problems with different tradeoffs
- web auth must mention XSS and CSRF
- mobile auth should mention secure storage
- access token plus refresh token is the practical modern baseline
- PKCE is mandatory language for public OAuth clients
- JWT payload is signed, not secret by default
- OAuth2 is the main practical protocol to remember; OIDC is the identity layer on top
