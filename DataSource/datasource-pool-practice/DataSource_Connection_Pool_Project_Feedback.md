# DataSource + Connection Pool Project Review

## Overall Assessment

This project shows a **good mechanical understanding of JDBC resource ownership and the basic transaction shape**, but it is not yet a reliable proof that the account-transfer use case is correct.

The strongest part is the resource graph:

```text
Main
  → creates one HikariDataSource
  → passes the same object to consumers as DataSource
  → repository methods borrow short-lived connections
  → TransferService borrows one connection for both updates
  → Main closes the application-owned pool
```

The transfer method correctly disables auto-commit, executes debit and credit on statements created from the same connection, checks update counts, commits only after both succeed, rolls back `SQLException` and `RuntimeException`, and preserves rollback failure with `addSuppressed(...)`.

The main weaknesses are at the boundaries around that otherwise sound transaction:

- invalid monetary amounts can be committed with reversed or rounded behavior;
- repository insert failures are hidden and converted to fake ID `0`;
- the intended rollback experiment never reaches the transaction;
- `Main` catches unexpected failures and lets Maven report `BUILD SUCCESS`;
- every run deletes the entire table;
- the configured Hikari `maxLifetime` is below the supported minimum and is reset to the default, while its warning is effectively hidden by the missing logging provider;
- connection-pool borrow/return behavior is not actually observed;
- configuration and secret-handling files are not safely organized.

Severity summary:

| Severity | Count | Meaning in this review |
|---|---:|---|
| 🔴 Critical | 2 | Can produce incorrect monetary behavior or convert database failure into false success |
| 🟠 Important | 7 | Blocks a trustworthy demonstration of transactions, pooling, or safe configuration |
| 🟡 Minor | 5 | Worth correcting, but does not invalidate the core resource design |

The review is based on the complete current project, its generated build metadata, the live Compose configuration, and the intended exercise. It does not penalize different names, formatting, pool sizes, output text, or Java 21 merely for differing from the reference; the report notes only the concrete loss of Java 17 bytecode compatibility.

## What I Implemented Well

- **One pool owner:** `Main.java:26-40` creates one `HikariDataSource`, owns it with try-with-resources, and supplies the same instance to application objects.
- **Correct abstraction boundary:** `AccountRepository.java:35-40` and `TransferService.java:26-31` depend on `javax.sql.DataSource`, not HikariCP implementation types.
- **No long-lived connection fields:** each repository operation and transaction borrows locally.
- **Strong JDBC cleanup:** connections, prepared statements, generated-key result sets, and query result sets use try-with-resources.
- **Correct generated-key mechanics before error handling:** the insert requests `Statement.RETURN_GENERATED_KEYS`, checks the affected-row count, calls `next()`, and reads the key.
- **One real transaction connection:** `TransferService.java:37-39` obtains one connection and creates both statements from it.
- **Correct transaction ordering:** auto-commit is disabled before either update; commit occurs only after debit and credit.
- **Atomic sufficient-funds predicate:** the debit SQL combines the balance check and update, avoiding a separate read-then-update race.
- **Correct update-count checks:** both debit and credit require exactly one affected row.
- **Good rollback mechanics:** both SQL and runtime failures trigger rollback while the connection is still open.
- **Excellent rollback-failure preservation:** `origin.addSuppressed(rollbackFailure)` retains the original failure as primary.
- **Appropriate money types:** the model, JDBC bindings, and PostgreSQL schema use `BigDecimal` / `NUMERIC(12,2)`.
- **Useful schema constraints:** PostgreSQL enforces an identity primary key, nonblank owner, non-null balance, and nonnegative balance.
- **Sound Compose topology:** only PostgreSQL is containerized; the port is loopback-bound; the data volume persists; the schema mount is read-only; and health-check variables are correctly escaped.
- **Correct host networking assumption:** the documented host JVM URL uses `localhost`; a Compose service name is not incorrectly used from Windows.
- **Basic environment validation:** missing or blank `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` fail before pool construction.
- **Deterministic pool shutdown:** the pool's try-with-resources boundary closes HikariCP even when work throws.
- **No forbidden framework leakage:** no Spring, Spring Boot, JPA, or Hibernate dependency or code appears.

Preparing the two statements before `setAutoCommit(false)` is not a defect. Preparing a statement does not update account rows; both executions happen after auto-commit is disabled. Manually restoring auto-commit is also unnecessary here because HikariCP resets tracked connection state when the handle returns.

## Critical Issues

### Issue 1 — Invalid transfer amounts can reverse a transfer or change the pair total

