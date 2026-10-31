# Java Time API — Essential Concepts for Backend Developers

**Level:** Beginner → practical Java backend development  
**Target:** Java 21, Spring Boot, PostgreSQL, JUnit  
**Focus:** `Clock`, `Instant`, `Duration`, `Period`, `LocalDate`, `LocalDateTime`, `OffsetDateTime`, `ZonedDateTime`, `ZoneId`, and reliable time-based tests  
**Example project:** CapsuleAI — account registration, login sessions, JWT expiration, refresh-token rotation

> **Core principle:** A backend should get the current time from an **injected `Clock`**, represent an event's moment using **`Instant`**, represent elapsed time using **`Duration`**, and convert to a user's **time zone only when local calendar time matters**.

## 1. Learning objectives

After reading this guide, you should be able to:

1. Distinguish *a moment in time*, *a local date/time*, *a time zone*, and *a length of time*.
2. Choose the appropriate `java.time` class for a backend field.
3. Explain why `Instant.now(clock)` is preferable to `Instant.now()` inside business logic.
4. Inject a `Clock` into Java/Spring Boot services without coupling your domain code to Spring.
5. Calculate session and JWT expiration correctly, including the exact expiration boundary.
6. Write deterministic tests using `Clock.fixed()` and `Clock.offset()`.
7. Handle UTC, Vietnam's time zone, API timestamps, and PostgreSQL `TIMESTAMPTZ` safely.
8. Avoid the most common bugs involving `LocalDateTime`, daylight saving, and time precision.

---

**How to use the code:** Each section contains an independent teaching example. Some examples reuse class names to demonstrate alternative designs; do not paste every example unchanged into the same Java package. The production pattern for Clean Architecture is in Section 7.

---

## 2. First mental model: four different questions

Imagine a user logs into CapsuleAI at **10:00 AM in Vietnam** and receives an access token valid for **15 minutes**.

| Question | Answer | Java type |
|---|---|---|
| **At exactly what moment did login happen?** | `2026-10-31T03:00:00Z` | `Instant` |
| **What local time did the user see?** | `2026-10-31 10:00` in Vietnam | `ZonedDateTime` |
| **How long is the token valid?** | 15 minutes | `Duration` |
| **Where does the application get “now”?** | Its injected time source | `Clock` |

Why is 10:00 in Vietnam equal to 03:00 UTC? Vietnam is UTC+07:00, so subtract seven hours to express the *same moment* in UTC.

```text
                       ONE MOMENT
                2026-10-31T03:00:00Z
                         |
          +--------------+--------------+
          |                             |
       UTC view                    Vietnam view
     03:00 (+00:00)                10:00 (+07:00)
          |                             |
          +---------- Same event -------+
```

**Important:** The `Z` at the end of an ISO-8601 timestamp means a zero UTC offset (*Zulu* time). An `Instant` is not a “Vietnam time” or a “New York time”; it identifies one moment on the timeline.

---

## 3. Which `java.time` type should I use?

| Priority | Type | Represents | Typical backend use | Example |
|---|---|---|---|---|
| ★★★★★ | `Instant` | An exact point on the timeline | `createdAt`, `expiresAt`, `revokedAt`, token timestamps | `2026-10-31T03:00:00Z` |
| ★★★★★ | `Clock` | Replaceable source of the current instant (and zone) | Production time and deterministic tests | `Clock.systemUTC()` |
| ★★★★★ | `Duration` | A fixed elapsed amount of time | 15-minute JWT TTL, 30 × 24-hour session TTL | `Duration.ofMinutes(15)` |
| ★★★★☆ | `ZoneId` | Time-zone rules or a region | Convert moments for a user | `Asia/Ho_Chi_Minh` |
| ★★★★☆ | `LocalDate` | A calendar date, without time or zone | Birth date, calendar-based day | `2026-10-31` |
| ★★★★☆ | `ZonedDateTime` | Local date/time plus region's zone rules | Show an event in a user's time zone | `2026-10-31T10:00+07:00[Asia/Ho_Chi_Minh]` |
| ★★★☆☆ | `OffsetDateTime` | Local date/time with a UTC offset | API timestamp and PostgreSQL JDBC `timestamptz` mapping | `2026-10-31T10:00+07:00` |
| ★★★☆☆ | `LocalDateTime` | Date and wall-clock time **without zone** | A local meeting time *before* choosing its zone | `2026-10-31T10:00` |
| ★★★☆☆ | `Period` | Calendar years, months, days | Add one calendar month to an invoice date | `Period.ofMonths(1)` |
| ★★☆☆☆ | `LocalTime` | Time of day without a date or zone | Daily shop opening time | `09:00` |
| ★★☆☆☆ | `DateTimeFormatter` | Parse or display date/time text | Human-readable UI/log formatting | `dd/MM/yyyy HH:mm` |

