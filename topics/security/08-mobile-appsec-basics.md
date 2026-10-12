# Mobile Application Security Boundary

A mobile app runs on a device the user controls. It can be inspected or modified, so the server must not treat an “official app” claim or a hidden value in the package as authorization.

## Design for a Public Client

Use a login flow suited to a client that cannot keep a long-term secret, such as authorization code with PKCE where OAuth/OIDC applies. Store sensitive tokens using platform protection and minimize what is kept on-device. Define token renewal, revocation, and behavior when a device is lost. The backend still enforces object access and sensitive workflow rules.

## Review the Device Surface

- Use TLS with normal certificate validation; pinning adds operational risk and needs a rotation plan if chosen.
- Review deep links, exported components, WebViews, file sharing, and clipboard or notification exposure.
- Know what third-party SDKs collect and which permissions they gain.
- Keep secrets out of the app package. Obfuscation and attestation can raise attack cost but cannot make client-side decisions authoritative.

Test the API with a modified client, stolen token, altered object ID, and out-of-order sensitive request. This reveals whether the backend relies on device-side checks. Use [authentication choices](./01-auth-sessions-vs-jwt.md) for token design and the [Flutter boundary note](../flutter/03-auth-api-and-mobile-boundaries.md) for that framework's implementation flow.

## Further Reading

- [OWASP MASVS](https://mas.owasp.org/MASVS/) for mobile requirements.
- [OWASP MASTG](https://mas.owasp.org/MASTG/) for testing guidance.
- [Android security](https://developer.android.com/privacy-and-security/security-best-practices) and [Apple Platform Security](https://support.apple.com/guide/security/welcome/web) for platform controls.
