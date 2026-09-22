# Flyway Database Migration — Essential Concepts

> A first-principles guide to evolving PostgreSQL schemas safely with Flyway while keeping Spring Boot, Hibernate, JDBC, HikariCP, and database transactions in one accurate mental model.

**Verified companion project:** `Transaction/book-catalog-api`  
**Verified baseline:** Java 21, Spring Boot 4.1.1, Spring Data JPA, Hibernate ORM, PostgreSQL 17, pgJDBC, a Boot-managed HikariCP `DataSource`, `ddl-auto: validate`, and SQL initialization through `schema.sql`. The current schema contains `books.version` for optimistic locking and `transaction_audits` for the transaction lab.  
**Learning goal:** Explain exactly how a migration file becomes a durable PostgreSQL schema change—and diagnose the process without saying “Spring does it automatically.”

This topic adds a new lifecycle to the stack you already know:

```text
Version-controlled migration file
    -> Spring Boot startup
    -> Flyway
    -> DataSource / HikariCP
    -> JDBC Connection
    -> PostgreSQL transaction and SQL
    -> schema change + flyway_schema_history row
    -> Hibernate schema validation
    -> application ready for HTTP traffic
```

Flyway does not replace any previously learned layer. It gives schema evolution an ordered, reviewable history.

## How to study

1. For every migration, predict the starting schema, SQL effect, final schema, and history row.
2. Keep migration-history validation separate from Hibernate entity/schema validation.
3. Translate Flyway operations back to JDBC connections and PostgreSQL transactions.
4. Treat a migration already applied to a shared database as immutable history.
5. Run the companion exercise against a disposable local database before considering an existing database.

## Concept priorities

| Priority | Meaning | Expected ability |
|---|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Required for safe everyday migration work | Explain, implement, and diagnose it |
| ⭐⭐⭐⭐ IMPORTANT | Common production or team concern | Apply deliberately and review carefully |
| ⭐⭐⭐ NICE TO KNOW | Useful for selected database objects or workflows | Recognize when to use it |
| ⭐⭐ FUTURE KNOWLEDGE | Advanced deployment or operational design | Understand the boundary; do not add casually |

---

## 1. Why database migrations exist

**⭐⭐⭐⭐⭐ MUST KNOW**

### 1.1 The real problem is time

A database is not recreated from source code on every deployment. It persists through many application versions:

```text
Monday       books table exists
Tuesday      application needs books.version
Wednesday    application needs transaction_audits
Thursday     an author query needs an index
```

Changing the last `CREATE TABLE` statement describes how a **new** database should look. It does not tell an already-running database how to move from Monday to Thursday without losing its data.

That is the core migration problem:

```text
known old database state
    + ordered, versioned change
    = known new database state
```

### 1.2 Four approaches compared

| Approach | What it provides | Main weakness after multiple environments exist |
|---|---|---|
| Manual SQL in pgAdmin/`psql` | Direct control | No reliable, shared record of who ran what, where, or in what order |
| One evolving `schema.sql` | Reproducible initialization for a fresh schema | Usually describes current shape, not the full upgrade path for populated databases |
| Hibernate `create`, `create-drop`, or `update` | Convenient development schema generation | Entity mappings are not a reviewed operational history; `update` is not a deliberate production migration plan |
| Version-controlled migrations | Ordered changes, history, validation, and repeatable deployment | Requires discipline: scripts must be reviewed and applied in order |

### 1.3 Example evolution

```text
V1  create books
 |
V2  add books.version
 |
V3  create transaction_audits
 |
V4  add an index for author queries
```

An empty database runs V1 through V4. A database whose Flyway history already records V1 through V3 runs only V4. Merely looking like the V3 schema without Flyway history is different—it requires deliberate adoption/baselining. This is more useful than repeatedly running one edited `CREATE TABLE IF NOT EXISTS` statement: `IF NOT EXISTS` does not add missing columns, reconstruct history, or prove that existing objects have the expected definition.

### 1.4 Why production needs history

When an application fails after deployment, the team must be able to answer:

- Which schema changes reached this database?
- In which order?
- Which script and checksum were applied?
- Did the migration finish successfully?
- Does this application version expect a migration not present here?

A migration tool makes those questions observable rather than dependent on memory.

## 2. What Flyway is

**⭐⭐⭐⭐⭐ MUST KNOW**

Flyway is a database migration tool. It discovers ordered migration scripts, compares them with a schema-history table, executes pending changes, and records their application.

Flyway is **not**:

- an ORM;
- a replacement for JPA entities or repositories;
- a database server;
- a connection pool;
- a substitute for PostgreSQL knowledge;
- a magic rollback engine for every deployed data change.

### 2.1 Responsibility map

| Component | Responsibility |
|---|---|
| Spring Boot | Configures startup, the `DataSource`, Flyway integration, and JPA initialization ordering |
| Flyway | Discovers, orders, validates, executes, and records database migrations |
| JPA | Defines the persistence API and mapping contracts used by application code |
| Hibernate ORM | Implements JPA, generates runtime SQL, and can validate mappings against the resulting schema |
| HikariCP | Pools database connections; it does not understand migration versions |
| pgJDBC | Implements JDBC communication with PostgreSQL |
| PostgreSQL | Executes SQL, owns tables/data/indexes/locks, and commits or rolls back database transactions |

```text
Spring Boot
    |
    v
Flyway
    |
    v
DataSource (HikariDataSource)
    |
    v
JDBC Connection / pgJDBC
    |
    v
PostgreSQL

After migration:

Hibernate / JPA
    |
    +-- validate and use the schema Flyway produced
```

