# Clean Architecture Mini Project

## `book-catalog-api` — Incremental Clean Architecture Refactoring

This exercise refactors **one vertical slice** of the existing project:

`D:\DSA-new\DSA\Spring_Security\book-catalog-api`

It does **not** create another application and it does **not** convert every package to Clean Architecture. The chosen slice is **Create Book** (`POST /books`), because it is small enough to understand end to end while still crossing HTTP, validation, security, transactions, persistence, Hibernate, HikariCP, and PostgreSQL.

The companion theory guide is [`Clean_Architecture_Essential_Concepts.md`](./Clean_Architecture_Essential_Concepts.md).

| Study item | This exercise |
|---|---|
| **Estimated time** | 8–12 focused hours, including manual HTTP/database verification |
| **Actual stack** | Java 21, Spring Boot 4.1.1, Spring Framework 7.0.9, Spring Data JPA 4.1.1, Hibernate 7.4.5.Final, Spring Security 7.1.1, Flyway 12.4.0, HikariCP 7.0.2, PostgreSQL 17 |
| **Learning focus** | Dependency Rule, use cases, input/output ports, adapters, transaction ownership, validation/security boundaries, pragmatic JPA coupling, unit vs integration testing |
| **Refactoring scope** | Create Book only; every other vertical slice remains recognizable |
| **Tools** | IDE, Maven, Docker-compatible runtime, curl/Postman, pgAdmin or psql, logs/debugger |

## Quick navigation

