# Testing Solution

Corresponds to [W1–W3 and Tasks 1–16](Testing_Exercise.md). Paths below are relative to `Spring_Security/book-catalog-api`. Attempt the exercise and write predictions before reading its answer. Complete cumulative files appear once; tasks sharing a file link to the final version rather than asking you to concatenate duplicate classes.

## Scope, prerequisites and verification limits

These are proposed learner changes to the test toolchain and `src/test`. This documentation update changes no application source, migration, test suite or POM. It retains the original reference implementation and adds missing cases using the existing architecture.

**Unresolved source prerequisite:** `src/main/resources/db` is empty; V1–V6 exist only in tracked `target/classes/db/migration`. A clean build cannot reconstruct that schema. The project owner must restore authoritative unchanged source migrations in a separate maintenance change before database examples can pass. No test profile, H2 database, Hibernate schema creation or copied build output is used to hide this issue.

Docker's daemon was unreachable during review. PostgreSQL/Flyway, transaction and authentication runtime behavior was inspected in source but not executed here. Verification evidence is recorded under Task 16; compilation alone is not a database test result. Existing target reports are historical artifacts.

Boot 4.1.1 manages the version set recorded in the exercise baseline. Use its modular imports and `org.testcontainers.postgresql.PostgreSQLContainer` from Testcontainers 2. The UTC test-JVM setting is deterministic configuration; the prior material's legacy-timezone error has not been reproduced here.

## Warm-up solutions

<a id="w1"></a>

## W1 — Assertions and exceptions with actual project classes

