# Git & GitHub Mini Project

## `git-github-practice` — Version Control and Collaboration Lab

Build understanding by creating and fixing small, intentional Git situations. The project consists of disposable Markdown/text files. Its purpose is to make repository state visible, not to build an application.

**Companion theory:** [Git & GitHub — Essential Concepts](Git_GitHub_Essential_Concepts.md)

| Study item | This exercise |
|---|---|
| Estimated time | 8–12 focused hours over several sessions; repeat difficult milestones |
| Tools | Windows PowerShell 5.1+ or PowerShell 7, VS Code, Git CLI, browser |
| Learning focus | index, commits, HEAD, branches, merge/conflict, recovery, remotes, review |
| Repository scope | one new disposable repository and, later, a second disposable clone |
| GitHub requirements | your own account and an empty repository; no collaborator or paid feature required |
| Application requirements | none; no Java, Maven, Spring, database, or Docker |
| Completion | 35 core tasks; advanced experiments are optional |

The Clean Architecture documents informed the teaching format. This lab has no dependency on their application, `book-catalog-api`, CapsuleAI, or any existing Java project. Create the practice directory separately from existing repositories. These instructions describe what **you will do**; the learning files themselves do not create a GitHub repository or publish anything.

**Validation note:** All 23 executable core walkthrough blocks were replayed using Git 2.45.1 for Windows and Windows PowerShell in isolated temporary directories. Checks confirmed the staged/unstaged distinction, expected conflict, recovery/replay results, fetch-only state, rejected/accepted pushes, final tracked paths, and annotated tag. Hosted merges/browser edits were simulated with a local bare remote. GitHub UI, authentication, Issue, and Release steps use official documentation and remain actions for you to perform on your own account.

## Quick navigation

