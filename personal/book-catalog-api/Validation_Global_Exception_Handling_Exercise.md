# Validation + Global Exception Handling Exercise

## Existing project

This exercise extends the existing project at:

```text
REST_API/book-catalog-api/
```

Keep `REST_API/REST_API_Mini_Project.md` nearby. You are evolving that same Book Catalog API, not building another application.

The checked-in project and the guide's reference listing are not perfectly identical. For this exercise, use the checked-in source as the code baseline and the guide's documented HTTP behavior as the contract:

| Baseline fact | What this exercise does |
|---|---|
| `pom.xml` uses Spring Boot `4.1.1` | Keep that exact version |
| `pom.xml` uses `<maven.compiler.release>21</maven.compiler.release>` | Keep Java 21; the older guide's reference POM says Java 17, but changing Java is unrelated |
| Artifact version is `1.0-SNAPSHOT` | Keep it |
| Boot plugin is under `pluginManagement` | Do not reorganize it in this topic |
| `application.yaml` contains the existing local password placeholder | Do not redesign configuration here; use the working local setup from the REST exercise |
| Controller checks trimmed text but POST/PUT pass the original untrimmed strings onward | Normalize nullable request strings in `BookRequest` so validation and stored values follow the guide's intended trimmed contract |
| DELETE does not call `checkId`, so `DELETE /books/0` currently falls through to `404` | Put `@Positive` on every path ID, including DELETE, restoring the documented `400` contract |
| Several request fixture files are empty | Populate them for the negative scenarios |

With the validation starter added, this Boot `4.1.1` build resolves Spring Framework `7.0.9`, Jakarta Validation API `3.1.1`, and Hibernate Validator `9.1.3.Final`. The exercise uses those current APIs rather than older Boot 2/3 controller-validation recipes.

The existing architecture remains:

```text
HTTP
  ↓
BookController
  ↓
BookService
  ↓
BookRepository
  ↓
JdbcTemplate → DataSource/HikariCP → PostgreSQL
```

Before this exercise, the controller also owns input checks and much of the exception-to-status translation:

```text
BookController
├── routing and HTTP responses
├── checkBook(...)
├── checkedText(...)
├── checkStock(...)
├── checkId(...)
├── DuplicateKeyException catch blocks
└── ResponseStatusException construction
```

After the exercise, request DTOs declare structural input constraints, Spring MVC activates validation, the service translates the one known database conflict into application meaning, and global advice translates expected failures into HTTP responses.

## Learning contract

For each task:

1. Read **Objective** and **Concept**.
2. Implement the incomplete **Starter code** without opening the complete solution.
3. Use **Hints** only after making an attempt.
4. Perform **How to verify** from a second terminal while the application keeps running.
5. Read **Common mistakes** and **Explanation**.

The starter fragments intentionally contain `TODO`s and are not complete files. The complete, compilable contents of every changed or added Java file appear only after the pre-solution barrier.

This is a manual HTTP exercise. Do not introduce MockMvc, Spring Boot Test, JUnit migration, Testcontainers, or other automated testing work. Those scenarios can become automated tests when you reach the Testing topic.

The successful API contract stays stable:

| Operation | Success |
|---|---|
| `GET /books` | `200` and a JSON array, possibly `[]` |
| `GET /books/{id}` | `200` and one `BookResponse` |
| `POST /books` | `201`, `Location: /books/{id}`, and the created `BookResponse` |
| `PUT /books/{id}` | `200` and the complete replaced representation |
| `PATCH /books/{id}` | `200` and the book with only stock changed |
| `DELETE /books/{id}` | `204` and no response body |

The expected failure statuses also stay stable: invalid client input is `400`, an absent book is `404`, an ISBN conflict is `409`, and an unexpected server failure remains `500`.

## Files that remain unchanged

Do not rewrite these files:

```text
REST_API/book-catalog-api/
├── compose.yaml
├── database/schema.sql
├── .env
├── .env.example
├── .gitignore
├── REST_API_Mini_Project.md
├── requests/create-book.json
├── requests/replace-book.json
├── src/main/resources/application.yaml
├── src/main/java/com/example/bookcatalog/Application.java
├── src/main/java/com/example/bookcatalog/model/Book.java
├── src/main/java/com/example/bookcatalog/dto/BookResponse.java
├── src/main/java/com/example/bookcatalog/repository/BookRepository.java
└── src/test/java/com/example/AppTest.java
```

Why they remain unchanged:

- PostgreSQL constraints still protect stored integrity; Bean Validation does not replace the schema.
- `JdbcTemplate`, the `RowMapper`, SQL, and connection-pool ownership already work.
- The `Book` domain record and `BookResponse` output contract do not need validation annotations.
- The strict Jackson settings already reject unknown JSON properties and fractional JSON numbers for integer targets.
- Java/POM cleanup, secret-management cleanup, packaging cleanup, and automated testing are separate work.

## Files to modify/add

Paths below are relative to `REST_API/book-catalog-api`.

| File | Action | New-topic reason |
|---|---|---|
| `pom.xml` | MODIFY | Add Boot's validation starter |
| `src/main/java/com/example/bookcatalog/dto/BookRequest.java` | MODIFY | Normalize text, then declare body constraints |
| `src/main/java/com/example/bookcatalog/dto/StockRequest.java` | MODIFY | Declare required nonnegative stock |
| `src/main/java/com/example/bookcatalog/service/BookService.java` | MODIFY | Translate only the known duplicate-key failure |
| `src/main/java/com/example/bookcatalog/web/BookController.java` | MODIFY | Activate body and MVC-native method validation; remove duplicate local mappings |
| `src/main/java/com/example/bookcatalog/validation/AtMostTwoDecimalPlaces.java` | ADD | Preserve the trailing-zero-aware price rule |
| `src/main/java/com/example/bookcatalog/validation/AtMostTwoDecimalPlacesValidator.java` | ADD | Implement that one justified custom constraint |
| `src/main/java/com/example/bookcatalog/exception/BookNotFoundException.java` | ADD | Express expected application-level absence without HTTP coupling |
| `src/main/java/com/example/bookcatalog/exception/DuplicateIsbnException.java` | ADD | Hide `DuplicateKeyException` from the web contract |
| `src/main/java/com/example/bookcatalog/web/GlobalExceptionHandler.java` | ADD | Centralize exception-to-HTTP translation with `ProblemDetail` |
| Five existing files under `requests/` | MODIFY | Restore the empty manual-verification fixtures |

Exactly two application exceptions are added. Validation annotations and their validator are not application exceptions.

No custom `ApiError` DTO is added. Spring Framework 7's `ProblemDetail` is the modest error representation for this exercise. Validation responses add one understandable extension property, `fieldErrors`.

## Concept priorities

| Priority | Concepts |
|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | DTO constraints, `@Valid`, deserialization versus validation, `MethodArgumentNotValidException`, `@RestControllerAdvice`, `@ExceptionHandler`, `400`/`404`/`409`/`500` |
| ⭐⭐⭐⭐ IMPORTANT | MVC-native validation, `HandlerMethodValidationException`, persistence-to-application translation, `ProblemDetail`, normalization, the custom price constraint, and preserving framework statuses/headers |
| ⭐⭐⭐ NICE TO KNOW | `ResponseEntityExceptionHandler` extension hooks, validation groups, proxy-based service method validation, and duplicate-message suppression |
| ⭐⭐ FUTURE KNOWLEDGE | JPA/Hibernate ORM, Spring Security, automated integration tests, tracing, localization, and distributed error contracts |

## Exercise Tasks — Attempt Before Reading the Solution

### Task 1 — Audit the before-state and freeze the contract

**Objective:**

Identify which existing responsibilities move and which behavior must not move.

**Concept:**

A refactor is safe only if you distinguish intended behavior from accidental implementation drift. Validation architecture may change while URLs, successful statuses, DTO shapes, SQL, and database integrity remain stable.

**What to implement:**

Do not edit code yet. Inspect the checked-in POM, DTOs, controller, service, repository, configuration, schema, and request fixtures. Fill in the table.

**Starter code:**

```text
Concern                              Current owner                 Target owner
Body field constraints               TODO                          TODO
Path/query constraints               TODO                          TODO
Text normalization                   TODO                          TODO
Book absence                          TODO                          TODO
DuplicateKeyException meaning        TODO                          TODO
Exception → HTTP status/body          TODO                          TODO
SQL and row mapping                  TODO                          TODO
Connection lifecycle                 TODO                          TODO
```

Also record these baseline observations:

```text
Actual Java release in pom.xml: TODO
Guide reference Java release: TODO
Actual Boot version: TODO
DELETE /books/0 currently follows which code path? TODO
Do POST/PUT currently persist checkedText(...)'s return value? TODO
Which request fixtures are zero bytes? TODO
```

**Hints:**

1. Follow the values, not only the annotations. `checkedText(...)` returns a normalized string, but a caller can ignore it.
2. Inspect DELETE independently from GET/PUT/PATCH.
3. `application.yaml` already contains two Jackson deserialization policies needed by later scenarios.

**How to verify:**

Explain aloud why a valid POST must still return `201` with `Location`, why an absent positive ID remains `404`, and why a malformed JSON body cannot be fixed by a Bean Validation annotation.

**Common mistakes:**

- Treating every discrepancy as permission to modernize unrelated files.
- Calling an accidental `DELETE /books/0 → 404` result the desired contract.
- Assuming a helper's returned trimmed value is used merely because the helper is called.
- Planning to delete PostgreSQL constraints after adding annotations.

**Explanation:**

The checked-in POM is the version baseline: Boot 4.1.1 and Java 21. The guide's endpoint table is the behavioral baseline. The validation refactor deliberately restores positive-ID validation for DELETE and the intended trimmed text behavior, while leaving packaging, secrets, JUnit, Docker, JDBC, and schema concerns alone.

