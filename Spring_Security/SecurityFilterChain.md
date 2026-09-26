# SecurityFilterChain in Spring Security

## 1. Core idea

`SecurityFilterChain` is one of the most important concepts in Spring Security.

A simple definition is:

> **`SecurityFilterChain` is the security pipeline that an HTTP request must pass through before it is allowed to reach your controller.**

It is not one single filter. It represents an ordered group of Spring Security filters that cooperate to process security concerns for matching HTTP requests.

---

## 2. Where does `SecurityFilterChain` sit?

Without focusing on security, a normal Spring MVC request looks like this:

```text
Client
  ↓
Tomcat
  ↓
DispatcherServlet
  ↓
Controller
  ↓
Service
  ↓
Repository
```

Spring Security inserts itself before Spring MVC:

```text
Client
  ↓
Tomcat
  ↓
DelegatingFilterProxy
  ↓
FilterChainProxy
  ↓
SecurityFilterChain
  ↓
DispatcherServlet
  ↓
Controller
  ↓
Service
```

This means Spring Security can reject a request before the controller ever executes.

For example:

```text
DELETE /books/5
      ↓
SecurityFilterChain
      ↓
User is not authorized
      ↓
403 Forbidden
      ↓
Controller is never called
```

---

## 3. What exactly is a `SecurityFilterChain`?

Think of it as a collection of security filters:

```text
SecurityFilterChain
│
├── restore/load SecurityContext
├── CSRF processing
├── authentication processing
├── logout processing
├── exception translation
├── authorization
└── other security filters
```

Different filters have different responsibilities.

The chain runs them in a specific order.

---

## 4. `DelegatingFilterProxy` vs `FilterChainProxy` vs `SecurityFilterChain`

Do not confuse these three:

```text
DelegatingFilterProxy
        ↓
FilterChainProxy
        ↓
SecurityFilterChain
```

| Component | Purpose |
|---|---|
| `DelegatingFilterProxy` | Bridge from the Servlet container to Spring-managed beans |
| `FilterChainProxy` | Main Spring Security filter coordinator |
| `SecurityFilterChain` | The actual configured security filter pipeline |

Conceptually:

```text
Tomcat
  ↓
DelegatingFilterProxy
  ↓
"Spring, handle security"
  ↓
FilterChainProxy
  ↓
"Which SecurityFilterChain matches this request?"
  ↓
Selected SecurityFilterChain
```

---

## 5. `HttpSecurity` vs `SecurityFilterChain`

You usually configure Spring Security like this:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http.authorizeHttpRequests(auth -> auth
        // authorization rules
    );

    return http.build();
}
```

The relationship is:

```text
HttpSecurity
= builder/configuration object

        ↓ configure

http.build()

        ↓

SecurityFilterChain
= final configured security chain
```

A useful analogy:

```text
HttpSecurity
= blueprint / construction tool

SecurityFilterChain
= finished security system
```

So `HttpSecurity` is used to define the behavior, while `SecurityFilterChain` is what Spring Security later uses when requests arrive.

---

## 6. Why is `SecurityFilterChain` declared as a Spring bean?

Example:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
    ...
    return http.build();
}
```

`@Bean` means Spring manages the resulting `SecurityFilterChain`.

This connects Spring Security with Spring Core IoC:

```text
Spring Core
   ↓
creates/manages SecurityFilterChain
   ↓
Spring Security uses it for HTTP requests
```

You do not manually call the security chain from your controller.

---

