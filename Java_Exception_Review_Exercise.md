# Java Exception Review Exercise

## 1. Exercise Overview

You will build a small console **Bank Account** program that reviews the core Java exception-handling toolkit: `try`, `catch`, `finally`, `throw`, `throws`, checked vs. unchecked exceptions, custom exceptions, and exception propagation between methods.

The logic itself (add/subtract a number from a balance) is intentionally trivial — the entire point of the exercise is practicing exception **syntax and flow**, not algorithms.

---

## 2. Concepts You Will Review

* `try` / `catch` / `finally`
* `throw` (manually creating and throwing an exception)
* `throws` (declaring that a method may propagate an exception)
* Checked exceptions (must be caught or declared)
* Unchecked exceptions (`RuntimeException` subclasses — not forced by the compiler)
* Custom exceptions (extending `Exception` vs. `RuntimeException`)
* Exception propagation across multiple method calls
* The difference between a method that **handles** an exception itself vs. one that **propagates** it to its caller

---

## 3. Scenario

You are building a very small **Bank Account** system.

* An account has a balance.
* You can **deposit** money into it.
* You can **withdraw** money from it.
* Amounts must be positive numbers — an invalid amount (zero, negative, or unparsable text) is a user-input mistake, not a fatal design flaw, so it will be represented with an **unchecked** exception.
* Withdrawing more money than the account holds is a meaningful business rule failure that callers *must* consciously deal with, so it will be represented with a **checked** custom exception.

---

## 4. Project Structure

```text
Main.java
BankAccount.java
InsufficientBalanceException.java
TransactionParser.java
```

| File | Purpose |
|---|---|
| `BankAccount.java` | Holds the balance; `deposit()` and `withdraw()` logic |
| `InsufficientBalanceException.java` | Custom **checked** exception for withdrawals that exceed the balance |
| `TransactionParser.java` | Turns a `String` amount into a `double`; demonstrates a method that **handles its own exception** |
| `Main.java` | Entry point; drives test scenarios and catches propagated exceptions |

---

## 5. Exception Flow Overview

This exercise deliberately includes **both** propagation styles from the assignment:

**Case 1 — Method handles its own exception** (`TransactionParser.parseAmount`):

```text
Main.main()
   │
   ▼
TransactionParser.parseAmount(String input)
   │
   ▼
Double.parseDouble(input) may throw NumberFormatException
   │
   ▼
parseAmount() catches it itself and returns a safe default (0.0)
   │
   ▼
Main never sees a NumberFormatException at all
```

**Case 2 — Method propagates the exception** (`BankAccount.withdraw`):

```text
Main.main()
   │
   ▼
account.withdraw(amount)
   │
   ▼
amount > balance → throw new InsufficientBalanceException(...)
   │
   ▼
withdraw() does NOT catch it — it only declares "throws InsufficientBalanceException"
   │
   ▼
exception propagates back up to Main.main()
   │
   ▼
Main's catch block handles it
```

There is also a third, simpler unchecked-exception flow: `validateAmount()` (called from both `deposit()` and `withdraw()`) throws `IllegalArgumentException` manually with `throw`. Because it's unchecked, neither `deposit()` nor `withdraw()` needs a `throws` clause for it — it silently propagates until something chooses to catch it (in this exercise, `Main` does).

---

## 6. Tasks

### Task 1 — Create the basic `BankAccount` class

Create `BankAccount.java` with:
* a private `double balance` field
* a constructor that takes an initial balance
* a `getBalance()` method

No exception handling yet — this is just the skeleton.

**Concept practiced:** none yet (setup only).

---

### Task 2 — Validate amounts with an unchecked exception

Add a **private** method:

```java
private void validateAmount(double amount)
```

It should manually `throw` an `IllegalArgumentException` (built into Java, already unchecked) when `amount <= 0`.

**Concept practiced:** `throw`, unchecked exceptions.

