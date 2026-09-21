# Flyway Database Migration Exercise

## Existing project

This exercise extends the latest checked-in Book Catalog snapshot:

```text
Transaction/book-catalog-api/
```

Do not create another Spring Boot application, switch to H2, replace PostgreSQL, or add business endpoints. The exercise changes **schema ownership and evolution**, not the REST design.

### Verified baseline

| Checked-in fact | Consequence |
|---|---|
| Java 21 and Spring Boot 4.1.1 | Keep both; use Boot 4's Flyway starter |
| Boot 4.1.1 manages Flyway 12.4.0 in the effective Maven model | Do not pin Flyway dependency versions |
| PostgreSQL 17 runs from `compose.yaml` on the configured local port | Learn against the real engine, not H2 |
| A named volume `postgres_data` persists database state | Rebuilding Java does not produce an empty database |
| `schema.sql` and `spring.sql.init.mode: always` currently create/evolve schema | Basic SQL initialization is the current schema owner |
| `books` has `version BIGINT NOT NULL DEFAULT 0` | V2 must preserve optimistic locking used by `@Version` |
| `transaction_audits` exists | V3 must preserve the transaction-learning entity/repository |
| Hibernate uses `ddl-auto: validate` | Keep Hibernate as validator, not schema mutator |
| HikariCP has maximum pool size 5 | Flyway uses the existing primary `DataSource`; no second pool is needed |
| No Flyway dependency, history table, or migration directory exists | This exercise introduces them |

The live Compose service was not running during inspection, so the actual local database and row count are intentionally treated as unknown. Never assume the named volume is empty.

### Real schema evolution used here

The workspace preserves the earlier JPA snapshot and the completed transaction snapshot, so the current schema's evolution is known:

```text
V1  original JPA-stage books table
V2  add version for @Version optimistic locking
V3  add transaction_audits for REQUIRED / REQUIRES_NEW labs
```

This is better than inventing history or forcing the entire current schema into one artificial V1.

## Objective

By the end, you will be able to:

- replace `schema.sql` ownership with ordered Flyway migrations;
- explain the Boot startup path through HikariCP, JDBC, and PostgreSQL;
- inspect and reason from `flyway_schema_history`;
- prove that versioned migrations run once;
- reproduce a checksum mismatch and a failed PostgreSQL migration safely;
- distinguish clean replay from deliberate baseline adoption;
- add a safe schema/data evolution without changing the Book API.

## What remains unchanged

- all Java source files;
- all entities, DTOs, repositories, services, and controllers;
- PostgreSQL and `compose.yaml`;
- existing Book and transaction-lab routes;
- Spring service `@Transactional` behavior;
- HikariCP as the primary `DataSource`;
- Hibernate `ddl-auto: validate`.

One inspected baseline detail matters during regression checks: the current `BookService.delete` is still an intentional checked-exception transaction lab—it flushes, throws, and rolls back. Therefore “endpoints still work” means Flyway preserves the behavior that exists before this exercise; this migration exercise does not silently repair that unrelated lab code.

Never read, print, or copy values from the tracked `.env`. Use your existing local credentials through process/IDE configuration.

## Learning contract

For every task:

1. Read **Objective** and **Concept**.
2. Write a prediction before changing or running anything.
3. Attempt **What to implement** from the incomplete **Starter code**.
4. Open **Hints** only after making a real attempt.
5. Use **How to verify** against logs and PostgreSQL.
6. Review **Common mistakes** and explain the result across every layer.

Every task follows this rhythm:

- **Objective**
- **Concept**
- **What to implement**
- **Starter code**
- **Hints**
- **How to verify**
- **Common mistakes**
- **Explanation**

The complete cumulative solution appears only after the stop barrier. Starter fragments deliberately contain TODOs and may represent disposable experiments.

Use Maven, application logs, pgAdmin or `psql`, Postman or `curl.exe`, and a debugger. Do not add JUnit, Testcontainers, Spring Security, message brokers, microservices, or deployment infrastructure.

## Safety boundary

The main path creates a new database named `book_catalog_flyway`; it does **not** delete or overwrite the existing `book_catalog` database.

Do not use either command as a routine reset:

```text
flyway clean
docker compose down -v
```

The second command deletes the Compose named volume and can destroy every database, schema, migration history, and row stored in that volume. It is not part of this exercise.

Before any administrative database operation, verify:

```sql
SELECT current_database(), current_schema(), current_user;
```

## Before and target architecture

### Before

```text
Spring Boot startup
    -> HikariDataSource
    -> basic SQL initializer
    -> schema.sql runs every startup
    -> Hibernate validates entities/schema
    -> application ready
```

### After

```text
Spring Boot startup
    -> HikariDataSource
    -> Flyway reads db/migration + flyway_schema_history
    -> pending SQL runs through JDBC against PostgreSQL
    -> schema change and history commit
    -> Hibernate validates entities/schema
    -> application ready

HTTP request later
    -> controller -> service proxy -> @Transactional business work
    -> no Flyway migration per request
```

## Exercise tasks — attempt before reading the solution

### Task 1 — Trace the current and target schema architecture

**Objective**

Identify every component involved before editing the project and assign one responsibility to each.

**Concept**

Flyway extends the stack you already know. It does not replace Boot, JDBC, HikariCP, PostgreSQL, JPA, or Hibernate.

**What to implement**

Inspect:

```text
pom.xml
src/main/resources/application.yaml
src/main/resources/schema.sql
Book.java
TransactionAudit.java
compose.yaml
```

Write two traces:

1. the current `schema.sql` startup path;
2. the target Flyway startup path.

For each of these, name its responsibility: Spring Boot, Flyway, `DataSource`, HikariCP, JDBC/pgJDBC, PostgreSQL, JPA entity, Hibernate.

**Starter code**

```text
Current:
Spring Boot -> TODO -> schema.sql -> TODO -> Hibernate -> ready

Target:
Spring Boot -> TODO -> Flyway -> TODO -> PostgreSQL -> TODO -> ready
```

Prediction: which component actually creates a PostgreSQL table, and which component merely validates that an entity can use it?

**Hints**

- `DataSource` provides connectivity; it does not version schema.
- HikariCP pools JDBC connections; it does not parse migration versions.
- PostgreSQL executes the DDL.
- `ddl-auto: validate` does not create missing tables.

**How to verify**

Make an inventory of the current tables/columns from `schema.sql` and compare it with both entities. If PostgreSQL is running, use read-only queries:

```sql
SELECT current_database(), current_schema(), current_user;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

**Common mistakes**

- Saying “Flyway creates connections.”
- Saying “Hibernate owns the schema” while `ddl-auto` is `validate`.
- Assuming source `schema.sql` proves the named volume has exactly that schema.
- Treating application service transactions as startup migration transactions.

**Explanation**

The target responsibility chain is:

```text
Boot configures
 -> Hikari-backed DataSource supplies a JDBC connection
 -> Flyway chooses migration SQL
 -> pgJDBC sends it
 -> PostgreSQL changes durable schema
 -> Flyway records history
 -> Hibernate validates the resulting schema
```

### Task 2 — Add the correct Boot 4 Flyway dependencies

**Objective**

Put Flyway and PostgreSQL database support on the classpath without manually choosing incompatible versions.

**Concept**

Spring Boot 4 modularized Flyway integration. The Boot starter brings Boot's Flyway integration and Flyway core; modern Flyway keeps PostgreSQL support in a database-specific module.

**What to implement**

Add exactly two versionless dependencies:

- `spring-boot-starter-flyway`;
- `flyway-database-postgresql`.

Keep the existing PostgreSQL JDBC driver.

Prediction: what failure would be likely if Flyway core were present but its PostgreSQL database module were absent?

**Starter code**

```xml
<!-- TODO: Boot 4 Flyway integration -->
<dependency>
    <groupId>...</groupId>
    <artifactId>...</artifactId>
</dependency>

<!-- TODO: Flyway support for PostgreSQL -->
<dependency>
    <groupId>...</groupId>
    <artifactId>...</artifactId>