### 2.2 Flyway versus Liquibase

Both solve database versioning. Flyway is commonly centered on ordered SQL migrations and a straightforward history model. Liquibase also supports structured changelogs and a broad change abstraction. Either can be used well. This guide chooses Flyway; a project should not add both simply to compare them.

## 3. Spring Boot + Flyway startup flow

**⭐⭐⭐⭐⭐ MUST KNOW**

In the common single-`DataSource` setup:

```text
SpringApplication starts
    |
    v
Spring Boot reads datasource configuration
    |
    v
HikariDataSource is created
    |
    v
Flyway discovers classpath:db/migration
    |
    v
Flyway acquires schema-history coordination/lock
    |
    +-- validates applied files and checksums
    +-- calculates pending migrations
    +-- runs pending migration SQL through JDBC
    +-- records successful applications
    |
    v
Hibernate builds the EntityManagerFactory
    |
    v
ddl-auto=validate checks mapped schema expectations
    |
    v
Application context finishes; HTTP traffic can be served
```

Flyway normally runs during **application initialization**, not once per controller call and not once per `@Transactional` service method.

```text
STARTUP PHASE                  REQUEST PHASE
-------------                  -------------
Flyway migrates                Controller
Hibernate validates               -> service proxy
Web application becomes ready     -> business transaction
```

If a required migration fails, failing startup is normally safer than accepting traffic with a schema that the code does not expect.

## 4. Adding Flyway to this Spring Boot project

**⭐⭐⭐⭐⭐ MUST KNOW**

The inspected project uses Spring Boot **4.1.1**. Boot 4 provides a Flyway starter; modern Flyway also uses a database-specific PostgreSQL module.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

Do not add versions when using the project's Spring Boot parent. Its dependency management keeps the starter, Flyway core, and PostgreSQL module compatible. In the inspected effective model, Boot 4.1.1 manages Flyway 12.4.0; re-check after a Boot upgrade rather than freezing that transitive version in this guide.

Older Spring Boot examples often add `flyway-core` directly. For this Boot 4.1 project, `spring-boot-starter-flyway` is the clearer integration dependency and already brings Flyway core and Boot's Flyway auto-configuration module. The PostgreSQL database module remains explicit.

Default location:

```text
src/main/resources/db/migration/
```

With the starter, a configured `DataSource`, and migrations in the default location, Boot auto-configures Flyway. No manual `Flyway` bean, startup runner, or custom transaction code is needed for this exercise.

## 5. Versioned migrations

**⭐⭐⭐⭐⭐ MUST KNOW**

Example names:

```text
V1__create_books.sql
V2__add_optimistic_lock_version.sql
V3__create_transaction_audits.sql
```

```text
V 1 __ create_books .sql
| | |       |          |
| | |       |          +-- SQL file
| | |       +------------- readable description
| | +--------------------- two underscores
| +----------------------- version
+------------------------- versioned migration prefix
```

Flyway orders files by parsed migration version, not by the order returned from the filesystem. It applies each version at most once to a given schema history.

Versions can contain segments, such as `V1_1` and `V1_2`, but a beginner-friendly project can use monotonically increasing integers:

```text
V1, V2, V3, V4, ...
```

Team rule: choose a convention, check the branch before assigning a number, and resolve duplicate versions before merge. Timestamp-like versions can reduce collisions on larger teams, but they do not remove the need for review.

By default, Flyway can ignore SQL resources whose names do not match its migration convention. In a learning/team project, make mistakes fail fast:

```yaml
spring:
  flyway:
    validate-migration-naming: true
```

Then a file such as `V1_create_books.sql` is reported as invalid instead of silently disappearing from the resolved migration list.

Also keep normal version ordering explicit. With `out-of-order: false`, if V5 is already applied and a new V4 appears later, Flyway does not treat V4 as the ordinary next migration. Do not enable out-of-order execution casually: environments can acquire different installation orders and become harder to reason about.

## 6. `flyway_schema_history`

**⭐⭐⭐⭐⭐ MUST KNOW**

Flyway normally creates a table named `flyway_schema_history`. It is Flyway's ledger for that schema.

Important columns include:

| Column | Meaning |
|---|---|
| `installed_rank` | Order in which history entries were installed |
| `version` | Version for a versioned migration; repeatables have no version |
| `description` | Parsed human-readable description |
| `type` | Migration type, such as SQL or baseline |
| `script` | Script name |
| `checksum` | Fingerprint of migration content where applicable |
| `installed_by` | Database user that installed it |
| `installed_on` | Installation timestamp |
| `execution_time` | Time spent, typically recorded in milliseconds |
| `success` | Whether that recorded execution succeeded |

Example reasoning:

```text
Files available:    V1  V2  V3
History contains:   V1  V2
                              ^
Pending:                    V3
```

Do not manually insert, delete, or rewrite history rows to make an error disappear. First determine why the files and database disagree.

## 7. Checksums

**⭐⭐⭐⭐⭐ MUST KNOW**

A checksum is a fingerprint calculated from migration content. Flyway records it when applying a migration and recalculates it during validation.

```text
V2 at application time      stored checksum = 184...
V2 in current checkout      current checksum = 927...
                                      |
                                      v
                              checksum mismatch
```

The mismatch is valuable evidence: the file now claiming to be V2 is not the file that built this database's V2 state.

The normal shared-environment rule is:

```text
Do not edit applied V2.
Create V4__correct_the_problem.sql.
```

