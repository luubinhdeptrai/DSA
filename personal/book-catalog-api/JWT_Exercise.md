# JWT exercise — convert the existing Book Catalog Session login into JWT bearer authentication

> **Build one focused JWT lab, not an identity platform.** Attempt all tasks before reading the cumulative reference solution. The purpose is to *see what stays the same*, and exactly what changes when `JSESSIONID` is replaced by `Authorization: Bearer <jwt>`.

**Source baseline:** the attached `Spring_Security_Exercise(2).md` and `Spring_Security_Essential_Concepts(3).md`. They document `Flyway/book-catalog-api` using Java 21, Spring Boot **4.1.1**, Spring Security **7.1.1**, PostgreSQL 17, Flyway V1–V6, and the package `com.example.bookcatalog`. **Important limit:** the **actual current source repository was not attached**. This is a fully worked *reference adaptation of the documented Session baseline*, not a claim to have compiled against your live code. First compare the documented classes with your repository; integrate rather than blindly overwrite later changes.

**Companion concepts:** [JWT_Essential_Concepts.md](JWT_Essential_Concepts.md)  
**Local-only choice:** RSA **RS256**, separate PEM private/public keys, **15-minute** access JWTs, trusted issuer `book-catalog-api-local`, audience `book-catalog-api`; bearer in `Authorization` header; **no auth cookies, no refresh token, no blacklist**. Spring Security's OAuth2 Resource Server machinery verifies JWTs; this application issues them during ordinary first-party username/password login. It is *not* an OAuth2 Authorization Server.

## The change map — read this first

| Concern | Keep or replace? | Existing Session lab | JWT lab |
|---|---|---|---|
| `AppUser`, `AppUserRepository`, `AppUserDetailsService` | **KEEP** | PostgreSQL identity + BCrypt hash | Same identity + BCrypt hash |
| `RegistrationService`, `RegisterRequest` | **KEEP** | Register only `USER` | Still register only `USER` |
| `PasswordEncoder`, `DaoAuthenticationProvider` | **KEEP** | Password check | Same password check |
| `@PreAuthorize`, Book/transfer role policy | **KEEP** | `ROLE_ADMIN` | `ROLE_ADMIN` |
| `SecurityContext`, `Authentication` | **KEEP concepts** | Loaded from session | Built from trusted JWT per request |
| `formLogin` at `/auth/login` | **REPLACE** | Form filter; URL-encoded; 204 | JSON controller; authenticated result → token |
| `JSESSIONID` | **REMOVE as auth continuity** | Sent on later calls | No session needed; bearer on every protected call |
| Session CSRF endpoint/config | **REPLACE** | Cookie-backed CSRF token | Disable CSRF only for this header-only/no-auth-cookie lab |
| Framework logout filter at `/auth/logout` | **REMOVE** | Invalidates server session | Client discards token; copies survive until expiry |
| `/auth/me` principal extraction | **ADAPT** | `UserDetails` | JWT-backed `Authentication.getName()` |
| `JwtEncoder`, `JwtDecoder`, `TokenService`, converter | **ADD** | Not used | Sign, verify, issue, map roles |

### Confirm the baseline before editing

The reference documents list:

```text
src/main/java/com/example/bookcatalog/
 ├─ security/SecurityConfig.java
 ├─ security/AppUserDetailsService.java
 ├─ security/LocalAdminBootstrap.java
 ├─ security/RestAuthenticationEntryPoint.java
 ├─ security/RestAccessDeniedHandler.java
 ├─ model/AppUser.java
 ├─ repository/AppUserRepository.java
 ├─ service/RegistrationService.java
 ├─ service/BookService.java
 ├─ web/AuthController.java
 ├─ web/GlobalExceptionHandler.java
 ├─ dto/RegisterRequest.java
 └─ dto/CurrentUserResponse.java
src/main/resources/db/migration/V6__create_app_users.sql
```

These are **documented reference paths**, not verified current files. Check your actual `pom.xml`, package names, Flyway migrations, controller endpoints, security beans, annotations, and tests; if your source differs, adapt the code. **Do not rewrite V1–V6** or introduce a second database. If you already implemented the Session exercise, preserve its existing data, stock/transaction behavior, method-security fix in `GlobalExceptionHandler`, and local admin bootstrap. You can do this work on a separate Git branch to compare Session and JWT behavior cleanly.

### API contract

```http
POST /auth/login
Content-Type: application/json

{"username":"alice","password":"<your local test password>"}
```

```http
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-store

{"accessToken":"<jwt>","tokenType":"Bearer","expiresIn":900}
```

```http
GET /auth/me
Authorization: Bearer <jwt>

HTTP/1.1 200 OK
Content-Type: application/json

{"username":"alice","authorities":["ROLE_USER"]}
```

**Policy preserved from the Session lab:** public `GET /books`, `GET /books/{id}`, and `POST /auth/register` and `POST /auth/login`; authenticated `GET /auth/me`; `ADMIN` for Book mutations and `/transaction-lab/**`; everything else denied by default. Permit `DispatcherType.ERROR` for normal error handling. The old `/auth/csrf` and `/auth/logout` session flow are deliberately **not part of this JWT API**.

## Part A — 26 tasks (no full solution yet)

### 1. Inspect the existing Session implementation

Open `pom.xml`, `SecurityConfig`, `AuthController`, `AppUser`, `AppUserRepository`, `AppUserDetailsService`, `RegistrationService`, `LocalAdminBootstrap`, `BookService`, and `GlobalExceptionHandler`. Find form login, CSRF, session logout, role rules, and the current tests. Verify latest Flyway history rather than assuming migration number.

- **Objective:** Locate the exact code responsible for session continuity and credential verification.
- **Concept:** Security is a composition of reusable and replaceable parts.
- **What to implement:** Make a source inventory; make **no code changes** yet.
- **Starter code:** Search for `.formLogin(`, `.logout(`, `CsrfToken`, `@PreAuthorize`, `UserDetailsService`.
- **Hints:** The prior reference config uses `POST /auth/login` as a *filter* endpoint and `POST /auth/logout` as a filter endpoint.
- **How to verify:** Point to the actual `SecurityFilterChain`, `V6__create_app_users.sql`, method guard and `@RestControllerAdvice` catch-all.
- **Common mistakes:** Assuming the teaching reference is the latest repository; forgetting `/transaction-lab/transfers`.
- **Explanation prompt:** What creates the authenticated `Authentication` today, and where is it saved?
- **Session comparison:** This entire task is a baseline snapshot; **no JWT changes yet**.

### 2. Draw all four request flows

Draw Session login, later session call, JWT login and later bearer request on paper.

- **Objective:** See the fork point immediately after successful password verification.
- **Concept:** Reuse credential verification; change authentication continuity.
- **What to implement:** Four small diagrams, no Java.
- **Starter code:** `AuthenticationManager → DaoAuthenticationProvider → UserDetailsService → PasswordEncoder`.
- **Hints:** Session next uses `HttpSession`; JWT next uses `JwtEncoder` and later `JwtDecoder`.
- **How to verify:** Explain why Session later requests usually skip password lookup and JWT later requests usually skip it too.
- **Common mistakes:** Assuming JWT replaces `AuthenticationManager` or `SecurityContext`.
- **Explanation prompt:** Which single arrow differs **after** `AuthenticationManager.authenticate(...)` succeeds?
- **Session comparison:** Before `Authentication → session`; now `Authentication → signed JWT`.

> **STOP BARRIER 1:** Before writing code, explain the divergence point and list at least five existing classes/beans that should **not** be rewritten.

### 3. Add JWT support

Keep the existing Spring Security/JPA/Flyway dependencies; add Spring Boot's OAuth2 Resource Server starter, letting the parent manage versions.

