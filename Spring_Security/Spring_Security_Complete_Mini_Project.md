# Spring Security Complete Mini Project

This document builds one deliberately small, runnable application named `spring-security-mini-demo`. It uses Java 21, Spring Boot 4.1.0, Boot-managed Spring Security 7.x, PostgreSQL, Flyway, and server-side HTTP sessions.

The companion implementation is already available in [`spring-security-mini-demo/`](./spring-security-mini-demo/); section 27 mirrors every source file for study.

> Version note: Spring Boot owns the compatible Spring Framework, Spring Security, Hibernate, Flyway, and driver versions. The POM does not override Spring Security. Boot 4.1’s MVC starter is `spring-boot-starter-webmvc`.

## 1. What this project teaches

By the end, you should be able to narrate this without memorizing isolated class definitions:

```text
Client submits username/password
        ↓
Spring Security form-login filter creates an untrusted Authentication
        ↓
AuthenticationManager (normally ProviderManager) receives it
        ↓
ProviderManager selects DaoAuthenticationProvider
        ↓
DaoAuthenticationProvider calls DatabaseUserDetailsService
        ↓
UserDetailsService calls AppUserRepository
        ↓
JPA → Hibernate → JDBC → HikariCP → PostgreSQL
        ↓
DaoAuthenticationProvider calls PasswordEncoder.matches(...)
        ↓
ProviderManager returns a trusted Authentication and erases credentials
        ↓
the filter places it in SecurityContext
        ↓
Spring Security saves the context in the HTTP session
        ↓
the client keeps only the opaque JSESSIONID cookie
        ↓
later requests restore the context and run authorization
```

The project also makes four boundaries explicit:

- authentication asks **who are you?**;
- authorization asks **may this authenticated identity do this?**;
- CSRF protection asks **did this cookie-authenticated browser intentionally submit this unsafe request?**;
- a transaction asks **should these database changes commit or roll back?**

JWT is intentionally absent. It is easier to understand token authentication after filters, providers, authorities, contexts, and authorization already make sense.

## 2. Final architecture

```text
┌──────────────┐       HTTP + JSESSIONID + CSRF token
│ curl/Postman │ ───────────────────────────────────────────────┐
└──────────────┘                                                │
                                                                ▼
                                                     ┌────────────────────┐
                                                     │ SecurityFilterChain│
                                                     │                    │
                                                     │ context / CSRF     │
                                                     │ form login/logout  │
                                                     │ authorization      │
                                                     └─────────┬──────────┘
                                                               │ allowed
                                                               ▼
                                                     ┌────────────────────┐
                                                     │ MVC controllers    │
                                                     └─────────┬──────────┘
                                                               │
                                                     ┌─────────▼──────────┐
                                                     │ service proxies    │
                                                     │ @PreAuthorize      │
                                                     │ @Transactional     │
                                                     └─────────┬──────────┘
                                                               │
                                                     ┌─────────▼──────────┐
                                                     │ repositories       │
                                                     └─────────┬──────────┘
                                                               │
                                      JPA → Hibernate → JDBC → HikariCP
                                                               │
                                                     ┌─────────▼──────────┐
                                                     │ PostgreSQL         │
                                                     └────────────────────┘
```

The important authentication collaborators are:

| Component | Supplied/configured by | Responsibility |
|---|---|---|
| `SecurityFilterChain` bean | developer | Declares the HTTP security policy |
| `UsernamePasswordAuthenticationFilter` | Spring Security | Reads form parameters and creates the authentication request |
| unauthenticated `Authentication` | Spring filter | Carries the submitted username and password claim |
| `AuthenticationManager` | Spring Security infrastructure | Defines the authentication entry point used by filters |
| `ProviderManager` | Spring Security infrastructure | Tries compatible authentication providers |
| `DaoAuthenticationProvider` | Spring Security | Performs database-backed username/password authentication |
| `DatabaseUserDetailsService` | developer | Loads Spring Security’s view of a user |
| `AppUserRepository` | developer interface; Spring implements it | Loads the persistence entity |
| `PasswordEncoder` bean | developer choice; Spring implementation | Encodes registration passwords and verifies login passwords |
| authenticated `Authentication` | Spring Security | Represents a verified identity plus authorities |
| `SecurityContext` lifecycle | Spring Security | Holds the current request’s `Authentication` |
| HTTP session integration | Spring Security + servlet container | Persists security state between requests |

The standard manager/provider infrastructure is assembled from the application’s single `UserDetailsService`, its `PasswordEncoder`, and `formLogin` configuration. We do **not** manually instantiate `ProviderManager` or `DaoAuthenticationProvider` for this ordinary login.

## 3. Project structure

```text
spring-security-mini-demo/
├── .env.example
├── .gitignore
├── compose.yaml
├── pom.xml
└── src/
    └── main/
        ├── java/com/example/securitydemo/
        │   ├── SecurityDemoApplication.java
        │   ├── admin/
        │   │   ├── AdminAuditEvent.java
        │   │   ├── AdminAuditEventRepository.java
        │   │   ├── AdminAuditResponse.java
        │   │   ├── AdminController.java
        │   │   ├── AdminOperationService.java
        │   │   └── AdminWorkflowService.java
        │   ├── auth/
        │   │   ├── AuthController.java
        │   │   ├── RegisterRequest.java
        │   │   ├── RegisterResponse.java
        │   │   ├── RegistrationService.java
        │   │   └── UsernameAlreadyExistsException.java
        │   ├── bootstrap/
        │   │   └── LocalAdminBootstrap.java
        │   ├── publicapi/
        │   │   └── PublicController.java
        │   ├── security/
        │   │   ├── RestAccessDeniedHandler.java
        │   │   ├── RestAuthenticationEntryPoint.java
        │   │   └── SecurityConfig.java
        │   ├── user/
        │   │   ├── AppUser.java
        │   │   ├── AppUserRepository.java
        │   │   ├── DatabaseUserDetailsService.java
        │   │   ├── Role.java
        │   │   └── UserController.java
        │   └── web/
        │       └── ApiExceptionHandler.java
        └── resources/
            ├── application.yaml
            └── db/migration/
                ├── V1__create_app_users.sql
                └── V2__create_admin_audit_events.sql
```

There are two migration files because the second table makes the transaction example observable. That also demonstrates the normal Flyway pattern: evolve a schema by adding a new immutable migration instead of editing a migration that has already run.

## 4. Dependencies

