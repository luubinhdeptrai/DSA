# JDBC + PostgreSQL Project Verification Guide

This guide verifies the project specified by `JDBC_PostgreSQL_Mini_Project.md`. It is tailored to the files currently located at:

```text
D:\DSA\JDBC\jdbc-postgresql-practice
```

It does **not** modify the implementation. It tells you how to inspect the code, execute each behavior, and confirm the real PostgreSQL state. If a test exposes a defect, the guide identifies the likely file or method to inspect.

> **Core rule:** compilation is not proof of correctness, console output is not proof of database state, and static inspection is not proof that runtime behavior works.

## How to Use This Guide

Open Windows PowerShell and start in the real project root:

```powershell
Set-Location -LiteralPath 'D:\DSA\JDBC\jdbc-postgresql-practice'
```

Run the stages in order. Stages 1, 2, and 5 are gates:

```text
project structure is coherent
        ↓
Maven build succeeds
        ↓
JDBC connection succeeds
        ↓
CRUD / generated keys / exceptions
        ↓
batch and transfer transactions
        ↓
persistence and final console test
```

If a gate fails, record the failure and continue only with independent checks. For example, Docker and schema inspection can still be performed when Java does not compile, but CRUD must not be marked PASS.

### Two kinds of evidence

| Evidence type | What it proves | What it does **not** prove |
|---|---|---|
| **STATIC / code inspection** | Required code, SQL, APIs, ownership, and package relationships are present | That the code compiles, connects, commits, rolls back, or changes the intended rows |
| **RUNTIME verification** | An operation actually ran and produced observable behavior | Database correctness unless the resulting rows/balances are also queried |

Every mutating Java test therefore has two halves:

```text
Java method/console result
        +
SELECT of actual PostgreSQL state
        =
usable verification evidence
```

### Safety rules

- Use fresh verification emails containing a timestamp.
- Query the before-state before every update, delete, transfer, or persistence test.
- Prefer an exact primary key or exact unique email in cleanup statements.
- Never print or paste the real database password into logs or screenshots.
- `TRUNCATE` and `docker compose down -v` are destructive. Their tests contain explicit warnings.
- Identity values may have gaps after failed or rolled-back inserts. A gap is normal and is not a failure.

## Actual Project Facts Found During Inspection

These are not tutorial placeholders. They were read from the actual project.

| Item | Actual value |
|---|---|
| Maven coordinates | `com.example:jdbc-postgresql-practice:1.0.0` |
| Java compiler release | `17` |
| pgJDBC | `org.postgresql:postgresql:42.7.13`, runtime scope |
| Project root | `D:\DSA\JDBC\jdbc-postgresql-practice` |
| Java package family | `com.example` (`config`, `dao`, `model`, and `service` subpackages) |
| Current `Main.java` declaration | `com.example.Main` |
| Main class configured in `pom.xml` | `com.example.Main` (matches current source) |
| Compose service | `postgres` |
| Container name | `jdbc-postgres` |
| Image | `postgres:17` |
| Database | `jdbc_practice` |
| Database role | `jdbc_app` |
| Port mapping | host `5432` → container `5432` |
| Compose volume key | `postgres_data` |
| Current Docker volume name | `jdbc-postgresql-practice_postgres_data` |
| Container data path | `/var/lib/postgresql/data` |
| Local configuration | `src/main/resources/database.properties` |
| Safe template | `src/main/resources/database.properties.example` |
| Expected packaged artifact | `target/jdbc-postgresql-practice-1.0.0.jar` |

The password is intentionally omitted. It is a development credential, but verification output still should not disclose it.

## Inspection Snapshot Before Testing

The following observations were refreshed on **2026-08-27 at 23:01 (UTC+07)** after concurrent source edits. Rerun every test yourself; this table is a starting point, not permanent evidence.

| Area | Observed status | Evidence |
|---|---|---|
| Docker Compose configuration | PASS | `docker compose config` parsed successfully |
| PostgreSQL process | PASS | Service `postgres` was Up and `pg_isready` reported accepting connections |
| Published port and volume | PASS | `5432:5432` and `jdbc-postgresql-practice_postgres_data` were present |
| Database schema | PASS | `students` and `accounts`, their constraints, and two seed accounts were queried |
| Current student data | Informational | `students` contained zero rows at inspection time |
| Maven main compilation | PASS | `mvn clean compile` compiled all five current main sources with release 17 |
| Maven tests/package | **FAIL** | `MainTest.java` imports JUnit 4 while the POM has no JUnit dependency, so `testCompile` fails and no normal package is produced |
| Property-file JDBC configuration | **FAIL by inspection** | Resource name, password property key, and pgJDBC URL prefix are inconsistent |
| Host JVM/PostgreSQL timezone handshake | **FAIL under the current default** | With correct diagnostic `DB_*` overrides, pgJDBC reached the server but PostgreSQL rejected startup `TimeZone=Asia/Saigon`; scoped canonical `Asia/Ho_Chi_Minh` succeeded |
| Default console connection | **FAIL** | `mvn compile exec:java` printed `Configuration error: Missing configuration: db.url ...`; Maven then printed `BUILD SUCCESS` only because Main caught the runtime error |
| Diagnostic console connection | PASS narrowly | With all three correct process-scoped `DB_*` overrides plus canonical timezone, Main printed `Connected successfully`, displayed the full menu, and exited on option 0 |
| DAO resource handling | PASS statically | JDBC resources use try-with-resources and result cursors are checked |
| CRUD/generated-key behavior | Not yet runtime-verified | Static DAO logic looks appropriate, but the current project cannot reach runtime |
| Batch | PASS statically / not runtime-verified | Transaction logic and `List<Student>` public contract match the specification; commit/rollback still require database-backed tests |
| Transfer | PASS statically / not runtime-verified | Core transaction and `findBalance(long)` are present; commit/rollback still require database-backed tests |

The current snapshot cannot receive an overall PASS merely because Docker and the schema are healthy.

---

## Stage 1 — Project Structure

### Test 1 — Maven layout, packages, resources, ignore rules, and Docker scope

**Verification Type:** STATIC

**Purpose:**  
Verify that Maven can discover the sources/resources, Java package declarations agree with directories and imports, secrets/build output are ignored, and Compose runs only PostgreSQL.

**Preconditions:**  
PowerShell is open at `D:\DSA\JDBC\jdbc-postgresql-practice`. Git is available for the ignore check.

**Commands / Steps:**

1. List the relevant files:

   ```powershell
   Get-ChildItem -LiteralPath . -Force
   Get-ChildItem -LiteralPath '.\src\main\java' -Recurse -Filter '*.java' | Select-Object FullName
   Get-ChildItem -LiteralPath '.\src\main\resources' -Force | Select-Object Name
   Get-ChildItem -LiteralPath '.\src\test\java' -Recurse -Filter '*.java' | Select-Object FullName
   ```

2. Inspect every package declaration:

   ```powershell
   Get-ChildItem '.\src\main\java','.\src\test\java' -Recurse -Filter '*.java' |
       Select-String -Pattern '^package\s+'
   ```

3. Compare these three facts together:

   ```powershell
   Select-String -LiteralPath '.\src\main\java\com\example\Main.java' -Pattern '^package|^import com\.example'
   Select-String -LiteralPath '.\pom.xml' -Pattern '<mainClass>'
   Get-ChildItem -LiteralPath '.\src\main\java\com\example' -Recurse -Filter '*.java' |
       Select-String -Pattern '^package'
   ```

4. Confirm the private file is ignored and the example remains visible to Git:

   ```powershell
   git check-ignore -v -- 'src/main/resources/database.properties'
   git status --short -- 'src/main/resources/database.properties' 'src/main/resources/database.properties.example'
   ```

5. Confirm there is no Java container or Java `Dockerfile`:

   ```powershell
   Get-Content -LiteralPath '.\compose.yaml' |
       ForEach-Object {
           if ($_ -match '^\s*POSTGRES_PASSWORD\s*:') {
               '      POSTGRES_PASSWORD: <redacted>'
           } else {
               $_
           }
       }
   Get-ChildItem -LiteralPath . -Recurse -File -Filter 'Dockerfile*'
   ```

6. Check the expected layout:

   ```text
   jdbc-postgresql-practice/
   ├── pom.xml
   ├── compose.yaml
   ├── .gitignore
   └── src/
       ├── main/
       │   ├── java/com/example/...
       │   └── resources/
       │       ├── database.properties          (local, ignored)
       │       └── database.properties.example  (shareable)
       └── test/java/com/example/MainTest.java
   ```

   `src/test/resources` is optional when there are no test resources.

**Example Input:**  
No runtime input.

**Expected Java Result:**  
Not applicable — this is static inspection.

**Expected Database Result:**  
Not applicable; this test must not change database state.

**Verification SQL:**  
Not applicable.

**PASS:**

- Every required file exists.
- Each package maps to its directory below `src/main/java` or `src/test/java`.
- Imports point to packages that really exist.
- The POM main class exactly equals the fully qualified package + class name of `Main`.
- `database.properties` is reported as ignored; the `.example` file is not ignored.
- `target/` is ignored.
- `compose.yaml` contains only the actual `postgres` database service.
- No Java Dockerfile exists.

**FAIL:**

- A declared package differs from its source path.
- An import names a package that is absent.
- The POM points at a different main class.
- The real properties file is not ignored.
- A Java container was added even though Java is required to run on the host.

**If It Fails:**  
Inspect the mismatching source file, `pom.xml`, `.gitignore`, or `compose.yaml`. In the latest inspected snapshot, source paths/packages/imports and the POM main class use `com.example` consistently, so this structural portion passes.

---

## Stage 2 — Maven Build

### Test 2 — Toolchain, compilation, dependency graph, tests, and package

**Verification Type:** STATIC + RUNTIME BUILD

**Purpose:**  
Verify the JDK/Maven toolchain, compile all main sources, prove pgJDBC is on the Maven runtime graph, compile/run tests, and create the expected JAR.

**Preconditions:**  
Test 1 passes. Internet access may be needed the first time Maven downloads plugins/dependencies.

**Commands / Steps:**

Run each command separately and read the first error before continuing:

```powershell
mvn --version
mvn clean compile
mvn dependency:tree
mvn test
mvn clean package
Get-ChildItem -LiteralPath '.\target' -Recurse | Select-Object FullName
```

What each command verifies:

| Command | What it verifies | Success evidence/output |
|---|---|---|
| `mvn --version` | Maven is on `PATH`, and which JDK Maven uses | Maven version plus Java version 17 or newer |
| `mvn clean compile` | Deletes old `target`, copies resources, and compiles all main Java sources with release 17 | `BUILD SUCCESS`; classes under `target/classes` |
| `mvn dependency:tree` | Maven resolved direct and transitive dependencies | `org.postgresql:postgresql:jar:42.7.13:runtime`, plus its transitive `checker-qual` |
| `mvn test` | Runs all phases through test compilation/execution | `BUILD SUCCESS`; either the intended tests pass or, per the original no-framework exercise, zero tests run cleanly |
| `mvn clean package` | Repeats prior phases and packages only after compilation/tests pass | `BUILD SUCCESS`; `target/jdbc-postgresql-practice-1.0.0.jar` |

`mvn clean package` does not mean “only make a JAR”:

```text
clean target/
      ↓
validate → compile → test → package
```

