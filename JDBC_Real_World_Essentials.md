# JDBC Real-World Essentials

This guide turns foundational JDBC knowledge into the practical habits needed in plain Java projects that use PostgreSQL. It uses Java 17, Maven, JDBC, and PostgreSQL—without Spring, JPA, Hibernate, or helper libraries that hide the JDBC calls.

> **Source note:** The two referenced PDFs were not included in the available workspace or attachments. Labels such as **PDF foundation** are therefore based on the detailed PDF-topic inventory supplied in the request, not on page-level inspection. **Real-world addition** marks material beyond that inventory, and **Modernization** identifies older techniques that should no longer be normal practice.

**Priority legend**

| Rating | Meaning |
|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Needed for correct everyday JDBC work |
| ⭐⭐⭐⭐ IMPORTANT | Used regularly in real projects |
| ⭐⭐⭐ NICE TO KNOW | Useful context, but not daily CRUD knowledge |
| ⭐⭐ LEARN LATER | Situational or advanced |

---

## 1. JDBC in One Minute

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **PDF foundation + real-world framing**

**JDBC** means **Java Database Connectivity**. It is the standard Java API for sending SQL to a relational database and reading the results.

```text
Java Application
      ↓
JDBC API (`java.sql` and parts of `javax.sql`)
      ↓
PostgreSQL JDBC Driver (pgJDBC)
      ↓
PostgreSQL network protocol
      ↓
PostgreSQL
```

For a query, data returns in the other direction:

```text
PostgreSQL rows
      ↓
ResultSet
      ↓
read each column
      ↓
Java objects
```

JDBC does not create SQL for you. You write the SQL, bind values, choose the correct execution method, process the result, and manage the transaction and resources.

**Mental model:** JDBC is a standard electrical socket. Java defines the socket; pgJDBC is the adapter that knows PostgreSQL's protocol.

**Remember:**

```text
configure driver → get connection → prepare SQL → bind values
→ execute → map results → commit/rollback if needed → close resources
```

---

## 2. JDBC Architecture

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **PDF foundation**, with modern responsibility boundaries

The important types and their roles are:

| Piece | Kind | Responsibility |
|---|---|---|
| JDBC API | Java standard API | Common database interfaces and contracts |
| JDBC driver | Vendor implementation | Translates JDBC operations to PostgreSQL's protocol |
| `DriverManager` | Java class | Finds a suitable registered driver and asks it for a connection |
| `Connection` | Java interface | Represents one database session/transaction context |
| `Statement` | Java interface | Executes unparameterized SQL; rarely preferred for application data |
| `PreparedStatement` | Java interface | Executes SQL with separately bound parameters |
| `ResultSet` | Java interface | Provides cursor-based access to returned rows |
| `SQLException` | Java exception class | Carries database/driver failure information |

Most application code imports standard types:

```java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
```

The PostgreSQL dependency supplies concrete implementations behind those interfaces. A value declared as `Connection` may internally be a pgJDBC connection object, but normal CRUD code should program against the standard interface.

```text
Your code calls Connection.prepareStatement(...)
                  ↓
pgJDBC implementation receives the call
                  ↓
driver communicates with PostgreSQL
```

`javax.sql.DataSource` is also part of Java SE despite the `javax` package name. It is covered later as a real-world connection-factory abstraction.

**Common mistake:** Thinking the JDBC API itself speaks PostgreSQL's wire protocol. The vendor driver does that work.

**Real-project rule:** Keep most persistence code on standard JDBC interfaces. Use vendor-specific APIs only for a deliberate PostgreSQL feature.

---

## 3. JDBC Drivers

> Priority: PostgreSQL driver setup—⭐⭐⭐⭐⭐ MUST KNOW; historical driver types—⭐⭐ NICE TO KNOW  
> Source: **PDF foundation + modernization**

A JDBC driver is a library that implements JDBC contracts for a particular database. PostgreSQL needs its own driver because PostgreSQL authentication, data types, messages, and network protocol differ from other database products.

The PostgreSQL driver is called **pgJDBC**. It is a pure-Java **Type 4** driver and communicates directly with PostgreSQL using PostgreSQL's native network protocol.

### The four traditional driver types

| Type | Historical idea | Priority today |
|---|---|---|
| Type 1 | JDBC-to-ODBC bridge | ⭐⭐ Historical; do not use |
| Type 2 | Part Java, part native database library | ⭐⭐ Specialized/legacy |
| Type 3 | Java driver talks through middleware | ⭐⭐ Specialized/legacy |
| Type 4 | Pure Java driver speaks the database protocol | ⭐⭐⭐⭐ Modern PostgreSQL style |

Do not spend time memorizing the old architectures. Know that pgJDBC is Type 4 and belongs on the application's runtime classpath.

### Modern driver loading

Modern JDBC drivers advertise themselves through Java's service-provider mechanism. When pgJDBC is on the classpath, `DriverManager` can discover it automatically.

```text
pgJDBC JAR on runtime classpath
          ↓
driver service metadata is discovered
          ↓
DriverManager knows a PostgreSQL driver is available
```

This older line may appear in the PDFs or old tutorials:

```java
Class.forName("org.postgresql.Driver");
```

It explicitly loads the driver class. It is usually unnecessary with JDBC 4+ drivers and modern classpaths. Learn to recognize it, but do not add it by habit.

### Historical installation techniques to avoid

Do **not** copy driver JARs into an old JRE extension directory such as `jre/lib/ext`. The extension mechanism is historical and was removed from modern Java. Do not manually scatter JARs through an IDE project either.

**Modern rule:** Declare pgJDBC in Maven. Maven resolves it, and the build/run tool places it on the appropriate classpath.

---

## 4. Maven and the PostgreSQL Driver

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **Real-world addition + modernization**

A minimal Java 17 Maven project can declare pgJDBC like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>jdbc-practice</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.13</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

The current driver version can change; check the official pgJDBC download page when starting a real project rather than memorizing `42.7.13`.

| Element | Meaning here |
|---|---|
| `groupId` | Publishing namespace: `org.postgresql` |
| `artifactId` | Driver artifact name: `postgresql` |
| `version` | Exact driver release |
| `dependency` | Artifact the project needs |
| `runtime` scope | Needed while running; main code compiles against standard `java.sql` APIs |

Default `compile` scope also works and is needed if main source imports vendor classes such as `org.postgresql.ds.PGSimpleDataSource`. For DriverManager-based code that imports only `java.sql`, `runtime` expresses the intent more precisely.