The complete POM is in [section 27](#pomxml). Its direct dependencies are intentionally few:

| Dependency | What it provides |
|---|---|
| `spring-boot-starter-webmvc` | Spring MVC, JSON HTTP conversion, validation integration, embedded Tomcat |
| `spring-boot-starter-security` | Spring Security’s servlet filters, authentication, authorization, CSRF, session and logout support |
| `spring-boot-starter-data-jpa` | Spring Data repositories, Jakarta Persistence, Hibernate ORM and JDBC integration |
| `spring-boot-starter-validation` | Jakarta Bean Validation for `RegisterRequest` |
| `spring-boot-starter-flyway` | Boot 4.1 Flyway auto-configuration and core integration |
| `flyway-database-postgresql` | Flyway’s PostgreSQL database support |
| `postgresql` | PostgreSQL JDBC driver; runtime-only |

The parent POM manages compatible versions. Pinning a random Spring Security version independently could combine incompatible Spring Framework/Security binaries, so this project does not do it.

Java 21 is set with:

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

No test dependency is included because this exact document contains only production code and a manual learning exercise. In a maintained application, add the Boot test and Spring Security test starters with test scope.

## 5. PostgreSQL and Docker Compose

The Compose file runs only PostgreSQL. The Java application runs from Maven on the host:

```text
Spring Boot application
        ↓ obtains a connection
HikariCP connection pool
        ↓ uses
PostgreSQL JDBC driver
        ↓ TCP 127.0.0.1:5432
PostgreSQL container
        ↓ persists data in
named Docker volume
```

The host binding is loopback-only, so the development database is not deliberately exposed on every network interface. `.env.example` contains disposable learning values, while `.gitignore` prevents a copied `.env` from being committed. A real deployed secret belongs in a secret manager or deployment environment, not in Git.

The healthcheck tells Docker whether PostgreSQL is accepting connections. It does not automatically start the host-side Java process.

## 6. Flyway migration

Flyway and Hibernate have different jobs:

```text
application startup
        ↓
Flyway reads schema history
        ↓
Flyway applies pending V1, V2, ... migrations
        ↓
JPA creates the EntityManagerFactory
        ↓
Hibernate validates entities against the resulting schema
```

The key setting is:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

`validate` means Hibernate fails startup when mappings and schema disagree; it does not create or mutate the schema. Flyway remains the schema owner.

`V1__create_app_users.sql` enforces the invariants even if a future code path forgets them:

- primary key identity;
- unique, non-null username;
- non-null password hash;
- non-null role restricted to `USER` or `ADMIN`.

The database stores a BCrypt hash such as `$2a$10$...`, never a raw password. `V2` adds an audit-event table used by the protected transactional method.

## 7. AppUser entity

`AppUser` is a persistence model:

```text
AppUser
├── maps to app_users
├── has database identity and column mappings
└── contains passwordHash and Role
```

`UserDetails` is a security-facing view:

```text
UserDetails
├── username
├── encoded password used during verification
├── authorities
└── account-state flags
```

Therefore:

```text
AppUser ≠ UserDetails
```

They can contain overlapping facts, but they serve different contracts. Keeping them separate prevents the database entity from leaking into security infrastructure and prevents an HTTP serializer from accidentally returning the password hash. No controller in this project returns `AppUser`.

`Role` stores `USER` or `ADMIN`, without a prefix. The conversion to Spring Security authorities happens in `DatabaseUserDetailsService`.

Common mistake: adding a public endpoint that returns `AppUser` because it is convenient. A BCrypt hash is not a harmless public value; it enables offline guessing and should remain secret.

## 8. Repository

The repository declares only the queries this project needs:

```java
Optional<AppUser> findByUsername(String username);
boolean existsByUsername(String username);
```

Spring Data creates the implementation at runtime. The complete database path is:

```text
application service or UserDetailsService
        ↓ calls developer-written repository interface
Spring Data JPA proxy
        ↓
Hibernate ORM
        ↓ emits SQL through
JDBC
        ↓ borrows a connection from
HikariCP
        ↓
PostgreSQL
```

`Optional` models “no row” explicitly. `existsByUsername` improves the normal duplicate-registration response, but the database `UNIQUE` constraint is still the final race-safe guard.

## 9. UserDetailsService

`DatabaseUserDetailsService.loadUserByUsername` is called by `DaoAuthenticationProvider`, not by the controller:

```text
username string
    ↓ normalize
AppUserRepository.findByUsername(...)
    ↓
AppUser persistence model
    ↓ map
UserDetails security model
```

Its central mapping is:

```java
return User.withUsername(appUser.getUsername())
        .password(appUser.getPasswordHash())
        .roles(appUser.getRole().name())
        .build();
```

The password supplied here is already encoded. `UserDetailsService` does **not**:

- receive the submitted raw password;
- call `matches`;
- decide whether authentication succeeded;
- place anything in `SecurityContext`.

Those are provider/filter responsibilities.

Role naming is a frequent source of mistakes:

| Value | Meaning |
|---|---|
| `USER` | application/database role name |
| `ADMIN` | application/database role name |
| `ROLE_USER` | Spring Security granted authority produced by `roles("USER")` |
| `ROLE_ADMIN` | Spring Security granted authority produced by `roles("ADMIN")` |

`roles("ADMIN")` adds `ROLE_`; do not pass `roles("ROLE_ADMIN")`. Conversely, `authorities("ADMIN")` does not add a prefix.

Spring Security 7 can also attach an authentication-factor authority such as `FACTOR_PASSWORD` to a successful password authentication. The `/auth/me` learning DTO deliberately projects only `ROLE_...` authorities so its output remains focused:

```json
{
  "username": "alice",
  "authorities": ["ROLE_USER"]
}
```

Internally, the authenticated token may contain additional framework authorities. Authorization checks should ask for the role they need rather than assume the authority list contains exactly one item.

## 10. PasswordEncoder

The application chooses BCrypt once, as a bean:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Registration and login deliberately use different encoder operations:

```text
registration
raw password → passwordEncoder.encode(raw) → salted BCrypt hash → database

login
submitted raw password + stored BCrypt hash
        → passwordEncoder.matches(raw, hash)
        → true or false
```

This is wrong:

```java
rawPassword.equals(storedHash)
```

One side is raw text and the other is a one-way password hash.

This is also wrong:

```java
passwordEncoder.encode(rawPassword).equals(storedHash)
```

BCrypt creates a new random salt on each call, so two valid encodings of the same password normally differ. `matches` reads the salt and work factor from the stored hash and performs the correct verification.

The controller never compares passwords. During login, `DaoAuthenticationProvider` calls `PasswordEncoder.matches`.

## 11. Registration

The public request type contains only `username` and `password`:

```json
{
  "username": "alice",
  "password": "alice-demo-password"
}
```

There is no input `role` property. Jackson ignores unknown properties by default in this setup, but even if a caller sends `"role":"ADMIN"`, the DTO has nowhere to store it and the service always constructs `Role.USER`. Public input can never select the persisted role.

The flow is:

```text
POST /auth/register + valid CSRF token
        ↓
@Valid RegisterRequest
        ↓
RegistrationService @Transactional
        ├── normalize username
        ├── friendly existsByUsername check
        ├── PasswordEncoder.encode(raw password)
        ├── new AppUser(..., Role.USER)
        └── saveAndFlush
                ↓
        database UNIQUE constraint is the race-safe last word
```

Why both checks? Two concurrent requests can both observe “not present.” Only PostgreSQL can serialize the final unique-key decision. `saveAndFlush` makes a race violation happen inside the service transaction, where it is translated to the same safe `409 Conflict` as the friendly check.

Validation errors return `400`. Duplicate usernames return a problem-details `409`. The error handler reports field messages but never echoes the rejected password.

## 12. Local admin provisioning

Public registration can only create `USER`, so the learning project needs a separate way to obtain an `ADMIN`. `LocalAdminBootstrap` exists only under the `local` Spring profile and reads:

```text
LOCAL_ADMIN_USERNAME
LOCAL_ADMIN_PASSWORD
```

Its rules are intentionally conservative:

```text
profile is not local
    → bean does not exist

either environment variable is blank
    → skip provisioning

username does not exist
    → BCrypt encode password, insert ADMIN

username already exists as ADMIN
    → leave it unchanged

username already exists as USER
    → warn and do not promote it
```

The password is never logged. This is disposable local-learning infrastructure, not a general production account-management design.

## 13. SecurityFilterChain

`HttpSecurity` is a builder used while creating the `SecurityFilterChain`. It is not itself the runtime chain. The important policy is:

```java
http.authorizeHttpRequests(authorize -> authorize
        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
        .requestMatchers(GET, "/public/hello", "/auth/csrf").permitAll()
        .requestMatchers(POST, "/auth/register", "/auth/login").permitAll()
        .requestMatchers(GET, "/auth/me").authenticated()
        .requestMatchers(GET, "/user/hello").hasAnyRole("USER", "ADMIN")
        .requestMatchers(GET, "/admin/hello").hasRole("ADMIN")
        .requestMatchers(POST, "/admin/action", "/admin/audit").hasRole("ADMIN")
        .requestMatchers(POST, "/auth/logout").authenticated()
        .anyRequest().denyAll()
);
```

The meanings are:

| Rule | Meaning |
|---|---|
| `permitAll()` | URL authorization allows anonymous and authenticated callers |
| `authenticated()` | any successfully authenticated identity is sufficient |
| `hasRole("ADMIN")` | requires `ROLE_ADMIN` under the default role-prefix convention |
| `hasAnyRole("USER", "ADMIN")` | either corresponding role authority is sufficient |
| `denyAll()` | safe fallback: unmatched requests never reach a controller |

Rules are evaluated in declaration order; the **first matching authorization rule wins**. Put specific rules before broad rules. A broad `requestMatchers("/**").permitAll()` at the top would make every later rule unreachable.

The first rule permits only the servlet container’s internal `ERROR` dispatcher type. Without it, a public controller’s JSON-parse or framework error can be redispatched to `/error` and then overwritten by the final deny rule. It does not make a normal client request to `/error` public.

Authentication-processing filters such as form login and logout can consume their configured URLs before MVC and before the final URL authorization decision. Keeping their URLs explicitly represented in the policy still documents the intended surface.

The project configures built-in form login at `POST /auth/login`, returns `204` on success, and delegates failure to the generic `401` entry point. Setting a custom login-page URL disables generation of Spring’s HTML login page; there is deliberately no GET controller for it.

CSRF remains enabled. Session fixation protection changes the session identifier after login. Built-in logout invalidates the session, clears authentication, deletes the `JSESSIONID` cookie, and returns `204`.

`WebSecurityConfigurerAdapter` is not used; it was the old inheritance-based style and is not part of this modern bean/lambda configuration.

## 14. Authentication flow

`POST /auth/login` is not mapped to a controller. It is handled by Spring Security’s form-login filter and must use `application/x-www-form-urlencoded` fields named `username` and `password`.

```text
POST /auth/login
Content-Type: application/x-www-form-urlencoded
Cookie: JSESSIONID=<anonymous-session-id>
X-CSRF-TOKEN: <token>

username=alice&password=alice-demo-password
```

Conceptually, the filter performs this operation:

```java
Authentication request =
        UsernamePasswordAuthenticationToken.unauthenticated(username, password);

Authentication result = authenticationManager.authenticate(request);
```

It does not run that exact source from our controller; the point is the shape of the standard filter’s work.

Successful login:

```text
Client
  ↓ POST form fields + CSRF token
SecurityFilterChain
  ↓
UsernamePasswordAuthenticationFilter
  ↓ Authentication(authenticated=false)
AuthenticationManager
  ↓
ProviderManager
  ↓ selects a provider by token type
DaoAuthenticationProvider
  ├─ DatabaseUserDetailsService → repository → PostgreSQL
  └─ PasswordEncoder.matches(raw, storedHash)
  ↓ Authentication(authenticated=true)
form-login filter
  ├─ installs Authentication in a SecurityContext
  ├─ saves the context through the configured repository
  └─ invokes the success handler
  ↓
204 No Content + session cookie
```

Failed login returns the same generic `401` whether the username is absent or the password is wrong. That avoids turning the login endpoint into a username-discovery API.

## 15. AuthenticationManager and ProviderManager

`AuthenticationManager` is a tiny strategy interface:

```java
Authentication authenticate(Authentication authentication)
        throws AuthenticationException;
```

It defines **what a caller asks for**, not how every credential type is verified. The common implementation is `ProviderManager`:

```text
authentication filter
        ↓
AuthenticationManager interface
        ↓ implemented by
ProviderManager
        ↓ delegates to one or more
AuthenticationProvider implementations
```

Why does the manager not query PostgreSQL directly? A single application could accept passwords, API keys, passkeys, one-time tokens, or another mechanism. Each mechanism needs different verification logic and often a different `Authentication` subtype. Providers keep these mechanisms separate.

For this project, standard Spring configuration assembles a manager containing a `DaoAuthenticationProvider` from:

```text
one UserDetailsService bean
        +
one PasswordEncoder bean
        +
formLogin configuration
```

Defining `DatabaseUserDetailsService` also makes Boot’s generated in-memory demo user back off. Defining our `SecurityFilterChain` makes Boot’s catch-all default chain back off. Spring Security still supplies the filters and normal manager/provider infrastructure used by that chain.

Common mistake: publishing an `AuthenticationManager` bean and manually wiring a provider simply because a tutorial from an older or specialized setup did so. That is unnecessary here and obscures what Spring assembles.

## 16. AuthenticationProvider and provider selection

Each provider advertises which token classes it can examine:

```java
boolean supports(Class<?> authentication);
```

The main selection is:

```text
UsernamePasswordAuthenticationToken
        ↓ supports(...) == true
DaoAuthenticationProvider
```

`DaoAuthenticationProvider` then:

1. asks `UserDetailsService` for the stored security user;
2. performs account-state checks;
3. calls `PasswordEncoder.matches(submittedPassword, storedHash)`;
4. creates a trusted authentication result containing the principal and authorities.

### Optional provider-selection thought experiment

Suppose a future, separate filter created `ApiKeyAuthenticationToken`. A corresponding provider might say:

```java
@Override
public boolean supports(Class<?> authentication) {
    return ApiKeyAuthenticationToken.class
            .isAssignableFrom(authentication);
}
```

Then the conceptual routing is:

```text
UsernamePasswordAuthenticationToken → DaoAuthenticationProvider
ApiKeyAuthenticationToken           → ApiKeyAuthenticationProvider
```

This snippet is deliberately not part of the runnable project. Adding a provider without a filter that creates its token would accomplish nothing.

When multiple providers support the same token class, `ProviderManager` considers them in configured order. A provider can return a successful result, return `null` so another provider may try, or throw an `AuthenticationException`. Some account-status failures stop processing immediately; otherwise the final failure is propagated when nobody succeeds. Overlapping providers therefore need intentional ordering and failure semantics. Distinct token types are easier to reason about.

## 17. Authentication

`Authentication` has two related uses.

Before verification, it is an authentication request or claim:

```text
Authentication
├── principal     = "alice"
├── credentials   = submitted raw password
├── authorities   = []
└── authenticated = false
```

After successful verification, it is a trusted result:

```text
Authentication
├── principal     = UserDetails("alice")
├── credentials   = normally erased
├── authorities   = [ROLE_USER]          ← role-focused simplified view
└── authenticated = true
```

With Spring Security 7 password authentication, the real authority collection may additionally contain `FACTOR_PASSWORD`. That does not change the meaning of `hasRole("USER")`.

The distinction is:

```text
before verification = “I claim these credentials belong to Alice”
after verification  = “a trusted provider verified Alice and granted these authorities”
```

Do not mark an arbitrary token authenticated in application code. Let a trusted provider create the successful result. `ProviderManager` also erases credentials from successful results when the principal supports credential erasure, shortening the lifetime of secrets in memory.

`Authentication` is not the same as `UserDetails`:

- `UserDetails` is loaded stored user information;
- `Authentication` is the request/result object for one security identity and includes the current authorities and authentication state;
- after login, the `Authentication.principal` commonly happens to be that `UserDetails`.

## 18. SecurityContext and SecurityContextHolder

The nesting is:

```text
SecurityContextHolder
        ↓ obtains the context associated with current execution
SecurityContext
        ↓ contains
Authentication
        ├── principal
        ├── authorities
        └── authentication state
```

`SecurityContext` is the container. `SecurityContextHolder` is the access strategy, using a thread-local strategy by default in a synchronous servlet request.

Direct access is possible:

```java
Authentication authentication = SecurityContextHolder
        .getContext()
        .getAuthentication();
```

That is useful in infrastructure and occasional low-level code, but scattering static lookups through business services hides dependencies and makes tests harder. At the web boundary, use `@AuthenticationPrincipal`, `Authentication`, or `Principal` parameters. At a business boundary, pass the small fact the service needs or authorize the service with method security.

`GET /auth/me` uses `@AuthenticationPrincipal UserDetails principal`, then returns a safe DTO:

```text
@AuthenticationPrincipal
        ↓ argument resolver reads
SecurityContextHolder
        ↓
SecurityContext
        ↓
Authentication
        ↓
principal (UserDetails)
```

It never returns the password, password hash, complete `UserDetails`, or `AppUser`.

## 19. Session and JSESSIONID

### Login and persistence

```text
POST /auth/login
        ↓
authentication succeeds
        ↓
SecurityContext contains trusted Authentication
        ↓
Spring Security saves the context in the server-side HTTP session
        ↓
servlet container sends JSESSIONID to the client
```

A later call looks like:

```http
GET /auth/me HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=opaque-value
```

Then:

```text
JSESSIONID
    ↓ locates server-side HTTP session
saved SecurityContext
    ↓ restored for this request
current Authentication
    ↓ authorization
controller
```

The three concepts must not collapse into one:

```text
JSESSIONID ≠ Authentication
JSESSIONID ≠ SecurityContext

JSESSIONID = opaque lookup identifier for server-side session state
```

The cookie does not contain the username, roles, password, or serialized context. Possession of a valid session identifier is nevertheless security-sensitive, so production cookies must be protected with HTTPS and suitable cookie attributes.

The standard context repository includes HTTP-session persistence. Modern Spring Security’s context filter loads the context for a request, and the built-in authentication mechanism explicitly saves a successful context. This project does not manually put the context in the session.

Spring Security applies session-fixation protection at login, so the identifier can change even though the logical session continues. A curl cookie jar must be written with `-c` during login so it captures the replacement cookie.

### Session lifetime versus request-thread lifetime

```text
server-side session (minutes, across requests)
│
├── Request A begins
│     Thread-17: SecurityContext = Alice
│     controller runs
│     Thread-17 context is cleared in finally-style cleanup
│
├── time passes; session still contains Alice's saved context
│
└── Request B begins with the same JSESSIONID
      Thread-23: SecurityContext = Alice
      request ends; Thread-23 context is cleared
```

Later, the pool can reuse Thread-17 for Bob:

```text
Request C → Thread-17 → SecurityContext = Bob → clear at request end
```

Clearing the thread-associated holder prevents identity leakage between pooled threads. It is **not logout**. The session may still hold Alice’s security context, allowing her next request to restore it.

Logout is different: it invalidates the server-side session and clears authentication/context as part of the logout operation. Also note that plain `ThreadLocal` state does not automatically follow work submitted to an arbitrary asynchronous executor.

`UserDetailsService` is normally not queried on every session request. The session restores the already authenticated object. Therefore a database role or account-state change may not affect an existing session until reauthentication, expiry, forced invalidation, or a deliberately different session strategy.

## 20. Authorization and roles

Authentication precedes authorization:

```text
credentials verified?  → authentication
requested action allowed for granted authorities? → authorization
```

With the default role prefix:

```java
hasRole("ADMIN")
```

checks for:

```text
ROLE_ADMIN
```

Expected outcomes:

| Caller | Request | Result | Reason |
|---|---|---:|---|
| anonymous | `GET /auth/me` | `401` | no authenticated identity |
| `ROLE_USER` | `GET /user/hello` | `200` | either USER or ADMIN is accepted |
| `ROLE_USER` | `GET /admin/hello` | `403` | authenticated, but missing `ROLE_ADMIN` |
| `ROLE_ADMIN` | `GET /admin/hello` | `200` | required authority is present |

`401` does not mean “you lack one role”; it means authentication is absent or invalid. `403` means the server understood the identity/request but refuses it—or that another security check such as CSRF rejected the request.

## 21. Method security

HTTP authorization and method authorization protect different boundaries:

```text
SecurityFilterChain → HTTP request boundary
@PreAuthorize       → Spring bean method boundary
```

Method security is not enabled merely by adding the security starter. The configuration declares:

```java
@EnableMethodSecurity
```

The protected operation declares:

```java
@PreAuthorize("hasRole('ADMIN')")
public String executeAdminOperation() {
    return "method-security-protected operation executed";
}
```

`AdminWorkflowService` is another Spring bean. It calls the injected `AdminOperationService`:

```text
AdminController
    ↓
AdminWorkflowService (no annotation on this method)
    ↓ calls injected proxy
AdminOperationService.executeAdminOperation()
    ↓ @PreAuthorize checks current Authentication
method body runs only for ADMIN
```

That same protected service could later be called from a message listener, scheduler, or another controller. URL rules do not cover those non-HTTP entry paths, while the service proxy still guards the business operation.

Spring method security is proxy-based. A call from one bean through the injected protected bean crosses the proxy. A method calling another annotated method on `this` is self-invocation and normally bypasses proxy advice. Split such boundaries into collaborating beans or call the correctly proxied entry point; do not use self-injection as a reflex.

## 22. Security + transactions

One service method deliberately has both annotations:

```java
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public AdminAuditResponse recordAdminAudit(String username) {
    AdminAuditEvent saved = repository.save(
            new AdminAuditEvent(username, "ADMIN_DEMO_ACTION", Instant.now())
    );
    return new AdminAuditResponse(saved.getId(), saved.getAction(), saved.getCreatedAt());
}
```

They answer independent questions:

```text
@PreAuthorize  → WHO may enter this method?
@Transactional → HOW its database work commits or rolls back?
```

The conceptual call path is:

```text
POST /admin/audit
        ↓ HTTP hasRole("ADMIN")
AdminController
        ↓ external call into service proxy
@PreAuthorize
        ↓ allowed
@Transactional begins transaction
        ↓
AdminAuditEventRepository
        ↓
PostgreSQL
        ↓
normal return → commit
runtime failure → rollback
```

Method-security advice runs before the body, so a denied caller never performs the repository write. Transaction advice is not authorization, and a rollback does not mean access was denied.

As with method security, transactional advice is proxy-based; same-class self-invocation is a caveat. The method in this project is called externally from `AdminController`, so both interceptors apply.

## 23. 401 and 403 handling

Security failures often occur before MVC, so `@RestControllerAdvice` alone is not enough. This project configures two security-level extension points:

| Component | When it responds | Status |
|---|---|---:|
| `RestAuthenticationEntryPoint` | authentication is required/failed, including the form-login failure handler’s delegation | `401` |
| `RestAccessDeniedHandler` | an authenticated caller lacks authority, or another access-denied security condition occurs | `403` |

They return `application/problem+json`, for example:

```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "The request is not allowed."
}
```

The responses deliberately omit submitted credentials, exception class names, stack traces, database details, and whether a login username exists.

Do not confuse these two layers:

- the security handlers handle filter-chain failures;
- `ApiExceptionHandler` handles application/MVC failures such as invalid registration input or a duplicate username.

## 24. CSRF

CSRF remains enabled because authentication is carried automatically by a browser cookie. An attacker’s site cannot normally read this application’s token, but a browser may attach cookies when sending a cross-site request. Requiring a second, unpredictable token distinguishes an intentional application request.

The learning endpoint is public:

```http
GET /auth/csrf
```

It forces generation of the current token and safely returns its transport names and value:

```json
{
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf",
  "token": "..."
}
```

For an unsafe request, send **both** artifacts from the same session:

```text
Cookie: JSESSIONID=...
X-CSRF-TOKEN: ...
```

Spring Security protects state-changing methods by default:

```text
POST   → token required
PUT    → token required
PATCH  → token required
DELETE → token required
```

Safe retrieval methods such as GET, HEAD, OPTIONS, and TRACE are not normally CSRF-protected. They must therefore remain read-only by application design.

The filter order explains a surprising result:

```text
anonymous POST /admin/audit without CSRF token
        ↓
CsrfFilter rejects request
        ↓
403
        ↓
role authorization is never reached
```

Therefore an anonymous unsafe request can receive `403`, not the `401` one might expect from the endpoint’s role rule.

Most importantly:

```text
permitAll() ≠ disable CSRF
```

`permitAll` is an authorization decision. CSRF is a different filter and still protects `POST /auth/register`, `POST /auth/login`, and `POST /auth/logout`. This also protects against login CSRF and logout CSRF.

Spring Security clears the previous CSRF token after successful authentication and during logout. Fetch `/auth/csrf` again after either event. With the default BREACH protection, the outward token representation can also be randomized, so “the string changed” by itself is not a precise test of repository-token rotation; simply fetch and use the newly returned value.

Do not disable CSRF merely because controllers return JSON. The important question is how credentials are transported. Cookies are sent automatically by browsers; a JSON response body does not change that.

## 25. Logout

There is no logout controller. Spring Security’s built-in `LogoutFilter` handles:

```text
POST /auth/logout + session cookie + CSRF token
        ↓
LogoutFilter
        ├── invalidates HTTP session
        ├── clears Authentication
        ├── clears SecurityContext
        ├── asks the client to delete JSESSIONID
        └── success handler returns 204 No Content
```

After invalidation:

```text
old JSESSIONID
    ↓ server no longer finds an authenticated session
GET /auth/me
    ↓
401 Unauthorized
```

Clearing a request thread’s holder at ordinary request completion is cleanup. Invalidating the persistent session during logout changes future requests. These are not the same operation.

## 26. CORS boundary

No CORS configuration is needed for curl, Postman, or a same-origin browser client.

```text
CORS          = which browser origins may read/send cross-origin HTTP requests
authentication = who the caller is
authorization  = what that identity may do
CSRF           = whether a cookie-authenticated unsafe request carries the expected token
```

CORS is not an authentication mechanism and does not replace CSRF. If a separate browser frontend is added later, configure an explicit trusted origin, allowed methods/headers, and credentials support; make the frontend send cookies (for example, fetch with `credentials: "include"`) and continue sending the CSRF token. Cross-site cookies may also require `SameSite=None; Secure`, which in turn requires HTTPS.

## 27. Complete source code

The runnable `spring-security-mini-demo` directory contains every file below at the shown relative path. There are no omitted methods or placeholder comments.

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
        <version>4.1.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>spring-security-mini-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>spring-security-mini-demo</name>
    <description>Small session-based Spring Security learning project</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
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
            <scope>runtime</scope>
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

### `compose.yaml`

```yaml
name: spring-security-mini-demo

services:
  postgres:
    image: postgres:17-alpine
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-security_demo}
      POSTGRES_USER: ${POSTGRES_USER:-security_demo}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?copy .env.example to .env}
    ports:
      - "127.0.0.1:${POSTGRES_PORT:-5432}:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 5s

volumes:
  postgres-data:
```

The doubled dollar signs in the healthcheck defer expansion until the command runs inside the container.

### `.env.example`

```dotenv
# Disposable local-learning values only. Copy this file to .env.
POSTGRES_DB=security_demo
POSTGRES_USER=security_demo
POSTGRES_PASSWORD=change-me-local-only
POSTGRES_PORT=5432
```

The Java process does not automatically load Compose’s `.env`; section 28 sets matching application environment variables explicitly.

### `.gitignore`

```gitignore
target/
.env
*.cookies
```

### `src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: spring-security-mini-demo

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/security_demo}
    username: ${DB_USERNAME:security_demo}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1

  flyway:
    enabled: true

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

