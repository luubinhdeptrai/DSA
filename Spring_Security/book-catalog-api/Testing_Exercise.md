# Testing Exercise

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
| Spring Framework 7.0.9 | Use `@MockitoBean`, not deprecated `@MockBean`. |
| Spring Security 7.1.1 | Add Boot's modular Security test starter. |
| Direct JUnit 4.11 and generated `assertTrue(true)` test | Remove both and migrate to JUnit Jupiter 6.0.3. |
| POM pins Surefire 2.22.1 | Remove stale plugin pins; use Boot-managed Surefire 3.5.6. |
| PostgreSQL 17 in Compose | Match it with `postgres:17` in Testcontainers. |
| Flyway 12.4.0, migrations V1–V6 | Start test PostgreSQL empty and apply the real history. |
| Hibernate `ddl-auto: validate` | Keep Hibernate as validator, never schema creator. |
| HikariCP is the application `DataSource` | Full DB tests still use the real pool and pgJDBC. |
| `GET /books` is public; book writes and transaction lab require ADMIN | Security tests must preserve the actual matrix. |
| login/logout are Security filter endpoints | Real authentication uses form parameters, session, and CSRF—not JSON controller calls. |
| only `BookService.setStock` has `@PreAuthorize` | Method-security tests target that method through its proxy. |
| `BookService.delete` intentionally flushes, throws, and rolls back | Test 500/exception plus row survival; do not expect normal deletion. |
| `StockRequest` has no constraints | Do not invent a negative-stock MVC 400 test. |
| validation advice has placeholder title/detail | Assert the checked-in response, even if its wording should later improve. |
| `.env.example` is empty | Tests must not read `.env` or local credentials. |

The inspected local JVM zone is the legacy ID `Asia/Saigon`, which PostgreSQL 17 rejects as a startup `TimeZone` parameter. The reference suite sets the **test JVM** to UTC through versionless Surefire configuration. This creates deterministic tests and leaves production behavior unchanged.

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

The exercise changes only the test dependencies, test runner configuration, and `src/test` content.

## Learning contract

For every task:

1. State the behavior being proved.
2. Mark what is real and what is mocked.
3. Predict the result before running.
4. Complete the starter containing deliberate `TODO`s.
5. Run the narrowest relevant Maven command.
6. Explain what the test still cannot prove.

Every core task contains exactly this rhythm:

```text
Objective
Concept
What to implement
Starter code
Prediction
Hints
How to verify
Common mistakes
Explanation
```

Do not read the cumulative solution until you have attempted the tasks.

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

## Exercise tasks — attempt before reading the solution

### Task 1 — Inventory behavior and choose test boundaries

**Objective**

Design before writing tests.

**Concept**

The smallest sufficient scope produces faster, clearer failures.

**What to implement**

Inspect the POM, current test, controllers, DTOs, services, repositories, security classes, migrations, and transaction lab. Build a matrix for at least registration, controller validation, authorization, repository constraints, transaction rollback, and real login.

**Starter code**

```text
| Behavior | Test level | Spring? | PostgreSQL? | Mocked? |
| TODO     | TODO       | TODO    | TODO        | TODO    |
```

**Prediction**

Which behaviors require a Spring proxy? Which require PostgreSQL? Which can use plain Java?

**Hints**

- An annotation on an object created with `new` has no interceptor.
- A database constraint cannot be proved by a repository mock.
- JSON/validation/advice can be proved without starting PostgreSQL.

**How to verify**

For each row, answer the ten boundary questions from the concepts guide.

**Common mistakes**

- Selecting `@SpringBootTest` for every row.
- Calling every Spring test an integration test without naming its boundary.
- Treating mock identity as real authentication.

**Explanation**

The intended progression is pure Java → Mockito → MVC slice → security slice → JPA/PostgreSQL → full transaction/authentication integration.

### Task 2 — Modernize the Boot 4 test toolchain

**Objective**

Replace the obsolete generated JUnit 4 setup with Boot-managed modern test support.

**Concept**

Boot 4.1 modularizes focused test support and manages compatible versions.

**What to implement**

Remove `junit:junit:4.11` and `src/test/java/com/example/AppTest.java`. Add the base, MVC, JPA, Security, Boot Testcontainers, and PostgreSQL Testcontainers dependencies with `test` scope and no versions. Remove stale plugin pins. Declare versionless Surefire only to set `user.timezone=UTC` for this environment.

**Starter code**

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

**Prediction**

Which versions will Maven select for Jupiter, Mockito, Testcontainers, and Surefire?

**Hints**