**Example Input:**  
No application input.

**Expected Java Result:**  
No application is run. Maven must report `BUILD SUCCESS` for compile, test, and package. The expected JAR is a thin JAR; it does not embed pgJDBC and is not proof that JDBC works.

**Expected Database Result:**  
No database changes.

**Verification SQL:**  
Not applicable.

**PASS:**

- Maven uses Java 17+.
- All five current main sources compile.
- The dependency tree includes pgJDBC at runtime.
- `mvn test` succeeds.
- `mvn clean package` produces `target/jdbc-postgresql-practice-1.0.0.jar`.

**FAIL:**

- `package ... does not exist` or `cannot find symbol` occurs in a main source.
- `org.junit does not exist` occurs during `testCompile`.
- pgJDBC is absent from `dependency:tree` or has the wrong scope.
- Maven says success but no expected JAR exists.

**If It Fails:**  
Start with the **first** error, not the final summary. In the latest inspected snapshot `mvn clean compile` succeeds, but `mvn test` reaches `testCompile` and fails because `MainTest.java` imports JUnit 4 while `pom.xml` has no JUnit dependency. `mvn clean package` therefore fails for the same reason. The original specification intentionally did not require a test framework, so do not add a dependency merely to preserve a meaningless generated `assertTrue(true)` test. Also remember that `mvn compile exec:java` can end with Maven's `BUILD SUCCESS` even when this Main catches a configuration exception, prints `Configuration error: ...`, and returns normally; read the application output too.

---

## Stage 3 — Docker/PostgreSQL Infrastructure

### Test 3 — Compose configuration, container, published port, readiness, and volume

**Verification Type:** RUNTIME INFRASTRUCTURE

**Purpose:**  
Verify that Docker is available, the real Compose configuration is valid, the `postgres` service is running, host port 5432 is published, PostgreSQL is ready, and the named volume exists.

**Preconditions:**  
Docker Desktop/Engine is running. Commands are executed beside `compose.yaml`.

**Commands / Steps:**

```powershell
docker --version
docker compose version
docker info --format '{{.ServerVersion}}'
docker compose config --quiet
docker compose config --services
docker compose config --volumes
docker compose up -d
docker compose ps --all
docker compose port postgres 5432
docker compose logs --tail 50 postgres

for ($attempt = 1; $attempt -le 30; $attempt++) {
    docker compose exec -T postgres pg_isready -h 127.0.0.1 -p 5432 -U jdbc_app -d jdbc_practice
    if ($LASTEXITCODE -eq 0) { break }
    Start-Sleep -Seconds 1
}
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL did not become ready.' }

docker volume ls --filter 'name=jdbc-postgresql-practice_postgres_data'
docker volume inspect jdbc-postgresql-practice_postgres_data
```

Important distinction:

- Compose **service name:** `postgres` — use it in `docker compose exec postgres ...`.
- Container name: `jdbc-postgres` — useful with plain `docker` commands.
- Volume key in YAML: `postgres_data`.
- Current Docker engine volume name: `jdbc-postgresql-practice_postgres_data` because Compose prefixes the project name.

There is no Compose healthcheck in this project, so `docker compose ps` may show `Up` without the word `healthy`. `pg_isready` is the readiness proof.

**Example Input:**  
No application input.

**Expected Java Result:**  
Not applicable; Java is not run.

**Expected Database Result:**  
The existing database remains unchanged. The service reports ready to accept connections.

**Verification SQL:**  
Not applicable in this infrastructure test.

**PASS:**

- Docker client and daemon respond.
- `docker compose config` has no error.
- `docker compose ps --all` shows `jdbc-postgres`, service `postgres`, status `Up`, and `5432->5432`.
- `pg_isready` prints `127.0.0.1:5432 - accepting connections`.
- the named volume exists and is mounted at `/var/lib/postgresql/data`.

**FAIL:**

- Docker daemon connection errors.
- Compose cannot find/parse `compose.yaml`.
- Container is absent, exited, or restarting.
- Port 5432 is not published or is already occupied.
- `pg_isready` says no response/rejecting connections.
- Volume is absent or the container uses an anonymous volume.

**If It Fails:**  
Inspect Docker Desktop, `compose.yaml`, and `docker compose logs postgres`. A Java error should not be debugged until `pg_isready` passes. If 5432 is occupied, the Compose host port and the JDBC URL must be changed together; PostgreSQL still listens on container port 5432.

---

## Stage 4 — Database Verification

### Test 4 — Session identity, tables, schemas, constraints, and seed rows

**Verification Type:** RUNTIME DATABASE

**Purpose:**  
Prove that the Java application will target the intended database/user and that both tables satisfy the original specification.

**Preconditions:**  
Test 3 passes. The `postgres` service is ready.

**Commands / Steps:**

Open the real in-container `psql` client:

```powershell
docker compose exec postgres psql -X -U jdbc_app -d jdbc_practice
```

At the `jdbc_practice=>` prompt, run:

```sql
\conninfo

SELECT current_database(), current_user, current_schema();

\dt
\d+ students
\d+ accounts

SELECT
    conrelid::regclass AS table_name,
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE conrelid IN ('students'::regclass, 'accounts'::regclass)
ORDER BY table_name, constraint_name;

SELECT id, name, email, age, created_at
FROM students
ORDER BY id;

SELECT id, owner_name, balance
FROM accounts
ORDER BY id;

SELECT SUM(balance) AS total_balance
FROM accounts;

\q
```

Expected schema essentials:

| Table | Required evidence |
|---|---|
| `students` | identity `BIGINT` primary key; nonblank/non-null `name`; unique/non-null `email`; `age` check 0–150; defaulted non-null `created_at` |
| `accounts` | identity `BIGINT` primary key; non-null owner; `NUMERIC(15,2)` nonnegative balance |

Expected initial account rows from the specification are Account A with `1000.00` and Account B with `500.00`. IDs are normally 1 and 2 after a fresh seed, but always read the actual IDs before a transfer.

**Example Input:**  
Database `jdbc_practice`, role `jdbc_app`.

**Expected Java Result:**  
Not applicable; Java is not run.

**Expected Database Result:**  
Both tables and all constraints are visible. The inspected database contained Account A ID 1 with `1000.00`, Account B ID 2 with `500.00`, total `1500.00`, and no student rows.

**Verification SQL:**

```sql
SELECT current_database(), current_user;
SELECT COUNT(*) AS student_count FROM students;
SELECT id, owner_name, balance FROM accounts ORDER BY id;
SELECT SUM(balance) AS total_balance FROM accounts;
```

**PASS:**

- Current database is `jdbc_practice` and current user is `jdbc_app`.
- `students` and `accounts` both exist in the visible schema.
- Every required constraint/default/type is present.
- Both initial accounts exist with expected balances.

**FAIL:**

- Connected to another database or role.
- A relation is missing (`42P01`).
- Unique, check, primary-key, identity, or not-null behavior is absent.
- Seed accounts are missing/duplicated or have unexpected balances before transaction testing.

**If It Fails:**  
Inspect the database selected by the `-d` argument, the volume history, and the table-creation SQL from the original project guide. Compose creates the database/role only when initializing an empty data directory; it does not create these application tables because this project mounts no initialization SQL.

---

## Stage 5 — JDBC Connection

### Test 5 — Host Java to PostgreSQL, configuration resolution, and controlled failures

**Verification Type:** STATIC + RUNTIME

**Purpose:**  
Verify this complete path and prove that bad credentials/routes fail rather than producing a false success:

```text
Java host process
      ↓ DriverManager
pgJDBC on Maven runtime classpath
      ↓ jdbc:postgresql://localhost:5432/jdbc_practice
host port 5432
      ↓ Docker publication
PostgreSQL container port 5432
      ↓
jdbc_practice as jdbc_app
```

**Preconditions:**  
Test 1, the main-compilation/dependency-tree portions of Test 2, and Tests 3–4 pass. Record the current unrelated `MainTest`/package failure, but it does not prevent `exec:java` from testing Main. Use a disposable PowerShell window for temporary environment variables.

**Commands / Steps:**

1. Check whether environment overrides are active **without printing their values**:

   ```powershell
   'DB_URL set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_URL))
   'DB_USERNAME set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_USERNAME))
   'DB_PASSWORD set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_PASSWORD))
   ```

2. In this disposable process, explicitly remove any inherited overrides and repeat the boolean checks until all three say `False`:

   ```powershell
   Remove-Item Env:DB_URL,Env:DB_USERNAME,Env:DB_PASSWORD -ErrorAction SilentlyContinue
   'DB_URL set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_URL))
   'DB_USERNAME set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_USERNAME))
   'DB_PASSWORD set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_PASSWORD))
   ```

   This changes only the current PowerShell process. Closing the window restores the normal inherited environment for future shells.

3. Inspect non-secret configuration while masking the password:

   ```powershell
   Get-Content -LiteralPath '.\src\main\resources\database.properties' |
       ForEach-Object {
           if ($_ -match '^\s*db\.password\s*=') { 'db.password=<redacted>' } else { $_ }
       }

   Select-String -LiteralPath '.\src\main\java\com\example\config\DatabaseConfig.java' `
       -Pattern 'CONFIG_FILE|requiredValue|DriverManager|getResourceAsStream'
   ```

4. Verify these exact relationships:

   | Concern | Required value/behavior |
   |---|---|
   | Classpath resource | `database.properties` |
   | URL property | `db.url` |
   | Username property | `db.username` |
   | Password property | `db.password` |
   | Driver URL prefix | `jdbc:postgresql:` |
   | Host | `localhost` because Java runs on the host |
   | Port/database | `5432/jdbc_practice` |
   | Resolution | nonblank `DB_*` environment value first; property fallback otherwise |

5. With `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` absent, run the required console connection path:

   ```powershell
   mvn compile exec:java
   ```

   This specifically verifies the local properties fallback. Expected first line: `Connected successfully`. Enter `0` when the menu appears so this connection-only run exits cleanly.

6. On this inspected host, also test the observed JVM/PostgreSQL timezone boundary. PostgreSQL 17 lists `Asia/Ho_Chi_Minh` but not the legacy `Asia/Saigon` name:

   ```powershell
   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT name FROM pg_timezone_names WHERE name IN ('Asia/Saigon', 'Asia/Ho_Chi_Minh') ORDER BY name;"

   mvn "-Duser.timezone=Asia/Ho_Chi_Minh" compile exec:java
   ```

   The scoped `-Duser.timezone` option is a diagnostic/runtime configuration, not an implementation edit. If default startup reports `FATAL: invalid value for parameter "TimeZone": "Asia/Saigon"` but the canonical run connects, the network, credentials, and driver route work; record the default-timezone compatibility issue separately.

7. Test the environment-variable branch with correct values without echoing the password. Save the good values in variables so each negative test changes only one condition:

   ```powershell
   $env:DB_URL = 'jdbc:postgresql://localhost:5432/jdbc_practice'
   $env:DB_USERNAME = 'jdbc_app'
   $securePassword = Read-Host 'POSTGRES_PASSWORD from compose.yaml' -AsSecureString
   $env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
   $goodDbUrl = $env:DB_URL
   $goodDbPassword = $env:DB_PASSWORD
   mvn "-Duser.timezone=Asia/Ho_Chi_Minh" compile exec:java
   ```

   Enter `0` after the connection succeeds. This proves environment values take precedence, but it does not replace the property-fallback check.

8. After the good environment connection passes, change **only** the password:

   ```powershell
   $env:DB_PASSWORD = 'definitely-wrong-verification-password'
   mvn "-Duser.timezone=Asia/Ho_Chi_Minh" compile exec:java
   $env:DB_PASSWORD = $goodDbPassword
   ```

   Expect authentication failure, normally SQLState `28P01`. Do not save this value to a file.

9. With the good password restored, change **only** the host port:

   ```powershell
   $env:DB_URL = 'jdbc:postgresql://localhost:65432/jdbc_practice'
   mvn "-Duser.timezone=Asia/Ho_Chi_Minh" compile exec:java
   $env:DB_URL = $goodDbUrl
   ```

   Expect a connection failure/refusal. It is commonly an SQLState in class `08`, but the exact state/message can vary by driver and operating system.

10. Rerun the restored good environment configuration, enter `0`, and only then close the disposable PowerShell window:

    ```powershell
    mvn "-Duser.timezone=Asia/Ho_Chi_Minh" compile exec:java
    ```

`psql` inside the container is not a substitute for this test. It proves PostgreSQL works internally; it does not prove the host-published TCP route, pgJDBC classpath, or Java configuration.

**Example Input:**  
Good URL: `jdbc:postgresql://localhost:5432/jdbc_practice`; username: `jdbc_app`; password: the matching local development value, never printed.

