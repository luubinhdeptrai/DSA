# Git & GitHub — Practical Command Reference

> **Goal:** Learn the commands you will actually use, understand what each changes, and avoid memorizing obscure syntax too early.  
> **Environment:** Windows + PowerShell + VS Code.  
> **Scope:** Core Git commands, common team workflows, and an **optional** section for the separate GitHub CLI (`gh`).  
> **Study basis:** `Git_GitHub_Essential_Concepts.md` and `Git_GitHub_Mini_Project.md`; the `gh` CLI reference and a few additional productivity commands are supplemental.

## How to use this guide

- **P1 — MUST KNOW:** Learn and practice without looking up the syntax. These are the daily commands.
- **P2 — IMPORTANT:** Understand and practice in a disposable repository; look up syntax when needed.
- **P3 — NICE TO KNOW / ADVANCED:** Recognize the purpose and risks. Don't memorize yet.
- **⚠️** Means the command may discard work, change history, delete a reference, or require careful context.
- Command patterns use placeholders such as `<COMMIT_ID>`, `<BRANCH>`, and `<URL>`. **Replace these with actual values**; do not type the angle brackets literally in PowerShell.
- Examples are **independent demonstrations**, not one script to run top to bottom. Creating the same branch/tag twice, or executing successive resets, will give different results or errors.

### Fast mental model

```text
Edit files (Working Tree)
        │ git add
        ▼
Staging Area (Index)
        │ git commit
        ▼
Local repository history ── git push ──► GitHub remote repository
        ▲                                     │
        │                 git fetch           │
        └─────────────────────────────────────┘

Git pull = fetch + integrate (fast-forward / merge / rebase by policy)
GitHub Pull Request = propose and review integration; NOT the git pull command
```

### Your first 25 commands to master

These form the core of everyday work. Learn them **before** the long reference tables:

| Group | Focus commands |
|---|---|
| Diagnose | `git status`, `git status --short`, `git diff`, `git diff --staged`, `git log --oneline --graph --decorate --all` |
| Save changes | `git add <FILE>`, `git add .`, `git commit -m "..."` |
| Branching | `git branch`, `git switch <BRANCH>`, `git switch -c <BRANCH>`, `git merge <BRANCH>` |
| Undo locally | `git restore <FILE>`, `git restore --staged <FILE>`, `git revert <COMMIT_ID>` |
| Remote work | `git remote -v`, `git fetch origin`, `git pull --ff-only`, `git push origin <BRANCH>`, `git push -u origin <BRANCH>`, `git branch -vv` |
| Collaboration | `git diff main origin/main`, `git log main..origin/main --oneline`, `git merge --abort`, `git tag`, `git stash push -u -m "..."` |

**Priority-based advice:** It's more useful to predict the effect of 25 commands accurately than to memorize 100 commands without understanding them.

---

## 1. Command syntax: how to read Git commands

General pattern:

```text
git <SUBCOMMAND> [OPTIONS] [POSITIONAL_ARGUMENTS] [-- <PATHS>]
```

| Syntax | Meaning | Example | Important note |
|---|---|---|---|
| `git` | Program | `git status` | Calls the Git CLI. |
| `status`, `commit`, `push` | Subcommand / action | `git commit` | Selects what operation to perform. |
| `-m` | Short option | `git commit -m "Fix bug"` | In `git commit`, `-m` = `--message`. **Options are command-specific**. |
| `--message` | Long option | `git commit --message "Fix bug"` | Usually easier to read and remember. |
| `--option=value` | Long option with a value | `git branch --set-upstream-to=origin/main main` | Some options take values with `=`; others take the next argument. |
| `-u origin` | Short option and following argument | `git push -u origin main` | Here `-u` = `--set-upstream`; `origin` is a positional remote argument. |
| `--` | Option/path separator | `git restore -- README.md` | Makes subsequent arguments file paths in commands supporting path separation. |
| `HEAD` | Current checkout reference | `git show HEAD` | Normally points through the current local branch to a commit. |
| `HEAD~1` | First-parent traversal | `git show 'HEAD~1'` | Goes back one first-parent link; not a reflog reference. |
| `HEAD^2` | Second parent of a merge commit | `git show 'HEAD^2'` | Only exists for commits with a second parent. |
| `HEAD@{1}` | Previous HEAD reflog entry | `git show 'HEAD@{1}'` | **Not necessarily the parent commit.** Quote it in PowerShell. |
| `main..feature` | Commit set: reachable from feature but not main | `git log main..feature --oneline` | Two-dot **log** meaning differs from some **diff** notation. |
| `main...feature` with `diff` | Compare merge base with feature tip | `git diff main...feature` | Common way to inspect a feature's proposed changes. |
| `.` | Current directory pathspec | `git add .` | Stages eligible changes under the **current directory**, not always the entire repository. |

**Abbreviation reminder:** The same letter can have different meanings for different subcommands. For example, `git commit -m` = **message**, but `git branch -m` = **move/rename**.

---

