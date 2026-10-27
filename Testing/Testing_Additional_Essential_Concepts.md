# Testing — Additional Essential Concepts for Java Spring Boot Developers

> **Supplement to:** `Testing_Essential_Concepts(3).md` (43 sections, reviewed in full)  
> **Audience:** Java 21 / Spring Boot 4.1 backend developer who already knows JUnit Jupiter, Mockito, MVC/JPA slices, PostgreSQL, Spring Security, Maven, JaCoCo, and CI.  
> **Goal:** Learn the next **high-value** techniques without starting a different testing curriculum.  
> **Example status:** Illustrative code is identified as either **complete teaching example** (shown source files and imports) or **integration recipe** (requires an existing application/configuration). No benchmark numbers or passing build results are asserted.  
> **Priority:** P0 = learn now; P1 = learn next; P2 = learn on demand.

## 1. Introduction

Your original guide provides the correct baseline: specify expected behavior; choose a *smallest sufficient testing boundary*; use real PostgreSQL only where its behavior matters; and be honest about what each test proves. This supplement deliberately **does not reteach** `@Mock`, `@MockitoBean`, `@DataJpaTest`, ordinary `@Transactional` rollback, CSRF, or JaCoCo mechanics. Instead, it extends them to problems that arise when a backend uses the clock, parallel execution, remote services, evolving APIs, and repeated operations.

### Learning outcomes

After the P0/P1 lessons, you should be able to: freeze time in tests; coordinate asynchronous work without sleeping; replace an external HTTP service with a deterministic local stand-in; distinguish real HTTP tests from MockMvc; verify database concurrency protections; evaluate whether assertions catch injected bugs; generate input cases from invariants; detect contract-breaking API changes; verify Flyway *upgrades* rather than only fresh installs; and test idempotent writes under retries.

**How to study:** Read one lesson, predict the result, implement its exercise in an isolated mini-project or your Book Catalog, then answer its *what it proves / doesn't prove* questions. Do not implement every tool immediately.

## 2. Existing Knowledge Assessment

### 2.1 What the original guide covers well

| Existing sections | Established competency | Why it is **not** repeated |
|---|---|---|
| 1–5 | Requirements → scenarios/cases; equivalence classes; boundaries; decision tables; state transitions; AAA; TDD; avoiding flaky tests | Already detailed; we use these techniques as prerequisites |
| 6–8 | JUnit Jupiter, lifecycle, parameterized assertions, exception assertions | Already demonstrated; used throughout |
| 9–11 | Test doubles, Mockito, captured interactions, unit-test boundaries | Already detailed; reuse for external collaborators |
| 12–19 | Spring context, slices, MVC JSON, validation, advice, mock-vs-real components | Already detailed; extend only to *real HTTP* / external clients |
| 20–25 | PostgreSQL, JPA flush/clear, database constraints, Testcontainers, fresh Flyway migrations | Solid for **new** schemas; lacks an upgrade-from-existing-data recipe |
| 26–27 | Transactions, proxies, commit/rollback and `REQUIRES_NEW` | Solid foundation; add simultaneous transactions and locking |
| 28–34 | URL/method security, CSRF, real login/session/logout, password hashing | Strong coverage; not reteaching |
| 35–43 | Fixtures, isolation, Maven, coverage, CI, debugging, architecture map | Strong foundation; deepen test effectiveness and nondeterminism |

### 2.2 Knowledge-gap audit

**Classification rule:** “Partially Covered” means the original mentions the concern but does not teach enough to apply it independently. “Not Covered” means no substantive executable technique is provided. Priorities describe general Java backend value, **not** an obligation to install every library.

| Concept | Existing coverage | Priority | Recommendation | Evidence and reason |
|---|---|---|---|---|
| Deterministic time / expiration | Partially Covered | **P0** | Deep lesson | §5 warns about uncontrolled clocks but has no injected `Clock` test |
| Asynchronous completion / eventual consistency | Partially Covered | **P0** | Deep lesson | §5 mentions `Future`/latch/Awaitility but no coherent implementation |
| External HTTP dependency testing | Not Covered | **P0** | Deep lesson | MVC tests exercise *incoming* requests, not outgoing client behavior |
| Real-server HTTP testing | Partially Covered | **P0** | Deep lesson | §14 describes `RANDOM_PORT`, but no actual network test |
| Concurrency / lost updates / locking | Partially Covered | **P1** | Deep lesson | §20 mentions optimistic locking; §27 transaction propagation; no simultaneous actors |
| Mutation testing / effectiveness of assertions | Partially Covered | **P1** | Deep lesson | §43 calls mutation optional; JaCoCo coverage cannot measure bug detection |
| Property-based testing | Partially Covered | **P1** | Deep lesson | §43 mentions it, without generators, properties, shrinking or examples |
| API contract / backward compatibility | Not Covered | **P1** | Deep lesson | Controller assertions alone don't establish consumer compatibility |
| Flyway upgrade testing for populated schemas | Partially Covered | **P1** | Deep lesson | §25 explicitly says fresh migration ≠ upgrade from older populated schema |
| Idempotency and replayed writes | Not Covered | **P1** | Deep lesson | Critical when network retries could create duplicate purchases/stock mutations |
| Cache correctness | Not Covered | **P2** | Learn when caching is added | Need real TTL/invalidations when Redis/cache becomes part of the contract |
| Message delivery / outbox / brokers | Not Covered | **P2** | Learn when adding queues/events | Duplicates, ordering, delivery and consumer retry semantics differ by system |
| Consumer-driven contract frameworks (Pact) | Not Covered | **P2** | Learn when multiple independently deployed teams/services exist | Adds cost beyond ordinary API compatibility checks |
| Performance/load/stress testing | Not Covered | **P2** | Learn for measurable SLOs and bottlenecks | JUnit is not a trustworthy load generator |
| Resilience/chaos testing | Partially Covered | **P2** | Learn when using complex distributed calls | Failure injection, bulkheads and circuit breakers require real use cases |
| Browser automation | Not Covered | **P2** | Learn if owning a frontend | Not required for ordinary backend-only learning |

### 2.3 Why these choices?

**P0** solves daily, easy-to-overlook reliability problems with modest tooling. **P1** strengthens tests for teams handling persistence, collaboration, and critical business operations. **P2** is specialized: useful later, but teaching everything now would create a QA course rather than an actionable backend testing supplement.

## 3. Prioritized Additional Topics

| Priority | Lesson | Why it matters | Existing coverage | Effort |
|---|---|---|---|---|
| P0 | 1. Controlled time with `Clock` | Expiration, rate limits, scheduled cutoffs | Partial | Small |
| P0 | 2. Asynchronous execution & eventual results | Work not complete when method returns | Partial | Medium |
| P0 | 3. External HTTP client tests | Timeouts, protocol/response mapping, outages | None | Medium |
| P0 | 4. Real HTTP vs MockMvc | Network/web-server behavior | Partial | Small |
| P1 | 5. Concurrency and database locking | Prevent lost updates and duplicate decisions | Partial | Large |
| P1 | 6. Mutation testing | Discover weak assertions under high coverage | Brief mention | Medium |
| P1 | 7. Property-based testing | Generate broad input families around invariants | Brief mention | Medium |
| P1 | 8. API contracts and compatibility | Avoid breaking callers during API evolution | None | Medium |
| P1 | 9. Flyway upgrade verification | Protect existing, populated production databases | Partial | Medium |
| P1 | 10. Idempotency and retries | Prevent double effects from network retries | None | Medium |

---

## 4. Supplementary Lessons

## Topic 1 — Deterministic Time-Based Testing (P0)

### 1. Intuitive Explanation

Imagine testing a library loan that expires at midnight. If your test calls `Instant.now()` directly, the expected result changes with the wall clock. A test written on Tuesday could behave differently on Wednesday. Instead, give the production code a **controllable clock**: like freezing a movie so the test can inspect the exact moment before, at, and after expiration.

### 2. Formal Definition

