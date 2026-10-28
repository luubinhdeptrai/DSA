# Testing Exercise

Read the [concepts](Testing_Essential_Concepts.md) first. Attempt each task manually before opening its matching [solution](Testing_Solution.md). This document contains **19 tasks: W1–W3 plus the original Tasks 1–16**, whose identifiers and core scenarios are preserved.

## Existing project

This exercise extends the checked-in application only:

```text
Spring_Security/book-catalog-api/
```

Do not create a second application, replace PostgreSQL with H2, bypass Flyway, or redesign the existing endpoints. The goal is to observe the architecture that already exists.

## Verified baseline

| Inspected fact | Consequence for this exercise |
|---|---|
| Java 21, Spring Boot 4.1.1 | Use Boot 4.1 packages and dependency management. |
| Spring Framework 7.0.9 | Use `@MockitoBean`, not the old Boot `@MockBean` (removed in Boot 4). |
| Spring Security 7.1.1 | Add Boot's modular Security test starter. |
| Direct JUnit 4.11 and generated `assertTrue(true)` test | Remove both and migrate to JUnit Jupiter 6.0.3. |
| POM pins Surefire 2.22.1 | Remove stale plugin pins; use Boot-managed Surefire 3.5.6. |
| PostgreSQL 17 in Compose | Match it with `postgres:17` in Testcontainers. |
| Flyway 12.4.0; V1–V6 SQL found only in tracked build output | Source migrations are missing; see the prerequisite below before database execution. |
| Hibernate `ddl-auto: validate` | Keep Hibernate as validator, never schema creator. |
| HikariCP is the application `DataSource` | Full DB tests still use the real pool and pgJDBC. |
| `GET /books` is public; book writes and transaction lab require ADMIN | Security tests must preserve the actual matrix. |
| login/logout are Security filter endpoints | Real authentication uses form parameters, session, and CSRF—not JSON controller calls. |
| only `BookService.setStock` has `@PreAuthorize` | Method-security tests target that method through its proxy. |
| `BookService.delete` intentionally flushes, throws, and rolls back | Test 500/exception plus row survival; do not expect normal deletion. |
| `StockRequest` has no constraints | Do not invent a negative-stock MVC 400 test. |
| validation advice has placeholder title/detail | Assert the checked-in response, even if its wording should later improve. |
| No test-specific source profile or container support is present | Add it manually as an exercise; do not depend on stale `target/test-classes`. |

The dependency versions above were checked against the locally resolved Boot 4.1.1 dependency-management POM, not inferred from a newer tutorial. It also manages Mockito 5.23.0, Testcontainers 2.0.5, pgJDBC 42.7.13, Hibernate 7.4.5.Final and HikariCP 7.0.2. The direct JUnit dependency overrides Boot until Task 2 is completed.

The reference configures the test JVM to UTC for deterministic time behavior. The previous material reported a PostgreSQL startup failure with the legacy zone `Asia/Saigon`; that specific failure was not reproduced in this review. Treat it as a possible environment diagnosis, not a universal PostgreSQL claim.

### Source-migration prerequisite and current execution status

On review, `src/main/resources/db` is empty. Six SQL files (V1–V6) exist under tracked `target/classes/db/migration` only. They describe books, optimistic versioning, audits, author indexes and users/role checks. They are build artifacts, not a reliable source migration history. A clean build cannot regenerate them, and an empty database will fail Hibernate schema validation.

Before executing database Tasks 9–15, the project owner must restore the authoritative unchanged migration files to the application's source migration location in a separate maintenance change. This documentation update does not create, copy, rewrite or repair production migrations. Do not use `ddl-auto=create`, skip Flyway, or rely on stale `target` files to conceal the issue. You can still complete design work and the non-database exercises.

The reviewed Docker CLI could not reach its daemon. Database runtime results are therefore unverified here. Old tracked test reports/classes are historical artifacts and do not establish that this source checkout can reproduce them. The solution's verification notes distinguish compilation from execution.

### Existing limitations to observe

| Source observation | Testing implication |
|---|---|
| `BookRequest.price` has no `@Digits`; the old `checkBook` helper is never called | Do not invent a two-decimal-place MVC rejection. Database numeric conversion is a separate boundary. |
| `StockRequest` has no constraints; inspected SQL has no nonnegative stock check | Do not expect a negative PATCH stock value to fail Bean Validation. Record the gap rather than adding a rule. |
| `@Max(1000)` on list size uses the message `size must be at least 1000` | Assert the real upper limit and identify the misleading wording as a limitation. |
| Book create/replace catch every `DataIntegrityViolationException` as duplicate ISBN; registration maps every such failure to username conflict | A translation test proves the current broad mapping, not that every possible integrity failure really is a duplicate. |
| Registration checks UTF-8 bytes separately from the DTO's character limit | Include multibyte passwords; a character-only test misses this service guard. |

These observations are characterization targets or documented gaps, not authorization to modify production behavior during the exercises. The intentional delete/transaction failures are teaching behavior, not defects to fix to obtain green tests.

## Objective

By the end, you will be able to:

- distinguish pure unit, MVC slice, JPA/PostgreSQL, method-security, and full-context tests;
- use JUnit Jupiter, AssertJ, Mockito, MockMvc, Spring Security test support, and Testcontainers;
- prove actual validation and `ProblemDetail` contracts;
- expose JPA SQL and constraints with flush/clear;
- run Flyway before Hibernate against disposable PostgreSQL;
- test transaction commit/rollback/propagation without a masking outer test transaction;
- contrast mock identities with real repository/BCrypt/session login;
- run the suite reproducibly with Maven.

## What remains unchanged

- all production Java classes;
- all controllers, routes, DTOs, and response shapes;
- PostgreSQL, Flyway V1–V6, Hibernate, and HikariCP;
- `SecurityConfig`, CSRF, form login, and session authentication;
- all transaction-lab scenarios and intentional failures;
- `compose.yaml`, `.env`, and local developer database state.

The learner may manually change test dependencies, runner configuration and `src/test` while doing these exercises. This documentation update applies none of those changes. All sample files are proposed reference implementations, not the checked-in test suite.

## Learning contract

For every task:

1. State the behavior being proved.
2. Mark what is real and what is mocked.
3. Predict the result before running.
4. Complete the starter containing deliberate `TODO`s.
5. Run the narrowest relevant Maven command.
6. Explain what the test still cannot prove.

Each task uses the same twelve fields. Fill in a matrix before coding when requested:

