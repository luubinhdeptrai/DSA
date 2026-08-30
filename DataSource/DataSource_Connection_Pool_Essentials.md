# DataSource and Connection Pool Essentials

> A focused guide for Java developers who already know JDBC and want to understand what frameworks later automate.

**Baseline:** Java 17+, standard JDBC, `javax.sql.DataSource`, PostgreSQL, and HikariCP.  
**Excluded:** Spring, Spring Boot, JPA, Hibernate, and advanced pool tuning.

## How to use this guide

1. Understand the physical-versus-logical connection model first.
2. Memorize the lifecycle rules and the items marked **MUST KNOW**.
3. Type the short examples.
4. Complete `DataSource_Connection_Pool_Exercise.md`.
5. Answer the self-check questions without looking back.

### Priority legend

| Marker | Meaning | Study approach |
|---|---|---|
| ⭐⭐⭐⭐⭐ **MUST KNOW** | Everyday correctness | Explain and apply without notes |
| ⭐⭐⭐⭐ **IMPORTANT** | Common production concern | Understand and recognize normal usage |
| ⭐⭐⭐ **NICE TO KNOW** | Helpful operating context | Know the purpose, not every detail |
| ⭐⭐ **LEARN LATER** | Advanced tuning/diagnostics | Recognize the term and postpone depth |

## The one-minute mental model

```text
Application code
      ↓ asks
DataSource abstraction
      ↓ implemented here by
HikariDataSource / connection pool
      ↓ manages
reusable physical PostgreSQL connections
```

For each operation:

```text
dataSource.getConnection()
      ↓
borrow a logical Connection handle
      ↓
execute JDBC work
      ↓
connection.close()
      ↓
return the healthy physical connection to the pool
```

The most important distinction is:

> `DataSource` does not automatically mean “connection pool.” It is an interface. HikariCP is one pooling implementation of that interface.

---

## 1. Why Database Connections Are Expensive

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

A JDBC `Connection` represents a live database session, not merely a small local Java object. Establishing a new physical PostgreSQL connection can involve:

```text
application asks the JDBC driver to connect
        ↓
open a TCP network connection
        ↓
optionally negotiate TLS encryption
        ↓
exchange PostgreSQL startup messages
        ↓
authenticate the user
        ↓
create server-side session resources
        ↓
initialize connection/session state
        ↓
Connection is ready for SQL
```

The exact protocol details vary by configuration, but the important fact is stable: connection setup needs network round trips, authentication, driver work, and database resources.

### The inefficient pattern

```java
public Student findById(long id) throws SQLException {
    try (Connection connection = DriverManager.getConnection(
            jdbcUrl, username, password)) {
        // execute one query
    }
}
```

If every request performs this sequence, the application repeatedly pays setup and teardown cost:

```text
request 1 → open physical connection → query → physically close
request 2 → open physical connection → query → physically close
request 3 → open physical connection → query → physically close
```

At low traffic this may work. Under concurrent backend traffic it wastes time and can create connection storms that pressure PostgreSQL.

### What pooling changes

Connection pooling pays physical setup cost less often. A bounded collection of physical connections is reused across many short operations.

Pooling does **not** make a slow SQL query fast. It mainly makes connection acquisition cheaper and controls how many database sessions the application can use.

**Common mistake:** Optimizing SQL and connection setup as if they were the same problem. Pooling reduces connection setup overhead; indexes and query design address SQL execution.

**Remember:** Creating a database session is expensive enough that backend applications normally reuse physical connections.

---

## 2. What `DataSource` Is

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

`javax.sql.DataSource` is a standard Java interface that acts as a **factory for JDBC connections**.

```java
import javax.sql.DataSource;

public final class StudentRepository {
    private final DataSource dataSource;

    public StudentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int countStudents() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // use JDBC
            return 0;
        }
    }
}
```

```text
DataSource
    ↓ getConnection()
Connection
```

### Why it is an abstraction

Repository code knows only:

> “I can ask this object for a JDBC `Connection`.”

It does not need to know whether the implementation:

- opens a new physical connection each time;
- borrows from a connection pool;
- is supplied by an application server;
- is replaced by a controlled implementation in a test.

That separates **how connections are created/managed** from **how SQL is executed**.

### Critical distinction: DataSource ≠ pool

```text
javax.sql.DataSource (interface)
├── non-pooling implementation → may open a physical connection per call
└── pooling implementation     → may borrow a reusable connection
```

`HikariDataSource` is both:

- an implementation of `DataSource`; and
- the public facade/lifecycle owner for a HikariCP connection pool.

