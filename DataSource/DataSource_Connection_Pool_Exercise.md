# DataSource and Connection Pool Mini Project

## `datasource-pool-practice` — Account Transfer Application

**Estimated time:** 3–5 focused hours<br>
**Stack:** Java 17+, Maven, JDBC, `javax.sql.DataSource`, HikariCP, pgJDBC, PostgreSQL 17, Docker Compose<br>
**Deliberately excluded:** Spring, Spring Boot, JPA, Hibernate, web/MVC, REST

This project makes database-resource ownership visible. Its business behavior is intentionally small: create two accounts, transfer money successfully, force a second transfer to fail, and prove that rollback restored the original balances.

Only PostgreSQL runs in Docker. The Java application runs on the Windows host with `mvn exec:java`, so the exercise stays focused on plain JDBC, one application-owned connection pool, and explicit transaction boundaries.

### Quick navigation

- [Target structure and priorities](#target-project-structure)
- [Exercise tasks](#exercise-tasks--attempt-before-reading-the-solution)
- [Manual verification scenarios](#manual-verification-scenarios)
- [Complete reference solution](#complete-reference-solution)
- [Architecture and troubleshooting](#architecture-review)
- [Checklist and reflection](#final-project-checklist)

```text
Main
  ↓ creates once
HikariDataSource
  ↓ implements
javax.sql.DataSource
  ↓ getConnection() supplies
logical Connection handle (Hikari proxy)
  ↓ borrows from / returns to
HikariCP physical connection pool
  ↓ opens database sessions through
pgJDBC
  ↓ connects through localhost:5432 to
PostgreSQL 17 in Docker
```

By the end, you should be able to point at every resource and answer:

- Who creates and closes the pool?
- Who borrows and returns each connection?
- Why do repositories depend on `DataSource` rather than `HikariDataSource`?
- Why must debit and credit use exactly one `Connection`?
- What survives `docker compose down`, and what is deleted by `docker compose down -v`?
- Why does host-run Java use `localhost`, while a Compose service would use `postgres`?

## Learning contract

Every task follows the same sequence:

1. Read **Objective** and **Concept**.
2. Implement **What to implement** using the incomplete **Starter code**.
3. Open **Hints** only when you need them.
4. Run **How to verify** immediately.
5. Review **Common mistakes** before moving on.
6. Read **Explanation** and connect the task to the larger ownership model.
7. Continue only when the task's observable behavior makes sense.

Starter code intentionally contains `TODO`s and does not reveal the essential implementation. The complete reference solution is near the end so you can practice before seeing the answers.

Some pool counts are timing-dependent. Verify relationships—such as active connections increasing while a handle is borrowed—rather than memorizing one exact metric line.

## Version note

The reference solution targets Java 17 and pins HikariCP `7.1.0`, pgJDBC `42.7.13`, Maven Compiler Plugin `3.15.0`, and Exec Maven Plugin `3.6.3`. Pinning versions makes the exercise reproducible. HikariCP is the concrete pooling implementation; application code still uses the standard JDBC and `javax.sql.DataSource` interfaces wherever Hikari-specific lifecycle or diagnostics are unnecessary.

PostgreSQL uses the `postgres:17` image tag. That pins the major database version while allowing current PostgreSQL 17 maintenance updates. Modern Docker Compose does not require a top-level `version:` field.

## Target project structure

```text
datasource-pool-practice/
├── docker-compose.yml
├── .env                 # local Compose values; do not commit
├── .env.example         # safe template; commit this
├── .gitignore
├── pom.xml
├── database/
│   └── schema.sql
└── src/
    └── main/
        └── java/
            └── com/example/poolpractice/
                ├── Main.java
                ├── config/
                │   ├── DatabaseSettings.java
                │   └── DataSourceFactory.java
                ├── model/
                │   └── Account.java
                ├── repository/
                │   └── AccountRepository.java
                ├── service/
                │   └── TransferService.java
                └── diagnostics/
                    └── PoolDiagnostics.java
```

Package rule:

```text
src/main/java/com/example/poolpractice/service/TransferService.java
                                      ↓
package com.example.poolpractice.service;
```

The package split is small but purposeful: configuration creates infrastructure, repositories execute persistence operations, the service owns the transfer boundary, diagnostics observe HikariCP, and `Main` owns application startup and shutdown.

## Project concept priorities

| Practice area | Priority |
|---|---|
| `DataSource`, pool ownership, connection borrow/return | ⭐⭐⭐⭐⭐ MUST KNOW |
| try-with-resources and connection-leak prevention | ⭐⭐⭐⭐⭐ MUST KNOW |
| exactly one `Connection` per transaction | ⭐⭐⭐⭐⭐ MUST KNOW |
| commit, rollback, and suppressed rollback failure | ⭐⭐⭐⭐⭐ MUST KNOW |
| repository depends on `DataSource`, not Hikari internals | ⭐⭐⭐⭐⭐ MUST KNOW |
| HikariCP beginner configuration | ⭐⭐⭐⭐ IMPORTANT |
| logical versus physical connections | ⭐⭐⭐⭐ IMPORTANT |
| Docker PostgreSQL, volumes, and schema initialization | ⭐⭐⭐⭐ IMPORTANT |
| host-versus-container networking and environment boundaries | ⭐⭐⭐⭐ IMPORTANT |
| generated keys and JDBC row mapping | ⭐⭐⭐⭐ IMPORTANT |
| pool diagnostics | ⭐⭐⭐ NICE TO KNOW |
| advanced pool sizing and production tuning | ⭐⭐ FUTURE KNOWLEDGE |

---

## Exercise Tasks — Attempt Before Reading the Solution

### Task 1 — Create the Maven project structure

Objective:

Create the project directory, professional package hierarchy, database directory, and safe configuration placeholders.

Concept:

Maven treats `src/main/java` as the Java source root. A Java package begins below that root and must match its directory path. Infrastructure files such as `docker-compose.yml` and `database/schema.sql` belong at the project level, not on the Java classpath.

What to implement:

Create:

- the `datasource-pool-practice` project directory;
- the `config`, `model`, `repository`, `service`, and `diagnostics` package directories;
- `database/schema.sql`;
- `docker-compose.yml`, `.env.example`, `.gitignore`, and `pom.xml`;
- a minimal `Main.java` in `com.example.poolpractice`;
- no Dockerfile and no Java container.

Starter code:

From the directory where you keep practice projects:

```powershell
New-Item -ItemType Directory -Force -Path `
  .\datasource-pool-practice\database, `
  .\datasource-pool-practice\src\main\java\com\example\poolpractice\config, `
  .\datasource-pool-practice\src\main\java\com\example\poolpractice\model, `
  .\datasource-pool-practice\src\main\java\com\example\poolpractice\repository, `
  .\datasource-pool-practice\src\main\java\com\example\poolpractice\service, `
  .\datasource-pool-practice\src\main\java\com\example\poolpractice\diagnostics

Set-Location .\datasource-pool-practice

# TODO: create the six project-level files and Main.java
```

```java
package com.example.poolpractice;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        // TODO: later tasks will assemble and run the application
    }
}
```

Hints:

1. `Main.java` sits directly under `com/example/poolpractice`.
2. `DatabaseSettings.java` and `DataSourceFactory.java` will use `package com.example.poolpractice.config;`.
3. Use nested directories, not one directory literally named `com.example.poolpractice`.
4. Empty placeholder files are acceptable until their task is reached.

How to verify:

```powershell
Get-ChildItem -Recurse
```

You should see every directory in the target structure. Check that there is no `src/main/resources` requirement and no Java Dockerfile.

Common mistakes:

- Creating `src/java/main` instead of `src/main/java`.
- Placing all classes in one package after choosing the professional structure.
- Putting `database/schema.sql` under `src/main/java`.
- Dockerizing the Java application even though the exercise runs Java on the host.

Explanation:

The directory tree establishes responsibility boundaries before code exists. Maven owns the Java build layout; Docker Compose will own only PostgreSQL infrastructure.

---

### Task 2 — Add Maven dependencies

Objective:

Configure a Java 17 build with only HikariCP and the PostgreSQL JDBC driver as application dependencies.

Concept:

HikariCP provides a pooling `DataSource`; pgJDBC provides the PostgreSQL JDBC implementation. Your source code mostly uses standard Java interfaces, so the PostgreSQL driver can remain a runtime dependency. Modern JDBC drivers self-register through the service-provider mechanism.

What to implement:

Create `pom.xml` with:

- coordinates `com.example:datasource-pool-practice:1.0.0`;
- Java release 17 and UTF-8 encoding;
- HikariCP `7.1.0`;
- pgJDBC `42.7.13` with runtime scope;
- Maven Compiler Plugin `3.15.0`;
- Exec Maven Plugin `3.6.3`;
- main class `com.example.poolpractice.Main`.

Do not add Spring, Spring Boot, JPA, Hibernate, a connection-pool starter, `Class.forName(...)`, or `setDriverClassName(...)`. Modern pgJDBC self-registers on the runtime classpath.

Starter code:

```xml
<properties>
    <maven.compiler.release>TODO</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
    <!-- TODO: com.zaxxer:HikariCP:7.1.0 -->
    <!-- TODO: org.postgresql:postgresql:42.7.13 with runtime scope -->
</dependencies>

<build>
    <plugins>
        <!-- TODO: compiler plugin configured for release 17 -->
        <!-- TODO: exec plugin configured with the fully qualified Main class -->
    </plugins>
</build>
```

Hints:

1. Each Maven dependency needs `groupId`, `artifactId`, and `version`.
2. Runtime scope keeps pgJDBC available when `Main` runs without requiring driver-specific source imports.
3. The exec plugin's `mainClass` is a dotted Java class name, not a filesystem path.
4. The compiler plugin belongs under `build/plugins`, not `dependencies`.

How to verify:

```powershell
mvn --version
mvn dependency:tree
mvn compile
```

Expected: Maven uses Java 17 or newer; the tree contains HikariCP and PostgreSQL; compilation reports `BUILD SUCCESS`; no Spring, JPA, or Hibernate dependency appears.

Common mistakes:

- Running Maven outside the directory containing `pom.xml`.
- Adding pgJDBC only to plugin configuration.
- Omitting versions because no dependency-management platform is present.
- Adding a logging provider merely because HikariCP reports that no SLF4J provider exists.

Explanation:

The POM supplies one pool implementation and one database driver. JDBC and `DataSource` remain the source-level contracts, which keeps most code independent of Hikari-specific types.

---

### Task 3 — Configure PostgreSQL with Docker Compose

Objective:

Declare one PostgreSQL service, persistent storage, loopback-only port publishing, schema mounting, and a health check.

Concept:

Compose describes infrastructure. A named volume persists PostgreSQL's data directory, a bind mount supplies initialization SQL, and `127.0.0.1:hostPort:5432` publishes the database only to the host loopback interface. Compose's project `.env` supplies interpolation values; it is not automatically imported into Java.

What to implement:

Create `docker-compose.yml` with:

- one service named `postgres`;
- image `postgres:17`;
- required `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` interpolation;
- host port `${POSTGRES_PORT:-5432}` bound to container port `5432` on `127.0.0.1`;
- named volume `postgres_data`;
- read-only mount of `database/schema.sql` into `/docker-entrypoint-initdb.d/01-schema.sql`;
- `pg_isready` health check;
- no Java service and no top-level `version:` key.

Populate `.env.example`, copy it to ignored local `.env`, and replace only the local password.

Starter code:

```yaml
services:
  postgres:
    image: TODO
    environment:
      POSTGRES_DB: "${POSTGRES_DB:?Set POSTGRES_DB in .env}"
      POSTGRES_USER: TODO
      POSTGRES_PASSWORD: TODO
    ports:
      - "TODO"
    volumes:
      - TODO
      - TODO
    healthcheck:
      test: ["CMD-SHELL", "TODO"]
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 10s

volumes:
  TODO:
```

```dotenv
POSTGRES_DB=pool_practice
POSTGRES_USER=pool_app
POSTGRES_PASSWORD=TODO-use-a-local-practice-password
POSTGRES_PORT=5432
```

```gitignore
.env
/target/
# TODO: common IDE metadata
```

Hints:

1. Escape Compose interpolation in the in-container health command as `$${POSTGRES_USER}` and `$${POSTGRES_DB}`.
2. The port mapping is `127.0.0.1:${POSTGRES_PORT:-5432}:5432`.
3. Mount the schema with `:ro`.
4. Declare `postgres_data:` once under top-level `volumes`.
5. Validate configuration now, but do not run `up -d` until Task 4 has completed `schema.sql`.

How to verify:

```powershell
Copy-Item .\.env.example .\.env
notepad .\.env
docker --version
docker compose version
docker compose config --quiet
docker compose config --services
```

Expected: configuration is valid and the only service printed is `postgres`. Do not start the fresh volume before the schema file is ready.

Common mistakes:

- Naming the service `database` but later running commands against `postgres`.
- Writing `$POSTGRES_USER` in the health check so Compose expands it too early.
- Mounting a nonexistent directory where a SQL file is expected.
- Committing the real `.env`.
- Adding a Java service to Compose.

Explanation:

Compose now knows how PostgreSQL will run, but the first start is deliberately postponed. The official image runs initialization scripts only when it creates a fresh data directory, so the schema must exist first.

---

### Task 4 — Initialize and verify the database schema

Objective:

Create the `accounts` table, start PostgreSQL with a fresh named volume, and verify the real container state.

Concept:

The official PostgreSQL image runs supported files in `/docker-entrypoint-initdb.d/` only while initializing an empty data directory. A restart reuses the named volume and skips those scripts. Container health proves PostgreSQL accepts checks; querying `accounts` separately proves schema initialization.

What to implement:

Create `database/schema.sql` with an `accounts` table containing:

- identity `BIGINT` primary key `id`;
- nonblank `owner_name` limited to 100 characters;
- `NUMERIC(12, 2)` nonnegative `balance`;
- `IF NOT EXISTS` so the statement itself is idempotent.

Then start Compose and verify both the database identity and table definition.

Starter code:

```sql
CREATE TABLE IF NOT EXISTS accounts (
    id TODO,
    owner_name TODO
        CHECK (TODO),
    balance TODO
        CHECK (TODO)
);
```

Hints:

1. Use `BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`.
2. Use `VARCHAR(100) NOT NULL` and `btrim(owner_name) <> ''`.
3. Use `NUMERIC(12, 2) NOT NULL` and `balance >= 0`.
4. The first `docker compose up -d` may need to download the image.

How to verify:

```powershell
docker compose up -d
docker compose ps
docker compose exec postgres psql -U pool_app -d pool_practice -c 'SELECT current_database(), current_user;'
docker compose exec postgres psql -U pool_app -d pool_practice -c '\d accounts'
```

Wait for `docker compose ps` to report `healthy`. Expected database/user: `pool_practice` / `pool_app`. Expected table columns: `id`, `owner_name`, and `balance`, with the primary-key and check constraints.

The PostgreSQL container already includes `psql`, so this verification does not require a host PostgreSQL installation.

If startup fails:

```powershell
docker compose logs --tail 100 postgres
```

Common mistakes:

- Starting the first volume while `schema.sql` is still empty.
- Assuming `healthy` proves that the table exists.
- Editing `schema.sql` later and expecting an existing volume to rerun it.
- Using `docker compose down -v` without understanding that it deletes all exercise rows.

Explanation:

PostgreSQL now has persistent container-managed storage. Plain `docker compose down` removes the container and network but keeps the named volume. For this disposable exercise only, `docker compose down -v` deletes that volume; the next `up -d` creates a fresh database and reruns the schema.

---

### Task 5 — Configure application environment variables

Objective:

Provide database settings to the host Java process and understand why Compose's `.env` is a separate configuration boundary.

Concept:

Docker Compose reads project `.env` values to interpolate `docker-compose.yml`, then passes explicitly declared `POSTGRES_*` values into the container. A Java process launched from PowerShell sees only its own process environment through `System.getenv()`. Compose does not export its `.env` values into the parent shell.

Because Java runs on the Windows host, it connects through the published port with `localhost`. If Java were another Compose service, it would normally use Compose DNS and the service hostname `postgres`; inside a container, `localhost` means that same container.

What to implement:

In the same PowerShell window that will run Maven, set:

- `DB_URL=jdbc:postgresql://localhost:5432/pool_practice`;
- `DB_USERNAME=pool_app`;
- `DB_PASSWORD` equal to the local PostgreSQL password;
- a safe presence check that never prints the password.

If `.env` uses a different `POSTGRES_PORT`, use that host port in `DB_URL`.

Starter code:

```powershell
$env:DB_URL = 'TODO'
$env:DB_USERNAME = 'TODO'
$env:DB_PASSWORD = 'TODO'

@('DB_URL', 'DB_USERNAME', 'DB_PASSWORD') | ForEach-Object {
    [pscustomobject]@{
        Name = $_
        IsSet = TODO
    }
}
```

Hints:

1. The default URL is `jdbc:postgresql://localhost:5432/pool_practice`.
2. Use `[Environment]::GetEnvironmentVariable($_)` inside the presence check.
3. Test with `IsNullOrWhiteSpace`; do not echo the value.
4. Environment assignments affect the current PowerShell process and child processes such as Maven.

How to verify:

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

All three `IsSet` values should be `True`. Then confirm the published port without revealing credentials:

```powershell
docker compose port postgres 5432
```

Common mistakes:

- Expecting Java to load `.env` automatically.
- Using `jdbc:postgresql://postgres:5432/...` from host-run Java.
- Setting variables in one PowerShell window and running Maven in another.
- Printing `DB_PASSWORD` while debugging.
- Changing `.env` credentials after database initialization and expecting the existing database role password to change.

Explanation:

The container and host JVM are different processes with different environments. Matching values connect them, but no configuration file crosses that boundary automatically.

---

### Task 6 — Create the `Account` model

Objective:

Represent one database account as a small immutable Java value.

Concept:

A record is appropriate for a value read from a row: its components are explicit, accessors are generated, and the object does not own database resources. PostgreSQL `NUMERIC(12, 2)` maps naturally to `BigDecimal`; using `double` would introduce binary floating-point behavior into money calculations.

What to implement:

Create `model/Account.java` as a record with:

- `long id`;
- `String ownerName`;
- `BigDecimal balance`;
- package `com.example.poolpractice.model`;
- no JDBC or HikariCP imports.

Starter code:

```java
package com.example.poolpractice.model;

// TODO: import the exact decimal type used for SQL NUMERIC

public record Account(
        // TODO: id
        // TODO: owner name
        // TODO: balance
) {
}
```

Hints:

1. Import `java.math.BigDecimal`.
2. Separate record components with commas.
3. Record accessors will be named `id()`, `ownerName()`, and `balance()`.

How to verify:

```powershell
mvn compile
Get-ChildItem .\target\classes\com\example\poolpractice\model
```

Expected: `BUILD SUCCESS` and `Account.class` in the package-matching output directory.

Common mistakes:

- Using a package that does not match the `model` directory.
- Choosing `double` for the balance.
- Giving the model a `Connection` or `DataSource` field.
- Adding setters to an immutable record.

Explanation:

`Account` carries data; it does not load or save itself. That separation keeps resource ownership in the repository and transaction ownership in the service.

---

### Task 7 — Load database settings safely

Objective:

Read required host-process environment variables, fail clearly when one is absent, and prevent accidental password disclosure.

Concept:

Configuration should enter the application at one boundary. Validating it before pool creation produces a focused startup error instead of a vague authentication or null-value failure later. Redacting `toString()` protects the password when the settings object is inspected.

What to implement:

Create `config/DatabaseSettings.java` as a record containing URL, username, and password. Implement:

- `fromEnvironment()` using `System.getenv(...)`;
- a `required(String name)` helper;
- rejection of null or blank values with `IllegalStateException`;
- an error that names the missing variable but never its value;
- a `toString()` that prints URL and username but shows `<redacted>` for the password.

Temporarily update `Main` to load and print the settings object.

Starter code:

```java
package com.example.poolpractice.config;

public record DatabaseSettings(
        String url,
        String username,
        String password
) {
    public static DatabaseSettings fromEnvironment() {
        // TODO: load all three required DB_* variables
        throw new UnsupportedOperationException("TODO: load settings");
    }

    private static String required(String name) {
        // TODO: read, validate, and return one environment value
        throw new UnsupportedOperationException("TODO: read " + name);
    }

    @Override
    public String toString() {
        // TODO: include URL/username and redact the password
        throw new UnsupportedOperationException("TODO: safe toString");
    }
}
```

```java
public static void main(String[] args) {
    // TODO: call DatabaseSettings.fromEnvironment()
    // TODO: print the record safely
}
```

Hints:

1. The names are exactly `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
2. A value is invalid when `value == null || value.isBlank()`.
3. Build the record by calling `required(...)` three times.
4. The literal text `<redacted>` belongs in `toString()`; the password value does not.

How to verify:

With all variables set:

```powershell
mvn compile exec:java
```

Expected: URL and username appear, but the actual password does not.

Then deliberately test fail-fast behavior and restore the variable:

```powershell
$savedDbUrl = $env:DB_URL
Remove-Item Env:DB_URL
mvn exec:java
$env:DB_URL = $savedDbUrl
```

Expected failure: `DB_URL is required and must not be blank`, with no password in the output.

Common mistakes:

- Reading Compose's `POSTGRES_*` variables instead of the Java-facing `DB_*` names.
- Returning an empty string and letting HikariCP fail later.
- Including the password in an exception, log line, record-generated `toString()`, or debugger probe.
- Forgetting to restore the test variable before the next task.

Explanation:

The application now has one validated configuration value object. It knows nothing about Compose; it sees only the environment inherited by the host JVM.

---

### Task 8 — Create one `HikariDataSource`

Objective:

Translate validated database settings into one small, observable HikariCP connection pool.

Concept:

`DataSource` is the standard connection-factory abstraction; it does not guarantee pooling. `HikariDataSource` is HikariCP's concrete pooling implementation. HikariCP is the pool engine that manages physical JDBC connections and lends logical proxy handles to callers.

For this exercise:

| Setting | Value | Meaning |
|---|---:|---|
| `maximumPoolSize` | `3` | Maximum physical connections managed by the pool |
| `minimumIdle` | `1` | Target minimum idle connections |
| `connectionTimeout` | `5_000 ms` | Maximum wait to borrow a connection |
| `idleTimeout` | `60_000 ms` | When surplus idle connections become eligible for retirement |
| `maxLifetime` | `600_000 ms` | Upper lifetime target for physical connections |

These are transparent learning values, not universal production tuning. `connectionTimeout` is acquisition wait time, not SQL execution timeout. `maxLifetime` does not terminate a connection in the middle of borrower work; an eligible connection is retired after it returns to the pool.

What to implement:

Create `config/DataSourceFactory.java` that:

- creates a `HikariConfig`;
- sets pool name `account-practice-pool`;
- sets JDBC URL, username, and password from `DatabaseSettings`;
- applies all five pool values above;
- returns one `new HikariDataSource(config)`;
- has no mutable static pool field.

Update the temporary `Main` probe so any pool it creates is closed.

Starter code:

```java
package com.example.poolpractice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceFactory {
    private DataSourceFactory() {
    }

    public static HikariDataSource create(DatabaseSettings settings) {
        HikariConfig config = new HikariConfig();

        // TODO: pool name
        // TODO: JDBC URL and credentials
        // TODO: maximumPoolSize and minimumIdle
        // TODO: connectionTimeout, idleTimeout, and maxLifetime

        throw new UnsupportedOperationException("TODO: create pool");
    }
}
```

```java
DatabaseSettings settings = DatabaseSettings.fromEnvironment();
// TODO: create one HikariDataSource in a temporary try-with-resources probe
// TODO: print only its pool name and closed state
```

Hints:

1. Hikari's setters use milliseconds for these timeout values.
2. Call `config.setJdbcUrl(settings.url())` and the corresponding credential setters.
3. The final line returns `new HikariDataSource(config)`.
4. Do not call `DataSourceFactory.create(...)` once per repository operation.

How to verify:

Ensure PostgreSQL is healthy and the `DB_*` variables are set, then run:

```powershell
mvn clean compile
mvn exec:java
```

Expected: pool creation succeeds, the pool name is `account-practice-pool`, and the temporary probe closes it. A one-time “no SLF4J providers” warning is harmless in this minimal project.

Common mistakes:

- Returning an unconfigured `HikariDataSource`.
- Treating `maximumPoolSize=3` as a command to open three connections immediately.
- Mistaking `connectionTimeout` for a query timeout.
- Printing the settings object or JDBC configuration carelessly around credentials.
- Creating a pool without closing the temporary verification probe.

Explanation:

The application now has a concrete, closeable pool at its composition boundary. Later, `Main` will retain that concrete type for shutdown while passing the same object downstream as `DataSource`.

---

### Task 9 — Implement `AccountRepository.insert`

Objective:

Insert an account through a borrowed connection and return its database-generated identity.

Concept:

A repository should receive `DataSource` as a constructor dependency, borrow a connection for one independent operation, and return it promptly. With HikariCP, closing the logical proxy normally returns the underlying physical connection to the pool. Statements and generated-key result sets still have their own lifetimes and must also close.

What to implement:

Create `repository/AccountRepository.java` with:

- a final `DataSource` constructor dependency;
- an `INSERT` prepared statement for `owner_name` and `balance`;
- `Statement.RETURN_GENERATED_KEYS`;
- bound string and `BigDecimal` parameters;
- an exact inserted-row count of `1`;
- a checked `keys.next()` before reading column `1`;
- nested try-with-resources for connection, statement, and keys;
- clear `SQLException`s when the row count or generated key is wrong.

Do not store a `Connection` field and do not construct a pool here.

Starter code:

```java
package com.example.poolpractice.repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public final class AccountRepository {
    private static final String INSERT_SQL = """
            INSERT INTO accounts (owner_name, balance)
            VALUES (?, ?)
            """;

    private final DataSource dataSource;

    public AccountRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    public long insert(String ownerName, BigDecimal openingBalance)
            throws SQLException {
        // TODO: borrow a connection
        // TODO: prepare for generated keys and bind both values
        // TODO: require one updated row
        // TODO: read and return the generated key
        throw new UnsupportedOperationException("TODO: insert account");
    }
}
```

Temporary probe in `Main`:

```java
try (HikariDataSource pool = DataSourceFactory.create(settings)) {
    DataSource dataSource = pool;
    // TODO: create AccountRepository
    // TODO: insert a uniquely named account with balance 1.00
    // TODO: print and validate the positive generated ID
}
```

Hints:

1. Call `connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)`.
2. Bind indexes `1` and `2` in SQL placeholder order.
3. Open `statement.getGeneratedKeys()` in a nested try-with-resources block after `executeUpdate()`.
4. If `keys.next()` is false, throw rather than returning a fake ID.

How to verify:

```powershell
mvn clean compile
mvn exec:java
docker compose exec postgres psql -U pool_app -d pool_practice -c 'SELECT id, owner_name, balance FROM accounts ORDER BY id DESC LIMIT 1;'
```

Expected: the application prints a positive generated ID, and `psql` shows the probe row with balance `1.00`.

Common mistakes:

- Omitting `Statement.RETURN_GENERATED_KEYS`.
- Calling `getGeneratedKeys()` without `next()`.
- Ignoring an unexpected update count.
- Closing only the connection while leaving the statement/result set unmanaged.
- Calling `pool.close()` from the repository.

Explanation:

One repository call now owns one short logical connection handle and all resources created from it. The pool remains alive for later operations because only the borrowed handle closes.

---

### Task 10 — Implement `AccountRepository.findAll`

Objective:

Read every account in deterministic order and map each row to the model record.

Concept:

JDBC exposes query results through a cursor-like `ResultSet`. The repository owns the connection, statement, and result set for the duration of mapping. Returning model values—not live JDBC objects—lets all database resources close before the method returns.

What to implement:

Extend `AccountRepository` with:

- a query selecting `id`, `owner_name`, and `balance`;
- `ORDER BY id`;
- an `ArrayList<Account>`;
- `while (rows.next())` mapping with `getLong`, `getString`, and `getBigDecimal`;
- an empty list, never `null`, when no rows exist;
- try-with-resources for connection, prepared statement, and result set.

Starter code:

```java
private static final String FIND_ALL_SQL = """
        SELECT id, owner_name, balance
        FROM accounts
        ORDER BY id
        """;

public List<Account> findAll() throws SQLException {
    List<Account> accounts = new ArrayList<>();

    // TODO: borrow a connection
    // TODO: prepare and execute FIND_ALL_SQL
    // TODO: map every row to Account

    return accounts;
}
```

Temporary probe:

```java
// TODO: call repository.findAll()
// TODO: print the returned records
// TODO: fail if IDs are not in ascending order
```

Hints:

1. Import `com.example.poolpractice.model.Account`, `ArrayList`, and `List`.
2. Use column labels exactly as written in SQL.
3. Put all three JDBC resources in one try-with-resources declaration.
4. The initialized empty `ArrayList` already represents “no rows.”

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected: all existing accounts print in ascending ID order. On a fresh database before any insert, the method should return `[]`, not `null`.

Common mistakes:

- Using `if (rows.next())` and mapping only the first row.
- Returning the `ResultSet` from the repository.
- Reading `balance` with `getDouble`.
- Forgetting the model import after splitting packages.
- Sorting only in Java and leaving database result order undefined.

Explanation:

The repository now converts database state into ordinary immutable values. Its callers never need to know which JDBC resources were used or how the pool supplied the connection.

---

### Task 11 — Observe connection-pool behavior

Objective:

Observe one pool's total, active, idle, and waiting counts while a logical connection is borrowed and after it is returned.

Concept:

`HikariDataSource.getConnection()` returns a logical proxy handle backed by a physical JDBC connection managed by HikariCP. Calling `Connection.close()` normally returns a healthy physical connection for reuse; it does not shut down the pool. Broken, expired, or evicted physical connections may instead be closed and replaced.

Pool metrics are snapshots. Exact totals and idle counts can vary as startup and housekeeping run. An identity comparison such as `connection1 == connection2` is not proof of physical reuse because HikariCP may create different proxy handles.

What to implement:

Create `diagnostics/PoolDiagnostics.java` that:

- accepts `HikariDataSource` and a label;
- obtains `HikariPoolMXBean`;
- safely handles a not-yet-started `null` metrics object;
- prints label, pool name, and `System.identityHashCode(pool)`;
- prints total, active, idle, and threads-waiting counts;
- never prints URL, username, or password.

In `Main`, print a snapshot, borrow one connection in try-with-resources, print the proxy class and another snapshot, then print a third snapshot after close.

Starter code:

```java
package com.example.poolpractice.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public final class PoolDiagnostics {
    private PoolDiagnostics() {
    }

    public static void print(HikariDataSource pool, String label) {
        HikariPoolMXBean metrics = pool.getHikariPoolMXBean();

        // TODO: handle metrics == null
        // TODO: print label, pool name, and pool object identity
        // TODO: print total, active, idle, and waiting counts
    }
}
```

```java
PoolDiagnostics.print(pool, "startup");
try (Connection connection = sharedDataSource.getConnection()) {
    // TODO: print the logical connection runtime class
    // TODO: print "while borrowed" metrics
}
// TODO: print metrics after Connection.close()
```

Hints:

1. The methods are `getTotalConnections()`, `getActiveConnections()`, `getIdleConnections()`, and `getThreadsAwaitingConnection()`.
2. `%x` produces a compact identity-hash representation.
3. Keep the “while borrowed” snapshot inside the connection's try block.
4. Look for active count movement, not a fixed total count.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected meaningful behavior:

```text
[startup] pool=account-practice-pool instance=... total=... active=0 idle=... waiting=0
Borrowed logical connection: com.zaxxer.hikari.pool.HikariProxyConnection
[while borrowed] ... active=1 ...
[after Connection.close() returned it] ... active=0 idle=...
```

The same pool identity should appear in every snapshot. Exact total/idle values may differ.

Common mistakes:

- Printing credentials in diagnostics.
- Taking the “after” snapshot before leaving the try-with-resources block.
- Assuming a new proxy object means a new physical database session.
- Treating metrics as synchronized assertions that never change.
- Closing the entire pool just to return one connection.

Explanation:

You have now observed the central pool lifecycle: borrow a logical handle, use it briefly, and close the handle so the application-owned pool can reuse or retire the physical connection.

---

### Task 12 — Implement the transfer transaction

Objective:

Debit and credit atomically on exactly one borrowed connection, committing both or rolling back both.

Concept:

A JDBC transaction belongs to a `Connection`. Disabling auto-commit on one connection does not affect work performed on another. Therefore debit and credit helpers must accept the exact connection borrowed by `transfer(...)`; neither helper may call `dataSource.getConnection()`.

Rollback must happen while the connection is still in scope. If rollback itself fails, preserve the original failure and attach the rollback error with `addSuppressed(...)`.

What to implement:

Create `service/TransferService.java` with:

- a final `DataSource` constructor dependency;
- validation for different account IDs;
- validation for a non-null, positive amount with at most two decimal places;
- exactly one `dataSource.getConnection()` call in `transfer(...)`;
- `setAutoCommit(false)` before either update;
- debit SQL that also requires `balance >= amount`;
- credit SQL targeting the destination ID;
- update-count checks requiring exactly one row for each operation;
- commit only after both operations succeed;
- catch of `SQLException | RuntimeException`;
- rollback, suppressed rollback failure, and rethrow of the original exception;
- try-with-resources for the connection and both statements.

Starter code:

```java
package com.example.poolpractice.service;

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
        // TODO: validate before borrowing
        // TODO: borrow exactly one Connection
        // TODO: disable auto-commit, debit, credit, and commit
        // TODO: rollback and preserve both failures when necessary
        throw new UnsupportedOperationException("TODO: transfer");
    }

    private int debit(Connection connection, long accountId, BigDecimal amount)
            throws SQLException {
        // TODO: prepare, bind, and execute DEBIT_SQL on the supplied connection
        throw new UnsupportedOperationException("TODO: debit");
    }

    private int credit(Connection connection, long accountId, BigDecimal amount)
            throws SQLException {
        // TODO: prepare, bind, and execute CREDIT_SQL on the supplied connection
        throw new UnsupportedOperationException("TODO: credit");
    }

    private static void validateTransfer(
            long fromId,
            long toId,
            BigDecimal amount
    ) {
        // TODO: validate IDs, sign, null, and scale
    }
}
```

Hints:

1. The outer resource is `try (Connection connection = dataSource.getConnection())`.
2. Put an inner `try/catch` after `connection.setAutoCommit(false)` so rollback can still use the open connection.
3. Bind debit parameters as amount, account ID, amount; bind credit as amount, account ID.
4. Treat update count `0` as a failure: missing/underfunded source or missing destination.
5. In the rollback catch, call `original.addSuppressed(rollbackFailure)` and then `throw original`.

How to verify:

First verify the structure:

```powershell
mvn clean compile
rg -n "getConnection\(" .\src\main\java\com\example\poolpractice\service\TransferService.java
```

Expected: compilation succeeds and `TransferService.java` contains exactly one acquisition call, inside `transfer(...)`. The helper methods accept `Connection`.

Then temporarily call an invalid transfer such as equal source/destination IDs with a positive amount. Expected: `IllegalArgumentException` before any SQL runs. Tasks 13 and 14 verify commit and rollback against real rows.

Common mistakes:

- Borrowing one connection in `debit` and another in `credit`.
- Disabling auto-commit after the debit.
- Ignoring update counts.
- Catching only `SQLException` and letting an in-transaction runtime failure skip rollback.
- Throwing the rollback exception instead of retaining the original failure.
- Closing the pool from the service.

Explanation:

The service now owns one business transaction while the repository owns independent one-operation connections. Both use the same `DataSource`, but their connection scope correctly follows their unit of work.

---

### Task 13 — Verify a successful commit

Objective:

Transfer `25.00` between real accounts and prove that both balances changed while their pair total stayed constant.

Concept:

A successful commit makes all statements in the transaction durable together. Verification must read current database state rather than trusting that `commit()` returned. Because the database volume persists across runs, calculate only the two accounts created for this run, not the whole table.

`BigDecimal.equals(...)` also compares scale; `75.0` and `75.00` are numerically equal but not equal by that method. Use `compareTo(...) == 0` for these balance assertions.

What to implement:

In `Main`:

1. create a unique marker with `System.currentTimeMillis()`;
2. insert a source account with `100.00`;
3. insert a destination account with `50.00`;
4. validate that both generated IDs are positive and distinct;
5. compute the initial total for only those IDs and require `150.00`;
6. transfer `25.00`;
7. reread through `findAll()`;
8. require source `75.00`, destination `75.00`, and total `150.00`;
9. print the IDs, marker, balances, and pair total.

Starter code:

```java
String marker = Long.toString(System.currentTimeMillis());

long fromId = repository.insert(
        "Alice-" + marker, new BigDecimal("TODO"));
long toId = repository.insert(
        "Bob-" + marker, new BigDecimal("TODO"));

// TODO: validate IDs and initial pair total
// TODO: transfer 25.00
// TODO: reread both balances from repository.findAll()
// TODO: compare 75.00, 75.00, and 150.00 numerically
// TODO: fail clearly if any invariant is false
```

```java
private static BigDecimal balanceOf(List<Account> accounts, long id) {
    // TODO: locate the requested ID or fail, then return its balance
    throw new UnsupportedOperationException("TODO: balance lookup");
}

private static BigDecimal pairTotal(
        List<Account> accounts,
        long firstId,
        long secondId
) {
    // TODO: add the two current balances
    throw new UnsupportedOperationException("TODO: pair total");
}
```

Hints:

1. Construct monetary literals from strings, for example `new BigDecimal("100.00")`.
2. Use `balanceOf(...)` twice to isolate the current run from older rows.
3. Check each expected value with `actual.compareTo(expected) == 0`.
4. Print the generated IDs so container-side SQL can verify the same pair.

How to verify:

```powershell
mvn clean compile
mvn exec:java
docker compose exec postgres psql -U pool_app -d pool_practice
```

Inside `psql`, replace the placeholders with the printed IDs:

```sql
SELECT id, owner_name, balance
FROM accounts
WHERE id IN (<source-id>, <destination-id>)
ORDER BY id;

SELECT SUM(balance) AS pair_total
FROM accounts
WHERE id IN (<source-id>, <destination-id>);

SELECT id, owner_name, balance
FROM accounts
WHERE owner_name LIKE '%<timestamp-marker>%'
ORDER BY id;
```

Expected: source `75.00`, destination `75.00`, sum `150.00`, and exactly two rows for this run's unique marker. Exit with `\q`.

Common mistakes:

- Comparing `BigDecimal` values with `equals(...)` or `==`.
- Summing the entire persistent table.
- Printing success before rereading the database.
- Reversing the source and destination IDs.
- Committing after debit but before credit.

Explanation:

The first real transaction has crossed the complete path: one logical handle, two SQL updates, one commit, and a fresh read proving that the pair's money was conserved.

---

### Task 14 — Force and verify rollback

Objective:

Make debit succeed and credit fail for destination ID `-1`, then prove rollback restored the pre-failure balances.

Concept:

A useful rollback experiment must create work that actually needs undoing. Here the real, funded source is debited first. The missing destination update affects zero rows, the service turns that count into `SQLException`, and rollback must reverse the earlier debit.

PostgreSQL does not consider “UPDATE matched zero rows” a server error. Therefore the application-created exception may report `SQLState=null`; the update-count check is what gives zero rows business meaning.

What to implement:

After the successful transfer:

1. save the source balance and pair total read from the database;
2. call `transfer(fromId, -1L, new BigDecimal("10.00"))`;
3. fail if the call unexpectedly returns;
4. catch the expected `SQLException` and print its SQL state/message;
5. reread both real accounts;
6. compare the post-failure source balance and pair total with the saved values;
7. print and require `Rollback preserved balances: true`.

Starter code:

```java
BigDecimal beforeFailedDebit = sourceAfterSuccess;
BigDecimal beforeFailedTotal = totalAfterSuccess;

try {
    // TODO: transfer 10.00 from the real source to destination -1
    // TODO: fail if no SQLException occurs
} catch (SQLException expected) {
    // TODO: print SQLState and message without treating this as app failure
}

// TODO: reread current accounts
// TODO: compare source and pair total with the saved values
// TODO: print and require rollbackWorked == true
```

Hints:

1. Keep `fromId` valid and funded so the debit update count is `1`.
2. Use `-1L` only for `toId`; the credit then returns update count `0`.
3. Compare database-derived values from immediately before and after the failed call.
4. The destination's real account balance should also remain `75.00`.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected meaningful output:

```text
Expected failure triggered rollback: SQLState=null message=Destination account not found: -1
Rollback preserved balances: true
```

Run the two `psql` queries from Task 13 again for the same IDs. Expected remains `75.00`, `75.00`, total `150.00`.

Common mistakes:

- Using a missing or underfunded source so debit never happens.
- Catching the exception outside the pool lifecycle and skipping verification.
- Treating zero updated destination rows as success.
- Verifying old in-memory `Account` objects instead of rereading.
- Rolling back a different connection from the one that performed debit.

Explanation:

The failure path now proves atomicity, not merely exception handling. The first update ran, the second could not satisfy the use case, and one connection's rollback restored the database.

---

### Task 15 — Verify `DataSource` and pool ownership

Objective:

Prove that `Main` creates one pool and passes the same object as `DataSource` to both repository and service.

Concept:

Pool creation is application composition, not repository behavior. `Main` retains the concrete `HikariDataSource` because it owns Hikari-specific diagnostics and shutdown. Consumers accept the narrower `DataSource` interface because they only need `getConnection()`.

One application pool does not mean one connection forever. It means one manager that can maintain up to the configured maximum of physical connections and issue many short-lived logical handles.

What to implement:

Assemble dependencies in `Main` exactly once:

- call `DataSourceFactory.create(settings)` once;
- assign that object to one `DataSource sharedDataSource` variable;
- pass the same variable to `AccountRepository`;
- pass the same variable to `TransferService`;
- print whether `sharedDataSource == pool`;
- confirm repository/service import `javax.sql.DataSource`, not Hikari types;
- confirm neither class creates or closes a pool or stores a `Connection`.

Starter code:

```java
DatabaseSettings settings = DatabaseSettings.fromEnvironment();

// TODO: call the factory once and retain HikariDataSource as the owner type
// TODO: expose the same object through DataSource sharedDataSource
// TODO: construct AccountRepository with sharedDataSource
// TODO: construct TransferService with sharedDataSource
// TODO: print whether sharedDataSource == pool
```

Hints:

1. One line should read `DataSource sharedDataSource = pool;`.
2. Both constructor calls should receive `sharedDataSource`.
3. Only the factory contains `new HikariDataSource(config)`.
4. Diagnostics may accept Hikari's concrete type; repository and service should not.

How to verify:

```powershell
mvn clean compile
rg -n "DataSourceFactory\.create|new HikariDataSource|HikariDataSource|DataSource" .\src\main\java
rg -n "Connection\s+\w+\s*;" .\src\main\java\com\example\poolpractice\repository .\src\main\java\com\example\poolpractice\service
mvn exec:java
```

Expected:

- one factory invocation in `Main`;
- one `new HikariDataSource(...)` in the factory;
- no Hikari type in repository/service;
- no long-lived connection field;
- `Same object exposed as DataSource: true`;
- the same pool identity in every diagnostics line.

Common mistakes:

- Creating a new pool in each constructor or method.
- Typing repository/service fields as `HikariDataSource`.
- adding getters purely to expose Hikari internals.
- Treating the diagnostics identity alone as proof of invisible constructor wiring; the composition code is the proof.
- Closing the shared pool from a consumer.

Explanation:

The dependency graph is explicit plain Java: `Main` constructs infrastructure, exposes the narrow interface to consumers, and remains the sole lifecycle owner.

---

### Task 16 — Shut down the Hikari pool

Objective:

Close the one application-owned pool on normal completion and on failure, then verify its closed state.

Concept:

Two `close()` calls have different scopes:

```text
Connection.close()
    → return one logical handle to the open pool

HikariDataSource.close()
    → shut down the pool and close its managed physical sessions
```

`DataSource` does not declare `close()`. That is why downstream consumers receive `DataSource`, while `Main` retains `HikariDataSource` and owns its lifetime.

What to implement:

Use the Java 9+ existing-variable form of try-with-resources:

1. create the pool before the resource statement;
2. enter `try (pool)`;
3. create and use consumers inside;
4. print final pool metrics before leaving;
5. after successful exit, print `pool.isClosed()`;
6. do not close the pool in repository/service.

The resource statement must close the pool even if application work throws.

Starter code:

```java
HikariDataSource pool = DataSourceFactory.create(settings);

try (pool) {
    // TODO: assign the shared DataSource
    // TODO: construct consumers and perform application work
    // TODO: print "before shutdown" diagnostics
}

// TODO: print pool.isClosed()
```

Hints:

1. Declaring `pool` before `try (pool)` keeps it in scope for the post-close check.
2. Do not use `try (HikariDataSource pool = ...)` if you need that variable afterward.
3. No explicit `finally` is needed when the resource statement owns shutdown.
4. A connection try-with-resources belongs inside the longer pool try-with-resources.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected final application line:

```text
Pool closed: true
```

Temporarily throw a `RuntimeException` inside `try (pool)` and debug or add an outer `finally` probe if you want to observe failure-path closure; restore the final integration afterward.

Common mistakes:

- Declaring the pool only inside a resource initializer and then trying to access it out of scope.
- Closing the pool after every query.
- Assuming returning the last connection automatically shuts down the pool.
- Calling `close()` through a `DataSource` reference.
- Using `System.exit(...)` before orderly resource cleanup.

Explanation:

The longest-lived resource now has one owner and a deterministic boundary. Short operations return connection handles; application exit closes the manager that owns the underlying physical sessions.

---

### Task 17 — Run the final integration

Objective:

Run every infrastructure, pooling, repository, transaction, diagnostics, and lifecycle behavior as one coherent console application.

Concept:

Integration proves that separately correct pieces agree at their boundaries: Compose credentials match host settings, pgJDBC reaches PostgreSQL, HikariCP lends and reclaims connections, repository mappings match the schema, one connection spans each transfer, and `Main` closes the pool.

What to implement:

Complete `Main` so it:

1. catches top-level `SQLException`, prints its chained SQL state/messages, and wraps it;
2. loads validated settings;
3. creates exactly one pool;
4. wires one shared `DataSource`;
5. observes borrow/return metrics;
6. inserts two unique accounts;
7. verifies initial total;
8. verifies the committed `25.00` transfer;
9. verifies rollback for destination `-1`;
10. prints metrics before shutdown;
11. prints `Pool closed: true`.

Keep helper methods for balance lookup, pair totals, and `SQLException.getNextException()` traversal.

Starter code:

```java
public static void main(String[] args) {
    try {
        // TODO: run the complete application
    } catch (SQLException exception) {
        // TODO: print the SQLException chain
        throw new RuntimeException("Database exercise failed", exception);
    }
}

private static void run() throws SQLException {
    // TODO: settings and exactly one pool
    // TODO: shared DataSource, repository, and transfer service
    // TODO: logical connection borrow/return observation
    // TODO: unique inserts and initial-total check
    // TODO: successful commit verification
    // TODO: forced rollback verification
    // TODO: orderly pool shutdown and closed-state proof
}

private static void printSQLExceptionChain(SQLException exception) {
    // TODO: follow getNextException() without printing credentials
}
```

Hints:

1. Keep `run()` focused on orchestration; reuse `balanceOf(...)` and `pairTotal(...)`.
2. Catch the expected missing-destination exception inside `run()` so it does not become a top-level application failure.
3. Print metrics before shutdown while the pool is still open.
4. Surrounding Maven logs and exact metric counts can vary; the invariants must not.
5. An ordinary Maven JAR is not automatically a dependency-containing executable JAR; use `mvn exec:java`.

How to verify:

```powershell
docker compose config --quiet
docker compose up -d
docker compose ps
mvn clean compile
mvn dependency:tree
mvn exec:java
mvn clean package
Get-ChildItem .\target
```

Expected meaningful application output:

```text
Same object exposed as DataSource: true
[startup] pool=account-practice-pool instance=... total=... active=0 idle=... waiting=0
Borrowed logical connection: com.zaxxer.hikari.pool.HikariProxyConnection
[while borrowed] instance=<same value> ... active=1 ...
[after Connection.close() returned it] instance=<same value> ... active=0 idle=...
Created accounts <source-id> and <destination-id>; pair total=150.00; marker=<marker>
After success: from=75.00 to=75.00 total=150.00
Expected failure triggered rollback: SQLState=null message=Destination account not found: -1
Rollback preserved balances: true
[before shutdown] instance=<same value> ... active=0 ... waiting=0
Pool closed: true
```

Expected artifact: `target\datasource-pool-practice-1.0.0.jar`. It is an ordinary Maven JAR; the Exec plugin supplies the dependency classpath for this exercise.

Common mistakes:

- Leaving temporary probe code in place so multiple pools are created.
- Verifying table-wide balances left by earlier runs.
- Treating the expected rollback exception as a failed integration.
- Requiring an exact idle/total count from asynchronous pool behavior.
- Expecting `java -jar` to work like a Spring Boot fat JAR.

Explanation:

The complete application demonstrates explicit resource composition without a framework: one host process, one pool owner, many bounded JDBC resources, one connection per transaction, and a Docker-managed PostgreSQL server.

---

## Stop Here and Build Your Version

Do not continue until you have attempted Tasks 1–17. Your formatting may differ, but the ownership graph, SQL behavior, transaction boundaries, and observable invariants should match.

---

## Manual Verification Scenarios

Run these against your attempted implementation before comparing it with the reference solution. “Expected result” means meaningful behavior, not an exact copy of surrounding Maven or Docker output.

### Scenario 1 — PostgreSQL and schema are ready

Input/action:

Run `docker compose config --quiet`, `docker compose up -d`, `docker compose ps`, and container `psql` with `\d accounts`.

Expected result:

The only service becomes healthy and the real database contains `accounts` with identity, nonblank-name, and nonnegative-balance constraints.

What concept it proves:

Compose interpolation, port publishing, the named volume, health check, and fresh-volume schema initialization agree.

PASS condition:

Both health and table inspection succeed.

FAIL symptoms:

An unhealthy service, missing relation, wrong database/user, or a schema script skipped because the volume was initialized earlier.

### Scenario 2 — Host configuration is validated and redacted

Input/action:

Run once with all three `DB_*` variables, then once with one variable deliberately removed.

Expected result:

Valid settings start the application without printing the password; the missing variable produces a focused `IllegalStateException` naming only that variable.

What concept it proves:

The host JVM environment is separate from Compose `.env`, and configuration fails fast.

PASS condition:

No password value appears in either path.

FAIL symptoms:

Java silently reads no value, an authentication failure appears much later, or record output exposes the secret.

### Scenario 3 — Insert returns a real generated key

Input/action:

Insert a uniquely named account, print its ID, then query that ID through container `psql`.

Expected result:

The ID is positive and selects exactly the inserted row.

What concept it proves:

The repository requests generated keys and closes all resources after mapping the result.

PASS condition:

Application and PostgreSQL report the same ID and values.

FAIL symptoms:

ID `0`, no generated-key row, duplicate pool creation, or an acquisition timeout after repeated calls.

### Scenario 4 — `findAll()` maps ordered values

Input/action:

Call `findAll()` after inserting at least two rows.

Expected result:

The returned records are ordered by ascending ID and balances are `BigDecimal` values; an empty database would yield an empty list.

What concept it proves:

The result set is fully consumed and converted before JDBC resources close.

PASS condition:

Every queried row maps correctly and no live `ResultSet` escapes.

FAIL symptoms:

Only one row, `null`, unordered output, or imprecise floating-point balance values.

### Scenario 5 — Borrow and return are observable

Input/action:

Print pool metrics before, during, and after one connection try-with-resources block.

Expected result:

Active usage rises while borrowed and falls after close; the same pool identity appears throughout.

What concept it proves:

`Connection.close()` returns a logical handle to the still-open HikariCP pool.

PASS condition:

The relationship is visible even if total/idle snapshots vary.

FAIL symptoms:

Active remains elevated, the handle leaks, a new pool identity appears, or the pool is shut down after the borrow.

### Scenario 6 — Successful transfer commits atomically

Input/action:

Insert `100.00` and `50.00`, transfer `25.00`, and reread the pair.

Expected result:

Balances are `75.00` and `75.00`; pair total is `150.00`.

What concept it proves:

Debit and credit completed on one transaction and committed together.

PASS condition:

Both Java verification and pair-specific `psql` queries agree.

FAIL symptoms:

One-sided state, total drift, old in-memory values, or a table-wide total contaminated by earlier runs.

### Scenario 7 — Failed destination rolls back a real debit

Input/action:

After the successful transfer, send `10.00` from the real source to ID `-1`, catch the expected `SQLException`, and reread.

Expected result:

The destination update affects zero rows, rollback runs, and the pair remains `75.00` / `75.00` / `150.00`.

What concept it proves:

One connection contains both statements, and rollback undoes already-executed work.

PASS condition:

The before/after source balance and pair total compare numerically equal.

FAIL symptoms:

Debit never ran, debit remains applied, zero rows were ignored, or verification reused stale records.

### Scenario 8 — One pool has one lifecycle owner

Input/action:

Search the source for `new HikariDataSource`, factory calls, and Hikari types; inspect `Main` wiring.

Expected result:

Only the factory constructs Hikari's pool, `Main` invokes it once, and both consumers receive one `DataSource` reference.

What concept it proves:

Pool creation and shutdown are composition responsibilities, not persistence/business responsibilities.

PASS condition:

Repository/service contain no Hikari type, pool construction, pool close, or connection field.

FAIL symptoms:

Pool count grows with operations, consumers depend on Hikari internals, or multiple identities appear.

### Scenario 9 — Pool shutdown closes physical sessions

Input/action:

Let `Main` leave `try (pool)` normally and inspect `pool.isClosed()`.

Expected result:

The final line is `Pool closed: true`.

What concept it proves:

The application-lifetime owner shuts down HikariCP after all borrowed handles have returned.

PASS condition:

Shutdown occurs on both normal and exceptional exits through resource cleanup.

FAIL symptoms:

The JVM lingers, sessions remain after exit, or a consumer closes the pool prematurely.

### Scenario 10 — Named-volume persistence is understood

Input/action:

Run the application, use `docker compose down`, start again, and query an earlier marker. Only if the data is disposable, repeat with the warned `down -v` reset.

Expected result:

Plain `down` preserves rows; `down -v` deletes the exercise volume and the next start recreates only the schema.

What concept it proves:

Container lifetime and named-volume lifetime are different.

PASS condition:

Observed persistence matches the command used.

FAIL symptoms:

Unexpected data loss, schema edits assumed to migrate existing data, or destructive reset run in the wrong project.

---

## Before You Reveal the Solution

- [ ] I can draw the host, pool, driver, container, and volume path from memory.
- [ ] I observed one logical connection being borrowed and returned.
- [ ] I verified a generated key and mapped rows.
- [ ] I proved `75.00 + 75.00 = 150.00` after commit.
- [ ] I forced debit to run before missing-destination credit failed.
- [ ] I reread the database and proved rollback.
- [ ] I found exactly one pool creation path.
- [ ] I observed `Pool closed: true`.

---

## Complete Reference Solution

Compare this with your version only after attempting the tasks. Small naming or formatting differences are fine if the ownership graph, transaction boundary, resource cleanup, and observable behavior are equivalent.

The solution deliberately does not contain a real `.env`. Copy the template, replace the password locally, and keep the real file uncommitted.

### `docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: "${POSTGRES_DB:?Set POSTGRES_DB in .env}"
      POSTGRES_USER: "${POSTGRES_USER:?Set POSTGRES_USER in .env}"
      POSTGRES_PASSWORD: "${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD in .env}"
    ports:
      - "127.0.0.1:${POSTGRES_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 10s

volumes:
  postgres_data:
```

The service name is `postgres`. Host Java does not use that name; it reaches the published loopback port. The `postgres_data` volume outlives ordinary container removal.

### `.env.example`

```dotenv
# Values consumed by Docker Compose.
POSTGRES_DB=pool_practice
POSTGRES_USER=pool_app
POSTGRES_PASSWORD=replace-with-a-local-practice-password
POSTGRES_PORT=5432

# Java does not automatically read this file.
# Set DB_URL, DB_USERNAME, and DB_PASSWORD in PowerShell before Maven runs.
```

The local `.env` is a plaintext development convenience, not encryption or a production secret manager. The image's bootstrap `POSTGRES_USER` has broad privileges; that simplification is acceptable only for this disposable local exercise.

### `.gitignore`

```gitignore
.env
/target/
.idea/
.vscode/
*.iml
.classpath
.project
.settings/
```

### `pom.xml`

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

Dependency explanation:

| Entry | Why it exists |
|---|---|
| HikariCP | Implements the pooling `DataSource` and exposes pool lifecycle/metrics |
| pgJDBC | Implements PostgreSQL JDBC communication on the runtime classpath |
| Compiler plugin | Compiles source using Java release 17 |
| Exec plugin | Runs the ordinary `Main` class with Maven's dependency classpath |

There are two direct application dependencies. HikariCP may bring transitive APIs such as SLF4J, so `mvn dependency:tree` can contain more than two total lines.

### `database/schema.sql`

```sql
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_name VARCHAR(100) NOT NULL
        CHECK (btrim(owner_name) <> ''),
    balance NUMERIC(12, 2) NOT NULL
        CHECK (balance >= 0)
);
```

### `src/main/java/com/example/poolpractice/model/Account.java`

```java
package com.example.poolpractice.model;

import java.math.BigDecimal;

public record Account(
        long id,
        String ownerName,
        BigDecimal balance
) {
}
```

### `src/main/java/com/example/poolpractice/config/DatabaseSettings.java`

```java
package com.example.poolpractice.config;

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
            throw new IllegalStateException(
                    name + " is required and must not be blank");
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

The record validates configuration before the pool starts. Its safe `toString()` never contains the password value.

### `src/main/java/com/example/poolpractice/config/DataSourceFactory.java`

```java
package com.example.poolpractice.config;

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

The factory configures HikariCP but does not retain a global pool. `Main` invokes it once and owns the returned closeable object.

### `src/main/java/com/example/poolpractice/repository/AccountRepository.java`

```java
package com.example.poolpractice.repository;

import com.example.poolpractice.model.Account;

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
        this.dataSource = Objects.requireNonNull(
                dataSource, "dataSource");
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
                throw new SQLException(
                        "Expected one inserted row, got " + rows);
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

    public List<Account> findAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_ALL_SQL);
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

Both methods borrow their own logical handle because each is an independent operation. They return ordinary values only after all JDBC resources have closed.

### `src/main/java/com/example/poolpractice/service/TransferService.java`

```java
package com.example.poolpractice.service;

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
        this.dataSource = Objects.requireNonNull(
                dataSource, "dataSource");
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
                            "Source account is missing or has "
                                    + "insufficient funds: " + fromId);
                }

                int credited = credit(connection, toId, amount);
                if (credited != 1) {
                    throw new SQLException(
                            "Destination account not found: " + toId);
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

    private int debit(
            Connection connection,
            long accountId,
            BigDecimal amount
    ) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement(DEBIT_SQL)) {
            statement.setBigDecimal(1, amount);
            statement.setLong(2, accountId);
            statement.setBigDecimal(3, amount);
            return statement.executeUpdate();
        }
    }

    private int credit(
            Connection connection,
            long accountId,
            BigDecimal amount
    ) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement(CREDIT_SQL)) {
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
}
```

The helper methods deliberately receive `Connection`. HikariCP resets tracked connection state such as auto-commit when the logical handle returns to the pool.

### `src/main/java/com/example/poolpractice/diagnostics/PoolDiagnostics.java`

```java
package com.example.poolpractice.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public final class PoolDiagnostics {
    private PoolDiagnostics() {
    }

    public static void print(HikariDataSource pool, String label) {
        HikariPoolMXBean metrics = pool.getHikariPoolMXBean();
        if (metrics == null) {
            System.out.printf(
                    "[%s] pool=%s instance=%x not started%n",
                    label,
                    pool.getPoolName(),
                    System.identityHashCode(pool)
            );
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

These metrics expose a moment-in-time relationship. They do not expose credentials or promise a particular physical-connection identity.

### `src/main/java/com/example/poolpractice/Main.java`

```java
package com.example.poolpractice;

import com.example.poolpractice.config.DatabaseSettings;
import com.example.poolpractice.config.DataSourceFactory;
import com.example.poolpractice.diagnostics.PoolDiagnostics;
import com.example.poolpractice.model.Account;
import com.example.poolpractice.repository.AccountRepository;
import com.example.poolpractice.service.TransferService;
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
            throw new RuntimeException(
                    "Database exercise failed", exception);
        }
    }

    private static void run() throws SQLException {
        DatabaseSettings settings =
                DatabaseSettings.fromEnvironment();
        HikariDataSource pool =
                DataSourceFactory.create(settings);

        try (pool) {
            DataSource sharedDataSource = pool;
            System.out.println(
                    "Same object exposed as DataSource: "
                            + (sharedDataSource == pool));

            AccountRepository repository =
                    new AccountRepository(sharedDataSource);
            TransferService transfers =
                    new TransferService(sharedDataSource);

            PoolDiagnostics.print(pool, "startup");
            try (Connection connection =
                         sharedDataSource.getConnection()) {
                System.out.println(
                        "Borrowed logical connection: "
                                + connection.getClass().getName());
                PoolDiagnostics.print(pool, "while borrowed");
            }
            PoolDiagnostics.print(
                    pool,
                    "after Connection.close() returned it"
            );

            String marker =
                    Long.toString(System.currentTimeMillis());
            long fromId = repository.insert(
                    "Alice-" + marker,
                    new BigDecimal("100.00")
            );
            long toId = repository.insert(
                    "Bob-" + marker,
                    new BigDecimal("50.00")
            );

            if (fromId <= 0 || toId <= 0 || fromId == toId) {
                throw new IllegalStateException(
                        "Generated account IDs are invalid");
            }

            List<Account> initial = repository.findAll();
            BigDecimal initialTotal =
                    pairTotal(initial, fromId, toId);
            System.out.printf(
                    "Created accounts %d and %d; "
                            + "pair total=%s; marker=%s%n",
                    fromId,
                    toId,
                    initialTotal,
                    marker
            );

            if (initialTotal.compareTo(
                    new BigDecimal("150.00")) != 0) {
                throw new IllegalStateException(
                        "Unexpected initial pair total: "
                                + initialTotal);
            }

            transfers.transfer(
                    fromId,
                    toId,
                    new BigDecimal("25.00")
            );

            List<Account> afterSuccess =
                    repository.findAll();
            BigDecimal sourceAfterSuccess =
                    balanceOf(afterSuccess, fromId);
            BigDecimal destinationAfterSuccess =
                    balanceOf(afterSuccess, toId);
            BigDecimal totalAfterSuccess =
                    pairTotal(afterSuccess, fromId, toId);

            System.out.printf(
                    "After success: from=%s to=%s total=%s%n",
                    sourceAfterSuccess,
                    destinationAfterSuccess,
                    totalAfterSuccess
            );

            boolean successfulTransferWorked =
                    sourceAfterSuccess.compareTo(
                            new BigDecimal("75.00")) == 0
                    && destinationAfterSuccess.compareTo(
                            new BigDecimal("75.00")) == 0
                    && totalAfterSuccess.compareTo(
                            new BigDecimal("150.00")) == 0;

            if (!successfulTransferWorked) {
                throw new IllegalStateException(
                        "Successful transfer verification failed");
            }

            BigDecimal beforeFailedDebit =
                    sourceAfterSuccess;
            BigDecimal beforeFailedTotal =
                    totalAfterSuccess;

            try {
                transfers.transfer(
                        fromId,
                        -1L,
                        new BigDecimal("10.00")
                );
                throw new IllegalStateException(
                        "The rollback test unexpectedly succeeded");
            } catch (SQLException expected) {
                System.out.printf(
                        "Expected failure triggered rollback: "
                                + "SQLState=%s message=%s%n",
                        expected.getSQLState(),
                        expected.getMessage()
                );
            }

            List<Account> afterFailure =
                    repository.findAll();
            BigDecimal afterFailedDebit =
                    balanceOf(afterFailure, fromId);
            BigDecimal afterFailedTotal =
                    pairTotal(afterFailure, fromId, toId);

            boolean rollbackWorked =
                    beforeFailedDebit.compareTo(
                            afterFailedDebit) == 0
                    && beforeFailedTotal.compareTo(
                            afterFailedTotal) == 0;

            System.out.println(
                    "Rollback preserved balances: "
                            + rollbackWorked);

            if (!rollbackWorked) {
                throw new IllegalStateException(
                        "Rollback verification failed");
            }

            PoolDiagnostics.print(pool, "before shutdown");
        }

        System.out.println("Pool closed: " + pool.isClosed());
    }

    private static BigDecimal balanceOf(
            List<Account> accounts,
            long id
    ) {
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

    private static void printSQLExceptionChain(
            SQLException exception
    ) {
        for (SQLException current = exception;
             current != null;
             current = current.getNextException()) {
            System.err.printf(
                    "Database failure: SQLState=%s message=%s%n",
                    current.getSQLState(),
                    current.getMessage()
            );
        }
    }
}
```

### Run the solution

From `datasource-pool-practice`, with PostgreSQL healthy and the three host `DB_*` variables set:

```powershell
mvn clean compile
mvn dependency:tree
mvn exec:java
mvn clean package
```

Expected meaningful output:

```text
Same object exposed as DataSource: true
[startup] pool=account-practice-pool instance=... total=... active=0 idle=... waiting=0
Borrowed logical connection: com.zaxxer.hikari.pool.HikariProxyConnection
[while borrowed] pool=account-practice-pool instance=<same> total=... active=1 idle=... waiting=0
[after Connection.close() returned it] pool=account-practice-pool instance=<same> total=... active=0 idle=... waiting=0
Created accounts <source-id> and <destination-id>; pair total=150.00; marker=<marker>
After success: from=75.00 to=75.00 total=150.00
Expected failure triggered rollback: SQLState=null message=Destination account not found: -1
Rollback preserved balances: true
[before shutdown] pool=account-practice-pool instance=<same> total=... active=0 idle=... waiting=0
Pool closed: true
```

The exact pool counts can vary. The stable facts are one pool identity, no more than three managed physical connections, active usage while borrowed, conserved balances, successful rollback, and orderly pool shutdown.

---

## Application Execution — What Happens

```text
DatabaseSettings.fromEnvironment()
        ↓ reads and validates DB_*