```text
Case ID | Behavior/requirement | Preconditions | Inputs and action
        | Expected output/state/forbidden side effects | Happy/edge/failure
        | Test level | Real components | Mocks | What remains unproved
```

### Prerequisites and execution order

Know Java 21, Maven, constructor injection, REST, JPA, transactions and the application's session security. Docker is needed only for database tasks. Read the production files named in each task.

W1–W3 appear first to teach small cases before framework tests. For their first execution, complete **Task 2's toolchain setup once**; the current JUnit 4 setup cannot run their Jupiter examples. You can design them before that setup. Do not install an unrelated project.

Task 9 introduces a security concept before the shared database fixture is built in Tasks 10–12. Design Task 9 in order, then run it after that fixture and the source-migration prerequisite are ready. The same helper is reused throughout; do not duplicate databases or configuration.

## Safety boundary

- Testcontainers may start disposable containers; it does not deploy/containerize the application.
- Never point tests at a production or valued local database.
- Never run Flyway clean or `docker compose down -v` as part of this exercise.
- Never read or print `.env`.
- Never log passwords, hashes, CSRF tokens, cookies, session IDs, or authorization headers.
- Do not use fixed sleeps, test ordering, or shared mutable test state.
- A working Docker-compatible runtime is required for PostgreSQL tests. If it is unavailable, report the infrastructure failure; do not fall back to H2.

## Before and target test architecture

### Before

```text
Maven
  -> JUnit 4.11
  -> AppTest.shouldAnswerWithTrue()
  -> no Spring, no behavior, no database, no confidence
```

### Target

```text
Pure Java test ----------------------> DTO/domain behavior
Mockito unit test -------------------> real service + mocked boundary
MVC slice ---------------------------> DispatcherServlet/controller/advice
Security MVC slice ------------------> real SecurityFilterChain + mock identity
JPA slice ---------------------------> Hibernate/JDBC/Hikari/PostgreSQL
                                         ^
                                         `-- Flyway V1–V6
Full Spring integration ------------> Security + controller + service proxy
                                      + repository + real PostgreSQL
                                      + real login/session/CSRF/transactions
```

## Beginner warm-up tasks

<a id="w1"></a>

### W1 — Assertions and exceptions with actual project classes

**Objective**

Observe constructor normalization and recognize an invalid unsaved-entity fixture.

**Prerequisites**

Task 2 toolchain for execution; read BookRequest, BookResponse and Book.

**Related concepts**

[Relevant theory](Testing_Essential_Concepts.md#junit).

**Requirements / behavior to test**

Use `BookRequest` to check trimmed text and unchanged price/stock. Separately call `BookResponse.from` with an unsaved `Book`: its null ID is unboxed into a primitive `long`, so the current code throws `NullPointerException`. This is a fixture limitation, not an intentional request-validation rule. Do not assert JVM-specific exception text.

**Test case design**

Create one case for normalization and one for the unsaved entity failure. Record the input object state, exact action, expected accessors/exception and boundary. Explain why nullability here differs from `@NotBlank` validation.

**What to implement**

Create `dto/AssertionsWarmupTest`. Use `assertAll`, `assertEquals`, and `assertThrows`. Keep the exception lambda limited to `BookResponse.from(unsaved)`.

**Starter code with TODOs**

```java
class AssertionsWarmupTest {
    @Test
    void request_normalizesTextAndPreservesNumbers() {
        // TODO: construct real BookRequest with padded text
        // TODO: group assertions for text, price and stock
    }