A `DataSource` reference alone does not prove pooling exists. You must know which implementation created it.

### Why real applications prefer it to direct `DriverManager` calls

- connection configuration is centralized;
- repositories depend on a standard interface;
- a single pool can be shared safely by many repositories;
- pool/lifecycle policy stays outside SQL methods;
- callers do not repeat URL and credential handling.

`DataSource` is in package `javax.sql`, which belongs to Java SE's `java.sql` module. The `javax` name here is correct; it is not a legacy Spring import.

**Common mistake:** Saying “I use `DataSource`, therefore I have pooling.” Ask which concrete implementation is behind it.

**Remember:** `DataSource` answers “where do Connections come from?” A pool is one possible answer.

---

## 3. `DriverManager` vs `DataSource`

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

| Concern | `DriverManager` | `DataSource` |
|---|---|---|
| Main role | Find a JDBC driver and open a connection from URL/credentials | Standard connection-factory abstraction |
| Configuration | Often repeated near call sites in beginner code | Usually centralized during application startup |
| Pooling by itself | No | Not guaranteed; depends on implementation |
| Test/replacement boundary | Static global API is harder to replace | Constructor can receive another implementation |
| Common use | Tiny demos, diagnostics, learning basic JDBC | Normal backend application architecture |
| Typical call | `DriverManager.getConnection(...)` | `dataSource.getConnection()` |

Both return objects implementing `java.sql.Connection`. Your `PreparedStatement`, `ResultSet`, transaction, commit, and rollback knowledge still applies.

### Manual connection creation

```java
try (Connection connection = DriverManager.getConnection(
        jdbcUrl, username, password)) {
    // this call normally creates a new physical database connection
}
```

### DataSource-based acquisition

```java
try (Connection connection = dataSource.getConnection()) {
    // behavior depends on the DataSource implementation
}
```

Use `DriverManager` when a tiny standalone diagnostic truly needs one connection. Prefer an application-lifetime `DataSource` in ordinary backend code.

**Remember:** The JDBC work after acquisition looks similar; ownership and connection management are what change.

---

## 4. What a Connection Pool Is

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

A connection pool manages a bounded set of reusable physical database connections.

### Pool startup and use

```text
Application startup
        ↓
create one Connection Pool
        ↓
pool creates/maintains physical DB connections as configured

Database operation
        ↓
borrow Connection
        ↓
execute JDBC work
        ↓
connection.close()
        ↓
close the logical handle
        ↓
healthy physical connection becomes available in the pool
```

The word **borrow** describes temporary exclusive use. The operation does not own the underlying database connection forever.

### The essential `close()` behavior

With a pool, the `Connection` returned to application code is usually a wrapper/proxy around a physical connection. Calling:

```java
connection.close();
```

normally:

1. ends the application's use of that logical handle;
2. lets the pool clean/reset relevant state;
3. makes a healthy physical connection available for reuse.

It may physically close the underlying connection if that connection is broken, expired, evicted, or the pool is shutting down.

This is different from:

```java
hikariDataSource.close();
```

which shuts down the **pool itself**.

**Common mistake:** Avoiding `connection.close()` because you want reuse. Closing the borrowed handle is exactly how you enable safe reuse.

**Remember:** Borrow late, use briefly, close promptly.

---

## 5. Physical Connection vs Logical/Pooled Connection

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

### Physical connection

A physical connection represents the real driver/network/database session:

```text
JDBC driver
    ↕ TCP/TLS/protocol
PostgreSQL server session
```

It is comparatively expensive to establish and consumes database resources while open.

### Logical connection handle

HikariCP normally gives application code a proxy implementing `Connection`:

```text
Application
    ↓ holds
logical/proxy Connection handle
    ↓ delegates to
pool entry
    ↓ owns
physical JDBC connection
    ↓ communicates with
PostgreSQL
```

On logical `close()`:

```text
logical handle becomes closed/unusable
        ↓
pool receives the physical connection back
        ↓
another operation can receive a new logical handle over it
```

Never keep using the old handle after `close()`. Its contract says it is closed, even if the pool retains the underlying physical session.

### One physical connection, several operations over time

```text
time →

Operation A: [logical handle A] ──close──┐
                                        ↓
Physical connection P:  ─────────── reused ───────────
                                        ↑
Operation B:                    [logical handle B] ──close
```

The operations do not use the physical connection simultaneously. The pool loans it to one borrower at a time.

**Remember:** Application code closes a logical lease; the pool decides the physical connection's lifecycle.

---