**Expected Java Result:**

- Good values: `Connected successfully`, followed by the menu. On this host, the scoped canonical timezone option may be required.
- Wrong password: a caught `SQLException`, usually SQLState `28P01`; no success message.
- Wrong port: a caught connection exception; no success message.

**Expected Database Result:**  
No rows or balances change in any connection test.

**Verification SQL:**

```sql
SELECT current_database(), current_user;
```

This query describes the intended session, but the Java connection success must still be observed from Java.

**PASS:**

- Static names/keys/URL are correct.
- The local ignored properties file works when no environment override exists.
- All three environment variables override properties when deliberately set.
- Correct settings connect through localhost:5432.
- PostgreSQL accepts the JVM timezone used for the session; the observed `Asia/Saigon`/`Asia/Ho_Chi_Minh` distinction is understood and recorded.
- Wrong password and wrong port both fail, and restoring settings reconnects.
- Diagnostics show message, SQLState, and vendor code without showing a password.

**FAIL:**

- Compilation or main-class lookup fails before a connection attempt.
- `Missing configuration: db.url`/password appears despite a correct resource.
- `No suitable driver` appears.
- PostgreSQL rejects startup parameter `TimeZone=Asia/Saigon` and the runtime continues using that unsupported name.
- A wrong password or wrong port prints success.
- Secrets appear in output.

**If It Fails:**  
Inspect `DatabaseConfig.getConnection()`, `loadProperties()`, `requiredValue()`, both properties files, the POM runtime dependency, the current host JVM timezone, and the POM/Main fully qualified class name. The inspected snapshot has three independent configuration defects: it requests resource `database_properties` instead of `database.properties`, requests `db.passwird` instead of `db.password`, and uses `jdbc:postgres://` instead of pgJDBC's `jdbc:postgresql://`. Main compilation now passes, and the default run reaches Main but prints `Configuration error: Missing configuration: db.url ...`. A direct diagnostic with correct environment overrides reached PostgreSQL but failed under `Asia/Saigon`; the same probe succeeded with JShell runtime option `-R-Duser.timezone=Asia/Ho_Chi_Minh`. Environment overrides and a timezone option can isolate deeper behavior, but bypassing broken defaults does **not** make this test PASS.

---

## Non-Invasive Method Probe for Stages 6–18

The final console application remains the required end-to-end route. Some details—such as comparing the returned generated ID with `student.getId()` or deliberately constructing a failed batch—are easier to inspect directly. JShell can call the actual public classes without adding a dependency or source file.

Use this only after `mvn clean compile` passes:

```powershell
# Omit this credential block only after Test 5 proves the property fallback works.
# In the current snapshot it must execute before JShell is launched.
$env:DB_URL = 'jdbc:postgresql://localhost:5432/jdbc_practice'
$env:DB_USERNAME = 'jdbc_app'
$securePassword = Read-Host 'POSTGRES_PASSWORD from compose.yaml' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password

mvn clean compile
mvn -q dependency:build-classpath "-DincludeScope=runtime" "-Dmdep.outputFile=target/runtime-classpath.txt"

$driverClasspath = (Get-Content -Raw -LiteralPath '.\target\runtime-classpath.txt').Trim()
$runtimeClasspath = (Resolve-Path '.\target\classes').Path + [IO.Path]::PathSeparator + $driverClasspath
$env:VERIFY_TAG = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds().ToString()
jshell "-R-Duser.timezone=Asia/Ho_Chi_Minh" --class-path $runtimeClasspath
```

At the JShell prompt:

```java
import com.example.dao.StudentDAO;
import com.example.model.Student;
import com.example.service.AccountTransferService;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

var dao = new StudentDAO();
var transfers = new AccountTransferService();
var run = System.getenv("VERIFY_TAG");
```

Keep JShell open in PowerShell window A. Use a second PowerShell window B for the `psql` checks and copy the non-secret tag printed for `run`:

```powershell
Set-Location -LiteralPath 'D:\DSA\JDBC\jdbc-postgresql-practice'
$env:VERIFY_TAG = '<paste the run value printed by JShell window A>'
```

This keeps the Java session alive while you independently inspect PostgreSQL. When `DB_*` overrides bypass the current property defects, label every result **diagnostic isolation only** and never award Stage 5 PASS from that workaround.

Exit JShell with `/exit`. Close the disposable PowerShell window afterward so those process-scoped overrides disappear.

### Optional component-only isolation if a future Main compile error blocks probing

The latest inspected main sources compile, so the normal probe above is preferred. If a later edit introduces an unrelated Main compile error, first record Test 2 as FAIL. You may then isolate the already-written DAO/service code by compiling only those four components into generated `target` output. This does not edit source and does **not** turn Maven, configuration, architecture, or final-console status into PASS:

```powershell
New-Item -ItemType Directory -Force -Path '.\target\verification-classes' | Out-Null

javac --release 17 -d '.\target\verification-classes' `
    '.\src\main\java\com\example\config\DatabaseConfig.java' `
    '.\src\main\java\com\example\model\Student.java' `
    '.\src\main\java\com\example\dao\StudentDAO.java' `
    '.\src\main\java\com\example\service\AccountTransferService.java'

mvn -q dependency:build-classpath "-DincludeScope=runtime" "-Dmdep.outputFile=target/verification-classpath.txt"

$driverClasspath = (Get-Content -Raw -LiteralPath '.\target\verification-classpath.txt').Trim()
$runtimeClasspath = (Resolve-Path '.\target\verification-classes').Path + [IO.Path]::PathSeparator + $driverClasspath

$env:DB_URL = 'jdbc:postgresql://localhost:5432/jdbc_practice'
$env:DB_USERNAME = 'jdbc_app'
$securePassword = Read-Host 'POSTGRES_PASSWORD from compose.yaml' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
$env:VERIFY_TAG = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds().ToString()

jshell "-R-Duser.timezone=Asia/Ho_Chi_Minh" --class-path $runtimeClasspath
```

Use the same JShell imports shown above. These environment overrides intentionally bypass the broken properties path so you can answer a narrower question: “Does this DAO/service method behave correctly when given a valid connection?”

---

## Stage 6 — CREATE Student

### Test 6 — Insert one valid student and verify the generated object/database state

**Verification Type:** STATIC + RUNTIME

**Purpose:**  
Verify parameter binding, one affected row, a positive generated ID, mutation of the `Student` object's ID, and the actual inserted row.

**Preconditions:**  
The main-compilation/dependency-tree portions of Test 2 and Tests 3–4 pass. Test 5 either passes normally or the connection is explicitly labeled diagnostic isolation. PostgreSQL is ready; JShell has `dao` and `run` from the probe setup. The unrelated Maven test/package failure remains recorded.

**Commands / Steps:**

At the JShell prompt:

```java
var createEmail = "verify.create." + run + "@example.test";
var createdStudent = new Student("Verification Alice", createEmail, 21);
long createdId = dao.create(createdStudent);
System.out.println("affected operation returned normally");
System.out.println("returned ID=" + createdId);
System.out.println("object ID=" + createdStudent.getId());
```

In PowerShell, query the real database:

```powershell
docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
    -c "SELECT id, name, email, age FROM students WHERE email = 'verify.create.$($env:VERIFY_TAG)@example.test';"
```

The method does not expose the raw affected-row count. Inspect its enforcement:

```powershell
Select-String -LiteralPath '.\src\main\java\com\example\dao\StudentDAO.java' `
    -Pattern 'executeUpdate|affected|Expected one inserted row'
```

**Example Input:**

| Field | Value |
|---|---|
| Name | `Verification Alice` |
| Email | `verify.create.<VERIFY_TAG>@example.test` |
| Age | `21` |

**Expected Java Result:**  
The method returns normally; returned ID is greater than 0; `createdStudent.getId()` contains the same value. The source requires `executeUpdate()` to return exactly 1 or throws.

**Expected Database Result:**  
Exactly one row has the test email, matching name and age, with the same positive ID.

**Verification SQL:**

```sql
SELECT id, name, email, age
FROM students
WHERE email = 'verify.create.<VERIFY_TAG>@example.test';
```

**PASS:**  
Java returned one positive ID, the object received that same ID, and PostgreSQL contains exactly one matching row.

**FAIL:**  
An exception occurs for valid data, ID is 0/nonpositive, object ID differs, more/less than one row exists, or console claims creation while the query finds nothing.

**If It Fails:**  
Inspect `StudentDAO.create()`, `bindStudent()`, the `INSERT_SQL` column order, connection configuration, and the `students` constraints. A configuration or compilation failure is a prerequisite failure, not a CREATE failure.

---

## Stage 7 — FIND Student by ID

### Test 7 — Find an existing ID and a confirmed-missing ID

**Verification Type:** STATIC + RUNTIME

**Purpose:**  
Verify `ResultSet.next()`, row mapping, and the required `null` result for no row.

**Preconditions:**  
Test 6 produced `createdId`; JShell remains open.

**Commands / Steps:**

```java
var existingStudent = dao.findById(createdId);
System.out.println(existingStudent);

long missingId = Long.MAX_VALUE;
var missingStudent = dao.findById(missingId);
System.out.println("missing result=" + missingStudent);
```

Inspect the cursor check and mapper:

```powershell
Select-String -LiteralPath '.\src\main\java\com\example\dao\StudentDAO.java' `
    -Pattern 'findById|rs\.next|mapRow|getLong\("id"\)|getString\("name"\)|getString\("email"\)|getInt\("age"\)'
```

**Example Input:**  
Existing: the `createdId` printed in Test 6. Missing: `9223372036854775807` (`Long.MAX_VALUE`).

**Expected Java Result:**

- Existing ID: a non-null `Student` with the exact ID, name, email, and age.
- Missing ID: `null`, not an exception and not an empty fake `Student`.

**Expected Database Result:**  
This read-only operation changes nothing. The existing row remains; no row has the missing ID.