Restoring an accidentally edited file is usually safer than immediately using `repair`, because restoring preserves the truthful relationship between file history and database history.

## 8. Immutable, append-only migration history

**⭐⭐⭐⭐⭐ MUST KNOW**

Think of two related histories:

```text
Git history                        Database history
-----------                        ----------------
application revision A             V1 applied
application revision B             V2 applied
application revision C             V3 applied
```

Versioned migration files are an append-only database evolution log once shared:

```text
BAD
V1 applied -> V2 applied -> silently rewrite V1

GOOD
V1 applied -> V2 applied -> V3 corrects the old design
```

Why? A rewritten V1 may build a fresh developer database differently from a production database that ran the original V1. Equal version numbers would then hide unequal schemas.

An unapplied, unshared local draft can still be edited. The immutability boundary is not mystical; it begins when other durable environments or teammates can have applied the migration.

## 9. `migrate`

**⭐⭐⭐⭐⭐ MUST KNOW**

Conceptually, `migrate` does this:

```text
Database history: V1, V2
Resolved files:   V1, V2, V3, V4

validate known history
skip V1
skip V2
run V3
record V3
run V4
record V4
```

It does not blindly execute every file on every startup. The history table plus version ordering determine what is pending.

Flyway coordinates access to its history so that concurrent application starts do not casually run the same migration twice. That does not make arbitrary migration SQL concurrency-safe; operational deployment still needs planning.

## 10. Flyway validation versus Hibernate validation

**⭐⭐⭐⭐⭐ MUST KNOW**

These checks answer different questions:

| Validator | Main question | Typical failure |
|---|---|---|
| Flyway `validate` | Do resolved migration files agree with recorded migration history? | Applied checksum differs, an applied migration is missing locally, or version metadata conflicts |
| Hibernate `ddl-auto: validate` | Does the current database schema satisfy Hibernate's mapped entity expectations? | A mapped table/column/type is missing or incompatible |

```text
Flyway validation
    migration files <-> flyway_schema_history

Hibernate validation
    JPA mappings <-> actual PostgreSQL schema
```

Examples:

- Editing applied V2 may make **Flyway** fail before any new migration runs.
- A successful V4 that accidentally drops `books.version` may let Flyway finish, then make **Hibernate** fail because `@Version` expects that column.
- A valid extra index normally affects neither entity mapping nor REST JSON.

Flyway validates migration provenance. Hibernate validates enough schema shape for ORM use. Neither replaces database constraints or application tests.

One Flyway 12 nuance is worth knowing:

- a standalone `validate` operation can report a resolved-but-unapplied migration as `Pending`/a validation concern;
- validation performed inside `migrate` intentionally ignores the expected pending state, validates the already-applied lineage, and then applies the pending migrations.

Therefore “pending” is normal input to `migrate`; it is not a checksum mismatch.

## 11. Migration failure

**⭐⭐⭐⭐⭐ MUST KNOW**

Suppose the schema is empty, no migration history exists yet, and files V1 through V4 are pending:

```text
V1 succeeds and commits       [recorded]
V2 succeeds and commits       [recorded]
V3 fails                      [startup stops]
V4 does not run               [still pending]
```

By default, Flyway generally treats each migration as its own unit rather than wrapping every pending version in one giant transaction. Therefore earlier successful migrations normally remain applied when a later migration fails.

With PostgreSQL and transactional SQL/DDL, the failed migration's changes can usually roll back together. In that common case, a durable failed V3 row may not remain; V3 remains pending for the next attempt. On a database or statement that cannot be transactional, partial changes and a failed history record may remain and require deliberate cleanup.

Important consequences:

- the Spring application context normally fails to finish;
- Hibernate validation and request handling do not proceed normally;
- later migrations are not applied;
- operators inspect both the Flyway error and actual PostgreSQL state before changing anything.

This fail-fast behavior prevents code expecting V4 from serving traffic against a database stuck at V2.

## 12. Flyway and database transactions

**⭐⭐⭐⭐⭐ MUST KNOW**

Reconnect to JDBC:

```text
Flyway chooses pending migration
    -> obtains JDBC Connection
    -> BEGIN / auto-commit management
    -> execute migration statements
    -> record history
    -> COMMIT

on failure, when transactional:
    -> ROLLBACK
```

Migration transaction behavior depends on:

- Flyway configuration;
- database capabilities;
- whether the specific statements can run in a transaction;
- optional grouping settings.

PostgreSQL supports transactional behavior for many DDL statements, which makes controlled failure experiments particularly useful. It still has exceptions: for example, some operational forms such as `CREATE INDEX CONCURRENTLY` cannot run inside a normal transaction block. Never generalize PostgreSQL behavior to every database.

Default mental model for this exercise:

```text
one versioned SQL migration
    -> one database transaction when PostgreSQL and its statements support it
    -> success: schema change and history record commit
    -> failure: that migration rolls back
```

This is unrelated to a request-time Spring service transaction:

| Migration transaction | Business transaction |
|---|---|
| Started by Flyway during initialization | Started by Spring's transaction interceptor around a service call |
| Evolves schema/reference data | Implements a use case such as transferring stock |
| Not controlled by service `@Transactional` | Uses `@Transactional` metadata and a transaction manager |
| Normally finishes before HTTP traffic | Normally runs while handling application work |

## 13. Flyway, `DataSource`, JDBC, and HikariCP

**⭐⭐⭐⭐ IMPORTANT**

In this project, Boot normally lets Flyway use the primary `DataSource`:

