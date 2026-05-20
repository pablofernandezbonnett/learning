# AppSec Standards and Learning Resources

AppSec learning gets confusing because many resources answer different
questions.

People often mix up:

- awareness lists
- secure coding guidance
- verification requirements
- mobile standards
- program maturity models
- practice labs

That confusion makes study paths feel random.

---

## 1. Why This Matters

If your goal is to be useful in AppSec inside a product team, you need to know
which resource solves which problem.

Otherwise you end up with bad study habits such as:

- reading `OWASP Top 10` as if it were a build checklist
- using pentesting labs as if they were a secure SDLC plan
- treating `ASVS` and `SAMM` like the same thing
- ignoring mobile-specific standards

---

## 2. Smallest Useful Mental Model

Use this map:

- awareness list: what commonly goes wrong
- cheat sheet: how to implement one area more safely
- verification standard: what requirements should exist
- testing guide: how to verify those requirements
- maturity model: how a team or org improves its software security program
- lab platform: where you practice finding and understanding failures

---

## 3. The Main OWASP Stack

### OWASP Top 10

Use for:

- awareness
- shared vocabulary
- explaining common web application risks

Do not use it as:

- a complete engineering security standard
- an API-specific checklist
- a mature secure SDLC plan

### OWASP API Security Top 10

Use for:

- API-specific awareness
- design and review questions around object authz, resource consumption,
  business flows, inventory, and unsafe API consumption

This is a better fit than generic web-only material when your product is
API-heavy or mobile-backed.

### OWASP Cheat Sheet Series

Use for:

- practical implementation guidance
- point solutions while designing or reviewing one area

Examples:

- authorization
- REST security
- SSRF prevention
- file uploads
- secrets management

### OWASP ASVS

Use for:

- turning vague AppSec advice into explicit web and API security requirements
- code review and design review questions
- defining what "good enough" should mean for a feature or service

Short rule:

> `ASVS` is closer to a requirements and verification reference than to a beginner awareness list.

### OWASP MASVS

Use for:

- mobile application security requirements
- deciding what a secure mobile app should protect around storage, auth,
  network, platform interaction, code quality, resilience, and privacy

Short rule:

> `MASVS` is the mobile equivalent of a verification standard, not just a list of mobile bugs.

### OWASP MASTG

Use for:

- learning how mobile security controls are tested and verified
- practical mobile security study after you understand the requirements model

Short rule:

> `MASTG` explains how to verify mobile security controls; `MASVS` explains what controls you should care about.

### OWASP SAMM

Use for:

- team and organization maturity
- secure SDLC planning
- deciding how to improve software security practices over time

Short rule:

> `SAMM` is about building a software security program, not about teaching one vulnerability class.

---

## 4. Best First Stack for Your Goal

If your goal is:

> I can do useful AppSec inside a product team and improve prevention.

Then the strongest free-first stack is:

1. repo notes for mental model
2. `OWASP Top 10` plus `API Security Top 10` for awareness
3. `ASVS` for requirements
4. `MASVS` plus `MASTG` for mobile
5. `Cheat Sheets` for implementation detail
6. `SAMM` for secure SDLC and team maturity
7. labs such as PortSwigger, crAPI, Juice Shop, and WebGoat for practice

That sequence is stronger than:

- pentesting breadth first
- random bug bounty content first
- buying expensive training before you know your exact gap

---

## 5. Recommended Free Resources

### Awareness and Standards

- OWASP Top 10: https://owasp.org/www-project-top-ten/
- OWASP API Security Top 10: https://owasp.org/API-Security/
- OWASP ASVS: https://owasp.org/www-project-application-security-verification-standard/
- OWASP SAMM: https://owasp.org/www-project-samm/
- OWASP Cheat Sheet Series: https://cheatsheetseries.owasp.org/
- OWASP Mobile Application Security: https://mas.owasp.org/
- OWASP MASVS: https://mas.owasp.org/MASVS/
- OWASP MASTG: https://mas.owasp.org/MASTG/

### Practice

- PortSwigger Web Security Academy: https://portswigger.net/web-security
- OWASP crAPI: https://owasp.org/www-project-crapi/
- OWASP Juice Shop: https://owasp.org/www-project-juice-shop/
- OWASP WebGoat: https://owasp.org/www-project-webgoat/

### Platform-Specific Mobile Docs

- Android security best practices: https://developer.android.com/privacy-and-security/security-best-practices
- Android security checklist: https://developer.android.com/privacy-and-security/security-tips
- Apple Platform Security: https://support.apple.com/en-tm/guide/security/welcome/web

---

## 6. Optional Paid Resources That Actually Fit AppSec

If you pay, prefer resources that train secure development and AppSec judgment,
not only exploitation skills.

Best fit:

- SecureFlag: https://www.secureflag.com/training
- Security Journey: https://www.securityjourney.com/appsec-training-library
- SANS SEC522: https://www.sans.org/cyber-security-courses/application-security-securing-web-apps-api-microservices

How I categorize them:

- SecureFlag: best when you want developer-focused secure coding labs, including team rollout potential
- Security Journey: best when you want a role-based AppSec learning program for developers and broader SDLC roles
- SANS SEC522: best when employer budget exists and you want a high-quality AppSec course for web apps, APIs, and microservices

Lower fit for your current goal:

- general pentesting courses whose center of gravity is host, network, or offensive breadth rather than secure product engineering

---

## 7. How To Use These Resources Without Wasting Time

Recommended habit:

1. study one concept in this repo
2. map it to one external standard or cheat sheet
3. solve one small lab
4. write 3 to 5 lines on how you would prevent it in a real product system

Examples:

- broken object authz -> `10-access-control-and-idor.md` -> API Top 10 -> Authorization Cheat Sheet -> crAPI lab
- mobile token storage -> `08-mobile-appsec-basics.md` -> MASVS -> Android/iOS platform docs
- webhook abuse -> `03-webhooks-basics.md` -> API Top 10 -> threat model -> Spring lab

---

## 8. Practical Summary

Good short answer:

> I use OWASP Top 10 and API Top 10 for awareness, Cheat Sheets for implementation guidance, ASVS for web and API requirements, MASVS and MASTG for mobile, and SAMM for secure SDLC maturity. Then I practice on real labs so the standards turn into engineering judgment instead of vocabulary.
