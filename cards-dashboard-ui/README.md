# Ledgerly — Cards Dashboard UI

Angular 19 standalone SPA for the credit card platform. Auth talks to the authentication service; all dashboard data goes through the BFF.

| Concern | URL |
|---------|-----|
| UI (local) | `http://localhost:4200` |
| UI (Docker) | `http://localhost:8088` |
| BFF | `http://localhost:8086` |
| Auth | `http://localhost:8081` |

## Prerequisites

- Node.js 20+ and npm
- Running platform services (auth on `8081`, BFF on `8086`)

## Install & run

```bash
cd cards-dashboard-ui
npm install
npm start
```

Open [http://localhost:4200](http://localhost:4200).

Optional local proxy (CORS-friendly):

```bash
npx ng serve --proxy-config proxy.conf.json
```

When using the proxy, point `environment.development.ts` `bffUrl` / `authUrl` at `''` (same origin) if you prefer relative paths.

## Build

```bash
npm run build
```

Production output: `dist/cards-dashboard-ui/browser`.

Watch mode:

```bash
npm run watch
```

## Screens

- **Login / Register** — `POST {authUrl}/api/v1/auth/login|register`, stores `accessToken` in `sessionStorage`
- **Dashboard** — `GET {bffUrl}/bff/v1/dashboard`
- **Accounts** — `GET /bff/v1/accounts` + `GET /bff/v1/accounts/{id}/transactions`
- **Payments** — initiate form → `POST /bff/v1/payments`
- **Notifications** — `GET /bff/v1/notifications`

## HTTP headers (interceptor)

Every request includes:

- `Authorization: Bearer <accessToken>` (when present)
- `X-Channel-Id: WEB`
- `X-Client-Id: cards-dashboard-ui`
- `X-Correlation-Id: <uuid>`

## Docker

```bash
# from repo root
docker compose up --build cards-dashboard-ui
```

Image serves the SPA on port **80** inside the container, mapped to host **8088**. `nginx.conf` includes `try_files` for Angular routing and an optional `/bff/` reverse proxy to `cards-bff-dashboard-service:8086`.

## Environment

`src/environments/environment.ts` / `environment.development.ts`:

```ts
bffUrl: 'http://localhost:8086'
authUrl: 'http://localhost:8081'
```
