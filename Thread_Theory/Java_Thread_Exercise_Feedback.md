# Java Thread Exercise — Code Review & Feedback

## 1. Overall Result

Score: **82/100**
Grade: **Very Good**

## 2. Executive Summary

The hard parts of this exercise — the race condition and the `synchronized` fix, and the `ExecutorService` thread pool — are done correctly, and I verified them by actually compiling and running the code (and, for the race condition, by checking your git history). `Counter` is genuinely shared between two threads, the unsynchronized version was written and (based on commit history) tested before you added `synchronized`, and the fixed pool of 2 threads visibly processes 4 tasks in two waves rather than spawning 4 threads.

The gap is that your **final, currently-active `Main.java` only contains Task 8/9 (race condition) and Task 10 (thread pool)**. Part 1 (`WorkerThread` vs `Runnable`, `isAlive()`, `join()`) is present only as a commented-out block at the top of the file, and Part 2 (thread priority) isn't in the file at all — though your git history shows you *did* write correct priority code for it at one point (commit `5cfeac1`, "Done task 7") before it got overwritten while working on Task 8. So `isAlive()` is never actually exercised in the program you'd run today, and the priority demo doesn't exist in the final version, even though the supporting classes (`WorkerThread`, `WorkerTask`) are fully capable of it — I confirmed this by wiring your unmodified classes into a scratch `Main` and running it.

## 3. Original Exercise Requirements Checklist

| # | Task | Status |
|---|------|--------|
| 1 | `WorkerThread` extends `Thread`, constructor(name, steps) | ✅ Done |
| 2 | `WorkerTask` implements `Runnable`, same constructor shape | ✅ Done |
| 3 | Loop steps 1..n, print, in both `run()`; start one of each from `Main` | ⚠️ Classes done; `Main` wiring only in commented-out code |
| 4 | `Thread.sleep(200)` + catch `InterruptedException` in both `run()` | ✅ Done |
| 5 | Print `isAlive()` before start and immediately after start | ❌ Not in active code (only commented out) |
| 6 | `join()` both workers, print `isAlive()` after join | ⚠️ Present and correct in Task 8's code; the Part 1-specific check is only in commented-out code |
| 7 | Two more `WorkerThread`s with `MIN_PRIORITY`/`MAX_PRIORITY`, start, join | ❌ Not in final `Main.java` (was written correctly in git history, then dropped) |
| 8 | Shared `Counter`, two threads, 10,000 increments each, plain (unsynchronized) `increment()` | ✅ Done — confirmed via git history (commit `6552953`) |
| 9 | Add `synchronized` to `increment()`, re-run | ✅ Done and active |
| 10 | `newFixedThreadPool(2)`, submit 4 `WorkerTask`s, `shutdown()` | ✅ Done and active, verified by running the program |

## 4. Project Structure Review

```text
thread/
├── WorkerThread.java   — extends Thread, name+steps fields, run() loop with sleep
├── WorkerTask.java     — implements Runnable, same shape as WorkerThread
├── Counter.java        — private int count, synchronized increment(), getCount()
└── Main.java           — active: Task 8/9 (race condition) + Task 10 (ExecutorService)
                           dormant: Task 1/3/5/6 demo, entirely commented out at the top
                           missing: Task 7 (priority) — not present in any form
```

All four required files exist, all declare `package thread;` consistently, and no extra/unnecessary files were added. `WorkerTask` is correctly reused in two different contexts in the surviving code — historically as `new Thread(new WorkerTask(...))` (Part 1, now commented out) and currently as `executor.submit(new WorkerTask(...))` (Part 4, active) — which is exactly the point of `Runnable` being decoupled from `Thread`.

## 5. Compilation Review

I compiled the project fresh (`javac -d . thread/*.java`) after deleting the pre-existing `.class` files. **It compiles cleanly, with zero errors or warnings.** No missing imports, no signature mismatches, no unhandled checked exceptions — `InterruptedException` is caught everywhere `Thread.sleep()` is called, and both required imports (`ExecutorService`, `Executors`) are present in `Main.java`.

## 6. Runtime / Execution Review

I ran the compiled program 5 times. Output (abbreviated):

```text
20000
t2: step 1
t1: step 1
t2: step 2
t1: step 2
t1: step 3
t2: step 3
t3: step 1
t4: step 1
t3: step 2
t4: step 2
t3: step 3
t4: step 3
```