```text
pom.xml
   ↓
Maven resolves org.postgresql:postgresql:42.7.13
   ↓
downloads JAR + metadata
   ↓
caches them under ~/.m2/repository
   ↓
build/run tool constructs runtime classpath
   ↓
DriverManager can discover pgJDBC
```

A normal Maven JAR is usually thin: declaring the driver does not automatically embed it in your application JAR. An IDE or Maven run plugin can build the runtime classpath; deployment packaging must also make the driver available.

**Common mistake:** Seeing that `mvn compile` succeeds and assuming the driver is definitely available to a separately launched `java -jar` command.

**Remember:** Maven manages the dependency. The driver must still be present on the runtime classpath.

---

## 5. JDBC URL

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **PDF foundation**, with practical URL rules

A JDBC URL tells `DriverManager` which driver and database endpoint to use.

```text
jdbc:postgresql://localhost:5432/student_db
 │       │              │       │       │
 │       │              │       │       └─ database name
 │       │              │       └───────── TCP port
 │       │              └───────────────── hostname
 │       └──────────────────────────────── PostgreSQL subprotocol
 └──────────────────────────────────────── JDBC URL prefix
```

| Part | Example | Meaning |
|---|---|---|
| Prefix | `jdbc` | A JDBC connection URL |
| Subprotocol | `postgresql` | Selects pgJDBC |
| Host | `localhost` | Database server is on this computer |
| Port | `5432` | PostgreSQL's conventional default port |
| Database | `student_db` | Specific database to connect to |

The general pgJDBC form is:

```text
jdbc:postgresql:[//host[:port]/][database][?property=value&...]
```

Credentials are separate connection properties in the examples:

```java
String url = "jdbc:postgresql://localhost:5432/student_db";
String username = "jdbc_app";
String password = configurationValue;
```

`localhost` is a host, not a database name. A PostgreSQL server can contain many databases, and a database can contain schemas. Those are different levels.

Do not put a password in a URL that may be logged. When URL parameters are necessary, follow URL-encoding and driver documentation.

**Common mistakes:** wrong port, misspelled database, using a table name as the database name, or pointing to `localhost` when PostgreSQL actually runs on another machine.

**Remember:**

```text
jdbc:postgresql://HOST:PORT/DATABASE
```

---

## 6. DriverManager

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW for a first plain JDBC project  
> Source: **PDF foundation + modernization**

`DriverManager.getConnection(...)` asks registered JDBC drivers to open a connection for a URL.

```java
String url = "jdbc:postgresql://localhost:5432/student_db";

try (Connection connection =
         DriverManager.getConnection(url, username, password)) {
    System.out.println("Connected: " + !connection.isClosed());
}
```

Conceptually:

```text
driver JAR is on runtime classpath
        ↓
DriverManager discovers registered drivers
        ↓
URL begins jdbc:postgresql:
        ↓
pgJDBC accepts that URL
        ↓
TCP connection + PostgreSQL authentication
        ↓
Connection is returned or SQLException is thrown
```

Getting a connection is real work: it can involve network communication, authentication, and server session setup. It is not equivalent to constructing a lightweight ordinary Java object.

`Class.forName("org.postgresql.Driver")` is historical background, not part of the normal modern flow.

**Real-project rule:** `DriverManager` is excellent for learning and small tools. Larger/server applications normally obtain connections through a `DataSource`, often backed by a pool.

---

## 7. Connection

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **PDF foundation + transaction/resource additions**

A `Connection` represents one database session and transaction context.

Important operations are:

| Method | Purpose |
|---|---|
| `connection.close()` | Release the connection; with a pool, usually return it to the pool |
| `connection.isClosed()` | Ask whether JDBC considers it closed |
| `connection.setAutoCommit(false)` | Start explicit transaction control |
| `connection.commit()` | Make the current transaction's changes permanent |
| `connection.rollback()` | Undo the current transaction's uncommitted changes |

By default, a new JDBC connection normally has auto-commit enabled:

```text
statement executes successfully
        ↓
its transaction is committed automatically
```

For multiple operations that must succeed together, disable auto-commit and use the **same connection** for every operation.

```java
try (Connection connection = DatabaseConfig.getConnection()) {
    connection.setAutoCommit(false);

    try {
        // execute every related statement with this connection
        connection.commit();
    } catch (SQLException | RuntimeException e) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            e.addSuppressed(rollbackFailure);
        }
        throw e;
    }
}
```

Section 17 explains this transaction pattern and its rules in detail.

Changing auto-commit from `false` back to `true` while a transaction is active commits that transaction. Never use `setAutoCommit(true)` as cleanup before an explicit `commit()` or `rollback()`.

Closing a connection does not mean shutting down PostgreSQL. It ends/releases this application's session.

`isClosed() == false` does not prove the network/server session is healthy; it mainly reports JDBC closed state. `isValid(timeoutSeconds)` is the dedicated validation API, although a maintained pool normally owns health checks in a server application.

**Common mistakes:**

- Leaving connections open indefinitely.
- Sharing one mutable `Connection` between unrelated threads.
- Creating a second connection inside a DAO method during a transaction; it will not participate in the first connection's transaction.
- Calling `commit()` while auto-commit is still enabled.

**Real-project rule:** Keep connection ownership obvious and its lifetime as short as the unit of work permits.

---

## 8. Statement Types

> Priority: `PreparedStatement`—⭐⭐⭐⭐⭐ MUST KNOW; `Statement`—⭐⭐⭐; `CallableStatement`—⭐⭐ LEARN LATER  
> Source: **PDF foundation**, reprioritized for real projects

| Type | Purpose | Practical priority |
|---|---|---|
| `Statement` | Executes a complete SQL string with no bound parameters | Useful for understanding JDBC; limited application use |
| `PreparedStatement` | SQL template plus bound data values | Normal choice for CRUD |
| `CallableStatement` | Calls stored procedures/functions through JDBC call syntax | Learn later if a project needs it |

Plain statement:

```java
try (Statement statement = connection.createStatement();
     ResultSet rs = statement.executeQuery("SELECT current_date")) {
    // read result
}
```

Prepared statement:

```java
String sql = "SELECT id, name FROM students WHERE email = ?";

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, email);
    try (ResultSet rs = ps.executeQuery()) {
        // read result
    }
}
```

**Real-project rule:** If SQL contains application/user data, use a `PreparedStatement` and bind the data. Do not concatenate it into SQL.

---

## 9. executeQuery / executeUpdate / execute

