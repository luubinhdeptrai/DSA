# Java Exception Exercise — Code Review & Feedback

## 1. Overall Result

**Score: 78/100**
**Grade: Good**

## 2. Executive Summary

Your understanding of the core exception vocabulary — `throw` vs `throws`, checked vs unchecked, custom exceptions, and the difference between a method that *handles* an exception and one that *propagates* it — is solid and correctly implemented everywhere it matters at the method level (`BankAccount`, `InsufficientBalanceException`, `TransactionParser`). The project compiles cleanly and runs without crashing.

The main weakness is in `Main.java`: you wrapped the `try`/`catch` around the **entire loop** instead of around **each individual transaction**, which the exercise's own Task 7 and expected output (Section 7) require. I compiled and ran your code to confirm the actual consequence: your deposit loop silently skips the `"-20"` test case entirely, and your withdrawal loop's `finally` block fires once instead of twice. This is not a conceptual misunderstanding of exceptions — it's a scoping mistake that happens to be exactly what the exercise was designed to test in Task 7. That, plus some missing confirmation output, is what separates this from a 90+ score.

## 3. Original Exercise Requirements Checklist

| Task | Requirement | Status |
|---|---|---|
| 1 | `BankAccount` skeleton: private `balance`, constructor, `getBalance()` | ✅ Done |
| 2 | `validateAmount()` throws `IllegalArgumentException` for `amount <= 0` | ✅ Done |
| 3 | `deposit()` calls `validateAmount()`, no internal try-catch | ✅ Done |
| 4 | `InsufficientBalanceException extends Exception`, `super(message)` | ✅ Done |
| 5 | `withdraw()` declares `throws InsufficientBalanceException`, doesn't catch internally | ✅ Done |
| 6 | `TransactionParser.parseAmount()` catches `NumberFormatException` locally, returns `0.0` | ✅ Done |
| 7 | `Main` wires everything with per-operation try-catch + `finally` | ⚠️ Structurally wrong — see [Issue 1](#issue-1-critical--trycatch-wraps-the-whole-loop-instead-of-each-transaction) |
| 8 | Actual output matches expected output in Section 7 | ❌ Does not match — see [Section 6](#6-test-case-results) |

## 4. Project Structure Review

```
bankaccount/
├── BankAccount.java
├── InsufficientBalanceException.java
├── TransactionParser.java
└── Main.java
```

All four required files are present and each contains exactly the class the exercise asked for — no extra/unrequired files. You added `package bankaccount;` to every file, which the starter code and reference solution didn't have. This is a valid, common Java practice (not a deviation worth penalizing) and I confirmed it compiles correctly given your folder is literally named `bankaccount`.

One minor note: the directory also had committed `.class` files sitting alongside the `.java` sources. Not a correctness issue, just something you'd normally `.gitignore` rather than keep in the project folder.

## 5. Compilation & Runtime Review

I compiled your code fresh with `javac -d out bankaccount/*.java` (JDK 21) — **zero errors, zero warnings**. Then ran it with `java -cp out bankaccount.Main`. Actual output:

```
Parse money SUCCESSFULLY!
Parse money UNSUCCESSFULLY. Exception is: For input string: "abc"
Amount must not be less than or equal 0
Parse money SUCCESSFULLY!
Parse money SUCCESSFULLY!
Not enough BALANCE
Withdrawal attempt finished.
120.0
```

No compilation problems. All checked-exception rules are satisfied correctly — this is exactly what you'd expect since `withdraw()` correctly declares `throws InsufficientBalanceException`.

## 6. Test Case Results

| Input | Expected (Section 7) | Actual | Verdict |
|---|---|---|---|
| deposit `"50"` | `Deposited 50.0. New balance: 150.0` | *(nothing printed)* | ❌ missing success message |
| deposit `"abc"` | invalid-format msg + `Deposit failed: ... 0.0` | `Parse money UNSUCCESSFULLY...` + `Amount must not be less than or equal 0` | ⚠️ present but wording differs, and this exception **aborts the rest of the deposit loop** |
| deposit `"-20"` | `Deposit failed: Amount must be greater than zero: -20.0` | **never runs** | ❌ this test case is silently skipped |
| withdraw `"30"` | `Withdrew 30.0. New balance: 120.0` + `Withdrawal attempt finished.` | *(no "Withdrew" line)* | ❌ missing success message |
| withdraw `"500"` | `Withdrawal failed: ...` + `Withdrawal attempt finished.` | `Not enough BALANCE` + `Withdrawal attempt finished.` | ⚠️ present but `finally` only fired **once total** for the loop, not once per attempt |
| Final balance | `120.0` | `120.0` | ✅ matches (coincidentally — see below) |

The final balance matching is a coincidence, not evidence the flow is correct: `"-20"` would have failed validation anyway even if it *had* run, so skipping it happened not to change the final number. If a later hidden test case depended on a deposit *after* the skipped one, this bug would silently corrupt the result.

## 7. Score Breakdown

```
Exercise requirements:            13 / 20
Exception concepts:                22 / 25
Checked/unchecked understanding:   14 / 15
throw / throws / propagation:      14 / 15
try/catch/finally:                  5 / 10
Code correctness:                   6 / 10
Code quality/readability:           4 / 5
---------------------------------------------
Total:                             78 / 100
```

- **Exercise requirements (13/20):** Tasks 1–6 fully correct. Task 7's structure is wrong (loop-level try instead of per-operation try), and Task 8's "match the expected output" is not satisfied (missing success lines, missing test case, wrong `finally` count).
- **Exception concepts (22/25):** Strong grasp of checked vs. unchecked, custom exceptions, and propagation vs. local handling. Small deduction for exception messages that don't carry contextual data (amount/balance).
- **Checked/unchecked understanding (14/15):** Textbook-correct application — no unnecessary `throws`, no missing `throws`.
- **throw/throws/propagation (14/15):** Every `throw` and `throws` in the project is placed correctly and for the right reason.
- **try/catch/finally (5/10):** Syntax is correct, but the *scope* is wrong in both loops in `Main.java`, which is the specific thing Task 7 was testing. This is the single biggest deduction in the review.
- **Code correctness (6/10):** Program runs and doesn't crash, but its behavior diverges materially from the specified behavior (dropped test case, missing output, wrong `finally` count).
- **Code quality/readability (4/5):** Clean, consistent, no duplication (validation logic correctly shared). `ac` as a variable name is a bit terse compared to `account`, minor.

## 8. What You Did Well

**You correctly built a checked custom exception matching the exact required shape.**
`InsufficientBalanceException` extends `Exception` (not `RuntimeException`) and its constructor calls `super(message)` — [InsufficientBalanceException.java:3-8](bankaccount/InsufficientBalanceException.java#L3-L8). This is precisely Task 4, and shows you understand that "checked" means extending `Exception` directly.

**`withdraw()` propagates the checked exception instead of catching it internally.**
```java
public void withdraw (double amount) throws InsufficientBalanceException
```
[BankAccount.java:24](bankaccount/BankAccount.java#L24) declares the exception on the signature and never catches it inside the method body. This demonstrates real understanding of Case 2 propagation — the method recognizes it doesn't have enough context to decide what the caller should do about an overdraft, so it hands the decision upward.

**`validateAmount()` throws an unchecked exception without a `throws` clause, and both callers let it propagate untouched.**
Neither `deposit()` nor `withdraw()` wraps the call to `validateAmount()` in a try-catch ([BankAccount.java:19](bankaccount/BankAccount.java#L19), [BankAccount.java:26](bankaccount/BankAccount.java#L26)), and neither declares `throws IllegalArgumentException`. This is exactly correct: unchecked exceptions are never compiler-enforced, and you clearly know that.

**`TransactionParser.parseAmount()` fully handles its own exception and never lets it escape.**
The `catch (NumberFormatException e)` block at [TransactionParser.java:14](bankaccount/TransactionParser.java#L14) prints a message and returns a safe default — `Main` never sees a `NumberFormatException`, which I confirmed matches the actual runtime output. This is a correct implementation of Case 1 (local handling).

**Main uses specific catch types instead of a single broad `catch (Exception e)`.**
[Main.java:33-40](bankaccount/Main.java#L33-L40) has two separate, specific catch blocks for `IllegalArgumentException` and `InsufficientBalanceException`. This is good practice the exercise explicitly calls out in Section 13 — you didn't take the lazy shortcut of catching everything with one generic type.

**Shared validation logic is not duplicated.**
Both `deposit()` and `withdraw()` call the same private `validateAmount()` rather than re-implementing the `<= 0` check twice — exactly the hint given in Section 9 of the exercise.

## 9. Problems / Mistakes

### Issue 1 (Critical) — try/catch wraps the whole loop instead of each transaction

**Location:** [Main.java:12-24](bankaccount/Main.java#L12-L24) and [Main.java:25-45](bankaccount/Main.java#L25-L45)

**My code:**
```java
try
{
    for (String s : withdrawInputs)
    {
        double amount = TransactionParser.parseAmount(s);
        ac.withdraw(amount);
    }
}
catch (IllegalArgumentException e) { ... }
catch (InsufficientBalanceException e) { ... }
finally { System.out.println("Withdrawal attempt finished."); }
```

**Problem:** The `for` loop is *inside* the `try` block, so the moment any single iteration throws, control jumps straight to the matching `catch`, abandoning every remaining loop iteration. I confirmed this by running the program: the deposit `"-20"` input never appears anywhere in the output — it's silently never attempted, because the `"abc"` iteration right before it threw and killed the rest of the loop.

**Why it's wrong:** Task 7 says to loop over the inputs, calling `deposit`/`withdraw` *"inside a try-catch"* per attempt — the reference solution puts `try` inside the `for` body, once per iteration, precisely so one bad transaction doesn't cancel the rest. A batch of independent bank transactions shouldn't all fail because the third one had a typo.

**Correct approach:** Move the `try`/`catch`/`finally` inside the loop body.

**Improved code:**
```java
for (String s : withdrawInputs)
{
    double amount = TransactionParser.parseAmount(s);
    try
    {
        ac.withdraw(amount);
    }
    catch (IllegalArgumentException e)
    {
        System.out.println("Withdrawal failed: " + e.getMessage());
    }
    catch (InsufficientBalanceException e)
    {
        System.out.println("Withdrawal failed: " + e.getMessage());
    }
    finally
    {
        System.out.println("Withdrawal attempt finished.");
    }
}
```
Same restructure applies to the deposit loop.

### Issue 2 (Important) — missing confirmation output on success

**Location:** [BankAccount.java:17-22](bankaccount/BankAccount.java#L17-L22) (`deposit`), [BankAccount.java:24-33](bankaccount/BankAccount.java#L24-L33) (`withdraw`)

**My code:**
```java
public void deposit (double amount)
{
    validateAmount(amount);
    balance += amount;
}
```

**Problem:** Neither method prints anything when the operation succeeds, and `Main` doesn't either. Section 7's expected output explicitly includes `"Deposited 50.0. New balance: 150.0"` and `"Withdrew 30.0. New balance: 120.0"` — confirmed missing in your actual runtime output.

**Why it matters:** Task 8 asks you to confirm your output matches Section 7. Right now, a successful transaction is completely silent, so there's no way to tell from the console that it happened.

**Correct approach:**
```java
public void deposit (double amount)
{
    validateAmount(amount);
    balance += amount;
    System.out.println("Deposited " + amount + ". New balance: " + balance);
}

public void withdraw (double amount) throws InsufficientBalanceException
{
    validateAmount(amount);
    if (amount > this.balance)
    {
        throw new InsufficientBalanceException(
            "Cannot withdraw " + amount + ". Current balance is only " + balance);
    }
    balance -= amount;
    System.out.println("Withdrew " + amount + ". New balance: " + balance);
}
```

### Issue 3 (Minor) — exception messages lose context

**Location:** [BankAccount.java:29](bankaccount/BankAccount.java#L29) and [BankAccount.java:39](bankaccount/BankAccount.java#L39)

⚠️ Works but should be improved. `"Not enough BALANCE"` and `"Amount must not be less than or equal 0"` are fixed strings that don't include the actual `amount`/`balance` values involved. This isn't a conceptual exception-handling error, but in practice it makes the message far less useful for debugging or for a caller deciding what to show a user. Prefer string-interpolating the real values, e.g. `"Cannot withdraw " + amount + ". Current balance is only " + balance`.

### Issue 4 (Style) — output text diverges from the spec's expected output

**Location:** [Main.java:20-23](bankaccount/Main.java#L20-L23), [Main.java:33-40](bankaccount/Main.java#L33-L40), [TransactionParser.java:11](bankaccount/TransactionParser.java#L11)

💡 Optional improvements: no `"--- Deposits ---"` / `"--- Withdrawals ---"` section headers, no `"Deposit failed: "` / `"Withdrawal failed: "` prefixes on caught messages, and `parseAmount()` prints an extra `"Parse money SUCCESSFULLY!"` line on every successful parse that isn't in the expected output at all. None of these break exception-handling correctness, but they mean your console output doesn't line up with Section 7 even where the logic itself is fine.

## 10. Checked vs. Unchecked Exception Review

| Exception | Checked/Unchecked | Where thrown | Where handled | Correct? |
|---|---|---|---|---|
| `InsufficientBalanceException` | Checked (`extends Exception`) | [BankAccount.java:29](bankaccount/BankAccount.java#L29) | Main withdrawal catch block ([Main.java:37-40](bankaccount/Main.java#L37-L40)) | ✅ classification & declaration correct; ⚠️ handling *scope* has the [Issue 1](#issue-1-critical--trycatch-wraps-the-whole-loop-instead-of-each-transaction) bug |
| `IllegalArgumentException` | Unchecked (built-in `RuntimeException`) | [BankAccount.java:39](bankaccount/BankAccount.java#L39), called from both `deposit()` and `withdraw()` | Main deposit catch block ([Main.java:20-23](bankaccount/Main.java#L20-L23)) | ✅ correctly never declared with `throws`, correctly caught only where needed |
| `NumberFormatException` | Unchecked (`RuntimeException` subclass) | `Double.parseDouble()` inside [TransactionParser.java:10](bankaccount/TransactionParser.java#L10) | Caught locally at [TransactionParser.java:14](bankaccount/TransactionParser.java#L14) | ✅ correct — never escapes to `Main`, verified by actual output |

You correctly never wrote a `throws IllegalArgumentException` or `throws NumberFormatException` anywhere — showing you understand these don't require compiler-enforced declaration.

## 11. throw vs. throws Review

**`throw` usages (both correct):**
- [BankAccount.java:29](bankaccount/BankAccount.java#L29) — `throw new InsufficientBalanceException("Not enough BALANCE");`
- [BankAccount.java:39](bankaccount/BankAccount.java#L39) — `throw new IllegalArgumentException("Amount must not be less than or equal 0");`

Both are statements inside a method body that construct and immediately raise a specific exception instance under the correct business condition.

**`throws` usage (correct, and correctly *absent* where it shouldn't be):**
- [BankAccount.java:24](bankaccount/BankAccount.java#L24) — `public void withdraw(double amount) throws InsufficientBalanceException` — required because the exception is checked.
- `deposit()` has **no** `throws` clause even though it calls `validateAmount()`, which can throw `IllegalArgumentException` — correct, because that exception is unchecked and the compiler never requires it.

No confusion between `throw` and `throws` anywhere in the project, and no unnecessary `throws` declarations.

## 12. Exception Propagation Review

**Checked exception — actual trace for `withdraw("500")`:**
```
Main.main()
   │  for-loop (Main.java:27-31), iteration s="500", inside try (Main.java:25)
   ▼
TransactionParser.parseAmount("500") → parses fine, returns 500.0
   ▼
ac.withdraw(500.0)                      [BankAccount.java:24]
   │  validateAmount(500.0) → 500 > 0, passes
   │  amount(500.0) > balance(120.0) → true
   ▼
throw new InsufficientBalanceException("Not enough BALANCE")   [BankAccount.java:29]
   │  withdraw() has no catch — only declares "throws InsufficientBalanceException"
   ▼
exception exits withdraw(), exits the for-loop (loop is INSIDE try)
   ▼
Main catch (InsufficientBalanceException e)   [Main.java:37-40] → prints "Not enough BALANCE"
   ▼
Main's finally   [Main.java:41-44] → prints "Withdrawal attempt finished." — ONCE for the
whole loop, not once per attempt, because finally belongs to the loop-wide try, not each iteration
```

**Unchecked exception — actual trace for `deposit("abc")`:**
```
Main.main()
   │  for-loop (Main.java:14-18), iteration s="abc", inside try (Main.java:12)
   ▼
TransactionParser.parseAmount("abc")
   │  Double.parseDouble("abc") throws NumberFormatException  [TransactionParser.java:10]
   │  caught locally  [TransactionParser.java:14] → prints failure msg → returns 0.0
   ▼
Main receives amount = 0.0   (NumberFormatException never reaches Main — correct)
   ▼
ac.deposit(0.0)                          [BankAccount.java:17]
   │  validateAmount(0.0) → 0 <= 0 → true
   ▼
throw new IllegalArgumentException("Amount must not be less than or equal 0")  [BankAccount.java:39]
   │  deposit() has no throws clause (correctly, since it's unchecked) and no internal catch
   ▼
exception propagates out of deposit(), out of the for-loop
   ▼
Main catch (IllegalArgumentException e)   [Main.java:20-23] → prints the message
   ▼
"-20" is NEVER attempted — the loop was abandoned before reaching it
```

Both traces confirm your propagation *mechanics* (throw site, no internal catch, matching catch at the call site) are correct — the only defect is where the loop boundary sits relative to the try block.

## 13. try / catch / finally Review

Syntax is correct throughout: proper brace placement, correct catch parameter types, `finally` with no parentheses, placed after the last `catch`. Execution-wise:

- **Success case:** if `withdraw()` succeeds, no `catch` runs, `finally` still runs (verified: `"Withdrawal attempt finished."` appears after the try block regardless of outcome).
- **Exception case:** the matching `catch` runs, then `finally` runs.

You clearly know *that* `finally` always runs. What's not yet solid is *at what granularity* — because your `try` spans the whole loop rather than one operation, `finally` ends up meaning "runs once no matter what happened to the whole batch" instead of "runs once per attempted withdrawal," which is what the exercise's expected output (two `"Withdrawal attempt finished."` lines) requires.

## 14. Custom Exception Review

`InsufficientBalanceException`:
- Extends `Exception` directly → correctly checked. ✅
- Constructor takes `String message`, calls `super(message)` → exactly matches the reference. ✅
- Thrown under the correct business condition (`amount > balance`) in `withdraw()`. ✅
- Caller (`Main`) catches and handles it; nothing further to propagate to since `Main` is the entry point. ✅

No structural issues here — the only weakness is the generic message text (see [Issue 3](#issue-3-minor--exception-messages-lose-context)), which is a usability point, not a conceptual one.

## 15. Code Quality Review

- **Naming:** clear class/method names; `ac` for the account variable is a bit terse next to `account`, but not confusing.
- **Duplication:** none — validation logic is correctly centralized in `validateAmount()` and reused by both `deposit()` and `withdraw()`.
- **Encapsulation:** `balance` is private with only a getter exposed; `validateAmount()` is correctly private (internal helper, not part of the public API).
- **Readability:** consistent Allman-brace style throughout; no dead code, no unnecessary comments.
- **Method responsibility:** each method does one thing — `validateAmount` validates, `deposit`/`withdraw` mutate balance, `parseAmount` parses. Good separation.

## 16. Comparison with Reference Solution

| Area | My implementation | Reference | Evaluation |
|---|---|---|---|
| Package declaration | `package bankaccount;` in all files | none (default package) | ✅ correct alternative |
| Custom exception structure | Identical | Identical | ✅ matches exactly |
| `validateAmount()` message | Fixed generic text | Includes actual `amount` | ⚠️ works but less informative |
| Deposit/withdraw success output | None | Prints confirmation line | ❌ required by Task 8's expected output |
| Loop + try/catch structure | try/catch wraps entire loop | try/catch inside loop, per iteration | ❌ causes early loop termination |
| `InsufficientBalanceException` message | Generic `"Not enough BALANCE"` | Includes `amount` and `balance` | ⚠️ works but should be improved |
| `parseAmount()` success case | Prints extra success line | Silent on success | 💡 harmless but diverges from expected output text |
| `throw`/`throws` placement & mechanics | Same as reference | Same | ✅ correct, matches exactly |

## 17. Concepts Clearly Understood

- Difference between checked and unchecked exceptions, applied correctly in both the custom exception and the built-in ones.
- `throw` vs `throws` mechanics — used correctly and consistently, no confusion anywhere in the project.
- Writing a custom checked exception (`extends Exception` + `super(message)`).
- Local handling vs. propagation — `TransactionParser` handles locally, `withdraw()` propagates, both implemented correctly.
- Using specific catch types instead of one broad `catch (Exception e)`.

## 18. Concepts Partially Understood

- **try/catch scope discipline** — you know the syntax and that `finally` always runs, but placed `try` around an entire loop of independent operations instead of around a single operation. This suggests the "wrap only the risky statement, as narrowly as possible" principle isn't fully internalized yet, even though nothing here violates a compiler rule.

## 19. Concepts That Need Review

- **Verifying output against a spec.** Task 8 explicitly asks you to confirm your output matches Section 7 line-by-line; running the program would have surfaced the missing `"-20"` case and the missing success messages immediately.
- **Batch error resilience** — understanding why, when processing a list of independent transactions, one failure should not silently cancel the ones that come after it.

## 20. Corrected Code for Important Issues

**`Main.java` — restructured loops (fixes Issue 1):**
```java
System.out.println("--- Deposits ---");
for (String s : depositInputs)
{
    double amount = TransactionParser.parseAmount(s);
    try
    {
        ac.deposit(amount);
    }
    catch (IllegalArgumentException e)
    {
        System.out.println("Deposit failed: " + e.getMessage());
    }
}

System.out.println("--- Withdrawals ---");
for (String s : withdrawInputs)
{
    double amount = TransactionParser.parseAmount(s);
    try
    {
        ac.withdraw(amount);
    }
    catch (IllegalArgumentException e)
    {
        System.out.println("Withdrawal failed: " + e.getMessage());
    }
    catch (InsufficientBalanceException e)
    {
        System.out.println("Withdrawal failed: " + e.getMessage());
    }
    finally
    {
        System.out.println("Withdrawal attempt finished.");
    }
}

System.out.println("Final balance: " + ac.getBalance());
```

**`BankAccount.java` — add success confirmation (fixes Issue 2):**
```java
public void deposit (double amount)
{
    validateAmount(amount);
    balance += amount;
    System.out.println("Deposited " + amount + ". New balance: " + balance);
}

public void withdraw (double amount) throws InsufficientBalanceException
{
    validateAmount(amount);
    if (amount > this.balance)
    {
        throw new InsufficientBalanceException(
            "Cannot withdraw " + amount + ". Current balance is only " + balance);
    }
    balance -= amount;
    System.out.println("Withdrew " + amount + ". New balance: " + balance);
}
```

These two changes alone would bring your actual output in line with Section 7's expected output.

## 21. Personalized Review Questions

1. In your `Main.java`, the `try` block wraps the entire `for`-loop rather than being placed inside it. If the first withdrawal in `withdrawInputs` throws an exception, what happens to the remaining withdrawals in the array — and why?
2. Why does moving the try/catch inside the loop body (one `try` per iteration) change how many times `finally` executes for a 2-element array where the first element succeeds and the second fails?
3. What is the difference between `throw` and `throws`? Give one example of each from your own `BankAccount.java`.
4. Why does `IllegalArgumentException` not require a `throws` declaration on `deposit()` or `withdraw()`, while `InsufficientBalanceException` does require one on `withdraw()`?
5. If `BankAccount.withdraw()` threw a *checked* exception but neither caught it nor declared `throws`, what would happen at compile time?
6. In `TransactionParser.parseAmount()`, why does the `NumberFormatException` never reach `Main.java`, even though `Main` calls `parseAmount()` directly?
7. Your `validateAmount()` throws `IllegalArgumentException` with a fixed, generic message. What Java syntax would you use to include the actual invalid amount value in the message instead?
8. Suppose you wanted `deposit()` to also fail with a *checked* exception (like `withdraw()` does) instead of an unchecked one. What would need to change in `deposit()`'s method signature and in every place `deposit()` is called?

## 22. Answers — Check Only After Attempting

1. The remaining withdrawals are skipped entirely. Once an exception is thrown inside a `try` block, control jumps immediately to the matching `catch`, abandoning the rest of the code still inside `try` — including the rest of the loop's iterations, since the loop itself is inside the `try`.
2. With `try` wrapped around the whole loop: item 1 succeeds, item 2 throws, `finally` runs once, after the loop has already exited. With `try` placed inside the loop: `finally` runs once per iteration — once after item 1 (success), again after item 2 (failure) — two executions total, matching Section 7's expected output.
3. `throw` is a statement inside a method body that actually creates and raises one specific exception object right now, e.g. `throw new InsufficientBalanceException("Not enough BALANCE");` in `withdraw()`. `throws` is a keyword in a method's *signature* declaring the method might propagate a checked exception, e.g. `public void withdraw(double amount) throws InsufficientBalanceException` — it doesn't throw anything itself, it's a compiler-enforced label.
4. `IllegalArgumentException` extends `RuntimeException`, making it unchecked — the compiler never forces a `throws` declaration for unchecked exceptions. `InsufficientBalanceException` extends `Exception` directly, making it checked — the compiler requires any method that might throw it (without catching it) to declare `throws` for it.
5. Compile error: the compiler reports something like "unreported exception ...; must be caught or declared to be thrown" — checked exceptions must be either caught inside the method or declared in its `throws` clause.
6. `parseAmount()` wraps `Double.parseDouble(input)` in its own `try-catch` and catches `NumberFormatException` right there, returning a default value instead of letting it escape. `Main` only ever sees `parseAmount()`'s return value, never the exception object.
7. `throw new IllegalArgumentException("Amount must not be less than or equal 0: " + amount);` — string-concatenate the `amount` parameter into the message passed to the constructor.
8. `deposit()`'s signature would need `throws SomeCheckedException` added, and every caller of `deposit()` (here, only `Main`) would need to wrap the call in a `try-catch` for that exception or declare `throws` itself — since `main()` can't propagate further, it would have to catch it.

## 23. Top 3 Things to Review Next

1. **try/catch scope discipline** — wrap only the single risky operation, not an entire loop of independent operations, especially when each iteration should be allowed to fail without cancelling the rest.
2. **Treat "expected output" as an executable spec** — run your program and diff its output against the exercise's expected output line-by-line before considering a task done; this would have caught both the dropped test case and the missing success messages immediately.
3. **Write exception messages that carry the actual failing values** (amount, balance, input string) instead of generic fixed text — makes propagated exceptions far more useful for whoever catches them.

## 24. Final Recommendation

**Mostly ready — review a few points first.**

Your grasp of exception *vocabulary* — checked vs. unchecked, `throw` vs. `throws`, custom exceptions, local handling vs. propagation — is correctly applied everywhere it counts at the method level, which is the conceptual core of this exercise. What's holding the score back is entirely in `Main.java`: the try/catch scope around the loops, and not verifying output against the spec before calling it done. Neither is a misunderstanding of what an exception *is* — both are about applying try/catch with the right granularity. Fix the loop restructuring in [Section 20](#20-corrected-code-for-important-issues), re-run against Section 7's expected output, and you'll be fully ready to move on.