## 2. Install, initialize, clone, and configuration

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P1 | `git --version` | Verify installation | `git --version` | `--version` prints the installed version. |
| P1 | `git init -b <BRANCH>` | Initialize Git in a folder | `git init -b main` | `-b` = `--initial-branch`; no remote is created. |
| P1 | `git clone <URL>` | Create a local copy of a remote repository | `git clone https://github.com/OWNER/REPO.git` | Typically creates a local folder and remote `origin`. |
| P2 | `git clone <URL> <DIRECTORY>` | Clone into a chosen folder | `git clone https://github.com/OWNER/REPO.git my-copy` | Uses `my-copy` as destination. |
| P1 | `git config --global user.name <NAME>` | Set default commit author name | `git config --global user.name "Alex Nguyen"` | `--global` = user-level setting, not GitHub login. |
| P1 | `git config --global user.email <EMAIL>` | Set default commit author email | `git config --global user.email "alex@example.com"` | Can use GitHub's no-reply address. |
| P2 | `git config --local user.name <NAME>` | Override name for current repo | `git config --local user.name "Team Alias"` | Run inside a repository; `--local` overrides global. |
| P2 | `git config --get <KEY>` | Inspect a specific setting | `git config --get user.email` | `--get` = retrieve one configuration value. |
| P2 | `git config --show-origin --get <KEY>` | Find where a setting comes from | `git config --show-origin --get user.name` | `--show-origin` = display configuration source. |
| P2 | `git config --list` | See effective configuration entries | `git config --list` | May reveal sensitive URLs; review before sharing output. |
| P2 | `git rev-parse --show-toplevel` | Find repository root | `git rev-parse --show-toplevel` | `--show-toplevel` = print the root of the working tree. |
| P3 | `git config --global core.editor <CMD>` | Set Git's message editor | `git config --global core.editor "code --wait"` | `--wait` makes the VS Code launcher wait for editing to finish. |

**Remember:** `git config user.name` and `user.email` describe commit metadata; they do **not** authenticate you to GitHub.

---

## 3. Working Tree, Staging Area, and commits

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P1 | `git status` | See current branch, staged/unstaged/untracked paths | `git status` | Safest first diagnostic. |
| P1 | `git status --short` | Get compact file-status output | `git status --short` | `--short` = concise format. `??` untracked, `M ` staged, ` M` unstaged, `MM` both. |
| P1 | `git add <FILE>` | Stage the current content of selected path | `git add README.md` | Staging snapshots current file content; later edits remain unstaged until re-added. |
| P1 | `git add .` | Stage eligible changes under current directory | `git add .` | Inspect with `git diff --staged`; ignored files normally excluded. |
| P2 | `git add -A` | Stage additions, modifications, deletions across working tree | `git add -A` | `-A` = `--all`; repository-wide when no path given. |
| P2 | `git add -p <FILE>` | Interactively choose change hunks | `git add -p README.md` | `-p` = `--patch`; useful for focused commits. |
| P1 | `git diff` | Inspect **unstaged** tracked edits | `git diff` | Compares Index → Working Tree. |
| P1 | `git diff --staged` | Inspect **staged** changes | `git diff --staged` | Compares HEAD → Index; also known as `--cached`. |
| P2 | `git diff HEAD` | Inspect total tracked changes since HEAD | `git diff HEAD` | Compares HEAD → Working Tree; ordinary untracked files don't show. |
| P1 | `git commit -m <MESSAGE>` | Record staged snapshot locally | `git commit -m "Add user registration"` | `-m` = `--message`; does not push. |
| P2 | `git commit -am <MESSAGE>` | Stage tracked file modifications/deletions and commit | `git commit -am "Fix typo"` | `-a` = `--all`; `-m` = `--message`. **Does not include new untracked files.** |
| P2 | `git commit --amend -m <MESSAGE>` ⚠️ | Replace latest commit with updated message/snapshot | `git commit --amend -m "Fix registration validation"` | `--amend` rewrites latest commit ID; prefer on **unpublished** commits. |
| P2 | `git commit --amend --no-edit` ⚠️ | Add staged changes into replacement latest commit | `git commit --amend --no-edit` | `--no-edit` retains existing message; staged changes all included. |
| P2 | `git status --ignored --short` | Show ignored files too | `git status --ignored --short` | `--ignored` adds ignored paths; `!!` indicates ignored. |

**Stages are not commits:** `git add` only proposes content for the **next** commit. `git commit` records the staged snapshot. `git push` sends existing **committed history**, not staged-only changes.

---

