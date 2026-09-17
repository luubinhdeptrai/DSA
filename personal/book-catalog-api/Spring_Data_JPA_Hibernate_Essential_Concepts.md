# Spring Data JPA + Hibernate — Essential Concepts

> A first-principles guide to moving the Book Catalog from hand-written JDBC persistence to JPA and Hibernate without forgetting the SQL, transactions, connections, pool, or database underneath.

**Example baseline:** Java 21, Spring Boot 4.1.1, Spring Data JPA 4.1.1, Jakarta Persistence 3.2.0, Hibernate ORM 7.4.5.Final, HikariCP 7.0.2, pgJDBC 42.7.13, and PostgreSQL 17.  
**Separate validation baseline:** Jakarta Validation 3.1.1 and Hibernate Validator 9.1.3.Final. Hibernate Validator is not Hibernate ORM.  
**Companion baseline:** the checked-in [`Validation/book-catalog-api`](Validation/book-catalog-api/) application, plus the earlier JDBC, DataSource, REST, and Validation guides.  
**Learning goal:** Predict what happens from a Java entity operation to generated SQL and back, decide where persistence responsibilities belong, and use Spring Data repositories without treating them as magic.

The checked-in application currently uses `JdbcTemplate`, a `RowMapper`, hand-written CRUD SQL, the Boot-managed `DataSource` and HikariCP, pgJDBC, and PostgreSQL. This guide adds abstractions above that stack. It does not replace the lower layers you already learned.

## How to study

1. Read each diagram from top to bottom and name the component responsible for every arrow.
2. Before opening a checkpoint answer, predict the entity state, whether SQL has run, and whether the transaction has committed.
3. Translate every JPA operation back into possible SQL—but do not assume one method call always means one immediate SQL statement.
4. Keep three models separate: HTTP DTOs, managed entities, and durable relational data.
5. Keep three moments separate: changing Java state, flushing SQL, and committing the database transaction.
6. Use the companion exercise to replace one JDBC responsibility at a time while keeping the public API stable.

Code fragments isolate one concept unless explicitly described as a complete type. They use `jakarta.persistence.*`, never the old `javax.persistence.*` namespace. Advanced transaction behavior belongs in the next roadmap topic.

## Big picture

The transition is not “SQL disappears.” It is “the application delegates repetitive mapping and unit-of-work mechanics to an ORM while retaining responsibility for data design and query behavior.”

```text
BEFORE

BookController
    ↓
BookService
    ↓
BookRepository class
    ↓
JdbcTemplate
    ↓
hand-written SQL + RowMapper
    ↓
JDBC → DataSource/HikariCP → pgJDBC → PostgreSQL

AFTER

BookController
    ↓
BookService
    ↓
Spring Data repository proxy
    ↓
EntityManager + persistence context
    ↓
Hibernate ORM
    ↓
generated/JPQL/native SQL
    ↓
JDBC → DataSource/HikariCP → pgJDBC → PostgreSQL
```

JPA does not replace PostgreSQL. Hibernate does not replace JDBC. Spring Data JPA does not replace Hibernate. HikariCP, SQL, transactions, indexes, and database constraints remain part of the runtime and your design responsibility.

## Concept priorities

| Priority | Meaning | What you should be able to do |
|---|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Required to use JPA without accidental behavior | Explain it from memory and implement the basic pattern |
| ⭐⭐⭐⭐ IMPORTANT | Common source of correctness or performance bugs | Apply it and diagnose failures |
| ⭐⭐⭐ NICE TO KNOW | Valuable after the central model is stable | Recognize when it is appropriate |
| ⭐⭐ FUTURE KNOWLEDGE | Better handled in a later roadmap topic | Understand its purpose without adding it now |

### Navigation by cluster

- Sections 1–4: ORM, the stack, Boot, and dependencies
- Sections 5–7: entity and column mapping
- Sections 8–13: persistence context, entity states, dirty checking, and flush
- Sections 14–20: repositories, queries, paging, and sorting
- Sections 21–29: relationships, fetching, and N+1
- Sections 30–33: transaction and web-boundary essentials
- Sections 34–45: identity details, schema ownership, errors, and useful extensions
- Sections 46–50: mistakes, debugging, priorities, and cumulative checkpoints
- Sections 51–52: final mental model and review sheet

---

## 1. From JDBC to ORM: why ORM exists

**⭐⭐⭐⭐⭐ MUST KNOW**

You already know the explicit JDBC round trip:

```text
Java Book values
    ↓ application extracts every field
PreparedStatement + SQL INSERT/UPDATE
    ↓ database stores columns
SQL SELECT + ResultSet
    ↓ RowMapper reads every column
Java Book values
```

That explicitness is valuable. It also repeats work across many entities and use cases:

- write almost-identical CRUD SQL;
- bind fields in the right order and type;
- keep `RowMapper` code aligned with selected columns;
- coordinate parent/child foreign keys;
- remember which objects changed inside a transaction;
- decide when updates should be issued.

ORM—object-relational mapping—uses metadata and a unit-of-work runtime to bridge Java objects and relational rows:

```text
Java entity
    ↕ mapping metadata
Hibernate ORM
    ↕ SQL through JDBC
relational tables
```

The bridge is useful because the models are different. Java has object identity, references, collections, inheritance, and mutation. A relational database has rows, keys, foreign keys, joins, sets, and constraints. This difference is the **object-relational impedance mismatch**. ORM does not erase it; ORM supplies conventions and machinery for handling it.

Hibernate can:

- hydrate rows into entities instead of requiring a `RowMapper` for every query;
- derive inserts, updates, and deletes from mappings and entity operations;
- preserve one managed Java representation of a database identity in a persistence context;
- track managed changes and generate updates during flush;
- navigate mapped relationships and coordinate cascaded entity operations;
- expose JPQL, which queries entities and attributes rather than tables and columns.

ORM does **not** decide whether the schema is good, an index is missing, a query is efficient, a relationship is modeled correctly, a transaction boundary matches a business operation, or an HTTP contract is safe. It also cannot repeal network latency, locks, concurrency, or relational constraints.

### JDBC-to-ORM responsibility shift

| Concern | JdbcTemplate version | JPA/Hibernate version | Still your responsibility? |
|---|---|---|---|
| CRUD statement text | Usually hand-written | Usually generated | Verify the resulting SQL |
| Row mapping | `RowMapper` | Entity metadata + Hibernate hydration | Keep mapping aligned with schema |
| In-memory identity | Ordinary objects | Persistence-context identity map | Understand context scope |
| Updating a loaded row | Explicit `UPDATE` | Managed mutation + dirty checking | Choose intended fields and transaction |
| Relationships | Explicit joins/FK handling | Association mappings + queries | Design ownership and fetch plan |
| Integrity | PostgreSQL constraints | Same PostgreSQL constraints | Preserve and handle violations |

---

## 2. JPA vs Jakarta Persistence vs Hibernate ORM vs Spring Data JPA

**⭐⭐⭐⭐⭐ MUST KNOW**

These names describe different layers:

```text
Spring Data JPA
    ↓ repository abstraction and query-method infrastructure
Jakarta Persistence (JPA)
    ↓ specification and standard API
Hibernate ORM
    ↓ JPA provider / ORM implementation
JDBC
    ↓ database-call API
DataSource / HikariCP
    ↓ connection acquisition and pooling
pgJDBC
    ↓ PostgreSQL JDBC driver and wire protocol
PostgreSQL
```

| Layer | What it is | Examples in your code/runtime | What it is not |
|---|---|---|---|
| Jakarta Persistence, commonly called JPA | A specification: annotations, interfaces, lifecycle rules, and query language | `@Entity`, `EntityManager`, JPQL | A concrete ORM engine |
| Hibernate ORM | The provider implementing Jakarta Persistence and generating/executing ORM SQL | dirty checking, proxy classes, SQL generation | Spring Data or a connection pool |
| Spring Data JPA | Spring's repository abstraction built for JPA | `JpaRepository`, derived queries, repository proxy | The JPA specification or ORM provider |
| Spring Boot | Dependency management and conditional infrastructure configuration | starter, `EntityManagerFactory`, transaction manager, properties | Your domain model or query designer |

“JPA” is still the familiar shorthand, but the official specification has been named **Jakarta Persistence** since the namespace moved to `jakarta.persistence`. The concepts are the same layer.

### Hibernate Validator is a different project

```text
Hibernate Validator 9.1.3.Final
    → implements Jakarta Validation
    → checks DTO/bean/method constraints such as @NotBlank

Hibernate ORM 7.4.5.Final
    → implements Jakarta Persistence
    → maps entities, manages persistence contexts, and produces SQL
```

They share the Hibernate project family name, not a job. A validation exception from `jakarta.validation.ConstraintViolationException` and a database constraint exception represented by Hibernate ORM are different failures despite similar words.

---

## 3. Spring Boot's role

**⭐⭐⭐⭐⭐ MUST KNOW**

Adding the JPA starter changes the classpath. Boot combines that classpath with configuration and existing beans:

```text
JPA/Hibernate/Spring Data classes on classpath
        +
spring.datasource.* and spring.jpa.* configuration
        +
existing Boot-managed DataSource
        ↓
conditional auto-configuration
        ↓
EntityManagerFactory
transaction infrastructure
Hibernate provider integration
entity and repository discovery
Spring Data repository beans
```

For the Book Catalog, Boot reuses the same `DataSource` and HikariCP pool already configured under `spring.datasource`. It does not open an unrelated connection mechanism merely because JPA is present. When Hibernate needs SQL, it obtains JDBC connections through that `DataSource`.

Boot typically provides or coordinates:

- compatible dependency versions through its parent/BOM;
- a `DataSource` from the existing URL, credentials, driver, and Hikari settings;
- a JPA `EntityManagerFactory` backed by Hibernate;
- Spring transaction-management infrastructure for JPA;
- repository scanning and proxy creation below the application package;
- entity discovery below the application package;
- binding of `spring.jpa.*` and provider properties.

Boot does not decide:

- what a `Book` means or which fields may change;
- whether a `Book`–`Author` relationship is one-to-many or many-to-many;
- whether a generated query is efficient or semantically correct;
- which service operations form a business transaction;
- what the REST payload and statuses should be;
- which PostgreSQL constraints and migrations are required.

Auto-configuration supplies infrastructure after evaluating conditions. It is not invisible magic: inspect the startup condition report, beans, configuration, dependency tree, and generated SQL when you need evidence.

---

## 4. Dependency setup in this project

**⭐⭐⭐⭐ IMPORTANT**

The checked-in POM uses Spring Boot 4.1.1 and Java 21 and currently declares `spring-boot-starter-jdbc`. The migration starter is:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

Use Boot's dependency management. Do not add versions to Hibernate ORM, Spring Data JPA, Jakarta Persistence, HikariCP, or pgJDBC merely to reproduce the versions shown in this guide. With the checked-in Boot parent, the relevant resolved baseline is:

| Component | Managed version in this learning baseline |
|---|---:|
| Spring Data JPA | 4.1.1 |
| Hibernate ORM | 7.4.5.Final |
| Jakarta Persistence | 3.2.0 |
| HikariCP | 7.0.2 |
| pgJDBC | 42.7.13 |
| Hibernate Validator | 9.1.3.Final, separate validation provider |

