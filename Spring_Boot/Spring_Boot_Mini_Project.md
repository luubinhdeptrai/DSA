# Spring Boot Mini Project

## `book-catalog-boot` — Book Catalog Import and Inventory Summary

**Estimated time:** 4–6 focused hours<br>
**Stack:** Java 17+, Maven 3.6.3+, Spring Boot 4.1.1, Spring JDBC, `javax.sql.DataSource`, HikariCP, pgJDBC, PostgreSQL 17, Docker Compose<br>
**Deliberately excluded:** Spring Web/MVC, REST, Spring Data, JPA, Hibernate, Spring Security, Testcontainers, and a Java application container

This project makes Spring Boot's automation visible without introducing HTTP. Its business behavior is deliberately small: optionally import three books from a classpath CSV file, read them through Spring JDBC, calculate an inventory summary, and print the result.

Only PostgreSQL runs in Docker. The Spring Boot application runs on the Windows host with Maven or as an executable JAR. You already know Spring Core, JDBC, `DataSource`, and HikariCP; this exercise concentrates on who now creates, configures, injects, starts, and closes those objects.

### Quick navigation

- [Target structure and priorities](#target-project-structure)
- [Exercise tasks](#exercise-tasks--attempt-before-reading-the-solution)
- [Manual verification scenarios](#manual-verification-scenarios)
- [Complete reference solution](#complete-reference-solution)
- [Architecture and troubleshooting](#architecture-review)
- [Checklist and reflection](#final-project-checklist)

```text
BookCatalogApplication.main(...)
        ↓ delegates startup to
SpringApplication.run(...)
        ↓ creates and refreshes
ApplicationContext
        ├── component scan → repository/service/runner beans
        ├── external configuration → CatalogProperties
        └── auto-configuration
                 ↓
          DataSource bean (HikariDataSource)
                 ↓
          HikariCP → pgJDBC → localhost:5432
                                     ↓
                              PostgreSQL in Docker

ApplicationRunner executes after startup
        ↓
import → query → summarize → print
        ↓
non-web command finishes
        ↓
ApplicationContext closes → HikariDataSource closes
```

By the end, you should be able to answer:

- What does `SpringApplication.run(...)` create and return?
- Which packages does `@SpringBootApplication` scan by default?
- Which values come from YAML, profiles, environment variables, and command-line arguments?
- Why does Boot create a `HikariDataSource` even though application code declares no pool factory?
- What condition allowed `DataSourceAutoConfiguration` to act, and when would it back off?
- Why do application classes receive dependencies through constructors rather than looking them up?
- Who closes the connection pool when the command-line application ends?
- Why does host-run Java use `localhost`, while a Compose service would use `postgres`?

## Learning contract

Every task follows the same sequence:

1. Read **Objective** and **Concept**.
2. Implement **What to implement** using the incomplete **Starter code**.
3. Open **Hints** only when you need them.
4. Run **How to verify** immediately.
5. Review **Common mistakes** before continuing.
6. Read **Explanation** and connect the result to Spring Boot's startup model.
7. Continue only when you can explain the observed behavior.

Starter code intentionally contains `TODO`s and omits the essential implementation. The complete reference solution is near the end so you can practice before seeing the answers.

Spring Boot logs and Hikari pool counts can vary slightly by environment. Verify relationships and lifecycle events rather than memorizing every line or thread name.

## Version note

The reference solution targets Spring Boot `4.1.1`, whose minimum Java version is 17 and whose Maven support begins at Maven 3.6.3. The `spring-boot-starter-parent` manages compatible Spring Framework, Spring JDBC, HikariCP, pgJDBC, validation, logging, and test dependency versions. Do not copy individual transitive versions into the POM unless you have a specific compatibility reason.

PostgreSQL uses the `postgres:17` image tag. The Java application is not containerized in this exercise. Spring Boot 4 uses Jakarta APIs where applicable, so validation imports come from `jakarta.validation`, while the JDBC `DataSource` contract remains `javax.sql.DataSource` because that type is part of Java SE.

## Target project structure

```text
book-catalog-boot/
├── compose.yaml
├── .env                 # local Compose values; do not commit
├── .env.example         # safe template; commit this
├── .gitignore
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/bookcatalog/
        │       ├── BookCatalogApplication.java
        │       ├── config/
        │       │   └── CatalogProperties.java
        │       ├── diagnostics/
        │       │   └── InfrastructureReporter.java
        │       ├── model/
        │       │   ├── Book.java
        │       │   └── InventorySummary.java
        │       ├── repository/
        │       │   └── BookRepository.java
        │       ├── runner/
        │       │   └── CatalogApplicationRunner.java
        │       └── service/
        │           ├── BookImportService.java
        │           └── InventorySummaryService.java
        └── resources/
            ├── application.yml
            ├── application-dev.yml
            ├── application-audit.yml
            ├── schema.sql
            └── books.csv
```

Package rule:

```text
src/main/java/com/example/bookcatalog/service/BookImportService.java
                                   ↓
package com.example.bookcatalog.service;
```

`BookCatalogApplication` deliberately sits in the root package. Its default component scan therefore reaches every application package below `com.example.bookcatalog`. Resources under `src/main/resources` are copied onto the runtime classpath.

## Project concept priorities

| Practice area | Priority |
|---|---|
| `SpringApplication.run(...)` and the `ApplicationContext` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@SpringBootApplication`, root package, and component scanning | ⭐⭐⭐⭐⭐ MUST KNOW |
| auto-configuration, conditions, and back-off | ⭐⭐⭐⭐⭐ MUST KNOW |
| constructor injection and bean collaboration | ⭐⭐⭐⭐⭐ MUST KNOW |
| externalized configuration and configuration precedence | ⭐⭐⭐⭐⭐ MUST KNOW |
| type-safe, validated `@ConfigurationProperties` | ⭐⭐⭐⭐⭐ MUST KNOW |
| profiles as configuration groups | ⭐⭐⭐⭐ IMPORTANT |
| Boot-created `DataSource`, HikariCP, and `JdbcTemplate` | ⭐⭐⭐⭐ IMPORTANT |
| `ApplicationRunner` startup work | ⭐⭐⭐⭐ IMPORTANT |
| schema initialization with `spring.sql.init.*` | ⭐⭐⭐⭐ IMPORTANT |
| context and pool lifecycle ownership | ⭐⭐⭐⭐ IMPORTANT |
| executable JAR packaging | ⭐⭐⭐⭐ IMPORTANT |
| condition evaluation report and infrastructure diagnostics | ⭐⭐⭐ NICE TO KNOW |
| Actuator, custom auto-configuration, and AOT/native images | ⭐⭐ FUTURE KNOWLEDGE |

---

## Exercise Tasks — Attempt Before Reading the Solution

### Task 1 — Create the project structure

Objective:

Create a conventional Maven/Spring Boot directory layout, root package, focused subpackages, classpath resources, and safe local-configuration files.

Concept:

Spring Boot builds on Maven's normal layout. Java source belongs under `src/main/java`; configuration and data files belong under `src/main/resources`. Package placement matters because the main application class becomes the default base package for component scanning and configuration-property scanning.

What to implement:

Create:

- the `book-catalog-boot` project directory;
- `config`, `diagnostics`, `model`, `repository`, `runner`, and `service` packages beneath `com.example.bookcatalog`;
- `src/main/resources`;
- the files shown in the target structure;
- no `controller` package, Dockerfile, or Java Compose service.

Starter code:

Run this from the directory where you keep practice projects and complete the missing paths:

```powershell
New-Item -ItemType Directory -Force -Path `
  .\book-catalog-boot\src\main\java\com\example\bookcatalog\config, `
  .\book-catalog-boot\src\main\java\com\example\bookcatalog\TODO, `
  .\book-catalog-boot\src\main\java\com\example\bookcatalog\model, `
  .\book-catalog-boot\src\main\java\com\example\bookcatalog\repository, `
  .\book-catalog-boot\src\main\java\com\example\bookcatalog\runner, `
  .\book-catalog-boot\src\main\java\com\example\bookcatalog\service, `
  .\book-catalog-boot\src\main\TODO

Set-Location .\book-catalog-boot

New-Item -ItemType File -Force -Path `
  .\pom.xml, .\compose.yaml, .\.env.example, .\.gitignore

# TODO: create the Java and resource files from the target tree
```

Hints:

1. The missing Java package is `diagnostics`.
2. The missing resource path is `resources` beside the `java` directory.
3. `BookCatalogApplication.java` belongs directly in `com/example/bookcatalog`.
4. Use `New-Item -ItemType File` for empty placeholders.

How to verify:

```powershell
Get-ChildItem -Recurse | Select-Object FullName
```

Expected: every Java file path begins below `src/main/java/com/example/bookcatalog`, and YAML, SQL, and CSV files are below `src/main/resources`.

Common mistakes:

- Creating `src/main/resource` instead of `src/main/resources`.
- Putting the main class inside `runner`, which narrows default scanning to that package.
- Using package names with hyphens.
- Placing `application.yml` beside `pom.xml`.
- Adding web/controller files before the REST stage of the roadmap.

Explanation:

The layout is not Boot-specific magic. Maven establishes source and resource roots; Java packages establish names; Boot uses the main class's package as a sensible scanning convention.

---

### Task 2 — Add the Spring Boot parent and focused starters

Objective:

Create a reproducible Maven build that uses Spring Boot's dependency management and includes only the capabilities this command-line project needs.

Concept:

A starter is a curated dependency descriptor, not a framework layer and not generated code. `spring-boot-starter-jdbc` brings Spring JDBC, JDBC auto-configuration, and HikariCP. The PostgreSQL driver supplies the runtime JDBC implementation. The parent manages compatible versions and configures useful Maven defaults; the Boot Maven plugin can later repackage the normal JAR into an executable archive.

What to implement:

Create `pom.xml` with:

- parent `org.springframework.boot:spring-boot-starter-parent:4.1.1`;
- coordinates `com.example:book-catalog-boot:1.0.0`;
- Java release 17;
- `spring-boot-starter-jdbc`;
- `spring-boot-starter-validation`;
- PostgreSQL with runtime scope;
- `spring-boot-starter-test` with test scope;
- `spring-boot-maven-plugin`;
- no web, REST, Spring Data, JPA, or Hibernate dependency.

Starter code:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>TODO</artifactId>
    <version>TODO</version>
    <relativePath/>
</parent>

<groupId>com.example</groupId>
<artifactId>book-catalog-boot</artifactId>
<version>1.0.0</version>

<properties>
    <java.version>TODO</java.version>
</properties>

<dependencies>
    <!-- TODO: Spring Boot JDBC starter -->
    <!-- TODO: Spring Boot validation starter -->
    <!-- TODO: PostgreSQL runtime driver -->
    <!-- TODO: Spring Boot test starter with test scope -->
</dependencies>

<build>
    <plugins>
        <!-- TODO: Spring Boot Maven plugin -->
    </plugins>
</build>
```

Hints:

1. The parent artifact is `spring-boot-starter-parent`.
2. Do not add versions to starter, driver, or test dependencies; Boot manages them.
3. PostgreSQL's artifact is `org.postgresql:postgresql`.
4. A Maven plugin belongs under `build/plugins`, not `dependencies`.

How to verify:

```powershell
mvn --version
mvn dependency:tree
```

Expected: Maven uses Java 17 or newer; HikariCP and Spring JDBC appear transitively; PostgreSQL has runtime scope; no servlet server, Spring Web, Spring Data JPA, or Hibernate appears.

Common mistakes:

- Using a Boot dependency version different from the parent version.
- Pinning arbitrary HikariCP or Spring Framework versions beside Boot's dependency management.
- Adding `spring-boot-starter-web` because it appears in most tutorials.
- Omitting runtime scope from the PostgreSQL driver.
- Confusing the starter parent with a starter dependency.

Explanation:

The POM declares desired capabilities. Boot's dependency management chooses a tested set of concrete library versions. Nothing has been auto-configured yet; that happens only when the application starts and Boot evaluates the classpath, configuration, and existing beans.

---

### Task 3 — Run PostgreSQL with Docker Compose

Objective:

Declare and start one persistent PostgreSQL service for the host-run Spring Boot application.

Concept:

Docker Compose owns development infrastructure, not the Java process. A named volume persists database files. A loopback-only published port lets the host JVM reach the container through `localhost`. Compose reads `.env` for interpolation, but it does not export those values into the PowerShell process that launches Maven or Java.

What to implement:

Create:

- one service named `postgres` using `postgres:17`;
- required Compose interpolation for database, username, and password;
- `127.0.0.1:${POSTGRES_PORT:-5432}:5432` port publishing;
- a named volume mounted at `/var/lib/postgresql/data`;
- a `pg_isready` health check;
- `.env.example`, a local ignored `.env`, and `.gitignore`;
- no schema bind mount: Boot will run the classpath `schema.sql` later.

Starter code:

```yaml
services:
  postgres:
    image: TODO
    environment:
      POSTGRES_DB: "${POSTGRES_DB:?Set POSTGRES_DB in .env}"
      POSTGRES_USER: TODO
      POSTGRES_PASSWORD: TODO
    ports:
      - "TODO"
    volumes:
      - TODO
    healthcheck:
      test: ["CMD-SHELL", "TODO"]
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 10s

volumes:
  TODO:
```

```dotenv
POSTGRES_DB=book_catalog
POSTGRES_USER=catalog_app
POSTGRES_PASSWORD=TODO-use-a-local-practice-password
POSTGRES_PORT=5432
```

```gitignore
.env
/target/
*.log
# TODO: common IDE metadata
```

Hints:

1. Escape variables evaluated inside the container as `$${POSTGRES_USER}` and `$${POSTGRES_DB}`.
2. Name the volume `book_catalog_data`.
3. Copy `.env.example` to `.env`, then replace only the local password.
4. Do not place credentials directly in `compose.yaml` or commit `.env`.

How to verify:

```powershell
Copy-Item .\.env.example .\.env
notepad .\.env
docker compose config --quiet
docker compose config --services
docker compose up -d
docker compose ps
```

Expected: the only service is `postgres`, and it becomes `healthy`. Confirm the host mapping with:

```powershell
docker compose port postgres 5432
```

Common mistakes:

- Expecting Compose's `.env` to become Java's environment.
- Using `postgres` as the JDBC hostname from a host-run JVM.
- Publishing the database on every host interface unnecessarily.
- Writing `$POSTGRES_USER` instead of `$${POSTGRES_USER}` in the health check.
- Adding the Java application as a second Compose service.

Explanation:

The runtime boundary is now explicit: PostgreSQL is a container; Java remains a host process. `localhost` reaches the published port. A future Java Compose service would normally use the service DNS name `postgres` instead.

---

### Task 4 — Add classpath schema and import data

Objective:

Define an idempotent PostgreSQL schema and a deliberately simple CSV import file on the application classpath.

Concept:

Spring Boot can initialize a `DataSource` from `schema.sql` and `data.sql`. Because PostgreSQL is not an embedded database, the application will later opt in with `spring.sql.init.mode=always`. Unlike the previous plain-Docker exercise, the PostgreSQL image does not run this schema; Boot does, after the `DataSource` exists and before application runners execute.

What to implement:

Create `schema.sql` with a `books` table containing:

- unique, nonblank ISBN;
- nonblank title and author;
- nonnegative `NUMERIC(10,2)` price;
- nonnegative integer stock;
- a non-null import timestamp;
- `CREATE TABLE IF NOT EXISTS` so repeated starts are safe.

Create `books.csv` with a header and three valid rows. Keep values free of commas because this small exercise intentionally does not add a CSV parsing library.

Starter code:

```sql
CREATE TABLE IF NOT EXISTS books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(160) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL,
    last_imported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT books_isbn_not_blank CHECK (TODO),
    CONSTRAINT books_title_not_blank CHECK (TODO),
    CONSTRAINT books_author_not_blank CHECK (TODO),
    CONSTRAINT books_price_nonnegative CHECK (TODO),
    CONSTRAINT books_stock_nonnegative CHECK (TODO)
);
```

```csv
isbn,title,author,price,stock
TODO,Effective Java,Joshua Bloch,45.00,4
TODO,Spring in Action,Craig Walls,52.00,2
TODO,Designing Data-Intensive Applications,Martin Kleppmann,60.00,1
```

Hints:

1. PostgreSQL can reject blank text with `btrim(column) <> ''`.
2. Price and stock constraints compare their columns with zero.
3. Use ISBNs `9780134685991`, `9781617294945`, and `9781492072508`.
4. Do not mount `schema.sql` into Docker; leave it under `src/main/resources`.

How to verify:

```powershell
Get-Content .\src\main\resources\schema.sql
Import-Csv .\src\main\resources\books.csv | Format-Table
```

Expected: PowerShell parses exactly three CSV records, and each has five populated fields. Database schema verification happens after Boot configuration in Task 6.

Common mistakes:

- Saving the files outside `src/main/resources`.
- Using `double precision` for currency.
- Omitting a unique constraint needed by the later upsert.
- Including commas inside fields while using a deliberately minimal parser.
- Assuming the PostgreSQL image will automatically see a classpath resource.

Explanation:

The files are application resources. Maven will copy them into `target/classes` and the executable JAR. Boot can discover the conventional `schema.sql`; application code will explicitly load the configured CSV resource.

---

### Task 5 — Bootstrap the Spring application

Objective:

Create the root application class and delegate startup to `SpringApplication.run(...)`.

Concept:

`@SpringBootApplication` conceptually supplies configuration, component scanning, and auto-configuration. `SpringApplication.run(...)` prepares the environment, creates and refreshes an `ApplicationContext`, registers beans, applies auto-configuration, and invokes runners. The IoC container from Spring Core still exists; Boot orchestrates its setup.

For this finite non-web command, the returned context will be a try-with-resources owner. `ApplicationRunner` beans execute before `run(...)` returns, and closing the context afterward triggers bean destruction, including pool shutdown.

What to implement:

Create `BookCatalogApplication.java` that:

- is in `com.example.bookcatalog`;
- uses `@SpringBootApplication`;
- enables configuration-property scanning;
- calls `SpringApplication.run(BookCatalogApplication.class, args)`;
- closes the returned `ConfigurableApplicationContext` after startup work finishes;
- never constructs a repository, service, `DataSource`, or Hikari pool manually.

Starter code:

```java
package com.example.bookcatalog;

// TODO: imports

@TODO
@TODO
public class BookCatalogApplication {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context =
                     TODO) {
            // ApplicationRunner beans will have completed before this block.
        }
    }
}
```

Hints:

1. Import `org.springframework.boot.autoconfigure.SpringBootApplication`.
2. Import `org.springframework.boot.context.properties.ConfigurationPropertiesScan`.
3. `SpringApplication.run(...)` returns a configurable context.
4. Keep the class above all packages that must be scanned.

How to verify:

```powershell
mvn compile
```

Expected: compilation succeeds. Running is postponed until `application.yml` supplies the database and non-web configuration in Task 6.

Common mistakes:

- Placing the application class in `com.example.bookcatalog.runner`.
- Calling `new AnnotationConfigApplicationContext(...)` as in a manual Spring Core exercise.
- Adding both `@ComponentScan` and the default root-package scan without a reason.
- Creating `HikariDataSource` inside `main`.
- Calling `System.exit(...)` from Maven's JVM.

Explanation:

The one-line `run(...)` call does not replace Spring Core. It creates and prepares the same kind of container you already understand, then adds Boot's environment processing, condition-based auto-configuration, runner invocation, logging, and lifecycle conventions.

---

### Task 6 — Bind and validate external configuration

Objective:

Configure the non-web application, Boot-managed `DataSource`, SQL initialization, pool settings, and a validated group of catalog-specific settings.

Concept:

Spring Boot builds an `Environment` from ordered property sources, then binds values to framework configuration and your own `@ConfigurationProperties` types. `spring.datasource.*` drives `DataSource` auto-configuration; `spring.datasource.hikari.*` configures the chosen Hikari implementation. A record under `catalog.*` gives application settings a typed, validated boundary instead of scattering `@Value` strings.

The YAML describes a pool; it does not instantiate one. Boot's conditions see JDBC, HikariCP, a driver, and no user-defined `DataSource`, then register the infrastructure beans.

What to implement:

Create `application.yml` with:

- application name `book-catalog-boot`;
- `spring.main.web-application-type=none`;
- JDBC settings sourced from `DB_URL`, `DB_USERNAME`, and required `DB_PASSWORD`;
- Hikari pool name, maximum size `4`, minimum idle `1`, and connection timeout `5000` ms;
- `spring.sql.init.mode=always`;
- defaults for report title, low-stock threshold, import toggle, and CSV location.

Create `CatalogProperties` as a validated `@ConfigurationProperties("catalog")` record. Set the three database variables in the PowerShell session that launches Maven.

Starter code:

```yaml
spring:
  application:
    name: TODO
  main:
    web-application-type: TODO
  datasource:
    url: ${DB_URL:TODO}
    username: ${DB_USERNAME:TODO}
    password: ${DB_PASSWORD}
    hikari:
      pool-name: TODO
      maximum-pool-size: TODO
      minimum-idle: TODO
      connection-timeout: TODO
  sql:
    init:
      mode: TODO

catalog:
  report-title: Book Catalog Inventory
  low-stock-threshold: 2
  import-enabled: false
  import-location: TODO
```

```java
package com.example.bookcatalog.config;

// TODO: validation and configuration-property imports

@TODO("catalog")
@TODO
public record CatalogProperties(
        @NotBlank String reportTitle,
        @Min(TODO) int lowStockThreshold,
        boolean importEnabled,
        @NotBlank String importLocation
) {
}
```

```powershell
$env:DB_URL = 'TODO'
$env:DB_USERNAME = 'TODO'
$env:DB_PASSWORD = 'TODO'
```

Hints:

1. The host URL is `jdbc:postgresql://localhost:5432/book_catalog` unless the published host port changed.
2. Validation annotations come from `jakarta.validation.constraints`; use `@Validated` on the record.
3. The CSV location is `classpath:books.csv`.
4. The application must use `always` because PostgreSQL is not an embedded database.
5. Check password presence without printing the value.

How to verify:

```powershell
@('DB_URL', 'DB_USERNAME', 'DB_PASSWORD') | ForEach-Object {
    [pscustomobject]@{
        Name = $_
        IsSet = -not [string]::IsNullOrWhiteSpace(
            [Environment]::GetEnvironmentVariable($_)
        )
    }
}

mvn spring-boot:run
docker compose exec postgres psql -U catalog_app -d book_catalog -c '\d books'
```

Expected: all environment checks are `True`; the application starts as a non-web process, creates the schema, then closes; `psql` describes the `books` table.

Negative check:

```powershell
$savedPassword = $env:DB_PASSWORD
Remove-Item Env:DB_PASSWORD
mvn spring-boot:run
$env:DB_PASSWORD = $savedPassword
```

Expected: startup fails clearly instead of silently using an empty secret.

Common mistakes:

- Assuming `.env` is read by the host JVM.
- Writing `jdbc:postgresql://postgres:5432/...` for host-run Java.
- Using `@Component` plus `@ConfigurationProperties` without understanding registration.
- Forgetting `@ConfigurationPropertiesScan` on the application class.
- Printing a password while debugging binding.
- Leaving SQL initialization at its embedded-database default.

Explanation:

One environment feeds both Boot infrastructure properties and application properties. Binding happens before dependent beans become usable, so invalid configuration prevents a partially configured application from running.

---

### Task 7 — Add profile-specific configuration and test precedence

Objective:

Create named configuration overlays for importing development data and auditing existing data, then override one value from the command line.

Concept:

A profile selects a group of bean definitions and configuration documents for an environment or use case. It is not a substitute for every feature flag. `application.yml` supplies common defaults; `application-dev.yml` and `application-audit.yml` override only their differences.

Command-line options are higher precedence than file-based configuration. Environment variables can also override canonical property names through relaxed binding—for example, `CATALOG_LOWSTOCKTHRESHOLD` maps to `catalog.low-stock-threshold` (dots become underscores; dashes are removed).

What to implement:

Create:

- a `dev` profile that enables CSV import and changes the title;
- an `audit` profile that disables import, uses threshold `0`, and changes the title;
- no hard-coded active profile in `application.yml`;
- verification commands using both profile selection and command-line override.

Starter code:

```yaml
# application-dev.yml
catalog:
  report-title: TODO
  import-enabled: TODO
```

```yaml
# application-audit.yml
catalog:
  report-title: TODO
  low-stock-threshold: TODO
  import-enabled: TODO
```

```powershell
# TODO: activate dev through the Boot Maven plugin
mvn spring-boot:run "-Dspring-boot.run.profiles=TODO"

# TODO: make a command-line property override the dev threshold
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev --catalog.low-stock-threshold=TODO"
```

Hints:

1. Use titles `Development Book Inventory` and `Existing Inventory Audit`.
2. Only `dev` should enable import.
3. The audit threshold is zero.
4. Do not place passwords or host-specific JDBC URLs in profile files.

How to verify:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
mvn spring-boot:run "-Dspring-boot.run.profiles=audit"
```

Expected: each run's startup log names its active profile and exits successfully. Business output arrives after the runner is added in Task 12.

Also run:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev --catalog.low-stock-threshold=-1"
```

Expected: validation prevents startup, proving that the command-line value overrode the valid YAML value.

Common mistakes:

- Naming a file `application_dev.yml` instead of `application-dev.yml`.
- Activating both profiles when their overrides conflict.
- Duplicating all common properties in every profile file.
- Treating a profile as a secure place to store secrets.
- Expecting a lower-precedence YAML value to beat a command-line option.

Explanation:

Profiles layer configuration over a common base; precedence chooses the winning value. The same validated `CatalogProperties` object is produced regardless of which property source supplied that value.

---

### Task 8 — Create immutable domain values

Objective:

Represent a book and an inventory summary with small immutable records that have no Spring, JDBC, or HikariCP responsibilities.

Concept:

Boot manages application wiring, not domain correctness. The `Book` value still validates its own basic invariants, uses `BigDecimal` for money, and remains independent of framework infrastructure. `InventorySummary` carries an already calculated result.

What to implement:

Create `Book` with:

- `isbn`, `title`, `author`, `price`, and `stock` components;
- non-null/nonblank text validation;
- nonnegative price and stock validation.

Create `InventorySummary` with:

- distinct title count;
- total copy count;
- low-stock title count;
- total inventory value.

Starter code:

```java
package com.example.bookcatalog.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Book(
        String isbn,
        String title,
        String author,
        BigDecimal price,
        int stock
) {
    public Book {
        // TODO: reject null or blank text
        // TODO: reject a negative price
        // TODO: reject negative stock
    }
}
```

```java
package com.example.bookcatalog.model;

// TODO: exact decimal import

public record InventorySummary(
        // TODO: distinct titles
        // TODO: total copies
        // TODO: low-stock titles
        // TODO: inventory value
) {
}
```

Hints:

1. `Objects.requireNonNull(value, "name")` establishes null checks.
2. Use `String.isBlank()` for text and `BigDecimal.signum()` for price.
3. Summary counts can be `int`; inventory value must be `BigDecimal`.

How to verify:

```powershell
mvn compile
Get-ChildItem .\target\classes\com\example\bookcatalog\model
```

Expected: both record class files exist. Optionally use JShell or a temporary local check to confirm a negative stock value throws `IllegalArgumentException`.

Common mistakes:

- Using `double` for price or inventory value.
- Annotating simple domain values as Spring components.
- Injecting `DataSource` into a model.
- Accepting whitespace-only ISBN/title/author values.
- Using `BigDecimal.equals(...)` when numerical comparison is intended.

Explanation:

Spring Boot automates infrastructure and assembly; it does not remove the need for purposeful domain types. These records remain ordinary Java and can be constructed or tested without a container.

---

### Task 9 — Implement a repository with Boot's `JdbcTemplate`

Objective:

Persist imported books and read the catalog through an injected, Boot-configured `JdbcTemplate`.

Concept:

In the previous plain JDBC project, you injected `DataSource` and managed connections, statements, and result sets directly. Here, `spring-boot-starter-jdbc` allows Boot to create both the pooled `DataSource` and a `JdbcTemplate` backed by it. The repository still owns SQL and row mapping; `JdbcTemplate` standardizes resource cleanup and translates SQL exceptions into Spring's unchecked data-access hierarchy.

What to implement:

Create `BookRepository` that:

- is discovered as a repository bean;
- receives one `JdbcTemplate` through its constructor;
- upserts a book by unique ISBN and returns the update count;
- reads all books with explicit columns ordered by ISBN;
- maps SQL `NUMERIC` to `BigDecimal`;
- contains no pool construction or stored `Connection` field.

Starter code:

```java
package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@TODO
public class BookRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO books (isbn, title, author, price, stock)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (isbn) DO UPDATE SET
                title = EXCLUDED.title,
                author = EXCLUDED.author,
                price = EXCLUDED.price,
                stock = EXCLUDED.stock,
                last_imported_at = CURRENT_TIMESTAMP
            """;

    private static final String FIND_ALL_SQL = """
            SELECT TODO
            FROM books
            ORDER BY TODO
            """;

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(TODO) {
        this.jdbcTemplate = TODO;
    }

    public int upsert(Book book) {
        return jdbcTemplate.update(
                UPSERT_SQL,
                // TODO: bind all five values in SQL order
        );
    }

    public List<Book> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, (resultSet, rowNumber) ->
                new Book(
                        // TODO: map all five columns
                ));
    }
}
```

Hints:

1. Use `@Repository`; one constructor needs no `@Autowired`.
2. Select `isbn, title, author, price, stock` explicitly.
3. Bind record accessors in the same order as the five placeholders.
4. Use `resultSet.getBigDecimal("price")`.

How to verify:

```powershell
mvn compile
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Expected: the application context creates the repository successfully and exits without a missing-bean error. No rows are imported until Task 12 invokes the import service.

Common mistakes:

- Adding `new JdbcTemplate(...)` inside every method.
- Injecting `HikariDataSource` even though only JDBC operations are needed.
- Using `SELECT *` and positional result-set access.
- Omitting the unique ISBN constraint needed by `ON CONFLICT`.
- Catching and suppressing `DataAccessException` so callers see false success.

Explanation:

Boot supplies a configured infrastructure chain: `JdbcTemplate → DataSource → HikariCP → pgJDBC`. Constructor injection makes that dependency explicit while keeping pool creation and lifecycle outside repository code.

---

### Task 10 — Implement the configurable import service

Objective:

Load a configured classpath resource, parse its rows, and ask the repository to upsert each book only when import is enabled.

Concept:

Business services remain ordinary Spring beans. Boot does not decide how your CSV should be interpreted or when an import is meaningful. The service receives its collaborators through constructor injection: validated settings, a `ResourceLoader`, and the repository. A classpath resource works from both `target/classes` and a packaged executable JAR; a raw source-tree `Path` would not.

What to implement:

Create `BookImportService` that:

- is a service bean;
- returns `0` without opening a resource when import is disabled;
- resolves `catalog.import-location` through `ResourceLoader`;
- reads UTF-8 with try-with-resources;
- skips the header and blank lines;
- requires exactly five comma-separated fields;
- creates `Book` values and sums repository update counts;
- wraps an I/O failure with a useful unchecked exception.

Starter code:

```java
package com.example.bookcatalog.service;

// TODO: application, Spring resource, I/O, decimal, and charset imports

@TODO
public class BookImportService {

    private final CatalogProperties properties;
    private final ResourceLoader resourceLoader;
    private final BookRepository repository;

    public BookImportService(TODO) {
        // TODO: assign constructor dependencies
    }

    public int importConfiguredBooks() {
        if (TODO) {
            return 0;
        }

        Resource resource = resourceLoader.getResource(TODO);
        try (BufferedReader reader = TODO) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(this::parse)
                    .mapToInt(repository::upsert)
                    .sum();
        } catch (IOException exception) {
            throw new IllegalStateException(TODO, exception);
        }
    }

    private Book parse(String line) {
        String[] cells = line.split(",", -1);
        if (cells.length != TODO) {
            throw new IllegalArgumentException("Expected five CSV columns: " + line);
        }
        return new Book(
                // TODO: parse ISBN, title, author, price, and stock
        );
    }
}
```

Hints:

1. Negate `properties.importEnabled()` for the early return.
2. Build the reader from `resource.getInputStream()` and `StandardCharsets.UTF_8`.
3. Trim each cell before conversion.
4. Use `new BigDecimal(cells[3].trim())` and `Integer.parseInt(...)`.
5. Include the configured resource location in the I/O error message, but never include credentials.

How to verify:

```powershell
mvn compile
```

Expected: compilation succeeds. The service is deliberately not called yet; Task 12 adds the startup orchestrator that makes import behavior observable.

Common mistakes:

- Opening `src/main/resources/books.csv` as a filesystem path.
- Forgetting try-with-resources around the reader.
- Using `split(",")`, which drops trailing empty columns.
- Catching malformed numeric data and pretending the row succeeded.
- Making the service obtain beans from `ApplicationContext` instead of constructor injection.

Explanation:

External configuration chooses whether and where to import. The service owns the use case, the repository owns SQL, and Boot owns how those singleton beans are constructed and connected.

---

### Task 11 — Calculate an inventory summary

Objective:

Create a focused service that calculates totals from books already loaded by the repository.

Concept:

Dependency injection does not mean every object must have many dependencies. This stateless service needs no repository and no configuration; it accepts all required input as method parameters. Marking it as a bean lets the runner receive it consistently, while its calculation remains easy to test without starting Spring.

What to implement:

Create `InventorySummaryService` that:

- is a service bean;
- rejects a negative threshold;
- counts distinct titles from list size;
- sums all stock;
- counts books whose stock is less than or equal to the threshold;
- calculates total value as `price × stock` using `BigDecimal`;
- returns an `InventorySummary`.

Starter code:

```java
package com.example.bookcatalog.service;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.model.InventorySummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@TODO
public class InventorySummaryService {

    public InventorySummary summarize(List<Book> books, int lowStockThreshold) {
        if (TODO) {
            throw new IllegalArgumentException("Low-stock threshold must not be negative");
        }

        int totalCopies = books.stream()
                .mapToInt(TODO)
                .sum();

        int lowStockTitles = (int) books.stream()
                .filter(book -> TODO)
                .count();

        BigDecimal inventoryValue = books.stream()
                .map(book -> TODO)
                .reduce(BigDecimal.ZERO, TODO);

        return new InventorySummary(
                TODO,
                totalCopies,
                lowStockTitles,
                inventoryValue
        );
    }
}
```

Hints:

1. Use method reference `Book::stock` for the stock sum.
2. Low stock includes equality with the configured threshold.
3. Multiply each price by `BigDecimal.valueOf(book.stock())`.
4. Reduce with `BigDecimal::add`.

How to verify:

```powershell
mvn compile
```

For the three provided books, calculate the expected result before running the final application: 3 titles, 7 copies, 2 titles at or below threshold 2, and inventory value `344.00`.

Common mistakes:

- Counting total copies as `books.size()`.
- Treating only stock strictly below the threshold as low.
- Converting monetary values to `double` for summation.
- Reading `CatalogProperties` directly inside a pure calculation that can accept a parameter.
- Starting a Spring context merely to test this method.

Explanation:

Boot can create this service, but Boot does not perform its calculation. Keeping the algorithm explicit separates framework assembly from business behavior.

---

### Task 12 — Orchestrate startup with `ApplicationRunner`

Objective:

Run the import, query, summary, and console report after the application context has been fully initialized.

Concept:

An `ApplicationRunner` is a Spring bean that Boot invokes after the context has refreshed. At that point configuration has been bound and validated, the schema initializer has run, and all required collaborators are available. It is a clean entry point for finite command-line work; the main method remains focused on application bootstrap and lifecycle.

What to implement:

Create `CatalogApplicationRunner` that:

- is a component;
- receives properties and three application services/repositories through one constructor;
- prints active profiles from Spring's `Environment`;
- calls import once, then `findAll()`, then the summary service;
- prints every book and a deterministic summary;
- performs no bean lookup and creates no application collaborator with `new`.

Starter code:

```java
package com.example.bookcatalog.runner;

// TODO: application and Spring Boot imports

@TODO
public class CatalogApplicationRunner implements ApplicationRunner {

    private final CatalogProperties properties;
    private final BookImportService importService;
    private final BookRepository repository;
    private final InventorySummaryService summaryService;
    private final Environment environment;

    public CatalogApplicationRunner(TODO) {
        // TODO: assign all constructor dependencies
    }

    @Override
    public void run(ApplicationArguments arguments) {
        int affectedRows = TODO;
        List<Book> books = TODO;
        InventorySummary summary = TODO;

        System.out.println("=== " + properties.reportTitle() + " ===");
        System.out.println("Active profiles: " +
                String.join(", ", environment.getActiveProfiles()));
        System.out.println("Imported/updated rows: " + affectedRows);

        books.forEach(book -> System.out.printf(
                "- %s | %s | %s | %s | %d%n",
                TODO));

        System.out.println("Titles: " + TODO);
        System.out.println("Copies: " + TODO);
        System.out.printf("Low-stock titles (<= %d): %d%n", TODO);
        System.out.println("Inventory value: " + TODO);
    }
}
```

Hints:

1. Import `ApplicationRunner` and `ApplicationArguments` from `org.springframework.boot`.
2. Call `importConfiguredBooks()` before reading all rows.
3. Pass `properties.lowStockThreshold()` to the summary service.
4. Empty active profiles are valid for the default run.
5. Do not catch startup failures merely to print them; let the process fail visibly.

How to verify:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Expected business lines include:

```text
=== Development Book Inventory ===
Active profiles: dev
Imported/updated rows: 3
Titles: 3
Copies: 7
Low-stock titles (<= 2): 2
Inventory value: 344.00
```

Run it a second time. Expected: still 3 titles and 7 copies because ISBN upsert is idempotent; rows are updated rather than duplicated.

Common mistakes:

- Running business work directly before `SpringApplication.run(...)`.
- Adding `@Autowired` to fields instead of using one constructor.
- Querying before import and reporting stale/empty data.
- Catching every exception and allowing Maven to report false success.
- Depending on exact Spring log order rather than the report values.

Explanation:

The runner is an application entry point managed by the container. Boot determines when it runs; your code still determines the use-case order and what constitutes successful output.

---

### Task 13 — Observe auto-configuration, pooling, and back-off

Objective:

Inspect the concrete infrastructure Boot selected and read the condition evidence that explains why it was selected.

Concept:

Auto-configuration is conditional configuration. JDBC classes and HikariCP are present, datasource properties are available, and no application `DataSource` bean exists, so Boot supplies a pooled datasource. HikariCP is preferred when available. Boot also supplies `JdbcTemplate` because a `DataSource` exists.

Back-off means the default configuration yields when you provide an equivalent application bean. `DataSourceAutoConfiguration` uses a missing-bean condition: if you define a `DataSource` or XA datasource, Boot does not create its default pool. This project observes that rule but deliberately keeps Boot's default; manually creating a pool would hide the lesson.

What to implement:

Create `InfrastructureReporter` that:

- depends on `DataSource`, using the interface for its field;
- prints the concrete datasource and logical connection class names;
- safely prints database product/driver names, never URL credentials or passwords;
- when the implementation is Hikari, prints pool name, configured maximum/minimum, and active/idle/total/waiting counts while a connection is borrowed and after it is returned;
- uses try-with-resources for the diagnostic connection.

Inject it into the runner and call it before import. Run once with `--debug` and inspect condition matches.

Starter code:

```java
package com.example.bookcatalog.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@TODO
public class InfrastructureReporter {

    private final DataSource dataSource;

    public InfrastructureReporter(TODO) {
        this.dataSource = TODO;
    }

    public void printSnapshot() {
        System.out.println("DataSource implementation: " + TODO);

        try (Connection connection = TODO) {
            System.out.println("Logical connection class: " + TODO);
            System.out.println("Database product: " + TODO);
            System.out.println("JDBC driver: " + TODO);

            if (dataSource instanceof HikariDataSource hikari) {
                // TODO: print safe configuration and borrowed metrics
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not inspect database infrastructure", exception);
        }

        if (dataSource instanceof HikariDataSource hikari) {
            // TODO: print metrics after the logical handle was returned
        }
    }
}
```

Hints:

1. Use `dataSource.getClass().getName()` and `connection.getClass().getName()`.
2. Database and driver names come from `connection.getMetaData()`.
3. Hikari configuration getters are on `HikariDataSource`; live counts are on `getHikariPoolMXBean()`.
4. Expect active count to include the borrowed diagnostic handle, then decrease after the try block.
5. Keep Hikari-specific code isolated in diagnostics; repository code needs only `JdbcTemplate`.

How to verify:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev --debug" 2>&1 |
    Tee-Object .\boot-debug.log

Select-String -Path .\boot-debug.log -Pattern `
    'DataSourceAutoConfiguration', `
    'JdbcTemplateAutoConfiguration', `
    'HikariDataSource', `
    'After return'
```

Expected: output identifies a Hikari datasource and a Hikari proxy connection. The condition report shows why datasource/JDBC auto-configuration matched. Active connections rise while borrowed and fall after return; exact idle/total counts may vary.

Answer this observation question before continuing: what would happen to Boot's default datasource configuration if the application declared its own `@Bean DataSource`? Expected: the missing-bean condition would fail and Boot would back off.

Common mistakes:

- Treating `DataSource` and HikariCP as synonyms.
- Comparing two proxy objects with `==` to prove physical connection reuse.
- Calling `HikariDataSource.close()` from diagnostics.
- Printing the JDBC URL when it may contain embedded credentials.
- Reading a negative condition as an error; many auto-configurations should not match.
- Defining a custom datasource merely to prove that back-off exists.

Explanation:

You now have evidence for the full chain rather than an assumption: classpath plus properties plus missing application bean caused Boot to create Hikari infrastructure, and the same pool supports `JdbcTemplate` operations.

---

### Task 14 — Verify context and pool lifecycle ownership

Objective:

Prove that closing the application context closes Boot's singleton Hikari pool, while closing a borrowed connection merely returns its logical handle.

Concept:

Spring's container owns the lifecycle of singleton infrastructure beans it creates. `HikariDataSource` has a destruction method, so context shutdown closes its physical pool. Application code should close each borrowed `Connection`, but it should not independently close the shared datasource. In a long-running server, Boot's JVM shutdown hook closes the context; this finite command explicitly closes the returned context when its runner completes.

What to implement:

Update the main method so it:

- stores the returned `ConfigurableApplicationContext`;
- obtains the datasource only for this lifecycle proof;
- closes the context with try-with-resources;
- after closure, conditionally reports `HikariDataSource.isClosed()`;
- does not call `close()` on the pool directly.

Starter code:

```java
public static void main(String[] args) {
    ConfigurableApplicationContext context =
            SpringApplication.run(BookCatalogApplication.class, args);

    DataSource dataSource;
    try (context) {
        dataSource = TODO;
    }

    if (dataSource instanceof HikariDataSource hikari) {
        System.out.println("ApplicationContext active: " + TODO);
        System.out.println("Hikari pool closed by context: " + TODO);
    }
}
```

Hints:

1. Use `context.getBean(DataSource.class)` only at this composition-root diagnostic boundary.
2. After the try block, `context.isActive()` should be false.
3. Use `hikari.isClosed()`; do not invoke `hikari.close()`.
4. `InfrastructureReporter` already proved logical connection return separately.

How to verify:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Expected final evidence:

```text
ApplicationContext active: false
Hikari pool closed by context: true
```

Hikari's shutdown log should appear during context closure. PostgreSQL remains running because Docker Compose has a separate owner.

Common mistakes:

- Closing the datasource inside a repository or runner.
- Assuming `Connection.close()` shuts down HikariCP.
- Assuming context closure stops the PostgreSQL container.
- Adding a second pool solely for diagnostics.
- Using `System.exit(...)` and bypassing understandable Maven output.

Explanation:

The ownership model is now explicit: callers own short-lived logical handles; the application context owns the singleton pool; Docker Compose owns PostgreSQL. Each owner ends only its own resource's lifetime.

---

### Task 15 — Package and run the complete application

Objective:

Build an executable JAR and verify configuration, profile selection, auto-configuration, business behavior, and shutdown together.

Concept:

The Spring Boot Maven plugin repackages the normal application output with its runtime dependencies and a Boot launcher. `java -jar` still starts your main class; the launcher makes the classpath self-contained. External configuration remains external, so the same artifact can run under different profiles and environment values.

What to implement:

Complete all remaining `TODO`s, package the project, and run:

- development import from the executable JAR;
- audit mode without re-importing;
- a command-line threshold override;
- a dependency check proving no web/JPA stack was added;
- clean context/pool shutdown after every successful run.

Starter code:

```powershell
# TODO: package the project (Maven runs any tests you add; this exercise has no test class yet)
mvn TODO

# TODO: run the executable artifact with the dev profile
java -jar .\target\TODO.jar --spring.profiles.active=TODO

# TODO: use audit and override the threshold to 3
java -jar .\target\TODO.jar `
  --spring.profiles.active=TODO `
  --catalog.low-stock-threshold=TODO
```

Hints:

1. Use `mvn clean package`.
2. The artifact is `book-catalog-boot-1.0.0.jar`.
3. Environment variables set in PowerShell are inherited by `java`.
4. Use `mvn dependency:tree` to confirm deliberate exclusions.

How to verify:

```powershell
mvn clean package
java -jar .\target\book-catalog-boot-1.0.0.jar --spring.profiles.active=dev
java -jar .\target\book-catalog-boot-1.0.0.jar `
  --spring.profiles.active=audit `
  --catalog.low-stock-threshold=3
mvn dependency:tree
```

Expected:

- the JAR contains Boot loader entries and application classes;
- dev imports/updates 3 rows and reports value `344.00`;
- audit imports 0 rows but reads the three persisted rows;
- the override changes the displayed threshold to 3 and low-stock count to 2;
- each run ends with context inactive and Hikari closed;
- no web server starts and no JPA/Hibernate dependency appears.

Common mistakes:

- Running the plain original JAR instead of the repackaged Boot JAR.
- Forgetting that the executable process still needs database environment variables.
- Expecting audit mode to create sample data on a fresh empty volume.
- Dockerizing Java and changing the networking model mid-exercise.
- Treating successful compilation as proof of configuration binding and database integration.

Explanation:

One artifact now demonstrates Boot's core value: conventional startup, external configuration, conditional infrastructure, container-managed collaboration, a well-timed application entry point, and lifecycle cleanup—all built on Spring Core and JDBC concepts you already know.

---

## Stop Here and Build Your Version

Complete Tasks 1–15 and run the manual scenarios before revealing the reference implementation. If a scenario fails, diagnose the boundary that failed rather than copying the final code immediately.

## Manual Verification Scenarios

### Scenario 1 — The dependency graph is focused

Run:

```powershell
mvn dependency:tree
```

Verify:

- Boot parent/version is `4.1.1`;
- Spring JDBC, HikariCP, validation, pgJDBC, and Boot test support are present;
- no Spring Web/MVC, servlet server, Spring Data, JPA, or Hibernate dependency appears.

### Scenario 2 — PostgreSQL is external and healthy

Run:

```powershell
docker compose config --quiet
docker compose ps
docker compose exec postgres psql -U catalog_app -d book_catalog -c 'SELECT current_database(), current_user;'
```

Verify one healthy PostgreSQL service. The Java application is absent from Compose.

### Scenario 3 — Boot initializes the schema before runners

Start from a disposable fresh volume only if its data may be deleted, then run the application:

```powershell
docker compose down -v
docker compose up -d
docker compose ps
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Verify that import succeeds on the first application start. This proves `schema.sql` ran before `ApplicationRunner`.

### Scenario 4 — Configuration fails fast and remains secret-safe

Temporarily remove `DB_PASSWORD` from the current process and run the application. Verify startup fails and no password value is printed. Restore the value afterward.

Then run with `--catalog.low-stock-threshold=-1`. Verify Bean Validation rejects the invalid bound properties before business output.

### Scenario 5 — Development import is repeatable

Run the dev profile twice. Verify both runs report three affected upserts, while the database still contains exactly three rows:

```powershell
docker compose exec postgres psql -U catalog_app -d book_catalog -c 'SELECT count(*) FROM books;'
```

### Scenario 6 — Audit reads without importing

After a dev run, execute the audit profile. Verify `Imported/updated rows: 0`, while titles, copies, and inventory value still reflect persisted rows.

### Scenario 7 — Property precedence is observable

Run audit with `--catalog.low-stock-threshold=3`. Verify the title still comes from `application-audit.yml`, but the threshold comes from the higher-precedence command line.

### Scenario 8 — Auto-configuration has evidence

Run with `--debug`. Locate positive matches for datasource/JDBC auto-configuration and the printed Hikari class. Explain why the default pool would back off if an application-defined `DataSource` bean existed.

### Scenario 9 — Borrow, return, and pool shutdown are distinct

Verify the diagnostic connection is active inside try-with-resources and no longer active afterward. Then verify closing the context prints `Hikari pool closed by context: true`.

### Scenario 10 — The JAR is environment-neutral

Run the same JAR once with `dev` and once with `audit`. Verify behavior changes through external configuration without rebuilding the artifact.

---

## Before You Reveal the Solution

You should be able to explain this chain first:

```text
classpath + properties + missing user bean
        ↓ conditions match
Boot registers HikariDataSource and JdbcTemplate
        ↓ constructor injection
repository/service/runner graph becomes usable
        ↓ context refreshed
schema initialized, then ApplicationRunner executes
        ↓ context closes
pool destruction runs
```

If any arrow still feels magical, revisit the corresponding task before comparing code.

---

## Complete Reference Solution

The following files form one complete implementation. Package names, imports, resource paths, property names, and SQL columns are consistent across the project.

### `compose.yaml`

```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: "${POSTGRES_DB:?Set POSTGRES_DB in .env}"
      POSTGRES_USER: "${POSTGRES_USER:?Set POSTGRES_USER in .env}"
      POSTGRES_PASSWORD: "${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD in .env}"
    ports:
      - "127.0.0.1:${POSTGRES_PORT:-5432}:5432"
    volumes:
      - book_catalog_data:/var/lib/postgresql/data
    healthcheck:
      test:
        - CMD-SHELL
        - pg_isready -U "$${POSTGRES_USER}" -d "$${POSTGRES_DB}"
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 10s

volumes:
  book_catalog_data:
```

### `.env.example`

```dotenv
# Docker Compose interpolation only.
# Copy to .env, then replace the password locally.
POSTGRES_DB=book_catalog
POSTGRES_USER=catalog_app
POSTGRES_PASSWORD=TODO-use-a-local-practice-password
POSTGRES_PORT=5432

# Compose does not export DB_URL, DB_USERNAME, or DB_PASSWORD
# into the PowerShell process that launches Java.
```

### `.gitignore`

```gitignore
.env
/target/
*.log
.idea/
.vscode/
*.iml
```

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
    <artifactId>book-catalog-boot</artifactId>
    <version>1.0.0</version>
    <name>book-catalog-boot</name>
    <description>Spring Boot command-line fundamentals practice</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
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
```

### `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: book-catalog-boot
  main:
    web-application-type: none
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/book_catalog}
    username: ${DB_USERNAME:catalog_app}
    password: ${DB_PASSWORD}
    hikari:
      pool-name: book-catalog-pool
      maximum-pool-size: 4
      minimum-idle: 1
      connection-timeout: 5000
  sql:
    init:
      mode: always
      encoding: UTF-8

catalog:
  report-title: Book Catalog Inventory
  low-stock-threshold: 2
  import-enabled: false
  import-location: classpath:books.csv
```

### `src/main/resources/application-dev.yml`

```yaml
catalog:
  report-title: Development Book Inventory
  import-enabled: true
```

### `src/main/resources/application-audit.yml`

```yaml
catalog:
  report-title: Existing Inventory Audit
  low-stock-threshold: 0
  import-enabled: false
```

### `src/main/resources/schema.sql`

```sql
CREATE TABLE IF NOT EXISTS books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(160) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL,
    last_imported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT books_isbn_not_blank CHECK (btrim(isbn) <> ''),
    CONSTRAINT books_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT books_author_not_blank CHECK (btrim(author) <> ''),
    CONSTRAINT books_price_nonnegative CHECK (price >= 0),
    CONSTRAINT books_stock_nonnegative CHECK (stock >= 0)
);
```

### `src/main/resources/books.csv`

```csv
isbn,title,author,price,stock
9780134685991,Effective Java,Joshua Bloch,45.00,4
9781617294945,Spring in Action,Craig Walls,52.00,2
9781492072508,Designing Data-Intensive Applications,Martin Kleppmann,60.00,1
```

### `src/main/java/com/example/bookcatalog/BookCatalogApplication.java`

```java
package com.example.bookcatalog;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BookCatalogApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(BookCatalogApplication.class, args);

        DataSource dataSource;
        try (context) {
            dataSource = context.getBean(DataSource.class);
        }

        if (dataSource instanceof HikariDataSource hikari) {
            System.out.println("ApplicationContext active: " + context.isActive());
            System.out.println("Hikari pool closed by context: " + hikari.isClosed());
        }
    }
}
```

The `DataSource` lookup in `main` exists only to prove lifecycle behavior. Normal business collaborators still use constructor injection. In a long-running service, `main` would normally just call `SpringApplication.run(...)`, and Boot's registered JVM shutdown hook would close the context when the process stops.

### `src/main/java/com/example/bookcatalog/config/CatalogProperties.java`

```java
package com.example.bookcatalog.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("catalog")
@Validated
public record CatalogProperties(
        @NotBlank String reportTitle,
        @Min(0) int lowStockThreshold,
        boolean importEnabled,
        @NotBlank String importLocation
) {
}
```

### `src/main/java/com/example/bookcatalog/model/Book.java`

```java
package com.example.bookcatalog.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Book(
        String isbn,
        String title,
        String author,
        BigDecimal price,
        int stock
) {
    public Book {
        isbn = requireText(isbn, "isbn");
        title = requireText(title, "title");
        author = requireText(author, "author");
        price = Objects.requireNonNull(price, "price");

        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock must not be negative");
        }
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
```

### `src/main/java/com/example/bookcatalog/model/InventorySummary.java`

```java
package com.example.bookcatalog.model;

import java.math.BigDecimal;

public record InventorySummary(
        int distinctTitles,
        int totalCopies,
        int lowStockTitles,
        BigDecimal inventoryValue
) {
}
```

### `src/main/java/com/example/bookcatalog/repository/BookRepository.java`

```java
package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO books (isbn, title, author, price, stock)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (isbn) DO UPDATE SET
                title = EXCLUDED.title,
                author = EXCLUDED.author,
                price = EXCLUDED.price,
                stock = EXCLUDED.stock,
                last_imported_at = CURRENT_TIMESTAMP
            """;

    private static final String FIND_ALL_SQL = """
            SELECT isbn, title, author, price, stock
            FROM books
            ORDER BY isbn
            """;

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int upsert(Book book) {
        return jdbcTemplate.update(
                UPSERT_SQL,
                book.isbn(),
                book.title(),
                book.author(),
                book.price(),
                book.stock()
        );
    }

    public List<Book> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, (resultSet, rowNumber) ->
                new Book(
                        resultSet.getString("isbn"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getBigDecimal("price"),
                        resultSet.getInt("stock")
                ));
    }
}
```

### `src/main/java/com/example/bookcatalog/service/BookImportService.java`

```java
package com.example.bookcatalog.service;

import com.example.bookcatalog.config.CatalogProperties;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
public class BookImportService {

    private final CatalogProperties properties;
    private final ResourceLoader resourceLoader;
    private final BookRepository repository;

    public BookImportService(
            CatalogProperties properties,
            ResourceLoader resourceLoader,
            BookRepository repository
    ) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.repository = repository;
    }

    public int importConfiguredBooks() {
        if (!properties.importEnabled()) {
            return 0;
        }

        Resource resource = resourceLoader.getResource(properties.importLocation());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();

            int affectedRows = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    affectedRows += repository.upsert(parse(line));
                }
            }
            return affectedRows;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read catalog resource: " + properties.importLocation(),
                    exception
            );
        }
    }

    private Book parse(String line) {
        String[] cells = line.split(",", -1);
        if (cells.length != 5) {
            throw new IllegalArgumentException("Expected five CSV columns: " + line);
        }

        return new Book(
                cells[0].trim(),
                cells[1].trim(),
                cells[2].trim(),
                new BigDecimal(cells[3].trim()),
                Integer.parseInt(cells[4].trim())
        );
    }
}
```

### `src/main/java/com/example/bookcatalog/service/InventorySummaryService.java`

```java
package com.example.bookcatalog.service;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.model.InventorySummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventorySummaryService {

    public InventorySummary summarize(List<Book> books, int lowStockThreshold) {
        if (lowStockThreshold < 0) {
            throw new IllegalArgumentException(
                    "Low-stock threshold must not be negative");
        }

        int totalCopies = books.stream()
                .mapToInt(Book::stock)
                .sum();

        int lowStockTitles = (int) books.stream()
                .filter(book -> book.stock() <= lowStockThreshold)
                .count();

        BigDecimal inventoryValue = books.stream()
                .map(book -> book.price().multiply(
                        BigDecimal.valueOf(book.stock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new InventorySummary(
                books.size(),
                totalCopies,
                lowStockTitles,
                inventoryValue
        );
    }
}
```
### `src/main/java/com/example/bookcatalog/diagnostics/InfrastructureReporter.java`

```java
package com.example.bookcatalog.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class InfrastructureReporter {

    private final DataSource dataSource;

    public InfrastructureReporter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void printSnapshot() {
        System.out.println(
                "DataSource implementation: " + dataSource.getClass().getName());

        try (Connection connection = dataSource.getConnection()) {
            System.out.println(
                    "Logical connection class: " + connection.getClass().getName());
            System.out.println(
                    "Database product: " + connection.getMetaData().getDatabaseProductName());
            System.out.println(
                    "JDBC driver: " + connection.getMetaData().getDriverName());

            if (dataSource instanceof HikariDataSource hikari) {
                System.out.printf(
                        "Pool configuration: name=%s, maximum=%d, minimumIdle=%d%n",
                        hikari.getPoolName(),
                        hikari.getMaximumPoolSize(),
                        hikari.getMinimumIdle()
                );
                printPoolMetrics("While borrowed", hikari);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not inspect database infrastructure", exception);
        }

        if (dataSource instanceof HikariDataSource hikari) {
            printPoolMetrics("After return", hikari);
        }
    }

    private static void printPoolMetrics(
            String phase,
            HikariDataSource hikari
    ) {
        HikariPoolMXBean metrics = hikari.getHikariPoolMXBean();
        if (metrics == null) {
            System.out.println(phase + ": pool metrics unavailable");
            return;
        }

        System.out.printf(
                "%s: active=%d, idle=%d, total=%d, waiting=%d%n",
                phase,
                metrics.getActiveConnections(),
                metrics.getIdleConnections(),
                metrics.getTotalConnections(),
                metrics.getThreadsAwaitingConnection()
        );
    }
}
```

### `src/main/java/com/example/bookcatalog/runner/CatalogApplicationRunner.java`

```java
package com.example.bookcatalog.runner;

import com.example.bookcatalog.config.CatalogProperties;
import com.example.bookcatalog.diagnostics.InfrastructureReporter;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.model.InventorySummary;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.service.BookImportService;
import com.example.bookcatalog.service.InventorySummaryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogApplicationRunner implements ApplicationRunner {

    private final CatalogProperties properties;
    private final BookImportService importService;
    private final BookRepository repository;
    private final InventorySummaryService summaryService;
    private final InfrastructureReporter infrastructureReporter;
    private final Environment environment;

    public CatalogApplicationRunner(
            CatalogProperties properties,
            BookImportService importService,
            BookRepository repository,
            InventorySummaryService summaryService,
            InfrastructureReporter infrastructureReporter,
            Environment environment
    ) {
        this.properties = properties;
        this.importService = importService;
        this.repository = repository;
        this.summaryService = summaryService;
        this.infrastructureReporter = infrastructureReporter;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        infrastructureReporter.printSnapshot();

        int affectedRows = importService.importConfiguredBooks();
        List<Book> books = repository.findAll();
        InventorySummary summary = summaryService.summarize(
                books, properties.lowStockThreshold());

        String[] activeProfiles = environment.getActiveProfiles();
        String profileText = activeProfiles.length == 0
                ? "(default)"
                : String.join(", ", activeProfiles);

        System.out.println("=== " + properties.reportTitle() + " ===");
        System.out.println("Active profiles: " + profileText);
        System.out.println("Imported/updated rows: " + affectedRows);

        books.forEach(book -> System.out.printf(
                "- %s | %s | %s | %s | %d%n",
                book.isbn(),
                book.title(),
                book.author(),
                book.price(),
                book.stock()
        ));

        System.out.println("Titles: " + summary.distinctTitles());
        System.out.println("Copies: " + summary.totalCopies());
        System.out.printf(
                "Low-stock titles (<= %d): %d%n",
                properties.lowStockThreshold(),
                summary.lowStockTitles()
        );
        System.out.println("Inventory value: " + summary.inventoryValue());
    }
}
```

### Run the solution

From `book-catalog-boot`:

```powershell
Copy-Item .\.env.example .\.env
notepad .\.env
docker compose up -d
docker compose ps

$env:DB_URL = 'jdbc:postgresql://localhost:5432/book_catalog'
$env:DB_USERNAME = 'catalog_app'
$env:DB_PASSWORD = 'use-the-same-local-password-as-.env'

mvn clean package
java -jar .\target\book-catalog-boot-1.0.0.jar `
  --spring.profiles.active=dev
```

Representative business and diagnostic evidence:

```text
DataSource implementation: com.zaxxer.hikari.HikariDataSource
Logical connection class: com.zaxxer.hikari.pool.HikariProxyConnection
Database product: PostgreSQL
While borrowed: active=1, idle=..., total=..., waiting=0
After return: active=0, idle=..., total=..., waiting=0
=== Development Book Inventory ===
Active profiles: dev
Imported/updated rows: 3
Titles: 3
Copies: 7
Low-stock titles (<= 2): 2
Inventory value: 344.00
ApplicationContext active: false
Hikari pool closed by context: true
```

Ellipses represent timing-dependent pool counts, not output you should literally print. Book detail rows and normal Boot/Hikari logs also appear.

---

## Application Execution — What Happens

```text
1. JVM enters BookCatalogApplication.main(args)
2. SpringApplication prepares Environment property sources
3. SpringApplication creates an ApplicationContext
4. @SpringBootApplication supplies configuration and component scanning
5. @ConfigurationPropertiesScan registers CatalogProperties binding
6. Auto-configuration evaluates classpath, properties, and existing beans
7. Boot creates one HikariDataSource and one JdbcTemplate
8. Boot runs schema.sql against that DataSource
9. Spring creates repository, services, diagnostics, and runner
10. ApplicationRunner borrows/returns a diagnostic connection
11. Dev mode imports/upserts books through JdbcTemplate
12. Repository reads books; service calculates; runner prints
13. SpringApplication.run(...) returns the refreshed context
14. main closes the context
15. Context destruction closes HikariDataSource
16. Java process exits; PostgreSQL container and volume remain
```

The key timing point is step 8 before step 10: database initialization is a Boot-managed startup dependency, so the runner can rely on the table being present.

## Architecture Review

### Runtime and dependency view

```text
External property sources
  ├── application.yml
  ├── application-{profile}.yml
  ├── OS environment
  └── command-line options
            ↓ ordered into
      Spring Environment
        ├── binds → CatalogProperties
        └── configures → DataSource auto-configuration
                              ↓
ApplicationContext      HikariDataSource → HikariCP → pgJDBC → PostgreSQL
  ├── BookRepository ← JdbcTemplate ───────────┘
  ├── BookImportService ← repository + properties + ResourceLoader
  ├── InventorySummaryService
  ├── InfrastructureReporter ← DataSource
  └── CatalogApplicationRunner ← all use-case collaborators
```

Spring Boot supplies orchestration and sensible defaults. Spring Core still owns bean definitions, dependency resolution, singleton creation, and destruction. Spring JDBC still performs JDBC work. HikariCP still owns physical connections. PostgreSQL still enforces SQL constraints.

### Component scanning view

```text
com.example.bookcatalog.BookCatalogApplication
        ↓ default scan base package
com.example.bookcatalog.*
        ├── config
        ├── diagnostics
        ├── repository
        ├── runner
        └── service
```

Moving the application class to `com.example.bookcatalog.runner` would normally scan that package and its children, not sibling packages such as `repository` and `service`. The resulting missing-bean errors are package-structure failures, not constructor-injection failures.

### Auto-configuration decision view

```text
Is JDBC infrastructure on the classpath? ── no ──> no JDBC auto-configuration
                 │ yes
                 ↓
Is a JDBC URL/driver available? ─────────── no ──> startup cannot configure database
                 │ yes
                 ↓
Does the application already define DataSource/XADataSource?
        │ yes                                  │ no
        ↓                                      ↓
Boot backs off                         pooled datasource candidates
                                               ↓
                                     HikariCP is available/preferred
                                               ↓
                                      HikariDataSource bean
```

Back-off is cooperation, not failure. Auto-configuration fills a missing application capability and yields when the application supplies that capability explicitly.

### Configuration precedence used here

From lower to higher precedence for the sources exercised by this project:

```text
application.yml
      ↓ overridden by
application-{active-profile}.yml
      ↓ overridden by
OS environment variables
      ↓ overridden by
command-line --property=value options
```

Spring Boot has additional property-source categories, but this smaller ordering is enough for the exercise. Do not confuse source precedence with bean creation order.

### Resource and lifecycle ownership

| Resource | Owner | End of lifetime |
|---|---|---|
| `ApplicationContext` | `BookCatalogApplication.main` in this finite command | End of runner work / context close |
| `CatalogProperties` | Spring container | Context close |
| Repository/service/runner singletons | Spring container | Context close |
| `JdbcTemplate` | Spring container | Context close |
| `HikariDataSource` | Spring container | Context close calls its destruction method |
| Logical JDBC connection | Code that borrows it, or `JdbcTemplate` for its operation | End of try-with-resources/operation |
| Physical PostgreSQL sessions | HikariCP | Retirement, failure, or pool shutdown |
| PostgreSQL container | Docker Compose | `docker compose down` |
| PostgreSQL data | Named volume | Explicit volume deletion |

Neither repository nor runner owns the singleton pool. Neither the context nor pool owns the Docker container.

### Plain configuration versus Boot configuration

```text
Plain JDBC/Hikari exercise              This Spring Boot exercise
--------------------------              -------------------------
read System.getenv manually             Environment + property binding
validate strings manually               @ConfigurationProperties + validation
new HikariConfig                         spring.datasource.* properties
new HikariDataSource                     DataSourceAutoConfiguration bean
new repository/service                   component scan + constructor injection
call application workflow in main        ApplicationRunner
close pool in main                        close ApplicationContext
Exec Maven plugin                         Boot Maven plugin/executable JAR
```

Boot automates repeatable infrastructure and assembly. It does not write SQL, choose business rules, validate every domain invariant, decide import order, or diagnose a broken network for you.

---

## Troubleshooting

Start at the earliest failed boundary:

```text
Maven/classpath
      ↓
Spring Environment and profile
      ↓
component/configuration-property scan
      ↓
auto-configuration conditions
      ↓
host port → PostgreSQL container
      ↓
schema initialization
      ↓
runner/business behavior
      ↓
context shutdown
```

| Symptom | Likely cause | Inspect/correct |
|---|---|---|
| `mvn` is not recognized | Maven missing or absent from `PATH` | Run `mvn --version`; install/configure Maven 3.6.3+ |
| Java release is unsupported | Maven is using an older JDK | Inspect `mvn --version` and `JAVA_HOME`; use Java 17+ |
| Boot parent cannot resolve | Wrong version or repository/network problem | Confirm `4.1.1`, Maven Central connectivity, and no unnecessary repositories |
| Application starts a web server | Web starter was added or web type changed | Remove web dependencies; retain `spring.main.web-application-type=none` |
| Docker configuration rejects missing values | `.env` missing/incomplete | Copy `.env.example`, set all `POSTGRES_*`, validate with `docker compose config` |
| PostgreSQL never becomes healthy | Initialization/configuration failed | Inspect `docker compose ps` and `docker compose logs --tail 100 postgres` |
| Host port already in use | Another server owns 5432 | Use `POSTGRES_PORT=5433` and change host `DB_URL` to port 5433 |
| `Connection refused` | Container stopped, wrong port, or wrong host | Verify service health and `docker compose port postgres 5432` |
| Unknown host `postgres` | Host JVM tried to use Compose DNS | Use `localhost`; `postgres` is for peers on the Compose network |
| Java cannot see Compose `.env` | Separate process environments | Set `DB_*` in the same PowerShell window that launches Maven/Java |
| Password authentication failed | Host and container credentials differ, or existing volume retained old credentials | Align values; changing `.env` does not rewrite an existing database role |
| `Could not resolve placeholder 'DB_PASSWORD'` | Required host variable missing | Set it without printing it |
| `CatalogProperties` bean is missing | Property scanning not enabled or package moved | Check `@ConfigurationPropertiesScan` and root application package |
| Repository/service bean is missing | Component outside scan tree or annotation absent | Compare package paths and main class location |
| `relation "books" does not exist` | SQL init disabled, wrong database, or resource missing | Check `spring.sql.init.mode=always`, `schema.sql`, URL, and init logs |
| Edited schema does not alter an existing table | `CREATE TABLE IF NOT EXISTS` is not a migration | Apply explicit SQL or reset only disposable practice data |
| Import reports zero in dev | Dev profile inactive or import property overridden | Print active profiles and inspect winning `catalog.import-enabled` value |
| CSV row has wrong column count | Comma inside a field or malformed row | Keep sample fields comma-free or use a real CSV library later |
| Duplicate books appear | Unique constraint/upsert missing | Inspect ISBN constraint and `ON CONFLICT (isbn)` |
| Condition report is enormous | `--debug` lists matching and nonmatching configurations | Search for the specific datasource/JDBC classes; remove debug afterward |
| Pool counts differ from sample | Pool population is timing-dependent | Verify active rises while borrowed and falls after return |
| Pool stays open | Context was not closed or process remains alive | Inspect main lifecycle and Hikari shutdown logs |
| Audit shows zero titles | No earlier dev import or volume was reset | Run dev once; audit deliberately does not seed data |
| JAR lacks dependencies | Boot Maven plugin did not repackage | Inspect POM and `mvn clean package` output |

Useful diagnostic sequence:

```powershell
mvn --version
mvn dependency:tree
docker compose config --quiet
docker compose ps
docker compose logs --tail 100 postgres
docker compose port postgres 5432
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev --debug"
```

### Fresh-volume reset

> **Destructive warning:** `docker compose down -v` deletes this project's named PostgreSQL volume and every imported row. Run it only from the disposable `book-catalog-boot` directory after confirming its data can be lost.

```powershell
docker compose down -v
docker compose up -d
docker compose ps
```

| Command | Container/network | Named database volume |
|---|---|---|
| `docker compose down` | Removed | Kept |
| `docker compose down -v` | Removed | **Deleted** |

Boot's `schema.sql` is suitable for this exercise, not a replacement for production database migrations.

---

## Concept Coverage / What This Project Proves

“Direct” means you implement or observe it here. “Conceptual/future” means the guide explains the boundary but intentionally does not add another subsystem.

| Concept | Project evidence | Coverage |
|---|---|---|
| Spring Boot builds on Spring Core | Real `ApplicationContext`, beans, DI, lifecycle | Direct |
| `@SpringBootApplication` | Root application configuration and scan | Direct |
| `SpringApplication.run(...)` | Environment-to-context startup and returned context | Direct |
| Component scanning | Repository/service/component discovery | Direct |
| Constructor injection | All application bean collaboration | Direct |
| Starter/dependency management | Parent and focused JDBC/validation starters | Direct |
| Externalized configuration | YAML, environment, and command-line values | Direct |
| Type-safe property binding | `CatalogProperties` record | Direct |
| Configuration validation | `@NotBlank` and `@Min` startup failure | Direct |
| Profiles | Dev import versus audit behavior | Direct |
| Property precedence | Command-line threshold overrides profile YAML | Direct |
| JDBC auto-configuration | Boot-created `DataSource` and `JdbcTemplate` | Direct |
| HikariCP selection | Concrete class and safe pool diagnostics | Direct |
| Conditions and back-off | Debug report plus missing-bean explanation | Direct observation/concept |
| SQL initialization | PostgreSQL `schema.sql` before runner | Direct |
| `ApplicationRunner` | Startup use-case orchestration | Direct |
| Classpath resources | CSV works from classes and executable JAR | Direct |
| Exception visibility | Binding, I/O, SQL, and runner failures fail startup | Direct |
| Context/pool lifecycle | Context close leads to `isClosed() == true` | Direct |
| Executable JAR | Boot Maven plugin and `java -jar` | Direct |
| Host-versus-container networking | `localhost` versus `postgres` | Direct |
| Actuator | Useful later for health/metrics; no HTTP endpoint in this finite app | Future |
| REST/MVC and embedded server | Deliberately absent before REST stage | Future |
| Spring Data JPA/Hibernate | Deliberately absent; Spring JDBC keeps automation visible | Future |
| Declarative transactions | No multi-step write use case here | Future |
| Custom auto-configuration | Application consumes conditions but does not publish a starter | Future |
| AOT/native images | Not needed for the startup mental model | Future |

---

## Final Project Checklist

- [ ] Maven is 3.6.3+ and runs on Java 17 or newer.
- [ ] The Boot parent is exactly `4.1.1`.
- [ ] Boot manages dependency/plugin versions where appropriate.
- [ ] JDBC, validation, pgJDBC, and test support are the only focused dependencies.
- [ ] No web, REST, Spring Data, JPA, Hibernate, or Security dependency exists.
- [ ] `BookCatalogApplication` is in the root package.
- [ ] `@SpringBootApplication` and `@ConfigurationPropertiesScan` are present.
- [ ] `main` delegates startup to `SpringApplication.run(...)`.
- [ ] `main` never creates a datasource, repository, or service.
- [ ] PostgreSQL is the only Compose service.
- [ ] Java runs on the host and connects through `localhost`.
- [ ] `.env` is ignored and contains only local Compose values.
- [ ] I understand why Compose `.env` does not populate the JVM environment.
- [ ] Password values are required and never printed.
- [ ] `schema.sql` is a classpath resource and is not also mounted into Docker.
- [ ] `spring.sql.init.mode=always` enables PostgreSQL initialization.
- [ ] `CatalogProperties` binds a coherent prefix and validates at startup.
- [ ] Common configuration is in `application.yml`.
- [ ] Profile files override only their differences.
- [ ] No active profile is hard-coded in the common file.
- [ ] Command-line configuration overrides profile YAML in the experiment.
- [ ] Domain records have no Spring/JDBC infrastructure dependency.
- [ ] `BookRepository` receives `JdbcTemplate` through its constructor.
- [ ] Repository SQL uses explicit columns and an idempotent ISBN upsert.
- [ ] Import uses a classpath resource and closes its reader.
- [ ] Import-disabled mode does not open the resource.
- [ ] The summary uses `BigDecimal` for monetary calculation.
- [ ] `ApplicationRunner` runs import, read, summarize, and print in order.
- [ ] Failures propagate instead of being converted to false success.
- [ ] Diagnostics depend on `DataSource`, with Hikari details isolated.
- [ ] A diagnostic logical connection is returned with try-with-resources.
- [ ] I verify pool-count relationships rather than exact timing-dependent values.
- [ ] The condition report explains why datasource/JDBC configuration matched.
- [ ] I can explain when datasource auto-configuration backs off.
- [ ] The dev profile imports exactly three unique ISBNs repeatedly.
- [ ] The audit profile imports zero rows and reads persisted data.
- [ ] The expected summary is 3 titles, 7 copies, and value `344.00`.
- [ ] Context closure makes `HikariDataSource.isClosed()` true.
- [ ] Context closure does not stop PostgreSQL or delete its volume.
- [ ] `mvn clean package` creates an executable Boot JAR.
- [ ] The same JAR changes behavior through profiles without rebuilding.

---

## Reflection Questions

Answer without looking at the key.

1. Does Spring Boot replace the Spring IoC container?
2. What three broad responsibilities does `@SpringBootApplication` combine?
3. Why is the main application class placed above the other packages?
4. What major work occurs inside `SpringApplication.run(...)`?
5. What is the difference between a starter and auto-configuration?
6. Which evidence caused Boot to configure a Hikari datasource here?
7. What does auto-configuration back-off mean?
8. Why does the repository receive `JdbcTemplate` rather than create it?
9. Where did the `JdbcTemplate` bean come from?
10. Why is `DataSource` still `javax.sql.DataSource` in Spring Boot 4?
11. Why is PostgreSQL SQL initialization set to `always`?
12. When does `ApplicationRunner` run relative to context refresh and `SpringApplication.run(...)` returning?
13. Why use `@ConfigurationProperties` rather than many unrelated `@Value` fields?
14. What happens when a command-line option and profile YAML define the same property?
15. Why does Compose reading `.env` not make `DB_PASSWORD` visible to Java?
16. Why does host-run Java use `localhost` instead of `postgres`?
17. What is the lifecycle difference between `Connection.close()` and context close?
18. Why should pool diagnostics compare count relationships instead of exact counts?
19. Why does the same executable JAR work under both dev and audit behavior?
20. Name three important things Boot still cannot decide for this application.

---

## Reflection Answer Key

1. No. Boot creates and configures an `ApplicationContext`; Spring Core still performs bean registration, creation, injection, and lifecycle management.
2. Application configuration, component scanning, and enabling Boot auto-configuration.
3. Default scanning starts from that package and includes subpackages; putting it too low can miss sibling components.
4. It prepares the environment, chooses/creates a context, registers configuration, refreshes the container, applies auto-configuration, invokes runners, and returns the context.
5. A starter contributes a curated dependency set. Auto-configuration is conditional bean configuration evaluated at runtime.
6. JDBC/Hikari classes and pgJDBC were present, datasource properties were supplied, and no application datasource bean existed.
7. Boot's default configuration yields when the application already provides the capability, such as a `DataSource` bean.
8. Constructor injection declares the dependency and preserves one container-managed infrastructure graph and lifecycle.
9. JDBC auto-configuration created it after a `DataSource` bean existed.
10. `javax.sql.DataSource` belongs to Java SE's JDBC API; Jakarta namespace migration applies to Jakarta EE APIs, not this Java SE type.
11. Boot initializes embedded databases by default; this exercise explicitly enables script initialization for external PostgreSQL.
12. It runs after the context is refreshed and before `run(...)` returns successfully.
13. It groups related settings, binds them type-safely, supports validation, and gives consumers one coherent dependency.
14. The command-line option wins because it has higher precedence than file-based configuration.
15. Compose loads `.env` in its own process for interpolation; it cannot modify the parent shell or an independently launched JVM.
16. The JVM reaches the container through a published host port. The service name is DNS available to containers on the Compose network.
17. Closing a borrowed logical connection returns it to Hikari; closing the context destroys the singleton datasource and shuts down the pool.
18. Pool creation and idle population are asynchronous/timing-dependent; the borrow/return relationship is the stable invariant.
19. Profiles and other property sources are read at runtime, outside the compiled artifact.
20. Examples include SQL design, domain invariants, CSV meaning, use-case order, acceptable error behavior, and production pool sizing.

---

## Final Mental Model

```text
Maven declares capabilities.
        ↓
SpringApplication builds an Environment and ApplicationContext.
        ↓
@SpringBootApplication establishes the scan and enables conditions.
        ↓
Boot sees what is present, what is configured, and what is missing.
        ↓
Auto-configuration supplies HikariDataSource and JdbcTemplate.
        ↓
Spring Core creates and constructor-injects application beans.
        ↓
ApplicationRunner executes the finite use case.
        ↓
The context closes its beans; the pool closes with it.
        ↓
Docker Compose continues to own PostgreSQL separately.
```

Spring Boot is not a second container and not a replacement for Spring Core. It is an opinionated startup, configuration, and production-support layer that uses the Spring container you already understand.

## Official references for later lookup

- [Spring Boot 4.1 system requirements](https://docs.spring.io/spring-boot/4.1/system-requirements.html)
- [Spring Boot reference documentation](https://docs.spring.io/spring-boot/reference/)
- [`@SpringBootApplication`](https://docs.spring.io/spring-boot/reference/using/using-the-springbootapplication-annotation.html)
- [Externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html)
- [SQL datasource and initialization](https://docs.spring.io/spring-boot/reference/data/sql.html)
- [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/maven-plugin/)
- [Docker Compose interpolation](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/)
- [PostgreSQL Docker Official Image](https://hub.docker.com/_/postgres)