**Verification SQL:**

```sql
SELECT id, name, email, age FROM students WHERE id = <CREATED_ID>;
SELECT COUNT(*) FROM students WHERE id = 9223372036854775807;
```

**PASS:**  
The mapped Java fields equal the selected database fields, and the missing lookup returns `null` after `next()` reports no row.

**FAIL:**  
Getter calls occur before `next()`, fields are swapped/missing, the existing row returns null, or the missing ID throws/returns a fabricated object.

**If It Fails:**  
Inspect `StudentDAO.findById()`, parameter index 1, `mapRow(ResultSet)`, the `Student(long, String, String, int)` constructor, and the exact database ID.

---

## Stage 8 — FIND ALL Students

### Test 8 — Multiple rows, deterministic order, correct mapping, and empty-list behavior

**Verification Type:** STATIC + RUNTIME

**Purpose:**  
Verify zero-to-many mapping, `ORDER BY id`, and the rule that no rows returns an empty `List`, never `null`.

**Preconditions:**  
JShell is open. For the multiple-row case, Stage 6 succeeded.

**Commands / Steps:**

1. Add two independent rows and inspect the returned list:

   ```java
   dao.create(new Student("Verification Bob", "verify.list.b." + run + "@example.test", 22));
   dao.create(new Student("Verification Carol", "verify.list.c." + run + "@example.test", 23));
   var allStudents = dao.findAll();
   System.out.println("list is null=" + (allStudents == null));
   allStudents.forEach(System.out::println);
   var ids = allStudents.stream().map(Student::getId).toList();
   var sortedIds = new ArrayList<Long>(ids);
   sortedIds.sort(Long::compareTo);
   System.out.println("ordered by ID=" + ids.equals(sortedIds));
   ```

2. Compare the list with PostgreSQL:

   ```powershell
   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT id, name, email, age FROM students ORDER BY id;"
   ```

3. Verify the empty-table case using one of these safe choices:

   - Best choice: run `dao.findAll().isEmpty()` before Test 6 when `SELECT COUNT(*)` confirms the table is already empty.
   - Otherwise use the destructive practice-only procedure below after recording any data you need.

> **⚠ DESTRUCTIVE — disposable practice data only:** The following `TRUNCATE` deletes **every student row**, not merely verification rows. Do not run it against data you care about. It also resets the identity counter.

   ```powershell
   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -v ON_ERROR_STOP=1 -c "TRUNCATE TABLE students RESTART IDENTITY;"
   ```

   Then in JShell:

   ```java
   var emptyStudents = dao.findAll();
   System.out.println("null=" + (emptyStudents == null));
   System.out.println("size=" + emptyStudents.size());
   ```

   Recreate fresh test rows before later stages if you used `TRUNCATE`.

**Example Input:**  
Two fresh timestamped emails; for the empty case, zero rows.

**Expected Java Result:**

- Multiple case: non-null list, all database rows mapped, IDs in ascending order.
- Empty case: non-null list with size 0.

**Expected Database Result:**  
Multiple case contains the inserted rows. Empty case contains zero student rows.

**Verification SQL:**

```sql
SELECT id, name, email, age FROM students ORDER BY id;
SELECT COUNT(*) AS student_count FROM students;
```

**PASS:**  
Java list matches the SQL rows and order; an empty table produces `[]`/size 0, never null.

**FAIL:**  
Rows are missing/duplicated/misordered, fields do not match, `findAll()` returns null, or it throws merely because the table is empty.

**If It Fails:**  
Inspect `StudentDAO.FIND_ALL_SQL`, its `ORDER BY id`, the `while (rs.next())` loop, list initialization, and `mapRow()`.

---

## Stage 9 — UPDATE Student

### Test 9 — Update an existing ID and reject a missing ID

**Verification Type:** STATIC + RUNTIME

**Purpose:**  
Verify parameter order, the primary-key `WHERE`, and affected-row interpretation: exactly one means true; zero means false.

**Preconditions:**  
JShell is open and the table exists. This test creates its own target, so it is safe after the optional empty-table test.

**Commands / Steps:**

```java
var updateTarget = new Student("Before Update", "verify.update.before." + run + "@example.test", 30);
long updateId = dao.create(updateTarget);

updateTarget.setName("After Update");
updateTarget.setEmail("verify.update.after." + run + "@example.test");
updateTarget.setAge(31);
boolean updatedExisting = dao.update(updateTarget);
System.out.println("existing updated=" + updatedExisting);

var missingUpdate = new Student(Long.MAX_VALUE, "Missing", "verify.update.missing." + run + "@example.test", 40);
boolean updatedMissing = dao.update(missingUpdate);
System.out.println("missing updated=" + updatedMissing);
```

**Example Input:**

- Existing: generated `updateId`, name `After Update`, age 31.
- Missing: ID `9223372036854775807`.

**Expected Java Result:**  
Existing update returns `true`; missing update returns `false` without an SQL exception.

**Expected Database Result:**  
Exactly the generated row has new fields. No row is created or changed for the missing ID.

**Verification SQL:**

```sql
SELECT id, name, email, age FROM students WHERE id = <UPDATE_ID>;
SELECT COUNT(*) FROM students WHERE id = 9223372036854775807;
```

PowerShell form for the unique updated email:

```powershell
docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
    -c "SELECT id, name, email, age FROM students WHERE email = 'verify.update.after.$($env:VERIFY_TAG)@example.test';"
```

**PASS:**  
Java returns true/false as required, only one intended row changes, and SQL shows the new values.

**FAIL:**  
Missing ID returns true, an existing ID returns false, values bind to wrong columns, or multiple rows change.

**If It Fails:**  
Inspect `StudentDAO.UPDATE_SQL`, `bindStudent()`, `statement.setLong(4, student.getId())`, and `executeUpdate() == 1`. Never remove the `WHERE id = ?` clause.

---

## Stage 10 — DELETE Student

### Test 10 — Delete one existing row, then delete the same missing row

**Verification Type:** STATIC + RUNTIME

**Purpose:**  
Verify an exact primary-key deletion and correct affected-row handling for the repeated/missing case.

**Preconditions:**  
JShell is open; table exists.

**Commands / Steps:**

```java
var deleteTarget = new Student("Delete Me", "verify.delete." + run + "@example.test", 25);
long deleteId = dao.create(deleteTarget);
boolean deletedFirst = dao.delete(deleteId);
boolean deletedAgain = dao.delete(deleteId);
System.out.println("first=" + deletedFirst + ", second=" + deletedAgain);
```

Review that the SQL contains its primary-key predicate:

```powershell
Select-String -LiteralPath '.\src\main\java\com\example\dao\StudentDAO.java' `
    -Pattern 'DELETE FROM students WHERE id = \?'
```

**Example Input:**  
The generated `deleteId` from this test.

**Expected Java Result:**  
First delete returns `true`; second delete returns `false`.

**Expected Database Result:**  
The row is absent after the first call and remains absent.

**Verification SQL:**

```sql
SELECT COUNT(*) FROM students WHERE id = <DELETE_ID>;
```

**PASS:**  
Results are `true` then `false`, and the database count is 0.

**FAIL:**  
Both calls return true, the first returns false for an existing row, the row remains, or unrelated rows disappear.

**If It Fails:**  
Inspect `StudentDAO.delete()`, parameter 1, `DELETE_SQL`, and the exact ID used. Deleting a missing row is not an SQL error; its affected count is zero.

---

## Stage 11 — Generated Keys

### Test 11 — JDBC generated-key protocol and ID equality

**Verification Type:** STATIC + RUNTIME

**Purpose:**  
Explicitly verify every required generated-key step, not merely that an INSERT happened.

**Preconditions:**  
JShell is open and CREATE can connect.

**Commands / Steps:**

1. Inspect the implementation:

   ```powershell
   Select-String -LiteralPath '.\src\main\java\com\example\dao\StudentDAO.java' `
       -Pattern 'RETURN_GENERATED_KEYS|getGeneratedKeys|keys\.next|keys\.getLong|setId'
   ```

2. Run an independent generated-key probe:

   ```java
   var keyStudent = new Student("Generated Key", "verify.key." + run + "@example.test", 26);
   long returnedKey = dao.create(keyStudent);
   var reloadedKeyStudent = dao.findById(returnedKey);
   System.out.println("positive=" + (returnedKey > 0));
   System.out.println("equals object=" + (returnedKey == keyStudent.getId()));
   System.out.println("reloaded=" + reloadedKeyStudent);
   ```

**Example Input:**  
Email `verify.key.<VERIFY_TAG>@example.test`.

**Expected Java Result:**  
All printed checks are true; `findById(returnedKey)` returns the same logical student.

**Expected Database Result:**  
One row exists with ID equal to both Java values.

**Verification SQL:**

```powershell
docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
    -c "SELECT id, name, email, age FROM students WHERE email = 'verify.key.$($env:VERIFY_TAG)@example.test';"
```

**PASS:**

- Statement is prepared with `Statement.RETURN_GENERATED_KEYS`.
- `getGeneratedKeys()` is called in try-with-resources.
- `keys.next()` is checked before reading.
- ID is positive, returned, assigned to the object, and finds the same row.

**FAIL:**  
ID stays 0, object/returned/database IDs differ, keys are read before `next()`, or INSERT succeeds but a fake/default ID is returned.

**If It Fails:**  
Inspect `StudentDAO.create()` and the identity definition of `students.id`. Do not assume identity values are gapless.

---

## Stage 12 — SQLException Handling

### Test 12 — Duplicate email, SQLState 23505, honest failure, and safe diagnostics

**Verification Type:** STATIC + RUNTIME NEGATIVE PATH

**Purpose:**  
Prove PostgreSQL enforces uniqueness, pgJDBC raises `SQLException`, the application classifies it by SQLState, no fake ID is returned, and only one row exists.

**Preconditions:**  
JShell is open; `students.email` has a unique constraint. Main can also be launched with the Test 5 connection configuration so its real application-boundary handling can be tested.

**Commands / Steps:**

```java
var duplicateEmail = "verify.duplicate." + run + "@example.test";
var duplicateFirst = new Student("Duplicate First", duplicateEmail, 20);
var duplicateSecond = new Student("Duplicate Second", duplicateEmail, 22);
long duplicateFirstId = dao.create(duplicateFirst);

try {
    dao.create(duplicateSecond);
    System.out.println("FAIL: second insert unexpectedly succeeded");
} catch (SQLException e) {
    for (SQLException current = e; current != null; current = current.getNextException()) {
        System.out.println("type=" + current.getClass().getName()
                + ", SQLState=" + current.getSQLState()
                + ", code=" + current.getErrorCode()
                + ", message=" + current.getMessage());
    }
}

System.out.println("first ID=" + duplicateFirstId);
System.out.println("rejected object ID=" + duplicateSecond.getId());
```

Then verify the **actual Main boundary**, not only the JShell harness. If Test 5's property fallback is still broken and this remains a diagnostic run, set the same correct `DB_URL`/`DB_USERNAME` and securely prompted `DB_PASSWORD` in PowerShell B before launching Main. In PowerShell B, create and print a second unique email:

```powershell
$env:CONSOLE_DUP_EMAIL = "verify.console.duplicate.$($env:VERIFY_TAG)@example.test"
$env:CONSOLE_DUP_EMAIL
mvn "-Duser.timezone=Asia/Ho_Chi_Minh" compile exec:java
```