> Priority: `executeQuery` and `executeUpdate`—⭐⭐⭐⭐⭐ MUST KNOW; `execute`—⭐⭐⭐ NICE TO KNOW  
> Source: **PDF foundation**

Choose the method from the SQL result you expect.

### `executeQuery()`

```text
SELECT
  ↓
executeQuery()
  ↓
ResultSet
```

```java
String sql = "SELECT id, name FROM students WHERE age >= ?";

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setInt(1, minimumAge);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            System.out.println(rs.getString("name"));
        }
    }
}
```

### `executeUpdate()`

```text
INSERT / UPDATE / DELETE
          ↓
executeUpdate()
          ↓
affected-row count
```

```java
String sql = "DELETE FROM students WHERE id = ?";

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setLong(1, id);
    int affectedRows = ps.executeUpdate();
    boolean deleted = affectedRows == 1;
}
```

DDL can also return an update count of zero through `executeUpdate`, but application CRUD is the main use here.

### `execute()`

`execute()` is the general method when SQL may produce either a result set or an update count. It returns `true` if the first result is a `ResultSet`, otherwise `false`.

```java
boolean hasResultSet = statement.execute(sql);
```

`false` does **not** mean execution failed; it means the first result is an update count or there is no result set. Failures are reported with `SQLException`.

Most normal CRUD code already knows what kind of SQL it is executing, so prefer the more specific method.

**Common mistake:** Calling `executeQuery()` for an `UPDATE`, or ignoring the affected-row count from `executeUpdate()`.

---

## 10. PreparedStatement

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **PDF foundation + real-world type/binding rules**

A `PreparedStatement` separates the SQL template from data values.

```java
String sql = "SELECT id, name, email FROM students WHERE email = ?";

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, email);

    try (ResultSet rs = ps.executeQuery()) {
        // process rows
    }
}
```

Parameter indexes begin at **1**, not 0.

```text
UPDATE students SET name = ?, age = ? WHERE id = ?
                           1       2            3
```

```java
ps.setString(1, student.getName());
ps.setInt(2, student.getAge());
ps.setLong(3, student.getId());
```

### Important setters

| Java/JDBC method | Common use |
|---|---|
| `setInt` | PostgreSQL `INTEGER` |
| `setLong` | `BIGINT`, identity IDs |
| `setString` | `VARCHAR`, `TEXT` |
| `setDouble` | Floating-point data—not exact money |
| `setBoolean` | `BOOLEAN` |
| `setBigDecimal` | `NUMERIC`/`DECIMAL`, especially money-like values |
| `setDate` | Legacy `java.sql.Date`; recognize it |
| `setObject` | Modern date/time and other supported types |

For modern date/time code, pgJDBC supports Java Time values through `setObject`/typed `getObject`:

```java
ps.setObject(1, LocalDate.now());

LocalDate date = rs.getObject("birth_date", LocalDate.class);
OffsetDateTime created =
        rs.getObject("created_at", OffsetDateTime.class);
```

Typical mappings include `DATE` ↔ `LocalDate`, `TIMESTAMP` ↔ `LocalDateTime`, and `TIMESTAMP WITH TIME ZONE` ↔ `OffsetDateTime`. Verify uncommon mappings in pgJDBC documentation.

For an explicit SQL `NULL`, use a type-aware call when inference is ambiguous:

```java
ps.setNull(2, Types.VARCHAR);
```

### What `?` can and cannot represent

Placeholders represent **data values**:

```sql
WHERE email = ?
SET age = ?
```

They do not represent SQL identifiers or syntax:

```sql
SELECT * FROM ?       -- not a parameterized table name
ORDER BY name ?       -- not a parameterized ASC/DESC keyword
```

If a table/column/sort direction must be dynamic, choose it from a strict application-controlled allowlist and concatenate only the validated identifier/syntax—not raw user input.

**Common mistakes:** using index 0, binding in the wrong order, using `double` for exact monetary data, keeping a statement open too long, or trying to parameterize a table name.

**Remember:** SQL structure stays in the SQL string; data values go through typed setters.

---

## 11. SQL Injection

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: Prepared statements are **PDF foundation**; the security mechanism/rules are a **real-world addition**

### Bad: data is concatenated into SQL text

```java
String sql =
        "SELECT id, username FROM users WHERE username = '" +
        username + "'";
```

If `username` contains quotes and SQL syntax, that syntax becomes part of the command. Escaping by hand is easy to get wrong and depends on database/string rules.

### Correct: SQL and data travel as different concepts

```java
String sql =
        "SELECT id, username FROM users WHERE username = ?";

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, username);
    try (ResultSet rs = ps.executeQuery()) {
        // process result
    }
}
```

Conceptually:

```text
SQL template: SELECT ... WHERE username = ?
                             +
bound value:  characters supplied by the user
                             ↓
driver sends the value as data for the parameter
                             ↓
the value is not reinterpreted as SQL structure
```

Prepared statements protect **bound values** because the driver/database knows where SQL structure ends and a data value begins. The database compares the column to the supplied value; it does not paste the value into the statement and parse it as new SQL.

Prepared statements do not automatically secure dynamic identifiers. Use an allowlist for a user-selectable sort column:

```java
String orderBy = switch (requestedSort) {
    case "name" -> "name";
    case "age" -> "age";
    default -> "id";
};

String sql = "SELECT id, name, age FROM students ORDER BY " + orderBy;
```

Here the SQL fragment comes only from hard-coded safe choices.

**Real-project rule:** Never concatenate untrusted data into SQL. Bind values; allowlist the rare dynamic syntax.

---

## 12. ResultSet

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **PDF foundation + practical null/label guidance**

A `ResultSet` is a cursor over rows returned by a query. Initially, the cursor is **before** the first row.

```text
before first row
      ↓ rs.next() returns true
row 1
      ↓ rs.next() returns true
row 2
      ↓ rs.next() returns false
after last row
```

The normal pattern is:

```java
try (ResultSet rs = ps.executeQuery()) {
    while (rs.next()) {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        System.out.println(id + ": " + name);
    }
}
```

Common getters mirror setters:

```text
getInt      getLong       getString
getDouble   getBoolean    getBigDecimal
getObject
```

You may access a column by label or 1-based index:

```java
long idByLabel = rs.getLong("id");
long idByIndex = rs.getLong(1);
```

Column labels are normally clearer and less fragile when the `SELECT` order changes. If SQL uses an alias, access its label:

```sql
SELECT COUNT(*) AS student_count FROM students
```

```java
long count = rs.getLong("student_count");
```