A **deterministic time-based test** controls the source of time used by business logic, usually by injecting `java.time.Clock`. The result depends on explicit test inputs, not the machine's current time, timezone, or timing variability.

### 3. Why It Matters

Expiration checks, OTP/session validity, promotion windows, deadlines, and time-based scheduling are vulnerable to boundary mistakes (`>` versus `>=`) and flaky assertions. Using `Thread.sleep()` to reach a deadline makes the test slower, not more rigorous.

### 4. How It Works

```text
Production: injected Clock.systemUTC() → Instant.now(clock)
Test:       injected Clock.fixed(...)   → exact, repeatable Instant

23:59:59 (valid) → 00:00:00 (expires) → 00:00:01 (expired)
```

### 5. Practical Java Example

**Complete teaching example — Production: `ExpiryPolicy.java`**

```java
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ExpiryPolicy {
    private final Clock clock;

    public ExpiryPolicy(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    // Contract: the deadline itself is already expired.
    public boolean isExpired(Instant deadline) {
        return !Instant.now(clock).isBefore(deadline);
    }
}
```

**Complete teaching example — Test: `ExpiryPolicyTest.java`**

```java
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExpiryPolicyTest {
    private static final Instant DEADLINE = Instant.parse("2026-11-01T00:00:00Z");

    private static ExpiryPolicy at(String instant) {
        return new ExpiryPolicy(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    @Test
    void beforeDeadline_isNotExpired() {
        assertThat(at("2026-10-31T23:59:59Z").isExpired(DEADLINE)).isFalse();
    }

    @Test
    void exactlyAtDeadline_isExpired() {
        assertThat(at("2026-11-01T00:00:00Z").isExpired(DEADLINE)).isTrue();
    }

    @Test
    void afterDeadline_isExpired() {
        assertThat(at("2026-11-01T00:00:01Z").isExpired(DEADLINE)).isTrue();
    }
}
```

**Spring wiring recipe — snippet, not a complete application**:

```java
@Bean
Clock appClock() { return Clock.systemUTC(); }
```

Inject `Clock` into your Spring Service constructor. Tests of the pure rule do not need to start Spring.

### 6. Step-by-Step Explanation

The test picks an immutable `DEADLINE`. `Clock.fixed(...)` makes `Instant.now(clock)` repeatable. The **exact boundary** proves the `>=` business decision, not just a nearby happy path. If production changes from `!now.isBefore(deadline)` to `now.isAfter(deadline)`, the equality test catches the regression.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| `ExpiryPolicy`, Java time calculation | Nothing; fixed clock is a real deterministic implementation | No | No | No | No |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Logic at before/equal/after timestamps | Correct scheduling service execution or cron triggering |
| No dependence on wall-clock time | Database timestamps or distributed-server clock agreement |
| Correct inclusive expiry decision | DST handling for rules expressed in local civil time |

### 9. Common Pitfalls

Using `LocalDateTime.now()` deep inside code prevents direct control. Comparing strings representing times can hide timezone errors. `Clock.fixed` does not magically simulate a scheduler firing; it only controls reads from that clock. Use `ZoneId` with real DST cases when rules are local-date-based.

### 10. When to Use It

Whenever a service checks expiration, validity windows, age, deadlines, scheduled eligibility, or time-derived keys. Do not inject a clock into code that has no time dependency.

### 11. Practical Exercises

1. **Basic:** Implement a coupon valid until 17:00 UTC; test one second before, exactly at, after.
2. **Intermediate:** Test a session expiration after a configured `Duration` without sleeping.
3. **Stretch:** Test a *local* 09:00 opening time through a daylight-saving boundary, stating your timezone assumption.

### 12. Mental Model / Key Takeaways

**Time is an input.** Control it like any other test input; never wait for the real world to reach your expected state.

---

## Topic 2 — Asynchronous Testing and Eventual Results (P0)

### 1. Intuitive Explanation

A waiter accepts your order and says “I'll bring it later.” The request **was accepted**, but the meal **has not arrived**. Similarly, an asynchronous method returning `CompletableFuture` doesn't mean the work is complete. A test must wait for or control a *completion signal* before asserting.

### 2. Formal Definition

**Asynchronous testing** verifies results produced after a method returns, without assuming the worker's completion time. **Eventual consistency testing** verifies that an observable state eventually satisfies a requirement within a bounded interval; it is not the same as testing simultaneous data races.

### 3. Why It Matters

`Thread.sleep(1000)` might be too short on CI and unnecessarily long on a fast laptop. Starting a background task and immediately querying the database can report a false failure. Tests need explicit completion semantics and a bounded timeout.

### 4. How It Works

```text
Caller calls service.refreshAsync()
    → returns CompletableFuture immediately
    → executor runs queued work
    → future completes
    → test observes result OR exception
```

### 5. Practical Java Example

**Complete teaching example — Production: `AsyncStockReader.java`**

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntSupplier;

public final class AsyncStockReader {
    private final Executor executor;

    public AsyncStockReader(Executor executor) { this.executor = executor; }

    public CompletableFuture<Integer> readAsync(IntSupplier supplier) {
        return CompletableFuture.supplyAsync(supplier::getAsInt, executor);
    }
}
```

**Complete teaching example — Test: `AsyncStockReaderTest.java`**

```java
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AsyncStockReaderTest {
    static final class ManualExecutor implements Executor {
        private final Queue<Runnable> queue = new ArrayDeque<>();
        public void execute(Runnable task) { queue.add(task); }
        void runNext() { queue.remove().run(); }
    }

    @Test
    void resultIsVisibleOnlyAfterQueuedWorkRuns() {
        ManualExecutor executor = new ManualExecutor();
        AsyncStockReader reader = new AsyncStockReader(executor);

        CompletableFuture<Integer> result = reader.readAsync(() -> 7);
        assertThat(result).isNotDone();

        executor.runNext();
        assertThat(result).isCompletedWithValue(7);
    }
}
```

**Integration recipe:** For a genuinely independent worker, assert a completion `Future` with `get(timeout, TimeUnit.SECONDS)` or poll the **observable business state** with a bounded condition (e.g., Awaitility). Configure the actual executor/queue where relevant; do not replace every async test with the manual executor.

### 6. Step-by-Step Explanation

`ManualExecutor.execute` queues work instead of running it. The test proves the future starts incomplete, triggers exactly one queued task, then verifies the result. This is deterministic because *the test controls scheduling*. It proves the service's interaction with an executor, **not real multi-threaded safety**. A full integration test is needed for worker pools, persistence, or cross-thread context propagation.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| Future creation and code scheduled via `Executor` | No Mockito; controllable executor | No | No | No | No |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Scheduled task controls the returned future's completion | Executor threads can start and finish correctly under load |
| Correct propagated value when executed | Real transaction/SecurityContext propagation |
| No sleeps needed | Eventual database or message-broker visibility |

### 9. Common Pitfalls

Avoid bare `Thread.sleep`, indefinite `future.join()` in infrastructure-dependent tests, and asserting “request accepted” as if it proved processing. Always bound real waits. A preemptive JUnit timeout can run a transactional test on another thread, breaking test-thread rollback expectations; see the Spring TestContext transaction documentation. For Spring `@Async`, verify it is invoked **through a proxied bean**, not `this.method()`.

### 10. When to Use It

For `CompletableFuture`, `@Async`, email/notification jobs, scheduled work, and eventual persistence after message processing. Start with completion futures; use polling only when no completion signal is exposed.

### 11. Practical Exercises

1. **Basic:** Add an error case where the supplier throws; assert the returned future completes exceptionally.
2. **Intermediate:** Use `CountDownLatch` to coordinate an actual two-thread task without sleeps.
3. **Stretch:** Add a background projection update and assert eventual database state with a bounded timeout; state who owns the transaction.

### 12. Mental Model / Key Takeaways

**An accepted request ≠ finished work.** Assert the completion contract, and control the scheduler where possible.

---
## Topic 3 — Testing Outgoing HTTP Dependencies (P0)

### 1. Intuitive Explanation

Imagine your Book Catalog calls a supplier's inventory API. A mock `SupplierClient` in a Service unit test can return “7 books,” but it cannot tell you if the real client sends the correct URL, handles HTTP 503, or times out safely. A **local HTTP stub server** plays the role of the supplier without relying on the internet.

### 2. Formal Definition

An **HTTP client integration/adapter test** executes the actual client code against a controlled HTTP endpoint, asserting request shape and response/error mapping. WireMock is one tool for implementing that endpoint in a test.

### 3. Why It Matters

Production clients fail through protocol mismatches, status handling, malformed JSON, timeouts, connection failures, and unexpected response fields. Unit-mocking `SupplierClient` makes all of these invisible.

### 4. How It Works

```text
BookService → real SupplierHttpClient → HTTP → WireMock local server
                                               ↳ response: 200 / 404 / 503 / delay
