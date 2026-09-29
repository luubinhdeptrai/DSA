# Testing Essential Concepts for `book-catalog-api`

Testing is not a separate island after Java, Spring, persistence, transactions, and security. It is the executable way to ask whether those layers still cooperate as intended.

## Priority legend

- ⭐⭐⭐⭐⭐ **MUST KNOW** — expected in day-to-day backend work and interviews.
- ⭐⭐⭐⭐ **IMPORTANT** — needed for reliable professional tests.
- ⭐⭐⭐ **USEFUL** — common when a suite grows.
- ⭐⭐ **NICE TO KNOW** — valuable context, but not the first priority.
- ⭐ **FUTURE KNOWLEDGE** — learn after the core suite is healthy.

## Verified project baseline

This guide is tied to the checked-in project at `Spring_Security/book-catalog-api`; it is not a generic Boot 2 or Boot 3 tutorial.

| Item | Actual checked-in/managed value | Testing consequence |
|---|---|---|
| Java | 21 (`maven.compiler.release`) | Tests may use records, text blocks, and modern Java syntax. |
| Spring Boot | 4.1.1 | Use Boot 4's modular test starters and Boot 4 annotation packages. |
| Spring Framework | 7.0.9 | Use Framework `@MockitoBean`; do not teach deprecated Boot `@MockBean`. |
| Spring Security | 7.1.1 | Use matching `spring-security-test` support through Boot's security test starter. |
| Existing JUnit | direct JUnit 4.11 | Remove it and migrate `AppTest` to meaningful JUnit Jupiter tests. |
| Boot-managed JUnit Jupiter | 6.0.3 | `org.junit.jupiter.*`; no Vintage engine is needed after migration. |
| Boot-managed Mockito | 5.23.0 | Use Mockito's Jupiter extension for plain unit tests. |
| PostgreSQL server | 17 in `compose.yaml` | Use PostgreSQL 17 in Testcontainers, not H2. |
| PostgreSQL JDBC driver | 42.7.13 | Boot manages the version. |
| Flyway | 12.4.0 | Run the real V1–V6 migrations in database integration tests. |
| Testcontainers | 2.0.5 | Use the non-deprecated `org.testcontainers.postgresql.PostgreSQLContainer`. |
| Hibernate ORM | 7.4.5.Final | Persistence-context, flush, and optimistic-lock behavior are real concerns. |
| HikariCP | 7.0.2 | A full database test still obtains JDBC connections through the pool. |
| Existing test | `com.example.AppTest`, JUnit 4, `assertTrue(true)` | It proves no application behavior and should be removed. |

The project owns its schema with Flyway and sets `spring.jpa.hibernate.ddl-auto: validate`. Its current test dependencies do **not** include Boot test support, Security test support, or Testcontainers.

Important checked-in behavior that tests must not rewrite:

- `BookService.delete` deletes and flushes, then intentionally throws a checked exception under `rollbackFor`; an existing book remains.
- transaction-lab methods deliberately demonstrate commit, rollback, `REQUIRED`, and `REQUIRES_NEW` behavior.
- registration always assigns `USER` server-side.
- login is form-filter based at `POST /auth/login`, not a JSON controller method.
- `StockRequest` currently has no constraints; a test must not pretend negative stock is rejected by MVC validation.
- validation errors currently use the literal placeholder title/detail `some fields failed (title)` and `some field failed (detail)`.
- the database has uniqueness and nullability constraints, plus an `app_users.role` check; it has no nonnegative `books.price` or `books.stock` check.

---

## 1. Why testing exists — ⭐⭐⭐⭐⭐ MUST KNOW

```text
code
  -> expected observable behavior
  -> executable verification
  -> fast feedback
  -> safer refactoring
```

A useful test states a behavior, arranges the relevant boundary, exercises it, and fails for a reason that helps diagnose a regression. `assertTrue(true)` executes a line but provides no confidence.

Testing provides:

- **confidence** that important paths work;
- **regression prevention** when code changes later;
- **living documentation** of input, output, error, and state contracts;
- **refactoring safety** because behavior is checked independently of implementation shape;
- **design feedback** because code that is impossible to isolate often has tangled responsibilities.

Coverage asks whether code was executed. Correctness asks whether all required behavior is right for relevant inputs, state, timing, failures, and integrations. Therefore:

```text
100% line coverage != 100% correctness
```

A test that calls `RegistrationService.register` but never verifies the assigned role can cover the line that creates an `AppUser` while missing a privilege-escalation bug.

## 2. Testing levels — ⭐⭐⭐⭐⭐ MUST KNOW

Names vary between teams, so always state the boundary explicitly.

| Level | What is real? | What is mocked? | Spring? | PostgreSQL? | Typical speed | Project example |
|---|---|---|---|---|---|---|
| Unit test | One Java object and simple values | Collaborator boundaries | No | No | milliseconds | `RegistrationService` with mocked repository and encoder |
| Spring slice test | One framework slice such as MVC or JPA | Other application layers, where appropriate | Partial context | No for MVC; yes for this project's JPA slice | tens to hundreds of ms plus container startup for DB | `@WebMvcTest(BookController.class)` or PostgreSQL `@DataJpaTest` |
| Integration test | Several real components cooperating | Only out-of-scope systems, if any | Often | Often | slower | Flyway + Hibernate + `BookRepository` + PostgreSQL |
| Full-context integration test | Nearly all application beans and filters | Usually none | Full context | Yes when persistence is involved | seconds | real form login, session, CSRF, controller, service, repository |
| End-to-end test | Deployed/running system from external client to database | Ideally nothing inside the system | Real server | Real test database | slowest | client calls a random-port server and checks the complete flow |