- **Location:** `src/main/java/com/example/poolpractice/service/TransferService.java:33-35,100-106`; `database/schema.sql:5-6`
- **Problem:** `validateTransfer(...)` checks only whether IDs are negative. It does not reject a null amount, zero, a negative amount, more than two decimal places, or the same source and destination.
- **Why it matters:** Money validation is part of transaction correctness, not cosmetic input checking. The SQL assumes a positive amount with the same scale as the stored currency.
- **Possible consequence:**
  - A negative amount makes `balance - negative` credit the source and `balance + negative` debit the destination, reversing the transfer.
  - A zero amount can commit as a misleading successful no-op.
  - A value with more than two decimals is coerced independently into `NUMERIC(12,2)` for each account; half-cent values can round asymmetrically and change the pair total.
  - Equal IDs debit and credit the same row and report success without moving money.
  - Null is allowed to reach JDBC/SQL instead of being rejected as an invalid method argument.
- **Recommended fix:** Validate domain preconditions before borrowing a connection and use `IllegalArgumentException` for caller errors:

```java
private static void validateTransfer(
        long fromId,
        long toId,
        BigDecimal amount
) {
    if (fromId == toId) {
        throw new IllegalArgumentException(
                "Source and destination must differ");
    }
    if (amount == null || amount.signum() <= 0) {
        throw new IllegalArgumentException(
                "Amount must be positive");
    }
    if (amount.scale() > 2) {
        throw new IllegalArgumentException(
                "Amount must have at most two decimal places");
    }
}
```

If positive IDs are also part of your public contract, validate them too—but then use a known nonexistent **positive** destination for the rollback test rather than `-1`.

- **Knowledge to review:** `BigDecimal` scale, monetary invariants, domain validation versus database errors, transaction preconditions.

### Issue 2 — `insert()` hides every SQL failure and returns a fake ID

- **Location:** `src/main/java/com/example/poolpractice/repository/AccountRepository.java:42-73`, especially lines `69-73`
- **Problem:** The method declares `throws SQLException`, catches every `SQLException`, prints it, and returns `0`.

```java
catch (SQLException e) {
    e.printStackTrace();
    return 0;
}
```

- **Why it matters:** A database write has no valid fallback ID. A connection failure, constraint violation, failed statement, missing generated key, or resource-close failure must remain a failure.
- **Possible consequence:** `Main` can continue with ID `0`, attempt a transfer against rows that do not exist, swallow that later failure, and finish as though the run were valid. More subtly, auto-commit may already have inserted the row before generated-key retrieval or resource closing fails; the database can contain a real account while the caller receives fake ID `0`.
- **Recommended fix:** Remove the catch and let the original checked exception propagate:

```java
public long insert(String ownerName, BigDecimal openingBalance)
        throws SQLException {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(
                 INSERT_SQL,
                 Statement.RETURN_GENERATED_KEYS)) {

        statement.setString(1, ownerName);
        statement.setBigDecimal(2, openingBalance);

        int affectedRows = statement.executeUpdate();
        if (affectedRows != 1) {
            throw new SQLException(
                    "Expected one inserted row, got " + affectedRows);
        }

        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException(
                        "Insert succeeded but returned no generated key");
            }
            return keys.getLong(1);
        }
    }
}
```

Adding context is acceptable only if the original exception remains the cause. Do not use a sentinel value for a failed write.

- **Knowledge to review:** checked exception propagation, generated keys, auto-commit write semantics, fake fallback values.

## Important Issues

### Issue 3 — The project does not execute a rollback test, and `-1` cannot reach rollback

- **Location:** `TransferService.java:35,100-106`; `Main.java:38-47`; `src/test/java/com/example/AppTest.java:15-19`
- **Problem:** The exercise's missing-destination scenario is absent from `Main`. In addition, the current ID validation rejects destination `-1` before a connection is acquired, before debit runs, and before rollback is possible. The only automated test is `assertTrue(true)`.
- **Why it matters:** “An exception occurred” and “rollback undid an executed debit” are different claims. Atomicity is proven only when debit succeeds first, credit fails second, and a database reread shows that debit was undone.
- **Possible consequence:** You can believe rollback works without ever exercising it. A future regression that removes rollback or uses two connections would remain undetected.
- **Recommended fix:** Add an explicit integration scenario:

```java
BigDecimal sourceBeforeFailure = balanceOf(repository.listAll(), fromId);
BigDecimal totalBeforeFailure =
        pairTotal(repository.listAll(), fromId, toId);

try {
    transfers.transfer(
            fromId,
            -1L,
            new BigDecimal("10.00"));
    throw new IllegalStateException(
            "Rollback scenario unexpectedly succeeded");
} catch (SQLException expected) {
    // Expected only because the destination update affected zero rows.
}

List<Account> afterFailure = repository.listAll();
if (balanceOf(afterFailure, fromId)
                .compareTo(sourceBeforeFailure) != 0
        || pairTotal(afterFailure, fromId, toId)
                .compareTo(totalBeforeFailure) != 0) {
    throw new IllegalStateException(
            "Rollback did not preserve balances");
}
```