```

### 5. Practical Java Example

**Complete teaching example — Production: `SupplierHttpClient.java`** (uses Java 21's built-in HTTP client)

```java
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class SupplierHttpClient {
    private final HttpClient http;
    private final String baseUrl;

    public SupplierHttpClient(HttpClient http, String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl;
    }

    // Teaching contract: plain-text body such as "7"; not a JSON supplier API.
    public int stock(String isbn) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/stock/" + isbn))
                .timeout(Duration.ofSeconds(2))
                .GET().build();
        HttpResponse<String> response = http.send(
                request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Supplier unavailable");
        }
        return Integer.parseInt(response.body());
    }
}
```

**Complete teaching example — Test: `SupplierHttpClientTest.java`** (with WireMock 3.x)

```java
import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@WireMockTest
class SupplierHttpClientTest {
    @Test
    void readsStockFromRealHttpResponse(WireMockRuntimeInfo info) throws Exception {
        stubFor(get(urlEqualTo("/stock/BOOK-1"))
                .willReturn(ok("7")));
        SupplierHttpClient client = new SupplierHttpClient(
                HttpClient.newHttpClient(), info.getHttpBaseUrl());
        assertThat(client.stock("BOOK-1")).isEqualTo(7);
        verify(getRequestedFor(urlEqualTo("/stock/BOOK-1")));
    }

