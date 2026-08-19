# Java Thread Review Exercise

## 1. Exercise Overview

You will build a small console **Worker Task Simulation** that reviews the core Java threading toolkit: creating threads two different ways, `start()` vs `run()`, `sleep()`, `join()`, `isAlive()`, thread priority, a tiny race condition fixed with `synchronized`, and a basic `ExecutorService` thread pool.

The "work" each thread does is just printing numbered steps with a short pause — the point is to practice thread **syntax and behavior**, not build anything algorithmically interesting.

---

## 2. Concepts You Will Review

* Extending `Thread` vs. implementing `Runnable`
* `start()` vs. `run()`
* `Thread.sleep()` and `InterruptedException`
* `join()`
* `isAlive()`
* Thread priority (`setPriority`, `MIN_PRIORITY`, `NORM_PRIORITY`, `MAX_PRIORITY`) as a scheduler *hint*, not a guarantee
* Non-deterministic interleaving of concurrent output
* A basic race condition on shared mutable state
* Fixing it with `synchronized`
* `ExecutorService` / `Executors.newFixedThreadPool(...)` as an alternative to manual threads

---

## 3. Scenario

You are simulating a small pool of **workers**, each of whom performs a task made up of a few numbered steps, pausing briefly between steps (simulating real work like a download or an order being prepared).

The exercise is split into four parts, run one after another from `main()`:

1. **Thread vs. Runnable** — start one worker each way, observe `isAlive()`, wait with `join()`.
2. **Priority** — start two more workers with different priorities and observe that ordering still isn't guaranteed.
3. **Race Condition** — two threads hammer a shared counter; first see it break, then fix it with `synchronized`.
4. **Thread Pool** — submit several worker tasks to a small `ExecutorService` instead of creating threads manually.

---

## 4. Project Structure

```text
Main.java
WorkerThread.java
WorkerTask.java
Counter.java
```

| File | Purpose |
|---|---|
| `WorkerThread.java` | A worker implemented by **extending `Thread`** |
| `WorkerTask.java` | A worker implemented by **implementing `Runnable`** (reusable — used both with manual `Thread`s and later with the thread pool) |
| `Counter.java` | A tiny shared counter used to demonstrate a race condition, then fixed with `synchronized` |
| `Main.java` | Drives all four parts of the exercise |

---

## 5. Thread Flow Overview

```text
WorkerThread (extends Thread)          WorkerTask (implements Runnable)
        │                                        │
        │ workerA.start()                        │ new Thread(taskB).start()
        ▼                                        ▼
   new OS-level thread runs workerA.run()    new OS-level thread runs taskB.run()
        │                                        │
        └──────────────── both run concurrently ─┘
                           │
                    main thread calls
                    workerA.join() / threadB.join()
                           │
                           ▼
              main thread resumes only after
                 both workers have finished
```

For the race condition:

```text
Thread 1 ──┐
           ├─► both call counter.increment() at nearly the same time
Thread 2 ──┘
           │
   unsynchronized: both may read the SAME old value before either writes it back
           → one increment is lost
           │
   synchronized: only one thread executes increment() at a time
           → no increments are lost
```

For the thread pool:

```text
4 tasks submitted
        │
        ▼
   task queue
        │
        ▼
 2 reusable worker threads   (fixed pool size)
        │
   threads pick up tasks from the queue as they become free
```

---

## 6. Tasks

### Task 1 — Create a `Thread` subclass

Create `WorkerThread.java`, extending `Thread`. Give it a constructor that takes a worker name (`String`) and a number of steps (`int`), and store them in fields.

**Concept practiced:** extending `Thread`.

---

### Task 2 — Create a `Runnable` task

Create `WorkerTask.java`, implementing `Runnable`, with the same constructor shape (name + steps).

**Concept practiced:** implementing `Runnable`.

*Question to answer for yourself:* `WorkerThread` **is-a** `Thread`; `WorkerTask` **is-a** task that some `Thread` can run. What's one practical reason you might prefer `Runnable` (hint: Java doesn't support extending more than one class)?

---

### Task 3 — Implement `run()` for both, and start them

In both `WorkerThread.run()` and `WorkerTask.run()`, loop from step `1` to the total number of steps, printing something like `"<name>: step <n>"` each time.

