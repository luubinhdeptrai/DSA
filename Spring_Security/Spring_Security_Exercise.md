# Spring Security exercise — secure the existing Book Catalog

This lab extends `Flyway/book-catalog-api`; it does not create another application. The project uses Java 21, Spring Boot **4.1.1** (which manages Spring Security **7.1.1**), Spring MVC, Spring Data JPA, PostgreSQL, HikariCP, and Flyway. Its current migrations are `V1` through `V5`; Hibernate uses `ddl-auto: validate`. The controllers are `BookController` and `TransactionLabController`. There is currently no Spring Security dependency, user table, or authentication mechanism.

Work through the tasks before the reference implementation. Predict each HTTP result, test it, then explain the request path aloud: client → security filters → authorization → controller → service → repository → PostgreSQL. A credential lookup additionally goes through `AuthenticationManager` → `DaoAuthenticationProvider` → `UserDetailsService` → `PasswordEncoder`.

## The policy you will build

| Request | Policy | Reason |
|---|---|---|
| `GET /books`, `GET /books/{id}` | Public | Anyone can browse the catalog. |
| `GET /auth/csrf`, `POST /auth/register`, `POST /auth/login` | Public entry points | Registration/login are anonymous use cases; unsafe requests still need CSRF. |
| `GET /auth/me` | Authenticated | A small way to observe the current principal. |
| `POST`, `PUT`, `PATCH`, `DELETE /books/**` | `ADMIN` | Catalog writes are privileged. |
| `POST /transaction-lab/transfers` | `ADMIN` | This endpoint changes stock and audit rows, even when demonstrating rollbacks. |
| Other application routes | Denied | No unnoticed public application routes. |

`USER` and `ADMIN` are **application** roles in `app_users`, not PostgreSQL database roles. Spring's `hasRole("ADMIN")` checks the authority `ROLE_ADMIN`.
Because this lab uses Spring Security's built-in form-login machinery without a custom login page, the framework may still serve its generated `GET /login` page. The API exercise uses `POST /auth/login`; the generated page is not a Book Catalog application route or the login endpoint used below.

## Your tasks — stop before the reference

### 1. Inspect the existing application

Read `pom.xml`, both controllers, the services, repositories, entities, DTOs, `GlobalExceptionHandler`, `application.yaml`, `compose.yaml`, and migrations `V1`–`V5`. List every endpoint. Which are public now? Which can mutate the database? Notice that `BookService.delete()` deliberately flushes and then throws a checked exception; its transaction rolls back. Notice that `/transaction-lab/transfers` is a mutation too. Verify that there is no security starter or user table.

- **Objective:** Produce an endpoint-and-data map before changing behavior.
- **Concept:** Controllers expose routes; services own transactions; repositories reach PostgreSQL.
- **What to implement:** A short inventory on paper only; do not edit source yet.
- **Starter code:** None—read the actual source, beginning with `BookController` and `TransactionLabController`.
- **Hints:** `GET /books` and `GET /books/{id}` read; Book POST/PUT/PATCH/DELETE and transfer POST mutate.
- **How to verify:** Find all controller mapping annotations and the latest Flyway version.
- **Common mistakes:** Forgetting the transaction-lab endpoint or assuming DELETE currently succeeds.
- **Explanation prompt:** Which public request can currently change stock, and why?

### 2. Add Spring Security

Add `spring-boot-starter-security` without a version; Boot's parent manages it. Do not replace PostgreSQL, Flyway, or JPA. Start the application before writing custom security configuration.

- **Objective:** See the starter's effect in isolation.
- **Concept:** Boot auto-configuration creates a default chain when you have not defined one.
- **What to implement:** One Maven dependency, then start the existing app.
- **Starter code:** `<artifactId>spring-boot-starter-security</artifactId>` inside a dependency with Boot's group ID.
- **Hints:** Keep the parent at 4.1.1; it manages Security 7.1.1.
- **How to verify:** Maven resolves the starter and the application starts against the same PostgreSQL database.
- **Common mistakes:** Pinning a different Security version or creating a second project.
- **Explanation prompt:** What did dependency injection add to the HTTP request path?

### 3. Observe Boot's default

Call `GET /books`. Record the status, `WWW-Authenticate`/redirect behavior, and startup-generated development password if one appears. Do not treat that generated password as an application account. Why did adding one starter change all routes?

- **Objective:** Establish a before/after observation.
- **Concept:** Default security protects routes until an explicit chain replaces it.
- **What to implement:** No custom code yet; record the response and startup message.
- **Starter code:** `curl -i http://127.0.0.1:8080/books`.
- **Hints:** The response can differ by client `Accept` header; inspect status and headers, not only the body.
- **How to verify:** Compare with the same request before adding the starter.
- **Common mistakes:** Treating the generated development user as a database row.
- **Explanation prompt:** Why can a filter reject a request before `BookController` runs?

### 4. Create `SecurityFilterChain`

Create a `@Configuration` class with a `SecurityFilterChain` bean using `HttpSecurity` and `@EnableMethodSecurity`. Which rules belong in the filter chain, and which belong on a service method? Avoid `WebSecurityConfigurerAdapter` and old chained configuration styles.

- **Objective:** Make security policy an explicit Spring bean.
- **Concept:** `FilterChainProxy` invokes the matching `SecurityFilterChain` before MVC.
- **What to implement:** `SecurityConfig` with `@Configuration`, `@EnableMethodSecurity`, and a `SecurityFilterChain` bean.
- **Starter code:** `@Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { /* rules */ return http.build(); }`
- **Hints:** Lambda-based `authorizeHttpRequests` is the modern configuration style.
- **How to verify:** Startup no longer relies on the starter's all-protected default policy.
- **Common mistakes:** Using removed `WebSecurityConfigurerAdapter` or omitting `http.build()`.
- **Explanation prompt:** Why is this configuration injected rather than called from controllers?

### 5. Implement the policy above

Use `authorizeHttpRequests` with specific matchers before the fallback. Keep `GET /books/**` public, require `ADMIN` for every Book mutation and the transaction lab, and make `/auth/me` authenticated. Explain why allowing `/transaction-lab/**` anonymously would bypass the spirit of the policy. What happens if a general `permitAll()` matcher is placed first?

- **Objective:** Protect every existing mutation without breaking catalog reads.
- **Concept:** URL authorization matches in declared order; roles map to authorities.
- **What to implement:** Specific GET/public and auth matchers, then Book/lab ADMIN matchers, then `denyAll`.
- **Starter code:** `auth.requestMatchers(HttpMethod.GET, "/books", "/books/**").permitAll()`.
- **Hints:** Permit the CSRF/registration/login entry points by exact method/path; do not permit the whole `/auth/**` tree.
- **How to verify:** Anonymous reads work, while an unauthenticated safe protected request does not.
- **Common mistakes:** Broad `permitAll()` before ADMIN rules; missing `/transaction-lab/**`.
- **Explanation prompt:** Why is `/books/**` alone not the entire write surface?