**Rule of thumb:** For database event timestamps and authorization expirations, choose `Instant`. For business-calendar concepts such as a birth date, choose `LocalDate`. Do **not** use `LocalDateTime` to represent a universal event timestamp.

---

## 4. `Instant`: a moment on the timeline

`Instant` is an immutable, thread-safe representation of a point in time.

```java
import java.time.Instant;
import java.time.Duration;

public class InstantExample {
    public static void main(String[] args) {
        Instant createdAt = Instant.parse("2026-10-31T03:00:00Z");
        Instant expiresAt = createdAt.plus(Duration.ofMinutes(15));

        System.out.println(createdAt); // 2026-10-31T03:00:00Z
        System.out.println(expiresAt); // 2026-10-31T03:15:00Z
        System.out.println(createdAt.isBefore(expiresAt)); // true
    }
}
```

### Most useful `Instant` methods

| Priority | Method | What it does |
|---|---|---|
| ★★★★★ | `Instant.now(clock)` | Gets **now from the supplied `Clock`**. |
| ★★★★★ | `Instant.parse(text)` | Reads ISO-8601 instant text such as `...Z`. |
| ★★★★★ | `instant.plus(duration)` | Returns a **new** instant later on the timeline. |
| ★★★★★ | `instant.minus(duration)` | Returns a **new** earlier instant. |
| ★★★★★ | `isBefore(other)` / `isAfter(other)` | Strict chronological comparison. |
| ★★★★★ | `equals(other)` | Checks whether two instants represent the same instant. |
| ★★★★☆ | `Duration.between(a, b)` | Finds elapsed time between instants. |
| ★★★★☆ | `instant.atZone(zone)` | Views that moment in a named time zone. |
| ★★★☆☆ | `instant.toEpochMilli()` | Epoch milliseconds (may lose sub-millisecond precision). |

**Immutability matters:** `createdAt.plus(...)` does **not** modify `createdAt`; it returns another `Instant`.

### Exact expiration semantics

For a session whose expiration is `expiresAt`, a common security rule is:

```java
boolean usable = now.isBefore(expiresAt);
```

That means:

| Current time | `now.isBefore(expiresAt)` | Result |
|---|---:|---|
| One nanosecond before expiration | `true` | Usable |
| Exactly equal to expiration | `false` | Expired |
| One nanosecond after expiration | `false` | Expired |

Do **not** implement the same rule with `!now.isAfter(expiresAt)`; that expression allows use **at** expiration, changing the boundary.

---

## 5. `Clock`: *where* does “now” come from?

`Clock` is an abstract JDK class that provides access to a time source and a zone. Unlike `Instant`, a `Clock` is not itself a timestamp.

```text
Clock ─── ask for the current moment ───> Instant
  |                                       |
  |                                       +── immutable value
  +── changeable choice of time source
      (real clock, fixed clock, offset clock, test clock)
```

### No injected clock (works, but harder to test)

```java
import java.time.Instant;

public class SessionService {
    public Instant createExpiry() {
        return Instant.now().plusSeconds(15 * 60);
    }
}
```

The system clock is **hardcoded**. A test cannot directly tell the service “pretend the current time is 10:00.” Tests that sleep and wait for expiry are slow and unreliable.

### Injected clock (recommended)

```java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class SessionPolicy {
    private final Clock clock;

    public SessionPolicy(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public Instant newAccessTokenExpiry() {
        Instant now = clock.instant();
        return now.plus(Duration.ofMinutes(15));
    }

    public boolean isUsable(Instant expiresAt) {
        Instant now = Instant.now(clock);
        return now.isBefore(expiresAt);
    }
}
```

Here, `clock.instant()` and `Instant.now(clock)` obtain the instant from the same injected clock. Either form is fine; use one convention consistently.

`SessionPolicy` depends **only on Java standard-library classes**, so it can live in a Clean Architecture application/domain layer without importing Spring.

### Common `Clock` factories