</dependency>
```

**Hints**

- Use the same Boot parent already in `pom.xml`.
- Do not add `<version>` to either dependency.
- The pgJDBC driver and Flyway PostgreSQL module have different jobs; keep both.

**How to verify**

From `Transaction/book-catalog-api`:

```powershell
mvn dependency:tree "-Dincludes=org.flywaydb:*"
mvn help:evaluate "-Dexpression=flyway.version" "-DforceStdout" -q
```

Expect Flyway core and `flyway-database-postgresql`; the inspected Boot 4.1.1 model resolves Flyway 12.4.0.

**Common mistakes**

- Copying a Boot 3 snippet without checking Boot 4 modularization.
- Adding only `flyway-database-postgresql` and expecting Boot integration to appear.
- Removing the PostgreSQL JDBC driver.
- hard-coding a Flyway version despite Boot dependency management.

**Explanation**

Maven supplies classes; it does not run migrations by itself. At runtime, Boot sees Flyway support plus the `DataSource` and configures startup migration.

### Task 3 — Create the migration location and learn naming

**Objective**

Create the conventional classpath location from which Flyway resolves SQL migrations.

**Concept**

Boot/Flyway's default migration location is:

```text
src/main/resources/db/migration/
```

Versioned names use `V<version>__<description>.sql`; the separator is two underscores.

**What to implement**

Create the directory only. Then write down the valid names you expect to add in Task 4.

Prediction: which of these is valid and why?

```text
V1_create_books.sql
V1__create_books.sql
v1__create_books.sql
Vone__create_books.sql
```

**Starter code**

```text
src/main/resources/
├── application.yaml
└── db/
    └── migration/
        └── TODO
```

**Hints**

- Use an uppercase `V`.
- Use a numeric version.
- Use two underscores before the description.
- Do not put versioned migrations in a package beside Java code.
- Task 7 enables `validate-migration-naming`, so a malformed resource fails fast instead of being silently ignored.

**How to verify**

Use `rg --files src/main/resources` or your IDE tree. Do not start the application yet; ownership still belongs to `schema.sql` until Tasks 4–7 are complete.

**Common mistakes**

- One underscore instead of two.
- Putting migrations under `src/main/java`.
- Using duplicate versions.
- Starting against a valued database before choosing a safety strategy.

**Explanation**

`src/main/resources` is copied to the application classpath. Flyway scans `classpath:db/migration` by default and parses each filename into type, version, and description.

### Task 4 — Convert real schema evolution into V1, V2, and V3

**Objective**

Replace the current end-state snapshot with a reproducible evolution that matches the Book Catalog's actual learning history.

**Concept**

A fresh database need not match every entity after V1 alone. It must match after Flyway finishes every pending migration and before Hibernate validates.

**What to implement**

Create:

```text
V1__create_books.sql
V2__add_optimistic_lock_version.sql
V3__create_transaction_audits.sql
```

Requirements:

- V1 creates the original `books` columns except `version`.
- V2 adds `version BIGINT NOT NULL DEFAULT 0`.
- V3 creates `transaction_audits` matching its entity.
- Preserve the current nullable ISBN, unique constraint, numeric precision, and audit timestamp type.
- Do not add demo rows.
- Do not use `IF NOT EXISTS`; Flyway history supplies once-only execution and strict DDL exposes drift.

Prediction: why will Hibernate validation still succeed even though V1 alone lacks `books.version` and `transaction_audits`?

**Starter code**

```sql
-- V1__create_books.sql
CREATE TABLE books (
    id TODO,
    isbn TODO,
    title TODO,
    author TODO,
    price TODO,
    stock TODO
);
```

```sql
-- V2__add_optimistic_lock_version.sql
ALTER TABLE books
ADD COLUMN TODO;
```

```sql
-- V3__create_transaction_audits.sql
CREATE TABLE transaction_audits (
    TODO
);
```

**Hints**

- The authoritative current types are in `schema.sql`.
- `Instant` maps to PostgreSQL `TIMESTAMP WITH TIME ZONE` in this project.
- `@Version Long` needs the `BIGINT` version column before Hibernate starts.
- All three pending migrations run before Hibernate creates the EntityManagerFactory.

**How to verify**

Review the files side by side with `schema.sql`, `Book.java`, and `TransactionAudit.java`. Build a column checklist; execution comes after the safety/configuration tasks.

**Common mistakes**

- Putting the current end state entirely in V1 and losing the known evolution lesson.
- Adding the version column both in V1 and V2.
- Omitting the audit table because it is not part of the main `/books` route.
- Changing ISBN nullability or lengths as an unrelated cleanup.
- Keeping `IF NOT EXISTS`, which can hide unexpected objects.

**Explanation**

The final state after V3 equals the current schema expected by both entities:

```text
empty -> V1 books -> V2 books.version -> V3 transaction_audits
                                      -> Hibernate validate
```

### Task 5 — Choose a safe learning database strategy

**Objective**

Practice on a clean database without deleting the existing Book Catalog database or Docker volume.

**Concept**

An empty schema can replay history from V1. A non-empty schema with no Flyway history needs deliberate adoption/baseline logic. Mixing those paths produces confusing failures.

**What to implement**

Use the main beginner path:

```text
book_catalog             preserve as-is
book_catalog_flyway      create as an empty practice database
```

Create the second database through pgAdmin or a verified PostgreSQL administrative session. Update only the application target (Task 7/reference configuration) to use it.

Prediction: does `docker compose down` remove the named volume? What would `docker compose down -v` destroy?

**Starter code**

First verify the server and intended user. A typical local command, if the configured role is `book_app`, is:

```powershell
docker compose up -d postgres
docker compose exec postgres psql -U book_app -d postgres -c "CREATE DATABASE book_catalog_flyway OWNER book_app;"
```

If your administrative role differs, use pgAdmin or your own verified role. Do not paste a password into source or command history.

**Hints**

- Creating a new named database loses no existing Book Catalog rows.
- The Compose `.env` is for Compose; Spring does not automatically read it.
- Task 16 covers baseline adoption on a separate disposable copy.

**How to verify**

Connect specifically to `book_catalog_flyway` and run:

```sql
SELECT current_database(), current_schema(), current_user;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public';
```

Before first migration, no application tables should exist.

If `book_catalog_flyway` already exists, stop and inspect it. Resetting it would lose **every table, row, view, index, and Flyway history entry in that database**. Do not drop it until you have explicitly confirmed it is disposable; never drop the original `book_catalog` as part of this lab.

**Common mistakes**

- Pointing the app at `book_catalog` by habit.
- Deleting the whole named volume to obtain one empty database.
- Assuming an existing same-named database is disposable.
- Printing `.env` contents.

**Explanation**

A new database is the cleanest experiment: it preserves old data while proving that migrations alone can reconstruct the required schema.

### Task 6 — Disable competing `schema.sql` initialization

**Objective**

Make Flyway the only mechanism that changes application schema during startup.

**Concept**

`schema.sql` plus Flyway creates two schema owners. A Flyway V1 can no longer prove it created a table if basic SQL initialization created it first.

**What to implement**

Only after V1–V3 contain all current schema logic:

1. set `spring.sql.init.mode` to `never`;
2. remove `src/main/resources/schema.sql` from the runtime source tree;
3. do not hand-edit `target/classes/schema.sql`—`target` is generated output.

Prediction: if a stale copied `target/classes/schema.sql` exists, why does `mode: never` still matter?

**Starter code**

```yaml
spring:
  sql:
    init:
      mode: TODO
```

**Hints**

- The SQL is not lost; Tasks 4's migrations preserve it as ordered history.
- Basic SQL initialization and Flyway are separate Boot mechanisms.
- A clean Maven build normally recreates `target`, but this repository happens to track generated output; treat source files as authoritative.

**How to verify**

Search runtime source:

```powershell
rg -n "CREATE TABLE|ADD COLUMN" src/main/resources
```

Expected schema DDL should now be in `db/migration`, not active `schema.sql`.

**Common mistakes**

- Deleting `schema.sql` before copying every table and column.
- Leaving `mode: always` and hoping ordering works.
- Keeping two files that both create `books`.
- Editing generated `target` as if it were source.

**Explanation**

One owner makes state explainable:

```text
Flyway history says V2 ran
    -> V2 is why books.version exists
```

### Task 7 — Keep Hibernate as validator and configure Flyway safely

**Objective**

Make the ownership split explicit: Flyway migrates; Hibernate validates.

**Concept**

Flyway validation checks files versus history. Hibernate validation checks mappings versus actual schema. Both are useful and different.

**What to implement**

Keep:

```yaml
spring.jpa.hibernate.ddl-auto: validate
```

Add focused Flyway settings and point the application at `book_catalog_flyway`, preferably using an environment-overridable URL. Keep automatic baseline off and clean disabled.

Prediction: which system should report the error if V2 is recorded successfully but the `version` column is manually removed afterward?

**Starter code**

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://127.0.0.1:5433/TODO}

  flyway:
    enabled: true
    locations: TODO
    validate-on-migrate: true
    validate-migration-naming: true
    baseline-on-migrate: false
    out-of-order: false
    clean-disabled: true

  jpa:
    hibernate:
      ddl-auto: TODO
```

**Hints**

- Location: `classpath:db/migration`.
- Database default: `book_catalog_flyway`.
- Do not duplicate URL/user/password under `spring.flyway`; the primary `DataSource` is intended.
- `ddl-auto: update` would create a competing schema owner.
- `validate-migration-naming: true` reports a name such as `V1_create.sql` rather than ignoring it.
- `out-of-order: false` prevents a late lower version from becoming a casual substitute for correct team coordination.

