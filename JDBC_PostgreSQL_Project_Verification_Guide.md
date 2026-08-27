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
| Config/DAO/model/service packages | `com.example.config`, `com.example.dao`, `com.example.model`, `com.example.service` |
| Current `Main.java` declaration | `com.example.jdbc.Main` |
| Main class configured in `pom.xml` | `com.example.Main` |
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

The following observations were made on the current files and running services. Rerun every test yourself; this table is a starting point, not permanent evidence.

| Area | Observed status | Evidence |
|---|---|---|
| Docker Compose configuration | PASS | `docker compose config` parsed successfully |
| PostgreSQL process | PASS | Service `postgres` was Up and `pg_isready` reported accepting connections |
| Published port and volume | PASS | `5432:5432` and `jdbc-postgresql-practice_postgres_data` were present |
| Database schema | PASS | `students` and `accounts`, their constraints, and two seed accounts were queried |
| Current student data | Informational | `students` contained zero rows at inspection time |
| Maven main compilation | **FAIL** | Current `Main.java` package/imports do not match the other classes; later API mismatches also exist |
| Maven tests/package | **FAIL** | Main-source blockers occur first; after those, `MainTest.java` also imports JUnit 4 while the POM has no JUnit dependency |
| Property-file JDBC configuration | **FAIL by inspection** | Resource name, password property key, and pgJDBC URL prefix are inconsistent |
| DAO resource handling | PASS statically | JDBC resources use try-with-resources and result cursors are checked |
| CRUD/generated-key behavior | Not yet runtime-verified | Static DAO logic looks appropriate, but the current project cannot reach runtime |
| Batch | PARTIAL statically | Transaction logic looks appropriate; public parameter type differs from the specification and current `Main` call |
| Transfer | PARTIAL statically | Core transaction looks appropriate; current `Main` calls a missing `findBalance(long)` method |

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
   Get-ChildItem '.\src\main\java','\.\src\test\java' -Recurse -Filter '*.java' |
       Select-String -Pattern '^package\s+'
   ```

   If PowerShell rejects the second literal because of the leading slash, use this equivalent command:

   ```powershell
   Get-ChildItem '.\src\main\java','.\src\test\java' -Recurse -Filter '*.java' |
       Select-String -Pattern '^package\s+'
   ```

3. Compare these three facts together:

   ```powershell
   Select-String -LiteralPath '.\src\main\java\com\example\Main.java' -Pattern '^package|^import com\.example'
   Select-String -LiteralPath '.\pom.xml' -Pattern '<mainClass>'
   Select-String -Path '.\src\main\java\com\example\**\*.java' -Pattern '^package'
   ```

4. Confirm the private file is ignored and the example remains visible to Git:

   ```powershell
   git check-ignore -v -- 'src/main/resources/database.properties'
   git status --short -- 'src/main/resources/database.properties' 'src/main/resources/database.properties.example'
   ```

5. Confirm there is no Java container or Java `Dockerfile`:

   ```powershell
   Get-Content -LiteralPath '.\compose.yaml'
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
Inspect the mismatching source file, `pom.xml`, `.gitignore`, or `compose.yaml`. In the inspected snapshot, `src/main/java/com/example/Main.java` declares `com.example.jdbc.Main`, imports `com.example.jdbc.*`, while the other sources declare `com.example.*` and the POM names `com.example.Main`. This is a real compile blocker, not a harmless style difference.

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
Start with the **first** error, not the final summary. The current snapshot fails `mvn clean compile` in `Main.java`: its package/imports disagree with the project. After that mismatch is resolved, inspect two more integration points that static review exposed: `Main.batchInsert()` passes a `List<Student>` to `StudentDAO.batchInsert(ArrayList<Student>)`, and `Main.transferMoney()` calls a missing `AccountTransferService.findBalance(long)`. Finally, `MainTest.java` imports JUnit 4 even though `pom.xml` has no JUnit dependency; the original specification intentionally did not require a test framework, so do not add a dependency merely to preserve a meaningless generated `assertTrue(true)` test.

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
docker compose config
docker compose up -d
docker compose ps --all
docker compose port postgres 5432
docker compose logs --tail 50 postgres
docker compose exec -T postgres pg_isready -h 127.0.0.1 -p 5432 -U jdbc_app -d jdbc_practice
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
Tests 2–4 pass. Use a new PowerShell window for temporary environment variables so closing it restores your normal environment.