## 6. Why Connection Pooling Improves Performance

**Priority: ⭐⭐⭐⭐ IMPORTANT**

Pooling helps by:

- avoiding repeated physical connection establishment;
- reusing authenticated database sessions;
- limiting the number of sessions the application can create;
- allowing concurrent threads to borrow different connections;
- making connection acquisition usually much faster while idle connections exist;
- smoothing short bursts instead of opening a new connection for every request.

```text
Without pool:  setup → use → teardown | setup → use → teardown
With pool:     borrow → use → return   | borrow → use → return
               └──── physical connection reused ────┘
```

A pool also provides **backpressure**: when the maximum is active, later callers wait instead of creating unlimited database sessions.

### What pooling does not guarantee

- It does not make every query faster.
- It does not fix missing indexes.
- It does not remove transaction rules.
- It does not make a `Connection` safe to share concurrently.
- It does not eliminate database connection limits.

**Remember:** Pooling optimizes and bounds connection lifecycle; it does not replace database performance work.

---

## 7. Connection Pool Lifecycle

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

```text
application starts
       ↓
create ONE application-lifetime HikariDataSource
       ↓
pool initializes and creates/maintains connections
       ↓
repositories/services repeatedly borrow and return handles
       ↓
application begins orderly shutdown
       ↓
HikariDataSource.close()
       ↓
pool stops and physical connections are closed
```

Pool implementations may create connections eagerly, lazily, or in the background depending on configuration and demand. Do not depend on an exact creation instant unless you have a real requirement.

### Correct ownership

```text
Main / application lifecycle owner
    └── creates HikariDataSource once
          ├── passed as DataSource to BookRepository
          ├── passed as DataSource to another repository
          └── closed once at application shutdown
```

Do not create a pool in a repository method. Pool creation is application infrastructure, not per-operation work.

Here, “one pool” means one shared pool for this single database configuration. An application that intentionally talks to different databases or database identities may own one lifecycle-managed pool for each configuration.

**Remember:** One pool is long-lived; borrowed connections are short-lived.

---

## 8. HikariCP

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW for basic use; ⭐⭐ LEARN LATER for tuning**

HikariCP is a small, production-oriented JDBC connection-pool implementation. It is commonly used because it focuses on speed, simple configuration, reliability, and a small runtime footprint. `HikariDataSource` implements `javax.sql.DataSource`, so repository code can depend on the standard interface while application startup uses Hikari-specific configuration and shutdown APIs.

```text
Repository sees:  DataSource
                      ↑ implemented by
Startup owns:     HikariDataSource
                      ↓ controls
                  HikariCP pool
```

### Maven dependency

```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>7.1.0</version>
</dependency>
```

HikariCP does not include a PostgreSQL JDBC driver. Add pgJDBC separately in a PostgreSQL application:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.13</version>
</dependency>
```

These versions are explicit so the build is reproducible. HikariCP 7.x and pgJDBC 42.7.x work with Java 17. Modern pgJDBC registers through JDBC's service-provider mechanism, so normal code does not need `Class.forName("org.postgresql.Driver")` or a Hikari `driverClassName` setting.

### Minimal programmatic configuration

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.time.Duration;

public final class DataSourceFactory {
    private DataSourceFactory() {
    }

    public static HikariDataSource create(
            String jdbcUrl,
            String username,
            String password
    ) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("practice-pool");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(4);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(Duration.ofSeconds(5).toMillis());
        config.setIdleTimeout(Duration.ofMinutes(1).toMillis());
        config.setMaxLifetime(Duration.ofMinutes(5).toMillis());

        return new HikariDataSource(config);
    }
}
```

The password is a method argument obtained from external configuration. It is not a string literal in source code.

HikariCP timeout and lifetime setters use **milliseconds**. `Duration.toMillis()` makes that unit explicit in the example.

### Settings you must understand

| Setting | Meaning | Priority |
|---|---|---|
| `jdbcUrl` | Driver URL, such as `jdbc:postgresql://localhost:5432/pool_practice` | ⭐⭐⭐⭐⭐ MUST KNOW |
| `username` | Database login name | ⭐⭐⭐⭐⭐ MUST KNOW |
| `password` | Database credential; obtain externally and never log it | ⭐⭐⭐⭐⭐ MUST KNOW |
| `maximumPoolSize` | Maximum total physical connections managed by the pool, including idle and borrowed connections | ⭐⭐⭐⭐⭐ MUST KNOW |
| `connectionTimeout` | Maximum time a caller waits to borrow a connection before acquisition fails | ⭐⭐⭐⭐⭐ MUST KNOW |

