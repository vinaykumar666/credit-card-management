# Kafka Consumer Production Checklist

Implemented in **`cards-notification-service`**.  
Each item: **why we use it** and **what happens if we don't**.

Related: [KAFKA_SETUP.md](KAFKA_SETUP.md) · [APP_EVENTS.md](APP_EVENTS.md)

---

## Checklist

| # | Setting / practice | Why we use it | If we don't |
|---|-------------------|---------------|-------------|
| 1 | **Unique `group-id`** (`cards-notification-service`) | Identifies this consumer group; offsets are tracked per group so each service scales independently. | Another app sharing the group steals partitions; messages are split unpredictably and some handlers never see events. |
| 2 | **Multi-broker `bootstrap-servers`** (env: `host1:9092,host2:9092,...`) | Client discovers the full cluster from any seed broker; survives one broker being down at startup. | Single broker down at boot → consumer cannot start; no HA discovery. Local default remains `localhost:9092`. |
| 3 | **`auto-offset-reset: latest`** | New consumer groups start at the **end** of the log (live traffic only). | `earliest` replays **all** historical messages on first start → duplicate notifications, thundering herd, long catch-up. |
| 4 | **`enable-auto-commit: false`** | Offsets commit only when we decide the message was handled safely. | Auto-commit can mark a message done **before** business logic finishes → crash loses the event permanently. |
| 5 | **`ack-mode: manual_immediate`** + `Acknowledgment` | Explicit `ack.acknowledge()` after success (and after idempotent skip). | Without manual ack, failed work can still be committed, or successful work retried forever depending on mode. |
| 6 | **`concurrency` (default 3)** | Parallel consumers within the group for higher throughput (one thread ≈ one partition assignment slice). | Single-threaded listener becomes a bottleneck under payment volume. |
| 7 | **`max-poll-records`** | Caps batch size per poll so processing stays within `max.poll.interval.ms`. | Huge polls → long processing → consumer kicked from group → rebalances and duplicate deliveries. |
| 8 | **`max.poll.interval.ms` / `session.timeout.ms` / `heartbeat.interval.ms`** | Keep the member alive while working; heartbeat proves liveness to the broker. | Mis-tuned timeouts → frequent rebalances, stuck partitions, or silent stalls. |
| 9 | **`JsonDeserializer` + `trusted.packages` + `use.type.headers: false`** | Safe typed deserialization limited to `com.cards.common.event`; type comes from listener `default.type`, not attacker-controlled headers. | Broad trust / type headers enable gadget attacks or wrong class binding. |
| 10 | **`DefaultErrorHandler` + retries** | Transient failures (DB blip, network) get a few retries with backoff. | First failure drops the message or blocks the partition forever. |
| 11 | **Dead Letter Topic (`topic.DLT`)** | Poison pills move to `payment.completed.DLT` (etc.) so the main topic keeps flowing. | Bad JSON / permanent errors block a partition; lag grows; payments stop notifying. |
| 12 | **Idempotency table `processed_kafka_event`** | At-least-once delivery is expected; claim-by-key prevents double emails/SMS. | Retries / rebalances send duplicate notifications to customers. |
| 13 | **SSL / SASL placeholders** (`KAFKA_SECURITY_PROTOCOL`, etc.) | Ready for encrypted, authenticated brokers in staging/prod. | PLAINTEXT on a shared network exposes payloads and allows broker spoofing. |
| 14 | **Lag / ops monitoring** (ops practice) | Watch consumer lag, DLT depth, rebalance rate (Prometheus/Grafana or cloud metrics). | Silent backlog; customers learn about payments late with no alarm. |

---

## DLT topic names

| Main topic | Dead-letter topic |
|------------|-------------------|
| `payment.completed` | `payment.completed.DLT` |
| `payment.failed` | `payment.failed.DLT` |
| `notification.requested` | `notification.requested.DLT` |

Create DLT topics in prod (or enable auto-create carefully). Inspect with:

```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic payment.completed.DLT --from-beginning
```

---

## Lifecycle logs on consume

Each listener emits:

```text
event="START", method="onPaymentCompleted()", userId="...", amount="...", transactionId="..."
event="END", method="onPaymentCompleted()", userId="...", amount="...", transactionId="...", transactionTime="12ms"
```

---

## Tuning knobs (env)

| Env | Default | Meaning |
|-----|---------|---------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Broker list |
| `KAFKA_LISTENER_CONCURRENCY` | `3` | Parallel listener threads |
| `KAFKA_MAX_POLL_RECORDS` | `50` | Max records per poll |
| `KAFKA_SECURITY_PROTOCOL` | `PLAINTEXT` | Use `SSL` / `SASL_SSL` in prod |