server:
  servlet:
    session:
      timeout: 30m
      cookie:
        http-only: true
        secure: ${SESSION_COOKIE_SECURE:false}
        same-site: lax
```

There is deliberately no default for `DB_PASSWORD`. For local HTTP, `Secure` must be false or the browser/curl will not send the cookie; deploy behind HTTPS with `SESSION_COOKIE_SECURE=true`.

### `src/main/resources/db/migration/V1__create_app_users.sql`

```sql
CREATE TABLE app_users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL,
    CONSTRAINT uq_app_users_username UNIQUE (username),
    CONSTRAINT ck_app_users_role CHECK (role IN ('USER', 'ADMIN'))
);
```

### `src/main/resources/db/migration/V2__create_admin_audit_events.sql`

```sql
CREATE TABLE admin_audit_events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### `src/main/java/com/example/securitydemo/SecurityDemoApplication.java`

```java
package com.example.securitydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityDemoApplication.class, args);
    }
}
```

### `src/main/java/com/example/securitydemo/user/Role.java`

```java
package com.example.securitydemo.user;

public enum Role {
    USER,
    ADMIN
}
```

### `src/main/java/com/example/securitydemo/user/AppUser.java`

```java
package com.example.securitydemo.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_app_users_username",
                columnNames = "username"
        )
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}
```