Here, `balanceOf(...)` and `pairTotal(...)` are small verification helpers to add; they are not methods currently present in the repository.

For this exact exercise, allow `-1` to reach the update-count check. If you deliberately require positive IDs, choose and verify a nonexistent positive destination instead.

- **Knowledge to review:** proving rollback, transaction atomicity, failure-path integration testing, database rereads versus stale objects.

### Issue 4 — `Main` swallows unexpected failures and reports false success

- **Location:** `src/main/java/com/example/poolpractice/Main.java:38-45,55-58`
- **Problem:** Both the transfer block and the application boundary catch broad `Exception`, print only `getMessage()`, and continue or return normally.
- **Why it matters:** An exception boundary must distinguish an expected experiment from an unexpected application failure. Printing a message discards the exception type, stack, cause chain, linked `SQLException`s, and any suppressed rollback failure.
- **Possible consequence:** A failed insert, failed transfer, bad environment, or unavailable database can look like a normal run. This was reproduced safely: with `DB_URL` absent, `Main` printed the missing-variable message and Maven still ended with `BUILD SUCCESS`.
- **Recommended fix:** Catch only the deliberately forced rollback failure inside the use case. Let unexpected failures propagate or wrap them with the original cause:

```java
public static void main(String[] args) {
    try {
        run();
    } catch (SQLException exception) {
        for (SQLException current = exception;
             current != null;
             current = current.getNextException()) {
            System.err.printf(
                    "Database failure: SQLState=%s message=%s%n",
                    current.getSQLState(),
                    current.getMessage());
        }
        throw new RuntimeException(
                "Database exercise failed", exception);
    }
}
```

Let configuration `IllegalStateException` propagate naturally, or catch it only to add useful context and then rethrow.

- **Knowledge to review:** exception boundaries, checked exceptions, cause/suppressed/linked exceptions, process exit status.

### Issue 5 — Normal startup deletes every account in the table

- **Location:** `AccountRepository.java:31-33,93-100`; `Main.java:31-34`
- **Problem:** `delete()` executes unqualified `DELETE FROM accounts`, and `Main` calls it on every run.
- **Why it matters:** Repository method scope should be explicit. A whole-table destructive operation is not a normal prerequisite for demonstrating insert or transfer. Each delete/insert uses a separate auto-commit connection, so setup is not atomic.
- **Possible consequence:** Every application run destroys unrelated rows. If the first or second insert then fails, the database can remain empty or partially reseeded. It also hides the named volume's persistence behavior.
- **Recommended fix:** Remove whole-table deletion from ordinary `Main`. Create uniquely marked accounts, retain their generated IDs, and calculate balances only for those IDs. If a disposable-test cleanup operation is intentionally retained, name it explicitly (for example, `deleteAllForTest`) and keep it in test setup with an unmistakable warning.
- **Knowledge to review:** repository method semantics, scoped test data, auto-commit boundaries, destructive database operations.

### Issue 6 — The configured Hikari `maxLifetime` is not actually used

- **Location:** `src/main/java/com/example/poolpractice/config/DataSourceFactory.java:20-25`
- **Problem:** `config.setMaxLifetime(20_000)` is below HikariCP 7.1.0's minimum accepted positive value of `30_000` ms.
- **Why it matters:** Hikari validates numeric configuration. It logs that the value is too low and replaces it with the default `1_800_000` ms. With no SLF4J provider, you may notice only the general no-provider warning and miss the specific reset.
- **Possible consequence:** You may reason about a 20-second physical-connection lifetime while the pool is actually using 30 minutes. That is a pool-configuration misunderstanding, not merely a production-tuning preference.
- **Recommended fix:** Use a valid learning value such as:

```java
config.setMaxLifetime(600_000);
```

`maximumPoolSize=5`, `minimumIdle=2`, `connectionTimeout=20_000`, and `idleTimeout=20_000` are reasonable learning values. `idleTimeout=20_000` is valid because it is at least 10 seconds and `minimumIdle < maximumPoolSize`; it applies asynchronously to surplus idle physical connections above `minimumIdle`, not as an exact closing deadline.

- **Knowledge to review:** HikariCP configuration validation, `maxLifetime`, effective versus requested settings, pool logging.

### Issue 7 — `.env` and generated build output are tracked