`@SpringBootTest` is not a synonym for “good test.” Starting every bean, security configuration, Flyway, Hibernate, HikariCP, and PostgreSQL to prove a record trims whitespace makes the suite slow and makes a failure harder to locate.

> **Use the smallest test scope that gives confidence in the behavior.**

## 3. Practical test pyramid — ⭐⭐⭐⭐ IMPORTANT

```text
many fast unit tests
        ↓
fewer slice/integration tests
        ↓
few full-flow/end-to-end tests
```

This is a cost model, not a quota. Unit tests are fast and diagnostic but cannot prove wiring, proxy advice, SQL, filters, or migrations. Full tests are realistic but slower, have more failure causes, and cost more to maintain. Choose the mix from risk:

- password role assignment is cheap to prove in a unit test;
- a unique constraint needs PostgreSQL;
- `@PreAuthorize` needs a Spring proxy;
- login/session/logout needs the Security filter chain and real authentication components.

## 4. AAA and Given–When–Then — ⭐⭐⭐⭐⭐ MUST KNOW

AAA and Given–When–Then describe the same shape:

```java
@Test
void replace_validValues_changesObservableBookState() {
    // Arrange / Given
    Book book = new Book("old", "Old", "Author",
            new BigDecimal("10.00"), 2);

    // Act / When
    book.replace("new", "New", "Writer",
            new BigDecimal("12.50"), 5);

    // Assert / Then
    assertAll(
            () -> assertEquals("new", book.getIsbn()),
            () -> assertEquals("New", book.getTitle()),
            () -> assertEquals(5, book.getStock()));
}
```

Good names include `method_condition_expectedResult`, such as `register_existingUsername_throwsConflict`, or sentence-style names with `@DisplayName`. Name the behavior and condition, not `test1` or an implementation detail.

## 5. Good test characteristics and flakiness — ⭐⭐⭐⭐⭐ MUST KNOW

A good test is:

- deterministic;
- isolated from unrelated tests;
- repeatable locally and in CI;
- readable;
- fast enough for its layer;
- independent of execution order;
- focused on observable behavior.

A flaky test sometimes passes and sometimes fails without a relevant code change. It trains developers to ignore failures.

Avoid:

- `Thread.sleep(...)` as synchronization;
- test-order dependencies;
- uncontrolled calls to the current clock;
- shared mutable static state;
- random values without a recorded/controlled seed;
- external internet services;
- production databases.

This connects directly to concurrency: sleeping does not establish a happens-before relationship or prove work completed. Prefer a completion signal, future/join, latch, polling with a bounded timeout, or Awaitility when asynchronous behavior truly exists.

## 6. JUnit architecture — ⭐⭐⭐⭐⭐ MUST KNOW

| Piece | Owner and job | This project |
|---|---|---|
| JUnit Platform | Launching/discovery infrastructure used by Maven and IDEs | managed with JUnit 6.0.3 |
| JUnit Jupiter | Modern programming and extension model: `@Test`, assertions, lifecycle, parameterized tests | use this |
| JUnit Vintage | Engine for old JUnit 3/4 tests | unnecessary after deleting the JUnit 4 `AppTest` |

Essential Jupiter annotations:

- `@Test`: one test case.
- `@BeforeEach` / `@AfterEach`: per-test setup/cleanup; use setup only when it improves clarity.
- `@BeforeAll` / `@AfterAll`: class-level lifecycle; methods are normally static unless using per-class lifecycle.
- `@Nested`: group related scenarios while sharing readable setup.
- `@DisplayName`: human-readable report name.
- `@ParameterizedTest`: run one behavior with several inputs.
- `@ValueSource`: simple one-column arguments.
- `@CsvSource`: several scalar arguments per invocation.

Do not use lifecycle methods to hide essential test data. A reader should still see why each test passes.

## 7. Assertions — ⭐⭐⭐⭐⭐ MUST KNOW

JUnit provides focused assertions:

```java
assertEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(value);
assertNotNull(value);
assertThrows(BookNotFoundException.class, () -> service.findById(99L));
assertDoesNotThrow(() -> request.title());
assertAll(() -> assertEquals("A", book.getTitle()),
          () -> assertEquals(3, book.getStock()));
```

AssertJ, included by Boot's test starter, is fluent and often gives richer diagnostics:

```java
assertThat(details.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_USER");

assertThatThrownBy(() -> service.findById(99L))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessage("Book with id: 99 is not found");
```

JUnit assertions are excellent for small direct checks. AssertJ shines for collections, exceptions, object graphs, and chained conditions. Consistency inside a test matters more than ideology.

## 8. Exception testing — ⭐⭐⭐⭐⭐ MUST KNOW

An exception is only one part of a failure contract. Verify what matters:

1. exact exception type;
2. message only when it is a stable contract;
3. collaborator interaction or non-interaction;
4. state after failure;
5. database commit or rollback when relevant.

```java
BookNotFoundException thrown = assertThrows(
        BookNotFoundException.class,
        () -> service.findById(41L));
assertEquals("Book with id: 41 is not found", thrown.getMessage());
```

For `BookService.delete`, a Mockito test can prove `delete` and `flush` were called and an exception was thrown. It **cannot** prove rollback; only an integration test that calls the proxied service and rereads PostgreSQL can prove the row remains.

## 9. Test doubles — ⭐⭐⭐⭐ IMPORTANT