**How to verify**

Read the final YAML as an ownership statement:

```text
SQL initializer: off
Flyway: on, validate history/names, ordered, no auto-baseline, no clean
Hibernate: validate only
```

**Common mistakes**

- Switching Hibernate to `update` to make an error disappear.
- Enabling `baseline-on-migrate` before understanding the target database.
- Adding a second set of Flyway database credentials unnecessarily.
- Changing the original database by leaving the old URL in effect.

**Explanation**

Boot uses the same primary Hikari-backed `DataSource` for Flyway, then arranges JPA initialization after database initialization. The two validators cover different contracts.

### Task 8 — Run V1–V3 on the clean database

**Objective**

Observe the full successful startup migration path.

**Concept**

On an empty database, Flyway creates its history table, executes all pending versions in order, then Hibernate validates the final state.

**What to implement**

Start PostgreSQL and the application. Before running, predict:

- how many versioned migrations execute;
- when `flyway_schema_history` appears;
- whether Hibernate can validate after V1 but before V2/V3;
- when the Hikari connection returns to the pool.

**Starter code**

```powershell
cd Transaction/book-catalog-api
docker compose up -d postgres
mvn spring-boot:run
```

Use your existing safe local credential mechanism; do not commit or paste a real password into the guide.

**Hints**

- Flyway completes all pending migrations before Hibernate validation.
- Expected order: V1, V2, V3.
- An application that reaches “started” has passed both Flyway work and Hibernate validation.

**How to verify**

In `book_catalog_flyway`:

```sql
SELECT installed_rank, version, description, type, script, checksum, success
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'books'
ORDER BY ordinal_position;
```

Verify `books`, `books.version`, and `transaction_audits`. Create a Book through the existing API and run a safe transaction-lab scenario if desired.

**Common mistakes**

- Querying the original `book_catalog` database.
- Expecting Hibernate SQL logs to show Flyway DDL in exactly the same logger.
- Assuming V1 alone must satisfy the final entities.
- Hard-coding a checksum value in notes.

**Explanation**

```text
V1 SQL + history insert -> COMMIT both
V2 SQL + history insert -> COMMIT both
V3 SQL + history insert -> COMMIT both
Hibernate validate -> success
web application -> ready
```

Earlier versions are durable before later versions run; Flyway does not normally wrap every pending version in one giant transaction.

### Task 9 — Add V4 as a useful index migration

**Objective**

Apply one small schema evolution that requires no entity or REST change.

**Concept**

An index is schema. A new migration records when and how it was introduced.

**What to implement**

Create:

```text
V4__add_books_author_index.sql
```

Add a simple index on `books(author)` for the existing author filter. Restart the application.

Prediction: which versions will Flyway skip, and which one will execute?

**Starter code**

```sql
CREATE INDEX TODO
ON books (TODO);
```

**Hints**

- Use a stable explicit name such as `idx_books_author`.
- Do not edit V1–V3.
- An index existing does not guarantee PostgreSQL will use it for a tiny table.

**How to verify**

```sql
SELECT installed_rank, version, description, script, checksum, success
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'books'
ORDER BY indexname;
```

Expect one new V4 history row and `idx_books_author`.

**Common mistakes**

- Editing V1 to include the index.
- Reusing an existing version number.
- Using `CREATE INDEX IF NOT EXISTS` to hide drift.
- Claiming the index makes every query faster.

**Explanation**

The entity maps rows, not indexes. Flyway changes PostgreSQL schema; Hibernate and the JSON response remain unchanged.

### Task 10 — Prove old migrations do not rerun

**Objective**

Demonstrate that restart is not equivalent to replaying every SQL file.

**Concept**

Flyway compares resolved files with `flyway_schema_history`. Applied versioned migrations are skipped; only pending versions run.

**What to implement**

Stop and restart the application without changing any migration.

Before starting, predict:

- Will V1's `CREATE TABLE books` execute again?
- Will V4's `CREATE INDEX` execute again?
- Will a new history row appear?

**Starter code**

```powershell
# Stop the running application cleanly, then:
mvn spring-boot:run
```

**Hints**

- The absence of `IF NOT EXISTS` is intentional.
- Once-only behavior comes from history, not from SQL that silently ignores existing objects.

**How to verify**

Compare history row count before and after restart:

```sql
SELECT COUNT(*) AS history_entries
FROM flyway_schema_history;

SELECT version, script, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

Flyway should report the schema as up to date; installed timestamps should not change.

**Common mistakes**

- Expecting a second row for every startup.
- Adding `IF NOT EXISTS` because strict SQL would fail if replayed.
- Confusing repeatable migration behavior with versioned migration behavior.

**Explanation**

```text
resolved V1–V4
  versus
history V1–V4
  -> zero pending versioned migrations
  -> no migration SQL execution
```

### Task 11 — Trigger a checksum mismatch safely

**Objective**

See why applied versioned migrations become immutable shared history.

**Concept**

Flyway stores V4's checksum. Changing V4 content makes the current file disagree with what built the database.

**What to implement**

In this disposable local environment only:

1. make an exact copy of V4's original text outside the migration directory or in your notes;
2. change V4 from an index on `(author)` to `(author, id)`;
3. restart and observe validation failure;
4. restore V4 **exactly** to its original content;
5. restart and confirm validation succeeds.

Prediction: will PostgreSQL replace the existing index with the edited definition before Flyway notices the mismatch?

**Starter code**

```sql
-- Temporary WRONG edit to already-applied V4:
CREATE INDEX idx_books_author
ON books (author, id);
```

**Hints**

- Do not change whitespace while trying to restore from memory; restore the exact file.
- Do not run `repair` to make the red message disappear.
- `validate-on-migrate` runs before pending changes.

**How to verify**

Observe a checksum validation error and failed application startup. Confirm the actual index is still the original one:

```sql
SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'books'
  AND indexname = 'idx_books_author';
```

After restoring V4, startup should succeed with no new history row.

**Common mistakes**

- Using `repair` immediately.
- Assuming the edited SQL ran because it exists on disk.
- leaving V4 modified before Task 12.
- Changing the stored history row manually.

**Explanation**

Flyway rejects the contradiction before executing the edited V4. The checksum is evidence that the file and database no longer share one history.

### Task 12 — Make the desired correction with V5

**Objective**

Apply the composite index design as a forward migration rather than rewriting V4.

**Concept**

Already-committed schema history is corrected by a new version.

**What to implement**

With V4 restored, create:

```text
V5__replace_author_index_for_paging.sql
```

V5 should:

1. drop the exact V4 index;
2. create `idx_books_author_id` on `(author, id)`.

Prediction: what remains in history after V5—does V4 disappear?

**Starter code**

```sql
DROP INDEX TODO;

CREATE INDEX TODO
ON books (TODO, TODO);
```

**Hints**

- Use the exact V4 index name.
- Do not use `IF EXISTS` to conceal a missing prerequisite.
- V4 remains true history even though V5 replaces its database object.

**How to verify**

Restart, then query:

```sql
SELECT version, description, script, success
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'books'
ORDER BY indexname;
```

Expect both V4 and V5 history rows, no `idx_books_author`, and one `idx_books_author_id`.

**Common mistakes**

- Deleting V4 after V5 works.
- Renaming V4 to V5.
- Keeping both redundant indexes without deciding why.
- Assuming a composite index is automatically selected on a tiny dataset.

**Explanation**

History records evolution, not just final DDL text:

```text
V4 introduced author index
V5 replaced it with author + id index
```

That same sequence can reproduce the outcome everywhere.

### Task 13 — Observe a failed PostgreSQL migration

**Objective**

Prove that statement execution, migration success, history recording, and application readiness are separate moments.

**Concept**

PostgreSQL can roll back ordinary transactional DDL in the failing migration. Earlier migration transactions stay committed, and later versions do not run.

**What to implement**

Create two **temporary local-only** files:

```text
V999__broken_local_experiment.sql
V1000__must_not_run.sql
```

V999 should successfully add a temporary column and then run an invalid `ALTER TABLE`. V1000 should create a clearly named temporary table. Restart.

Predict before running:

- Does application startup finish?
- Does the first statement in V999 remain visible?
- Is V999 recorded as successful?
- Does V1000 run?
- Do V1–V5 remain applied?

**Starter code**

```sql
-- V999__broken_local_experiment.sql
ALTER TABLE books ADD COLUMN failed_demo INTEGER;

ALTER TABLE books
ALTER COLUMN column_that_does_not_exist SET NOT NULL;
```

```sql
-- V1000__must_not_run.sql
CREATE TABLE migration_should_not_exist (
    id INTEGER PRIMARY KEY
);
```

**Hints**

- Ordinary `ALTER TABLE` is suitable for this PostgreSQL transactional-DDL lab.
- Do not use `CREATE INDEX CONCURRENTLY`, `CREATE DATABASE`, or another statement with special transaction rules.
- Flyway normally runs migrations separately; V1–V5 were already committed.

**How to verify**

After failed startup:

```sql
SELECT version, script, success
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT column_name
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'books'
  AND column_name = 'failed_demo';