    @Test
    void response_unsavedBook_hasNoUsableId() {
        // TODO: construct real Book without a database-generated ID
        // TODO: assert the actual exception from BookResponse.from
    }
}
```

**Expected behavior prediction**

Predict whether `BookRequest` construction evaluates Bean Validation and why the response conversion fails before any database exists.

**Hints**

Use a `BigDecimal` string constructor; no Spring annotation or mock is needed. Import project DTO/entity classes and Jupiter assertions. Complete imports are in the matching solution.

**How to run and verify**

```powershell
mvn -Dtest=AssertionsWarmupTest test
```

**Common mistakes**

Avoid adding Spring, expecting a validation exception during construction, or treating the observed null-ID failure as a desirable API contract.

**What the test proves and does not prove**

Proves Java normalization and the current unsaved-fixture failure for these objects. Does not prove HTTP validation, ID generation, persistence or domain rejection of an HTTP request.

After attempting: [Solution W1](Testing_Solution.md#w1).

<a id="w2"></a>

### W2 — Parameterized normalization cases

**Objective**

Replace repeated assertions with independently designed data rows.

**Prerequisites**

Task 2 toolchain; W1; read BookRequest.normalize via its constructor.

**Related concepts**

[Relevant theory](Testing_Essential_Concepts.md#junit).

**Requirements / behavior to test**

The constructor uses `String.trim()` on non-null ISBN/title/author. Null is retained; blank strings can become empty. None of this automatically runs Bean Validation.

**Test case design**

Partition input into already trimmed, padded, empty, whitespace-only and null. Add at least one row per class. Choose expected values before writing a parameter source; consider tabs and spaces without assuming Unicode `strip()` semantics.

**What to implement**

Create `dto/ParameterizedBookRequestTest`. Use `@ParameterizedTest` with `@CsvSource` for input/expected pairs, plus `@NullAndEmptySource` and `@ValueSource` for null/blank behavior. Keep all numeric inputs valid.

**Starter code with TODOs**

```java
class ParameterizedBookRequestTest {
    @ParameterizedTest
    @CsvSource({/* TODO: input and independent expected output */})
    void title_normalizesEachInput(String input, String expected) {
        // TODO: construct BookRequest and assert title
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {/* TODO: blank inputs */})
    void title_nullOrBlank_remainsNullOrBecomesEmpty(String input) {
        // TODO: distinguish null from empty in the assertion
    }
}
```

**Expected behavior prediction**

Predict the report invocation count and the output of null, empty and tab-only inputs. Which constraint could still reject the normalized result later?

**Hints**

CSV quoting/empty semantics matter; use explicit quoted values. Use a nullable String parameter. Do not compute every expectation by calling the same production normalization algorithm.

**How to run and verify**

```powershell
mvn -Dtest=ParameterizedBookRequestTest test
```

**Common mistakes**

Do not attach both `@Test` and `@ParameterizedTest`, put null in `@ValueSource`, or assume normalization means validation success.

**What the test proves and does not prove**

Proves normalization for all supplied rows. Does not prove `@Valid`, nonblank acceptance or HTTP response mapping.

After attempting: [Solution W2](Testing_Solution.md#w2).

<a id="w3"></a>

### W3 — Design partitions and boundaries before implementing them

**Objective**

Derive cases from a real DTO constraint, then verify them with a real Validator.

**Prerequisites**

Task 2 toolchain; W2; read BookRequest annotations and compact constructor.

**Related concepts**

[Relevant theory](Testing_Essential_Concepts.md#test-case-design).

**Requirements / behavior to test**

ISBN is normalized, then constrained by `@NotBlank` and `@Size(max=20)` when validation runs. Keep title, author, price and stock valid. A direct constructor can still contain invalid data until the Validator evaluates it.

**Test case design**

Build a matrix for null, empty, blank, nonblank lengths 1/19/20/21, and a padded 20-character value. Fill in expected ISBN validity and justify the representative and boundary choices. Explain why the normalized length is relevant. Also name the separate MVC test needed to prove HTTP rejection.

**What to implement**

Create `dto/BookRequestBoundaryTest`. Build/close a `ValidatorFactory`, use a parameterized `@MethodSource`, and examine violations for the `isbn` property. Keep a hand-written matrix beside the test during study.

**Starter code with TODOs**

```java
class BookRequestBoundaryTest {
    static Stream<Arguments> isbnCases() {
        // TODO: supply case name, input and expected ISBN validity
        throw new UnsupportedOperationException("TODO");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("isbnCases")
    void isbn_obeysDeclaredConstraints(String caseName, String input, boolean expectedValid) {
        // TODO: create real request and explicitly validate it
        // TODO: check ISBN property violations, not arbitrary other fields
    }
}
```

**Expected behavior prediction**

Predict which cases pass construction but fail validation, and whether padding counts toward the final size limit.

**Hints**

Use `Validation.buildDefaultValidatorFactory()` and `validator.validate(request)`. Close the factory with try-with-resources or class cleanup. No Spring context, mock or database is required.

**How to run and verify**

```powershell
mvn -Dtest=BookRequestBoundaryTest test
```

**Common mistakes**

Do not vary every field simultaneously, test only length 21, or add an ISBN format/checksum rule absent from the annotations.

**What the test proves and does not prove**

Proves the actual ISBN constraints and normalization using the real validation provider. Does not prove MVC activates validation, error JSON, service behavior or database ISBN constraints.

After attempting: [Solution W3](Testing_Solution.md#w3).

## Core project tasks — original identifiers preserved

<a id="task-1"></a>

### Task 1 — Inventory behavior and choose test boundaries

**Objective**

Design before writing tests.

**Prerequisites**

Read the scope, application source and theory principles/design chapter.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#principles). The smallest sufficient scope produces faster, clearer failures.

**Requirements / behavior to test**

Inventory registration, validation, authorization, repository constraints, transaction outcomes and real login from source. Distinguish observed contracts from desirable changes.

**Test case design**

Create at least six behavior rows with preconditions, exact action, expected outcome, real components and mocks. Include one happy, edge and failure case where applicable.

**What to implement**

Inspect the POM, current test, controllers, DTOs, services, repositories, security classes, migrations, and transaction lab. Build a matrix for at least registration, controller validation, authorization, repository constraints, transaction rollback, and real login.

**Starter code with TODOs**

```text
| Behavior | Test level | Spring? | PostgreSQL? | Mocked? |
| TODO     | TODO       | TODO    | TODO        | TODO    |
```

**Expected behavior prediction**

Which behaviors require a Spring proxy? Which require PostgreSQL? Which can use plain Java?

**Hints**

- An annotation on an object created with `new` has no interceptor.
- A database constraint cannot be proved by a repository mock.
- JSON/validation/advice can be proved without starting PostgreSQL.

**How to run and verify**

```powershell
mvn --version
```

For each row, answer the boundary questions in the [theory architecture map](Testing_Essential_Concepts.md#architecture-map).

**Common mistakes**

- Selecting `@SpringBootTest` for every row.
- Calling every Spring test an integration test without naming its boundary.
- Treating mock identity as real authentication.

**What the test proves and does not prove**

The intended progression is pure Java → Mockito → MVC slice → security slice → JPA/PostgreSQL → full transaction/authentication integration.

This is a design/build audit; it does not establish application behavior without executed assertions.

After attempting: [Solution Task 1](Testing_Solution.md#task-1).

<a id="task-2"></a>

### Task 2 — Modernize the Boot 4 test toolchain

**Objective**

Replace the obsolete generated JUnit 4 setup with Boot-managed modern test support.

**Prerequisites**

Task 1 inventory; Maven using Java 21. This setup can be performed first to execute W1–W3.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#spring-testing). Boot 4.1 modularizes focused test support and manages compatible versions.

**Requirements / behavior to test**

The current build uses direct JUnit 4.11 and pins Surefire 2.22.1. The target reference uses Boot-managed Jupiter with modular testing support; no production dependency or behavior is redesigned.

**Test case design**

Predict discovery for the old AppTest and new *Test names. Record dependency/compiler/runner prerequisites separately from behavior assertions.

**What to implement**

As a learner change, remove `junit:junit:4.11` and `src/test/java/com/example/AppTest.java`. Add the base, MVC, JPA, Security, Boot Testcontainers, and PostgreSQL Testcontainers dependencies with `test` scope and no versions. Remove stale plugin pins. Declare versionless Surefire only to set `user.timezone=UTC` for this environment.

**Starter code with TODOs**

```xml
<!-- TODO: replace old JUnit 4 dependency -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>TODO</artifactId>
  <scope>test</scope>
</dependency>

<!-- TODO: Boot/Testcontainers service connection support -->
<!-- TODO: Testcontainers PostgreSQL 2.x module -->
```

**Expected behavior prediction**

Which versions will Maven select for Jupiter, Mockito, Testcontainers, and Surefire?

**Hints**

- Boot 4 uses `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, and `spring-boot-starter-security-test`.
- Testcontainers 2 uses `testcontainers-postgresql`.
- Do not add JUnit Vintage merely to retain `assertTrue(true)`.

**How to run and verify**

```powershell
mvn dependency:tree
mvn help:effective-pom
mvn test -DskipTests
```

Confirm JUnit Jupiter 6.0.3, Mockito 5.23.0, Testcontainers 2.0.5, and no direct JUnit 4 dependency.

**Common mistakes**

- Copying Boot 3 package/dependency examples.
- Hard-coding versions already managed by Boot.
- Leaving Surefire 2.22.1 pinned.

**What the test proves and does not prove**

The Boot parent is a compatibility contract. The project chooses capabilities; Boot chooses the tested version set.

This is a design/build audit; it does not establish application behavior without executed assertions.

After attempting: [Solution Task 2](Testing_Solution.md#task-2).

<a id="task-3"></a>

### Task 3 — Write the first pure JUnit test

**Objective**

Prove a real behavior without Spring or mocks.

**Prerequisites**

Task 2; W1/W2; inspect BookRequest.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#junit). `BookRequest`'s compact constructor normalizes three text values.

**Requirements / behavior to test**

Non-null ISBN/title/author are trimmed; price and stock are passed through. Null text remains null. No constraint evaluation occurs during direct construction.

**Test case design**

Select padded, already trimmed and null inputs, with independent expected values. Decide which cases overlap W1/W2 and which assertion adds confidence.

**What to implement**

Create `dto/BookRequestTest` using Jupiter and AssertJ. Verify trimming while leaving price/stock as supplied; include null preservation.

**Starter code with TODOs**

```java
class BookRequestTest {
    @Test
    void constructor_textValues_trimsThem() {
        BookRequest request = new BookRequest(
                "  isbn-1  ", "  Clean Code  ", "  Robert Martin  ",
                new BigDecimal("19.99"), 3);

        // TODO: assert the three normalized accessors
    }
}
```

**Expected behavior prediction**

Will Spring, validation, or PostgreSQL start?

**Hints**

This test calls Java code directly. Record component constraints are not automatically evaluated.

**How to run and verify**

```powershell
mvn -Dtest=BookRequestTest test
```

**Common mistakes**

- Adding `@SpringBootTest`.
- Claiming this proves `@NotBlank` HTTP handling.
- Testing trivial generated record accessors instead of normalization.

**What the test proves and does not prove**

The production record is real; everything outside it is absent. A fast test precisely proves constructor normalization.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 3](Testing_Solution.md#task-3).

<a id="task-4"></a>

### Task 4 — Unit-test registration with Mockito

**Objective**

Prove security-sensitive service decisions in isolation.

**Prerequisites**

Task 3; inspect RegistrationService and RegisterRequest.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#mockito). Mock collaborator boundaries, capture important output, and verify only meaningful interactions.

**Requirements / behavior to test**

Success encodes and saves a USER. Duplicate precheck and write-time integrity failure map to 409. Passwords over 72 UTF-8 bytes map to 400 before repository/encoder work, even if character count is within DTO bounds.

**Test case design**

Before mocking, tabulate success, precheck conflict, simulated write conflict, ASCII 72/73 bytes and multibyte 72/>72 bytes. Record whether lookup/encode/save may run. Explain character count versus byte count.

**What to implement**

Create `RegistrationServiceTest` with `@Mock`, `@InjectMocks`, stubbing, verification, and `ArgumentCaptor`. Cover success, duplicate precheck, simulated race-time database failure and the 72-byte password guard.

**Starter code with TODOs**

```java
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock AppUserRepository users;
    @Mock PasswordEncoder encoder;
    @InjectMocks RegistrationService service;

    @Test
    void register_newUsername_encodesPasswordAndAssignsUserRole() {
        // TODO: stub exists/encode
        // TODO: call register
        // TODO: capture AppUser and assert hash plus role USER
    }
}
```

**Expected behavior prediction**

Could this test still pass if BCrypt configuration or the database unique constraint were broken?

**Hints**

- Do not assert an exact BCrypt hash; the encoder is mocked here.
- Duplicate precheck should produce no encoder or save interaction.
- `saveAndFlush` failure is translated to a 409 `ResponseStatusException`.

**How to run and verify**

```powershell
mvn -Dtest=RegistrationServiceTest test
```

**Common mistakes**

- Accepting or assigning `ADMIN` from request input.
- Mocking `RegisterRequest` or `AppUser`.
- Verifying every internal call rather than the boundary contract.

**What the test proves and does not prove**

This unit test proves orchestration and server-side role assignment. Database and real encoder tests appear later.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 4](Testing_Solution.md#task-4).

<a id="task-5"></a>

### Task 5 — Unit-test `AppUserDetailsService`

**Objective**

Verify conversion from the stored application user to Spring Security's `UserDetails`.

**Prerequisites**

Task 4; inspect AppUserDetailsService and AppUserRepository.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#mockito). One collaborator (`AppUserRepository`) can be stubbed while authority mapping remains real Java behavior.

**Requirements / behavior to test**

Stored username/hash are passed through and the stored role becomes a ROLE_ authority. An absent account raises UsernameNotFoundException with the generic message in source.

**Test case design**

Use a decision table for USER, ADMIN and missing account. Specify username/hash/authorities or exception and which repository lookup is expected.

**What to implement**

Cover a known ADMIN or USER and an unknown username. Assert username, stored hash propagation, `ROLE_` prefix, and generic missing-user message.

**Starter code with TODOs**

```java
@Test
void loadUserByUsername_knownAdmin_mapsStoredDataAndAuthority() {
    when(users.findByUsername("admin"))
            .thenReturn(Optional.of(
                    new AppUser("admin", "stored-hash", "ADMIN")));

    UserDetails details = service.loadUserByUsername("admin");

    // TODO: username, password, and ROLE_ADMIN assertions
}
```

**Expected behavior prediction**

Does this prove `DaoAuthenticationProvider` calls the service during login?

**Hints**

`User.withUsername(...).roles("ADMIN")` creates authority `ROLE_ADMIN`.

**How to run and verify**

```powershell
mvn -Dtest=AppUserDetailsServiceTest test
```

**Common mistakes**

- Expecting authority `ADMIN` rather than `ROLE_ADMIN`.
- Returning different messages that reveal whether an account exists.
- Labeling this a full authentication test.

**What the test proves and does not prove**

It is a pure unit test. The repository is mocked; no AuthenticationManager, BCrypt, filter, or session runs.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 5](Testing_Solution.md#task-5).

<a id="task-6"></a>

### Task 6 — Build an MVC controller slice

**Objective**

Test Book HTTP mapping, query parameters, JSON, status, content type, and `Location` without a database.

**Prerequisites**

Tasks 2–5; inspect BookController, BookResponse and advice.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#spring-testing). `@WebMvcTest` loads the web slice; `@MockitoBean` supplies its service boundary.

**Requirements / behavior to test**

GET /books normalizes author, accepts page/size and maps results to a JSON list. POST /books returns 201, Location and mapped JSON. Fixture entities require a non-null ID.

**Test case design**

List GET with filter/page/size, empty result and POST success. For each, specify expected JSON fields, normalization and service arguments. Mark which checks need no filters/database.

**What to implement**

Create `BookControllerWebMvcTest` with Boot 4 imports. Intentionally disable filters for this controller-contract class and explain why a separate class tests security. Cover list and create success.

**Starter code with TODOs**

```java
@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerWebMvcTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean BookService bookService;

    @Test
    void findAll_withQueryParameters_returnsMappedJson() throws Exception {
        // TODO: stub a Book with a non-null id
        // TODO: GET /books?author=...&page=0&size=2
        // TODO: status/content-type/JSON assertions
    }
}
```

**Expected behavior prediction**

Which components are real, and which production failure could this test miss?

**Hints**

- Boot 4 import: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`.
- The entity has no public ID setter; `ReflectionTestUtils` is acceptable for test fixture identity.
- MockMvc does not start a network server.

**How to run and verify**

```powershell
mvn -Dtest=BookControllerWebMvcTest test
```

**Common mistakes**

- Using the removed Boot `@MockBean` import.
- Returning an unsaved `Book`; `BookResponse` unboxes its null ID.
- Claiming mocked service output proves persistence.

**What the test proves and does not prove**

Real MVC mapping, conversion, and controller code run. `BookService`, AOP, JPA, Flyway, and PostgreSQL do not.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 6](Testing_Solution.md#task-6).

<a id="task-7"></a>

### Task 7 — Test validation and global error mapping

**Objective**

Prove the checked-in 400, 404, and 409 HTTP contracts.

**Prerequisites**

Task 6; W3; inspect actual DTO/controller constraints and advice.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#spring-testing). An exception unit test and an HTTP exception-mapping test answer different questions.

**Requirements / behavior to test**

An invalid BookRequest is rejected before the service. Missing book and duplicate ISBN are mapped to 404 and 409 by the real advice. Preserve its actual placeholder validation wording.

**Test case design**

Select one-field invalid ISBN, price and stock cases; valid lower boundaries; size 0/1/1000/1001 and page -1/0/1000000/1000001; malformed JSON; missing ID; duplicate ISBN. Predict status, field errors and service non-interaction. Do not invent decimal-place or StockRequest constraints.

**What to implement**

Extend the MVC slice with invalid `BookRequest`, missing-book, and duplicate-ISBN scenarios, plus your designed numeric/query boundaries and malformed JSON. Assert selected actual `ProblemDetail` fields and no service call for invalid input.

**Starter code with TODOs**

```java
mockMvc.perform(post("/books")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {"isbn":"   ","title":"Title","author":"Author",
                 "price":12.50,"stock":4}
                """))
    .andExpect(status().isBadRequest())
    // TODO: actual title/detail/fieldErrors assertion
    ;

// TODO: verify service was not invoked
```

**Expected behavior prediction**

What exact title/detail does the current advice return, and does it include `instance`?

**Hints**

- Do not improve the response inside the test.
- Use `contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)`.
- Do not assert JSON property order.

**How to run and verify**

```powershell
mvn -Dtest=BookControllerWebMvcTest test
```

Run the slice and inspect only failures, not production secrets.

**Common mistakes**

- Asserting imaginary `timestamp` or `errors` fields.
- Writing a false negative-stock validation test for unconstrained `StockRequest`.
- Calling the service and then accepting a 400 produced somewhere else.

**What the test proves and does not prove**

The test makes current behavior executable documentation and exposes design gaps without silently changing them.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 7](Testing_Solution.md#task-7).

<a id="task-8"></a>

### Task 8 — Test the actual URL security policy

**Objective**

Verify authentication, role, and CSRF decisions independently of persistence.

**Prerequisites**

Tasks 6–7; inspect SecurityConfig, handlers and AuthController.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#security). A security MVC slice keeps filters and imports the real `SecurityConfig`, handlers, and advice while service beans remain mocked.

**Requirements / behavior to test**

Public GET is permitted; /auth/me needs authentication; writes need ADMIN and CSRF. Missing CSRF can reject before authentication, and ADMIN does not bypass CSRF.

**Test case design**

Write the anonymous/USER/ADMIN x read/write x valid/missing/invalid CSRF matrix. Mark irrelevant/impossible combinations and identify rejection precedence.

**What to implement**

Create `SecurityPolicyWebMvcTest` for anonymous public GET, anonymous `/auth/me`, anonymous write with/without CSRF, USER write with CSRF, and ADMIN write with/without CSRF, plus an invalid token case.

**Starter code with TODOs**

```java
@WebMvcTest(controllers = {BookController.class, AuthController.class})
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, GlobalExceptionHandler.class})
class SecurityPolicyWebMvcTest {
    // TODO: MockMvc and Spring-context service replacements

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminWithCsrf_reachesController() throws Exception {
        // TODO: valid request with csrf(), expect 201
    }
}
```

**Expected behavior prediction**

Why does anonymous POST without CSRF return 403, while the same request with CSRF returns 401?

**Hints**

- CSRF filtering can reject before authorization.
- `@WithMockUser(roles="ADMIN")` creates `ROLE_ADMIN`.
- A valid admin response needs a stubbed `Book` with an ID.

**How to run and verify**

```powershell
mvn -Dtest=SecurityPolicyWebMvcTest test
```

**Common mistakes**

- Disabling filters in the security test.
- Assuming an ADMIN bypasses CSRF.
- Claiming `@WithMockUser` proves database login.

**What the test proves and does not prove**

This proves filter-chain policy using synthetic identities. Real authentication comes later.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 8](Testing_Solution.md#task-8).

<a id="task-9"></a>

### Task 9 — Test method security through a Spring proxy

**Objective**

Prove the existing `@PreAuthorize` guard on `BookService.setStock`.

**Prerequisites**

Task 8 for concepts; design now, then execute after Tasks 10–12 provide the shared container/schema support. Source-migration prerequisite and Docker are required.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#transactions). Method security is AOP. The call must pass through the injected proxy.

**Requirements / behavior to test**

The real BookService.setStock method guard denies USER and allows ADMIN. Successful service calls must commit before a new read observes stock.

**Test case design**

Specify USER denial with unchanged stock and ADMIN success with a new committed value. Identify the method proxy and explain why an HTTP-only test would not isolate it.

**What to implement**

Create a Spring integration test using the real `BookService` and PostgreSQL. Seed a book. Assert USER is denied with unchanged stock; ADMIN reaches the transactional method and commits the new stock.

**Starter code with TODOs**

```java
@SpringBootTest
@Import(TestPostgresConfiguration.class)
@ActiveProfiles("test")
class BookServiceMethodSecurityIntegrationTest {
    @Autowired BookService service;

    @Test
    @WithMockUser(roles = "USER")
    void setStock_user_isDeniedByMethodProxy() {
        // TODO: call injected proxy and assert AccessDeniedException
        // TODO: assert database state is unchanged
    }
}
```

**Expected behavior prediction**

What would happen if the test used `new BookService(repository)`?

**Hints**

- Do not annotate this class/test with `@Transactional` if the ADMIN case must prove commit.
- URL authorization is not involved in a direct service call.

**How to run and verify**

```powershell
mvn -Dtest=BookServiceMethodSecurityIntegrationTest test
```

Run with Docker available and compare stock after each invocation.

**Common mistakes**

- Calling a directly constructed target.
- Testing only the HTTP rule and assuming the method rule ran.
- Using USER authority `USER` instead of role `ROLE_USER` semantics.

**What the test proves and does not prove**

The Spring proxy applies method authorization, then the target's transaction interceptor/business code when permitted.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 9](Testing_Solution.md#task-9).

<a id="task-10"></a>

### Task 10 — Add a PostgreSQL repository slice

**Objective**

Test real entity mappings and derived queries against PostgreSQL 17.

**Prerequisites**

Task 2 dependencies; actual migrations restored separately; Docker reachable. Create test profile and shared helper once.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#persistence). Testcontainers supplies disposable infrastructure; Boot service connections publish dynamic JDBC details.

**Requirements / behavior to test**

Real repositories/mappings run against disposable PostgreSQL 17 using dynamic connection details; MVC is absent. Source migrations must be restored separately before execution.

**Test case design**

Specify saved/reloaded values, existing/missing username, and author-query exact matching with sorted paging. Seed enough rows to cross a page boundary; control order explicitly.

**What to implement**

Create `TestPostgresConfiguration` with a Boot-managed `PostgreSQLContainer` bean. Create `RepositoryPostgresIntegrationTest` using Boot 4 `@DataJpaTest`, no database replacement, test profile, and explicit Flyway auto-configuration. Test a round trip and `findByUsername`, including an absent username, plus exact-author queries with stable paging.

**Starter code with TODOs**

```java
@TestConfiguration(proxyBeanMethods = false)
class TestPostgresConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("TODO");
    }
}

@DataJpaTest
@AutoConfigureTestDatabase(replace = TODO)
@Import(TestPostgresConfiguration.class)
class RepositoryPostgresIntegrationTest {
    // TODO
}
```

**Expected behavior prediction**

Does this slice start MVC or the Security filter chain? Which persistence components are real?

**Hints**

- Testcontainers 2 import: `org.testcontainers.postgresql.PostgreSQLContainer`.
- Use `Replace.NONE`.
- Flush and clear before the database reread.

**How to run and verify**

```powershell
mvn -Dtest=RepositoryPostgresIntegrationTest test
```

Confirm PostgreSQL 17 from the image/container lifecycle and a mapped host port. Do not add diagnostic output for credentials.

**Common mistakes**

- Adding H2.
- Hard-coding `localhost:5432` test settings.
- Using the deprecated Testcontainers 1.x PostgreSQL class/package when a current class exists.

**What the test proves and does not prove**

The slice excludes MVC/services but runs DataSource, HikariCP, Hibernate, repositories, pgJDBC, Flyway (when imported), and real PostgreSQL.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 10](Testing_Solution.md#task-10).

<a id="task-11"></a>

### Task 11 — Expose persistence-context and constraint behavior

**Objective**

See why `save`, flush, clear, and exception-translation boundaries matter.

**Prerequisites**

Task 10; read entity mapping and inspected SQL constraints.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#persistence). Managed state can look correct before SQL executes, and the first-level cache can hide database reads.

**Requirements / behavior to test**

Flush synchronizes SQL; clear removes managed cached entities. A duplicate ISBN update fails at repository flush under the inspected SQL constraint. A failed transaction is not reused.

**Test case design**

Separate round-trip, duplicate ISBN update, duplicate username and invalid-role cases. Identify the intended SQL constraint and synchronization boundary for each.

**What to implement**

Test `saveAndFlush` + `clear` + reload. Then persist two valid books, mutate the second to duplicate the first ISBN, call `save(second)`, and assert failure when calling `books.flush()`. Add separate duplicate-username and invalid-role constraint cases.

**Starter code with TODOs**

```java
Book first = books.saveAndFlush(book("unique-isbn-1"));
Book second = books.saveAndFlush(book("unique-isbn-2"));
second.replace(first.getIsbn(), second.getTitle(), second.getAuthor(),
        second.getPrice(), second.getStock());
books.save(second);

// TODO: force synchronization and assert Spring's translated exception
```

**Expected behavior prediction**

Why is this update experiment more deterministic than promising a new IDENTITY insert waits for flush?

**Hints**

- IDENTITY may execute INSERT during `save` to obtain an ID.
- `BookRepository.flush()` crosses Spring repository exception translation; raw `EntityManager.flush()` can expose a Hibernate exception instead.

**How to run and verify**

```powershell
mvn -Dtest=RepositoryPostgresIntegrationTest test
```

Temporarily inspect SQL: the duplicate UPDATE appears at flush and the test transaction rolls back.

**Common mistakes**

- Reading the same managed object and claiming a round trip.
- Continuing database work after a constraint violation in the same transaction.
- Saying flush committed data.

**What the test proves and does not prove**

Flush synchronizes the persistence context; clear forces future reads beyond its L1 cache; the test transaction still rolls back.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 11](Testing_Solution.md#task-11).

<a id="task-12"></a>

### Task 12 — Prove Flyway owns the test schema

**Objective**

Verify the empty test database is migrated before Hibernate uses it.

**Prerequisites**

Tasks 10–11; authoritative source migrations restored separately; Flyway enabled and Hibernate validation retained.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#persistence). The integration test must reproduce production schema ownership.

**Requirements / behavior to test**

After authoritative source migrations are restored separately, Flyway must apply V1–V6 before Hibernate validates the schema. Current target-only SQL is insufficient for a clean build.

**Test case design**

Predict six successful history versions and the presence of books, transaction_audits and app_users. List what fresh installation proves and what an upgrade test would still need.

**What to implement**

Import `FlywayAutoConfiguration`, keep `ddl-auto=validate`, and query `flyway_schema_history` for the six successful versioned migrations and inspect their identities. Record the missing source prerequisite before attempting execution.

**Starter code with TODOs**

```java
@ImportAutoConfiguration(TODO.class)
class RepositoryPostgresIntegrationTest {
    @Autowired JdbcTemplate jdbc;

    @Test
    void startup_flywayAppliedAllSixMigrations() {
        // TODO: query successful history row count
    }
}
```

**Expected behavior prediction**

What failure appears if a migration and entity mapping disagree?

**Hints**

- Boot 4 class: `org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration`.
- V1 already contains `version`; V2 logs that its idempotent add is skipped. Both history rows are still successful when those authoritative migrations run.

**How to run and verify**

```powershell
mvn -Dtest=RepositoryPostgresIntegrationTest test
```

Look for the order: container → Flyway V1–V6 → Hibernate validation → test.

**Common mistakes**

- Switching to `ddl-auto=create`.
- Assuming `@DataJpaTest` automatically imports every modular integration.
- Merely asserting that a table exists while ignoring migration history.

**What the test proves and does not prove**

This test proves the checked-in migration chain builds a usable current schema from nothing.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 12](Testing_Solution.md#task-12).

<a id="task-13"></a>

### Task 13 — Test real transaction boundaries

**Objective**

Verify existing rollback, checked-exception, and `REQUIRES_NEW` behavior.

**Prerequisites**

Tasks 9–12; inspect all transaction services and propagation/rollback annotations.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#transactions). State after an exception is the transaction contract; a test-owned transaction can conceal it.

**Requirements / behavior to test**

Test existing success, runtime rollback, flush-then-rollback, checked default commit, checked rollbackFor, caught REQUIRED failure and REQUIRES_NEW audit, plus intentional delete rollback.

**Test case design**

Start stocks at 10 and 5; transfer 3. For all seven scenarios, predict returned/raised outcome, both final stocks, audit event/count and whether SQL can have executed. Add the delete row-survival case.

**What to implement**

Create `TransactionBehaviorIntegrationTest` with no test `@Transactional`. Explicitly clean/seed. Cover all seven transaction scenarios in the design matrix plus intentional delete rollback. Preserve the original runtime, checked-default and independent-audit cases.

**Starter code with TODOs**

```java
@Test
void requiresNewAudit_outerFailureRollsBackStockButAuditSurvives() {
    assertThatThrownBy(() ->
            transfers.requiresNewAuditThenFail(fromId, toId, 3))
            .isInstanceOf(IllegalStateException.class);

    // TODO: original stocks
    // TODO: exactly one TRANSFER_ATTEMPT audit
}
```

**Expected behavior prediction**

For each scenario, write expected stock and audit rows before running.

**Hints**

- Reread through repositories after the service returns/throws.
- `flushThenRuntimeRollback` would still roll back emitted SQL.
- `BookService.delete` is intentionally not a normal successful delete.

**How to run and verify**

```powershell
mvn -Dtest=TransactionBehaviorIntegrationTest test
```

Run the class alone and map transaction logs to begin/suspend/resume/commit/rollback.

**Common mistakes**

- Annotating the test with `@Transactional`.
- Asserting only exception type.
- “Fixing” the checked-default-commit demonstration.

**What the test proves and does not prove**

The real service proxies decide rollback rules. Repository rereads observe durable outcomes after those boundaries complete.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 13](Testing_Solution.md#task-13).

<a id="task-14"></a>

### Task 14 — Test real database-backed authentication

**Objective**

Go beyond mock identities and exercise the complete login path.

**Prerequisites**

Tasks 8–13; shared database support; inspect registration and form-login configuration.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#security). Form login is handled by Spring Security filters and persists a `SecurityContext` in the session.

**Requirements / behavior to test**

Registration always persists USER with an encoded password. Real form login returns 204; the same session can access /auth/me. Wrong credentials return 401 without authentication.

**Test case design**

Model anonymous -> registered-but-anonymous -> authenticated -> later authenticated request, plus wrong-login remaining anonymous. Specify which state/response proves each transition.

**What to implement**

Create a full-context MockMvc test with PostgreSQL. Register a disposable USER, obtain a CSRF token/session, submit form parameters to `/auth/login`, retain the session, then call `/auth/me`. Add a wrong-password test.

**Starter code with TODOs**

```java
MvcResult login = mockMvc.perform(post("/auth/login")
        .session(csrf.session())
        .param(csrf.parameterName(), csrf.token())
        .param("username", "session-user")
        .param("password", TEST_PASSWORD))
    // TODO: 204 and authenticated username
    .andReturn();

// TODO: GET /auth/me with the returned session
```

**Expected behavior prediction**

Which real components run that `@WithMockUser` bypasses?

**Hints**

- Login consumes form parameters, not JSON.
- Registration is `permitAll` but still needs CSRF.
- Assert encoder matching without logging the raw value/hash.

**How to run and verify**

```powershell
mvn -Dtest=AuthenticationFlowIntegrationTest test
```

Assert saved role `USER`, stored value differs from raw, login is 204, and `/auth/me` returns `ROLE_USER` with the same session.

**Common mistakes**

- Using `@WithMockUser` in the real-login test.
- Posting JSON credentials to `/auth/login`.
- Printing security artifacts to diagnose failure.

**What the test proves and does not prove**

This path runs the login filter, authentication manager/provider, application `UserDetailsService`, repository, BCrypt, SecurityContext, and session.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 14](Testing_Solution.md#task-14).

<a id="task-15"></a>

### Task 15 — Complete CSRF, ADMIN write, session, and logout flow

**Objective**

Prove an authenticated session survives requests and logout invalidates it.

**Prerequisites**

Task 14; inspect CSRF endpoint and logout configuration.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#security). CSRF token state and authentication state both participate; roles remain separate.

**Requirements / behavior to test**

An ADMIN seeded directly can log in and create a persisted book using a fresh CSRF token. Logout returns 204 and invalidates the USER session; later anonymous /auth/me is 401.

**Test case design**

Extend the state model through refreshed CSRF, authorized persisted write and logout invalidation. Explain which session/token must travel with each request.

**What to implement**

Create a helper that calls `GET /auth/csrf` and returns session, parameter name, header name, and token. Seed an ADMIN directly, log in, obtain a fresh token, create a book, and prove persistence. In the USER flow, refresh the token, log out, assert invalidation, then verify anonymous `/auth/me` is 401.

**Starter code with TODOs**

```java
private CsrfData csrf(MockHttpSession existingSession) throws Exception {
    // TODO: GET /auth/csrf, optionally reuse session
    // TODO: parse parameterName, headerName, token without logging them
}

private record CsrfData(
        MockHttpSession session,
        String parameterName,
        String headerName,
        String token) { }
```

**Expected behavior prediction**

Why can registration not create the ADMIN used for the successful book write?

**Hints**

- `RegistrationService` always assigns `USER`.
- Fetch a token after login before an unsafe write/logout.
- MockMvc uses `MockHttpSession`; assert it becomes invalid.

**How to run and verify**

```powershell
mvn -Dtest=AuthenticationFlowIntegrationTest test
```

Assert ADMIN create returns 201 and the repository has the book. Assert logout returns 204, `unauthenticated()`, and an invalidated session.

**Common mistakes**

- Sharing USER and ADMIN identity assumptions.
- Replacing real login with request `user(...)`.
- Logging tokens/cookies/session IDs.

**What the test proves and does not prove**

This is the broadest core test: Security filters, MVC, service proxies, JPA, Flyway-built PostgreSQL, BCrypt, session, and CSRF are real.

Its evidence is limited to the declared cases and real boundary; it does not establish behavior of components excluded or replaced by mocks.

After attempting: [Solution Task 15](Testing_Solution.md#task-15).

<a id="task-16"></a>

### Task 16 — Run the Maven suite and explain every boundary

**Objective**

Make the suite reproducible outside the IDE and audit its value.

**Prerequisites**

All attempted tasks; for full runtime verification the migration prerequisite and Docker must be ready.

**Related concepts**

[Theory](Testing_Essential_Concepts.md#delivery). A green suite is useful only if you understand what each test proves and omits.

**Requirements / behavior to test**

A clean source build must discover and execute the intended tests. Record passed, failed, skipped and blocked work separately; no runtime claim follows from compilation alone.

**Test case design**

Trace every important requirement to a test. Identify at least one meaningful gap that line coverage could miss, and review a hypothetical AI-generated test for an invented rule or mocked-away integration.

**What to implement**

Run clean Maven verification with Docker available. Build a final matrix containing test level, context, database, mocks, security identity, transaction, proven behavior, and limitation.

**Starter code with TODOs**

```powershell
# TODO: cleanly compile and run every test
mvn TODO

# TODO: run through Maven's verification phase
mvn TODO
```

**Expected behavior prediction**

Which classes should dominate runtime, and why?

**Hints**

- Surefire discovers the `*Test` names used here.
- `verify` does not imply Failsafe separation unless configured.
- Context caching can reuse identical configurations; different slices need different contexts.

**How to run and verify**

```powershell
mvn clean test
mvn verify
```

After the source-migration prerequisite is fixed separately, expect all tests green and no connection to the configured developer database. Inspect the dynamic Testcontainers JDBC port, migration count, and Maven summary.

**Common mistakes**

- Running only from the IDE.
- Treating a missing Docker daemon as a reason to use H2.
- Adding test order to hide leaked state.

**What the test proves and does not prove**

Maven is the reproducible entry point. The final matrix prevents false claims about what a green test means.

This is a design/build audit; it does not establish application behavior without executed assertions.

After attempting: [Solution Task 16](Testing_Solution.md#task-16).

## Optional next steps

After the core suite works, consider `@Nested`, a small fixture builder, optimistic locking with two persistence contexts, a random-port HTTP test or separate Surefire/Failsafe lifecycles. Coverage and CI configuration remain optional study examples in the theory; this update installs neither.

<a id="competency-checklist"></a>

## Final learning competency checklist

Mark a row complete only when you can demonstrate it from a blank test file and explain its boundary without copying the reference.

| Done | Competency | Successful completion evidence |
|---|---|---|
| [ ] | Derive cases from requirements | Produce a matrix tracing each important criterion to preconditions, inputs/action, expected state/output and a test level; flag ambiguity instead of inventing it. |
| [ ] | Identify partitions and boundaries | Choose valid/invalid classes and values at/around the real limits; explain normalized characters versus UTF-8 bytes where relevant. |
| [ ] | Write JUnit tests | Write grouped assertions, narrow exception assertions and parameterized cases; predict invocations and explain lifecycle isolation. |
| [ ] | Use Mockito properly | Keep the subject real, stub only relevant collaborators, capture saved data and verify meaningful forbidden calls; explain why the test cannot prove persistence. |
| [ ] | Write service tests | Cover registration success, duplicate paths and byte-limit rejection, asserting role/hash and side effects. |
| [ ] | Test controllers with MockMvc | Prove mapping, selected JSON/status/Location, invalid-body rejection and advice; explain the effect of enabled/disabled filters. |
| [ ] | Test repositories | Use real mappings and derived queries, stable sort/paging, flush/clear/reread and independent constraint cases. |
| [ ] | Use PostgreSQL/Testcontainers | Supply dynamic connection details and source-owned migrations; explain the difference between a container-runtime failure and an assertion failure. |
| [ ] | Verify commit/rollback | Call real proxies without an outer test transaction and explain final stocks/audits for every transaction scenario, including flushed rollback and REQUIRES_NEW. |
| [ ] | Test authorization | Execute anonymous/USER/ADMIN and CSRF decisions, and invoke method security directly through its proxy. |
| [ ] | Test real authentication | Register/seed a real account, perform form login with CSRF, retain the session, check /auth/me, wrong credentials and logout invalidation without mock users. |
| [ ] | Diagnose failures | Read Surefire/root causes, locate the failing layer, compare fixture/configuration/runtime and reproduce one failing class through Maven. |
| [ ] | Understand coverage limits | Explain a case with full statement coverage but incomplete branches, and a requirement bug missed even at full branch coverage. Interpret a JaCoCo report without adding meaningless assertions. |
| [ ] | Run reliably through Maven | Show a clean source build, actual discovered/executed counts and no hidden dependence on target artifacts, IDE state, test order or developer databases. Record blocked integration runs honestly. |
| [ ] | Review AI-generated tests | Identify invented APIs/rules, tautological expectations, over-mocking, disabled protections and missing negative cases; repair the design manually before accepting code. |

Successful completion means independently producing and explaining a meaningful layered suite. If database prerequisites are unresolved, mark those execution competencies pending even if your code compiles. A passing historical report or copied reference is not evidence of your current suite.