### 6. Version the user schema

Create **new** `V6__create_app_users.sql`, with `id`, unique non-null `username`, non-null `password_hash`, and a constrained `role`. Do not edit `V1`–`V5`. Inspect `flyway_schema_history` and the new table after startup. Why does `ddl-auto: validate` reinforce, rather than replace, Flyway?

- **Objective:** Give authentication durable application data.
- **Concept:** Flyway owns schema evolution; Hibernate validates mappings against it.
- **What to implement:** New `V6__create_app_users.sql` in the existing migration directory.
- **Starter code:** `CREATE TABLE app_users (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, ...);`
- **Hints:** Use `UNIQUE` on username and a small role check constraint; store a hash, not a password.
- **How to verify:** Inspect both `app_users` and successful `V6` in `flyway_schema_history`.
- **Common mistakes:** Editing an applied V1–V5 file or relying on `ddl-auto: create`.
- **Explanation prompt:** Which component creates the table, and which detects an entity/table mismatch?

### 7. Map an application user

Add a small JPA entity for `app_users`. Do not return it from an HTTP controller: it holds the password hash. Which object represents database data, and which object represents Spring Security's authenticated user?

- **Objective:** Map the application account without exposing its hash.
- **Concept:** A JPA entity and Spring Security's `UserDetails` are different types with different purposes.
- **What to implement:** `AppUser` fields matching the V6 columns, with no controller serialization.
- **Starter code:** `@Entity @Table(name = "app_users") public class AppUser { ... }`.
- **Hints:** Map `passwordHash` to `password_hash`; retain a protected no-arg constructor for JPA.
- **How to verify:** Application startup passes Hibernate schema validation.
- **Common mistakes:** Returning the entity from `/auth/me` or mapping the column to raw `password`.
- **Explanation prompt:** Why is a dedicated safe response DTO needed?

### 8. Add `AppUserRepository`

Extend `JpaRepository`; add `Optional<AppUser> findByUsername(String username)` and a duplicate check. Trace its database access through Hibernate → JDBC → the existing Hikari pool → PostgreSQL.

- **Objective:** Retrieve accounts and detect duplicate usernames.
- **Concept:** Spring Data generates repository queries over the same datasource as Book data.
- **What to implement:** `AppUserRepository` with `findByUsername` and `existsByUsername`.
- **Starter code:** `interface AppUserRepository extends JpaRepository<AppUser, Long> { Optional<AppUser> findByUsername(String username); }`
- **Hints:** `Optional` represents a missing account; it is not authentication by itself.
- **How to verify:** Log in later and observe a query on `app_users` in SQL debug output.
- **Common mistakes:** Adding a second datasource or returning `null` for missing users.
- **Explanation prompt:** Where does HikariCP fit into this lookup?

### 9. Add `UserDetailsService`

Adapt `AppUser` to `UserDetails`, including username, hash, and authority. `UserDetailsService` loads data; it does **not** itself compare passwords. What does `DaoAuthenticationProvider` do with the loaded user?

- **Objective:** Bridge JPA user storage into Spring Security's authentication contract.
- **Concept:** `DaoAuthenticationProvider` calls `loadUserByUsername` and later verifies the supplied password.
- **What to implement:** A Spring service implementing `UserDetailsService`.
- **Starter code:** `UserDetails loadUserByUsername(String username) throws UsernameNotFoundException`.
- **Hints:** `User.withUsername(...).password(hash).roles(role).build()` adapts one row.
- **How to verify:** Valid login works after seeding; unknown username gives generic 401.
- **Common mistakes:** Calling `PasswordEncoder.matches` here or prefixing stored `ADMIN` twice.
- **Explanation prompt:** Which object owns the DB query, and which owns credential comparison?

### 10. Configure `PasswordEncoder`

Publish a `BCryptPasswordEncoder` bean. For registration use `encode(raw)`; for login the provider uses `matches(raw, storedHash)`. Check UTF-8 byte length first: BCrypt 7.1.1 rejects inputs over 72 bytes, which a Java character count cannot reliably detect. Explain why two BCrypt encodings of one password differ and why `raw.equals(hash)` is wrong.

- **Objective:** Persist only salted adaptive hashes.
- **Concept:** Hash verification is one-way; passwords are not decrypted.
- **What to implement:** A `PasswordEncoder` bean and registration-time `encode` call.
- **Starter code:** `@Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }`
- **Hints:** Use `password.getBytes(StandardCharsets.UTF_8).length <= 72` before BCrypt encoding.
- **How to verify:** The DB value differs from raw input and a correct login still succeeds.
- **Common mistakes:** `raw.equals(hash)`, encoding twice and comparing literal hashes, or ignoring the byte limit.
- **Explanation prompt:** Why do two hashes differ even with the same password?

### 11. Make disposable practice accounts safely

Use public registration to make a `USER`. Use a **local-profile-only** bootstrap that reads an admin username/password from environment variables, hashes the password, and inserts an `ADMIN` only if absent. It must never silently promote an existing `USER`. Do not hard-code or log raw credentials; do not include a real password in SQL or YAML. Why is a public “register as ADMIN” request unsafe?

- **Objective:** Have two roles to compare without an ADMIN-registration vulnerability.
- **Concept:** Role assignment is trusted server-side provisioning, not client input.
- **What to implement:** Public USER registration plus `@Profile("local")` admin bootstrap.
- **Starter code:** `@Profile("local") class LocalAdminBootstrap implements ApplicationRunner { ... }`.
- **Hints:** Read disposable admin credentials from environment, encode once, and refuse a username already used by a USER.
- **How to verify:** One USER and one ADMIN row exist, both with hashes; repeat startup creates no duplicate admin.
- **Common mistakes:** Plain-text SQL seed, logging secrets, or silently upgrading an existing account.
- **Explanation prompt:** Who is trusted to choose `ADMIN`, and why?

### 12. Implement registration

Add `POST /auth/register` with a validated DTO containing only username and password. Reject malformed input, assign `USER` on the server, and preserve the database unique constraint as the final race-safe check. With the project's `fail-on-unknown-properties: true`, a submitted `role` field is rejected. Decide what duplicate username should return (the reference uses 409).

- **Objective:** Practice validation, hashing, transaction boundaries, and safe role assignment together.
- **Concept:** A DTO shapes accepted input; validation cannot grant authority; UNIQUE protects races.
- **What to implement:** `RegisterRequest`, transactional `RegistrationService`, and controller endpoint.
- **Starter code:** `@PostMapping("/register") void register(@Valid @RequestBody RegisterRequest request)`.
- **Hints:** Assign literal `USER` in service code; check UTF-8 byte count before BCrypt; flush to surface unique conflicts.
- **How to verify:** Good request yields 201; duplicate 409; malformed body 400; extra `role` rejected.
- **Common mistakes:** Mapping request directly to the entity or trusting a client-provided role.
- **Explanation prompt:** What protects uniqueness if two requests pass an `exists` check simultaneously?

