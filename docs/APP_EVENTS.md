# App Event Footfall & Lifecycle Logging

Tracks **every meaningful footfall** into and through the platform: HTTP entry, method START/END, and Kafka consume.

---

## Why we persist `app_event`

| Reason | If we don't |
|--------|-------------|
| Reconstruct a user's journey across services (`correlation_id`) | Support can only guess from scattered log files |
| Measure latency per method / HTTP call (`duration_ms`) | Slow payments stay invisible until customers complain |
| Audit who hit which API (`user_id`, `path`, `channel_id`) | Weak fraud / abuse investigation |
| Prove Kafka handlers ran (`KAFKA_CONSUMED`) | Hard to tell “not published” vs “not consumed” |

Failures writing `app_event` are **logged and swallowed** so audit never breaks payments.

Toggle: `cards.app-events.enabled` (default `true`).  
HTTP filter: `cards.app-events.footfall-filter` (default `true`).

---

## Table: `app_event`

Applied via Flyway in `auth_db`, `account_db`, `payment_db`, `notification_db`  
(also `infra/postgres/app-event-table.sql`).

| Column | Type | Purpose |
|--------|------|---------|
| `id` | UUID | Primary key |
| `event_name` | VARCHAR(100) | Canonical name (see event catalog) |
| `event_phase` | VARCHAR(20) | `FOOTFALL`, `START`, `END`, `ERROR`, `CONSUMED` |
| `service_name` | VARCHAR(100) | e.g. `cards-payment-service` |
| `method_name` | VARCHAR(150) | e.g. `makePayment()` |
| `user_id` | UUID | Subject user when known |
| `user_name` | VARCHAR(255) | Display / email when known |
| `amount` | NUMERIC(19,4) | Payment amount when relevant |
| `transaction_id` | VARCHAR(64) | Payment / notification id |
| `correlation_id` | VARCHAR(64) | Cross-service request id |
| `channel_id` | VARCHAR(64) | `WEB` / `MOBILE` / … |
| `client_id` | VARCHAR(64) | Calling client app |
| `http_method` | VARCHAR(16) | `GET` / `POST` / … |
| `path` | VARCHAR(512) | Request URI or Kafka topic |
| `status` | VARCHAR(40) | HTTP status or `OK` / error type |
| `duration_ms` | BIGINT | Elapsed time |
| `details` | VARCHAR(2000) | Extra context |
| `created_at` | TIMESTAMPTZ | Insert time |

### Useful queries

```sql
-- Last footfalls in payment service
SELECT created_at, event_name, event_phase, method_name, user_id, amount, transaction_id, path, status, duration_ms
FROM app_event
ORDER BY created_at DESC
LIMIT 50;

-- One user journey by correlation id
SELECT created_at, service_name, event_name, event_phase, method_name, path, status, duration_ms
FROM app_event
WHERE correlation_id = '<id>'
ORDER BY created_at;
```

---

## Event catalog (footfall names)

| `event_name` | When it is written | Phase |
|--------------|-------------------|-------|
| `HTTP_FOOTFALL` | Every inbound HTTP request (filter), except actuator/swagger | `FOOTFALL` |
| `METHOD_START` | Enter `@MethodLifecycle` method | `START` |
| `METHOD_END` | Method returns successfully | `END` |
| `METHOD_ERROR` | Method throws | `ERROR` |
| `KAFKA_CONSUMED` | Notification listener processed a message | `CONSUMED` |
| `KAFKA_DLT` | Reserved for DLT recoverer audits | `DLT` |
| `LOGIN` / `PAYMENT` / `TRANSFER` / `BILL_PAY` | Reserved business names (extend as needed) | varies |

---

## Console START / END logs

Logger: `com.cards.lifecycle`  
Default logback uses a **plain console** pattern so lines are visible in service windows.
Use Spring profile `json-logs` for Logstash JSON in prod.

### Expected format

```text
event="START", method="makePayment()", userId="...", userName="...", amount="...", transactionId="..."
event="END", method="makePayment()", userId="...", userName="...", amount="...", transactionTime="42ms"
```

### Where wired today

| Service | Methods |
|---------|---------|
| `cards-payment-service` | `makePayment`, `transfer`, `initiate` |
| `cards-authentication-service` | `login`, `register` |
| `cards-notification-service` | Kafka listeners (`onPaymentCompleted`, …) |

Controllers call `LifecycleLog.bind(...)` **before** the service method so START includes `userId` / `amount`.  
Payment sets `transactionId` after the row is saved.

### Annotate more methods

```java
@MethodLifecycle("listAccounts")
public List<Account> listAccounts(UUID userId) { ... }
```

Bind context in the controller first:

```java
LifecycleLog.bind(userId, userName, amount, transactionId);
try {
  return service.listAccounts(userId);
} finally {
  LifecycleLog.clearBusinessContext();
}
```

---

## Kafka idempotency companion table

`notification_db.processed_kafka_event` (`event_key`, `topic`, `processed_at`)  
Prevents duplicate notification side effects when Kafka redelivers.