In `Main.java`:
* Create one `WorkerThread` directly.
* Create one `WorkerTask` and wrap it in a plain `new Thread(task)`.
* Start both with `.start()`.

**Concept practiced:** `start()` actually launching a new thread that runs `run()` concurrently with `main`.

*Question to answer:* what would happen to the output if you called `workerA.run()` instead of `workerA.start()`? Try it and see before reading the explanation section.

---

### Task 4 — Add `Thread.sleep()`

Inside the step loop of both `run()` methods, add a short pause between steps, e.g. `Thread.sleep(200)`. This requires handling `InterruptedException` (a checked exception) with a `try-catch`.

**Concept practiced:** `sleep()`, and handling `InterruptedException`.

---

### Task 5 — Observe `isAlive()`

In `Main.java`, around your calls to `.start()`, print `workerA.isAlive()`:
* once **before** calling `.start()`
* once **immediately after** calling `.start()`

**Concept practiced:** `isAlive()` reflecting whether a thread has been started and hasn't finished yet.

---

### Task 6 — Wait using `join()`

After starting both workers from Task 3, call `.join()` on each of them before printing `"All initial workers completed."`. Then print `isAlive()` one more time, **after** `join()`.

**Concept practiced:** `join()` blocking the calling thread (here, `main`) until the target thread finishes.

---

### Task 7 — Experiment with thread priority

Create two more `WorkerThread` instances. Give one `Thread.MIN_PRIORITY` and the other `Thread.MAX_PRIORITY` using `setPriority(...)`, start both, then `join()` both.

**Concept practiced:** priority as a scheduler *hint*. Run your program a few times — do not expect the high-priority worker's output to consistently appear first.

---

### Task 8 — Demonstrate a race condition

Create `Counter.java` with a private `int count` field and a plain (not yet synchronized) `increment()` method that does `count++`.

In `Main.java`, create one shared `Counter`, then create two plain `Thread`s (a lambda `Runnable` is fine) that each call `increment()` a large number of times (e.g. 10,000). Start both, join both, then print the counter's value and compare it to the expected total (e.g. 20,000).

**Concept practiced:** `count++` is not one atomic operation — it's a read, an increment, and a write. Two threads can interleave those three steps and lose an update. Run the program a few times; the final count will often (not always) be less than expected.

---

### Task 9 — Fix it using `synchronized`

Add the `synchronized` keyword to `Counter.increment()`. Re-run the same test from Task 8.

**Concept practiced:** `synchronized` ensures only one thread can execute that method on a given object at a time, eliminating the lost-update problem.

---

### Task 10 — Repeat a small task using `ExecutorService`

In `Main.java`, create a fixed thread pool with `Executors.newFixedThreadPool(2)`. Submit four `WorkerTask` instances to it with `executor.submit(...)`. Finish by calling `executor.shutdown()`.

**Concept practiced:** a thread pool reuses a small, fixed number of worker threads to process many tasks from a queue, instead of creating a brand-new `Thread` per task.

---

## 7. Possible Output

> This is only one possible output. The exact interleaving of thread output is not guaranteed — every run may look slightly different, and that's expected.

```text
=== Part 1: Thread vs Runnable ===
Worker A isAlive before start: false
Worker A isAlive after start: true
Worker A: step 1
Worker B: step 1
Worker B: step 2
Worker A: step 2
Worker A: step 3
Worker B: step 3
Worker A finished.
Worker B finished.
Worker A isAlive after join: false
All initial workers completed.

=== Part 2: Thread Priority ===
High Priority Worker: step 1
Low Priority Worker: step 1
Low Priority Worker: step 2
High Priority Worker: step 2
High Priority Worker: step 3
Low Priority Worker: step 3
Priority demo finished (order is not guaranteed).

=== Part 3: Race Condition ===
Expected count: 20000
Actual count:   20000

=== Part 4: Thread Pool (ExecutorService) ===
Pool Task 1: step 1
Pool Task 2: step 1
Pool Task 1: step 2
Pool Task 2: step 2
Pool Task 1 finished.
Pool Task 2 finished.
Pool Task 3: step 1
Pool Task 4: step 1
Pool Task 3: step 2
Pool Task 4: step 2
Pool Task 3 finished.
Pool Task 4 finished.
```

---

## 8. Starter Code