### 13. Connect username/password authentication

Trace: submitted credentials → form-login filter → `AuthenticationManager` → `DaoAuthenticationProvider` → `UserDetailsService` → `AppUserRepository` → database → `PasswordEncoder.matches()` → authenticated `Authentication`. A `UserDetailsService` and `PasswordEncoder` bean are enough for the standard provider; do not write a password-comparison controller.

- **Objective:** Identify the standard provider behind a successful login.
- **Concept:** The manager delegates; the DAO provider loads `UserDetails` and checks the hash.
- **What to implement:** No custom manager/provider; wire one `UserDetailsService` and one encoder bean.
- **Starter code:** Draw `UsernamePasswordAuthenticationToken → AuthenticationManager → DaoAuthenticationProvider`.
- **Hints:** The controller does not see raw login credentials when the form-login filter processes the URL.
- **How to verify:** Correct credentials authenticate; bad credentials use the configured failure handler.
- **Common mistakes:** Manually comparing passwords in a controller or querying users on every session request.
- **Explanation prompt:** Which component actually calls `matches()`?

### 14. Log in with an HTTP session

Configure Spring Security's built-in **form login** at `POST /auth/login`. Send `application/x-www-form-urlencoded` fields `username` and `password`, not JSON. Use a status-only success response and a generic 401 failure response. Keep CSRF enabled, including on login. Do not configure `loginPage("/auth/login")` unless you also build its GET page.

- **Objective:** Establish a stateful authenticated session through a standard filter.
- **Concept:** Form login verifies credentials and saves `SecurityContext` in an HTTP session.
- **What to implement:** Configure `loginProcessingUrl`, success/failure handlers, and keep CSRF.
- **Starter code:** `.formLogin(form -> form.loginProcessingUrl("/auth/login").successHandler(...).failureHandler(...))`.
- **Hints:** Fetch `/auth/csrf` before login; preserve its cookie and use the returned header.
- **How to verify:** Valid login returns 204 and a usable session; invalid password returns 401.
- **Common mistakes:** Sending JSON to the form filter, disabling CSRF, or assuming `loginPage` creates a custom page.
- **Explanation prompt:** Where is the authentication saved for the next request?

### 15. Observe `JSESSIONID`

Obtain a CSRF token, preserve the cookie jar, log in, then inspect the session cookie. The cookie contains a session **identifier**, not the `SecurityContext` or password. Send it on another request. The session ID can change at login because of session-fixation protection. What state is on the server?

- **Objective:** Observe browser-style session continuity.
- **Concept:** The server retains security context; the client presents only the session ID cookie.
- **What to implement:** No Java code; use one cookie jar for token fetch, login, and follow-up.
- **Starter code:** `curl -i -c user.cookies -b user.cookies ...` (full commands follow the barrier).
- **Hints:** Login may rotate the existing session ID instead of creating an entirely new session.
- **How to verify:** `/auth/me` works with the current cookie jar and fails without it.
- **Common mistakes:** Treating `JSESSIONID` as a JWT or copying a pre-login token to post-login writes.
- **Explanation prompt:** What changes on the wire versus on the server at login?

### 16. Inspect the principal

Add `GET /auth/me` using `@AuthenticationPrincipal` (or `Principal`). Return only username and authorities, never the password hash. Compare anonymous and logged-in calls.

- **Objective:** See the authenticated identity safely.
- **Concept:** `SecurityContext` holds an `Authentication` with principal and authorities.
- **What to implement:** A protected endpoint returning a small response DTO.
- **Starter code:** `CurrentUserResponse me(@AuthenticationPrincipal UserDetails principal)`.
- **Hints:** `principal.getAuthorities()` is safe to report; `principal.getPassword()` is not.
- **How to verify:** Anonymous gets 401; USER and ADMIN see their own usernames/roles.
- **Common mistakes:** Returning `AppUser` or raw `UserDetails` directly.
- **Explanation prompt:** Where did `@AuthenticationPrincipal` obtain its value?

### 17. Test role authorization

Register/login as `USER`; log in as local `ADMIN` in a separate cookie jar. Test public reads, `/auth/me`, `POST /books`, and `PATCH /books/{id}`. Predict the statuses before calling. A successful `POST /books` should be 201; a valid stock PATCH should be 200.

- **Objective:** Compare anonymous, USER, and ADMIN outcomes on real Book routes.
- **Concept:** Authentication identifies; authorization decides allowed operations.
- **What to implement:** A manual three-identity request table, not another role system.
- **Starter code:** `GET /books`, `GET /auth/me`, `POST /books`, `PATCH /books/{id}`.
- **Hints:** Use distinct cookie jars and fresh CSRF tokens after each login.
- **How to verify:** Record the result matrix and check successful ADMIN writes in PostgreSQL.
- **Common mistakes:** Reusing the ADMIN cookie for USER tests or sending an invalid Book DTO.
- **Explanation prompt:** Why may both users read, but only one write?

### 18. Distinguish 401 from 403

Anonymous `GET /auth/me` → 401. Logged-in `USER` making a Book write **with a valid CSRF token** → 403. Logged-in `ADMIN` making the same valid write reaches the controller. For an anonymous unsafe request **without** CSRF, expect 403 first: `CsrfFilter` rejects it before the authorization decision. Do not use that case to demonstrate 401.

- **Objective:** Separate absent identity, insufficient authority, and invalid CSRF.
- **Concept:** 401 means authentication is needed; 403 means a request is forbidden.
- **What to implement:** Three manual calls with controlled cookie/token state.
- **Starter code:** `GET /auth/me` without cookie; `PATCH /books/1` as USER with token.
- **Hints:** A missing token can mask the authentication experiment on unsafe methods.
- **How to verify:** Compare status, the generic error body, and whether the controller ran.
- **Common mistakes:** Claiming every anonymous POST must return 401.
- **Explanation prompt:** Which filter made each decision, and in what order?

### 19. Customize 401

Supply a REST-friendly `AuthenticationEntryPoint` with a generic `application/problem+json` response. Also use it for failed login. Do not disclose whether the username exists. Why won't the existing `@RestControllerAdvice` normally handle a missing-session rejection in the security filter chain?

- **Objective:** Make missing/invalid authentication useful to API clients without leaking details.
- **Concept:** `AuthenticationEntryPoint` handles authentication commencement in the filter chain.
- **What to implement:** A fixed generic 401 Problem JSON handler; wire it in `exceptionHandling` and login failure.
- **Starter code:** `void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)`.
- **Hints:** Do not serialize `ex.getMessage()` or the submitted username.
- **How to verify:** Anonymous `/auth/me` and bad-password login both produce 401 JSON.
- **Common mistakes:** Expecting controller advice to catch a filter rejection.
- **Explanation prompt:** Why did the request never reach the controller?

### 20. Customize 403

