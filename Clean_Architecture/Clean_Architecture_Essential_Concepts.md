# Clean Architecture — Essential Concepts

> A first-principles guide to deciding **why a dependency exists, who should own it, and which direction it should point** in a real Java 21/Spring Boot application.

**Inspected baseline:** `Spring_Security/book-catalog-api`  
**Companion exercise:** [Clean Architecture Mini Project](Clean_Architecture_Mini_Project.md)  
**Learning goal:** reason about boundaries and trade-offs—not memorize `domain/`, `application/`, and `adapter/` folder names.

The current Book Catalog is a recognizable layered application:

```text
HTTP
  ↓
Spring Security Filter Chain
  ↓
BookController
  ↓
BookService
  ↓
BookRepository extends JpaRepository
  ↓
Hibernate → JDBC → HikariCP → PostgreSQL
```

That is **not automatically bad architecture**. It is compact, familiar, and often the right design for a small CRUD application. Clean Architecture becomes useful when selected business decisions should remain understandable and testable without being tied directly to HTTP, Spring Data, or another outer technology.

The companion exercise makes one controlled change:

```text
POST /books
  ↓
BookController                         inbound adapter
  ↓
CreateBookUseCase                     input port
  ↓
CreateBookService                     application use case
  ↓
BookRepositoryPort                    output port
  ↑ implemented by
BookPersistenceAdapter                outbound adapter
  ↓
existing BookRepository               Spring Data infrastructure
  ↓
Hibernate → HikariCP → PostgreSQL
```

Only **Create Book** is refactored. Reads, replacement, stock changes, deletion, registration, authentication, and the transaction lab remain recognizable. This makes the benefit and the extra ceremony measurable.

## Verified project baseline

The versions below come from the current POM and Maven dependency tree, not from an older lesson:

| Component | Actual version |
|---|---:|
| Java source target / inspected runtime | 21 / Temurin 21.0.12 |
| Spring Boot | 4.1.1 |
| Spring Framework | 7.0.9 |
| Spring Data JPA | 4.1.1 |
| Hibernate ORM | 7.4.5.Final |
| Spring Security | 7.1.1 |
| Flyway | 12.4.0 |
| PostgreSQL JDBC | 42.7.13 |
| HikariCP | 7.0.2 |
| PostgreSQL image | `postgres:17` |
| Current source-test dependency | JUnit 4.11 only |

The authoritative source currently contains only the generated-style `AppTest`. Stale test classes and V1–V6 migrations under `target/` are build artifacts, not source. The companion explicitly restores the missing migration sources before any clean build and introduces only the test support needed to prove the new boundaries.

## Connections to what you already know

| Previous knowledge | Clean Architecture interpretation |
|---|---|
| OOP interface | A port when it deliberately defines a boundary |
| SOLID Dependency Inversion | Inner policy owns an abstraction; outer detail implements it |
| Spring IoC/DI | Runtime composition of ports and adapters |
| `@RestController` | Inbound/driving HTTP adapter |
| Service method | Often a use-case implementation |
| `JpaRepository` | Spring Data persistence infrastructure |
| Persistence context and dirty checking | Behavior inside a persistence adapter/use-case transaction |
| `@Transactional` | Usually a use-case boundary in pragmatic Spring code |
| `SecurityFilterChain` | Outer HTTP authentication/authorization boundary |
| `@PreAuthorize` | Proxy-based method authorization near an application boundary |
| Flyway | Infrastructure-owned schema evolution |
| JUnit/Mockito | Exercise inner policy through ports without the outer world |

## How to study

1. Read the concept.
2. Predict where each real Book Catalog class belongs.
3. Draw compile-time dependency arrows; do not draw only runtime calls.
4. Ask who owns every interface.
5. Decide whether each dependency points inward or outward.
6. Answer the checkpoint before opening its answer.
7. Compare the layered and port-based versions.
8. Apply only the corresponding step in the companion exercise.
9. Explain what each abstraction costs and which change it protects.
10. Delete an abstraction mentally: if nothing important becomes worse, it may be ceremony.

## Priorities

| Priority | Meaning | You should be able to… |
|---|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Daily architectural reasoning | recognize the boundary, explain ownership, design the arrow, implement it, and debug wrong wiring |
| ⭐⭐⭐⭐ IMPORTANT | Common production decision | explain trade-offs, implement a pragmatic choice, and diagnose leaks |
| ⭐⭐⭐ NICE TO KNOW | Useful when complexity grows | recognize when the technique pays for itself and avoid premature use |
| ⭐⭐ FUTURE KNOWLEDGE | Broader architectural enforcement | describe the purpose and defer tooling/modularity until justified |

Topic priorities for this guide:

| Practice area | Priority |
|---|---|
| Dependency Rule and Dependency Inversion | ⭐⭐⭐⭐⭐ MUST KNOW |
| Use-case boundaries and input/output ports | ⭐⭐⭐⭐⭐ MUST KNOW |
| Inbound/outbound adapters and ownership | ⭐⭐⭐⭐⭐ MUST KNOW |
| Spring dependency injection and proxy behavior | ⭐⭐⭐⭐ IMPORTANT |
| Transaction, validation, security, and error boundaries | ⭐⭐⭐⭐ IMPORTANT |
| Testing through ports and real persistence adapters | ⭐⭐⭐⭐ IMPORTANT |
| Same JPA model versus separate domain/persistence models | ⭐⭐⭐⭐ IMPORTANT |
| Strict framework independence and automated architecture rules | ⭐⭐⭐ NICE TO KNOW |

## Navigation