- **Location:** empty `.gitignore`; tracked `.env`; tracked files under `target/`; credential-shaped values duplicated in `draft.md:1-10`
- **Problem:** Git currently tracks `.env`, and `.gitignore` is empty. It also tracks compiled classes and Maven compiler-status files.
- **Why it matters:** `.env` is the local file most likely to receive a real password. Ignoring a file later does not retroactively untrack it. Tracked build output can also become stale and make repository state misleading.
- **Possible consequence:** A future real credential can be committed accidentally, and generated binaries can disagree with source. The current password appears placeholder-like, so this review found **no evidence of an actual secret disclosure** and intentionally does not reproduce the value.
- **Recommended fix:**

```gitignore
.env
/target/
.idea/
.vscode/
*.iml
```

Then untrack already committed files:

```powershell
git rm --cached .env
git rm -r --cached target
```

Keep `.env.example`. If a real credential was ever committed, rotate it; removing the current file alone does not erase Git history.

- **Knowledge to review:** secret hygiene, `.gitignore` behavior, tracked versus ignored files, generated artifacts.

### Issue 8 — The Compose template and host-Java instructions disagree

- **Location:** `.env.example:1-4`; `draft.md:1-3,8-10`; `DatabaseSettings.java:7`
- **Problem:** The database and username in `.env.example` differ from the values used by the PowerShell/JDBC instructions and the current local Compose configuration.
- **Why it matters:** Compose's `.env` supplies `POSTGRES_*` interpolation, while host Java reads separate `DB_*` process variables. The names may be separate, but their database, user, password, and host port values must describe the same server.
- **Possible consequence:** A learner can copy `.env.example`, follow `draft.md`, and receive “database does not exist” or authentication errors despite both files appearing individually valid. Also, changing `POSTGRES_DB`, `POSTGRES_USER`, or `POSTGRES_PASSWORD` after the named data volume has initialized does not reconfigure that existing PostgreSQL cluster.
- **Recommended fix:** Choose one nonsecret database/user/port set and use it consistently in `.env.example` and the documented PowerShell commands. Add an explicit note:

```text
Docker Compose reads POSTGRES_* from .env.
The host JVM does not load .env.
Set DB_URL, DB_USERNAME, and DB_PASSWORD in the same PowerShell
process that launches Maven, using values matching PostgreSQL.
```

If `POSTGRES_PORT` changes, change the port in `DB_URL` too.

Treat `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` as first-initialization settings. For an existing volume, change roles/databases with SQL or deliberately recreate only this disposable exercise volume after confirming its data can be lost.

- **Knowledge to review:** Compose interpolation versus `System.getenv()`, parent/child process environments, host/container networking.

### Issue 9 — Pool diagnostics exist but do not demonstrate borrowing or returning

- **Location:** `Main.java:27-29`; `PoolDiagnostics.java:13-20`
- **Problem:** The only diagnostics call is commented out. `Main` never deliberately holds a connection while taking a snapshot, never prints the proxy class, and never observes the state after `Connection.close()`. The helper prints four unlabeled numbers with no pool name or object identity.
- **Why it matters:** The code uses a pool correctly, but it does not prove that you understand logical handles, active/idle transitions, or single-pool identity—central goals of this learning project.
- **Possible consequence:** A connection leak, repeated pool creation, or confusion between logical and physical connections could go unnoticed.
- **Recommended fix:** Give metrics names and observe them around one explicit borrow:

```java
public static void print(HikariDataSource pool, String label) {
    HikariPoolMXBean metrics = pool.getHikariPoolMXBean();
    System.out.printf(
            "[%s] pool=%s instance=%x "
                    + "total=%d active=%d idle=%d waiting=%d%n",
            label,
            pool.getPoolName(),
            System.identityHashCode(pool),
            metrics.getTotalConnections(),
            metrics.getActiveConnections(),
            metrics.getIdleConnections(),
            metrics.getThreadsAwaitingConnection());
}
```

```java
PoolDiagnostics.print(pool, "before borrow");
try (Connection connection = pool.getConnection()) {
    System.out.println(connection.getClass().getName());
    PoolDiagnostics.print(pool, "while borrowed");
}
PoolDiagnostics.print(pool, "after return");
```

Expect relationships, not exact counts: active should rise while borrowed and fall after close. Different proxy objects do not prove different physical connections. Pool shutdown itself is implemented correctly, but declaring the pool before `try (pool)` would also let you verify `pool.isClosed()` afterward.

- **Knowledge to review:** logical versus physical connections, Hikari proxy lifecycle, pool metrics, connection return versus pool shutdown.

## Minor Issues

### Issue 10 — `DatabaseSettings.toString()` prints placeholders instead of values