In the menu, choose option 1 twice:

```text
First add:
  name  = Console Duplicate First
  email = value printed in $env:CONSOLE_DUP_EMAIL
  age   = 20

Second add:
  name  = Console Duplicate Second
  email = the exact same value
  age   = 22
```

After the second add, expect the database diagnostic to contain SQLState `23505` and expect the menu to appear again. Choose option 3 to prove the console is still usable, then option 0 to exit.

Do not print `DB_PASSWORD`, the full `Properties`, or a JDBC URL containing secrets.

**Example Input:**  
Two different names using exactly `verify.duplicate.<VERIFY_TAG>@example.test` for JShell, and a separate `verify.console.duplicate.<VERIFY_TAG>@example.test` pair through Main.

**Expected Java Result:**  
First JShell insert returns a positive ID. Second insert throws; at least one exception in the chain has SQLState `23505`. No success/positive ID exists for the rejected object; its ID remains 0. Through Main, the second add prints SQLState `23505`, then the menu continues accepting options.

**Expected Database Result:**  
Exactly one row exists for each duplicated email (one JShell survivor and one console survivor).

**Verification SQL:**

```powershell
docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
    -c "SELECT email, COUNT(*) AS row_count, MIN(id) AS surviving_id FROM students WHERE email IN ('verify.duplicate.$($env:VERIFY_TAG)@example.test', 'verify.console.duplicate.$($env:VERIFY_TAG)@example.test') GROUP BY email ORDER BY email;"
```

**PASS:**  
Both PostgreSQL grouped counts are 1, SQLState is `23505`, the JShell rejected object has no positive ID, Main's menu survives the expected error, and diagnostics are useful but secret-free.

**FAIL:**  
Either duplicate count becomes 2, an exception is swallowed, a false success/ID is produced, Main exits instead of redisplaying the menu, SQLState is not exposed at the application boundary, or password material appears.

**If It Fails:**  
Inspect the `students_email_key` constraint, `StudentDAO.create()` exception propagation, and `Main.printSqlException()`. Current `Main` contains chained diagnostic logic, but it cannot earn runtime PASS until the JDBC configuration blockers are resolved. Sequence gaps after this failure are normal.

---

## Stage 13 — Resource Handling Code Review

### Test 13 — JDBC ownership, closure, cursor discipline, and connection lifetime

**Verification Type:** STATIC

**Purpose:**  
Verify resource correctness that cannot be proven merely by seeing successful output.

**Preconditions:**  
Source files are available; no database process is required.

**Commands / Steps:**

Open these files side by side:

```powershell
Get-Content -LiteralPath '.\src\main\java\com\example\dao\StudentDAO.java'
Get-Content -LiteralPath '.\src\main\java\com\example\service\AccountTransferService.java'
Get-Content -LiteralPath '.\src\main\java\com\example\config\DatabaseConfig.java'
Get-Content -LiteralPath '.\src\main\java\com\example\Main.java'
```

Check each rule:

| Inspection item | Why it matters | Actual evidence to look for |
|---|---|---|
| `Connection` in try-with-resources | Closes sockets/sessions on success and failure; prevents connection leaks | Every DAO method that opens one owns it; transfer owns one connection |
| `PreparedStatement` in try-with-resources | Releases server/driver statement resources | Nested or same try-with-resources block |
| `ResultSet` in try-with-resources | Closes cursors promptly | find methods and generated keys own their result sets |
| `ResultSet.next()` checked | Cursor begins before its first row; reading early is invalid | ternary/loop/key check before getters |
| No JDBC resource escapes its method | Returning a live cursor/statement ties callers to a closing connection | DAO returns `Student`, `List`, booleans, IDs, or counts—not JDBC handles |
| No global/static `Connection` | Long-lived shared connections become stale and are unsafe across threads/transactions | only SQL strings/properties are static |
| One connection owns both transfer statements | Commit/rollback can cover debit and credit together | both statements created from the same local connection |
| Configuration `InputStream` closes | Classpath resource handle is released even when loading fails | try-with-resources in `loadProperties()` |
| Rollback failure is preserved | The original failure remains primary while cleanup failure is not lost | rollback failure added as suppressed |

Also search mechanically:

```powershell
Get-ChildItem -LiteralPath '.\src\main\java\com\example' -Recurse -Filter '*.java' |
    Select-String -Pattern 'try \(|Connection |PreparedStatement |ResultSet |\.next\(\)|static .*Connection|commit\(\)|rollback\(\)'
```

**Example Input:**  
No runtime input.

**Expected Java Result:**  
Not applicable — static inspection.

**Expected Database Result:**  
Not applicable; this test must not change database state.

**Verification SQL:**  
Not applicable.

**PASS:**  
Every checklist rule holds. In the inspected snapshot, the DAO and transfer service meet these resource-ownership rules; `DatabaseConfig` also closes its stream even though its resource name/key are wrong.

**FAIL:**  
Any owned JDBC resource lacks deterministic closure, a cursor escapes, getters run before `next()`, a global connection exists, or transfer statements use different connections.

**If It Fails:**  
Inspect the exact method that creates the resource. Ownership should be simple: the method that opens a resource closes it, except a transaction deliberately passes one shared connection through all work it owns.

---

## Stage 14 — Batch Insert Success

### Test 14 — One prepared batch commits all unique students

**Verification Type:** STATIC + RUNTIME TRANSACTION

**Purpose:**  
Verify one `addBatch()` per student, one `executeBatch()`, the returned `int[]`, successful commit, and all-or-nothing use of one connection.

**Preconditions:**  
JShell is open; `dao`, `run`, and a healthy database connection are available.

**Commands / Steps:**

1. Use the specification-level `List<Student>` contract:

   ```java
   List<Student> successfulBatch = List.of(
       new Student("Batch OK A", "verify.batch.ok.a." + run + "@example.test", 20),
       new Student("Batch OK B", "verify.batch.ok.b." + run + "@example.test", 21),
       new Student("Batch OK C", "verify.batch.ok.c." + run + "@example.test", 22)
   );

   int[] successfulCounts = dao.batchInsert(successfulBatch);
   System.out.println("length=" + successfulCounts.length);
   System.out.println("counts=" + Arrays.toString(successfulCounts));
   System.out.println("accepted counts=" + Arrays.stream(successfulCounts)
           .allMatch(count -> count == 1 || count == Statement.SUCCESS_NO_INFO));
   ```

2. Query all three unique emails:

   ```powershell
   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT id, name, email, age FROM students WHERE email IN ('verify.batch.ok.a.$($env:VERIFY_TAG)@example.test', 'verify.batch.ok.b.$($env:VERIFY_TAG)@example.test', 'verify.batch.ok.c.$($env:VERIFY_TAG)@example.test') ORDER BY email;"
   ```

3. Inspect the implementation for this sequence:

   ```text
   one Connection
       ↓ setAutoCommit(false)
   bind all three fields → addBatch() for every student
       ↓ executeBatch()
   success → commit()
   failure → rollback()
   ```

4. Confirm the public method accepts the interface type used above:

   ```powershell
   Select-String -LiteralPath '.\src\main\java\com\example\dao\StudentDAO.java' `
       -Pattern 'batchInsert\s*\(List<Student>'
   ```

   The latest inspected signature is `batchInsert(List<Student>)`, so it matches the specification and the current Main caller.

**Example Input:**  
Three unique emails under `verify.batch.ok.<letter>.<VERIFY_TAG>@example.test`.

**Expected Java Result:**  
The count array length is 3. Each successful entry is normally 1; JDBC also permits `Statement.SUCCESS_NO_INFO` (`-2`). The method returns only after commit.

**Expected Database Result:**  
Exactly three matching rows exist.

**Verification SQL:**

```sql
SELECT email, COUNT(*)
FROM students
WHERE email LIKE 'verify.batch.ok.%.<VERIFY_TAG>@example.test'
GROUP BY email
ORDER BY email;
```

**PASS:**  
All runtime evidence succeeds **and** the method accepts the specification's `List<Student>` contract.

**FAIL:**  
Array length differs, a count signals `EXECUTE_FAILED`, any row is missing/duplicated, commit is absent, or the required `List<Student>` call does not compile.

**If It Fails:**  
Inspect `StudentDAO.batchInsert()`, its public parameter type, per-iteration binding, `addBatch()`, `executeBatch()`, and commit placement. The latest inspected transaction body and `List<Student>` signature are structurally correct; runtime SQL evidence is still required.

---

## Stage 15 — Batch Insert Failure / Atomicity

### Test 15 — Duplicate inside a batch rolls back every row from that batch

**Verification Type:** RUNTIME NEGATIVE TRANSACTION

**Purpose:**  
Prove that partial driver execution does not become partial committed data when one batch entry violates the unique-email constraint.

**Preconditions:**  
Test 12 confirmed SQLState `23505`; JShell is open.

**Commands / Steps:**

1. Create one anchor row **before** starting the batch:

   ```java
   var batchDuplicateEmail = "verify.batch.anchor." + run + "@example.test";
   dao.create(new Student("Batch Anchor", batchDuplicateEmail, 30));
   ```

2. Put a new valid row, the duplicate anchor email, and another new valid row into one batch:

   ```java
   List<Student> failedBatch = List.of(
       new Student("Failed Batch A", "verify.batch.fail.a." + run + "@example.test", 31),
       new Student("Failed Batch Duplicate", batchDuplicateEmail, 32),
       new Student("Failed Batch C", "verify.batch.fail.c." + run + "@example.test", 33)
   );

   try {
       dao.batchInsert(failedBatch);
       System.out.println("FAIL: batch unexpectedly succeeded");
   } catch (SQLException e) {
       System.out.println("type=" + e.getClass().getName() + ", SQLState=" + e.getSQLState());
       if (e instanceof BatchUpdateException batchError) {
           System.out.println("driver partial counts=" + Arrays.toString(batchError.getUpdateCounts()));
       }
       for (SQLException current = e; current != null; current = current.getNextException()) {
           System.out.println("chain SQLState=" + current.getSQLState() + ", message=" + current.getMessage());
       }
   }
   ```

3. Treat PostgreSQL state—not the partial-count array—as authoritative:

   ```powershell
   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT email, COUNT(*) AS row_count FROM students WHERE email IN ('verify.batch.anchor.$($env:VERIFY_TAG)@example.test', 'verify.batch.fail.a.$($env:VERIFY_TAG)@example.test', 'verify.batch.fail.c.$($env:VERIFY_TAG)@example.test') GROUP BY email ORDER BY email;"
   ```

**Example Input:**  
Anchor email reused as the middle entry; two otherwise-valid new emails.

**Expected Java Result:**  
`SQLException`/`BatchUpdateException` is raised, normally with `23505` somewhere in its chain. No success message/count array is returned to the caller.

**Expected Database Result:**

- Anchor row count remains 1 because it was committed before the batch.
- Failed-batch A count is 0.
- Failed-batch C count is 0.

**Verification SQL:**

```sql
SELECT
    COUNT(*) FILTER (WHERE email = 'verify.batch.anchor.<VERIFY_TAG>@example.test') AS anchor_count,
    COUNT(*) FILTER (WHERE email = 'verify.batch.fail.a.<VERIFY_TAG>@example.test') AS failed_a_count,
    COUNT(*) FILTER (WHERE email = 'verify.batch.fail.c.<VERIFY_TAG>@example.test') AS failed_c_count