```text
Flyway
  -> javax.sql.DataSource
  -> HikariDataSource
  -> leases JDBC Connection
  -> pgJDBC sends SQL
  -> PostgreSQL executes it
  -> COMMIT or ROLLBACK
  -> connection returned to HikariCP
```

Flyway is not a pool. HikariCP knows nothing about V1 or checksums; it only manages connections. Flyway knows nothing about HTTP controllers; it uses connectivity during startup.

Startup migrations can still affect resources:

- a long DDL or data migration keeps a connection occupied;
- locks can block other sessions;
- multiple application replicas may wait for migration coordination;
- increasing pool size does not make unsafe or slow SQL safe.

The common flow runs migrations before normal traffic in that application instance, but it may run while **older deployed instances** still serve traffic. That compatibility concern matters in production.

## 14. Flyway versus `schema.sql`

**⭐⭐⭐⭐⭐ MUST KNOW**

The current project has:

```text
src/main/resources/schema.sql
spring.sql.init.mode: always
```

Its SQL describes the present shape, including a redundant create/alter pattern needed for an older named volume. Flyway replaces that evolving snapshot with explicit history:

```text
src/main/resources/db/migration/
  V1__create_books.sql
  V2__add_optimistic_lock_version.sql
  V3__create_transaction_audits.sql
```

Once Flyway becomes the schema owner, do not leave `schema.sql` independently changing the same tables. Two initialization mechanisms create ambiguity:

```text
Who created books.version?
Was V2 actually needed?
Did schema.sql run before or after Flyway?
Why does history say V2 is pending although the column exists?
```

Safe transition:

1. inspect and preserve all useful SQL;
2. express that history as migrations;
3. prove migrations build a clean database;
4. disable basic SQL initialization;
5. remove or archive `schema.sql` outside the runtime classpath according to team policy.

Never delete the only copy of schema logic before it has been reconstructed and verified.

## 15. Flyway with Hibernate `ddl-auto`

**⭐⭐⭐⭐⭐ MUST KNOW**

Relevant Hibernate modes:

| Mode | High-level behavior | Fit with Flyway ownership |
|---|---|---|
| `create` | Recreates schema at startup | Competes with/defeats migration history |
| `create-drop` | Creates, then drops around lifecycle | Test/demo behavior, not persistent migration ownership |
| `update` | Tries to mutate schema to match mappings | Creates unversioned changes and unclear ownership |
| `validate` | Checks mappings against schema without changing it | Strong common pairing with Flyway |
| `none` | No Hibernate schema action | Valid option when validation is handled elsewhere |

Recommended ownership:

```text
Flyway       changes schema deliberately
Hibernate    validates and uses schema
PostgreSQL   enforces actual constraints
```

For this project:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Using Flyway and `ddl-auto: update` together asks two independent systems to evolve the schema. Even if a startup appears successful, no reviewed migration may explain Hibernate's change.

## 16. Existing databases and named volumes

**⭐⭐⭐⭐⭐ MUST KNOW**

The project's Docker named volume can outlive application rebuilds and `docker compose down`. It may already contain:

- `books` and data;
- `books.version`;
- `transaction_audits`;
- no `flyway_schema_history`.

Flyway then sees a non-empty schema without a Flyway lineage. It cannot safely infer which historical scripts produced that schema.

### Option A — new disposable local practice database

Create a separate empty database, point the application at it, and let Flyway replay V1 onward.

Advantages:

- no old application data needs to be deleted;
- every migration is actually tested;
- history contains each version;
- drift is easy to see.

This is the preferred learning path.

### Option B — deliberately baseline a verified existing schema

After comparing every relevant table, column, constraint, and data assumption, tell Flyway that the existing schema represents a chosen version. Flyway records a baseline marker, then runs migrations above that version.

This is an adoption operation, not a shortcut around uncertainty.

### `baselineOnMigrate`

When enabled, Flyway can automatically baseline a non-empty schema that lacks history during `migrate`. That convenience removes an important wrong-database safety check. Pointing the application at the wrong non-empty database could cause Flyway to accept it as a legitimate baseline and continue with later changes.

Keep this false for the main exercise:

```yaml
spring:
  flyway:
    baseline-on-migrate: false
```

Never use a baseline as proof that the existing schema is correct. Flyway records the declaration; a human/process must verify the state first.

| Baseline setting/concept | Meaning |
|---|---|
| Baseline version | The version the verified pre-existing schema is declared to represent; migrations at or below it are skipped there |
| Baseline description | Human-readable text stored with the baseline history marker |
| Explicit `baseline` operation | A deliberate administrative command that creates the marker |
| `baselineOnMigrate` | Automation that may create the marker as part of `migrate`; convenient but riskier if the target is wrong |

Flyway's ordinary default baseline version is `1`. Declaring the current Book Catalog as V3 therefore requires explicitly selecting baseline version `3` (for example, the CLI's `baselineVersion=3`). Choosing the wrong version can incorrectly suppress migrations at or below that version.

## 17. Baseline versus migration

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
Baseline
  "Treat the schema already here as version 3."
  No V1/V2/V3 schema SQL is replayed.

Migration
  "Execute SQL that changes version 3 state into version 4 state."
```

Example:

```text
Existing database already matches V1 + V2 + V3
    -> explicit baseline at 3
    -> history gains a baseline marker for 3
    -> V1/V2/V3 are not executed there
    -> V4 and later may execute
```

On a clean database:

```text
no baseline
    -> run V1
    -> run V2
    -> run V3
    -> run V4
