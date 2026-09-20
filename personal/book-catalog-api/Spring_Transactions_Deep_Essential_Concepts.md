# Spring Transactions — Deep Essential Concepts

> A first-principles guide to making a Spring service method, a JPA persistence context, one or more Hibernate SQL statements, a pooled JDBC connection, and PostgreSQL behave as one deliberate unit of work.

**Project baseline:** Java 21, Spring Boot 4.1.1, Spring Framework 7.0.9, Spring Data JPA, Hibernate ORM 7.4.5.Final, pgJDBC, HikariCP with `maximum-pool-size: 5`, PostgreSQL 17, and `spring.jpa.open-in-view: false`.

**Companion project:** `JPA_Hibernate/book-catalog-api`. This guide extends that project and its existing service, repository, entity, DTO, validation, and global-exception architecture.

**Learning goal:** Be able to predict transaction start, entity state, flush, SQL execution, commit, rollback, locking, connection use, and exception behavior without saying “Spring does magic.”

The previous guides already established the pieces. This guide reconnects them:

```text
Spring Core proxy/AOP
        ↓ applies
@Transactional metadata
        ↓ drives
PlatformTransactionManager
        ↓ coordinates
EntityManager + persistence context
        ↓ implemented by
Hibernate ORM
        ↓ executes through
JDBC Connection
        ↓ borrowed from
DataSource / HikariCP
        ↓ communicates through pgJDBC with
PostgreSQL
```

This is not a replacement for the JDBC mental model. It is that same model with Spring and JPA coordinating the repetitive mechanics.

## How to study

1. For every example, identify the **transaction boundary** and whether the call actually crosses a Spring proxy.
2. Predict these moments separately: Java mutation, dirty checking, flush, SQL execution, commit, and rollback.
3. Ask which JDBC connection PostgreSQL sees and how long HikariCP must keep it leased.
4. Predict the database result before opening each checkpoint answer.
5. Keep HTTP, Java method, persistence-context, and database-transaction lifetimes separate.
6. Use the companion exercise to verify claims with logs and PostgreSQL, not only annotations.

Code fragments isolate one idea unless identified as a complete type. They use Spring's `org.springframework.transaction.annotation.Transactional` and Jakarta Persistence's `jakarta.persistence.*` APIs.

## Big picture

```text
HTTP request
    ↓ validation, conversion, routing
BookController
    ↓ ordinary call to a Spring-managed bean reference
BookService proxy
    ↓ TransactionInterceptor reads transaction metadata
PlatformTransactionManager
    ↓ begins or joins a transaction and binds resources
BookService target method
    ↓ repository operations and managed entity changes
EntityManager / persistence context
    ↓ dirty checking and flush
Hibernate ORM
    ↓ INSERT / UPDATE / DELETE / SELECT
JDBC Connection
    ↓ leased from HikariCP
PostgreSQL transaction
    ├── COMMIT: make the whole database outcome durable
    └── ROLLBACK: discard its uncommitted database changes
```

Three boundaries are related but not identical:

```text
HTTP request boundary     controller receives and returns HTTP representations
service/use-case boundary decides what must succeed or fail together
database transaction      PostgreSQL coordinates SQL visibility and durability
```

With OSIV disabled in this project, required database work should finish in the service transaction. The controller maps already-available state after the service call; it should not trigger surprise lazy SQL.

## Concept priorities

| Priority | Meaning | Expected ability |
|---|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Daily correctness knowledge | Explain, implement, and diagnose it from memory |
| ⭐⭐⭐⭐ IMPORTANT | Common design/performance/concurrency concern | Apply it deliberately and recognize failures |
| ⭐⭐⭐ NICE TO KNOW | Useful for selected use cases | Know when to look it up and test it |
| ⭐⭐ FUTURE KNOWLEDGE | Advanced architecture or uncommon policy | Understand the boundary without adding it casually |

### Navigation by cluster

- Sections 1–3: transaction fundamentals, Spring architecture, and proxy behavior
- Sections 4–5: JPA unit of work and rollback rules
- Sections 6–7: propagation and `REQUIRED` versus `REQUIRES_NEW`
- Sections 8–9: isolation, concurrency, and locking
- Sections 10–12: timeouts, read-only work, and multi-repository atomicity
- Sections 13–15: external systems, after-commit work, and HikariCP
- Sections 16–18: mistakes, debugging, and interviews
- Section 19: complete mental model and compact review sheet

---

## 1. Transaction fundamentals

**⭐⭐⭐⭐⭐ MUST KNOW**

### 1.1 What a database transaction is

A database transaction is a group of database actions that PostgreSQL treats as one logical unit:

```text
BEGIN
    operation A
    operation B
    operation C
COMMIT
```

If the unit cannot complete safely:

```text
BEGIN
    operation A succeeds
    operation B fails
ROLLBACK
```

The rollback does not mean operation A never reached PostgreSQL. It means PostgreSQL does not make A's uncommitted effect part of the final durable state.

The raw JDBC model from the earlier guide was explicit:

```java
connection.setAutoCommit(false);
try {
    debit(connection, fromId, amount);
    credit(connection, toId, amount);
    connection.commit();
} catch (SQLException | RuntimeException failure) {
    connection.rollback();
    throw failure;
}
```

Spring preserves this fundamental model. Instead of repeating the boundary code, a transaction interceptor and transaction manager coordinate it.

### 1.2 ACID, without slogans

| Property | Practical meaning | What it does not mean |
|---|---|---|
| **Atomicity** | All database effects in the transaction commit, or none of its uncommitted effects do | It cannot unsend an email or undo another system's HTTP request |
| **Consistency** | Declared database rules hold before and after a successful transaction | The database does not automatically know every business rule; application logic and constraints still matter |
| **Isolation** | Concurrent transactions observe and affect one another according to an isolation policy | Every isolation level does not behave identically, and stronger isolation can produce waits or retryable failures |
| **Durability** | After a successful commit, PostgreSQL promises the result survives according to its durability configuration | A Java object mutation before commit is not durable |

“C” is often misunderstood. A transaction can atomically commit a logically wrong stock value if neither application logic nor a constraint rejects it. Transactions supply a correctness mechanism; they do not invent the invariant.

PostgreSQL identity/sequence numbers are another useful boundary: a transaction that obtains an ID and later rolls back can leave a gap. Sequence advancement is not promised to roll back. Primary keys need uniqueness, not gaplessness.

### 1.3 Transaction boundary

The transaction boundary answers:

> Which database work must have one success or failure outcome?

For the Book Catalog, changing two books as one stock transfer is one use case:

```text
source stock -= amount
destination stock += amount
```

It would be incorrect for the source change to commit while the destination change fails. Therefore the boundary surrounds both changes.

```java
@Transactional
public void transferStock(long fromId, long toId, int amount) {
    Book from = required(fromId);
    Book to = required(toId);

    from.decreaseStock(amount);
    to.increaseStock(amount);
}
```

The annotation is not there because the method performs “database stuff.” It is there because the method defines an atomic business operation.

### 1.4 Transaction lifecycle

A typical successful JPA transaction is:

```text
1. caller enters the Spring service proxy
2. transaction advice selects a transaction manager
3. transaction begins; transaction resources are bound
4. service target method runs
5. repositories load or change data
6. persistence context tracks managed entities
7. before completion, Hibernate flushes required SQL
8. PostgreSQL accepts all statements
9. transaction manager commits
10. persistence resources are cleaned up
11. JDBC connection is returned to HikariCP
12. caller receives the result
```

A typical failure path is:

```text
1–6. same beginning
7. target throws a failure that matches a rollback rule
8. transaction advice marks/requests rollback
9. PostgreSQL rolls back uncommitted work
10. resources are cleaned up
11. connection returns to HikariCP
12. exception continues to the caller
```

The connection may be acquired lazily rather than at the exact first instruction of the service method. That provider detail does not change the ownership model: SQL in the transaction is coordinated on the transaction-associated JDBC resource.

### 1.5 Why the service/use-case layer normally owns the boundary

The service knows the complete operation:

```text
Controller: HTTP input/output orchestration
Service:    business use case and atomic boundary
Repository: entity persistence/query operation
Database:   concurrent integrity and durable outcome
```

If each repository method owns only its own independent transaction, this is possible:

```text
repository operation A → commits
repository operation B → fails and rolls back
overall use case        → half complete
```

With an outer service transaction:

```text
@Transactional service use case
    ├── repository operation A joins
    └── repository operation B joins

B fails → one rollback covers A and B
```

The controller should normally not own this boundary. A controller knows HTTP details; putting persistence lifetime there couples routing/serialization to database resources, encourages lazy loading outside a deliberate fetch plan, and can hold connections while constructing a response.

### 1.6 Transaction versus one repository method

Spring Data's inherited repository methods commonly carry transaction metadata. That is useful when called alone, but it is not the architecture for a multi-step use case.

```java
// Two calls are not automatically one large transaction merely because
// both repository methods are transactional internally.
public void unsafeTransfer(...) {
    bookRepository.operationA(); // transaction may start and finish here
    bookRepository.operationB(); // another transaction may start here
}
```

An outer `@Transactional` service method changes the picture. With default `REQUIRED` propagation, both repository calls join the existing transaction.

### Checkpoint 1

An endpoint calls two repository write methods. The first returns normally; the second throws. Does the HTTP request itself guarantee that the first rolls back? Where should the intended all-or-nothing boundary be declared?

<details>
<summary>Answer</summary>

No. An HTTP request is not automatically one database transaction. Declare a service/use-case transaction around both operations. The default `REQUIRED` behavior lets participating repository calls join it.

</details>

---

## 2. Spring transaction architecture

**⭐⭐⭐⭐⭐ MUST KNOW**

### 2.1 Runtime path in this project

```text
BookController
    ↓ calls a container-injected service reference
Spring AOP service proxy
    ↓
TransactionInterceptor
    ↓ reads @Transactional metadata
PlatformTransactionManager
    ↓ typically JpaTransactionManager for this JPA application
EntityManager proxy
    ↓ delegates to a transaction-bound EntityManager
Persistence Context
    ↓ managed entities, snapshots, pending work
Hibernate ORM
    ↓ SQL generation, hydration, dirty checking, flush
JDBC / pgJDBC
    ↓ commands on a transaction-associated Connection
Boot-managed DataSource
    ↓
HikariCP
    ↓ logical handle backed by a PostgreSQL session
PostgreSQL
```

Boot configures the infrastructure because the JPA starter, `DataSource`, `EntityManagerFactory`, and transaction support are present. The annotation remains metadata; the runtime components give it behavior.

### 2.2 Responsibility of each layer

| Layer | Responsibility | Does not decide |
|---|---|---|
| Controller | Bind/validate HTTP input, call service, create HTTP response | Transaction propagation or Hibernate flush policy |
| Service proxy | Intercept eligible calls around the target | Book business rules |
| `TransactionInterceptor` | Read transaction attributes, obtain/join a transaction, invoke target, complete with commit/rollback | SQL text or connection-pool size |
| `PlatformTransactionManager` | Provide Spring's common begin/commit/rollback contract for a resource strategy | Entity mapping or HTTP status |
| `JpaTransactionManager` | Coordinate JPA `EntityManager` and its resource-local transaction; integrate access to the underlying `DataSource` | PostgreSQL's isolation implementation |
| `EntityManager` | JPA API over a persistence context | Physical pool lifecycle |
| Persistence context | Identity map, managed state, change tracking, pending unit of work | Durability before commit |
| Hibernate ORM | Implement JPA, map entities, dirty-check, flush SQL | Whether a business use case chose the right boundary |
| JDBC/pgJDBC | Execute database protocol operations through a `Connection` | Business meaning of a row change |
| `DataSource` | Standard abstraction for obtaining connections | Transaction sharing across arbitrary separate connections |
| HikariCP | Bound and reuse physical database connections | Make a slow transaction fast or correct |
| PostgreSQL | Execute SQL, enforce constraints/locks/isolation, commit or roll back | Undo non-database side effects |

### 2.3 From the Spring annotation back to JDBC

The earlier Spring Core guide established that a proxy adds cross-cutting behavior around a bean call. The JDBC and pool guides established that a local database transaction belongs to one connection. Join those facts:

```text
@Transactional call enters proxy
        ↓
transaction manager begins/joins logical transaction
        ↓
EntityManager participates
        ↓
Hibernate obtains transaction-associated JDBC access
        ↓
connection auto-commit is not used as one-commit-per-statement policy
        ↓
all participating SQL uses the coordinated database transaction
        ↓
transaction manager requests commit or rollback
```

You normally do not call `setAutoCommit(false)`, `commit()`, or `rollback()` yourself in a JPA service. That work still exists underneath; Spring, Hibernate, and the driver coordinate it.

Mixing manual commit calls into declarative transaction code breaks ownership. The transaction manager must remain the component that completes the resource it began.

### 2.4 Resource binding and current execution

Spring associates transaction resources with the current execution flow. In the usual imperative servlet application, this is thread-bound coordination:

```text
request thread
    ├── transaction status
    ├── EntityManager for this transaction
    └── JDBC resource associated as needed
```

This does not make an `EntityManager` thread-safe. Spring injects a proxy that finds the appropriate transaction-bound manager. Do not store managed entities or an actual `EntityManager` as mutable singleton state, and do not assume a transaction automatically moves to a manually created thread.

### 2.5 Commit completion path

The target method returning is not itself the commit:

```text
BookService.setStock target method returns Book
        ↓
control returns to TransactionInterceptor
        ↓
transaction manager begins completion
        ↓
Hibernate flushes dirty managed state if required
        ↓
UPDATE reaches PostgreSQL
        ↓
JDBC transaction commits
        ↓
interceptor returns Book to controller
```

An exception can therefore arise after the last line of the target method, during flush or commit. This explains why a `try/catch` around only `repo.save(...)` may be too early to translate a constraint violation.

### Checkpoint 2

Who makes `@Transactional` active? Who detects a changed managed `Book`? Who sends SQL? Who owns row locks and the durable commit? Who returns the connection to reusable pool capacity?

<details>
<summary>Answer</summary>