## 4. Inspect history, commits, and differences

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P1 | `git log --oneline` | See condensed commit history | `git log --oneline` | `--oneline` = compact hash + subject. |
| P1 | `git log --oneline --graph --decorate --all` | Visualize all locally known branch histories | `git log --oneline --graph --decorate --all` | `--graph` topology; `--decorate` branch/tag names; `--all` refs. |
| P2 | `git log -5 --oneline` | See latest five commits | `git log -5 --oneline` | `-5` = limit count to five. |
| P2 | `git log -1 --oneline` | Inspect newest commit | `git log -1 --oneline` | `-1` = one commit. |
| P1 | `git show <COMMIT>` | Inspect a commit and its patch | `git show HEAD` | `HEAD` usually means current branch's tip. |
| P2 | `git show <REV>:<PATH>` | Print one file from a recorded version | `git show HEAD:README.md` | Useful without switching branches. |
| P2 | `git diff <REF1> <REF2>` | Compare two tip snapshots | `git diff main origin/main` | Reports content differences, not a commit list. |
| P1 | `git log main..origin/main --oneline` | List commits the known remote has that local main lacks | `git log main..origin/main --oneline` | Direction matters: right side minus left side. |
| P2 | `git log origin/main..main --oneline` | List local commits not in known remote history | `git log origin/main..main --oneline` | Useful before pushing. |
| P2 | `git diff main...feature/login` | View feature changes since the merge base | `git diff main...feature/login` | For `diff`, three dots use merge base → feature. |
| P2 | `git diff --name-only` | List paths with unstaged tracked changes | `git diff --name-only` | `--name-only` = filenames only. |
| P2 | `git blame <FILE>` | Identify commit responsible for each recorded line | `git blame README.md` | Investigate historical context, not assign fault. |
| P2 | `git branch --show-current` | Print checked-out branch name | `git branch --show-current` | Empty when HEAD is detached. |
| P2 | `git rev-parse --short HEAD` | Print abbreviated current commit ID | `git rev-parse --short HEAD` | `--short` = abbreviated hash. |
| P3 | `git show 'HEAD~1'` | Inspect previous first-parent commit | `git show 'HEAD~1'` | Parent notation; PowerShell quoting is safe. |
| P3 | `git show 'HEAD^2'` | Inspect a merge commit's second parent | `git show 'HEAD^2'` | Only valid if the commit has a second parent. |

**Comparison rule:** `git diff` is primarily about **file snapshots**; `git log A..B` is about **commits reachable from B but not A**. These answer different questions.

---

## 5. Branch creation, switching, and deletion

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P1 | `git branch` | List local branches | `git branch` | `*` marks current branch. |
| P2 | `git branch -a` | List local and remote-tracking refs | `git branch -a` | `-a` = `--all`. |
| P2 | `git branch -r` | List remote-tracking refs | `git branch -r` | `-r` = `--remotes`. |
| P1 | `git branch <NAME>` | Create local branch without switching | `git branch feature/profile` | Creates reference at current commit. |
| P1 | `git switch <NAME>` | Switch to existing local branch | `git switch main` | HEAD now refers to selected branch; may modify checked-out files. |
| P1 | `git switch -c <NAME>` | Create and switch in one step | `git switch -c feature/login` | `-c` = `--create`. |
| P2 | `git switch -` | Switch back to previously checked-out branch | `git switch -` | `-` is previous checkout shorthand, not an option expansion. |
| P1 | `git branch -vv` | View branches with upstream and ahead/behind info | `git branch -vv` | `-vv` = more verbose, includes upstream. Comparison may be stale until fetch. |
| P2 | `git branch -m <OLD> <NEW>` | Rename a branch | `git branch -m feature/logn feature/login` | `-m` = `--move` **for branch**, not message. |
| P2 | `git branch -d <NAME>` ⚠️ | Safely delete local branch ref | `git branch -d feature/login` | `-d` = `--delete`; refuses if integration status doesn't meet safety check. |
| P3 | `git branch -D <NAME>` ⚠️ | Force-delete local branch | `git branch -D feature/login` | `-D` = `--delete --force`. Verify history and retention first. |
| P3 | `git switch --detach <REF>` | Inspect commit/tag without selecting branch | `git switch --detach v1.0.0` | `--detach` = detached HEAD; create a branch before lasting work. |
| P3 | `git checkout -b <NAME>` | Older create-and-switch syntax | `git checkout -b feature/login` | `-b` = create a **branch** (`checkout` old syntax). Prefer `switch -c`. |

**Key idea:** A branch is a **movable name** for a commit, not a duplicate project folder. Ordinary commits advance the currently checked-out branch.

---

## 6. Merging and resolving conflicts

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P1 | `git merge <SOURCE>` | Integrate source history **into current branch** | `git switch main` then `git merge feature/login` | Current branch is target; argument is source. |
| P1 | `git merge --ff-only <SOURCE>` | Integrate only if fast-forward possible | `git merge --ff-only origin/main` | `--ff-only` = fast-forward only; refuses divergent integration. |
| P2 | `git merge --no-ff <SOURCE>` | Create merge commit even if FF possible | `git merge --no-ff feature/login` | `--no-ff` = no fast-forward; preserves explicit integration. |
| P1 | `git status` | Check merge progress and unmerged files | `git status` | Do this after a conflict before acting. |
| P1 | `git diff --name-only --diff-filter=U` | List currently unmerged paths | `git diff --name-only --diff-filter=U` | `--name-only` = paths; `U` = **Unmerged**. |
| P1 | `git add <RESOLVED_FILE>` | Mark path resolved with chosen content | `git add src/greeting.txt` | Does **not** finish the merge. |
| P1 | `git merge --continue` | Finish a merge after resolving/staging conflicts | `git merge --continue` | `--continue` = resume merge; alternative to `git commit` for prepared merge. |
| P1 | `git merge --abort` ⚠️ | Abandon in-progress merge | `git merge --abort` | `--abort` = stop merge, attempt to restore pre-merge state. Not for completed merge commits. |
| P2 | `git commit -m <MESSAGE>` | Complete a prepared conflicted merge | `git commit -m "Resolve profile merge conflict"` | `-m` = `--message`; alternative to `merge --continue`. |