### `src/main/java/com/example/securitydemo/user/AppUserRepository.java`

```java
package com.example.securitydemo.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
```

### `src/main/java/com/example/securitydemo/user/DatabaseUserDetailsService.java`

```java
package com.example.securitydemo.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public DatabaseUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        String normalizedUsername = username.strip().toLowerCase(Locale.ROOT);

        AppUser appUser = appUserRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(appUser.getRole().name())
                .build();
    }
}
```

### `src/main/java/com/example/securitydemo/auth/RegisterRequest.java`

```java
package com.example.securitydemo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(
                regexp = "[A-Za-z0-9._-]+",
                message = "must contain only letters, digits, dot, underscore, or hyphen"
        )
        String username,

        @NotBlank
        @Size(min = 12, max = 72)
        String password
) {
}
```

### `src/main/java/com/example/securitydemo/auth/RegisterResponse.java`

```java
package com.example.securitydemo.auth;

public record RegisterResponse(String username, String role) {
}
```

### `src/main/java/com/example/securitydemo/auth/UsernameAlreadyExistsException.java`

```java
package com.example.securitydemo.auth;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException() {
        super("Username is not available");
    }
}
```

### `src/main/java/com/example/securitydemo/auth/RegistrationService.java`

```java
package com.example.securitydemo.auth;

import com.example.securitydemo.user.AppUser;
import com.example.securitydemo.user.AppUserRepository;
import com.example.securitydemo.user.Role;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.username().strip().toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        AppUser appUser = new AppUser(username, passwordHash, Role.USER);

        try {
            AppUser saved = appUserRepository.saveAndFlush(appUser);
            return new RegisterResponse(saved.getUsername(), saved.getRole().name());
        } catch (DataIntegrityViolationException exception) {
            // The UNIQUE constraint closes the race between existsByUsername and insert.
            throw new UsernameAlreadyExistsException();
        }
    }
}
```

### `src/main/java/com/example/securitydemo/auth/AuthController.java`

```java
package com.example.securitydemo.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public MeResponse me(
            @AuthenticationPrincipal UserDetails principal,
            Authentication authentication
    ) {
        List<String> roleAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .sorted()
                .toList();

        return new MeResponse(principal.getUsername(), roleAuthorities);
    }

    public record CsrfResponse(
            String headerName,
            String parameterName,
            String token
    ) {
    }

    public record MeResponse(
            String username,
            List<String> authorities
    ) {
    }
}
```

There is intentionally no `@PostMapping("/login")` and no logout controller. Those URLs are consumed by Spring Security filters.

### `src/main/java/com/example/securitydemo/security/RestAuthenticationEntryPoint.java`

```java
package com.example.securitydemo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {
                  "type": "about:blank",
                  "title": "Unauthorized",
                  "status": 401,
                  "detail": "Authentication is required."
                }
                """);
    }
}
```

### `src/main/java/com/example/securitydemo/security/RestAccessDeniedHandler.java`

```java
package com.example.securitydemo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {
                  "type": "about:blank",
                  "title": "Forbidden",
                  "status": 403,
                  "detail": "The request is not allowed."
                }
                """);
    }
}
```

### `src/main/java/com/example/securitydemo/security/SecurityConfig.java`

```java
package com.example.securitydemo.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(GET, "/public/hello", "/auth/csrf").permitAll()
                        .requestMatchers(POST, "/auth/register", "/auth/login").permitAll()
                        .requestMatchers(GET, "/auth/me").authenticated()
                        .requestMatchers(GET, "/user/hello").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(GET, "/admin/hello").hasRole("ADMIN")
                        .requestMatchers(POST, "/admin/action", "/admin/audit")
                            .hasRole("ADMIN")
                        .requestMatchers(POST, "/auth/logout").authenticated()
                        .anyRequest().denyAll()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)
                        )
                        .failureHandler((request, response, exception) ->
                                authenticationEntryPoint.commence(request, response, exception)
                        )
                )
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.changeSessionId())
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
```

### `src/main/java/com/example/securitydemo/publicapi/PublicController.java`

```java
package com.example.securitydemo.publicapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "hello from a public endpoint");
    }
}
```

### `src/main/java/com/example/securitydemo/user/UserController.java`

```java
package com.example.securitydemo.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/hello")
    public Map<String, String> hello(Principal principal) {
        return Map.of(
                "message", "hello from a USER-or-ADMIN endpoint",
                "username", principal.getName()
        );
    }
}
```

### `src/main/java/com/example/securitydemo/admin/AdminAuditEvent.java`

```java
package com.example.securitydemo.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "admin_audit_events")
public class AdminAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AdminAuditEvent() {
    }

    public AdminAuditEvent(String username, String action, Instant createdAt) {
        this.username = username;
        this.action = action;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
```

### `src/main/java/com/example/securitydemo/admin/AdminAuditEventRepository.java`

```java
package com.example.securitydemo.admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditEventRepository
        extends JpaRepository<AdminAuditEvent, Long> {
}
```

### `src/main/java/com/example/securitydemo/admin/AdminAuditResponse.java`

```java
package com.example.securitydemo.admin;

import java.time.Instant;

public record AdminAuditResponse(
        Long id,
        String action,
        Instant createdAt
) {
}
```

