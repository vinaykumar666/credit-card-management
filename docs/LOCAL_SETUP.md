# Local Setup Guide (Spoon-Fed)

**Preferred for daily work: no Docker.**  
Run PostgreSQL + Kafka on the machine, then start every Spring Boot service and Angular with Maven/npm.

Docker Compose remains an optional “all-in-one” shortcut at the end of this doc.

---

## 0. What you will have running

| Piece | URL / Port |
|-------|------------|
| Angular UI | http://localhost:4200 |
| BFF | http://localhost:8086 |
| API Gateway | http://localhost:8080 |
| Auth (OAuth2 AS) | http://localhost:8081 |
| Account | 8082 |
| Payment | 8083 |
| Notification | 8084 |
| Enterprise | 8085 |
| Postgres | localhost:5432 |
| Kafka | localhost:9092 |

Defaults in `application.yml` already point at `localhost` — no Docker hostnames required.

Required headers for BFF/gateway (non-auth):

```http
X-Channel-Id: WEB
X-Client-Id: cards-dashboard-ui
X-Correlation-Id: <any-uuid>
Authorization: Bearer <accessToken>
```

Seed login (after auth starts): see [USERS.md](USERS.md)  
`ada.lovelace@cards.local` / `Password123!`

---

## 1. Install prerequisites (no Docker)

### 1.1 Java 21
```powershell
java -version
```
Must show `21.x` (Temurin / Microsoft OpenJDK).

### 1.2 Maven 3.9+
```powershell
mvn -version
```

### 1.3 Node.js 20+
```powershell
node -v
npm -v
```

### 1.4 PostgreSQL 16 (native Windows)
1. Install from https://www.postgresql.org/download/windows/
2. During setup, remember the password for user `postgres` (or create user `cards` / password `cards`).
3. Ensure service is running and port **5432** is free.
4. Verify:
   ```powershell
   psql -U postgres -c "SELECT version();"
   ```

### 1.5 Kafka (native — no Docker)