`spring-boot-starter-data-jpa` brings the JPA, Hibernate, transaction, and Spring JDBC/DataSource infrastructure it needs. Once the application removes direct `JdbcTemplate` use, the explicit JDBC starter is redundant and may be replaced by the JPA starter. Retaining it is functionally possible, but it falsely suggests that application code still needs two separate persistence starters unless that choice is documented.

The PostgreSQL driver remains a runtime dependency. JPA does not speak PostgreSQL's network protocol directly; Hibernate still executes through JDBC and pgJDBC.

For authoritative version-sensitive lookup, use the [Spring Boot SQL/JPA reference](https://docs.spring.io/spring-boot/reference/data/sql.html), [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/), [Jakarta Persistence 3.2 specification](https://jakarta.ee/specifications/persistence/3.2/), and [Hibernate ORM 7.4 documentation](https://docs.hibernate.org/orm/7.4/).

### Checkpoint 1

If the repository no longer imports `JdbcTemplate`, did JDBC and HikariCP disappear? Does `JpaRepository` implement JPA?

<details>
<summary>Answer</summary>

No. Hibernate ORM still executes SQL through JDBC, obtains connections from the Boot-managed `DataSource`/HikariCP, and uses pgJDBC to reach PostgreSQL. `JpaRepository` is a Spring Data abstraction that delegates to JPA infrastructure; Hibernate ORM is the JPA provider.

</details>

---

## 5. Entities: mapped Java identity and state

**⭐⭐⭐⭐⭐ MUST KNOW**

An entity is a Java object with persistent identity whose state is mapped to relational data. An entity instance is not literally a row: it can be new, managed, detached, or removed, while a row is durable database state.

The central annotations are:

| Annotation | Purpose | Book example |
|---|---|---|
| `@Entity` | Marks a persistent entity type | `Book` participates in JPA lifecycle |
| `@Table` | Selects table and optional schema/catalog metadata | `@Table(name = "books")` |
| `@Id` | Marks the entity identifier | `id` maps the primary-key identity |
| `@GeneratedValue` | Declares identifier generation | PostgreSQL identity-backed ID |
| `@Column` | Describes basic column mapping | lengths, precision, nullability metadata |
| `@Transient` | Excludes a field/property from persistence mapping | a calculated display label |

A deliberately partial Book mapping looks like this:

```java
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Book() {
        // Required by Jakarta Persistence for portable entity construction.
    }

    // Other mapped state and application-facing methods omitted.
}
```

### Practical entity requirements

- A portable entity has a public or protected no-argument constructor. Hibernate uses it when materializing objects.
- Use an ordinary non-final class for the normal mutable entity model. Hibernate has capabilities beyond the strict portable minimum, but final types/methods can prevent proxy-based lazy behavior.
- An entity needs a stable persistence identity expressed by `@Id`.
- Hibernate must be able to read/write mapped state. Field access or property access supplies that path.
- Mutation is normal because managed state changes drive dirty checking. Mutation can still be constrained through meaningful methods rather than exposing setters for everything.

### Field access versus property access

Where you place `@Id` normally determines the default access strategy:

- annotations on fields → provider reads/writes fields;
- annotations on getters → provider uses JavaBean properties.

Be consistent unless you deliberately override access. With field access, application code can still use getters and domain methods; Hibernate is not required to call those methods to hydrate the entity.

### Why the existing `Book` record should change

The current `Book` is a record with primitive `long id`. Records are final, their components are final, they have no JPA no-argument constructor, and they model immutable data carriers rather than a mutable managed lifecycle. Jakarta Persistence 3.2 added support for records as embeddable value types; that does not make a record the normal portable entity model. Records remain excellent DTO candidates but a poor default for a mutable JPA entity.

Convert `Book` to an ordinary entity class in the exercise. Keep `BookRequest` and `BookResponse` as records because those are boundary data shapes, not managed entities.

`@Transient` is a JPA mapping annotation. Java's `transient` keyword concerns Java serialization. They solve different problems.

---

## 6. Identifier generation

**⭐⭐⭐⭐ IMPORTANT**

| Strategy | Meaning | Practical note |
|---|---|---|
| `GenerationType.IDENTITY` | Database identity/autoincrement column produces the key | Closest match to the existing `GENERATED ... AS IDENTITY` PostgreSQL column |
| `GenerationType.SEQUENCE` | Provider obtains values from a database sequence | Often flexible and efficient on PostgreSQL; requires matching schema mapping |
| `GenerationType.AUTO` | Provider selects an appropriate strategy | Convenient, but makes the chosen database mechanism less explicit |
| `GenerationType.TABLE` | A table emulates key allocation | Portable but usually not the first choice on PostgreSQL |

The exercise should use:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Use `Long`, not `long`, because `null` naturally represents “this new Java entity has no database identity yet.” A primitive `0` is an ambiguous application sentinel, not a database identity.

With identity generation, an insert may need to occur earlier than you expect so the database can produce the key. That database round trip can also constrain some insert-batching strategies compared with preallocated sequence values. A successful `save` returning an entity with an ID still must not be generalized into “every `save` immediately issues exactly one INSERT”; state detection, transaction state, generation strategy, and provider behavior matter.

Matching the existing schema is more important than demonstrating every strategy. Switching a working identity column to a sequence solely for a tutorial would mix schema redesign into the ORM migration.

---

## 7. Mapping fields and columns

**⭐⭐⭐⭐⭐ MUST KNOW**

`@Column` expresses how an attribute maps. Common elements include:

| Element | Mapping intent | Important boundary |
|---|---|---|
| `name` | Physical column name | Omit when naming convention already matches |
| `nullable` | DDL/mapping expectation | Does not add a constraint when Hibernate is not managing DDL |
| `unique` | DDL uniqueness hint | Does not replace an existing PostgreSQL `UNIQUE` constraint |
| `length` | Character-column DDL length hint | Not runtime string validation |
| `precision`, `scale` | Decimal DDL/mapping shape | Not request validation and not a migration |
| `insertable` | Whether provider includes column in generated INSERT | Not a user-write authorization rule |
| `updatable` | Whether provider includes column in generated UPDATE | Not a database privilege or immutable-object guarantee |

The actual checked-in `schema.sql` currently declares:

```text
id      BIGINT identity primary key
isbn    VARCHAR(120) UNIQUE, currently nullable
title   VARCHAR(200) NOT NULL
author  VARCHAR(120) NOT NULL
price   NUMERIC(12,2) NOT NULL
stock   INTEGER NOT NULL
```

A mapping must acknowledge that actual schema rather than silently imagining stronger constraints. The HTTP validation contract may be stricter—for example, a nonblank ISBN—without changing what the checked-in PostgreSQL DDL presently enforces.

```java
@Column(name = "isbn", length = 120, unique = true)
private String isbn;

@Column(name = "price", nullable = false, precision = 12, scale = 2)
private BigDecimal price;
```

Three layers answer different questions:

```text
BookRequest validation
    → Is this incoming API value acceptable and explainable to the client?

entity mapping metadata
    → How does Java state correspond to columns?

PostgreSQL constraint
    → Can invalid durable state exist despite concurrency or another writer?
```

Do not copy every DTO validation annotation onto the entity. `@Column(nullable = false)` is not equivalent to `@NotNull`, and neither alone replaces `NOT NULL` in an independently managed schema.

### Checkpoint 2

If Hibernate schema generation is disabled, does `@ManyToOne` create a foreign key? Does `@Column(unique = true)` make the database safe against concurrent duplicate ISBN inserts?

<details>
<summary>Answer</summary>

No. Mapping annotations tell the ORM how the model corresponds to a schema and can influence generated DDL when generation is enabled. They do not alter an externally owned schema by themselves. The actual PostgreSQL foreign key or `UNIQUE` constraint must exist and remains the concurrency-safe integrity boundary.

</details>

---

## 8. Persistence context

**⭐⭐⭐⭐⭐ MUST KNOW**

The persistence context is the central JPA unit-of-work state holder:

```text
EntityManager
    ↓ operates on
Persistence Context
    ├── Book id=10 → one managed Java instance
    ├── original snapshots / tracking information
    ├── pending insert/update/delete work
    └── association proxies and collections
```

It behaves partly as three things:

1. **Identity map:** within one persistence context, one entity type and database identity correspond to one managed Java instance.
2. **First-level cache:** a direct `find` can reuse an already-managed instance instead of hydrating a second one. This cache is mandatory and local to that persistence context, not a cross-request application cache.
3. **Change-tracking scope:** Hibernate knows which managed entities may need an INSERT, UPDATE, or DELETE when it flushes.

```text
entityManager.find(Book.class, 10L)
    ↓ SELECT if not already managed
Book#10 becomes MANAGED
    ↓
book.setStock(3)
    ↓ only Java state has necessarily changed
flush
    ↓ compare tracked state / dirty attributes
UPDATE books SET ... WHERE id = 10
```

Suppose two `find` calls target `Book.class` and ID `10` in the same persistence context:

```java
Book first = entityManager.find(Book.class, 10L);
Book second = entityManager.find(Book.class, 10L);

boolean sameReference = first == second; // normally true in this context
```

That identity guarantee applies to managed entity identity inside one persistence context. It does not imply two HTTP requests, two transactions, or two contexts receive the same Java object. A JPQL query may still execute SQL even when matching entities are managed; Hibernate resolves returned identities to the already-managed instances.

In a typical Spring transaction, application code sees a Spring-managed `EntityManager` proxy while the actual transaction-bound persistence context is associated with the current execution flow. Do not store an `EntityManager` or managed entity as singleton mutable state.

An actual `EntityManager` is not thread-safe. Spring can safely inject its shared proxy into a singleton service because that proxy delegates each call to the appropriate transaction-bound manager; this does not make one underlying manager a concurrent singleton.

---

## 9. Entity lifecycle states

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
new Book(...)
    │
    │ persist
    ▼
MANAGED ───────── remove ────────► REMOVED
    │                                  │
    │ detach / clear / context closes  │ flush issues DELETE;
    ▼                                  │ commit makes it durable
DETACHED                              ▼
                                  no durable row

DETACHED ── merge ──► managed copy
```

| State | Meaning | What changes do |
|---|---|---|
| Transient/new | Ordinary Java entity not associated with a persistence context; usually no durable row identity yet | Mutation is only Java mutation; `persist` can make it managed |
| Managed/persistent | Associated with the current persistence context | Mutations are tracked and may become SQL at flush |
| Detached | Has persistent identity/state but is no longer managed by this context | Mutation is not tracked; `merge` can copy state into a managed instance |
| Removed | Managed entity scheduled for deletion | Flush issues deletion; treating it as an ordinary active entity is incorrect |

Operation rules make the states practical:

- **Transient:** use `persist` for a genuinely new entity; `remove` requires managed state.
- **Managed:** ordinary mapped mutation participates in dirty checking; `remove` schedules deletion; `detach` stops tracking this instance.
- **Detached:** changing fields affects only that Java object; use `merge` when copying its state into the current unit of work is actually intended, and continue with the returned managed instance.
- **Removed:** the instance is scheduled for deletion and should not be merged or treated as active domain state; transaction rollback can cancel the database outcome.

Practical examples:

- `new Book(...)` before `save`/`persist`: transient.
- result of `findById` inside the active service transaction: managed.
- entity returned out of a completed transaction: typically detached.
- entity passed to `remove`: removed until the context flushes and completes.

State is a relationship among an object, an identity, and a particular persistence context. “This class has `@Entity`” does not mean every instance is always managed.

---

## 10. EntityManager

**⭐⭐⭐⭐⭐ MUST KNOW**

`EntityManager` is the central Jakarta Persistence interface for interacting with a persistence context. Spring Data repositories hide many direct calls, but their behavior makes sense only when you understand the underlying operations.

| Operation | Priority | Mental model |
|---|---|---|
| `persist(entity)` | MUST KNOW | Make a new entity managed; schedule/perform insertion as required |
| `find(Type, id)` | MUST KNOW | Return managed state by identity, checking the context before database access |
| `remove(entity)` | MUST KNOW | Mark a managed entity for deletion |
| `merge(entity)` | MUST KNOW | Copy new/detached state into a managed instance and return that instance |
| `flush()` | MUST KNOW | Synchronize pending context changes to SQL; do not commit |
| `getReference(Type, id)` | IMPORTANT | Obtain a reference whose state may be loaded later; absence can surface on access |
| `contains(entity)` | IMPORTANT | Ask whether this exact instance is managed in this context |
| `detach(entity)` | NICE TO KNOW | Stop managing one instance |
| `clear()` | NICE TO KNOW | Detach everything and empty the persistence context |

`getReference` is useful when only an identity reference is needed, but `findById` is clearer for the Book API's explicit “missing ID becomes 404” behavior. A reference can defer the existence check and therefore move failure timing.

The current Spring Data repository counterpart is `getReferenceById`. Older examples may show `getOne` or `getById`; do not copy those superseded names into a Spring Data JPA 4.1 guide. Prefer `findById` when the use case must establish absence immediately.

Direct `EntityManager` use belongs in a repository/custom persistence component or a tightly controlled lab, not in a REST controller.

---

## 11. `persist()` versus `merge()`—and what `save()` means

**⭐⭐⭐⭐⭐ MUST KNOW**

### `persist`: make this new instance managed

```java
Book book = new Book(/* no ID yet */);
entityManager.persist(book);

entityManager.contains(book); // true in the active context
```

The same Java object becomes managed. Its generated ID may be populated when the provider must obtain it, subject to generation strategy and transaction conditions.

### `merge`: copy state, return the managed instance

```java
Book detached = obtainDetachedBook();
Book managed = entityManager.merge(detached);

// Continue with 'managed', not an assumption that 'detached' was reattached.
```

`merge` does not simply attach the same object. It copies state from the supplied instance onto a managed instance—existing or newly created—and returns that managed instance. The supplied detached object normally remains detached.

### Spring Data `save`

Spring Data JPA's default implementation determines whether an entity is new. In the usual case it delegates a new entity to `EntityManager.persist` and a non-new entity to `EntityManager.merge`. Newness detection can use version/identifier state or a `Persistable` strategy.

Therefore this slogan is wrong:

```text
save() = execute INSERT if ID empty, otherwise execute UPDATE now
```

A safer model is:

```text
save(entity)
    ↓ Spring Data decides newness
persist(new) OR merge(non-new)
    ↓ affects persistence-context state
flush timing decides when required SQL reaches the database
```

Always use the instance returned by `save`, especially when merge might be involved. For an entity already managed inside a service transaction, calling `save` after mutation is usually unnecessary for dirty checking.

---

## 12. Dirty checking

**⭐⭐⭐⭐⭐ MUST KNOW**

Dirty checking turns managed mutation into an eventual database update:

```java
@Transactional
public Book changeStock(long id, int stock) {
    Book book = repository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));

    book.changeStock(stock);
    return book;
}
```

The lifecycle is:

```text
transaction begins
    ↓