| Double | Practical meaning | Project example |
|---|---|---|
| Dummy | Passed only to satisfy a signature; behavior is irrelevant | an unused argument in a focused callback test |
| Stub | Returns a prepared answer | repository returns an existing user |
| Mock | Records interactions and is verified | verify the encoder and repository calls |
| Fake | Working simplified implementation | an in-memory repository, if its semantics are sufficient (not suitable for PostgreSQL constraints) |
| Spy | Wraps a real object while allowing selective stubbing/verification | rare; perhaps observe one collaborator without replacing all behavior |

Mockito objects can act as stubs and mocks. The useful distinction is why the test uses them. Excessive spying couples tests to internal calls and can bypass proxy behavior.

## 10. Mockito — ⭐⭐⭐⭐⭐ MUST KNOW

Plain Mockito tests do not start Spring:

```java
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock AppUserRepository users;
    @Mock PasswordEncoder encoder;
    @InjectMocks RegistrationService service;
}
```

Core operations:

```java
when(users.existsByUsername("alice")).thenReturn(false);
when(encoder.encode("test-password-123")).thenReturn("encoded");
when(users.saveAndFlush(any(AppUser.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

verify(users).saveAndFlush(any(AppUser.class));
verifyNoInteractions(encoder);
verifyNoMoreInteractions(users, encoder);

ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
verify(users).saveAndFlush(captor.capture());
assertThat(captor.getValue().getRole()).isEqualTo("USER");
```

Use `eq(...)`, `any(...)`, and other `ArgumentMatchers` consistently: if one argument uses a matcher, all arguments in that invocation must use matchers.

- **State verification** checks the returned value or changed object/database.
- **Interaction verification** checks an important boundary call, such as “do not encode or save after duplicate precheck.”

Do not verify every internal line. Do not mock simple records or value objects. Private methods are implementation details; exercise them through public behavior. If a private method seems to need direct tests, consider whether it contains a separate responsibility that deserves its own type.

## 11. Unit-testing actual services — ⭐⭐⭐⭐⭐ MUST KNOW

Good unit boundaries in this project are:

- `RegistrationService` → mock `AppUserRepository` and `PasswordEncoder`;
- `AppUserDetailsService` → mock `AppUserRepository`;
- `BookService` → mock `BookRepository` for lookup, translation, pagination, and collaboration logic.

A registration success test should capture the saved entity and prove:

- the raw password was passed to `encode`;
- the saved hash is the encoder result, not the raw value;
- the role is exactly `USER` regardless of client wishes;
- `saveAndFlush` is called.

Duplicate tests should cover both the fast precheck and the database-race path. The unique constraint remains the final authority because two requests can both observe “not present” concurrently.

Do not claim a plain unit test proves `@Transactional`, `@PreAuthorize`, constraint enforcement, or BCrypt itself. Annotations and collaborators are inert unless their infrastructure runs.

## 12. Spring TestContext Framework — ⭐⭐⭐⭐ IMPORTANT

When a Spring test starts a context, the TestContext Framework:

1. derives configuration and profiles/properties;
2. creates or reuses an `ApplicationContext`;
3. registers beans and proxy post-processors;
4. injects the test instance;
5. runs test listeners, including security and transactional listeners.

Contexts are cached by a key derived from configuration, profiles, properties, imports, and bean overrides. Tests with the same key can reuse an expensive context. Many slightly different `@MockitoBean` sets or properties create many cache keys.

`@DirtiesContext` evicts a context because a test changed singleton/context state. It is a last resort, not general database cleanup. Prefer restoring the particular state you changed. Rebuilding the full context and restarting a container makes suites slow.

Profiles and test properties can select safe logging/configuration. Never activate the `local` profile in integration tests: `LocalAdminBootstrap` requires external admin secrets. Use a `test` profile and explicit test data.

## 13. Spring Boot 4 test dependencies and ownership — ⭐⭐⭐⭐⭐ MUST KNOW

Boot 4.1 is modular. The base starter supplies JUnit Jupiter, Spring Test, Mockito, AssertJ, Hamcrest, JSONassert/JsonPath, Awaitility, and Boot test support. The MVC, JPA, and Security test starters supply their focused modules and depend transitively on the base starter.

For this project, use versionless test dependencies managed by Boot:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security-test</artifactId>
  <scope>test</scope>
