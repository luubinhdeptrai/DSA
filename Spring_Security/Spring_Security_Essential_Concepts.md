# Spring Security — Essential Concepts

> A first-principles guide to authentication and authorization for the existing Book Catalog, with session authentication before JWT.

**Verified companion project:** `Flyway/book-catalog-api`  
**Baseline:** Java 21, Spring Boot 4.1.1 (managing Spring Security 7.1.1), Spring MVC, Spring Data JPA/Hibernate, PostgreSQL 17, HikariCP, Flyway migrations `V1`–`V5`, and Hibernate `ddl-auto: validate`. There is currently **no** Spring Security dependency, application-user table, or login mechanism. The next exercise adds a `V6` migration; it does not rewrite applied migrations.  
**Learning goal:** Explain how one authenticated HTTP request passes through security and reaches a protected controller—and why a rejected request often never reaches Spring MVC.

The existing routes are `GET /books`, `GET /books/{id}`, `POST /books`, `PUT /books/{id}`, `PATCH /books/{id}`, `DELETE /books/{id}`, and the transaction lab's `POST /transaction-lab/transfers`. A simple learning policy can leave book reads public and require `ADMIN` for mutations. The transaction-lab route is also a mutation and must not be overlooked. In the current snapshot, `BookService.delete` deliberately flushes and throws a checked exception to demonstrate rollback; adding security should preserve that behavior, not promise an ordinary `204` from that route.

## How to study

1. Predict whether each request is anonymous, authenticated, or authorized *before* looking at code.
2. Trace the filter chain separately from the controller/service/repository path.
3. For a database-backed login, identify the point at which Hibernate borrows a HikariCP connection.
4. Trace a later session request without assuming it repeats the password check or database lookup.
5. Treat CSRF and CORS as distinct browser concerns, not as synonyms for authentication.

## Concept priorities

| Priority | Meaning |
|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Needed to secure and diagnose ordinary Spring applications |
| ⭐⭐⭐⭐ IMPORTANT | Common practical or production design choice |
| ⭐⭐⭐ NICE TO KNOW | Useful for selected applications |
| ⭐⭐ FUTURE KNOWLEDGE | Recognize the boundary; study deeply later |

## 1. Why application security exists

**⭐⭐⭐⭐⭐ MUST KNOW**

An HTTP request arrives with data controlled by a client. The backend must decide whether that client may read or change a resource. It needs answers to seven questions: Who claims to be calling? How is that claim verified? What may the verified caller do? How are credentials stored? Which routes are protected? What response is sent when credentials are missing? What response is sent when permission is missing?

For Book Catalog, an anonymous visitor could read `/books`, a signed-in `USER` could identify themselves through `/auth/me`, and an `ADMIN` could create books. This is a *policy choice*, not something Spring Security infers from `@PostMapping`. A request to `POST /books` must be rejected unless the configured policy permits it.

## 2. Authentication versus authorization

**⭐⭐⭐⭐⭐ MUST KNOW**

| Question | Concept | Book Catalog example |
|---|---|---|
| “Who are you?” | Authentication | Check a submitted username and password, then identify `alice`. |
| “May you do this?” | Authorization | Decide whether `alice` has permission to `POST /books`. |

Authentication produces a trusted identity and authorities. Authorization uses that identity, its authorities, and the requested operation. A valid `USER` password does **not** imply permission to delete a book. Conversely, a request cannot be treated as an `ADMIN` merely because JSON says `{"role":"ADMIN"}`.

HTTP `401 Unauthorized` is historically named: in practice it means authentication is missing or invalid. HTTP `403 Forbidden` means access is refused; in the ordinary role-check example, the caller is authenticated but lacks permission. There are nuances—CSRF rejection can produce `403` before login—so diagnose the filter and reason, not only the status number.

## 3. Big-picture architecture

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
Client
  -> Servlet container
  -> DelegatingFilterProxy / FilterChainProxy
  -> selected SecurityFilterChain
       -> load an existing SecurityContext (if one exists)
       -> authentication filter, if this request submits credentials
            -> AuthenticationManager -> AuthenticationProvider
            -> UserDetailsService -> UserRepository
            -> Hibernate -> JDBC -> HikariCP -> PostgreSQL
            -> PasswordEncoder.matches(...)
       -> SecurityContext holding Authentication
       -> URL authorization decision
  -> DispatcherServlet -> BookController -> BookService
  -> BookRepository -> Hibernate -> JDBC -> HikariCP -> PostgreSQL
```

This is a *conditional* flow. The database-backed credential path runs for a login or another authentication event that needs it. A later request with an already-established HTTP session generally restores the saved security context instead of rechecking the password. A public request might need neither credentials nor a user query. The service/repository path happens only if the request reaches that application use case. [Spring Security servlet architecture](https://docs.spring.io/spring-security/reference/7.1/servlet/architecture.html), [authentication architecture](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/architecture.html).

## 4. The Spring Security filter chain

**⭐⭐⭐⭐⭐ MUST KNOW**

A servlet `Filter` sees a request before Spring MVC dispatches it to a controller. The container invokes Spring's `DelegatingFilterProxy`, which delegates to the Spring-managed `FilterChainProxy`. That proxy chooses a matching `SecurityFilterChain` and runs its security filters in order. Filters cooperate to load context, process credentials, check CSRF, translate security exceptions, and authorize access. `FilterChainProxy` also clears the request's thread-associated security context afterward.

This is Spring Core's IoC applied to web infrastructure: the container knows a proxy filter; the Spring context owns the security graph. Security can reject a request before `@Valid`, `@RestControllerAdvice`, or a controller executes. Controllers should not manually parse password headers or implement their own authentication filter logic. [Servlet architecture](https://docs.spring.io/spring-security/reference/7.1/servlet/architecture.html).

## 5. `SecurityFilterChain` and `HttpSecurity`

**⭐⭐⭐⭐⭐ MUST KNOW**

`SecurityFilterChain` is a Spring bean describing security for matching requests. `HttpSecurity` is the builder used to configure authorization, an authentication mechanism, CSRF, sessions, CORS, and exception handling. In Spring Security 7, use lambda-style configuration rather than the removed `WebSecurityConfigurerAdapter` style:

```java
@Configuration
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/books", "/books/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                .requestMatchers("/books", "/books/**", "/transaction-lab/**")
                    .hasRole("ADMIN")
                .anyRequest().denyAll())
            .formLogin(form -> form.loginProcessingUrl("/auth/login").permitAll());
        // Keep CSRF enabled for session/cookie authentication.
        return http.build();
    }
}
```

This is a **policy sketch**, not a copy-paste final configuration. It needs imports for `HttpMethod` and the Spring Security annotations/classes. Actual endpoint matching, entry point, logout, and CSRF-token exposure must be designed together. More specific matchers belong before broader ones; the first matching authorization rule applies. A public registration endpoint must not accept a caller-selected `ADMIN` role. `permitAll()` grants access to that route, not to every method it calls. [Java configuration](https://docs.spring.io/spring-security/reference/7.1/servlet/configuration/java.html), [HTTP authorization](https://docs.spring.io/spring-security/reference/7.1/servlet/authorization/authorize-http-requests.html).
The `denyAll()` fallback closes unlisted **application** routes. Built-in form-login filters can still provide framework endpoints such as a generated `GET /login` page unless you explicitly change that behavior; the exercise sends credentials to `POST /auth/login` instead.

## 6. What happens when the starter is first added

**⭐⭐⭐⭐⭐ MUST KNOW**

Adding Boot's `spring-boot-starter-security` to this currently unsecured application makes Boot provide a default web security chain when no user-defined chain exists. By default it requires authentication for requests and enables form login and HTTP Basic. Boot can provide a generated-password in-memory practice user when no user-defined authentication setup replaces it. Observe the startup log locally; never treat that generated credential as a production account or publish it. Supplying your own `SecurityFilterChain` replaces Boot's default web rules; supplying your own `UserDetailsService` or provider replaces the default user setup. These back-off conditions are related but not identical. [Spring Boot security reference](https://docs.spring.io/spring-boot/reference/web/spring-security.html).

## 7. `Authentication`

**⭐⭐⭐⭐⭐ MUST KNOW**

An `Authentication` represents either an authentication *request* or an authenticated *result*. It exposes a principal, credentials, authorities, and authenticated state. Before password verification, a username/password token can hold submitted credentials and be unauthenticated. After successful verification, it holds a trusted principal and granted authorities such as `ROLE_USER`; password credentials should not be exposed in application responses. For example:

```text
Before: principal="alice", credentials=<submitted password>,
        authorities=[], authenticated=false
