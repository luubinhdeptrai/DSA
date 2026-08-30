# DataSource and Connection Pool Exercise

## Account Transfer Pool Practice

**Project name:** `datasource-pool-practice`  
**Estimated time:** 30–60 minutes  
**Stack:** Java 17+, Maven, PostgreSQL, JDBC, `javax.sql.DataSource`, HikariCP  
**Not used:** Spring, Spring Boot, JPA, Hibernate, Docker

This is one small console project. Its purpose is not to teach banking logic; it is to make connection-pool ownership, borrowing, returning, transactions, rollback, and shutdown visible.

> Attempt the starter project and TODOs before opening the optional reference solution at the very end.

## Learning Objectives

By completing the exercise, you will practice how to:

- configure one application-lifetime `HikariDataSource`;
- read URL, username, and password from environment variables;
- pass the same `DataSource` to multiple application objects;
- borrow connections with `dataSource.getConnection()`;
- return borrowed handles with try-with-resources;
- insert accounts and select all accounts;
- run debit and credit on the same transactional connection;
- commit a successful transfer;
- roll back a transfer whose destination does not exist;
- inspect safe pool metrics without logging credentials;
- close the pool once when the console application exits.

## Scenario and Mental Model

The program creates two accounts for each run:

```text
Source account       Destination account
    100.00      → 25.00 →      50.00

After success:
     75.00                       75.00
Total remains 150.00
```

It then intentionally transfers `10.00` to missing account ID `-1`:

```text
debit source succeeds inside transaction
        ↓
credit missing destination affects 0 rows
        ↓
throw SQLException
        ↓
rollback
        ↓
source remains 75.00 and total remains 150.00
```

Application architecture:

```text
Main
 ├── creates ONE HikariDataSource
 ├── AccountRepository ─┐
 └── TransferService ───┴─→ same javax.sql.DataSource
                                  ↓
                              HikariCP
                                  ↓
                               pgJDBC
                                  ↓
                             PostgreSQL
```

Resource lifetimes:

```text
pool:       ├──────────── whole application ────────────┤
connection:     ├─ insert ─┤  ├─ select ─┤  ├─ transfer ─┤
statement:        ├ SQL ┤       ├ SQL ┤      ├ SQL ┤
```

## Expected Project Structure

```text
datasource-pool-practice/
├── .gitignore
├── pom.xml
├── database/
│   └── schema.sql
└── src/
    └── main/
        └── java/
            └── com/example/poolpractice/
                ├── Account.java
                ├── AccountRepository.java
                ├── DatabaseSettings.java
                ├── DataSourceFactory.java
                ├── PoolDiagnostics.java
                ├── TransferService.java
                └── Main.java
```

All Java files use:

```java
package com.example.poolpractice;
```

Do not create a Spring configuration file or keep a `Connection` field in any class.

---

## Database Setup

### 1. Create a local role and database

Open `psql` as a PostgreSQL administrator:

```powershell
psql -h localhost -U postgres
```

Inside `psql`:

```sql
CREATE ROLE pool_app LOGIN;
\password pool_app
CREATE DATABASE pool_practice OWNER pool_app;
\q
```

`\password` prompts securely instead of placing a password in SQL or shell history. Choose a local practice password and do not commit it anywhere.

If the role or database already exists, do not recreate it. Connect to the existing practice database instead.

### 2. Create `database/schema.sql`

```sql
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_name VARCHAR(100) NOT NULL
        CHECK (btrim(owner_name) <> ''),
    balance NUMERIC(12, 2) NOT NULL
        CHECK (balance >= 0)
);
```

Run it as the application user:

```powershell
psql -h localhost -U pool_app -d pool_practice -f .\database\schema.sql
```

Verify:

```powershell
psql -h localhost -U pool_app -d pool_practice -c '\d accounts'
```

Expected columns include `id`, `owner_name`, and `balance`, with primary-key and nonnegative-balance constraints.

---

## Maven Dependencies

Create `pom.xml` using these stable versions:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>datasource-pool-practice</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>7.1.0</version>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.13</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.15.0</version>
                <configuration>
                    <release>17</release>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.6.3</version>
                <configuration>
                    <mainClass>com.example.poolpractice.Main</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Why only two application dependencies:

| Dependency | Purpose |
|---|---|
| HikariCP | Implements a pooling `DataSource` |
| pgJDBC | Implements PostgreSQL JDBC communication at runtime |

The source code uses standard JDBC interfaces, so pgJDBC can have runtime scope. Modern pgJDBC registers itself automatically; do not add `Class.forName(...)` or `setDriverClassName(...)` unless you are diagnosing an unusual discovery failure.

HikariCP uses the SLF4J logging API. This minimal exercise deliberately does not add a logging provider. A one-time “no SLF4J providers” warning is not a pool failure; the application prints pool metrics directly.

Create `.gitignore`:

```gitignore
/target/
.idea/
.vscode/
*.iml
```

No credential file is needed because the exercise uses process environment variables.

---

## Environment Configuration

Set variables in the same PowerShell window where you will run Maven:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/pool_practice'
$env:DB_USERNAME = 'pool_app'
$env:DB_PASSWORD = '<the password you entered with \password>'
```

Check only whether names exist; do not print the secret:

```powershell
@('DB_URL', 'DB_USERNAME', 'DB_PASSWORD') | ForEach-Object {
    [pscustomobject]@{
        Name = $_
        IsSet = -not [string]::IsNullOrWhiteSpace(
            [Environment]::GetEnvironmentVariable($_)
        )
    }
}
```

All three `IsSet` values should be `True`.

---

## Starter Code

The starter methods deliberately throw `UnsupportedOperationException` or contain TODOs. This keeps the project structurally coherent while leaving the important work to you.

### `Account.java`

```java
package com.example.poolpractice;

import java.math.BigDecimal;

public record Account(
        long id,
        String ownerName,
        BigDecimal balance
) {
}
```

### `DatabaseSettings.java`

```java
package com.example.poolpractice;

