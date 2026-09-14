# Spring Data JPA + Hibernate Exercise

## Existing project

This exercise evolves the checked-in project at:

~~~text
Validation/book-catalog-api/
~~~

It is the same Book Catalog API already used for REST, validation, and global exception handling. Do not create a JPA demo beside it.

The checked-in source is the implementation baseline. The existing public HTTP behavior is the contract. The two are not perfectly aligned, so start with these facts:

| Baseline fact | Decision for this exercise |
|---|---|
| pom.xml uses Spring Boot 4.1.1 and Java 21 | Keep both exact versions |
| The repository is a concrete JdbcTemplate class | Replace only this persistence path |
| Book is a record with primitive long id | Convert it to a JPA entity with nullable Long identity |
| schema.sql is under src/main/resources | Keep Spring SQL initialization and this schema owner |
| The inline PostgreSQL ISBN UNIQUE constraint is normally named books_isbn_key | Use that current name when classifying the known conflict |
| application.yaml already configures PostgreSQL and HikariCP | Retain the DataSource URL, credentials approach, and pool sizes |
| GlobalExceptionHandler already uses Spring Framework 7 ProblemDetail handling | Leave it unchanged |
| The current validation exercise is unfinished | Complete that earlier exercise before treating its intended negative contract as active |
| No requests directory is checked in | Do not invent or reproduce fixtures in the JPA solution |

> **Prerequisite discrepancy:** The current checkout does not yet contain the completed validation result described by Validation_Global_Exception_Handling_Exercise.md. StockRequest has no constraints, BookRequest still has a decimal-rule TODO, and the controller's size upper bound/message are inconsistent. This JPA exercise does not repair those files because that would mix two learning topics. Finish the earlier validation exercise first if you expect its full 400 matrix.

> **Schema discrepancy:** The actual schema declares isbn VARCHAR(120) UNIQUE but nullable; title VARCHAR(200) NOT NULL; author VARCHAR(120) NOT NULL; price NUMERIC(12,2) NOT NULL; and stock INTEGER NOT NULL. It does not contain the earlier guide's ISBN NOT NULL/length-20 or nonnegative CHECK constraints. The entity in this exercise maps the checked-in schema exactly. ddl-auto=validate cannot add missing constraints, and CREATE TABLE IF NOT EXISTS cannot repair an existing volume.

Never print or copy values from the real .env file. The checked-in placeholder/configuration approach remains outside this persistence migration.

The successful API contract remains:

| Operation | Success contract |
|---|---|
| GET /books | 200 and a JSON array, possibly [] |
| GET /books/{id} | 200 and one BookResponse |
| POST /books | 201, Location: /books/{id}, and the created BookResponse |
| PUT /books/{id} | 200 and the full replaced representation |
| PATCH /books/{id} | 200 and the representation with logically only stock changed |
| DELETE /books/{id} | 204 and no body |

The intended failure distinctions also remain: invalid input is 400, an absent book is 404, the known duplicate-ISBN conflict is 409, and an unexpected persistence failure is a sanitized 500.

## Learning contract

For each task:

1. Read **Objective** and **Concept**.
2. Attempt **What to implement** using the incomplete **Starter code**.
3. Open **Hints** only after making a prediction.
4. Perform **How to verify** against PostgreSQL.
5. Review **Common mistakes** and **Explanation**.

Starter fragments intentionally contain TODOs and are not complete files. A complete, internally consistent reference appears only after the stop barrier.

This remains a manual HTTP exercise. Use Postman, curl.exe, PowerShell, logs, and a debugger. Do not add JUnit migration, MockMvc, Testcontainers, @DataJpaTest, Spring Security, deployment work, or a Docker image for the Java application.

The task rhythm, before/after diagrams, manual matrices, stop barrier, and file-by-file reference follow the house style established by DataSource_Connection_Pool_Exercise.md and Validation_Global_Exception_Handling_Exercise.md.

The migration teaches enough @Transactional behavior for a correct JPA unit of work. Propagation combinations, isolation levels, advanced rollback rules, transaction synchronization, and optimistic-lock retry policy belong to the next transactions topic.

## Before architecture

~~~text
HTTP
  |
Jackson + Validation
  |
BookController
  |
BookService
  |
BookRepository class
  |
JdbcTemplate
  +-- handwritten SELECT / INSERT / UPDATE / DELETE
  +-- RowMapper<Book>
  |
DataSource
  |
HikariCP
  |
pgJDBC
  |
PostgreSQL

Expected/framework exception
  |
GlobalExceptionHandler
  |
ProblemDetail + HTTP status
~~~

Trace a current GET:

~~~text
GET /books/{id}
 -> BookController.findById
 -> BookService.findById
 -> BookRepository.findById
 -> JdbcTemplate.query
 -> SELECT ... WHERE id = ?
 -> RowMapper creates Book record
 -> BookResponse
~~~

Trace a current POST:

~~~text
POST /books
 -> Jackson + request validation
 -> BookController.create
 -> BookService.create
 -> BookRepository.create
 -> INSERT ... RETURNING ...
 -> RowMapper creates Book record
 -> 201 + Location + BookResponse
~~~

## Target architecture

Only the persistence portion changes:

~~~text
HTTP
  |
Jackson + Validation
  |
BookController
  |
BookService
  |  @Transactional unit of work
  |
Spring Data BookRepository proxy
  |
EntityManager
  |
Persistence Context
  |
Hibernate ORM
  |
generated SQL
  |
JDBC
  |
Boot-managed DataSource
  |
HikariCP
  |
pgJDBC
  |
PostgreSQL

Expected/framework exception
  |
unchanged GlobalExceptionHandler
  |
unchanged ProblemDetail contract
~~~

Spring Data does not replace Hibernate. Hibernate does not replace JDBC. JPA does not replace the database, and none of these replaces the pool.

## Files that remain unchanged

Paths are relative to Validation/book-catalog-api.

| File or area | Why it remains unchanged |
|---|---|
| compose.yaml | PostgreSQL 17, port mapping, health check, and volume already work |
| src/main/resources/schema.sql | It deliberately owns the table definition; Hibernate performs limited startup compatibility checks |
| .env and .env.example | Secret/configuration cleanup is unrelated; never reproduce .env |
| src/main/java/com/example/bookcatalog/Application.java | Boot already scans entities and repositories below this package |
| dto/BookRequest.java | Request-boundary validation must not migrate to the entity |
| dto/StockRequest.java | Same boundary rule; finish its earlier validation task separately |
| dto/BookResponse.java | Record-style entity accessors preserve its current mapper and JSON |
| exception/BookNotFoundException.java | Absence still has the same application meaning |
| exception/DuplicateIsbnException.java | The known conflict still maps to the same 409 |
| web/BookController.java | Routes, DTOs, status codes, and Location behavior remain stable |
| web/GlobalExceptionHandler.java | Existing Spring 7 ProblemDetail handling remains correct |
| src/test/java/com/example/AppTest.java | Automated testing migration is explicitly deferred |
| Existing project guides and any fixtures completed earlier | Documentation and manual input files are not persistence infrastructure |

The solution does not reproduce these unchanged files.

## Files to modify/add/remove

Exactly five project files change in the reference design:

| File | Action | Why JPA/Hibernate requires it |
|---|---|---|
| pom.xml | MODIFY | Replace the direct JDBC starter with Boot's Data JPA starter |
| model/Book.java | MODIFY | Replace the record with a mapped, mutable entity |
| repository/BookRepository.java | MODIFY | Replace JdbcTemplate, RowMapper, and manual CRUD SQL with a repository interface |
| service/BookService.java | MODIFY | Define transactions, use managed state, paging, flush, and narrow integrity translation |
| src/main/resources/application.yaml | MODIFY | Validate the existing schema, disable OSIV deliberately, and expose development SQL |

No new production file is required. BookRepository.java changes from a class to an interface at the same path. No controller, DTO, exception, advice, schema, Compose, fixture, or test file changes.

## JdbcTemplate versus JPA/Hibernate

| Before | After |
|---|---|
| Handwritten CRUD SQL | Hibernate-generated SQL, plus derived queries or JPQL when needed |
| RowMapper | Entity mapping metadata |
| Repository class | Repository interface implemented by a Spring Data proxy |
| INSERT ... RETURNING | Entity construction plus save/flush and generated identity |
| Manual UPDATE | Mutation of a managed entity plus dirty checking |
| LIMIT/OFFSET arithmetic | Pageable/PageRequest |
| Manual author SELECT | Derived query |
| DuplicateKeyException path | DataIntegrityViolationException with a Hibernate ORM cause |

Generated SQL is not automatically better SQL. You still inspect it, reason about indexes, understand query count, and keep PostgreSQL constraints.

## Concept priorities

| Priority | Concepts |
|---|---|
| 5/5 MUST KNOW | JPA/Hibernate/Spring Data distinction, entity mapping, identity, persistence context, entity states, dirty checking, flush versus commit, repository proxy, generated SQL, DTO separation |
| 4/5 IMPORTANT | Service transaction boundary, derived query, JPQL, Pageable, exception timing, lazy loading, owning side, N+1 |
| 3/5 NICE TO KNOW | EntityGraph, projections, native queries, explicit flush tradeoffs, provider details |
| 2/5 FUTURE KNOWLEDGE | @Version policy, transaction propagation/isolation, auditing, migrations, automated persistence tests |

## Exercise Tasks — Attempt Before Reading the Solution

### Task 1 — Audit the JdbcTemplate persistence model

**Objective**

Freeze the real implementation and public contract before changing persistence.

**Concept**

A safe migration changes one responsibility at a time. Here the persistence mechanism changes; HTTP, validation, exception representation, database, and pool ownership do not.

**What to implement**

Do not edit code. Inspect the POM, Book, repository, service, controller, DTOs, exceptions, global advice, YAML, Compose file, and schema. Trace one GET and one POST to PostgreSQL. Record actual facts rather than relying on an older guide.

**Starter code**

~~~text
Question                                      Observation
Boot version                                  TODO
Java release                                  TODO
Current Book representation                   TODO
Owner of SQL                                  TODO
Owner of row-to-object mapping                TODO
GET /books order and paging                   TODO
POST generated-ID mechanism                   TODO
Current duplicate exception                  TODO
DataSource implementation                     TODO
Actual ISBN column definition                 TODO
Actual stock database constraints             TODO
Files with unfinished validation work         TODO
~~~

**Hints**

1. Look at source files, not target/classes.
2. Follow both success and exception paths.
3. schema.sql is in src/main/resources, not a Compose-mounted database directory.
4. Record absent constraints too.

**How to verify**

Explain a current GET and POST aloud from controller to pgJDBC. Then identify every public behavior that the JPA migration is forbidden to change.

**Common mistakes**

- Assuming the project already equals the previous guide's final solution.
- Describing HikariCP as repository code.
- Treating DTO validation metadata as a database constraint.
- Reading or copying the real .env contents.