| Priority | API | Meaning | Where to use |
|---|---|---|---|
| ★★★★★ | `Clock.systemUTC()` | Real system clock with UTC zone | Production backend |
| ★★★★★ | `Clock.fixed(instant, zone)` | Clock that **always** returns exactly one instant | Unit tests |
| ★★★★★ | `Clock.offset(base, duration)` | Clock shifted forward/backward relative to another clock | Tests of future/past |
| ★★★☆☆ | `Clock.system(zone)` | Real clock associated with a chosen zone | Calendar-oriented logic |
| ★★★☆☆ | `clock.withZone(zone)` | Clock using same time source but a different zone | Date calculations in a particular zone |
| ★★☆☆☆ | `Clock.tick(base, duration)` | Clock quantized to ticks | Special-purpose clocks; rarely needed for auth |

**Don't confuse time source with time zone:** `Clock.systemUTC()` still represents the current universal moment, not a different “UTC reality.” Time-zone choice affects interpretation as local date/time; it does not make the actual current instant seven hours earlier.

---

## 6. Why `Clock.fixed()` makes testing easy

Suppose a JWT expires 15 minutes after issuance. A test should be able to check *before*, *exactly at*, and *after* expiration without waiting 15 real minutes.

```java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionPolicyTest {

    private static final Instant LOGIN_TIME =
            Instant.parse("2026-10-31T03:00:00Z");

    @Test
    void sessionIsUsableBeforeExpiryButNotAtExpiry() {
        Clock startClock = Clock.fixed(LOGIN_TIME, ZoneOffset.UTC);
        Instant expiresAt = LOGIN_TIME.plus(Duration.ofMinutes(15));

        Clock beforeExpiry = Clock.offset(startClock, Duration.ofMinutes(14));
        Clock atExpiry = Clock.offset(startClock, Duration.ofMinutes(15));
        Clock afterExpiry = Clock.offset(startClock, Duration.ofMinutes(16));

        assertTrue(new SessionPolicy(beforeExpiry).isUsable(expiresAt));
        assertFalse(new SessionPolicy(atExpiry).isUsable(expiresAt));
        assertFalse(new SessionPolicy(afterExpiry).isUsable(expiresAt));
    }
}
```

What happens:

1. `LOGIN_TIME` is known exactly.
2. `Clock.fixed(...)` never moves by itself.
3. `Clock.offset(...)` gives us a different fixed time for each scenario.
4. No `Thread.sleep(...)` is required.
5. The test is fast, repeatable, and checks the precise boundary.

**Caution:** `Clock.offset(fixedClock, ...)` produces another fixed clock. It does not advance the original object dynamically. When you need to move the same injected clock while exercising a stateful service, use a well-designed *mutable test clock* instead; keep that helper in test code.

### Testing an entire request flow

For a controller/service/integration test, it may be easiest to supply a test `Clock` bean to the Spring context, then make all application components use that bean. Otherwise, one class could read a fixed clock while another secretly calls `Instant.now()` and makes the test inconsistent.

**What a fake clock can prove:** Your Java policy reacts correctly to chosen timestamps.

**What it cannot prove by itself:** PostgreSQL locking, real JWT validator settings, database clock behavior, or the complete Spring Security filter chain. Those require appropriate integration tests.

---

## 7. Spring Boot: configuring and injecting one `Clock`

For a Spring Boot backend, define one production clock bean in the infrastructure/configuration layer.

**`TimeConfiguration.java`**

```java
package com.example.time;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
```

**`SessionPolicy.java` — pure Java application logic**

```java
package com.example.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public final class SessionPolicy {
    private final Clock clock;

    public SessionPolicy(Clock clock) {
        this.clock = clock;
    }

    public Instant sessionExpiresAt() {
        return clock.instant().plus(Duration.ofDays(30));
    }

    public boolean sessionIsUsable(Instant expiresAt) {
        return clock.instant().isBefore(expiresAt);
    }
}
```

**`SessionPolicyConfiguration.java` — wires application logic**

```java
package com.example.time;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionPolicyConfiguration {

    @Bean
    public SessionPolicy sessionPolicy(Clock clock) {
        return new SessionPolicy(clock);
    }
}
```

Execution:

```text
Spring Boot starts
     |
     v
@Bean Clock.systemUTC()
     |
     v
@Bean SessionPolicy(clock)
     |
     v
SessionPolicy reads clock.instant()
     |
     v
Instant: createdAt / expiresAt
```

