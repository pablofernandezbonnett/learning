# Designing an Embedded Rule

An embedded script is a small decision function inside a larger application. Its value is independent rule publication; its cost is a new trust boundary and weaker compile-time checking. Choose it only when that tradeoff is useful.

## Decide Whether Scripting Fits

A promotion or eligibility rule may change often while its inputs and output stay stable. A script can evaluate it without redeploying the whole application. Authentication, transaction coordination, arbitrary integrations, and final writes should stay in application code because they need stronger control over permissions and side effects.

Before choosing Lua, ask:

- How often does this rule change, and who approves it?
- Can it be expressed as a small, deterministic input-to-output decision?
- Would configuration or ordinary code be simpler?
- What happens when the script is unavailable, slow, or returns an invalid result?

## Host Contract

```text
host validates request and chooses approved rule version
  → host builds a small immutable input
  → isolated runtime evaluates the rule within resource limits
  → host validates output and applies the final business invariant
  → host records version, outcome, latency, and any failure
```

For a pricing rule, pass only the needed product, customer tier, quantity, and effective date. Return a discount decision with a reason. The host computes or verifies the final price and owns the order transaction. Supplying the effective date also makes the rule reproducible in tests.

## Contain and Operate the Rule

Treat scripts as untrusted code, including scripts uploaded by an internal user. Expose only the functions and data they need; do not grant file, network, process, or host-runtime access. Use a fresh or proven-isolated evaluation environment so one request cannot leave state for another.

Restrict script size and execution time. Verify how the chosen runtime bounds CPU and memory; a restricted global table alone does not guarantee either. If hard resource isolation matters, use a separate process with enforced limits. Test prohibited capabilities and infinite loops against the actual host integration, not a simulation run in a normal Lua CLI.

Store immutable versions. Separate drafting, approval, and activation; let the runtime read only approved versions. Record which version produced each decision and keep a tested rollback or disable path. Review the effect of a new version on error rate, latency, and business output before wider activation.

## Exercise: Promotion Decision

Design a rule that receives a validated base price, customer tier, quantity, and effective date. A gold customer buying at least three items gets a 10% discount. The host caps any returned discount at 20%.

```text
evaluate(ctx):
  if tier is gold and quantity is at least three:
    return discount 10%, reason "gold bulk"
  return discount 0%, reason "none"

host:
  select approved version
  validate inputs → evaluate within limits → validate result
  clamp or reject a discount outside the contract
  calculate final price and commit the transaction
```

Check the threshold at two and three items, an unknown tier, invalid input, a script that returns 90%, a script that loops, and activation followed by rollback. For a money rule, decide rounding and integer units in the host contract rather than leaving them to each script.

See [Lua essentials](./01-language-basics.md) for the syntax needed to implement the decision.