After:  principal=UserDetails("alice"), credentials=<normally erased>,
        authorities=[ROLE_USER], authenticated=true
```

The precise token class and principal type depend on the configured authentication mechanism. Do not confuse “a client sent a username” with “the framework trusts this identity.” The provider constructs the trusted result only after verification. [Authentication architecture](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/architecture.html).

## 8. `SecurityContext`

**⭐⭐⭐⭐⭐ MUST KNOW**

`SecurityContext` holds the current `Authentication`. `SecurityContextHolder` makes it available to security infrastructure during request execution, commonly through a thread-local strategy in synchronous servlet code. A caller can inspect `SecurityContextHolder.getContext().getAuthentication()`, but repeated static access scattered across business services obscures dependencies and needs extra care in async work. A controller that merely displays the caller can accept `@AuthenticationPrincipal` (or `Principal`) and return a *safe response DTO*—username and role, never password hash. The context tells the application *who is associated with this execution*; it is not a new query to `app_users` every time it is read. A service-level authorization rule belongs on the service if calls from multiple adapters must be protected. [SecurityContextHolder](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/architecture.html).

## 9. `AuthenticationManager`

**⭐⭐⭐⭐⭐ MUST KNOW**

`AuthenticationManager` defines `authenticate(...)`. The common `ProviderManager` implementation delegates to one or more `AuthenticationProvider`s. The manager orchestrates; it does not inherently query PostgreSQL or compare password hashes itself:

```text
submitted credentials -> AuthenticationManager
                      -> compatible AuthenticationProvider
                      -> authenticated Authentication or failure
```

Conceptually, the form-login filter does the equivalent of:

```java
Authentication attempt =
        UsernamePasswordAuthenticationToken.unauthenticated(username, password);
Authentication result = authenticationManager.authenticate(attempt);
```

The filter then applies the configured success/failure and session behavior; a beginner exercise does **not** need to hand-build a JSON login controller that repeats this work. A manager may try a provider compatible with the token and return the verified result, or reject it with an authentication exception.

[AuthenticationManager and ProviderManager](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/architecture.html).

## 10. `AuthenticationProvider`

**⭐⭐⭐⭐⭐ MUST KNOW**

An `AuthenticationProvider` knows how to verify a supported kind of authentication request. Multiple providers can support different mechanisms. For database-backed username/password authentication, `DaoAuthenticationProvider` obtains a `UserDetails` from `UserDetailsService` and uses `PasswordEncoder` to verify the submitted password. It then returns a trusted authentication result with authorities or fails without revealing whether the username or password was wrong. [DaoAuthenticationProvider](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/dao-authentication-provider.html).

This boundary matters in debugging: if a username does not load, inspect the repository path; if it loads but the password fails, inspect the stored hash and encoder; if login succeeds but `POST /books` fails, inspect authorities and authorization rather than the password lookup. The provider is the bridge from credential lookup to trusted `Authentication`, not an HTTP controller.

## 11. `UserDetails`

**⭐⭐⭐⭐⭐ MUST KNOW**

`UserDetails` is Spring Security's credential-and-authority view: username, encoded password, granted authorities, and account status flags such as enabled, locked, expired, and credentials expired. The application's JPA `AppUser` entity is a persistence model. They are **not the same abstraction**. A `UserDetailsService` may map an `AppUser` to Spring Security's built-in `User`, or an adapter may implement `UserDetails`; the latter is optional. Avoid returning either entity or `UserDetails` as a public API response because they can expose password hashes. [UserDetailsService](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/user-details-service.html).

For a database row `("alice", <BCrypt hash>, "USER")`, the adapter produces `UserDetails(username="alice", password=<same hash>, authorities=[ROLE_USER])`. It does **not** create a new raw password or automatically grant `ADMIN`. Account flags can prevent login even if a password matches; the minimal learning schema may leave advanced account lifecycle flags for later.

## 12. `UserDetailsService`

**⭐⭐⭐⭐⭐ MUST KNOW**

Its `loadUserByUsername(String username)` finds the credential record or throws `UsernameNotFoundException`. A minimal adapter could map `AppUser` into the built-in `UserDetails` view:

```java
return repository.findByUsername(username)
        .map(appUser -> User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(appUser.getRole()) // stored value USER or ADMIN
                .build())
        .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
```

Here `User` is Spring Security's `org.springframework.security.core.userdetails.User`, **not** the JPA entity. The adapter returns the stored *hash*, not a raw password; the provider later calls `matches`. The code assumes the database role does not already include the `ROLE_` prefix. In this project, a typical login lookup is:

```text
username -> UserDetailsService -> AppUserRepository.findByUsername(...)
         -> Spring Data JPA -> Hibernate -> JDBC -> HikariCP -> PostgreSQL
         -> AppUser -> UserDetails -> DaoAuthenticationProvider