1. [Learning outcome](#learning-outcome)
2. [Learning contract](#learning-contract)
3. [Repository model](#repository-model)
4. [Practice scope and command safety](#practice-scope-and-command-safety)
5. [Target practice files](#target-practice-files)
6. [Practice priorities](#practice-priorities)
7. [Progressive tasks: local foundations](#local-foundations)
8. [Branching, merging, and conflicts](#branching-merging-and-conflicts)
9. [Recovery exercises](#recovery-exercises)
10. [GitHub collaboration phase](#github-collaboration-phase)
11. [Optional advanced practice](#optional-advanced-practice)
12. [Pre-solution checkpoint](#pre-solution-checkpoint)
13. [Verification plan](#verification-plan)
14. [Complete Reference Walkthrough](#complete-reference-walkthrough)
15. [Troubleshooting](#troubleshooting)
16. [Completion checklist](#completion-checklist)
17. [Final mental model](#final-mental-model)

Pause points appear after tasks 6, 14, 22, 29, and 32. Attempt each before opening its answer. For a short session, finish a milestone cleanly and record the current branch and last commit outside the repository before stopping.

## Learning outcome

```text
Working Tree
     | git add
     v
Staging Area / Index
     | git commit
     v
Local Repository
     | git push
     v
GitHub Remote -> Pull Request -> Review / Merge
     |
     | git fetch, then deliberate integration (or pull)
     v
Updated Local Repository
```

You should finish able to explain which files, index entries, objects, and references each step affected. You will intentionally create an unstaged edit, a staged mistake, a bad commit, a merge conflict, a temporarily lost commit, a stale local branch, and a rejected push.

## Learning contract

```text
read Objective
 -> understand Concept
 -> predict repository state
 -> attempt Starter code/task
 -> use Hints only if needed
 -> run status / log / diff
 -> verify actual state
 -> review Common mistakes
 -> read Explanation
```

Before each state-changing Git command, predict what status or the graph will show afterward. Say which repository you are in before using the two clones. When confused, stop and run `git status`.

Starter blocks deliberately leave some choices as `# TODO` comments. They are prompts, not complete scripts. Do not paste a literal `<COMMIT_ID>`, `<URL>`, or `<ISSUE_NUMBER>` into PowerShell; substitute a real value. The reference walkthrough after the barrier supplies representative completed sequences and expected states.

Core tasks are sequential and are **not idempotent scripts**. Repeating a branch creation or commit without checking current state can create a different exercise. Git hashes and status wording vary; compare relationships and content, not illustrative IDs.

## Repository model

```text
original working directory                   GitHub
  working tree + index                         remote main
  main                       push/fetch        remote feature branches
  origin/main  <-----------------------------> repository objects

second working directory (task 30)
  its own working tree + index
  its own main and origin/main
  its own local config and reflog
```

Both clones can use your account. “Collaborator” means a second local copy simulating another machine; it does not require another person or account.

## Practice scope and command safety

Use a fresh `$env:USERPROFILE\git-learning-labs\git-github-practice` folder. Choose another empty location if that parent is itself inside a Git repository. Never run the lab in an existing project, and do not reuse a nonempty practice directory as if it were fresh. No task needs to delete or edit `.git` manually.

| Risk group | Examples | Required reasoning |
|---|---|---|
| Generally observational | `status`, `log`, `diff`, `show`, `branch -vv`, `remote -v`, `reflog` | read the evidence before changing state |
| Usually local and manageable | ordinary `add`, `commit`, `branch`, `switch` | know the current branch and selected paths; switch may carry edits or refuse |
| Requires understanding | `merge`, `rebase`, `reset`, `restore`, `revert`, stash application | identify destinations, possible conflicts, and data to preserve |
| Removes a reference or remote state | `branch -d`, stash drop, remote branch deletion | verify integration or another retained copy |
| Potentially destructive | hard reset, clean with deletion flags, unconditional force push | excluded from required practice; do not use as casual troubleshooting |

`git restore` can discard uncommitted content even though it does not rewrite history. The restore task uses an explicitly disposable edit. The reset task uses unpublished commits and a backup name. Use only fake configuration values; never copy real credentials into this repository.

PowerShell examples use `New-Item`, `Set-Content`, `Add-Content`, and `Set-Location`. Sample text uses `-Encoding ascii` to keep tiny exercise files predictable across Windows PowerShell and PowerShell 7. Real multilingual projects should use UTF-8 with a consistent editor configuration. CRLF/LF conversion warnings can reflect Git/editor settings; inspect the actual diff rather than changing global line-ending options blindly. Git's pager exits with `q`; VS Code's `code` command is optional if it is not on PATH.

## Target practice files

Initial files:

```text
git-github-practice/
  README.md
  notes/
    learning-log.md
  src/
    greeting.txt
```

Later tasks add ignore rules and small notes. Files such as `local.env` and `build/output.txt` contain fake/generated data and stay ignored. Branch-specific files temporarily appear and disappear when you switch. That is an intended checkout effect.

## Practice priorities

| Practice area | Priority | Core tasks |
|---|---|---|
| three areas, status/add/commit/diff/log | ⭐⭐⭐⭐⭐ MUST KNOW | 1–7 |
| HEAD, branches, merge, conflicts | ⭐⭐⭐⭐⭐ MUST KNOW | 8–14 |
| safe undo decisions, restore and revert | ⭐⭐⭐⭐⭐ MUST KNOW | 15–16 |
| reset, amend, stash, private rebase | ⭐⭐⭐⭐ IMPORTANT | 17–19, 22 |
| reflog and selective cherry-pick | ⭐⭐⭐ NICE TO KNOW | 20–21 |
| remotes, tracking, fetch/pull/push | ⭐⭐⭐⭐⭐ MUST KNOW | 23–25, 28–32 |
| GitHub PR/review workflow | ⭐⭐⭐⭐⭐ MUST KNOW | 26–27, 33 |
| Issue, tag, Release relationships | ⭐⭐⭐⭐ IMPORTANT | 33–35 |
| fork/rulesets/lease details | ⭐⭐⭐ NICE TO KNOW | optional |
| Actions and later CI/CD | ⭐⭐ FUTURE KNOWLEDGE | optional |

---

## Local foundations

### Task 1 — Verify tools and configure commit identity

**Objective:**

Run Git locally and choose the metadata for future commits.

**Concept:**

Git CLI is the `git` program you invoke from PowerShell. Commit identity is separate from GitHub authentication.

**What to implement:**

Check Git's version. If missing, install Git for Windows from [the official download](https://git-scm.com/downloads/win), then reopen PowerShell. Configure your real display name and chosen email globally if appropriate; retain existing correct values. Use GitHub's exact no-reply email from settings if desired.

**Starter code:**

```powershell
git --version
git config --global --get user.name
git config --global --get user.email
# TODO: configure missing/incorrect values using git config --global.
```

**Hints:**

The theory's [identity section](Git_GitHub_Essential_Concepts.md#10-identity-and-configuration) explains global/local scope. No repository or account login is needed yet.

**How to verify:**

Git reports a version supporting `init -b`, `switch`, and `restore`; name/email queries print your intended values. These commands have created no commits or hosted repository.

**Common mistakes:**

Using a PAT as an email/password setting, assuming name/email logs you in, or replacing an already correct identity just to match an example.

**Explanation:**

Identity becomes commit metadata when you record history. It grants no GitHub permissions.

### Task 2 — Create the independent directory and starter files

**Objective:**

Prepare disposable files outside existing projects.

**Concept:**

An ordinary directory can exist before Git history does.

**What to implement:**

Choose the fresh path below. If it already exists, stop and choose a fresh name/location rather than overwriting it. Confirm the parent is outside any existing Git repository. Create README, notes, and greeting with the exact sample text.

**Starter code:**

```powershell
$practiceRoot = Join-Path $env:USERPROFILE 'git-learning-labs'
$repoPath = Join-Path $practiceRoot 'git-github-practice'
if (Test-Path -LiteralPath $repoPath) { throw 'Choose a fresh practice location.' }
New-Item -ItemType Directory -Path $practiceRoot -Force | Out-Null
git -C "$practiceRoot" rev-parse --show-toplevel
# Expected above: not a git repository. If it prints a root, choose another parent.
# TODO: create repoPath, notes, and src; then write these initial contents:
# README.md: '# Git and GitHub Practice', blank line, 'Disposable version-control lab.'
# notes/learning-log.md: '# Learning log'
# src/greeting.txt: 'Hello, learner.'
```

**Hints:**

Use `New-Item -ItemType Directory`, `Set-Content`, and quoted paths. The failed parent repository query is intentional. Do not run init in the parent.

**How to verify:**

Inside `$repoPath`, `Get-ChildItem -Recurse -Force` shows only the intended files/directories and no `.git`. `Get-Content` confirms the text.

**Common mistakes:**

Creating the folder inside an existing application repository, overwriting an old lab, or treating an editor save as version history.

**Explanation:**

You have changed only the filesystem. This makes the effect of initialization observable in the next task.

### Task 3 — Initialize Git and observe untracked files

**Objective:**

Create repository metadata without changing starter content.

**Concept:**

An unborn branch has a name but no commit. Existing files start untracked.

**What to implement:**

Enter the practice directory, initialize with main as the initial branch, and inspect hidden entries and status.

**Starter code:**

```powershell
Set-Location -LiteralPath $repoPath
# TODO: initialize Git with initial branch main.
git status
Get-ChildItem -Force
```

**Hints:**

Use `git init -b main`. Predict whether the README content changes and whether a remote is created.

**How to verify:**

Status reports main, no commits yet, and untracked README/notes/src. `.git` exists. `git remote -v` is empty. `git log` has no history to display yet.

**Common mistakes:**

Expecting init to commit files, create a GitHub repository, or report a usable HEAD hash before the first commit.

**Explanation:**

HEAD now names unborn main. The starter files are still ordinary working-tree content outside the index.

### Task 4 — Stage README and make the first commit

**Objective:**

Record one selected file while leaving other files untracked.

**Concept:**

Staging prepares content; committing records that proposed snapshot.

**What to implement:**

Stage only README, inspect its proposed addition, and commit with message `Add project introduction`.

**Starter code:**

```powershell
git add README.md
git status
git diff --staged
# TODO: commit the staged snapshot with the specified message.
git log --oneline
```

**Hints:**

Use `git commit -m`. Do not stage notes/src yet. Predict which files the root commit will contain.

**How to verify:**

One root commit exists. `git ls-files` lists only README. Notes/src remain untracked. `git show HEAD:README.md` prints the initial README.

**Common mistakes:**

Using `add .` and including all files, expecting a fully clean status, or expecting GitHub to show the commit already.

**Explanation:**

Main points to the new commit; HEAD names main. Untracked files were excluded from its snapshot.

### Task 5 — Stage a version, then edit it again

**Objective:**

Observe two different changes for one filename.

**Concept:**

The index captures content when added; later edits are separate.

**What to implement:**

Append `A staged sentence.` to README and stage it. Then append `A later sentence.` without staging again.

**Starter code:**

```powershell
Add-Content -LiteralPath README.md -Value 'A staged sentence.' -Encoding ascii
git add README.md
# TODO: append the second sentence, then inspect both comparisons.
git diff --staged
git diff
git status --short
```

**Hints:**

Do not commit yet. Write down which sentence is in HEAD, the index, and the working tree.

**How to verify:**

README shows `MM`. Staged diff adds the first sentence; ordinary diff adds the later sentence. The original commit contains neither.

**Common mistakes:**

Staging twice before observing, expecting add to stage future edits automatically, or interpreting MM as two files.

**Explanation:**

You now have three versions of README. The filename is the same; the compared content differs.

### Task 6 — Commit a selected snapshot and finish cleanly

**Objective:**

Prove what a normal commit includes, then establish a clean baseline.

**Concept:**

Commit reads the index even if the working file has newer edits.

**What to implement:**

Without adding README again, commit `Add staged sentence`. Verify that the later sentence remains unstaged. Then stage README, notes/learning-log.md, and src/greeting.txt and commit `Record remaining practice content`.

**Starter code:**

```powershell
# TODO: commit the already staged snapshot before adding anything else.
git show HEAD:README.md
git diff
# TODO: select the three remaining paths, inspect staged diff, and commit.
git status
```

**Hints:**

The first commit in this task should contain only the first appended sentence. The second records the remaining intended content.

**How to verify:**

Three commits now exist; all three starter files are tracked; status is clean. HEAD's README includes both sentences.

**Common mistakes:**

Using commit `-a` for the first step, silently including the later sentence, or forgetting the untracked starter files.

**Explanation:**

You chose a snapshot independently of the newest file, then deliberately brought all remaining practice content into history.

> **Pause point 1 — predict the three areas**
>
> At task 5's end, which README version would a normal commit record? Where did the later sentence exist? Did either add or commit upload anything?

<details>
<summary>Answer</summary>

The commit records the index containing the first appended sentence. The later sentence exists only in the working-tree difference. Neither operation uploads. Task 6 then commits that remaining difference separately.

</details>

### Task 7 — Ignore fake local configuration and generated output

**Objective:**

Keep disposable local files outside versioned snapshots.

**Concept:**

Ignore rules exclude matching untracked paths, without erasing tracked history.

**What to implement:**

Create fake `local.env` containing `DEMO_MODE=practice` and `build/output.txt` containing `generated output`. Create `.gitignore` with `local.env`, `.env`, `build/`, `target/`, and `*.log`. Commit only `.gitignore` as `Ignore local practice output`.

**Starter code:**

```powershell
# TODO: create the fake files and .gitignore; do not use real secrets.
git status --short
git check-ignore -v local.env build/output.txt
# TODO: stage/commit .gitignore only.
```

**Hints:**

Use `New-Item` for build and an array of lines with `Set-Content`. A `.gitignore` file should itself be tracked.

**How to verify:**

Status is clean; `git status --ignored --short` shows ignored paths. `git ls-files -- local.env build/output.txt` returns nothing.

**Common mistakes:**

Staging fake local files before defining ignore rules, or assuming ignore rules would secure a real committed credential.

**Explanation:**

The project now records its exclusion policy. The excluded files remain on disk and need not disappear when branches switch.

## Branching, merging, and conflicts

### Task 8 — Create and inspect the first feature branch

**Objective:**

Create a development line without creating a snapshot.

**Concept:**

HEAD normally names a local branch; a branch names a commit.

**What to implement:**

From clean main, create and switch to `feature/profile`. Draw HEAD, main, and the feature reference before and after.

**Starter code:**

```powershell
git status
# TODO: create and switch with a modern Git command.
git branch
git log --oneline --graph --decorate --all
```

**Hints:**

Use `switch -c`. Both branches initially point to the same tip.

**How to verify:**

Current branch is feature/profile; commit count and files are unchanged. Main and feature decorate the same commit.

**Common mistakes:**

Using `branch` alone and assuming it switched, or expecting a new commit from branch creation.

**Explanation:**

You created a name and redirected HEAD. The next commit will advance that current branch.

### Task 9 — Commit on the feature and inspect main separately

**Objective:**

Observe one branch advance while the other stays still.

**Concept:**

Branch names share ancestry; they do not own independent folder copies.

**What to implement:**

Create `notes/profile.md` containing `I am learning deliberate Git workflows.` Commit it on feature/profile as `Add learner profile`. Switch to main, inspect files, then return to feature/profile.

**Starter code:**

```powershell
# TODO: create, stage, and commit the profile on feature/profile.
git log --oneline --graph --decorate --all
git switch main
Test-Path -LiteralPath notes/profile.md
# TODO: return to the feature and compare.
```

**Hints:**

Start clean before switching. Note the main tip's hash and compare it after the feature commit.

**How to verify:**

Feature is one commit ahead of main. The profile is absent on main and present on feature. No source file was manually deleted.

**Common mistakes:**

Committing before checking the current branch or treating checkout-driven file disappearance as lost history.

**Explanation:**

Switch changes the checked-out snapshot. The feature commit remains reachable through its branch.

### Task 10 — Fast-forward main and delete the finished name

**Objective:**

Integrate an undiverged feature in the correct direction.

**Concept:**

If main is an ancestor, integration can advance its reference without another commit.

**What to implement:**

Switch to main, merge feature/profile with `--ff-only`, verify the profile, then delete the local feature with `-d`.

**Starter code:**

```powershell
git switch main
# TODO: merge the feature into current main; then inspect the graph.
git log --oneline --graph --decorate --all
# TODO: delete the integrated local branch safely.
```

**Hints:**

The source argument names feature/profile. Predict which branch moves and whether a new commit is needed.

**How to verify:**

Main reaches the existing profile commit; no merge commit was created; status is clean. The feature name is gone but its commit/content remains.

**Common mistakes:**

Merging main while still on the feature, or expecting branch deletion to erase integrated content.

**Explanation:**

Main advanced to a descendant. Deleting the redundant name leaves history reachable from main.

### Task 11 — Create divergent histories without conflicting files

**Objective:**

Make a real split in history.

**Concept:**

Two branches diverge when each has a commit absent from the other.

**What to implement:**

Create feature/docs. Add `notes/branch-guide.md` with `Branches name commits.` and commit `Add branch guide`. Return to main. Add `notes/main-note.md` with `Main also advances.` and commit `Add main-side note`.

**Starter code:**

```powershell
# TODO: create feature/docs, write its file, and commit.
git switch main
# TODO: write the separate main-side file and commit.
git log --oneline --graph --decorate --all
```

**Hints:**

Both commits must start from the shared base, not one from the other's new tip.

**How to verify:**

The graph has two children after the old shared tip. Main lacks the guide; feature/docs lacks the main-side note.

**Common mistakes:**

Making both commits on main or changing the same file and unintentionally introducing a conflict early.

**Explanation:**

Main can no longer simply advance to the feature tip while retaining its own new commit.

### Task 12 — Merge diverged histories and inspect two parents

**Objective:**

Create an integration commit while preserving both lines.

**Concept:**

A three-way merge can combine compatible changes automatically.

**What to implement:**

While on main, merge feature/docs using `--no-edit`. Inspect the resulting commit, then delete feature/docs with `-d`.

**Starter code:**

```powershell
git branch --show-current
# TODO: merge feature/docs into current main.
git show --no-patch --format='%h %p %s' HEAD
git log --oneline --graph --decorate --all
```

**Hints:**

`%p` prints abbreviated parent IDs. Two new files should not conflict.

**How to verify:**

The new merge commit has two parents and contains both notes. Status is clean. Existing commits retain their IDs.

**Common mistakes:**

Assuming every merge fast-forwards or treating an automatic merge as proof of software correctness.

**Explanation:**

The merge records both ancestries and a combined snapshot. No earlier commit was rewritten.

### Task 13 — Intentionally stop at a merge conflict

**Objective:**

Observe an unresolved integration before deciding its result.

**Concept:**

Incompatible replacements of the same base line need a human decision.

**What to implement:**

Create feature/conflict-demo and replace the whole greeting with `Hello from feature.` Commit `Change feature greeting`. Switch to main, replace the same line with `Hello from main.`, and commit `Change main greeting`. Attempt to merge feature/conflict-demo. Stop when Git reports a conflict.

**Starter code:**

```powershell
# TODO: create/commit the two different greeting versions on the two branches.
# TODO: while on main, attempt their merge.
git status
Get-Content -LiteralPath src/greeting.txt
```

**Hints:**

Both versions must descend independently from `Hello, learner.` A nonzero merge exit is expected here. Do not run another merge or pull.

**How to verify:**

Status reports an in-progress merge with an unmerged greeting. The file contains current/incoming markers. HEAD remains the main-side commit.

**Common mistakes:**

Editing after merging instead of before, or resolving immediately without reading both intentions.

**Explanation:**

Git has not chosen the final greeting or completed a merge commit. Explain the base, ours, and theirs before continuing.

### Task 14 — Resolve the greeting deliberately

**Objective:**

Choose and record a valid combined result.

**Concept:**

Resolving means authoring final content, then replacing unmerged index entries and completing integration.

**What to implement:**

First propose your own final greeting and explain why it meets both intentions. For the remaining lab's common baseline, use `Hello from both branches.` Remove all markers, stage only the greeting, and finish with `Resolve greeting conflict`. Delete the integrated feature name afterward.

**Starter code:**

```powershell
code src/greeting.txt
# TODO: decide/edit final content and remove markers.
# TODO: stage the resolution, inspect it, and finish the merge.
git status
git log --oneline --graph --decorate --all
```

**Hints:**

`git add` marks your file resolved. It does not finish the merge. If abandoning the still-running attempt instead, use `git merge --abort`, then recreate task 13 before attempting this task again.

**How to verify:**

No markers or unmerged paths remain. Main's merge commit has two parents and the agreed greeting. Status is clean.

**Common mistakes:**

Keeping both conflicting lines unintentionally, staging unrelated edits, or trying to abort after already committing the merge.

**Explanation:**

The computer combined ancestry; you supplied the missing content decision.

> **Pause point 2 — branch and conflict reasoning**
>
> On main, `git merge feature/conflict-demo` changes which branch? Does staging the resolved greeting change HEAD? What additional step finishes the merge?

<details>
<summary>Answer</summary>

Main is the target. Staging settles index entries but does not advance HEAD. Completing the merge creates a commit with main's previous tip and the feature tip as parents; main advances to it. The source name stays at its original tip until deleted.

</details>

## Recovery exercises

Everything through task 22 remains local and unpublished. Start each task on clean main unless instructed otherwise. Backup branches created here are local retention aids; later push only named intended branches, rather than using `push --all`.

### Task 15 — Restore a disposable edit and unstage another

**Objective:**

Distinguish discarding working content from removing staged intent.

**Concept:**

Ordinary restore copies index → working tree; staged restore normally copies HEAD → index.

**What to implement:**

Replace the greeting with `DISPOSABLE EDIT`. Inspect its diff, then deliberately discard it with ordinary restore. Next append `Temporary staged note.` to the learning log and stage it. Unstage while preserving the edit. Inspect, then deliberately discard that disposable log edit too.

**Starter code:**

```powershell
Set-Content -LiteralPath src/greeting.txt -Value 'DISPOSABLE EDIT' -Encoding ascii
git diff
# TODO: restore the greeting from the index.
# TODO: edit/stage the log, then unstage with the modern restore form.
git diff --staged
git diff
```

**Hints:**

Use a specific path. After unstaging, the ordinary diff should still show the temporary log line. Inspect it before the final discard.

**How to verify:**

The committed greeting returns. Unstaging keeps the log edit, while its staged diff becomes empty. Final status is clean; the commit graph never changed.

**Common mistakes:**

Using working-tree restore when you intended only to unstage, or expecting reflog to recover discarded editor content.

**Explanation:**

You manipulated content/index destinations without moving a branch or creating a commit.

### Task 16 — Revert a deliberately bad commit

**Objective:**

Undo committed content while preserving the history record.

**Concept:**

Revert appends an inverse commit, appropriate for many shared corrections.

**What to implement:**

Add `notes/slogan.txt` containing `Wrong slogan: random commands solve everything.` Commit `Add incorrect project slogan`. Capture that commit ID and revert it with `--no-edit`.

**Starter code:**

```powershell
# TODO: create, stage, and commit the intentionally incorrect file.
$badCommit = git rev-parse HEAD
# TODO: revert badCommit without replacing existing history.
git log --oneline -3
git status
```

**Hints:**

Use `git revert --no-edit $badCommit`. Only this isolated addition should be reversed.

**How to verify:**

Both the bad commit and new revert commit appear. The slogan file is absent. Main advanced twice; it did not move backward. Status is clean.

**Common mistakes:**

Using reset because the word “undo” appeared, or assuming revert removes the bad object from history.

**Explanation:**

You practiced a correction that can normally be pushed without asking the server to discard shared ancestry.

### Task 17 — Compare soft and mixed reset safely

**Objective:**

Change an unpublished branch pointer while retaining content.

**Concept:**

Soft preserves index/files; mixed resets the index and preserves files.

**What to implement:**

Create lab/reset from main. Add `notes/reset-demo.md` with `Private reset practice.` and commit `Add private reset note`. Create backup/reset at that tip. Soft-reset lab/reset back one parent. Inspect. Then mixed-reset to its **current HEAD**, not another parent. Inspect again. Re-add the note and commit `Keep reset practice result`. Switch main and fast-forward lab/reset into it; delete lab/reset with `-d`, retaining backup/reset for comparison.

**Starter code:**

```powershell
# TODO: create the isolated branch, file, commit, and backup reference.
git reset --soft 'HEAD~1'
git status
git diff --staged
# TODO: reset the index to current HEAD using mixed mode, without another rewind.
```

**Hints:**

After the soft step, `git reset --mixed HEAD` changes only the index relative to the same target. The newly added note becomes untracked, not an ordinary tracked modification.

**How to verify:**

Soft shows a staged addition; mixed shows an untracked file. File content survives both. Backup/reset still names the original commit. End clean on main with the recommitted note integrated.

**Common mistakes:**

Using `HEAD~1` twice and rewinding farther, omitting the backup, or using hard reset to force a clean status.

**Explanation:**

Your local name moved; original objects remained reachable through backup/reset. No hosted history exists to disrupt. Hard reset is conceptual only here because it can overwrite useful content.

### Task 18 — Amend a private message and forgotten file

**Objective:**

Replace a local tip deliberately and observe its identity change.

**Concept:**

Amend creates a replacement commit; it does not edit the original object.

**What to implement:**

Create `notes/amend.md` containing `A focused change.` Commit with the intentionally misspelled message `Add amend ntoe`. Save its ID. Create `notes/amend-extra.md` containing `This belongs to the same change.` Stage it, inspect, then amend the latest commit with message `Add amend notes`.

**Starter code:**

```powershell
# TODO: create the first file and typo-message commit.
$beforeAmend = git rev-parse HEAD
# TODO: create/stage the forgotten file and amend the unpublished commit.
git log -1 --oneline
git rev-parse HEAD
```

**Hints:**

Inspect all staged content before amend. Use a changed message so the identity difference is unambiguous.

**How to verify:**

The tip ID differs from `$beforeAmend`, the message is corrected, and both files are in the replacement snapshot. Commit count along main did not increase for the amend step itself.

**Common mistakes:**

Amending an unrelated tip, including another staged experiment, or assuming the same operation is harmless on a branch already used by teammates.

**Explanation:**

Main now names the replacement. The old tip may appear in local reflog, but it is no longer main's latest commit.

### Task 19 — Stash unfinished work and restore it

**Objective:**

Pause work safely and compare apply with pop.

**Concept:**

A stash is a local saved change set, separate from an ordinary published branch workflow.

**What to implement:**

Append `Unfinished tracked note.` to the learning log and create untracked `notes/wip.txt` containing `Unfinished new note.` Stash both using `-u` and a message. Switch briefly to new lab/stash-break, then return main. Apply the stash, verify, and drop the saved entry only after checking content. Commit both files as `Record restored notes`. For a second cycle, append `Second stash cycle.` to the log, stash it, pop it, verify, and commit `Record popped note`. Delete lab/stash-break safely.

**Starter code:**

```powershell
# TODO: make the two unfinished changes.
git stash push -u -m "Unfinished learning notes"
git stash list
# TODO: switch away and back, then apply the saved entry without removing it.
git status
```

**Hints:**

Quote `'stash@{0}'` in PowerShell. Apply retains its entry; successful pop removes its entry. Check the list before dropping anything.

**How to verify:**

After stash, tracked changes and the untracked WIP file are saved and absent from normal status. Apply restores both while retaining the entry. The second successful pop restores content and removes its entry. End clean on main.

**Common mistakes:**

Forgetting `-u`, applying twice, dropping before inspection, or assuming ignored files were included.

**Explanation:**

You controlled when working changes disappeared and returned. Applying onto changed content can conflict; the saved entry is not a permanent backup.

### Task 20 — Recover a temporarily lost commit through reflog

**Objective:**

Find and preserve an earlier local tip without destroying working content.

**Concept:**

Reflog records local reference movements; a new branch can rescue a found commit.

**What to implement:**

Create lab/reflog from main. Commit `notes/recovered.md` containing `This commit can be found again.` as `Add recoverable note`. Soft-reset that branch back one parent. Inspect reflog and find the removed tip by its message/hash. Create rescue/reflog there, then switch to rescue/reflog. Its snapshot matches the preserved staged note, so status should become clean. Switch main, fast-forward rescue/reflog, and delete lab/reflog and rescue/reflog with `-d`.

**Starter code:**

```powershell
# TODO: create lab/reflog and the recoverable commit.
git reset --soft 'HEAD~1'
git reflog --oneline -6
# TODO: inspect the recovered hash and give it the name rescue/reflog.
```

**Hints:**

Immediately after the reset, `'HEAD@{1}'` refers to the prior HEAD tip. Read the actual entry instead of assuming reflog numbers stay fixed after more operations.

**How to verify:**

The rescued branch names the original commit ID. Switching to it preserves content and produces a clean comparison. Final main reaches that commit. No hard reset or file deletion was needed.

**Common mistakes:**

Calling reflog a permanent remote backup, selecting an entry by position after several additional checkouts, or overwriting current state before naming the recovered object.

**Explanation:**

The commit was temporarily absent from the experiment branch's ancestry, but locally retained data let you locate and name it again.

### Task 21 — Cherry-pick a focused change onto different ancestry

**Objective:**

Reuse one change without merging its entire branch.

**Concept:**

Replay on a different parent normally creates a distinct commit identity.

**What to implement:**

Create lab/cherry-source from main. Commit `notes/portable-tip.md` containing `Inspect state before changing it.` as `Add portable Git tip`; capture its hash. Switch main, commit separate `notes/cherry-context.md` containing `The destination has its own commit.` as `Add destination context`. Cherry-pick the captured source commit onto main. Keep lab/cherry-source for later graph comparison.

**Starter code:**

```powershell
# TODO: commit the source tip on lab/cherry-source.
$tipCommit = git rev-parse HEAD
git switch main
# TODO: create/commit destination context BEFORE picking tipCommit.
# TODO: cherry-pick tipCommit, then compare IDs and parents.
```

**Hints:**

Different parent context makes the new identity demonstrable. Avoid editing the same file in the context commit.

**How to verify:**

Main contains the portable tip and destination context. Its cherry-picked commit has a different ID/parent from `$tipCommit`. The source name still points to the original.

**Common mistakes:**

Picking without a distinct destination parent and assuming IDs must always differ, or merging when the task asks to select one commit.

**Explanation:**

Equivalent introduced changes do not imply identical commit objects. The source commit itself is not an ancestor of main through this pick.

### Task 22 — Rebase a private feature and compare with merge

**Objective:**

Recreate a feature on newer main and explain the rewritten identities.

**Concept:**

Rebase replays selected commits; merging combines ancestry while retaining existing IDs.

**What to implement:**

Create feature/rebase-demo from main. Commit `notes/rebase-feature.md` with `Private feature work.` as `Add private rebase feature`. Save its ID and create backup/rebase. Switch main, add `notes/rebase-main.md` containing `Main advanced independently.` and commit `Advance main for rebase`. Draw the split. Rebase feature/rebase-demo onto main, compare IDs/parents, then switch main and fast-forward the rebased feature. Delete the finished feature name, keeping backup/rebase for comparison.

**Starter code:**

```powershell
# TODO: create/commit feature work, preserve its old tip, then advance main.
git log --oneline --graph --decorate --all
git switch feature/rebase-demo
# TODO: rebase onto main; inspect the rebuilt tip before integrating it.
```

**Hints:**

Use different files to avoid unnecessary conflicts. On an unexpected replay conflict, inspect, resolve/add, then `rebase --continue`, or `rebase --abort` to abandon. Do not skip a patch blindly.

**How to verify:**

The feature's new parent is the newer main; its hash changed. Main did not move until the final fast-forward. Status is clean; backup/rebase still names the old commit.

**Common mistakes:**

Running rebase while on main, claiming old objects were edited, or practicing this first on already shared work.

**Explanation:**

Compare task 12's two-parent merge with this rebuilt linear feature. Neither strategy is universally better; the right choice depends on shared history and team policy.

> **Pause point 3 — choose the safe correction**
>
> Which operations above recreated commit identities? If a teammate had already pulled them, what would change about your decision? What can reflog recover that an editor-only undo cannot?

<details>
<summary>Answer</summary>

Amend and rebase created replacements; cherry-pick created a replay on another parent. Reset moved a ref without editing its old objects. Shared rewrites need coordination; prefer a corrective commit/revert for an ordinary shared mistake. Reflog can locate prior local tips while retained, but cannot generally recover never-recorded edits discarded by restore.

</details>

## GitHub collaboration phase

Only now add hosting. Core tasks assume your own ordinary repository permits direct main pushes for the deliberate synchronization demonstrations. Leave protection/ruleset experiments until the optional section. Do not change protections on an existing team repository to fit this lab.

### Task 23 — Create an empty GitHub repository

**Objective:**

Prepare a hosted destination for existing local history.

**Concept:**

Creating hosting is distinct from init, commit, and push.

**What to implement:**

On GitHub, create your own repository named git-github-practice (or a distinct practice name if taken). Choose public or private deliberately. Do not initialize README, ignore rules, or a license for this already initialized local history. Copy its HTTPS or SSH URL without embedded credentials.

**Starter code:**

```text
GitHub -> New repository
Owner: your account
Name: git-github-practice
Initialize files: none
```

**Hints:**

HTTPS with Git Credential Manager is usually simplest on Windows. Read the theory's [authentication section](Git_GitHub_Essential_Concepts.md#50-authentication) before your first protected operation.

**How to verify:**

GitHub shows an empty repository's setup instructions. Your local graph/files are unchanged and `git remote -v` is still empty.

**Common mistakes:**

Initializing an unrelated remote root, using someone else's repository, or posting a PAT in the URL.

**Explanation:**

You created a destination and permissions, not synchronization. Your history still lives locally until pushed.

### Task 24 — Add origin, push main, and establish tracking

**Objective:**

Share the intended local history and configure its counterpart.

**Concept:**

Origin names a remote configuration; `-u` establishes a branch's upstream relationship.

**What to implement:**

Set `$repoUrl` to the copied URL, add origin, inspect it, and push main with `-u`. Authenticate using the selected method. Push only main, leaving local lab/backup branches local.

**Starter code:**

```powershell
$repoUrl = Read-Host 'Paste the repository HTTPS or SSH URL (no embedded token)'
# TODO: add origin with repoUrl and inspect remote -v.
git push -u origin main
git branch -vv
```

**Hints:**

GCM can open browser authentication. A GitHub account password is not a Git-over-HTTPS password. Changing `user.email` will not repair missing authorization.

**How to verify:**

GitHub main shows the local history/content. Main tracks origin/main; their tips agree locally. Normal push did not upload fake ignored files, stash entries, or every local branch name.

**Common mistakes:**

Using `push --all`, mistaking origin for a branch, or solving an authentication error with force.

**Explanation:**

Objects reachable from main were transferred and the hosted main ref updated. Tracking configuration remains a local relationship.

### Task 25 — Push a new feature branch

**Objective:**

Make a proposal available without changing hosted main.

**Concept:**

A pushed feature is another server-side branch reference.

**What to implement:**

From clean main, create feature/github-profile. Add `notes/github-profile.md` containing `This feature will be reviewed through a Pull Request.` Commit `Add GitHub workflow profile` and push that branch with `-u`.

**Starter code:**

```powershell
# TODO: create feature/github-profile, write its file, stage, inspect, commit.
# TODO: publish this branch and establish its tracking counterpart.
git branch -vv
```

**Hints:**

Explicitly name origin and feature/github-profile in the first push. No PR has been created by Git itself.

**How to verify:**

Both local and hosted feature have the new commit. Main remains on its previous tip and lacks the new file. The feature tracks origin/feature/github-profile.

**Common mistakes:**

Committing on main or expecting feature push to integrate its changes into main automatically.

**Explanation:**

You shared an alternate development tip. The proposal/review decision comes next.

### Task 26 — Open a PR and perform a self-review

**Objective:**

Inspect a hosted integration proposal as a reviewer.

**Concept:**

A PR is GitHub collaboration data around branches/commits, not a Git object.

**What to implement:**

Open a PR with base main and compare feature/github-profile. Title it `Add GitHub workflow profile`. Describe the change and verification. Inspect commits and Files changed; check for accidental files and credentials. Add a useful comment if desired.

**Starter code:**

```text
Base: main
Compare: feature/github-profile
Description:
  Change: ...
  Reason: ...
  Verification: ...
```

**Hints:**

Authors cannot approve their own PRs. Reading and commenting is enough for this solo review; no second account or required approval rule is needed. See [GitHub's review rules](https://docs.github.com/en/pull-requests/how-tos/review-pull-requests/approving-a-pull-request-with-required-reviews).

**How to verify:**

The PR shows exactly the intended file and commit(s), with correct base/head. Hosted main and local main remain unchanged until integration.

**Common mistakes:**

Reversing base/compare, approving your own work as a requirement, or equating PR creation with a merge.

**Explanation:**

The proposal is now reviewable. Comments alone have changed no Git snapshot.

### Task 27 — Merge the PR with a merge commit

**Objective:**

Connect hosted review completion to Git ancestry.

**Concept:**

The selected merge strategy determines the commits and parents on the base branch.

**What to implement:**

Choose **Create a merge commit**, confirm the merge, and inspect GitHub's commit history. If unavailable in your own practice repository, enable the ordinary merge-commit option in repository settings for this lab. Delete the hosted feature branch using GitHub's post-merge button if desired.

**Starter code:**

```text
PR -> merge method: Create a merge commit -> confirm
Inspect: merge commit, two parents, feature file on main
```

**Hints:**

GitHub's merge-commit strategy records a merge even when local Git could fast-forward. Local files do not receive this event automatically.

**How to verify:**

GitHub main contains the profile and a merge commit. In your terminal, local main has not moved merely because you clicked Merge.

**Common mistakes:**

Selecting squash then expecting identical ancestry, or assuming deleting the hosted feature also deletes its local branch.

**Explanation:**

The server changed its branch. You now need to update local knowledge and integrate it deliberately.

### Task 28 — Update local main after the hosted merge

**Objective:**

Synchronize clean local main and remove a finished local name.

**Concept:**

Pull includes fetch plus integration, while status/branch output can use stale local knowledge.

**What to implement:**

Inspect status and branch tracking before fetching. Switch main, then use `git pull --ff-only`. Inspect the graph and delete local feature/github-profile with `-d`. If the hosted branch was deleted, fetch with `--prune` to remove its stale tracking reference too.

**Starter code:**

```powershell
git status
git branch -vv
git switch main
# TODO: pull with an explicit fast-forward-only policy.
# TODO: verify ancestry and delete the integrated local feature.
```

**Hints:**

Local main can fast-forward to the hosted merge commit because its old tip is an ancestor. This does not create another local merge commit.

**How to verify:**

Main and origin/main agree at the hosted merge, profile content is present, status is clean, and the local feature name is gone.

**Common mistakes:**

Pulling while still on the feature, believing an earlier “up to date” message queried the server, or confusing prune with local branch deletion.

**Explanation:**

Local state now reflects the hosted integration. Explicit pull policy kept the operation predictable.

### Task 29 — Fetch a browser-created commit without integrating it

**Objective:**

Separate knowledge refresh from branch/worktree changes.

**Concept:**

Ordinary fetch updates remote-tracking refs, leaving local main alone.

**What to implement:**

On GitHub's main, use Add file/Create new file to commit `notes/browser-note.md` with `Created through GitHub.` and message `Add browser-side note`. In the original terminal, save main and origin/main hashes before fetching. Fetch origin, compare the refs, and verify the file is still absent locally. Only after explaining that state, integrate with `git merge --ff-only origin/main`.

**Starter code:**

```powershell
$mainBeforeFetch = git rev-parse main
$trackingBeforeFetch = git rev-parse origin/main
# TODO: after the browser commit, fetch without pulling.
git log --oneline --graph --decorate --all
Test-Path -LiteralPath notes/browser-note.md
```

**Hints:**

Make sure the browser commits to main in this disposable repository. Fetch is the experiment; do not integrate until after checking the missing local file.

**How to verify:**

After fetch alone, main stays at its saved hash while origin/main advances. The file appears in origin/main's snapshot, not the current worktree. After the explicit integration, main advances and the file appears.

**Common mistakes:**

Using pull and missing the intermediate state, or interpreting a clean working tree as proof main equals GitHub.

**Explanation:**

You observed two separate operations: learn a new tip, then choose to check out its content through integration.

> **Pause point 4 — three different main references**
>
> Immediately after the browser commit but before fetch, which main changed? Immediately after fetch alone, which local reference changed? Which operation made the new file appear locally?

<details>
<summary>Answer</summary>

Only GitHub main changed in the browser. Fetch advanced origin/main and downloaded objects. The final fast-forward advanced local main and updated its index/working tree.

</details>

### Task 30 — Simulate another machine with a second clone

**Objective:**

Observe independent local repositories using one hosted destination.

**Concept:**

Each clone has its own refs, files, index, config, and reflog.

**What to implement:**

Clone `$repoUrl` into a fresh sibling `git-github-practice-collaborator`. Use the same PowerShell session or re-establish your path variables. In the second clone, configure local identity if needed, create `notes/collaborator-note.md` containing `A second clone made this change.`, commit `Add second-clone note`, and push main. Return to the original without fetching yet.

**Starter code:**

```powershell
$collabPath = Join-Path $practiceRoot 'git-github-practice-collaborator'
if (Test-Path -LiteralPath $collabPath) { throw 'Choose a fresh clone directory.' }
# TODO: clone repoUrl into collabPath, then make/commit/push the note there.
Set-Location -LiteralPath $repoPath
git status
git log -1 --oneline
```

**Hints:**

Set local name/email if you want distinct simulated authorship; this still uses your account for access. Confirm `Get-Location` before each commit/push.

**How to verify:**

The second clone and GitHub have the new commit. Original main and its origin/main remain at their earlier values until network synchronization.

**Common mistakes:**

Editing in the wrong window/directory or expecting both origin/main refs to be shared storage.

**Explanation:**

The two folders are independent repositories. Sharing a server does not create live synchronization.

### Task 31 — Fetch and integrate the second clone's change

**Objective:**

Explain all three tips using a real remote update.

**Concept:**

Fetch changes local knowledge; integration changes local main.

**What to implement:**

In the original clone, compare main and origin/main before fetch. Fetch, inspect `main..origin/main`, then fast-forward main explicitly. Explain where the new commit was after each step.

**Starter code:**

```powershell
Set-Location -LiteralPath $repoPath
# TODO: record/inspect main and origin/main, then fetch.
git log --oneline main..origin/main
# TODO: integrate the fetched descendant without creating another merge commit.
```

**Hints:**

With no original-side commit yet, `merge --ff-only origin/main` should succeed. No force is needed.

**How to verify:**

After fetch alone, original main is behind by one. After integration, the note exists, main equals origin/main, and status is clean.

**Common mistakes:**

Calling origin/main the hosted ref itself or using fetch and claiming it updated your working files.

**Explanation:**

You have repeated the browser experiment using a separate Git client, proving that the model does not depend on the GitHub editor.

### Task 32 — Create and resolve a normal push rejection

**Objective:**

Preserve both developers' commits when histories diverge.

**Concept:**

A normal push refuses to replace a hosted tip that is not an ancestor of the proposed new tip.

**What to implement:**

In the second clone, update main with `pull --ff-only`, add `notes/remote-only.md` containing `Keep this remote-side change.`, commit `Add remote-side change`, and push. Without fetching in the original, add `notes/local-only.md` containing `Keep this local-side change.`, commit `Add local-side change`, and attempt a normal main push. Read the rejection. Fetch in the original, draw the split, merge origin/main with `--no-edit`, inspect/test the combined result, then push normally.

**Starter code:**

```powershell
# TODO: create/push the second-clone commit, then independently commit in original.
# Original clone:
git push origin main
# Expected: rejection. Stop and inspect; do not force.
# TODO: fetch, compare both unique histories, integrate, verify, then push.
```

**Hints:**

Use separate files so the integration succeeds automatically. The original will be one ahead and one behind its refreshed origin/main. Rejection is the expected result, not a reason to discard either commit.

**How to verify:**

The first push fails with non-fast-forward/fetch-first wording. After merge and normal push, both files and both parent histories appear on GitHub. Original main equals origin/main and status is clean.

**Common mistakes:**

Pulling before the intended rejection, committing both changes in one clone, force pushing, or resetting away the local commit without preserving it.

**Explanation:**

Git protected the remote-side ancestry. Fetch plus deliberate merge satisfied the ancestry requirement while retaining both changes.

> **Pause point 5 — explain the rejection without commands**
>
> Why was the first push rejected? Did it upload your uncommitted files? Why could the second ordinary push succeed after integration?

<details>
<summary>Answer</summary>

The remote had a commit absent from the original's branch. Replacing that tip would not be a fast-forward. A normal branch push concerns committed reachable objects, not working edits. The new merge contained both tips as ancestors, so the remote tip became an ancestor of the proposed main.

</details>

### Task 33 — Track a small Issue through another PR

**Objective:**

Connect a work item to a focused branch and reviewed change.

**Concept:**

Issue, branch, commit, and PR each answer a different question.

**What to implement:**

Create GitHub Issue `Improve README usage section` and record its actual number. From updated main, create docs/readme-usage. Append a README usage section explaining that this repository is a disposable lab. Commit `Document practice usage`, push the branch, and open a PR to main. Put `Closes #<actual number>` in its description. Self-review, merge using a merge commit, update local main with `pull --ff-only`, and delete the integrated local feature name.

**Starter code:**

```text
Issue number: record the actual value
Branch: docs/readme-usage
PR base: main
PR description: describe change, verification, and Closes #actual-number
```

**Hints:**

Use the actual default branch main. Closing keywords work in supported links/merge contexts; inspect whether the Issue closes after merge. See [GitHub's linking guidance](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/linking-a-pull-request-to-an-issue).

**How to verify:**

The Issue links to the PR and closes after its suitable merge. README usage is on both hosted and local main. Status is clean.

**Common mistakes:**

Copying an illustrative Issue number, using the wrong base, or putting a closing claim in an unrelated commit without checking the result.

**Explanation:**

The Issue described needed work; the branch held development; the commit recorded content; the PR supplied review and integration.

### Task 34 — Create and push an annotated version tag

**Objective:**

Name the verified practice version.

**Concept:**

A tag labels a recorded object rather than following ongoing development.

**What to implement:**

On clean synchronized main, create annotated tag v1.0.0 with message `Complete Git and GitHub practice version`. Inspect it and push only that tag.

**Starter code:**

```powershell
git status
git log -1 --oneline
# TODO: create the annotated tag, inspect it, and explicitly push it.
```

**Hints:**

Use `tag -a`, not both lightweight and annotated creation with the same name. If v1.0.0 already exists, diagnose before choosing a new label; do not casually move a published tag.

**How to verify:**

`git show v1.0.0` shows tag metadata and the intended commit. `git rev-list -n 1 v1.0.0` equals main's tip. GitHub displays the tag.

**Common mistakes:**

Tagging from the wrong branch/tip, assuming uncommitted files enter a tag, or assuming an ordinary branch push always pushes tags.

**Explanation:**

The version now has a stable reference. No Release page has been created merely by pushing it.

### Task 35 — Publish a simple practice Release and assess readiness

**Objective:**

Present the tagged version and finish with evidence of understanding.

**Concept:**

A GitHub Release is hosting metadata around a Git tag, not another commit.

**What to implement:**

On GitHub, draft a new Release using the existing v1.0.0 tag. Title it `Git practice v1.0.0`. Describe the local Git, conflict, recovery, and PR skills practiced; no binary assets are needed. Publish it in your own disposable repository, then complete the verification matrix and checklist below.

**Starter code:**

```text
Repository -> Releases -> Draft a new release
Tag: existing v1.0.0
Title: Git practice v1.0.0
Notes: what this practice version demonstrates
```

**Hints:**

Select the existing tag rather than creating another label on an unintended target. A Release does not automatically deploy anything. See [GitHub's release overview](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases).

**How to verify:**

The published Release points to v1.0.0, which resolves to the verified commit. Local status is still clean; publishing the Release did not create a new local commit.

**Common mistakes:**

Confusing release notes with a commit message or claiming the local repository automatically includes hosted Issue/PR/Release discussions.

**Explanation:**

You connected versioned content, a stable label, and hosted presentation. Core practice is complete when you can explain its state transitions independently.

## Optional advanced practice

These independent extensions come **after** the core lab. They are not prerequisites. Give each a new feature or experiment branch, inspect before/after, and return main to a clean state when finished.

### Optional A — Fork workflow and origin plus upstream

Draw `original repository -> your fork -> local clone`. On a repository you are permitted to fork, clone your fork; origin normally points to it. Add the original repository as upstream:

```powershell
git remote add upstream <ORIGINAL_REPOSITORY_URL>
git fetch upstream
git remote -v
```

Inspect before updating your base from upstream/main, then create a fresh contribution branch, push to origin, and propose a cross-repository PR if you actually have a useful permitted contribution. A conceptual drawing is sufficient; do not create a nuisance PR or fork your own repository just to fill a checkbox. Upstream as a remote name differs from a branch's configured upstream.

### Optional B — Rulesets or branch protection

In your own disposable repository, inspect Settings for rules targeting main. Plan a PR-only or force-push restriction and read its effect before enabling it. Do not require another person's approval in this solo exercise. Do not require a check that has never run. Availability varies by plan/visibility; consult [GitHub rulesets](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/about-rulesets). Revert the optional setting if it blocks your intended solo experiments, using the repository's actual admin controls.

Verification: a local commit on main is still possible; the hosted policy controls whether the server accepts its update. Write down why an attempted operation is accepted/rejected.

### Optional C — Squash and rebase merge

Create a new branch from updated main and make two small commits. On a new PR, choose Squash and merge; inspect the **new single commit** on main and compare ancestry. On another fresh branch/PR, try GitHub Rebase and merge; inspect its new commit IDs. Read [the merge-strategy comparison](Git_GitHub_Essential_Concepts.md#46-github-merge-strategies) first.

Do not assume local `branch -d` understands equivalent content after squash/rebase. Compare the PR result and retain a backup name before any forced local deletion. Start the next feature from updated main.

### Optional D — An explicit lease on a disposable remote branch

This exercise intentionally replaces **only** a new remote branch named `lab/lease-demo`; never aim it at main. Finish the core lab first. Start with a clean working tree and verify no local/remote branch with that name already exists.

```powershell
git switch -c lab/lease-demo
Set-Content -LiteralPath notes/lease-demo.md -Value 'Disposable lease experiment.' -Encoding ascii
git add notes/lease-demo.md
git commit -m "Add disposable lease note"
git push -u origin lab/lease-demo
$expectedLeaseTip = git rev-parse HEAD
git branch backup/lease-original
git commit --amend -m "Clarify disposable lease experiment"
# This rewrites a published branch. Use only this isolated agreed experiment branch.
git push "--force-with-lease=refs/heads/lab/lease-demo:$expectedLeaseTip" origin HEAD:refs/heads/lab/lease-demo
```

Verify that the hosted experiment branch points to the amended tip and main is unchanged. Repeating the push with the old expectation after another actual remote update should reject. For a stronger simulation, have the second clone fetch and add a commit to **lab/lease-demo only**, then try the original's now-stale expectation. Preserve any useful content and inspect the updated tip before further integration; do not respond with unconditional force. An explicit lease guards an expected ID, not the content quality or your teammates' agreement. Keep the branch/backup until you finish inspecting.

### Optional E — Smallest GitHub Actions demonstration

From updated main, create feature/actions-demo. Put the following in `.github/workflows/practice.yml`, commit/push the feature, and inspect its Actions run and PR checks:

```yaml
name: Practice event
on: [push, pull_request]
permissions: {}
jobs:
  explain:
    runs-on: windows-latest
    steps:
      - name: Show that a job ran
        shell: pwsh
        run: Write-Output 'A GitHub event started this practice job.'
```

This job needs no checkout action, secret, package installation, or deployment. It only prints a message. `permissions: {}` grants no repository permissions to its token because none are needed. See the [workflow syntax reference](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax). Availability and usage limits depend on account/repository settings; the extension remains optional.

Verification: identify event → workflow → job → step → result. Explain why a successful print job does not prove application correctness. Delete or retain the demonstration through an ordinary PR according to your preference.

## Pre-solution checkpoint

```text
============================================================
STOP — COMPLETE THE PRACTICE TASKS BEFORE CONTINUING
============================================================
```

Explain these without notes before opening the walkthrough:

1. Working tree vs staging area vs recorded repository history.
2. What HEAD points to, and how a branch moves.
3. Why task 5 had both staged and unstaged README changes.
4. Fast-forward vs merge commit, and merge direction.
5. Why the greeting conflict happened and how resolution finished.
6. Restore vs reset vs revert; which state each targeted.
7. How amend/rebase changed identities and why we practiced them unpublished.
8. What stash retained, and how reflog found a previous tip.
9. What cherry-pick recreated.
10. Main vs origin/main vs GitHub main before/after fetch.
11. Fetch vs pull, and what an ordinary push sends.
12. Clone vs fork; origin vs a remote named upstream.
13. A PR's base/head, self-review limits, and the chosen merge strategy.
14. Why task 32 rejected the push and how integration preserved both sides.
15. Issue vs branch vs commit vs PR vs tag vs Release.

<details>
<summary>Checkpoint answer criteria</summary>

Good answers name the data location and affected reference, separate content from ancestry, distinguish a local cached remote tip from the live hosted tip, and explain why shared history constrains rewriting. Use the companion theory for a missing concept, then retry the matching task rather than memorizing an unexplained recipe.

</details>

## Verification plan

Record evidence as you reach each milestone. Put notes outside the repository if you do not intend them as another versioned exercise change.

| Milestone | Evidence to collect | Expected invariant |
|---|---|---|
| init and first commit | status, ls-files, show | only deliberately staged files recorded |
| task 5 | both diffs and short status | README's index and working versions differ |
| first feature | graph before/after | only current feature advances |
| fast-forward | target tip and commit count | target advances; no extra commit |
| divergent merge | parent IDs and both files | two parents, original IDs retained |
| conflict | status/markers, then final file/parents | unresolved state becomes verified merge |
| restore/unstage | before/after diffs | discard and unstage have different destinations |
| revert | log and absent slogan | bad commit plus corrective commit remain |
| reset | graph, status, backup/reset | soft stages; mixed preserves file as untracked |
| amend/rebase | saved old IDs vs new IDs/parents | replacement identities differ |
| stash/reflog | stash list; rescue hash | saved content restored; old tip named safely |
| cherry-pick | source/target parents | same intended change, different identity/context |
| initial push | branch -vv and hosted history | main tracks origin/main; hosted main agrees |
| PR merge | hosted PR and local graph afterward | server merge becomes local through synchronization |
| fetch-only | saved tips and missing worktree file | origin/main advances before main |
| second clone | two independent graphs | no live ref synchronization |
| push rejection | rejection plus integrated graph | both sides preserved before accepted push |
| Issue/tag/Release | linked PR, tag target, Release page | hosted records refer to intended history |

Hashes, timestamps, generated merge messages, and exact status wording can differ. “Clean” means no relevant file/index difference; separately verify upstream relationships and hosted state.

## Complete Reference Walkthrough

Read only after attempting the tasks. This is a milestone-based explanation, not an instruction to replay commands blindly in your already completed repository. Starting fresh in another empty disposable location is appropriate for a replay.

The blocks below are sequential for the **core** path. Execute one milestone at a time and inspect its expected state. Commands marked as expected failures require you to stop and inspect; they are not evidence of a broken lab. Optional extensions are outside this sequence. Ordinary local backup/source branches from recovery are retained for comparison and are not pushed by the named main pushes.

### Milestone 1 — From ordinary files to three recorded snapshots

Tasks 1–6 teach that creation, staging, and recording are different transitions. Keep existing correct global identity. If identity is missing, configure it using your chosen name/email before committing.

<!-- verify: bootstrap -->
```powershell
$practiceRoot = Join-Path $env:USERPROFILE 'git-learning-labs'
$repoPath = Join-Path $practiceRoot 'git-github-practice'
if (Test-Path -LiteralPath $repoPath) { throw 'Choose a fresh practice location.' }
New-Item -ItemType Directory -Path $practiceRoot -Force | Out-Null
# Confirm the parent is outside an existing repository before proceeding.
git -C "$practiceRoot" rev-parse --show-toplevel
# Expected failure: not a git repository. If a root was printed, choose another parent.
New-Item -ItemType Directory -Path $repoPath | Out-Null
Set-Location -LiteralPath $repoPath
New-Item -ItemType Directory -Path notes,src | Out-Null
Set-Content -LiteralPath README.md -Value @('# Git and GitHub Practice','','Disposable version-control lab.') -Encoding ascii
Set-Content -LiteralPath notes/learning-log.md -Value '# Learning log' -Encoding ascii
Set-Content -LiteralPath src/greeting.txt -Value 'Hello, learner.' -Encoding ascii
git init -b main
git status
git add README.md
git diff --staged
git commit -m "Add project introduction"
Add-Content -LiteralPath README.md -Value 'A staged sentence.' -Encoding ascii
git add README.md
Add-Content -LiteralPath README.md -Value 'A later sentence.' -Encoding ascii
git diff --staged
git diff
git status --short
```

At this stop, HEAD contains the original README, the index adds the staged sentence, and the working tree also adds the later sentence. Short status contains MM for README plus untracked starter paths. Commit **before** staging again to prove the distinction:

<!-- verify: snapshots -->
```powershell
git commit -m "Add staged sentence"
git show HEAD:README.md
git diff
git add README.md notes/learning-log.md src/greeting.txt
git diff --staged
git commit -m "Record remaining practice content"
git status
git log --oneline
```

Expected: three commits on main, all starter files tracked, clean status. The middle commit lacks the later sentence; the third includes it. No remote exists yet.

### Milestone 2 — Exclude output and integrate one undiverged feature

<!-- verify: ignore-feature -->
```powershell
New-Item -ItemType Directory -Path build | Out-Null
Set-Content -LiteralPath local.env -Value 'DEMO_MODE=practice' -Encoding ascii
Set-Content -LiteralPath build/output.txt -Value 'generated output' -Encoding ascii
Set-Content -LiteralPath .gitignore -Value @('local.env','.env','build/','target/','*.log') -Encoding ascii
git check-ignore -v local.env build/output.txt
git add .gitignore
git commit -m "Ignore local practice output"
git switch -c feature/profile
Set-Content -LiteralPath notes/profile.md -Value 'I am learning deliberate Git workflows.' -Encoding ascii
git add notes/profile.md
git commit -m "Add learner profile"
git switch main
Test-Path -LiteralPath notes/profile.md
git switch feature/profile
Test-Path -LiteralPath notes/profile.md
git switch main
git merge --ff-only feature/profile
git branch -d feature/profile
git status
```

The two Test-Path results are False, then True. After integration, main points to the already existing feature commit. Deleting the feature name leaves its file/history intact. Ignored fake/generated files stay on disk and outside normal status.

### Milestone 3 — Divergence, two parents, and an unresolved conflict

<!-- verify: divergent-merge -->
```powershell
git switch -c feature/docs
Set-Content -LiteralPath notes/branch-guide.md -Value 'Branches name commits.' -Encoding ascii
git add notes/branch-guide.md
git commit -m "Add branch guide"
git switch main
Set-Content -LiteralPath notes/main-note.md -Value 'Main also advances.' -Encoding ascii
git add notes/main-note.md
git commit -m "Add main-side note"
git log --oneline --graph --decorate --all
git merge --no-edit feature/docs
git show --no-patch --format='%h %p %s' HEAD
git branch -d feature/docs
```

```text
          F-------.
         /         \
...---B---L---------M  main

F = feature guide commit; L = main-side note; M has parents L and F.
```

Now deliberately use the same base greeting line on two diverging histories:

<!-- verify: conflict -->
```powershell
git switch -c feature/conflict-demo
Set-Content -LiteralPath src/greeting.txt -Value 'Hello from feature.' -Encoding ascii
git add src/greeting.txt
git commit -m "Change feature greeting"
git switch main
Set-Content -LiteralPath src/greeting.txt -Value 'Hello from main.' -Encoding ascii
git add src/greeting.txt
git commit -m "Change main greeting"
git merge feature/conflict-demo
# EXPECTED nonzero exit: conflict. Inspect before continuing.
git status
Get-Content -LiteralPath src/greeting.txt
```

At this stop: HEAD is still the main-side greeting commit; no merge result commit exists. The index has unmerged entries and the file contains markers. The complete intended result for the shared lab baseline is:

<!-- verify: resolve -->
```powershell
Set-Content -LiteralPath src/greeting.txt -Value 'Hello from both branches.' -Encoding ascii
git add src/greeting.txt
git diff --staged
git status
git commit -m "Resolve greeting conflict"
git branch -d feature/conflict-demo
git show --no-patch --format='%h %p %s' HEAD
git status
```

Expected: two-parent merge, no marker text, no in-progress merge, clean status. If instead you aborted before completion, recreate the merge before applying this resolution sequence.

### Milestone 4 — Content undo, corrective history, and private reset

Tasks 15–17 affect different destinations. The first block discards only the disposable edits it just creates; do not substitute meaningful changes.

<!-- verify: restore-revert -->
```powershell
Set-Content -LiteralPath src/greeting.txt -Value 'DISPOSABLE EDIT' -Encoding ascii
git diff
git restore src/greeting.txt
Add-Content -LiteralPath notes/learning-log.md -Value 'Temporary staged note.' -Encoding ascii
git add notes/learning-log.md
git restore --staged notes/learning-log.md
git diff --staged
git diff
# Deliberately discard only the temporary note just inspected.
git restore notes/learning-log.md
Set-Content -LiteralPath notes/slogan.txt -Value 'Wrong slogan: random commands solve everything.' -Encoding ascii
git add notes/slogan.txt
git commit -m "Add incorrect project slogan"
$badCommit = git rev-parse HEAD
git revert --no-edit $badCommit
git log --oneline -3
git status
```

Expected: unstaging preserved the log edit; its subsequent working-tree restore discarded it. The slogan commit and a new inverse commit both remain in history. The slogan file is gone; greeting is the resolved version.

<!-- verify: reset -->
```powershell
git switch -c lab/reset
Set-Content -LiteralPath notes/reset-demo.md -Value 'Private reset practice.' -Encoding ascii
git add notes/reset-demo.md
git commit -m "Add private reset note"
git branch backup/reset
git reset --soft 'HEAD~1'
git status
git diff --staged
git reset --mixed HEAD
git status
Get-Content -LiteralPath notes/reset-demo.md
git add notes/reset-demo.md
git commit -m "Keep reset practice result"
git switch main
git merge --ff-only lab/reset
git branch -d lab/reset
```

Soft reset moved lab/reset to its parent, leaving a staged addition. Mixed reset did **not** rewind again; it made the index match the same current HEAD, so the new note became untracked. Recommitting gave main a retained practice result. Backup/reset still reaches the original commit, which is a different object with a different message.

### Milestone 5 — Replace a private tip, pause work, and rescue a tip

<!-- verify: amend -->
```powershell
Set-Content -LiteralPath notes/amend.md -Value 'A focused change.' -Encoding ascii
git add notes/amend.md
git commit -m "Add amend ntoe"
$beforeAmend = git rev-parse HEAD
Set-Content -LiteralPath notes/amend-extra.md -Value 'This belongs to the same change.' -Encoding ascii
git add notes/amend-extra.md
git diff --staged
git commit --amend -m "Add amend notes"
$afterAmend = git rev-parse HEAD
$beforeAmend -eq $afterAmend
git status
```

Expected comparison: False. The replacement commit contains both files; the old object was not edited. This branch has not been pushed.

<!-- verify: stash -->
```powershell
Add-Content -LiteralPath notes/learning-log.md -Value 'Unfinished tracked note.' -Encoding ascii
Set-Content -LiteralPath notes/wip.txt -Value 'Unfinished new note.' -Encoding ascii
git stash push -u -m "Unfinished learning notes"
git stash list
git status
git switch -c lab/stash-break
git switch main
git stash apply 'stash@{0}'
git status
Get-Content -LiteralPath notes/wip.txt
git stash list
# Content has been checked; remove the retained saved entry deliberately.
git stash drop 'stash@{0}'
git add notes/learning-log.md notes/wip.txt
git commit -m "Record restored notes"
Add-Content -LiteralPath notes/learning-log.md -Value 'Second stash cycle.' -Encoding ascii
git stash push -m "Second stash cycle"
git stash pop
git stash list
git add notes/learning-log.md
git commit -m "Record popped note"
git branch -d lab/stash-break
git status
```

Expected: first apply retains the stash; explicit drop follows inspection. The second successful pop restores its note and removes that entry. No stash entries remain from this lab. Starting from clean main made application straightforward.

<!-- verify: reflog -->
```powershell
git switch -c lab/reflog
Set-Content -LiteralPath notes/recovered.md -Value 'This commit can be found again.' -Encoding ascii
git add notes/recovered.md
git commit -m "Add recoverable note"
git reset --soft 'HEAD~1'
git reflog --oneline -6
# Immediately after this reset, identify the prior tip; confirm its message.
$lostCommit = git rev-parse 'HEAD@{1}'
git show --no-patch --oneline $lostCommit
git branch rescue/reflog $lostCommit
git switch rescue/reflog
git status
git switch main
git merge --ff-only rescue/reflog
git branch -d lab/reflog rescue/reflog
```

The soft reset preserved staged content while the local branch stopped reaching the commit. The rescued branch restores a name for that exact ID. Switching to the matching snapshot is clean; final main reaches it. If your reflog contains other intervening movements, use the hash actually associated with `Add recoverable note` instead of blindly taking entry 1.

### Milestone 6 — Selective replay and private rebase

<!-- verify: cherry -->
```powershell
git switch -c lab/cherry-source
Set-Content -LiteralPath notes/portable-tip.md -Value 'Inspect state before changing it.' -Encoding ascii
git add notes/portable-tip.md
git commit -m "Add portable Git tip"
$tipCommit = git rev-parse HEAD
git switch main
Set-Content -LiteralPath notes/cherry-context.md -Value 'The destination has its own commit.' -Encoding ascii
git add notes/cherry-context.md
git commit -m "Add destination context"
git cherry-pick $tipCommit
$pickedCommit = git rev-parse HEAD
$tipCommit -eq $pickedCommit
git show --no-patch --format='%h %p %s' $tipCommit
git show --no-patch --format='%h %p %s' $pickedCommit
```

Expected comparison: False. The introduced tip content is reused with different ancestry. Keep the source name; `-d` may correctly refuse to treat a cherry-picked original as merged.

<!-- verify: rebase -->
```powershell
git switch -c feature/rebase-demo
Set-Content -LiteralPath notes/rebase-feature.md -Value 'Private feature work.' -Encoding ascii
git add notes/rebase-feature.md
git commit -m "Add private rebase feature"
$beforeRebase = git rev-parse HEAD
git branch backup/rebase
git switch main
Set-Content -LiteralPath notes/rebase-main.md -Value 'Main advanced independently.' -Encoding ascii
git add notes/rebase-main.md
git commit -m "Advance main for rebase"
git log --oneline --graph --decorate --all
git switch feature/rebase-demo
git rebase main
$afterRebase = git rev-parse HEAD
$beforeRebase -eq $afterRebase
git show --no-patch --format='%h %p %s' HEAD
git switch main
git merge --ff-only feature/rebase-demo
git branch -d feature/rebase-demo
git status
```

```text
before:       C           main
             /
...---------B
             \
              F          feature, backup/rebase

after rebase and integration:
...---------B---C---F'    main
             \
              F          backup/rebase (old ID retained for inspection)
```

Expected comparison: False. The replayed feature's parent changed. Its old object is retained by backup/rebase. At the end main has the new base plus feature, and the working tree is clean. These were private operations before any hosting was added.

### Milestone 7 — Publish a branch, review, and integrate on GitHub

Tasks 23–28 add hosting. In your browser, create an empty repository under your account. Copy its URL; use GCM/SSH authentication as described in the theory. No credentials belong in the commands below.

<!-- verify: publish -->
```powershell
$repoUrl = Read-Host 'Paste the repository HTTPS or SSH URL (no embedded token)'
git remote add origin "$repoUrl"
git remote -v
git push -u origin main
git branch -vv
git switch -c feature/github-profile
Set-Content -LiteralPath notes/github-profile.md -Value 'This feature will be reviewed through a Pull Request.' -Encoding ascii
git add notes/github-profile.md
git commit -m "Add GitHub workflow profile"
git push -u origin feature/github-profile
```

Hosted main still lacks the new profile. Open the PR with base main and head feature/github-profile. Read the entire diff, add a useful comment if desired, and describe verification. There is no self-approval requirement. Merge using **Create a merge commit**, then optionally delete the hosted feature branch.

```text
after clicking Merge, before local synchronization:
local main:      B
local feature:   F
origin/main:     B   (potentially stale)
GitHub main:    M   (parents B and F)
```

<!-- verify: sync-pr -->
```powershell
git status
git branch -vv
git switch main
git pull --ff-only
git log --oneline --graph --decorate --all
git branch -d feature/github-profile
git fetch --prune origin
git status
```

Expected: main and origin/main now name M; profile is present locally. Local deletion removes the feature name; prune removes its remote-tracking ref only if the hosted branch was deleted.

### Milestone 8 — Isolate fetch from integration

In the GitHub browser, create `notes/browser-note.md` on main containing `Created through GitHub.`, with commit message `Add browser-side note`. The following is run in the original local clone, after the browser commit but before local fetching:

<!-- verify: browser-fetch -->
```powershell
$mainBeforeFetch = git rev-parse main
$trackingBeforeFetch = git rev-parse origin/main
git fetch origin
git rev-parse main
git rev-parse origin/main
git log --oneline main..origin/main
git show origin/main:notes/browser-note.md
Test-Path -LiteralPath notes/browser-note.md
git status
```

Expected: main still equals `$mainBeforeFetch`; origin/main changed from `$trackingBeforeFetch`; show prints the remote snapshot's note; Test-Path is False. Status reports clean files and a behind relationship. Fetch downloaded the object but did not check it out.

<!-- verify: browser-integrate -->
```powershell
git merge --ff-only origin/main
Test-Path -LiteralPath notes/browser-note.md
git status
```

Now Test-Path is True and main agrees with origin/main. You have observed the two stages that pull normally combines.

### Milestone 9 — Two clones and a rejected stale push

In the same PowerShell session, use the retained variables. If opening another terminal, redefine `$practiceRoot`, `$repoPath`, and `$repoUrl` to the actual values; do not assume a new terminal inherits variables.

<!-- verify: collaborator -->
```powershell
$collabPath = Join-Path $practiceRoot 'git-github-practice-collaborator'
if (Test-Path -LiteralPath $collabPath) { throw 'Choose a fresh clone directory.' }
git clone "$repoUrl" "$collabPath"
Set-Location -LiteralPath $collabPath
# Optional simulated authorship, separate from your account authentication:
git config --local user.name "Practice Collaborator"
git config --local user.email "practice-collaborator@example.invalid"
Set-Content -LiteralPath notes/collaborator-note.md -Value 'A second clone made this change.' -Encoding ascii
git add notes/collaborator-note.md
git commit -m "Add second-clone note"
git push origin main
Set-Location -LiteralPath $repoPath
git status
Test-Path -LiteralPath notes/collaborator-note.md
```

The example.invalid address is intentionally a fake simulated author, not a login. Test-Path is False in the original; the second clone's push did not modify this clone's files or refs.

<!-- verify: collaborator-fetch -->
```powershell
git fetch origin
git log --oneline --graph --decorate --all
git log --oneline main..origin/main
git merge --ff-only origin/main
git status
```

Now create independent commits after that common point:

<!-- verify: rejection -->
```powershell
Set-Location -LiteralPath $collabPath
git pull --ff-only
Set-Content -LiteralPath notes/remote-only.md -Value 'Keep this remote-side change.' -Encoding ascii
git add notes/remote-only.md
git commit -m "Add remote-side change"
git push origin main
Set-Location -LiteralPath $repoPath
Set-Content -LiteralPath notes/local-only.md -Value 'Keep this local-side change.' -Encoding ascii
git add notes/local-only.md
git commit -m "Add local-side change"
git push origin main
# EXPECTED nonzero exit: rejected non-fast-forward/fetch-first push.
```

The original's tip is not a descendant of the remote's latest tip. Diagnose and integrate:

<!-- verify: reconcile -->
```powershell
git status
git fetch origin
git branch -vv
git log --oneline --graph --decorate --all
git log --oneline origin/main..main
git log --oneline main..origin/main
git merge --no-edit origin/main
Get-Content -LiteralPath notes/local-only.md
Get-Content -LiteralPath notes/remote-only.md
git show --no-patch --format='%h %p %s' HEAD
git push origin main
git status
```

```text
before reconciliation:     L  original main
                          /
                     ...-B
                          \
                           R  GitHub main (origin/main after fetch)

after ordinary push: ...-B---L---M  main, origin/main, GitHub main
                          \     /
                           R---/
```

Both files and histories survived. The second clone's main may now be behind; that is expected until it fetches/integrates. No required task uses force push.

### Milestone 10 — Link an Issue, label a version, and publish notes

Create the Issue in GitHub and record its actual number. Then:

<!-- verify: issue-branch -->
```powershell
git switch main
git pull --ff-only
git switch -c docs/readme-usage
Add-Content -LiteralPath README.md -Value @('','## Usage','Use this disposable repository to practice Git state transitions.') -Encoding ascii
git diff
git add README.md
git diff --staged
git commit -m "Document practice usage"
git push -u origin docs/readme-usage
```

Open/review a PR to main, with `Closes #` followed by the **actual** Issue number in its description. Merge using a merge commit. Check the linked Issue's closed state; optionally delete the hosted branch.

<!-- verify: tag -->
```powershell
git switch main
git pull --ff-only
git branch -d docs/readme-usage
git fetch --prune origin
git status
git tag -a v1.0.0 -m "Complete Git and GitHub practice version"
git show v1.0.0
git push origin v1.0.0
git rev-list -n 1 v1.0.0
git rev-parse main
```

The final two commit IDs match. In GitHub Releases, select the existing v1.0.0 tag and publish your practice notes. Publishing this Release creates no additional local commit.

### Expected final state

| Location | Expected core result |
|---|---|
| original main | contains all integrated core notes and README usage; clean index/worktree |
| origin/main in original | agrees with hosted main after final synchronization |
| GitHub main | contains reviewed merge commits and both sides of task 32 |
| retained local names | backup/reset, backup/rebase, lab/cherry-source can remain for comparison |
| ignored local content | fake local.env and build/output.txt remain untracked/ignored in original |
| second clone | may be behind after the final original pushes; update deliberately if desired |
| stash | no entries left by completed core stash cycles |
| version label | annotated v1.0.0 resolves to verified main tip |
| hosted collaboration | merged PRs, linked/closed Issue, published Release |

GitHub has no copy of your reflog or ordinary stash merely because main was pushed. Local backup branch names were not published. Core final tracked files should include the following; notes/slogan.txt should be absent because it was reverted:

```text
.gitignore
README.md
src/greeting.txt
notes/
  learning-log.md          profile.md             branch-guide.md
  main-note.md             reset-demo.md          amend.md
  amend-extra.md           wip.txt                recovered.md
  cherry-context.md        portable-tip.md        rebase-main.md
  rebase-feature.md        github-profile.md      browser-note.md
  collaborator-note.md     local-only.md          remote-only.md
```

Use `git ls-files`, status, the graph, branch tracking, and the hosted UI together to establish this result. No single green message establishes every part.

## Troubleshooting

Diagnose before modifying. Start with the smallest relevant observations:

```powershell
Get-Location
git status
git diff
git diff --staged
git log --oneline --graph --decorate --all
git branch -vv
git remote -v
```

If a commit seems missing, add `git reflog --oneline`. If remote knowledge matters, fetch deliberately; remember it updates that knowledge even when a later integration refuses. The remedies below depend on what the diagnosis establishes, not merely the error's wording.

### “Not a git repository”

Check your current directory and `Get-ChildItem -Force`. You may be in the parent or the other clone. Enter the intended repository with `Set-Location -LiteralPath $repoPath`, then inspect status. During task 2 the parent check is supposed to fail. Do not run init in arbitrary folders to suppress this message; that can create an accidental nested or unrelated repository.

### “Nothing to commit”

Read status and both diffs. Is the file saved? Are you in the right clone? Was the intended edit staged? Is it ignored or untracked? Commit records the index, not an editor's unsaved buffer. If everything already matches HEAD, there may be nothing to record. Do not create an empty commit to satisfy an expected exercise count without understanding why it differs.

### Untracked file confusion

Use `git status --short` and `git ls-files -- path`. An untracked file is not yet in the index. Ordinary diff will normally omit its contents; inspect it with VS Code/Get-Content, then add it deliberately to inspect the staged addition. After mixed reset of a newly added file, becoming untracked is expected. Do not mistake that state for a deleted file.

### Accidentally staged a file

Inspect `git diff --staged -- path` and `git status`. If you only want to remove staged intent and HEAD exists, use `git restore --staged -- path`. The working edit remains. If you instead want to discard the edit, inspect the remaining working diff and choose restore separately. These two intentions require different destinations; do not combine them reflexively.

### Wrong last commit message

Inspect `git log -1` and whether the commit has been pushed or used by others. For an unpublished tip, `git commit --amend -m "Correct message"` creates a replacement; inspect staged content first because amend can include it too. For shared work, leaving a harmless message typo or adding an appropriate follow-up is often preferable to a disruptive rewrite. Do not force push just to polish a shared message.

### Forgotten file in the last commit

Check the file, the staged diff, and the scope of the latest commit. If it belongs to that same **unpublished** change, stage it and use `git commit --amend --no-edit`. If shared, or logically separate, create a follow-up commit. Verify the resulting snapshot rather than relying only on the message. Amend can absorb every currently staged path, including unrelated mistakes.

### Merge conflict

Read status to identify the operation and unmerged paths. Inspect the base/current/incoming intentions and the full file. Edit the intended final content, remove markers, verify it, add only resolved files, and finish the merge. A textually clean result can still be wrong. In the deliberate greeting task, do not run pull or rebase to make the conflict disappear; those introduce another decision before the current one is settled.

### “Merge in progress” after editing

Status distinguishes unmerged paths from “all conflicts fixed but still merging.” Editing alone is insufficient; add the resolved files. Staging alone also leaves the merge in progress; finish with commit or `merge --continue`. If you want to abandon the still-running operation, inspect saved work and use `merge --abort`. Abort is not an undo command for a merge you already committed.

### “Rebase in progress”

Read status and the current replay step. Resolve/add the intended files, then `git rebase --continue`, or abandon with `git rebase --abort`. An editor may open for a commit message; save/close the message file appropriately. Do not make a standalone ordinary commit to imitate merge completion, and do not use `--skip` unless you intentionally mean to drop that patch. Default replay labels can make ours/theirs differ from your branch-name intuition.

### Detached HEAD

Inspect status and `git log -1`. You may have selected a commit or remote-tracking ref for inspection. If you have useful work at the current tip, `git switch -c rescue-work` names it before switching away. If you already left, locate the commit in reflog and create a rescue branch at its actual hash. If you only inspected content, switch back to your intended local branch. Detached HEAD is a valid state, not repository corruption.

### Branch cannot be deleted

Inspect current branch, linked worktrees if any, `branch -vv`, and the branch's unique history (`git log main..branch-name`). You cannot delete the checked-out branch. A -d refusal may protect unmerged commits or reflect upstream reachability; cherry-pick/squash/rebase can integrate equivalent changes without preserving the original ancestry. Retain a backup name and verify the actual result before considering forced deletion. No core lab cleanup needs `-D` for the retained comparison branches.

### Push rejected

Read the complete reason. Inspect `remote -v` and `branch -vv`, fetch, then compare `origin/main..main` and `main..origin/main`. For task 32's ancestry rejection, merge the fetched tip into original main, verify both files, and push normally. A permission failure, protected branch, or wrong URL requires another remedy. Unconditional force bypasses the safeguard rather than answering why it fired.

### “Remote origin already exists”

Run `git remote -v` and `git remote get-url origin`. The desired remote may already be configured. If correct, use it without adding again. If wrong, deliberately correct it with `git remote set-url origin <CORRECT_URL>`. Removing/re-adding blindly can obscure useful fetch configuration and still will not fix authentication.

### Wrong remote URL

Inspect fetch and push URLs with `remote -v`; compare owner/repository with the GitHub page's copied URL. Check spelling, renamed repository, protocol, and whether you meant your fork or the original. Set the intended URL, then fetch to verify access. Never put a token in the URL to bypass a credential prompt. Existing local commits do not vanish when you correct remote configuration.

### Authentication failure or “repository not found”

Confirm the exact URL and that your intended account has access. A private repository can appear absent when the account lacks permission. For HTTPS inspect the configured credential helper and cached account; sign in using GCM or a suitable token. For SSH inspect the key/registered public key and test the connection using official setup guidance. Account password, commit name, and commit email do not authenticate Git pushes. Organization SSO and token permissions may also matter. See [GitHub's Windows credential guidance](https://docs.github.com/en/get-started/git-basics/caching-your-github-credentials-in-git?platform=windows).

### Local branch behind remote

After fetching, inspect the graph and ensure no unique local commit needs preservation. If it is strictly behind and clean, `git pull --ff-only` or a deliberate `git merge --ff-only origin/main` on main should advance it. Before fetch, the reported ahead/behind values can be stale. Behind is a reference relationship, not a statement that your editor is broken.

### Diverged branches or “Not possible to fast-forward”

Fetch, then inspect both unique ranges and why each line exists. A fast-forward-only refusal is a useful guard. Merge to preserve both histories, or replay **private** local work onto the fetched base if that matches policy. The core rejection exercise uses merge. Do not change global pull defaults merely to hide the immediate warning. If the histories have independent roots because you initialized both ends separately, stop and decide whether to clone the intended authoritative history rather than applying unrelated-history options blindly.

### Accidental reset

Avoid further resets. Inspect status, diffs, graph, and reflog. Locate the former tip, inspect it, and create `rescue-before-reset` at its actual ID. Naming it does not overwrite current files. Decide later whether to merge, cherry-pick, or switch to that recovered history. A hard reset may already have destroyed uncommitted working edits; reflog's prior commit record does not promise recovery of those edits.

### Lost commit

Search normal branches and `log --all` first, then reflog if a ref moved. Check whether you are in the wrong clone: reflogs are local. Create a branch at the found hash before further experimentation. If the commit was never recorded, or objects have expired/pruned, reflog may not help. A hosted branch/another clone or an independent backup may retain committed work, but ordinary fetch does not download someone else's reflog.

### Commit on the wrong branch

Inspect status and the graph; create a rescue branch at the useful tip first. If the intended branch lacks the change, cherry-pick the specific commit onto it or use the preserved branch appropriately. Removing it from the wrong branch is a separate decision: a private tip may be reset after preserving it and checking uncommitted work; shared history normally needs a revert/correction. Do not blindly reset one parent if later valid commits or a merge are involved.

### Committed `.env` or another real credential

Inspect whether the file is tracked and what commits contain it, without pasting sensitive content into a chat/log. Revoke/rotate the credential first. Add proper exclusions and remove current tracking as appropriate; understand that a deletion/revert does not remove old versions. Coordinate any necessary history cleanup with maintainers and follow [GitHub's sensitive-data removal procedure](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository). The lab only uses fake local.env content, so it should never require a real credential incident response.

### GitHub says the PR branch has conflicts

Check the PR's base/head, fetch the latest refs, and inspect the graph. On the feature branch, merge the updated base according to team policy, or rebase only when you understand the rewrite and sharing constraints. Resolve/test, then push the updated head; the PR refreshes. Do not assume a browser conflict editor can express every resolution, or force push a shared feature as an automatic response. Core tasks use an uncomplicated PR after practicing local conflicts.

### Pull Request targets the wrong base

Read base and compare names at the top of the PR and inspect the resulting diff. Use GitHub's base-branch edit control to select the intended receiving branch, then recheck commits and Files changed; changing base can change the comparison and review context. If the local feature started from unrelated history, merely changing the displayed base may not repair the branch. Diagnose ancestry before adding more commits.

### Temptation to force push

Write down the actual problem first: stale knowledge, missing remote commits, permission failure, required PR, or intentional coordinated rewrite. None of the first four is solved safely by bypassing ancestry. Use fetch/graph inspection and ordinary integration. Only the optional isolated lease experiment deliberately replaces its remote branch; even a lease does not guarantee that everyone wanted the rewrite.

### Stash application conflicts

Inspect status, the affected files, and stash list. A failed/conflicting pop often retains its entry, so do not pop repeatedly. Resolve the content you want and verify it before discarding the saved entry. Stash application has no general `stash --abort` equivalent; careless reset/clean can destroy unrelated work. Starting from a clean destination and preferring apply with its retained copy makes learning safer.

### Status, hashes, or file changes differ from the walkthrough

Use the invariant rather than exact output text. Extra commits, different merge strategy, replayed instructions, inherited Git configuration, or an editor's line-ending conversion may explain a difference. Check current branch, parents, staged diff, and actual file content. An exercise command with a TODO is not a finished recipe. Never “repair” a cosmetic mismatch by deleting useful history or changing global settings without a reason.

## Completion checklist

### Local understanding and evidence

- [ ] I can explain Git vs GitHub without calling them interchangeable.
- [ ] I can locate my working tree, index, and local repository.
- [ ] I can predict both diffs after staging and editing again.
- [ ] I can create a coherent commit and inspect its snapshot/history.
- [ ] I can explain HEAD, parent notation, and branch pointer movement.
- [ ] I can create/switch branches and read a split/join in a graph.
- [ ] I merged into the intended current target.
- [ ] I observed a fast-forward and a two-parent merge.
- [ ] I created and manually resolved the deliberate greeting conflict.
- [ ] I know when to complete an operation and when an abort applies.
- [ ] I verified ignored paths and understand why ignore rules do not untrack history.

### Recovery decisions

- [ ] I distinguished unstaging from discarding working content with restore.
- [ ] I kept the bad slogan commit plus its revert in history.
- [ ] I compared soft and mixed reset using unpublished disposable content.
- [ ] I know why hard reset is dangerous and did not need it for routine undo.
- [ ] I observed a replacement ID after amend and kept its scope coherent.
- [ ] I saved/restored tracked and untracked work with stash.
- [ ] I compared apply and pop and checked stash retention.
- [ ] I located a lost local tip with reflog and named it safely.
- [ ] I cherry-picked a focused change onto a different parent.
- [ ] I rebased private work and explained recreated commit identities.
- [ ] I can explain why shared history changes my undo/rewrite decision.

### Collaboration and hosting

- [ ] I know origin is a remote name and upstream has two distinct meanings.
- [ ] I can distinguish main, origin/main, and GitHub main.
- [ ] I understand fetch vs pull, including pull's integration policy.
- [ ] I know what push sends and why uncommitted files are excluded.
- [ ] I can explain clone vs fork without requiring a fork for normal write access.
- [ ] I authenticated without storing a credential in the practice repository.
- [ ] I pushed a feature and opened a PR with the correct base/head.
- [ ] I performed a useful self-review without expecting self-approval.
- [ ] I deliberately chose a merge strategy and inspected its history.
- [ ] I synchronized local main after a hosted merge.
- [ ] I observed fetch-only state before integration.
- [ ] I used a second clone and observed independent refs/files.
- [ ] I caused a normal push rejection and preserved both sides without force.
- [ ] I linked a work Issue with its implementation PR.
- [ ] I created/pushed an annotated tag and used it for a Release.
- [ ] I understand why committing or later ignoring a secret does not undo exposure.
- [ ] I can complete an ordinary feature/PR workflow independently.

## Readiness reflection

Choose a random operation from the lab and answer: What did it read? What did it write? Which reference moved? Did it contact a remote? Did it recreate commits? Would the decision change if someone else already relied on the history?

Repeat one local milestone from memory in a **new** disposable directory, then compare actual state with your predictions. Explain task 32 to an imaginary teammate without naming any force option. If you can preserve both histories and justify each step, you are reasoning about Git rather than guessing commands.

The repositories are safe to delete after you no longer need the practice history, but deletion is optional. Before any cleanup, inspect the full resolved directory and confirm it is the disposable lab, not an existing project. Deleting a local clone does not delete the hosted repository, and deleting hosting does not automatically delete your local clone. No cleanup command is necessary to complete the lesson.

## Final mental model

```text
EDITING

file -> working tree -> git add -> staging area -> git commit -> local history

BRANCHING

branch -> movable name pointing to a commit
HEAD   -> what is checked out, normally through the current branch

COLLABORATION

local feature -> push -> GitHub feature -> Pull Request -> review
                                                        |
                                                        v
local main <- deliberate integration <- fetch <- GitHub main after merge

WHEN CONFUSED

1. Stop and identify the repository and branch.
2. Do not enter random destructive commands.
3. git status
4. git log --oneline --graph --decorate --all
5. git diff; git diff --staged
6. git branch -vv; git remote -v when relationships matter
7. git reflog if recorded history seems lost; name a rescue tip first
8. Understand the current state before choosing the next transformation.
```

**What state am I in? What state do I want? Which operation gets there while preserving useful work and the shared history people rely on?**
