# PostgreSQL SQL Syntax Review

A compact, practical reference for Java, JDBC, Spring Boot, Spring Data JPA, Hibernate, and Flyway work. This is a review sheet, not an introduction to relational databases.

The examples use modern PostgreSQL syntax and were checked against PostgreSQL 18 documentation. Always test migrations against the exact PostgreSQL major version used by the application.

## Priority legend

| Marker | Meaning |
|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Daily backend syntax or a critical safety rule |
| ⭐⭐⭐⭐ IMPORTANT | Frequent and interview-relevant |
| ⭐⭐⭐ USEFUL | Common in some applications |
| ⭐⭐ NICE TO KNOW | Learn after the core |
| ⭐ ADVANCED | Recognize it; study when needed |

## Example model and result vocabulary

Examples assume these logical tables unless a section creates them explicitly:

```text
users(id, username, email, active, created_at)
books(id, isbn, title, author, price, stock, created_at)
orders(id, user_id, status, total, created_at)
order_items(order_id, book_id, quantity, unit_price)
```

- A **result set** is a set of rows returned by a query.
- An **affected-row count** is what JDBC normally receives from `INSERT`, `UPDATE`, or `DELETE` without `RETURNING`.
- A **command tag** such as `CREATE TABLE`, `UPDATE 3`, or `COMMIT` is PostgreSQL's completion status. Many GUI tools display it as a message rather than a row.
- `?` in Java examples is a JDBC `PreparedStatement` placeholder, not literal PostgreSQL SQL sent directly through `psql`.
- Production queries should normally name the columns they need. This guide sometimes uses `SELECT *` only where the shape is the point of the example.

---

## 1. Database and schema basics

Schemas are namespaces inside one database. They are useful for separating modules, avoiding name collisions, controlling permissions, or supporting carefully designed multi-tenant layouts. Most application migrations operate inside an existing database; creating and dropping the database itself is normally deployment/administration work.

### `CREATE DATABASE`

**Priority:** ⭐⭐ NICE TO KNOW

**Syntax**

```sql
CREATE DATABASE database_name;
```

**Example**

```sql
CREATE DATABASE bookshop;
```

**What it does:** Creates a PostgreSQL database.  
**Return/result:** A `CREATE DATABASE` command result; no row set.  
**Practical use:** Initial local, test, or deployment setup.  
**Important note:** It requires suitable privilege and cannot run inside a transaction block, so it normally does not belong in a regular transactional Flyway migration.

### `DROP DATABASE`

**Priority:** ⭐⭐ NICE TO KNOW — DESTRUCTIVE

**Syntax and example**

```sql
DROP DATABASE IF EXISTS bookshop;
```

**What it does:** Removes the database and everything in it.  
**Return/result:** A `DROP DATABASE` command result.  
**Practical use:** Rebuilding disposable local/test environments.  
**Important note:** You cannot drop the database to which the current session is connected. It cannot run inside a transaction block. Verify the target name and environment first.

### `CREATE SCHEMA` and `DROP SCHEMA`

**Priority:** ⭐⭐⭐ USEFUL

**Syntax**

```sql
CREATE SCHEMA schema_name;
DROP SCHEMA [IF EXISTS] schema_name [RESTRICT | CASCADE];
```

**Example**

```sql
CREATE SCHEMA catalog;
CREATE TABLE catalog.books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title TEXT NOT NULL
);

-- RESTRICT succeeds for an empty schema:
CREATE SCHEMA scratch;
DROP SCHEMA scratch RESTRICT;
```

**What it does:** Creates or removes a namespace and its objects.  
**Return/result:** A DDL command result; no rows.  
**Practical use:** Separating `catalog`, `billing`, and `audit` objects in one database.  
**Important notes:** `RESTRICT` refuses to drop a nonempty/dependent schema and is the default. `CASCADE` also drops dependent objects and is dangerous.

### `SET search_path`

**Priority:** ⭐⭐⭐ USEFUL

**Syntax and example**

```sql
SET search_path TO catalog, public;

SELECT id, title
FROM books; -- resolves catalog.books first
```

**What it does:** Sets the ordered schemas searched for unqualified object names in the current session/transaction scope.  
**Return/result:** A `SET` command result.  
**Practical use:** Letting application SQL use `books` instead of `catalog.books`.  
**Important notes:** Schema-qualify objects in migrations when ambiguity would be risky. Do not put an untrusted, user-writable schema early in `search_path`; object shadowing can be a security problem.

---

## 2. Table creation and core column types

### `CREATE TABLE`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

**Syntax**

```sql
CREATE TABLE [IF NOT EXISTS] table_name (
    column_name data_type [column_constraint ...],
    ...,
    [table_constraint ...]
);
```

**Example**

```sql
CREATE TABLE books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(120) UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(120) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    stock INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

**What it does:** Defines a table, its columns, defaults, and constraints.  
**Return/result:** A `CREATE TABLE` command result; no row set.  
**Practical use:** The central DDL statement in initial Flyway migrations.  
**Important notes:** Database constraints protect data regardless of which application writes it. Java validation improves error messages but does not replace `NOT NULL`, `CHECK`, `UNIQUE`, or foreign keys.

### Common PostgreSQL types

**Priority:** ⭐⭐⭐⭐⭐ for the bold rows; ⭐⭐⭐ for the rest

| PostgreSQL type | Meaning | Typical Java mapping/use | Important note |
|---|---|---|---|
| `SMALLINT` | 2-byte signed integer | `short` / `Short` | Range is about ±32 thousand. |
| **`INTEGER`** | 4-byte signed integer | `int` / `Integer` | Normal counts and bounded numeric values. |
| **`BIGINT`** | 8-byte signed integer | `long` / `Long` | Common for IDs and large counters. |
| **`NUMERIC(p,s)`** | Exact decimal | `BigDecimal` | Prefer for money-like exact values; define precision and scale deliberately. |
| `REAL` | 4-byte approximate floating point | `float` / `Float` | Do not use for exact money. |
| `DOUBLE PRECISION` | 8-byte approximate floating point | `double` / `Double` | Approximate scientific/measurement values. |
| **`BOOLEAN`** | `TRUE`, `FALSE`, or `NULL` | `boolean` / `Boolean` | Use wrapper type if SQL `NULL` is meaningful. |
| `CHAR(n)` | Fixed length, blank-padded | `String` | Usually avoid unless fixed-width semantics are required. |
| **`VARCHAR(n)`** | Variable length with maximum | `String` | The limit is a business constraint, not a performance optimization. |
| **`TEXT`** | Variable-length text | `String` | Often preferable when no real maximum exists. |
| **`DATE`** | Calendar date | `LocalDate` | No time or time zone. |
| `TIME` | Time of day | `LocalTime` | No date; plain `TIME` has no time zone. |
| `TIMESTAMP` | Date and time without time zone | `LocalDateTime` | Represents a wall-clock value; it is not an instant. |
| **`TIMESTAMP WITH TIME ZONE`** / `TIMESTAMPTZ` | An instant displayed in the session time zone | `Instant` or `OffsetDateTime` | PostgreSQL stores the instant, not the original zone name/offset. |
| **`UUID`** | 128-bit identifier | `UUID` | Good for externally exposed/distributed IDs; larger indexes than `BIGINT`. |
| `JSON` | Validated JSON text | `String` or library-specific mapping | Preserves input text details; less efficient for most querying. |
| **`JSONB`** | Binary/decomposed JSON | JSON mapping/value object | Usually preferred for indexing and operators; key order/whitespace are not preserved. |
| `BYTEA` | Binary bytes | `byte[]` | Suitable for modest binary values; large-object/external storage may be better for huge files. |
| `type[]` | PostgreSQL array | JDBC `Array` or provider mapping | Useful for bounded scalar collections; relations are often better for entity data. |

Example declarations:

```sql
CREATE TABLE product_details (
    product_id BIGINT PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    metadata JSONB NOT NULL DEFAULT '{}'::JSONB,
    thumbnail BYTEA,
    tags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]
);
```

### `DROP TABLE`, `TRUNCATE TABLE`, and generic `ALTER TABLE`

**Priority:** ⭐⭐⭐⭐ IMPORTANT — potentially destructive

**Syntax and examples**

```sql
DROP TABLE IF EXISTS old_books RESTRICT;

TRUNCATE TABLE staging_books CONTINUE IDENTITY;

ALTER TABLE books
ADD COLUMN description TEXT;
```

**What they do:** `DROP` removes the table definition and data; `TRUNCATE` quickly removes all rows; `ALTER` changes the definition.  
**Return/result:** DDL command results; no row sets.  
**Practical use:** Schema evolution, clearing staging/test tables, and retiring obsolete tables.  
**Important notes:** These commands commonly take strong locks. PostgreSQL can roll back ordinary `DROP TABLE` and `TRUNCATE` operations when they run inside a transaction, but that is not a reason to use them casually. `CASCADE` may affect more objects than the named target.

---

## 3. PostgreSQL identity / auto-generated IDs

### `GENERATED ALWAYS AS IDENTITY`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

**Syntax and example**

```sql
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE
);
```

**What it does:** Connects the numeric column to an implicitly managed sequence and generates a value when the column is omitted. `ALWAYS` rejects ordinary explicit ID values.  
**Return/result:** On insert, the ID is stored; use `RETURNING id` to receive it.  
**Practical use:** Default choice for database-generated entity IDs.  
**Important note:** Identity alone does not guarantee uniqueness; pair it with `PRIMARY KEY` or `UNIQUE`. Exceptional data loading can explicitly use `OVERRIDING SYSTEM VALUE`:

```sql
INSERT INTO users (id, username)
OVERRIDING SYSTEM VALUE
VALUES (100, 'imported-user');
```

### `GENERATED BY DEFAULT AS IDENTITY`

**Priority:** ⭐⭐⭐⭐ IMPORTANT

**Syntax and example**

```sql
CREATE TABLE imported_users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username TEXT NOT NULL
);

INSERT INTO imported_users (id, username)
VALUES (500, 'legacy-user'); -- explicit ID is accepted
```

**What it does:** Generates an ID only when the insert does not provide one.  
**Return/result:** A stored generated or explicit value.  
**Practical use:** Imports or systems that legitimately accept upstream IDs.  
**Important note:** Explicit values can collide, and a high explicit value does not automatically make the backing sequence skip beyond it. Prefer `ALWAYS` when the database owns IDs.

### Identity versus `SERIAL` / `BIGSERIAL`

| Choice | Nature | Explicit insert behavior | Recommendation |
|---|---|---|---|
| `GENERATED ALWAYS AS IDENTITY` | SQL-standard identity backed by an owned sequence | Rejected unless overridden | Preferred default |
| `GENERATED BY DEFAULT AS IDENTITY` | SQL-standard identity backed by an owned sequence | Allowed | Useful for controlled imports |
| `SERIAL` / `BIGSERIAL` | PostgreSQL shorthand that creates a sequence and `DEFAULT nextval(...)` | Allowed | Maintain legacy schemas; use identity for new design |

Sequence values can have gaps after rollbacks, crashes, caching, or failed inserts. Never treat generated IDs as gapless invoice numbers or as proof of row count.

---

## 4. Constraints

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

### Syntax and complete example

```sql
CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    total NUMERIC(12,2) NOT NULL,

    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_orders_total CHECK (total >= 0),
    CONSTRAINT chk_orders_status
        CHECK (status IN ('NEW', 'PAID', 'CANCELLED'))
);

CREATE TABLE user_bookmarks (
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_bookmarks_user_book UNIQUE (user_id, book_id),
    CONSTRAINT fk_user_bookmarks_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT fk_user_bookmarks_book
        FOREIGN KEY (book_id)
        REFERENCES books(id)
);
```

Column-level forms are compact:

```sql
CREATE TABLE inventory (
    book_id BIGINT PRIMARY KEY REFERENCES books(id),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0)
);
```

Table-level forms are required for multi-column keys and are convenient when naming constraints.

| Constraint | Syntax | What it enforces | Result/use | Important note |
|---|---|---|---|---|
| `PRIMARY KEY` | `PRIMARY KEY (id)` | Unique, non-null row identity | Referenced by foreign keys; maps naturally to JPA `@Id` | One primary key per table; it may contain multiple columns. |
| `FOREIGN KEY` | `FOREIGN KEY (user_id) REFERENCES users(id)` | Child value must reference a parent | Referential integrity between entities | Index the child FK when joins/deletes need it; PostgreSQL does not create that child index automatically. |
| `UNIQUE` | `UNIQUE (user_id, book_id)` | No duplicate key combination | Business key/idempotency guarantee | By default, multiple `NULL`s do not conflict. Modern PostgreSQL also supports `UNIQUE NULLS NOT DISTINCT` when nulls should conflict. |
| `NOT NULL` | `title TEXT NOT NULL` | Column must have a value | Protects required entity fields | Validate in Java too, but keep the database rule. |
| `CHECK` | `CHECK (stock >= 0)` | Each row satisfies a Boolean expression | Protects ranges and allowed states | A check passes when its expression is `TRUE` **or `NULL`**; combine with `NOT NULL` when null is forbidden. |
| `DEFAULT` | `status TEXT DEFAULT 'NEW'` | Supplies a value when the column is omitted | Keeps inserts concise | It does not replace an explicitly inserted `NULL`. |

**Return/result:** Constraint DDL returns a command result. Violating DML fails with a PostgreSQL error; JDBC receives an `SQLException` (and a SQLSTATE).  
**Practical use:** Preserve invariants even across batch jobs, admin scripts, multiple services, and ORM code.  
**Important note:** Constraint names make migrations and production errors easier to understand. Do not rely only on controller/Bean Validation.

### Common foreign-key delete actions

```sql
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
```

- `CASCADE`: delete dependent rows too; suitable for true owned children such as `order_items` owned by an `order`, but dangerous for historical/business records.
- `SET NULL`: keep the child but remove the link; the FK column must allow `NULL`.
- `RESTRICT`: reject the parent delete while children exist.
- `NO ACTION`: the default; similar outcome in ordinary use, but unlike `RESTRICT` its check can be deferred when the constraint is declared deferrable. Section 36 compares them directly.

---

## 5. `ALTER TABLE`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW for migrations

### Common forms

```sql
-- Add/drop columns
ALTER TABLE books ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE books DROP COLUMN legacy_code;