**Clean Architecture note:** `Clock` belongs to the JDK, not to Spring. Passing `Clock` into an application service is commonly acceptable because it doesn't import infrastructure libraries. Spring configuration is responsible for constructing the production clock and wiring it into the application.

**Single snapshot rule:** At the start of a logical operation, read `Instant now = clock.instant()` once and reuse it to calculate related timestamps. Don't call `now()` separately for `createdAt` and `expiresAt` if they must derive from the exact same moment.

---

## 8. `Duration` vs. `Period`: elapsed time versus calendar time

These types answer different questions.

### `Duration`: exact elapsed amount

```java
import java.time.Duration;
import java.time.Instant;

Instant issuedAt = Instant.parse("2026-10-31T03:00:00Z");
Duration accessTtl = Duration.ofMinutes(15);
Instant accessExpiresAt = issuedAt.plus(accessTtl);

Duration sessionTtl = Duration.ofDays(30); // exactly 30 × 24 hours
Instant sessionExpiresAt = issuedAt.plus(sessionTtl);
```

Useful methods:

| Priority | Method | Purpose |
|---|---|---|
| ★★★★★ | `Duration.ofSeconds(n)` / `ofMinutes(n)` / `ofHours(n)` / `ofDays(n)` | Create elapsed durations. |
| ★★★★★ | `Duration.between(start, end)` | Calculate elapsed time. |
| ★★★★☆ | `duration.toSeconds()` / `toMinutes()` | Whole elapsed units. |
| ★★★☆☆ | `duration.isNegative()` / `isZero()` | Validate durations. |

### `Period`: calendar amounts

```java
import java.time.LocalDate;
import java.time.Period;

LocalDate billingDate = LocalDate.of(2026, 1, 31);
LocalDate nextBillingDate = billingDate.plus(Period.ofMonths(1));

System.out.println(nextBillingDate); // 2026-02-28
```

A calendar month isn't always 30 days. A calendar day in a daylight-saving region can also be 23 or 25 hours. `Duration.ofDays(1)` is **always 24 hours**; `Period.ofDays(1)` means **one calendar day** when applied to calendar-aware types.

**CapsuleAI:** A 15-minute access-token TTL and a fixed 30-day (30 × 24-hour) maximum session lifetime naturally fit `Duration`. A reminder such as “every first day of the month at 09:00 local time” needs calendar-aware date/time logic.

---

## 9. Time zones: `ZoneId`, `ZoneOffset`, `ZonedDateTime`

### Convert an `Instant` to Vietnamese local time

```java
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

Instant occurredAt = Instant.parse("2026-10-31T03:00:00Z");
ZoneId vietnam = ZoneId.of("Asia/Ho_Chi_Minh");

ZonedDateTime localView = occurredAt.atZone(vietnam);
System.out.println(localView);
// 2026-10-31T10:00+07:00[Asia/Ho_Chi_Minh]
```

**The moment did not change.** Only its human-readable interpretation changed.

### `ZoneId` versus `ZoneOffset`

- `ZoneOffset.ofHours(7)` is a **fixed** UTC+07:00 offset.
- `ZoneId.of("Asia/Ho_Chi_Minh")` identifies a named region with a maintained set of time-zone rules.
- `ZoneId.of("America/New_York")` is a region whose UTC offset changes with daylight-saving rules.

For user-facing scheduling, prefer a region `ZoneId` when the actual geographic time zone matters; an offset alone cannot describe future daylight-saving transitions.

### `LocalDateTime` does **not** identify a universal moment

```java
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;

LocalDateTime userEntered = LocalDateTime.of(2026, 10, 31, 10, 0);
Instant actualMoment = userEntered
        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        .toInstant();
```

`LocalDateTime.of(2026, 10, 31, 10, 0)` could mean 10:00 in Vietnam, Tokyo, London, or elsewhere. You need a zone or offset before it represents a unique instant. Some local times in daylight-saving regions are ambiguous or nonexistent.

### Practical selection

- **Database audit timestamp:** `Instant`
- **JWT `iat` / `exp`:** `Instant` for application calculations; JWT NumericDate is generally represented in epoch seconds.
- **User's birth date:** `LocalDate`
- **10 AM next Monday in the user's region:** `LocalDateTime` + `ZoneId`, or `ZonedDateTime`
- **Timestamp with explicit offset in an API:** `OffsetDateTime` (or ISO-8601 `Instant` in UTC)