---

### Task 2 — Add the validation runtime

**Objective:**

Put Jakarta Validation and Hibernate Validator on the application classpath through Spring Boot dependency management.

**Concept:**

Jakarta Validation defines the API and constraint model. Hibernate Validator is the provider that executes those constraints. Spring MVC integrates the provider into controller argument processing. Hibernate Validator is not Hibernate ORM and adding it does not add JPA.

**What to implement:**

Add one dependency to the existing `<dependencies>` section. Keep Boot `4.1.1`, Java release `21`, the existing starters, JUnit, devtools, artifact version, and build layout unchanged.

**Starter code:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId><!-- TODO: validation starter --></artifactId>
</dependency>
```

Use no explicit Hibernate Validator or Jakarta Validation version. The Boot parent manages compatible versions.

**Hints:**

1. The artifact is `spring-boot-starter-validation`.
2. Keep `spring-boot-starter-webmvc`; Boot 4 renamed the preferred MVC starter.
3. Imports later will begin with `jakarta.validation`, not `javax.validation`.

**How to verify:**

From the project directory:

```powershell
mvn compile
mvn dependency:tree '-Dincludes=org.hibernate.validator:hibernate-validator'
```

Expect a successful compile and Hibernate Validator supplied transitively. Start the application using the same database and environment setup that already worked in the REST mini-project.

**Common mistakes:**

- Copying `javax.validation` examples from Spring Boot 2.
- Adding a provider version that fights Boot dependency management.
- Replacing `spring-boot-starter-webmvc` with an older tutorial's starter.
- Moving the Boot plugin or changing Java versions during this focused task.

**Explanation:**

The starter contributes the provider and expression-language support. Boot detects the provider and configures a Spring `Validator`. That infrastructure makes later `@Valid` and method constraints executable; annotations alone are only metadata.

---

### Task 3 — Normalize and constrain the complete book request

**Objective:**

Move suitable ISBN, title, author, price, and stock rules from `checkBook(...)` to `BookRequest`.

**Concept:**

The request record is the natural validation boundary because it describes client-writable JSON. Normalization and validation remain distinct operations:

```text
JSON string
  ↓ Jackson calls the record constructor
nullable trim normalization
  ↓
constructed BookRequest
  ↓ Bean Validation
constraints inspect normalized components
```

The compact constructor preserves `null` so missing values can be reported by `@NotBlank` or `@NotNull`. It trims only non-null text with the same `String.trim()` semantics used by the earlier controller helper.

**What to implement:**

In `BookRequest`:

- Normalize `isbn`, `title`, and `author` in a compact record constructor.
- Require nonblank normalized text.
- Limit normalized lengths to 20, 200, and 120.
- Require price and stock.
- Require both numeric values to be nonnegative.
- Cap price at `9999999999.99`.
- Leave the exact decimal-place rule as a TODO for Task 4.

**Starter code:**

```java
public record BookRequest(
        @NotBlank(message = "TODO")
        @Size(max = 20, message = "TODO")
        String isbn,

        // TODO: title and author

        @NotNull(message = "TODO")
        @PositiveOrZero(message = "TODO")
        @DecimalMax(value = "9999999999.99", message = "TODO")
        // TODO: exact decimal-place rule in Task 4
        BigDecimal price,

        // TODO: required, zero allowed
        Integer stock) {

    public BookRequest {
        isbn = normalize(isbn);
        // TODO: normalize title and author
    }

    private static String normalize(String value) {
        // TODO: keep null; trim non-null text
        return value;
    }
}
```

**Hints:**

1. `@NotBlank` rejects `null`, `""`, and whitespace-only strings.
2. `@Size` considers `null` valid, so it complements rather than replaces `@NotBlank`.
3. `@PositiveOrZero` considers `null` valid, so combine it with `@NotNull`.
4. Assigning to compact-constructor parameters changes the values assigned to record components.

**How to verify:**

Run `mvn compile`. Before adding `@Valid` to the controller, predict whether an invalid request will already be rejected over HTTP. Then explain why the answer is no: DTO constraints exist, but the HTTP boundary has not activated them yet.

**Common mistakes:**

- Calling `trim()` before checking for `null`.
- Assuming validation annotations transform values.
- Using primitive `int` for request stock and losing missing/null information.
- Using `double` for price.
- Putting request constraints on the database-facing `Book` record.

**Explanation:**

Normalization in the record fixes a checked-in drift: the previous controller validated trimmed text but sent raw strings to the service. Now both constraints and downstream code see the normalized request. This intentionally matches the guide's documented contract rather than preserving the accidental raw-whitespace behavior.

---

### Task 4 — Preserve meaningful decimal places and validate stock

**Objective:**

Implement the one rule standard constraints do not preserve exactly, then finish `StockRequest`.

**Concept:**

The catalog accepts numerically equivalent trailing zeros:

```text
39.90    valid
39.9000  valid: only two meaningful decimal places
39.9010  invalid: three meaningful decimal places
```

For this Boot 4.1.1 project, Hibernate Validator 9.1.3 evaluates a `BigDecimal`'s represented scale for `@Digits`. Therefore `@Digits(fraction = 2)` would reject `39.9000` and silently change the existing contract. A small custom constraint using `stripTrailingZeros().scale()` is justified.

**What to implement:**

Add `@AtMostTwoDecimalPlaces` and its `ConstraintValidator`. Treat `null` as valid in the custom validator because `@NotNull` owns presence. Add the annotation to `BookRequest.price` alongside the standard constraints.

Then annotate `StockRequest.stock` with `@NotNull` and `@PositiveOrZero`.

**Starter code:**

```java
@Documented
@Constraint(validatedBy = /* TODO */)
@Target(/* TODO: record/component-compatible targets */)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtMostTwoDecimalPlaces {
    String message() default "must have at most two meaningful decimal places";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
public class AtMostTwoDecimalPlacesValidator
        implements ConstraintValidator<AtMostTwoDecimalPlaces, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        // TODO: null belongs to @NotNull
        // TODO: ignore insignificant trailing zeros
        return false;
    }
}
```

```java
public record StockRequest(
        // TODO: missing/null invalid; zero valid
        Integer stock) {
}
```

**Hints:**

1. Use `value == null || value.stripTrailingZeros().scale() <= 2`.
2. A custom constraint should answer one question. Keep nonnegative and maximum rules in standard annotations.
3. Constraint metadata requires `message`, `groups`, and `payload` elements.

**How to verify:**

Run `mvn compile`. On paper, predict the results for:

| Value | Expected |
|---|---|
| `null` | Custom constraint passes; `@NotNull` fails |
| `0` | Pass |
| `39.9000` | Pass |
| `39.9010` | Fail |
| `-1.00` | Decimal-place constraint passes; `@PositiveOrZero` fails |
| `10000000000.00` | `@DecimalMax` fails |

**Common mistakes:**

- Using `BigDecimal.scale() <= 2` and rejecting harmless trailing zeros.
- Replacing every price annotation with one opaque custom validator.
- Returning false for null and creating duplicate presence messages.
- Forcing a custom stock constraint when standard annotations are sufficient.

**Explanation:**

This is the narrow case where custom validation improves correctness. The database still stores `NUMERIC(12,2)` and remains the final integrity boundary. The annotation prevents predictable client errors before SQL; it does not redesign persistence.

> **Stop barrier 1:** Without looking ahead, explain why JSON parsing, compact-constructor normalization, Bean Validation, and PostgreSQL constraints are four different stages. Also explain why `39.9000` must not use `@Digits(fraction = 2)` in this exact project.

---

### Task 5 — Activate body and MVC-native method validation

**Objective:**

Make Spring MVC execute the DTO constraints and replace repeated path/paging checks with direct constraints.

**Concept:**

`@RequestBody` and `@Valid` do different work:

```text
@RequestBody
  JSON bytes → BookRequest

@Valid
  constructed BookRequest → constraint evaluation
```

Spring Framework 7 also performs MVC-native method validation when constraints such as `@Positive`, `@Min`, and `@Max` appear directly on controller parameters. Do not put class-level `@Validated` on this controller. Class-level `@Validated` selects proxy-based method validation; Spring MVC documentation recommends removing it to use the built-in controller method-validation path.

**What to implement:**

- Add `@Valid` to POST, PUT, and PATCH request bodies.
- Add `@Positive` to every path ID, including DELETE.
- Add `@Min`/`@Max` to page and size.
- Remove `checkBook`, `checkStock`, `checkId`, `MAX_PRICE`, local duplicate try/catch blocks, and the `notFound()` helper.
- Keep one small `normalizeAuthor(...)` helper. Author is optional, but if supplied it must be nonblank and at most 120 characters after trimming. A direct `@NotBlank` would incorrectly reject an absent `null` author; `@Size` also validates raw rather than normalized length.
- For now, convert existing `Optional.empty()` and `false` results to a TODO exception added in Task 9.

**Starter code:**

```java
@GetMapping(produces = "application/json")
public List<BookResponse> findAll(
        @RequestParam(name = "author", required = false) String author,
        @RequestParam(name = "page", defaultValue = "0")
        @Min(value = 0, message = "page must be at least 0")
        @Max(value = 1_000_000, message = "page must be at most 1000000") int page,
        @RequestParam(name = "size", defaultValue = "20")
        // TODO: 1..100
        int size) {
    String filter = normalizeAuthor(author);
    // TODO: unchanged service call and mapping
}
```

```java
@PostMapping(consumes = "application/json", produces = "application/json")
public ResponseEntity<BookResponse> create(
        @Valid @RequestBody BookRequest request) {
    // TODO: no checkBook and no DuplicateKeyException catch here
}
```

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(
        @PathVariable("id") @Positive(message = "id must be positive") long id) {
    // TODO: delegate; false will become BookNotFoundException later
}
```