Primitive getters cannot return Java `null`. After `getInt`/`getLong`, `rs.wasNull()` tells whether SQL `NULL` was read. Typed `getObject` can preserve nullability:

```java
Integer age = rs.getObject("age", Integer.class);
```

**Common mistakes:** reading before `rs.next()`, using index 0, calling `next()` twice accidentally, assuming a query always returns a row, or returning an open `ResultSet` after its statement/connection closes.

**Remember:** Move the cursor, then read the current row.

---

## 13. Mapping Rows to Java Objects

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **Real-world addition**

Real applications rarely pass a `ResultSet` throughout the program. Persistence code reads each row and creates a domain/model object.

```java
private Student mapRow(ResultSet rs) throws SQLException {
    return new Student(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getInt("age")
    );
}
```

```text
database row
    ↓
ResultSet cursor points to row
    ↓
get columns by label
    ↓
construct Student
    ↓
return ordinary Java object
```

Benefits:

- SQL/JDBC stays inside the DAO/repository layer.
- The rest of the program works with normal Java objects.
- Mapping logic is written once instead of repeated in every query.
- A closed `ResultSet` does not escape its resource scope.

Make the `SELECT` list explicit so mapping expectations are visible:

```sql
SELECT id, name, email, age
FROM students
WHERE id = ?
```

**Common mistake:** Using `SELECT *` everywhere and silently depending on a table's current column order or shape.

**Real-project rule:** Map inside the resource scope and return Java data, not live JDBC resources.

---

## 14. CRUD

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **PDF execution primitives + real-world DAO synthesis**

CRUD maps directly to four SQL operations.

| CRUD | SQL | JDBC method | Result |
|---|---|---|---|
| Create | `INSERT` | `executeUpdate()` | affected count + optional generated key |
| Read | `SELECT` | `executeQuery()` | `ResultSet` |
| Update | `UPDATE` | `executeUpdate()` | affected count |
| Delete | `DELETE` | `executeUpdate()` | affected count |

### Create

```java
String sql =
        "INSERT INTO students (name, email, age) VALUES (?, ?, ?)";

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, student.getName());
    ps.setString(2, student.getEmail());
    ps.setInt(3, student.getAge());
    int inserted = ps.executeUpdate();
}
```

### Read

```java
String sql =
        "SELECT id, name, email, age FROM students WHERE id = ?";

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setLong(1, id);
    try (ResultSet rs = ps.executeQuery()) {
        Student result = rs.next() ? mapRow(rs) : null;
    }
}
```

### Update

```java
String sql = """
        UPDATE students
        SET name = ?, email = ?, age = ?
        WHERE id = ?
        """;

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, student.getName());
    ps.setString(2, student.getEmail());
    ps.setInt(3, student.getAge());
    ps.setLong(4, student.getId());
    boolean updated = ps.executeUpdate() == 1;
}
```

### Delete

```java
String sql = "DELETE FROM students WHERE id = ?";

try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setLong(1, id);
    boolean deleted = ps.executeUpdate() == 1;
}
```

Every complete method must also acquire and close its connection. Section 15 shows the full resource pattern, and the mini project supplies complete DAO code.

**Real-project rules:** use explicit columns, bind every value, interpret affected counts, and map results before resources close.

---

## 15. Try-With-Resources

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: Closing resources is **PDF foundation**; try-with-resources discipline is a **modern real-world requirement**

`Connection`, `Statement`, and `ResultSet` are `AutoCloseable`, so Java can close them automatically.

```java
String sql =
        "SELECT id, name, email, age FROM students WHERE id = ?";

try (Connection connection = DatabaseConfig.getConnection();
     PreparedStatement ps = connection.prepareStatement(sql)) {

    ps.setLong(1, id);

    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }
}
```

The nested shape makes the lifetime clear:

```text
Connection opened
   ↓
PreparedStatement opened
   ↓
ResultSet opened
   ↓
read/map rows
   ↓
ResultSet closes
   ↓
PreparedStatement closes
   ↓
Connection closes
```

Resources declared in one try-with-resources header close in reverse declaration order. Nesting the `ResultSet` after binding also keeps the code readable.

This is safer than manual `finally` blocks because resources close on success, early return, and exceptions. If closing also fails while another exception is active, Java preserves it as a suppressed exception.

Why timely closing matters:

- An open `ResultSet` can retain server/client resources.
- An open statement can retain resources and depend on its connection.
- An open physical connection consumes a limited database session.
- In a pool, `connection.close()` normally returns the logical connection to the pool; forgetting it leaks pool capacity.

Closing a `Connection` releases its associated JDBC resources, but do not use that fact as the normal ownership strategy. Explicit try-with-resources scopes close each result and statement at the earliest clear point and remain correct if the connection's lifetime later grows.

**Remember:** Acquire late, close automatically, and never keep JDBC resources open longer than needed.

---

## 16. SQLException

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **Real-world addition**

JDBC reports database/driver failures with `SQLException`.

```java
try {
    studentDao.create(student);
} catch (SQLException e) {
    System.err.println("Database operation failed");
    System.err.println("Message: " + e.getMessage());
    System.err.println("SQLState: " + e.getSQLState());
    System.err.println("Vendor code: " + e.getErrorCode());
}
```

| Information | Use |
|---|---|
| `getMessage()` | Human-readable server/driver detail |
| `getSQLState()` | Standardized five-character category/code |
| `getErrorCode()` | Vendor-specific numeric code; pgJDBC often relies more on SQLState |
| `getCause()` | Underlying cause when one exists |
| `getNextException()` | Additional chained SQL exception, sometimes relevant to batches |

PostgreSQL's SQLState `23505` means a unique-constraint violation, such as a duplicate email. Prefer a named constant in larger code, but recognize the value during the exercise.

### Four handling choices

| Choice | Beginner meaning |
|---|---|
| Recover | Handle a known expected failure, perhaps ask for a different email |
| Log | Record useful context for diagnosis without logging passwords or sensitive data |
| Propagate | Declare `throws SQLException` so a higher layer decides |
| Wrap | Put the SQL exception inside an application-specific unchecked exception while preserving the cause |

A DAO can reasonably propagate `SQLException` in this learning project. `Main` can catch it at the application boundary and print safe diagnostic information.

Do not do this:

```java
catch (SQLException e) {
    // ignored
}
```

And do not convert every failure into “database unavailable.” Constraint violations, authentication errors, invalid SQL, and network failures need different responses.

**Real-project rule:** Catch only where you can add meaning, recover, rollback, or translate. Otherwise propagate without losing the original cause.

