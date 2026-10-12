# Choosing an Authentication Model

Authentication establishes who or what is calling. Authorization decides what that caller may do. A session ID, API key, or access token carries evidence of identity or delegated access; none replaces object and workflow authorization.

## Choose by Client and Control Needs

| Situation | Useful starting point | Main decision |
|---|---|---|
| Browser app with a backend | Server-managed session in a secure cookie | Where session state lives, how it expires, and how browser requests are protected. |
| Mobile app or SPA acting as a public client | Authorization code flow with PKCE, where delegated login is needed | Where tokens live, how refresh and revocation work, and what the client can safely hold. |
| Service-to-service call | Workload identity, client credentials, or a scoped API key | How the caller is identified, scoped, rotated, and audited. |

A JWT is a token format, not an authentication architecture. A signed JWT can be validated without a database lookup, but expiration, revocation, audience, key rotation, and claim mapping still need deliberate design. A server session makes revocation and server-side state easier to control but needs a session store or equivalent operating plan.

## Browser and Device Risks

A browser automatically sends matching cookies. Protect state-changing cookie-backed requests against cross-site request forgery (CSRF), and use cookie attributes suited to the deployment. Cross-site scripting (XSS) can still act as the user or steal accessible data; moving tokens between storage locations does not remove the need to prevent XSS.

A mobile app runs on a user-controlled device. It cannot keep a client secret confidential. Store sensitive tokens with platform protections and make the backend enforce every permission and sensitive state transition.

## Check the Token's Job

For every token or session, identify issuer, intended audience, lifetime, storage, renewal, and revocation. Distinguish an OIDC ID token, which conveys login identity to its client, from an OAuth access token, which authorizes access to a resource server. Validate the claims required by the receiving service.

See [cookies and sessions](../appsec/02-cookies-and-sessions.md), [JWT](../appsec/05-jwt.md), [OAuth and OIDC](../appsec/06-oauth2-and-openid-connect.md), and [CSRF](../appsec/09-csrf.md) for the mechanics. [Service identity and SSO](./04-advanced-auth-and-sso.md) covers the organizational choices.

## Further Reading

- [OWASP session management](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html).
- [OWASP OAuth 2.0](https://cheatsheetseries.owasp.org/cheatsheets/OAuth2_Cheat_Sheet.html).
