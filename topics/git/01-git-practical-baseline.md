# Git Practical Baseline for Backend Engineers

You do not need every Git feature in active memory.

You do need a mental model strong enough to answer:

- what state is local vs remote
- what happens when you commit
- what a branch pointer really is
- when to merge
- when to rebase
- how to recover without panic

---

## 1. Why This Matters

Weak Git understanding does not usually fail during the happy path.
It fails when:

- your branch is behind `main`
- a pull request became a pile of noisy commits
- you rewrote history that somebody else already pulled
- you need to recover from a bad reset, bad rebase, or wrong checkout

The job is not "know many commands".
The job is "match the command to the collaboration boundary".

---

## 2. Smallest Useful Mental Model

Keep these pieces separate:

- `working tree`: files on disk that you are editing now
- `staging area` / `index`: the next snapshot you intend to commit
- `commit`: an immutable snapshot plus metadata and parent links
- `branch`: a movable pointer to a commit
- `remote`: another repository, usually `origin`

Plain-English version:

> Git is mostly snapshots plus pointers. A branch is not a folder of changes. It
> is a name pointing at one commit, and that commit points backward through its
> parents.

That one idea makes `merge`, `rebase`, `reset`, and `reflog` much easier to
reason about.

---

## 3. Bad Mental Model vs Better Mental Model

Bad mental model:

> My branch contains files, and Git somehow mixes them together.

Better mental model:

> My branch points to a line of commits. `merge` joins histories. `rebase`
> replays my commits onto a different base.

Bad mental model:

> A conflict means I used the wrong command.

Better mental model:

> A conflict means two histories changed the same area differently. The command
> changes when and how I resolve it, not whether the underlying change overlaps.

Bad mental model:

> `push` sends my files.

Better mental model:

> `push` updates a remote branch reference so it points at my local commit
> history.

---

## 4. The Daily Workflow That Usually Scales Well

Strong default:

1. create a short-lived feature branch
2. make small commits that each preserve a sensible state
3. sync with `main` before the branch drifts too far
4. clean noisy local history before review if needed
5. do not rewrite history on a shared branch unless the team deliberately allows it

Minimal command loop:

```bash
git switch -c feature/order-idempotency
git status
git add path/to/files
git commit -m "Add idempotency key validation to checkout"
git fetch origin
git rebase origin/main
git push -u origin feature/order-idempotency
```

Why this works:

- `fetch` updates your view of the remote without changing your branch
- `rebase origin/main` makes the history easy to read before review
- small commits reduce the blast radius of conflicts and review comments

---

## 5. What Good Commits Look Like

A good commit is not "small" in the abstract.
It is one that a reviewer can explain in one sentence.

Good commit shape:

- one intention
- clear message
- tests or validation close to the change
- no unrelated formatting or drive-by edits mixed in

Weak commit messages:

- `fix stuff`
- `changes`
- `wip`

Stronger commit messages:

- `Reject duplicate checkout requests by idempotency key`
- `Split payment authorization from order persistence`
- `Use List.copyOf for immutable API response snapshot`

Short rule:

> Optimize commits for review and rollback, not only for local convenience.

---

## 6. Updating Your Branch Without Making A Mess

The safest learning-friendly update flow is:

```bash
git fetch origin
git rebase origin/main
```

Why this is a good default:

- network access and history rewriting stay separate
- you can inspect `origin/main` before changing your branch
- the command makes the intent explicit

If your team prefers merge-on-branch:

```bash
git fetch origin
git merge origin/main
```

That is also valid.
The important question is not style purity.
It is whether the branch is still local-only or already shared widely.

---

## 7. Merge vs Rebase In One Page

`merge`:

- combines histories
- usually preserves the real branch structure
- may create a merge commit
- is safer for shared branches because it does not rewrite existing commit IDs

`rebase`:

- replays your commits onto a new base
- gives a more linear history
- rewrites commit IDs
- is best treated as a local cleanup and branch-refresh tool

Strong default:

> Use `rebase` while shaping your own branch. Use `merge` when preserving shared
> published history is the safer collaboration move.

---

## 8. What To Keep Ready During Conflict Resolution

When a rebase or merge stops on a conflict:

1. read the conflict in terms of intent, not only markers
2. decide what the final code should mean
3. run tests or at least the nearest validation
4. continue the operation

Useful commands:

```bash
git status
git diff
git add path/to/resolved-file
git rebase --continue
git merge --continue
git rebase --abort
git merge --abort
```

Important reminder:

- `rebase` may make you resolve related conflicts commit by commit
- `merge` usually makes you resolve them in one merge step

Neither command makes overlapping edits disappear.

---

## 9. Recovery Tools Worth Remembering

The first recovery tool to remember is not force push.
It is `reflog`.

Why:

- it shows where your branch tip and `HEAD` used to point
- it often lets you recover from a bad `reset`, bad `rebase`, or wrong checkout

Useful commands:

```bash
git reflog
git reset --hard HEAD@{1}
git switch -
```

Use `reset --hard` only when you truly want to throw away current uncommitted
state.
The important point here is not the exact recovery recipe.
It is knowing that your lost-looking commit often still exists in the reflog.

---

## 10. Practical Rules For Shared Repos

- do not work directly on `main`
- keep branches short-lived when possible
- avoid giant "final cleanup" commits that hide many unrelated decisions
- prefer reviewable commit scope over perfect local diary history
- do not rebase a branch that other engineers are actively basing work on unless the team explicitly coordinates it
- treat force push as a deliberate branch-maintenance step, not as a normal reflex

What usually goes wrong:

- branch lives too long and conflicts become expensive
- unrelated changes get mixed into one commit
- someone rewrites a shared branch without warning
- engineer panics after a history edit and forgets `reflog` exists

---

## 11. Short Takeaways

- a branch is a pointer, not a container
- `fetch` updates your remote view without touching your branch
- `merge` joins histories; `rebase` replays commits onto a new base
- conflicts come from overlapping changes, not from moral failure
- local cleanup is good; rewriting shared history casually is not
- `reflog` is the recovery tool worth remembering

## 12. Further Reading

- Git `rebase` documentation: https://git-scm.com/docs/git-rebase
- Git `merge` documentation: https://git-scm.com/docs/git-merge
- Git `pull` documentation: https://git-scm.com/docs/git-pull
- Git `reflog` documentation: https://git-scm.com/docs/git-reflog