**Explanation**

The real baseline is Boot 4.1.1, Java 21, JdbcTemplate, a Book record, PostgreSQL, and a Boot-managed HikariDataSource. The checkout's validation and schema drift are prerequisites, not hidden permission for this exercise to edit unrelated files.

---

### Task 2 — Add Spring Data JPA and draw the runtime stack

**Objective**

Replace the direct JDBC starter with Spring Boot's JPA starter and identify every retained layer.

**Concept**

The starter supplies Spring Data JPA and Hibernate ORM and still brings Spring JDBC infrastructure transitively. Boot manages compatible versions. Hibernate ultimately borrows JDBC connections from the same Hikari-backed DataSource.

**What to implement**

Replace only the existing persistence starter. Do not specify Hibernate, Spring Data, Jakarta Persistence, or Hikari versions manually. Leave Boot 4.1.1 and Java 21 unchanged.

**Starter code**

~~~xml
<!-- Remove the direct JDBC starter, then add: -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId><!-- TODO: Boot Data JPA starter --></artifactId>
</dependency>
~~~

Complete this stack:

~~~text
BookRepository proxy
 -> TODO
 -> Hibernate ORM
 -> TODO
 -> DataSource
 -> TODO
 -> PostgreSQL
~~~

**Hints**

1. The artifact is spring-boot-starter-data-jpa.
2. Do not add an explicit version.
3. Spring Data JPA is not the Jakarta Persistence specification.
4. Look for spring-jdbc, HikariCP, Hibernate core, and the PostgreSQL driver in the dependency tree.

**How to verify**

~~~powershell
mvn compile
mvn dependency:tree '-Dincludes=org.springframework.boot:spring-boot-starter-data-jpa,org.springframework.data:spring-data-jpa,org.hibernate.orm:hibernate-core,org.springframework:spring-jdbc,com.zaxxer:HikariCP,org.postgresql:postgresql'
~~~

Draw the resolved stack. Answer: "When Hibernate needs to execute SQL, where does its JDBC Connection ultimately come from?"

**Common mistakes**

- Keeping both starters without recognizing that direct JDBC is now redundant.
- Manually pinning a Hibernate version.
- Saying JPA eliminates JDBC.
- Removing the PostgreSQL driver or Hikari configuration.

**Explanation**

The answer is the Boot-managed DataSource/HikariCP infrastructure already used by JdbcTemplate. The higher-level persistence API changes; connection pooling does not disappear.

---

### Task 3 — Configure Hibernate safely

**Objective**

Make Hibernate validate, rather than mutate, the checked-in PostgreSQL schema and expose useful development SQL.

**Concept**

Schema validation checks useful structural compatibility such as table/column presence and compatible JDBC types. It is not comprehensive: do not assume it proves nullability, length/precision, `CHECK`, index, or `UNIQUE` agreement. It is also not a migration and creates none of those things. OSIV controls whether a web request can keep a persistence context available beyond the service transaction.

**What to implement**

Retain datasource, Hikari, SQL initialization, Jackson, and server settings. Add JPA configuration for ddl-auto validate, explicitly disable OSIV as this exercise's DTO/service-boundary choice, format SQL, and enable the org.hibernate.SQL logger. Do not enable bind-value logging.

**Starter code**

~~~yaml
spring:
  # existing sections stay
  jpa:
    open-in-view: TODO
    hibernate:
      ddl-auto: TODO
    properties:
      hibernate:
        format_sql: TODO

logging:
  level:
    org.hibernate.SQL: TODO
~~~

**Hints**

1. Use validate, never update, create, or create-drop here.
2. Keep spring.sql.init.mode: always so the existing schema script remains the owner.
3. OSIV is enabled by default in a typical servlet JPA application; false makes lazy access outside the service boundary fail visibly.
4. SQL text is useful; bind values can leak sensitive data.

**How to verify**

Start PostgreSQL, then start the application. Confirm schema validation succeeds and Hibernate SQL appears only when an operation executes. Temporarily point one local annotation at a nonexistent table/column, or choose a clearly incompatible JDBC type, observe startup validation fail, then restore it. Do not use a length, nullability, precision, index, or uniqueness mismatch as this proof; inspect those separately because default validation is not comprehensive.

**Common mistakes**

- Assuming validate repairs a schema.
- Using update as a migration system.
- Replacing the real database with H2.
- Enabling verbose parameter logging in production guidance.
- Presenting OSIV false as a universal rule rather than a deliberate boundary.

**Explanation**

Boot initializes schema.sql before the entity manager factory needs the table. Hibernate then performs its limited compatibility validation. Manual schema comparison and database constraints remain authoritative. Explicit OSIV false keeps required database access and lazy initialization service-owned; the unchanged controller can safely map this entity's already-loaded scalar state immediately afterward.

---

### Task 4 — Convert Book into a schema-matching entity

**Objective**

Map the existing books table without inventing stricter constraints.

**Concept**

An entity has persistence identity and mutable managed state. Field annotations describe mapping metadata. With Hibernate schema generation disabled, they do not alter PostgreSQL.

**What to implement**

Replace the Book record with a non-final class using field access. Map every actual column. Use a nullable Long ID with IDENTITY. Map ISBN as length 120, unique, and nullable; title and author with actual lengths and non-nullability; price precision 12/scale 2 and non-nullability; and stock non-nullability.

**Starter code**

~~~java
@Entity
@Table(name = "TODO")
public class Book {

    @Id
    @GeneratedValue(strategy = TODO)
    private Long id;

    @Column(name = "isbn", length = TODO, unique = true)
    private String isbn;

    // TODO: title, author, price, stock mappings
}
~~~

**Hints**

1. Put @Id on the field to select field access.
2. Use GenerationType.IDENTITY for GENERATED ALWAYS AS IDENTITY.
3. Do not add nullable=false to ISBN; the checked-in column is nullable.
4. Do not invent a stock CHECK annotation or constraint.

**How to verify**

Compare each annotation, type, length, precision, scale, and nullable flag with schema.sql one column at a time. Start the app with ddl-auto validate.

**Common mistakes**

- Using primitive long for a not-yet-persisted ID.
- Copying the DTO's ISBN maximum of 20 into the entity mapping when the actual column is 120.
- Claiming @Column(unique=true) replaces PostgreSQL UNIQUE.
- Adding Bean Validation annotations to the entity merely because it is persistent.

**Explanation**

The DTO, entity, and table have different jobs. BookRequest controls accepted HTTP input, Book maps persisted state, and PostgreSQL enforces the constraints actually declared in schema.sql.

---

### Task 5 — Satisfy entity construction and access requirements

**Objective**

Make the entity usable by Hibernate while preserving unchanged controller/response code.

**Concept**

Hibernate needs a no-argument constructor and benefits from a non-final entity for proxy options. New entities have no database ID yet. Domain mutation should be narrow rather than exposed through arbitrary setters.

**What to implement**

Add a protected no-argument constructor, a creation constructor without ID, record-style read accessors id(), isbn(), title(), author(), price(), and stock(), plus replaceWith(...) and changeStock(...) methods. Do not add Lombok.

**Starter code**

~~~java
protected Book() {
}

public Book(String isbn, String title, String author,
        BigDecimal price, Integer stock) {
    // TODO
}

public Long id() {
    return TODO;
}

public void replaceWith(/* TODO */) {
    // TODO: update all five writable values
}

public void changeStock(Integer stock) {
    // TODO: update exactly the logical stock field
}
~~~

**Hints**

1. The protected constructor may be empty.
2. Do not accept an ID in the public creation constructor.
3. Record-style accessors allow BookResponse and BookController to remain unchanged.
4. Avoid @Data: entity equality, hash codes, and association traversal need deliberate design.

**How to verify**

Compile with the unchanged BookResponse.from(Book). Set a breakpoint in the protected constructor during a GET and observe Hibernate materialize the class.

**Common mistakes**

- Making the entity final.
- Omitting the no-argument constructor.
- Adding a public setId.
- Generating equality/hashCode from every mutable field.
- Changing BookResponse solely because the entity stopped being a record.

**Explanation**

An ordinary class is the clear default for a mutable managed entity. The narrow methods state intent, while the compatibility accessors keep the REST layer isolated from this persistence refactor.

---

### Task 6 — Replace manual BookRepository with a Spring Data repository

**Objective**

Remove JdbcTemplate, RowMapper, and handwritten CRUD SQL from the repository.

**Concept**

Spring creates a runtime proxy for a repository interface. The proxy delegates persistence work through an EntityManager to Hibernate. JpaRepository adds JPA-specific operations such as flush and saveAndFlush.

**What to implement**

Change BookRepository from a class into an interface extending JpaRepository<Book, Long>. Declare only the list/query methods the service needs.

**Starter code**

~~~java
public interface BookRepository
        extends JpaRepository<TODO, TODO> {

    List<Book> findAllBy(TODO pageable);

    List<Book> findByAuthor(TODO);
}
~~~

Delete from this file:

~~~text
JdbcTemplate field and constructor
RowMapper
SELECT / INSERT / UPDATE / DELETE strings
manual create, replace, setStock, and delete implementations
@Repository annotation
~~~

**Hints**

1. Spring scans this package below Application automatically.
2. The generated bean is a proxy; there is no handwritten implementation file.
3. Return List, not Page, to help preserve the public array response.
4. JpaRepository<Book, Long> must agree with the entity ID type.

**How to verify**

Run mvn compile and inspect startup logs. In a debugger, inspect the injected repository's runtime class. Re-run the dependency tree and confirm HikariCP and spring-jdbc still exist.

**Common mistakes**

- Creating BookRepositoryImpl for inherited CRUD.
- Leaving manual CRUD SQL "just in case."
- Adding @Repository because the interface otherwise looks empty.
- Returning entities directly from the controller.
- Believing the proxy is Hibernate itself.

**Explanation**

Spring Data supplies the repository implementation. It interprets method metadata and calls an EntityManager; Hibernate implements JPA, generates SQL, and still uses JDBC and HikariCP.

---

### Task 7 — Recreate exact author filtering

**Objective**

Preserve GET /books?author=... with exact equality, pagination, and ID-ascending order.

**Concept**

A readable derived method lets Spring Data parse property names into a query. Pageable carries limit, offset, and sorting separately from the method name.

**What to implement**

Declare one unfiltered list method accepting Pageable and one exact author method accepting String plus Pageable. Supply Sort.by(id).ascending() from the service rather than encoding order in a long name.

**Starter code**

~~~java
List<Book> findAllBy(Pageable pageable);

List<Book> findByTODO(String author, Pageable pageable);
~~~

Prediction:

~~~text
Method fragment findByAuthor means SQL predicate: TODO
PageRequest page 2, size 20 means offset: TODO
Sort property "id" refers to entity/table name: TODO / TODO
~~~

**Hints**