Spring's proxy and `TransactionInterceptor` interpret the metadata; the transaction manager coordinates the boundary; Hibernate detects managed changes and sends SQL through JDBC; PostgreSQL owns locks/isolation/commit; transaction cleanup releases the logical connection handle back to HikariCP.

</details>

---

## 3. `@Transactional` and proxy behavior

**⭐⭐⭐⭐⭐ MUST KNOW**

### 3.1 What the annotation declares

`@Transactional` describes transaction semantics:

```java
@Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.DEFAULT,
        timeout = 10,
        readOnly = false,
        rollbackFor = SomeCheckedException.class)
public void operation() {
    // business work
}
```

It does not itself open a connection. It is metadata discovered by Spring transaction infrastructure.

The ordinary defaults are:

| Attribute | Default |
|---|---|
| Propagation | `REQUIRED` |
| Isolation | `DEFAULT` |
| Read-only | `false` |
| Timeout | Underlying/default timeout, often none |
| Rollback | `RuntimeException` and `Error`; not checked exceptions by default |

The Book Catalog has not globally changed Spring's default rollback policy. Spring 7 can opt into checked-exception rollback globally with `@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)`, but this project has not done so. Never silently assume that another project uses either policy; inspect its configuration.

### 3.2 Method-level declaration

Use method-level metadata when only selected use cases differ:

```java
@Service
public class InventoryService {

    @Transactional
    public void transferStock(...) {
        // ...
    }
}
```

The method should express a coherent use case, not be annotated merely because one line calls a repository.

### 3.3 Class-level declaration and method override

The current Book Catalog uses the useful read-mostly pattern:

```java
@Service
@Transactional(readOnly = true)
public class BookService {

    public Book findById(long id) {
        // inherits readOnly = true
    }

    public List<Book> findAll(String author, int page, int size) {
        // inherits readOnly = true
    }

    @Transactional
    public Book create(...) {
        // method-level annotation overrides class-level readOnly setting
    }

    @Transactional
    public Book setStock(...) {
        // ordinary read-write transaction
    }
}
```

The most specific applicable transaction metadata wins. A method annotation overrides the class declaration for that method. Plain `@Transactional` sets `readOnly` back to its default `false`; it does not merge `true` from the class.

### 3.4 Proxy interception

The important call shape is:

```text
Controller
    ↓ calls Spring-managed BookService reference
BookService proxy
    ↓ transaction advice begins/joins
BookService target object
    ↓ actual method body
BookService proxy
    ↓ commit/rollback completion
Controller
```

Only an eligible invocation that passes through the proxy receives proxy advice.

The safest application design rule is:

> Put transaction boundaries on public service methods called from another Spring bean.

Why “safest”? Interface-based proxies expose public interface methods. Modern class-based Spring proxies can support some non-public overridable methods, but private methods cannot be overridden/intercepted, and proxy style can change. Public use-case methods make the boundary visible, portable, and testable.

Also avoid relying on transaction advice for:

- a service created manually with `new` instead of obtained from the container;
- a private method;
- a final method or final class when class-based proxying would need overriding;
- a call made during construction before the proxy can be used;
- work launched on an unrelated executor thread without explicit transaction design.

### 3.5 The self-invocation problem

```java
@Service
public class BookService {

    public void outer(long id) {
        inner(id); // equivalent to this.inner(id)
    }

    @Transactional
    public void inner(long id) {
        // expected database work
    }
}
```

The call path is:

```text
external caller → proxy → target.outer()
                          ↓ direct call on same target
                        target.inner()
```

It is not:

```text
external caller → proxy.outer() → proxy.inner()
```

Therefore the `inner` annotation does not independently trigger transaction interception. If `outer` has no transaction, `inner` does not gain one merely because its source code carries the annotation. If `outer` already has a transaction, `inner` runs within that existing context because the thread already has one—not because its annotation was re-evaluated.

This distinction is crucial for `REQUIRES_NEW`, custom isolation, or rollback rules. A self-call cannot activate those separate semantics.

### 3.6 Practical solutions

Choose the simplest boundary that matches the use case:

**Solution A — put the transaction on the outer use case**

```java
@Transactional
public void outer(long id) {
    innerHelper(id);
}

private void innerHelper(long id) {
    // part of the same use case
}
```

Use this when the helper should always be part of the outer transaction. It is usually the best answer.

**Solution B — move the independent operation to another bean**

```java
@Service
public class AuditService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(...) {
        // independently intercepted through this bean's proxy
    }
}
```

Inject `AuditService` into the business service. Use this only when the operation genuinely has an independent transaction contract.

**Solution C — use `TransactionTemplate` for a genuinely programmatic boundary**

This can be appropriate when the boundary is dynamic or internal and proxy decomposition would be artificial. It is lower-level and should be uncommon in ordinary CRUD services.

Avoid self-injection, looking up your own proxy, or enabling `AopContext.currentProxy()` as a first response. Those techniques couple business code to proxy mechanics and obscure the use case.

### 3.7 Annotation placement is API design

Transaction attributes are part of a service method's behavioral contract:

- Does it require an existing transaction?
- Can it commit independently?
- Can it run without a transaction?
- Which failures make the work invalid?
- Which concurrency view does it require?

Keep that contract on concrete service methods/classes where readers can see it. Avoid scattering contradictory propagation and isolation settings through low-level helpers.

### Checkpoint 3

`outer()` is called through a Spring bean reference and calls `this.inner()`. Only `inner()` declares `REQUIRES_NEW`. Does Spring suspend the outer transaction and create a new one?

<details>
<summary>Answer</summary>

No. The self-call bypasses proxy interception, so Spring does not evaluate `inner()`'s `REQUIRES_NEW` declaration. Move the independent operation to another bean, use a programmatic transaction deliberately, or move the appropriate boundary to `outer()`.

</details>

---

## 4. JPA, Hibernate, and transactions

**⭐⭐⭐⭐⭐ MUST KNOW**

### 4.1 The persistence context is a transactional unit-of-work partner

Inside a typical Spring JPA transaction:

```text
EntityManager
    ↓ operates on
Persistence Context
    ├── Book id=1 → one managed Java instance
    ├── Book id=2 → one managed Java instance
    ├── loaded-state snapshots/change-tracking information
    ├── pending insert/update/delete work
    └── proxies and persistent collections
```

The persistence context is not the database transaction, but they normally cooperate:

```text
persistence context = in-memory managed unit of work
database transaction = PostgreSQL's atomic/isolation/durability boundary
```

Hibernate flush connects them by translating pending managed state into SQL.

### 4.2 Managed update sequence

The current `setStock` pattern is intentionally simple:

```java
@Transactional
public Book setStock(long id, Integer stock) {
    Book book = repo.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));

    book.setStock(stock);
    return book;
}
```

Sequence:

```text
transaction starts
    ↓
repo.findById(id)
    ↓ SELECT executes if needed
Book becomes MANAGED in this persistence context
    ↓
book.setStock(stock)
    ↓ Java field changes; no commit has happened
target method returns
    ↓
Hibernate dirty checking during flush
    ↓
UPDATE books SET ... WHERE id = ?
    ↓ SQL executed inside open transaction
transaction manager commits
    ↓
row becomes durable; context later closes and Book becomes detached
```

No `repo.save(book)` is needed merely to tell Hibernate that this already-managed entity changed.

### 4.3 Keep the moments separate

```text
Java object mutation
        ≠
dirty checking
        ≠
flush
        ≠
SQL execution
        ≠
commit
```

| Moment | What happened | Can database rollback still undo durable outcome? |
|---|---|---|
| `book.setStock(3)` | Java state changed | Yes; SQL may not even exist yet |
| Dirty check | Hibernate notices mapped managed state differs | Yes |
| Flush | Hibernate synchronizes required changes as SQL | Yes; transaction remains active |
| SQL `UPDATE` succeeds | PostgreSQL changed transaction-visible row version and may hold locks | Yes |
| Commit succeeds | Database transaction completes successfully | No ordinary rollback of that completed transaction |

The Java object is another concern: rollback does not automatically reverse its field back to the earlier value. The database discards uncommitted changes, while the in-memory object may still contain the mutation. After a persistence failure, let the transaction end and discard that persistence context rather than continuing to treat its entities as authoritative.

### 4.4 Dirty checking

Hibernate's persistence context works like a transactional write-behind unit:

```text
load managed state
    ↓ retain tracking information
mutate mapped attributes
    ↓
flush point
    ↓ compare/detect dirty state
generate UPDATE
```

Dirty checking requires the instance to be managed by the active context. It does not persist arbitrary Java objects.

It will not behave as expected when:

- the entity is detached;
- the changed field is not mapped;
- no suitable transaction/persistence context flushes the change;
- a read-only optimization suppresses normal dirty processing;
- bulk JPQL/native SQL changes the row behind an already-managed instance;
- code modified a separate DTO or a newly constructed entity instead of managed state.

### 4.5 What `save()` means—and does not mean

Spring Data JPA commonly implements `save` as:

```text
is entity new?
    ├── yes → EntityManager.persist(entity)
    └── no  → EntityManager.merge(entity)
```

Therefore:

```text
save(entity) ≠ commit now
save(entity) ≠ always issue SQL immediately
save(managedEntity) ≠ required after every mutation
```

`merge` copies state to a managed instance and returns that instance. The supplied detached object does not simply become managed. Use the returned value when merge semantics may apply.

For a new `Book` with identity generation, Hibernate may have to execute an insert early to obtain the generated ID. That SQL timing still does not make the transaction committed.

### 4.6 Flush is not commit

```text
managed Book changes
    ↓
repo.flush() / EntityManager.flush() / automatic flush
    ↓
UPDATE reaches PostgreSQL
    ↓
transaction remains open
    ├── later success → COMMIT
    └── later failure → ROLLBACK, including that UPDATE
```

Flush may occur:

- before transaction commit;
- explicitly through `EntityManager.flush()` or `JpaRepository.flush()`;
- through `saveAndFlush()`;
- before a query when `AUTO` flush mode determines pending changes may affect query correctness;
- at other provider-required points, such as some identifier-generation cases.

Do not teach “every query always flushes.” In normal `AUTO` mode, Hibernate decides whether synchronization is required. Do not teach “SQL only happens at commit” either; explicit flush and several automatic cases execute it earlier.

### 4.7 Why explicit flush sometimes exists

The checked-in Book Catalog's create/replace path explicitly flushes so a database constraint error occurs while the service's `try/catch` is active. That timing goal is sound. A production-grade translation should then classify only the known ISBN unique violation; the current source's broad conversion of every `DataIntegrityViolationException` must not be treated as a general pattern:

```java
@Transactional
public Book replace(...) {
    Book book = required(id);
    book.replace(...);

    try {
        repo.flush();
        return book;
    } catch (DataIntegrityViolationException failure) {
        throw translateKnownConstraint(failure);
    }
}
```

Without the flush, the target method could return and the transaction interceptor could encounter the constraint violation during completion—outside that `try` block.

Explicit flush is a timing tool, not a durability tool. Use it for a concrete reason such as early constraint detection or a query that must see pending changes. Flushing after every repository call reduces batching opportunities and complicates reasoning.

After a flush failure, do not continue normal work in the same transaction. The database/provider transaction may be marked rollback-only, and the persistence context can no longer be trusted as synchronized.

### 4.8 Rollback after flush: controlled timeline

```java
@Transactional
public void demonstrateFlushThenRollback(long id) {
    Book book = required(id);
    book.setStock(999);

    repo.flush(); // UPDATE executes

    throw new IllegalStateException("Intentional failure after flush");
}
```

```text
T1  transaction begins
T2  SELECT loads Book
T3  Java stock becomes 999
T4  flush sends UPDATE; transaction now sees stock=999
T5  RuntimeException escapes
T6  transaction rolls back
T7  a fresh transaction reads the original database stock
```

The SQL log can contain the `UPDATE` even though PostgreSQL ultimately contains no committed change. SQL visibility is not commit evidence.

### 4.9 Detached entities and transaction completion

An entity loaded inside a transaction is managed only by that persistence context:

```text
inside service transaction: Book is managed
service transaction/context ends
outside that context:       Book is detached
```

Changing a detached `Book` does not trigger dirty checking in the old context:

```java
Book detached = service.findById(1L);
detached.setStock(4); // ordinary Java change; no automatic database update
```

Do not stretch transactions merely to keep arbitrary entities managed. Design service methods that perform the complete use case and return DTO-ready state.

### 4.10 Lazy loading and the boundary

This project uses:

```yaml
spring:
  jpa:
    open-in-view: false
```

That makes the intended boundary visible:

```text
service transaction
    ├── load required scalar/association state
    ├── perform business work
    └── map or return safely available data
transaction/context ends
controller builds HTTP response without surprise SQL
```

A lazy association accessed after the context closes can produce `LazyInitializationException`. The solution is a deliberate fetch plan or mapping inside the use case—not `@Transactional` on the controller.

OSIV, when enabled, can keep a persistence context available beyond the service transaction. It does not mean the database transaction remains open for the whole request. Do not confuse persistence-context lifetime with transaction lifetime.

### Checkpoint 4

A managed `Book` is changed, explicitly flushed, and then a runtime exception is thrown. The log shows `UPDATE`. Is the new value durable? Does the Java object's field necessarily revert?

<details>
<summary>Answer</summary>

No. Flush executed SQL inside the still-open transaction; rollback discards the uncommitted database change. The Java object's field is not automatically rewound, so discard the failed persistence context/entity state rather than treating it as fresh database truth.

</details>

---

## 5. Rollback behavior

**⭐⭐⭐⭐⭐ MUST KNOW**

### 5.1 Default rule

With the Book Catalog's ordinary Spring defaults:

| What escapes the transactional proxy | Default outcome |
|---|---|
| `RuntimeException` | Roll back |
| `Error` | Roll back |
| Checked `Exception` | Commit if completion otherwise succeeds |
| Normal return | Commit if not marked rollback-only and flush/commit succeed |

This is a Spring transaction rule, not a Java language rule and not a PostgreSQL rule.

```java
@Transactional
public void runtimeFailure(long id) {
    required(id).setStock(0);
    throw new IllegalStateException("rollback by default");
}
```

The `IllegalStateException` crosses the interceptor, so the default rule selects rollback.

### 5.2 Checked exceptions