- **Location:** `DatabaseSettings.java:22-26`, `toString()`
- **Problem:** Java does not interpolate `${url}` or `${username}` inside an ordinary string literal, so the method prints those characters literally. The method does correctly avoid including the password.
- **Why it matters:** Configuration diagnostics should be accurate enough to distinguish a bad URL from an authentication problem, while still redacting secrets.
- **Possible consequence:** Startup logs can mislead you during environment-variable troubleshooting. This is not currently a credential leak.
- **Recommended fix:** Build the text explicitly and keep the password redacted:

```java
@Override
public String toString() {
    return "DatabaseSettings[url=" + url
            + ", username=" + username
            + ", password=<redacted>]";
}
```

- **Knowledge to review:** Java string construction, safe configuration logging, secret redaction.

### Issue 11 — Money is constructed from a `double` literal

- **Location:** `Main.java:40`, `new BigDecimal(20.00)`
- **Problem:** Constructing `BigDecimal` from a binary floating-point value can preserve an approximation rather than the intended decimal value. The particular value `20.00` is exactly representable, so it does not break this run, but the pattern is fragile for values such as `0.1`.
- **Why it matters:** Monetary calculations need predictable decimal values and scales.
- **Possible consequence:** A later amount can contain unexpected digits, fail scale validation, or be rounded unexpectedly by PostgreSQL.
- **Recommended fix:** Use a decimal string when the scale is meaningful:

```java
transferService.transfer(sourceId, destinationId, new BigDecimal("20.00"));
```

`BigDecimal.valueOf(20.00)` is also safer than the constructor, although the string form communicates the intended two-decimal representation most clearly.

- **Knowledge to review:** binary floating point, `BigDecimal` constructors, scale versus numeric equality.

### Issue 12 — `listAll()` relies on `SELECT *`

- **Location:** `AccountRepository.java:25-29,78-90`, `SELECT_SQL` and `listAll()`
- **Problem:** The query requests every column even though the mapper needs only `id`, `owner_name`, and `balance`. The current name-based mapping is correct and does not depend on column order.
- **Why it matters:** An explicit column list documents the repository contract and avoids fetching unrelated columns if the table evolves.
- **Possible consequence:** A schema change could add unnecessarily fetched data or make the query's intent less clear. It does not cause a current correctness failure.
- **Recommended fix:** Use:

```sql
SELECT id, owner_name, balance
FROM accounts
ORDER BY id
```

- **Knowledge to review:** stable SQL projections and repository mapping contracts.

### Issue 13 — Transaction failure messages lose useful context

- **Location:** `TransferService.java:81-96`, `debit()` and `credit()`
- **Problem:** The update-count failures say only `Something went wrong at DEBIT process` and `Something went wrong at CREDIT process`. The debit predicate intentionally combines two cases: a missing source account and insufficient funds.
- **Why it matters:** Re-throwing is correct, but a caller or learner still needs enough context to understand which business condition caused rollback.
- **Possible consequence:** Troubleshooting requires another query or debugger, and an integration scenario cannot distinguish expected insufficient funds from an unexpected missing row.
- **Recommended fix:** Include the relevant account ID and what the update count means, without including credentials or sensitive configuration. For example:

```java
if (updatedRows != 1) {
    throw new SQLException(
            "Debit affected " + updatedRows
                    + " rows; source account may be missing or underfunded: "
                    + sourceAccountId);
}
```

A later refinement could use domain-specific exceptions, but that is optional for this fundamentals project.

- **Knowledge to review:** update-count validation, actionable exception messages, separating database failures from business-rule failures.

### Issue 14 — The Maven/test setup still contains archetype residue

- **Location:** `pom.xml`; `src/test/java/com/example/AppTest.java`
- **Problem:** JUnit 4.11 is old, and the only test is `assertTrue(true)`, so Maven reports a passing test without checking JDBC behavior. The PostgreSQL driver can also be declared with runtime scope because application code uses JDBC interfaces rather than driver classes. Plugin declarations under `pluginManagement` are indirect for a small standalone application, although the configured `mvn exec:java` command works in this project. The POM emits Java 21 bytecode while the reference exercise deliberately targets Java 17 bytecode.
- **Why it matters:** A green build currently says only that compilation and a placeholder assertion succeeded; it gives no confidence in commit, rollback, or error propagation.
- **Possible consequence:** Transaction regressions can pass CI. Dependency intent is less clear than it could be, and the compiled application cannot run on a Java 17 runtime.
- **Recommended fix:** Replace the placeholder with focused integration tests once the critical fixes are made, update to a supported test framework version, and consider `runtime` scope for pgJDBC. Move plugins from `pluginManagement` to `plugins` if you want their application to be explicit. Java 21 is valid within the broad “Java 17+” stack, but this exercise's reference build deliberately emits Java 17 bytecode. The current release 21 output cannot run on Java 17, so set the release to 17 if that exercise-level compatibility is intended.
- **Knowledge to review:** Maven dependency scopes, plugin management versus plugin application, `--release` and bytecode compatibility, integration testing of database transactions.