1. findByAuthor means equality unless another keyword is present.
2. The sort property is the Java entity attribute id, not an arbitrary SQL fragment.
3. Keep null author as "no filter"; do not turn it into author IS NULL.

**How to verify**

Create books for two exact author values, including a case variation. Request filtered and unfiltered pages and inspect SQL for WHERE author = ?, ORDER BY id ASC, offset, and fetch/limit behavior.

**Common mistakes**

- Accidentally using Contains or IgnoreCase and changing semantics.
- Passing null into findByAuthor and expecting "all."
- Returning Page from the web endpoint.
- Building ORDER BY from raw request text.

**Explanation**

Derived queries remove this small handwritten SELECT while keeping its semantics visible. If a name becomes difficult to read, JPQL or a specification is preferable; method-name length is not a virtue.

---

### Task 8 — Adapt read operations

**Objective**

Use the repository proxy for lookup and list reads while preserving absence and response shape.

**Concept**

findById returns Optional. A read-only service transaction defines the persistence-context boundary in which this scalar-only entity is loaded and fully initialized for the response. The unchanged controller maps its already-loaded basic state immediately afterward. An association-bearing design must fetch and shape required lazy state before that boundary closes. A class-level read-only transaction is a default, not a security control.

**What to implement**

Annotate the service class read-only. Keep findById throwing BookNotFoundException. Build a PageRequest with page, size, and ID-ascending Sort; dispatch to the unfiltered or author-derived method.

**Starter code**

~~~java
@Service
@Transactional(readOnly = true)
public class BookService {

    public Book findById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> TODO);
    }

    public List<Book> findAll(String author, int page, int size) {
        Pageable pageable = PageRequest.of(
                TODO,
                TODO,
                Sort.by(TODO).ascending());
        return author == null
                ? repo.TODO(pageable)
                : repo.TODO(author, pageable);
    }
}
~~~

**Hints**

1. Use the entity attribute name id in Sort.
2. PageRequest computes a long offset, avoiding the old page * size arithmetic in application code.
3. Do not change the controller's List<BookResponse> mapping.

**How to verify**

Trace:

~~~text
Controller
 -> service transaction proxy
 -> repository proxy
 -> EntityManager
 -> persistence context
 -> Hibernate
 -> SQL/JDBC
 -> HikariCP
 -> PostgreSQL
~~~

Verify GET one, absent GET, unfiltered list, exact author filter, page boundaries, and ID order.

**Common mistakes**

- Returning Optional from the service and moving absence logic back to the controller.
- Sorting by a column name that differs from the entity attribute.
- Calling unbounded inherited findAll().
- Returning Page and changing the JSON shape.

**Explanation**

The repository proxy hides repetitive plumbing, not the persistence model. The service still owns the application meaning of absence and the unit-of-work boundary.

---

### Task 9 — Persist creation and generated identity

**Objective**

Replace INSERT ... RETURNING with entity construction and JPA persistence while preserving 201, Location, and BookResponse.

**Concept**

A new Book is transient and has id == null. save delegates to JPA persist for a new entity. GenerationType.IDENTITY lets PostgreSQL generate the key; Hibernate obtains it and assigns it to the managed entity. Identity generation often requires an early INSERT, but exact SQL timing must still be observed rather than assumed.

**What to implement**

Mark create as a write transaction. Construct Book without an ID. Persist it through the repository and ensure the returned entity has an assigned ID before the controller builds Location. The next task will make the deliberate flush and conflict translation exact.

**Starter code**

~~~java
@Transactional
public Book create(String isbn, String title, String author,
        BigDecimal price, Integer stock) {
    Book book = new Book(TODO);
    Book saved = repo.TODO(book);
    // Predict saved.id() here: TODO
    return saved;
}
~~~

**Hints**

1. Do not accept an ID from BookRequest.
2. IDENTITY matches GENERATED ALWAYS AS IDENTITY.
3. save is a repository operation, not a promise of exactly one immediate SQL statement.
4. The unchanged controller still creates /books/{id}.

**How to verify**

POST a unique book. Record the transient ID before persistence, the managed ID afterward, the INSERT SQL, the 201 response, the Location header, and a confirming GET.

**Common mistakes**

- Initializing new IDs to 0.
- Using primitive long and treating 0 as "not persisted."
- Calling an inherited constructor with a client-controlled ID.
- Changing POST to 200.
- Claiming save always means immediate INSERT.

**Explanation**

The generated identity becomes entity state. The controller need not know how it was obtained; it only reads id() after the service has completed the persistence operation.

---

### Task 10 — Preserve duplicate ISBN as a narrow 409

**Objective**

Translate only the known database UNIQUE violation into DuplicateIsbnException at a deterministic service boundary.

**Concept**

Hibernate can delay SQL until flush. Spring translates persistence failures to DataIntegrityViolationException. Its cause chain can contain org.hibernate.exception.ConstraintViolationException, which is a database/ORM exception and is not jakarta.validation.ConstraintViolationException.

**What to implement**

Use saveAndFlush during create. Catch DataIntegrityViolationException around the flushing operation. Walk its causes and require Hibernate ConstraintKind.UNIQUE. If Hibernate reports a constraint name, require books_isbn_key. Permit a null-name fallback only in create/replace, where ISBN is the sole client-controlled unique value in this exact model. Rethrow every unknown integrity failure unchanged.

**Starter code**

~~~java
private static final String ISBN_UNIQUE_CONSTRAINT = "TODO";

@Transactional
public Book create(/* parameters unchanged */) {
    try {
        return repo.TODO(new Book(/* TODO */));
    } catch (DataIntegrityViolationException failure) {
        throw translateKnownIsbnConflict(failure);
    }
}

private static RuntimeException translateKnownIsbnConflict(
        DataIntegrityViolationException failure) {
    org.hibernate.exception.ConstraintViolationException constraint =
            findHibernateConstraintViolation(failure);

    if (constraint == null
            || constraint.getKind() != TODO) {
        return failure;
    }

    String name = constraint.getConstraintName();
    if (name == null || TODO.equalsIgnoreCase(name)) {
        return new DuplicateIsbnException(failure);
    }
    return failure;
}
~~~

**Hints**

1. Hibernate ORM 7 exposes getKind() and ConstraintKind.UNIQUE.
2. getConstraintName() is nullable because some databases/dialects cannot extract it.
3. PostgreSQL normally names this inline constraint books_isbn_key; confirm rather than guess.
4. Do not import jakarta.validation.ConstraintViolationException.
5. Do not translate NOT NULL, CHECK, FK, or an unrelated UNIQUE constraint to "ISBN conflict."

**How to verify**

Create the same non-null ISBN twice. The second INSERT should execute during saveAndFlush, the service should throw DuplicateIsbnException, and the unchanged advice should return 409. Then temporarily provoke a different integrity failure outside normal validated input and confirm it is not mislabeled as ISBN conflict.

To inspect the current constraint:

~~~sql
SELECT conname, contype
FROM pg_constraint
WHERE conrelid = 'books'::regclass
ORDER BY conname;
~~~

**Common mistakes**

- Catching the Jakarta Validation exception.
- Catching around save while SQL happens only at transaction completion.
- Turning every DataIntegrityViolationException into 409.
- Depending only on a name that may be null.
- Parsing localized exception-message text.
- Flushing every repository operation.

**Explanation**

The intended path becomes:

~~~text
PostgreSQL UNIQUE violation
 -> Hibernate ORM ConstraintViolationException(kind = UNIQUE)
 -> Spring DataIntegrityViolationException
 -> BookService checks kind and known name
 -> DuplicateIsbnException
 -> unchanged GlobalExceptionHandler
 -> 409 ProblemDetail
~~~

The null-name fallback is intentionally narrow, not a reusable global rule. It is acceptable here only because create and full replacement expose ISBN as the only client-controlled unique value. An unknown integrity problem is rethrown and reaches the existing sanitized 500 path.

---

### Task 11 — Implement PUT with managed state and dirty checking

**Objective**

Preserve full replacement semantics and the duplicate-ISBN contract without calling save on an already managed entity.

**Concept**

An entity loaded in an active transaction is managed. Hibernate snapshots it, detects changed state, and generates UPDATE during flush. merge/save is unnecessary for that managed instance.

**What to implement**

Mark replace as a write transaction. Load or throw 404, call replaceWith with all five writable fields, then flush inside the same narrow integrity catch used by create. Return the managed entity.

**Starter code**

~~~java
@Transactional
public Book replace(long id, String isbn, String title,
        String author, BigDecimal price, Integer stock) {
    Book book = required(id);
    book.TODO(isbn, title, author, price, stock);

    try {
        // TODO: deliberately synchronize here
        return book;
    } catch (DataIntegrityViolationException failure) {
        throw translateKnownIsbnConflict(failure);
    }
}
~~~

Before coding, answer:

~~~text
Is book managed after required(id)? TODO
Is repo.save(book) necessary? TODO
Why flush before leaving the try block? TODO
If ISBN conflicts, should the transaction commit other changes? TODO
~~~

**Hints**

1. required(id) executes inside the same service transaction.
2. Call repo.flush(), not save, to observe this UPDATE in the catch block.
3. The runtime exception causes rollback.
4. Full replacement means update ISBN, title, author, price, and stock.

**How to verify**

PUT all new values into an existing ID and confirm 200 plus stored replacement. PUT a missing ID and confirm 404. PUT another book's ISBN and confirm 409 with no partial state persisted.

**Common mistakes**

- Constructing a detached Book with an ID and blindly calling save.
- Omitting a writable field and turning PUT into a partial update.
- Catching outside the flush point.
- Catching integrity exceptions in the controller.
- Believing no explicit save means no UPDATE.

**Explanation**

The unit of work is load -> mutate managed state -> flush -> commit. The explicit flush is for predictable conflict translation, not to make dirty checking function. Without the conflict-timing requirement, transaction completion could flush automatically.

---

### Task 12 — Implement stock PATCH with dirty checking

**Objective**

Keep the logical PATCH limited to stock.

**Concept**

Changing one field on a managed entity is sufficient for dirty checking. However, default Hibernate UPDATE SQL may assign multiple mapped columns unless dynamic-update behavior is configured. Logical API semantics and the exact generated SET list are related but not identical.

**What to implement**

Mark setStock as a write transaction. Load or throw 404, call changeStock(stock), and return the managed entity. Do not call save and do not add a production endpoint or entity annotation merely to force a one-column SQL statement.

**Starter code**

~~~java
@Transactional
public Book setStock(long id, Integer stock) {
    Book book = TODO;
    book.TODO(stock);
    return TODO;
}
~~~

**Hints**

1. The unchanged DTO/controller own HTTP validation.
2. The entity remains managed until transaction completion.
3. Compare values before and after, then inspect the actual UPDATE.
4. @DynamicUpdate is not required for correct PATCH semantics.

**How to verify**