### `src/main/java/com/example/securitydemo/admin/AdminOperationService.java`

```java
package com.example.securitydemo.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdminOperationService {

    private final AdminAuditEventRepository adminAuditEventRepository;

    public AdminOperationService(
            AdminAuditEventRepository adminAuditEventRepository
    ) {
        this.adminAuditEventRepository = adminAuditEventRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String executeAdminOperation() {
        return "method-security-protected operation executed";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AdminAuditResponse recordAdminAudit(String username) {
        AdminAuditEvent event = new AdminAuditEvent(
                username,
                "ADMIN_DEMO_ACTION",
                Instant.now()
        );
        AdminAuditEvent saved = adminAuditEventRepository.save(event);

        return new AdminAuditResponse(
                saved.getId(),
                saved.getAction(),
                saved.getCreatedAt()
        );
    }
}
```

### `src/main/java/com/example/securitydemo/admin/AdminWorkflowService.java`

```java
package com.example.securitydemo.admin;

import org.springframework.stereotype.Service;

@Service
public class AdminWorkflowService {

    private final AdminOperationService adminOperationService;

    public AdminWorkflowService(AdminOperationService adminOperationService) {
        this.adminOperationService = adminOperationService;
    }

    public String runProtectedOperation() {
        // Cross-bean call: it passes through AdminOperationService's security proxy.
        return adminOperationService.executeAdminOperation();
    }
}
```

### `src/main/java/com/example/securitydemo/admin/AdminController.java`

```java
package com.example.securitydemo.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminWorkflowService adminWorkflowService;
    private final AdminOperationService adminOperationService;

    public AdminController(
            AdminWorkflowService adminWorkflowService,
            AdminOperationService adminOperationService
    ) {
        this.adminWorkflowService = adminWorkflowService;
        this.adminOperationService = adminOperationService;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "hello from an ADMIN-only endpoint");
    }

    @PostMapping("/action")
    public Map<String, String> action() {
        return Map.of("message", adminWorkflowService.runProtectedOperation());
    }

    @PostMapping("/audit")
    public AdminAuditResponse audit(
            @AuthenticationPrincipal UserDetails principal
    ) {
        return adminOperationService.recordAdminAudit(principal.getUsername());
    }
}
```

### `src/main/java/com/example/securitydemo/bootstrap/LocalAdminBootstrap.java`

```java
package com.example.securitydemo.bootstrap;

import com.example.securitydemo.user.AppUser;
import com.example.securitydemo.user.AppUserRepository;
import com.example.securitydemo.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

@Component
@Profile("local")
public class LocalAdminBootstrap implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(LocalAdminBootstrap.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public LocalAdminBootstrap(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${LOCAL_ADMIN_USERNAME:}") String adminUsername,
            @Value("${LOCAL_ADMIN_PASSWORD:}") String adminPassword
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminUsername)
                || !StringUtils.hasText(adminPassword)) {
            log.info("Local admin provisioning skipped: credentials are not set");
            return;
        }

        String username = adminUsername.strip().toLowerCase(Locale.ROOT);
        Optional<AppUser> existing = appUserRepository.findByUsername(username);

        if (existing.isPresent()) {
            if (existing.get().getRole() == Role.ADMIN) {
                log.info("Local admin account already exists; leaving it unchanged");
            } else {
                log.warn("Local admin username belongs to a USER; not promoting it");
            }
            return;
        }

        String passwordHash = passwordEncoder.encode(adminPassword);
        appUserRepository.saveAndFlush(
                new AppUser(username, passwordHash, Role.ADMIN)
        );
        log.info("Created the disposable local admin account '{}'; password not logged",
                username);
    }
}
```

### `src/main/java/com/example/securitydemo/web/ApiExceptionHandler.java`

```java
package com.example.securitydemo.web;

import com.example.securitydemo.auth.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> duplicateUsername(
            UsernameAlreadyExistsException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
        problem.setTitle("Registration conflict");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validationFailure(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );
        problem.setTitle("Invalid request");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
```

## 28. How to run

Prerequisites:

- JDK 21;
- Maven 3.9 or newer;
- Docker Desktop or another Docker Engine with Compose.

Check them from PowerShell:

```powershell
java -version
mvn -version
docker version
docker compose version
```

Open PowerShell in `spring-security-mini-demo`. In terminal 1:

```powershell
if (-not (Test-Path -LiteralPath '.env')) {
    Copy-Item -LiteralPath '.env.example' -Destination '.env'
}

docker compose up -d
docker compose ps
```

Set the host-side application environment. These values intentionally match `.env.example`:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/security_demo'
$env:DB_USERNAME = 'security_demo'
$env:DB_PASSWORD = 'change-me-local-only'

$env:SPRING_PROFILES_ACTIVE = 'local'
$env:LOCAL_ADMIN_USERNAME = 'localadmin'
$env:LOCAL_ADMIN_PASSWORD = 'Local-admin-demo-123!'
$env:SESSION_COOKIE_SECURE = 'false'

mvn spring-boot:run
```

These are disposable local-learning credentials. Do not reuse them. If `.env` uses a different database name, port, username, or password, change the corresponding `DB_...` value too.

The expected startup order in the log is approximately:

```text
HikariCP connects
→ Flyway validates/applies V1 and V2
→ Hibernate validates entity mappings
→ Tomcat listens on port 8080
→ local ApplicationRunner provisions ADMIN if appropriate
```

Stop the Java process with Ctrl+C. Stop PostgreSQL later with:

```powershell
docker compose down
```

That preserves the named database volume, so restarting does not erase accounts or migration history.

## 29. Manual testing

Use `curl.exe`, not bare `curl`, because Windows PowerShell versions can alias `curl` to `Invoke-WebRequest`.

### 29.1 Prepare a second PowerShell terminal

```powershell
$BaseUrl = 'http://localhost:8080'

$UserName = 'alice'
$UserPassword = 'Alice-demo-password-123!'
$AdminName = 'localadmin'
$AdminPassword = 'Local-admin-demo-123!'

$UserJar = Join-Path $PWD 'user.cookies'
$AdminJar = Join-Path $PWD 'admin.cookies'
$ProbeJar = Join-Path $PWD 'probe.cookies'

Remove-Item -LiteralPath $UserJar, $AdminJar, $ProbeJar `
    -ErrorAction SilentlyContinue

$StatusFormat = "`nHTTP %{http_code}`n"

function Get-Csrf {
    param(
        [Parameter(Mandatory)]
        [string] $CookieJar
    )

    $json = curl.exe --silent --show-error --fail-with-body `
        --cookie "$CookieJar" `
        --cookie-jar "$CookieJar" `
        "$BaseUrl/auth/csrf"

    if ($LASTEXITCODE -ne 0) {
        throw 'Could not obtain a CSRF token.'
    }

    return $json | ConvertFrom-Json
}

function Get-JSessionId {
    param(
        [Parameter(Mandatory)]
        [string] $CookieJar
    )

    if (-not (Test-Path -LiteralPath $CookieJar)) {
        return $null
    }

    $line = Get-Content -LiteralPath $CookieJar |
        Where-Object { $_ -match "`tJSESSIONID`t" } |
        Select-Object -Last 1

    if ($null -eq $line) {
        return $null
    }

    return ($line -split "`t")[-1]
}
```

`--cookie` (`-b`) sends stored cookies. `--cookie-jar` (`-c`) accepts and saves response cookies. Use both on stateful calls so curl captures login-time session-ID rotation and logout-time deletion.

### 29.2 Verify public versus anonymous access

```powershell
curl.exe -sS -w $StatusFormat "$BaseUrl/public/hello"
# Expected: 200

curl.exe -sS -w $StatusFormat "$BaseUrl/auth/me"
# Expected: 401 application/problem+json
```

### 29.3 Prove that `permitAll` does not bypass CSRF

```powershell
$RegisterJson = @{
    username = $UserName
    password = $UserPassword
} | ConvertTo-Json -Compress

$RegisterJson | curl.exe -sS -w $StatusFormat `
    -b "$ProbeJar" -c "$ProbeJar" `
    -H 'Content-Type: application/json' `
    --data-binary '@-' `
    "$BaseUrl/auth/register"
# Expected: 403 because the POST has no CSRF token.
```

The request is allowed to be anonymous by URL policy, but `CsrfFilter` rejects it before that matters.

### 29.4 Fetch CSRF and register a USER

```powershell
$UserCsrf = Get-Csrf -CookieJar $UserJar
$UserCsrf | Format-List headerName, parameterName, token

$UserAnonymousId = Get-JSessionId -CookieJar $UserJar
"Anonymous JSESSIONID: $UserAnonymousId"

$RegisterJson | curl.exe -sS -i -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    -H 'Content-Type: application/json' `
    -H "$($UserCsrf.headerName): $($UserCsrf.token)" `
    --data-binary '@-' `
    "$BaseUrl/auth/register"
# Expected: 201 with {"username":"alice","role":"USER"}.
```

Registration persists Alice but does not log her in:

```powershell
curl.exe -sS -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    "$BaseUrl/auth/me"
# Expected: 401
```

Optional race/invariant lesson: repeat the registration with a valid token. The service/database reject the duplicate:

```powershell
$UserCsrf = Get-Csrf -CookieJar $UserJar

$RegisterJson | curl.exe -sS -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    -H 'Content-Type: application/json' `
    -H "$($UserCsrf.headerName): $($UserCsrf.token)" `
    --data-binary '@-' `
    "$BaseUrl/auth/register"
# Expected: 409
```

### 29.5 Distinguish missing CSRF from wrong credentials

A bad password with a valid CSRF token reaches the login filter and returns `401`:

```powershell
$ProbeCsrf = Get-Csrf -CookieJar $ProbeJar

curl.exe -sS -w $StatusFormat `
    -b "$ProbeJar" -c "$ProbeJar" `
    -H "$($ProbeCsrf.headerName): $($ProbeCsrf.token)" `
    -H 'Content-Type: application/x-www-form-urlencoded' `
    --data-urlencode "username=$UserName" `
    --data-urlencode 'password=definitely-wrong' `
    "$BaseUrl/auth/login"
# Expected: 401
```

The same login request without a CSRF token receives `403` before credential verification.

### 29.6 Log in as USER and observe session fixation protection

Fetch a token immediately before login, and record the anonymous session identifier:

```powershell
$UserCsrfBeforeLogin = Get-Csrf -CookieJar $UserJar
$UserPreLoginId = Get-JSessionId -CookieJar $UserJar