**Hints:**

1. PUT and PATCH have both a constrained path value and a validated body.
2. `normalizeAuthor(null)` returns null. For non-null input, trim first, then check empty/length.
3. It is acceptable for this one helper to throw `ResponseStatusException(BAD_REQUEST, ...)`; inherited Spring error handling already understands that status-bearing exception. It is not a third application exception.
4. Keep `service.setStock(long, Integer)` and repository signatures unchanged; validation guarantees non-null at this HTTP boundary.

**How to verify:**

Compile, restart the application, then send one blank-title POST and `GET /books/0`. Both should now produce `400`. Try `DELETE /books/0`; it should now also produce `400`, repairing the checked-in omission.

At this point, do not require the final `fieldErrors` shape; global advice is not complete yet.

**Common mistakes:**

- Adding `@Valid` to the record type but not the controller parameter.
- Assuming `@RequestBody` automatically means validation.
- Adding class-level `@Validated` from an older method-validation tutorial.
- Annotating optional author with `@NotBlank` and breaking `GET /books` without a filter.
- Keeping old manual checks alongside identical constraints and producing two sources of truth.

**Explanation:**

Direct path/query constraints cause MVC method validation. A standalone POST body commonly fails with `MethodArgumentNotValidException`. Because PUT/PATCH also have constrained IDs, their body and ID failures can be represented together by `HandlerMethodValidationException`. The global handler must support both forms.

---

### Task 6 — Choose the error contract and create global advice

**Objective:**

Create one centralized Spring MVC error boundary without replacing framework status semantics.

**Concept:**

`@RestControllerAdvice` is a component-scanned Spring bean used by MVC's exception-resolution infrastructure. `ResponseEntityExceptionHandler` already understands standard MVC exceptions and their status-specific headers. Extending it lets this exercise customize selected bodies while preserving details such as the `Allow` header on `405`.

The chosen representation is Spring 7 `ProblemDetail`:

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more request values are invalid.",
  "fieldErrors": {
    "title": ["title is required"],
    "stock": ["stock must be nonnegative"]
  }
}
```

The framework may also supply an `instance` member. An omitted `type` has the standard `about:blank` meaning; this solution does not force either URI member itself. Do not make a timestamp, trace ID, SQL detail, exception class, or rejected value part of this beginner contract.

**What to implement:**

Add `GlobalExceptionHandler extends ResponseEntityExceptionHandler` and annotate it with `@RestControllerAdvice`. Add helper methods that:

- create a validation `ProblemDetail`;
- append messages to `Map<String, List<String>>`;
- suppress duplicate messages for one field;
- never echo rejected values.

Do not add handlers yet; Tasks 7–11 fill them in.

**Starter code:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static ProblemDetail validationProblem(
            HttpStatusCode status,
            Map<String, List<String>> fieldErrors) {
        // TODO: title, status/detail, and fieldErrors extension
        throw new UnsupportedOperationException("Implement ProblemDetail");
    }

    private static void addError(
            Map<String, List<String>> fieldErrors,
            String field,
            String message) {
        // TODO: preserve insertion order and do not duplicate a message
    }
}
```

**Hints:**

1. Use `ProblemDetail.forStatusAndDetail(status, detail)`.
2. Use `problem.setProperty("fieldErrors", fieldErrors)`.
3. Use `computeIfAbsent`, then check `contains` before adding.
4. Do not override every MVC hook. Inherited handling is useful.

**How to verify:**

Run `mvn compile`. Explain why the advice is discovered even though no controller constructs it: `Application` is in `com.example.bookcatalog`, above the `web` package, so component scanning finds `@RestControllerAdvice`.

**Common mistakes:**

- Creating a large enterprise error hierarchy.
- Returning stack traces or exception class names.
- Using one string per field and accidentally losing a second constraint message.
- Replacing all framework errors with `400`.
- Constructing a new application context or validator manually.

**Explanation:**

Problem Details supplies a standard small vocabulary. The `fieldErrors` extension exists because clients need actionable validation locations. `ResponseEntityExceptionHandler` remains responsible for ordinary MVC status and header mechanics; the subclass changes only what this API deliberately promises.

---

### Task 7 — Handle request-body validation failures

**Objective:**

Translate body constraint violations into a `400 ProblemDetail` with safe field messages.

**Concept:**

For a request body validated independently, Spring MVC raises `MethodArgumentNotValidException`. Its `BindingResult` contains `FieldError` objects. The controller method does not execute.

**What to implement:**

Override the Spring 7 hook with the exact `HttpStatusCode` signature. Collect all field errors, collect any object-level errors under `_request`, and call `handleExceptionInternal(...)` using the headers and status supplied by Spring.

**Starter code:**

```java
@Override
protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request) {

    Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
    // TODO: exception.getBindingResult().getFieldErrors()
    // TODO: global errors under "_request"
    ProblemDetail problem = validationProblem(status, fieldErrors);
    return handleExceptionInternal(
            exception, problem, headers, status, request);
}
```

**Hints:**

1. `FieldError.getField()` gives the DTO component name.
2. `getDefaultMessage()` gives the configured constraint message.
3. Do not include `FieldError.getRejectedValue()` in the response.
4. The override's status parameter is `HttpStatusCode`, not an old `HttpStatus` signature copied from another Spring generation.

**How to verify:**

Send an invalid POST with a blank title and negative stock. Expect:

- `400`;
- `application/problem+json` when acceptable to the client;
- `title`, `status`, and `detail`;
- both `title` and `stock` under `fieldErrors`;
- no controller/service/repository execution;
- no rejected body values echoed.

**Common mistakes:**

- Handling only the first error.
- Using `Collectors.toMap` without a merge strategy; two messages for one field can throw `IllegalStateException` and turn a client error into `500`.
- Returning `200` with an error object.
- Calling the repository to validate a syntactically invalid DTO.

**Explanation:**

The exception propagates out of MVC argument resolution, not out of the controller body. Global advice converts framework validation details into the public HTTP error representation. That is ordinary Java exception propagation plus Spring MVC exception resolution.

---

### Task 8 — Handle combined body, path, and query validation

**Objective:**

Support Spring Framework 7's MVC-native method-validation result without losing nested request-body field errors.

**Concept:**

Direct constraints on controller parameters raise `HandlerMethodValidationException`. Its results have two relevant forms:

- `getBeanResults()` contains `ParameterErrors`, including field errors from a validated body when method validation governs the whole handler.
- `getValueResults()` contains direct-value errors, such as `id`, `page`, or `size`.

PUT and PATCH combine `@Positive id` with `@Valid @RequestBody`, so handling only `getValueResults()` would omit body field failures. Handling only `getBeanResults()` would omit path/query failures.

**What to implement:**

Override `handleHandlerMethodValidationException(...)`. For input failures, process both collections and cross-parameter errors. If `isForReturnValue()` is true, preserve Spring's 500 status, log the server-side contract failure, and return a generic safe server-error detail instead of publishing request field errors. Add a small helper that derives the public parameter name from explicit `@PathVariable`/`@RequestParam` names before falling back to the Java parameter name.

**Starter code:**

```java
@Override
protected ResponseEntity<Object> handleHandlerMethodValidationException(
        HandlerMethodValidationException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request) {

    // TODO: keep return-value violations as a sanitized, logged 500
    Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

    for (ParameterErrors beanResult : exception.getBeanResults()) {
        // TODO: beanResult.getFieldErrors()
        // TODO: beanResult.getGlobalErrors()
    }

    for (ParameterValidationResult valueResult : exception.getValueResults()) {
        // TODO: name the id/page/size parameter
        // TODO: collect every resolvable message
    }

    // TODO: cross-parameter messages under "_request"
    ProblemDetail problem = validationProblem(status, fieldErrors);
    return handleExceptionInternal(
            exception, problem, headers, status, request);
}
```

**Hints:**

1. `ParameterErrors` implements Spring's `Errors` interface.
2. `ParameterValidationResult.getResolvableErrors()` can contain multiple messages.
3. Explicit annotation names make the error stable even if Java parameter metadata is unavailable.
4. Continue using the duplicate-suppressing `addError(...)` helper.
5. Do not expose invalid server return values; `isForReturnValue()` distinguishes that path.

**How to verify:**

Try:

```powershell
curl.exe -i 'http://localhost:8080/books?page=-1&size=101'
curl.exe -i 'http://localhost:8080/books/0'
curl.exe -i -X PUT 'http://localhost:8080/books/1' `
    -H 'Content-Type: application/json' `
    --data-binary '{"isbn":"x","title":" ","author":"a","price":1,"stock":-1}'
```

Expect `400`. The first response should identify page and size. The PUT should retain body field messages even though the same handler method also has a constrained ID.

**Common mistakes:**

- Handling `ConstraintViolationException` only; that is typical of a different proxy-validation route.
- Adding class-level `@Validated` and changing the exception model unintentionally.
- Reading only `getValueResults()`.
- Assuming every handler method always throws `MethodArgumentNotValidException` for its body.

**Explanation:**

Spring MVC validates the complete method invocation when direct constraints are present. This is why the global error boundary must understand both bean-shaped and scalar-shaped input results. The same exception type can represent a return-value failure, but Spring assigns that server-side contract violation 500; the handler must not relabel or expose it. The controller stays focused on orchestration.

> **Stop barrier 2:** Trace three requests: blank-title POST, `GET /books/0`, and invalid-body PUT to a positive ID. State which exception reaches the advice, whether the controller body runs, and where each field name comes from.