```

The Boot-managed `DataSource` and pool remain the same infrastructure already used for books. A later session request can reuse its session authentication without running this lookup again; security policy and session freshness determine what happens. Do not assume that updating a user's role in the database immediately changes an existing session's already-stored authorities. [UserDetailsService](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/user-details-service.html).

## 13. Password security

**⭐⭐⭐⭐⭐ MUST KNOW**

Never store or log plain-text passwords. A password hash is a one-way, salted, deliberately expensive verification value. Modern adaptive password hashing slows offline guessing if a credential database is stolen. For this learning project, BCrypt is a practical choice:

```text
Registration: raw password -> PasswordEncoder.encode(...) -> hash -> app_users.password_hash
Login:        raw submitted password + stored hash
              -> PasswordEncoder.matches(raw, hash) -> accept or reject
```

The password is not “decrypted.” A database backup containing hashes is still sensitive and must be protected. Use TLS for credentials in transit in any non-local deployment. [Password storage](https://docs.spring.io/spring-security/reference/7.1/features/authentication/password-storage.html).

## 14. `PasswordEncoder`

**⭐⭐⭐⭐⭐ MUST KNOW**

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

`encode(raw)` creates a salted hash. The same raw password will normally produce different literal hashes on separate calls; equality of two hash strings is therefore not the login test. Use `matches(raw, storedHash)`, never `raw.equals(storedHash)` or `encode(raw).equals(storedHash)`. Tune BCrypt work factor to the deployment's capacity; it is intentionally costly. The hash's parameters permit verification. [Password storage](https://docs.spring.io/spring-security/reference/7.1/features/authentication/password-storage.html).

## 15. Authorities and roles

**⭐⭐⭐⭐⭐ MUST KNOW**

An authority is a granted string represented by `GrantedAuthority`, such as `BOOK_WRITE`. A role is a coarse-grained authority convention: `hasRole("ADMIN")` checks for `ROLE_ADMIN` by default. `hasAuthority("ROLE_ADMIN")` checks that literal authority; `hasAuthority("BOOK_WRITE")` checks a separate fine-grained permission. If `AppUser.role` stores `ADMIN`, `User.withUsername(...).roles("ADMIN")` creates the `ROLE_ADMIN` granted authority. A request with only `ROLE_USER` fails an admin rule even after a perfectly valid login. Do not pass `"ROLE_ADMIN"` to `hasRole` when using the default role prefix. For this exercise, `USER` and `ADMIN` are enough; a permission database would add complexity without teaching the core flow. [Authorities](https://docs.spring.io/spring-security/reference/7.1/servlet/authorization/architecture.html).

## 16. URL-based authorization

**⭐⭐⭐⭐⭐ MUST KNOW**

`authorizeHttpRequests` protects HTTP paths and methods *before* the controller. Typical choices are `permitAll()` for public reads, `authenticated()` for `/auth/me`, `hasRole("ADMIN")` for mutations, and `denyAll()` for a deliberately closed fallback. Matchers are considered in declaration order, so put specific GET rules before a broad `/books/**` rule. For example, if `requestMatchers("/books/**").hasRole("ADMIN")` appears first, a later public-GET rule may never decide `GET /books/1`. Cover both collection `/books` and item `/books/{id}` paths, and remember `POST /transaction-lab/transfers`. A policy should fail closed on an overlooked route, not quietly permit everything. URL rules cannot validate a Book DTO or enforce the `BookService`'s transactional rollback; those occur later. [Authorize HTTP requests](https://docs.spring.io/spring-security/reference/7.1/servlet/authorization/authorize-http-requests.html).

## 17. Method security

**⭐⭐⭐⭐ IMPORTANT**

`@EnableMethodSecurity` activates annotations such as `@PreAuthorize("hasRole('ADMIN')")`. Place a rule on a public Spring-managed service method when the business operation must remain protected even if invoked outside a particular HTTP route. Method security uses Spring AOP interception: a call through the Spring proxy is checked; a `this.otherMethod()` self-call normally bypasses a separate proxy interception. This mirrors the transaction-proxy lesson. Do not duplicate every URL rule at the method layer; choose the boundary each rule actually protects.

A method-level denial happens while a controller is invoking a service, so MVC exception resolution can interact with it. In this project's existing `GlobalExceptionHandler`, the broad `@ExceptionHandler(Exception.class)` may intercept an `AccessDeniedException` and render an incorrect `500` instead of letting security return `403`. The exercise must explicitly preserve/delegate security exceptions or handle them with the correct status; a filter-chain denial normally never reaches that advice. [Method security](https://docs.spring.io/spring-security/reference/7.1/servlet/authorization/method-security.html).

## 18. `401` versus `403`

**⭐⭐⭐⭐⭐ MUST KNOW**

| Request | Normal policy outcome | Reason |
|---|---:|---|
| Anonymous `GET /auth/me` | `401` with REST entry point | No trusted login. |
| Authenticated `USER` `POST /books` | `403` | Identity exists, but lacks `ADMIN`. |
| Authenticated `ADMIN` `POST /books` with valid CSRF token and valid body | `201` | Authorized; application can create a book. |
| Session user `POST /books` without required CSRF token | Often `403` | CSRF defense rejects the unsafe request, regardless of role. |

A custom REST `AuthenticationEntryPoint` is needed if you want a consistent `401` JSON response rather than a form-login redirect. An authenticated admin can still receive `400`, `404`, `409`, or a transaction-lab error from application behavior *after* authorization.

## 19. `AuthenticationEntryPoint`

**⭐⭐⭐⭐⭐ MUST KNOW**

An `AuthenticationEntryPoint` starts the authentication response when a protected request lacks acceptable authentication—for example, returning a generic `401` Problem Details body for an API. Form-login applications instead commonly redirect to a login page. It receives the request, response, and an `AuthenticationException`; a REST implementation sets the status and content type and writes a generic error body. It does **not** call the controller and should not return a stack trace, submitted password, or user-existence hint. For invalid credentials submitted to a login processing endpoint, an authentication *failure handler* may handle that login response, whereas the entry point handles a request that needs authentication. [Exception translation](https://docs.spring.io/spring-security/reference/7.1/servlet/architecture.html), [form login](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/form.html).

```http
GET /auth/me HTTP/1.1
Host: 127.0.0.1:8080

HTTP/1.1 401 Unauthorized
Content-Type: application/problem+json

{"status":401,"title":"Authentication required"}
```

## 20. `AccessDeniedHandler`

**⭐⭐⭐⭐⭐ MUST KNOW**

An `AccessDeniedHandler` handles access denial where the request has an authenticated identity but lacks authorization, typically writing a `403` response. It receives the request, response, and an `AccessDeniedException`. For `POST /books` by `ROLE_USER`, the URL rule can deny before `BookController.create` runs. It can also be involved in CSRF-related denials, so a `403` does not prove the caller failed a role check. The `AuthenticationEntryPoint` addresses missing authentication; the denied handler addresses forbidden access. Neither should reveal sensitive security internals. [Exception translation](https://docs.spring.io/spring-security/reference/7.1/servlet/architecture.html), [CSRF](https://docs.spring.io/spring-security/reference/7.1/servlet/exploits/csrf.html).

```http
POST /books HTTP/1.1
Cookie: JSESSIONID=<USER-session-id>
X-CSRF-TOKEN: <valid-token>

HTTP/1.1 403 Forbidden
Content-Type: application/problem+json

{"status":403,"title":"Access denied"}
```

The request includes a valid CSRF token so this experiment isolates *role denial*. Without that token, the same status may have a different cause.

## 21. Security errors versus `@RestControllerAdvice`

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
Before DispatcherServlet: security filter rejection
    -> authentication entry point / access-denied or login failure handler

After DispatcherServlet: controller or service exception
    -> MVC exception resolver / GlobalExceptionHandler
```

The existing `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler` and builds `ProblemDetail` for validation and application failures. Its broad `@ExceptionHandler(Exception.class)` is not a universal security-error handler: a request denied in the filter chain has not entered MVC. Conversely, an exception thrown by `@PreAuthorize` during a controller-to-service invocation **can** meet MVC's exception resolver; if the broad catch-all turns `AccessDeniedException` into `500`, the exercise must correct that handling. Align response *shape* across both boundaries, but keep ownership clear.

## 22. Session-based authentication comes first

**⭐⭐⭐⭐⭐ MUST KNOW**

The main exercise uses a server-side HTTP session. A user submits credentials, the authentication provider verifies them, and Spring Security establishes an authenticated `SecurityContext`. The server associates it with an HTTP session. The browser/client receives a session-ID cookie and returns it later:

```text
POST /auth/login (form fields + valid CSRF token in the exercise)
 -> password verification -> SecurityContext saved in session
 <- Set-Cookie: JSESSIONID=<opaque-id>

GET /auth/me with Cookie: JSESSIONID=<opaque-id>
 -> session/context restored -> authorization -> controller
```

The later request does not necessarily execute `UserDetailsService` again. A browser may send the cookie automatically; a CLI must retain and resend it. Because browsers send cookies automatically, CSRF protection matters for unsafe actions. [Form login](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/form.html), [session management](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/session-management.html).

## 23. HTTP session and cookie

**⭐⭐⭐⭐⭐ MUST KNOW**

`JSESSIONID` is an opaque identifier, **not** all session data. The server holds session state; the cookie points to it. Session expiration or invalidation removes the server-side authentication state. Logout should invalidate the session, clear the `SecurityContext`, and handle the session cookie; a stale cookie must not restore access. Protect session IDs as credentials: use HTTPS, secure cookie settings in deployed environments, avoid logging IDs, and account for session fixation protection. [Session management](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/session-management.html), [logout](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/logout.html).

## 24. Stateful versus stateless authentication

**⭐⭐⭐⭐⭐ MUST KNOW**

Session login is stateful: the server maintains authentication state keyed by a session identifier. Bearer-token authentication is commonly stateless with respect to an HTTP session: each request supplies a token that is validated. “Stateless” does not mean the system has no user database, revocation data, keys, or other state. Sessions simplify browser login/logout but require session storage strategy when scaling. Tokens can simplify certain distributed-client flows but introduce issuance, validation, storage, expiration, and revocation design. Choose from requirements, not fashion.

## 25. JWT, after sessions

**⭐⭐⭐ NICE TO KNOW**

A JSON Web Token has a header, payload/claims, and signature in a compact serialized format. A valid signature detects tampering; it does **not** automatically encrypt the payload. Claims such as expiration, issuer, and audience must be checked according to policy. A JWT is one possible bearer credential format, **not Spring Security itself**. The main Book Catalog exercise does not need a home-made JWT issuer. [Spring Security OAuth2 resource server JWT support](https://docs.spring.io/spring-security/reference/7.1/servlet/oauth2/resource-server/jwt.html).

## 26. Conceptual JWT request flow

**⭐⭐⭐ NICE TO KNOW**

```text
Initial authentication -> a trusted issuer creates a signed, expiring token
Later request: Authorization: Bearer <token>
 -> security authentication mechanism verifies signature, expiry, issuer/audience
 -> Authentication with mapped authorities -> SecurityContext
 -> authorization -> controller
```

The backend must reject invalid or expired tokens and deliberately map trusted claims to authorities. A signature does not make client-side token storage safe by itself. Do not place passwords or secrets in claims. Token issuance, key rotation, revocation, and resource-server configuration deserve their own exercise after sessions. [JWT resource server](https://docs.spring.io/spring-security/reference/7.1/servlet/oauth2/resource-server/jwt.html).

## 27. Session versus JWT

**⭐⭐⭐⭐ IMPORTANT**

| Concern | Server-side session | Signed JWT bearer token |
|---|---|---|
| Authentication state | Stored server-side, referenced by session ID | Carried in token claims, verified each request |
| Logout/revocation | Invalidate session centrally | Often needs expiry, revocation strategy, or short lifetime |
| Browser integration | Cookies work naturally | Client must store/send bearer token; storage choice matters |
| CSRF | Cookie auto-sending makes CSRF relevant | Bearer header not auto-sent by browsers; a JWT *in a cookie* can still have CSRF risk |
| XSS/storage | Script access to cookie can be restricted; XSS still dangerous | Exposed script-readable token storage can be stolen by XSS |
| Scaling | Shared/sticky session strategy may be required | Signature verification can be local; key distribution and revocation remain |
| Complexity | Session lifecycle and CSRF | Issuance, validation, keys, expiry, revocation, client storage |
| Typical fit | Browser-centric app with direct login/logout | Distributed API clients or an external identity platform |

Neither is automatically safer or “more RESTful.” The threat model and client behavior determine the design.

## 28. CSRF

**⭐⭐⭐⭐⭐ MUST KNOW**

Cross-site request forgery occurs when another site causes a victim's browser to send a request using credentials the browser attaches automatically, such as a session cookie. An attacker cannot normally read the target site's CSRF token, so the server requires an unpredictable token *in addition to* the automatically sent cookie on unsafe operations. Spring Security enables CSRF protection by default for unsafe methods; safe methods such as GET should not change state.

```http
POST /books HTTP/1.1
Cookie: JSESSIONID=<opaque-id>
X-CSRF-TOKEN: <token-from-this-session>
Content-Type: application/json

{"isbn":"...","title":"...","author":"...","price":12.50,"stock":2}
```

The default servlet CSRF repository is session-based. A missing/invalid token may produce `403` **before** authentication or role authorization, so a diagnostic “anonymous POST should be 401” test must either send a valid CSRF token or intentionally account for CSRF taking precedence. On successful authentication or logout, the old token is cleared; fetch a fresh token before the next unsafe action. Do not copy a token from one cookie jar to another. Disabling CSRF solely because an endpoint uses JSON is unsafe when a browser still sends a session cookie. [CSRF protection](https://docs.spring.io/spring-security/reference/7.1/servlet/exploits/csrf.html).

## 29. CORS

**⭐⭐⭐⭐ IMPORTANT**

Cross-origin resource sharing is a browser access-control protocol. A browser script from one origin may need permission to call an API on another origin. A preflight `OPTIONS` request asks whether the origin, method, and headers are allowed; it can arrive without a session cookie and needs to be processed before security rejects it as anonymous. Spring Security can integrate with a deliberate MVC/CORS configuration via `http.cors(Customizer.withDefaults())`. The current Book Catalog binds to `127.0.0.1:8080` and needs no CORS configuration for Postman, `curl`, or same-origin access. Add specific origins only for an actual cross-origin browser client; credentials require careful origin selection. CORS does not identify users, assign roles, or replace CSRF protection. [CORS integration](https://docs.spring.io/spring-security/reference/7.1/servlet/integrations/cors.html).

For a real cross-origin browser client, decide the exact allowed **origin**, HTTP **methods**, and request **headers**, and whether cookies/credentials may be sent. Credentialed requests require an explicit trusted origin and corresponding browser/server credential settings; do not use a blanket wildcard as a substitute for this decision. A successful preflight merely permits browser JavaScript to make the later request—it grants no application role.

## 30. CORS versus CSRF

**⭐⭐⭐⭐⭐ MUST KNOW**

| Question | CORS | CSRF |
|---|---|---|
| Core concern | May JavaScript from origin A read/call responses from origin B? | Can another site induce an authenticated unsafe request? |
| Enforced by | Browser plus server CORS policy | Server-side CSRF verification plus browser credential behavior |
| Typical signal | `Origin`, preflight, allow-origin/method/header response headers | Session/cookie plus CSRF token in header/form field |
| Does it authenticate? | No | No |

An attacker can submit certain cross-origin forms even when CORS blocks reading the response, so “we did not enable CORS” is not a CSRF defense.

## 31. Login flow, end to end

**⭐⭐⭐⭐⭐ MUST KNOW**

Default Spring Security form login accepts form-encoded `username` and `password` at `POST /login`, subject to CSRF protection. The exercise keeps that built-in filter but changes its processing URL to `POST /auth/login`; it still expects **form parameters, not JSON**. A JSON login controller is not created automatically and would need separate authentication/session-persistence design. Using the standard filter makes the mechanics visible without inventing a credential parser.

```text
Client            Security filter      Manager/Provider       Data layer
  | POST /auth/login   |                      |                      |
  | form + CSRF ------>| check CSRF           |                      |
  |                    | authenticate(...) -->|                      |
  |                    |                      | loadUserByUsername -->| JPA/Hibernate/DB
  |                    |                      |<-- hash + authorities|
  |                    |                      | matches(raw, hash)   |
  |                    |<-- Authentication ---|                      |
  |                    | SecurityContext saved in HTTP session        |
  |<-- response + JSESSIONID cookie           |                      |
```

Failure should be generic: do not disclose whether the username or password was wrong. Default form login can redirect on success/failure; an API-oriented success/failure handler may return status/JSON instead, but that changes response presentation, not credential verification. A session already authenticated as one user should not be silently elevated because a request body names a different user. [Form login](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/form.html).

## 32. Registration is an application use case

**⭐⭐⭐⭐⭐ MUST KNOW**

Spring Security authenticates users; it does not automatically decide whether this Book Catalog allows public signup. A small registration use case is:

```text
POST /auth/register -> validate DTO -> normalize/check username
 -> encode password -> create AppUser with server-chosen USER role
 -> repository.save(...) -> PostgreSQL UNIQUE constraint
```

The DTO can require a nonblank username with a bounded length and a password meeting the selected practice policy. Bean Validation checks request shape; it does not authorize callers. A database `UNIQUE` constraint remains authoritative when two requests race to claim the same username; convert that conflict into a safe `409` response without echoing the password. Use a transaction for the write. Never accept a registration `role` field that lets an anonymous caller choose `ADMIN`. An admin-creation mechanism is a separate trusted operation, not a property of public signup. [Spring Security password storage](https://docs.spring.io/spring-security/reference/7.1/features/authentication/password-storage.html).

## 33. A small users-and-roles model

**⭐⭐⭐⭐ IMPORTANT**

For the beginner exercise, one table is sufficient:

```sql
CREATE TABLE app_users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);
```

An `AppUser` JPA entity maps this table; `AppUserRepository` can expose `Optional<AppUser> findByUsername(String username)`. The table holds *application* accounts. The PostgreSQL connection account in `compose.yaml`/`application.yaml` is a **database principal** used by the app's pool. Its privileges and password are different from an `AppUser`'s login role and BCrypt hash. A user-to-many-roles mapping is possible later, but a join table is not required to learn authentication.

## 34. Security schema changes belong to Flyway

**⭐⭐⭐⭐⭐ MUST KNOW**

`Flyway/book-catalog-api/src/main/resources/db/migration` already has `V1` through `V5`. The security exercise should add a new `V6__create_app_users.sql`; it must not rewrite already-applied files merely to make a new table appear. At startup, Flyway applies pending SQL and records it in `flyway_schema_history`; Hibernate's `ddl-auto: validate` then checks the entity mapping. Neither JPA nor Spring Security replaces Flyway's schema ownership. Verify the connected database/schema before inspecting rows or migration history, and never assume the Compose named volume is empty. [Spring Boot Flyway database initialization](https://docs.spring.io/spring-boot/reference/how-to/data-initialization.html).

## 35. Security and transactions solve different problems

**⭐⭐⭐⭐⭐ MUST KNOW**

```java
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public Book setStock(long id, Integer stock) { /* load and mutate a managed Book */ }
```

The security interceptor asks whether the caller may execute the use case; the transaction interceptor defines atomic database work. These are separate concerns and proxies/interceptors. Denial should prevent the intended mutation, while rollback semantics still govern failures *after* an authorized call begins. Do not assume annotation order in source code alone precisely describes advisor nesting. In the current app, `BookService` has a read-only class-level transaction and write-method overrides; the delete route is an intentional rollback lab. [Method security](https://docs.spring.io/spring-security/reference/7.1/servlet/authorization/method-security.html), [Spring declarative transactions](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative.html).

## 36. Security still uses the existing connection pool

**⭐⭐⭐⭐ IMPORTANT**

A login that loads an `AppUser` performs an ordinary repository query. Hibernate sends SQL through pgJDBC using a connection borrowed from the Boot-managed HikariCP `DataSource`. The pool's lifecycle does not change because the query is “for security.” BCrypt verification is CPU work on the submitted password and retrieved hash, not a database-side decryption operation. Avoid logging JDBC credentials or user hashes while debugging.

## 37. JPA/Hibernate boundary for credentials and authorities

**⭐⭐⭐⭐ IMPORTANT**

Keep `AppUser` persistence separate from its `UserDetails` adaptation. If roles are stored in a lazy collection, accessing it after the persistence context closes may fail or cause extra queries. The single-role column avoids that issue in this exercise. If a richer role model is added later, load the authorities deliberately within a transaction or query projection rather than turning `open-in-view` back on; this project intentionally has `spring.jpa.open-in-view: false`. Never serialize a credential entity directly. [UserDetailsService](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/user-details-service.html).

## 38. Current user in a controller

**⭐⭐⭐⭐ IMPORTANT**

Only ask for the current user when the endpoint needs it. A read-only identity endpoint can be simple:

```java
@GetMapping("/auth/me")
CurrentUserResponse me(@AuthenticationPrincipal UserDetails user) {
    return new CurrentUserResponse(user.getUsername(),
            user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
}
```

This example assumes the principal is a `UserDetails`; that is true if the exercise's adapter returns one, but not universal for every authentication mechanism. A `Principal` or `Authentication` method argument can be used when appropriate. The response DTO must omit credentials and password hash. The authenticated identity should come from the security context, never a client-submitted `username` field that is trusted without checking. [Spring MVC authentication principal integration](https://docs.spring.io/spring-security/reference/7.1/servlet/integrations/mvc.html).

## 39. Logout

**⭐⭐⭐⭐⭐ MUST KNOW**

For session login, logout must clear authentication and invalidate the session. Spring Security's logout support handles security context cleanup and session invalidation; configure its response to match the REST exercise (for example, a success status instead of an HTML redirect). With CSRF enabled, the default `POST /logout` needs a valid token; the exercise changes this processing URL to `POST /auth/logout`. After logout, reuse the old cookie against `/auth/me` and expect it not to authenticate. Obtain a new CSRF token before another unsafe action, because logout clears the old one. A JWT cannot be invalidated by clearing a server session unless the system has chosen an additional revocation mechanism; client-side deletion alone does not guarantee a copied token is unusable. [Logout](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/logout.html), [CSRF](https://docs.spring.io/spring-security/reference/7.1/servlet/exploits/csrf.html).

## 40. Remember-me

**⭐⭐⭐ NICE TO KNOW**

Remember-me can reauthenticate a user across browser restarts using a long-lived cookie and an associated server-side or signed-token strategy. It is not just “make `JSESSIONID` live forever.” The longer lifetime increases theft/revocation concerns. It is unnecessary for the first Book Catalog security exercise; understand ordinary session expiration first. [Remember-me](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/rememberme.html).

## 41. Anonymous authentication

**⭐⭐⭐⭐ IMPORTANT**

Spring Security may place an `AnonymousAuthenticationToken` in the context so downstream code has a consistent representation for a request without login. This is **not** a verified application user. A check such as `authenticated()` is designed to distinguish real authenticated users from anonymous access. Do not authorize a mutation merely because `SecurityContextHolder.getContext().getAuthentication()` is non-null. [Anonymous authentication](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/anonymous.html).

## 42. Security-context lifetime

**⭐⭐⭐⭐ IMPORTANT**

For a synchronous servlet request, security filters load/create a context, make it available during execution, and clear its thread association afterward. With sessions, an authenticated context may be saved between requests and restored for the next request carrying the session ID. A `SecurityContext` is **not** a transaction context: authentication may be available while no database transaction exists; an authorized service may start and finish a transaction while the request remains authenticated. The contexts are both often associated with execution/thread boundaries, but their ownership and persistence rules differ. [SecurityContext](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/persistence.html).

## 43. Threading and async context propagation

**⭐⭐⭐ NICE TO KNOW / ⭐⭐ FUTURE KNOWLEDGE for custom async designs**

Manually created threads do not safely inherit the request's identity by magic. Thread-local context may be absent—or, if propagated incorrectly, leak between tasks in a pool. Spring Security provides integrations and delegating wrappers for selected async uses. For this exercise, keep the request path synchronous; when async work is introduced, explicitly decide which identity (if any) should run the task and use framework-supported propagation. [Concurrency support](https://docs.spring.io/spring-security/reference/7.1/features/integrations/concurrency.html).

## 44. Password change

**⭐⭐⭐⭐ IMPORTANT**

An authenticated change-password operation should establish the current user's identity from the security context, verify the supplied current password with `matches`, validate the new password, encode it once, and update the stored hash transactionally. Do not allow a request field to choose another account. Decide whether existing sessions should remain valid after a password change; rotating/invalidation policy is an explicit security decision. Never log raw old/new passwords or return the stored hash. Password reset by email introduces identity-proofing and token design and belongs to a later topic.

## 45. Security logging

**⭐⭐⭐⭐ IMPORTANT**

Log enough to diagnose outcomes—request route/method, coarse failure type, and a safe account identifier if policy permits—but never raw passwords, password hashes, session IDs, CSRF tokens, bearer tokens, or full `Authorization`/`Cookie` headers. Avoid logging a whole `Authentication`, `AppUser`, or request DTO without inspecting what `toString()` includes. Centralized logs and traces are secondary stores of sensitive data; development DEBUG/TRACE settings should not be casually shipped to production. Failed-login responses should remain generic even if internal logs identify a useful operational category.

## 46. Common mistakes and threats

**⭐⭐⭐⭐⭐ MUST KNOW**

| Mistake | Why it fails | Better boundary |
|---|---|---|
| Plain-text/reversible passwords | Database compromise exposes usable secrets | Adaptive salted hash; `matches` |
| Comparing a raw password or a newly generated hash with the stored hash using `equals` | BCrypt salts make literal hash equality the wrong test | Use `PasswordEncoder.matches(raw, storedHash)` |
| Returning a JPA user entity or `UserDetails` from an API | Password hashes or account flags may be serialized | Return a safe DTO without credentials |
| Copying an outdated `WebSecurityConfigurerAdapter` tutorial | Removed configuration style does not match Security 7 | Use `SecurityFilterChain` beans and current APIs |
| Trusting a registration `role` field | Anonymous caller can become `ADMIN` | Server assigns safe default role |
| `permitAll()` on every route | Authentication exists but authorizes nothing | Explicit ordered policy, closed fallback |
| Protecting only controllers | Another adapter/service call can bypass HTTP rule | Method security for genuinely shared use cases |
| Expecting advice to catch all security errors | Filters may reject before MVC | Security entry point/denied handler |
| Disabling CSRF because requests use JSON | Cookie-authenticated browser requests can still be forged | Keep token defense for session authentication |
| Treating CORS as access control | Non-browser clients are not stopped by browser CORS policy | Server-side authentication/authorization |
| Putting JWT in a cookie and calling it CSRF-free | Browser auto-sends cookie | Analyze actual credential transport |
| Logging credential/session material | Logs become an attack surface | Redact and log outcomes only |
| `hasRole("ROLE_ADMIN")` with default prefix | Doubles the role prefix | `hasRole("ADMIN")` or literal `hasAuthority("ROLE_ADMIN")` |
| Relying on a role check before CSRF | CSRF filter may reject first | Supply valid token to isolate role behavior |
| Mixing app users with database users | Separate identity domains have different privileges | `app_users` table vs PostgreSQL connection account |
| Hiding a button or endpoint in the frontend | A client can call HTTP routes directly | Enforce policy on the backend, including transaction-lab routes |
| Hardcoding passwords in source or YAML | Secrets leak through Git and deployments | Use trusted local provisioning/secret management |
| Accepting arbitrary “already encoded” strings as passwords | A hash-looking value is not proof of safe encoding or ownership | Encode only validated raw registration input server-side |
| Making every practice account `ADMIN` | Role checks stop demonstrating least privilege | Register `USER`; provision `ADMIN` separately |
| Trusting a JWT payload without signature, expiry, issuer, or audience checks | Claims are client-controlled until verified | Use a vetted verification mechanism |
| Assuming signed JWT means encrypted or easily revoked | Claims can be read; a copied long-lived token may remain valid | Keep claims nonsecret; design expiry and revocation |
| Omitting a database `UNIQUE` username constraint | Concurrent registration can create duplicate identities | Enforce uniqueness in PostgreSQL and handle conflicts |
| Treating security as validation or transaction management | Identity checks do not validate a DTO or make writes atomic | Keep validation, security, and `@Transactional` at their own boundaries |
| Treating every `401` or `403` as the same failure | Missing login, missing role, and missing CSRF need different fixes | Diagnose request stage and credentials/token state |
| Keeping bearer tokens in script-readable storage without a threat model | XSS can steal a reusable credential | Choose storage and lifetimes deliberately; prevent XSS |

## 47. Debugging security, layer by layer

**⭐⭐⭐⭐⭐ MUST KNOW**

1. Record method, path, headers/cookies *names* (not secrets), expected role, and response status. Confirm which host/port and database the application uses.
2. Ask whether the request reached the security chain. A redirect to `/login`, `401`, or `403` before a controller breakpoint indicates a pre-MVC decision.
3. If login fails, verify request encoding and field names (`username`, `password` for default form login), the CSRF token, user lookup, stored hash format, `PasswordEncoder.matches`, account status, and authority mapping. Never print the password.
4. If a later request is anonymous, check that the client retained and resent `JSESSIONID`, that the session was not expired/invalidated, and that the login response actually saved a context.
5. If a role check fails, inspect the *granted authorities*, matcher order, route/method, and whether the role is represented as `ROLE_ADMIN`.
6. If an unsafe request gets `403`, first separate missing/invalid CSRF token from missing authority. Refresh token after login/logout.
7. If an authorized request reaches the controller and then fails, return to validation, service transactions, JPA, Flyway, and PostgreSQL diagnosis. In this project, DELETE's intentional checked-exception rollback is an application result, not a security denial.
8. If a browser reports CORS failure, inspect the browser preflight, `Origin`, requested method/headers, response CORS headers, and whether credentialed requests are deliberately allowed. `curl` cannot reproduce the browser's same-origin enforcement.
9. At a breakpoint in a safe development environment, inspect the current `Authentication` type, `isAuthenticated`, principal, and granted authorities. Remember that an anonymous token can be non-null; never display credentials or the complete security context in shared logs.

For local diagnosis, `logging.level.org.springframework.security: DEBUG` reveals filter-chain and authorization decisions; `logging.level.org.hibernate.SQL: DEBUG` shows user lookup SQL, not password comparison. Use targeted logger categories temporarily, and never enable verbose credential/header logging in a shared environment. [Servlet architecture](https://docs.spring.io/spring-security/reference/7.1/servlet/architecture.html).

## 48. Manual experiments with Postman or `curl`

**⭐⭐⭐⭐ IMPORTANT**

Use disposable local accounts and redact all values before sharing output. A cookie jar keeps the same session across `curl` calls; do not commit it. For session login, first obtain a CSRF token for that jar, then submit form fields plus the token, then refresh the token because login clears the old one. The exact token-fetch URL/header is defined by the exercise's configuration, not a built-in universal `/csrf` route.

```text
1. GET /books                            -> public read (if selected policy)
2. GET /auth/me                          -> 401 for anonymous API client
3. GET /auth/csrf                        -> obtain token in current cookie jar
4. POST /auth/login (exercise form username/password + token)
5. GET /auth/me with returned JSESSIONID -> authenticated identity
6. GET /auth/csrf again                  -> fresh post-login token
7. POST /books as USER + valid token     -> 403 role denial
8. POST /books as ADMIN + valid token    -> 201 if body valid
9. POST /auth/logout + valid token       -> session invalidated
10. GET /auth/me with old cookie         -> 401
```

To test an *anonymous* route's authorization response rather than its CSRF response, provide a valid token even for an anonymous cookie jar. To prove the CSRF defense, deliberately omit the token from an unsafe request and expect rejection. Postman may not behave like a browser's automatic cookie/CORS behavior, so reason about the browser threat model separately. Check `flyway_schema_history` and the `app_users` table without selecting or displaying password hashes unnecessarily.

## 49. Security testing preview

**⭐⭐⭐ NICE TO KNOW / ⭐⭐ FUTURE KNOWLEDGE for a full testing module**

`spring-security-test` integrates with MockMvc; helpers include `@WithMockUser`, `formLogin()`, and `csrf()` request support. A mocked user can test authorization without loading a real `AppUser`, but it does not prove the JPA user lookup, BCrypt verification, or session persistence path. A later testing topic should cover both focused MVC authorization tests and database-backed integration tests. Keep this guide's main exercise manual, as requested. [Spring Security testing](https://docs.spring.io/spring-security/reference/7.1/servlet/test/index.html).

## 50. Interview retrieval practice

**⭐⭐⭐⭐ IMPORTANT**

Answer these aloud without reading the guide; for each answer, use one Book Catalog example.

1. What problem does authentication solve, and what does authorization solve?
2. Why does HTTP `401` usually mean “not authenticated” despite its name?
3. Give a case where `403` is caused by CSRF rather than a missing role.
4. What is a `SecurityFilterChain`, and who invokes it?
5. Why can a request fail before any controller method runs?
6. What is in an `Authentication` before and after password verification?
7. What does `SecurityContext` hold, and how long does it last?
8. What does `AuthenticationManager` delegate to?
9. What does `DaoAuthenticationProvider` need to check a password?
10. How are `AppUser`, `UserDetails`, and `UserDetailsService` different?
11. Why use `PasswordEncoder.matches`, not string equality?
12. Why can two BCrypt hashes of the same password differ?
13. What is the difference between `ROLE_ADMIN` and `BOOK_WRITE`?
14. How do `hasRole` and `hasAuthority` differ?
15. Why does matcher order matter in `authorizeHttpRequests`?
16. When is `@PreAuthorize` valuable beyond HTTP route rules?
17. What does an `AuthenticationEntryPoint` do versus an `AccessDeniedHandler`?
18. Why may `@RestControllerAdvice` not handle a filter-chain denial?
19. What is stored in `JSESSIONID`, and what is stored server-side?
20. Does every session request query `app_users`? Why or why not?
21. What is CSRF, and why do cookie-based sessions matter?
22. Why does disabling CORS not prevent CSRF?
23. Why does “JWT is signed” not mean its payload is secret?
24. How does logout differ between server sessions and bearer JWTs?
25. Why do both `@PreAuthorize` and `@Transactional` sometimes appear on one method?
26. Which connection pool and database does a user lookup use here?
27. Why must the `app_users` table be introduced in a new Flyway migration?
28. How is an application `ADMIN` different from the PostgreSQL connection account?

## 51. Final review

**⭐⭐⭐⭐⭐ MUST REMEMBER**

### Complete mental model and request flow

```text
HTTP request
 -> servlet filter proxy -> chosen Spring Security filter chain
 -> restore any saved SecurityContext
 -> CSRF check for unsafe requests (before the login filter and URL authorization)
 -> authenticate submitted credentials, if this request is a login attempt
 -> URL authorization using the resulting identity/authorities
 -> DispatcherServlet -> controller -> service proxy
 -> transaction (if configured) -> repository -> Hibernate/JDBC/HikariCP/PostgreSQL
 -> response; clear request-thread SecurityContext association
```

The shorter memory phrase is: **verify identity, decide permission, then run business work**. A public route skips identity requirements; a later session request normally restores identity without rechecking the password. The login path adds `AuthenticationManager -> AuthenticationProvider -> UserDetailsService -> JPA -> PostgreSQL -> PasswordEncoder.matches`. Security, persistence, transactions, and migrations each own a different boundary.

### Login sequence to recall

```text
credentials + CSRF token
 -> login filter -> AuthenticationManager -> DaoAuthenticationProvider
 -> UserDetailsService -> AppUserRepository -> PostgreSQL
 -> PasswordEncoder.matches -> authenticated Authentication
 -> SecurityContext -> HTTP session -> JSESSIONID cookie
 -> later request restores context -> authorization
```

### Comparison sheet

| Pair | First | Second |
|---|---|---|
| Authentication / authorization | Establish trusted caller identity | Decide if caller may perform operation |
| `401` / `403` | Missing/invalid authentication in normal API flow | Authenticated but forbidden, or another denial such as CSRF |
| Role / authority | Coarse convention such as `ROLE_ADMIN` | Any granted permission string, e.g. `BOOK_WRITE` |
| Session / JWT | Server-stored context keyed by cookie ID | Signed bearer credential checked per request |
| CSRF / CORS | Defend against browser-induced unsafe requests using ambient credentials | Control browser cross-origin script access |
| Validation / authorization | Is input structurally/business-rule valid? | May this caller execute this action? |
| SecurityContext / transaction | Current authentication for execution | Atomic database work boundary |
| App user / DB user | Account that calls the Book API | PostgreSQL account used by the application's pool |

#### `401` versus `403` quick test

| Situation | Expected response | First thing to inspect |
|---|---:|---|
| Anonymous `GET /auth/me` | `401` | Is a valid session cookie present? |
| Authenticated `USER` writes a Book with valid CSRF | `403` | Does the caller have `ROLE_ADMIN`? |
| Any unsafe request missing required CSRF | `403` | Was a token from the same session sent? |

#### Session versus JWT quick choice

| Decision | Session | JWT bearer token |
|---|---|---|
| Identity between requests | Server saves a context; client sends session ID | Client sends token; server validates it each request |
| Logout | Invalidate server-side session | Requires expiry/revocation design beyond client deletion |
| Browser risks | Auto-sent cookie makes CSRF important | Header token avoids ambient-cookie CSRF, but token storage/XSS matter |
| Scaling | Share/stick sessions if needed | Distribute verification keys and handle revocation if needed |

#### CSRF versus CORS quick test

| Question | CSRF | CORS |
|---|---|---|
| What is defended? | Forged unsafe request using ambient browser credentials | Cross-origin JavaScript access under browser policy |
| Main evidence | Cookie, CSRF token, unsafe method | `Origin`, preflight, allow-origin/method/header/credentials |
| Does it authenticate or authorize? | No | No |

### Must-remember rules

1. Never store or log raw passwords; encode at registration and use `matches` at login.
2. Never trust a public registration request to assign `ADMIN`.
3. Define explicit ordered HTTP policy and use method security where a service boundary needs it.
4. Keep CSRF protection for session/cookie authentication; obtain a fresh token after login/logout.
5. Do not mistake CORS, validation, or a JWT format for authorization.
6. Give filter-chain failures security-owned responses; keep application exception handling separate.
7. Do not modify applied Flyway migrations; add the next version for security schema.
8. Preserve the existing Book Catalog business behavior, including the transaction lab's intentional failures.

### Checklist for securing a new REST endpoint

```text
[ ] Name the data and action (read or mutation).
[ ] Decide anonymous, authenticated, role, or specific authority policy.
[ ] Add a specific HTTP matcher before the fallback; check method and path.
[ ] Decide whether the service also needs method-level protection.
[ ] For unsafe session requests, define how clients obtain/send/refresh CSRF tokens.
[ ] Validate input separately from authorization.
[ ] Return safe 401/403/application errors at their proper boundaries.
[ ] Test anonymous, USER, ADMIN, missing-CSRF, invalid-body, and logout paths.
[ ] Verify database constraints, transaction behavior, and migration state where relevant.
```

### Official references for later lookup

- [Spring Security 7.1 servlet architecture](https://docs.spring.io/spring-security/reference/7.1/servlet/architecture.html)
- [Spring Security 7.1 authentication architecture](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/architecture.html)
- [Spring Security 7.1 HTTP authorization](https://docs.spring.io/spring-security/reference/7.1/servlet/authorization/authorize-http-requests.html)
- [Spring Security 7.1 form login and password storage](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/form.html)
- [Spring Security 7.1 CSRF and CORS](https://docs.spring.io/spring-security/reference/7.1/servlet/exploits/csrf.html)
- [Spring Boot 4 security integration](https://docs.spring.io/spring-boot/reference/web/spring-security.html)
