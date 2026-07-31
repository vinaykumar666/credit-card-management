# Credit Card Management Platform — Architecture & Design README

A production-grade, cloud-native microservices platform for credit card account management,
OAuth2 authentication, payments, notifications, BFF aggregation, and an Angular dashboard —
built on Spring Boot 3 / Java 21, PostgreSQL, Kafka, Docker, Kubernetes, and GitHub Actions → EC2.

This document is the **single source of truth** for *why* each architectural decision was made,
not just *what* was built.

**Start here:**
- [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md) — spoon-fed local run
- [docs/KAFKA_SETUP.md](docs/KAFKA_SETUP.md) — Kafka step-by-step
- [postman/README.md](postman/README.md) — Postman import + run order
- [docs/USERS.md](docs/USERS.md) — seed users & payees
- [docs/BANKING_FEATURES.md](docs/BANKING_FEATURES.md) — transfer, bill pay, beneficiaries
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) · [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) · [docs/PRODUCTION_READINESS.md](docs/PRODUCTION_READINESS.md)
- [docs/CLASS_CATALOG.md](docs/CLASS_CATALOG.md) · [docs/CI_CD.md](docs/CI_CD.md) · [docs/JAVA21.md](docs/JAVA21.md)

---

## 0. Getting Started

### One-click on Windows (recommended — **no Docker**)
1. Have (or let the script install) native **PostgreSQL** on Windows. Your machine already can use `postgresql-x64-*` service.
2. Right-click **`start-all-local.bat`** → **Run as administrator** (once, so Postgres password can be set to `admin`).
3. Wait — script finds/downloads Java / Maven / Node / Kafka, configures Postgres + pgAdmin, builds, and starts every service + Angular.

| What | URL / credentials |
|------|-------------------|
| UI | http://localhost:4200 — `ada.lovelace@cards.local` / `Password123!` |
| pgAdmin | desktop app — master/DB password **`admin`** |
| Postgres | `localhost:5432` — user `cards` / password **`admin`** |
| Kafka | `localhost:9092` (native download under `.runtime`) |

Stop apps + Kafka: **`stop-all-local.bat`** (leaves Windows Postgres service running).

### Prerequisites (manual path)
- Java 21, Maven 3.9+, Node 20+ (Angular)
- **Native local (no Docker):** PostgreSQL 16 + Kafka on `localhost` — see [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md)
- Optional: Docker Compose, kubectl

### Build
```bash
mvn clean verify
```

### Run locally (manual, no one-click)
1. Create DBs: `psql -U postgres -f infra/postgres/init-databases.sql`
2. Start Kafka on `localhost:9092` — [docs/KAFKA_SETUP.md](docs/KAFKA_SETUP.md)
3. Start services: `.\scripts\run-local-no-docker.ps1` (or Maven per service)
4. UI: `cd cards-dashboard-ui && npm install && npm start` → http://localhost:4200

Full spoon-fed steps: **[docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md)**

### Optional: full Docker Compose (apps in containers too)
```bash
docker compose up --build
```
UI: http://localhost:8088

### Key endpoints
| Method | Path | Service |
|--------|------|---------|
| POST | `/api/v1/auth/register\|login\|refresh` | OAuth2 AS + first-party tokens |
| GET | `/.well-known/openid-configuration` | OAuth2 discovery |
| GET/POST | `/api/v1/accounts/**` | account-details |
| POST/GET | `/api/v1/payments/**` | payment |
| GET | `/api/v1/notifications/**` | notification |
| POST | `/api/v1/enterprise/payments/authorize` | enterprise adapter |
| GET | `/bff/v1/dashboard` | BFF (requires `X-Channel-Id`, `X-Client-Id`, Bearer) |

Error codes: [`cards-common/src/main/resources/error-codes.yml`](cards-common/src/main/resources/error-codes.yml)

### Deploy
- Kubernetes: `kubectl apply -k k8s/overlays/dev`
- EC2 auto-deploy: see [docs/CI_CD.md](docs/CI_CD.md)

---

## 1. Service Inventory

| # | Service | Responsibility | Data Store | Sync/Async |
|---|---------|----------------|------------|------------|
| 1 | `cards-account-details-service` | User profile, account details, transaction history | PostgreSQL (own schema/DB) | REST (sync) |
| 2 | `cards-authentication-service` | OAuth2 Authorization Server, users/roles, login/register | PostgreSQL (users/roles) | REST (sync) |
| 3 | `cards-payment-service` | Payment orchestration, ledger updates | PostgreSQL | REST + Kafka (async events) |
| 4 | `cards-notification-service` | Email/SMS/push alerts on account & payment events | PostgreSQL (notification log) | Kafka consumer (async) |
| 5 | `cards-api-gateway-service` | Entry point: routing, correlation/tenant headers, CB, bulkhead, JWT | — (stateless) | REST |
| 6 | `cards-enterprise-api-service` | Adapter to external payment networks / core banking | — / cache | REST |
| 7 | `cards-bff-dashboard-service` | UI BFF aggregation; enforces `channelId` / `clientId` | — (stateless) | REST |
| 8 | `cards-dashboard-ui` | Angular dashboard (Login, Accounts, Payments, Notifications) | — | Browser → BFF |