```java
// WorkerThread.java
public class WorkerThread extends Thread {
    // TODO: fields for worker name and number of steps
    // TODO: constructor

    @Override
    public void run() {
        // TODO: loop through steps, print progress, sleep between steps
    }
}
```

```java
// WorkerTask.java
public class WorkerTask implements Runnable {
    // TODO: fields for task name and number of steps
    // TODO: constructor

    @Override
    public void run() {
        // TODO: loop through steps, print progress, sleep between steps
    }
}
```

```java
// Counter.java
public class Counter {
    private int count = 0;

    void increment() {
        // TODO: increment count (add synchronized later, in Task 9)
    }

    public int getCount() {
        return count;
    }
}
```

```java
// Main.java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        // TODO Part 1: create a WorkerThread and a Thread(WorkerTask),
        //              check isAlive before/after start, start both, join both

        // TODO Part 2: create two more WorkerThreads with different priorities

        // TODO Part 3: shared Counter, two threads incrementing it many times

        // TODO Part 4: ExecutorService with a fixed thread pool, submit tasks, shutdown
    }
}
```

---

## 9. Hints

* Starting a thread:
  ```java
  thread.start();
  ```
* Pausing (inside `run()`, so it must handle the checked exception):
  ```java
  try {
      Thread.sleep(200);
  } catch (InterruptedException e) {
      // decide what to print/do here
  }
  ```
* Waiting for a thread to finish, from `main`:
  ```java
  thread.join();
  ```
* Checking thread state:
  ```java
  thread.isAlive();
  ```
* Setting priority:
  ```java
  thread.setPriority(Thread.MAX_PRIORITY);
  ```
* Protecting a shared method:
  ```java
  synchronized void increment() {
      count++;
  }
  ```
* Creating and shutting down a thread pool:
  ```java
  ExecutorService executor = Executors.newFixedThreadPool(2);
  executor.submit(someRunnable);
  executor.shutdown();
  ```
* `Runnable` can also be written as a lambda: `Runnable r = () -> { ... };` — you don't strictly need `WorkerTask` for the race-condition part in Task 8.
* Remember the two imports for the thread pool: `java.util.concurrent.ExecutorService` and `java.util.concurrent.Executors`.

---

## 10. Self-Review Checklist

```text
- [ ] I can create a class that extends Thread.
- [ ] I can create a class that implements Runnable.
- [ ] I understand the difference between Thread and Runnable.
- [ ] I understand start() vs run().
- [ ] I can use Thread.sleep().
- [ ] I understand InterruptedException.
- [ ] I can use join().
- [ ] I understand isAlive().
- [ ] I understand that thread priority does not guarantee execution order.
- [ ] I understand what a race condition is.
- [ ] I understand why count++ is not automatically thread-safe.
- [ ] I can use synchronized in a basic example.
- [ ] I understand the basic purpose of a thread pool.
- [ ] I can use ExecutorService and shutdown().
```

---

## 11. Solution — Do Not Read Until You Finish

**`WorkerThread.java`**

```java
public class WorkerThread extends Thread {
    private final String workerName;
    private final int steps;

    public WorkerThread(String workerName, int steps) {
        this.workerName = workerName;
        this.steps = steps;
    }

    @Override
    public void run() {
        for (int i = 1; i <= steps; i++) {
            System.out.println(workerName + ": step " + i);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println(workerName + " was interrupted.");
            }
        }
        System.out.println(workerName + " finished.");
    }
}
```

**`WorkerTask.java`**

```java
public class WorkerTask implements Runnable {
    private final String taskName;
    private final int steps;

    public WorkerTask(String taskName, int steps) {
        this.taskName = taskName;
        this.steps = steps;
    }

    @Override
    public void run() {
        for (int i = 1; i <= steps; i++) {
            System.out.println(taskName + ": step " + i);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println(taskName + " was interrupted.");
            }
        }
        System.out.println(taskName + " finished.");
    }
}
```

**`Counter.java`**

```java
public class Counter {
    private int count = 0;

    synchronized void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
```