**Conflict markers:** `<<<<<<< HEAD` = current side, `=======` = separator, `>>>>>>> <BRANCH>` = incoming side. Determine the correct meaning, remove markers, run tests, stage, then finish.

---

## 7. Remotes, fetching, pulling, pushing, and upstream tracking

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P1 | `git remote -v` | List configured remote URLs | `git remote -v` | `-v` = `--verbose`. `(fetch)` / `(push)` labels describe URLs. |
| P1 | `git remote add origin <URL>` | Configure a remote alias | `git remote add origin https://github.com/OWNER/REPO.git` | **Does not upload** commits or files. |
| P2 | `git remote get-url origin` | See the URL for a remote | `git remote get-url origin` | `get-url` = retrieve configured URL. |
| P2 | `git remote set-url origin <URL>` | Fix/change the remote address | `git remote set-url origin https://github.com/OWNER/NEW.git` | Use instead of `remote add` if `origin` already exists. |
| P1 | `git fetch origin` | Download remote history; refresh remote-tracking refs | `git fetch origin` | Normal fetch doesn't integrate into current branch. |
| P2 | `git fetch --prune origin` | Remove stale remote-tracking refs | `git fetch --prune origin` | `--prune` = remove refs for branches removed from remote; not local branches. |
| P1 | `git pull --ff-only` | Fetch + fast-forward current branch if possible | `git pull --ff-only` | `--ff-only` = fast-forward only; requires suitable upstream or explicit arguments. |
| P2 | `git pull --ff-only origin main` | Pull from explicit remote/branch | `git pull --ff-only origin main` | Updates **current** branch, so verify it first. |
| P2 | `git pull --no-rebase` | Fetch + merge integration | `git pull --no-rebase` | `--no-rebase` = do not rebase; may create merge commit. |
| P2 | `git pull --rebase` ⚠️ | Fetch + replay local commits onto fetched upstream | `git pull --rebase` | `--rebase` = use rebase; rewrites replayed commit IDs. |
| P1 | `git push origin <BRANCH>` | Publish committed history to specified remote branch | `git push origin main` | Does **not** publish unstaged, staged-only, or stash changes. |
| P1 | `git push -u origin <BRANCH>` | First push + configure upstream tracking | `git push -u origin feature/profile` | `-u` = `--set-upstream`. |
| P1 | `git push` | Push using configured defaults | `git push` | Behavior depends on upstream and `push.default`; inspect if uncertain. |
| P2 | `git branch --set-upstream-to=origin/main main` | Configure upstream without pushing | `git branch --set-upstream-to=origin/main main` | Sets local `main`'s tracking counterpart. |
| P2 | `git push origin --delete <BRANCH>` ⚠️ | Delete a **remote** branch | `git push origin --delete feature/old` | `--delete` removes hosted branch ref if permitted; different from `git branch -d`. |
| P3 | `git remote add upstream <URL>` | Name an additional remote (fork workflow) | `git remote add upstream https://github.com/ORG/PROJECT.git` | `upstream` as remote **name** ≠ your branch's **upstream tracking** relationship. |
| P3 | `git fetch upstream` | Fetch from original project remote | `git fetch upstream` | Common in fork-based open-source workflows. |

### Three different things that people call “main”

| Reference | Stored where? | Meaning |
|---|---|---|
| `main` | Your local repository | Your local branch tip. |
| `origin/main` | Your local repository | Cached knowledge of remote `main` (updated by fetch and sometimes push). |
| `main` on GitHub | Remote repository | Actual hosted branch tip; can change when others push or merge. |

**Push rejection rule:** If GitHub has new commits and your local branch diverges, **do not immediately force-push**. Run `git fetch origin`, inspect the graph and both commit ranges, then integrate intentionally. Authentication and branch-protection rejections require different fixes.

---