    @Test
    void upstream503IsReportedAsFailure(WireMockRuntimeInfo info) {
        stubFor(get(urlEqualTo("/stock/BOOK-2"))
                .willReturn(aResponse().withStatus(503)));
        SupplierHttpClient client = new SupplierHttpClient(
                HttpClient.newHttpClient(), info.getHttpBaseUrl());
        assertThatThrownBy(() -> client.stock("BOOK-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Supplier unavailable");
    }
}
```

**Maven test dependency** (verified stable 3.x teaching baseline, in addition to JUnit/AssertJ):

```xml
<dependency>
  <groupId>org.wiremock</groupId>
  <artifactId>wiremock</artifactId>
  <version>3.13.2</version>
  <scope>test</scope>
</dependency>
```

This exact teaching adapter concatenates a controlled ISBN into a path. **Production code** should encode arbitrary URI path segments properly, configure reasonable deadlines and handle `InterruptedException` according to the caller's contract. The example intentionally uses plain text to isolate HTTP behavior.

### 6. Step-by-Step Explanation

`@WireMockTest` starts a local HTTP server on an available port. `stubFor(...)` declares what it returns for `/stock/BOOK-1`; the **real Java HttpClient** performs a network request to that local server. `verify(...)` checks the request reached the expected route. The 503 case asserts a meaningful exception rather than silently returning zero stock (which could be mistaken for real inventory).

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| Actual HTTP client and local TCP request | Supplier's remote system replaced by WireMock | No | No | No | **Yes**, local stub server |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Outgoing URL, HTTP success/error mapping | Actual supplier uptime or its current public API |
| Real HTTP client works with a local server | TLS/reverse-proxy configuration of an external deployment |
| Correct behavior on simulated 503 | Retry/backoff behavior unless tested explicitly |

### 9. Common Pitfalls

Don't confuse **mocking an interface** with **simulating an HTTP server**. Avoid live third-party APIs in routine CI (flaky, rate-limited, potentially expensive). Stub what the contract states—not the behavior you wish the provider had. Use explicit error assertions; avoid returning fabricated successes on network failures. In Spring Boot 4.1, `@RestClientTest` and `MockRestServiceServer` are alternative focused options for testing `RestClient` without a real TCP server; Spring's reference prefers mock web servers for fuller transport-level behavior.

### 10. When to Use It

Use when integrating payment, shipping, exchange-rate, identity, or inventory APIs. Interface mocks remain useful for *business-rule* unit tests; add a small number of real-client tests for protocol boundaries.

### 11. Practical Exercises

1. **Basic:** Add a 404 response test with a distinct business-specific exception.
2. **Intermediate:** Stub a delayed response and assert the configured request timeout (use a generous CI-safe bound, not a brittle millisecond benchmark).
3. **Stretch:** Send and verify JSON, including a required request header; exercise a malformed JSON response.

### 12. Mental Model / Key Takeaways

**Mocked Java collaborator** proves Service decisions. **Local HTTP stub server** proves the actual HTTP adapter. Neither proves the remote vendor's production environment.

---

## Topic 4 — Testing a Real HTTP Server, Not Only MockMvc (P0)

### 1. Intuitive Explanation

MockMvc is like a driving simulator: it verifies much of Spring MVC's decision-making without leaving the test process. A `RANDOM_PORT` test starts a real embedded HTTP server and calls it over a local socket. That can reveal issues tied to transport, servlet container and HTTP-client behavior.

### 2. Formal Definition

A **running-server integration test** starts the application's embedded web server and sends actual HTTP requests through a client to an ephemeral port. It is broader than MockMvc but not automatically a full browser or deployment End-to-End Test.

### 3. Why It Matters

MockMvc does not create actual network connections or exercise real client transport behavior. This matters for response headers, cookie transport, streaming, filters/servlet-container effects, and server-side concurrency.

### 4. How It Works

```text
MockMvc test:  mock request → Spring MVC / filters (no socket)
Real HTTP test: Java HttpClient → localhost:randomPort → embedded server → Spring MVC
```

### 5. Practical Java Example

**Integration recipe — requires an existing Spring Boot app with a public `GET /api/ping` endpoint returning literal `pong` and a web server starter.** (The endpoint/contract is **illustrative**, not an existing Book Catalog claim.)

**Production snippet (inside an application-scanned controller):**

```java
@RestController
class PingController {
    @GetMapping("/api/ping")
    String ping() { return "pong"; }
}
```

**Test class — enough context to adapt in a Spring Boot 4.1 application:**

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PingRealHttpIT {
    @Value("${local.server.port}")
    int port;

    @Test
    void servesActualHttpRequest() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/ping"))
                .timeout(Duration.ofSeconds(5)).GET().build();
        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("pong");
    }
}
```

If the application has security, make the route public in its actual `SecurityFilterChain` or use valid authentication: **do not globally disable security** just for this test. This example uses Java's HTTP client to avoid depending on a version-specific Boot test client. Spring Boot 4.1 also documents `@AutoConfigureRestTestClient` with `RestTestClient` for running-server tests.

### 6. Step-by-Step Explanation

`RANDOM_PORT` starts a server on an available local port; `local.server.port` supplies the chosen value. `HttpClient.send(...)` performs an actual request. The test asserts status and body, rather than merely asserting the endpoint method can be called directly. No hard-coded port avoids collisions.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| Boot web server, client, MVC | None for this endpoint | **Full** | Only if your app needs it | Only if app's test DB uses Testcontainers | **Yes** |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Local embedded server listens and returns specified response | TLS, proxy, load balancer, DNS, browser behavior |
| Network request crosses HTTP transport | Production deployment configuration |
| Selected headers/body can be observed end-to-end within the test process | Every cross-layer use case |

### 9. Common Pitfalls

**Transaction warning:** with `RANDOM_PORT`, the HTTP server handles requests on a different thread, so an outer test `@Transactional` **does not normally roll back the server's committed changes**. Clean real records explicitly. Avoid hard-coded ports and calling this “browser E2E.” Do not use a real server for pure validation functions easily checked by a MVC slice.

### 10. When to Use It

For a few critical cross-boundary smoke tests, session-cookie behavior, network-facing API details, and transport-specific bugs. Keep the bulk of controller tests on MockMvc.

### 11. Practical Exercises

1. **Basic:** Return JSON `{"status":"UP"}` from an illustrative endpoint and assert status, content type and a parsed field.
2. **Intermediate:** Call a protected endpoint without credentials, then with an authenticated test session through HTTP (not a synthetic mock identity).
3. **Stretch:** Compare the same endpoint with MockMvc and `RANDOM_PORT`; list what only the second test establishes.

### 12. Mental Model / Key Takeaways

**MockMvc checks application HTTP semantics without a socket.** `RANDOM_PORT` adds the embedded server and real HTTP transport, but not your production network.

---

## Topic 5 — Concurrency Tests, Lost Updates and Database Locking (P1)

### 1. Intuitive Explanation

Two customers try to buy the last copy of a book at the same moment. Both initially see stock = 1; both decide “available”; both write. A sequential test might always pass, while the actual system oversells. Concurrency tests create overlapping operations to expose **interleaving-sensitive** defects.

### 2. Formal Definition

**Concurrency testing** exercises interacting operations with overlapping lifetimes and verifies invariants (e.g., stock never negative, no lost update, one successful reservation for the final item). It includes JVM thread safety and database concurrency control such as optimistic version checks and row locks.

### 3. Why It Matters

A data race is an unsafe interaction on shared memory; a database *lost update* is two transactions overwriting one another's changes. They are related risks but solved at different boundaries. Running a test 100 times does **not** prove absence of a race.

### 4. How It Works

```text
T1: reads stock=1 ─────── writes stock=0
T2:      reads stock=1 ─────── writes stock=0  (both claimed last copy)

Safer: atomic conditional UPDATE ... WHERE stock >= quantity
       OR versioned optimistic update + defined retry/conflict response
       OR correctly scoped SELECT ... FOR UPDATE transaction
```

### 5. Practical Java Example

**Complete teaching example — pure Java lost-update-shaped counter test.** This tests a JVM concurrency contract, not PostgreSQL.

**Production: `SafeCounter.java`**

```java
import java.util.concurrent.atomic.AtomicInteger;

public final class SafeCounter {
    private final AtomicInteger count = new AtomicInteger();
    public void increment() { count.incrementAndGet(); }
    public int value() { return count.get(); }
}
```

**Test: `SafeCounterTest.java`**

```java
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SafeCounterTest {
    @Test
    void concurrentIncrementsAreNotLost() throws Exception {
        SafeCounter counter = new SafeCounter();
        int workers = 8;
        CountDownLatch start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(workers)) {
            var tasks = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < workers; i++) {
                tasks.add(pool.submit(() -> {
                    try {
                        if (!start.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Workers did not start");
                        }
                        for (int n = 0; n < 1_000; n++) counter.increment();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }));
            }
            start.countDown();
            for (var task : tasks) task.get(10, TimeUnit.SECONDS);
        }
        assertThat(counter.value()).isEqualTo(8_000);
    }
}
```

**PostgreSQL integration recipe — schema and transaction semantics must be verified in your app:**

```sql
-- Atomic decrement: affected row count must equal 1 for a successful reservation.
UPDATE books
SET stock = stock - :quantity
WHERE id = :bookId AND stock >= :quantity;
```

Run two concurrent **committed** service calls against one Book with stock 1 and quantity 1, each in its own transaction. Assert: exactly one successful reservation, final stock 0, no negative stock, and expected distinct responses for losing caller. In JDBC, examine the update count; in JPA, a properly declared modifying query and its transaction are needed. For optimistic locking (`@Version`), assert the *expected* conflict/rollback behavior, not only final stock. Re-read state in a new transaction; use PostgreSQL, not H2.

### 6. Step-by-Step Explanation

The `start` latch synchronizes worker *release* (not a guarantee of identical execution instants). `Future.get` propagates worker failures back into JUnit. The atomic counter guarantees correct increments for this JVM test. The SQL version goes further: the condition and update execute atomically at the **database boundary**, which is what a real stock-reservation requirement needs.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| JVM threads + atomic counter (shown complete example) | No | No | No | No | No |
| For SQL reservation recipe: real Service/proxy/repository/transactions | Normally none | Full or focused context | **Yes** | Via Testcontainers | Not required |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Atomic increments satisfy invariant in the executed run | Absence of all possible JVM scheduling races |
| If SQL recipe implemented: overlapping requests respect the chosen business invariant | Behavior across all isolation levels or distributed replicas |
| Worker failures are observable in test | No deadlocks or starvation under every production workload |

### 9. Common Pitfalls

Avoid `Thread.sleep()` as a barrier; never assume a passing sequential transaction test proves concurrency safety. Test-level rollback can hide independent committed operations. Don't call PostgreSQL row locks “just JPA annotations”: locking behavior and transaction scope matter. Parallel tests that clean entire tables can delete one another's data; isolate schemas/DBs or serialize destructive suites.

### 10. When to Use It

When multiple users can mutate the same stock, account balance, reservation, allocation, or uniqueness decision; when using `@Version`, `FOR UPDATE`, or independent transactions. Do not build thread tests for a purely single-threaded value-object method.

### 11. Practical Exercises

1. **Basic:** Replace `AtomicInteger` with a non-atomic mutable `int`; explain why the test may or may not expose the race on a particular run.
2. **Intermediate:** Add a `version` field and test a stale concurrent update raises the configured optimistic-lock exception.
3. **Stretch:** Test two simultaneous attempts to reserve the last stock item against real PostgreSQL, assert one succeeds and the other follows the documented failure contract; repeat selectively to increase confidence without claiming proof.

### 12. Mental Model / Key Takeaways

**A sequential test tests ordering; a concurrency test tests overlapping actors.** Use synchronization for reproducibility and a real database to establish database behavior.

---
## Topic 6 — Mutation Testing and Test Effectiveness (P1)

### 1. Intuitive Explanation

Imagine a guard tests whether somebody may enter a room. They see every visitor (**coverage**), but would they stop someone carrying a fake badge? **Mutation testing** deliberately changes a tiny rule in production code and checks whether your tests notice the wrong behavior.

### 2. Formal Definition

A **mutant** is a deliberately altered version of production bytecode/source behavior, e.g., changing `>=` to `>` or changing a returned boolean. A mutant is **killed** when tests fail due to the change; it **survives** if the relevant tests still pass. A **mutation score** measures detected mutants under the tool's reporting conventions; equivalent mutants (changes that cannot alter observable behavior) complicate interpretation.

### 3. Why It Matters

`assertNotNull(result)` can execute every line in a method while failing to notice wrong amounts, statuses, or permissions. Mutation testing evaluates *how sensitive your tests are to plausible defects*, not just which bytecode executed.

### 4. How It Works

```text
Original:      return stock >= requested;
Mutant:        return stock > requested;
Test (10,3):   passes both → mutant survives
Test (5,5):    original true, mutant false → test fails → mutant killed
```

### 5. Practical Java Example

**Complete teaching example — Production: `StockRule.java`**

```java
public final class StockRule {
    public boolean canBuy(int stock, int requested) {
        return requested > 0 && stock >= requested;
    }
}
```

**Complete teaching example — Test: `StockRuleTest.java`**

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StockRuleTest {
    private final StockRule rule = new StockRule();

    @Test
    void exactStockIsAllowed() {
        assertThat(rule.canBuy(5, 5)).isTrue();
    }

    @Test
    void zeroQuantityIsRejected() {
        assertThat(rule.canBuy(5, 0)).isFalse();
    }

    @Test
    void tooLargeQuantityIsRejected() {
        assertThat(rule.canBuy(5, 6)).isFalse();
    }
}
```

**PIT Maven integration recipe — plugin block within `build/plugins`, not a new project:**

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.19.4</version>
  <dependencies>
    <dependency>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-junit5-plugin</artifactId>
      <version>1.2.3</version>
    </dependency>
  </dependencies>
  <configuration>
    <targetClasses>
      <param>your.package.StockRule*</param>
    </targetClasses>
  </configuration>
</plugin>
```

Change `your.package` to your actual package; the shown compatible version pair is a **teaching baseline**, not necessarily the newest. JUnit Platform compatibility must be checked against your Spring Boot-managed versions. Run as an optional, focused exercise:

```powershell
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

### 6. Step-by-Step Explanation

PIT finds eligible production methods, creates mutations, executes relevant tests against mutants, and reports detected/surviving modifications. The equality test kills the `>=` → `>` mutant; the zero-quantity test detects removal of the positivity guard. A surviving mutant invites a question: missing assertion? equivalent change? unreachable behavior? undesirable generated mutation? Investigation matters more than chasing 100%.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| Pure `StockRule`, JUnit tests, PIT runner | None | No | No | No | No |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Selected tests detect selected injected changes | Every possible bug is detectable |
| Boundary assertions are meaningful for mutated operators | Database wiring, concurrency, authentication |
| Identifies weak assertions for selected code | Equivalent mutants can be killed; always complete fault model |

### 9. Common Pitfalls

Don't mutate the full Spring integration suite on the first attempt (slow and noisy). Don't increase mutation score by adding tests that only assert implementation details. Don't assume every survivor indicates a real defect; classify equivalent/unreachable mutants thoughtfully. PIT mutates candidates for testing rather than rewriting your shipped source code.

### 10. When to Use It

For high-risk pure business rules (prices, eligibility, authorization decisions) with reasonably fast tests. Run selectively, e.g., before refactoring critical logic or during a quality review.

### 11. Practical Exercises

1. **Basic:** Delete `exactStockIsAllowed`; predict which relational-operator mutation survives.
2. **Intermediate:** Add a tax/discount rule and test which mutants your existing assertions kill.
3. **Stretch:** Run PIT on one Service with mocked collaborators; classify three survivors (missing test, equivalent, irrelevant configuration).

### 12. Mental Model / Key Takeaways

**Coverage = was the code visited? Mutation = would the tests notice a plausible wrong decision?** Neither is a universal correctness proof.

---

## Topic 7 — Property-Based Testing (P1)

### 1. Intuitive Explanation

Example-based testing asks: “Does adding 3 and 4 give 7?” Property-based testing asks: “For *many generated* valid inputs, does swapping the operands leave addition unchanged?” It checks a **rule that should always hold**, rather than a small hand-picked set.

### 2. Formal Definition

**Property-Based Testing (PBT)** generates many inputs within defined domains and verifies properties/invariants. Tools such as **jqwik** use generators, edge-case selection, replayable random seeds and **shrinking** (simplifying a failing generated input to an easier counterexample).

### 3. Why It Matters

Data combinations grow quickly. PBT is valuable for parsers, transformations, normalization, sorting, money/quantity rules and algebraic invariants. It complements—not replaces—named boundary and regression tests.

### 4. How It Works

```text
Property: remaining stock is never negative after a successful sale
Generator → (stock=100, qty=23), (stock=0, qty=1), ...
                    ↓
             Evaluate property
                    ↓
       Failure → shrink to a simpler case → replay
```

### 5. Practical Java Example

**Complete teaching example — Production: `PurchaseRule.java`**

```java
import java.util.OptionalInt;

public final class PurchaseRule {
    public OptionalInt remaining(int stock, int quantity) {
        if (stock < 0 || quantity <= 0 || quantity > stock) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(stock - quantity);
    }
}
```

**Complete teaching example — Test: `PurchaseRuleProperties.java` (jqwik)**

```java
import java.util.OptionalInt;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import static org.assertj.core.api.Assertions.assertThat;

class PurchaseRuleProperties {
    private final PurchaseRule rule = new PurchaseRule();