---

## 10. `OffsetDateTime`: what it adds

`OffsetDateTime` combines a local date/time and a numeric offset. Like `Instant`, it can identify a moment, but it also preserves the chosen **offset representation**, not the full rules of a named geographic region.

```java
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

Instant instant = Instant.parse("2026-10-31T03:00:00Z");
OffsetDateTime vietnamOffsetView = instant.atOffset(ZoneOffset.ofHours(7));

System.out.println(vietnamOffsetView); // 2026-10-31T10:00+07:00
System.out.println(vietnamOffsetView.toInstant().equals(instant)); // true
```

Choose `ZonedDateTime` when region-specific historical/future time-zone rules matter; choose `OffsetDateTime` when a numeric offset is enough.

---

## 11. CapsuleAI example: access tokens and absolute session expiration

A login occurs at `10:00` Vietnam time (`03:00Z`). The access-token TTL is 15 minutes and the login session ends 30 × 24 hours after login.

```java
import java.time.Duration;
import java.time.Instant;

Instant loggedInAt = Instant.parse("2026-10-31T03:00:00Z");
Instant sessionEndsAt = loggedInAt.plus(Duration.ofDays(30));

Instant accessExpiresAt = loggedInAt.plus(Duration.ofMinutes(15));

System.out.println(sessionEndsAt);      // 2026-11-30T03:00:00Z
System.out.println(accessExpiresAt);    // 2026-10-31T03:15:00Z
```

When a refresh occurs near the 30-day session maximum, a new access token **must not outlive the session** (per the current Sprint design):

```java
Instant refreshedAt = sessionEndsAt.minus(Duration.ofMinutes(5));
Instant desiredAccessExpiry = refreshedAt.plus(Duration.ofMinutes(15));

Instant actualAccessExpiry = desiredAccessExpiry.isBefore(sessionEndsAt)
        ? desiredAccessExpiry
        : sessionEndsAt;

System.out.println(actualAccessExpiry.equals(sessionEndsAt)); // true
```

**Mental model:** `accessExpiresAt = min(now + accessTTL, sessionEndsAt)`.

### Exactly at expiration

```java
boolean authorized = now.isBefore(accessExpiresAt)
        && now.isBefore(sessionEndsAt);
```

This illustrates **time conditions only**. Actual authentication must also validate the JWT signature and claims, account/session identity, revocation status, and relevant policies. Do not substitute these two comparisons for the real Spring Security authentication chain.

### JWT clock-skew warning

Your business code may use an injected `Clock`, while a JWT decoder may have a **separate** clock and/or permitted clock skew. For exact-boundary tests, configure or test the decoder's effective clock/skew deliberately; injecting a `Clock` into one service alone does not guarantee the JWT library uses it.

---

## 12. PostgreSQL and JDBC: storing moments safely

For event instants, PostgreSQL normally uses `TIMESTAMP WITH TIME ZONE`, commonly written `TIMESTAMPTZ`:

```sql
CREATE TABLE example_session (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);
```

**What `TIMESTAMPTZ` means:** PostgreSQL represents an absolute moment and normalizes it for storage; it does **not** preserve the user's original time-zone name such as `Asia/Ho_Chi_Minh`. Results can be displayed using the database session's configured time zone.

### A subtle pgJDBC detail

The PostgreSQL JDBC driver's documented native Java 8+ mapping for `TIMESTAMPTZ` is **`OffsetDateTime`**, not `Instant` directly. Therefore, when using explicit JDBC mappings, you can convert at the adapter boundary:

```java
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

// Java domain → JDBC parameter:
Instant expiresAt = Instant.parse("2026-10-31T03:15:00Z");
OffsetDateTime jdbcValue = expiresAt.atOffset(ZoneOffset.UTC);

// JDBC result → Java domain (inside a RowMapper):
// Instant restored = resultSet
//         .getObject("expires_at", OffsetDateTime.class)
//         .toInstant();
```

For example, in Spring JDBC `JdbcClient`:

```java
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

// Given an existing JdbcClient jdbcClient and a valid UUID sessionId:
Instant expiresAt = jdbcClient.sql("""
        SELECT expires_at
        FROM example_session
        WHERE id = :id
        """)
        .param("id", sessionId)
        .query((rs, rowNum) -> rs.getObject("expires_at", OffsetDateTime.class).toInstant())
        .single();

// For writes, bind an OffsetDateTime when using pgJDBC native time mappings:
OffsetDateTime parameter = expiresAt.atOffset(ZoneOffset.UTC);
```

