# REST API Mini Project

## `book-catalog-api` — Book Catalog REST API

**Estimated time:** 5–7 focused hours<br>
**Stack:** Java 17+, Maven, Spring Boot 4.1.1, Spring MVC, Spring JDBC, Boot-managed HikariCP, PostgreSQL 17, Docker Compose<br>
**Learning focus:** HTTP, resource contracts, JSON, controller mappings, DTOs, response status/headers, and the request lifecycle.

Build one small catalog that can create, list, retrieve, replace, change stock, and delete books. PostgreSQL runs in Docker; the Java application runs on your Windows host. A client in a second terminal sends HTTP requests while the application keeps listening.

You already know the lower half of this stack:

```text
HTTP client in terminal B
    ↓ method + URI + headers + optional JSON
embedded Tomcat in the Java process in terminal A
    ↓
Spring MVC / DispatcherServlet
    ↓ mapping + argument resolution + JSON deserialization
BookController → BookService → BookRepository
                                    ↓
                               JdbcTemplate
                                    ↓
                         DataSource / HikariCP
                                    ↓ pgJDBC
                         PostgreSQL in Docker

Book → BookResponse → Jackson → JSON + status + headers → client
```

The service and repository remain Spring beans, constructor injection still applies, and Boot still owns infrastructure. The new boundary is an HTTP request and response. Keep [REST_API_Essential_Concepts.md](REST_API_Essential_Concepts.md) beside this exercise.

### Quick navigation

