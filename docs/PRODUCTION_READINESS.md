# Production Readiness & Interview Proof (10/10 Checklist)

This document is the **evidence pack** that the platform is enterprise-grade and interview-ready.

## Scorecard

| Pillar | Evidence | Status |
|--------|----------|--------|
| Microservices + DB-per-service | 6 domain services + BFF + gateway; separate Postgres DBs | Done |
| OAuth2 | Spring Authorization Server + resource servers + JWKS | Done |
| API Gateway patterns | Routing, CB, bulkhead, correlation, tenant headers | Done |
| Async / Saga | Kafka `payment.*` → notification consumer | Done |
| SOLID / GoF | Strategy, Factory, Adapter, CQRS interfaces, Repository | Done |
| 12-Factor | Env config, Dockerfile, stdout logs, disposability | Done |
| Error standards | YAML `error-codes.yml`, sealed exceptions, ErrorResponse | Done |
| BFF + tenancy | channelId/clientId enforced | Done |
| Frontend | Production UI (AURUM) → BFF only | Done |
| CI/CD | Build→Test→Sonar→Nexus IQ→Docker→Publish→EC2 | Done |
| K8s | Deploy/Service/HPA/PDB/Ingress overlays | Done |
| Java 21 | Records, sealed, pattern matching, virtual threads | Done |
| Docs | LOCAL_SETUP, ARCHITECTURE, DEPLOYMENT, USERS, CLASS_CATALOG | Done |
| Seed data | Auth + Account initializers + USERS.md | Done |
| Code docs | JavaDoc on classes/methods + CLASS_CATALOG | Done |

## Interview talking points (memorize)

1. **Why microservices?** Different scale/failure domains for auth, payments, notifications.
2. **Why BFF?** UI needs aggregation without chatty browser→N calls; enforces channel/client.
3. **Why OAuth2 AS?** Standard tokens + JWKS; swap to Keycloak later via issuer-uri.
4. **Why Kafka saga?** Avoid 2PC; payment completes then notification reacts; compensate via failed events.
5. **Why YAML error codes?** Product/ops can change copy without redeploying business logic.
6. **Why Resilience4j?** Fail fast to external networks; isolate bulkheads per route.
7. **Why virtual threads?** Cheap blocking I/O concurrency on Java 21 Tomcat.
8. **Why multi-stage Docker?** Smaller attack surface + faster pulls for HPA.
9. **Why HPA + PDB?** Scale payments at month-end; survive node drains.
10. **Why correlation id?** Debug a single payment across 4 services in logs.

## Demo script (5 minutes)

1. `docker compose up --build`
2. Open UI → quick-fill **Ada** → login
3. Show dashboard metrics (seed account + txs)
4. Initiate CARD payment → show Kafka-driven notification path in logs
5. Force bad password → show `AUTH_001` from YAML
6. Open `docs/ARCHITECTURE.md` + `docs/DEPLOYMENT.md` diagrams
7. Point to `.github/workflows/ci-cd.yml` stages

## Gaps to call out honestly (senior maturity)

These are intentional next steps, not blockers for a strong portfolio:

- Persist RSA signing keys (current AS generates per process — fine for demo; prod needs mounted keys)
- Enable `@Profile("!prod")` on seed initializers before real prod traffic
- Add distributed tracing exporter (OTel) beyond correlation headers
- Add Redis rate limiting at gateway if abusive traffic is a threat model
- Wire real SMS/email providers behind `NotificationSender`

Calling these out in an interview **increases** credibility.