---

### Task 9 — Express book absence once

**Objective:**

Replace scattered local `404` construction with one meaningful application exception and one global mapping.

**Concept:**

`Optional.empty()` and `false` are service/repository outcomes already used by this project. Minimize signature churn: keep those signatures, but let the HTTP orchestration convert an absent required resource into `BookNotFoundException`. The exception itself contains no HTTP annotation or status.

```text
repository/service absence
  ↓ controller requires a book for this operation
BookNotFoundException
  ↓ global advice
404 ProblemDetail
```

**What to implement:**

- Add `BookNotFoundException(long id)`.
- In GET-by-ID, PUT, PATCH, and DELETE, throw it when the existing service result indicates absence.
- Add a matching `@ExceptionHandler` that returns `404` through `handleExceptionInternal(...)`.

**Starter code:**

```java
public final class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(long id) {
        super(/* TODO: safe client-facing message */);
    }
}
```

```java
@ExceptionHandler(BookNotFoundException.class)
public ResponseEntity<Object> handleBookNotFound(
        BookNotFoundException exception,
        WebRequest request) {
    // TODO: 404 ProblemDetail
    // TODO: call handleExceptionInternal with empty headers
}
```

**Hints:**

1. Use `.orElseThrow(() -> new BookNotFoundException(id))`.
2. For DELETE, throw when `service.delete(id)` is false; otherwise return the unchanged empty `204`.
3. Do not put `@ResponseStatus` on the exception; keep mapping in one advice.

**How to verify:**

Use a confirmed absent positive ID for GET, PUT, PATCH, and DELETE. Expect `404` Problem Details from all four and no inserted/changed row. A zero ID must still be `400`, because validation occurs before absence lookup.

**Common mistakes:**

- Changing repository methods to throw HTTP exceptions.
- Returning `404` for `GET /books` when the list is empty.
- Letting PUT create a missing resource.
- Returning a JSON body with successful `204` DELETE.

**Explanation:**

The application exception names the expected failure; advice owns its HTTP meaning. The repository remains unaware of HTTP. Keeping existing `Optional` and boolean service signatures makes this an exception-handling lesson rather than a CRUD redesign.

---

### Task 10 — Translate only the known ISBN conflict

**Objective:**

Remove persistence exception knowledge from the controller while preserving `409`.

**Concept:**

The database unique constraint remains the concurrency-safe final arbiter:

```text
PostgreSQL UNIQUE(isbn)
  ↓
Spring translates the SQL failure to DuplicateKeyException
  ↓ service recognizes this operation's application meaning
DuplicateIsbnException
  ↓ advice
409 Conflict
```

A pre-query cannot replace the unique constraint because concurrent requests can both observe a temporarily free ISBN.

**What to implement:**

- Add `DuplicateIsbnException(Throwable cause)`.
- Catch `DuplicateKeyException` only around `repository.insert(...)` and `repository.replace(...)` in the service.
- Preserve the cause for server-side diagnostics.
- Do not catch broader `DataAccessException` types.
- Add a global `409` handler.
- Ensure the controller contains no duplicate-key try/catch.

**Starter code:**

```java
public final class DuplicateIsbnException extends RuntimeException {
    public DuplicateIsbnException(Throwable cause) {
        super("TODO", cause);
    }
}
```

```java
public Book create(/* unchanged parameters */) {
    try {
        // TODO: existing repository call
    } catch (DuplicateKeyException exception) {
        throw new DuplicateIsbnException(exception);
    }
}
```

```java
@ExceptionHandler(DuplicateIsbnException.class)
public ResponseEntity<Object> handleDuplicateIsbn(
        DuplicateIsbnException exception,
        WebRequest request) {
    // TODO: 409 ProblemDetail
}
```

**Hints:**

1. The current schema's deliberate duplicate key is ISBN; identity IDs are server-generated.
2. Preserve every existing public service method signature.
3. A broken connection, bad SQL, or database outage is not an ISBN conflict.

**How to verify:**

POST one valid unique book, then repeat the same POST. Expect `201` then `409`. Confirm only one row exists for that ISBN. Also replace one existing book with another existing book's ISBN and expect `409` without changing either row.

**Common mistakes:**

- Catching `Exception` or all `DataAccessException` and returning `409`.
- Pre-checking uniqueness and removing the database constraint.
- Exposing a JDBC/Spring exception class or SQL string to the client.
- Moving connection cleanup into advice.

**Explanation:**

`JdbcTemplate` and the pooled `DataSource` still release resources as the exception unwinds. The service translation changes persistence vocabulary into application vocabulary. Global advice performs the separate application-to-HTTP translation.

---

### Task 11 — Normalize conversion failures and protect server errors

**Objective:**

Distinguish unreadable JSON, path/query type mismatch, standard MVC errors, and genuinely unexpected failures.

**Concept:**

Bean Validation sees a Java object. It cannot run when Jackson cannot construct that object or when MVC cannot convert a path/query string.

| Failure | Typical Spring 7 exception | Controller body entered? | Status |
|---|---|---:|---:|
| Malformed JSON | `HttpMessageNotReadableException` | No | `400` |
| Wrong JSON property type/shape | `HttpMessageNotReadableException` | No | `400` |
| Unknown JSON property under current strict policy | `HttpMessageNotReadableException` | No | `400` |
| Fractional JSON number for `Integer` under current strict policy | `HttpMessageNotReadableException` | No | `400` |
| `id=abc`, `page=abc` | `MethodArgumentTypeMismatchException`, handled by the `TypeMismatchException` hook | No | `400` |
| Unsupported request media type | `HttpMediaTypeNotSupportedException` | No | `415` |
| Unacceptable response media type | `HttpMediaTypeNotAcceptableException` | No | `406` |
| Unsupported method | `HttpRequestMethodNotSupportedException` | No | `405` plus `Allow` |
| Unexpected programming/database failure | ordinary exception | Maybe | `500` |

**What to implement:**

- Override `handleHttpMessageNotReadable(...)` with a generic safe detail.
- Override `handleTypeMismatch(...)` with a generic safe detail.
- Let inherited `ResponseEntityExceptionHandler` handling preserve `405`, `406`, `415`, and their headers.
- Add a final `@ExceptionHandler(Exception.class)` fallback only after the inherited specific mappings exist. If an unlisted exception implements Spring's `ErrorResponse`, preserve its supplied body, headers, and status. Otherwise log the full exception server-side and return a generic `500 ProblemDetail`.

Do not import Jackson exception implementation classes. Boot 4 uses Jackson 3; Spring's `HttpMessageNotReadableException` is the stable MVC boundary needed here.

**Starter code:**

```java
@Override
protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request) {
    // TODO: generic 400 ProblemDetail; do not expose exception.getMessage()
}

@Override
protected ResponseEntity<Object> handleTypeMismatch(
        TypeMismatchException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request) {
    // TODO: generic parameter conversion detail
}

@ExceptionHandler(Exception.class)
public ResponseEntity<Object> handleUnexpected(
        Exception exception,
        WebRequest request) {
    if (exception instanceof ErrorResponse errorResponse) {
        // TODO: preserve body, headers, and status
    }
    // TODO: log full details server-side
    // TODO: sanitized 500 ProblemDetail
}
```

**Hints:**

1. Every override should pass Spring's existing `headers` and `status` into `handleExceptionInternal(...)`.
2. The inherited final handler declares specific MVC exception mappings, so those are more specific than the fallback. The `ErrorResponse` branch provides an additional safeguard.
3. Use the inherited `logger` for the unexpected exception.
4. Never return `exception.getMessage()` for the generic 500 response.

**How to verify:**

Send malformed JSON, wrong JSON types, `/books/not-a-number`, `?page=abc`, a write with `text/plain`, a GET with `Accept: application/xml`, and PUT to `/books`. Verify the original `400`, `415`, `406`, and `405` meanings remain distinct. For `405`, inspect the `Allow` header.

A `406` response may have no JSON body: the client explicitly said it cannot accept JSON, which also excludes `application/problem+json`. The status is the contract in that scenario.

**Common mistakes:**

- Treating deserialization failures as constraint violations.
- Copying `com.fasterxml.jackson.databind` handlers into a Jackson 3 application.
- Overriding standard errors and discarding the `Allow` header.
- Catching everything and returning `400`.
- Sending SQL, credentials, class names, or stack traces to clients.

**Explanation:**

Known failures are translated deliberately. Unknown failures retain server-error meaning. The catch-all is safe here because inherited mappings remain more specific and the fallback explicitly preserves any Spring `ErrorResponse`; ordinary unknown exceptions alone become sanitized `500` responses.

> **Stop barrier 3:** Explain why `{"stock":-1}` reaches Bean Validation but `{"stock":1.5}` does not. Then explain why global advice may shape an error response but never manages a JDBC connection.

---

### Task 12 — Restore fixtures and run the negative matrix

**Objective:**

Make every important failure observable through an independent HTTP client.

**Concept:**

Compilation proves types fit together. A request matrix proves routing, conversion, validation, exception translation, response headers, and stored-state effects agree with the contract.

**What to implement:**

Populate the five currently empty fixtures:

```text
requests/stock.json
requests/missing-stock.json
requests/unknown-field.json
requests/fractional-stock.json
requests/malformed.json
```