Verified across all 5 runs:
- The printed count was **exactly `20000` every time** — the synchronized `Counter` is protecting the critical section correctly.
- `t1` and `t2` step output interleaves differently between runs (sometimes `t1: step 1` first, sometimes `t2: step 1` first) — correct non-deterministic behavior, not a bug.
- **`t3`/`t4` never start until `t1`/`t2` finish.** This is real evidence that the pool has exactly 2 worker threads being reused across 4 tasks, not 4 threads created up front — the thread pool's actual behavior, not just correct-looking syntax.

I additionally verified two things using scratch copies (your original files were not modified):
1. Removing `synchronized` from a copy of `Counter.increment()` and running it 5 times gave counts of `15589, 14294, 16674, 13040, 13617` — well under 20000, confirming the lost-update race condition this exercise is meant to demonstrate.
2. Wiring your unmodified `WorkerThread`/`WorkerTask` into the commented-out Part 1 logic and running it gave: `isAlive before start: false`, `isAlive after start: true`, interleaved step output, `isAlive after join: false` — exactly the expected behavior, confirming your classes fully support Part 1, it's only the active `Main.java` wiring that's missing.

## 7. Score Breakdown

| Category | Earned | Max | Notes |
|---|---|---|---|
| Exercise requirements | 9 | 15 | Tasks 1,2,4,8,9,10 done and active; Task 5 and 7 absent from the final program; Task 3/6 only partially active |
| Thread vs Runnable | 9 | 10 | Both classes correct; `WorkerTask` reuse across `Thread` and `ExecutorService` demonstrated (partly via history) |
| start()/run()/sleep()/join()/isAlive() | 14 | 20 | start/run/sleep/join all correct and verified; `isAlive()` never invoked in active code (−6) |
| Thread priority | 1 | 5 | Not present in final `Main.java`; historical commit shows correct `setPriority` usage but no `join()`, then removed entirely |
| Race condition understanding | 15 | 15 | Verified via git history + independent reproduction; correct shared-object, 10k-iteration, join-before-read design |
| synchronized / thread safety | 15 | 15 | Correct critical section, correct shared monitor, verified 20000/20000 across 5 runs |
| ExecutorService / thread pool | 10 | 10 | Correct pool size, correct submit() usage, correct shutdown(); pool reuse empirically confirmed |
| Compilation and runtime correctness | 5 | 5 | Compiles cleanly, runs correctly and consistently |
| Code quality / readability | 4 | 5 | Clean and consistent, but a large commented-out block and inconsistent indentation in `WorkerThread.run()` |
| **Total** | **82** | **100** | |

## 8. Thread vs Runnable Review

`WorkerThread extends Thread` and `WorkerTask implements Runnable` are both correctly structured — same constructor shape (name, steps), same private fields, same `run()` logic. You correctly used `WorkerTask` in two different roles: wrapped in a manual `new Thread(...)` (Part 1, in the commented-out block) and submitted directly to an `ExecutorService` (Part 4, active). This is the actual point of the Task 2 self-check question ("why prefer `Runnable`?") — a `Runnable` is just work, not a thread, so it can be handed to *anything* that knows how to run it. Your code demonstrates that in practice, not just in theory.

## 9. start() vs run() Review

I found no place in your code where `run()` is called directly instead of `start()`. Every thread that should run concurrently is started with `.start()`:
```java
tf.start();
ts.start();
```
```java
threadf.start();  // Part 1, commented out — but present and correct
threads.start();
```
This is correct. You never conflated "call `run()`" with "start a new thread," which is the single most common beginner mistake in this exercise category.

## 10. sleep() and InterruptedException Review

Both `WorkerThread.run()` and `WorkerTask.run()` sleep *inside the loop, on the thread executing that method*:
```java
System.out.println (this.threadName + ": step " + i);
try {
    Thread.sleep(200);
} catch (InterruptedException e) {
    System.out.println (e.getMessage());
}
```
This correctly pauses only the worker thread itself, never `main` and never the other worker — confirmed by the interleaved output in Part 4 (`t1`/`t2` steps interleave rather than running sequentially). The checked exception is caught properly.