Capture a GET, PATCH only stock, then GET again. Confirm ISBN, title, author, and price values are unchanged. Inspect SQL and record whether Hibernate assigns only stock or more columns.

**Common mistakes**

- Calling replaceWith and accidentally changing other values.
- Requiring repo.save(book) for managed dirty checking.
- Claiming the generated SQL must contain only SET stock.
- Moving StockRequest constraints to the entity.

**Explanation**

At the application level, only stock is mutated. Hibernate is allowed to generate an UPDATE that assigns unchanged column values too. That implementation choice does not turn the logical PATCH into a full replacement.

The entity has no @Version in this exercise. Concurrent lost-update protection and optimistic locking are future knowledge, not something to smuggle into this migration.

---

### Task 13 — Implement DELETE with explicit absence

**Objective**

Preserve existing = 204 and missing = 404.

**Concept**

delete(entity), deleteById(id), and find-then-delete express different observability. Current Spring Data deleteById may silently do nothing for a missing ID, so it cannot by itself preserve the application's 404 contract.

**What to implement**

Use a write transaction. Load the entity with the existing required lookup, then delete that managed entity.

**Starter code**

~~~java
@Transactional
public void delete(long id) {
    Book book = TODO;
    repo.TODO(book);
}
~~~

Comparison:

| Option | Missing-row behavior | Selection |
|---|---|---|
| repo.deleteById(id) | May be a no-op | Not sufficient alone |
| existsById then deleteById | Can issue extra work and still races | Avoid |
| findById then delete(entity) | Explicitly produces current 404 meaning | Use |

**Hints**

1. Reuse required(id).
2. The unchanged controller still constructs the empty 204.
3. SQL can be delayed until flush/commit; no client-specific delete conflict is being mapped here.

**How to verify**

Delete an existing ID and check 204 with an empty body, then GET it and check 404. Delete that ID again and check 404.

**Common mistakes**

- Assuming deleteById always throws EmptyResultDataAccessException.
- Returning 204 for a missing book without acknowledging a contract change.
- Performing existence and deletion in separate service transactions.
- returning the deleted entity from the controller.

**Explanation**

The select is deliberate: absence has application meaning. One transaction contains lookup and removal, and the existing web layer remains unchanged.

---

### Task 14 — Persistence-context debugger lab

**Objective**

Observe identity reuse, managed mutation, flush, and transaction completion without adding a production endpoint or test.

**Concept**

Within one persistence context, repeated lookup of the same entity identity normally returns the same Java instance. The first-level cache is scoped to that context, not global application memory.

**What to implement**

Use an existing transactional write method as the lab. Temporarily add the marked lines immediately after required(id), set a debugger breakpoint, invoke PUT or PATCH externally, record observations, and remove the lines before completing the exercise.

**Starter code**

~~~java
Book first = required(id);

// TEMPORARY LAB - remove after observation
Book second = repo.findById(id).orElseThrow();
boolean sameReference = first == second; // TODO: predict, then inspect
boolean managed = TODO;                  // inspect via EntityManager.contains if desired

// Continue the real mutation.
first.TODO(/* value */);

// Predict SQL before flush: TODO
repo.flush();
// Predict SQL after flush but before commit: TODO
~~~

Observation record:

~~~text
First lookup emitted SELECT: TODO
Second lookup emitted SELECT: TODO
first == second: TODO
Mutation immediately emitted UPDATE: TODO
Flush emitted UPDATE: TODO
Flush committed transaction: TODO
After a new request, same Java reference expected: TODO
~~~

**Hints**

1. Stay inside one service invocation and transaction.
2. EntityManager.contains is optional; inject it only temporarily if the debugger cannot show state clearly.
3. Flush synchronizes SQL but does not commit.
4. Remove every lab line afterward.

**How to verify**

Use the SQL logger and transaction-aware debugger. Pause before and after the second lookup, mutation, flush, and method return. Confirm no lab endpoint or lab class remains in the final diff.

**Common mistakes**

- Comparing objects loaded in separate HTTP requests.
- Calling clear between lookups and still expecting the same object.
- Treating the first-level cache as a query-result cache.
- Committing sample data changes unintentionally.
- leaving diagnostic code in the final solution.

**Explanation**

The persistence context guarantees one managed representation per entity identity within its scope. Dirty checking observes state in that scope. Flush sends SQL; the surrounding transaction later commits or rolls it back.

---

### Task 15 — Classify entity lifecycle states

**Objective**

Predict transient, managed, detached, and removed state before checking the answer.

**Concept**

State describes an entity's relationship to a persistence context, not whether a Java variable exists.

**What to implement**

Classify each scenario and predict whether a later field mutation is automatically dirty-checked.

**Starter code**

~~~text
Scenario                                                        State       Auto UPDATE?
new Book(...) before save                                       TODO        TODO
saveAndFlush result while create transaction is active          TODO        TODO
Book returned after service transaction has completed           TODO        TODO
entity after EntityManager.detach(entity)                        TODO        TODO
entity after EntityManager.clear()                               TODO        TODO
entity passed to delete while transaction remains active         TODO        TODO
object returned by merge(detached)                               TODO        TODO
original detached object passed to merge                         TODO        TODO
~~~

**Hints**

1. merge returns a managed copy; it does not reattach the original instance.
2. Removed entities remain associated until flush/completion but are scheduled for deletion.
3. A DTO has no JPA lifecycle state.

**How to verify**

Use EntityManager.contains in a temporary debugger experiment for the ambiguous cases. Do not expose lifecycle diagnostics over HTTP.

**Common mistakes**

- Calling every object with a non-null ID "managed."
- Assuming a returned entity stays managed forever.
- Mutating a detached entity and expecting automatic SQL.
- Calling a removed entity detached immediately.

**Explanation**

The useful mental model is:

~~~text
new -> transient
persist/find/query -> managed
detach/clear/context closes -> detached
remove(managed) -> removed
merge(detached) -> a different managed copy
~~~

---

### Task 16 — Compare a derived query with JPQL

**Objective**

Express the same read first through method-name derivation and then through JPQL.

**Concept**

Derived queries use Java property names. JPQL uses entity and attribute names, not SQL table and column names. Native SQL uses database names.

**What to implement**

Keep findByAuthor(String, Pageable) in the core solution. Temporarily replace it with an equivalent @Query method, verify behavior and SQL, then restore the simpler derived method before the final diff.

**Starter code**

~~~java
// Derived form
List<Book> findByTODO(String author, Pageable pageable);

// Temporary equivalent JPQL form
@Query("""
        select b
        from TODO b
        where b.TODO = :author
        """)
List<Book> findByAuthorWithJpql(
        @Param("author") String author,
        Pageable pageable);
~~~

Fill in:

~~~text
JPQL entity name: TODO
JPQL attribute name: TODO
SQL table name: TODO
SQL column name: TODO
Why this is not a native query: TODO
~~~

**Hints**

1. The JPQL FROM target is Book, not books.
2. b.author is a Java entity attribute.
3. Pageable still supplies sorting and limits.
4. Use a native query only when database-specific SQL is justified.

**How to verify**

Run the same exact-author request with each declaration. Compare result, generated SQL, and repository readability. Remove the temporary JPQL method/imports afterward.

**Common mistakes**

- Writing FROM books in JPQL.
- Using column aliases as entity properties.
- Marking nativeQuery=true for ordinary entity queries.
- Keeping two redundant production methods only to display both styles.

**Explanation**

The derived method is clearest for one equality predicate. JPQL becomes valuable when joins, projections, or predicates outgrow a readable method name. Neither eliminates the need to understand the generated SQL.

---

### Task 17 — Preserve pagination, sorting, and array JSON

**Objective**

Replace LIMIT/OFFSET arithmetic with PageRequest without changing the controller contract.

**Concept**

Pageable is a request for a window. Page adds content plus total-count metadata and commonly triggers a count query. Slice knows whether another slice exists without necessarily computing a total. List returns only rows.

**What to implement**

Use PageRequest.of(page, size, Sort.by("id").ascending()) and repository methods returning List<Book>. Keep the controller returning List<BookResponse>.

**Starter code**

~~~java
Pageable pageable = PageRequest.of(
        TODO,
        TODO,
        Sort.by(Sort.Direction.TODO, "TODO"));

List<Book> books = author == null
        ? repo.TODO(pageable)
        : repo.TODO(author, pageable);
~~~

Comparison:

| Return type | Content | Total count | Public JSON risk |
|---|---|---|---|
| List<Book> | Yes | No | Preserves array |
| Slice<Book> | Yes | No total | Wrapper unless unwrapped |
| Page<Book> | Yes | Usually yes | Changes shape if returned directly |

**Hints**

1. page is zero-based.
2. The old offset was page * size; PageRequest carries the equivalent safely.
3. Use a stable order for repeatable pages.

**How to verify**

Create enough books for multiple pages. Request page 0 and page 1 with a small size. Confirm no overlap, ascending IDs, exact author filtering, and a top-level JSON array. Inspect whether a count query appears.

**Common mistakes**

- Returning Page<BookResponse> directly.
- Omitting deterministic sorting.
- Using one-based page arithmetic.
- Fetching all records and slicing in memory.
- Claiming every pageable query must execute a count.

**Explanation**

The persistence implementation becomes idiomatic without expanding the REST representation. A future API may intentionally expose page metadata, but that would be a separate public-contract change.

---

### Task 18 — Inspect generated SQL for every CRUD path

**Objective**

Map Java persistence operations to the SQL Hibernate actually emits.

**Concept**

ORM moves SQL generation; it does not remove SQL consequences. Dirty checking, flush mode, identity generation, and repository choices influence statement count and timing.

**What to implement**

Run one controlled scenario for GET list, GET one, POST, PUT, PATCH, and DELETE. Before each call, predict SQL shape and timing. Then record logs.

**Starter code**

~~~text
Operation | Predicted SQL | Observed SQL | When emitted | Expected?
GET one  | TODO          | TODO         | TODO         | TODO
GET list | TODO          | TODO         | TODO         | TODO
POST     | TODO          | TODO         | TODO         | TODO
PUT      | TODO          | TODO         | TODO         | TODO
PATCH    | TODO          | TODO         | TODO         | TODO
DELETE   | TODO          | TODO         | TODO         | TODO
~~~

Trace one statement to its connection source:

~~~text
repository proxy
 -> EntityManager
 -> persistence context
 -> Hibernate SQL generation
 -> JDBC
 -> DataSource
 -> HikariCP borrows Connection
 -> PostgreSQL
~~~

**Hints**

1. A missing DELETE deliberately selects first.
2. POST uses saveAndFlush for deterministic constraint observation.
3. PUT explicitly flushes after mutation.
4. PATCH may update more columns than the one Java field changed.
5. Logger output without bind values is still enough to reason about structure.

**How to verify**

Save the prediction table, call the endpoints, and compare it with org.hibernate.SQL output. Use PostgreSQL observation tools if desired, but do not enable unsafe production logging.