## 7. Example configuration for the Book Catalog

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http.authorizeHttpRequests(auth -> auth

        .requestMatchers(
            HttpMethod.GET,
            "/books",
            "/books/**"
        ).permitAll()

        .requestMatchers(
            HttpMethod.GET,
            "/auth/me"
        ).authenticated()

        .requestMatchers(
            "/books",
            "/books/**",
            "/transaction-lab/**"
        ).hasRole("ADMIN")

        .anyRequest().denyAll()
    );

    return http.build();
}
```

Read it like English:

```text
GET /books/**
→ everyone may access

GET /auth/me
→ must be authenticated

Other /books/** requests
→ must have ADMIN role

/transaction-lab/**
→ must have ADMIN role

Anything else
→ denied
```

---

## 8. `permitAll()`

Example:

```java
.requestMatchers(
    HttpMethod.GET,
    "/books/**"
).permitAll()
```

This means:

```text
GET /books/123
      ↓
Rule matches
      ↓
permitAll()
      ↓
Authentication is not required for authorization
      ↓
Request may continue
```

Important:

```text
permitAll()
≠ skip the entire application/security pipeline
```

It does not mean:

```text
skip validation
skip controller
skip database constraints
skip business rules
```

It means the authorization rule does not require the caller to be authenticated.

---

## 9. `authenticated()`

Example:

```java
.requestMatchers(
    HttpMethod.GET,
    "/auth/me"
).authenticated()
```

Spring checks whether the current `SecurityContext` contains an authenticated `Authentication`.

Example:

```text
SecurityContext
└── Authentication
    ├── principal = "binh"
    ├── authorities = [ROLE_USER]
    └── authenticated = true
```

Then:

```text
GET /auth/me
      ↓
authenticated()?
      ↓
YES
      ↓
Controller
```

If there is no valid authentication:

```text
GET /auth/me
      ↓
No authenticated user
      ↓
401 Unauthorized
```

---

## 10. `hasRole("ADMIN")`

Example:

```java
.requestMatchers("/books/**")
.hasRole("ADMIN")
```

Spring checks the authorities of the current `Authentication`.

Conceptually:

```text
SecurityContext
    ↓
Authentication
    ↓
Authorities
    ↓
Does it contain ROLE_ADMIN?
```

Example with a normal user:

```text
Authentication
└── authorities = [ROLE_USER]

Required = ROLE_ADMIN

→ access denied
→ 403 Forbidden
```

Example with an admin:

```text
Authentication
└── authorities = [ROLE_ADMIN]

Required = ROLE_ADMIN

→ access allowed
```

With Spring Security's default role prefix convention:

```java
hasRole("ADMIN")
```

checks for:

```text
ROLE_ADMIN
```

So normally do not write:

```java
hasRole("ROLE_ADMIN")
```

---

## 11. `denyAll()`

Example:

```java
.anyRequest().denyAll()
```

This means any request that did not match an earlier rule is rejected.

Conceptually:

```text
Request
   ↓
No earlier authorization rule matched
   ↓
denyAll()
   ↓
blocked
```

This follows a **fail-closed** approach.

For example, suppose you later add:

```text
POST /dangerous-admin-operation
```

but forget to configure it.

With:

```java
.anyRequest().permitAll()
```

you might accidentally expose it.

With:

```java
.anyRequest().denyAll()
```

the forgotten endpoint stays closed.

---

## 12. Matcher order matters

Authorization matchers are evaluated in declaration order.

The first matching rule decides the authorization policy.

Bad ordering:

```java
.requestMatchers("/books/**")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.GET, "/books/**")
.permitAll()
```

Request:

```text
GET /books/1
```

The first rule already matches:

```text
/books/**
```

so the request requires ADMIN.

The later public GET rule does not decide it.

Better ordering:

```java
.requestMatchers(
    HttpMethod.GET,
    "/books",
    "/books/**"
).permitAll()

.requestMatchers(
    "/books",
    "/books/**"
).hasRole("ADMIN")
```

Mental model:

```text
Authorization rules

TOP
 ↓
check first rule
 ↓
check second rule
 ↓
check third rule
 ↓
FIRST matching rule wins
```

---

## 13. Example: anonymous public request

Request:

```http
GET /books/5
```

Flow:

```text
Client
  ↓
FilterChainProxy
  ↓
SecurityFilterChain
  ↓
Authorization rules
  ↓
GET /books/** matches
  ↓
permitAll()
  ↓
DispatcherServlet
  ↓
BookController
```

---

## 14. Example: anonymous protected request

Request:

```http
DELETE /books/5
```

Flow:

```text
DELETE /books/5
      ↓
SecurityFilterChain
      ↓
GET public rule?
NO
      ↓
/books/** ADMIN rule?
YES
      ↓
Requires ROLE_ADMIN
      ↓
No authenticated user
      ↓
Request rejected
      ↓
Controller never executes
```

---

## 15. Example: already authenticated user

Suppose the client sends:

```http
Cookie: JSESSIONID=ABC123
```

Spring Security may restore:

```text
SecurityContext
└── Authentication
    ├── principal = "binh"
    ├── authorities = [ROLE_ADMIN]
    └── authenticated = true
```

Then:

```text
DELETE /books/5
      ↓
SecurityFilterChain
      ↓
restore SecurityContext
      ↓
Authentication = Bình
ROLE_ADMIN
      ↓
authorization
      ↓
requires ADMIN
      ↓
PASS
      ↓
BookController
```

---

## 16. What does "restore the SecurityContext" mean?

After successful login, Spring Security associates the authenticated security state with the HTTP session.

Conceptually:

```text
Session ABC123
└── SecurityContext
    └── Authentication
        ├── principal = "binh"
        └── authorities = [ROLE_USER]
```

The browser normally stores only:

```text
JSESSIONID=ABC123
```

On a later request:

```text
JSESSIONID=ABC123
        ↓
find HTTP session
        ↓
find saved SecurityContext
        ↓
make it the current request's SecurityContext
```

That is what "restore the saved SecurityContext" means.

It avoids authenticating with username/password on every request.

---

## 17. Thread-associated `SecurityContext`

During a synchronous servlet request, Spring Security commonly associates the current `SecurityContext` with the thread processing that request.

Example:

```text
Thread-17
└── SecurityContext
    └── Authentication
        └── Bình
```

This allows code during the request to access:

```java
SecurityContextHolder
    .getContext()
    .getAuthentication();
```

After the request finishes, Spring clears the thread-associated context.

Why?

Because Tomcat reuses threads.

Without clearing:

```text
Bình's request
→ Thread-17
→ SecurityContext = Bình

request ends

Alice's request
→ Thread-17 reused
→ old SecurityContext could still say Bình ❌
```

So Spring does:

```text
Request starts
    ↓
restore SecurityContext
    ↓
associate with request thread
    ↓
process request
    ↓
request finishes
    ↓
clear thread-associated SecurityContext
```

Important:

```text
clearing the thread-associated SecurityContext
≠ logging the user out
```

The HTTP session can still remain valid.

---

## 18. Does `SecurityFilterChain` authenticate every request?

No.

The exact path depends on the request.

### Public request

```text
GET /books
→ no username/password authentication required
```

### Login request

```text
POST /auth/login
→ credentials submitted
→ authentication occurs
```

### Later authenticated session request

```text
GET /auth/me
Cookie: JSESSIONID=ABC123
→ restore existing SecurityContext
→ password normally not checked again
```

So the security flow is conditional.

---

## 19. Login flow through the security chain

Suppose form login is configured:

```java
.formLogin(form -> form
    .loginProcessingUrl("/auth/login")
)
```

Then a request like:

```http
POST /auth/login
username=binh&password=123456
```

can be processed before the controller:

```text
SecurityFilterChain
        ↓
authentication filter recognizes /auth/login
        ↓
extract username/password
        ↓
AuthenticationManager
        ↓
AuthenticationProvider
        ↓
UserDetailsService
        ↓
UserRepository
        ↓
PostgreSQL
        ↓
PasswordEncoder.matches()
        ↓
authenticated Authentication
        ↓
SecurityContext
        ↓
HTTP Session
```

When Spring Security's built-in form-login filter handles `/auth/login`, you do not necessarily need a controller method such as:

```java
@PostMapping("/auth/login")
```

---

## 20. `SecurityFilterChain` vs `AuthenticationManager`

Do not confuse them.

```text
SecurityFilterChain
= processes/protects HTTP requests
```

while:

```text
AuthenticationManager
= coordinates an authentication attempt
```

Relationship:

```text
SecurityFilterChain
   ↓
authentication filter
   ↓
AuthenticationManager
   ↓
AuthenticationProvider
```

`AuthenticationManager` is one component that may be invoked during security-filter processing when authentication is required.

---

## 21. `SecurityFilterChain` vs `SecurityContext`

They also have different responsibilities.

```text
SecurityFilterChain
= security processing pipeline

SecurityContext
= current authentication state
```

Think:

```text
SecurityFilterChain
"Process this HTTP request securely."

SecurityContext
"Who is currently authenticated?"
```

The chain can:

```text
restore SecurityContext
use Authentication
authorize request
clear thread association afterward
```

---

## 22. `SecurityFilterChain` vs `@PreAuthorize` vs `@Transactional`

These represent three different boundaries.

```text
SecurityFilterChain
→ HTTP boundary

@PreAuthorize
→ method/service boundary

@Transactional
→ transaction boundary
```

### `SecurityFilterChain` → HTTP boundary

Example:

```java
.requestMatchers(
    HttpMethod.PATCH,
    "/books/**"
).hasRole("ADMIN")
```

Request:

```http
PATCH /books/5
```

Flow:

```text
HTTP request
    ↓
SecurityFilterChain
    ↓
Does current user have ROLE_ADMIN?
```

If the user only has:

```text
ROLE_USER
```

then:

```text
PATCH /books/5
      ↓
SecurityFilterChain
      ↓
requires ADMIN
      ↓
USER ❌
      ↓
403 Forbidden
```

And:

```text
BookController
    ❌ never called

BookService
    ❌ never called
```

The HTTP boundary answers:

> **May this HTTP request enter through this route?**

### `@PreAuthorize` → method/service boundary

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
public Book setStock(long id, int stock) {
    ...
}
```

This protects the Java method itself.

Why can this be useful if the HTTP route is already protected?

Because another Spring bean could call the service directly:

```java
@Service
public class InventoryImportService {

    private final BookService bookService;

    public void importStock() {
        bookService.setStock(5L, 100);
    }
}
```

Flow:

```text
InventoryImportService
        ↓
BookService proxy
        ↓
@PreAuthorize
        ↓
Does current Authentication have ADMIN?
```

If not:

```text
bookService.setStock(...)
        ↓
@PreAuthorize
        ↓
USER is not ADMIN
        ↓
AccessDeniedException
        ↓
method body does not execute
```

The method boundary answers:

> **May the current authenticated user execute this business operation?**

### `@Transactional` → transaction boundary

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public Book setStock(long id, int stock) {

    Book book = repository.findById(id)
        .orElseThrow();

    book.setStock(stock);

    return book;
}
```

`@Transactional` does not check whether the user is ADMIN.

It controls the database unit of work.

Successful flow:

```text
ADMIN authorized
      ↓
@Transactional starts transaction
      ↓
SELECT Book
      ↓
Book becomes managed
      ↓
change stock
      ↓
Hibernate dirty checking
      ↓
UPDATE
      ↓
COMMIT
```

Failure flow:

```text
transaction starts
      ↓
change Book A
      ↓
change Book B
      ↓
RuntimeException
      ↓
ROLLBACK
```

So:

```text
Security
→ decides WHO may run the operation

Transaction
→ decides HOW database work succeeds/fails together
```

---

## 23. Full example combining all three boundaries

Controller:

```java
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @PatchMapping("/{id}")
    public BookResponse setStock(
            @PathVariable long id,
            @RequestBody StockRequest request) {

        return toResponse(
            bookService.setStock(id, request.stock())
        );
    }
}
```

HTTP security:

```java
.requestMatchers(
    HttpMethod.PATCH,
    "/books/**"
).hasRole("ADMIN")
```

Service:

```java
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public Book setStock(long id, int stock) {

    Book book = repository.findById(id)
        .orElseThrow();

    book.setStock(stock);

    return book;
}
```

Full flow:

```text
PATCH /books/5
      ↓
┌─────────────────────────────┐
│ SecurityFilterChain         │
│ HTTP boundary               │
│                             │
│ "May this HTTP request in?" │
└─────────────────────────────┘
      ↓ ADMIN passes

BookController
      ↓

BookService proxy
      ↓
┌─────────────────────────────┐
│ @PreAuthorize               │
│ Method boundary             │
│                             │
│ "May this user run this     │
│  business method?"          │
└─────────────────────────────┘
      ↓ ADMIN passes

┌─────────────────────────────┐
│ @Transactional              │
│ Transaction boundary        │
│                             │
│ "Which DB work must commit  │
│  or roll back together?"    │
└─────────────────────────────┘
      ↓

BookService.setStock()
      ↓
BookRepository
      ↓
Hibernate
      ↓
PostgreSQL
      ↓
COMMIT / ROLLBACK
```

### Three failure examples

| Situation | Boundary | Result |
|---|---|---|
| Anonymous calls `PATCH /books/5` | `SecurityFilterChain` | Rejected before controller |
| Another Spring bean calls `setStock()` as `USER` | `@PreAuthorize` | Service method denied |
| `ADMIN` is allowed but DB operation fails | `@Transactional` | Database work rolls back |

---

## 24. Can an application have multiple `SecurityFilterChain`s?

Yes.

Example:

```text
Chain 1
→ /api/**

Chain 2
→ /admin/**
```

Conceptually:

```text
Request
   ↓
FilterChainProxy
   ↓
Which SecurityFilterChain matches?
   ├── /api/**   → Chain A
   └── /admin/** → Chain B
```

This is useful when different groups of routes require different security mechanisms or policies.

For a simple Book Catalog project, one chain is usually enough.

---

## 25. Security-chain matching vs authorization matching

These are two different questions:

```text
Question 1:
Which SecurityFilterChain handles this request?

Question 2:
Inside that chain, which authorization rule applies?
```

For a simple application with one chain:

```text
all application requests
        ↓
same SecurityFilterChain
        ↓
requestMatchers(...)
        ↓
authorization rule chosen
```

---

## 26. Connection to other Spring concepts

`SecurityFilterChain` connects naturally to concepts from the broader Spring stack:

```text
Spring Core
→ @Bean
→ IoC manages SecurityFilterChain

Spring MVC
→ filters run before DispatcherServlet

Spring Security
→ authentication + authorization

Session
→ SecurityContext can be restored

JPA/Hibernate
→ login may load AppUser

HikariCP
→ login DB query uses pooled connection

Transactions
→ business method transaction runs after security allows the call
```

A protected request can look like:

```text
Client
  ↓
SecurityFilterChain
  ↓
SecurityContext restored
  ↓
Authorization passes
  ↓
BookController
  ↓
BookService proxy
  ↓
@PreAuthorize passes
  ↓
@Transactional begins
  ↓
BookRepository
  ↓
Hibernate
  ↓
JDBC
  ↓
HikariCP
  ↓
PostgreSQL
```

---

## 27. Five things to remember

| Concept | Mental model |
|---|---|
| `SecurityFilterChain` | Security pipeline before MVC |
| `HttpSecurity` | Builder used to configure the pipeline |
| `requestMatchers()` | Define which requests a rule applies to |
| `permitAll()`, `authenticated()`, `hasRole()` | Authorization decisions |
| `http.build()` | Creates the final `SecurityFilterChain` |

---

## 28. Final mental model

```text
HTTP request
      ↓
SecurityFilterChain
      ↓
Restore/load authentication state
      ↓
Run relevant security filters
      ↓
Authorization rule
      ↓
Allowed?
  /          \
NO            YES
↓              ↓
401/403      DispatcherServlet
                 ↓
              Controller
                 ↓
              Service
```

And remember the three major boundaries:

```text
SecurityFilterChain
→ "May this HTTP request enter?"

@PreAuthorize
→ "May this user execute this method?"

@Transactional
→ "If the method runs, which DB changes succeed or fail together?"
```

## 29. Interview answer

If asked:

> **What is `SecurityFilterChain` in Spring Security?**

A concise answer is:

> **`SecurityFilterChain` is a Spring-managed chain of security filters that processes matching HTTP requests before Spring MVC. It handles concerns such as restoring the `SecurityContext`, authentication, CSRF, exception translation, and authorization. It is typically configured using `HttpSecurity` and exposed as a `@Bean`.**