💡 Optional improvement: `Thread.sleep()`'s `InterruptedException` is normally thrown with a `null` message, so `e.getMessage()` would print the literal word `null` if this ever actually fired (it never does in your current program, since nothing calls `.interrupt()`). Printing `e` or `e.toString()`, or restoring the interrupt flag with `Thread.currentThread().interrupt()`, is a nicer pattern — but this is not required for this exercise and isn't a correctness issue.

## 11. join() Review

Active and correct in the race-condition test:
```java
tf.start();
ts.start();
tf.join();
ts.join();
System.out.println (counter.getCount());
```
`main` blocks on `tf.join()` then `ts.join()` before reading `counter.getCount()` — this is exactly right. If `join()` were missing or misplaced, you could read the counter before both threads finished incrementing it, and the printed value would be unreliable regardless of `synchronized`. You got the sequencing correct here.

The Part 1-specific `join()` calls (`threadf.join(); threads.join();`) are correct in the commented-out block and in my scratch re-test, but are not part of the program as it stands today.

## 12. isAlive() Review

```text
main
 ↓
threadf.isAlive()   → false   (before start)
 ↓
threadf.start()
 ↓
threadf.isAlive()   → true    (after start)
 ↓
threadf.join()
 ↓
threadf.isAlive()   → false   (after join)
```

This is the correct mental model, and I confirmed it produces exactly `false / true / false` when I ran your (unmodified) `WorkerThread`/`WorkerTask` through this exact sequence. However, **this code is only present as a comment in `Main.java` — `isAlive()` is never actually called in the program that runs today.** Task 5 explicitly asks for this observation to be printed before and after `start()`; right now that observation doesn't happen in your final submission.

## 13. Thread Priority Review