Supply an `AccessDeniedHandler` for denied permissions and CSRF rejection. Compare it to the entry point. A status alone does not reveal whether the 403 was caused by role authorization or CSRF; inspect the request and safe debug logs while learning.

- **Objective:** Return a consistent forbidden response.
- **Concept:** The exception handling configuration supplies the denied handler used for authorization and CSRF attempts.
- **What to implement:** A generic 403 Problem JSON handler wired through `exceptionHandling`.
- **Starter code:** `void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)`.
- **Hints:** Keep the body generic; use request conditions to distinguish missing CSRF from missing role.
- **How to verify:** Test USER write with valid token and any unsafe write without token.
- **Common mistakes:** Calling `AuthenticationEntryPoint` for every denial or leaking internal exception text.
- **Explanation prompt:** How does this handler's responsibility differ from the 401 handler?

### 21. Add one method-security guard

Put `@PreAuthorize("hasRole('ADMIN')")` on `BookService.setStock`, a meaningful business mutation. URL rules already guard this HTTP route; the method annotation also protects another Spring bean calling that public service method. Method security is proxy-based: a call from inside the same instance can bypass its proxy.

- **Objective:** Protect one business use case beyond a single HTTP route.
- **Concept:** `@EnableMethodSecurity` uses Spring proxy interception for `@PreAuthorize`.
- **What to implement:** One annotation on the existing public `setStock` service method.
- **Starter code:** `@PreAuthorize("hasRole('ADMIN')")` above `@Transactional`.
- **Hints:** Keep the URL rule too because it gives early rejection at the HTTP boundary.
- **How to verify:** USER cannot PATCH stock; ADMIN can, given valid CSRF and valid payload.
- **Common mistakes:** Annotating a private method or relying on self-invocation to trigger the proxy.
- **Explanation prompt:** What does a service-level guard protect that an HTTP matcher cannot?

### 22. Relate method security to transactions

`BookService.setStock` now has `@PreAuthorize` and `@Transactional`. Explain: authorization decides **who may run it**; the transaction decides **how its database work commits or rolls back**. The existing `GlobalExceptionHandler` has a broad `Exception` handler; add a specific `AccessDeniedException` handler for method denials reaching MVC so they do not accidentally become 500. URL-level denials still use the filter-chain handler.

- **Objective:** Keep authorization and atomicity conceptually and operationally separate.
- **Concept:** Security and transaction advice are different interceptors; MVC may resolve method-denial exceptions.
- **What to implement:** Specific 403 handling for `AccessDeniedException` in existing advice.
- **Starter code:** `@ExceptionHandler(AccessDeniedException.class)` before the broad exception handler.
- **Hints:** URL denials use the filter handler; method denials raised inside MVC may use advice.
- **How to verify:** A method denial never becomes the existing catch-all's 500.
- **Common mistakes:** Assuming a transaction annotation grants permissions or one error handler catches all layers.
- **Explanation prompt:** If authorization rejects the method, what database work should run?

### 23. Run the CSRF experiment

Try `POST /auth/login` or `PATCH /books/{id}` without a CSRF token: 403. `GET /books` needs no token. Why can an anonymous `POST /auth/register` still require a token even though its authorization rule is `permitAll()`? `permitAll` does not disable CSRF.

- **Objective:** Observe the browser-cookie attack defense directly.
- **Concept:** CSRF checks unsafe methods before controller logic, separately from route authorization.
- **What to implement:** A comparison table for GET and POST/PATCH with and without token.
- **Starter code:** `POST /auth/login` with a cookie but no `X-CSRF-TOKEN` header.
- **Hints:** The default protected methods are POST, PUT, PATCH, DELETE and other non-safe methods.
- **How to verify:** Tokenless unsafe request is 403; safe public GET remains available.
- **Common mistakes:** Interpreting the 403 as a bad password or automatically disabling CSRF.
- **Explanation prompt:** Why does a browser's automatic cookie sending create this risk?

### 24. Use one clear CSRF strategy

Keep Spring Security's default `HttpSessionCsrfTokenRepository`. Expose `GET /auth/csrf`; it returns `token` and `headerName` and creates/uses a session. Keep the matching cookie jar and send the returned value in the named header on every unsafe request. Fetch a **fresh** token after successful login and after logout; Spring Security clears the old token at those transitions. Do not replace this with “REST means disable CSRF.”

- **Objective:** Make CSRF-protected session authentication usable by Postman/curl.
- **Concept:** The expected token is session-backed; a client proves intent by echoing it in a header.
- **What to implement:** Public GET token endpoint accepting Spring's `CsrfToken` argument.
- **Starter code:** `@GetMapping("/csrf") CsrfToken csrf(CsrfToken token) { return token; }`
- **Hints:** Save/send the same cookie jar; use the response's `headerName`; refetch after login/logout.
- **How to verify:** Token + cookie succeeds where the same unsafe call without token is 403.
- **Common mistakes:** Sending a token with the wrong session cookie or reusing one cleared at login.
- **Explanation prompt:** Why are both a cookie and a request header needed?

### 25. Consider CORS separately

This repository has no cross-origin browser client and binds to `127.0.0.1`, so add no CORS policy for the lab. Postman and curl are not restricted by browser CORS. If a separate browser frontend is later added, allow only its exact origin and required methods/headers and deliberately handle credentials. Explain why CORS ≠ authentication ≠ authorization ≠ CSRF.

- **Objective:** Avoid unnecessary cross-origin configuration while understanding its boundary.
- **Concept:** CORS governs browser JavaScript origin access; it does not prove user identity or intent.
- **What to implement:** No CORS bean for this local API-only exercise.
- **Starter code:** None until a real browser frontend with a known origin exists.
- **Hints:** Postman/curl are not blocked by browser preflight rules.
- **How to verify:** Postman/curl can call the local API without CORS configuration; a same-origin browser page would also need no cross-origin allowance.
- **Common mistakes:** Using wildcard origins with credentials or treating CORS as CSRF protection.
- **Explanation prompt:** What question does CORS answer that CSRF does not?

### 26. Log out

Use Spring Security's `POST /auth/logout` (not a handmade controller), with current cookie and CSRF header. It invalidates the HTTP session, clears authentication/security context and CSRF state, and returns 204. The old cookie may remain in a client jar, but it no longer authenticates. Verify `/auth/me` returns 401 afterward.

- **Objective:** End a server-side authenticated session correctly.
- **Concept:** `LogoutFilter` runs logout handlers before MVC and clears session/security/CSRF state.
- **What to implement:** Configure logout URL and a 204 success handler.
- **Starter code:** `.logout(logout -> logout.logoutUrl("/auth/logout").logoutSuccessHandler(...))`.
- **Hints:** Send POST with the current CSRF token; GET `/logout` is not the logout action under CSRF.
- **How to verify:** Logout returns 204; old cookie then gets 401 at `/auth/me`.
- **Common mistakes:** Making a controller that only clears a local variable, or using GET to mutate session state.
- **Explanation prompt:** What has been invalidated even if the browser still holds the old cookie?

