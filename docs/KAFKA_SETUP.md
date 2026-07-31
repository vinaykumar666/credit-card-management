# Kafka Setup — Each Step

Kafka is used for the payment → notification saga:
- `payment.completed`
- `payment.failed`
- `notification.requested`

Producer: `cards-payment-service`  
Consumer: `cards-notification-service`

---

## Option A — Easiest (Docker Compose, recommended)

### Step 1 — Start Docker Desktop
Wait until it shows **Running**.

### Step 2 — Start Kafka (and Postgres)
From the repo root:

```powershell
cd C:\Users\medip\credit-card-management
docker compose up postgres kafka -d
```

### Step 3 — Wait until Kafka is healthy
```powershell
docker compose ps
```
`kafka` should show **healthy** (or running). First boot can take 30–60 seconds.

### Step 4 — Verify broker from inside the container
```powershell
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```
Empty list is OK at first — topics auto-create when payment service publishes.

### Step 5 — Start payment + notification services
Either full stack:
```powershell
docker compose up --build
```
Or only the apps that use Kafka (with infra already up):
```powershell
docker compose up cards-payment-service cards-notification-service cards-enterprise-api-service -d
```

### Step 6 — Create a payment (triggers events)
Use Postman: **02 BFF → Initiate Payment** after Login (Ada),  
or:

```powershell
# after login, set $token
curl -X POST http://localhost:8086/bff/v1/payments `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -H "X-Channel-Id: WEB" `
  -H "X-Client-Id: cards-dashboard-ui" `
  -d "{\"accountId\":\"a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1\",\"userId\":\"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa\",\"amount\":25.5,\"currency\":\"USD\",\"paymentMethod\":\"CARD\"}"
```

### Step 7 — Confirm topics exist
```powershell
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```
You should see:
- `payment.completed` and/or `payment.failed`
- possibly `notification.requested`

### Step 8 — Read messages (optional)
```powershell
docker compose exec kafka kafka-console-consumer.sh `
  --bootstrap-server localhost:9092 `
  --topic payment.completed `
  --from-beginning `
  --timeout-ms 5000
```

### Step 9 — Confirm notification service consumed
```powershell
docker compose logs cards-notification-service --tail=50
```
Look for `Received payment.completed` / dispatch logs.

---

## Option B — Kafka only for host-run Java apps

### Step 1
```powershell
docker compose up postgres kafka -d
```

### Step 2 — Point apps at the broker
In each service env (or `application.yml` default):
```text
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**Important:** Compose advertises `kafka:9092` for **containers**.  
If your Java apps run on the **host** and cannot connect, use this dual-listener override (advanced):

1. Stop kafka: `docker compose stop kafka`
2. Prefer running payment/notification **inside compose** (Option A), which always works.
3. Or run the full stack with `docker compose up` so broker and clients share the Docker network.

### Step 3 — Run services
```powershell
mvn -pl cards-payment-service -am spring-boot:run
mvn -pl cards-notification-service -am spring-boot:run
```

---

## Topics (reference)

| Topic | Publisher | Consumer | When |
|-------|-----------|----------|------|
| `payment.completed` | payment-service | notification-service | Payment strategy succeeds |
| `payment.failed` | payment-service | notification-service | Payment strategy fails |
| `notification.requested` | payment-service (optional direct) | notification-service | Explicit notify request |

Constants: `cards-common` → `KafkaTopics.java`  
Auto-create is enabled in compose (`KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true`).

---

## Manual topic create (if auto-create is off)

```powershell
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic payment.completed --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic payment.failed --partitions 3 --replication-factor 1
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic notification.requested --partitions 3 --replication-factor 1
```

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Connection refused` to 9092 | `docker compose up kafka -d` and wait for healthy |
| Topics never appear | Send a payment first; check payment-service logs for Kafka errors |
| Consumer not reading | Same `group-id` already committed — use a fresh group or `--from-beginning` in console consumer |
| Host app vs `kafka:9092` | Run apps in Docker, or use Option A full compose |
| Reclaim disk | `docker compose down -v` (wipes Kafka + Postgres volumes) |

---

## What is required (checklist)

- [ ] Docker Desktop running  
- [ ] `docker compose up postgres kafka -d` (minimum)  
- [ ] Payment + notification services up  
- [ ] Login + Initiate Payment (Postman or UI)  
- [ ] Topics listed / consumer logs show events  