```

A baseline does not create missing tables, backfill missing data, validate every definition, or convert a random schema into V3.

Flyway 12 also supports **baseline migration files** with names such as `B3__current_schema.sql`. Those build a new environment from a consolidated baseline script and are different from:

- the `baseline` command/history marker used to adopt an already-existing schema; and
- `baselineOnMigrate`, which automates that adoption decision.

`B...` files are out of scope for this beginner exercise; it keeps the complete V1–V6 history visible.

## 18. Repeatable migrations

**⭐⭐⭐ NICE TO KNOW**

Repeatable migrations have a description but no version:

```text
R__book_stock_summary_view.sql
```

They behave differently from versioned migrations:

| Versioned | Repeatable |
|---|---|
| Runs once for one version | Runs initially, then again when its checksum changes |
| Ordered by version | Run after pending versioned migrations |
| Best for schema evolution | Useful for replaceable definitions |
| Applied file should not be edited | Editing is the intended trigger for re-execution |

Reasonable uses include:

- `CREATE OR REPLACE VIEW`;
- replaceable functions or procedures;
- carefully managed reference objects.

Example:

```sql
CREATE OR REPLACE VIEW book_stock_summary AS
SELECT author,
       COUNT(*) AS book_count,
       SUM(stock)::BIGINT AS total_stock
FROM books
GROUP BY author;
```

Changing ordinary table structure should normally remain versioned. A repeatable table script can make destructive re-execution surprisingly easy.

## 19. Data migrations

**⭐⭐⭐⭐ IMPORTANT**

A migration can evolve schema, data, or both:

```text
Schema migration: add books.status
Data migration:   populate status for existing rows
Constraint step:  make status NOT NULL
```

Small example:

```sql
ALTER TABLE books ADD COLUMN status VARCHAR(30);

UPDATE books
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE books ALTER COLUMN status SET DEFAULT 'ACTIVE';
ALTER TABLE books ALTER COLUMN status SET NOT NULL;
```

The order matters. PostgreSQL checks existing data when enforcing the constraint.

Large production data changes need more planning than “put a huge `UPDATE` in startup”:

- transaction log and disk growth;
- row and table locks;
- migration duration and application startup timeout;
- old/new application compatibility;
- batching, monitoring, restartability, and recovery.

Those concerns may lead to a separate operational backfill process, but the final schema transition must still have a traceable migration plan.

## 20. Index migrations

**⭐⭐⭐⭐ IMPORTANT**

An index is database schema and belongs in migration history.

```text
V4__add_books_author_index.sql
```

```sql
CREATE INDEX idx_books_author
    ON books (author);
```

That migration demonstrates an important property: schema can evolve without changing a JPA entity or REST contract.

The current repository has an exact-author query, so an author index is plausible. Whether PostgreSQL actually uses it depends on data size, selectivity, query shape, statistics, and the planner. A migration tool records the index; it does not prove the index is useful.

For large live tables, index creation can be expensive and lock-sensitive. PostgreSQL's `CONCURRENTLY` option changes operational behavior and cannot run in a normal transaction block; treat that as an environment-specific production design, not a word to add blindly.

## 21. Adding `NOT NULL` columns safely

**⭐⭐⭐⭐⭐ MUST KNOW**

This can fail against a populated table:

```sql
ALTER TABLE books
ADD COLUMN category VARCHAR(100) NOT NULL;
```

Every existing row would immediately need a non-null category, but no value was supplied.

A safer general sequence is:

```text
1. Add a compatible nullable column or an intentional default.
2. Backfill every existing row.
3. Verify no NULL remains.
4. Set a default for future inserts if the application will omit the column.
5. Enforce NOT NULL.
```

```sql
ALTER TABLE books ADD COLUMN category VARCHAR(100);

UPDATE books
SET category = 'UNCLASSIFIED'
WHERE category IS NULL;

ALTER TABLE books ALTER COLUMN category SET DEFAULT 'UNCLASSIFIED';
ALTER TABLE books ALTER COLUMN category SET NOT NULL;
```

Prediction checkpoint: if Hibernate does not map `category`, can inserts still succeed? Only if PostgreSQL can supply a valid value—for example, through the retained default—or the insert is changed to provide it. “Hibernate ignores an extra column” does not override a database `NOT NULL` constraint.

## 22. Renaming and dropping columns

**⭐⭐⭐ NICE TO KNOW / ⭐⭐ FUTURE KNOWLEDGE for zero-downtime rollout**

Destructive changes are riskier because old application instances, reports, scripts, and data may still depend on the old structure.

Use an expand-contract mental model:

```text
Phase 1 — expand
add the new column/table without removing the old one

Phase 2 — compatibility
deploy code that can work during the transition

Phase 3 — migrate data
backfill and verify

Phase 4 — contract
remove the old structure in a later deployment
```

An immediate `DROP COLUMN` may be correct for a local exercise, yet unsafe during a rolling production deployment. Designing fully zero-downtime schema changes is future knowledge; the must-know lesson is to separate additive and destructive phases when multiple application versions can overlap.

## 23. Multiple developers and migration conflicts

**⭐⭐⭐⭐ IMPORTANT**

Common branch collision:

```text
main currently ends at V4