---

## 17. Transactions

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **Real-world addition emphasized for correctness**

A transaction groups database operations into one atomic unit.

```text
Operation A
+ Operation B
+ Operation C
       ↓
all succeed
       ↓
COMMIT: keep all changes
```

```text
one operation fails
       ↓
ROLLBACK: undo the unit's uncommitted changes
```

Classic example:

```text
subtract 100 from Account A
             +
add 100 to Account B
             ↓
must succeed together
```

Basic pattern:

```java
try (Connection connection = DatabaseConfig.getConnection()) {
    connection.setAutoCommit(false);

    try {
        debit(connection, fromAccountId, amount);
        credit(connection, toAccountId, amount);
        connection.commit();
    } catch (SQLException | RuntimeException e) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            e.addSuppressed(rollbackFailure);
        }
        throw e;
    }
}
```

Important rules:

1. Every statement in the unit must use the **same `Connection`**.
2. Disable auto-commit before the first operation.
3. Commit only after all required checks and statements succeed.
4. Roll back when any operation fails.
5. Explicitly commit or roll back before close; do not rely on close to resolve an active transaction.
6. Before returning a pooled connection, follow the pool's contract for restoring changed state.
7. Keep the transaction short; do not wait for console input or make slow network calls inside it.

With auto-commit enabled, each statement is committed when it completes. That is convenient for independent CRUD operations but cannot protect a multi-statement invariant.

`rollback()` can itself fail, so preserve the original exception and attach rollback failure rather than hiding the first cause.

Transaction isolation levels, savepoints, retry strategies, deadlocks, XA, and distributed transactions are ⭐⭐ LEARN LATER.

**Remember:** A transaction boundary is a business unit of work, not merely a block around one arbitrary SQL statement.

---

## 18. Generated Keys

> Priority: ⭐⭐⭐⭐ IMPORTANT  
> Source: **Real-world addition**

PostgreSQL often generates a primary key through an identity column. Java usually needs that new ID so the created object can be referenced immediately.

```text
INSERT student
      ↓
PostgreSQL generates id
      ↓
JDBC exposes generated keys
      ↓
Java reads and stores new id
```

```java
String sql =
        "INSERT INTO students (name, email, age) VALUES (?, ?, ?)";

try (Connection connection = DatabaseConfig.getConnection();
     PreparedStatement ps = connection.prepareStatement(
             sql,
             Statement.RETURN_GENERATED_KEYS)) {

    ps.setString(1, student.getName());
    ps.setString(2, student.getEmail());
    ps.setInt(3, student.getAge());

    int affectedRows = ps.executeUpdate();
    if (affectedRows != 1) {
        throw new SQLException(
                "Expected one inserted row, got " + affectedRows);
    }

    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (!keys.next()) {
            throw new SQLException("Database returned no generated key");
        }

        long id = keys.getLong(1);
        student.setId(id);
        return id;
    }
}
```

Important details:

- Request keys when creating the prepared statement.
- Execute the insert before calling `getGeneratedKeys()`.
- The returned keys are themselves a `ResultSet` and must be advanced/closed.
- Verify that exactly one row was inserted when the method expects one.

PostgreSQL also supports `INSERT ... RETURNING id`, which returns a regular result set and can be convenient, but it is PostgreSQL-specific. Learn the standard generated-key pattern first because it is explicitly part of JDBC.

Generated identity/sequence values can contain gaps after failed or rolled-back inserts. IDs identify rows; do not assume they are gapless business counters.

**Common mistake:** Assuming the Java object automatically receives the database-generated ID.

---

## 19. Batch Operations

> Priority: ⭐⭐⭐⭐ IMPORTANT  
> Source: **Real-world addition**

A batch groups repeated executions of a statement so the driver/database can reduce communication overhead.

```text
four separate application calls
→ repeated client/server round trips

one prepared batch
→ bind/add several parameter sets
→ executeBatch()
→ fewer round trips and less repeated overhead
```

```java
String sql =
        "INSERT INTO students (name, email, age) VALUES (?, ?, ?)";

try (Connection connection = DatabaseConfig.getConnection();
     PreparedStatement ps = connection.prepareStatement(sql)) {

    for (Student student : students) {
        ps.setString(1, student.getName());
        ps.setString(2, student.getEmail());
        ps.setInt(3, student.getAge());
        ps.addBatch();
    }

    int[] updateCounts = ps.executeBatch();
}
```

`addBatch()` captures the current parameter set. On normal completion, `executeBatch()` returns update counts; an entry may be `Statement.SUCCESS_NO_INFO` when the command succeeded but the driver cannot report an exact count. If a command fails, JDBC throws `BatchUpdateException`; when a driver continues processing, that exception's update-count array may contain `Statement.EXECUTE_FAILED`.

### A batch is not automatically your transaction policy

Do not assume a batch is guaranteed to be all-or-nothing in every mode. If partial insertion would be incorrect, execute it inside an explicit transaction:

```text
setAutoCommit(false)
      ↓
addBatch + executeBatch
      ↓
success → commit
failure → rollback
```

pgJDBC also has an optional `reWriteBatchedInserts=true` connection property that can optimize compatible inserts. It is a tuning option, not required to learn batching.

**Common mistakes:** forgetting `addBatch()`, changing parameters after `addBatch()` and assuming the earlier entry changed, ignoring `BatchUpdateException`, or treating batching as a replacement for transaction design.

---

## 20. ResultSetMetaData

> Priority: ⭐⭐⭐ NICE TO KNOW  
> Source: **PDF foundation**

`ResultSet` contains actual row values. `ResultSetMetaData` describes the columns returned by that result.

```text
ResultSet
→ row data

ResultSetMetaData
→ returned-column descriptions
```

```java
ResultSetMetaData meta = rs.getMetaData();
int count = meta.getColumnCount();

for (int i = 1; i <= count; i++) {
    System.out.printf(
            "%s (%s)%n",
            meta.getColumnLabel(i),
            meta.getColumnTypeName(i));
}
```

The important methods are:

| Method | Meaning |
|---|---|
| `getColumnCount()` | Number of returned columns |
| `getColumnName(i)` | Underlying database column name when available |
| `getColumnLabel(i)` | SQL alias/label; usually best for displaying a result |
| `getColumnType(i)` | JDBC type constant from `java.sql.Types` |
| `getColumnTypeName(i)` | Database type name |

Metadata is useful in generic query tools, exporters, or diagnostics. Normal DAO mapping already knows the expected columns, so it is not daily CRUD knowledge.