-- Change a type; USING controls conversion of existing values
ALTER TABLE books
ALTER COLUMN price TYPE NUMERIC(14,2)
USING price::NUMERIC(14,2);

-- Defaults
ALTER TABLE books ALTER COLUMN stock SET DEFAULT 0;
ALTER TABLE books ALTER COLUMN stock DROP DEFAULT;

-- Nullability
ALTER TABLE books ALTER COLUMN isbn SET NOT NULL;
ALTER TABLE books ALTER COLUMN isbn DROP NOT NULL;

-- Constraints
ALTER TABLE books
ADD CONSTRAINT chk_books_stock CHECK (stock >= 0);

ALTER TABLE books DROP CONSTRAINT chk_books_stock;

-- Rename
ALTER TABLE books RENAME COLUMN author TO author_name;
ALTER TABLE books RENAME TO catalog_books;
```

**What it does:** Evolves an existing table definition.  
**Return/result:** An `ALTER TABLE` command result; no row set.  
**Practical use:** The most frequent command family in later Flyway migrations.  
**Important notes:**

- Type changes, nullability validation, and some defaults can scan/rewrite a large table or hold consequential locks. Rehearse large production migrations.
- `SET NOT NULL` fails if any existing row is null. Backfill first.
- `DROP COLUMN` destroys data and may invalidate application queries.
- Renames require coordinated application deployment unless old and new versions are compatible.
- Do not edit a Flyway migration already applied to shared environments. Add a new versioned migration.

A safe nullable → backfill → required pattern appears in Section 44.

---

## 6. `INSERT`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

### Syntax

```sql
INSERT INTO table_name (column1, column2)
VALUES (value1, value2);
```

### Single row

```sql
INSERT INTO books (title, author, price, stock)
VALUES ('Effective Java', 'Joshua Bloch', 45.00, 10);
```

### Multiple rows

```sql
INSERT INTO books (title, author, price, stock)
VALUES
    ('Clean Code', 'Robert C. Martin', 38.00, 7),
    ('Java Concurrency in Practice', 'Brian Goetz', 52.00, 4);
```

### `INSERT ... RETURNING`

```sql
INSERT INTO books (title, author, price, stock)
VALUES ('Effective Java', 'Joshua Bloch', 45.00, 10)
RETURNING id;
```

### `INSERT ... SELECT`

```sql
INSERT INTO archived_books (id, title, author)
SELECT id, title, author
FROM books
WHERE stock = 0;
```

**What it does:** Adds one or more rows; `INSERT ... SELECT` copies query output into compatible target columns.  
**Return/result:** Without `RETURNING`, an affected-row count/command tag. With `RETURNING`, a result set containing the requested inserted values.  
**Practical use:** Repository creates, batch inserts, migrations, and generated-ID retrieval.  
**Important notes:**

- Name target columns; relying on physical column order makes migrations brittle.
- Omitted columns use their defaults or `NULL`; explicitly supplied `NULL` does not invoke a default.
- Use JDBC parameters for application values. `RETURNING` can obtain IDs/defaults in the same round trip; check JDBC/framework support for consuming its result set.
- Multi-row insert is normally more efficient than many single-row round trips.

---

## 7. `UPDATE`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW — `WHERE` IS A SAFETY BOUNDARY

### Syntax and example

```sql
UPDATE table_name
SET column1 = expression1,
    column2 = expression2
WHERE condition
RETURNING column_list;
```

```sql
UPDATE books
SET stock = stock - 1
WHERE id = 10
RETURNING *;
```

A safer atomic inventory update also rejects an empty shelf:

```sql
UPDATE books
SET stock = stock - 1
WHERE id = ?
  AND stock > 0
RETURNING id, stock;
```

**What it does:** Changes columns in every row that satisfies `WHERE`. Expressions can use the row's previous values.  
**Return/result:** Normally an affected-row count. With `RETURNING`, a result set of updated values. An affected count of `0` is meaningful and must be handled.  
**Practical use:** State changes, optimistic operations, stock changes, and data migrations.  
**Important notes:**

- Omitting `WHERE` updates **every row**. Preview the predicate with `SELECT`, review the affected count, and use a transaction for risky manual work.
- `SET stock = stock - 1` is atomic at the row level; a Java read-then-write sequence can lose updates unless it locks or uses optimistic concurrency.
- Returning `*` is convenient while debugging; production code should return only needed columns.

---

## 8. `DELETE`, `TRUNCATE`, and `DROP`

### `DELETE`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW — DESTRUCTIVE

**Syntax and example**

```sql
DELETE FROM table_name
WHERE condition
RETURNING column_list;
```

```sql
DELETE FROM books
WHERE id = ?
RETURNING id, title;
```

**What it does:** Removes rows matching `WHERE`.  
**Return/result:** An affected-row count, or deleted-row values when `RETURNING` is present.  
**Practical use:** Removing an entity, clearing expired data, or recording exactly what was deleted.  
**Important note:** `DELETE FROM books;` is valid and deletes **all rows**. It is not the same as dropping the table.

### Comparison

| Command | Removes | Can filter? | Table remains? | Identity/sequence | Typical behavior/use |
|---|---|---:|---:|---|---|
| `DELETE FROM books WHERE ...` | Matching rows | Yes | Yes | Unchanged | Row-level deletion; fires normal delete triggers; returns affected count/`RETURNING` rows. |
| `DELETE FROM books` | All rows | No predicate present | Yes | Unchanged | Full row deletion through normal DML semantics. |
| `TRUNCATE TABLE books` | All rows | No | Yes | `CONTINUE IDENTITY` by default; optional `RESTART IDENTITY` | Fast bulk emptying; stronger lock and truncate-specific trigger/visibility behavior. |
| `DROP TABLE books` | Rows **and definition** | No | No | Owned identity sequence is removed with the table | Retire the object completely. |

```sql
TRUNCATE TABLE books RESTART IDENTITY;
DROP TABLE IF EXISTS books RESTRICT;
```

**Return/result:** `TRUNCATE` and `DROP` return command results, not rows.  
**Practical use:** `TRUNCATE` for disposable staging/test data; `DROP` in explicit schema-retirement migrations.  
**Important notes:** `TRUNCATE ... CASCADE` can empty referencing tables too. `DROP ... CASCADE` can remove dependent objects. Both demand environment and dependency checks.

---

## 9. `SELECT` basics

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

### Syntax

```sql
SELECT [DISTINCT] expression [AS alias], ...
FROM table_name [AS table_alias]
WHERE condition
ORDER BY expression [ASC | DESC] [NULLS FIRST | NULLS LAST]
LIMIT count OFFSET count;
```

### Example

```sql
SELECT id, title, author
FROM books
WHERE stock > 0
ORDER BY title ASC
LIMIT 10 OFFSET 20;
```

**What it does:** Builds a row set by choosing sources, filtering rows, projecting columns/expressions, sorting, and optionally limiting the result.  
**Return/result:** A result set; zero rows is a successful result.  
**Practical use:** Repository reads, reporting, troubleshooting, and nearly every backend request path.  
**Important notes:**

- Name required columns instead of routinely using `SELECT *`. This reduces data transfer and protects mappings from schema changes.
- SQL rows have no guaranteed order without `ORDER BY`.
- Add a unique tie-breaker for stable pages: `ORDER BY title, id`.
- `LIMIT` without deterministic ordering can return an arbitrary subset.

### `DISTINCT` and aliases

**Priority:** ⭐⭐⭐⭐ IMPORTANT

```sql
SELECT DISTINCT author
FROM books
ORDER BY author;

SELECT b.title AS book_title,
       b.price * 1.10 AS adjusted_price
FROM books AS b;
```

**What they do:** `DISTINCT` removes duplicate projected rows; `AS` gives a readable output or table alias.  
**Return/result:** A result set with the aliased column labels and, if requested, duplicates removed.  
**Practical use:** DTO-friendly labels and unique lookup values.  
**Important note:** `DISTINCT` can require sorting or hashing; do not use it to hide an incorrect join.

### Sort direction and null placement

```sql
ORDER BY title ASC NULLS LAST;
ORDER BY created_at DESC NULLS LAST;
```

- `ASC` is ascending and is the default.
- `DESC` is descending.
- PostgreSQL defaults to `NULLS LAST` for `ASC` and `NULLS FIRST` for `DESC`; say it explicitly when API behavior matters.

---

## 10. Filtering operators and Boolean logic

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

### Operator review

| Syntax | Meaning | Short example | Important note |
|---|---|---|---|
| `=` | Equal | `price = 45.00` | Not valid for testing nullness. |
| `<>` or `!=` | Not equal | `status <> 'CANCELLED'` | `<>` is the standard spelling; PostgreSQL accepts `!=`. |
| `>`, `<`, `>=`, `<=` | Ordered comparison | `stock >= 5` | A comparison with `NULL` produces unknown. |
| `BETWEEN a AND b` | Inclusive range | `price BETWEEN 20 AND 50` | Equivalent to `price >= 20 AND price <= 50`. |
| `IN (...)` | Equal to any listed value | `status IN ('NEW', 'PAID')` | The list can also be a one-column subquery. An empty `IN ()` list is invalid SQL. |
| `NOT IN (...)` | Different from every listed value | `status NOT IN ('CANCELLED')` | A null in the list/subquery can make the result unknown; often prefer `NOT EXISTS`. |
| `LIKE` | Case-sensitive pattern | `title LIKE 'Java%'` | `%` and `_` are wildcards. |
| `ILIKE` | Case-insensitive pattern (PostgreSQL) | `title ILIKE '%java%'` | Convenient, but a leading `%` limits ordinary B-tree index use. |
| `IS NULL` | Is the null marker | `deleted_at IS NULL` | Correct null test. |
| `IS NOT NULL` | Has a non-null value | `isbn IS NOT NULL` | Correct non-null test. |

### Combining predicates

```sql
SELECT id, title, price
FROM books
WHERE stock > 0
  AND (author = 'Joshua Bloch' OR title ILIKE '%java%')
  AND NOT (price > 100);
```

**What it does:** `WHERE` keeps rows for which the complete predicate is `TRUE`; rows producing `FALSE` or unknown are removed.  
**Return/result:** A filtered result set for `SELECT`, or a restricted affected-row set for `UPDATE`/`DELETE`.  
**Practical use:** Search endpoints, authorization scoping, state transitions, and bulk operations.  
**Important note:** Logical precedence is `NOT` → `AND` → `OR`. Use parentheses to express intent even when precedence would produce the same answer.

---

## 11. String pattern matching

**Priority:** ⭐⭐⭐⭐ IMPORTANT

### Syntax and examples

```sql
-- % = zero or more characters
SELECT id, title
FROM books
WHERE title ILIKE '%java%';

-- _ = exactly one character
SELECT username
FROM users
WHERE username LIKE 'dev_';
```

**What it does:** `LIKE` performs case-sensitive wildcard matching; PostgreSQL-specific `ILIKE` performs case-insensitive matching according to the active locale/collation behavior.  
**Return/result:** Rows whose text matches the pattern.  
**Practical use:** Simple name/title search and admin filters.  
**Important notes:**

- `%java%` is a contains search; `java%` is a prefix search.
- User-provided `%` and `_` are wildcards. Escape them when the API promises a literal search.
- Concatenate wildcards in SQL while still binding the value: `WHERE title ILIKE '%' || ? || '%'`. Never concatenate untrusted text into the SQL statement itself.
- For large-scale contains search, investigate trigram or full-text indexing rather than assuming a normal index will help.

---

## 12. `NULL`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

`NULL` means missing/unknown, not zero, an empty string, or a normal value. Most comparisons with it evaluate to the third logical state, **unknown**. A `WHERE` clause retains only `TRUE`.

### Correct null predicates

```sql
SELECT id, isbn
FROM books
WHERE isbn IS NULL;