This is **illustrative adapter code**, not a complete working repository file. Some frameworks and mappings handle `Instant` conversion for you; always check your actual driver and mapping conventions instead of assuming `ResultSet.getObject(..., Instant.class)` works.

**Precision caution:** Java `Instant` supports nanoseconds; PostgreSQL timestamps typically have microsecond precision. Round-trip values may not preserve all nanoseconds. Avoid tests that expect nanosecond-exact equality across a database round trip unless you normalize precision.

---

## 13. Formatting, parsing, and API timestamps

### Recommended machine-readable format: ISO-8601

```java
import java.time.Instant;

Instant timestamp = Instant.parse("2026-10-31T03:00:00Z");
String serialized = timestamp.toString();

System.out.println(serialized); // 2026-10-31T03:00:00Z
```

For an API, a timestamp with an explicit UTC marker or offset is much less ambiguous than `"31/10/2026 10:00"`.

### Human-readable local format

```java
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

DateTimeFormatter formatter = DateTimeFormatter
        .ofPattern("dd/MM/uuuu HH:mm")
        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

String display = formatter.format(
        Instant.parse("2026-10-31T03:00:00Z")
);

System.out.println(display); // 31/10/2026 10:00
```

**Recommended boundary:** Store and transmit unambiguous instants, then format for display using the user's intended time zone.

---

## 14. Common mistakes and why they matter

| Mistake | Why it is a problem | Better approach |
|---|---|---|
| `Instant.now()` directly inside every use case | Tests cannot control time consistently | Inject `Clock`; use `clock.instant()` |
| `LocalDateTime.now()` for an event timestamp | Local date/time lacks time zone | Use `Instant` for moments |
| Mixing server-local and user-local time | Different servers may use different default zones | Store UTC instants and convert explicitly |
| `now.isAfter(expiresAt)` as the only expiry check | Equality is accidentally treated as unexpired | Expired when `!now.isBefore(expiresAt)` |
| Calling `clock.instant()` repeatedly for one logical decision | Small inconsistencies between timestamps | Capture one `now` and reuse it |
| Using `Period.ofMonths(1)` as “30 days” | Months have varying lengths | `Duration.ofDays(30)` when the rule is 720 hours |
| Using `Duration.ofDays(1)` for “same local time tomorrow” | DST may shift the wall-clock hour | `ZonedDateTime.plus(Period.ofDays(1))` when calendar days matter |
| Assuming `Clock.fixed()` moves automatically | It always returns the same instant | Make a new offset clock or a mutable test clock |
| Assuming injecting `Clock` controls every library | JWT validator, database, external systems may read separate clocks | Configure/test each time source explicitly |
| Assuming PostgreSQL preserves zone IDs | `TIMESTAMPTZ` does not store region name | Store the user's `ZoneId` separately if needed |
| Comparing DB timestamps at nanosecond precision | PostgreSQL and Java may differ in precision | Normalize/truncate if appropriate |
| Using `System.currentTimeMillis()` to measure latency | Wall-clock adjustments can distort elapsed durations | Use `System.nanoTime()` for measuring elapsed time |
| Assuming the system clock always moves forward | Real clock can be adjusted | Use database synchronization for concurrency; monotonic timing only for elapsed measurements |

### Security-specific reminder

A controllable `Clock` is a **testability tool**, not a substitute for database transactions, locking, JWT signature verification, or replay detection.

---

## 15. Quick-reference recipes

### Recipe A: Get a timestamp

```java
Instant now = clock.instant();
```

### Recipe B: Add an access-token TTL

```java
Instant expiresAt = now.plus(Duration.ofMinutes(15));
```

### Recipe C: Enforce strict expiry

```java
boolean expired = !clock.instant().isBefore(expiresAt);
```

### Recipe D: Find time remaining

```java
Duration remaining = Duration.between(clock.instant(), expiresAt);
```

`remaining` is negative after expiry; clamp to `Duration.ZERO` when a UI needs a non-negative countdown.

### Recipe E: Get today's calendar date for Vietnam

```java
ZoneId vietnam = ZoneId.of("Asia/Ho_Chi_Minh");
LocalDate todayInVietnam = LocalDate.now(clock.withZone(vietnam));
```

### Recipe F: Convert a local time to an instant