`connectionTimeout` is **not** a SQL query timeout and not a network socket timeout. It specifically limits waiting for a connection from this pool.

### Settings to recognize, then tune later

| Setting | Beginner meaning | Priority |
|---|---|---|
| `minimumIdle` | Minimum number of idle connections Hikari tries to maintain | ⭐⭐⭐ NICE TO KNOW; tuning can wait |
| `idleTimeout` | How long excess idle connections may remain before retirement; relevant when `minimumIdle < maximumPoolSize` | ⭐⭐ LEARN LATER |
| `maxLifetime` | Maximum lifetime of a physical pool connection before planned retirement; an in-use connection is not retired until returned | ⭐⭐ LEARN LATER |
| `poolName` | Human-readable name used in diagnostics | ⭐⭐⭐ NICE TO KNOW |
| leak detection threshold | Optional diagnostic that can report unusually long-held connections | ⭐⭐ LEARN LATER |

If `minimumIdle` is not set, its HikariCP default is the same as `maximumPoolSize`, producing fixed-size behavior. HikariCP recommends leaving it unset for maximum responsiveness; the exercise sets a smaller explicit minimum only to make changing idle capacity observable. Do not copy arbitrary timeout values into production; real values depend on PostgreSQL, network infrastructure, and workload.

### What you actually need to remember

```text
Correct URL + credentials
        ↓
small intentional maximumPoolSize
        ↓
bounded connectionTimeout
        ↓
measure before tuning lifetime/idle details
```

**Common mistake:** Treating every visible setting as a knob that must be optimized immediately. Safe resource ownership matters more at this stage.

---

## 9. Getting a Connection from the Pool

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

The normal usage remains standard JDBC:

```java
try (Connection connection = dataSource.getConnection()) {
    try (PreparedStatement statement = connection.prepareStatement(
            "SELECT id, title FROM books ORDER BY id")) {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                // map each row
            }
        }
    }
}
```

### What happens at `dataSource.getConnection()`

For a Hikari-backed `DataSource`, conceptually:

1. The caller asks the pool for a connection.
2. If a healthy idle physical connection is available, Hikari reserves it for this caller.
3. If capacity remains, the pool may create a physical connection.
4. If every connection is borrowed and the pool is at its maximum, the caller waits.
5. Hikari returns a logical/proxy object implementing `Connection`.
6. The physical connection is counted as active/borrowed until the handle is closed.

The exact internal selection and validation strategy is an implementation detail; application code relies on the `DataSource` and `Connection` contracts.

### What happens at `connection.close()`

Conceptually, Hikari:

1. marks the logical handle closed;
2. cleans up tracked JDBC resources/state as needed;
3. returns a healthy physical connection to the idle pool;
4. or discards/closes it if it should no longer be reused.

Then another thread may borrow the physical connection through a different logical handle.

### Scope rule

```java
try (Connection connection = dataSource.getConnection()) {
    // one short unit of JDBC work
} // return promptly, including when an exception occurs
```

Do not store this connection in an instance field. The `DataSource` is shared; each operation borrows its own connection.

**Remember:** `getConnection()` acquires a lease; `close()` releases it.

---

## 10. Pool Exhaustion

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Suppose `maximumPoolSize` is 3:

```text
Thread 1 → borrowed Connection A
Thread 2 → borrowed Connection B
Thread 3 → borrowed Connection C
Thread 4 → asks for a connection
              ↓
          no idle connection
          pool already at maximum
              ↓
          wait for a return
```

If A, B, or C is returned before `connectionTimeout`, Thread 4 receives a connection and continues. If not, acquisition fails with a `SQLException`—in HikariCP typically a transient connection exception describing the timeout.

```text
all connections borrowed
        ↓
new caller waits
        ↓
connection returned in time?
   ├── yes → caller borrows it
   └── no  → acquisition timeout / SQLException
```

Waiting is not automatically a bug. It is bounded backpressure. Persistent waiting means you should investigate:

- connections held too long;
- connection leaks;
- slow queries or transactions;
- unexpectedly high concurrency;
- database trouble preventing new physical connections;
- a pool size inappropriate for the measured workload.

Do not respond by immediately setting the pool size to a huge number.

**Common mistake:** Confusing connection acquisition timeout with statement execution timeout.

**Remember:** Exhaustion means all allowed pool connections are unavailable; callers wait only up to `connectionTimeout`.

---

## 11. Connection Leaks

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

A **connection leak** occurs when code borrows a connection and fails to return it after the work ends.