### 27. Verify the password hash

Query `username`, `role`, and safe derived fields from `password_hash` in PostgreSQL using disposable local accounts. Confirm the prefix and length look like a salted BCrypt hash without displaying the full value. Do not print or store any real password or session ID in screenshots/logs. A changed bootstrap environment password does not rotate an already-created admin account.

- **Objective:** Confirm secure storage independently of the HTTP response.
- **Concept:** Stored BCrypt hashes include algorithm/cost/salt information; they are not ciphertext.
- **What to implement:** A read-only SQL check of account rows.
- **Starter code:** `SELECT username, role, left(password_hash, 7), length(password_hash) FROM app_users;`
- **Hints:** View prefix and length instead of copying the whole hash into notes.
- **How to verify:** The hash prefix resembles `$2a$`/`$2b$` and length is around 60.
- **Common mistakes:** Trying to “decrypt” a hash or logging raw passwords for convenience.
- **Explanation prompt:** How does login verification work without recovering the original password?

### 28. Trace one database-backed login

Follow the first login query through form-login filter → manager → DAO provider → `UserDetailsService` → Spring Data JPA → Hibernate → JDBC → HikariCP → PostgreSQL. Then follow `matches()` and session creation. Why does a later request with a valid session normally not query `app_users` again?

- **Objective:** Reconnect security to earlier persistence and infrastructure topics.
- **Concept:** Authentication queries are ordinary repository work; session restoration reuses server-side state.
- **What to implement:** Draw the full login sequence and mark DB and non-DB steps.
- **Starter code:** `form filter → manager → provider → UDS → repository → DB → matches → session`.
- **Hints:** Observe SQL logs only for safe query shape, never credentials or tokens.
- **How to verify:** Compare login-time SQL with a later `/auth/me` call using the session.
- **Common mistakes:** Assuming every request compares the password or that Spring Security replaces HikariCP.
- **Explanation prompt:** Which prior roadmap topics are exercised during the login lookup?

### 29. Verify Flyway and existing behavior

Check `V6` in `flyway_schema_history`, `app_users` in PostgreSQL, and successful Hibernate validation. Verify older migrations remain untouched. Confirm public Book reads, protected Book writes, and protected transaction lab. Authorized `DELETE /books/{id}` is special: the **existing** `BookService.delete()` intentionally throws `TransactionLabCheckedException` after flush, so expect an application 500 and rollback—not a 204. Security allowed the call; business code then failed by design.

- **Objective:** Verify security without mistaking prior lab behavior for a security regression.
- **Concept:** Flyway schema, JPA validation, authorization, and transaction rollback are distinct checks.
- **What to implement:** A manual checklist and read-only DB inspection.
- **Starter code:** `SELECT version, success FROM flyway_schema_history ORDER BY installed_rank;`
- **Hints:** Test admin POST/PATCH for successful writes; DELETE is a deliberate failure test.
- **How to verify:** V6 succeeds, a created Book persists, and an authorized DELETE leaves its Book intact.
- **Common mistakes:** Declaring DELETE security-broken because it returns the existing 500.
- **Explanation prompt:** Which layer allowed DELETE, and which layer rolled it back?

### 30. Optional JWT concept lab

Only after sessions work: trace a hypothetical login → issued signed token → `Authorization: Bearer ...` → token validation → `Authentication` → `SecurityContext` → authorization. A JWT is a token format, not Spring Security itself; it is not automatically safer. Do not replace this working lab with a homemade JWT implementation.

- **Objective:** Contrast the session mechanism with a hypothetical token mechanism.
- **Concept:** A signed JWT can carry claims, but is not automatically encrypted or revocable.
- **What to implement:** A diagram only; this task is optional and changes no code.
- **Starter code:** `Bearer token → validation → Authentication → SecurityContext → authorization`.
- **Hints:** Compare logout/revocation, browser storage, and CSRF/XSS exposure.
- **How to verify:** Explain where session lookup disappears and where token verification appears.
- **Common mistakes:** Calling JWT inherently more secure or replacing this lab with handmade token code.
- **Explanation prompt:** Which tradeoff would make a session simpler for this application?

### 31. Final manual verification

Fill in the result column **before** trying each call; use Postman or curl, application logs, and PostgreSQL. For unsafe calls, note whether a valid CSRF token was sent.

- **Objective:** Demonstrate the final behavior end to end.
- **Concept:** Authentication, authorization, CSRF, validation, and business failures produce different results.
- **What to implement:** Fill the expected and actual columns of the matrix below.
- **Starter code:** Use the route and cookie/token scenarios named in the matrix.
- **Hints:** Keep USER and ADMIN cookie jars separate; refresh CSRF after login.
- **How to verify:** Compare each observed status and DB effect to your prediction.
- **Common mistakes:** Using a missing CSRF token as the 401 demonstration or calling intentional DELETE a success test.
- **Explanation prompt:** Explain one 401, one role 403, one CSRF 403, and one business 500.

| Scenario | Expected result (you fill in) | Actual result |
|---|---|---|
| Anonymous `GET /books` | TODO | TODO |
| Anonymous `GET /auth/me` | TODO | TODO |
| Anonymous unsafe call without CSRF | TODO | TODO |
| Anonymous registration with CSRF | TODO | TODO |
| Invalid login password with CSRF | TODO | TODO |
| Valid login with CSRF | TODO | TODO |
| `USER` calls `/auth/me` | TODO | TODO |
| `USER` writes Book with CSRF | TODO | TODO |
| `ADMIN` creates Book with CSRF | TODO | TODO |
| `ADMIN` runs transaction lab with CSRF | TODO | TODO |
| `ADMIN` deletes Book with CSRF | TODO | TODO |
| Old session after logout | TODO | TODO |
| Invalid or missing CSRF on a write | TODO | TODO |

==================================================
STOP — ATTEMPT THE EXERCISE BEFORE READING SOLUTION
==================================================

## Cumulative reference implementation