DataSourceFactory.create(settings)
        ↓ builds HikariConfig
new HikariDataSource(config)
        ↓ starts one application-owned pool
Main exposes the same object as DataSource
        ├── AccountRepository borrows per operation
        └── TransferService borrows once per transaction
                    ↓
HikariCP supplies logical Connection proxies
                    ↓
pgJDBC sends SQL through physical PostgreSQL sessions
                    ↓
Connection.close() returns handles to the open pool
                    ↓
HikariDataSource.close() shuts the pool down at application exit
```

No framework discovers or wires these objects. The composition is ordinary Java and every ownership transition is visible in `Main`, constructors, and try-with-resources blocks.

---

## Architecture Review

### Runtime and dependency view

```text
Main
├── creates DatabaseSettings
├── asks DataSourceFactory for ONE HikariDataSource
├── retains HikariDataSource for metrics and shutdown
├── exposes the same object as javax.sql.DataSource
│   ├── passes it to AccountRepository
│   └── passes it to TransferService
└── coordinates verification

AccountRepository ──depends on──> DataSource
TransferService   ──depends on──> DataSource
PoolDiagnostics  ──observes────> HikariDataSource
```

`DataSource` is an interface for obtaining connections; it does not itself promise a pool. `HikariDataSource` implements that interface and adds HikariCP lifecycle and diagnostic operations. Depending on `DataSource` downstream avoids unnecessary coupling without hiding who owns the concrete resource.

### Resource ownership and lifetimes

```text
application
├── HikariDataSource pool ───────────────────────────────┐
│                                                       │
├── insert Connection ── statement ── generated keys ─┐ │
│                                                     └─┤
├── findAll Connection ── statement ── result set ─────┤
│                                                       │
├── transfer Connection                                │
│   ├── debit statement                                │
│   ├── credit statement                               │
│   └── commit or rollback                             │
│                                                       │
└── pool.close() ◄──────────────────────────────────────┘
```

Ownership rule:

| Resource | Owner | End of lifetime |
|---|---|---|
| `HikariDataSource` | `Main` | Application shutdown |
| Logical `Connection` | Method that called `getConnection()` | End of operation/transaction |
| `PreparedStatement` | Method that created it | End of SQL operation |
| `ResultSet` | Method that executed/read it | End of mapping |
| Physical PostgreSQL connection | HikariCP | Retirement, failure, eviction, or pool shutdown |
| PostgreSQL container | Docker Compose | `docker compose down` |
| PostgreSQL data | Named volume | Explicit volume deletion |

### One-connection transaction view

```text
TransferService.transfer(...)
        ↓ borrow once