Run every scenario in [Manual verification scenarios](#manual-verification-scenarios). For each rejected write, GET the book afterward and confirm no state changed.

**Starter code:**

```text
Scenario | observed status | title/detail | fieldErrors | relevant header | DB effect | PASS/FAIL
TODO
```

```json
// requests/missing-stock.json
TODO
```

```json
// requests/fractional-stock.json
TODO
```

**Hints:**

1. Use `curl.exe`, not PowerShell's `curl` alias, when following the supplied commands.
2. Keep the application in terminal A and send requests from terminal B.
3. Use unique ISBN values; generated IDs are not predictable.
4. A response body alone is not proof. Record status, useful headers, and database-visible state.

**How to verify:**

Complete the full matrix below. At minimum, observe missing/blank/long title; every stock failure; malformed/wrong/unknown JSON; all three price failures; invalid IDs/paging/author; missing book; duplicate ISBN; media/method errors; and an unexpected database failure.

**Common mistakes:**

- Leaving zero-byte fixtures and believing curl sent the documented JSON.
- Reusing an unknown existing ID and misclassifying a lookup result.
- Running only happy paths.
- Assuming a rejected write preserved state without reading it afterward.

**Explanation:**

Validation and exception handling are user-visible behavior. The matrix turns each mental-model boundary into evidence and prepares these cases for later automated tests without introducing testing frameworks now.

---

### Task 13 — Trace ownership and review the after-state

**Objective:**

Explain the final failure lifecycle and confirm no unrelated layer was rebuilt.

**Concept:**

The resulting design has distinct translations:

```text
JSON/body/path/query
  ↓
Jackson and Spring MVC
  ↓
Bean Validation
  ↓ valid invocation
Controller → Service → Repository → PostgreSQL

Expected failures
  ↓ normal Java exception propagation
GlobalExceptionHandler
  ↓
status + headers + ProblemDetail
```

**What to implement:**

Review the diff. It should contain only the files listed under **Files to modify/add**. Fill in these traces:

**Starter code:**

```text
Blank title:
JSON → TODO → TODO exception → TODO response

ID 0:
path text → TODO → TODO exception → TODO response

Missing positive ID:
repository TODO → service TODO → controller TODO → advice TODO

Duplicate ISBN:
database TODO → Spring TODO → service TODO → advice TODO

Broken database connection:
repository throws TODO → advice TODO → client TODO; pool cleanup owned by TODO
```

**Hints:**

1. Distinguish detection from HTTP translation.
2. Check that `BookRepository`, schema, Compose, and `application.yaml` did not change.
3. Check that no `javax.validation`, JPA, security, test framework, or custom datasource code appeared.

**How to verify:**

Run:

```powershell
mvn compile
git diff -- REST_API/book-catalog-api
```

Then state the owner of every changed responsibility without opening the code.

**Common mistakes:**

- Saying “Spring handles it” without naming the relevant stage.
- Saying advice catches SQL before `JdbcTemplate` releases resources.
- Claiming Bean Validation guarantees uniqueness.
- Treating `ProblemDetail` as permission to expose internal diagnostics.

**Explanation:**

The repository still owns SQL, the pool still owns reusable connections, and PostgreSQL still owns stored integrity. The new work changes the request/error boundaries: declarative validation, application exceptions, and centralized HTTP translation.

## Manual verification scenarios

Run these after finishing all tasks and before reading the solution. Start from `REST_API/book-catalog-api` with PostgreSQL healthy and the application running. Use a second PowerShell terminal:

You can run the same matrix interactively in Postman: create one request per scenario, keep the base URL in a collection variable, and inspect **status**, **Headers**, **Body**, and the follow-up GET that proves database state. The `curl.exe`/PowerShell forms below make every case reproducible and avoid depending on an exported Postman collection.

```powershell
$base = 'http://localhost:8080'
$runTag = [DateTime]::UtcNow.ToString('yyyyMMddHHmmssfff')
$isbnA = "VALIDATION-A-$runTag"
$isbnB = "VALIDATION-B-$runTag"
$missingId = '9223372036854775807'
```

If the maximum positive `long` exists in your database, choose another confirmed absent positive ID.

### Scenario 1 — Happy paths remain unchanged

Input/action:

Create a book and capture its generated ID:

```powershell
$validBook = @{
    isbn = $isbnA
    title = '  Effective Java  '
    author = '  Joshua Bloch  '
    price = 39.9000
    stock = 10
} | ConvertTo-Json

$create = Invoke-WebRequest -UseBasicParsing -Method Post "$base/books" `
    -ContentType 'application/json' -Body $validBook
$create.StatusCode
$create.Headers.Location
$create.Content
$bookId = ($create.Content | ConvertFrom-Json).id
```

Expected result:

`201`, `Location: /books/{id}`, and normalized ISBN/title/author in the response. PowerShell may serialize the numeric literal as `39.9`; Scenario 6 sends raw numeric tokens to prove the trailing-zero rule precisely. GET, complete PUT, stock-only PATCH, and later DELETE retain their original success statuses.

What concept it proves:

Changing validation and error architecture must not change successful routes, representations, statuses, headers, or normalization.

PASS condition:

The book is created once, its ID can be read from the response, `Location` identifies it, and a follow-up GET returns the normalized values.

FAIL symptoms:

POST returns 200 instead of 201, omits `Location`, stores padded text, changes the response DTO, or rejects otherwise valid input.

### Scenario 2 — Missing, blank, and oversized title

Input/action:

```powershell
$missingTitle = @{
    isbn = "MISSING-TITLE-$runTag"
    author = 'Author'
    price = 1.00
    stock = 1
} | ConvertTo-Json
$missingTitle | curl.exe -i -X POST "$base/books" -H 'Content-Type: application/json' --data-binary '@-'

$blankTitle = @{
    isbn = "BLANK-TITLE-$runTag"
    title = '   '
    author = 'Author'
    price = 1.00
    stock = 1
} | ConvertTo-Json
$blankTitle | curl.exe -i -X POST "$base/books" -H 'Content-Type: application/json' --data-binary '@-'

$longTitle = @{
    isbn = "LONG-TITLE-$runTag"
    title = ('x' * 201)
    author = 'Author'
    price = 1.00
    stock = 1
} | ConvertTo-Json
$longTitle | curl.exe -i -X POST "$base/books" -H 'Content-Type: application/json' --data-binary '@-'
```

Expected result:

Three `400` Problem Details. `fieldErrors.title` is present and no row is created.

What concept it proves:

Missing, content, and size rules are distinct constraints on the normalized request DTO, executed before controller/service work.

PASS condition:

All three requests identify `title`, use the stable error shape, and leave the catalog unchanged.

FAIL symptoms:

One case reaches SQL, whitespace is stored, a 201/500 appears, or the error handler loses the field name.

### Scenario 3 — Missing, null, negative, and zero stock

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/missing-stock.json'
'{"stock":null}' | curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
'{"stock":-1}' | curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
'{"stock":0}' | curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i "$base/books/$bookId"
```

Expected result:

Three `400` responses followed by a successful `200` for zero. Final stock is zero.

What concept it proves:

Boxed `Integer` preserves missing/null information, while `@NotNull` and `@PositiveOrZero` compose requiredness with the rule that zero is valid.

PASS condition:

Each rejected request identifies `stock`, only explicit zero updates the row, and the follow-up GET reports zero.

FAIL symptoms:

Missing/null becomes zero, zero is rejected, a negative value is stored, or null causes an unboxing 500.

### Scenario 4 — Fractional stock fails before validation

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/fractional-stock.json'
curl.exe -i "$base/books/$bookId"
```

Expected result:

`400` with the unreadable/incompatible-body detail. The controller is not invoked and stock remains zero.

What concept it proves:

Jackson's strict integer conversion runs before Bean Validation; `@PositiveOrZero` never receives a usable `Integer` for `1.5`.

PASS condition:

The response is a safe conversion problem and a follow-up GET shows no update.

FAIL symptoms:

The value is truncated to 1, reported as a constraint violation, or allowed to reach the repository.

### Scenario 5 — Malformed JSON, wrong type/shape, and unknown property

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/malformed.json'
'{"stock":"many"}' | curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
'[3]' | curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/unknown-field.json'
curl.exe -i "$base/books/$bookId"
```

Expected result:

Four `400` responses from deserialization/conversion rather than Bean Validation. No write occurs.

What concept it proves:

Syntax, Java-type compatibility, root JSON shape, and unknown-property policy are Jackson/message-conversion concerns that precede DTO constraint evaluation.

PASS condition:

Each request returns the sanitized unreadable-body problem, and the follow-up GET is unchanged.

FAIL symptoms:

Parser/provider details leak, unknown fields are silently ignored despite the existing policy, or any request updates stock.

### Scenario 6 — Price boundaries and meaningful scale

Input/action:

Generate complete replacement bodies so PUT's full-representation contract remains satisfied. Treat each price as text while constructing the JSON; converting a PowerShell numeric value back to JSON can erase insignificant trailing zeros before the request is sent.

```powershell
function Send-Price([string] $priceJson) {
    $body = @"
{"isbn":"$isbnA","title":"Effective Java","author":"Joshua Bloch","price":$priceJson,"stock":0}
"@
    $body | curl.exe -i -X PUT "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
}

Send-Price '-1'
Send-Price '10000000000.00'
Send-Price '1.234'
Send-Price '39.9000'
```

Expected result:

`400`, `400`, `400`, then `200`. The final value proves insignificant trailing zeros were not rejected.

What concept it proves:

Presence/sign/range use standard constraints, while the one custom validator preserves the established meaning-based scale rule that `@Digits` would narrow.

PASS condition:

Negative, out-of-range, and genuinely over-precise prices are rejected; raw JSON `39.9000` is accepted and the final row remains valid.

FAIL symptoms:

PostgreSQL silently rounds `1.234`, `39.9000` is rejected, or any price failure is mislabeled 404/409/500.

### Scenario 7 — Invalid path IDs and conversions

Input/action:

```powershell
curl.exe -i "$base/books/not-a-number"
curl.exe -i "$base/books/0"
curl.exe -i -X DELETE "$base/books/0"
```

Expected result:

Three `400` responses. The first is type conversion; the latter two are MVC-native constraint violations. DELETE now matches the documented positive-ID contract.

What concept it proves:

Lexical conversion and numeric constraints are separate pre-controller stages, and all routes share the positive-ID rule.

PASS condition:

The type mismatch uses the safe parameter problem, both zero IDs identify `id`, and no repository lookup/delete occurs.

FAIL symptoms:

Nonnumeric input becomes 500, zero becomes 404, or DELETE still bypasses ID validation.

### Scenario 8 — Invalid paging and optional author

Input/action:

```powershell
curl.exe -i "$base/books?page=-1"
curl.exe -i "$base/books?page=1000001"
curl.exe -i "$base/books?size=0"
curl.exe -i "$base/books?size=101"
curl.exe -i "$base/books?author=%20%20%20"
curl.exe -i "$base/books"
```

Expected result:

Five `400` responses and then `200` for the omitted optional author.

What concept it proves:

Direct MVC constraints enforce numeric query bounds, while an optional filter may be absent but must be normalized and nonblank when supplied.

PASS condition:

Every out-of-range value is rejected, whitespace-only author is rejected, and omission still lists books normally.

FAIL symptoms:

Defaults become invalid, absence is confused with blank text, invalid paging reaches offset calculation, or a request becomes 500.

### Scenario 9 — Missing book is distinct from bad input

Input/action:

```powershell
curl.exe -i "$base/books/$missingId"
curl.exe -i -X PATCH "$base/books/$missingId" -H 'Content-Type: application/json' --data-binary '@requests/stock.json'
curl.exe -i -X DELETE "$base/books/$missingId"
```

Expected result:

Three `404` Problem Details. No missing-resource operation creates a row.

What concept it proves:

A valid positive identifier can pass input validation and then fail resource lookup; that application outcome is not a malformed request.

PASS condition:

GET, PATCH, and DELETE share the same safe not-found contract and leave the database unchanged.

FAIL symptoms:

Absence becomes 400/500, PUT/PATCH creates a missing book, or different routes publish incompatible error shapes.

### Scenario 10 — Duplicate ISBN remains a conflict

Input/action:

```powershell
$validBook | curl.exe -i -X POST "$base/books" -H 'Content-Type: application/json' --data-binary '@-'
```

Expected result:

`409`, not `400` or `500`. Confirm the original book remains the only row with that ISBN.

To exercise PUT conflict, create a second book with `$isbnB`, record its ID, then replace it using `$isbnA` in an otherwise complete valid body. Expect `409` and unchanged rows.

What concept it proves:

PostgreSQL remains the concurrency-safe uniqueness arbiter; the service translates only this known persistence conflict and advice maps it to HTTP.

PASS condition:

Both create and replace conflicts return sanitized 409 Problem Details, and neither overwrites or duplicates stored data.

FAIL symptoms:

A pre-check replaces the unique constraint, raw SQL/JDBC detail leaks, every database exception becomes 409, or a conflicting write changes a row.

### Scenario 11 — Media type and method statuses survive the advice

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: text/plain' -H 'Accept: application/json' --data-binary '@requests/stock.json'
curl.exe -i "$base/books" -H 'Accept: application/xml'
curl.exe -i -X PUT "$base/books" -H 'Content-Type: application/json' --data-binary '@requests/replace-book.json'
```

Expected result:

`415`, `406`, and `405`. The `405` response retains an `Allow` header containing GET and POST. Do not require JSON for `406`, because the request rejects JSON-compatible representations.

What concept it proves:

Global consistency does not authorize flattening framework errors; status, relevant headers, and negotiable body are separate parts of the HTTP contract.

PASS condition:

All three statuses remain distinct, 405 retains `Allow`, and 406 is accepted even if no Problem Details body can be negotiated.

FAIL symptoms:

The catch-all turns them into 400/500, `Allow` disappears, or verification incorrectly requires JSON from a client that refused it.

### Scenario 12 — Unexpected failure remains 500

Input/action:

Use a reversible database outage instead of damaging SQL or code:

```powershell
docker compose stop postgres
curl.exe -i "$base/books"
docker compose start postgres
docker compose ps
```

Expected result:

`500`, never `400` or `409`. The client does not receive SQL, credentials, a stack trace, or internal class names. Terminal A contains useful server-side diagnostics. Restart PostgreSQL even if the HTTP request behaves differently than expected, wait for it to become healthy, and confirm the API recovers.

This does not test a custom `503` policy; the existing contract deliberately has none.

What concept it proves:

An unknown infrastructure failure retains server-error meaning; `JdbcTemplate`/HikariCP resource ownership remains independent of web advice.

PASS condition:

The response is sanitized 500, detailed diagnostics stay in server logs, PostgreSQL is restarted, and a later request proves recovery.

FAIL symptoms:

The outage is reported as invalid input or duplicate ISBN, internals leak to the client, the pool is manipulated from advice, or PostgreSQL is left stopped.

### Scenario 13 — Successful DELETE remains empty

Input/action:

```powershell
curl.exe -i -X DELETE "$base/books/$bookId"
curl.exe -i -X DELETE "$base/books/$bookId"
```

Expected result:

Empty `204`, then `404 ProblemDetail`. Repetition leaves the resource absent even though response statuses differ.

What concept it proves:

Centralized error handling changes the second outcome without changing successful DELETE semantics; idempotent effect does not require identical statuses.

PASS condition:

The first response has no body, the second has the shared not-found shape, and the book remains absent.

FAIL symptoms:

The 204 contains JSON, deletion returns 200, the second delete succeeds again, or the resource reappears.

### Verification record

For every scenario, record:

```text
Scenario | status | content type | key body fields | key headers | stored-state effect | PASS/FAIL
```

## Stop Before the Complete Solution

Do not continue until you can answer all of these without opening the solution:

1. Why does `@RequestBody` not activate Bean Validation by itself?
2. Why can POST body errors and PUT body errors arrive through different Spring exception types?
3. Why must the method-validation handler inspect both `getBeanResults()` and `getValueResults()`?
4. Why is optional author still normalized with a small conditional helper?
5. Why is `@Digits(fraction = 2)` wrong for this exact price contract?
6. Why does `BookNotFoundException` not contain an HTTP status annotation?
7. Why does the service catch `DuplicateKeyException` but not every `DataAccessException`?
8. Why must `handleExceptionInternal(...)` receive Spring's existing headers and status?
9. Why might `406` have no Problem Details JSON body?
10. Why does an unexpected database exception remain `500` while HikariCP/JdbcTemplate still clean up resources?

Make a commit or otherwise save your attempt before comparing files.

## Complete Reference Solution

Only added or modified files appear below. Do not replace the unchanged repository, schema, Compose file, application configuration, domain record, response DTO, application class, or test skeleton.

### Responsibility changes

| File | Before responsibility | After responsibility |
|---|---|---|
| `pom.xml` | Web/JDBC/runtime dependencies | Same, plus validation integration |
| `BookRequest` | Unconstrained deserialization shape | Normalized complete-write input plus declarative constraints |
| `StockRequest` | Unconstrained patch shape | Required nonnegative stock constraint |
| `BookService` | Straight repository delegation | Same CRUD contract plus one known persistence-to-application translation |
| `BookController` | HTTP orchestration, body checks, path checks, 404/409 status construction | HTTP orchestration, validation activation, optional-author normalization, and absence signaling |
| `GlobalExceptionHandler` | Did not exist | Expected exception-to-HTTP translation and safe framework error shaping |
| Custom constraint files | Did not exist | Exact trailing-zero-aware decimal-place rule |
| Request fixtures | Five files were empty | Reproducible conversion/binding scenarios |

### `pom.xml` — MODIFY

The only intentional POM change is `spring-boot-starter-validation`. Java 21, Boot 4.1.1, the snapshot version, existing JUnit/devtools choices, and the plugin layout remain as checked in.

```xml
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
      <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
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
```

### `src/main/java/com/example/bookcatalog/validation/AtMostTwoDecimalPlaces.java` — ADD

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
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
        ElementType.TYPE_USE,
        ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
public @interface AtMostTwoDecimalPlaces {

    String message() default "must have at most two meaningful decimal places";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

This annotation answers only the decimal-place question. It deliberately leaves presence, sign, and maximum value to standard constraints.

### `src/main/java/com/example/bookcatalog/validation/AtMostTwoDecimalPlacesValidator.java` — ADD

```java
package com.example.bookcatalog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class AtMostTwoDecimalPlacesValidator
        implements ConstraintValidator<AtMostTwoDecimalPlaces, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return value == null || value.stripTrailingZeros().scale() <= 2;
    }
}
```

Examples:

| Value | `stripTrailingZeros().scale()` | Result |
|---|---:|---|
| `39.90` | `1` | valid |
| `39.9000` | `1` | valid |
| `39.9010` | `3` | invalid |
| `0.0000` | `0` | valid |
| `1000` | `-3` | valid |

Negative scale is valid here; it means trailing zeros occur to the left of the decimal point, not that the number has excessive fractional precision.

### `src/main/java/com/example/bookcatalog/dto/BookRequest.java` — MODIFY

Before, this record only described JSON types. After, it normalizes nullable text and declares the complete-write input constraints.

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
        @Size(max = 20, message = "isbn must be at most 20 characters")
        String isbn,

        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @NotBlank(message = "author is required")
        @Size(max = 120, message = "author must be at most 120 characters")
        String author,

        @NotNull(message = "price is required")
        @PositiveOrZero(message = "price must be nonnegative")
        @DecimalMax(
                value = "9999999999.99",
                message = "price must be at most 9999999999.99")
        @AtMostTwoDecimalPlaces
        BigDecimal price,

        @NotNull(message = "stock is required")
        @PositiveOrZero(message = "stock must be nonnegative")
        Integer stock) {

    public BookRequest {
        isbn = normalize(isbn);
        title = normalize(title);
        author = normalize(author);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
```