1. [Why Clean Architecture exists](#1-why-clean-architecture-exists)
2. [The Dependency Rule](#2-the-dependency-rule)
3. [Responsibility before folder names](#3-responsibility-before-folder-names)
4. [Dependency Inversion](#4-dependency-inversion)
5. [The domain layer](#5-the-domain-layer)
6. [Application services and use cases](#6-application-services-and-use-cases)
7. [Input ports](#7-input-ports)
8. [Output ports](#8-output-ports)
9. [Inbound or driving adapters](#9-inbound-or-driving-adapters)
10. [Outbound or driven adapters](#10-outbound-or-driven-adapters)
11. [Spring Boot's role](#11-spring-boots-role)
12. [Ownership of the major components](#12-ownership-of-the-major-components)
13. [HTTP DTOs and application commands](#13-http-dtos-and-application-commands)
14. [Mapping boundaries](#14-mapping-boundaries)
15. [The persistence adapter](#15-the-persistence-adapter)
16. [Transaction boundaries](#16-transaction-boundaries)
17. [Validation at three boundaries](#17-validation-at-three-boundaries)
18. [Exception flow](#18-exception-flow)
19. [Spring Security](#19-spring-security)
20. [Flyway and PostgreSQL](#20-flyway-and-postgresql)
21. [Testing through boundaries](#21-testing-through-boundaries)
22. [Package organization](#22-package-organization)
23. [Dependency matrix](#23-dependency-matrix)
24. [Layered versus Clean Architecture](#24-layered-versus-clean-architecture)
25. [Clean, Hexagonal, Ports and Adapters, and Onion](#25-clean-hexagonal-ports-and-adapters-and-onion)
26. [Clean Architecture and DDD](#26-clean-architecture-and-ddd)
27. [Strict versus pragmatic Spring architecture](#27-strict-versus-pragmatic-spring-architecture)
28. [Switching infrastructure](#28-switching-infrastructure)
29. [External services](#29-external-services)
30. [When not to use full Clean Architecture](#30-when-not-to-use-full-clean-architecture)
31. [Common mistakes](#31-common-mistakes)
32. [Trace Create Book completely](#32-trace-create-book-completely)
33. [Readiness and interview review](#33-readiness-and-interview-review)

---

## 1. Why Clean Architecture exists

**⭐⭐⭐⭐⭐ MUST KNOW**

The familiar structure is often excellent:

```text
Controller → Service → Repository → Database
```

Use it confidently when:

- behavior is mostly straightforward CRUD;
- one delivery mechanism and one database are expected;
- tests can cover the important behavior cheaply;
- framework coupling is understood and acceptable;
- the team gains more from fewer classes than from stronger isolation.

The pressure appears when an application service starts importing details from every direction:

```java
package com.example.bookcatalog.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
```

Now business orchestration can know about paging infrastructure, persistence exceptions, thread-associated security, and HTTP status translation. Changing or testing one decision may require much of Spring and the database.

Ask about the real `BookRepository`:

```java
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthor(String author, Pageable pageable);
}
```

- It is a developer-declared Spring Data interface.
- Spring creates its runtime proxy implementation.
- It exposes far more operations than Create Book requires.
- A service importing it depends on Spring Data JPA at compile time.
- That coupling may be entirely acceptable for most of this small project.

Clean Architecture does not remove frameworks. It makes selected policy depend on abstractions it owns, while outer code adapts frameworks to those abstractions.

```text
Goal: protect valuable decisions from volatile details.
Not a goal: pretend frameworks, SQL, and databases do not matter.
```

### Checkpoint 1

If a five-endpoint internal CRUD tool is easy to test and will always use Spring Data JPA, must it introduce input and output ports?

<details>
<summary>Answer</summary>

No. The boundary must repay its cost. A conventional service can be clearer. Introduce a port when it improves dependency direction, isolates a meaningful policy, supports a useful test seam, or allows genuinely different adapters—not merely because a diagram contains one.

</details>

---

## 2. The Dependency Rule

**⭐⭐⭐⭐⭐ MUST KNOW**

> Outer mechanisms may depend on inner policy. Inner policy should not depend on outer implementation details.

```text
┌────────────────────────────────────────────┐
│ Frameworks / infrastructure                │
│ Spring MVC, Spring Data, Hibernate, DB     │
│   ┌────────────────────────────────────┐   │
│   │ Interface adapters                 │   │
│   │ web adapter / persistence adapter  │   │
│   │   ┌────────────────────────────┐   │   │
│   │   │ Application / use cases    │   │   │
│   │   │ ports and orchestration    │   │   │
│   │   │   ┌────────────────────┐   │   │   │
│   │   │   │ Domain policy      │   │   │   │
│   │   │   └────────────────────┘   │   │   │
│   │   └────────────────────────────┘   │   │
│   └────────────────────────────────────┘   │
└────────────────────────────────────────────┘
                 dependencies point inward
```

The rule is visible in imports. This application-layer interface points outward and is therefore the wrong abstraction:

```java
package com.example.bookcatalog.book.application;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepositoryPort extends JpaRepository<Book, Long> {
}
```

The application now owns a type that inherits Spring Data's entire contract. Calling it a “port” did not invert anything.

A narrow inner-owned interface is different:

```java
package com.example.bookcatalog.book.application.port.out;

import com.example.bookcatalog.model.Book;

public interface BookRepositoryPort {
    Book save(Book book);
}
```

The outer persistence adapter imports and implements this interface. The application service imports it but does not import `JpaRepository`.

Runtime calls still move outward:

```text
CreateBookService → BookRepositoryPort → BookPersistenceAdapter → JpaRepository
```

Compile-time ownership is inverted:

```text
application defines BookRepositoryPort
adapter imports BookRepositoryPort and implements it
```

Do not confuse runtime call direction with source dependency direction.

### Checkpoint 2

`BookPersistenceAdapter` is physically outside the application package. How can `CreateBookService` call it without depending on it?

<details>
<summary>Answer</summary>

The service knows only `BookRepositoryPort`. Spring injects an object whose runtime class is `BookPersistenceAdapter`. The service's compile-time dependency points to its own port; the adapter's compile-time dependency points inward to that same port.

</details>

---

## 3. Responsibility before folder names

**⭐⭐⭐⭐⭐ MUST KNOW**

A folder name proves nothing. A package named `domain` can still import `ResponseEntity`, `JpaRepository`, and `SecurityContextHolder`.

Start with responsibility:

| Area | Owns | May depend on | Should not own |
|---|---|---|---|
| Domain | business state and invariants | Java/domain concepts | HTTP, SQL, Spring Data queries |
| Application | use-case orchestration and its ports | domain + owned port contracts | controllers, database implementations |
| Inbound adapter | translate an external request to an input port | application API + MVC/security binding | persistence implementation |
| Outbound adapter | translate an output port to a technology | application port + JPA/HTTP client SDK | business-use-case policy |
| Configuration/runtime | discover and connect implementations | all required bean types | business rules |

For every class ask:

```text
What decision does it make?
Who calls it?
What does it call next?
What imports are necessary?
Who should own the abstraction?
What becomes harder if this dependency stays?
```

Spring is allowed in an outer adapter. In a pragmatic application service, selected Spring annotations may also be an intentional trade-off. JPA is allowed in a persistence adapter. Whether JPA annotations remain on the shared `Book` model is a separate model-coupling decision.

---

## 4. Dependency Inversion

**⭐⭐⭐⭐⭐ MUST KNOW**

Dependency Inversion says high-level policy should not depend directly on low-level details; both meet through an abstraction suited to the policy. It is not merely “use an interface.”

Before:

```text
Create policy ──imports──> Spring Data BookRepository
```

After:

```text
Create policy ──imports──> BookRepositoryPort
                                  ▲
                                  │ implements/imports
                         BookPersistenceAdapter
                                  │
                                  ▼
                         Spring Data repository
```

The inner side owns `BookRepositoryPort` because it states what the use case needs. If infrastructure owned the interface, inner policy would still be shaped by an outer component's API.

Useful port:

```java
public interface BookRepositoryPort {
    Book save(Book book);
}
```

Ceremonial port:

```java
public interface BookRepositoryPort {
    Page<Book> findAll(Pageable pageable);
    Book saveAndFlush(Book entity);
    void deleteAllInBatch(Iterable<Book> entities);
    EntityManager entityManager();
}
```

The second contract copies infrastructure concepts inward and does not protect the use case.

### Checkpoint 3

Is every Java interface an architectural port?

<details>
<summary>Answer</summary>

No. A port expresses a deliberate application boundary. An interface that merely mirrors a concrete class, leaks outer types, has one trivial caller with no isolation benefit, or exists only to satisfy a naming convention may be ordinary indirection or ceremony.

</details>

---

## 5. The domain layer

**⭐⭐⭐⭐⭐ MUST KNOW**

Domain code represents business meaning: a Book, ISBN, price, stock, and rules such as whether a stock transition is valid. A rich domain is not required; CRUD-oriented domains can be intentionally modest.

### Approach A — one `Book` class for domain and JPA

The current project does this:

```java
package com.example.bookcatalog.model;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String isbn;
    private String title;
    private String author;
    private BigDecimal price;
    private Integer stock;

    protected Book() {
    }

    public Book(String isbn, String title, String author,
                BigDecimal price, Integer stock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
}
```

Benefits:

- one representation;
- no persistence mapper;
- Hibernate dirty checking continues naturally;
- minimal change to existing services and DTO mapping.

Costs:

- domain/application code compiles against a JPA-annotated type;
- constructor/no-arg and persistence mapping needs influence the model;
- replacing JPA may affect the model itself.

This is the companion's deliberate pragmatic choice.

### Approach B — separate domain and persistence models

```java
package com.example.bookcatalog.book.domain;

import java.math.BigDecimal;

public record Book(Long id, String isbn, String title,
                   String author, BigDecimal price, int stock,
                   long version) {
}
```

```java
package com.example.bookcatalog.book.adapter.out.persistence;

@Entity
@Table(name = "books")
class BookJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private BigDecimal price;
    private Integer stock;
    @Version
    private Long version;

    protected BookJpaEntity() {
    }
}
```

Benefits: the domain has no JPA imports, persistence shape can evolve separately, and mapping is explicit. Costs: another class, two-way mapping, duplicated fields, version/ID synchronization, and more tests. For this small first slice, those costs exceed the demonstrated benefit.

The choice is not “clean versus wrong.” It is **which coupling is worth paying to remove now**.

---

## 6. Application services and use cases

**⭐⭐⭐⭐⭐ MUST KNOW**

A use case is an application capability with a meaningful boundary: Create Book, Transfer Stock, Register User, or Change Password. An application service coordinates domain objects and output ports to complete it.

`BookService` currently groups several use cases. That is reasonable while they remain cohesive and small. A separate class per one-line CRUD method can obscure rather than clarify.

Create Book is selected because it crosses a useful persistence boundary and contains transaction/constraint behavior:

```java
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
        Book book = new Book(command.isbn(), command.title(),
                command.author(), command.price(), command.stock());
        return books.save(book);
    }
}
```

Distinguish:

- **application service:** orchestrates a use case and ports;
- **domain service:** expresses domain logic that fits no one entity/value object;
- **Spring `@Service`:** a bean stereotype used for discovery.

One class can be both an application service and a Spring `@Service`, but the terms describe different things.

---

## 7. Input ports

**⭐⭐⭐⭐⭐ MUST KNOW**

An input port describes how an outside driver invokes a use case.

```java
package com.example.bookcatalog.book.application.port.in;

import com.example.bookcatalog.model.Book;

public interface CreateBookUseCase {
    Book create(CreateBookCommand command);
}
```

Ownership:

```text
developer defines it
application layer owns it
BookController calls it
CreateBookService implements it
Spring injects the implementation
```

Why it helps here:

- the controller depends on an application capability, not a multi-purpose concrete service;
- a future message consumer could call the same use case;
- tests can target the implementation contract;
- the public application API becomes explicit.

When it may be unnecessary: a private, tiny use case with one stable controller and no useful boundary. Depending directly on a concrete application service is still dependency injection and can be perfectly maintainable.

### Checkpoint 4

Who should define `CreateBookUseCase`: the controller package or the application package?

<details>
<summary>Answer</summary>

The application owns it because it defines an application capability. The web adapter imports and calls that capability. If the controller owned it, another outer adapter would define the inner application's API.

</details>

---

## 8. Output ports

**⭐⭐⭐⭐⭐ MUST KNOW**

An output port describes what the use case needs from an outside capability.

```java
package com.example.bookcatalog.book.application.port.out;

import com.example.bookcatalog.model.Book;

public interface BookRepositoryPort {
    Book save(Book book);
}
```

It deliberately does not extend `JpaRepository`, expose `EntityManager`, accept `Pageable`, or copy every database operation. Create Book needs to persist one Book and receive the resulting Book.

```text
CreateBookService
       ↓ calls
BookRepositoryPort               owned by application
       ↑ implements
BookPersistenceAdapter           owned by outbound infrastructure
       ↓ delegates
Spring Data BookRepository
```

If list/replace/delete are refactored later, evolve the port according to demonstrated application needs or create more focused ports. Do not predict every future repository method today.

---

## 9. Inbound or driving adapters

**⭐⭐⭐⭐⭐ MUST KNOW**

A driving adapter initiates a use case:

- REST controller;
- command-line handler;
- scheduled job;
- message consumer.

In this project, **presentation** means the inbound web adapter: controllers, request/response DTOs, HTTP validation, status/header selection, and HTTP exception translation. “Presentation” is not the application use case itself.

For the Book Catalog, `BookController` translates HTTP into an application command:

```java
@PostMapping(consumes = "application/json", produces = "application/json")
public ResponseEntity<BookResponse> create(
        @Valid @RequestBody BookRequest request) {
    CreateBookCommand command = new CreateBookCommand(
            request.isbn(), request.title(), request.author(),
            request.price(), request.stock());
    Book book = createBookUseCase.create(command);
    return ResponseEntity.created(URI.create("/books/" + book.getId()))
            .body(BookResponse.from(book));
}
```

The adapter owns `@PostMapping`, `@RequestBody`, validation activation, `ResponseEntity`, `201`, `Location`, and response DTO mapping. The use case owns none of those HTTP decisions.

```text
HTTP request → BookRequest → CreateBookCommand → input port
```

---

## 10. Outbound or driven adapters

**⭐⭐⭐⭐⭐ MUST KNOW**

A driven adapter is called by the application to reach an outside capability:

- JPA persistence;
- email delivery;
- payment gateway;
- object storage;
- external HTTP API;
- cache;
- message producer.

The persistence adapter converts the application request into Spring Data work:

```java
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
```

`saveAndFlush` preserves the current behavior: the ISBN constraint is observed before the use case returns, allowing the existing advice to translate `DuplicateIsbnException` to `409`. Flush is still not commit; the outer use-case transaction commits afterward.

The catch is deliberately faithful to the current implementation. If more integrity constraints are added, refine the translation rather than labeling every possible constraint failure “duplicate ISBN.”

### Checkpoint 5

Why do both `BookRepositoryPort` and `BookRepository extends JpaRepository` exist?

<details>
<summary>Answer</summary>

They serve different owners. The port is the small capability required by the application. The Spring Data interface is an infrastructure API from which Spring creates a runtime proxy. The adapter translates between them. For a trivial feature where this isolation adds no value, using the Spring Data interface directly may be the better choice.

</details>

---

## 11. Spring Boot's role

**⭐⭐⭐⭐⭐ MUST KNOW**

Clean Architecture does not mean “remove Spring.” It means **Spring helps assemble the application without becoming the reason every business decision exists**.

In this project, Spring Boot provides the composition and runtime machinery:

```text
application starts
    ↓
component scanning discovers @RestController, @Service, @Component
    ↓
Spring constructs the object graph through constructor injection
    ↓
Spring creates infrastructure proxies where required
    ├── CreateBookService transaction proxy
    ├── method-security proxy where @PreAuthorize is used
    └── Spring Data proxy implementing BookRepository
    ↓
embedded web server accepts requests
```

The important distinction is between **compile-time structure** and **runtime wiring**.

```text
Compile time
CreateBookService ───────depends on──────> BookRepositoryPort

Runtime
CreateBookService ───────calls───────────> BookPersistenceAdapter
                                             implements the port
```

Constructor injection makes this possible:

```java
@Service
@Transactional
public class CreateBookService implements CreateBookUseCase {
    private final BookRepositoryPort bookRepository;

    public CreateBookService(BookRepositoryPort bookRepository) {
        this.bookRepository = bookRepository;
    }
}
```

`CreateBookService` does not construct a PostgreSQL adapter. It asks for the capability it owns. Spring finds the one bean that implements that capability and supplies it.

### Where does Spring belong?

There are two defensible styles.

| Style | Inner code | Benefit | Cost |
|---|---|---|---|
| Strict | Plain Java; configuration creates beans externally | Maximum framework independence | More explicit configuration and wiring |
| Pragmatic Spring | `@Service`, `@Transactional`, sometimes validation annotations | Less ceremony; idiomatic Spring operation | Inner class has a limited Spring dependency |

The companion chooses pragmatic Spring: `CreateBookService` uses `@Service` and `@Transactional`, but it does **not** import HTTP, Spring Data, JDBC, or PostgreSQL types. This is a deliberate trade-off, not an architectural accident.

Spring annotations have different coupling costs:

```text
usually low coupling                         usually high boundary leakage
@Service / @Transactional        <------>    ResponseEntity / @RequestBody
                                             JpaRepository / PageRequest
                                             SecurityContextHolder
```

The left side describes runtime composition or a use-case boundary. The right side exposes a particular delivery or persistence technology to policy code.

> **Practical rule:** do not measure architecture by the number of Spring imports. Measure whether important policy can be understood and exercised without booting unrelated outer systems.

### Checkpoint 6

If the application service is annotated with `@Service`, is it no longer Clean Architecture?

<details>
<summary>Answer</summary>

No. It is a pragmatic framework dependency. The more important question is whether business orchestration depends on web and persistence details. If strict framework independence has real value, move bean and transaction configuration outward later. Do not add that ceremony by reflex.

</details>

---

## 12. Ownership of the major components

**⭐⭐⭐⭐⭐ MUST KNOW**

Ownership answers two questions:

1. Which layer is allowed to decide this shape or rule?
2. Which layer should change when the decision changes?

For the selected Create Book slice:

| Component | Architectural role | Owns | Must not own |
|---|---|---|---|
| `SecurityConfig` | Outer framework configuration | HTTP authentication, CSRF, URL authorization, session rules | Book creation policy |
| `BookController` | Inbound HTTP adapter | route, JSON input, `201`, `Location`, response mapping | transaction, SQL, persistence exception handling |
| `BookRequest` | HTTP DTO | JSON-facing fields and request-shape constraints | JPA mapping or use-case orchestration |
| `CreateBookCommand` | Application input model | values needed by the use case | HTTP status, JSON, JPA operations |
| `CreateBookUseCase` | Input port | capability exposed to callers | implementation technology |
| `CreateBookService` | Application service | create-book workflow and transaction boundary | controller or `JpaRepository` APIs |
| `BookRepositoryPort` | Output port | minimal persistence capability needed by this use case | database implementation details |
| `BookPersistenceAdapter` | Outbound adapter | Spring Data invocation and persistence exception translation | HTTP response decisions |
| `BookRepository` | Spring Data infrastructure | JPA query/proxy contract | application workflow |
| `Book` | Shared domain/JPA model in this pragmatic design | book state and relevant behavior | HTTP response construction |
| `GlobalExceptionHandler` | Inbound error adapter | exception-to-HTTP translation | deciding whether a database transaction commits |
| Flyway SQL | Persistence infrastructure | PostgreSQL schema history and constraints | Java use-case sequencing |

The following lifecycle-and-call matrix answers the ownership questions that folder names cannot answer:

| Component | Who defines it? | Who creates it at runtime? | Who calls it? | What does it call next? | Allowed dependencies |
|---|---|---|---|---|---|
| `BookController` | Developer, in the web adapter | Spring MVC's application context | `DispatcherServlet` after the security chain | `CreateBookUseCase` and response mapping | Spring MVC, web DTOs, application input port |
| `BookRequest` | Developer, for HTTP | Jackson constructs it; Bean Validation inspects it | MVC/controller | no application collaborator; controller maps it | Java and HTTP-shape validation annotations |
| `CreateBookUseCase` | Developer, owned by application | No interface instance; Spring injects an implementing proxy | Controller or another driving adapter | Contractually, its implementation | Stable application types only |
| `CreateBookService` | Developer, owned by application | Spring creates the target and transaction proxy | Transaction proxy after an input-port call | `BookRepositoryPort` | Application types; Spring stereotypes/transactions only in the selected pragmatic style |
| `BookRepositoryPort` | Developer, owned by application | No interface instance; an adapter is injected | `CreateBookService` | Contractually, its adapter implementation | Application/domain types only; no Spring Data/JPA repository APIs |
| `BookPersistenceAdapter` | Developer, outer persistence adapter | Spring component scanning | Use case through `BookRepositoryPort` dynamic dispatch | Spring Data `BookRepository` | Application port/model plus persistence framework types |
| `BookRepository` declaration/proxy | Developer declares the interface; Spring Data supplies behavior | Spring Data creates the runtime proxy | Persistence adapter | `EntityManager`/Hibernate | Spring Data JPA and mapped entities |
| Shared `Book` | Developer defines state/behavior/mapping | Use case constructs new instances; Hibernate rehydrates persisted ones | Use case, adapter, Hibernate, response mapper | Its own behavior; Hibernate callbacks internally | Java plus deliberate Jakarta Persistence coupling in this pragmatic design |
| Transaction proxy/interceptor | Spring provides it from developer annotations/configuration | Spring's transaction infrastructure | Controller calls the injected proxy | transaction manager, then target service | Spring transaction APIs and configured manager |
| `SecurityFilterChain` | Developer declares policy; Spring Security defines machinery | Spring Boot/Security creates filters and chain | Servlet container before MVC | authentication, CSRF, authorization, then MVC | Servlet and Spring Security infrastructure |
| `GlobalExceptionHandler` | Developer, web adapter | Spring MVC | MVC when controller processing raises an exception | Problem Detail/HTTP response creation | Web exceptions, stable application exceptions, Spring MVC |
| Flyway migrations/runner | Developer owns SQL history; Flyway supplies runner | Spring Boot auto-configuration creates/runs Flyway | Application startup | PostgreSQL through a datasource | SQL/PostgreSQL at the infrastructure boundary |

“Who creates it?” and “who defines it?” often differ. Developers define `BookRepository`; Spring Data creates its proxy. Developers define transaction metadata; Spring creates the interceptor. That distinction is why Clean Architecture must describe both compile-time ownership and runtime composition.

Ownership is more precise than “put business logic in services.” For example:

```text
“Create a book”                      application responsibility
“Require ADMIN for POST /books”      HTTP security responsibility
“ISBN is UNIQUE”                     database invariant / final concurrency guard
“Duplicate ISBN becomes HTTP 409”    inbound HTTP error translation
```

The same fact may be defended at multiple boundaries for different reasons. An ISBN duplicate can be rejected by a friendly application check, but the database unique constraint remains necessary under concurrency. The adapter translates the database failure; the controller advice selects HTTP `409`.

### The interface-owner test

Ask: “Who needs this abstraction?”

```text
CreateBookService needs persistence
        ↓
application owns BookRepositoryPort
        ↑
outer persistence adapter implements it
```

Do not place every interface in a global `interfaces` package. Location should reveal the consumer and the direction of dependency.

---

## 13. HTTP DTOs and application commands

**⭐⭐⭐⭐⭐ MUST KNOW**

The current `BookRequest` is an HTTP input model. It contains Jakarta Validation annotations and performs whitespace normalization. That is suitable at the web boundary:

```java
public record BookRequest(
        @NotBlank @Size(max = 20) String isbn,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 120) String author,
        @NotNull @PositiveOrZero BigDecimal price,
        @NotNull @PositiveOrZero Integer stock) {
}
```

The use case receives a separate application command:

```java
package com.example.bookcatalog.book.application.port.in;

import java.math.BigDecimal;

public record CreateBookCommand(
        String isbn,
        String title,
        String author,
        BigDecimal price,
        Integer stock) {
}
```

The controller maps one to the other:

```java
CreateBookCommand command = new CreateBookCommand(
        request.isbn(),
        request.title(),
        request.author(),
        request.price(),
        request.stock());
```

Why not pass `BookRequest` directly?

- a use case should not require an HTTP-shaped type;
- another adapter—a CLI, batch job, or message consumer—need not construct a web DTO;
- changing the JSON contract need not automatically change the application API;
- annotations at the network boundary remain at that boundary.

Why not use five scalar arguments?

```java
create(String isbn, String title, String author,
       BigDecimal price, Integer stock);
```

Five adjacent values are easy to reorder and make the use-case signature harder to evolve. A command gives the input one meaningful name.

Why not create commands for every method immediately? Because a command is useful only when it protects a boundary or clarifies a meaningful operation. `findById(long id)` does not need a `FindBookByIdQuery` merely to look architectural.

### Command, DTO, entity, and response

| Type | Direction | Primary owner | May contain |
|---|---|---|---|
| `BookRequest` | HTTP → controller | Web adapter | JSON/request validation concerns |
| `CreateBookCommand` | controller → application | Application | use-case input values |
| `Book` | application ↔ persistence in this design | Shared pragmatic model | state, behavior, JPA annotations |
| `BookResponse` | controller → HTTP | Web adapter | serialized response representation |

The names are less important than keeping the responsibilities explicit.

### Checkpoint 7

Would `CreateBookCommand` still be useful if there were only one REST controller forever?

<details>
<summary>Answer</summary>

Possibly, but the benefit is smaller. It still prevents the application API from being defined by HTTP annotations and gives the use-case input a stable name. If the operation had only one simple scalar and no foreseeable boundary pressure, passing that scalar directly could be clearer.

</details>

---

## 14. Mapping boundaries

**⭐⭐⭐⭐ IMPORTANT**

Mapping is not “busywork” by definition. It marks a change of ownership:

```text
JSON
  ↓ Jackson
BookRequest                 owned by HTTP adapter
  ↓ explicit mapping
CreateBookCommand           owned by application boundary
  ↓ construction
Book                        domain/JPA model in this design
  ↓ explicit mapping
BookResponse                owned by HTTP adapter
  ↓ Jackson
JSON
```

For a five-field object, direct Java mapping is easiest to read and debug:

```java
private static CreateBookCommand toCommand(BookRequest request) {
    return new CreateBookCommand(
            request.isbn(),
            request.title(),
            request.author(),
            request.price(),
            request.stock());
}
```

No mapper framework is needed. A framework such as MapStruct can become valuable when mapping is large and repetitive, but it is not a prerequisite for Clean Architecture.

### Where should mapping happen?

| Mapping | Natural owner | Reason |
|---|---|---|
| JSON/request DTO → command | Inbound adapter | It knows both the delivery contract and input port |
| command → new domain object | Application/domain factory | It owns creation semantics |
| domain → response DTO | Inbound adapter | It owns the delivery representation |
| domain ↔ separate JPA entity | Outbound persistence adapter | It owns persistence representation |
| persistence exception → application exception | Outbound persistence adapter | It understands infrastructure failures |
| application exception → HTTP status/body | Inbound error adapter | It understands HTTP |

Avoid a universal `MapperUtil` that imports every layer. That creates a central coupling hub and hides who owns each conversion.

### Mapping is not validation

Mapping copies or transforms representation. Validation decides acceptability. Do not hide business decisions in generic mapping code:

```java
// Surprising: mapping silently changes a business value.
int stock = Math.max(request.stock(), 0);
```

If negative stock is illegal, reject it explicitly at the appropriate boundary.

---

## 15. The persistence adapter

**⭐⭐⭐⭐⭐ MUST KNOW**

The adapter protects the application from a particular persistence API. In the selected slice, the application declares:

```java
public interface BookRepositoryPort {
    Book save(Book book);
}
```

Infrastructure implements it:

```java
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
```

Underneath that short method:

```text
BookPersistenceAdapter
    ↓ calls
Spring Data BookRepository proxy
    ↓ delegates to
EntityManager
    ↓ manages
Hibernate persistence context
    ↓ emits SQL through
JDBC Connection borrowed from HikariCP
    ↓ executes against
PostgreSQL
```

`BookRepository` remains useful:

```java
public interface BookRepository extends JpaRepository<Book, Long> {
    // Existing query methods remain available to unchanged features.
}
```

Spring Data generates an implementation proxy at runtime. `BookPersistenceAdapter` is not replacing Spring Data; it is placing a use-case-sized boundary in front of it.

### Why is this port deliberately small?

An output port represents what the application needs, not everything the tool offers:

```java
// Focused
interface BookRepositoryPort {
    Book save(Book book);
}
```

```java
// Infrastructure leaked back into the application
interface BookRepositoryPort extends JpaRepository<Book, Long> {
}
```

The second version does not invert anything. Application code still sees Spring Data paging, sorting, flush, specifications, and repository lifecycle semantics.

Do not mechanically build a wrapper with all `JpaRepository` methods. Add a port operation when a real use case needs it, and name it in application language where possible.

### Persistence context still matters

The adapter boundary does not change JPA rules:

- a persisted entity becomes managed in the transaction's persistence context;
- Hibernate may delay SQL until flush;
- dirty checking compares managed state;
- flush synchronizes SQL but does not commit;
- commit makes the database transaction durable;
- rollback discards the transaction's uncommitted SQL effects;
- after the persistence context closes, entities are detached.

Architecture controls dependencies. It does not repeal Hibernate behavior.

### Why use `saveAndFlush` for this baseline?

The existing create path catches `DataIntegrityViolationException` so duplicate ISBN is returned as `409`. PostgreSQL reports the unique violation when SQL is sent. Explicit flush makes that happen inside the adapter's `try` block.

```text
repository.save(book)       may queue INSERT until later
repository.saveAndFlush()   requests INSERT now
transaction commit          still happens after service returns
```

This is a behavioral preservation choice, not a general rule that every adapter should flush eagerly.

### Checkpoint 8

Does introducing `BookRepositoryPort` make the application database-independent?

<details>
<summary>Answer</summary>

Not fully. The use-case orchestration no longer imports Spring Data, but `Book` still has JPA annotations and its identity strategy is persistence-aware. The selected change buys focused isolation without paying for a second persistence model. Database and ORM independence are gradients, not a switch.

</details>

---

## 16. Transaction boundaries

**⭐⭐⭐⭐⭐ MUST KNOW**

A transaction belongs around a complete business use case, not around an arbitrary DAO line.

```java
@Service
@Transactional
public class CreateBookService implements CreateBookUseCase {
    private final BookRepositoryPort bookRepository;

    public CreateBookService(BookRepositoryPort bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book create(CreateBookCommand command) {
        Book book = new Book(
                command.isbn(),
                command.title(),
                command.author(),
                command.price(),
                command.stock());
        return bookRepository.save(book);
    }
}
```

Runtime flow:

```text
Controller
  ↓ calls Spring proxy implementing CreateBookUseCase
TransactionInterceptor
  ↓ asks JpaTransactionManager to begin
EntityManager / persistence context associated with current thread
  ↓
CreateBookService.create(...)
  ↓
BookRepositoryPort → adapter → Spring Data → Hibernate
  ↓
flush sends INSERT through JDBC connection
  ↓ method returns
transaction manager commits PostgreSQL transaction
  ↓
connection is returned to HikariCP
```

Failure flow:

```text
PostgreSQL unique constraint fails
  ↑ SQLException
Hibernate/JPA exception
  ↑ Spring translates to DataIntegrityViolationException
adapter translates to DuplicateIsbnException
  ↑ unchecked exception crosses service proxy
transaction interceptor rolls back
  ↑
GlobalExceptionHandler maps it to HTTP 409
```

### Why not put `@Transactional` on the controller?

- HTTP parsing and response production should not keep a database connection and locks alive;
- controller concerns can grow to include slow serialization or other I/O;
- the same use case called from another adapter would need the same atomic boundary;
- the application operation, not the transport, defines what must commit together.

### Why not rely only on repository transactions?

Spring Data write methods are transactional, but separate repository calls are not automatically one business transaction when the surrounding service has none.

```text
save A commits
save B fails
result: partial business operation
```

With one application transaction:

```text
BEGIN
save A
save B fails
ROLLBACK
result: neither commits
```

### Proxy warning

Default annotation-driven transactions are proxy-based. A call must cross the Spring proxy:

```text
controller → transactional service proxy → target method    intercepted
```

An internal call does not re-enter the proxy:

```java
public void outer() {
    inner(); // direct call on this
}

@Transactional
public void inner() {
}
```

If `outer()` was not already transactional, `inner()` does not start a transaction in the usual proxy mode. Prefer a public use-case boundary called by another bean; split a genuinely separate use case into a collaborator rather than using proxy tricks.

### Strict and pragmatic placement

| Choice | Transaction declaration | Trade-off |
|---|---|---|
| Pragmatic selected design | `@Transactional` on application service | Clear, concise, Spring-coupled application class |
| Strict design | transaction-decorating adapter/configuration around plain service | Framework-free use case, extra configuration/indirection |
| Too far outward | controller transaction | Transport owns atomicity and may hold resources too long |
| Too far inward | each repository call separately | Multi-step use case can partially commit |

The selected reference uses **method-level** `@Transactional` on the single write method. Some examples use a class-level annotation for a service whose every public method has the same semantics. Both are intercepted through the same proxy mechanism; class-level metadata applies broadly and method-level metadata can override it (for example, a class-level `readOnly = true` with write-method overrides). Choose deliberately and keep one reference implementation consistent.

If strict framework independence is worth the extra composition code, the plain service can be wrapped at the outer edge with `TransactionTemplate`. This is a complete alternative shape, not the recommendation for this small exercise:

```java
package com.example.bookcatalog.book.config;

import com.example.bookcatalog.book.application.port.in.CreateBookUseCase;
import com.example.bookcatalog.book.application.port.out.BookRepositoryPort;
import com.example.bookcatalog.book.application.service.CreateBookService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class CreateBookTransactionConfiguration {

    @Bean
    CreateBookUseCase createBookUseCase(
            BookRepositoryPort books,
            PlatformTransactionManager transactionManager) {
        CreateBookUseCase target = new CreateBookService(books);
        TransactionTemplate transactions =
                new TransactionTemplate(transactionManager);

        return command -> transactions.execute(
                status -> target.create(command));
    }
}
```

In that variant, `CreateBookService` has no `@Service` or `@Transactional`; configuration creates the plain target and exposes exactly one transactional `CreateBookUseCase` bean. The benefit is a framework-free use case. The costs are an extra wrapper/configuration concept and less immediately visible transaction metadata.

### Connection-pool consequence

An active database transaction generally occupies a JDBC connection until completion:

```text
begin → borrow/use connection → SQL/locks → commit or rollback → return connection
```

Keep slow HTTP calls, file operations, and human waits out of database transaction boundaries. Increasing the Hikari pool size is not a substitute for short, correctly scoped transactions.

### Checkpoint 9

If the adapter calls `saveAndFlush`, has the transaction committed?

<details>
<summary>Answer</summary>

No. Flush synchronizes pending changes with PostgreSQL through SQL. The transaction can still roll back afterward. Commit is performed by the transaction manager after the transactional method successfully returns.

</details>

---

## 17. Validation at three boundaries

**⭐⭐⭐⭐⭐ MUST KNOW**

“Validation” is not one responsibility. A production request can be rejected for three different reasons:

```text
HTTP representation validation
        ↓
application/domain rule validation
        ↓
database constraint enforcement
```

### Boundary 1 — request shape

The web adapter answers questions such as:

- Is required JSON present?
- Can the value be parsed as a number?
- Is the submitted string blank or longer than the public contract allows?
- Is stock syntactically a non-negative request value?

```java
public record BookRequest(
        @NotBlank(message = "isbn must not be blank")
        @Size(max = 20, message = "isbn size must be less than or equal to 20 chars")
        String isbn,

        @NotNull(message = "stock must not NULL")
        @PositiveOrZero(message = "stock must be >= 0")
        Integer stock) {
}
```

`@Valid @RequestBody` activates this at the controller boundary. Invalid input becomes the project's existing `400 Bad Request` response before the use case runs.

### Boundary 2 — business rules

Application or domain validation answers questions that must hold regardless of transport:

- Can this actor create a catalog entry?
- Is this state transition allowed?
- May stock move from its current value to the requested value?
- Does this operation violate an application invariant?

If “stock can never be negative” is a true business invariant, relying solely on `BookRequest` is incomplete. A batch adapter or a Java caller could construct `CreateBookCommand` directly. The rule can be defended in a domain constructor/factory or the application service.

```java
public Book(String isbn, String title, String author,
            BigDecimal price, Integer stock) {
    if (stock == null || stock < 0) {
        throw new IllegalArgumentException("stock must be greater than or equal to zero");
    }
    this.isbn = isbn;
    this.title = title;
    this.author = author;
    this.price = price;
    this.stock = stock;
}
```

That snippet illustrates ownership; the companion does not silently add this rule because it would change the shared entity and more behavior than the selected vertical slice.

### Boundary 3 — database integrity

PostgreSQL is the final concurrent-state authority:

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
```

The unique constraint matters even if Java first queries for the ISBN:

```text
Transaction A checks: absent
Transaction B checks: absent
A inserts
B inserts
database UNIQUE constraint lets only one succeed
```

### Defense is not duplication without purpose

| Boundary | Optimizes for | Example failure |
|---|---|---|
| HTTP DTO | clear client feedback and protocol shape | `400` for blank title |
| Application/domain | rules independent of delivery | forbidden state transition |
| Database | integrity under every writer and race | unique/not-null violation |

Do not put every rule at every layer. Place it where it can be authoritative, then add earlier validation only when it improves feedback or protects a meaningful boundary.

### Baseline mismatch to notice

The current request contract limits ISBN to 20 characters while V1 declares `VARCHAR(120)`. That is not automatically a defect: an API can deliberately be stricter than storage. It should, however, be a conscious rule rather than an unexplained drift.

### Checkpoint 10

If `@PositiveOrZero` rejects negative stock at the REST controller, can the application safely assume every future caller supplies non-negative stock?

<details>
<summary>Answer</summary>

No. That annotation protects one inbound adapter. A domain invariant must be enforced where every caller crosses it, or ultimately by a database constraint. The right choice depends on whether non-negative stock is truly invariant across all use cases.

</details>

---

## 18. Exception flow

**⭐⭐⭐⭐⭐ MUST KNOW**

Exceptions also cross architectural boundaries and may need translation.

The duplicate-ISBN path is an excellent complete example:

```text
PostgreSQL reports SQLSTATE unique violation
    ↑
JDBC / Hibernate / JPA report persistence failure
    ↑
Spring persistence translation produces DataIntegrityViolationException
    ↑
BookPersistenceAdapter throws DuplicateIsbnException
    ↑
CreateBookService proxy marks transaction for rollback
    ↑
GlobalExceptionHandler produces HTTP 409 response
```

Each translation removes an outer vocabulary:

| Boundary | Should expose | Should hide |
|---|---|---|
| PostgreSQL/JPA → application | meaningful application failure such as duplicate ISBN | vendor SQL exception details |
| application → HTTP | `409 Conflict` and safe error body | stack trace, JDBC details |

The persistence adapter catches the technology-facing exception:

```java
@Override
public Book save(Book book) {
    try {
        return repository.saveAndFlush(book);
    } catch (DataIntegrityViolationException exception) {
        throw new DuplicateIsbnException(exception);
    }
}
```

The web error adapter maps the application-facing exception:

```java
@ExceptionHandler(DuplicateIsbnException.class)
public ResponseEntity<ProblemDetail> handleDuplicateIsbn(
        DuplicateIsbnException exception) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, exception.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
}
```

The project's actual handler has its established error-body format; the point is the direction, not replacing that contract.

### Application code should not speak HTTP

Avoid this in an application service:

```java
throw new ResponseStatusException(HttpStatus.CONFLICT, "duplicate ISBN");
```

It binds the use case to HTTP. A scheduled import and a REST call can both receive `DuplicateIsbnException`; only the HTTP adapter converts it to `409`.

### Catching can alter rollback

Spring normally rolls back on unchecked exceptions that leave a transactional proxy invocation. Swallowing one may cause an unexpected commit:

```java
@Transactional
public void create(CreateBookCommand command) {
    try {
        bookRepository.save(toBook(command));
    } catch (DuplicateIsbnException exception) {
        log.warn("ignored duplicate", exception);
        // Method returns normally. Do not assume rollback now happens as intended.
    }
}
```

Translate and rethrow when failure should abort the use case. Do not catch exceptions solely to log them; global logging and exception advice can avoid duplicate logs.

### Do not overtranslate blindly

The baseline maps any `DataIntegrityViolationException` caught on create to duplicate ISBN. That preserves current behavior, but it is only safe while ISBN is the relevant integrity failure. As constraints grow, inspect constraint identity or design narrower operations so a not-null/check violation is not mislabeled.

### Current optimistic-locking behavior

`Book` already has `@Version`. A stale update can produce an optimistic-locking exception. The current global catch-all will expose it as the generic `500` response. A later feature could translate it to a deliberate conflict response, but this architecture exercise does not change that API behavior.

---

## 19. Spring Security

**⭐⭐⭐⭐⭐ MUST KNOW**

Security is not one ring. Separate four concerns:

1. **Authentication:** who is the caller?
2. **Transport-level authorization:** may this HTTP request reach the controller?
3. **Business authorization:** may this actor perform this use case on this resource?
4. **Security infrastructure:** sessions, password hashing, CSRF, filters, framework context.

### HTTP security is an outer adapter concern

The inspected application is session-based and CSRF-protected:

```text
request
  ↓
SecurityFilterChain
  ├── load/authenticate session principal
  ├── enforce CSRF on unsafe methods
  ├── apply URL authorization
  └── continue or return 401/403
  ↓
BookController
```

For the current contract:

| Request | Expected boundary behavior |
|---|---|
| `GET /books` | public |
| `GET /books/{id}` | public |
| `GET /auth/csrf` | public token bootstrap |
| anonymous protected safe request | `401` |
| unsafe request without valid CSRF | `403`, usually before controller |
| authenticated `USER` writing books with valid CSRF | `403` |
| authenticated `ADMIN` writing books with valid CSRF | allowed to controller/use case |

Refactoring `POST /books` behind an input port must not weaken or duplicate those filter-chain rules.

### Method security is near an application boundary

The existing `setStock` uses `@PreAuthorize("hasRole('ADMIN')")`. Method authorization is implemented through Spring AOP interception, so it shares the proxy caveat:

```text
external bean → method-security proxy → secured method     checked
same object → self-invoked secured method                  may bypass proxy
```

Method security can protect a use case that has multiple entry points. URL security protects an HTTP surface. Using both can be valid defense in depth, but duplicating every rule in both places without a reason makes policy harder to audit.

### Business authorization belongs inward

Suppose a future rule says, “A catalog editor may update only books assigned to their branch.” That is not merely a URL pattern. The use case needs actor identity and book context.

There are three practical ways to supply the actor:

#### Option A — map actor identity into the command

```java
public record UpdateBookCommand(
        long bookId,
        long actorId,
        String title) {
}
```

The web adapter reads the authenticated principal and supplies a stable application value. This is explicit and very testable.

#### Option B — define an application-owned current-user port

```java
package com.example.bookcatalog.identity.application.port.out;

public interface CurrentUserPort {
    String requiredUsername();
    boolean hasRole(String role);
}
```

An outer Spring Security adapter can implement it using `SecurityContextHolder`:

```java
package com.example.bookcatalog.identity.adapter.out.security;

import com.example.bookcatalog.identity.application.port.out.CurrentUserPort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    @Override
    public String requiredUsername() {
        Authentication authentication = currentAuthentication();
        return authentication.getName();
    }

    @Override
    public boolean hasRole(String role) {
        String authority = role.startsWith("ROLE_")
                ? role
                : "ROLE_" + role;
        return currentAuthentication().getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    private static Authentication currentAuthentication() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("An authenticated user is required");
        }
        return authentication;
    }
}
```

The application owns the stable question; the adapter owns Spring Security's thread-associated context. This is useful when many use cases need the caller and makes unit testing possible through a fake port. Its trade-off is that identity remains an implicit runtime dependency instead of an explicit command value, and asynchronous work needs deliberate security-context propagation or an explicit actor value.

#### Option C — accept a framework principal inside the use case

```java
public void update(Authentication authentication) {
}
```

This is simplest but couples application policy directly to Spring Security. It may be acceptable in a small pragmatic system, but recognize the trade-off.

### Do not invent identity dependencies

Create Book currently needs no username or user ID for its business behavior. Therefore the selected slice does **not** add `CurrentUserPort`. Adding an abstraction for a hypothetical requirement is not Clean Architecture; it is speculative complexity.

### Passwords remain infrastructure-sensitive

Password hashing, session configuration, CSRF token handling, and form-login endpoints stay in the security/web side. The application may express “register user,” but it should not implement a hashing algorithm itself. A password-encoder port is justified only when isolating that capability brings real value.

### Checkpoint 11

Should the controller pass Spring Security's `Authentication` object to every application service “just in case”?

<details>
<summary>Answer</summary>

No. Pass only information the use case needs. If actor identity becomes a real application input, use an explicit stable value or an application-owned port. Avoid spreading framework context through methods with no authorization requirement.

</details>

---

## 20. Flyway and PostgreSQL

**⭐⭐⭐⭐ IMPORTANT**

The database is an outer implementation detail, but it is also a real source of guarantees. Clean Architecture does not mean pretending SQL and PostgreSQL behavior are interchangeable.

### Schema ownership

Flyway migration files belong to the persistence/infrastructure side:

```text
src/main/resources/db/migration/
├── V1__create_books.sql
├── V2__add_optimistic_lock_version.sql
├── V3__create_transaction_audits.sql
├── V4__add_books_author_index.sql
├── V5__replace_author_index_for_paging.sql
└── V6__create_app_users.sql
```

These files define durable database history. JPA mappings must agree with that history; they do not replace it.

The project uses:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

That is a strong production-oriented division:

```text
Flyway changes schema
Hibernate validates mappings against schema
application code uses mapped data
```

### A critical source-tree fact

In the inspected working tree, V1–V6 exist only under `target/classes/db/migration`, while `src/main/resources/db/migration` is absent. `target/` is generated output. A clean build can delete it; a fresh database then has no authoritative migration resources.

The companion restores the exact V1–V6 SQL into source before refactoring. That is a baseline integrity repair, not a new architecture feature.

### Constraints and architecture cooperate

- Java request validation produces friendly early feedback.
- application policy protects use-case rules.
- PostgreSQL constraints protect stored integrity under all writers and races.
- the persistence adapter translates relevant failures.

Do not remove a unique constraint because an application port exists. Dependency direction does not make concurrent state checks atomic.

### Database independence has a price

An output port can prevent an application service from importing `JpaRepository`, but switching PostgreSQL to a different database can still require changes to:

- DDL syntax and identity generation;
- data types and precision;
- isolation and locking behavior;
- index design and query plans;
- exception translation;
- Testcontainers image/configuration;
- operational backup and monitoring.

The architecture localizes many changes. It does not make them disappear.

### Never edit applied migrations casually

Once V1–V6 are applied to shared environments, preserve their checksums. Add V7 for a new change. Restoring missing source copies must reproduce the already-applied contents exactly; silently rewriting history can make Flyway validation fail.

---

## 21. Testing through boundaries

**⭐⭐⭐⭐⭐ MUST KNOW**

Clean Architecture is valuable when it gives tests a cheaper, clearer seam—not when it merely increases class count.

### 1. Pure application test

Construct the service with a handwritten fake port. No Spring context, HTTP server, Hibernate, HikariCP, or PostgreSQL is required.

```java
package com.example.bookcatalog.book.application.service;

import com.example.bookcatalog.book.application.port.in.CreateBookCommand;
import com.example.bookcatalog.book.application.port.out.BookRepositoryPort;
import com.example.bookcatalog.model.Book;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CreateBookServiceTest {

    @Test
    void createsBookThroughOutputPort() {
        RecordingBookRepository repository = new RecordingBookRepository();
        CreateBookService service = new CreateBookService(repository);
        CreateBookCommand command = new CreateBookCommand(
                "978-0134685991",
                "Effective Java",
                "Joshua Bloch",
                new BigDecimal("45.00"),
                3);

        Book result = service.create(command);

        assertSame(result, repository.savedBook());
        assertEquals("978-0134685991", result.getIsbn());
        assertEquals(3, result.getStock());
    }

    private static final class RecordingBookRepository
            implements BookRepositoryPort {
        private Book saved;

        @Override
        public Book save(Book book) {
            saved = book;
            return book;
        }

        private Book savedBook() {
            return saved;
        }
    }
}
```

This verifies orchestration and construction. It intentionally does **not** prove JPA mapping, SQL, constraints, transaction proxies, or PostgreSQL behavior.

### 2. Persistence-adapter integration test

Test the real adapter with the real relevant infrastructure:

```text
BookPersistenceAdapter
  ↓
Spring Data repository proxy
  ↓
Hibernate + Flyway
  ↓
PostgreSQL Testcontainer
```

This is where to verify:

- entity mapping and generated identity;
- migration correctness;
- actual PostgreSQL unique constraint;
- adapter exception translation;
- flush behavior.

Do not replace PostgreSQL with H2 when the behavior being tested is PostgreSQL DDL or constraint semantics. A fake port and a real-database adapter test have different jobs.

### 3. Web/security test

A focused MVC or end-to-end test can verify:

- JSON-to-DTO validation;
- DTO-to-command mapping;
- response DTO, status, and `Location`;
- authentication, role, session, and CSRF behavior;
- exception-to-HTTP translation.

It should not be the only place that application behavior is tested.

### 4. Manual contract verification

For this learning project, `curl` or Postman plus `psql`/pgAdmin remains valuable:

```text
obtain CSRF + session
  ↓
authenticate as ADMIN
  ↓
POST /books
  ↓
observe 201 / Location / JSON
  ↓
query books table
```

Also verify missing CSRF, `USER` authorization, invalid input, duplicate ISBN, and the intentionally failing delete rollback path.

### Test what each boundary promises

| Test | Uses Spring? | Uses PostgreSQL? | Best evidence |
|---|---:|---:|---|
| Application unit test | No | No | use-case decisions and port interaction |
| Adapter integration test | Yes | Yes | mapping, constraints, exception translation |
| MVC/security test | Yes | Usually no/controlled | web contract and security chain |
| End-to-end/manual | Yes | Yes | complete runtime composition |
| Architecture rule test | Optional | No | forbidden compile-time dependencies |

### Current project test baseline

The source currently has only an old generated-style JUnit 4.11 `AppTest`; compiled tests under `target/` are stale artifacts. The companion replaces that source baseline with the smallest current Spring Boot 4.1 test stack needed for JUnit Jupiter, JPA slice testing, and PostgreSQL Testcontainers.

Boot 4 uses modular test starters. The exercise uses the current packages for `@DataJpaTest`, test-database replacement, Flyway auto-configuration, and Testcontainers service connections rather than copying older Boot 3 imports.

### Architecture tests are optional

ArchUnit or Java modules can enforce rules such as “application packages do not import web packages.” They become useful when a larger team repeatedly violates boundaries. They are future tooling, not a replacement for understanding the arrows.

### Checkpoint 12

If `CreateBookServiceTest` passes with a fake port, is the feature proven production-ready?

<details>
<summary>Answer</summary>

No. It proves the inner orchestration in isolation. Adapter integration tests prove JPA/PostgreSQL behavior; web/security tests prove the HTTP boundary; a small end-to-end path proves wiring. Each test answers a different question.

</details>

---

## 22. Package organization

**⭐⭐⭐⭐ IMPORTANT**

Packages should make responsibility and dependency direction discoverable. The companion introduces one feature-oriented island inside the existing application:

```text
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
├── dto                         existing web DTOs
├── exception                   existing application-facing exceptions
├── model                       existing shared JPA models
├── repository                  existing Spring Data infrastructure
├── security                    existing Spring Security configuration
├── service                     unchanged use cases/services
└── web                         existing inbound web adapters
```

This is intentionally hybrid. A full folder migration would obscure the lesson and create review noise unrelated to the one boundary being evaluated.

### Package by layer

```text
controller/
service/
repository/
```

Advantages:

- familiar and small;
- easy to scan in a tiny project;
- minimal nesting.

Costs as the system grows:

- one feature is scattered across the entire tree;
- package-private feature collaboration is difficult;
- ownership can become vague.

### Package by feature with internal roles

```text
book/
  application/
  adapter/
user/
  application/
  adapter/
```

Advantages:

- feature cohesion;
- explicit boundaries inside a feature;
- easier extraction into a module if ever justified.

Costs:

- deeper tree;
- repeated role names;
- overkill for features with one trivial class.

### Naming rule

Name by role or use case, not by fashionable suffix alone:

```text
CreateBookUseCase       says what callers can do
BookRepositoryPort      says what capability is required
BookPersistenceAdapter  says which outer mechanism implements it
```

Avoid dumping grounds such as `common`, `utils`, `manager`, and `impl` unless their responsibility is genuinely precise.

---

## 23. Dependency matrix

**⭐⭐⭐⭐⭐ MUST KNOW**

Use this matrix to review imports and calls. “May depend” means the dependency can be reasonable; it is not an instruction to add it.

| From ↓ / To → | Domain model | Application/ports | Inbound web | Outbound persistence | Spring Data/JPA infra | Spring Security config |
|---|---:|---:|---:|---:|---:|---:|
| Domain behavior | itself | usually no | no | no | pragmatic annotations only | no |
| Application service | yes | yes | no | no concrete adapter | no | preferably no |
| Input port/command | stable domain/value types | itself | no | no | no | no |
| Output port | stable domain/value types | itself | no | no | no | no |
| Inbound web adapter | yes for mapping | yes | yes | no | no | may read principal |
| Outbound persistence adapter | yes | yes, implements port | no | yes | yes | no |
| Spring Data repository | JPA entity | no | no | yes | yes | no |
| Security configuration | no | optional bean references | web routes | no | user infrastructure if needed | yes |
| Global exception handler | error types | application exceptions | yes | should not expose vendor failures | no | access-denied translation |

The essential arrows are:

```text
inbound adapter ───────> input port <────── application service
                                               │
                                               ▼
                                         output port
                                               ▲
                                               │ implements
                                    outbound adapter ─────> framework/tool
```

Notice the runtime call from application to adapter points outward, while the **source dependency** of the adapter points inward toward the port. Dependency inversion is about source-code dependency, not pretending runtime calls run backward.

### Import review checklist

Inside `book.application`, question imports from:

- `org.springframework.web.*`;
- `com.example.bookcatalog.web.*`;
- `org.springframework.data.*`;
- `jakarta.servlet.*`;
- `org.springframework.security.core.context.*`;
- vendor JDBC/PostgreSQL packages.

`@Service` and `@Transactional` are allowed by this project's pragmatic policy. That exception should be explicit so “Clean Architecture” does not become a vague claim that changes per reviewer.

### Checkpoint 13

At runtime, `CreateBookService` calls `BookPersistenceAdapter`. Why does the compile-time dependency still point inward?

<details>
<summary>Answer</summary>

The service is compiled against `BookRepositoryPort`, which it owns. The adapter imports and implements that port. Spring connects the implementation at runtime. The concrete outer class depends on the inner contract even though method calls flow from the use case to the supplied object.

</details>

---

## 24. Layered versus Clean Architecture

**⭐⭐⭐⭐⭐ MUST KNOW**

Layered Architecture and Clean Architecture are not enemies. They emphasize different controls.

### Conventional layered flow

```text
Controller
    ↓
Service
    ↓
Spring Data Repository
    ↓
Database
```

This organizes code by technical responsibility and usually allows dependencies to point downward. It is excellent when business behavior is simple and framework coupling has low cost.

### Port-based flow

```text
HTTP adapter → input port ← application service
                                ↓
                           output port
                                ↑
                         JPA adapter → Spring Data → database
```

This adds an ownership rule: the use case defines what it needs; the outer mechanism adapts to that need.

### Direct comparison using Create Book

| Question | Existing layered path | Selected Clean slice |
|---|---|---|
| Controller calls | concrete `BookService` | `CreateBookUseCase` |
| Service calls | `BookRepository` | `BookRepositoryPort` |
| Spring Data exception translation | inside service | persistence adapter |
| Pure use-case test | requires mocking Spring Data-shaped dependency | uses tiny fake application port |
| Number of types | lower | higher |
| JPA annotations on `Book` | yes | still yes, pragmatically |
| HTTP contract | existing | unchanged |
| Database | PostgreSQL | still PostgreSQL |
| Switching persistence work | spread into service plus repo | localized more strongly, not eliminated |

### Before and after dependency pressure

```text
BEFORE
BookController → BookService → JpaRepository
                       │              │
                       └─ catches Spring DAO exception

AFTER
BookController → CreateBookUseCase ← CreateBookService → BookRepositoryPort
                                                       ↑
                                  BookPersistenceAdapter → JpaRepository
```

The after version is not “better” merely because it has more arrows. It is better only if these benefits matter:

- Create Book is an explicit application capability;
- its orchestration has a cheap test seam;
- persistence vocabulary no longer appears in that service;
- database-specific failure translation has a clear owner;
- future adapters can invoke the same input port.

Its real costs are:

- four new application/adapter types plus mapping;
- more navigation;
- potential confusion in a hybrid tree;
- temptation to copy the pattern onto trivial CRUD without evidence.

### A useful evolution strategy

```text
start simple
  ↓
observe change/testing/coupling pressure
  ↓
extract one valuable boundary
  ↓
measure whether it helped
  ↓
repeat only where justified
```

Architecture can evolve one vertical slice at a time. A “big-bang clean rewrite” is rarely required.

---

## 25. Clean, Hexagonal, Ports and Adapters, and Onion

**⭐⭐⭐ NICE TO KNOW**

These families overlap strongly, but their metaphors emphasize different things.

| Name | Main emphasis | Common language |
|---|---|---|
| Clean Architecture | policy inward, details outward, Dependency Rule | entities, use cases, interface adapters, frameworks |
| Hexagonal Architecture | application protected by interchangeable sides | inside/outside, ports, primary/secondary adapters |
| Ports and Adapters | explicit contracts connecting application to drivers/tools | input/output ports, driving/driven adapters |
| Onion Architecture | domain model at the center; dependencies point inward | domain, domain services, application, infrastructure rings |

For this guide, the vocabulary maps as follows:

```text
driver / primary / inbound adapter
    BookController
          ↓
input port
    CreateBookUseCase
          ↓
application use case
    CreateBookService
          ↓
output port
    BookRepositoryPort
          ↑
driven / secondary / outbound adapter
    BookPersistenceAdapter
```

Do not spend design meetings arguing whether a class is in “ring three” or on the “secondary side of the hexagon.” Ask concrete questions:

- Who owns this interface?
- What policy is protected?
- Which outer detail is hidden?
- Which direction do imports point?
- Which test becomes cheaper?
- What does the boundary cost?

The answers matter more than the diagram shape.

### Checkpoint 14

Are “input port” and “REST controller” two names for the same thing?

<details>
<summary>Answer</summary>

No. The controller is a driving adapter tied to HTTP. The input port is the application-owned capability it calls. Another adapter can call the same port without becoming an HTTP controller.

</details>

---

## 26. Clean Architecture and DDD

**⭐⭐⭐ NICE TO KNOW**

Clean Architecture and Domain-Driven Design solve related but different problems.

```text
Clean Architecture asks:
How should dependencies and boundaries protect policy?

DDD asks:
How should we model a complex business domain and its language?
```

DDD concepts can live inside Clean Architecture:

- entity: identity across time;
- value object: value-based, usually immutable concept;
- aggregate: consistency boundary changed together;
- repository: collection-like abstraction for aggregate persistence;
- domain service: domain operation that fits no single entity;
- bounded context: boundary within which a model and language are consistent.

But ports do not automatically create DDD:

```java
public interface CreateBookUseCase {
    Book create(CreateBookCommand command);
}
```

That is an application boundary. It says nothing by itself about aggregates, ubiquitous language, or domain richness.

### The current `Book` is pragmatic

The real `Book` is a JPA entity with state plus `replace` and `setStock`. It is not a deeply modeled aggregate, and the catalog does not currently demonstrate domain complexity that demands one.

Do not invent:

- `ISBNValueObjectFactoryStrategy`;
- an aggregate root hierarchy;
- domain events for every setter;
- a repository per entity by ritual;
- a bounded context diagram for three CRUD fields.

Introduce richer modeling when rules, language, and invariants require it. Clean boundaries can make that growth safer later.

### Repository vocabulary differs

A DDD repository expresses domain collection operations. A Spring Data `JpaRepository` is a framework abstraction with broad persistence capabilities. They may coincide in a simple system, but they are conceptually different. `BookRepositoryPort` in this slice is a narrow application output port, not proof of a full DDD repository.

---

## 27. Strict versus pragmatic Spring architecture

**⭐⭐⭐⭐⭐ MUST KNOW**

Architecture quality comes from explicit trade-offs, not maximum purity.

### Decision 1 — framework annotations in the application

Strict:

```java
public final class CreateBookService implements CreateBookUseCase {
    // plain Java; an outer @Configuration supplies the bean and transaction wrapper
}
```

Pragmatic:

```java
@Service
@Transactional
public class CreateBookService implements CreateBookUseCase {
}
```

Selected: pragmatic. `@Service` and `@Transactional` make composition and atomicity obvious to this Spring team. They do not expose HTTP or persistence operations.

### Decision 2 — one entity or two models

Strict separation:

```text
DomainBook ↔ mapper ↔ BookJpaEntity
```

Pragmatic sharing:

```text
Book with domain behavior + JPA annotations
```

Selected: one `Book`. A separate persistence model would add duplicate fields, identity/version mapping, conversion code, and synchronization risk without a current incompatible model requirement.

### Decision 3 — input ports for every service

Strict uniformity:

```text
every public service gets an interface
```

Pragmatic extraction:

```text
Create Book gets a port because this exercise evaluates that boundary;
unchanged simple operations keep their current service API.
```

Selected: one meaningful slice. Interfaces should represent boundaries, not satisfy a class-count rule.

### Decision 4 — exception types

Strict:

```text
every outer failure becomes an inner sealed error model
```

Pragmatic:

```text
translate the failure that changes application meaning;
let unexpected failures reach the generic safety handler.
```

Selected: translate duplicate ISBN because the API already promises `409`; do not create a hierarchy for every hypothetical SQL failure.

### Decision record for the exercise

| Choice | Selected | Why |
|---|---|---|
| Refactor scope | Create Book only | isolates learning and review cost |
| Domain/JPA model | existing `Book` | avoids unnecessary parallel model |
| Input model | separate command | protects HTTP/application boundary |
| Output port | one `save` capability | smallest use-case need |
| Transaction | application service annotation | atomic use-case boundary |
| Security | existing filter-chain rule | preserves exact HTTP behavior |
| Mapping | manual | five fields; transparent |
| Persistence | existing Spring Data repository behind adapter | reuse proven infrastructure |
| Database tests | real PostgreSQL | relevant DDL/constraint behavior |

Pragmatism is not “anything goes.” State the exceptions, keep arrows deliberate, and revisit a compromise when its cost becomes real.

### Checkpoint 15

Is a domain class with `@Entity` automatically an architectural failure?

<details>
<summary>Answer</summary>

No. It is ORM coupling. Decide whether eliminating that coupling is worth a separate model and mapping burden. For this small catalog it is not; for incompatible persistence and domain models it may be.

</details>

---

## 28. Switching infrastructure

**⭐⭐⭐⭐ IMPORTANT**

“We can switch databases easily” is often used to sell ports. Treat that claim precisely.

### What a good port actually helps

If `CreateBookService` depends only on:

```java
interface BookRepositoryPort {
    Book save(Book book);
}
```

then a different adapter can satisfy the use case:

```text
CreateBookService
    ↓ same port
    ├── BookPersistenceAdapter → PostgreSQL/JPA
    ├── InMemoryBookAdapter → deterministic local/test storage
    └── RemoteCatalogAdapter → remote catalog API
```

The service algorithm and input port need not import those technologies.

### What still changes

Switching PostgreSQL/JPA can require:

- a new adapter and its configuration;
- new concurrency and identity semantics;
- different transaction capabilities;
- different query expressiveness;
- new failure translation;
- replacement migrations;
- operational tooling;
- integration and performance tests;
- possibly a separate domain model if `Book` can no longer carry JPA annotations.

The port creates a controlled seam. It does not guarantee that two infrastructures have equal semantics.

### SQL-to-SQL versus SQL-to-document storage

```text
PostgreSQL → another relational SQL database
  likely keeps tables and transaction-shaped concepts
  but still changes dialect, identity, migrations, locking, types,
  constraint details, driver/configuration, and integration tests

PostgreSQL → MongoDB/document storage
  may change aggregate/document shape, joins, query capabilities,
  transaction boundaries, identifiers, consistency assumptions,
  indexes, migrations, adapter code, and possibly the port itself
```

The second change is not merely “write another adapter.” If the old port assumes relational semantics—for example, one atomic transaction across independently stored rows—the application contract or domain design may also need to change. A port reduces how far technology types propagate; it cannot manufacture semantic equivalence.

### A more common benefit than database replacement

Entire database engines are rarely swapped casually. More frequent benefits are:

- test the application without infrastructure;
- isolate a third-party SDK;
- change a vendor client version;
- support a batch or HTTP inbound adapter;
- prevent broad framework types from infecting use-case signatures;
- make ownership obvious during maintenance.

### Do not design to the lowest common denominator

Hiding every PostgreSQL capability behind generic methods can make the system worse. If a feature genuinely needs PostgreSQL full-text search or locking behavior, name that required semantic in a port and test the adapter. Abstraction should clarify a need, not deny reality.

---

## 29. External services

**⭐⭐⭐ NICE TO KNOW**

When a real use case needs email, object storage, payments, or another HTTP service, define a capability in application language:

```java
public interface BookNotificationPort {
    void bookCreated(long bookId, String title);
}
```

An outer adapter may use an HTTP client or mail library:

```text
CreateBookService → BookNotificationPort
                           ↑
                 EmailNotificationAdapter → mail provider
```

Use names that describe the required outcome, not the SDK:

```text
good:    PaymentAuthorizer
weaker:  StripeClientWrapper
good:    CoverImageStore
weaker:  S3Service
```

### The transaction boundary problem

A PostgreSQL transaction cannot roll back an email already sent or an HTTP request already accepted:

```text
BEGIN database transaction
INSERT book
send email successfully
COMMIT fails

Result: email says a book exists, but database row did not commit.
```

Nor should a slow network call hold a JDBC connection and database locks unnecessarily.

Practical options, chosen by reliability need:

- invoke a best-effort action after successful commit;
- save an outbox record in the same database transaction and publish later;
- use idempotency and retry policies;
- coordinate a multi-step workflow with a Saga at larger scale.

After-commit hooks, transactional outbox, and Sagas are **future knowledge** here. The important current rule is: an output port makes a dependency direction clean, but it does not make two systems atomically transactional.

### Resilience belongs near the adapter

Timeouts, connection settings, protocol error parsing, vendor retries, and circuit breakers are generally outer concerns. The application should receive a stable success/failure meaning. Be careful with automatic retries on non-idempotent operations.

### Do not add unused ports

The selected Book Catalog slice sends no email and publishes no message. Therefore the exercise adds neither port. A hypothetical adapter is still code that must be named, wired, tested, and maintained.

---

## 30. When not to use full Clean Architecture

**⭐⭐⭐⭐⭐ MUST KNOW**

Prefer the simpler layered design when most of these are true:

- the application is small and CRUD-oriented;
- business rules are limited;
- one HTTP adapter and one persistence technology are stable;
- integration tests are cheap enough;
- the team understands the direct framework APIs;
- changes usually cross controller, service, and repository together;
- ports would simply mirror `JpaRepository` one-for-one;
- extra navigation would cost more than isolation saves.

Introduce a stronger boundary when one or more pressures become real:

- application policy is valuable and changing independently;
- more than one inbound or outbound adapter is needed;
- infrastructure types are spreading through core code;
- testing important rules requires starting many unrelated systems;
- vendor APIs change frequently;
- a team needs enforceable ownership boundaries;
- transaction/use-case scope is unclear;
- the same business operation must be invoked outside HTTP.

### A proportionality test

For each proposed port, complete this sentence:

> “This boundary costs ___ classes/mappings, and protects ___ decision from ___ likely change while enabling ___ valuable test.”

If the last three blanks are vague, keep the direct dependency for now.

### Three valid architecture levels

```text
Level 1: Controller → Service → Spring Data
         best for simple CRUD

Level 2: selected use cases with ports/adapters
         best for uneven complexity and incremental evolution

Level 3: strongly isolated modules/domain and explicit composition
         best when scale, policy, or team boundaries repay it
```

The companion deliberately demonstrates Level 2.

---

## 31. Common mistakes

**⭐⭐⭐⭐⭐ MUST KNOW**

### 1. Renaming folders and declaring victory

```text
controller/ → adapter/in/
service/    → application/
repository/ → adapter/out/
```

If application classes still import controllers and `JpaRepository`, the dependency rule did not change.

### 2. Creating an interface for every class

An interface is valuable when it defines a boundary, supports alternate behavior, or stabilizes a contract. `BookService` plus `BookServiceImpl` with identical framework dependencies adds indirection without inversion.

### 3. Making an output port extend `JpaRepository`

That leaks Spring Data and its full surface inward. Keep the application-owned port focused on required capability.

### 4. Returning `Page`, `Pageable`, `ResponseEntity`, or `MultipartFile` from input ports

These types couple use-case APIs to framework or delivery choices. Use stable application pagination/value types when isolation is worth it; otherwise acknowledge the pragmatic coupling explicitly.

### 5. Letting the controller orchestrate the use case

```java
repository.save(first);
repository.save(second);
emailClient.send(...);
```

The controller should adapt HTTP. Application sequencing and atomicity belong at the use-case boundary.

### 6. Putting `@Transactional` on controllers

This makes HTTP processing own database atomicity and can hold connections during avoidable work. Place it around the application operation.

### 7. Assuming an adapter removes JPA behavior

Persistence context, dirty checking, flush, commit, lazy loading, locking, and detachment still apply behind the port.

### 8. Treating `save()` or `flush()` as commit

`save()` registers persistence work; flush emits synchronization SQL; commit completes the database transaction. A failure after flush can still roll everything back.

### 9. Returning managed entities everywhere without awareness

The selected project returns `Book` to the controller for mapping while the use-case call is active. With OSIV disabled, lazy relationships would not be available later. If the domain grows relationships, return a stable application result or map while required data is loaded.

### 10. Duplicating the model automatically

`Book`, `BookDomain`, `BookEntity`, `BookData`, and `BookDto` can be justified by incompatible responsibilities—but not merely by a diagram. Every duplicate requires reliable mapping.

### 11. Reusing one DTO everywhere

The opposite extreme couples JSON, application input, persistence, and output evolution. Separate types where ownership differs and change pressure is real.

### 12. Mapping in a global utility

A universal mapper tends to import all layers and hide boundary ownership. Map at the adapter that crosses the boundary.

### 13. Throwing HTTP exceptions from the application

Application exceptions should describe application meaning. The inbound HTTP adapter decides status and body.

### 14. Leaking raw persistence exceptions through the API

Clients should not learn Hibernate class names, SQL text, schema details, or stack traces. Translate expected failures and safely handle unexpected ones.

### 15. Catching exceptions and accidentally committing

Swallowing an unchecked exception inside a transactional method can make it return normally. Rethrow when failure must abort the operation.

### 16. Forgetting proxy behavior

Self-invocation can bypass `@Transactional` and `@PreAuthorize` interception. Design externally invoked use-case methods rather than relying on private/internal annotated calls.

### 17. Reading `SecurityContextHolder` throughout application code

It hides the caller as thread-local framework state. Supply explicit identity or an application-owned port when a real business rule needs it.

### 18. Moving business authorization entirely into URL rules

Route roles cannot express ownership, resource state, limits, or contextual permissions. Those belong with use-case policy.

### 19. Believing a port solves distributed transactions

An interface cannot roll back a sent email. Model after-commit delivery, outbox, idempotency, or workflow coordination when needed.

### 20. Keeping migrations only in `target/`

Build output is disposable. Authoritative Flyway history belongs under `src/main/resources/db/migration` and in version control.

### 21. Editing applied Flyway migrations

Changing V1 after environments applied it breaks history/checksums. Add a later migration unless restoring an exact accidentally missing source artifact.

### 22. Testing only with fakes

Fakes prove application behavior, not JPA mapping, PostgreSQL constraints, Flyway, transaction proxies, or SQL semantics. Test adapters with their real relevant technology.

### 23. Testing PostgreSQL behavior only with H2

H2 can differ in DDL, types, isolation, constraints, and SQL. Use PostgreSQL Testcontainers when PostgreSQL behavior is the subject.

### 24. Building generic CRUD ports

```java
interface RepositoryPort<T, ID> {
    T save(T value);
    Optional<T> findById(ID id);
    void delete(ID id);
}
```

This often recreates a weaker framework repository and loses use-case language. Design the smallest capability actually required.

### 25. Hiding useful database features for theoretical portability

If a use case needs a strong concurrency or query semantic, express and test it. Lowest-common-denominator abstractions can sacrifice correctness.

### 26. Refactoring all features at once

A mass folder move mixes architecture with behavior, produces large reviews, and makes regressions hard to locate. Move one vertical slice and verify its contract.

### 27. Changing security while changing architecture

If an architectural refactor also alters roles, CSRF, sessions, and endpoints, failures become difficult to attribute. Preserve behavior unless security is explicitly in scope.

### 28. Calling all outer code “infrastructure”

Controllers are adapters too, but they drive the application. Persistence and external clients are driven adapters. The distinction helps reason about direction.

### 29. Treating annotations as either harmless or forbidden

Each annotation carries a different dependency and runtime effect. Define the team's pragmatic boundary instead of arguing from purity slogans.

### 30. Ignoring the abstraction budget

Every port and model adds naming, wiring, files, tests, and navigation. Record the concrete benefit so the design remains teachable and maintainable.

### 31. Letting infrastructure own the port

Putting `BookRepositoryPort` beside the Spring Data adapter and shaping it around JPA makes the inner use case depend conceptually on an outer need. The consumer—the application—defines the capability; infrastructure implements it.

### 32. Creating circular module dependencies

If `application` imports `adapter`, while `adapter` imports `application`, neither boundary is independent. Break the cycle by moving a stable contract to the side that needs it, not by adding bean indirection or `@Lazy` to hide the source dependency.

### 33. Exposing a JPA entity as the public API DTO

The selected design shares `Book` inside the application boundary, but `BookController` still maps it to `BookResponse`. Returning a managed entity directly couples JSON to persistence fields, can trigger lazy-loading problems, and may expose new columns accidentally.

### 34. Creating one class and interface per trivial action without benefit

Splitting every getter into `FindTitleUseCase`, `FindTitleService`, and three adapters can make a small CRUD system harder to understand. Group cohesive operations or retain a conventional service until a real boundary or policy needs protection.

### 35. Treating Clean Architecture as mandatory

Clean Architecture is a tool for dependency and change risk, not a maturity badge. A conventional layered design can be the more maintainable choice. Apply stronger boundaries where their testability, ownership, or change-isolation benefit repays their cost.

### Checkpoint 16

Which is the more serious problem: a domain class annotated with `@Entity`, or an application service returning `ResponseEntity` and accepting `Pageable`?

<details>
<summary>Answer</summary>

It depends on the system, but the latter usually leaks delivery and framework policy more broadly through the application API. A JPA annotation is real coupling, yet a shared entity may be a deliberate low-cost compromise. Evaluate the change pressure and blast radius rather than counting annotations.

</details>

---

## 32. Trace Create Book completely

**⭐⭐⭐⭐⭐ MUST KNOW**

This section combines source dependencies, runtime calls, proxy behavior, transaction mechanics, persistence, security, validation, and HTTP translation.

### 32.1 Compile-time structure

```text
BookController
    ├── imports BookRequest / BookResponse       web-owned DTOs
    ├── imports CreateBookCommand                application input model
    └── depends on CreateBookUseCase             input port

CreateBookService
    ├── implements CreateBookUseCase
    ├── constructs/returns Book
    └── depends on BookRepositoryPort            output port

BookPersistenceAdapter
    ├── implements BookRepositoryPort
    ├── depends on existing BookRepository       Spring Data
    └── translates DataIntegrityViolationException

BookRepository
    └── extends JpaRepository<Book, Long>
```

The key inverted arrow is:

```text
application output port  ←imports/implements—  persistence adapter
```

The application does not import `BookPersistenceAdapter`.

### 32.2 Relevant code chain

Input contract:

```java
package com.example.bookcatalog.book.application.port.in;

import com.example.bookcatalog.model.Book;

public interface CreateBookUseCase {
    Book create(CreateBookCommand command);
}
```

Application implementation:

```java
package com.example.bookcatalog.book.application.service;

import com.example.bookcatalog.book.application.port.in.CreateBookCommand;
import com.example.bookcatalog.book.application.port.in.CreateBookUseCase;
import com.example.bookcatalog.book.application.port.out.BookRepositoryPort;
import com.example.bookcatalog.model.Book;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateBookService implements CreateBookUseCase {
    private final BookRepositoryPort bookRepository;

    public CreateBookService(BookRepositoryPort bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book create(CreateBookCommand command) {
        Book book = new Book(
                command.isbn(),
                command.title(),
                command.author(),
                command.price(),
                command.stock());
        return bookRepository.save(book);
    }
}
```

Output contract and adapter:

```java
package com.example.bookcatalog.book.application.port.out;

import com.example.bookcatalog.model.Book;

public interface BookRepositoryPort {
    Book save(Book book);
}
```

```java
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
```

Inbound adapter method:

```java
@PostMapping(consumes = "application/json", produces = "application/json")
public ResponseEntity<BookResponse> create(
        @Valid @RequestBody BookRequest request) {
    CreateBookCommand command = new CreateBookCommand(
            request.isbn(),
            request.title(),
            request.author(),
            request.price(),
            request.stock());

    Book book = createBookUseCase.create(command);

    return ResponseEntity
            .created(URI.create("/books/" + book.getId()))
            .body(BookResponse.from(book));
}
```

### 32.3 Successful request timeline

Assume an authenticated `ADMIN`, a valid session CSRF token, valid JSON, and a new ISBN.

```text
Client
  │ POST /books + session cookie + CSRF header + JSON
  ▼
SecurityFilterChain
  │ 1. loads authenticated session
  │ 2. verifies CSRF because POST is unsafe
  │ 3. verifies ADMIN authorization for /books write
  ▼
DispatcherServlet / Spring MVC
  │ 4. Jackson creates BookRequest
  │ 5. Jakarta Validation validates request
  ▼
BookController                         inbound adapter
  │ 6. maps BookRequest → CreateBookCommand
  ▼
Spring proxy for CreateBookUseCase
  │ 7. TransactionInterceptor begins transaction
  │ 8. JPA EntityManager/persistence context is associated
  ▼
CreateBookService                     application service
  │ 9. constructs transient Book
  │ 10. calls application-owned BookRepositoryPort
  ▼
BookPersistenceAdapter                outbound adapter
  │ 11. calls Spring Data saveAndFlush
  ▼
BookRepository runtime proxy
  │ 12. delegates to EntityManager/Hibernate
  ▼
Hibernate
  │ 13. makes the entity managed; IDENTITY may require an early INSERT,
  │     and saveAndFlush guarantees synchronization before returning
  ▼
JDBC Connection from HikariCP
  │ 14. executes INSERT in PostgreSQL transaction
  ▼
PostgreSQL
  │ 15. checks NOT NULL/UNIQUE and returns generated identity
  ▲
CreateBookService returns managed Book
  ▲
TransactionInterceptor
  │ 16. commits transaction
  │ 17. persistence context finishes; connection returns to pool
  ▲
BookController
  │ 18. maps Book → BookResponse
  │ 19. returns 201 Created, Location: /books/{id}, JSON body
  ▲
Client
```

### 32.4 State timeline: do not collapse the stages

```text
new Book(...)             Java object exists; no SQL yet
       ↓
repository.save(...)      entity becomes managed; with IDENTITY, Hibernate
                          may INSERT now to obtain the generated identifier
       ↓
flush                     guarantees pending SQL is synchronized now
       ↓
PostgreSQL executes       change is still uncommitted
       ↓
transaction commit        change becomes durable/visible per isolation
```

Therefore:

```text
Java state change
    ≠ persistence-context dirty tracking
    ≠ SQL generation
    ≠ flush
    ≠ database commit
```

For an update to an already-managed entity, no additional `save()` call is generally required:

```text
load managed Book → call book.setStock(...) → dirty checking → flush UPDATE → commit
```

The new port does not change that JPA mental model.

Exact SQL timing is provider and identifier-strategy dependent. This project uses `GenerationType.IDENTITY`, which commonly forces an INSERT during `persist`/`save` so Hibernate can obtain the generated ID. `saveAndFlush()` still guarantees synchronization before the adapter returns. Whether SQL ran during save or flush, **SQL execution is not commit**.

### 32.5 Duplicate ISBN failure timeline

```text
valid ADMIN POST reaches use case
  ↓
transaction begins
  ↓
saveAndFlush sends INSERT
  ↓
PostgreSQL UNIQUE constraint rejects ISBN
  ↓
Hibernate/JPA/Spring translate persistence failure
  ↓
BookPersistenceAdapter catches DataIntegrityViolationException
  ↓
throws DuplicateIsbnException
  ↓
unchecked exception exits transactional proxy
  ↓
transaction rolls back; connection returns to HikariCP
  ↓
GlobalExceptionHandler returns 409 Conflict
```

No book row commits. The `409` decision belongs to the HTTP exception adapter; the meaningful duplicate failure is exposed by the persistence adapter.

### 32.6 Invalid-request failure timeline

```text
POST /books with blank title
  ↓
security checks pass
  ↓
Jackson + Jakarta Validation
  ↓ validation fails
GlobalExceptionHandler returns 400
```

`CreateBookUseCase` is never called. No application transaction or JDBC connection is required for this rejected request.

### 32.7 Security failure timelines

```text
POST without valid CSRF
  ↓
SecurityFilterChain returns 403
  ↓
controller and use case are not called
```

```text
authenticated USER + valid CSRF + POST /books
  ↓
URL authorization returns 403
  ↓
controller and use case are not called
```

Architecture refactoring must preserve these early exits.

### 32.8 Other database failures—and a known translation limitation

With the adapter exactly as shown, **every** `DataIntegrityViolationException` caught during Create Book becomes `DuplicateIsbnException`. A future not-null or check-constraint violation of that same Spring exception type would therefore be mislabeled and mapped to `409`, not to the generic `500` path:

```text
other integrity constraint fails
  ↓
DataIntegrityViolationException
  ↓ broad current catch
DuplicateIsbnException
  ↓
409 (misleading if the failure was not ISBN uniqueness)
```

That broad mapping is retained only to preserve the current project behavior. As constraints grow, inspect reliable constraint identity or introduce narrower persistence operations/exceptions.

A failure outside that catch—for example, many connection/resource failures—follows the generic path:

```text
PostgreSQL unavailable / uncaught persistence failure
  ↓
exception exits adapter and transaction proxy
  ↓
transaction rolls back
  ↓
generic exception handler logs server-side details
  ↓
client receives safe 500 problem response
```

### 32.9 Ownership summary for one request

| Decision | Owner |
|---|---|
| POST route and JSON | `BookController` |
| session, CSRF, ADMIN route access | `SecurityFilterChain` configuration |
| request shape | `BookRequest` + web validation |
| Create Book capability | `CreateBookUseCase` |
| creation orchestration | `CreateBookService` |
| atomic transaction | application service boundary + Spring proxy |
| persistence capability shape | `BookRepositoryPort` |
| Spring Data and exception translation | `BookPersistenceAdapter` |
| ORM mapping/persistence context | `Book` mapping + Hibernate |
| connection reuse | HikariCP |
| uniqueness/durable commit | PostgreSQL |
| duplicate-to-409 translation | `GlobalExceptionHandler` |
| `201`, `Location`, response JSON | `BookController` |

### Checkpoint 17

In the successful path, which direction does control flow, and which direction does the persistence source dependency point?

<details>
<summary>Answer</summary>

Runtime control flows from controller through service and port into the persistence adapter. The persistence adapter's source code depends inward on the application-owned `BookRepositoryPort`; the service does not import the concrete adapter. Runtime calls and compile-time dependencies answer different questions.

</details>

---

## 33. Readiness and interview review

### One complete mental model

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
1. Start with a business capability, not a folder template.
2. Put its orchestration behind an application-owned input port when the boundary pays.
3. Let the use case depend only on stable models and capabilities it needs.
4. Express outside needs as focused output ports owned inward.
5. Implement those ports with outer adapters for JPA, HTTP clients, files, or messages.
6. Let Spring compose implementations and add proxy behavior at runtime.
7. Put the database transaction around the complete application operation.
8. Validate protocol shape, business rules, and stored integrity at their proper boundaries.
9. Translate failures as they cross technology/protocol boundaries.
10. Test inner policy cheaply and each adapter with its real relevant technology.
11. Preserve real security, transaction, ORM, pool, migration, and PostgreSQL semantics.
12. Pay abstraction cost only where a protected decision or useful test justifies it.
```

### Compact review sheet

| Term | Remember this |
|---|---|
| Dependency Rule | source dependencies point toward policy |
| Input port | application-owned capability offered to drivers |
| Output port | application-owned capability required from tools |
| Inbound adapter | translates an external request into an input-port call |
| Outbound adapter | implements an output port with a technology |
| Application service | coordinates one use case and usually its transaction |
| Domain | business concepts, state, rules, and behavior |
| Spring Boot | composes beans and starts runtime infrastructure |
| Spring proxy | adds transactions/security when calls cross the proxy |
| DTO | representation owned by an adapter/contract |
| Command | application-owned input for one use case |
| Persistence adapter | translates application persistence needs to Spring Data/JPA |
| Flyway | authoritative schema history in source |
| PostgreSQL | actual constraints, transactions, locks, and durable data |
| HikariCP | lends connections; long transactions hold scarce resources |
| Pragmatic architecture | explicit compromise with understood coupling/cost |

### Must-remember rules

- Dependency direction is about **source imports**, not only runtime call arrows.
- The consumer that needs a capability normally owns the port.
- Controllers adapt protocols; application services orchestrate use cases.
- `JpaRepository` is infrastructure even though it is an interface.
- A port should be smaller and more meaningful than the framework API it hides.
- A command is useful when it stabilizes the application boundary; it is not mandatory for every scalar.
- `@Transactional` normally belongs on an externally invoked application/use-case method.
- Proxy-based transactions and method security can be bypassed by self-invocation.
- `save()` is not commit; flush is not commit.
- A persistence adapter does not remove Hibernate's persistence-context rules.
- URL authorization and business authorization solve different problems.
- A database constraint remains necessary under concurrent writers.
- `target/` is never the authoritative home of Flyway migrations.
- Fake ports and PostgreSQL integration tests provide complementary evidence.
- Framework independence and database replaceability are gradients, not binary claims.
- Do not build an adapter, event, mapper, or port for a merely hypothetical future.
- A good architecture explains both its protections and its costs.

### Practical architecture-review checklist

- [ ] Can we name the use case in business language?
- [ ] Is the transaction boundary the complete atomic operation?
- [ ] Does the controller only translate HTTP and delegate?
- [ ] Are web DTOs kept out of the application API where isolation matters?
- [ ] Does each port have a concrete consumer and a small purpose?
- [ ] Does the outer adapter implement an inward-owned port?
- [ ] Are Spring Data and vendor types absent from protected application code?
- [ ] Is exception translation done at the boundary that understands both sides?
- [ ] Are protocol, business, and storage validation distinguished?
- [ ] Are security filter rules preserved and business authorization placed correctly?
- [ ] Are self-invocation/proxy limitations understood?
- [ ] Are migrations authoritative under `src/main/resources`?
- [ ] Does a pure test cover application behavior?
- [ ] Does a real PostgreSQL test cover the persistence adapter?
- [ ] Is the mapping cost proportionate to the isolation gained?
- [ ] Can the team state what intentionally remains coupled?
- [ ] Have unchanged endpoints and error contracts been verified?

### Readiness self-check

You are ready to use this architecture pragmatically when you can:

1. draw both runtime calls and compile-time dependencies;
2. explain why an interface belongs on one side of a boundary;
3. implement an input port, application service, output port, and adapter;
4. place transactions without moving them into HTTP;
5. preserve security and error semantics during a refactor;
6. test a use case without Spring and an adapter with real PostgreSQL;
7. explain why `@Entity` is retained or separated in a specific project;
8. identify when direct layered code is the better design;
9. trace one request down to JDBC and back to an HTTP response;
10. defend the architecture's cost in terms of an actual protected decision.

### Interview questions — answer before reading the key

1. What problem does Clean Architecture solve?
2. What is the Dependency Rule?
3. How do runtime calls differ from compile-time dependency arrows?
4. Who should own an output port, and why?
5. What is the difference between an input port and an inbound adapter?
6. What is the difference between an output port and an outbound adapter?
7. Does using `@Service` mean the design is not Clean Architecture?
8. Why use `CreateBookCommand` instead of passing `BookRequest` to the service?
9. Where should request-to-command and domain-to-response mapping occur?
10. Where should a Spring transaction boundary normally be placed?
11. Why can self-invocation break `@Transactional` or `@PreAuthorize` behavior?
12. Does `saveAndFlush()` commit a transaction?
13. Why translate `DataIntegrityViolationException` in the persistence adapter?
14. How do HTTP authorization and business authorization differ?
15. How can a use case receive current-user information without importing `SecurityContextHolder`?
16. Why do Flyway migrations belong in source rather than `target/`?
17. How is a port-based design different from ordinary layered architecture?
18. Is a JPA-annotated domain entity always wrong?
19. What does a pure application test with a fake output port prove—and not prove?
20. Why test this persistence adapter with PostgreSQL rather than only H2?
21. How are Clean, Hexagonal, Ports and Adapters, and Onion related?
22. Is Clean Architecture the same as DDD?
23. Does an output port make switching databases effortless?
24. When should you avoid a full ports-and-adapters design?
25. Trace a successful `POST /books` from the filter chain through commit and response.

==================================================
STOP — ANSWER THE INTERVIEW QUESTIONS BEFORE READING THE KEY
==================================================

### Interview answer key

#### 1. What problem does Clean Architecture solve?

It protects valuable policy from volatile delivery, framework, persistence, and vendor details by making boundaries and dependency direction explicit. It does not eliminate infrastructure or guarantee that every project needs many layers.

#### 2. What is the Dependency Rule?

Source-code dependencies should point inward toward more stable policy. Outer adapters may import application ports; protected application code should not import concrete outer adapters.

#### 3. How do runtime calls differ from compile-time dependency arrows?

At runtime a service calls an adapter object through a port. At compile time the service knows only the port, while the concrete adapter imports and implements that inward-owned interface. Control flow can go outward while source dependency points inward.

#### 4. Who should own an output port, and why?

The application side that needs the capability. Its needs define the smallest stable contract, and outer technology adapts to that contract instead of forcing its API inward.

#### 5. What is the difference between an input port and an inbound adapter?

An input port is an application-owned capability such as `CreateBookUseCase`. An inbound adapter, such as `BookController`, translates an external protocol into a call to that capability.

#### 6. What is the difference between an output port and an outbound adapter?

An output port declares what the application needs from the outside. An outbound adapter implements it using JPA, an HTTP client, a file system, or another technology.

#### 7. Does using `@Service` mean the design is not Clean Architecture?

No. It is a pragmatic Spring coupling. Evaluate whether policy leaks HTTP, Spring Data, and vendor details. A strict design can move composition outward, but the extra ceremony must earn its cost.

#### 8. Why use `CreateBookCommand` instead of passing `BookRequest` to the service?

The command gives the application its own stable input and keeps HTTP validation/serialization concerns in the web adapter. It can also group multiple values safely. It is not necessary for every one-value operation.

#### 9. Where should request-to-command and domain-to-response mapping occur?

In the inbound adapter, because it understands both the external representation and the application boundary. Domain-to-separate-persistence-model mapping, when present, belongs in the outbound persistence adapter.

#### 10. Where should a Spring transaction boundary normally be placed?

Around the complete application use case, usually on a public application-service method invoked through a Spring proxy. This keeps all required persistence steps atomic without extending the transaction over HTTP concerns.

#### 11. Why can self-invocation break `@Transactional` or `@PreAuthorize` behavior?

In default proxy mode, advice runs when a call crosses the proxy. A method calling another method on `this` directly reaches the target object and does not re-enter the proxy for the inner annotation.

#### 12. Does `saveAndFlush()` commit a transaction?

No. It asks Hibernate to synchronize pending state with the database by executing SQL. The transaction manager can still roll back; commit occurs after the transactional operation completes successfully.

#### 13. Why translate `DataIntegrityViolationException` in the persistence adapter?

The adapter understands both persistence vocabulary and the application meaning. It can expose a stable `DuplicateIsbnException`, while the HTTP adapter independently maps that meaning to `409`.

#### 14. How do HTTP authorization and business authorization differ?

HTTP authorization governs routes, methods, sessions, roles, and CSRF before the controller. Business authorization depends on use-case facts such as ownership, resource state, limits, or actor relationship and belongs with application policy.

#### 15. How can a use case receive current-user information without importing `SecurityContextHolder`?

The inbound adapter can map stable actor values into a command, or the application can own a `CurrentUserPort` implemented by a Spring Security adapter. Add either only when the use case actually needs identity.

#### 16. Why do Flyway migrations belong in source rather than `target/`?

`target/` is disposable generated output and is deleted by clean builds. Versioned source migrations are the authoritative, reviewable schema history from which every environment must be reproducible.

#### 17. How is a port-based design different from ordinary layered architecture?

Both can separate controllers, services, and persistence. Ports additionally let the application own its contracts so concrete outer implementations depend inward. This buys isolation and test seams at the cost of more types and mappings.

#### 18. Is a JPA-annotated domain entity always wrong?

No. It couples the model to JPA, but sharing one model may be the best trade-off in a small system. Separate models when persistence shape and domain shape diverge enough to repay mapping cost.

#### 19. What does a pure application test with a fake output port prove—and not prove?

It proves application construction, decisions, and collaboration with the declared port. It does not prove Spring proxies, transactions, entity mapping, SQL, Flyway, PostgreSQL constraints, or adapter translation.

#### 20. Why test this persistence adapter with PostgreSQL rather than only H2?

The relevant contract includes PostgreSQL DDL, identity, unique constraints, exception behavior, and Flyway scripts. H2 can differ in exactly those semantics. A PostgreSQL container gives representative evidence.

#### 21. How are Clean, Hexagonal, Ports and Adapters, and Onion related?

They are overlapping architectural families that protect inner policy and separate external mechanisms. They use different diagrams and vocabulary, so apply the dependency and ownership ideas rather than treating the names as perfectly identical standards.

#### 22. Is Clean Architecture the same as DDD?

No. Clean Architecture focuses on boundaries and dependency direction. DDD focuses on modeling a complex domain and its language. DDD models can be protected by Clean boundaries, but either can be used without fully adopting the other.

#### 23. Does an output port make switching databases effortless?

No. It localizes dependencies and may preserve the use-case API, but schema, transactions, identity, query behavior, operations, exception mapping, and tests can still change. It creates a seam, not semantic equivalence.

#### 24. When should you avoid a full ports-and-adapters design?

When the system is small, stable, CRUD-heavy, cheaply testable, and each port would merely mirror a framework API. Use the simplest architecture that keeps current risks understandable and evolve boundaries where pressure appears.

#### 25. Trace a successful `POST /books` from the filter chain through commit and response.

The security chain loads the session, verifies CSRF and ADMIN access; MVC deserializes and validates `BookRequest`; the controller maps it to `CreateBookCommand`; a transaction proxy invokes `CreateBookService`; the service constructs `Book` and calls `BookRepositoryPort`; the JPA adapter uses Spring Data/Hibernate; SQL runs through a Hikari-borrowed JDBC connection against PostgreSQL; the proxy commits; the connection returns to the pool; the controller maps the book and returns `201`, `Location`, and JSON.

### Final architecture and cross-cutting placement

```text
                                      OUTSIDE
┌────────────────────────────────────────────────────────────────────────────┐
│ Spring Boot composition                                                   │
│ - component scanning, configuration, bean wiring, runtime startup          │
│                                                                            │
│  HTTP client                                                               │
│      │                                                                     │
│      ▼                                                                     │
│  SecurityFilterChain ── session / authentication / CSRF / URL roles        │
│      │                                                                     │
│      ▼                                                                     │
│  BookController + BookRequest/BookResponse          INBOUND ADAPTER        │
│      │ request validation and mapping                                      │
│      ▼                                                                     │
│  ┌────────────────────── APPLICATION BOUNDARY ─────────────────────────┐   │
│  │ CreateBookUseCase              INPUT PORT                           │   │
│  │          ▲                                                          │   │
│  │          │ implemented by                                          │   │
│  │ CreateBookService              USE CASE                            │   │
│  │ - creation orchestration                                            │   │
│  │ - @Transactional boundary, applied by an outer Spring proxy          │   │
│  │ - depends on BookRepositoryPort                                     │   │
│  │                         │                                            │   │
│  │                         ▼                                            │   │
│  │                  BookRepositoryPort        OUTPUT PORT              │   │
│  └─────────────────────────▲────────────────────────────────────────────┘   │
│                            │ implements / source dependency points inward  │
│  BookPersistenceAdapter ───┘                        OUTBOUND ADAPTER        │
│  - Spring Data call                                                      │
│  - persistence exception translation                                     │
│      │                                                                     │
│      ▼                                                                     │
│  BookRepository proxy → EntityManager → Hibernate                         │
│      │                     persistence context / dirty checking / flush     │
│      ▼                                                                     │
│  JDBC Connection ↔ HikariCP                                               │
│      │                                                                     │
│      ▼                                                                     │
│  PostgreSQL                                                               │
│  - transaction, constraints, locks, indexes, durable commit                │
│  - schema history supplied by source-controlled Flyway migrations          │
│                                                                            │
│  GlobalExceptionHandler: application/web failures → safe HTTP responses    │
│  Logging/metrics: observe boundaries; do not become business policy        │
│                                                                            │
│  TESTING AROUND THE BOUNDARIES                                              │
│  - pure unit test → CreateBookService + fake BookRepositoryPort             │
│  - web/security test → controller, validation, CSRF, roles, HTTP mapping    │
│  - adapter test → Flyway + JPA/Hibernate + real PostgreSQL Testcontainer    │
└────────────────────────────────────────────────────────────────────────────┘

Compile-time rule: outer adapters import inner ports; inner policy does not
import concrete outer adapters. Runtime rule: Spring wires the graph and its
proxies, then real Hibernate, JDBC, HikariCP, and PostgreSQL semantics apply.
```

