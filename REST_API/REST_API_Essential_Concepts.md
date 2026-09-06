# REST API Essential Concepts

> A first-principles guide for a Java developer who has finished Spring Boot and is now learning how an application communicates over HTTP.

**Example baseline:** Java 17+, Spring Boot 4.1.1, Spring MVC, JSON, and the JDBC stack you already know.  
**Companion exercise:** [Book Catalog REST API mini-project](REST_API_Mini_Project.md).  
**Learning goal:** Explain what happens between a client sending a request and receiving a response, then design and implement a small, predictable HTTP API.

You already know how to build an application with beans, services, repositories, `JdbcTemplate`, and a Boot-managed `DataSource`. REST development introduces an external boundary: callers can now reach your application through a protocol, without sharing your JVM or Java types.

The task is to translate between two contracts:

```text
Outside the application                    Inside the application
HTTP method, URL, headers, JSON     <-->    Java arguments and return values
HTTP status and response body      <-->    application outcomes
```

Keep the previous [Spring Boot guide](../Spring_Boot/Spring_Boot_Essentials.md) available as a reference. This guide builds on it rather than repeating bean lifecycle, Maven, SQL, or connection-pool lessons.

## How to study

1. Read a concept and predict the HTTP behavior in its example.
2. Answer each checkpoint before opening the answer.
3. Follow one request through the complete application flow.
4. Build the companion exercise incrementally.
5. Inspect status, headers, body, and database state when verifying an endpoint.

Code snippets here isolate individual concepts; they are not files to assemble unchanged. Controller fragments assume a constructor-injected `BookService bookService` and the necessary imports. Required-value checks and duplicate-conflict handling are omitted from these small examples and supplied in full by the companion. The service returns domain values; the controller maps them to response DTOs and HTTP outcomes.

### Priorities

| Priority | Meaning | What you should be able to do |
|---|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Required for a first REST application | Explain and implement it |
| ⭐⭐⭐⭐ IMPORTANT | Needed for predictable API behavior | Apply it and recognize mistakes |
| ⭐⭐⭐ NICE TO KNOW | Useful surrounding web knowledge | Recognize it and know when it matters |
| ⭐⭐ FUTURE KNOWLEDGE | Later roadmap topics | Understand its purpose without implementing it here |

### Navigation

