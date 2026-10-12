# Service Identity and Enterprise SSO

The central question is which party the application trusts to identify a user or workload, and how that identity reaches the API with limited authority.

## Machine Clients

For a partner integration or small machine client, a distinct API key can be practical. Scope it to one integration, store and transmit it carefully, provide rotation and revocation, and log its use without logging the key. Shared human passwords are a poor machine identity.

For many services or an existing identity platform, OAuth client credentials or workload identity can issue short-lived, audience-limited access. The receiving API still checks token issuer, audience, expiry, and scopes or equivalent permissions. Network location alone does not authenticate a caller.

## Human SSO

OpenID Connect (OIDC) adds login identity to OAuth flows. A confidential web app can exchange the authorization code on the server and establish its own session. A public SPA or mobile client needs a flow suited to a client that cannot keep a secret, normally authorization code with PKCE.

Enterprise single sign-on may use OIDC or SAML. Choose based on the customer's identity provider and supported integration, then define tenant mapping, account linking, provisioning and deprovisioning, logout expectations, and support procedures. A successful signature check alone does not prove the user belongs to the intended tenant or role.

The protocol mechanics are in [OAuth and OIDC](../appsec/06-oauth2-and-openid-connect.md), and [authentication choices](./01-auth-sessions-vs-jwt.md) covers session versus token operation.

## Further Reading

- [OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0.html).
- [OWASP OAuth 2.0 cheat sheet](https://cheatsheetseries.owasp.org/cheatsheets/OAuth2_Cheat_Sheet.html).