**Common mistakes**

- Declaring success because the endpoint returned 2xx without reading SQL.
- Equating one repository call with one statement.
- Calling flush to make every log easier.
- Ignoring extra selects or unbounded result sets.

**Explanation**

SQL inspection closes the abstraction loop. You should be able to explain both the Java intent and the database work, including connection acquisition and transaction timing.

---

### Task 19 — Model a relationship in a disposable Book/Author lab

**Objective**

Practice many-to-one, one-to-many, ownership, mappedBy, lazy loading, and cascade decisions without changing the production schema or API.

**Concept**

In a conventional Book/Author model, the books table contains author_id. Therefore Book.author owns the foreign key and Author.books is the inverse side. mappedBy names the owning Java attribute; it is not a column name.

**What to implement**

Work only in this document, a scratch pad, or disposable files that never enter the final diff. Sketch an alternative relationship model and its hypothetical schema. Do not replace the real books.author VARCHAR column.

**Starter code**

~~~sql
-- DISPOSABLE MODEL ONLY; do not run against the exercise database
CREATE TABLE authors (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL
);

ALTER TABLE books
    ADD COLUMN author_id BIGINT REFERENCES authors(id);
~~~

~~~java
// DISPOSABLE MODEL ONLY
class Book {
    @ManyToOne(fetch = FetchType.TODO, cascade = { TODO_CASCADE })
    @JoinColumn(name = "TODO")
    private Author author;
}

class Author {
    @OneToMany(mappedBy = "TODO", cascade = { TODO_CASCADE })
    private List<Book> books = new ArrayList<>();

    void addBook(Book book) {
        // TODO: keep both Java sides consistent
    }
}
~~~

Decision table:

| Question | Prediction |
|---|---|
| Which side writes author_id? | TODO |
| Does changing only Author.books change the FK? | TODO |
| Should deleting an author automatically delete every book? | TODO |
| Is CascadeType.ALL justified? | TODO |
| Does this mapping create the FK when ddl-auto is validate? | TODO |

**Hints**

1. Book is the owning side because its table holds author_id.
2. mappedBy = "author" refers to Book.author.
3. Prefer LAZY explicitly for the many-to-one in this teaching model.
4. Cascade persist may be reasonable in some aggregate designs; cascade remove is a separate, dangerous decision.
5. ORM cascade and database ON DELETE CASCADE are different mechanisms.

**How to verify**

Explain which SQL should execute after changing only the inverse collection, after setting Book.author, and after persisting a new graph under each cascade choice. Confirm no relationship file or schema change appears in the real project diff.

**Common mistakes**

- Writing mappedBy = "author_id".
- Mutating only the inverse side and expecting the FK to change.
- Adding CascadeType.ALL by habit.
- Making every relationship bidirectional.
- Altering the production Book API to satisfy a teaching example.

**Explanation**

Relationships are essential JPA knowledge, but the current schema stores author as text. A contained model teaches correct ownership without distorting the required migration. The complete solution intentionally contains no Author entity or relationship.

---

### Task 20 — Diagnose N+1 in the disposable model

**Objective**

Predict query count and choose a use-case-specific fetch plan.

**Concept**

N+1 occurs when one query loads N parent/root rows and later lazy access executes one related query per row. For 100 books, that can be 1 + 100 = 101 statements.

**What to implement**

Reason over the Task 19 model only. Sketch one JPQL fetch join and one EntityGraph alternative. Do not add either to the production Book repository.

**Starter code**

~~~java
List<Book> books = entityManager.createQuery(
        "select b from Book b", Book.class)
        .getResultList();

for (Book book : books) {
    System.out.println(book.author().name());
}

// Predicted query count for 100 books: TODO

@Query("""
        select b
        from Book b
        join fetch TODO
        """)
List<Book> findBooksWithAuthors();

@EntityGraph(attributePaths = "TODO")
@Query("select b from Book b")
List<Book> findAllWithAuthors();
~~~

**Hints**

1. Lazy is not the bug; unplanned access is.
2. A fetch join changes what is fetched, while an ordinary join can be used only for filtering.
3. Fetching a to-many collection can duplicate roots and complicate pagination.
4. EntityGraph expresses a fetch plan without embedding the join in JPQL.

**How to verify**

On paper, calculate the statements for N = 0, 1, 10, and 100. Explain when the fetch join or EntityGraph is appropriate and when a projection would load less data. Keep the actual project unchanged.

**Common mistakes**

- Fixing all N+1 risks by making every association EAGER.
- Assuming one repository method means one SQL query.
- Combining pageable root queries with collection fetch joins without analyzing duplicates/counts.
- Fetching a full graph for every use case.

**Explanation**

Fetch behavior belongs to a query use case. Keep relationships lazy by default where appropriate, load exactly what the DTO needs within the transaction, and verify SQL count.

---

### Task 21 — Explain the lazy-loading boundary

**Objective**

Understand why lazy association access can fail after a persistence context closes and choose a boundary-aware remedy.

**Concept**

A lazy proxy or collection may hold only an identity/loading plan. After the service transaction and persistence context end, Hibernate cannot execute the missing query. Access then can throw LazyInitializationException. OSIV can postpone closure, but it also lets web mapping trigger database work and can hide N+1.

**What to implement**

Use the Task 19 disposable model. Predict the result when a service returns a Book without initializing author and code accesses book.author().name() after the transaction. List three deliberate remedies.

**Starter code**

~~~text
managed Book + uninitialized lazy author
 -> service transaction completes
 -> persistence context closes
 -> web layer accesses author.name
 -> TODO exception

Possible deliberate remedies:
1. TODO query/fetch plan
2. Map TODO inside the transaction
3. Use a TODO projection

Rejected blanket remedy:
make every relationship TODO
~~~

**Hints**

1. JOIN FETCH and EntityGraph are query-specific options.
2. DTO mapping in the transactional service is an architectural option.
3. A projection can bypass an unnecessary entity graph.
4. open-in-view=false makes an accidental boundary crossing visible.

**How to verify**

Explain why the failure is about context lifetime, not a corrupt row. Contrast an initialized association with an uninitialized association after detachment. Confirm the production solution has no relationship through which to trigger this error.

**Common mistakes**

- Treating OSIV as the only possible fix.
- Changing every association to EAGER.
- Serializing JPA entities directly.
- Catching LazyInitializationException in the controller.

**Explanation**

The preferred response is to design the use-case query and map the response while required state is available. The exercise disables OSIV to reinforce that service boundary, but this is an explicit design decision rather than a universal configuration rule.

---

### Task 22 — Run the complete regression

**Objective**

Prove that the persistence implementation changed while the external contract remained stable.

**Concept**

A migration is complete only when happy paths, negative paths, query semantics, transaction behavior, SQL, and pool usage agree with the frozen baseline.

**What to implement**

Run every scenario in the manual section. Record status, headers, response shape, stored state, generated SQL, and whether the observed result matches the prerequisite baseline.

**Starter code**

~~~text
Scenario             Status  Shape/header        SQL/pool observation    PASS/FAIL
GET list             TODO    TODO                TODO                    TODO
GET one              TODO    TODO                TODO                    TODO
author + paging      TODO    TODO                TODO                    TODO
POST                 TODO    TODO                TODO                    TODO
duplicate POST       TODO    TODO                TODO                    TODO
PUT                  TODO    TODO                TODO                    TODO
duplicate PUT        TODO    TODO                TODO                    TODO
PATCH                TODO    TODO                TODO                    TODO
DELETE               TODO    TODO                TODO                    TODO
missing book         TODO    TODO                TODO                    TODO
validation failures TODO    TODO                TODO                    TODO
malformed JSON       TODO    TODO                TODO                    TODO
unknown DB failure   TODO    TODO                TODO                    TODO
~~~

**Hints**

1. Use fresh, short ISBNs that satisfy the unchanged DTO constraints.
2. Capture Location and generated IDs; never assume identity values.
3. Read state after rejected writes.
4. Compare SQL with the Task 18 predictions.
5. Distinguish current-checkout validation gaps from JPA regressions.

**How to verify**

Complete the manual matrix below using Postman, curl.exe, or PowerShell. Run mvn compile and inspect the final five-file diff. Confirm the application still uses PostgreSQL through HikariCP.

**Common mistakes**

- Testing only 2xx paths.
- Calling an unfinished validation scenario a JPA defect.
- Accepting a 409 without proving the row stayed unchanged.
- Returning Page metadata instead of an array.
- Leaving disposable lab code in the project.

**Explanation**

The final evidence should show the same API at the top and the same PostgreSQL/Hikari infrastructure at the bottom, with only the persistence implementation between service and JDBC replaced.

## Manual verification scenarios

Complete the earlier validation exercise first if you want every intended 400 assertion below to pass. The checked-in StockRequest and decimal rule are currently unfinished, and the actual database lacks nonnegative CHECK constraints; this JPA exercise intentionally does not hide or repair that fact.

### Environment and observation setup

From Validation/book-catalog-api:

~~~powershell
docker compose up -d
docker compose ps
mvn compile
mvn spring-boot:run
~~~

Keep the application in terminal A. Use terminal B for requests:

~~~powershell
$base = 'http://127.0.0.1:8080'
$tag = [DateTime]::UtcNow.ToString('HHmmssfff')
$isbnA = "JPA-A-$tag"
$isbnB = "JPA-B-$tag"
$missingId = 9223372036854775807
~~~

The generated ISBNs remain below the unchanged BookRequest maximum of 20. If the maximum long already exists, choose another confirmed absent positive ID.

In Postman, create the same requests with a baseUrl collection variable. Inspect status, response headers, ProblemDetail body, and the follow-up GET. The PowerShell examples below provide reproducible equivalents.

### Scenario 1 - Dependency and connection-pool trace

Run:

~~~powershell
mvn dependency:tree '-Dincludes=org.springframework.data:spring-data-jpa,org.hibernate.orm:hibernate-core,org.springframework:spring-jdbc,com.zaxxer:HikariCP,org.postgresql:postgresql'
~~~

Expected:

- Spring Data JPA, Hibernate ORM, Spring JDBC, HikariCP, and pgJDBC are present.
- No application code constructs a JDBC Connection.
- Startup identifies a Hikari pool.
- SQL execution still ends at PostgreSQL.

Record:

~~~text
JpaRepository -> EntityManager -> Hibernate -> JDBC -> DataSource -> HikariCP -> pgJDBC -> PostgreSQL
~~~

### Scenario 2 - POST preserves generated ID, Location, and body

~~~powershell
$createBodyA = @{
    isbn = $isbnA
    title = 'JPA in Practice'
    author = 'Ada Author'
    price = 39.90
    stock = 10
} | ConvertTo-Json

