# JWT — Essential Concepts

> A first-principles, **Session → JWT** guide for a Java backend developer who already knows Spring Security. This is about what changes in authentication continuity—not about learning Spring Security again.

**Reference baseline:** `Spring_Security_Essential_Concepts(3).md`, `Spring_Security_Exercise(2).md` (the attached versions; the pasted instructions use older suffixes). Their *documented* Book Catalog snapshot uses Java 21, Spring Boot **4.1.1**, Spring Security **7.1.1**, Spring MVC, JPA/Hibernate, PostgreSQL 17, Flyway V1–V6, and a `USER`/`ADMIN` account model. **A live source repository was not provided or inspected**; verify the current project state before applying the companion exercise.  
**Previous knowledge:** username/password Session login, `AuthenticationManager`, `DaoAuthenticationProvider`, `UserDetailsService`, BCrypt, `SecurityContext`, `JSESSIONID`, role checks, CSRF.  
**Companion exercise:** [JWT_Exercise.md](JWT_Exercise.md)  
**Goal:** understand trusted token issuance, bearer validation, rebuilding authentication on each request, and the trade-offs against sessions.

## How to study

1. Recall the equivalent Session flow **before** reading a JWT section.
2. Ask: **What stays? What moves? What is reconstructed?**
3. Trace a token from issuance → transport → validation → `Authentication` → authorization.
4. Predict `401` and `403` before checking answers.
5. Treat **signed** and **secret** as different properties.
6. Answer each checkpoint without opening its `<details>` answer.
7. Do the matching practical task in `JWT_Exercise.md` before reading its reference solution.

| Priority | Meaning |
|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Daily JWT and API-security competence |
| ⭐⭐⭐⭐ IMPORTANT | Practical architecture and threat decisions |
| ⭐⭐⭐ NICE TO KNOW | Useful awareness; implement later if needed |
| ⭐⭐ FUTURE KNOWLEDGE | Beyond the basic learning exercise |

## Part I — The one mental-model change

### 1. Why JWT exists — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** a trusted `Authentication` ultimately enables authorization. **Different:** where the proof of an earlier successful login lives. **Flow change:** Session uses a server-held entry looked up by a session ID; a self-contained JWT carries signed claims validated on the new request. **Replacement:** `HttpSession`/`JSESSIONID` continuity → bearer-token validation.

```text
SESSION
login → verify password → Authentication → server-side SecurityContext/session
                                           ↳ client gets JSESSIONID
later → Cookie: JSESSIONID → restore context → authorize

JWT (SELF-CONTAINED BEARER)
login → verify password → Authentication → sign access JWT
                                           ↳ client receives JWT
later → Authorization: Bearer <JWT> → validate → rebuild Authentication → authorize
```

JWT can make separately deployed APIs verify a trusted issuer's assertions without reading a shared session on every request. **It is an option, not a universal improvement**: distributed session storage or opaque bearer tokens can also work. Both modes still need a security policy and appropriately protected credentials.

### 2. JWT is not authentication by itself — ⭐⭐⭐⭐⭐ MUST KNOW

| Term | Precise job |
|---|---|
| **JWT** | A compact JSON claims format, commonly signed as a JWS. |
| **Bearer** | A credential-use rule: possession normally suffices to present it. |
| **Authentication** | A trusted assertion about the current caller after verification. |
| **Authorization** | A decision on what that caller may do. |
| **Spring Security** | Framework that authenticates requests and enforces those decisions. |
| **OAuth2** | An authorization framework; not a synonym for JWT. |

A three-part string *claiming* `"roles":["ADMIN"]` is only attacker-controlled data until the application verifies it using trusted key material **and** its policy. A JWT may be used as an access token, ID token, or other type; being a JWT alone does not make it an API credential.

### 3. JWT structure — ⭐⭐⭐⭐⭐ MUST KNOW

```text
base64url(HEADER) . base64url(PAYLOAD) . base64url(SIGNATURE)
       xxxxx       .        yyyyy       .         zzzzz
```

**Header** describes the token type/cryptographic algorithm:

```json
{"alg":"RS256","typ":"JWT"}
```

**Payload** contains claims (example values are illustrative):

```json
{
  "sub": "alice",
  "roles": ["USER"],
  "iss": "book-catalog-api",
  "aud": ["book-catalog-client"],
  "iat": 1790000000,
  "exp": 1790000900
}
```

**Signature** protects the encoded header/payload from undetected changes, assuming a secure algorithm and trusted key. An attacker can decode the first two parts, or produce an entirely new JSON payload—but cannot forge an accepted signature without the signing capability. The API must not choose its trust policy merely from the untrusted `alg` header. **JWS signing is not encryption.** Do not put raw passwords, password hashes, API keys, or private data in the JWT because you assume the payload is hidden.

### 4. Base64URL is encoding, not secrecy — ⭐⭐⭐⭐⭐ MUST KNOW

