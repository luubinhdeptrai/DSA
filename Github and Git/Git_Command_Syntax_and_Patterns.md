# Git Command Syntax and Patterns — A Beginner's Guide

> **Goal:** Understand how Git commands are written so you can read unfamiliar commands instead of memorizing them.
>
> **Environment:** Git CLI in Windows PowerShell. Most syntax conventions also apply to other command-line tools, although each tool defines its own options and arguments.

## 1. English terminology

A natural way to ask about this topic is:

> **Could you explain the syntax of Git commands, including single hyphens (`-`), double hyphens (`--`), options, arguments, and key-value pairs?**

| Term | Part of speech | Meaning |
|---|---|---|
| **Syntax** | Noun | The rules describing how a command is written. |
| **Option** | Noun | A setting that modifies a command's behavior. Often called a *flag*. |
| **Argument** | Noun | A value given to a command or option. |
| **Positional argument** | Noun phrase | An argument whose position helps determine its meaning. |
| **Key-value pair** | Noun phrase | A setting name (key) and the value assigned to it. |
| **Abbreviation** | Noun | A shortened form of a word or phrase. |

---

## 2. General structure of Git commands

A common pattern is:

```text
git <subcommand> [options] [arguments]
```

- `git`: the program being executed.
- `<subcommand>`: the action, such as `commit`, `status`, `log`, or `push`.
- `[options]`: optional settings that modify the action.
- `[arguments]`: optional or required targets, depending on the command.

**Example:**

```powershell
git commit -m "Fix authentication bug"
```

| Component | Type | Meaning |
|---|---|---|
| `git` | Program | Run Git. |
| `commit` | Subcommand | Record a new commit. |
| `-m` | Short option | Short for `--message`; supply a commit message. |
| `"Fix authentication bug"` | Option value | The message associated with the commit. |

**Note:** A real command does not always follow exactly this order. Some Git commands accept options and arguments in different positions; consult the help for that specific command.

---

## 3. Single hyphen (`-`) — Short options

A **short option** typically starts with a single hyphen followed by one letter.

```powershell
git commit -m "Initial commit"
```

Here, `-m` is shorthand for `--message`.

The equivalent long form is:

```powershell
git commit --message "Initial commit"
```

- `-m`: short option.
- `--message`: full option name.
- `"Initial commit"`: value supplied to the option.

**Remember:** Option names are defined by each subcommand. The same letter can mean different things in different commands, and not every short option has a matching long form.

---

## 4. Double hyphen plus a name (`--name`) — Long options

A **long option** usually begins with two hyphens followed by a descriptive name.

```powershell
git status --short
```

This also has a short form:

```powershell
git status -s
```

Here, `-s` corresponds to `--short`.

### Short and long options you will encounter

| Command and short option | Corresponding long option | Meaning |
|---|---|---|
| `git commit -m` | `--message` | Specify the commit message. |
| `git init -b` | `--initial-branch` | Name the initial branch. **`b` is a useful mnemonic for “branch.”** |
| `git switch -c` | `--create` | Create a branch and switch to it. |
| `git push -u` | `--set-upstream` | Configure the upstream tracking relationship. |
| `git remote -v` | `--verbose` | Show more detail. |
| `git status -s` | `--short` | Produce concise output. |
| `git log -n` | `--max-count` | Limit the number of commits displayed; remember `n` as “number.” |

**Important:** A short option's meaning can vary by subcommand. For example, `git checkout -b <branch>` uses `-b` to create and check out a branch, while `git init -b <branch>` uses `-b` to name a repository's initial branch. The specific long option `--initial-branch` belongs to **`git init`**, not every command using `-b`.

---

## 5. Options with values vs. options without values

### Type A: Option without a separate value

```powershell
git status --short
```

`--short` simply changes the output format. You do not supply another value.

### Type B: Option that requires a value

```powershell
git log --max-count=5
```

| Part | Meaning |
|---|---|
| `--max-count` | Option name: maximum number of commits. |
| `=` | Separator between an option and its value. |
| `5` | Value. |

For this particular option, both forms are valid:

```powershell
git log --max-count=5
git log --max-count 5
```

Or use the short option:

```powershell
git log -n 5
```

All three commands request at most five commits.

**Remember:** Support for `--option=value` and `--option value` varies by command and option. Do not assume every CLI supports both forms.

---

## 6. Positional arguments

An **argument** is not necessarily an option. Positional arguments specify *what* a command operates on.

```powershell
git push origin main
```

| Component | Type | Purpose |
|---|---|---|
| `git` | Program | Run Git. |
| `push` | Subcommand | Send commits and request a remote reference update. |
| `origin` | Positional argument | Remote name. |
| `main` | Positional argument | Branch/refspec to push. |

Compare with:

```powershell
git push -u origin main
```

Here:

- `-u` means `--set-upstream`.
- `origin` identifies the remote.
- `main` identifies the branch/refspec.

The option changes **how** Git performs the operation; positional arguments identify **where or what** it acts upon.

---

## 7. The standalone `--` separator

**Very important:** `--short` and `--` are **not the same thing**.