---

## 21. DatabaseMetaData

> Priority: ⭐⭐⭐ NICE TO KNOW  
> Source: **PDF foundation**

`DatabaseMetaData` describes the connected database, driver, schema objects, and supported capabilities.

```java
DatabaseMetaData meta = connection.getMetaData();

System.out.println(meta.getDatabaseProductName());
System.out.println(meta.getDatabaseProductVersion());
System.out.println(meta.getDriverName());
System.out.println(meta.getDriverVersion());
```

```text
DatabaseMetaData
→ PostgreSQL/driver identity and capabilities

ResultSetMetaData
→ columns of one query result
```

Schema discovery APIs can be useful for tools and frameworks, but you do not need to memorize them before CRUD and transactions.

---

## 22. ResultSet Type and Concurrency

> Priority: ⭐⭐ LEARN LATER  
> Source: **PDF foundation**, deliberately deprioritized

Statements can request a result-set cursor type:

| Type | Meaning |
|---|---|
| `TYPE_FORWARD_ONLY` | Move forward with `next()`; normal/default practical style |
| `TYPE_SCROLL_INSENSITIVE` | Move backward/forward; generally does not reflect later database changes |
| `TYPE_SCROLL_SENSITIVE` | Intended to reflect some later changes; support/behavior varies |

They can also request concurrency:

| Mode | Meaning |
|---|---|
| `CONCUR_READ_ONLY` | Read rows but do not update through the result set |
| `CONCUR_UPDATABLE` | Attempt updates through result-set methods when driver/query supports it |

Example syntax to recognize:

```java
Statement statement = connection.createStatement(
        ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
```

> These are useful JDBC concepts but not something to spend much study time on before real CRUD development.

Prefer a normal forward-only, read-only result set and explicit `UPDATE ... WHERE ...` through `PreparedStatement`. Explicit SQL is clearer, easier to review, and more predictable across queries/drivers.

---

## 23. DataSource

> Priority: ⭐⭐⭐⭐ IMPORTANT conceptually  
> Source: **Real-world addition**

`javax.sql.DataSource` is a standard Java connection-factory abstraction.

```text
DriverManager
→ call a static method with URL/credentials
→ simple and direct for learning/tools

DataSource
→ configured object that supplies connections
→ commonly preferred in larger/server applications
```

Application code asks:

```java
try (Connection connection = dataSource.getConnection()) {
    // JDBC work
}
```

Why it helps:

- Connection creation configuration has one owner.
- Callers depend on an interface instead of repeating URLs/credentials.
- A `DataSource` can be supplied by an application server or pool.
- Test/deployment configuration can provide another implementation.

Important distinction:

> A `DataSource` does not automatically mean connections are pooled.

Some data sources create a physical connection on every call; a pooling library can expose a pooling `DataSource`. pgJDBC also provides vendor data-source implementations, but using them directly introduces a compile-time vendor dependency.

For concrete names, pgJDBC's `PGSimpleDataSource` is nonpooling. Its old `PGPoolingDataSource` is deprecated and not recommended; a larger application should use a maintained pool such as HikariCP (or its runtime/container's pool) behind the `DataSource` abstraction.

**Mental model:** `DataSource` is the connection vending machine interface; pooling describes how the machine manages/reuses its inventory.

---

## 24. Connection Pooling

> Priority: ⭐⭐⭐⭐ IMPORTANT conceptually; tuning—⭐⭐ LEARN LATER  
> Source: **Real-world addition**

Opening a physical database connection can require TCP setup, authentication, and session initialization. Repeating that for every server request is inefficient:

```text
request
  ↓
open brand-new physical DB connection
  ↓
query
  ↓
close physical connection
```

A pool keeps a controlled set of reusable physical connections:

```text
Connection Pool
├── Physical Connection 1
├── Physical Connection 2
├── Physical Connection 3
└── ... bounded maximum
```

Application flow:

```text
borrow logical connection
      ↓
use it briefly
      ↓
close it
      ↓
pool resets/returns physical connection for reuse
```

That is why try-with-resources is still essential with a pool. `close()` normally means **return**, not destroy.

Real server applications commonly use a pool such as HikariCP, an application-server pool, or another maintained implementation. The mini project intentionally uses `DriverManager` so the raw JDBC flow remains visible.

Pool concepts worth understanding now:

- maximum size protects the database from unlimited sessions;
- callers may wait when all connections are borrowed;
- acquisition/query timeouts prevent indefinite waiting;
- leaked logical connections eventually exhaust the pool;
- connections must be returned with clean state, especially auto-commit/read-only/isolation.

Exact pool sizing, leak detection thresholds, lifetime tuning, and monitoring are ⭐⭐ LEARN LATER.

---

## 25. Configuration and Secrets

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW habit  
> Source: **Real-world addition**

This is unsafe in committed source:

```java
String password = "mypassword";
```

Problems include source-history exposure, environment-specific rebuilds, accidental logs/screenshots, and credentials copied across machines.

Configuration options form a maturity ladder:

```text
learning project
→ local ignored properties file

simple deployed application
→ environment variables / protected external config

managed production
→ secret-management service + rotated credentials
```

For the mini project:

```properties
db.url=jdbc:postgresql://localhost:5432/jdbc_practice
db.username=jdbc_app
db.password=change_me_local_only
```

Keep the real file out of version control and commit only an example template:

```gitignore
target/
src/main/resources/database.properties
```

`.gitignore` prevents Git exposure, not artifact exposure. Maven normally copies
`src/main/resources/database.properties` into `target/classes` and packages it in
the JAR, so do not distribute a learning artifact that contains real credentials.

The project solution lets environment variables override file values:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Production rules:

- never log passwords or complete credential-bearing URLs;
- give the database role only required privileges;
- use different credentials per environment;
- rotate exposed secrets rather than merely deleting them from the latest commit;
- secure transport and secret storage according to deployment policy.

**Remember:** Configuration is input to the application, not source code behavior.

---

## 26. DAO / Repository Structure

> Priority: ⭐⭐⭐⭐ IMPORTANT  
> Source: **Real-world addition**

A small DAO keeps SQL and JDBC mechanics out of console/UI logic.

```text
Main
 ↓ calls methods
StudentDAO
 ↓ uses JDBC
PreparedStatement / ResultSet
 ↓
PostgreSQL
```

```java
public class StudentDAO {
    public long create(Student student) throws SQLException { ... }

    public Student findById(long id) throws SQLException { ... }

    public List<Student> findAll() throws SQLException { ... }

    public boolean update(Student student) throws SQLException { ... }

    public boolean delete(long id) throws SQLException { ... }
}
```