repository loads Book#10
    ↓ Hibernate registers a MANAGED instance and tracked state
book.changeStock(3)
    ↓ Java state differs
flush before transaction completion
    ↓ Hibernate detects dirty mapped state
UPDATE books SET stock = ? ...
    ↓
commit or rollback
```

No `repository.save(book)` is required merely to inform Hibernate about a mutation to an already-managed entity. The persistence context is already tracking it.

Dirty checking does not work as hoped when:

- the entity is detached because the context closed;
- no write transaction/context will flush the change;
- code mutates an unmapped or `@Transient` value;
- a bulk JPQL/native update changes rows behind the context, leaving managed state stale;
- a read-only transaction/provider optimization suppresses normal change processing;
- application code creates a different object rather than mutating or merging managed state.

Hibernate commonly keeps snapshots and compares them at flush; bytecode enhancement can support attribute-level tracking. You need the behavior, not the enhancement internals, for this topic.

---

## 13. Flush is not commit

**⭐⭐⭐⭐⭐ MUST KNOW**

Flush synchronizes the persistence context with the database by executing required SQL. Commit ends the transaction successfully and makes its changes durable according to database rules.

```text
Java entity changes
    ↓
persistence context tracks pending work
    ↓ FLUSH
INSERT / UPDATE / DELETE sent to PostgreSQL
    ↓ transaction is still active; rollback is still possible
    ↓ COMMIT
database transaction completes durably
```

Flush can occur:

- automatically before transaction commit;
- before a query when the configured flush mode and affected query space require database results to reflect pending changes;
- explicitly through `EntityManager.flush()` or repository `flush()`;
- as part of `saveAndFlush(entity)`.

Do not rely on every query always flushing. With normal `AUTO` behavior, the provider decides whether pending changes could affect query correctness. Flush-mode changes alter this behavior.

`saveAndFlush` means “apply save semantics, then synchronize pending work.” It does not mean “commit this entity.” The surrounding transaction can still roll back everything.

### Why timing matters

```java
try {
    repository.save(book);
} catch (DataIntegrityViolationException exception) {
    // This may be too early if the INSERT is delayed until later flush/commit.
}
```

If the service must translate a known database conflict before leaving its boundary, it can deliberately flush inside the relevant `try` block. That is a targeted correctness choice, not a reason to flush after every repository call.

### Checkpoint 3

If a managed `Book` changes but code never calls `save`, can an `UPDATE` still occur? Does `flush()` mean the transaction committed? If `find` loads the same ID twice in one context, should you expect two independent objects?

<details>
<summary>Answer</summary>

Yes, dirty checking can generate the update during flush. No, flush only synchronizes SQL and the transaction can still roll back. Within one persistence context, the identity-map rule normally gives one managed Java instance for the same entity type and ID.

</details>

---
## 14. Spring Data repository abstraction

**⭐⭐⭐⭐⭐ MUST KNOW**

Spring Data repositories reduce repetitive persistence adapter code. You declare a typed interface:

```java
public interface BookRepository extends JpaRepository<Book, Long> {
}
```

Spring creates a bean backed by its JPA repository implementation and an `EntityManager`. The interface is not the ORM, and it does not own the database connection pool.

### The modern interface family is not one straight ladder

Avoid the obsolete diagram `Repository → CrudRepository → PagingAndSortingRepository → JpaRepository`. In current Spring Data, paging/sorting interfaces are separate repository fragments rather than subclasses of CRUD interfaces.

```text
Repository<T, ID>                         marker/type capture
├── CrudRepository<T, ID>                 CRUD, many results as Iterable
│   └── ListCrudRepository<T, ID>          list-returning CRUD variant
└── PagingAndSortingRepository<T, ID>      Sort and Pageable operations
    └── ListPagingAndSortingRepository     list-returning sorting variant

JpaRepository<T, ID>
    → composes the useful list CRUD/paging capabilities
    → adds JPA-specific operations such as flush and saveAndFlush
```

The exact Java inheritance is less important than the capability model:

| Interface | Use it when |
|---|---|
| `Repository` | You want a selective marker interface and declare only chosen operations |
| `CrudRepository` / `ListCrudRepository` | Generic CRUD is enough; list variant is convenient for multi-row results |
| `PagingAndSortingRepository` / list variant | You need store-neutral paging/sorting fragments |
| `JpaRepository` | You are using JPA and want the practical aggregate of CRUD, paging/sorting, and JPA-specific methods |

For this one-entity exercise, `JpaRepository<Book, Long>` is the straightforward choice.

---

## 15. Repository proxies and runtime implementations

**⭐⭐⭐⭐⭐ MUST KNOW**

You write an interface, but Spring Data registers a runtime implementation as a bean:

```text
BookRepository interface
    ↓ repository scanning finds it
Spring Data repository factory
    ↓ composes base implementation + query-method interceptors
runtime proxy bean
    ↓ delegates a method invocation
SimpleJpaRepository / generated query execution
    ↓
EntityManager → Hibernate ORM
```

This connects directly to Spring Core knowledge:

- component/repository scanning discovers candidates;
- a factory creates and registers a bean;
- dependency injection supplies that bean to `BookService`;
- proxy/interceptor logic chooses an inherited CRUD implementation or a derived/declared query;
- transaction interception can wrap repository calls.

The generated proxy is why no handwritten `BookRepositoryImpl` is required for basic operations. It is not evidence that implementation work vanished; framework components perform it using metadata and conventions.

Method-name mistakes usually fail early because Spring Data parses repository methods during startup. SQL/database errors generally occur when a query is executed or the context flushes.

---

## 16. Basic repository operations

**⭐⭐⭐⭐⭐ MUST KNOW**

| Method | Conceptual effect | SQL caveat |
|---|---|---|
| `save(entity)` | Make new state persistent or merge non-new state | Not a guarantee of immediate one-row SQL |
| `saveAll(entities)` | Apply save semantics to a group | Not automatically optimal JDBC batching |
| `findById(id)` | Find by persistence identity, returning `Optional` | May use the persistence context before a SELECT |
| `findAll()` | Load all entities | Dangerous for large tables |
| `existsById(id)` | Determine whether identity exists | Usually an existence query, not entity loading |
| `count()` | Count entities | Generates a count query |
| `delete(entity)` | Remove entity state | May need managed state/merge and flush later |
| `deleteById(id)` | Request deletion by identity | Do not rely on it alone to implement a guaranteed missing-resource 404 |
| `flush()` | Synchronize all pending context changes | Still not commit |
| `saveAndFlush(entity)` | Save semantics plus explicit synchronization | Still participates in the surrounding transaction |

The important split is:

```text
repository method invocation
    ≠ necessarily
SQL executes at that exact source line
```

Batching, ID generation, first-level cache state, flush mode, transaction boundaries, cascades, and provider strategy all influence SQL shape and timing. Inspect SQL rather than reasoning from method names alone.

For missing DELETE behavior, a clear service flow is `findById → throw BookNotFoundException if absent → delete(managedBook)`. That adds a lookup but preserves the public 404 contract deliberately.

---

## 17. Derived query methods

**⭐⭐⭐⭐⭐ MUST KNOW**

Spring Data can parse a method name into a query over Java property names:

```java
Optional<Book> findByIsbn(String isbn);

List<Book> findByAuthor(String author, Pageable pageable);

boolean existsByIsbn(String isbn);