### Incorrect: every path leaks the connection

```java
public void insertBook(DataSource dataSource, Book book)
        throws SQLException {
    Connection connection = dataSource.getConnection();
    PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO books(title, author) VALUES (?, ?)");

    statement.setString(1, book.title());
    statement.setString(2, book.author());
    statement.executeUpdate();

    // BUG: statement and connection are never closed
}
```

If this happens repeatedly:

```text
borrow → never return
borrow → never return
borrow → never return
        ↓
pool reaches maximum
        ↓
later callers wait and time out
```

### Correct: nested try-with-resources

```java
public void insertBook(DataSource dataSource, Book book)
        throws SQLException {
    String sql = "INSERT INTO books(title, author) VALUES (?, ?)";

    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, book.title());
        statement.setString(2, book.author());
        statement.executeUpdate();
    }
}
```

Try-with-resources closes in reverse declaration order and runs even when JDBC throws. For a pooled connection, this reliably returns the lease.

HikariCP offers leak-detection diagnostics for connections held unusually long, but that is a debugging aid—not a substitute for correct ownership. It reports a possible leak; it does not reclaim the connection, and a legitimate long operation can also trigger a warning.

**Remember:** A bounded pool makes leaks visible quickly. Every successful `getConnection()` must have a guaranteed `close()` path.

---

## 12. Thread Safety and Concurrent Requests

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

A single Hikari `DataSource`/pool is designed to be shared by many application threads. Each operation should borrow a separate connection handle.

```text
                 same shared pool
                /       |       \
Thread 1 → Connection A |        |
Thread 2 →──────── Connection B  |
Thread 3 →──────────────── Connection C
```

More precisely:

```text
Thread 1 ─┐
Thread 2 ─┼─→ one HikariDataSource → bounded physical connections
Thread 3 ─┘
```

### Safe ownership pattern

```java
public void printAll() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
        // only this operation uses this handle
    }
}
```

### Unsafe pattern

```java
public final class BookRepository {
    private final Connection sharedConnection; // unsafe lifetime/ownership
}
```

JDBC `Connection`, `PreparedStatement`, and `ResultSet` objects should not be shared across unrelated concurrent operations. Their transaction state and cursor/statement state would interfere.

The pool coordinates concurrent borrowing; your code owns one borrowed handle for one operation/transaction.

**Common mistake:** Assuming that because the pool is thread-safe, every connection obtained from it can be shared between threads.

**Remember:** Share the `DataSource`; do not share a borrowed `Connection` across unrelated work.

---

## 13. Pool Size

**Priority: ⭐⭐⭐⭐ IMPORTANT**

`maximumPoolSize` is the maximum number of physical connections Hikari manages in the pool, including both:

```text
total pool connections = idle connections + borrowed/active connections
```

It is not:

- one connection per user;
- one connection per repository;
- one connection per Java thread forever;
- a target that must be made as large as possible.

### Beginner tradeoff

```text
too small
    ↓
callers wait even when PostgreSQL could handle more useful work

too large
    ↓
too many database sessions, memory/process pressure, and excessive concurrency
```

Start with an intentional small value for a small application, observe the workload, database limits, acquisition wait, and query duration, then adjust from evidence. Complicated sizing formulas are unnecessary at this stage.

Remember that every running application instance normally owns its own pool:

```text
possible PostgreSQL sessions
    approximately application instances × maximumPoolSize
```

A maximum of 10 across five application instances can therefore allow roughly 50 application pool sessions, before counting administration and monitoring connections.

**Common mistake:** Increasing the pool to hide a leak or a slow transaction. That delays the symptom instead of fixing the cause.

**Remember:** The pool is a resource limit as well as a performance tool.

---

## 14. DataSource and Transactions

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Transactions still belong to a JDBC `Connection`. The `DataSource` supplies the connection; it does not make separate connections share one local JDBC transaction.

```text
DataSource
    ↓ borrow ONE Connection
transaction begins on that Connection
    ├── operation 1 uses it
    ├── operation 2 uses it
    └── operation 3 uses it
commit or rollback that same Connection
    ↓
close handle → return to pool
```

### Correct transaction boundary

```java
public void registerBookAndEvent(
        DataSource dataSource,
        BookRepository repository,
        Book book
) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
        connection.setAutoCommit(false);

        try {
            long bookId = repository.insert(connection, book);
            repository.insertEvent(connection, bookId, "BOOK_CREATED");
            connection.commit();
        } catch (SQLException | RuntimeException failure) {
            try {
                connection.rollback();
            } catch (SQLException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }
    }
}
```

