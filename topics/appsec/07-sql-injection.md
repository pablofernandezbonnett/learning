# SQL Injection

SQL injection is one of the classic AppSec topics because it teaches a broader lesson:
untrusted input must not be allowed to change the structure of a command.

---

## 1. What This Topic Is

SQL injection happens when user-controlled input is inserted into a database query in an unsafe way, allowing the attacker to change the meaning of the query.

Short rule:

> input should become data inside the query, not part of the query language itself

---

## 2. Why It Matters

The impact can include:

- reading other users' data
- bypassing login logic
- changing or deleting records
- in some cases reaching deeper system compromise

This still appears in real software, especially in rushed code, legacy code, or custom query builders.

---

## 3. What You Should Understand

- string concatenation versus parameterization
- prepared statements
- login bypass examples
- blind SQL injection at a high level
- why input validation helps but does not replace parameterization

---

## 4. How It Breaks In Real Apps

Common failures:

- building SQL with string concatenation
- using "trusted" stored data later in a dangerous query
- unsafe dynamic filters or sort fields
- overconfidence in blacklist filtering

---

## 5. How To Build It Better

- use prepared statements or parameterized queries
- constrain dynamic query parts carefully
- keep database permissions narrower than the app's full imagined power
- test suspicious query-building code explicitly

---

## 6. What To Look For In Code

- raw SQL with string interpolation
- custom repository methods
- dynamic `WHERE`, `ORDER BY`, or search builders
- error handling that leaks query details
- database accounts with excessive privileges

---

## 7. Practical Exercise

Find one query in code and answer:

- where does user input enter?
- is it bound as a parameter or concatenated?
- if an attacker controls that field, can they change query structure?
- how would you rewrite it safely?

---

## 8. Resources

- base and practice: [PortSwigger SQL Injection](https://portswigger.net/web-security/sql-injection)
- guided labs: [PortSwigger SQL Injection Learning Path](https://portswigger.net/web-security/learning-paths/sql-injection)
- defense: [OWASP SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)