The compact constructor runs while Jackson constructs the record. Bean Validation then reads the normalized record components. This is normalization plus validation, not validation magically changing strings.

### `src/main/java/com/example/bookcatalog/dto/StockRequest.java` — MODIFY

```java
package com.example.bookcatalog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockRequest(
        @NotNull(message = "stock is required")
        @PositiveOrZero(message = "stock must be nonnegative")
        Integer stock) {
}
```

`Integer` remains boxed. Missing and explicit `null` remain distinguishable from valid zero until validation runs.

### `src/main/java/com/example/bookcatalog/exception/BookNotFoundException.java` — ADD

```java
package com.example.bookcatalog.exception;

public final class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(long id) {
        super("Book with id " + id + " was not found");
    }
}
```

### `src/main/java/com/example/bookcatalog/exception/DuplicateIsbnException.java` — ADD

```java
package com.example.bookcatalog.exception;

public final class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(Throwable cause) {
        super("ISBN already exists", cause);
    }
}
```

Neither application exception chooses an HTTP status. The global advice owns that mapping.

### `src/main/java/com/example/bookcatalog/service/BookService.java` — MODIFY

All public signatures remain the same. Only create/replace gain the known persistence-exception translation.

```java
package com.example.bookcatalog.service;

import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    public List<Book> findAll(String author, int page, int size) {
        return bookRepository.findAll(author, size, page * size);
    }

    public Book create(
            String isbn,
            String title,
            String author,
            BigDecimal price,
            int stock) {
        try {
            return bookRepository.insert(isbn, title, author, price, stock);
        } catch (DuplicateKeyException exception) {
            throw new DuplicateIsbnException(exception);
        }
    }

    public Optional<Book> replace(
            long id,
            String isbn,
            String title,
            String author,
            BigDecimal price,
            int stock) {
        try {
            return bookRepository.replace(id, isbn, title, author, price, stock);
        } catch (DuplicateKeyException exception) {
            throw new DuplicateIsbnException(exception);
        }
    }

    public Optional<Book> setStock(long id, Integer stock) {
        return bookRepository.setStock(id, stock);
    }

    public boolean delete(long id) {
        return bookRepository.delete(id);
    }
}
```