Follow **[KAFKA_SETUP.md → Option B: Native / no Docker](KAFKA_SETUP.md#option-b--native--no-docker-windows)**.

You need a broker listening on **`localhost:9092`**.

### 1.6 Git
```powershell
git --version
```

---

## 2. Get the code

```powershell
cd C:\Users\medip
git clone https://github.com/vinaykumar666/credit-card-management.git
cd credit-card-management
```

---

## 3. Create Postgres databases (one time)

Using `psql` (or pgAdmin → Query Tool):

```powershell
psql -U postgres -f infra\postgres\init-databases.sql
psql -U postgres -f infra\postgres\init-local-role.sql
```

If your superuser is not `postgres`, adjust `-U`.  
If a database already exists, `CREATE DATABASE` errors are harmless — ignore or drop/recreate.

Services default to:

| Service | JDBC URL | User / pass |
|---------|----------|-------------|
| Auth | `jdbc:postgresql://localhost:5432/auth_db` | `cards` / `cards` |
| Account | `jdbc:postgresql://localhost:5432/account_db` | `cards` / `cards` |
| Payment | `jdbc:postgresql://localhost:5432/payment_db` | `cards` / `cards` |
| Notification | `jdbc:postgresql://localhost:5432/notification_db` | `cards` / `cards` |

Override anytime:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="yourpassword"
$env:DB_URL="jdbc:postgresql://localhost:5432/auth_db"   # per service
```

---

## 4. Build the backend

```powershell
cd C:\Users\medip\credit-card-management
mvn clean verify
```

Wait for `BUILD SUCCESS`.

---

## 5. Start Kafka (must be up before payment / notification)

See [KAFKA_SETUP.md](KAFKA_SETUP.md). Quick check:

```powershell
# after Kafka is running
# list topics (path depends on your Kafka install)
kafka-topics.bat --bootstrap-server localhost:9092 --list
```

---

## 6. Start all Java services (no Docker)

### Option A — helper script (opens 7 PowerShell windows)

```powershell
cd C:\Users\medip\credit-card-management
.\scripts\run-local-no-docker.ps1
```

### Option B — manual (one terminal per service)

Set issuer for all resource servers (same machine):

```powershell
$env:OAUTH2_ISSUER="http://localhost:8081"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

**Start Auth first** (issues JWTs / JWKS):

```powershell
cd C:\Users\medip\credit-card-management
mvn -pl cards-authentication-service -am spring-boot:run
```

Then in other terminals:

```powershell
mvn -pl cards-account-details-service spring-boot:run
mvn -pl cards-enterprise-api-service spring-boot:run
mvn -pl cards-payment-service spring-boot:run
mvn -pl cards-notification-service spring-boot:run
mvn -pl cards-api-gateway-service spring-boot:run
mvn -pl cards-bff-dashboard-service spring-boot:run
```

Health checks:

```powershell
curl http://localhost:8081/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8086/actuator/health
```

---

## 7. Start Angular (no Docker)

```powershell
cd C:\Users\medip\credit-card-management\cards-dashboard-ui
npm install
npm start
```

Open **http://localhost:4200**  
Quick-fill **Ada** → password `Password123!`

Env files already use:
- Auth: `http://localhost:8081`
- BFF: `http://localhost:8086`

---

## 8. Postman (no Docker)

1. Import `postman/Credit-Card-Platform.postman_collection.json`
2. Import `postman/Credit-Card-Platform.postman_environment.json` (**Credit Card Local** = localhost URLs)
3. Run **01 Auth → Login (Ada)** → **02 BFF → Dashboard**
4. Banking: List Beneficiaries → Transfer Money → Bill Pay  

Details: [../postman/README.md](../postman/README.md)

---

## 9. First API smoke test

```powershell
# Login Ada
curl -X POST http://localhost:8081/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"ada.lovelace@cards.local\",\"password\":\"Password123!\"}"
```

Copy `accessToken`, then:

```powershell
$token = "<PASTE>"
curl http://localhost:8086/bff/v1/dashboard `
  -H "Authorization: Bearer $token" `
  -H "X-Channel-Id: WEB" `
  -H "X-Client-Id: cards-dashboard-ui"
```

Transfer / bill pay: [BANKING_FEATURES.md](BANKING_FEATURES.md)

---

## 10. Environment variables cheat sheet (host run)

| Variable | Typical local value |
|----------|---------------------|
| `OAUTH2_ISSUER` | `http://localhost:8081` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/<db>` |
| `DB_USERNAME` / `DB_PASSWORD` | `cards` / `cards` (or your Postgres user) |
| `GATEWAY_BASE_URL` (BFF) | `http://localhost:8080` |
| `ENTERPRISE_API_URL` (payment) | `http://localhost:8085` |
| `AUTH_SERVICE_URL` (gateway) | `http://localhost:8081` |
| …other `*_SERVICE_URL` | `http://localhost:8082` … `8086` |

Gateway and BFF defaults already use `localhost` — usually no overrides needed.

---

## 11. Common failures (no Docker)

| Symptom | Fix |
|---------|-----|
| `Connection refused` :5432 | Start PostgreSQL Windows service; check password / DBs created |
| `Connection refused` :9092 | Start Kafka; see [KAFKA_SETUP.md](KAFKA_SETUP.md) |
| JWT / issuer errors | Auth must start first; `OAUTH2_ISSUER=http://localhost:8081` |
| Flyway errors | Drop/recreate empty DB, restart that service |
| Port in use | `netstat -ano \| findstr :8081` → stop the PID |
| BFF `BFF_001` / `BFF_002` | Send channel/client headers (UI/Postman do this) |
| Payment works but no notification | Kafka down or notification service not running |

---

## 12. Stop everything

1. Ctrl+C in each `spring-boot:run` / `npm start` window (or close windows from the script).  
2. Stop Kafka (Ctrl+C in Kafka terminal).  
3. Leave PostgreSQL running as a Windows service, or stop it from Services.msc.

---

## 13. Optional — Docker (only if you want it)

Not required. If you prefer one command later:

```powershell
docker compose up --build
```

UI then at http://localhost:8088. Mixed mode (Docker only for Postgres/Kafka) is in older notes; prefer **full native** above for interviews/demos without Docker Desktop.

---

## 14. Next docs

- Kafka native steps: [KAFKA_SETUP.md](KAFKA_SETUP.md)
- Banking transfer/bill pay: [BANKING_FEATURES.md](BANKING_FEATURES.md)
- Seed users: [USERS.md](USERS.md)
- Architecture: [ARCHITECTURE.md](ARCHITECTURE.md)