FROM students;
```

**PASS:**  
The result is `anchor_count=1`, `failed_a_count=0`, and `failed_c_count=0`. This proves rollback made the failed unit atomic even if the driver attempted an earlier entry.

**FAIL:**  
Either valid row from the failed batch remains committed, no exception is raised, or rollback is missing/swallowed.

**If It Fails:**  
Inspect `StudentDAO.batchInsert()`: `setAutoCommit(false)` must occur before execution, commit only after the whole batch, and every `SQLException`/runtime failure must call rollback on the same connection. Identity gaps are not evidence of partial commit because PostgreSQL sequences are not transactional.

---

## Stage 16 — Account Transfer Success

### Test 16 — Debit and credit commit together while preserving the total

**Verification Type:** RUNTIME TRANSACTION

**Purpose:**  
Verify exact decimal arithmetic, both affected rows, commit, and conservation of total funds.

**Preconditions:**  
`accounts` has two distinct valid IDs. JShell has `transfers`. No other process should change these balances during the test.

**Commands / Steps:**

1. Record actual IDs, balances, and total immediately before transfer:

   ```powershell
   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT id, owner_name, balance, SUM(balance) OVER () AS total_balance FROM accounts ORDER BY id;"
   ```

2. If the rows are still the inspected seed state, use Account A ID 1 and Account B ID 2. Otherwise substitute the actual two IDs. In JShell:

   ```java
   transfers.transfer(1L, 2L, new BigDecimal("100.00"));
   System.out.println("transfer returned normally; verify PostgreSQL now");
   ```

3. Immediately rerun the same SQL and compare before/after.

**Example Input:**

```text
fromId = 1 (Account A)
toId   = 2 (Account B)
amount = 100.00
```

For the original seed balances, the example transition is:

```text
Account A: 1000.00 → 900.00
Account B:  500.00 → 600.00
Total:     1500.00 → 1500.00
```

**Expected Java Result:**  
`transfer()` returns normally. Because it returns `void`, a Java message alone is not proof of commit.

**Expected Database Result:**  
Source decreases by exactly 100.00, destination increases by exactly 100.00, and total is byte-for-decimal-value unchanged.

**Verification SQL:**

```sql
SELECT id, owner_name, balance,
       SUM(balance) OVER () AS total_balance
FROM accounts
ORDER BY id;
```

**PASS:**  
Both deltas are exact opposites, total is unchanged, and no third account changes.

**FAIL:**  
Only one balance changes, either delta is wrong, floating-point artifacts appear, total changes, or the method reports success before both statements complete.

**If It Fails:**  
Inspect `AccountTransferService.transfer()`, use of one connection, `BigDecimal`/`setBigDecimal`, debit/credit parameter order, affected-row checks, and commit placement. The current service includes `findBalance(long)`, but direct SQL before/after remains the authoritative balance check.

---

## Stage 17 — Account Transfer Rollback

### Test 17 — Missing destination and insufficient funds leave every balance unchanged

**Verification Type:** RUNTIME NEGATIVE TRANSACTION

**Purpose:**  
Prove a debit that happens before a later failure is undone, and prove a failed safe debit cannot commit a credit.

**Preconditions:**  
Test 16 established valid source ID 1 and destination ID 2, or replace them with actual IDs. No concurrent balance changes.

**Commands / Steps:**

#### Case A — valid source, missing destination

1. Confirm `999999` is absent and record every balance/total:

   ```powershell
   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT EXISTS (SELECT 1 FROM accounts WHERE id = 999999) AS destination_exists; SELECT id, owner_name, balance, SUM(balance) OVER () AS total_balance FROM accounts ORDER BY id;"
   ```

   If `destination_exists` is `true`, choose another positive ID, prove it is absent with the same query, and substitute it in every command below. A rollback test with a real destination is invalid.

2. Run:

   ```java
   try {
       transfers.transfer(1L, 999999L, new BigDecimal("10.00"));
       System.out.println("FAIL: missing-destination transfer succeeded");
   } catch (SQLException e) {
       System.out.println("expected failure=" + e.getMessage() + ", SQLState=" + e.getSQLState());
   }
   ```

3. Query the balances again. The service-created zero-row exception may have a null SQLState; the required evidence is the exception plus unchanged state.

#### Case B — insufficient funds

1. Record the same before-state.
2. Attempt an amount greater than the source balance:

   ```java
   try {
       transfers.transfer(1L, 2L, new BigDecimal("999999999.00"));
       System.out.println("FAIL: insufficient-funds transfer succeeded");
   } catch (SQLException e) {
       System.out.println("expected failure=" + e.getMessage());
   }
   ```

3. Query balances and total again.

**Example Input:**

- Missing destination: `1 → 999999`, `10.00`.
- Insufficient funds: `1 → 2`, `999999999.00`.

**Expected Java Result:**  
Both calls throw `SQLException`; neither prints a commit/success result.

**Expected Database Result:**  
Every account balance and the total exactly equal their respective before-state after each case.

**Verification SQL:**

```sql
SELECT id, owner_name, balance,
       SUM(balance) OVER () AS total_balance
FROM accounts
ORDER BY id;
```

**PASS:**  
Missing destination can fail after debit execution yet source is restored; insufficient funds changes nothing; totals remain identical.

**FAIL:**  
Source stays debited, destination changes on a rejected transfer, total changes, exception is swallowed, or debit/credit use independent connections.

**If It Fails:**  
Inspect the `try/catch` transaction boundary, `connection.rollback()`, affected-row requirements, and the debit predicate `balance >= ?`. Both updates must be prepared from and executed on the same connection with auto-commit disabled.

---

## Stage 18 — Input Validation

### Test 18 — Separate Java validation from PostgreSQL constraint validation

**Verification Type:** STATIC + RUNTIME NEGATIVE PATH

**Purpose:**  
Verify that invalid input is rejected at the intended layer and that rejection leaves no unintended database changes.

**Preconditions:**  
JShell is open. Record account balances before transfer-validation cases.

**Commands / Steps:**

Use this matrix. `10.001` is intentionally over-precise; `10.000` may reduce exactly to two decimal places and is not a reliable rejection case.

| Case | Example | Expected rejecting layer | Expected exception/state |
|---|---|---|---|
| Nonpositive account ID | `0 → 2`, `1.00` | Java `validateTransfer` | `IllegalArgumentException` |
| Same source/destination | `1 → 1`, `1.00` | Java `validateTransfer` | `IllegalArgumentException` |
| Zero amount | `1 → 2`, `0` | Java `validateTransfer` | `IllegalArgumentException` |
| Negative amount | `1 → 2`, `-1.00` | Java `validateTransfer` | `IllegalArgumentException` |
| Too many decimals | `1 → 2`, `10.001` | Java `validateTransfer` | `IllegalArgumentException` |
| Positive but nonexistent destination | `1 → 999999`, `1.00` | SQL/service row-count logic | `SQLException`, rollback |
| Invalid age | student age `-1` | PostgreSQL check constraint | SQLState `23514` |
| Blank name through DAO | name `"   "` | PostgreSQL check constraint | SQLState `23514` |
| Null email through DAO | email `null` | PostgreSQL not-null constraint | SQLState `23502` |
| Duplicate email | same email twice | PostgreSQL unique constraint | SQLState `23505` |

Run the Java-validation cases, one at a time:

```java
try { transfers.transfer(0L, 2L, new BigDecimal("1.00")); System.out.println("FAIL"); }
catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }

try { transfers.transfer(1L, 1L, new BigDecimal("1.00")); System.out.println("FAIL"); }
catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }

try { transfers.transfer(1L, 2L, new BigDecimal("0")); System.out.println("FAIL"); }
catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }

try { transfers.transfer(1L, 2L, new BigDecimal("-1.00")); System.out.println("FAIL"); }
catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }

try { transfers.transfer(1L, 2L, new BigDecimal("10.001")); System.out.println("FAIL"); }
catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }
```

Run representative database-validation cases and print only safe diagnostics:

```java
try { dao.create(new Student("Invalid Age", "verify.invalid.age." + run + "@example.test", -1)); }
catch (SQLException e) { System.out.println("age SQLState=" + e.getSQLState()); }

try { dao.create(new Student("   ", "verify.invalid.name." + run + "@example.test", 20)); }
catch (SQLException e) { System.out.println("name SQLState=" + e.getSQLState()); }

try { dao.create(new Student("Null Email " + run, null, 20)); }
catch (SQLException e) { System.out.println("email SQLState=" + e.getSQLState()); }
```

The current `Student` model/DAO does not perform age/name/email domain validation. That is not the same as no validation: PostgreSQL remains the final integrity authority. The console's `readRequired()` rejects blank text interactively, but direct DAO calls still rely on constraints.

**Example Input:**  
The values in the matrix.

**Expected Java Result:**  
Transfer shape/amount errors produce `IllegalArgumentException` before a connection is opened. Invalid student rows produce the listed SQLStates. No case prints success.

**Expected Database Result:**  
Balances are unchanged. No row exists for invalid age/name/null-email inputs. The duplicate-email test retains only its first valid row.

**Verification SQL:**

```powershell
docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
    -c "SELECT email, COUNT(*) FROM students WHERE email IN ('verify.invalid.age.$($env:VERIFY_TAG)@example.test', 'verify.invalid.name.$($env:VERIFY_TAG)@example.test') GROUP BY email; SELECT COUNT(*) AS null_email_row_count FROM students WHERE name = 'Null Email $($env:VERIFY_TAG)'; SELECT id, owner_name, balance, SUM(balance) OVER () AS total_balance FROM accounts ORDER BY id;"