SELECT to_regclass('public.migration_should_not_exist');
```

On PostgreSQL, expect no `failed_demo`, no later table, and normally no durable V999 history row because the failing migration and its history work roll back. V1–V5 remain.

Then remove both disposable files. They never succeeded or became shared history. If V999 represented a real desired change, correct it before its first successful application; if a bad migration had already committed elsewhere, use a new forward version instead.

**Common mistakes**

- Claiming every PostgreSQL operation is transactional.
- Expecting V1–V5 to roll back with V999.
- Running `repair` even though PostgreSQL left no failed history row.
- Leaving V999/V1000 in the final project.
- Editing an already successful migration and calling it the same experiment.

**Explanation**

```text
V1–V5: already committed

V999 transaction:
  add failed_demo       SQL executes inside transaction
  invalid ALTER         error
  ROLLBACK              failed_demo disappears

V1000: never attempted
application: not ready
```

Execution is not commit. This is the same database principle learned in Spring Transactions, but Flyway owns this startup transaction rather than a service proxy.

### Task 14 — Read `flyway_schema_history`

**Objective**

Use Flyway's ledger to reconstruct what happened without guessing from filenames alone.

**Concept**

Flyway combines resolved migrations with recorded history to classify applied and pending work.

**What to implement**

Query every important field and explain it in one sentence:

- `installed_rank`;
- `version`;
- `description`;
- `type`;
- `script`;
- `checksum`;
- `installed_by`;
- `installed_on`;
- `execution_time`;
- `success`.

Prediction: is `installed_rank` the same concept as version?

**Starter code**

```sql
SELECT TODO
FROM flyway_schema_history
ORDER BY TODO;
```

**Hints**

- Version describes logical ordering.
- Installed rank describes history insertion order.
- Repeatable migrations have no version.
- Never hard-code or manually invent a checksum.

**How to verify**

Compare V1–V5 filenames with their rows. Confirm V999/V1000 are absent after the PostgreSQL rollback experiment.

**Common mistakes**

- Treating `installed_rank` as the version.
- Assuming `success=false` must appear after every failure.
- Editing a row directly.
- Believing history alone proves every object has not been manually changed.

**Explanation**

History proves what Flyway recorded applying. It does not prevent an administrator from creating drift afterward, which is why schema inspection and Hibernate validation remain useful.

### Task 15 — Compare versioned and repeatable migrations (optional)

**Objective**

Use one reasonable repeatable object and observe checksum-triggered re-execution.

**Concept**

Repeatables run after pending versioned migrations and rerun when their content changes. They are appropriate for replaceable definitions such as views.

**What to implement**

Create:

```text
R__book_stock_summary_view.sql
```

Define a view using `CREATE OR REPLACE VIEW`. Start once, restart unchanged, then make one meaningful view-definition change and restart again.

Predict:

- Does the repeatable row have a version?
- Does unchanged content rerun?
- What triggers a second execution?

**Starter code**

```sql
CREATE OR REPLACE VIEW book_stock_summary AS
SELECT
    TODO
FROM books;
```

**Hints**

- Include existing columns only; Task 17 has not added `status` yet.
- A useful derived column is `(stock = 0) AS out_of_stock`.
- Keep table evolution in V files, not R files.

**How to verify**

```sql
SELECT *
FROM book_stock_summary
ORDER BY id;

SELECT installed_rank, version, description, type, script, checksum, installed_on
FROM flyway_schema_history
WHERE script = 'R__book_stock_summary_view.sql'
ORDER BY installed_rank;
```

After a content change, expect another repeatable execution record; `version` is null. Exact presentation of old repeatable entries can vary by Flyway tooling/state labels.

**Common mistakes**

- Naming it `V__...` or `R1__...`.
- Using plain `CREATE VIEW` so re-execution fails because the view exists.
- Putting destructive table DDL in a repeatable.
- Expecting it to rerun on every unchanged startup.

**Explanation**

Versioned history is append-only. A repeatable's checksum is deliberately used as a “definition changed” trigger.

### Task 16 — Adopt an existing schema with an explicit baseline (optional advanced lab)

**Objective**

Understand how a verified non-empty database can enter Flyway management without replaying V1–V3 over existing objects.

**Concept**

The current legacy Book Catalog schema already represents the state after V3 but has no history. Baseline declares that fact; it does not discover or prove it.

**What to implement**

Perform this only against a separate disposable copy, for example `book_catalog_flyway_adopt`—never the original database during the tutorial.

1. Create/restore a copy whose schema exactly contains current `books.version` and `transaction_audits` but no Flyway history.
2. Back it up if its rows matter.
3. Verify database, schema, user, tables, columns, constraints, and row counts.
4. Start with `baseline-on-migrate: false`; predict and observe that Flyway refuses to guess how the non-empty schema was created.
5. With a separately installed/configured Flyway CLI or approved operations tooling, explicitly baseline at version `3` with a clear description.
6. Run normal migration: V1–V3 are skipped; V4, V5, and the optional repeatable apply.
7. Keep `baseline-on-migrate: false` in final application configuration.

**Starter code**

```sql
SELECT current_database(), current_schema(), current_user;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

SELECT COUNT(*) FROM books;
SELECT COUNT(*) FROM transaction_audits;
```

Conceptual explicit operation—configure credentials outside source and verify the scratch URL immediately before running:

```text
flyway ... -baselineVersion=3 \
           -baselineDescription="Existing transaction-stage schema" \
           baseline
```

The Flyway CLI is optional and separate from Boot startup; exact installation/credential flags depend on the tool installation. Do not add a Maven plugin solely to make this optional lab larger.

**Hints**

- A clean database history has SQL rows V1, V2, V3.
- An adopted database instead has a BASELINE marker at V3.
- A baseline checksum is normally null because no V3 SQL file was executed by that marker.
- `baseline-on-migrate=true` automates the assertion and weakens wrong-database protection.
- Flyway's ordinary default baseline version is 1, so this lab must explicitly select version 3.
- A `B3__...sql` baseline migration file is a different Flyway feature and is out of scope here.

**How to verify**

```sql
SELECT installed_rank, version, description, type, script, checksum, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Verify legacy row counts are unchanged. Expect a V3 BASELINE entry rather than SQL rows for V1–V3, followed by later SQL migrations.

**Common mistakes**

- Baseline at V3 without proving the schema equals V3.
- Baseline the original database during a tutorial experiment.
- Expect baseline to execute V1–V3.
- Leave `baseline-on-migrate` enabled.
- Demand identical history between clean and adopted databases.

**Explanation**

```text
clean path:    V1 SQL -> V2 SQL -> V3 SQL -> V4 -> V5
adopted path:  V3 BASELINE marker          -> V4 -> V5

same intended current structure; legitimately different provenance
```

### Task 17 — Add a safe `NOT NULL` column in V6

**Objective**

Perform a small schema-plus-data evolution that respects existing rows and future JPA inserts.

**Concept**

Existing rows have no value for a new column. The application also does not map that column, so PostgreSQL must supply a value for future inserts.

**What to implement**

Create:

```text
V6__add_books_status.sql
```

In order:

1. add nullable `status VARCHAR(30)`;
2. backfill null rows to `ACTIVE`;
3. set default `ACTIVE` for future inserts;
4. enforce `NOT NULL`.

Prediction: why might this one-line version fail or break later inserts?

```sql
ALTER TABLE books ADD COLUMN status VARCHAR(30) NOT NULL;
```

**Starter code**

```sql
ALTER TABLE books ADD COLUMN status TODO;

UPDATE books
SET status = TODO
WHERE status IS NULL;

ALTER TABLE books ALTER COLUMN status SET TODO;
ALTER TABLE books ALTER COLUMN status SET TODO;
```

**Hints**

- Use a quoted SQL string literal: `'ACTIVE'`.
- Retain the default because current Hibernate INSERT statements do not supply `status`.
- An extra database column need not appear in the REST response.

**How to verify**

```sql
SELECT id, title, status
FROM books
ORDER BY id;

SELECT column_name, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'books'
  AND column_name = 'status';
```

Create another Book through `POST /books`; verify PostgreSQL gives it `ACTIVE` and Hibernate validation still succeeds.

**Common mistakes**

- Enforcing `NOT NULL` before backfilling.
- Removing the default while the application does not write the column.
- Adding `status` to DTOs/entities/endpoints and turning this into a business feature.
- Editing V1 instead of adding V6.

**Explanation**

```text
schema expands -> old data becomes compatible -> future inserts get default -> rule tightens
```

This is the smallest form of an expand/backfill/contract migration.

### Task 18 — Perform final database and API verification