public record DatabaseSettings(
        String url,
        String username,
        String password
) {
    public static DatabaseSettings fromEnvironment() {
        // TODO: read DB_URL, DB_USERNAME, and DB_PASSWORD
        throw new UnsupportedOperationException("TODO: load settings");
    }

    private static String required(String name) {
        // TODO: fail clearly when the named variable is absent/blank
        throw new UnsupportedOperationException("TODO: read " + name);
    }

    @Override
    public String toString() {
        // Never expose the password if this object is logged.
        return "DatabaseSettings[url=" + url
                + ", username=" + username
                + ", password=<redacted>]";
    }
}
```

### `DataSourceFactory.java`

```java
package com.example.poolpractice;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceFactory {
    private DataSourceFactory() {
    }

    public static HikariDataSource create(DatabaseSettings settings) {
        HikariConfig config = new HikariConfig();

        // TODO: configure HikariDataSource
        // Required values: URL, username, password
        // Pool name: account-practice-pool
        // maximumPoolSize=3, minimumIdle=1
        // connectionTimeout=5_000 ms
        // idleTimeout=60_000 ms
        // maxLifetime=600_000 ms

        throw new UnsupportedOperationException("TODO: create pool");
    }
}
```

### `AccountRepository.java`

```java
package com.example.poolpractice;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class AccountRepository {
    private static final String INSERT_SQL = """
            INSERT INTO accounts (owner_name, balance)
            VALUES (?, ?)
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id, owner_name, balance
            FROM accounts
            ORDER BY id
            """;

    private final DataSource dataSource;

    public AccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long insert(String ownerName, BigDecimal openingBalance)
            throws SQLException {
        // TODO: obtain a Connection
        // TODO: execute INSERT with PreparedStatement
        // TODO: read and return the generated key
        throw new UnsupportedOperationException("TODO: insert account");
    }

    public List<Account> findAll() throws SQLException {
        // TODO: obtain a Connection
        // TODO: execute SELECT and map every ResultSet row
        // TODO: return results (an empty List when there are no rows)
        throw new UnsupportedOperationException("TODO: find all accounts");
    }
}
```

The imports are clues, not extra requirements. Each repository method should borrow and return its own connection because each method is one independent database operation.

### `TransferService.java`

```java
package com.example.poolpractice;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class TransferService {
    private static final String DEBIT_SQL = """
            UPDATE accounts
            SET balance = balance - ?
            WHERE id = ? AND balance >= ?
            """;

    private static final String CREDIT_SQL = """
            UPDATE accounts
            SET balance = balance + ?
            WHERE id = ?
            """;

    private final DataSource dataSource;

    public TransferService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void transfer(long fromId, long toId, BigDecimal amount)
            throws SQLException {
        // TODO: validate different accounts, a positive amount, and at most 2 decimals
        // TODO: obtain ONE Connection for the whole transaction
        // TODO: disable auto-commit
        // TODO: debit and credit using that same Connection
        // TODO: require an update count of exactly 1 for each statement
        // TODO: commit
        // TODO: rollback if any SQLException or RuntimeException occurs
        throw new UnsupportedOperationException("TODO: transfer money");
    }

    private int debit(Connection connection, long accountId, BigDecimal amount)
            throws SQLException {
        // TODO: execute DEBIT_SQL using the supplied Connection
        throw new UnsupportedOperationException("TODO: debit");
    }

    private int credit(Connection connection, long accountId, BigDecimal amount)
            throws SQLException {
        // TODO: execute CREDIT_SQL using the supplied Connection
        throw new UnsupportedOperationException("TODO: credit");
    }
}
```

The helper methods receive a `Connection` on purpose. If they call `dataSource.getConnection()` themselves, debit and credit may use different database sessions and no longer form one transaction.

### `PoolDiagnostics.java`

```java
package com.example.poolpractice;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public final class PoolDiagnostics {
    private PoolDiagnostics() {
    }

    public static void print(HikariDataSource pool, String label) {
        HikariPoolMXBean metrics = pool.getHikariPoolMXBean();

        // TODO: print the label, pool name, and pool object identity
        // TODO: print total, active, idle, and waiting connection counts
        // Do not print the JDBC URL, username, or password here.
    }
}
```

These values are a moment-in-time snapshot. Startup and scheduling can change exact counts, so look for relationships such as “active increases while a handle is borrowed” rather than memorizing one exact line.

### `Main.java`

```java
package com.example.poolpractice;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws SQLException {
        DatabaseSettings settings = DatabaseSettings.fromEnvironment();
        HikariDataSource pool = DataSourceFactory.create(settings);

        try {
            DataSource sharedDataSource = pool;
            AccountRepository repository =
                    new AccountRepository(sharedDataSource);
            TransferService transfers =
                    new TransferService(sharedDataSource);

            // TODO: observe one borrowed and returned connection
            // TODO: insert two accounts and select all accounts
            // TODO: execute and verify a successful transfer
            // TODO: force a missing-destination failure and verify rollback
        } finally {
            // TODO: close pool
        }
    }
}
```

Do not create another `HikariDataSource` in `AccountRepository` or `TransferService`. `Main` owns the one pool and passes it down as the standard `DataSource` interface.

---

## TODO Tasks

Complete these in order. Compile after each small group rather than waiting until the end.

### Task 1 — Load configuration safely

In `DatabaseSettings`:

1. Read `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` with `System.getenv(...)`.
2. Reject a missing or blank value with a clear `IllegalStateException` that names the variable.
3. Never include the password value in an error or `toString()`.

### Task 2 — Configure one pool

In `DataSourceFactory`:

1. Set the JDBC URL, username, and password from `DatabaseSettings`.
2. Apply the pool settings listed in the starter comments.
3. Return one `new HikariDataSource(config)`.

All Hikari time settings in this exercise are milliseconds. `connectionTimeout` is how long a caller waits to borrow a connection; it is not a SQL query timeout.

### Task 3 — Implement insert

In `AccountRepository.insert(...)`:

1. Borrow a connection with `dataSource.getConnection()`.
2. Create a `PreparedStatement` with `Statement.RETURN_GENERATED_KEYS`.
3. Bind the owner and opening balance.
4. Require `executeUpdate()` to report exactly one inserted row.
5. Call `getGeneratedKeys()`, check `next()`, and return the new ID.
6. Close the connection, statement, and generated-keys result set with try-with-resources.

### Task 4 — Implement select-all

In `AccountRepository.findAll()`:

1. Borrow a connection.
2. Execute the ordered query.
3. Map every row to `new Account(...)`.
4. Return a list; return an empty list rather than `null` if the table is empty.
5. Close all JDBC resources with try-with-resources.

### Task 5 — Implement one real transaction

In `TransferService.transfer(...)`:

1. Reject null, zero, or negative amounts, amounts with more than two decimal places, and transfers to the same account.
2. Borrow exactly one connection.
3. Call `setAutoCommit(false)` before either update.
4. Debit the source and require an update count of `1`.
5. Credit the destination and require an update count of `1`.
6. Commit only after both updates succeed.
7. On `SQLException` or `RuntimeException`, roll back and rethrow the original exception.
8. If rollback itself fails, attach that failure with `original.addSuppressed(rollbackFailure)`.
9. Return the connection with try-with-resources. Hikari resets important connection state before another borrower receives it.

The debit predicate checks `balance >= amount`; therefore an update count of zero may mean “source missing” or “insufficient funds.” Either must abort the transfer.

### Task 6 — Make pool behavior visible

Complete `PoolDiagnostics.print(...)`. In `Main`:

1. Print metrics after pool startup.
2. Borrow one connection in try-with-resources.
3. Print its runtime class and metrics while it is borrowed.
4. Leave the try block, then print metrics again.
5. Print `System.identityHashCode(pool)` with each snapshot; the same identity must appear every time.

You should see a Hikari proxy class. After `close()`, active count should fall and an idle connection should normally be available. That demonstrates handle return. It does not prove that Java proxy object identities or physical connection identities must be identical.

### Task 7 — Prove commit and rollback

For each run, use unique owner names, for example by appending `System.currentTimeMillis()`:

1. Insert a source account with `100.00`.
2. Insert a destination account with `50.00`.
3. Transfer `25.00` from source to destination.
4. Read both back: expected balances are `75.00` and `75.00`; total is `150.00`.
5. Record the source balance and pair total.
6. Attempt a transfer of `10.00` to missing destination ID `-1`.
7. Catch the expected `SQLException`.
8. Read both real accounts again and compare database-derived balances with the values from step 5.
9. Fail the program if the source changed or the total changed.

The missing destination is deliberate: the debit runs first, then the credit affects zero rows, so rollback has real work to undo.

### Task 8 — Shut down the owner resource

Close the one `HikariDataSource` in `Main` even when work fails. A try-with-resources statement around the pool is a compact solution. After shutdown, `pool.isClosed()` should be `true`.

Do not close the pool inside repository methods. They own borrowed connections, not the application-lifetime pool.

---

## Hints

Open these only after trying the relevant TODO.

<details>
<summary>Hint: generated keys</summary>

Pass `Statement.RETURN_GENERATED_KEYS` when preparing the INSERT. After a successful `executeUpdate()`, open a nested try-with-resources block around `statement.getGeneratedKeys()` and check `keys.next()` before reading column 1.

</details>

<details>
<summary>Hint: mapping rows</summary>

Create an `ArrayList<Account>`. In `while (resultSet.next())`, read `id` with `getLong`, `owner_name` with `getString`, and `balance` with `getBigDecimal`.

</details>

<details>
<summary>Hint: transaction shape</summary>

The outer resource is one borrowed `Connection`. Disable auto-commit, call both helper methods with that connection, then commit. Catch `SQLException | RuntimeException` inside the connection's try block so the connection is still available when you call `rollback()`.

</details>

<details>
<summary>Hint: checking balances</summary>

Use `BigDecimal.compareTo(...) == 0` for numeric equality. `new BigDecimal("75.00")` and `new BigDecimal("75.0")` have the same numeric value but are not equal according to `BigDecimal.equals(...)` because their scales differ.

</details>

<details>
<summary>Hint: pool shutdown</summary>

`HikariDataSource` is `AutoCloseable`, so the pool itself can be the resource of a try-with-resources statement. The general `DataSource` interface does not promise a `close()` method; the lifecycle owner deliberately retains the Hikari type.

</details>

---

## Expected Program Behavior

A successful run should demonstrate these facts (wording and metric counts may differ):

```text
[startup] pool=account-practice-pool instance=... total=... active=0 idle=... waiting=0
Borrowed logical connection: com.zaxxer.hikari.pool.HikariProxyConnection
[while borrowed] instance=<same value> ... active=1 ...
[after Connection.close() returned it] instance=<same value> ... active=0 idle=...
Created accounts <source-id> and <destination-id>; pair total=150.00
After success: from=75.00 to=75.00 total=150.00
Expected failure triggered rollback: SQLState=null message=Destination account not found: -1
Rollback preserved balances: true
[before shutdown] instance=<same value> ... active=0 ... waiting=0
Pool closed: true
```

`SQLState=null` is expected in that particular line: PostgreSQL treats an UPDATE that matches zero rows as normal, and the application then creates its own `SQLException` after checking the update count. A server-raised JDBC error would normally carry a PostgreSQL SQLState.

Exact total and idle counts can vary because pool creation and housekeeping are asynchronous. The invariants matter:

- the same pool name appears throughout;
- the same pool object identity appears throughout;
- at most the configured maximum of three physical connections exists;
- an in-use handle is active;
- a closed handle is no longer active;
- the successful transfer preserves `150.00` total;
- the failed transfer preserves the pre-failure balances;
- the application closes the pool.

---

## Manual Testing Instructions

Run commands from the `datasource-pool-practice` directory containing `pom.xml`.

### 1. Compile and inspect dependencies

```powershell
mvn clean compile
mvn dependency:tree
```

Expected:

- `BUILD SUCCESS`;
- HikariCP appears in the dependency tree;
- PostgreSQL appears with runtime scope;
- no Spring, JPA, or Hibernate dependency appears.

### 2. Run the application

```powershell
mvn exec:java
```

If PostgreSQL rejects the JVM time-zone name `Asia/Saigon`, run:

```powershell
mvn "-Duser.timezone=Asia/Ho_Chi_Minh" exec:java
```

That option changes only the JVM time-zone setting for this run; it does not change the database credentials or pool design.

### 3. Verify committed state in PostgreSQL

Console output alone is not proof. Use the IDs printed by the program:

```powershell
psql -h localhost -U pool_app -d pool_practice
```

Inside `psql`, replace the placeholders:

```sql
SELECT id, owner_name, balance
FROM accounts
WHERE id IN (<source-id>, <destination-id>)
ORDER BY id;