The following changes apply to the existing project, not to a new project. Spring Security 7.1 documentation for the [servlet CSRF endpoint/token lifecycle](https://docs.spring.io/spring-security/reference/7.1/servlet/exploits/csrf.html), [form login](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/passwords/form.html), and [logout](https://docs.spring.io/spring-security/reference/7.1/servlet/authentication/logout.html) informs this design. Boot manages Security 7.1.1; do not pin it separately.

### File inventory

Modify:

- `Flyway/book-catalog-api/pom.xml` — one dependency.
- `Flyway/book-catalog-api/src/main/java/com/example/bookcatalog/service/BookService.java` — one import and one method annotation.
- `Flyway/book-catalog-api/src/main/java/com/example/bookcatalog/web/GlobalExceptionHandler.java` — one import and one specific method-security exception handler.

Add:

- `src/main/resources/db/migration/V6__create_app_users.sql`
- `src/main/java/com/example/bookcatalog/model/AppUser.java`
- `src/main/java/com/example/bookcatalog/repository/AppUserRepository.java`
- `src/main/java/com/example/bookcatalog/security/AppUserDetailsService.java`
- `src/main/java/com/example/bookcatalog/security/SecurityConfig.java`
- `src/main/java/com/example/bookcatalog/security/RestAuthenticationEntryPoint.java`
- `src/main/java/com/example/bookcatalog/security/RestAccessDeniedHandler.java`
- `src/main/java/com/example/bookcatalog/security/LocalAdminBootstrap.java`
- `src/main/java/com/example/bookcatalog/dto/RegisterRequest.java`
- `src/main/java/com/example/bookcatalog/dto/CurrentUserResponse.java`
- `src/main/java/com/example/bookcatalog/service/RegistrationService.java`
- `src/main/java/com/example/bookcatalog/web/AuthController.java`

The paths in the “Add” list are relative to `Flyway/book-catalog-api`. Leave `Application.java`, `BookController.java`, `TransactionLabController.java`, the existing Book/transaction DTOs, entities, repositories, and services **other than the listed `BookService` edit**, `compose.yaml`, `application.yaml`, and migrations `V1`–`V5` unchanged. `GlobalExceptionHandler` is modified as listed, not unchanged. In particular, no controller action is added for login or logout: Spring Security's filters own those URLs. No second database or test framework is introduced.

Final project source tree (existing files remain unless marked modified):

```text
Flyway/book-catalog-api/
├── pom.xml (modified)
├── compose.yaml
└── src/
    ├── main/
    │   ├── java/com/example/bookcatalog/
    │   │   ├── Application.java
    │   │   ├── dto/
    │   │   │   ├── BookRequest.java, BookResponse.java
    │   │   │   ├── StockRequest.java, StockTransferRequest.java
    │   │   │   └── RegisterRequest.java, CurrentUserResponse.java (new)
    │   │   ├── exception/
    │   │   │   ├── BookNotFoundException.java, DuplicateIsbnException.java
    │   │   │   └── TransactionLabCheckedException.java
    │   │   ├── model/Book.java, TransactionAudit.java
    │   │   │         AppUser.java (new)
    │   │   ├── repository/
    │   │   │   ├── BookRepository.java, TransactionAuditRepository.java
    │   │   │   └── AppUserRepository.java (new)
    │   │   ├── security/ (new)
    │   │   │   ├── AppUserDetailsService.java, SecurityConfig.java
    │   │   │   ├── RestAuthenticationEntryPoint.java, RestAccessDeniedHandler.java
    │   │   │   └── LocalAdminBootstrap.java
    │   │   ├── service/
    │   │   │   ├── BookService.java (modified), BookStockService.java
    │   │   │   ├── StockTransferService.java, TransactionAuditService.java
    │   │   │   └── RegistrationService.java (new)
    │   │   └── web/
    │   │       ├── BookController.java, TransactionLabController.java
    │   │       ├── GlobalExceptionHandler.java (modified)
    │   │       └── AuthController.java (new)
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    │           ├── V1__create_books.sql through V5__replace_author_index_for_paging.sql
    │           └── V6__create_app_users.sql (new)
    └── test/java/com/example/AppTest.java
```

### 1. `pom.xml`

Add exactly this dependency inside the existing `<dependencies>` element. Do not add a `<version>` or remove any current dependency:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 2. `src/main/resources/db/migration/V6__create_app_users.sql`

```sql
CREATE TABLE app_users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL,
    CONSTRAINT ck_app_users_role CHECK (role IN ('USER', 'ADMIN'))
);
```

There is no password in this migration. PostgreSQL's `book_app` remains the database login; `app_users` rows are application identities.

### 3. `src/main/java/com/example/bookcatalog/model/AppUser.java`

```java
package com.example.bookcatalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 16)
    private String role;

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, String role) {
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

    public String getRole() {
        return role;
    }
}
```

The entity has a hash getter so the adapter can authenticate, but no HTTP endpoint serializes this entity. The database check and server-side construction constrain roles; client JSON never sets one.

### 4. `src/main/java/com/example/bookcatalog/repository/AppUserRepository.java`

```java
package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

### 5. `src/main/java/com/example/bookcatalog/security/AppUserDetailsService.java`

```java
package com.example.bookcatalog.security;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
    }
}
```

`roles("ADMIN")` creates `ROLE_ADMIN`; do **not** store `ROLE_ADMIN` in the table and then call `roles(...)`, which would double-prefix it. `AppUser` and `UserDetails` have different jobs. During login the provider loads this data and uses the configured `PasswordEncoder`; a later request with a valid session usually restores the saved authentication without another user lookup.

### 6. `src/main/java/com/example/bookcatalog/security/RestAuthenticationEntryPoint.java`

```java
package com.example.bookcatalog.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Unauthorized\","
                + "\"status\":401,\"detail\":\"Authentication is required or credentials are invalid.\"}");
    }
}
```

The fixed response does not echo submitted credentials, usernames, exception text, or a session ID. A login failure calls this handler explicitly from the form-login failure handler.

### 7. `src/main/java/com/example/bookcatalog/security/RestAccessDeniedHandler.java`

```java
package com.example.bookcatalog.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Forbidden\","
                + "\"status\":403,\"detail\":\"This request is not permitted.\"}");
    }
}
```

This handler can handle CSRF failures as well as filter-chain authorization denials when wired through `exceptionHandling`. Do not infer the cause from the generic body alone.

### 8. `src/main/java/com/example/bookcatalog/security/SecurityConfig.java`

```java
package com.example.bookcatalog.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint entryPoint,
            RestAccessDeniedHandler deniedHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/auth/csrf", "/books", "/books/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                        .requestMatchers("/books", "/books/**", "/transaction-lab/**")
                                .hasRole("ADMIN")
                        .anyRequest().denyAll())
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login")
                        .successHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .failureHandler((request, response, exception) ->
                                entryPoint.commence(request, response, exception))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .csrf(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
```

The `DispatcherType.ERROR` rule lets normal application errors reach their error response instead of being masked by security's fallback. `formLogin` and `logout` install security filters, not controller methods. Spring Security builds the standard `DaoAuthenticationProvider` from the single `UserDetailsService` and `PasswordEncoder` beans. Keep the default stateful session policy and default session-backed CSRF repository; do not set `STATELESS` or disable CSRF. HTTP Basic is disabled so this exercise has one clear login mechanism.

### 9. `src/main/java/com/example/bookcatalog/dto/RegisterRequest.java`

```java
package com.example.bookcatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z][A-Za-z0-9._-]{2,79}",
                 message = "username must be 3-80 allowed characters")
        String username,

        @NotBlank
        @Size(min = 12, max = 72)
        String password) {
}
```

`@Size` counts Java characters, not UTF-8 bytes; the service below additionally enforces BCrypt's 72-byte limit. A production password policy also needs breached-password and rate-limit design; do not present this minimum length as a complete policy.

### 10. `src/main/java/com/example/bookcatalog/dto/CurrentUserResponse.java`

```java
package com.example.bookcatalog.dto;

