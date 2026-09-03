# Spring Boot Essentials

> A focused pre-REST guide for a Java developer who already understands Maven, JDBC, `DataSource`, HikariCP, PostgreSQL, and Spring Core.

**Target:** Spring Boot 4.1.1, Java 17+  
**Goal:** Understand what Boot automates before using that automation in a REST application.  
**Deliberately postponed:** controllers, HTTP request mapping, REST API design, request validation, global exception handling, JPA/Hibernate, Security, deployment, and advanced framework internals.

Spring Boot is easiest to understand when it is treated as an automation layer over concepts you already know. The `ApplicationContext`, bean definitions, dependency injection, lifecycle callbacks, JDBC driver, `DataSource`, connection pool, and database do not disappear. Boot supplies sensible defaults and connects those pieces when its conditions are satisfied.

### Quick navigation

- [Boot and Spring Core](#1-what-problem-spring-boot-solves)
- [Startup and the application class](#3-springbootapplication)
- [Starters and auto-configuration](#5-starters-and-dependency-management)
- [External configuration and profiles](#7-externalized-configuration)
- [DataSource, runners, and lifecycle](#10-datasource-and-hikaricp-auto-configuration)
- [Logging, Actuator, testing, and packaging](#14-logging)
- [Diagnostics and readiness for REST](#19-failure-diagnostics)
- [Checklist and questions](#23-pre-rest-readiness-checklist)

## How to use this guide

1. Preserve the Spring Core mental model: Boot still starts a Spring container.
2. For each section, identify what your code declares and what Boot supplies.
3. Type the compact examples and predict the beans before running them.
4. Use the checkpoints to explain behavior without saying “Boot does magic.”
5. Build the companion `Spring_Boot_Mini_Project.md` after the foundations are clear.

### Priority legend

| Marker | Meaning | Study approach |
|---|---|---|
| ⭐⭐⭐⭐⭐ **MUST KNOW** | Appears in nearly every Boot application | Explain it and use it without notes |
| ⭐⭐⭐⭐ **IMPORTANT** | Common production knowledge | Understand normal use and failure modes |
| ⭐⭐⭐ **NICE TO KNOW** | Useful context and diagnostics | Recognize it and know when to look it up |
| ⭐⭐ **FUTURE KNOWLEDGE** | Safe to postpone | Learn the purpose, not implementation details |

## Version note

This guide targets the current stable Spring Boot **4.1.1** release as of September 2026. Spring Boot 4.1.1 requires Java 17 or newer and Maven 3.6.3 or newer. The examples deliberately use the Java 17 baseline even if your local JDK is newer.

Boot 4.1.1 manages Spring Framework 7.0.9 or newer within its compatible dependency set, along with supported third-party versions. Do not separately pin Spring Framework modules. Use the Maven wrapper or verify the actual toolchain with:

```powershell
java -version
mvn --version
```

Older tutorials may use Spring Boot 2.x, Java 8/11, or `javax.annotation` APIs. Do not copy their versions or imports into a Boot 4 project without checking the current reference documentation. This guide uses current `jakarta.*` lifecycle/validation annotations where applicable.

---

## The high-level mental model

### Plain Spring

```text
main()
  ↓
manually create ApplicationContext
  ↓
register configuration
  ↓
component scan
  ↓
create beans
  ↓
inject dependencies
  ↓
use application
  ↓
close context
```

### Spring Boot

```text
main()
  ↓
SpringApplication.run(...)
  ↓
prepare Environment and choose context type
  ↓
create and refresh ApplicationContext
  ├── component scanning
  ├── application configuration
  ├── conditional auto-configuration
  └── bean creation / dependency injection
  ↓
run startup callbacks
  ↓
application ready
  ↓
close ApplicationContext at shutdown
```

The important continuity is:

```text
Spring Core: manual context startup and explicit configuration
                         ↓
Spring Boot: SpringApplication.run(...) and convention-based configuration
                         ↓
The result is still an ApplicationContext containing Spring beans
```

Boot changes **how much setup you write**, not the underlying container model.

---

## 1. What Problem Spring Boot Solves

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### The problem

Spring Framework is modular and flexible. That flexibility means a developer must normally make many setup choices:

- which Spring modules and third-party libraries belong together;
- which compatible dependency versions to use;
- how to create common infrastructure beans;
- where configuration values come from;
- how the application is started, packaged, logged, observed, and stopped.

Those decisions are valuable when an application is unusual. They are repetitive when an application follows a common shape.

Spring Boot solves this **application setup and integration problem**. It provides:

- curated dependency versions;
- starter dependencies for common capabilities;
- conditional auto-configuration;
- a standard startup mechanism;
- externalized configuration conventions;
- executable packaging;
- production-oriented logging, health, and diagnostic support.

### Spring Framework versus Spring Boot

| Spring Framework | Spring Boot |
|---|---|
| Supplies IoC, DI, configuration, lifecycle, AOP, JDBC support, transactions, MVC, and more | Builds on those Framework capabilities |
| Lets you assemble infrastructure explicitly | Offers a conventional assembly with defaults |
| Can be used without Boot | Depends on and configures Spring Framework |
| Owns the `ApplicationContext` abstraction | Starts and customizes an `ApplicationContext` |
| Defines beans and dependency injection | Adds more bean definitions when conditions match |

**Does Boot replace Spring Core?** No. A Boot application is a Spring application. Component scanning, `@Bean`, constructor injection, bean scopes, profiles, and lifecycle remain Spring Framework concepts.

### What Boot automates

Boot can automate infrastructure setup when it has enough evidence. Examples:

- a JDBC starter and driver are present;
- `spring.datasource.*` properties exist;
- no application-defined `DataSource` bean replaces the default;
- therefore Boot can create and configure a pooled `DataSource`.

It also configures logging defaults, configuration-file loading, test support, packaging, and—later, when a web starter is present—an embedded server and MVC infrastructure.

### What Boot does not automate

Boot does not decide:

- your business rules;
- your domain model or layer boundaries;
- which operations belong in one transaction;
- whether a database query is correct or efficient;
- how secrets should be issued and rotated;
- which profiles your organization should deploy;
- how much traffic a pool or server must support;
- whether a default is appropriate for production.

It can create a `DataSource`; it cannot determine whether your transfer operation needs one connection and one atomic transaction. It can bind a timeout value; it cannot choose the correct service-level objective for you.

### Three related terms

| Term | Meaning | Example |
|---|---|---|
| **Convention over configuration** | Follow a known structure and write less explicit setup | `application.yaml` is loaded by its conventional name |
| **Opinionated configuration** | Boot chooses a reasonable default among several valid options | HikariCP is selected when the JDBC setup and classpath support it |
| **Auto-configuration** | Conditional Spring configuration contributes beans and settings | A pooled `DataSource` is configured only when relevant conditions match |

An opinion is not a rule. You can override properties, define your own beans, or exclude an auto-configuration—but do that because the application requires it, not merely to make Boot look more explicit.

**Mental model:** Spring Framework provides the machinery. Spring Boot notices the application shape and preconfigures common machinery.

**Common mistake:** Calling all automatic behavior “component scanning.” Scanning finds your components; auto-configuration imports Boot's conditional configuration. They cooperate, but they are different mechanisms.

### Checkpoint 1

1. Does a Boot application still contain an `ApplicationContext`?
2. Name two decisions Boot can automate and two it cannot.
3. How is an opinionated default different from a mandatory rule?

---

## 2. Boot Vocabulary Before Code

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

| Concept | Short definition | What it is not |
|---|---|---|
| **Starter** | A curated dependency descriptor for one capability | A special container or annotation |
| **Dependency management** | A tested table of library versions | A guarantee that every library is included |
| **Auto-configuration** | Conditional bean/configuration registration | Source-code generation |
| **Back-off** | Boot refrains from supplying a default when your configuration takes control | Boot deleting your bean |
| **External configuration** | Values supplied outside compiled Java code | Only `application.properties` |
| **Profile** | A named environment/configuration group | A secure secret store |
| **Actuator** | Operational endpoints and observability integration | A replacement for application monitoring |
| **Executable JAR** | Application classes, dependencies, and Boot launcher packaged to run with `java -jar` | A native operating-system executable |

Keep this causal chain in mind:

```text
dependency on classpath
        +
external properties
        +
existing beans
        +
application type
        ↓
auto-configuration conditions
        ↓
matching Boot configuration contributes beans
        ↓
normal Spring bean creation and injection
```

---

## 3. `@SpringBootApplication`

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

A typical application begins with one class:

```java
package com.example.bootpractice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BootPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootPracticeApplication.class, args);
    }
}
```

Conceptually, `@SpringBootApplication` combines three responsibilities:

```text
@SpringBootApplication
        ├── @SpringBootConfiguration
        │       └── identifies the primary Boot configuration class
        │           (and is itself based on Spring @Configuration)
        ├── @EnableAutoConfiguration
        │       └── imports matching Boot auto-configuration
        └── @ComponentScan
                └── discovers your components from this package downward
```

### Configuration responsibility

The application class is a Spring configuration source. You may declare `@Bean` methods there, although focused configuration classes are usually clearer once an application grows.

### Component-scanning responsibility

The package containing the application class is normally the scan base:

```text
com.example.myapp.Application
        ↓ scan normally covers
com.example.myapp.*
        ├── service/
        ├── repository/
        ├── config/
        └── diagnostics/
```

Recommended structure:

```text
src/main/java/
└── com/example/myapp/
    ├── Application.java       ← root package
    ├── config/
    ├── model/
    ├── repository/
    └── service/
```

Problematic structure:

```text
com.example.bootstrap.Application
com.example.service.AccountService   ← sibling package, not below bootstrap
```

In the problematic structure, default scanning from `com.example.bootstrap` does not discover `com.example.service`. Constructor injection then fails because no service bean was registered.

Put the main class in a meaningful root package. Explicit `scanBasePackages` exists, but package repair is usually simpler and less fragile.

### Auto-configuration responsibility

Enabling auto-configuration asks Boot to consider its registered configurations. It does **not** mean every possible bean is created. Conditions still determine which configurations apply.

**Mental model:** `@SpringBootApplication` establishes the application root for your configuration, your component scan, and Boot's conditional configuration.

**Common mistakes:**

- Putting the application class in the default package, which can cause an excessively broad scan.
- Putting it too deep, so sibling packages are invisible.
- Adding `@ComponentScan` everywhere to compensate for a confused package structure.
- Thinking the annotation itself creates all beans before the context refreshes.

### Checkpoint 2

1. Which three responsibilities does `@SpringBootApplication` combine conceptually?
2. If `Application` is in `com.example.app`, is `com.example.shared` scanned by default?
3. Does enabling auto-configuration guarantee that a `DataSource` will exist?

---

## 4. `SpringApplication.run(...)` and the Context

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

```java
ConfigurableApplicationContext context =
        SpringApplication.run(BootPracticeApplication.class, args);
```

The method receives:

- the primary configuration source (`BootPracticeApplication.class`);
- command-line arguments that may also participate in configuration.

It returns the running `ConfigurableApplicationContext`. Most applications do not store the return value because Boot manages normal lifecycle integration, but the return type is useful evidence: Boot has started a Spring context, not replaced it.

### Conceptual startup sequence

The exact internal sequence is more detailed, but this is the useful developer model:

```text
1. Build SpringApplication from the primary source
2. Read command-line arguments and prepare Environment
3. Determine the application/context type
4. Create ApplicationContext
5. Load your bean definitions and matching auto-configuration
6. Refresh context
   ├── process configuration
   ├── instantiate eager singleton beans
   ├── inject dependencies
   └── invoke initialization callbacks
7. Run ApplicationRunner / CommandLineRunner beans
8. Publish ready state
```

If a required bean cannot be created or configuration cannot bind, context refresh fails. Runners do not execute after a failed refresh.

### The first argument matters

The class passed to `run` anchors primary configuration and application discovery. Normally pass the class carrying `@SpringBootApplication`, not an arbitrary service class.

### The `args` matter

Arguments in Boot property form can override configuration:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--app.batch-size=25"
```

or:

```powershell
java -jar target/boot-practice-1.0.0.jar --app.batch-size=25
```

Do not write an ad hoc argument parser for values already handled by externalized configuration.

### Non-web versus web applications

`SpringApplication.run` does not inherently start an HTTP server. A command-line project with `spring-boot-starter` and `spring-boot-starter-jdbc` can start a non-web context and run finite work. It should then close the returned context explicitly, or let JVM shutdown trigger Boot's shutdown hook once no non-daemon work remains. A later web project has web libraries and matching auto-configuration, so Boot creates the appropriate web context and embedded server.

**Mental model:** `run` is the Boot entry point into an otherwise recognizable Spring context lifecycle.

**Common mistake:** Calling `new AnnotationConfigApplicationContext(...)` as well as `SpringApplication.run(...)`. That creates two contexts and may duplicate singleton infrastructure such as pools.

### Checkpoint 3

1. What does `SpringApplication.run` return?
2. At what stage are normal singleton beans created?
3. In a finite non-web command, how should the application ensure its context and resources close after runners finish?

---

## 5. Starters and Dependency Management

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### A starter is a dependency set

A starter is a small Maven artifact that pulls in a supported set of dependencies for a capability. For example, the JDBC starter brings the Spring JDBC and pooling-related pieces expected by Boot's JDBC auto-configuration.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

The starter does not execute code merely because its name contains “starter.” Its transitive dependencies change the classpath; Boot's conditional auto-configuration reacts to that classpath.

### The parent and managed versions

A focused Maven project can inherit Boot's parent:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
    <relativePath/>
</parent>

<properties>
    <java.version>17</java.version>
</properties>
```

The parent supplies dependency management and useful plugin defaults. Therefore managed dependencies normally omit versions:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
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
```

Boot's dependency-management BOM is an alternative when a project must inherit a different corporate parent. The parent is simpler for a learning project.

### Why omitting versions is intentional

Boot 4.1.1 manages a tested compatibility set. Adding arbitrary versions can break that set. Override one only for a documented reason and verify compatibility.

```text
Boot version
   ↓ selects
dependency management table
   ↓ supplies versions for
Spring modules + common third-party libraries
```

Dependency management supplies a version only if you declare or transitively receive the dependency. It does not add every managed library to the application.

### Starter selection

| Need | Typical dependency now or later |
|---|---|
| Core Boot application | `spring-boot-starter` |
| JDBC and pooled `DataSource` | `spring-boot-starter-jdbc` plus database driver |
| Operational endpoints | `spring-boot-starter-actuator` |
| Boot-aware testing | `spring-boot-starter-test` with test scope |
| REST/MVC later | A web/MVC starter—deliberately postponed here |

Do not add every starter “in case.” Classpath contents influence auto-configuration and application startup.

**Mental model:** A starter assembles ingredients; dependency management aligns their versions; auto-configuration decides what to cook.

**Common mistakes:**

- Adding explicit versions to all Boot-managed dependencies.
- Confusing a starter with an annotation or generated code.
- Adding both overlapping starters without checking the dependency tree.
- Declaring a database driver with test scope when the application needs it at runtime.

### Checkpoint 4

1. Does dependency management add a PostgreSQL driver without a dependency declaration?
2. Why can adding a starter change the beans in the context?
3. When should a managed dependency version be overridden?

---

## 6. Conditional Auto-configuration and Back-off

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Auto-configuration is ordinary Spring Java configuration guarded by conditions. Useful condition questions include:

- Is a required class on the classpath?
- Is a property present or set to a particular value?
- Is the application a web or non-web application?
- Is a required bean already present?
- Is a particular resource available?

Conceptual example:

```java
@Configuration
@ConditionalOnClass(UsefulClient.class)
class UsefulClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    UsefulClient usefulClient() {
        return new UsefulClient();
    }
}
```

Read this as:

```text
UsefulClient class exists?
        ↓ yes
No UsefulClient bean supplied by application?
        ↓ yes
Create Boot's default UsefulClient bean
```

The real Boot configurations contain more conditions, ordering, and property binding, but the decision model is the same.

### Back-off

Many auto-configurations use missing-bean conditions. If you deliberately provide a bean of the relevant type, Boot's default backs off:

```java
@Configuration
class DatabaseConfig {

    @Bean
    DataSource dataSource() {
        // Your code has accepted DataSource creation and lifecycle responsibility.
        return createCustomDataSource();
    }
}
```

For `DataSource` specifically, Boot's pooled data-source configuration is conditional on a missing `DataSource` (and missing XA data source). Defining your own is therefore a significant architectural choice, not a harmless duplicate.

### Override properties before replacing infrastructure

Prefer the smallest customization:

```text
Need a different pool size?
        ↓
set spring.datasource.hikari.maximum-pool-size
        ↓
keep Boot's DataSource ownership
```

Only define a custom `DataSource` when properties and supported customizers cannot express the requirement.

### Auto-configuration is deterministic, not magical

You can inspect it:

```powershell
java -jar target/app.jar --debug
```

The conditions evaluation report explains positive and negative matches. A later Actuator-enabled application can also expose the `conditions` endpoint deliberately, but that endpoint can reveal application internals and should not be publicly exposed by default.

**Mental model:** Auto-configuration is a set of `if` statements around Spring configuration.

**Common mistakes:**

- Believing Boot scans the internet or guesses business intent.
- Defining a bean with the same purpose and expecting Boot to merge both configurations.
- Excluding auto-configuration before reading the failure report.
- Copying Boot's internal configuration into application code.

### Checkpoint 5

1. What evidence can an auto-configuration condition inspect?
2. What does “Boot backs off” mean?
3. Why is a property override usually safer than replacing an infrastructure bean?

---
## 7. Externalized Configuration

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Externalized configuration separates deploy-time values from compiled code. The same JAR can run with different database URLs, pool sizes, feature settings, and log levels.

```text
application.yaml
OS environment variables
Java system properties
command-line arguments
        ↓
Spring Environment
        ↓
property binding / @Value / Environment
        ↓
configured beans
```

### Conventional files

Boot automatically discovers config data named `application.properties` or `application.yaml` in conventional classpath and external locations.

Properties:

```properties
app.report.title=Daily account summary
app.report.batch-size=100
spring.datasource.url=jdbc:postgresql://localhost:5432/boot_practice
```

YAML:

```yaml
app:
  report:
    title: Daily account summary
    batch-size: 100

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/boot_practice
```

Both forms become keys in the same `Environment`. Choose one format consistently. YAML is compact for nested groups; properties are explicit and easy to override one key at a time. YAML is not a mechanism for type safety—that comes from binding to a Java type.

### Useful precedence model

Boot has a detailed property-source order. For everyday reasoning, later and more deployment-specific sources override packaged defaults:

```text
highest practical precedence
command-line arguments                 --app.report.batch-size=25
SPRING_APPLICATION_JSON
Java system properties                 -Dapp.report.batch-size=50
OS environment variables               APP_REPORT_BATCHSIZE=75
external application config files
packaged application config files
SpringApplication default properties
lowest practical precedence
```

Profile-specific config overrides non-profile config at the same location. Test-specific property sources can override normal sources during tests. When diagnosis depends on an exact edge case, consult the full official property-source order rather than memorizing a shortened list.

### Environment-variable mapping

Spring's relaxed binding converts canonical property names to environment-variable form. The reliable conversion rule is:

1. replace `.` with `_`;
2. remove `-`;
3. convert to uppercase.

```text
spring.datasource.url          → SPRING_DATASOURCE_URL
app.report.batch-size          → APP_REPORT_BATCHSIZE
logging.level.com.example      → LOGGING_LEVEL_COM_EXAMPLE
```

Alternatively, use placeholders to deliberately bridge an existing environment-variable name:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

`${NAME:default}` supplies a fallback:

```yaml
app:
  report:
    batch-size: ${REPORT_BATCH_SIZE:100}
```

Do not give secrets insecure defaults. A missing database password should normally fail configuration rather than silently using a known password.

### `.env` is not automatically loaded by Boot

A file named `.env` has meaning to tools such as Docker Compose, shells, and IDE launch configurations. It is not automatically an application property source merely because a Boot application starts in the same directory. Export the variables into the Java process, configure the IDE, or use a deliberate supported config import.

```text
Docker Compose reads .env
        ↓
interpolates compose.yaml

Host JVM starts separately
        ↓
sees only its process environment and Boot config sources
```

### Never log the entire environment

Configuration debugging can expose passwords, tokens, and URLs containing credentials. Log only safe, selected values, and redact secrets in `toString()` methods and diagnostic endpoints.

**Mental model:** Boot builds one ordered `Environment`; the winning value for each key is then consumed by binding or bean configuration.

**Common mistakes:**

- Assuming `application.yaml` always wins over environment variables.
- Expecting Compose's `.env` to enter an independently launched host JVM.
- Inventing environment-variable spellings without checking relaxed-binding rules.
- Committing passwords in profile files.
- Mixing YAML and properties for the same settings until precedence becomes unclear.

### Checkpoint 6

1. Which normally wins: `application.yaml` or an OS environment variable?
2. Does Docker Compose export `.env` values into its parent shell?
3. What environment-variable name corresponds to `app.report.batch-size`?

---

## 8. Type-safe Configuration with `@ConfigurationProperties`

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

`@Value` is acceptable for one isolated value:

```java
public ReportService(@Value("${app.report.title}") String title) {
    this.title = title;
}
```

For a cohesive group, use `@ConfigurationProperties`. It provides structured binding, conversion, metadata support, and validation.

```java
package com.example.bootpractice.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.report")
public record ReportProperties(
        @NotBlank String title,
        @Min(1) @Max(1_000) int batchSize
) {
}
```

Register configuration-properties types by scanning from the application root:

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class BootPracticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootPracticeApplication.class, args);
    }
}
```

Then inject the typed object normally:

```java
@Service
public class ReportService {
    private final ReportProperties properties;

    public ReportService(ReportProperties properties) {
        this.properties = properties;
    }
}
```

Matching YAML:

```yaml
app:
  report:
    title: Daily account summary
    batch-size: 100
```

For Jakarta Bean Validation constraints, include the validation starter. This use validates application configuration at startup; request-body validation belongs to the later REST topic.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

An invalid value should prevent the context from starting. That is desirable: a service configured with an impossible batch size is not “partially ready.”

### `@ConfigurationProperties` versus `@Value`

| Question | `@ConfigurationProperties` | `@Value` |
|---|---|---|
| Several related values? | Preferred | Becomes scattered |
| Type conversion? | Yes | Yes, but less cohesive |
| Nested structure? | Natural | Awkward |
| Bean Validation? | Designed for it | Manual |
| Relaxed binding? | Yes | More limited expression-oriented access |
| One genuinely isolated value? | May be too much ceremony | Reasonable |

Configuration-properties classes should represent configuration. Do not turn them into services or inject business collaborators into them.

**Mental model:** The `Environment` is an untyped key/value view; `@ConfigurationProperties` creates a validated domain model for configuration.

**Common mistakes:**

- Adding the annotation but never registering the properties class.
- Putting `@ConfigurationPropertiesScan` in a package that cannot see the type.
- Using public mutable fields for convenience when an immutable record expresses the contract.
- Validating input requests here; these constraints validate startup configuration.
- Including a password in an auto-generated or custom `toString()`.

### Checkpoint 7

1. Why is a typed settings object safer than five unrelated `@Value` fields?
2. When should invalid configuration fail?
3. Does `@ConfigurationProperties` decide which property source wins?

---

## 9. Profiles

**Priority: ⭐⭐⭐⭐ IMPORTANT**

A profile selects configuration and beans for a named environment or mode.

```text
application.yaml             ← common values
application-dev.yaml         ← dev overrides
application-prod.yaml        ← prod overrides
```

Profile-specific files are loaded by convention. At the same location, their values override the non-profile file.

Activate a profile from outside the artifact:

```powershell
java -jar target/app.jar --spring.profiles.active=dev
```

or with an environment variable:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
```

Profiles can also select bean definitions:

```java
public interface StartupMessage {
    String text();
}
```

```java
@Bean
@Profile("dev")
StartupMessage developmentMessage() {
    return () -> "Development diagnostics enabled";
}

@Bean
@Profile("prod")
StartupMessage productionMessage() {
    return () -> "Production mode";
}
```

### Good profile use

- environment-specific endpoints;
- safe local diagnostics;
- selecting an infrastructure adapter when environments genuinely differ;
- grouping a coherent set of configuration overrides.

### Poor profile use

- storing real passwords in `application-prod.yaml`;
- encoding every small business flag as a profile;
- activating `prod` inside the packaged base file;
- creating many overlapping profiles whose winning values are impossible to reason about.

Profile names are labels, not security boundaries. A profile file committed to Git is still committed to Git.

**Mental model:** A profile changes part of the bean/configuration graph before the context is refreshed.

**Common mistake:** Treating profiles as runtime `if` statements inside business methods. Prefer selecting configuration at startup; use a deliberate feature-management approach for runtime business flags.

### Checkpoint 8

1. Does `application-prod.yaml` replace or supplement `application.yaml`?
2. Should a production password live in a production profile file?
3. When is `@Profile` more appropriate than a business-method conditional?

---

## 10. `DataSource` and HikariCP Auto-configuration

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

This is the clearest bridge from your plain JDBC project to Boot.

### Plain JDBC/Hikari ownership

```text
Main
  ↓ reads environment
DatabaseSettings
  ↓ configures
HikariConfig
  ↓ constructs and owns
HikariDataSource
  ↓ passed as DataSource
repositories and services
```

### Boot ownership

```text
JDBC starter + PostgreSQL driver on classpath
        +
spring.datasource.* properties
        +
no application-defined DataSource bean
        ↓
DataSource auto-configuration
        ↓ creates bean
HikariDataSource (normally selected when Hikari is available)
        ↓ exposed through
javax.sql.DataSource
        ↓ constructor-injected into
repositories and services
        ↓ closed when
ApplicationContext shuts down
```

Your repository can remain interface-oriented:

```java
@Repository
public class AccountRepository {
    private final DataSource dataSource;

    public AccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Use getConnection() and try-with-resources as before.
}
```

Boot does not rewrite JDBC semantics. A Hikari proxy `Connection.close()` normally returns the logical connection to the pool. The context later closes the `HikariDataSource`, which shuts down the pool.

### Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Configuration

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      pool-name: boot-practice-pool
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 20000 # Hikari property unit: milliseconds
```

Boot supports duration syntax such as `20s` for properties declared as durations. Hikari's `connectionTimeout` setting is a millisecond `long`, so use the unit its configuration metadata/API declares rather than assuming every timeout accepts duration syntax.

Configure only settings you understand. Defaults are not proof of production sizing, but copying a large block of pool values is not tuning either.

### Ownership rules remain

| Resource | Owner in this Boot application | Consumer responsibility |
|---|---|---|
| `ApplicationContext` | `SpringApplication` / application lifecycle | Do not create a second context casually |
| `HikariDataSource` bean | Spring context | Do not call `close()` from a repository |
| Logical `Connection` | Method that calls `getConnection()` | Close promptly with try-with-resources |
| Statement / `ResultSet` | Method that creates it | Close promptly with try-with-resources |
| Physical pooled connections | HikariCP | Configure/observe through the pool |
| PostgreSQL container | Docker Compose | Start/stop separately from host JVM |

### Boot's JDBC conveniences do not erase choices

The JDBC starter can also auto-configure a `JdbcTemplate`. You may use it later or continue with raw JDBC for this learning boundary. The existence of a `JdbcTemplate` does not create tables, define transaction boundaries, or choose correct SQL.

Boot can run `schema.sql` and `data.sql`. For an external database such as PostgreSQL, script initialization is not simply something to assume; configure the intended mode deliberately, for example in a disposable learning project:

```yaml
spring:
  sql:
    init:
      mode: always
```

Choose one schema owner. Do not have both the PostgreSQL container's fresh-volume initialization and Boot repeatedly initialize the same schema unless the scripts are intentionally designed for that arrangement. Production applications normally use a migration tool later in the roadmap.

### Host versus container address

If the Boot application runs through Maven on the host and PostgreSQL runs in Compose:

```text
jdbc:postgresql://localhost:5432/boot_practice
```

If the Boot application later becomes another Compose service, the host is normally the Compose service name:

```text
jdbc:postgresql://postgres:5432/boot_practice
```

Boot externalizes the URL; it does not change networking rules.

### When concrete Hikari types are appropriate

Business repositories should depend on `DataSource`. Focused diagnostics may conditionally inspect `HikariDataSource` or its MXBean because metrics and shutdown state are implementation-specific. Keep that dependency at the diagnostic boundary.

**Mental model:** Boot moved pool construction from your `Main` method into a conditional configuration class; connection ownership rules did not move.

**Common mistakes:**

- Creating another `HikariDataSource` in a repository.
- Closing the application-owned pool after one method call.
- Assuming Boot makes a leaked connection safe.
- Setting both `DB_URL` and `spring.datasource.url` without defining how they relate.
- Expecting host Java to reach a container through the hostname `postgres`.
- Running two independent schema-initialization mechanisms accidentally.

### Checkpoint 9

1. Who closes the auto-configured pool?
2. Who closes each borrowed logical connection?
3. What normally causes Boot's data-source auto-configuration to back off?
4. Does adding the JDBC starter determine the correct JDBC URL?

---

## 11. Application Structure and Bean Boundaries

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Boot removes setup code, not architecture. Keep dependencies pointed inward through clear constructor boundaries:

```text
startup runner
      ↓
application service
      ↓
repository interface / repository
      ↓
DataSource
      ↓
HikariCP + pgJDBC + PostgreSQL
```

Suggested pre-REST structure:

```text
com.example.bootpractice/
├── BootPracticeApplication.java
├── config/
│   └── ReportProperties.java
├── model/
│   └── Account.java
├── repository/
│   └── AccountRepository.java
├── service/
│   └── AccountSummaryService.java
├── startup/
│   └── VerificationRunner.java
└── diagnostics/
    └── DataSourceDiagnostics.java
```

Use constructor injection exactly as in Spring Core:

```java
@Service
public class AccountSummaryService {
    private final AccountRepository repository;

    public AccountSummaryService(AccountRepository repository) {
        this.repository = repository;
    }
}
```

Do not use the `ApplicationContext` as a service locator inside business code:

```java
// Avoid: hides the real dependency.
AccountRepository repository = context.getBean(AccountRepository.class);
```

The main class starts the application. It should not become a container for database queries and business logic.

**Mental model:** Boot assembles the graph; package and constructor boundaries explain the graph.

**Common mistake:** Believing stereotype annotations define architecture by themselves. A class named `service` can still contain SQL; an annotation does not repair misplaced responsibility.

---

## 12. Startup Work with Runners

**Priority: ⭐⭐⭐⭐ IMPORTANT**

Non-web applications often need work to run after the context is ready. Boot provides two runner interfaces:

| Interface | Argument view |
|---|---|
| `ApplicationRunner` | Parsed `ApplicationArguments` |
| `CommandLineRunner` | Raw `String... args` |

```java
@Component
public class VerificationRunner implements ApplicationRunner {
    private final AccountSummaryService service;

    public VerificationRunner(AccountSummaryService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) {
        service.printSummary();
    }
}
```

At runner time:

- the context has refreshed;
- eager singleton beans have been created;
- constructor injection is complete;
- initialization callbacks have run;
- all matching auto-configuration has been applied.

Use `@Order` or `Ordered` only when multiple runners genuinely depend on an order. Prefer one orchestrating runner over an accidental chain of callbacks.

If a runner cannot complete essential startup work, let the exception propagate. Catching it, printing a message, and returning can make an unhealthy command-line application report success.

### Runner versus lifecycle callback

| Need | Appropriate mechanism |
|---|---|
| Validate one bean's internal state | constructor or initialization callback |
| Run application-level startup workflow | runner |
| Release a bean-owned resource | destruction callback |
| React to detailed Boot lifecycle stage | application event, when truly needed |

Do not put slow database workflows in `@PostConstruct`. It blurs bean construction with application orchestration and makes failures harder to locate.

**Mental model:** Runners are Spring-managed entry points after context creation, not substitutes for `main()` or background-job infrastructure.

**Common mistakes:**

- Manually constructing a runner, bypassing injection.
- Swallowing runner exceptions.
- Adding several ordered runners when one coordinator would be clearer.
- Assuming a runner executes before singleton initialization.

### Checkpoint 10

1. When does a runner execute relative to context refresh?
2. Why should an essential runner failure propagate?
3. When would `ApplicationRunner` be more convenient than `CommandLineRunner`?

---

## 13. Application Lifecycle and Shutdown

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Boot integrates the Spring context with the JVM lifecycle. Normal termination closes the context, which destroys managed singleton beans in dependency-aware order.

```text
JVM shutdown / explicit context close
        ↓
ApplicationContext closes
        ↓
destruction callbacks run
        ↓
HikariDataSource.close()
        ↓
pool stops and physical connections close
```

You can observe bean lifecycle:

```java
@Component
public class LifecycleProbe {
    private static final Logger log =
            LoggerFactory.getLogger(LifecycleProbe.class);

    @PostConstruct
    void initialized() {
        log.info("Lifecycle probe initialized");
    }

    @PreDestroy
    void shuttingDown() {
        log.info("Lifecycle probe shutting down");
    }
}
```

With Java 17+ and modern Spring, these annotations come from `jakarta.annotation`, not the old `javax.annotation` package.

### Who owns what?

If Boot creates the `DataSource` bean, the context owns its lifecycle. A repository owns each handle it borrows, but not the pool:

```java
try (Connection connection = dataSource.getConnection()) {
    // use and return logical handle
}
```

Do not call `dataSource.close()` inside application code. `DataSource` does not even define a general `close()` method; Hikari's concrete implementation does, and the container invokes it as a bean destruction method.

### Deterministic non-web shutdown

A normal Boot application registers a shutdown hook. A finite command-line tool can also explicitly scope the context when appropriate:

```java
public static void main(String[] args) {
    try (ConfigurableApplicationContext context =
                 SpringApplication.run(BootPracticeApplication.class, args)) {
        // Runners execute before run(...) returns.
    }
}
```

Use this pattern only when the application is intentionally finite. A future server application must keep its context open to receive work.

Abrupt process termination may prevent graceful callbacks. Resource cleanup remains necessary, but it is not a substitute for database durability, idempotency, or operational process management.

**Mental model:** Bean creation and bean destruction are two halves of context ownership.

**Common mistakes:**

- Closing an injected pool from a consumer.
- Treating `Connection.close()` as pool shutdown.
- Expecting `@PreDestroy` after a forced process kill.
- Explicitly closing the context immediately in an application intended to remain a server.

### Checkpoint 11

1. What operation causes Boot-managed pool shutdown?
2. How does `Connection.close()` differ from `HikariDataSource.close()`?
3. Why is explicit context scoping suitable for a finite CLI but not a server?

---

## 14. Logging

**Priority: ⭐⭐⭐⭐ IMPORTANT**

Boot starters normally establish a logging facade and default implementation. Application code should log through SLF4J:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountSummaryService {
    private static final Logger log =
            LoggerFactory.getLogger(AccountSummaryService.class);

    public void printSummary(int accountCount) {
        log.info("Loaded {} accounts", accountCount);
    }
}
```

Parameterized logging avoids manual string construction and defers message formatting when a level is disabled.

Configure levels externally:

```yaml
logging:
  level:
    root: INFO
    com.example.bootpractice: DEBUG
    com.zaxxer.hikari: INFO
```

or temporarily:

```powershell
java -jar target/app.jar --logging.level.com.example.bootpractice=DEBUG
```

### Practical level meanings

| Level | Typical use |
|---|---|
| `ERROR` | Operation/application failure requiring attention |
| `WARN` | Unexpected or degraded behavior that can continue |
| `INFO` | Important lifecycle and business milestones |
| `DEBUG` | Developer diagnostics, normally disabled in production |
| `TRACE` | Very detailed flow; enable narrowly and temporarily |

Do not log passwords, complete environment maps, authorization headers, tokens, or sensitive database data. Logging “connection succeeded” is useful; logging the password used is never useful.

Boot's banner and startup messages are not evidence that every business dependency works. A database connection may be acquired lazily after startup unless something validates it.

**Mental model:** Boot configures the logging system early; your code still chooses meaningful events, levels, and safe fields.

**Common mistakes:**

- Using `System.out.println` for operational events.
- Concatenating expensive log messages.
- Enabling root `DEBUG` in production instead of targeting one package.
- Logging configuration objects that include secrets.
- Assuming the absence of an error log proves startup work ran.

### Checkpoint 12

1. Why prefer `log.info("Loaded {} accounts", count)` over concatenation?
2. Which package should normally receive DEBUG when diagnosing your code?
3. Can a successful Boot banner prove a lazy database connection works?

---

## 15. Actuator Basics

**Priority: ⭐⭐⭐⭐ IMPORTANT**

Actuator contributes operational information through managed endpoints and observability integrations.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Common endpoint concepts include:

| Endpoint | Purpose |
|---|---|
| `health` | Aggregated readiness of the application and dependencies |
| `info` | Deliberately supplied application information |
| `metrics` | Named measurements when the relevant observation support exists |
| `env` | Configuration view—sensitive and not for broad exposure |
| `configprops` | Bound configuration-properties view—also sensitive |
| `conditions` | Why auto-configurations matched or did not match |

When a web application is added later, the conventional base path is `/actuator`, so health is commonly available at `/actuator/health`. Actuator alone does not turn a non-web command-line application into an HTTP server; a web transport must exist.

Exposure is a security decision:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: never
```

Start with the minimum. Endpoints such as `env`, `configprops`, `beans`, and `conditions` can reveal structure or configuration even when sensitive values are sanitized. Do not expose every endpoint publicly.

### Health is not a business audit

A health indicator answers an operational question such as “Can this dependency be reached?” It does not prove that transfer logic preserves money or that every query is correct. Likewise, returning `UP` does not replace monitoring, alerts, logs, or acceptance tests.

### Hikari metrics

With the relevant observation/metrics infrastructure, Boot can expose pool measurements. These measurements help diagnose saturation and leaks, but they do not choose a correct `maximumPoolSize`. Interpret active, idle, pending, and total connections in the context of workload and database limits.

**Mental model:** Actuator makes selected runtime state observable; exposure policy decides who can see it.

**Common mistakes:**

- Adding Actuator and assuming all endpoints are safely public.
- Exposing `*` during deployment and forgetting to restrict it.
- Treating health as a substitute for business verification.
- Expecting an HTTP health URL in a non-web process.

### Checkpoint 13

1. Does Actuator itself necessarily start a web server?
2. Why should `env` and `configprops` be protected?
3. What can pool metrics show, and what can they not decide?

---

## 16. Testing Concepts in Boot

**Priority: ⭐⭐⭐⭐ IMPORTANT**

You will study testing more deeply later, but you need to distinguish test scopes before REST.

### 1. Plain unit test

Construct the class yourself. No Spring context starts.

```java
class AccountSummaryServiceTest {

    @Test
    void calculatesTotalFromRepositoryData() {
        AccountRepository repository = new FakeAccountRepository();
        AccountSummaryService service = new AccountSummaryService(repository);

        // Exercise and assert the business result.
    }
}
```

Use this for business behavior that does not require container integration. Constructor injection keeps this easy.

### 2. Context smoke test

```java
@SpringBootTest
class BootPracticeApplicationTest {

    @Test
    void contextLoads() {
    }
}
```

This verifies that the full application context can start with the test configuration. It can detect missing beans, invalid bindings, and some auto-configuration problems. An empty `contextLoads` test does **not** prove SQL correctness or business behavior.

### 3. Focused test slice

Boot test slices load a constrained part of the application. A JDBC-focused test can use `@JdbcTest`; later web work has its own slice. A slice is not simply a faster spelling of `@SpringBootTest`: excluded components must be imported or supplied deliberately.

### 4. Real integration test

An integration test may start a real PostgreSQL dependency, load relevant configuration, execute repository/service behavior, and assert database state. This has more confidence and more cost than a unit test. Testcontainers is valuable later, but its details are not required before REST.

### Test dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Use test profiles or test property sources for safe test settings:

```java
@SpringBootTest
@ActiveProfiles("test")
class BootPracticeApplicationTest {
}
```

Never let a routine test accidentally connect to or modify a production database. A profile name alone is not protection; verify the effective URL and credentials.

### What each test proves

| Test | Proves | Does not prove |
|---|---|---|
| Plain unit test | One class's logic with controlled collaborators | Boot wiring or database integration |
| Context smoke test | Selected/full context can start | Every behavior works |
| Slice test | One framework layer integrates correctly | Whole application starts |
| PostgreSQL integration test | Real SQL and relevant wiring behave as asserted | Production capacity or deployment correctness |

**Mental model:** Load only as much infrastructure as the claim under test requires.

**Common mistakes:**

- Using `@SpringBootTest` for every pure calculation.
- Keeping only an empty `contextLoads` test.
- Assuming an in-memory database behaves exactly like PostgreSQL.
- Hiding broken architecture behind container-managed test mocks.
- Reusing real development or production data in tests.

### Checkpoint 14

1. What does an empty context smoke test actually prove?
2. Why is a plain unit test often preferable for service logic?
3. When does real PostgreSQL integration provide information an in-memory database cannot?

---

## 17. Maven Run, Package, and Executable JAR

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Declare the Boot Maven plugin:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

With the Boot parent, the plugin participates in creating a repackaged executable archive.

### Development run

```powershell
mvn spring-boot:run
```

This uses Maven's resolved runtime classpath and is convenient during development. It is not the artifact you deploy.

### Verification and packaging

```powershell
mvn clean verify
```

`verify` compiles, runs the configured tests, packages the application, and completes later verification phases.

For packaging alone:

```powershell
mvn package
```

### Run the built artifact

```powershell
java -jar target/boot-practice-1.0.0.jar
```

The executable JAR contains:

- your compiled application classes and resources;
- nested runtime dependencies;
- Boot launcher support and metadata.

It is not the same as a thin JAR that requires you to construct an external classpath manually.

### Arguments and JVM options are different

```powershell
# JVM system property: handled by the JVM
java -Dspring.profiles.active=dev -jar target/app.jar

# Boot command-line property: handled through application arguments
java -jar target/app.jar --spring.profiles.active=dev
```

JVM options must appear before `-jar`. Application arguments appear after the JAR name.

### Inspect what Maven selected

```powershell
mvn dependency:tree
mvn help:effective-pom
```

The first shows resolved dependencies and scopes. The second shows inherited parent and plugin configuration. Use them before guessing why a class or plugin behavior exists.

**Mental model:** Maven builds the artifact; the Boot plugin repackages it so the Boot launcher can start the application with its nested dependencies.

**Common mistakes:**

- Running `java -jar` against the unrepackaged thin archive.
- Putting `-D...` after the JAR name and expecting a JVM system property.
- Declaring the database driver only in a Maven plugin's dependencies.
- Treating `spring-boot:run` as a production process manager.
- Skipping tests with every package command.

### Checkpoint 15

1. What work does the Boot Maven plugin add to packaging?
2. Where must a JVM `-D` option appear?
3. Why run the packaged JAR at least once before considering the build verified?

---

## 18. DevTools

**Priority: ⭐⭐⭐ NICE TO KNOW**

DevTools can improve the edit/compile/restart loop:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

Its restart mechanism watches classpath changes and uses classloader separation to restart application code more quickly. This is a restart, not proof that arbitrary live object state was safely hot-swapped.

DevTools can also apply development-friendly property defaults. Therefore behavior with DevTools may differ slightly from a packaged run. Verify important lifecycle and configuration behavior with the built JAR too.

DevTools is disabled for normal fully packaged application execution and should not become a production runtime dependency. Marking it optional also helps prevent it from leaking transitively to consumers.

**Common mistakes:**

- Expecting source-file save alone to restart when the IDE has not recompiled.
- Diagnosing classloader-sensitive libraries without considering the restart classloader.
- Relying only on DevTools execution and never running the packaged artifact.
- Treating restart as a deployment strategy.

---

## 19. Failure Diagnostics

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Boot improves failure messages through startup analysis, but you still need a systematic sequence.

```text
Read the first clear failure description
        ↓
find the deepest relevant "Caused by"
        ↓
classify the boundary
  ├── package scanning / bean definition
  ├── dependency resolution
  ├── property binding / validation
  ├── conditional auto-configuration
  ├── database/network
  └── application startup code
        ↓
inspect effective configuration and conditions
        ↓
fix one cause and rerun
```

### Useful tools

```powershell
# Auto-configuration condition report
java -jar target/app.jar --debug

# Resolved dependency graph
mvn dependency:tree

# Inherited Maven configuration
mvn help:effective-pom

# Which profile is active (from startup logs or targeted safe diagnostics)
java -jar target/app.jar --spring.profiles.active=dev
```

### Common failures

| Symptom | Likely boundary | What to inspect |
|---|---|---|
| “required a bean … could not be found” | Scanning or missing bean | Main-class package, stereotype/`@Bean`, conditions |
| Several candidates for one constructor parameter | Bean resolution | `@Primary`, `@Qualifier`, or clearer design |
| Configuration-properties binding failure | External config | Key spelling, winning source, target type, validation constraint |
| No `DataSource` could be configured | Dependency/config | JDBC starter, driver, URL, exclusion, custom bean |
| Database connection refused | Network/process | Container health, host/port, host-versus-Compose DNS |
| Password authentication failed | Credentials/database state | Effective username/password source; never print the password |
| Component works in one package but disappears after move | Component scan | Root application package and scan boundary |
| Runner prints an error but process exits successfully | Exception handling | Swallowed exception; allow essential failure to propagate |
| Application starts but immediately exits | Application type/lifecycle | Non-web app with no continuing work; runner completion |
| Context fails with a dependency cycle | Architecture | Constructor graph; separate responsibilities rather than enabling cycles |

### Read condition reports correctly

A **negative match** is not automatically an error. Most auto-configurations should not match a small application. Find the configuration related to the missing capability and read why its relevant condition did not match.

A stack trace often contains cleanup failures after the primary startup failure. Preserve causality: the first meaningful configuration or bean error is usually more useful than the final generic “application run failed” line.

**Mental model:** Diagnose from the application's declared capability through classpath, properties, conditions, bean graph, and external dependency—in that order.

**Common mistakes:**

- Adding annotations randomly until the error disappears.
- Enabling all DEBUG logging before reading the concise failure analysis.
- Treating every negative auto-configuration match as broken.
- Pasting secrets or complete environment output into a bug report.
- Catching startup exceptions in `main` and returning exit code zero.

### Checkpoint 16

1. Why is a negative condition match often normal?
2. What should you inspect for a missing service bean before changing auto-configuration?
3. Why should `main` not swallow a failed `SpringApplication.run`?

---

## 20. Frequently Confused Boundaries

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

| Confusion | Correct distinction |
|---|---|
| Spring versus Spring Boot | Framework supplies foundations; Boot configures an application built on them |
| Component scanning versus auto-configuration | Scanning finds your components; auto-configuration imports conditional Boot configuration |
| Starter versus auto-configuration | Starter affects dependencies; auto-configuration reacts to classpath/configuration evidence |
| Dependency management versus dependency | Management selects a version; a dependency puts an artifact on the classpath |
| `ApplicationContext` versus `SpringApplication` | Context stores/manages beans; `SpringApplication` orchestrates Boot startup |
| `Environment` versus OS environment | Spring `Environment` combines many ordered property sources, including but not limited to OS variables |
| Profile versus secret store | Profile groups configuration; it does not secure values |
| `DataSource` versus HikariCP | `DataSource` is the contract; HikariCP is one pooling implementation |
| Logical connection close versus pool close | One returns a handle; the other shuts down application infrastructure |
| Health endpoint versus business correctness | Health observes operational state; tests verify business invariants |
| Runner versus `@PostConstruct` | Runner starts application-level work; callback initializes one bean |
| `spring-boot:run` versus executable JAR | One is a Maven development goal; one is the packaged runtime artifact |

---

## 21. Concept Priority Map

| Practice area | Priority | Ready when you can… |
|---|---|---|
| Boot as automation over Spring Core | ⭐⭐⭐⭐⭐ MUST KNOW | Explain what stayed the same and what became automatic |
| `@SpringBootApplication` and package placement | ⭐⭐⭐⭐⭐ MUST KNOW | Predict which components are scanned |
| `SpringApplication.run` and context startup | ⭐⭐⭐⭐⭐ MUST KNOW | Narrate startup through context refresh and runners |
| Starters and dependency management | ⭐⭐⭐⭐⭐ MUST KNOW | Separate classpath selection from bean configuration |
| Conditional auto-configuration and back-off | ⭐⭐⭐⭐⭐ MUST KNOW | Explain why a bean was or was not supplied |
| External configuration and precedence | ⭐⭐⭐⭐⭐ MUST KNOW | Predict the winning value without exposing secrets |
| `@ConfigurationProperties` | ⭐⭐⭐⭐⭐ MUST KNOW | Bind and validate a cohesive settings group |
| DataSource/Hikari lifecycle ownership | ⭐⭐⭐⭐⭐ MUST KNOW | Name pool, connection, and context owners |
| Executable JAR lifecycle | ⭐⭐⭐⭐⭐ MUST KNOW | Build and run the real artifact |
| Profiles | ⭐⭐⭐⭐ IMPORTANT | Keep common values separate from environment overrides |
| Runners and shutdown | ⭐⭐⭐⭐ IMPORTANT | Put startup work in the right lifecycle phase |
| Logging | ⭐⭐⭐⭐ IMPORTANT | Configure targeted levels and safe messages |
| Actuator health and exposure | ⭐⭐⭐⭐ IMPORTANT | Observe the app without exposing internals |
| Test scope selection | ⭐⭐⭐⭐ IMPORTANT | Choose unit, slice, context, or integration scope |
| DevTools | ⭐⭐⭐ NICE TO KNOW | Use restart support without depending on it |
| Custom auto-configuration authoring | ⭐⭐ FUTURE KNOWLEDGE | Recognize it as library/infrastructure work |
| `EnvironmentPostProcessor` and bootstrap internals | ⭐⭐ FUTURE KNOWLEDGE | Know extension points exist; avoid studying them now |
| AOT/native images | ⭐⭐ FUTURE KNOWLEDGE | Recognize constraints; postpone optimization |
| Advanced Actuator/Micrometer tracing | ⭐⭐ FUTURE KNOWLEDGE | Learn after basic APIs and operations |

---

## 22. Small Practice Prompts

These are deliberately smaller than the companion project.

### Exercise A — Predict the scan boundary

Given:

```text
com.example.start.Application
com.example.start.service.ReportService
com.example.repository.AccountRepository
```

Predict which application component is discovered by default. Move `Application` to the smallest sensible shared root and predict again.

### Exercise B — Predict auto-configuration

For each change, predict whether Boot can auto-configure a pooled `DataSource`:

1. JDBC starter present, PostgreSQL driver present, valid datasource properties.
2. Driver removed.
3. Application declares its own `DataSource` bean.
4. URL is absent and no embedded database exists.

Explain each answer with classpath, property, and missing-bean conditions.

### Exercise C — Resolve configuration precedence

Assume:

```yaml
# application.yaml
app:
  report:
    batch-size: 100
```

```text
OS environment: APP_REPORT_BATCHSIZE=75
command line: --app.report.batch-size=25
```

Predict the effective value, then remove the command-line value and predict again.

### Exercise D — Convert manual pool ownership

Start with a plain Java `Main` that creates `HikariConfig`, creates `HikariDataSource`, and passes it to a repository. Write down which lines disappear when Boot owns the pool and which lines **must remain** inside repository methods.

Expected distinction: factory and pool-close code move to Boot; `getConnection()` and try-with-resources do not disappear.

### Exercise E — Classify tests

Choose the smallest credible test scope for:

1. summing three `BigDecimal` balances;
2. verifying `ReportProperties` rejects a batch size of zero;
3. checking PostgreSQL-generated identity keys;
4. verifying the whole Boot bean graph starts.

### Exercise F — Diagnose before editing

Intentionally move a `@Service` outside the scan root and run the application. Read the failure analysis, restore the package, and rerun. Do not “fix” it by adding scans in several configuration classes.

---

## 23. Pre-REST Readiness Checklist

### Spring and Boot relationship

- [ ] I can explain why Boot does not replace Spring Framework.
- [ ] I can name which concerns Boot automates and which remain application decisions.
- [ ] I can distinguish convention, opinion, starter, auto-configuration, and back-off.
- [ ] I understand that Spring beans still live in an `ApplicationContext`.

### Startup and bean discovery

- [ ] I can explain the three responsibilities of `@SpringBootApplication`.
- [ ] My application class sits in the root package above components.
- [ ] I can narrate startup from `main()` through context refresh and runners.
- [ ] I know that `SpringApplication.run(...)` returns a configurable application context.
- [ ] I do not create a second context or manually construct Spring-managed services.

### Dependencies and configuration

- [ ] I can distinguish dependency declaration, dependency management, starter, and Maven plugin.
- [ ] I let Boot manage compatible dependency versions unless an override has a documented reason.
- [ ] I can identify which property source wins in a normal override scenario.
- [ ] I know how dots and dashes map to OS environment-variable names.
- [ ] I know that a generic `.env` file is not automatically loaded by Boot.
- [ ] I use `@ConfigurationProperties` for cohesive settings.
- [ ] Invalid required configuration fails startup without printing secrets.
- [ ] I understand profile files, active profiles, and the fact that profiles are not secret stores.

### JDBC and resource ownership

- [ ] I can explain why the JDBC starter and properties lead to an auto-configured `DataSource`.
- [ ] Repositories depend on `javax.sql.DataSource`, not unnecessarily on `HikariDataSource`.
- [ ] I understand when a user-defined `DataSource` makes Boot back off.
- [ ] The Spring context owns the pool; each borrowing method owns its logical connection handle.
- [ ] Every connection, statement, and result set still has a clear close boundary.
- [ ] I understand that Boot does not choose transaction boundaries or validate SQL correctness.
- [ ] I can explain `localhost` for a host JVM versus a Compose service name for a containerized JVM.
- [ ] Exactly one mechanism has clear ownership of development schema initialization.

### Operations and verification

- [ ] Startup work uses a runner rather than a constructor or an oversized `@PostConstruct` method.
- [ ] Essential startup exceptions propagate and produce failure status.
- [ ] Logs use SLF4J parameters, useful levels, and no secrets.
- [ ] I understand what health checks prove and what they do not.
- [ ] Actuator endpoint exposure is minimal and deliberate.
- [ ] I can choose among unit, slice, context, and database integration tests.
- [ ] I have run `mvn clean verify` and the packaged JAR.
- [ ] I can use `--debug`, the condition report, and the deepest relevant cause to diagnose startup.
- [ ] I understand how context shutdown closes managed infrastructure.

If several MUST KNOW items remain unclear, repeat the matching mini-project task before adding the web layer. REST introduces HTTP mapping and serialization; it should not also be the first time you debug component scanning or configuration precedence.

---

## 24. What Comes Next—and What Still Waits

**Priority: ⭐⭐⭐⭐ IMPORTANT**

You are ready to begin REST when the following pipeline makes sense without hidden steps:

```text
main()
  ↓
SpringApplication.run(...)
  ↓
ApplicationContext
  ├── discovers application components
  ├── applies conditional auto-configuration
  ├── binds external configuration
  ├── creates one managed DataSource/pool
  └── injects the application graph
  ↓
application is ready for an entry-point adapter
```

A REST controller will later become another entry-point adapter into a service. It should not own SQL, pool creation, environment lookup, or application startup. Learning Boot now protects those boundaries before HTTP is added.

Postpone these details until their roadmap stage:

| Topic | Why it can wait |
|---|---|
| Controller mappings and HTTP status design | This is the next REST layer |
| JSON serialization customization | Learn with real request/response models |
| Request validation and global exception handling | Requires HTTP failure semantics |
| Spring Data JPA/Hibernate | Adds a different persistence abstraction |
| Deeper declarative transactions | First preserve the one-transaction mental model |
| Spring Security | Easier after request flow is visible |
| Full Testcontainers strategy | Belongs with deeper integration testing |
| Dockerizing the Boot application | First understand host-run application configuration |
| AOT/native-image optimization | Solves later build/runtime concerns |
| Writing custom Boot starters/auto-configuration | Library-author work, not application fundamentals |

---

## 25. Reflection Questions

Answer these without looking at the key.

1. If Boot starts the context, where did Spring Core's `ApplicationContext` go?
2. What three responsibilities does `@SpringBootApplication` combine conceptually?
3. Why does the package of the main application class matter?
4. What does `SpringApplication.run(...)` return?
5. How does a starter differ from auto-configuration?
6. How does dependency management differ from a dependency declaration?
7. Name three kinds of evidence an auto-configuration condition can inspect.
8. What does it mean when Boot's default “backs off”?
9. Why should you try a supported property before replacing an infrastructure bean?
10. If `application.yaml` says `100`, an environment variable says `75`, and the command line says `25`, which value normally wins?
11. How does `app.demo.seed-count` map to an OS environment-variable name?
12. Why does Docker Compose reading `.env` not configure a separately launched host JVM?
13. Why is `@ConfigurationProperties` preferable for a group of settings?
14. What happens when a validated required setting is invalid?
15. Why is a profile not a safe place for committed secrets?
16. Under what conditions can Boot normally create a Hikari-backed `DataSource`?
17. Why should a repository still depend on `DataSource`?
18. Who closes the Hikari pool, and who closes a logical connection handle?
19. What does `Connection.close()` normally do when Hikari supplied the connection?
20. Why can Boot not decide whether debit and credit belong in one transaction?
21. When should application-level startup work use a runner?
22. Why should an essential runner exception escape?
23. What is the difference between a healthy dependency and a correct business operation?
24. What does an empty `@SpringBootTest` context test prove?
25. Why test the executable JAR in addition to `spring-boot:run`?
26. What should you read first when Boot startup fails?
27. Why are most negative matches in a condition report normal?
28. What resource cleanup happens when the `ApplicationContext` closes?

---

## Reflection Answer Key

1. It did not go anywhere. Boot creates, configures, and refreshes a Spring `ApplicationContext` for you.
2. Primary Boot configuration, component scanning, and enabling conditional auto-configuration.
3. Default component scanning begins there and proceeds into subpackages.
4. A `ConfigurableApplicationContext`.
5. A starter assembles dependencies; auto-configuration conditionally registers/configures beans based on the resulting application state.
6. Management selects a compatible version; declaration actually requests an artifact for the project.
7. Classpath classes, existing/missing beans, properties, resources, and application type are common examples.
8. A condition—often a missing-bean condition—tells Boot not to create its default because application configuration has taken control.
9. It preserves Boot's tested lifecycle and supporting integration while changing only the needed value.
10. `25`, because the command-line property has higher precedence in this scenario.
11. `APP_DEMO_SEEDCOUNT`: dots become underscores, dashes are removed, and letters become uppercase.
12. Compose changes configuration for its own interpolation/child containers; it cannot mutate the parent shell or an unrelated JVM process.
13. It binds related values into one typed, convertible, validatable object instead of scattering string expressions.
14. Binding/validation fails and the application context should not finish starting.
15. Profiles select committed configuration; they do not encrypt, issue, or restrict access to the file.
16. Relevant JDBC/pool classes must be present, configuration must be sufficient, and no user-defined data source can have made the default back off.
17. `DataSource` is the standard acquisition contract and avoids coupling business persistence code to Hikari-specific management APIs.
18. The Spring context closes its managed pool; the method that borrowed a connection closes its logical handle.
19. It closes/releases the logical handle and returns the underlying pool entry for reuse when healthy; it does not shut down the pool.
20. Atomicity is a business/application boundary. Classpath conditions and property values cannot infer that invariant.
21. When work must run after the full context has refreshed and dependencies are ready.
22. Propagation marks startup as failed instead of reporting false success.
23. Health reports an operational signal such as reachability; only business tests/assertions prove business invariants.
24. It proves the selected application context can start with that test configuration, not that every operation is correct.
25. The packaged artifact exercises repackaging, nested dependencies, launcher behavior, and production-like configuration boundaries.
26. The concise failure analysis and deepest relevant cause, before adding annotations or broad debug logging.
27. Boot considers many capabilities that a small application does not use; their conditions should fail.
28. Destruction callbacks run and context-owned resources such as the Hikari pool are closed.

---

## Final Mental Model

```text
My Maven build declares capabilities
        ↓
Boot dependency management aligns versions
        ↓
SpringApplication prepares configuration and starts a context
        ↓
@SpringBootApplication establishes the application root
        ├── my components are scanned
        └── Boot configurations are considered
                ↓
        conditions inspect classpath, properties, and beans
                ↓
        matching defaults are registered; my deliberate beans can cause back-off
                ↓
the Spring container creates and injects the final bean graph
        ↓
my code owns business rules and short-lived resources
        ↓
Actuator, logs, and tests provide different kinds of evidence
        ↓
context shutdown destroys managed infrastructure
```

The sentence to carry into REST is:

> Spring Boot automates application setup around the Spring container; it does not replace the container, Java resource rules, external systems, or architectural judgment.

---

## Official References for Later Lookup

- [Spring Boot reference documentation](https://docs.spring.io/spring-boot/reference/)
- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [`@SpringBootApplication`](https://docs.spring.io/spring-boot/reference/using/using-the-springbootapplication-annotation.html)
- [Structuring Spring Boot code](https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html)
- [`SpringApplication` and application events](https://docs.spring.io/spring-boot/reference/features/spring-application.html)
- [Build systems, starters, and dependency management](https://docs.spring.io/spring-boot/reference/using/build-systems.html)
- [Auto-configuration](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html)
- [Externalized configuration and property-source order](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html)
- [SQL database and `DataSource` support](https://docs.spring.io/spring-boot/reference/data/sql.html)
- [Logging](https://docs.spring.io/spring-boot/reference/features/logging.html)
- [Actuator endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- [Testing Spring Boot applications](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/maven-plugin/index.html)
- [`javax.sql.DataSource` Java 17 API](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/javax/sql/DataSource.html)
- [HikariCP configuration reference](https://github.com/brettwooldridge/HikariCP)