Not present anywhere in the current `thread/Main.java`. However, your git history (commit `5cfeac1`, "Done task 7") shows you wrote:
```java
threadf.setPriority(Thread.MIN_PRIORITY);
threads.setPriority(Thread.MAX_PRIORITY);
threadf.start();
threads.start();
```
This is correct usage of `setPriority`/`MIN_PRIORITY`/`MAX_PRIORITY`. Two things worth flagging about that historical version, for your own awareness:
1. It never called `.join()` on either thread — so `main` would reach the end of that version of `main()` without waiting for the priority workers to finish (harmless here since they're non-daemon threads and the JVM waits anyway, but it skips the "join to guarantee completion" lesson from Task 6).
2. This entire block was deleted in the very next commit (`6552953`, "done task 8") and never merged back in, so Task 7 does not exist in your final deliverable at all.

**Correct mental model, confirmed once, but not part of what you'd hand in today.**

## 14. Shared Counter Review

```text
Thread tf ───┐
             ├──► same Counter instance.increment()
Thread ts ───┘
```
`Counter counter = new Counter();` is created exactly once in `Main`, and both lambda `Runnable`s close over that same local variable — this is genuinely one shared object, not two independent counters. This is the single most important thing to get right in this part of the exercise, and you got it right.

## 15. Race Condition Review

Your git history confirms you implemented this in the correct order:
- Commit `6552953` ("done task 8") added `Counter` with a **plain, non-synchronized** `increment()`:
  ```java
  public void increment() { this.count++; }
  ```
- Commit `b499606` ("done v1 thread") is what added the `synchronized` keyword.

That means Task 8 (see the race condition) was genuinely attempted before Task 9 (fix it) — not written synchronized from the start. I independently reproduced this on a scratch copy of your unsynchronized `Counter` and got totals of 13040–16674 (out of an expected 20000) across 5 runs, which is exactly the "lost update" behavior the exercise is testing for: `count++` is a read, an increment, and a write, and two threads can interleave those steps so that one thread's write overwrites the other's.

Design is correct: one shared `Counter`, two `Thread`s each looping `10000` times, both started, both joined before reading the result. This satisfies Task 8's requirements.

## 16. synchronized Review

```java
public synchronized void increment() {
    this.count++;
}
```
Both `tf` and `ts` call `increment()` on the **same `counter` object**, and `synchronized` on an instance method locks on `this` — so both threads are contending for the same monitor. That means only one thread can execute `increment()` at a time on that object, turning the read-modify-write sequence back into an effectively atomic step from the outside. I verified this empirically: 5/5 runs printed exactly `20000`.

## 17. Compare Unsynchronized and Synchronized Versions

Both stages of Task 8/9 are genuinely demonstrated — not just claimed. Evidence:
- **Task 8 (unsynchronized):** git commit `6552953`, plus my independent re-run of that exact code showing counts well below 20000.
- **Task 9 (synchronized):** the current, active `Counter.java`, plus 5/5 clean runs at exactly 20000.

This is one of the strongest-verified parts of the whole review.

## 18. ExecutorService Review

```java
ExecutorService executor = Executors.newFixedThreadPool (2);
executor.submit(t1);
executor.submit(t2);
executor.submit(t3);
executor.submit(t4);
executor.shutdown();
```
Correct: a fixed pool of 2 threads, 4 `WorkerTask` instances (each a valid `Runnable`) submitted, `shutdown()` called afterward. `t1`–`t4` here are `WorkerTask` objects, not `Thread` objects — you didn't confuse "the task" with "the thread that runs it."

## 19. submit() / execute() Review

You used `executor.submit(...)` as the exercise specifically asked for (Task 10), not `execute(...)`. `submit()` accepts your `Runnable` and returns a `Future` (unused here, which is fine — nothing in the exercise requires inspecting it). No deviation to flag.

## 20. shutdown() Review

`executor.shutdown()` is called once, after all 4 tasks are submitted — correct placement. You're not calling it mid-submission, and you're not describing or treating it as an immediate kill switch anywhere in the code. It correctly stops the pool from accepting new work while letting the 4 already-queued tasks finish (confirmed by the run output actually completing all of `t1`–`t4`'s steps).

## 21. Thread-Safety Review

| Shared Data | Accessing Threads | Protected? | Correct? |
|---|---|---|---|
| `Counter.count` | `tf`, `ts` (both incrementing 10,000×) | Yes — `synchronized` instance method | ✅ Correct |

No other shared mutable state exists in your project. `WorkerThread`/`WorkerTask`'s `steps`/`threadName` fields are per-instance and never shared across threads, so they need no protection.

## 22. Non-Deterministic Output Review

| Behavior | Classification | Observed |
|---|---|---|
| `t1`/`t2` step interleaving | May vary between runs | Confirmed — order differed across my 5 runs |
| `t3`/`t4` starting only after `t1`/`t2` finish | Must be deterministic (pool size = 2) | Confirmed on every run |
| Final synchronized count | Must be deterministic (=20000) | Confirmed — 20000 on every run |
| High-priority worker printing first | Not guaranteed, never assumed in your code | N/A — priority code isn't in the active program to check |

Nothing in your active code incorrectly assumes a specific interleaving or relies on priority for ordering.

## 23. What You Did Well

**You correctly demonstrated the read-modify-write race condition and its fix, in the correct order, and this is verifiable from your own commit history.** Commit `6552953` added an unsynchronized `increment()`; the very next commit added `synchronized`. This isn't just "the final code has `synchronized` on it" — you can show you actually tested the broken version first, which is exactly the point of Tasks 8 and 9.

**Your `Counter` is genuinely shared, not accidentally duplicated.** A common beginner mistake is creating two separate `Counter` instances (one per thread) and being confused about why nothing breaks. You created one `Counter` and closed over it from both lambdas — the setup where a race condition is actually possible.

**Your thread pool correctly reuses 2 threads across 4 tasks rather than creating 4 threads.** I confirmed this isn't just correct syntax — running your program shows `t3`/`t4` don't start until `t1`/`t2` finish, which is real evidence the pool is queuing work rather than spawning a thread per task.

**`WorkerTask` is genuinely reusable as a `Runnable`.** You used the same class both wrapped in a manual `Thread` (historically, Part 1) and submitted to the `ExecutorService` (Part 4, active) — showing you understand `Runnable` as "the task" independent of whatever executes it.

**No `start()`/`run()` confusion anywhere.** Every thread that needs to run concurrently is started with `.start()`, never `.run()`.

## 24. Problems / Mistakes

### Issue 1 — Part 1 and Part 2 are missing from the active program

Severity: **Important**

Location: `thread/Main.java`, lines 9–38 (commented out); Part 2 absent entirely.

My code:
```java
// WorkerThread threadf = new WorkerThread("Thread 1",10);
// Thread threads = new Thread (new WorkerTask("Thread 2", 10));
// System.out.println(threadf.isAlive());
// threadf.start();
// ...
```

Problem: The program you'd actually run today never prints `isAlive()`, never demonstrates the `WorkerThread`-vs-`Thread(WorkerTask)` comparison from Task 3/5/6, and doesn't include the priority experiment from Task 7 at all (it existed once, in commit `5cfeac1`, then was deleted in `6552953` and never restored).

Why it is wrong: These are graded checkpoints in the exercise (Tasks 5 and 7 specifically). Having correct supporting classes isn't the same as having them wired up and run.

Correct mental model: Each of the 4 parts should run one after another from `main()`, ideally with a print header per part (as shown in the exercise's example output), so all 10 tasks are visible in one execution.

Correct approach: Uncomment the Part 1 block, add `.join()` calls where missing, and re-add the Part 2 priority block (you already wrote correct code for it — it just needs restoring and a `.join()` on each thread added before moving on).

### Issue 2 — Historical priority code never joined its threads

Severity: **Minor** (since this code isn't even in the final file, but worth knowing for next time)

Location: git commit `5cfeac1`, `thread/Main.java`

My code (historical):
```java
threadf.start();
threads.start();
// (no join() calls before main() returns)
```

Problem: Task 7 asks you to start both priority workers then `join()` both. That `join()` was never added before this code was replaced.

Why it matters: Without `join()`, `main` has no guarantee the priority workers finished before moving on to the next part — it happened to work out here only because they were the last lines of `main()`.

Correct approach:
```java
threadf.start();
threads.start();
threadf.join();
threads.join();
```

### Issue 3 — Leftover large comment block reduces readability

Severity: **Style**

Location: `thread/Main.java`, lines 9–38

Problem: A ~30-line commented-out block sits at the top of the currently active `main()`, ahead of the working Task 8/10 code.

Why it is wrong: Not a correctness issue, but it makes the file harder to read and obscures which parts of the exercise are actually being demonstrated when the program runs.

Correct approach: Either delete it (git history preserves it) or move it into its own restored, active section as discussed in Issue 1.

### Issue 4 — Minor indentation inconsistency in `WorkerThread.run()`

Severity: **Style**

Location: `thread/WorkerThread.java`, lines 16–29

My code:
```java
for (int i = 1; i <= this.steps; i++)
{
    System.out.println (this.threadName + ": step " + i);

try
{
    Thread.sleep(200);
}
...
}
```

Problem: The `try/catch` block is indented one level shallower than the `println` right above it, even though it's still inside the `for` loop body.

Why it is wrong: Purely cosmetic — Java doesn't care about whitespace — but it makes the loop body harder to scan at a glance.

Correct approach: Indent the `try/catch` to match the rest of the loop body.

## 25. Code Quality Review

Class names, method names, and field names are clear and consistent (`threadName`, `steps`, `count`). Fields are private with a proper constructor and a public getter on `Counter` — reasonable encapsulation for this scope. You avoided over-engineering (no unnecessary interfaces, no premature abstraction) which is appropriate for a syntax-review exercise. One nice detail I noticed: when you added the Task 10 `WorkerTask t1..t4` variables, you renamed the race-condition threads from `t1`/`t2` to `tf`/`ts` specifically to avoid a naming collision — a small but real sign of paying attention to your own code as it grew. The main deductions here are the leftover comment block and the minor indentation slip noted above (Issues 3–4), neither of which affects correctness.

## 26. Comparison with Reference Solution

| Area | My Implementation | Reference | Evaluation |
|---|---|---|---|
| `WorkerThread` | Fields, constructor, `run()` loop + sleep — matches structurally | Same shape | ✅ Equivalent |
| `WorkerTask` | Fields, constructor, `run()` loop + sleep — matches structurally | Same shape | ✅ Equivalent |
| `start()`/`run()` | Only `.start()` used, everywhere | Same | ✅ Equivalent |
| `sleep()`/`InterruptedException` | Caught, prints `e.getMessage()` (would print `null` if ever triggered) | Prints a custom "was interrupted" message | 💡 Reference's message is more informative, but yours isn't wrong |
| `isAlive()` | Correct logic, but only in commented-out code — not active | Active in all 3 checkpoints | ❌ Missing from final program |
| `join()` | Correct and active for Task 8; correct-but-inactive for Part 1 | Active everywhere | ⚠️ Partially active |
| Priority | Correct historically, absent from final file | Active, `MIN_PRIORITY`/`MAX_PRIORITY`, joined | ❌ Missing from final program |
| `Counter` | Single shared instance, `synchronized` instance method | Same approach | ✅ Equivalent |
| `ExecutorService` | `newFixedThreadPool(2)`, 4×`submit()`, `shutdown()` | Same | ✅ Equivalent |

Where your code differs from the reference, it's either equivalent (Counter, ExecutorService, start/run) or a completeness gap (isAlive, priority) rather than a wrong-but-valid alternative — there's no case here where I'm docking you for a stylistic difference from the reference.

## 27. Concepts Clearly Understood

- `Thread` vs `Runnable` as two different ways to define work, including that a `Runnable` is reusable across contexts (manual `Thread`, `ExecutorService`)
- `start()` genuinely launches a new thread; `run()` would not — no misuse anywhere in your code
- `Thread.sleep()` pauses only the thread executing that line, not `main` or other workers
- `join()` blocks the calling thread until the target finishes, and you sequenced it correctly before reading `counter.getCount()`
- `count++` is a read-modify-write, not one atomic step — demonstrated with real before/after evidence from your own commits
- `synchronized` on an instance method locks on the shared object, serializing access to the critical section
- A fixed thread pool reuses a small number of threads across many tasks rather than creating one thread per task
- `shutdown()` stops new task acceptance while letting queued tasks finish

## 28. Concepts Partially Understood

- `join()` specifically as "print `isAlive()` before/after start/join" — you understand `join()`'s blocking semantics (proven in Task 8), but the specific Part 1 checkpoint sequence isn't in your final program to confirm end-to-end
- Thread priority as a scheduler hint — you used `setPriority` correctly once (git history), but never joined those threads, and the "run it a few times, order isn't guaranteed" observation was never actually made since this code didn't survive into the final version

## 29. Concepts That Need Review

- `isAlive()` — not exercised anywhere in the program as it stands today; you should re-add and actually run this checkpoint
- Priority + `join()` together — re-add Task 7's code with the missing `join()` calls, then actually observe a few runs to see that ordering isn't guaranteed

## 30. Corrected Code for Important Issues

For Issue 1/2 (restoring Part 1 and Part 2 into the active program), based on your own historical code plus the missing `join()`:

```java
// Part 1
System.out.println("=== Part 1: Thread vs Runnable ===");
WorkerThread workerA = new WorkerThread("Worker A", 3);
Thread workerB = new Thread(new WorkerTask("Worker B", 3));

System.out.println("Worker A isAlive before start: " + workerA.isAlive());
workerA.start();
workerB.start();
System.out.println("Worker A isAlive after start: " + workerA.isAlive());

workerA.join();
workerB.join();
System.out.println("Worker A isAlive after join: " + workerA.isAlive());
System.out.println("All initial workers completed.");

// Part 2
System.out.println("=== Part 2: Thread Priority ===");
WorkerThread lowPriority = new WorkerThread("Low Priority Worker", 3);
WorkerThread highPriority = new WorkerThread("High Priority Worker", 3);

lowPriority.setPriority(Thread.MIN_PRIORITY);
highPriority.setPriority(Thread.MAX_PRIORITY);

lowPriority.start();
highPriority.start();

lowPriority.join();   // <- this was missing in commit 5cfeac1
highPriority.join();  // <- this was missing in commit 5cfeac1
System.out.println("Priority demo finished (order is not guaranteed).");
```
(`main` would need `throws InterruptedException`, or these `join()` calls wrapped in try/catch as you did elsewhere.)

## 31. Personalized Review Questions

1. Why does calling `threadf.start()` let `main` continue immediately, while calling `threadf.run()` would not?
2. In `WorkerThread.run()`, when `Thread.sleep(200)` executes, which thread is actually paused — `main`, or the worker?
3. In your Task 8 code, which thread does `tf.join()` pause — `tf` itself, or the thread that called `join()`?
4. Why is it important that `tf.join()` and `ts.join()` both happen *before* `System.out.println(counter.getCount())`?
5. `count++` looks like a single operation. What three steps is it actually made of, and how can two threads interleave them to lose an update?
6. Why does it matter that both `tf` and `ts` call `increment()` on the *same* `Counter` object rather than two separate ones?
7. When you mark `increment()` as `synchronized`, what object is actually being locked?
8. If `highPriority.setPriority(Thread.MAX_PRIORITY)` doesn't guarantee that thread prints first, what is it actually doing?
9. What is the practical difference between `new Thread(task).start()` for 4 tasks versus `Executors.newFixedThreadPool(2)` + 4×`submit()`?
10. In your Task 10 output, `t3`/`t4` don't start printing until `t1`/`t2` finish. What does that tell you about how many threads the pool actually has?
11. What does `executor.shutdown()` actually stop, and what does it *not* stop?
12. If you called `workerA.isAlive()` right before `workerA.start()`, what would it print, and why?

## 32. Answers — Check Only After Attempting

1. `start()` asks the JVM to create a genuinely new OS-backed thread, which will call `run()` on its own; `main` doesn't wait and moves to the next line immediately. `run()` called directly is just an ordinary synchronous method call on whichever thread called it.
2. The worker thread itself — `sleep()` only pauses the thread executing that line, never `main` or any other thread.
3. `join()` pauses the *caller* — in your code, `main` — until `tf` finishes; it does not pause `tf`.
4. Without both `join()` calls first, `main` could read `counter.getCount()` while `tf` and/or `ts` are still mid-loop, making the printed value meaningless regardless of whether `synchronized` is present.
5. Read `count`, compute `count + 1`, write it back. If two threads both read the same old value before either writes back, one thread's write overwrites the other's, and one increment is silently lost.
6. If they used two separate `Counter` instances, there would be no shared mutable state at all — each thread would safely maintain its own `count`, and there would be nothing to race on. The exercise only demonstrates anything because both threads mutate the exact same object.
7. The shared `Counter` instance itself (`this` inside `increment()`) — since both threads call `increment()` on the same object, they contend for the same monitor.
8. It only gives the JVM/OS scheduler a hint to prefer giving that thread more CPU time. It's not a synchronization or ordering mechanism, and actual behavior can vary by run, OS, and core count.
9. `new Thread(task).start()` for 4 tasks creates 4 separate OS threads, one per task, discarded afterward. `newFixedThreadPool(2)` creates exactly 2 reusable threads up front, and all 4 tasks queue up and get processed 2 at a time as those threads free up.
10. It tells you the pool has exactly 2 worker threads — if it had 4, all four tasks' "step 1" lines could appear before any "step 2," but instead `t3`/`t4` wait for `t1`/`t2`'s threads to become free.
11. `shutdown()` stops the pool from accepting *new* submitted tasks; it does not kill tasks that are already running or already queued — those are allowed to finish normally.
12. `false` — the thread hasn't been launched yet, so it can't be alive, regardless of how short or long its task is.

## 33. Top 3 Things to Review Next

1. **Re-add and actually run the `isAlive()` checkpoint from Part 1.** Your classes already support it correctly (I verified this independently) — it's purely a matter of wiring it back into `Main.java` and observing the `false → true → false` sequence yourself.
2. **Restore the priority experiment (Task 7) with the missing `join()` calls, and run it several times.** You wrote the core of this correctly once (commit `5cfeac1`) — finish it by adding `join()` and actually watching for the fact that `MAX_PRIORITY` does *not* reliably print first.
3. **Re-check the overall structure of `Main.java`** — with all four parts active, add print headers per part (as in the exercise's example output) so a single run demonstrates the whole assignment end-to-end, and clean out the old commented-out block once it's superseded.

## Final Recommendation

```text
Mostly ready — review a few points first
```

You've correctly and verifiably handled the two conceptually hardest parts of this exercise — the race condition and its `synchronized` fix, and the `ExecutorService` thread pool — including confirming both the "broken" and "fixed" behavior empirically rather than just having the right keyword in place. The `Thread`/`Runnable` distinction, `start()` vs `run()`, `sleep()`, and `join()` are all used correctly wherever they appear active. What's holding this back from "ready to move on" is that two of the ten tasks — `isAlive()` (Task 5) and thread priority (Task 7) — aren't present in the program you'd actually run today, even though the supporting code and, in the priority case, prior git history show you know the syntax. Restoring those two pieces (see items 1–2 above) would close the gap.