```java
public final class InventoryImportException extends Exception {
    public InventoryImportException(String message) {
        super(message);
    }
}
```

```java
@Transactional
public void checkedFailure(long id) throws InventoryImportException {
    required(id).setStock(0);
    throw new InventoryImportException("checked failure");
}
```

Under default rules, this checked exception does not request rollback. If flush and commit can succeed, the stock change can commit even though the caller receives an exception.

When the checked exception means the unit of work failed, declare that intent narrowly:

```java
@Transactional(rollbackFor = InventoryImportException.class)
public void importStock(...) throws InventoryImportException {
    // ...
}
```

Prefer an exception class literal over a name pattern. Pattern rules can match more broadly than intended.

Do not reflexively add `rollbackFor = Exception.class` everywhere. First decide which checked failures are expected in the use case and whether each invalidates its database work. Broad rules obscure that design and can roll back on control-flow exceptions that were intended to report a committed outcome.

### 5.3 `noRollbackFor`

```java
@Transactional(noRollbackFor = NotificationPreviewException.class)
public void updateAndPreview(...) {
    // ...
}
```

This asks Spring to commit even if that exception escapes. It is uncommon and must express a real business contract. Never use `noRollbackFor` to continue after a database integrity, connection, or Hibernate failure; the resource may already be rollback-only or unusable.

### 5.4 Catching an exception can prevent expected rollback

```java
@Transactional
public Book unsafe(long id) {
    Book book = required(id);
    book.setStock(0);

    try {
        performBusinessStep();
    } catch (IllegalStateException failure) {
        log.warn("ignored", failure);
        return book;
    }
}
```

If no participating component has already marked the transaction rollback-only, the transaction interceptor sees a normal return and attempts commit. Catching is not neutral; it changes what crosses the boundary.

Better choices are:

- let the failure propagate;
- translate it and rethrow while retaining the cause;
- catch and perform a genuine recovery that makes commit correct;
- in rare low-level code, mark rollback-only programmatically—but prefer declarative rules.

```java
catch (LowLevelInventoryException failure) {
    throw new StockAdjustmentException("Adjustment failed", failure);
}
```

If `StockAdjustmentException` is a runtime exception, the proxy sees it and rolls back by default.

### 5.5 Rollback-only state

A participating scope can decide that the shared transaction must not commit:

```text
outer REQUIRED transaction starts
    ↓
inner REQUIRED participant joins same physical transaction
    ↓ inner runtime failure crosses inner proxy
shared transaction marked rollback-only
    ↓
outer catches the exception and returns normally
    ↓
outer interceptor attempts commit
    ↓
rollback-only flag forces rollback
```

The outer method cannot erase that decision merely by catching the exception.

### 5.6 Why `UnexpectedRollbackException` exists

Consider two separate Spring beans so the inner call crosses a proxy:

```java
@Service
public class InnerStockService {

    @Transactional // REQUIRED
    public void change(long id) {
        // mutate, then fail
        throw new IllegalStateException("inner failure");
    }
}
```

```java
@Transactional
public void outer(long id) {
    try {
        innerStockService.change(id);
    } catch (IllegalStateException failure) {
        // dangerous assumption: "we handled it, so outer can commit"
    }

    // more work
}
```

The inner interceptor marks the shared transaction rollback-only. When the outer interceptor reaches what looks like commit, it rolls back and normally throws `UnexpectedRollbackException` so the caller is not falsely told that a commit occurred.

```text
inner failure caught by application
        ≠
rollback-only decision cleared
```

The exception is “unexpected” from the outer caller's perspective, not from Spring's internal state.

### 5.7 Catch location matters

Compare these cases:

**The transaction interceptor sees the failure**

```text
outer proxy → inner proxy → target throws RuntimeException
                       inner interceptor marks rollback-only
```

**Application catches before any transaction interceptor sees it**

```text
one target method
    try { ordinary helper throws RuntimeException; }
    catch (...) { recover and return; }

outer interceptor sees normal return
```

In the second case, Spring's exception rule alone has no escaping exception to inspect. However, a persistence provider or database failure may independently mark the transaction rollback-only. Never assume “caught” means “safe to continue.”

### 5.8 Persistence failure after catch

```java
@Transactional
public void wrong() {
    try {
        repo.flush();
    } catch (DataIntegrityViolationException failure) {
        // do not continue with more JPA work and try to commit
    }
}
```

After failed SQL/flush, PostgreSQL often treats the current transaction as aborted until rollback, and Hibernate considers the persistence context unreliable. The safe pattern is to translate/rethrow and let the transaction end.

### 5.9 Programmatic rollback—recognize, do not default to it

**⭐⭐⭐ NICE TO KNOW**

Spring exposes:

```java
TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
```

This can mark the current transaction rollback-only after a caught condition. It couples business code to Spring transaction infrastructure and can lead to a later `UnexpectedRollbackException`. Prefer an exception and declarative rollback rule when possible.

### Checkpoint 5

An inner `REQUIRED` bean method throws a runtime exception. The outer `REQUIRED` method catches it and returns a success value. What should the external caller expect?

<details>
<summary>Answer</summary>

The inner scope normally marked the shared transaction rollback-only. Completion rolls it back, and Spring normally raises `UnexpectedRollbackException` so the caller does not mistake a rollback for a commit.

</details>

---

## 6. Transaction propagation

**⭐⭐⭐⭐⭐ MUST KNOW for `REQUIRED`; ⭐⭐⭐⭐ IMPORTANT for `REQUIRES_NEW`; lower priority for the rest**

Propagation answers:

> What should this method do when the caller already has—or does not have—a transaction?

It does not describe data visibility; that is isolation. It does not describe which exceptions roll back; those are rollback rules.

Propagation metadata is applied only when a call crosses transaction advice. A self-invocation does not activate a different propagation mode.

### 6.1 Summary table

| Propagation | Existing transaction | No existing transaction | Typical priority |
|---|---|---|---|
| `REQUIRED` | Join it | Create one | ⭐⭐⭐⭐⭐ default and daily use |
| `REQUIRES_NEW` | Suspend it; create an independent one | Create one | ⭐⭐⭐⭐ deliberate special case |
| `SUPPORTS` | Join it | Run without an actual transaction | ⭐⭐⭐ recognize |
| `MANDATORY` | Join it | Throw an exception | ⭐⭐⭐ useful invariant in selected designs |
| `NOT_SUPPORTED` | Suspend it | Run without a transaction | ⭐⭐ uncommon |
| `NEVER` | Throw an exception | Run without a transaction | ⭐⭐ uncommon guardrail |
| `NESTED` | Create a savepoint in the same physical transaction when supported | Start a transaction like `REQUIRED` | ⭐⭐ manager/provider-specific |

“Create” means create a new physical transaction for this resource. “Join” means the method participates in the existing physical transaction, though Spring can track a logical scope for each advised method.

### 6.2 `REQUIRED` — the default

```java
@Transactional // Propagation.REQUIRED is implicit
public void transferStock(...) {
    sourceOperation();
    destinationOperation();
}
```

```text
caller has no transaction
    ↓
REQUIRED starts transaction T1

caller already has T1
    ↓
REQUIRED joins T1
```

Realistic use cases:

- create/update/delete one aggregate;
- load, validate current state, and mutate it;
- coordinate multiple repositories atomically;
- make several helper services part of the same business operation.

Advantages:

- matches the usual “one use case, one transaction” model;
- lets lower-level participating calls compose naturally;
- minimizes extra connections and independent partial commits.

Risks:

- an inner participant can mark the shared transaction rollback-only;
- a very broad outer boundary makes every participant part of a long transaction;
- inner isolation/read-only/timeout declarations do not create a new transaction and therefore cannot simply replace characteristics of the existing one.

This is the propagation mode a beginner must understand deeply. Most application services need no explicit `propagation` attribute because `REQUIRED` is already the default.

### 6.3 Logical scopes inside one physical `REQUIRED` transaction

```text
outer @Transactional(REQUIRED) logical scope
    ↓ creates physical PostgreSQL transaction T1
    ├── repository call joins T1
    └── inner bean @Transactional(REQUIRED) logical scope joins T1
```

The inner scope can influence the shared outcome by marking it rollback-only. It cannot independently commit its portion. Only T1 commits or rolls back.

If strict validation of incompatible inner declarations matters, Spring transaction-manager configuration can reject mismatched isolation/read-only characteristics instead of leniently participating. Do not design code that depends on a lower-level `REQUIRED` method silently upgrading an already-started transaction.

### 6.4 `REQUIRES_NEW`

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordAudit(...) {
    auditRepository.save(...);
}
```

```text
no current transaction
    ↓
start T2

current transaction T1 exists
    ↓
suspend T1's association with the execution
    ↓
start independent T2
    ↓
commit or roll back T2
    ↓
resume T1
```

The two transactions have independent outcomes. T2 committing does not force T1 to commit. T1 rolling back does not undo already committed T2.

Realistic but selective uses:

- record a minimal audit/failure fact that must survive rollback of the main operation;
- reserve a small independent database fact with consciously accepted partial-commit semantics;
- isolate a tiny operation whose failure policy is deliberately separate.

Disadvantages:

- can persist misleading audit data if it claims the outer operation succeeded before it actually did;
- requires another connection while an outer transaction may still retain its connection;
- increases transaction complexity and pool pressure;
- creates partial commits that can violate an invariant if used for ordinary business updates;
- an inner exception still propagates unless caught, so it can cause the resumed outer transaction to roll back even though the transactions are physically independent.

Use it because the outcome must be independent, not as a generic way to “fix rollback.”

### 6.5 `SUPPORTS`

```java
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public Book findMaybeTransactional(long id) {
    return required(id);
}
```

```text
existing T1 → participate in T1
no T1       → execute without an actual transaction
```

Possible use: a read helper that can benefit from a caller's transaction but does not require one.

Tradeoffs:

- behavior changes with the caller;
- lazy loading, repeatable reads, locks, and persistence-context scope can differ;
- without a transaction, each data-access operation may have its own resource behavior.

Beginners usually should prefer an explicit read-only `REQUIRED` service boundary. Predictability is more valuable than avoiding a short read transaction by default.

### 6.6 `MANDATORY`

```java
@Transactional(propagation = Propagation.MANDATORY)
public void applyPartOfTransfer(...) {
    // must be called inside an existing transaction
}
```

```text
existing T1 → join T1
no T1       → IllegalTransactionStateException
```

Possible use: enforce that a low-level operation must never commit independently because it is meaningful only as part of a larger unit.

Advantage: fails fast when the composition contract is violated.

Disadvantage: tightly couples the method to transactional callers and can make reuse/testing less direct.

It is useful in selected designs, but most beginners can express the boundary cleanly with a public `REQUIRED` use case and non-transactional private helpers.

### 6.7 `NOT_SUPPORTED`

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void runWithoutTransaction() {
    // ...
}
```

```text
existing T1 → suspend T1; execute nontransactionally; resume T1
no T1       → execute nontransactionally
```

Possible use: a deliberately nontransactional operation that must not inherit a caller's transaction.

Risks:

- database writes may commit individually according to resource/auto-commit behavior;
- it can split an expected atomic operation;
- suspension still adds complexity;
- it is not a way to make a long external call safe while retaining an outer transaction—the outer transaction can remain suspended with resources held.

Beginners rarely need it.

### 6.8 `NEVER`

```java
@Transactional(propagation = Propagation.NEVER)
public void assertNontransactional() {
    // ...
}
```

```text
existing T1 → throw IllegalTransactionStateException
no T1       → execute nontransactionally
```

This is a guardrail, not a performance annotation. It can assert that a method must not be invoked under a transaction. It is uncommon in ordinary service code.

Possible use: protect infrastructure code whose contract is explicitly nontransactional. Its advantage is immediate detection of an accidental transactional caller. Its disadvantage is poor composability: a harmless future caller with a transaction now fails, and the method still gains no atomicity when run alone. Beginners normally do not need it.

### 6.9 `NESTED`

```text
outer physical transaction T1
    ↓ create savepoint S1
inner nested work
    ├── success → keep changes; outer later decides T1
    └── failure → roll back to S1; T1 may continue
```

`NESTED` is not a second independent transaction. It normally uses a JDBC savepoint in the same physical transaction and same connection.

If no transaction exists, it behaves like `REQUIRED` and starts one.

Possible use: a JDBC-oriented batch where one sub-operation may roll back to a savepoint while the larger transaction continues.

Important cautions for JPA:

- nested transactions require transaction-manager and resource support;
- JPA does not provide a portable nested-transaction model;
- a savepoint can rewind database changes without automatically rewinding Hibernate's in-memory persistence-context state;
- locks and resource state around savepoints are database-specific;
- `JpaTransactionManager` should not be assumed to make arbitrary JPA entity work safely nested.

For this Book Catalog, do not introduce `NESTED` just to demonstrate annotation variety. Prefer one clear `REQUIRED` unit, or a consciously independent `REQUIRES_NEW` unit where that semantics is truly required.

### 6.10 Decision guide

```text
Should this work be part of the caller's atomic business unit?
    ├── yes → REQUIRED
    └── no
         ↓
Must it commit/roll back independently even when a caller has a transaction?
    ├── yes → consider REQUIRES_NEW, including pool and consistency costs
    └── no
         ↓
Is a strict existing/non-existing transaction invariant required?
    ├── must exist     → MANDATORY
    ├── must not exist → NEVER
    └── otherwise      → usually keep design simple; do not reach for exotic modes
```

### Checkpoint 6

An outer service changes Book A. An inner `REQUIRED` service changes Book B. The inner call returns normally, then the outer fails. Which changes commit?

<details>
<summary>Answer</summary>

Neither. Both participated in the same physical transaction, so the outer rollback discards both uncommitted changes.

</details>

---

## 7. `REQUIRED` versus `REQUIRES_NEW`

**⭐⭐⭐⭐⭐ MUST KNOW comparison**

### 7.1 Same outcome versus independent outcome

Suppose the main operation reduces stock and then fails. A minimal audit record is intended to preserve the failure fact.

With `REQUIRED`:

```text
outer main transaction T1
    ├── change Book stock
    └── audit REQUIRED joins T1
             ↓
outer fails
             ↓
ROLLBACK T1: stock change and audit row both disappear
```