List<Book> findByTitleContainingIgnoreCase(String text);
```

The subject (`find`, `exists`, `count`, `delete`) selects a result intent. The predicate after `By` describes entity attributes and operators. Useful vocabulary includes:

- `And`, `Or` for predicate composition;
- `Between`, `LessThan`, `GreaterThan` for comparisons;
- `Containing`, `StartingWith` for text patterns;
- `IgnoreCase` where supported and semantically appropriate;
- `OrderBy...Asc` or `OrderBy...Desc` for fixed ordering.

Method parsing speaks in **entity property names**, not column names. `findByAuthor` targets `Book.author` even if its column were named `author_name`.

The Book filter is readable as a derived query:

```java
List<Book> findByAuthor(String author, Pageable pageable);
```

Let `Pageable` carry the required `id ASC` ordering rather than embedding every paging concern in the name.

Replace a derived name with JPQL, a specification, or a custom repository when the name becomes a sentence, grouping of `And`/`Or` is difficult to verify, the query needs joins/fetch plans, or its performance deserves explicit review.

---

## 18. JPQL

**⭐⭐⭐⭐⭐ MUST KNOW**

SQL addresses relational objects:

```sql
SELECT *
FROM books
WHERE author = ?
ORDER BY id ASC;
```

JPQL addresses mapped entities and Java attributes:

```jpql
select b
from Book b
where b.author = :author
order by b.id asc
```

```text
SQL  = table + column + row
JPQL = entity + attribute + association
```

Spring Data lets you declare JPQL with named parameters:

```java
@Query("""
       select b
       from Book b
       where b.author = :author
       order by b.id asc
       """)
List<Book> findBooksByAuthor(@Param("author") String author, Pageable pageable);
```

Hibernate parses JPQL, applies mapping metadata, and generates database SQL. JPQL remains a query language; it does not guarantee good query plans automatically. You still inspect joins, selected data, indexes, and SQL.

For updates/deletes written as JPQL bulk operations, `@Modifying` and transaction handling are needed, and bulk statements bypass ordinary per-entity dirty checking. That is useful but not the default learning path for Book updates.

---

## 19. Native SQL queries

**⭐⭐⭐ NICE TO KNOW**

Native queries keep SQL as SQL:

```java
@Query(value = """
       select *
       from books
       where author = :author
       order by id asc
       """, nativeQuery = true)
List<Book> findNativeByAuthor(@Param("author") String author, Pageable pageable);
```

Native SQL is justified when a PostgreSQL-specific feature, window function, CTE shape, operator, query hint, or carefully tuned plan cannot be expressed well through JPQL or other JPA abstractions.

Tradeoffs include:

- table/column names couple the query to the schema and dialect;
- result mapping must match the entity/projection shape;
- pageable native queries may need an explicit count query when returning `Page`;
- portability and provider-level query rewriting may decrease.

Native SQL is not “bad.” The rule is to choose it because the query benefits, not because the entity/query model was never understood. Always bind parameters; ORM does not make string-concatenated SQL safe.

---

## 20. Pagination and sorting without changing the API

**⭐⭐⭐⭐⭐ MUST KNOW**

The Book API already accepts:

```text
GET /books?page=0&size=20
```

Spring Data represents that request internally:

```java
Pageable pageable = PageRequest.of(
        page,
        size,
        Sort.by(Sort.Direction.ASC, "id")
);
```

| Return model | Contains | Typical SQL/cost | API implication |
|---|---|---|---|
| `Page<T>` | Content, page data, total elements/pages | Content query plus a count query in common cases | Serializing it changes the current array contract |
| `Slice<T>` | Content and whether another slice exists | Commonly requests one extra row; no full total required | Still a wrapper unless content is extracted |
| `List<T>` with `Pageable` | Only requested content | Bounded content query; no promised total | Fits the existing JSON array contract |
| unbounded `List<T>` | All results | Can load the entire table | Unsafe as data grows |

For the current API, a repository method returning `List<Book>` with a `Pageable` is a good fit:

```java
List<Book> findByAuthor(String author, Pageable pageable);
```

For the unfiltered path, calling `findAll(pageable)` returns `Page<Book>` through `JpaRepository`. The service may use `getContent()` while keeping the controller's `List<BookResponse>` contract, though that commonly pays for count metadata the API does not use. A deliberate list-returning pageable query can avoid promising or computing totals.

Always preserve `ORDER BY id ASC`. Pagination without deterministic ordering can move or duplicate items between requests as the database chooses row order.

### Checkpoint 4

Does a `BookRepository` interface contain the implementation at compile time? Does `save()` guarantee immediate SQL? Should this API return `Page<BookResponse>` merely because the repository can?

<details>
<summary>Answer</summary>

No. Spring Data creates a proxy and composes runtime implementations. No repository method name guarantees exact SQL timing. Returning a `Page` directly would change the established JSON-array contract and can add a count query; use paging internally while deliberately preserving the external shape.

</details>

---

## 21. Relationship mappings begin with foreign keys

**⭐⭐⭐⭐⭐ MUST KNOW**

Start with relational truth:

```text
authors
┌────────────┬──────────┐
│ id (PK)    │ name     │
└─────▲──────┴──────────┘
      │ books.author_id (FK)
┌─────┴──────┬──────────┐
│ books.id   │ author_id│
└────────────┴──────────┘
```

Then map object navigation:

```java
@Entity
class Book {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;
}

@Entity
class Author {
    @OneToMany(mappedBy = "author")
    private List<Book> books = new ArrayList<>();
}
```

The four cardinality annotations describe object association shape:

| Mapping | Example | Usual relational expression |
|---|---|---|
| `@OneToOne` | User ↔ Profile | Unique foreign key or shared primary key |
| `@OneToMany` | Author → Books | Foreign key is normally in the many-side table |
| `@ManyToOne` | Many Books → one Author | `books.author_id` foreign key |
| `@ManyToMany` | Books ↔ Categories | Join table with two foreign keys |

Annotations map a relationship that the schema must support. With schema generation disabled, they do not create missing tables or foreign keys.

The production Book Catalog currently stores `author` as text and has no relationship table. Learn associations through a contained Book/Author model; do not distort the stable API merely to demonstrate annotations.

---

## 22. Owning side and inverse side

**⭐⭐⭐⭐⭐ MUST KNOW**

In a bidirectional relationship, both Java objects can navigate the association, but one mapping controls the database relationship.

For `books.author_id`:

```java
class Book {
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;       // owning side: writes the FK
}

class Author {
    @OneToMany(mappedBy = "author")
    private List<Book> books;    // inverse side: mirrors Book.author
}
```

`mappedBy = "author"` names the **Java attribute** on `Book`, not the SQL column. It tells Hibernate, “This collection is the inverse view; the `Book.author` mapping owns the foreign key.”

Changing only the inverse collection may make the Java graph look right without changing `books.author_id`:

```java
author.getBooks().add(book); // insufficient by itself for FK ownership
```

Keep both sides consistent through a helper when bidirectional navigation is genuinely useful:

```java
public void addBook(Book book) {
    books.add(book);
    book.changeAuthor(this);
}
```

Ownership is a mapping/write concept, not a statement that one domain object is more important.

---

## 23. Cascade

**⭐⭐⭐⭐ IMPORTANT**

Cascade propagates an **EntityManager operation** across an association:

| Cascade | Parent operation propagated to child | Typical question |
|---|---|---|
| `CascadeType.PERSIST` | persist | Should a new child be inserted when this new parent is persisted? |
| `CascadeType.MERGE` | merge | Should detached child state be copied during parent merge? |
| `CascadeType.REMOVE` | remove | Should deleting this parent delete the associated child entity? |
| `CascadeType.REFRESH` | refresh | Should database refresh propagate? |
| `CascadeType.DETACH` | detach | Should detachment propagate? |
| `CascadeType.ALL` | all cascade operations | Are all of those lifecycle decisions truly correct? |

`CascadeType.REMOVE` is not PostgreSQL `ON DELETE CASCADE`:

```text
CascadeType.REMOVE
    → ORM propagates remove operations and issues SQL

ON DELETE CASCADE
    → database reacts to a parent-row deletion
```

They can coexist, conflict, or be used separately. Select cascade based on lifecycle ownership. A Book usually must not cascade-remove a shared Author. `CascadeType.ALL` is not a harmless convenience default.

---

## 24. `orphanRemoval`

**⭐⭐⭐⭐ IMPORTANT**

`orphanRemoval = true` means that removing a child from a parent-owned association can schedule that child entity for deletion:

```java
@OneToMany(mappedBy = "order", orphanRemoval = true)
private List<OrderLine> lines;
```

```text
order.removeLine(line)
    ↓ line no longer belongs to its lifecycle owner
flush
    ↓ DELETE order_line ...
```

Compare the mechanisms:

| Mechanism | Trigger |
|---|---|
| cascade `REMOVE` | The parent entity itself is removed |
| `orphanRemoval` | A child is removed from the owning aggregate association |
| database `ON DELETE CASCADE` | The database observes deletion of a referenced parent row |

Use orphan removal only where the child has no independent lifecycle. Removing a Book from an Author collection should not automatically destroy the Book unless that is genuinely the domain rule.

---

## 25. Fetching: lazy and eager

**⭐⭐⭐⭐⭐ MUST KNOW**

Fetch type describes when associated state is requested, not whether the relationship exists.

```text
LAZY  → a Jakarta Persistence hint that loading may be deferred until needed
EAGER → a requirement that the association be available as part of loading,
         through a join or additional query chosen by the provider
```

Jakarta Persistence defaults are:

| Relationship | Default |
|---|---|
| `@ManyToOne` | `EAGER` |
| `@OneToOne` | `EAGER` |
| `@OneToMany` | `LAZY` |
| `@ManyToMany` | `LAZY` |

Defaults are facts, not a fetch-plan design. Hibernate recommends favoring lazy static mappings and requesting needed graphs deliberately. To-one associations should therefore usually state `fetch = LAZY` explicitly when the model permits it.

Hibernate can represent unloaded state with a proxy for an entity reference or a persistent collection wrapper. Accessing it can trigger SQL while the persistence context is open. Consequently, an innocent-looking getter is sometimes a database operation.

EAGER does not promise one join. The provider may issue secondary selects, which can still create N+1 behavior.

---

## 26. `LazyInitializationException`

**⭐⭐⭐⭐⭐ MUST KNOW**

The common lifecycle is:

```text
transaction/context loads Book
    ↓ Book.author remains an unloaded lazy reference
transaction ends; context closes
    ↓ Book is detached
code calls book.getAuthor().getName()
    ↓ no active context can initialize the reference
LazyInitializationException
```

The problem is not “lazy is broken.” The code crossed a loading boundary without loading the data it needed.

Valid solutions depend on the use case:

- fetch exactly the required relationship within the service transaction;
- map the entity to a DTO while the required state is available;
- use a fetch join or entity graph for that query;
- use a projection shaped for the response;
- redesign a boundary that is passing detached entities too far.

Making every association EAGER hides the immediate exception by creating permanent over-fetching and often N+1 queries. Keeping a persistence context open across arbitrary web serialization also hides ownership and performance problems rather than solving the query design.

---

## 27. The N+1 query problem

**⭐⭐⭐⭐⭐ MUST KNOW**

Java code can look like one loop while producing many queries:

```text
1 query:  SELECT ... FROM books                 → 100 books