## 8. Undo changes and recover commits (read warnings)

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P1 | `git restore <FILE>` ⚠️ | Discard unstaged edits to a tracked path | `git restore README.md` | Restores from Index by default. **Can lose uncommitted work.** |
| P1 | `git restore --staged <FILE>` | Unstage while keeping Working Tree edits | `git restore --staged README.md` | `--staged` = act on Index; HEAD must normally exist. |
| P2 | `git rm --cached -- <FILE>` ⚠️ | Stop tracking a file while keeping it on disk | `git rm --cached -- .env` | `rm` = remove; `--cached` = Index only; commit the removal. **Doesn't erase past commits.** |
| P2 | `git reset HEAD -- <FILE>` | Older idiom to unstage selected file | `git reset HEAD -- README.md` | Path-form reset does **not** move branch. Prefer `restore --staged`. |
| P2 | `git reset --soft 'HEAD~1'` ⚠️ | Move current branch to parent, keep staged snapshot | `git reset --soft 'HEAD~1'` | `--soft` = change ref only; keep Index/Working Tree. **Private history only.** |
| P2 | `git reset --mixed 'HEAD~1'` ⚠️ | Move branch, reset Index, keep Working Tree | `git reset --mixed 'HEAD~1'` | `--mixed` is default commit-reset mode; edits become unstaged. |
| P2 | `git reset --mixed HEAD` | Unstage all currently staged changes (HEAD unchanged) | `git reset --mixed HEAD` | Useful after `--soft`; **do not** accidentally reset `HEAD~1` a second time. |
| P3 | `git reset --hard <COMMIT>` ⚠️ | Reset branch, Index, and tracked Working Tree state | `git reset --hard <COMMIT_ID>` | `--hard` **can destroy uncommitted edits**; not part of recommended beginner practice. |
| P1 | `git revert <COMMIT>` | Create a new commit reversing an earlier commit | `git revert HEAD` | Usually safer for **shared** committed history; may conflict. |
| P2 | `git revert --no-edit HEAD` | Revert using generated commit message | `git revert --no-edit HEAD` | `--no-edit` = skip message editor. |
| P2 | `git reflog --oneline` | Inspect recorded local HEAD movements | `git reflog --oneline` | `reflog` = reference log; records branch switches, resets, rewrites, etc. |
| P2 | `git show 'HEAD@{1}'` | Inspect prior HEAD reflog position | `git show 'HEAD@{1}'` | Quote in PowerShell. Not the same as `HEAD~1`. |
| P2 | `git branch rescue-work <COMMIT>` | Preserve recovered commit through new branch | `git branch rescue-work a1b2c3d` | Replace hash with actual reflog candidate; doesn't change Working Tree. |
| P2 | `git branch backup-before-reset` | Keep a named reference before rewriting history | `git branch backup-before-reset` | Makes original tip reachable while you experiment. |
| P3 | `git clean -nd` | **Preview** deletion of untracked files | `git clean -nd` | `-n` = `--dry-run`; `-d` = include untracked directories. Never assume a later deleting command is safe. |

**Choose before you undo:** Unstaged → `restore`; staged → `restore --staged`; unpublished commits → possibly `reset`/`amend`; shared commit → normally `revert`. Reflog only helps with **recorded** local reference history, not arbitrary deleted editor edits.

---

## 9. `.gitignore`, ignored files, and secret prevention

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P1 | Edit `.gitignore` | Exclude matching **untracked** paths from normal tracking | Add `.env`, `target/`, `*.log` to `.gitignore` | Not a Git subcommand. Usually commit `.gitignore` itself. |
| P2 | `git check-ignore -v <PATH>` | Explain which ignore pattern matches a path | `git check-ignore -v local.env` | `-v` = `--verbose`; tracked paths need special consideration. |
| P2 | `git ls-files -- <PATH>` | Check whether a path is in the Index | `git ls-files -- .env` | `ls-files` = list indexed files; empty output often means not tracked. |
| P2 | `git status --ignored --short` | Display ignored paths | `git status --ignored --short` | `!!` indicates ignored paths. |
| P2 | `git rm --cached -- <PATH>` ⚠️ | Stage removal from tracking without deleting local file | `git rm --cached -- .env` | For already staged/tracked path; older commits still retain earlier contents. |

**Security:** Never commit real passwords, Personal Access Tokens (PATs), or private keys. If a secret was committed or pushed, **revoke/rotate it immediately**; later `.gitignore` rules and removal commits do not retroactively make old history confidential. Use safe `.env.example` templates.

---

## 10. Stash: set unfinished work aside

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P2 | `git stash push -m <MESSAGE>` | Stash tracked changes with a label | `git stash push -m "WIP registration"` | `-m` = `--message`; `WIP` = work in progress. |
| P2 | `git stash push -u -m <MESSAGE>` | Include untracked, non-ignored files too | `git stash push -u -m "Unfinished notes"` | `-u` = `--include-untracked`. |
| P2 | `git stash list` | List locally saved stashes | `git stash list` | Most recent usually `stash@{0}`. |
| P2 | `git stash show -p 'stash@{0}'` | Inspect saved tracked changes as patch | `git stash show -p 'stash@{0}'` | `-p` = `--patch`; untracked content may need additional options. |
| P2 | `git stash apply 'stash@{0}'` | Reapply a stash **without removing it** | `git stash apply 'stash@{0}'` | Check status afterward; applying again may duplicate/conflict. |
| P2 | `git stash pop 'stash@{0}'` | Apply and remove stash on success | `git stash pop 'stash@{0}'` | Keeps stash if conflict blocks successful application. |
| P2 | `git stash drop 'stash@{0}'` ⚠️ | Remove one saved stash without applying | `git stash drop 'stash@{0}'` | Verify recovered changes first. |
| P3 | `git stash apply --index 'stash@{0}'` | Attempt to restore saved staged/unstaged split | `git stash apply --index 'stash@{0}'` | `--index` = reinstate Index state; may fail. |
| P3 | `git stash push -a -m <MESSAGE>` ⚠️ | Stash tracked, untracked **and ignored** files | `git stash push -a -m "Full temporary save"` | `-a` = `--all`; can include secrets or huge build output. Avoid casually. |