- [Learning contract](#learning-contract)
- [API contract](#api-contract)
- [Target project structure](#target-project-structure)
- [Exercise tasks](#exercise-tasks--attempt-before-reading-the-solution)
- [Manual verification scenarios](#manual-verification-scenarios)
- [Complete reference solution](#complete-reference-solution)
- [Troubleshooting](#troubleshooting)
- [Checklist and reflection](#final-project-checklist)

## Learning contract

For each task, read **Objective** and **Concept**, implement the incomplete **Starter code**, use **Hints** only when needed, run **How to verify**, then read **Common mistakes** and **Explanation**. The early snippets are partial fragments; later tasks make the application complete. Do not expect a fragment containing `TODO` or `UnsupportedOperationException` to behave as the completed project.

At each stop barrier, explain what happened before continuing. The full solution is deliberately after the tasks and verification scenarios. Try your implementation first, then compare one file at a time.

The project uses small manual checks and controller-local status mapping as a bridge to your next topic. It does not introduce Bean Validation, global exception advice, JPA, Spring Security, a testing framework, application containers, or a multi-operation transaction tutorial. Every write here is one SQL statement. Production concerns such as authentication and concurrency version checks remain future work.

## Version note

The reference pins Spring Boot **4.1.1** and Java **17**. Boot manages compatible Spring, Jackson, HikariCP, and pgJDBC versions. For Boot 4, use `spring-boot-starter-webmvc`; the familiar `spring-boot-starter-web` name from Boot 3 tutorials is deprecated in Boot 4. Boot 4 uses Jackson 3 by default; this exercise needs no direct Jackson imports. Do not mix Boot 3/Jackson 2 customization examples into this POM. [Spring Boot starters](https://docs.spring.io/spring-boot/reference/using/build-systems.html#using.build-systems.starters), [Spring Boot JSON support](https://docs.spring.io/spring-boot/reference/features/json.html).

`postgres:17` pins the database major version and can receive maintenance updates. Commands below use a per-process UTC JVM setting for a consistent local baseline, including environments where the JVM's `Asia/Saigon` alias is rejected by PostgreSQL. This changes only the launched JVM; it does not change Windows time or global Java settings.

## API contract

Base URL: `http://localhost:8080`. All successful representations use JSON; a `204` has no response body. `isbn` is a unique, nonblank catalog key of at most 20 characters; checking real ISBN checksums is outside this exercise.

| Method and path | Request | Successful response | Expected client mistakes |
|---|---|---|---|
| `GET /books` | Optional `author`, `page`, `size` query parameters | `200` and a JSON array, possibly `[]` | Invalid paging or author input: `400` |
| `GET /books/{id}` | Positive ID in path | `200` and one book | Bad ID format/value: `400`; absent book: `404` |
| `POST /books` | All five writable fields | `201`, `Location: /books/{id}`, created book | Bad input: `400`; duplicate ISBN: `409` |
| `PUT /books/{id}` | All five writable fields, even unchanged ones | `200` and the replaced book | Bad input: `400`; missing book: `404`; ISBN conflict: `409` |
| `PATCH /books/{id}` | Only `{"stock":3}` | `200` and the updated book | Missing/null/negative stock or extra fields: `400`; absent book: `404` |
| `DELETE /books/{id}` | No body | `204`, no body | Bad ID: `400`; absent book: `404` |

The writable fields are `isbn`, `title`, `author`, `price`, and `stock`. The server assigns `id`. `title` is 1–200 characters after trimming, `author` 1–120, `price` is nonnegative and at most `9999999999.99` with at most two significant decimal places, and `stock` is a nonnegative Java `Integer` value. JSON numbers such as `19.9900` have the same monetary value as `19.99` and are accepted. A fractional stock such as `1.5` is rejected by the configured JSON conversion policy.

For all request DTOs, **unknown properties are rejected**. Sending `id`, misspelling `stock`, or including `title` in the stock-only PATCH produces `400`. This is an explicit application policy, not a universal REST rule. DTOs use boxed `Integer` so missing or `null` stock remains distinguishable from valid zero.

PUT replaces all writable fields of an existing resource; this API does not create through PUT. PATCH uses our documented, custom JSON format for assigning an **absolute stock value**. It is neither JSON Patch nor JSON Merge Patch. Repeating `{"stock":3}` leaves stock at `3`; it does not add three. Repeating DELETE can change `204` to `404` while its intended effect—resource absent—remains idempotent.

List filtering is an exact, case-sensitive author match after trimming the query value. Paging is zero-based: `page=0`, `size=20` by default; allowed page range is `0..1000000`, size `1..100`. Results always use `ORDER BY id ASC`. There is no dynamic sorting parameter or total-count envelope in this small API. An empty collection or a page beyond the data returns `200 []`.

Error **status codes** are part of the exercise contract. The exact default error JSON is not: it can vary with Boot version, request `Accept`, and error configuration. Do not assert a particular `message`, `path`, timestamp, or Problem Details shape. Unexpected server/database failures remain `500` here; no special database-outage-to-`503` mapping has been installed. [Spring MVC response handling](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responseentity.html).

## Target project structure

Create this as a separate practice project; the Markdown file itself does not install an application into the learning repository.

```text
book-catalog-api/
├── pom.xml
├── docker-compose.yml
├── .env.example
├── .env                         # local copy; ignored
├── .gitignore
├── database/
│   └── schema.sql
├── requests/
│   ├── create-book.json
│   ├── replace-book.json
│   ├── stock.json
│   ├── missing-stock.json
│   ├── unknown-field.json
│   ├── fractional-stock.json
│   └── malformed.json
└── src/main/
    ├── resources/application.yml
    └── java/com/example/bookcatalog/
        ├── Application.java
        ├── model/Book.java
        ├── dto/BookRequest.java
        ├── dto/StockRequest.java
        ├── dto/BookResponse.java
        ├── repository/BookRepository.java
        ├── service/BookService.java
        └── web/BookController.java
```

The main class sits above every application package. The `web` package owns HTTP decisions; DTOs own the public JSON shape; the service exposes use cases without HTTP types; the repository owns SQL. The domain record and response DTO happen to have the same fields today but represent different contracts.

| Practice area | Priority |
|---|---|
| Resource URIs, methods, request/response anatomy | ⭐⭐⭐⭐⭐ MUST KNOW |
| Controller mapping, JSON request/response DTOs | ⭐⭐⭐⭐⭐ MUST KNOW |
| `200` / `201` + `Location` / `204` / `400` / `404` / `409` | ⭐⭐⭐⭐⭐ MUST KNOW |
| Controller → service → repository and constructor injection | ⭐⭐⭐⭐⭐ MUST KNOW |
| PUT/PATCH distinction and observable idempotency | ⭐⭐⭐⭐ IMPORTANT |
| Query parameters, bounded paging, content negotiation | ⭐⭐⭐⭐ IMPORTANT |
| Manual negative scenarios and boundary diagnosis | ⭐⭐⭐⭐ IMPORTANT |
| Representation evolution and pagination envelopes | ⭐⭐⭐ NICE TO KNOW |
| Validation framework, global errors, locking, security, test automation | ⭐⭐ FUTURE KNOWLEDGE |

## Exercise Tasks — Attempt Before Reading the Solution

### Task 1 — Draw the boundaries and create the project

Objective:

Create the Maven layout and identify the responsibility of each layer before adding routes.

Concept:

The client does not call a Java method directly. It sends an HTTP message; Spring MVC selects a method on a Spring bean. Your existing DI graph remains inside the server process.

What to implement:

Create `book-catalog-api`, the directories in the target tree, `.gitignore`, and a short handwritten sketch of the request/return flow. Add files as their tasks arrive so unfinished future classes do not block compilation. Keep PostgreSQL infrastructure separate from Java source.

Starter code:

```powershell
New-Item -ItemType Directory -Force -Path .\book-catalog-api
Set-Location .\book-catalog-api
# TODO: create database/, requests/, src/main/resources/
# TODO: create the com/example/bookcatalog Java package and its five subpackages
# TODO: ignore .env, target/, and local IDE metadata
```

```text
Client → TODO → DispatcherServlet → TODO → Service → TODO → PostgreSQL
Java response DTO → TODO → HTTP response
```

Hints:

1. The source root is `src/main/java`, not part of a Java package name.
2. `Application` belongs in `com.example.bookcatalog`, above `web`, `service`, and `repository`.
3. The five subpackages are `model`, `dto`, `repository`, `service`, and `web`.

How to verify:

Run `Get-ChildItem -Recurse`. Point at each directory and name its responsibility. The project has no Dockerfile or Java Compose service.

Common mistakes:

- Placing `Application` under `com.example.bookcatalog.app`, making sibling packages fall outside its default scan.
- Putting SQL inside `src/main/java`.
- Naming classes for verbs such as `GetBookController` instead of grouping a resource's routes.

Explanation:

The package split makes the new HTTP boundary visible without changing how Spring constructs and injects beans.

---

### Task 2 — Add the web runtime and a persistent entry point

Objective:

Create a Java 17 Boot application with MVC, JDBC, and the PostgreSQL driver.

Concept:

The MVC starter supplies the web stack and embedded server. Boot's application context still owns beans; now it also owns infrastructure that must remain alive while clients make requests.

What to implement:

Create the POM using Boot parent `4.1.1`, coordinates `com.example:book-catalog-api:1.0.0`, Java 17, the MVC and JDBC starters, runtime pgJDBC, and the Boot Maven plugin. Create the main application class. Do not manually version the managed libraries or instantiate Tomcat/HikariCP.

Starter code:

```xml
<!-- Fragment inside your Maven project; add the normal project/modelVersion elements. -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
    <relativePath/>
</parent>
<!-- TODO: coordinates and java.version -->
<dependencies>
    <!-- TODO: spring-boot-starter-webmvc -->
    <!-- TODO: spring-boot-starter-jdbc -->
    <!-- TODO: org.postgresql:postgresql with runtime scope -->
</dependencies>
<!-- TODO: build/plugins/spring-boot-maven-plugin -->
```

```java
package com.example.bookcatalog;

// TODO: imports and the composite Boot application annotation
public class Application {
    public static void main(String[] args) {
        // TODO: start Boot with this source class and the command-line arguments
    }
}
```

Hints:

1. The new Boot 4 artifact is `spring-boot-starter-webmvc`.
2. `SpringApplication.run(...)` is the same bootstrap you studied in the Boot guide.
3. Do not immediately close its returned context. That would stop the embedded server.

How to verify:

```powershell
mvn --version
mvn dependency:tree
mvn compile
```

Expect Java 17 or newer and `BUILD SUCCESS`. Locate Spring MVC, Tomcat, JDBC, HikariCP, and pgJDBC in the tree. Start the application only after Task 4 supplies database configuration.

Common mistakes:

- Assuming the core Boot starter alone supplies HTTP routing.
- Mixing WebFlux and MVC starters without a reason.
- Using a one-shot console application's `try (context)` around `run(...)`.

Explanation:

The HTTP server runs in the same JVM as your application. Packaging an executable JAR packages a server application, not a separate Tomcat installation.

---

### Task 3 — Prepare PostgreSQL and the resource table

Objective:

Start one local database with the exact storage constraints required by the API contract.

Concept:

HTTP requests will cross a network boundary before reaching the JDBC operations you know. Docker still owns only the database. The official image runs initialization SQL on the first start of an empty data directory.

What to implement:

Create Compose project `book-catalog-api`, service `postgres`, `postgres:17`, a named `postgres_data` volume, loopback host port `5433`, required `.env` values, and a health check. Mount `database/schema.sql` read-only under `/docker-entrypoint-initdb.d/01-schema.sql`. Create `books` with generated ID, unique ISBN, required text, `NUMERIC(12,2)` price, and nonnegative integer stock. There are no seed rows.

Starter code:

```yaml
name: book-catalog-api
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: "${POSTGRES_DB:?Set POSTGRES_DB in .env}"
      # TODO: required user and password
    ports:
      - "TODO:loopback:host-port:container-port"
    volumes:
      # TODO: persistent data and read-only schema mount
    healthcheck:
      # TODO: pg_isready using escaped in-container variables
volumes:
  # TODO: declare the named volume
```

```sql
CREATE TABLE IF NOT EXISTS books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn TODO,
    title TODO,
    author TODO,
    price TODO,
    stock TODO
);
```

Create `.env.example` with database `book_catalog`, user `book_app`, placeholder password, and `POSTGRES_PORT=5433`.

Hints:

1. Port publishing is `127.0.0.1:${POSTGRES_PORT:-5433}:5432`.
2. Escape Compose interpolation for health-check shell variables as `$${POSTGRES_USER}` and `$${POSTGRES_DB}`.
3. Use `VARCHAR(20)`, `VARCHAR(200)`, `VARCHAR(120)` and nonblank checks for ISBN/title/author.
4. Finish the schema before starting the fresh volume. Container health and schema existence are separate checks.

How to verify:

```powershell
Copy-Item .\.env.example .\.env
notepad .\.env
docker compose config --quiet
docker compose up -d
docker compose ps
docker compose exec postgres psql -U book_app -d book_catalog -c '\d books'
docker compose exec postgres psql -U book_app -d book_catalog -c 'SELECT count(*) FROM books;'
```

Replace the local password before starting. Expect a healthy container, the required columns/constraints, and zero rows on a fresh volume.

Common mistakes:

- Starting with an empty schema file and expecting a restart to run the edited script.
- Reusing port 5432 while your earlier pool exercise still owns it.
- Assuming editing `.env` changes a role already stored in the database volume.

Explanation:

The unique constraint is the final arbiter for duplicate ISBNs. An HTTP-level pre-check cannot replace it because requests can arrive concurrently.

---

### Task 4 — Configure the host application and observe its lifetime

Objective:

Connect the host JVM to PostgreSQL and keep the HTTP server available for a second terminal.

Concept:

Compose reads `.env` for its own interpolation. It does not export those values into the PowerShell process that launches Maven. Boot reads the host process environment through externalized configuration. [Compose interpolation](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/).

What to implement:

Create `application.yml`: application name, environment-based datasource values, Hikari max 5/min idle 1, disabled Boot SQL initialization, and server bound to `127.0.0.1:8080`. Add explicit Jackson policies to reject unknown properties and fractional integer input. Set host `DB_*` variables and check presence before launching.

Starter code:

```yaml
spring:
  application:
    name: book-catalog-api
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5433/book_catalog}
    username: ${DB_USERNAME:book_app}
    password: ${DB_PASSWORD}
    # TODO: Hikari settings
  # TODO: disable Boot SQL initialization; Compose owns the schema
  # TODO: spring.jackson.deserialization policies
server:
  address: TODO
  port: TODO
```

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5433/book_catalog'
$env:DB_USERNAME = 'book_app'
$env:DB_PASSWORD = 'TODO-the-same-local-password-used-by-PostgreSQL'
foreach ($name in @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD')) {
    # TODO: throw if this process environment value is missing or blank; never print it
}
mvn '-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC' spring-boot:run
```

Hints:

1. Under `spring.jackson.deserialization`, set `fail-on-unknown-properties: true` and `accept-float-as-int: false`.
2. Set `spring.sql.init.mode: never`.
3. `[Environment]::GetEnvironmentVariable($name)` and `[string]::IsNullOrWhiteSpace(...)` provide the preflight.
4. Host Java uses `localhost:5433`, not Compose DNS `postgres:5432`.

How to verify:

Keep terminal A running. In terminal B run `curl.exe -i http://localhost:8080/books`. At this stage expect **404**, because no controller exists yet, rather than connection refused. Stop Java with Ctrl+C before continuing edits if you are not using IDE restarts.

Common mistakes:

- Setting `DB_PASSWORD` in terminal B but starting Java in terminal A.
- Treating `${DB_PASSWORD}` as guaranteed early validation with a predictable error message.
- Thinking a startup banner proves a database operation has succeeded.
- Closing the context or pool after startup as in a console exercise.

Explanation:

The HTTP transport can be alive even when no route is registered. A later DB-backed GET will verify the full path. Leave global `JAVA_TOOL_OPTIONS` unchanged; the supplied timezone option applies only to this launched app.

> **Stop barrier 1:** Explain why you now receive an HTTP 404 instead of a network connection error, why the process stays alive, and which process owns each environment value.

---

### Task 5 — Define request, response, and domain records

Objective:

Make the public JSON shape explicit without exposing server-owned input fields.

Concept:

Jackson deserializes JSON into request records and serializes response records into JSON. The domain model is the service/repository representation. Identical fields today do not make those contracts identical responsibilities.

What to implement:

Create `Book`, `BookRequest`, `StockRequest`, and `BookResponse` with the fields from the contract. Use `BigDecimal` for price, `long` for the stored ID, and primitive stored stock. Both request records must use boxed `Integer stock`. Add `BookResponse.from(Book)`.

Starter code:

```java
// In model/Book.java: add package and imports.
public record Book(long id, String isbn, String title, String author,
                   BigDecimal price, int stock) {
}
```

```java
// In dto/BookRequest.java: add package and imports.
public record BookRequest(/* TODO: writable fields only; preserve missing stock */) {
}
```

```java
// In dto/StockRequest.java
public record StockRequest(/* TODO: only the permitted patch field */) {
}
```

```java
// In dto/BookResponse.java: add package, imports, and record components.
public record BookResponse(/* TODO */) {
    public static BookResponse from(Book book) {
        // TODO: map each domain value to the public response
        throw new UnsupportedOperationException("Implement mapping");
    }
}
```

Hints:

1. Requests have no `id`; responses do.
2. Records expose accessors such as `book.title()`, not `getTitle()`.
3. `null` stock means missing/null input; zero is a legitimate stock value.

How to verify:

Run `mvn compile`. On paper, map the create fixture's JSON property names to record components and predict what `{}` produces for a `StockRequest` before manual checks run.

Common mistakes:

- Using one database record for every input and accidentally accepting client IDs.
- Making a stock patch use the entire `BookRequest` with unclear null semantics.
- Using `double` for money or primitive request stock.

Explanation:

The DTO boundary is where the HTTP representation becomes typed Java input. It lets the resource contract evolve without forcing every persistence/domain detail onto clients.

---

### Task 6 — Implement read-only JDBC repository operations

Objective:

Supply typed book results to the upper layers using the JDBC infrastructure you already understand.

Concept:

`JdbcTemplate` still performs parameterized SQL and row mapping through the shared `DataSource`. A missing row is a persistence outcome; the repository does not decide whether that becomes HTTP 404.

What to implement:

Create `@Repository BookRepository` with constructor-injected `JdbcTemplate`, one `RowMapper<Book>`, `Optional<Book> findById(long id)`, and `List<Book> findAll(String author, int size, long offset)`. The author may be null for no filter. Always order by ID and bind paging values.

Starter code:

```java
@Repository
public class BookRepository {
    private final JdbcTemplate jdbc;
    // TODO: constructor and a reusable RowMapper<Book>

    public Optional<Book> findById(long id) {
        // TODO: SELECT columns FROM books WHERE id = ?
        // TODO: map zero rows to Optional.empty(), one row to Optional.of(book)
        throw new UnsupportedOperationException("Implement lookup");
    }

    public List<Book> findAll(String author, int size, long offset) {
        // TODO: choose fixed SQL with or without an author predicate
        // TODO: ORDER BY id ASC LIMIT ? OFFSET ?
        throw new UnsupportedOperationException("Implement list");
    }
}
```

Hints:

1. `jdbc.query(sql, rowMapper, parameters...)` gives a list; `.stream().findFirst()` handles zero/one row.
2. Use `ResultSet.getBigDecimal("price")`.
3. Concatenate only fixed SQL fragments, never author or other user input.

How to verify:

Run `mvn compile`. Inspect SQL parameter order against argument order. Actual HTTP-to-database verification follows in Task 7; do not add an unrelated console runner just to duplicate it.

Common mistakes:

- Throwing `ResponseStatusException` from the repository.
- Keeping a `Connection` field or creating a pool in each method.
- Returning `null` for an empty list.
- Paging without deterministic ordering.

Explanation:

The old repository/resource-ownership model survives. The difference is that a service invoked by a controller, rather than your console `main`, is now the caller.

---

### Task 7 — Expose collection and individual GET routes

Objective:

Serve real database results through two distinguishable HTTP resource shapes.

Concept:

Class-level `@RequestMapping("/books")` combines with method mappings. `@PathVariable` converts a path segment; returning a DTO or list from a `@RestController` uses message conversion for the response body.

What to implement:

Create `@Service BookService`, constructor-inject the repository, and delegate the two reads. Create `@RestController BookController`, inject the service, and implement JSON-producing GET collection and item methods. For the first collection version, call the service with `author=null`, `page=0`, `size=20`; Task 9 exposes query inputs. Reject nonpositive IDs as `400`; translate absent items to `404` in the controller.

Starter code:

```java
@Service
public class BookService {
    // TODO: injected repository
    public Optional<Book> findById(long id) {
        // TODO: delegate without importing any HTTP class
        throw new UnsupportedOperationException("Implement service lookup");
    }
    // TODO: findAll(author, page, size); calculate (long) page * size
}
```

```java
@RestController
@RequestMapping("/books")
public class BookController {
    // TODO: constructor-injected service

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BookResponse> findAll() {
        // TODO: domain list → response DTO list
        throw new UnsupportedOperationException("Implement collection response");
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookResponse findById(@PathVariable("id") long id) {
        // TODO: reject nonpositive ID, map missing item to 404, return DTO
        throw new UnsupportedOperationException("Implement item response");
    }
}
```

Hints:

1. An ordinary DTO/list return defaults to successful `200` here.
2. Use `Optional.orElseThrow(...)` with a local `ResponseStatusException(HttpStatus.NOT_FOUND, ...)`.
3. Define small private `checkId`, `badRequest`, and `notFound` helpers if useful.

How to verify:

Restart Java and send `GET /books`, `GET /books/999999999`, `GET /books/0`, and `GET /books/abc` using `curl.exe -i`. On a fresh database expect `200 []`, `404`, `400`, and `400`. `/abc` fails type conversion before the handler body.

Common mistakes:

- Returning 404 for an empty collection.
- Returning an `Optional` directly and leaving absent-resource semantics unclear.
- Putting SQL in the controller because it is now the entry point.
- Forgetting `@RestController` and accidentally asking MVC to resolve a view.

Explanation:

You have now exercised the complete request chain, including actual PostgreSQL access. The controller translates the Java result into the public HTTP contract.

> **Stop barrier 2:** Trace the `200 []` request from socket to SQL and back to JSON. Then explain why `/books/abc` can fail before repository code runs.

---

### Task 8 — Create a resource with POST, 201, and Location

Objective:

Accept a complete JSON representation of writable fields and report the identity of the new resource.

Concept:

POST to a collection asks the server to create a member. A successful create returns `201`; `Location` identifies the created resource. `@RequestBody` delegates body conversion to Jackson before your method executes.

What to implement:

Add a repository `insert(...)` using `INSERT ... RETURNING`, a service `create(...)`, and a JSON-consuming/producing controller POST. Add small local checks for required trimmed text, length limits, monetary bounds/scale, and nonnegative non-null stock. Catch `DuplicateKeyException` locally and return `409`. Create `requests/create-book.json` using the input below.

Starter code:

```java
// Repository: fill in the SQL and argument order.
public Book insert(String isbn, String title, String author,
                   BigDecimal price, int stock) {
    // TODO: one INSERT with five placeholders and RETURNING all Book columns
    throw new UnsupportedOperationException("Implement insert");
}
```

```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
             produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<BookResponse> create(@RequestBody BookRequest request) {
    // TODO: local input checks
    // TODO: call the HTTP-independent service, catching duplicate ISBN conflict
    // TODO: 201 + Location /books/{generatedId} + created response DTO
    throw new UnsupportedOperationException("Implement creation response");
}
```

```json
{
  "isbn": "9780134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "price": 39.90,
  "stock": 10
}
```

Hints:

1. `ResponseEntity.created(URI.create("/books/" + book.id())).body(...)` constructs the required status/header/body.
2. `RETURNING` supplies the database-generated ID; do not guess it from a count or `MAX(id)`.
3. Compare monetary bounds numerically; `price.stripTrailingZeros().scale() > 2` detects values needing more than two decimal places without rejecting `39.9000`.
4. Do not pre-check ISBN then assume an insert cannot conflict; let the unique constraint decide.

How to verify:

```powershell
curl.exe -i -X POST http://localhost:8080/books -H 'Content-Type: application/json' `
    --data-binary '@requests/create-book.json'
```

Expect `201`, a `Location` using the generated ID, and a JSON book. Follow that location with GET. Repeat POST with the same fixture: expect `409`, then confirm there is still one row for that ISBN. Record the ID; never assume it is 1.

Common mistakes:

- Returning `200` for creation or omitting `Location`.
- Accepting client IDs or hand-building JSON strings.
- Treating all database failures as duplicate ISBN.
- Returning `500` for the deliberate duplicate request.

Explanation:

Response status and headers are now deliberate API decisions. The service still returns a `Book`; it does not need to know that HTTP creation uses `201`.

---

### Task 9 — Add author filtering and bounded pagination

Objective:

Select a subset of the collection without inventing new action-style endpoints.

Concept:

The path names the collection; query parameters refine which representation the client requests. `@RequestParam` performs string-to-Java conversion and supports defaults.

What to implement:

Replace the Task 7 no-argument list handler with optional `author`, default `page=0`, and default `size=20`. Apply contract bounds, reject blank/oversized supplied authors, and pass normalized values into the existing service/repository. Return a plain list; do not add a total-count query or dynamic sort.

Starter code:

```java
@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public List<BookResponse> findAll(
        @RequestParam(name = "author", required = false) String author,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size) {
    // TODO: page 0..1000000, size 1..100; reject invalid input with 400
    // TODO: null means no author filter; supplied author must be valid trimmed text
    // TODO: map the service result to DTOs
    throw new UnsupportedOperationException("Implement query parameters");
}
```

Hints:

1. Calculate offset as `(long) page * size` so multiplication happens as a long.
2. Encode spaces in query values as `%20` or let a client encode them.
3. Keep `ORDER BY id ASC`; SQL identifiers cannot be safely selected by binding an arbitrary sort expression as a value.

How to verify:

```powershell
curl.exe -i 'http://localhost:8080/books?author=Joshua%20Bloch&page=0&size=1'
curl.exe -i 'http://localhost:8080/books?size=0'
curl.exe -i 'http://localhost:8080/books?page=-1'
curl.exe -i 'http://localhost:8080/books?page=1000000&size=100'
```

Expect a matching one-element page when the created book exists, `400`, `400`, then `200 []` for a page beyond this small dataset. A plain array does not tell the client the total number of books.

Common mistakes:

- Implementing `/getBooksByAuthor` when the resource remains the books collection.
- Treating `page` as one-based while documenting zero-based behavior.
- Interpolating author strings into SQL.
- Allowing an unbounded size because this first dataset is small.

Explanation:

The same route now accepts a richer collection request while keeping stable resource naming and explicit resource-use limits.

---

### Task 10 — Replace an existing book with PUT

Objective:

Implement full replacement of the writable state at a known resource URI.

Concept:

This API's PUT requires every writable field and preserves the server-owned ID. It does not merge omitted values and does not create a missing resource.

What to implement:

Add repository/service `replace(...)` returning `Optional<Book>`. Use one `UPDATE` of all five writable columns with `WHERE id = ? RETURNING ...`. In the controller, reuse the complete-body checks, translate absence to `404`, duplicate ISBN to `409`, and return the updated DTO with `200`. Create `replace-book.json` with the same ISBN, title `Effective Java - Local Catalog Edition`, author `Joshua Bloch`, price `35.50`, and stock `8`.

Starter code:

```java
public Optional<Book> replace(long id, String isbn, String title,
                              String author, BigDecimal price, int stock) {
    // TODO: UPDATE all writable fields, bind id last, and RETURNING all columns
    // TODO: zero returned rows means absent resource
    throw new UnsupportedOperationException("Implement replacement");
}
```

```java
@PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
public BookResponse replace(@PathVariable("id") long id,
                            @RequestBody BookRequest request) {
    // TODO: checks, service call, 404/409 translation, response mapping
    throw new UnsupportedOperationException("Implement PUT");
}
```

Hints:

1. Do not SELECT first just to establish existence; the UPDATE's result is authoritative for that statement.
2. Returning a DTO directly is enough for `200` when no custom header is needed.
3. Reuse the POST input DTO because both accept the same complete writable shape.

How to verify:

Use `curl.exe -i -X PUT` with the replacement fixture and your actual ID. Expect `200` and changed values. Repeat the same PUT and GET: the writable state is unchanged by the repeat. Sending `{ "stock": 3 }` to PUT must return `400`. Sending a complete body to a confirmed absent positive ID must return `404`.

Common mistakes:

- Updating only non-null fields and calling it full replacement.
- Creating a new ID when the targeted book is missing.
- Reading the book, writing it, then reading again in three separate statements.

Explanation:

The full replacement rule is part of the contract you chose. HTTP permits other documented PUT creation behavior, but this API consistently chooses update-only PUT.

---

### Task 11 — Assign stock with a narrowly defined PATCH

Objective:

Change one permitted part of a book without touching its other values.

Concept:

PATCH means applying a partial modification; the payload format must define that modification. Here `{"stock":3}` means set the stock to exactly three. The format does not implement either standardized JSON patch document format.

What to implement:

Add `setStock(long id, int stock)` in repository/service, one `UPDATE books SET stock = ? WHERE id = ? RETURNING ...`, and a PATCH handler accepting `StockRequest`. Reject missing/null/negative stock. Unknown fields must be rejected through the configured JSON policy. Create `stock.json` containing `{"stock":3}`.

Starter code:

```java
public Optional<Book> setStock(long id, int stock) {
    // TODO: one statement; no read-modify-write of the whole Book
    throw new UnsupportedOperationException("Implement stock assignment");
}
```

```java
@PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
              produces = MediaType.APPLICATION_JSON_VALUE)
public BookResponse setStock(@PathVariable("id") long id,
                             @RequestBody StockRequest request) {
    // TODO: positive id; stock must be non-null and nonnegative
    // TODO: service result maps to 200 DTO or 404
    throw new UnsupportedOperationException("Implement PATCH");
}
```

Hints:

1. Call `checkStock(Integer)` before auto-unboxing to `int`.
2. Do not calculate `currentStock + request.stock()`.
3. Assign only the `stock` column so unrelated writable fields are not overwritten from a stale read.

How to verify:

Send the patch twice, then GET. Stock stays `3`, and ISBN/title/author/price match their pre-patch values. Send `{}`, `{"stock":null}`, `{"stock":-1}`, and `{"stock":3,"title":"extra"}`: each must produce `400` and leave the book unchanged.

Common mistakes:

- Using primitive request stock and accepting `{}` as an instruction to set zero.
- Describing every PATCH as idempotent just because this assignment is.
- Claiming `application/json` plus a partial DTO is automatically JSON Merge Patch.
- Allowing a stock patch to rewrite title or price.

Explanation:

The distinction between PUT and PATCH is visible in permitted inputs and database effects. The atomic stock statement avoids a read/whole-book-write gap, though it does not solve every possible stale-client concurrency problem.

---

### Task 12 — Delete with an empty 204 response

Objective:

Remove a resource and communicate success without serializing a response body.

Concept:

`204 No Content` is a complete successful response with no content. Idempotency refers to the intended state effect, so a later `404` does not make repeated deletion non-idempotent.

What to implement:

Add a parameterized repository DELETE returning whether exactly one row was affected, a boolean service method, and a controller `ResponseEntity<Void>`. Return `404` for zero affected rows and empty `204` for a deletion.

Starter code:

```java
public boolean delete(long id) {
    // TODO: DELETE WHERE id = ?, inspect affected-row count
    throw new UnsupportedOperationException("Implement database deletion");
}
```

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable("id") long id) {
    // TODO: validate id, delegate, map false to 404
    // TODO: build no-content response
    throw new UnsupportedOperationException("Implement DELETE");
}
```

Hints:

1. `jdbc.update(...)` returns the number of affected rows.
2. Use `ResponseEntity.noContent().build()`.
3. There is no JSON request body for this deletion contract.

How to verify:

DELETE the created ID using `curl.exe -i`; inspect the `204` and lack of body. GET the same ID: `404`. DELETE it again: `404`. A fresh POST may reuse the deleted ISBN but gets a newly generated ID, not necessarily the old one.

Common mistakes:

- Sending `{"deleted":true}` with a `204` status.
- Ignoring the affected-row count and returning success for every ID despite the stated contract.
- Resetting the identity sequence to keep demo IDs predictable.

Explanation:

Empty responses are intentional HTTP messages. A JSON body is useful only when the contract calls for one.

> **Stop barrier 3:** State the exact contract for each method without opening the controller. Explain why an empty list, an absent item, and a successful deletion have three different response forms.

---

### Task 13 — Make input and media-type failures observable

Objective:

Distinguish route selection, argument conversion, body conversion, and explicit application checks.

Concept:

Not every failure reaches your handler. Wrong methods and media types can fail during routing; malformed JSON and nonnumeric path IDs can fail during conversion; semantically bad values can fail in the controller's small checks.

What to implement:

Audit `consumes`/`produces`, the strict Jackson settings, boxed stock checks, `404` handling, and local duplicate conflict mapping. Create fixtures `missing-stock.json` (`{}`), `unknown-field.json` (`stock` plus an extra title), `fractional-stock.json` (`{"stock":1.5}`), and `malformed.json` (`{"stock": }`). Fill in predictions before sending requests.

Starter code:

```text
Input/action                                      Expected status
PATCH valid ID, JSON body {}, JSON Content-Type    TODO
PATCH valid ID, malformed JSON                     TODO
PATCH valid ID, JSON stock 1.5                     TODO
POST /books, text/plain Content-Type               TODO
PUT /books (collection, not item)                  TODO
GET /books, Accept: application/xml                TODO
GET /books/abc                                     TODO
```

```java
private static void checkStock(Integer stock) {
    // TODO: reject missing/null and negative; allow explicit zero
}
```

Hints:

1. The relevant statuses are `400`, `405`, `406`, and `415`; do not relabel everything `500`.
2. `Content-Type` describes the body sent; `Accept` describes an acceptable response.
3. Check status and state preservation; default error JSON is not your stable schema yet.

How to verify:

Run scenarios 10–15 below. For rejected writes, GET afterward to prove that state did not change. If an error happens before a controller breakpoint, identify which earlier boundary rejected it.

Common mistakes:

- Testing only happy paths and inferring failure behavior.
- Adding `catch (Exception)` and turning broken SQL into a client error.
- Asserting a particular Boot error `message` without configuring that contract.
- Introducing a global advice/error hierarchy before learning the local HTTP behavior.

Explanation:

You now have evidence for which layer owns each failure. This prepares you for the next roadmap topic, where validation and global handling make these decisions more reusable and consistent.

---

### Task 14 — Run the full scenario sequence and packaged server

Objective:

Verify one complete cumulative API, including persistence across application restarts.

Concept:

Compilation proves types fit; HTTP scenarios prove method, path, headers, status, JSON, and database effects agree. A server lifetime spans many requests; database storage spans server processes.

What to implement:

Finish every TODO, execute the manual scenarios in order, record failures, and package the executable JAR. Stop the Maven-run server, start the JAR with the same environment, and reread an undeleted book. Stop the application cleanly and keep the PostgreSQL volume for future practice.

Starter code:

```powershell
# TODO: stop the old server with Ctrl+C before rebuilding or reusing port 8080
mvn clean package
java '-Duser.timezone=UTC' -jar .\target\book-catalog-api-1.0.0.jar
# TODO in terminal B: GET an ID that existed before the application restart
```

```text
Scenario | observed status | key header/body evidence | DB effect | PASS/FAIL
TODO: record each scenario; do not replace observations with predictions
```

Hints:

1. Put the JVM timezone argument before `-jar`.
2. Keep the `DB_*` values in the shell that starts Java.
3. A skipped identity value after a failed duplicate insert is normal; sequence values are not a row count.
4. Restarting the Java process and restarting a database with its named volume should preserve stored rows.

How to verify:

Run all scenarios below. Confirm `mvn clean package` succeeds, the JAR serves the same routes, and an undeleted book retains its values after the restart.

Common mistakes:

- Launching both Maven and the JAR on the same HTTP port.
- Deleting the database volume to hide a schema or behavior bug.
- Calling the build successful while HTTP requests still fail.
- Treating a generated ID gap as a missing or accidentally deleted book.

Explanation:

The deliverable is one coherent HTTP contract backed by the database, with observable behavior from an independent client. Compare against the reference only after recording what your implementation actually did.

## Manual Verification Scenarios

Run these in order after completing the tasks. Keep Java running in terminal A. Use terminal B, in the project directory, for client commands. Record **status, relevant headers, body, and stored-state effect**; an error JSON document alone is not a pass.

Use a fresh practice database if convenient, but do not erase an existing volume just to run these checks. The setup below gives this run distinct ISBNs and authors, so earlier practice rows can remain. A collection need not be empty on a reused database. Keep these variables in terminal B throughout the sequence:

```powershell
$base = 'http://localhost:8080'
$runTag = [DateTime]::UtcNow.ToString('yyyyMMddHHmmssfff')
$isbnA = "A-$runTag"
$isbnB = "B-$runTag"
$authorA = "HTTP Learner $runTag"
$authorB = "Database Learner $runTag"
$createPayload = @{
    isbn = $isbnA
    title = 'Effective Java'
    author = $authorA
    price = 39.90
    stock = 10
} | ConvertTo-Json
```

Successful requests use `Invoke-WebRequest -UseBasicParsing` when capturing status, headers, and a returned ID helps. Expected failures use `curl.exe -i`, because Windows PowerShell 5.1 throws exceptions for HTTP error responses. In the few examples piping a generated payload to curl, `--data-binary '@-'` reads standard input; these payloads deliberately contain only ASCII characters. For non-ASCII input, use a UTF-8 JSON file as in the exercise tasks. Do not add `--fail` here: inspect the printed HTTP status instead of interpreting curl's process exit code as the API outcome.

### Scenario 1 — Database, server, and collection are reachable

Input/action:

```powershell
docker compose ps
docker compose exec postgres psql -U book_app -d book_catalog -c '\d books'
curl.exe -i "$base/books"
```

Expected result:

PostgreSQL is healthy, `books` has the expected columns, and GET returns `200` with a JSON array. The array is `[]` on a fresh database; previous rows are legitimate on a reused volume.

What concept it proves:

The HTTP listener, controller mapping, JDBC query, pool, driver, and database work together. A collection can exist without members.

PASS condition:

A real DB-backed `200` response arrives while terminal A keeps serving.

FAIL symptoms:

Connection refused indicates a listener problem; `404` suggests route/scanning issues; `500` requires inspecting the server/database failure rather than changing the expected status.

### Scenario 2 — POST creates a resource and identifies its location

Input/action:

```powershell
$createResponse = Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/books" `
    -ContentType 'application/json' -Headers @{ Accept = 'application/json' } -Body $createPayload
$createResponse.StatusCode
$createResponse.Headers['Location']
$createResponse.Headers['Content-Type']
$createdBook = $createResponse.Content | ConvertFrom-Json
$createdBook
$bookId = $createdBook.id
if ($null -eq $bookId -or $bookId -le 0) { throw 'Stop: no valid generated ID' }
```

Expected result:

`201`, JSON Content-Type, `Location: /books/{bookId}`, and all six response fields. The five writable values match the submitted values; the ID is generated by the server.

What concept it proves:

Deserialization supplies a request DTO; `ResponseEntity` supplies status and Location; serialization supplies response JSON.

PASS condition:

The status is 201, the Location names the returned positive ID, and the representation contains the intended values. Do not assume that ID is 1.

FAIL symptoms:

`200` indicates missing creation-status handling; an absent Location loses discoverability; `409` means these run identifiers were already used—start a new scenario run rather than guessing another ID.

### Scenario 3 — Follow Location and confirm persistent state

Input/action:

```powershell
curl.exe -i "$base$($createResponse.Headers['Location'])"
docker compose exec postgres psql -U book_app -d book_catalog `
    -c "SELECT id, isbn, title, author, price, stock FROM books WHERE id = $bookId;"
```

Expected result:

GET returns `200` and one object. The database row and public representation agree, including stock 10. This diagnostic SQL uses the server-returned numeric ID; application SQL still binds client values through placeholders.

What concept it proves:

An item representation is distinct from the collection array, and a response is backed by stored state rather than a hardcoded controller value.

PASS condition:

Both reads identify the created row. Compare money numerically; `39.9` and `39.90` represent the same value.

FAIL symptoms:

The response only echoes the input without persistence, Location points at the wrong route, or the row mapper swaps columns.

### Scenario 4 — Filter and page a collection

Input/action:

```powershell
$secondPayload = @{
    isbn = $isbnB
    title = 'SQL Practice'
    author = $authorB
    price = 25.00
    stock = 6
} | ConvertTo-Json
$secondBook = Invoke-RestMethod -Method Post -Uri "$base/books" `
    -ContentType 'application/json' -Body $secondPayload
$secondId = $secondBook.id
$encodedAuthor = [Uri]::EscapeDataString($authorA)
curl.exe -i "$base/books?author=$encodedAuthor&page=0&size=20"
curl.exe -i "$base/books?page=0&size=1"
curl.exe -i "$base/books?page=1&size=1"
curl.exe -i "$base/books?page=1000000&size=100"
```

Expected result:

The author-filtered result contains only book A for this run. The first two unfiltered pages each contain one book with different, ascending IDs, assuming no other client changes the collection during this check. The far-beyond-data page returns `200 []` for this small dataset.

What concept it proves:

Query parameters refine a collection, spaces need URL encoding, and deterministic ordering supports basic pagination. Old rows may occupy the first pages.

PASS condition:

Filtering does not leak unrelated authors, the page size is respected, and the empty page remains a successful array response.

FAIL symptoms:

All rows return regardless of parameters, paging is one-based, or changing pages repeats the same row because OFFSET was ignored.

### Scenario 5 — A uniqueness conflict is 409, not a second book

Input/action:

```powershell
$createPayload | curl.exe -i -X POST "$base/books" -H 'Content-Type: application/json' --data-binary '@-'
$conflictPayload = @{
    isbn = $isbnB
    title = 'Conflicting replacement'
    author = $authorA
    price = 39.90
    stock = 10
} | ConvertTo-Json
$conflictPayload | curl.exe -i -X PUT "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i "$base/books/$bookId"
```

Expected result:

Both writes return `409`: the POST duplicates book A's ISBN; the PUT attempts to give A the ISBN already held by B. Book A still has its original values.

What concept it proves:

A database uniqueness constraint arbitrates conflicts, and the controller translates that known outcome to HTTP. POST itself does not promise retry safety.

PASS condition:

No second row with A's ISBN appears, B remains intact, and rejected PUT does not partially change A. A skipped identity value after the failed insert is normal.

FAIL symptoms:

A duplicate row is inserted, every SQL failure is mislabeled 409, or a deliberate unique-key conflict becomes 500.

### Scenario 6 — Repeated PUT replaces the complete writable state

Input/action:

```powershell
$replacePayload = @{
    isbn = $isbnA
    title = 'Effective Java - Local Catalog Edition'
    author = $authorA
    price = 35.50
    stock = 8
} | ConvertTo-Json
$replacePayload | curl.exe -i -X PUT "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
$replacePayload | curl.exe -i -X PUT "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i "$base/books/$bookId"
```

Expected result:

Both PUTs return `200`. GET returns the same ID and all submitted writable values, including stock 8 and price 35.50.

What concept it proves:

PUT replacement can change state and still be idempotent. A repeated request need not create another resource.

PASS condition:

The second request does not accumulate changes or change identity.

FAIL symptoms:

A new ID appears, stock becomes 16, or some supplied fields are silently ignored.

### Scenario 7 — Partial PUT is not a valid replacement

Input/action:

```powershell
curl.exe -i -X PUT "$base/books/$bookId" -H 'Content-Type: application/json' `
    --data-binary '@requests/stock.json'
curl.exe -i "$base/books/$bookId"
```

Expected result:

`400` for the stock-only PUT; GET still shows the full representation from Scenario 6.

What concept it proves:

The application's full-replacement contract is stricter than merely having syntactically valid JSON.

PASS condition:

The incomplete replacement is rejected without changing the row.

FAIL symptoms:

Stock changes while missing fields are silently retained, or omitted fields become null/defaults in the database.

### Scenario 8 — Repeated PATCH changes only stock

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/stock.json'
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/stock.json'
$afterPatch = Invoke-RestMethod -Uri "$base/books/$bookId"
$afterPatch
```

Expected result:

Both responses are `200`, stock stays 3, and ISBN/title/author/price are unchanged from Scenario 6.

What concept it proves:

This custom patch assigns an absolute value. It is an idempotent operation, although PATCH in general provides no such guarantee.

PASS condition:

Only the stock value changes, and repetition does not add or subtract stock.

FAIL symptoms:

The second request changes stock again, unrelated fields are overwritten, or the implementation claims that Spring automatically applied JSON Merge Patch.

### Scenario 9 — Valid JSON can still contain unacceptable values

Input/action:

```powershell
$badBookPayload = @{
    isbn = $isbnA
    title = ' '
    author = $authorA
    price = 35.50
    stock = 3
} | ConvertTo-Json
$badBookPayload | curl.exe -i -X PUT "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i "$base/books/$bookId"
```

Expected result:

`400` for the blank title; the existing title remains unchanged. Also try negative price, price `1.234`, and price `10000000000.00` individually in an otherwise complete replacement: each is outside this contract.

What concept it proves:

JSON syntax and type conversion are not business-value validation. These few manual checks precede the later validation-framework lesson.

PASS condition:

Each invalid value is rejected as client input and no rejected write alters the book.

FAIL symptoms:

Blank titles are stored, out-of-contract prices are silently rounded, or predictable input mistakes reach SQL and become 500.

### Scenario 10 — Missing, null, negative, and fractional stock are rejected

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/missing-stock.json'
'{"stock":null}' | curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
'{"stock":-1}' | curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/fractional-stock.json'
curl.exe -i "$base/books/$bookId"
```

Expected result:

Four `400` responses; stock remains 3. Missing and explicit null reach the boxed-value check; a fractional number is rejected by the configured conversion policy rather than truncated to an integer.

What concept it proves:

DTO types and converter settings affect the values your handler can observe. Explicit zero is different from missing/null and is valid in this API.

PASS condition:

None of the rejected inputs silently sets stock to zero or truncates 1.5 to 1.

FAIL symptoms:

`{}` is accepted as zero, null causes an unboxing error, or the strict fractional-integer policy is not active.

### Scenario 11 — Unknown fields do not silently become accepted input

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/unknown-field.json'
$withClientId = $createPayload | ConvertFrom-Json
$withClientId | Add-Member -NotePropertyName id -NotePropertyValue 42
$withClientId | ConvertTo-Json | curl.exe -i -X POST "$base/books" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i "$base/books/$bookId"
```

Expected result:

Both writes return `400` during JSON binding. The PATCH DTO allows only stock; the create DTO does not accept a client-supplied ID. The duplicate ISBN in the second request is not reached because its body first fails conversion.

What concept it proves:

Unknown-field rejection is an explicit converter policy supporting a DTO contract, not an automatic REST property.

PASS condition:

Neither request changes stored state.

FAIL symptoms:

Extra fields are ignored despite the strict policy, or the client is allowed to choose a generated database ID.

### Scenario 12 — Malformed JSON fails before business work

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' --data-binary '@requests/malformed.json'
curl.exe -i "$base/books/$bookId"
```

Expected result:

`400`; the existing representation is unchanged. If using a breakpoint, the PATCH handler body is not entered for this unreadable request.

What concept it proves:

Argument construction must succeed before Spring can invoke a handler that requires the request object.

PASS condition:

An HTTP error is observable even without controller execution, and no write occurs.

FAIL symptoms:

An exception is exposed as 500 or the server tries to repair malformed input and performs unintended work.

### Scenario 13 — Request and response media types are separate

Input/action:

```powershell
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: text/plain' -H 'Accept: application/json' `
    --data-binary '@requests/stock.json'
curl.exe -i "$base/books" -H 'Accept: application/xml'
```

Expected result:

The write returns `415` because its declared request type is unsupported. The read returns `406` because the JSON-only route cannot supply XML. Do not require a particular error body shape for the XML preference.

What concept it proves:

`Content-Type` answers what is being sent; `Accept` answers what can be received. `consumes` and `produces` participate in matching.

PASS condition:

The two distinct mismatches produce their distinct statuses.

FAIL symptoms:

Changing Accept is assumed to fix the request's Content-Type, or the media constraints in the reference contract were omitted.

### Scenario 14 — A known path can reject a method

Input/action:

```powershell
curl.exe -i -X PUT "$base/books" -H 'Content-Type: application/json' --data-binary '@requests/replace-book.json'
```

Expected result:

`405` with an `Allow` header including GET and POST. Header ordering is not significant; do not assert an exact ordered string.

What concept it proves:

An operation is selected using method plus route. PUT belongs on the individual-resource path in this contract.

PASS condition:

The response distinguishes an unsupported method from an absent book or an invalid body.

FAIL symptoms:

An unrestricted generic `@RequestMapping` accidentally accepts PUT on the collection.

### Scenario 15 — Invalid identifiers differ from absent resources

Input/action:

```powershell
curl.exe -i "$base/books/not-a-number"
curl.exe -i "$base/books/0"
curl.exe -i "$base/books?page=-1"
curl.exe -i "$base/books?size=101"
$missingId = '9223372036854775807'
curl.exe -i "$base/books/$missingId"
$replacePayload | curl.exe -i -X PUT "$base/books/$missingId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i -X PATCH "$base/books/$missingId" -H 'Content-Type: application/json' --data-binary '@requests/stock.json'
```

Expected result:

The first four requests return `400`. The last three return `404` for a confirmed absent positive ID. The maximum positive `long` is used only as a convenient absent ID for this practice database; if it actually exists in your data, choose and confirm another absent positive ID first.

What concept it proves:

Conversion, input bounds, and lookup absence are separate decisions. This API's PUT does not create missing resources.

PASS condition:

No missing-resource write inserts a row, and none of these expected client outcomes becomes 500.

FAIL symptoms:

Every failure uses the same status, or a missing PUT creates an unexpected row.

### Scenario 16 — DELETE is empty and remains idempotent

Input/action:

```powershell
curl.exe -i -X DELETE "$base/books/$bookId"
curl.exe -i -X DELETE "$base/books/$bookId"
curl.exe -i "$base/books/$bookId"
curl.exe -i "$base/books?author=$encodedAuthor"
```

Expected result:

`204` with no response body, then `404`, then `404`. The author-filtered collection still exists and returns `200 []`.

What concept it proves:

Idempotency concerns intended resource effect, not identical responses. An empty collection and an absent item remain different concepts.

PASS condition:

The first response contains no JSON/message, the resource stays absent, and book B is untouched.

FAIL symptoms:

The 204 contains a serialized value, a second deletion affects another row, or an empty filter result becomes 404.

### Scenario 17 — The packaged server reuses persistent storage

Input/action:

In terminal A, stop the running Java application with Ctrl+C. Keep its `DB_*` environment values, then run:

```powershell
mvn clean package
java '-Duser.timezone=UTC' -jar .\target\book-catalog-api-1.0.0.jar
```

After the server starts, in terminal B:

```powershell
curl.exe -i "$base/books/$secondId"
curl.exe -i "$base/books/$bookId"
```

Expected result:

Book B still returns `200` with its original values; deleted book A still returns `404`. A new Java process does not recreate or reset the database.

What concept it proves:

The executable JAR serves the same API; application lifetime and persisted resource lifetime are different.

PASS condition:

The build succeeds and the restarted server preserves both existence and absence correctly. There are no automated tests in this exercise; a successful Maven test phase alone does not establish these HTTP results.

FAIL symptoms:

Port 8080 is occupied by the old process, host environment values are missing, or startup code unexpectedly clears the table.

Finish by stopping Java, then running `docker compose down` from this project directory if you are done using the database. This retains its named volume and books; do not add `-v` for an ordinary shutdown.

## Stop Before the Complete Solution

Do not scroll further until your API compiles, you have attempted each route, and you can explain the status, headers, body, and database effect of each request. For a failing scenario, record the observed response and the boundary you suspect before comparing code.

## Complete Reference Solution

Every authored project file is below. `.env` is the local copy of `.env.example` with your own practice password; generated `target/` files are not source files. Create the files with your editor at the exact paths shown. The malformed JSON fixture is intentionally invalid.

### `pom.xml`

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
    <version>1.0.0</version>
    <name>book-catalog-api</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
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
```

### `docker-compose.yml`

```yaml
name: book-catalog-api

services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: "${POSTGRES_DB:?Set POSTGRES_DB in .env}"
      POSTGRES_USER: "${POSTGRES_USER:?Set POSTGRES_USER in .env}"
      POSTGRES_PASSWORD: "${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD in .env}"
    ports:
      - "127.0.0.1:${POSTGRES_PORT:-5433}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 10s

volumes:
  postgres_data:
```

### `.env.example`

```dotenv
POSTGRES_DB=book_catalog
POSTGRES_USER=book_app
POSTGRES_PASSWORD=replace-with-a-local-practice-password
POSTGRES_PORT=5433
```

Copy this to `.env` and replace its password. Do not commit `.env`. The same password must be supplied separately as `DB_PASSWORD` to the host process.

### `.gitignore`

```gitignore
.env
/target/
.idea/
*.iml
.vscode/
```

### `database/schema.sql`

```sql
CREATE TABLE IF NOT EXISTS books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE CHECK (btrim(isbn) <> ''),
    title VARCHAR(200) NOT NULL CHECK (btrim(title) <> ''),
    author VARCHAR(120) NOT NULL CHECK (btrim(author) <> ''),
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0)
);
```

### `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: book-catalog-api
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5433/book_catalog}
    username: ${DB_USERNAME:book_app}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1
  sql:
    init:
      mode: never
  jackson:
    deserialization:
      fail-on-unknown-properties: true
      accept-float-as-int: false

server:
  address: 127.0.0.1
  port: 8080
```

Compose initialization owns the schema; Boot SQL initialization is disabled to make that ownership unambiguous. The password placeholder is not a Bean Validation rule or a guarantee of a friendly early missing-secret error. Run the host-environment preflight below. [Boot externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html).

### `src/main/java/com/example/bookcatalog/Application.java`

```java
package com.example.bookcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Do not put `run(...)` inside a try-with-resources block that immediately exits. Unlike the previous one-shot JDBC application, this server must keep its context, server, and pool alive between client requests. Ctrl+C initiates normal application shutdown.

### `src/main/java/com/example/bookcatalog/model/Book.java`

```java
package com.example.bookcatalog.model;

import java.math.BigDecimal;

public record Book(long id, String isbn, String title, String author,
                   BigDecimal price, int stock) {
}
```

### `src/main/java/com/example/bookcatalog/dto/BookRequest.java`

```java
package com.example.bookcatalog.dto;

import java.math.BigDecimal;

public record BookRequest(String isbn, String title, String author,
                          BigDecimal price, Integer stock) {
}
```

### `src/main/java/com/example/bookcatalog/dto/StockRequest.java`

```java
package com.example.bookcatalog.dto;

public record StockRequest(Integer stock) {
}
```

### `src/main/java/com/example/bookcatalog/dto/BookResponse.java`

```java
package com.example.bookcatalog.dto;

import com.example.bookcatalog.model.Book;
import java.math.BigDecimal;

public record BookResponse(long id, String isbn, String title, String author,
                           BigDecimal price, int stock) {
    public static BookResponse from(Book book) {
        return new BookResponse(book.id(), book.isbn(), book.title(),
                book.author(), book.price(), book.stock());
    }
}
```

### `src/main/java/com/example/bookcatalog/repository/BookRepository.java`

```java
package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.Book;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {
    private static final String COLUMNS = "id, isbn, title, author, price, stock";

    private static final RowMapper<Book> ROW_MAPPER = (rs, rowNum) -> new Book(
            rs.getLong("id"),
            rs.getString("isbn"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getBigDecimal("price"),
            rs.getInt("stock"));

    private final JdbcTemplate jdbc;

    public BookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Book> findAll(String author, int size, long offset) {
        if (author == null) {
            return jdbc.query("SELECT " + COLUMNS
                            + " FROM books ORDER BY id ASC LIMIT ? OFFSET ?",
                    ROW_MAPPER, size, offset);
        }
        return jdbc.query("SELECT " + COLUMNS
                        + " FROM books WHERE author = ? ORDER BY id ASC LIMIT ? OFFSET ?",
                ROW_MAPPER, author, size, offset);
    }

    public Optional<Book> findById(long id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM books WHERE id = ?",
                ROW_MAPPER, id).stream().findFirst();
    }

    public Book insert(String isbn, String title, String author,
                       BigDecimal price, int stock) {
        return jdbc.query("INSERT INTO books (isbn, title, author, price, stock)"
                        + " VALUES (?, ?, ?, ?, ?) RETURNING " + COLUMNS,
                ROW_MAPPER, isbn, title, author, price, stock).get(0);
    }

    public Optional<Book> replace(long id, String isbn, String title,
                                  String author, BigDecimal price, int stock) {
        return jdbc.query("UPDATE books SET isbn = ?, title = ?, author = ?,"
                        + " price = ?, stock = ? WHERE id = ? RETURNING " + COLUMNS,
                ROW_MAPPER, isbn, title, author, price, stock, id)
                .stream().findFirst();
    }

    public Optional<Book> setStock(long id, int stock) {
        return jdbc.query("UPDATE books SET stock = ? WHERE id = ? RETURNING " + COLUMNS,
                ROW_MAPPER, stock, id).stream().findFirst();
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM books WHERE id = ?", id) == 1;
    }
}
```

`RETURNING` returns the row from the write itself. An update with no matching ID returns no rows, so the repository can report absence without a separate existence check. A unique constraint arbitrates duplicate ISBNs even if two clients race. Each statement is atomic; this does not provide a client version/ETag lost-update policy. Fixed SQL fragments above are safe to concatenate; user values are always bound to `?` parameters. [PostgreSQL `RETURNING`](https://www.postgresql.org/docs/17/dml-returning.html), [Spring JDBC `JdbcTemplate`](https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html).

### `src/main/java/com/example/bookcatalog/service/BookService.java`

```java
package com.example.bookcatalog.service;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author, int page, int size) {
        return repository.findAll(author, size, (long) page * size);
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book create(String isbn, String title, String author,
                       BigDecimal price, int stock) {
        return repository.insert(isbn, title, author, price, stock);
    }

    public Optional<Book> replace(long id, String isbn, String title,
                                  String author, BigDecimal price, int stock) {
        return repository.replace(id, isbn, title, author, price, stock);
    }

    public Optional<Book> setStock(long id, int stock) {
        return repository.setStock(id, stock);
    }

    public boolean delete(long id) {
        return repository.delete(id);
    }
}
```

The service is intentionally small because the use cases are small. It accepts ordinary Java values and returns domain values, `Optional`, or a boolean; it knows no HTTP status, header, servlet, or request DTO. Do not invent business complexity just to make the class longer. Later lessons can place richer domain rules and transaction boundaries here.

### `src/main/java/com/example/bookcatalog/web/BookController.java`

```java
package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.dto.BookResponse;
import com.example.bookcatalog.dto.StockRequest;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.service.BookService;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BookResponse> findAll(
            @RequestParam(name = "author", required = false) String author,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        if (page < 0 || page > 1_000_000 || size < 1 || size > 100) {
            throw badRequest("page must be 0..1000000 and size must be 1..100");
        }
        String filter = author == null ? null : checkedText(author, "author", 120);
        return service.findAll(filter, page, size).stream()
                .map(BookResponse::from).toList();
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookResponse findById(@PathVariable("id") long id) {
        checkId(id);
        return BookResponse.from(service.findById(id).orElseThrow(BookController::notFound));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponse> create(@RequestBody BookRequest request) {
        checkBook(request);
        try {
            Book book = service.create(request.isbn().trim(), request.title().trim(),
                    request.author().trim(), request.price(), request.stock());
            return ResponseEntity.created(URI.create("/books/" + book.id()))
                    .body(BookResponse.from(book));
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ISBN already exists");
        }
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public BookResponse replace(@PathVariable("id") long id,
                                @RequestBody BookRequest request) {
        checkId(id);
        checkBook(request);
        try {
            Book book = service.replace(id, request.isbn().trim(), request.title().trim(),
                            request.author().trim(), request.price(), request.stock())
                    .orElseThrow(BookController::notFound);
            return BookResponse.from(book);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ISBN already exists");
        }
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE)
    public BookResponse setStock(@PathVariable("id") long id,
                                 @RequestBody StockRequest request) {
        checkId(id);
        checkStock(request.stock());
        Book book = service.setStock(id, request.stock())
                .orElseThrow(BookController::notFound);
        return BookResponse.from(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        checkId(id);
        if (!service.delete(id)) {
            throw notFound();
        }
        return ResponseEntity.noContent().build();
    }

    private static void checkBook(BookRequest request) {
        checkedText(request.isbn(), "isbn", 20);
        checkedText(request.title(), "title", 200);
        checkedText(request.author(), "author", 120);
        BigDecimal price = request.price();
        if (price == null || price.signum() < 0 || price.compareTo(MAX_PRICE) > 0
                || price.stripTrailingZeros().scale() > 2) {
            throw badRequest("price must be nonnegative, fit NUMERIC(12,2), and have at most two decimal places");
        }
        checkStock(request.stock());
    }

    private static String checkedText(String value, String field, int maxLength) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > maxLength) {
            throw badRequest(field + " is required and must fit its maximum length");
        }
        return value.trim();
    }

    private static void checkStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw badRequest("stock is required and must be nonnegative");
        }
    }

    private static void checkId(long id) {
        if (id <= 0) {
            throw badRequest("id must be positive");
        }
    }

    private static ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
    }
}
```

These few local checks keep deliberate client mistakes out of the `500` category. Mapping `DuplicateKeyException` locally is a temporary coupling to Spring data-access errors; later, domain exceptions plus global error handling can improve it. Do not catch every `Exception` and relabel real server failures as `400`. Spring handles malformed bodies and argument conversion failures before the method can run. [Spring MVC request bodies](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html).

### `requests/create-book.json`

```json
{
  "isbn": "9780134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "price": 39.90,
  "stock": 10
}
```

### `requests/replace-book.json`

```json
{
  "isbn": "9780134685991",
  "title": "Effective Java - Local Catalog Edition",
  "author": "Joshua Bloch",
  "price": 35.50,
  "stock": 8
}
```

### `requests/stock.json`

```json
{"stock": 3}
```

### `requests/missing-stock.json`

```json
{}
```

### `requests/unknown-field.json`

```json
{"stock": 3, "title": "This field is not allowed in a stock patch"}
```

### `requests/fractional-stock.json`

```json
{"stock": 1.5}
```

### `requests/malformed.json`

```text
{"stock": }
```

### Run the reference implementation

In **terminal A**, inside `book-catalog-api`, after creating all files:

```powershell
Copy-Item .\.env.example .\.env
notepad .\.env
docker compose config --quiet
docker compose up -d
docker compose ps
docker compose exec postgres psql -U book_app -d book_catalog -c '\d books'

$env:DB_URL = 'jdbc:postgresql://localhost:5433/book_catalog'
$env:DB_USERNAME = 'book_app'
$env:DB_PASSWORD = 'replace-with-the-same-password-as-your-local-env-file'

foreach ($name in @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD')) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
        throw "$name must be set in this terminal before starting Java"
    }
}

mvn clean package
mvn '-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC' spring-boot:run
```

If you already customized `.env`, skip the copy so you do not overwrite your settings. Wait for PostgreSQL to be healthy and for the table query to succeed. The password assignment above is an example placeholder: enter your own local value; avoid sharing terminal history containing it. In another terminal the environment must be set again because process environments are separate.

Keep terminal A running. In **terminal B**, also inside the project directory:

```powershell
$base = 'http://localhost:8080'
curl.exe -i "$base/books"
$created = Invoke-RestMethod -Method Post -Uri "$base/books" `
    -ContentType 'application/json' -InFile '.\requests\create-book.json'
$bookId = $created.id
if ($null -eq $bookId) { throw 'Create failed; inspect the response before continuing' }
curl.exe -i "$base/books/$bookId"
curl.exe -i -X PUT "$base/books/$bookId" -H 'Content-Type: application/json' `
    --data-binary '@requests/replace-book.json'
curl.exe -i -X PATCH "$base/books/$bookId" -H 'Content-Type: application/json' `
    --data-binary '@requests/stock.json'
curl.exe -i -X DELETE "$base/books/$bookId"
```

Run the fuller scenarios above for status/header checks and negative cases. Use `curl.exe`, not PowerShell's historical `curl` alias. Keeping JSON in files avoids Windows native-argument quoting surprises; the quoted `@requests/...` is passed literally to curl so it reads the file.

To verify the executable JAR, stop the Maven-run server with Ctrl+C in terminal A, keep the same `DB_*` environment there, and run:

```powershell
java '-Duser.timezone=UTC' -jar .\target\book-catalog-api-1.0.0.jar
```

The JVM option belongs **before** `-jar`. In terminal B, repeat GET. Do not run both app launch commands at once on port 8080. To stop the database after stopping Java, use `docker compose down`; its named volume persists.

## Architecture Review

| Layer | Knows | Does not own |
|---|---|---|
| Controller | HTTP inputs, DTOs, status, headers | SQL and connection lifecycle |
| Service | Catalog use cases and domain values | HTTP responses and JSON serialization |
| Repository | SQL, row mapping, affected rows | HTTP status and client representations |
| Boot/MVC/Jackson | Server, routing infrastructure, conversion | Your endpoint/business contract |
| Boot/JDBC/HikariCP | JDBC infrastructure and pooled connections | Your SQL/schema meaning |

All request-specific values are method-local. Singleton controllers and services have only immutable shared dependencies, not mutable fields such as `currentBook`, `lastRequest`, or a retained JDBC connection. A new HTTP request does not require a new `DataSource` or pool.

The repository's parameterized SQL and `JdbcTemplate` reuse the JDBC concepts you know. `JdbcTemplate` obtains and releases logical connections through the Boot-managed `DataSource`; Boot closes the pool on application shutdown. No repository calls `new HikariDataSource(...)` or shuts down shared infrastructure.

## Troubleshooting

| Symptom | Boundary to inspect | Corrective action |
|---|---|---|
| Connection refused at `localhost:8080` | Client → HTTP server | Keep terminal A running; inspect startup logs and port |
| Server starts, first database request fails | HTTP server → database | Check container, credentials, URL, and schema; server startup alone need not prove database connectivity |
| Port 8080 already in use | Server binding | Stop your other practice server or choose a different `server.port` and base URL |
| Maven cannot delete the packaged JAR on Windows | Build output is in use | Stop the Java process running that JAR before `mvn clean package`; then restart it |
| Database host port 5433 occupied | Host → container | Change `.env` `POSTGRES_PORT` and the host JDBC URL together |
| Host JVM cannot resolve `postgres` | Network namespace | Use `localhost:5433`; `postgres:5432` is for containers on the Compose network |
| Credentials in `.env` appear correct but Java fails | Process environment | Set `DB_*` in the terminal that starts Maven/Java |
| Missing password does not produce a friendly early message | Placeholder vs validation | Run the preflight; `${DB_PASSWORD}` is not application validation |
| PostgreSQL rejects time zone `Asia/Saigon` | JVM → pgJDBC startup parameters | Use the documented per-process `-Duser.timezone=UTC` setting |
| Relation `books` does not exist | Schema initialization | Check `\d books` and container logs; init scripts run only for a fresh data directory |
| Edited `.env` password does not change DB login | Existing database volume | Existing roles keep their password; align with the initialized role or change it deliberately |
| Every controller route is `404` | Component scanning or URI | Keep `Application` in `com.example.bookcatalog`; include `/books` in URI |
| `400` before a breakpoint in the controller | Binding/JSON conversion | Inspect path types, JSON syntax, unknown properties, and the full request body |
| Missing stock unexpectedly becomes zero | Request DTO type | Use `Integer`, then reject null; a primitive loses the missing-value distinction |
| `415` | Request media type | Supply `Content-Type: application/json` for JSON writes |
| `406` | Response negotiation | Request supported JSON with `Accept: application/json` |
| `405` | Method + path mapping | Use PUT/PATCH/DELETE on `/books/{id}`, POST on `/books` |
| `409` on a repeated create | Unique resource state | Retrieve/use the prior book or choose a new ISBN; do not assume POST retries are harmless |
| PUT silently retains omitted values | Replacement contract | Require all five writable fields; do not implement PUT as a merge |
| PATCH overwrites title/price | Partial-write implementation | Use one `UPDATE books SET stock = ? ... RETURNING ...` statement |
| DELETE sends a JSON body with `204` | Response construction | Return `ResponseEntity.noContent().build()` |
| JSON file not found | Client terminal directory | Run from project root or use an absolute path |
| `curl` behaves unlike examples | PowerShell command resolution | Call `curl.exe` explicitly |
| IDE starts and exits immediately | Application lifecycle | Remove immediate context closing, `System.exit`, or one-shot runner logic |

Useful checks from the project directory:

```powershell
docker compose config --quiet
docker compose ps
docker compose logs --tail 100 postgres
docker compose port postgres 5432
docker compose exec postgres psql -U book_app -d book_catalog -c '\d books'
mvn dependency:tree
```

Plain `docker compose down` retains this project's named database volume. Do not add `-v` during ordinary shutdown: it deletes the named volume and its books. If you deliberately want a fresh disposable database, first confirm the project is `book-catalog-api` and all its data can be lost, then use `docker compose down -v` followed by `docker compose up -d`. The fresh volume reruns initialization; an ordinary restart does not. [PostgreSQL official image initialization](https://hub.docker.com/_/postgres), [Compose down](https://docs.docker.com/reference/cli/docker/compose/down/).

## Concept Coverage / What This Project Proves

| Concept | Observable evidence |
|---|---|
| Request/response lifecycle | Terminal B receives a response while terminal A continues serving |
| Resource collection vs item | `/books` returns an array; `/books/{id}` returns an object |
| Path/query/body/header roles | ID path, author/page/size query, JSON body, media headers |
| HTTP method semantics | Read, create, replace, stock-only change, remove |
| Serialization/deserialization | JSON input becomes request records; response records become JSON |
| DTO boundary | Clients cannot assign `id`; patch accepts only stock |
| Response status/header control | `201` with `Location`, `204` without a body |
| Idempotent intended effects | Repeated PUT/PATCH assignments converge; repeated DELETE leaves absence |
| Error boundary | Invalid JSON `400`, missing resource `404`, duplicate ISBN `409` |
| JDBC continuity | Parameterized SQL, row mapping, one managed `DataSource` |
| Single-statement writes | `RETURNING` supplies the actual created/updated representation |
| Persistent server and storage | Multiple HTTP requests, application restart, database volume reuse |
| Later topics remain later | No global error framework, ORM, authentication, or transaction choreography |

## Final Project Checklist

- [ ] Java 17 and the pinned Boot parent build the executable JAR.
- [ ] The web MVC and JDBC starters plus runtime pgJDBC are the direct dependencies.
- [ ] PostgreSQL is the only Compose service; the application runs on the host.
- [ ] The schema exists before the first HTTP database operation.
- [ ] Compose `.env` and host `DB_*` are configured separately.
- [ ] `Application` stays above the controller/service/repository packages.
- [ ] The application keeps listening after `SpringApplication.run(...)` returns.
- [ ] The controller uses constructor injection and stores no per-request state.
- [ ] Request/response DTOs are separate from `Book`.
- [ ] `Integer` request stock distinguishes missing/null input from explicit zero.
- [ ] Unknown JSON fields and fractional integer input follow the documented strict policy.
- [ ] List/item/create/PUT/PATCH/delete routes implement the stated contract.
- [ ] POST returns `201` and a usable `Location`; DELETE returns empty `204`.
- [ ] Missing resource and empty collection have different responses.
- [ ] Duplicate ISBN produces `409` without creating a second row.
- [ ] PUT requires all writable fields and does not create missing IDs.
- [ ] PATCH assigns stock only, through one atomic SQL statement.
- [ ] User inputs are bound as SQL parameters; ordering is fixed.
- [ ] Paging is bounded and deterministic; it does not claim a total count.
- [ ] Every manual failure scenario was observed, not guessed from the code.
- [ ] The packaged application serves the same contract as the Maven-run application.

## Reflection Questions

1. Which part of `GET /books/10` selects the controller method, and which part becomes a Java argument?
2. Why can `GET /books` return `[]` with `200` while `GET /books/10` returns `404`?
3. Why is a server-generated ID absent from `BookRequest`?
4. What two extra pieces does `ResponseEntity` let POST control beyond the JSON body?
5. What would go wrong if request stock were primitive `int` and no field-presence handling existed?
6. Why is this stock assignment PATCH idempotent even though PATCH in general is not guaranteed to be?
7. Why does repeated DELETE remain idempotent when the second response is `404`?
8. Why can the HTTP server start successfully before a bad database password is discovered?
9. What race does `UPDATE ... RETURNING` avoid compared with checking existence and reading back separately?
10. What concurrency guarantee does a single atomic SQL statement still not provide to a stale client?
11. Why does closing the application context immediately after `run(...)` break this project?
12. Which responsibilities should move or grow in the next validation/global-errors lesson?

## Reflection Answer Key

1. Spring combines the HTTP method with the class and method mapping paths; `{id}` is converted to `long` for `@PathVariable`.
2. An existing collection can contain no elements; an individual-resource URI can identify a book that is absent.
3. The input contract exposes only writable fields. PostgreSQL assigns the identifier and the response exposes it.
4. The status (`201`) and response headers (especially `Location`).
5. Missing stock could become zero, indistinguishable from an explicitly supplied valid zero. Boxed `Integer` allows a null check.
6. It sets stock to a fixed value. Repeating the same assignment has the same intended effect, unlike an increment operation.
7. Idempotency describes intended server-state effect, not identical status codes or response bytes. Both attempts leave the resource absent.
8. MVC/Tomcat infrastructure and a configured `DataSource` can exist before the first actual connection is needed. A successful DB-backed request proves more than the startup banner.
9. It derives absence and the returned row from the write itself, avoiding a separate check/write/read gap. The unique constraint similarly arbitrates simultaneous ISBN inserts.
10. It does not reject a stale client's overwrite; conditional requests, version fields, and optimistic locking are later design tools.
11. The context owns the embedded server and managed pool. Closing it ends the process's ability to keep serving requests.
12. Reusable input validation and centralized, stable error representations. Domain rules can be made reusable without coupling the service to HTTP.

## Official References for Later Lookup

- [Spring Boot servlet web applications](https://docs.spring.io/spring-boot/reference/web/servlet.html)
- [Spring MVC annotated controllers](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html)
- [Spring MVC request mappings](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html)
- [Spring MVC request body conversion](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html)
- [Spring MVC ResponseEntity](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responseentity.html)
- [Spring JDBC core](https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html)
- [PostgreSQL 17 RETURNING](https://www.postgresql.org/docs/17/dml-returning.html)
- [Docker Compose interpolation](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/)
- [HTTP semantics: RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html)
- [PATCH method: RFC 5789](https://www.rfc-editor.org/rfc/rfc5789.html)