SELECT id, isbn
FROM books
WHERE isbn IS NOT NULL;
```

This is wrong:

```sql
-- Wrong: the expression is unknown, never TRUE
WHERE isbn = NULL
```

**What they do:** `IS NULL` and `IS NOT NULL` test the null marker directly.  
**Return/result:** A Boolean predicate used to filter rows.  
**Practical use:** Optional fields, soft-delete markers, incomplete workflows.  
**Important note:** Java `null` and SQL `NULL` are related concepts but SQL applies three-valued logic throughout an expression.

### `COALESCE`

**Priority:** ⭐⭐⭐⭐ IMPORTANT

```sql
SELECT title, COALESCE(isbn, 'NO-ISBN') AS displayed_isbn
FROM books;
```

**What it does:** Returns the first non-null argument.  
**Return/result:** One value of a common compatible type.  
**Practical use:** Display defaults, fallback configuration, and null-safe calculations.  
**Important note:** A display fallback does not update the stored value. Applying functions to an indexed column can also affect index use.

### `NULLIF`

**Priority:** ⭐⭐⭐ USEFUL

```sql
SELECT total / NULLIF(item_count, 0) AS average_item_value
FROM order_summaries;
```

**What it does:** Returns `NULL` when its two arguments are equal; otherwise returns the first argument.  
**Return/result:** A scalar value.  
**Practical use:** Avoiding division by zero or turning sentinel values into nulls.  
**Important note:** The resulting null can propagate into later arithmetic.

### Null-safe equality and inequality

**Priority:** ⭐⭐⭐ USEFUL — PostgreSQL/SQL null-safe syntax

```sql
-- TRUE when values differ, including one NULL and one non-NULL
WHERE old_email IS DISTINCT FROM new_email

-- TRUE when values are equal, including NULL compared with NULL
WHERE submitted_code IS NOT DISTINCT FROM stored_code
```

**What it does:** Treats null as a comparable value for equality purposes.  
**Return/result:** Always `TRUE` or `FALSE`, never unknown.  
**Practical use:** Change detection, nullable key comparisons, audit logic.  
**Important note:** Prefer these over elaborate `a = b OR (a IS NULL AND b IS NULL)` expressions.

---

## 13. Aggregate functions

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

### Syntax and examples

```sql
SELECT COUNT(*)
FROM books;

SELECT author,
       COUNT(*) AS book_count,
       SUM(stock) AS total_stock,
       AVG(price) AS average_price,
       MIN(price) AS lowest_price,
       MAX(price) AS highest_price
FROM books
GROUP BY author;
```

| Function | Result |
|---|---|
| `COUNT(*)` | Number of input rows, including rows containing null columns |
| `COUNT(column)` | Number of rows where that expression is not null |
| `SUM(column)` | Sum of non-null numeric values |
| `AVG(column)` | Average of non-null numeric values |
| `MIN(column)` / `MAX(column)` | Smallest/largest non-null value |

**What they do:** Collapse many input rows into one result per query or per group.  
**Return/result:** One aggregate row without `GROUP BY`, or one row per group with it. `COUNT` returns `0` for no input rows; most other aggregates return `NULL`.  
**Practical use:** Dashboard counts, totals, reporting, and existence/health checks.  
**Important notes:**

- `COUNT(*)` and `COUNT(nullable_column)` answer different questions.
- Aggregates ignore null inputs except `COUNT(*)`.
- Use `COALESCE(SUM(amount), 0)` when an empty input must appear as zero.
- For money, preserve an appropriate `NUMERIC` type instead of converting to floating point.

---

## 14. `GROUP BY` and `HAVING`

**Priority:** ⭐⭐⭐⭐ IMPORTANT

### Syntax and example

```sql
SELECT author, COUNT(*) AS book_count
FROM books
WHERE price >= 20
GROUP BY author
HAVING COUNT(*) >= 2
ORDER BY book_count DESC;
```

```text
WHERE  -> filters individual rows before grouping
GROUP BY -> forms groups
HAVING -> filters groups after aggregation
```

**What it does:** `GROUP BY` creates one group for each distinct grouping key; `HAVING` removes aggregate groups.  
**Return/result:** One result row per surviving group.  
**Practical use:** Sales per user, stock per author, orders per status, threshold reports.  
**Important notes:**

- In normal grouped queries, every selected expression must be aggregated or validly determined by the grouping columns.
- Put ordinary row predicates in `WHERE` so unwanted rows do not enter aggregates.
- `HAVING` is for conditions such as `COUNT(*) >= 2`, which do not exist until after grouping.

---

## 15. Joins

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

Joins combine related rows. The diagrams use three logical regions:

```text
+----------------+----------------+----------------+
| left table only| matching pairs |right table only|
+----------------+----------------+----------------+
```

Every join returns a result set. Its most important correctness rule is the `ON` condition: a missing or incomplete condition can multiply rows and corrupt totals.

### `INNER JOIN`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

```text
+----------------+################+----------------+
| left only      |### matched ####| right only     |
+----------------+################+----------------+
                 returned
```

**Syntax**

```sql
SELECT columns
FROM left_table AS l
INNER JOIN right_table AS r
    ON r.foreign_key = l.primary_key;
```

**Example**

```sql
SELECT o.id AS order_id, u.username, o.total
FROM orders AS o
INNER JOIN users AS u
    ON u.id = o.user_id;
```

**What it does:** Returns only pairs that satisfy `ON`.  
**When to use:** The related row must exist in the output—for example, orders with their users.  
**Important note:** One-to-many joins legitimately repeat the one-side columns. Aggregate at the correct grain instead of adding `DISTINCT` blindly.

### `LEFT JOIN` / `LEFT OUTER JOIN`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

```text
+################+################+----------------+
|## left only ###|### matched ####| right only     |
+################+################+----------------+
 all left rows returned; missing right columns become NULL
```

**Syntax and example**

```sql
SELECT u.id, u.username, o.id AS order_id
FROM users AS u
LEFT JOIN orders AS o
    ON o.user_id = u.id;
```

**What it does:** Returns every left row plus matching right rows; unmatched right-side columns are null.  
**When to use:** Include users who have no orders, books with no order items, or other optional relationships.  
**Important note:** A right-table filter in `WHERE` can accidentally remove null-extended rows and turn the query into inner-join behavior:

```sql
-- Keeps all users; only PAID orders are joined
SELECT u.id, u.username, o.id
FROM users AS u
LEFT JOIN orders AS o
    ON o.user_id = u.id
   AND o.status = 'PAID';
```

### `RIGHT JOIN` / `RIGHT OUTER JOIN`

**Priority:** ⭐⭐ NICE TO KNOW

```text
+----------------+################+################+
| left only      |### matched ####|## right only ##|
+----------------+################+################+
                 all right rows returned
```

```sql
SELECT b.id, b.title, oi.quantity
FROM order_items AS oi
RIGHT JOIN books AS b
    ON b.id = oi.book_id;
```

**What it does:** Returns every right row plus matching left rows; unmatched left columns are null.  
**When to use:** When preserving the written right side makes a query clearer.  
**Important note:** Teams often rewrite it as `LEFT JOIN` with table order reversed for easier left-to-right reading.

### `FULL OUTER JOIN`

**Priority:** ⭐⭐⭐ USEFUL

```text
+################+################+################+
|## left only ###|### matched ####|# right only ###|
+################+################+################+
 all three regions returned
```

```sql
SELECT a.external_id AS import_id,
       b.external_id AS database_id
FROM imported_products AS a
FULL OUTER JOIN products AS b
    ON b.external_id = a.external_id;
```

**What it does:** Returns matches plus unmatched rows from both sides, null-extending the missing side.  
**When to use:** Reconciliation and comparing two datasets.  
**Important note:** Less common in request-path code; be explicit about which null pattern means missing from which source.

### `CROSS JOIN`

**Priority:** ⭐⭐⭐ USEFUL — handle carefully

```text
left rows x right rows = every possible pair

u1 -> (u1,b1) (u1,b2) (u1,b3)
u2 -> (u2,b1) (u2,b2) (u2,b3)
```

```sql
SELECT s.status, d.day
FROM order_statuses AS s
CROSS JOIN reporting_days AS d;
```

**What it does:** Produces the Cartesian product: `left_count × right_count` rows.  
**When to use:** Intentionally generating all combinations, such as statuses × reporting days.  
**Important note:** An accidental cross join can explode result size. A comma-separated `FROM users u, orders o` without a proper predicate has the same danger; prefer explicit join syntax.

### Self join

**Priority:** ⭐⭐⭐ USEFUL

```text
same employees table, two aliases/roles

employee e (child) ---- e.manager_id = m.id ----> employee m (manager)
```

```sql
SELECT e.name AS employee_name,
       m.name AS manager_name
FROM employees AS e
LEFT JOIN employees AS m
    ON m.id = e.manager_id;
```

**What it does:** Joins a table to itself through distinct aliases.  
**When to use:** Hierarchies, predecessor links, duplicate detection, and row comparisons.  
**Important note:** Aliases are essential because every referenced column must identify which logical role it belongs to.

### Join debugging checklist

- State the intended result grain: one row per order, user, book, or item?
- Check every key in a composite relationship.
- Qualify ambiguous columns (`o.id`, not merely `id`).
- Compare expected and actual row counts before aggregating.
- Decide whether an optional relation requires `LEFT JOIN` rather than `INNER JOIN`.
- Watch ORM-generated joins and N+1 query patterns in logs.

---

## 16. Subqueries

**Priority:** ⭐⭐⭐⭐ IMPORTANT

### Scalar subquery

```sql
SELECT title, price
FROM books
WHERE price > (
    SELECT AVG(price)
    FROM books
);
```

**What it does:** Uses a subquery that must return at most one row and one column as a scalar value. No row becomes `NULL`; more than one row is an error.  
**Return/result:** The outer query's result set.  
**Practical use:** Compare a row with a global aggregate.

### Subquery in `WHERE`

```sql
SELECT id, username
FROM users
WHERE id IN (
    SELECT user_id
    FROM orders
    WHERE status = 'PAID'
);
```

**What it does:** Supplies a set used by an outer predicate.  
**Practical use:** Filter by a related set.  
**Important note:** For nullable anti-matches, prefer `NOT EXISTS` to `NOT IN`.

### Subquery in `FROM` (derived table)

```sql
SELECT x.user_id, x.order_total
FROM (
    SELECT user_id, SUM(total) AS order_total
    FROM orders
    GROUP BY user_id
) AS x
WHERE x.order_total > 100;
```

**What it does:** Treats query output as a temporary relational source.  
**Practical use:** Querying an intermediate aggregate.  
**Important note:** Give the derived table an alias and expose only useful columns.

### Correlated subquery

```sql
SELECT u.id, u.username
FROM users AS u
WHERE (
    SELECT COUNT(*)
    FROM orders AS o
    WHERE o.user_id = u.id
) >= 3;
```

**What it does:** References a value from the current outer row. PostgreSQL may transform/optimize it, but logically it is evaluated in the outer-row context.  
**Practical use:** Row-specific checks and aggregates.  
**Important note:** `EXISTS`, a join, or a CTE is often clearer. Inspect the plan when a correlated query is slow.

---

## 17. `EXISTS` and `NOT EXISTS`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

### `EXISTS`

```sql
SELECT u.id, u.username
FROM users AS u
WHERE EXISTS (
    SELECT 1
    FROM orders AS o
    WHERE o.user_id = u.id
);
```

### `NOT EXISTS`

```sql
SELECT u.id, u.username
FROM users AS u
WHERE NOT EXISTS (
    SELECT 1
    FROM orders AS o
    WHERE o.user_id = u.id
);
```

**What it does:** Tests whether the correlated subquery returns at least one row; `NOT EXISTS` tests that it returns none.  
**Return/result:** A Boolean predicate; the outer query returns matching outer rows.  
**Practical use:** Users with/without orders, authorization checks, and null-safe anti-joins.  
**Important notes:**

- `SELECT 1` communicates that only row existence matters. The selected value is not read; `SELECT *` has the same existence semantics.
- PostgreSQL can stop looking after establishing existence.
- Index the correlated key for large data, for example `orders(user_id)`.
- `NOT EXISTS` does not suffer from the `NULL` surprise of `NOT IN (subquery)`.

---

## 18. Common table expressions (`WITH`)

### Non-recursive CTE

**Priority:** ⭐⭐⭐⭐ IMPORTANT

**Syntax and example**

```sql
WITH expensive_books AS (
    SELECT id, title, price
    FROM books
    WHERE price > 50
)
SELECT id, title, price
FROM expensive_books
ORDER BY price DESC;
```

**What it does:** Names a query result for use by the single statement that follows.  
**Return/result:** The outer statement's result or affected-row count. The CTE itself is not a persistent view/table.  
**Practical use:** Break complex reporting or data-change logic into readable stages.  
**Important notes:**

- A CTE can make intent clearer than deeply nested subqueries.
- Modern PostgreSQL may inline a side-effect-free, non-recursive CTE; do not assume every CTE is an optimization fence or is always materialized.
- Prefer clear relational steps, then use `EXPLAIN` to assess performance.

### `WITH RECURSIVE`

**Priority:** ⭐⭐ NICE TO KNOW

```sql
WITH RECURSIVE category_tree AS (
    SELECT id, parent_id, name, 0 AS depth
    FROM categories
    WHERE parent_id IS NULL

    UNION ALL

    SELECT c.id, c.parent_id, c.name, t.depth + 1
    FROM categories AS c
    JOIN category_tree AS t
      ON c.parent_id = t.id
)
SELECT id, parent_id, name, depth
FROM category_tree;
```

**What it does:** Repeatedly applies the recursive term to rows produced so far.  
**Return/result:** A result set containing the reached rows.  
**Practical use:** Trees, organizational structures, and graph traversal.  
**Important note:** Define termination carefully and consider cycle detection/maximum-depth requirements for untrusted hierarchical data.

---

## 19. `CASE` expressions

**Priority:** ⭐⭐⭐⭐ IMPORTANT

### Syntax and example

```sql
SELECT title,
       CASE
           WHEN stock = 0 THEN 'OUT_OF_STOCK'
           WHEN stock < 5 THEN 'LOW_STOCK'
           ELSE 'AVAILABLE'
       END AS status