## File-by-File Review

### `Main.java`

The strongest choice is lifecycle ownership: `Main` creates one `HikariDataSource`, passes that same instance to both collaborators as a `DataSource`, and closes the pool with try-with-resources. This correctly separates application-level pool shutdown from per-operation connection return.

The orchestration is not yet a trustworthy verification harness. It deletes all rows at startup, uses a `BigDecimal(double)` constructor, leaves diagnostics disabled, and catches failures only to print their messages. It neither runs a genuine rollback scenario nor asserts balances and total funds. Keep `Main` as the composition root, but make test data cleanup targeted, let fatal failures reach Maven/the process, and turn each learning requirement into an explicit check.

### `DatabaseSettings.java`

Reading through `System.getenv()` and rejecting null or blank values is correct. Making the settings immutable is also a good fit, and the current `toString()` does not expose the password. Its placeholder syntax is not valid Java interpolation, however, so safe diagnostic output is inaccurate. It would also help the surrounding documentation to state clearly that this class reads the host JVM process environment—it does not parse Compose's `.env` file.

### `DataSourceFactory.java`

This class has a focused responsibility and creates one configured `HikariDataSource` without retaining global state. URL, username, password, pool name, maximum size, minimum idle, connection timeout, and idle timeout are all set in a beginner-readable way. `maximumPoolSize = 5`, `minimumIdle = 2`, and the two 20-second timeout values are reasonable learning settings.

The 20-second `maxLifetime` is the exception: HikariCP 7.1.0 treats values below 30 seconds as invalid and resets this one to its 30-minute default; pool construction still proceeds. Use a valid educational value and confirm it with `pool.getMaxLifetime()` or with startup configuration logs after adding an SLF4J provider; the current no-provider setup discards Hikari's warning. Returning the concrete type here is useful because `Main` owns and closes the pool; repository and service constructors correctly accept only `DataSource`.

### `Account.java`

The record is a concise immutable row model, and `BigDecimal` is the correct Java type for PostgreSQL `NUMERIC`. No Hikari or JDBC concern leaks into the model. A later domain model might validate names and amounts, but that is not required for a JDBC mapping record.

### `AccountRepository.java`

The repository correctly depends on `DataSource`, borrows a connection per independent operation, scopes `Connection`, `PreparedStatement`, and `ResultSet` with try-with-resources, binds parameters, requests generated keys, checks update count, and reads the generated ID. `listAll()` also maps money to `BigDecimal` correctly.

The critical defect is that `insert()` catches `SQLException`, prints it, and returns `0`, turning a failed write into a plausible ID. Remove that catch or rethrow with the original cause. The unrestricted `delete()` makes normal startup destructive; replace it with targeted cleanup used only by a verification fixture. An explicit `SELECT` column list would make the mapping contract clearer.

### `TransferService.java`

The core JDBC transaction mechanics are the best part of the project. The service owns the transaction boundary, borrows exactly one connection, disables auto-commit, prepares both statements on that connection, validates both update counts, commits only after both updates, rolls back on `SQLException` or `RuntimeException`, preserves rollback failure as a suppressed exception, and rethrows the original failure. Resource scopes are tight, and the repository is correctly kept out of this multi-statement transaction.

The service is still unsafe for arbitrary input because it validates only negative IDs. It must reject null, zero, negative, and over-scale monetary amounts and same-account transfers before any SQL. The requested `-1` rollback demonstration is also blocked by prevalidation, so rollback is never proved. Use an absent but otherwise valid positive destination ID for that scenario.

### `PoolDiagnostics.java`

Using `HikariPoolMXBean` is appropriate in a diagnostics-only class; Hikari-specific coupling here is intentional rather than a repository abstraction leak. The helper retrieves the right active, idle, waiting, and total metrics.

It is currently unused and prints unlabeled integers, so it does not teach what happens while a connection is borrowed and after it is closed. Add labels, pool identity, and before/during/after snapshots. Remember that metrics can change asynchronously; verify relationships rather than exact idle counts.

### `schema.sql`

The schema is well chosen for the exercise: an identity primary key supports generated-key practice, the owner name has nonblank constraints, `NUMERIC(12,2)` models money, and a nonnegative balance check provides a database backstop. The service's atomic debit predicate complements this constraint.