    @Property(tries = 200)
    void successfulSalesPreserveConservation(
            @ForAll @IntRange(min = 0, max = 1_000) int stock,
            @ForAll @IntRange(min = 1, max = 1_000) int quantity) {
        OptionalInt result = rule.remaining(stock, quantity);
        if (result.isPresent()) {
            int remaining = result.getAsInt();
            assertThat(remaining).isGreaterThanOrEqualTo(0);
            assertThat(remaining + quantity).isEqualTo(stock);
        }
        if (quantity > stock) {
            assertThat(result).isEmpty();
        }
    }
}
```

**Maven dependency — add jqwik test engine in a JUnit Platform project:**

```xml
<dependency>
  <groupId>net.jqwik</groupId>
  <artifactId>jqwik</artifactId>
  <version>1.10.2</version>
  <scope>test</scope>
</dependency>
```

This version is from the verified current guide consulted for this document. **Check the jqwik/JUnit Platform engine compatibility against your effective Spring Boot 4.1 dependency tree** before treating it as a drop-in configuration. The pure rule and property need no Spring context. Run it under a JUnit Platform-capable test runner.

### 6. Step-by-Step Explanation

`@Property(tries = 200)` invokes the test for multiple generated value pairs; `@IntRange` constrains inputs so `quantity` is positive. The test verifies two **independent properties**: non-negative remainder and quantity conservation when accepted. When demand exceeds stock, the outcome must be empty. If a generated case fails, jqwik reports the case and tries to shrink it. The test still needs a **human-designed property**; the tool cannot discover requirements for you.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| `PurchaseRule`, jqwik generator, assertions | None | No | No | No | No |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Properties hold across the generated tested input sample | Every mathematically possible input was explored |
| Invariants are independently asserted | A correct oracle for a requirement you never specified |
| Shrinkable counterexample aids debugging | Persistent database constraints or concurrent stock updates |

### 9. Common Pitfalls

Don't write a property that merely reproduces the production algorithm on the expected side. Avoid huge numbers of discarded inputs (`Assume.that`) by generating valid domains directly. Generated testing is sampled, not exhaustive. Keep a failing seed/counterexample so the failure can be reproduced and convert important discoveries into named regression cases.

### 10. When to Use It

When a rule has a simple invariant and large input domain (sorting, encoding, normalization, integer/decimal calculations). Skip it for a simple one-off status check with a handful of discrete scenarios.

### 11. Practical Exercises

1. **Basic:** Write a property asserting `normalize(normalize(s)) == normalize(s)` for a normalization function.
2. **Intermediate:** Create generated quantity inputs including 0 and negatives; verify rejection.
3. **Stretch:** Deliberately introduce an off-by-one bug and inspect the shrunk failing case/seed.

### 12. Mental Model / Key Takeaways

**Examples check named cases; properties check rules over generated cases.** Both require a correct, independent oracle.

---

## Topic 8 — API Contract Testing and Compatibility (P1)

### 1. Intuitive Explanation

Your API returns a book with `id` and `title`. A mobile app expects `title`. You refactor the backend to rename it to `name` without changing your own client. Your backend tests may all pass, while the mobile app breaks. A **contract** makes the agreed request/response shape explicit.

### 2. Formal Definition

An **API contract** specifies observable behavior between a provider and consumer: routes, methods, fields, types, status codes, media types, authentication assumptions and errors. **Contract tests** check that one or both sides honor it. **Backward compatibility** means existing supported consumers can continue working after a provider change.

### 3. Why It Matters

Unit tests validate internal logic; controller tests might prove response content under your assumptions. Contract validation checks those assumptions against a **published or consumer-verified agreement**. Particularly important with separate frontend/mobile teams and microservices.

### 4. How It Works

```text
Mobile consumer expects: GET /api/books/42 → 200 {"id":42,"title":"..."}
                      |
                      v
Provider contract test verifies: route/status/field name/type
                      |
                      v
Change title→name without compatibility → test FAILS
```

### 5. Practical Java Example

**Integration recipe — illustrative Spring MVC endpoint: production types shown, security and application configuration omitted.**

```java
// Production: BookReadController.java (within a scanned Spring MVC application)
import org.springframework.web.bind.annotation.*;

@RestController
class BookReadController {
    record BookView(long id, String title) {}