curl.exe -sS -i -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    -H "$($UserCsrfBeforeLogin.headerName): $($UserCsrfBeforeLogin.token)" `
    -H 'Content-Type: application/x-www-form-urlencoded' `
    --data-urlencode "username=$UserName" `
    --data-urlencode "password=$UserPassword" `
    "$BaseUrl/auth/login"
# Expected: 204 No Content
```

Inspect the new identifier:

```powershell
$UserPostLoginId = Get-JSessionId -CookieJar $UserJar

[pscustomobject]@{
    BeforeLogin = $UserPreLoginId
    AfterLogin  = $UserPostLoginId
    Rotated     = $UserPreLoginId -ne $UserPostLoginId
}
```

`Rotated` should be `True` with the configured `changeSessionId` protection. Prove the pre-login ID is not an authenticated session:

```powershell
curl.exe -sS -w $StatusFormat `
    --cookie "JSESSIONID=$UserPreLoginId" `
    "$BaseUrl/auth/me"
# Expected: 401
```

Do not add the current cookie jar to that probe, or it would also send the valid post-login ID.

Successful authentication clears the earlier CSRF token. Fetch a fresh one:

```powershell
$UserCsrf = Get-Csrf -CookieJar $UserJar
```

### 29.7 Exercise USER authorization

```powershell
curl.exe -sS -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    "$BaseUrl/auth/me"
# Expected: 200 with alice and ROLE_USER only in the projected authorities list.

curl.exe -sS -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    "$BaseUrl/user/hello"
# Expected: 200

curl.exe -sS -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    "$BaseUrl/admin/hello"
# Expected: 403
```

For an unsafe authorization test, include a valid token; otherwise CSRF—not the role—would explain the `403`:

```powershell
$UserCsrf = Get-Csrf -CookieJar $UserJar

curl.exe -sS -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    -H "$($UserCsrf.headerName): $($UserCsrf.token)" `
    -X POST `
    "$BaseUrl/admin/action"
# Expected: 403 because Alice has ROLE_USER, not ROLE_ADMIN.
```

### 29.8 Log in separately as ADMIN

The second cookie jar models another browser session. Reusing Alice’s jar would replace that session’s identity rather than create an independent admin client.

```powershell
$AdminCsrfBeforeLogin = Get-Csrf -CookieJar $AdminJar
$AdminPreLoginId = Get-JSessionId -CookieJar $AdminJar

curl.exe -sS -i -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    -H "$($AdminCsrfBeforeLogin.headerName): $($AdminCsrfBeforeLogin.token)" `
    -H 'Content-Type: application/x-www-form-urlencoded' `
    --data-urlencode "username=$AdminName" `
    --data-urlencode "password=$AdminPassword" `
    "$BaseUrl/auth/login"
# Expected: 204

$AdminPostLoginId = Get-JSessionId -CookieJar $AdminJar

[pscustomobject]@{
    BeforeLogin = $AdminPreLoginId
    AfterLogin  = $AdminPostLoginId
    Rotated     = $AdminPreLoginId -ne $AdminPostLoginId
}

$AdminCsrf = Get-Csrf -CookieJar $AdminJar
```

Test the safe endpoints:

```powershell
curl.exe -sS -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    "$BaseUrl/auth/me"
# Expected: 200 with localadmin and ROLE_ADMIN in the projected list.

curl.exe -sS -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    "$BaseUrl/user/hello"
# Expected: 200 because that route allows USER or ADMIN.

curl.exe -sS -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    "$BaseUrl/admin/hello"
# Expected: 200
```

First omit CSRF deliberately:

```powershell
curl.exe -sS -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    -X POST `
    "$BaseUrl/admin/action"
# Expected: 403 from CSRF; the service does not execute.
```

Now call the cross-bean, method-security-protected operation correctly:

```powershell
$AdminCsrf = Get-Csrf -CookieJar $AdminJar

curl.exe -sS -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    -H "$($AdminCsrf.headerName): $($AdminCsrf.token)" `
    -X POST `
    "$BaseUrl/admin/action"
# Expected: 200 with the method-security-protected message.
```

Call the separate method protected by both `@PreAuthorize` and `@Transactional`:

```powershell
$AdminCsrf = Get-Csrf -CookieJar $AdminJar

curl.exe -sS -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    -H "$($AdminCsrf.headerName): $($AdminCsrf.token)" `
    -X POST `
    "$BaseUrl/admin/audit"
# Expected: 200 with an id, ADMIN_DEMO_ACTION, and createdAt.
```

That last request follows:

```text
HTTP hasRole("ADMIN")
→ controller
→ service proxy
→ @PreAuthorize("hasRole('ADMIN')")
→ @Transactional
→ repository
→ PostgreSQL
→ commit
```

### 29.9 Logout and replay the old ID

Save the authenticated identifier and obtain a current token:

```powershell
$AdminAuthenticatedId = Get-JSessionId -CookieJar $AdminJar
$AdminCsrfForLogout = Get-Csrf -CookieJar $AdminJar

curl.exe -sS -i -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    -H "$($AdminCsrfForLogout.headerName): $($AdminCsrfForLogout.token)" `
    -X POST `
    "$BaseUrl/auth/logout"
# Expected: 204
```

The updated cookie jar no longer authenticates:

```powershell
curl.exe -sS -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    "$BaseUrl/auth/me"
# Expected: 401
```

Even manually replaying the value captured before logout fails because its server-side session was invalidated:

```powershell
curl.exe -sS -w $StatusFormat `
    --cookie "JSESSIONID=$AdminAuthenticatedId" `
    "$BaseUrl/auth/me"
# Expected: 401
```

After logout, fetch a new anonymous-session token before another unsafe request:

```powershell
$AdminCsrfAfterLogout = Get-Csrf -CookieJar $AdminJar

curl.exe -sS -w $StatusFormat `
    -b "$AdminJar" -c "$AdminJar" `
    -H "$($AdminCsrfAfterLogout.headerName): $($AdminCsrfAfterLogout.token)" `
    -X POST `
    "$BaseUrl/admin/audit"
# Expected: 401: CSRF passed, but authentication is now missing.
```

Logging out the admin jar does not affect Alice’s independent session:

```powershell
curl.exe -sS -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    "$BaseUrl/auth/me"
# Expected: 200
```

Finally, log Alice out with Alice’s current token:

```powershell
$UserCsrfForLogout = Get-Csrf -CookieJar $UserJar

curl.exe -sS -w $StatusFormat `
    -b "$UserJar" -c "$UserJar" `
    -H "$($UserCsrfForLogout.headerName): $($UserCsrfForLogout.token)" `
    -X POST `
    "$BaseUrl/auth/logout"
# Expected: 204
```

Cookie jars contain bearer-like session identifiers. Keep them local and out of source control.

### 29.10 Expected-status matrix

| Request and state | Expected | Which check explains it? |
|---|---:|---|
| `GET /public/hello`, anonymous | `200` | public URL rule |
| `GET /auth/me`, anonymous | `401` | authentication required |
| `POST /auth/register`, no CSRF | `403` | CSRF before authorization |
| `POST /auth/register`, valid CSRF | `201` | validation and registration succeed |
| duplicate registration | `409` | application/database invariant |
| `POST /auth/login`, valid CSRF, wrong password | `401` | authentication failure handler |
| `POST /auth/login`, no CSRF | `403` | rejected before credential processing |
| `GET /user/hello`, USER | `200` | USER role accepted |
| `GET /admin/hello`, USER | `403` | authenticated but lacks ADMIN |
| `POST /admin/action`, USER + valid CSRF | `403` | URL authorization |
| `POST /admin/action`, ADMIN without CSRF | `403` | CSRF; service is not reached |
| `POST /admin/action`, ADMIN + valid CSRF | `200` | URL and method authorization pass |
| `POST /admin/audit`, ADMIN + valid CSRF | `200` | authorization passes and transaction commits |
| `/auth/me` with pre-login ID after rotation | `401` | old anonymous ID is not authenticated |
| `/auth/me` with pre-logout ID after logout | `401` | server-side session was invalidated |

When testing role authorization on an unsafe method, always include a valid CSRF token; otherwise two different causes both appear as `403`.

## 30. End-to-end request traces

The filter list below is intentionally simplified to the parts relevant to this project:

```text
SecurityContextHolderFilter
→ CsrfFilter
→ LogoutFilter
→ UsernamePasswordAuthenticationFilter
→ ExceptionTranslationFilter
→ AuthorizationFilter
```

Other framework filters also exist. The ordering explains why CSRF can reject login, logout, or an admin POST before credential/role logic runs.

### A. Successful login

```text
Client
  │ POST /auth/login
  │ Cookie: JSESSIONID=S0
  │ X-CSRF-TOKEN: T0
  │ username=alice&password=...
  ▼
SecurityFilterChain
  ▼
CsrfFilter validates T0 for session S0
  ▼
UsernamePasswordAuthenticationFilter
  │ creates UsernamePasswordAuthenticationToken
  │ authenticated=false
  ▼
AuthenticationManager
  ▼
ProviderManager
  │ supports check chooses
  ▼
DaoAuthenticationProvider
  ├── DatabaseUserDetailsService
  │     └── AppUserRepository
  │           └── JPA → Hibernate → JDBC → HikariCP → PostgreSQL
  └── PasswordEncoder.matches(raw, stored BCrypt hash)
  ▼
UsernamePasswordAuthenticationToken
  │ authenticated=true
  │ principal=UserDetails("alice")
  │ authorities include ROLE_USER and FACTOR_PASSWORD
  │ credentials erased
  ▼
SecurityContextHolder
  ▼ explicit save by built-in authentication mechanism
SecurityContextRepository → HTTP session
  ▼ session-fixation protection changes S0 to S1
Client receives updated JSESSIONID=S1
  ▼
204 No Content
```

### B. Later authenticated request

```text
GET /auth/me + Cookie: JSESSIONID=S1
        ↓
servlet container locates server-side session
        ↓
SecurityContextRepository supplies saved SecurityContext
        ↓
SecurityContextHolderFilter associates it with this request/thread
        ↓
Authentication = Alice, authenticated=true, authorities=[...]
        ↓
authenticated() authorization passes
        ↓
AuthController.me(...)
        ↓
safe JSON projection
        ↓
request ends; thread-local holder cleared
        ↓
session still retains the saved context for a future request
```