- Boot 4 uses `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, and `spring-boot-starter-security-test`.
- Testcontainers 2 uses `testcontainers-postgresql`.
- Do not add JUnit Vintage merely to retain `assertTrue(true)`.

**How to verify**

```powershell
mvn dependency:tree
mvn test -DskipTests
```

Confirm JUnit Jupiter 6.0.3, Mockito 5.23.0, Testcontainers 2.0.5, and no direct JUnit 4 dependency.

**Common mistakes**

- Copying Boot 3 package/dependency examples.
- Hard-coding versions already managed by Boot.
- Leaving Surefire 2.22.1 pinned.

**Explanation**

The Boot parent is a compatibility contract. The project chooses capabilities; Boot chooses the tested version set.

### Task 3 — Write the first pure JUnit test

**Objective**

Prove a real behavior without Spring or mocks.

**Concept**

`BookRequest`'s compact constructor normalizes three text values.

**What to implement**

Create `dto/BookRequestTest` using Jupiter and AssertJ. Verify trimming while leaving price/stock as supplied.

**Starter code**

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

**Prediction**

Will Spring, validation, or PostgreSQL start?

**Hints**

This test calls Java code directly. Record component constraints are not automatically evaluated.

**How to verify**

```powershell
mvn -Dtest=BookRequestTest test
```

**Common mistakes**

- Adding `@SpringBootTest`.
- Claiming this proves `@NotBlank` HTTP handling.
- Testing trivial generated record accessors instead of normalization.

**Explanation**

The production record is real; everything outside it is absent. A fast test precisely proves constructor normalization.

### Task 4 — Unit-test registration with Mockito

**Objective**

Prove security-sensitive service decisions in isolation.

**Concept**

Mock collaborator boundaries, capture important output, and verify only meaningful interactions.

**What to implement**

Create `RegistrationServiceTest` with `@Mock`, `@InjectMocks`, stubbing, verification, and `ArgumentCaptor`. Cover success, duplicate precheck, and race-time database failure.

**Starter code**

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

**Prediction**

Could this test still pass if BCrypt configuration or the database unique constraint were broken?

**Hints**

- Do not assert an exact BCrypt hash; the encoder is mocked here.
- Duplicate precheck should produce no encoder or save interaction.
- `saveAndFlush` failure is translated to a 409 `ResponseStatusException`.

**How to verify**

```powershell
mvn -Dtest=RegistrationServiceTest test
```

**Common mistakes**

- Accepting or assigning `ADMIN` from request input.
- Mocking `RegisterRequest` or `AppUser`.
- Verifying every internal call rather than the boundary contract.

**Explanation**

This unit test proves orchestration and server-side role assignment. Database and real encoder tests appear later.

### Task 5 — Unit-test `AppUserDetailsService`

**Objective**

Verify conversion from the stored application user to Spring Security's `UserDetails`.

**Concept**

One collaborator (`AppUserRepository`) can be stubbed while authority mapping remains real Java behavior.

**What to implement**

Cover a known ADMIN or USER and an unknown username. Assert username, stored hash propagation, `ROLE_` prefix, and generic missing-user message.

**Starter code**

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

**Prediction**

Does this prove `DaoAuthenticationProvider` calls the service during login?

**Hints**

`User.withUsername(...).roles("ADMIN")` creates authority `ROLE_ADMIN`.

**How to verify**

```powershell
mvn -Dtest=AppUserDetailsServiceTest test
```

**Common mistakes**

- Expecting authority `ADMIN` rather than `ROLE_ADMIN`.
- Returning different messages that reveal whether an account exists.
- Labeling this a full authentication test.

**Explanation**

It is a pure unit test. The repository is mocked; no AuthenticationManager, BCrypt, filter, or session runs.

### Task 6 — Build an MVC controller slice

**Objective**

Test Book HTTP mapping, query parameters, JSON, status, content type, and `Location` without a database.

**Concept**

`@WebMvcTest` loads the web slice; `@MockitoBean` supplies its service boundary.

**What to implement**

Create `BookControllerWebMvcTest` with Boot 4 imports. Intentionally disable filters for this controller-contract class and explain why a separate class tests security. Cover list and create success.

**Starter code**

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

**Prediction**

Which components are real, and which production failure could this test miss?

**Hints**

- Boot 4 import: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`.
- The entity has no public ID setter; `ReflectionTestUtils` is acceptable for test fixture identity.
- MockMvc does not start a network server.

**How to verify**

```powershell
mvn -Dtest=BookControllerWebMvcTest test
```

**Common mistakes**

- Using deprecated `@MockBean`.
- Returning an unsaved `Book`; `BookResponse` unboxes its null ID.
- Claiming mocked service output proves persistence.

**Explanation**

Real MVC mapping, conversion, and controller code run. `BookService`, AOP, JPA, Flyway, and PostgreSQL do not.

### Task 7 — Test validation and global error mapping

**Objective**

Prove the checked-in 400, 404, and 409 HTTP contracts.

**Concept**

An exception unit test and an HTTP exception-mapping test answer different questions.

**What to implement**

Extend the MVC slice with invalid `BookRequest`, missing-book, and duplicate-ISBN scenarios. Assert selected actual `ProblemDetail` fields and no service call for invalid input.

**Starter code**

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

**Prediction**

What exact title/detail does the current advice return, and does it include `instance`?

**Hints**

- Do not improve the response inside the test.
- Use `contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)`.
- Do not assert JSON property order.

**How to verify**

Run the slice and inspect only failures, not production secrets.

**Common mistakes**

- Asserting imaginary `timestamp` or `errors` fields.
- Writing a false negative-stock validation test for unconstrained `StockRequest`.
- Calling the service and then accepting a 400 produced somewhere else.

**Explanation**

The test makes current behavior executable documentation and exposes design gaps without silently changing them.

### Task 8 — Test the actual URL security policy

**Objective**

Verify authentication, role, and CSRF decisions independently of persistence.

**Concept**

A security MVC slice keeps filters and imports the real `SecurityConfig`, handlers, and advice while service beans remain mocked.

**What to implement**

Create `SecurityPolicyWebMvcTest` for anonymous public GET, anonymous `/auth/me`, anonymous write with/without CSRF, USER write with CSRF, and ADMIN write with/without CSRF.

**Starter code**

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

**Prediction**

Why does anonymous POST without CSRF return 403, while the same request with CSRF returns 401?

**Hints**

- CSRF filtering can reject before authorization.
- `@WithMockUser(roles="ADMIN")` creates `ROLE_ADMIN`.
- A valid admin response needs a stubbed `Book` with an ID.

**How to verify**

```powershell
mvn -Dtest=SecurityPolicyWebMvcTest test
```

**Common mistakes**

- Disabling filters in the security test.
- Assuming an ADMIN bypasses CSRF.
- Claiming `@WithMockUser` proves database login.

**Explanation**

This proves filter-chain policy using synthetic identities. Real authentication comes later.

### Task 9 — Test method security through a Spring proxy

**Objective**

Prove the existing `@PreAuthorize` guard on `BookService.setStock`.

**Concept**

Method security is AOP. The call must pass through the injected proxy.

**What to implement**

Create a Spring integration test using the real `BookService` and PostgreSQL. Seed a book. Assert USER is denied with unchanged stock; ADMIN reaches the transactional method and commits the new stock.

**Starter code**

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

**Prediction**

What would happen if the test used `new BookService(repository)`?

**Hints**

- Do not annotate this class/test with `@Transactional` if the ADMIN case must prove commit.
- URL authorization is not involved in a direct service call.

**How to verify**

Run with Docker available and compare stock after each invocation.

**Common mistakes**

- Calling a directly constructed target.
- Testing only the HTTP rule and assuming the method rule ran.
- Using USER authority `USER` instead of role `ROLE_USER` semantics.

**Explanation**

The Spring proxy applies method authorization, then the target's transaction interceptor/business code when permitted.

### Task 10 — Add a PostgreSQL repository slice

**Objective**

Test real entity mappings and derived queries against PostgreSQL 17.

**Concept**

Testcontainers supplies disposable infrastructure; Boot service connections publish dynamic JDBC details.

**What to implement**

Create `TestPostgresConfiguration` with a Boot-managed `PostgreSQLContainer` bean. Create `RepositoryPostgresIntegrationTest` using Boot 4 `@DataJpaTest`, no database replacement, test profile, and explicit Flyway auto-configuration. Test a round trip and `findByUsername`.

