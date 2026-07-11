# Git Refresh for Backend Engineers

Use this folder for the Git judgment that matters in day-to-day engineering
work.

The goal is not to memorize every command.
The goal is to keep a safe mental model for:

- local work vs shared history
- clean reviewable commits vs noisy history
- merge vs rebase
- conflict handling and recovery

Focus:

- what a branch really is
- how to update your branch safely
- when `rebase` helps and when it is the wrong tool
- how to keep commits reviewable without breaking collaborators
- how to recover when history editing goes wrong

Why this topic exists:

- many engineers can "use Git" but still carry weak rules such as "rebase is always better" or "merge conflicts mean I used the wrong command"
- the real value is not command recall alone
- it is knowing which history operation matches the collaboration boundary

## Recommended Order

1. [01-git-practical-baseline.md](./01-git-practical-baseline.md): the smallest useful mental model for branches, commits, staging, review, sync, and recovery
2. [02-rebase-and-history-shaping-cheatsheet.md](./02-rebase-and-history-shaping-cheatsheet.md): when to use `rebase`, when not to, how it differs from `merge`, and how to recover safely

## Refresh

- [01-git-practical-baseline.md](./01-git-practical-baseline.md)
- [02-rebase-and-history-shaping-cheatsheet.md](./02-rebase-and-history-shaping-cheatsheet.md)

## Required

- [01-git-practical-baseline.md](./01-git-practical-baseline.md)
- [02-rebase-and-history-shaping-cheatsheet.md](./02-rebase-and-history-shaping-cheatsheet.md)

## Growth

- keep the official Git docs nearby once the baseline is warm
- practice `reflog` recovery before you need it under stress

## Core Rule

- use `rebase` to improve your own local branch history
- use `merge` when preserving shared history is the safer collaboration move
- treat history rewriting as a local cleanup tool, not as a default move on branches other people already use