| Syntax | Meaning |
|---|---|
| `--short` | A named long option. |
| `--` | A separator, often marking the end of options/revisions and the beginning of file paths. |

### Example 1: Filter commit history by file

```powershell
git log -- README.md
```

Read this as:

> Show Git history concerning the file `README.md`.

### Example 2: Compare the current commit with a working file

```powershell
git diff HEAD -- README.md
```

| Component | Meaning |
|---|---|
| `git diff` | Compare content. |
| `HEAD` | Current checked-out commit (normally via the current branch). |
| `--` | Separate the revision part from the path part. |
| `README.md` | File path to compare. |

`--` is useful when a file path could otherwise be confused with a revision or an option. It is common in Git commands that accept revisions and paths.

---

## 8. Keys and values in `git config`

Git configuration uses **keys** and **values**.

```powershell
git config --global user.name "John"
```

| Component | Type | Meaning |
|---|---|---|
| `git` | Program | Git. |
| `config` | Subcommand | Read or write Git configuration. |
| `--global` | Option | Use the user's global Git configuration scope. |
| `user.name` | Configuration key | Name of the setting. |
| `"John"` | Value | Value assigned to that setting. |

Another example:

```powershell
git config --global user.email "john@example.com"
```

Conceptually:

```text
key        = value
--------------------------
user.name  = John
user.email = john@example.com
```

**Important:** `user.name` is a **configuration key**, not a `--name` option.

---

## 9. Analyze one complete Git command

```powershell
git log --oneline --max-count=5 -- src/BookService.java
```

| Part | Role | Meaning |
|---|---|---|
| `git` | Program | Run Git. |
| `log` | Subcommand | Show commit history. |
| `--oneline` | Long option | Show each commit in a compact one-line format. |
| `--max-count=5` | Long option with value | Show up to five commits. |
| `--` | Separator | The following argument is a path. |
| `src/BookService.java` | Path argument | Limit history to the specified file. |

**Full meaning:**

> Show up to five commits related to `src/BookService.java`, with one line per commit.

---

## 10. Quick reference table

| Syntax | Meaning | Example |
|---|---|---|
| `-x` | Short option | `git status -s` |
| `--name` | Long option | `git status --short` |
| `--name=value` | Long option with a value | `git log --max-count=5` |
| `-x value` | Short option followed by a value | `git commit -m "Fix bug"` |
| `--` | End-of-options / revision-path separator | `git log -- README.md` |
| `key value` | Key followed by value in an appropriate configuration command | `git config user.name "John"` |
| `.` | Current directory as a path argument | `git add .` |
| `<VALUE>` | Placeholder in documentation | `git clone <URL>` |
| `[option]` | Optional element in documentation | `git status [--short]` |

### Reading documentation notation

These characters mean something **in documentation**, not necessarily in a command you paste:

```text
git clone <URL>
git status [--short]
```

- `<URL>`: replace it with the actual repository URL; do **not** type the angle brackets in PowerShell.
- `[--short]`: the bracketed option is optional; type `--short` only if you want it, without brackets.

### Useful reminders

- `.` is a path argument in `git add .`, **not** a short option.
- `=` is used in certain long options to supply a value.
- Quotation marks group words that belong to one argument, such as a commit message with spaces.
- `--` alone is different from a named long option like `--short`.
- `-s` is **not universally** “short”: meanings depend on the command.

---

## 11. Mental model

```text
git commit -m "Fix bug"
│   │      │      │
│   │      │      └── Option value
│   │      └───────── Short option (-m = --message)
│   └──────────────── Subcommand
└──────────────────── Program
```

Remember:

- **`-x`** usually introduces a short option.
- **`--name`** introduces a descriptive long option.
- **`--` alone** separates command options/revisions from what follows (commonly file paths).
- **`=`** can assign a value to an option.
- **Arguments** tell a command what to operate on.
- **Configuration keys** such as `user.name` name settings; they are not CLI options.

> **Core principle:** These are common conventions in Git, Docker, Maven, and other command-line tools, but each tool defines which options, values, and argument formats are valid. When unsure, check that command's help rather than assuming a flag has the same meaning everywhere.

## 12. Practice: Read these commands aloud

Try identifying the program, subcommand, options, full option names, and arguments before looking them up.

```powershell
git init -b main
git switch -c feature/login
git commit -m "Add login"
git status -s
git remote -v
git log -n 5
git push -u origin main
git log -- README.md
```

| Command | Key interpretation |
|---|---|
| `git init -b main` | Initialize a repository with initial branch `main`; `-b = --initial-branch`. |
| `git switch -c feature/login` | Create and switch to a branch; `-c = --create`. |
| `git commit -m "Add login"` | Commit with a message; `-m = --message`. |
| `git status -s` | Show short status; `-s = --short`. |
| `git remote -v` | Show verbose remote information; `-v = --verbose`. |
| `git log -n 5` | Display up to five commits; `-n = --max-count`. |
| `git push -u origin main` | Push to origin and set upstream; `-u = --set-upstream`. |
| `git log -- README.md` | Show commits affecting README.md; `--` separates the path. |