- **Objective:** Reuse established bearer/JWT infrastructure.
- **Concept:** Resource Server handles bearer filtering and trusted token validation.
- **What to implement:** Add `spring-boot-starter-oauth2-resource-server` with **no explicit version**.
- **Starter code:** `<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>`.
- **Hints:** Do **not** add a second full OAuth2 Authorization Server.
- **How to verify:** Maven resolves `spring-security-oauth2-resource-server` and `spring-security-oauth2-jose`.
- **Common mistakes:** Copying mismatched JWT libraries or writing a custom parser.
- **Explanation prompt:** Why does installing resource-server support not automatically create a `/auth/login` token endpoint?
- **Session comparison:** `spring-boot-starter-security` remains; the new starter adds bearer-JWT support.

### 4. Generate and protect a **local-only** RSA key pair

Create a private PKCS#8 PEM and public X.509 PEM in a Git-ignored `local-secrets/` directory. **Never commit or log the private key.**

- **Objective:** Understand the sign/verify key boundary.
- **Concept:** `RS256` uses private signing and public verification keys.
- **What to implement:** Use an RSA key generator (for example OpenSSL); keep the files outside Git.
- **Starter code:** `openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out local-secrets/jwt-private.pem`.
- **Hints:** Then extract public key with `openssl pkey -in ... -pubout -out ...`.
- **How to verify:** Verify PEM headers `BEGIN PRIVATE KEY` and `BEGIN PUBLIC KEY`; do not paste key bytes anywhere.
- **Common mistakes:** Committing PEMs; confusing an SSH public key with X.509 SubjectPublicKeyInfo PEM.
- **Explanation prompt:** Can a party holding **only** the public key mint an accepted RS256 JWT?
- **Session comparison:** A session ID needs server-side lookup; JWT verifier needs a trusted public key.

### 5. Configure `JwtEncoder`

Publish a Spring bean that signs RS256 JWTs with your local private key using `NimbusJwtEncoder`.

- **Objective:** Isolate the token-signing dependency.
- **Concept:** A producer encodes trusted claims and signs them.
- **What to implement:** One `JwtEncoder` bean backed by the private/public RSA pair.
- **Starter code:** `NimbusJwtEncoder.withKeyPair(publicKey, privateKey).build()`.
- **Hints:** The Spring 7.1 builder exists; no home-made JWKSource required for the simple lab.
- **How to verify:** An issued token has an `RS256` JOSE header.
- **Common mistakes:** Using public key to sign; returning key contents in errors.
- **Explanation prompt:** Who defines, creates, and calls the encoder?
- **Session comparison:** It replaces the *creation of server-side authenticated continuity*, not password verification.

### 6. Configure `JwtDecoder` + claim validators

Publish a decoder that checks the RSA public-key signature, standard time validity, exact issuer, intended audience, and **required** `exp`/`sub` claims.

- **Objective:** Enforce a known token trust policy.
- **Concept:** Decoding alone is not permission; every necessary validation must succeed.
- **What to implement:** `NimbusJwtDecoder.withPublicKey(publicKey).build()` plus composed Spring validators.
- **Starter code:** `JwtValidators.createDefaultWithIssuer("book-catalog-api-local")`.
- **Hints:** Add `JwtClaimValidator` for audience membership and required claims; an absent `exp` must fail in this exercise.
- **How to verify:** A locally minted, valid token decodes; wrong issuer/audience/expiry fails.
- **Common mistakes:** Signature-only validation; accidentally accepting a signed non-expiring access token.
- **Explanation prompt:** Why can an authentic, untampered token still get `401`?
- **Session comparison:** Server checks `JSESSIONID` in its session store vs checks signed JWT and policy.

### 7. Create `TokenService`

Write a small service that takes **successfully authenticated** `Authentication`, derives the username and authorities, and builds claims (`sub`, `iss`, `aud`, `iat`, `exp`, `roles`).

- **Objective:** Issue minimum-privilege, short-lived access tokens.
- **Concept:** Only trusted post-authentication data becomes roles.
- **What to implement:** `AccessTokenResponse issue(Authentication authentication)` calling `JwtEncoder`.
- **Starter code:** `JwtClaimsSet.builder().subject(authentication.getName()).issuedAt(now)...`.
- **Hints:** Map `ROLE_USER` → `USER` and `ROLE_ADMIN` → `ADMIN`; 900 seconds.
- **How to verify:** Decoded claims show correct username/role/issuer/audience/time but no password/hash.
- **Common mistakes:** Taking roles from `LoginRequest`; making the token perpetual.
- **Explanation prompt:** Where did `roles` come from, and how are they protected?
- **Session comparison:** Previously `Authentication` was saved in an HTTP session; now trusted identity data is signed into a token.

### 8. Replace form login with JSON `/auth/login`

Remove `.formLogin(...)`, preserve the `UserDetailsService`, BCrypt bean and standard DAO path. Add a JSON MVC endpoint that calls `AuthenticationManager.authenticate(...)` and then `TokenService.issue(...)`.

- **Objective:** Keep the same username/password verification while changing the response.
- **Concept:** Login is still a credential-authentication operation, not a manual database comparison.
- **What to implement:** `LoginRequest`, `AccessTokenResponse`, new login method, explicit `AuthenticationManager` bean if needed.
- **Starter code:** `UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())`.
- **Hints:** `AuthenticationException` thrown inside MVC needs a specific generic `401` handler; previous catch-all advice may otherwise mask it as `500`.
- **How to verify:** Valid credentials produce JSON JWT; bad password produces generic `401`, not 500, and no token.
- **Common mistakes:** Two components handling `/auth/login` simultaneously; directly calling `passwordEncoder.matches(...)` in controller.
- **Explanation prompt:** Is the password-check chain the same as in Session login?
- **Session comparison:** Before form-filter URL-encoded login → `204/JSESSIONID`; now controller JSON login → `200/accessToken`.

### 9. Inspect a disposable JWT

Use a **local test token**, not a real user's token. Decode its first two Base64URL segments with local tooling.

- **Objective:** Observe readable claims and integrity protection.
- **Concept:** Encoded is not encrypted.
- **What to implement:** A safe local decode exercise only.
- **Starter code:** `String[] parts = jwt.split("\\.");` (demonstration only; never a validator).
- **Hints:** Do not paste bearer tokens into external JWT websites or logs.
- **How to verify:** `sub`, `roles`, `iss`, `aud`, `exp` are visible; password/hash absent.
- **Common mistakes:** Treating Base64URL parsing as token authentication.
- **Explanation prompt:** What would stop you from editing `"roles"` from `USER` to `ADMIN`?
- **Session comparison:** You could not decode roles from an opaque `JSESSIONID`.

### 10. Configure stateless security

Use `SessionCreationPolicy.STATELESS` so Spring Security does not use an HTTP session for its security context.

- **Objective:** Make the bearer token the only authentication continuity mechanism.
- **Concept:** Stateless **auth** does not mean a stateless database/application.
- **What to implement:** Session policy, disable form login, disable HTTP Basic and the old logout filter.
- **Starter code:** `.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`.
- **Hints:** A stray cookie should not grant access to `/auth/me`.
- **How to verify:** A call with old session cookie but no JWT gets `401`.
- **Common mistakes:** Believing `STATELESS` automatically verifies tokens.
- **Explanation prompt:** Where is the current `SecurityContext` stored between JWT requests?
- **Session comparison:** Remove session continuity; each call rebuilds auth from a bearer token.

> **STOP BARRIER 2:** Session authenticated continuity lives **where**? JWT authenticated continuity lives **where**? Does `STATELESS` remove your user database?

### 11. Enable Spring's bearer JWT authentication

Configure the resource-server JWT DSL inside your `SecurityFilterChain`.

