# AppSec Mechanics

Use these notes to understand how specific web and API failures work. For system-level decisions and secure operation, use [Security](../security/README.md).

1. [HTTP and HTTPS](./01-http-and-https.md): request, response, and transport basics.
2. [Cookies and sessions](./02-cookies-and-sessions.md): browser session mechanics.
3. [Basic, bearer, and API keys](./03-basic-auth-bearer-and-api-keys.md): credential transport.
4. [Authentication and authorization](./04-authentication-vs-authorization.md): identity versus permission.
5. [JWT](./05-jwt.md): token format and validation failures.
6. [OAuth and OIDC](./06-oauth2-and-openid-connect.md): delegated access and login.
7. [SQL injection](./07-sql-injection.md): unsafe query construction.
8. [XSS](./08-cross-site-scripting-xss.md): untrusted content in browser output.
9. [CSRF](./09-csrf.md): cross-site requests with ambient credentials.
10. [Object access and IDOR](./10-access-control-and-idor.md): checking the exact object and action.

Read `01`–`06` for request and identity mechanics, then `07`–`10` for common failure modes. For each issue, trace one real request, mark the trust boundary, explain the failure, choose a prevention rule, and write a test that would catch it. Use the [standards map](../security/09-appsec-standards-and-resources.md) when you need a requirement or practice lab.