Connection C
        ↓ setAutoCommit(false)
debit(C)
        ↓ update count must be 1
credit(C)
        ├── update count 1 ──> C.commit()
        └── update count 0 / exception
                    ↓
                C.rollback()
                    ↓
        rethrow original failure
        + suppress rollback failure if one occurred
```

The transaction cannot be split across `Connection A` and `Connection B`. A commit or rollback affects only the work associated with the connection on which it is called.

### Host and container networking view

```text
Windows host
│
├── Maven / Java process
│       ↓
│   HikariDataSource
│       ↓
│   HikariCP
│       ↓
│   pgJDBC
│       ↓
│   jdbc:postgresql://localhost:5432/pool_practice
│       ↓ published loopback port
│
└── Docker Compose
        ↓
    postgres service
        ↓
    PostgreSQL container:5432
        ↓
    postgres_data named volume
```

If Java later became another Compose service, the connection would normally use `jdbc:postgresql://postgres:5432/pool_practice`. That hostname works through Compose networking. It is intentionally not used by this host-run application.

### Responsibilities

| Type/file | Responsibility |
|---|---|
| `Main` | Compose objects, run verification, own and close the pool |
| `DatabaseSettings` | Read/validate host environment without exposing the password |
| `DataSourceFactory` | Translate settings into one configured Hikari pool |
| `Account` | Carry immutable account data |
| `AccountRepository` | Perform independent insert/select operations and map rows |
| `TransferService` | Own the debit/credit transaction boundary |
| `PoolDiagnostics` | Observe safe Hikari pool metrics |
| `docker-compose.yml` | Define the PostgreSQL development service |
| `schema.sql` | Define the fresh-database schema |