FROM books;
```

General searched form:

```sql
CASE
    WHEN condition_1 THEN result_1
    WHEN condition_2 THEN result_2
    ELSE fallback_result
END
```

**What it does:** Evaluates conditions in order and returns the result for the first true branch.  
**Return/result:** A scalar expression, so it can appear in `SELECT`, `ORDER BY`, aggregates, or DML expressions.  
**Practical use:** DTO status labels, conditional totals, custom sorting, and data migration transforms.  
**Important notes:**

- Without `ELSE`, no matching branch produces `NULL`.
- Branch results must resolve to a compatible output type.
- Do not duplicate critical business invariants only in display-oriented `CASE`; enforce valid stored states with constraints too.

---

## 20. Set operations

**Priority:** ⭐⭐⭐ USEFUL (`UNION ALL` is often the practical default)

### Syntax and examples

```sql
-- Remove duplicate result rows
SELECT email FROM customers
UNION
SELECT email FROM newsletter_subscribers;

-- Keep duplicate result rows
SELECT email FROM customers
UNION ALL
SELECT email FROM newsletter_subscribers;

-- Rows present in both results
SELECT book_id FROM current_inventory
INTERSECT
SELECT book_id FROM featured_books;

-- Rows in the first result but not the second
SELECT book_id FROM current_inventory
EXCEPT
SELECT book_id FROM discontinued_books;
```

| Operation | Return/result | Common use | Important note |
|---|---|---|---|
| `UNION` | Combined rows, duplicates removed | Merge and deduplicate | Duplicate removal costs work. |
| `UNION ALL` | Combined rows, duplicates retained | Append results efficiently | Prefer when duplicates are valid or impossible. |
| `INTERSECT` | Rows common to both results | Find overlap | Duplicate rows are removed unless `INTERSECT ALL` is requested. |
| `EXCEPT` | First-result rows absent from second | Compare/difference | Duplicate rows are removed unless `EXCEPT ALL` is requested. |

**What they do:** Combine complete query results vertically.  
**Practical use:** Merging feeds, reconciliation, and comparison queries.  
**Important notes:** Each side must return the same number of columns in corresponding compatible types. Output column names come from the first query. A final `ORDER BY` applies to the combined result:

```sql
(SELECT id, title FROM new_books)
UNION ALL
(SELECT id, title FROM archived_books)
ORDER BY title, id;
```

---

## 21. Pagination

### Offset pagination: `LIMIT` / `OFFSET`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

```sql
SELECT id, title, price
FROM books
ORDER BY id
LIMIT ? OFFSET ?;
```

For page number `p` using zero-based pages and size `s`, the offset is `p × s`.

**What it does:** Sorts, skips `OFFSET` rows, and returns at most `LIMIT` rows.  
**Return/result:** One page-shaped result set.  
**Practical use:** Simple REST lists and Spring Data `Pageable`.  
**Important notes:**

- Always use a deterministic `ORDER BY`; add a unique tie-breaker.
- Deep offsets get slower because PostgreSQL still has to locate and discard earlier rows.
- Concurrent inserts/deletes can shift rows between offset pages.
- A Spring Data `Page` often also runs a count query; a `Slice` can avoid a total count when the API does not need one.

### Keyset (seek/cursor) pagination

**Priority:** ⭐⭐⭐⭐ IMPORTANT

```sql
SELECT id, title, price
FROM books
WHERE id > ?
ORDER BY id
LIMIT ?;
```

For a newest-first composite order:

```sql
SELECT id, title, created_at
FROM books
WHERE (created_at, id) < (?, ?)
ORDER BY created_at DESC, id DESC
LIMIT ?;
```

**What it does:** Starts after the last ordering key returned by the previous page.  
**Return/result:** The next result slice; the API normally encodes the last key as a cursor.  
**Practical use:** Large feeds, infinite scrolling, and endpoints with deep navigation.  
**Important notes:** The ordering must be stable and uniquely determined. Direction and comparison must agree (`>` for ascending, `<` for descending in these examples). Keyset pagination is excellent for next/previous navigation but does not naturally jump to arbitrary page numbers.

| Style | Strength | Cost/trade-off |
|---|---|---|
| Offset | Easy page numbers and `Pageable` support | Slower/inconsistent at deep pages under concurrent changes |
| Keyset | Scalable, stable continuation with a suitable index | Requires cursor state and a unique total order |

---

## 22. Sorting

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

### Syntax and example

```sql
SELECT id, author, title, created_at
FROM books
ORDER BY author ASC, id DESC;
```

```sql
SELECT id, title, created_at
FROM books
ORDER BY created_at DESC NULLS LAST, id DESC;
```

**What it does:** Orders result rows by the first expression, then uses later expressions to break ties.  
**Return/result:** The same result rows in defined order.  
**Practical use:** Stable API responses, leaderboards, reports, and pagination.  
**Important notes:**

- `ASC` is default; `DESC` reverses direction.
- PostgreSQL defaults to `NULLS LAST` for ascending and `NULLS FIRST` for descending ordering.
- Include a unique tie-breaker such as `id` for repeatable pagination.
- A matching B-tree index may avoid a separate sort, but the planner chooses based on the full query and estimated cost.
- Never splice an unvalidated request string into `ORDER BY`. JDBC parameters bind values, not identifiers/directions; map an allow-listed API sort key to trusted SQL.

---

## 23. Indexes and query plans

### `CREATE INDEX`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

**Syntax and examples**

```sql
CREATE INDEX idx_books_author
ON books(author);

CREATE INDEX idx_books_author_id
ON books(author, id);
```

**What it does:** Builds an auxiliary access structure; B-tree is the default method.  
**Return/result:** A `CREATE INDEX` command result. Queries return the same logical rows with or without the index.  
**Practical use:** Speed filters, joins, and ordered reads that match real query patterns.  
**Important notes:**

- Indexes consume disk and memory, and every relevant insert/update/delete must maintain them.
- A sequential scan can be correct for a small table or a query that returns much of the table.
- PostgreSQL automatically creates indexes for primary-key and unique constraints. Do not create duplicates.
- PostgreSQL does **not** automatically index the referencing side of a foreign key. An index such as `orders(user_id)` is often valuable.

### Composite index

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

```sql
CREATE INDEX idx_orders_user_created
ON orders(user_id, created_at DESC);
```

Target query:

```sql
SELECT id, status, created_at
FROM orders
WHERE user_id = ?
ORDER BY created_at DESC
LIMIT 20;
```

**What it does:** Stores multiple keys in defined order.  
**Return/result:** A reusable index that may support filtering and ordering.  
**Practical use:** “Newest orders for one user” and other multi-column access patterns.  
**Important note:** Column order matters. A multicolumn B-tree is generally most effective when leading columns match query predicates. Design from actual `WHERE`/`JOIN`/`ORDER BY` patterns, not from a rule to index every column.

### `CREATE UNIQUE INDEX`

**Priority:** ⭐⭐⭐⭐ IMPORTANT

```sql
CREATE UNIQUE INDEX uq_users_email
ON users(email);
```

**What it does:** Provides index access and rejects duplicate indexed keys.  
**Return/result:** A command result; later duplicate writes fail.  
**Practical use:** Technical uniqueness, expression uniqueness, or partial uniqueness.  
**Important note:** For a normal business rule, a named `UNIQUE` constraint communicates intent well and creates its own unique index. Null semantics still matter.

### `DROP INDEX`

**Priority:** ⭐⭐⭐⭐ IMPORTANT

```sql
DROP INDEX IF EXISTS idx_books_author;
```

**What it does:** Removes the index, not the table data.  
**Return/result:** A `DROP INDEX` command result.  
**Practical use:** Remove redundant/unused indexes or replace an index design in a migration.  
**Important note:** Removing an index that backs a constraint must be handled through the constraint; measure usage and query impact before dropping production indexes.

### `EXPLAIN` and `EXPLAIN ANALYZE`

**Priority:** ⭐⭐⭐⭐ IMPORTANT

```sql
EXPLAIN
SELECT id, title
FROM books
WHERE author = 'Joshua Bloch';

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, title
FROM books
WHERE author = 'Joshua Bloch';
```

**What they do:** `EXPLAIN` returns the planner's proposed plan without running the statement. `ANALYZE` actually executes it and adds real row/timing measurements; `BUFFERS` adds buffer activity.  
**Return/result:** Plan text/rows, not the query's ordinary result set.  
**Practical use:** Diagnose slow SQL and validate whether an index helps.  
**Important notes:**

- Planner cost numbers are relative cost units, not milliseconds.
- Estimated versus actual row-count differences often reveal stale statistics or difficult selectivity.
- `EXPLAIN ANALYZE` on `INSERT`, `UPDATE`, or `DELETE` **really changes data**. A surrounding `BEGIN`/`ROLLBACK` can protect transactional table changes, but sequences and external trigger effects need separate caution.
- `CREATE INDEX CONCURRENTLY` reduces write blocking for a live table but takes longer and cannot run inside a transaction block. Configure the specific Flyway migration appropriately; do not sprinkle it into ordinary transactional migrations.

---

## 24. Transactions

### `BEGIN`, `COMMIT`, and `ROLLBACK`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

```sql
BEGIN;

UPDATE accounts
SET balance = balance - 100
WHERE id = 1;

UPDATE accounts
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

On failure:

```sql
ROLLBACK;
```

**What it does:** Groups statements into one atomic unit. `COMMIT` makes its changes durable/visible according to concurrency rules; `ROLLBACK` discards its transactional changes.  
**Return/result:** Transaction-state command results plus the ordinary results of statements inside it.  
**Practical use:** Money transfers, checkout, multi-table state changes, and consistent batch work.  
**Important notes:**

- JDBC connections normally begin in auto-commit mode, where each statement is its own transaction.
- After an unhandled SQL error, a PostgreSQL transaction is aborted until `ROLLBACK` or a valid `ROLLBACK TO SAVEPOINT`.
- PostgreSQL DDL is generally transactional. Important exceptions include database creation/drop and concurrent index creation.
- Do not mix application-managed transaction boundaries casually with raw `BEGIN`/`COMMIT` on the same connection.

### Savepoints

**Priority:** ⭐⭐⭐ USEFUL

```sql
BEGIN;

UPDATE books SET price = price * 0.95 WHERE id = 10;
SAVEPOINT before_second_change;

UPDATE books SET price = price * 0.95 WHERE id = 11;
ROLLBACK TO SAVEPOINT before_second_change;

RELEASE SAVEPOINT before_second_change;
COMMIT;
```

**What it does:** Marks a point within a transaction so later work can be undone without rolling back earlier work.  
**Return/result:** Transaction command results.  
**Practical use:** Recovering from an optional sub-operation or implementing nested transaction-like behavior.  
**Important note:** A savepoint is not an independent commit. The outer transaction still decides final durability.

### JDBC and Spring bridge

```java
connection.setAutoCommit(false);
try {
    // execute parameterized statements on this connection
    connection.commit();
} catch (SQLException ex) {
    connection.rollback();
    throw ex;
}
```

Spring's `@Transactional` normally lets the transaction manager obtain a connection, begin, commit, or roll back around a method boundary.

> **Flush is not commit.** Hibernate flush synchronizes pending entity changes by sending SQL to the database. The transaction can still roll back afterward, and other transactions ordinarily do not see those uncommitted changes.

---

## 25. Row locking

### `SELECT ... FOR UPDATE`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW for read-modify-write workflows

```sql
BEGIN;

SELECT id, stock
FROM books
WHERE id = 10
FOR UPDATE;

UPDATE books
SET stock = stock - 1
WHERE id = 10;

COMMIT;
```

**What it does:** Returns selected rows and locks them against conflicting updates/deletes until the transaction ends.  
**Return/result:** A normal result set plus transaction-held row locks.  
**Practical use:** Inventory, account balance, checkout, and other read-then-write logic.  
**Important notes:**

- In auto-commit mode, the lock is released when the select statement finishes, so it cannot protect a later application statement. Keep both operations in one transaction.
- Lock rows in a consistent order to reduce deadlocks; transactions may still need retry handling.
- An atomic statement such as `UPDATE ... WHERE stock > 0 RETURNING stock` is often simpler than explicit read-then-write locking.
- JPA's pessimistic write locking is the ORM-side bridge; it must also run inside a transaction.

### `SELECT ... FOR SHARE`

**Priority:** ⭐⭐⭐ USEFUL

```sql
BEGIN;

SELECT id, status
FROM orders
WHERE id = ?
FOR SHARE;

COMMIT;
```

**What it does:** Takes a shared row lock that can coexist with other shared lockers but blocks conflicting row update/delete operations.  
**Return/result:** Result rows plus shared locks until transaction end.  
**Practical use:** Protect a row from change while validating related work without intending to update that row.  
**Important note:** Use only when its exact concurrency semantics match the workflow; unnecessary locks reduce throughput.

Brief modifiers:

```sql
SELECT id
FROM jobs
WHERE status = 'READY'
FOR UPDATE SKIP LOCKED
LIMIT 1;
```

- `NOWAIT` fails immediately rather than waiting for a lock.
- `SKIP LOCKED` skips locked rows and is useful for queue-like workers, but it intentionally returns an incomplete view and is not for general reporting.

---

## 26. Transaction isolation

**Priority:** ⭐⭐⭐⭐ IMPORTANT

### Syntax

```sql
BEGIN;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
-- queries and changes
COMMIT;
```

Equivalent concise start:

```sql
BEGIN ISOLATION LEVEL REPEATABLE READ;
```

| PostgreSQL level | Practical behavior | Typical use / caveat |
|---|---|---|
| `READ COMMITTED` | Default; each statement sees a snapshot as of that statement's start | Good default for most request transactions; repeated reads can see newly committed values. |
| `REPEATABLE READ` | Transaction keeps a stable snapshot; PostgreSQL also prevents phantom reads at this level | Consistent multi-query reads; serialization anomalies such as write skew can still occur, and conflicts can abort work. |
| `SERIALIZABLE` | Strongest level; outcome is equivalent to some serial order of successful transactions | Complex invariants; can raise serialization failures, so retry the **whole transaction**. |
| `READ UNCOMMITTED` | Treated as `READ COMMITTED` by PostgreSQL | Do not expect dirty reads. |

**What it does:** Controls which concurrent changes a transaction can observe and which anomalies PostgreSQL prevents.  
**Return/result:** Changes transaction semantics; statements keep their normal results.  
**Practical use:** Balance/inventory invariants, consistent reports, and highly concurrent workflows.  
**Important notes:**

- Set the level before the transaction's first query or data-change statement.
- Stronger isolation does not eliminate application design. `SERIALIZABLE` may fail with SQLSTATE `40001`; deadlocks use `40P01`. Retry the complete transaction with a bounded policy.
- Isolation is not the same as explicit row locking. Pick the mechanism that protects the actual invariant.

---

## 27. PostgreSQL upsert: `ON CONFLICT`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW — PostgreSQL-specific

### `DO NOTHING`

```sql
INSERT INTO users (username)
VALUES ('alice')
ON CONFLICT (username)
DO NOTHING;
```

The conflict target may be omitted for “ignore any applicable uniqueness conflict” behavior:

```sql
INSERT INTO users (username)
VALUES ('alice')
ON CONFLICT DO NOTHING;
```

**What it does:** Inserts the proposed row unless the specified unique key conflicts; a conflicting row is skipped.  
**Return/result:** Affected count `1` when inserted or `0` when skipped. With `RETURNING`, a skipped conflict returns no row.  
**Practical use:** Idempotent imports, deduplicated event handling, and “create if absent.”  
**Important note:** `DO NOTHING RETURNING id` does **not** return the already-existing row's ID.

### `DO UPDATE` and `EXCLUDED`

```sql
INSERT INTO users (username, email)
VALUES ('alice', 'alice.new@example.com')
ON CONFLICT (username)
DO UPDATE
SET email = EXCLUDED.email
RETURNING id, username, email;
```

**What it does:** Atomically inserts or updates the conflicting row. `EXCLUDED` represents the row proposed by the failed insert.  
**Return/result:** Affected count or returned inserted/updated rows.  
**Practical use:** Synchronization, idempotent API writes, counters, and mutable natural-key records.  
**Important notes:**

- The conflict target normally infers a non-deferrable unique index/constraint, such as the unique `username` key. `DO UPDATE` needs a usable conflict target.
- `ON CONFLICT` does not turn `NOT NULL`, `CHECK`, or foreign-key failures into updates.
- The assignment target is unqualified: `SET email = ...`, not `SET users.email = ...`.
- Upsert is atomic, but business rules may still require a carefully chosen `WHERE` clause or transaction.

Conditional update example:

```sql
INSERT INTO inventory (book_id, stock)
VALUES (?, ?)
ON CONFLICT (book_id)
DO UPDATE
SET stock = EXCLUDED.stock
WHERE inventory.stock IS DISTINCT FROM EXCLUDED.stock
RETURNING book_id, stock;
```

---

## 28. PostgreSQL `RETURNING`

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW — PostgreSQL-specific and backend-friendly

### All three DML forms

```sql
INSERT INTO books (title, author, price, stock)
VALUES (?, ?, ?, ?)
RETURNING id, created_at;
```

```sql
UPDATE books
SET stock = stock - 1
WHERE id = ? AND stock > 0
RETURNING id, stock;
```

```sql
DELETE FROM books
WHERE id = ?
RETURNING id, title;
```

**What it does:** Makes changed rows produce selected columns/expressions as a result set in the same statement.  
**Return/result:** One returned row per row actually affected; zero affected rows means an empty result set. `INSERT` exposes generated/default values, `UPDATE` exposes new values, and `DELETE` exposes the removed values.  
**Practical use:** Obtain identity IDs, atomic counters/statuses, audit details, and deletion confirmation without a second query.  
**Important notes:**

- Return only needed columns in production mappings.
- In JDBC, explicit SQL containing `RETURNING` is consumed as a result-producing statement; framework APIs differ, so use the API designed for returned rows. JDBC generated-key support is an alternative for generated IDs.
- A second `SELECT` can observe different concurrent state and costs another round trip; `RETURNING` avoids both issues for the changed row.
- Triggers can affect returned values, making `RETURNING` useful for seeing the final stored result.

---

## 29. Date and time

**Priority:** ⭐⭐⭐⭐ IMPORTANT

### Current values

```sql
SELECT CURRENT_DATE,
       CURRENT_TIME,
       CURRENT_TIMESTAMP,
       NOW();
```

| Expression | Result |
|---|---|
| `CURRENT_DATE` | Current transaction date |
| `CURRENT_TIME` | Current transaction time with time zone |
| `CURRENT_TIMESTAMP` | Current transaction timestamp with time zone |
| `NOW()` | PostgreSQL function equivalent to `CURRENT_TIMESTAMP` |

**What they do:** Return current temporal values. `CURRENT_TIMESTAMP` and `NOW()` are fixed at transaction start.  
**Return/result:** Scalar date/time values.  
**Practical use:** Created/updated defaults, time-window queries, and audit data.  
**Important note:** A long transaction sees the same `NOW()` value throughout. PostgreSQL has statement/wall-clock functions when that distinction is genuinely required, but stable transaction time is usually desirable.

### `INTERVAL`, `DATE_TRUNC`, and `EXTRACT`

```sql
SELECT id, created_at
FROM orders
WHERE created_at >= NOW() - INTERVAL '7 days';
```

```sql
SELECT DATE_TRUNC('month', created_at) AS month_start,
       COUNT(*) AS order_count
FROM orders
GROUP BY DATE_TRUNC('month', created_at)
ORDER BY month_start;
```

```sql
SELECT EXTRACT(YEAR FROM created_at) AS created_year
FROM books;
```

**What they do:** `INTERVAL` represents a duration-like calendar/time amount, `DATE_TRUNC` rounds to a unit, and `EXTRACT` retrieves a numeric field.  
**Return/result:** Scalar temporal/numeric expressions, usable in filters and grouping.  
**Practical use:** Recent-data endpoints, monthly reports, retention jobs.  
**Important notes:** Time zones affect date boundaries and rendered `TIMESTAMPTZ` values. Make the application/database zone policy explicit.

### Java mapping guidance

- `TIMESTAMP WITH TIME ZONE` (`TIMESTAMPTZ`) represents an instant; Java `Instant` is the clearest conceptual match, with `OffsetDateTime` also commonly supported.
- PostgreSQL normalizes the instant and displays it in the session time zone; it does not preserve the submitted IANA zone name or original offset.
- `TIMESTAMP WITHOUT TIME ZONE` maps naturally to `LocalDateTime` and represents a wall-clock value, not an instant.
- A recurring local event may need both a local date/time and a separate zone ID such as `Asia/Ho_Chi_Minh`.

---

## 30. String functions

**Priority:** ⭐⭐⭐ USEFUL

| Function | Short example | Result |
|---|---|---|
| `LOWER` | `LOWER('Java SQL')` | `'java sql'` |
| `UPPER` | `UPPER('Java SQL')` | `'JAVA SQL'` |
| `LENGTH` | `LENGTH('Java')` | `4` characters |
| `TRIM` | `TRIM('  Java  ')` | `'Java'` |
| `CONCAT` | `CONCAT(author, ': ', title)` | Combined text |
| `SUBSTRING` | `SUBSTRING(title FROM 1 FOR 20)` | First 20 characters |
| `REPLACE` | `REPLACE(title, 'JVM', 'Java')` | Text with matches replaced |

```sql
SELECT id,
       TRIM(title) AS clean_title,
       LOWER(author) AS normalized_author,
       CONCAT(author, ': ', title) AS label
FROM books;
```

**What they do:** Transform or inspect text values.  
**Return/result:** A scalar text value, except `LENGTH`, which returns a character count.  
**Practical use:** Display projections, imports/cleanup, case-normalized comparisons, and DTO labels.  
**Important notes:** Functions in `WHERE` can prevent use of a plain index; an expression index must match the expression when such a query matters. PostgreSQL `CONCAT` ignores null arguments, whereas the `||` operator generally yields null if an operand is null.

---

## 31. Numeric functions

**Priority:** ⭐⭐⭐ USEFUL

```sql
SELECT ROUND(45.678::NUMERIC, 2) AS rounded, -- 45.68
       CEIL(45.1) AS ceiling,                -- 46
       FLOOR(45.9) AS floor_value,           -- 45
       ABS(-12.5) AS absolute_value;          -- 12.5
```

| Function | What it does | Practical use |
|---|---|---|
| `ROUND(value [, scale])` | Rounds a value | Report/display precision |
| `CEIL(value)` / `CEILING(value)` | Smallest integer not below value | Capacity/page calculations |
| `FLOOR(value)` | Largest integer not above value | Bucketing |
| `ABS(value)` | Absolute magnitude | Differences and validation |

**Return/result:** Scalar numeric values; exact return type depends on the input/function overload.  
**Important note:** Rounding presentation is not a substitute for choosing correct storage precision. Store money-like values in `NUMERIC`, not binary floating point.

---

## 32. Type conversion

### Standard `CAST`

**Priority:** ⭐⭐⭐⭐ IMPORTANT

```sql
SELECT CAST('123' AS INTEGER);
```

### PostgreSQL `::` shorthand

**Priority:** ⭐⭐⭐⭐ IMPORTANT — PostgreSQL-specific spelling

```sql
SELECT '123'::INTEGER;
SELECT metadata->>'price' AS price_text,
       (metadata->>'price')::NUMERIC AS price_number
FROM products;
```

**What it does:** Converts a value/expression to a requested type.  
**Return/result:** A scalar value in the target type, or an error if conversion is invalid.  
**Practical use:** Imports, JSON extraction, comparisons, arithmetic, and explicit API projections.  
**Important notes:**

- Explicit conversion documents intent and avoids surprising implicit-cast choices.
- A bad value such as `'abc'::INTEGER` fails the statement; validate/clean external data.
- Do not cast an indexed column casually in a predicate. Casting every row can prevent a plain index from matching.
- In `ALTER COLUMN ... TYPE`, use `USING` for controlled conversion of existing values; the old default may need to be dropped/recreated separately.

---

## 33. `JSON` and `JSONB`

**Priority:** ⭐⭐⭐⭐ IMPORTANT for document-shaped attributes; relational columns remain the default for core relationships

### Declaration and operators

```sql
CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::JSONB
);

INSERT INTO products (name, metadata)
VALUES (
    'Mechanical Keyboard',
    '{"name":"Mechanical Keyboard","brand":"KeyCo","dimensions":{"width":36},"wireless":true}'::JSONB
);
```

```sql
SELECT metadata->>'name'
FROM products;
```

For the sample document above:

```sql
SELECT metadata->'dimensions' AS dimensions_json,
       metadata->>'brand' AS brand_text,
       (metadata->'dimensions'->>'width')::INTEGER AS width
FROM products;
```

| Syntax | Return/result | Meaning |
|---|---|---|
| `document -> 'key'` | JSON/JSONB value | Preserve JSON type for an object/array/scalar |
| `document ->> 'key'` | `TEXT` | Extract the value as text |
| `document @> other_json` | `BOOLEAN` | Left JSONB contains the right JSON structure |

Containment example:

```sql
SELECT id, name
FROM products
WHERE metadata @> '{"wireless": true}'::JSONB;
```

**What it does:** Stores validated JSON and queries its structure. `JSONB` stores a decomposed representation that supports useful operators and indexing.  
**Practical use:** Flexible metadata, third-party payload snapshots, or genuinely variable attributes.  
**Important notes:**

- `JSON` preserves input text details; `JSONB` normally reads/queries faster, supports indexing, and does not preserve whitespace/key order/duplicate keys as entered.
- Missing keys/path elements return SQL `NULL` rather than raising an error for these extraction operators.
- `->>` returns text; cast it deliberately before numeric/date comparison.
- Do not hide stable, relationally important data in JSONB merely to avoid migrations. Foreign keys and ordinary constraints are much easier on proper columns/tables.
- A GIN index can support common JSONB containment queries; see the optional advanced section.

---

## 34. PostgreSQL arrays