The overloads used above accept the already-borrowed connection. They must not call `dataSource.getConnection()` internally.

### Incorrect transaction split

```text
service wants one transaction
    ↓
repository method 1 borrows Connection A
repository method 2 borrows Connection B
    ↓
two independent JDBC transaction contexts
```

Explicitly commit on success and roll back on failure. Do not rely on closing a connection with unfinished work as your transaction strategy. Hikari cleans/resets connection state when a handle returns, but your code should make the outcome intentional.

**Common mistake:** Turning off auto-commit on one connection while repository methods silently borrow other connections.

**Remember:** One JDBC transaction = one appropriate borrowed `Connection` used for all its operations.

---

## 15. Application Architecture

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

```text
Service
   ↓ calls
Repository
   ↓ asks
DataSource
   ↓ implemented by
Connection Pool
   ↓ uses
JDBC Driver
   ↓ speaks protocol to
PostgreSQL
```

### Responsibilities

| Layer/component | Responsibility |
|---|---|
| Service | Coordinate a business use case and own multi-operation transaction boundaries |
| Repository | Execute SQL, bind parameters, map rows, and borrow short-lived connections for standalone operations |
| `DataSource` | Standard connection-factory interface presented to application code |
| Connection pool | Create, track, lend, reset, reuse, limit, and retire physical connections |
| JDBC driver | Implement JDBC calls and PostgreSQL's wire-protocol communication |
| PostgreSQL | Authenticate sessions, execute SQL, enforce constraints, and store data |

### Composition at startup

```java
HikariDataSource pool = DataSourceFactory.create(
        settings.jdbcUrl(),
        settings.username(),
        settings.password());
BookRepository repository = new BookRepository(pool);
BookRegistrationService service =
        new BookRegistrationService(pool, repository);
```

There is no need for a custom `ConnectionProvider` interface merely to hide `DataSource`; Java already provides the useful abstraction.

Repositories may receive the shared `DataSource` through constructor injection even without Spring. This is ordinary Java composition.

**Common mistake:** Putting connection-pool configuration inside each repository. Pool construction belongs to application infrastructure/startup.

**Remember:** Business code coordinates work, repositories own SQL, and the pool owns reusable connection lifecycle.

---

## 16. Configuration and Secrets

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Database location and credentials differ between developer machines, tests, and deployments. Do not hard-code them in Java source.

### Environment variables

PowerShell for the current terminal process:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/pool_practice'
$env:DB_USERNAME = 'pool_app'
$env:DB_PASSWORD = '<your local database password>'
```

Read them without printing their values:

```java
public record DatabaseSettings(
        String jdbcUrl,
        String username,
        String password
) {
    public static DatabaseSettings fromEnvironment() {
        return new DatabaseSettings(
                required("DB_URL"),
                required("DB_USERNAME"),
                required("DB_PASSWORD")
        );
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + name
            );
        }
        return value;
    }
}
```

This error reports the missing variable's **name**, never its secret value.

### Properties-file alternative

`database.properties.example` may safely contain placeholders:

```properties
db.url=jdbc:postgresql://localhost:5432/pool_practice
db.username=pool_app
db.password=replace-me
```

Copy it to a local `database.properties`, use your real password, and ignore that real file:

```gitignore
database.properties
```

Do not commit credentials. For this exercise, environment variables are simpler because there is no secret file to protect.

### Never log configuration blindly

Avoid code such as:

```java
System.out.println(settings); // record toString would include password
```

Print safe diagnostics such as pool name, active count, or database URL host only if needed; never print the password.

**Remember:** Configuration is external; secrets stay out of source control and logs.

---

## 17. Pool Shutdown

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

Borrowed connections and the pool have different lifecycles:

```text
Connection.close()
    → end one borrow and normally return physical connection

HikariDataSource.close()
    → stop the entire pool and close its managed physical connections