No Docker command is hidden in Java, no transaction is hidden in the repository, and no pool construction is hidden in a consumer.

### Pool reuse evidence

Use evidence carefully:

```text
getConnection()          → active count rises
Connection.close()       → active count falls; idle normally rises
later getConnection()    → another logical handle is supplied
pool.close()             → application-owned physical sessions end
```

Do not use `connection1 == connection2` as a reuse test. Proxy identities are not physical-session identities. Pool metrics, bounded PostgreSQL sessions, and correct handle closure are better beginner-level evidence.

### Optional live-session observation

To inspect physical sessions while the pool is open, temporarily add this immediately after the `before shutdown` metrics line:

```java
System.out.println(
        "Pool is open. Inspect pg_stat_activity, then press Enter.");
try {
    System.in.read();
} catch (java.io.IOException exception) {
    throw new RuntimeException(
            "Could not read console input", exception);
}
```

Run the program. From a second PowerShell window:

```powershell
docker compose exec postgres psql -U pool_app -d pool_practice -c 'SELECT pid, usename, datname, application_name, state FROM pg_stat_activity WHERE datname = current_database() AND pid <> pg_backend_pid() ORDER BY pid;'
```

Idle application sessions are normal while a pool remains open. Press Enter so `Main` shuts down the pool, then run the query again; Hikari's sessions should disappear, although administration tools may have their own sessions. Remove the pause afterward.