**Starter code**

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

**Prediction**

Does this slice start MVC or the Security filter chain? Which persistence components are real?

**Hints**

- Testcontainers 2 import: `org.testcontainers.postgresql.PostgreSQLContainer`.
- Use `Replace.NONE`.
- Flush and clear before the database reread.

**How to verify**

Observe a dynamic JDBC port and PostgreSQL 17 in startup logs. Never print credentials.

**Common mistakes**

- Adding H2.
- Hard-coding `localhost:5432` test settings.
- Using the deprecated Testcontainers 1.x PostgreSQL class/package when a current class exists.

**Explanation**

The slice excludes MVC/services but runs DataSource, HikariCP, Hibernate, repositories, pgJDBC, Flyway (when imported), and real PostgreSQL.

### Task 11 — Expose persistence-context and constraint behavior

**Objective**

See why `save`, flush, clear, and exception-translation boundaries matter.

**Concept**

Managed state can look correct before SQL executes, and the first-level cache can hide database reads.

**What to implement**

Test `saveAndFlush` + `clear` + reload. Then persist two valid books, mutate the second to duplicate the first ISBN, call `save(second)`, and assert failure when calling `books.flush()`.

**Starter code**

```java
Book first = books.saveAndFlush(book("unique-isbn-1"));
Book second = books.saveAndFlush(book("unique-isbn-2"));
second.replace(first.getIsbn(), second.getTitle(), second.getAuthor(),
        second.getPrice(), second.getStock());
books.save(second);

// TODO: force synchronization and assert Spring's translated exception
```

**Prediction**

Why is this update experiment more deterministic than promising a new IDENTITY insert waits for flush?

**Hints**

- IDENTITY may execute INSERT during `save` to obtain an ID.
- `BookRepository.flush()` crosses Spring repository exception translation; raw `EntityManager.flush()` can expose a Hibernate exception instead.

**How to verify**

Temporarily inspect SQL: the duplicate UPDATE appears at flush and the test transaction rolls back.

**Common mistakes**

- Reading the same managed object and claiming a round trip.
- Continuing database work after a constraint violation in the same transaction.
- Saying flush committed data.

**Explanation**

Flush synchronizes the persistence context; clear forces future reads beyond its L1 cache; the test transaction still rolls back.

### Task 12 — Prove Flyway owns the test schema

**Objective**

Verify the empty test database is migrated before Hibernate uses it.

**Concept**

The integration test must reproduce production schema ownership.

**What to implement**

Import `FlywayAutoConfiguration`, keep `ddl-auto=validate`, and query `flyway_schema_history` for six successful migrations.

**Starter code**

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

**Prediction**

What failure appears if a migration and entity mapping disagree?

**Hints**

- Boot 4 class: `org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration`.
- V1 already contains `version`; V2 logs that its idempotent add is skipped. Both history rows are still successful.

**How to verify**

Look for the order: container → Flyway V1–V6 → Hibernate validation → test.

**Common mistakes**

- Switching to `ddl-auto=create`.
- Assuming `@DataJpaTest` automatically imports every modular integration.
- Merely asserting that a table exists while ignoring migration history.

**Explanation**

This test proves the checked-in migration chain builds a usable current schema from nothing.

### Task 13 — Test real transaction boundaries

**Objective**

Verify existing rollback, checked-exception, and `REQUIRES_NEW` behavior.

**Concept**

State after an exception is the transaction contract; a test-owned transaction can conceal it.

**What to implement**

Create `TransactionBehaviorIntegrationTest` with no test `@Transactional`. Explicitly clean/seed. Cover runtime rollback, checked default commit, independently committed audit, and intentional delete rollback.

**Starter code**

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

**Prediction**

For each scenario, write expected stock and audit rows before running.

**Hints**

- Reread through repositories after the service returns/throws.
- `flushThenRuntimeRollback` would still roll back emitted SQL.
- `BookService.delete` is intentionally not a normal successful delete.

**How to verify**

Run the class alone and map transaction logs to begin/suspend/resume/commit/rollback.

**Common mistakes**

- Annotating the test with `@Transactional`.
- Asserting only exception type.
- “Fixing” the checked-default-commit demonstration.

**Explanation**

The real service proxies decide rollback rules. Repository rereads observe durable outcomes after those boundaries complete.

### Task 14 — Test real database-backed authentication

**Objective**

Go beyond mock identities and exercise the complete login path.

**Concept**

Form login is handled by Spring Security filters and persists a `SecurityContext` in the session.

**What to implement**

Create a full-context MockMvc test with PostgreSQL. Register a disposable USER, obtain a CSRF token/session, submit form parameters to `/auth/login`, retain the session, then call `/auth/me`. Add a wrong-password test.

**Starter code**

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

**Prediction**

Which real components run that `@WithMockUser` bypasses?

**Hints**

- Login consumes form parameters, not JSON.
- Registration is `permitAll` but still needs CSRF.
- Assert encoder matching without logging the raw value/hash.

**How to verify**

Assert saved role `USER`, stored value differs from raw, login is 204, and `/auth/me` returns `ROLE_USER` with the same session.

**Common mistakes**

- Using `@WithMockUser` in the real-login test.
- Posting JSON credentials to `/auth/login`.
- Printing security artifacts to diagnose failure.

**Explanation**

This path runs the login filter, authentication manager/provider, application `UserDetailsService`, repository, BCrypt, SecurityContext, and session.

### Task 15 — Complete CSRF, ADMIN write, session, and logout flow

**Objective**

Prove an authenticated session survives requests and logout invalidates it.

**Concept**

CSRF token state and authentication state both participate; roles remain separate.

**What to implement**

Create a helper that calls `GET /auth/csrf` and returns session, parameter name, header name, and token. Seed an ADMIN directly, log in, obtain a fresh token, create a book, and prove persistence. In the USER flow, refresh the token, log out, assert invalidation, then verify anonymous `/auth/me` is 401.

**Starter code**

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

**Prediction**

Why can registration not create the ADMIN used for the successful book write?

**Hints**

- `RegistrationService` always assigns `USER`.
- Fetch a token after login before an unsafe write/logout.
- MockMvc uses `MockHttpSession`; assert it becomes invalid.

**How to verify**

Assert ADMIN create returns 201 and the repository has the book. Assert logout returns 204, `unauthenticated()`, and an invalidated session.

**Common mistakes**