```

**PASS:**  
Each invalid value is rejected at the stated layer, no invalid row/balance change survives, and database SQLStates match constraint classes.

**FAIL:**  
Any invalid operation succeeds, a Java validation case reaches and mutates the database, a rejected transfer changes balances, or exception handling hides the useful classification.

**If It Fails:**  
For transfer-shape/amount cases inspect `AccountTransferService.validateTransfer()`. For student data inspect Java input handling, `StudentDAO.create()`, and the live table constraints. Do not remove a database constraint merely to make invalid input succeed.

---

## Stage 19 — Docker Persistence

### Test 19 — Data survives stop/start and down/up through the named volume

**Verification Type:** RUNTIME INFRASTRUCTURE + DATABASE

**Purpose:**  
Prove PostgreSQL data lives in the named volume rather than only in the running container.

**Preconditions:**  
The application can create a student, or the diagnostic DAO probe is available. No uncommitted work is in progress. Docker commands run from the project root.

**Commands / Steps:**

1. Reuse the shared verification tag so JShell window A and PowerShell window B address the same row. In PowerShell B:

   ```powershell
   $env:PERSIST_EMAIL = "verify.persistence.$($env:VERIFY_TAG)@example.test"
   $env:PERSIST_EMAIL
   ```

   In JShell A, create the exact marker and note its ID:

   ```java
   long persistenceId = dao.create(new Student(
       "Persistence Marker",
       "verify.persistence." + run + "@example.test",
       24
   ));
   System.out.println("persistence ID=" + persistenceId);
   ```

   Then prove the row exists in PowerShell B:

   ```powershell
   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT id, name, email, age FROM students WHERE email = '$env:PERSIST_EMAIL';"
   ```

   Exit JShell with `/exit` before stopping PostgreSQL so no Java database work remains active.

2. Test `stop`/`start`:

   ```powershell
   docker compose stop postgres
   docker compose ps --all
   docker compose start postgres

   for ($attempt = 1; $attempt -le 30; $attempt++) {
       docker compose exec -T postgres pg_isready -h 127.0.0.1 -p 5432 -U jdbc_app -d jdbc_practice
       if ($LASTEXITCODE -eq 0) { break }
       Start-Sleep -Seconds 1
   }
   if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL did not become ready after start.' }

   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT id, name, email, age FROM students WHERE email = '$env:PERSIST_EMAIL';"
   ```

   `stop` stops but retains the same container. `start` starts that existing container.

3. Test `down`/`up`:

   ```powershell
   docker compose down
   docker volume ls --filter 'name=jdbc-postgresql-practice_postgres_data'
   docker compose up -d

   for ($attempt = 1; $attempt -le 30; $attempt++) {
       docker compose exec -T postgres pg_isready -h 127.0.0.1 -p 5432 -U jdbc_app -d jdbc_practice
       if ($LASTEXITCODE -eq 0) { break }
       Start-Sleep -Seconds 1
   }
   if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL did not become ready after up.' }

   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
       -c "SELECT id, name, email, age FROM students WHERE email = '$env:PERSIST_EMAIL';"
   ```

   `down` removes the Compose container and default network, but without `-v` it retains the named volume. `up -d` creates a new container and remounts the same database files.

**Example Input:**  
Name `Persistence Marker`, email stored in `$env:PERSIST_EMAIL`, age `24`.

**Expected Java Result:**  
Initial create returns a positive ID. No Java process should be running while PostgreSQL is stopped. After each restart/recreation, Java can reconnect.

**Expected Database Result:**  
The same row, same ID, tables, constraints, and account balances survive both cycles.

**Verification SQL:**

```sql
SELECT id, name, email, age
FROM students
WHERE email = '<PERSIST_EMAIL>';
```

**PASS:**  
The marker row is identical after stop/start and after down/up, and the named volume remains after ordinary `down`.

**FAIL:**  
The row/table disappears, `start` cannot find an existing stopped container, `up` creates an empty database unexpectedly, or the volume is missing.

**If It Fails:**  
Inspect `compose.yaml` volume mapping, the Compose project name, `docker volume inspect jdbc-postgresql-practice_postgres_data`, and whether an accidental `-v` or different project directory/name was used. Logs saying “Skipping initialization” on an existing volume are expected.

---

## Stage 20 — Optional Destructive Reset

### Test 20 — Delete and deliberately recreate the disposable practice database

**Verification Type:** DESTRUCTIVE RUNTIME RESET

**Purpose:**  
Demonstrate the difference between ordinary `down` and `down -v`, and document exactly what must be rebuilt.

**Preconditions:**  
All valuable evidence/data has been saved. You have confirmed this is only the disposable practice project at `D:\DSA\JDBC\jdbc-postgresql-practice`.

> # ⚠ DESTRUCTIVE WARNING — PRACTICE PROJECT ONLY
>
> `docker compose down -v` deletes the named PostgreSQL volume `jdbc-postgresql-practice_postgres_data` and therefore **all databases, tables, constraints, sequences, students, accounts, and balances stored in it**. This is not undoable through Docker Compose. Never run this command against a project containing data you need.

**Commands / Steps:**

1. Resolve and inspect the exact target first:

   ```powershell
   docker compose config --volumes
   docker volume inspect jdbc-postgresql-practice_postgres_data
   ```

2. Only after confirming the target, remove this practice stack and its named volume:

   ```powershell
   docker compose down -v
   docker volume ls --filter 'name=jdbc-postgresql-practice_postgres_data'
   ```

3. Recreate PostgreSQL and wait for readiness:

   ```powershell
   docker compose up -d

   for ($attempt = 1; $attempt -le 30; $attempt++) {
       docker compose exec -T postgres pg_isready -h 127.0.0.1 -p 5432 -U jdbc_app -d jdbc_practice
       if ($LASTEXITCODE -eq 0) { break }
       Start-Sleep -Seconds 1
   }
   if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL did not become ready after reset.' }

   docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice -c "\dt"
   ```

   Compose/official-image initialization recreates the configured role `jdbc_app` and database `jdbc_practice` in the new empty volume. It does **not** recreate application tables because this project has no mounted initialization SQL.

4. Open `psql` and recreate the exact schema and seed rows from the original specification:

   ```powershell
   docker compose exec postgres psql -X -v ON_ERROR_STOP=1 -U jdbc_app -d jdbc_practice
   ```

   ```sql
   CREATE TABLE students (
       id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
       name VARCHAR(100) NOT NULL CHECK (btrim(name) <> ''),
       email VARCHAR(150) UNIQUE NOT NULL,
       age INTEGER NOT NULL CHECK (age BETWEEN 0 AND 150),
       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
   );

   CREATE TABLE accounts (
       id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
       owner_name VARCHAR(100) NOT NULL,
       balance NUMERIC(15, 2) NOT NULL CHECK (balance >= 0)
   );

   INSERT INTO accounts (owner_name, balance)
   VALUES
       ('Account A', 1000.00),
       ('Account B', 500.00);

   \q
   ```

5. Rerun Test 4 before any Java test.

**Example Input:**  
No Java input. The account seed values are exactly those above.

**Expected Java Result:**  
Java cannot use the tables between the reset and schema recreation; it would receive `42P01` for a missing table. It reconnects after infrastructure/schema are restored.

**Expected Database Result:**  
Old data is gone permanently. After recreation, both empty/new tables exist and only the two seed accounts are present.

**Verification SQL:**

```sql
SELECT COUNT(*) FROM students;
SELECT id, owner_name, balance FROM accounts ORDER BY id;
```

**PASS:**  
The old marker is gone, a new volume was created, schema recreation succeeds, students count is 0, and seed accounts are 1000.00/500.00.

**FAIL:**  
Old data unexpectedly remains (wrong volume/project was targeted), the wrong volume was deleted, role/database cannot initialize, or tables are assumed to exist without rerunning DDL.

**If It Fails:**  
Inspect the exact Compose project/volume name, service logs, current database/user, and the DDL error. Do not repeat destructive commands while the target is uncertain.

---

## Stage 21 — Final End-to-End Console Test

### Test 21 — Run every menu option and verify every mutation with SQL

**Verification Type:** END-TO-END RUNTIME

**Purpose:**  
Prove the required host Java console integrates configuration, DAO, service, pgJDBC, Docker networking, and PostgreSQL without relying on a component-only probe.

**Preconditions:**  
Test 1, the main-compilation/dependency-tree portions of Test 2, and Tests 3–5 pass; schema and seed accounts exist. Keep the unrelated Maven test/package failure recorded—it still makes the overall Maven area FAIL, but it does not prevent `exec:java`. No diagnostic environment overrides may hide property-file defects unless environment-based configuration is the behavior intentionally under test.

**Commands / Steps:**

First record whether the host's default timezone can start the configured application:

```powershell
mvn clean compile exec:java
```

On this inspected host, use the already-proven canonical timezone for the full menu run after recording the default `Asia/Saigon` compatibility result:

```powershell
mvn "-Duser.timezone=Asia/Ho_Chi_Minh" clean compile exec:java
```

The required menu is:

```text
1. Add student
2. Find student by ID
3. List students
4. Update student
5. Delete student
6. Batch insert sample students
7. Transfer money (transaction exercise)
0. Exit
```

Use this operation checklist. Replace `<ID>` with the generated ID printed by option 1 and use a fresh `<TAG>`.

| Menu | Example console input | Expected Java behavior | Required PostgreSQL evidence |
|---|---|---|---|
| 1 — Add | `Verification Menu`, `verify.menu.<TAG>@example.test`, `27` | `Created student with ID <ID>` | exact row exists; selected ID equals printed ID |
| 1 — Duplicate first row | `Duplicate Menu A`, `verify.menu.duplicate.<TAG>@example.test`, `28` | positive generated ID; menu returns | exact email count is 1 |
| 1 — Duplicate second row | `Duplicate Menu B`, same exact email, `29` | database error with SQLState `23505`; menu appears again | exact email count remains 1 |
| 2 — Find existing | `<ID>` | prints correct `Student` | selected row fields match output |
| 2 — Find missing | `9223372036854775807` | `Student not found.` | count for that ID is 0 |
| 3 — List | no additional input | every student printed in ID order, or `No students.` only when empty | `SELECT ... ORDER BY id` matches |
| 4 — Update existing | `<ID>`, new name/email/age | `Updated.` | exactly that row has new fields |
| 4 — Update missing | `9223372036854775807`, valid fields | `Student not found.` | no row changed/created |
| 5 — Delete existing | `<ID>` | `Deleted.` | count for ID becomes 0 |
| 5 — Delete again | same `<ID>` | `Student not found.` | count remains 0 |
| 6 — Batch | no additional input | `Batch completed for 4 statements.` | four new `Batch A`–`Batch D` rows with one timestamp suffix |
| 7 — Transfer success | `1`, `2`, `100.00` | before values, `Committed.`, correct after values | source −100, target +100, total unchanged |
| 7 — Transfer failure | `1`, `999999`, `10.00` | database error; menu remains usable | all balances equal before-state |
| 0 — Exit | `0` | clean return to PowerShell | no database change |

Run these queries after each related menu action rather than waiting until the end:

```sql
SELECT id, name, email, age
FROM students
ORDER BY id;

SELECT id, name, email, age
FROM students
WHERE id = <ID>;

SELECT email, COUNT(*)
FROM students
WHERE email = 'verify.menu.duplicate.<TAG>@example.test'
GROUP BY email;

SELECT id, owner_name, balance,
       SUM(balance) OVER () AS total_balance