</dependency>
```

`spring-boot-starter-security-test` brings `spring-security-test`. Remove `junit:junit:4.11`; do not add Vintage merely to preserve the meaningless generated test.

The local POM also pins Maven Surefire 2.22.1 inside `pluginManagement`, overriding Boot's managed 3.5.6. Remove the stale plugin pins and let the Boot parent manage the test runner.

Ownership summary:

| Component | Who provides it? | What do you write? | Starts Spring? | Real DB? |
|---|---|---|---|---|
| JUnit Jupiter | JUnit | test methods and lifecycle | No | No |
| Mockito | Mockito | mock setup and verification | No | No |
| Spring TestContext | Spring Framework | context test configuration | Yes | only if configured |
| Boot test annotations | Spring Boot | slice/full test classes | Yes | depends on slice |
| MockMvc | Spring Framework/Boot configuration | request and response assertions | MVC context | No server; DB depends on context |
| Testcontainers | Testcontainers | container declaration | No by itself | starts real disposable PostgreSQL |
| Flyway | Flyway + Boot integration | versioned migrations | runs during DB context startup | Yes |
| HikariCP | Hikari + Boot | normally configuration only | part of DB context | connects to PostgreSQL |

## 14. `@SpringBootTest` — ⭐⭐⭐⭐⭐ MUST KNOW

`@SpringBootTest` asks Boot to find `@SpringBootConfiguration` (`Application`) and build the full application context. It does **not** magically start PostgreSQL; a `DataSource` still needs a reachable database or Testcontainers connection. Beans are real unless explicitly replaced.

`webEnvironment` modes:

| Mode | Meaning |
|---|---|
| `MOCK` (default) | Web application context with mock servlet environment; combine with `@AutoConfigureMockMvc`. |
| `RANDOM_PORT` | Start the embedded server on an available port; useful for true HTTP-client tests. |
| `DEFINED_PORT` | Start on the configured port; collision-prone in tests. |
| `NONE` | Non-web application context. |

Use it for real authentication/session flow, cross-layer transaction behavior, and broad wiring smoke tests. Do not use it for DTO normalization or isolated service branching.

## 15. Boot 4 test slices — ⭐⭐⭐⭐⭐ MUST KNOW

Boot 4.1 package names differ from many older tutorials:

```java
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
```

| Slice | Loads | Excludes | Replacements/config needed | Project use |
|---|---|---|---|---|
| `@WebMvcTest` | MVC infrastructure, selected controller(s), converters, validation, controller advice | services, repositories, general components | `@MockitoBean` service collaborators; import the actual security config only for security-policy tests | mappings, JSON, validation, ProblemDetail |
| `@DataJpaTest` | DataSource/JDBC/JPA, repositories, entity manager; tests are transactional by default | MVC and normal service layer | real PostgreSQL service connection, no DB replacement, and explicit Flyway auto-configuration in Boot 4's focused slice | mappings, derived queries, constraints, flush behavior |

Do not stack multiple `@...Test` slice annotations. Choose one slice and import only the additional infrastructure that the behavior requires.

For controller-only tests it is acceptable to use `@AutoConfigureMockMvc(addFilters = false)` **when the test explicitly says security is out of scope**. A separate security-policy test must keep filters enabled and import the real `SecurityConfig`.

## 16. `@Mock` versus a mocked Spring bean — ⭐⭐⭐⭐⭐ MUST KNOW

```text
Mockito @Mock
  -> creates a plain Mockito object
  -> no ApplicationContext

Spring Framework @MockitoBean
  -> adds/replaces a bean in the test ApplicationContext
  -> controllers and other Spring beans receive that mock
```

Boot 4.1 / Spring Framework 7 code:

```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(BookController.class)
class BookControllerWebMvcTest {
    @MockitoBean
    BookService bookService;
}
```

Old examples often use `org.springframework.boot.test.mock.mockito.MockBean`. Do not copy them into this project; `@MockitoBean` is the current Framework mechanism. Every different bean override also affects context-cache identity, so keep override sets consistent where practical.

## 17. Controller testing with MockMvc — ⭐⭐⭐⭐⭐ MUST KNOW

MockMvc sends a mock servlet request through `DispatcherServlet`, handler mapping, argument resolution, JSON conversion, validation, controller advice, and—when enabled—the Security filter chain. It does not open a TCP port.

```java
mockMvc.perform(get("/books/{id}", 7))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.isbn").value("isbn-7"));
```

For create behavior assert the status, `Location`, content type, and selected JSON fields. For list behavior use real query parameters. Avoid comparing a complete raw JSON string: property order is not the contract.

MockMvc can prove HTTP mapping/serialization. If `BookService` is mocked, it cannot prove database persistence or transactions.

## 18. Validation testing — ⭐⭐⭐⭐⭐ MUST KNOW

Two tests answer different questions:

- a pure validator/record test asks whether constraints and normalization exist on a DTO;
- an MVC test asks whether request JSON is deserialized, `@Valid` executes, the advice maps failures, and the service is not called.

For invalid `BookRequest`, the actual response includes:

```json
{
  "type": "about:blank",
  "title": "some fields failed (title)",
  "status": 400,
  "detail": "some field failed (detail)",
  "instance": "/books",
  "fieldErrors": {
    "isbn": ["isbn must not be blank"]
  }
}
```

Assert stable pieces and `verifyNoInteractions(bookService)`. `RegisterRequest` exercises `@NotBlank`, `@Pattern`, and `@Size`. No checked-in DTO uses a bean-validation `@Pattern` except registration.

Do not write false tests:

- `StockRequest` has no constraints;
- `BookRequest.price` has no `@Digits`, so validation does not enforce two decimal places;
- the size upper-bound message says “at least 1000,” although the constraint is `@Max(1000)`.

Tests often reveal such gaps. Report them; do not silently redesign production behavior in a testing lesson.

## 19. Testing `@RestControllerAdvice` — ⭐⭐⭐⭐⭐ MUST KNOW

Testing an exception class proves Java behavior. Testing the advice proves HTTP mapping.

Actual mappings include:

| Failure | HTTP | Actual title | Actual detail |
|---|---:|---|---|
| `BookNotFoundException(id)` | 404 | `Book not found` | `Book with id: {id} is not found` |
| `DuplicateIsbnException` | 409 | `ISBN conflict` | `Isbn is duplicated` |
| invalid arguments/body | 400 | checked-in validation/unreadable title | checked-in detail and optionally `fieldErrors` |
| intentional `IllegalStateException` | 500 | `Test Transaction` | exception message |
| unexpected exception | 500 | `Internal Server Error` | sanitized generic detail |

Advice-created ProblemDetails normally include `instance`. The filter-level handwritten 401/403 JSON does not. Use `contentTypeCompatibleWith("application/problem+json")` and JSON paths, not property order.

## 20. Repository testing — ⭐⭐⭐⭐⭐ MUST KNOW

Repository tests should prove things the repository/mapping/database could get wrong:

- `AppUserRepository.findByUsername` and `existsByUsername`;
- `BookRepository.findByAuthor` exact matching and paging/order;
- column/entity mappings;
- PostgreSQL unique, nullability, role check, numeric, timestamp, or identity behavior;
- optimistic locking and version updates when intentionally tested.

Do not write a test whose only assertion is that `JpaRepository.save()` exists. Spring Data already tests its framework. Test your method names, mappings, migrations, and constraints.

`TransactionAuditRepository` has no custom query, so it is most valuable in transaction integration tests that prove which audit row commits.

## 21. Persistence-context pitfalls — ⭐⭐⭐⭐⭐ MUST KNOW

Key terms:

- **persist**: make a new entity managed; SQL timing depends on strategy and flush requirements.
- **dirty checking**: Hibernate detects changes to managed entities.
- **flush**: synchronize pending persistence-context changes with SQL/database; it is not a commit.
- **clear**: detach managed entities and empty the first-level cache.
- **first-level cache**: one persistence context can return the same in-memory instance without a database round trip.

False confidence example:

```java
Book saved = books.saveAndFlush(book);
Long id = saved.getId();

