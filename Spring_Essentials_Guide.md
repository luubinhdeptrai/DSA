# Spring Essentials Guide

> A focused Spring Core guide for a Java developer who already knows Maven, SQL, PostgreSQL, and raw JDBC.

This guide teaches the small set of Spring concepts that makes most later Spring Boot code understandable. It uses Spring Framework 6.x terminology and Java 17+ examples. It does **not** teach Spring Boot implementation, MVC, REST, JPA, or security yet.

### Quick navigation

- [Foundations: Spring, IoC, DI, container, and beans](#1-what-spring-is)
- [Registration and wiring: scanning through bean resolution](#6-component-scanning)
- [Scope, lifecycle, configuration, layers, JDBC, and transactions](#11-bean-scope)
- [AOP, proxies, annotation reference, and runtime synthesis](#20-aop--basic-concept)
- [Priorities, exercises, checklist, and answer key](#26-priority-levels--what-to-study-deeply)

## How to use this guide

1. Read Sections 1–10 slowly; they contain the central mental model.
2. Type the short exercises instead of only reading them.
3. Answer each checkpoint without looking at the answer key.
4. Build the companion project in `Spring_Core_Mini_Project.md`.
5. Use Sections 22, 23, 26, and 30 for review.

### Priority legend

| Marker | Meaning | Study approach |
|---|---|---|
| ⭐⭐⭐⭐⭐ **MUST KNOW** | Used constantly | Explain it and write it without notes |
| ⭐⭐⭐⭐ **IMPORTANT** | Common and important | Understand it and recognize normal usage |
| ⭐⭐⭐ **NICE TO KNOW** | Helpful context | Know the purpose; details can wait |
| ⭐⭐ **FUTURE KNOWLEDGE** | Deliberately postponed | Recognize the name only for now |

---

## The high-level mental model

### Without Spring

```text
Java application
      ↓
classes call new themselves
      ↓
objects choose and create their own dependencies
      ↓
wiring is scattered through the application
      ↓
replacing or testing dependencies becomes harder
```

### With Spring

```text
Java application starts
      ↓
Spring container reads configuration
      ↓
creates application objects
      ↓
connects their dependencies
      ↓
manages their lifecycle
      ↓
application asks for and uses the ready object graph
```

The one sentence to keep throughout the guide is:

> Instead of my classes manually creating and wiring their dependencies, the Spring container creates, configures, injects, and manages the application's objects.

This does **not** mean that `new` is bad or that Spring creates every object. Domain values such as `new Student(...)` are normally still created by your code. Spring primarily manages long-lived collaborating application objects such as services, repositories, and configuration objects.

---

## 1. What Spring Is

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### Definition

Spring Framework is a modular Java framework that supplies infrastructure for building applications. Its foundation is an **IoC container**: a system that creates selected Java objects, connects them, configures them, and manages them.

Spring is not a programming language, JVM, server, database, or IDE. Your code is still Java and runs on the JVM.

### Why it exists

In a growing Java application, manual object wiring spreads everywhere:

```java
StudentRepository repository = new JdbcStudentRepository(dataSource);
StudentService service = new StudentService(repository);
StudentController controller = new StudentController(service);
```

This is manageable for three objects. It becomes repetitive when there are hundreds, several environments, transactions, and cross-cutting concerns. Spring centralizes that infrastructure while your classes remain ordinary Java classes.

### What “Spring Core” means

In everyday learning, Spring Core means the foundation around:

- the IoC container;
- beans and dependency injection;
- Java/annotation configuration;
- resource and property handling;
- bean scopes and lifecycle;
- the basic AOP/proxy mechanism used by features such as transactions.

The underlying modules include `spring-core`, `spring-beans`, `spring-context`, and Spring Expression. Depending on `spring-context` through Maven brings its required core modules transitively.

### Spring Framework versus Spring Boot

| Spring Framework | Spring Boot |
|---|---|
| Provides the container, DI, transactions, MVC, JDBC support, and other foundations | Builds on Spring Framework |
| You can create and configure the context manually | Starts and configures a typical application with convenient defaults |
| Usually requires more explicit setup | Adds auto-configuration, starters, externalized configuration conveniences, and operational features |
| Can be used without Boot | Cannot replace the underlying Framework concepts |

```text
Your application
      ↓
Spring Boot conveniences       ← learn later
      ↓
Spring Framework container     ← learn now
      ↓
Java, JDBC, HTTP libraries, database, and other systems
```

Spring became common in Java backend work because it encourages loosely coupled code and offers consistent infrastructure for data access, transactions, web applications, testing, and more.

### Not needed yet

Do not study Spring MVC implementation, REST controllers, Spring Data JPA, Hibernate, Security, WebFlux, Cloud, or Boot auto-configuration internals yet. They make more sense once the container and DI are clear.

**Mental model:** Spring Framework is a toolbox; the container is its object factory and wiring system; Spring Boot is a convenient application setup built on that toolbox.

**Common mistake:** Saying “Spring and Spring Boot are the same.” Boot uses Spring Framework, but they are different layers.

**Remember:** Spring Core is mainly about who creates and connects important application objects.

### Checkpoint 1

1. What work does the Spring container perform?
2. Is every object created with `new` a Spring bean?
3. Does Spring Boot replace Spring Framework?

### Practice prompt — Find the wiring

Look at a small Java program you already wrote. Circle every place where a service or DAO is created with `new`. Those lines are the **manual composition/wiring** that a Spring container could centralize.

---

## 2. Inversion of Control — IoC

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### Definition

**Inversion of Control (IoC)** means control over creating and connecting selected application objects moves from your application code to a container.

### Normal control

```java
public class StudentService {
    private final StudentRepository repository;

    public StudentService() {
        this.repository = new JdbcStudentRepository();
    }
}
```

`StudentService` decides:

- which repository implementation to use;
- when to construct it;
- how to configure it.

### Inverted control

```java
public class StudentService {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }
}
```

Now another party supplies the repository. In a Spring application, that party is normally the Spring container.

```text
Before: StudentService → creates → JdbcStudentRepository

After:  Spring → creates repository
               → creates service
               → gives repository to service
```

It is called an **inversion** because the direction of control changed. Your service no longer controls construction of its collaborator; it declares what it needs.

**Mental model:** A class submits an ingredient list; the container assembles the meal.

**Common mistake:** Treating IoC as a special Java syntax. It is a design principle. Spring is one implementation of it.

**Remember:** IoC answers, “Who controls creation and wiring?” With Spring, the container does for managed objects.

---

## 3. Dependency Injection — DI

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### Definition

A **dependency** is an object another object needs to do its work. If `StudentService` calls a `StudentRepository`, the repository is a dependency of the service.

**Dependency Injection (DI)** is the act of supplying that dependency from outside instead of having the class construct it internally.

```text
IoC = the broad change in who controls wiring
DI  = the usual technique Spring uses to perform that wiring
```

### Why DI reduces coupling

Without DI, the service is tied to a concrete class and its construction details:

```java
public class StudentService {
    private final JdbcStudentRepository repository =
            new JdbcStudentRepository(DatabaseConfig.createConnection());
}
```

With DI, the service depends only on the behavior it requires:

```java
public class StudentService {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }
}
```

That makes replacement and testing straightforward:

```java
StudentRepository fake = new InMemoryStudentRepository();
StudentService service = new StudentService(fake);
```

The class can still be used without Spring, which is a sign of healthy design.

### Three injection styles

| Style | Example | Assessment |
|---|---|---|
| Constructor | `StudentService(StudentRepository r)` | Preferred for required dependencies |
| Setter | `setRepository(StudentRepository r)` | Possible for a reconfigurable dependency; optionality is separate |
| Field | `@Autowired private StudentRepository r;` | Usually discouraged |

In that field example, `@Autowired` is Spring metadata asking the container to inject a matching dependency into the marked member. Section 8 explains its resolution rules and why constructor injection usually needs no such annotation.

Constructor injection is normally preferred because:

- required dependencies are explicit;
- fields can be `final`;
- an invalid half-created object is harder to make;
- tests can construct the class directly;
- it avoids hidden framework-only mutation.

Setter injection example:

```java
public void setAuditLogger(AuditLogger auditLogger) {
    this.auditLogger = auditLogger;
}
```

Field injection example to recognize, not copy as your default:

```java
@Autowired
private StudentRepository repository;
```

**Mental model:** IoC chooses the assembler; DI is the assembler passing each constructor argument.

**Common mistake:** Thinking DI requires interfaces. DI works with concrete classes too. Interfaces are useful when there is a meaningful abstraction or replaceable implementation.

**Remember:** Prefer a constructor for dependencies that the object cannot work without.

### Checkpoint 2

1. What is the dependency in `StudentService(StudentRepository repository)`?
2. How does constructor injection improve a unit test?
3. Why is field injection less explicit?

### Practice prompt — Preview Exercise A

Refactor this class so that `ReportWriter` arrives through its constructor. This previews Exercise A in Section 28:

```java
class ReportService {
    private final ReportWriter writer = new FileReportWriter();
}
```

Do this with plain Java first. Spring will later call the same constructor.

---

## 4. Spring Container

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### Definition

The **Spring IoC container** holds bean definitions, creates beans, resolves their dependencies, applies framework behavior, and controls relevant lifecycle callbacks.

`BeanFactory` is the basic container contract. `ApplicationContext` extends that idea with features applications commonly need, such as events, resource loading, messages, environment/profiles, and convenient annotation support. In normal application code, use `ApplicationContext`.

### Manual startup without Spring Boot

```java
try (AnnotationConfigApplicationContext context =
         new AnnotationConfigApplicationContext(AppConfig.class)) {

    StudentService service = context.getBean(StudentService.class);
    service.enroll("Linh");
}
```

`AnnotationConfigApplicationContext` is an `ApplicationContext` implementation designed for annotation-based Java configuration.

### Startup flow

```text
Application starts
    ↓
create AnnotationConfigApplicationContext
    ↓
register/read configuration
    ↓
scan configured packages
    ↓
register bean definitions (recipes/metadata)
    ↓
instantiate non-lazy singleton beans
    ↓
resolve and inject dependencies
    ↓
run initialization callbacks
    ↓
application retrieves/uses beans
    ↓
close context → destruction callbacks
```

A **bean definition** is metadata telling Spring how to obtain and configure an object. It is not necessarily the object itself.

**Mental model:** `ApplicationContext` is a registry plus an object factory plus a lifecycle coordinator.

**Common mistake:** Calling `context.getBean(...)` the moment a bean is created. Most singleton beans are normally created during context initialization; `getBean` often retrieves an existing managed instance.

**Remember:** Create the context, let it assemble the object graph, use a top-level bean, and close the context.

---

## 5. Spring Beans

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### Definition

A **Spring bean** is an object whose creation and/or lifecycle is managed by a Spring container.

```text
ordinary Java object ≠ automatically a Spring bean

Java object + registered with ApplicationContext + managed by it = Spring bean
```

Objects become beans commonly through:

- component scanning (`@Component` marks a general candidate; `@Service` specializes it for service-layer intent);
- an `@Bean` method whose returned object is registered by Spring configuration;
- explicit programmatic registration;
- XML configuration (historical and still supported, but not the focus here).

### Bean names and dependencies

Spring assigns each bean a name. A scanned `StudentService` normally becomes `studentService`. A bean returned by this method normally has the name `clock`:

```java
@Bean
Clock clock() {
    return Clock.systemUTC();
}
```

A bean dependency is another bean needed to create or operate it. Spring builds an object graph from these relationships.

```text
studentService bean
    └── depends on studentRepository bean
            └── may depend on DataSource bean
```

### Basic lifecycle

```text
definition → instantiation → injection → initialization → ready → destruction
```

Destruction is meaningful when the context closes and for scopes whose lifecycle Spring fully manages.

**Common mistake:** Creating a component manually and expecting Spring features:

```java
StudentService service = new StudentService(repository); // ordinary object
```

This object was not obtained from the container. Spring will not automatically inject it, call its managed lifecycle, or wrap it with a transaction proxy.

**Remember:** “Bean” describes the object's relationship with a particular Spring container, not a special kind of Java class.

### Checkpoint 3

1. What turns a normal Java object into a Spring bean?
2. What is the difference between a bean definition and a bean instance?
3. Why might `new SomeService(...)` bypass Spring behavior?

---

## 6. Component Scanning

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Component scanning asks Spring to search selected packages for annotated classes and register them as bean definitions.

```java
@Configuration
@ComponentScan("com.example.school")
public class AppConfig {
}
```

Here `@Configuration` marks `AppConfig` as Java bean configuration, and `@ComponentScan` tells Spring which package tree to search. Section 9 explains Java configuration more fully.

Spring scans that package and its subpackages.

### Stereotype annotations

```text
                 @Component
                     ↑
        ┌────────────┼─────────────┐
     @Service    @Repository    @Controller
```

The more specific annotations are themselves component stereotypes.

| Annotation | Intended meaning | Typical location |
|---|---|---|
| `@Component` | General Spring-managed component | Utility/infrastructure class |
| `@Service` | Business/application operation | Service layer |
| `@Repository` | Data access component | Repository/DAO layer |
| `@Controller` | Web request-handling component | MVC/web layer; concept only for now |

All four make a class discoverable by component scanning. The specific annotation communicates architectural intent; `@Repository` also participates in Spring's persistence-exception translation infrastructure.

### Why package structure matters

```text
com.example.school
├── config/AppConfig.java       ← scans com.example.school
├── service/StudentService.java ← found
└── repository/JdbcStudentRepository.java ← found

com.other.LegacyService.java    ← not under the scan root; not found
```

Package declarations must match source paths as normal Java requires. The scan base must include your components.

**Common mistake:** Annotating a class correctly but scanning the wrong parent package.

**Remember:** An annotation marks a candidate; the scan determines whether Spring discovers it.

### Practice prompt — Classify components

Choose the best stereotype for `PasswordEncoder`, `EnrollmentService`, `JdbcStudentRepository`, and a future `StudentController`. Explain why the specific name communicates more than using `@Component` everywhere.

---

## 7. Constructor Injection

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Consider these complete components:

```java
package com.example.school.repository;

import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {
    public void save(String name) {
        System.out.println("Saved " + name);
    }
}
```

```java
package com.example.school.service;

import com.example.school.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public void enroll(String name) {
        repository.save(name);
    }
}
```

### Exact runtime flow

1. Component scanning finds `StudentRepository` and `StudentService`.
2. Spring registers a bean definition for each.
3. Spring creates the `StudentRepository` bean.
4. Spring inspects the only constructor of `StudentService`.
5. It sees a required parameter of type `StudentRepository`.
6. It searches its registered beans for a compatible type.
7. It selects the repository bean.
8. **Spring calls** `new StudentService(repositoryBean)` internally.
9. The resulting service object becomes a managed bean.

```text
Spring container
  ├── creates StudentRepository ───────┐
  └── calls StudentService(repository) │
                         ↑─────────────┘
```

The argument does not appear from nowhere: it comes from the container's bean registry.

**Common mistake:** Thinking the annotation itself calls the constructor. The annotation is metadata; the container reads it and performs the work.

**Remember:** Your constructor declares the dependency contract. Spring resolves a bean and calls that constructor.

### Checkpoint 4

1. Who creates `StudentRepository`?
2. Who calls the `StudentService` constructor?
3. Where does its argument come from?
4. Could you construct `StudentService` yourself in a unit test?

---

## 8. `@Autowired`

**Priority: ⭐⭐⭐⭐ IMPORTANT**

`@Autowired` marks a constructor, method, or field as an injection point that Spring should satisfy.

### Single constructor

Since Spring Framework 4.3, a bean with one constructor does not need `@Autowired` on that constructor:

```java
@Service
public class StudentService {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }
}
```

Spring sees the single constructor, resolves all parameters from the container, and invokes it.

### Multiple constructors

If there are multiple constructors and Spring cannot infer which one should be used, `@Autowired` can identify the intended injection constructor. Prefer designs with one clear required-dependency constructor.

### Setter injection

```java
@Autowired
public void setAuditLogger(AuditLogger auditLogger) {
    this.auditLogger = auditLogger;
}
```

Use setter style for a dependency that is genuinely reconfigurable or for a carefully designed optional collaboration, not merely to avoid writing a constructor. The setter form does **not** make the dependency optional by itself: `@Autowired` is required by default. True optional injection needs explicit semantics, such as `Optional<AuditLogger>` or `@Autowired(required = false)`; keep required dependencies in the constructor.

### Field injection

```java
@Autowired
private StudentRepository repository;
```

It works, but it hides the dependency from the constructor, prevents a `final` field, and makes plain unit construction awkward. Recognize legacy code that uses it; prefer constructor injection in new code.

### How resolution happens conceptually

```text
injection point says: “I require StudentRepository”
      ↓
container finds compatible bean candidates by type
      ↓
zero candidates → unsatisfied dependency error
one candidate  → inject it
many candidates → use qualifier/primary/name rules or fail as ambiguous
```

**Common mistake:** Saying `@Autowired` creates a dependency. It identifies an injection point; the dependency must also be registered or otherwise obtainable as a bean.

**Remember:** A single constructor normally needs no annotation. Constructor injection is the technique; `@Autowired` is optional metadata in that common case.

---

## 9. `@Configuration` and `@Bean`

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Component scanning works when you own a class and can annotate it. `@Configuration` plus `@Bean` gives explicit factory-style registration.

```java
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public Clock applicationClock() {
        return Clock.systemUTC();
    }
}
```

```text
Spring reads @Configuration
      ↓
discovers @Bean method
      ↓
calls the method when that bean instance is created
      ↓
registers/manages the returned Clock
```

The default bean name is the method name, here `applicationClock`.

### Dependencies in `@Bean` methods

Declare dependencies as parameters:

```java
@Bean
public ReportScheduler reportScheduler(Clock applicationClock) {
    return new ReportScheduler(applicationClock);
}
```

Spring resolves the `Clock` bean and supplies it, just as it does for a component constructor.

### Component scanning versus `@Bean`

| Component scanning | `@Configuration` + `@Bean` |
|---|---|
| Put a stereotype on the class | Put a factory method in configuration |
| Good for your services/repositories | Good for third-party or JDK classes |
| Registration is discovered | Registration is explicit |
| Configuration is close to the class | Construction details are centralized |

Use `@Bean` for objects you cannot annotate (such as `Clock`), objects requiring explicit construction, or integration configuration. Do not turn every simple component into a factory method without a reason.

**Common mistake:** Calling an `@Bean` method yourself and assuming any returned object is managed. It is Spring's processing of the configuration that registers the result.

**Remember:** `@Component` marks the class; `@Bean` marks a method whose returned object becomes a bean.

### Practice prompt — Preview Exercise D

Register `java.time.Clock.systemUTC()` as a bean. Inject `Clock` into a service constructor and use `clock.instant()` instead of calling `Instant.now()` directly. A structured version appears as Exercise D in Section 28.

---

## 10. Bean Resolution

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

When Spring sees this constructor:

```java
StudentService(StudentRepository repository)
```

it first looks for beans assignable to `StudentRepository`.

### One candidate

```text
StudentRepository interface
      ↑
JdbcStudentRepository bean
```

There is one compatible bean, so Spring injects it.

### Multiple candidates

```java
public interface NotificationSender {
    void send(String message);
}

@Component("emailSender")
class EmailSender implements NotificationSender { /* ... */ }

@Component("smsSender")
class SmsSender implements NotificationSender { /* ... */ }
```

```text
NotificationSender
├── emailSender bean
└── smsSender bean

Which one should Spring inject?
```

Without more information, startup normally fails with a “required a single bean, but 2 were found” error.

### `@Primary`

Mark the general default:

```java
@Primary
@Component("emailSender")
class EmailSender implements NotificationSender { /* ... */ }
```

An unqualified `NotificationSender` injection now chooses email when it is the only primary candidate.

### `@Qualifier`

Select deliberately at an injection point:

```java
public NotificationService(
        @Qualifier("smsSender") NotificationSender sender) {
    this.sender = sender;
}
```

`@Qualifier` is more explicit for that particular dependency. It narrows candidate selection; it is not merely “string-based magic” replacing the Java type.

| Tool | Meaning |
|---|---|
| `@Primary` | “Prefer this candidate by default.” |
| `@Qualifier("smsSender")` | “For this injection point, select the candidate with this qualifier.” |

**Common mistake:** Adding `@Primary` to both implementations. That recreates ambiguity.

**Remember:** Type determines candidates; qualifier/primary metadata chooses among multiple candidates.

### Checkpoint 5

1. What happens when no bean matches a required constructor parameter?
2. What happens when two beans match and neither is distinguished?
3. How do `@Primary` and `@Qualifier` differ?

### Practice prompt — Preview Exercise C

Create `EmailSender` and `SmsSender` beans implementing `NotificationSender`. Inject the interface without a qualifier and observe startup failure. Then make email primary and request SMS explicitly using a qualifier. The failure is part of the lesson; Exercise C in Section 28 formalizes it.

---

## 11. Bean Scope

**Priority: ⭐⭐⭐⭐ IMPORTANT**

A scope answers: **How many instances does the container create, and how long are they associated with that scope?**

### Singleton scope

`singleton` is Spring's default scope.

```text
one bean definition
      ↓
one shared bean instance per Spring container
```

```java
StudentService a = context.getBean(StudentService.class);
StudentService b = context.getBean(StudentService.class);
System.out.println(a == b); // normally true
```

A Spring singleton is **one instance per bean definition per container**. It is not necessarily the classic Singleton design pattern, which often enforces one JVM-wide instance through static code. Two different application contexts can each hold their own singleton bean.

Singleton beans are shared, so avoid unsafe mutable request-specific state in them. A service should not keep “current student” in a mutable field.

### Prototype scope

Spring creates a new instance each time that prototype bean is requested or injected during creation:

```java
@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
NotificationDraft notificationDraft() {
    return new NotificationDraft();
}
```

```java
NotificationDraft a = context.getBean(NotificationDraft.class);
NotificationDraft b = context.getBean(NotificationDraft.class);
System.out.println(a == b); // false
```

Spring creates and configures a prototype but does not manage its complete destruction lifecycle. Also, injecting a prototype once into a singleton normally gives that singleton one prototype instance at construction time; dynamic lookup requires an additional technique that can wait.

### Web scopes

`request` and `session` associate instances with an HTTP request or session. They require a web-aware context and are **⭐⭐ FUTURE KNOWLEDGE**.

**Mental model:** Singleton is a shared tool in one workshop; prototype is a newly issued tool each time you ask the storeroom.

**Common mistake:** Equating “singleton” with thread-safe. Scope does not make mutable code safe.

**Remember:** Default is one shared bean instance per container. Use prototype only when a real per-instance need exists.

---

## 12. Bean Lifecycle

**Priority: ⭐⭐⭐⭐ IMPORTANT**

### Useful lifecycle

```text
bean definition
    ↓
instantiation (constructor)
    ↓
dependency injection
    ↓
initialization callback
    ↓
ready for callers
    ↓
context closes
    ↓
destruction callback
```

With Spring 6 and Java 17+, lifecycle annotations come from `jakarta.annotation`:

```java
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class StudentService {
    @PostConstruct
    void initialize() {
        System.out.println("StudentService initialized");
    }

    @PreDestroy
    void destroy() {
        System.out.println("StudentService destroyed");
    }
}
```

- `@PostConstruct` runs after Spring has supplied dependencies, before normal use.
- `@PreDestroy` runs during orderly destruction of a fully managed bean, commonly when the context closes.

In a plain Java 17 Maven project, include `jakarta.annotation-api`; these annotations are no longer bundled in the JDK.

Good uses include validating configuration, opening/closing a component-owned resource, or logging lifecycle boundaries. Avoid putting large business workflows in lifecycle methods.

```java
try (AnnotationConfigApplicationContext context =
         new AnnotationConfigApplicationContext(AppConfig.class)) {
    // use beans
} // close() happens here, enabling destruction callbacks
```

**Common mistakes:** Forgetting to close a manually created context; using `javax.annotation` with modern Spring code; expecting `@PreDestroy` on prototype beans to be called automatically.

**Remember:** Constructor first, injection next, initialization after that; destruction requires an orderly managed shutdown.

### Checkpoint 6

1. When does `@PostConstruct` run relative to injection?
2. Why use try-with-resources for `AnnotationConfigApplicationContext`?
3. Does Spring automatically destroy every prototype instance?

### Practice prompt — Preview Exercise E

Add one `@PostConstruct` and one `@PreDestroy` message to a service. Start and close its context. Write down the order of constructor, initialization, business method, and destruction output. Continue with Exercise E in Section 28.

---

## 13. Configuration and Properties

**Priority: ⭐⭐⭐⭐ IMPORTANT**

Values that differ by environment should not be buried in Java source. External configuration lets the same compiled code use different settings. A classpath properties file is external to Java source, though it is commonly copied into the JAR; Spring Boot later makes configuration supplied from outside the packaged application much more convenient.

`src/main/resources/application.properties`:

```properties
school.display-name=Spring Practice School
```

Load it in a non-Boot Spring application:

```java
@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
}
```

### `@Value`

```java
@Service
public class BannerService {
    private final String displayName;

    public BannerService(@Value("${school.display-name}") String displayName) {
        this.displayName = displayName;
    }
}
```

A default value is possible:

```java
@Value("${school.display-name:Unnamed School}")
```

### `Environment`

Use `Environment` when code needs programmatic lookup:

```java
@Bean
Banner banner(Environment environment) {
    String name = environment.getRequiredProperty("school.display-name");
    return new Banner(name);
}
```

`@Value` is concise for a small number of values. `Environment` makes lookup explicit. Spring Boot later offers richer type-safe grouped configuration binding.

Plain Spring can resolve placeholders through its environment. For a fail-fast standalone setup—so a misspelled `${...}` key stops startup—you can register a static `PropertySourcesPlaceholderConfigurer` bean, as the companion mini-project does.

**Common mistakes:** Assuming a plain Spring application automatically loads a file because it is named `application.properties`; misspelling a key; committing credentials; hard-coding environment-specific values.

**Remember:** Put the file on the classpath, load it explicitly in Spring Core, then inject or query values.

---

## 14. Profiles

**Priority: ⭐⭐⭐ IMPORTANT**

Profiles let selected bean definitions exist only when a named environment is active.

Typical environments need different collaborators or settings:

| Environment | Typical need |
|---|---|
| Development | Fast local/in-memory tools, verbose diagnostics, developer-safe endpoints |
| Testing | Deterministic fakes/test resources and isolated configuration |
| Production | Real infrastructure, hardened settings, production credentials supplied externally |

```java
public interface StudentRepository { /* ... */ }

@Repository
@Profile("development")
class InMemoryStudentRepository implements StudentRepository { /* ... */ }

@Repository
@Profile("production")
class DatabaseStudentRepository implements StudentRepository { /* ... */ }
```

```text
development profile → in-memory implementation registered
production profile  → database implementation registered
```

With manual context construction, activate a profile before refresh:

```java
try (AnnotationConfigApplicationContext context =
         new AnnotationConfigApplicationContext()) {
    context.getEnvironment().setActiveProfiles("development");
    context.register(AppConfig.class);
    context.refresh();
}
```

Profiles can also be activated through the `spring.profiles.active` property. They are useful for coarse environment choices. Do not use them to create an unmanageable maze of small conditionals.

**Common mistake:** Activating a profile after the context has already refreshed; then bean definitions have already been selected.

**Remember:** A profile controls whether a bean definition is registered for a given environment.

### Checkpoint 7

1. Why does plain Spring need `@PropertySource` in this example?
2. When would `Environment` be clearer than `@Value`?
3. At what point must a profile be active?

### Practice prompt — Preview Exercise I

Create two `GreetingSource` implementations with `@Profile("development")` and `@Profile("production")`. Activate one before `refresh()` and verify only the corresponding bean exists. Exercise I in Section 28 gives the corresponding implementation challenge.

---

## 15. Interfaces and Loose Coupling

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Spring applications often place an interface between a consumer and replaceable infrastructure:

```text
StudentService
      ↓ depends on
StudentRepository interface
      ↑
      ├── InMemoryStudentRepository
      └── JdbcStudentRepository
```

```java
public class StudentService {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }
}
```

Benefits:

- a test can supply an in-memory/fake repository;
- JDBC can later be replaced without rewriting service logic;
- callers depend on required behavior, not construction details;
- layers have clearer responsibilities.

Spring does not remove the need to design the interface. It only selects and injects an implementation.

Do not automatically create `UserService` plus `UserServiceImpl` when there is only one class and no meaningful boundary. Interfaces are most valuable at replacement points, external boundaries, or when multiple implementations make sense.

**Common mistake:** Believing annotations create loose coupling while the service still imports and constructs a concrete implementation.

**Remember:** Depend on a stable behavior boundary when replacement has real value; do not create interfaces as ceremony.

---

## 16. Layered Architecture

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

```text
Controller
    ↓ calls
Service
    ↓ calls
Repository
    ↓ accesses
Database
```

| Layer/type | Responsibility | Should not own |
|---|---|---|
| Controller | Convert an incoming request into a service call and a response | SQL or core business rules |
| Service | Coordinate use cases, rules, and transaction boundaries | HTTP details or raw UI input |
| Repository/DAO | Store and retrieve domain data | Presentation/menu logic |
| Model/domain | Represent business data and behavior | Container bootstrapping |
| Configuration | Declare infrastructure and bean construction | Normal business operations |

`@Controller` is only a conceptual marker here. Spring MVC and REST endpoint implementation come later.

Layering is not “one annotation per folder.” It is separation of reasons to change:

- a database query change should stay in the repository;
- an enrollment rule should stay in the service;
- a future HTTP input change should stay in the controller.

**Common mistake:** Moving all logic into a service solely to make a controller short, including SQL and formatting. Each layer should own the right kind of logic.

**Remember:** Controllers adapt input/output, services implement use cases, and repositories handle persistence.

### Practice prompt — Preview Exercise G

Place each operation in a layer: validate enrollment capacity, execute `SELECT`, parse a future HTTP path variable, map a result row, and choose a transaction boundary. Exercise G in Section 28 continues the same classification.

---

## 17. `@Repository` and Exception Translation

**Priority: ⭐⭐⭐⭐ IMPORTANT**

`@Repository` declares that a component performs persistence work. It communicates intent and makes the class discoverable.

Spring provides a common unchecked `DataAccessException` hierarchy so calling code does not need to understand every database/vendor exception type. For example, `JdbcTemplate` directly translates SQL exceptions into this hierarchy.

For other persistence technologies, annotation-driven exception translation requires the appropriate Spring translation infrastructure (commonly a persistence exception translation post-processor). Therefore, do not memorize the inaccurate rule “adding `@Repository` changes every exception by itself.”

```text
vendor-specific persistence exception
      ↓ appropriate Spring data-access infrastructure
DataAccessException subtype
      ↓
service handles a technology-neutral category if needed
```

**Why useful:** Services can reason about categories such as duplicate data or unavailable resources without being tightly coupled to one driver's exception classes.

**Common mistake:** Catching `Exception` in every repository method and returning fake success or `null`. Preserve failure information and choose handling at the proper boundary.

**Remember:** `@Repository` identifies the layer; actual translation is supplied by the relevant Spring data-access infrastructure.

---

## 18. Spring JDBC Relationship

**Priority: ⭐⭐⭐⭐ IMPORTANT**

Your raw JDBC knowledge remains valuable. Spring JDBC uses JDBC underneath and removes repeated resource/error boilerplate.

### Raw JDBC

```text
obtain Connection
    ↓
create PreparedStatement
    ↓
bind parameters
    ↓
execute
    ↓
iterate ResultSet and map rows
    ↓
close ResultSet / statement / connection
    ↓
translate or propagate SQLException
```

### With `JdbcTemplate`

```java
String sql = "SELECT id, name, email FROM students WHERE id = ?";

Student student = jdbcTemplate.queryForObject(
        sql,
        (rs, rowNum) -> new Student(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email")),
        id);
```

```text
your SQL + parameters + row mapping
      ↓
JdbcTemplate
      ↓
JDBC Connection / PreparedStatement / ResultSet
      ↓
resource cleanup and exception translation
```

Spring JDBC does not replace SQL, transaction reasoning, or row mapping. It gives those responsibilities a safer, less repetitive template.

`JdbcTemplate` is provided by the `spring-jdbc` module; depending on `spring-context` alone does not add Spring's JDBC API.

**Common mistake:** Assuming `JdbcTemplate` is an ORM. It is a JDBC abstraction; you still write SQL.

**Remember:** Raw JDBC explains what happens; `JdbcTemplate` automates the repetitive mechanics.

### Checkpoint 8

1. Why can `StudentService` depend on `StudentRepository` without knowing whether storage is in memory or JDBC?
2. Which layer should own SQL and row mapping, and which layer normally owns the business use case?
3. Does `@Repository` alone translate every possible persistence exception?
4. What JDBC responsibilities does `JdbcTemplate` reduce, and what must you still provide?

---

## 19. Transactions in Spring

**Priority: ⭐⭐⭐⭐ IMPORTANT**

You already know a JDBC transaction:

```text
connection.setAutoCommit(false)
      ↓
execute related operations on that connection
      ↓
success → commit()
failure → rollback()
```

Spring can express the boundary declaratively:

```java
@Transactional
public void transfer(long fromId, long toId, BigDecimal amount) {
    accountRepository.debit(fromId, amount);
    accountRepository.credit(toId, amount);
}
```

Conceptual flow:

```text
caller
  ↓
Spring transaction proxy
  ↓ starts/binds transaction
real transfer method
  ↓
returns normally → commit
throws matching failure → rollback
```

A **transaction boundary** identifies which operations must succeed or fail as one unit. In typical Spring JDBC setup, transaction-aware infrastructure makes repository operations inside the method participate in the same transaction.

`@Transactional` is provided by the `spring-tx` module and requires setup: an appropriate `PlatformTransactionManager` and transaction annotation processing (for example `@EnableTransactionManagement`, or Boot's later auto-configuration). The annotation by itself is only metadata.

By default, Spring's declarative transactions roll back for unchecked `RuntimeException` and `Error`, not every checked exception. Propagation, isolation, timeouts, read-only hints, and custom rollback rules are **⭐⭐ FUTURE KNOWLEDGE**.

One useful caveat to recognize now: in the common proxy model, a call from one method to another method on the same object (`this.otherMethod()`) does not pass through the proxy, so the second method's transaction annotation may not be applied independently.

**Common mistake:** Catching a runtime failure inside the transactional method and returning success, which can prevent the proxy from seeing a failure that should trigger rollback.

**Remember:** `@Transactional` declares the boundary; a configured Spring proxy and transaction manager perform begin/commit/rollback.

### Checkpoint 9

1. What raw JDBC work does Spring transaction management coordinate?
2. Does `@Transactional` work merely because the import compiles?
3. What failures trigger rollback by default?

---

## 20. AOP — Basic Concept

**Priority: ⭐⭐⭐ NICE TO KNOW**

**Aspect-Oriented Programming (AOP)** separates behavior that cuts across many business methods.

```text
business logic: enroll student / transfer money / send notification
cross-cutting:  transactions / logging / security / timing
```

Without separation, every service repeats infrastructure code around its actual work. Spring AOP can place extra behavior around selected bean method calls.

Terms to recognize:

| Term | Beginner meaning |
|---|---|
| Aspect | A module describing a cross-cutting concern |
| Advice | The extra action run before, after, or around a call |
| Pointcut | A rule selecting eligible method executions/join points that receive advice |
| Proxy | The wrapper object that intercepts calls and delegates to the real bean |

For transactions, the advice begins a transaction, calls the real method, then commits or rolls back.

**Common mistake:** Mixing Spring AOP with JavaScript-oriented or general aspect terminology and assuming every method call is intercepted. Only eligible calls through a configured proxy are affected.

**Remember:** For now, AOP explains how Spring adds reusable behavior around bean calls without copying that behavior into every method.

---

## 21. Spring Proxy Concept

**Priority: ⭐⭐⭐⭐ IMPORTANT**

A proxy is an object presented to a caller in place of the target bean:

```text
Caller
  ↓ invokes method
Spring proxy
  ↓ starts transaction / logs / checks rule
Real service bean
  ↓ returns or throws
Spring proxy
  ↓ commits / rolls back / completes extra behavior
Caller receives result
```

The caller often does not need to know whether `context.getBean(StudentService.class)` returned the raw object or an eligible proxy around it. It calls the same business interface or method.

This explains two important rules:

1. Obtain managed services from the container; an object constructed with `new` is not the configured proxy.
2. Calls generally need to enter through the proxy for proxy-based features such as ordinary `@Transactional` interception.

JDK dynamic proxies versus class-based proxies and detailed AOP pointcut syntax are **⭐⭐ FUTURE KNOWLEDGE**.

**Mental model:** A proxy is a reception desk through which eligible calls pass before reaching the real worker.

**Common mistake:** Assuming a proxy changes the source code inside your method. It surrounds an eligible invocation.

**Remember:** Many seemingly automatic Spring features are additional behavior applied at a managed-object boundary.

### Checkpoint 10

1. What problem does AOP address that would otherwise be repeated across business methods?
2. In the flow `caller → proxy → real bean`, where is transaction advice applied?
3. Why can manually calling `new PaymentService(...)` bypass proxy-based features?
4. Why may `this.otherMethod()` behave differently from a call entering through the Spring bean reference?

---

## 22. Common Spring Annotations

Use this as a lookup table after you understand the container. Annotations are metadata; Spring infrastructure must discover and process them.

| Annotation | Purpose | Typical layer/location | Short example | Priority |
|---|---|---|---|---|
| `@Component` | Register a general scanned component | Infrastructure/helper | `@Component class IdGenerator` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@Service` | Mark a business/application service | Service | `@Service class StudentService` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@Repository` | Mark a persistence component | Repository/DAO | `@Repository class JdbcStudentRepository` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@Controller` | Mark an MVC controller candidate | Future web layer | `@Controller class StudentController` | ⭐⭐ FUTURE KNOWLEDGE |
| `@Configuration` | Declare a Java configuration class | Configuration | `@Configuration class AppConfig` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@Bean` | Register the object returned by a method | Configuration method | `@Bean Clock clock()` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@Autowired` | Mark an injection point | Constructor/setter; occasionally legacy field | `@Autowired SomeService(Repo r)` | ⭐⭐⭐⭐ IMPORTANT |
| `@Qualifier` | Narrow matching bean candidates | Constructor parameter or bean declaration | `@Qualifier("smsSender")` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@Primary` | Prefer one candidate as the default | Bean class/method | `@Primary @Component` | ⭐⭐⭐⭐ IMPORTANT |
| `@Value` | Inject a resolved value/expression | Constructor parameter/field | `@Value("${app.name}")` | ⭐⭐⭐⭐ IMPORTANT |
| `@Profile` | Register a bean only for selected profiles | Component or `@Bean` method | `@Profile("development")` | ⭐⭐⭐ IMPORTANT |
| `@PostConstruct` | Run initialization after injection | Managed bean method | `@PostConstruct void init()` | ⭐⭐⭐⭐ IMPORTANT |
| `@PreDestroy` | Run cleanup during managed destruction | Managed bean method | `@PreDestroy void close()` | ⭐⭐⭐⭐ IMPORTANT |
| `@Transactional` | Declare a transaction boundary | Usually public service method/class | `@Transactional public void transfer()` | ⭐⭐⭐⭐ IMPORTANT |
| `@ComponentScan` | Select packages whose component candidates are discovered | Configuration | `@ComponentScan("com.example")` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `@PropertySource` | Add a properties resource to the environment | Configuration | `@PropertySource("classpath:app.properties")` | ⭐⭐⭐⭐ IMPORTANT |
| `@Scope` | Select a bean scope | Component or `@Bean` method | `@Scope("prototype")` | ⭐⭐⭐ IMPORTANT |
| `@EnableTransactionManagement` | Enable annotation-driven transaction interception when infrastructure exists | Transaction configuration | `@EnableTransactionManagement` | ⭐⭐ FUTURE KNOWLEDGE |

The first fourteen rows are the requested everyday reference; the final configuration rows make their supporting mechanics explicit.

---

## 23. Common Beginner Confusions

| Confusion | Clear distinction |
|---|---|
| **Spring vs Spring Boot** | Spring Framework supplies foundational APIs and infrastructure. Boot uses them and adds opinionated startup, auto-configuration, starters, and operational conveniences. |
| **IoC vs DI** | IoC is the broad transfer of construction/wiring control. DI is the technique of supplying dependencies from outside and is Spring's main IoC mechanism. |
| **Bean vs normal object** | Both are Java objects. A bean is registered and managed by a particular Spring container. |
| **`@Component` vs `@Bean`** | `@Component` marks a scanned class. `@Bean` marks a configuration method whose returned object is registered. |
| **`@Component` vs `@Service`** | Both register component candidates. `@Service` states that the class owns business/application operations. |
| **`@Service` vs `@Repository`** | A service coordinates use cases/rules. A repository stores and retrieves data; `@Repository` also connects to persistence exception-translation conventions. |
| **`@Autowired` vs constructor injection** | Constructor injection is a design/injection style. `@Autowired` is Spring metadata; it is usually unnecessary on a bean's only constructor. |
| **`ApplicationContext` vs `BeanFactory`** | `BeanFactory` is the basic container contract. `ApplicationContext` builds on it with the capabilities normal applications use. |
| **singleton bean vs Singleton pattern** | Spring singleton means one instance per bean definition per container. The design pattern commonly enforces JVM-wide access through static construction. |
| **Spring JDBC vs raw JDBC** | Spring JDBC uses JDBC and handles repeated workflow/resource/error code. You still write SQL and row mappings. |
| **`@Transactional` vs `connection.commit()`** | `commit()` is an imperative action on one JDBC connection. `@Transactional` declares a method boundary that configured Spring infrastructure manages, commonly through a proxy. |
| **Spring Framework vs Spring MVC** | The Framework is the broad foundation. Spring MVC is its servlet-based web module. |
| **Spring MVC vs REST API** | MVC is a web framework. REST is an architectural style an MVC application can implement; they are not synonyms. |
| **Spring Data JPA vs Hibernate** | Spring Data JPA can generate repository implementations over JPA. Hibernate is a common JPA provider/ORM. Neither is Spring Core. |
| **JPA vs JDBC** | JDBC is lower-level SQL/driver access. JPA specifies object-relational persistence APIs; providers still ultimately communicate with databases through lower layers. |
| **Spring-managed object vs object created with `new`** | A managed object receives configured injection, lifecycle, and eligible proxy behavior. A manually created object is valid Java but outside that container management unless registered. |

Two more useful distinctions:

- `@Controller` alone does not make a REST endpoint; mappings and Spring MVC infrastructure are still required.
- Compilation proves Java types are valid. It does not prove the context can resolve every bean dependency at startup.

### Checkpoint 11

1. Why can a project compile but fail when `ApplicationContext` starts?
2. Is `@Service` functionally unrelated to `@Component`?
3. Does a method become transactional when its annotation merely compiles?
4. Is a manually constructed service always “bad”?

---

## 24. What Happens at Runtime

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Here is one complete, small example.

`AppConfig.java`:

```java
@Configuration
@ComponentScan("com.example.school")
public class AppConfig {
}
```

`StudentRepository.java`:

```java
@Repository
public class StudentRepository {
    public void save(String name) {
        System.out.println("Saved " + name);
    }
}
```

`StudentService.java`:

```java
@Service
public class StudentService {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public void enroll(String name) {
        repository.save(name);
    }
}
```

`Main.java`:

```java
public static void main(String[] args) {
    try (var context =
             new AnnotationConfigApplicationContext(AppConfig.class)) {
        StudentService service = context.getBean(StudentService.class);
        service.enroll("Mai");
    }
}
```

### Runtime narrative

1. Java enters `main`.
2. `AnnotationConfigApplicationContext` is constructed with `AppConfig`.
3. Spring reads `@Configuration` and `@ComponentScan`.
4. The configured package is scanned.
5. `@Repository` causes a repository bean definition to be registered.
6. `@Service` causes a service bean definition to be registered.
7. During context refresh, Spring creates the repository instance.
8. To create the service, Spring inspects its constructor.
9. It resolves the only compatible `StudentRepository` bean.
10. Spring calls the constructor with that object.
11. The completed service is stored as a singleton bean.
12. `getBean(StudentService.class)` returns that managed service.
13. Your code calls `enroll`; ordinary Java method calls then occur.
14. The try-with-resources block closes the context and managed destruction runs.

```text
Main
  ↓ creates context from AppConfig
ApplicationContext
  ↓ scans
StudentRepository definition + StudentService definition
  ↓ constructs dependency first
StudentRepository bean
  ↓ injects through constructor
StudentService bean
  ↓ returned to Main
service.enroll("Mai")
```

Spring does not replace Java method calls. Its central job occurred while assembling and managing the objects.

---

## 25. Manual Java vs Spring Comparison

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### A. Pure Java composition

```java
public static void main(String[] args) {
    StudentRepository repository = new StudentRepository();
    StudentService service = new StudentService(repository);

    service.enroll("Mai");
}
```

### B. Spring composition

```java
public static void main(String[] args) {
    try (var context =
             new AnnotationConfigApplicationContext(AppConfig.class)) {
        StudentService service = context.getBean(StudentService.class);
        service.enroll("Mai");
    }
}
```

### What changed?

| Concern | Pure Java version | Spring version |
|---|---|---|
| Repository construction | `main` | Container |
| Service construction | `main` | Container |
| Constructor argument | Passed explicitly by `main` | Resolved and passed by container |
| Wiring source | Composition code | Scanning/configuration metadata |
| Lifecycle callbacks | Your code must call them | Container coordinates them |
| Eligible AOP/proxy features | Must be built manually | Can be applied to managed beans |
| Business method | Ordinary Java call | Still an ordinary Java call on the obtained bean/proxy |

The pure Java version is not wrong. It is useful and testable. Spring becomes valuable when the object graph and infrastructure grow, or when consistent lifecycle/proxy/configuration behavior is needed.

**Remember:** Spring changes composition and management more than it changes your business-class syntax.

---

## 26. Priority Levels — What to Study Deeply

| Concept | Priority | Required depth now |
|---|---|---|
| Spring Framework vs Boot | ⭐⭐⭐⭐⭐ MUST KNOW | Explain the relationship in your own words |
| IoC and DI | ⭐⭐⭐⭐⭐ MUST KNOW | Trace who constructs and injects each object |
| Constructor injection | ⭐⭐⭐⭐⭐ MUST KNOW | Write it naturally and explain why it is preferred |
| ApplicationContext | ⭐⭐⭐⭐⭐ MUST KNOW | Start, use, and close a plain context |
| BeanFactory concept | ⭐⭐⭐ NICE TO KNOW | Recognize it as the lower-level container contract |
| Bean vs ordinary object | ⭐⭐⭐⭐⭐ MUST KNOW | Identify managed and unmanaged objects |
| Component scanning/stereotypes | ⭐⭐⭐⭐⭐ MUST KNOW | Configure a scan and choose layer annotations |
| `@Configuration` / `@Bean` | ⭐⭐⭐⭐⭐ MUST KNOW | Register a third-party object |
| Resolution / `@Qualifier` / `@Primary` | ⭐⭐⭐⭐⭐ MUST KNOW | Diagnose zero/multiple-candidate failures |
| Interfaces and layering | ⭐⭐⭐⭐⭐ MUST KNOW | Design a useful service-to-repository boundary |
| `@Autowired` rules | ⭐⭐⭐⭐ IMPORTANT | Recognize it and omit it on one constructor |
| Singleton scope | ⭐⭐⭐⭐ IMPORTANT | Understand sharing and mutable-state risk |
| Prototype scope | ⭐⭐⭐ NICE TO KNOW | Demonstrate two direct lookups; know lifecycle limitation |
| Lifecycle callbacks | ⭐⭐⭐⭐ IMPORTANT | Observe initialization and orderly destruction |
| Properties and profiles | ⭐⭐⭐⭐ IMPORTANT | Load/inject a value; recognize environment selection |
| Repository exception translation | ⭐⭐⭐⭐ IMPORTANT | Understand the benefit and infrastructure caveat |
| `JdbcTemplate` | ⭐⭐⭐⭐ IMPORTANT | Understand its relationship to raw JDBC |
| `@Transactional` | ⭐⭐⭐⭐ IMPORTANT | Explain boundary, proxy, manager, commit, and rollback |
| AOP vocabulary | ⭐⭐⭐ NICE TO KNOW | Recognize why cross-cutting features use proxies |
| Request/session scopes | ⭐⭐ FUTURE KNOWLEDGE | Know they are web-aware scopes |
| Detailed proxy/AOP internals | ⭐⭐ FUTURE KNOWLEDGE | Postpone implementation detail |
| Advanced transaction settings | ⭐⭐ FUTURE KNOWLEDGE | Postpone until basic transactions work |

Allocate most study time to the rows marked **MUST KNOW**.

---

## 27. Knowledge Checkpoints

Answer these without running code. Answers are in Section 32.

### Cumulative checkpoint

1. Exactly what control is “inverted” in Spring IoC?
2. Why is DI not exactly the same term as IoC?
3. What is the difference between a bean definition and an instance?
4. What three broad outcomes are possible during resolution by type?
5. Why can two correct component classes prevent the context from starting?
6. When is `@Bean` better than annotating a class?
7. Does a singleton service automatically become thread-safe?
8. Why might `@PreDestroy` never print?
9. What JDBC work does `JdbcTemplate` still perform underneath?
10. Why might `this.transferPartTwo()` bypass transaction advice?
11. Which layer normally owns a business transaction boundary?
12. What should you be able to explain before starting Spring Boot?

---

## 28. Small Coding Exercises

Attempt these before reading Section 32. The companion mini-project turns them into a single working application.

### Exercise A — Manual wiring to DI

**Problem:** `OrderService` creates `FileOrderRepository` internally. Refactor it to receive an `OrderRepository` through its constructor.

```java
class OrderService {
    // TODO: depend on the interface and receive it from outside
    private final FileOrderRepository repository = new FileOrderRepository();
}
```

Expected property: `new OrderService(fakeRepository)` is possible in a plain unit test.

### Exercise B — Component scan boundary

Put `AppConfig` in `com.example.app.config`, `OrderService` in `com.example.app.service`, and scan only `com.example.app.config`. Call `context.getBean(OrderService.class)` and expect `NoSuchBeanDefinitionException`. Then change the scan root so both packages are covered and verify the lookup succeeds.

### Exercise C — Multiple senders

Create two component implementations of `NotificationSender`. First reproduce the ambiguous dependency. Then:

1. choose a default using `@Primary`;
2. inject the non-default at another injection point with `@Qualifier`.

### Exercise D — Explicit `@Bean`

Register a UTC `Clock` using a method in `AppConfig`. Inject it into `AuditService` and call `clock.instant()`.

### Exercise E — Lifecycle and scopes

Add lifecycle messages to a singleton service. Retrieve it twice. Then define a prototype draft object and retrieve it twice. Predict both `==` results and observe shutdown output.

### Exercise F — External property

Place `app.display-name=Core Practice` in a classpath properties file. Inject it into a constructor. Change the file to `DI Practice`, rerun without changing Java source, and verify output changes.

### Exercise G — Layer review

Refactor code where `Main` contains a SQL string, a transfer rule, and console printing. Move each responsibility to repository, service, or `Main` respectively. Do not add Spring MVC.

### Exercise H — Proxy prediction

Suppose `PaymentService` is proxied and `pay()` calls `this.recordAudit()`. Both methods have annotations. Draw which call enters through the external proxy and which is an internal call. No AOP code is required.

### Exercise I — Switch a profile before refresh

Create `DevelopmentGreetingSource` and `ProductionGreetingSource` beans guarded by `@Profile("development")` and `@Profile("production")`. Use the no-argument `AnnotationConfigApplicationContext`, activate `development`, register `AppConfig`, then call `refresh()`. Verify the development implementation is the only matching bean. Repeat with production.

---

## 29. Final Spring Core Mental Model

```text
Spring application
        ↓
ApplicationContext
        ↓
bean definitions from scans and @Bean methods
        ↓
Spring creates bean instances
        ↓
resolves and injects dependencies
        ↓
performs initialization / eligible proxy wrapping
        ↓
application uses the managed object graph
        ↓
context closes and managed destruction occurs
```

```text
Your code
    ↓ asks Spring-managed collaborators to perform work
Spring Framework
    ↓ coordinates infrastructure
JDBC / HTTP / messaging / etc.
    ↓
database / remote service / other external system
```

And the dependency direction remains ordinary Java:

```text
high-level use case
StudentService
      ↓ constructor dependency
StudentRepository interface
      ↑ selected and injected by Spring
JdbcStudentRepository bean
```

The annotations describe candidates and configuration. The container reads that metadata, builds the graph, and applies its management at runtime.

---

## 30. Final Knowledge Checklist

- [ ] I can explain what Spring is.
- [ ] I can distinguish Spring Framework from Spring Boot.
- [ ] I understand IoC.
- [ ] I understand DI.
- [ ] I know what a Spring bean is.
- [ ] I understand `ApplicationContext`.
- [ ] I understand component scanning and scan boundaries.
- [ ] I can use constructor injection.
- [ ] I know `@Component`, `@Service`, and `@Repository`.
- [ ] I understand `@Configuration` and `@Bean`.
- [ ] I understand `@Qualifier` and `@Primary`.
- [ ] I know singleton and prototype scopes.
- [ ] I understand the basic bean lifecycle.
- [ ] I can load and inject an external property in plain Spring.
- [ ] I understand profiles at a basic level.
- [ ] I understand layered architecture.
- [ ] I understand how Spring relates to JDBC.
- [ ] I understand the purpose and required infrastructure of `@Transactional`.
- [ ] I understand the basic proxy/AOP idea.
- [ ] I can trace container startup from configuration to a usable service.
- [ ] I know why a manually constructed object is not automatically managed.
- [ ] I am ready to start Spring Boot.

You are ready for Spring Boot when you can explain not only **what** annotations appear, but also **which container reads them, what beans it registers, how it resolves a constructor, and where proxy behavior comes from**.

---

## 31. What NOT to Learn Deeply Yet

**Priority: ⭐⭐ FUTURE KNOWLEDGE**

| Topic to postpone | Why it can wait |
|---|---|
| Advanced AOP and custom pointcut expressions | The proxy mental model is enough before you write custom aspects |
| Custom `BeanPostProcessor` implementations | These are container extension internals, not normal beginner application code |
| Deep bean factory/container internals | Useful for framework authors and difficult edge cases, not ordinary service design |
| Advanced transaction propagation/isolation | First learn one correct service transaction and rollback behavior |
| Advanced SpEL | Most beginner configuration needs simple property placeholders only |
| Reactive Spring and WebFlux | They introduce a different execution model; learn normal request processing later first |
| Spring Security | It has its own filter chain and security model; add it after web fundamentals |
| Spring Data JPA and Hibernate | They add ORM concepts; your JDBC foundation is enough for this stage |
| Spring MVC and REST implementation | Learn after Spring Core, usually through Spring Boot |
| Spring Cloud | Distributed-system patterns matter after building a normal backend |
| Request/session scopes | They require web context and real HTTP use cases |
| Detailed JDK proxy/CGLIB selection | Recognize proxies now; implementation mechanics can wait |

Historical XML bean configuration is worth recognizing when reading older projects, but annotation-based Java configuration is the practical starting point here.

---

## 32. Answer Key

Do not use this section until you have attempted the checkpoints and exercises.

### Checkpoint answers

**Checkpoint 1**

1. The container registers definitions, creates selected objects, resolves/injects their dependencies, and manages relevant lifecycle/proxy behavior.
2. No. It is an ordinary object unless a container registers and manages it.
3. No. Boot builds on Framework and automates common setup.

**Checkpoint 2**

1. `StudentRepository` is the dependency required by `StudentService`.
2. A test can pass a fake/in-memory implementation directly, without starting Spring or a database.
3. The required collaborator is not visible in the constructor contract and usually requires framework mutation/reflection during testing.

**Checkpoint 3**

1. Registration and management by a Spring container make it a bean.
2. A definition is the recipe/metadata; the instance is the actual constructed object.
3. Manual construction bypasses container injection, lifecycle processing, and eligible proxies.

**Checkpoint 4**

1. The Spring container creates the repository bean.
2. The Spring container calls the service constructor.
3. The argument is the compatible repository bean selected from the context.
4. Yes. Constructor injection keeps it usable as normal Java.

**Checkpoint 5**

1. Context creation fails with an unsatisfied dependency.
2. Context creation fails with an ambiguous/non-unique bean error.
3. `@Primary` establishes a default; `@Qualifier` narrows a particular injection point to a named/qualified candidate.

**Checkpoint 6**

1. After construction and dependency injection, before normal bean use.
2. Closing the context allows orderly destruction callbacks.
3. No. Spring does not manage the full destruction lifecycle for prototypes.

**Checkpoint 7**

1. Plain Spring does not give the filename Boot's automatic convention; the source must be registered.
2. When lookup is dynamic, optional, or several related values are read programmatically.
3. Before the context refresh selects and creates profile-specific beans.

**Checkpoint 8**

1. The service depends on the stable interface; Spring can inject either registered implementation without changing service construction code.
2. The repository owns SQL/row mapping; the service normally owns the coordinated business use case and its transaction boundary.
3. No. The relevant exception-translation infrastructure must exist; `JdbcTemplate` performs its own JDBC exception translation.
4. It reduces connection/statement/result-set cleanup and exception boilerplate. You still supply SQL, parameters, and row mapping/business meaning.

**Checkpoint 9**

1. It coordinates the connection/resource participation and begin/commit/rollback around the declared boundary.
2. No. A transaction manager and annotation/proxy infrastructure must be configured.
3. `RuntimeException` and `Error` trigger rollback by default; rules can be configured.

**Checkpoint 10**

1. It centralizes cross-cutting concerns such as transaction management, logging, or security instead of mixing repeated infrastructure into business methods.
2. The proxy applies advice around the delegated call before/after reaching the real bean.
3. The manually created instance is not the configured proxy returned by the container.
4. Self-invocation stays on the target object and normally does not cross the external proxy boundary again.

**Checkpoint 11**

1. Compilation checks types, but startup must discover configuration and resolve a unique bean for every required dependency.
2. No. `@Service` is a more specific component stereotype.
3. No. Transaction infrastructure must interpret it and the call must be eligible for interception.
4. No. Manual construction can be useful, especially in tests or explicit composition; the object simply is not automatically Spring-managed.

**Cumulative checkpoint**

1. Creation and wiring control moves from consumer classes/application composition to the container.
2. IoC is the broad principle; DI is one mechanism that implements it.
3. Metadata/recipe versus constructed managed object.
4. Zero candidates fails, one candidate injects, and unresolved multiple candidates fail.
5. They both satisfy one required type, so Spring cannot choose without more metadata.
6. Use it when you cannot/should not annotate the class or need explicit construction.
7. No. Shared mutable state can still race.
8. The context may not have closed orderly, the object may be unmanaged, or it may be a prototype whose destruction is not tracked.
9. It obtains connections, prepares/executes statements, iterates results, closes resources, and translates SQL exceptions while using your SQL/mapping.
10. The internal call does not enter through the external proxy in the normal proxy model.
11. Usually the service/use-case layer, because it knows which repository actions form one unit.
12. The container/bean/DI mental model, startup and resolution flow, configuration styles, lifecycle, layers, and basic proxy/transaction idea.

### Exercise reference notes

**Exercise A:** Store an `OrderRepository` in a `final` field and accept it in `OrderService(OrderRepository repository)`. Construct the desired implementation only in composition/configuration code.

**Exercise B:** Scan `com.example.app`, the common parent. Scanning only `.config` cannot find the sibling `.service` package.

**Exercise C:** With two implementations, expect startup ambiguity. Mark email `@Primary` for the default; use `@Qualifier("smsSender")` where SMS is specifically required.

**Exercise D:** Put `@Bean Clock applicationClock() { return Clock.systemUTC(); }` in a registered `@Configuration` class and add `Clock` to the service constructor.

**Exercise E:** Two singleton service lookups normally compare `true`; two direct prototype lookups compare `false`. `@PreDestroy` prints when the context closes.

**Exercise F:** Use `@PropertySource` to load the classpath file and inject `@Value("${app.display-name}")`. Rebuilding/rerunning reads the changed resource without editing Java.

**Exercise G:** SQL and row mapping belong in the repository, the transfer rule/boundary belongs in the service, and console parsing/printing stays in `Main`.

**Exercise H:** The external call to `pay()` enters the proxy. `this.recordAudit()` is a direct call on the target object and normally bypasses a second proxy interception.

**Exercise I:** Set the active profile before the context is refreshed. Only the implementation whose `@Profile` matches the active environment is registered; activating the profile after `refresh()` is too late for that context build.

**Open practice prompt note:** “Find the wiring” and “Classify components” are observation/classification prompts rather than fixed coding problems. A sound classification puts general infrastructure under `@Component`, business operations under `@Service`, persistence under `@Repository`, and future web adapters under `@Controller`.

---

## Official references for later lookup

- [Spring Framework 6.2 reference — Core Technologies](https://docs.spring.io/spring-framework/reference/6.2/core.html)
- [Spring Framework 6.2 reference — Data Access](https://docs.spring.io/spring-framework/reference/6.2/data-access.html)
- [Spring Framework overview](https://spring.io/projects/spring-framework)

Use the companion project now. Its goal is to make the container's behavior visible before Spring Boot automates startup and configuration.