With a separately proxied `REQUIRES_NEW` audit service:

```text
outer main transaction T1
    ├── change Book stock
    ├── call audit proxy
    │      ↓ suspend T1
    │      ↓ start T2
    │      ↓ INSERT audit
    │      ↓ COMMIT T2
    │      ↓ resume T1
    └── fail
           ↓ ROLLBACK T1

final state:
    stock change absent
    audit row present
```

That final state is correct only if the audit row says something true, such as “attempt failed.” An audit row saying “stock transfer completed” would be false.

### 7.2 Comparison table

| Concern | `REQUIRED` inner method | `REQUIRES_NEW` inner method |
|---|---|---|
| Existing outer transaction | Joins it | Suspends it |
| Physical database transaction | Same one | Independent one |
| Commit outcome | One shared outcome | Inner can commit before outer |
| Rollback-only | Inner can mark shared transaction | Inner rollback does not directly mark outer transaction |
| JDBC connection | Normally same transaction-associated connection | Usually needs another connection while outer resource remains retained |
| Isolation/timeout | Existing transaction's characteristics govern | New transaction can have its own characteristics |
| Locks | Held as part of shared transaction | Each transaction owns its locks; inner may contend with outer or others |
| Main use | Compose one atomic use case | Deliberately independent small outcome |
| Beginner default | Yes | No |

### 7.3 Suspension does not mean “return the outer connection to the pool”

Conceptually Spring suspends T1's association while T2 runs. The outer transaction and its resources still exist:

```text
thread enters outer T1
    ↓ Hikari connection C1 belongs to T1
call REQUIRES_NEW
    ↓ T1 suspended; C1 usually remains retained
    ↓ request Hikari connection C2 for T2
    ↓ T2 completes; C2 returns
resume T1 with C1
```

With this project's pool maximum of 5:

```text
five concurrent outer transactions each hold C1..C5
        ↓
each reaches REQUIRES_NEW and asks for another connection
        ↓
no idle connection exists
        ↓
all can wait for a connection that none can release until inner work finishes
        ↓
acquisition timeout / severe pool starvation
```

The exact outcome depends on timing and whether the outer transaction has acquired a physical connection, but the risk is real. The official Spring guidance explicitly warns that `REQUIRES_NEW` can exhaust a pool unless it is sized beyond concurrent outer usage.

Do not mechanically “fix” this by making the pool huge. First ask:

- Is independent commit truly required?
- Can the audit be written after the outer transaction ends?
- Is a transactional outbox the more reliable model?
- Is the outer transaction holding a connection unnecessarily long?
- What concurrency and PostgreSQL connection limits were measured?

### 7.4 Independent does not mean isolated from all consequences

An inner T2 can contend with T1:

- T1 may hold a row lock that T2 needs;
- T2 may commit a row that changes assumptions T1 later uses;
- an inner exception can propagate into outer code;
- the outer method may choose to rethrow and roll back after T2 fails;
- deadlocks remain possible when transactions lock resources in inconsistent orders.

`REQUIRES_NEW` is not a magical logging channel. It is a real concurrent database transaction.

### 7.5 Correct proxy structure

This does **not** activate `REQUIRES_NEW`:

```java
@Transactional
public void mainOperation() {
    recordAudit(); // self-invocation
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordAudit() {
    // annotation bypassed by self-call
}
```

A simple correct structure is:

```java
@Service
public class AuditService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(long bookId, String reason) {
        auditRepository.save(new TransactionAudit(bookId, reason));
    }
}
```

```java
@Service
public class StockService {
    private final AuditService auditService;

    // constructor omitted here only to focus on the call shape

    @Transactional
    public void changeStock(long id) {
        // main work in T1
        auditService.recordFailure(id, "attempt observed"); // proxy call creates T2
        // if T1 later fails, T2 remains committed
    }
}
```

Even here, decide whether “attempt observed” is the correct durable fact and whether failure before/after the audit changes its meaning.

### Checkpoint 7

Why can adding one `REQUIRES_NEW` call make a Hikari pool of five connections fail under five concurrent requests, even though each request appears to perform database work sequentially?

<details>
<summary>Answer</summary>

Each outer transaction can retain one connection while suspended, then its inner transaction requests another. Five outers can occupy all five connections, leaving every inner call waiting. Sequential source-code order does not mean one connection is released before the independent transaction starts.

</details>

---

## 8. Isolation levels and PostgreSQL

**⭐⭐⭐⭐⭐ MUST KNOW for `DEFAULT`/`READ_COMMITTED`; ⭐⭐⭐⭐ IMPORTANT for the stronger levels**

Isolation answers:

> What can one transaction observe while other transactions are running, and which concurrent histories are allowed?

Spring declares a requested isolation level. PostgreSQL implements the actual visibility, locking, and failure behavior. Never infer complete behavior from the enum name alone.

### 8.1 The standard anomalies

**Dirty read**

```text
Transaction A                    Transaction B
-------------                    -------------
UPDATE stock = 5
(not committed)
                                 reads stock = 5  ← dirty value
ROLLBACK
                                 value it used never committed
```

PostgreSQL does not allow dirty reads, even when `READ_UNCOMMITTED` is requested.

**Non-repeatable read**

```text
Transaction A                    Transaction B
-------------                    -------------
read stock = 10
                                 UPDATE stock = 5
                                 COMMIT
read same row again = 5
```

The same row returned a different committed value within A.

**Phantom read**

```text
Transaction A                    Transaction B
-------------                    -------------
count books WHERE stock > 0 = 10
                                 INSERT matching book
                                 COMMIT
same count = 11
```

The query predicate gained a matching row.

**Lost update**

```text
Transaction A                    Transaction B
-------------                    -------------
read stock = 10                  read stock = 10
calculate 9                      calculate 8
UPDATE stock = 9; COMMIT
                                 UPDATE stock = 8; COMMIT

final stock = 8; A's decision is overwritten
```

Lost update is a practical read-modify-write race. It does not fit perfectly into every simplified SQL isolation table, and behavior depends on statement shape, database, locking, and version checks.

### 8.2 Spring isolation choices

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void operation() {
    // ...
}
```

| Spring value | General intent | PostgreSQL 17 behavior |
|---|---|---|
| `DEFAULT` | Use underlying resource default | Normally PostgreSQL `READ COMMITTED` unless configured otherwise |
| `READ_UNCOMMITTED` | Weakest standard request | Treated exactly like `READ COMMITTED` |
| `READ_COMMITTED` | See data committed before each statement begins | PostgreSQL default; two SELECTs can see different committed snapshots |
| `REPEATABLE_READ` | Stable transaction snapshot | PostgreSQL prevents dirty, non-repeatable, and phantom reads; can abort conflicting updates; serialization anomalies are still theoretically possible |
| `SERIALIZABLE` | Successful transactions behave like a serial order | PostgreSQL SSI detects dangerous dependency patterns and aborts a transaction with a serialization failure when needed |

This table is PostgreSQL-specific. Another database may implement the names differently.

### 8.3 `Isolation.DEFAULT`

```java
@Transactional // isolation = Isolation.DEFAULT
public void useCase() {
    // ...
}
```

`DEFAULT` delegates to the configured database/connection default. In this PostgreSQL project, that is normally `READ COMMITTED`.

Advantages:

- respects the database's normal policy;
- avoids unnecessary per-method isolation declarations;
- is appropriate for most ordinary CRUD work when combined with constraints and explicit concurrency controls where needed.

Do not translate `DEFAULT` as “no isolation.” Every PostgreSQL transaction runs at an isolation level.

### 8.4 `READ_UNCOMMITTED`

The SQL standard permits the weakest behavior here, but PostgreSQL's MVCC implementation maps it to `READ COMMITTED`.

```text
Spring requests READ_UNCOMMITTED
        ↓
pgJDBC/PostgreSQL accept the level name
        ↓
PostgreSQL provides READ COMMITTED behavior
```

Therefore a PostgreSQL experiment will not demonstrate a dirty read. Do not write a cross-database lesson that promises it will.

Beginner usage: normally none. Use the normal database default unless a measured use case requires a different explicit policy.

### 8.5 `READ_COMMITTED`

At PostgreSQL `READ COMMITTED`, each command gets a snapshot of rows committed before that command began (plus the transaction's own prior effects).

```text
Transaction A: BEGIN
Transaction A: SELECT stock → 10   [snapshot for statement 1]

Transaction B: UPDATE stock = 5
Transaction B: COMMIT

Transaction A: SELECT stock → 5    [new snapshot for statement 2]
Transaction A: COMMIT
```

Consequences:

- no dirty reads;
- non-repeatable reads can occur;
- phantoms can occur;
- concurrent updates wait as required for row versions/locks, then PostgreSQL evaluates according to its `READ COMMITTED` rules;
- an application-level read-modify-write can still overwrite a newer decision without `@Version`, a lock, or an atomic SQL update.

It is a strong practical default, not a guarantee that all concurrency races vanish.

### 8.6 `REPEATABLE_READ`

PostgreSQL gives the transaction a stable snapshot established at its first data-access command:

```text
Transaction A: BEGIN ISOLATION LEVEL REPEATABLE READ
Transaction A: SELECT stock → 10   [snapshot established]

Transaction B: UPDATE stock = 5; COMMIT

Transaction A: SELECT stock → 10   [same snapshot]
```

PostgreSQL's implementation also prevents phantom reads, which is stronger than the minimum SQL-standard requirement for this level.

If A later tries to update a row changed since its snapshot, PostgreSQL can abort A with a serialization-style concurrent update failure. The application must treat the whole transaction as failed and, if safe, retry the complete use case in a new transaction.

Do not keep using the old persistence context after such a failure.

### 8.7 `SERIALIZABLE`

PostgreSQL Serializable Snapshot Isolation tracks read/write dependencies. Successfully committed transactions have an outcome equivalent to some serial execution order.

It does not mean “all transactions literally run one at a time.” PostgreSQL permits concurrency and aborts a transaction when a dangerous structure cannot be allowed.

```text
Transaction A reads shared facts
Transaction B reads shared facts
A and B make writes that cannot both fit one serial history
        ↓
PostgreSQL aborts one with SQLSTATE 40001
        ↓
application may retry the entire transaction from the beginning
```

Advantages:

- strongest general database isolation guarantee;
- can protect invariants spanning predicate reads when used correctly.

Costs:

- serialization failures are expected control flow under contention;
- the complete transaction must be retryable;
- longer transactions increase conflict windows;
- external side effects inside a retried transaction are especially dangerous.

Do not set every method to `SERIALIZABLE` to avoid learning the actual invariant. Use it when the correctness requirement and retry design justify it.

### 8.8 Isolation declaration applies when a transaction is created

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void outer() {
    stronger();
}

@Transactional(isolation = Isolation.SERIALIZABLE)
public void stronger() {
    // if REQUIRED joins outer T1, it does not create a new serializable T2
}
```

Even with a proxy-crossing call, `REQUIRED` joins the existing transaction. The outer transaction's characteristics govern. Isolation declarations are meaningful for newly started transactions, such as a top-level `REQUIRED` with no existing transaction or a `REQUIRES_NEW` transaction.

If incompatible participation must fail rather than be silently accepted, configure validation of existing transactions and test it. A clearer design is usually to place the required isolation on the true outer boundary.

### 8.9 Isolation is not a substitute for atomic SQL

Compare:

```text
read stock into Java
calculate stock - amount
write absolute new stock
```

with:

```sql
UPDATE books
SET stock = stock - :amount
WHERE id = :id
  AND stock >= :amount;
```

The second statement lets PostgreSQL perform a guarded relative update atomically. Its update count can signal “missing or insufficient stock.” It may be a better tool than strengthening isolation for one narrow invariant.

JPA managed-state updates are convenient, but concurrency design still requires choosing among atomic SQL, optimistic versioning, pessimistic locks, constraints, or stronger isolation.

### 8.10 Verify the actual database level

Inside a transaction, PostgreSQL can report:

```sql
SHOW transaction_isolation;
```

or:

```sql
SELECT current_setting('transaction_isolation');
```

This is stronger evidence than assuming an annotation produced the intended database behavior—especially when a method might have joined an existing transaction.

### Checkpoint 8

On PostgreSQL, Transaction A at `READ_UNCOMMITTED` tries to read Transaction B's uncommitted stock update. What does A see? At `READ_COMMITTED`, can two SELECTs in A see different committed values?

<details>
<summary>Answer</summary>

PostgreSQL treats `READ_UNCOMMITTED` as `READ_COMMITTED`, so A does not see B's uncommitted value. Yes, at `READ_COMMITTED` each statement receives a fresh committed snapshot, so two reads can differ after B commits.

</details>

---

## 9. Concurrency, optimistic locking, pessimistic locking, and database locks

**⭐⭐⭐⭐ IMPORTANT**

Transactions define an atomic lifetime. Isolation defines general visibility/concurrency rules. Locks and versions protect particular conflict patterns. These tools overlap, but they are not synonyms.

### 9.1 Relationship map

```text
transaction
    ├── has an isolation level
    ├── causes PostgreSQL locks through writes or explicit lock requests
    ├── can use optimistic version checks at flush
    └── releases transaction-duration locks at commit/rollback
```

| Tool | Main question |
|---|---|
| Transaction | What succeeds or fails together? |
| Isolation | What concurrent database states can this transaction observe? |
| Optimistic locking | Has someone changed this row since I read its version? |
| Pessimistic locking | Should another conflicting writer wait before I make my decision? |
| Constraint/atomic SQL | Can PostgreSQL enforce the invariant directly? |

### 9.2 Lost update in managed entities

Without versioning:

```text
T1 loads Book#1 stock=10
T2 loads Book#1 stock=10
T1 sets 9; flushes UPDATE; commits
T2 sets 8; flushes UPDATE; commits
final stock=8
```

T2's update may overwrite the row after using stale state. An ordinary transaction does not automatically tell Hibernate that another completed unit changed the entity between read and write.

### 9.3 Optimistic locking with `@Version`

Add a version attribute only with the matching schema column and an API/error policy:

```java
@Version
@Column(nullable = false)
private Long version;
```

Conceptual SQL:

```sql
UPDATE books
SET stock = ?, version = ?
WHERE id = ?
  AND version = ?;
```

Timeline:

```text
T1 reads stock=10, version=7
T2 reads stock=10, version=7

T1 updates WHERE version=7
    → one row changed; version becomes 8; COMMIT

T2 updates WHERE version=7
    → zero rows changed
    → Hibernate detects stale state
    → optimistic-lock failure; T2 rolls back
```

Hibernate commonly exposes `jakarta.persistence.OptimisticLockException`; Spring's persistence exception translation can present an `ObjectOptimisticLockingFailureException` family exception at Spring boundaries. Exact wrapping depends on where flush/commit occurs.

Optimistic locking is useful when:

- concurrent conflicts are possible but not constant;
- holding a database lock across user think time is unacceptable;
- detecting and rejecting/retrying stale work is better than silent overwrite;
- a row/aggregate has a natural version policy.

It detects conflict; it does not choose the user experience. Decide whether to return a conflict, reload and ask the client, or retry a safe operation. Never blindly retry a non-idempotent use case or one with external side effects.

### 9.4 What rollback means after an optimistic conflict

The stale transaction is invalid. Do not catch an optimistic-lock exception inside the same transaction and keep mutating entities. Let it roll back, discard the persistence context, and retry from a fresh transaction only if the entire use case is safe to repeat.

### 9.5 Pessimistic locking

JPA can request a database lock while loading:

```java
public interface BookRepository extends JpaRepository<Book, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") long id);
}
```

PostgreSQL will typically use locking SQL such as `SELECT ... FOR UPDATE`.

```text
T1 SELECT ... FOR UPDATE Book#1
    ↓ obtains row lock
T2 tries conflicting lock/update Book#1
    ↓ waits (or fails according to timeout/deadlock policy)
T1 decides, updates, commits
    ↓ releases lock
At READ COMMITTED, T2 continues against the current row;
at REPEATABLE READ/SERIALIZABLE it may instead fail with SQLSTATE 40001
```

Pessimistic locking may be necessary when:

- a decision and update must serialize around a hot row;
- a conflict is frequent enough that repeated optimistic failures are wasteful;
- the workflow must inspect current state while preventing a conflicting writer before acting.

Costs and dangers:

- blocked transactions hold connections;
- locks last until commit/rollback;
- inconsistent lock order can deadlock;
- broad queries can lock more than expected;
- long external calls while holding locks harm throughput;
- lock-timeout behavior must be designed and handled.

Keep the transaction short and acquire multiple locks in a consistent order.

### 9.6 Ordinary writes already lock

You do not need `PESSIMISTIC_WRITE` for PostgreSQL to use locks. `UPDATE` and `DELETE` acquire row-level locks needed for correctness. Explicit pessimistic locking moves coordination earlier, before the update, so the application can make a protected decision.

### 9.7 Isolation versus locking

Stronger isolation can stabilize a snapshot or reject a nonserializable history. A row lock can make a particular concurrent writer wait. Neither completely replaces the other.

Examples:

- `READ_COMMITTED` + `@Version`: reads can change between statements, but stale entity writes are detected.
- `READ_COMMITTED` + `SELECT FOR UPDATE`: targeted rows are locked before a decision.
- `SERIALIZABLE`: PostgreSQL protects serializable history, possibly by aborting a whole transaction; the application still needs retry policy.
- `REPEATABLE_READ` without a version: stable snapshots do not mean every attempted concurrent write succeeds; PostgreSQL can abort a conflicting updater.

### 9.8 Choose by invariant, not fashion

```text
Can a database constraint or one atomic UPDATE express it?
    ├── yes → start there
    └── no
         ↓
Are conflicts uncommon and stale detection acceptable?
    ├── yes → optimistic @Version
    └── no
         ↓
Must a small set of rows be reserved while deciding?
    ├── yes → consider pessimistic locking, short transaction, lock order
    └── no
         ↓
Does a multi-query invariant require serializable behavior?
    └── consider SERIALIZABLE plus whole-transaction retry design
```

### Checkpoint 9

Does adding `@Version` stop Transaction B from reading the same Book while Transaction A is editing it? What does it do?

<details>
<summary>Answer</summary>

No. Optimistic locking normally allows both to read without reserving the row. At update/flush, the old version is included in the write condition; one update advances the version and a stale writer affects zero rows, producing an optimistic-lock failure instead of silently overwriting.

</details>

---

## 10. Transaction timeout and transaction length

**⭐⭐⭐⭐ IMPORTANT**

### 10.1 Declaring a timeout

```java
@Transactional(timeout = 5)
public void recalculateCatalogState() {
    // timeout value is expressed in seconds
}
```

The timeout is a maximum transaction-time policy communicated through Spring's transaction infrastructure to cooperating resources. The default is the underlying system's default, often effectively no explicit application timeout.

Do not interpret it as an exact Java stopwatch that forcibly kills any code at precisely 5.000 seconds. Enforcement can occur when Spring checks the transaction deadline, applies remaining time to a query, or the underlying JDBC/database resource reports a timeout. Driver cancellation and PostgreSQL execution state have their own behavior.

Distinguish:

| Setting/concept | Meaning |
|---|---|
| Spring transaction `timeout` | Deadline for the newly created transaction as coordinated by Spring/resources |
| Hikari `connectionTimeout` | How long a caller waits to borrow a connection from the pool |
| JDBC query timeout | Limit for a statement/query, if supported and applied |
| PostgreSQL `statement_timeout` | Database-side statement execution limit |
| PostgreSQL `lock_timeout` | Database-side time spent waiting for a lock |

One timeout does not automatically replace the others.

### 10.2 Timeout applies to the transaction that is created

```java
@Transactional(timeout = 30)
public void outer() {
    inner();
}

@Transactional(timeout = 2)
public void inner() {
    // REQUIRED participation does not start a new 2-second transaction
}
```

If `inner` joins the existing `REQUIRED` transaction, the outer transaction's deadline governs. Put the policy on the true outer boundary. `REQUIRES_NEW` can create a transaction with its own timeout, but all the independent-outcome and extra-connection costs still apply.

### 10.3 Why long transactions are dangerous

```text
long transaction
    ├── keeps a pooled connection unavailable longer
    ├── holds row/table locks longer
    ├── increases deadlock/conflict window
    ├── can retain transaction/XID state and, at stronger isolation or during
    │   an active statement, keep an older MVCC snapshot relevant longer
    ├── accumulates more managed state in the persistence context
    ├── increases chance of timeout or serialization failure
    └── delays other requests that need the same resources
```

“Long” is workload-specific. A 500 ms transaction may be normal in one system and harmful in a high-throughput hot-row path. Measure connection acquisition, query duration, lock waits, and transaction age.

### 10.4 Slow external calls inside a transaction

```java
@Transactional
public void createBookAndNotify(...) {
    Book book = repo.save(...);
    emailClient.send(...); // network delay while DB transaction remains open
}
```

Problems:

- the transaction may hold a JDBC connection while waiting on DNS, TLS, or a remote server;
- locks can remain held;
- the external call can succeed and the later database commit can fail;
- a timeout can roll back the database but cannot unsend the email;
- remote latency reduces pool throughput.

Prefer a short database transaction and an explicit post-commit/outbox design appropriate to the reliability need. Section 13 covers the consistency boundary.

### 10.5 User think time is never transaction time

Do not open a transaction, return a form to a client, and expect the same transaction to remain open until the client responds. Web requests are separate and user think time is unbounded.

```text
request 1: read Book and return representation → transaction ends
user edits for 30 seconds/minutes
request 2: submit update                → new transaction
```

Use a version field, ETag/precondition, or another concurrency token if request 2 must detect that the row changed since request 1.

### Checkpoint 10

Does `@Transactional(timeout = 5)` mean a caller may wait only five seconds for HikariCP to provide a connection? Is a two-second inner `REQUIRED` timeout guaranteed to shorten an existing thirty-second outer transaction?

<details>
<summary>Answer</summary>

No to both. Hikari connection acquisition uses its own `connectionTimeout`. A participating `REQUIRED` method joins the existing transaction; its timeout declaration does not create or replace the outer deadline.

</details>

---

## 11. Read-only transactions

**⭐⭐⭐⭐ IMPORTANT**

### 11.1 Intent

```java
@Transactional(readOnly = true)
public Book findById(long id) {
    return required(id);
}
```

`readOnly = true` declares:

> This transaction is intended to read, not perform normal persistent writes.

It communicates design intent to maintainers and allows transaction managers, Hibernate, JDBC drivers, or the database to apply optimizations/hints where supported.

### 11.2 Hibernate/Spring optimization

With Spring's Hibernate integration, a read-only transaction may adjust Hibernate flush/read-only behavior so loaded entities need less normal dirty-checking work and automatic flush is reduced. Exact behavior depends on the Spring transaction manager, JPA dialect/provider, and versions.

Conceptually:

```text
read-write transaction
    → track managed mutations for normal flush

read-only transaction
    → communicate no-write intent
    → provider may skip snapshots/dirty processing or reduce flush behavior
```

Do not code against one internal flush-mode name as if it were the contract. The public contract is a read-only hint/declaration, not “all Java setters now throw.”

### 11.3 What it does not guarantee

`readOnly = true` is not:

- a database user permission;
- a Java compiler rule;
- a Bean Validation constraint;
- proof that no native or bulk SQL can execute;
- a universal cross-database guarantee that every write is rejected;
- a substitute for a PostgreSQL role with appropriate privileges;
- a reason to mutate an entity and hope Hibernate will save it.

```java
@Transactional(readOnly = true)
public void misleading(long id) {
    Book book = required(id);
    book.setStock(0); // Java allows this
}
```

Depending on provider/settings, the scalar mutation may not be automatically flushed. Another explicit write path may behave differently, and a database-level read-only transaction—if actually requested/enforced—may reject writes. The only sound design is: do not write in a method declared read-only.

### 11.4 Read-only is not authorization

Authorization is enforced with database privileges and application security policy. If the application's database user has `UPDATE` permission, an annotation hint does not revoke it.

```text
@Transactional(readOnly = true) = intent and possible optimization
PostgreSQL GRANT/REVOKE        = database authorization
```

These solve different problems.

### 11.5 Class-level read-only with write overrides

This is the current Book Catalog pattern:

```java
@Service
@Transactional(readOnly = true)
public class BookService {

    public Book findById(long id) {
        return required(id);
    }

    public List<Book> findAll(String author, int page, int size) {
        // read use case
    }

    @Transactional
    public Book create(...) {
        // readOnly defaults to false here
    }

    @Transactional
    public Book replace(...) {
        // write transaction
    }

    @Transactional
    public Book setStock(...) {
        // write transaction
    }

    @Transactional
    public void delete(...) {
        // write transaction
    }
}
```

Benefits:

- the safe default for this read-heavy service is visible once;
- every write boundary is conspicuous;
- write methods still use `REQUIRED` by default;
- read methods get a coherent persistence context with OSIV disabled.

The method-level annotation must be on an invocation intercepted by the service proxy. Self-invocation caveats still apply.

### 11.6 Do all reads require an explicit transaction?

Spring Data repository methods can supply their own transactional behavior, and some databases permit individual reads outside an explicit application transaction. The service-level read-only boundary is still useful when:

- several reads must form one coherent use case;
- lazy associations must be initialized deliberately;
- entities need one persistence context identity scope;
- query/DTO mapping should complete before returning;
- transaction intent should be explicit and observable.

Do not add a transaction around CPU-only formatting after all data is loaded. Keep the boundary aligned with database work.

### Checkpoint 11

Can `readOnly = true` replace a PostgreSQL read-only user? If code calls a setter, will Java reject it?

<details>
<summary>Answer</summary>

No. It is intent plus possible transaction/provider optimizations, not authorization or a Java immutability rule. Write permission belongs to database security, and application design must avoid mutation in the read-only use case.

</details>

---

## 12. Multiple repositories in one transaction

**⭐⭐⭐⭐⭐ MUST KNOW**

This is one of the main reasons transaction boundaries belong at the service/use-case layer.

### 12.1 One service operation, several persistence participants

Imagine a minimal inventory adjustment with a small audit row that should be part of the same outcome:

```java
@Service
public class StockTransferService {
    private final BookRepository bookRepository;
    private final StockAuditRepository auditRepository;

    @Transactional
    public void transfer(long fromId, long toId, int amount) {
        Book from = required(fromId);
        Book to = required(toId);

        from.decreaseStock(amount);
        to.increaseStock(amount);

        auditRepository.save(
                StockAudit.completed(fromId, toId, amount));
    }
}
```

```text
service transaction T1
    ├── BookRepository loads Book A
    ├── BookRepository loads Book B
    ├── managed A and B mutate
    └── StockAuditRepository persists audit
         ↓
SQL synchronization: UPDATE A, UPDATE B, INSERT audit
    (an IDENTITY audit INSERT may execute earlier to obtain its ID)
         ↓
all succeed → COMMIT all
one fails   → ROLLBACK all
```

Whether the audit insert runs during `save`/`persist` or at flush depends on identifier generation and provider behavior. Neither timing is a commit.

The number of repositories does not determine the number of transactions. The outer service boundary and propagation do.

### 12.2 Failure between two changes

```java
@Transactional
public void demonstrateAtomicity(long firstId, long secondId) {
    Book first = required(firstId);
    Book second = required(secondId);

    first.setStock(first.getStock() - 1);
    repo.flush(); // first UPDATE may execute

    if (true) {
        throw new IllegalStateException("fail between changes");
    }

    second.setStock(second.getStock() + 1);
}
```

Even though the first update was flushed, the transaction rollback discards it. If the second book was never mutated, there is also nothing for it to commit.

An even stronger exercise mutates both and then throws, proving both SQL updates roll back.

### 12.3 Repository-local boundaries are insufficient

Without an outer transaction:

```text
bookRepository operation A
    ↓ transaction TA commits

auditRepository operation B
    ↓ transaction TB fails

database contains A without B
```

The repository cannot know that a future operation in another repository is required for the same business invariant. The service can.

### 12.4 One transaction manager must cover the resources

In this project, both repositories use the same JPA `EntityManagerFactory` and PostgreSQL `DataSource`, so one JPA transaction manager can coordinate them.