### C. USER accesses an ADMIN endpoint

```text
GET /admin/hello + Alice's JSESSIONID
        ↓
restore SecurityContext
        ↓
Authentication authorities include ROLE_USER
        ↓
hasRole("ADMIN") asks for ROLE_ADMIN
        ↓ missing
AccessDeniedHandler
        ↓
403 application/problem+json
        ↓
AdminController is never called
```

### D. ADMIN reaches the transactional service

```text
POST /admin/audit + admin cookie + valid CSRF token
        ↓
CsrfFilter passes
        ↓
HTTP hasRole("ADMIN") passes
        ↓
AdminController
        ↓
AdminOperationService Spring proxy
        ↓
@PreAuthorize("hasRole('ADMIN')") passes
        ↓
@Transactional starts transaction
        ↓
AdminAuditEventRepository
        ↓
Hibernate → JDBC → HikariCP → PostgreSQL
        ↓
normal return: commit
exception: rollback
```

### E. Logout

```text
POST /auth/logout + JSESSIONID=S1 + current CSRF token
        ↓
CsrfFilter validates token
        ↓
LogoutFilter
        ├── invalidates session S1
        ├── clears Authentication
        ├── clears SecurityContextHolder
        ├── saves an empty context as appropriate
        └── expires the client's JSESSIONID cookie
        ↓
204 No Content

replay JSESSIONID=S1
        ↓
no authenticated server-side session exists
        ↓
GET /auth/me → 401
```

### Status decision tree for unsafe requests

```text
unsafe request
    ↓
valid CSRF token for this session?
    ├── no  → 403 (authentication/role test may never occur)
    └── yes
          ↓
      authenticated where required?
          ├── no  → 401
          └── yes
                ↓
            required authority present?
                ├── no  → 403
                └── yes → controller/service
```

## 31. How all files connect

| File/class | Why it exists; who calls it; what it calls |
|---|---|
| `SecurityDemoApplication` | Boot entry point. Starts component scanning and auto-configuration. |
| `SecurityConfig` | Developer’s HTTP/method-security policy and BCrypt choice. Boot supplies `HttpSecurity`; the bean returns the built filter chain. |
| `RestAuthenticationEntryPoint` | Spring calls it when a protected request lacks valid authentication; login failure deliberately delegates to it for the same generic 401 body. |
| `RestAccessDeniedHandler` | Spring calls it for authorization/CSRF access denial; it emits a safe 403 problem document. |
| `Role` | Persistence enum restricted to `USER` and `ADMIN`; it has no `ROLE_` prefix. |
| `AppUser` | JPA persistence model for `app_users`; never returned by a controller. |
| `AppUserRepository` | Developer-written Spring Data interface; Spring creates the runtime proxy used by registration, bootstrap, and user loading. |
| `DatabaseUserDetailsService` | `DaoAuthenticationProvider` calls it with a username. It calls the repository and maps `AppUser` to `UserDetails`. |
| `RegisterRequest` | Validated public input with no role field. |
| `RegistrationService` | Transactional registration use case. It normalizes, checks, hashes, assigns `USER`, and persists. |
| `AuthController` | Owns registration, CSRF-token retrieval, and safe current-user projection. It does not implement login/logout. |
| `PublicController` | Demonstrates a truly public safe GET. |
| `UserController` | Demonstrates HTTP role authorization for USER or ADMIN. |
| `AdminController` | HTTP adapter for ADMIN GET/POST operations. Calls protected service beans. |
| `AdminWorkflowService` | Demonstrates one Spring bean calling another bean’s method-security proxy. |
| `AdminOperationService` | Holds meaningful `@PreAuthorize` checks; one method also opens a transaction and writes an audit row. |
| `AdminAuditEvent` / repository | Persistence model and Spring Data interface that make the protected transaction visible. |
| `LocalAdminBootstrap` | Local-profile-only runner that safely inserts, but never promotes, the learning ADMIN account. |
| `ApiExceptionHandler` | MVC-layer problem details for validation and duplicate registration; it does not replace filter-level handlers. |
| `application.yaml` | Wires datasource/Hikari, Flyway, Hibernate validation, and servlet-session cookie behavior. |
| `V1` / `V2` | Flyway-owned schema history for users and audit events. |
| `compose.yaml` | Runs the PostgreSQL development dependency with a healthcheck and persistent volume. |

The principal dependency relationships are:

```text
SecurityConfig
├── PasswordEncoder bean
├── RestAuthenticationEntryPoint
└── RestAccessDeniedHandler

Spring form-login filter
└── AuthenticationManager (ProviderManager)
    └── DaoAuthenticationProvider
        ├── DatabaseUserDetailsService
        │   └── AppUserRepository
        │       └── PostgreSQL
        └── PasswordEncoder

AuthController
└── RegistrationService
    ├── PasswordEncoder
    └── AppUserRepository

AdminController
├── AdminWorkflowService
│   └── proxied AdminOperationService.executeAdminOperation
└── proxied AdminOperationService.recordAdminAudit
    └── AdminAuditEventRepository
        └── PostgreSQL

LocalAdminBootstrap
├── PasswordEncoder
└── AppUserRepository
```

For every important application class, the input/output view is:

| Class | Input | Output |
|---|---|---|
| `DatabaseUserDetailsService` | username | `UserDetails` or `UsernameNotFoundException` |
| `RegistrationService` | validated `RegisterRequest` | safe `RegisterResponse` or conflict exception |
| `PasswordEncoder` | raw password for `encode`, or raw+hash for `matches` | hash, or verification boolean |
| `AuthController.me` | current security principal/token | username plus role-authority strings |
| `AdminOperationService.recordAdminAudit` | current username after authorization | safe audit DTO; committed database row |
| security handlers | servlet request plus security exception | 401/403 HTTP response; no sensitive details |

## 32. What I wrote vs what Spring does automatically

### Application/developer code

```text
I write/configure:
├── SecurityFilterChain policy
├── PasswordEncoder choice
├── UserDetailsService mapping
├── entity and repository interfaces
├── migrations
├── registration rules
├── controllers and protected services
├── @PreAuthorize expressions
├── REST 401/403 response handlers
└── local admin provisioning policy
```

### Framework/runtime infrastructure

```text
Spring Security supplies:
├── DelegatingFilterProxy / FilterChainProxy
├── SecurityContextHolderFilter
├── CsrfFilter
├── LogoutFilter
├── UsernamePasswordAuthenticationFilter
├── ExceptionTranslationFilter
├── AuthorizationFilter
├── AuthenticationManager contract implementation
├── ProviderManager
├── DaoAuthenticationProvider
├── password-verification call during authentication
├── SecurityContext creation/load/save/cleanup integration
├── session-fixation protection
├── built-in login and logout processing
└── method-security interceptors/proxies

Spring Boot supplies/configures:
├── embedded Tomcat
├── application configuration binding
├── DataSource and HikariCP
├── Flyway startup integration
├── JPA/Hibernate setup
└── framework object assembly and lifecycle

Spring Data supplies:
└── runtime implementations of repository interfaces

Tomcat/servlet infrastructure supplies:
└── HTTP session storage and JSESSIONID cookie mechanics
```

The boundary is precise: our `SecurityConfig` asks for form login; it does not instantiate the form-login filter. Our `UserDetailsService` loads a user; it does not verify the submitted password. Our `PasswordEncoder` bean selects the algorithm; `DaoAuthenticationProvider` calls `matches`. Our success handler chooses `204`; Spring’s authentication filter has already created and explicitly saved the successful context before invoking it.

Likewise, defining a custom `UserDetailsService` makes Boot’s generated in-memory user back off, and defining a `SecurityFilterChain` makes Boot’s default catch-all chain back off. That does **not** mean all Spring Security infrastructure disappears; the framework builds the standard chain around our policy and collaborators.

## 33. Common mistakes

| Mistake | Why it is wrong | Correct mental move |
|---|---|---|
| Writing a login controller that loads a user and compares strings | Bypasses the standard filter/provider/context/session lifecycle | Configure built-in form login and provide `UserDetailsService` + `PasswordEncoder` |
| Posting JSON to `/auth/login` | The standard form filter reads request parameters, not a JSON body | Use `application/x-www-form-urlencoded` |
| Comparing `encode(raw)` with the stored hash | BCrypt uses a new random salt | Use `matches(raw, storedHash)` |
| Passing `roles("ROLE_ADMIN")` | `roles` adds `ROLE_` itself | Pass `roles("ADMIN")` |
| Assuming ADMIN automatically has USER | No role hierarchy is configured | Use `hasAnyRole("USER", "ADMIN")` where both should enter |
| Returning `AppUser` or raw `UserDetails` | Can expose the password hash and internal flags | Return a purpose-built safe DTO |
| Accepting `role` during public registration | Enables privilege escalation | Server always assigns `Role.USER` |
| Checking duplicates only in Java | Two concurrent inserts can race | Keep the database `UNIQUE` constraint |
| Using `ddl-auto: update` with Flyway | Creates two competing schema owners | Flyway migrates; Hibernate validates |
| Disabling CSRF because responses are JSON | Browser cookies are still attached automatically | Keep CSRF for session/cookie authentication |
| Believing `permitAll` skips CSRF | Authorization and CSRF are separate filters | Send a token even to public unsafe endpoints |
| Reusing the pre-login CSRF token | Successful authentication clears the old token | Fetch `/auth/csrf` after login |
| Using only curl `-b` or only `-c` | The client may fail to send or save a rotated session cookie | Use both for every stateful step |
| Setting cookie `Secure=true` on local HTTP | A conforming client will not send that cookie over HTTP | Use false locally; true behind production HTTPS |
| Treating `JSESSIONID` as the security context | It is only an opaque session lookup key | Follow it to server session → context → authentication |
| Assuming thread-context cleanup logs out | The session remains authenticated | Logout must invalidate persistent session state |
| Expecting `@RestControllerAdvice` to format filter failures | Many failures happen before MVC | Configure entry point and denied handler |
| Calling an annotated method via `this` | Proxy advice is normally bypassed | Cross a Spring bean proxy boundary |
| Assuming `@Transactional` grants permission | Transactions manage commit/rollback only | Use authorization separately |
| Adding a custom provider bean casually | It can change/back off automatic username/password provider assembly | Keep the API-key example conceptual or explicitly register all providers |
| Placing a broad matcher before a specific one | First matching rule wins | Order from specific to fallback |
| Denying internal `ERROR` dispatches | A real 400/500 may be overwritten by 401/403 | Permit only `DispatcherType.ERROR`, then keep normal requests deny-by-default |

