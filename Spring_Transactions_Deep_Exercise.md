# Spring Transactions — Deep Exercise

## Existing project

This exercise extends the checked-in project at:

~~~text
JPA_Hibernate/book-catalog-api/
~~~

It is the same Book Catalog API used for Spring Data JPA and Hibernate. Do **not** create another Spring Boot application, switch to H2, or replace the existing PostgreSQL/HikariCP stack.

### Verified baseline

The exercise is based on the source that is actually checked in, not only on the earlier guide's idealized reference:

| Baseline fact | Consequence for this exercise |
|---|---|
| Java 21 and Spring Boot 4.1.1 | Keep both; `pom.xml` already has everything needed |
| Spring Framework 7.0.9 and Hibernate ORM 7.4.5.Final resolve through Boot | Do not pin transaction or Hibernate versions manually |
| PostgreSQL 17 runs through `compose.yaml` | Keep the real database; isolation behavior is PostgreSQL behavior |
| HikariCP maximum pool size is 5 | `REQUIRES_NEW` resource usage is visible and important |
| `BookService` has class-level `@Transactional(readOnly = true)` | Existing reads already have a read-only service boundary |
| `create`, `replace`, `setStock`, and `delete` override with `@Transactional` | Existing writes already use read-write `REQUIRED` transactions |
| `setStock` changes a managed entity without calling `save()` | It is already the ideal dirty-checking experiment |
| `create` and `replace` explicitly flush | They already prove that flush timing can be deliberate |
| `spring.jpa.open-in-view` is `false` | The persistence context does not remain open through the web layer |
| `schema.sql` uses `CREATE TABLE IF NOT EXISTS` | Adding `@Version` also requires an `ALTER TABLE` for an existing Docker volume |
| There is no version column or audit table | Add exactly those two transaction-learning pieces |

The current checkout also has unrelated imperfections: `StockRequest` has no validation constraints, the controller has an inaccurate paging-validation message, entity column metadata is sparse, and `BookService` translates integrity failures too broadly. Record those facts, but do not turn this transaction exercise into a cleanup rewrite.

Never print or reproduce values from a real `.env` file.

## What remains stable

All existing Book endpoints keep their current routes and successful response shapes:

| Operation | Existing behavior to preserve |
|---|---|
| `GET /books` | `200` and a JSON array |
| `GET /books/{id}` | `200` and `BookResponse`, or `404` |
| `POST /books` | `201`, `Location`, and `BookResponse` |
| `PUT /books/{id}` | `200` and the replaced representation |
| `PATCH /books/{id}` | `200` and the changed representation |
| `DELETE /books/{id}` | `204` with no body |

The final reference adds one lab route:

~~~text
POST /transaction-lab/transfers
~~~

Its request chooses a controlled scenario. This avoids adding a separate endpoint for every annotation experiment.

## Learning contract

For every task:

1. Read **Objective** and **Concept**.
2. Predict the outcome before running anything.
3. Attempt **What to implement** from the incomplete **Starter code**.
4. Use **Hints** only after making a real attempt.
5. Follow **How to verify** with application logs and PostgreSQL.
6. Explain the result without saying “Spring does magic.”

Every task uses this rhythm:

- **Objective**
- **Concept**
- **What to implement**
- **Starter code**
- **Hints**
- **How to verify**
- **Common mistakes**
- **Explanation**

The complete, cumulative implementation appears only after the stop barrier. Starter fragments are intentionally incomplete and sometimes disposable.

This is a manual learning lab. Use Postman, `curl.exe`, pgAdmin, `psql`, application logs, and a debugger. Do not add JUnit, MockMvc, Testcontainers, Spring Security, Kafka, deployment work, or another database.

## Before architecture

~~~text
HTTP request
    |
BookController
    |
Spring proxy around BookService
    |
TransactionInterceptor
    |
JpaTransactionManager
    |
transaction-bound EntityManager / Persistence Context
    |
Spring Data repository proxy
    |
Hibernate ORM
    |
JDBC Connection from HikariCP
    |
PostgreSQL 17
~~~

The existing `BookService.setStock` path is already:

~~~text
PATCH /books/{id}
  -> controller calls the Spring-managed BookService reference
  -> service proxy starts a read-write transaction
  -> repository loads Book; Book is managed
  -> Java code changes stock
  -> method returns without save()
  -> Hibernate dirty checking during flush
  -> UPDATE executes
  -> transaction commits
  -> connection is returned to HikariCP
~~~

## Target learning architecture

The existing API remains recognizable. The lab adds one orchestrating service, two small participant services, and one tiny audit table:

~~~text
TransactionLabController                  no @Transactional
    |
    v
StockTransferService proxy                outer use-case transaction
    |                         \
    v                          v
BookStockService proxy          TransactionAuditService proxy
Propagation.REQUIRED            REQUIRED or REQUIRES_NEW
    |                          |
BookRepository                 TransactionAuditRepository
    \                          /
     +---- same EntityManager/transaction when REQUIRED ----+
                              |
                         Hibernate / JDBC
                              |
                       HikariCP / PostgreSQL
~~~

`REQUIRES_NEW` is the intentional exception: it suspends the outer transaction and creates an independent transaction that may require another pooled connection.

## Concept priorities

| Priority | Practice in this exercise |
|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | service boundary, proxy interception, managed state, dirty checking, flush versus commit, runtime rollback, `REQUIRED`, atomic multi-operation use case |
| ⭐⭐⭐⭐ IMPORTANT | checked-exception rules, rollback-only state, `REQUIRES_NEW`, connection-pool cost, PostgreSQL `READ COMMITTED`, `@Version` |
| ⭐⭐⭐ NICE TO KNOW | `UnexpectedRollbackException`, repeatable-read experiment, transaction tracing |
| ⭐⭐ FUTURE KNOWLEDGE | retries, outbox/Saga, distributed transactions, production audit design, advanced lock tuning |

## Exercise tasks — attempt before reading the solution

### Task 1 — Trace the current transaction architecture

**Objective**

Trace one existing request from HTTP to PostgreSQL and back, naming which component owns each responsibility.

**Concept**

`@Transactional` is metadata. Spring Core's proxy/AOP infrastructure intercepts an eligible service call; a transaction manager coordinates JPA resources; Hibernate uses JDBC; the `DataSource` and HikariCP supply a connection; PostgreSQL performs the real database transaction.

**What to implement**

Do not edit code. Inspect `BookController`, `BookService`, `BookRepository`, `Book`, `application.yaml`, `schema.sql`, and `pom.xml`. Complete the ownership table and draw both a successful PATCH and a failed PATCH.

**Starter code**

~~~text
Layer/component                       Responsibility
BookController                        TODO
Spring service proxy                  TODO
TransactionInterceptor                TODO
PlatformTransactionManager            TODO: which implementation is likely here?
EntityManager                         TODO
Persistence Context                   TODO
Hibernate ORM                         TODO
JDBC Connection                       TODO
HikariCP                              TODO
PostgreSQL                            TODO
GlobalExceptionHandler                TODO: before or after rollback?
~~~

Complete this flow:

~~~text
Controller
 -> TODO proxy boundary
 -> TODO transaction manager
 -> TODO persistence context
 -> Hibernate
 -> TODO pooled resource
 -> PostgreSQL
~~~

**Hints**

1. The service bean reference injected into the controller is eligible for proxying.
2. Boot auto-configures a `JpaTransactionManager` when this single JPA persistence setup is present.
3. The repository is also a proxy, but the outer service transaction defines the use case.
4. Exception advice formats HTTP errors; it does not perform JDBC rollback.

**How to verify**

Place a breakpoint in `BookService.setStock`. Inspect the runtime class of `bookService` in the controller and the call stack. Run a PATCH, then query the row with pgAdmin or `psql`.

**Common mistakes**

- Calling Spring Data the ORM provider.
- Saying Hibernate replaces JDBC or HikariCP.
- Assuming the HTTP request itself is the database transaction.
- Saying the global exception handler rolls the transaction back.

**Explanation**

The transaction interceptor runs around the service target method. A normal return leads to flush/commit; a matching failure leads to rollback. The resulting exception can then travel to MVC exception handling. By the time `GlobalExceptionHandler` creates a `ProblemDetail`, transaction completion has normally already occurred.

---

### Task 2 — Classify the current `BookService` methods

**Objective**

Decide which existing service methods are read-only and which require a read-write transaction.

**Concept**

A transaction boundary should describe a use case, not merely decorate every repository call. A class-level read-only default plus method-level write overrides is a practical pattern.

**What to implement**

Inspect the current annotations without changing them. Complete the table. Explain why the bare method-level `@Transactional` on a write method overrides the class-level `readOnly = true` declaration.

**Starter code**

| Method | Read-only or read-write? | Why? |
|---|---|---|
| `findById` | TODO | TODO |
| `findAll` | TODO | TODO |
| `create` | TODO | TODO |
| `replace` | TODO | TODO |
| `setStock` | TODO | TODO |
| `delete` | TODO | TODO |

Predict the resolved settings for `setStock`:

~~~text
propagation = TODO
isolation   = TODO
readOnly    = TODO
rollback    = TODO
~~~

**Hints**

1. The most specific method annotation takes precedence over the class annotation.
2. Plain `@Transactional` means `readOnly = false`.
3. Default propagation is `REQUIRED`; default isolation is `DEFAULT`.
4. A read-only flag communicates intent and may enable optimizations; it is not write authorization.

**How to verify**

Enable transaction TRACE logging temporarily or inspect annotation metadata in the debugger. Call one GET and one PATCH and compare transaction names and generated SQL.

**Common mistakes**

- Adding `@Transactional` to the controller.
- Removing the read transaction because “SELECT does not need transactions.”
- Assuming a repository method and service method create nested independent transactions.
- Treating `readOnly = true` as a PostgreSQL permission.

**Explanation**

`findById` and `findAll` inherit the class-level read-only boundary. Create, replace, stock update, and delete use method-level read-write boundaries. Repository calls made inside those methods participate in the surrounding `REQUIRED` transaction.

---

### Task 3 — Observe dirty checking without `save()`

**Objective**

Prove that a managed `Book` can be updated without calling `repository.save(book)`.

**Concept**