**Priority:** ⭐⭐⭐ USEFUL for small scalar collections

### Declaration, insert, and membership

```sql
CREATE TABLE articles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title TEXT NOT NULL,
    tags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]
);

INSERT INTO articles (title, tags)
VALUES ('JDBC and PostgreSQL', ARRAY['java', 'jdbc', 'postgresql']);
```

```sql
SELECT id, title
FROM articles
WHERE 'java' = ANY(tags);
```

The reverse pattern tests a scalar against an input array:

```sql
SELECT id, title
FROM books
WHERE id = ANY(?);
```

**What it does:** Stores multiple values of one element type in a column; `ANY(array)` compares a scalar with each array element.  
**Return/result:** Array expressions return arrays; a comparison with `ANY` returns a Boolean predicate.  
**Practical use:** Bounded tags, feature flags, or a JDBC-bound list of scalar IDs.  
**Important notes:**

- In JDBC, one `?` represents one value. Bind a PostgreSQL SQL array for `= ANY (?)`; `IN (?)` does not automatically expand a Java collection.
- Prefer a normalized child/junction table when elements need foreign keys, metadata, independent updates, or frequent relational querying.
- PostgreSQL has rich array operators and GIN indexes, but keep this review focused on common membership.

---

## 35. Views

### `CREATE VIEW` / `CREATE OR REPLACE VIEW`

**Priority:** ⭐⭐⭐ USEFUL

```sql
CREATE VIEW available_books AS
SELECT id, isbn, title, author, price
FROM books
WHERE stock > 0;
```

```sql
CREATE OR REPLACE VIEW available_books AS
SELECT id, isbn, title, author, price, stock
FROM books
WHERE stock > 0;
```

### `DROP VIEW`

```sql
DROP VIEW IF EXISTS available_books RESTRICT;
```

**What it does:** A normal view stores a query definition, not a copied result set. Querying it runs an equivalent underlying query. `CREATE OR REPLACE` updates a compatible view definition.  
**Return/result:** DDL command results when defining it; later `SELECT` returns view rows.  
**Practical use:** Reusable reporting projections, compatibility layers during schema evolution, and permission boundaries.  
**Important notes:**

- A view does not automatically make a slow query fast.
- `CREATE OR REPLACE VIEW` cannot arbitrarily change existing output column names/types/order; preserve its contract or plan a drop/recreate and dependent changes.
- Avoid turning views into deeply nested layers that hide query cost.

### Materialized view (brief)

**Priority:** ⭐⭐ NICE TO KNOW

```sql
CREATE MATERIALIZED VIEW monthly_sales AS
SELECT DATE_TRUNC('month', created_at) AS month,
       SUM(total) AS sales
FROM orders
GROUP BY DATE_TRUNC('month', created_at);

REFRESH MATERIALIZED VIEW monthly_sales;
```

A materialized view stores query results and must be refreshed; it trades freshness and refresh cost for faster reads.

---

## 36. Foreign-key actions

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

### Focused comparison

| Action | Parent delete when children exist | Realistic backend use | Critical note |
|---|---|---|---|
| `ON DELETE CASCADE` | Automatically deletes children | `orders` → lifecycle-owned `order_items` | Powerful and potentially broad; database cascade is separate from Hibernate cascade configuration. |
| `ON DELETE SET NULL` | Keeps children and sets FK to null | Preserve an audit row after its optional actor is removed | FK column must allow null; independent history should remain meaningful. |
| `ON DELETE RESTRICT` | Rejects immediately | Prevent deleting a referenced category/customer | Cannot be deferred. Application must remove/reassign children first. |
| `ON DELETE NO ACTION` | Rejects if relationship still violates the FK when checked | Default protective behavior | Usually looks like `RESTRICT`; a deferrable `NO ACTION` constraint may check later. |

### Syntax examples

```sql
CREATE TABLE order_items (
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_book
        FOREIGN KEY (book_id)
        REFERENCES books(id)
        ON DELETE RESTRICT
);
```

```sql
CREATE TABLE audit_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    actor_user_id BIGINT,
    event_type TEXT NOT NULL,
    CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);
```

**What it does:** Defines how PostgreSQL preserves referential integrity when a referenced parent is deleted.  
**Return/result:** The parent `DELETE` succeeds with the configured child action or fails with a foreign-key violation.  
**Practical use:** Encode aggregate ownership and retention rules at the database boundary.  
**Important notes:** Choose from domain lifecycle, not convenience. Index child foreign-key columns when parent changes and joins must find children efficiently. `TRUNCATE ... CASCADE` follows a different, table-wide mechanism and can be much broader than row-level `ON DELETE CASCADE`.

---

## 37. Generated values and sequences

**Priority:** ⭐⭐⭐⭐⭐ for identity; ⭐⭐ NICE TO KNOW for direct sequence calls

### Identity-first recommendation

```sql
CREATE TABLE books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title TEXT NOT NULL
);

INSERT INTO books (title)
VALUES ('Effective Java')
RETURNING id;
```

Identity is the normal modern choice because the generation rule and owned sequence are part of the column definition and can be managed through `ALTER TABLE`.

### Explicit sequence syntax

```sql
CREATE SEQUENCE invoice_number_seq
START WITH 1000
INCREMENT BY 1;

SELECT nextval('invoice_number_seq');
SELECT currval('invoice_number_seq');
```

**What it does:** A sequence is an independent, concurrency-safe number generator. `nextval` advances and returns a value; `currval` returns the most recent value obtained by the current session.  
**Return/result:** One numeric scalar per function call.  
**Practical use:** Identity backing, specialized numbering, and understanding legacy `SERIAL` schemas.  
**Important notes:**

- `nextval` values are not rolled back, so gaps are normal.
- `currval` fails if this database session has not called `nextval` for the sequence. With connection pools, do not use it as a generic “last ID” lookup; use `INSERT ... RETURNING id`.
- Generated IDs are identifiers, not gapless business documents. Legal invoice numbering often needs a separately designed process.
- `SERIAL`/`BIGSERIAL` expand into integer columns plus sequence-backed defaults; Section 3 explains why identity is preferred.

---

## 38. PostgreSQL Boolean values

**Priority:** ⭐⭐⭐⭐ IMPORTANT

### Syntax and examples

```sql
SELECT id, username
FROM users
WHERE active = TRUE;
```

Idiomatic form:

```sql
SELECT id, username
FROM users
WHERE active;
```

Negative form:

```sql
SELECT id, username
FROM users
WHERE NOT active;
```

**What it does:** Tests PostgreSQL `BOOLEAN` values: `TRUE`, `FALSE`, or `NULL`.  
**Return/result:** Rows for which the predicate is true.  
**Practical use:** Active flags, feature switches, and simple state attributes.  
**Important note:** `WHERE NOT active` excludes nulls as well as true values. If null means “not active” in the business rule, make that explicit with `COALESCE(active, FALSE) = FALSE`—or better, declare the column `NOT NULL DEFAULT FALSE` when three states are unnecessary.

---

## 39. PostgreSQL enums

**Priority:** ⭐⭐⭐ USEFUL, but choose deliberately

### Native enum

```sql
CREATE TYPE order_status AS ENUM (
    'NEW',
    'PAID',
    'SHIPPED',
    'CANCELLED'
);

CREATE TABLE enum_orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    status order_status NOT NULL DEFAULT 'NEW'
);
```

Adding a value:

```sql
ALTER TYPE order_status ADD VALUE 'REFUNDED';
```

**What it does:** Defines a reusable PostgreSQL type restricted to a fixed ordered set of labels.  
**Return/result:** Type/DDL command results; writes with invalid labels fail.  
**Practical use:** Stable, small state sets shared across tables.  
**Important notes:** Native enums are readable and strongly constrained, but removing/reordering values and coordinating rolling deployments is awkward. JPA/Hibernate mapping should be configured explicitly and tested against the PostgreSQL enum type.

### Migration-friendly alternative: text plus `CHECK`

```sql
CREATE TABLE orders_with_check (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT chk_order_status
        CHECK (status IN ('NEW', 'PAID', 'SHIPPED', 'CANCELLED'))
);
```

Many backend teams prefer `VARCHAR`/`TEXT` plus a named check constraint because changing the allowed set is straightforward in versioned migrations. It still protects the database; a Java enum alone does not.

---

## 40. DDL vs DML vs DQL vs TCL

**Priority:** ⭐⭐⭐ USEFUL interview vocabulary

| Category | Meaning | Common commands | Return/effect |
|---|---|---|---|
| DDL | Data Definition Language | `CREATE`, `ALTER`, `DROP`, `TRUNCATE` | Changes database objects/schema |
| DML | Data Manipulation Language | `INSERT`, `UPDATE`, `DELETE` | Changes table rows; affected count or `RETURNING` rows |
| DQL | Data Query Language | `SELECT` | Returns a result set |
| TCL | Transaction Control Language | `BEGIN`, `COMMIT`, `ROLLBACK`, `SAVEPOINT` | Controls transaction state |

Terminology varies: some sources include `SELECT` under DML and classify permission commands separately as DCL. The practical distinction—schema, row changes, queries, and transaction boundaries—is more important than memorizing one taxonomy.

---

## 41. SQL conceptual execution order

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

```text
FROM / JOIN
    ↓
WHERE
    ↓
GROUP BY
    ↓
HAVING
    ↓
SELECT
    ↓
DISTINCT
    ↓
ORDER BY
    ↓
LIMIT / OFFSET
```

Example:

```sql
SELECT author, COUNT(*) AS book_count
FROM books
WHERE stock > 0
GROUP BY author
HAVING COUNT(*) >= 2
ORDER BY book_count DESC
LIMIT 10;
```

**What it explains:** SQL is written starting with `SELECT`, but names and row sets become logically available in a different order.  
**Return/result:** This is a reasoning model, not a statement itself.  
**Practical use:** Diagnose alias errors, grouping mistakes, outer-join filters, and pagination behavior.  
**Important notes:**

- A `SELECT` alias generally cannot be used in `WHERE` because `WHERE` is logically evaluated earlier:

```sql
-- Invalid: adjusted_price does not yet exist for WHERE
SELECT price * 1.10 AS adjusted_price
FROM books
WHERE adjusted_price > 50;
```

Repeat the expression or place it in a subquery/CTE:

```sql
WITH priced AS (
    SELECT id, title, price * 1.10 AS adjusted_price
    FROM books
)
SELECT id, title, adjusted_price
FROM priced
WHERE adjusted_price > 50;
```

- `ORDER BY` can normally use a select-list alias because it comes later conceptually.
- PostgreSQL's optimizer may physically execute an equivalent plan in a different order. The list above explains semantics, not literal CPU steps.

---

## 42. JDBC parameter placeholders

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW — security-critical

### `PreparedStatement` syntax

```java
PreparedStatement statement =
    connection.prepareStatement(
        "SELECT id, title, author FROM books WHERE id = ?"
    );

statement.setLong(1, bookId);
```

**What it does:** JDBC `?` marks a value to be bound separately from SQL text. Parameter indexes start at `1`.  
**Return/result:** Depends on the SQL: `executeQuery()` yields a `ResultSet`; DML through `executeUpdate()` yields an affected-row count.  
**Practical use:** Every repository query containing request/application values.  
**Important notes:**

- Bind values with the appropriate setter (`setLong`, `setString`, `setBigDecimal`, `setObject`, and so on).
- Placeholders represent values, not table names, column names, keywords, or sort direction. Map dynamic identifiers from a strict allow-list.
- PostgreSQL-native prepared syntax/protocol displays positional parameters as `$1`, `$2`, and so on; JDBC application SQL uses `?` and the driver translates/binds it.

Never do this:

```java
// Vulnerable to SQL injection and quoting bugs
String sql =
    "SELECT id, username FROM users WHERE username = '" + username + "'";
```

Use this:

```java
PreparedStatement statement = connection.prepareStatement(
    "SELECT id, username FROM users WHERE username = ?"
);
statement.setString(1, username);
```

Prepared statements separate code from data. Validation is still useful for business rules, but escaping/string concatenation is not a substitute for parameter binding.

---

## 43. Spring Data JPA / Hibernate relation

**Priority:** ⭐⭐⭐⭐ IMPORTANT

| SQL concept | Spring Data JPA / Hibernate bridge | Important note |
|---|---|---|
| `SELECT` | Derived repository method, JPQL, Criteria, or native query | JPQL names entities/attributes; native SQL names tables/columns. Inspect generated SQL. |
| `INSERT` | `EntityManager.persist(...)` or repository `save(...)` for a new entity | `save()` does not necessarily execute SQL immediately. Identity generation can influence timing. |
| `UPDATE` | Dirty checking, bulk JPQL/native update | Managed entity changes commonly become SQL at flush; bulk DML bypasses normal entity synchronization rules. |
| `DELETE` | `delete(...)`, JPQL/native delete | ORM cascade/orphan removal and database `ON DELETE` actions are different mechanisms. |
| `JOIN` | JPQL join or mapped relationship navigation/fetching | Lazy access can produce N+1 queries; eager fetching can over-fetch or multiply rows. |
| `LIMIT` / `OFFSET` | `Pageable`, `PageRequest`, `Slice`, `Page` | A `Page` commonly requires an additional count query; stable sorting still matters. |
| Transaction | `@Transactional` / transaction manager | Flush synchronizes SQL; commit finalizes the transaction. |
| `FOR UPDATE` | Pessimistic write lock, such as `@Lock(PESSIMISTIC_WRITE)` | Must execute inside a transaction; generated SQL/provider behavior should be tested. |
| Constraints | Entity/Bean Validation annotations plus database constraints | Application validation improves UX; only the database protects every writer and race. |