The initialization script runs only when PostgreSQL initializes an empty data directory. Editing this file while reusing the named volume will not migrate the existing schema. The initial database, user, and password settings likewise do not re-bootstrap an existing data directory merely because `.env` changes. That behavior is normal and should be documented; do not automatically delete the persistent volume in routine runs.

### `compose.yaml` / `docker-compose.yml`

The present `compose.yaml` is technically sound for the stated architecture. It runs only PostgreSQL, publishes the database on loopback, uses required-variable interpolation, persists data in a named volume, mounts the schema read-only into the standard initialization directory, and has a correctly escaped health check. The live service was healthy during this review.

Because Java runs on the Windows host, a JDBC URL using `localhost` and the published host port is correct. If Java later became another Compose service, it would normally connect to the PostgreSQL service name and container port instead. Compose's `.env` interpolation does not place `DB_URL`, `DB_USERNAME`, or `DB_PASSWORD` into the separately launched host JVM.

### `.env.example` and `.gitignore`

The sample exposes variable names without needing to expose a real secret, but its database/user choices do not match the host-Java instructions. Align those files and explain the `POSTGRES_*` versus `DB_*` mapping. The empty `.gitignore` is a substantive repository hygiene problem because `.env` and `target/` are already tracked. The current password looked placeholder-like; this report deliberately records no credential value.

### `pom.xml`

HikariCP and pgJDBC versions resolve, compilation succeeds, and the exec plugin points at the correct main class. No Spring, JPA, or Hibernate dependency has been introduced. Java 21 is a valid runtime choice for a Java 17+ stack, but `--release 21` emits class-file version 65 and cannot run on Java 17. Because the reference exercise intentionally targets release 17, using 21 is a minor compatibility mismatch even though it works on the current JDK.

Clean up the placeholder JUnit 4.11 test/dependency, consider runtime scope for pgJDBC, and make plugin application explicit if desired. These are secondary to transaction and exception fixes.

### `AppTest.java` and `draft.md`

`AppTest` is a generated smoke placeholder and proves no behavior. Replace it with integration scenarios for commit, rollback after a successful debit, invalid amounts, and insert failure propagation. `draft.md` is useful as a scratch runbook, but it duplicates credential-shaped configuration and conflicts with `.env.example`; turn it into a consistent README-style guide using placeholders rather than keeping two sources of truth.

### Generated and peripheral files

The tracked `target/classes` and Maven compiler-status files are generated build output, not source. Untrack and ignore them. The local ignored modernization hook files do not alter this application's runtime architecture and revealed no DataSource or transaction behavior relevant to the review.

## My Main Knowledge Gaps

| Topic | Assessment | Evidence | What to Review |
|---|---|---|---|
| JDBC fundamentals | 🟢 Good | Prepared statements, binding, result iteration, generated keys, and update counts are mostly correct. | Keep practicing explicit projections and meaningful SQL failure reporting. |
| `DataSource` abstraction | 🟢 Good | Repository and service accept `DataSource`; only the composition/diagnostics layer needs Hikari's concrete API. | Composition-root ownership versus consumer dependencies. |
| Connection pooling | 🟡 Needs Review | One pool is owned and closed correctly, but `maxLifetime` is invalid and diagnostics do not demonstrate borrow/return. | Effective Hikari configuration, proxy connections, MXBean observations. |
| Connection lifecycle | 🟢 Good | Every borrowed connection is scoped with try-with-resources; no leak path was found. | Difference between closing a logical handle/releasing its pool entry and shutting down the pool. |
| Resource management | 🟢 Good | Connections, statements, and result sets have clear, nested ownership. | Continue preserving the same discipline in early-return and exception paths. |
| Transactions | 🟡 Needs Review | One-connection commit/rollback mechanics are strong, but unsafe monetary inputs can violate transfer semantics and rollback is not actually demonstrated. | Preconditions, monetary scale, transaction invariants, failure injection, atomicity proofs. |
| Exception handling | 🔴 Weak / Relearn | Rollback exceptions are preserved well, but repository and top-level catches convert failures into apparent success. | Propagation, contextual wrapping, exit status, fake fallback values. |
| Repository/service boundaries | 🟡 Needs Review | The service correctly owns the multi-statement transaction, but repository-wide deletion is mixed into normal application flow. | Transaction ownership, test-fixture cleanup, narrow repository operations. |
| PostgreSQL and money | 🟡 Needs Review | Schema constraints and `NUMERIC` are good; over-scale and non-positive transfer input are not controlled in Java. | Decimal precision/scale, driver/database rounding, constraints as a backstop. |
| Docker Compose | 🟢 Good | Loopback publishing, volume, init script, health check, and host/container boundary are correct. | Fresh-volume initialization versus migrations. |
| Environment configuration | 🟡 Needs Review | Required JVM variables are validated, but Compose and host settings are inconsistent and `.env` is tracked. | Interpolation versus process environment, single source of truth, secret hygiene. |
| Maven and testing | 🟡 Needs Review | Dependencies compile and `exec:java` works, but the only test is a vacuous JUnit 4 assertion. | Scopes, plugin application, database integration tests and failure assertions. |