entityManager.clear();             // force the next lookup beyond L1 cache
Book reloaded = books.findById(id).orElseThrow();
assertThat(reloaded.getIsbn()).isEqualTo("isbn-1");
```

Use `flush()` or `saveAndFlush(...)` when the assertion requires SQL/constraint execution, then `clear()` when the assertion requires a database reread.

Nuance: all project entities use `GenerationType.IDENTITY`, so an INSERT may occur during `save()` to obtain the ID. Never promise all INSERT errors wait for explicit flush. Flush is the deterministic synchronization boundary; failure can legally occur earlier.

To demonstrate deferred dirty-check SQL reliably, persist two valid books, mutate the second managed book to reuse the first ISBN, call `save(second)` (which need not issue an update), and assert the unique violation at `flush()`.

## 22. Testing database constraints — ⭐⭐⭐⭐⭐ MUST KNOW

Application validation improves client feedback; database constraints protect durable state and concurrent writers. Both are required.

Actual constraints worth testing:

- unique, non-null `app_users.username`;
- `app_users.role IN ('USER', 'ADMIN')`;
- unique `books.isbn` (nullable at the database level);
- non-null book title, author, price, stock, version;
- non-null transaction-audit columns.

Do **not** claim PostgreSQL rejects negative price or stock: the migrations contain no such `CHECK`. Do not claim ISBN length is 20 in the database: the DTO limit is 20, but V1 defines `VARCHAR(120)`.

For concurrency, the registration precheck is not enough. A real unique constraint catches the race; the service translates `DataIntegrityViolationException` to 409.

## 23. H2 versus PostgreSQL — ⭐⭐⭐⭐⭐ MUST KNOW

H2 is a different engine. Compatibility modes do not make it PostgreSQL. Differences can hide bugs in:

- dialect and DDL syntax;
- identity generation;
- constraint timing/names;
- locking and isolation;
- transaction behavior;
- numeric, timestamp-with-time-zone, and other data types;
- Flyway SQL and PostgreSQL metadata.

This project deliberately uses PostgreSQL migrations, identity columns, a role check, unique constraints, optimistic locking, and transaction propagation. Use disposable PostgreSQL, not H2.

## 24. Testcontainers with PostgreSQL — ⭐⭐⭐⭐⭐ MUST KNOW

Testcontainers starts an ephemeral real PostgreSQL process in a container, gives the test dynamic connection details, and destroys it after the owning lifecycle. It requires a working Docker-compatible container runtime; this is test infrastructure, not Dockerizing the application for deployment.

Boot 4.1 service-connection dependencies are versionless under Boot management:

```xml
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
```

Reusable Boot-managed container bean:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestPostgresConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:17")
                .withDatabaseName("book_catalog_test");
    }
}
```

`@ServiceConnection` publishes JDBC connection details; Boot configures the Hikari `DataSource` without hard-coded ports or credentials. Boot manages the bean lifecycle with the test context.

Alternative: a static JUnit Testcontainers `@Container` plus `@DynamicPropertySource` can register `spring.datasource.url`, username, and password. Use it for libraries without a Boot connection-details factory or custom properties. Do not use both approaches for the same connection.

## 25. Flyway in tests — ⭐⭐⭐⭐⭐ MUST KNOW

The desired startup is:

```text
empty PostgreSQL 17 container
  -> Hikari DataSource / pgJDBC
  -> Flyway V1–V6
  -> flyway_schema_history
  -> Hibernate ddl-auto=validate
  -> test method
```

This proves the production migration sequence can build a schema that current entities can use. `ddl-auto=create` would let Hibernate build a different schema and could conceal broken migrations. Keep Flyway as owner and Hibernate as validator.

