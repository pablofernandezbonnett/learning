## PR Title

`docs(learning): strengthen learning paths, entrypoints, and decision-oriented docs`

## PR Type

- [ ] Feature
- [ ] Bug fix
- [ ] Refactor
- [x] Docs
- [ ] Test
- [ ] Chore
- [ ] Breaking change

## Summary

This PR strengthens the repo as a learning surface rather than only a note dump.
It improves entrypoint clarity, fixes stale internal navigation, adds stronger
decision-oriented synthesis in architecture and system design, and aligns a much
broader part of the repo with the canonical learning-doc style.

## Problem / Context

The repo had three main issues:

- some internal links and top-level framing had drifted, which made navigation and canon less reliable
- newer SRE and Solutions Architecture material was good, but still inconsistent in how explicitly it taught mental models, weak-vs-better reasoning, and strong defaults
- several topic entrypoints and older notes still used correct terminology without always giving the smallest useful explanation early enough
- pattern-heavy backend topics were covered, but some of the "when to choose what" reasoning was still too distributed across multiple docs

## Changes Made

- repaired stale internal Markdown links and reconciled top-level migration framing
- improved entrypoints across `README.md`, `paths/`, and topic `README.md` files so readers can tell where to start and why
- deepened `topics/sre/` and `topics/solutions-architecture/` with more explicit `why this matters`, `smallest mental model`, `bad vs better`, and strong-default teaching patterns
- added new decision-oriented synthesis docs for distributed workflow and integration choice in `architecture`, `api`, and `system-design`
- aligned older decision-heavy docs across `system-design`, `architecture`, `api`, `security`, `cloud`, `devops`, `databases`, and `spring-boot` with the same teaching pattern
- tightened neutral materials such as topic `README.md` files, cheatsheets, and fast-review docs so they reopen concepts faster and explain why they matter before diving into details
- added a repo-local PR template and a draft PR body suited to documentation-heavy review

## Areas Changed

- `README.md`, `paths/`, and topic entrypoints:
  improved first-contact clarity, role-path guidance, and jargon expansion
- `topics/sre/`:
  strengthened operational reasoning with clearer mental models, contrasts, and tradeoffs
- `topics/solutions-architecture/`:
  strengthened design-judgment teaching with clearer constraints, migration realism, and decision framing
- `topics/architecture/`, `topics/api/`, and `topics/system-design/`:
  added stronger pattern-choice and case-study guidance for interview-ready reasoning
- older reference and framework materials:
  improved quick-reopen usefulness with clearer problem framing, smaller mental models, and short reusable takeaways
- `.github/PULL_REQUEST_TEMPLATE.md` and `docs/pr-drafts/learning-docs-review.md`:
  added review scaffolding for this repo

## How to Review

1. Start with `README.md`, `paths/general-refresh.md`, `paths/sre-from-backend-engineers.md`, and `paths/solutions-architect-from-backend.md`.
2. Review `topics/sre/` and `topics/solutions-architecture/` as the main role-path and teaching-style upgrade in this PR.
3. Review `topics/architecture/16-distributed-workflow-pattern-choice.md`, `topics/api/07-sync-vs-async-integration-choice.md`, and `topics/system-design/distributed-workflow-case-studies.md` as the main decision-synthesis additions.
4. Spot check older docs such as `topics/architecture/03-distributed-transactions-and-events.md`, `topics/security/02-web-and-api-security.md`, `topics/devops/02-zero-downtime-deployments.md`, and `topics/spring-boot/13-spring-data.md` to confirm the teaching pattern is now more consistent.
5. Check that first-use jargon is explained and that decision-heavy docs expose weak-vs-strong reasoning clearly without drifting into textbook style.

## Validation Evidence

- local Markdown link validation across `README.md`, `paths/`, `topics/`, and `docs/`: `OK`
- no runnable code changes

## Risks / Trade-offs

- some docs are slightly longer because the teaching pattern is more explicit
- there is intentional repetition in a few quick-reopen docs so they work better as standalone refresh material
- this PR deliberately optimizes for learning clarity over maximal brevity

## Backward Compatibility

- [x] No breaking changes
- [ ] Breaking changes (described below)

## Deployment / Rollout Notes

- no deployment impact
- no config or runtime changes

## Checklist

- [x] Scope is focused and aligned with the repo's documentation direction
- [x] Documentation keeps the teaching style more consistent
- [x] Jargon is explained where first used in entrypoints and decision docs
- [x] Tradeoffs or failure modes are visible where they matter
- [x] Quick-reopen docs and cheatsheets still stay compact enough to scan
- [x] Local verification completed