The existing bounded page/size contract makes the checked-in `page * size` calculation safe at its allowed maximum. It is retained rather than turning this lesson into an unrelated service refactor.

### `src/main/java/com/example/bookcatalog/web/BookController.java` — MODIFY

```java
package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.dto.BookResponse;
import com.example.bookcatalog.dto.StockRequest;
import com.example.bookcatalog.exception.BookNotFoundException;
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

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping(produces = "application/json")
    public List<BookResponse> findAll(
            @RequestParam(name = "author", required = false) String author,
            @RequestParam(name = "page", defaultValue = "0")
            @Min(value = 0, message = "page must be at least 0")
            @Max(value = 1_000_000, message = "page must be at most 1000000") int page,
            @RequestParam(name = "size", defaultValue = "20")
            @Min(value = 1, message = "size must be at least 1")
            @Max(value = 100, message = "size must be at most 100") int size) {
        String filter = normalizeAuthor(author);
        return bookService.findAll(filter, page, size).stream()
                .map(BookResponse::from)
                .toList();
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public BookResponse findById(
            @PathVariable("id")
            @Positive(message = "id must be positive") long id) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return BookResponse.from(book);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<BookResponse> create(
            @Valid @RequestBody BookRequest request) {
        Book book = bookService.create(
                request.isbn(),
                request.title(),
                request.author(),
                request.price(),
                request.stock());
        URI location = URI.create("/books/" + book.id());
        return ResponseEntity.created(location).body(BookResponse.from(book));
    }

    @PutMapping(
            path = "/{id}",
            consumes = "application/json",
            produces = "application/json")
    public BookResponse replace(
            @PathVariable("id")
            @Positive(message = "id must be positive") long id,
            @Valid @RequestBody BookRequest request) {
        Book book = bookService.replace(
                        id,
                        request.isbn(),
                        request.title(),
                        request.author(),
                        request.price(),
                        request.stock())
                .orElseThrow(() -> new BookNotFoundException(id));
        return BookResponse.from(book);
    }

    @PatchMapping(
            path = "/{id}",
            consumes = "application/json",
            produces = "application/json")
    public BookResponse setStock(
            @PathVariable("id")
            @Positive(message = "id must be positive") long id,
            @Valid @RequestBody StockRequest request) {
        Book book = bookService.setStock(id, request.stock())
                .orElseThrow(() -> new BookNotFoundException(id));
        return BookResponse.from(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id")
            @Positive(message = "id must be positive") long id) {
        if (!bookService.delete(id)) {
            throw new BookNotFoundException(id);
        }
        return ResponseEntity.noContent().build();
    }

    private static String normalizeAuthor(String author) {
        if (author == null) {
            return null;
        }

        String normalized = author.trim();
        if (normalized.isEmpty() || normalized.length() > 120) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "author must be nonblank and at most 120 characters after trimming");
        }
        return normalized;
    }
}
```

There is intentionally no class-level `@Validated`. Direct parameter constraints activate Spring MVC's native validation. The small author helper remains because optionality plus normalization makes a direct `@NotBlank @Size` combination contract-incompatible: `@NotBlank` rejects absent null, while `@Size` measures the raw value before trimming.

### `src/main/java/com/example/bookcatalog/web/GlobalExceptionHandler.java` — ADD

```java
package com.example.bookcatalog.web;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String REQUEST_ERROR = "_request";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        addFieldErrors(fieldErrors, exception.getBindingResult().getFieldErrors());
        addGlobalErrors(fieldErrors, exception.getBindingResult().getGlobalErrors());

        ProblemDetail problem = validationProblem(status, fieldErrors);
        return handleExceptionInternal(
                exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        if (exception.isForReturnValue()) {
            logger.error("Controller return-value validation failed", exception);
            ProblemDetail problem = problem(
                    status,
                    "Internal Server Error",
                    "An unexpected server error occurred.");
            return handleExceptionInternal(
                    exception, problem, headers, status, request);
        }

        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        for (ParameterErrors beanResult : exception.getBeanResults()) {
            addFieldErrors(fieldErrors, beanResult.getFieldErrors());
            addGlobalErrors(fieldErrors, beanResult.getGlobalErrors());
        }

        for (ParameterValidationResult valueResult : exception.getValueResults()) {
            String parameterName = parameterName(valueResult);
            for (MessageSourceResolvable error : valueResult.getResolvableErrors()) {
                addError(fieldErrors, parameterName, message(error));
            }
        }

        for (MessageSourceResolvable error
                : exception.getCrossParameterValidationResults()) {
            addError(fieldErrors, REQUEST_ERROR, message(error));
        }

        ProblemDetail problem = validationProblem(status, fieldErrors);
        return handleExceptionInternal(
                exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = problem(
                status,
                "Unreadable request body",
                "The request body is missing, malformed, or contains an incompatible JSON value.");
        return handleExceptionInternal(
                exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = problem(
                status,
                "Invalid parameter",
                "A path or query parameter has an incompatible value.");
        return handleExceptionInternal(
                exception, problem, headers, status, request);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Object> handleBookNotFound(
            BookNotFoundException exception,
            WebRequest request) {
        ProblemDetail problem = problem(
                HttpStatus.NOT_FOUND,
                "Book not found",
                exception.getMessage());
        return handleExceptionInternal(
                exception,
                problem,
                new HttpHeaders(),
                HttpStatus.NOT_FOUND,
                request);
    }

    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<Object> handleDuplicateIsbn(
            DuplicateIsbnException exception,
            WebRequest request) {
        ProblemDetail problem = problem(
                HttpStatus.CONFLICT,
                "ISBN conflict",
                exception.getMessage());
        return handleExceptionInternal(
                exception,
                problem,
                new HttpHeaders(),
                HttpStatus.CONFLICT,
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(
            Exception exception,
            WebRequest request) {
        if (exception instanceof ErrorResponse errorResponse) {
            return handleExceptionInternal(
                    exception,
                    errorResponse.getBody(),
                    errorResponse.getHeaders(),
                    errorResponse.getStatusCode(),
                    request);
        }

        logger.error("Unhandled exception while processing an HTTP request", exception);
        ProblemDetail problem = problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected server error occurred.");
        return handleExceptionInternal(
                exception,
                problem,
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    private static ProblemDetail validationProblem(
            HttpStatusCode status,
            Map<String, List<String>> fieldErrors) {
        ProblemDetail problem = problem(
                status,
                "Validation failed",
                "One or more request values are invalid.");
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    private static ProblemDetail problem(
            HttpStatusCode status,
            String title,
            String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }

    private static void addFieldErrors(
            Map<String, List<String>> fieldErrors,
            List<FieldError> errors) {
        for (FieldError error : errors) {
            addError(fieldErrors, error.getField(), message(error));
        }
    }

    private static void addGlobalErrors(
            Map<String, List<String>> fieldErrors,
            List<ObjectError> errors) {
        for (ObjectError error : errors) {
            addError(fieldErrors, REQUEST_ERROR, message(error));
        }
    }

    private static void addError(
            Map<String, List<String>> fieldErrors,
            String field,
            String message) {
        List<String> messages = fieldErrors.computeIfAbsent(
                field, ignored -> new ArrayList<>());
        if (!messages.contains(message)) {
            messages.add(message);
        }
    }

    private static String message(MessageSourceResolvable error) {
        String message = error.getDefaultMessage();
        return message == null ? "is invalid" : message;
    }

    private static String parameterName(ParameterValidationResult result) {
        MethodParameter parameter = result.getMethodParameter();

        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            String name = pathVariable.name().isBlank()
                    ? pathVariable.value()
                    : pathVariable.name();
            if (!name.isBlank()) {
                return name;
            }
        }

        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            String name = requestParam.name().isBlank()
                    ? requestParam.value()
                    : requestParam.name();
            if (!name.isBlank()) {
                return name;
            }
        }

        String discoveredName = parameter.getParameterName();
        return discoveredName != null
                ? discoveredName
                : "argument" + parameter.getParameterIndex();
    }
}
```

