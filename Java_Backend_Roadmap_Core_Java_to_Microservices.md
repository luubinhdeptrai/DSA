# Java Backend Roadmap — Core Java to Microservices

> A practical roadmap for becoming a Java backend developer, starting from Java fundamentals and ending with production-oriented microservices.

---

## How to use this roadmap

For every major topic, use this cycle:

```text
Learn concepts
→ Write small examples
→ Build a focused exercise
→ Apply it to a real project
→ Debug failures
→ Explain the topic without notes
```

Priority:

```text
⭐⭐⭐⭐⭐ MUST KNOW
⭐⭐⭐⭐ IMPORTANT
⭐⭐⭐ USEFUL
⭐⭐ NICE TO KNOW
⭐ ADVANCED / FUTURE
```

---

# Phase 1 — Java Foundations

## 1. Core Java ⭐⭐⭐⭐⭐

Learn:

- variables, primitive types, operators;
- control flow;
- methods;
- arrays and strings;
- classes and objects;
- constructors;
- access modifiers;
- `static`, `final`;
- packages;
- enums;
- records;
- `equals()`, `hashCode()`, `toString()`;
- Java pass-by-value and memory basics.

Goal:

```text
Java syntax
→ comfortable enough to focus on design and backend logic
```

---

## 2. OOP ⭐⭐⭐⭐⭐

Learn:

- encapsulation;
- abstraction;
- inheritance;
- polymorphism;
- interfaces;
- abstract classes;
- composition;
- method overriding;
- method overloading;
- SOLID basics.

Backend connection:

```text
interface
→ abstraction boundary

implementation
→ concrete behavior

dependency injection
→ collaborators supplied externally
```

---

# Phase 2 — Core Java for Real Applications

## 3. Collections + Generics ⭐⭐⭐⭐⭐

Learn:

```text
List / ArrayList / LinkedList
Set / HashSet / LinkedHashSet / TreeSet
Map / HashMap / LinkedHashMap / TreeMap
Queue / Deque / ArrayDeque
PriorityQueue
```

Also learn:

- iterators;
- comparators;
- sorting;
- generics;
- wildcards;
- time/space complexity.

Keep DSA practice as a parallel track.

---

## 4. Exceptions ⭐⭐⭐⭐⭐

Learn:

```text
checked vs unchecked
try / catch / finally
throws
custom exceptions
try-with-resources
propagation
suppressed exceptions
```

Backend mental model:

```text
Repository failure
→ Service translation
→ Global exception handler
→ HTTP response
```

---

## 5. Java I/O ⭐⭐⭐⭐

Learn:

- byte streams;
- character streams;
- buffered I/O;
- `Path` / `Files`;
- serialization basics;
- file processing.

Useful for uploads, CSV, reports, configuration, and data import/export.

---

## 6. Threads and Concurrency Basics ⭐⭐⭐⭐

Learn:

```text
Thread
Runnable
Callable
ExecutorService
Future
CompletableFuture basics
synchronized
locks basics
atomic classes
race conditions
deadlocks
```

Important backend idea:

```text
many requests
→ many threads
→ shared singleton beans
→ mutable shared state can be dangerous
```

---

# Phase 3 — Tooling

## 7. Maven / Gradle ⭐⭐⭐⭐⭐

Learn Maven first unless your project requires Gradle.

Understand:

```text
pom.xml
dependencies
scopes
plugins
build lifecycle
profiles
dependency management
```

Commands:

```powershell
mvn compile
mvn test
mvn package
mvn clean package
mvn dependency:tree
mvn spring-boot:run
```

---

## 8. Git + GitHub ⭐⭐⭐⭐⭐

Learn:

```text
commit
branch
merge
rebase basics
pull
push
.gitignore
pull requests
merge conflicts
```

---

# Phase 4 — SQL and Databases

## 9. SQL + PostgreSQL ⭐⭐⭐⭐⭐

Learn:

```text
SELECT
INSERT
UPDATE
DELETE
JOIN
GROUP BY
HAVING
subqueries
CTEs
indexes
constraints
transactions
views
```

Database design:

```text
primary key
foreign key
unique
not null
check
one-to-many
many-to-many
normalization basics
```

Also learn `EXPLAIN` and index basics.