Responsibilities remain simple:

| Class | Responsibility |
|---|---|
| `Main` | Input/output and application flow |
| `Student` | Java representation of a student |
| `StudentDAO` | Student SQL, parameter binding, execution, row mapping |
| `DatabaseConfig` | URL/credentials and connection creation for this learning project |

Do not put all SQL in `main()`: it becomes difficult to read, reuse, test, and wrap in meaningful operations.

### Transaction-boundary warning

DAO methods that each open a new connection cannot automatically form one transaction together. A multi-DAO business operation needs a service/transaction method that obtains one connection and passes that same connection to all participating JDBC operations.

```text
TransferService owns Connection + transaction
       ├── debit using that Connection
       └── credit using that Connection
```

This is why the mini project's transfer example uses a small service rather than calling two independent DAO methods that each create a connection.

**Remember:** DAO separates persistence mechanics; transaction ownership follows the whole business unit of work.

---

## 27. Common JDBC Mistakes

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **Real-world addition and modernization summary**

| Mistake | Why it fails | Correct habit |
|---|---|---|
| Concatenate user values into SQL | SQL injection, quoting/type bugs | Bind through `PreparedStatement` |
| Call `ps.executeQuery(sql)` after preparing | Prepared SQL should not be supplied again | Call `ps.executeQuery()`/`ps.executeUpdate()` |
| Start parameter/column index at 0 | JDBC indexes are 1-based | Begin at 1 or use column labels |
| Read `ResultSet` before `next()` | Cursor is before first row | Check `next()` first |
| Use `executeQuery` for updates | Wrong contract/result type | Use `executeUpdate()` |
| Ignore affected-row count | Cannot distinguish changed/missing row | Interpret the returned count |
| Forget generated-key request | `getGeneratedKeys()` may be empty | Use `RETURN_GENERATED_KEYS` when preparing |
| Leak result/statement/connection | Exhausts server/pool resources | Use try-with-resources |
| Swallow `SQLException` | Failure disappears; data may be wrong | Recover, log safely, or propagate |
| Roll back on another connection | Does not undo original work | One connection per transaction |
| Forget auto-commit behavior | Multi-step operation partially commits | Disable it before the transaction |
| Assume a batch is atomic | Partial success may be possible | Use an explicit transaction when required |
| Use `double` for money | Binary floating-point is not exact decimal | Use `BigDecimal`/`NUMERIC` |
| Commit credentials | Exposes secrets/history | External/ignored configuration |
| Share one connection across threads | Mutable session/transaction state conflicts | Borrow/own per unit of work |
| Treat `isClosed()` as a health check | An open flag does not prove server reachability | Handle operation failures/use `isValid` or pool validation |
| Close with an unfinished transaction | Commit/rollback outcome is not a safe implicit policy | Explicitly commit or roll back first |
| Return live `ResultSet` from DAO | Depends on resources that should close | Map rows to Java objects inside DAO |
| Use `SELECT *` in stable DAO queries | Hidden schema/order coupling | Select explicit columns |
| Add `Class.forName` automatically | Obsolete noise in normal JDBC 4+ setup | Rely on modern driver discovery |
| Copy JARs into JRE/IDE folders | Non-reproducible historical setup | Declare Maven dependency |
| Assume Maven's thin JAR contains pgJDBC | Runtime driver can be missing | Build/provide the runtime classpath |

When debugging, reduce the flow one boundary at a time:

```text
server running?
→ URL/credentials correct?
→ driver resolved at runtime?
→ connection opens?
→ SQL works in psql?
→ parameter order/types correct?
→ cursor advanced?
→ transaction committed?
```

---

## 28. Real-World JDBC Flow

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW  
> Source: **Integrated foundation + real-world additions**

```text
Java Application
      ↓ calls DAO/service
StudentDAO / transaction service
      ↓ obtains a connection through one configured path

external config → DatabaseConfig → DriverManager
or
external config → pooled DataSource

      ↓ both paths supply
Connection
      ↓ prepares SQL template
PreparedStatement
      ↓ binds typed values separately
SQL + parameters
      ↓ through pgJDBC and PostgreSQL protocol
PostgreSQL
      ├── write → int affected-row count
      ├── generated key → generated-keys ResultSet
      └── query rows → query ResultSet
                         ↓ cursor + getters
                      row mapping
                         ↓
                      Java Objects
                         ↓
                  Main/application logic
```

Cross-cutting responsibilities surround the flow:

```text
try-with-resources → guarantees timely release
SQLException       → reports failure details
transaction        → makes related statements atomic
configuration      → supplies endpoint/credentials safely
connection pool    → reuses connections in server applications
```

For a read:

```text
connection → prepare SELECT → bind → executeQuery
→ while/if rs.next() → map row → close resources
```

For a write:

```text
connection → prepare INSERT/UPDATE/DELETE → bind
→ executeUpdate → inspect count/key → commit if explicit transaction
→ close resources
```

**Final mental rule:** Correct JDBC is not just “run SQL.” It is SQL + types + resources + errors + transaction boundaries.

---

## 29. What You Must Memorize

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW
> Source: **Synthesis of PDF foundation + real-world additions**

Memorize this 80/20 core:

1. PostgreSQL Maven coordinate:

   ```text
   org.postgresql:postgresql:<chosen-version>
   ```

2. URL shape:

   ```text
   jdbc:postgresql://host:5432/database
   ```

3. Main JDBC types:

   ```text
   Connection
   PreparedStatement
   ResultSet
   SQLException
   ```

4. Execution choice:

   ```text
   SELECT                → executeQuery()  → ResultSet
   INSERT/UPDATE/DELETE  → executeUpdate() → row count
   ```

5. Parameters and result indexes begin at **1**.
6. Bind untrusted values; never concatenate them into SQL.
7. Advance a `ResultSet` with `next()` before reading.
8. Map rows to Java objects inside the DAO.
9. Put JDBC resources in try-with-resources.
10. Inspect `SQLException` message and SQLState; do not swallow it.
11. Multi-step atomic work uses the same connection with:

    ```text
    setAutoCommit(false) → statements → commit
                                      ↘ failure → rollback
    ```

12. Request/read generated keys explicitly.
13. `addBatch()` collects parameter sets; `executeBatch()` runs them.
14. Keep real credentials outside committed Java source.

---

## 30. What You Only Need to Understand

> Priority: ⭐⭐⭐⭐ IMPORTANT
> Source: **Synthesis of PDF foundation + real-world additions**

