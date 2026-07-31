# Deployment Design — Production Path

## Target architecture (EC2 + Docker Compose + GHCR)

```mermaid
flowchart TB
  Dev[Developer_push_to_main]
  GHA[GitHub_Actions]
  Sonar[SonarQube]
  IQ[Nexus_IQ]
  GHCR[GHCR_Images]
  EC2[EC2_Host]
  ALB[Optional_ALB_or_SG_ports]
  Users[End_Users]

  Dev --> GHA
  GHA --> Sonar
  GHA --> IQ
  GHA --> GHCR
  GHA -->|SSH_compose_up| EC2
  GHCR --> EC2
  Users --> ALB
  ALB --> EC2
```

## Pipeline stages (fail-fast)

```mermaid
flowchart LR
  B[1_Build] --> T[2_Test_JaCoCo]
  T --> S[3_Sonar_QG]
  T --> N[4_Nexus_IQ]
  S --> D[5_Docker_Publish]
  N --> D
  D --> E[6_EC2_Deploy]
```

Details and secrets: [CI_CD.md](CI_CD.md)

## Runtime on EC2

Compose file: [`deploy/ec2/docker-compose.prod.yml`](../deploy/ec2/docker-compose.prod.yml)

```mermaid
flowchart TB
  subgraph ec2 [EC2_VM]
    UI[UI_nginx_80]
    BFF[BFF_8086]
    GW[Gateway_8080]
    Auth[Auth]
    Acct[Account]
    Pay[Payment]
    Notif[Notification]
    Ent[Enterprise]
    PG[(Postgres)]
    K[Kafka]
  end

  UI --> BFF
  BFF --> GW
  GW --> Auth
  GW --> Acct
  GW --> Pay
  GW --> Notif
  Pay --> Ent
  Pay --> K
  K --> Notif
  Auth --> PG
  Acct --> PG
  Pay --> PG
  Notif --> PG
```

## Kubernetes path (alternate)

Kustomize bases under `k8s/base` with overlays `dev` / `stage` / `prod`:

- Deployment + Service + ConfigMap + Secret
- HPA + PDB
- Ingress only on gateway (`cards.local`)
- Anti-affinity, probes, non-root securityContext

```bash
kubectl apply -k k8s/overlays/prod
```

## Production controls checklist

| Control | How |
|---------|-----|
| Immutable artifacts | GHCR image tag = git SHA |
| Secrets | GitHub Secrets + EC2 `.env` / K8s Secrets |
| Health | `/actuator/health/liveness` + `readiness` |
| Rollback | `IMAGE_TAG=<previousSha> docker compose up -d` |
| Tenant isolation | Channel/client allow-lists in YAML |
| Auth | OAuth2 JWT + JWKS issuer |
| SCA / quality | Nexus IQ + Sonar quality gate |
| Observability | Structured JSON logs + correlation id |

## Disable seed data in prod

Set `app.seed.enabled=false` (recommended next hardening) or exclude `*DataInitializer` with `@Profile("!prod")` before go-live with real customers.