**PowerShell note:** Quote refs such as `'stash@{0}'`. Stashes are local; normal `git push` does not publish them. If you created another branch or tag literally named `stash`, you may receive an ambiguous-ref warning; inspect `git show-ref stash` before retrying an apply.

---

## 11. Rebase, cherry-pick, and more advanced history operations

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P2 | `git rebase main` ⚠️ | Replay current feature branch's selected commits on `main` | `git switch feature/login` then `git rebase main` | Recreated commits normally get new IDs. Prefer private/unpublished work. |
| P2 | `git rebase --continue` | Continue after resolving and staging conflicts | `git rebase --continue` | `--continue` = resume. |
| P2 | `git rebase --abort` | Cancel unfinished rebase | `git rebase --abort` | `--abort` = return toward pre-rebase state. |
| P3 | `git rebase --skip` ⚠️ | Omit the commit currently being replayed | `git rebase --skip` | `--skip` = omit patch, potentially lose required change. |
| P3 | `git rebase -i 'HEAD~3'` ⚠️ | Interactively reorder/squash/edit recent private commits | `git rebase -i 'HEAD~3'` | `-i` = `--interactive`; rewrites commit IDs; advanced. |
| P2 | `git cherry-pick <COMMIT>` | Replay one commit's changes on current branch | `git cherry-pick a1b2c3d` | Typically creates new commit with a different parent and ID. |
| P2 | `git cherry-pick --continue` | Finish cherry-pick after conflict resolution | `git cherry-pick --continue` | Stage resolved files first. |
| P2 | `git cherry-pick --abort` | Cancel unfinished cherry-pick | `git cherry-pick --abort` | Doesn't undo a completed cherry-pick. |
| P3 | `git push --force-with-lease origin <BRANCH>` ⚠️ | Conditionally replace remote history if expected tip hasn't changed | `git push --force-with-lease origin feature/private` | **Advanced, coordinated use only**. Can still disrupt collaborators; never the default rejection fix. |

**Compare:** `merge` integrates a line of history; `cherry-pick` replays selected commit changes; `rebase` replays a sequence onto a new base. Avoid rewriting history others are already using.

---

## 12. Tags and version releases

| Priority | Command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P2 | `git tag` | List local tag names | `git tag` | Tags conventionally stay fixed at important versions. |
| P2 | `git tag <VERSION>` | Create lightweight tag at HEAD | `git tag v1.0.0` | Simple reference; alternative to annotated creation. |
| P2 | `git tag -a <VERSION> -m <MESSAGE>` | Create annotated tag | `git tag -a v1.0.0 -m "Version 1.0.0"` | `-a` = `--annotate`; `-m` = `--message`. |
| P2 | `git show <TAG>` | Inspect tag/underlying commit | `git show v1.0.0` | Useful before releasing. |
| P2 | `git push origin <TAG>` | Publish a specific tag | `git push origin v1.0.0` | Branch push doesn't publish every local tag. |
| P3 | `git tag -d <TAG>` ⚠️ | Delete a **local** tag | `git tag -d v0.1.0` | `-d` = `--delete`; don't casually retarget published release tags. |
| P3 | `git push origin --delete refs/tags/<TAG>` ⚠️ | Delete a **remote** tag | `git push origin --delete refs/tags/v0.1.0` | Subject to remote rules; avoid for published versions. |

**Git tag vs GitHub Release:** A Git tag is a Git reference. A GitHub Release is a hosted page tied to a tag, with release notes and optional files. Neither automatically deploys your application.

---

## 13. GitHub CLI (`gh`) — OPTIONAL, not core Git

The `gh` command is **GitHub CLI**, a separate program from `git`. These are useful for managing GitHub Issues, Pull Requests, checks, and Releases **without opening the browser**. Install and authenticate `gh` separately if you want to use this section. The original study material emphasizes Git and GitHub UI; this section expands beyond those core exercises.