then for each book.getAuthor():
100 queries: SELECT ... FROM authors WHERE id=? → one per book

total: 1 + 100 = 101 queries
```

The first-level cache can reduce repeated author lookups when many books share the same author, but it does not make the query plan acceptable or predictable.

Diagnose N+1 by observing SQL counts and call sites, not by staring only at the repository method. Common remedies are:

- a JPQL fetch join for a known entity graph;
- `@EntityGraph`/named entity graph to select a fetch plan;
- a DTO/interface projection that selects exactly the read model;
- a deliberately designed batch-fetch strategy where appropriate;
- changing the use case so it does not traverse an unbounded relationship.

Switching everything to EAGER is not a remedy; eager associations can themselves be loaded with secondary selects.

---

## 28. Fetch join

**⭐⭐⭐⭐ IMPORTANT**

An ordinary join can filter without initializing the relationship in returned entities:

```jpql
select b
from Book b
join b.author a
where a.name = :name
```

A fetch join requests the association as part of this entity query:

```jpql
select b
from Book b
join fetch b.author a
where a.name = :name
```

```text
JOIN       → use relationship in query semantics
JOIN FETCH → also initialize that relationship in the result graph
```

Fetch-joining a to-one association is commonly straightforward. Fetch-joining a to-many collection can duplicate root rows at the SQL/result-processing boundary and interacts poorly with limit/offset pagination. Fetching multiple bag-like collections can be especially problematic. Treat a fetch join as a use-case query plan, not a global mapping switch.

---

## 29. Entity graphs

**⭐⭐⭐ NICE TO KNOW**

An entity graph declares which attributes should be fetched for a particular operation without embedding `join fetch` into every query string:

```java
@EntityGraph(attributePaths = "author")
List<Book> findByTitleContaining(String text);
```

It solves the same broad question as a fetch join: “Which related state does this use case need now?” It can keep query predicates separate from fetch-plan selection and can be reused through named graphs.

At recognition level, a **fetch graph** treats listed attributes as eagerly requested and treats unspecified attributes as lazy for that graph, while a **load graph** eagerly requests listed attributes and otherwise respects their mapped fetch behavior. Spring Data's `@EntityGraph` exposes these semantics; choose deliberately rather than memorizing the enum names first.

Entity graphs are not automatic N+1 proof. Inspect the SQL, understand graph semantics, and remain careful with collection fetching and pagination.

### Checkpoint 5

Which side writes `books.author_id` in the bidirectional example? Why can one `findAll()` produce 101 queries? Why can a lazy association fail after a service returns, and why is universal EAGER fetching not the answer?

<details>
<summary>Answer</summary>

`Book.author`, the side with `@JoinColumn`, owns and writes the foreign key; `Author.books` is inverse through `mappedBy`. Iterating unloaded associations can trigger one query per root result. After the persistence context closes, a lazy proxy has no context to load through. A use-case-specific fetch join, entity graph, or projection loads the required state without forcing every query to over-fetch.

</details>

---

## 30. Transactions and JPA

**⭐⭐⭐⭐⭐ MUST KNOW**

JPA's managed unit of work normally lives inside a database transaction:

```text
@Transactional service method enters
    ↓ Spring transaction interceptor begins/binds transaction resources
transaction-scoped persistence context participates
    ↓
entities load and become managed
    ↓
application changes managed state
    ↓
flush synchronizes SQL
    ↓
commit makes it durable OR rollback discards the transaction
```

Writes normally belong inside a transaction because several Java operations may need one atomic database outcome. Dirty checking needs the entity to remain managed until the unit of work flushes. If an unchecked failure escapes, Spring's normal transaction rules cause rollback; exact rollback customization belongs in the next topic.

```java
@Transactional
public Book replace(long id, BookRequest request) {
    Book book = loadRequired(id);  // managed in this transaction
    book.replaceWith(
            request.isbn(),
            request.title(),
            request.author(),
            request.price(),
            request.stock());
    return book;                   // dirty checking handles mapped changes
}
```

The transaction should surround the **use case**, not merely one repository call. That keeps a load, decision, mutation, and flush in one coherent boundary.

This guide deliberately defers detailed propagation modes, isolation levels, rollback-rule customization, transaction synchronization callbacks, and proxy self-invocation. Remember only that `@Transactional` is commonly applied through a Spring proxy and that method boundaries must be designed deliberately.

---

## 31. Repository methods and transactions

**⭐⭐⭐⭐ IMPORTANT**

Spring Data JPA's standard implementation gives inherited CRUD methods transactional metadata: reads are generally marked read-only, while mutating inherited operations use ordinary read-write transactions. A read-only declaration is an optimization hint and intent marker, not a security control and not a universal guarantee that no write can occur.

Declared query methods do not gain transactionality merely because their names start with `find`. Add transaction semantics at the use-case boundary or declare them explicitly when a repository is used independently.

```text
controller
    ↓ no persistence transaction here
@Transactional BookService.replace(...)
    ├── repository.findById(...)
    ├── mutate managed Book
    └── repository.flush() if this use case needs early failure
```

When a service transaction already exists, repository calls participate in it. The outer service boundary determines the cohesive unit of work and can span multiple repository operations. Putting arbitrary transaction annotations in controllers couples HTTP orchestration to persistence lifetime and encourages lazy loading during response construction.

Use `@Transactional(readOnly = true)` for a coherent read use case when helpful, and ordinary `@Transactional` for create/replace/patch/delete. Deeper propagation and isolation design comes next in the roadmap.

---

## 32. Open EntityManager in View / Open Session in View

**⭐⭐⭐⭐ IMPORTANT**

Open EntityManager in View (often called OSIV; Hibernate wording often says Open Session in View) binds an `EntityManager`/persistence context for more of a web request than the service transaction alone:

```text
HTTP request begins
    ↓ persistence context opened/bound
service transaction loads entities
    ↓ transaction ends, request context may remain open
controller/Jackson accesses lazy association
    ↓ additional SQL can occur outside the intended service use case
HTTP response completes; context closes
```

The context remaining open does **not** mean one database transaction stays open for the entire request. Lazy loads after the service can borrow connections and execute work outside the business transaction that selected the root data.

For Spring Boot 4.1.1 servlet JPA applications, `spring.jpa.open-in-view` remains enabled by default when applicable; Boot logs a warning when the application relies on that default. The checked-in Book Catalog does not explicitly set it.

OSIV can make lazy navigation appear convenient, but it can:

- hide N+1 queries in controller or serialization code;
- blur which layer is allowed to fetch data;
- make endpoint performance depend on JSON traversal;
- postpone `LazyInitializationException` lessons rather than producing deliberate fetch plans.

An explicit teaching choice is:

```yaml
spring:
  jpa:
    open-in-view: false
```

Then complete all required database loading inside a service transaction. If a response needs lazy association data, fetch and map that shape before the boundary closes; a scalar-only entity such as this exercise's `Book` can be mapped from its already-loaded basic state immediately afterward. This is a boundary choice, not a universal claim that every application must use the same setting.

---

## 33. DTOs versus entities

**⭐⭐⭐⭐⭐ MUST KNOW**

The existing DTO boundary remains valuable after JPA:

```text
HTTP JSON
    ↓ Jackson + Jakarta Validation
BookRequest / StockRequest
    ↓ service maps accepted values
Book entity (managed persistence model)
    ↓ service/controller maps output
BookResponse
    ↓ Jackson
HTTP JSON
```

Do not return `Book` entities directly just because Hibernate can load them. Entity exposure creates several forms of coupling:

- schema/persistence refactors can silently change the API;
- lazy getters can run SQL during serialization or fail after detachment;
- bidirectional relationships can recurse indefinitely;
- internal fields can leak accidentally;
- clients can begin depending on associations that were not an API promise;
- deserializing input directly into managed types invites over-posting.

Request validation, entity mapping, and database constraints remain separate:

```text
BookRequest annotations → friendly request-boundary rules
Book entity annotations  → object/relational mapping
PostgreSQL constraints   → durable integrity under every writer/concurrency path
```

Converting the `Book` record to an entity means the entity/response boundary must be handled deliberately. You can change `BookResponse.from(Book)` to call conventional getters, or preserve the record-style `id()`, `isbn()`, and similar read accessors on the entity. The companion exercise chooses the second option, so `BookResponse` and the controller remain unchanged. Either choice keeps the DTO boundary; neither is permission to return the entity directly.

### Checkpoint 6

Why does dirty checking usually need a service transaction? Does OSIV keep the business transaction open through JSON serialization? Why should `BookResponse` remain separate from `Book`?

<details>
<summary>Answer</summary>

The transaction-scoped persistence context keeps the loaded entity managed through mutation and flush. OSIV may keep a persistence context available longer, but it does not extend one business transaction across the whole request. A response DTO freezes the HTTP shape, prevents accidental lazy traversal/internal-field exposure, and lets persistence and API models evolve separately.

</details>

---

## 34. Entity `equals()` and `hashCode()`

**⭐⭐⭐ NICE TO KNOW**

Entity equality is subtle because several identities coexist:

- **object identity:** two references point to the same Java instance (`==`);
- **persistence-context identity:** one managed instance per entity type and ID in one context;
- **database identity:** rows share the same primary key;
- sometimes a stable, immutable business/natural identity.

Generated IDs create a timing problem:

```text
new Book → id == null
persist/insert → id becomes 42
```

If `hashCode()` depends on that generated ID and the entity is already in a `HashSet`, its hash can change after persistence, making lookup behavior incorrect. If equality treats every null ID as equal, all new books collapse into one logical item. If it uses mutable title/author fields, changing them breaks hashed collections.

There is no universal one-line recipe. Decide based on whether the domain has an immutable natural key, whether entities cross persistence contexts, whether proxies are involved, and whether the entity enters sets/maps before ID assignment. Keep mutable associations and ordinary mutable fields out of equality/hash calculations. For this beginner migration, identity-sensitive collection behavior should remain simple and documented.

---

## 35. Lombok and entities

**⭐⭐⭐ NICE TO KNOW**

Lombok can reduce typing, but `@Data` is a risky entity default because it generates setters, `equals`, `hashCode`, and `toString` across fields. Those generated methods may:

- traverse lazy associations and issue SQL unexpectedly;
- recurse through bidirectional associations;
- include mutable state in hash codes;
- expose more mutation than the domain intends.

Write the small Book entity explicitly in the exercise. Use a protected no-arg constructor, a creation constructor, the read accessors needed by the response mapper, and narrow mutation methods. The companion exercise preserves record-style read accessors for compatibility. Do not add a dependency simply to save a few lines.

---

## 36. Schema generation and validation modes

**⭐⭐⭐⭐ IMPORTANT**

Hibernate's schema tooling can be selected through Boot's `spring.jpa.hibernate.ddl-auto`:

| Mode | Conceptual action | Appropriate use |
|---|---|---|
| `none` | Do not run Hibernate schema actions | Externally managed schema with no startup compatibility check |
| `validate` | Check selected structural/type compatibility with an existing schema and fail on detected incompatibility | Useful guard when another mechanism owns DDL |
| `update` | Attempt incremental schema alteration | Convenient experiment, not reliable migration management |
| `create` | Create schema objects for this startup, replacing existing mapped schema | Disposable environments only |
| `create-drop` | Create at startup and drop on shutdown | Disposable demos/tests only |

The Book project already uses `spring.sql.init.mode: always` and classpath `src/main/resources/schema.sql`. Boot normally performs script-based DataSource initialization before creating the JPA `EntityManagerFactory`, so `schema.sql` can create the table and Hibernate can then validate it:

```yaml
spring:
  sql:
    init:
      mode: always
  jpa:
    hibernate:
      ddl-auto: validate