- **Objective:** Let framework filters interpret `Authorization: Bearer`.
- **Concept:** Spring supplies bearer extraction, decoder/provider, and context construction.
- **What to implement:** `.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> ...))`.
- **Starter code:** `jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)`.
- **Hints:** Use the configured trusted decoder and validators automatically.
- **How to verify:** `/auth/me` with a valid bearer token reaches controller.
- **Common mistakes:** Building a parallel custom JWT servlet filter for a problem Spring already solves.
- **Explanation prompt:** Which component owns the raw bearer header in the real request path?
- **Session comparison:** `SecurityContext` restoration from server session → bearer filter and decoder reconstruct context.

### 12. Map JWT roles to Spring authorities

For a signed claim `"roles":["ADMIN"]`, map it into the usual `ROLE_ADMIN` granted authority.

- **Objective:** Reuse existing `.hasRole("ADMIN")` and `@PreAuthorize` rules unchanged.
- **Concept:** Token claims are **not** Java authorities until converted.
- **What to implement:** `JwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles")` and prefix `ROLE_` inside `JwtAuthenticationConverter`.
- **Starter code:** `rolesConverter.setAuthorityPrefix("ROLE_")`.
- **Hints:** Default `SCOPE_` mapping is not this project's custom roles model.
- **How to verify:** USER token returns `ROLE_USER`; ADMIN returns `ROLE_ADMIN` via `/auth/me`.
- **Common mistakes:** Double `ROLE_ROLE_ADMIN` prefix; assuming automatic custom roles parsing.
- **Explanation prompt:** Who turns claims into `GrantedAuthority` objects?
- **Session comparison:** Session roles came from `UserDetails`; JWT roles come from the verified claim converter.

### 13. Adapt `/auth/me`

The existing controller's `@AuthenticationPrincipal UserDetails` is tailored to Session/DAO login. JWT authentication normally supplies a `Jwt` principal, not `UserDetails`.

- **Objective:** Expose a safe current-user DTO without exposing token secrets.
- **Concept:** `Authentication.getName()` resolves the verified JWT subject by default.
- **What to implement:** Accept `Authentication`, return `CurrentUserResponse(username, authorities)`.
- **Starter code:** `authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()`.
- **Hints:** Do not return `Jwt`, `Authentication`, or `AppUser` as JSON wholesale.
- **How to verify:** Matching usernames and roles for each account; no password/hash or access token in response.
- **Common mistakes:** Leaving a nullable `UserDetails` parameter and dereferencing it.
- **Explanation prompt:** Why did the principal Java type change but the endpoint's JSON contract need not change?
- **Session comparison:** Same `SecurityContext` concept; different concrete principal implementation.

### 14. Preserve the endpoint policy

Keep Book reads public, registration/login public, `/auth/me` authenticated, and writes ADMIN-only. Deny unknown routes and protect the transaction lab.

- **Objective:** Make the JWT migration change **authentication**, not privileges.
- **Concept:** Least privilege and first-matching rule remain.
- **What to implement:** URL matchers in explicit order; keep existing `@PreAuthorize` on stock mutation.
- **Starter code:** `.requestMatchers("/books", "/books/**", "/transaction-lab/**").hasRole("ADMIN")`.
- **Hints:** Permit `DispatcherType.ERROR` so application errors aren't hidden by fallback.
- **How to verify:** GET books unauthenticated succeeds; mutations require ADMIN.
- **Common mistakes:** An accidentally broad `/auth/**.permitAll()` or `anyRequest().permitAll()`.
- **Explanation prompt:** What policy remains identical despite replacing sessions?
- **Session comparison:** Same role/authorization decision against a newly reconstructed `Authentication`.

### 15. Missing bearer token

Call `GET /auth/me` with no `Authorization` header.

- **Objective:** Differentiate authentication failure from application `404`.
- **Concept:** A protected endpoint requires an accepted authentication credential.
- **What to implement:** No new Java; run HTTP check.
- **Starter code:** `curl.exe -i http://127.0.0.1:8080/auth/me`.
- **Hints:** Expect `401`; response often contains a bearer `WWW-Authenticate` challenge.
- **How to verify:** Controller breakpoint does not fire.
- **Common mistakes:** Calling this a `403` missing-role scenario.
- **Explanation prompt:** Which filter/security component responds before MVC?
- **Session comparison:** Missing session cookie and missing bearer token both lack trusted identity.

### 16. Malformed and tampered token

Use one disposal-only token. Change the payload character without re-signing, then try `/auth/me`.

- **Objective:** Witness signature verification in action.
- **Concept:** Readable claims are not trusted claims.
- **What to implement:** Only an HTTP experiment; avoid writing crypto validation code.
- **Starter code:** `Authorization: Bearer <modified-token>`.
- **Hints:** A malformed token may also fail parsing before signature verification; distinguish the two cases.
- **How to verify:** Invalid bearer returns `401`.
- **Common mistakes:** Changing `ADMIN` in a local decoded text representation without constructing the changed compact JWT correctly.
- **Explanation prompt:** Could the attacker re-sign it with **only** your public key?
- **Session comparison:** A forged/unknown session ID also fails—but by a different mechanism.

### 17. Expired token experiment

Temporarily set the **local** access-token TTL to a few seconds, log in, wait past expiry, then make a protected request. Restore 900 seconds.

- **Objective:** Verify time enforcement separately from signature validity.
- **Concept:** A correctly signed token can be invalid due to `exp`.
- **What to implement:** Temporary local config change and HTTP check.
- **Starter code:** `app.jwt.access-token-ttl-seconds: 3` (**local test only**).
- **Hints:** Account for small validator clock tolerance; if your configuration allows skew, wait beyond TTL + skew or use a crafted test token.
- **How to verify:** Valid JWT at first; `401` after allowed time window.
- **Common mistakes:** Testing immediately after expiry with an allowed clock skew, then assuming validation is disabled.
- **Explanation prompt:** Why does expiry override a still-correct signature?
- **Session comparison:** Different expiration mechanism; same goal of bounded credential lifetime.

### 18. Wrong issuer and audience

Validate tokens with deliberately wrong `iss` or `aud` **in a local unit/integration test**. Don't expose any endpoint for arbitrary token minting.

- **Objective:** Demonstrate contextual trust beyond signature validity.
- **Concept:** `JwtDecoder` enforces expected issuer and resource.
- **What to implement:** Directly encode a test token with wrong claims, then assert decoder rejection.
- **Starter code:** `assertThrows(JwtException.class, () -> jwtDecoder.decode(token))`.
- **Hints:** The private key is trusted, yet wrong claims must still be rejected. Example test shown after the STOP barrier.
- **How to verify:** Both wrong-claim scenarios fail under the same trusted key.
- **Common mistakes:** Changing app issuer configuration in tandem with the token so issuer always matches.
- **Explanation prompt:** Why do two services not automatically accept each other's signed tokens?
- **Session comparison:** Session store decides whether an ID belongs to this application; JWT validators decide token target and issuer.

### 19. Distinguish `401` from `403` with roles

Authenticate as USER and ADMIN. Try a valid Book create with both.

- **Objective:** Show successful authentication does not imply ADMIN authorization.
- **Concept:** Signature/claim validity first; role policy second.
- **What to implement:** Two bearer-token requests; no extra roles database.
- **Starter code:** `POST /books` with `Authorization: Bearer <user-or-admin-jwt>`.
- **Hints:** Use the exact Book DTO valid in your current project; test with one role at a time.
- **How to verify:** USER receives `403`; ADMIN reaches the application (normally `201` for valid create).
- **Common mistakes:** Testing with malformed Book JSON and blaming security for a 400.
- **Explanation prompt:** Why can both tokens pass signature verification but reach different authorization decisions?
- **Session comparison:** Same `hasRole("ADMIN")` decision after changing how roles enter `Authentication`.

### 20. Explain CSRF rather than blindly disabling it

Compare a browser cookie-authenticated Session write with a manually attached header-only JWT write.