| Priority | `gh` command / pattern | Purpose | Example (PowerShell) | Flags / notes |
|---|---|---|---|---|
| P2 | `gh --version` | Verify GitHub CLI installation | `gh --version` | Separate binary from `git`. |
| P2 | `gh auth login` | Sign into GitHub CLI | `gh auth login` | Interactive auth flow. Does not change commit author metadata. |
| P2 | `gh auth status` | Inspect CLI authentication | `gh auth status` | Useful when `gh` actions fail. |
| P2 | `gh repo view --web` | Open current repository in browser | `gh repo view --web` | `--web` = open browser. |
| P3 | `gh repo clone <OWNER/REPO>` | Clone a GitHub repository | `gh repo clone OWNER/REPO` | GitHub CLI wrapper for obtaining clone. |
| P3 | `gh repo create <NAME> --private` | Create a private GitHub repository | `gh repo create practice-repo --private` | Follow prompts; `--private` specifies visibility. |
| P2 | `gh pr create --base <BASE> --head <HEAD>` | Open a PR from current repository branches | `gh pr create --base main --head feature/login` | `--base` = target, `--head` = source; interactive prompts if other fields omitted. |
| P2 | `gh pr list` | List PRs for repository | `gh pr list` | Quick review queue. |
| P2 | `gh pr view <NUMBER> --web` | Open PR in browser | `gh pr view 12 --web` | `--web` = open browser. |
| P2 | `gh pr diff <NUMBER>` | Inspect PR's proposed file changes | `gh pr diff 12` | Review scope before approval. |
| P2 | `gh pr checks <NUMBER>` | Show status checks for a PR | `gh pr checks 12` | Reports checks that exist; doesn't create CI workflows. |
| P3 | `gh pr checkout <NUMBER>` | Check out a PR locally | `gh pr checkout 12` | Can create/update local branch and fetch relevant history. |
| P3 | `gh pr review <NUMBER> --approve` | Approve someone else's PR | `gh pr review 12 --approve` | PR authors cannot approve their own PRs. |
| P3 | `gh pr review <NUMBER> --request-changes --body <TEXT>` | Request review changes | `gh pr review 12 --request-changes --body "Add validation tests"` | `--request-changes` = blocking feedback depending on rules. |
| P3 | `gh pr merge <NUMBER> --merge` | Merge PR using merge-commit strategy | `gh pr merge 12 --merge` | `--merge` = create merge commit, if permitted. |
| P3 | `gh pr merge <NUMBER> --squash` | Squash-merge PR | `gh pr merge 12 --squash` | `--squash` = one commit on base. |
| P3 | `gh pr merge <NUMBER> --rebase` | Rebase-merge PR | `gh pr merge 12 --rebase` | `--rebase` = replay commits onto base. |
| P2 | `gh issue list` | List Issues | `gh issue list` | GitHub work tracking, not Git commits. |
| P2 | `gh issue create --title <TITLE> --body <TEXT>` | Create an Issue | `gh issue create --title "Fix blank email" --body "Reject empty registration emails"` | `--title` and `--body` set Issue fields. |
| P2 | `gh issue view <NUMBER> --web` | Open an Issue | `gh issue view 12 --web` | Read requirements and comments. |
| P3 | `gh issue close <NUMBER>` | Close an Issue | `gh issue close 12` | Track work as finished; doesn't modify source code. |
| P3 | `gh release list` | List GitHub Releases | `gh release list` | Distinct from `git tag`. |
| P3 | `gh release create <TAG> --title <TITLE> --notes <TEXT>` | Publish a GitHub Release | `gh release create v1.0.0 --title "Version 1.0.0" --notes "First stable release"` | Prefer creating/pushing verified tag first. |
| P3 | `gh run list` | List GitHub Actions workflow runs | `gh run list` | Useful once CI is configured. |

**GitHub browser tasks still matter:** Selecting a reviewer, code review discussion, GitHub repository rulesets/branch protection, and viewing checks can all be done via the GitHub website. A PR is **not** created by `git push` alone, and `git pull` is **not** a Pull Request.

---

## 14. Copy-friendly everyday workflows

### A. New project, starting with `git init` (existing local source)

Use a **new empty GitHub repository** if you're publishing existing local history to avoid unrelated initial commits on the remote.

```powershell
git init -b main
git status
git add README.md
git commit -m "Initialize project"
git remote add origin https://github.com/OWNER/REPO.git
git push -u origin main
```

**Expansion:** `-b` = `--initial-branch`; `-m` = `--message`; `-u` = `--set-upstream`.

### B. New feature → review → merge → update local main

```powershell
# Begin with a clean local main tracking origin/main
git switch main
git pull --ff-only
git switch -c feature/login

# After changing relevant files
git status
git add src/
git diff --staged
git commit -m "Implement login feature"
git push -u origin feature/login

# GitHub: open PR (base main, compare feature/login)
# GitHub: request review, address comments, then merge after requirements pass

# After successful GitHub merge
git switch main
git pull --ff-only
git log --oneline --graph --decorate --all
```

**Expansion:** `--ff-only` = fast-forward only; `-c` = `--create`; `-m` = `--message`; `-u` = `--set-upstream`.

### C. Safe response to a rejected push