Understand these without memorizing implementation details:

| Concept | Enough for now |
|---|---|
| Driver discovery | Modern JDBC finds pgJDBC when its JAR is on the runtime classpath |
| Type 4 driver | Pure Java driver speaks PostgreSQL's protocol directly |
| `DataSource` | Standard connection factory abstraction; pooling is separate |
| Pooling | Borrow, use briefly, close/return; physical connections are reused |
| Metadata | Describes result columns or database/driver capabilities |
| Batch counts | May contain exact counts or JDBC status constants |
| SQLState | Five-character failure classification; `23505` is unique violation in PostgreSQL |
| Auto-commit | Each independent statement commits automatically unless disabled |
| Thin JAR | Maven dependency declaration does not automatically embed pgJDBC |
| DAO structure | Keeps SQL/resource/mapping code separate from console logic |

---

## 31. What You Can Learn Later

> Priority: ⭐⭐ LEARN LATER
> Source: **Mixed deferred foundation topics + real-world additions**

- Stored procedures and `CallableStatement`
- Scroll-sensitive/updatable result sets
- Advanced metadata/schema discovery
- Transaction isolation in depth, anomalies, savepoints, and retry strategies
- XA and distributed transactions
- Advanced connection-pool sizing/tuning/metrics
- PostgreSQL-specific extensions and custom types
- Large objects, streaming, and fetch-size tuning for huge results
- SSL/TLS and certificate deployment details
- Advanced batch rewrite/performance tuning
- Framework abstractions such as Spring JDBC, JPA, or Hibernate—after plain JDBC is comfortable

Recognize these names, but do not delay practical CRUD work to master them.

---

## 32. Final Cheat Sheet

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW
> Source: **Synthesis of PDF foundation + real-world additions**

### Connect

```java
try (Connection con = DriverManager.getConnection(url, user, password)) {
    // work
}
```

### Query one/many

```java
try (PreparedStatement ps = con.prepareStatement(
        "SELECT id, name FROM students WHERE age >= ?")) {
    ps.setInt(1, minimumAge);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            long id = rs.getLong("id");
            String name = rs.getString("name");
        }
    }
}
```

### Write

```java
try (PreparedStatement ps = con.prepareStatement(
        "UPDATE students SET age = ? WHERE id = ?")) {
    ps.setInt(1, age);
    ps.setLong(2, id);
    boolean updated = ps.executeUpdate() == 1;
}
```

### Generated key

```text
prepare with Statement.RETURN_GENERATED_KEYS
→ executeUpdate()
→ getGeneratedKeys()
→ keys.next()
→ keys.getLong(1)
```

### Transaction

```text
setAutoCommit(false)
→ execute all related operations on same connection
→ commit on success
→ rollback on failure
→ restore connection state
```

### Diagnostic table

| Symptom | First check |
|---|---|
| No suitable driver | pgJDBC on runtime classpath and correct URL prefix |
| Connection refused | Server/host/port/listening status |
| Authentication failed | Role/password/`pg_hba.conf` policy |
| Database not found | URL database name |
| Relation not found | Connected database/schema/table creation |
| Duplicate email | SQLState `23505` and unique constraint |
| Parameter error | Count/order; indexes begin at 1 |
| ResultSet cursor error | Call/check `next()` before getters |
| Missing runtime class | Thin-JAR/runtime classpath packaging |

### One-line rules

```text
values → bind them
rows → advance then map them
resources → try-with-resources
multiple atomic writes → one connection + transaction
errors → inspect SQLState and preserve cause
server application → DataSource + maintained pool
secrets → external configuration
```

---

## 33. Self-Review Checklist

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW
> Source: **Synthesis of PDF foundation + real-world additions**

### Foundation

- [ ] I can draw Java → JDBC API → pgJDBC → PostgreSQL.
- [ ] I know which parts are standard Java interfaces and which part is vendor-specific.
- [ ] I can add the PostgreSQL driver with Maven.
- [ ] I can break down a PostgreSQL JDBC URL.
- [ ] I understand modern automatic driver discovery.
- [ ] I can open and close a `Connection`.

### Daily JDBC work

- [ ] I choose `PreparedStatement` for parameterized SQL.
- [ ] I can explain why binding prevents values from becoming SQL syntax.
- [ ] I know parameter indexes begin at 1.
- [ ] I choose `executeQuery()` for `SELECT`.
- [ ] I choose `executeUpdate()` for `INSERT`, `UPDATE`, and `DELETE`.
- [ ] I inspect the affected-row count.
- [ ] I call `rs.next()` before reading columns.
- [ ] I can use labels and typed getters.
- [ ] I can map a row to a Java object.
- [ ] I use try-with-resources for connections, statements, and result sets.
- [ ] I can inspect a `SQLException` message and SQLState.

### Real-world essentials

- [ ] I can implement CRUD in a DAO.
- [ ] I can retrieve a generated primary key.
- [ ] I can build and execute a prepared batch.
- [ ] I know a batch is not automatically my atomicity policy.
- [ ] I can create a multi-statement transaction on one connection.
- [ ] I commit only after every required operation succeeds.
- [ ] I roll back after failure without discarding the original exception.
- [ ] I understand `DataSource` conceptually.
- [ ] I can explain connection borrowing and returning in a pool.
- [ ] I keep credentials out of committed source code.
- [ ] I know a normal Maven JAR may still need runtime dependency packaging/classpath setup.

Explain this final flow aloud:

```text
Java Application
      ↓
JDBC API
      ↓
PostgreSQL JDBC Driver
      ↓
Connection / DataSource
      ↓
PreparedStatement + bound parameters
      ↓
PostgreSQL executes SQL
      ↓
row count / generated key / ResultSet
      ↓
Java object or application result
      ↓
commit or rollback when required
      ↓
resources close automatically
```

If every arrow has a clear reason, you have the practical JDBC foundation needed to complete the mini project.

### Official references

> Priority: ⭐⭐⭐ NICE TO KNOW

- [pgJDBC download and current version](https://jdbc.postgresql.org/download/)
- [Java 17 `java.sql` API](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/package-summary.html)
- [pgJDBC connection setup](https://jdbc.postgresql.org/documentation/use/)
- [pgJDBC queries and `PreparedStatement`](https://jdbc.postgresql.org/documentation/query/)
- [pgJDBC data sources and pooling](https://jdbc.postgresql.org/documentation/datasource/)
- [PostgreSQL documentation](https://www.postgresql.org/docs/current/)