```

For a non-embedded database such as this PostgreSQL instance, Boot's inferred `ddl-auto` default is normally `none` when no schema manager changes that decision. Setting `validate` explicitly makes the learning project's intended startup check visible.

`validate` is a useful but limited compatibility check, not a migration. It checks mapped structures such as table/column presence and compatible JDBC types, but default validation does not comprehensively prove nullability, length/precision, `UNIQUE`, `CHECK`, index, default, or business-rule agreement. It will not add the current missing constraints or rewrite an already-existing `CREATE TABLE IF NOT EXISTS` table.

Never present `update` as a production schema-evolution strategy. It lacks the reviewed, ordered, repeatable history expected of real migrations.

---

## 37. Database migrations

**⭐⭐ FUTURE KNOWLEDGE**

Production systems normally evolve schemas with versioned migrations, commonly Flyway or Liquibase in the Spring ecosystem:

```text
versioned migration files
    ↓ applied once in order
schema history table
    ↓
repeatable, reviewable database evolution
```

That gives code review, deployment ordering, and reproducibility that `ddl-auto=update` does not provide. The Book exercise keeps its existing `schema.sql`; adding a migration tool would distract from the ORM transition. Treat migrations as an important later production practice.

---

## 38. SQL visibility

**⭐⭐⭐⭐ IMPORTANT**

Make this your default diagnostic question:

> What SQL did this Java operation actually cause, and when?

For local learning, SQL logging can be enabled explicitly:

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

Logger configuration is often more controllable:

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    # Very verbose and development-only:
    org.hibernate.orm.jdbc.bind: TRACE
```

Observe:

- whether `findById` issues a SELECT or reuses managed state;
- when an identity INSERT occurs and the ID becomes available;
- whether dirty checking updates every column or only selected columns;
- whether a page causes a count query;
- how many association queries a loop triggers;
- whether a flush happens before a query or transaction completion.

Do not enable bind-value trace logging indiscriminately in production. Values can contain credentials, personal data, tokens, or commercially sensitive content, and high-volume SQL logs can become a performance problem.

---

## 39. Hibernate-generated SQL versus hand-written SQL

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
JdbcTemplate
    → you write statement text and map each result row

Hibernate ORM
    → mappings + entity/query operations produce statement text and hydration
```

Generated does not mean unknowable or automatically optimal. Strong SQL knowledge remains necessary to:

- recognize full scans and missing indexes;
- understand joins and cardinality;
- catch N+1 or accidental cartesian products;
- reason about locks and update ordering;
- read PostgreSQL query plans;
- understand why uniqueness or foreign keys fail;
- choose JPQL versus native SQL;
- know when batching is or is not happening.

ORM changes who writes common SQL text. It does not remove your accountability for the SQL workload.

---

## 40. Database constraints remain authoritative

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
Bean Validation
    ↓ rejects friendly, known invalid request values
JPA mapping
    ↓ translates Java state and relationships to relational operations
PostgreSQL constraints
    ↓ reject invalid durable state from every writer and under concurrency
```

Keep actual `UNIQUE`, `NOT NULL`, foreign keys, and applicable `CHECK` constraints. An application pre-check such as `existsByIsbn` can improve a message but has a race:

```text
request A checks: absent       request B checks: absent
request A inserts             request B inserts
                               ↓
database UNIQUE must decide
```

The current checked-in Book schema has a real `UNIQUE` on ISBN and `NOT NULL` on title, author, price, and stock, but it lacks the stronger nonblank/nonnegative checks described in older learning material and currently allows null ISBN at the DDL level. A mapping must not claim those missing constraints exist. `@Column` metadata will not repair them while schema ownership remains external.

### Checkpoint 7

What does `ddl-auto=validate` prove? Does SQL literacy become optional when Hibernate generates statements? Can an `existsByIsbn` pre-check replace PostgreSQL uniqueness?

<details>
<summary>Answer</summary>

Validation checks selected structural/type compatibility at startup; it is not a migration and does not comprehensively prove nullability, length/precision, constraints, indexes, defaults, or business rules. Generated SQL still needs inspection and database knowledge. A pre-check is race-prone, so the database `UNIQUE` constraint remains decisive under concurrent writers.

</details>

---

## 41. Persistence exceptions

**⭐⭐⭐⭐⭐ MUST KNOW**

The current JdbcTemplate path is approximately:

```text
PostgreSQL unique violation
    ↓ pgJDBC SQLException
Spring JDBC translation
    ↓ DuplicateKeyException
BookService
    ↓ DuplicateIsbnException
GlobalExceptionHandler
    ↓ 409 ProblemDetail
```

The JPA path changes its middle while preserving application and HTTP meaning:

```text
PostgreSQL unique violation
    ↓ pgJDBC SQLException
Hibernate ORM exception / Jakarta Persistence wrapper
    ↓ Spring persistence exception translation
DataIntegrityViolationException (typical outward Spring category)
    ↓ BookService classifies the known ISBN conflict
DuplicateIsbnException
    ↓ existing GlobalExceptionHandler
409 ProblemDetail
```

Names that must remain separate:

| Type | Layer and meaning |
|---|---|
| `org.hibernate.exception.ConstraintViolationException` | Hibernate ORM reports that SQL violated a database constraint |
| `jakarta.validation.ConstraintViolationException` | Jakarta Validation reports bean/method constraint violations; Hibernate Validator commonly creates it |
| `org.springframework.dao.DataIntegrityViolationException` | Spring's translated data-access category; may wrap an ORM/JDBC constraint failure |
| `jakarta.persistence.PersistenceException` / `RollbackException` | JPA-level persistence/transaction wrappers that can appear depending on call boundary |

Do not import the wrong `ConstraintViolationException`. Do not expose provider exception types through the controller contract. Translate a known persistence meaning at the service/persistence boundary and let the existing advice translate the application exception to HTTP.

---

## 42. Flush timing and error translation

**⭐⭐⭐⭐⭐ MUST KNOW**

This code has an incomplete timing assumption:

```java
try {
    return repository.save(book);
} catch (DataIntegrityViolationException exception) {
    throw new DuplicateIsbnException(exception);
}
```

If required INSERT/UPDATE SQL is delayed until a later flush or transaction commit, the exception occurs after the `try` block—or even after the method body returns while transaction advice completes.

A service that must translate the conflict at a deterministic point can force synchronization inside the narrow operation:

```java
try {
    Book saved = repository.save(book);
    repository.flush();
    return saved;
} catch (DataIntegrityViolationException exception) {
    if (isIsbnUniqueViolation(exception)) {
        throw new DuplicateIsbnException(exception);
    }
    throw exception;
}
```

For an already-managed replacement, mutate first and call `repository.flush()` inside the same narrow `try`; calling `save` is not required simply for dirty checking.

Two rules matter:

1. Do not convert every `DataIntegrityViolationException` into “duplicate ISBN.” It might represent a `NOT NULL`, check, foreign-key, length, or different uniqueness failure. Inspect reliable cause information such as SQL state (`23505` is PostgreSQL unique violation) and constraint identity where available; constraint names can be null or environment-dependent, so classification needs a documented fallback.
2. Do not flush everywhere. Extra flushes reduce batching/reordering opportunities and couple code to database timing. Force one only where early SQL is required for a meaningful boundary, diagnostic, or subsequent query.

After a flush failure, do not continue normal work in that transaction. Translate and let the transaction roll back.

---

## 43. Optimistic locking

**⭐⭐⭐ NICE TO KNOW**

Two requests can load the same row, make different decisions, and overwrite each other:

```text
A reads stock=5, version=7       B reads stock=5, version=7
A writes stock=4, version=8      B tries write stock=3 where version=7
                                  ↓ zero rows updated → stale version detected
```

JPA supports optimistic locking with a version attribute:

```java
@Version
private long version;
```

Hibernate includes the prior version in update/delete checks and advances it on success. A stale update becomes an optimistic-locking failure during flush/commit rather than silently overwriting newer state.

Do not add `@Version` to the basic migration without deciding how the REST API communicates concurrency and how the schema gains the column. Bulk JPQL/native updates can bypass normal managed-entity/version behavior. Revisit this with deeper transactions and concurrency.

---

## 44. Projections

**⭐⭐⭐ NICE TO KNOW**

A read use case sometimes needs only a few attributes:

```java
public interface BookSummary {
    Long getId();
    String getTitle();
}

List<BookSummary> findByAuthor(String author);
```

Spring Data supports interface and DTO/class-based projections. They can reduce loaded state and avoid materializing an unnecessary entity graph, especially for read-only lists or reports.

Projection kind and query shape determine the actual SQL; nested properties can still cause joins or broader materialization. Inspect generated SQL. Keep projections optional for the first migration because the existing `BookResponse` already provides a clear HTTP boundary and the Book entity is small.

---

## 45. Auditing

**⭐⭐ FUTURE KNOWLEDGE**

Spring Data can populate metadata such as:

```java
@CreatedDate
private Instant createdAt;

@LastModifiedDate
private Instant updatedAt;
```

The annotations alone do nothing. Auditing requires enabling/configuring Spring Data auditing and the appropriate entity-listener infrastructure; user attribution needs an auditor provider.

Do not add audit columns to Book solely to demonstrate annotations. That requires deliberate schema and API/domain decisions and belongs after the core persistence model is understood.

### Checkpoint 8

Why might `save()` not throw a duplicate-key error immediately? Which `ConstraintViolationException` describes a database constraint? Does `@Version` lock the row while a user edits it?

<details>
<summary>Answer</summary>

SQL may be deferred until flush or commit, so the database has not yet rejected the write at the `save` line. `org.hibernate.exception.ConstraintViolationException` is the ORM/database form; `jakarta.validation.ConstraintViolationException` belongs to Bean Validation. `@Version` implements optimistic conflict detection rather than holding a pessimistic database lock throughout user think time.

</details>

---

## 46. Common performance mistakes

**⭐⭐⭐⭐ IMPORTANT**