```

For a console application, make the pool the outer resource:

```java
public static void main(String[] args) throws Exception {
    DatabaseSettings settings = DatabaseSettings.fromEnvironment();

    try (HikariDataSource dataSource = DataSourceFactory.create(
            settings.jdbcUrl(),
            settings.username(),
            settings.password())) {
        BookRepository repository = new BookRepository(dataSource);
        // run application work
    } // pool shuts down here
}
```

`HikariDataSource` is closeable. The `DataSource` interface itself does not declare `close()`, so the application lifecycle owner should retain the concrete `HikariDataSource` reference. Repositories should still depend on `DataSource` and must not shut it down.

Closing the pool matters because it releases PostgreSQL sessions, network sockets, and Hikari's housekeeping resources. Stop accepting new application work and let in-flight database work finish before closing the pool; pool shutdown is not a request-draining mechanism. In a long-running server, shutdown is connected to the application's lifecycle hook. In this console exercise, try-with-resources is enough.

**Common mistakes:** Calling `dataSource.close()` after every query, or never calling it at all. Close borrowed handles per operation; close the pool once.

**Remember:** The code that creates the pool owns its final shutdown.

---

## 18. DataSource and Connection Pools in Spring Later

**Priority: ⭐⭐⭐ NICE TO KNOW**

Spring and Spring Boot will later automate much of the composition and lifecycle work:

```text
configuration
    ↓
framework creates/manages DataSource and pool
    ↓
repositories borrow Connections
    ↓
JDBC driver communicates with PostgreSQL
```

The underlying model does not disappear:

```text
DataSource → connection pool → Connection → JDBC → database
```

Spring transaction management also still needs an appropriate connection to represent the database transaction. Understanding borrow/use/return, pool limits, and leaks will make later framework behavior easier to diagnose.

Do not learn Spring Boot pool properties yet. First become comfortable creating and owning one pool manually.

---

## 19. Common Beginner Mistakes

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW**

| Mistake | Why it is wrong | Correct mental model |
|---|---|---|
| Believing every `DataSource` is a pool | The interface promises connection creation, not reuse | Identify the concrete implementation |
| Forgetting to close borrowed connections | Leases accumulate until the pool is exhausted | Every acquisition needs guaranteed try-with-resources |
| Creating a pool for every repository call | Repeats expensive setup and creates many independent pools | Create one application-lifetime pool |
| Keeping one `Connection` permanently in a singleton repository | Mixes transaction state, fails under concurrency, and prevents normal return | Store `DataSource`; borrow per operation/transaction |
| Creating a new `HikariDataSource` for every request | Pools are heavyweight lifecycle objects, not request objects | Share one pool across requests |
| Setting pool size extremely high without evidence | Pushes resource/concurrency pressure onto PostgreSQL | Start intentionally and measure |
| Confusing `Connection.close()` with pool shutdown | One releases a lease; the other stops all pool resources | Close connections often, pool once |
| Hard-coding credentials | Leaks secrets through source/history/builds | Read environment or protected ignored config |
| Sharing one `Connection` across unrelated threads | Transaction and statement state interfere | Threads share the pool, not a connection handle |
| Forgetting pool shutdown | Leaves sessions/sockets/resources open until process termination | Lifecycle owner calls `HikariDataSource.close()` |
| Assuming pooling fixes slow SQL | Pooling reduces setup overhead, not query work | Measure and tune SQL separately |
| Swallowing acquisition/SQL errors | Hides the cause and may fake success | Propagate `SQLException` or preserve it as a cause |
| Committing one connection and expecting another to join | Local JDBC transactions are connection-bound | Pass the same connection to every transactional operation |
| Holding a connection while doing unrelated slow work | Reduces available pool capacity | Borrow immediately before JDBC work and return promptly |

---

## 20. Important Terminology

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW as a reference**

| Term | Meaning |
|---|---|
| `DataSource` | Standard `javax.sql` interface used to obtain JDBC connections |
| `DriverManager` | Static JDBC facility that selects a driver and opens a connection from URL/properties |
| `Connection` | JDBC interface representing a database session/transaction context from the caller's perspective |
| Physical connection | Actual driver/network/PostgreSQL session managed by the pool |
| Logical connection | Borrower-facing proxy/handle over a pooled physical connection |
| Connection pool | Component that owns and reuses a bounded collection of physical connections |
| Borrow / acquire | Temporarily obtain exclusive use of one pooled connection handle |
| Return / release | Close the logical handle so the pool can reuse/discard the physical connection |
| Pool size | Number/limit of physical connections managed by the pool |
| Connection timeout | Maximum wait to acquire a pooled connection; not the SQL query timeout |
| Connection leak | A borrowed connection that application code fails to return promptly/correctly |
| HikariCP | Concrete JDBC connection-pool library; `HikariDataSource` implements `DataSource` |

---

## 21. Final Mental Model

**Priority: ⭐⭐⭐⭐⭐ MUST KNOW as a summary**

### Without pooling

```text
operation 1
   ↓
open physical connection
   ↓
authenticate / create session
   ↓
use JDBC
   ↓
close physical connection

operation 2
   ↓
open another physical connection
   ↓