**Objective**

Prove the migration setup is complete using evidence from every layer.

**Concept**

Successful startup alone is insufficient. Verify migration provenance, PostgreSQL state, Hibernate compatibility, and existing application behavior.

**What to implement**

Create a verification sheet covering:

- application starts;
- Flyway reports no pending migration on a second unchanged start;
- V1–V6 appear once on the clean path;
- optional repeatable view works;
- `books.version`, `books.status`, and `transaction_audits` exist;
- only the composite author index remains after V5;
- old migrations did not rerun;
- Hibernate validation succeeds;
- existing Book reads/writes and transaction-lab behavior match the pre-Flyway baseline.

Before running, draw the expected startup order and expected history rows.

**Starter code**

```sql
-- TODO: current target identity

-- TODO: full Flyway history

-- TODO: tables/columns

-- TODO: indexes

-- TODO: row checks
```

**Hints**

- Use the complete queries in the reference only after attempting your own.
- The pre-existing DELETE rollback lab is not a Flyway regression.
- A second unchanged restart is essential evidence.

**How to verify**

Use application logs, the SQL queries you built, and the existing endpoints. Confirm that no migration runs during an HTTP request.

**Common mistakes**

- Checking only HTTP responses.
- Querying the wrong database.
- Ignoring a Hibernate validation warning after Flyway logs success.
- Treating one successful startup as proof that versioned migrations will not rerun.

**Explanation**

The exercise is complete only when you can narrate:

```text
migration file
 -> Boot startup
 -> Flyway validation/history decision
 -> Hikari/JDBC connection
 -> PostgreSQL transaction and schema change
 -> history commit
 -> Hibernate validation
 -> application ready
 -> unchanged request-time transaction behavior
```

==================================================
STOP — ATTEMPT THE EXERCISE BEFORE READING SOLUTION
===================================================

Do not continue until you have written predictions for Tasks 1–18 and attempted the core implementation through V6.

## Complete cumulative reference solution

### Selected approach

The reference uses the real current project at `Transaction/book-catalog-api` and reconstructs its known evolution:

```text
V1  books without optimistic version
V2  add books.version
V3  transaction_audits
V4  first author index
V5  forward replacement with author/id index
V6  safe status add/backfill/default/constraint
R   replaceable stock-summary view
```

The normal learning target is a new `book_catalog_flyway` database. The original `book_catalog` and its named-volume data remain untouched. The optional adoption lab uses a separate copy and baselines it at V3 only after verification.

All Java source remains unchanged. Flyway changes schema lifecycle, not business behavior.

V4 and V5 deliberately remain as two files because this exercise first applies V4 and then teaches a forward correction. Before any migration has been shared or applied, a team may simplify local drafts. After V4 is part of durable history, V5 is the honest correction and V4 must remain.

### Exact change manifest

Paths below are relative to `Transaction/book-catalog-api`.

#### Existing files modified

| File | Why |
|---|---|
| `pom.xml` | Add Boot 4 Flyway integration and Flyway's PostgreSQL database module |
| `src/main/resources/application.yaml` | Select a safe practice target, disable basic SQL initialization, configure Flyway, keep Hibernate validation |

#### Files added

```text
src/main/resources/db/migration/V1__create_books.sql
src/main/resources/db/migration/V2__add_optimistic_lock_version.sql
src/main/resources/db/migration/V3__create_transaction_audits.sql
src/main/resources/db/migration/V4__add_books_author_index.sql
src/main/resources/db/migration/V5__replace_author_index_for_paging.sql
src/main/resources/db/migration/V6__add_books_status.sql
src/main/resources/db/migration/R__book_stock_summary_view.sql
```

The R file implements the optional Task 15. Omit it and its expected history row if you skip that task.

#### File removed from active source

```text
src/main/resources/schema.sql
```

Its logic is not discarded: V1–V3 reproduce it as explicit evolution. Basic SQL initialization is also set to `never`, so a stale generated `target/classes/schema.sql` cannot compete at runtime.

#### Existing files unchanged

- `src/main/java/com/example/bookcatalog/Application.java`;
- `src/main/java/com/example/bookcatalog/dto/BookRequest.java`;
- `src/main/java/com/example/bookcatalog/dto/BookResponse.java`;
- `src/main/java/com/example/bookcatalog/dto/StockRequest.java`;
- `src/main/java/com/example/bookcatalog/dto/StockTransferRequest.java`;
- `src/main/java/com/example/bookcatalog/exception/BookNotFoundException.java`;
- `src/main/java/com/example/bookcatalog/exception/DuplicateIsbnException.java`;
- `src/main/java/com/example/bookcatalog/exception/TransactionLabCheckedException.java`;
- `src/main/java/com/example/bookcatalog/model/Book.java`;
- `src/main/java/com/example/bookcatalog/model/TransactionAudit.java`;
- `src/main/java/com/example/bookcatalog/repository/BookRepository.java`;
- `src/main/java/com/example/bookcatalog/repository/TransactionAuditRepository.java`;
- `src/main/java/com/example/bookcatalog/service/BookService.java`;
- `src/main/java/com/example/bookcatalog/service/BookStockService.java`;
- `src/main/java/com/example/bookcatalog/service/StockTransferService.java`;
- `src/main/java/com/example/bookcatalog/service/TransactionAuditService.java`;
- `src/main/java/com/example/bookcatalog/web/BookController.java`;
- `src/main/java/com/example/bookcatalog/web/GlobalExceptionHandler.java`;
- `src/main/java/com/example/bookcatalog/web/TransactionLabController.java`;
- `src/test/java/com/example/AppTest.java`;
- `compose.yaml`, `.env`, and `.env.example`;
- `Spring_Transactions_Deep_Essential_Concepts.md` and `Spring_Transactions_Deep_Exercise.md`;
- all existing public routes and DTO response shapes.

The deliberately broken V999/V1000 files and the temporary edited V4 are experiments only. They do not belong in the final tree.

### Final relevant project structure

```text
Transaction/book-catalog-api/
├── pom.xml                                      # modified
├── compose.yaml                                 # unchanged
└── src/
    └── main/
        ├── java/com/example/bookcatalog/        # entirely unchanged
        └── resources/
            ├── application.yaml                 # modified
            └── db/
                └── migration/
                    ├── V1__create_books.sql
                    ├── V2__add_optimistic_lock_version.sql
                    ├── V3__create_transaction_audits.sql
                    ├── V4__add_books_author_index.sql
                    ├── V5__replace_author_index_for_paging.sql
                    ├── V6__add_books_status.sql
                    └── R__book_stock_summary_view.sql
```

There is no final `src/main/resources/schema.sql`.

### Complete final `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
    <relativePath/>
  </parent>

  <groupId>com.example</groupId>
  <artifactId>book-catalog-api</artifactId>
  <version>1.0-SNAPSHOT</version>

  <name>book-catalog-api</name>
  <url>http://www.example.com</url>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>21</maven.compiler.release>
  </properties>

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-flyway</artifactId>
    </dependency>

    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-database-postgresql</artifactId>
    </dependency>

    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-devtools</artifactId>
      <optional>true</optional>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
  </dependencies>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.1.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-resources-plugin</artifactId>
          <version>3.0.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.8.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>2.22.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-jar-plugin</artifactId>
          <version>3.0.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-install-plugin</artifactId>
          <version>2.5.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-deploy-plugin</artifactId>
          <version>2.8.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-site-plugin</artifactId>
          <version>3.7.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-project-info-reports-plugin</artifactId>
          <version>3.0.0</version>
        </plugin>
        <plugin>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

Why these two new dependencies:

```text
spring-boot-starter-flyway
  -> Boot 4 Flyway integration + Flyway core

flyway-database-postgresql
  -> Flyway's PostgreSQL database implementation

postgresql
  -> JDBC driver used to communicate with PostgreSQL
```

No Flyway Maven plugin is required. Normal migration occurs through Spring Boot startup, and the optional baseline lab may use separately installed administrative tooling.

### Complete final `application.yaml`

```yaml
spring:
  application:
    name: book-catalog-api

  datasource:
    url: ${DB_URL:jdbc:postgresql://127.0.0.1:5433/book_catalog_flyway}
    username: ${DB_USERNAME:book_app}
    password: ${DB_PASSWORD:replace-with-a-local-practice-password}
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2

  sql:
    init:
      mode: never

  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
    validate-migration-naming: true
    baseline-on-migrate: false
    out-of-order: false
    clean-disabled: true

  jackson:
    deserialization:
      fail-on-unknown-properties: true
      accept-float-as-int: false

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format-sql: true

logging:
  level:
    org.flywaydb: INFO
    org.springframework.transaction: TRACE
    org.springframework.orm.jpa.JpaTransactionManager: DEBUG
    org.hibernate.SQL: DEBUG

server:
  address: 127.0.0.1
  port: 8080
```