import java.util.List;

public record CurrentUserResponse(String username, List<String> authorities) {
}
```

### 11. `src/main/java/com/example/bookcatalog/service/RegistrationService.java`

```java
package com.example.bookcatalog.service;

import com.example.bookcatalog.dto.RegisterRequest;
import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public RegistrationService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password exceeds the 72-byte BCrypt limit");
        }
        if (users.existsByUsername(request.username())) {
            throw duplicateUsername();
        }

        try {
            users.saveAndFlush(new AppUser(
                    request.username(), encoder.encode(request.password()), "USER"));
        } catch (DataIntegrityViolationException duplicateOrConstraint) {
            // The unique database constraint also protects concurrent registrations.
            throw duplicateUsername();
        }
    }

    private static ResponseStatusException duplicateUsername() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Username is unavailable");
    }
}
```

The catch produces a deliberately generic conflict; if more constraints are added later, distinguish which one failed rather than labeling every integrity error a duplicate. Validation is input correctness, not authorization. The transaction groups the read/write use case; Spring Security does not replace it.

### 12. `src/main/java/com/example/bookcatalog/web/AuthController.java`

```java
package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.CurrentUserResponse;
import com.example.bookcatalog.dto.RegisterRequest;
import com.example.bookcatalog.service.RegistrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegistrationService registration;

    public AuthController(RegistrationService registration) {
        this.registration = registration;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        registration.register(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal UserDetails principal) {
        List<String> authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new CurrentUserResponse(principal.getUsername(), authorities);
    }
}
```

The `CsrfToken` argument forces deferred token generation and gives clients the actual `headerName` to use. The JSON token is a secret; do not place it in URLs or application logs. `/auth/me` is protected before the controller, so its principal is present in the expected flow.

### 13. `src/main/java/com/example/bookcatalog/security/LocalAdminBootstrap.java`

```java
package com.example.bookcatalog.security;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalAdminBootstrap implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public LocalAdminBootstrap(
            AppUserRepository users,
            PasswordEncoder encoder,
            @Value("${BOOK_ADMIN_USERNAME:}") String username,
            @Value("${BOOK_ADMIN_PASSWORD:}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "local profile requires BOOK_ADMIN_USERNAME and BOOK_ADMIN_PASSWORD");
        }
        if (!username.matches("[A-Za-z][A-Za-z0-9._-]{2,79}")) {
            throw new IllegalStateException("local admin username is invalid");
        }
        if (password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "local admin password needs at least 12 characters and at most 72 UTF-8 bytes");
        }

        users.findByUsername(username).ifPresentOrElse(existing -> {
            if (!"ADMIN".equals(existing.getRole())) {
                throw new IllegalStateException(
                        "local admin username is already used by a non-admin account");
            }
            // Idempotent: do not overwrite an existing administrator's hash.
        }, () -> users.save(new AppUser(
                username, encoder.encode(password), "ADMIN")));
    }
}
```

For this exercise, activate the local profile only on the local machine and supply **disposable** credentials through environment variables. Do not commit the values. If the account exists as `ADMIN`, restarting does not reset its password. A real deployment should use an explicit privileged provisioning process, secret management, account lockout/rate limiting, and audit trails rather than this teaching bootstrap.

### 14. Method security in the existing `BookService.java`

Add this import among its current imports:

```java
import org.springframework.security.access.prepost.PreAuthorize;
```

Change only the existing `setStock` method to this complete method; its other methods and transactional behavior remain unchanged:

```java
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public Book setStock(long id, Integer stock) {
    Book book = repo.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    book.setStock(stock);
    return book;
}
```

The controller still calls this Spring-managed service proxy. The request matcher normally rejects a non-admin HTTP call first; the method guard matters for callers that reach the service through another Spring bean. It is not a replacement for the transaction.

### 15. Method-denial handling in existing `GlobalExceptionHandler.java`

Add this import:

```java
import org.springframework.security.access.AccessDeniedException;
```

Add this method **before** the existing catch-all `@ExceptionHandler(Exception.class)` method; keep the other handlers intact:

```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<Object> handleMethodAccessDenied(
        AccessDeniedException exception,
        WebRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN, "This request is not permitted.");
    problem.setTitle("Forbidden");
    return handleExceptionInternal(exception, problem,
            new HttpHeaders(), HttpStatus.FORBIDDEN, request);
}
```

Why this extra method? A filter-level URL denial happens **before** MVC and uses `RestAccessDeniedHandler`. A service `@PreAuthorize` denial can be raised **inside** MVC; the project's existing broad `@RestControllerAdvice` catch-all would otherwise turn it into 500. The specific handler preserves 403 for that path. Do not assume one handler automatically owns every possible security failure.

### 16. Relevant `application.yaml` and startup

Do not change the existing `application.yaml`: its PostgreSQL URL remains `jdbc:postgresql://127.0.0.1:5434/book_catalog_flyway`, the datasource user remains `book_app`, Flyway remains enabled at `classpath:db/migration`, and JPA remains `ddl-auto: validate`. No raw application-account password belongs in YAML. The existing `compose.yaml` still hosts PostgreSQL 17 on local port 5434; its `.env` credentials are **database** credentials, not `app_users` identities. The existing `spring.jpa.open-in-view: false` also stays in place.

Relevant excerpt of the existing, **unchanged** `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5434/book_catalog_flyway
    username: book_app
    password: replace-with-a-local-practice-password
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
  jackson:
    deserialization:
      fail-on-unknown-properties: true
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate

server:
  address: 127.0.0.1
  port: 8080
```

For a local run, set `SPRING_PROFILES_ACTIVE=local`, `BOOK_ADMIN_USERNAME` and `BOOK_ADMIN_PASSWORD` in your process environment, then start the app with the existing Maven/Boot workflow. Use a throwaway admin password of at least 12 characters and at most 72 UTF-8 bytes; do not paste it into documentation, Git, or logs. If you start without `local`, the bootstrap does not run; public registration still creates only `USER` accounts. Flyway V6 runs before JPA validation and before the local bootstrap inserts an admin.

### 17. Manual HTTP walkthrough

Use one cookie jar per identity; never mix `USER` and `ADMIN` sessions. In Postman, enable its cookie manager, send `GET http://127.0.0.1:8080/auth/csrf`, copy `token` and `headerName` from the JSON into the next unsafe request, and keep the same cookies. For `POST /auth/login`, choose **x-www-form-urlencoded**, not raw JSON. After login, call `/auth/csrf` again before a Book write or logout. The login/logout responses are 204, so confirm identity with `GET /auth/me`.

Equivalent curl sequence (shown for Bash/Git Bash/WSL; substitute only disposable local credentials):

