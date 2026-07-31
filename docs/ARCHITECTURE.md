# System Design — Development Architecture

Interview-ready view of how the platform is shaped in local/dev.

## Context (C4 Level 1)

```mermaid
flowchart TB
  User[Cardholder_or_Operator]
  UI[AURUM_Angular_UI]
  BFF[cards_bff_dashboard]
  GW[cards_api_gateway]
  Auth[OAuth2_Auth_Server]
  Acct[account_service]
  Pay[payment_service]
  Notif[notification_service]
  Ent[enterprise_adapter]
  PG[(PostgreSQL_DBs)]
  K[(Kafka)]

  User --> UI
  UI --> Auth
  UI --> BFF
  BFF --> GW
  GW --> Auth
  GW --> Acct
  GW --> Pay
  GW --> Notif
  GW --> Ent
  Pay --> Ent
  Pay --> K
  K --> Notif
  Auth --> PG
  Acct --> PG
  Pay --> PG
  Notif --> PG
```

## Request path (happy path)

```mermaid
sequenceDiagram
  participant UI as Angular_UI
  participant Auth as Auth_Service
  participant BFF as BFF
  participant GW as Gateway
  participant Acct as Account_Service

  UI->>Auth: POST /api/v1/auth/login
  Auth-->>UI: accessToken + refreshToken
  UI->>BFF: GET /bff/v1/dashboard
  Note over UI,BFF: Bearer + X-Channel-Id + X-Client-Id + X-Correlation-Id
  BFF->>GW: GET /api/v1/accounts/user/{id}
  GW->>Acct: forward JWT + tenant headers
  Acct-->>GW: accounts JSON
  GW-->>BFF: accounts JSON
  BFF-->>UI: DashboardResponse
```

## Bounded contexts

| Context | Service | Owns |
|---------|---------|------|
| Identity | authentication | Users, roles, OAuth2 tokens, refresh tokens |
| Account | account-details | Profiles, cards, transaction history |
| Payment | payment | Payment orchestration, ledger, Kafka publish |
| Notify | notification | Channel senders, notification log |
| Edge | gateway | Routing, CB, tenant/correlation, JWT gate |
| Experience | BFF + Angular | Aggregation, UX, channel/client enforcement |
| External | enterprise-api | Adapter to “core banking” simulation |

## Cross-cutting standards

- **12-Factor**: config via env, stdout JSON logs, disposability, backing services
- **SOLID**: Strategy payments, segregated account read/write, NotificationSender Liskov
- **Error codes**: YAML in `cards-common` (`error-codes.yml`) — never hardcode messages
- **Java 21**: records, sealed exceptions, pattern matching, virtual threads — see [JAVA21.md](JAVA21.md)
- **Tenancy**: `X-Channel-Id` + `X-Client-Id` on BFF/gateway

## Local topology (ports)

```mermaid
flowchart LR
  UI4200[UI_4200_or_8088] --> BFF8086[BFF_8086]
  BFF8086 --> GW8080[GW_8080]
  GW8080 --> S8081[Auth_8081]
  GW8080 --> S8082[Acct_8082]
  GW8080 --> S8083[Pay_8083]
  GW8080 --> S8084[Notif_8084]
  GW8080 --> S8085[Ent_8085]
  S8083 --> K9092[Kafka_9092]
  K9092 --> S8084
  PG5432[(Postgres_5432)]
  S8081 --> PG5432
  S8082 --> PG5432
  S8083 --> PG5432
  S8084 --> PG5432
```

## Class map

See [CLASS_CATALOG.md](CLASS_CATALOG.md) for every major class in one-line English.