    @GetMapping("/api/books/{id}")
    BookView get(@PathVariable long id) {
        return new BookView(id, "Clean Code"); // teaching fixture only
    }
}
```

```java
// Test: BookReadContractTest.java (MVC slice; assumes app's actual security permits this GET)
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookReadController.class)
class BookReadContractTest {
    @Autowired MockMvc mvc;

    @Test
    void maintainsPublishedBookResponse() throws Exception {
        mvc.perform(get("/api/books/42"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").isString());
    }
}
```

This is a **provider-side schema smoke test**, not yet a complete independent consumer-driven contract. To achieve true independent contract enforcement, compare the response to a separately maintained specification (e.g., OpenAPI) or consumer expectations, and include missing/invalid request and error cases. A security-enabled Boot 4.1 MVC slice may need the real security configuration, `@WithMockUser`, or an explicit public-read rule; do **not** globally switch filters off in a security-policy test.

### 6. Step-by-Step Explanation

A published response must retain `title` as a string. Renaming it to `name` causes the selected JSON assertion to fail. **Adding** a new optional field may remain backward compatible; removing a required field or changing its type usually isn't. But compatibility depends on the contract and consumer parsing behavior, so never assume all additions are safe for all consumers.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| Controller, mapping and JSON converter | None in teaching controller | MVC slice | No | No | No (MockMvc) |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Selected published fields and status remain available | Consumer app actually deserializes and uses the entire response correctly |
| Provider's MVC mapping matches the test's expectations | Consumer-driven verification or all backward-compatibility guarantees |
| Basic JSON serialization | Real database or proxy/cookie network behavior |

### 9. Common Pitfalls

A self-authored “contract test” that simply mirrors a breaking implementation is a weak contract. Keep the specification independently reviewed. Avoid snapshotting complete responses when incidental fields vary; verify required fields, types, error shapes and documented compatibility policies. Consumer-driven contract frameworks (e.g., Pact) become more valuable when teams deploy independently, but add operational overhead.

### 10. When to Use It

When your API has separate frontend/mobile clients, public consumers, or other services; especially before releases with DTO or endpoint changes. A simple API with one tightly coupled client may be served by published OpenAPI + focused compatibility tests before adopting Pact.

### 11. Practical Exercises

1. **Basic:** Change `title` to `name`; show which published-response test fails.
2. **Intermediate:** Add a documented `404` error contract and test type/status/content fields.
3. **Stretch:** Keep a small separate JSON-schema/OpenAPI contract and validate a provider response against it; list what your validator checks and omits.

### 12. Mental Model / Key Takeaways

**API test:** Does our API behave as *we tested*? **Contract test:** Does it honor the *agreed interface* that callers depend upon?

---
## Topic 9 — Testing Flyway Upgrades from Populated Schemas (P1)

### 1. Intuitive Explanation

A fresh test database is like a brand-new apartment: everything can be arranged without disturbing anyone. A production migration is a renovation **while people and furniture already exist**. Creating a fresh schema successfully doesn't prove an upgrade preserves existing rows or handles old null values.

### 2. Formal Definition

An **upgrade migration test** begins with a real schema at an earlier supported version and representative data, applies subsequent migration scripts using the production migration mechanism, then verifies schema correctness **and data preservation/transformation**. This extends—but does not replace—the fresh-install Flyway tests in §25.

### 3. Why It Matters

Adding a `NOT NULL` column to populated rows may fail; renaming a field may destroy data; a backfill may mis-map old values. The fresh installation path may never expose these conditions.

### 4. How It Works

```text
Disposable PostgreSQL
  → Flyway migrate to old version V1
  → Insert representative legacy rows
  → Flyway apply V2..Vlatest
  → Assert: existing rows preserved, new constraints hold, Hibernate maps schema
```

### 5. Practical Java Example

**Integration recipe — source migrations (`src/main/resources/db/migration`)**. These are **teaching SQL examples**, not a replacement for any existing production migration.

**`V1__create_books.sql`:**

```sql
CREATE TABLE books (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(40) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL
);
```

**`V2__add_stock.sql`:**

```sql
ALTER TABLE books ADD COLUMN stock INTEGER;
UPDATE books SET stock = 0 WHERE stock IS NULL;
ALTER TABLE books ALTER COLUMN stock SET NOT NULL;
ALTER TABLE books ADD CONSTRAINT ck_books_stock_nonnegative CHECK (stock >= 0);
```

**Representative legacy fixture and post-upgrade verification:**

```sql
-- Execute this ONLY after upgrading a disposable DB to V1, before V2.
INSERT INTO books (isbn, title) VALUES ('LEGACY-001', 'Original Title');

-- Execute this AFTER V2 migrations finish.
SELECT title, stock FROM books WHERE isbn = 'LEGACY-001';
-- Contract: ('Original Title', 0)
```

**Flyway test harness recipe** (intentionally not presented as a drop-in complete Java class):

```text
1. Start an isolated PostgreSQL container/test schema.
2. Configure Flyway to apply migrations up to version 1.
3. Insert LEGACY-001 and commit it.
4. Reconfigure Flyway target to latest, then call migrate().
5. Query with JDBC in a new transaction: assert exactly one legacy row,
   preserved title, stock = 0, NOT NULL/CHECK work as expected.
6. Optionally start the Hibernate mapping validator against upgraded schema.
7. Dispose database. Separately test clean migration from empty database.
```

Use the actual Flyway APIs/settings supported by your installed version and PostgreSQL test configuration. Keep application startup from auto-applying *all* migrations **before** the intentional V1 fixture stage, otherwise you are not testing an upgrade.

### 6. Step-by-Step Explanation

The V1 schema accepts a row with no `stock`. V2 adds the new column, backfills old rows to zero, then tightens constraints. A migration that immediately adds `stock INTEGER NOT NULL` without a suitable default/backfill can fail on populated tables. The assertion on `LEGACY-001` establishes preservation of legacy data, not only that a new table definition exists.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| Flyway SQL, JDBC, historical schema and data | Nothing | Optional; run migration before full Boot auto-init | **Yes** | Typically Testcontainers | No |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| One supported upgrade path preserves the selected legacy fixtures | Every historical production version and data shape is handled |
| New constraints work on the migrated schema | Migration is safe under live concurrent traffic/zero-downtime rollout |
| Current mapping can validate against upgraded schema (if checked) | Application deployment orchestration works |

### 9. Common Pitfalls

Don't modify an already-applied migration to “fix” an upgrade; create a new versioned migration according to your Flyway policy. Test migrations against actual PostgreSQL, not H2. Never point destructive setup or migration tests at production/developer data. A clean install test and an upgrade test answer **different questions**. Avoid claiming a migration is zero-downtime safe from a small integration test alone.

### 10. When to Use It

Before releases that modify schemas of applications with real stored data—especially new required columns, changed enum values, renamed/merged tables, and backfills.

### 11. Practical Exercises

1. **Basic:** Create V1 + V2 in a disposable DB and assert the legacy title survives.
2. **Intermediate:** Insert several V1 rows including special strings; verify all are backfilled correctly.
3. **Stretch:** Add V3 with a new unique field; design how duplicates in old data must be resolved **before** uniqueness is enforced.

### 12. Mental Model / Key Takeaways

**Fresh install = Can migrations construct a schema? Upgrade = Can migrations safely transform *existing state*?** Test both paths when production has existing data.

---

## Topic 10 — Idempotency, Duplicate Requests and Retries (P1)

### 1. Intuitive Explanation

A customer clicks “Pay” and the network times out. The client retries. Did the first request actually succeed? If the backend charges twice, the API is broken. An **idempotency key** lets repeated requests for the same intended action reuse a previously determined result rather than repeat a business effect.

### 2. Formal Definition

An operation is **idempotent** if repeating it has the same intended effect as performing it once (within a declared scope and time window). For non-idempotent operations such as creating a payment, servers commonly implement request deduplication keyed by a client-provided **idempotency key**, tied to a canonicalized request identity/fingerprint. Keys reused with *different* payloads should follow a defined conflict contract.

### 3. Why It Matters

Mobile clients, proxies, queues and retry libraries can resend requests due to uncertain outcomes. “No error returned” and “one payment created” are different facts. This topic combines your knowledge of unique constraints, transactions, concurrent writes and state assertions.

### 4. How It Works

```text
Request A: key=K, order=O1 → result R1, one charge
Request B: key=K, order=O1 → return R1, zero new charges
Request C: key=K, order=O2 → conflict, zero new charges
```

### 5. Practical Java Example

**Complete teaching example — in-memory model, NOT production persistence: `IdempotentCheckout.java`**

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class IdempotentCheckout {
    public record Receipt(String id) {}
    private record Saved(String orderId, Receipt receipt) {}

    private final ConcurrentHashMap<String, Saved> byKey = new ConcurrentHashMap<>();
    private final AtomicInteger chargeCount = new AtomicInteger();

    public Receipt charge(String key, String orderId) {
        Saved result = byKey.compute(key, (k, existing) -> {
            if (existing != null) {
                if (!existing.orderId().equals(orderId)) {
                    throw new IllegalArgumentException("Idempotency key reused");
                }
                return existing;
            }
            int sequence = chargeCount.incrementAndGet();
            return new Saved(orderId, new Receipt("R-" + sequence));
        });
        return result.receipt();
    }

    public int charges() { return chargeCount.get(); }
}
```

**Complete teaching example — Test: `IdempotentCheckoutTest.java`**

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class IdempotentCheckoutTest {
    @Test
    void sameRequestKeyIsNotChargedTwice() {
        IdempotentCheckout checkout = new IdempotentCheckout();
        var first = checkout.charge("key-1", "order-1");
        var replay = checkout.charge("key-1", "order-1");
        assertThat(replay).isEqualTo(first);
        assertThat(checkout.charges()).isEqualTo(1);
    }

    @Test
    void sameKeyForDifferentOrderIsRejected() {
        IdempotentCheckout checkout = new IdempotentCheckout();
        checkout.charge("key-1", "order-1");
        assertThatThrownBy(() -> checkout.charge("key-1", "order-2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Idempotency key reused");
        assertThat(checkout.charges()).isEqualTo(1);
    }
}
```

**PostgreSQL integration recipe:** Define a unique idempotency key in an appropriate scoped table, store request fingerprint and result/status, and make the deduplication claim and local business effect atomic in the *same transaction* where feasible. Execute two **committed** requests with the same key and assert one durable business effect. Add concurrent repeats, response replay, payload mismatch, restart/recovery, and a defined expiration window. **Never** use the in-memory example as evidence of persistent payment safety.

### 6. Step-by-Step Explanation

`ConcurrentHashMap.compute` coordinates update logic for a particular key in this single JVM. The first call creates a receipt; a repeat for the same order returns the stored receipt; a repeat with a conflicting order throws. `chargeCount` is an *observable side effect*—the critical assertion is that it remains 1. A real payment involves external side effects and failure windows: a database transaction **cannot atomically commit an external provider charge** by itself, so real systems may need provider idempotency, reconciliation, and appropriate outbox/inbox mechanisms.

### 7. Testing Boundaries

| Real | Mocked | Spring | PostgreSQL | Docker | Real HTTP |
|---|---|---|---|---|---|
| In-memory deduplication and counter | None | No | No | No | No |
| For production-grade recipe: Service, DB constraint/transactions, external provider sandbox or controlled stub | Provider stub where appropriate | Integration | **Yes** | Testcontainers | Optional |

### 8. What the Test Proves and Does Not Prove

| Proves | Does not prove |
|---|---|
| Sequential requests in one JVM reuse first result | Durability across application crashes/restarts |
| Different-order replay is rejected | Atomicity of external payment + local DB commit |
| Observed simulated side effect occurs once | Distributed deployments and retention expiry |

### 9. Common Pitfalls

Idempotency is **not** merely “add a unique UUID to every retry”; retries must reuse the same key. Don't claim a JPA `existsByKey` precheck prevents races; database uniqueness/locking is the durable authority. Don't blindly retry every POST—first specify whether the operation/provider is retry-safe. Keep key scope (user/tenant, operation), request identity, retention and error replay contract explicit.

### 10. When to Use It

Payment creation, order placement, inventory reservation, message consumption, and webhook processing—any operation likely to be retried with side effects.

### 11. Practical Exercises

1. **Basic:** Verify repeated requests return the exact same receipt and do not increase the charge counter.
2. **Intermediate:** Implement a concurrent same-key test using a `CountDownLatch` and verify one effect; discuss limitations of one-JVM guarantees.
3. **Stretch:** Add a PostgreSQL-backed idempotency record and test restart/replay plus key-conflict and transaction failure cases.

### 12. Mental Model / Key Takeaways

**A retry is not a new intention.** Deduplicate repeated intentions at a durable, explicitly defined boundary.

---

## 5. Important Concept Comparisons

| Pair | First technique | Second technique | Practical selection |
|---|---|---|---|
| Code coverage vs mutation testing | Measures which code executed | Checks whether tests detect selected injected faults | Use coverage to find *gaps*, mutation testing to challenge assertions in critical rules |
| Example-based vs property-based | Named concrete inputs, e.g., quantity = 0/1/100 | Generated inputs checked against invariants | Always keep meaningful named boundaries; add PBT for large structured domains |
| `@WebMvcTest`/MockMvc vs real HTTP | MVC/filter semantics in mock servlet environment | Real socket, embedded server and client behavior | Mostly MVC slices, a few HTTP smoke tests |
| `@Mock SupplierClient` vs WireMock | Simulates Java collaborator return/exception | Simulates remote endpoint for the real HTTP client | Service decisions vs transport/HTTP integration |
| Async vs concurrent tests | Work completes *later* | Work happens *overlapping* | Completion signals vs shared-state invariants and races |
| Functional vs performance tests | Does it return correct result/state? | Under what load/latency/throughput? | JUnit for semantics; k6/JMeter for controlled load testing |
| Deterministic vs randomized tests | Fixed, reproducible inputs | Generated inputs within a domain | Control seeds and keep shrinking output for replay |
| API test vs contract test | Verifies behavior according to local expectation | Verifies behavior against independently agreed interface | Use contract checks when external consumers rely on version stability |
| Fresh migrations vs upgrade migrations | Builds a new schema from scratch | Evolves earlier schema with data present | Both when databases already exist in the field |
| “Request returned OK” vs idempotent behavior | Confirms one response | Confirms replays do not repeat effects | Test durable state and repeated requests |

## 6. Recommended Learning Roadmap

Recommended **relative** effort is for concept + a modest exercise, not a fixed calendar prediction.

### Phase A — Learn Now (P0)

| Order | Concept | Effort | Demonstrate competency by |
|---|---|---|---|
| 1 | Controllable time with `Clock` | Small | Write before/exactly-at/after expiration tests with no sleeps |
| 2 | Async completion and eventual state | Medium | Test an incomplete `Future`, then completion and exception; no blind sleeps |
| 3 | Outgoing HTTP client | Medium | Verify 200 + 503 against WireMock and check request route |
| 4 | Actual server HTTP test | Small | Compare MockMvc and `RANDOM_PORT` for the same endpoint; explain extra evidence |

### Phase B — Learn Next (P1)

| Order | Concept | Effort | Demonstrate competency by |
|---|---|---|---|
| 5 | Concurrency/locking | Large | One-stock/two-buyers invariant with real PostgreSQL and two transactions |
| 6 | Mutation testing | Medium | Kill an off-by-one mutation with a new boundary assertion |
| 7 | Property-based testing | Medium | Express a conservation invariant and save a shrunk regression case |
| 8 | API contract compatibility | Medium | Catch removal/rename of a required response field |
| 9 | Migration upgrades | Medium | Preserve legacy rows when applying V2 to an initialized V1 schema |
| 10 | Idempotency/replay | Medium | Prove repeat key returns same result and no extra durable effect |

### Phase C — Learn When Needed (P2)

| Topic | Trigger | Sensible first exercise | Effort |
|---|---|---|---|
| Caching/Redis TTL | Your application introduces cache-aside/invalidations | Freeze clock or control TTL; update database; prove stale cache is refreshed | Medium |
| Message consumers/outbox | Introducing Kafka/RabbitMQ/event handlers | Duplicate event delivered twice → one business effect | Large |
| Resilience patterns | Adding retries, circuit breakers, bulkheads | Stub 503/timeout; verify limited retry and no duplicate writes | Medium |
| Pact/consumer-driven contracts | Independent provider/consumer teams | Share and verify one provider interaction | Large |
| Load/performance tests | Explicit throughput/latency goals or regressions | Specify workload and SLO; run k6 on isolated staging; record p95 and errors | Medium |
| Browser E2E | Owning an interactive frontend | One login-and-checkout browser smoke test | Medium |

**Stop rule:** You need not complete every P1/P2 technique before applying for backend roles. Master deterministic core tests, database transaction evidence, and an honest test boundary first. Adopt specialized tools when an actual dependency or risk justifies them.

## 7. Practical Competency Checklist

### P0

- [ ] I can state a requirement-derived time boundary and test it with injected `Clock`.
- [ ] I can explain why sleeping is neither synchronization nor proof of completion.
- [ ] I can test `CompletableFuture` success and exceptional completion with controlled execution.
- [ ] I can explain the difference between mock collaborator, fake, and HTTP stub server.
- [ ] I can verify an outgoing HTTP client maps 200 and 503 differently.
- [ ] I can say exactly what MockMvc omits compared with an actual HTTP test.
- [ ] I know why an outer test rollback is unreliable for server-thread writes in `RANDOM_PORT`.

### P1

- [ ] I can state a concurrency invariant for the last available book and design a two-transaction test.
- [ ] I know why one passing concurrency run cannot prove freedom from every race.
- [ ] I can explain a surviving mutant and add a test that detects a real missing decision.
- [ ] I can write an invariant for PBT without copying the implementation algorithm into my expected result.
- [ ] I can explain a generated counterexample, shrinking and replay seeds.
- [ ] I can distinguish an independently agreed API contract from a self-written snapshot.
- [ ] I can create a real database upgrade fixture and check data preservation after new migrations.
- [ ] I can show that two identical idempotency-key requests cause only one durable effect.
- [ ] I can name the external provider failure window that a DB transaction alone does not eliminate.

### Quality of evidence

- [ ] For every test, I can name the real components, doubles, Spring context, database, network and transaction ownership.
- [ ] I can identify at least one plausible production bug a passing test would still miss.
- [ ] I can review an AI-generated test and detect tautological assertions, excessive mocking, misleading success-only checks and undocumented assumptions.
- [ ] I can reproduce the exact test from Maven/CI and explain any infrastructure failure separately from a failed business assertion.

## 8. Final Testing Knowledge Map

```text
Requirement / acceptance criteria
    |
    +-- Test design (ORIGINAL §§1–5): partitions, boundaries, decision tables, oracle
    |
    +-- Smallest suitable test boundary
          |
          +-- Pure unit: JUnit + Mockito (ORIGINAL §§6–11)
          |      +-- NEW: fixed Clock; controlled Executor
          |      +-- NEW: mutation + property-based effectiveness checks
          |
          +-- MVC slice: @WebMvcTest / MockMvc (ORIGINAL §§12–19)
          |      +-- NEW: published API contract assertions
          |
          +-- HTTP adapter test: NEW WireMock / actual outgoing client
          |
          +-- PostgreSQL slice/full context (ORIGINAL §§20–27)
          |      +-- NEW: overlapping transactions, locking, idempotency
          |      +-- NEW: legacy-data migration upgrades
          |
          +-- Spring Security slice/full flow (ORIGINAL §§28–34)
          |
          +-- Real HTTP test (NEW): RANDOM_PORT + actual client
          |
          `-- Full browser/deployment/load tests: ON DEMAND
    |
    +-- Reliable execution (ORIGINAL §§35–43)
          +-- deterministic data/time/scheduling
          +-- Maven clean verify + Testcontainers + CI
          +-- JaCoCo asks 'what executed?'
          +-- PIT asks 'would wrong behavior be caught?'
          `-- Useful failure reports + honest evidence boundaries
```

**A practical layered strategy for an ordinary Spring Boot API:** Many rule-level JUnit tests; focused MVC tests; a few real PostgreSQL tests for queries/transactions; a few HTTP-adapter stubs; one or two real-server tests for critical flows; targeted contract, concurrency, mutation and property tests wherever risk justifies them. Don't start the entire application for a function that can be proved in one pure-Java test.

## 9. Official Documentation and References

The following are **source references for further reading**, not claims that these example projects were executed in this document. Official primary references were consulted for version-sensitive examples. Check your application's effective Maven POM before adding a third-party tool: Boot's managed JUnit Platform version and each test engine must be compatible.

1. **Original guide:** `Testing_Essential_Concepts(3).md`, §§5, 12–14, 20–27, 35–43 (basis of the knowledge-gap assessment). Source-provided material.
2. **Spring Boot 4.1 Testing:** https://docs.spring.io/spring-boot/4.1/reference/testing/index.html — test scope and running-server support.
3. **Spring Framework client tests:** https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-client.html — `MockRestServiceServer` vs mock web servers.
4. **Spring Framework test transactions:** https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html — thread binding, server-thread/preemptive-timeout cautions.
5. **Spring Framework `RestClient`:** https://docs.spring.io/spring-framework/reference/integration/rest-clients.html — outgoing HTTP clients.
6. **WireMock Jupiter extension:** https://wiremock.org/docs/junit-jupiter/ — `@WireMockTest` and runtime URLs.
7. **WireMock 3.x installation:** https://wiremock.org/docs/download-and-installation/ — artifact `org.wiremock:wiremock`.
8. **PIT Maven quickstart:** https://pitest.org/quickstart/maven/ — mutation analysis and report command.
9. **PIT JUnit Platform plugin:** https://github.com/pitest/pitest-junit5-plugin — compatible platform bridge for PIT.
10. **jqwik user guide:** https://jqwik.net/docs/current/user-guide — `@Property`, generators, edge cases, shrinking and seeds.
11. **JUnit user guide:** https://docs.junit.org/current/user-guide/ — execution, parameterization and assertions.
12. **PostgreSQL transactions and locking:** https://www.postgresql.org/docs/current/explicit-locking.html and https://www.postgresql.org/docs/current/transaction-iso.html — actual database semantics.
13. **Flyway documentation:** https://documentation.red-gate.com/fd/ — migrations, target versions and checksums.
14. **Spring Security testing:** https://docs.spring.io/spring-security/reference/servlet/test/index.html — preserve separation of mock identities and real authentication.
15. **Testcontainers PostgreSQL:** https://java.testcontainers.org/modules/databases/postgres/ — disposable PostgreSQL environments.

### Technical accuracy notes

- The complete pure-Java teaching examples require Java 21 and the named JUnit/AssertJ/jqwik libraries; **they were not executed against your Book Catalog repository**.
- Spring/Maven integration recipes deliberately flag assumptions instead of pretending they match unknown production packages, SecurityFilterChains, Flyway baselines or DTOs.
- `@WebMvcTest`/Testcontainers packages in Boot 4.1 differ from older Boot 3 guides. Adopt imports compatible with the **effective** managed dependencies.
- Dependency versions given for WireMock, PIT, and jqwik are **verified teaching examples**, not instructions to force versions where another release is required by your managed build.
- Mutation scores, coverage percentages and performance values are intentionally **not fabricated**.
- Code and contract examples illustrate selected behaviors. They cannot substitute for your project's actual requirements.

---

> **Final takeaway:** You already possess a solid baseline in Spring Boot Testing. Your next improvement is not memorizing more annotations: it is learning to **control nondeterminism, test real integration boundaries, challenge the effectiveness of assertions, and protect business invariants under change, concurrency and retries**.