Hibernate is a SQL generator and unit-of-work tool, not a replacement for SQL knowledge. You still need SQL to design constraints/indexes, reason about joins and transactions, diagnose N+1 queries, read plans, and verify migrations.

---

## 44. Flyway relation and safe schema evolution

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

Common SQL in versioned migration files:

```text
CREATE TABLE       ALTER TABLE
CREATE INDEX       ADD/DROP CONSTRAINT
UPDATE (backfill)  DROP obsolete objects carefully
```

### Safe nullable → backfill → required example

For a new file such as `V12__add_book_status.sql`:

```sql
ALTER TABLE books
ADD COLUMN status VARCHAR(30);

UPDATE books
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE books
ALTER COLUMN status SET DEFAULT 'ACTIVE';

ALTER TABLE books
ADD CONSTRAINT chk_books_status
CHECK (status IN ('ACTIVE', 'INACTIVE'));

ALTER TABLE books
ALTER COLUMN status SET NOT NULL;
```

The three statements specifically requested for the core pattern are:

```sql
ALTER TABLE books
ADD COLUMN status VARCHAR(30);

UPDATE books
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE books
ALTER COLUMN status SET NOT NULL;
```

**What it does:** Adds a nullable column compatible with existing rows, fills old data, then enforces the invariant for all future writes.  
**Return/result:** DDL command results and a backfill affected-row count.  
**Practical use:** Evolve live schemas without failing immediately on existing data.  
**Important notes:**

- Think about old rows, table size, locks, runtime, and old/new application versions before tightening a schema.
- A default affects future inserts; it does not by itself backfill old null values.
- Applied versioned migrations have history/checksums. Add a new migration rather than editing one already applied to shared environments.
- Do not use `IF NOT EXISTS` merely to make inconsistent environments “pass.” It tests existence (often only by object name), not whether the existing object has the intended definition.
- Qualify schemas or deliberately control `search_path`.
- PostgreSQL DDL is mostly transactional, but commands such as `CREATE INDEX CONCURRENTLY` cannot run in a transaction block and require specific migration configuration/planning.

---

## 45. Common SQL mistakes checklist

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW

| Mistake | Why it hurts | Better habit |
|---|---|---|
| `UPDATE` without `WHERE` | Changes every row | Preview the predicate, use a transaction, and check the affected count. |
| `DELETE` without `WHERE` | Deletes every row | Verify target/environment and select the candidate IDs first. |
| `column = NULL` | Produces unknown, not true | Use `IS NULL` / `IS NOT NULL`. |
| `NOT IN` with a nullable list/subquery | One null can make every non-match unknown | Prefer correlated `NOT EXISTS` or explicitly remove nulls. |
| Wrong/incomplete join condition | Multiplies or mismatches rows and corrupts aggregates | Join all key parts and state the intended result grain. |
| Accidental cross join | Produces `left_count × right_count` rows | Use explicit joins and require an intentional `CROSS JOIN`. |
| Routine production `SELECT *` | Transfers extra data and creates brittle mappings | Name required columns; reserve `*` for exploration or deliberate shapes. |
| ORM N+1 queries | One parent query triggers many child queries | Inspect SQL, use appropriate fetch joins/entity graphs/batching, and measure. |
| Missing index | Repeated large scans/slow joins | Index proven filter/join/order patterns and verify with plans. |
| Too many indexes | Wastes space/cache and slows writes | Remove redundant/unused indexes after evidence-based review. |
| Wrong composite-index order | Index poorly matches predicates/sort | Design leading columns from actual query patterns and selectivity. |
| String-concatenated SQL | SQL injection and quoting bugs | Use `PreparedStatement`/framework binding; allow-list identifiers. |
| Deep `OFFSET` | PostgreSQL finds/discards many earlier rows | Use keyset pagination for deep sequential navigation. |
| Assuming `save()` or flush means commit | Transaction can still roll back; SQL timing can differ | Understand persistence context, flush, and transaction boundary separately. |
| Assuming an index guarantees speed | Planner may correctly choose another plan; writes pay maintenance cost | Compare realistic `EXPLAIN (ANALYZE, BUFFERS)` results. |
| Editing an applied Flyway migration | Checksum mismatch and irreproducible environments | Add a new versioned migration. |
| Using `IF NOT EXISTS` to hide migration drift | Silently accepts an object with the wrong definition | Detect/reconcile drift explicitly; keep deterministic migrations. |

Additional habits:

- Handle zero affected rows as a real outcome (not found, stale version, or failed precondition).
- Bound transaction retries for SQLSTATE `40001`/`40P01`; do not retry non-idempotent external side effects blindly.
- Keep transactions short and never wait for user/network interaction while holding row locks.

---

## Optional advanced PostgreSQL topics

These features are valuable, but they should follow mastery of joins, constraints, indexes, transactions, and execution plans.

### Window functions

**Priority:** ⭐⭐⭐⭐ IMPORTANT for reporting/interviews

```sql
SELECT author,
       title,
       price,
       ROW_NUMBER() OVER (
           PARTITION BY author
           ORDER BY price DESC, id
       ) AS row_number,
       RANK() OVER (
           PARTITION BY author
           ORDER BY price DESC
       ) AS price_rank,
       DENSE_RANK() OVER (
           PARTITION BY author
           ORDER BY price DESC
       ) AS dense_price_rank,
       LAG(price) OVER (
           PARTITION BY author
           ORDER BY created_at, id
       ) AS previous_price,
       LEAD(price) OVER (
           PARTITION BY author
           ORDER BY created_at, id
       ) AS next_price
FROM books;
```

**What it does:** Calculates across related rows without collapsing them like `GROUP BY`. `PARTITION BY` restarts the window per group.  
**Return/result:** The original row grain plus computed values.  
**Practical use:** Top-N per author, running comparisons, rankings, and previous/next event analysis.  
**Important note:** `ROW_NUMBER` is unique by ordering position; `RANK` leaves gaps after ties; `DENSE_RANK` does not. Use deterministic tie-breakers where row identity matters.

### Recursive CTE

**Priority:** ⭐⭐ NICE TO KNOW

Use `WITH RECURSIVE` for trees/graphs with a non-recursive seed joined by `UNION ALL` to a recursive term. Section 18 contains a complete category-tree example. Protect traversal from cycles and unbounded growth.

### Materialized views

**Priority:** ⭐⭐ NICE TO KNOW

```sql
REFRESH MATERIALIZED VIEW monthly_sales;
```

They persist query results and can be indexed, but data becomes stale until refreshed. `REFRESH MATERIALIZED VIEW CONCURRENTLY` has prerequisites and operational trade-offs; study them before production use.

### Partial indexes

**Priority:** ⭐⭐⭐ USEFUL

```sql
CREATE INDEX idx_orders_open_created
ON orders(created_at DESC)
WHERE status = 'OPEN';
```

Smaller index for rows matching a stable predicate. The query predicate must let PostgreSQL prove that it implies the index predicate; some parameterized forms cannot do so at planning time.

### Expression indexes

**Priority:** ⭐⭐⭐ USEFUL

```sql
CREATE UNIQUE INDEX uq_users_lower_email
ON users(LOWER(email));
```

Supports queries/constraints on a computed expression when the query uses the matching expression, for example `WHERE LOWER(email) = LOWER(?)`.

### GIN and GiST overview

**Priority:** ⭐⭐ NICE TO KNOW

- **GIN** is commonly used for multi-valued/inverted search such as JSONB containment, arrays, and full-text search.
- **GiST** is a general framework often used for ranges, geometric/spatial data, nearest-neighbor patterns, and exclusion constraints.

```sql
CREATE INDEX idx_articles_tags_gin
ON articles USING GIN(tags);

CREATE TABLE room_bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    during TSTZRANGE NOT NULL
);

CREATE INDEX idx_room_bookings_during_gist
ON room_bookings USING GIST(during);
```

The operator and operator class determine whether an index can support a query; “GIN/GiST exists” is not enough by itself.

### JSONB indexing

**Priority:** ⭐⭐⭐ USEFUL when JSONB queries are proven hot

```sql
CREATE INDEX idx_products_metadata_gin
ON products USING GIN(metadata);

SELECT id, name
FROM products
WHERE metadata @> '{"wireless": true}'::JSONB;
```

Default GIN supports common containment/key-existence operations. Specialized operator classes and expression indexes trade flexibility, size, and speed; choose from measured query patterns.

### Full-text search

**Priority:** ⭐ ADVANCED

```sql
SELECT id, title
FROM books
WHERE TO_TSVECTOR('english', title)
      @@ PLAINTO_TSQUERY('english', ?);
```

```sql
CREATE INDEX idx_books_title_search
ON books USING GIN(TO_TSVECTOR('english', title));
```

Full-text search tokenizes/normalizes language-aware text and differs from substring `ILIKE`. Ensure the query expression/configuration matches the index; consider a dedicated search system when product requirements exceed PostgreSQL's role.

---

## 46. SQL cheat sheet

### Read, filter, join, group, sort, page

```sql
SELECT [DISTINCT] expression AS alias, ...
FROM table_name AS t
[INNER | LEFT] JOIN other_table AS o ON o.fk = t.id
WHERE row_condition
GROUP BY grouping_columns
HAVING aggregate_condition
ORDER BY sort_expression ASC | DESC [NULLS FIRST | NULLS LAST]
LIMIT count OFFSET count;
```

```sql
-- Keyset page
SELECT id, title
FROM books
WHERE id > ?
ORDER BY id
LIMIT ?;
```

`WHERE` filters rows; `HAVING` filters groups. Stable pagination requires a deterministic, unique order.

### Insert, upsert, and return generated values

```sql
INSERT INTO table_name (column1, column2)
VALUES (?, ?), (?, ?)
ON CONFLICT (unique_column)
DO UPDATE SET column2 = EXCLUDED.column2
RETURNING id, column2;
```

```sql
INSERT INTO target_table (column1, column2)
SELECT source_column1, source_column2
FROM source_table
WHERE condition;
```

### Update and delete

```sql
UPDATE table_name
SET column1 = expression
WHERE condition
RETURNING id, column1;

DELETE FROM table_name
WHERE condition
RETURNING id;
```

Never omit `WHERE` accidentally.

### Tables, constraints, alterations, and indexes

```sql
CREATE TABLE table_name (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT REFERENCES parent(id) ON DELETE RESTRICT,
    name TEXT NOT NULL UNIQUE,
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE table_name ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE table_name ADD CONSTRAINT constraint_name CHECK (condition);
ALTER TABLE table_name DROP CONSTRAINT constraint_name;

CREATE INDEX index_name ON table_name(column1, column2);
CREATE UNIQUE INDEX unique_index_name ON table_name(column1);
DROP INDEX index_name;
```

### Transactions and row locks

```sql
BEGIN;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

SELECT id, stock
FROM books
WHERE id = ?
FOR UPDATE;

SAVEPOINT step_one;
-- statements
ROLLBACK TO SAVEPOINT step_one; -- when needed
COMMIT;                         -- or ROLLBACK
```

Locks last until transaction end. Flush is not commit. Retriable concurrency failures require retrying the whole transaction.

### CTE and `CASE`

```sql
WITH named_result AS (
    SELECT columns
    FROM table_name
    WHERE condition
)
SELECT columns
FROM named_result;
```

```sql
CASE
    WHEN condition_1 THEN result_1
    WHEN condition_2 THEN result_2
    ELSE fallback
END
```

### Five PostgreSQL features to remember

```text
ILIKE                    case-insensitive pattern search
RETURNING                changed values without a second query
ON CONFLICT              atomic insert-or-ignore/update
IS DISTINCT FROM         null-safe inequality
GENERATED ... AS IDENTITY modern database-generated IDs
```

---

## 47. Priority summary