Do not generalize “multiple repositories” to “any number of unrelated systems.” Two databases, a database plus Kafka, or a database plus filesystem are not automatically one local transaction. Section 13 explains that boundary.

### 12.5 Failure ordering and constraints

Hibernate may reorder or batch DML during flush. Do not build correctness on an assumed log order such as “repository call A means SQL A has committed before repository call B.” Database constraints and the transaction outcome remain authoritative.

If early constraint detection is necessary, flush deliberately and still remember that the whole transaction can roll back later.

### Checkpoint 12

Book A's update SQL succeeds, then an audit insert violates a `NOT NULL` constraint in the same service transaction. What should remain in PostgreSQL after rollback?

<details>
<summary>Answer</summary>

Neither change. SQL success before the later failure is not an independent commit. The service transaction covers both repositories, so PostgreSQL rolls back A's uncommitted update and the failed insert produces no audit row.

</details>

---

## 13. Transactions and external systems

**⭐⭐⭐⭐ IMPORTANT boundary; ⭐⭐ FUTURE KNOWLEDGE for advanced patterns**

### 13.1 A local database transaction controls database resources

PostgreSQL can roll back:

- inserts, updates, and deletes in its transaction;
- constraint-triggered work in that transaction;
- other PostgreSQL state participating in that same transaction.

It cannot automatically roll back:

- an email already accepted by a mail server;
- an HTTP request already processed by another service;
- a message already published outside the transaction;
- a file already written to disk;
- a payment already accepted by a provider.

```text
PostgreSQL transaction T1
    ├── UPDATE books
    └── COMMIT / ROLLBACK controls PostgreSQL work

email / HTTP / file / external broker
    └── separate system with separate outcome
```

### 13.2 The dual-write problem

```java
@Transactional
public void createAndPublish(...) {
    repo.save(...);          // database work
    broker.publish(...);     // external work
}
```

Possible failure histories:

```text
publish succeeds → database commit fails
    result: external event exists for absent database change

database commits → process crashes before publish
    result: database change exists but event is absent
```

Simply swapping the order changes which gap exists; it does not create atomicity across systems.

### 13.3 Slow calls add a resource problem

An external call inside the transaction also keeps database resources open while waiting. Even if logical consistency were acceptable, connection and lock duration may not be.

### 13.4 After-commit action

**⭐⭐⭐ NICE TO KNOW**

An action triggered only after a successful commit avoids sending something for a rolled-back transaction:

```text
database transaction commits
        ↓
after-commit callback sends notification
```

It does **not** make delivery guaranteed:

```text
COMMIT succeeds
process crashes before or during notification
```

Use it for best-effort work or combine it with durable retry/idempotency appropriate to the requirement.

### 13.5 Transactional outbox

**⭐⭐ FUTURE KNOWLEDGE**

```text
one PostgreSQL transaction
    ├── change Book state
    └── insert outbox event row
COMMIT both

separate worker
    ↓ reads unpublished outbox rows
    ↓ publishes externally
    ↓ records delivery/progress with retry and idempotency
```

The atomic step is only “business row plus outbox row,” both in the same database. External delivery remains asynchronous and usually at-least-once, so consumers need idempotency.

This is more reliable than an in-memory after-commit callback when losing an event is unacceptable. It is not required for the small Book Catalog transaction exercise.

### 13.6 Saga

**⭐⭐ FUTURE KNOWLEDGE**

A Saga coordinates several local transactions across services and defines compensating actions for later failure. Compensation is a new business operation, not a time machine that erases every external observation.

Do not turn the Book Catalog into a distributed-systems project. Remember only the boundary:

```text
one local @Transactional method
    ≠ automatic atomic transaction across independent systems
```

### Checkpoint 13

If an email is sent inside a transaction and PostgreSQL commit fails afterward, will Spring rollback recall the email? Would moving the send to `AFTER_COMMIT` guarantee delivery?

<details>
<summary>Answer</summary>

No and no. A database rollback cannot undo the mail server's action. After-commit prevents sending for a failed commit, but a crash after commit and before delivery can still lose the action. Use a durable outbox when that gap is unacceptable.

</details>

---

## 14. Transaction synchronization and after-commit work

**⭐⭐⭐ NICE TO KNOW; advanced APIs are not the daily default**

### 14.1 Why phase matters

Suppose a “Book created” notification is emitted before commit:

```text
service sends notification
    ↓
flush finds duplicate ISBN
    ↓
transaction rolls back
    ↓
recipient was told about a Book that does not exist
```

Binding a listener to successful commit avoids that exact false notification.

### 14.2 `@TransactionalEventListener`

```java
public record BookCreatedEvent(long bookId) {}
```

```java
@Transactional
public Book create(...) {
    Book book = repo.save(...);
    eventPublisher.publishEvent(new BookCreatedEvent(book.getId()));
    return book;
}
```

```java
@Component
public class BookNotificationListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterBookCreated(BookCreatedEvent event) {
        // best-effort external action after successful commit
    }
}
```

The default phase is `AFTER_COMMIT`. Available phases include:

| Phase | Meaning |
|---|---|
| `BEFORE_COMMIT` | Before commit completion; failure can still affect the transaction |
| `AFTER_COMMIT` | Only after successful commit |
| `AFTER_ROLLBACK` | Only after rollback |
| `AFTER_COMPLETION` | After either outcome |

If no transaction exists, a transactional event listener is normally not invoked unless `fallbackExecution = true` is chosen. Use that option only if nontransactional publication should deliberately trigger it.

### 14.3 Important after-commit caveats

- The database transaction has already committed; a listener exception cannot undo it.
- The listener is synchronous by default and can still delay the request thread.
- A process crash can lose an in-memory event/action.
- Resources can still be present during completion callbacks, but further database writes do not become part of the already-committed transaction.
- A synchronous `AFTER_COMMIT` listener runs before ordinary completion cleanup has fully returned to the caller. Slow email/HTTP work can delay the response and may delay release of transaction-associated connection capacity back to HikariCP even though PostgreSQL already committed; keep the callback short.
- If a listener truly needs a new database write, it needs a deliberate new transaction such as `REQUIRES_NEW`; then apply the same pool/consistency analysis.
- For reliable external publication, prefer an outbox rather than pretending a callback is atomic delivery.

### 14.4 Low-level synchronization API

**⭐⭐ FUTURE KNOWLEDGE**

Spring also exposes transaction synchronization directly:

```java
if (!TransactionSynchronizationManager.isSynchronizationActive()) {
    throw new IllegalStateException("Transaction synchronization required");
}

TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // best-effort action
            }
        });
```

This is infrastructure-oriented and couples code to Spring transaction context. Prefer a clear application event listener for ordinary application code. Use the low-level API only when building a focused infrastructure integration and when callback semantics are fully understood.

### 14.5 `AFTER_COMMIT` versus `REQUIRES_NEW` audit

```text
AFTER_COMMIT
    outer transaction must commit first
    no record/action for rolled-back attempts

REQUIRES_NEW called before outer completion
    inner result may commit even if outer later rolls back
    useful only when the attempt itself is the durable fact
```

Neither is universally better. They encode different truths.

### Checkpoint 14

An `AFTER_COMMIT` listener throws while sending an HTTP notification. Can that exception roll back the already-created Book? If the notification must never be lost, is an in-memory listener sufficient?

<details>
<summary>Answer</summary>

No. Commit already succeeded. An in-memory listener also cannot close the crash gap, so it is insufficient for guaranteed delivery; a durable outbox/retry design is the usual future solution.

</details>

---

## 15. Connection-pool relationship

**⭐⭐⭐⭐⭐ MUST KNOW**

The previous DataSource/HikariCP model remains unchanged:

```text
transaction begins or first needs JDBC work
    ↓
transaction obtains/uses a JDBC Connection
    ↓
HikariCP marks the logical connection handle active
    ↓
Hibernate executes SQL through pgJDBC
    ↓
PostgreSQL commits or rolls back
    ↓
transaction cleanup closes the logical handle
    ↓
HikariCP returns reusable physical capacity to the pool
```

### 15.1 Transaction lifetime drives connection occupancy

The transaction manager/provider may acquire a connection lazily, and release mode is provider-specific, but for practical design assume database work ties up pool capacity for the transactional work.

```text
short transaction
    → connection returned quickly
    → next request can borrow it

long transaction
    → connection remains active longer
    → more callers wait
    → acquisition can exceed Hikari connectionTimeout
```

With `maximum-pool-size: 5`, at most five physical pool connections are managed for this application instance. That is a deliberate bound, not one connection per service or per user.

### 15.2 Commit/rollback before reuse

The transaction manager completes the transaction before returning the handle. HikariCP resets relevant connection state for safe reuse, but the application should not depend on pool cleanup as its transaction strategy.

Never manually close or commit an injected transaction-bound connection from service code. Ownership belongs to the transaction infrastructure.

### 15.3 `REQUIRES_NEW` and extra leases

```text
outer T1 holds C1
    ↓ suspended
inner T2 borrows C2
    ↓ completes and returns C2
outer T1 resumes with C1
```

Nested `REQUIRES_NEW` calls can increase simultaneous connection demand per request. With enough concurrent outer transactions, this can exhaust a pool even when there is no traditional connection leak.

### 15.4 Locks amplify pool pressure

```text
T1 holds row lock and connection C1
T2 waits for row lock while holding connection C2
T3 waits for another lock while holding connection C3
T4 and T5 do similar work
T6 asks Hikari for a connection
    ↓ pool already active=5
    ↓ waits, then may time out
```

The immediate error may look like a pool acquisition timeout, while the root cause is lock contention or a long transaction. Diagnose PostgreSQL waits and transaction ages before increasing the pool.

### 15.5 Pool size is not the universal fix

A larger pool can:

- let more slow work reach PostgreSQL at once;
- increase lock contention;
- consume more server sessions/memory;
- delay, rather than solve, exhaustion;
- multiply across application instances.

```text
possible application sessions
    ≈ number of instances × maximumPoolSize
```

Investigate first:

- transaction duration;
- slow queries/indexes;
- lock waits/deadlocks;
- unnecessary external calls;
- accidental `REQUIRES_NEW` nesting;
- leaked manually managed connections;
- traffic/concurrency relative to database capacity.

### 15.6 Pool metrics tell a resource story, not transaction semantics

Active, idle, pending, and acquisition-time metrics can show pressure. They do not prove whether a transaction committed. Pair pool observations with Spring transaction logs, Hibernate SQL, and PostgreSQL state.

### Checkpoint 15

Hikari reports all five connections active and new callers timing out. Should the first action always be increasing `maximum-pool-size`?

<details>
<summary>Answer</summary>

No. Find why connections are held: long transactions, slow SQL, locks, external calls, `REQUIRES_NEW`, leaks, or excessive concurrency. A larger pool can amplify database pressure and only postpone the same failure.

</details>

---

## 16. Common transaction mistakes

**⭐⭐⭐⭐⭐ MUST KNOW**

### 16.1 Mistake matrix

| Mistake | Why it fails | Better model |
|---|---|---|
| Put `@Transactional` on controllers | Couples HTTP/serialization lifetime to persistence; can keep DB resources open and hide lazy SQL | Put atomic use-case boundaries on service methods |
| Assume one HTTP request is one transaction | MVC dispatch and DB transaction lifetimes are separate | Trace the proxied service call that actually begins/joins |
| Assume `save()` commits | `save` changes persistence-context state through persist/merge semantics | Commit occurs when transaction advice successfully completes |
| Assume `flush()` commits | Flush executes/synchronizes SQL inside the active transaction | A later rollback can discard flushed SQL |
| Call `save()` after every managed mutation | Dirty checking already tracks managed state | Mutate the managed entity; save only when persist/merge semantics are needed |
| Annotate a private method | Proxy advice cannot intercept a private target method | Use a public service boundary or a deliberate programmatic transaction |
| Rely on self-invocation | `this.method()` does not re-enter the proxy | Put boundary on outer method or move independent operation to another bean |
| Instantiate service with `new` | Object is not the Spring proxy | Obtain/inject the Spring-managed bean |
| Put `REQUIRES_NEW` everywhere | Creates partial commits, extra connections, suspension, and harder reasoning | Use default `REQUIRED`; choose independence only for a real requirement |
| Make transactions excessively broad | Holds connections/locks and managed state too long | Include database work needed for the use case, not unrelated waiting/formatting |
| Make a remote HTTP/email call inside a DB transaction | Holds resources and creates an unrollbackable side effect | Use after-commit or durable outbox design as requirements demand |
| Treat `readOnly = true` as write prevention | It is intent/hint, not universal authorization | Do not write; use DB privileges for security |
| Catch runtime exceptions and return success | Proxy can see a normal return and attempt commit | Rethrow/translate or perform a recovery that truly makes commit correct |
| Catch a flush error and continue | Transaction/context may be rollback-only or unusable | Translate/rethrow; let boundary roll back and discard context |
| Assume every exception rolls back | Checked exceptions do not by default in this project | Define narrow `rollbackFor` rules when checked failure invalidates work |
| Add `rollbackFor = Exception.class` mechanically | Hides intended exception contracts and may roll back legitimate checked outcomes | Decide per use case and exception meaning |
| Assume inner `REQUIRED` can commit independently | It joins the same physical transaction | Use one shared outcome, or consciously choose `REQUIRES_NEW` |
| Catch inner failure and assume rollback-only vanished | Participating scope can already have marked shared transaction | Expect rollback/`UnexpectedRollbackException`; redesign recovery |
| Assume every repository call joins one large transaction | It joins only when an actual outer transaction exists and propagation permits | Establish the service boundary explicitly |
| Declare stronger isolation only on an inner joining method | Existing transaction's isolation governs | Put isolation on the outer transaction creator |
| Assume isolation names behave identically on all DBs | Databases implement levels differently | Verify PostgreSQL semantics and actual configured level |
| Assume PostgreSQL `READ_UNCOMMITTED` permits dirty reads | PostgreSQL maps it to `READ_COMMITTED` | Test database-specific behavior |
| Use high isolation instead of identifying the invariant | Can cause avoidable aborts and still lacks retry design | Consider constraints, atomic SQL, `@Version`, or targeted locks |
| Add `@Version` without schema/API policy | Startup/schema mismatch and unhandled stale failures | Add column and define conflict/retry behavior deliberately |
| Hold pessimistic locks through slow work | Blocks transactions and consumes pool capacity | Keep locked transaction short; order locks consistently |
| Retry only the failed SQL statement after serialization error | The prior reads/decisions used an invalid history | Retry the complete transaction in a fresh context when safe |
| Ignore `REQUIRES_NEW` pool demand | Outer can retain one connection while inner requests another | Model concurrent leases against Hikari's maximum |
| Increase pool size before diagnosing | Can amplify slow SQL and lock contention | Inspect transactions, queries, locks, and acquisition waits |
| Treat Spring transaction abstraction as magic | Leads to wrong assumptions about SQL and resource lifetime | Translate every operation down to Hibernate, JDBC, Hikari, and PostgreSQL |