Boot 4's focused `@DataJpaTest` does not automatically import every unrelated module. For this project explicitly import `FlywayAutoConfiguration` in the JPA slice and keep the real database:

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(TestPostgresConfiguration.class)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class RepositoryPostgresIntegrationTest { }
```

## 26. Transactional tests — ⭐⭐⭐⭐⭐ MUST KNOW

These annotations can look identical while controlling different transactions:

- `@Transactional` on an application service is production behavior, normally applied by a Spring proxy.
- `@Transactional` on a test is test infrastructure, normally wrapping the test and rolling it back afterward.

Transactional test rollback is excellent for repository isolation. It can hide:

- commit-time failures;
- detached behavior;
- incorrect service boundaries;
- `REQUIRES_NEW` survival;
- rollback-only failure at outer commit.

```text
flush != commit
```

Flush sends SQL inside the current transaction. A later rollback can undo it. Tests proving the transaction lab should have **no outer test transaction**, call the real proxied service, catch the expected exception, then query again through repositories.

## 27. Testing the existing transaction lab — ⭐⭐⭐⭐⭐ MUST KNOW

The current intentional truth table is:

| Scenario | Result | Stock state | Audit state |
|---|---|---|---|
| `SUCCESS_REQUIRED` | returns normally / HTTP 204 | committed transfer | `TRANSFER_COMPLETED` commits |
| `RUNTIME_ROLLBACK` | runtime failure / 500 | unchanged | REQUIRED audit rolls back |
| `FLUSH_THEN_RUNTIME_ROLLBACK` | runtime failure / 500 | unchanged despite emitted SQL | none |
| `CHECKED_DEFAULT_COMMIT` | checked failure / 500 | transfer commits by default rule | `CHECKED_DEFAULT_COMMITTED` commits |
| `CHECKED_ROLLBACK` | checked failure / 500 | unchanged | audit rolls back |
| `CAUGHT_REQUIRED_INNER_FAILURE` | outer commit throws `UnexpectedRollbackException` / 500 | unchanged | inner flushed audit rolls back |
| `REQUIRES_NEW_AUDIT` | outer runtime failure / 500 | outer stock changes roll back | `TRANSFER_ATTEMPT` remains committed |

Test the state, not only the exception. The `REQUIRES_NEW` call works because `TransactionAuditService` is a different proxied bean. Self-invocation would not cross the proxy.

`BookService.delete` is another existing lab: after its checked exception, reread the book and prove it still exists.

## 28. Spring Security testing — ⭐⭐⭐⭐⭐ MUST KNOW

`spring-security-test` supplies:

```java
@WithMockUser(username = "alice", roles = "USER")
```

and MockMvc helpers:

```java
get("/auth/me").with(user("alice").roles("USER"));
post("/books").with(user("admin").roles("ADMIN")).with(csrf());
formLogin("/auth/login").user("alice").password("test-password");
authenticated().withUsername("alice");
unauthenticated();
```

`@WithMockUser` and `user(...)` place a synthetic identity in the security context/request. They can prove URL/method authorization. They do **not** prove:

- `AppUserRepository` lookup;
- `AppUserDetailsService` mapping;
- BCrypt matching;
- `DaoAuthenticationProvider`;
- persistence of authentication in an HTTP session.

Use mock identity for authorization matrices; use real form login for the authentication flow.

## 29. SecurityFilterChain tests — ⭐⭐⭐⭐⭐ MUST KNOW

The actual policy matrix is:

| Request | Expected result | Why |
|---|---:|---|
| anonymous `GET /books` | controller response (normally 200) | GET is `permitAll` |
| anonymous `GET /auth/me` | 401 | authentication required |
| anonymous `POST /books` without CSRF | 403 | CSRF rejects before authorization |
| anonymous `POST /books` with valid CSRF | 401 | now authorization sees anonymous |
| USER `POST /books` with CSRF | 403 | lacks `ROLE_ADMIN` |
| ADMIN `POST /books` without CSRF | 403 | role does not bypass CSRF |
| ADMIN `POST /books` with CSRF | reaches controller | both controls pass |

401 means authentication is missing/invalid. 403 means an authenticated identity lacks authority **or** another access control such as CSRF rejected the request. Read which filter/handler produced the response.

Do not disable security in this test. Use a separate controller-contract slice with filters disabled only when security is explicitly outside that slice.

## 30. CSRF testing — ⭐⭐⭐⭐⭐ MUST KNOW

For unsafe methods, the test helper attaches a valid token:

```java
mockMvc.perform(post("/books")
        .with(user("admin").roles("ADMIN"))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validBookJson));
```

Also test the same request without `csrf()` and expect 403. `permitAll()` answers an authorization question; it does not disable exploit protection. Therefore `POST /auth/register` and `POST /auth/login` are permitted to anonymous users but still require CSRF.

For a realistic session flow, call `GET /auth/csrf`, keep its `MockHttpSession`, read `parameterName`/`headerName`/`token`, and send that exact token on login, protected writes, and logout.

## 31. Method-security testing — ⭐⭐⭐⭐⭐ MUST KNOW

The project has one method guard:

```java
@PreAuthorize("hasRole('ADMIN')")
public Book setStock(long id, Integer stock)
```

Test through the injected `BookService`, not `new BookService(...)`. The injected object is a Spring AOP proxy:

```text
caller -> method-security interceptor -> target method
```

A USER test should fail before repository work. An ADMIN test should reach business behavior. This is the same proxy principle used by `@Transactional`: self-invocation bypasses proxy interception, and direct construction does not activate annotations.

## 32. Authentication integration testing — ⭐⭐⭐⭐⭐ MUST KNOW

Real login should exercise:

```text
POST /auth/login (form fields + valid CSRF)
  -> UsernamePasswordAuthenticationFilter
  -> AuthenticationManager
  -> DaoAuthenticationProvider
  -> AppUserDetailsService
  -> AppUserRepository
  -> BCryptPasswordEncoder.matches
  -> SecurityContext
  -> HTTP session
```

Seed or register a disposable account with a test password. Do not use `@WithMockUser` in this test. Assert 204, an authenticated session, then `GET /auth/me` using that session. Also test a wrong password returning the custom 401 problem JSON.

Registration cannot create an ADMIN; seed an ADMIN directly only when a full-flow test needs an administrator.

## 33. Session and logout testing — ⭐⭐⭐⭐ IMPORTANT

```text
successful login
  -> SecurityContext saved in session
later request with same session
  -> authentication restored
logout with same session + valid CSRF
  -> authentication cleared and session invalidated