| Topic | Priority | Backend frequency |
|---|---|---|
| Databases and schemas | ⭐⭐⭐ USEFUL | Low/Medium |
| `CREATE TABLE` and core data types | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| Identity columns | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| Constraints | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| `ALTER TABLE` | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| `INSERT` | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| `UPDATE` | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| `DELETE` | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| `TRUNCATE` / `DROP` safety | ⭐⭐⭐⭐ IMPORTANT | Low/Medium |
| `SELECT` | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| `WHERE` and filtering operators | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| `LIKE` / `ILIKE` | ⭐⭐⭐⭐ IMPORTANT | High |
| `NULL` and three-valued logic | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| Aggregate functions | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| `GROUP BY` / `HAVING` | ⭐⭐⭐⭐ IMPORTANT | High |
| `INNER JOIN` / `LEFT JOIN` | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| Other join variants | ⭐⭐⭐ USEFUL | Medium |
| Subqueries | ⭐⭐⭐⭐ IMPORTANT | High |
| `EXISTS` / `NOT EXISTS` | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| Non-recursive CTE | ⭐⭐⭐⭐ IMPORTANT | Medium/High |
| `CASE` | ⭐⭐⭐⭐ IMPORTANT | High |
| Set operations | ⭐⭐⭐ USEFUL | Medium |
| Offset pagination | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| Keyset pagination | ⭐⭐⭐⭐ IMPORTANT | High at scale |
| Sorting | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| Index fundamentals | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| `EXPLAIN` / `EXPLAIN ANALYZE` | ⭐⭐⭐⭐ IMPORTANT | Medium/High |
| Transactions | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| Savepoints | ⭐⭐⭐ USEFUL | Low/Medium |
| `FOR UPDATE` | ⭐⭐⭐⭐⭐ MUST KNOW | Medium/High |
| `FOR SHARE` | ⭐⭐⭐ USEFUL | Low/Medium |
| Isolation levels | ⭐⭐⭐⭐ IMPORTANT | High |
| `ON CONFLICT` upsert | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| `RETURNING` | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| Date/time | ⭐⭐⭐⭐ IMPORTANT | High |
| String functions | ⭐⭐⭐ USEFUL | Medium |
| Numeric functions | ⭐⭐⭐ USEFUL | Medium |
| Casts | ⭐⭐⭐⭐ IMPORTANT | High |
| JSONB basics | ⭐⭐⭐⭐ IMPORTANT | Medium/High |
| Arrays | ⭐⭐⭐ USEFUL | Low/Medium |
| Views | ⭐⭐⭐ USEFUL | Medium |
| Foreign-key actions | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| Direct sequence operations | ⭐⭐ NICE TO KNOW | Low |
| Boolean predicates | ⭐⭐⭐⭐ IMPORTANT | High |
| Native enums | ⭐⭐⭐ USEFUL | Low/Medium |
| DDL/DML/DQL/TCL terms | ⭐⭐⭐ USEFUL | Medium (interviews) |
| SQL execution order | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| JDBC placeholders / injection safety | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| Spring Data JPA / Hibernate SQL relation | ⭐⭐⭐⭐ IMPORTANT | Very High |
| Flyway schema evolution | ⭐⭐⭐⭐⭐ MUST KNOW | High |
| Common mistake prevention | ⭐⭐⭐⭐⭐ MUST KNOW | Very High |
| Window functions | ⭐⭐⭐⭐ IMPORTANT | Medium |
| Recursive CTE | ⭐⭐ NICE TO KNOW | Low/Medium |
| Partial/expression indexes | ⭐⭐⭐ USEFUL | Medium |
| GIN/GiST and JSONB indexing | ⭐⭐ NICE TO KNOW | Low/Medium |
| Materialized views | ⭐⭐ NICE TO KNOW | Low |
| Full-text search | ⭐ ADVANCED | Low/Domain-specific |

---

## 48. Mini review exercises

Use this compact exercise schema:

```text
users(
  id BIGINT PK, username TEXT UNIQUE, email TEXT NULL,
  active BOOLEAN, created_at TIMESTAMPTZ
)

books(
  id BIGINT PK, isbn TEXT UNIQUE, title TEXT, author TEXT,
  price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  stock INTEGER NOT NULL CHECK (stock >= 0),
  metadata JSONB, created_at TIMESTAMPTZ
)

orders(
  id BIGINT PK, user_id BIGINT FK -> users.id,
  status TEXT, total NUMERIC(12,2), created_at TIMESTAMPTZ
)

order_items(
  order_id BIGINT FK -> orders.id ON DELETE CASCADE,
  book_id BIGINT FK -> books.id,
  quantity INTEGER, unit_price NUMERIC(12,2),
  PK(order_id, book_id)
)
```

Assume identity/default values are configured where the prompts omit them. Write PostgreSQL SQL and name only the columns you need.

### Questions

1. Return `id`, `title`, and `author` for in-stock books. Sort by title, then ID.
2. Find books whose title contains `java` case-insensitively and whose price is between 20 and 60 inclusive.
3. Find active users whose email is missing.
4. Return page 3 of books with 10 rows per page, where page 1 starts at offset 0. Sort deterministically by title and ID.
5. Return the next 10 books after ID 100 using keyset pagination.
6. Show each order ID with its user's username and the order status.
7. List every user with their order count, including users with zero orders.
8. Show order ID, username, book title, quantity, and unit price for every order line.
9. Calculate the total value of current inventory (`price × stock`). Return zero for an empty table.
10. Show book count and average price, rounded to two decimal places, per author.
11. Return only authors who have at least two books.
12. Find books priced above the overall average book price.
13. Find users who have at least one `PAID` order.
14. Find books that have never appeared in an order.
15. Use a CTE to find orders whose stored `total` differs from the total calculated from their items. Treat an order with no items as calculated total zero.
16. Derive `OUT_OF_STOCK`, `LOW_STOCK` (1–4), or `AVAILABLE` from each book's stock.
17. Insert one book and return its generated ID and creation timestamp.
18. Insert two books in one statement and return their IDs and titles.
19. Atomically decrement book 10's stock only when stock is available; return the new stock.
20. Delete cancelled orders older than 90 days and return the deleted IDs.
21. Upsert Alice by username, updating her email on conflict, and return the resulting row.
22. In one transaction, reduce prices for books 10 and 11. Use a savepoint so the change to book 11 is undone while the change to book 10 commits.
23. Lock book 10 for an inventory read-modify-write operation, decrement stock if available, and commit.
24. Create an index for queries that filter orders by `user_id` and sort newest first, using ID as a stable tie-breaker.
25. Create a partial index for a dashboard that reads newest `OPEN` orders, again using ID as a tie-breaker.
26. Inspect actual execution and buffer usage for the newest 20 orders belonging to user 42.
27. Write a safe Flyway migration that adds required `books.status`, backfills existing rows to `ACTIVE`, supplies that default for future rows, and restricts values to `ACTIVE` or `INACTIVE`.

```text
=================================
STOP — SOLVE BEFORE CHECKING
=================================
```

### Solutions

#### 1. In-stock books

```sql
SELECT id, title, author
FROM books
WHERE stock > 0
ORDER BY title ASC, id ASC;
```

#### 2. Case-insensitive title and price range

```sql
SELECT id, title, price
FROM books
WHERE title ILIKE '%java%'
  AND price BETWEEN 20 AND 60
ORDER BY price, id;
```

#### 3. Active users with no email

```sql
SELECT id, username
FROM users
WHERE active
  AND email IS NULL
ORDER BY id;
```

#### 4. Third offset page

```sql
SELECT id, title, author
FROM books
ORDER BY title ASC, id ASC
LIMIT 10 OFFSET 20;
```

#### 5. Keyset page after ID 100

```sql
SELECT id, title, author
FROM books
WHERE id > 100
ORDER BY id ASC
LIMIT 10;
```

#### 6. Orders with usernames

```sql
SELECT o.id AS order_id,
       u.username,
       o.status
FROM orders AS o
INNER JOIN users AS u
    ON u.id = o.user_id
ORDER BY o.id;
```

#### 7. Every user and order count

```sql
SELECT u.id,
       u.username,
       COUNT(o.id) AS order_count
FROM users AS u
LEFT JOIN orders AS o
    ON o.user_id = u.id
GROUP BY u.id, u.username
ORDER BY u.id;
```

`COUNT(o.id)` counts matched orders; `COUNT(*)` would count the preserved user row even when no order exists.

#### 8. Complete order-line view

```sql
SELECT o.id AS order_id,
       u.username,
       b.title,
       oi.quantity,
       oi.unit_price
FROM orders AS o
INNER JOIN users AS u
    ON u.id = o.user_id
INNER JOIN order_items AS oi
    ON oi.order_id = o.id
INNER JOIN books AS b
    ON b.id = oi.book_id
ORDER BY o.id, b.title, b.id;
```

#### 9. Current inventory value

```sql
SELECT COALESCE(SUM(price * stock), 0) AS inventory_value
FROM books;
```

#### 10. Book statistics per author

```sql
SELECT author,
       COUNT(*) AS book_count,
       ROUND(AVG(price), 2) AS average_price
FROM books
GROUP BY author
ORDER BY author;
```

#### 11. Authors with at least two books

```sql
SELECT author,
       COUNT(*) AS book_count
FROM books
GROUP BY author
HAVING COUNT(*) >= 2
ORDER BY book_count DESC, author;
```

#### 12. Books above the overall average

```sql
SELECT id, title, price
FROM books
WHERE price > (
    SELECT AVG(price)
    FROM books
)
ORDER BY price DESC, id;
```

#### 13. Users with at least one paid order

```sql
SELECT u.id, u.username
FROM users AS u
WHERE EXISTS (
    SELECT 1
    FROM orders AS o
    WHERE o.user_id = u.id
      AND o.status = 'PAID'
)
ORDER BY u.id;
```

#### 14. Books never ordered

```sql
SELECT b.id, b.title
FROM books AS b
WHERE NOT EXISTS (
    SELECT 1
    FROM order_items AS oi
    WHERE oi.book_id = b.id
)
ORDER BY b.id;
```

This is null-safe and communicates the anti-join more reliably than a nullable `NOT IN` subquery.

#### 15. Stored versus calculated order total

```sql
WITH calculated_totals AS (
    SELECT oi.order_id,
           SUM(oi.quantity * oi.unit_price) AS calculated_total
    FROM order_items AS oi
    GROUP BY oi.order_id
)
SELECT o.id,
       o.total AS stored_total,
       COALESCE(ct.calculated_total, 0::NUMERIC) AS calculated_total
FROM orders AS o
LEFT JOIN calculated_totals AS ct
    ON ct.order_id = o.id
WHERE o.total IS DISTINCT FROM
      COALESCE(ct.calculated_total, 0::NUMERIC)
ORDER BY o.id;
```

#### 16. Stock status with `CASE`

```sql
SELECT id,
       title,
       CASE
           WHEN stock = 0 THEN 'OUT_OF_STOCK'
           WHEN stock < 5 THEN 'LOW_STOCK'
           ELSE 'AVAILABLE'
       END AS stock_status
FROM books
ORDER BY id;
```

#### 17. Insert and return generated values

```sql
INSERT INTO books (isbn, title, author, price, stock)
VALUES (
    '9780134685991',
    'Effective Java',
    'Joshua Bloch',
    45.00,
    10
)
RETURNING id, created_at;
```

#### 18. Multi-row insert

```sql
INSERT INTO books (isbn, title, author, price, stock)
VALUES
    ('9781617294945', 'Spring in Action', 'Craig Walls', 49.00, 8),
    ('9781491950357', 'Building Microservices', 'Sam Newman', 52.00, 6)
RETURNING id, title;
```

#### 19. Atomic stock decrement

```sql
UPDATE books
SET stock = stock - 1
WHERE id = 10
  AND stock > 0
RETURNING id, stock;
```

An empty result means the book did not exist or had no available stock.

#### 20. Delete old cancelled orders

```sql
DELETE FROM orders
WHERE status = 'CANCELLED'
  AND created_at < CURRENT_TIMESTAMP - INTERVAL '90 days'
RETURNING id;
```

The exercise schema's `ON DELETE CASCADE` removes owned `order_items`; review the affected scope before running this against real data.

#### 21. Upsert Alice

```sql
INSERT INTO users (username, email, active)
VALUES ('alice', 'alice.new@example.com', TRUE)
ON CONFLICT (username)
DO UPDATE
SET email = EXCLUDED.email
RETURNING id, username, email, active;
```

#### 22. Savepoint and partial rollback

```sql
BEGIN;

UPDATE books
SET price = price * 0.95
WHERE id = 10;

SAVEPOINT before_second_change;

UPDATE books
SET price = price * 0.95
WHERE id = 11;

ROLLBACK TO SAVEPOINT before_second_change;
RELEASE SAVEPOINT before_second_change;

COMMIT;
```

#### 23. Inventory row lock

```sql
BEGIN;

SELECT id, stock
FROM books
WHERE id = 10
FOR UPDATE;

UPDATE books
SET stock = stock - 1
WHERE id = 10
  AND stock > 0
RETURNING id, stock;

COMMIT;
```

The lock is useful only because the read and write share the same transaction. For this simple condition, Solution 19 alone is often sufficient.

#### 24. Composite orders index

```sql
CREATE INDEX idx_orders_user_created_at_id
ON orders(user_id, created_at DESC, id DESC);
```

Target query:

```sql
SELECT id, status, total, created_at
FROM orders
WHERE user_id = ?
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

#### 25. Partial open-orders index

```sql
CREATE INDEX idx_orders_open_created_at_id
ON orders(created_at DESC, id DESC)
WHERE status = 'OPEN';
```

Target query predicates must imply `status = 'OPEN'` for PostgreSQL to use this partial index.

#### 26. Actual plan and buffers

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, total, created_at
FROM orders
WHERE user_id = 42
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

`ANALYZE` executes this read query. It would also execute a write if the explained statement were DML.

#### 27. Safe Flyway status migration

In a new migration such as `V12__add_book_status.sql`:

```sql
ALTER TABLE books
ADD COLUMN status VARCHAR(30);

UPDATE books
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE books
ALTER COLUMN status SET DEFAULT 'ACTIVE';

ALTER TABLE books
ADD CONSTRAINT chk_books_status
CHECK (status IN ('ACTIVE', 'INACTIVE'));

ALTER TABLE books
ALTER COLUMN status SET NOT NULL;
```

Existing rows are handled before `NOT NULL` is enforced. Add this as a new versioned migration; do not edit an already-applied migration.