repeat setup ...
```

### With pooling

```text
              Connection Pool
           /         |         \
   Physical A   Physical B   Physical C
       ↑             ↑             ↑
       └──── reusable DB sessions ─┘

application operation
        ↓
dataSource.getConnection()
        ↓
borrow logical handle
        ↓
use PreparedStatement / ResultSet / transaction
        ↓
connection.close()
        ↓
return healthy physical connection to pool
```

### Whole backend view

```text
many request threads
        ↓
services and repositories
        ↓
one shared DataSource / HikariCP pool
        ↓
bounded reusable physical connections
        ↓
pgJDBC
        ↓
PostgreSQL
```

### The five rules

1. Create the pool once.
2. Share the `DataSource`.
3. Borrow a connection for one short operation/transaction.
4. Always close the borrowed connection.
5. Close the pool once during application shutdown.

---

## 22. Knowledge Priorities

### Memorize and practice

| Concept | Priority | What you should be able to do |
|---|---|---|
| `DataSource` does not guarantee pooling | ⭐⭐⭐⭐⭐ MUST KNOW | Explain the interface/implementation distinction |
| Borrow/use/close lifecycle | ⭐⭐⭐⭐⭐ MUST KNOW | Write correct try-with-resources naturally |
| Logical versus physical connection | ⭐⭐⭐⭐⭐ MUST KNOW | Explain what pooled `close()` normally means |
| One shared pool, short-lived borrowed handles | ⭐⭐⭐⭐⭐ MUST KNOW | Design correct ownership |
| Connection leak and exhaustion | ⭐⭐⭐⭐⭐ MUST KNOW | Recognize how one causes the other |
| One connection per JDBC transaction | ⭐⭐⭐⭐⭐ MUST KNOW | Pass the same handle to all operations |
| External credentials and pool shutdown | ⭐⭐⭐⭐⭐ MUST KNOW | Keep secrets safe and close lifecycle resources |

### Understand; look up syntax when needed

| Concept | Priority | Required depth |
|---|---|---|
| `maximumPoolSize` meaning | ⭐⭐⭐⭐⭐ MUST KNOW | Know what the limit counts |
| Pool-size tuning tradeoff | ⭐⭐⭐⭐ IMPORTANT | Understand too-small/too-large outcomes |
| `connectionTimeout` meaning | ⭐⭐⭐⭐⭐ MUST KNOW | Distinguish acquisition wait from query timeout |
| Exact timeout choice | ⭐⭐ LEARN LATER | Choose from deployment evidence rather than copying values |
| Thread sharing model | ⭐⭐⭐⭐⭐ MUST KNOW | Share pool, not connection |
| Hikari metrics/pool name | ⭐⭐⭐ NICE TO KNOW | Use active/idle/total/waiting counts for observation |
| `minimumIdle` behavior | ⭐⭐⭐ NICE TO KNOW | Recognize idle-capacity policy |
| `idleTimeout`, `maxLifetime`, leak diagnostics | ⭐⭐ LEARN LATER | Tune only with deployment knowledge and measurements |
| Detailed pool-sizing formulas and JMX administration | ⭐⭐ LEARN LATER | Not needed before normal backend practice |

---

## Official References

- [Java `DataSource` API](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/javax/sql/DataSource.html)
- [HikariCP configuration reference](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [PostgreSQL JDBC driver documentation](https://jdbc.postgresql.org/documentation/)

---

## 23. Self-Check Questions

Do not look back until you have answered in your own words.

1. Why does opening a physical PostgreSQL connection cost more than obtaining an ordinary Java object?
2. What does `javax.sql.DataSource` promise, and what does it **not** promise?
3. When is `DriverManager` still reasonable, and why is `DataSource` normally preferred in backend applications?
4. What is the difference between a physical connection and the logical `Connection` handle Hikari gives application code?
5. What normally happens when `connection.close()` is called on a healthy Hikari connection?
6. How is `connection.close()` different from `HikariDataSource.close()`?
7. What happens when every connection is borrowed and another thread calls `getConnection()`?
8. How can one leaked connection eventually affect otherwise correct requests?
9. Which object may be shared by many threads: the `DataSource`, a borrowed `Connection`, or both?
10. What exactly does `maximumPoolSize` limit?
11. Why can setting `maximumPoolSize` extremely high harm PostgreSQL?
12. Why must all statements in one local JDBC transaction use the same appropriate connection?
13. Where should the pool be created and where should it be closed?
14. Why should database credentials stay out of Java source and diagnostic output?
15. Which Hikari settings must you understand now, and which should you postpone tuning until you have measurements?