- Sharing USER and ADMIN identity assumptions.
- Replacing real login with request `user(...)`.
- Logging tokens/cookies/session IDs.

**Explanation**

This is the broadest core test: Security filters, MVC, service proxies, JPA, Flyway-built PostgreSQL, BCrypt, session, and CSRF are real.

### Task 16 — Run the Maven suite and explain every boundary

**Objective**

Make the suite reproducible outside the IDE and audit its value.

**Concept**

A green suite is useful only if you understand what each test proves and omits.

**What to implement**

Run clean Maven verification with Docker available. Build a final matrix containing test level, context, database, mocks, security identity, transaction, proven behavior, and limitation.

**Starter code**

```powershell
# TODO: cleanly compile and run every test
mvn TODO

# TODO: run through Maven's verification phase
mvn TODO
```

**Prediction**

Which classes should dominate runtime, and why?

**Hints**

- Surefire discovers the `*Test` names used here.
- `verify` does not imply Failsafe separation unless configured.
- Context caching can reuse identical configurations; different slices need different contexts.

**How to verify**

Expect all tests green and no connection to the configured developer database. Inspect the dynamic Testcontainers JDBC port, migration count, and Maven summary.

**Common mistakes**

- Running only from the IDE.
- Treating a missing Docker daemon as a reason to use H2.
- Adding test order to hide leaked state.

**Explanation**

Maven is the reproducible entry point. The final matrix prevents false claims about what a green test means.

## Optional advanced extensions

After the core suite is stable, consider parameterized DTO tests, `@Nested` scenario grouping, an optimistic-lock test with two persistence contexts, a custom fixture builder, a `RANDOM_PORT` full HTTP test, or deliberate Surefire/Failsafe separation. These are not required to complete this lesson.

============================================================
STOP — ATTEMPT THE EXERCISE BEFORE READING THE SOLUTION
============================================================

Do not continue until you have written predictions for all 16 tasks and attempted each core layer.

## Complete cumulative reference solution

The following solution is cumulative. Paths are relative to `Spring_Security/book-catalog-api`.

### Design choices

- Production code and migrations are unchanged.
- JUnit 4 and the generated no-op test are removed.
- Boot manages every library/plugin version.
- Boot 4 modular test starters provide current annotation packages.
- A Boot-managed PostgreSQL 17 container supplies both JDBC and Flyway connection details.
- JPA slice tests roll back; full transaction/authentication tests explicitly clean data and do not have an outer test transaction.
- MVC contract tests intentionally omit filters; a separate MVC security class imports and tests the real filter chain.
- Full authentication tests never use mock identities.
- Test JVM timezone is UTC because the inspected legacy local zone ID is rejected by PostgreSQL.

### Change manifest

Modify:

```text
pom.xml
```

Remove:

```text
src/test/java/com/example/AppTest.java
```

Add only test source/configuration under `src/test`; do not edit `src/main`.

### Final `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
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
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-flyway</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

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
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <systemPropertyVariables>
            <user.timezone>UTC</user.timezone>
          </systemPropertyVariables>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

There are no hard-coded test-library/plugin versions. The Boot parent manages them. The Surefire declaration changes configuration only.

### Final project structure

```text
book-catalog-api/
├── pom.xml
├── compose.yaml
└── src/
    ├── main/                                  # unchanged
    └── test/
        ├── java/com/example/bookcatalog/
        │   ├── dto/
        │   │   └── BookRequestTest.java
        │   ├── service/
        │   │   └── RegistrationServiceTest.java
        │   ├── web/
        │   │   └── BookControllerWebMvcTest.java
        │   ├── security/
        │   │   ├── AppUserDetailsServiceTest.java
        │   │   ├── SecurityPolicyWebMvcTest.java
        │   │   └── BookServiceMethodSecurityIntegrationTest.java
        │   ├── repository/
        │   │   └── RepositoryPostgresIntegrationTest.java
        │   ├── integration/
        │   │   ├── TransactionBehaviorIntegrationTest.java
        │   │   └── AuthenticationFlowIntegrationTest.java
        │   └── support/
        │       └── TestPostgresConfiguration.java
        └── resources/
            └── application-test.yaml
```

Naming uses `*Test`, so Surefire executes every class during `mvn test`. `IntegrationTest` in a name documents scope; this reference does not add Failsafe complexity.

## Complete test files

### `src/test/resources/application-test.yaml`

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
  flyway:
    enabled: true
    clean-disabled: true

logging:
  level:
    org.hibernate.SQL: WARN
    org.springframework.transaction: WARN
    org.springframework.orm.jpa.JpaTransactionManager: WARN
    org.springframework.security: WARN
