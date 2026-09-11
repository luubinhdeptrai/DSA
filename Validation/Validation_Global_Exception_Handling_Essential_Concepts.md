# Validation + Global Exception Handling — Essential Concepts

> A first-principles guide to turning invalid input and application failures into predictable REST responses without mixing HTTP, business, and persistence responsibilities.

**Example baseline:** Java 21 project, Spring Boot 4.1.1, Spring Framework 7.0.9, Jakarta Validation 3.1.1, Hibernate Validator 9.1.3.Final, Spring MVC, `JdbcTemplate`, HikariCP, and PostgreSQL 17.  
**Companion baseline:** [REST API Essential Concepts](../REST_API/REST_API_Essential_Concepts.md) and the existing [Book Catalog REST API mini-project](../REST_API/REST_API_Mini_Project.md).  
**Learning goal:** Decide which layer detects each failure, express suitable input rules declaratively, and publish a small, stable error contract without disguising server failures as client mistakes.

The earlier REST project intentionally used controller-local checks and `ResponseStatusException`. Those choices made the first HTTP contract visible before adding another framework layer. This guide evolves that model; it does not replace the project, its routes, its JDBC repository, or its database.

## How to study

1. Read each lifecycle from left to right and name the owner at every arrow.
2. Before opening a checkpoint answer, predict whether the controller method executes.
3. Treat every validation annotation as a rule over an already-created Java value.
4. Keep four questions separate: Can JSON be read? Is the DTO structurally acceptable? Is the operation allowed? Can the database preserve the state?
5. Use the companion exercise to replace one local responsibility at a time.
6. Verify status, headers, body, and database effect—not only the text of an exception.

Code fragments isolate one idea unless explicitly described as a complete type. They use `jakarta.validation.*`, not the pre-Jakarta `javax.validation.*` namespace. The complete implementation belongs in the companion exercise.

## Big picture

The goal is not “more annotations.” It is a request boundary whose failures remain as deliberate as its successful responses:

```text
routing → argument resolution → Jackson → Bean Validation → controller
        → service/application → repository/JdbcTemplate → PostgreSQL → response

failure at any stage
        → normal Java propagation + Spring MVC exception resolution
        → status + relevant headers + safe ProblemDetail body
```

Each rule belongs at the earliest boundary that can enforce it accurately, while durable database constraints remain in place. The rest of the guide explains those boundaries and the places where similarly shaped failures are not interchangeable.

## Concept priorities

| Priority | Meaning | What you should be able to do |
|---|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Required for a predictable Spring REST boundary | Explain it and implement the basic pattern |
| ⭐⭐⭐⭐ IMPORTANT | Needed to avoid subtle production bugs | Apply it and diagnose mistakes |
| ⭐⭐⭐ NICE TO KNOW | Useful extension of the core model | Recognize when it becomes valuable |
| ⭐⭐ FUTURE KNOWLEDGE | Belongs later in the roadmap | Understand the purpose without adding it now |

### Navigation