SELECT SUM(balance) AS pair_total
FROM accounts
WHERE id IN (<source-id>, <destination-id>);
```

Expected after both the successful and deliberately failed transfers:

| Row | Expected balance |
|---|---:|
| Source | `75.00` |
| Destination | `75.00` |
| Sum | `150.00` |

### 4. Verify each run creates only its intended rows

Use the unique timestamp marker printed in owner names:

```sql
SELECT id, owner_name, balance
FROM accounts
WHERE owner_name LIKE '%<timestamp-marker>%'
ORDER BY id;
```

Expected: exactly two rows. The failed transfer updates existing rows temporarily; it must not insert another account.

### 5. Inspect database sessions while the app is alive (optional)

Connection pools keep physical sessions open for reuse. To pause this console program before shutdown, temporarily add the following immediately after the `before shutdown` metrics line in `Main`:

```java
System.out.println("Pool is open. Inspect pg_stat_activity, then press Enter.");
try {
    System.in.read();
} catch (java.io.IOException exception) {
    throw new RuntimeException("Could not read console input", exception);
}
```

Run the program and, while it waits, inspect from a second `psql` terminal:

```sql
SELECT usename, datname, state, application_name
FROM pg_stat_activity
WHERE datname = 'pool_practice';
```

Idle sessions during application life are normal for a pool. Press Enter to let `Main` close the pool, then run the query again: the application's Hikari sessions should disappear. Administrative tools may have their own sessions. Remove the temporary pause afterward.

---

## Pool Reuse Observation

Use this evidence carefully:

```text
getConnection()          -> active count rises
Connection.close()       -> active count falls; idle count normally rises
later getConnection()    -> pool supplies another logical handle
pool.close()             -> application-owned physical sessions close
```

`Connection.close()` on a healthy Hikari proxy normally returns its underlying physical connection to the pool. It does not mean every close must preserve every physical connection: broken, expired, or evicted connections may be physically closed and replaced.

Avoid this misleading test:

```java
connection1 == connection2
```

Hikari may give different proxy objects around a reused physical connection. Pool metrics, bounded database sessions, and correct borrow/return behavior are better beginner-level evidence.

---

## Common Mistakes

| Symptom | Likely cause | What to inspect |
|---|---|---|
| `DB_PASSWORD is required` | Variable is absent in the Maven process | Set variables in the same PowerShell window; verify only `IsSet` |
| `Connection refused` | PostgreSQL is stopped or URL/port is wrong | `DB_URL`, PostgreSQL service, port `5432` |
| `password authentication failed` | Wrong role/password | `DB_USERNAME`; reset password securely with `\password` |
| `relation "accounts" does not exist` | Schema ran in a different database/schema | Run `schema.sql` against `pool_practice` as `pool_app` |
| Pool grows on every operation | Code creates `HikariDataSource` repeatedly | `Main`, repository/service constructors |
| Timeout waiting for a connection | Handles leaked or pool exhausted | Every `getConnection()` path and try-with-resources |
| Debit remains after failed credit | Missing rollback or different connections used | `TransferService.transfer`, `debit`, and `credit` |
| Rollback test “passes” without debit | Failure occurs before first update | Ensure source exists/funded and missing destination is checked second |
| Source goes negative | Debit SQL omits `balance >= ?` or binds incorrectly | `DEBIT_SQL` and parameter order |
| Transfer silently succeeds to missing ID | Update counts ignored | Require exactly one row from debit and credit |
| Pool never shuts down | Owner does not close `HikariDataSource` | `Main`'s outer lifecycle |
| SLF4J provider warning | No logging backend in this minimal project | It is harmless here; do not confuse it with a JDBC error |

Never “fix” an acquisition timeout by simply increasing pool size before checking for leaked connections or slow transactions.

---

## Self-Review Checklist

- [ ] The project compiles on Java 17+.
- [ ] The POM contains HikariCP and pgJDBC only as application dependencies.
- [ ] Credentials come from environment variables.
- [ ] Password values never appear in output or exception messages.
- [ ] `Main` creates exactly one `HikariDataSource`.
- [ ] Repository and service receive the same `DataSource` object.
- [ ] `AccountRepository` has no long-lived `Connection` field.
- [ ] Every borrowed connection is closed by try-with-resources.
- [ ] Every statement and result set is closed.
- [ ] INSERT uses a prepared statement and returns a generated ID.
- [ ] SELECT returns ordered `Account` objects and never returns `null`.
- [ ] Debit and credit use one shared transaction connection.
- [ ] Auto-commit is disabled before the first transfer update.
- [ ] Both update counts are checked.
- [ ] Success commits only after both updates.
- [ ] Failure rolls back and rethrows the original exception.
- [ ] A rollback failure is attached as a suppressed exception.
- [ ] SQL verification confirms `75.00`, `75.00`, and total `150.00`.
- [ ] Metrics show borrow/return behavior without exposing secrets.
- [ ] The application closes the pool once at shutdown.

---

## Short Conceptual Questions

Answer these in your own words after completing the program.

1. Why is a `DataSource` better than calling `DriverManager` throughout application code?
2. Does every `DataSource` automatically provide connection pooling?
3. What usually happens when `close()` is called on a healthy Hikari proxy connection?
4. Why should a repository borrow a connection per operation instead of storing one in a field?
5. Why must debit and credit use the exact same `Connection`?
6. What does `connectionTimeout` limit, and what does it not limit?
7. Why can a failed destination update prove rollback only if the debit ran first?
8. Who owns the pool, and who owns each borrowed connection?

---

## Optional Full Solution

Do not compare line by line until your own version compiles and you have run both the success and failure paths. Equivalent designs are valid if they preserve the same ownership, transaction, resource, and security rules.

<details>
<summary>Reveal the complete reference implementation</summary>

The `pom.xml`, `.gitignore`, and `database/schema.sql` are exactly the versions shown earlier. Replace the starter Java files with the following.

### Solution: `Account.java`

```java
package com.example.poolpractice;

