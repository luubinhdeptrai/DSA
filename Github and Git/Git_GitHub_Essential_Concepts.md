# Git & GitHub — Essential Concepts

> A first-principles guide to understanding version control, project history,
> branches, collaboration, and GitHub workflows.

**Environment:** Windows + PowerShell + VS Code  
**Companion exercise:** [Git & GitHub Mini Project](Git_GitHub_Mini_Project.md)  
**Learning goal:** predict what an operation changes in files, the index, history, local references, and remote repositories before running it.

Your starting point is ordinary development: open a folder, edit source code, save files, and eventually share a working version. No particular Java application is needed. The companion uses a separate disposable folder named `git-github-practice`; its text files give us changes to reason about.

```text
understand a state -> predict an operation -> perform it -> inspect evidence
        ^                                                    |
        +---------------- explain the result ----------------+
```

Git is useful when this loop becomes deliberate. Memorizing fifty commands without knowing which state they affect makes mistakes harder to diagnose.

**Verification note:** The companion's core reference commands were replayed with Git 2.45.1 for Windows in isolated temporary repositories. A local bare remote simulated hosted commits and merges; GitHub UI/authentication steps were checked against official documentation, without live account operations. GitHub settings and feature availability can vary; relevant sections link to their current documentation.

## Connections to what you already know

| Existing idea | Git/GitHub interpretation | Where the analogy stops |
|---|---|---|
| project folder | working tree containing editable files | history lives separately in Git's repository data |
| save a file in VS Code | change working-tree content | saving creates no commit |
| selected changes for one delivery | staging area/index | the index holds proposed file content, not merely a list of filenames |
| recorded version | commit | includes ancestry and metadata as well as a snapshot |
| development line | branch | a movable reference, with no duplicated folder required |
| stable version label | tag | publishing release notes is a separate hosting feature |
| shared project server | GitHub remote repository | local repositories also contain history |
| team review | Pull Request and review | these are GitHub records, outside Git's object model |
| CI/CD | automation reacting to pushes/PRs | a successful push alone does not build or deploy anything |
| build output such as `target/` | generated files usually excluded from tracking | reproducible source and configuration still belong in Git |

## How to study

1. Read the concept before its command.
2. Predict the repository state before running the command.
3. Try the matching task in the companion practice repository.
4. Run `git status`.
5. Inspect `git diff`, `git diff --staged`, or the commit graph.
6. Explain what changed in the working tree, index, references, or history.
7. Answer the checkpoint before opening its answer.
8. Continue when the observed state makes sense.

> When confused, run `git status` before randomly entering more commands.

Examples in this guide illustrate individual concepts; they are **not one continuous script**. The companion supplies the ordered practice. Replace placeholders such as `<COMMIT_ID>` before use; angle brackets are not PowerShell syntax for a value. Keep revision expressions such as `'HEAD@{1}'` quoted in PowerShell. Use `q` to leave Git's pager; `git --no-pager log --oneline` avoids it for one invocation.

## Priorities

| Priority | Meaning | You should be able to… |
|---|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Everyday state and collaboration reasoning | predict the change, perform it, inspect it, and diagnose a mistake |
| ⭐⭐⭐⭐ IMPORTANT | Common development and recovery operation | explain the trade-off and use it with appropriate safeguards |
| ⭐⭐⭐ NICE TO KNOW | Useful selective or deeper technique | recognize its purpose and know where to look next |
| ⭐⭐ FUTURE KNOWLEDGE | Later automation or organizational topic | explain where it fits without mastering configuration |

| Topic | Priority |
|---|---|
| repository, working tree, index, file states | ⭐⭐⭐⭐⭐ MUST KNOW |
| status, add, commit, log, diff | ⭐⭐⭐⭐⭐ MUST KNOW |
| HEAD, branches, switch, merge, conflicts | ⭐⭐⭐⭐⭐ MUST KNOW |
| local/remote, clone, origin, remote-tracking references | ⭐⭐⭐⭐⭐ MUST KNOW |
| fetch, pull, push, upstream relationship | ⭐⭐⭐⭐⭐ MUST KNOW |
| Pull Requests, review, feature-branch workflow | ⭐⭐⭐⭐⭐ MUST KNOW |
| safe undo decisions, restore, revert, secret handling | ⭐⭐⭐⭐⭐ MUST KNOW |
| reset modes, amend, stash, rebase, authentication | ⭐⭐⭐⭐ IMPORTANT |
| reflog, cherry-pick, object model, annotated tags | ⭐⭐⭐ NICE TO KNOW |
| detailed rulesets, fork administration, Actions/CI/CD | ⭐⭐ FUTURE KNOWLEDGE |

## Navigation