Loading an entity inside the service transaction makes it managed in that persistence context. Mutating mapped state changes the Java object. Hibernate detects the difference at flush and generates SQL.

**What to implement**

Use the existing `BookService.setStock`; it already contains the desired implementation. Do not add `save()` and do not add an explicit flush. Add breakpoints or temporary comments identifying each lifecycle moment.

**Starter code**

~~~java
@Transactional
public Book setStock(long id, Integer stock) {
    Book book = repo.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));

    // Moment A: TODO — what is book's entity state?
    book.setStock(stock);
    // Moment B: TODO — has SQL necessarily run?

    return book;
    // Moment C: TODO — who flushes and commits after this line?
}
~~~

Write the sequence from memory:

~~~text
transaction starts
 -> TODO entity load/state
 -> TODO Java mutation
 -> TODO dirty checking
 -> TODO flush/SQL
 -> TODO commit
~~~

**Hints**

1. The entity remains managed until the transaction-bound persistence context ends.
2. A setter call is not JDBC.
3. SQL commonly appears during transaction-completion flush.
4. The proxy completes the transaction after the target method returns.

**How to verify**

Record the current stock, call the existing PATCH endpoint, watch `SELECT` and `UPDATE` SQL, then query PostgreSQL. Remove any experimental `save()` call and prove the update still commits.

**Common mistakes**

- Calling `save()` out of habit for an already-managed entity.
- Saying mutation immediately executed an `UPDATE`.
- Confusing return from the Java method with commit timing.
- Inspecting only the response object instead of PostgreSQL.

**Explanation**

The important chain is:

~~~text
managed Java state changes
    != SQL has executed
dirty checking decides pending work
    != commit
flush executes UPDATE
    != commit
commit makes the database outcome durable
~~~

---

### Task 4 — Prove that flush is not commit

**Objective**

Force Hibernate to execute SQL, then fail before commit and prove the database change disappears.

**Concept**

`flush()` synchronizes the persistence context to the database inside the still-active transaction. PostgreSQL can still roll back that SQL.

**What to implement**

Begin the small stock-transfer lab. Create an outer service scenario that changes both managed books, calls `BookRepository.flush()`, and then throws an unchecked exception. Do not catch the exception inside the transactional method.

**Starter code**

~~~java
@Transactional
public void flushThenFail(long fromId, long toId, int amount) {
    // TODO: decrease one managed Book
    // TODO: increase the other managed Book

    bookRepository.TODO();

    throw new IllegalStateException("Intentional failure after flush");
}
~~~

Before running it, predict:

~~~text
Will UPDATE SQL appear?                    TODO
Will either final stock change?            TODO
Will version values change permanently?    TODO (after Task 13)
Does executed UPDATE SQL prove commit?      TODO
~~~

**Hints**

1. Use `flush()`, not `saveAndFlush()` on each managed book.
2. The runtime exception must escape the proxied public method.
3. Query PostgreSQL using a different session after the request finishes.
4. Treat the transaction-completion log and a fresh query—not the UPDATE line—as outcome evidence.

**How to verify**

Capture the two stocks before the request. Run the scenario. Confirm SQL logs show `UPDATE`, transaction logs show rollback, the HTTP lab request fails, and a fresh SQL query shows both original values.

**Common mistakes**

- Treating visible `UPDATE` SQL as proof of commit.
- Querying from the same uncommitted transaction and misreading visibility.
- Catching the deliberate exception and returning normally.
- Calling `save()` merely to force SQL.

**Explanation**

Expected timeline:

~~~text
BEGIN
 -> load A and B
 -> mutate A and B in Java
 -> FLUSH
 -> UPDATE A; UPDATE B       (SQL has executed)
 -> RuntimeException
 -> ROLLBACK                 (both UPDATEs undone)
~~~

---

### Task 5 — Verify default rollback for an unchecked failure

**Objective**

Change two books and an audit row in one transaction, then prove a runtime failure rolls all three back.

**Concept**

By default, an unchecked `RuntimeException` escaping a Spring transactional proxy marks the transaction for rollback. Every `REQUIRED` participant in the same physical transaction shares that outcome.

**What to implement**

Add a scenario that decreases Book A, increases Book B, records an audit using `REQUIRED`, and then throws `IllegalStateException`. Do not explicitly flush in this scenario; let transaction completion decide whether pending changes need synchronization.

**Starter code**

~~~java
@Transactional
public void changeBothThenRuntimeFailure(
        long fromId, long toId, int amount) {
    stockService.TODO(fromId, amount);
    stockService.TODO(toId, amount);
    auditService.recordRequired("TODO", "TODO");

    throw new IllegalStateException("Intentional runtime failure");
}
~~~

**Hints**

1. Keep transaction orchestration in the outer service.
2. Put stock and audit persistence in separate Spring beans so later propagation experiments cross proxies.
3. The audit table belongs to the same PostgreSQL database; this is not an external-system transaction.
4. The absence of SQL before rollback would not make rollback semantics incorrect; it can mean Hibernate discarded pending work before flushing it.
5. Conversely, the audit's `IDENTITY` ID may require an early INSERT; that SQL is still uncommitted and can roll back.

**How to verify**

Record both books and the audit-row count. Invoke the runtime-failure scenario. Verify the response is the existing sanitized `500`, both stocks are unchanged, and no audit row committed.

**Common mistakes**

- Catching the runtime exception in the transactional method and returning success.
- Giving each operation an independent transaction.
- Expecting the global exception handler to decide rollback.
- Assuming a rolled-back identity value must be reused.

**Explanation**

The two book changes and the required audit are one logical unit because the outer service transaction surrounds all repository work. The service proxy, not the controller advice, observes the escaping runtime exception and requests rollback.

---

### Task 6 — Compare checked and unchecked exception rules

**Objective**

Observe the default difference between `RuntimeException` and a checked exception, then deliberately change the checked-exception rule.

**Concept**

With the normal Spring defaults used by this project:

~~~text
RuntimeException or Error escapes  -> rollback
ordinary checked Exception escapes -> commit unless a rollback rule says otherwise
~~~

The checked-exception behavior is a rule, not a claim that the operation succeeded. Spring Framework can be globally configured differently, so inspect project policy instead of memorizing only folklore. This exercise does not enable a global override.

**What to implement**

Create one small checked exception and two otherwise-equivalent service methods. Both transfer stock, add a `REQUIRED` audit row, and throw the checked exception. Leave one annotation at its default. Add a narrow `rollbackFor` rule to the other.

**Starter code**

~~~java
public class TransactionLabCheckedException extends Exception {
    public TransactionLabCheckedException(String message) {
        super(message);
    }
}
~~~

~~~java
@Transactional
public void checkedFailureDefault(...) throws TransactionLabCheckedException {
    // TODO: modify both books and add required audit
    throw new TransactionLabCheckedException("Intentional checked failure");
}

@Transactional(rollbackFor = TODO.class)
public void checkedFailureWithRollback(...)
        throws TransactionLabCheckedException {
    // TODO: make the same changes
    throw new TransactionLabCheckedException("Intentional checked failure");
}
~~~

Predict both outcomes before running them.

**Hints**

1. The checked exception must escape the public method through the Spring proxy.
2. Use `rollbackFor = TransactionLabCheckedException.class`, not an unexplained `Exception.class` catch-all.
3. Both requests can end as HTTP `500`; HTTP status does not tell you whether the database committed.
4. Reset database state between scenarios.

**How to verify**

Run `CHECKED_DEFAULT_COMMIT`. Verify the request reports an error but the two stock changes and audit row committed. Reset state. Run `CHECKED_ROLLBACK`; verify the same apparent error now leaves no database changes.

**Common mistakes**

- Assuming every thrown exception rolls back.
- Catching the checked exception in the controller and inferring database outcome from the response.
- Applying `rollbackFor = Exception.class` everywhere without a business policy.
- Forgetting that a configured global rollback policy can change defaults.

**Explanation**

The transaction interceptor applies rollback rules after the target throws. With no matching rollback rule, an ordinary checked exception produces a commit attempt; with the narrow `rollbackFor`, it produces rollback. The intentionally surprising “HTTP 500 but committed data” is why exception contracts and rollback policy must be designed together.

---

### Task 7 — Build one atomic multi-repository use case

**Objective**

Make a stock transfer and required audit succeed or fail as one unit.

**Concept**

This is the main reason service-level transaction boundaries matter. A repository method can make one call transactional; the service use case can make **all** participating calls atomic.

**What to implement**

Implement the success scenario:

~~~text
Book A stock -= amount
Book B stock += amount
transaction_audits receives TRANSFER_COMPLETED
~~~

Use `BookRepository` through the stock participant and `TransactionAuditRepository` through the audit participant. Then invoke the same success scenario with a nonexistent destination ID so failure occurs after the source was changed in Java but before the audit call.

**Starter code**

~~~java
@Transactional
public void transferSuccessfully(long fromId, long toId, int amount) {
    validateTransfer(fromId, toId, amount);
    stockService.decrease(TODO);
    stockService.increase(TODO);
    auditService.recordRequired("TRANSFER_COMPLETED", TODO);
}
~~~

~~~text
Valid destination:
  A decreases + B increases + audit inserts -> TODO

Missing destination:
  A changed in managed Java state
  B lookup throws BookNotFoundException
  -> TODO final database state
~~~

**Hints**

1. The outer method owns the use-case transaction.
2. `BookNotFoundException` is a runtime exception.
3. An inner `REQUIRED` call joins the outer transaction rather than committing independently.
4. Do not add a second database or a messaging system to prove atomicity.

**How to verify**

For valid IDs, verify both stocks and one audit row commit. Reset. Use a real source ID and a nonexistent destination ID; verify `404`, unchanged source stock, and no new audit row.

**Common mistakes**

- Putting `@Transactional` on the lab controller.
- Letting decrease and increase execute as separate top-level transactions.
- Treating two calls to `save()` as atomic without an outer boundary.
- Calling a successful required audit “independent.”

**Explanation**

On the failure path, the first Java mutation belongs to the same persistence context and transaction in which the second lookup fails. The escaping runtime exception rolls back the unit. Whether an `UPDATE` was already flushed or remained pending, PostgreSQL ends with neither partial business change nor audit row.

---

### Task 8 — Prove how `Propagation.REQUIRED` joins and marks rollback-only