---

## Troubleshooting

Start at the boundary that failed:

```text
host Java
   ↓ DB_* and JDBC URL
localhost published port
   ↓
postgres Compose service
   ↓
database role / accounts table
   ↓
JDBC resource and transaction code
```

| Symptom | Likely cause | Inspect/correct |
|---|---|---|
| `mvn` is not recognized | Maven is not installed or not on `PATH` | Run `mvn --version`; install/configure Maven |
| `release version 17 not supported` | Maven is using an older JDK | Inspect `mvn --version` and `JAVA_HOME` |
| Docker client cannot reach daemon | Docker Desktop/Engine is stopped | Start it, then run `docker version` |
| `postgres` never becomes healthy | Initialization/configuration failed or server is still starting | `docker compose ps`; `docker compose logs --tail 100 postgres` |
| Host port is already in use | Local PostgreSQL or another container owns `5432` | Set `POSTGRES_PORT=5433` and also use `localhost:5433` in `DB_URL`, or stop the conflict |
| `Connection refused` | Service stopped, wrong host port, or wrong URL | Check Compose state and `docker compose port postgres 5432` |
| Host Java uses hostname `postgres` | Compose DNS is not available to a host process | Use `localhost` and the published port |
| A future Java container uses `localhost` | Container loopback points to itself | A Compose Java service would normally use `postgres:5432` |
| `DB_PASSWORD is required` | Variable is absent from Maven's PowerShell process | Set all `DB_*` in the same terminal; check presence without printing values |
| Java cannot see `.env` | Compose project interpolation does not export to host JVM | Set Java-facing variables explicitly |
| `password authentication failed` | Java/container values differ, or `.env` changed after volume initialization | Align credentials; remember an existing database role is not rewritten by editing `.env` |
| `relation "accounts" does not exist` | Wrong database or initialization script was skipped/failed | Verify `pool_practice`, logs, and `\d accounts` |
| Edited `schema.sql` has no effect | Initialization scripts run only for a fresh data directory | Apply SQL as a migration, or reset only this disposable volume |
| `no such service: ...` | Command uses the wrong service name | Use `postgres` |
| `No suitable driver` | pgJDBC is absent from the runtime classpath | Inspect POM and `mvn dependency:tree`; run with `mvn exec:java` |
| Pool grows on every operation | Code creates `HikariDataSource` repeatedly | Search factory calls and pool constructors |
| Timeout waiting for a connection | Handles leaked, operations slow, or pool exhausted | Audit every `getConnection()` path before increasing pool size |
| Debit remains after failed credit | Missing rollback or different connections used | Inspect transaction connection and helper parameters |
| Rollback test “passes” without undoing work | Failure happened before debit | Use a real funded source and missing destination second |
| Source becomes negative | Debit predicate or parameter order is wrong | Require `balance >= ?`; bind amount, ID, amount |
| Missing destination silently succeeds | Credit update count is ignored | Require exactly one affected row |
| Pool never shuts down | Lifecycle owner does not close `HikariDataSource` | Inspect `Main`'s outer try-with-resources |
| One-time SLF4J provider warning | Minimal project has no logging backend | It is harmless here; distinguish it from a JDBC exception |
| PostgreSQL rejects JVM zone `Asia/Saigon` | JVM supplies an alias not accepted in that path | Run `mvn "-Duser.timezone=Asia/Ho_Chi_Minh" exec:java` |