**Expected behavior:** calling `validateAmount(-5)` should throw immediately; calling `validateAmount(10)` should do nothing.

---

### Task 3 — Implement `deposit()`

Add:

```java
public void deposit(double amount)
```

It should call `validateAmount(amount)` first, then add `amount` to the balance. Do **not** wrap the call to `validateAmount` in a `try-catch` here — let the exception pass straight through `deposit()` if it occurs.

**Concept practiced:** letting an unchecked exception propagate without any special syntax (no `throws` needed, since it's unchecked).

---

### Task 4 — Create the checked custom exception

Create `InsufficientBalanceException.java`, extending `Exception` (not `RuntimeException`), with a constructor that takes a `String message` and passes it to `super(message)`.

**Concept practiced:** custom checked exceptions.

---

### Task 5 — Implement `withdraw()` with `throws`

Add:

```java
public void withdraw(double amount) throws InsufficientBalanceException
```

It should:
1. Call `validateAmount(amount)` (same as `deposit`).
2. If `amount > balance`, manually `throw new InsufficientBalanceException(...)` with a descriptive message.
3. Otherwise, subtract `amount` from the balance.

Do **not** catch `InsufficientBalanceException` inside `withdraw()` — declare it with `throws` and let it propagate to whoever calls `withdraw()`.

**Concept practiced:** `throws`, checked exceptions, propagation (Case 2 from Section 5).

---

### Task 6 — Implement `TransactionParser.parseAmount()`

Create `TransactionParser.java` with a static method:

```java
public static double parseAmount(String input)
```

Inside, wrap `Double.parseDouble(input)` in a `try-catch` that catches `NumberFormatException`. On failure, print a message explaining the input was invalid and return `0.0` instead of throwing anything further.

**Concept practiced:** `try-catch` that fully **handles** an exception locally (Case 1 from Section 5) — nothing escapes this method.

---

### Task 7 — Wire everything together in `Main`, with `finally`

In `Main.java`:
* Create one `BankAccount`.
* Loop over a few sample deposit inputs (as `String`s), parse each with `TransactionParser.parseAmount`, then call `account.deposit(...)` inside a `try-catch` that catches `IllegalArgumentException`.
* Loop over a few sample withdrawal inputs the same way, calling `account.withdraw(...)` inside a `try` that catches **both** `IllegalArgumentException` and `InsufficientBalanceException` in separate `catch` blocks, followed by a `finally` block that prints something like `"Withdrawal attempt finished."`.

**Concept practiced:** catching propagated checked and unchecked exceptions with specific catch types, and `finally` running regardless of outcome.

---

### Task 8 — Test success and failure scenarios

Run your program using the sample inputs from Section 7 and confirm your output matches the expected output in Section 7.

**Concept practiced:** confirming your understanding of the whole flow end-to-end.

---

## 7. Test Cases and Expected Output

Use these sample inputs:

```java
String[] depositInputs  = {"50", "abc", "-20"};
String[] withdrawInputs = {"30", "500"};
```

Starting balance: `100.0`

**Case 1 — Valid operation → no exception**
Input `"50"` for deposit → parses fine, `deposit(50.0)` succeeds.
```text
Deposited 50.0. New balance: 150.0
```

**Case 2 — Unparsable text → exception handled internally (Case 1 style, Section 5)**
Input `"abc"` for deposit → `NumberFormatException` is caught *inside* `parseAmount`, never reaches `Main`.
```text
Invalid number format: "abc". Using 0.0 instead.
Deposit failed: Amount must be greater than zero: 0.0
```
(The second line happens because `parseAmount` returns `0.0`, and `deposit(0.0)` then fails `validateAmount` — an unchecked exception caught in `Main`.)

**Case 3 — Invalid negative value → unchecked exception**
Input `"-20"` for deposit → parses fine as a number, but fails validation.
```text
Deposit failed: Amount must be greater than zero: -20.0
```

**Case 4 — Operation exceeds allowed value → custom checked exception propagates (Case 2 style, Section 5)**
Withdrawing `"500"` when the balance is only `120.0`:
```text
Withdrawal failed: Cannot withdraw 500.0. Current balance is only 120.0
Withdrawal attempt finished.
```

Full expected console output, top to bottom:

```text
--- Deposits ---
Deposited 50.0. New balance: 150.0
Invalid number format: "abc". Using 0.0 instead.
Deposit failed: Amount must be greater than zero: 0.0
Deposit failed: Amount must be greater than zero: -20.0
--- Withdrawals ---
Withdrew 30.0. New balance: 120.0
Withdrawal attempt finished.
Withdrawal failed: Cannot withdraw 500.0. Current balance is only 120.0
Withdrawal attempt finished.
Final balance: 120.0
```

---

## 8. Starter Code

```java
// Main.java
public class Main {
    public static void main(String[] args) {
        // TODO: create a BankAccount
        // TODO: loop over sample deposit inputs, parse, deposit, catch IllegalArgumentException
        // TODO: loop over sample withdrawal inputs, parse, withdraw,
        //       catch IllegalArgumentException and InsufficientBalanceException, use finally
        // TODO: print the final balance
    }
}
```

```java
// BankAccount.java
public class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    private void validateAmount(double amount) {
        // TODO: throw IllegalArgumentException if amount <= 0
    }

    public void deposit(double amount) {
        // TODO
    }

    public void withdraw(double amount) /* TODO: throws ? */ {
        // TODO
    }
}
```

```java
// InsufficientBalanceException.java
public class InsufficientBalanceException /* TODO: extends ? */ {
    // TODO: constructor(String message) calling super(message)
}
```

```java
// TransactionParser.java
public class TransactionParser {
    public static double parseAmount(String input) {
        // TODO: try Double.parseDouble(input), catch NumberFormatException,
        //       print a message, and return 0.0 on failure
        return 0.0;
    }
}
```

---

## 9. Hints

* Syntax reminder for validation:
  ```java
  if (amount <= 0) {
      throw new IllegalArgumentException("some message");
  }
  ```
* Syntax reminder for a checked exception on a method signature:
  ```java
  public void method() throws SomeCheckedException {
      ...
  }
  ```
* A checked exception's constructor almost always looks like:
  ```java
  public SomeException(String message) {
      super(message);
  }
  ```
* `Double.parseDouble("abc")` throws `NumberFormatException`, which is a subclass of `RuntimeException` — so it's unchecked, and you're allowed to catch it even though nothing forces you to.
* You can have **multiple `catch` blocks** after one `try`, one per exception type — Java checks them top to bottom and uses the first one that matches.
* `finally` goes after the last `catch` block and has no parentheses:
  ```java
  try {
      ...
  } catch (SomeException e) {
      ...
  } finally {
      ...
  }
  ```
* Remember: `deposit()` and `withdraw()` share the same validation rule — don't duplicate the logic, call the shared private method from both.

---

## 10. Self-Review Checklist

```text
- [ ] I understand what an exception is.
- [ ] I can write a try-catch block.
- [ ] I understand when finally executes.
- [ ] I know the difference between throw and throws.
- [ ] I understand checked exceptions.
- [ ] I understand unchecked exceptions.
- [ ] I can create a custom exception.
- [ ] I understand exception propagation.
- [ ] I understand when a method should catch an exception itself.
- [ ] I understand when a method should propagate an exception to its caller.
```

---

## 11. Solution — Do Not Read Until You Finish

**`InsufficientBalanceException.java`**

```java
public class InsufficientBalanceException extends Exception {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
```

**`BankAccount.java`**

```java
public class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero: " + amount);
        }
    }

    public void deposit(double amount) {
        validateAmount(amount);
        balance += amount;
        System.out.println("Deposited " + amount + ". New balance: " + balance);
    }

    public void withdraw(double amount) throws InsufficientBalanceException {
        validateAmount(amount);

        if (amount > balance) {
            throw new InsufficientBalanceException(
                "Cannot withdraw " + amount + ". Current balance is only " + balance);
        }

        balance -= amount;
        System.out.println("Withdrew " + amount + ". New balance: " + balance);
    }
}
```

**`TransactionParser.java`**

```java
public class TransactionParser {

    public static double parseAmount(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: \"" + input + "\". Using 0.0 instead.");
            return 0.0;
        }
    }
}
```

**`Main.java`**

```java
public class Main {
    public static void main(String[] args) {
        BankAccount account = new BankAccount(100.0);

        String[] depositInputs = {"50", "abc", "-20"};
        String[] withdrawInputs = {"30", "500"};

        System.out.println("--- Deposits ---");
        for (String input : depositInputs) {
            double amount = TransactionParser.parseAmount(input);
            try {
                account.deposit(amount);
            } catch (IllegalArgumentException e) {
                System.out.println("Deposit failed: " + e.getMessage());
            }
        }

        System.out.println("--- Withdrawals ---");
        for (String input : withdrawInputs) {
            double amount = TransactionParser.parseAmount(input);
            try {
                account.withdraw(amount);
            } catch (IllegalArgumentException e) {
                System.out.println("Withdrawal failed: " + e.getMessage());
            } catch (InsufficientBalanceException e) {
                System.out.println("Withdrawal failed: " + e.getMessage());
            } finally {
                System.out.println("Withdrawal attempt finished.");
            }
        }

        System.out.println("Final balance: " + account.getBalance());
    }
}
```

---

## 12. Solution Explanation

### `try`

Only the code that might actually throw the exception you care about belongs inside `try` — in `Main`, that's the single call to `account.deposit(...)` or `account.withdraw(...)`. Keeping `try` narrow makes it obvious which statement is responsible if a `catch` block runs.

### `catch`

When an exception is thrown inside `try`, Java looks at the `catch` blocks **in order** and runs the first one whose type matches the thrown exception (including subclasses). In the withdrawal loop there are two `catch` blocks — one for `IllegalArgumentException`, one for `InsufficientBalanceException` — because these are two *different, unrelated* failure types, and lumping them into a single `catch (Exception e)` would hide which one actually happened.

### `finally`

The `finally` block after the withdrawal `try-catch` runs **every single time**, whether `withdraw()` succeeded, threw `IllegalArgumentException`, or threw `InsufficientBalanceException`. That's why `"Withdrawal attempt finished."` appears after every withdrawal attempt in the expected output, success or failure alike.

### `throw`

```java
throw new InsufficientBalanceException("Cannot withdraw " + amount + ". Current balance is only " + balance);
```

This line does the actual work: it constructs a new exception object and immediately hands control to the nearest matching `catch` (or, if there isn't one in the current method, up to the caller). `throw` is an *action* — it's what makes an exception happen at all, as opposed to `throws`, which is just a signature-level warning label.

### `throws`

```java
public void withdraw(double amount) throws InsufficientBalanceException
```

Because `InsufficientBalanceException` is **checked**, the compiler forces `withdraw()` to either catch it or declare it. Here it's declared, meaning `withdraw()` is telling every caller: "I might throw this — you deal with it." No `throws` clause exists for `IllegalArgumentException` because it's unchecked, and the compiler never requires one.

### Exception propagation

Trace the `InsufficientBalanceException` from Case 4 in Section 7:

```text
Main.main()
    │  calls account.withdraw(500.0) inside a try block
    ▼
BankAccount.withdraw(500.0)
    │  validateAmount(500.0) passes (500 > 0, no exception)
    │  amount (500.0) > balance (120.0) → condition true
    ▼
throw new InsufficientBalanceException(...)
    │  withdraw() has no catch for this — it only declares "throws"
    ▼
exception propagates back up out of withdraw()
    ▼
Main's catch (InsufficientBalanceException e) block executes
    │  prints "Withdrawal failed: ..."
    ▼
Main's finally block still runs afterward
    │  prints "Withdrawal attempt finished."
```

Compare this with the `IllegalArgumentException` thrown by `validateAmount()` during a deposit of `"-20"` — it's unchecked, so `deposit()` never needed a `throws` clause, yet it propagates through `deposit()` exactly the same way, straight up to `Main`'s `catch (IllegalArgumentException e)` block.

And compare both to `TransactionParser.parseAmount("abc")` — there, the `NumberFormatException` is caught *inside* `parseAmount` itself and never leaves that method at all; `Main` never even knows a `NumberFormatException` almost happened.

---

## 13. Checked vs. Unchecked Exceptions Used

```text
Exception                     Type        Why
--------------------------------------------------------------------------
InsufficientBalanceException  Checked     Extends Exception (not RuntimeException).
                                           The compiler forces withdraw() to either
                                           catch it or declare it with throws — it
                                           represents a business-rule failure the
                                           caller must consciously plan for.

IllegalArgumentException      Unchecked   Extends RuntimeException (built into the
                                           JDK). The compiler does not force
                                           deposit()/withdraw() to declare or catch
                                           it — it represents a programming/input
                                           mistake (an invalid amount) rather than
                                           a condition every caller must plan for.

NumberFormatException         Unchecked   Also a RuntimeException subclass, thrown
                                           by Double.parseDouble(). It's caught
                                           voluntarily inside parseAmount() purely
                                           because that's the most useful place to
                                           handle bad text input — not because the
                                           compiler requires it.
```

---

## 14. throw vs. throws

```text
throw
  → a statement, used INSIDE a method body
  → actually creates and raises one specific exception instance, right now
  → example: throw new IllegalArgumentException("bad amount");

throws
  → a keyword used in a METHOD SIGNATURE
  → does not create or raise anything by itself
  → declares that this method might propagate a checked exception,
    so callers are required to handle or re-declare it
  → example: public void withdraw(double amount) throws InsufficientBalanceException
```

A useful way to remember it: `throw` is a verb (do it now), `throws` is an adjective describing the method (a warning label on the door).

---

## 15. Exception Propagation Walkthrough

Two propagation styles appear in this exercise, matching Section 5:

**Style A — Handled locally (`TransactionParser.parseAmount`)**

```text
Main.main() → parseAmount("abc") → NumberFormatException thrown
                                  → caught immediately inside parseAmount
                                  → parseAmount returns 0.0
Main.main() never sees the exception.
```

**Style B — Propagated to the caller (`BankAccount.withdraw`)**

```text
Main.main() → account.withdraw(500.0) → InsufficientBalanceException thrown
                                       → withdraw() has no catch, only throws
                                       → exception exits withdraw()
                                       → arrives back at Main.main()
                                       → Main's catch block handles it
```

The deciding question for which style to use: *does this method have enough context to meaningfully recover, or does only the caller know what to do next?* `parseAmount` can recover on its own (just use a default). `withdraw` cannot decide on `BankAccount`'s behalf whether a failed withdrawal should retry, alert a user, or abort a larger transaction — so it hands the decision upward.

---

## 16. Final Knowledge Summary

```text
Exception occurs
       │
       ▼
Is it handled here?
   │          │
  YES         NO
   │          │
catch it      propagate it
(e.g.         with throws
parseAmount   (e.g. withdraw() →
catches       InsufficientBalanceException
NumberFormat  → Main catches it)
Exception)
```

By finishing this exercise you should be able to explain, using your own project's methods as examples:

* the difference between `throw` and `throws`
* why `InsufficientBalanceException` is checked and `IllegalArgumentException` is not
* how to write a custom exception class in two lines
* why `finally` runs even when a `catch` block runs
* when a method should swallow/handle an exception itself vs. let it propagate
