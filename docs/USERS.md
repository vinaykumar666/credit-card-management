# Seed Users Catalog

These users are created automatically on auth-service startup by `AuthDataInitializer`.  
Accounts and sample transactions are created by `AccountDataInitializer` using the same stable UUIDs.

**Default password for every seed user:** `Password123!`

| Full name | Email | User ID (UUID) | Roles | Demo account |
|-----------|-------|----------------|-------|--------------|
| Ada Lovelace | `ada.lovelace@cards.local` | `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` | ROLE_USER | VISA •••• 1111, limit $12,000 |
| Ben Franklin | `ben.franklin@cards.local` | `bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb` | ROLE_USER | MASTERCARD •••• 0004, limit $8,000 |
| Cara Admin | `cara.admin@cards.local` | `cccccccc-cccc-cccc-cccc-cccccccccccc` | ROLE_USER + ROLE_ADMIN | (no card seed — use for admin login demos) |

## Stable account IDs

| Owner | Account ID | Account number | Brand |
|-------|------------|----------------|-------|
| Ada | `a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1` | 4111111111111111 | VISA |
| Ben | `b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2` | 5500000000000004 | MASTERCARD |

## Seed beneficiaries (payees)

| Owner | Beneficiary ID | Nickname | Type | Account |
|-------|----------------|----------|------|---------|
| Ada | `d1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1` | Mom Home | PERSON | 998877665544 |
| Ada | `d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2` | Electric Co | MERCHANT | UTIL99887766 |
| Ben | `d3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3` | Workshop Rent | MERCHANT | 112233445566 |

Banking how-to: [BANKING_FEATURES.md](BANKING_FEATURES.md)

## How to sign in (UI)

1. Open http://localhost:4200 (dev) or http://localhost:8088 (Docker).
2. On the login screen, click **Ada**, **Ben**, or **Cara** quick-fill — or type the email yourself.
3. Password is always `Password123!` for seed users.
4. After login, BFF calls require headers (UI interceptor adds them):
   - `X-Channel-Id: WEB`
   - `X-Client-Id: cards-dashboard-ui`

## Maintaining this file

When you add a new seed user:

1. Add a row to the table above.
2. Add `ensureUser(...)` in `AuthDataInitializer`.
3. If they need a card, add `seedAccount(...)` in `AccountDataInitializer` with a new fixed UUID.
4. Keep UUIDs stable forever so demos and integration tests do not break.

## Security note

Seed passwords are for **local / demo** only. Production must disable `*DataInitializer` beans (profile `prod`) or gate them with `app.seed.enabled=false`.