---

# Phase 5 — Java Database Access

## 10. JDBC ⭐⭐⭐⭐⭐

Learn:

```text
Connection
PreparedStatement
ResultSet
SQLException
commit
rollback
generated keys
batch operations
```

Architecture:

```text
Java
→ JDBC API
→ PostgreSQL driver
→ PostgreSQL
```

---

## 11. DataSource + Connection Pool ⭐⭐⭐⭐⭐

Learn:

```text
DataSource
HikariCP
pool size
borrow connection
return connection
timeouts
```

Architecture:

```text
Application
→ DataSource
→ HikariCP
→ JDBC connection
→ PostgreSQL
```

---

# Phase 6 — Spring Core

## 12. Spring Core ⭐⭐⭐⭐⭐

Learn:

```text
IoC
Dependency Injection
ApplicationContext
Bean
@Component
@Service
@Repository
@Configuration
@Bean
@Primary
@Qualifier
bean scopes
bean lifecycle
```

Also learn Spring proxy/AOP basics because they later power:

```text
@Transactional
@PreAuthorize
method validation
```

---

# Phase 7 — Spring Boot

## 13. Spring Boot ⭐⭐⭐⭐⭐

Learn:

```text
@SpringBootApplication
starters
auto-configuration
application.yaml
profiles
external configuration
embedded server
Actuator basics
```

Startup mental model:

```text
main()
→ SpringApplication.run()
→ ApplicationContext
→ auto-configuration
→ beans
→ embedded server
```

---

# Phase 8 — REST APIs

## 14. REST API ⭐⭐⭐⭐⭐

Learn:

```text
@RestController
@RequestMapping
@GetMapping
@PostMapping
@PutMapping
@PatchMapping
@DeleteMapping
@PathVariable
@RequestParam
@RequestBody
ResponseEntity
```

HTTP:

```text
GET POST PUT PATCH DELETE
200 201 204 400 401 403 404 409 500
Content-Type
Accept
Authorization
Cookie
```

Architecture:

```text
Controller
→ HTTP boundary

Service
→ business/use-case boundary

Repository
→ persistence boundary
```

---

## 15. DTO Design ⭐⭐⭐⭐⭐

Understand:

```text
Request DTO
≠ Response DTO
≠ Entity
```

Do not expose persistence entities directly as API contracts.

---

# Phase 9 — Validation and Error Handling

## 16. Validation ⭐⭐⭐⭐⭐

Learn:

```text
@NotNull
@NotBlank
@Size
@Pattern
@Positive
@Valid
@Validated
```

Remember:

```text
validation
≠ authorization
≠ database constraints
```

---

## 17. Global Exception Handling ⭐⭐⭐⭐⭐

Learn:

```text
@RestControllerAdvice
@ExceptionHandler
ProblemDetail
ResponseEntityExceptionHandler
```

Example mappings:

```text
Not found → 404
Conflict → 409
Validation → 400
```

---

# Phase 10 — Persistence

## 18. Spring Data JPA + Hibernate ⭐⭐⭐⭐⭐

Learn:

```text
@Entity
@Id
@GeneratedValue
@Column
JpaRepository
derived queries
JPQL
relationships
```

Hibernate fundamentals:

```text
Persistence Context
managed / detached entities
dirty checking
flush
commit
first-level cache
```

Relationships:

```text
@OneToMany
@ManyToOne
@OneToOne
@ManyToMany
mappedBy
cascade
orphanRemoval
```

Performance:

```text
N+1
fetch join
@EntityGraph
projections
```

Architecture:

```text
Repository
→ Spring Data JPA
→ Hibernate
→ JDBC
→ HikariCP
→ PostgreSQL
```

---

# Phase 11 — Database Migration

## 19. Flyway or Liquibase ⭐⭐⭐⭐⭐

Learn one deeply first. Flyway is a good first choice.

Learn:

```text
versioned migrations
repeatable migrations
flyway_schema_history
checksums
migrate
validate
baseline
repair
```

Ownership:

```text
Flyway
→ schema evolution

Hibernate ddl-auto: validate
→ mapping/schema validation
```

---

# Phase 12 — Transactions

## 20. Spring Transactions Deeper ⭐⭐⭐⭐⭐