**Why microservices at all (vs. a modular monolith)?**
Each of these domains (auth, accounts, payments, notifications) has a different scaling profile,
release cadence, and failure blast radius. Payments must be independently scalable and auditable;
notifications are bursty and best decoupled via a queue; auth is security-sensitive and benefits
from being a small, hardened, independently-patchable surface. Splitting them lets each team/service
scale, deploy, and fail independently — the core promise of microservices — at the cost of added
operational complexity, which we offset with the platform patterns below.

---

## 2. The 12-Factor App — How & Why Applied

| Factor | Application in this platform | Why |
|---|---|---|
| **I. Codebase** | One Git repo per service, one deploy per environment (dev/stage/prod) from the same image | Guarantees what you tested is what you ship — no environment drift |
| **II. Dependencies** | Explicit `pom.xml`, no reliance on system-installed JARs; dependencies vendored into the Docker image | Reproducible builds; a fresh clone + build never silently depends on the host machine |
| **III. Config** | All config (DB URLs, Kafka brokers, JWT secrets) via environment variables / Kubernetes ConfigMaps & Secrets — never hardcoded or committed | Same image can run in dev/stage/prod; secrets aren't leaked into source control |
| **IV. Backing Services** | PostgreSQL, Kafka, Redis (if added for caching) are attached resources reachable via URL/config, swappable without code change | Lets us point `cards-payment-service` at a different Postgres instance (e.g., DR failover) with zero code change |
| **V. Build, Release, Run** | Strict separation: CI builds an immutable Docker image (Build) → tagged + config-injected per environment (Release) → `kubectl`/Helm runs it (Run) | Enables instant rollback to a prior Release without rebuilding |
| **VI. Processes** | Services are stateless; session/auth state lives in JWT (self-contained) or Postgres/Redis, never in-memory on the pod | Any pod can die and be replaced without losing session data — required for HPA and rolling updates |
| **VII. Port Binding** | Each service is self-contained and exports its functionality via a port binding (embedded Tomcat/Netty), not deployed into an external container | Services are portable — the API Gateway routes to them as plain HTTP endpoints, no app-server config needed |
| **VIII. Concurrency** | Scale out via process/pod replication (horizontal), not thread hacks — this is what enables Kubernetes HPA | Payments spike at month-end; we scale `cards-payment-service` pods, not vertical CPU on one box |
| **IX. Disposability** | Fast startup (Spring Boot lazy init where safe), graceful shutdown hooks draining in-flight requests/Kafka offsets before `SIGTERM` | Kubernetes routinely kills/reschedules pods — disposability is what makes that safe |
| **X. Dev/Prod Parity** | Same Docker image + same PostgreSQL/Kafka versions (via Testcontainers/docker-compose) in dev as in prod | Eliminates "works on my machine" — the #1 cause of prod-only bugs in distributed systems |
| **XI. Logs** | Services write structured JSON logs to `stdout`/`stderr` only; log aggregation (e.g., ELK/EFK, Loki) happens outside the app | The app never manages log files or rotation — that's an infra concern, keeping the service simple |
| **XII. Admin Processes** | One-off tasks (DB migrations via Flyway/Liquibase, data backfills) run as separate Kubernetes Jobs using the same release image | Same dependency versions as the running app — no drift between migration scripts and app code |

---

## 3. SOLID Principles — Applied at the Service & Class Level

- **S — Single Responsibility**: Each microservice owns exactly one bounded context (accounts ≠ payments ≠ notifications). Inside a service, controllers only handle HTTP concerns, services hold business logic, repositories only do persistence.
  *Why:* a change to notification templates should never require touching or redeploying the payment service.

- **O — Open/Closed**: Payment processing uses a `PaymentStrategy` interface (Card, NetBanking, UPI, external-network via `cards-enterprise-api-service`) — new payment rails are added as new strategy implementations, not by editing existing code.
  *Why:* reduces regression risk when the business adds a new payment method.

- **L — Liskov Substitution**: All `NotificationSender` implementations (Email, SMS, Push) are interchangeable behind the same interface and contract.
  *Why:* the Kafka consumer in `cards-notification-service` doesn't need to know or care which channel is used.

- **I — Interface Segregation**: Separate `AccountReadService` / `AccountWriteService` interfaces rather than one fat `AccountService`, so read-heavy callers (e.g., a future reporting service) don't depend on write methods they'll never call.
  *Why:* smaller, purpose-built interfaces are easier to mock, test, and reason about.