```bash
BASE=http://127.0.0.1:8080
read -r -s -p 'Disposable USER password: ' USER_PASSWORD; echo

# Save the anonymous CSRF session cookie; copy token and headerName from JSON.
curl -i -c user.cookies -b user.cookies "$BASE/auth/csrf"
TOKEN='paste-token-from-the-previous-response'
HEADER='X-CSRF-TOKEN'  # use the returned headerName if different

# Public registration still needs CSRF. JSON has no role field.
curl -i -c user.cookies -b user.cookies -H "$HEADER: $TOKEN" \
  -H 'Content-Type: application/json' \
  --data "{\"username\":\"learner\",\"password\":\"$USER_PASSWORD\"}" \
  "$BASE/auth/register"

# Built-in form login reads URL-encoded fields; inspect Set-Cookie/JSESSIONID.
curl -i -c user.cookies -b user.cookies -H "$HEADER: $TOKEN" \
  --data-urlencode 'username=learner' \
  --data-urlencode "password=$USER_PASSWORD" "$BASE/auth/login"

curl -i -c user.cookies -b user.cookies "$BASE/auth/me"

# Refresh token after login, then copy the new token from the response.
curl -i -c user.cookies -b user.cookies "$BASE/auth/csrf"
TOKEN='paste-fresh-token-after-login'

# With valid CSRF, USER reaches authorization and gets 403.
curl -i -c user.cookies -b user.cookies -X PATCH \
  -H "$HEADER: $TOKEN" -H 'Content-Type: application/json' \
  --data '{"stock":7}' "$BASE/books/1"

# Logout uses POST plus the current CSRF token, not GET.
curl -i -c user.cookies -b user.cookies -X POST \
  -H "$HEADER: $TOKEN" "$BASE/auth/logout"
curl -i -b user.cookies "$BASE/auth/me"
```

For `ADMIN`, start a separate `admin.cookies` jar, get its CSRF token, log in with the environment-provisioned username/password, refresh the token, then create a Book:

```bash
curl -i -c admin.cookies -b admin.cookies "$BASE/auth/csrf"
# Set ADMIN_TOKEN to the token just returned; log in using that header.
curl -i -c admin.cookies -b admin.cookies -H "X-CSRF-TOKEN: $ADMIN_TOKEN" \
  --data-urlencode "username=$BOOK_ADMIN_USERNAME" \
  --data-urlencode "password=$BOOK_ADMIN_PASSWORD" "$BASE/auth/login"
curl -i -c admin.cookies -b admin.cookies "$BASE/auth/csrf"
# Replace ADMIN_TOKEN with the fresh post-login token.
curl -i -c admin.cookies -b admin.cookies -H "X-CSRF-TOKEN: $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  --data '{"isbn":"SEC-LAB-1","title":"Security Lab","author":"Student","price":12.50,"stock":5}' \
  "$BASE/books"
```

`ADMIN` receives 201 for a valid new Book; reusing its ISBN returns the existing application's 409. The sample `DELETE /books/{id}` is **not** a success test: once authorized and CSRF-valid, it reaches the intentional checked exception, returns the existing handler's 500, and rolls back. `POST /transaction-lab/transfers` is also admin-only; its `SUCCESS_REQUIRED` scenario commits, while the deliberately failing scenarios have the transaction behavior taught in the prior lab. Keep source/destination Book IDs and stock valid when testing it.

To demonstrate 401 cleanly, call `GET /auth/me` with no cookie. To demonstrate 403 from role authorization, send an authenticated `USER` write **with** its fresh CSRF token. To demonstrate 403 from CSRF, omit the token on an unsafe request. A malformed Book body or missing Book ID may instead produce 400 or 404 **after** security succeeds; these are application outcomes, not authorization outcomes.

### 18. Verify PostgreSQL and Flyway

Run these read-only SQL checks using the existing local PostgreSQL database account:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'app_users'
ORDER BY ordinal_position;

SELECT username, role,
       left(password_hash, 7) AS hash_prefix,
       length(password_hash) AS hash_length
FROM app_users
ORDER BY username;
```

Expect `V6` recorded as successful; one `ADMIN` from local bootstrap and a `USER` after registration; and BCrypt-looking prefixes with approximately 60-character hashes, not raw passwords. The query avoids displaying full hashes; do not print real passwords. For the intentional delete, verify the Book row remains after the 500 rollback. For a successful admin create, verify the new Book row persists. These observations connect security to Flyway, JPA, transactions, JDBC, HikariCP, and PostgreSQL without confusing an application role with the `book_app` database login.

### Filled verification matrix (compare with your predictions)

| Scenario | Expected result |
|---|---|
| Anonymous `GET /books` | 200 when application/database are healthy |
| Anonymous `GET /auth/me` | 401 |
| Anonymous unsafe call without CSRF | 403, rejected by CSRF before auth decision |
| Anonymous valid registration with CSRF | 201 |
| Duplicate registration with CSRF | 409 |
| Invalid login password with CSRF | 401 |
| Valid login with CSRF | 204 and usable session cookie |
| `USER` calls `/auth/me` | 200 with safe username and `ROLE_USER` |
| `USER` writes Book with fresh CSRF | 403 from role policy |
| `ADMIN` creates valid new Book with fresh CSRF | 201 |
| `ADMIN` runs valid transaction-lab `SUCCESS_REQUIRED` | 204; other scenarios may intentionally fail |
| `ADMIN` deletes existing Book with fresh CSRF | 500 from intentional checked exception; deletion rolls back |
| Old session after logout at `/auth/me` | 401 |
| Invalid or missing CSRF on a write | 403 |

## Common mistakes and interview checks

- `permitAll()` grants URL access; it does not skip `CsrfFilter`.
- A missing CSRF token on anonymous `POST` can be **403**, not 401. Use a safe protected `GET`, or a valid token on the unsafe request, to test the authentication boundary.
- Login is form-encoded because the built-in form filter parses form parameters. A JSON login endpoint would need separate, careful authentication and context/session persistence code.
- Refresh CSRF after login/logout. Preserve the cookie jar throughout each flow.
- `JSESSIONID` is a session pointer, not a JWT and not the whole user object.
- `UserDetailsService` loads credentials; `DaoAuthenticationProvider` and `PasswordEncoder` verify them.
- `hasRole("ADMIN")` looks for `ROLE_ADMIN`, while the database stores `ADMIN`.
- `@PreAuthorize` and `@Transactional` are different interceptors with different jobs; self-invocation bypasses proxy advice.
- Filter-chain denial and MVC method-security denial can reach different error handlers. In this project, the catch-all advice necessitates a specific method-denial handler.
- CORS is a browser cross-origin rule; it is not a CSRF substitute or a permission system.
- A 500 from the existing `delete()` is not evidence that security failed; the request passed security and the transaction lab's deliberate exception ran.

Can you now explain, without looking, how a browser's second authenticated Book write travels through the session-backed security chain, why it needs both cookie and CSRF header, and where its transaction starts?