Learn:

```text
@Transactional
REQUIRED
REQUIRES_NEW
rollback rules
checked vs unchecked exceptions
readOnly
isolation
timeout
rollback-only
UnexpectedRollbackException
```

Important:

```text
flush
≠ commit
```

Also learn:

```text
@Version optimistic locking
```

and remember that DB rollback cannot automatically undo external email/HTTP/message side effects.

---

# Phase 13 — Security

## 21. Spring Security ⭐⭐⭐⭐⭐

Learn:

```text
Authentication
Authorization
SecurityFilterChain
AuthenticationManager
ProviderManager
AuthenticationProvider
DaoAuthenticationProvider
UserDetails
UserDetailsService
PasswordEncoder
GrantedAuthority
SecurityContext
SecurityContextHolder
```

Session flow:

```text
login
→ Authentication
→ SecurityContext
→ HttpSession
→ JSESSIONID
```

Authorization:

```text
hasRole(...)
hasAuthority(...)
@PreAuthorize
```

Also learn:

```text
401 vs 403
AuthenticationEntryPoint
AccessDeniedHandler
CSRF
CORS
logout
anonymous authentication
```

---

# Phase 14 — Testing

## 22. Testing ⭐⭐⭐⭐⭐

Learn:

```text
Unit tests
Integration tests
MVC slice tests
Repository tests
Full application tests
```

Tools:

```text
JUnit Jupiter
Mockito
AssertJ
Spring Test
MockMvc
Spring Security Test
Testcontainers
```

Important Spring testing concepts:

```text
@SpringBootTest
@WebMvcTest
@DataJpaTest
@Mock / @InjectMocks
mocked Spring beans
@WithMockUser
csrf()
formLogin()
```

Database integration target:

```text
PostgreSQL Testcontainer
→ Flyway
→ Hibernate validate
→ tests
```

Remember:

```text
coverage
≠ correctness
```

---

# Phase 15 — Docker

## 23. Docker Fundamentals ⭐⭐⭐⭐⭐

Learn:

```text
image
container
Dockerfile
layer
port
volume
environment variable
network
registry
```

---

## 24. Dockerize Spring Boot + PostgreSQL ⭐⭐⭐⭐⭐

Learn:

```text
multi-stage Dockerfile
Docker Compose
service DNS
healthcheck
named volumes
environment variables
```

Target architecture:

```text
docker compose up
→ PostgreSQL
→ Spring Boot
→ Flyway
→ Hibernate validation
→ API ready
```

---

# Phase 16 — Deployment

## 25. Deployment ⭐⭐⭐⭐⭐

Learn:

```text
production profiles
environment variables
secrets
managed PostgreSQL
HTTPS
domain
reverse proxy basics
health checks
logs
```

Deploy one monolithic Spring Boot application before microservices.

---

# Phase 17 — CI/CD

## 26. CI/CD ⭐⭐⭐⭐⭐

Learn GitHub Actions first.

Pipeline:

```text
git push
→ checkout
→ setup Java
→ mvn clean test
→ package
→ Docker image
→ registry
→ deploy
```

---

# Phase 18 — Observability

## 27. Spring Boot Actuator ⭐⭐⭐⭐⭐

Learn:

```text
health
info
metrics
readiness
liveness
```

---

## 28. Logging ⭐⭐⭐⭐⭐

Learn:

```text
SLF4J
Logback
log levels
structured logging basics
correlation IDs
```

Never log secrets, passwords, session IDs, or tokens.

---

## 29. Metrics and Monitoring ⭐⭐⭐⭐

Learn:

```text
Micrometer
Prometheus
Grafana
```

Architecture:

```text
Spring Boot
→ Micrometer
→ Prometheus
→ Grafana
```

---

# Phase 19 — Redis and Caching

## 30. Redis ⭐⭐⭐⭐⭐

Learn:

```text
strings
hashes
sets
sorted sets
TTL
atomic operations
```

---

## 31. Spring Cache ⭐⭐⭐⭐

Learn:

```text
@Cacheable
@CachePut
@CacheEvict
```

Mental model:

```text
request
→ cache hit? return
→ miss? database
→ cache result
```

Also learn cache invalidation, stale data, TTL, and stampede basics.

---