| Mistake | Why it hurts | Better question/action |
|---|---|---|
| N+1 association traversal | Query count grows with result count | Which graph does this use case need, and how many statements ran? |
| Making every relationship EAGER | Permanent over-fetching; secondary-select N+1 remains possible | Keep mappings deliberate and select fetch plans per use case |
| Loading huge collections | Memory, network, hydration, and dirty-check cost grow | Page, slice, project, or query only needed rows |
| Calling unbounded `findAll()` | Works in a demo, degrades as the table grows | Require a bound and deterministic sort |
| Calling `flush()` after every operation | Loses batching/reordering opportunities and adds round trips | Flush only for a specific timing/visibility need |
| Calling `save()` repeatedly in a large loop | Does not guarantee efficient JDBC batching; persistence context can grow | Design batch size, ID strategy, flush/clear intervals, and transaction scope explicitly |
| Loading full entities for tiny read views | Hydrates/tracks unused state and associations | Consider a projection or focused query |
| Forgetting database indexes | ORM-generated predicates still need efficient access paths | Inspect SQL and PostgreSQL plans |
| Assuming ORM makes SQL performance irrelevant | Hides joins, locks, row counts, and query plans | Treat generated SQL as production code you must observe |

Other traps include offset paging deep into large tables, collection fetch joins with pagination, accidental flushes before queries, and holding thousands of managed entities in one persistence context. Those are signals to inspect the workload, not reasons to abandon ORM automatically.

---

## 47. Common architecture mistakes

**⭐⭐⭐⭐⭐ MUST KNOW**

- Returning JPA entities directly from REST controllers.
- Putting HTTP/Jackson concerns on persistence entities merely to reduce mapping code.
- Moving request DTO validation wholesale onto entities and assuming the boundary is unchanged.
- Exposing Hibernate/JPA exception classes as a controller contract.
- Calling `EntityManager` directly from controllers.
- Putting broad transactions around web rendering instead of use cases.
- Applying `CascadeType.ALL` without lifecycle reasoning.
- Making every relationship bidirectional “just in case.”
- Making every association EAGER to suppress lazy-loading errors.
- Using `ddl-auto=update` as migration management;
- treating `JpaRepository.save()` as guaranteed immediate INSERT/UPDATE SQL;
- calling `save` after every managed mutation because dirty checking was not understood;
- treating `flush` as commit;
- assuming a Spring Data method name removes the need to understand SQL;
- catching every integrity violation and labeling it “duplicate ISBN.”

The common pattern is a boundary collapse: HTTP, use-case, entity-lifecycle, ORM, and database responsibilities become indistinguishable. Restore the layer that owns the decision instead of adding another annotation blindly.

---

## 48. A layer-by-layer debugging strategy

**⭐⭐⭐⭐⭐ MUST KNOW**

Trace from the observed symptom toward the layer that owns it:

```text
REST request
    ↓
DTO binding / validation
    ↓
service use case and transaction
    ↓
Spring Data repository proxy
    ↓
EntityManager
    ↓
persistence context and entity state
    ↓
Hibernate mappings / generated SQL
    ↓
JDBC
    ↓
DataSource / HikariCP
    ↓
pgJDBC
    ↓
PostgreSQL schema, plan, locks, and data
```

### Diagnostic sequence

1. **Freeze the external evidence.** Record method, URI, status, Problem Details body, request values, and whether database state changed.
2. **Classify pre-persistence failures.** Did Jackson bind the DTO? Did Jakarta Validation reject it? Did the controller execute?
3. **Mark the transaction boundary.** Which service method started it? Is the entity still managed at the point of mutation/access?
4. **Identify repository behavior.** Inherited CRUD, derived query, JPQL, or native SQL? Did a proxy create the implementation?
5. **Inspect entity state.** Transient, managed, detached, or removed? Does `entityManager.contains(entity)` confirm your prediction in a controlled lab?
6. **Observe flush timing.** Has SQL run yet? Did a query, explicit flush, or transaction completion trigger it?
7. **Read generated SQL and bind values safely.** Count statements; verify predicates, ordering, selected columns, and joins.
8. **Inspect infrastructure only when indicated.** Pool timeout suggests connection ownership/exhaustion; syntax/constraint/lock errors point lower.
9. **Ask PostgreSQL.** Confirm schema, constraints, actual data, execution plan, and session/lock state.
10. **Trace exception wrapping outward.** PostgreSQL/pgJDBC → Hibernate/JPA → Spring data-access category → application exception → global HTTP advice.

### Symptom-to-layer hints

| Symptom | First places to inspect |
|---|---|
| Repository bean fails at startup | repository method parsing, entity scan, ID/property names |
| “Not an entity” | `@Entity`, package discovery, persistence unit |
| Unknown table/column | actual schema, naming/mapping, startup initialization |
| Update never appears | entity state, transaction, mapped field, flush |
| Duplicate error escapes as 500 | flush location, exception translation/classification, application exception mapping |
| Lazy exception | context lifetime and fetch plan |
| Endpoint becomes slow with more rows | query count, N+1, bounds, indexes, plan |
| Pool timeout | transaction/connection duration, leaks, slow queries, pool capacity—not an entity annotation |

### Checkpoint 9

A PATCH returns 200 but PostgreSQL did not change. What three questions should you ask before adding `repository.save(book)`? A list endpoint becomes slower linearly with result count—what evidence distinguishes one slow query from N+1?

<details>
<summary>Answer</summary>

Ask whether the loaded instance was managed, whether mutation occurred inside a write transaction, and whether a flush/commit completed rather than rolled back. Then inspect mapped state and generated SQL. For the list, count statements and correlate them with association access: one slow statement has one execution and a costly plan; N+1 shows a root query followed by repeated related-row queries.

</details>

---

## 49. Concept priorities—what to master now

This section consolidates the priority markers used throughout the guide.

### ⭐⭐⭐⭐⭐ MUST KNOW

- ORM as mapping/unit-of-work abstraction above SQL, JDBC, and the database;
- Jakarta Persistence/JPA versus Hibernate ORM versus Spring Data JPA versus Spring Boot;
- Hibernate ORM versus Hibernate Validator;
- `@Entity`, `@Table`, `@Id`, generation, and field/column mapping;
- persistence context as identity map, first-level cache, and change-tracking scope;
- transient, managed, detached, and removed states;
- essential `EntityManager` behavior, especially `persist`, `find`, `merge`, `remove`, and `flush`;
- `persist` versus `merge`, Spring Data newness detection, and returned save instance;
- dirty checking and when it does not apply;
- flush versus commit and exception timing;
- `JpaRepository`, runtime repository proxies, basic operations, and SQL timing;
- derived queries and JPQL basics;
- paging with deterministic sorting while preserving the public response shape;
- relationships, owning side, `mappedBy`, and foreign-key control;
- LAZY/EAGER semantics, lazy-boundary failures, and N+1 diagnosis;
- service-level transaction boundaries required for a JPA unit of work;
- DTO/entity/database-integrity separation;
- generated SQL inspection and PostgreSQL constraints.

### ⭐⭐⭐⭐ IMPORTANT

- Boot auto-configuration and DataSource/Hikari reuse;
- identity-strategy consequences;
- cascade/orphan-removal decisions;
- fetch joins and use-case fetch plans;
- repository transaction defaults and OSIV boundaries;
- safe schema validation/ownership;
- persistence exception translation and deliberate flush placement;
- performance/debugging method.

### ⭐⭐⭐ NICE TO KNOW

- native queries as an escape hatch;
- entity graphs and projections;
- equality/hash-code tradeoffs;
- Lombok entity hazards;
- optimistic locking fundamentals.

### ⭐⭐ FUTURE KNOWLEDGE

- transaction propagation, isolation, rollback customization, and synchronization;
- production Flyway/Liquibase adoption;
- auditing and auditor identity;
- advanced batching, second-level/query caches, locking, and concurrency patterns;
- specifications, Criteria API, Querydsl, custom repository fragments, and multi-tenancy.

---

## 50. Checkpoints and cumulative retrieval practice

**⭐⭐⭐⭐⭐ MUST KNOW**

The earlier checkpoints test each cluster. Now answer these without looking back:

1. Does `JpaRepository` replace Hibernate? Does Hibernate replace JDBC?
2. If a managed entity field changes but code never calls `save`, can SQL `UPDATE` still happen?
3. Does `flush` mean the transaction committed?
4. If `findById` targets the same ID twice in one persistence context, should two independent Java objects appear?
5. Does `@ManyToOne` create a PostgreSQL foreign key when Hibernate schema generation is disabled?
6. Which side of a bidirectional relationship actually controls the foreign key, and what does `mappedBy` name?
7. Why can accessing a lazy association fail outside a persistence context?
8. How can one apparent `findAll` produce 101 queries?
9. Does `@Column(unique = true)` replace PostgreSQL `UNIQUE`?
10. Why might `save` not throw a duplicate-key error immediately?
11. What creates the implementation of `BookRepository`, and what does it delegate to?
12. When Hibernate needs a JDBC `Connection`, where does it ultimately come from in this project?

<details>
<summary>Answers</summary>

1. No. Spring Data JPA delegates repository work to JPA; Hibernate ORM implements JPA and still calls JDBC.
2. Yes. Dirty checking can generate the update when a managed entity is flushed in a write transaction.
3. No. Flush sends/synchronizes SQL inside a transaction that can still roll back.
4. Normally no. The persistence-context identity map reuses one managed instance for one type and ID.
5. No. With external schema ownership, the actual database DDL must create the foreign key.
6. The owning mapping—usually the many side with `@JoinColumn`—controls the FK. `mappedBy` names its Java attribute.
7. An unloaded proxy/collection needs an active persistence context/provider session to issue its query.
8. The root query can return 100 entities, and accessing one unloaded related value on each can issue 100 more queries.
9. No. It is mapping/schema-generation metadata; the durable PostgreSQL constraint must remain.
10. The persistence context may defer INSERT/UPDATE SQL until flush or commit.
11. Spring Data's repository factory registers a proxy backed by its JPA implementation, which delegates through `EntityManager` to Hibernate.
12. From the Boot-managed `DataSource`; HikariCP supplies a logical pooled connection backed by pgJDBC to PostgreSQL.

</details>

---

## Complete persistence lifecycle

The complete lifecycle has a startup phase, a request/unit-of-work phase, and a cleanup phase.

### 1. Application startup

```text
Boot reads pom-managed classpath + application.yaml
    ↓
creates/reuses HikariDataSource from spring.datasource.*
    ↓
runs classpath schema.sql because spring.sql.init.mode=always
    ↓
builds EntityManagerFactory with Hibernate ORM
    ↓ ddl-auto=validate checks selected structural/type compatibility
discovers Book @Entity
    ↓
Spring Data discovers BookRepository interface
    ↓
repository factory registers a proxy bean
    ↓
constructor injection supplies it to BookService
```

The pool owns reusable physical connections. Neither repository proxies nor entities own connections.

### 2. Read request

```text
GET /books/10
    ↓ Spring MVC / controller
BookService.findById(10) transaction boundary
    ↓ repository proxy
EntityManager.find / generated query
    ↓ persistence context checked first
Hibernate generates SELECT if data is not already managed
    ↓ borrows JDBC connection from Hikari-backed DataSource
pgJDBC sends SQL to PostgreSQL
    ↓ row returns
Hibernate hydrates Book#10 and marks it MANAGED
    ↓ service returns the fully loaded basic Book state
transaction ends; Book becomes detached
    ↓ controller calls the preserved record-style read accessors
BookResponse crosses the HTTP boundary
connection handle returns to HikariCP
```

This scalar-only Book entity has no lazy relationships, so the companion exercise can preserve its existing controller mapper safely. If a response later needs lazy associations, fetch and shape that required graph deliberately within the service boundary rather than relying on detached traversal.