1. [Why version control exists](#1-why-version-control-exists)
2. [Git vs GitHub](#2-git-vs-github)
3. [Repository](#3-repository)
4. [The three core areas](#4-the-three-core-areas)
5. [File states](#5-file-states)
6. [Init and clone](#6-init-and-clone)
7. [Status](#7-status)
8. [Add and staging](#8-add-and-staging)
9. [Commit](#9-commit)
10. [Identity and configuration](#10-identity-and-configuration)
11. [Hashes and the object model](#11-hashes-and-the-object-model)
12. [Log and show](#12-log-and-show)
13. [Diff](#13-diff)
14. [HEAD and parent notation](#14-head-and-parent-notation)
15. [Branches and moving pointers](#15-branches-and-moving-pointers)
16. [Create and switch branches](#16-create-and-switch-branches)
17. [Merging in the correct direction](#17-merging-in-the-correct-direction)
18. [Fast-forward and merge commits](#18-fast-forward-and-merge-commits)
19. [Three-way merge and conflicts](#19-three-way-merge-and-conflicts)
20. [Resolve or abort a merge](#20-resolve-or-abort-a-merge)
21. [Delete branches safely](#21-delete-branches-safely)
22. [Ignore rules and secrets](#22-ignore-rules-and-secrets)
23. [Choose an undo operation](#23-choose-an-undo-operation)
24. [Restore](#24-restore)
25. [Reset](#25-reset)
26. [Revert](#26-revert)
27. [Amend and commit quality](#27-amend-and-commit-quality)
28. [Stash](#28-stash)
29. [Reflog and recovery](#29-reflog-and-recovery)
30. [Cherry-pick](#30-cherry-pick)
31. [Rebase](#31-rebase)
32. [Merge vs rebase](#32-merge-vs-rebase)
33. [Detached HEAD](#33-detached-head)
34. [Tags](#34-tags)
35. [Local and remote repositories](#35-local-and-remote-repositories)
36. [Remotes and origin](#36-remotes-and-origin)
37. [Remote-tracking branches](#37-remote-tracking-branches)
38. [Fetch](#38-fetch)
39. [Pull](#39-pull)
40. [Push and rejection](#40-push-and-rejection)
41. [Upstream tracking](#41-upstream-tracking)
42. [GitHub repositories](#42-github-repositories)
43. [Clone vs fork](#43-clone-vs-fork)
44. [Pull Requests](#44-pull-requests)
45. [Code review](#45-code-review)
46. [GitHub merge strategies](#46-github-merge-strategies)
47. [Issues](#47-issues)
48. [Branch protection and rulesets](#48-branch-protection-and-rulesets)
49. [GitHub Releases](#49-github-releases)
50. [Authentication](#50-authentication)
51. [Force push](#51-force-push)
52. [Team workflow](#52-team-workflow)
53. [GitHub Actions and CI/CD](#53-github-actions-and-cicd)
54. [Common mistakes](#54-common-mistakes)
55. [Complete workflow trace](#55-complete-workflow-trace)
56. [Readiness and interview review](#readiness-and-interview-review)

---

## 1. Why version control exists

**⭐⭐⭐⭐⭐ MUST KNOW**

Imagine keeping versions by copying folders:

```text
project/
project-final/
project-final-2/
project-final-real/
project-final-fixed/
```

Which one works? Which file changed? Why? Where is the version before the regression? How do you combine your change with a teammate's without overwriting either one?

Folder copies do not record relationships or intent. Comparing them manually becomes expensive, and two people's idea of “final” diverges immediately.

Version control records meaningful versions and their relationships. Git lets you inspect history, compare changes, experiment on another development line, integrate parallel work, reverse a mistake, and name a release. Messages and authorship provide context for an audit or investigation.

The essential idea is a **graph of recorded states**, rather than a pile of folders. An operation either observes that state or transforms part of it. A saved file still needs an explicit decision before it becomes recorded history.

### Checkpoint 1

You saved a bug fix in VS Code and closed the editor. What evidence tells a teammate why the fix happened?

<details>
<summary>Answer</summary>

Saving alone gives no Git history or explanation. A coherent commit records the selected content and a message; sharing that commit makes it available to others. A PR can add review and discussion around it.

</details>

## 2. Git vs GitHub

**⭐⭐⭐⭐⭐ MUST KNOW**

| Question | Git | GitHub |
|---|---|---|
| What is it? | distributed version-control system | repository hosting and collaboration platform |
| Where does it work? | locally; also communicates with other repositories | hosted service reached over the network |
| Primary data | objects, commits, references, index | hosted Git data plus PRs, Issues, reviews, settings |
| Works offline? | commit, branch, diff, log, merge generally do | hosted UI and synchronization require connectivity |
| Typical operation | create a commit | review a proposed branch change |
| Is an account necessary? | no for local work | yes for your hosted repositories and authenticated actions |

```text
Laptop: files + local Git repository
             | push commits
             v
GitHub: hosted repository + collaboration records
             | fetch commits
             v
Laptop: updated knowledge of hosted history
```

Git works without GitHub. GitHub repositories use Git. GitHub is not Git. A network outage does not stop local commits; a local commit does not update GitHub.

## 3. Repository

**⭐⭐⭐⭐⭐ MUST KNOW**

A normal newly initialized working repository can be pictured as:

```text
git-github-practice/
  README.md           editable project file
  notes/              editable project files
  .git/               Git's history, references, index, configuration
```

The repository database stores recorded content and relationships. `.git` also contains local bookkeeping: branch references, `HEAD`, configuration, and usually reflogs. A working tree is the checked-out, editable view of a recorded version plus your uncommitted changes.

Working files and repository data are different. Deleting a tracked working file does not immediately delete it from earlier commits. Deleting `.git` can remove the local history and recovery data. Do not manually edit its internals to “fix” Git.

`git status` reads repository state; `git rev-parse --show-toplevel` identifies the working tree root. Inspect hidden entries with `Get-ChildItem -Force` when learning. Advanced worktrees can use a `.git` file pointing elsewhere, and bare server repositories have no normal working tree; these do not change the basic distinction.

## 4. The three core areas

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
+-------------------------+
| WORKING TREE            |  actual editable files on disk
+------------+------------+
             | git add: copy selected content into the index
             v
+-------------------------+
| STAGING AREA / INDEX    |  proposed next snapshot
+------------+------------+
             | git commit: record that snapshot and its ancestry
             v
+-------------------------+
| LOCAL REPOSITORY        |  recorded objects and references
+-------------------------+
```

| Area | Answers | Changed by | Retained automatically in commit history? |
|---|---|---|---|
| working tree | What am I editing now? | editor, file commands, checkout/integration operations | no |
| staging area | What snapshot am I proposing next? | add, unstage, integration operations | only when committed |
| repository history | What versions have I recorded? | commit, merge, imported commits | recorded objects remain while retained/reachable |

The index normally starts as the full snapshot of the checked-out commit. Adding one modified file replaces its proposed content in that snapshot. Other tracked files remain represented; a commit does not contain only files named in the latest `git add`.

### Why staging exists

Suppose you fix validation in one file and experiment with logging in another. You want the validation fix reviewed independently. The index lets you select the validation change without including unfinished logging.

Even within one file, `git add -p` can select separate hunks. Learn ordinary file staging first; use patch staging when changes really are separable.

```text
HEAD snapshot:       README version 1, notes version 1
working tree:        README version 2, notes experimental
after add README:    index = README version 2, notes version 1
after commit:        new commit records that index snapshot
                     working-tree notes remain experimental
```

The file is not “moved out” of your folder when staged. Git records its proposed content. This is why a later edit can coexist with an earlier staged version.

### Checkpoint 2

You edited `README.md` and `notes/learning-log.md`, then staged only README. What will a normal commit record?

<details>
<summary>Answer</summary>

The index snapshot: the new README plus the previously indexed versions of other tracked paths. The unstaged notes edit stays in the working tree. Nothing is uploaded.

</details>

## 5. File states

**⭐⭐⭐⭐⭐ MUST KNOW**

| Term | Meaning relative to Git's comparisons |
|---|---|
| untracked | path is outside the current index and is not ignored |
| tracked | path is represented in the index; it may be clean, modified, or staged |
| unmodified/clean | relevant working/index content agrees with HEAD |
| modified, unstaged | working-tree content differs from the index |
| staged | index differs from HEAD for that path |
| ignored | an untracked path matches an exclusion rule |

```text
new file -> untracked -> add -> staged addition -> commit -> tracked/clean
tracked/clean -> edit -> unstaged modification -> add -> staged change
staged change -> edit again -> staged change AND unstaged modification
tracked file -> delete on disk -> unstaged deletion -> add -> staged deletion
```

These are comparisons, not mutually exclusive stickers attached to a filename. Short status has two columns: index vs HEAD first, working tree vs index second. `??` means untracked; `M ` means staged modification; ` M` means unstaged modification; `MM` means both. A newly staged file edited afterward can show `AM`. During a conflict you may see `UU` and unmerged paths.

Untracked files normally do not appear in `git diff`; inspect the file and stage it deliberately to see its proposed addition in `git diff --staged`.

## 6. Init and clone

**⭐⭐⭐⭐⭐ MUST KNOW**

Both give you a local repository, but they solve different starting problems.

| Concern | `git init -b main` | `git clone <URL>` |
|---|---|---|
| Starting point | your existing/new ordinary folder | an existing repository to copy from |
| History initially | none in a new repository | available history, subject to clone options/access |
| Files | existing files stay as they are | normally checks out the remote's default branch |
| Remote configuration | no remote added automatically | normally creates `origin` and fetch mapping |
| Initial branch | unborn `main` until first commit | local branch usually tracks its remote counterpart |
| Use it when | starting history for your own folder | joining a project or getting another local copy |

```powershell
git init -b main
git status
```

Expected in a fresh folder: Git reports `main`, no commits yet, and any ordinary files as untracked. Source content is unchanged. Git 2.28+ supports `init -b`; `switch` and `restore` were introduced in 2.23. Use a current Git for Windows installation. Do not clone inside another project's repository accidentally.

## 7. Status

**⭐⭐⭐⭐⭐ MUST KNOW**

`git status` turns invisible bookkeeping into a readable report:

```powershell
git status
git status --short
```

It tells you the current branch or detached state, staged changes, unstaged changes, untracked files, merge/rebase progress, and ahead/behind information when an upstream is configured.

Ahead/behind is based on **local references**, which may be stale. A clean working tree says that your file/index comparisons are clean. It does not prove GitHub is synchronized, the program is correct, or a PR is merged.

Use status before switching tasks, before committing, after a conflict, and before undoing. It is observational with respect to project history and content, though Git may refresh index bookkeeping while inspecting files.

## 8. Add and staging

**⭐⭐⭐⭐⭐ MUST KNOW**

```powershell
git add README.md
git diff --staged
```

`add` updates the index with selected working-tree content, including additions, modifications, and deletions. It prepares the next commit. It does not send anything to GitHub.

`git add .` stages eligible changes under your current directory. From the repository root that is broad; from a subdirectory it has narrower scope. `git add -A` without a path covers the whole working tree. Neither is a substitute for inspecting the staged diff, especially when generated files or credentials exist.

### Staging freezes a version

```text
HEAD       = version 1
edit       -> working tree version 2
add        -> index version 2
edit again -> working tree version 3; index still version 2
commit     -> records version 2; version 3 remains unstaged
```

To include version 3, stage again. To keep it for later, commit version 2 and leave the remaining edit. `git restore --staged README.md` removes the proposed difference against HEAD while preserving the working file, once HEAD exists.

### Checkpoint 3

Can the same file have changes in both status sections? Has GitHub changed after `add`?

<details>
<summary>Answer</summary>

Yes: the index can differ from HEAD while the working tree differs from the index. GitHub has not changed. `add` only changes local staging state.

</details>

## 9. Commit

**⭐⭐⭐⭐⭐ MUST KNOW**

A commit records a snapshot with its history context. It solves “which deliberate version did I choose to keep?”

```powershell
git diff --staged
git commit -m "Add project introduction"
git status
```

A commit object refers to a root tree, parent commit(s), author and committer identities/timestamps, and a message. The first/root commit has no parent; an ordinary later commit has one; a normal two-branch merge commit has two.

```text
old tip A <- new commit B
                     ^
                    main <- HEAD
```

The current branch advances to B. The index usually still contains the snapshot just recorded. Unstaged content remains untouched. If everything intended was staged and committed, status reports clean. If it does not, inspect the remaining difference instead of committing randomly again.

An editor save changes a file; a commit records an indexed snapshot and its ancestry; a push shares reachable objects and requests a remote reference update. Those are separate decisions.

`git commit -a` stages modifications/deletions of already tracked files as part of committing; it does not include ordinary untracked files. Prefer explicit staging while learning so you can explain the snapshot.

## 10. Identity and configuration

**⭐⭐⭐⭐ IMPORTANT**

Commit authorship answers “who is named in this record?” Authentication answers “who may access this GitHub repository?” They are independent.

```powershell
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global --get user.name
git config --global --get user.email
```

Replace the examples with your identity. Use the exact no-reply email shown in your GitHub email settings if you prefer that privacy option. Global configuration applies to your Windows user across repositories; repository-local configuration overrides it:

```powershell
git config --local user.name "Practice Student"
git config --show-origin --get user.name
git config --show-origin --get user.email
git config --list
```

Run `--local` inside a repository. Configuration affects future operations; changing your email does not rewrite earlier commits. `--show-origin` helps diagnose a surprising value. Inspect configuration locally; avoid pasting a full dump if old settings contain sensitive URLs.

An optional editor preference is `git config --global core.editor "code --wait"` when VS Code's `code` command is available. `--wait` lets Git wait for the message file to close. Messages supplied with `-m` avoid that editor for the individual commit.

## 11. Hashes and the object model

**⭐⭐⭐ NICE TO KNOW**

Git gives objects content-based IDs. A short prefix such as `a4f9c2b` is convenient when unique in that repository. Do not expect the same example IDs in your exercise.

| Object | Represents | Useful implication |
|---|---|---|
| blob | file content | filenames are recorded by trees, not the blob itself |
| tree | names, modes, and references to blobs/subtrees | represents a directory snapshot |
| commit | root tree, parents, metadata, message | connects a snapshot to history |
| annotated tag object | target object, tagger, message, optional signature | gives a version label richer metadata |

```text
commit B --parent--> commit A
    |
    +--root tree--> README.md -> blob
                   notes/    -> subtree -> learning-log.md -> blob
```

Changing the message, parent, snapshot, or relevant metadata changes the commit object's content and therefore its ID. This explains why amend and rebase create different identities even when visible file changes seem similar.

Git's logical model is snapshots and shared immutable objects. It can compress storage using deltas internally; that storage optimization does not make a commit merely a saved line-by-line patch. A diff is computed by comparing states. Branches and tags are references to objects rather than extra copies of all their content.

## 12. Log and show

**⭐⭐⭐⭐⭐ MUST KNOW**

```powershell
git log
git log --oneline
git log --oneline --graph --decorate --all
git show HEAD
```

`log` walks commit ancestry. The default view starts from your current tip; `--oneline` compresses each entry; `--graph` draws parent connections; `--decorate` displays reference names; `--all` starts from all refs, making other branches and remote-tracking refs visible. This does not fetch GitHub's latest state or automatically include every unreachable commit.

`show HEAD` helps inspect the latest commit's metadata and introduced change. `git show HEAD:README.md` prints that committed file, which may differ from your working copy.

Graphs in this guide use letters as illustrative commits. Unless an arrow explicitly points to a parent, time runs left to right and a line connects a child to its parent:

```text
A---B---C  main
     \
      D---E  feature
```

`main` points to C; `feature` points to E. B is their common ancestor. Actual objects point backward to parents, not forward to descendants.

## 13. Diff

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
HEAD snapshot <--------- index <--------- working tree
              --staged          ordinary diff
HEAD snapshot <------------------------- working tree
                            diff HEAD
```

| Command | Compares | Question answered | Moves anything? |
|---|---|---|---|
| `git diff` | index → working tree | What tracked edits have I not staged? | no |
| `git diff --staged` | HEAD → index | What proposed changes will a normal commit record? | no |
| `git diff HEAD` | HEAD → working tree, for relevant tracked paths | What is the combined difference from my commit? | no |
| `git diff main feature` | two endpoint trees | How do their final snapshots differ? | no |

Patch lines prefixed `-` come from the old side; `+` from the new. Hunk headers identify nearby line ranges. Inspect context, not only the number of changed lines.

The two-dot endpoint form above differs from `git diff main...feature`: the three-dot form compares the merge base to `feature`, useful for understanding a feature's proposed changes. Do not assume every command uses dots identically; revision ranges in `log` have different semantics.

### Checkpoint 4

After staging version 2 and editing version 3, which diff shows 1 → 2, and which shows 2 → 3?

<details>
<summary>Answer</summary>

`git diff --staged` shows HEAD version 1 → index version 2. `git diff` shows index version 2 → working-tree version 3. An empty ordinary diff does not imply that nothing is staged.

</details>

## 14. HEAD and parent notation

**⭐⭐⭐⭐⭐ MUST KNOW**

`HEAD` identifies what is currently checked out. Normally it is a symbolic reference to your current local branch:

```text
HEAD -> main -> C
```

When you commit D on main, main advances and HEAD still names main:

```text
HEAD -> main -> D --parent--> C
```

Changing branches redirects HEAD and updates checked-out content as needed. Before the first commit, HEAD can name an unborn branch with no commit yet. Detached HEAD instead directly names a commit; see [section 33](#33-detached-head).

```powershell
git branch --show-current
git rev-parse --short HEAD
git show 'HEAD~1'
```

The last two need an existing commit, and `HEAD~1` needs a parent. `HEAD~1` means follow the first parent once. For a merge, `HEAD^1` identifies its first parent and `HEAD^2` the second; `HEAD~2` follows two first-parent steps. A relative name is evaluated **at the moment you run it**. After reset or another commit, the same text may identify something different. Save a hash or reference when a later step must use an exact earlier commit.

## 15. Branches and moving pointers

**⭐⭐⭐⭐⭐ MUST KNOW**

A branch is a movable named reference to a commit. Creating it does not duplicate your folder or record a new snapshot.

| Concern | Branch | Commit |
|---|---|---|
| Nature | name/reference | immutable recorded object |
| Changes as work continues? | normally advances on commit | existing object stays unchanged |
| Can several names identify it? | several branches can point to one commit | one commit can be reached through many refs |
| Deleting it | removes a branch name | does not immediately erase reachable commits |
| Example | `feature/profile` | a specific hash |

```text
initial:             A---B  main, feature/profile
                           ^ HEAD -> feature/profile

commit on feature:   A---B  main
                         \
                          C  feature/profile <- HEAD
```

Only the current branch advances on an ordinary commit. Main remains at B. The branches share ancestry rather than holding unrelated project copies. A commit can be on multiple branches in the sense of being reachable from their tips; it is not owned exclusively by the branch where it was created.

## 16. Create and switch branches

**⭐⭐⭐⭐⭐ MUST KNOW**

```powershell
git branch
git branch feature/profile
git switch feature/profile
```

`branch feature/profile` creates a name at your current commit but does not switch. The combined modern operation is:

```powershell
git switch -c feature/profile
```

Choose one creation form, not both with the same existing name. `git switch main` changes your current branch. Its checked-out tree and index normally follow main; Git refuses if switching would overwrite relevant local changes. Some non-conflicting uncommitted edits can carry across a switch. They are not automatically assigned to the branch you were on when you typed them.

Start beginner branch exercises clean. Commit a coherent change or stash unfinished work before switching when ownership is uncertain. Do not use discard/force options to bypass a warning without understanding the files at risk.

Older tutorials use `git checkout -b` to create/switch and `git checkout -- path` to restore files. Recognize those forms; prefer `switch` for branch selection and `restore` for content so their purposes remain distinct.

### Checkpoint 5

You create a feature branch at B, switch to it, and commit C. Which references move?

<details>
<summary>Answer</summary>

The feature branch advances to C. HEAD still points to that branch. Main stays at B. Neither GitHub nor a remote-tracking branch needs to change.

</details>

## 17. Merging in the correct direction

**⭐⭐⭐⭐⭐ MUST KNOW**

Merging integrates another history into your **current branch**:

```powershell
git switch main
git status
git merge feature/profile
```

Read this as “integrate feature/profile INTO main.” Main is the target; the named branch is the source. Feature/profile does not advance merely because main merged it.

The result depends on ancestry: already up to date, a fast-forward, or a merge requiring a new commit. Combining file content may succeed automatically or stop for conflicts. Integration can change the index, working files, and current branch history; it is more consequential than `fetch`.

Begin with a clean index and working tree so unrelated work is not mixed into the merge and abort is easier to reason about. `git log --oneline --graph --decorate --all` establishes ancestry before you decide what should happen.

## 18. Fast-forward and merge commits

**⭐⭐⭐⭐⭐ MUST KNOW**

If main's tip is an ancestor of feature's tip, the target can simply advance:

```text
before: A---B---C---D  feature
            ^ main

after:  A---B---C---D  main, feature
```

No new commit is required. `git merge --ff-only feature` accepts only this kind of advancement (or an already up-to-date state); it refuses a divergent integration.

When both branches have new commits:

```text
before:       C  main
             /
        A---B
             \
              D  feature

after:  A---B---C---M  main
             \     /
              D---/   feature still points to D
```

M has parents C and D. Existing commits retain their IDs. `git merge --no-edit feature` can use an automatically generated merge message for this ordinary merge. `--no-ff` deliberately records a merge commit even when a fast-forward is possible, useful when branch boundaries matter.

| Concern | Fast-forward | Merge commit |
|---|---|---|
| Necessary ancestry | target is ancestor of source | divergent histories, or deliberately `--no-ff` |
| New commit object | no | yes, normally with two parents |
| Target reference | advances to existing source tip | advances to new merge commit |
| Source reference | unchanged | unchanged |
| Existing commit identities | preserved | preserved |
| Can need content resolution? | no three-way content combination | yes, depending on changes |

## 19. Three-way merge and conflicts

**⭐⭐⭐⭐⭐ MUST KNOW**

A three-way merge compares a common ancestor, the current branch, and the incoming branch. It asks what each side changed relative to the base, then combines compatible changes.

```text
                 current C: greeting = Hello team
                /
base B: Hello -<
                \
                 incoming D: greeting = Hello developers
```

Both sides replaced the same base line differently. Git cannot decide the intended final meaning. Other conflicts involve modify/delete, add/add, rename, or file/directory collisions. Many edits to different parts of one file merge automatically; conflict is about incompatible changes, not simply two people editing any shared filename.

In a content conflict:

```text
<<<<<<< HEAD
Hello team
=======
Hello developers
>>>>>>> feature/conflict-demo
```

`<<<<<<<` starts current-side content, `=======` separates sides, and `>>>>>>>` ends incoming content and names its source. Some configurations include an ancestor block beginning `|||||||`. During an ordinary merge, HEAD remains the pre-merge current tip until completion. The index records unmerged entries for base/ours/theirs instead of one settled result for that path.

Automatic merging is textual. A clean merge can still be semantically wrong; run the relevant checks and review behavior.

### Checkpoint 6

Does removing conflict-marker lines automatically give you the correct result?

<details>
<summary>Answer</summary>

No. It only removes syntax marking an unresolved choice. You must determine the intended final content, which may use one side, both sides, or a third version. Then stage the resolved result and finish the operation.

</details>

## 20. Resolve or abort a merge

**⭐⭐⭐⭐⭐ MUST KNOW**

After a merge stops:

```powershell
git status
git diff --name-only --diff-filter=U
code src/greeting.txt
```

Read both intentions. Edit a coherent final file, remove markers, and verify it. VS Code's merge editor is optional; its buttons do not decide the correct business meaning for you.

```powershell
git add src/greeting.txt
git diff --staged
git status
git commit -m "Resolve greeting conflict"
git log --oneline --graph --decorate --all
```

`add` replaces the unmerged entries with your resolved index content. It does not finish the merge. The commit records the combined snapshot with both parents; status should then be clean if no unrelated edits remain. `git merge --continue` is another way to complete a prepared merge.

If you intend to abandon the in-progress merge, inspect status and use `git merge --abort`. Starting clean matters: abort attempts to reconstruct the pre-merge state and cannot always preserve unrelated edits made around an already dirty start. It is not an operation for undoing an already completed merge commit.

Do not start a pull, rebase, or another merge to escape an unresolved merge. Finish or abort the current operation first.

## 21. Delete branches safely

**⭐⭐⭐⭐ IMPORTANT**

Finished branch names can clutter your view. Delete the name only after verifying that useful work is integrated or otherwise retained:

```powershell
git switch main
git branch --merged main
git branch -d feature/profile
```

You cannot delete the branch currently checked out; a branch checked out in another linked worktree is also protected. `-d` checks merge reachability, using the branch's upstream when configured or HEAD otherwise. It does not understand that a squash-created commit might contain equivalent changes.

Deleting a local name does not delete the GitHub branch, erase a merged commit, or delete your current files. Remote deletion is separate: `git push origin --delete feature/profile`. Fetch with `git fetch --prune origin` removes stale local remote-tracking references for deleted remote branches. It does not delete corresponding local branches.

`-D` overrides the merge check and can discard your convenient reference to unmerged work. Diagnose a refused deletion with `git log main..feature/profile` and the PR's actual merge strategy before deciding. Create a backup branch if the work's retention is uncertain.

## 22. Ignore rules and secrets

**⭐⭐⭐⭐⭐ MUST KNOW**

`.gitignore` excludes matching untracked paths from normal consideration for tracking. It reduces accidental additions of generated or local-only files:

```gitignore
target/
build/
.env
local.env
*.log
# Team decision: ignore personal editor settings if they are not shared.
.vscode/
```

Some teams share selected `.vscode` tasks/settings; ignoring the whole directory is a policy choice. Keep required source, migration scripts, and reproducible configuration templates tracked. Git does not track empty directories independently; a tracked file is needed to retain one.

```powershell
git check-ignore -v local.env
git status --ignored --short
git ls-files -- local.env
```

The first command identifies an exclusion rule; `ls-files` tells you whether the file is in the index. Ignore patterns use Git's slash/pattern rules, not PowerShell wildcard processing. A trailing slash selects directories; a leading slash anchors a pattern to the ignore file's directory.

Adding a tracked path to `.gitignore` does **not** untrack it. `git rm --cached -- local.env` stages removal from the repository while leaving the local file, and a later commit records that removal. Prior commits still contain its old content.

Never commit real `.env` values, PATs, private SSH keys, or passwords. Use fake placeholders and safe templates. `.gitignore` is not a security system. If an actual secret entered history, revoke/rotate it first; then remove it from current tracking and coordinate any history cleanup using [GitHub's sensitive-data guidance](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository). A deletion commit or revert cannot make the old secret confidential again.

### Checkpoint 7

You committed a real token yesterday and ignored its file today. Is the token now safe?

<details>
<summary>Answer</summary>

No. The token remains in existing commits and may already have been copied. Revoke/rotate it. Ignoring an untracked file helps prevent a future mistake; it cannot undo exposure.

</details>

## 23. Choose an undo operation

**⭐⭐⭐⭐⭐ MUST KNOW**

Begin by locating the change and deciding what must be preserved:

```text
Is it committed?
  no -> Is it staged?
          yes -> unstage while keeping files: restore --staged
          no  -> keep it? commit/stash/copy
                 discard selected tracked edit? restore, after inspecting diff
  yes -> Has someone else received/built on it?
          yes -> normally add a corrective commit or revert
          no  -> amend/reset/rebase may reshape private history deliberately
```

| Operation | Main target | Changes existing branch history? | Preserves working edits? | Typical use |
|---|---|---|---|---|
| restore | selected working-tree/index paths | no branch movement | depends on destination/source | discard an edit or unstage |
| reset | branch tip and/or index | commit-form moves tip; path-form does not | depends on mode | reorganize unpublished work |
| revert | new corrective commit | appends instead of replacing existing commits | normally start clean | undo a shared change |

These are not three spellings of “undo.” First inspect `status`, both diffs, and the graph. If history seems missing, inspect reflog and create a rescue reference before more rewriting. Clean working files and an unchanged commit graph are different goals.

## 24. Restore

**⭐⭐⭐⭐⭐ MUST KNOW**

Two destinations explain two commonly confused forms:

```powershell
git restore --staged README.md
git restore README.md
```

The first copies the default source HEAD into that path's index entry: it unstages while leaving your working file alone. The second copies the default source index into the working file: it discards the unstaged difference for that tracked path. Neither moves your branch or uploads anything.

```text
HEAD v1; index v2; worktree v3
restore README            -> HEAD v1; index v2; worktree v2
restore --staged README   -> HEAD v1; index v1; worktree v3
                            (evaluated from the original state)
```

**Inspect the diff before restoring working content.** Uncommitted edits overwritten this way are not generally recoverable through reflog. For a newly added file, unstaging normally makes it untracked; it does not delete the file. Before a first commit, HEAD does not exist, so do not expect HEAD-based unstaging to work the same way.

To deliberately copy an older file into the working tree, `git restore --source=<COMMIT_ID> -- README.md` selects the source explicitly. This still does not move HEAD; you can inspect and commit the resulting change. Avoid broad path selections until you know every affected file.

## 25. Reset

**⭐⭐⭐⭐ IMPORTANT**

The commit form resets the current branch tip to a selected commit. Consider private history `A---B---C`, currently clean at C, and deliberately target B:

| Mode | Branch/HEAD after reset | Index | Working tree | Visible result from this clean example |
|---|---|---|---|---|
| `--soft` | B | keeps C snapshot | keeps C files | C's difference is staged |
| `--mixed` (default) | B | B snapshot | keeps C files | C's difference is unstaged; newly added paths can become untracked |
| `--hard` | B | B snapshot | overwritten toward B | tracked changes can be lost |

The modes describe destinations; they do not inherently mean “go back one commit.” The target determines how far the pointer moves.

```powershell
# Only an isolated, unpublished experiment; first keep a reference to the tip.
git branch backup-before-reset
git reset --soft 'HEAD~1'
git status
```

This leaves the removed commit reachable through the backup branch. Git has moved a name, not edited the old commit object. Do not use this recipe on shared main to undo a pushed change.

**`--hard` can overwrite tracked edits and may remove/overwrite untracked paths that obstruct writing the target tree.** It is not a general cleanup operation. The companion practices soft/mixed with a backup and does not require hard reset. See the official [reset mode semantics](https://git-scm.com/docs/git-reset).

The path form is different: `git reset HEAD -- README.md` updates index content without moving the branch. Recognize it as an older unstaging idiom; `git restore --staged README.md` communicates that intent more clearly.

## 26. Revert

**⭐⭐⭐⭐⭐ MUST KNOW**

Revert solves “reverse this committed change while retaining the shared record.” It computes the inverse of a commit's introduced change and records a new commit on your current branch:

```text
before: A---B---C          main
after:  A---B---C---R      main
                   R reverses C's change
```

```powershell
git status
git revert --no-edit <BAD_COMMIT_ID>
git log --oneline -3
```

The bad commit remains, and a new commit explains the undo. Later unrelated work should remain where compatible. Revert can conflict when later edits interact with the target. Resolve, stage, then `git revert --continue`, or `git revert --abort` to abandon the operation.

| Concern | Reset to an earlier commit | Revert a commit |
|---|---|---|
| Pointer/history | moves current tip away from later commits | advances to a new corrective commit |
| Old commits | may become unreachable without other refs | stay in ancestry |
| Shared branch | often creates history disagreement | usually fits ordinary push/PR workflow |
| Undo a middle change | requires more careful reshaping | can target it, subject to conflicts |
| Uncommitted loss | mode-dependent | begin clean to avoid mixing work |

Reverting a merge requires deciding a mainline parent (`-m`) and affects future merge behavior; treat it as a separate advanced decision. Revert is also not a secret-removal tool.

### Checkpoint 8

A teammate pulled your bad commit. You want a normal collaborative correction. Reset or revert?

<details>
<summary>Answer</summary>

Normally revert, or write a targeted corrective commit, through the team's usual PR process. Reset only changes your local pointer; forcing that rewritten branch onto the server would disrupt the history the teammate already has.

</details>

## 27. Amend and commit quality

**⭐⭐⭐⭐ IMPORTANT**

Amend replaces the latest commit with a newly created commit. Use it for an unpublished message typo or a forgotten file belonging to that same change:

```powershell
git log -1 --oneline
git commit --amend -m "Add project introduction"
```

For forgotten content, stage it and inspect `git diff --staged`, then `git commit --amend --no-edit` to keep the previous message. All currently staged changes become part of the amended snapshot, so check for unrelated staged files.

```text
before: A---B       branch
after:  A---B'      branch
             B' has a changed message and/or snapshot; B is not mutated
```

The new object normally has a different ID because its content/metadata changed. Treat already-pushed commits as shared unless coordination says otherwise; a follow-up commit is usually easier for everyone.

Good commits have one coherent purpose and a message describing it: `Add registration validation` communicates more than `stuff` or `fix`. Review staged content, avoid generated folders and unrelated formatting, and run checks appropriate to the change. Conventional Commits are an optional team convention, not a prerequisite for understanding Git.

## 28. Stash

**⭐⭐⭐⭐ IMPORTANT**

Stash temporarily stores unfinished local changes so you can change tasks without inventing a finished commit:

```powershell
git stash push -u -m "Unfinished learning notes"
git stash list
git stash show -p 'stash@{0}'
```

The default stash includes tracked staged/unstaged changes; `-u` also includes untracked files. Ignored files require different treatment (`-a` includes them too), so do not assume an ignored secret or large generated directory belongs in a stash. Stash stores local Git objects and references; it is not uploaded by a normal branch push or shared as a team handoff.

| Operation | Applies saved changes? | Removes saved entry? | Useful when |
|---|---|---|---|
| `stash apply` | yes | no | keep a fallback while verifying |
| `stash pop` | yes | on successful application | restore and remove in one step |
| `stash drop` | no | yes | delete an entry after checking it is no longer needed |

```powershell
git stash apply 'stash@{0}'
git status
```

Application may conflict if the destination has changed. A conflicting pop generally keeps the stash entry. Inspect `stash list` before retrying so you do not apply twice. Default application does not promise to recreate the original staged/unstaged split; `--index` attempts to reinstate index state and can fail. Restore to a suitable branch, verify the content, then stage/commit it or drop the saved copy deliberately.

## 29. Reflog and recovery

**⭐⭐⭐ NICE TO KNOW**

The commit graph records ancestry. A reflog records local movements of references, including HEAD's checkout history. It can reveal a tip that disappeared from your current branch after reset or rebase:

```powershell
git reflog --oneline
git show <RECOVERED_COMMIT_ID>
git branch rescue-work <RECOVERED_COMMIT_ID>
```

Creating a rescue branch preserves a candidate commit without overwriting current files. You can inspect or switch to it afterward. Prefer this to immediately resetting everything to an entry whose meaning you have not checked.

`'HEAD@{1}'` means the prior reflog entry for HEAD; it does not necessarily mean the parent commit. A checkout can create an entry too, and later operations shift entry numbers. Read the messages and capture the actual hash.

Reflogs are local, not fetched/pushed as ordinary shared history. Retention and garbage collection are configurable; common defaults expire ordinary entries after 90 days and unreachable entries sooner, often 30 days. Do not rely on a fixed recovery window. They cannot restore arbitrary never-recorded editor changes. See [Git's reflog reference](https://git-scm.com/docs/git-reflog).

## 30. Cherry-pick

**⭐⭐⭐ NICE TO KNOW**

Sometimes you need one focused fix without integrating an entire branch. Cherry-pick applies the change introduced by a chosen commit onto the current branch:

```powershell
git switch release-fix
git cherry-pick <FIX_COMMIT_ID>
```

```text
source: A---B---F---G      feature
target: A---R---F'         release-fix
```

F' applies F's change in a different parent context and normally has a new ID. Dependencies matter: a commit may rely on code added earlier on the source branch. Picking it alone may conflict or compile incorrectly. Use merge when the whole line of work belongs together; repeatedly cherry-picking a long branch makes provenance and later integration harder.

`git cherry-pick --continue` completes after conflict resolution; `--abort` abandons it. Some cases can retain the original identity or fast-forward with suitable options, so the practical rule is to expect a new identity when replaying onto a different history. The companion deliberately creates that different parent context.

## 31. Rebase

**⭐⭐⭐⭐ IMPORTANT**

A private feature may need the latest main before review. Rebase rebuilds the feature's selected commits on a new base:

```text
before: A---B---C          main
             \
              D---E      feature <- HEAD

after:  A---B---C          main
                 \
                  D'---E' feature <- HEAD
```

While on the feature, `git rebase main` identifies commits to replay and applies them onto main. The new commits have different parents, so their IDs change. Original objects are not edited in place. Main does not move. Ordinary rebase can flatten merge topology and omit commits whose changes are already present; advanced options change those choices.

Start clean. First make a local backup reference when learning. On conflict, inspect the current replay step, resolve the intended final file, stage it, and use `git rebase --continue`. `git rebase --abort` returns to the pre-rebase branch state. `--skip` drops the current patch; it is not a general way to fix a conflict. “Ours/theirs” labels during rebase are easy to misread because the checked-out base is the rebuilt history.

Avoid rebasing commits other people are already building on unless the team intentionally coordinates that workflow. A private unpushed branch is the clearest learning setting. See [Git's rebase documentation](https://git-scm.com/docs/git-rebase).

## 32. Merge vs rebase

**⭐⭐⭐⭐ IMPORTANT**

Both can bring main's work into a feature, but they make different history:

| Concern | Merge main into feature | Rebase feature onto main |
|---|---|---|
| Existing commit IDs | preserved | replayed commits get new IDs |
| New integration history | merge commit when histories diverge | rebuilt sequence on new base |
| Main pointer | unchanged | unchanged |
| Feature pointer | advances to combined result | replaced by rebuilt tip |
| Branch topology | retains recorded split/join | simple form produces a linear feature sequence |
| Conflict experience | combine the histories once | may resolve at several replay steps |
| Shared branch | commonly suitable | coordinate if anyone relies on original commits |
| Best reason to choose | preserve integration history | prepare private work on a current base |

Rebase is not universally superior. A team may preserve merge boundaries, squash PRs, or rebase private feature work. Follow the repository's policy and explain its effect. A fast-forward merge **after** rebasing does not undo the fact that the earlier commits were recreated.

### Checkpoint 9

After `git rebase main` on feature, did main advance? Why can the feature's commit IDs differ despite similar patches?

<details>
<summary>Answer</summary>

Main stays at its existing tip. The feature is rebuilt on that tip. Parents are part of commit identity, so replayed commits with new parents have new IDs, even if their introduced file changes look the same.

</details>

## 33. Detached HEAD

**⭐⭐⭐⭐ IMPORTANT**

Checking out a specific old commit can be useful for inspection:

```powershell
git switch --detach <COMMIT_ID>
```

HEAD then points directly to a commit rather than a local branch. You can still inspect and even commit, but no ordinary branch name automatically keeps the new work when you switch away.

```text
normal:    HEAD -> main -> C
detached:  HEAD ---------> B
```

If you made useful work while detached, `git switch -c rescue-work` creates a branch at the current tip and attaches HEAD. If you already left it, use reflog to locate its hash and create a branch there. Do not assume detached means broken, or delete `.git` to fix it.

## 34. Tags

**⭐⭐⭐⭐ IMPORTANT**

A tag gives an important version a stable name. Unlike a development branch, it is conventionally kept fixed as new commits arrive.

```powershell
git tag
git tag -a v1.0.0 -m "Version 1.0.0"
git show v1.0.0
git push origin v1.0.0
```

An annotated tag has its own tag object with metadata and a message. A lightweight tag, created with `git tag v1.0.0`, is a simpler reference. Those two creation forms are alternatives, not sequential steps using the same name. Release tags are commonly annotated, and can optionally be signed.

A regular branch push does not automatically publish every tag. Push the intended tag explicitly. A tag labels a commit; it does not copy uncommitted files or generate release notes. Treat published version tags as stable contracts with consumers rather than moving them casually.

## 35. Local and remote repositories

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
Your computer                            GitHub
+-----------------------+                +----------------------+
| working tree + index  |                | hosted Git repository|
| local Git repository  | -- push ------> | branches and objects |
| local references      | <-- fetch -----|                      |
+-----------------------+                +----------------------+
```

Your local repository has history too. A normal full clone obtains history reachable through its configured fetches; shallow/partial clones and permissions can limit what is present. Ordinary local commits work offline. GitHub is another repository plus hosting features, not a live mirror of every unsaved or uncommitted file on your machine.

Network synchronization is explicit. Another computer can push a commit while your local files, local main, and cached remote-tracking reference all remain unchanged.

## 36. Remotes and origin

**⭐⭐⭐⭐⭐ MUST KNOW**

A remote is a named configuration describing another repository's URL and how to fetch/push references:

```powershell
git remote -v
git remote add origin <REPOSITORY_URL>
git remote get-url origin
```

Replace the URL placeholder with the copied HTTPS or SSH repository address. `origin` is a conventional name, often configured by clone. It is neither a GitHub keyword nor the remote branch itself. A remote can point to GitHub, another host, or a local path.

Adding a remote changes configuration only. It does not upload your project. If origin exists, inspect it; use `git remote set-url origin <CORRECT_URL>` when a correction is intended instead of blindly adding it again.

## 37. Remote-tracking branches

**⭐⭐⭐⭐⭐ MUST KNOW**

There are **three distinct references** to keep in mind:

```text
LOCAL REPOSITORY                           GITHUB REPOSITORY
main          -> B                         main -> C
origin/main   -> B  (last known state)

after fetch:
main          -> B                         main -> C
origin/main   -> C

after intentional fast-forward integration:
main          -> C                         main -> C
origin/main   -> C
```

| Concern | Local branch `main` | Remote-tracking `origin/main` | GitHub's actual `main` |
|---|---|---|---|
| Stored where? | local repository | local repository | hosted repository |
| Meaning | your development tip | locally recorded remote state | remote's current branch tip |
| Normally changes through | commit, merge, reset, etc. | fetch; successful pushes can update matching tracking refs too | accepted push or hosted merge/edit |
| Where you work | switch to main and commit | generally inspect/compare | propose changes through push/PR |
| Could be stale relative to GitHub? | yes | yes | it is the server's state at observation |

`origin/main` is not a second local working branch. Switching directly to a remote-tracking ref for inspection can leave you detached. Use a local branch, with tracking configured when needed.

“Last fetched state” is a useful shorthand, but successful pushes can also update matching remote-tracking refs. It is still a local record, not a live network query. Ahead/behind is measured against that record.

### Checkpoint 10

Someone merges a PR on GitHub while your computer is offline. Has local main changed? Has origin/main changed?

<details>
<summary>Answer</summary>

Neither changes automatically. Fetch updates the relevant remote-tracking record; an integration step updates local main and its checked-out files. GitHub's main can be ahead while status still compares against an old origin/main.

</details>

## 38. Fetch

**⭐⭐⭐⭐⭐ MUST KNOW**

Fetch solves “learn what the remote has without integrating it into my current work.”

```powershell
git fetch origin
git log --oneline --graph --decorate --all
git log main..origin/main --oneline
git diff main origin/main
```

With ordinary origin configuration, fetch downloads missing objects and updates configured remote-tracking refs plus fetch bookkeeping. It leaves your current local branch, index, and working files alone. Custom refspecs can target other refs, so the ordinary statement assumes the usual clone configuration.

If main is behind without divergence, a deliberate `git merge --ff-only origin/main` while on main integrates the fetched tip. If it diverged, inspect why before choosing merge or rebase. Fetch itself has not solved that integration decision.

| Concern | Fetch | Clone |
|---|---|---|
| Destination | an existing repository | creates a new local repository/directory |
| Core effect | obtain missing objects/update configured tracking refs | initialize, fetch, configure origin, normally check out a branch |
| Working files | no normal checkout change | initial checkout created |
| Use | refresh knowledge | obtain a first/second local copy |

Fetching with `--prune` also removes stale tracking refs for remote branches that no longer exist. It does not remove your local feature branches.

## 39. Pull

**⭐⭐⭐⭐⭐ MUST KNOW**

Pull is a convenience operation combining fetch with integration into the current branch. Depending on explicit flags and configuration, integration can use merge or rebase, or refuse anything except a fast-forward. It does more than download files.

```powershell
git switch main
git status
git pull --ff-only
```

This assumes main has an upstream. It accepts a straightforward advancement and refuses divergent integration; it does not discard local commits to make the branch fit. Fetch may already have updated tracking refs even when the integration step refuses.

| Concern | Fetch | Pull |
|---|---|---|
| Network retrieval | yes | yes |
| Updates remote knowledge | yes | yes |
| Integrates into current branch | no, with ordinary fetch mapping | yes, or refuses under the selected policy |
| Can change working files/index | not in the ordinary form | yes |
| Can trigger conflicts | not content integration conflicts | merge/rebase integration can |
| Best beginner use | inspect before deciding | explicit `--ff-only` for routine main updates |

For divergent histories, `git pull --no-rebase` requests merge integration and `git pull --rebase` requests replay. Understand the graph and whether your commits are shared before selecting either. Do not keep rerunning pull with random flags during a conflict.

## 40. Push and rejection

**⭐⭐⭐⭐⭐ MUST KNOW**

Push sends needed local objects and asks the remote to update named refs, subject to ancestry, permissions, and server rules:

```powershell
git push origin main
```

Unstaged edits, staged-only changes, stash entries, and untracked files are not sent as part of a normal branch push. What matters is the history reachable from the branch you push. A clean working tree is not required for pushing an existing commit, but it helps beginners keep the steps distinct.

| Concern | Pull | Push |
|---|---|---|
| Main direction | remote → local | local → remote |
| Local integration | yes | no |
| Remote branch update | no | requests one |
| Typical problem | local integration conflict/divergence | non-fast-forward rejection, permission/rule failure |

### Why a normal push can be rejected

```text
known base A---B---C      GitHub main
               \
                D        local main
```

C is absent from local main. Replacing GitHub's tip with D would lose C from that branch's ancestry. A normal push refuses. Fetch, inspect C and D, then integrate intentionally. A merge on local main can preserve both:

```text
A---B---D---M             local main, then pushed main
     \     /
      C---/
```

Rebase of **private** D onto C is another deliberate policy, but do not rewrite teammates' work. Authentication failures and protected-branch rejections need their own diagnoses; fetching cannot fix missing write permission. See [Git's push reference](https://git-scm.com/docs/git-push).

### Checkpoint 11

Does push send your saved but uncommitted README? If a push fails, should you immediately force it?

<details>
<summary>Answer</summary>

No to both. An ordinary branch push shares committed reachable objects, not your current working file. Inspect the rejection: you may need to fetch/integrate, authenticate correctly, use a PR, or correct the remote URL.

</details>

## 41. Upstream tracking

**⭐⭐⭐⭐⭐ MUST KNOW**

Your local branch can record a default counterpart used for status comparisons and argument-free synchronization:

```powershell
git push -u origin feature/profile
git branch -vv
```

On a successful push, `-u` records the upstream relationship, commonly local feature/profile → origin/feature/profile. This is configuration; the local branch and remote-tracking ref remain distinct. `git branch -vv` displays the relationship and locally known ahead/behind counts.

An upstream helps an argument-free pull know which branch to integrate. Push defaults also depend on settings such as `push.default` and possible push-remote overrides; explicitly naming remote/branch is clearest when uncertain. If the remote branch already exists, `git branch --set-upstream-to=origin/main main` configures main without pushing.

| Term | Meaning |
|---|---|
| remote `origin` | conventional name for the repository cloned from or your primary push destination |
| remote `upstream` | conventional additional remote name for an original project in a fork workflow |
| a branch's upstream | configured comparison/integration counterpart, such as `origin/main` |

These names do not need to match. A branch can track `origin/main` even if there is no remote named upstream. A remote named upstream does not automatically set tracking for every branch.

## 42. GitHub repositories

**⭐⭐⭐⭐⭐ MUST KNOW**

A GitHub repository hosts Git objects and refs while adding access controls, collaboration, automation, and release presentation. Visibility controls who can read; permissions and rules control who can write or merge. Public read access does not imply write access.

When uploading an already initialized local repository, create an **empty** hosted repository: do not independently initialize a README, license, or ignore file for the basic exercise. Independent initialization creates a separate root commit, which requires extra reconciliation. If the server already has the desired project history, cloning that history is often the simpler start.

Pushing your initial main transfers Git data. It does not create a PR, Issue, release description, or production deployment automatically. The GitHub UI can create commits too—for example, editing a README in the browser changes the hosted branch while your local copy remains unchanged.

## 43. Clone vs fork

**⭐⭐⭐⭐⭐ MUST KNOW**

| Concern | Clone | Fork |
|---|---|---|
| Kind of operation | Git operation | GitHub hosting operation |
| Creates | local repository copy | server-side repository in another account/namespace |
| Typical location | your computer | GitHub |
| Needs write access to original? | no to clone readable data | subject to hosting/organization policy |
| Gives write access to original? | no | no; you typically write to your own fork |
| Needed with team write access? | usually obtain a clone | usually unnecessary |
| Includes PR/Issue discussion locally? | no | separate hosting relationships/features, not a local checkout |

```text
original GitHub repository
          | fork on GitHub
          v
your GitHub fork
          | clone
          v
local repository
  origin   -> your fork
  upstream -> original repository (added separately)
```

In a fork workflow, fetch upstream, update your local base deliberately, develop on a feature branch, push to origin, and propose a PR back to the original repository. Avoid merging unrelated local experiments into that contribution. The companion keeps forking optional; you can practice ordinary collaboration using your own repository and two clones.

## 44. Pull Requests

**⭐⭐⭐⭐⭐ MUST KNOW**

A Pull Request proposes integrating a head/compare branch into a base branch and gives people a place to review that proposal.

```text
main -> create feature -> commit -> push feature
                                    |
                                    v
                      PR: feature/profile -> main
                              review + checks
                                    |
                                    v
                             merge into main
```

Base is the receiving branch; head/compare supplies proposed changes. Opening a PR does not itself merge either branch or update local main. Further commits pushed to its head branch normally update the open PR.

| Concern | Git commit | GitHub Pull Request |
|---|---|---|
| Data type | Git object | hosting collaboration record |
| Exists offline? | yes when present locally | hosted discussion requires service access |
| Captures | snapshot, parent(s), metadata | proposal, diff, comments, reviews, checks, merge state |
| Scope | one recorded version | one or many commits between histories |
| Created by | `git commit` | GitHub UI/API/CLI |
| Changes branch upon creation? | advances current local branch | opening alone does not merge |

Inspect the base/head names, commits, Files changed, and checks. A good description says what changed, why, and how you verified it. `git pull` and “Pull Request” share a word but are different mechanisms; there is no `git pull-request` command in core Git.

## 45. Code review

**⭐⭐⭐⭐⭐ MUST KNOW**

Review checks whether a proposed change makes sense, stays in scope, and has enough evidence. Read the full diff and relevant surrounding code. Look for accidental secrets, generated files, missing cases, unclear behavior, and weak verification.

| Review interaction | Purpose | What happens in Git? |
|---|---|---|
| comment | ask, explain, or suggest | no commit changes automatically |
| request changes | record blocking feedback | author normally follows with commits |
| approval | signal reviewer acceptance | does not itself perform a merge |
| automated check | report a workflow result | attached to a commit/check context |
| author follow-up push | address feedback | adds or deliberately updates head history |

Make feedback specific and explain the effect: “This accepts a blank email; add a validation case” is more actionable than “bad code.” Authors should explain decisions, verify fixes, and resolve discussions when addressed.

For solo practice, read Files changed and add a comment if useful. **Authors cannot approve their own PRs.** Do not configure required independent approval as a prerequisite for this solo lab. See [GitHub's required-review guidance](https://docs.github.com/en/pull-requests/how-tos/review-pull-requests/approving-a-pull-request-with-required-reviews).

## 46. GitHub merge strategies

**⭐⭐⭐⭐ IMPORTANT**

| GitHub option | History added to the base | Existing feature IDs on the base? | Consequence |
|---|---|---|---|
| Create a merge commit | original commits plus a two-parent merge | yes | preserves ancestry and integration boundary |
| Squash and merge | one new commit representing combined changes | not through this integration alone | concise base history; original commits are not ancestors of the squash commit |
| Rebase and merge | replayed commits on the base, without a merge commit | GitHub creates new IDs | linear sequence with rewritten identities |

GitHub's merge-commit option uses no-fast-forward merging, even when Git could advance the base directly. Its rebase-and-merge behavior creates new commit IDs even in cases where a local rebase might have no work to do. See [GitHub's merge behavior](https://docs.github.com/en/pull-requests/reference/pull-request-merges).

The lab uses **Create a merge commit** so earlier feature commits remain ancestors and local branch deletion is easy to verify. Available options depend on repository settings and rules. If a repository intentionally uses squash or rebase merge, compare the resulting content and follow its policy; a local `branch -d` reachability check may reject deletion even though the PR's changes were integrated.

After a hosted merge, your local main is still where you left it. Fetch and integrate before starting the next feature. Do not continue building unrelated work on a long-lived squash-merged feature branch; create a new branch from updated main.

## 47. Issues

**⭐⭐⭐⭐ IMPORTANT**

An Issue records a problem, task, or investigation. It can have a description, comments, labels, assignments, and links. It is not a snapshot or development line.

| Item | Main question |
|---|---|
| Issue | What work or discussion is needed? |
| branch | Where is the development line? |
| commit | What version/change was recorded? |
| PR | Should this proposed branch change be integrated? |

For example, Issue “Improve README usage section” can lead to branch `docs/readme-usage`, a focused commit, and a PR whose description says `Closes #12` using the **actual** Issue number. Closing keywords in a suitable PR merged into the default branch can close the linked Issue. Merely mentioning a number does not prove the work is complete. Read [GitHub's Issue/PR linking rules](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/linking-a-pull-request-to-an-issue).

## 48. Branch protection and rulesets

**⭐⭐ FUTURE KNOWLEDGE**

Teams often protect main to require PRs, independent approvals, successful checks, resolved discussions, or restrictions on deletion and force pushes. These policies protect the hosted reference; they do not stop you creating a local commit on a branch named main.

Rulesets can apply to branches or tags, with bypass policies and overlapping rules. Availability depends on plan and repository visibility; ordinary branch/tag rulesets are available for public repositories on GitHub Free and more repository types on paid plans. Review [current ruleset availability](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/about-rulesets) before configuring them.

A rejected direct push may mean “use a PR,” not “your credentials are wrong.” Requiring someone else's approval in a solo repository can block your own merge. Requiring a nonexistent check can also leave the repository waiting indefinitely. Practice configuration only after the core lab, using settings you can explain and revert. There is no need to buy a plan for the required exercises.

## 49. GitHub Releases

**⭐⭐⭐⭐ IMPORTANT**

```text
recorded commit -> Git tag -> GitHub Release with notes/assets
```

| Concern | Git tag | GitHub Release |
|---|---|---|
| Owner | Git reference/object model | hosting platform |
| Names a version | yes, by identifying an object, usually a commit | uses a tag as its version anchor |
| Notes/assets | annotated tag has a message | release description and optional downloadable assets |
| Appears in local history | fetched tag can be local | release page is not a Git commit |
| Created by | `git tag` | GitHub release UI/API/CLI |

Create the tag at the verified commit, push that intended tag, then select the **existing tag** in the release UI. Write concise notes explaining what that version demonstrates. Creating a Release is not automatically a deployment. For this text-only lab, extra binary assets are unnecessary. See [GitHub's release overview](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases).

## 50. Authentication

**⭐⭐⭐⭐ IMPORTANT**

Authentication proves access to GitHub. Authorization decides whether that account/credential may read, push, or merge. `user.name` and `user.email` only supply commit metadata; setting them does not log you in or grant access.

| Concern | HTTPS | SSH |
|---|---|---|
| Remote URL | `https://github.com/OWNER/REPO.git` | `git@github.com:OWNER/REPO.git` |
| Typical proof | credential manager browser sign-in or token | registered SSH public key and matching local private key |
| Secret kept locally | stored credential/token | private key, often protected with passphrase/agent |
| GitHub receives during setup | account authorization/token requests | public key |
| Beginner Windows path | Git for Windows + Git Credential Manager | configure SSH key and connection deliberately |
| Common failure | wrong cached account, expired token, insufficient access | wrong key, missing public key, agent/host verification problem |

For the simplest Windows HTTPS setup, use a current Git for Windows installation with Git Credential Manager. A protected operation can open a browser sign-in flow; GCM manages credentials without requiring you to hand-create a PAT. See [GitHub's credential-manager instructions](https://docs.github.com/en/get-started/git-basics/caching-your-github-credentials-in-git?platform=windows).

If manually using a Personal Access Token, grant only needed repository access and permissions, set an expiration, and supply it through the secure credential prompt/helper. GitHub account passwords are not supported as passwords for Git-over-HTTPS operations. Never put a PAT in a remote URL, example command, repository file, or chat. `git config --get credential.helper` inspects the helper; browser sign-in alone does not guarantee every Git client is using the same credential.

For SSH, create a key using [GitHub's SSH setup](https://docs.github.com/en/authentication/connecting-to-github-with-ssh), retain the private key locally, and register only the public key. Do not overwrite an existing key casually. Verify GitHub's host fingerprint through its official documentation before accepting a new host identity. `ssh -T git@github.com` tests identity; GitHub's success greeting also explains that shell access is unavailable, and a nonzero exit can accompany that successful authentication test.

Public HTTPS clones may need no authentication; pushing still requires write access. Organization SSO or repository rules can add authorization requirements. Diagnose using the actual error and remote URL, not by changing commit email.

## 51. Force push

**⭐⭐⭐ NICE TO KNOW — requires coordination**

A normal push guards against replacing a remote branch with a history that does not contain its current tip. `--force` bypasses that ancestry safeguard and can remove other people's work from the named branch. It changes the remote ref; old objects may exist elsewhere, but the shared branch no longer leads through them.

`--force-with-lease` adds an expectation about the remote's current value before allowing replacement. If someone updated it unexpectedly, the lease can reject. It is safer than unconditional force, but does not prove that the replacement retains all useful work or that anyone agreed to a rewrite.

Implicit leases often rely on remote-tracking refs; background fetching can change that knowledge. An explicit expected object ID is a more deliberate guard for an advanced, isolated exercise. Protected branches may reject either form. See the [lease discussion in Git's push documentation](https://git-scm.com/docs/git-push#Documentation/git-push.txt---force-with-lease).

Do not force push merely because a push was rejected. Fetch and inspect first. The companion's optional lease demonstration uses a separate disposable remote branch; required tasks do not rewrite hosted history.

## 52. Team workflow

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
LOCAL                            GITHUB
update main
  -> create feature
  -> edit, inspect, stage
  -> commit
  -> push feature -------------> branch available
                                 -> open PR
                                 -> review + checks
follow-up commits -------------> PR updates
                                 -> merge
fetch + integrate <------------ hosted main advanced
  -> delete finished local branch
```

A practical routine is to keep main clean, advance it with an explicit policy such as `pull --ff-only`, create a short-lived feature branch, and make coherent commits. Review staged content before each commit and the entire proposal before opening a PR. Push the feature, address feedback, and merge through the repository's chosen strategy.

Synchronize main after the hosted merge, then start the next feature from that updated base. Delete finished branch names after confirming integration. If a teammate changed main during your feature, fetch and compare; merge main into the feature or rebase private work according to team policy.

Long-lived branches accumulate integration risk. Small focused PRs make review and conflict resolution easier. None of this requires forking when you already have write access to the shared repository.

## 53. GitHub Actions and CI/CD

**⭐⭐ FUTURE KNOWLEDGE**

```text
push / pull_request event
          |
          v
workflow -> job -> steps such as build/test/lint
          |
          v
status/check result shown on commit and PR
```

A workflow is YAML stored under `.github/workflows/`. A runner executes jobs; job steps perform commands or reusable actions. CI checks whether changes integrate successfully. Continuous delivery/deployment adds later packaging and release/deployment decisions; a Git commit or PR merge is not itself that entire pipeline.

The optional lab workflow only prints a message on a GitHub-hosted Windows runner. It needs no deployment credentials and no application stack. A production Java workflow might later run the project's established build/tests and report results, with appropriate permissions and secret handling. Understand the [workflow syntax](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax) when you reach that stage. A green print-only job demonstrates execution, not software correctness.

## 54. Common mistakes

**⭐⭐⭐⭐⭐ MUST KNOW**

| Mistake | Why it misleads or harms | Better diagnostic/habit |
|---|---|---|
| “add uploads” | it only updates the local index | inspect `diff --staged` |
| “commit uploads” | local history and hosted history are independent | push an intended branch after reviewing it |
| “branch copies the project” | misses shared ancestry and pointer movement | draw refs pointing to commits |
| skipping status | loses track of branch and operation state | status before the next mutation |
| blind `add .` | includes accidental files or unrelated changes | inspect status and staged diff |
| committing secrets | exposes credentials through history/copies | fake values/templates; rotate after exposure |
| ignoring an already committed `.env` | does not remove prior versions or tracking | inspect `ls-files`; rotate and plan cleanup |
| treating `origin/main` as live GitHub | it is locally cached knowledge | fetch before remote comparisons |
| treating fetch as pull | download and integration affect different state | inspect refs before integrating |
| pulling blindly | can start an unintended merge/rebase | read graph, choose explicit integration policy |
| merging in the wrong direction | current branch receives the result | name current target before merge |
| just deleting conflict markers | may silently discard required behavior | decide final content and verify it |
| casual hard reset | can destroy uncommitted work | locate the change and select a narrower operation |
| force after rejection | can overwrite teammates' history | diagnose ancestry, access, and rules |
| rebasing shared history casually | recreates identities others rely on | coordinate or preserve history with merge |
| expecting revert to erase history | it adds an inverse commit | distinguish content correction from removal |
| calling a PR a Git object | confuses hosted review with local ancestry | separate commit graph from PR record |
| confusing clone and fork | local copies and hosted repositories differ | draw locations and URLs |
| assuming origin means GitHub | any repository URL can be a remote | inspect `remote -v` |
| committing `target/` or build output | creates noise and needless generated history | ignore reproducible output; track its inputs |
| giant unrelated commits | harder review, rollback, and attribution | stage a coherent purpose |
| meaningless messages | hides why a version exists | describe the actual change |
| weeks on one stale feature | raises integration and review cost | smaller increments and deliberate synchronization |
| random fixes during conflict | can mix operation states or discard evidence | status, understand, resolve/continue or abort |
| assuming clean means backed up | local clean can still be unpushed | inspect refs/upstream and use real backups |
| treating Git as a complete backup | untracked content and hosted discussions differ | know what each copy actually retains |

## 55. Complete workflow trace

**⭐⭐⭐⭐⭐ MUST KNOW**

Assume a fresh independent folder, then an empty GitHub repository, and use main as the default branch. The following is a state trace rather than a script to paste wholesale.

| Step | Operation | Local state change | GitHub state change |
|---|---|---|---|
| 1 | `git init -b main` | Git metadata created; HEAD names unborn main | none |
| 2 | create/edit README | working-tree file changes | none |
| 3 | `git status`; inspect file/diff | observe state; untracked file may need direct reading | none |
| 4 | `git add README.md` | index proposes file content | none |
| 5 | `git diff --staged` | observe index vs initial/HEAD snapshot | none |
| 6 | `git commit -m "Add introduction"` | root commit A created; main points to A | none |
| 7 | add origin; push main with `-u` | configure remote/upstream; matching tracking ref can update | hosted main points to A |
| 8 | `git switch -c feature/profile` | feature points to A; HEAD names feature | none |
| 9 | edit, inspect, stage, commit | commit B created; feature advances; main stays A | none |
| 10 | push feature with `-u` | establish tracking; matching cached ref updated | hosted feature points to B |
| 11 | open PR feature → main | no local Git mutation | review proposal created; main still A |
| 12 | review; follow-up commit if needed | optional new feature commit | pushed follow-up updates PR |
| 13 | Create a merge commit | local main and tracking refs may still be stale | merge M created with parents A and feature tip; main advances |
| 14 | fetch origin | objects/M and origin/main updated; local main stays A | none |
| 15 | switch main; `merge --ff-only origin/main` | main, index, and files advance to M | none |
| 16 | delete integrated local feature | local branch name removed | hosted feature remains unless separately deleted |

```text
after hosted merge and local synchronization:

A-----------M  main, origin/main <- HEAD names main
 \         /
  B-------/    feature tip if its name still exists
```

Checkpoints along this trace are content (`diff`), intent (`diff --staged`), ancestry (`log --graph`), operation state (`status`), and relationships (`branch -vv`, `remote -v`). Test the software where applicable; Git's graph cannot tell you whether the code works.

## Readiness and interview review

### One complete mental model

```text
Files I edit                    working tree
Snapshot I propose              index/staging area
Versions I record               commit objects connected by parents
Line I develop                  local branch reference
What I have checked out         HEAD, usually -> branch -> commit
Remote address/configuration    remote such as origin
Locally known remote tip        origin/main
Actual shared tip               GitHub's main
Review/integration proposal     GitHub Pull Request
```

### Compact comparison sheet

| Need | Think in these terms |
|---|---|
| save vs add vs commit vs push | edit disk → propose snapshot → record history → synchronize |
| working edit vs staged edit | working tree/index difference vs index/HEAD difference |
| branch vs commit vs tag | movable line name vs recorded object vs stable version label |
| main vs origin/main vs remote main | development tip vs cached knowledge vs hosted tip |
| fetch vs pull vs push | learn → learn and integrate → send/request remote update |
| restore vs reset vs revert | content/index selection → pointer/index manipulation → corrective commit |
| merge vs rebase | combine ancestry → recreate a sequence on a new base |
| clone vs fork | new local repository → new hosted repository relationship |
| tag vs release vs deployment | Git version label → hosted release presentation → operational rollout |

### Must-remember rules

- Predict the affected state before every state-changing command.
- `status` describes your current local comparisons and operation state.
- Staging records content at that moment; a later edit is separate.
- A commit records the index and advances the current branch; it does not upload.
- A branch is a reference. Existing commit objects are immutable.
- Merge direction is into the current branch.
- Textual integration success still needs behavior verification.
- Fetch updates ordinary remote knowledge without integrating into your current branch.
- Pull includes integration; choose the policy deliberately.
- Revert normally suits shared corrections; rewriting deserves coordination.
- Reflog helps recover locally recorded tips, not every lost file forever.
- Secrets need rotation after exposure, even if the latest file is removed.

### Practical workflow checklist

- [ ] Identify the repository root and current branch.
- [ ] Inspect status and both diffs.
- [ ] Fetch before relying on remote comparisons.
- [ ] Update main deliberately, then create a focused feature branch.
- [ ] Stage one coherent change and inspect its proposed snapshot.
- [ ] Commit with useful intent and verify appropriate behavior.
- [ ] Push the intended branch and inspect its PR base/head.
- [ ] Review changes and check results; address feedback.
- [ ] Merge using repository policy; synchronize local main afterward.
- [ ] Delete finished names after confirming integration.

### Readiness self-check

You are ready for everyday use when you can predict the lab's states without following its walkthrough, resolve its deliberate conflict, explain a rejected push without forcing, and recover a temporarily lost commit by first naming it safely. Complete the companion's core tasks to turn these statements into evidence.

### Interview questions — answer without notes

1. What problem does Git solve, and why are folder copies inadequate?
2. How is Git different from GitHub?
3. What is a repository, and how do working files differ from `.git`?
4. What do the working tree, index, and repository each represent?
5. Why does staging exist, and can a file be staged and modified simultaneously?
6. What is a commit, and what does it reference?
7. What changes a commit ID? How does the object model represent a snapshot?
8. What does HEAD point to normally, and before a first commit?
9. What is a branch, and which branch advances when you commit?
10. How do `branch`, `switch`, and `switch -c` differ?
11. What do ordinary diff and staged diff compare?
12. What distinguishes a fast-forward from a merge commit?
13. Which direction does `git merge feature/profile` integrate?
14. What is a three-way merge, and why can it conflict?
15. What are the steps to resolve or abort an in-progress merge?
16. What does `.gitignore` do for a file already tracked or a secret already exposed?
17. How do restore, reset, and revert solve different undo problems?
18. What state does each reset mode preserve or overwrite?
19. Why is revert usually preferable for a shared bad commit?
20. What does amend do, and how should you judge commit scope?
21. What do stash apply and pop retain, and what can conflict?
22. What can reflog recover, and what are its limits?
23. What is cherry-pick, and when would merge be clearer?
24. What does rebase recreate, and why does shared history matter?
25. What is detached HEAD, and how do you preserve work made there?
26. What are origin, origin/main, main, and a branch's upstream?
27. How do fetch, pull, push, clone, and fork differ?
28. How do a Git tag, GitHub Release, Issue, and PR differ?
29. How do review, merge strategies, rulesets, and Actions fit around Git history?
30. How do authentication and commit identity differ, and why is force pushing dangerous?

```text
============================================================
STOP — ANSWER THE QUESTIONS BEFORE OPENING THE ANSWER KEY
============================================================
```

<details>
<summary>Interview answer key</summary>

1. Git records versions and ancestry for comparison, parallel development, integration, and recovery. Folder names cannot explain relationships or intent reliably.
2. Git manages version history; GitHub hosts repositories and adds collaboration and automation. Local Git works offline.
3. A repository contains recorded objects and refs. Working files are editable checkouts; `.git` normally stores history and local bookkeeping.
4. The working tree is current editable content, the index is the proposed next snapshot, and history consists of recorded objects and their relationships.
5. Staging selects coherent content independently of all current edits. A later edit can leave index vs HEAD and working tree vs index both different.
6. A commit references a tree, parent(s), author/committer metadata, and message. It records the index, not every working edit.
7. Changing tree, parents, metadata, or message changes identity. Trees name blobs/subtrees; commits connect snapshots through parents.
8. HEAD normally symbolically names a local branch; initially that branch can be unborn. Detached HEAD names a commit directly.
9. A branch is a movable ref. Only the current branch normally advances on commit; other names keep their tips.
10. `branch name` creates without switching; `switch name` selects; `switch -c name` creates and selects.
11. Ordinary diff compares index to working tree; staged diff compares HEAD to index. Untracked content usually needs separate inspection.
12. A fast-forward advances to an existing descendant. A merge commit records combined ancestry with multiple parents.
13. Into the current branch. Switch to the intended target first.
14. Compare base, current, and incoming changes. Incompatible edits or structural changes require a human decision.
15. Inspect status, decide/edit final files, remove markers, verify, add, finish the merge. Or abort the still-running merge, ideally from a clean starting state.
16. Ignore rules affect untracked eligibility. They do not untrack existing files, erase old commits, or revoke credentials.
17. Restore copies selected content into files/index; reset changes index and/or branch position; revert adds an inverse commit.
18. Soft keeps index/files; mixed resets index and keeps files; hard resets index and overwrites working content toward the target. Commit-form modes move the tip.
19. Revert appends a correction while preserving the ancestry collaborators already have.
20. Amend replaces the tip with a new commit. Keep one coherent purpose and inspect all staged content before using it.
21. Apply retains the stash; successful pop removes it. Both can conflict against changed destination content; default application need not restore index state.
22. Reflog records local ref movements and can locate an old tip. It expires, is not ordinarily shared, and is not an editor backup.
23. Cherry-pick replays one commit's change. Merge preserves a whole line's ancestry and is clearer when all its work belongs together.
24. Rebase rebuilds selected commits on another base, changing identities. Rewriting commits others use creates history disagreement.
25. HEAD directly names a commit. Create a branch at useful detached work, or recover its hash through reflog after leaving.
26. Origin is a remote configuration; origin/main a local remote-state record; main a local branch. Its upstream is a configured counterpart, distinct from a remote named upstream.
27. Fetch learns; pull learns and integrates; push sends committed reachable history and requests ref updates; clone creates a local copy; fork creates a hosted repository relationship.
28. A tag labels an object/version. A Release presents a tag with notes/assets. An Issue tracks work. A PR proposes and reviews branch integration.
29. Review/checks assess a proposal; merge strategy shapes ancestry; rules gate hosted updates; Actions runs event-triggered jobs. These do not turn PRs into Git objects.
30. Name/email identify commits; credentials prove remote access. Force can replace a shared tip and hide others' commits from the branch; a lease adds an expectation check, not team consent.

</details>

The practical question to carry into the [companion lab](Git_GitHub_Mini_Project.md) is: **What state do I have, what state do I want, and which operation changes only the state I intend?**