**Commands / Steps:**

1. Check whether environment overrides are active **without printing their values**:

   ```powershell
   'DB_URL set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_URL))
   'DB_USERNAME set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_USERNAME))
   'DB_PASSWORD set: ' + [bool](-not [string]::IsNullOrWhiteSpace($env:DB_PASSWORD))
   ```

2. Inspect non-secret configuration while masking the password:

   ```powershell
   Get-Content -LiteralPath '.\src\main\resources\database.properties' |
       ForEach-Object {
           if ($_ -match '^\s*db\.password\s*=') { 'db.password=<redacted>' } else { $_ }
       }

   Select-String -LiteralPath '.\src\main\java\com\example\config\DatabaseConfig.java' `
       -Pattern 'CONFIG_FILE|requiredValue|DriverManager|getResourceAsStream'
   ```

3. Verify these exact relationships:

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

4. With `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` absent, run the required console connection path:

   ```powershell
   mvn compile exec:java
   ```

   This specifically verifies the local properties fallback. Expected first line: `Connected successfully`.

5. After the good connection passes, test a wrong password in the disposable PowerShell window:

   ```powershell
   $env:DB_URL = 'jdbc:postgresql://localhost:5432/jdbc_practice'
   $env:DB_USERNAME = 'jdbc_app'
   $env:DB_PASSWORD = 'definitely-wrong-verification-password'
   mvn compile exec:java
   ```

   Expect authentication failure, normally SQLState `28P01`. Do not save this value to a file.

6. Test a closed/wrong host port:

   ```powershell
   $env:DB_URL = 'jdbc:postgresql://localhost:65432/jdbc_practice'
   mvn compile exec:java
   ```

   Expect a connection failure/refusal. It is commonly an SQLState in class `08`, but the exact state/message can vary by driver and operating system.

7. Close this temporary PowerShell window, then rerun the good configuration to prove recovery.

`psql` inside the container is not a substitute for this test. It proves PostgreSQL works internally; it does not prove the host-published TCP route, pgJDBC classpath, or Java configuration.

**Example Input:**  
Good URL: `jdbc:postgresql://localhost:5432/jdbc_practice`; username: `jdbc_app`; password: the matching local development value, never printed.

**Expected Java Result:**

- Good values: `Connected successfully`, followed by the menu.
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
- Wrong password and wrong port both fail, and restoring settings reconnects.
- Diagnostics show message, SQLState, and vendor code without showing a password.

**FAIL:**

- Compilation or main-class lookup fails before a connection attempt.
- `Missing configuration: db.url`/password appears despite a correct resource.
- `No suitable driver` appears.
- A wrong password or wrong port prints success.
- Secrets appear in output.

**If It Fails:**  
Inspect `DatabaseConfig.getConnection()`, `loadProperties()`, `requiredValue()`, both properties files, the POM runtime dependency, and the POM/Main fully qualified class name. The inspected snapshot has three independent configuration defects: it requests resource `database_properties` instead of `database.properties`, requests `db.passwird` instead of `db.password`, and uses `jdbc:postgres://` instead of pgJDBC's `jdbc:postgresql://`. Current Maven compilation also fails before this code can run. Environment overrides can isolate DAO behavior later, but bypassing the broken fallback does **not** make this test PASS.

---

## Non-Invasive Method Probe for Stages 6–18

The final console application remains the required end-to-end route. Some details—such as comparing the returned generated ID with `student.getId()` or deliberately constructing a failed batch—are easier to inspect directly. JShell can call the actual public classes without adding a dependency or source file.

Use this only after `mvn clean compile` passes:

```powershell
mvn clean compile
mvn -q dependency:build-classpath "-Dmdep.includeScope=runtime" "-Dmdep.outputFile=target/runtime-classpath.txt"

$driverClasspath = (Get-Content -Raw -LiteralPath '.\target\runtime-classpath.txt').Trim()
$runtimeClasspath = (Resolve-Path '.\target\classes').Path + [IO.Path]::PathSeparator + $driverClasspath
$env:VERIFY_TAG = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds().ToString()
jshell --class-path $runtimeClasspath
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

If you set all three `DB_*` environment variables before JShell to bypass the currently broken property loader, label the result **diagnostic isolation only**. Never award Stage 5 PASS from that workaround. To enter the real password without echoing it:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/jdbc_practice'
$env:DB_USERNAME = 'jdbc_app'
$securePassword = Read-Host 'POSTGRES_PASSWORD from compose.yaml' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
```