### 3. Create request

```text
POST JSON → BookRequest validation
    ↓ service constructs transient Book(id=null)
repository.save
    ↓ newness detection → EntityManager.persist
Book becomes managed
    ↓ identity strategy may require INSERT to obtain ID
flush synchronizes remaining work
    ↓ PostgreSQL validates UNIQUE/NOT NULL/etc.
commit
    ↓ service/controller maps ID into Location + BookResponse
```

If PostgreSQL rejects a duplicate ISBN, the service's deliberate flush point must be inside the narrow translation boundary so the known failure becomes `DuplicateIsbnException`; the existing global advice publishes 409.

### 4. Managed update request

```text
PUT/PATCH → validated DTO
    ↓ service transaction begins
findById returns managed Book
    ↓ application mutation method changes mapped state
no explicit save is inherently required
    ↓ flush performs dirty checking
Hibernate generates UPDATE
    ↓ PostgreSQL checks integrity
commit or rollback
```

### 5. Delete request

```text
DELETE /books/10
    ↓ findById makes absence explicit
managed Book → repository.delete / EntityManager.remove
    ↓ entity state becomes REMOVED
flush → DELETE SQL
    ↓ commit → controller returns empty 204
```

### Failure and cleanup ownership

| Failure point | Typical owner/category | Outward policy |
|---|---|---|
| JSON/validation | Spring MVC + Hibernate Validator | Existing 400 Problem Details handling |
| Missing managed row | Service application meaning | `BookNotFoundException` → 404 |
| Known ISBN uniqueness | PostgreSQL → Hibernate/JPA → Spring translation → service | `DuplicateIsbnException` → 409 |
| Unexpected mapping/SQL/infrastructure error | Persistence/infrastructure | Sanitized 500; do not relabel as client error |

On success or failure, transaction synchronization ends the persistence context, JDBC connection handles return to HikariCP, and the pool retains/reuses physical connections until application shutdown.

---

## Common mistakes

Use this short list as a review alarm:

| Incorrect thought | Correct mental replacement |
|---|---|
| “JPA is the ORM library running my SQL.” | Jakarta Persistence is the specification; Hibernate ORM is this project's provider. |
| “Spring Data replaces Hibernate.” | Spring Data's repository proxy delegates through JPA to Hibernate. |
| “Hibernate replaces JDBC/Hikari.” | Hibernate uses JDBC connections obtained from the Boot-managed Hikari `DataSource`. |
| “An `@Entity` object is always managed.” | Management belongs to one persistence context and has a lifecycle. |
| “`save` means SQL happened.” | `save` changes persistence-context state through persist/merge; flush controls synchronization timing. |
| “`merge` reattaches my object.” | Merge copies state and returns a managed instance. |
| “Flush committed.” | Flush issued SQL inside a still-active transaction. |
| “EAGER means one efficient join.” | EAGER is a fetch requirement; it can use secondary selects and produce N+1. |
| “`mappedBy` names the foreign-key column.” | It names the owning Java attribute. |
| “Cascade remove equals `ON DELETE CASCADE`.” | One is ORM operation propagation; the other is database behavior. |
| “`@Column(unique=true)` protects concurrency.” | Only the real database uniqueness constraint is decisive. |
| “Returning entities avoids needless DTO code.” | It couples HTTP to persistence and lazy graphs. |
| “`ddl-auto=update` is migration management.” | Use reviewed versioned migrations for production evolution. |
| “Every integrity error is duplicate ISBN.” | Classify the specific known constraint; rethrow unknown failures. |

---

## 51. Final mental model

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
HTTP request
    ↓
Spring MVC + Jackson
    ↓
Jakarta Validation / Hibernate Validator
    ↓
BookController                 HTTP orchestration only
    ↓
BookService                   use-case + @Transactional boundary
    ↓
Spring Data repository proxy  generated/inherited query dispatch
    ↓
EntityManager                 Jakarta Persistence API
    ↓
Persistence Context           identity map + managed state + dirty tracking
    ↓
Hibernate ORM                 provider, mapping, SQL generation, hydration
    ↓
JDBC                          standard database calls
    ↓
DataSource / HikariCP         logical handles + pooled physical connections
    ↓
pgJDBC                        PostgreSQL driver/protocol
    ↓
PostgreSQL                    rows, indexes, locks, durable constraints
```

Read the same picture upward for a failure:

```text
PostgreSQL constraint rejects SQL
    ↑ pgJDBC exposes database error
Hibernate/JPA wrap it
    ↑ Spring translates the persistence category
service assigns known application meaning
    ↑ global advice assigns safe HTTP meaning
ProblemDetail response
```

The abstraction adds leverage at each layer, but no layer erases the one beneath it.

---

## 52. 20-minute review cheat sheet

### First five minutes: name the stack

```text
JPA / Jakarta Persistence = specification and API
Hibernate ORM             = JPA implementation/provider
Spring Data JPA           = repository/query abstraction above JPA
Spring Boot               = managed dependencies + conditional infrastructure
Hibernate Validator       = Jakarta Validation provider, not Hibernate ORM

JpaRepository
    → EntityManager
    → persistence context
    → Hibernate ORM
    → JDBC
    → DataSource/HikariCP
    → pgJDBC
    → PostgreSQL
```

### Next five minutes: entities and unit of work

```text
@Entity = persistent type
@Id     = persistence identity

transient --persist--> managed --detach/context close--> detached
managed   --remove-->  removed
detached  --merge-->   managed copy (returned instance)

Persistence Context = identity map + first-level cache + change tracking

managed entity mutation
    → dirty checking
    → flush
    → SQL

flush != commit
```

### Next five minutes: queries and relationships

```text
derived query = method parsed over entity property names
JPQL          = entity + attribute
SQL           = table + column

Page  = content + total metadata, commonly needs count
Slice = content + has-next knowledge
List  = content only

owning side = mapping that controls FK
mappedBy    = Java attribute on owning side

LAZY  = defer association loading until needed
EAGER = request association immediately; not a one-join promise

N+1 = 1 root query + N related queries
remedies = designed fetch join / EntityGraph / projection / query
```

### Final five minutes: boundaries and failure timing

```text
DTO validation != entity mapping != database integrity
DTO != Entity
JPA != database
Hibernate != JDBC replacement
@Column(unique=true) != replacement for PostgreSQL UNIQUE

save
    → newness decision
    → persist or merge
    → SQL may wait for flush

known unique failure
    → deliberate flush in service translation boundary
    → classify exact conflict
    → DuplicateIsbnException
    → existing advice returns 409

@Transactional service use case
    → persistence context
    → managed work
    → flush
    → commit or rollback
```

## Readiness checklist

Before moving to deeper Spring transactions, verify that you can do each item without notes.

### Abstractions and infrastructure

- [ ] Explain why ORM exists and name at least three problems it does not solve.
- [ ] Distinguish Jakarta Persistence/JPA, Hibernate ORM, Spring Data JPA, and Spring Boot.
- [ ] Distinguish Hibernate ORM 7.4.5.Final from Hibernate Validator 9.1.3.Final.
- [ ] Draw the complete path from `JpaRepository` through JDBC, HikariCP, pgJDBC, and PostgreSQL.
- [ ] Explain why the JPA starter makes direct JDBC infrastructure available transitively.

### Entity mapping and identity

- [ ] Explain `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, and `@Transient`.
- [ ] Explain why the current `Book` record becomes an ordinary entity class while DTOs remain records.
- [ ] Compare `IDENTITY`, `SEQUENCE`, `AUTO`, and `TABLE`, and justify `IDENTITY` for the existing schema.
- [ ] Explain why a nullable `Long` ID represents newness more clearly than primitive `long`.
- [ ] Explain why mapping metadata does not replace database DDL or request validation.

### Persistence context and entity state

- [ ] Define persistence context, identity map, first-level cache, and change-tracking scope.
- [ ] Classify an entity as transient, managed, detached, or removed in a concrete scenario.
- [ ] Explain essential `EntityManager` operations and prioritize the ones used daily.
- [ ] Explain `persist` versus `merge`, including why the returned merge/save instance matters.
- [ ] Predict whether two same-ID loads in one context reuse a Java object.
- [ ] Explain dirty checking and list cases where it will not persist a mutation.
- [ ] Explain flush versus commit and name the main automatic/explicit flush triggers.

### Repositories and queries

- [ ] Explain what creates a Spring Data repository implementation at runtime.
- [ ] Describe the current repository interface family without drawing an obsolete linear hierarchy.
- [ ] Explain the conceptual meaning and SQL caveat of `save`, `findById`, `delete`, `flush`, and `saveAndFlush`.
- [ ] Write and explain a readable derived query method.
- [ ] Translate one SQL query into JPQL and explain table/column versus entity/attribute names.
- [ ] State when native SQL is the better tool.
- [ ] Compare `Page`, `Slice`, and `List` and preserve the Book API's JSON-array contract.
- [ ] Require deterministic sorting for paging.

### Relationships and fetching

- [ ] Explain `@OneToOne`, `@OneToMany`, `@ManyToOne`, and `@ManyToMany` from their relational design.
- [ ] Identify the owning side and explain `mappedBy` in a bidirectional relationship.
- [ ] Distinguish cascade operations, orphan removal, and database `ON DELETE CASCADE`.
- [ ] Explain LAZY/EAGER semantics and defaults without treating defaults as a design.
- [ ] Explain a `LazyInitializationException` from persistence-context lifetime.
- [ ] Predict a 1 + N query sequence and select an appropriate remedy.
- [ ] Explain fetch-join duplication/pagination risks and the purpose of an entity graph.

### Boundaries, SQL, and failure behavior

- [ ] Place a basic JPA transaction at the service use-case boundary and explain why.
- [ ] Explain inherited repository transaction defaults versus declared query methods.
- [ ] Explain OSIV and why an open persistence context is not one open business transaction.
- [ ] Keep `BookRequest`, `StockRequest`, and `BookResponse` separate from the entity.
- [ ] Explain entity equality/hash-code hazards with generated IDs.
- [ ] Explain why entity `@Data` can be dangerous and why Lombok is unnecessary here.
- [ ] Compare `none`, `validate`, `update`, `create`, and `create-drop`.
- [ ] Explain why Flyway/Liquibase, not `ddl-auto=update`, is the future production direction.
- [ ] Inspect generated SQL and avoid sensitive production bind logging.
- [ ] Keep PostgreSQL `UNIQUE`, `NOT NULL`, foreign keys, and applicable checks.
- [ ] Distinguish Hibernate ORM's database constraint exception from Jakarta Validation's exception.
- [ ] Explain why a duplicate failure may occur at flush/commit and how a narrow deliberate flush preserves 409 translation.
- [ ] Recognize optimistic locking, projections, and auditing without forcing them into the basic migration.
- [ ] Diagnose a problem layer by layer before changing annotations.

### Ready for the next topic

- [ ] I can use basic `@Transactional` boundaries for managed JPA work.
- [ ] I have not mistaken this guide for full coverage of propagation, isolation, rollback rules, locking, or transaction synchronization.
- [ ] I am ready to study those deeper transaction behaviors next.
