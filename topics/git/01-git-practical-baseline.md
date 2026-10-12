# Practical Git Guide

Git records snapshots as commits. A branch is a movable name pointing to one commit; that commit links to its parents. This model explains why a merge joins histories and a rebase creates new commits on a different base.

## Know which state you are changing

| State | Meaning | Inspect with |
| --- | --- | --- |
| Working tree | Files currently on disk | `git status`, `git diff` |
| Staging area | Changes selected for the next commit | `git diff --staged` |
| Local branch | Your current commit pointer and its history | `git log --oneline --graph` |
| Remote-tracking branch | Your last fetched view of a remote branch | `git fetch`, then inspect `origin/main` |

`git fetch` updates the remote view; it does not merge or rebase your current branch. `git push` asks the remote to update a branch reference to include your commits.

## Make a reviewable change

1. Create a branch from the agreed base.
2. Check `git status` before and after editing.
3. Stage only the intended changes; inspect `git diff --staged`.
4. Commit one coherent intention with its relevant validation.
5. Review the branch diff before sharing it.

```bash
git switch -c feature/order-idempotency
git status
git add path/to/changed-file
git diff --staged
git commit -m "Reject duplicate checkout requests"
```

A useful commit lets a reviewer state what changed and why. Do not mix unrelated formatting or cleanup with a behavioral fix just to reduce the number of commits.

## Update your branch

First fetch the latest remote state. Then choose how to incorporate it:

| Situation | Typical choice | Why |
| --- | --- | --- |
| Your own unpublished feature commits | `git rebase origin/main` | Replays them on the new base and keeps a linear branch history |
| A branch others have based work on | `git merge origin/main` | Preserves existing commit IDs and shared history |
| Team has an explicit integration policy | Follow that policy | Review and automation may depend on it |

```bash
git fetch origin
git rebase origin/main
```

Rebase creates new commit IDs. It does not prevent conflicts: if two changes overlap, you still need to decide the final content. Avoid rewriting a published branch that collaborators use unless you coordinate it.

## Resolve a conflict

Read the two intentions, edit the file to its desired final state, and run the nearest useful validation. Then stage the resolution and continue the operation in progress.

```bash
git status
git diff
git add path/to/resolved-file
git rebase --continue
```

Use `git merge --continue` during a merge. If the chosen operation is no longer appropriate, `git rebase --abort` or `git merge --abort` returns to its pre-operation state. Check `git status` before choosing a command; do not run both continue commands.

## Clean only your own history

Use `git rebase -i origin/main` when several local commits obscure one change. `reword` changes a message; `squash` combines a commit with the previous one; `drop` removes it. Inspect the resulting diff and rerun relevant checks because the rewritten history is new.

If that branch already exists remotely and you intentionally rewrote it, `git push --force-with-lease` checks that the remote reference still matches your local expectation before updating it. It still rewrites the remote branch; coordinate when others may depend on it.

## Recover before discarding

`git reflog` shows recent positions of `HEAD` and branch references, including positions before many resets and rebases. Inspect the commit you need before moving any pointer. Create a recovery branch if you want to preserve both the current state and the old commit:

```bash
git reflog
git branch recovery/my-work abc1234
```

Avoid using `git reset --hard` as a generic recovery step: it can discard uncommitted work in the working tree and staging area. If you are unsure what changed, inspect `git status`, `git diff`, and the relevant reflog entry first.

## Quick decision check

- Are my working-tree and staged changes safe before I change history?
- Is this branch private or already shared?
- Do I need to preserve existing commit IDs?
- Have I checked the final diff and relevant tests after resolving conflicts?
- If recovery is needed, can I point to the exact commit I want to keep?

Official references: [rebase](https://git-scm.com/docs/git-rebase), [merge](https://git-scm.com/docs/git-merge), [push and force-with-lease](https://git-scm.com/docs/git-push), [reflog](https://git-scm.com/docs/git-reflog).