```java
LocalDateTime vietnamTime = LocalDateTime.of(2026, 10, 31, 10, 0);
Instant moment = vietnamTime
        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        .toInstant();
```

### Recipe G: Write a stable time-based test

```java
Instant base = Instant.parse("2026-10-31T03:00:00Z");
Clock testClock = Clock.fixed(base, ZoneOffset.UTC);
```

### Recipe H: Measure code execution duration

```java
long startNanos = System.nanoTime();
try {
    // perform operation
} finally {
    long elapsedNanos = System.nanoTime() - startNanos;
    System.out.println("Elapsed nanos: " + elapsedNanos);
}
```

Use this for relative measurements, not for timestamps stored in the database.

---

## 16. Practice exercises (for manual Java learning)

**Exercise 1 — Expiration boundary.** A JWT is issued at `2026-10-31T03:00:00Z` and expires 15 minutes later. Write Java code that evaluates `usable` at `03:14:59Z`, `03:15:00Z`, and `03:15:01Z`. Expected: `true`, `false`, `false`.

**Exercise 2 — Injected clock.** Write a pure Java `SessionPolicy` class taking a `Clock`. Add `newExpiry(Duration ttl)` and `isUsable(Instant expiry)`. Test its behavior with `Clock.fixed` and `Clock.offset`; do not use `Thread.sleep`.

**Exercise 3 — Time-zone conversion.** Convert `2026-10-31T03:00:00Z` to the `Asia/Ho_Chi_Minh` local date/time. Explain why the two representations refer to one moment.

**Exercise 4 — Absolute session lifetime.** At `2026-10-31T03:00:00Z`, a login session starts with a 30-day maximum. A refresh occurs 5 minutes before that maximum. Calculate the new access token's expiration when its nominal TTL is 15 minutes. Expected: the session's original `expiresAt`, **not** 10 minutes beyond it.

**Exercise 5 — Database mapping.** Explain why a domain field may be `Instant` while a pgJDBC parameter/result for `TIMESTAMPTZ` is handled through `OffsetDateTime` in the persistence adapter.

**Exercise 6 — Design choice.** Classify these fields: `birthday`, `createdAt`, `lastLoginAt`, `dailyReminderLocalTime`, `accessTokenTTL`, and `userTimeZone`.

<details>
<summary>Answers to Exercise 6</summary>

- `birthday` → `LocalDate`
- `createdAt` → `Instant`
- `lastLoginAt` → `Instant`
- `dailyReminderLocalTime` → `LocalTime` together with a `ZoneId` when scheduling actual occurrences
- `accessTokenTTL` → `Duration`
- `userTimeZone` → `ZoneId` (or its string ID in persistence)

</details>

---

## 17. Final mental model: remember these five lines

```text
Clock           = Where does my program get NOW?
Instant         = WHEN exactly did something happen?
Duration        = HOW LONG has elapsed / should elapse?
ZoneId          = WHICH time-zone rules should interpret that moment?
LocalDate(Time) = WHAT calendar date / wall-clock time, without a zone?
```

For CapsuleAI authentication, the default pattern is:

```java
Instant now = clock.instant();
Instant expiresAt = now.plus(Duration.ofMinutes(15));
boolean stillUsable = clock.instant().isBefore(expiresAt);
```

In a single business operation, prefer using the same captured `now` for related calculations and comparisons. In later requests, read a new `now` from the clock.

**The most valuable takeaway:** Use `Instant` for the timestamp, `Duration` for TTL, `Clock` for deterministic time, and explicit time zones when interacting with human calendars.

---

## References (official documentation)

1. Oracle Java 21 — [`Clock`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html)
2. Oracle Java 21 — [`Instant`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html)
3. Oracle Java 21 — [`Duration`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Duration.html)
4. Oracle Java 21 — [`Period`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Period.html)
5. Oracle Dev.java — [Working with Clock](https://dev.java/learn/date-time/clock/)
6. pgJDBC — [Java date/time type mappings](https://vlsi.github.io/pgjdbc/documentation/data-types/date-time/) (consult the driver's current date/time documentation for the supported mappings)
7. PostgreSQL — [Date/Time Types](https://www.postgresql.org/docs/current/datatype-datetime.html)

*Note: The CapsuleAI examples illustrate the existing Sprint 1 design discussed in this conversation. They are educational and do not imply that the project implementation has already passed compilation or tests.*