$createA = Invoke-WebRequest -UseBasicParsing -Method Post "$base/books" `
    -ContentType 'application/json' -Body $createBodyA

$createA.StatusCode
$createA.Headers.Location
$createA.Content
$bookA = $createA.Content | ConvertFrom-Json
$idA = [long] $bookA.id
~~~

Expected: 201, Location /books/{generated-id}, and a BookResponse. The SQL log contains an INSERT. A follow-up GET returns the same values.

### Scenario 3 - GET one and missing one

~~~powershell
Invoke-WebRequest -UseBasicParsing "$base/books/$idA"

try {
    Invoke-WebRequest -UseBasicParsing "$base/books/$missingId"
} catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}
~~~

Expected: existing is 200; confirmed absent positive ID is 404 with the unchanged ProblemDetail contract.

### Scenario 4 - Exact author filter, paging, order, and array shape

Create a second book:

~~~powershell
$createBodyB = @{
    isbn = $isbnB
    title = 'Another JPA Book'
    author = 'Ada Author'
    price = 20.00
    stock = 4
} | ConvertTo-Json

$createB = Invoke-WebRequest -UseBasicParsing -Method Post "$base/books" `
    -ContentType 'application/json' -Body $createBodyB
$idB = [long](($createB.Content | ConvertFrom-Json).id)

$all = Invoke-RestMethod "$base/books?page=0&size=20"
$filtered = Invoke-RestMethod "$base/books?author=Ada%20Author&page=0&size=20"
$caseDifferent = Invoke-RestMethod "$base/books?author=ada%20author&page=0&size=20"

$all.GetType().FullName
$filtered | Select-Object id,isbn,author
$caseDifferent
~~~

Expected: top-level arrays, exact author equality, ascending IDs, and no Page metadata. Repeat with size=1 across pages and confirm stable order.

### Scenario 5 - Duplicate POST is exactly the known 409