Why the handler has this shape:

- It handles both validation exception models used by these controller signatures.
- It reads both bean and scalar method-validation results.
- Lists retain multiple messages per field; `contains` prevents duplicate text.
- It passes Spring's headers/status through every protected hook.
- Inherited handling keeps standard `405`, `406`, and `415` behavior.
- The generic fallback preserves an unlisted `ErrorResponse` before treating anything as an unknown `500`.
- Only server logs receive unexpected exception details.

### `requests/stock.json` — MODIFY

```json
{"stock": 3}
```

### `requests/missing-stock.json` — MODIFY

```json
{}
```

### `requests/unknown-field.json` — MODIFY

```json
{"stock": 3, "title": "This field is not allowed in a stock patch"}
```

### `requests/fractional-stock.json` — MODIFY

```json
{"stock": 1.5}
```

### `requests/malformed.json` — MODIFY

This file is intentionally invalid JSON:

```text
{"stock": }
```

### Compile and run

From `REST_API/book-catalog-api`:

```powershell
mvn compile
mvn spring-boot:run
```

Use the same database and host environment established in the REST mini-project. This exercise deliberately does not change the checked-in password/configuration or plugin layout. If the baseline application did not run before this exercise, return to the earlier environment setup rather than diagnosing that as a validation failure.

## Final architecture comparison

### Before

```text
HTTP request
  ↓
Jackson
  ↓
BookController
├── checkBook / checkStock / checkId
├── page and author checks
├── local DuplicateKeyException catches
├── local ResponseStatusException construction
└── service call
      ↓
BookService → BookRepository → PostgreSQL
```

Problems in the before-state:

- Reusable body rules were embedded in controller methods/helpers.
- POST/PUT ignored the normalized strings returned by `checkedText`.
- DELETE accidentally skipped positive-ID checking.
- 404s and 409s were constructed locally.
- Error bodies varied by failure path.
- Framework conversion failures and manual value failures had no shared public representation.

### After

```text
HTTP request
  ↓
Spring MVC routing and argument resolution
  ├── method/media mismatch → standard MVC ErrorResponse
  ├── path/query conversion → TypeMismatchException
  └── JSON conversion → HttpMessageNotReadableException
             ↓ successful conversion
BookRequest / StockRequest construction
  ├── nullable text normalization
  └── declarative Jakarta Validation constraints
             ↓ valid invocation
BookController
  ├── HTTP orchestration
  ├── optional-author normalization
  ├── Optional/boolean absence → BookNotFoundException
  └── success status/header/body
             ↓
BookService
  ├── existing use cases
  └── DuplicateKeyException → DuplicateIsbnException
             ↓
BookRepository → JdbcTemplate → HikariCP → PostgreSQL constraints

Any expected exception
  ↓ normal Java propagation
DispatcherServlet exception resolution
  ↓
GlobalExceptionHandler
  ├── 400 validation/conversion ProblemDetail
  ├── 404 book absence ProblemDetail
  ├── 409 ISBN conflict ProblemDetail
  ├── standard MVC status + headers preserved
  └── unknown failure logged + sanitized 500
```

### Responsibility boundary summary

| Layer | Owns after the exercise | Still does not own |
|---|---|---|
| Request DTO | Writable shape, normalization, structural value constraints | Resource lookup, uniqueness, HTTP status, SQL |
| Controller | HTTP mapping, validation activation, success response construction, optional-filter orchestration | Repeated body checks, SQL, database exception details |
| Service | Use-case delegation and known persistence-to-application conflict meaning | HTTP response bodies/statuses, connection cleanup |
| Repository | Parameterized SQL, row mapping, affected rows/optionals | HTTP and public error representation |
| PostgreSQL | Final persistent integrity and uniqueness | Friendly field-level client feedback |
| Global advice | Expected exception-to-status/body mapping | Business operations, SQL, connection-pool management |
| Spring MVC/Jackson | Routing, conversion, validation invocation, exception resolution | Your domain contract and database rules |

### Failure lifecycle comparison

```text
BEFORE
invalid DTO → controller helper → ResponseStatusException → framework default error
duplicate ISBN → controller catch → ResponseStatusException → framework default error
missing book → several local branches → differing 404 construction

AFTER
invalid DTO → Bean Validation → validation exception → advice → 400 ProblemDetail
duplicate ISBN → database → DuplicateKeyException → service translation
               → DuplicateIsbnException → advice → 409 ProblemDetail
missing book → BookNotFoundException → advice → 404 ProblemDetail
unknown failure → safe fallback → logged server detail + generic 500 ProblemDetail
```

## Final checklist

### Project scope

- [ ] I extended `REST_API/book-catalog-api`; I did not create another application.
- [ ] Spring Boot remains `4.1.1`.
- [ ] The checked-in Java release remains `21`, despite the prior guide's Java 17 reference listing.
- [ ] I did not fix unrelated POM plugin, JUnit, password, Compose, or packaging drift.
- [ ] `BookRepository`, `Book`, `BookResponse`, schema, Compose, and `application.yaml` are unchanged.
- [ ] No JPA/Hibernate ORM, Spring Security, automated Spring testing, application containerization, or deployment work was introduced.

### Validation

- [ ] The POM contains `spring-boot-starter-validation` without an explicit version.
- [ ] All validation imports use `jakarta.validation`.
- [ ] I can explain what Jakarta Validation specifies and what Hibernate Validator provides.
- [ ] `BookRequest` normalizes nullable text before constraints inspect it.
- [ ] ISBN/title/author are nonblank and bounded after normalization.
- [ ] Price is required, nonnegative, bounded, and has at most two meaningful decimal places.
- [ ] `39.9000` is accepted and `39.9010` is rejected.
- [ ] I can explain why `@Digits(fraction = 2)` was not used for this `BigDecimal` contract.
- [ ] Stock is boxed, required, and nonnegative; zero remains valid.
- [ ] POST, PUT, and PATCH use `@Valid @RequestBody`.
- [ ] Every path ID, including DELETE, is positive.
- [ ] Page remains `0..1000000`; size remains `1..100`.
- [ ] Omitted author is valid; supplied author is trimmed, nonblank, and no longer than 120.

### Exception handling

- [ ] Exactly two meaningful application exceptions were added: book absence and duplicate ISBN.
- [ ] Application exceptions contain no HTTP annotations or response types.
- [ ] The service translates only expected `DuplicateKeyException` from create/replace.
- [ ] Other database failures retain `500` semantics.
- [ ] Advice is a component-scanned `@RestControllerAdvice` bean.
- [ ] I can distinguish `@ExceptionHandler` from `@RestControllerAdvice`.
- [ ] Body-only validation failures are handled through `MethodArgumentNotValidException`.
- [ ] MVC method validation is handled through `HandlerMethodValidationException`.
- [ ] The method-validation handler reads both `getBeanResults()` and `getValueResults()`.
- [ ] Duplicate messages for one field do not crash or repeat unnecessarily.
- [ ] Malformed/incompatible JSON is handled through `HttpMessageNotReadableException`.
- [ ] Path/query conversion is handled through the current type-mismatch hook.
- [ ] Standard MVC statuses and headers remain intact.
- [ ] No class-level `@Validated` was added to the controller.
- [ ] Unexpected exceptions are logged server-side and returned as sanitized `500`, never `400`.

### HTTP behavior

- [ ] POST still returns `201`, a usable `Location`, and `BookResponse`.
- [ ] GET and successful PUT/PATCH still return `200`.
- [ ] Successful DELETE still returns bodyless `204`.
- [ ] Empty collections still return `200 []`.
- [ ] Invalid input returns `400` with useful safe details.
- [ ] A missing positive ID returns `404`.
- [ ] Duplicate ISBN returns `409` and does not create/overwrite another row.
- [ ] Unsupported methods, request media types, and response media types retain `405`, `415`, and `406`.
- [ ] I understand why a `406` response may not contain JSON Problem Details.
- [ ] Rejected writes leave database state unchanged.

### I can explain

- [ ] `@RequestBody` versus `@Valid`.
- [ ] `@Valid` versus `@Validated`.
- [ ] Body validation versus MVC-native path/query method validation.
- [ ] Deserialization failure versus constraint violation.
- [ ] Normalization versus validation.
- [ ] DTO/input validation versus business/application rules versus database constraints.
- [ ] Custom constraint annotation versus `ConstraintValidator`.
- [ ] Java exception propagation through controller/service/repository calls.
- [ ] Persistence exception → application exception → HTTP response translation.
- [ ] `400` versus `404` versus `409` versus `500`.
- [ ] Why the database unique constraint remains the concurrency-safe final arbiter.
- [ ] Why global advice does not manage HikariCP connections.
- [ ] Why unknown failures must never be mislabeled as client mistakes.

When every box is true from memory and every manual scenario is observed—not merely predicted—you are ready to proceed to Spring Data JPA and Hibernate ORM without confusing validation provider behavior with persistence behavior.