**Objective**

Observe a separate proxied inner service joining the outer transaction, then see what happens when the inner participant fails and the outer method catches that failure.

**Concept**

`REQUIRED` means:

~~~text
existing transaction? yes -> participate in it
existing transaction? no  -> create one
~~~

An inner `REQUIRED` interceptor can mark the shared transaction rollback-only. Catching the exception outside that inner proxy does not erase the rollback-only marker. If the outer method returns normally, commit can fail with `UnexpectedRollbackException` so the caller is not falsely told that a commit occurred.

**What to implement**

Keep stock participant methods and normal audit recording at `Propagation.REQUIRED`. Add one lab-only audit method that saves and flushes, then throws a runtime exception. In an outer transactional scenario, catch that exception and return normally.

**Starter code**

~~~java
@Transactional(propagation = Propagation.REQUIRED)
public void recordRequiredThenFail(String type, String details) {
    repository.TODO(new TransactionAudit(type, details));
    throw new IllegalStateException("Intentional inner REQUIRED failure");
}
~~~

~~~java
@Transactional
public void catchInnerRequiredFailure(...) {
    moveStock(...);

    try {
        auditService.recordRequiredThenFail("INNER_FAILURE", details(...));
    } catch (IllegalStateException expected) {
        // Predict: is the shared transaction still committable?
    }
}
~~~

Draw the physical transaction count and JDBC connection count.

**Hints**

1. `auditService` must be another Spring bean; the call must cross its proxy.
2. Use `saveAndFlush` in this deliberate lab method so SQL is visible before failure.
3. The inner runtime exception crosses its interceptor, which marks the shared transaction rollback-only.
4. Do not confuse “caught Java exception” with “transaction restored to healthy state.”

**How to verify**

Run `CAUGHT_REQUIRED_INNER_FAILURE`. Look for one physical transaction, an inner “participating” boundary, rollback, and `UnexpectedRollbackException`. Verify neither stock nor audit state committed even though the outer target method caught the original `IllegalStateException`.

**Common mistakes**

- Throwing and catching entirely inside one target method, which does not prove inner proxy participation.
- Expecting `REQUIRED` to create a nested savepoint.
- Swallowing `UnexpectedRollbackException` at the web boundary and reporting success.
- Thinking a caught exception automatically clears rollback-only.

**Explanation**

Timeline:

~~~text
outer proxy starts Tx-1
  -> stock REQUIRED joins Tx-1
  -> audit REQUIRED joins Tx-1
       -> flush SQL
       -> runtime failure crosses inner proxy
       -> Tx-1 marked rollback-only
  -> outer target catches runtime failure
outer target returns normally
outer interceptor attempts commit
  -> sees rollback-only
  -> rolls back Tx-1
  -> throws UnexpectedRollbackException
~~~

---

### Task 9 — Contrast `REQUIRED` with `REQUIRES_NEW`

**Objective**

Persist one truthful transfer-attempt audit independently while the main stock transfer rolls back.

**Concept**

`REQUIRES_NEW` suspends the outer transaction and starts an independent physical transaction. Its commit does not make the outer operation commit. Its resource cost and consistency semantics are both different from `REQUIRED`.

**What to implement**

Add `recordRequiresNew` to the separate audit service. In the outer scenario, first load/change both books, then record `TRANSFER_ATTEMPT` with `REQUIRES_NEW`, then throw a runtime exception from the outer method.

**Starter code**

~~~java
@Transactional(propagation = Propagation.TODO)
public void recordRequiresNew(String type, String details) {
    auditRepository.save(new TransactionAudit(type, details));
}
~~~

~~~java
@Transactional
public void requiresNewAuditThenFail(...) {
    moveStock(...); // outer transaction has already performed SELECTs
    auditService.recordRequiresNew("TRANSFER_ATTEMPT", details(...));
    throw new IllegalStateException("Intentional outer failure");
}
~~~

Predict:

| Item | Expected final outcome |
|---|---|
| Book A | TODO |
| Book B | TODO |
| `TRANSFER_ATTEMPT` audit | TODO |
| Outer JDBC connection during inner call | TODO |
| Inner JDBC connection | TODO |

**Hints**

1. The outer transaction is suspended, not committed.
2. The outer entity manager/resources remain associated with suspended Tx-1.
3. Tx-2 needs its own connection while Tx-1 still retains resources.
4. The current Hikari maximum is five, so concurrent nesting matters.
5. Name the independent event `ATTEMPT`, not `COMPLETED`.

**How to verify**

Run `REQUIRES_NEW_AUDIT`. Confirm the HTTP request fails and both book rows/versions remain unchanged, but exactly one `TRANSFER_ATTEMPT` row persists. Correlate transaction logs with two transaction lifecycles.

**Common mistakes**

- Calling a `REQUIRES_NEW` method by `this.recordRequiresNew(...)`.
- Assuming transaction suspension releases every outer resource.
- Using `REQUIRES_NEW` everywhere for “safety.”
- Increasing the pool size without first fixing transaction design.
- Calling the audit row proof of atomicity with the outer transaction.

**Explanation**

~~~text
Tx-1 begins; connection A is used for book reads
  -> Tx-1 suspended; connection A remains occupied/bound
  -> Tx-2 begins; connection B writes audit
  -> Tx-2 commits; connection B returns
Tx-1 resumes
  -> runtime failure
  -> Tx-1 rolls back; connection A returns
~~~

At high concurrency, several requests can each hold an outer connection while waiting for an inner one. With a pool of five, five such outer transactions can consume all five connections. This is why `REQUIRES_NEW` is a deliberate exception, not a routine logging annotation.

---

### Task 10 — Reproduce the self-invocation problem

**Objective**

Prove that a same-instance call does not pass through the ordinary Spring transaction proxy, then apply a simple correction.

**Concept**

The proxy intercepts calls entering through the bean reference. `this.inner()` calls the target object directly. A public annotation on `inner` does not force the call to leave the object and re-enter its proxy.

**What to implement**

Use this as a disposable experiment in the lab service. Add a nontransactional outer method that calls an annotated inner method in the same class. The inner method loads through the repository and then mutates the returned book. Expose it temporarily or invoke it from the debugger. Verify it does not persist. Then remove it and use one of the two practical corrections:

1. Put `@Transactional` on the externally invoked outer use-case method; or
2. Move the independently transactional operation to another bean and call that bean.

The cumulative reference uses both sound patterns and keeps the broken probe out of final source.

**Starter code**

~~~java
public Book brokenOuter(long id, int stock) {
    return innerTransactionalUpdate(id, stock); // same object
}

@Transactional
public Book innerTransactionalUpdate(long id, int stock) {
    Book book = bookRepository.findById(id).orElseThrow(...);
    book.setStock(stock);
    return book;
}
~~~

Predict:

~~~text
Does inner call cross StockTransferService proxy?  TODO
Does inner @Transactional start a service transaction? TODO
What transaction may repository.findById create?   TODO
Is Book still managed when setStock runs?           TODO
Can returned Java state differ from database state? TODO
~~~

**Hints**

1. The repository proxy can run `findById` in its own read-only transaction when no outer transaction exists.
2. That repository transaction ends before `findById` returns to the caller; with OSIV disabled, the entity is then detached.
3. Do not solve this with self-injection or `AopContext.currentProxy()` in this beginner project.
4. The final transfer orchestrator is externally invoked and calls separate proxied participant beans.

**How to verify**

Call the broken outer method, observe the returned object's changed stock in the debugger, and then issue a fresh GET or SQL query. The database should remain unchanged. Add `@Transactional` to the outer method and repeat, or delegate to existing proxied `BookService.setStock`; the update should now commit.

**Common mistakes**

- Believing “public” alone fixes a same-object call.
- Annotating a private method and expecting proxy interception.
- Constructing the service with `new`.
- keeping intentionally broken lab code in the final application.

**Explanation**

~~~text
Correct external call:
controller -> Spring proxy -> transaction advice -> target method

Broken self-call:
target outer -> this.inner -> target method directly
                          (no second proxy interception)
~~~

Design the transaction at the public use-case boundary. Extract a second bean only when it represents a real separate responsibility or truly needs independent propagation.

---

### Task 11 — Use and explain read-only transactions

**Objective**

Retain read-only intent on queries without treating it as a write prohibition.

**Concept**

`@Transactional(readOnly = true)` can let Spring, Hibernate, and the JDBC layer apply hints or optimizations. It is not database authorization and not a portable guarantee that SQL writes are impossible.

**What to implement**

Keep the existing class-level default and write-method overrides. Do not create a “test” that intentionally corrupts data inside a read-only method. Instead, inspect resolved transaction attributes and explain what each layer may do.

**Starter code**

~~~java
@Service
@Transactional(readOnly = true)
public class BookService {

    public Book findById(long id) {
        // TODO: read-only use case
    }

    @Transactional
    public Book create(...) {
        // TODO: method-level read-write override
    }
}
~~~

Complete these statements:

~~~text
Intent communicated:                          TODO
Possible Hibernate optimization:              TODO
Database privilege changed?                   TODO
Guaranteed exception on every attempted write? TODO
Correct place for a real write guard:          TODO
~~~

**Hints**

1. Spring can propagate read-only state to transaction resources.
2. Hibernate may reduce normal dirty-checking/flush work for loaded state.
3. Actual behavior depends on transaction manager, ORM, driver, and database.
4. Database roles and `GRANT`/`REVOKE` enforce authorization.

**How to verify**

Call `GET /books/{id}` and inspect transaction logs. Confirm the class-level attribute is read-only. Call POST/PATCH and confirm the method-level annotation resolves as read-write. Explain the difference without claiming that the read method makes PostgreSQL immutable.

**Common mistakes**

- Treating read-only as security.
- Omitting write overrides under a read-only class default.
- Expecting the exact same optimization from every provider/database.
- Mutating an entity in a read-only method and relying on undefined/implementation-specific persistence behavior.

**Explanation**

The pattern clearly states the normal policy while keeping annotations small. It can reduce accidental work and help reviewers, but correctness still comes from method design, validation/business rules, transaction policy, database constraints, and database privileges.

---

### Task 12 — Run a PostgreSQL isolation/concurrency experiment

**Objective**