~~~powershell
try {
    Invoke-WebRequest -UseBasicParsing -Method Post "$base/books" `
        -ContentType 'application/json' -Body $createBodyA
} catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}
~~~

Expected: 409 through DuplicateIsbnException and the unchanged global advice. Inspect the log: INSERT is forced during saveAndFlush, not left beyond the service catch.

### Scenario 6 - PUT replacement and duplicate rollback

~~~powershell
$replaceA = @{
    isbn = $isbnA
    title = 'JPA Fully Replaced'
    author = 'Grace Writer'
    price = 44.50
    stock = 7
} | ConvertTo-Json

Invoke-WebRequest -UseBasicParsing -Method Put "$base/books/$idA" `
    -ContentType 'application/json' -Body $replaceA

$conflictingReplace = @{
    isbn = $isbnB
    title = 'Must Roll Back'
    author = 'Must Roll Back'
    price = 1.00
    stock = 1
} | ConvertTo-Json

try {
    Invoke-WebRequest -UseBasicParsing -Method Put "$base/books/$idA" `
        -ContentType 'application/json' -Body $conflictingReplace
} catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}

Invoke-RestMethod "$base/books/$idA"
~~~

Expected: the first PUT returns 200 with all replacement values. The second returns 409, and the follow-up GET proves the entire conflicting mutation rolled back.

### Scenario 7 - PATCH changes logical stock only

~~~powershell
$beforePatch = Invoke-RestMethod "$base/books/$idA"
$patchBody = @{ stock = 3 } | ConvertTo-Json
$patch = Invoke-RestMethod -Method Patch "$base/books/$idA" `
    -ContentType 'application/json' -Body $patchBody
$afterPatch = Invoke-RestMethod "$base/books/$idA"

$beforePatch
$patch
$afterPatch
~~~

Expected: 200; stock becomes 3; ISBN, title, author, and price values remain unchanged. Record the UPDATE SET list. Hibernate may assign more mapped columns than stock even though the Java/domain mutation changes only stock.

### Scenario 8 - DELETE preserves 204 and missing 404

~~~powershell
$deleted = Invoke-WebRequest -UseBasicParsing -Method Delete "$base/books/$idB"
$deleted.StatusCode
$deleted.Content.Length

try {
    Invoke-WebRequest -UseBasicParsing -Method Delete "$base/books/$idB"
} catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}
~~~

Expected: first DELETE is 204 with an empty body; the second is 404. The SQL shows the deliberate lookup before DELETE.

### Scenario 9 - Validation remains an HTTP boundary

After completing the validation prerequisite, submit representative invalid bodies and parameters:

~~~powershell
$blankTitle = @{
    isbn = "BAD-$tag"
    title = '   '
    author = 'Author'
    price = 10.00
    stock = 1
} | ConvertTo-Json

try {
    Invoke-WebRequest -UseBasicParsing -Method Post "$base/books" `
        -ContentType 'application/json' -Body $blankTitle
} catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}

try {
    Invoke-WebRequest -UseBasicParsing "$base/books/0"
} catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}
~~~

Expected after the prerequisite: 400 ProblemDetail with the existing validation shape, and no persistence SQL for a rejected request. Also rerun missing/null/negative stock, price boundaries/scale, overlong fields, invalid page/size, and blank optional author from the validation exercise.

If the checked-in incomplete StockRequest accepts missing or negative stock, record it as the known prerequisite gap, not a JPA success.

### Scenario 10 - Malformed JSON and conversion still differ from validation

~~~powershell
try {
    Invoke-WebRequest -UseBasicParsing -Method Post "$base/books" `
        -ContentType 'application/json' `
        -Body '{"isbn":"X","title":"X","author":"X","price":10,"stock":'
} catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}

try {
    Invoke-WebRequest -UseBasicParsing "$base/books/not-a-number"
} catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}
~~~

Expected: 400 through the existing MVC/global-handler paths, before the repository is invoked.

### Scenario 11 - Unknown integrity failure is not mislabeled

Do not damage shared data. In an isolated local database only, temporarily bypass normal validated input or temporarily introduce a safe-to-revert mismatch that produces a non-UNIQUE integrity error. Revert it immediately.

Expected: the service does not convert it to DuplicateIsbnException. The existing catch-all returns sanitized 500 behavior, and internal details remain server-side. If a safe isolated setup is unavailable, verify this path by code review rather than destructive mutation.

### Scenario 12 - SQL, transaction, and pool record

For each CRUD path record:

~~~text
Request | service transaction | repository call | SQL count/shape | flush point | status
TODO
~~~

Confirm:

- read transactions are read-only service units of work;
- write methods override the class default;
- dirty checking produces PUT/PATCH updates;
- flush is observable before commit for create and PUT;
- no endpoint returns a JPA entity;
- Hikari pool settings remain maximum 5/minimum 2;
- no bind-value logger exposes request values.

### Regression matrix

| Scenario | Required result | State check |
|---|---|---|
| GET /books | 200 array, ID ascending | No mutation |
| GET /books/{existing} | 200 BookResponse | No mutation |
| GET /books/{missing positive} | 404 ProblemDetail | No mutation |
| Exact author + paging | 200 array with stable window | No mutation |
| POST unique | 201 + Location + generated ID | Row inserted |
| POST duplicate ISBN | 409 | No second row |
| PUT existing | 200 full replacement | All writable values replaced |
| PUT duplicate ISBN | 409 | Whole transaction rolled back |
| PUT missing | 404 | No row inserted |
| PATCH existing | 200 | Logical stock only |
| PATCH missing | 404 | No mutation |
| DELETE existing | 204 empty body | Row removed |
| DELETE missing | 404 | No mutation |
| Invalid DTO/path/query | 400 after prerequisite | Repository not invoked |
| Malformed JSON/type conversion | 400 | Repository not invoked |
| Unexpected persistence failure | Sanitized 500 | Never mislabeled as ISBN |

Use Postman, curl.exe, or PowerShell. These scenarios can become automated integration tests in the later testing topic; do not introduce those tools here.

## Stop Before the Complete Solution

Do not continue until your five-file attempt compiles, PostgreSQL is healthy, and you have written answers to these questions from memory:

1. What is the difference among JPA, Hibernate ORM, and Spring Data JPA?
2. Where is the persistence context?
3. Why can UPDATE happen without repository.save()?
4. Why is flush not commit?
5. Does HikariCP still exist?
6. Why should PostgreSQL keep UNIQUE(isbn) when the entity declares unique metadata?
7. Why can save() fail only later at flush?
8. What makes a particular Book instance managed?
9. Why should BookResponse remain separate from Book entity?
10. What creates the Spring Data repository implementation?

Also predict the SQL for one successful PUT, one conflicting PUT, one PATCH, and one missing DELETE. If an answer is only "Spring does it," name the responsible component before proceeding.

<details>
<summary><strong>Open only after completing the tasks and predictions</strong></summary>

The complete reference begins immediately below. Compare it with your attempt; do not copy it without explaining each persistence transition.

</details>

## Complete Reference Solution

This reference assumes the earlier validation/global-exception exercise has been completed when its full intended 400 behavior is required. It does not repair the checked-in StockRequest, unfinished decimal rule, controller size typo, missing fixtures, or missing database CHECK/ISBN-nullability constraints.

Only the following five modified files are included:

| File | Responsibility change |
|---|---|
| pom.xml | Direct JDBC starter becomes Data JPA starter |
| model/Book.java | Record becomes schema-mapped entity |
| repository/BookRepository.java | Concrete JDBC implementation becomes Spring Data interface |
| service/BookService.java | CRUD becomes repository operations plus managed-state transactions |
| application.yaml | Adds safe Hibernate validation/boundary/logging settings |

Everything else remains unchanged and is intentionally not reproduced.

### pom.xml - MODIFY

The Boot parent manages Spring Data JPA, Hibernate ORM, Jakarta Persistence, Spring JDBC, and Hikari versions. The JPA starter brings JDBC infrastructure transitively, so the direct JDBC starter is replaced rather than duplicated.

~~~xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
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
  <!-- FIXME change it to the project's website -->
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
    <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
      <plugins>
        <!-- clean lifecycle, see https://maven.apache.org/ref/current/maven-core/lifecycles.html#clean_Lifecycle -->
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.1.0</version>
        </plugin>
        <!-- default lifecycle, jar packaging: see https://maven.apache.org/ref/current/maven-core/default-bindings.html#Plugin_bindings_for_jar_packaging -->
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
        <!-- site lifecycle, see https://maven.apache.org/ref/current/maven-core/lifecycles.html#site_Lifecycle -->
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
~~~

The existing explicit JUnit 4/plugin versions are not modernized here because testing/build cleanup is outside this persistence lesson.

### src/main/java/com/example/bookcatalog/model/Book.java - MODIFY

~~~java
package com.example.bookcatalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "isbn", length = 120, nullable = true, unique = true)
    private String isbn;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "author", length = 120, nullable = false)
    private String author;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    protected Book() {
    }

    public Book(
            String isbn,
            String title,
            String author,
            BigDecimal price,
            Integer stock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public Long id() {
        return id;
    }

    public String isbn() {
        return isbn;
    }

    public String title() {
        return title;
    }

    public String author() {
        return author;
    }

    public BigDecimal price() {
        return price;
    }

    public Integer stock() {
        return stock;
    }

    public void replaceWith(
            String isbn,
            String title,
            String author,
            BigDecimal price,
            Integer stock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public void changeStock(Integer stock) {
        this.stock = stock;
    }
}
~~~

Why these details matter:

- Long can be null while the instance is transient; long cannot.
- IDENTITY matches the actual PostgreSQL identity declaration.
- Field placement selects field access, so Hibernate does not require JavaBean setters.
- The protected constructor is for persistence materialization.
- The class and mutation methods are non-final and narrow.
- Record-style accessors keep the unchanged BookResponse.from(Book) and controller compiling.
- No equality/hashCode recipe is introduced casually.
- Mapping length 120 and nullable ISBN reflects schema.sql, even though the request DTO currently accepts a narrower/nonblank value.
- There is no invented nonnegative stock constraint.

The annotations document mapping intent and participate in Hibernate's limited compatibility validation. They do not prove every constraint detail and do not replace the live table or its constraints.

### src/main/java/com/example/bookcatalog/repository/BookRepository.java - MODIFY

~~~java
package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.Book;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findAllBy(Pageable pageable);

    List<Book> findByAuthor(String author, Pageable pageable);
}
~~~

There is no @Repository annotation and no implementation class. Spring Data discovers this interface, creates the proxy, and delegates through an EntityManager.

findByAuthor is exact equality. Sorting is supplied by Pageable. List avoids a count query contract and keeps the controller's top-level array unchanged.

### src/main/java/com/example/bookcatalog/service/BookService.java - MODIFY

~~~java
package com.example.bookcatalog.service;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private static final String ISBN_UNIQUE_CONSTRAINT = "books_isbn_key";

    private final BookRepository repo;

    public BookService(BookRepository repo) {
        this.repo = repo;
    }

    public Book findById(long id) {
        return required(id);
    }

    public List<Book> findAll(String author, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id"));

        return author == null
                ? repo.findAllBy(pageable)
                : repo.findByAuthor(author, pageable);
    }

    @Transactional
    public Book create(
            String isbn,
            String title,
            String author,
            BigDecimal price,
            Integer stock) {
        Book book = new Book(isbn, title, author, price, stock);

        try {
            return repo.saveAndFlush(book);
        } catch (DataIntegrityViolationException failure) {
            throw translateKnownIsbnConflict(failure);
        }
    }

    @Transactional
    public Book replace(
            long id,
            String isbn,
            String title,
            String author,
            BigDecimal price,
            Integer stock) {
        Book book = required(id);
        book.replaceWith(isbn, title, author, price, stock);

        try {
            repo.flush();
            return book;
        } catch (DataIntegrityViolationException failure) {
            throw translateKnownIsbnConflict(failure);
        }
    }

    @Transactional
    public Book setStock(long id, Integer stock) {
        Book book = required(id);
        book.changeStock(stock);
        return book;
    }

    @Transactional
    public void delete(long id) {
        Book book = required(id);
        repo.delete(book);
    }

    private Book required(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private static RuntimeException translateKnownIsbnConflict(
            DataIntegrityViolationException failure) {
        ConstraintViolationException constraint =
                findHibernateConstraintViolation(failure);

        if (constraint == null
                || constraint.getKind()
                        != ConstraintViolationException.ConstraintKind.UNIQUE) {
            return failure;
        }

        String constraintName = constraint.getConstraintName();
        if (constraintName == null
                || ISBN_UNIQUE_CONSTRAINT.equalsIgnoreCase(constraintName)) {
            return new DuplicateIsbnException(failure);
        }

        return failure;
    }

    private static ConstraintViolationException
            findHibernateConstraintViolation(Throwable failure) {
        Throwable current = failure;

        while (current != null) {
            if (current instanceof ConstraintViolationException constraint) {
                return constraint;
            }

            Throwable next = current.getCause();
            if (next == current) {
                break;
            }
            current = next;
        }

        return null;
    }
}
~~~

Transaction reasoning:

| Method | Boundary | Persistence behavior |
|---|---|---|
| findById/findAll | Class-level read-only transaction | Load and return basic entity state |
| create | Method-level write transaction | Persist new entity; saveAndFlush makes constraint timing deliberate |
| replace | Method-level write transaction | Load managed entity, mutate every writable field, flush for conflict translation |
| setStock | Method-level write transaction | Load, mutate stock, rely on transaction-completion dirty checking |
| delete | Method-level write transaction | Load to preserve 404, then remove |

Important exception details:

- DataIntegrityViolationException is Spring's translated persistence exception.
- ConstraintViolationException in this file is org.hibernate.exception.ConstraintViolationException.
- It is not jakarta.validation.ConstraintViolationException.
- Hibernate ORM 7's getKind() must report UNIQUE.
- If getConstraintName() is non-null, it must match books_isbn_key.
- getConstraintName() may legally be null. The fallback is deliberately confined to create and replace, the two exact operations where ISBN is the only client-controlled unique value.
- A non-unique violation, an unknown Hibernate cause, or a different reported constraint name is rethrown. The unchanged global handler sanitizes it as 500 instead of lying that it is an ISBN conflict.

If the constraint query in Task 10 reports a different real name for an intentionally different local schema, change the constant to that verified name. Do not broaden the condition or parse an exception message.

The explicit create/replace flushes are not commits. They synchronize SQL while the service try/catch is active; the transaction interceptor commits afterward. PATCH and DELETE do not flush merely for ceremony.

### src/main/resources/application.yaml - MODIFY

~~~yaml
spring:
  application:
    name: book-catalog-api
  datasource:
    url: jdbc:postgresql://127.0.0.1:5433/book_catalog
    username: book_app
    password: replace-with-a-local-practice-password
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
  sql:
    init:
      mode: always
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
  jackson:
    deserialization:
      fail-on-unknown-properties: true
      accept-float-as-int: false
server:
  address: 127.0.0.1
  port: 8080
logging:
  level:
    org.hibernate.SQL: DEBUG
~~~

Configuration ownership:

- The datasource URL, local credential approach, and Hikari 5/2 settings are unchanged.
- spring.sql.init.mode remains always; schema.sql still owns table creation.
- ddl-auto validate fails on incompatibilities it checks, such as missing mapped structures or incompatible types, rather than mutating the schema; it does not prove every constraint detail.
- open-in-view false makes the service boundary explicit. Boot's usual servlet JPA default is enabled when the property is absent, so this change is deliberate.
- format_sql plus org.hibernate.SQL DEBUG is development inspection. Bind logging is intentionally absent.

### Compile and run

From Validation/book-catalog-api:

~~~powershell
mvn compile
docker compose up -d
mvn spring-boot:run
~~~

Then run the complete manual matrix. A successful compile is necessary but not sufficient: verify generated SQL, the 409 flush path, rollback after conflicting PUT, list shape, missing DELETE, and Hikari presence.

## Before vs After architecture

### Before

~~~text
HTTP request
    |
Jackson + Validation
    |
BookController
    |
BookService
    |
BookRepository class
    |
JdbcTemplate
    |
RowMapper + handwritten SQL
    |
Spring JDBC
    |
DataSource
    |
HikariCP
    |
pgJDBC
    |
PostgreSQL
~~~

The application repository decides SQL text, parameter order, LIMIT/OFFSET arithmetic, INSERT ... RETURNING, update statements, delete row-count handling, and row mapping.

### After

~~~text
HTTP request
    |
Jackson + Validation
    |
BookController
    |
BookService
    |  @Transactional
    |
BookRepository interface
    |
Spring Data repository proxy
    |
EntityManager
    |
Persistence Context
    |
Hibernate ORM
    |
generated SQL
    |
JDBC
    |
DataSource
    |
HikariCP
    |
pgJDBC
    |
PostgreSQL
~~~

The application declares entity mapping, repository intent, transaction boundaries, domain mutation, and the narrow error translation. Spring Data builds the repository proxy. The EntityManager owns JPA operations and the persistence context. Hibernate maps managed state to SQL. JDBC and pgJDBC still communicate with PostgreSQL, and HikariCP still supplies pooled connections.

### What did not change

| Stable concern | Preserved behavior |
|---|---|
| REST API contract | Same URLs, methods, request DTOs, response DTO, and top-level array list |
| Success behavior | GET 200, POST 201 plus Location, PUT/PATCH 200, DELETE 204 empty |
| Failure distinctions | Intended validation 400, missing book 404, known duplicate ISBN 409, unknown failure sanitized 500 |
| Validation | BookRequest and StockRequest remain HTTP-boundary DTOs; their checked-in unfinished work is still a prerequisite |
| Global exception handling | GlobalExceptionHandler, ProblemDetail, framework status/header preservation, and application exception mappings |
| PostgreSQL | Same PostgreSQL database and Compose service; no H2 |
| HikariCP | Same Boot-managed pool and maximum 5/minimum 2 configuration |
| Database constraints | The actual checked-in constraints remain; no nonexistent CHECK or ISBN NOT NULL constraint is claimed |
| Schema ownership | src/main/resources/schema.sql plus Spring SQL initialization remain responsible |
| Jackson | Existing strict deserialization settings remain |

### Responsibility boundary summary

| Responsibility | Before owner | After owner |
|---|---|---|
| HTTP routing/status/Location | BookController | BookController, unchanged |
| Input validation | Request DTOs + Spring MVC | Request DTOs + Spring MVC, unchanged |
| Application absence/conflict meaning | BookService | BookService, unchanged in meaning |
| Unit of work | Mostly individual JdbcTemplate calls | Transactional BookService method |
| Query declaration | SQL strings in BookRepository class | Repository method names/Pageable |
| Repository implementation | Application class | Spring Data proxy |
| Row/object mapping | RowMapper | Entity metadata + Hibernate |
| Identity map | Not supplied by JdbcTemplate | Persistence context |
| Change detection | Explicit UPDATE method | Hibernate dirty checking |
| SQL generation | Application | Hibernate |
| SQL execution | Spring JDBC/JDBC | Hibernate through JDBC |
| Connection pooling | HikariCP | HikariCP, unchanged |
| Stored integrity | PostgreSQL | PostgreSQL, unchanged |
| Error representation | GlobalExceptionHandler | GlobalExceptionHandler, unchanged |

### Successful request lifecycle

~~~text
POST /books
    |
Jackson creates BookRequest
    |
Validation accepts request
    |
BookController calls BookService
    |
transaction begins
    |
new Book is transient, id == null
    |
repository proxy -> EntityManager persist
    |
Book becomes managed
    |
saveAndFlush
    |
Hibernate INSERT -> JDBC -> Hikari Connection -> PostgreSQL
    |
PostgreSQL generates identity
    |
managed Book receives ID
    |
transaction commits
    |
BookController builds Location and BookResponse
    |
201 Created
~~~

IDENTITY commonly requires the INSERT before the generated ID is available. The explicit flush also places integrity failure inside the service catch. Neither observation means flush equals commit.

### Managed-update lifecycle

~~~text
PUT /books/{id}
    |
transaction begins
    |
findById -> SELECT
    |
Book is managed and snapshotted
    |
replaceWith mutates all writable state
    |
no repository.save call
    |
explicit flush
    |
dirty checking -> UPDATE
    |
transaction commits
    |
200 BookResponse
~~~

For PATCH, changeStock is the only logical mutation. Hibernate's default UPDATE may still assign additional mapped columns. The API values of those fields remain unchanged.

### Failure lifecycle comparison

Before migration, the known conflict travels through Spring JDBC:

~~~text
PostgreSQL UNIQUE(isbn)
    |
pgJDBC SQLException
    |
Spring JDBC exception translation
    |
DuplicateKeyException
    |
BookService
    |
DuplicateIsbnException
    |
GlobalExceptionHandler
    |
409 ProblemDetail
~~~

After migration, the known conflict travels through Hibernate and Spring's persistence translation:

~~~text
PostgreSQL UNIQUE(isbn)
    |
pgJDBC SQLException
    |
Hibernate ORM ConstraintViolationException
    |  kind == UNIQUE
    |  name == books_isbn_key when available
    |
Spring DataIntegrityViolationException
    |
BookService narrow classifier during flush
    |
DuplicateIsbnException
    |
unchanged GlobalExceptionHandler
    |
409 ProblemDetail
~~~

The similarly named Jakarta validation exception is on a different path:

~~~text
invalid HTTP value
    |
Jakarta Validation / Spring MVC
    |
MethodArgumentNotValidException
or HandlerMethodValidationException
    |
unchanged GlobalExceptionHandler
    |
400 ProblemDetail
~~~

It is not org.hibernate.exception.ConstraintViolationException and must not be caught by the service's persistence classifier.

Unknown database failures retain server-error meaning:

~~~text
DataIntegrityViolationException
    |
not Hibernate UNIQUE
or known non-null constraint name does not match
    |
BookService rethrows original failure
    |
unchanged sanitized catch-all
    |
500 ProblemDetail
~~~

### Flush and transaction timeline

| Operation | Deliberate synchronization | Why | Commit |
|---|---|---|---|
| Create | saveAndFlush | Obtain identity and catch the known unique failure in service | After method succeeds |
| PUT | repository.flush after managed mutation | Catch a conflicting new ISBN in service | After method succeeds |
| PATCH | Automatic flush by transaction completion | No client-controlled unique value changes | Transaction interceptor |
| DELETE | Automatic flush by transaction completion | No special client-facing integrity translation | Transaction interceptor |

If create or PUT fails during flush, the runtime exception leaves the transactional method and the transaction rolls back. A flush sends pending SQL but still can be rolled back; only successful commit makes the transaction durable.

## Final checklist

### Project scope

- [ ] I evolved Validation/book-catalog-api rather than creating another application.
- [ ] Spring Boot remains 4.1.1 and Java remains 21.
- [ ] Exactly five project files differ in the reference design.
- [ ] I did not add Spring Security, deployment, a Java Docker image, H2, Lombok, or advanced DDD layers.
- [ ] I did not migrate JUnit, add MockMvc, Testcontainers, @DataJpaTest, or other automated Spring testing.
- [ ] I did not change Controller, DTO, application-exception, global-advice, schema, Compose, fixture, or test files.
- [ ] Task 14 debugger code and Tasks 16/19/20/21 disposable material are absent from the final project.
- [ ] I did not expose values from .env.

### Baseline honesty

- [ ] I can identify the checked-in StockRequest, decimal-rule, and paging-validation gaps as prerequisite validation work.
- [ ] I do not claim the current schema has ISBN NOT NULL, ISBN length 20, or nonnegative CHECK constraints.
- [ ] I map the actual ISBN VARCHAR(120) UNIQUE nullable column.
- [ ] I understand that CREATE TABLE IF NOT EXISTS does not migrate an existing volume.
- [ ] I understand that ddl-auto=validate diagnoses some mapping/schema incompatibilities, does not comprehensively verify every constraint detail, and does not repair anything.

### Dependency and configuration

- [ ] spring-boot-starter-jdbc was replaced by spring-boot-starter-data-jpa.
- [ ] Boot manages the Spring Data JPA, Hibernate ORM, and Jakarta Persistence versions.
- [ ] PostgreSQL, pgJDBC, Spring JDBC infrastructure, DataSource, and HikariCP remain.
- [ ] application.yaml retains SQL initialization, datasource settings, Hikari 5/2 settings, Jackson settings, and server settings.
- [ ] Hibernate uses ddl-auto validate, not update/create/create-drop.
- [ ] open-in-view is explicitly false as a teaching/service-boundary choice.
- [ ] Development SQL is visible without enabling unsafe bind-value logging.

### Entity and repository

- [ ] Book is a non-final @Entity mapped to @Table(name = "books").
- [ ] Book uses nullable Long identity with @Id and @GeneratedValue(IDENTITY).
- [ ] @Column metadata matches every actual checked-in column.
- [ ] Book has a protected no-argument constructor and a creation constructor without ID.
- [ ] Book retains id()/isbn()/title()/author()/price()/stock() read accessors.
- [ ] Book exposes only replaceWith and changeStock as focused mutations.
- [ ] I did not move request validation wholesale onto the entity.
- [ ] BookRepository is a JpaRepository<Book, Long> interface.
- [ ] JdbcTemplate, RowMapper, and manual CRUD SQL are gone from BookRepository.
- [ ] findAllBy(Pageable) and findByAuthor(String, Pageable) return List<Book>.
- [ ] I understand Spring Data creates the repository implementation proxy.

### Persistence behavior

- [ ] The service class has a read-only transaction default.
- [ ] Create, replace, stock PATCH, and delete have write transactions.
- [ ] A new Book starts transient with a null ID.
- [ ] saveAndFlush persists creation and exposes the database result deliberately.
- [ ] PUT loads a managed entity and relies on dirty checking rather than a redundant save.
- [ ] PUT mutates all writable values and flushes inside the conflict catch.
- [ ] PATCH calls only changeStock; I do not promise a one-column generated UPDATE.
- [ ] DELETE finds first so a missing ID remains 404.
- [ ] I know flush synchronizes SQL but does not commit.
- [ ] I know transaction completion may flush pending work.
- [ ] I know the persistence context is scoped, not a global cache.
- [ ] I know the same identity in one persistence context normally means the same managed Java instance.
- [ ] I did not add @Version; optimistic locking/lost-update policy remains future knowledge.

### Query and API compatibility

- [ ] Null author selects the unfiltered repository method.
- [ ] Non-null author uses exact equality, not contains or ignore-case.
- [ ] PageRequest uses zero-based page, size, and Sort by entity attribute id ascending.
- [ ] The controller still returns a JSON array, not Page metadata.
- [ ] POST still returns 201, Location, and BookResponse.
- [ ] PUT and PATCH still return 200 BookResponse.
- [ ] Existing DELETE still returns 204 with no body.
- [ ] Missing positive IDs still become BookNotFoundException and 404.
- [ ] Request DTOs and BookResponse remain separate from the JPA entity.
- [ ] I inspected generated SQL rather than assuming one repository call equals one statement.

### Integrity and exception translation

- [ ] PostgreSQL UNIQUE(isbn) remains the stored-integrity authority.
- [ ] Create uses saveAndFlush so duplicate SQL executes inside the service catch.
- [ ] A duplicate-changing PUT calls flush inside the service catch.
- [ ] The service catches Spring DataIntegrityViolationException, not Jakarta Validation ConstraintViolationException.
- [ ] The nested Hibernate ORM exception must report ConstraintKind.UNIQUE.
- [ ] A reported constraint name must match books_isbn_key.
- [ ] I understand getConstraintName() may be null.
- [ ] I can justify why the null-name fallback is restricted to these exact ISBN-changing operations.
- [ ] Unknown integrity failures are rethrown and remain sanitized 500 errors.
- [ ] A conflicting PUT rolls back every mutation.
- [ ] GlobalExceptionHandler and the ProblemDetail contract remain unchanged.

### Relationship and performance labs

- [ ] The production schema/API has no forced Book/Author relationship.
- [ ] In the disposable model, Book.author owns author_id and Author.books uses mappedBy = "author".
- [ ] I do not assume mutating only the inverse side updates the FK.
- [ ] I distinguish cascade remove, orphanRemoval, and database ON DELETE CASCADE.
- [ ] I do not use CascadeType.ALL or bidirectional relationships automatically.
- [ ] I understand LAZY versus EAGER and do not make everything EAGER.
- [ ] I can calculate 1 + N, including the 1 + 100 = 101 example.
- [ ] I can compare JOIN FETCH, EntityGraph, and projections as use-case fetch choices.
- [ ] I understand why a to-many fetch join can complicate pagination.
- [ ] I can explain LazyInitializationException from persistence-context lifetime.

### I can explain from memory

- [ ] ORM
- [ ] JPA/Jakarta Persistence
- [ ] Hibernate ORM
- [ ] Spring Data JPA
- [ ] @Entity
- [ ] @Table
- [ ] @Id
- [ ] @GeneratedValue
- [ ] @Column
- [ ] EntityManager
- [ ] persistence context
- [ ] first-level cache
- [ ] transient/managed/detached/removed
- [ ] persist vs merge
- [ ] save
- [ ] dirty checking
- [ ] flush vs commit
- [ ] JpaRepository
- [ ] Spring Data repository proxy
- [ ] derived query methods
- [ ] JPQL
- [ ] native queries
- [ ] Pageable/Page/Slice
- [ ] @OneToOne
- [ ] @OneToMany
- [ ] @ManyToOne
- [ ] @ManyToMany
- [ ] owning side
- [ ] mappedBy
- [ ] cascade
- [ ] orphanRemoval
- [ ] LAZY/EAGER
- [ ] LazyInitializationException
- [ ] N+1
- [ ] fetch join
- [ ] EntityGraph
- [ ] DTO vs Entity
- [ ] basic @Transactional relationship to JPA
- [ ] JPA-generated SQL
- [ ] persistence exceptions
- [ ] why JDBC still exists
- [ ] why HikariCP still exists
- [ ] why PostgreSQL constraints still exist

### Ready for the next topic

- [ ] I can draw the complete stack without omitting JDBC, HikariCP, or PostgreSQL.
- [ ] I can identify which component owns each important behavior.
- [ ] I can predict SQL and flush timing before running an endpoint.
- [ ] I can explain the transaction boundary needed for managed dirty checking.
- [ ] I have deliberately deferred propagation, isolation, rollback customization, synchronization, and concurrency policy to the deeper Spring Transactions topic.
