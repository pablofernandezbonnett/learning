# Mobile AppSec Basics for Backend and Product Engineers

Mobile AppSec is easy to misunderstand if you come mainly from backend work.

The common mistake is thinking:

- the mobile app is part of your trusted security boundary
- client-side checks are meaningful enforcement
- hiding values in the app package is close to secret storage

That mental model causes weak API design and weak auth decisions.

---

## 1. Why This Matters

If your product has a mobile client, then AppSec is not only:

- endpoint validation
- browser behavior
- Spring Security annotations

It is also:

- how a public client logs in
- what data is stored on-device
- how the app talks to your APIs
- how much trust the backend gives to device-side claims
- how SDKs, deep links, and WebViews expand the attack surface

For a backend engineer, this matters because weak mobile assumptions often
produce weak server-side security decisions.

---

## 2. Smallest Useful Mental Model

A mobile app is a `public client`.

That means:

- the user controls the device
- the app package can be inspected or modified
- tokens and local state can be targeted
- device-side checks can be bypassed

Practical rule:

> The backend must treat the mobile app as an untrusted caller with a different runtime model, not as a trusted extension of the server.

---

## 3. Bad Mental Model vs Better Mental Model

Bad mental model:

- mobile is safer than web because the app is installed
- the app can safely hold long-term secrets
- local biometric or local PIN checks are enough authorization
- the API can trust claims because they came from the official app

Better mental model:

- mobile changes the client and storage model, not the need for server-side
  enforcement
- the app still needs short-lived credentials, safe storage, and strong backend
  validation
- biometrics, local PIN, attestation, and jailbreak checks are defense-in-depth,
  not trust anchors

Small concrete example:

- weak approach: the mobile app hides an API key, decides who can refund, and
  the backend trusts the role sent by the client
- better approach: the app uses a safe login flow, stores as little as possible
  on-device, and the backend enforces authn/authz and sensitive workflow rules

---

## 4. High-Value Mobile AppSec Areas

### Authentication and Token Handling

For user login, the strong default is:

- `OAuth 2.0 Authorization Code` with `PKCE`
- short-lived access tokens
- refresh flows designed for revocation and rotation

Important rule:

> Do not treat the mobile app like a confidential client that can safely hide a long-lived secret.

### On-Device Storage

Store as little sensitive data as possible.

Use platform secure storage for sensitive material:

- Android Keystore / encrypted storage patterns
- iOS Keychain and platform protection features

Avoid:

- hardcoded API keys
- raw tokens in logs
- sensitive data in shared or backup-exposed storage
- assuming "private app storage" means "safe enough forever"

### Network Communication

High-value defaults:

- TLS everywhere
- strict certificate validation
- no cleartext traffic
- careful treatment of pinned certificates if you choose pinning

Pinning is not a magic answer.
It adds operational cost and can create availability problems if managed badly.

### Platform Interaction

Mobile attack surface is not only network traffic.

Watch:

- deep links and app links
- exported components / IPC
- WebViews and JavaScript bridges
- clipboard, screenshots, notifications, and backups
- file import/export and document sharing

### SDK and Supply Chain Risk

Third-party SDKs are part of your app's attack surface.

Questions that matter:

- what permissions does the SDK use
- what data does it collect
- how quickly can it be updated
- does it introduce hidden webviews, trackers, or unsafe native code

### Reverse Engineering and Tampering

Assume the app can be inspected.

Defense-in-depth controls can still be worth it:

- code obfuscation
- anti-debugging or integrity checks
- root or jailbreak detection
- device or app attestation

But remember:

> These controls raise cost. They do not replace server-side security.

---

## 5. What Changes for a Backend Engineer

If you mainly own the backend, mobile AppSec should change your API thinking in
these ways:

- never rely on client-side role checks
- treat device claims carefully and validate them server-side
- assume tokens can be stolen or replayed
- design revocation, rotation, and step-up paths for sensitive actions
- distinguish user presence from authorization
- model sensitive flows such as payments, transfers, account recovery, and MFA resets as abuse targets

---

## 6. Strong Default

For mobile-backed product systems, use this default:

- mobile app is a public client
- login uses modern delegated auth patterns such as `PKCE`
- secrets are not hardcoded in the app package
- sensitive material uses platform secure storage
- backend enforces authorization and workflow safety
- local protections are defense-in-depth only

---

## 7. What Good Looks Like In Practice

Strong default:

- the mobile app is treated as a public client
- long-lived secrets are kept out of the app package
- tokens use safe login, storage, rotation, and revocation patterns
- sensitive actions are always enforced server-side
- local protections help raise attacker cost, but do not define trust

Bad vs better:

- bad: "it is the official app, so the backend can trust its claims more than the web client"
- better: "the device is user-controlled, so the backend still enforces identity, authorization, and workflow rules itself"

- bad: local biometrics, PIN, attestation, or jailbreak checks are treated as the main authorization decision
- better: those signals are defense-in-depth layered on top of server-side security and abuse controls

Small practical rule:

- if stealing or modifying the app would break your trust model, the trust model is too client-heavy

---

## 8. Practical Checklist

- Is the mobile app treated as a public client?
- Are long-lived secrets absent from the app package?
- Are tokens stored and rotated safely?
- Are sensitive actions enforced server-side?
- Are deep links, WebViews, and SDKs reviewed as attack surface?
- Is sensitive data excluded from logs, notifications, screenshots, and backups where needed?
- Are attestation or device-integrity signals used only as extra signals, not sole trust anchors?

---

## 9. Practical Summary

Practical summary:

> Mobile AppSec is mostly about respecting the public-client model. The app runs on an untrusted device, so I keep secrets out of the package, use safe login and token patterns, store sensitive material with platform protections, and keep the real authorization and workflow enforcement on the server.

---

## 10. Further Reading

- OWASP Mobile Application Security project: https://mas.owasp.org/
- OWASP MASVS: https://mas.owasp.org/MASVS/
- OWASP MASTG: https://mas.owasp.org/MASTG/
- Android security best practices: https://developer.android.com/privacy-and-security/security-best-practices
- Android security checklist: https://developer.android.com/privacy-and-security/security-tips
- Apple Platform Security: https://support.apple.com/en-tm/guide/security/welcome/web
- Apple app security overview: https://support.apple.com/guide/security/seccd5016d31/web
