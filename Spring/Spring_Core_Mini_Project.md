# Spring Core Mini Project

## `spring-core-practice` — Notification Management Application

**Estimated time:** 1–3 focused hours  
**Stack:** Java 17+, Maven, Spring Framework 6.2.x, Spring Context  
**Deliberately excluded:** Spring Boot, web/MVC, REST, databases, JPA, Security, Docker

This project exists to make Spring's object management visible. Its business behavior is intentionally tiny.

### Quick navigation

- [Target structure and priorities](#target-project-structure)
- [Tasks 1–17](#exercise-tasks--attempt-before-reading-the-solution)
- [Manual verification scenarios](#manual-verification-scenarios)
- [Complete reference solution](#complete-reference-solution)
- [Architecture, troubleshooting, and coverage](#architecture-review)
- [Checklist and reflection](#final-project-checklist)

```text
Main
  ↓ starts
ApplicationContext
  ↓ creates and wires
NotificationService
  ├── NotificationSender
  │      ├── EmailNotificationSender
  │      └── SmsNotificationSender
  ├── NotificationRepository
  │      └── InMemoryNotificationRepository
  ├── Clock (@Bean)
  └── sender name (application.properties)
```

By the end, you should be able to point at every object and answer:

- Who creates it?
- Is it a bean or an ordinary Java value?
- How does its constructor receive dependencies?
- Why does Spring choose one implementation rather than another?
- When do its lifecycle callbacks run?

## Learning contract

Each task gives requirements, incomplete starter code, and progressively stronger hints. Stop after **Hints**, implement your attempt, and use **How to verify**. The complete reference solution is intentionally near the end.

The temporary ambiguity in Task 11 is expected. A build can compile successfully while the Spring container fails at runtime; that distinction is important.

## Version note

The reference solution pins Spring Framework `6.2.19`, a stable 6.x release available when this guide was prepared, and targets Java 17. Pinning a version makes the exercise reproducible. `spring-context` brings the required core container modules transitively. `jakarta.annotation-api` is included only because Java 17 does not contain Spring 6's Jakarta lifecycle annotations.

## Target project structure

```text
spring-core-practice/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/springpractice/
        │       ├── Main.java
        │       ├── config/
        │       │   └── AppConfig.java
        │       ├── model/
        │       │   └── Notification.java
        │       ├── repository/
        │       │   ├── NotificationRepository.java
        │       │   └── InMemoryNotificationRepository.java
        │       ├── sender/
        │       │   ├── NotificationSender.java
        │       │   ├── EmailNotificationSender.java
        │       │   └── SmsNotificationSender.java
        │       └── service/
        │           └── NotificationService.java
        └── resources/
            └── application.properties
```

Package rule:

```text
src/main/java/com/example/springpractice/service/NotificationService.java
                                      ↓
package com.example.springpractice.service;
```

## Project concept priorities

| Practice area | Priority |
|---|---|
| Container, beans, IoC, constructor DI | ⭐⭐⭐⭐⭐ MUST KNOW |
| Component scanning and stereotypes | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@Configuration`, `@Bean`, resolution, interfaces | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@Primary`, `@Qualifier`, lifecycle, properties | ⭐⭐⭐⭐ IMPORTANT |
| Singleton behavior | ⭐⭐⭐⭐ IMPORTANT |
| Prototype scope and profiles | ⭐⭐⭐ NICE TO KNOW |
| Proxy internals, web scopes, advanced transactions | ⭐⭐ FUTURE KNOWLEDGE — excluded here |

---

## Exercise Tasks — Attempt Before Reading the Solution

### Task 1 — Create the Maven project structure

Objective:

Create the standard directory layout and establish the package root without writing Spring code yet.

Concept:

Maven treats `src/main/java` as the Java source root and `src/main/resources` as the classpath-resource root. Java packages begin below the source root.

What to implement:

Create a directory named `spring-core-practice` with the structure shown above. Do not create a Dockerfile or Spring Boot files.

Starter code:

From the directory where you keep practice projects:

```powershell
New-Item -ItemType Directory -Force -Path `
  .\spring-core-practice\src\main\java\com\example\springpractice\config, `
  .\spring-core-practice\src\main\java\com\example\springpractice\model, `
  .\spring-core-practice\src\main\java\com\example\springpractice\repository, `
  .\spring-core-practice\src\main\java\com\example\springpractice\sender, `
  .\spring-core-practice\src\main\java\com\example\springpractice\service, `
  .\spring-core-practice\src\main\resources

Set-Location .\spring-core-practice
```

Hints:

1. `Main.java` will later sit directly under `com/example/springpractice`.
2. Resources do not use the Java package hierarchy unless your application needs it.
3. Every public Java type must use a matching filename.

How to verify:

```powershell
Get-ChildItem -Recurse
```

You should see `src\main\java\com\example\springpractice` and `src\main\resources`. There is nothing to compile yet.

Common mistakes:

- Creating `src/java/main` instead of `src/main/java`.
- Adding `com.example.springpractice` as one directory name instead of nested directories.
- Putting `application.properties` under `src/main/java`.

Explanation:

Maven and Spring are separate. Maven defines build layout/classpaths; Spring will later scan Java packages within that layout.

---

### Task 2 — Add Spring Context to Maven

Objective:

Configure a Java 17 build and obtain only the libraries required for this Spring Core project.

Concept:

Maven coordinates download libraries. `spring-context` supplies `ApplicationContext`, annotation configuration, component scanning, and transitively its required Spring Core modules. This is not a Boot starter.

What to implement:

Create `pom.xml` with:

- coordinates `com.example:spring-core-practice:1.0.0`;
- compiler release 17;
- `org.springframework:spring-context` version `6.2.19`;
- `jakarta.annotation:jakarta.annotation-api` version `3.0.0`;
- compiler and exec plugins;
- main class `com.example.springpractice.Main`.

Starter code:

```xml
<properties>
    <maven.compiler.release>TODO</maven.compiler.release>
    <spring.version>TODO</spring.version>
</properties>

<dependencies>
    <!-- TODO: Spring Context -->
    <!-- TODO: Jakarta annotation API for lifecycle callbacks -->
</dependencies>
```

Hints:

1. A Maven dependency needs `groupId`, `artifactId`, and `version`.
2. Use `${spring.version}` in the Spring dependency.
3. The exec plugin's `<mainClass>` is a fully qualified class name, not a path.

How to verify:

After completing the POM:

```powershell
mvn --version
mvn dependency:tree
```

Maven should report Java 17 or newer and show `org.springframework:spring-context:jar:6.2.19`. It will also show transitive Spring modules. Compilation cannot prove anything yet because no Java source exists.

Common mistakes:

- Adding `spring-boot-starter`.
- Using `javax.annotation-api` with Spring 6 code.
- Running Maven outside the directory containing `pom.xml`.
- Omitting the Spring version because no Boot dependency management is present.

Explanation:

Spring Framework 6 requires Java 17+. `jakarta.annotation-api` is a necessary small API dependency for `@PostConstruct` and `@PreDestroy`; it is not a second framework.

---

### Task 3 — Create the Notification model

Objective:

Represent one sent notification as a small immutable Java value.

Concept:

Not every application object should be a Spring bean. Domain values are commonly created by business code because each value represents different data.

What to implement:

Create `model/Notification.java` as a record with:

- `String channel`;
- `String recipient`;
- `String message`;
- `Instant createdAt`.

Do not annotate it as a component.

Starter code:

```java
package com.example.springpractice.model;

import java.time.Instant;

public record Notification(
        // TODO: four components
) {
}
```

Hints:

1. A record declaration separates components with commas.
2. Import `java.time.Instant`.
3. The service will later create a new record for each send operation.

How to verify:

```powershell
mvn compile
```

Expected: `BUILD SUCCESS` and a generated `Notification.class` below `target/classes/com/example/springpractice/model`.

Common mistakes:

- Using a package that does not match the folder.
- Adding `@Component` to a per-notification data value.
- Importing a similarly named non-JDK time class.

Explanation:

Spring manages collaborating application objects, not every short-lived value. This record is intentionally an ordinary Java object.

---

### Task 4 — Define the NotificationSender interface

Objective:

Define the behavior that the service needs without choosing email or SMS yet.

Concept:

Depending on an interface creates a useful replacement point. Spring can later inject any registered implementation.

What to implement:

Create `sender/NotificationSender.java` with:

- `String channel()`;
- `void send(String senderName, Notification notification)`.

Starter code:

```java
package com.example.springpractice.sender;

import com.example.springpractice.model.Notification;

public interface NotificationSender {
    // TODO: channel method
    // TODO: send method
}
```

Hints:

1. Interface methods need no implementation body.
2. `channel()` will let the service label the saved notification.
3. Both implementations must use exactly the same method signatures.

How to verify:

```powershell
mvn compile
```

Expected: `BUILD SUCCESS`.

Common mistakes:

- Putting implementation state in the interface.
- Making email-specific methods that SMS cannot implement.
- Forgetting the `Notification` import.

Explanation:

The interface expresses behavior the service needs. It is useful because this exercise genuinely has multiple implementations; it is not an interface created only for ceremony.

---

### Task 5 — Define the repository boundary

Objective:

Separate notification storage from notification sending.

Concept:

A repository boundary keeps storage details out of the service. This project uses memory so Spring concepts remain visible, but the boundary could later have a JDBC implementation.

What to implement:

Create `repository/NotificationRepository.java` with:

- `void save(Notification notification)`;
- `List<Notification> findAll()`.

Starter code:

```java
package com.example.springpractice.repository;

// TODO: imports

public interface NotificationRepository {
    // TODO: save
    // TODO: findAll
}
```

Hints:

1. Import both `Notification` and `java.util.List`.
2. Return a list, never `null` for “no notifications.”
3. This interface receives no Spring annotation.

How to verify:

```powershell
mvn compile
```

Expected: `BUILD SUCCESS`.

Common mistakes:

- Returning the implementation's mutable internal list by contract.
- Annotating the interface and forgetting to register an implementation.
- Adding database dependencies that this exercise does not need.

Explanation:

The interface is a contract. Spring needs a concrete bean implementing it before it can satisfy a service constructor.

---

### Task 6 — Implement an in-memory `@Repository`

Objective:

Create the first scanned Spring bean and implement safe read access to stored values.

Concept:

`@Repository` is a specialized `@Component` stereotype for persistence/storage classes. Component scanning can discover it and register an instance as a bean.

What to implement:

Create `InMemoryNotificationRepository` that:

- implements `NotificationRepository`;
- is annotated `@Repository`;
- stores values in an `ArrayList`;
- returns `List.copyOf(...)` from `findAll()`.

Starter code:

```java
@Repository
public class InMemoryNotificationRepository
        implements NotificationRepository {

    private final List<Notification> notifications = /* TODO */;

    @Override
    public void save(Notification notification) {
        // TODO
    }

    @Override
    public List<Notification> findAll() {
        // TODO: return a defensive read-only copy
    }
}
```

Hints:

1. `@Repository` comes from `org.springframework.stereotype.Repository`.
2. Initialize storage with `new ArrayList<>()`.
3. `List.copyOf(notifications)` prevents callers from mutating the internal list through the returned reference.

How to verify:

```powershell
mvn compile
```

Expected: `BUILD SUCCESS`. Bean discovery cannot be verified until a context is added.

Common mistakes:

- Importing a Spring Data repository type; Spring Data is not used here.
- Returning `null` for an empty list.
- Assuming `@Repository` works without a component scan or explicit registration.

Explanation:

The class is ordinary Java plus descriptive Spring metadata. The annotation makes it a candidate; a later scan will actually discover it.

---

### Task 7 — Add the email `@Component`

Objective:

Create the first `NotificationSender` implementation as a scanned component.

Concept:

`@Component` marks a general class as a component-scan candidate. Giving it the explicit name `emailSender` makes later qualifier examples predictable.

What to implement:

Create `EmailNotificationSender` that:

- implements `NotificationSender`;
- uses `@Component("emailSender")`;
- returns `"EMAIL"` from `channel()`;
- prints sender name, recipient, and message in `send(...)`.

Starter code:

```java
@Component("emailSender")
public class EmailNotificationSender implements NotificationSender {
    // TODO: implement both methods
}
```

Hints:

1. The stereotype import is `org.springframework.stereotype.Component`.
2. Read record data with `notification.recipient()` and `notification.message()`.
3. Use `System.out.printf(...%n...)` for readable output.

How to verify:

```powershell
mvn compile
```

Expected: `BUILD SUCCESS`. It will become a bean only after Task 9 starts scanning the package.

Common mistakes:

- Naming the bean `emailSender` in one place and `emailNotificationSender` in a later qualifier.
- Creating a sender inside the service with `new`.
- Confusing a component candidate with an already running object.

Explanation:

`@Component` does not instantiate the class by itself. It provides metadata that the container's scan will use.

---

### Task 8 — Create `NotificationService` with constructor injection

Objective:

Make a service depend on abstractions and receive them through its constructor.

Concept:

`@Service` is the service-layer component stereotype. Constructor injection makes required collaborators explicit and testable. A single constructor does not need `@Autowired`.

What to implement:

Create a `@Service` named `NotificationService` with final fields for:

- `NotificationSender defaultSender`;
- `NotificationRepository repository`.

Inject both through one constructor. Add a temporary `send(...)` method that builds a `Notification` using `Instant.now()`, calls the sender with the temporary literal sender name `"Spring Practice App"`, saves the result, and returns it. Add `history()`. Task 15 will replace that literal with external configuration.

Do **not** write `new EmailNotificationSender()` or `new InMemoryNotificationRepository()`.

Starter code:

```java
@Service
public class NotificationService {
    private final NotificationSender defaultSender;
    private final NotificationRepository repository;

    public NotificationService(
            // TODO: constructor parameters
    ) {
        // TODO: assign final fields
    }

    public Notification send(String recipient, String message) {
        // TODO: create, send, save, return
    }

    public List<Notification> history() {
        // TODO
    }
}
```

Hints:

1. The constructor parameter types exactly match registered implementation interfaces.
2. `new Notification(defaultSender.channel(), recipient, message, Instant.now())` is appropriate: the record is not a bean.
3. You may temporarily add `@Autowired` to the only constructor, compile, then remove it and verify behavior remains the same.

How to verify:

```powershell
mvn compile
```

Expected: `BUILD SUCCESS`. Runtime injection will be verified after creating the context.

Common mistakes:

- Field injection instead of a constructor.
- Forgetting `final` on required dependency fields.
- Manually creating concrete collaborators inside the service.
- Believing `@Autowired` is mandatory on the only constructor.

Explanation:

The service declares what it needs. Spring will later find one sender bean and one repository bean, then call this constructor with them. You can still instantiate it manually in a plain unit test.

---

### Task 9 — Configure component scanning

Objective:

Tell a plain Spring container where to discover the project's components.

Concept:

`@Configuration` marks a Java configuration class. `@ComponentScan` defines the package tree in which Spring searches for stereotypes. Plain Spring does not infer this automatically like a typical Boot application does.

What to implement:

Create `config/AppConfig.java` with:

- `@Configuration`;
- `@ComponentScan("com.example.springpractice")`.

Starter code:

```java
package com.example.springpractice.config;

// TODO: imports

@Configuration
@ComponentScan(/* TODO: common package root */)
public class AppConfig {
}
```

Hints:

1. Both annotations come from `org.springframework.context.annotation`.
2. Scan the common root, not only the `config` package.
3. A scan includes subpackages such as `sender`, `service`, and `repository`.

How to verify:

```powershell
mvn compile
```

Expected: `BUILD SUCCESS`. Runtime discovery is verified in Task 10.

Common mistakes:

- Scanning `com.example.springpractice.config` only.
- Writing a filesystem path with slashes instead of a Java package name.
- Assuming `@Configuration` itself scans every package.

Explanation:

The configuration class supplies bean-definition instructions. Registering `AppConfig` with a context will trigger its scan directive.

---

### Task 10 — Start `ApplicationContext` manually

Objective:

Start Spring without Boot, retrieve the service bean, and prove the initial dependency graph works.

Concept:

`AnnotationConfigApplicationContext` reads annotation-based configuration. Creating it with `AppConfig.class` registers configuration, refreshes the container, scans components, constructs beans, and injects constructors.

What to implement:

Create `Main.java`. In `main`:

1. open `AnnotationConfigApplicationContext(AppConfig.class)` with try-with-resources;
2. retrieve `NotificationService` by type;
3. send one email notification;
4. print the history size;
5. let the context close.

Use `"Spring Practice App"` temporarily when calling the sender inside the service. Task 15 will replace this hard-coded value.

Starter code:

```java
package com.example.springpractice;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try (var context = /* TODO: create context from AppConfig */) {
            // TODO: retrieve NotificationService by type
            // TODO: send a notification and print history size
        }
    }
}
```

Hints:

1. Import `AnnotationConfigApplicationContext`, `AppConfig`, and `NotificationService`.
2. Use `context.getBean(NotificationService.class)`.
3. The expected graph currently has exactly one `NotificationSender` implementation.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected meaningful application output resembles:

```text
[Spring Practice App] EMAIL -> learner@example.com: Welcome to Spring Core
Saved notifications: 1
```

Surrounding Maven log lines may vary.

Common mistakes:

- Constructing `NotificationService` manually in `Main`.
- Passing `Main.class` instead of `AppConfig.class` to the context.
- Leaving the context unclosed.
- Treating a successful compile as proof that component scanning worked.

Explanation:

At runtime, Spring discovers the repository, sender, and service definitions. It creates the two dependencies first, calls the service constructor with them, and returns the ready service from `getBean`.

---

### Task 11 — Reproduce multiple-bean ambiguity

Objective:

Observe the exact failure that occurs when a constructor requests one interface but two beans implement it.

Concept:

Autowiring begins with type-compatible candidates. Once email and SMS are both beans, the unqualified `NotificationSender` parameter no longer identifies a unique object.

What to implement:

Create `SmsNotificationSender` that:

- implements `NotificationSender`;
- is annotated `@Component("smsSender")`;
- returns `"SMS"`;
- prints an SMS-style line.

Do **not** add `@Primary` or `@Qualifier` yet.

Starter code:

```java
@Component("smsSender")
public class SmsNotificationSender implements NotificationSender {
    // TODO: implement channel() and send(...)
}
```

Hints:

1. Make its method signatures identical to the interface.
2. Leave the service constructor's single sender parameter unchanged.
3. Compilation should succeed; failure should happen while the context creates beans.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected behavior:

- `mvn clean compile` succeeds;
- `mvn exec:java` fails during context startup;
- the cause mentions an unsatisfied/non-unique dependency and two candidates similar to `emailSender` and `smsSender`.

This is a **successful experiment** if ambiguity is the cause.

Common mistakes:

- Thinking a runtime context failure means Java compilation failed.
- “Fixing” it by changing the field type to `EmailNotificationSender`, which couples the service to one concrete class.
- Catching and hiding the startup exception.

Explanation:

Spring knows both beans satisfy the Java type but cannot invent your business preference. You must express a default or a specific qualifier.

---

### Task 12 — Select the default with `@Primary`

Objective:

Resolve the unqualified sender dependency by declaring email as the default candidate.

Concept:

`@Primary` tells Spring to prefer one bean when several type-compatible candidates remain and the injection point does not specify a qualifier.

What to implement:

Add `@Primary` to `EmailNotificationSender`. Change nothing else, then rerun.

Starter code:

```java
@Component("emailSender")
// TODO: mark this candidate as the default
public class EmailNotificationSender implements NotificationSender {
    // existing implementation
}
```

Hints:

1. Import `org.springframework.context.annotation.Primary`.
2. Do not mark both senders primary.
3. The service may continue requesting only `NotificationSender` by type.

How to verify:

```powershell
mvn compile exec:java
```

Expected: the context starts again and the default `send(...)` call prints `EMAIL`, not `SMS`.

Common mistakes:

- Importing an unrelated `Primary` annotation.
- Marking both implementations.
- Assuming `@Primary` means the SMS bean no longer exists.

Explanation:

Both beans remain in the context. `@Primary` changes candidate preference for an otherwise unqualified injection; it does not delete or disable the other bean.

---

### Task 13 — Select SMS with `@Qualifier`

Objective:

Keep a default sender and also request the non-default SMS bean explicitly.

Concept:

`@Qualifier` narrows candidates at one injection point. It expresses a local selection, while `@Primary` supplies a general default.

What to implement:

Evolve `NotificationService` so its constructor receives:

- unqualified `NotificationSender defaultSender` (resolved by `@Primary`);
- `@Qualifier("smsSender") NotificationSender smsSender`;
- the repository.

Add `sendWithDefault(...)` and `sendSms(...)` methods that share a private creation method. Call both from `Main`.

Starter code:

```java
public NotificationService(
        NotificationSender defaultSender,
        @Qualifier(/* TODO */) NotificationSender smsSender,
        NotificationRepository repository
) {
    // TODO: assign all fields
}
```

Hints:

1. Import `org.springframework.beans.factory.annotation.Qualifier`.
2. The qualifier text must match the component name exactly.
3. Pass a sender into a private `createAndSend(...)` method to avoid duplicating logic.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected meaningful output includes one `EMAIL` line, one `SMS` line, and `Saved notifications: 2`.

Common mistakes:

- Qualifying the field while the real injection point is a differently declared constructor parameter and using inconsistent metadata.
- Writing `@Qualifier("SmsNotificationSender")` when the bean is named `smsSender`.
- Removing `@Primary` and leaving the default parameter ambiguous.

Explanation:

Spring first finds beans assignable to `NotificationSender`. The unqualified parameter selects the primary email bean; the qualified parameter narrows selection to the SMS bean.

---

### Task 14 — Register a JDK object with `@Bean`

Objective:

Register an object whose source you cannot annotate and inject it into the service.

Concept:

`@Bean` marks a configuration factory method. Spring calls the method and manages its returned object. This complements component scanning.

What to implement:

In `AppConfig`:

- add an `@Bean` method named `applicationClock`;
- return `Clock.systemUTC()`.

In `NotificationService`:

- add `Clock` as a constructor dependency;
- replace `Instant.now()` with `clock.instant()`.

Starter code:

```java
@Bean
public Clock applicationClock() {
    // TODO: return a UTC clock
}
```

Hints:

1. Import `java.time.Clock` and `org.springframework.context.annotation.Bean`.
2. `Clock` cannot be edited to add `@Component`; it belongs to the JDK.
3. Add it to the constructor exactly like any scanned bean dependency.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Also retrieve it in `Main`:

```java
Clock clock = context.getBean(Clock.class);
System.out.println("Clock bean zone: " + clock.getZone());
```

Expected zone for the configured clock: `Z`.

Common mistakes:

- Calling `applicationClock()` manually in `Main`.
- Forgetting that `@Bean` methods belong in registered configuration.
- Keeping `Instant.now()` so the new dependency is unused.

Explanation:

The clock and scanned service are both beans even though they were registered differently. Spring resolves by type across both registration sources.

---

### Task 15 — Load and inject an external property

Objective:

Move the sender name out of Java source and into a classpath configuration file.

Concept:

Plain Spring does not automatically treat `application.properties` as Boot does. `@PropertySource` registers the file. `@Value` asks Spring to resolve a property into an injection point. A placeholder configurer makes `${...}` processing explicit and fail-fast in this project.

What to implement:

1. Create `src/main/resources/application.properties` with `notification.sender-name=Spring Practice App`.
2. Add `@PropertySource("classpath:application.properties")` to `AppConfig`.
3. Add a static `PropertySourcesPlaceholderConfigurer` `@Bean`.
4. Add `@Value("${notification.sender-name}") String senderName` to the service constructor.
5. Replace the temporary hard-coded sender name.

Starter code:

```properties
notification.sender-name=TODO
```

```java
@Configuration
@ComponentScan("com.example.springpractice")
@PropertySource(/* TODO */)
public class AppConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer
            propertySourcesPlaceholderConfigurer() {
        // TODO
    }
}
```

```java
public NotificationService(
        // existing dependencies,
        @Value(/* TODO placeholder */) String senderName
) {
    // TODO
}
```

Hints:

1. `@Value` comes from `org.springframework.beans.factory.annotation.Value`.
2. The placeholder syntax is `${key}`.
3. The configurer factory method is `static` so this post-processor can be created early.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected: output includes `[Spring Practice App]`. Change the property value to `My Notification Lab`, then rerun `mvn compile exec:java` so Maven recopies the resource; confirm output changes without changing Java.

Common mistakes:

- Placing the file outside `src/main/resources`.
- Writing `$notification.sender-name` instead of `${notification.sender-name}`.
- Using `@Value` from an unrelated package.
- Assuming Boot-style property loading in a plain context.

Explanation:

The classpath file becomes a property source. Before bean construction, Spring resolves the constructor's placeholder and supplies the resulting string like any other argument.

---

### Task 16 — Observe lifecycle and bean scopes

Objective:

Prove singleton reuse, prototype creation, initialization, and orderly destruction through observable behavior.

Concept:

The default Spring scope is singleton: one instance per bean definition per context. Prototype creates a new instance for each direct request. `@PostConstruct` runs after injection; `@PreDestroy` runs when the singleton is destroyed as the context closes.

What to implement:

1. Add `@PostConstruct` and `@PreDestroy` methods to `NotificationService`.
2. Print `NotificationService initialized: <senderName>` and `NotificationService destroyed`.
3. Retrieve `NotificationService` twice in `Main` and compare with `==`.
4. Add a prototype `StringBuilder` bean named `notificationDraft` in `AppConfig`.
5. Retrieve it twice by name/type and compare with `==`.

Starter code:

```java
@PostConstruct
public void initialize() {
    // TODO
}

@PreDestroy
public void destroy() {
    // TODO
}
```

```java
@Bean
@Scope(/* TODO: prototype constant */)
public StringBuilder notificationDraft() {
    return new StringBuilder();
}
```

Hints:

1. Lifecycle imports are `jakarta.annotation.PostConstruct` and `jakarta.annotation.PreDestroy`.
2. Use `ConfigurableBeanFactory.SCOPE_PROTOTYPE` rather than a misspelled string.
3. Call `context.getBean("notificationDraft", StringBuilder.class)` twice.

How to verify:

```powershell
mvn clean compile
mvn exec:java
```

Expected meaningful lines:

```text
NotificationService initialized: Spring Practice App
Same NotificationService bean: true
Same prototype draft bean: false
NotificationService destroyed
```

The destruction line must appear after application work when the try-with-resources block closes.

Common mistakes:

- Importing `javax.annotation`.
- Expecting prototype objects to be destroyed automatically by Spring.
- Believing singleton implies JVM-global or automatically thread-safe.
- Calling `System.exit(...)` before orderly context closure.

Explanation:

The service is a shared singleton inside this one context. Two prototype lookups cause two factory calls. The project intentionally keeps lifecycle callbacks on the singleton because Spring does not manage the complete destruction lifecycle of prototypes.

---

### Task 17 — Run the final integration and explain the graph

Objective:

Integrate every exercise, verify output, and explain the complete construction flow without relying on the phrase “Spring does it automatically.”

Concept:

The context builds a dependency graph from scanned components, explicit bean methods, qualifiers, primary metadata, and properties. Successful integration requires both valid Java code and resolvable runtime configuration.

What to implement:

Complete `Main` so it:

1. starts `AnnotationConfigApplicationContext` with `AppConfig`;
2. verifies singleton service identity;
3. sends one default email and one explicit SMS;
4. prints saved history size;
5. retrieves the `Clock` bean;
6. verifies prototype draft identity;
7. closes the context naturally.

Starter code:

```java
public static void main(String[] args) {
    try (var context =
             new AnnotationConfigApplicationContext(AppConfig.class)) {
        // TODO: two service lookups
        // TODO: singleton comparison
        // TODO: email and SMS calls
        // TODO: history size
        // TODO: Clock lookup
        // TODO: two prototype lookups and comparison
    }
}
```

Hints:

1. Ask the context for top-level objects; do not manually reconstruct the graph.
2. Exact timestamps are intentionally not printed, so output remains stable.
3. The `@PreDestroy` line appears only after leaving the block.

How to verify:

```powershell
mvn clean compile
mvn dependency:tree
mvn exec:java
mvn clean package
Get-ChildItem .\target
```

Expected application behavior:

```text
NotificationService initialized: Spring Practice App
Same NotificationService bean: true
[Spring Practice App] EMAIL -> learner@example.com: Welcome to Spring Core
[Spring Practice App] SMS -> +15551234567: Constructor injection works
Saved notifications: 2
Clock bean zone: Z
Same prototype draft bean: false
NotificationService destroyed
```

Expected Maven artifact: `target\spring-core-practice-1.0.0.jar`.

Common mistakes:

- Running with the intentionally ambiguous Task 11 state still present.
- Adding both senders as primary.
- Forgetting the property placeholder configurer or resource.
- Expecting this normal JAR to be a Spring Boot executable JAR.

Explanation:

Spring creates the repository, two sender beans, `Clock`, and service. Email is chosen for the unqualified parameter because it is primary; SMS is chosen for the qualified parameter. The service is initialized once and reused. Domain notifications are created by service code. Context closure destroys the managed singleton service.

---

## Stop Here and Build Your Version

Do not continue until you have attempted Tasks 1–17. Your implementation does not need identical formatting, but it should produce the same dependency graph and observable behavior.

---

## Manual Verification Scenarios

Run these against your attempted implementation before comparing it with the reference solution. “Expected result” means meaningful application output, not an exact copy of Maven's surrounding log.

### Scenario 1 — ApplicationContext starts successfully

Input/action:

Run `mvn compile exec:java` with the final, unambiguous configuration.

Expected result:

The context starts and the first application message is the initialization callback. No bean-creation exception appears.

What concept it proves:

Java configuration is registered, the component scan works, and the complete dependency graph is resolvable.

PASS condition:

Business output appears and the Maven command finishes successfully.

FAIL symptoms:

`NoSuchBeanDefinitionException` suggests a missing registration/scan; `UnsatisfiedDependencyException` suggests a missing or ambiguous constructor dependency.

### Scenario 2 — NotificationService is discovered

Input/action:

Call `context.getBean(NotificationService.class)`.

Expected result:

A non-null service is returned without manually invoking its constructor.

What concept it proves:

`@Service` is discovered through component scanning and becomes a bean.

PASS condition:

The lookup succeeds by type.

FAIL symptoms:

No bean of that type exists; inspect `@Service`, the package declaration, and `@ComponentScan` root.

### Scenario 3 — Constructor dependencies are injected

Input/action:

Use the retrieved service to send and store one notification.

Expected result:

The sender prints a line and `history().size()` increases without a `NullPointerException`.

What concept it proves:

Spring supplied the sender, repository, clock, and property value when calling the constructor.

PASS condition:

One service call uses all collaborators successfully; no service field was manually assigned in `Main`.

FAIL symptoms:

A null field, manual setter call, or internal `new EmailNotificationSender()` means constructor DI was not implemented correctly.

### Scenario 4 — Email sender works

Input/action:

Call `sendWithDefault("learner@example.com", "Welcome to Spring Core")`.

Expected result:

Output contains `EMAIL`, the email recipient, and message.

What concept it proves:

The unqualified `NotificationSender` resolves to the primary email bean.

PASS condition:

The returned/saved notification has channel `EMAIL`.

FAIL symptoms:

SMS output, ambiguous startup, or no saved notification; inspect `@Primary` and `createAndSend`.

### Scenario 5 — SMS sender works

Input/action:

Call `sendSms("+15551234567", "Constructor injection works")`.

Expected result:

Output contains `SMS`, the phone number, and message.

What concept it proves:

The explicitly qualified non-primary implementation is injected and usable.

PASS condition:

The returned/saved notification has channel `SMS`.

FAIL symptoms:

Email output or startup failure; inspect `@Qualifier("smsSender")` and the SMS component's bean name.

### Scenario 6 — Multiple-bean ambiguity can be reproduced

Input/action:

Temporarily remove only `@Primary` from email. Keep both sender components and leave the service's unqualified `defaultSender` plus qualified `smsSender` parameters intact. Then rebuild and run:

```powershell
mvn compile exec:java
```

Expected result:

Context startup fails with a cause that identifies two sender candidates. Restore your final code immediately afterward.

What concept it proves:

Type resolution cannot choose arbitrarily between multiple compatible beans.

PASS condition:

Compilation succeeds but runtime context creation fails specifically because two candidates exist.

FAIL symptoms:

The context starts because another qualifier/primary remains, or failure concerns unrelated compilation/configuration.

### Scenario 7 — `@Qualifier` resolves a specific dependency

Input/action:

First restore `@Primary` on email. To make the qualifier's effect visible, temporarily remove `@Qualifier("smsSender")` from the second sender parameter, run `mvn compile exec:java`, and observe that both parameters resolve to the primary email bean. Restore `@Qualifier("smsSender")`, rebuild, and run again.

Expected result:

Without the qualifier, the method misleadingly uses email. With the qualifier restored, the SMS method uses `SmsNotificationSender` even though email is primary.

What concept it proves:

A qualifier narrows one injection point to the intended candidate.

PASS condition:

After restoration, context startup succeeds and the SMS-specific output is produced.

FAIL symptoms:

Qualifier name mismatch, missing SMS component, stale classes from skipping `compile`, or the restored SMS call still prints email.

### Scenario 8 — `@Primary` supplies the default

Input/action:

Keep email marked `@Primary` and observe the unqualified `defaultSender` through `sendWithDefault(...)`.

Expected result:

The default operation uses email.

What concept it proves:

Primary selection supplies a preference only when the injection point does not demand another qualifier.

PASS condition:

Default is email while qualified SMS still works.

FAIL symptoms:

Ambiguity, two primary candidates, or default SMS behavior.

### Scenario 9 — `@Bean`-created object is retrievable

Input/action:

Call `context.getBean(Clock.class)` and print `getZone()`.

Expected result:

The lookup succeeds and prints `Z` for the UTC clock.

What concept it proves:

An object returned by a processed `@Bean` method is a managed bean even though its class has no component annotation.

PASS condition:

Exactly one `Clock` candidate is retrieved and it is also injected into the service.

FAIL symptoms:

No clock bean, multiple clock beans, or the service still calls `Instant.now()` directly.

### Scenario 10 — `@PostConstruct` runs

Input/action:

Start the application and observe output before the first business call.

Expected result:

`NotificationService initialized: Spring Practice App` appears once before send output.

What concept it proves:

Initialization occurs after construction/property injection and before the bean is used.

PASS condition:

The callback runs once and can read the injected sender name.

FAIL symptoms:

No message, a null/unresolved value, legacy `javax.annotation` import, or a manually created service.

### Scenario 11 — `@PreDestroy` runs on close

Input/action:

Let execution leave the context's try-with-resources block normally.

Expected result:

`NotificationService destroyed` appears after the final in-block operation.

What concept it proves:

The container controls orderly singleton destruction.

PASS condition:

The destruction callback appears once during close.

FAIL symptoms:

The context was not closed, the process was forcibly terminated, the service is unmanaged, or the annotation import is wrong.

### Scenario 12 — Singleton lookups reuse one instance

Input/action:

Retrieve `NotificationService` twice and print `(first == second)`.

Expected result:

`Same NotificationService bean: true`.

What concept it proves:

Singleton is the default scope for that bean definition in one context.

PASS condition:

Identity comparison is `true`.

FAIL symptoms:

The class or bean is marked prototype, different contexts were used, or one object was constructed manually.

### Scenario 13 — Prototype lookups create two instances

Input/action:

Retrieve `notificationDraft` twice from the same context and compare with `==`.

Expected result:

`Same prototype draft bean: false`.

What concept it proves:

A direct request for a prototype bean creates a new instance.

PASS condition:

Identity comparison is `false`.

FAIL symptoms:

Missing `@Scope`, wrong bean name, or comparing the same variable twice.

### Scenario 14 — External property is loaded

Input/action:

Run `mvn compile exec:java` once with `notification.sender-name=Spring Practice App`, change only the value to `My Notification Lab`, then run `mvn compile exec:java` again. The `compile` phase also processes the changed resource into `target/classes`.

Expected result:

Both initialization and sender output use the changed value on the second run.

What concept it proves:

Configuration is supplied from a classpath property source instead of being hard-coded in the service.

PASS condition:

The output changes without editing Java source.

FAIL symptoms:

An unresolved placeholder, unchanged hard-coded output, wrong resource location, or a property key mismatch.

---

## Before You Reveal the Solution

- [ ] I attempted the temporary ambiguity and understood why compilation still passed.
- [ ] I can draw the final dependency graph from memory.
- [ ] I can explain why `Notification` is not a bean.
- [ ] I can explain why a `Clock` returned by `@Bean` is a bean.
- [ ] I know which sender is chosen by `@Primary` and which by `@Qualifier`.
- [ ] I closed the context and observed destruction.

---

## Complete Reference Solution

Compare this with your version only after attempting the tasks. Small naming or formatting differences are fine if your dependency graph and behavior are equivalent.

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>spring-core-practice</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring.version>6.2.19</spring.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <dependency>
            <groupId>jakarta.annotation</groupId>
            <artifactId>jakarta.annotation-api</artifactId>
            <version>3.0.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.15.0</version>
                <configuration>
                    <release>17</release>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.6.3</version>
                <configuration>
                    <mainClass>com.example.springpractice.Main</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Dependency explanation:

| Entry | Why it exists |
|---|---|
| `spring-context` | Provides the application context, annotation configuration, scanning, and required core Spring modules transitively |
| `jakarta.annotation-api` | Provides `@PostConstruct` and `@PreDestroy` for Java 17/Spring 6 code |
| compiler plugin | Compiles the project with Java release 17 |
| exec plugin | Runs the ordinary main class with its Maven dependency classpath |

No Spring Boot dependency or plugin is present. The two plugin entries are build tools, not application libraries.

### `src/main/resources/application.properties`

```properties
notification.sender-name=Spring Practice App
```

### `src/main/java/com/example/springpractice/model/Notification.java`

```java
package com.example.springpractice.model;

import java.time.Instant;

public record Notification(
        String channel,
        String recipient,
        String message,
        Instant createdAt
) {
}
```

Each `Notification` is an ordinary immutable domain value. It is deliberately not a Spring bean.

### `src/main/java/com/example/springpractice/sender/NotificationSender.java`

```java
package com.example.springpractice.sender;

import com.example.springpractice.model.Notification;

public interface NotificationSender {

    String channel();

    void send(String senderName, Notification notification);
}
```

### `src/main/java/com/example/springpractice/sender/EmailNotificationSender.java`

```java
package com.example.springpractice.sender;

import com.example.springpractice.model.Notification;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("emailSender")
@Primary
public class EmailNotificationSender implements NotificationSender {

    @Override
    public String channel() {
        return "EMAIL";
    }

    @Override
    public void send(String senderName, Notification notification) {
        System.out.printf(
                "[%s] EMAIL -> %s: %s%n",
                senderName,
                notification.recipient(),
                notification.message()
        );
    }
}
```

Email is primary, so it becomes the default for an unqualified `NotificationSender` parameter.

### `src/main/java/com/example/springpractice/sender/SmsNotificationSender.java`

```java
package com.example.springpractice.sender;

import com.example.springpractice.model.Notification;
import org.springframework.stereotype.Component;

@Component("smsSender")
public class SmsNotificationSender implements NotificationSender {

    @Override
    public String channel() {
        return "SMS";
    }

    @Override
    public void send(String senderName, Notification notification) {
        System.out.printf(
                "[%s] SMS -> %s: %s%n",
                senderName,
                notification.recipient(),
                notification.message()
        );
    }
}
```

### `src/main/java/com/example/springpractice/repository/NotificationRepository.java`

```java
package com.example.springpractice.repository;

import com.example.springpractice.model.Notification;

import java.util.List;

public interface NotificationRepository {

    void save(Notification notification);

    List<Notification> findAll();
}
```

### `src/main/java/com/example/springpractice/repository/InMemoryNotificationRepository.java`

```java
package com.example.springpractice.repository;

import com.example.springpractice.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InMemoryNotificationRepository
        implements NotificationRepository {

    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public void save(Notification notification) {
        notifications.add(notification);
    }

    @Override
    public List<Notification> findAll() {
        return List.copyOf(notifications);
    }
}
```

The `ArrayList` is acceptable for this single-threaded learning program. It is not presented as production concurrent storage.

### `src/main/java/com/example/springpractice/service/NotificationService.java`

```java
package com.example.springpractice.service;

import com.example.springpractice.model.Notification;
import com.example.springpractice.repository.NotificationRepository;
import com.example.springpractice.sender.NotificationSender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationSender defaultSender;
    private final NotificationSender smsSender;
    private final NotificationRepository repository;
    private final Clock clock;
    private final String senderName;

    public NotificationService(
            NotificationSender defaultSender,
            @Qualifier("smsSender") NotificationSender smsSender,
            NotificationRepository repository,
            Clock clock,
            @Value("${notification.sender-name}") String senderName
    ) {
        this.defaultSender = defaultSender;
        this.smsSender = smsSender;
        this.repository = repository;
        this.clock = clock;
        this.senderName = senderName;
    }

    @PostConstruct
    public void initialize() {
        System.out.println(
                "NotificationService initialized: " + senderName
        );
    }

    public Notification sendWithDefault(
            String recipient,
            String message
    ) {
        return createAndSend(defaultSender, recipient, message);
    }

    public Notification sendSms(
            String recipient,
            String message
    ) {
        return createAndSend(smsSender, recipient, message);
    }

    public List<Notification> history() {
        return repository.findAll();
    }

    private Notification createAndSend(
            NotificationSender sender,
            String recipient,
            String message
    ) {
        Notification notification = new Notification(
                sender.channel(),
                recipient,
                message,
                clock.instant()
        );

        sender.send(senderName, notification);
        repository.save(notification);
        return notification;
    }

    @PreDestroy
    public void destroy() {
        System.out.println("NotificationService destroyed");
    }
}
```

Notice that the only constructor has no `@Autowired`; Spring automatically treats it as the injection constructor.

### `src/main/java/com/example/springpractice/config/AppConfig.java`

```java
package com.example.springpractice.config;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.time.Clock;

@Configuration
@ComponentScan("com.example.springpractice")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer
            propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public Clock applicationClock() {
        return Clock.systemUTC();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StringBuilder notificationDraft() {
        return new StringBuilder();
    }
}
```

`Clock` and `StringBuilder` are JDK classes that cannot be edited with your component annotations. Their `@Bean` factory methods make the returned objects managed beans. The static placeholder-configurer bean processes `${...}` values explicitly.

### `src/main/java/com/example/springpractice/Main.java`

```java
package com.example.springpractice;

import com.example.springpractice.config.AppConfig;
import com.example.springpractice.service.NotificationService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Clock;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try (var context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            NotificationService first =
                    context.getBean(NotificationService.class);
            NotificationService second =
                    context.getBean(NotificationService.class);

            System.out.println(
                    "Same NotificationService bean: " + (first == second)
            );

            first.sendWithDefault(
                    "learner@example.com",
                    "Welcome to Spring Core"
            );

            first.sendSms(
                    "+15551234567",
                    "Constructor injection works"
            );

            System.out.println(
                    "Saved notifications: " + first.history().size()
            );

            Clock clock = context.getBean(Clock.class);
            System.out.println("Clock bean zone: " + clock.getZone());

            StringBuilder draftA =
                    context.getBean(
                            "notificationDraft",
                            StringBuilder.class
                    );
            StringBuilder draftB =
                    context.getBean(
                            "notificationDraft",
                            StringBuilder.class
                    );

            System.out.println(
                    "Same prototype draft bean: " + (draftA == draftB)
            );
        }
    }
}
```

### Run the solution

```powershell
Set-Location .\spring-core-practice
mvn clean compile
mvn dependency:tree
mvn exec:java
mvn clean package
```

Expected meaningful output:

```text
NotificationService initialized: Spring Practice App
Same NotificationService bean: true
[Spring Practice App] EMAIL -> learner@example.com: Welcome to Spring Core
[Spring Practice App] SMS -> +15551234567: Constructor injection works
Saved notifications: 2
Clock bean zone: Z
Same prototype draft bean: false
NotificationService destroyed
```

Maven produces `target\spring-core-practice-1.0.0.jar`. It is an ordinary Maven JAR, not a Boot executable/fat JAR. Use `mvn exec:java` for this exercise; do not assume `java -jar` includes dependencies or a main manifest.

---

## Application Startup — What Spring Does

```text
new AnnotationConfigApplicationContext(AppConfig.class)
        ↓
register AppConfig and refresh
        ↓
read @ComponentScan and @PropertySource
        ↓
discover component bean definitions
        ↓
register @Bean definitions
        ↓
create dependencies in graph order
        ↓
resolve constructor parameters
        ↓
call NotificationService constructor
        ↓
run @PostConstruct
        ↓
getBean(...) returns ready managed service
        ↓
application performs work
        ↓
close context → @PreDestroy
```

This differs from Spring Boot startup in visibility and convenience. Here you explicitly choose the context class, configuration class, component scan, property source, dependency versions, and execution method. Boot later supplies an application launcher, auto-configuration, starters, and property conventions—but it still creates an `ApplicationContext` and manages beans using these same Spring Framework ideas.

---

## Architecture Review

### Runtime view

```text
Main
    ↓ starts and queries
Spring Container
    ↓ returns
NotificationService
    ↓ delegates sending
NotificationSender
    ├── EmailNotificationSender (@Primary)
    └── SmsNotificationSender (@Qualifier target)
```

### Storage view

```text
NotificationService
    ↓ depends on interface
NotificationRepository
    ↓ implemented by
InMemoryNotificationRepository
```

### Complete bean graph

```text
ApplicationContext
├── appConfig
├── emailSender (@Component, @Primary)
├── smsSender (@Component)
├── inMemoryNotificationRepository (@Repository)
├── applicationClock (@Bean, singleton)
├── notificationDraft (@Bean, prototype)
└── notificationService (@Service, singleton)
      ├── defaultSender ──→ emailSender
      ├── smsSender ──────→ smsSender
      ├── repository ─────→ inMemoryNotificationRepository
      ├── clock ──────────→ applicationClock
      └── senderName ─────→ property value

Ordinary values, not beans:
└── Notification records created for each operation
```

### Responsibilities

| Type | Responsibility |
|---|---|
| `Main` | Bootstrap the context, obtain the top-level service, demonstrate behavior, close the context |
| `AppConfig` | Define scan/property boundaries and explicitly construct JDK beans |
| `NotificationService` | Coordinate the use case: construct, send, and store a notification |
| `NotificationSender` | Define a replaceable delivery behavior |
| Email/SMS senders | Implement channel-specific output |
| `NotificationRepository` | Define storage behavior |
| In-memory repository | Store notifications for this learning process |
| `Notification` | Carry one notification's data; remain framework-independent |

No SQL, menu handling, or container setup is hidden in `NotificationService`. Each type has one clear reason to change.

---

## Optional Focused Extensions

These are optional and should not replace the working final solution.

### Profile practice

**Priority: ⭐⭐⭐ NICE TO KNOW**

Create two simple implementations of a new `DeliveryPolicy` interface:

```java
@Component
@Profile("development")
class PermissiveDeliveryPolicy implements DeliveryPolicy { /* ... */ }

@Component
@Profile("production")
class StrictDeliveryPolicy implements DeliveryPolicy { /* ... */ }
```

Start a manually configured context so the profile is chosen before refresh:

```java
try (var context = new AnnotationConfigApplicationContext()) {
    context.getEnvironment().setActiveProfiles("development");
    context.register(AppConfig.class);
    context.refresh();
    // retrieve DeliveryPolicy
}
```

This practices `@Profile` without introducing a database or web layer. Remove the extension afterward if it distracts from the core graph.

### Plain-Java testability practice

Create tiny fake implementations of `NotificationSender` and `NotificationRepository`, then construct `NotificationService` directly in a separate scratch class. You will need to pass every constructor dependency explicitly. This proves constructor injection improves testability without requiring Spring or an added testing library.

---

## Troubleshooting Guide

| Symptom | Likely cause | Inspect |
|---|---|---|
| `mvn` is not recognized | Maven is not installed or not on `PATH` | `mvn --version`, Maven installation |
| `release version 17 not supported` | Maven is using an older JDK | `mvn --version` and `JAVA_HOME` |
| `package org.springframework... does not exist` | Dependency coordinates/version are wrong or Maven did not resolve them | `pom.xml`, `mvn dependency:tree` |
| `package jakarta.annotation does not exist` | Missing Jakarta annotation API dependency | `pom.xml` |
| No `NotificationService` bean | Scan root/package/`@Service` mismatch | `AppConfig`, service package declaration |
| Required one bean but found two | Both senders match and selection metadata is missing | `@Primary`, constructor `@Qualifier` |
| Qualifier finds no matching bean | Qualifier text differs from component/qualifier value | `"smsSender"` spelling |
| `${notification.sender-name}` prints literally or fails | Property source/configurer/key is wrong | `AppConfig`, resource path, property key |
| Initialization does not print | Unmanaged service or wrong annotation import | Lookup/construction path, `jakarta.annotation` import |
| Destruction does not print | Context did not close orderly | try-with-resources in `Main` |
| Singleton comparison is false | Different contexts, prototype scope, or manual construction | Both lookup lines and bean scope |
| Prototype comparison is true | Missing `@Scope` or same reference compared twice | `notificationDraft` bean and lookups |
| `mvn package` works but `java -jar` does not | This is not an executable fat JAR with dependencies/main manifest | Run with `mvn exec:java` |

When diagnosing, read the **deepest cause** in the exception chain. Spring often wraps a precise bean-resolution error inside a broader bean-creation error.

---

## Guide-to-Project Coverage Map

“Direct” means you implement/observe it here. “Conceptual” means the guide teaches it, but adding its infrastructure would violate this project's intentionally small scope.

| Spring Essentials concept | Project evidence | Coverage |
|---|---|---|
| IoC and container | Manual `AnnotationConfigApplicationContext` startup | Direct |
| Bean versus normal object | Managed service/senders versus `Notification` values | Direct |
| Constructor DI | `NotificationService` constructor | Direct |
| Component scanning | `@ComponentScan` over the package root | Direct |
| `@Component` | Email and SMS senders | Direct |
| `@Service` | `NotificationService` | Direct |
| `@Repository` | In-memory repository | Direct |
| `@Autowired` rule | Temporarily add/remove it from the only constructor | Direct |
| `@Configuration` / `@Bean` | `AppConfig`, `Clock`, prototype draft | Direct |
| Interface substitution | Sender and repository interfaces | Direct |
| Ambiguity | Intentional Task 11 startup failure | Direct |
| `@Primary` | Default email candidate | Direct |
| `@Qualifier` | Explicit SMS constructor parameter | Direct |
| Singleton/prototype | Identity checks | Direct |
| Lifecycle | Initialization and destruction output | Direct |
| External properties | `@PropertySource` and constructor `@Value` | Direct |
| Profiles | Optional `DeliveryPolicy` extension | Optional direct practice |
| Layered responsibilities | Main/config/service/sender/repository/model packages | Direct |
| Spring JDBC / `JdbcTemplate` | Explained in the guide; no database added | Conceptual by design |
| Transactions / `@Transactional` | Explained using your raw JDBC foundation | Conceptual by design |
| AOP/proxies | Runtime prediction/reflection only | Conceptual by design |
| Controller/MVC/REST | Deliberately excluded until the next learning stage | Future |
| Data JPA/Hibernate/Security | Deliberately excluded | Future |

The conceptual rows are not omissions. Practicing them would require extra modules and business infrastructure that would hide this project's core goal: container-managed object composition.

---

## Final Project Checklist

- [ ] Maven resolves Spring dependencies.
- [ ] Maven uses Java release 17 or newer.
- [ ] `ApplicationContext` starts.
- [ ] Beans are discovered.
- [ ] Constructor injection works.
- [ ] I understand who creates each object.
- [ ] `@Component` works.
- [ ] `@Service` works.
- [ ] `@Repository` works.
- [ ] `@Configuration` works.
- [ ] `@Bean` works.
- [ ] `@Qualifier` works.
- [ ] `@Primary` works.
- [ ] Bean lifecycle callbacks work.
- [ ] Singleton scope behavior is understood.
- [ ] Prototype scope behavior is understood.
- [ ] Configuration properties are externalized.
- [ ] I can explain the dependency graph.
- [ ] I can explain how this differs from manual `new`.
- [ ] I can explain how this differs from Spring Boot.
- [ ] The final state has no unresolved bean ambiguity.
- [ ] The context closes normally.

---

## Reflection Questions

Answer without looking below.

1. Who creates `NotificationService`?
2. Who calls its constructor?
3. Where does each `NotificationSender` constructor argument come from?
4. Why is `NotificationService` a bean while each `Notification` record is normally not?
5. What would happen if you manually used `new NotificationService(...)`?
6. Why does constructor injection improve testability?
7. What happens if two beans implement `NotificationSender` and neither is distinguished?
8. When should you use `@Bean` instead of `@Component`?
9. What does `ApplicationContext` contain/manage conceptually?
10. Why are singleton beans reused, and does reuse guarantee thread safety?
11. What happens when the context is closed?
12. What work would Spring Boot remove or simplify in this project?

---

## Reflection Answer Key

1. The Spring container creates `NotificationService` because component scanning registers its `@Service` class as a bean definition.
2. The container selects and calls its only constructor while building the dependency graph.
3. The unqualified argument comes from the primary email bean; the qualified argument comes from the bean identified by `smsSender`.
4. The service is registered and lifecycle-managed by the context. Each record represents new operation data and is constructed by business code, so it need not be managed.
5. You would create a valid ordinary Java object if all arguments were supplied, but it would not automatically receive container injection, lifecycle callbacks, or eligible proxy behavior.
6. Every required collaborator is explicit, so a test can pass small fake implementations without reflection, field mutation, or a Spring context.
7. Context startup fails with a non-unique/ambiguous dependency because type matching leaves two candidates.
8. Use `@Bean` when you cannot or should not annotate the class, or when construction needs explicit configuration. `Clock` is the example here.
9. It maintains bean definitions, managed instances, dependency relationships, environment/property information, and lifecycle/infrastructure services.
10. Singleton is the default efficient shared scope per bean definition per context. It does not make mutable shared state thread-safe.
11. Managed destruction callbacks such as the singleton service's `@PreDestroy` method run during orderly close.
12. Boot would provide a standard launcher, dependency starters/version management, automatic configuration, automatic property conventions, and other defaults. The underlying context, bean resolution, DI, and lifecycle ideas remain.

---

## Final Mental Model

```text
Your classes declare:
“These are my components and these are my constructor needs.”
        ↓
Configuration tells Spring where/how to obtain candidates.
        ↓
ApplicationContext builds a valid object graph.
        ↓
Your code asks for a top-level service and performs ordinary Java calls.
        ↓
The context manages lifecycle until it closes.
```

If you can explain every arrow without saying only “magic” or “automatic,” the project has achieved its purpose and you are ready to see how Spring Boot simplifies the same startup process.

## Official references for later lookup

- [Spring Framework 6.2 — The IoC Container](https://docs.spring.io/spring-framework/reference/6.2/core/beans.html)
- [Spring Framework 6.2 — Java-based Container Configuration](https://docs.spring.io/spring-framework/reference/6.2/core/beans/java.html)
- [Spring Framework 6.2 — Annotation-based Container Configuration](https://docs.spring.io/spring-framework/reference/6.2/core/beans/annotation-config.html)