```

This profile contains no URL or credentials. `@ServiceConnection` supplies them dynamically. It also cannot activate `LocalAdminBootstrap`, which is restricted to profile `local`.

### `src/test/java/com/example/bookcatalog/support/TestPostgresConfiguration.java`

```java
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
        return new PostgreSQLContainer("postgres:17")
                .withDatabaseName("book_catalog_test");
    }
}
```

Boot starts/stops the container bean with the test context. Its JDBC and Flyway connection-details factories configure the real application infrastructure.


### `src/test/java/com/example/bookcatalog/dto/BookRequestTest.java`

```java
package com.example.bookcatalog.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BookRequestTest {

    @Test
    void constructor_textValues_trimsThem() {
        BookRequest request = new BookRequest(
                "  isbn-1  ", "  Clean Code  ", "  Robert Martin  ",
                new BigDecimal("19.99"), 3);

        assertThat(request.isbn()).isEqualTo("isbn-1");
        assertThat(request.title()).isEqualTo("Clean Code");
        assertThat(request.author()).isEqualTo("Robert Martin");
    }
}
```


### `src/test/java/com/example/bookcatalog/service/RegistrationServiceTest.java`

```java
package com.example.bookcatalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.bookcatalog.dto.RegisterRequest;
import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final String RAW_PASSWORD = "test-password-123";

    @Mock
    AppUserRepository users;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    RegistrationService service;

    @Test
    void register_newUsername_encodesPasswordAndAssignsUserRole() {
        RegisterRequest request = new RegisterRequest("alice", RAW_PASSWORD);
        when(users.existsByUsername("alice")).thenReturn(false);
        when(encoder.encode(RAW_PASSWORD)).thenReturn("encoded-test-value");

        service.register(request);

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).saveAndFlush(saved.capture());
        verify(encoder).encode(RAW_PASSWORD);
        assertThat(saved.getValue().getUsername()).isEqualTo("alice");
        assertThat(saved.getValue().getPasswordHash())
                .isEqualTo("encoded-test-value")
                .isNotEqualTo(RAW_PASSWORD);
        assertThat(saved.getValue().getRole()).isEqualTo("USER");
    }

    @Test
    void register_existingUsername_returnsConflictWithoutEncodingOrSaving() {
        RegisterRequest request = new RegisterRequest("alice", RAW_PASSWORD);
        when(users.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT));

        verifyNoInteractions(encoder);
        verify(users, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void register_concurrentDuplicate_translatesDatabaseFailureToConflict() {
        RegisterRequest request = new RegisterRequest("alice", RAW_PASSWORD);
        when(users.existsByUsername("alice")).thenReturn(false);
        when(encoder.encode(RAW_PASSWORD)).thenReturn("encoded-test-value");
        when(users.saveAndFlush(any(AppUser.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT));
    }
}
```


### `src/test/java/com/example/bookcatalog/security/AppUserDetailsServiceTest.java`

```java
package com.example.bookcatalog.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    AppUserRepository users;

    @InjectMocks
    AppUserDetailsService service;

    @Test
    void loadUserByUsername_knownAdmin_mapsStoredDataAndAuthority() {
        when(users.findByUsername("admin"))
                .thenReturn(Optional.of(
                        new AppUser("admin", "stored-hash", "ADMIN")));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("stored-hash");
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_unknownUser_throwsGenericCredentialError() {
        when(users.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid credentials");
    }
}
```


### `src/test/java/com/example/bookcatalog/web/BookControllerWebMvcTest.java`

```java
package com.example.bookcatalog.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.service.BookService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @Test
    void findAll_withQueryParameters_returnsMappedJson() throws Exception {
        when(bookService.findAll("Ursula Le Guin", 0, 2))
                .thenReturn(List.of(book(7L, "isbn-7")));

        mockMvc.perform(get("/books")
                        .param("author", "  Ursula Le Guin  ")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].isbn").value("isbn-7"));
    }

    @Test
    void create_validBody_returnsCreatedLocationAndBody() throws Exception {
        when(bookService.create("isbn-8", "Title", "Author",
                new BigDecimal("12.50"), 4))
                .thenReturn(book(8L, "isbn-8"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson("isbn-8")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/books/8"))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.isbn").value("isbn-8"));
    }

    @Test
    void create_invalidBody_returnsActualValidationProblemAndSkipsService()
            throws Exception {
        String invalid = """
                {
                  "isbn": "   ",
                  "title": "Title",
                  "author": "Author",
                  "price": 12.50,
                  "stock": 4
                }
                """;

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                        .value("some fields failed (title)"))
                .andExpect(jsonPath("$.detail")
                        .value("some field failed (detail)"))
                .andExpect(jsonPath("$.fieldErrors.isbn[0]")
                        .value("isbn must not be blank"));

        verifyNoInteractions(bookService);
    }

    @Test
    void findById_missingBook_returnsActualNotFoundProblem() throws Exception {
        when(bookService.findById(99L))
                .thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/books/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail")
                        .value("Book with id: 99 is not found"));
    }

    @Test
    void create_duplicateIsbn_returnsActualConflictProblem() throws Exception {
        when(bookService.create(anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyInt()))
                .thenThrow(new DuplicateIsbnException(
                        new IllegalStateException("test cause")));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson("duplicate")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("ISBN conflict"))
                .andExpect(jsonPath("$.detail").value("Isbn is duplicated"));
    }

    private static Book book(long id, String isbn) {
        Book book = new Book(isbn, "Title", "Author",
                new BigDecimal("12.50"), 4);
        ReflectionTestUtils.setField(book, "id", id);
        return book;
    }

    private static String validBookJson(String isbn) {
        return """
                {
                  "isbn": "%s",
                  "title": "Title",
                  "author": "Author",
                  "price": 12.50,
                  "stock": 4
                }
                """.formatted(isbn);
    }
}
```


### `src/test/java/com/example/bookcatalog/security/SecurityPolicyWebMvcTest.java`

```java
package com.example.bookcatalog.security;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.service.BookService;
import com.example.bookcatalog.service.RegistrationService;
import com.example.bookcatalog.web.AuthController;
import com.example.bookcatalog.web.BookController;
import com.example.bookcatalog.web.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {BookController.class, AuthController.class})
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, GlobalExceptionHandler.class})
class SecurityPolicyWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @MockitoBean
    RegistrationService registrationService;

    @MockitoBean
    UserDetailsService userDetailsService;

    @Test
    void booksGet_anonymous_isPermitted() throws Exception {
        when(bookService.findAll(null, 0, 20)).thenReturn(List.of());

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @Test
    void me_anonymous_returnsUnauthorizedProblem() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void create_anonymousWithoutCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    void create_anonymousWithCsrf_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_userWithCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminWithoutCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminWithCsrf_reachesController() throws Exception {
        when(bookService.create("security-isbn", "Title", "Author",
                new BigDecimal("12.50"), 4))
                .thenReturn(book(31L));

        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(31));
    }

    private static Book book(long id) {
        Book book = new Book("security-isbn", "Title", "Author",
                new BigDecimal("12.50"), 4);
        ReflectionTestUtils.setField(book, "id", id);
        return book;
    }

    private static String validBookJson() {
        return """
                {
                  "isbn": "security-isbn",
                  "title": "Title",
                  "author": "Author",
                  "price": 12.50,
                  "stock": 4
                }
                """;
    }
}
```


### `src/test/java/com/example/bookcatalog/repository/RepositoryPostgresIntegrationTest.java`

```java
package com.example.bookcatalog.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestPostgresConfiguration.class)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@ActiveProfiles("test")
class RepositoryPostgresIntegrationTest {

    @Autowired
    BookRepository books;

    @Autowired
    AppUserRepository users;

    @Autowired
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void startup_flywayAppliedAllSixMigrations() {
        Integer count = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success",
                Integer.class);