Observe PostgreSQL `READ COMMITTED` and `REPEATABLE READ` with two real concurrent transactions.

**Concept**

Spring's `Isolation` enum requests a level when a **new** transaction begins. PostgreSQL defines the behavior. In PostgreSQL 17:

- `READ COMMITTED` uses a new statement snapshot, so two SELECTs in one transaction can see different committed values.
- `REPEATABLE READ` uses one transaction snapshot, so the second SELECT sees the earlier snapshot; PostgreSQL also prevents phantoms at this level.
- requested `READ UNCOMMITTED` behaves as `READ COMMITTED`.
- concurrent writes at stronger levels can fail and require retry of the whole transaction.

**What to implement**

Do not add a permanent sleeping endpoint. Open two pgAdmin query windows or two `psql` sessions against the same database. Choose one existing book ID and run the timelines below. Restore the original value afterward.

For application code, write—but do not need to keep—two method signatures showing how Spring would request the levels:

~~~java
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public StockObservation observeReadCommitted(long id) { /* two scalar reads */ }

@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public StockObservation observeRepeatableRead(long id) { /* two scalar reads */ }
~~~

Use scalar queries or clear the persistence context between reads. Calling `findById` twice can return the same managed object from the first-level cache and hide the database isolation behavior.

**Starter code**

Session A — first experiment:

~~~sql
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT stock FROM books WHERE id = <BOOK_ID>; -- A1
-- pause here
SELECT stock FROM books WHERE id = <BOOK_ID>; -- A2
COMMIT;
~~~

Session B, between A1 and A2:

~~~sql
BEGIN;
UPDATE books SET stock = stock + 1 WHERE id = <BOOK_ID>;
COMMIT;
~~~

Repeat after restoring stock, changing Session A to:

~~~sql
BEGIN ISOLATION LEVEL REPEATABLE READ;
~~~

Complete the timeline:

| Level | A1 | B commits a new stock | A2 |
|---|---:|---:|---:|
| `READ COMMITTED` | 10 | 11 | TODO |
| `REPEATABLE READ` | 10 | 11 | TODO |

**Hints**

1. Do not run Session B before Session A's first SELECT.
2. A new transaction is required when switching isolation levels.
3. After A commits, a new transaction sees B's value at either level.
4. If you use the application for Session B after adding `@Version`, prefer the normal PATCH; raw SQL that ignores `version` bypasses Hibernate's optimistic-lock check.

**How to verify**

At `READ COMMITTED`, A2 should see B's committed value. At `REPEATABLE READ`, A2 should retain A1's snapshot, while a new transaction after A commits sees B's value. Optionally attempt an update from the stale repeatable-read transaction and observe PostgreSQL's concurrent-update/serialization failure.

**Common mistakes**

- Generalizing PostgreSQL's guarantees to every database.
- Calling `findById` twice and measuring the JPA first-level cache instead of isolation.
- Changing isolation on an inner `REQUIRED` method and expecting it to replace the outer transaction's level.
- Keeping an artificial sleep or debugger pause in production code.

**Explanation**

Isolation, persistence-context identity, and locking are separate layers that interact. A managed entity cache can make repeated Java reads look stable even when two scalar SQL statements at `READ COMMITTED` would see different snapshots. Test the layer you intend to study.

---

### Task 13 — Add optimistic locking with `@Version`

**Objective**

Detect a stale Book update instead of silently overwriting a newer committed value.

**Concept**

Hibernate includes the loaded version in the update predicate and increments it on a successful update:

~~~sql
UPDATE books
SET ..., stock = ?, version = ?
WHERE id = ? AND version = ?;
~~~

If another transaction already changed the row, the stale predicate updates zero rows. Hibernate raises an optimistic-lock failure during flush/commit.

**What to implement**

Add a nullable Java `Long` field annotated `@Version` and a getter to `Book`. Add a non-null `BIGINT` version column with default zero. Because the current named Docker volume may already contain `books`, include an idempotent `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` in `schema.sql`; editing only the original `CREATE TABLE IF NOT EXISTS` is insufficient.

**Starter code**

~~~java
@Version
@Column(name = "version", nullable = false)
private TODO version;
~~~

~~~sql
ALTER TABLE books
ADD COLUMN IF NOT EXISTS version TODO NOT NULL DEFAULT TODO;
~~~

Simulate this timeline with the existing PATCH endpoint and a debugger breakpoint immediately after `BookService.setStock` loads the entity:

~~~text
Request A / Tx-A                     Request B / Tx-B
load Book version=0
pause after load
                                     load Book version=0
                                     change stock
                                     UPDATE ... WHERE version=0
                                     COMMIT -> version=1
resume; change stale entity
UPDATE ... WHERE version=0 -> 0 rows
optimistic locking failure; ROLLBACK
~~~

**Hints**

1. Suspend only request A's thread; disable the breakpoint before sending B.
2. Do not accept the version from the client for this debugger experiment.
3. The exception normally appears at flush or commit, not at the setter.
4. The current global catch-all will sanitize it as `500`; a production API might deliberately translate a stale-write conflict to `409`, but that HTTP change is outside this minimal exercise.
5. Do not continue using a persistence context after its transaction fails.

**How to verify**

Query `id`, `stock`, and `version` before the race. Let B commit, then resume A. Confirm one request wins, one fails, final stock equals B's value, and the version increments exactly once.

**Common mistakes**

- Adding `@Version` without changing the real existing schema.
- Using raw SQL as the competing writer without also managing `version`.
- Treating optimistic locking as a database row lock.
- Retrying only the final UPDATE instead of re-running the complete use case with fresh state.

**Explanation**

`@Version` does not prevent both transactions from reading. It detects that one transaction's assumptions became stale before its write could commit. That is often appropriate for ordinary catalog updates where conflicts are possible but not constant.

---

### Task 14 — Correlate transaction logs, SQL, flush, commit, and rollback

**Objective**

Build a debugging record that separates Spring transaction events from Hibernate SQL events.

**Concept**

One logger cannot reveal every layer. Transaction logs show interception and completion; Hibernate SQL logs show statements; PostgreSQL queries show durable state.

**What to implement**

Add development-only transaction logging while retaining SQL logging. Correct the Hibernate property spelling to `format_sql`. Do not enable bind-value TRACE logging in shared or production environments.

**Starter code**

~~~yaml
spring:
  jpa:
    properties:
      hibernate:
        format_sql: TODO

logging:
  level:
    org.springframework.transaction: TODO
    org.springframework.orm.jpa.JpaTransactionManager: TODO
    org.hibernate.SQL: TODO
~~~

Build a log worksheet:

| Scenario | Begin/new or participate | SQL before finish | Commit or rollback | PostgreSQL final state |
|---|---|---|---|---|
| Success | TODO | TODO | TODO | TODO |
| Flush then runtime failure | TODO | TODO | TODO | TODO |
| Checked default | TODO | TODO | TODO | TODO |
| Caught inner REQUIRED | TODO | TODO | TODO | TODO |
| REQUIRES_NEW audit | TODO | TODO | TODO | TODO |

**Hints**

1. `org.springframework.transaction: TRACE` reveals annotation/interceptor decisions but is noisy.
2. `JpaTransactionManager` DEBUG helps show create, participate, suspend/resume, commit, and rollback behavior.
3. `org.hibernate.SQL: DEBUG` shows SQL text, not durable commit by itself.
4. Log order across concurrent threads can interleave; include thread names/timestamps when reading it.

**How to verify**

Run each lab scenario once from a clean state. Save the relevant log slice and pair it with a fresh PostgreSQL query. Point to the exact evidence for transaction begin, flush/SQL, commit or rollback, and final row state.

**Common mistakes**

- Inferring commit from an `UPDATE` log line.
- Enabling very verbose transaction/bind logging permanently in production.
- Reading interleaved requests as one transaction.
- Assuming absence of early SQL means no transaction existed.

**Explanation**

The most reliable diagnosis triangulates three kinds of evidence:

~~~text
application code/breakpoint   -> intended control flow and Java state
Spring + Hibernate logs       -> boundary decisions and SQL timing
fresh PostgreSQL query        -> committed durable result
~~~

---

### Task 15 — Perform final database verification

**Objective**

Prove every expected commit and rollback from PostgreSQL rather than from response bodies alone.

**Concept**

An HTTP failure can accompany either rollback or commit, depending on rollback rules. An SQL log can accompany a later rollback. The database's committed state is the final evidence.

**What to implement**

Create a result sheet for all scenarios. Reset the two practice books and clear the lab audit table between scenarios. Record book stock, version, audit rows, response status, and log outcome.

**Starter code**

~~~sql
SELECT id, isbn, stock, version
FROM books
ORDER BY id;

SELECT id, event_type, details, created_at
FROM transaction_audits
ORDER BY id;

SELECT pid,
       state,
       xact_start,
       wait_event_type,
       wait_event,
       query
FROM pg_stat_activity
WHERE datname = current_database();
~~~

Complete the matrix:

| Scenario | Books committed? | Required audit committed? | Independent audit committed? |
|---|---|---|---|
| `SUCCESS_REQUIRED` | TODO | TODO | n/a |
| Missing destination | TODO | TODO | n/a |
| `RUNTIME_ROLLBACK` | TODO | TODO | n/a |
| `FLUSH_THEN_RUNTIME_ROLLBACK` | TODO | TODO | n/a |
| `CHECKED_DEFAULT_COMMIT` | TODO | TODO | n/a |
| `CHECKED_ROLLBACK` | TODO | TODO | n/a |
| `CAUGHT_REQUIRED_INNER_FAILURE` | TODO | TODO | n/a |
| `REQUIRES_NEW_AUDIT` | TODO | n/a | TODO |
| Optimistic race | exactly one | n/a | n/a |

**Hints**

1. Use a new query/transaction for verification.
2. Reset `version` along with stock only in this controlled lab reset.
3. Identity gaps are normal after rolled-back inserts.
4. `pg_stat_activity` is a moment-in-time observation, not a historical transaction log.

**How to verify**

Every row in the completed matrix must be supported by an HTTP result, a transaction-log slice, and a fresh SQL result. If any disagree, stop and diagnose before reading the solution.

**Common mistakes**