`Base64URL` maps bytes into URL/header-friendly characters (`-` and `_` instead of standard Base64's `+` and `/`; optional padding). Anyone can reverse it. **Encoded ≠ encrypted**. HTTPS protects the token **in transit**; signing protects **integrity**; neither hides its payload from its holder.

### 5. Claims — the statements in a token — ⭐⭐⭐⭐⭐ MUST KNOW

Claims have meanings only within a verifier's trust rules. RFC-registered claims are **not all automatically mandatory**; your application should require the ones its policy needs.

| Claim | Meaning / reason | Usually required? | If ignored or misused |
|---|---|---|---|
| `sub` | Subject: whom the token represents | Require for this exercise | No reliable principal identity |
| `iss` | Issuer: who issued it | Require here | Token from wrong issuer could be accepted |
| `aud` | Audience: intended recipient/API | Require here | Valid token for a different API reused here |
| `exp` | Expires at (NumericDate, epoch seconds) | Require for access JWT | Old stolen token may work indefinitely |
| `iat` | Issued at | Include here | Harder to reason about token age; may need additional validation |
| `nbf` | Not valid before | Optional here | Premature use if your policy expects this check |
| `jti` | Unique token identifier | Optional | Difficult to refer to a specific token for revocation/audit |

**Custom claims:** `roles`, `permissions`, and `userId` are possible. Add only a small, stable, necessary set of nonsecret assertions. Roles are **not** trusted merely because a client supplied them at login; the issuer must derive them from an authenticated authority source. Claim names and values are an explicit contract between issuer and verifier.

### 6. `sub` and the principal — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** an authenticated identity is exposed to the application. **Different:** Session can restore a previously stored `UserDetails` principal; JWT authentication normally reconstructs its principal from a verified `sub` claim. **Flow change:** `JSESSIONID → SecurityContext` becomes `valid JWT → JwtAuthenticationToken → SecurityContext`. **Replacement:** session principal restoration → claim-derived principal.

For the learning exercise `sub="alice"` identifies the username. More complex systems may prefer immutable internal user IDs, because usernames can change. Do not infer a fresh database account lookup simply from `sub`.

### 7. Roles and authorities — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** `hasRole("ADMIN")` checks `ROLE_ADMIN`, and `@PreAuthorize` is still valid. **Different:** after Session login, authorities may be restored from session state; after JWT verification, they may be converted from signed `roles` claims. **Replacement:** stored session authorities → `JwtAuthenticationConverter` + `JwtGrantedAuthoritiesConverter` or equivalent mapping.

```text
SESSION: UserDetails.roles("ADMIN") → ROLE_ADMIN → session → hasRole("ADMIN")
JWT:     signed roles=["ADMIN"]   → explicit converter → ROLE_ADMIN → hasRole("ADMIN")
```

Spring's default JWT scope mapping commonly produces `SCOPE_...` from `scope`/`scp`, **not** `ROLE_ADMIN` from a custom `roles` claim. Configure it deliberately. Roles in tokens can become stale (section 37).

### 8. What a signature proves — ⭐⭐⭐⭐⭐ MUST KNOW

For signed JWS tokens, the signer signs the **encoded header + period + encoded payload** using a cryptographic key; the verifier checks with trusted key material. A changed payload means the old signature fails verification.

| Signature **can** support | Signature **does not** supply |
|---|---|
| Data integrity | Encryption/confidentiality |
| Attribution to a holder of the signing capability | Safe browser storage or immunity to XSS |
| Tamper detection | Expiration, audience, issuer, or entitlement freshness by itself |
| Trust *when the key/algorithm and issuer policy are correct* | Automatic logout/revocation |

**Important:** A perfectly signed token may still be expired, intended for a different service, or carry no sufficient authority.

### 9. Symmetric vs asymmetric signing — ⭐⭐⭐⭐⭐ MUST KNOW

```text
HMAC / HS256:             one shared secret
                          ├─ issuer signs
                          └─ API verifier checks (and could also forge if compromised)

RSA / RS256:              private key ── signs (issuer only)
                          public key  ── verifies (one or many APIs)
```

| Property | HS256 / HMAC | RS256 / RSA (exercise choice) |
|---|---|---|
| Signer | Anyone with shared secret | Holder of private key |
| Verifier | Same shared secret | Public key holder |
| Key-sharing risk | Every verifier can mint tokens | Verifiers cannot mint without private key |
| Typical use | Simple single-issuer tightly trusted backend | Issuer + independently deployed verifiers |
| Trade-off | Simpler key handling, but wider secret exposure | More key types/management and some computational cost |

`ES256` is an elliptic-curve signature option with its own key pair. Never use handwritten cryptography or accept algorithms chosen by an untrusted request. The exercise uses **RS256**, with a local-only private/public PEM pair and Spring's established encoder/decoder.

### 10. Bearer credentials — ⭐⭐⭐⭐⭐ MUST KNOW

```http
GET /auth/me HTTP/1.1
Authorization: Bearer <access-jwt>
```

The *bearer* may access whatever this token grants: **whoever possesses a usable token can usually replay it**. HTTPS, secure storage, short lifetimes, and redacted logs matter.

> 🔁 **Similar to Session-based authentication**
>
> **Same:** stolen `JSESSIONID` and stolen bearer JWT may both permit impersonation. **Different:** session ID is an opaque lookup handle; a signed JWT can often be verified locally without a central lookup. **Replacement:** Cookie-carried session ID → explicitly attached bearer credential in this exercise.

## Part II — Follow a login and a protected request

### 11. Login and JWT issuance — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** credential verification can use exactly the same `AuthenticationManager → DaoAuthenticationProvider → UserDetailsService → repository → BCrypt` path. **Different after success:** Session saves context server-side; JWT code issues a signed token. **Replacement:** session-saving login success handler → token issuer (`TokenService`/`JwtEncoder`).

```text
POST /auth/login (JSON username/password)
  └→ your LoginController
      └→ AuthenticationManager.authenticate(...)
          └→ DaoAuthenticationProvider
              ├→ UserDetailsService → AppUserRepository → PostgreSQL
              └→ PasswordEncoder.matches(raw, storedHash)
                  ↓
              authenticated Authentication
                  ↓
              TokenService → JwtEncoder (sign claims) → access JWT to client
```

**What is reused:** `AppUser`, its PostgreSQL mapping, registration, `UserDetailsService`, `PasswordEncoder`, and the provider. **What changes:** disable framework form-login filter on `/auth/login`; a JSON controller now calls the manager *after* authentication infrastructure is configured, and returns `accessToken`, `tokenType`, `expiresIn`. The controller does not manually compare passwords or query password hashes.

### 12. Later JWT-authenticated request — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** trusted `Authentication` enters `SecurityContext`, then URL/method authorization runs. **Different:** no session restoration; the bearer token is independently parsed and validated every request. **Replacement:** session-context lookup → Spring resource-server's bearer-token filter, `JwtDecoder`, validators and authority converter.

```text
GET /auth/me + Authorization: Bearer eyJ...
    ↓
BearerTokenAuthenticationFilter
    ↓
JwtAuthenticationProvider → JwtDecoder
    ├→ verify RS256 signature against trusted public key
    ├→ check exp/nbf and expected issuer/audience
    └→ get verified claims
    ↓
JwtAuthenticationConverter → roles → ROLE_USER/ROLE_ADMIN
    ↓
JwtAuthenticationToken / Authentication
    ↓
SecurityContext (for THIS request)
    ↓
authorizeHttpRequests / @PreAuthorize → Controller → Service
    ↓
request completes → thread-associated SecurityContext cleared
```

The **bearer filter and JWT authentication provider are supplied by Spring Security**; you generally do not write your own `OncePerRequestFilter` merely to read a JWT.

### 13. Is `UserDetailsService` called for every JWT request? — ⭐⭐⭐⭐⭐ MUST KNOW

**Normally not in the simple self-contained design.** The DAO provider calls it for password verification during login. On a later request, the resource-server JWT provider validates the token and creates a JWT-specific `Authentication` without invoking the username/password `UserDetailsService`.

**Exceptions:** an API may deliberately query users or permissions to check account disablement, up-to-date roles, `tokenVersion`, or required freshness. That adds DB/cache state and latency. **JWT does not prohibit a database lookup**, but the exercise keeps validation self-contained so the contrast is visible.

### 14. `SecurityContext` is still essential — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** `SecurityContextHolder` exposes the current `Authentication`; authorization reads its authorities. **Different:** sessions may persist/restore context across HTTP requests; bearer JWT authentication constructs it on each request. **Replacement:** session-based `SecurityContextRepository` continuity → token validation plus request-scope context construction.

`SecurityContext` is **not** the JWT. It is a framework object available during processing. A controller accepting `Authentication` (or a suitable JWT-aware principal) is simpler than parsing HTTP headers. Context cleanup after request execution still matters.

### 15. Stateless JWT authentication — ⭐⭐⭐⭐⭐ MUST KNOW

**Stateless with respect to HTTP session authentication** means the server does not rely on a remembered login session for subsequent bearer requests. Every request brings a credential and is independently authenticated.

```text
NO session-authentication continuity
≠ NO PostgreSQL
≠ NO application state
≠ NO signing keys
≠ NO optional revocation/refresh records
```

In a stateless bearer setup, the backend still persists books, account rows, audit logs, and may maintain other kinds of state. Running on many instances still requires consistent validation keys, policy, clocks, and deployment design.

### 16. `SessionCreationPolicy.STATELESS` — ⭐⭐⭐⭐⭐ MUST KNOW

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

This tells Spring Security not to create or use an HTTP session to obtain its `SecurityContext`. **It does not sign or decode JWTs.** You still need issuance, trusted signature validation, claims checks, authority mapping, and bearer support. No automatic `JSESSIONID` is required for authentication continuity in the intended exercise; unrelated code could still create sessions if implemented separately.

### 17. Validation is more than verifying a signature — ⭐⭐⭐⭐⭐ MUST KNOW

| Check | Why it matters |
|---|---|
| Trusted signature/key | Reject modifications and unknown signers |
| Accepted algorithm (`RS256` here) | Reject algorithm confusion or unexpected crypto |
| `exp` **present** and future | Reject expired or non-expiring access tokens |
| `nbf` if present | Reject use before valid time |
| Expected `iss` | Reject another issuer's tokens |
| `aud` contains this API's identifier | Reject tokens intended for another resource |
| Required nonempty `sub`, sensible claims | Avoid malformed or unusable identities |
| Roles conversion policy | Prevent unsupported / improperly prefixed permissions |

Some framework defaults **validate `exp` if present** without universally requiring the claim to exist. The companion exercise explicitly requires `exp` as an application policy. Validators should not be replaced with ad hoc `split(".")` logic. Reject verification/claim failures before granting access; respond generically with `401`.

### 18. Expiration (`exp`) — ⭐⭐⭐⭐⭐ MUST KNOW

`exp` is an absolute expiration instant (JSON NumericDate). At/after expiry a token must not be accepted (accounting for configured small clock tolerance). The exercise uses a **15-minute** access-token lifetime; shorter experiments temporarily reduce it.

> 🔁 **Similar to Session-based authentication**
>
> **Same:** credentials have a lifetime. **Different:** invalidating a server session immediately ends its lookup; a previously issued JWT may remain usable until expiration unless the API also consults revocation/freshness state. **Replacement:** session invalidation → expiry policy plus optional revocation mechanisms.

### 19. `iat`, `nbf`, and clock skew — ⭐⭐⭐⭐ IMPORTANT

`iat` says when the token was issued. `nbf` says when its validity begins; `exp` says when it ends. Real systems have slightly imperfect clocks. A small configured tolerance (for instance 30 seconds) can prevent accidental cross-host rejections; it also slightly widens an acceptance window. Synchronize clocks, keep skew small, and **do not** treat `iat` as automatic protection against reuse or rely on it for lifetime enforcement unless explicitly validated.

### 20. Issuer (`iss`) — ⭐⭐⭐⭐⭐ MUST KNOW

**Question:** *Who issued this token?* The verifier should compare `iss` to a configured trusted issuer such as `book-catalog-api-local` in the exercise, instead of trusting arbitrary token data. Signature validation is necessary but not always sufficient: a key infrastructure can support multiple issuers, or clients can present valid but wrong-context tokens.

### 21. Audience (`aud`) — ⭐⭐⭐⭐⭐ MUST KNOW

**Question:** *Is this token intended for this API?*

```text
Token: aud=["inventory-api"]
Request: book-catalog-api
Result: reject, even if signature is authentic
```

The exercise chooses `aud=["book-catalog-api"]`. **`aud` is the intended resource, not necessarily the client application making the request.** Services should not casually accept each other's tokens.

### 22. Access tokens — ⭐⭐⭐⭐⭐ MUST KNOW

An **access token** is the short-lived credential a caller sends to a protected API. Its claims tell the resource server what it may trust *after verification*. Here it is a signed JWT; access tokens can also be opaque. Access-token exposure is frequent because the client sends one on each protected call.

### 23. Refresh tokens — ⭐⭐⭐⭐ IMPORTANT (concept only)

A refresh token helps obtain a **new access token** without asking for the password every 15 minutes. It commonly lives longer, is presented to a dedicated refresh endpoint—not to `/books`—and needs stronger storage and lifecycle controls. Refresh tokens can be opaque; a JWT format is not required. **Rotation** replaces a used refresh token with a new one; **reuse detection** can notice that an already-used token has been replayed. Many robust implementations keep token-family/revocation state. The basic lab does **not** implement refresh tokens.

> 🔁 **Similar to Session-based authentication**
>
> **Same:** users want continuous access without re-entering credentials. **Different:** an HTTP session may extend/renew server-side; an access+refresh design makes renewal a separate credential exchange. **Replacement:** server session renewal behavior → dedicated refresh flow (future study).

### 24. Access vs refresh token — ⭐⭐⭐⭐ IMPORTANT

| Aspect | Access token | Refresh token |
|---|---|---|
| Primary job | Authorize API access | Obtain new access credentials |
| Typical lifetime | Minutes | Days/weeks, depending on policy |
| Sent to | Resource endpoints | Authorization/refresh endpoint |
| Exposure | Often, each API call | Infrequently |
| Storage | Protect against theft; often memory | Usually stronger client/server controls |
| Revocation | May be difficult for self-contained JWT | Frequently managed with server state |
| If stolen | Attacker may call API until expiry/revoked | Attacker may mint further access tokens until detected/revoked |

### 25. Logout with JWT — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same intention:** make the client stop acting as the user. **Different semantics:** Session logout invalidates the server-held session. Removing a JWT from a client **only deletes that copy**. **Replacement:** server-side session invalidation → local token deletion, short expiry, and optional server-side revocation/refresh invalidation.

```text
SESSION: invalidate session ID → even a copied old JSESSIONID cannot restore it
JWT:     delete browser copy   → copied still-valid JWT may still work
```

Options: short-lived access tokens (usually baseline), central blocklist/revocation checks (adds state), per-user token version validation (adds lookup), refresh-token revocation, or emergency key rotation (affects every token using that key). No blacklist is mandatory for every project. The basic lab's “logout” is **client-side token discard**; it deliberately demonstrates that copies may still be usable.

---
## Part III — Browser/security consequences and architecture choices

### 26. Revocation versus stateless validation — ⭐⭐⭐⭐ IMPORTANT

Self-contained verification is convenient precisely because it avoids contacting a central store. But a newly imposed rule such as “disable **this** token immediately” requires current server-side knowledge. Possible mechanisms:

| Approach | Mechanism | Cost or limitation |
|---|---|---|
| Short access-token TTL | Wait for expiry | Not immediate; short replay window remains |
| Denylist keyed by `jti`/hash | Query cache/DB on request | Adds shared state and availability/cleanup requirements |
| User `tokenVersion` | Compare signed claim to live account version | Lookup on request unless cached |
| Refresh-token revocation | Stop future access-token minting | Existing access JWT remains valid until expiry |
| Signing-key rotation | Stop accepting affected signed tokens | Broad blast radius; coordination necessary |

**Design decision:** how quickly must a compromised token or removed privilege stop working? Session invalidation offers convenient centralized control; the JWT approach trades that for local verification unless extra state is introduced.

### 27. Where can a client store JWTs? — ⭐⭐⭐⭐⭐ MUST KNOW

| Storage/transport | Benefit | Main risks / caveats |
|---|---|---|
| Browser memory | Disappears on reload; not automatically sent | XSS can still use/steal accessible token while page runs; reload needs re-auth or renewal |
| `localStorage` | Persists across reload | Script-readable; XSS can exfiltrate bearer token |
| `sessionStorage` | Scoped to tab session | Still script-readable; XSS risk remains |
| `HttpOnly; Secure; SameSite` cookie | Scripts cannot directly read cookie | Automatically attached to matching requests; CSRF defenses and cookie policy matter; XSS can still cause actions |
| Native mobile secure storage | OS-backed credential storage | Device/app compromise, logs/backups, secure networking still matter |
| Server-to-server protected store | No end-user browser storage | Secret management, rotation, process access, logging remain |

This lab uses a **manually attached `Authorization: Bearer` header** from `curl`/Postman. It does not prescribe a production browser storage strategy. Do not put JWTs in URLs, source control, bug reports, or ordinary logs.

### 28. CSRF with JWT — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** protect state-changing operations when a browser can send a user's authentication credential without the user's intentional per-request action. **Different:** an ordinary cross-site HTML request cannot automatically add a manually controlled `Authorization` bearer header the way the browser auto-sends matching cookies. **Replacement:** session-cookie CSRF token protection may be unnecessary for a strictly header-only, cookie-free bearer API; if JWT is cookie-carried, analyze CSRF again.

```text
SESSION COOKIE: browser automatically sends Cookie: JSESSIONID=...
                → malicious site can attempt unsafe cross-site requests
                → CSRF token defense matters

HEADER JWT:    application explicitly adds Authorization: Bearer ...
               → not ambient cookie authentication
               → standard cross-site form cannot attach that header

COOKIE JWT:    browser auto-sends cookie again → CSRF concern returns
```

`csrf.disable()` **is justified in the companion exercise only because of its explicitly header-only/no-auth-cookie contract**, not because tokens are called JWTs. CORS does not replace CSRF protection.

### 29. XSS and token theft — ⭐⭐⭐⭐⭐ MUST KNOW

An XSS vulnerability can run attacker-controlled JavaScript in a trusted origin. If tokens are script-readable, they may be copied and replayed elsewhere. An `HttpOnly` cookie prevents direct `document.cookie` access to that credential, **but does not stop malicious in-origin JavaScript from sending requests or reading accessible responses**. Defend against XSS (safe output encoding, content security, dependency hygiene), protect credentials, and minimize token lifetime/privileges.

### 30. CORS is separate — ⭐⭐⭐⭐ IMPORTANT

> 🔁 **Similar to Session-based authentication**
>
> **Same:** browser cross-origin access rules still apply regardless of how you authenticate. **Different practical detail:** adding an `Authorization` header generally leads to a CORS preflight for cross-origin browser requests. **Replacement:** none—CORS configuration remains an independent concern.

```text
CORS = which browser origins may read/send supported cross-origin requests
JWT  = format of signed token claims
Authentication = verification of caller
Authorization = permission for caller
```

Non-browser clients are not “blocked by CORS” in the same way. Permissive CORS is not permission to access an authenticated API; the server still validates tokens. When configuring a browser client later, allow trusted origins/methods/headers explicitly, including `Authorization`, and handle `OPTIONS` preflight.

### 31. `401` vs `403` — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** missing/invalid authentication normally leads to `401`; valid identity lacking permission normally leads to `403`. **Different:** what fails authentication—absent/invalid bearer token rather than absent/invalid session. **Replacement:** session entry point → resource-server bearer authentication entry point where appropriate.

| Request | Expected result | Why |
|---|---:|---|
| `GET /auth/me` without JWT | 401 | Missing identity |
| `GET /auth/me` with malformed/expired JWT | 401 | Bad bearer credential |
| `POST /books` with valid `USER` JWT | 403 | Authenticated but not `ROLE_ADMIN` |
| `POST /books` with valid `ADMIN` JWT and valid body | Application handles write (often 201) | Authentication and authorization passed |
| `POST /books` with invalid DTO | Application 400 even if ADMIN | Input validation is separate |

**Nuance:** exact response body/headers depend on the authentication entry point, exception filters, and application handlers. CSRF can also cause `403` in cookie-auth designs before a role decision.

### 32. `AuthenticationEntryPoint` and `AccessDeniedHandler` — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same responsibilities:** entry point handles authentication challenge/failure; denied handler handles forbidden access. **Different:** JWT resource-server infrastructure commonly provides `BearerTokenAuthenticationEntryPoint` and `BearerTokenAccessDeniedHandler`, with bearer-standard `WWW-Authenticate` responses. **Replacement:** the prior custom session REST entry point/handler may be replaced or adapted by bearer-specific handlers.

`BearerTokenAuthenticationFilter` catches invalid bearer authentication through its configured failure handling; `ExceptionTranslationFilter` handles security exceptions such as missing authentication during authorization. `@RestControllerAdvice` does not ordinarily catch pre-MVC filter failures. In Book Catalog, keep the **existing specific MVC handler** for method-security denial if a catch-all advice would incorrectly convert it to a 500.

### 33. URL and method rules usually stay — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** your least-privilege policy, matcher order, `@EnableMethodSecurity`, and `@PreAuthorize` are still meaningful. **Different:** trusted authorities are reconstructed from verified token claims. **Replacement:** none in the authorization rule itself.

```java
.requestMatchers(HttpMethod.GET, "/books", "/books/**").permitAll()
.requestMatchers("/books", "/books/**", "/transaction-lab/**")
    .hasRole("ADMIN")
.anyRequest().denyAll()
```

```java
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public Book setStock(long id, Integer stock) { /* unchanged business logic */ }
```

This is **only a policy excerpt**, not a complete configuration. Add explicit `/auth/login`, `/auth/register`, `/auth/me`, ERROR dispatch, resource server, CSRF choice, and a real service body in the companion exercise.

### 34. Password hashing is unchanged — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same:** raw password → `PasswordEncoder.matches(...)` against stored BCrypt hash during login; registration stores only a hash. **Different:** the server's response **after** authentication carries a JWT rather than creating session continuity. **Replacement:** no replacement for `PasswordEncoder` or `DaoAuthenticationProvider`.

Do not sign raw passwords into a JWT. `AuthenticationManager` handles password verification before `TokenService` issues anything.

### 35. Registration is unchanged in principle — ⭐⭐⭐⭐ IMPORTANT

> 🔁 **Similar to Session-based authentication**
>
> **Same:** validate username/password, limit BCrypt input bytes, encode once, save `app_users` via JPA, keep DB uniqueness. **Different:** later login issues a JWT. **Replacement:** none; retain the existing `RegistrationService`.

Only a trusted server-side process assigns `ADMIN`. Public registration always creates `USER`. A `role` field supplied by the client is not authorization.

### 36. JWT vs `JSESSIONID` at a glance — ⭐⭐⭐⭐⭐ MUST KNOW

| Dimension | JSESSIONID | Self-contained JWT bearer |
|---|---|---|
| On-wire content | Opaque session ID | Encoded header, claims, signature |
| Client sees claims? | No, lookup ID only | Yes, payload usually readable |
| Auth state | Typically on server in session | Claims in token; request-local context |
| Server validation | Find valid active session | Signature + claims policy |
| Storage/transport | Usually browser cookie | Header or possibly cookie, depending on design |
| Expiration | Server session lifecycle | Signed `exp` plus validation policy |
| Logout | Server invalidates session | Client discards token; may need revocation |
| Network overhead | Short ID | Larger header each authenticated request |
| Role changes | Session authorities may be stale | JWT roles may be stale |
| Multi-instance | Shared/sticky session strategy | Consistent keys/issuer policy; optional state for revocation |
| Stolen credential | Can allow impersonation | Can allow impersonation |
| CSRF | Relevant when auto-sent cookie | Depends on transport, not token format |

### 37. Role changes and stale claims — ⭐⭐⭐⭐⭐ MUST KNOW

> 🔁 **Similar to Session-based authentication**
>
> **Same problem:** cached or stored authorities can become stale. **Different:** a JWT may contain the old `roles` value and remain valid until expiry, whereas session auth has a central context that can be invalidated. **Replacement:** session update/invalidation controls → token expiry plus optional live lookup/revocation/token-version policy.

```text
10:00 Alice logs in as ADMIN → JWT roles=["ADMIN"] (expires 10:15)
10:05 DB app_users.role becomes USER
10:06 Old JWT is still correctly signed with roles=["ADMIN"]
10:06 JWT-only API may still allow ADMIN operation until 10:15
```

You **must** choose a freshness policy for high-risk operations. A signature proves the claim was issued—not that the claim remains true in PostgreSQL today. The exercise intentionally demonstrates this behavior using a disposable account and restores the original role afterward.

### 38. Token size — ⭐⭐⭐ IMPORTANT

Bearer JWTs are often transmitted in `Authorization` on **every** protected request. Big arrays of permissions, user profiles, and other data increase bandwidth and may hit header/proxy limits. Prefer minimal `sub`, issuer, audience, timing, and narrowly needed authorities; use the database for mutable application data.

### 39. JWT is not inherently safer, faster, or more RESTful — ⭐⭐⭐⭐⭐ MUST KNOW

JWT does **not** automatically mean better security, superior scalability, pure statelessness, or compatibility with microservices. Sessions can scale with external stores; signed tokens can introduce key distribution, longer revocation windows, stale privileges, and client storage risks. Choose based on browser vs mobile/service clients, revocation guarantees, identity provider, network topology, operational complexity, and threat model. For a conventional server-rendered website, Session + HttpOnly cookie is often simpler; for APIs across multiple consumers, bearer tokens may fit well.

### 40. JWT vs opaque bearer token — ⭐⭐⭐⭐ IMPORTANT

```text
JWT bearer:    value contains signed claims → validate locally (usually)
Opaque bearer: random-looking value → introspection/database lookup for meaning
```

**Bearer is how a credential is presented**, not its internal format. OAuth2 supports opaque and JWT access tokens. Opaque tokens can offer centralized revocation and smaller credentials at the cost of lookup infrastructure.

### 41. OAuth2 is not JWT — ⭐⭐ FUTURE KNOWLEDGE

OAuth2 defines patterns and roles for delegated access (authorization server, client, resource server, tokens). It does not mandate a JWT. A simple first-party Book Catalog username/password → JWT lab is **not** a full OAuth2 authorization server. We reuse Spring's **OAuth2 Resource Server** bearer-token validation infrastructure because it is robust and standardized; using the library does not imply we implemented OAuth2 authorization flows.

### 42. OpenID Connect and ID tokens — ⭐⭐ FUTURE KNOWLEDGE

OIDC builds identity/authentication capabilities on OAuth2. An **ID token** is for a client to learn about authentication at an identity provider; an **access token** is intended for a resource/API. Do **not** treat every ID token as a valid Book Catalog API access token. Validate the right audience, token purpose and issuer.

### 43. Use established JWT libraries — ⭐⭐⭐⭐⭐ MUST KNOW

Avoid home-made Base64/signature logic, manual `Authorization` parsing in MVC controllers, insecure `alg=none` cases, or blindly pasting deprecated filter tutorials. Use Spring Security's `oauth2ResourceServer().jwt(...)` plus `JwtDecoder`, and Spring's `JwtEncoder` if your **learning app** also issues tokens. Production applications frequently delegate issuance to an authorization server or identity provider.

### 44. Key management — ⭐⭐⭐⭐⭐ MUST KNOW

The **private RSA key** signs tokens and must remain confidential. The **public RSA key** verifies signatures and can be shared with trusted verifiers. Store secret material outside Git and runtime logs; restrict local file permissions; use appropriate vault/HSM/key management in production. A learner may create disposable PEM files locally **only for this lab**. An API must select its trusted key/algorithm from configuration, not trust arbitrary keys declared by a submitted JWT.

### 45. Key rotation and `kid` — ⭐⭐⭐ IMPORTANT

A JWT header may include a key identifier (`kid`), allowing a verifier to select an approved key during rotation. A typical transition: issue new tokens under a new private key, allow old public key temporarily while existing tokens expire, then retire old key. The verifier must still obtain keys from a **trusted** configured source; attacker-supplied `kid` is not a permission to fetch arbitrary URLs/files. Key rotation is **not implemented** in the basic lab.

### 46. JWKS — ⭐⭐⭐ NICE TO KNOW

A **JSON Web Key Set** publishes one or more public verification keys in a structured JSON format. A trusted issuer may expose a JWKS endpoint so resource servers can refresh verification keys and handle `kid`. Key retrieval, trusted issuer binding, caching, and rotation matter. The simple Book Catalog exercise reads a single local PEM public key instead.

### 47. Common JWT mistakes — ⭐⭐⭐⭐⭐ MUST KNOW

| Mistake | Correct mental model |
|---|---|
| “Base64URL is encryption” | Payload is readable |
| Put password/hash/API secret in claims | Signatures protect integrity, not secrecy |
| Accept token without verification | Claims are untrusted until verification succeeds |
| Verify signature but ignore expiry | Valid signature is not sufficient |
| Forget `iss`, `aud`, required `exp` | Enforce your own token policy |
| Trust a client-supplied `ADMIN` role | Derive roles from authenticated trusted server state |
| Use very long access-token TTL | Short TTL limits theft impact |
| Add huge mutable profiles in JWT | Minimize claims; fetch dynamic data separately |
| Log full bearer tokens | Treat tokens as secrets |
| Hard-code real keys | External secret/key management |
| Implement custom JWT crypto/filter | Prefer tested framework components |
| Assume logout deletes all copies | Revocation and expiry are separate |
| Disable CSRF solely because JWT is involved | Decide based on credential transport |
| Assume localStorage is always safe | Model XSS and replay risk |
| Use an ID token as API access token | Validate token purpose and audience |
| Assume microservices require JWT | Opaque tokens/session solutions also exist |
| Query `UserDetailsService` unconditionally per JWT request | Usually unnecessary for self-contained bearer path |
| Believe a signed `ADMIN` claim guarantees current DB role | Token claims can become stale |
| Use default `SCOPE_` authority mapping for `roles` | Explicitly map to `ROLE_` if using `hasRole` |

## Part IV — Master reference and request traces

### Session vs JWT master comparison

| Topic | Relationship | Session authentication | JWT bearer authentication (this lab) |
|---|---|---|---|
| Login username/password | **Same** | Submits credentials | Submits credentials |
| Password check | **Same** | BCrypt `matches` | BCrypt `matches` |
| `AuthenticationManager` | **Same** | Verifies submitted login | Verifies submitted login |
| `AuthenticationProvider` | **Same for password step** | `DaoAuthenticationProvider` | `DaoAuthenticationProvider` during login; JWT provider for later requests |
| `UserDetailsService` | **Same at login** | Loads user for DAO provider | Loads user at login; usually not per later bearer request |
| `PasswordEncoder` | **Same** | Verifies BCrypt | Verifies BCrypt |
| `SecurityContext` | **Same core concept** | Request context potentially session-persisted | Request context rebuilt per token |
| Authentication continuity | **Different** | Server session | Signed bearer credential on each request |
| Client credential | **Different** | `JSESSIONID` opaque cookie | JWT in `Authorization` header |
| Server auth state | **Different** | HTTP session | No HTTP session required for bearer path |
| Role source | **Similar** | UserDetails → session | Issuer → JWT roles → converter |
| Authorities | **Same contract** | `ROLE_ADMIN` | `ROLE_ADMIN` after mapping |
| `@PreAuthorize` | **Same** | Checks `Authentication` | Checks `Authentication` |
| URL authorization | **Same** | `hasRole`, `authenticated` | Same decisions |
| `401` | **Same concept** | No accepted session | Missing/invalid bearer token |
| `403` | **Same concept** | No authority or CSRF | Valid token but insufficient authority (header-only lab) |
| CSRF | **Transport-dependent** | Cookie → important | Header-only → different risk; cookie JWT → relevant again |
| CORS | **Same** | Browser policy | Browser policy |
| Expiration | **Different mechanism** | Session timeout/invalidation | `exp` claim and token validation |
| Logout | **Different** | Invalidate session | Discard own copy; optional revocation |
| Revocation | **Different** | Immediate central invalidation easy | Additional shared state or short expiry |
| Scaling | **Trade-off** | Session coordination/storage | Shared trust/keys; claims may be verified locally |
| Stolen credential | **Similar** | Stolen ID can be replayed | Stolen JWT can be replayed |
| Role freshness | **Similar hazard** | Existing sessions may hold stale authority | Existing JWTs may hold stale authority |
| Storage | **Different options** | Usually HttpOnly cookie | Memory, header, cookie, secure mobile store |
| Distributed systems | **Trade-off** | Shared session strategy | Key distribution + expiry/revocation strategy |

### Complete login and protected request flow

```text
LOGIN — Session and JWT share password verification

Client (username/password)
       │
       ▼
POST /auth/login
       │
       ▼
AuthenticationManager
       │
       ▼
DaoAuthenticationProvider
       ├── UserDetailsService
       │     └── AppUserRepository → Hibernate/JDBC → PostgreSQL
       └── PasswordEncoder.matches(raw, hash)
       │
       ▼
authenticated Authentication
       │
       ├── SESSION: save SecurityContext → HttpSession → JSESSIONID cookie
       │
       └── JWT:    TokenService → JwtEncoder(private key)
                       └── sign sub, roles, iss, aud, iat, exp
                               └── return accessToken
```

```text
PROTECTED REQUEST

SESSION
Cookie: JSESSIONID → server locates session → restores Authentication
                                          │
                                          ▼
                                  SecurityContext → authorization

JWT
Authorization: Bearer <JWT>
       │
       ▼
BearerTokenAuthenticationFilter → JwtAuthenticationProvider
       │
       ▼
JwtDecoder(public key)
       ├── check signature / expected signing algorithm
       ├── check exp, nbf, expected issuer and audience
       └── check required claims (application policy)
       │
       ▼
JwtAuthenticationConverter (roles → ROLE_...)
       │
       ▼
JwtAuthenticationToken → SecurityContext → URL/method authorization
       │
       ▼
Controller → Service → Repository (only if business use case needs DB)
```

### Who owns which component?

| Component | Defined/created by | Invoked by | Next step |
|---|---|---|---|
| `AuthenticationManager` | Spring Security contract; bean configured via auth setup | JSON login controller | DAO provider |
| `DaoAuthenticationProvider` | Spring Security, wired to app services | Authentication manager | `UserDetailsService`, `PasswordEncoder` |
| `UserDetailsService` | Your existing app adapter | DAO provider | `AppUserRepository` |
| `PasswordEncoder` | Your existing BCrypt bean | DAO provider / registration | `matches` / `encode` |
| `TokenService` | **You** | Login controller | `JwtEncoder` |
| `JwtEncoder` (`NimbusJwtEncoder`) | Spring library; **you configure bean** | TokenService | RSA signature |
| Bearer filter/provider | **Spring Security** | Servlet filter chain | Decoder / auth conversion |
| `JwtDecoder` (`NimbusJwtDecoder`) | Spring library; **you configure bean** | JWT authentication provider | Verify signature and claims |
| JWT converters | Spring library; **you configure mapping** | JWT authentication provider | `JwtAuthenticationToken` |
| `SecurityFilterChain` | **You configure**, Spring builds | Servlet security infrastructure | Authentication/authorization |
| `SecurityContext` | Spring Security | Auth filters/authorization | Current request's trusted identity |
| Entry point / denied handler | Spring implementations or **your** handlers | Security exception machinery | HTTP 401 / 403 |

## Checkpoints — answer before expanding

**1. Is the JWT payload encrypted?**
<details><summary>Answer</summary>No. Ordinary signed JWS JWTs have readable Base64URL payloads; encryption would require a different mechanism (for example JWE).</details>

**2. What makes a JWT trustworthy?**
<details><summary>Answer</summary>A verified signature under an explicitly trusted key/algorithm, plus policy checks for issuer, audience, time validity, required claims, and permitted authority mapping.</details>

**3. If you modify the payload but reuse the signature?**
<details><summary>Answer</summary>Verification fails; the signature was generated for the original encoded header and payload.</details>

**4. Does a valid signature prove Alice is still ADMIN in PostgreSQL?**
<details><summary>Answer</summary>No. A token's old signed roles may be stale unless your design performs freshness/revocation checks.</details>

**5. Does `UserDetailsService` run for every JWT request?**
<details><summary>Answer</summary>Not normally for a self-contained resource-server path. It runs during password login; a later JWT request can construct Authentication from validated claims.</details>

**6. Who creates `Authentication` on a later JWT request?**
<details><summary>Answer</summary>Spring's bearer-token authentication provider using JwtDecoder and JwtAuthenticationConverter, producing a trusted JwtAuthenticationToken.</details>

**7. Where is the `SecurityContext` between JWT requests?**
<details><summary>Answer</summary>In this stateless lab it is not stored in a server HTTP session; it is rebuilt and cleared for each request.</details>

**8. Why reject an expired but correctly signed JWT?**
<details><summary>Answer</summary>Its `exp` violates the acceptance policy. Authentic past issuance does not grant unlimited validity.</details>

**9. Why validate `aud`?**
<details><summary>Answer</summary>To stop a token issued for another API from being accepted here.</details>

**10. What does `iss` mean?**
<details><summary>Answer</summary>Issuer: the identity of the trusted token issuer expected by the verifier.</details>

**11. Why doesn't client-side logout revoke a copied JWT?**
<details><summary>Answer</summary>The server may still accept its signature/claims until expiry or explicit revocation.</details>

**12. Does JWT automatically remove CSRF?**
<details><summary>Answer</summary>No. CSRF depends on automatically attached browser credentials. Cookie-carried JWTs can require CSRF defense.</details>

**13. If you put JWT in an HttpOnly cookie, what changes?**
<details><summary>Answer</summary>Scripts cannot directly read the cookie, but browsers may automatically send it, bringing cookie/CSRF design back. XSS is still dangerous.</details>

**14. What's the primary difference between access and refresh tokens?**
<details><summary>Answer</summary>Access tokens are presented to APIs; refresh tokens are presented to a refresh endpoint to obtain new access credentials.</details>

**15. Does OAuth2 require JWT? Does JWT require OAuth2?**
<details><summary>Answer</summary>No to both. OAuth2 may use opaque tokens, and JWTs can be used outside OAuth2.</details>

## Self-test barrier — 25 review/interview questions

> **STOP.** Close this section, write your answers without notes, then open your sources to check. Do not advance if you cannot trace each request unaided.

1. What is the single biggest difference between Session and bearer-JWT authentication?
2. Identify which parts of login are **exactly reused**.
3. What replaces `HttpSession` and `JSESSIONID` continuity?
4. Define JWT, bearer, `Authentication`, and authorization separately.
5. Decode the meaning of the three JWT segments.
6. Why is Base64URL not encryption?
7. Which claims are registered, and which are app-defined?
8. What does `sub` represent, and what makes it trustworthy?
9. Explain `iss` vs `aud` using two APIs.
10. Why enforce `exp` *presence* as well as validate its value?
11. Distinguish `iat`, `nbf`, `exp`, and clock skew.
12. What do HS256 and RS256 differ in about signing/verification power?
13. What exact mechanism creates `Authentication` from a JWT?
14. What maps `roles=["ADMIN"]` to `ROLE_ADMIN`?
15. Why is the default JWT `SCOPE_` mapping not enough here?
16. Does a valid JWT always trigger a PostgreSQL user lookup?
17. Which JWT failures cause `401` versus `403`?
18. Can Session authorities also become stale? How?
19. What security property does a signature *not* supply?
20. When is disabling CSRF defensible? When is it unsafe?
21. Compare browser memory, HttpOnly cookie, and localStorage.
22. Why is logout harder with a self-contained access token?
23. Describe two immediate revocation choices and their costs.
24. Distinguish opaque bearer, JWT bearer, OAuth2, OIDC, and ID token.
25. Would you always choose JWT over a server session? Justify with requirements.

### Readiness review

You are ready for the exercise when you can explain without notes: **header.payload.signature; claims; trusted signing/verification; `iss`, `aud`, `exp`; bearer replay; issuing after password login; validating and rebuilding `SecurityContext` per request; custom role conversion; CSRF/XSS/token storage; access/refresh distinction; logout/revocation limits; and why JWT is a trade-off rather than a universal upgrade**.

### Future study (not needed to master basic JWT)

OAuth2 grants and Authorization Server; OIDC/ID tokens; social login; identity providers (Keycloak, Auth0, Entra ID); SSO; access+refresh token architecture; refresh rotation and reuse detection; server-side revocation; JWKS and key rotation; multi-resource-server trust; external identity providers; fine-grained authorization; proof-of-possession tokens.

## Final mental model — remember this

```text
SESSION
login → verify password → Authentication → save context server-side → JSESSIONID
later → JSESSIONID → restore Authentication → authorize

JWT
login → verify password → Authentication → sign access JWT → client receives token
later → Bearer JWT → validate signature + policy → reconstruct Authentication → authorize
```

**Not disappearing:** `AuthenticationManager` (for password login), `UserDetailsService`, `PasswordEncoder`, `SecurityContext`, roles, authorities, `401`, `403`, and `@PreAuthorize`. **Biggest change: how authenticated identity survives between HTTP requests.**

### Source/version references

- Attached source: `Spring_Security_Essential_Concepts(3).md` and `Spring_Security_Exercise(2).md` (documented Book Catalog snapshot, **not a live repo**).
- Spring Security 7.1.1 [resource server and JWT](https://docs.spring.io/spring-security/reference/7.1/servlet/oauth2/resource-server/jwt.html), [OAuth2 module overview](https://docs.spring.io/spring-security/reference/servlet/oauth2/), [JWT converter API](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/server/resource/authentication/JwtAuthenticationConverter.html), [JWT validators](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/jwt/JwtValidators.html).