        assertThat(count).isEqualTo(6);
    }

    @Test
    void save_flushClearAndFind_roundTripsThroughPostgres() {
        Book saved = books.saveAndFlush(book("round-trip-isbn", "Author A"));
        Long id = saved.getId();
        entityManager.clear();

        Book reloaded = books.findById(id).orElseThrow();

        assertThat(reloaded.getIsbn()).isEqualTo("round-trip-isbn");
        assertThat(reloaded.getStock()).isEqualTo(10);
    }

    @Test
    void findByUsername_existingUser_usesRealMappingAndDerivedQuery() {
        users.saveAndFlush(new AppUser(
                "repository-user", "stored-hash", "USER"));
        entityManager.clear();

        AppUser found = users.findByUsername("repository-user")
                .orElseThrow();

        assertThat(found.getPasswordHash()).isEqualTo("stored-hash");
        assertThat(found.getRole()).isEqualTo("USER");
    }

    @Test
    void flush_dirtyEntityWithDuplicateIsbn_exposesDatabaseConstraint() {
        Book first = books.saveAndFlush(book("unique-isbn-1", "Author A"));
        Book second = books.saveAndFlush(book("unique-isbn-2", "Author B"));
        second.replace(first.getIsbn(), second.getTitle(), second.getAuthor(),
                second.getPrice(), second.getStock());
        books.save(second);

        assertThatThrownBy(books::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void flush_invalidRole_exposesPostgresCheckConstraint() {
        assertThatThrownBy(() -> users.saveAndFlush(
                new AppUser("bad-role-user", "stored-hash", "SUPERUSER")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Book book(String isbn, String author) {
        return new Book(isbn, "Repository Test", author,
                new BigDecimal("20.00"), 10);
    }
}
```


### `src/test/java/com/example/bookcatalog/security/BookServiceMethodSecurityIntegrationTest.java`

```java
package com.example.bookcatalog.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.service.BookService;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestPostgresConfiguration.class)
@ActiveProfiles("test")
class BookServiceMethodSecurityIntegrationTest {

    @Autowired
    BookService service;

    @Autowired
    BookRepository books;

    private Long bookId;

    @BeforeEach
    void setUp() {
        books.deleteAllInBatch();
        bookId = books.saveAndFlush(new Book(
                "method-security-isbn", "Title", "Author",
                new BigDecimal("15.00"), 5)).getId();
    }

    @Test
    @WithMockUser(roles = "USER")
    void setStock_user_isDeniedByMethodProxy() {
        assertThatThrownBy(() -> service.setStock(bookId, 9))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(books.findById(bookId).orElseThrow().getStock())
                .isEqualTo(5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setStock_admin_reachesTransactionalBusinessMethod() {
        service.setStock(bookId, 9);

        assertThat(books.findById(bookId).orElseThrow().getStock())
                .isEqualTo(9);
    }
}
```


### `src/test/java/com/example/bookcatalog/integration/TransactionBehaviorIntegrationTest.java`

```java
package com.example.bookcatalog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookcatalog.exception.TransactionLabCheckedException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.repository.TransactionAuditRepository;
import com.example.bookcatalog.service.BookService;
import com.example.bookcatalog.service.StockTransferService;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestPostgresConfiguration.class)
@ActiveProfiles("test")
class TransactionBehaviorIntegrationTest {

    @Autowired
    StockTransferService transfers;

    @Autowired
    BookService bookService;

    @Autowired
    BookRepository books;

    @Autowired
    TransactionAuditRepository audits;

    private Long fromId;
    private Long toId;

    @BeforeEach
    void setUp() {
        audits.deleteAllInBatch();
        books.deleteAllInBatch();
        fromId = books.saveAndFlush(book("tx-from", 10)).getId();
        toId = books.saveAndFlush(book("tx-to", 5)).getId();
    }

    @Test
    void runtimeFailure_rollsBackStockAndRequiredAudit() {
        assertThatThrownBy(() ->
                transfers.transferThenRuntimeRollback(fromId, toId, 3))
                .isInstanceOf(IllegalStateException.class);

        assertStocks(10, 5);
        assertThat(audits.count()).isZero();
    }

    @Test
    void checkedFailureWithDefaultRules_commitsStockAndAudit() {
        assertThatThrownBy(() ->
                transfers.checkedFailureDefault(fromId, toId, 3))
                .isInstanceOf(TransactionLabCheckedException.class);

        assertStocks(7, 8);
        assertThat(audits.findAll())
                .singleElement()
                .satisfies(audit -> assertThat(audit.getEventType())
                        .isEqualTo("CHECKED_DEFAULT_COMMITTED"));
    }

    @Test
    void requiresNewAudit_outerFailureRollsBackStockButAuditSurvives() {
        assertThatThrownBy(() ->
                transfers.requiresNewAuditThenFail(fromId, toId, 3))
                .isInstanceOf(IllegalStateException.class);

        assertStocks(10, 5);
        assertThat(audits.findAll())
                .singleElement()
                .satisfies(audit -> assertThat(audit.getEventType())
                        .isEqualTo("TRANSFER_ATTEMPT"));
    }

    @Test
    void delete_intentionalCheckedFailure_rollsBackFlushedDelete() {
        assertThatThrownBy(() -> bookService.delete(fromId))
                .isInstanceOf(TransactionLabCheckedException.class)
                .hasMessage("Intentional failure after flush");

        assertThat(books.existsById(fromId)).isTrue();
    }

    private void assertStocks(int expectedFrom, int expectedTo) {
        assertThat(books.findById(fromId).orElseThrow().getStock())
                .isEqualTo(expectedFrom);
        assertThat(books.findById(toId).orElseThrow().getStock())
                .isEqualTo(expectedTo);
    }

    private static Book book(String isbn, int stock) {
        return new Book(isbn, "Transaction Test", "Author",
                new BigDecimal("10.00"), stock);
    }
}
```


### `src/test/java/com/example/bookcatalog/integration/AuthenticationFlowIntegrationTest.java`

```java
package com.example.bookcatalog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.repository.TransactionAuditRepository;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestPostgresConfiguration.class)
@ActiveProfiles("test")
class AuthenticationFlowIntegrationTest {

    private static final String TEST_PASSWORD = "integration-password-123";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AppUserRepository users;

    @Autowired
    BookRepository books;

    @Autowired
    TransactionAuditRepository audits;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void cleanDatabase() {
        audits.deleteAllInBatch();
        books.deleteAllInBatch();
        users.deleteAllInBatch();
    }

    @Test
    void registerLoginMeAndLogout_usesRealDatabaseBackedSessionFlow()
            throws Exception {
        CsrfData registrationCsrf = csrf(null);

        mockMvc.perform(post("/auth/register")
                        .session(registrationCsrf.session())
                        .param(registrationCsrf.parameterName(),
                                registrationCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "session-user",
                                  "password": "integration-password-123"
                                }
                                """))
                .andExpect(status().isCreated());

        AppUser stored = users.findByUsername("session-user").orElseThrow();
        assertThat(stored.getRole()).isEqualTo("USER");
        assertThat(stored.getPasswordHash()).isNotEqualTo(TEST_PASSWORD);
        assertThat(encoder.matches(TEST_PASSWORD, stored.getPasswordHash()))
                .isTrue();

        CsrfData loginCsrf = csrf(registrationCsrf.session());
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .session(loginCsrf.session())
                        .param(loginCsrf.parameterName(), loginCsrf.token())
                        .param("username", "session-user")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andExpect(authenticated().withUsername("session-user"))
                .andReturn();
        MockHttpSession session = (MockHttpSession)
                login.getRequest().getSession(false);

        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("session-user"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));

        CsrfData logoutCsrf = csrf(session);
        mockMvc.perform(post("/auth/logout")
                        .session(session)
                        .param(logoutCsrf.parameterName(), logoutCsrf.token()))
                .andExpect(status().isNoContent())
                .andExpect(unauthenticated());

        assertThat(session.isInvalid()).isTrue();
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_wrongPassword_returnsUnauthorizedWithoutAuthentication()
            throws Exception {
        users.saveAndFlush(new AppUser("wrong-password-user",
                encoder.encode(TEST_PASSWORD), "USER"));
        CsrfData csrf = csrf(null);

        mockMvc.perform(post("/auth/login")
                        .session(csrf.session())
                        .param(csrf.parameterName(), csrf.token())
                        .param("username", "wrong-password-user")
                        .param("password", "definitely-wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void databaseAdminLogin_withFreshCsrf_canCreateBook() throws Exception {
        users.saveAndFlush(new AppUser("integration-admin",
                encoder.encode(TEST_PASSWORD), "ADMIN"));
        CsrfData loginCsrf = csrf(null);
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .session(loginCsrf.session())
                        .param(loginCsrf.parameterName(), loginCsrf.token())
                        .param("username", "integration-admin")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andExpect(authenticated().withRoles("ADMIN"))
                .andReturn();
        MockHttpSession session = (MockHttpSession)
                login.getRequest().getSession(false);
        CsrfData writeCsrf = csrf(session);

        mockMvc.perform(post("/books")
                        .session(session)
                        .header(writeCsrf.headerName(), writeCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "integration-isbn",
                                  "title": "Integration Book",
                                  "author": "Test Author",
                                  "price": 25.00,
                                  "stock": 6
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("integration-isbn"));

        assertThat(books.findAll())
                .singleElement()
                .satisfies(book -> assertThat(book.getStock()).isEqualTo(6));
    }

    private CsrfData csrf(MockHttpSession existingSession) throws Exception {
        MockHttpServletRequestBuilder request = get("/auth/csrf");
        if (existingSession != null) {
            request.session(existingSession);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        MockHttpSession session = (MockHttpSession)
                result.getRequest().getSession(false);
        return new CsrfData(
                session,
                JsonPath.read(body, "$.parameterName"),
                JsonPath.read(body, "$.headerName"),
                JsonPath.read(body, "$.token"));
    }

    private record CsrfData(
            MockHttpSession session,
            String parameterName,
            String headerName,
            String token) {
    }
}
```

The helper returns sensitive values to the caller but never logs them. MockMvc holds a mock servlet session; it does not expose a real browser cookie jar.

## Verification matrix

| Test class | Level | Spring context? | Real PostgreSQL / Flyway? | Mocked collaborators | Security | Transaction behavior proved |
|---|---|---:|---:|---|---|---|
| `BookRequestTest` | pure Java unit | no | no | none | absent | none |
| `RegistrationServiceTest` | Mockito unit | no | no | repository, encoder | absent | none; annotation is inert |
| `AppUserDetailsServiceTest` | Mockito unit | no | no | user repository | maps authorities only | none |
| `BookControllerWebMvcTest` | MVC slice | partial | no | `BookService` | filters deliberately out of scope | none |
| `SecurityPolicyWebMvcTest` | security MVC slice | partial | no | application service boundaries | real filter chain, synthetic users | none |
| `RepositoryPostgresIntegrationTest` | JPA/database slice | partial | yes / V1–V6 | none | absent | test transaction rolls back each method |
| `BookServiceMethodSecurityIntegrationTest` | full service integration | full | yes / V1–V6 | none | real method interceptor, synthetic identity | service transaction commits/denies |
| `TransactionBehaviorIntegrationTest` | transaction integration | full | yes / V1–V6 | none | not the behavior under test | real commit, rollback, and `REQUIRES_NEW`; no outer test transaction |
| `AuthenticationFlowIntegrationTest` | full MVC/security integration | full + MockMvc | yes / V1–V6 | none | real repository/BCrypt/login/session/CSRF | real service/repository transactions |

### What each infrastructure component owns

| Component | Defined/provided by | Called by | Calls/uses | Starts Spring? | Uses real DB here? |
|---|---|---|---|---:|---:|
| Jupiter `@Test` | JUnit | Surefire/JUnit Platform | developer test method | no | no |
| Mockito mocks | Mockito | developer test | prepared collaborator behavior | no | no |
| `@MockitoBean` | Spring Framework TestContext | context customization | Mockito mock inserted as bean | yes, partial | no in MVC slices |
| `@WebMvcTest` | Spring Boot | TestContext bootstrapper | focused MVC auto-configuration | yes, partial | no |
| MockMvc | Spring Test + Boot config | test method | filters/DispatcherServlet/MVC chain | requires web context | depends on context |
| `@DataJpaTest` | Spring Boot | TestContext bootstrapper | focused JDBC/JPA/repository config | yes, partial | yes in this suite |
| `@SpringBootTest` | Spring Boot | TestContext bootstrapper | full `ApplicationContext` | yes, full | yes when container imported |
| `@ServiceConnection` | Spring Boot Testcontainers | connection-details auto-config | container JDBC/Flyway metadata | participates in context | yes |
| PostgreSQLContainer | Testcontainers | Boot bean lifecycle | Docker-compatible runtime | no by itself | is the real DB engine |
| Flyway | Flyway + Boot | context startup | V1–V6 through JDBC | inside DB context | yes |
| HikariCP | Boot/Hikari | repositories/Flyway infrastructure | pooled pgJDBC connections | inside DB context | yes |

## Commands to run

From `Spring_Security/book-catalog-api`:

```powershell
# Confirm the effective dependency graph and managed versions.
mvn dependency:tree

# Fast focused feedback while developing.
mvn -Dtest=BookRequestTest,RegistrationServiceTest test
mvn -Dtest=BookControllerWebMvcTest,SecurityPolicyWebMvcTest test

# Requires a working Docker-compatible runtime.
mvn -Dtest=RepositoryPostgresIntegrationTest test
mvn -Dtest=TransactionBehaviorIntegrationTest test
mvn -Dtest=AuthenticationFlowIntegrationTest test

# Final reproducible runs.
mvn clean test
mvn verify
```

This reference keeps all tests in Surefire. `mvn verify` runs the `test` phase on the way to `verify`; it does not run a second Failsafe suite because none is configured.

### Expected full-run evidence

```text
Surefire selects JUnit Platform
  -> pure/Mockito/MVC tests run
  -> Boot starts PostgreSQL 17 on a dynamic host port
  -> Flyway validates and applies V1–V6
  -> Hibernate validates the migrated schema
  -> repository, method-security, transaction, and auth tests run
  -> all tests pass
```

On the inspected environment, the compiled reference suite contains 32 tests. Runtime can vary with Docker image/cache speed. The number is evidence of the supplied reference, not a target to inflate.

## Failure diagnosis

| Symptom | Likely boundary | What to inspect |
|---|---|---|
| Jupiter tests are not discovered | Maven test runner | old JUnit/Surefire pin, dependency tree, test names |
| Boot 4 annotation import does not compile | test dependency/package | modular starter and exact `org.springframework.boot.webmvc.test.autoconfigure` / `org.springframework.boot.data.jpa.test.autoconfigure` package |
| `No qualifying bean` in MVC slice | slice boundary | missing `@MockitoBean` for a controller collaborator |
| controller test gets 401/403 unexpectedly | Security filter | whether this is the contract slice or security slice; identity and CSRF |
| anonymous unsafe request is 403 rather than 401 | CSRF/filter order | missing token may be rejected before authorization |
| Hibernate says a table is missing | schema startup | Flyway import, active profile, `ddl-auto=validate`, migration logs |
| test tries configured port 5432 | connection details | missing `@Import(TestPostgresConfiguration.class)` / service connection |
| PostgreSQL rejects `Asia/Saigon` | test JVM environment | versionless Surefire `user.timezone=UTC` configuration |
| constraint test sees no failure | persistence context | flush boundary and actual migration constraint |
| raw Hibernate constraint exception appears | exception-translation boundary | use repository `flush()` when asserting Spring's translated exception |
| transaction test reports wrong durable state | transaction boundary | remove test `@Transactional`; call injected proxy; reread after completion |
| `REQUIRES_NEW` audit disappears | propagation/proxy | outer test transaction, self-invocation, or wrong bean call |
| login returns 403 | CSRF | obtain/use token with the same session |
| login returns 401 | authentication | form field names, saved hash, `UserDetailsService`, wrong password |
| `/auth/me` is 401 after successful login | session | reuse the returned `MockHttpSession` |
| logout test cannot reuse session | expected invalidation | assert `isInvalid()` and issue a new anonymous request |

## Common mistakes and interview checks

### Mistakes this solution deliberately avoids

- It does not create another Spring Boot project.
- It does not add H2 or a developer-database fallback.
- It does not let Hibernate create/update the schema.
- It does not globally disable Spring Security or CSRF.
- It does not use deprecated `@MockBean` or Testcontainers' deprecated PostgreSQL class.
- It does not pretend `StockRequest` has validation.
- It does not expect DELETE success when the service intentionally rolls back.
- It does not put transaction-boundary tests inside a test-owned transaction.
- It does not assume IDENTITY inserts always wait for flush.
- It does not assert an exact BCrypt encoding.
- It does not expose security artifacts in logs/assertion messages.
- It does not use `Thread.sleep`, test order, production credentials, or a production database.

### Retrieval questions with concise answers

1. **Why is `BookRequestTest` not an integration test?** It directly invokes one Java record with no framework or collaborator boundary.
2. **Why use `@MockitoBean` in the MVC slice?** The controller is created by Spring and needs its service dependency inside that context.
3. **Why are filters disabled in one MVC class but enabled in another?** One isolates HTTP/controller contracts; the separate class explicitly proves the security contract.
4. **What does the Security policy slice not prove?** User repository lookup, BCrypt verification, login provider, and session persistence.
5. **Why does the JPA slice use PostgreSQL?** Dialect, migrations, constraints, identity, locking, and transaction semantics are database behavior.
6. **Why explicitly import Flyway in the Boot 4 JPA slice?** Focused Boot 4 slices do not load every modular integration automatically.
7. **Why keep `ddl-auto=validate`?** Flyway owns schema changes; Hibernate only checks compatibility.
8. **Why call `clear()` after flush?** To avoid returning the already managed L1-cached entity and force a database-backed lookup.
9. **Why can `save()` execute an INSERT immediately here?** `GenerationType.IDENTITY` may require the database-generated ID.
10. **Why use repository `flush()` for `DataIntegrityViolationException`?** The Spring repository proxy translates persistence-provider exceptions.
11. **Why is the transaction test not annotated `@Transactional`?** It must observe actual service commit/rollback and `REQUIRES_NEW` boundaries.
12. **What proves method security rather than URL security?** Calling the injected `BookService` proxy as USER and ADMIN.
13. **What proves real authentication?** Form login using a database user, real encoder/provider, and a later request with the same session.
14. **Why does `permitAll` not remove the need for `csrf()`?** Authorization and CSRF protection are separate controls.
15. **Why seed ADMIN directly?** Registration intentionally and correctly assigns only `USER`.
16. **What does Testcontainers solve?** Isolated, disposable access to the real database engine with dynamic connection details.
17. **Is using Testcontainers the Docker deployment lesson?** No; it is ephemeral test infrastructure, not packaging/deploying the application.
18. **Why run Maven after IDE tests?** Maven is the reproducible build/runner used outside one IDE configuration.

## Final mental model

```text
Choose a behavior
  -> choose the narrowest observable boundary
  -> make real only what that boundary requires
  -> control everything nondeterministic
  -> exercise success and important failure
  -> assert output, interaction, and/or durable state
  -> state what the test still cannot prove
```

The completed suite is intentionally layered. A failure in `BookRequestTest` points to Java normalization; a failure in the MVC slice points to HTTP mapping/validation/advice; a failure in the JPA slice points to migrations/mapping/SQL/constraints; a failure in the authentication flow points to the integrated security/session/database path. That diagnostic precision is the practical value of test architecture.