# Phase 20 — Production API Design

## 32. OpenAPI + Swagger ⭐⭐⭐⭐

Learn:

```text
OpenAPI
Swagger UI
request/response schemas
status codes
security schemes
```

---

## 33. Pagination, Filtering, Sorting ⭐⭐⭐⭐⭐

Learn production-friendly API querying and large-data concerns.

---

## 34. Idempotency ⭐⭐⭐⭐

Important for:

```text
payments
checkout
order creation
retries
```

Learn idempotency keys and duplicate-request prevention.

---

# Phase 21 — Messaging

## 35. Messaging Fundamentals ⭐⭐⭐⭐⭐

Learn:

```text
producer
consumer
queue
topic
message
acknowledgement
retry
dead-letter queue
```

Understand synchronous REST vs asynchronous messaging.

---

## 36. RabbitMQ or Kafka ⭐⭐⭐⭐

Suggested order:

```text
RabbitMQ
→ easier queue-based introduction

Kafka
→ event streaming and distributed event systems
```

---

## 37. Messaging Reliability ⭐⭐⭐⭐⭐

Learn:

```text
at-most-once
at-least-once
duplicate delivery
idempotent consumers
ordering
retry
dead-letter handling
```

---

## 38. Transactional Outbox ⭐⭐⭐⭐

Problem:

```text
DB commit succeeds
message publish fails
```

Pattern:

```text
business change
+
outbox row
→ same transaction
→ later publisher sends event
```

---

# Phase 22 — Resilience

## 39. Resilience Patterns ⭐⭐⭐⭐

Learn:

```text
timeout
retry
exponential backoff
circuit breaker
bulkhead
fallback
rate limiting
```

Then learn Resilience4j.

Do not retry non-idempotent operations blindly.

---

# Phase 23 — Architecture

## 40. Modular Monolith ⭐⭐⭐⭐⭐

Learn:

```text
package-by-feature
module boundaries
dependency direction
public module APIs
```

Prefer:

```text
books/
orders/
users/
payments/
```

over only:

```text
controller/
service/
repository/
entity/
```

A well-structured modular monolith is an excellent foundation for microservices.

---

## 41. Clean / Hexagonal Architecture ⭐⭐⭐⭐

Learn:

```text
domain
application
ports
adapters
infrastructure
```

Principle:

```text
business logic
should not depend directly
on framework details
```

---

## 42. DDD Basics ⭐⭐⭐⭐

Learn:

```text
Entity
Value Object
Aggregate
Aggregate Root
Repository
Domain Service
Domain Event
Bounded Context
Ubiquitous Language
```

---

# Phase 24 — OAuth2 and OIDC

## 43. OAuth 2.0 ⭐⭐⭐⭐⭐

Learn:

```text
client
resource owner
authorization server
resource server
access token
scope
```

Important flows:

```text
Authorization Code + PKCE
Client Credentials
```

---

## 44. OpenID Connect ⭐⭐⭐⭐⭐

Understand:

```text
OAuth2
→ authorization

OIDC
→ authentication / identity layer
```

Learn ID tokens, claims, issuer, and audience.

---

## 45. Spring Security Resource Server ⭐⭐⭐⭐

Learn:

```text
Bearer token
JWT validation
JWKS
scopes
authorities
```

Remember:

```text
OAuth2
≠ JWT
```

JWT is only one possible token format.

---

# Phase 25 — Microservices Fundamentals

## 46. Why Microservices Exist ⭐⭐⭐⭐⭐

Understand the benefits:

```text
independent deployment
team autonomy
independent scaling
business boundaries
```

and the costs:

```text
network failures
latency
distributed debugging
data consistency
security
deployment complexity
observability
```

---

## 47. Service Boundaries ⭐⭐⭐⭐⭐

Split by business capability, not technical layer.

Better examples:

```text
catalog-service
order-service
payment-service
notification-service
```

when the domain genuinely justifies them.

---

## 48. Database per Service ⭐⭐⭐⭐⭐

Understand:

```text
service
→ owns its data
```

Avoid multiple services directly sharing the same tables.

---

# Phase 26 — Service Communication

## 49. Synchronous Communication ⭐⭐⭐⭐⭐

Learn:

```text
HTTP
REST
RestClient / WebClient
timeouts
retries
error handling
```

---

## 50. Asynchronous Communication ⭐⭐⭐⭐⭐

Learn:

```text
events
commands
Kafka/RabbitMQ
eventual consistency
idempotency
```

---

# Phase 27 — Gateway and Configuration

## 51. API Gateway ⭐⭐⭐⭐

Learn:

```text
routing
authentication integration
TLS termination
rate limiting
cross-cutting concerns
```

Do not put business logic into the gateway.

---

## 52. Centralized Configuration ⭐⭐⭐

Understand the problem before tools such as Spring Cloud Config or platform-native configuration.

---

## 53. Service Discovery ⭐⭐⭐

Understand:

```text
service name
→ network location
```

In Kubernetes, DNS often handles this.

---

# Phase 28 — Distributed Observability

## 54. Distributed Tracing ⭐⭐⭐⭐⭐

Learn:

```text
trace ID
span ID
context propagation
OpenTelemetry
Micrometer Tracing
```

Example:

```text
Gateway
→ Order Service
→ Payment Service
→ Notification Service
```

One user action can produce one distributed trace.

---

# Phase 29 — Distributed Data

## 55. Eventual Consistency ⭐⭐⭐⭐⭐

Understand why several services usually cannot share one local ACID transaction.

Learn:

```text
eventual consistency
reconciliation
compensation
```

---

## 56. Saga Pattern ⭐⭐⭐⭐

Learn:

```text
orchestration
choreography
compensating actions
```

---

## 57. Distributed Idempotency ⭐⭐⭐⭐⭐

Critical for:

```text
HTTP retries
message redelivery
payments
event consumers
```

---

# Phase 30 — Microservices Security

## 58. OAuth2/OIDC in Microservices ⭐⭐⭐⭐⭐

Typical architecture:

```text
Authorization Server
→ Access Token
→ API Gateway
→ Microservice
```

Learn scopes, audience, service identity, and token validation.

---

## 59. Service-to-Service Authentication ⭐⭐⭐⭐

Learn:

```text
OAuth2 Client Credentials
mTLS basics
workload identity
```

---

# Phase 31 — Spring Cloud

## 60. Spring Cloud — Selectively ⭐⭐⭐

Learn tools only after understanding the problem they solve.

Possible topics:

```text
Spring Cloud Gateway
Spring Cloud Config
OpenFeign when useful
resilience integrations
```

Do not memorize Spring Cloud annotations without distributed-systems understanding.

---

# Phase 32 — Kubernetes Basics

## 61. Kubernetes ⭐⭐⭐⭐

Learn:

```text
Pod
Deployment
Service
ConfigMap
Secret
Ingress
readiness probe
liveness probe
horizontal scaling
```

Architecture:

```text
Docker image
→ Pod
→ Deployment
→ Service
```

---

# Phase 33 — Final Capstone

## 62. Build a Production-Style System ⭐⭐⭐⭐⭐

Start as a modular monolith.

Add incrementally:

```text
Testing
Security
Flyway
Docker
Deployment
CI/CD
Observability
Redis
Messaging
Resilience
OAuth2/OIDC
```

Then identify one clear bounded context and extract only that service.

A possible final architecture:

```text
API Gateway
    ↓
Catalog Service
Order Service
Payment Service
Notification Service
```

Possible infrastructure:

```text
PostgreSQL per service
Redis
Kafka/RabbitMQ
Docker
Testcontainers
CI/CD
OpenTelemetry
OAuth2/OIDC
```

Do not add every technology at once.

---

# Parallel Track — DSA

Keep DSA separate from the backend roadmap:

```text
Arrays
Strings
Hashing
Two Pointers
Sliding Window
Stack / Queue
Linked List
Binary Search
Trees
Heaps
Graphs
Backtracking
Intervals
Greedy
Dynamic Programming
```

Practice consistently.

---

# Parallel Track — Computer Science Fundamentals

Study gradually:

```text
Networking
HTTP / TCP / TLS
Operating systems basics
Database internals
Concurrency
Memory
Distributed systems
```

These become increasingly important as you move toward senior backend and microservices work.

---

# Recommended Complete Sequence