FROM accounts
ORDER BY id;
```

For option 6, record a before count and compare after:

```sql
SELECT COUNT(*) AS batch_rows_before
FROM students
WHERE name IN ('Batch A', 'Batch B', 'Batch C', 'Batch D');
```

Run the same query after the option; the count should increase by exactly 4. Inspect the four newest matching IDs/emails as well.

**Example Input:**  
The values in the menu table. Verify account IDs 1 and 2 still exist before using them.

**Expected Java Result:**  
Connection succeeds, duplicate email displays SQLState `23505` without terminating the menu, every message matches the DAO/service result, and exit terminates normally.

**Expected Database Result:**  
Each CRUD mutation, four-row batch, successful transfer, and failed-transfer rollback match their prior stage criteria.

**Verification SQL:**  
Use the three queries above after every mutation. A final broad query is a summary, not a replacement for before/after evidence.

**PASS:**

- [ ] Connection succeeds before the menu.
- [ ] Add works and printed/generated ID matches PostgreSQL.
- [ ] Duplicate add reports SQLState `23505`, leaves one row, and the menu remains usable.
- [ ] Find works for existing and missing IDs.
- [ ] List order and mapping match PostgreSQL.
- [ ] Update returns the correct outcome and changes only the intended row.
- [ ] Delete returns true then false and removes only that row.
- [ ] Batch adds exactly four committed rows.
- [ ] Successful transfer preserves total.
- [ ] Failed transfer rolls back.
- [ ] Exit is clean.

**FAIL:**  
The build/main class cannot start, any option is absent, Java output disagrees with SQL, the app exits on an expected handled error, or transaction state is wrong.

**If It Fails:**  
Inspect `Main.java` for the failing menu action and boundary catch, then the delegated DAO/service method. In the latest snapshot main compilation succeeds, but the default run prints `Configuration error: Missing configuration: db.url (or environment variable DB_URL)` and then Maven reports `BUILD SUCCESS` because Main catches the exception and returns. That application output is a connection FAIL despite Maven's final line. Do not mark component JShell evidence as a substitute for this final integration test.

---

## Stage 22 — Code Quality Review

### Test 22 — Responsibilities, safe SQL, exception flow, and transaction ownership

**Verification Type:** STATIC ARCHITECTURE

**Purpose:**  
Verify the beginner project has clear boundaries and avoids common JDBC design errors.

**Preconditions:**  
All source files are available. Runtime tests should already have supplied behavioral evidence.

**Commands / Steps:**

Search where SQL and JDBC APIs occur:

```powershell
Get-ChildItem -LiteralPath '.\src\main\java\com\example' -Recurse -Filter '*.java' |
    Select-String -Pattern 'SELECT |INSERT |UPDATE |DELETE |DriverManager|prepareStatement|setAutoCommit|commit\(|rollback\('

Select-String -LiteralPath '.\src\main\java\com\example\Main.java' `
    -Pattern 'SELECT |INSERT |UPDATE |DELETE '
```

Review this responsibility table:

| Class | Required responsibility | PASS evidence |
|---|---|---|
| `Main` | Console input/output, menu control, boundary exception reporting | delegates; contains no SQL/JDBC transaction logic |
| `DatabaseConfig` | Load external configuration and create a new `Connection` | no business SQL; no printed secret; env/property resolution |
| `Student` | Plain model state and representation | no SQL, connection, console, or transaction logic |
| `StudentDAO` | Student SQL, binding, row mapping, CRUD, generated keys, batch | uses `PreparedStatement`; propagates `SQLException` |
| `AccountTransferService` | Validate transfer and own debit+credit transaction | one connection; explicit commit/rollback |

Also verify:

- Parameterized values use `?` and typed setters, not string concatenation.
- SQL exceptions are not caught and silently converted into fake success.
- `Main` catches/report exceptions at the application boundary and continues when appropriate.
- Batch and transfer do not call helper methods that open separate connections inside one transaction.
- Password values are never logged.

Minor current style note: `Main.java` imports `java.util.List` twice and imports `java.util.ArrayList` without using it. These imports do not break compilation or architecture, but removing redundant/unused imports would make the file cleaner. Do not confuse this style issue with a core JDBC failure.

**Example Input:**  
No runtime input.

**Expected Java Result:**  
Not applicable — static architecture review.

**Expected Database Result:**  
Not applicable; this test must not change database state.

**Verification SQL:**  
Not applicable.

**PASS:**  
Every class owns only its intended responsibility, SQL parameters are prepared safely, errors remain honest, and each atomic unit owns exactly one connection.

**FAIL:**  
SQL appears in `Main`, model knows about JDBC, exceptions disappear, a password is logged, global connections exist, or transaction statements use separate connections.

**If It Fails:**  
Inspect the class that contains misplaced work. In the latest snapshot the package/POM/API integration and class responsibilities pass static review: Main contains no SQL, the DAO owns student persistence, and the service owns the transaction. `DatabaseConfig` still has functional name/key/URL defects, but those are recorded under JDBC connection rather than falsely treating the package architecture as broken.

---

## Master Verification Checklist

Do not check an item merely because its source looks plausible. Check it only when the required evidence has been recorded.

### Static/code inspection

- [ ] Maven source/resource/test roots are correct.
- [ ] Package declarations match directories and imports.
- [ ] POM main class matches the actual fully qualified `Main` class.
- [ ] All required source files exist.
- [ ] `database.properties` and `target/` are ignored.
- [ ] No Java Docker service or unnecessary Java Dockerfile exists.
- [ ] JDBC URL, resource filename, and property keys are correct.
- [ ] `PreparedStatement` is used for parameterized SQL.
- [ ] `Connection`, `PreparedStatement`, and `ResultSet` are safely closed.
- [ ] `ResultSet.next()` is checked before reads.
- [ ] No unnecessary global/static connection is kept.
- [ ] Main handles input/output only.
- [ ] StudentDAO owns student SQL and mapping.
- [ ] AccountTransferService owns the transfer transaction.
- [ ] DatabaseConfig owns configuration/connection creation.
- [ ] Student remains only a model.
- [ ] Batch and transfer each use one shared connection for their transaction.
- [ ] SQLException is propagated/reported, not silently swallowed.

### Build and runtime

- [ ] Maven compiles successfully.
- [ ] `mvn test` succeeds.
- [ ] `mvn clean package` creates the expected JAR.
- [ ] pgJDBC exists on the runtime classpath.
- [ ] PostgreSQL container is running and ready.
- [ ] Correct host port is published.
- [ ] Named volume is mounted.
- [ ] Database/user and both schemas are correct.
- [ ] JDBC connection succeeds through `localhost:5432`.
- [ ] Wrong password and wrong port fail safely.
- [ ] CREATE works and database state confirms it.
- [ ] Generated key is positive and matches the object/database row.
- [ ] `findById` works for existing and missing IDs.
- [ ] `findAll` works for multiple and zero rows and orders by ID.
- [ ] UPDATE returns true/false correctly and changes only one row.
- [ ] DELETE returns true/false correctly and removes only one row.
- [ ] Duplicate email raises SQLState `23505` and leaves one row.
- [ ] Batch success commits all rows.
- [ ] Failed batch rolls back all batch rows.
- [ ] Successful transfer commits both changes.
- [ ] Failed missing-destination transfer rolls back the debit.
- [ ] Insufficient-funds transfer leaves balances unchanged.
- [ ] Total account balance is preserved.
- [ ] Java and PostgreSQL validation cases are distinguished and pass.
- [ ] Docker stop/start persistence works.
- [ ] Docker down/up named-volume persistence works.
- [ ] Every final console menu option passes with SQL evidence.
- [ ] Responsibilities are separated correctly.

## Evidence Worksheet

Fill this while running the guide. Link or paste only non-secret excerpts.

| Test | Date/time | Evidence recorded | Status (PASS/FAIL) | Notes / likely file |
|---|---|---|---|---|
| 1 — Structure | | | | |
| 2 — Maven | | | | |
| 3 — Docker | | | | |
| 4 — Schema | | | | |
| 5 — Connection | | | | |
| 6 — CREATE | | | | |
| 7 — findById | | | | |
| 8 — findAll | | | | |
| 9 — UPDATE | | | | |
| 10 — DELETE | | | | |
| 11 — Generated keys | | | | |
| 12 — SQLException | | | | |
| 13 — Resources | | | | |
| 14 — Batch success | | | | |
| 15 — Batch rollback | | | | |
| 16 — Transfer success | | | | |
| 17 — Transfer rollback | | | | |
| 18 — Validation | | | | |
| 19 — Persistence | | | | |
| 20 — Reset (optional) | | | | |
| 21 — Console | | | | |
| 22 — Architecture | | | | |

## Final Verification Summary

The required proof chain is:

```text
coherent source tree
        ↓
Maven compile + test + package
        ↓
pgJDBC resolved at runtime
        ↓
Java connection through localhost:5432
        ↓
CRUD results confirmed with SELECT
        ↓
generated ID confirmed in Java object and PostgreSQL
        ↓
constraint failures classified by SQLState
        ↓
batch failure leaves zero batch rows
        ↓
transfer failure restores all balances
        ↓
named-volume restarts preserve data
        ↓
complete console menu works end to end
```

Use these decision rules:

- Choose **PASS** only when every core static and runtime item has evidence.
- Choose **PARTIAL PASS** when the main application and core database behavior work, but one or more non-core requirements/edge cases remain incorrect.
- Choose **FAIL** when Maven/main integration, JDBC connection, CRUD, generated keys, atomic batch/transfer, or the final application cannot function.
- “Not yet tested” is not PASS. For the final area table, mark an unproven core area FAIL and explain that runtime evidence is missing.

## Final Verdict

### Possible statuses

**✅ PASS**  
The project satisfies the requirements of `JDBC_PostgreSQL_Mini_Project.md`.

**⚠ PARTIAL PASS**  
The main functionality works, but some requirements or edge cases are incorrect.

**❌ FAIL**  
One or more core JDBC requirements are not functioning correctly.

### Your completed verdict table

| Area | Status | Evidence | Fix Needed |
|---|---|---|---|
| Maven | PASS/FAIL | | |
| Docker | PASS/FAIL | | |
| Database schema | PASS/FAIL | | |
| JDBC connection | PASS/FAIL | | |
| CRUD | PASS/FAIL | | |
| Generated keys | PASS/FAIL | | |
| SQLException | PASS/FAIL | | |
| Batch | PASS/FAIL | | |
| Transaction | PASS/FAIL | | |
| Resource handling | PASS/FAIL | | |
| Architecture | PASS/FAIL | | |

### Current inspected-snapshot verdict

**❌ FAIL**

This verdict is based on definite build/configuration/integration blockers plus read-only infrastructure/schema checks. It does **not** pretend that CRUD or transaction runtime tests were executed through the broken application.

| Area | Status | Evidence | Fix Needed |
|---|---|---|---|
| Maven | FAIL | `mvn clean compile` succeeds, but `mvn test`/normal package fail because `MainTest.java` references absent JUnit API | Inspect `MainTest.java`/POM consistency |
| Docker | PASS | Compose parsed; `postgres` was Up; port and named volume were present; `pg_isready` accepted | None observed |
| Database schema | PASS | Both live table descriptions/constraints and seed accounts matched the specification | None observed |
| JDBC connection | FAIL | Static resource name, password key, and JDBC scheme are wrong; a correct override reached PostgreSQL but default `Asia/Saigon` was rejected (canonical runtime probe succeeded) | Inspect `DatabaseConfig.java`, both properties files, and host JVM timezone configuration |
| CRUD | FAIL | DAO looks plausible statically, but no conforming end-to-end runtime evidence exists | Clear build/config gates, then run Tests 6–10 with SQL |
| Generated keys | FAIL | Correct APIs appear statically; returned/object/database ID equality is unproven | Run Test 11 |
| SQLException | FAIL | Duplicate handling is unproven through the required application | Run Test 12 after connection/build gates |
| Batch | FAIL | `List<Student>` API and transaction code pass static review, but commit/rollback database behavior is not yet proven | Run Tests 14–15 |
| Transaction | FAIL | Core service and balance lookup look sound statically, but commit/rollback database state is unproven | Run Tests 16–17 |
| Resource handling | PASS | DAO/service JDBC resources use try-with-resources; cursors are checked; no global connection | None observed in these classes |
| Architecture | PASS | Main delegates, DAO owns student SQL, service owns transfer, config owns connections, and model is plain | None observed; configuration values remain a separate functional failure |

The project should be reevaluated from Test 1 after defects are corrected. The verdict changes only when new evidence—not intention or console wording—supports it.