**`Main.java`**

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws InterruptedException {

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
        System.out.println("All initial workers completed.\n");

        System.out.println("=== Part 2: Thread Priority ===");
        WorkerThread lowPriority = new WorkerThread("Low Priority Worker", 3);
        WorkerThread highPriority = new WorkerThread("High Priority Worker", 3);

        lowPriority.setPriority(Thread.MIN_PRIORITY);
        highPriority.setPriority(Thread.MAX_PRIORITY);

        lowPriority.start();
        highPriority.start();

        lowPriority.join();
        highPriority.join();
        System.out.println("Priority demo finished (order is not guaranteed).\n");

        System.out.println("=== Part 3: Race Condition ===");
        Counter counter = new Counter();
        Runnable incrementTask = () -> {
            for (int i = 0; i < 10000; i++) {
                counter.increment();
            }
        };

        Thread t1 = new Thread(incrementTask);
        Thread t2 = new Thread(incrementTask);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Expected count: 20000");
        System.out.println("Actual count:   " + counter.getCount());
        System.out.println();

        System.out.println("=== Part 4: Thread Pool (ExecutorService) ===");
        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i = 1; i <= 4; i++) {
            executor.submit(new WorkerTask("Pool Task " + i, 2));
        }

        executor.shutdown();
    }
}
```

---

## 12. Solution Explanation

### `start()`

`workerA.start()` asks the JVM to create a real, new operating-system-backed thread of execution. That new thread's job is to call `workerA.run()` on its own — `main` does not wait for it and continues immediately to the next line.

### `run()`

`run()` is just a normal method containing the code you want executed. There's nothing magic about it — it's *only* special because `Thread.start()` happens to call it internally on the new thread.

### `start()` vs `run()`

```java
thread.run();
```

This does **not** start a new thread. It's an ordinary method call, so it executes on whichever thread called it (usually `main`), synchronously, exactly like calling any other method. You'd see all of `run()`'s output before the next line of `main()` runs, and `isAlive()` would report `false` even during execution, because no new thread was ever created.

### `sleep()`

`Thread.sleep(200)` pauses **the thread that is currently executing that line** — in `WorkerThread.run()`, that's the worker thread itself, not `main`. That's why `main` can keep running (e.g. moving on to start the next worker) while one worker is asleep. `sleep()` declares `throws InterruptedException` because another thread could theoretically interrupt the sleeping thread early, which is why it must be wrapped in `try-catch` (it's a checked exception).

### `join()`

```java
workerA.join();
```

means: *the thread that calls this line blocks until `workerA` finishes.* In `Main.java`, that caller is the `main` thread, so `main` pauses at that line until `workerA`'s `run()` method returns. Without `join()`, `main` could reach `"All initial workers completed."` while the workers are still printing their steps.

### `isAlive()`

* **Before `start()`:** always `false` — the thread hasn't been launched yet.
* **Right after `start()`:** almost always `true` — the new thread has been created and is (or is about to be) running `run()`.
* **After `join()`:** always `false` — `join()` only returns once the thread has finished, so by definition it can no longer be alive.

### Priority

```java
highPriority.setPriority(Thread.MAX_PRIORITY);
```

This only gives the OS/JVM scheduler a *hint* that this thread would like more CPU time relative to others. It does not force `highPriority` to finish first or even to print its first line first — the scheduler is still free to interleave threads however it wants, especially on a multi-core machine where both threads may simply run at the same time. That's why Part 2's expected output explicitly says the order is not guaranteed.

### Race Condition

`count++` looks like one operation but is actually three: **read** `count`, **add** 1, **write** the result back. If Thread 1 reads `count` (say, `5`) and then Thread 2 also reads `count` (still `5`) before Thread 1 writes back `6`, both threads compute `6` and write `6` — one of the two increments is silently lost. Run enough increments across two threads without protection, and the final total will often land below the mathematically expected value.

### `synchronized`

```java
synchronized void increment() {
    count++;
}
```

`synchronized` on an instance method means only **one thread at a time** can be executing that method on the same object; any other thread calling `increment()` concurrently has to wait its turn. This turns the read-modify-write sequence back into something that behaves as one atomic step from the outside, so no increments get lost.

### Thread Pool

```text
Task submitted → executor.submit(task)
                       ↓
                  task queue
                       ↓
        a small, fixed number of worker threads
        pick up tasks from the queue as they free up