[Exercise W1](Testing_Exercise.md#w1).

### `src/test/java/com/example/bookcatalog/dto/AssertionsWarmupTest.java`

```java
package com.example.bookcatalog.dto;

import com.example.bookcatalog.model.Book;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssertionsWarmupTest {
    @Test
    void request_normalizesTextAndPreservesNumbers() {
        BigDecimal price = new BigDecimal("19.99");
        BookRequest request = new BookRequest("  isbn-1  ", "  Title  ",
                "  Author  ", price, 3);
        assertAll(
                () -> assertEquals("isbn-1", request.isbn()),
                () -> assertEquals("Title", request.title()),
                () -> assertEquals("Author", request.author()),
                () -> assertEquals(price, request.price()),
                () -> assertEquals(Integer.valueOf(3), request.stock()));
    }

    @Test
    void response_unsavedBook_hasNoUsableId() {
        Book unsaved = new Book("isbn-1", "Title", "Author",
                new BigDecimal("19.99"), 3);
        assertNull(unsaved.getId());
        assertThrows(NullPointerException.class, () -> BookResponse.from(unsaved));
    }
}
```

The text assertions detect missing normalization; numeric assertions detect unintended changes. The null-ID assertion establishes the fixture precondition before the exception assertion. Everything is real Java; no constraints, context or database run. If the exception disappears, inspect whether the DTO conversion or fixture identity contract intentionally changed. This observes a current limitation rather than prescribing public API behavior.

Run after Task 2 setup:

```powershell
mvn -Dtest=AssertionsWarmupTest test
```

<a id="w2"></a>

## W2 — Parameterized normalization cases

[Exercise W2](Testing_Exercise.md#w2).

### `src/test/java/com/example/bookcatalog/dto/ParameterizedBookRequestTest.java`

```java
package com.example.bookcatalog.dto;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ParameterizedBookRequestTest {
    @ParameterizedTest
    @CsvSource({"'Title', Title", "'  Title  ', Title", "' A ', A"})
    void title_normalizesEachInput(String input, String expected) {
        assertEquals(expected, request(input).title());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void title_nullOrBlank_remainsNullOrBecomesEmpty(String input) {
        String actual = request(input).title();
        if (input == null) assertNull(actual);
        else assertEquals("", actual);
    }

    private static BookRequest request(String title) {
        return new BookRequest("isbn-1", title, "Author", new BigDecimal("1.00"), 1);
    }
}
```

Three CSV rows plus null, empty and three whitespace rows yield eight invocations. Each expectation is independently supplied. Null remains null; blank text becomes empty. The record is real, while validation/MVC are absent. A CSV argument-conversion failure is setup, not a normalization regression.

Run after Task 2 setup:

```powershell
mvn -Dtest=ParameterizedBookRequestTest test
```

<a id="w3"></a>

## W3 — Design partitions and boundaries before implementing them

[Exercise W3](Testing_Exercise.md#w3).

### `src/test/java/com/example/bookcatalog/dto/BookRequestBoundaryTest.java`

```java
package com.example.bookcatalog.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BookRequestBoundaryTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (factory != null) factory.close();
    }

    static Stream<Arguments> isbnCases() {
        return Stream.of(
                Arguments.of("null", null, false),
                Arguments.of("empty", "", false),
                Arguments.of("blank", "   ", false),
                Arguments.of("length 1", "a", true),
                Arguments.of("length 19", "a".repeat(19), true),
                Arguments.of("length 20", "a".repeat(20), true),
                Arguments.of("length 21", "a".repeat(21), false),
                Arguments.of("padded 20", "  " + "a".repeat(20) + "  ", true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("isbnCases")
    void isbn_obeysDeclaredConstraints(String caseName, String input, boolean expectedValid) {
        BookRequest request = new BookRequest(input, "Title", "Author",
                new BigDecimal("1.00"), 1);
        boolean isbnValid = validator.validate(request).stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("isbn"));
        assertEquals(expectedValid, isbnValid, caseName);
    }
}
```

Null/empty/blank are invalid partitions; lengths 1/19/20 are accepted representatives/boundaries, 21 crosses the maximum. Padding is removed first, so padded 20 passes. The real validator and record run; Spring and PostgreSQL do not. Filtering the ISBN property prevents another field violation from satisfying the case. A missing-provider error points to validation dependencies, while wrong expected validity points to constraints or normalization.

Run after Task 2 setup:

```powershell
mvn -Dtest=BookRequestBoundaryTest test
```

## Core solutions

<a id="task-1"></a>

## Task 1 — Inventory and boundaries

[Exercise Task 1](Testing_Exercise.md#task-1).

| Behavior | Preconditions/action | Expected result | Boundary | Real / mocked | Not proved |
|---|---|---|---|---|---|
| Registration | New valid account, call register | USER and encoder output saved | Unit | Service real; repository/encoder mocks | BCrypt or uniqueness |
| Body rejection | Blank normalized ISBN, POST /books | 400 + field error; service skipped | MVC | Controller/validation/advice real; service mock | Schema |
| Role denial | USER writes with valid CSRF | 403; service skipped | Security MVC | Filter chain real; synthetic identity | Login |
| Unique/check constraints | Violating data, save/flush | Integrity exception | PostgreSQL JPA | Database/mappings/repository real | HTTP mapping |
| Transaction outcome | Seed committed rows, invoke service proxy | Expected final stocks/audits | Full service integration | Proxies and PostgreSQL real | HTTP filter behavior |
| Real login | Stored encoded password, form credentials+CSRF | Session authenticates later request | Full MockMvc integration | Real security/provider/repository/database | Browser/TCP deployment |

This is a sample matrix, not a substitute for the detailed case tables. The source-migration gap belongs in the inventory. Annotations do not activate on directly constructed targets. Compare `mvn --version` with Java 21 before toolchain work.

<a id="task-2"></a>

## Task 2 — Boot-managed toolchain and shared test profile

[Exercise Task 2](Testing_Exercise.md#task-2).

The original reference POM is retained below. Apply these test-toolchain changes manually when doing the exercise; this update does not install them. Production dependencies and Java/Boot versions are preserved. The inherited plugin versions replace stale local pins. Surefire changes configuration only.

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
    root: WARN
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
    org.springframework.transaction: WARN
    org.springframework.orm.jpa.JpaTransactionManager: WARN
    org.springframework.security: WARN
```

This profile contains no URL or credentials. `@ServiceConnection` supplies them dynamically. It also cannot activate `LocalAdminBootstrap`, which is restricted to profile `local`.

The profile avoids local admin bootstrap and allows service connections to override developer datasource properties. Root and MVC logging are limited to WARN so debug logging does not serialize credential-bearing request DTOs. Full authentication tests below suppress MockMvc's automatic failure dump because it can contain passwords/tokens. Do not add request `.andDo(print())` logging.

Run `mvn dependency:tree`, `mvn help:effective-pom` and `mvn test -DskipTests`. Inspect managed Jupiter 6.0.3, Mockito 5.23.0, Testcontainers 2.0.5 and Surefire 3.5.6. Test compilation is not execution evidence. The W1–W3 files also belong in `src/test/java/com/example/bookcatalog/dto`; the retained core structure above omits them for readability.

<a id="task-3"></a>

## Task 3 — Pure JUnit normalization

[Exercise Task 3](Testing_Exercise.md#task-3).

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
        assertThat(request.price()).isEqualByComparingTo("19.99");
        assertThat(request.stock()).isEqualTo(3);
    }

    @Test
    void constructor_nullText_preservesNullWithoutRunningValidation() {
        BookRequest request = new BookRequest(null, null, null,
                new BigDecimal("1.00"), 0);
        assertThat(request.isbn()).isNull();
        assertThat(request.title()).isNull();
        assertThat(request.author()).isNull();
    }
}
```

The record and its numbers are real; no mock or Spring context exists. Text checks detect trimming regressions, while numeric checks protect pass-through behavior. Null text is preserved and remains a separate validation concern. Run `mvn -Dtest=BookRequestTest test`; this cannot establish HTTP validation.

<a id="task-4"></a>

## Task 4 — Registration decisions with Mockito

[Exercise Task 4](Testing_Exercise.md#task-4).

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
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

    static Stream<String> passwordsAtByteLimit() {
        return Stream.of("a".repeat(72), "é".repeat(36));
    }

    @ParameterizedTest(name = "accepted byte-limit case {index}")
    @MethodSource("passwordsAtByteLimit")
    void register_passwordAt72Bytes_reachesEncoderAndSave(String password) {
        when(users.existsByUsername("boundary-user")).thenReturn(false);
        when(encoder.encode(password)).thenReturn("encoded-boundary");

        service.register(new RegisterRequest("boundary-user", password));

        verify(encoder).encode(password);
        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("encoded-boundary");
        assertThat(saved.getValue().getRole()).isEqualTo("USER");
    }

    static Stream<String> passwordsOverByteLimit() {
        return Stream.of("a".repeat(73), "é".repeat(37));
    }

    @ParameterizedTest(name = "rejected byte-limit case {index}")
    @MethodSource("passwordsOverByteLimit")
    void register_passwordOver72Bytes_rejectsBeforeAnyCollaborator(String password) {
        assertThatThrownBy(() -> service.register(new RegisterRequest("boundary-user", password)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> {
                            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                            assertThat(exception.getReason())
                                    .isEqualTo("Password exceeds the 72-byte BCrypt limit");
                        });
        verifyNoInteractions(users, encoder);
    }
}
```

The captured entity proves username, encoder-output storage and server-side USER assignment. The duplicate-precheck assertions ensure encoding/saving never occurs on that path. The simulated write failure exercises translation; it is not an actual concurrent database race. The added byte-limit cases distinguish 72 ASCII bytes, 73 ASCII bytes, 72 multibyte bytes and 74 multibyte bytes; a character-only limit would miss the last rejection. The real service runs with a mocked repository/encoder, so neither BCrypt nor database transactions are established. Run `mvn -Dtest=RegistrationServiceTest test`; unused stubs usually indicate setup unrelated to the selected branch.

<a id="task-5"></a>

## Task 5 — Stored-user mapping

[Exercise Task 5](Testing_Exercise.md#task-5).

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

    @Test
    void loadUserByUsername_knownUser_mapsUserAuthority() {
        when(users.findByUsername("reader"))
                .thenReturn(Optional.of(new AppUser("reader", "reader-hash", "USER")));
        UserDetails details = service.loadUserByUsername("reader");
        assertThat(details.getUsername()).isEqualTo("reader");
        assertThat(details.getPassword()).isEqualTo("reader-hash");
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }
}
```

The real mapper is tested with repository answers for USER, ADMIN and absence. Username/hash assertions check propagation; authority assertions catch missing or duplicated ROLE_ prefixes. The generic missing-user message is checked because this implementation intentionally uses it. It does not prove login-provider wiring or password verification. Run `mvn -Dtest=AppUserDetailsServiceTest test`.

<a id="task-6"></a>

## Task 6 — Controller success contracts

[Exercise Task 6](Testing_Exercise.md#task-6).

Use the complete `BookControllerWebMvcTest` in [Task 7](#task-7), which includes both success and error cases in one class. Keep filters off only for this declared HTTP/controller boundary. MVC, binding, serialization, validation and advice are real; BookService is a context mock, so JPA/proxies are absent.

The filtered GET asserts exact normalized service arguments, mapped JSON and list size; an empty list is a separate case. Create asserts 201, Location, content type and returned identity. Reflection sets identity only in unsaved MVC fixtures because BookResponse unboxes it. No reflection changes production behavior. Run `mvn -Dtest=BookControllerWebMvcTest test`; a null-ID conversion failure usually means the fixture was not assigned an ID.

<a id="task-7"></a>

## Task 7 — Validation, boundaries and error mapping

[Exercise Task 7](Testing_Exercise.md#task-7).

### `src/test/java/com/example/bookcatalog/web/BookControllerWebMvcTest.java`

```java
package com.example.bookcatalog.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
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
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.CsvSource;
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
                .andExpect(jsonPath("$.length()").value(1))
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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
                .andExpect(jsonPath("$.instance").value("/books"))
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


    @Test
    void findAll_noMatches_returnsEmptyJsonList() throws Exception {
        when(bookService.findAll(null, 0, 20)).thenReturn(List.of());
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    static Stream<Arguments> invalidBodies() {
        String valid = validBookJson("boundary");
        return Stream.of(
                Arguments.of("long ISBN", valid.replace("boundary", "a".repeat(21)), "isbn"),
                Arguments.of("negative price", valid.replace("12.50", "-0.01"), "price"),
                Arguments.of("negative stock", valid.replace("\"stock\": 4", "\"stock\": -1"), "stock"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBodies")
    void create_invalidField_returnsFieldErrorAndSkipsService(
            String caseName, String body, String field) throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.fieldErrors." + field).isArray());
        verifyNoInteractions(bookService);
    }

    @ParameterizedTest
    @CsvSource({"page,-1,page", "page,1000001,page", "size,0,size", "size,1001,size"})
    void findAll_outOfRangeQuery_rejectsBeforeService(
            String parameter, String value, String field) throws Exception {
        mockMvc.perform(get("/books").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors." + field).isArray());
        verifyNoInteractions(bookService);
    }

    @ParameterizedTest
    @CsvSource({"0,1", "0,1000", "1000000,20"})
    void findAll_validQueryBoundaries_reachService(int page, int size) throws Exception {
        when(bookService.findAll(null, page, size)).thenReturn(List.of());
        mockMvc.perform(get("/books").param("page", Integer.toString(page))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(bookService).findAll(null, page, size);
    }

    @Test
    void create_zeroPriceAndStock_areAllowed() throws Exception {
        when(bookService.create("boundary", "Title", "Author", new BigDecimal("0.00"), 0))
                .thenReturn(book(9L, "boundary"));
        String body = validBookJson("boundary").replace("12.50", "0.00")
                .replace("\"stock\": 4", "\"stock\": 0");
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        verify(bookService).create("boundary", "Title", "Author", new BigDecimal("0.00"), 0);
    }

    @Test
    void create_malformedJson_returnsUnreadableProblem() throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Unreadable request body"));
        verifyNoInteractions(bookService);
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

The additional field-isolated and query-boundary cases turn the design matrix into executable checks. Invalid values must produce the intended field error and no service call. Valid zero price/stock and size boundaries must reach the controller/service. Malformed JSON is a binding case, distinct from Bean Validation. Missing/duplicate cases stub domain failures and assert their advice mapping; they do not cause real repository failures. The current placeholder title/detail and misleading upper-size message are preserved rather than repaired. Run `mvn -Dtest=BookControllerWebMvcTest test`.

<a id="task-8"></a>

## Task 8 — URL authorization and CSRF

[Exercise Task 8](Testing_Exercise.md#task-8).

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


    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminWithInvalidCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books").with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON).content(validBookJson()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(bookService);
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

The seven original policy cases are preserved, with an invalid-token case added. Authentication entry point and access-denied handler stay real. A valid ADMIN response proves the controller can be reached, while denied cases verify no service interaction. `@WithMockUser` supplies a synthetic identity and does not invoke repository lookup, BCrypt or form login. An unexpected 403 often means CSRF failed before authorization. Run `mvn -Dtest=SecurityPolicyWebMvcTest test`.

<a id="task-9"></a>

## Task 9 — Method-security proxy

[Exercise Task 9](Testing_Exercise.md#task-9).

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

Design this task before persistence work; execute after the shared helper in [Task 10](#task-10), test profile and restored source migrations are ready. No test `@Transactional` wraps the call. USER denial is checked by exception and unchanged persisted stock; ADMIN success is checked in a new repository read after commit. The direct injected service call isolates method security from URL rules. All service/database components are real; only identity is synthetic. Run `mvn -Dtest=BookServiceMethodSecurityIntegrationTest test`.

<a id="task-10"></a>

## Task 10 — PostgreSQL repository integration

[Exercise Task 10](Testing_Exercise.md#task-10).

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        assertThat(jdbc.queryForList(
                "select version from flyway_schema_history where success and type = 'SQL' order by installed_rank",
                String.class)).containsExactly("1", "2", "3", "4", "5", "6");
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


    @Test
    void findByUsername_absentUser_returnsEmptyAndExistenceMatches() {
        assertThat(users.findByUsername("absent-repository-user")).isEmpty();
        assertThat(users.existsByUsername("absent-repository-user")).isFalse();
        users.saveAndFlush(new AppUser("present-repository-user", "stored-hash", "USER"));
        entityManager.clear();
        assertThat(users.existsByUsername("present-repository-user")).isTrue();
    }

    @Test
    void findByAuthor_exactFilterAndSortedPages_returnOnlyMatchingRows() {
        books.saveAndFlush(book("author-a1", "Author A"));
        books.saveAndFlush(book("other-author", "Author B"));
        books.saveAndFlush(book("author-a2", "Author A"));
        books.saveAndFlush(book("author-a3", "Author A"));
        entityManager.clear();

        assertThat(books.findByAuthor("Author A", PageRequest.of(0, 2, Sort.by("id"))))
                .extracting(Book::getIsbn).containsExactly("author-a1", "author-a2");
        assertThat(books.findByAuthor("Author A", PageRequest.of(1, 2, Sort.by("id"))))
                .extracting(Book::getIsbn).containsExactly("author-a3");
        assertThat(books.findByAuthor("Missing Author", PageRequest.of(0, 2, Sort.by("id"))))
                .isEmpty();
    }

    @Test
    void saveAndFlush_duplicateUsername_exposesUniqueConstraint() {
        users.saveAndFlush(new AppUser("duplicate-user", "stored-hash", "USER"));
        assertThatThrownBy(() -> users.saveAndFlush(
                new AppUser("duplicate-user", "another-hash", "USER")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Book book(String isbn, String author) {
        return new Book(isbn, "Repository Test", author,
                new BigDecimal("20.00"), 10);
    }
}
```

The real container bean follows context lifetime. Do not start/stop it manually per test. The repository slice excludes normal services/MVC/security, imports Flyway explicitly, keeps the database and uses transactional rollback for isolation. Round-trip checks flush then clear to avoid merely observing the same managed object. Existing/missing username cases check derived-query behavior. Exact-author pages include nonmatching data and an explicit ID sort; no assertion assumes identity starts at 1. Run `mvn -Dtest=RepositoryPostgresIntegrationTest test` after the source-migration prerequisite is fixed separately.

<a id="task-11"></a>

## Task 11 — SQL synchronization and constraints

[Exercise Task 11](Testing_Exercise.md#task-11).

The complete repository class in [Task 10](#task-10) contains round-trip, duplicate ISBN update, duplicate username and invalid-role cases. Each failure is a separate transaction; no queries follow a constraint failure inside that test.

These cases prove actual constraint enforcement when the database prerequisites are restored. They should not be read as evidence that the services classify every integrity failure precisely: the current create/replace/registration catch blocks map the entire exception category to a conflict. That broad mapping is an existing limitation documented in the exercise.

The dirty duplicate update deliberately avoids relying on delayed IDENTITY insertion. `books.flush()` crosses Spring's exception-translation boundary. A mocked `DataIntegrityViolationException` would not establish the real constraint. The inspected SQL has no negative stock/price CHECK and permits a null database ISBN; the DTO has a stricter nonblank/20-character contract. Do not claim these layers have identical constraints. Run the same repository-class command and inspect the root SQL cause when a different constraint fails.

<a id="task-12"></a>

## Task 12 — Flyway schema ownership

[Exercise Task 12](Testing_Exercise.md#task-12).

The history test in [Task 10](#task-10) checks both count and exact successful versions `1` through `6`, so six arbitrary history rows cannot satisfy it. The empty test database must run source migrations before Hibernate validates. V1 in the inspected build output already contains version, and V2 uses `ADD COLUMN IF NOT EXISTS`; both migrations still belong to history.

This snapshot cannot reproduce that clean startup until authoritative source migrations are restored separately. Finding six historical target SQL files or reports is insufficient. Keep `ddl-auto=validate`, Flyway enabled and the JPA slice import. Fresh-install success would establish current migration/entity cooperation; it would not establish upgrade safety for every populated older schema. Run `mvn -Dtest=RepositoryPostgresIntegrationTest test` when the prerequisite is resolved.

<a id="task-13"></a>

## Task 13 — Transaction outcomes

[Exercise Task 13](Testing_Exercise.md#task-13).

Start from committed stocks 10 and 5 and transfer 3. These are actual service behaviors from source, not proposed business rules:

| Scenario/method | Result | Stocks | Audit after completion |
|---|---|---|---|
| SUCCESS_REQUIRED / transferSuccessfully | Normal | 7, 8 | TRANSFER_COMPLETED |
| RUNTIME_ROLLBACK / transferThenRuntimeRollback | IllegalStateException | 10, 5 | None |
| FLUSH_THEN_RUNTIME_ROLLBACK / flushThenRuntimeRollback | IllegalStateException | 10, 5 | None |
| CHECKED_DEFAULT_COMMIT / checkedFailureDefault | TransactionLabCheckedException | 7, 8 | CHECKED_DEFAULT_COMMITTED |
| CHECKED_ROLLBACK / checkedFailureWithRollback | TransactionLabCheckedException | 10, 5 | None |
| CAUGHT_REQUIRED_INNER_FAILURE / catchInnerRequiredFailure | UnexpectedRollbackException | 10, 5 | None |
| REQUIRES_NEW_AUDIT / requiresNewAuditThenFail | IllegalStateException | 10, 5 | TRANSFER_ATTEMPT |
| BookService.delete | TransactionLabCheckedException | Book remains | No new audit |

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
import org.springframework.transaction.UnexpectedRollbackException;

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


    @Test
    void successfulTransfer_commitsStockAndRequiredAudit() {
        transfers.transferSuccessfully(fromId, toId, 3);
        assertStocks(7, 8);
        assertThat(audits.findAll()).singleElement().satisfies(audit ->
                assertThat(audit.getEventType()).isEqualTo("TRANSFER_COMPLETED"));
    }

    @Test
    void flushThenRuntimeFailure_stillRollsBackStock() {
        assertThatThrownBy(() -> transfers.flushThenRuntimeRollback(fromId, toId, 3))
                .isInstanceOf(IllegalStateException.class);
        assertStocks(10, 5);
        assertThat(audits.count()).isZero();
    }

    @Test
    void checkedFailureWithRollbackFor_rollsBackStockAndAudit() {
        assertThatThrownBy(() -> transfers.checkedFailureWithRollback(fromId, toId, 3))
                .isInstanceOf(TransactionLabCheckedException.class);
        assertStocks(10, 5);
        assertThat(audits.count()).isZero();
    }

    @Test
    void caughtRequiredFailure_leavesTransactionRollbackOnly() {
        assertThatThrownBy(() -> transfers.catchInnerRequiredFailure(fromId, toId, 3))
                .isInstanceOf(UnexpectedRollbackException.class);
        assertStocks(10, 5);
        assertThat(audits.count()).isZero();
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

No outer test transaction masks completion. Each repository read after the proxy returns/throws observes state in a new repository transaction. Audit count/event assertions distinguish a rollback from an independently committed inner action; original-stock assertions catch partial transfer failures. Cleanup runs before every case, so method order is irrelevant under sequential execution. This class proves service/database behavior, not transaction-controller HTTP mapping. Run `mvn -Dtest=TransactionBehaviorIntegrationTest test`. Wrong state usually indicates a missing proxy, different rollback rule or accidental test transaction.

<a id="task-14"></a>

## Task 14 — Real form login and authentication

[Exercise Task 14](Testing_Exercise.md#task-14).

The complete combined authentication/session class is in [Task 15](#task-15); it retains the original register/login/me/logout flow, wrong-password case and ADMIN write. Registration returns 201 but does not itself establish a logged-in session. The stored USER/hash assertions check server assignment and actual BCrypt matching. Form parameters exercise the real login filter, provider, UserDetailsService and repository. The 204 success assertion plus an authenticated later `/auth/me` request proves session restoration, not just a successful login handler.

No mock user, mock repository or mock encoder exists. A wrong password yields 401, a generic problem and no authentication. This remains a MockMvc servlet integration test, not real browser/network testing. Run `mvn -Dtest=AuthenticationFlowIntegrationTest test` after database prerequisites are ready. For 403 inspect token/session pairing; for 401 inspect form fields, stored account/hash and credentials without logging them.

<a id="task-15"></a>

## Task 15 — CSRF refresh, ADMIN write and logout

[Exercise Task 15](Testing_Exercise.md#task-15).

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
@AutoConfigureMockMvc(printOnlyOnFailure = false)
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
                .satisfies(book -> {
                    assertThat(book.getIsbn()).isEqualTo("integration-isbn");
                    assertThat(book.getStock()).isEqualTo(6);
                });
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

The helper obtains the application's real token and matching session. Login can change session/token state, so subsequent unsafe actions obtain a fresh token. ADMIN is seeded directly because public registration always assigns USER. The final repository assertion proves the write persisted beyond the request's service transaction. Logout asserts response, cleared identity and invalidated session; the next anonymous request verifies access is gone.

`printOnlyOnFailure=false` prevents Boot's automatic MockMvc dump from including credentials/token data if an assertion fails; inspect selected safe fields instead. The helper passes sensitive values in memory and never logs them. Run `mvn -Dtest=AuthenticationFlowIntegrationTest test`. If another test's cleanup deletes your fixture, check that you have not enabled parallel execution against the shared context/container.

<a id="task-16"></a>

## Task 16 — Verification evidence and suite audit

[Exercise Task 16](Testing_Exercise.md#task-16).

### Verification matrix

| Test class | Level | Spring context? | Real PostgreSQL / Flyway? | Mocked collaborators | Security | Transaction behavior proved |
|---|---|---:|---:|---|---|---|
| `AssertionsWarmupTest` | pure Java unit | no | no | none | absent | none |
| `ParameterizedBookRequestTest` | parameterized Java unit | no | no | none | absent | none |
| `BookRequestBoundaryTest` | real-validator unit | no | no | none | absent | none |
| `BookRequestTest` | pure Java unit | no | no | none | absent | none |
| `RegistrationServiceTest` | Mockito unit | no | no | repository, encoder | absent | none; annotation is inert |
| `AppUserDetailsServiceTest` | Mockito unit | no | no | user repository | maps authorities only | none |
| `BookControllerWebMvcTest` | MVC slice | partial | no | `BookService` | filters deliberately out of scope | none |
| `SecurityPolicyWebMvcTest` | security MVC slice | partial | no | application service boundaries | real filter chain, synthetic users | none |
| `RepositoryPostgresIntegrationTest` | JPA/database slice | partial | yes / V1–V6 | none | absent | test transaction rolls back each method |
| `BookServiceMethodSecurityIntegrationTest` | full service integration | full | yes / V1–V6 | none | real method interceptor, synthetic identity | service transaction commits/denies |
| `TransactionBehaviorIntegrationTest` | transaction integration | full | yes / V1–V6 | none | not the behavior under test | real commit, rollback, and `REQUIRES_NEW`; no outer test transaction |
| `AuthenticationFlowIntegrationTest` | full MVC/security integration | full + MockMvc | yes / V1–V6 | none | real repository/BCrypt/login/session/CSRF | real service/repository transactions |

#### What each infrastructure component owns

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

### Commands to run

From `Spring_Security/book-catalog-api`:

```powershell
# Confirm the effective dependency graph and managed versions.
mvn dependency:tree

# Fast focused feedback while developing.
mvn '-Dtest=BookRequestTest,RegistrationServiceTest' test
mvn '-Dtest=BookControllerWebMvcTest,SecurityPolicyWebMvcTest' test

# Requires a working Docker-compatible runtime.
mvn -Dtest=RepositoryPostgresIntegrationTest test
mvn -Dtest=TransactionBehaviorIntegrationTest test
mvn -Dtest=AuthenticationFlowIntegrationTest test

# Final reproducible runs.
mvn clean test
mvn verify
```

This reference keeps all tests in Surefire. `mvn verify` runs the `test` phase on the way to `verify`; it does not run a second Failsafe suite because none is configured.

#### Expected full-run evidence

```text
Surefire selects JUnit Platform
  -> pure/Mockito/MVC tests run
  -> Boot starts PostgreSQL 17 on a dynamic host port
  -> Flyway validates and applies V1–V6
  -> Hibernate validates the migrated schema
  -> repository, method-security, transaction, and auth tests run
  -> all tests pass
```

These are expected results after the source-migration prerequisite and runtime are ready, not a report of a full run in this review. The previous document cited 32 tests; that historical count is not used as current verification evidence. Parameterized invocations and the added cases change the count.

### Failure diagnosis

| Symptom | Likely boundary | What to inspect |
|---|---|---|
| Jupiter tests are not discovered | Maven test runner | old JUnit/Surefire pin, dependency tree, test names |
| Boot 4 annotation import does not compile | test dependency/package | modular starter and exact `org.springframework.boot.webmvc.test.autoconfigure` / `org.springframework.boot.data.jpa.test.autoconfigure` package |
| `No qualifying bean` in MVC slice | slice boundary | missing `@MockitoBean` for a controller collaborator |
| controller test gets 401/403 unexpectedly | Security filter | whether this is the contract slice or security slice; identity and CSRF |
| anonymous unsafe request is 403 rather than 401 | CSRF/filter order | missing token may be rejected before authorization |
| Hibernate says a table is missing | schema startup | First check missing source migrations: target-only SQL disappears on clean; then inspect Flyway import/profile/logs |
| test tries configured port 5432 | connection details | missing `@Import(TestPostgresConfiguration.class)` / service connection |
| A server rejects a legacy JVM timezone | test JVM environment | versionless Surefire `user.timezone=UTC` configuration |
| constraint test sees no failure | persistence context | flush boundary and actual migration constraint |
| raw Hibernate constraint exception appears | exception-translation boundary | use repository `flush()` when asserting Spring's translated exception |
| transaction test reports wrong durable state | transaction boundary | remove test `@Transactional`; call injected proxy; reread after completion |
| `REQUIRES_NEW` audit disappears | propagation/proxy | outer test transaction, self-invocation, or wrong bean call |
| login returns 403 | CSRF | obtain/use token with the same session |
| login returns 401 | authentication | form field names, saved hash, `UserDetailsService`, wrong password |
| `/auth/me` is 401 after successful login | session | reuse the returned `MockHttpSession` |
| logout test cannot reuse session | expected invalidation | assert `isInvalid()` and issue a new anonymous request |

### Common mistakes and interview checks

#### Mistakes this solution deliberately avoids

- It does not create another Spring Boot project.
- It does not add H2 or a developer-database fallback.
- It does not let Hibernate create/update the schema.
- It does not globally disable Spring Security or CSRF.
- It does not use the removed Boot `@MockBean` or Testcontainers' deprecated PostgreSQL class.
- It does not pretend `StockRequest` has validation.
- It does not expect DELETE success when the service intentionally rolls back.
- It does not put transaction-boundary tests inside a test-owned transaction.
- It does not assume IDENTITY inserts always wait for flush.
- It does not assert an exact BCrypt encoding.
- It does not expose security artifacts in logs/assertion messages.
- It does not use `Thread.sleep`, test order, production credentials, or a production database.

#### Retrieval questions with concise answers

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

### Final boundary model

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
### Documentation-review execution evidence

Review performed on 2026-10-16 with Maven 3.9.16 and Java 21.0.12. A temporary copy used the unchanged current application Java/configuration plus the POM and complete test files extracted from this document; no source migrations or historical target classes were copied.

| Check | Actual result |
|---|---|
| Java compilation | All 30 production Java files and 13 test/support Java files compiled with the proposed test toolchain. |
| Warm-ups, pure/Mockito tests and MVC/security slices | 56 invocations passed; 0 failures, 0 errors, 0 skipped, across eight test classes. |
| PostgreSQL repository/method-security/transaction/authentication runtime | Not run: Docker daemon unreachable and authoritative source migrations missing. These four integration test classes compiled only. |
| Documentation structure | 19 matching task IDs, all 12 fields per exercise, separate complete implementations, balanced fences and local links checked. |
| Application preservation | File hashes checked: no changes to application code, current tests, POM, configuration or existing migration artifacts. |

The focused command actually executed in the temporary copy was:

```powershell
mvn --batch-mode --no-transfer-progress '-Dtest=AssertionsWarmupTest,ParameterizedBookRequestTest,BookRequestBoundaryTest,BookRequestTest,RegistrationServiceTest,AppUserDetailsServiceTest,BookControllerWebMvcTest,SecurityPolicyWebMvcTest' test
```

Compilation included all complete Java solution files, including the database test classes. The focused selection excluded those four classes from execution; it did not mark them skipped or prove their runtime outcomes. No clean full-suite success, JaCoCo run or GitHub Actions run is claimed.

The Java 21 run emitted Mockito's dynamic-agent warning but passed. If a future JDK restricts attachment, consult the version's Mockito agent setup; do not suppress a real initialization failure by skipping tests.

No production/test/POM changes were applied to the checked-in application. Optional JaCoCo/CI examples remain documentation only. Review the [competency checklist](Testing_Exercise.md#competency-checklist) and be able to trace every added assertion to a case before using AI to generate more tests.