subsequent anonymous request
  -> 401
```

`JSESSIONID` identifies the server-side session; it is not the authentication itself. MockMvc uses `MockHttpSession` rather than a real network cookie jar. Assert the session was invalidated and a later request is unauthenticated; do not print session IDs.

## 34. Password-security testing — ⭐⭐⭐⭐⭐ MUST KNOW

Prove behavior without leaking secrets:

- unit test that registration invokes `encode(raw)`;
- capture the `AppUser` and assert the stored value is the encoder result and is not raw;
- integration test that the correct password logs in;
- integration test that a wrong password fails;
- optionally use `encoder.matches` against the saved hash without printing either value.

Never assert an exact BCrypt result. BCrypt uses a random salt, so two correct encodings normally differ. Never include passwords, hashes, CSRF tokens, session IDs, authorization headers, or cookie headers in diagnostics.

## 35. Test data and fixtures — ⭐⭐⭐⭐ IMPORTANT

- **Factory method**: a small local `book(...)` helper; best default.
- **Builder**: useful when an object has many optional fields and variants.
- **Fixture**: shared known data; keep it small and explicit.
- **Repository setup**: useful when testing real mappings/services.
- **SQL setup**: useful for exact database states, but can bypass entity invariants and become migration-coupled.

Prefer data near the test. Giant global fixtures make it unclear which row matters. Use descriptive unique ISBNs/usernames rather than random data unless randomness is the behavior under test.

## 36. Database cleanup and isolation — ⭐⭐⭐⭐⭐ MUST KNOW

| Strategy | Strength | Risk/cost |
|---|---|---|
| transactional rollback | fast and automatic for repository tests | can hide commit boundaries; cannot model all `REQUIRES_NEW` behavior |
| explicit cleanup | proves real commits and supports full flows | must respect FK/order and run reliably |
| fresh context/container | strongest isolation | slow and defeats context caching |
| unique test data | reduces collisions | old state can still affect counts/queries |

Use slice rollback for repository tests. Use explicit repository cleanup for transaction/authentication full-context tests, which intentionally need commits. Never depend on method order.

## 37. Maven test lifecycle — ⭐⭐⭐⭐⭐ MUST KNOW

```powershell
mvn test        # compile and run tests without deleting target first
mvn clean test  # clean build, then run tests
mvn verify      # run through the verification phase
```

Maven Surefire normally runs unit-style tests during `test`. Failsafe normally runs integration tests during `integration-test`/`verify`, but this beginner suite can remain under Surefire until separate lifecycles provide real value. Naming a class `*IntegrationTest` documents scope even if Surefire runs it.

The Boot 4.1.1 parent manages Surefire 3.5.6. Remove the project's obsolete 2.22.1 override so JUnit Platform 6 is launched with the managed toolchain.

## 38. IDE versus Maven — ⭐⭐⭐⭐ IMPORTANT

An IDE can use a different JDK, classpath, runner, working directory, or environment. “It passes in my IDE” is useful but incomplete. Run the reproducible project command:

```powershell
mvn clean test
```

The Docker runtime must be available for Testcontainers integration tests. A failure because Docker is unavailable is an infrastructure failure, not permission to fall back silently to H2.

## 39. Code coverage — ⭐⭐⭐ USEFUL

- **line coverage**: whether lines executed;
- **branch coverage**: whether alternatives executed.

```text
coverage = diagnostic signal, not the goal
```

Use uncovered risky behavior to ask better questions. Do not write getter tests or assertion-free calls to inflate a number. This exercise does not add a coverage plugin; the test strategy matters first.

## 40. Common anti-patterns — ⭐⭐⭐⭐⭐ MUST KNOW

- testing private methods directly;
- mocking everything, including records/entities;
- using `@SpringBootTest` for every behavior;
- asserting internal call order that is not a contract;
- shared mutable state and test ordering;
- using a production database;
- fixed `Thread.sleep` synchronization;
- exact BCrypt hash assertions;
- disabling all security/CSRF to make tests green;
- using H2 for PostgreSQL-specific behavior;
- never flushing JPA before a database assertion;
- assuming `@Transactional` on a test proves commit behavior;
- assuming `save()` always means SQL already ran;
- catching an exception without checking resulting state;
- asserting entire fragile JSON bodies when selected contract fields suffice.

## 41. Debugging failed tests — ⭐⭐⭐⭐ IMPORTANT

Use a layered order:

```text
read the assertion and root cause
  -> identify unit / MVC / JPA / full-flow layer
  -> inspect arrange/setup data
  -> inspect mock stubbing and unused/incorrect matchers
  -> inspect context/configuration and active profiles
  -> inspect Flyway/Hibernate SQL for DB tests
  -> inspect Security decision: authentication, role, or CSRF
  -> inspect transaction boundary, rollback-only, flush, and commit
```

Useful logging is targeted and temporary. Spring Security logs explain filter decisions; Hibernate SQL shows synchronization; transaction logs show begin/suspend/resume/commit/rollback; Flyway logs show migration validation. Never enable request logging that exposes credentials, tokens, session/cookie headers, or password hashes.

## 42. Testing architecture map — ⭐⭐⭐⭐⭐ MUST KNOW

```text
JUnit Platform / Jupiter
    |
    +-- pure Java + AssertJ
    |
    +-- Mockito --------------------------- mocked collaborator boundary
    |
    `-- Spring TestContext
           |
           +-- MVC slice -> MockMvc -> [SecurityFilterChain when enabled]
           |                  -> DispatcherServlet -> Controller
           |                  -> mocked Service
           |
           +-- JPA slice -> Repository -> Hibernate -> JDBC -> HikariCP
           |                                      -> PostgreSQL Testcontainer
           |                                             ^
           |                                             `-- Flyway V1–V6
           |
           `-- full context -> SecurityFilterChain -> DispatcherServlet
                                -> Controller -> Service proxy
                                   -> @PreAuthorize -> @Transactional
                                   -> Repository -> Hibernate -> JDBC
                                   -> HikariCP -> PostgreSQL Testcontainer
                                                    ^
                                                    `-- Flyway at startup
