# Distributed Consistency and Crash Recovery — Small Java Exercise

**Level:** Beginner-friendly Java 21 console exercise  
**Estimated scope:** One source file; no Maven, Spring Boot, database, Docker, cloud account, HTTP server, or threads.  
**Important:** The exercise is a **simulation**. It does **not** demonstrate real persistence across an operating-system process crash.

## Contents

1. [Learning objectives and prerequisites](#1-learning-objectives-and-prerequisites)
2. [Exercise instructions — complete before seeing the solution](#2-exercise-instructions--complete-before-seeing-the-solution)
3. [Expected behavior and verification](#3-expected-behavior-and-verification)
4. [Reference solution — read after attempting](#4-reference-solution--read-after-attempting)
5. [Why the important methods work](#5-why-the-important-methods-work)
6. [Failure matrix and real-world limitations](#6-failure-matrix-and-real-world-limitations)
7. [Review questions and answers](#7-review-questions-and-answers)

---

# 1. Learning objectives and prerequisites

By the end, you should be able to:

- Explain why a document can exist in storage even when its operation record is not `READY`.
- Use a Java `Map` to simulate two independent systems.
- Represent an operation using a Java `record` and a small `enum`.
- Inject a simulated failure between two successful steps.
- Write a recovery method that examines stored evidence before finalizing.
- Explain exactly why this simulation is **not** durable across a real JVM crash.

**Requirements:** Java 21 installed and available on `PATH`, a terminal (PowerShell on Windows 11), and a text editor. Check Java with:

```powershell
java -version
javac -version
```

No external libraries are needed.

# 2. Exercise instructions — complete before seeing the solution

## 2.1 Scenario

A program accepts a small text document. The logical workflow is:

```mermaid
flowchart LR
    A[Save intent PENDING] --> B[Store document in fake storage]
    B --> C[Mark intent READY]
```

The **fake metadata repository** remembers an operation's status. The **fake external storage** remembers a document's text under an object key.

The workflow can fail **after** storage succeeds but **before** metadata changes to `READY`.

## 2.2 Folder and PowerShell setup

Create a new folder anywhere you have permission to write; the following commands use your home directory:

```powershell
New-Item -ItemType Directory -Force -Path "$HOME\distributed-recovery-lab"
Set-Location "$HOME\distributed-recovery-lab"
New-Item -ItemType File -Force -Path "RecoveryDemo.java"
code RecoveryDemo.java
```

The intended folder structure is:

```text
distributed-recovery-lab/
└── RecoveryDemo.java
```

If the `code` command is unavailable, open the file manually in VS Code. Use **no package declaration**.

## 2.3 Tasks to implement yourself

1. Define `enum Status { PENDING, READY }`. This deliberately small exercise uses only two states; the theory discusses richer state machines.
2. Define `record Operation(String id, String objectKey, Status status)`.
3. Define `FakeMetadataRepository` with a `Map<String, Operation>`. Add methods to create a `PENDING` intent, read it, list records, and mark one `READY`.
4. Define `FakeObjectStorage` with a **separate** `Map<String, String>`. Add methods to store document text and check whether a key exists.
5. Define `UploadWorkflow` receiving both fake systems in its constructor. Implement `upload(id, document, crashAfterStorage)`:
   - Save `PENDING` intent.
   - Store document text at `documents/<id>.txt`.
   - If the injected-failure flag is true, throw a custom `RuntimeException` **before** marking `READY`.
   - Otherwise mark the intent `READY`.
6. Implement `recover()`:
   - Visit unfinished metadata records.
   - If the corresponding object exists, mark the record `READY`.
   - If it does not exist, leave it incomplete and report that it cannot yet be finalized.
7. In `main()`, run the three required cases below. Call `recover()` twice to verify that the second run does not change already-finished operations.

**Keep it simple.** There is no real network, transaction manager, retry queue, or background worker here.

## 2.4 Required cases

| Case | Steps | What should happen |
|---|---|---|
| A — Happy path | Save intent → store bytes/text → mark ready | Metadata `READY`; object exists |
| B — Simulated crash after storage | Save intent → store → throw before `READY` | Metadata remains `PENDING`; object exists |
| C — Recovery | Construct a *new workflow instance* with the **same fake repository/storage objects**; call `recover()` | Unfinished operation moves to `READY` |

**Crucial simulation detail:** Case C is **not** a real JVM restart. Both Maps remain alive inside the same running JVM and are explicitly passed to the newly created workflow object. If you truly stop and restart the Java program, ordinary in-memory Maps disappear; real systems use durable PostgreSQL and independently durable storage instead.

# 3. Expected behavior and verification

After implementing the program, run these Windows PowerShell commands:

```powershell
javac --release 21 RecoveryDemo.java
java RecoveryDemo
```

Expected output from the reference implementation:

```text
CASE A - Happy path
Intent saved: op-001 -> PENDING
Object stored: documents/op-001.txt
Marked READY: op-001
State: READY

CASE B - Failure after storage
Intent saved: op-002 -> PENDING
Object stored: documents/op-002.txt
Simulated failure: Stopped before READY: op-002
State: PENDING
Object exists: true

CASE C - Recovery with new service instance
Recovered READY: op-002
Recovered: 1
State: READY
Second recovery changes: 0
```

Verify that:

- Case A ends with `READY`.
- Case B ends with `PENDING` **even though its document exists**.
- Case C changes the unfinished operation to `READY`.
- A second recovery run reports **0 changes**, demonstrating repeatable finalization in this toy program.

No image or document files will appear on disk: the fake storage is a `Map`, not a filesystem writer. Only `.class` files appear after `javac` compiles the program.

To remove compiled output on Windows:

```powershell
Remove-Item .\*.class
```

---

# 4. Reference solution — read after attempting

Save the following **complete** source code as `RecoveryDemo.java`:

```java
import java.util.LinkedHashMap;
import java.util.Map;

public class RecoveryDemo {
    enum Status { PENDING, READY }

    record Operation(String id, String objectKey, Status status) {
        Operation withStatus(Status next) {
            return new Operation(id, objectKey, next);
        }
    }

    static class FakeMetadataRepository {
        private final Map<String, Operation> rows = new LinkedHashMap<>();

        void createIntent(String id, String key) {
            if (rows.containsKey(id)) throw new IllegalArgumentException("Duplicate operation ID");
            rows.put(id, new Operation(id, key, Status.PENDING));
        }

        Operation find(String id) {
            return rows.get(id);
        }

        void markReady(String id) {
            Operation old = rows.get(id);
            if (old == null) throw new IllegalArgumentException("Unknown operation ID");
            if (old.status() == Status.READY) return; // Repeatable finalization
            rows.put(id, old.withStatus(Status.READY));
        }

        Iterable<Operation> all() {
            return rows.values();
        }
    }

    static class FakeObjectStorage {
        private final Map<String, String> objects = new LinkedHashMap<>();

        void upload(String key, String documentText) {
            objects.put(key, documentText);
        }

        boolean exists(String key) {
            return objects.containsKey(key);
        }
    }

    static class UploadWorkflow {
        private final FakeMetadataRepository metadata;
        private final FakeObjectStorage storage;

        UploadWorkflow(FakeMetadataRepository metadata, FakeObjectStorage storage) {
            this.metadata = metadata;
            this.storage = storage;
        }

        void upload(String id, String document, boolean crashAfterStorage) {
            String key = "documents/" + id + ".txt";
            metadata.createIntent(id, key);       // Step 1: record intent
            System.out.println("Intent saved: " + id + " -> PENDING");

            storage.upload(key, document);        // Step 2: external effect
            System.out.println("Object stored: " + key);

            if (crashAfterStorage) {
                throw new SimulatedCrash("Stopped before READY: " + id);
            }

            metadata.markReady(id);               // Step 3: finalize
            System.out.println("Marked READY: " + id);
        }

        int recover() {
            int recovered = 0;
            for (Operation operation : metadata.all()) {
                if (operation.status() == Status.READY) continue;
                if (storage.exists(operation.objectKey())) {
                    metadata.markReady(operation.id());
                    System.out.println("Recovered READY: " + operation.id());
                    recovered++;
                } else {
                    System.out.println("Cannot finalize; object missing: " + operation.id());
                }
            }
            return recovered;
        }
    }

    static class SimulatedCrash extends RuntimeException {
        SimulatedCrash(String message) { super(message); }
    }

    public static void main(String[] args) {
        var metadata = new FakeMetadataRepository();
        var storage = new FakeObjectStorage();
        var firstService = new UploadWorkflow(metadata, storage);

        System.out.println("CASE A - Happy path");
        firstService.upload("op-001", "First document", false);
        System.out.println("State: " + metadata.find("op-001").status());

        System.out.println("\nCASE B - Failure after storage");
        try {
            firstService.upload("op-002", "Second document", true);
        } catch (SimulatedCrash e) {
            System.out.println("Simulated failure: " + e.getMessage());
        }
        System.out.println("State: " + metadata.find("op-002").status());
        System.out.println("Object exists: " + storage.exists("documents/op-002.txt"));

        System.out.println("\nCASE C - Recovery with new service instance");
        var newService = new UploadWorkflow(metadata, storage);
        System.out.println("Recovered: " + newService.recover());
        System.out.println("State: " + metadata.find("op-002").status());
        System.out.println("Second recovery changes: " + newService.recover());
    }
}

```

This is intentionally all in one Java source file. Nested types group the demonstration without creating a package structure or introducing frameworks.

# 5. Why the important methods work

### `createIntent()`

Creates a database-like record in the **metadata Map** with state `PENDING`. This acts like a bookmark that says what the operation intended to do. The duplicate-ID check is deliberately basic—it rejects a repeated identifier rather than implementing the full request-fingerprint and result-replay protocol taught in the theory.

### `storage.upload()`

Stores document text in a **different Map** under a key such as `documents/op-002.txt`. In reality, uploading through HTTP to a separate object-storage service would succeed or fail independently of the database. Here, both operations are just in-memory method calls; the separation is conceptual.

### `throw new SimulatedCrash(...)`

Models a failure after the object is stored but before finalization. The exception does **not** shut down the JVM or erase Maps. The `catch` in `main()` lets you observe the deliberately unfinished state immediately.

### `recover()`

Scans metadata records. If a record is incomplete and an object exists at its key, it marks that record `READY`; it does nothing to already-`READY` records. The second recovery run therefore produces no changes.

**Production caveat:** Object existence alone is insufficient to prove valid document content. Real recovery should verify the intended object identity, expected length/hash and applicable validation before marking `READY`. This toy does not enforce all of those conditions.

### `new UploadWorkflow(metadata, storage)`

Creates a new **service object** but passes it the exact same Map-backed fake systems. This is a simulation of reconstructing service logic from preexisting durable resources, not a real process restart. In a real system, the new JVM would reconnect to PostgreSQL and object storage rather than reusing Java heap objects.

# 6. Failure matrix and real-world limitations

| Failure point | Fake metadata state | Fake object state | What `recover()` can do |
|---|---|---|---|
| Before intent is created | No operation | No object | No work to recover |
| After intent, before storage | `PENDING` | Missing | Report unresolved; source must be retried/provided |
| After storage, before `READY` | `PENDING` | Present | Mark `READY` (simplified verification) |
| After `READY` | `READY` | Present | Skip: no additional changes |

The exercise demonstrates **reasoning about partial failure**, not real ACID commits, real network timeouts, or durable crash recovery.

| Simulation | Production equivalent |
|---|---|
| Metadata `Map` | Persisted DB operations table with constraints and transactions |
| Object `Map` | Actual external object-storage service |
| Thrown `SimulatedCrash` exception | Process termination, network loss, or worker failure |
| New workflow instance | New process reconnecting to durable systems |
| `exists()` check | Remote observation plus identity/content verification |
| Single sequential execution | Possible concurrent workers; requires safe claims/transitions |

The reference solution intentionally **does not** implement full idempotent upload retries, request fingerprints, data-integrity checks, backoff, concurrency control, or a cleanup job. Those topics belong in the theory, and adding them here would make the first exercise unnecessarily large.

# 7. Review questions and answers

**Q1.** The storage call completed, but the application crashed before saving `READY`. Did the whole operation succeed or fail?  
**Answer:** It was **partially completed**: the object exists, but the operation record remains incomplete. Recovery must reconcile the two states.

**Q2.** Why does a fresh `UploadWorkflow` object find the previous operation in this exercise?  
**Answer:** Because `main()` passes the same `FakeMetadataRepository` and `FakeObjectStorage` objects to it. Their Maps were **not** destroyed. This is not evidence of real persistence across a JVM restart.

**Q3.** If a remote upload times out, can the application safely assume the object was never stored?  
**Answer:** No. The result may be **unknown**: the remote service may have stored it even though the response was lost. Check the remote object and validate its identity before finalizing or retrying.

---

## Final mental model

A workflow can be interrupted between independently completed steps. Recording an intention lets another execution find the unfinished work; checking actual external state lets it decide how to finish safely. **This tiny exercise simulates that reasoning in memory, while real crash recovery requires durable systems and stronger validation.**