### 16.2 `save()` versus dirty checking mistake

Unnecessary:

```java
@Transactional
public Book setStock(long id, int stock) {
    Book book = required(id); // managed
    book.setStock(stock);
    return repo.save(book);   // usually redundant merge decision/work
}
```

Clearer:

```java
@Transactional
public Book setStock(long id, int stock) {
    Book book = required(id);
    book.setStock(stock);
    return book;
}
```

This is not a ban on `save`. Creating a new entity needs persist semantics, and a deliberately detached input may need merge semantics. The mistake is using `save` as a ritual because one assumes dirty checking does not exist.

### 16.3 Controller transaction mistake

```java
@RestController
@Transactional // avoid this broad web/persistence coupling
public class BookController {
    // ...
}
```

Consequences can include:

- transactions around request/response work unrelated to database consistency;
- lazy queries during response mapping;
- unclear rollback rule for web exceptions;
- longer connection/lock duration;
- service methods that cannot express their own atomic contract clearly.

Keep request validation and global exception handling at the web boundary. Keep database atomicity at the service boundary.

### 16.4 Hidden partial commit mistake

```java
public void transferWithoutBoundary(...) {
    firstRepository.change(...);  // own transaction can complete
    secondRepository.change(...); // later failure cannot undo first
}
```

Adding `@Transactional` to each repository is not equivalent to adding it to the service use case.

### 16.5 “Log and swallow” mistake

```java
@Transactional
public void operation() {
    try {
        performRequiredStep();
    } catch (RuntimeException failure) {
        log.error("Step failed", failure);
    }
}
```

Logging does not make the required step optional. If failure invalidates the use case, rethrow or translate. If it is truly optional, define why committing without it is correct and ensure the resource has not marked the transaction rollback-only.

### 16.6 Blind retry mistake

A retry repeats code. It can repeat:

- a payment request;
- an email;
- an audit insert without a uniqueness key;
- a non-idempotent stock decrement;
- stale inputs that must be revalidated.

Retries for optimistic/serialization failures must start a fresh transaction and persistence context, reread current state, and be limited to a use case safe to repeat. Retry policy is not a one-line annotation substitute for business analysis.

---

## 17. Debugging transactions

**⭐⭐⭐⭐⭐ MUST KNOW**

Debugging works best when each layer supplies one piece of evidence.

### 17.1 Development logging

The current project already enables Hibernate SQL. A focused local configuration can add Spring transaction detail:

```yaml
logging:
  level:
    org.springframework.transaction: TRACE
    org.springframework.orm.jpa.JpaTransactionManager: DEBUG
    org.hibernate.SQL: DEBUG
```

What each logger reveals:

| Logger | Useful evidence |
|---|---|
| `org.springframework.transaction` at `TRACE` | Transaction attributes, interceptor boundaries, participating/creating behavior, completion decisions |
| `org.springframework.orm.jpa.JpaTransactionManager` at `DEBUG` | JPA transaction begin/participation/commit/rollback and EntityManager coordination |
| `org.hibernate.SQL` at `DEBUG` | SQL text generated/executed by Hibernate |

Optional and very verbose local diagnostics:

```yaml
logging:
  level:
    org.hibernate.orm.jdbc.bind: TRACE
    com.zaxxer.hikari: DEBUG
```

`org.hibernate.orm.jdbc.bind` can reveal bound values. Those values may include sensitive or personal data; enable it only in a controlled development environment. Hikari debug logging is more useful for configuration and pool lifecycle than as proof of every business transaction.

Do not leave blanket transaction, bind, or pool trace logging enabled in production. It can expose data, create large volumes, and change timing.

### 17.2 Read log order correctly

Example reasoning:

```text
Spring: creating transaction for BookService.setStock
Hibernate: SELECT ... FROM books WHERE id=?
application: Book Java field changes
Hibernate: UPDATE books SET ... WHERE id=?
Spring: committing JPA transaction
```

The SQL logger may not print `COMMIT` as an SQL statement because commit is a JDBC transaction operation/protocol action, not ordinary Hibernate-generated DML. Use transaction logs and a fresh database read as commit evidence.

An `UPDATE` line alone proves only that SQL execution was attempted during flush.

### 17.3 Deliberately force both paths

Success experiment:

```java
@Transactional
public void successfulChange(long id) {
    required(id).setStock(41);
}
```

Failure after mutation:

```java
@Transactional
public void rollbackBeforeFlush(long id) {
    required(id).setStock(42);
    throw new IllegalStateException("intentional rollback");
}
```

Failure after explicit flush:

```java
@Transactional
public void rollbackAfterFlush(long id) {
    required(id).setStock(43);
    repo.flush();
    throw new IllegalStateException("intentional rollback after SQL");
}
```

After every call, verify with a **fresh** request or `psql` query. Do not inspect the same mutated Java object and call that database verification.

### 17.4 Useful PostgreSQL observations

In `psql` or pgAdmin, inspect the committed row:

```sql
SELECT id, isbn, stock
FROM books
WHERE id = 1;
```

Inspect the session's actual isolation level:

```sql
SHOW transaction_isolation;
```

Observe active/idle-in-transaction sessions and waits:

```sql
SELECT
    pid,
    usename,
    application_name,
    state,
    wait_event_type,
    wait_event,
    xact_start,
    query_start,
    query
FROM pg_stat_activity
WHERE datname = current_database()
ORDER BY xact_start NULLS LAST, query_start;
```

Observe locks for the current database:

```sql
SELECT
    l.pid,
    l.locktype,
    l.mode,
    l.granted,
    l.relation::regclass AS relation,
    a.state,
    a.wait_event_type,
    a.wait_event,
    a.query
FROM pg_locks AS l
LEFT JOIN pg_stat_activity AS a ON a.pid = l.pid
WHERE a.datname = current_database()
ORDER BY l.pid, l.granted, l.locktype, l.mode;
```

These views can expose other sessions' queries depending on permissions. Use appropriate development credentials and never publish secrets or sensitive SQL values.

A session in `idle in transaction` has begun a transaction and is waiting for the client instead of completing it. It may retain locks, an old snapshot, and a pooled connection; repeated or old entries deserve investigation.

### 17.5 Two-session concurrency lab

Open two `psql` terminals.

**Session A**

```sql
BEGIN;
UPDATE books SET stock = stock - 1 WHERE id = 1;
-- do not commit yet
```

**Session B**

```sql
BEGIN;
UPDATE books SET stock = stock - 1 WHERE id = 1;
-- observe blocking while A holds the row lock
```

Back in A:

```sql
COMMIT;
```

Then observe B continue and decide whether to `COMMIT` or `ROLLBACK`.

This demonstrates that ordinary updates acquire locks. It does not by itself demonstrate optimistic locking; `@Version` adds a version predicate and stale-update detection through Hibernate.

### 17.6 Breakpoints that answer useful questions

Place breakpoints:

1. at the first line of the public transactional method;
2. after the entity load;
3. after Java mutation;
4. before and after explicit `flush()`;
5. in the global exception handler;
6. in a separate bean's inner transactional method for propagation labs.

At a controlled diagnostic breakpoint, inspect:

- entity values and whether the expected instance is being mutated;
- call stack for AOP proxy/interceptor frames;
- whether SQL appeared before or after this point;
- PostgreSQL `pg_stat_activity` and `pg_locks` from another session;
- Hikari active/pending metrics if exposed.

For a temporary diagnostic—not business logic—Spring can report:

```java
boolean transactionActive =
        TransactionSynchronizationManager.isActualTransactionActive();
```

This answers “is Spring currently coordinating an actual transaction here?” It does not prove which data committed or whether a later rollback-only decision will occur.

### 17.7 Diagnosing self-invocation

Symptoms:

- no transaction log entry for the inner method;
- `REQUIRES_NEW` does not create a second transaction;
- timeout/isolation/rollback rules on inner method appear ignored;
- call stack shows direct `target.outer → target.inner`.

Test by moving the inner method temporarily to a separate Spring bean and calling that injected bean. If logs now show a second interception, the original problem was call topology, not annotation spelling.

### 17.8 Diagnosing `UnexpectedRollbackException`

Trace backward for:

- an inner participating method that threw a runtime exception;
- a repository/Hibernate failure caught by outer code;
- explicit `setRollbackOnly()`;
- an optimistic, serialization, or constraint failure;
- logs saying the existing transaction was marked rollback-only.

The commit-site exception is often the final symptom, not the first cause. Preserve exception causes and inspect earlier logs.

### 17.9 Diagnosing “UPDATE logged but row unchanged”

Possible explanations:

- transaction rolled back after flush;
- commit failed;
- a later transaction changed the row again;
- verification queried a different database/schema/port;
- optimistic version condition affected zero rows and caused rollback;
- application was stopped before completion.

Check transaction completion logs and query the same PostgreSQL instance configured at `127.0.0.1:5433/book_catalog`.

### 17.10 Diagnosing “no UPDATE logged”

Ask in order:

1. Was the expected method invoked through the proxy?
2. Is the entity managed or detached?
3. Did the mapped value actually change?
4. Is the transaction read-only?
5. Did execution fail before flush?
6. Is SQL logging enabled for the correct logger?
7. Did a bulk/native operation or another persistence context create stale state?
8. Was the transaction completed, or is execution paused before flush?

### 17.11 Layer-by-layer debugging sequence

```text
1. HTTP boundary
   Did validation/conversion prevent controller invocation?
        ↓
2. Controller → service reference
   Is this the Spring-managed proxy?
        ↓
3. Transaction logs
   New, joined, suspended, rollback-only, committed, or rolled back?
        ↓
4. Persistence context
   Is the entity managed and actually dirty?
        ↓
5. Hibernate SQL
   Did SELECT/UPDATE/INSERT/DELETE execute, and when?
        ↓
6. PostgreSQL
   What are the current rows, isolation, locks, waits, and transaction ages?
        ↓
7. HikariCP
   Are connections active because work is slow/blocked/independent?
```

### Checkpoint 17

Hibernate logs an `UPDATE`, then the API returns an error. What two pieces of evidence are needed before claiming the row committed?

<details>
<summary>Answer</summary>

Evidence of successful transaction completion/commit and a fresh read of the PostgreSQL row. The SQL line alone can represent a flush whose transaction later rolled back.

</details>

---

## 18. Interview-relevant knowledge

**⭐⭐⭐⭐ IMPORTANT**

### 18.1 Core questions and compact answers

| Question | Strong answer |
|---|---|
| How does `@Transactional` work internally? | It is metadata read by Spring transaction advice, commonly through an AOP proxy. `TransactionInterceptor` asks a `PlatformTransactionManager` to create/join, invokes the target, then commits or rolls back according to outcome and rules. |
| Why does self-invocation break transaction interception? | `this.otherMethod()` calls the target directly and does not re-enter the proxy, so the inner method's metadata is not independently evaluated. |
| Where should boundaries normally be placed? | On public service/use-case methods that know all database steps that must succeed or fail together. |
| What is default propagation? | `REQUIRED`: join an existing transaction or create one when absent. |
| What is default isolation? | `DEFAULT`: use the underlying transaction system/database default; PostgreSQL normally uses `READ COMMITTED`. |
| What rolls back by default? | An escaping `RuntimeException` or `Error`; not an ordinary checked exception under the project's default policy. |
| What is flush versus commit? | Flush synchronizes persistence-context changes as SQL inside the active transaction. Commit successfully completes the database transaction. Flushed SQL can still roll back. |
| Does `save()` commit immediately? | No. For JPA it typically selects persist or merge semantics; SQL and commit timing are separate. |
| Is `save()` needed after changing a managed entity? | Usually no. Dirty checking detects mapped state changes at flush. |
| `REQUIRED` versus `REQUIRES_NEW`? | `REQUIRED` shares the caller's physical transaction/outcome. `REQUIRES_NEW` suspends it and creates an independent transaction, usually requiring another connection. |
| Why can `REQUIRES_NEW` exhaust a pool? | The outer transaction can retain one connection while the inner transaction requests a second; concurrent outers can occupy the whole pool. |
| What is rollback-only? | A transaction status meaning commit must not succeed. An inner participating scope can mark the shared transaction rollback-only. |
| Why `UnexpectedRollbackException`? | The outer caller attempted/expected commit, but an inner participant had already silently marked the shared transaction rollback-only; Spring reports the rollback instead of returning false success. |
| What does `readOnly = true` do? | Declares read intent and may enable transaction/provider/JDBC optimizations. It is not authorization or a universal write prohibition. |
| What does isolation control? | The visibility and permitted interaction of concurrent database transactions. Actual guarantees are database-specific. |
| Isolation versus optimistic locking? | Isolation governs transaction-wide visibility/history; `@Version` detects that a particular entity became stale at update/delete time. |
| Optimistic versus pessimistic locking? | Optimistic permits concurrency and rejects stale writes via a version; pessimistic acquires a DB lock so conflicting work waits/fails before proceeding. |
| Does a transaction roll back an email or HTTP call? | No. A local DB transaction controls its participating DB resource, not already completed external side effects. |
| What happens to a connection? | SQL uses a transaction-associated JDBC connection leased through HikariCP; after commit/rollback and cleanup, the logical handle is returned to the pool. |
| Does OSIV mean one transaction for the whole HTTP request? | No. OSIV can extend persistence-context availability, not necessarily the database transaction. This project disables it. |

### 18.2 Explain `@Transactional` in sixty seconds