Useful compact diagnostic sequence:

```powershell
docker compose config --quiet
docker compose ps
docker compose logs --tail 100 postgres
docker compose exec postgres psql -U pool_app -d pool_practice -c '\d accounts'
mvn dependency:tree
```

### Fresh-volume reset

> **Destructive warning:** the next command deletes this exercise's named PostgreSQL volume and every row in it. Confirm that your current directory is the disposable `datasource-pool-practice` project and that its data can be lost.

```powershell
docker compose down -v
docker compose up -d
docker compose ps
```

| Command | Container/network | Named database volume |
|---|---|---|
| `docker compose down` | Removed | Kept |
| `docker compose down -v` | Removed | **Deleted** |

Never use a destructive reset as a substitute for proper production database migrations.

---

## Concept Coverage / What This Project Proves

“Direct” means you implement or observe it here. “Conceptual/future” means the guide explains the boundary but intentionally avoids infrastructure that would hide the core lesson.

| Concept | Project evidence | Coverage |
|---|---|---|
| `javax.sql.DataSource` abstraction | Repository/service constructor dependencies | Direct |
| `HikariDataSource` implementation | One factory result retained by `Main` | Direct |
| HikariCP physical pool | Configuration, proxy connections, metrics | Direct |
| One pool per application | One factory call and shared reference | Direct |
| Logical versus physical connection | Proxy class, borrow/return explanation | Direct |
| Connection borrowing/returning | `getConnection()` plus try-with-resources | Direct |
| `Connection.close()` versus pool close | Tasks 11 and 16 | Direct |
| `maximumPoolSize` / `minimumIdle` | Factory values and diagnostics | Direct |
| `connectionTimeout` / `idleTimeout` / `maxLifetime` | Factory values and semantics table | Direct |
| Pool diagnostics | Hikari MXBean snapshots | Direct |
| Resource ownership/leak prevention | Nested try-with-resources throughout | Direct |
| Prepared statements/generated keys | Repository insert | Direct |
| Result-set row mapping | Repository `findAll()` | Direct |
| One connection per transaction | Transfer helper signatures | Direct |
| Auto-commit, commit, rollback | Successful and forced-failure transfers | Direct |
| Suppressed rollback failure | `addSuppressed(...)` | Direct |
| `SQLException` handling | Update-count failures and exception-chain printer | Direct |
| Environment variables/password redaction | `DatabaseSettings` | Direct |
| PostgreSQL and pgJDBC | Real container-backed integration | Direct |
| Docker Compose and health checks | One-service infrastructure | Direct |
| Named-volume persistence | `down` versus `down -v` experiment | Direct |
| Fresh-volume schema initialization | Read-only init script mount | Direct |
| Host-versus-container networking | `localhost` versus `postgres` explanation | Direct |
| Advanced pool sizing under concurrency | No load generator in this small app | Conceptual/future |
| Transaction isolation/deadlock retry | Not required for the single-threaded use case | Future |
| Production migrations/secret management | Deliberately simplified local setup | Future |
| Spring, Spring Boot, JPA, Hibernate | Deliberately excluded | Future/different project |

