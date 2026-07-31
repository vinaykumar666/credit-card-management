# Postman — What You Need

## Files
| File | Purpose |
|------|---------|
| `Credit-Card-Platform.postman_collection.json` | All APIs (Auth, BFF, Gateway, health) |
| `Credit-Card-Platform.postman_environment.json` | Local URLs + channel/client defaults |

## Import (Postman Desktop or web)
1. Open Postman → **Import** → select both JSON files above.
2. Top-right environment dropdown → choose **Credit Card Local**.
3. Start the platform (`docker compose up` or local services) — see [docs/LOCAL_SETUP.md](../docs/LOCAL_SETUP.md).

## Run order (clear path)
1. **01 Auth → Login (Ada)** — auto-saves `accessToken` + `userId`.
2. **02 BFF → Dashboard** — proves tenant headers + JWT.
3. **02 BFF → Initiate Payment** — publishes Kafka events.
4. **02 BFF → Notifications** — after a few seconds, alerts appear.
5. Optional: **03 Gateway** folder for direct domain APIs through the gateway.

## Headers (automatic)
Collection pre-request script adds on every call:
- `X-Correlation-Id` (new UUID)
- `X-Channel-Id` = `WEB`
- `X-Client-Id` = `cards-dashboard-ui`
- `Authorization: Bearer {{accessToken}}` (collection auth; Auth folder uses noauth where needed)

## Seed credentials
Documented in [docs/USERS.md](../docs/USERS.md):
- `ada.lovelace@cards.local` / `Password123!`
- `ben.franklin@cards.local` / `Password123!`
- `cara.admin@cards.local` / `Password123!`