- **Objective:** Choose CSRF policy based on credential transport.
- **Concept:** Automatic credential attachment enables CSRF scenarios.
- **What to implement:** Disable `csrf` only in this JWT lab; give a written threat-model explanation.
- **Starter code:** `.csrf(AbstractHttpConfigurer::disable)`.
- **Hints:** A cookie-carried JWT can reintroduce CSRF; CORS is not equivalent defense.
- **How to verify:** Header-authenticated ADMIN writes work without `/auth/csrf`; unauthorized requests remain rejected.
- **Common mistakes:** Writing “JWT means CSRF is always unnecessary.”
- **Explanation prompt:** Why was CSRF needed with `JSESSIONID`; what changes for explicitly supplied `Authorization`; what if JWT is stored in cookie?
- **Session comparison:** Session cookie ambient auth vs JWT lab manual header auth.

### 21. Confirm there is no session restoration

Call `/auth/me` twice with bearer headers, then call again with **only** an old `JSESSIONID` (if you have one).

- **Objective:** Understand per-request authentication reconstruction.
- **Concept:** The JWT is verified each request.
- **What to implement:** HTTP experiment; optionally step through debugger.
- **Starter code:** `GET /auth/me` with/without `Authorization`.
- **Hints:** A token-bearing request should not need a `Set-Cookie: JSESSIONID` authentication response.
- **How to verify:** Both bearer requests succeed; cookie-only protected request fails `401`.
- **Common mistakes:** Believing the first bearer request secretly logs in the client for later cookie-only requests.
- **Explanation prompt:** What does Spring reconstruct on each request and clear afterward?
- **Session comparison:** Session restores context from server state; JWT rebuilds it from validated signed claims.

> **STOP BARRIER 3:** Can an untampered RS256 JWT fail? Give at least **four** independent validation reasons.

### 22. Change a test user's role in PostgreSQL

Use a **disposable, isolated** local ADMIN account and mint an ADMIN JWT. Change that account's DB role to USER, then retry with the **old** JWT. Restore its original role.

- **Objective:** Observe stale authority claims.
- **Concept:** Issued JWTs assert a snapshot, not a live DB read.
- **What to implement:** One local SQL `UPDATE`, two API calls, then revert.
- **Starter code:** `UPDATE app_users SET role='USER' WHERE username='<disposable_admin>';`.
- **Hints:** Issue a NEW JWT after the DB change to compare old vs new authority; don't reuse shared/admin production accounts.
- **How to verify:** Old ADMIN JWT may still authorize until expiry under this design; new login obtains USER role.
- **Common mistakes:** Believing a PostgreSQL update mutates an already-signed token.
- **Explanation prompt:** Is this *also* a possible Session problem?
- **Session comparison:** Stored session authorities may become stale too; invalidating sessions offers a different response mechanism.

### 23. Understand JWT “logout” by experiment

Copy a local JWT string into two temporary variables; delete **one** variable; use the remaining copy to call `/auth/me`.

- **Objective:** See why client discard is not revocation.
- **Concept:** Possession of a still-valid bearer token enables replay.
- **What to implement:** No `/auth/logout` endpoint and no blacklist.
- **Starter code:** `$copy = $token; Remove-Variable token` (PowerShell).
- **Hints:** Do not persist copies to a file or commit them; restore login only if needed.
- **How to verify:** The copied JWT still works until its validity window ends.
- **Common mistakes:** Telling users token deletion revokes all stolen copies.
- **Explanation prompt:** What extra state/mechanism would immediate server-side revocation require?
- **Session comparison:** Existing Session logout invalidated the server-side entry immediately.

> **STOP BARRIER 4:** Explain why deleting your token locally does **not** delete every usable copy. Propose two production revocation strategies and identify the costs.

### 24. Refresh tokens: conceptual review only

Draw access-token renewal but implement **no** refresh endpoint, refresh DB table or rotation mechanism.

- **Objective:** Know the boundary of this learning project.
- **Concept:** Separate API authorization credential from session-renewal credential.
- **What to implement:** One short diagram or paragraph, not code.
- **Starter code:** `refresh token → refresh endpoint → new short-lived access token`.
- **Hints:** Refresh-token theft can be more serious because it enables repeated access-token issuance.
- **How to verify:** Answer why making the access token valid for 30 days is a poor shortcut.
- **Common mistakes:** Sending refresh tokens to `/books` or treating them as necessarily JWTs.
- **Explanation prompt:** Why might refresh rotation/revocation require server-side state?
- **Session comparison:** Session lifetime extension vs separate access-credential renewal.

### 25. Trace the complete login, identifying database work

Use the debugger or draw the call path from `LoginController` to the returned JWT.

- **Objective:** Separate the password-authentication DB path from pure token signing.
- **Concept:** DAO provider uses the database; encoder normally does not.
- **What to implement:** A labelled flow with DB/non-DB steps.
- **Starter code:** `controller → manager → provider → UserDetailsService → repository → Hibernate → Hikari → PostgreSQL`.
- **Hints:** Add `PasswordEncoder.matches`, `Authentication`, `TokenService`, `JwtEncoder`.
- **How to verify:** You can point to every relevant class file/bean and explain the return values.
- **Common mistakes:** Believing `JwtEncoder` reads users from PostgreSQL.
- **Explanation prompt:** What exactly is passed into `TokenService`?
- **Session comparison:** Same verification; different post-success persistence/response step.

### 26. Trace one later protected request

Use `Authorization: Bearer <jwt>` to reach a valid Book mutation, and trace **filter → validation → mapping → context → authorization → MVC → service**.

- **Objective:** Reconstruct Spring's runtime call chain without conflating it with login.
- **Concept:** A resource-server bearer request usually doesn't call the DAO provider.
- **What to implement:** Diagram and optional debugger breakpoints; no new code.
- **Starter code:** `BearerTokenAuthenticationFilter → JwtAuthenticationProvider → JwtDecoder → JwtAuthenticationConverter → SecurityContext`.
- **Hints:** Mark where signature/claims are checked, role is mapped, and `hasRole` is evaluated.
- **How to verify:** Name the first business-layer method reached only after authorization.
- **Common mistakes:** Assuming `AuthenticationManager` always invokes `DaoAuthenticationProvider` for a JWT; a manager can use different providers for different authentication tokens.
- **Explanation prompt:** Which exact step corresponds to session restoration in the old flow?
- **Session comparison:** `JSESSIONID → restore` becomes `JWT → verify/rebuild`; subsequent authorization and business logic stay familiar.

## Final manual-verification matrix (record **actual** results)

| Scenario | Expected in this reference design | Actual |
|---|---|---|
| Public `GET /books` without token | Allowed (normally 200) | ☐ |
| `POST /auth/register` with valid USER data, no CSRF | 201 | ☐ |
| JSON login with correct password | 200 + JWT | ☐ |
| Login with wrong password | 401 (generic), no token | ☐ |
| `GET /auth/me` without token | 401 | ☐ |
| `GET /auth/me` with valid USER token | 200 + `ROLE_USER` | ☐ |
| Book create with valid USER JWT | 403 | ☐ |
| Book create with valid ADMIN JWT and valid DTO | Reaches MVC, normally 201 | ☐ |
| Tampered/malformed JWT | 401 | ☐ |
| Expired JWT (past any allowed skew) | 401 | ☐ |
| Valid signature but wrong `iss` | Rejected by decoder / 401 at HTTP boundary | ☐ |
| Valid signature but wrong `aud` | Rejected by decoder / 401 at HTTP boundary | ☐ |
| Cookie-only `JSESSIONID` without bearer | 401 on protected route | ☐ |
| Old ADMIN JWT after DB role lowered to USER | May still pass until expiry (this design) | ☐ |
| Old copied token after local “logout” | Still valid until expiry/revocation | ☐ |
| Existing DELETE transaction rollback lesson | Preserved *after* ADMIN authorization | ☐ |
| Transaction-lab mutation with USER JWT | 403 | ☐ |

---