The future rows are not accidental omissions. This project stays small so manual JDBC resource and transaction mechanics remain visible.

---

## Final Project Checklist

- [ ] Maven uses Java release 17 or newer.
- [ ] HikariCP and runtime pgJDBC are the only direct application dependencies.
- [ ] No Spring, Spring Boot, JPA, or Hibernate code/dependency exists.
- [ ] Docker Compose declares one PostgreSQL service and no Java service.
- [ ] The official PostgreSQL 17 image is used.
- [ ] The default host port is loopback `5432`, or the chosen override matches `DB_URL`.
- [ ] `database/schema.sql` is mounted read-only under `/docker-entrypoint-initdb.d/`.
- [ ] A named PostgreSQL data volume is declared.
- [ ] `.env` is ignored and `.env.example` contains only safe placeholders.
- [ ] I understand that Compose `.env` does not populate host Java's environment.
- [ ] Container `psql` confirms the real database/user and `accounts` table.
- [ ] Host Java uses `localhost`, not the Compose service name.
- [ ] `DatabaseSettings` rejects missing/blank values.
- [ ] Password values never appear in output or exceptions.
- [ ] `Main` causes exactly one `HikariDataSource` to be created.
- [ ] `maximumPoolSize`, `minimumIdle`, `connectionTimeout`, `idleTimeout`, and `maxLifetime` are configured.
- [ ] Repository and service receive the same `DataSource` object.
- [ ] Repository/service do not depend on Hikari-specific types.
- [ ] No class stores a long-lived `Connection` field.
- [ ] Every borrowed connection is closed by try-with-resources.
- [ ] Every statement and result set is closed.
- [ ] INSERT uses a prepared statement and returns a generated key.
- [ ] SELECT maps ordered `Account` records and never returns `null`.
- [ ] Pool diagnostics show total, active, idle, and waiting counts without secrets.
- [ ] I understand why proxy identity does not prove physical reuse.
- [ ] Transfer validates account IDs and a positive two-decimal amount.
- [ ] Debit and credit use exactly one shared transaction connection.
- [ ] Auto-commit is disabled before the first update.
- [ ] Both update counts are checked.
- [ ] Success commits only after both updates.
- [ ] Failure rolls back and rethrows the original exception.
- [ ] A rollback failure is attached as suppressed.
- [ ] Commit verification reads `75.00`, `75.00`, total `150.00`.
- [ ] Rollback verification makes debit run before credit fails.
- [ ] Rollback verification rereads unchanged database balances.
- [ ] `Connection.close()` and `HikariDataSource.close()` have distinct meanings.
- [ ] `Main` closes the pool once at application shutdown.
- [ ] The final output includes `Pool closed: true`.
- [ ] Plain `docker compose down` preserves the named volume.
- [ ] I understand that `docker compose down -v` permanently deletes its data.