- [Learning outcome](#learning-outcome)
- [Learning contract](#learning-contract)
- [Current and target architecture](#current-and-target-architecture)
- [Real project baseline](#real-baseline)
- [Scope guardrails](#scope-guardrails)
- [What remains unchanged](#what-remains-unchanged)
- [Target packages](#final-target-packages)
- [Practice priorities](#practice-priorities)
- [Progressive tasks](#tasks)
- [Pre-solution checkpoint](#pre-solution-architecture-checkpoint)
- [Manual verification plan](#manual-verification-plan-before-the-solution)
- [Complete reference solution](#complete-reference-solution)
- [Troubleshooting](#23-troubleshooting-guide)
- [Completion checklist](#26-completion-checklist)
- [Final mental model](#28-final-mental-model)

> **Inspection note**
>
> This exercise is based on the actual source tree inspected on 2026-10-13. The database container was not assumed to be running, so the baseline HTTP outcomes below are derived from the source, configuration, and schema rather than claimed as a live run. You will verify them yourself.

## Learning outcome

By the end, you will be able to explain and implement this dependency direction:

~~~text
outside / framework-specific                         inside / policy

HTTP request
   |
   v
BookController  --->  CreateBookUseCase  <---  CreateBookService
 inbound adapter          input port              application policy
                                                    |
                                                    v
                                            BookRepositoryPort
                                                output port
                                                    ^
                                                    |
BookRepository (Spring Data) <--- BookPersistenceAdapter
       framework repository          outbound adapter
               |
               v
      Hibernate -> JDBC -> HikariCP -> PostgreSQL
~~~

The application layer owns the interfaces that describe what it needs. Spring still assembles the objects and supplies cross-cutting infrastructure, but Spring Data is no longer the API used by the Create Book use case.

## Learning contract

For every task, use the same rhythm:

```text
read Objective
  → understand Concept
  → predict dependency direction
  → attempt Starter code
  → use Hints only if needed
  → compile/test or inspect behavior
  → review Common mistakes
  → read Explanation
```

Starter code intentionally contains `TODO` markers. Do not copy the complete reference first. Write the arrow and ownership answer before writing an interface: an abstraction without a reason is only ceremony.

## Current and target architecture

Current Create Book dependency and runtime path:

```text
HTTP
  ↓
SecurityFilterChain
  ↓
BookController
  ↓
BookService
  ↓
BookRepository extends JpaRepository
  ↓
Hibernate → JDBC → HikariCP → PostgreSQL
```

Target for **Create Book only**:

```text
HTTP
  ↓
SecurityFilterChain
  ↓
BookController                       inbound adapter
  ↓
CreateBookUseCase                    input port
  ↑ implemented by
CreateBookService                    application service
  ↓
BookRepositoryPort                   output port
  ↑ implemented by
BookPersistenceAdapter               outbound adapter
  ↓
existing BookRepository
  ↓
Hibernate → JDBC → HikariCP → PostgreSQL
```

Runtime calls move downward through the diagram. Compile-time dependencies at the adapter boundaries point inward: the controller and persistence adapter import application-owned ports; the application never imports either concrete adapter.

## Real baseline

### Version note

The following are the resolved project versions, not generic tutorial assumptions:

| Item | Inspected value |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Framework | 7.0.9 |
| Spring Data JPA | 4.1.1 |
| Hibernate ORM | 7.4.5.Final |
| Spring Security | 7.1.1 |
| Flyway | 12.4.0 |
| PostgreSQL driver | 42.7.13 |
| HikariCP | 7.0.2 |
| PostgreSQL image | 17 |
| Persistence | Spring Data JPA + Hibernate |
| Web security | session login + CSRF; HTTP Basic disabled |
| JPA setting | `spring.jpa.open-in-view=false` |
| Schema setting | Flyway enabled; `ddl-auto=validate` |
| Hikari pool | maximum 5, minimum idle 2 |
| Current source tests | JUnit 4.11 `AppTest` only |
| Target test stack | JUnit Jupiter via Boot 4.1.1 test starters + Testcontainers 2.0.5 |

### Current Create Book path

~~~text
POST /books
  -> SecurityFilterChain: authenticated ADMIN + valid CSRF token
  -> BookController.create(BookRequest)
  -> BookService.create(five scalar arguments)
  -> BookRepository.save(...)
  -> BookRepository.flush()
  -> Hibernate
  -> JDBC Connection borrowed through HikariCP
  -> PostgreSQL INSERT
  -> commit
  -> 201 Created + Location header + BookResponse
~~~

### Important baseline defect to repair first

The configured location is `classpath:db/migration`, but the inspected source tree has **no** `src/main/resources/db/migration` directory. Six migrations exist only under `target/classes/db/migration`.

`target/` is generated and disposable. A clean build deletes it. Therefore:

~~~text
works because stale target/classes happens to contain migrations
                              !=
source-controlled, reproducible project
~~~

You will restore the exact V1–V6 migration history to source before refactoring. This is a baseline integrity repair, not a Clean Architecture layer.

## Scope guardrails

You will:

- refactor only Create Book;
- keep the existing `Book` JPA entity as the application/domain model for now;
- introduce one command, one input port, one output port, one use-case service, and one persistence adapter;
- preserve URLs, JSON, validation, security, transactions, and status codes;
- add one pure unit test and one real-PostgreSQL persistence adapter test;
- restore source-owned migrations;
- modernize the stale JUnit 4 test setup to the Boot 4.1.1 test stack.

You will not:

- create a new Spring Boot project;
- add a second Book entity or mapping library;
- refactor read/update/delete/stock/authentication/transaction-lab slices;
- add Kafka, messaging, microservices, H2, or a database migration tool other than the existing Flyway;
- move every file merely to produce visually “clean” folders;
- alter the REST contract.

## What remains unchanged

This is a behavior-preserving architectural refactor. The schema and Flyway history are restored to their intended source location, but not redesigned.

| Scenario | Expected behavior |
|---|---|
| Public `GET /books` or `GET /books/{id}` | allowed |
| Anonymous unsafe request without CSRF | `403 Forbidden` at the security filter chain |
| Authenticated USER with valid CSRF attempts Book write | `403 Forbidden` |
| Authenticated ADMIN with valid CSRF creates valid unique book | `201 Created`, `Location: /books/{id}` |
| Invalid `BookRequest` | `400 Bad Request` with existing Problem Detail shape |
| Duplicate ISBN | `409 Conflict` |
| Existing deliberate `DELETE /books/{id}` experiment | `500` and deletion rolls back |
| Other endpoints | unchanged |

## Final target packages

~~~text
com.example.bookcatalog
├── book
│   ├── application
│   │   ├── port
│   │   │   ├── in
│   │   │   │   ├── CreateBookCommand.java
│   │   │   │   └── CreateBookUseCase.java
│   │   │   └── out
│   │   │       └── BookRepositoryPort.java
│   │   └── service
│   │       └── CreateBookService.java
│   └── adapter
│       └── out
│           └── persistence
│               └── BookPersistenceAdapter.java
├── dto                         # existing HTTP DTOs
├── model                       # existing JPA models, including Book
├── repository                  # existing Spring Data repositories
├── security                    # unchanged
├── service                     # existing services; BookService keeps non-create use cases
└── web                         # controllers and exception translation
~~~

This is intentionally transitional. A realistic codebase can move one slice at a time.

## Practice priorities

| Practice area | Priority |
|---|---|
| Dependency Rule | ⭐⭐⭐⭐⭐ MUST KNOW |
| Dependency Inversion | ⭐⭐⭐⭐⭐ MUST KNOW |
| Input/output ports | ⭐⭐⭐⭐⭐ MUST KNOW |
| Use-case boundary | ⭐⭐⭐⭐⭐ MUST KNOW |
| Persistence adapter | ⭐⭐⭐⭐⭐ MUST KNOW |
| Spring DI and proxy wiring | ⭐⭐⭐⭐ IMPORTANT |
| Transaction placement | ⭐⭐⭐⭐ IMPORTANT |
| Security and validation boundaries | ⭐⭐⭐⭐ IMPORTANT |
| Domain model versus JPA model | ⭐⭐⭐⭐ IMPORTANT |
| Testing through ports and real adapters | ⭐⭐⭐⭐ IMPORTANT |
| Strict versus pragmatic Clean Architecture | ⭐⭐⭐ NICE TO KNOW |

---

<a id="tasks"></a>

### Task 1 — Repair the reproducible database baseline

**Objective:**

Make Flyway migration history source-controlled before evaluating architecture.

**Concept:**

Source files are inputs; `target/` is output. Architecture work is not reproducible when a clean build removes the schema definition.

**What to implement:**

Create `src/main/resources/db/migration` and restore **exactly** V1 through V6 from `target/classes/db/migration`. Do not renumber, rename, or rewrite migrations already recorded in PostgreSQL.

**Starter code:**

~~~text
src/main/resources/
└── db/
    └── migration/
        ├── V1__create_books.sql
        ├── V2__add_optimistic_lock_version.sql
        ├── V3__create_transaction_audits.sql
        ├── V4__add_books_author_index.sql
        ├── V5__replace_author_index_for_paging.sql
        └── V6__create_app_users.sql
~~~

**Hints:**

- Compare hashes or file contents after copying.
- `mvn clean` deliberately removes `target`.
- Never “fix” an applied migration in place; add a new migration for a future schema change.

**How to verify:**

Run:

~~~powershell
mvn clean test
Get-ChildItem src/main/resources/db/migration
~~~

Even if tests need later dependency work, confirm that `mvn clean` no longer destroys the only migration copies.

**Common mistakes:**

- Treating `target/classes` as source.
- Combining V1–V6 into one new V1.
- Editing checksums in an already-used database.
- Enabling Hibernate schema generation to hide missing migrations.

**Explanation:**

Flyway owns schema evolution; Hibernate `ddl-auto=validate` checks mapping/schema agreement. Neither Clean Architecture nor JPA can compensate for absent source migrations.

---

### Task 2 — Trace the current Create Book slice

**Objective:**

Draw what actually runs before changing packages.

**Concept:**

Clean Architecture starts with responsibilities and dependency arrows, not folders.

**What to implement:**

On paper or in your notes, trace:

~~~text
SecurityFilterChain
 -> BookController
 -> BookService
 -> BookRepository proxy
 -> EntityManager/persistence context
 -> Hibernate
 -> JDBC Connection
 -> HikariCP
 -> PostgreSQL
~~~

For every arrow, label whether it is a compile-time dependency, a runtime call, or both.

**Starter code:**

~~~text
Component                Responsibility                         Depends on
-----------------------  -------------------------------------  ----------------
BookController           HTTP translation                       ?
BookService              use-case orchestration/transaction     ?
BookRepository           Spring Data persistence API            ?
Hibernate                object-relational work                 ?
HikariCP                 JDBC Connection lifecycle              ?
PostgreSQL               constraints/durable commit             ?
~~~

**Hints:**

- The runtime call direction and source-code dependency direction need not be identical.
- A Spring Data repository instance is a runtime-generated proxy.
- The controller currently knows `BookService`; `BookService` knows Spring Data `BookRepository`.

**How to verify:**

You should be able to answer: “If Spring Data JPA disappeared, which Create Book source file would fail to compile first?”

**Common mistakes:**

- Drawing only package boxes.
- Omitting security because it runs before the controller.
- Saying HikariCP starts transactions; the transaction manager coordinates them using a JDBC connection.
- Calling a runtime Spring proxy a source file.

**Explanation:**

The current design is a conventional layered design. It works, but its use-case class imports the chosen persistence API. The exercise reverses only that one dependency.

> **Pause point 1 — draw before revealing**
>
> Which direction should the source dependency between application policy and persistence implementation point?

<details>
<summary>Reveal the checkpoint</summary>

The application owns an output-port interface. The persistence adapter implements it. Therefore both the use case and adapter depend toward the application contract; the inner application package does not import Spring Data.

</details>

---

### Task 3 — Freeze the externally visible contract

**Objective:**

Define what must remain unchanged while internals move.

**Concept:**

A refactor changes structure without intentionally changing observable behavior.

**What to implement:**

Write a short characterization checklist for Create Book:

- request URL/method/content type;
- request and response fields;
- Bean Validation behavior;
- ADMIN and CSRF requirements;
- `201` and `Location` behavior;
- duplicate ISBN `409` behavior;
- transaction boundary.

**Starter code:**

~~~text
[ ] POST /books
[ ] consumes/produces application/json
[ ] same BookRequest validation
[ ] requires authenticated ADMIN and CSRF
[ ] returns 201 + Location + BookResponse
[ ] duplicate ISBN becomes DuplicateIsbnException -> 409
[ ] persistence participates in one application transaction
~~~

**Hints:**

Read `BookController`, `BookRequest`, `SecurityConfig`, `BookService`, and `GlobalExceptionHandler`. A package refactor must not accidentally weaken security.

**How to verify:**

Compare your list with the “Behavior contract to preserve” table above. Add anything you missed.

**Common mistakes:**

- Assuming `permitAll()` disables CSRF for unsafe methods.
- moving validation into persistence;
- changing response DTOs while claiming a pure refactor;
- adding method security that changes behavior for non-HTTP callers.

**Explanation:**

The existing URL authorization remains in `SecurityFilterChain`. Create Book does not require the current username in business policy, so a `CurrentUserPort` would be unnecessary here.

---

### Task 4 — Classify responsibilities before moving code

**Objective:**

Decide which code belongs to HTTP, application, and persistence responsibilities.

**Concept:**

Ownership follows the reason a piece of code changes.

**What to implement:**

Classify these statements:

1. Parse JSON and produce `201 Created`.
2. Define “create a book” as a callable capability.
3. Construct a new `Book` and coordinate saving it.
4. Express the persistence operation the use case needs.
5. Invoke Spring Data and translate `DataIntegrityViolationException`.
6. Enforce the unique ISBN constraint.

**Starter code:**

~~~text
HTTP adapter:
Application input boundary:
Application use case:
Application output boundary:
Persistence adapter:
PostgreSQL:
~~~

**Hints:**

Put each statement in exactly one primary owner. Other layers may participate, but one layer should decide.

**How to verify:**

Your result should put items 1–6 respectively in the six categories shown in the starter.

**Common mistakes:**

- Putting HTTP status codes in the use case.
- Making the controller call `JpaRepository`.
- Putting database exception types in the input port.
- Assuming the Java duplicate check can replace the unique constraint.

**Explanation:**

The adapter translates technology-specific details at a boundary. PostgreSQL remains the final concurrency-safe uniqueness authority.

---

### Task 5 — Design the dependency arrows

**Objective:**

Design the target before creating files.

**Concept:**

The Dependency Rule says source dependencies point inward toward policy.

**What to implement:**

Draw the target path and mark interface ownership:

~~~text
BookController -> [input port] <- CreateBookService
CreateBookService -> [output port] <- BookPersistenceAdapter -> BookRepository
~~~

Name the exact contracts:

- `CreateBookUseCase`
- `BookRepositoryPort`

**Starter code:**

~~~text
// inbound caller sees:
interface CreateBookUseCase {
    Book create(/* application-owned input */);
}

// use case sees:
interface BookRepositoryPort {
    Book save(Book book);
}
~~~

**Hints:**

An adapter can depend on the port it implements. The port must not depend on its adapter.

**How to verify:**

Delete the words “Spring Data” and “HTTP” from the diagram mentally. The application contracts should still make sense.

**Common mistakes:**

- Defining ports in `adapter` packages.
- Returning `ResponseEntity` from an input port.
- Extending `JpaRepository` from an output port.
- Making one giant repository port mirror every Spring Data method.

**Explanation:**

The controller is replaceable because it talks through an application capability. Persistence is replaceable because the use case talks through the narrow operation it needs.

> **Pause point 2 — dependency inversion**
>
> How can `CreateBookService` call code that ultimately uses PostgreSQL without importing the PostgreSQL/Spring Data implementation?

<details>
<summary>Reveal the checkpoint</summary>

It calls `BookRepositoryPort`. At startup, Spring finds `BookPersistenceAdapter`, which implements that port, and injects it. Compile time points inward; runtime control returns outward through polymorphism.

</details>

---

### Task 6 — Introduce an application command

**Objective:**

Prevent the use case from depending on the web request DTO.

**Concept:**

`BookRequest` belongs to the HTTP adapter. `CreateBookCommand` belongs to the application boundary.

**What to implement:**

Create `book/application/port/in/CreateBookCommand.java` with the five values needed by the use case.

**Starter code:**

~~~java
package com.example.bookcatalog.book.application.port.in;

import java.math.BigDecimal;

public record CreateBookCommand(
        // TODO declare only the five values the use case needs
) {
}
~~~

**Hints:**

Keep Bean Validation on `BookRequest` for this slice. Do not duplicate all annotations simply to make the command look “clean.”

**How to verify:**

The new record imports only Java types. It imports neither `dto.BookRequest` nor a Spring type.

**Common mistakes:**

- Passing `BookRequest` directly into the application.
- Creating commands for unrelated use cases during this exercise.
- adding setters or framework annotations with no need;
- hiding five values in a `Map<String, Object>`.

**Explanation:**

Five scalar arguments obscure meaning and are easy to reorder. Reusing `BookRequest` would couple the application API to HTTP. One small command is the balanced choice.

---

### Task 7 — Define the input port

**Objective:**

Give inbound adapters a stable application-facing capability.

**Concept:**

An input port says what the application can do. It does not describe HTTP mechanics.

**What to implement:**

Create `book/application/port/in/CreateBookUseCase.java`.

**Starter code:**

~~~java
package com.example.bookcatalog.book.application.port.in;

public interface CreateBookUseCase {
    // TODO declare one Create Book application operation
}
~~~

**Hints:**

Keep the existing `Book` return type in this exercise. The deliberate JPA coupling trade-off is evaluated in Task 14.

**How to verify:**

The interface has no Spring MVC, security, JPA repository, `ResponseEntity`, or `BookRequest` import.

**Common mistakes:**

- Naming it `BookControllerService`.
- Returning HTTP status information.
- Adding every Book operation to one broad interface.
- Annotating the interface with `@Service`.

**Explanation:**

The port belongs to the application. A REST controller, CLI, scheduled job, or message consumer could invoke the same capability without changing its vocabulary.

---

### Task 8 — Define the narrow output port

**Objective:**

Describe only the persistence capability required by Create Book.

**Concept:**

An output port expresses a need of application policy; it is not a copy of a framework repository.

**What to implement:**

Create `book/application/port/out/BookRepositoryPort.java`.

**Starter code:**

~~~java
package com.example.bookcatalog.book.application.port.out;

public interface BookRepositoryPort {
    // TODO declare only the persistence capability Create Book needs
}
~~~

**Hints:**

Do not extend `JpaRepository`. Do not add `flush` to the application contract merely because the adapter needs it internally.

**How to verify:**

Search this file for `org.springframework` and `jakarta.persistence`. Neither should appear.

**Common mistakes:**

- Exposing `Pageable` or `EntityManager`.
- Creating `saveAndFlush` as application vocabulary.
- Adding methods for reads and updates that this slice does not use.
- Placing the interface beside `BookRepository` in the outer repository package.

**Explanation:**

`save` describes application intent. Whether the adapter uses JPA, SQL, or another mechanism—and whether it flushes to translate a constraint error—is an adapter decision.

---

### Task 9 — Implement the application use case

**Objective:**

Move Create Book orchestration into an application service.

**Concept:**

The use case coordinates policy and owns the transaction boundary.

**What to implement:**

Create `book/application/service/CreateBookService.java`. It should:

1. implement `CreateBookUseCase`;
2. depend only on `BookRepositoryPort`;
3. create a `Book` from the command;
4. save through the port;
5. use `@Transactional` on the public use-case method.

**Starter code:**

~~~java
package com.example.bookcatalog.book.application.service;

// imports intentionally left for you

@Service
public class CreateBookService implements CreateBookUseCase {
    private final BookRepositoryPort books;

    public CreateBookService(BookRepositoryPort books) {
        this.books = books;
    }

    @Override
    @Transactional
    public Book create(CreateBookCommand command) {
        // TODO construct Book and call the port
    }
}
~~~

**Hints:**

`@Transactional` is a pragmatic framework annotation in the application layer. It declares a use-case boundary, not a persistence implementation. Spring calls the public method through a proxy and binds an `EntityManager`/transaction context. Hibernate may acquire the physical JDBC connection from HikariCP lazily when SQL is first needed.

**How to verify:**

The service must not import:

- `BookRequest`;
- `BookController`;
- `BookRepository`;
- `DataIntegrityViolationException`;
- `ResponseEntity`.

**Common mistakes:**

- Putting `@Transactional` only on the adapter.
- Calling `new BookPersistenceAdapter(...)` yourself.
- accepting five unrelated scalar arguments again;
- swallowing exceptions in the use case.

**Explanation:**

One application operation should be atomic. The outer Spring proxy starts the transaction before entering `create`. The injected adapter later joins that same transaction.

| Placement | Meaning | Trade-off |
|---|---|---|
| Selected pragmatic design | `@Transactional` on the public `CreateBookService.create` method | concise and obvious, but the application class imports Spring transaction metadata |
| Stricter alternative | plain service wrapped by an outer transaction decorator or `TransactionTemplate` configuration | framework-free application service, but more composition code and indirection |

Use the pragmatic option here because the project is a small Spring application and the annotation expresses the use-case boundary clearly. The stricter alternative becomes worthwhile only when framework-free application code is a concrete requirement, not as automatic ceremony.

---

### Task 10 — Implement the Spring Data persistence adapter

**Objective:**

Connect the application output port to the existing repository.

**Concept:**

An outbound adapter knows both sides: the inner port and the outer framework.

**What to implement:**

Create `book/adapter/out/persistence/BookPersistenceAdapter.java`. Inject the existing `BookRepository`, implement `BookRepositoryPort`, use `saveAndFlush`, and translate `DataIntegrityViolationException` to the existing `DuplicateIsbnException`.

**Starter code:**

~~~java
package com.example.bookcatalog.book.adapter.out.persistence;

// TODO imports

@Component
public class BookPersistenceAdapter implements BookRepositoryPort {
    private final BookRepository repository;

    public BookPersistenceAdapter(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        try {
            // TODO persist and force constraint detection here
        } catch (DataIntegrityViolationException exception) {
            // TODO translate outward technology exception
        }
    }
}
~~~

**Hints:**

Why flush? The unique violation must occur while execution is still inside the adapter's `try` block. `save` alone can defer SQL until transaction commit, after this method returns.

**How to verify:**

The adapter:

- implements the application-owned port;
- is a Spring bean;
- is the only new production class importing both `BookRepository` and `BookRepositoryPort`;
- contains no HTTP code.

**Common mistakes:**

- Catching `Exception`.
- Calling `flush` and assuming it commits.
- adding another `@Transactional(REQUIRES_NEW)`;
- replacing the database unique constraint with `existsByIsbn`.

**Explanation:**

`saveAndFlush` may execute the INSERT before the transaction ends, but commit still belongs to the surrounding use-case transaction. If an exception propagates, Spring rolls back. The broad integrity-to-duplicate translation preserves current behavior, but should be refined if other integrity constraints later need different API errors.

---

### Task 11 — Rewire only `POST /books`

**Objective:**

Make the controller call the input port without disturbing other endpoints.

**Concept:**

An inbound adapter translates transport input into application input and application output into transport output.

**What to implement:**

Change `BookController` to inject both:

- existing `BookService` for untouched operations;
- `CreateBookUseCase` for Create Book.

Map `BookRequest` to `CreateBookCommand` inside `create`. Preserve `201`, `Location`, and `BookResponse`.

**Starter code:**

~~~java
private final BookService bookService;
private final CreateBookUseCase createBook;

public BookController(
        BookService bookService,
        CreateBookUseCase createBook) {
    this.bookService = bookService;
    this.createBook = createBook;
}

@PostMapping(consumes = "application/json", produces = "application/json")
public ResponseEntity<BookResponse> create(
        @Valid @RequestBody BookRequest request) {
    CreateBookCommand command = new CreateBookCommand(
            request.isbn(),
            request.title(),
            request.author(),
            request.price(),
            request.stock());
    Book book = /* TODO call input port */;
    return /* TODO preserve existing response */;
}
~~~

**Hints:**

Keep `@Valid` here because it validates the HTTP request. Do not move `URI.create(...)` into the use case.

**How to verify:**

Only the POST method uses the new port. GET, PUT, PATCH, and DELETE still delegate to `BookService`.

**Common mistakes:**

- Rewriting the whole controller.
- Returning `Book` directly.
- moving `@Valid` to the persistence adapter;
- removing the security configuration because the input port “protects” the use case.

**Explanation:**

The controller is still a Spring MVC adapter. Its mapping work is small and explicit, which is preferable here to adding MapStruct or a mapper abstraction for five fields.

---

### Task 12 — Remove the old Create Book implementation

**Objective:**

Establish one owner for the use case.

**Concept:**

Incremental refactoring should not leave two active paths for the same business operation.

**What to implement:**

Delete only `BookService.create(...)` and its now-unused imports. Keep:

- class-level `@Transactional(readOnly = true)`;
- `findById` and `findAll`;
- `replace`;
- `setStock` and its method security;
- the deliberate rollback experiment in `delete`.

**Starter code:**

~~~java
@Service
@Transactional(readOnly = true)
public class BookService {
    // create(...) is removed
    // all other existing methods remain
}
~~~

**Hints:**

Compile immediately. Import cleanup is useful feedback that the dependency was actually moved.

**How to verify:**

~~~powershell
rg "Book create\(" src/main/java
rg "bookService\.create" src/main/java
~~~

The only production `create` implementation should be `CreateBookService.create`, and the controller should no longer call `bookService.create`.

**Common mistakes:**

- Removing the entire legacy service.
- changing the checked-exception rollback demonstration;
- moving unrelated methods as “cleanup”;
- leaving dead code commented out.

**Explanation:**

A transitional architecture is healthy when the boundary is explicit: Create Book follows ports and adapters; legacy operations continue in the existing service until they earn a separate refactor.

---

### Task 13 — Prove transaction ownership and runtime wiring

**Objective:**

Explain what Spring does underneath the new boundary.

**Concept:**

Dependency inversion changes source dependencies; Spring dependency injection and transaction proxies supply runtime behavior.

**What to implement:**

Trace a successful call and a duplicate call:

~~~text
controller
 -> injected CreateBookUseCase reference (Spring proxy)
 -> TransactionInterceptor
 -> JpaTransactionManager
 -> CreateBookService
 -> BookRepositoryPort reference
 -> BookPersistenceAdapter
 -> Spring Data repository proxy
 -> EntityManager/Hibernate
 -> JDBC/Hikari/PostgreSQL
~~~

Mark where transaction start, flush, INSERT, commit, and rollback occur.

**Starter code:**

~~~text
Success:
transaction start -> ? -> flush/INSERT -> ? -> 201

Duplicate:
transaction start -> ? -> unique violation -> exception translation
                  -> ? -> 409
~~~

**Hints:**

The `@Transactional` method must be reached through its Spring proxy. The adapter's `saveAndFlush` participates in the existing transaction.

**How to verify:**

Enable the existing transaction and SQL loggers, then correlate one request with transaction creation, SQL INSERT, commit or rollback, and connection return to HikariCP.

**Common mistakes:**

- Saying the controller opens the transaction.
- Saying `flush` commits.
- assuming the port itself performs dependency injection;
- putting `REQUIRES_NEW` on the adapter.

**Explanation:**

Spring creates the concrete object graph. The application source knows only contracts, yet runtime dispatch reaches the outer adapter. One transaction spans the use case because Spring's proxy starts it before the service method and completes it after the method returns or throws.

---

### Task 14 — Make the pragmatic JPA-domain decision consciously

**Objective:**

Decide whether to retain one `Book` class or split domain and persistence models.

**Concept:**

Clean Architecture is a dependency strategy, not an automatic requirement to duplicate every model.

**What to implement:**

Compare two options:

| Option | Benefit | Cost |
|---|---|---|
| Keep existing JPA `Book` | minimal refactor, no mapper, identity/version behavior stays clear | application model retains Jakarta Persistence annotations |
| Add `Book` + `BookJpaEntity` | framework-free domain | mapping, identity/version synchronization, more files and tests |

Write and defend your own decision before opening the reference decision in the Explanation. Document the benefit, accepted coupling, and a concrete trigger for revisiting it.

**Starter code:**

~~~text
Decision:
We retain com.example.bookcatalog.model.Book.

Reason:
____________________________________________________

Accepted coupling:
____________________________________________________

Trigger that could justify a later split:
____________________________________________________
~~~

**Hints:**

Consider behavior complexity, team size, alternative persistence needs, mapping bugs, and the actual cost of Jakarta annotations.

**How to verify:**

No `BookJpaEntity`, mapper interface, or mapping dependency appears in your changes.

**Common mistakes:**

- Calling the choice “perfectly pure.”
- claiming database replacement is now free;
- adding duplicate models without behavior that justifies them;
- refusing any framework annotation on ideological grounds.

**Explanation:**

> **Pause point 3 — make the decision before reading on**
>
> Would a separate persistence entity produce enough value in this project today to pay for mapping IDs, `@Version`, dirty-checking semantics, and every field twice?

<details>
<summary>Reference decision</summary>

No. Keep one `Book` for this exercise. Revisit the choice only if the domain grows richer, persistence models diverge, multiple storage mechanisms appear, or framework-free domain testing becomes materially valuable.

</details>

The reference therefore retains the existing entity. For this small CRUD-oriented model, that is a transparent compromise. The port removes Spring Data from the use case, but `Book` still couples the model to JPA annotations. Clean Architecture can be applied by degree.

---

### Task 15 — Trace validation, security, and exception ownership

**Objective:**

Verify that cross-cutting concerns still occur at the correct boundaries.

**Concept:**

Clean boundaries do not mean every concern belongs in the center.

**What to implement:**

Complete this ownership map:

| Concern | Owner in this slice |
|---|---|
| JSON shape and Bean Validation | ? |
| URL authorization and CSRF | ? |
| use-case transaction | ? |
| ISBN uniqueness under concurrency | ? |
| provider/JDBC failure → Spring persistence exception | ? |
| Spring persistence exception → application exception | ? |
| exception-to-HTTP Problem Detail | ? |

**Starter code:**

~~~text
invalid request -> controller validation -> 400
non-admin request -> security filter chain -> 403
duplicate ISBN -> database -> adapter -> application exception -> web advice -> 409
~~~

**Hints:**

The existing unique database constraint remains necessary even if an availability check is added later.

**How to verify:**

Name the exact current classes/configuration for every row without saying only “Spring handles it.”

**Common mistakes:**

- Moving `ProblemDetail` into the use case.
- Catching `DuplicateIsbnException` in the controller.
- treating Bean Validation as the database integrity layer;
- assuming authentication equals business authorization.

**Explanation:**

The answer is: controller/DTO; `SecurityFilterChain`; `CreateBookService`; PostgreSQL; Hibernate/JPA plus Spring's repository exception-translation infrastructure; `BookPersistenceAdapter`; `GlobalExceptionHandler`. Spring first exposes a stable `DataIntegrityViolationException`; the adapter then converts that persistence meaning to the application-facing `DuplicateIsbnException`.

---

### Task 16 — Add a pure use-case unit test

**Objective:**

Test application orchestration without starting Spring or PostgreSQL.

**Concept:**

An output port is a controllable seam. A handwritten fake makes the test fast and explicit.

**What to implement:**

Create `CreateBookServiceTest` using JUnit Jupiter and a small recording implementation of `BookRepositoryPort`. Assert that command values become the saved `Book` and the returned object is the adapter result.

**Starter code:**

~~~java
class CreateBookServiceTest {

    @Test
    void createsAndSavesBook() {
        RecordingBookRepository books = new RecordingBookRepository();
        CreateBookService service = new CreateBookService(books);

        CreateBookCommand command = new CreateBookCommand(
                "978-1", "Clean Boundaries", "A. Author",
                new BigDecimal("39.90"), 7);

        Book result = service.create(command);

        // TODO assertions
    }

    private static final class RecordingBookRepository
            implements BookRepositoryPort {
        private Book saved;

        @Override
        public Book save(Book book) {
            // TODO record and return
        }
    }
}
~~~

**Hints:**

`@Transactional` does nothing in a plain `new CreateBookService(...)` unit test—and that is fine. This test targets orchestration, not Spring interception.

**How to verify:**

The test imports no Spring test annotation, starts no application context, and needs no Docker daemon.

**Common mistakes:**

- Calling it an integration test.
- asserting a generated database ID;
- mocking `JpaRepository` instead of the application port;
- believing this proves transaction or constraint behavior.

**Explanation:**

The test checks application policy in isolation. A separate adapter integration test must prove JPA mapping, Flyway schema, PostgreSQL constraints, and exception translation.

---

### Task 17 — Add a real PostgreSQL adapter integration test

**Objective:**

Test the persistence boundary against the same database family used in production.

**Concept:**

Ports make unit tests easy; adapters still require integration tests because framework mappings and database behavior are real risks.

**What to implement:**

1. Replace the stale JUnit 4 dependency with Boot 4.1.1 test dependencies.
2. Remove `src/test/java/com/example/AppTest.java`.
3. Add `TestPostgresConfiguration` with a PostgreSQL 17 Testcontainer and `@ServiceConnection`.
4. Add `application-test.yaml`.
5. Add `BookPersistenceAdapterIntegrationTest` using `@DataJpaTest`, Flyway, and no H2 replacement.
6. Test a successful save and duplicate-ISBN translation.

**Starter code:**

~~~java
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TestPostgresConfiguration.class,
        BookPersistenceAdapter.class
})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@ActiveProfiles("test")
class BookPersistenceAdapterIntegrationTest {
    // TODO inject adapter and repository
    // TODO save test
    // TODO duplicate test
}
~~~

**Hints:**

For Boot 4.1, use the modular test artifacts and packages shown in the reference solution. `@ServiceConnection` supplies connection details that override the normal local datasource for the test context.

**How to verify:**

With a Docker-compatible runtime available:

~~~powershell
mvn clean test
~~~

Look for Flyway applying V1–V6 to the container and both test classes passing.

**Common mistakes:**

- Adding H2 and assuming PostgreSQL constraints behave identically.
- reading migrations from stale `target/classes`;
- allowing `@DataJpaTest` to replace the datasource with an embedded database;
- using an old Surefire version that does not discover Jupiter correctly;
- claiming the test is “unit” because it has one adapter.

**Explanation:**

The integration test crosses adapter, Spring Data proxy, Hibernate mapping, JDBC, container connection, Flyway, and PostgreSQL. It intentionally does not test the controller or security filter chain.

---

### Task 18 — Verify architecture and observable behavior

**Objective:**

Prove that dependency direction improved and the API did not regress.

**Concept:**

Architecture is valuable only when source dependencies, runtime behavior, and operational behavior all agree.

**What to implement:**

Perform four checks:

1. compile/test check;
2. import/dependency check;
3. manual ADMIN/CSRF API check;
4. PostgreSQL state check.

**Starter code:**

~~~powershell
mvn clean test
rg "com\.example\.bookcatalog\.(web|repository|dto)" src/main/java/com/example/bookcatalog/book/application
rg "CreateBookUseCase|BookRepositoryPort" src/main/java/com/example/bookcatalog
~~~

**Hints:**

Use browser/Postman/curl session cookies and a fresh CSRF token after login. Query `books` directly after successful, duplicate, and deliberately rolled-back operations.

**How to verify:**

Your evidence should show:

- no forbidden imports in application classes;
- one `INSERT` and one commit for a valid create;
- rollback and `409` for duplicate ISBN;
- unchanged `201` body and `Location`;
- unchanged `403` security behavior;
- unchanged deliberate DELETE rollback behavior;
- all new tests pass.

**Common mistakes:**

- Verifying only that the project compiles.
- skipping authorization and CSRF checks;
- inspecting stale `target` classes instead of source;
- equating an INSERT log line with a committed row;
- refactoring unrelated code after tests pass.

**Explanation:**

This task closes the loop: dependency rules are checked statically, framework wiring is checked by tests, the HTTP contract is checked externally, and durable state is checked in PostgreSQL.

## Pre-solution architecture checkpoint

Answer these without opening the reference implementation:

1. Which layer owns `CreateBookUseCase`?
2. Why does `BookPersistenceAdapter` depend on `BookRepositoryPort` rather than the reverse?
3. Why is `CreateBookCommand` different from `BookRequest` even though their fields match?
4. Where does the transaction begin at runtime?
5. Why does the adapter use `saveAndFlush`?
6. Does flush mean commit?
7. Which component turns a duplicate constraint failure into `409`?
8. Which source coupling remains because the same JPA `Book` is reused?
9. Why is the persistence integration test not allowed to use H2?
10. What behavior proves that this was a refactor rather than an API redesign?

<details>
<summary>Short checkpoint answers</summary>

1. The application layer.
2. The application owns the abstraction; the outbound adapter implements it.
3. One is application vocabulary; the other is HTTP input with web validation.
4. In the Spring proxy around `CreateBookService.create`.
5. To surface the unique violation inside the adapter's translation boundary.
6. No. A later rollback still undoes flushed SQL.
7. PostgreSQL detects it, the adapter translates it, and `GlobalExceptionHandler` maps the translated exception to HTTP.
8. The application uses a class annotated with Jakarta Persistence annotations.
9. PostgreSQL-specific schema and constraint behavior must be tested on PostgreSQL.
10. The same request, security requirements, status, headers, JSON, and database effects.

</details>

## Manual verification plan before the solution

Record evidence for this matrix:

| ID | Actor/state | Request | Expected |
|---|---|---|---|
| M1 | anonymous | `GET /books` | `200` |
| M2 | anonymous | `GET /books/{existingId}` / missing ID | `200` / `404` |
| M3 | anonymous/no token | `POST /books` | `403` before the controller |
| M4 | anonymous + valid token | `POST /books` | `401` because authentication is still required |
| M5 | logged-in USER + valid token | `POST /books` | `403` |
| M6 | logged-in ADMIN + valid token | valid unique `POST /books` | `201` + `Location` + row |
| M7 | logged-in ADMIN + valid token | invalid create body | `400` + no row |
| M8 | logged-in ADMIN + valid token | duplicate ISBN | `409` + one row for ISBN |
| M9 | logged-in ADMIN + valid token | existing valid `PUT /books/{id}` | unchanged `200` + replacement |
| M10 | logged-in ADMIN + valid token | existing `PATCH /books/{id}` | unchanged `200` + stock update |
| M11 | logged-in ADMIN + valid token | existing `DELETE /books/{id}` | `500` + row still exists |

M9–M11 are regression checks for slices you did not refactor. Do not “improve” the current `StockRequest` validation during this exercise: it has no constraints, and the schema has no nonnegative-stock check. Record that debt separately rather than mixing behavior changes into the architecture refactor.

Do not read the reference code until you have at least:

- drawn both dependency directions;
- written your interfaces;
- attempted controller wiring;
- predicted transaction and exception flow;
- decided why a separate persistence entity is not being added.

==================================================
STOP — ATTEMPT THE EXERCISE BEFORE READING SOLUTION
==================================================
## Complete Reference Solution

The reference chooses a **pragmatic one-model approach**: `Book` remains both the JPA entity and the model passed across the port. The key gain is that Create Book application orchestration no longer knows Spring Data or HTTP. A second entity and mapper would add more cost than learning value for this slice.

### 1. Exact change inventory

#### Existing files modified

| File | Why |
|---|---|
| `pom.xml` | replace stale JUnit 4/plugin pins with the Boot 4.1.1 test stack |
| `src/main/java/com/example/bookcatalog/web/BookController.java` | route only Create Book through the input port |
| `src/main/java/com/example/bookcatalog/service/BookService.java` | remove the old duplicate Create Book implementation |

#### Existing file removed

| File | Why |
|---|---|
| `src/test/java/com/example/AppTest.java` | generated JUnit 4 placeholder; replaced by meaningful Jupiter tests |

#### Files added

~~~text
src/main/java/com/example/bookcatalog/book/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateBookCommand.java
│   │   │   └── CreateBookUseCase.java
│   │   └── out/
│   │       └── BookRepositoryPort.java
│   └── service/
│       └── CreateBookService.java
└── adapter/
    └── out/
        └── persistence/
            └── BookPersistenceAdapter.java

src/main/resources/db/migration/
├── V1__create_books.sql
├── V2__add_optimistic_lock_version.sql
├── V3__create_transaction_audits.sql
├── V4__add_books_author_index.sql
├── V5__replace_author_index_for_paging.sql
└── V6__create_app_users.sql

src/test/java/com/example/bookcatalog/
├── book/
│   ├── application/service/CreateBookServiceTest.java
│   └── adapter/out/persistence/BookPersistenceAdapterIntegrationTest.java
└── support/TestPostgresConfiguration.java

src/test/resources/application-test.yaml
~~~

| Added file/group | Why it is required |
|---|---|
| `CreateBookCommand` | application-owned input instead of an HTTP DTO or five scalar arguments |
| `CreateBookUseCase` | stable inbound application capability |
| `BookRepositoryPort` | application-owned persistence requirement |
| `CreateBookService` | use-case orchestration and transaction boundary |
| `BookPersistenceAdapter` | Spring Data implementation and persistence-exception translation |
| source V1–V6 migrations | make clean builds and fresh databases reproducible |
| `CreateBookServiceTest` | framework-free use-case verification |
| `TestPostgresConfiguration` | real PostgreSQL 17 connection for integration tests |
| `BookPersistenceAdapterIntegrationTest` | verify JPA, Flyway, constraint, and adapter behavior together |
| `application-test.yaml` | explicit integration-test Flyway/JPA/logging settings |

#### Existing source/configuration files unchanged

The following are intentionally unchanged:

~~~text
.env
.env.example
compose.yaml
.github/modernize/java-upgrade/.gitignore
.github/modernize/java-upgrade/hooks/scripts/recordToolUse.ps1
.github/modernize/java-upgrade/hooks/scripts/recordToolUse.sh
src/main/java/com/example/bookcatalog/Application.java

src/main/java/com/example/bookcatalog/dto/
├── BookRequest.java
├── BookResponse.java
├── CurrentUserResponse.java
├── RegisterRequest.java
├── StockRequest.java
└── StockTransferRequest.java

src/main/java/com/example/bookcatalog/exception/
├── BookNotFoundException.java
├── DuplicateIsbnException.java
└── TransactionLabCheckedException.java

src/main/java/com/example/bookcatalog/model/
├── AppUser.java
├── Book.java
└── TransactionAudit.java

src/main/java/com/example/bookcatalog/repository/
├── AppUserRepository.java
├── BookRepository.java
└── TransactionAuditRepository.java

src/main/java/com/example/bookcatalog/security/
├── AppUserDetailsService.java
├── LocalAdminBootstrap.java
├── RestAccessDeniedHandler.java
├── RestAuthenticationEntryPoint.java
└── SecurityConfig.java

src/main/java/com/example/bookcatalog/service/
├── BookStockService.java
├── RegistrationService.java
├── StockTransferService.java
└── TransactionAuditService.java

src/main/java/com/example/bookcatalog/web/
├── AuthController.java
├── GlobalExceptionHandler.java
└── TransactionLabController.java

src/main/resources/application.yaml
~~~

Everything under `target/` is generated build output, not source inventory. Run `mvn clean` rather than editing it.

### 2. Final relevant project tree

~~~text
book-catalog-api/
├── compose.yaml
├── pom.xml                                      [modified]
├── src/
│   ├── main/
│   │   ├── java/com/example/bookcatalog/
│   │   │   ├── Application.java
│   │   │   ├── book/
│   │   │   │   ├── application/
│   │   │   │   │   ├── port/
│   │   │   │   │   │   ├── in/
│   │   │   │   │   │   │   ├── CreateBookCommand.java       [new]
│   │   │   │   │   │   │   └── CreateBookUseCase.java       [new]
│   │   │   │   │   │   └── out/
│   │   │   │   │   │       └── BookRepositoryPort.java       [new]
│   │   │   │   │   └── service/
│   │   │   │   │       └── CreateBookService.java            [new]
│   │   │   │   └── adapter/out/persistence/
│   │   │   │       └── BookPersistenceAdapter.java            [new]
│   │   │   ├── dto/                                           [unchanged]
│   │   │   ├── exception/                                     [unchanged]
│   │   │   ├── model/                                         [unchanged]
│   │   │   ├── repository/                                    [unchanged]
│   │   │   ├── security/                                      [unchanged]
│   │   │   ├── service/
│   │   │   │   ├── BookService.java                           [modified]
│   │   │   │   └── other services                             [unchanged]
│   │   │   └── web/
│   │   │       ├── BookController.java                        [modified]
│   │   │       └── other web classes                          [unchanged]
│   │   └── resources/
│   │       ├── application.yaml                               [unchanged]
│   │       └── db/migration/                                  [restored]
│   │           ├── V1__create_books.sql
│   │           ├── V2__add_optimistic_lock_version.sql
│   │           ├── V3__create_transaction_audits.sql
│   │           ├── V4__add_books_author_index.sql
│   │           ├── V5__replace_author_index_for_paging.sql
│   │           └── V6__create_app_users.sql
│   └── test/
│       ├── java/com/example/bookcatalog/
│       │   ├── book/application/service/
│       │   │   └── CreateBookServiceTest.java                 [new]
│       │   ├── book/adapter/out/persistence/
│       │   │   └── BookPersistenceAdapterIntegrationTest.java [new]
│       │   └── support/TestPostgresConfiguration.java          [new]
│       └── resources/application-test.yaml                     [new]
└── target/                                                     [generated]
~~~

### 3. Complete `pom.xml`

The parent manages compatible versions. Removing old lifecycle plugin pins is important: the old Surefire 2.22.1 configuration came from an archetype-era build and should not override Boot 4.1.1's managed test toolchain.

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
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

    <properties>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
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
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
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
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
~~~

Why each test dependency exists:

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-test` | JUnit Jupiter, assertions, and core Boot test support |
| `spring-boot-starter-data-jpa-test` | Boot 4 modular JPA test slice, including `@DataJpaTest` |
| `spring-boot-testcontainers` | `@ServiceConnection` integration |
| `testcontainers-postgresql` | PostgreSQL container implementation |

In Boot 4.1, `spring-boot-starter-data-jpa-test` already composes the core test starter. Listing `spring-boot-starter-test` explicitly above is intentional for teaching visibility but redundant; you may remove that one declaration after confirming the dependency tree. Do not remove the JPA test starter, Testcontainers support, or PostgreSQL module used by the shown imports.

### 4. Restored Flyway migrations

These are restorations of the inspected V1–V6 files. If your database already contains these versions, the contents and names must match the applied history.

#### `src/main/resources/db/migration/V1__create_books.sql`

~~~sql
CREATE TABLE IF NOT EXISTS books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(120) UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(120) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    stock INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
~~~

#### `src/main/resources/db/migration/V2__add_optimistic_lock_version.sql`

~~~sql
ALTER TABLE books
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
~~~

V2 is redundant for a fresh database because V1 already has `version`. Preserve it anyway: migration history is historical record, not a script collection to rewrite casually.

#### `src/main/resources/db/migration/V3__create_transaction_audits.sql`

~~~sql
CREATE TABLE IF NOT EXISTS transaction_audits (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    details VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
~~~

#### `src/main/resources/db/migration/V4__add_books_author_index.sql`

~~~sql
CREATE INDEX idx_books_author
ON books (author);
~~~

#### `src/main/resources/db/migration/V5__replace_author_index_for_paging.sql`

~~~sql
DROP INDEX idx_books_author;

CREATE INDEX idx_books_author_id
ON books (author, id);
~~~

#### `src/main/resources/db/migration/V6__create_app_users.sql`

~~~sql
CREATE TABLE app_users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL,
    CONSTRAINT ck_app_users_role CHECK (role IN ('USER', 'ADMIN'))
);
~~~

Expected clean-start sequence:

~~~text
Testcontainer PostgreSQL starts
 -> HikariCP obtains connections
 -> Flyway creates flyway_schema_history
 -> V1, V2, V3, V4, V5, V6 run in order
 -> Hibernate validates entity mappings
 -> repository tests execute
~~~

### 5. Application input model and input port

#### `src/main/java/com/example/bookcatalog/book/application/port/in/CreateBookCommand.java`

~~~java
package com.example.bookcatalog.book.application.port.in;

import java.math.BigDecimal;

public record CreateBookCommand(
        String isbn,
        String title,
        String author,
        BigDecimal price,
        Integer stock) {
}
~~~

#### `src/main/java/com/example/bookcatalog/book/application/port/in/CreateBookUseCase.java`

~~~java
package com.example.bookcatalog.book.application.port.in;

import com.example.bookcatalog.model.Book;

public interface CreateBookUseCase {

    Book create(CreateBookCommand command);
}
~~~

Why not pass `BookRequest`?

~~~text
BookRequest
  owns HTTP input validation and JSON-facing shape
  lives in an inbound adapter concern

CreateBookCommand
  names the application operation's input
  remains useful if another inbound adapter is added
~~~

Why return `Book` rather than a second result type? It keeps this first slice small. If exposing the JPA-backed model across the application boundary becomes costly, introduce an application result type deliberately later.

### 6. Application output port

#### `src/main/java/com/example/bookcatalog/book/application/port/out/BookRepositoryPort.java`

~~~java
package com.example.bookcatalog.book.application.port.out;

import com.example.bookcatalog.model.Book;

public interface BookRepositoryPort {

    Book save(Book book);
}
~~~

Notice what is absent:

- no `JpaRepository` inheritance;
- no `EntityManager`;
- no `saveAndFlush`;
- no `Pageable`;
- no Spring annotation.

The port is narrow because Create Book needs one persistence operation.

### 7. Application service

#### `src/main/java/com/example/bookcatalog/book/application/service/CreateBookService.java`

~~~java
package com.example.bookcatalog.book.application.service;

import com.example.bookcatalog.book.application.port.in.CreateBookCommand;
import com.example.bookcatalog.book.application.port.in.CreateBookUseCase;
import com.example.bookcatalog.book.application.port.out.BookRepositoryPort;
import com.example.bookcatalog.model.Book;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateBookService implements CreateBookUseCase {

    private final BookRepositoryPort books;

    public CreateBookService(BookRepositoryPort books) {
        this.books = books;
    }

    @Override
    @Transactional
    public Book create(CreateBookCommand command) {
        Book book = new Book(
                command.isbn(),
                command.title(),
                command.author(),
                command.price(),
                command.stock());

        return books.save(book);
    }
}
~~~

#### Transaction meaning

At compile time, `CreateBookService` knows only the port. At runtime:

~~~text
controller calls injected CreateBookUseCase proxy
 -> TransactionInterceptor opens transaction
 -> JpaTransactionManager binds EntityManager
 -> Hibernate obtains/uses JDBC Connection through HikariCP
 -> use case calls BookRepositoryPort
 -> injected BookPersistenceAdapter runs in same transaction
 -> proxy commits on normal return
 -> connection becomes available to the pool
~~~

The Spring annotations are a deliberate pragmatic choice. `@Service` enables component discovery; `@Transactional` declares a use-case boundary. Neither causes the service to import Spring Data or HTTP.

### 8. Outbound persistence adapter

#### `src/main/java/com/example/bookcatalog/book/adapter/out/persistence/BookPersistenceAdapter.java`

~~~java
package com.example.bookcatalog.book.adapter.out.persistence;

import com.example.bookcatalog.book.application.port.out.BookRepositoryPort;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class BookPersistenceAdapter implements BookRepositoryPort {

    private final BookRepository repository;

    public BookPersistenceAdapter(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        try {
            return repository.saveAndFlush(book);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateIsbnException(exception);
        }
    }
}
~~~

#### Why `saveAndFlush` appears only here

With `GenerationType.IDENTITY`, Hibernate may issue the INSERT early to obtain the identifier, but application code must not rely on every provider/timing detail. Explicit flush makes database constraint detection happen inside this adapter method:

~~~text
saveAndFlush
 -> persistence context receives Book
 -> flush
 -> SQL INSERT executes
 -> PostgreSQL checks UNIQUE(isbn)
 -> success returns OR DataIntegrityViolationException is caught here
~~~

Flush still does not commit:

~~~text
Java save
   != flush
flush / INSERT
   != commit
commit occurs only when outer transaction completes
~~~

The adapter intentionally has no `@Transactional`. It joins the transaction already established by the application service. Spring Data's repository transaction metadata does not replace the use-case boundary.

> **Practical limitation**
>
> The adapter maps every `DataIntegrityViolationException` to duplicate ISBN because that preserves the project's current behavior. If more independent constraints become client-visible, inspect the constraint/cause or introduce more precise persistence translation rather than mislabeling every integrity error.

### 9. Modified inbound adapter

#### Complete `src/main/java/com/example/bookcatalog/web/BookController.java`

~~~java
package com.example.bookcatalog.web;

import com.example.bookcatalog.book.application.port.in.CreateBookCommand;
import com.example.bookcatalog.book.application.port.in.CreateBookUseCase;
import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.dto.BookResponse;
import com.example.bookcatalog.dto.StockRequest;
import com.example.bookcatalog.exception.TransactionLabCheckedException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CreateBookUseCase createBook;

    public BookController(
            BookService bookService,
            CreateBookUseCase createBook) {
        this.bookService = bookService;
        this.createBook = createBook;
    }

    @GetMapping(produces = "application/json")
    public List<BookResponse> findAll(
            @RequestParam(name = "author", required = false) String author,
            @Min(value = 0, message = "page must be at least 0")
            @Max(value = 1_000_000, message = "page must be at most 1000000")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Min(value = 1, message = "size must be at least 1")
            @Max(value = 1000, message = "size must be at least 1000")
            @RequestParam(name = "size", defaultValue = "20") int size) {
        String filter = author == null
                ? null
                : checkedText(author, "author", 120);
        return bookService.findAll(filter, page, size)
                .stream()
                .map(BookResponse::from)
                .toList();
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public BookResponse findById(
            @Positive @PathVariable("id") long id) {
        return BookResponse.from(bookService.findById(id));
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<BookResponse> create(
            @Valid @RequestBody BookRequest request) {
        CreateBookCommand command = new CreateBookCommand(
                request.isbn(),
                request.title(),
                request.author(),
                request.price(),
                request.stock());

        Book book = createBook.create(command);

        return ResponseEntity
                .created(URI.create("/books/" + book.getId()))
                .body(BookResponse.from(book));
    }

    @PutMapping(
            path = "/{id}",
            consumes = "application/json",
            produces = "application/json")
    public BookResponse replace(
            @Positive(message = "id must be positive")
            @PathVariable("id") long id,
            @Valid @RequestBody BookRequest request) {
        return BookResponse.from(bookService.replace(
                id,
                request.isbn(),
                request.title(),
                request.author(),
                request.price(),
                request.stock()));
    }

    @PatchMapping(
            path = "/{id}",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<BookResponse> setStock(
            @Positive(message = "id must be positive")
            @PathVariable("id") long id,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(
                BookResponse.from(bookService.setStock(id, request.stock())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Positive(message = "id must be positive")
            @PathVariable("id") long id)
            throws TransactionLabCheckedException {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static String checkedText(
            String value,
            String field,
            int maxLength) {
        if (value == null
                || value.trim().isEmpty()
                || value.trim().length() > maxLength) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " is required and must fit its maximum length");
        }
        return value.trim();
    }
}
~~~

The only architectural route change is inside `create`. The controller still owns:

- Spring MVC annotations;
- `@Valid` on the HTTP DTO;
- command mapping;
- response DTO mapping;
- `201 Created` and the `Location` URI.

The compact reference removes inactive commented alternatives and unused private helpers from the inspected file. It does not change an active endpoint contract. In particular, `checkedText` still raises `ResponseStatusException(400)`; changing it to a generic exception would accidentally turn bad input into the global handler's `500`. If you want the absolutely smallest textual diff, retain the dead comments/helpers; they are irrelevant to the architecture.

### 10. Modified legacy service

#### Complete `src/main/java/com/example/bookcatalog/service/BookService.java`

~~~java
package com.example.bookcatalog.service;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.exception.TransactionLabCheckedException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository repo;

    public BookService(BookRepository repo) {
        this.repo = repo;
    }

    public Book findById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public List<Book> findAll(String author, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id"));

        if (author == null) {
            return repo.findAll(pageable).getContent();
        }

        return repo.findByAuthor(author, pageable);
    }

    @Transactional
    public Book replace(
            long id,
            String isbn,
            String title,
            String author,
            BigDecimal price,
            Integer stock) {
        try {
            Book book = repo.findById(id)
                    .orElseThrow(() -> new BookNotFoundException(id));

            book.replace(isbn, title, author, price, stock);
            repo.flush();
            return book;
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateIsbnException(exception);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Book setStock(long id, Integer stock) {
        Book book = repo.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        book.setStock(stock);
        return book;
    }

    @Transactional(rollbackFor = TransactionLabCheckedException.class)
    public void delete(long id) throws TransactionLabCheckedException {
        Book book = repo.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        repo.delete(book);
        repo.flush();
        throw new TransactionLabCheckedException(
                "Intentional failure after flush");
    }
}
~~~

Only `create` moved. Notice that:

- reads inherit class-level `readOnly = true`;
- write methods override it;
- update still uses managed-entity dirty checking;
- DELETE still flushes and throws a checked exception configured for rollback;
- existing method security on `setStock` remains.

### 11. Pure application test

#### `src/test/java/com/example/bookcatalog/book/application/service/CreateBookServiceTest.java`

~~~java
package com.example.bookcatalog.book.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.example.bookcatalog.book.application.port.in.CreateBookCommand;
import com.example.bookcatalog.book.application.port.out.BookRepositoryPort;
import com.example.bookcatalog.model.Book;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CreateBookServiceTest {

    @Test
    void createsBookAndSavesItThroughTheOutputPort() {
        RecordingBookRepository books = new RecordingBookRepository();
        CreateBookService service = new CreateBookService(books);
        CreateBookCommand command = new CreateBookCommand(
                "978-1-23456-789-0",
                "Clean Boundaries",
                "A. Author",
                new BigDecimal("39.90"),
                7);

        Book result = service.create(command);

        assertSame(books.saved, result);
        assertEquals("978-1-23456-789-0", result.getIsbn());
        assertEquals("Clean Boundaries", result.getTitle());
        assertEquals("A. Author", result.getAuthor());
        assertEquals(new BigDecimal("39.90"), result.getPrice());
        assertEquals(7, result.getStock());
    }

    private static final class RecordingBookRepository
            implements BookRepositoryPort {

        private Book saved;

        @Override
        public Book save(Book book) {
            this.saved = book;
            return book;
        }
    }
}
~~~

What this proves:

- command-to-model construction;
- use of the output port;
- no Spring context is required;
- the application service does not need a Spring Data mock.

What it does not prove:

- `@Transactional` interception;
- JPA mapping;
- SQL;
- PostgreSQL constraints;
- generated IDs.

### 12. PostgreSQL test infrastructure

#### `src/test/java/com/example/bookcatalog/support/TestPostgresConfiguration.java`

~~~java
package com.example.bookcatalog.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestPostgresConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:17");
    }
}
~~~

`@ServiceConnection` publishes JDBC connection details to Boot. The normal hard-coded local URL, username, and password in `application.yaml` are not used for this test datasource.

#### `src/test/resources/application-test.yaml`

~~~yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
    validate-migration-naming: true
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate

logging:
  level:
    org.springframework.transaction: INFO
    org.springframework.orm.jpa.JpaTransactionManager: INFO
    org.hibernate.SQL: DEBUG
    org.testcontainers: INFO
~~~

There are deliberately no H2 settings and no copied test schema. Production Flyway migrations initialize PostgreSQL.

### 13. Persistence adapter integration test

#### `src/test/java/com/example/bookcatalog/book/adapter/out/persistence/BookPersistenceAdapterIntegrationTest.java`

~~~java
package com.example.bookcatalog.book.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TestPostgresConfiguration.class,
        BookPersistenceAdapter.class
})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@ActiveProfiles("test")
class BookPersistenceAdapterIntegrationTest {

    @Autowired
    private BookPersistenceAdapter adapter;

    @Autowired
    private BookRepository repository;

    @Test
    void savesBookUsingJpaAndPostgreSql() {
        Book saved = adapter.save(book(
                "978-0-11111-111-1",
                "Adapter Test"));

        assertNotNull(saved.getId());
        assertEquals(1, repository.count());
    }

    @Test
    void translatesDuplicateIsbnConstraintFailure() {
        adapter.save(book(
                "978-0-22222-222-2",
                "First Copy"));

        assertThrows(
                DuplicateIsbnException.class,
                () -> adapter.save(book(
                        "978-0-22222-222-2",
                        "Duplicate Copy")));
    }

    private static Book book(String isbn, String title) {
        return new Book(
                isbn,
                title,
                "Test Author",
                new BigDecimal("19.95"),
                3);
    }
}
~~~

Boot 4.1 package names matter:

| Type | Boot 4.1 import |
|---|---|
| `@DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` |
| `@AutoConfigureTestDatabase` | `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase` |
| `FlywayAutoConfiguration` | `org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration` |
| `PostgreSQLContainer` | `org.testcontainers.postgresql.PostgreSQLContainer` |

`@DataJpaTest` wraps each test in a transaction that is normally rolled back after the test. That cleanup behavior is separate from the production `CreateBookService` transaction. The duplicate test deliberately stops after the exception because PostgreSQL marks the failed transaction as unusable until rollback.

### 14. Files deliberately unchanged

#### Existing `BookRepository`

No change is required:

~~~java
package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.Book;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAuthor(String author, Pageable pageable);
}
~~~

This framework repository remains an outer persistence detail. The adapter may use it; the Create Book application service may not.

#### Existing `Book` entity

No change is required. This complete existing class is deliberately reused by the application and persistence adapter:

~~~java
package com.example.bookcatalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String isbn;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private BigDecimal price;

    @Column
    private Integer stock;

    @Version
    @Column
    private Long version;

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

    public void replace(
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

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }
}
~~~

The application therefore retains a known JPA annotation dependency. That is the exercise's explicit pragmatic compromise.

#### Existing security configuration

No change is required. The relevant rules remain conceptually:

~~~java
.requestMatchers(
        HttpMethod.GET,
        "/auth/csrf",
        "/books",
        "/books/**").permitAll()
.requestMatchers(
        HttpMethod.POST,
        "/auth/register",
        "/auth/login").permitAll()
.requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
.requestMatchers(
        "/books",
        "/books/**",
        "/transaction-lab/**").hasRole("ADMIN")
~~~

CSRF remains enabled. The security filter chain runs before `BookController`, so forbidden calls never reach the new input port.

#### Existing global exception handler

No change is required. Its `DuplicateIsbnException` handler still returns `409 Conflict`. Moving Spring's persistence exception translation into the adapter preserves this web mapping without making the use case aware of HTTP.

### 15. Expected SQL and transaction behavior

Exact SQL formatting and column order can vary by Hibernate version. With Hibernate ORM 7.4 and PostgreSQL, expect an INSERT with this shape:

~~~sql
insert into books
    (author, isbn, price, stock, title, version)
values
    (?, ?, ?, ?, ?, ?)
~~~

The important observation is the table and persisted columns, not whitespace, exact column order, or generated-key syntax. Hibernate/JDBC obtains the identity value using the mechanism selected by the PostgreSQL dialect and driver; do not hard-code a `RETURNING` clause in your expected log.

#### Successful Create Book

~~~text
1. SecurityFilterChain accepts ADMIN session and CSRF token
2. BookController validates BookRequest
3. controller calls CreateBookUseCase Spring proxy
4. TransactionInterceptor asks JpaTransactionManager to begin
5. EntityManager/persistence context is bound to the transaction
6. CreateBookService constructs a transient Book
7. service calls BookRepositoryPort.save
8. BookPersistenceAdapter calls Spring Data saveAndFlush
9. Book becomes managed; IDENTITY may force INSERT during save, and the explicit flush guarantees pending SQL has executed before the adapter returns
10. PostgreSQL accepts UNIQUE(isbn)
11. adapter returns Book with generated id
12. service returns normally
13. transaction manager commits
14. JDBC connection is eventually returned to HikariCP
15. controller returns 201 + Location + body
~~~

#### Duplicate ISBN

~~~text
1-8. same path
9. Hibernate sends INSERT during flush
10. PostgreSQL rejects UNIQUE(isbn)
11. Spring translates provider/JDBC failure to DataIntegrityViolationException
12. adapter translates it to DuplicateIsbnException
13. runtime exception escapes CreateBookService proxy
14. transaction manager rolls back
15. GlobalExceptionHandler maps DuplicateIsbnException to 409
16. no second ISBN row is committed
~~~

The controller calls a proxied service synchronously. The proxy commits or rolls back before returning control to the controller, so a successful `201` is built only after the transactional method completes normally.

#### Existing DELETE experiment remains unchanged

~~~text
load managed Book
 -> repository.delete
 -> flush sends DELETE SQL
 -> TransactionLabCheckedException is thrown
 -> rollbackFor matches checked exception
 -> PostgreSQL transaction rolls back
 -> HTTP 500
 -> row still exists
~~~

This is useful proof that SQL execution does not mean commit.

### 16. Build and test commands

From `Spring_Security/book-catalog-api`:

~~~powershell
mvn clean test
~~~

Expected test layers:

| Test | Spring context | PostgreSQL | Primary proof |
|---|---:|---:|---|
| `CreateBookServiceTest` | no | no | command mapping and output-port orchestration |
| `BookPersistenceAdapterIntegrationTest` | JPA slice | PostgreSQL 17 container | mapping, migrations, constraint, translation |

If Docker/Testcontainers is unavailable, the pure unit test can still be selected:

~~~powershell
mvn -DskipTests test
mvn -Dtest=CreateBookServiceTest test
~~~

The first command compiles main and test sources without running tests; the second executes the framework-free unit test. Neither is evidence that the PostgreSQL adapter test passed.

Do not report the persistence boundary as verified until the container test has actually run.

For a structural inspection:

~~~powershell
rg "org\.springframework\.data|com\.example\.bookcatalog\.repository|com\.example\.bookcatalog\.web|com\.example\.bookcatalog\.dto" src/main/java/com/example/bookcatalog/book/application
~~~

Expected output: nothing. `CreateBookService` may import `@Service` and `@Transactional` as the documented pragmatic choice.

### 17. Run the real application

Start PostgreSQL using the existing Compose file:

~~~powershell
docker compose up -d postgres
docker compose ps
~~~

Ensure the datasource values used by Spring match the database/user/password from your local `.env`. Do not commit real secrets. For the existing local administrator bootstrap:

~~~powershell
$env:SPRING_PROFILES_ACTIVE = "local"
$env:BOOK_ADMIN_USERNAME = "localadmin"
$env:BOOK_ADMIN_PASSWORD = "change-this-local-password"
mvn spring-boot:run
~~~

The password example is for local practice only. Use your own value and keep it outside source control.

On startup, verify logs in this order:

1. HikariCP starts;
2. Flyway validates/applies V1–V6;
3. Hibernate validates the schema;
4. the application listens on `127.0.0.1:8080`.

### 18. Session and CSRF workflow with PowerShell

Spring Security uses a login session and CSRF. HTTP Basic is disabled.

#### Obtain a pre-login token

~~~powershell
$csrfJson = curl.exe -s -c admin-cookies.txt http://127.0.0.1:8080/auth/csrf
$csrf = ($csrfJson | ConvertFrom-Json).token
~~~

The response supplies `headerName`, `parameterName`, and `token`. The default header is normally `X-CSRF-TOKEN`.

#### Log in as the bootstrapped administrator

~~~powershell
curl.exe -i -b admin-cookies.txt -c admin-cookies.txt -H "X-CSRF-TOKEN: $csrf" -H "Content-Type: application/x-www-form-urlencoded" --data-urlencode "username=localadmin" --data-urlencode "password=change-this-local-password" -X POST http://127.0.0.1:8080/auth/login
~~~

Expected: `204 No Content` and a session cookie.

Fetch a **fresh token after login**, because authentication can rotate or clear the previous token:

~~~powershell
$csrfJson = curl.exe -s -b admin-cookies.txt -c admin-cookies.txt http://127.0.0.1:8080/auth/csrf
$csrf = ($csrfJson | ConvertFrom-Json).token
~~~

#### Create a book

~~~powershell
$body = @{
  isbn = "978-0-12345-678-9"
  title = "Ports in Practice"
  author = "A. Student"
  price = 29.95
  stock = 8
} | ConvertTo-Json

curl.exe -i -b admin-cookies.txt -H "X-CSRF-TOKEN: $csrf" -H "Content-Type: application/json" --data $body -X POST http://127.0.0.1:8080/books
~~~

Expected response shape:

~~~http
HTTP/1.1 201 Created
Location: /books/42
Content-Type: application/json

{
  "id": 42,
  "isbn": "978-0-12345-678-9",
  "title": "Ports in Practice",
  "author": "A. Student",
  "price": 29.95,
  "stock": 8
}
~~~

The actual ID depends on current database state.

#### Repeat the same ISBN

Repeat the POST with the same ISBN and a valid token/session.

Expected:

~~~http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json
~~~

The Problem Detail title remains `ISBN conflict` and the detail remains `Isbn is duplicated`.

#### Verify missing CSRF

~~~powershell
curl.exe -i -b admin-cookies.txt -H "Content-Type: application/json" --data $body -X POST http://127.0.0.1:8080/books
~~~

Expected: `403 Forbidden` before the controller.

#### Verify a normal USER cannot write

Use a separate cookie jar:

1. GET `/auth/csrf`;
2. POST `/auth/register` with a valid username and 12–72-character password plus that token;
3. POST `/auth/login`;
4. fetch a fresh token;
5. POST `/books`.

Expected: `403 Forbidden` even with valid authentication and CSRF, because the URL requires `ROLE_ADMIN`.

### 19. PostgreSQL verification

Use pgAdmin or `psql`:

~~~sql
SELECT id, isbn, title, author, price, stock, version
FROM books
ORDER BY id;
~~~

After one valid request and one duplicate request, expect exactly one row with `isbn = '978-0-12345-678-9'`.

Check migration ownership:

~~~sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
~~~

Expect V1 through V6 to be successful. A restored source migration does not rerun when Flyway history already records it; it is validated.

To verify the existing DELETE rollback:

~~~sql
SELECT id, isbn
FROM books
WHERE isbn = '978-0-12345-678-9';
~~~

Use the ID returned by that query (or by the POST response) in the ADMIN request `DELETE /books/{actual-id}`, with a valid token. Expect `500`. Run the ISBN query again; the row should remain. Do not copy an illustrative ID such as `42` unless that is the ID your database actually returned.

### 20. Log-to-code correlation

The existing development logging includes:

~~~yaml
logging:
  level:
    org.springframework.transaction: TRACE
    org.springframework.orm.jpa.JpaTransactionManager: DEBUG
    org.hibernate.SQL: DEBUG
~~~

Use it to build an evidence table:

| Observation | Meaning |
|---|---|
| transaction trace before application method | Spring proxy intercepted the use-case call |
| Hibernate INSERT | flush reached the database |
| commit log after method return | durable success path |
| rollback log after translated exception | failed unit of work was undone |
| no controller breakpoint on missing CSRF | request was rejected in the filter chain |

SQL logs do not inline bound values by default and do not by themselves prove commit. Avoid TRACE-level transaction logging in routine production operation; it is intentionally noisy.

### 21. Before/after dependency and cost comparison

#### Before

~~~text
BookController
    |
    v
BookService
    |
    | imports concrete Spring Data interface
    v
BookRepository extends JpaRepository
~~~

#### After for Create Book

~~~text
BookController
    |
    | imports application input port
    v
CreateBookUseCase  <---implemented by---  CreateBookService
                                             |
                                             | imports application output port
                                             v
                                      BookRepositoryPort
                                             ^
                                             | implemented by
                                  BookPersistenceAdapter
                                             |
                                             v
                                       BookRepository
~~~

| Cost/quality | Before | After |
|---|---|---|
| classes for Create Book | fewer | five small boundary/application classes |
| use-case dependency on Spring Data | direct | removed |
| controller dependency | concrete legacy service | input-port contract |
| pure use-case test | requires mocking concrete dependency or using repository type | simple fake output port |
| persistence exception location | service | outbound adapter |
| transaction boundary | service | application use-case service |
| mappings | controller passes five scalars | controller maps request → command; same `Book` is deliberately reused, so no domain/JPA mapper |
| ownership clarity | conventions imply responsibilities | ports and adapters make the selected boundary explicit |
| persistence change blast radius | use case imports Spring Data directly | Create Book orchestration is insulated; adapter/schema/tests still change |
| future adapter flexibility | new caller/store touches concrete dependencies | new driving/driven adapter can target an existing port where semantics match |
| JPA coupling in `Book` | present | still present |
| REST contract | existing | unchanged |
| cognitive overhead and navigation | low | moderately higher: command, two ports, service, adapter, tests |

The result is not “more correct” merely because it has more interfaces. It is useful when independent testing, policy clarity, change isolation, or multiple adapters repay the extra navigation.

### 22. What “switching databases” really costs

The output port isolates the use case from Spring Data calls, but it does not make a database switch free:

~~~text
isolated:
  CreateBookService orchestration
  input-port API
  narrow save requirement

still coupled:
  Book has JPA annotations
  Flyway SQL uses PostgreSQL syntax and behavior
  identity generation strategy
  unique-constraint exception details
  integration tests/container
  datasource/driver configuration
~~~

A new adapter could implement `BookRepositoryPort`, but replacing PostgreSQL still requires schema, mapping, operational, and test work. Clean Architecture localizes costs; it does not erase them.

PostgreSQL → another relational database may preserve the basic `save` semantic while changing SQL dialect, migrations, identity, locking, constraint translation, and tests. PostgreSQL → MongoDB is a larger semantic change: document/aggregate shape, joins, transaction guarantees, identifiers, indexes, queries, and possibly the port itself may change. “There is an interface” is not evidence that either migration is cheap.

### 23. Troubleshooting guide

#### “No qualifying bean of type BookRepositoryPort”

Check:

- `BookPersistenceAdapter` has `@Component`;
- it implements the exact `BookRepositoryPort` type;
- its package is below `com.example.bookcatalog` so component scanning reaches it;
- you did not instantiate `CreateBookService` as an application bean manually with a missing dependency.

This is a **runtime composition-boundary** failure: the application contract exists, but Spring has no outbound implementation to inject.

#### Spring cannot choose an adapter implementation

`NoUniqueBeanDefinitionException` means two beans implement `BookRepositoryPort`. Decide whether there should really be two active persistence adapters. If both are intentional, select one at the outer composition root with `@Primary` or an explicit `@Qualifier`; do not make the use case import concrete adapters. The failed boundary is composition, not domain logic.

#### Component scanning misses the adapter

`Application` sits in `com.example.bookcatalog`, so the selected adapter package below that root is scanned. If you place an adapter outside the root, either move it beneath the root or explicitly import/scan it in configuration. Adding repository calls to the use case does not solve this wiring failure.

#### `JpaRepository` leaked into the application package

Run:

~~~powershell
rg "org\.springframework\.data|JpaRepository|Pageable" `
  src/main/java/com/example/bookcatalog/book/application
~~~

A match in `CreateBookService` or an application port means the **source dependency boundary** failed. Keep Spring Data on the persistence-adapter side; do not rename a `JpaRepository` extension to “Port.”

#### The application depends on a web DTO

If `CreateBookUseCase` accepts `BookRequest`, JSON and Jakarta request-validation choices leak inward. Map `BookRequest` to `CreateBookCommand` in `BookController`. The failed boundary is HTTP-to-application translation.

#### The domain/application returns `ResponseEntity`

`ResponseEntity`, HTTP status, and headers belong to the inbound web adapter. Return an application result/model, then build `201`, `Location`, and `BookResponse` in the controller. A `ResponseEntity` import in an input port reveals the leak immediately.

#### A circular dependency appears

Typical symptom: the controller, service, and adapter require one another during bean creation. Draw source arrows. The controller may know the input port; the service may know the output port; the persistence adapter may know the output port and Spring Data repository. The service must not call the controller, and the adapter must not call the service. `@Lazy` can hide the cycle without correcting ownership.

#### A mapper loses data, or `Book` and `BookJpaEntity` diverge

The selected solution avoids a second persistence model. If you chose the strict two-model experiment, write round-trip mapping tests for every field, including ID/version behavior, and keep mapping in the persistence adapter. A null/missing field is an **adapter translation** failure, not a reason for the domain to import JPA.

#### “No tests were executed” or Jupiter tests are ignored

Remove JUnit 4.11 and the old plugin-management block. Use the parent-managed Boot 4.1.1 test dependencies and current Surefire plugin.

#### `@DataJpaTest` or `@AutoConfigureTestDatabase` import cannot be found

Boot 4 modularized the test support. Verify:

~~~text
spring-boot-starter-data-jpa-test
org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
~~~

Do not copy Boot 3 package imports from an older tutorial.

#### Test tries an embedded database or reports no embedded database

Ensure:

~~~java
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
~~~

and import the `TestPostgresConfiguration` carrying `@ServiceConnection`. Do not add H2 as a workaround.

#### Testcontainers cannot find Docker

Start a compatible Docker runtime and verify:

~~~powershell
docker info
docker version
~~~

The unit test can run without it; the adapter integration test cannot honestly pass without a real PostgreSQL target.

#### Flyway reports “No migrations found”

Confirm the files are under:

~~~text
src/main/resources/db/migration
~~~

not only `target/classes/db/migration`. Then run `mvn clean test`.

#### Flyway checksum mismatch

Do not edit an applied V1–V6 to silence it. Restore the exact original content. For a legitimate future change, create V7.

#### Hibernate reports missing table or column

Read startup order:

1. did the Testcontainer/local database start?
2. did Flyway find and apply V1–V6?
3. is Hibernate validating the same datasource?
4. does the schema contain `books.version`?

Do not change `ddl-auto` to `update`; that would create two competing schema owners.

#### The persistence-adapter test fails schema validation

Read the failure in boundary order: did `@ServiceConnection` supply the container datasource, did Flyway apply the restored source migrations, and only then did Hibernate validate the same schema? Compare entity columns/types with V1–V6. Do not make the test pass by disabling validation or adding H2; that would remove the behavior the adapter test exists to verify.

#### Duplicate ISBN returns `500` instead of `409`

Check:

- adapter catches `DataIntegrityViolationException`;
- it throws `DuplicateIsbnException`;
- `GlobalExceptionHandler` still has its `409` handler;
- `saveAndFlush` surfaces the constraint inside the adapter.

If you used only `save`, the failure may occur during proxy commit after the adapter's `try` block has ended.

#### Create Book is not transactional

Check that:

- `CreateBookService` is a Spring bean;
- `@Transactional` is on its public `create` method;
- the controller calls the injected `CreateBookUseCase`;
- no code constructs the production service with `new`;
- the call is not a same-object self-invocation.

#### `@Transactional` or `@PreAuthorize` stops applying after a move

Both are normally proxy-intercepted. Verify that the class is a Spring bean, the invoked method is public, method security remains enabled for `@PreAuthorize`, and an external bean calls the injected proxy. Moving code into a private helper or constructing it with `new` bypasses the framework boundary.

The selected Create Book slice keeps URL-based ADMIN authorization and deliberately does not add `@PreAuthorize`. Existing `BookService.setStock` retains method security unchanged.

#### Self-invocation bypasses a proxy

`this.inner()` does not leave and re-enter the Spring proxy. Put the transaction/security annotation on the externally invoked use-case method or extract a genuinely separate collaborator. Do not inject “self” merely to force proxy traversal.

#### A unit test unexpectedly starts Spring

`CreateBookServiceTest` should have no `@SpringBootTest`, `@ExtendWith(SpringExtension.class)`, or autowired fields. Construct the service and fake port directly. If the unit test loads an application context, the pure application test boundary has been lost; reserve Spring for adapter/wiring tests.

#### POST returns `403` before your controller breakpoint

This is normally security, not architecture:

- missing/expired CSRF token;
- stale token after login;
- missing session cookie;
- USER rather than ADMIN.

Fetch a fresh token using the same cookie jar after login.

#### POST returns `400`

Inspect `BookRequest` constraints and JSON types. The current request requires:

- nonblank bounded text;
- non-null, nonnegative price within decimal maximum;
- non-null, nonnegative integer stock;
- no unknown JSON fields because Jackson rejects them.

#### Book is inserted but ID/response looks wrong

Keep the call inside the use-case transaction and return the object from `saveAndFlush`. With IDENTITY generation, the generated ID is available after insertion.

#### SQL INSERT is logged but no row exists

SQL logging proves flush, not commit. Look for a later rollback, especially in the duplicate path, test transaction cleanup, or the deliberate DELETE experiment.

#### Pool pressure appears during tests or local use

The use case needs one transaction and normally one JDBC connection. Do not add `REQUIRES_NEW` to adapters. Long transactions, blocked SQL, or leaked/parallel test containers should be investigated before increasing Hikari's pool size.

### 24. Verification coverage matrix

| Requirement | Unit test | Adapter test | Manual HTTP | PostgreSQL/logs |
|---|---:|---:|---:|---:|
| command values reach `Book` | yes | indirectly | yes | row |
| use case calls output port | yes | no | indirectly | no |
| JPA mapping works | no | yes | yes | SQL/row |
| V1–V6 are usable | no | yes | startup | Flyway history |
| duplicate constraint translates | no | yes | yes | unique failure |
| transaction commits | no | test transaction differs | yes | commit + row |
| transaction rolls back | no | test cleanup/failed tx | DELETE/duplicate | rollback + state |
| ADMIN authorization | no | no | yes | no |
| CSRF enforcement | no | no | yes | filter behavior |
| `201`/`Location`/JSON unchanged | no | no | yes | no |
| dependency direction | imports/fake | wiring | runtime | no |

No single test proves everything. The table prevents overclaiming.

### 25. Task answer key

1. Restore V1–V6 under source because `target` is disposable.
2. Current control flows controller → service → repository → JPA → JDBC → pool → database.
3. Freeze the existing REST, validation, security, error, and transaction behavior.
4. Assign transport, application, adapter, and database responsibilities by reason to change.
5. Application owns both ports; adapters depend inward on them.
6. Use an application command so the input boundary does not accept a web DTO.
7. `CreateBookUseCase` exposes one application capability.
8. `BookRepositoryPort` exposes only `save`, not the framework repository surface.
9. `CreateBookService` constructs the model, calls the port, and owns the transaction.
10. `BookPersistenceAdapter` invokes Spring Data and translates its exception.
11. Only `POST /books` is rewired; transport mapping stays in the controller.
12. Remove the old `BookService.create` to prevent two owners.
13. Spring proxy/transaction manager provide runtime behavior around the input-port implementation.
14. Keep one JPA-backed `Book` and state the coupling honestly.
15. Validation/security/transactions/constraints/error mapping remain at their appropriate boundaries.
16. Test application orchestration with a handwritten port fake.
17. Test persistence with Boot's JPA slice, Flyway, Testcontainers, and PostgreSQL 17.
18. Combine static dependency, automated, HTTP, logging, and database-state verification.

### 26. Completion checklist

#### Understanding

- [ ] I can explain the Dependency Rule using Java import arrows.
- [ ] I can distinguish a use case from a controller.
- [ ] I can identify input ports, output ports, inbound adapters, and outbound adapters.
- [ ] I can explain why the application owns `BookRepositoryPort`.
- [ ] I understand why each new interface exists and which abstractions were intentionally not added.
- [ ] I can compare strict and pragmatic Clean Architecture.
- [ ] I can explain when this design would be overengineering.

#### Source and build

- [ ] V1–V6 exist under `src/main/resources/db/migration`.
- [ ] `mvn clean` no longer removes the only migration copies.
- [ ] JUnit 4 placeholder/dependency is gone.
- [ ] Boot 4.1.1 manages the test toolchain.
- [ ] `mvn clean test` passes with a Docker-compatible runtime.

#### Architecture

- [ ] Controller maps `BookRequest` to `CreateBookCommand`.
- [ ] Controller depends on `CreateBookUseCase` for POST only.
- [ ] `CreateBookService` depends on `BookRepositoryPort`.
- [ ] `BookPersistenceAdapter` implements the output port.
- [ ] The application package does not import web DTOs or Spring Data repositories.
- [ ] HTTP concerns remain near the inbound adapter.
- [ ] Persistence/PostgreSQL details remain near the outbound adapter unless deliberately shared.
- [ ] The remaining JPA `Book` coupling is documented.
- [ ] No unrelated slice was moved.

#### Behavior

- [ ] public reads remain public.
- [ ] missing CSRF still yields `403`.
- [ ] USER write still yields `403`.
- [ ] ADMIN create yields `201` and `Location`.
- [ ] invalid create yields `400`.
- [ ] duplicate ISBN yields `409` and no duplicate row.
- [ ] deliberate DELETE yields `500` and the row remains.
- [ ] request/response fields are unchanged.

#### Transactions and persistence

- [ ] `@Transactional` is at the public use-case boundary.
- [ ] adapter joins rather than starts an independent transaction.
- [ ] `saveAndFlush` is understood as error timing, not commit.
- [ ] one successful create produces one durable row.
- [ ] PostgreSQL remains the uniqueness authority.
- [ ] connections are not held across external calls.

### 27. Reflection questions

1. What source dependency was reversed?
2. Why is the controller an inbound adapter rather than the application?
3. Why does the output port live inside the application side?
4. What would break if `CreateBookUseCase` returned `ResponseEntity`?
5. Why is `JpaRepository` too broad as this use case's port?
6. Why does `saveAndFlush` help exception translation without committing?
7. Which behavior is protected by PostgreSQL rather than Java?
8. Why is a pure unit test insufficient for the adapter?
9. Why is H2 specifically a poor substitute in this exercise?
10. Which JPA coupling remains after the refactor?
11. What new costs did the port introduce?
12. At what project size/complexity might those costs be unjustified?
13. If a CLI invoked Create Book, which classes could it reuse?
14. If an HTTP DTO gained a presentation-only field, should the command automatically gain it?
15. Where would an external ISBN-catalog API port live?
16. Why should a use case avoid holding the transaction open during a slow HTTP call?
17. What would justify splitting `Book` and `BookJpaEntity` later?
18. How would you migrate the next slice without a “big bang” rewrite?

<details>
<summary>Reflection guidance</summary>

Good answers emphasize policy ownership, inward source dependencies, explicit boundary translation, PostgreSQL as a real adapter concern, and measured trade-offs. Avoid absolute claims such as “the domain can now switch databases with no work” or “every service always needs an interface.”

</details>

### 28. Final mental model

Use this sentence:

> The controller translates HTTP into an application command; a Spring-proxied use case owns the transaction and calls an application-owned persistence port; an outbound adapter implements that port with Spring Data/Hibernate/JDBC/HikariCP/PostgreSQL; errors cross boundaries as progressively more stable meanings.

Use this final end-to-end diagram:

~~~text
                          INWARD SOURCE DEPENDENCIES
         +-------------------------------------------------------+
         |                                                       |
         v                                                       |
+-------------------+      +-------------------------------+      |
| Security filters  |      | Application                   |      |
| session + CSRF    |      |                               |      |
+---------+---------+      | CreateBookUseCase  <------+   |      |
          |                |       ^                    |   |      |
          v                |       | implements         |   |      |
+-------------------+      | CreateBookService          |   |      |
| BookController    +----->|  @Transactional            |   |      |
| inbound adapter   |      |  (Spring transaction proxy|   |      |
| @Valid request    |      |   opens/commits/rolls back)|   |      |
| Request -> Command|      |       |                    |   |      |
| Book -> Response  |      |       v                    |   |      |
+-------------------+      | BookRepositoryPort <-------+---+------+
                           +----------+--------------------+
                                      ^
                                      | implements
                           +----------+--------------------+
                           | BookPersistenceAdapter        |
                           | saveAndFlush + translation    |
                           +----------+--------------------+
                                      |
                                      v
                           +-------------------------------+
                           | Spring Data repository proxy  |
                           | -> EntityManager              |
                           | -> persistence context        |
                           | -> Hibernate SQL/dirty state  |
                           | -> JDBC Connection            |
                           | -> HikariCP                    |
                           | -> PostgreSQL constraints     |
                           |    schema from Flyway V1-V6   |
                           +-------------------------------+

CROSS-CUTTING / BOUNDARY PLACEMENT
Security          before MVC: session, CSRF, URL roles
Validation        web shape in BookRequest; business rules inward;
                  final integrity constraints in PostgreSQL
Exceptions        adapter: persistence -> application meaning;
                  GlobalExceptionHandler: application meaning -> HTTP
Transactions      Spring proxy around the public use-case method
Flyway            outbound infrastructure; source-controlled schema history
Testing           pure use-case test -> service + fake port
                  web/security test -> inbound boundary
                  PostgreSQL adapter test -> Flyway/JPA/real database

SUCCESS
filter -> controller -> transaction begin -> use case -> port -> adapter
       -> flush/INSERT -> commit -> connection returned -> 201

DUPLICATE
filter -> controller -> transaction begin -> use case -> port -> adapter
       -> flush/unique violation -> translated exception -> rollback -> 409
~~~