## Fix Priority

### 1. Fix Immediately

1. Validate transfer invariants before borrowing a connection: non-null, positive, at most two decimal places, valid distinct account IDs.
2. Make `AccountRepository.insert()` propagate `SQLException`; never return a fabricated ID after a failed write.
3. Stop converting fatal startup/transaction failures into a successful process result in `Main`.
4. Remove `.env` and `target/` from version control and add the appropriate ignore rules. Rotate a credential if any non-placeholder secret was ever committed.

### 2. Fix Next

1. Add a rollback scenario in which the debit really succeeds and the credit then fails, using an absent positive destination ID.
2. Assert the successful transfer balances and conserved total, then assert unchanged balances after rollback.
3. Remove unrestricted table deletion from normal startup and use isolated or targeted verification data.
4. Set `maxLifetime` to a supported value and run the diagnostics before, during, and after a borrowed connection.

### 3. Improve Afterwards

1. Align `.env.example`, the host-Java commands, and the Compose values, with a clear explanation of the two environment contexts.
2. Correct the safely redacted `DatabaseSettings.toString()`.
3. Construct money from decimal strings, select explicit columns, and make update-count errors more informative.
4. Replace the placeholder test with transaction integration tests and clean up Maven scopes/plugin placement.

### 4. Nice-to-Have Improvements

1. Introduce small domain exception types if callers later need to distinguish missing accounts from insufficient funds.
2. Give verification data a repeatable fixture strategy rather than making the demo dependent on a globally empty table.
3. Add a bounded pool-contention exercise to observe `threadsAwaitingConnection`; this is useful practice, not required production tuning.

## Concepts I Should Review

1. **Transaction correctness starts before `setAutoCommit(false)`.** A technically correct commit/rollback sequence cannot rescue invalid business input. Define invariants for IDs and money, validate them before SQL, and ensure the database schema remains a second line of defense.
2. **One logical failure must remain a failure at every layer.** A repository should either return a real generated key or throw. A process that cannot initialize or transfer should exit unsuccessfully. Review when to propagate an exception unchanged and when to wrap it while retaining the cause.
3. **Rollback must be proved after a real mutation.** A useful rollback test confirms that statement one changed a row, statement two failed, rollback ran, and the original balances and total remained unchanged. Failing before the transaction starts proves validation, not rollback.
4. **Pool handles and pool lifetime are different lifecycles.** `Connection.close()` closes the logical proxy and normally releases its healthy underlying physical connection for pool reuse; an evicted or broken physical connection is closed instead. `HikariDataSource.close()` shuts down the application-owned pool. Effective configuration—not merely setter calls—determines what the pool actually uses.
5. **Decimal transfer invariants need an explicit policy.** Review `BigDecimal` construction, sign, scale, equality, PostgreSQL `NUMERIC(12,2)`, and whether amounts with extra fractional digits are rejected or deliberately rounded once before both statements.
6. **Compose interpolation and JVM environment variables are separate channels.** Trace each value from `.env` into the container and from PowerShell into Maven/the JVM. Keep templates consistent, never commit live secrets, and use `localhost` only because this Java process runs on the host.

A good self-check is to explain, without looking at the code, exactly who creates the pool, who borrows each connection, who returns it, who owns a transfer transaction, and what observable evidence proves rollback.

## Final Assessment

**🟡 Mostly ready, but review these concepts first**

You demonstrate a sound foundation in the `DataSource` abstraction, one-pool ownership, JDBC resource scoping, generated keys, and the mechanics of a one-connection transaction. In particular, the commit/rollback structure and suppressed rollback-failure handling show more than beginner-level awareness.

The project is not yet safe enough to count transaction fundamentals as fully understood because invalid monetary input can produce incorrect transfer semantics, write failures can be reported as fake success, and the intended rollback behavior is never exercised. Before moving to the next backend topic, fix and verify these five concepts:

1. transfer invariants and `BigDecimal` scale;
2. exception propagation without fake fallback values;
3. a rollback test that fails after the debit;
4. logical-handle close/physical-connection reuse versus pool shutdown, plus effective Hikari settings;
5. Compose/JVM environment separation and secret hygiene.