---

## Reflection Questions

Answer without looking at the key.

1. Why is `DataSource` a better repository dependency than calling `DriverManager` throughout the application?
2. Does every `DataSource` automatically pool connections?
3. What normally happens when `close()` is called on a healthy Hikari proxy connection?
4. Why does `Main` retain `HikariDataSource` while repository/service receive `DataSource`?
5. Why should a repository borrow per operation instead of storing a connection field?
6. Why must debit and credit use exactly the same `Connection`?
7. What does `connectionTimeout` limit, and what does it not limit?
8. Why does a missing destination prove rollback only when debit runs first?
9. Why check update counts after both SQL statements?
10. Why attach rollback failure as suppressed instead of replacing the original exception?
11. Why use `BigDecimal.compareTo(...)` for balance verification?
12. Why does host Java use `localhost` while a hypothetical Compose Java service would use `postgres`?
13. Why does editing `schema.sql` not change a database in an existing volume?
14. What is the data-loss difference between `docker compose down` and `docker compose down -v`?
15. Why does Compose reading `.env` not make those values available through `System.getenv()`?
16. Why are active/idle counts better evidence than comparing two connection proxy objects?

---

## Reflection Answer Key

1. `DataSource` centralizes connection acquisition behind a standard interface, permits pooling, and makes resource supply replaceable without spreading driver/configuration code.
2. No. It is a connection-factory abstraction. `HikariDataSource` is a specific pooled implementation.
3. The logical handle closes and HikariCP normally returns a healthy underlying physical connection for reuse; broken or retired connections may be physically closed.
4. `Main` owns Hikari-specific metrics and shutdown. Consumers need only the standard acquisition operation and should not control pool lifecycle.
5. A stored connection can remain checked out, leak, carry stale transaction state, fail over time, and prevent fair pool reuse.
6. Commit and rollback apply to work on one database session represented by one connection. Separate connections create separate transaction contexts.
7. It limits how long a caller waits to acquire a connection. It does not limit SQL query execution time.
8. A failure before debit leaves nothing to undo. A successful debit followed by failed credit creates observable transactional work that rollback must reverse.
9. JDBC can execute an update successfully while matching zero rows. Business correctness requires exactly one source and one destination row.
10. The original error explains why the business transaction failed. Suppression preserves that primary cause while retaining evidence that cleanup also failed.
11. `compareTo` checks numeric value, while `equals` also requires identical scale.
12. Host Java reaches Docker through a published host port. Containers on a Compose network resolve one another by service name.
13. The official image runs initialization scripts only while creating a fresh data directory; an existing volume is started as-is.
14. Plain `down` removes the container/network and normally retains named volumes. `down -v` also deletes the named volume and all its database data.
15. Compose loads `.env` for project interpolation. It cannot modify the environment of its parent PowerShell process or an independently launched host JVM.
16. HikariCP can return distinct logical proxy objects around managed physical sessions. Metrics reveal whether handles are checked out and returned without assuming proxy identity.

---

## Final Mental Model

```text
Main declares:
“This process owns one configured pool.”
        ↓
The pool declares:
“Borrow a logical connection and return it promptly.”
        ↓
Repository methods declare:
“One independent database operation owns one short handle.”
        ↓
TransferService declares:
“Debit and credit share one connection and one commit/rollback decision.”
        ↓
Docker Compose declares:
“PostgreSQL runs separately; its named volume outlives its container.”
        ↓
Application exit declares:
“All handles are back; now the pool itself closes.”
```

If you can explain every arrow and name its owner, the project has achieved its purpose.

## Official references for later lookup

- [Java 17 `DataSource` API](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/javax/sql/DataSource.html)
- [HikariCP README and configuration](https://github.com/brettwooldridge/HikariCP)
- [HikariCP pool analysis](https://github.com/brettwooldridge/HikariCP/wiki/Pool-Analysis)
- [HikariCP JMX monitoring and management](https://github.com/brettwooldridge/HikariCP/wiki/MBean-%28JMX%29-Monitoring-and-Management)
- [PostgreSQL JDBC Driver documentation](https://jdbc.postgresql.org/documentation/)
- [PostgreSQL Docker Official Image](https://hub.docker.com/_/postgres)
- [Docker Compose environment interpolation](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/)
- [`docker compose down` reference](https://docs.docker.com/reference/cli/docker/compose/down/)