- **D — Dependency Inversion**: Controllers/services depend on repository *interfaces* (Spring Data JPA), not concrete JDBC/Postgres classes; the JWT validator in the gateway depends on a `TokenValidator` abstraction, not a concrete library.
  *Why:* lets us swap PostgreSQL for another RDBMS, or swap the JWT library, without touching business logic.

---

## 4. Design Patterns Used — and Why

### 4.1 System-level (cross-cutting) patterns

| Pattern | Where | Why |
|---|---|---|
| **API Gateway** | `cards-api-gateway-service` (Spring Cloud Gateway) | One public entry point instead of exposing 6 services to the internet — centralizes auth, rate-limiting, routing, and TLS termination |
| **Circuit Breaker** (Resilience4j) | Gateway → downstream service calls, `cards-payment-service` → `cards-enterprise-api-service` | Prevents cascading failure — if the external payment network is slow/down, we fail fast instead of exhausting threads/connections platform-wide |
| **Bulkhead** (Resilience4j) | Gateway, per downstream route | Isolates thread/connection pools per downstream service so a slow `notification-service` can't starve calls to `payment-service` |
| **Retry + Rate Limiter** (Resilience4j) | Gateway and inter-service calls | Transient network blips shouldn't surface as user-facing errors; rate limiting protects services from noisy-neighbor traffic |
| **Correlation ID / Distributed Tracing** | Gateway generates `X-Correlation-Id` if absent, propagated via headers, logged by every service, exported to Sleuth/Zipkin or OpenTelemetry | Without this, debugging "why did payment X fail" across 4 services is nearly impossible — this is the #1 production-debugging lifesaver |
| **Database per Service** | Each service owns its own Postgres schema/instance; no cross-service SQL joins | Enforces true service autonomy — a schema change in `notification-service` can never break `account-service` |
| **Saga Pattern (choreography via Kafka)** | Payment initiation → ledger update → notification, coordinated via Kafka events rather than a distributed transaction | Avoids 2-phase-commit across services (which doesn't scale); each service reacts to events and can compensate (e.g., refund event) on failure |
| **Event-Driven / Pub-Sub** (Kafka) | `cards-payment-service` publishes `PaymentCompleted`/`PaymentFailed`; `cards-notification-service` consumes | Decouples payment throughput from notification delivery speed — a slow SMS provider never slows down a payment |
| **Service Discovery** (Eureka/Kubernetes DNS) | Gateway resolves service instances dynamically instead of hardcoded IPs | Required for horizontal pod scaling — pods are ephemeral and IPs change constantly |
| **Externalized Config** (Spring Cloud Config / K8s ConfigMap+Secret) | All services | Same image, environment-specific behavior — ties directly back to 12-Factor Factor III |

### 4.2 Code-level (GoF) patterns

| Pattern | Where | Why |
|---|---|---|
| **Strategy** | `PaymentStrategy` (Card/UPI/NetBanking/External), `NotificationSender` (Email/SMS/Push) | New variants without touching existing call sites (Open/Closed) |
| **Factory** | `NotificationFactory` picks the right `NotificationSender` based on user preference/event type | Centralizes object-creation logic, keeps `if/else` chains out of business code |
| **Builder** | Building complex DTOs like `TransactionResponse`, JWT claims objects | Readable, immutable object construction instead of telescoping constructors |
| **Repository** | Spring Data JPA repositories in every service | Decouples domain/service layer from persistence details (Dependency Inversion) |
| **DTO / Mapper (MapStruct)** | Entity ↔ API contract translation in every service | Prevents leaking JPA entities (and lazy-loading issues) directly onto the wire |
| **Singleton** (via Spring bean scope) | `RestTemplate`/`WebClient` beans, `ObjectMapper`, Kafka producer factory | Avoids the overhead/connection churn of re-creating expensive clients per request |
| **Adapter** | `cards-enterprise-api-service` wraps the external/core-banking network's proprietary API in our own domain contract | Isolates the rest of the platform from a 3rd-party API's quirks — if the external vendor changes their API, only this one adapter changes |
| **Circuit Breaker as Decorator** | Resilience4j wraps outbound calls transparently | Cross-cutting resilience without polluting business logic with try/catch retry code |
| **CQRS (light)** | `cards-account-details-service`: read-heavy transaction history queries can use a read replica / projection, separate from the write path | Read and write traffic for transaction history have very different scaling needs |

---

## 5. Docker — Multi-Stage Builds

Every service uses a **multi-stage Dockerfile**:

```dockerfile
# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B          # cache deps in their own layer
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
```

**Why multi-stage, specifically:**
- **Smaller final image**: the JDK, Maven, and full source tree (hundreds of MB) never ship — only the JRE + fat JAR do. Smaller images pull faster, which matters directly for pod autoscaling speed.
- **Smaller attack surface**: no build tools, no Maven cache, no `.git` metadata in the production image — fewer CVEs to patch.
- **Layer caching**: dependencies (`pom.xml`) are copied and resolved *before* source code, so a source-only change doesn't force re-downloading the world — faster CI builds.
- **Non-root user**: runtime stage drops to an unprivileged `spring` user — a Kubernetes/Docker security best practice (also enforceable via a `PodSecurityContext`/`securityContext: runAsNonRoot: true`).

---

## 6. Kubernetes — Load Balancing & Pod Scaling

### 6.1 Load balancing
- Each service is fronted by a Kubernetes **`Service`** (ClusterIP) — kube-proxy load-balances requests across all healthy pod replicas automatically (round-robin/`iptables`/IPVS).
- The **`cards-api-gateway-service`** is the only service exposed externally, via an **Ingress** (NGINX/Traefik) or a `LoadBalancer`-type Service — this keeps a single, controlled, TLS-terminated entry point, consistent with the API Gateway pattern above.
- **Readiness probes** ensure a pod only receives traffic once it's actually ready (DB connection pool warmed, Kafka consumer group joined) — this *is* load balancing correctness, not just scaling.
- **Liveness probes** let Kubernetes kill and replace a pod stuck in a bad state (e.g., deadlocked thread pool), which the Service then automatically routes around.

### 6.2 Pod scaling
- **Horizontal Pod Autoscaler (HPA)** scales `cards-payment-service` and `cards-api-gateway-service` on CPU/memory, and — for a more meaningful signal — on custom metrics (e.g., Kafka consumer lag, requests-per-second via Prometheus Adapter).
  *Why HPA over vertical scaling:* fits 12-Factor Factor VIII (Concurrency) directly — we add stateless replicas rather than resizing one giant pod, and it plays well with rolling deployments (no downtime while resizing).
- **PodDisruptionBudget (PDB)** ensures a minimum number of pods stay up during voluntary disruptions (node drains, cluster upgrades).
- **Rolling updates** (`RollingUpdateStrategy` with `maxSurge`/`maxUnavailable`) combined with readiness probes give zero-downtime deploys.
- **Resource `requests`/`limits`** set per service so the scheduler bin-packs pods sensibly and HPA has a stable baseline to scale from.
- **Anti-affinity rules** spread replicas of the same service across nodes/zones so a single node failure doesn't take down all replicas of, say, `cards-authentication-service`.

---

## 7. Suggested Repo/Module Layout

```
credit-card-platform/
├── cards-common/                      # error-codes.yml, events, sealed exceptions
├── cards-account-details-service/
├── cards-authentication-service/      # OAuth2 Authorization Server
├── cards-payment-service/
├── cards-notification-service/
├── cards-api-gateway-service/
├── cards-enterprise-api-service/
├── cards-bff-dashboard-service/
├── cards-dashboard-ui/                # Angular
├── deploy/ec2/                        # prod compose + EC2 bootstrap
├── .github/workflows/ci-cd.yml
├── k8s/
│   ├── base/
│   └── overlays/{dev,stage,prod}/
├── docs/{LOCAL_SETUP,CI_CD,JAVA21}.md
├── docker-compose.yml
└── README.md
```

---

## 8. Implementation Status
1. ~~Scaffold each service (Spring Boot + Spring Cloud + Postgres + Flyway).~~
2. ~~Wire Kafka topics: `payment.completed`, `payment.failed`, `notification.requested`.~~
3. ~~API Gateway with Resilience4j + correlation/tenant filters + JWT.~~
4. ~~Kubernetes manifests (Deployment, Service, HPA, PDB, ConfigMap, Secret).~~
5. ~~OAuth2 Authorization Server + resource servers.~~
6. ~~YAML error codes + sealed `BusinessException` hierarchy.~~
7. ~~BFF dashboard with `X-Channel-Id` / `X-Client-Id`.~~
8. ~~Angular frontend (`cards-dashboard-ui`).~~
9. ~~CI/CD: build → test → Sonar → Nexus IQ → Docker → GHCR publish → EC2 deploy.~~

### Ports
| Service | Port |
|---------|------|
| `cards-api-gateway-service` | 8080 |
| `cards-authentication-service` | 8081 |
| `cards-account-details-service` | 8082 |
| `cards-payment-service` | 8083 |
| `cards-notification-service` | 8084 |
| `cards-enterprise-api-service` | 8085 |
| `cards-bff-dashboard-service` | 8086 |
| `cards-dashboard-ui` (Docker) | 8088 |

---

*This README is intentionally the first artifact of the project — every design decision above should be traceable back to a 12-Factor principle, a SOLID principle, or a named pattern, with the "why" stated explicitly, before any code is written.*