> A caller invokes a Spring-managed service proxy. Spring's `TransactionInterceptor` reads the method/class transaction metadata and asks the configured transaction manager—normally `JpaTransactionManager` here—to start or join a transaction. The target method uses repositories through a transaction-bound persistence context. Hibernate tracks managed entity changes and flushes SQL through a JDBC connection obtained from the Hikari-backed DataSource. If the method and completion succeed, the manager commits PostgreSQL; if an applicable failure escapes or the status is rollback-only, it rolls back. Cleanup returns the connection to the pool. Calls that bypass the proxy, such as self-invocation, do not activate new annotation semantics.

### 18.3 Explain flush versus commit in thirty seconds

> A Java mutation changes in-memory state. Hibernate dirty checking detects it, and flush translates pending work into SQL inside the current transaction. PostgreSQL may execute the update and hold locks, but the change is still uncommitted. Commit is the later transaction outcome that makes all its successful work durable. A runtime failure after flush can roll the update back.

### 18.4 Explain service boundary in thirty seconds

> Repository methods know persistence operations; the service knows the use case. If changing two books and inserting an audit record is one invariant, the service transaction surrounds all three. Default `REQUIRED` repository calls join it, so a later failure rolls back the whole database unit. Putting separate boundaries only around repository calls allows partial commits.

### 18.5 Follow-up traps

**“I caught the inner exception, so can outer commit?”**

Not necessarily. If a participating transactional interceptor already marked the shared transaction rollback-only, completion rolls back and can raise `UnexpectedRollbackException`.

**“Can `REQUIRES_NEW` always write an audit?”**

Only if the call crosses a proxy, a second connection/resource can be obtained, it does not block on outer locks, and its independent partial commit is semantically correct.

**“Does `SERIALIZABLE` prevent all errors?”**

It provides the strongest successful-history guarantee, often by aborting a transaction. Correct code must handle serialization failures and retry the whole safe unit.

**“Does `@Version` lock the row when read?”**

No. It normally detects stale state during update/delete through a version predicate.

**“Can a checked exception still lead to rollback?”**

Yes, with a matching `rollbackFor`, a globally customized default, explicit rollback-only status, or a resource/provider failure that marks the transaction. But the ordinary project default does not roll back merely because a checked exception escaped.

---

## 19. Final review

### 19.1 One complete mental model

**⭐⭐⭐⭐⭐ MUST KNOW**

```text
CLIENT
  ↓ HTTP request
Spring MVC conversion + validation
  ├── failure before service → 400/other framework response; no DB transaction
  └── valid controller invocation
          ↓
BookController calls injected BookService reference
          ↓
SPRING AOP PROXY
  TransactionInterceptor reads @Transactional
          ↓
PlatformTransactionManager / JpaTransactionManager
  ├── REQUIRED: create or join
  ├── bind/coordinate EntityManager
  └── associate JDBC transaction resource as needed
          ↓
BOOK SERVICE TARGET METHOD
  ├── repository loads Book
  ├── persistence context makes it managed
  ├── Java code checks business rules
  └── Java code mutates managed state
          ↓
HIBERNATE
  dirty checking at flush
          ↓
JDBC / pgJDBC
  SQL through transaction-associated Connection
          ↓
HIKARICP
  bounded physical connection capacity
          ↓
POSTGRESQL
  isolation + MVCC + constraints + locks
          ↓
completion
  ├── success
  │     flush succeeds
  │     COMMIT succeeds
  │     connection returns to pool
  │     service result continues to controller
  │     controller returns HTTP success
  │
  └── failure / rollback-only
        ROLLBACK uncommitted DB work
        persistence context is discarded after failure
        connection returns to pool
        exception propagates/translates
        global advice produces deliberate HTTP error where applicable
```

At no point does `@Transactional` eliminate Hibernate, JDBC, HikariCP, PostgreSQL, or database concurrency. It coordinates them.

### 19.2 Successful managed-update timeline

```text
time →

caller      proxy/TM       persistence context     Hibernate/JDBC       PostgreSQL
  |             |                    |                    |                  |
  | setStock()  |                    |                    |                  |
  |------------>| begin/join T1      |                    |                  |
  |             |------------------->|                    | BEGIN/tx context |
  |             |   target loads Book|------------------->| SELECT           |
  |             |                    |<-------------------|<-----------------|
  |             |                    | Book is managed    |                  |
  |             |   Java mutation    | stock changes      |                  |
  |             |   target returns   |                    |                  |
  |             | commit completion  | dirty check        |                  |
  |             |                    |------------------->| UPDATE           |
  |             |                    |                    |----------------->|
  |             |                    |                    |      SQL succeeds|
  |             |                    |                    | COMMIT---------->|
  |             | release resources  | context ends       | connection return|
  |<------------| return result      |                    |                  |
```

### 19.3 Failing-after-flush timeline

```text
transaction begins
    ↓
Book loaded and managed
    ↓
Java state changed
    ↓
explicit flush
    ↓
UPDATE executes; row lock may be held; still uncommitted
    ↓
RuntimeException escapes
    ↓
transaction interceptor selects rollback
    ↓
PostgreSQL ROLLBACK discards UPDATE
    ↓
connection returned; exception continues
```

### 19.4 Before and after the abstraction

```text
RAW JDBC YOU ALREADY LEARNED

Connection c = dataSource.getConnection()
c.setAutoCommit(false)
try {
    SQL operation A on c
    SQL operation B on c
    c.commit()
} catch (...) {
    c.rollback()
    throw ...
} finally {
    c.close() // return pooled handle
}
```

```text
SPRING + JPA VERSION

caller → transactional proxy
proxy/manager creates or joins transaction
service performs repository/entity operations
Hibernate flushes through transaction-associated connection
proxy/manager commits or rolls back
cleanup returns pooled handle
```

The second is declarative, not magical.

### 19.5 Must-remember rules

1. A transaction is a business unit of database work, not an annotation decorating arbitrary persistence calls.
2. Put the normal boundary on a public service/use-case method called through a Spring-managed proxy.
3. `@Transactional` is metadata; `TransactionInterceptor` and a transaction manager make it active.
4. Self-invocation does not activate a second transaction declaration.
5. `REQUIRED` joins or creates and is the correct default for most use cases.
6. An inner `REQUIRED` failure can mark the shared transaction rollback-only.
7. Catching an exception does not necessarily restore commit ability.
8. Runtime exceptions and `Error` roll back by default; checked exceptions do not under this project's defaults.
9. Java mutation, dirty checking, flush, SQL execution, and commit are different moments.
10. A managed entity normally needs no `save()` after mutation.
11. `save()` does not mean commit; `flush()` does not mean commit.
12. Rollback changes the database outcome, not necessarily the fields of an already-mutated Java object.
13. `REQUIRES_NEW` creates an independent outcome and usually needs another connection.
14. PostgreSQL treats `READ_UNCOMMITTED` as `READ_COMMITTED`.
15. Isolation, optimistic versioning, pessimistic locking, and constraints solve related but distinct problems.
16. `@Version` detects stale updates; it does not lock at read time.
17. `readOnly = true` expresses intent/optimization, not authorization or guaranteed write impossibility.
18. Long transactions hold scarce connections/locks and increase conflicts.
19. A local database transaction cannot undo email, HTTP, filesystem, or external-message side effects.
20. Diagnose from proxy logs through Hibernate SQL to PostgreSQL rows/locks and Hikari pool state.

### 19.6 Practical design checklist

Before writing a transactional method:

- [ ] Can I state the business operation in one sentence?
- [ ] Which database changes must succeed or fail together?
- [ ] Is the boundary on a service method rather than a controller?
- [ ] Will the call enter through a Spring proxy?
- [ ] Is default `REQUIRED` sufficient?
- [ ] Are all participating repositories covered by the same transaction manager/resource?
- [ ] Is the transaction no broader than necessary?
- [ ] Are slow network/user/file operations outside it?
- [ ] Does a checked failure need an explicit narrow rollback rule?
- [ ] Could code catch a failure after rollback-only has been set?
- [ ] Is explicit flush needed for a specific timing reason?
- [ ] Are managed entities mutated directly without ritual `save()` calls?

Before choosing concurrency controls:

- [ ] What exact race or invariant must be protected?
- [ ] Can a PostgreSQL constraint or atomic SQL statement enforce it?
- [ ] Is `@Version` useful for stale-update detection?
- [ ] Is a pessimistic lock truly necessary, and are lock order/timeouts defined?
- [ ] Does stronger isolation require whole-transaction retry?
- [ ] Have PostgreSQL-specific semantics been verified?

Before choosing `REQUIRES_NEW`:

- [ ] Must the inner outcome remain committed if outer work rolls back?
- [ ] Will the call cross a separate Spring proxy?
- [ ] Is the audit/fact still truthful after every outer outcome?
- [ ] Can the pool support the extra simultaneous connection demand?
- [ ] Can inner work block on rows locked by the outer transaction?
- [ ] Would after-commit or an outbox better express the requirement?

Before shipping:

- [ ] Successful commit verified with a fresh PostgreSQL read
- [ ] Runtime rollback verified
- [ ] Checked-exception behavior intentionally verified
- [ ] Flush-then-fail verified to prove `flush ≠ commit`
- [ ] Transaction logs correlate with method boundaries
- [ ] SQL logs correlate with mutation/flush timing
- [ ] Concurrency behavior checked with two independent transactions
- [ ] Pool and lock implications reviewed
- [ ] Verbose/sensitive development logging disabled for production

### 19.7 Compact review sheet

```text
BOUNDARY
    service use case, normally public and proxy-invoked

DEFAULTS
    propagation = REQUIRED
    isolation   = DEFAULT (PostgreSQL normally READ COMMITTED)
    readOnly    = false
    rollback    = RuntimeException + Error, not checked Exception

JPA FLOW
    load → managed → mutate Java state → dirty check → flush → SQL → commit

NOT EQUAL
    mutation ≠ SQL
    save ≠ commit
    flush ≠ commit
    persistence context ≠ database transaction
    HTTP request ≠ transaction

ROLLBACK
    escaping matching failure → rollback
    caught failure may allow commit unless already rollback-only
    inner REQUIRED can mark shared T1 rollback-only
    commit attempt then → UnexpectedRollbackException

PROPAGATION
    REQUIRED     join/create; one shared outcome
    REQUIRES_NEW suspend outer; independent T2; usually extra connection
    SUPPORTS     join or none
    MANDATORY    existing required
    NOT_SUPPORTED suspend and none
    NEVER        fail if transaction exists
    NESTED       savepoint in same T1 when supported; tricky with JPA

ISOLATION ON POSTGRESQL
    READ_UNCOMMITTED = READ_COMMITTED behavior
    READ_COMMITTED   fresh snapshot per statement
    REPEATABLE_READ  stable snapshot; no phantoms; failures possible
    SERIALIZABLE     serializable successful history; SQLSTATE 40001 retry

CONCURRENCY
    constraint/atomic SQL first when suitable
    @Version detects stale write
    pessimistic lock makes conflicting work wait/fail
    isolation governs wider visibility/history

POOL
    transaction uses leased connection capacity
    long transaction = long lease + long locks
    REQUIRES_NEW can need C2 while outer retains C1

EXTERNAL EFFECTS
    DB rollback cannot undo email/HTTP/file/message
    after-commit closes one ordering gap, not crash/delivery gap
    durable outbox is future reliability pattern
```

### 19.8 Final interview questions for retrieval practice

Answer these without looking up the sheet:

1. Walk from a controller call to PostgreSQL commit through every layer.
2. Why is a service method normally the transaction boundary?
3. What is the difference between a logical `REQUIRED` scope and the physical transaction?
4. Why can `UnexpectedRollbackException` appear after an outer method returned normally?
5. Give one case where a checked exception would commit by default.
6. Why can an explicit flush be useful even though it does not commit?
7. When is `save()` unnecessary?
8. Why does self-invocation prevent `REQUIRES_NEW`?
9. How does `REQUIRES_NEW` affect HikariCP with five concurrent outer transactions?
10. What does PostgreSQL do with `READ_UNCOMMITTED`?
11. How do `READ_COMMITTED` and PostgreSQL `REPEATABLE_READ` snapshots differ?
12. Why must a serializable retry rerun the complete transaction?
13. How does `@Version` turn a lost update into a detectable failure?
14. When might `SELECT FOR UPDATE` be justified?
15. What does `readOnly = true` communicate, and what does it not guarantee?
16. Why should a slow HTTP call not occur while a database transaction is open?
17. What problem does an outbox solve that `AFTER_COMMIT` alone does not?
18. Which logs and PostgreSQL views would you use to prove rollback after flush?

### 19.9 Readiness statement

You are ready for the companion exercise when you can explain this sentence precisely:

> A proxied service method defines one business boundary; Spring coordinates it, Hibernate manages entity state and flushes SQL, JDBC and Hikari supply the transaction-associated connection, and PostgreSQL alone decides database visibility, locks, commit, and rollback—while external side effects remain outside that local transaction.

---

## Official references for later lookup

- [Spring Framework — Declarative transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative.html)
- [Spring Framework — Using `@Transactional`](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
- [Spring Framework — Transaction propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)
- [Spring Framework — Rolling back a declarative transaction](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html)
- [Spring Framework — Transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html)
- [Spring Data JPA — Transactionality](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html)
- [Hibernate ORM User Guide — Flushing](https://docs.hibernate.org/orm/7.4/userguide/html_single/Hibernate_User_Guide.html#flushing)
- [Hibernate ORM User Guide — Locking](https://docs.hibernate.org/orm/7.4/userguide/html_single/Hibernate_User_Guide.html#locking)
- [Jakarta Persistence 3.2 specification](https://jakarta.ee/specifications/persistence/3.2/)
- [PostgreSQL 17 — Transaction isolation](https://www.postgresql.org/docs/17/transaction-iso.html)
- [PostgreSQL 17 — Explicit locking](https://www.postgresql.org/docs/17/explicit-locking.html)
- [PostgreSQL 17 — `SET TRANSACTION`](https://www.postgresql.org/docs/17/sql-set-transaction.html)
- [HikariCP documentation](https://github.com/brettwooldridge/HikariCP)