```

Compare this to `new Thread(task).start()`: a manual thread is created fresh for every single task and discarded afterward, which becomes wasteful if you have many short-lived tasks. `Executors.newFixedThreadPool(2)` instead creates exactly 2 reusable threads up front; your 4 submitted tasks queue up and get processed 2-at-a-time as those threads become free. `executor.shutdown()` tells the pool to stop accepting new tasks and let its threads exit once the queue is empty — without calling it, the pool's threads would keep the JVM process alive indefinitely.

---

## 13. start() vs run()

```text
thread.start()
  → creates a genuinely new thread of execution
  → that new thread calls run() on its own
  → main (or whichever thread called start()) continues immediately,
    without waiting
  → isAlive() becomes true

thread.run()
  → an ordinary method call
  → executes on the CURRENT thread, synchronously
  → the caller is blocked until run() returns, just like any method
  → no new thread is created — isAlive() never becomes true because of this call
```

---

## 14. sleep() and join()

```text
Thread.sleep(ms)
  → pauses the CURRENTLY EXECUTING thread (the one running this line)
  → other threads are unaffected and keep running
  → may throw InterruptedException (checked) — must be caught or declared

someThread.join()
  → pauses the CALLING thread until someThread finishes
  → does not pause someThread itself
  → without join(), the caller has no guarantee someThread is done yet
```

Concretely in this exercise: `Thread.sleep()` inside `WorkerThread.run()` pauses *that worker*, while `workerA.join()` inside `main()` pauses *main*, waiting on *workerA*. Two different threads are being paused for two different reasons.

---

## 15. Thread Priority

```text
setPriority(Thread.MAX_PRIORITY)
  → a HINT to the scheduler: "prefer giving this thread CPU time"
  → NOT a guarantee of execution order
  → NOT a synchronization mechanism
  → actual behavior depends on the OS, JVM, and number of CPU cores,
    and can vary between runs of the exact same program
```

Never rely on priority to make one thread's output appear before another's — if you need a guaranteed order, that's what `join()` (sequencing) or `synchronized` (mutual exclusion) are for, not priority.

---

## 16. Race Condition and synchronized

```text
count++  is really three steps:
    1. read count
    2. compute count + 1
    3. write the result back into count

Two threads interleaving those steps can both read the SAME
value before either writes back → one update is lost.

synchronized void increment() { count++; }
    → only one thread may run this method on this object at a time
    → the three steps above happen as one uninterrupted unit
    → no lost updates
```

This is intentionally the smallest possible race condition example — real programs usually protect more than a single `int`, but the underlying reasoning (unprotected shared mutable state + concurrent read-modify-write) is exactly the same.

---

## 17. Manual Threads vs Thread Pool

```text
Manual threads                          Thread pool (ExecutorService)
-----------------------------------------------------------------------
new Thread(task).start()                executor.submit(task)
one brand-new OS thread per task        a small, fixed number of reusable threads
you manage creation yourself            the pool manages a queue + worker threads
fine for a handful of long-lived tasks  better for many short-lived tasks
no built-in shutdown mechanism          executor.shutdown() cleanly stops the pool
```

In this exercise, Parts 1–3 create threads manually because there are only a couple of them and you want to directly observe `start()`, `join()`, and `isAlive()`. Part 4 switches to a pool because there are more tasks (4) than you'd want dedicated threads for, and a fixed pool of 2 threads processes them without the overhead of creating and destroying 4 separate `Thread` objects.

---

## 18. Final Knowledge Summary

```text
TASK
 ↓
Thread / Runnable
 ↓
start()
 ↓
new thread runs
 ↓
scheduler decides execution
 ↓
run()

Coordination:
sleep()  → pause the current thread
join()   → caller waits for another thread to finish

Shared data:
race condition (unsynchronized count++)
 ↓
synchronized (protects the critical section)

Many tasks:
ExecutorService
 ↓
task queue
 ↓
fixed pool of reusable worker threads
```

By finishing this exercise you should be able to explain, using your own project's code as examples:

* why `thread.run()` never creates a new thread, but `thread.start()` does
* which thread is paused by a given `Thread.sleep()` call, and which thread is paused by a given `.join()` call
* what `isAlive()` returns before start, right after start, and after join
* why thread priority is only a hint, never a guarantee
* why `count++` can lose updates across threads, and how `synchronized` fixes it
* why you'd reach for `ExecutorService` instead of manually creating a `Thread` per task