## 34. Debugging guide

| Symptom | First component to inspect | Concrete checks |
|---|---|---|
| Username not found | `DatabaseUserDetailsService` / repository | normalization, stored lowercase username, SQL row, datasource target |
| Password always fails | stored hash / `PasswordEncoder` | database value begins like BCrypt, registration used `encode`, login uses same encoder |
| Login endpoint returns 403 | CSRF path | same cookie jar and token, header name/value, token fetched before login |
| Wrong password does not return 401 | login failure handler | ensure CSRF passed first and request is URL-encoded |
| Login returns 204 but next request is 401 | cookie/session handling | use both `-b` and `-c`, inspect rotated ID, do not set Secure on HTTP |
| `/auth/me` has an unexpected factor authority | Spring Security 7 authentication result | `FACTOR_PASSWORD` is framework-added; role checks still use `ROLE_...` |
| ADMIN route fails after successful login | role mapping | database `ADMIN` → `.roles("ADMIN")` → `ROLE_ADMIN`; inspect `/auth/me` |
| POST gets 403 unexpectedly | `CsrfFilter` first | fetch token for the same current session, especially after login/logout |
| Controller never executes | filter-chain matcher/CSRF | method + path, first matching rule, final `denyAll`, HTTP status |
| Validation should be 400 but becomes 401/403 | error dispatch rule | ensure `DispatcherType.ERROR` is permitted internally |
| Method call is denied | `@PreAuthorize` and current context | authority, proxy boundary, caller thread, no self-invocation |
| Method annotation appears ignored | proxy topology | protected class is a Spring bean and call comes through its injected proxy |
| Database change rolls back | transaction boundary / exception | exception after save, proxy crossing, PostgreSQL constraint, logs |
| Startup says schema validation failed | Flyway + entity mapping | applied migration version, column type/name/nullability, correct database |
| Default generated user appears | Boot back-off | verify custom `UserDetailsService` is component-scanned |
| Old ID still appears in a file after logout | server versus client state | replay it; invalid session should still yield 401 even if text remains locally |

For temporary diagnosis, this can reveal which security rule/filter handled a request:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

Remove verbose security logging after diagnosis. Never add code that logs submitted passwords, CSRF tokens, or session IDs.

## 35. Interview questions

1. Walk through a successful form login from HTTP bytes to a saved `SecurityContext`; name each major collaborator.
2. Why is `AuthenticationManager` an interface while `ProviderManager` delegates to multiple providers?
3. What application behavior changes when you define a custom `UserDetailsService` but do not define a custom provider?
4. How would adding an API-key mechanism change the filter, token type, and provider set without disturbing password login?
5. A user’s role changes in PostgreSQL while their session is active. Why might authorization still use the old role, and what invalidation strategies are possible?
6. Explain three different reasons a POST can return 403 and how you would isolate them.
7. Why can method security stop a scheduled/message-driven invocation that URL authorization cannot see?
8. If this service runs on three nodes, what must be true about session routing/storage for `JSESSIONID` authentication to work reliably?
9. Why are CORS allowlists, CSRF tokens, authentication, and authorization four independent decisions?
10. What security and data-consistency guarantees are provided—and not provided—by combining `@PreAuthorize` and `@Transactional`?

## 36. Self-test questions

Answer these without looking at the next section.

1. Who creates the unauthenticated username/password `Authentication` token?
2. Who calls `AuthenticationManager.authenticate(...)`?
3. Why does `AuthenticationManager` not directly query PostgreSQL?
4. How does `ProviderManager` choose which provider to ask?
5. What does `UserDetailsService` do, and what two common authentication tasks does it not do?
6. Who calls `PasswordEncoder.matches(...)` during this project’s login?
7. How are `AppUser`, `UserDetails`, and `Authentication` different?
8. What is stored in `SecurityContext`, and what does `SecurityContextHolder` add?
9. What does `JSESSIONID` contain, and what does it not contain?
10. Why is `UserDetailsService` usually not called again on every authenticated session request?
11. Why can Alice receive `403` after her username/password login succeeded?
12. Why does `permitAll()` on registration not remove its CSRF requirement?
13. Why use both `SecurityFilterChain` authorization and `@PreAuthorize`?
14. How does `@Transactional` differ from either form of authorization?
15. Why is clearing the thread-associated `SecurityContext` at request completion not logout?

## 37. Answer key

1. `UsernamePasswordAuthenticationFilter`, using submitted form parameters, creates the untrusted `UsernamePasswordAuthenticationToken`.
2. That authentication filter calls the manager as part of its standard authentication workflow.
3. The manager coordinates credential mechanisms. `ProviderManager` delegates the actual verification to a provider specialized for the presented token type.
4. It iterates configured providers in order, skips those whose `supports` is false, and lets supporting providers succeed, return `null`, or fail according to provider-manager rules.
5. It loads stored security-user data and maps it to `UserDetails`. It neither compares the submitted password nor authenticates/saves a context by itself.
6. `DaoAuthenticationProvider` calls `matches` with the submitted raw password and the encoded password from `UserDetails`.
7. `AppUser` is the JPA/database model; `UserDetails` is stored security-user information; `Authentication` is an untrusted request before verification or the trusted current identity after verification.
8. `SecurityContext` contains the current `Authentication`. `SecurityContextHolder` associates/accesses that context for current execution, normally the request thread.
9. It contains only an opaque session identifier. It does not contain the username, password, authorities, `Authentication`, or `SecurityContext`.
10. The HTTP-session repository restores the already authenticated context. This also means database role changes do not necessarily refresh an existing session immediately.
11. Authentication proved who Alice is; authorization can still find that `ROLE_USER` does not satisfy `ROLE_ADMIN`.
12. `permitAll` is an authorization rule, while `CsrfFilter` independently protects unsafe cookie-authenticated requests.
13. The chain protects HTTP routes. `@PreAuthorize` protects a service method reached through its Spring proxy, including callers that do not originate at that URL.
14. `@Transactional` controls the database unit of work—commit or rollback. It neither establishes identity nor grants permission.
15. Per-request cleanup prevents a pooled thread from leaking Alice into Bob’s request. The server-side session can still preserve Alice’s saved context; logout invalidates that longer-lived state.

## 38. Final mental model

Keep these pairs distinct:

| A | B | Difference |
|---|---|---|
| authentication | authorization | verifies identity vs decides permission |
| `Authentication` | `UserDetails` | current request/result identity vs loaded stored security user |
| `AppUser` | `UserDetails` | persistence entity vs security view |
| `SecurityContext` | HTTP session | contains current authentication vs persists state across requests |
| `SecurityContext` | `SecurityContextHolder` | data container vs current-execution access strategy |
| `AuthenticationManager` | `AuthenticationProvider` | coordination API vs one credential mechanism’s verifier |
| `ProviderManager` | `DaoAuthenticationProvider` | iterates providers vs handles username/password using user details |
| `SecurityFilterChain` | `@PreAuthorize` | HTTP boundary vs proxied method boundary |
| security | `@Transactional` | identity/permission vs commit/rollback |
| `401` | `403` | authentication needed/failed vs request forbidden/security check rejected |
| CORS | CSRF | browser cross-origin policy vs forged cookie-authenticated unsafe requests |
| `JSESSIONID` | `SecurityContext` | opaque lookup key vs server-side container holding authentication |

The complete project in one compact picture:

```text
                                    REGISTRATION
Client + CSRF ──POST /auth/register──> AuthController
                                           │ validated DTO, role forced to USER
                                           ▼
                                  RegistrationService @Transactional
                                           ├── BCryptPasswordEncoder.encode
                                           └── AppUserRepository
                                                    │
                                                    ▼
LOGIN                                         PostgreSQL
Client + CSRF ──POST /auth/login───────────────────┐
                                                   ▼
SecurityFilterChain → form-login filter → Authentication(false)
                                                   │
                                                   ▼
AuthenticationManager → ProviderManager → DaoAuthenticationProvider
                                                   │
                          ┌────────────────────────┴──────────────────┐
                          ▼                                           ▼
                 UserDetailsService                         PasswordEncoder.matches
                          │
                 AppUserRepository
                          │
          JPA → Hibernate → JDBC → HikariCP → PostgreSQL
                          │
                          └────────── UserDetails + stored hash ──────┘
                                                   │
                                                   ▼
                         Authentication(true, principal, authorities)
                                                   │
                                                   ▼
                       SecurityContext → HTTP session → JSESSIONID
                                                   │
                                                   ▼
LATER REQUEST: JSESSIONID → restore context → authorize URL
                                                   │ allowed
                                                   ▼
                              Controller → service proxy
                                      → @PreAuthorize
                                      → @Transactional when needed
                                      → repository → PostgreSQL
                                                   │
                                                   ▼
                                              HTTP response

REQUEST END: clear thread-associated holder; keep session state
LOGOUT: validate CSRF → invalidate session → old JSESSIONID cannot authenticate
```

The central insight is that the application supplies policy, persistence mapping, user loading, and business behavior. Spring Security supplies and coordinates the security machinery that turns an untrusted credential claim into a trusted, request-associated, session-persisted identity and then enforces policy around it.

### Documentation baseline

- [Spring Security 7 servlet authentication architecture](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/architecture.html)
- [DaoAuthenticationProvider](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/passwords/dao-authentication-provider.html)
- [Form login](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/passwords/form.html)
- [SecurityContext persistence](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/persistence.html)
- [CSRF protection](https://docs.spring.io/spring-security/reference/7.0/servlet/exploits/csrf.html)
- [Method security](https://docs.spring.io/spring-security/reference/7.0/servlet/authorization/method-security.html)
- [Spring Boot 4.1 Maven plugin and dependency-management guide](https://docs.spring.io/spring-boot/maven-plugin/using.html)