==================================================
STOP — ATTEMPT THE EXERCISE BEFORE READING SOLUTION
==================================================

The material below is a **complete reference adaptation for the documented baseline**. Compare your actual code before replacing existing files. No full OAuth2 Authorization Server, refresh store, denylist, or custom JWT filter is needed.

## Complete Reference Solution

### 0. File ownership / change inventory

**Keep unchanged if your actual project matches the documented Session version:** `AppUser.java`, `AppUserRepository.java`, `AppUserDetailsService.java`, `RegisterRequest.java`, `RegistrationService.java`, `CurrentUserResponse.java`, `LocalAdminBootstrap.java`, existing Book/transaction controllers/services/repositories, `BookService` method-level `@PreAuthorize`, migrations V1–V6, datasource/Compose configuration, and the specific `GlobalExceptionHandler` handler for service-level `AccessDeniedException`.

**Modify:** `pom.xml`, `security/SecurityConfig.java`, `web/AuthController.java`.

**Add:** `dto/LoginRequest.java`, `dto/AccessTokenResponse.java`, `security/JwtCryptoConfig.java`, `security/TokenService.java`, `web/JwtLoginExceptionHandler.java`, `src/main/resources/application-local.yaml` (append/merge with your existing local profile configuration; never replace unrelated settings).

**Deactivate/remove from Session configuration:** old `.formLogin(...)`, `.logout(...)`, `CsrfToken` endpoint, JSESSIONID continuity, and CSRF-token fetch flow. Existing `RestAuthenticationEntryPoint` and `RestAccessDeniedHandler` classes may remain in the repo but are no longer injected by the reference JWT chain; the Spring bearer-specific equivalents are used here. Inspect other beans or chains before deleting anything.

**No new Flyway migration is required.** JWTs are generated from the existing `app_users` identity data. A separate OAuth2 Authorization Server, refresh schema, blacklist, or manual bearer parsing class would obscure the lesson.

### 1. `pom.xml` — add the JWT dependency (and test starter only if absent)

Keep the Spring Boot **4.1.1** parent and existing dependencies from your actual project. Add the following inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Keep `spring-boot-starter-security`. This resource-server starter brings JWT verification support and JOSE crypto through managed dependencies; **no custom token parser and no manual dependency version pinning** are needed. The issuer below is an in-app teaching issuer, not a full OAuth2 Authorization Server.

The attached baseline lists a minimal existing test tree but does **not** establish that Spring Boot Test/JUnit Jupiter is installed. If your real `pom.xml` lacks it and you want to run the optional `@SpringBootTest` validation tests in section 13, also add this **test-scoped** dependency (do not duplicate an existing one):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. Generate a disposable local RSA pair (NOT for production)

The commands below assume OpenSSL is installed and available in PowerShell, Git Bash, or a compatible terminal. In the existing **`book-catalog-api` project root**:

```powershell
New-Item -ItemType Directory -Force local-secrets | Out-Null
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out local-secrets/jwt-private.pem
openssl pkey -in local-secrets/jwt-private.pem -pubout -out local-secrets/jwt-public.pem
```

Add this line to the project's existing `.gitignore`:

```gitignore
/local-secrets/
```

Verify **without displaying private-key contents**:

```powershell
openssl pkey -in local-secrets/jwt-private.pem -check -noout
openssl pkey -pubin -in local-secrets/jwt-public.pem -noout
```

The first file is **unencrypted PKCS#8** with header `-----BEGIN PRIVATE KEY-----`; the second is X.509/SPKI with header `-----BEGIN PUBLIC KEY-----`. This deliberately simple loading scheme is **LOCAL LEARNING ONLY**; restrict file permissions and do not use this as a production private-key management recipe. If OpenSSL is unavailable, install a reputable OpenSSL implementation or generate equivalent PKCS#8/SPKI PEMs using a trusted tool. Never create an online key on a third-party site.

### 3. `src/main/resources/application-local.yaml` — local-only token policy

Add these properties to **the existing** `application-local.yaml` if it exists; otherwise create that file. Do not overwrite existing settings.

```yaml
app:
  jwt:
    issuer: book-catalog-api-local
    audience: book-catalog-api
    access-token-ttl-seconds: 900
    private-key: ${JWT_PRIVATE_KEY_RESOURCE:file:./local-secrets/jwt-private.pem}
    public-key: ${JWT_PUBLIC_KEY_RESOURCE:file:./local-secrets/jwt-public.pem}
```

**Only the resource locations are stored here, not key bytes.** The application should fail startup in any other profile unless that profile supplies valid signing/verifying key locations and a deliberate issuer/audience policy. For this exercise, activate the `local` profile and keep your prior datasource/Compose configuration intact. The existing local-only admin bootstrap (`@Profile("local")`) still needs your *disposable* `BOOK_ADMIN_USERNAME` and `BOOK_ADMIN_PASSWORD` variables to create or find an admin.

If you use PowerShell with Maven:

```powershell
$env:BOOK_ADMIN_USERNAME = "practice_admin"   # choose an unused disposable local account
$env:BOOK_ADMIN_PASSWORD = Read-Host "Local-only admin password"
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Note:** `Read-Host` without `-AsSecureString` displays password characters as you type, although it avoids putting the value into shell history. Prefer your local secret-injection method, or prompt with `-AsSecureString` and convert only if needed for the environment variable. Avoid using real credentials. The bootstrap does not reset an existing ADMIN password on restart. Make sure your database is already started using the existing `compose.yaml` and that your actual Spring profile structure is compatible.

### 4. `security/JwtCryptoConfig.java` — key beans, encoder, decoder and validators

This file uses Spring Security 7.1 APIs. The private/public PEM values are read from resources at application startup. The decoder **only uses the public key**. `JwtValidators.createDefaultWithIssuer` supplies standard time validations; explicit validators additionally enforce the exercise's `aud`, **presence of `exp`**, and nonempty subject. An absent expiry should never become a timeless access token by accident.

```java
package com.example.bookcatalog.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtCryptoConfig {
    @Bean
    RSAPrivateKey jwtPrivateKey(
            @Value("${app.jwt.private-key}") Resource pem)
            throws IOException, GeneralSecurityException {
        byte[] der = readPem(pem, "PRIVATE KEY");
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    @Bean
    RSAPublicKey jwtPublicKey(
            @Value("${app.jwt.public-key}") Resource pem)
            throws IOException, GeneralSecurityException {
        byte[] der = readPem(pem, "PUBLIC KEY");
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(der));
    }

    private static byte[] readPem(Resource resource, String label)
            throws IOException {
        String content;
        try (var input = resource.getInputStream()) {
            content = new String(input.readAllBytes(), StandardCharsets.US_ASCII);
        }
        String begin = "-----BEGIN " + label + "-----";
        String end = "-----END " + label + "-----";
        if (!content.contains(begin) || !content.contains(end)) {
            throw new IllegalArgumentException("Expected " + label + " PEM format");
        }
        String body = content.replace(begin, "").replace(end, "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }

    @Bean
    JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        return NimbusJwtEncoder.withKeyPair(publicKey, privateKey).build();
    }

    @Bean
    JwtDecoder jwtDecoder(
            RSAPublicKey publicKey,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey)
                .build(); // RS256 expected by default for this builder

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer(issuer),
                        new JwtClaimValidator<List<String>>(
                                JwtClaimNames.AUD,
                                audiences -> audiences != null
                                        && audiences.contains(audience)),
                        new JwtClaimValidator<Instant>(
                                JwtClaimNames.EXP, Objects::nonNull),
                        new JwtClaimValidator<String>(
                                JwtClaimNames.SUB,
                                subject -> subject != null && !subject.isBlank()));
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
```

**Ownership:** You define this configuration and Spring constructs the beans. `TokenService` calls `JwtEncoder`; Spring's JWT authentication provider calls `JwtDecoder`. The standard `NimbusJwtDecoder` verifies the accepted signature algorithm using trusted public-key material; composed validators then check the accepted claims. The code is deliberately small: no JWKS endpoint, key rotation, HSM, or refresh token.

### 5. `dto/LoginRequest.java` — new JSON input

```java
package com.example.bookcatalog.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
```

The DTO **has no role field** and is not a database entity. Password minimum/maximum policy remains owned by the existing registration logic; login simply checks supplied credentials. Never log `LoginRequest.toString()` because the generated record representation would include the raw password.

### 6. `dto/AccessTokenResponse.java` — new safe output

```java
package com.example.bookcatalog.dto;

public record AccessTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
```

Only a token and metadata are returned. Set a `Cache-Control: no-store` header on responses that contain tokens, use HTTPS outside localhost, and ensure normal server access logs do not record response bodies.

### 7. `security/TokenService.java` — issue signed claims from trusted authentication

```java
package com.example.bookcatalog.security;

import com.example.bookcatalog.dto.AccessTokenResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final String issuer;
    private final String audience;
    private final long ttlSeconds;

    public TokenService(
            JwtEncoder encoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience,
            @Value("${app.jwt.access-token-ttl-seconds}") long ttlSeconds) {
        if (ttlSeconds < 1) {
            throw new IllegalArgumentException("JWT lifetime must be positive");
        }
        this.encoder = encoder;
        this.issuer = issuer;
        this.audience = audience;
        this.ttlSeconds = ttlSeconds;
    }

    public AccessTokenResponse issue(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Trusted authentication required");
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.equals("ROLE_USER") || a.equals("ROLE_ADMIN"))
                .map(a -> a.substring("ROLE_".length()))
                .distinct()
                .sorted()
                .toList();
        if (roles.isEmpty()) {
            throw new IllegalStateException("Authenticated user has no supported role");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(authentication.getName())
                .audience(List.of(audience))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String signedJwt = encoder.encode(
                JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AccessTokenResponse(signedJwt, "Bearer", ttlSeconds);
    }
}
```

**Who calls what?** Your `AuthController` calls `TokenService.issue(authentication)` **only after** the DAO provider returns successfully authenticated `Authentication`. `TokenService` derives **server-trusted** authorities, prepares timing/issuer/audience claims, and calls Spring's `JwtEncoder`; that encoder signs with the configured RSA private key. PostgreSQL is accessed by the user-loading path during login, **not** by token signing itself.

### 8. `security/SecurityConfig.java` — JWT replacement for the Session chain

This is a complete reference replacement **for the documented Session `SecurityConfig`**, not a patch applicable to an arbitrary unknown repository.

```java
package com.example.bookcatalog.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/books", "/books/**")
                    .permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/me")
                    .authenticated()
                .requestMatchers("/books", "/books/**", "/transaction-lab/**")
                    .hasRole("ADMIN")
                .anyRequest().denyAll())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }
}
```

**Session comparison:** The *ordered authorization matchers*, `@EnableMethodSecurity`, BCrypt and the account DAO infrastructure are intact. `STATELESS` removes session-based security continuity. Resource-server bearer JWT authentication replaces Session restoration. Form login and session logout are removed; new JSON login lives in `AuthController`. `csrf.disable()` is used **only** because this exercise has **no automatically attached authentication cookies**. A browser setup carrying a JWT in a cookie needs a fresh CSRF design.

**Potential status nuance:** Spring's `BearerTokenAuthenticationEntryPoint`/`BearerTokenAccessDeniedHandler` use bearer-style challenges and can include `WWW-Authenticate` metadata; do **not** assume the old session lab's exact JSON error body remains. The pre-existing global MVC catch-all must still allow method-security errors to produce a correct status.

### 9. `web/AuthController.java` — registration, JSON login and JWT-aware `/auth/me`

The original `AuthController` used `@AuthenticationPrincipal UserDetails` and `/auth/csrf`. This replacement keeps safe registration and the `CurrentUserResponse` DTO, removes the now-irrelevant CSRF endpoint and adapts current-user lookup to JWT-backed `Authentication`.

```java
package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.AccessTokenResponse;
import com.example.bookcatalog.dto.CurrentUserResponse;
import com.example.bookcatalog.dto.LoginRequest;
import com.example.bookcatalog.dto.RegisterRequest;
import com.example.bookcatalog.security.TokenService;
import com.example.bookcatalog.service.RegistrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final AuthenticationManager authenticationManager;
    private final TokenService tokens;

    public AuthController(
            RegistrationService registration,
            AuthenticationManager authenticationManager,
            TokenService tokens) {
        this.registration = registration;
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        registration.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @Valid @RequestBody LoginRequest request) {
        Authentication attempt = UsernamePasswordAuthenticationToken
                .unauthenticated(request.username(), request.password());
        Authentication verified = authenticationManager.authenticate(attempt);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(tokens.issue(verified));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new CurrentUserResponse(authentication.getName(), authorities);
    }
}
```

**Important:** The Authentication argument in `/auth/me` is framework-supplied after JWT validation, not created from any JSON input. We return its safe name/authority fields, not a `Jwt`, `UserDetails`, or `AppUser`. If the prior source's `RegistrationService` or DTO contracts have changed, preserve those updated contracts when adapting this file.

### 10. `web/JwtLoginExceptionHandler.java` — REST-safe 401 for JSON login

Because password verification now occurs **inside a controller**, `AuthenticationException` may be seen by your prior `GlobalExceptionHandler` catch-all and incorrectly become HTTP 500. Add a narrowly scoped, high-priority handler that returns a generic 401 without leaking whether a username exists.

```java
package com.example.bookcatalog.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = AuthController.class)
public class JwtLoginExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> onAuthenticationFailure(
            AuthenticationException failure) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        body.setTitle("Unauthorized");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}
```

This is for **controller-originating JSON login failures only**. The resource server's bearer authentication failures are handled in the filter chain, typically before MVC, using the configured bearer entry point. Retain the old project's specific method-level `AccessDeniedException` handling in `GlobalExceptionHandler` (or an equivalent correct arrangement), since it may otherwise be caught by a broad `Exception.class` handler.

### 11. Existing code intentionally retained — no need to rewrite it

The attached Session exercise already gives complete implementations for these files. You should **reuse** your actual current versions rather than copying another implementation here:

| Existing file | Why it remains the same |
|---|---|
| `model/AppUser.java` | JPA account with `id`, `username`, BCrypt hash, `USER`/`ADMIN` role |
| `repository/AppUserRepository.java` | DB lookup by username and duplication check |
| `security/AppUserDetailsService.java` | Maps `AppUser` → `UserDetails`, e.g. `roles(user.getRole())` |
| `service/RegistrationService.java` | Server assigns `USER`, validates and BCrypt-encodes raw password, enforces UNIQUE |
| `dto/RegisterRequest.java` | No caller-chosen role; validate username/password |
| `dto/CurrentUserResponse.java` | Safe `(username, authorities)` response |
| `security/LocalAdminBootstrap.java` | Disposable local ADMIN provisioning, never public promotion |
| `service/BookService.java` | Keep `@PreAuthorize("hasRole('ADMIN')")` on existing `setStock` |
| `web/GlobalExceptionHandler.java` | Preserve the method-auth denial handler; don't turn `403` into `500` |
| `db/migration/V6__create_app_users.sql` | Existing application-account schema; don't edit applied migrations |

For clarity, the *existing adapter* and *existing password encoder* still conceptually perform:

```java
// Inside existing AppUserDetailsService, unchanged:
return User.withUsername(user.getUsername())
        .password(user.getPasswordHash())
        .roles(user.getRole())
        .build();

// Inside existing RegistrationService, unchanged:
users.saveAndFlush(new AppUser(
        request.username(), passwordEncoder.encode(request.password()), "USER"));
```

These are **source-derived representative excerpts**, not replacement complete files: your attached Session exercise already contains their complete implementations and safe constraints. This reference solution's *new and changed files* above are complete.

### 12. Manual HTTP walkthrough (Windows PowerShell / Postman)

Use **only disposable local accounts and test tokens**. PowerShell's `Invoke-RestMethod` automatically deserializes the JWT login JSON. Do not display or save credentials/tokens in committed files. API calls below assume the existing Book Catalog runs at `127.0.0.1:8080` and the local PostgreSQL service is available on its unchanged configured port.

```powershell
$base = "http://127.0.0.1:8080"

# Public routes do not require a session, bearer token, or CSRF token.
curl.exe -i "$base/books"

# Register one disposable USER once; choose a password locally.
$userPassword = Read-Host "Disposable USER password"
$registerJson = @{ username = "learner_jwt"; password = $userPassword } |
    ConvertTo-Json -Compress
Invoke-WebRequest -Method Post -Uri "$base/auth/register" `
    -ContentType "application/json" -Body $registerJson

# Login now accepts JSON (old Session form login accepted URL-encoded fields).
$loginJson = @{ username = "learner_jwt"; password = $userPassword } |
    ConvertTo-Json -Compress
$userLogin = Invoke-RestMethod -Method Post -Uri "$base/auth/login" `
    -ContentType "application/json" -Body $loginJson
$userToken = $userLogin.accessToken  # Don't print in logs/screenshots.

# No cookie jar and no CSRF header: explicitly attach the bearer credential.
$userHeaders = @{ Authorization = "Bearer $userToken" }
Invoke-RestMethod -Uri "$base/auth/me" -Headers $userHeaders

# USER must be rejected before the Book service runs.
$bookBody = @{
    isbn = "JWT-LAB-1"; title = "JWT Lab"; author = "Student"
    price = 12.50; stock = 5
} | ConvertTo-Json -Compress
try {
    Invoke-WebRequest -Method Post -Uri "$base/books" `
        -Headers $userHeaders -ContentType "application/json" -Body $bookBody
} catch { $_.Exception.Response.StatusCode }

# A missing bearer credential is not an authenticated request.
curl.exe -i "$base/auth/me"
```

For ADMIN, use the disposable `local` profile bootstrap account. You should already have its local environment variables supplied to the process (you may need to enter the password separately in your terminal rather than reading process environment):

```powershell
$adminPassword = Read-Host "Disposable ADMIN password"
$adminJson = @{ username = "practice_admin"; password = $adminPassword } |
    ConvertTo-Json -Compress
$adminLogin = Invoke-RestMethod -Method Post -Uri "$base/auth/login" `
    -ContentType "application/json" -Body $adminJson
$adminToken = $adminLogin.accessToken
$adminHeaders = @{ Authorization = "Bearer $adminToken" }

Invoke-RestMethod -Uri "$base/auth/me" -Headers $adminHeaders
Invoke-WebRequest -Method Post -Uri "$base/books" `
    -Headers $adminHeaders -ContentType "application/json" -Body $bookBody
```

`POST /books` succeeds with `201` if this ISBN is new and the body meets the actual DTO; otherwise expect existing application errors like `409` for a duplicate ISBN. A successful `DELETE` is **not** a suitable Book Catalog test: your documented `BookService.delete()` deliberately flushes and throws a checked exception to demonstrate rollback. Do not interpret its expected application failure as JWT authorization failure.

**Postman equivalent:** Set `POST /auth/login`, **Body → raw → JSON**; copy `accessToken` from response; for next requests use **Authorization → Bearer Token**. Do **not** set `JSESSIONID` or retrieve `/auth/csrf` for this header-only lab. Try the same calls with token removed and with a USER token against an ADMIN route.

**Safe token decoding (local only):**

```powershell
# Inspect just the payload of a disposable JWT without third-party websites.
$payload = $userToken.Split('.')[1]
$payload += '=' * ((4 - $payload.Length % 4) % 4)
$payload = $payload.Replace('-', '+').Replace('_', '/')
[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload))
```

This code **reads a payload; it does not verify a signature**. The actual server performs verification and policy checks using `JwtDecoder`. An arbitrary change to a signed payload would invalidate its original signature.

### 13. Test `iss`, `aud`, required `exp` and expiration without making a dangerous endpoint

`src/test/java/com/example/bookcatalog/security/JwtValidationTest.java`

This example uses Spring Boot Test support **if present, or added as the optional test dependency in section 1** and active `local` profile to obtain the same encoder and decoder beans. It assumes disposable local PEM files and database/bootstrap configuration are available, as for a normal local app start. It deliberately never logs bearer token strings.

```java
package com.example.bookcatalog.security;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local")
class JwtValidationTest {
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;

    private String sign(String issuer, String audience, Instant expiration) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("learner_jwt")
                .audience(List.of(audience))
                .issuedAt(now.minusSeconds(60))
                .claim("roles", List.of("USER"));
        if (expiration != null) {
            claims.expiresAt(expiration);
        }
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(),
                claims.build())).getTokenValue();
    }

    @Test
    void acceptsMatchingClaims() {
        String token = sign("book-catalog-api-local", "book-catalog-api",
                Instant.now().plusSeconds(900));
        assertDoesNotThrow(() -> decoder.decode(token));
    }

    @Test
    void rejectsWrongIssuer() {
        String token = sign("wrong-issuer", "book-catalog-api",
                Instant.now().plusSeconds(900));
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void rejectsWrongAudience() {
        String token = sign("book-catalog-api-local", "another-api",
                Instant.now().plusSeconds(900));
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void rejectsExpiredEvenWhenSignatureIsCorrect() {
        String token = sign("book-catalog-api-local", "book-catalog-api",
                Instant.now().minusSeconds(3600));
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void rejectsMissingExpirationClaim() {
        String token = sign("book-catalog-api-local", "book-catalog-api", null);
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }
}
```

Run this class with your project's existing Maven test setup, for example:

```powershell
mvn "-Dtest=JwtValidationTest" test
```

The test encodes with a **trusted private key** but deliberately inserts wrong claims. That makes it a much stronger demonstration than testing a malformed three-part string only. Expiration is a full hour in the past to avoid confusing validator clock-skew allowances. If your project has changed its test dependencies, package or profile, adapt before running. Do not add a public “mint arbitrary roles” endpoint for testing.

### 14. Role-change and local-logout experiments

**Role freshness** (disposable local admin only):

```sql
-- In psql or pgAdmin against the local Book Catalog database only:
SELECT username, role FROM app_users WHERE username = 'practice_admin';

-- After you have already obtained one ADMIN JWT:
UPDATE app_users SET role = 'USER' WHERE username = 'practice_admin';

-- Repeat the old JWT request; then log in again to compare the new claims.
-- Finally restore original local state:
UPDATE app_users SET role = 'ADMIN' WHERE username = 'practice_admin';
```

The *old* JWT may continue to carry `ADMIN` in its valid claims until `exp`, even after PostgreSQL role becomes USER. A new password login loads the changed role and mints a different token. Do not run this SQL against anyone else's account or production database. Session-stored authorities could become stale too; the difference is the mechanism for invalidation and freshness.

**Client-side logout** (PowerShell, disposable token):

```powershell
$copiedToken = $userToken
Remove-Variable userToken
Invoke-RestMethod -Uri "$base/auth/me" `
    -Headers @{ Authorization = "Bearer $copiedToken" }
Remove-Variable copiedToken
```

Deleting your original variable did **not** revoke a copied token on the server. No JWT logout/revocation endpoint exists in this exercise. Real systems need a carefully chosen policy for replay and immediate deactivation requirements.

### 15. Troubleshooting order

| Symptom | Where to investigate |
|---|---|
| App fails startup with key parsing error | PEM header, correct PKCS#8 private/X.509 public encoding, paths, key-pair match |
| App fails startup due to missing properties | Ensure the intended local profile is active and YAML merged with existing configuration |
| App fails startup with database errors | Unchanged PostgreSQL/Compose/Flyway setup, local admin bootstrap variables |
| `POST /auth/login` returns 401 | JSON field names, stored BCrypt hash, account lookup/provider, password and generic MVC auth handler |
| `POST /auth/login` returns 500 | Check if login `AuthenticationException` was swallowed by broad global advice; inspect stack trace privately |
| `POST /auth/login` returns 403 | Check for leftover form-login/CSRF filter or another matching chain |
| Valid login token rejected everywhere | Private/public key pair, `iss`, `aud`, required `exp`, clock skew, token transport |
| JWT gives 401 after tampering | Expected: bad signature or malformed token |
| Authenticated `USER` gets 403 on Book write | Expected role policy; verify mapped `ROLE_USER` |
| Authenticated `ADMIN` gets 403 | Inspect `roles` → `ROLE_ADMIN` converter, matcher order, method guard |
| `/auth/me` returns NPE | Old `@AuthenticationPrincipal UserDetails` was not adapted to JWT `Authentication` |
| Status becomes 500 inside `@PreAuthorize` path | Check old broad MVC advice and specific `AccessDeniedException` handler |
| No `JSESSIONID` on bearer auth | Expected; authentication uses token per request |
| Token survives client “logout” | Expected absent server-side revocation |

Keep logs at outcome/route level only. Do not log raw passwords, JWTs, signing private keys, or complete HTTP `Authorization` values.

### 16. Final architecture explanation and ownership checklist

```text
JWT LOGIN (database access for credentials)

POST /auth/login JSON
  → AuthController [developer-written MVC endpoint]
  → AuthenticationManager [Spring interface, bean from AuthenticationConfiguration]
  → DaoAuthenticationProvider [Spring provider for username/password]
  → AppUserDetailsService [your existing adapter]
  → AppUserRepository [existing Spring Data repository]
  → Hibernate → JDBC → HikariCP → PostgreSQL [same as Session]
  → PasswordEncoder.matches [existing BCrypt bean]
  → authenticated Authentication
  → TokenService [new application code]
  → JwtEncoder / NimbusJwtEncoder [Spring bean using private RSA key]
  → JSON access token returned (no session persistence)

LATER JWT REQUEST (usually NO credential DB lookup)

Authorization: Bearer <access JWT>
  → Spring Security's BearerTokenAuthenticationFilter [framework]
  → JwtAuthenticationProvider [framework]
  → JwtDecoder / NimbusJwtDecoder [configured bean: public key + validators]
  → JwtAuthenticationConverter [configured bean: roles → ROLE_...]
  → authenticated JwtAuthenticationToken [framework object]
  → SecurityContext [current request]
  → URL authorization / @PreAuthorize [existing policy]
  → Controller → Service → Repository [when business operation needs DB]
```

| Component | Defines / creates | Caller | Called next / responsibility | Code ownership |
|---|---|---|---|---|
| `SecurityFilterChain` | You configure bean; Spring builds | Servlet filter infrastructure | JWT validation, authorization | `SecurityConfig.java` |
| `AuthenticationManager` | Spring contract/configured bean | `AuthController` at login | `DaoAuthenticationProvider` | `SecurityConfig.java` bean |
| `DaoAuthenticationProvider` | Spring | Manager at credential login | `UserDetailsService`, `PasswordEncoder` | Existing framework wiring |
| `AppUserDetailsService` | You, via Spring bean | DAO provider | Repository query | Existing `AppUserDetailsService.java` |
| `PasswordEncoder` | You publish BCrypt bean | DAO provider, registration service | Verify or hash | `SecurityConfig.java` |
| `TokenService` | You | Controller after verification | Build trusted claims, call encoder | `TokenService.java` |
| `JwtEncoder` | You configure Spring implementation | TokenService | Sign with private key | `JwtCryptoConfig.java` |
| `JwtDecoder` | You configure Spring implementation | JWT provider | Verify public-key signature and validators | `JwtCryptoConfig.java` |
| `BearerTokenAuthenticationFilter` | Spring | Selected filter chain | Bearer extraction and authentication | Framework, not a custom class |
| `JwtAuthenticationConverter` | You configure Spring implementation | JWT provider | Convert verified claims to auth+authorities | `SecurityConfig.java` |
| `Authentication` | Spring interfaces/classes | Login manager or JWT provider | Put into `SecurityContext` | Used, not rewritten |
| `SecurityContext` | Spring | Auth/authorization infrastructure | Current request security identity | Framework |
| `AuthenticationEntryPoint` | Spring bearer handler for filters; small separate MVC advice for login errors | Security exception flow / MVC advice | Generic 401 | `SecurityConfig.java`, `JwtLoginExceptionHandler.java` |
| `AccessDeniedHandler` | Spring bearer handler | Filter-security exception machinery | 403 | `SecurityConfig.java` |
| `@PreAuthorize` | You annotate existing service | Spring method interceptor | Allow/deny privileged business method | Existing `BookService.java` |

**Important precision:** `AuthenticationManager` is an *interface*, and the standard manager can delegate to different provider types. `DaoAuthenticationProvider` handles the password-login token; `JwtAuthenticationProvider` handles decoded bearer JWTs. Sharing the `Authentication` abstraction **does not** mean the same DAO provider runs again on each JWT-authenticated request.

### 17. What this lab intentionally does *not* prove

The example demonstrates the mechanics of signed bearer tokens, short expiry, issuer/audience checks, and role-based authorization. It is **not production authentication infrastructure**. Additional production concerns may include TLS termination, rate limiting/password-guess resistance, secure browser/mobile storage, real account lockout/disablement, sign-in audit, compromised key response, refresh token rotation, immediate revocation, server-wide key rotation, distributed resource servers, key vault/HSM, observability and cookie/CSRF policy when appropriate.

A team might instead choose server sessions with shared storage, opaque access tokens, or an established identity provider. **JWT is a trade-off, not an automatic upgrade over Session.**

### Future exercises (not part of this one)

OAuth2 Authorization Server; OIDC and ID tokens; Keycloak/Auth0/Entra; browser token transport and BFF pattern; access/refresh-token split; refresh rotation and reuse detection; refresh revocation and server-side sessions; key rotation and JWKS; multi-resource-server architectures; role freshness / account disablement; SSO; external identity providers; end-to-end integration/security tests.

## Final mental model

```text
SESSION: login → verify → Authentication → server-side session + JSESSIONID
         later → restore Authentication from valid session → authorize

JWT:     login → verify → Authentication → sign JWT with private key
         later → verify JWT with public key + validate claims
               → reconstruct Authentication for THIS request → authorize
```

**Still present:** `AuthenticationManager` (at password login), `DaoAuthenticationProvider`, `UserDetailsService`, `PasswordEncoder`, `SecurityContext`, roles/authorities, `401`, `403`, and `@PreAuthorize`. **Changed:** how verified identity continues across separate HTTP requests.

### Documentation and version grounding

- Source exercise: attached `Spring_Security_Exercise(2).md`, especially its cumulative reference source files and `BookService.delete`/transaction caveat; source guide: attached `Spring_Security_Essential_Concepts(3).md`. These document a **snapshot**, not a live repository.
- Spring Security 7.1 [JWT Resource Server reference](https://docs.spring.io/spring-security/reference/7.1/servlet/oauth2/resource-server/jwt.html), [NimbusJwtEncoder API](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/jwt/NimbusJwtEncoder.html), [JwtClaimValidator API](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/jwt/JwtClaimValidator.html), [JwtValidators API](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/jwt/JwtValidators.html), [JwtAuthenticationConverter API](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/server/resource/authentication/JwtAuthenticationConverter.html), [BearerTokenAccessDeniedHandler API](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/server/resource/web/access/BearerTokenAccessDeniedHandler.html).