- [1. APIs, the web, and REST](#1-apis-the-web-and-rest)
- [2. HTTP messages and stateless communication](#2-http-messages-and-stateless-communication)
- [3. HTTP methods](#3-http-methods)
- [4. Status codes](#4-status-codes)
- [5. Resource and URI design](#5-resource-and-uri-design)
- [6. JSON and Java representations](#6-json-and-java-representations)
- [7. Spring Boot's web infrastructure](#7-spring-boots-web-infrastructure)
- [8. DTOs and application boundaries](#8-dtos-and-application-boundaries)
- [9. Core Spring annotations](#9-core-spring-annotations)
- [10. Returning responses](#10-returning-responses)
- [11. Trace one request completely](#11-trace-one-request-completely)
- [12. Calling and inspecting an API](#12-calling-and-inspecting-an-api)
- [13. Failures and troubleshooting](#13-failures-and-troubleshooting)
- [14. Useful surrounding concepts](#14-useful-surrounding-concepts)
- [15. What comes later](#15-what-comes-later)
- [16. Readiness review](#16-readiness-review)

---

## 1. APIs, the web, and REST

**⭐⭐⭐⭐⭐ MUST KNOW**

### 1.1 Client and server

A **client** initiates an interaction. A **server** receives requests and provides a service. These are roles in an interaction, not necessarily different types of computers.

For the book catalog:

```text
curl / PowerShell / browser / mobile app
                  |
                  | request: give me book 10
                  v
         Spring Boot application
                  |
                  | response: a representation of book 10
                  v
                client
```

Your Java application is an HTTP server to the browser, but a database client to PostgreSQL. A backend can also be an HTTP client when it calls another backend.

The client does not invoke your controller as a Java method. It sends a message. Spring interprets that message and invokes a Java method inside the server process.

### 1.2 API, Web API, and HTTP API

An **API**, or application programming interface, is a contract through which software offers capabilities to other software. You have already used APIs: `List.add(...)`, `DataSource.getConnection()`, and `JdbcTemplate.query(...)`.

A **Web API** offers capabilities over web technologies. An **HTTP API** specifically communicates using HTTP requests and responses.

A caller of an HTTP API needs the public contract, not your Java implementation:

- where to send the request;
- which method to use;
- which headers and body to send;
- what successful and failed responses mean.

A Python client, a browser, and another Java service can all use the same JSON HTTP API. None needs the server's `BookService` class on its classpath.

### 1.3 What REST means

**REST** means *Representational State Transfer*. It is an architectural style: a set of constraints for organizing interactions in a distributed system. It is not a Java library, a JSON format, or an annotation.

Its constraints are:

| Constraint | First mental model |
|---|---|
| Client-server separation | A client uses a public interface independently of server storage and implementation |
| Stateless interactions | Each request carries the context needed to understand it |
| Cacheability | Responses indicate whether they can be reused |
| Uniform interface | Resources and their representations follow shared interaction rules |
| Layered system | Intermediaries such as proxies can sit between client and origin server |
| Code on demand, optional | A server can supply executable client code; this is not needed for our JSON API |

The uniform interface includes resource identification, manipulation through representations, self-descriptive messages, and hypermedia guiding available transitions. Hypermedia means the response can tell a client which related resources or next actions are available through links and their meaning. Recognize this requirement; implementing hypermedia infrastructure is outside this exercise. [Fielding's REST definition](https://ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm)

For example, a representation could include:

```json
{
  "id": 10,
  "title": "Learning HTTP",
  "links": [
    { "rel": "self", "href": "/books/10" },
    { "rel": "collection", "href": "/books" }
  ]
}
```

This is an illustrative link shape, not a claim that an arbitrary `links` property automatically makes an API REST-compliant.

### 1.4 REST API versus HTTP API

An HTTP API can expose commands such as `POST /calculateShipping`. Using HTTP does not by itself establish all REST constraints.

In everyday backend work, “REST API” often means a resource-oriented HTTP API with meaningful methods and statuses, frequently using JSON. Our book exercise uses that familiar scope. It practices the important HTTP and resource-design foundations, without claiming complete hypermedia-driven REST.

Keep these distinctions:

```text
HTTP = communication protocol
REST = architectural constraints
JSON = representation format
Spring MVC = server-side framework used to implement an HTTP interface
```

REST does not require JSON. A resource can have JSON, HTML, XML, image, or other representations when the contract supports them.

### 1.5 Resources and representations

A **resource** is a concept exposed by the API: a book, a collection of books, or a particular author's books.

A **representation** is data sent to describe a resource at a point in time.

```text
Resource: book with identity 10
URI:      /books/10
JSON:     {"id":10,"isbn":"9780000000011","title":"Learning HTTP","author":"Mira Tran","price":19.95,"stock":4}
```

The JSON text is not the resource itself. Updating the title changes its representation without necessarily changing its identity.

A resource is also not automatically a database table. An API may combine data from several tables, omit internal columns, or expose a calculated report with no corresponding table.

### 1.6 URI, URL, and endpoint

A **URI** identifies a resource. A **URL** is a URI that also describes how to locate it, for example:

```text
http://localhost:8080/books/10
```

`/books/10` is commonly called a resource URI or path in API discussions; strictly, it is a relative URI reference until combined with a base URL.

An **endpoint** is an exposed interaction point. For API design, identify an operation with both method and route:

```text
GET    /books/10  -> read book 10
DELETE /books/10  -> remove book 10
```

These share a resource path but have different behavior. A URL alone does not identify which action you requested.

### Checkpoint 1

Does a `@RestController` returning JSON prove that the entire application satisfies REST? Does adding PostgreSQL make it stateful in the forbidden REST sense?

<details>
<summary>Answer</summary>

No to both. The annotation configures Spring response handling; REST describes the larger interaction architecture. Database resource state is expected. The stateless constraint concerns dependence on a hidden, server-stored client conversation between requests.

</details>

---

## 2. HTTP messages and stateless communication

**⭐⭐⭐⭐⭐ MUST KNOW**

### 2.1 Request anatomy

Consider this complete URL:

```text
http://localhost:8080/books?author=Mira%20Tran&page=0&size=20
|      |         |   |     |
scheme host      port path query string
```

The query string starts after `?`. It contains named values separated by `&`. `%20` encodes a space. A fragment such as `#chapter2` belongs to client-side navigation and is not sent as part of the HTTP request target.

A readable HTTP/1.1 request could look like this:

```http
GET /books?author=Mira%20Tran&page=0&size=20 HTTP/1.1
Host: localhost:8080
Accept: application/json

```

The blank line separates headers from an optional body. This GET request has no body.

| Request part | Example | Responsibility |
|---|---|---|
| Method | `GET` | Requested operation semantics |
| Request target | `/books?...` | Resource path and query |
| Host / authority | `localhost:8080` | Server being addressed |
| Headers | `Accept: application/json` | Message metadata and preferences |
| Body | A JSON document for a POST | Representation or operation input |

HTTP/2 and HTTP/3 use different wire framing. The method, target, headers, status, and representation mental model still applies; these examples use readable HTTP/1.1 notation.

A request that creates a book has a body:

```http
POST /books HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
Content-Length: 93

{"isbn":"9780000000011","title":"Learning HTTP","author":"Mira Tran","price":19.95,"stock":4}
```

The Content-Length above counts the UTF-8 bytes of that exact ASCII body, with no trailing newline. A real HTTP client handles framing for you. Body-framing headers are omitted from subsequent teaching examples; you do not manually calculate Content-Length in a Spring controller.

### 2.2 Response anatomy

```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /books/10

{"id":10,"isbn":"9780000000011","title":"Learning HTTP","author":"Mira Tran","price":19.95,"stock":4}
```

| Response part | Meaning in this example |
|---|---|
| Status | A resource was successfully created |
| Headers | The body is JSON; the created resource is at `/books/10` |
| Body | The server's representation of the created book |

The client reads all three. A correct JSON body with an incorrect status is still an incorrect API response.

Some valid responses have no body:

```http
HTTP/1.1 204 No Content

```

Do not append JSON, a message string, or `null` to a 204 response.

### 2.3 `Content-Type` and `Accept`

`Content-Type` describes the representation **contained in this message**. A request with a JSON body says `Content-Type: application/json`. A response with JSON also says `Content-Type: application/json`.

`Accept` is the client's preference for the **response representation**: `Accept: application/json` asks the server to return JSON.

```text
Request Content-Type -> What am I sending you?
Request Accept       -> What can I receive back?
Response Content-Type-> What did the server actually send?
```

Setting `Accept` does not turn a form-encoded request body into JSON. Setting `Content-Type` does not validate JSON syntax. Header names are case-insensitive; application field names and URI paths should not be assumed to be.

### 2.4 Request-response lifecycle

```text
1. Client constructs request
2. Client connects to the server, or reuses a connection
3. Server receives and routes the request
4. Application executes the requested behavior
5. Server writes status, headers, and optional body
6. Client interprets the response
```

One network connection can carry multiple requests. A connection being reused does not mean that the API depends on a server-side conversation.

A failed network call also does not prove that the server performed no work. If PostgreSQL commits an insert and the response is lost, the client sees uncertainty even though the row exists. This is why retry behavior and idempotency matter.

### 2.5 HTTP and HTTPS

HTTPS uses HTTP over a secured transport based on TLS. Conceptually, it protects messages against reading and modification in transit and enables the client to authenticate the server through its certificate.

Your controller still receives an HTTP request with a method, path, headers, and body. A local exercise can use `http://localhost:8080`; configuring certificates and public deployment belongs later in the roadmap.

HTTPS does not decide whether a user is allowed to delete a book. Transport security and application authorization solve different problems.

### 2.6 Stateless requests and persistent resource state

Consider an inconvenient conversation:

```text
Request 1: select book 10
Server remembers a currentBookId for this client
Request 2: update the selected book's stock to 3
```

The second request cannot be interpreted without hidden knowledge of the first.

Our API instead sends:

```http
PATCH /books/10 HTTP/1.1
Content-Type: application/json

{"stock":3}
```

The target and requested change are present together. The server may still read the existing book from PostgreSQL, enforce rules, write a change, and log the outcome.

Statelessness does not mean no database, no cache, no logs, no users, or no authentication. It concerns the client conversation needed to understand a request. Likewise, a stateless controller can depend on a stateful persistence system.

### Checkpoint 2

For `POST /books` with JSON, what happens if the request says `Content-Type: text/plain` and `Accept: application/json`?

<details>
<summary>Answer</summary>

The response preference is JSON, but the request declares plain text. An endpoint that consumes only JSON can reject it with 415 before the controller body executes. The Accept header does not repair the request's media type.

</details>

---

## 3. HTTP methods

**⭐⭐⭐⭐⭐ MUST KNOW**

HTTP methods express semantics. Choosing an annotation only routes a request; your implementation must preserve the intended meaning.

### 3.1 Safety and idempotency first

A **safe** method asks to read rather than change resource state. Logging, metrics, and other incidental server work do not make a read unsafe. A GET endpoint that deletes a book is unsafe despite its annotation.

An **idempotent** method has the same intended effect when an identical request is applied repeatedly as when it is applied once. This concerns the requested effect, not identical response bytes, status codes, logging events, or execution cost. GET, PUT, and DELETE have idempotent semantics; POST has no such general guarantee. [HTTP method properties](https://httpwg.org/specs/rfc9110.html#method.properties)

```text
Safe:       Did the client request a state change?
Idempotent: Does repeating this request compound its intended effect?
```

A read is safe and idempotent. Replacing a title is unsafe but can be idempotent. Creating a new generated-ID book with every retry is unsafe and non-idempotent.

### 3.2 The five main methods

| Method | Intended use | Request body in this API | Typical success | Safe? | Idempotent semantics? |
|---|---|---|---|---|---|
| GET | Retrieve a representation | None | 200 with object or array | Yes | Yes |
| POST | Ask the target resource to process input; often create under a collection | Usually JSON | 201 plus Location for creation | No | Not guaranteed |
| PUT | Create or replace state at a client-known target URI | Full writable representation | 200/204 for replacement; 201 if creation is supported | No | Yes |
| PATCH | Apply a specified partial change | Patch document | 200 or 204 | No | Depends on the operation and format |
| DELETE | Remove the target resource's association/availability | None | 204, or 200 with a result document | No | Yes |

These are design semantics, not a promise that every server implements them correctly. For GET and DELETE, request bodies have no generally defined meaning. Do not use them as the input mechanism in this exercise. The standard defines PATCH separately. [PATCH specification](https://www.rfc-editor.org/rfc/rfc5789.html)

### 3.3 GET: read

```http
GET /books HTTP/1.1
Accept: application/json
```

```json
[
  { "id": 10, "isbn": "9780000000011", "title": "Learning HTTP", "author": "Mira Tran", "price": 19.95, "stock": 4 }
]
```

```http
GET /books/10 HTTP/1.1
Accept: application/json
```

The collection response is an array. The individual-resource response is one object. An existing collection with no matching books returns `200` and `[]`; an individual book that does not exist normally returns `404`.

Repeated GET responses may differ because another client changed the book. GET remains idempotent: the read itself did not request a cumulative state change.

### 3.4 POST: create under a collection

```http
POST /books HTTP/1.1
Content-Type: application/json

{"isbn":"9780000000011","title":"Learning HTTP","author":"Mira Tran","price":19.95,"stock":4}
```

The client knows the collection but does not choose the generated book ID. The server inserts the book and returns `201`, a `Location` header, and usually the created representation.

Without a duplicate-prevention rule, two identical POST requests may create books 10 and 11. In the companion, ISBN is a unique catalog key, so repeating the same valid creation after it succeeds produces 409 instead of a second row. That behavior comes from our constraint and conflict handling, not from POST semantics. Later, an API may define an idempotency-key contract that recognizes a retry and replays its result; an arbitrary header alone does nothing.

POST is broader than “SQL INSERT.” It asks the target to process the supplied representation according to that resource's contract.

### 3.5 PUT: replace the writable representation

Suppose book 10 is:

```json
{ "id": 10, "isbn": "9780000000011", "title": "Learning HTTP", "author": "Mira Tran", "price": 19.95, "stock": 4 }
```

The client sends:

```http
PUT /books/10 HTTP/1.1
Content-Type: application/json

{"isbn":"9780000000011","title":"HTTP in Practice","author":"Mira Tran","price":21.50,"stock":7}
```

Our contract replaces **all writable fields**: `isbn`, `title`, `author`, `price`, and `stock`. The server-owned ID stays 10. Server-controlled metadata, if introduced later, does not become client-writable merely because PUT is used.

Sending only `{"stock":7}` is not a valid full replacement for this contract. We reject an incomplete PUT instead of silently treating it as a partial update. APIs must define what absence means; this exercise requires each writable field.

A general HTTP API may allow PUT to create a resource at the specified URI. Our book API deliberately supports replacement of an existing book only, returning 404 if it is missing. This is an endpoint design choice, not the universal definition of PUT.

Repeating the full request leaves the same requested values. That is the idempotency you should preserve.

### 3.6 PATCH: change selected state

Our small exercise defines one custom JSON patch shape:

```http
PATCH /books/10 HTTP/1.1
Content-Type: application/json

{"stock":3}
```

Meaning: **set the existing book's stock to 3; leave ISBN, title, author, and price unchanged**.

This is a documented, application-specific partial-update contract. It is not JSON Patch (`application/json-patch+json`) and not JSON Merge Patch (`application/merge-patch+json`). A PATCH annotation does not select or implement either standard format for you.

Compare two possible operations:

| Patch meaning | Start | Once | Twice | Idempotent? |
|---|---:|---:|---:|---|
| Set stock to 3 | 8 | 3 | 3 | Yes, for this operation |
| Decrease stock by 3 | 8 | 5 | 2 | No |

PATCH itself does not guarantee idempotency. Its format and operation determine that property. A patch must be applied as a unit: a server should not expose half of a multi-change patch as if it succeeded. Our one-column update keeps the exercise small. [PATCH semantics and atomic application](https://www.rfc-editor.org/rfc/rfc5789.html#section-2)

### 3.7 DELETE: remove availability

```http
DELETE /books/10 HTTP/1.1
```

Our contract returns `204` when a book is deleted and `404` when it is already absent.

```text
First request:  exists -> absent -> 204
Second request: absent -> absent -> 404
```

The response codes differ, but both leave the resource absent. DELETE is still idempotent. Another API could return 204 in both cases if its contract defines absence as success.

Deletion from the API does not necessarily mean every stored trace is erased. A real application may archive records; the public resource behavior and the storage strategy are separate decisions.

### 3.8 POST versus PUT, PUT versus PATCH

| Decision | Use this mental model |
|---|---|
| POST `/books` | “Process this new book; you assign its location.” |
| PUT `/books/10` | “Make the writable state at this known location match this full representation.” |
| PATCH `/books/10` | “Apply this documented change to the existing state.” |

Do not decide solely from the SQL statement. PUT and PATCH can both execute SQL `UPDATE`, but they make different promises to the client.

### Checkpoint 3

1. Can an idempotent method change the database?
2. Is PATCH always non-idempotent?
3. If DELETE returns 204 followed by 404, did the implementation violate idempotency?
4. A POST response times out. Is an automatic retry guaranteed to be harmless?

<details>
<summary>Answers</summary>

1. Yes. PUT and DELETE deliberately change state while preserving repeated-request semantics.
2. No. An absolute stock assignment can be idempotent; a stock decrement is not.
3. No. The intended final state remains “book absent.”
4. No. The first request may have committed before the response was lost, so the retry might create a duplicate.

</details>

---

## 4. Status codes

**⭐⭐⭐⭐⭐ MUST KNOW**

The status describes the outcome of the HTTP interaction. A Java method returning normally is not enough information to choose the right status; the controller must express whether the outcome was retrieval, creation, absence, rejection, or failure.

### 4.1 Families

| Family | General meaning | Early examples |
|---|---|---|
| 1xx | Interim information; a final response follows | 100 Continue |
| 2xx | Request succeeded | 200, 201, 204 |
| 3xx | Redirection or representation reuse | 301/302 redirects, 304 Not Modified |
| 4xx | Request cannot be fulfilled as sent, or access is not permitted | 400, 401, 403, 404 |
| 5xx | Server-side failure or unavailability | 500, 503 |

These families help a caller decide what kind of problem occurred. Individual codes carry more precise meaning.

### 4.2 Codes to recognize and choose

| Code | Practical meaning | Book API example or design note |
|---|---|---|
| **200 OK** | Successful request with an ordinary result | GET a book; return updated book after PUT/PATCH |
| **201 Created** | One or more resources were created | POST book; include `Location: /books/10` |
| **204 No Content** | Success with no response content | Successful DELETE; absolutely no JSON body |
| **400 Bad Request** | Invalid request syntax or unacceptable request input | Malformed JSON, nonnumeric ID, required input missing |
| **401 Unauthorized** | Valid authentication credentials are needed | Protected endpoint called without acceptable credentials |
| **403 Forbidden** | Server understands the request but refuses permission | Recognized user lacks permission to delete |
| **404 Not Found** | Resource is unavailable at this target | Missing book or no matching public route |
| **405 Method Not Allowed** | This target does not support that method | `POST /books/10` when only item reads/updates/deletion exist |
| **406 Not Acceptable** | No offered representation meets the response preference | JSON-only endpoint requested with `Accept: application/xml` |
| **409 Conflict** | Request conflicts with the resource's current state | A documented unique catalog key already exists |
| **415 Unsupported Media Type** | The request representation's media type is unsupported | Sending `text/plain` to a JSON-consuming endpoint |
| **422 Unprocessable Content** | Media type and syntax understood, but content instructions cannot be processed | An API deliberately distinguishes semantic rejection from malformed input |
| **500 Internal Server Error** | Unexpected server-side failure | A programming defect or an unhandled data-access failure |
| **503 Service Unavailable** | Temporary inability to serve requests | Deliberate maintenance/overload response; possibly `Retry-After` |

Use the standard semantics to guide these choices. A 401 response includes a suitable `WWW-Authenticate` challenge, while 405 includes `Allow` describing supported methods. 422 is not mandated for every business-rule failure. [HTTP status definitions](https://httpwg.org/specs/rfc9110.html#status.codes)

The mini-project makes 409 concrete with a unique ISBN catalog key. It translates that known constraint conflict locally; unexpected data-access failures are not all conflicts.

### 4.3 Common choices, explained

**200 versus 201:** A normal `@RestController` object return typically leads to 200. Creating a book does not automatically change that to 201 because the method is named `create` or annotated with `@PostMapping`. Select 201 explicitly.

**200 with `[]` versus 404:** `/books` denotes a collection even when empty. `/books/987654` denotes a specific book, which may not exist. Do not make a successful collection search look like a missing route.

**401 versus 403:** Despite its name, 401 is about authentication. 403 is refusal of permission. Authentication and authorization will be implemented in the Spring Security stage, not here.

**400 versus 422:** First decide on a documented input-error contract. Our introductory project uses 400 for its small set of manual input checks. That is a legitimate choice; introducing 422 requires a meaningful distinction and consistent use, not merely valid JSON syntax.

**500 versus 503:** A database exception is not automatically converted into 503 by Spring Boot. A known temporary outage might deliberately be translated to 503 in a more mature application. Unexpected unhandled exceptions commonly end in 500. Do not claim a retry is safe just because a status is 5xx.

### 4.4 Avoid these response contracts

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"success":false,"message":"Book not found"}
```

The body says failure while the HTTP status says success. Clients, monitoring, and caches generally cannot infer your private meaning for every body field.

Also avoid throwing a generic unchecked exception for every missing book. Expected absence is an application outcome you can translate to 404; a 500 should not be the routine way a caller learns an ID is missing.

### Checkpoint 4

Choose statuses for: an empty book list; one nonexistent ID; successful creation; wrong request media type; successful deletion without a result body; malformed JSON.

<details>
<summary>Answer</summary>

200 with `[]`; 404; 201 with Location; 415; 204 with no body; 400. Response-body details for errors depend on the configured error handling and are not the current learning contract.

</details>

---

## 5. Resource and URI design

**⭐⭐⭐⭐⭐ MUST KNOW** for paths and input placement. **⭐⭐⭐⭐ IMPORTANT** for collection options.

### 5.1 Nouns, collections, and individual resources

For this catalog:

```text
/books       collection of books
/books/10    individual book
```

Use resource nouns and let the HTTP method carry the common action. Prefer `GET /books` over `/getAllBooks` and `DELETE /books/10` over `/deleteBook?id=10`.

Plural names are a useful convention, not a wire-protocol requirement. Consistency matters more than debating singular versus plural on each route. Use readable lowercase paths and hyphens for multiword segments, for example `/reading-lists`.

Avoid exposing Java package names, controller class names, and database table names as if they were required URI structure. Renaming a repository should not force an API-breaking URL change.

### 5.2 Nested resources

```text
/authors/7/books        books related to author 7
/books/10/reviews       reviews belonging to book 10
/books/10/reviews/42    a particular review within that relationship
```

Nest when the relationship is meaningful to callers. Do not reproduce every foreign-key chain in a very long URL. If a review has a globally unique identity and can be addressed independently, `/reviews/42` may be a useful canonical route.

For a nested item route, verify that review 42 actually belongs to book 10. The presence of both path variables does not automatically enforce the relationship.

### 5.3 Where does each input belong?

| Place | Decision rule | Example | Spring binding |
|---|---|---|---|
| Path | Identifies the resource or meaningful parent relationship | `/books/10` | `@PathVariable("id")` |
| Query | Refines which representation/list is requested | `/books?author=Mira&page=0` | `@RequestParam` |
| Body | Contains a representation or structured change | `{"title":"...","stock":3}` | `@RequestBody` |
| Header | Carries message metadata, negotiation, or cross-cutting context | `Accept`, `Content-Type` | `@RequestHeader` when application code needs it |

The HTTP method is another part of the contract. A body of `{"action":"delete"}` is not needed when `DELETE /books/10` already expresses the operation.

Avoid putting secrets in query strings: URLs commonly appear in access logs and browser history. Authentication design comes later, but input placement already affects accidental disclosure.

### 5.4 Filtering, searching, and sorting

These are separate operations over a collection:

```text
/books?author=Mira%20Tran       filter by author
/books?q=http                  search according to a defined matching rule
/books?sort=title,asc           order the result
/books?author=Mira&sort=id,desc  combine a filter and sorting
```

The query names do not implement themselves. You must decide:

- whether an author match is exact, case-insensitive, or partial;
- which fields `q` searches;
- what the default order is;
- which sort fields and directions are allowed.

Pass values through SQL parameters, as you learned with JDBC. A column name in `ORDER BY` cannot safely be chosen by blindly concatenating user input. Map public sort keys to a small allowlist of known SQL fragments.

The mini-project implements an optional exact author filter and bounded page/size parameters, with a fixed ID-ascending order. Search and user-selected sorting here are design concepts, not routes promised by its reference code.

### 5.5 Basic pagination

Returning every row eventually becomes slow and produces unnecessarily large responses. Pagination returns a portion of the collection:

```text
GET /books?page=0&size=20
```

For this example convention:

```text
page 0 -> offset 0  -> first 20 rows
page 1 -> offset 20 -> next 20 rows
offset = page * size
```

`page` being zero-based is an API choice, not a rule of HTTP. Document defaults and a maximum size. Reject negative values and values outside your supported range.

A possible response is:

```json
{
  "items": [
    { "id": 10, "isbn": "9780000000011", "title": "Learning HTTP", "author": "Mira Tran", "price": 19.95, "stock": 4 }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

This is a possible explicit response DTO shape, not the companion's response contract. The companion deliberately returns a bare array with no total count; its page/size inputs still limit the results. You do not need Spring Data or JPA to implement pagination. `LIMIT`, `OFFSET`, and a deliberate `ORDER BY` are enough for a basic JDBC version.

Use stable ordering, such as `ORDER BY title, id`, so equal titles have a tie-breaker. Rows changing between requests can still shift offset-based pages. Cursor pagination addresses different tradeoffs and can wait.

### Checkpoint 5

Where should book identity, an author filter, replacement fields, and a preferred response media type go?

<details>
<summary>Answer</summary>

Book identity in the path; author filter in the query; replacement fields in the request body; preferred response media type in the Accept header.

</details>

---

## 6. JSON and Java representations

**⭐⭐⭐⭐⭐ MUST KNOW**

### 6.1 The JSON values you need

JSON is a text format built from a small set of value types:

```json
{
  "title": "Learning HTTP",
  "stock": 4,
  "price": 19.95,
  "available": true,
  "subtitle": null,
  "tags": ["java", "web"],
  "publisher": {
    "name": "Example Press",
    "country": "VN"
  }
}
```

| JSON feature | Example | Java representation you may choose |
|---|---|---|
| Object | `{"title":"Learning HTTP"}` | Record, DTO class, or map |
| Array | `["java","web"]` | `List<String>` |
| String | `"Learning HTTP"` | `String` |
| Number | `4`, `19.95` | `Integer`, `Long`, `BigDecimal`, etc., according to the contract |
| Boolean | `true` | `boolean` or `Boolean` |
| Null | `null` | A nullable reference |
| Nested object | `"publisher":{"name":"..."}` | Another DTO |

JSON member names and strings use double quotes. Standard JSON has no comments or trailing commas. Dates are usually represented by strings using an agreed format; JSON does not have a built-in date type.

Use a decimal type such as `BigDecimal` for money, as the companion does for price. A JSON number alone does not dictate a Java `double`.

### 6.2 Missing, null, and an actual value

These are different JSON inputs:

```json
{}
```

```json
{ "stock": null }
```

```json
{ "stock": 0 }
```

The first omits the property, the second explicitly provides null, and the third provides zero. Your API decides which are accepted and what they mean.

For create and full-replacement requests, a nullable `Integer stock` lets a basic manual check reject missing/null stock instead of silently treating it as an intentional zero. A primitive `int` cannot represent null.

Ordinary DTO binding may map both missing and explicit null to the same Java null. That is sufficient when both are rejected, as in our exercise. A generic patch where “missing means leave unchanged” but “null means clear” needs a representation that preserves presence. A naive nullable DTO cannot make that distinction by itself.

### 6.3 Serialization and deserialization

```text
JSON request bytes
       |
       | deserialization
       v
BookRequest Java object
       |
       | application work
       v
BookResponse Java object
       |
       | serialization
       v
JSON response bytes
```

**Deserialization** reads a representation and constructs a Java value. **Serialization** converts a Java value into a representation.

In this Boot 4 application, Spring MVC uses HTTP message converters for that boundary, with Jackson 3 as the default JSON library supplied by the selected starter. Jackson 2 examples using `com.fasterxml.jackson.databind.ObjectMapper` should not be copied as custom configuration into a Jackson 3 project without checking compatibility. Our controller DTOs require no direct ObjectMapper imports. [Spring Boot JSON support](https://docs.spring.io/spring-boot/reference/features/json.html)

The responsibilities remain separate:

```text
Spring MVC: choose a converter and connect it to HTTP
Jackson:   read/write JSON for the relevant Java types
Your code: decide fields, business behavior, and outcomes
```

### 6.4 A record is a convenient DTO

```java
import java.math.BigDecimal;

public record BookRequest(
        String isbn,
        String title,
        String author,
        BigDecimal price,
        Integer stock) {
}
```

```java
import java.math.BigDecimal;

public record BookResponse(
        long id,
        String isbn,
        String title,
        String author,
        BigDecimal price,
        int stock) {
    public static BookResponse from(Book book) {
        return new BookResponse(book.id(), book.isbn(), book.title(),
                book.author(), book.price(), book.stock());
    }
}
```

Here `Book` is the domain record from the companion; add its import when using these types in separate packages. The incoming JSON object maps to the request record's named components. The outgoing response record becomes a JSON object with the selected public fields.

There is no need to call `toString()` to create JSON. A record's generated `toString()` produces diagnostic Java text, not a JSON document.

Do not manually concatenate JSON strings with user data. Quotes and other characters require correct escaping; returning a DTO delegates representation writing to the converter.

### 6.5 Binding is not business validation

These are different failure categories:

```text
{bad json                  -> malformed representation
{"stock":{"value":3}}    -> wrong shape for an Integer field
{"stock":-3}              -> valid JSON and Java integer; unacceptable catalog value
```

The framework can reject unreadable JSON without entering the controller. A negative stock value still needs an application rule. `@RequestBody` does not mean “validate all fields.”

The exercise uses a few explicit checks to keep its contract coherent. Bean Validation and a consistent global error format are the next learning stage.

### Checkpoint 6

If `BookRequest` uses `Integer stock`, does Jackson automatically reject `{"stock":-8}` because stock should be nonnegative?

<details>
<summary>Answer</summary>

No. Negative eight is a valid integer. JSON conversion supplies a Java value; your application decides whether that value is allowed. A validation library can express that rule later, but deserialization alone does not establish it.

</details>

---

## 7. Spring Boot's web infrastructure

**⭐⭐⭐⭐⭐ MUST KNOW**

### 7.1 From a finite Boot runner to a server

Your previous mini-project may have used `ApplicationRunner` to perform work once and then deliberately close the context.

A web application stays available for requests:

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`SpringApplication.run(...)` still creates and refreshes a Spring application context. With the servlet web dependencies, Boot selects a web application context and starts an embedded server as part of startup.

The call returns after startup processing; the running server keeps the application alive. Do not surround this context with an immediately completed try-with-resources block or call `close()` right after `run(...)`, or the server will shut down.

Likewise, remove `spring.main.web-application-type: none` if it was used in the earlier CLI exercise. An `ApplicationRunner` can still do startup work, but it is not the handler for subsequent HTTP requests.

### 7.2 `spring-boot-starter-web` and the Boot 4 starter name

You will see **`spring-boot-starter-web`** throughout Spring tutorials. Historically, it is the starter that brings together the servlet web stack, Spring MVC, an embedded server, and JSON support.

For the Boot 4 baseline used here, `spring-boot-starter-web` is deprecated in favor of the more explicit **`spring-boot-starter-webmvc`**. The companion uses the new starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

Boot's dependency management supplies compatible versions. The default servlet stack includes embedded Tomcat and Jackson JSON integration. Do not add both web starters or independently pin Spring MVC and Jackson versions for this exercise. [Spring Boot starters](https://docs.spring.io/spring-boot/reference/using/build-systems.html#using.build-systems.starters)

This adds web infrastructure to the application you already understand. Keeping `spring-boot-starter-jdbc` and the PostgreSQL driver preserves the same JDBC persistence model.

### 7.3 Embedded HTTP server

Tomcat accepts incoming network connections, parses HTTP, and dispatches work into the servlet application. “Embedded” means it is started with your application from its dependencies; you do not need a separate Tomcat installation for this exercise.

```yaml
server:
  port: 8080
```

`8080` is the application's listening port, not PostgreSQL's port. A port conflict means another process is already listening on the requested address/port.

Boot supplies the server and the MVC infrastructure through auto-configuration when their conditions apply. That is the same classpath-plus-configuration mental model from the Boot chapter. [Spring Boot servlet web applications](https://docs.spring.io/spring-boot/reference/web/servlet.html)

### 7.4 Spring MVC and `DispatcherServlet`

Spring MVC is the servlet-based web framework. Its central dispatcher receives web requests and coordinates mapping, method invocation, and response handling.

You do not manually instantiate `DispatcherServlet` or write a switch statement for every URL in a normal Boot application.

```text
HTTP request
    |
    v
Embedded server (Tomcat)
    |
    v
DispatcherServlet
    |
    v
Handler mapping: find matching controller method
    |
    v
Argument resolution / request-body conversion
    |
    v
@RestController method
    |
    v
Service
    |
    v
Repository
    |
    v
JdbcTemplate -> DataSource -> HikariCP -> pgJDBC -> PostgreSQL
```

The reverse direction is:

```text
Database result -> repository result -> service result
                                           |
                                           v
                              controller Java return value
                                           |
                                           v
                          status / headers / body handling
                                           |
                                           v
                        HTTP message converter using Jackson
                                           |
                                           v
                                JSON in HTTP response
```

`DispatcherServlet` is a front controller, not the database connector. The controller calls the service through ordinary Java method calls after HTTP input has been resolved. [Spring MVC dispatcher](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html)

### 7.5 Mapping, argument resolution, and conversion

Given:

```java
@GetMapping("/{id}")
public BookResponse findById(@PathVariable("id") long id) {
    return BookResponse.from(bookService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
}
```

Spring must answer three separate questions:

| Stage | Question | Example |
|---|---|---|
| Mapping | Which method matches this request? | GET plus `/books/{id}` |
| Argument resolution | What Java arguments should it receive? | Convert path text `10` to `long 10` |
| Return handling | How should its Java result become HTTP? | Serialize `BookResponse` as JSON |

Mapping does not query the database to find a book. It selects the Java handler. The handler or service performs the lookup.

For a POST with `@RequestBody`, argument resolution delegates JSON reading to a message converter. If the JSON cannot be read, method invocation may never happen. [Spring MVC handler methods](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods.html)

### 7.6 Your Spring Core knowledge still applies

Controllers are Spring beans. They receive dependencies through constructor injection, and the application class must sit above the component packages so scanning finds them:

```text
com.example.bookcatalog
|-- Application
|-- web/
|-- service/
|-- repository/
|-- dto/
`-- model/
```

```java
@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
}
```

A `BookRequest` is an ordinary per-request data value produced by conversion. It does not need `@Component` or registration as a singleton bean.

Controllers and services are normally singleton beans. Multiple requests can call the same instance concurrently. Store dependencies in fields and request-specific data in method parameters/local variables.

```java
// Wrong: shared mutable request state in a singleton controller.
private long currentBookId;
```

One request could overwrite another's value. Stateless bean design from Spring Core now has a directly observable web consequence.

### 7.7 HTTP concurrency and JDBC connections are different resources

```text
Several HTTP requests can be in flight
                 |
                 v
Shared controller/service/repository beans
                 |
                 v
JdbcTemplate borrows JDBC connections as needed
                 |
                 v
Finite HikariCP pool
```

An HTTP connection is not a JDBC connection. Increasing the server's capacity to handle requests does not create unlimited database capacity. Continue using the shared Boot-managed `DataSource`; do not create a new Hikari pool per controller call.

With `JdbcTemplate`, the framework manages the connection lifecycle for each operation, cooperating with transactions when present. You still own query meaning, service behavior, and the HTTP contract.

### Checkpoint 7

What new machinery turns `GET /books/10` into a call to a Spring bean, and which pieces from your previous JDBC project remain?

<details>
<summary>Answer</summary>

The embedded server, DispatcherServlet, handler mapping, argument resolution, and message converters form the web boundary. Constructor-injected services, repositories, JdbcTemplate, the DataSource bean, HikariCP, pgJDBC, and PostgreSQL remain the familiar application and persistence stack.

</details>

---

## 8. DTOs and application boundaries

**⭐⭐⭐⭐ IMPORTANT**

A **DTO**, or data transfer object, defines data crossing a boundary. For HTTP, request DTOs describe accepted input and response DTOs describe published output.

```text
BookRequest  -> accepted isbn, title, author, price, stock
Book         -> application's representation of a stored book
BookResponse -> published id, isbn, title, author, price, stock
```

The internal model and response happen to have similar fields in this small project. They serve different responsibilities. Separating them makes it clear that a future internal `supplierCost` column must not automatically appear in responses.

Do not accept a server-generated ID in the create DTO simply because the database row has an ID. The path identifies the existing resource during an update; the body describes writable state.

### 8.1 Keep HTTP concerns at the controller boundary

| Layer | Owns | Should not need for this project |
|---|---|---|
| Controller | HTTP input binding, status, headers, response DTO | SQL statements or a manually created pool |
| Service | Application behavior and coordination | `HttpServletRequest` or response headers |
| Repository | SQL and row mapping | `ResponseEntity` or endpoint routing |

The controller can translate `Optional.empty()` into 404 while a repository simply reports absence. Your service remains callable from another adapter, such as a future scheduled job, without constructing a fake HTTP request.

Keep mapping between models and DTOs explicit and small. A new mapping library is unnecessary for these six fields.

### 8.2 A clear contract before implementation

Write a miniature contract first:

```text
POST /books
Consumes: application/json
Required writable fields: isbn, title, author, price, stock
Server-owned field: id
Success: 201, Location, BookResponse
Invalid input: 400
Duplicate ISBN: 409
```

This forces a useful question: which part will HTTP/Spring validate automatically, and which part requires your application code?

`@RequestBody` can require a readable body. It does not automatically require nonblank titles or nonnegative stock. Those are separate rules.

---

## 9. Core Spring annotations

**⭐⭐⭐⭐⭐ MUST KNOW**

The examples use imports from `org.springframework.web.bind.annotation`. `@Controller` comes from `org.springframework.stereotype`. Named binding annotations such as `@PathVariable("id")` are explicit so readers do not need to rely on retained parameter names.

### 9.1 `@RestController`

**Purpose:** Declare a controller whose handler return values are written to the HTTP response body. It combines the controller stereotype with response-body behavior.

```java
@RestController
public class GreetingController {
    @GetMapping("/greeting")
    public Map<String, String> greeting() {
        return Map.of("message", "Hello HTTP");
    }
}
```

**Behind the scenes:** Component scanning registers the controller bean; MVC discovers its mapped handler methods; response handling uses the configured message converters for returned body values. [Controller declaration](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann.html)

**Common mistakes:** Placing it outside the scanned package; assuming every public method becomes an endpoint without a mapping; thinking it automatically makes the entire architecture REST-compliant; expecting a returned Java string to be a structured JSON object.

### 9.2 `@Controller` and the comparison with `@RestController`

**Purpose:** Mark a Spring MVC controller. It is frequently used for server-rendered pages; response-body handling must be selected for methods that should write data directly.

```java
@Controller
public class PageController {
    @GetMapping("/catalog-page")
    public String page() {
        return "catalog"; // Logical view name; needs a configured view/template.
    }
}
```

**Behind the scenes:** MVC interprets this string as a view name and uses view resolution. In a `@RestController`, a returned string is body content instead. Adding `@ResponseBody` to a method in a `@Controller` selects body-writing behavior for that method. [Response-body handling](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html)

**Common mistakes:** Returning `"books"` from `@Controller` while expecting a JSON list; returning `"catalog"` from `@RestController` while expecting a template. A Controller method returning `ResponseEntity` can also explicitly return an HTTP body; the annotation does not forbid data responses.

For this JSON API, use `@RestController` consistently.

### 9.3 `@RequestMapping`

**Purpose:** Define request-matching conditions. At class level it is useful for a shared path; method-specific mappings narrow that path.

```java
@RestController
@RequestMapping("/books")
public class BookController {
    @GetMapping("/{id}")
    public BookResponse get(@PathVariable("id") long id) {
        return BookResponse.from(bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
    }
}
```

Effective operation: `GET /books/{id}`.

**Behind the scenes:** MVC records the combined class/method conditions during handler registration, then matches incoming requests against them. Conditions can involve path, method, parameters, headers, and media types. [Request mapping](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html)

**Common mistakes:** Repeating `/books` in both class and method paths and accidentally creating `/books/books`; putting multiple mapping annotations on one method; omitting a method restriction on a generic method-level `@RequestMapping` and accepting more methods than intended.

`@RequestMapping(method = RequestMethod.GET)` and `@GetMapping` express the same method selection. Use the readable shortcut for ordinary handler methods.

### 9.4 `@GetMapping`

**Purpose:** Map a read operation to HTTP GET.

```java
@GetMapping
public List<BookResponse> list() {
    return bookService.findAll(null, 0, 20).stream()
            .map(BookResponse::from).toList();
}
```

With class mapping `/books`, this is `GET /books`.

**Behind the scenes:** The mapping participates in MVC's normal method/path selection. After invocation, the return handler writes the collection through an appropriate converter. JSON output is an array.

**Common mistakes:** Performing a requested insert/delete in GET; relying on a GET body for filters; returning 404 because the list has zero elements. Safety comes from what the method does, not from the annotation label.

### 9.5 `@PostMapping`

**Purpose:** Map HTTP POST; commonly used for collection creation.

```java
@PostMapping(consumes = "application/json")
public ResponseEntity<BookResponse> create(@RequestBody BookRequest request) {
    Book created = bookService.create(request.isbn(), request.title(),
            request.author(), request.price(), request.stock());
    URI location = URI.create("/books/" + created.id());
    return ResponseEntity.created(location).body(BookResponse.from(created));
}
```

**Behind the scenes:** MVC selects the POST handler and reads the JSON argument before invoking it. The returned ResponseEntity supplies status and headers, while its body is converted normally.

**Common mistakes:** Assuming POST automatically produces 201; accepting arbitrary fields from the persistence model; omitting `Content-Type` when sending JSON; assuming a retry cannot create a second record.

### 9.6 `@PutMapping`

**Purpose:** Map HTTP PUT for the target's replacement contract.

```java
@PutMapping(path = "/{id}", consumes = "application/json")
public BookResponse replace(
        @PathVariable("id") long id,
        @RequestBody BookRequest request) {
    return BookResponse.from(bookService.replace(id, request.isbn(), request.title(),
                    request.author(), request.price(), request.stock())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
}
```

**Behind the scenes:** Spring binds the path ID and deserializes the request. Your service performs replacement; the mapping annotation does not infer a SQL UPDATE or decide whether creation is allowed.

**Common mistakes:** Updating only nonnull fields while claiming full replacement; silently taking identity from the body instead of the path; omitting required writable fields; believing PUT must universally create a missing book.

### 9.7 `@PatchMapping`

**Purpose:** Map HTTP PATCH for a documented partial change.

```java
public record StockRequest(Integer stock) {
}
```

```java
@PatchMapping(path = "/{id}", consumes = "application/json")
public BookResponse changeStock(
        @PathVariable("id") long id,
        @RequestBody StockRequest request) {
    return BookResponse.from(bookService.setStock(id, request.stock())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
}
```

**Behind the scenes:** This works through the same MVC mapping and argument conversion infrastructure. Your implementation defines the partial-change meaning and must check that stock was actually supplied.

**Common mistakes:** Assuming PATCH automatically merges arbitrary JSON into an object; treating every PATCH as non-idempotent; using a generic nullable DTO when omitted and explicit-null values need different meanings.

Here `stock` is an absolute new value. The request is not an instruction to add or subtract stock.

### 9.8 `@DeleteMapping`

**Purpose:** Map HTTP DELETE for an individual resource.

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable("id") long id) {
    if (!bookService.delete(id)) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
}
```

**Behind the scenes:** MVC binds the ID, calls the method, and writes the selected status with no body. The service/repository determines whether anything was deleted.

**Common mistakes:** Sending the ID only in a JSON DELETE body; returning a message body with 204; treating a second 404 as proof that DELETE is non-idempotent; assuming `void` always implies 204.

### 9.9 `@PathVariable`

**Purpose:** Bind a captured path segment to a method parameter.

```java
@GetMapping("/{bookId}")
public BookResponse get(@PathVariable("bookId") long id) {
    return BookResponse.from(bookService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
}
```

**Behind the scenes:** Mapping captures the text in `{bookId}` and Spring's type conversion turns it into the target Java type. A value such as `abc` cannot normally become a long, so the request fails before the method body runs.

**Common mistakes:** Using `@PathVariable("id")` when the template says `{bookId}`; looking for `?id=10` with this annotation; assuming successful numeric conversion proves the book exists. A syntactically valid ID still requires a lookup.

### 9.10 `@RequestParam`

**Purpose:** Bind a query parameter for this API. Spring MVC also supports form request parameters, which we are not using here.

```java
@GetMapping
public List<BookResponse> list(
        @RequestParam(name = "author", required = false) String author) {
    return bookService.findAll(author, 0, 20).stream()
            .map(BookResponse::from).toList();
}
```

Request: `GET /books?author=Mira%20Tran`.

**Behind the scenes:** Spring resolves the named parameter and converts it when the Java type requires conversion. Parameters are required by default; `required = false`, an Optional parameter, or a suitable default changes that behavior. A declared default also makes the parameter optional. [Request parameters](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestparam.html)

For the companion's paginated endpoint:

```java
@RequestParam(name = "page", defaultValue = "0") int page
```

**Common mistakes:** Forgetting to make an optional filter optional; placing `?author=...` inside `@GetMapping` instead of binding it; expecting a JSON object property to bind through `@RequestParam`; allowing unbounded page sizes because integer conversion succeeded.

### 9.11 `@RequestBody`

**Purpose:** Read the HTTP message body into a Java value.

```java
@PostMapping(consumes = "application/json")
public ResponseEntity<BookResponse> create(@RequestBody BookRequest request) {
    Book created = bookService.create(request.isbn(), request.title(),
            request.author(), request.price(), request.stock());
    return ResponseEntity.created(URI.create("/books/" + created.id()))
            .body(BookResponse.from(created));
}
```

**Behind the scenes:** MVC chooses a message converter based on the media type and Java target type. JSON conversion builds the request object before the handler executes. The annotation requires a body by default. [Request-body conversion](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html)

**Common mistakes:** Forgetting the annotation and expecting JSON object binding; trying to declare one separate `@RequestBody` parameter per JSON property; assuming the annotation enforces nonblank strings; expecting a missing required body and a missing required field to be the same validation mechanism.

Use one DTO that contains the desired body fields. The HTTP message has one body.

### 9.12 `@RequestHeader`

**Purpose:** Bind a request header when the application needs to inspect it.

```java
@GetMapping("/request-info")
public Map<String, String> requestInfo(
        @RequestHeader(name = "User-Agent", defaultValue = "unknown") String agent) {
    return Map.of("clientAgent", agent);
}
```

**Behind the scenes:** MVC retrieves the named header and performs type conversion when needed. As with query parameters, required/default behavior must match the endpoint's contract. [Request headers](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestheader.html)

**Common mistakes:** Using it to read JSON properties; assuming a client-supplied identity header authenticates a user; manually parsing Accept in every controller instead of letting MVC negotiate media types; logging every header and exposing credentials.

The example is a small teaching endpoint, not part of the book mini-project's required routes.

### 9.13 `consumes`, `produces`, and common imports

```java
@PostMapping(consumes = "application/json", produces = "application/json")
```

`consumes` constrains the request representation. `produces` constrains response representations and participates in matching the client's Accept preference. They do not themselves serialize JSON or validate field values.

Useful imports for these examples are:

```java
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
// Also import your Book, BookRequest, StockRequest, BookResponse, and BookService types.
```

Use explicit imports in your final project if your preferred style avoids wildcards.

### Checkpoint 9

For `PUT /books/10?source=manual`, identify which mechanism reads the path ID, optional source parameter, and JSON body. Which annotation actually writes SQL?

<details>
<summary>Answer</summary>

`@PathVariable` reads 10, `@RequestParam` can read `source`, and `@RequestBody` reads JSON. None writes SQL. The controller calls application code, eventually reaching your JDBC repository.

</details>

---

## 10. Returning responses

**⭐⭐⭐⭐⭐ MUST KNOW**

### 10.1 Returning an ordinary object

```java
@GetMapping("/{id}")
public BookResponse findById(@PathVariable("id") long id) {
    return BookResponse.from(bookService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
}
```

In a `@RestController`, a successful nonnull response object is normally serialized with status 200. Here the service reports absence with Optional; the controller explicitly translates it to 404. A method name alone does not create missing-resource behavior.

Do not return null hoping Spring will infer 404. Select missing-resource behavior explicitly.

### 10.2 Returning collections

```java
@GetMapping
public List<BookResponse> findAll() {
    return bookService.findAll(null, 0, 20).stream()
            .map(BookResponse::from).toList();
}
```

JSON output for no matches:

```json
[]
```

Returning an empty list is a useful stable contract. Returning null instead gives callers a different shape to handle. For a paginated response, return a DTO containing `items` and pagination metadata rather than pretending the bare array contains page information.

### 10.3 `ResponseEntity<T>`

`ResponseEntity<T>` represents a response's **status, headers, and optional body**. The generic parameter is the Java body type; it is not a database entity or a JPA concept. The body still goes through the configured HTTP message converters. [ResponseEntity reference](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responseentity.html)

```text
ResponseEntity<BookResponse>
|-- HTTP status
|-- HTTP headers
`-- BookResponse body -> JSON
```

Use it when a handler needs to choose statuses, set headers, or express an empty response. You do not have to wrap every always-200 read in it.

### 10.4 Explicit 200 or 404

```java
@GetMapping("/{id}")
public ResponseEntity<BookResponse> findById(@PathVariable("id") long id) {
    return bookService.findById(id)
            .map(BookResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```

Here `findById` returns `Optional<Book>`. The controller maps a present domain book to its response DTO and translates absence into HTTP. Returning `Optional<BookResponse>` directly is not the same declaration of status behavior; unwrap it at the boundary.

### 10.5 201 with `Location`

```java
@PostMapping(consumes = "application/json")
public ResponseEntity<BookResponse> create(@RequestBody BookRequest request) {
    Book created = bookService.create(request.isbn(), request.title(),
            request.author(), request.price(), request.stock());
    URI location = URI.create("/books/" + created.id());
    return ResponseEntity.created(location).body(BookResponse.from(created));
}
```

Observable contract:

```http
HTTP/1.1 201 Created
Location: /books/10
Content-Type: application/json

{"id":10,"isbn":"9780000000011","title":"Learning HTTP","author":"Mira Tran","price":19.95,"stock":4}
```

`created(location)` selects 201 and sets Location. `.body(BookResponse.from(created))` supplies the public Java DTO for conversion.

The relative URI is intentional for this app at the root context path. A client resolves `/books/10` against the response origin. If you later add a context path or expose the application behind a proxy prefix, construct externally correct locations using the appropriate URI infrastructure; do not hardcode a development hostname in every response.

### 10.6 Empty responses

```java
return ResponseEntity.noContent().build();
```

The handler return type can be `ResponseEntity<Void>`. `Void` communicates that no body value is intended; it does not itself choose a status.

```java
return ResponseEntity.notFound().build();
```

An empty 404 is also a coherent small contract. A consistent structured error body is useful later, but a status does not require inventing a body for every response.

Returning `void` from a handler without an explicit status does not mean “send 204.” Choose the success status deliberately.

### 10.7 Setting other headers

```java
return ResponseEntity.ok()
        .header("Cache-Control", "no-store")
        .body(book);
```

This example explicitly prevents storage of this response. It is a demonstration of header control, not a recommendation to disable caching for every read API.

Usually let MVC select `Content-Type` through message conversion and the endpoint's media-type contract. Set response headers for an actual requirement rather than mirroring every request header.

### 10.8 A fixed status versus a conditional response

For a handler that always succeeds with the same status, Spring also supports `@ResponseStatus(HttpStatus.NO_CONTENT)`. It declares a fixed response status; it does not by itself solve conditional outcomes.

The companion uses ordinary DTO/list returns for successful 200 responses and ResponseEntity when creation headers or an empty 204 are needed. Local status decisions keep the HTTP contract visible. More annotation combinations are unnecessary for the current goal.

### Checkpoint 10

Does `.body(BookResponse.from(created))` send a Java `toString()` value? Does `ResponseEntity<Void>` always imply 204? Does POST automatically return 201?

<details>
<summary>Answer</summary>

No to all three. A message converter serializes the body. The selected status builder determines the status, independently of Void. POST creation needs an explicit 201 decision, such as `ResponseEntity.created(location)`.

</details>

---

## 11. Trace one request completely

**⭐⭐⭐⭐⭐ MUST KNOW**

Use this trace to connect the new HTTP concepts with your existing Spring/JDBC model.

### 11.1 Successful creation

Client intent: create ISBN “9780000000011” with title “Learning HTTP,” author “Mira Tran,” price 19.95, and stock 4.

```text
Client constructs POST /books with JSON and Content-Type
    |
Tomcat accepts the request
    |
DispatcherServlet coordinates processing
    |
Handler mapping selects BookController.create(...)
    |
Message converter/Jackson reads JSON into BookRequest
    |
Controller performs its small input checks and calls the service
    |
Service performs the application operation
    |
Repository calls JdbcTemplate with SQL parameters
    |
JdbcTemplate borrows through the Boot-managed DataSource
    |
HikariCP supplies a logical connection handle backed by a DB session
    |
pgJDBC sends SQL to PostgreSQL
    |
PostgreSQL inserts a row and returns generated values
    |
Repository maps the row; JDBC resources are released appropriately
    |
Service returns Book; controller maps it to BookResponse
    |
Controller returns 201 + Location + Java body
    |
Message converter/Jackson serializes body as JSON
    |
Tomcat sends the HTTP response
```

The HTTP client never sees the JDBC connection. The database never sees `@PostMapping`. Each boundary has its own representation and responsibility.

### 11.2 Failures can occur before your handler runs

| Request/problem | Likely stopping point | Typical observable result |
|---|---|---|
| Nothing listening on port 8080 | Before HTTP reaches the application | Connection refused; no HTTP status from this app |
| Unsupported HTTP method on an existing route | Request mapping | 405 |
| Unsupported request Content-Type | Mapping/converter selection | 415 |
| Malformed JSON | Request-body conversion | 400 |
| `GET /books/not-a-number` | Path argument conversion | 400 |
| Well-formed request for missing numeric ID | Service/repository lookup | Deliberate 404 |
| Broken SQL or unexpected server bug | Application/persistence execution | Usually 500 unless specifically handled |

Do not assume that “I did not see my controller log” means the request never arrived. It may have arrived and failed during mapping or argument conversion.

### 11.3 Requests do not automatically become one transaction

HTTP request duration, Java method duration, and database transaction duration are different boundaries.

A controller calling two repository methods does not by itself guarantee they share one transaction or roll back together. You already learned why transaction boundaries matter with JDBC. The current exercise uses small single-statement writes; deeper declarative transaction design belongs in the later transaction stage.

---

## 12. Calling and inspecting an API

**⭐⭐⭐⭐ IMPORTANT**

You need a client that lets you inspect more than the JSON body. Verify at least method, path, status, headers, response shape, and the intended state change.

### 12.1 Browser versus command-line client

Entering `http://localhost:8080/books` in a browser's address bar usually issues a GET. It does not let you directly exercise PUT, PATCH, or arbitrary JSON request bodies.

Use a command-line client or an API client for those operations. On Windows PowerShell, write `curl.exe` explicitly because `curl` may be an alias on older Windows PowerShell installations.

```powershell
curl.exe -i "http://localhost:8080/books"
```

`-i` includes response headers and the status line. A server returning 404 is different from curl being unable to connect. Curl normally prints HTTP error responses without treating every 4xx/5xx as a process failure.

### 12.2 Send JSON without shell-quoting surprises

Create `book-create.json` in your editor:

```json
{
  "isbn": "9780000000011",
  "title": "Learning HTTP",
  "author": "Mira Tran",
  "price": 19.95,
  "stock": 4
}
```

Then send its bytes:

```powershell
curl.exe -i -X POST "http://localhost:8080/books" -H "Content-Type: application/json" -H "Accept: application/json" --data-binary "@book-create.json"
```

Using a file avoids differences in how Windows PowerShell and newer PowerShell versions pass embedded quotes to native programs. The `@` here is curl's “read this file” syntax inside an argument.

For PowerShell-native JSON construction, use another unused ISBN (the example below differs from the file above):

```powershell
$bookPayload = @{
    isbn = "9780000000012"
    title = "Learning HTTP"
    author = "Mira Tran"
    price = 19.95
    stock = 4
} | ConvertTo-Json

$createdBook = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/books" -ContentType "application/json" -Body $bookPayload
$createdBook
```

`Invoke-RestMethod` conveniently converts JSON responses into PowerShell objects. Use curl's `-i` when you want to see the exact status and headers. Older PowerShell versions throw for HTTP error responses, so do not confuse that client behavior with the server returning no response.

### 12.3 Observe a complete change cycle

```text
GET collection            -> inspect current rows
POST new book              -> record the returned ID and Location
GET returned Location      -> confirm stored representation
PUT same ID                -> confirm every writable field changed as specified
PATCH same ID              -> confirm only stock changed
DELETE same ID             -> confirm 204 and empty body
GET deleted ID             -> confirm 404
```

Do not assume the generated ID is 1. PostgreSQL identity sequences can advance despite deleted rows, failed insert attempts, or earlier runs.

The companion turns this sequence into individual tasks and concrete verification commands, with the full implementation placed after the exercises.

---

## 13. Failures and troubleshooting

**⭐⭐⭐⭐ IMPORTANT**

### 13.1 Small explicit error handling is enough for this stage

For a known missing resource, use a deliberate ResponseEntity branch, or a local `ResponseStatusException` where appropriate:

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
```

The necessary imports are:

```java
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
```

Keep HTTP status translation at the controller boundary when practical. The mini-project uses small explicit checks to support its documented contract; it does not build a global error-handling framework.

Do not depend on the exact default JSON shape or on the reason string being exposed in a Boot error response. Error representation depends on configuration and content negotiation. Verify the intended status now; learn a consistent public error schema with global exception handling later. [Spring Boot error handling](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.error-handling)

Do not return stack traces, SQL text, connection strings, or raw exception messages to a caller as the API contract.

### 13.2 Diagnose at the correct boundary

| Symptom | Check first | Reason |
|---|---|---|
| Connection refused | Is the app running, and is the port correct? | HTTP routing has not happened |
| Application exits immediately | Is the context closed after startup? Is web mode disabled? | A finite CLI pattern may have been retained |
| 404 for every book route | Controller package and mapping path | The bean or route may be undiscovered |
| 405 | Actual HTTP method and matching route | The path can exist without supporting that operation |
| 400 before handler log | JSON syntax, required body, path/query conversion | Binding can fail before application code |
| 415 | Request Content-Type and endpoint consumes | Request representation mismatch |
| 406 | Request Accept and endpoint produces | Response representation mismatch |
| JSON has unexpected fields | Response DTO and converter behavior | Public output comes from the serialized value |
| Duplicate row after retry | POST retry behavior and returned/generated IDs | The first request may already have succeeded |
| Cross-request data mix-up | Mutable controller/service fields | Singleton beans handle concurrent calls |
| Database changes but wrong status | Controller return handling | SQL success does not choose the HTTP status |
| No response body after DELETE | Status code | An empty 204 is correct |

### 13.3 A useful debugging sequence

1. Capture the exact client command or request.
2. Confirm method, URL, and request headers.
3. Read the response status before interpreting the body.
4. Determine whether the handler executed.
5. Inspect service/repository behavior only after the web boundary is understood.
6. Check database state to verify the requested effect.

Do not change a correct repository merely because the client sent the wrong media type.

---

## 14. Useful surrounding concepts

**⭐⭐⭐ NICE TO KNOW**

### 14.1 HEAD and OPTIONS

HEAD is a retrieval request whose response has no content; it is useful for metadata. OPTIONS asks about communication options available for a target. Spring MVC supports common HEAD/OPTIONS handling for mapped controllers, so you rarely need to write explicit handlers for a first API. [Spring MVC HEAD and OPTIONS support](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html#mvc-ann-requestmapping-head-options)

Do not confuse HEAD's empty response with DELETE's 204. They have different methods and meanings.

### 14.2 Browser origins and CORS

An origin combines scheme, host, and port. `http://localhost:3000` and `http://localhost:8080` are different origins.

A browser may apply cross-origin restrictions when JavaScript calls your API. Some cross-origin requests first trigger an OPTIONS preflight. Command-line tools do not enforce the browser's same-origin restrictions, so a request working in curl does not prove a browser frontend is configured correctly.

CORS is a browser access policy mechanism, not authentication or a way to secure an API from all callers. Configure it when you have an actual frontend origin; a tutorial-wide wildcard is unnecessary for this command-line exercise. [Spring MVC CORS reference](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)

### 14.3 Caching and conditional requests

An API can describe cache behavior using headers such as `Cache-Control`. A validator such as an ETag can allow a client to ask whether its stored representation is still current. A 304 response tells a client it can reuse its stored representation under the relevant conditions.

Later, conditional updates can also help reject stale writes rather than letting one user's update overwrite another's unnoticed. Recognize headers such as `If-Match` and the 412 Precondition Failed status; implementing concurrent-edit protection is beyond the first CRUD exercise.

### 14.4 API documentation and evolution

A useful API description records operation paths, methods, accepted fields, response shapes, statuses, and examples. OpenAPI is a machine-readable description format; a documentation UI is a tool that displays such a contract.

The small endpoint table in the companion is enough for this stage. Generating a UI or adding a library does not fix an ambiguous PUT/PATCH contract.

Changing field names, removing accepted fields, or changing response shapes can break existing clients. Stable DTOs make those public changes intentional. Versioning is one possible evolution strategy, not a requirement to add `/v1` to every learning endpoint.

---

## 15. What comes later

**⭐⭐ FUTURE KNOWLEDGE**

| Later topic | What it will add | What you need now |
|---|---|---|
| Validation + global exception handling | Declarative input rules and consistent error responses | Distinguish conversion failures, rejected input, absence, and server bugs |
| Spring Data JPA + Hibernate | Another persistence model | Keep the controller independent of the repository technology |
| Deeper transactions | Deliberate atomic boundaries across operations | Do not assume one HTTP request automatically means one transaction |
| Spring Security | Authentication and authorization | Understand 401/403 and avoid trusting arbitrary client headers |
| Testing | Automated HTTP, unit, and integration checks | Define observable contracts and manually verify them |
| Dockerizing the application | Running the app and database as deployable containers | Understand the app's HTTP port and the database connection separately |
| Deployment | Public addresses, TLS, configuration, observability | Keep configuration external and do not hardcode development URLs |

These topics improve the application after the HTTP boundary is understandable. They are not prerequisites for implementing the book catalog with the JDBC stack you already know.

---

## 16. Readiness review

### 16.1 Explain the complete model without saying “Spring does magic”

```text
Client: method + target + headers + optional representation
                              |
                              v
Boot-provided server and Spring MVC infrastructure
                              |
                              v
Controller bean: binds input and translates application outcomes to HTTP
                              |
                              v
Service and repository beans: familiar application and JDBC work
                              |
                              v
Controller return: status + headers + optional Java body
                              |
                              v
Message converter/Jackson: Java body -> JSON
                              |
                              v
Client: interprets response and decides its next action
```

### 16.2 Questions before the mini-project

1. Why does the client not need your Java DTO class to call the API?
2. How can a server use PostgreSQL while exposing stateless interactions?
3. Why is `GET /books` different from `GET /books/10` when nothing matches?
4. What does an omitted stock field mean in this guide's PUT contract?
5. Why does setting stock to 3 differ from decrementing it by 3 under retries?
6. Which input does `@RequestParam` read? Which input does `@RequestBody` read?
7. What is the difference between 406 and 415?
8. Does a controller's ordinary object return automatically select 201 after an insert?
9. What does `ResponseEntity<Void>` tell you, and what does it not tell you?
10. Why can malformed JSON prevent your controller method from running?
11. Why is a mutable `currentBookId` field in a controller unsafe?
12. What happens if you close the application context immediately after starting the web app?
13. Who creates the HikariDataSource in the Boot application, and who writes SQL?
14. Does `@RestController` implement every REST constraint?
15. Why can a failed network response leave uncertainty about a completed POST?

<details>
<summary>Reference answers</summary>

1. HTTP and JSON define the cross-process contract; the client's language/types can differ.
2. Persistent resource state is allowed; a request must not depend on a hidden prior client conversation for its meaning.
3. The collection exists and can be empty, giving 200 with `[]`; a missing individual resource gives 404.
4. The full replacement is incomplete and should be rejected under this contract.
5. Repeating an assignment preserves the same requested value; repeating a decrement compounds the effect.
6. Query parameters for this JSON API; the body representation, respectively.
7. 406 concerns an unavailable response representation; 415 concerns the unsupported incoming representation.
8. No. Choose 201 explicitly and normally provide Location.
9. It declares no intended body type. The selected builder/status determines whether the response is 204, 404, or another code.
10. MVC must deserialize the argument before calling the handler.
11. Requests can concurrently use the same singleton controller instance and overwrite shared state.
12. The server and managed resources shut down, so it cannot keep serving requests.
13. Boot auto-configuration creates the DataSource when its conditions apply; your repository supplies SQL through JdbcTemplate.
14. No. It establishes controller and response-body behavior, not whole-system REST architecture.
15. The server may commit the work before the connection fails or the response reaches the client.

</details>

### 16.3 Practical readiness checklist

- [ ] I can read a raw request and identify method, target, headers, and body.
- [ ] I can explain resources, representations, and the distinction between HTTP and REST.
- [ ] I can distinguish safety from idempotency using PUT, PATCH, and DELETE examples.
- [ ] I can choose statuses for success, creation, empty success, missing resources, and input problems.
- [ ] I can place identity, filters, structured input, and message metadata appropriately.
- [ ] I can explain JSON-to-Java and Java-to-JSON conversion without confusing it with SQL mapping.
- [ ] I know why the Boot 4 example uses `spring-boot-starter-webmvc` when older lessons name `spring-boot-starter-web`.
- [ ] I can explain all twelve controller/mapping/binding annotations taught above.
- [ ] I can use a response DTO and select status/headers with ResponseEntity.
- [ ] I can follow a request through Tomcat, MVC, controller, service, repository, and the JDBC stack.
- [ ] I can keep request-specific state out of singleton bean fields.
- [ ] I can inspect an API with curl or PowerShell and verify its database effect.

Continue with [REST API Mini Project](REST_API_Mini_Project.md). Attempt each task and verify its behavior before reading the reference implementation.