Developer A: V5__add_category.sql
Developer B: V5__add_search_index.sql
```

Both files cannot represent one unique version in the same migration stream.

Before merge:

1. fetch/rebase or merge the latest branch;
2. inspect `db/migration`;
3. rename one **unapplied/unshared** migration to the next available version;
4. review dependencies between the SQL files;
5. run from a clean database and from the previous shared version.

If either V5 has already reached a shared environment, do not rewrite that history casually. Coordinate a forward correction.

Teams with many concurrent branches may use timestamp-based versions or a version-allocation convention. Git resolves file text; the team must resolve database ordering semantics.

## 24. Flyway commands and operations

**⭐⭐⭐⭐ IMPORTANT**

The exact invocation can be through Spring Boot startup, Flyway CLI, Maven/Gradle plugin, or an operations pipeline. The operation meanings remain the same.

| Operation | What it does | Typical use | Danger |
|---|---|---|---|
| `migrate` | Validates as configured, then applies pending migrations | Normal deployment/startup | Medium: executes schema/data SQL |
| `validate` | Compares resolved migrations with history; standalone validation can report pending resolved files, while migrate-time validation expects them | CI, startup, diagnosis | Low: read/compare behavior |
| `info` | Reports applied, pending, failed, ignored, or repeatable states | Diagnosis and release review | Low |
| `baseline` | Records an existing schema as a chosen starting version | Deliberate adoption of a verified database | High if version/schema assumption is wrong |
| `repair` | Repairs metadata in the schema-history table | Controlled recovery after understanding mismatch/failure | High if used to conceal drift |
| `clean` | Drops objects in configured schemas | Rare disposable-database reset | **Destructive** |

Spring Boot normally calls `migrate` automatically during startup. It does not make `clean`, `repair`, or `baseline` part of normal request processing.

## 25. `repair`

**⭐⭐⭐ NICE TO KNOW, HIGH CAUTION**

`repair` changes schema-history metadata: it removes failed history entries, realigns stored checksums/descriptions/types with resolved migrations, and marks missing applied migrations as deleted. It must use the same migration locations as `migrate`. Any partially created or leftover database objects still require deliberate manual reconciliation.

It does **not** inspect arbitrary partial DDL and automatically reconstruct the intended database.

Bad response to a checksum mismatch:

```text
"Startup is red; run repair until it is green."
```

Controlled example:

1. confirm that an applied file was intentionally and legitimately changed—not merely edited by mistake;
2. compare actual database state with the intended state;
3. understand every environment affected;
4. back up where appropriate;
5. only then consider whether repairing metadata is the correct operation.

For the exercise's accidental V4 edit, the correct first action is to restore V4. The desired new change goes into V5.

## 26. `clean`

**⭐⭐⭐⭐ IMPORTANT, DESTRUCTIVE**

> **Warning:** Flyway `clean` can drop the objects in its configured schemas. It is not rollback, undo, or routine startup housekeeping.

Keep it disabled in normal configuration:

```yaml
spring:
  flyway:
    clean-disabled: true
```

Even on a developer machine, first identify the exact database and what would be lost. A Docker named volume may contain several databases; deleting the volume can destroy more than one practice schema. A separate disposable database is easier to reason about.

Production/shared environments should enforce this operationally as well as by configuration and permissions. One property is not a complete authorization model.

## 27. Practical Spring Boot configuration

**⭐⭐⭐⭐ IMPORTANT**

Focused, explicit learning configuration:

```yaml
spring:
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

  jpa:
    hibernate:
      ddl-auto: validate
```

| Property | Intent |
|---|---|
| `enabled` | Enables Boot's Flyway integration; true is the normal default when available |
| `locations` | Where migrations are resolved; default is `classpath:db/migration` |
| `validate-on-migrate` | Validate known history before migration; normally keep enabled |
| `validate-migration-naming` | Fail fast for incorrectly named migration resources rather than silently ignore them |
| `baseline-on-migrate` | Automatically baseline qualifying non-empty schemas; keep false unless deliberately adopting one |
| `out-of-order` | Whether a newly discovered lower version may run after a higher version; keep false for a simple ordered history |
| `clean-disabled` | Prevent Flyway clean through this configuration; keep true outside tightly controlled disposable work |

These explicit values teach ownership. A mature project may omit properties equal to safe defaults, but should not add duplicate Flyway credentials when the primary `DataSource` is intended.

## 28. Migration SQL best practices

**⭐⭐⭐⭐⭐ MUST KNOW**

- Give every version one clear purpose where practical.
- Use descriptive names: `V7__add_books_status.sql`, not `V7__changes.sql`.
- Review migration SQL like application code.
- Never silently rewrite applied shared history.
- Test from an empty database **and** from the previous production-like version.
- Make data assumptions explicit and query them before enforcing constraints.
- Prefer deterministic SQL. Do not use `IF EXISTS` merely to hide unexpected drift.
- Keep credentials, tokens, and environment secrets out of migration files.
- Estimate lock scope, runtime, table size, and transaction-log impact.
- Decide recovery or forward-fix steps before deployment.
- Ensure the application version is compatible with both sides of a rolling transition where needed.
- Use database constraints for database invariants; JPA annotations alone are not a migration.

Idempotence is not automatically correctness. A versioned migration already has “apply once” semantics through history. `IF NOT EXISTS` can turn a wrong pre-existing object into a silent success.

## 29. Migration rollback versus application rollback

**⭐⭐⭐⭐⭐ MUST KNOW**

Two meanings of “rollback” are easily confused.

### Request-time rollback

```text
@Transactional stock transfer
    -> UPDATE A
    -> UPDATE B
    -> exception
    -> database ROLLBACK
```

That transaction has not been committed, so PostgreSQL can discard its changes.

### Deployed migration recovery

```text
V3 applied and committed in production
    -> application discovers design problem later
    -> create V4 to correct it
