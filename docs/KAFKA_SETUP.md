# Kafka Setup — Each Step

Kafka carries the payment → notification saga:
- `payment.completed`
- `payment.failed`
- `notification.requested`

Producer: `cards-payment-service`  
Consumer: `cards-notification-service`  
Apps expect: **`localhost:9092`** when running without Docker.

---

## Option B — Native / no Docker (Windows) — preferred with local Maven

Use this with [LOCAL_SETUP.md](LOCAL_SETUP.md) (no Docker Desktop).

### Step 1 — Install a JDK 17+ (you already need 21 for the apps)

### Step 2 — Download Apache Kafka
1. Open https://kafka.apache.org/downloads  
2. Download the latest **binary** (Scala 2.13) ZIP, e.g. `kafka_2.13-3.7.x.tgz`  
3. On Windows, easiest path is **WSL2 (Ubuntu)**:

```powershell
wsl --install
# reboot if prompted, then open Ubuntu
```

Inside WSL:

```bash
cd ~
wget https://downloads.apache.org/kafka/3.7.1/kafka_2.13-3.7.1.tgz
tar -xzf kafka_2.13-3.7.1.tgz
cd kafka_2.13-3.7.1
```

(Adjust version numbers to whatever you downloaded.)

### Step 3 — Start Kafka in KRaft mode (no ZooKeeper)

```bash
# Generate a cluster ID (once)
KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"
bin/kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c config/kraft/server.properties

# Start broker (leave this terminal open)
bin/kafka-server-start.sh config/kraft/server.properties
```

Default listener is usually `localhost:9092`. From **Windows** host apps, `localhost:9092` works if WSL port forwarding is active (Windows 11 usually does this automatically).

### Step 4 — Verify from WSL

```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Empty list is OK until the first payment.

### Step 5 — Point Java apps at the broker

In each PowerShell before `spring-boot:run` (payment + notification at minimum):

```powershell
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

Defaults in `application.yml` are already `localhost:9092`.

### Step 6 — Start payment + notification services (host)

```powershell
cd C:\Users\medip\credit-card-management
mvn -pl cards-payment-service -am spring-boot:run
# other terminal:
mvn -pl cards-notification-service spring-boot:run
```

### Step 7 — Trigger a payment / transfer

UI → Payments → Transfer, or Postman **Transfer Money** / **Bill Pay**.

### Step 8 — Confirm topics

```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Expect `payment.completed` and/or `payment.failed`.

### Step 9 — Peek at messages (optional)

```bash
bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic payment.completed \
  --from-beginning \
  --timeout-ms 5000
```

### Step 10 — Confirm consumer

In the notification-service terminal logs, look for `Received payment.completed`.

### Manual topic create (if auto-create is disabled)

```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic payment.completed --partitions 3 --replication-factor 1
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic payment.failed --partitions 3 --replication-factor 1
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic notification.requested --partitions 3 --replication-factor 1
```

---

## Option A — Docker Compose (optional only)

Only if you use Docker Desktop. Not required for local Maven runs.

```powershell
cd C:\Users\medip\credit-card-management
docker compose up postgres kafka -d
docker compose ps
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

**Caveat:** Compose advertises `kafka:9092` for containers. Host JVM apps should prefer **Option B (native)** so `localhost:9092` always works.

---

## Topics (reference)

| Topic | Publisher | Consumer | When |
|-------|-----------|----------|------|
| `payment.completed` | payment-service | notification-service | Success |
| `payment.failed` | payment-service | notification-service | Failure |
| `notification.requested` | payment-service | notification-service | Explicit notify |

Constants: `com.cards.common.kafka.KafkaTopics`

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Connection refused` :9092 | Broker not started; finish Step 3 |
| Windows app cannot reach WSL Kafka | Use `localhost`; on older Win10 try `wsl hostname -I` and set that IP as bootstrap |
| Topics never appear | Send a payment first; check payment-service logs |
| Consumer idle | Notification service down, or wrong `KAFKA_BOOTSTRAP_SERVERS` |
| Docker + host mix | Prefer full native (Option B) for no-Docker local setup |

---

## Checklist (no Docker)

- [ ] Kafka running (KRaft) on `localhost:9092`  
- [ ] `cards-payment-service` up  
- [ ] `cards-notification-service` up  
- [ ] Login + Transfer or Bill Pay  
- [ ] Topics listed / consumer logs show events  