```

| Layer in path | Unit | MVC slice | PostgreSQL JPA slice | Full integration |
|---|---:|---:|---:|---:|
| JUnit | yes | yes | yes | yes |
| Mockito | often | service bean replacement | normally no | normally no |
| Spring context | no | partial | partial | full |
| DispatcherServlet/JSON/validation | no | yes | no | yes |
| SecurityFilterChain | no | optional/yes in policy test | no | yes |
| service AOP proxy | no | service mocked | no | yes |
| Hibernate/JDBC/Hikari | no | no | yes | yes |
| Flyway/PostgreSQL | no | no | yes | yes |

For every test, be able to answer:

```text
What behavior is proved? What is real? What is mocked?
Which Spring infrastructure runs? Is PostgreSQL real? Does Flyway run?
Is Hibernate involved? Is the filter chain involved?
Is authentication real or synthetic? Where is the transaction?
Could this pass while production behavior is still broken?
```

## 43. Interview retrieval practice — ⭐⭐⭐⭐⭐ MUST KNOW

Answer aloud before opening the key.

1. What distinguishes a unit test from an integration test?
2. Why should `@SpringBootTest` not be used everywhere?
3. What does Boot 4.1 `@WebMvcTest` load, and what does it normally exclude?
4. Why does this project need Boot 4 modular MVC/JPA/Security test starters?
5. What is the difference between JUnit Platform, Jupiter, and Vintage?
6. Why should JUnit 4.11 and the generated `AppTest` be removed?
7. What is the difference between `@Mock` and `@MockitoBean`?
8. What does `@WithMockUser` prove?
9. What does `@WithMockUser` not prove?
10. Why can a permitted login or registration endpoint still return 403?
11. What is the practical difference between 401 and 403?
12. Why can a JPA test pass without proving a database round trip?
13. What do `flush()` and `clear()` prove?
14. Why might `save()` issue SQL early for this project's entities?
15. Why is H2 not equivalent to PostgreSQL 17?
16. What problem does Testcontainers solve?
17. What does `@ServiceConnection` do?
18. Why should Flyway run in integration tests?
19. Why is `ddl-auto=create` wrong for this project's integration suite?
20. Why can `@Transactional` on a test hide production bugs?
21. Why is flush not a commit?
22. How do you prove `REQUIRES_NEW` behavior?
23. Why should BCrypt hashes not be compared exactly?
24. Why must tests be independent of execution order?
25. What does code coverage fail to tell you?
26. Which test proves `@PreAuthorize` is active?
27. Which test proves real database-backed login and session persistence?

```text
STOP — SELF-TEST BEFORE READING THE ANSWERS
```

### Concise answer key

1. A unit test isolates one object; an integration test checks cooperating real components at a declared boundary.
2. It loads unnecessary beans/infrastructure, slows feedback, and broadens failure causes.
3. MVC/controller infrastructure and selected web components; normal services/repositories are excluded and commonly replaced with `@MockitoBean`.
4. Boot 4 moved focused test auto-configuration into modular artifacts/packages; the base starter alone is not the whole older “kitchen sink.”
5. Platform discovers/runs; Jupiter supplies the modern API/engine; Vintage runs legacy JUnit 3/4.
6. It is obsolete here and its `assertTrue(true)` proves nothing; Jupiter is Boot-managed.
7. `@Mock` is a plain Mockito field; `@MockitoBean` inserts/replaces a bean in a Spring test context.
8. Authorization behavior for a synthetic identity and its roles/authorities.
9. Repository lookup, BCrypt, the authentication provider, login filter, and session persistence.
10. `permitAll` does not disable CSRF; unsafe requests still need a token.
11. 401 means authentication is required/failed; 403 means access was denied after or by another control such as CSRF.
12. The first-level cache can return the same managed object without SQL.
13. Flush synchronizes SQL/constraints; clear removes managed cached state so later reads must reload.
14. `GenerationType.IDENTITY` may need an INSERT to obtain the ID.
15. It differs in dialect, types, constraints, identity, locking, transactions, and migration SQL.
16. It supplies an isolated disposable instance of the real PostgreSQL engine.
17. It converts container metadata into Boot connection details used to configure the `DataSource`.
18. To prove V1–V6 can construct the schema that Hibernate/current code expects.
19. It bypasses the production schema owner and can conceal broken migrations.
20. Its outer rollback can hide commit-time, detached, rollback-only, and `REQUIRES_NEW` behavior.
21. SQL can be sent while the transaction remains open and can still roll back.
22. Call the real proxied services without an outer test transaction, then reread committed database state.
23. BCrypt salts make valid outputs intentionally different.
24. Order-dependent tests are nondeterministic and fail under isolated/parallel/different-order execution.
25. Whether assertions are meaningful, requirements are complete, or untested inputs/concurrency are correct.
26. A Spring-context test that invokes the injected `BookService` proxy as USER and ADMIN.
27. A full-context MockMvc flow using real form login, repository-backed user, BCrypt, and the same session on a later request.

The professional mental model is simple: select the narrowest boundary that can observe the risk, then be explicit about everything outside that boundary.
