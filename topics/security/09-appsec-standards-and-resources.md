# AppSec Reference Map

Use a reference for the question it answers. A risk list helps identify what can go wrong; it does not by itself specify implementation or prove that a system is secure.

| Need | Reference | Use it for |
|---|---|---|
 Recognize common web risks | [OWASP Top 10](https://owasp.org/www-project-top-ten/) | Awareness and shared vocabulary. |
 Recognize API-specific risks | [OWASP API Security Top 10](https://owasp.org/API-Security/) | Object access, resource use, business flows, and API inventory. |
 Implement one control | [OWASP Cheat Sheets](https://cheatsheetseries.owasp.org/) | Practical guidance on sessions, authorization, uploads, SSRF, and more. |
 Define verifiable web/API requirements | [OWASP ASVS](https://owasp.org/www-project-application-security-verification-standard/) | Requirements for design, review, and testing. |
 Define and test mobile requirements | [OWASP MASVS](https://mas.owasp.org/MASVS/) and [MASTG](https://mas.owasp.org/MASTG/) | Mobile controls and how to verify them. |
 Improve a team's security program | [OWASP SAMM](https://owasp.org/www-project-samm/) | Maturity and delivery practice. |

Start from a concrete feature: identify its threats, select requirements, use a focused cheat sheet for implementation, and test the result. For hands-on vulnerability practice, use [PortSwigger Web Security Academy](https://portswigger.net/web-security) or [OWASP Juice Shop](https://owasp.org/www-project-juice-shop/), then write down the design change that would have prevented the issue.
