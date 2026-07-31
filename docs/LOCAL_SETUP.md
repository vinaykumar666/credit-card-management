# Local Setup Guide (Spoon-Fed)

Follow these steps **in order** on Windows 10/11. Goal: run the full platform on your laptop.

---

## 0. What you will have running

| Piece | URL / Port |
|-------|------------|
| Angular UI | http://localhost:4200 (dev) or http://localhost:8088 (Docker) |
| BFF | http://localhost:8086 |
| API Gateway | http://localhost:8080 |
| Auth (OAuth2 AS) | http://localhost:8081 |
| Account | 8082 |
| Payment | 8083 |
| Notification | 8084 |
| Enterprise | 8085 |
| Postgres | localhost:5432 |
| Kafka | localhost:9092 |

Required headers for BFF/gateway (non-auth):

```http
X-Channel-Id: WEB
X-Client-Id: cards-dashboard-ui
X-Correlation-Id: <any-uuid>
Authorization: Bearer <accessToken>
```

---

## 1. Install prerequisites

### 1.1 Java 21
1. Install **Temurin 21** (or Microsoft OpenJDK 21).
2. Open a **new** PowerShell and run:
   ```powershell
   java -version
   ```
   You must see `21.x`.

### 1.2 Maven 3.9+
```powershell
mvn -version
```
If missing: download Apache Maven, unzip, add `bin` to PATH.

### 1.3 Docker Desktop
1. Install [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/).
2. Start Docker Desktop and wait until it says **Running**.
3. Verify:
   ```powershell
   docker version
   docker compose version
   ```

### 1.4 Node.js 20+ (for Angular)
```powershell
node -v
npm -v
```
Install from https://nodejs.org if missing.

### 1.5 Git
```powershell
git --version
```

---

## 2. Get the code

```powershell
cd C:\Users\medip
git clone <YOUR_REPO_URL> credit-card-management
cd credit-card-management
```

If you already have the folder, just:
```powershell
cd C:\Users\medip\credit-card-management
```

---

## 3. Build the backend (Maven)

```powershell
cd C:\Users\medip\credit-card-management
mvn clean verify
```

**Success** looks like `BUILD SUCCESS` at the end.  
If a test fails, paste the error — do not skip ahead.

---

## 4. Start infrastructure + services (easiest path)

### Option A — Everything in Docker (recommended first run)

```powershell
cd C:\Users\medip\credit-card-management
docker compose up --build
```

First build takes a long time (Maven inside each Dockerfile). Leave this terminal open.

When healthy, open:
- UI: http://localhost:8088
- Gateway health: http://localhost:8080/actuator/health
- Auth health: http://localhost:8081/actuator/health
- BFF health: http://localhost:8086/actuator/health

### Option B — Infra in Docker, apps on host (faster iteration)

Terminal 1:
```powershell
docker compose up postgres kafka
```

Wait until Postgres is healthy, then create DBs are auto-created via `infra/postgres/init-databases.sql`.

Then run services (separate terminals or IDE):

```powershell
# Auth first (issuer for JWTs)
mvn -pl cards-authentication-service -am spring-boot:run

# Then others
mvn -pl cards-account-details-service spring-boot:run
mvn -pl cards-payment-service spring-boot:run
mvn -pl cards-notification-service spring-boot:run
mvn -pl cards-enterprise-api-service spring-boot:run
mvn -pl cards-api-gateway-service spring-boot:run
mvn -pl cards-bff-dashboard-service spring-boot:run
```

---

## 5. Start the Angular UI (dev mode)

```powershell
cd C:\Users\medip\credit-card-management\cards-dashboard-ui
npm install
npm start
```

Open http://localhost:4200

---

## 6. First login (API walkthrough)

### 6.1 Register a user

```powershell
curl -X POST http://localhost:8081/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -H "X-Correlation-Id: local-demo-1" `
  -d "{\"email\":\"demo@cards.local\",\"password\":\"Password123!\",\"fullName\":\"Demo User\"}"
```

Copy `accessToken` from the JSON response.

### 6.2 Call BFF dashboard

```powershell
$token = "<PASTE_ACCESS_TOKEN>"
curl http://localhost:8086/bff/v1/dashboard `
  -H "Authorization: Bearer $token" `
  -H "X-Channel-Id: WEB" `
  -H "X-Client-Id: cards-dashboard-ui" `
  -H "X-Correlation-Id: local-demo-2"
```

### 6.3 Create an account (via gateway)

```powershell
curl -X POST http://localhost:8080/api/v1/accounts `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -H "X-Channel-Id: WEB" `
  -H "X-Client-Id: cards-dashboard-ui" `
  -H "X-Correlation-Id: local-demo-3" `
  -d "{\"userId\":\"<USER_ID_FROM_LOGIN>\",\"accountNumber\":\"4111111111111111\",\"cardLastFour\":\"1111\",\"cardBrand\":\"VISA\",\"creditLimit\":5000,\"currency\":\"USD\",\"holderName\":\"Demo User\",\"email\":\"demo@cards.local\"}"
```

Adjust the JSON field names to match `CreateAccountRequest` in the account service if your IDE shows different property names.

### 6.4 OAuth2 discovery

```powershell
curl http://localhost:8081/.well-known/openid-configuration
curl http://localhost:8081/oauth2/jwks
```

Client credentials (BFF/service):

```powershell
curl -u cards-bff-service:bff-secret -X POST http://localhost:8081/oauth2/token `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=client_credentials&scope=cards.read cards.write"
```

---

## 7. Error codes

All business errors return JSON like:

```json
{
  "timestamp": "...",
  "status": 401,
  "errorCode": "AUTH_001",
  "message": "Invalid credentials",
  "path": "/api/v1/auth/login",
  "correlationId": "...",
  "channelId": "WEB",
  "clientId": "cards-dashboard-ui"
}
```

Definitions live in [`cards-common/src/main/resources/error-codes.yml`](../cards-common/src/main/resources/error-codes.yml).  
**Never hardcode messages in Java** — change the YAML.

---

## 8. Common failures

| Symptom | Fix |
|---------|-----|
| `Connection refused` Postgres | Start Docker / `docker compose up postgres` |
| JWT / issuer errors on BFF | Auth must be up first; `OAUTH2_ISSUER=http://localhost:8081` |
| BFF `BFF_001` / `BFF_002` | Send `X-Channel-Id` and `X-Client-Id` |
| Gateway `GW_003` | Same tenant headers required |
| Angular CORS errors | Auth/BFF already allow localhost:4200; restart those services after pull |
| Port already in use | `netstat -ano \| findstr :8080` then kill the PID |
| Docker build OOM | In Docker Desktop → Settings → Resources, raise RAM to 8GB+ |

---

## 9. Stop everything

```powershell
docker compose down
# optional wipe DB:
docker compose down -v
```

Stop `npm start` with Ctrl+C.

---

## 10. Postman

1. Import `postman/Credit-Card-Platform.postman_collection.json`
2. Import `postman/Credit-Card-Platform.postman_environment.json`
3. Select environment **Credit Card Local**
4. Run **01 Auth → Login (Ada)** then **02 BFF → Dashboard**

Details: [../postman/README.md](../postman/README.md)

## 11. Kafka

Step-by-step: [KAFKA_SETUP.md](KAFKA_SETUP.md)

## 12. Next docs

- Java 21 rationale: [JAVA21.md](JAVA21.md)
- CI/CD + EC2 secrets: [CI_CD.md](CI_CD.md)
- Architecture: [ARCHITECTURE.md](ARCHITECTURE.md)
- Seed users: [USERS.md](USERS.md)