```powershell
# First read the actual error and confirm current branch
git status
git branch -vv

# If the rejection is non-fast-forward:
git fetch origin
git log --oneline --graph --decorate --all
git log main..origin/main --oneline
git log origin/main..main --oneline

# Choose a deliberate merge or rebase policy after inspecting the graph.
# Example (only if you are on main and clean, and merging is appropriate):
git switch main
git merge origin/main
# Resolve conflicts if any, verify and finish the merge.
git push origin main
```

**Not a universal rejection fix:** This workflow is for **history divergence**. Missing permissions, credentials, and protected-branch rules need their own solutions. **Do not automatically add `--force`.**

### D. Respond to a merge conflict in a feature PR

```powershell
# With a clean working state on your feature branch
git switch feature/login
git fetch origin
git merge origin/main

# If Git pauses for conflicts:
git status
git diff --name-only --diff-filter=U
# Open each affected file in VS Code; choose correct final content.
git add <RESOLVED_FILE>
git status
git merge --continue
# Test the result, then update your PR's branch.
git push origin feature/login
```

**Expansion:** `--name-only` = show filenames; `--diff-filter=U` = select **unmerged** paths; `--continue` = finish prepared merge. Replace `<RESOLVED_FILE>`.

---

## 15. Common errors → what to do first

| Symptom | Likely cause | Safe first action | Avoid |
|---|---|---|---|
| “Nothing to commit” | No staged changes; or only ignored/untracked paths | `git status`, `git diff`, `git diff --staged` | Blindly committing again |
| “No upstream branch” | New branch has no tracking configuration | `git branch -vv`; often `git push -u origin <BRANCH>` | Guessing destination |
| “Remote origin already exists” | Already configured alias | `git remote -v`; optionally `git remote set-url origin <URL>` | Adding same alias repeatedly |
| “Rejected (fetch first)” | Remote includes commits missing from local branch ancestry | `git fetch origin`; inspect log/diff | Unconditional `git push --force` |
| PR cannot merge automatically | Incompatible changes in branches | Fetch, merge base into feature, inspect unmerged files | Blindly accepting one side |
| Branch won't delete with `-d` | Git cannot confirm merged reachability | Inspect graph and PR merge type | Casual `-D` |
| `stash` reference ambiguous | Another ref named `stash` may exist | `git status`; `git show-ref stash`; check `git stash list` | Applying same stash twice blindly |
| SSH permission denied | SSH key/auth configuration issue | Check URL, registered public key, SSH identity | Editing commit `user.email` |
| HTTPS push denied | Wrong credential, missing write access, or branch protection | Read remote error; `git remote -v` | Assuming `git fetch` fixes permissions |
| “detached HEAD” | Checked out a commit/tag rather than local branch | `git status`; `git switch main` if safe | Creating important commits without a branch reference |

---

## 16. Learning plan: what to memorize and when

| Phase | Study focus | Priority | Mastery test |
|---|---|---|---|
| **1 — Daily basics** | `status`, `add`, `diff`, `commit`, `log`, `show` | P1 | Can predict staged vs unstaged snapshots. |
| **2 — Branch collaboration** | `branch`, `switch`, `merge`, conflict resolution, `restore` | P1 | Can explain HEAD and resolve a simple merge conflict. |
| **3 — Remote teamwork** | `remote`, `fetch`, `pull --ff-only`, `push -u`, `branch -vv`, PR workflow | P1 | Can publish a feature, raise a PR, update local main. |
| **4 — Safe maintenance** | `.gitignore`, `revert`, `amend`, `stash`, `reset --soft/--mixed` | P2 | Can pick an undo operation without risking existing work. |
| **5 — History and releases** | `reflog`, `rebase`, `cherry-pick`, `tag`, GitHub Release | P2/P3 | Can explain new commit IDs and recovery without using force. |
| **6 — Optional automation** | `gh` CLI, Actions checks, advanced policies | P3 | Can inspect PRs and runs without confusing Git with GitHub. |

### Four questions to ask before any unfamiliar command

1. **Where am I?** Current repository and checked-out branch (`git status`).
2. **What will change?** Working Tree, Index, local commits, local refs, or remote refs?
3. **Will history be rewritten?** Is anyone else already relying on these commits?
4. **How will I verify it?** `status`, `diff`, `log`, and/or GitHub PR/CI checks.

### Source and scope notes

- Core concepts, ordering, command safety, and practice scenarios are grounded in the two supplied learning materials: **Git & GitHub — Essential Concepts** and **Git & GitHub Mini Project**.
- The optional **GitHub CLI (`gh`)** table and supplemental convenience commands extend that material for practical usage; they are not prerequisites for completing the original mini-project.
- GitHub CLI features, repository permissions, and branch rules vary by setup. Follow the actual message produced by your installed tools and your team's repository policy.
- Official references: [Git command reference](https://git-scm.com/docs), [GitHub CLI manual](https://cli.github.com/manual/), and [GitHub Docs](https://docs.github.com/).

**Final rule: inspect first, predict the state transition, run the command, then verify what changed.**