- Trusting only Postman output.
- Assuming `500` always means rollback.
- Mistaking a missing identity number for a committed row.
- Forgetting to reset state between experiments.

**Explanation**

The exercise is complete only when you can predict the result before running it and explain the result across all layers:

~~~text
Java call
 -> proxy interception
 -> transaction attributes
 -> persistence-context state
 -> flush / SQL
 -> JDBC commit or rollback
 -> Hikari connection return
 -> PostgreSQL committed state
~~~

## Manual verification setup

1. From `JPA_Hibernate/book-catalog-api`, start PostgreSQL with `docker compose up -d`.
2. Start the application with `mvn spring-boot:run`.
3. Create two books through the existing `POST /books` endpoint, each with stock `10`, or select two existing safe practice rows.
4. Record their IDs as `A` and `B`.
5. Before each scenario, use a controlled reset in `psql` or pgAdmin:

~~~sql
UPDATE books
SET stock = 10,
    version = 0
WHERE id IN (<A>, <B>);

DELETE FROM transaction_audits;
~~~

The version reset is only a deterministic learning convenience. Normal application code must never reset optimistic-lock versions.

Example lab request body:

~~~json
{
  "fromBookId": 1,
  "toBookId": 2,
  "amount": 3,
  "scenario": "SUCCESS_REQUIRED"
}
~~~

Use your actual IDs. Valid scenario names are revealed by your implementation after you attempt the tasks.

==================================================
STOP — ATTEMPT THE EXERCISE BEFORE READING SOLUTION
===================================================

Do not continue until you have written predictions for Tasks 1–15 and attempted the implementation.

## Complete reference implementation

### Selected approach

The reference keeps the normal Book API unchanged and adds one explicitly educational controller. One request DTO selects a scenario; the controller dispatches directly to a separately annotated public service method so every transaction attribute is reached through the Spring proxy.

The design uses:

- `StockTransferService` for the outer use-case boundary;
- `BookStockService` as a separate `REQUIRED` participant;
- `TransactionAuditService` as a separate `REQUIRED`/`REQUIRES_NEW` participant;
- one `TransactionAudit` entity and table;
- one `@Version` column on `Book`;
- one lab endpoint, not one endpoint per concept.

The intentionally broken self-invocation snippet and the two-session isolation script are disposable experiments. They are not kept in the final source.

### Exact change manifest

Paths below are relative to `JPA_Hibernate/book-catalog-api`.

#### Existing files modified

| File | Why |
|---|---|
| `src/main/java/com/example/bookcatalog/model/Book.java` | Add the optimistic-lock version field and getter |
| `src/main/resources/schema.sql` | Upgrade existing `books` tables with `version` and create the one audit table |
| `src/main/resources/application.yaml` | Correct `format_sql` and add development transaction logging |

#### Files added

| File | Why |
|---|---|
| `src/main/java/com/example/bookcatalog/dto/StockTransferRequest.java` | One validated request selects a controlled transaction scenario |
| `src/main/java/com/example/bookcatalog/exception/TransactionLabCheckedException.java` | Narrow checked-exception rollback experiment |
| `src/main/java/com/example/bookcatalog/model/TransactionAudit.java` | Minimal local audit record |
| `src/main/java/com/example/bookcatalog/repository/TransactionAuditRepository.java` | Spring Data persistence for the audit entity |
| `src/main/java/com/example/bookcatalog/service/BookStockService.java` | Separate proxy boundary for inner `REQUIRED` work |
| `src/main/java/com/example/bookcatalog/service/TransactionAuditService.java` | Separate proxy boundary for `REQUIRED` and `REQUIRES_NEW` |
| `src/main/java/com/example/bookcatalog/service/StockTransferService.java` | Outer use cases and rollback experiments |
| `src/main/java/com/example/bookcatalog/web/TransactionLabController.java` | One additive manual-lab endpoint |

#### Existing files that remain unchanged

| File or area | Reason |
|---|---|
| `pom.xml` | Data JPA already brings Spring transaction, ORM, JDBC, and Hikari infrastructure |
| `compose.yaml` | PostgreSQL 17 setup remains correct |
| `.env`, `.env.example` | No credential/configuration change belongs here |
| `src/main/java/com/example/bookcatalog/Application.java` | Existing package scan discovers every added component/entity/repository |
| `dto/BookRequest.java`, `dto/BookResponse.java`, `dto/StockRequest.java` | Existing Book REST contract stays unchanged |
| `exception/BookNotFoundException.java`, `exception/DuplicateIsbnException.java` | Existing meanings and handlers remain unchanged |
| `repository/BookRepository.java` | Existing `JpaRepository` operations are enough |
| `service/BookService.java` | It already has the correct read-only default, write overrides, flush examples, and dirty-checking update |
| `web/BookController.java` | Existing routes and responses remain unchanged |
| `web/GlobalExceptionHandler.java` | Existing errors still work; lab failures use its sanitized catch-all |
| `src/test/java/com/example/AppTest.java` | This remains a manual transaction lab, not a testing-framework exercise |
| Existing Markdown guides | Learning history is not application source |
| `target/` | Build output may regenerate but is not source to edit |

No existing file is removed. No migration framework, security layer, message broker, second database, or test framework is introduced.

### Final source tree additions

~~~text
src/main/java/com/example/bookcatalog/
├── dto/
│   └── StockTransferRequest.java                 ADD
├── exception/
│   └── TransactionLabCheckedException.java       ADD
├── model/
│   ├── Book.java                                 MODIFY
│   └── TransactionAudit.java                     ADD
├── repository/
│   └── TransactionAuditRepository.java           ADD
├── service/
│   ├── BookStockService.java                     ADD
│   ├── StockTransferService.java                 ADD
│   └── TransactionAuditService.java              ADD
└── web/
    └── TransactionLabController.java             ADD

src/main/resources/
├── application.yaml                              MODIFY
└── schema.sql                                    MODIFY
~~~

### `src/main/java/com/example/bookcatalog/model/Book.java` — MODIFY

