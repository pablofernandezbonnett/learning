# Rebase and History Shaping Cheat Sheet

Use this note when you remember the word `rebase` but want the practical
decision boundary back quickly.

Shortest rule:

- `rebase` is for replaying your commits onto a new base
- it does not remove the possibility of conflicts
- it is strongest as a local branch cleanup tool

---

## 1. What `git rebase` Actually Does

Smallest useful mental model:

- Git finds the commits that are on your branch but not on the chosen upstream
- Git moves your branch to the new base
- Git reapplies your commits one by one on top of that base

Why this matters:

- your commit IDs change
- conflicts may appear during replay
- the resulting history often looks linear and cleaner

Plain-English version:

> `rebase` does not "merge more nicely." It rebuilds your branch as if your work
> had started from a newer commit.

---

## 2. `merge` vs `rebase`

`merge`:

- combines two histories
- often creates a merge commit
- preserves existing commit IDs
- safer default when the branch history is already shared

`rebase`:

- rewrites your branch commits on top of a new base
- usually keeps the graph straighter
- changes commit IDs
- best for local branch refresh and cleanup

Bad simplification:

> `rebase` is better than `merge`.

Better rule:

> `rebase` is cleaner for local branch shaping. `merge` is safer when preserving
> published history matters more than a perfectly straight graph.

---

## 3. Does Rebase Avoid Conflicts?

No.

What changes is:

- conflicts may be resolved commit by commit during replay
- you often resolve them before opening or updating the final pull request
- the final branch history can stay cleaner

What does not change:

- if `main` and your branch changed the same code differently, Git still needs
  a human decision

Strong sentence worth remembering:

> `rebase` changes conflict timing and conflict shape, not the fact that
> overlapping edits need reconciliation.

---

## 4. When To Use Rebase

Good uses:

- your local feature branch drifted behind `main`
- you want to clean a noisy commit sequence before review
- you want to squash "checkpoint" commits into a more reviewable shape
- you want to drop or reorder local commits before publishing

Typical local refresh flow:

```bash
git fetch origin
git rebase origin/main
```

Typical local cleanup flow:

```bash
git rebase -i origin/main
```

Good interactive actions to remember:

- `pick`: keep the commit
- `reword`: keep it but change the message
- `squash`: combine it with the previous commit and edit the final message
- `fixup`: combine it with the previous commit and discard this message
- `drop`: remove the commit from the rewritten history

---

## 5. When Not To Use Rebase Casually

Avoid casual rebase when:

- the branch is already shared and other engineers may have based work on it
- the team treats that branch as a stable integration branch
- you do not understand whether you will need a force push afterward

The real risk is not that `rebase` is evil.
The risk is that rewritten commit IDs make other collaborators reconcile a
history they already pulled.

Short rule:

> If other engineers are already building on top of that exact branch history,
> prefer preserving it unless the rewrite is coordinated.

---

## 6. Safe Practical Flows

### Refresh your branch from `main`

```bash
git fetch origin
git rebase origin/main
```

### Clean your local commits before opening a pull request

```bash
git rebase -i origin/main
```

### Abort when the rewrite is getting messy

```bash
git rebase --abort
```

### Continue after resolving conflicts

```bash
git add path/to/resolved-file
git rebase --continue
```

### Recover after a bad rewrite

```bash
git reflog
```

Then reset or branch from the commit you want to recover.

---

## 7. Force Push: What It Really Means

After rebasing a branch that already exists on the remote, you usually need a
force-style push because your local branch no longer has the same commit IDs as
the remote branch.

Better default:

```bash
git push --force-with-lease
```

Why this is better than plain `--force`:

- it refuses to overwrite the remote branch if it changed in ways you do not
  have locally

This is still a destructive-looking operation.
Use it deliberately and mainly on your own feature branch.

---

## 8. Good Team Rules

- keep feature branches short-lived
- rebase your own branch when it improves reviewability
- prefer `--force-with-lease` over plain `--force`
- do not rewrite long-lived shared branches casually
- if the team wants merge commits on integration, respect that convention
- do not turn Git history style into ideology; optimize for safe collaboration and clear review

---

## 9. Short Answers You Can Reuse

What is `rebase`?

> It replays my branch commits on top of a newer base, so the history becomes
> linear but the commit IDs change.

When should I use it?

> Mainly on my own feature branch to refresh from `main` or clean local history
> before review.

Does it avoid merge conflicts?

> No. It only changes when and how I resolve them.

When should I avoid it?

> When the branch history is already shared and other people depend on those
> exact commit IDs.

---

## 10. Further Reading

- Git `rebase` documentation: https://git-scm.com/docs/git-rebase
- Git `merge` documentation: https://git-scm.com/docs/git-merge
- Git `pull` documentation: https://git-scm.com/docs/git-pull