```

Once V3 committed—and perhaps transformed or deleted data—there is no universal magic reverse. Flyway workflows commonly use forward migrations. Some changes can be reversed manually; others need backups, restore procedures, compensating data logic, or coordinated application rollback.

Do not promise that every migration has an automatic “down” operation. A transaction can roll back a migration **while it is failing before commit**; that is different from reversing a migration deployed yesterday.

## 30. Production deployment mental model

**⭐⭐⭐⭐ IMPORTANT**

Simple startup-migration flow:

```text
deploy new application artifact
    -> instance connects to intended database
    -> Flyway validates history
    -> pending migrations run
    -> Hibernate validates mappings
    -> application becomes ready
    -> traffic arrives
```

The simple diagram hides a major compatibility question: older instances may still be serving traffic while one new instance migrates the shared database.

Prefer backward-compatible, additive changes when overlap is possible:

```text
add nullable/default-compatible column
    -> deploy code that can use it
    -> backfill/verify
    -> enforce stronger rule later
    -> remove obsolete structure in a later release
```

Migration ownership also varies by organization. Migrations may run inside application startup, in a single release job, or through an operations pipeline. What must remain consistent is the versioned history and coordination between schema and application code.

**⭐⭐ FUTURE KNOWLEDGE:** zero-downtime rollout design, online large-table changes, deployment orchestration, and multi-service schema ownership deserve separate treatment.

## 31. Common mistakes

**⭐⭐⭐⭐⭐ MUST KNOW**

| Mistake | Why it is wrong | Better response |
|---|---|---|
| Edit an applied versioned migration | Fresh and existing databases can diverge; checksum fails | Restore it and add a new version |
| Delete history rows | Erases Flyway's evidence without undoing schema | Diagnose files and actual schema first |
| Manually patch production only | Other environments and future databases cannot reproduce it | Encode a reviewed migration |
| Use Flyway plus `ddl-auto: update` | Two schema owners create untracked changes | Flyway migrates; Hibernate validates |
| Keep active `schema.sql` and Flyway for the same tables | Ordering and ownership become ambiguous | Move logic into migrations; disable basic initialization |
| Assume every file runs every startup | Ignores schema history | Inspect applied and pending state |
| Assume SQL execution means commit | A later statement can fail and roll back transactional DDL | Separate statement execution from transaction commit |
| Enable `baselineOnMigrate` everywhere | Can accept the wrong non-empty database | Baseline only after exact verification |
| Run `repair` to silence any error | Can make metadata agree while schema remains wrong | Understand and reconcile state first |
| Run `clean` on valued data | Drops database objects | Restrict it to explicitly disposable databases |
| Put secrets in SQL | Commits credentials into source/history | Use secure configuration/secret management |
| Assume all databases behave like PostgreSQL | DDL transaction behavior differs | Test against the actual engine/version |
| Add `NOT NULL` without considering rows | Existing data may violate the new rule | Add, backfill, verify, constrain |
| Run a huge backfill during every instance startup | Long locks/startup and resource pressure | Plan an operational strategy |
| Reuse a teammate's migration version | Causes duplicate-version conflict | Coordinate and renumber before application |
| Misspell a migration filename | It may be ignored when naming validation is off | Enable naming validation and fix the resource name |
| Add a lower version after a higher one is applied | It becomes out-of-order rather than normal next history | Coordinate versions; do not enable out-of-order casually |
| Treat an entity change as a schema change | JPA source does not alter a Flyway-owned DB | Add the corresponding migration |
| Use `IF NOT EXISTS` everywhere | Can hide drift rather than prove expected state | Prefer deterministic migrations |
| Point at the wrong database | Correct SQL can damage the wrong environment | Verify URL, database, schema, user, and backups |

## 32. Debugging Flyway

**⭐⭐⭐⭐⭐ MUST KNOW**

### 32.1 Evidence sequence

1. Find the first Flyway error in startup logs, not only the final Spring stack trace.
2. Verify the JDBC URL, database name, schema, and database user—without printing a password.
3. List resolved migration filenames and check naming.
4. Query `flyway_schema_history`.
5. Compare the applied version/checksum with the local files.
6. Inspect actual PostgreSQL tables, columns, constraints, and indexes.
7. Determine whether failure was transactional and whether partial objects remain.
8. If Flyway succeeded but startup still failed, read the Hibernate validation error separately.

Useful history query:

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

PostgreSQL schema inspection:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'books'
ORDER BY ordinal_position;

SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'books'
ORDER BY indexname;
```

Development logging:

```yaml
logging:
  level:
    org.flywaydb: DEBUG
    org.hibernate.SQL: DEBUG
```

Use DEBUG temporarily. Migration logs can expose object names and SQL details; excessive logging creates noise and may be inappropriate in production.

### 32.2 Identify the failing layer

| Symptom | Likely layer |
|---|---|
| “Unsupported Database” or driver/connectivity failure | Flyway database module, pgJDBC, URL, credentials, PostgreSQL availability |
| Checksum/description mismatch | Flyway resolved files versus history |
| SQL syntax or missing relation during `Migrating schema...` | Migration SQL/PostgreSQL |
| Flyway reports success, then “Schema-validation” fails | Hibernate mapping versus resulting schema |
| Application starts, request later rolls back | Business `@Transactional` path, not startup migration |

## 33. Interview-relevant knowledge

**⭐⭐⭐⭐ IMPORTANT**

### Questions with compact answers

**Why do we need migrations?**  
Persistent databases outlive application releases. Migrations provide an ordered, repeatable, reviewable path from one known schema state to the next.

**What is Flyway?**  
A migration tool that resolves migration files, compares them with schema history, executes pending changes, and records results.

