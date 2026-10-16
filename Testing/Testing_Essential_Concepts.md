# Testing Essential Concepts

Testing is the executable way to ask whether software behaves as required. This guide teaches testing as an engineering discipline using Java 21, JUnit Jupiter, Mockito and Spring Boot 4.1 examples. It is understandable without opening any application. Boot 4 package names differ from many Boot 3 tutorials; use your dependency management when adapting examples.

The companion [exercises](Testing_Exercise.md) apply these ideas to one existing application; the [solutions](Testing_Solution.md) should be read after a manual attempt. General snippets marked as fragments assume the surrounding components; complete small examples declare their own teaching types. No illustrative endpoint or rule in this document defines a companion-project requirement.

## Learning path

| Stage | Learn to do | Practice |
|---|---|---|
| [Principles and design](#principles) | Choose behaviors, cases and boundaries | W1–W3, Task 1 |
| [JUnit](#junit) | Express assertions, exceptions and data-driven cases | W1–W3, Task 3 |
| [Mockito](#mockito) | Isolate service decisions without hiding limitations | Tasks 4–5 |
| [Spring testing](#spring-testing) | Select context/slice, validation and advice | Tasks 2, 6–7 |
| [Persistence](#persistence) | Prove SQL, mappings, migrations and constraints | Tasks 10–12 |
| [Transactions](#transactions) | Observe real commit and rollback | Tasks 9, 13 |
| [Security](#security) | Separate authorization from real login/session flow | Tasks 8–9, 14–15 |
| [Reliable delivery](#delivery) | Isolate data, use Maven, coverage and CI reports | Task 16 and final checklist |

Essential vocabulary: a **fixture** is prepared test data/state; a **test double** substitutes for a collaborator; an **ApplicationContext** is Spring's bean container; a **proxy** wraps a bean to intercept calls; **regression** means previously working behavior breaks. A passing test supplies evidence for its declared inputs and boundary, not universal correctness.

<a id="principles"></a>

## Part 1 — Principles and test design

**Learning objective:** Choose a behavior and the smallest boundary that can prove it.

### 1. Why testing exists

```text
code
  -> expected observable behavior
  -> executable verification
  -> fast feedback
  -> safer refactoring
```

A useful test states a behavior, arranges the relevant boundary, exercises it, and fails for a reason that helps diagnose a regression. `assertTrue(true)` executes a line but provides no confidence.

Testing provides:

- **confidence** that important paths work;
- **regression prevention** when code changes later;
- **living documentation** of input, output, error, and state contracts;
- **refactoring safety** because behavior is checked independently of implementation shape;
- **design feedback** because code that is impossible to isolate often has tangled responsibilities.

Coverage asks whether code was executed. Correctness asks whether all required behavior is right for relevant inputs, state, timing, failures, and integrations. Therefore:

```text
100% line coverage != 100% correctness
```

A test that invokes account creation but never checks the assigned authority can execute every creation line while missing a privilege-escalation bug.

### 2. Testing levels

Names vary between teams, so always state the boundary explicitly. A **boundary** is the set of real components whose cooperation a test observes.

| Level | What is real? | What is mocked? | Spring context? | PostgreSQL? | Typical purpose |
|---|---|---|---|---|---|
| Unit | One Java object and value objects | Relevant collaborator boundaries | No | No | Branching, calculations, normalization |
| Spring slice | MVC or persistence infrastructure plus selected application code | Components outside the slice when needed | Partial | No for isolated MVC; yes for PostgreSQL persistence | JSON/validation or mappings/queries |
| Integration | Several cooperating real components | Out-of-scope systems, if any | Often | When database behavior matters | Wiring, SQL, migration or proxy behavior |
| Full-context integration | Application beans and configured filters | Usually none inside the boundary | Full | When persistence is involved | Authentication, transactions, cross-layer flows |
| End-to-end | Running system through an external client | Ideally nothing inside the system | Real server | Real isolated database | User-visible behavior across deployment boundaries |

`@SpringBootTest` is not a synonym for a good test. Starting the context, migration engine, ORM and database to prove that a Java method trims text makes feedback slower and failures harder to locate. A full-context MockMvc test still uses a mock servlet environment: it is not an external network/browser test.

> Use the smallest test scope that gives confidence in the required behavior.

### 3. Practical test pyramid

```text
many fast unit tests
        ↓
fewer slice/integration tests
        ↓
few full-flow/end-to-end tests
```

This is a cost model, not a quota. Unit tests are fast and diagnostic but cannot prove wiring, proxy advice, SQL, filters, or migrations. Full tests are realistic but slower, have more failure causes, and cost more to maintain. Choose the mix from risk:

- password role assignment is cheap to prove in a unit test;
- a unique constraint needs PostgreSQL;
- `@PreAuthorize` needs a Spring proxy;
- login/session/logout needs the Security filter chain and real authentication components.

<a id="test-case-design"></a>

### Designing test cases before writing code

**Learning objectives:** turn requirements into observable cases; select representative inputs; prioritize risk; explain why the collection is meaningful before choosing annotations.

A **test scenario** is a behavior area, such as "reject an invalid quantity." A **test case** is a precise example with preconditions, inputs, action and expected results. A scenario may need many cases. An **oracle** is the source of the expected answer: an acceptance criterion, specified rule, state model or independently calculated result.

| Element | Question | Example |
|---|---|---|
| Requirement | What must be true? | Quantity must be an integer from 1 through 100, inclusive |
| Scenario | Which behavior area? | Reject out-of-range quantities |
| Preconditions | What state must exist? | No shared state; all other fields valid |
| Input | What exact value? | 0 |
| Action | What does the caller do? | Invoke quantity validation |
| Expected result | What observable outcome? | Declared invalid result/exception; no state change |
| Boundary | Which real components are required? | Java unit if implemented as a pure rule; MVC if proving request validation |

**Positive testing** checks permitted behavior; **negative testing** checks deliberate rejection or failure handling. A **happy path** is a common successful flow. A **failure path** follows a rejection, missing dependency, exception or failed operation. An **edge case** is an unusual or extreme input/state and can be valid (quantity 1) or invalid (quantity 0). Negative does not mean "negative number."

| Case | Preconditions | Input/action | Expected result | Category |
|---|---|---|---|---|
| Q1 | Other fields valid | Submit 20 | Accepted | Positive, happy path |
| Q2 | Other fields valid | Submit 1 | Accepted | Positive edge |
| Q3 | Other fields valid | Submit 0 | Rejected | Negative edge |
| Q4 | Quantity required | Submit missing/null | Required-field error | Negative input |
| Q5 | Database unavailable during write | Submit valid quantity | Defined failure response; no false success | Failure path, integration |

Q4 and Q5 require their own stated contracts. Do not infer null handling or an HTTP status from a range requirement alone. Write ambiguities down before turning guesses into tests.

#### Equivalence Partitioning

**Definition:** divide inputs into classes expected to behave alike, then choose representatives. It reduces redundant testing while exposing missing categories.

For an integer range 1..100, useful partitions are below-range, in-range and above-range. At an HTTP boundary, missing, wrong type and non-integral JSON values may be separate classes because different parsing/validation mechanisms handle them.

| Partition | Representative | Expected for the stated integer rule | Why separate? |
|---|---:|---|---|
| Below minimum | -5 | Reject | Lower guard |
| Valid range | 20 | Accept | Successful rule |
| Above maximum | 150 | Reject | Upper guard |
| Missing value | null | Requirement must specify | Different validation concern |
| Wrong type | "many" | Binding contract must specify | Not a Java integer input |

Common mistakes: choosing ten valid values and no invalid classes; assuming one representative establishes every input; treating strings and already parsed integers as the same boundary. Use partitions to organize a finite suite, then add boundaries and risk-specific examples.

#### Boundary Value Analysis

**Definition:** test at limits and immediately around them, where `<`/`<=` and off-by-one mistakes cluster. For a discrete integer range use `min-1`, `min`, `min+1`, `max-1`, `max`, `max+1` where relevant.

| Value | Expected for 1..100 | Fault exposed |
|---:|---|---|
| 0 | Reject | Missing lower guard |
| 1 | Accept | Exclusive minimum mistake |
| 2 | Accept | Neighbor near minimum |
| 99 | Accept | Neighbor near maximum |
| 100 | Accept | Exclusive maximum mistake |
| 101 | Reject | Missing upper guard |

The next self-contained example is a teaching rule, not a new application requirement:

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RangeRuleTest {
    static boolean accepts(int quantity) {
        return quantity >= 1 && quantity <= 100;
    }

    @ParameterizedTest
    @CsvSource({"0,false", "1,true", "2,true", "99,true", "100,true", "101,false"})
    void accepts_boundaryValues_followInclusiveRange(int value, boolean expected) {
        assertEquals(expected, accepts(value));
    }
}
```

Common mistakes: testing only outside limits; using an arbitrary epsilon for money; forgetting normalization before a string-length check. For a two-decimal amount, a meaningful neighboring step may be 0.01; for floating-point calculations the specification must define precision/tolerance. Boundary selection depends on the input domain.

#### Decision Table Testing

**Definition:** list combinations of conditions and their required outcomes. Use it when multiple rules interact; individual happy paths can miss combinations.

Suppose a write requires a signed-in administrator and a valid CSRF token. This is an illustrative policy with explicit rejection precedence: invalid token first, then missing identity, then role.

| Rule | Token valid? | Signed in? | Administrator? | Expected decision |
|---|---|---|---|---|
| D1 | No | Either | Irrelevant | Reject token |
| D2 | Yes | No | Irrelevant | Require authentication |
| D3 | Yes | Yes | No | Deny authorization |
| D4 | Yes | Yes | Yes | Allow action |

"Either" merges combinations only because their result is the same under this stated policy. An administrator without an authenticated identity is not a feasible state here. Expand merged rows when a different side effect or filter path matters. Translate each row to a separate case or parameterized invocation; assert both the response and whether the protected action ran.

Common mistakes: leaving impossible combinations unexplained; assuming rejection precedence without reading the chain; writing only D4; using synthetic users to claim password authentication is tested.

#### State Transition Testing

**Definition:** model states, events and transitions, then test legal transitions and prohibited operations in each relevant state. Use it for sessions or any behavior whose answer depends on history.

```text
Anonymous -- valid login --> Authenticated -- logout --> Anonymous
    |                            |
    `-- bad login -> Anonymous    `-- permitted request -> Authenticated
```

| Starting state | Event | Expected next state | Observable assertion |
|---|---|---|---|
| Anonymous | Wrong credentials | Anonymous | No authenticated session |
| Anonymous | Valid credentials | Authenticated | Later request using same session succeeds |
| Authenticated | Permitted request | Authenticated | Identity remains the same |
| Authenticated | Logout | Anonymous | Session invalidated/identity cleared as configured |
| Anonymous after logout | Protected request | Anonymous | Authentication required |

Each transition can be tested in an independent test with its own complete setup; do not depend on another test having logged in. A deliberate multi-request test can exercise one coherent lifecycle. Common mistakes: checking only the login response, reusing an invalidated session object, or resetting state between steps that are supposed to demonstrate persistence.

#### Error Guessing

**Definition:** use experience with likely failures to add cases beyond systematic partitions. It supplements structured design rather than replacing it.

| Suspected failure | Proposed input/state | Expected behavior source |
|---|---|---|
| Hidden whitespace | Leading/trailing spaces | Normalization contract |
| Unicode exceeds a byte limit | Multibyte text within character limit | Encoding/service rule |
| Duplicate race | Precheck succeeds, write rejects duplicate | Constraint + translation contract |
| Numeric overflow | Largest supported value plus increment | Arithmetic contract |
| Stale persistence state | Clear context before reread | Database round-trip objective |
| Missing CSRF | Unsafe request with a valid identity | Security configuration |

Common mistakes: guessing a desired behavior and calling it a requirement, or collecting random cases without explaining the likely fault. Tie every added case to evidence or mark it as a question for clarification.

#### Statement coverage versus branch coverage

**Statement coverage** asks which executable statements ran. **Branch coverage** asks which alternatives of decisions ran. Both are structural measures, not requirements.

```java
static int adjusted(int value, boolean addBonus) {
    int result = value;
    if (addBonus) result++;
    return result;
}
```

| Case | Inputs | Expected | Coverage implication |
|---|---|---:|---|
| C1 | 5, true | 6 | Executes every statement, but only the true decision outcome |
| C2 | 5, false | 5 | Adds the false decision outcome |

C1 alone can yield 100% statement coverage and still miss a bug affecting false. C1+C2 does not prove overflow handling or every value. Line/bytecode metrics differ from source statements, and branch coverage does not mean all paths or all Boolean condition combinations. The [coverage chapter](#coverage) explains JaCoCo's actual counters.

#### Risk-based test prioritization

**Definition:** spend effort first where failure impact and likelihood justify it. Add detectability, recent changes and integration uncertainty to the decision; no universal numerical score is required.

| Risk | Why prioritize? | Smallest useful checks |
|---|---|---|
| Unauthorized write | Data/security impact | Denial matrix + protected action not invoked |
| Partial commit | Durable inconsistent state | Real transaction proxy + database reread |
| Boundary rejection error | Common user failure | Parameterized limit cases |
| Display wording | Usually lower impact | Stable contract fields; avoid fragile incidental wording |

Common mistakes: prioritizing by ease of coverage, testing only success because failures are inconvenient, or assuming low-risk means never test. Reassess risk when behavior changes.

#### From requirements to a test collection

Use this procedure before implementing a test:

1. Identify the requirement and source of expected behavior. Separate existing behavior from proposed improvements.
2. Split it into scenarios and observable results, including state and side effects.
3. Enumerate partitions, limits, decision combinations and state transitions.
4. Keep other inputs valid to isolate a rule; add combinations only where interactions matter.
5. Prioritize risk and select a boundary able to observe each case.
6. Name the real components, doubles and missing infrastructure.
7. Record the expected result before executing code; then implement AAA.
8. Trace each important acceptance criterion to a case and identify remaining uncertainty.

Example acceptance criteria: "an integer quantity from 1 through 100 is accepted; others are rejected; rejection leaves stored quantity unchanged." The first two criteria need range cases; the final criterion needs a state assertion at the persistence boundary, not only a boolean helper test.

| Criterion | Cases derived | Boundary | Main oracle | Remaining gap |
|---|---|---|---|---|
| Inclusive range | Q1, values 1/2/99/100 | Unit rule | Accept | No HTTP proof |
| Reject out of range | Values 0/101 plus negative representative | Unit rule | Reject | No persistence proof |
| Rejection keeps stored state | Seed valid state, submit invalid change | Integration at actual write boundary | Original state after rejection | No concurrency proof |
| Missing/type behavior | Pending specification | Chosen after clarification | Explicitly unresolved | Do not invent result |

Use this reusable design matrix in the exercises:

```text
Case ID | Requirement | Preconditions | Inputs/action | Expected output/state
        | Happy/edge/failure | Level | Real | Mocked | Limitation
```

**Key takeaways:** scenarios name behavior areas; cases make them executable. Combine input techniques with decisions, state and risk. Coverage helps find questions but cannot supply missing requirements.

### 4. AAA and Given–When–Then

AAA means **Arrange, Act, Assert**. Given–When–Then describes the same structure in requirement language: initial context, action, observable outcome.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuantityRulesTest {
    static int requirePositive(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        return quantity;
    }

    @Test
    void requirePositive_one_returnsOne() {
        // Arrange / Given
        int quantity = 1;
        // Act / When
        int result = requirePositive(quantity);
        // Assert / Then
        assertEquals(1, result);
    }
}
```

Name tests by condition and behavior, such as `requirePositive_zero_throws`, or use `@DisplayName`. Avoid `test1`. Keep one main action per case; several assertions may describe that action's contract. Do not put the call under test inside setup where its failures become confusing.

### 5. Good test characteristics and flakiness

A good test is:

- deterministic;
- isolated from unrelated tests;
- repeatable locally and in CI;
- readable;
- fast enough for its layer;
- independent of execution order;
- focused on observable behavior.

A flaky test sometimes passes and sometimes fails without a relevant code change. It trains developers to ignore failures.

Avoid:

- `Thread.sleep(...)` as synchronization;
- test-order dependencies;
- uncontrolled calls to the current clock;
- shared mutable static state;
- random values without a recorded/controlled seed;
- external internet services;
- production databases.

This connects directly to concurrency: sleeping does not establish a happens-before relationship or prove work completed. Prefer a completion signal, future/join, latch, polling with a bounded timeout, or Awaitility when asynchronous behavior truly exists.

### Basic TDD and maintainable tests

**Learning objective:** use tests to guide a small behavior change without confusing implementation details with contracts.

Test-Driven Development (TDD) cycles through **Red -> Green -> Refactor**. Red: write one meaningful failing test for a specified behavior and confirm the failure is the expected assertion, not a broken build. Green: implement the smallest change that satisfies that behavior. Refactor: improve structure while the tests protect behavior.

For a new positive-quantity rule, start with rejection of zero, implement that rule, then add valid/invalid boundaries and refactor repetition. When studying an existing application, first write **characterization tests** for observed behavior. If it conflicts with an intended requirement, document the conflict; changing the requirement is not a test refactor.

Maintainability means a legitimate internal refactor rarely breaks behavior tests. Prefer public results, selected JSON fields and meaningful boundary interactions over private methods, field layout, all-call ordering or exact incidental error wording. Keep fixtures small and intent visible. Extract repeated setup only when the helper has a clear name and preserves important inputs. A helper that reproduces production logic can hide the same bug.

Do not write a passing test after implementation and claim you practiced the Red step. Do not keep a deliberately failing test in a finished suite. Use this cycle when implementing a behavior change; for documentation-only study, predict a failure locally without altering production requirements.

**Key takeaway:** test design decides what matters; TDD decides when to implement it. Neither guarantees correctness without a reliable oracle.

**Part takeaway:** Behavior, real components and limitations should always be named.

<a id="junit"></a>

## Part 2 — JUnit and assertions

**Learning objective:** Write readable cases with independently chosen expected results.

For ordinary synchronous tests, lifecycle methods run around each test. Cleanup must also handle failed setup; prefer try-with-resources for a resource used only by one case.

### 6. JUnit architecture and lifecycle

JUnit is the test framework; Maven and the IDE are launchers. Jupiter is the modern programming model used by JUnit 5 and JUnit 6. The package remains `org.junit.jupiter`, including in JUnit 6. Java 21 works with the JUnit 6 generation used in the companion exercises.

| Piece | Responsibility |
|---|---|
| JUnit Platform | Discovers engines and launches tests |
| Jupiter | Test API, assertions, lifecycle, extensions, parameterized tests, engine |
| Vintage | Runs legacy JUnit 3/4 tests; unnecessary when a suite uses Jupiter only |

Jupiter normally creates a **new test class instance for every test method**. This limits accidental instance-state sharing, but static fields and external resources can still leak state.

```text
BeforeAll -> [new instance -> BeforeEach -> test -> AfterEach] per case -> AfterAll
```

| Annotation | Use |
|---|---|
| `@Test` | One example of a behavior |
| `@BeforeEach` / `@AfterEach` | Per-case setup/cleanup |
| `@BeforeAll` / `@AfterAll` | Class setup/cleanup; normally static methods |
| `@TestInstance(PER_CLASS)` | Share one instance; then class lifecycle methods can be nonstatic, but mutable state requires care |
| `@Nested` | Group related scenarios |
| `@DisplayName` | Readable report name |
| `@ParameterizedTest` | Repeat a behavior with supplied inputs |
| `@ValueSource`, `@CsvSource`, `@MethodSource` | One-column, scalar table, or Java-produced arguments |
| `@NullSource`, `@EmptySource`, `@NullAndEmptySource` | Explicit null/empty input cases |
| `@ExtendWith` | Register an extension, such as Mockito's Jupiter support |

Do not hide essential inputs in lifecycle methods. A parameterized test needs an argument source, and `@ValueSource` cannot contain null. Use a nullable parameter type for null cases. Keep assertions about one behavior in each parameterized method.

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalizationTest {
    @ParameterizedTest
    @CsvSource({"' A ', A", "'B', B", "'  C', C"})
    void trim_removesSurroundingWhitespace(String input, String expected) {
        assertEquals(expected, input.trim());
    }
}
```

A parameterized row is a separate invocation in reports. Parameterization reduces repetition; it does not replace choosing meaningful partitions and boundaries.

### 7. Assertions

An **assertion** compares observed behavior with an independently chosen expected result (the test's **oracle**). A test that only calls a method can pass while its result is wrong.

```java
// Method-body snippets: static imports from org.junit.jupiter.api.Assertions.
assertEquals(expected, actual);       // expected first improves diagnostics
assertTrue(condition);
assertFalse(condition);
assertNull(value);
assertNotNull(value);
assertAll(() -> assertEquals("A", result.name()),
          () -> assertEquals(3, result.quantity()));
```

`assertAll` reports several related failures together. `assertDoesNotThrow` proves only the absence of an exception for the supplied call; pair it with output/state assertions when those matter. Avoid checking only non-null or only HTTP status when incorrect content could still pass.

AssertJ, supplied by Boot's test starter, offers fluent collection and exception assertions:

```java
// Static imports from org.assertj.core.api.Assertions; illustrative method body.
assertThat(values).containsExactly("A", "B");
assertThatThrownBy(() -> operation.run())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("quantity must be positive");
```

JUnit is convenient for direct checks; AssertJ helps with collections and object graphs. Keep a consistent style within a test. `BigDecimal.equals` compares scale too (`1.0` differs from `1.00`); use `compareTo` or AssertJ `isEqualByComparingTo` when numeric equality is the contract. Never compute expected output by repeating the production algorithm: both can share the same error.

### 8. Exception testing

An exception is one part of a failure contract. Check the relevant type, stable message, absence of forbidden side effects, and state after failure.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {
    static int positive(int value) {
        if (value <= 0) throw new IllegalArgumentException("quantity must be positive");
        return value;
    }

    @Test
    void positive_zero_throwsWithReason() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class, () -> positive(0));
        assertEquals("quantity must be positive", thrown.getMessage());
    }
}
```

`assertThrows` accepts the expected type **or a subtype**. Use `assertThrowsExactly` only when exact type is part of the contract. Keep the lambda narrow: an exception from setup must not satisfy the assertion accidentally. Do not catch and ignore failures or use `Exception.class` when the requirement names a specific error.

A mock test may prove that a repository operation is requested before a service throws. It cannot prove that a real database rolls back. A transaction integration test must invoke the Spring proxy and read state after transaction completion.

**Part takeaway:** Assertions and input selection matter more than the number of test methods.

<a id="mockito"></a>

## Part 3 — Test doubles and service unit tests

**Learning objective:** Control collaborators while keeping the object under test real.

### 9. Test doubles

| Double | Practical meaning | Example |
|---|---|---|
| Dummy | Passed only to satisfy a signature; behavior is irrelevant | an unused argument in a focused callback test |
| Stub | Returns a prepared answer | repository returns an existing user |
| Mock | Records interactions and is verified | verify the encoder and repository calls |
| Fake | Working simplified implementation | an in-memory repository, if its semantics are sufficient (not suitable for PostgreSQL constraints) |
| Spy | Wraps a real object while allowing selective stubbing/verification | rare; perhaps observe one collaborator without replacing all behavior |

Mockito objects can act as stubs and mocks. The useful distinction is why the test uses them. Excessive spying couples tests to internal calls and can bypass proxy behavior.

### 10. Mockito

Mockito creates controllable collaborators. **Stubbing** prepares answers; **verification** checks important calls. A mock's default answers (often null, false, zero or empty values) are not business behavior.

The following complete example needs Jupiter and Mockito Jupiter test dependencies. The nested types exist only to explain the boundary; no application is required.

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LookupServiceTest {
    interface Lookup { String read(long id); }
    static class LookupService {
        private final Lookup lookup;
        LookupService(Lookup lookup) { this.lookup = lookup; }
        String label(long id) { return lookup.read(id).trim(); }
    }

    @Mock Lookup lookup;
    @InjectMocks LookupService service;

    @Test
    void label_existingValue_trimsCollaboratorOutput() {
        when(lookup.read(7L)).thenReturn("  A  ");
        assertEquals("A", service.label(7L));
        verify(lookup).read(7L);
    }
}
```

The service is real; `Lookup` is mocked. The test proves trimming of the prepared result and lookup identity. It cannot prove a real lookup can reach a database, its query is correct, or that dependency injection is configured correctly. Mocking can hide incompatible assumptions between layers: both the stub and expectation may describe a contract the real collaborator never implements.

Useful operations (illustrative method-body fragments):

```java
when(collaborator.call()).thenThrow(new IllegalStateException("unavailable"));
doThrow(new IllegalStateException("unavailable")).when(collaborator).voidCall();
verify(collaborator, never()).forbiddenCall();
verifyNoInteractions(otherCollaborator);

ArgumentCaptor<Value> captured = ArgumentCaptor.forClass(Value.class);
verify(writer).save(captured.capture());
assertEquals("expected", captured.getValue().name());
```

`ArgumentCaptor` observes a value crossing a boundary; import it from `org.mockito`. If an invocation uses one argument matcher (`eq`, `any`), use matchers for all its arguments. Prefer precise matching for important inputs. Strict stubbing helps expose unused setup and wrong arguments; remove unnecessary stubs instead of making everything lenient.

**State verification** checks a result or changed state. **Interaction verification** checks a contractual boundary call or absence of one. Do not verify every line, mock the object under test, mock simple records, or test private methods directly. Use spies sparingly; `when(spy.method())` calls the real method during setup, so `doReturn(...).when(spy)...` may be appropriate.

### 11. Unit-testing service behavior

A service coordinates collaborators and applies rules. Construct it directly, stub only external decisions, call its public method, and assert its output, captured values, and important forbidden side effects.

For a create operation with a duplicate precheck, design at least:

| Case | Stub | Main assertion | Limitation |
|---|---|---|---|
| Available value | Lookup says absent; writer accepts | Correct data sent to writer | Does not prove persistence |
| Existing value | Lookup says present | Conflict and no write | Does not prove concurrency protection |
| Database-race translation | Precheck absent; writer throws integrity failure | Service maps failure as designed | Does not prove the database constraint exists |

The unique database constraint remains the authority when concurrent requests both pass a precheck. A unit test can exercise translation without reproducing a real race. Name it accordingly.

Do not claim direct construction proves `@Transactional`, `@PreAuthorize`, Bean Validation, SQL constraints, or an encoding implementation. Those require the corresponding real infrastructure. Prefer a small unit test for service decisions plus focused integration tests for those boundaries.

**Part takeaway:** Mocks establish service decisions; real integrations require other tests.

<a id="spring-testing"></a>

## Part 4 — Spring TestContext and HTTP testing

**Learning objective:** Choose context scope, then prove MVC/validation/advice behavior.

### 12. Spring TestContext Framework

When a Spring test starts a context, the TestContext Framework:

1. derives configuration and profiles/properties;
2. creates or reuses an `ApplicationContext`;
3. registers beans and proxy post-processors;
4. injects the test instance;
5. runs test listeners, including security and transactional listeners.

Contexts are cached by a key derived from configuration, profiles, properties, imports, and bean overrides. Tests with the same key can reuse an expensive context. Many slightly different `@MockitoBean` sets or properties create many cache keys.

`@DirtiesContext` evicts a context because a test changed singleton/context state. It is a last resort, not general database cleanup. Prefer restoring the particular state you changed. Rebuilding the full context and restarting a container makes suites slow.

Profiles and test properties select test configuration. Avoid profiles that need developer secrets or bootstrap real accounts. Use explicit disposable test data and ensure external connection details cannot fall back to a valued database.

### 13. Spring Boot 4 test dependencies and ownership

Boot 4.1 is modular. The base starter supplies JUnit Jupiter, Spring Test, Mockito, AssertJ, Hamcrest, JSONassert/JsonPath, Awaitility, and Boot test support. The MVC, JPA, and Security test starters supply their focused modules and depend transitively on the base starter.

In a Boot 4 Maven module, use versionless test dependencies managed by Boot:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security-test</artifactId>
  <scope>test</scope>
</dependency>
```

`spring-boot-starter-security-test` brings `spring-security-test`. Let the Boot parent/BOM manage compatible library versions. Maven plugin management comes from the parent, not merely a dependency BOM. Remove obsolete local test-runner overrides when migrating a toolchain; inspect the effective POM rather than guessing the version.

Ownership summary:

| Component | Who provides it? | What do you write? | Starts Spring? | Real DB? |
|---|---|---|---|---|
| JUnit Jupiter | JUnit | test methods and lifecycle | No | No |
| Mockito | Mockito | mock setup and verification | No | No |
| Spring TestContext | Spring Framework | context test configuration | Yes | only if configured |
| Boot test annotations | Spring Boot | slice/full test classes | Yes | depends on slice |
| MockMvc | Spring Framework/Boot configuration | request and response assertions | MVC context | No server; DB depends on context |
| Testcontainers | Testcontainers | container declaration | No by itself | starts real disposable PostgreSQL |
| Flyway | Flyway + Boot integration | versioned migrations | runs during DB context startup | Yes |
| HikariCP | Hikari + Boot | normally configuration only | part of DB context | connects to PostgreSQL |

### 14. `@SpringBootTest`

`@SpringBootTest` asks Boot to find `@SpringBootConfiguration` (`Application`) and build the full application context. It does **not** magically start PostgreSQL; a `DataSource` still needs a reachable database or Testcontainers connection. Beans are real unless explicitly replaced.

`webEnvironment` modes:

| Mode | Meaning |
|---|---|
| `MOCK` (default) | Web application context with mock servlet environment; combine with `@AutoConfigureMockMvc`. |
| `RANDOM_PORT` | Start the embedded server on an available port; useful for true HTTP-client tests. |
| `DEFINED_PORT` | Start on the configured port; collision-prone in tests. |
| `NONE` | Non-web application context. |

Use it for real authentication/session flow, cross-layer transaction behavior, and broad wiring smoke tests. Do not use it for DTO normalization or isolated service branching.

### 15. Boot 4 test slices

Boot 4.1 package names differ from many older tutorials:

```java
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
```

| Slice | Loads | Excludes | Replacements/config needed | Typical purpose |
|---|---|---|---|---|
| `@WebMvcTest` | MVC infrastructure, selected controller(s), converters, validation, controller advice | services, repositories, general components | `@MockitoBean` service collaborators; import the application security config only for security-policy tests | mappings, JSON, validation, ProblemDetail |
| `@DataJpaTest` | DataSource/JDBC/JPA, repositories, entity manager; tests are transactional by default | MVC and normal service layer | real PostgreSQL service connection, no DB replacement, and explicit Flyway auto-configuration in Boot 4's focused slice | mappings, derived queries, constraints, flush behavior |

Do not stack multiple `@...Test` slice annotations. Choose one slice and import only the additional infrastructure that the behavior requires.

For controller-only tests it is acceptable to use `@AutoConfigureMockMvc(addFilters = false)` **when the test explicitly says security is out of scope**. A separate security-policy test must keep filters enabled and import the real `SecurityConfig`.

### 16. `@Mock` versus a mocked Spring bean

```text
Mockito @Mock
  -> creates a plain Mockito object
  -> no ApplicationContext

Spring Framework @MockitoBean
  -> adds/replaces a bean in the test ApplicationContext
  -> controllers and other Spring beans receive that mock
```

Boot 4.1 / Spring Framework 7 annotation fragment (controller/service names are placeholders):

```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(ExampleController.class)
class ExampleControllerWebMvcTest {
    @MockitoBean
    ExampleService service;
}
```

Older Boot versions used `org.springframework.boot.test.mock.mockito.MockBean`, which is removed in Boot 4. Do not copy that import into a Boot 4 test; `@MockitoBean` is the current Framework mechanism. Every different bean override also affects context-cache identity, so keep override sets consistent where practical.
`@InjectMocks` is Mockito constructor/field injection into a plain object; it is not Spring injection. A service replaced by `@MockitoBean` no longer exercises its production transaction or method-security advice.

### 17. Controller testing with MockMvc

MockMvc sends a mock servlet request through `DispatcherServlet`, handler mapping, argument resolution, JSON conversion, validation, controller advice, and—when enabled—the Security filter chain. It does not open a TCP port.

```java
mockMvc.perform(get("/entries/{id}", 7))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.label").value("entry-7"));
```

For create behavior assert the status, `Location`, content type, and selected JSON fields. For list behavior use real query parameters. Avoid comparing a complete raw JSON string: property order is not the contract.

MockMvc can prove HTTP mapping/serialization. If the service is mocked, it cannot prove database persistence or transactions.

### 18. Validation testing

Bean Validation evaluates metadata such as `@NotNull`, `@Size`, and `@Positive`. Calling a record constructor does not normally evaluate those constraints. Spring MVC invokes validation for a `@Valid @RequestBody`; a standalone validator test calls the validator explicitly.

```java
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestValidationTest {
    record QuantityRequest(@NotBlank String label, @Positive int quantity) { }

    @Test
    void zeroQuantity_hasConstraintViolation() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator()
                    .validate(new QuantityRequest("A", 0));
            assertFalse(violations.isEmpty());
        }
    }
}
```

Use field-specific checks when several constraints exist, so the wrong violation cannot satisfy the test. Many constraints (for example `@Size`) allow null; combine `@NotNull`/`@NotBlank` where the actual contract requires it.

| Boundary | Proves | Does not prove |
|---|---|---|
| Direct constructor test | Normalization implemented in Java | Constraint evaluation |
| Real Validator | Declared constraints for supplied values | MVC invokes them or maps failures |
| MVC with real validation/advice | JSON binding, `@Valid`, status/error body, service not called | Database constraints |
| PostgreSQL | Schema constraints | Friendly HTTP feedback |

Test null, empty, blank, length and numeric boundaries separately. Change one field at a time while keeping the other fields valid. For MVC rejection, assert error field identity and verify no service interaction. Do not invent a constraint because its presence would be desirable; an unconstrained DTO is a documented gap. An unused helper method is not an active rule.

### 19. Testing controller advice and exception handling

`@RestControllerAdvice` maps controller failures to HTTP responses. An exception class test proves Java behavior; an MVC test proves mapping through Spring's exception resolvers.

Design cases for a declared not-found failure, a conflict, invalid JSON, invalid fields, incompatible query/path types, and an unexpected exception. Use the application's actual response contract; `ProblemDetail` commonly represents RFC-style problem fields such as status, title, detail and instance.

```java
// MockMvc method-body fragment; controller, service and advice are configured by the slice.
when(service.find(99L)).thenThrow(new MissingValueException(99L));
mockMvc.perform(get("/entries/{id}", 99))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Entry not found"));
```

The custom names/message above illustrate a contract to define in a small example; they are not framework defaults. Assert the actual status, content type and selected documented fields. Avoid brittle whole-JSON string comparisons or invented fields. Check sanitization for unexpected failures if promised by the handler.

Security filters may reject before MVC executes. Their authentication/access-denied handlers can produce a different problem shape; controller advice is not automatically responsible for filter failures. Determine which component owns each error before choosing assertions.

**Part takeaway:** The context supplies infrastructure; choose only the infrastructure the behavior needs.

<a id="persistence"></a>

## Part 5 — Persistence and database integration

**Learning objective:** Make SQL execute and verify the database boundary with PostgreSQL.

### 20. Repository testing

A Spring Data repository is a framework-created implementation. A mock repository cannot verify its derived query, entity mapping, generated SQL or database constraints. Use a real repository, Hibernate, JDBC and PostgreSQL for those questions.

Test your derived-query names and semantics (exact matching, absent values, pagination and ordering), mappings, unique/null/check constraints, relevant numeric/time types and optimistic locking when needed. Test your contract rather than merely demonstrating that `JpaRepository.save()` exists.

For pagination, seed enough rows to cross a page boundary and include an explicit stable sort. Otherwise a test can pass by accidental insertion order. A repository with no custom query can still be valuable in a transaction integration test that observes which rows commit.

### 21. Persistence-context pitfalls

Key terms:

- **persist**: make a new entity managed; SQL timing depends on strategy and flush requirements.
- **dirty checking**: Hibernate detects changes to managed entities.
- **flush**: synchronize pending persistence-context changes with SQL/database; it is not a commit.
- **clear**: detach managed entities and empty the first-level cache.
- **first-level cache**: one persistence context can return the same in-memory instance without a database round trip.

Database round-trip fragment (an `Entry` entity and repository are assumed):

```java
Entry saved = entries.saveAndFlush(entry);
Long id = saved.getId();

entityManager.clear();             // force the next lookup beyond L1 cache
Entry reloaded = entries.findById(id).orElseThrow();
assertThat(reloaded.getLabel()).isEqualTo("entry-1");
```

Use `flush()` or `saveAndFlush(...)` when the assertion requires SQL/constraint execution, then `clear()` when the assertion requires a database reread.

Clearing removes the first-level cache only. If second-level or query caching is enabled, account for it separately (for example, evict the relevant cache or use an appropriate JDBC assertion) before claiming a physical database read. `clear()` itself neither flushes pending changes nor commits them; flush before clearing when changes must reach SQL.

Nuance: entities using `GenerationType.IDENTITY` can need an early INSERT. An INSERT may therefore occur during `save()` to obtain the ID. Never promise all INSERT errors wait for explicit flush. Flush is the deterministic synchronization boundary; failure can legally occur earlier.

To demonstrate deferred dirty-check SQL reliably, persist two valid entities, mutate the second managed entity to reuse the first unique value, call `save(second)` (which need not issue an update), and assert the unique violation at `flush()`.

### 22. Testing database constraints

Application validation improves feedback; database constraints protect durable state and concurrent writers. Their contracts may differ: an HTTP field can have a shorter allowed length than its database column, and annotations alone do not create schema constraints when Flyway owns the schema.

Read the migration before expecting a unique, non-null, foreign-key or check violation. Choose data that violates exactly the intended constraint. Verify the failure at a deterministic SQL synchronization boundary, using the repository proxy if expecting Spring's translated `DataIntegrityViolationException`.

```text
valid setup -> violate one known constraint -> save/flush fails -> rollback -> separate case
```

IDENTITY inserts may fail during save; dirty updates often fail at flush. Do not continue querying in a PostgreSQL transaction after a constraint violation: that transaction may be aborted or rollback-only. Give each failing case its own test/transaction. A uniqueness test proves rejection for those values, not a complete concurrency policy.

### 23. Why PostgreSQL integration needs PostgreSQL

H2 is a different engine. Compatibility modes do not make it PostgreSQL. Differences can hide bugs in:

- dialect and DDL syntax;
- identity generation;
- constraint timing/names;
- locking and isolation;
- transaction behavior;
- numeric, timestamp-with-time-zone, and other data types;
- Flyway SQL and PostgreSQL metadata.

When production uses PostgreSQL, use disposable PostgreSQL for tests of those integration boundaries. A passing test against another engine cannot establish PostgreSQL behavior.

### 24. Testcontainers with PostgreSQL

Testcontainers starts an ephemeral real PostgreSQL process in a container, gives the test dynamic connection details, and destroys it after the owning lifecycle. It requires a working Docker-compatible container runtime; this is test infrastructure, not Dockerizing the application for deployment.

Boot 4.1 service-connection dependencies are versionless under Boot management:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-testcontainers</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers-postgresql</artifactId>
  <scope>test</scope>
</dependency>
```

Reusable Boot-managed container bean (imports shown):

```java
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestPostgresConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:17")
                .withDatabaseName("testing_example");
    }
}
```

`@ServiceConnection` publishes JDBC connection details; Boot configures the Hikari `DataSource` without hard-coded ports or credentials. Boot manages the bean lifecycle with the test context.

Alternative: a static JUnit Testcontainers `@Container` plus `@DynamicPropertySource` can register `spring.datasource.url`, username, and password. Use it for libraries without a Boot connection-details factory or custom properties. Do not use both approaches for the same connection.
Import this configuration into the test context. A container declaration alone does not route an application to it. Keep the container alive for every context using its connection details; do not mix a short per-method container lifetime with a cached context. Image downloads and runtime discovery can fail before application assertions run. Pin a compatible database major version and investigate runtime failures separately.

### 25. Flyway in tests

The desired startup is:

```text
empty PostgreSQL container
  -> Hikari DataSource / pgJDBC
  -> Flyway migration history
  -> flyway_schema_history
  -> Hibernate ddl-auto=validate
  -> test method
```

This proves the production migration sequence can build a schema that current entities can use. `ddl-auto=create` would let Hibernate build a different schema and could conceal broken migrations. Keep Flyway as owner and Hibernate as validator.

Boot 4's focused `@DataJpaTest` does not automatically import every unrelated module. For a slice needing Flyway, explicitly import `FlywayAutoConfiguration` in the JPA slice and keep the real database:

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(TestPostgresConfiguration.class)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class RepositoryIntegrationTest { }
```
Migration SQL must live in source resources (normally `src/main/resources/db/migration`). Build output under `target` is disposable and cannot be the source of truth. A clean build is part of the reproducibility check. An empty-database migration test proves fresh installation; it does not prove every upgrade from an older populated production schema.

**Part takeaway:** Flush, reread and migrated schema are separate evidence from in-memory state.

<a id="transactions"></a>

## Part 6 — Transaction behavior

**Learning objective:** Observe outcomes after real service transaction boundaries finish.

### 26. Transactional tests

These annotations can look identical while controlling different transactions:

- `@Transactional` on an application service is production behavior, normally applied by a Spring proxy.
- `@Transactional` on a test is test infrastructure, normally wrapping the test and rolling it back afterward.

Transactional test rollback is excellent for repository isolation. It can hide:

- commit-time failures;
- detached behavior;
- incorrect service boundaries;
- `REQUIRES_NEW` survival;
- rollback-only failure at outer commit.

```text
flush != commit
```

Flush sends SQL inside the current transaction. A later rollback can undo it. Tests proving a service transaction boundary should have **no outer test transaction**, call the real proxied service, catch the expected exception, then query again through repositories.

### 27. Transaction rules, propagation and Spring proxies

A **proxy** is the Spring-managed wrapper that intercepts a call before/after invoking the target object. It applies transaction and method-security advice. Direct construction and ordinary self-invocation (`this.otherMethod()`) do not cross that wrapper.

```text
test -> injected service proxy -> transaction interceptor -> target
                                      |
                                      +-> begin/join transaction
                                      +-> return: commit (unless rollback-only)
                                      `-> throw: apply rollback rules
```

Under Spring's default rollback rules, unchecked exceptions (`RuntimeException` and `Error`) roll back; checked exceptions do not automatically roll back. Explicit `rollbackFor` or global configuration can change this, so inspect the application configuration.

| Pattern | Mechanism | Integration assertion |
|---|---|---|
| Normal return | Transaction commits | New read sees changed state |
| Runtime failure | Default rollback | New read sees original state |
| Checked failure | Default can commit | Assert state as well as exception |
| Checked + `rollbackFor` | Explicit rollback | Original state remains |
| Flush then runtime failure | SQL ran, transaction still open | Flushed changes disappear |
| Inner `REQUIRED` failure caught by outer | Shared transaction marked rollback-only | Outer may throw `UnexpectedRollbackException`; no partial commit |
| Inner `REQUIRES_NEW`, outer fails | Outer suspended; inner independent transaction | Inner row survives, outer changes disappear |

Call cooperating services through different injected beans where propagation is meant to apply. For these tests, omit an outer test `@Transactional`, set up data in committed repository calls, invoke the proxy, then read in a new transaction. Clean committed fixture data explicitly.

`REQUIRES_NEW` may need an additional pooled connection while the outer one is held; concurrent tests can exhaust a small pool. `readOnly=true` is generally an optimization hint, not a universal write prohibition. Tests using a real server or preemptive timeouts can execute work on another thread; test-thread rollback cannot be assumed to cover that work. Keep thread and transaction ownership explicit.

**Part takeaway:** An exception alone does not establish whether data committed.

<a id="security"></a>

## Part 7 — Security testing

**Learning objective:** Separate URL/method authorization, CSRF and real authentication.

### 28. Spring Security testing

`spring-security-test` supplies:

```java
@WithMockUser(username = "alice", roles = "USER")
```

and MockMvc helpers:

```java
get("/example/me").with(user("alice").roles("USER"));
post("/example/entries").with(user("admin").roles("ADMIN")).with(csrf());
formLogin("/example/login").user("alice").password("test-password");
authenticated().withUsername("alice");
unauthenticated();
```

`@WithMockUser` and `user(...)` place a synthetic identity in the security context/request. They can prove URL/method authorization. They do **not** prove:

- `user repository` lookup;
- `application UserDetailsService` mapping;
- BCrypt matching;
- `DaoAuthenticationProvider`;
- persistence of authentication in an HTTP session.

Use mock identity for authorization matrices; use real form login for the authentication flow.

### 29. Testing a SecurityFilterChain

Authorization asks whether an identity may act; authentication establishes that identity. Security filter tests keep the real filter chain and replace only downstream collaborators that are out of scope. Import the application's actual security configuration; a default generated filter chain does not establish its custom policy.

For an illustrative policy with public reads and administrator writes, design:

| Identity | Action | CSRF | Expected under that policy |
|---|---|---|---|
| Anonymous | Public read | Not needed | Reaches controller |
| Anonymous | Protected read | Not needed | Authentication entry point |
| Anonymous | Write | Missing | CSRF can reject before authorization |
| Anonymous | Write | Valid | Authentication required |
| Ordinary user | Administrator write | Valid | Access denied |
| Administrator | Write | Missing | CSRF rejection |
| Administrator | Write | Valid | Reaches controller |

Exact statuses/body depend on configured handlers. In a REST configuration, missing/invalid authentication commonly maps to 401; authorization or CSRF rejection commonly maps to 403. Inspect which filter/handler owns the failure. Assert that denied requests never reach the downstream service.

Do not disable filters in this class. A separate controller-contract slice can omit filters when security is explicitly outside its boundary.

### 30. CSRF testing

Cross-Site Request Forgery (CSRF) protection matters when browsers automatically attach credentials such as a session cookie. A CSRF token ties an unsafe request to the expected session/client context. Authorization and CSRF are separate checks; `permitAll()` does not disable CSRF.

```java
// MockMvc method-body fragment; real security chain, static security-test imports.
mockMvc.perform(post("/example/entries")
        .with(user("admin").roles("ADMIN"))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validJson));
```

Test valid, missing and invalid tokens (`csrf().useInvalidToken()`) for relevant unsafe requests. The helper is useful for policy tests but does not prove a real client can acquire and reuse the application's token.

For a session integration flow, use the application's token mechanism, retain the matching session, and send the returned parameter/header token on unsafe requests. Authentication/logout may rotate or clear token state; obtain a fresh token afterward as appropriate. Never disable CSRF just to make a protected request pass.

### 31. Method-security testing

`@PreAuthorize("hasRole('ADMIN')")` is method advice activated by method-security configuration. It is independent of URL rules.

```text
caller -> method-security interceptor -> authorized target method
```

Invoke the injected real service proxy in a context that enables method security. Use a synthetic ordinary user for denial and an administrator for success. Assert unchanged state/no downstream effect after denial, and relevant result/state after permission. Direct service calls isolate method security from HTTP authorization.

`roles="ADMIN"` generates authority `ROLE_ADMIN`; using `authorities` requires the full authority string. `new Service(...)` and a `@MockitoBean` service replacement cannot establish the real guard. Self-invocation can bypass the proxy just as with transactions. A successful administrator test alone cannot prove unauthorized callers are denied.

### 32. Authentication integration testing

For a form-login/session application, real authentication follows this path:

```text
form credentials + valid CSRF
  -> login filter -> AuthenticationManager -> authentication provider
  -> application UserDetailsService -> repository -> database
  -> PasswordEncoder.matches -> SecurityContext -> HTTP session
```

Use the existing application's authentication mechanism and configured processing URL/field names. Seed or register a disposable account with an encoded test password. Submit real form parameters, assert the configured success response, retain the returned session, and request authenticated data with that session. Also test wrong credentials and missing authentication.

Do not use `@WithMockUser` or request `user(...)` in a test claiming to prove login. They bypass credential verification. A real MockMvc login flow proves filter/provider/repository/session cooperation, but still does not prove real browser cookie handling, TLS, reverse proxies, or a deployed server.

### 33. Session and logout testing

```text
successful login
  -> SecurityContext saved in session
later request with same session
  -> authentication restored
logout with same session + valid CSRF
  -> authentication cleared and session invalidated
subsequent anonymous request
  -> configured unauthenticated response
```

`JSESSIONID` identifies the server-side session; it is not the authentication itself. MockMvc uses `MockHttpSession` rather than a real network cookie jar. Assert the session was invalidated and a later request is unauthenticated; do not print session IDs.

### 34. Password-security testing

Prove behavior without leaking secrets:

- unit test that registration invokes `encode(raw)`;
- capture the saved account and assert the stored value is the encoder result and is not raw;
- integration test that the correct password logs in;
- integration test that a wrong password fails;
- optionally use `encoder.matches` against the saved hash without printing either value.

Never assert an exact BCrypt result. BCrypt uses a random salt, so two correct encodings normally differ. Never include passwords, hashes, CSRF tokens, session IDs, authorization headers, or cookie headers in diagnostics.
If the encoder has a byte-length limit, distinguish Java string length from UTF-8 byte count. Exercise the actual service rule with multibyte input; a character-count DTO constraint alone may be insufficient.

**Part takeaway:** Synthetic users and real credentials answer different questions.

<a id="delivery"></a>

## Part 8 — Reliable suites, coverage and delivery

**Learning objective:** Keep tests isolated and diagnose failures through reproducible Maven runs.

### 35. Test data and fixtures

- **Factory method**: a small local `validEntry(...)` helper; best default.
- **Builder**: useful when an object has many optional fields and variants.
- **Fixture**: shared known data; keep it small and explicit.
- **Repository setup**: useful when testing real mappings/services.
- **SQL setup**: useful for exact database states, but can bypass entity invariants and become migration-coupled.

Prefer data near the test. Giant global fixtures make it unclear which row matters. Use descriptive unique business keys rather than random data unless randomness is the behavior under test.

### 36. Database cleanup and isolation

| Strategy | Strength | Risk/cost |
|---|---|---|
| transactional rollback | fast and automatic for repository tests | can hide commit boundaries; cannot model all `REQUIRES_NEW` behavior |
| explicit cleanup | proves real commits and supports full flows | must respect FK/order and run reliably |
| fresh context/container | strongest isolation | slow and defeats context caching |
| unique test data | reduces collisions | old state can still affect counts/queries |

Use slice rollback for repository tests. Use explicit repository cleanup for transaction/authentication full-context tests, which intentionally need commits. Never depend on method order.

### 37. Maven test lifecycle

```powershell
mvn test        # compile and run tests without deleting target first
mvn clean test  # clean build, then run tests
mvn verify      # run through the verification phase
```

Maven Surefire normally runs unit-style tests during `test`. Failsafe normally runs integration tests during `integration-test`/`verify`, but this beginner suite can remain under Surefire until separate lifecycles provide real value. Naming a class `*IntegrationTest` documents scope even if Surefire runs it.

Surefire commonly discovers `Test*`, `*Test`, `*Tests` and `*TestCase`; Failsafe commonly discovers `IT*`, `*IT` and `*ITCase`. A class ending `IntegrationTest` still matches `*Test`. Explicit plugin includes/excludes can change discovery. Use `mvn -Dtest=ExampleTest test` for focused feedback and inspect the test count: zero discovered tests is not proof of correctness.

`-DskipTests` normally compiles tests without executing them; `-Dmaven.test.skip=true` also skips test compilation. Neither validates runtime behavior. Quote comma-separated selectors in PowerShell, for example `mvn '-Dtest=FirstTest,SecondTest' test`.

### 38. IDE versus Maven

An IDE can use a different JDK, classpath, runner, working directory, or environment. “It passes in my IDE” is useful but incomplete. Run the reproducible project command:

```powershell
mvn clean test
```

The Docker runtime must be available for Testcontainers integration tests. A failure because Docker is unavailable is an infrastructure failure, not permission to fall back silently to H2.

<a id="coverage"></a>

### 39. Coverage and JaCoCo

Coverage records which code ran. It does not judge requirement completeness or assertion quality. An executed branch with no meaningful assertion can still contain an undetected bug.

| Metric | Question | Important limit |
|---|---|---|
| Statement | Did each executable source statement run? | Source statements and lines are not identical |
| Line | Did an instruction mapped to this line run? | Several statements/branches may share one line |
| Branch | Did decision alternatives run? | Does not establish all condition combinations, paths or data cases |

JaCoCo instruments Java bytecode with an agent and produces HTML/XML reports. Its counters include instructions, lines and branches; it does not publish a source-level statement counter. Exception handling is not counted as branches in JaCoCo. Read yellow/partially covered lines as questions to investigate, not as a demand for arbitrary extra tests. See [JaCoCo counters](https://www.jacoco.org/jacoco/trunk/doc/counters.html).

This optional Maven fragment belongs under `build/plugins`; it is a learning example, not a change applied to any companion project. Version 0.8.14 supports Java 21 and is an explicit example, not a claim about the latest release.

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.14</version>
  <executions>
    <execution>
      <id>coverage-agent</id>
      <goals><goal>prepare-agent</goal></goals>
    </execution>
    <execution>
      <id>coverage-report</id>
      <phase>verify</phase>
      <goals><goal>report</goal></goals>
    </execution>
  </executions>
</plugin>
```

After manually configuring it, run `mvn clean verify` and open `target/site/jacoco/index.html`. `prepare-agent` records execution; `report` renders it. An optional `check` goal can enforce a justified threshold. A report alone does not fail the build for low coverage. See the [Maven integration guide](https://www.jacoco.org/jacoco/trunk/doc/maven.html).

Preserve the agent's `argLine` if Surefire also configures JVM arguments (late expansion `@{argLine}` can help); do not disable forking. If no report appears, inspect test discovery, the execution-data file and agent arguments before blaming assertions. See [prepare-agent configuration](https://www.jacoco.org/jacoco/trunk/doc/prepare-agent-mojo.html).

Review uncovered high-risk decisions first. Ask whether each case would fail if its expected behavior broke. Even 100% branch coverage cannot establish missing requirements, all input partitions, real infrastructure, concurrency correctness or security completeness.

### CI/CD, reporting and diagnosing remote failures

**Learning objectives:** run the same Maven verification outside the IDE, retain useful reports, and distinguish code failures from environment failures.

**Continuous Integration (CI)** runs checks when changes are proposed or merged. **Continuous Delivery/Deployment (CD)** uses verified artifacts for release; tests are one gate and are not a proof that deployment itself works. Begin with one reproducible Maven job. Provision Java 21 and a working container runtime when the suite uses Testcontainers. Avoid adding a second database service when Testcontainers already owns the database.

The following illustrative `.github/workflows/tests.yml` assumes the Maven module is at the repository root. Set `working-directory`, the cache POM path and report paths when it lives in a subdirectory. It is a documentation example only:

```yaml
name: Java tests
on: [push, pull_request]
permissions:
  contents: read
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v6
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - name: Check test infrastructure
        run: docker info
      - name: Verify
        run: mvn --batch-mode --no-transfer-progress clean verify
      - name: Retain test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: |
            target/surefire-reports/
            target/failsafe-reports/
            target/site/jacoco/
          if-no-files-found: warn
```

The example follows the [GitHub Maven workflow guidance](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven). It preserves failure exit codes and uploads reports even after failure. A missing optional Failsafe/JaCoCo directory is expected when those plugins are absent.

Surefire's text and `TEST-*.xml` reports contain method failures, counts, errors and skips. Failsafe uses its own directory when configured. A failure is usually an unmet assertion; an error often means setup/runtime failed before a useful assertion. Read the earliest relevant root cause, not only the final Maven exception.

| CI symptom | First investigation |
|---|---|
| No tests discovered | Naming, engine/runner, includes/excludes, test count |
| Compilation failure | JDK, dependency versions, package/import mismatch |
| Container discovery/pull failure | Docker daemon, runner access, network/image cache |
| Context/schema startup error | Profiles, source migrations, Flyway and Hibernate logs |
| Passes alone, fails together | Leaked data, shared state, parallel cleanup collisions |
| Different numeric/time result | Locale, timezone, clock assumptions, platform differences |
| Only slow under CI | Resource limits, deterministic coordination, measured timeouts |

Reproduce the exact command/JDK locally; compare environment inputs and run the failing class alone. Do not hide failures with `continue-on-error`, test skips or automatic retries. Redact credentials and session/token data from reports. Check the suite can build from clean source without cached `target` output.

**Key takeaway:** reports are diagnostic evidence; a green job proves only the tests that were actually discovered and executed.

### 40. Common anti-patterns

- testing private methods directly;
- mocking everything, including records/entities;
- using `@SpringBootTest` for every behavior;
- asserting internal call order that is not a contract;
- shared mutable state and test ordering;
- using a production database;
- fixed `Thread.sleep` synchronization;
- exact BCrypt hash assertions;
- disabling all security/CSRF to make tests green;
- using H2 for PostgreSQL-specific behavior;
- never flushing JPA before a database assertion;
- assuming `@Transactional` on a test proves commit behavior;
- assuming `save()` always means SQL already ran;
- catching an exception without checking resulting state;
- asserting entire fragile JSON bodies when selected contract fields suffice.

### 41. Debugging failed tests

Use a layered order:

```text
read the assertion and root cause
  -> identify unit / MVC / JPA / full-flow layer
  -> inspect arrange/setup data
  -> inspect mock stubbing and unused/incorrect matchers
  -> inspect context/configuration and active profiles
  -> inspect Flyway/Hibernate SQL for DB tests
  -> inspect Security decision: authentication, role, or CSRF
  -> inspect transaction boundary, rollback-only, flush, and commit
```

Useful logging is targeted and temporary. Spring Security logs explain filter decisions; Hibernate SQL shows synchronization; transaction logs show begin/suspend/resume/commit/rollback; Flyway logs show migration validation. Never enable request logging that exposes credentials, tokens, session/cookie headers, or password hashes.

<a id="architecture-map"></a>

### 42. Testing architecture map

```text
JUnit Platform / Jupiter
    |
    +-- pure Java + AssertJ
    |
    +-- Mockito --------------------------- mocked collaborator boundary
    |
    `-- Spring TestContext
           |
           +-- MVC slice -> MockMvc -> [SecurityFilterChain when enabled]
           |                  -> DispatcherServlet -> Controller
           |                  -> mocked Service
           |
           +-- JPA slice -> Repository -> Hibernate -> JDBC -> HikariCP
           |                                      -> PostgreSQL Testcontainer
           |                                             ^
           |                                             `-- Flyway migrations
           |
           `-- full context -> SecurityFilterChain -> DispatcherServlet
                                -> Controller -> Service proxy
                                   -> @PreAuthorize -> @Transactional
                                   -> Repository -> Hibernate -> JDBC
                                   -> HikariCP -> PostgreSQL Testcontainer
                                                    ^
                                                    `-- Flyway at startup
```

| Layer in path | Unit | MVC slice | PostgreSQL JPA slice | Full integration |
|---|---:|---:|---:|---:|
| JUnit | yes | yes | yes | yes |
| Mockito | often | service bean replacement | normally no | normally no |
| Spring context | no | partial | partial | full |
| DispatcherServlet/JSON/validation | no | yes | no | yes |
| SecurityFilterChain | no | optional/yes in policy test | no | yes |
| service AOP proxy | no | service mocked | no | yes |
| Hibernate/JDBC/Hikari | no | no | yes | yes |
| Flyway/PostgreSQL | no | no | yes | yes |

For every test, be able to answer:

```text
What behavior is proved? What is real? What is mocked?
Which Spring infrastructure runs? Is PostgreSQL real? Does Flyway run?
Is Hibernate involved? Is the filter chain involved?
Is authentication real or synthetic? Where is the transaction?
Could this pass while production behavior is still broken?
```

### 43. Retrieval practice and independent review

Answer before opening the key or the companion solutions.

1. What behavior and oracle does your test have? Which requirement produced it?
2. Which partitions and boundaries did you choose, and why?
3. What is real, mocked and absent? Which failure could still escape?
4. Why is that the smallest sufficient boundary?
5. When do you need Spring, PostgreSQL, an HTTP server or none of them?
6. How do `@Mock`, `@InjectMocks` and `@MockitoBean` differ?
7. What does a synthetic user fail to prove about authentication?
8. Why do flush, clear and commit answer different questions?
9. How can an outer test transaction conceal a service defect?
10. Why can 100% statement coverage miss a decision error?
11. How would you diagnose a failure seen only in CI?
12. Could an AI-generated test pass with the wrong production behavior?

<details>
<summary>Answer cues — check after answering aloud</summary>

1. Observable output/state/interaction, compared with an independent requirement-derived expected result.
2. Representative equivalence classes, valid/invalid neighbors at relevant limits, and meaningful decision/state combinations.
3. Name components explicitly; mocked infrastructure cannot establish real cooperation.
4. Enough real components to observe the risk, with fewer unrelated failure causes.
5. Spring for container/proxy/framework behavior; PostgreSQL for its SQL/schema/transaction semantics; a real server for network behavior.
6. Plain mock, injection into a plain object, replacement/addition of a context bean respectively.
7. Repository lookup, password matching, provider/filter login and session persistence.
8. SQL synchronization, removal of managed cached instances, transaction completion respectively.
9. It can roll back work that would commit or fail at a different boundary in production.
10. All statements can execute while some false outcomes or decision combinations remain absent.
11. Read reports/root cause; compare JDK, discovery, profiles, runtime availability, fixtures and timing.
12. Look for invented rules, tautological assertions, over-mocking, disabled security, swallowed failures and missing negative cases.

</details>

Mutation testing, property-based testing and performance testing are optional future study. First become able to design, implement, debug and explain the core suite manually.

**Part takeaway:** A healthy suite is meaningful, maintainable, deterministic and explainable.

## Documentation references

Package and framework examples were checked against the project's managed generation and official documentation. Context7's nearest indexed Boot release was 4.1.0; exact companion-project versions are recorded in the exercise baseline rather than assumed from that index.

- [Spring Boot testing](https://docs.spring.io/spring-boot/4.1/reference/testing/index.html) and [test slices](https://docs.spring.io/spring-boot/4.1/appendix/test-auto-configuration/slices.html).
- [JUnit 6.0.3 user guide](https://docs.junit.org/6.0.3/user-guide/).
- [Mockito API](https://javadoc.io/doc/org.mockito/mockito-core/5.23.0/org.mockito/org/mockito/Mockito.html).
- [Spring bean overrides](https://docs.spring.io/spring-framework/reference/testing/annotations/integration-spring/annotation-mockitobean.html) and [test transactions](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html).
- [Spring Security testing](https://docs.spring.io/spring-security/reference/servlet/test/index.html).
- [Testcontainers PostgreSQL module](https://java.testcontainers.org/modules/databases/postgres/).

Use the [exercise competency checklist](Testing_Exercise.md#competency-checklist) to demonstrate these skills before asking AI to accelerate implementation.