The `DB_*` placeholders add safe process-level overrides while retaining non-secret local defaults. Set the real local password outside source control. The pre-existing `format-sql` entry is preserved because correcting unrelated Hibernate formatting configuration is outside this migration exercise.

For a temporary Flyway diagnosis, change only this logger to `DEBUG`, then return it to `INFO`:

```yaml
logging:
  level:
    org.flywaydb: DEBUG
```

### Complete final migration SQL

#### `V1__create_books.sql`

```sql
CREATE TABLE books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(120) UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(120) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    stock INTEGER NOT NULL
);
```

V1 preserves the original JPA-stage table. ISBN remains nullable because that is the checked-in schema; the exercise does not silently change unrelated constraints.

#### `V2__add_optimistic_lock_version.sql`

```sql
ALTER TABLE books
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

V2 supplies the column required by `Book.@Version` before Hibernate validates.

#### `V3__create_transaction_audits.sql`

```sql
CREATE TABLE transaction_audits (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    details VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

V3 matches `TransactionAudit`, including `Instant` storage through PostgreSQL `TIMESTAMP WITH TIME ZONE`.

#### `V4__add_books_author_index.sql`

```sql
CREATE INDEX idx_books_author
ON books (author);
```

V4 is the applied file used by the checksum experiment. Its exact final content must be restored before proceeding.

#### `V5__replace_author_index_for_paging.sql`

```sql
DROP INDEX idx_books_author;

CREATE INDEX idx_books_author_id
ON books (author, id);
```

V5 is the forward correction. V4 remains in history.

#### `V6__add_books_status.sql`

```sql
ALTER TABLE books
ADD COLUMN status VARCHAR(30);

UPDATE books
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE books
ALTER COLUMN status SET DEFAULT 'ACTIVE';

ALTER TABLE books
ALTER COLUMN status SET NOT NULL;
```

The retained default is essential because the unchanged JPA entity does not include `status`; future Hibernate inserts omit it.

#### `R__book_stock_summary_view.sql`

```sql
CREATE OR REPLACE VIEW book_stock_summary AS
SELECT id,
       isbn,
       title,
       author,
       stock,
       version,
       (stock = 0) AS out_of_stock
FROM books;
```

This view is intentionally simple and requires no entity. It can be safely replaced when its repeatable migration checksum changes.

## Safe execution guide

### 1. Start PostgreSQL without resetting its volume

From the project:

```powershell
cd D:\DSA-new\DSA\Transaction\book-catalog-api
docker compose up -d postgres
docker compose ps
```

Do not run `docker compose down -v`. The named volume may contain the original `book_catalog` database and valued learning data.

### 2. Create the clean practice database

If the configured PostgreSQL role is `book_app`, this is a typical local command:

```powershell
docker compose exec postgres psql -U book_app -d postgres -c "CREATE DATABASE book_catalog_flyway OWNER book_app;"
```

If that role is not allowed to create databases, use pgAdmin or a verified administrative role, then set ownership/permissions for the application's role. `CREATE DATABASE` itself is an administrative operation outside a normal PostgreSQL transaction block.

If the command says the database already exists, do **not** automatically drop it. Connect to it and inspect it first. Dropping it would remove all objects, rows, and Flyway history in that database.

### 3. Select the target without exposing credentials

The YAML defaults to the scratch database. You can make the target explicit in the current PowerShell session:

```powershell
$env:DB_URL = 'jdbc:postgresql://127.0.0.1:5433/book_catalog_flyway'
$bookDbCredential = Get-Credential -UserName 'book_app'
$env:DB_USERNAME = $bookDbCredential.UserName
$env:DB_PASSWORD = $bookDbCredential.GetNetworkCredential().Password
```

This keeps the entered password out of the command text and repository. Do not print `$env:DB_PASSWORD`.

Before an administrative operation, verify through the target connection:

```sql
SELECT current_database(), current_schema(), current_user;
```

Expected database: `book_catalog_flyway`.

### 4. Verify dependencies

```powershell
mvn dependency:tree "-Dincludes=org.flywaydb:*"
mvn help:evaluate "-Dexpression=flyway.version" "-DforceStdout" -q
```

Expected important artifacts:

```text
org.flywaydb:flyway-core
org.flywaydb:flyway-database-postgresql
```

The Boot starter supplies the integration module and Flyway core; the database-specific dependency supplies PostgreSQL support.

### 5. Start the application

```powershell
mvn spring-boot:run
```

Representative first-start ordering—not guaranteed literal log wording—is:

```text
Hikari pool starts / database connection becomes available
Flyway identifies PostgreSQL and default schema
Flyway creates or finds flyway_schema_history
Flyway validates resolved migrations
Migrating schema "public" to version "1 - create books"
Migrating schema "public" to version "2 - add optimistic lock version"
Migrating schema "public" to version "3 - create transaction audits"
... later pending versions ...
Successfully applied migrations
Hibernate builds/validates the persistence unit
Application started
```

On a second unchanged startup:

```text
Flyway validates
schema is up to date / no migration necessary
Hibernate validates
application starts
```

Do not require exact logger sentences; compare lifecycle ordering and resulting database state.

## Complete PostgreSQL verification

Run these against `book_catalog_flyway`.

### Target identity

```sql
SELECT current_database(), current_schema(), current_user;
```

### Full migration history

```sql
SELECT installed_rank,
       version,
       description,
       type,
       script,
       checksum,
       installed_by,
       installed_on,
       execution_time,
       success
FROM flyway_schema_history
ORDER BY installed_rank;
```

For a brand-new database started with the complete final directory, expect the logical rows below. Checksums and timestamps are generated values, so do not copy fixed numbers:

| Version | Type | Script | Success |
|---|---|---|---|
| `1` | SQL | `V1__create_books.sql` | true |
| `2` | SQL | `V2__add_optimistic_lock_version.sql` | true |
| `3` | SQL | `V3__create_transaction_audits.sql` | true |
| `4` | SQL | `V4__add_books_author_index.sql` | true |
| `5` | SQL | `V5__replace_author_index_for_paging.sql` | true |
| `6` | SQL | `V6__add_books_status.sql` | true |
| null | SQL | `R__book_stock_summary_view.sql` (repeatable because version is null) | true |

If the repeatable was changed during Task 15, more than one historical repeatable execution may be visible. Versioned V1–V6 still appear once.

### Tables and view

```sql
SELECT table_name, table_type
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

SELECT table_name
FROM information_schema.views
WHERE table_schema = 'public'
ORDER BY table_name;
```

Expected application tables:

```text
books
flyway_schema_history
transaction_audits
```

Expected optional view:

```text
book_stock_summary
```

### `books` columns

```sql
SELECT ordinal_position,
       column_name,
       data_type,
       character_maximum_length,
       numeric_precision,
       numeric_scale,
       is_nullable,
       column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'books'
ORDER BY ordinal_position;
```

Expected final logical shape:

| Column | Important definition |
|---|---|
| `id` | identity `BIGINT`, primary key |
| `isbn` | nullable `VARCHAR(120)`, unique |
| `title` | `VARCHAR(200) NOT NULL` |
| `author` | `VARCHAR(120) NOT NULL` |
| `price` | `NUMERIC(12,2) NOT NULL` |
| `stock` | `INTEGER NOT NULL` |
| `version` | `BIGINT NOT NULL DEFAULT 0` |
| `status` | `VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'` |

PostgreSQL may render defaults with casts or identity expressions; compare meaning, not one exact display string.

### Audit columns

```sql
SELECT ordinal_position, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'transaction_audits'
ORDER BY ordinal_position;
```

### Constraints

```sql
SELECT tc.table_name,
       tc.constraint_name,
       tc.constraint_type
FROM information_schema.table_constraints AS tc
WHERE tc.table_schema = 'public'
  AND tc.table_name IN ('books', 'transaction_audits')
ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;
```

Expect primary keys and the existing ISBN uniqueness rule.

### Indexes

```sql
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename IN ('books', 'transaction_audits')
ORDER BY tablename, indexname;
```

After V5:

- `idx_books_author` is gone;
- `idx_books_author_id` exists on `(author, id)`;
- PostgreSQL-created primary-key/unique indexes also exist.

### Data and the optional view

```sql
SELECT id, isbn, title, author, stock, version, status
FROM books
ORDER BY id;

SELECT *
FROM book_stock_summary
ORDER BY id;

SELECT id, event_type, details, created_at
FROM transaction_audits
ORDER BY id;
```

## API regression checks

Create a book using the unchanged API:

```powershell
curl.exe -i -X POST http://127.0.0.1:8080/books `
  -H "Content-Type: application/json" `
  --data-raw '{"isbn":"flyway-001","title":"Migration Practice","author":"A. Learner","price":19.99,"stock":4}'
```

Read it and list/filter books:

```powershell
curl.exe -i http://127.0.0.1:8080/books
curl.exe -i "http://127.0.0.1:8080/books?author=A.%20Learner&page=0&size=20"
```

Update stock using the returned ID:

```powershell
curl.exe -i -X PATCH http://127.0.0.1:8080/books/1 `
  -H "Content-Type: application/json" `
  --data-raw '{"stock":0}'
```

Use the actual ID. Then verify:

```sql
SELECT id, stock, version, status
FROM books
WHERE isbn = 'flyway-001';
```

Expected behavior:

- the existing JSON shape has no `status` field;
- PostgreSQL supplies `status = 'ACTIVE'`;
- Hibernate manages `version` as before;
- the repeatable view reflects `out_of_stock` after the stock change;
- no Flyway SQL runs during these requests.

The current checked-in DELETE path intentionally throws after flush and rolls back. A 500/rollback there is pre-existing transaction-lab behavior, not a Flyway migration failure. Do not “fix” it as part of this exercise.

## Expected execution beneath the abstractions

### Successful V6

```text
Boot initialization
 -> Flyway sees V6 pending
 -> primary HikariDataSource supplies JDBC connection
 -> PostgreSQL begins migration transaction
 -> ADD nullable status executes
 -> UPDATE backfills old rows
 -> SET DEFAULT configures future omitted values
 -> SET NOT NULL validates/tightens rule
 -> Flyway inserts V6 history record
 -> COMMIT
 -> connection is returned to HikariCP
 -> Hibernate validates mapped columns
 -> application becomes ready
```

Hibernate does not map `status`, but schema validation normally tolerates extra database columns. The default prevents unchanged Hibernate inserts from violating the new database rule.

### SQL logger boundaries

Flyway executes migration SQL directly through JDBC, not through Hibernate's persistence context. Therefore:

```text
org.flywaydb logs          migration discovery/version/application
org.hibernate.SQL logs     request-time ORM SQL
Spring transaction logs    business transaction boundaries
PostgreSQL catalog          durable schema truth
```

Do not expect `org.hibernate.SQL` to be the authoritative logger for Flyway DDL.

## Reference outcomes for the controlled experiments

### Task 1 — Responsibility answer

| Component | Responsibility in this exercise |
|---|---|
| Spring Boot | Creates/configures the application context, primary `DataSource`, Flyway integration, and initialization dependencies |
| Flyway | Resolves files, reads history, validates checksums, applies pending migration SQL, and records history |
| `DataSource` | Standard abstraction through which Flyway/Hibernate obtain JDBC connections |
| HikariCP | Implements the primary pooled `DataSource` and manages connection leases |
| pgJDBC/JDBC | Transports SQL and transaction operations to PostgreSQL |
| PostgreSQL | Executes DDL/DML, owns locks/schema/data, and commits or rolls back |
| JPA entities | Declare the application's mapping expectations; they are not migration history |
| Hibernate | Implements JPA and validates/uses the schema after migration |
| Spring service proxy | Manages later business `@Transactional` calls; it does not wrap Flyway startup |

### Task 2 — Dependency answer

The correct Boot 4.1.1 pair is:

```text
org.springframework.boot:spring-boot-starter-flyway
org.flywaydb:flyway-database-postgresql
```

Neither has an explicit version. Missing database support commonly produces a Flyway “unsupported database”/database-recognition failure even though pgJDBC can establish a connection.

### Tasks 8–10 — First run versus restart

```text
First clean run:
history absent
 -> create history
 -> run all resolved pending V migrations
 -> run repeatable
 -> Hibernate validate

Second unchanged run:
history agrees with files
 -> validate
 -> zero pending
 -> no versioned/repeatable SQL
 -> Hibernate validate
```

The history row count and installed timestamps remain unchanged on the second run.

### Task 11 — Checksum experiment, exact sequence

1. V4 has already been applied with:

```sql
CREATE INDEX idx_books_author
ON books (author);
```

2. Temporarily replace its source text with:

```sql
CREATE INDEX idx_books_author
ON books (author, id);
```

3. Restart. Expected sequence:

```text
Flyway reads stored V4 checksum
 -> calculates current V4 checksum
 -> mismatch
 -> validation exception
 -> no migration SQL runs
 -> Hibernate/web startup does not complete
```

4. PostgreSQL still contains the original one-column index because the edited V4 was not executed.
5. Restore the exact original V4 and restart. Validation passes.

Do not use `repair`: that would change history metadata without executing the edited index SQL, potentially making the ledger claim agreement while PostgreSQL still has the old definition.

### Task 12 — Forward fix outcome

V5 runs once and commits both statements in its migration transaction:

```text
DROP idx_books_author
CREATE idx_books_author_id(author, id)
record V5
COMMIT
```

History still contains V4 because V4 truly happened. Database state contains only the newer composite index.

### Task 13 — Failed migration outcome

With the temporary V999/V1000 files present:

```text
history before: V1–V5 successful

V999 begins
  ALTER ADD failed_demo              succeeds provisionally
  ALTER missing column               fails
  PostgreSQL ROLLBACK                removes failed_demo

Flyway aborts startup
V1000 is never attempted
```

Expected queries:

```sql
SELECT column_name
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'books'
  AND column_name = 'failed_demo';
-- zero rows

SELECT to_regclass('public.migration_should_not_exist');
-- null

SELECT version, script, success
FROM flyway_schema_history
WHERE version IN ('999', '1000');
-- normally zero rows on PostgreSQL for this transactional failure
```

The `success` column exists because Flyway supports databases/statements where failed history can persist. Do not infer that every failed PostgreSQL transactional migration must leave `success=false`.

Delete the two local disposable files, verify the directory contains only V1–V6 and R, then restart. No `repair` is needed for this PostgreSQL outcome.

### Task 15 — Repeatable sequence

An initial version might be:

```sql
CREATE OR REPLACE VIEW book_stock_summary AS
SELECT id,
       title,
       stock
FROM books;
```

After applying it:

- unchanged restart: no repeatable execution;
- change the file to the complete final view shown earlier;
- restart: checksum differs, so Flyway executes `CREATE OR REPLACE VIEW` again;
- another unchanged restart: no execution.

An experimental database can therefore have two historical rows for the R script. A clean database built from the final checkout has one.

### Task 16 — Complete optional baseline lab

Use a separate database named `book_catalog_flyway_adopt`. The following builds a controlled **legacy V3 simulation**; it is not a replacement for migration scripts in normal operation:

```sql
CREATE TABLE books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(120) UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(120) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    stock INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE transaction_audits (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    details VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO books (isbn, title, author, price, stock)
VALUES ('legacy-sentinel', 'Must Survive Baseline', 'Migration Lab', 10.00, 3);
```

Confirm there is no Flyway history:

```sql
SELECT to_regclass('public.flyway_schema_history');
-- null
```

Point a process at this database with `baseline-on-migrate: false`. Expected startup failure is conceptually:

```text
non-empty public schema
+ no flyway_schema_history
-> Flyway refuses to guess the baseline
```

Now verify exact V3 state and the sentinel row. If a separately installed Flyway CLI is available, set its password in the process environment rather than the command text and run an explicit baseline:

```powershell
$env:FLYWAY_URL = 'jdbc:postgresql://127.0.0.1:5433/book_catalog_flyway_adopt'
$env:FLYWAY_USER = 'book_app'
$adoptDbCredential = Get-Credential -UserName 'book_app'
$env:FLYWAY_PASSWORD = $adoptDbCredential.GetNetworkCredential().Password

flyway '-baselineVersion=3' `
       '-baselineDescription=Existing transaction-stage schema' `
       baseline
```

Depending on how the CLI was installed, URL/user can instead come from its secure local configuration. Immediately re-run:

```sql
SELECT current_database(), current_schema(), current_user;

SELECT installed_rank, version, description, type, script, checksum, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Then start the Boot application with `DB_URL` pointing to the adoption database. It applies V4, V5, V6, and the repeatable. It skips V1–V3 because the baseline marker says the schema already represents V3.

Expected adopted history shape:

| Version | Type | Meaning |
|---|---|---|
| `3` | BASELINE | Existing schema was declared to represent V3; V1–V3 SQL did not run |
| `4` | SQL | First author index |
| `5` | SQL | Forward index replacement |
| `6` | SQL | Status evolution |
| null | SQL | Stock summary repeatable view; its version is null |

Verify:

```sql
SELECT isbn, title, status
FROM books
WHERE isbn = 'legacy-sentinel';
```

The row remains and receives `ACTIVE` from V6's backfill.

Finally remove the CLI password variable from the session when it is no longer needed:

```powershell
Remove-Item Env:FLYWAY_PASSWORD
```

Never leave this in final application configuration:

```yaml
spring:
  flyway:
    baseline-on-migrate: true
```

An automatic baseline can be demonstrated only against another disposable database after the explicit lab is understood. It is not needed for the reference solution.

### Task 17 — Safe status outcome

Before V6:

```text
existing rows: no status column
new Hibernate INSERT: no status value
```

During V6:

```text
ADD nullable -> backfill ACTIVE -> set default ACTIVE -> enforce NOT NULL
```

After V6:

```text
existing rows: ACTIVE
new Hibernate INSERT: PostgreSQL default ACTIVE
REST DTO/entity: unchanged
```

The whole ordinary PostgreSQL migration is transactional. A failure before its commit should prevent a partially applied V6 in this setup.

## Clean path versus adopted path

Both paths can reach the same intended schema, but their histories truthfully differ:

```text
CLEAN book_catalog_flyway
  V1 SQL
  V2 SQL
  V3 SQL
  V4 SQL
  V5 SQL
  V6 SQL
  R  SQL

ADOPTED book_catalog_flyway_adopt
  V3 BASELINE
  V4 SQL
  V5 SQL
  V6 SQL
  R  SQL
```

Do not “fix” this difference. The clean database was built by Flyway from V1. The adopted database arrived with a verified V3 schema and has different provenance.

## Expected Hibernate behavior

| Schema condition after Flyway | Expected Hibernate result |
|---|---|
| V1–V3 complete | Required `books`, `version`, and `transaction_audits` mappings validate |
| V2 missing/fails | `Book.@Version` cannot validate against the absent column; startup fails |
| V3 missing/fails | `TransactionAudit` mapping cannot validate against absent table; startup fails |
| V4/V5 index changes | Entity mappings remain unaffected |
| V6 extra `status` with default | Extra column is tolerated; unchanged inserts remain valid because PostgreSQL supplies default |
| Flyway succeeds but a mapped type/name is wrong | Flyway history may be valid, then Hibernate schema validation fails |

This is why both validators remain enabled.

## Failure diagnosis table

| Symptom | Likely cause | Safe first checks/fix |
|---|---|---|
| Flyway reports unsupported PostgreSQL database | Missing `flyway-database-postgresql` or incompatible dependency override | Inspect dependency tree; use Boot-managed versions |
| “No migrations found” | Wrong directory/name/location | Check `src/main/resources/db/migration` and double underscore |
| Non-empty schema without history | App targets old/incorrect DB | Verify `current_database`; choose clean DB or deliberate baseline |
| Checksum mismatch | Applied V file was edited | Restore the exact applied file; add a new version for real change |
| Duplicate migration version | Two files claim one V number | Renumber an unapplied branch migration before merge |
| Migration SQL error | Invalid assumption, permissions, syntax, or current state | Read first Flyway/PostgreSQL cause; inspect actual schema; do not repair blindly |
| Flyway succeeds, Hibernate fails | Resulting schema does not satisfy entity mapping | Compare entity/table/column/type; add forward migration |
| `relation already exists` on clean path | Target is not actually clean or another initializer ran | Verify target and `spring.sql.init.mode: never` |
| V6 INSERT later fails on `status` | Default was omitted/removed while entity does not supply status | Add a forward migration restoring compatible insert behavior |
| Application endpoint fails but startup succeeded | Business/application transaction issue | Inspect service/ORM logs; do not blame startup migration automatically |
| Old `schema.sql` seems to run | SQL init still enabled or stale configuration/profile | Verify effective config; source owner; generated `target` is not authoritative |

## Common implementation mistakes diagnosed

- **Only `flyway-core` was added to Boot 4:** add the Boot 4 starter and PostgreSQL module shown in the final POM.
- **V1 contains current end state while V2/V3 repeat it:** choose one coherent history; this reference uses the known real evolution.
- **`schema.sql` remains active:** migrate all logic, set SQL initialization to `never`, and remove source `schema.sql`.
- **Hibernate changed to `update`:** restore `validate`; Flyway must remain the schema owner.
- **Original database was used accidentally:** stop, identify what ran, preserve evidence, and do not use `clean` or manual history deletion.
- **Applied migration was edited:** restore it; write a forward version.
- **`repair` was treated as schema repair:** remember it changes history metadata; it does not execute the newly typed SQL.
- **Failed PostgreSQL V999 was followed by repair:** ordinary transactional rollback normally leaves it pending with no durable failed row; remove/correct the never-applied local probe.
- **Baseline was selected from a guess:** restore/inspect a copy and prove it equals V3 before baseline.
- **A one-line `NOT NULL` migration was used:** account for both old rows and future inserts.
- **Index use was inferred from existence:** use `EXPLAIN` with representative data for performance decisions; a tiny table may be scanned.
- **Real secrets were committed:** remove them from source and rotate any exposed credential.

## Final exercise checklist

### Project and ownership

- [ ] The target is `Transaction/book-catalog-api`.
- [ ] Boot 4's Flyway starter and PostgreSQL module are present without explicit versions.
- [ ] `schema.sql` is no longer an active source owner.
- [ ] `spring.sql.init.mode` is `never`.
- [ ] Flyway location is `classpath:db/migration`.
- [ ] Invalid migration names fail fast through `validate-migration-naming: true`.
- [ ] `baseline-on-migrate` is false.
- [ ] `out-of-order` is false.
- [ ] `clean-disabled` is true.
- [ ] Hibernate remains `ddl-auto: validate`.

### Migration history

- [ ] V1 creates the original `books` table.
- [ ] V2 adds optimistic-lock `version`.
- [ ] V3 creates `transaction_audits`.
- [ ] V4 adds the initial author index.
- [ ] V5 replaces it through forward history.
- [ ] V6 safely adds/backfills/defaults/constrains status.
- [ ] The optional R migration uses `CREATE OR REPLACE VIEW`.
- [ ] Applied versioned files have not been rewritten.

### Safety

- [ ] The original `book_catalog` was not deleted or baselined by the main lab.
- [ ] The named Docker volume was not deleted.
- [ ] Every administrative target was verified with `current_database()`.
- [ ] No real `.env` value or password was printed/committed.
- [ ] `clean` and blind `repair` were not used.

### Evidence

- [ ] First clean startup applies the expected pending versions.
- [ ] Second unchanged startup applies none.
- [ ] History fields can be explained from memory.
- [ ] Checksum editing fails validation before SQL runs.
- [ ] The failed PostgreSQL migration rolls back its provisional DDL.
- [ ] Later migration does not run after failure.
- [ ] Hibernate validation succeeds after the final schema.
- [ ] Existing API and transaction-lab behavior matches the pre-Flyway baseline.

## Reflection questions

1. Why is editing `CREATE TABLE books` insufficient for a database already containing `books`?
2. Which component decides V5 is pending, and which component executes `DROP INDEX`?
3. Why are the Flyway checksum validator and Hibernate schema validator both valuable?
4. Why can V1 and V2 remain committed when V3 fails?
5. Why does failed V999 normally leave no `failed_demo` column on PostgreSQL?
6. Why should the checksum experiment end by restoring V4 instead of repairing history?
7. Why does the adopted database have a V3 BASELINE row rather than V1–V3 SQL rows?
8. Why must V6 retain a default if the entity does not map `status`?
9. Which connection-pool responsibility does Flyway use, and which migration responsibilities does HikariCP not have?
10. At what point can the application safely accept HTTP traffic?

### Compact answer key

1. A create statement describes a fresh object; an existing database needs an ordered `ALTER`/evolution path.
2. Flyway decides from resolved files/history; PostgreSQL executes SQL received through JDBC.
3. One checks migration provenance; the other checks ORM/schema compatibility.
4. Migrations are normally separate transactions; prior versions already committed.
5. Its ordinary DDL is inside the failed migration transaction and rolls back.
6. Repair would bless changed metadata without executing the edited SQL or proving schema agreement.
7. Baseline declares existing V3 state and deliberately skips earlier SQL.
8. Hibernate omits the unmapped column, so PostgreSQL must supply a non-null value.
9. Hikari leases/returns connections; it does not order files, calculate checksums, or change schema.
10. After Flyway finishes and Hibernate/application-context initialization succeeds.

## Final mental model

```text
Git contains immutable V migrations
    |
Spring Boot starts
    |
primary Hikari DataSource becomes available
    |
Flyway reads migration files + flyway_schema_history
    |
validate checksums and determine pending versions
    |
JDBC / pgJDBC sends SQL to PostgreSQL
    |
PostgreSQL transaction
    +-- success: execute SQL -> insert history -> COMMIT both
    `-- failure: ROLLBACK current migration when supported; stop startup
    |
Hikari connection returns
    |
Hibernate validates JPA mappings against the migrated schema
    |
application becomes ready
    |
HTTP requests use normal service @Transactional behavior
```

Flyway is not an ORM, HikariCP is not a migration engine, a baseline is not a migration, a checksum repair is not a schema fix, and migration rollback is not an automatic reverse of yesterday's deployment.