import java.math.BigDecimal;

public record Account(long id, String ownerName, BigDecimal balance) {
}
```

### Solution: `DatabaseSettings.java`

```java
package com.example.poolpractice;

public record DatabaseSettings(
        String url,
        String username,
        String password
) {
    public static DatabaseSettings fromEnvironment() {
        return new DatabaseSettings(
                required("DB_URL"),
                required("DB_USERNAME"),
                required("DB_PASSWORD")
        );
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required and must not be blank");
        }
        return value;
    }

    @Override
    public String toString() {
        return "DatabaseSettings[url=" + url
                + ", username=" + username
                + ", password=<redacted>]";
    }
}
```

### Solution: `DataSourceFactory.java`

```java
package com.example.poolpractice;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceFactory {
    private DataSourceFactory() {
    }

    public static HikariDataSource create(DatabaseSettings settings) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("account-practice-pool");
        config.setJdbcUrl(settings.url());
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(5_000);
        config.setIdleTimeout(60_000);
        config.setMaxLifetime(600_000);
        return new HikariDataSource(config);
    }
}
```

### Solution: `AccountRepository.java`

```java
package com.example.poolpractice;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AccountRepository {
    private static final String INSERT_SQL = """
            INSERT INTO accounts (owner_name, balance)
            VALUES (?, ?)
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id, owner_name, balance
            FROM accounts
            ORDER BY id
            """;

    private final DataSource dataSource;

    public AccountRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    public long insert(String ownerName, BigDecimal openingBalance)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, ownerName);
            statement.setBigDecimal(2, openingBalance);

            int rows = statement.executeUpdate();
            if (rows != 1) {
                throw new SQLException("Expected one inserted row, got " + rows);
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Insert succeeded but returned no generated key");
                }
                return keys.getLong(1);
            }
        }
    }

    public List<Account> findAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                accounts.add(new Account(
                        rows.getLong("id"),
                        rows.getString("owner_name"),
                        rows.getBigDecimal("balance")
                ));
            }
        }

        return accounts;
    }
}
```

### Solution: `TransferService.java`

```java
package com.example.poolpractice;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public final class TransferService {
    private static final String DEBIT_SQL = """
            UPDATE accounts
            SET balance = balance - ?
            WHERE id = ? AND balance >= ?
            """;

    private static final String CREDIT_SQL = """
            UPDATE accounts
            SET balance = balance + ?
            WHERE id = ?
            """;

    private final DataSource dataSource;

    public TransferService(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    public void transfer(long fromId, long toId, BigDecimal amount)
            throws SQLException {
        validateTransfer(fromId, toId, amount);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int debited = debit(connection, fromId, amount);
                if (debited != 1) {
                    throw new SQLException(
                            "Source account is missing or has insufficient funds: " + fromId);
                }

                int credited = credit(connection, toId, amount);
                if (credited != 1) {
                    throw new SQLException("Destination account not found: " + toId);
                }

                connection.commit();
            } catch (SQLException | RuntimeException original) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    original.addSuppressed(rollbackFailure);
                }
                throw original;
            }
        }
    }

    private int debit(Connection connection, long accountId, BigDecimal amount)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DEBIT_SQL)) {
            statement.setBigDecimal(1, amount);
            statement.setLong(2, accountId);
            statement.setBigDecimal(3, amount);
            return statement.executeUpdate();
        }
    }

    private int credit(Connection connection, long accountId, BigDecimal amount)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(CREDIT_SQL)) {
            statement.setBigDecimal(1, amount);
            statement.setLong(2, accountId);
            return statement.executeUpdate();
        }
    }

    private static void validateTransfer(
            long fromId,
            long toId,
            BigDecimal amount
    ) {
        if (fromId == toId) {
            throw new IllegalArgumentException("Source and destination must differ");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount must have at most two decimal places");
        }
    }
}
```

### Solution: `PoolDiagnostics.java`

```java
package com.example.poolpractice;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public final class PoolDiagnostics {
    private PoolDiagnostics() {
    }

    public static void print(HikariDataSource pool, String label) {
        HikariPoolMXBean metrics = pool.getHikariPoolMXBean();
        if (metrics == null) {
            System.out.printf("[%s] pool=%s not started%n",
                    label, pool.getPoolName());
            return;
        }

        System.out.printf(
                "[%s] pool=%s instance=%x "
                        + "total=%d active=%d idle=%d waiting=%d%n",
                label,
                pool.getPoolName(),
                System.identityHashCode(pool),
                metrics.getTotalConnections(),
                metrics.getActiveConnections(),
                metrics.getIdleConnections(),
                metrics.getThreadsAwaitingConnection()
        );
    }
}
```

### Solution: `Main.java`

```java
package com.example.poolpractice;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            run();
        } catch (SQLException exception) {
            printSQLExceptionChain(exception);
            throw new RuntimeException("Database exercise failed", exception);
        }
    }

    private static void run() throws SQLException {
        DatabaseSettings settings = DatabaseSettings.fromEnvironment();
        HikariDataSource pool = DataSourceFactory.create(settings);

        try (pool) {
            DataSource sharedDataSource = pool;
            AccountRepository repository =
                    new AccountRepository(sharedDataSource);
            TransferService transfers =
                    new TransferService(sharedDataSource);

            PoolDiagnostics.print(pool, "startup");
            try (Connection connection = sharedDataSource.getConnection()) {
                System.out.println(
                        "Borrowed logical connection: "
                                + connection.getClass().getName());
                PoolDiagnostics.print(pool, "while borrowed");
            }
            PoolDiagnostics.print(
                    pool, "after Connection.close() returned it");

            String marker = Long.toString(System.currentTimeMillis());
            long fromId = repository.insert(
                    "Alice-" + marker, new BigDecimal("100.00"));
            long toId = repository.insert(
                    "Bob-" + marker, new BigDecimal("50.00"));

            List<Account> initial = repository.findAll();
            BigDecimal initialTotal = pairTotal(initial, fromId, toId);
            System.out.printf(
                    "Created accounts %d and %d; pair total=%s; marker=%s%n",
                    fromId, toId, initialTotal, marker);

            transfers.transfer(fromId, toId, new BigDecimal("25.00"));
            List<Account> afterSuccess = repository.findAll();
            System.out.printf(
                    "After success: from=%s to=%s total=%s%n",
                    balanceOf(afterSuccess, fromId),
                    balanceOf(afterSuccess, toId),
                    pairTotal(afterSuccess, fromId, toId));

            BigDecimal beforeFailedDebit = balanceOf(afterSuccess, fromId);
            BigDecimal beforeFailedTotal = pairTotal(
                    afterSuccess, fromId, toId);

            try {
                transfers.transfer(fromId, -1L, new BigDecimal("10.00"));
                throw new IllegalStateException(
                        "The rollback test unexpectedly succeeded");
            } catch (SQLException expected) {
                System.out.printf(
                        "Expected failure triggered rollback: "
                                + "SQLState=%s message=%s%n",
                        expected.getSQLState(), expected.getMessage());
            }

            List<Account> afterFailure = repository.findAll();
            BigDecimal afterFailedDebit = balanceOf(afterFailure, fromId);
            BigDecimal afterFailedTotal = pairTotal(
                    afterFailure, fromId, toId);
            boolean rollbackWorked =
                    beforeFailedDebit.compareTo(afterFailedDebit) == 0
                    && beforeFailedTotal.compareTo(afterFailedTotal) == 0;

            System.out.println(
                    "Rollback preserved balances: " + rollbackWorked);
            if (!rollbackWorked) {
                throw new IllegalStateException("Rollback verification failed");
            }

            PoolDiagnostics.print(pool, "before shutdown");
        }

        System.out.println("Pool closed: " + pool.isClosed());
    }

    private static BigDecimal balanceOf(List<Account> accounts, long id) {
        return accounts.stream()
                .filter(account -> account.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Account not returned: " + id))
                .balance();
    }

    private static BigDecimal pairTotal(
            List<Account> accounts,
            long firstId,
            long secondId
    ) {
        return balanceOf(accounts, firstId)
                .add(balanceOf(accounts, secondId));
    }

    private static void printSQLExceptionChain(SQLException exception) {
        for (SQLException current = exception;
             current != null;
             current = current.getNextException()) {
            System.err.printf(
                    "Database failure: SQLState=%s message=%s%n",
                    current.getSQLState(), current.getMessage());
        }
    }
}
```

</details>
