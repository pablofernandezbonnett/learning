# Contributing

This repository is public, but it is kept read-only by default.

## Default Path

If you want to reuse the material for your own study:

- fork the repository
- customize the structure, notes, and examples around your own interests

That is the expected path for most people.

## Direct Contributions

If you want to contribute back to this repository itself:

- contact me first
- if it makes sense, I can add you as a contributor

The goal is to keep the repository curated and consistent instead of turning it into an open-ended shared notebook.

## Commercial Use

Commercial use is not allowed.

See `LICENSE` for the repository licensing split between documentation and code.

## Practical Note

This file describes the intended usage and contribution policy.

## Documentation Verification

Before proposing documentation changes, run:

```bash
node scripts/verify-markdown.mjs
```

It checks repository-local Markdown links and heading anchors. It deliberately
does not request external URLs, because external documentation can be
temporarily unavailable even when the repository link is correct.