- [1. Why the controller-local approach stops scaling](#1-why-the-controller-local-approach-stops-scaling)
- [2. The request and failure lifecycle](#2-the-request-and-failure-lifecycle)
- [3. Jakarta Validation, Hibernate Validator, Spring, and Boot](#3-jakarta-validation-hibernate-validator-spring-and-boot)
- [4. Core constraints and their null semantics](#4-core-constraints-and-their-null-semantics)
- [5. DTO validation, `@Valid`, nesting, and collections](#5-dto-validation-valid-nesting-and-collections)
- [6. The Book price rule and justified custom validation](#6-the-book-price-rule-and-justified-custom-validation)
- [7. Request-body validation versus path and query validation](#7-request-body-validation-versus-path-and-query-validation)
- [8. Validation, business rules, and database integrity](#8-validation-business-rules-and-database-integrity)
- [9. Exceptions in a Spring REST application](#9-exceptions-in-a-spring-rest-application)
- [10. `@ExceptionHandler` and `@RestControllerAdvice`](#10-exceptionhandler-and-restcontrolleradvice)
- [11. A consistent Problem Details error contract](#11-a-consistent-problem-details-error-contract)
- [12. Framework request failures and status choices](#12-framework-request-failures-and-status-choices)
- [13. Complete lifecycle and infrastructure boundaries](#13-complete-lifecycle-and-infrastructure-boundaries)
- [14. Anti-patterns](#14-anti-patterns)
- [Final mental model](#final-mental-model)
- [20-minute review cheat sheet](#20-minute-review-cheat-sheet)
- [Readiness checklist](#readiness-checklist)

---

## 1. Why the controller-local approach stops scaling

**⭐⭐⭐⭐⭐ MUST KNOW**

The existing controller contains checks such as:

```java
private static void checkStock(Integer stock) {
    if (stock == null || stock < 0) {
        throw badRequest("stock is required and must be nonnegative");
    }
}
```

This is ordinary Java and was a reasonable bridge. It made three facts explicit:

- missing or explicit `null` stock becomes Java `null` because the DTO uses `Integer`;
- negative integers deserialize successfully but violate the API contract;
- the controller currently chooses HTTP 400.

The problem is not that an `if` statement is inherently wrong. The problem is accumulated responsibility and repetition. Both POST and PUT need the same five field rules; PATCH repeats the stock rule; every operation maps absence; more database conflicts would add more `try/catch` blocks.

```text
Controller
├── routing
├── JSON-bound arguments
├── repeated validation logic
├── exception mapping
├── status selection
├── service calls
└── DTO mapping
```

As the application grows, this creates predictable failure modes:

- one route forgets a rule;
- equivalent invalid inputs receive different messages;
- a new maximum is changed in POST but not PUT;
- controller tests must exercise business and persistence error translation;
- `catch` blocks tempt developers to turn unrelated failures into 400 or 409;
- the actual success orchestration becomes hard to see.

The desired separation is:

```text
Request DTO
    ↓ declares structural input constraints
Bean Validation
    ↓ rejects invalid Java values
Controller
    ↓ performs HTTP orchestration
Service/application
    ↓ enforces use-case and current-state rules
Repository/database
    ↓ performs persistence and protects stored integrity

Escaping exception
    ↓
Global REST advice
    ↓ translates a known failure into status + headers + safe body
```

This does not remove validation from the system. It moves each rule to the boundary that owns it.

### Before and after in the Book Catalog

```text
BEFORE

POST /books
    ↓
BookController
├── checkBook(...)
├── trim text
├── catch DuplicateKeyException
├── throw ResponseStatusException
└── call BookService

AFTER

POST /books
    ↓
Jackson creates BookRequest
    ↓
Bean Validation checks BookRequest
    ↓
BookController calls BookService
    ↓
known exceptions escape
    ↓
GlobalExceptionHandler publishes a consistent HTTP error
```

The successful contract stays unchanged: POST still returns `201 Created`, a `Location` header, and `BookResponse`; GET stays 200; PUT and PATCH stay 200; DELETE stays an empty 204.

### Checkpoint 1

Does replacing `checkStock(...)` with `@NotNull` and `@PositiveOrZero` remove the stock rule? Should the repository now return `ResponseEntity` because errors are centralized?

<details>
<summary>Answer</summary>

No to both. The rule remains, but its declaration moves to the request DTO and its execution moves to validation infrastructure. The repository still owns persistence, not HTTP. Centralization means exception-to-HTTP translation belongs in advice, not that HTTP types move down the stack.

</details>

---

## 2. The request and failure lifecycle

**⭐⭐⭐⭐⭐ MUST KNOW**

Validation does not inspect raw network bytes. Spring MVC and Jackson must first produce a Java value that can be validated.

```text
HTTP request
    ↓
Spring MVC routing and argument resolution
    ↓
HTTP message converter selects JSON support
    ↓
Jackson deserializes JSON
    ↓
Jakarta Validation evaluates constraints
    ↓
Controller method
    ↓
Service/application rules
    ↓
Repository / JdbcTemplate
    ↓
PostgreSQL constraints
    ↓
response construction and serialization
```

A failure stops this path at the boundary that detects it. Later boundaries do not run.

### 2.1 Seven failure categories

| Category and Book example | First detecting layer | Typical Java/Spring failure | Controller method executes? | Status in this project | Global translation? |
|---|---|---|---|---|---|
| A. Malformed JSON: `{"stock": }` | Jackson through the request-body converter | `HttpMessageNotReadableException` | No | 400 | Customize the safe body; preserve the framework status |
| B. Well-formed JSON, incompatible Java type: `{"stock":"many"}`, object instead of integer, or `1.5` under the strict integer policy | Jackson | Usually `HttpMessageNotReadableException`, often wrapping a Jackson conversion cause | No | 400 | Yes, but do not expose the parser cause |
| C. Valid Java DTO, violated constraint: blank title or negative stock | Jakarta Validation invoked by MVC | `MethodArgumentNotValidException` or `HandlerMethodValidationException`, depending on the method signature | No, unless a deliberate adjacent `BindingResult` changes the flow | 400 | Yes; extract safe field/parameter messages |
| D. Valid DTO, disallowed operation/current state | Service/application | A meaningful application exception | Yes | Contract-specific; often 409, sometimes another deliberate status | Yes, when the public contract defines it |
| E. Valid positive ID, book absent | Service/application after repository result | `BookNotFoundException` | Yes | 404 | Yes |
| F. Database integrity conflict: duplicate ISBN | PostgreSQL, translated by Spring JDBC | `DuplicateKeyException`, then preferably `DuplicateIsbnException` | Yes | 409 | Translate only the recognized conflict |
| G. Unexpected bug or database failure | Any executing layer | `NullPointerException`, an unrecognized `DataAccessException`, etc. | Maybe | 500 | Never relabel as 400; expose only a generic safe detail |

Categories A and B are not Bean Validation failures. No `BookRequest` exists in A; an incompatible field prevents a usable `BookRequest` in B.

Category C starts only after deserialization succeeds:

```text
JSON syntax valid
    ↓
types compatible
    ↓
BookRequest exists
    ↓
constraints evaluated
```

### 2.2 “Did the controller execute?” is a diagnostic question

If JSON is malformed, the method argument cannot be constructed, so MVC cannot call a method that requires it. If the DTO is constructed but rejected by validation, validation normally occurs before invocation, so the controller still does not execute.

If the controller calls the service and the service discovers a missing book, controller execution has begun. The exception then unwinds the ordinary Java call stack.

```text
BookRepository throws/returns absence
        ↑
BookService gives it application meaning
        ↑
BookController call cannot complete
        ↑
DispatcherServlet exception resolution
        ↓
@RestControllerAdvice
        ↓
HTTP response
```

An `Errors` or `BindingResult` parameter immediately following a validated argument can let a controller inspect certain validation failures locally. That is useful for form-style flows, but this REST project deliberately lets failures escape to global advice so all endpoints share one error contract.

### Checkpoint 2

For `{"stock":"abc"}`, does `@PositiveOrZero` reject the value? For `{"stock":-1}`, does Jackson reject it?

<details>
<summary>Answer</summary>

No to both. Jackson cannot create an `Integer` from the incompatible string in the first request, so Bean Validation never sees a stock value. Negative one is a valid JSON number and Java integer, so Jackson succeeds; Bean Validation then rejects it.

</details>

---

## 3. Jakarta Validation, Hibernate Validator, Spring, and Boot

**⭐⭐⭐⭐⭐ MUST KNOW**

“Bean Validation” is the familiar historical name. Jakarta Validation 3.1 uses the shorter specification name, but developers still commonly use both phrases.

```text
Jakarta Validation 3.1.1
    ↓ specification, annotations, and API contracts
Hibernate Validator 9.1.3.Final
    ↓ provider that implements those contracts
Spring Framework 7.0.9
    ↓ integrates a Validator with MVC and method invocation
Spring Boot 4.1.1
    ↓ supplies compatible dependencies and auto-configuration
Your code
    ↓ declares constraints and defines the public error contract
```

### 3.1 Specification versus provider

Jakarta Validation defines concepts such as:

- `@NotNull`, `@Size`, and the other standard constraint annotations;
- `@Valid` cascading;
- `Validator`, `ConstraintViolation`, and executable validation contracts;
- how custom constraints are declared.

An API alone does not evaluate annotations. A **validation provider** performs that work. Hibernate Validator is the reference implementation and the common provider used by Spring Boot.

### 3.2 Hibernate Validator is not Hibernate ORM

The shared “Hibernate” name is a frequent source of confusion:

| Technology | Purpose | Used now? |
|---|---|---|
| Hibernate Validator | Implements Jakarta Validation constraints | Yes |
| Hibernate ORM | Maps objects to relational persistence for JPA/ORM | No; that is a later topic |

Adding Hibernate Validator does not turn `Book` into an entity, generate repository queries, or replace `JdbcTemplate`. No JPA annotation or persistence context is involved.

### 3.3 Dependency setup for this project

The checked-in project uses the Spring Boot 4.1.1 parent and compiles with Java 21. Add one managed starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Do not put versions on the starter, `jakarta.validation-api`, or Hibernate Validator. Boot 4.1.1 manages the compatible set used here:

| Component | Managed baseline |
|---|---|
| Spring Framework | 7.0.9 |
| Jakarta Validation API | 3.1.1 |
| Hibernate Validator | 9.1.3.Final |
| Project Java release | 21; Boot 4 itself supports Java 17+ |

The validation starter places the API and provider on the classpath. When the provider is present, Boot configures validation infrastructure including a Spring `Validator` backed by a `LocalValidatorFactoryBean`; Spring MVC can then invoke it for controller arguments. Boot also enables proxy-based method validation infrastructure for eligible Spring beans.

Boot provides and connects infrastructure. It does not decide:

- which fields are required;
- whether a duplicate ISBN is a conflict;
- which error properties your clients receive;
- whether a particular application exception means 404 or 409.

Useful verification after adding the dependency:

```powershell
mvn dependency:tree '-Dincludes=org.springframework.boot:spring-boot-starter-validation,org.hibernate.validator:hibernate-validator,jakarta.validation:jakarta.validation-api'
```

The source imports remain `jakarta.validation.Valid` and `jakarta.validation.constraints.*`. Examples using `javax.validation.*` target the old namespace and should not be copied into this Boot 4 project.

Current primary references for this version-sensitive behavior are Spring Boot's [validation reference](https://docs.spring.io/spring-boot/reference/io/validation.html), Spring MVC's [controller-method validation reference](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html), the [Jakarta Validation 3.1 specification](https://jakarta.ee/specifications/bean-validation/3.1/jakarta-validation-spec-3.1), and the [Hibernate Validator 9.1 reference](https://docs.jboss.org/hibernate/validator/9.1/reference/en-US/html_single/).

### Checkpoint 3

If `@NotBlank` compiles after adding only `jakarta.validation-api`, is validation guaranteed to execute? Does adding Hibernate Validator also add Hibernate ORM?

<details>
<summary>Answer</summary>

No to both. The API supplies types, while a provider evaluates them; Spring integration must also invoke that provider at the relevant boundary. Hibernate Validator is a validation provider and is separate from Hibernate ORM.

</details>

---

## 4. Core constraints and their null semantics

**⭐⭐⭐⭐⭐ MUST KNOW**

The most important general rule is:

```text
Most value constraints treat null as valid.
Requiredness is usually a separate constraint.
```

This separation is deliberate. “A value may be absent” and “when present, the value must be positive” are different rules.

```java
@NotNull
@PositiveOrZero
Integer stock;
```

`@PositiveOrZero` checks the sign when a value exists. `@NotNull` makes existence mandatory.

The tables below use **supported value types**, not merely Java locations where an annotation may be written. Standard constraints can generally be declared on fields, properties, constructor/method parameters, return values, record components through their applicable generated elements, and type uses as permitted by each annotation.

### 4.1 Presence and size constraints

| Constraint | What it checks | Supported value types | `null` | Common misunderstanding | Simple example |
|---|---|---|---|---|---|
| `@NotNull` | Value exists | Any reference type | Invalid | It does not reject `""`, whitespace, zero, or an empty list; primitives can never be null | `@NotNull Integer stock` |
| `@NotEmpty` | Value exists and length/size is greater than zero | `CharSequence`, `Collection`, `Map`, arrays | Invalid | It does not treat whitespace-only text as empty | `@NotEmpty List<String> tags` |
| `@NotBlank` | Text exists and contains at least one non-whitespace character | `CharSequence` | Invalid | It checks text content but does not mutate or trim the stored value | `@NotBlank String title` |
| `@Size(min, max)` | Inclusive character count, collection/map size, or array length | `CharSequence`, `Collection`, `Map`, arrays | Valid | It does not check numeric magnitude, and `@Size(max=20)` alone allows null | `@Size(max = 20) String isbn` |

For required Book text, the usual pair is:

```java
@NotBlank(message = "title is required")
@Size(max = 200, message = "title must have at most 200 characters")
String title
```

`@NotBlank` already rejects null, empty, and whitespace-only values. `@Size` expresses the independent maximum.

### 4.2 `@NotNull` versus `@NotEmpty` versus `@NotBlank`

For `String` values:

| Value | `@NotNull` | `@NotEmpty` | `@NotBlank` |
|---|---|---|---|
| `null` | Reject | Reject | Reject |
| `""` | Accept | Reject | Reject |
| `"   "` | Accept | Accept | Reject |
| `"Java"` | Accept | Accept | Accept |

Use the narrowest statement that matches the contract:

- any object merely required: `@NotNull`;
- required nonempty collection/array/map: `@NotEmpty`;
- required human text: usually `@NotBlank`;
- bounded length or collection size: add `@Size`.

Do not stack all three on every string. `@NotBlank` plus the required `@Size` bound communicates the Book fields clearly.

### 4.3 Integral bounds

| Constraint | What it checks | Supported value types in Jakarta Validation | `null` | Common misunderstanding | Simple example |
|---|---|---|---|---|---|
| `@Min(long)` | Numeric value is at least the integral threshold | `BigDecimal`, `BigInteger`, `byte`, `short`, `int`, `long`, and wrappers; float/double are not standard-supported here | Valid | It is not string length and its threshold is a Java `long` | `@Min(0) int page` |
| `@Max(long)` | Numeric value is at most the integral threshold | Same standard types as `@Min` | Valid | It does not describe decimal precision | `@Max(100) int size` |

`@Min` and `@Max` are ideal for page and size because their boundaries are integers. Although `BigDecimal` is supported, `@DecimalMin` and `@DecimalMax` express decimal thresholds more naturally and exactly as strings.

### 4.4 Sign constraints

| Constraint | What it checks | Supported value types | `null` | Common misunderstanding | Simple example |
|---|---|---|---|---|---|
| `@Positive` | Strictly greater than zero | `BigDecimal`, `BigInteger`, numeric primitives/wrappers including float and double | Valid | Zero is invalid; add `@NotNull` for a required wrapper | `@Positive long id` |
| `@PositiveOrZero` | Greater than or equal to zero | Same numeric family | Valid | It accepts zero and accepts null | `@NotNull @PositiveOrZero Integer stock` |
| `@Negative` | Strictly less than zero | Same numeric family | Valid | Rare in this catalog; it is not a “non-positive” check | `@Negative BigDecimal adjustment` |
| `@NegativeOrZero` | Less than or equal to zero | Same numeric family | Valid | It accepts zero and null | `@NegativeOrZero int debitDelta` |

The negative constraints are useful when a domain represents signed deltas, temperatures, or accounting adjustments. They are not needed merely to demonstrate every annotation in the Book project.

### 4.5 Decimal boundaries and digit shape

| Constraint | What it checks | Supported value types in the specification | `null` | Common misunderstanding | Simple example |
|---|---|---|---|---|---|
| `@DecimalMin(value, inclusive)` | Value is above, or optionally equal to, an exact decimal threshold | `BigDecimal`, `BigInteger`, `CharSequence`, integral primitives/wrappers | Valid | The threshold is a string to preserve decimal exactness; it does not impose scale | `@DecimalMin("0.00") BigDecimal price` |
| `@DecimalMax(value, inclusive)` | Value is below, or optionally equal to, an exact decimal threshold | Same standard family as `@DecimalMin` | Valid | It limits numeric value, not number of written fractional digits | `@DecimalMax("9999999999.99") BigDecimal price` |
| `@Digits(integer, fraction)` | Number has no more than the configured integral and fractional digits | `BigDecimal`, `BigInteger`, `CharSequence`, integral primitives/wrappers | Valid | It validates representation/digit counts; it does not round. With Hibernate Validator, a `BigDecimal`'s scale makes trailing zeros significant to this check | `@Digits(integer = 5, fraction = 2) BigDecimal rate` |

Use `@PositiveOrZero` when the concept is sign. Use `@DecimalMin` when the exact boundary or inclusive/exclusive choice communicates the rule better. Stacking equivalent constraints only creates duplicate messages.

### 4.6 Format constraints

| Constraint | What it checks | Supported value types | `null` | Common misunderstanding | Simple example |
|---|---|---|---|---|---|
| `@Pattern(regexp)` | A `CharSequence` matches a Java regular expression | `CharSequence` | Valid | It does not make the value required, and a complex regex is not automatically a good domain model | `@Pattern(regexp = "[A-Z]{2}-\\d{4}") String code` |
| `@Email` | Provider-defined well-formed email syntax, with optional additional regex | `CharSequence` | Valid | It does not prove the address exists, can receive mail, or belongs to the caller | `@NotBlank @Email String email` |
| `@Valid` | Cascades validation into an object's constrained properties | Object references and container element type uses | No object is traversed when null | It is not itself a constraint, does not mean required, and does not deserialize JSON | `@NotNull @Valid AddressRequest address` |

Other standard constraints include `@Null`, `@AssertTrue`, `@AssertFalse`, and past/future date constraints. Recognize them and use them when a real contract needs them; the Book API does not benefit from artificial examples.

### 4.7 Messages

Every constraint has a `message` attribute:

```java
@NotBlank(message = "title is required")
String title
```

Three layers are involved:

```text
constraint message template
    ↓ provider resolves/interpolates it
validation error object
    ↓ advice selects safe public information
API error JSON
```

Direct messages are clearest for this beginner project. Reusable keys in `ValidationMessages.properties` and locale-specific bundles become useful when messages are shared or localized. Internationalization is future knowledge here.

Do not blindly expose rejected values. A password, token, personal identifier, or unexpectedly large input can be sensitive. Field name plus a controlled message is enough for this exercise.

### Checkpoint 4

Does `@Size(max = 20)` reject a null ISBN? Does `@PositiveOrZero` reject null stock? Does `@NotBlank` trim the title before the service receives it?

<details>
<summary>Answer</summary>

No to all three. `@Size` and `@PositiveOrZero` consider null valid, so required values need a presence constraint. `@NotBlank` evaluates text but does not mutate it. Normalization is a separate operation.

</details>

---

## 5. DTO validation, `@Valid`, nesting, and collections

**⭐⭐⭐⭐⭐ MUST KNOW** for request DTOs and `@Valid`. **⭐⭐⭐ NICE TO KNOW** for nesting and container elements.

A request DTO is a natural input boundary because it describes exactly what an HTTP client may send. It is not a database entity and does not need persistence annotations.

### 5.1 Conceptual Book request constraints

The existing `BookRequest` can evolve toward:

```java
package com.example.bookcatalog.dto;

import com.example.bookcatalog.validation.AtMostTwoDecimalPlaces;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record BookRequest(
        @NotBlank(message = "isbn is required")
        @Size(max = 20, message = "isbn must have at most 20 characters")
        String isbn,

        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must have at most 200 characters")
        String title,

        @NotBlank(message = "author is required")
        @Size(max = 120, message = "author must have at most 120 characters")
        String author,

        @NotNull(message = "price is required")
        @PositiveOrZero(message = "price must be nonnegative")
        @DecimalMax(value = "9999999999.99",
                message = "price must be at most 9999999999.99")
        @AtMostTwoDecimalPlaces(
                message = "price must have at most two meaningful decimal places")
        BigDecimal price,

        @NotNull(message = "stock is required")
        @PositiveOrZero(message = "stock must be nonnegative")
        Integer stock) {
}
```

The custom price constraint is justified in section 6. Do not substitute `@Digits(integer = 10, fraction = 2)` without first understanding the trailing-zero behavior.

The stock-only request stays narrow:

```java
public record StockRequest(
        @NotNull(message = "stock is required")
        @PositiveOrZero(message = "stock must be nonnegative")
        Integer stock) {
}
```

Using `Integer` remains important. A primitive `int` would turn missing input into the default zero before validation, making omitted stock indistinguishable from an explicitly valid zero.

### 5.2 Validation does not normalize

The previous contract measures text after trimming and stores normalized values. `@NotBlank` can reject whitespace-only input, but it does not change `"  Java  "` into `"Java"`; `@Size` counts the value it receives.

Choose and document one normalization boundary. For example, a compact record constructor can normalize before MVC validates the completed DTO:

```java
public BookRequest {
    isbn = normalize(isbn);
    title = normalize(title);
    author = normalize(author);
}

private static String normalize(String value) {
    return value == null ? null : value.trim();
}
```

Alternatively, preserve normalization in one application mapping method. Do not silently change from “maximum after trimming” to “maximum before trimming” merely because `@Size` is convenient.

### 5.3 `@RequestBody` and `@Valid` perform different work

```java
public ResponseEntity<BookResponse> create(
        @Valid @RequestBody BookRequest request) {
    // Runs only after body conversion and this validation path succeed.
}
```

Mental model:

```text
@RequestBody
    ↓ select a message converter
JSON bytes → BookRequest

@Valid
    ↓ ask the configured validator to cascade into BookRequest
BookRequest → constraint results
```

`@RequestBody` does not run Bean Validation by itself. `@Valid` cannot repair malformed JSON because there is no DTO to validate.

### 5.4 Nested validation

Suppose another API accepts an address:

```java
public record AddressRequest(
        @NotBlank String street,
        @NotBlank String city) {
}

public record OrderRequest(
        @NotNull
        @Valid
        AddressRequest shippingAddress) {
}
```

`@Valid` on `shippingAddress` tells the provider to traverse into `AddressRequest`. `@NotNull` independently says the nested object must exist. Without `@Valid`, the nested annotations are not automatically traversed from the outer object. Without `@NotNull`, null has nothing to traverse and does not fail merely because `@Valid` is present.

The Book request is flat, so do not add nesting merely to demonstrate it.

### 5.5 Container and collection validation

Constraints may target a container and its elements separately:

```java
public record TagsRequest(
        @NotNull
        @Size(max = 10)
        List<@NotBlank @Size(max = 30) String> tags) {
}
```

```java
public record BatchRequest(
        @NotEmpty
        List<@Valid BookRequest> books) {
}
```

The first `@Size` limits the number of tags; the type-use constraints validate each string. In the second example, `@Valid` cascades into each book. This is useful knowledge, but the current API accepts one book per write and needs no batch endpoint.

### Checkpoint 5

If an `OrderRequest` has `shippingAddress = null` and the component has only `@Valid`, is that a validation failure? Does `@Valid @RequestBody` mean validation happens before JSON deserialization?

<details>
<summary>Answer</summary>

No to both. Add `@NotNull` when the nested value is required. Jackson must first construct the outer request; only then can validation cascade through it.

</details>

---

## 6. The Book price rule and justified custom validation

**⭐⭐⭐⭐ IMPORTANT**

The Book API accepts a price when all of these are true:

- it exists;
- it is nonnegative;
- it is no greater than `9999999999.99`, matching the useful range of `NUMERIC(12,2)`;
- its numeric value needs at most two fractional decimal places;
- numerically harmless trailing zeros are allowed, so `19.9900` is accepted like `19.99`.

The first three rules map cleanly to standard constraints:

```java
@NotNull
@PositiveOrZero
@DecimalMax("9999999999.99")
BigDecimal price
```

The final rule needs care.

### 6.1 The `@Digits` trailing-zero trap

This looks attractive:

```java
@Digits(integer = 10, fraction = 2)
BigDecimal price
```

For Hibernate Validator's `BigDecimal` validator, the fractional digit count follows the value's `scale()`. Jackson can construct these numerically equal values with different scales:

```text
JSON       BigDecimal concept       scale     @Digits(... fraction=2)
19.99      19.99                    2         pass
19.9900    19.9900                  4         fail
19.9990    19.9990                  4         fail
```

The first two prices are numerically equal according to `BigDecimal.compareTo(...)`, and the established API explicitly accepts the trailing-zero form. Therefore `@Digits(integer = 10, fraction = 2)` would silently narrow the existing contract.

`@Digits` is not broken. It answers a different question: does this represented number fit the configured integral/fractional digit shape? It does not promise to ignore trailing zeros and it never rounds.

### 6.2 Meaningful fractional scale

For this contract, normalize only for the scale decision:

```text
19.9900.stripTrailingZeros()  → 19.99   → scale 2 → accept
19.9990.stripTrailingZeros()  → 19.999  → scale 3 → reject
1000.00.stripTrailingZeros()  → 1E+3    → scale -3 → accept
```

A small custom constraint communicates that exact rule.

```java
package com.example.bookcatalog.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AtMostTwoDecimalPlacesValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE,
        ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AtMostTwoDecimalPlaces {
    String message() default "must have at most two meaningful decimal places";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

```java
package com.example.bookcatalog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class AtMostTwoDecimalPlacesValidator
        implements ConstraintValidator<AtMostTwoDecimalPlaces, BigDecimal> {

    @Override
    public boolean isValid(
            BigDecimal value,
            ConstraintValidatorContext context) {
        return value == null || value.stripTrailingZeros().scale() <= 2;
    }
}
```

The custom validator deliberately treats null as valid. `@NotNull` owns requiredness, following the same composable convention as most standard constraints.

### 6.3 When a custom constraint is appropriate

Use one when:

- a real, reusable structural rule is not represented faithfully by standard constraints;
- its name makes the DTO contract clearer;
- validation needs more than one property and a class-level rule is genuinely structural.

Do not create one when a standard constraint already says the same thing. A custom “nonnegative stock” annotation would obscure `@PositiveOrZero` without adding meaning.

A cross-field constraint can be useful for a request such as `startDate <= endDate`. Its validator receives the whole DTO and its annotation usually targets `TYPE`. That is **⭐⭐⭐ NICE TO KNOW**; the flat Book request has no cross-field rule requiring it.

Never implement “unique ISBN” by querying the database from a `ConstraintValidator`. It introduces I/O into structural validation and still loses a race between the check and insert. Database uniqueness belongs in PostgreSQL, with a known conflict translated afterward.

### Checkpoint 6

Why is `@Digits(integer = 10, fraction = 2)` not used for this Book price? Should `AtMostTwoDecimalPlacesValidator` return false for null?

<details>
<summary>Answer</summary>

Hibernate Validator counts a `BigDecimal`'s scale, so it can reject `19.9900` even though the established contract accepts its numeric value. The custom validator should accept null and let `@NotNull` report requiredness; otherwise one absent value can produce overlapping messages from unrelated rules.

</details>

---

## 7. Request-body validation versus path and query validation

**⭐⭐⭐⭐⭐ MUST KNOW**

Controller inputs reach Java through different argument resolvers. Validating a request DTO and constraining a scalar method parameter are related but not identical Spring MVC paths.

### 7.1 Individual request-object validation

With no direct method constraints elsewhere in the signature:

```java
@PostMapping
public ResponseEntity<BookResponse> create(
        @Valid @RequestBody BookRequest request) {
    // ...
}
```

MVC validates this command object individually. A failure normally raises `MethodArgumentNotValidException`. It contains a `BindingResult` with object and field errors for that one argument.

```text
BookRequest created
    ↓
@Valid cascade
    ↓ failure
MethodArgumentNotValidException
    ↓
controller method is not invoked
```

### 7.2 MVC-native method validation

Direct constraints on controller method parameters trigger method validation:

```java
@GetMapping("/{id}")
public BookResponse findById(
        @PathVariable("id") @Positive(message = "id must be positive") long id) {
    // ...
}
```

```java
@GetMapping
public List<BookResponse> findAll(
        @RequestParam(required = false)
        @Size(max = 120) String author,
        @RequestParam(defaultValue = "0")
        @Min(0) @Max(1_000_000) int page,
        @RequestParam(defaultValue = "20")
        @Min(1) @Max(100) int size) {
    // ...
}
```

Spring Framework 7 provides built-in validation for `@RequestMapping` methods. Input constraint failures normally raise `HandlerMethodValidationException`. Its results are grouped by method parameter and can be visited by parameter kind such as `@PathVariable`, `@RequestParam`, or `@RequestBody`.

Do **not** place class-level `@Validated` on this controller. That switches the controller to older AOP proxy method validation and prevents use of MVC's native method-validation path. For the current Spring MVC model:

```text
Controller
    → no class-level @Validated
    → MVC validates mapped method parameters natively
```

### 7.3 The mixed-signature rule that changes the exception

Consider PUT:

```java
@PutMapping("/{id}")
public BookResponse replace(
        @PathVariable("id") @Positive long id,
        @Valid @RequestBody BookRequest request) {
    // ...
}
```

The direct `@Positive` constraint requires method validation. Method validation supersedes the otherwise individual request-body validation and covers both:

- the direct ID constraint;
- nested `BookRequest` constraints reached through `@Valid`.

Therefore an invalid ID, an invalid body, or both can surface as `HandlerMethodValidationException` for this signature—not `MethodArgumentNotValidException` for the body.

```text
create(@Valid @RequestBody BookRequest)
    ↓ no direct parameter constraint
MethodArgumentNotValidException on invalid body

replace(@Positive @PathVariable long,
        @Valid @RequestBody BookRequest)
    ↓ direct constraint activates method validation for the method
HandlerMethodValidationException for ID and/or nested body failures
```

A robust global handler supports both exception families. Do not write code that works for POST body errors and then assumes PUT body errors must arrive through the same exception.

`@Valid` is a cascading marker, not a constraint. By itself it does not trigger method-level validation. A direct `@NotNull`, `@Positive`, `@Min`, or other constraint does.

### 7.4 Return-value validation is a server failure

MVC method validation can also validate a controller return value. `HandlerMethodValidationException` reports input validation with 400, but a return-value violation represents the server breaking its own declared contract and uses 500. Do not flatten both into 400 merely because the exception class is the same.

### 7.5 Optional query values need optional-compatible constraints

The Book `author` filter is optional but rejects blank input when supplied. `@NotBlank` would also reject null, making absence invalid. One simple option is a null-accepting pattern plus a size bound:

```java
@RequestParam(required = false)
@Pattern(regexp = ".*\\S.*", message = "author must not be blank")
@Size(max = 120, message = "author must have at most 120 characters")
String author
```

Both constraints accept null. If the value exists, it must include a non-whitespace character and fit the bound. Remember that neither constraint trims it; preserve the project's normalization decision separately.

### 7.6 Conversion still comes before constraint validation

`GET /books/not-a-number` cannot produce a `long id`. Argument conversion fails before `@Positive` can inspect the value. Likewise `page=abc` is a conversion failure, while `page=-1` converts to `int` and then violates `@Min(0)`.

| Input | Stops at | Typical exception |
|---|---|---|
| `/books/abc` for `long id` | String-to-number conversion | `MethodArgumentTypeMismatchException` |
| `/books/0` with `@Positive` | MVC method validation | `HandlerMethodValidationException` |
| `?page=abc` for `int page` | String-to-number conversion | `MethodArgumentTypeMismatchException` |
| `?page=-1` with `@Min(0)` | MVC method validation | `HandlerMethodValidationException` |

### 7.7 `@Valid` versus `@Validated`

| Annotation | Namespace | Main role | Groups | Use here |
|---|---|---|---|---|
| `@Valid` | `jakarta.validation.Valid` | Cascade into the annotated object or elements | Uses the default group; does not select groups | Request DTOs and nested values |
| `@Validated` | `org.springframework.validation.annotation.Validated` | Spring variant that can select validation groups; also marks bean types for proxy-based method validation | Yes | Service method validation when deliberately needed, not class-level on MVC controllers |

Validation groups let one DTO apply different subsets of constraints for different operations:

```java
@Validated(CreateChecks.class)
```

They are **⭐⭐⭐ NICE TO KNOW** here. POST and PUT share the same complete `BookRequest` contract, and PATCH already has its own narrow `StockRequest`; separate DTOs are clearer than a group hierarchy.

### 7.8 Service method validation uses Spring AOP proxies

MVC controllers have native method validation. An ordinary service bean is different. Boot can apply executable validation through a proxy when the service type is marked `@Validated`:

```java
@Service
@Validated
public class InventoryService {

    public Book setStock(
            @Positive long id,
            @PositiveOrZero int stock) {
        // ...
    }
}
```

```text
controller calls Spring proxy
    ↓ proxy validates method arguments
target service method executes only on success
```

This connects to your Spring Core/AOP knowledge:

- the caller must enter through the Spring-managed proxy;
- self-invocation such as `this.otherValidatedMethod()` bypasses proxy interception;
- proxy limitations still apply;
- by default, violations commonly surface as `ConstraintViolationException`; Spring can alternatively adapt them to `MethodValidationException` when configured.

Do not duplicate every HTTP constraint at the service merely because method validation exists. Use service validation when the service has callers beyond this controller or when the method contract independently requires it.

### Checkpoint 7

Which exception should you prepare for when POST has only `@Valid @RequestBody`? What about PUT with `@Positive @PathVariable` and `@Valid @RequestBody`? Should the controller class be annotated `@Validated`?

<details>
<summary>Answer</summary>

POST normally produces `MethodArgumentNotValidException`. The direct ID constraint on PUT activates MVC method validation for the whole method, so ID and nested body errors can produce `HandlerMethodValidationException`. Do not put class-level `@Validated` on the controller; reserve it for deliberate proxy-based validation on service beans.

</details>

---

## 8. Validation, business rules, and database integrity

**⭐⭐⭐⭐⭐ MUST KNOW**

These are complementary layers, not interchangeable names for “checking data.”

```text
Bean Validation
    = properties of an input value

Business/application rules
    = whether an operation is allowed or meaningful now

Database constraints
    = which persistent states the database will accept
```

### 8.1 Placement decision table

| Rule/failure | Primary owner | Defensive overlap | Why |
|---|---|---|---|
| Title must not be blank | Request DTO | PostgreSQL `NOT NULL` plus nonblank `CHECK` | Give clients early field feedback and protect stored state from every write path |
| Title maximum 200 characters | Request DTO | `VARCHAR(200)` | Public representation and storage both have the bound |
| Stock must be at least zero | Request DTO for write input | Service for stateful stock operations; PostgreSQL `CHECK (stock >= 0)` | Input correctness and persistent integrity overlap legitimately |
| Price is required, bounded, and has meaningful scale ≤ 2 | Request DTO, including the custom scale rule | PostgreSQL `NUMERIC(12,2)`, `NOT NULL`, and nonnegative check | Reject predictable input before SQL while retaining storage enforcement |
| JSON is malformed | Jackson/MVC | None in service/database | No Java DTO exists |
| Path ID must be positive | MVC method parameter | Service may reject invalid IDs for non-HTTP callers | It is a scalar boundary contract |
| Positive ID does not identify a book | Service/application | Repository reports absence | Absence has application meaning; the repository should not choose HTTP 404 |
| ISBN must be unique | PostgreSQL unique constraint | Optional application messaging and known exception translation | Only the database can arbitrate concurrent writes reliably |
| Operation forbidden by current book state | Service/domain | Database constraint if the invariant can and should be stored | The rule depends on existing state, not DTO shape alone |
| SQL syntax, connectivity, or unexpected database failure | Persistence infrastructure detects it | Advice keeps public detail generic | It is a server failure, not invalid client input |

The controller should contain little rule logic after this refactor. It still owns HTTP orchestration: binding annotations, success status and headers, response mapping, and calling the service.

### 8.2 Why database constraints still matter

An “is this ISBN free?” pre-check is vulnerable to a race:

```text
Request A: SELECT ISBN → free
Request B: SELECT ISBN → free
Request A: INSERT ISBN → succeeds
Request B: INSERT ISBN → database UNIQUE constraint rejects it
```

Without `UNIQUE (isbn)`, both requests can insert after both pre-checks observe absence. Bean Validation has the same limitation: it validates one object, not an atomic future database state.

The database is the final arbiter because it coordinates concurrent changes at the persistence boundary. A pre-check can sometimes improve user experience, but it cannot replace the constraint and still must handle the insert race.

### 8.3 Known conflict translation

Spring translates a unique-key violation from JDBC into `DuplicateKeyException`, a persistence-oriented exception. The service can give the known operation application meaning:

```java
public Book create(BookRequest request) {
    try {
        return repository.insert(/* mapped values */);
    }
    catch (DuplicateKeyException exception) {
        throw new DuplicateIsbnException(exception);
    }
}
```

This translation is safe only when the operation and schema make the violated key understood. `DuplicateKeyException` means a duplicate key—not automatically “duplicate ISBN” in every table and future schema. Preserve the cause for diagnostics, and do not catch every `DataAccessException` as 409.

```text
known ISBN unique conflict
    ↓
DuplicateKeyException
    ↓ service recognizes operation meaning
DuplicateIsbnException
    ↓ web advice
409 Conflict

connection outage / broken SQL / unknown constraint
    ↓
server failure
    ↓
500, not 409
```

### Checkpoint 8

Can Bean Validation replace `UNIQUE (isbn)` if a validator queries the table first? Should every `DataAccessException` become 409?

<details>
<summary>Answer</summary>

No to both. Two requests can both pass a pre-check before either insert commits; the database constraint is the concurrent arbiter. Only a deliberately recognized state conflict maps to 409. Connectivity errors, broken SQL, and unrelated persistence failures remain server failures.

</details>

---

## 9. Exceptions in a Spring REST application

**⭐⭐⭐⭐⭐ MUST KNOW**

Global handling builds on normal Java exception propagation. Spring does not create a separate law of exceptions.

```text
Repository
    ↓ returns absence or throws a persistence exception
Service
    ↓ gives known outcomes application meaning
Controller
    ↓ does not catch when it cannot complete normally
exception escapes handler invocation
    ↓
DispatcherServlet delegates to HandlerExceptionResolver chain
    ↓
@ExceptionHandler method is selected
    ↓
status + headers + error body
```

### 9.1 Expected versus unexpected failures

An **expected application failure** is a known negative outcome in the public contract:

- a valid ID identifies no book;
- an ISBN conflicts with existing catalog state;
- a valid request asks for an operation forbidden by current state.

An **unexpected failure** means the program or infrastructure did not complete as designed:

- a null dereference;
- malformed SQL;
- database connectivity loss;
- a serialization bug.

Expected does not mean “successful.” It means the application deliberately understands and translates the outcome. Unexpected does not mean “show the exception to the client.” It means log diagnostic detail server-side and return a safe 500 response.

### 9.2 Small, meaningful application exceptions

```java
public final class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(long id) {
        super("Book " + id + " was not found");
    }
}
```

```java
public final class DuplicateIsbnException extends RuntimeException {
    public DuplicateIsbnException(Throwable cause) {
        super("ISBN already exists", cause);
    }
}
```

These exceptions describe application meaning without importing `HttpStatus`, `ResponseEntity`, or servlet types. The web layer decides that absence is 404 and this conflict is 409.

The public response does not have to repeat `exception.getMessage()`. The internal message can help logs, while the advice returns a controlled detail such as `"Book not found"` or `"ISBN already exists"`.

Avoid an exception class for every minor conditional. Create one when callers need to distinguish and handle that application outcome.

### 9.3 Where translation belongs

| Layer | Receives/knows | May translate to | Must not own |
|---|---|---|---|
| Repository | SQL/JDBC outcome | Domain values, absence, or Spring data-access exception | HTTP status/body |
| Service/application | Use-case meaning | Small application exception | `ResponseEntity` and content negotiation |
| Web advice | Escaping exception and HTTP request context | HTTP status, headers, safe representation | SQL, connection cleanup, business decisions |

Throwing `ResponseStatusException` from a repository would make persistence code depend on one delivery mechanism. The same repository might later be called from a batch job with no HTTP response.

### Checkpoint 9

If the service throws `BookNotFoundException`, which ordinary Java mechanism gets it back to Spring MVC? Should its message automatically become the public `detail`?

<details>
<summary>Answer</summary>

Normal stack unwinding propagates the unchecked exception through the controller invocation. Spring MVC's exception resolvers then look for a matching handler. The advice should choose a safe public detail deliberately; it should not blindly expose every exception message.

</details>

---

## 10. `@ExceptionHandler` and `@RestControllerAdvice`

**⭐⭐⭐⭐⭐ MUST KNOW**

### 10.1 The three related annotations

```text
@ControllerAdvice
    = shared advice applied across controllers

@RestControllerAdvice
    = @ControllerAdvice + response-body semantics

@ExceptionHandler
    = a method that handles selected exception types
```

`@RestControllerAdvice` is a Spring-managed bean stereotype. Component scanning discovers the advice class; Spring MVC's exception-resolution infrastructure inspects its handler methods.

```text
component scanning
    ↓
GlobalExceptionHandler bean
    ↓ registered with MVC exception resolution
escaped controller exception
    ↓ matching @ExceptionHandler
converted response body
```

The advice does not wrap every controller in handwritten `try/catch`, and it does not intercept arbitrary failures outside the MVC request pipeline such as an error in a process that never reaches `DispatcherServlet`.

### 10.2 `@ExceptionHandler` mechanics

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleBookNotFound(
            BookNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "Book not found");
        problem.setTitle("Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }
}
```

The annotation declares which exception type the method handles. The method can accept the exception and useful web context, and may return `ProblemDetail`, `ResponseEntity<?>`, or another supported response value. `ResponseEntity` is useful when headers as well as status and body matter.

Exception matching respects type inheritance. Prefer focused, non-overlapping handlers and the most meaningful application type. Do not make clients depend on a low-level cause such as `PSQLException`.

### 10.3 Local, global, and inline handling

| Pattern | Appropriate use | Main tradeoff |
|---|---|---|
| `try/catch` inside a controller method | Recovery local to that operation, not ordinary reusable status translation | Repetition and mixed success/error orchestration |
| Controller-local `@ExceptionHandler` | A truly controller-specific policy | Not shared with other controllers |
| Global `@RestControllerAdvice` | Common REST error contract and application exception mappings | Must remain focused; broad policies affect every controller |
| Framework defaults | Failures whose default status/body is acceptable | Body may not match an application-specific contract |

The Book API has repeated missing-resource and duplicate-conflict behavior, so global advice is the natural next step.

### 10.4 Do not confuse centralization with catch-all conversion

This is dangerous:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleEverything(Exception exception) {
    return ResponseEntity.badRequest().body(exception.getMessage());
}
```

It turns programming bugs, database outages, and serialization failures into false client blame. It also leaks internal messages.

If the application later chooses a final `Exception.class` safety net, it must:

- run after more specific mappings;
- log the complete exception server-side;
- return 500, never 400;
- use a generic public detail;
- avoid recursive attempts to handle a failure while rendering the error itself.

A catch-all is optional for this learning project. Unknown failures already have 500 semantics; consistency must not come at the cost of false classification.

### Checkpoint 10

Does `@RestControllerAdvice` close a JDBC connection after a repository failure? Should `catch (Exception)` make every failed request return the same 400 body?

<details>
<summary>Answer</summary>

No to both. JDBC resource ownership remains with `JdbcTemplate`, the `DataSource`, and transaction infrastructure. Advice translates failures at the HTTP boundary. Unknown failures remain 500 and must not be mislabeled as invalid input.

</details>

---

## 11. A consistent Problem Details error contract

**⭐⭐⭐⭐ IMPORTANT**

Clients should not need a different parser for a blank title, a missing book, and a duplicate ISBN. This project uses Spring's `ProblemDetail`, the framework representation for the Problem Details standard, rather than inventing a large error hierarchy.

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more request values are invalid.",
  "instance": "/books",
  "fieldErrors": {
    "title": ["title is required"],
    "stock": ["stock must be nonnegative"]
  }
}
```

The standard members have stable jobs:

| Member | Meaning in this API |
|---|---|
| `type` | Optional identifier for the problem kind; when omitted, Problem Details semantics default it to `about:blank` |
| `title` | Short category such as `Validation failed` or `Conflict` |
| `status` | HTTP status repeated in the representation |
| `detail` | Safe explanation of this occurrence |
| `instance` | Request URI when Spring supplies it; the application does not add one manually |
| `fieldErrors` | Project extension used only when locations make the response actionable |

Why use `Map<String, List<String>>` instead of one string per field? One value can violate more than one constraint. A missing price might accidentally produce overlapping messages if custom constraints handle null incorrectly; another field can legitimately fail both shape and size rules. Collecting into a list prevents a duplicate map key from making the error handler itself fail.

For request-object validation, Spring's `FieldError` exposes the field name, the rejected value, and the resolved constraint message through `getField()`, `getRejectedValue()`, and `getDefaultMessage()`. That does not mean all three belong in public JSON. This handler selects the field and controlled message; it intentionally does not copy `getRejectedValue()` into the response. A narrowly designed API could echo a demonstrably safe small value, but a reusable global default should assume values may be sensitive.

The public contract includes field names and controlled constraint messages. It deliberately excludes rejected values, stack traces, SQL, credentials, exception class names, and arbitrary `exception.getMessage()` text. Even an apparently harmless rejected value can later become a password, token, or large payload.

`ProblemDetail` is a representation, not a status-selection algorithm. Advice still chooses or preserves the correct status and relevant headers. On successful requests, the existing `BookResponse` contract remains unchanged.

Extending `ResponseEntityExceptionHandler` is useful in this Spring 7 application because it already understands standard MVC exceptions. Selected protected hooks can customize the body, while inherited behavior preserves framework decisions such as `Allow` for 405 and supported-media-type headers for 415.

### Checkpoint 11

Why can `fieldErrors` contain a list for one field? Should it contain the rejected ISBN or stock value? Does using `ProblemDetail` decide whether an error is 400 or 500?

<details>
<summary>Answer</summary>

A field can have several violations, so the representation must not lose or collide on a second message. Do not echo rejected values. `ProblemDetail` shapes the body; the detected failure and application contract determine the status.

</details>

---

## 12. Framework request failures and status choices

**⭐⭐⭐⭐⭐ MUST KNOW** for the distinctions and statuses. **⭐⭐⭐ NICE TO KNOW** for memorizing exact exception class names.

You do not need to memorize Spring MVC's entire exception taxonomy. You do need to identify the stage, because that tells you whether validation ran and whether the controller could have executed.

| Failure and Book example | Detected by | Typical Spring 7 exception | Controller entered? | Status | Customize now? |
|---|---|---|---:|---:|---|
| Malformed JSON, such as `{"stock": }` | Jackson/message conversion | `HttpMessageNotReadableException` | No | 400 | Yes: use a generic safe body |
| Incompatible JSON type or shape, such as `{"stock":"many"}` or `[3]` | Jackson/message conversion | `HttpMessageNotReadableException` | No | 400 | Yes: same safe body |
| Missing required request body | MVC request-body resolution | Usually `HttpMessageNotReadableException` | No | 400 | Yes through the same hook |
| Invalid standalone `@Valid @RequestBody` | Bean Validation through MVC | `MethodArgumentNotValidException` | No | 400 | Yes: publish field/object messages |
| Invalid direct path/query constraint, or a body on a method with direct constraints | MVC-native method validation | `HandlerMethodValidationException` | No | 400 for input; 500 for an invalid return value | Yes: process bean and value results, preserving Spring's status |
| Nonnumeric `id`/`page` for a numeric parameter | MVC conversion | `MethodArgumentTypeMismatchException`, handled by the `TypeMismatchException` hook | No | 400 | Yes: sanitize conversion detail |
| Missing required query parameter | MVC argument resolution | `MissingServletRequestParameterException` | No | 400 | Inherited Problem Details is sufficient unless the public contract needs different wording |
| Unsupported request `Content-Type` | MVC message-converter selection | `HttpMediaTypeNotSupportedException` | No | 415 | Usually inherit; preserve supported-media-type headers |
| Unsupported HTTP method | MVC handler mapping | `HttpRequestMethodNotSupportedException` | No | 405 | Inherit and preserve `Allow` |
| Client refuses available response formats | MVC content negotiation | `HttpMediaTypeNotAcceptableException` | Maybe | 406 | Inherit; a JSON error body may itself be unacceptable |

There are two especially important body-validation paths:

```text
POST create(@Valid @RequestBody BookRequest request)
    → invalid DTO → MethodArgumentNotValidException

PUT replace(@Positive @PathVariable long id,
            @Valid @RequestBody BookRequest request)
    → method validation covers direct ID and cascaded body
    → HandlerMethodValidationException
```

For `HandlerMethodValidationException`, Spring 7 exposes cascaded bean results through `getBeanResults()` and direct scalar results through `getValueResults()`. A handler that reads only one collection loses either DTO field messages or path/query messages. It should also retain object/cross-parameter messages under a stable key such as `_request`.

### 12.1 Choosing 400, 404, 409, and 500

| Status | Meaning here | Book example |
|---:|---|---|
| 400 Bad Request | The request cannot satisfy the public input contract | Blank title, negative stock, malformed JSON, `id=abc`, `id=0` |
| 404 Not Found | The request is structurally valid, but the addressed book is absent | `GET /books/999` after a successful positive-ID lookup |
| 409 Conflict | The request is valid in isolation but conflicts with current stored state | PostgreSQL rejects a known duplicate ISBN |
| 500 Internal Server Error | The application or infrastructure failed unexpectedly | Programming error, broken SQL, database outage, invalid validated return value |

Status is only one part of a REST response. Preserve relevant headers and choose a safe body as well. A 405 without its `Allow` header or a 415 that discards supported media-type information is a degraded contract even when the number is correct.

Known failures receive deliberate translations. Unknown failures should be logged with diagnostic detail on the server and returned as a generic 500. The client-facing detail and the server log serve different audiences.

```text
client: "An unexpected error occurred."
server log: exception type + stack trace + contextual diagnostics
```

Never turn unknown failures into 400. That blames the client, hides defects from monitoring, and teaches callers to retry or edit a request that was not the cause.

### Checkpoint 12

Does `{"stock":1.5}` reach `@PositiveOrZero` when `accept-float-as-int` is false? Should a return-value `HandlerMethodValidationException` be forced to 400? Can a 406 response always contain JSON Problem Details?

<details>
<summary>Answer</summary>

No. Jackson rejects the fractional value for `Integer` first. Preserve the status supplied by Spring: an invalid server return value is 500. A client that explicitly refuses JSON may also refuse `application/problem+json`, so a 406 body can be absent.

</details>

---

## 13. Complete lifecycle and infrastructure boundaries

**⭐⭐⭐⭐⭐ MUST KNOW**

The complete success-and-failure path is:

```text
Client
  ↓ HTTP request
Tomcat
  ↓
DispatcherServlet
  ↓ routing + handler/argument resolution
Jackson deserializes request JSON
  ├── malformed/incompatible
  │      ↓ HttpMessageNotReadableException
  │   exception resolution → advice → 400 ProblemDetail
  │
  └── Java request value exists
         ↓
      Bean Validation
         ├── DTO/direct input violation
         │      ↓ MethodArgumentNotValidException
         │        or HandlerMethodValidationException
         │   exception resolution → advice → 400 ProblemDetail
         │
         └── valid invocation
                ↓
             Controller
                ↓
             Service/application
                ├── required book absent
                │      ↓ BookNotFoundException
                │   advice → 404 ProblemDetail
                │
                └── Repository / JdbcTemplate
                       ↓ borrow pooled connection through DataSource/HikariCP
                    PostgreSQL
                       ├── known ISBN unique conflict
                       │      ↓ DuplicateKeyException
                       │   service → DuplicateIsbnException
                       │   advice → 409 ProblemDetail
                       │
                       ├── unexpected failure
                       │      ↓ resource cleanup + exception propagation
                       │   server log + advice/default handling → 500
                       │
                       └── success
                              ↓ result maps upward
                           Controller builds status + headers + BookResponse
                              ↓ response serialization
                           Client
```

### 13.1 Connection and exception ownership stay separate

An SQL failure does not hand a connection to `@RestControllerAdvice`:

```text
JdbcTemplate executes callback
    ↓ SQL throws
JdbcTemplate/DataSource cleanup releases the connection
    ↓ translated exception propagates
service/application may recognize meaning
    ↓
web advice creates an HTTP response
```

HikariCP owns reusable pooled connections; `JdbcTemplate` owns its JDBC resource workflow; advice owns HTTP translation. Advice must not close connections, retry SQL, inspect the pool, or contain datasource credentials.

### 13.2 What Spring Boot supplies—and what your code decides

```text
Spring Boot
    → manages compatible dependencies and configures validation/MVC infrastructure

Spring MVC
    → resolves handler arguments, invokes validation, and resolves exceptions

Hibernate Validator
    → evaluates Jakarta Validation constraints

Your request DTOs
    → declare structural input rules

Your service/application code
    → recognizes use-case meaning and known persistence conflicts

Your advice
    → defines safe public exception-to-HTTP mappings

PostgreSQL
    → enforces persistent integrity under concurrency
```

Boot does not invent the Book rules or API error contract. MVC does not decide that this project's duplicate key represents ISBN. Hibernate Validator does not parse JSON. Advice does not enforce database integrity. Saying only “Spring handles it” erases the boundary you need for diagnosis.

### Checkpoint 13

When PostgreSQL rejects an insert, does advice release the pooled connection? Which code decides that the known conflict is specifically a duplicate ISBN? Which component evaluates `@NotBlank`?

<details>
<summary>Answer</summary>

No. JDBC resource handling completes through `JdbcTemplate`/`DataSource` as the exception unwinds. The service recognizes the persistence failure's application meaning, and Hibernate Validator evaluates the Jakarta constraint when Spring MVC invokes validation.

</details>

---

## 14. Anti-patterns

**⭐⭐⭐⭐ IMPORTANT**

| Anti-pattern | Why it fails | Better boundary |
|---|---|---|
| Giant `try/catch` in every controller | Repeats translation and hides successful orchestration | Let known exceptions escape to global advice |
| `catch (Exception)` → 400 | Mislabels bugs/outages as client mistakes | Specific expected mappings; unknown stays 500 |
| Throwing `ResponseStatusException` from a repository | Couples persistence to HTTP | Repository reports persistence outcome; service gives it meaning; advice maps HTTP |
| Returning `ResponseEntity` from a repository | Makes SQL code own transport semantics | Return domain data/absence or throw persistence exceptions |
| Relying only on database errors for obvious input rules | Gives late, coarse feedback and may permit database coercion/rounding | Validate predictable request structure before controller execution |
| Relying only on Bean Validation for durable integrity | Cannot arbitrate concurrency or protect non-HTTP writers | Keep `NOT NULL`, `CHECK`, `UNIQUE`, and type constraints in PostgreSQL |
| Copying the same checks into POST and PUT | Creates drift between equivalent DTO uses | Put shared structural rules on `BookRequest` |
| Exposing `exception.getMessage()` blindly | Can leak SQL, paths, classes, credentials, or unstable provider wording | Controlled client detail; full diagnostics in logs |
| Exposing stack traces | Reveals internals and creates an unstable contract | Generic 500 response plus server-side logging |
| Using annotations as a substitute for business logic | DTO validation has no reliable view of current application state | Stateful/use-case rules belong in service/domain code |
| Assuming `@Valid` means JSON parsing succeeded | There may be no Java object to validate | Diagnose Jackson conversion before validation |
| Assuming validation runs before deserialization | Constraints operate on Java values | JSON → Java first, then validation |
| Creating a custom validator for every trivial rule | Hides familiar semantics and multiplies infrastructure | Prefer standard constraints; customize only a real unmatched rule |
| Querying the database from a uniqueness validator | Adds I/O and still races before INSERT | Let `UNIQUE(isbn)` arbitrate, then translate the known conflict |
| Adding class-level `@Validated` to this MVC controller | Switches away from MVC-native method validation and changes exception behavior | Direct constraints on parameters; no controller-level `@Validated` |
| Collecting field errors with an unmerged `toMap` | A second violation for one field can crash the handler | Accumulate `Map<String, List<String>>` safely |
| Handling only `MethodArgumentNotValidException` | Misses Spring 7 combined method/body validation failures | Also handle `HandlerMethodValidationException` |
| Catching every `DataAccessException` as duplicate ISBN | Connectivity and SQL defects become false 409 responses | Translate only the known `DuplicateKeyException` at the known operation |

### Checkpoint 14

Which is more dangerous: omitting a pretty body for an unknown error, or returning a polished 400 for a database outage? Can a database-backed uniqueness validator remove `UNIQUE(isbn)`?

<details>
<summary>Answer</summary>

The polished but false 400 is more dangerous because it corrupts semantics and hides a server problem. No pre-check removes the concurrency race; the database constraint remains necessary.

</details>

---

## Final mental model

**⭐⭐⭐⭐⭐ MUST KNOW**

Think in four questions, in this order:

1. **Can the request become the required Java values?** Routing, argument resolution, and Jackson answer this. Failure is usually 400 before the controller.
2. **Do those Java values satisfy structural input rules?** Bean Validation answers this. Failure is 400 before the controller body.
3. **Can this use case succeed in current application state?** Controller/service/repository collaboration answers this. Known absence becomes 404; a known ISBN state conflict becomes 409.
4. **Can persistent state remain valid?** PostgreSQL constraints answer this atomically. Unknown failures remain 500.

Then ask a fifth question: **Which web component publishes the response?** Global advice translates escaping known failures into status + headers + safe `ProblemDetail`; it does not become the owner of the underlying rule.

```text
deserialize ≠ validate ≠ decide business meaning ≠ enforce stored integrity

DTO constraint       → input shape
service exception    → application meaning
database constraint  → durable integrity
REST advice          → HTTP translation
```

---

## 20-minute review cheat sheet

### First five minutes: pipeline

```text
routing → argument resolution → Jackson → Bean Validation → controller
        → service → repository/JdbcTemplate → PostgreSQL → response
```

- Failure stops later stages.
- Malformed/type-incompatible JSON is not a constraint violation.
- Controller bodies normally do not execute after conversion or input-validation failure.

### Next five minutes: annotations

```text
@RequestBody  → JSON to Java
@Valid        → cascade validation into a Java value
@NotBlank     → required non-whitespace text
@Size         → text/container length; null is valid
@NotNull      → required reference value
@Positive     → greater than zero; null is valid
@PositiveOrZero → zero or greater; null is valid
@Min/@Max     → integral bounds
@DecimalMin/@DecimalMax → exact decimal bounds
@Digits       → digit representation, not rounding
@Pattern/@Email → text format, not existence/ownership
```

Most constraints accept null. Required numeric wrappers need `@NotNull` plus their value constraint. `@Valid` does not mean required and does not normalize.

### Next five minutes: Spring MVC failures

```text
malformed/wrong JSON        → HttpMessageNotReadableException → 400
standalone invalid body     → MethodArgumentNotValidException → 400
direct/mixed method values  → HandlerMethodValidationException → 400 input / 500 return
bad numeric path/query text → MethodArgumentTypeMismatchException → 400
missing book                → BookNotFoundException → 404
known duplicate ISBN        → DuplicateIsbnException → 409
unknown bug/outage          → safe 500 + detailed server log
```

No class-level `@Validated` is needed on a Spring 7 MVC controller. `@Validated` remains useful for groups and deliberate proxy-based service method validation; self-invocation bypasses that proxy.

### Final five minutes: boundaries

```text
Jakarta Validation API → defines annotations/contracts
Hibernate Validator    → implements/evaluates them; not Hibernate ORM
Spring MVC             → invokes validation and resolves exceptions
Spring Boot            → supplies compatible infrastructure
application code       → declares rules and public mappings
PostgreSQL             → final concurrent integrity arbiter
```

```text
@RestControllerAdvice
    → global REST exception translation

@ExceptionHandler(X.class)
    → X to a chosen HTTP response

Bean Validation
    ≠ JSON deserialization
    ≠ normalization
    ≠ business logic
    ≠ database constraints
```

---

## Readiness checklist

Before moving to Spring Data JPA and Hibernate ORM, you should be able to say:

- [ ] I can trace routing, conversion, validation, controller, service, repository, database, and response stages.
- [ ] I can predict whether a controller method executes for malformed JSON, a wrong type, and an invalid DTO.
- [ ] I can distinguish Jakarta Validation, Hibernate Validator, Spring MVC integration, and Spring Boot auto-configuration.
- [ ] I know Hibernate Validator does not introduce Hibernate ORM or JPA.
- [ ] I know the supported value kinds and null behavior of the core constraints.
- [ ] I can choose among `@NotNull`, `@NotEmpty`, and `@NotBlank`.
- [ ] I know why a required numeric wrapper needs `@NotNull` plus its numeric constraint.
- [ ] I can explain `@RequestBody` versus `@Valid`, and nested versus container validation.
- [ ] I can explain why validation does not trim or otherwise normalize values.
- [ ] I can preserve the Book price's meaningful-decimal rule without misusing `@Digits`.
- [ ] I can distinguish MVC-native controller validation from proxy-based `@Validated` service validation.
- [ ] I can handle both `MethodArgumentNotValidException` and `HandlerMethodValidationException` without losing field errors.
- [ ] I can explain `@ExceptionHandler`, `@ControllerAdvice`, and `@RestControllerAdvice` separately.
- [ ] I can choose 400, 404, 409, or 500 from failure meaning rather than exception convenience.
- [ ] I can keep input validation, business rules, database integrity, and HTTP translation in their proper layers.
- [ ] I can explain the concurrent duplicate-ISBN race and why `UNIQUE(isbn)` remains mandatory.
- [ ] I can preserve framework status-specific headers and avoid leaking rejected values or internal diagnostics.
- [ ] I can explain why advice never manages HikariCP connections or JDBC cleanup.
- [ ] I can describe what Boot supplies and what the application must still decide.
- [ ] I can recognize and avoid the anti-patterns in section 14.

If any item is uncertain, revisit the corresponding checkpoint, predict the outcome before opening its answer, and then prove it with the companion exercise's manual request matrix.