```text
Core Java
↓
OOP
↓
Collections + Generics
↓
Exceptions
↓
Java I/O
↓
Concurrency basics
↓
Maven + Git
↓
SQL + PostgreSQL
↓
JDBC
↓
DataSource + HikariCP
↓
Spring Core
↓
Spring Boot
↓
REST API
↓
DTO Design
↓
Validation + Global Exception Handling
↓
Spring Data JPA + Hibernate
↓
Flyway / Liquibase
↓
Spring Transactions deeper
↓
Spring Security
↓
Testing
↓
Docker
↓
Deployment
↓
CI/CD
↓
Observability
↓
Redis + Caching
↓
OpenAPI + production API concerns
↓
Messaging
↓
Transactional Outbox
↓
Resilience
↓
Modular Monolith
↓
Clean / Hexagonal Architecture
↓
DDD Basics
↓
OAuth2 + OIDC
↓
Microservices Fundamentals
↓
Service-to-Service Communication
↓
API Gateway
↓
Distributed Tracing
↓
Eventual Consistency
↓
Saga
↓
Microservice Security
↓
Spring Cloud
↓
Kubernetes Basics
```

---

# Priority Summary

| Stage | Topic | Priority |
|---:|---|---|
| 1 | Core Java | ⭐⭐⭐⭐⭐ |
| 2 | OOP | ⭐⭐⭐⭐⭐ |
| 3 | Collections + Generics | ⭐⭐⭐⭐⭐ |
| 4 | Exceptions | ⭐⭐⭐⭐⭐ |
| 5 | Java I/O | ⭐⭐⭐⭐ |
| 6 | Concurrency basics | ⭐⭐⭐⭐ |
| 7 | Maven + Git | ⭐⭐⭐⭐⭐ |
| 8 | SQL + PostgreSQL | ⭐⭐⭐⭐⭐ |
| 9 | JDBC | ⭐⭐⭐⭐⭐ |
| 10 | DataSource + HikariCP | ⭐⭐⭐⭐⭐ |
| 11 | Spring Core | ⭐⭐⭐⭐⭐ |
| 12 | Spring Boot | ⭐⭐⭐⭐⭐ |
| 13 | REST API + DTOs | ⭐⭐⭐⭐⭐ |
| 14 | Validation + Error Handling | ⭐⭐⭐⭐⭐ |
| 15 | JPA + Hibernate | ⭐⭐⭐⭐⭐ |
| 16 | Flyway / Liquibase | ⭐⭐⭐⭐⭐ |
| 17 | Transactions | ⭐⭐⭐⭐⭐ |
| 18 | Spring Security | ⭐⭐⭐⭐⭐ |
| 19 | Testing | ⭐⭐⭐⭐⭐ |
| 20 | Docker | ⭐⭐⭐⭐⭐ |
| 21 | Deployment | ⭐⭐⭐⭐⭐ |
| 22 | CI/CD | ⭐⭐⭐⭐⭐ |
| 23 | Observability | ⭐⭐⭐⭐⭐ |
| 24 | Redis + Caching | ⭐⭐⭐⭐ |
| 25 | Messaging | ⭐⭐⭐⭐⭐ |
| 26 | Resilience | ⭐⭐⭐⭐ |
| 27 | Modular Monolith | ⭐⭐⭐⭐⭐ |
| 28 | Clean Architecture + DDD | ⭐⭐⭐⭐ |
| 29 | OAuth2 + OIDC | ⭐⭐⭐⭐⭐ |
| 30 | Microservices | ⭐⭐⭐⭐⭐ |
| 31 | Distributed systems | ⭐⭐⭐⭐⭐ |
| 32 | Spring Cloud | ⭐⭐⭐ |
| 33 | Kubernetes basics | ⭐⭐⭐⭐ |

---

# Final Principle

Do not treat the roadmap as:

```text
"I learned the annotation,
therefore I learned the topic."
```

Instead:

```text
Concept
→ Exercise
→ Real project
→ Failure/debugging
→ Explain the architecture
```

The strongest progression is:

```text
Build one good monolith
→ test it
→ secure it
→ deploy it
→ observe it
→ make it resilient
→ organize it into modules
→ then distribute selected boundaries
  into microservices
```

That sequence teaches not only *how* to build microservices, but also *why* they are needed.