Exit JShell with `/exit`. Close the disposable PowerShell window afterward so those process-scoped overrides disappear.

### Diagnostic isolation when the current `Main.java` blocks Maven compilation

First record Test 2 as FAIL. If you still want to isolate the already-written DAO/service code, compile only those four components into generated `target` output. This does not edit source and does **not** turn Maven, configuration, architecture, or final-console status into PASS:

```powershell
New-Item -ItemType Directory -Force -Path '.\target\verification-classes' | Out-Null

javac --release 17 -d '.\target\verification-classes' `
    '.\src\main\java\com\example\config\DatabaseConfig.java' `
    '.\src\main\java\com\example\model\Student.java' `
    '.\src\main\java\com\example\dao\StudentDAO.java' `
    '.\src\main\java\com\example\service\AccountTransferService.java'

mvn -q dependency:build-classpath "-Dmdep.includeScope=runtime" "-Dmdep.outputFile=target/verification-classpath.txt"

$driverClasspath = (Get-Content -Raw -LiteralPath '.\target\verification-classpath.txt').Trim()
$runtimeClasspath = (Resolve-Path '.\target\verification-classes').Path + [IO.Path]::PathSeparator + $driverClasspath

$env:DB_URL = 'jdbc:postgresql://localhost:5432/jdbc_practice'
$env:DB_USERNAME = 'jdbc_app'
$securePassword = Read-Host 'POSTGRES_PASSWORD from compose.yaml' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
$env:VERIFY_TAG = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds().ToString()

jshell --class-path $runtimeClasspath
```

Use the same JShell imports shown above. These environment overrides intentionally bypass the broken properties path so you can answer a narrower question: “Does this DAO/service method behave correctly when given a valid connection?”

---

## Stage 6 — CREATE Student

### Test 6 — Insert one valid student and verify the generated object/database state

**Verification Type:** STATIC + RUNTIME

**Purpose:**  
Verify parameter binding, one affected row, a positive generated ID, mutation of the `Student` object's ID, and the actual inserted row.

**Preconditions:**  
Tests 2–5 pass, or the diagnostic-isolation procedure is active and clearly recorded as such. PostgreSQL is ready. JShell has `dao` and `run` from the probe setup.

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
JShell is open; `students.email` has a unique constraint.

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

Do not print `DB_PASSWORD`, the full `Properties`, or a JDBC URL containing secrets.

**Example Input:**  
Two different names using exactly `verify.duplicate.<VERIFY_TAG>@example.test`.

**Expected Java Result:**  
First insert returns a positive ID. Second insert throws; at least one exception in the chain has SQLState `23505`. No success message or returned positive ID exists for the second object; its ID remains 0.

**Expected Database Result:**  
Exactly one row exists with that email.

**Verification SQL:**

```powershell
docker compose exec -T postgres psql -X -U jdbc_app -d jdbc_practice `
    -c "SELECT email, COUNT(*) AS row_count, MIN(id) AS surviving_id FROM students WHERE email = 'verify.duplicate.$($env:VERIFY_TAG)@example.test' GROUP BY email;"
```

**PASS:**  
PostgreSQL count is 1, SQLState is `23505`, the rejected object has no positive ID, and diagnostics are useful but secret-free.

**FAIL:**  
Two rows exist, the exception is swallowed, a false success/ID is produced, SQLState is not exposed at the application boundary, or password material appears.

**If It Fails:**  
Inspect the `students_email_key` constraint, `StudentDAO.create()` exception propagation, and `Main.printSqlException()`. Current `Main` contains chained diagnostic logic, but it cannot earn runtime PASS until its package/API build blockers are resolved. Sequence gaps after this failure are normal.

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
Select-String -Path '.\src\main\java\com\example\**\*.java' `
    -Pattern 'try \(|Connection |PreparedStatement |ResultSet |\.next\(\)|static .*Connection|commit\(\)|rollback\(\)'
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