**How does Flyway know what ran?**  
From `flyway_schema_history`, including versions, scripts, checksums, order, timing, and success metadata.

**Why not edit an applied migration?**  
Its checksum and meaning are already recorded; editing makes environments with the same claimed version potentially different.

**Flyway versus Hibernate schema generation?**  
Flyway owns explicit versioned evolution. Hibernate maps runtime objects and can validate the resulting schema. `ddl-auto: update` is not a reviewed migration history.

**What is a baseline?**  
A declaration that a verified existing schema represents a selected starting version; it is not execution of earlier migrations.

**What is a repeatable migration?**  
An unversioned migration such as `R__view.sql` that reruns when its checksum changes, useful for replaceable objects.

**What happens when a migration fails?**  
Startup normally fails and later migrations stop. Transactional DDL may roll back that migration; behavior depends on the database and SQL.

**Schema migration versus data migration?**  
One changes structure; the other transforms or seeds data. A safe evolution may combine ordered steps of both.

**How do you add `NOT NULL` to populated data?**  
Add a compatible column, backfill, verify, provide future insert behavior if needed, then enforce the constraint.

**Why is `clean` dangerous?**  
It drops objects in configured schemas and can destroy valued data.

**Does Flyway use Spring `@Transactional`?**  
Not for migration boundaries. Flyway manages JDBC/database migration transactions during startup; service proxies manage business transactions later.

## 34. Final review

**⭐⭐⭐⭐⭐ MUST REMEMBER**

### 34.1 Complete mental model

```text
Developer writes V4__add_books_author_index.sql
    |
    v
Git versions and reviewers inspect the file
    |
    v
Spring Boot starts with a configured HikariDataSource
    |
    v
Flyway resolves db/migration and reads flyway_schema_history
    |
    +-- validates applied files/checksums
    +-- identifies V4 as pending
    |
    v
Flyway leases JDBC connection from HikariCP
    |
    v
pgJDBC sends SQL to PostgreSQL
    |
    +-- success -> execute schema SQL -> insert history row -> COMMIT both
    +-- failure -> ROLLBACK when supported; startup fails
    |
    v
connection returns to pool
    |
    v
Hibernate validates JPA mappings against resulting schema
    |
    v
application becomes ready; later HTTP requests use normal service transactions
```

### 34.2 Migration lifecycle

```text
design change
 -> choose next unique version
 -> write deterministic SQL
 -> test against previous state
 -> review code + data/lock assumptions
 -> deploy to intended database
 -> Flyway validate
 -> execute migration SQL
 -> insert successful history record
 -> commit both when transactional
 -> never rewrite shared applied version
 -> create a later version for the next change
```

### 34.3 Must-remember rules

1. The database persists across application versions; migrations describe evolution through time.
2. Flyway changes schema; Hibernate should normally validate/use it.
3. `flyway_schema_history` is the source of Flyway's applied-state knowledge.
4. Applied shared versioned migrations are append-only history.
5. A checksum mismatch is evidence, not an inconvenience to silence.
6. Baseline means “accept verified existing state,” not “build that state.”
7. PostgreSQL can roll back much DDL, but not every database/statement behaves alike.
8. Migration transactions are not service `@Transactional` transactions.
9. Disable competing `schema.sql` initialization when Flyway takes ownership.
10. Keep `ddl-auto: validate`; avoid `update` as a second schema owner.
11. Consider existing rows, locks, duration, and old application instances.
12. Never use `clean`, `repair`, or automatic baseline without understanding the exact target and consequence.

### 34.4 Compact review table

| Concept | One-line memory aid |
|---|---|
| Versioned migration | Ordered, one-time forward change: `V4__description.sql` |
| Repeatable migration | Re-executes when content changes: `R__description.sql` |
| History | What Flyway believes this schema has applied |
| Checksum | Detects changed applied content |
| Pending | Resolved version not yet recorded as applied |
| Baseline | Accept verified pre-existing schema at a version |
| Validate | Migration files versus migration history |
| Hibernate validate | Entity mappings versus actual schema |
| Repair | History metadata repair, not automatic schema repair |
| Clean | Destructive removal of configured schema objects |
| Forward fix | New version corrects an already committed version |

### 34.5 Checklist for every new migration

- [ ] Is the target project/database/schema explicit?
- [ ] Is the version unique on the latest branch?
- [ ] Is the filename valid and descriptive?
- [ ] Does the SQL start from the actual previous state?
- [ ] Have existing rows and constraints been considered?
- [ ] Could the SQL lock a large table or run for a long time?
- [ ] Will old and new application versions remain compatible if they overlap?
- [ ] Was it tested from the previous migration version?
- [ ] Was a clean-database replay tested?
- [ ] Is recovery/forward-fix thinking documented?
- [ ] Are secrets absent?
- [ ] Did reviewers inspect SQL, not just Java?
- [ ] After application, does history show the expected checksum and success?
- [ ] Does Hibernate validation pass?

### 34.6 Final self-test

Explain this without notes:

> Spring Boot creates the primary Hikari-backed `DataSource`. During startup, its Flyway integration resolves versioned SQL, compares it with `flyway_schema_history`, validates checksums, and runs only pending changes through JDBC against PostgreSQL. Supported migration SQL commits with its history record; failure stops startup and rolls back the current migration when transactional DDL permits. Hibernate then validates its mappings against the resulting schema. Business `@Transactional` methods run later and are a different transaction lifecycle.

If every arrow and owner in that answer is clear, the central Flyway mental model is in place.