~~~java
package com.example.bookcatalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String isbn;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private BigDecimal price;

    @Column
    private Integer stock;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Book() {
    }

    public Book(
            String isbn,
            String title,
            String author,
            BigDecimal price,
            Integer stock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public void replace(
            String isbn,
            String title,
            String author,
            BigDecimal price,
            Integer stock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public Long getVersion() {
        return version;
    }
}
~~~

Why this is the only entity change:

- `@Version` tells Hibernate to use optimistic version checking.
- A wrapper `Long` lets a new transient entity begin without application-assigned version state; Hibernate initializes the numeric version.
- The version does not need to enter `BookResponse` to protect writes.
- Existing constructors, accessors, controller mapping, and business methods remain compatible.
- Sparse pre-existing `@Column` metadata is not silently refactored during the transaction lesson.

### `src/main/resources/schema.sql` — MODIFY

~~~sql
CREATE TABLE IF NOT EXISTS books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(120) UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(120) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    stock INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE books
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS transaction_audits (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    details VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
~~~

Both the fresh-create definition and the `ALTER TABLE` are intentional:

~~~text
new empty database       -> CREATE includes version
existing named volume    -> CREATE does nothing; ALTER adds version
later restarts           -> ALTER IF NOT EXISTS is a no-op
~~~

This is a constrained learning-project technique. Production schema evolution belongs to a later migration-tool topic.

### `src/main/resources/application.yaml` — MODIFY

~~~yaml
spring:
  application:
    name: book-catalog-api
  datasource:
    url: jdbc:postgresql://127.0.0.1:5433/book_catalog
    username: book_app
    password: replace-with-a-local-practice-password
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
  sql:
    init:
      mode: always
  jackson:
    deserialization:
      fail-on-unknown-properties: true
      accept-float-as-int: false
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.springframework.transaction: TRACE
    org.springframework.orm.jpa.JpaTransactionManager: DEBUG
    org.hibernate.SQL: DEBUG

server:
  address: 127.0.0.1
  port: 8080
~~~

The datasource, credentials approach, Hikari 5/2 limits, SQL initialization, Jackson behavior, OSIV choice, schema validation, and server binding are preserved. Transaction TRACE/DEBUG is for this local exercise; reduce it after the lab. Bind-value logging is deliberately absent.

### `src/main/java/com/example/bookcatalog/dto/StockTransferRequest.java` — ADD

~~~java
package com.example.bookcatalog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockTransferRequest(
        @NotNull(message = "fromBookId must not be null")
        @Positive(message = "fromBookId must be positive")
        Long fromBookId,

        @NotNull(message = "toBookId must not be null")
        @Positive(message = "toBookId must be positive")
        Long toBookId,

        @NotNull(message = "amount must not be null")
        @Positive(message = "amount must be positive")
        Integer amount,

        @NotNull(message = "scenario must not be null")
        Scenario scenario) {

    public enum Scenario {
        SUCCESS_REQUIRED,
        RUNTIME_ROLLBACK,
        FLUSH_THEN_RUNTIME_ROLLBACK,
        CHECKED_DEFAULT_COMMIT,
        CHECKED_ROLLBACK,
        CAUGHT_REQUIRED_INNER_FAILURE,
        REQUIRES_NEW_AUDIT
    }
}
~~~

The enum keeps one endpoint deterministic. It is explicitly a learning control, not a pattern for a normal business request DTO.

### `src/main/java/com/example/bookcatalog/exception/TransactionLabCheckedException.java` — ADD

~~~java
package com.example.bookcatalog.exception;

public class TransactionLabCheckedException extends Exception {

    public TransactionLabCheckedException(String message) {
        super(message);
    }
}
~~~

This exception exists only to make the default checked-exception rule observable. The reference deliberately names the exact type in `rollbackFor`.

### `src/main/java/com/example/bookcatalog/model/TransactionAudit.java` — ADD

~~~java
package com.example.bookcatalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "transaction_audits")
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "details", nullable = false, length = 500)
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransactionAudit() {
    }

    public TransactionAudit(
            String eventType,
            String details,
            Instant createdAt) {
        this.eventType = eventType;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
~~~

This is the only new entity. It is intentionally independent of `Book`: no relationship mapping, cascade, or foreign key is needed to learn propagation.

### `src/main/java/com/example/bookcatalog/repository/TransactionAuditRepository.java` — ADD

~~~java
package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionAuditRepository
        extends JpaRepository<TransactionAudit, Long> {
}
~~~

Spring Data supplies the implementation proxy. No custom query is required.

### `src/main/java/com/example/bookcatalog/service/BookStockService.java` — ADD

~~~java
package com.example.bookcatalog.service;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookStockService {

    private final BookRepository bookRepository;

    public BookStockService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void decrease(long bookId, int amount) {
        requirePositiveAmount(amount);
        Book book = requiredBook(bookId);

        if (book.getStock() < amount) {
            throw new IllegalStateException(
                    "Insufficient stock for transaction exercise");
        }

        book.setStock(book.getStock() - amount);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void increase(long bookId, int amount) {
        requirePositiveAmount(amount);
        Book book = requiredBook(bookId);
        book.setStock(Math.addExact(book.getStock(), amount));
    }

    private Book requiredBook(long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private static void requirePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
~~~

Why a separate service is justified here:

- calls from `StockTransferService` cross a real Spring proxy;
- `REQUIRED` participation can be observed rather than merely described;
- both loaded `Book` objects remain managed in the outer persistence context;
- no `save()` is needed after either stock mutation.

Outside this learning lab, do not split every small mutation into a service solely to collect annotations.

### `src/main/java/com/example/bookcatalog/service/TransactionAuditService.java` — ADD

~~~java
package com.example.bookcatalog.service;

import com.example.bookcatalog.model.TransactionAudit;
import com.example.bookcatalog.repository.TransactionAuditRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionAuditService {

    private final TransactionAuditRepository auditRepository;

    public TransactionAuditService(
            TransactionAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordRequired(String eventType, String details) {
        auditRepository.save(new TransactionAudit(
                eventType,
                details,
                Instant.now()));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordRequiredThenFail(String eventType, String details) {
        auditRepository.saveAndFlush(new TransactionAudit(
                eventType,
                details,
                Instant.now()));

        throw new IllegalStateException(
                "Intentional inner REQUIRED failure");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRequiresNew(String eventType, String details) {
        auditRepository.saveAndFlush(new TransactionAudit(
                eventType,
                details,
                Instant.now()));
    }
}
~~~

The separate bean is essential: same-class calls would not apply independent propagation in normal proxy mode. The explicit flush in the failure method makes the “SQL executed, then rollback” evidence visible. The `REQUIRES_NEW` flush is followed by its own commit when the method returns.

### `src/main/java/com/example/bookcatalog/service/StockTransferService.java` — ADD

~~~java
package com.example.bookcatalog.service;

import com.example.bookcatalog.exception.TransactionLabCheckedException;
import com.example.bookcatalog.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockTransferService {

    private final BookStockService stockService;
    private final TransactionAuditService auditService;
    private final BookRepository bookRepository;

    public StockTransferService(
            BookStockService stockService,
            TransactionAuditService auditService,
            BookRepository bookRepository) {
        this.stockService = stockService;
        this.auditService = auditService;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void transferSuccessfully(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);
        auditService.recordRequired(
                "TRANSFER_COMPLETED",
                details(fromBookId, toBookId, amount));
    }

    @Transactional
    public void transferThenRuntimeRollback(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);
        auditService.recordRequired(
                "RUNTIME_FAILURE_WILL_ROLL_BACK",
                details(fromBookId, toBookId, amount));

        throw new IllegalStateException(
                "Intentional runtime failure after both changes");
    }

    @Transactional
    public void flushThenRuntimeRollback(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);
        bookRepository.flush();

        throw new IllegalStateException(
                "Intentional runtime failure after flush");
    }

    @Transactional
    public void checkedFailureDefault(
            long fromBookId,
            long toBookId,
            int amount) throws TransactionLabCheckedException {
        moveStock(fromBookId, toBookId, amount);
        auditService.recordRequired(
                "CHECKED_DEFAULT_COMMITTED",
                details(fromBookId, toBookId, amount));

        throw new TransactionLabCheckedException(
                "Intentional checked failure with default rules");
    }

    @Transactional(rollbackFor = TransactionLabCheckedException.class)
    public void checkedFailureWithRollback(
            long fromBookId,
            long toBookId,
            int amount) throws TransactionLabCheckedException {
        moveStock(fromBookId, toBookId, amount);
        auditService.recordRequired(
                "CHECKED_FAILURE_WILL_ROLL_BACK",
                details(fromBookId, toBookId, amount));

        throw new TransactionLabCheckedException(
                "Intentional checked failure with rollbackFor");
    }

    @Transactional
    public void catchInnerRequiredFailure(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);

        try {
            auditService.recordRequiredThenFail(
                    "INNER_REQUIRED_FAILURE",
                    details(fromBookId, toBookId, amount));
        } catch (IllegalStateException expected) {
            // The inner REQUIRED interceptor has already marked the shared
            // transaction rollback-only. Catching does not clear that flag.
        }
    }

    @Transactional
    public void requiresNewAuditThenFail(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);

        auditService.recordRequiresNew(
                "TRANSFER_ATTEMPT",
                details(fromBookId, toBookId, amount));

        throw new IllegalStateException(
                "Intentional outer failure after REQUIRES_NEW audit");
    }

    private void moveStock(
            long fromBookId,
            long toBookId,
            int amount) {
        validateTransfer(fromBookId, toBookId, amount);
        stockService.decrease(fromBookId, amount);
        stockService.increase(toBookId, amount);
    }

    private static void validateTransfer(
            long fromBookId,
            long toBookId,
            int amount) {
        if (fromBookId == toBookId) {
            throw new IllegalArgumentException(
                    "source and destination books must differ");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    private static String details(
            long fromBookId,
            long toBookId,
            int amount) {
        return "fromBookId=%d, toBookId=%d, amount=%d"
                .formatted(fromBookId, toBookId, amount);
    }
}
~~~

Every annotated public method is called directly through the `StockTransferService` proxy. The private `moveStock` helper does not require its own transaction annotation: it runs inside the already-established public use-case boundary. Its calls to the two participant services cross their proxies.

### `src/main/java/com/example/bookcatalog/web/TransactionLabController.java` — ADD

~~~java
package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.StockTransferRequest;
import com.example.bookcatalog.exception.TransactionLabCheckedException;
import com.example.bookcatalog.service.StockTransferService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction-lab")
public class TransactionLabController {

    private final StockTransferService stockTransferService;

    public TransactionLabController(
            StockTransferService stockTransferService) {
        this.stockTransferService = stockTransferService;
    }

    @PostMapping(
            path = "/transfers",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> runTransferScenario(
            @Valid @RequestBody StockTransferRequest request)
            throws TransactionLabCheckedException {
        long fromBookId = request.fromBookId();
        long toBookId = request.toBookId();
        int amount = request.amount();

        switch (request.scenario()) {
            case SUCCESS_REQUIRED ->
                    stockTransferService.transferSuccessfully(
                            fromBookId, toBookId, amount);
            case RUNTIME_ROLLBACK ->
                    stockTransferService.transferThenRuntimeRollback(
                            fromBookId, toBookId, amount);
            case FLUSH_THEN_RUNTIME_ROLLBACK ->
                    stockTransferService.flushThenRuntimeRollback(
                            fromBookId, toBookId, amount);
            case CHECKED_DEFAULT_COMMIT ->
                    stockTransferService.checkedFailureDefault(
                            fromBookId, toBookId, amount);
            case CHECKED_ROLLBACK ->
                    stockTransferService.checkedFailureWithRollback(
                            fromBookId, toBookId, amount);
            case CAUGHT_REQUIRED_INNER_FAILURE ->
                    stockTransferService.catchInnerRequiredFailure(
                            fromBookId, toBookId, amount);
            case REQUIRES_NEW_AUDIT ->
                    stockTransferService.requiresNewAuditThenFail(
                            fromBookId, toBookId, amount);
        }

        return ResponseEntity.noContent().build();
    }
}
~~~

There is deliberately no `@Transactional` on this controller. It chooses a use case, calls the proxied service, and translates successful completion to `204`. Failed scenarios escape to the existing global exception layer.

### Compile and start

From `JPA_Hibernate/book-catalog-api`:

~~~powershell
mvn compile
docker compose up -d
mvn spring-boot:run
~~~

At startup, verify this order conceptually:

~~~text
Boot obtains the configured DataSource
 -> schema.sql creates/upgrades tables
 -> Hibernate builds the EntityManagerFactory
 -> ddl-auto=validate checks mapped structures
 -> Spring Data creates repository proxies
 -> Spring creates transaction-aware service proxies
 -> web server accepts requests
~~~

If validation reports that `books.version` is missing, do not disable validation. Confirm that the updated `schema.sql` ran against the same database URL and schema used by Hibernate.

### Seed two practice books

Use the existing `POST /books` endpoint or Postman. Example request bodies:

~~~json
{
  "isbn": "TX-LAB-A",
  "title": "Transaction Lab A",
  "author": "Practice",
  "price": 10.00,
  "stock": 10
}
~~~

~~~json
{
  "isbn": "TX-LAB-B",
  "title": "Transaction Lab B",
  "author": "Practice",
  "price": 10.00,
  "stock": 10
}
~~~

Record their returned IDs. Replace `1` and `2` below if necessary.

### Invoke a scenario

Send:

~~~http
POST /transaction-lab/transfers HTTP/1.1
Host: 127.0.0.1:8080
Content-Type: application/json

{
  "fromBookId": 1,
  "toBookId": 2,
  "amount": 3,
  "scenario": "SUCCESS_REQUIRED"
}
~~~

The success scenario returns `204 No Content`. Intentional failure scenarios reach the existing global exception handler and normally return a sanitized `500`. A nonexistent Book still maps to the existing `404`.

### Scenario reference matrix

Reset both books to stock `10`, version `0`, and clear audit rows before each isolated experiment.

| Scenario | Target behavior | Expected response | Final books | Final audit |
|---|---|---|---|---|
| `SUCCESS_REQUIRED` | Outer transaction plus inner `REQUIRED` participants | `204` | A=`7`, B=`13`; both versions increment | `TRANSFER_COMPLETED` commits |
| valid source + nonexistent destination with `SUCCESS_REQUIRED` | Failure between the two book operations | `404` | Source remains `10`; destination absent | None |
| `RUNTIME_ROLLBACK` | Both books and required audit changed, then unchecked failure | `500` | Both remain `10`; versions unchanged | None |
| `FLUSH_THEN_RUNTIME_ROLLBACK` | Book UPDATE SQL forced before unchecked failure | `500` | Both remain `10`; versions unchanged | None; this scenario does not insert an audit |
| `CHECKED_DEFAULT_COMMIT` | Checked exception with default rule | `500` | A=`7`, B=`13`; versions increment | `CHECKED_DEFAULT_COMMITTED` commits |
| `CHECKED_ROLLBACK` | Same checked exception with narrow `rollbackFor` | `500` | Both remain `10`; versions unchanged | None |
| `CAUGHT_REQUIRED_INNER_FAILURE` | Inner proxy marks shared transaction rollback-only; outer catches | `500` (`UnexpectedRollbackException`) | Both remain `10`; versions unchanged | None |
| `REQUIRES_NEW_AUDIT` | Independent audit commits, outer then fails | `500` | Both remain `10`; versions unchanged | `TRANSFER_ATTEMPT` commits |

Do not infer a commit merely from the response:

~~~text
CHECKED_DEFAULT_COMMIT -> HTTP 500 + database COMMIT
RUNTIME_ROLLBACK      -> HTTP 500 + database ROLLBACK
~~~

That contrast is intentional.

### Expected SQL shape

Exact formatting and updated column lists are provider details, but the important shapes are:

~~~sql
-- load each managed Book
SELECT ... FROM books WHERE id = ?;

-- optimistic update; Hibernate may include additional mapped columns
UPDATE books
SET ..., stock = ?, version = ?
WHERE id = ? AND version = ?;

-- local audit insert
INSERT INTO transaction_audits
    (created_at, details, event_type)
VALUES (?, ?, ?);
~~~

With default Hibernate mapping, a dirty update need not be a one-column SQL statement. What matters for this lesson is that no `save()` is needed for managed books and that the version predicate is present.

`FLUSH_THEN_RUNTIME_ROLLBACK` should produce both book UPDATEs before the rollback log. A fresh PostgreSQL query must still show the old values. `REQUIRES_NEW_AUDIT` should show a distinct audit transaction commit followed by the outer rollback.

### Task 1 reference — responsibility map

| Component | Responsibility in this project |
|---|---|
| Controller | HTTP binding, validation trigger, response status; no database transaction policy |
| Spring service proxy | Receives external calls and applies transaction advice |
| `TransactionInterceptor` | Resolves transaction attributes; invokes begin/join/suspend; completes by commit/rollback rules |
| `JpaTransactionManager` | Coordinates the transaction-bound `EntityManager` and underlying JDBC resource |
| `EntityManager` | JPA API facade for the transaction-scoped persistence context |
| Persistence context | Identity map, managed entity state, snapshots, pending work |
| Hibernate ORM | JPA provider; dirty checking, flush ordering, SQL generation, version check |
| JDBC/pgJDBC connection | Carries the actual local database transaction and statements |
| HikariCP | Leases/reuses physical connections; does not define business atomicity |
| PostgreSQL | Executes SQL, enforces constraints/isolation/locks, commits or rolls back |
| Global exception handler | Converts an already-propagated failure to HTTP; it does not roll back JDBC |

### Task 2 reference — current service classification

| Method | Resolved intent |
|---|---|
| `findById` | class-level `readOnly = true` |
| `findAll` | class-level `readOnly = true` |
| `create` | method-level read-write `REQUIRED` |
| `replace` | method-level read-write `REQUIRED` |
| `setStock` | method-level read-write `REQUIRED` |
| `delete` | method-level read-write `REQUIRED` |

For a plain method-level `@Transactional`, the resolved defaults are `REQUIRED`, `DEFAULT`, read-write, default timeout, and rollback for `RuntimeException`/`Error` unless project-wide rules say otherwise.

### Task 3 reference — dirty-checking trace

~~~text
service proxy begins transaction
 -> repo.findById executes SELECT
 -> Hibernate associates Book with the persistence context
 -> setStock changes only Java managed state at that moment
 -> target method returns
 -> transaction manager initiates completion
 -> Hibernate dirty-checking detects changed stock
 -> flush issues versioned UPDATE
 -> PostgreSQL commit succeeds
 -> persistence context closes; returned entity becomes detached
 -> connection handle returns to HikariCP
~~~

No `repo.save(book)` belongs between `setStock` and return.

### Task 4 reference — flush/rollback trace

~~~text
Tx-1 begins
 -> both Books load and become managed
 -> both stock fields change
 -> bookRepository.flush() synchronizes the persistence context
 -> UPDATE A and UPDATE B execute
 -> deliberate RuntimeException escapes
 -> Tx-1 rolls back
 -> new PostgreSQL transaction sees neither book change
~~~

### Task 5–8 reference — one transaction, several participants

~~~text
StockTransferService proxy
 -> creates Tx-1
 -> BookStockService.decrease proxy sees Tx-1 and joins
 -> BookStockService.increase proxy sees Tx-1 and joins
 -> TransactionAuditService.recordRequired proxy sees Tx-1 and joins
 -> exactly one physical business transaction reaches its final outcome
~~~

For the caught-inner scenario:

~~~text
recordRequiredThenFail target throws RuntimeException
 -> its proxy marks participating Tx-1 rollback-only
 -> outer target catches the Java exception
 -> outer proxy attempts normal completion
 -> commit is impossible because Tx-1 is rollback-only
 -> rollback occurs
 -> UnexpectedRollbackException tells the caller no commit occurred
~~~

### Task 9 reference — `REQUIRES_NEW` and the pool

~~~text
Tx-1 / EntityManager-1 / connection A
 -> Books selected and changed
 -> suspend Tx-1 resources

Tx-2 / EntityManager-2 / connection B
 -> INSERT TRANSFER_ATTEMPT
 -> flush
 -> COMMIT Tx-2
 -> return connection B

resume Tx-1 / connection A
 -> deliberate runtime failure
 -> ROLLBACK Tx-1
 -> return connection A
~~~

One request can therefore occupy two pool connections during the nested portion. With five maximum connections, five concurrent outer transactions that all hold a connection and wait for an inner connection can starve one another. The remedy starts with reducing unnecessary nested transactions and shortening boundaries—not reflexively raising the pool limit.

### Task 10 reference — disposable self-invocation probe

The final source intentionally excludes broken methods. To reproduce the issue, temporarily add these methods to `StockTransferService` together with imports for `Book` and `BookNotFoundException`:

~~~java
public Book brokenOuter(long bookId, int newStock) {
    return innerTransactionalUpdate(bookId, newStock);
}

@Transactional
public Book innerTransactionalUpdate(long bookId, int newStock) {
    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
    book.setStock(newStock);
    return book;
}
~~~

Invoke `brokenOuter` through the debugger while stopped inside a lab controller request. The call entering `brokenOuter` crosses the service proxy, but that method has no transaction metadata. Its call to `innerTransactionalUpdate` is a direct same-object call:

~~~text
controller -> proxy -> brokenOuter target
                         |
                         +-> this.innerTransactionalUpdate
                             no proxy re-entry
~~~

With no surrounding service transaction, `bookRepository.findById` can open and finish its own repository read transaction. The returned entity is detached by the time the method mutates it, so the Java object can display the new stock while a fresh database read displays the old stock.

Two correct, ordinary solutions are:

~~~java
// Solution A: the externally invoked use case owns the boundary.
@Transactional
public Book correctedOuter(long bookId, int newStock) {
    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
    book.setStock(newStock);
    return book;
}
~~~

~~~java
// Solution B: delegate through a different, meaningful Spring bean.
@Service
public class CorrectedDelegatingService {

    private final BookService bookService;

    public CorrectedDelegatingService(BookService bookService) {
        this.bookService = bookService;
    }

    public Book correctedByDelegation(long bookId, int newStock) {
        return bookService.setStock(bookId, newStock);
    }
}
~~~

The cumulative reference already uses Solution A for outer transfers and Solution B's principle for stock/audit participants. Remove the disposable methods after observing them. Self-injection and `AopContext.currentProxy()` are not justified here.

### Task 11 reference — what read-only means here

| Question | Answer |
|---|---|
| What does it communicate? | The use case is intended to read rather than mutate persistent state |
| What can Spring/Hibernate do? | Propagate a read-only hint and reduce normal flush/dirty-checking work, depending on the stack |
| Does it change database authorization? | No |
| Does it guarantee every attempted write fails? | No |
| What enforces database permissions? | PostgreSQL roles and privileges |
| Why keep method write overrides? | A method-level plain `@Transactional` resolves as read-write and makes write intent explicit |

Do not use “mutate inside read-only and see what happens” as a correctness strategy. Provider optimizations and database handling are not a portable write guard.

### Task 12 reference — exact isolation observations

Suppose the initial stock is `10`.

#### PostgreSQL `READ COMMITTED`

~~~text
Session A                               Session B
BEGIN READ COMMITTED
SELECT -> 10
                                        BEGIN
                                        UPDATE stock -> 11
                                        COMMIT
SELECT -> 11
COMMIT
~~~

Each SELECT sees data committed before that statement began, so A can observe a non-repeatable read.

#### PostgreSQL `REPEATABLE READ`

~~~text
Session A                               Session B
BEGIN REPEATABLE READ
SELECT -> 10  (snapshot established)
                                        BEGIN
                                        UPDATE stock -> 11
                                        COMMIT
SELECT -> 10  (same snapshot)
COMMIT
new transaction SELECT -> 11
~~~

PostgreSQL's repeatable-read implementation also prevents phantom reads, which is stronger than the minimum SQL-standard allowance often shown in generic tables. If A tries to update the concurrently changed row, PostgreSQL can abort A with a concurrent-update/serialization error. The application must retry the complete transaction from fresh state if its policy allows retry.

Equivalent Spring declarations are:

~~~java
@Transactional(
        readOnly = true,
        isolation = Isolation.READ_COMMITTED)
public void readCommittedUseCase() {
    // scalar query 1, debugger pause, scalar query 2
}

@Transactional(
        readOnly = true,
        isolation = Isolation.REPEATABLE_READ)
public void repeatableReadUseCase() {
    // scalar query 1, debugger pause, scalar query 2
}
~~~

The methods must be entered through a proxy and must start new transactions. An inner `REQUIRED` method normally joins the existing transaction; it cannot retroactively replace the already-chosen isolation level.

### Task 13 reference — optimistic race procedure

1. Confirm the row has `stock = 10`, `version = 0`.
2. Set a debugger breakpoint in existing `BookService.setStock` immediately after `findById` returns.
3. Configure the breakpoint to suspend only the current thread.
4. Send PATCH A setting stock to `20`; pause A after it loaded version `0`.
5. Disable the breakpoint.
6. Send PATCH B setting stock to `30`; allow B to commit.
7. Query PostgreSQL: expect stock `30`, version `1`.
8. Resume A.
9. A attempts an update with `WHERE id = ? AND version = 0`; zero rows match.
10. Hibernate raises an optimistic-lock failure and A rolls back.
11. Query again: stock remains `30`, version remains `1`.

The current generic exception handler returns a sanitized `500` for the losing request. A production API may define a narrow `409 Conflict` mapping and a client retry contract, but that is an explicit API choice—not a reason to catch the exception and continue using the failed transaction.

### Task 14 reference — logger meanings

| Logger | What it helps reveal | What it cannot prove alone |
|---|---|---|
| `org.springframework.transaction` at TRACE | Transaction attributes, interceptor entry/exit, participation | Exact SQL or durable database state |
| `org.springframework.orm.jpa.JpaTransactionManager` at DEBUG | JPA transaction creation, joins, suspension/resumption, commit/rollback | Business correctness |
| `org.hibernate.SQL` at DEBUG | Generated SQL and flush timing | That a later commit succeeded |

For a production incident, enable only the narrow logging needed, for a bounded time, under an operational plan. Parameter/bind logging can be high volume and expose sensitive values.

### Task 15 reference — final SQL checks

~~~sql
SELECT id, isbn, stock, version
FROM books
ORDER BY id;

SELECT id, event_type, details, created_at
FROM transaction_audits
ORDER BY id;
~~~

Expected durable results from a `10/10`, version `0/0`, empty-audit baseline:

| Scenario | A stock/version | B stock/version | Audit rows |
|---|---|---|---|
| `SUCCESS_REQUIRED`, amount 3 | `7 / 1` | `13 / 1` | one `TRANSFER_COMPLETED` |
| Missing destination | `10 / 0` | unchanged/absent | none |
| `RUNTIME_ROLLBACK` | `10 / 0` | `10 / 0` | none |
| `FLUSH_THEN_RUNTIME_ROLLBACK` | `10 / 0` | `10 / 0` | none |
| `CHECKED_DEFAULT_COMMIT` | `7 / 1` | `13 / 1` | one `CHECKED_DEFAULT_COMMITTED` |
| `CHECKED_ROLLBACK` | `10 / 0` | `10 / 0` | none |
| `CAUGHT_REQUIRED_INNER_FAILURE` | `10 / 0` | `10 / 0` | none |
| `REQUIRES_NEW_AUDIT` | `10 / 0` | `10 / 0` | one `TRANSFER_ATTEMPT` |
| Optimistic race on Book A | winner's stock / `1` | unchanged | none; stale Book A value is absent |

Identity values in `transaction_audits` can contain gaps after rollback. Test row existence and contents, not sequence continuity.

## End-to-end successful path

~~~text
POST /transaction-lab/transfers (SUCCESS_REQUIRED)
    |
TransactionLabController
    | external bean call
StockTransferService proxy
    | TransactionInterceptor asks JpaTransactionManager for Tx-1
    | EntityManager/persistence context bound
    |
    +-> BookStockService proxy: REQUIRED joins Tx-1
    |      -> SELECT A -> managed Book A -> Java stock mutation
    |
    +-> BookStockService proxy: REQUIRED joins Tx-1
    |      -> SELECT B -> managed Book B -> Java stock mutation
    |
    +-> TransactionAuditService proxy: REQUIRED joins Tx-1
           -> save may execute the IDENTITY INSERT to obtain its ID
              (the row is still uncommitted)
    |
outer method returns
    -> dirty checking
    -> flush: versioned UPDATE A + UPDATE B + any remaining SQL
    -> PostgreSQL COMMIT
    -> connection returned to HikariCP
    -> controller returns 204
~~~

## End-to-end failing paths

### Runtime failure

~~~text
Tx-1 -> mutate A/B -> required audit -> RuntimeException
     -> rollback rule matches
     -> ROLLBACK everything
     -> sanitized 500
~~~

### Checked default

~~~text
Tx-1 -> mutate A/B -> required audit -> checked exception
     -> default rollback rule does not match
     -> flush + COMMIT
     -> checked exception still propagates
     -> sanitized 500
~~~

### Independent audit

~~~text
Tx-1 -> mutate A/B
     -> suspend
       Tx-2 -> INSERT attempt audit -> COMMIT
     -> resume Tx-1 -> RuntimeException -> ROLLBACK books
     -> 500; audit remains
~~~

## Common implementation mistakes diagnosed

| Symptom | Likely cause |
|---|---|
| Every scenario behaves nontransactionally | Service constructed with `new`, annotation processing unavailable, or call bypasses bean proxy |
| `REQUIRES_NEW` audit also rolls back | Same-class invocation or method is not reached through `TransactionAuditService` proxy |
| Checked-default scenario rolls back | A global all-exceptions rollback policy exists, another runtime failure occurred, or a participant marked rollback-only |
| Caught-inner scenario commits | Failure did not cross the inner REQUIRED proxy, or it was caught inside the participant before advice saw it |
| Flush scenario shows no UPDATE | Flush is missing, mutation did not affect managed mapped state, or execution failed earlier |
| SQL appears but final rows are unchanged | This is expected when the encompassing transaction rolled back |
| Startup says `version` is missing | Existing table was not upgraded; `CREATE TABLE IF NOT EXISTS` alone did nothing |
| Two `findById` reads look repeatable at `READ COMMITTED` | Persistence-context identity/first-level cache masked the database experiment |
| Both concurrent updates silently win | `@Version`/column is absent or a raw SQL writer ignored the version protocol |
| Requests wait for pool connections | Transactions are long, concurrency is high, or nested `REQUIRES_NEW` consumes additional leases |

## Final exercise checklist

### Scope and architecture

- [ ] I extended `JPA_Hibernate/book-catalog-api`; I did not create another project.
- [ ] Existing Book routes, DTO response shapes, PostgreSQL, Spring Data JPA, Hibernate, and HikariCP remain.
- [ ] I added only one entity/table and one lab endpoint.
- [ ] I left `BookService` unchanged because it already demonstrates the desired transaction pattern.
- [ ] Controllers contain no database transaction policy.
- [ ] Every propagation-specific call crosses a Spring bean proxy.

### Core transaction behavior

- [ ] I can point to transaction start, Java mutation, dirty checking, flush, SQL, commit, and rollback as different moments.
- [ ] I proved a managed update works without `save()`.
- [ ] I proved visible SQL can later roll back.
- [ ] I proved a runtime exception rolls back by default.
- [ ] I proved a checked exception commits by default in this uncustomized project.
- [ ] I used a narrow checked-exception `rollbackFor` and explained why.
- [ ] I observed rollback-only and `UnexpectedRollbackException`.

### Propagation and resources

- [ ] I can explain `REQUIRED` as join-or-create.
- [ ] I can explain `REQUIRES_NEW` as suspend-and-create-independent.
- [ ] The independent audit is truthfully named `TRANSFER_ATTEMPT`.
- [ ] I understand that the audit is another local PostgreSQL transaction, not an outbox or distributed transaction.
- [ ] I can explain why one nested call may occupy a second Hikari connection.
- [ ] I know why raising pool size is not the first correction for long or excessive transactions.

### Concurrency

- [ ] I observed PostgreSQL statement snapshots at `READ COMMITTED`.
- [ ] I observed the stable snapshot at PostgreSQL `REPEATABLE READ`.
- [ ] I avoided measuring the first-level cache when studying isolation.
- [ ] I added both the Java `@Version` field and the real PostgreSQL column.
- [ ] I produced one optimistic-lock winner and one stale loser.
- [ ] I know optimistic locking detects a conflict; it does not hold a pessimistic row lock while thinking.

### Debugging and evidence

- [ ] I correlated Spring transaction logs, Hibernate SQL, and fresh PostgreSQL queries.
- [ ] I did not treat SQL logging as commit evidence.
- [ ] I reduced verbose logging after the local exercise.
- [ ] I can explain why identity gaps can survive rolled-back inserts.
- [ ] I can explain why the global exception handler does not control rollback.

## Reflection questions

1. Why does the outer stock-transfer method own the main boundary rather than either repository?
2. At what exact point does `book.setStock(...)` become a database operation?
3. Why can an UPDATE appear in logs and still leave no durable change?
4. Why can an HTTP `500` accompany a committed checked-exception transaction?
5. Why does catching an inner `REQUIRED` runtime exception still end with `UnexpectedRollbackException`?
6. Which call in the solution suspends the outer transaction, and why must it target another bean?
7. How many connections can one `REQUIRES_NEW` request occupy during nesting?
8. Why is `TRANSFER_ATTEMPT` more honest than `TRANSFER_COMPLETED` for the independent audit?
9. Why can two `findById` calls be a bad isolation-level experiment?
10. What does the version predicate detect that `READ COMMITTED` alone does not prevent?

## Final mental model

~~~text
Public service use case is called through a Spring proxy
    -> TransactionInterceptor resolves @Transactional
    -> JpaTransactionManager starts or joins a transaction
    -> transaction-bound EntityManager owns a persistence context
    -> Hibernate loads and tracks managed entities
    -> Java mutation is only Java mutation at first
    -> dirty checking determines pending work
    -> flush executes SQL but does not commit
    -> rollback rules choose commit or rollback when control leaves
    -> JDBC Connection carries that decision to PostgreSQL
    -> HikariCP receives the connection back
    -> only committed PostgreSQL state is durable evidence
~~~

If you can predict every row in the scenario matrix before running it—and explain the proxy, persistence-context, JDBC, pool, and PostgreSQL behavior behind that prediction—you are ready to use Spring transactions in ordinary backend work.
