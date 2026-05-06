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
It improves entrypoint clarity, fixes stale internal navigation, deepens SRE and
Solutions Architecture materials with stronger mental models, and aligns more of
the repo with the canonical learning-doc style.

## Problem / Context

The repo had three main issues:

- some internal links and top-level framing had drifted, which made navigation and canon less reliable
- newer SRE and Solutions Architecture material was good, but still inconsistent in how explicitly it taught mental models, weak-vs-better reasoning, and strong defaults
- several topic entrypoints still used correct terminology without always giving the smallest useful explanation early enough

## Changes Made

- repaired stale internal Markdown links and reconciled top-level migration framing
- improved entrypoints across `README.md`, `paths/`, and topic `README.md` files so readers can tell where to start and why
- deepened `topics/sre/` and `topics/solutions-architecture/` with more explicit `why this matters`, `smallest mental model`, `bad vs better`, and strong-default teaching patterns
- aligned additional topic entrypoints such as `java`, `kotlin`, `algorithms`, `system-design`, `api`, `architecture`, and `dart`
- added a repo-local PR template and a draft PR body suited to documentation-heavy review

## Areas Changed

- `README.md`, `paths/`, and topic entrypoints:
  improved first-contact clarity, role-path guidance, and jargon expansion
- `topics/sre/`:
  strengthened operational reasoning with clearer mental models, contrasts, and tradeoffs
- `topics/solutions-architecture/`:
  strengthened design-judgment teaching with clearer constraints, migration realism, and decision framing
- `.github/PULL_REQUEST_TEMPLATE.md` and `docs/pr-drafts/learning-docs-review.md`:
  added review scaffolding for this repo

## How to Review

1. Start with `README.md`, `paths/general-refresh.md`, `paths/sre-from-backend-engineers.md`, and `paths/solutions-architect-from-backend.md`.
2. Review `topics/sre/` and `topics/solutions-architecture/` as the main teaching-style upgrade in this PR.
3. Spot check entrypoints such as `topics/system-design/README.md`, `topics/api/README.md`, `topics/architecture/README.md`, and `topics/security/README.md` to confirm consistency.
4. Check that first-use jargon is explained and that decision-heavy docs expose weak-vs-strong reasoning clearly.

## Validation Evidence

- local Markdown link validation across `README.md`, `paths/`, `topics/`, and `docs/`: `OK`
- no runnable code changes

## Risks / Trade-offs

- some docs are slightly longer because the teaching pattern is more explicit
- a few sections may still merit taste-level trimming after human review
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
- [x] Local verification completed
