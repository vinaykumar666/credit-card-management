# Banking Features — Transfers, Bill Pay & Beneficiaries

## What was missing before
Only a generic **Initiate payment** existed (no payee, no transfer, no bill context).

## What you have now (banking view)

| Feature | API | UI |
|---------|-----|-----|
| **Beneficiaries (payees)** | CRUD under `/api/v1/beneficiaries` | **Payees** page |
| **Transfer money** | `POST /api/v1/payments/transfer` | Payments → **Transfer** |
| **Make payment (bill pay)** | `POST /api/v1/payments/bill-pay` | Payments → **Bill pay** |
| **Card self-settle** | `POST /api/v1/payments` | Payments → **Card settle** |
| **Payment history** | `GET /api/v1/payments?userId=` | Payments → **History** |

BFF mirrors these under `/bff/v1/**` (UI calls BFF only).

## Beneficiary fields
- Nickname (how you see them in the app)
- Legal / payee name
- Account number
- Bank name
- IFSC or routing code
- Type: `PERSON` | `MERCHANT` | `INTERNAL`
- Status: `ACTIVE` | `INACTIVE` (delete = soft deactivate)

## Seed payees (Ada)
| Nickname | Type | Account |
|----------|------|---------|
| Mom Home | PERSON | 998877665544 |
| Electric Co | MERCHANT | UTIL99887766 |

Ben has **Workshop Rent** (MERCHANT). Full catalog: [USERS.md](USERS.md).

## Typical flows

### 1) Add a payee
UI **Payees** → fill form → Save  
or Postman `POST /bff/v1/beneficiaries`

### 2) Transfer money
1. Login as Ada  
2. Payments → Transfer  
3. Choose card + **Mom Home**  
4. Amount + remarks → Submit  
5. History shows `paymentType=TRANSFER` with beneficiary snapshot  

### 3) Pay a bill
1. Payments → Bill pay  
2. Select **Electric Co** (or enter one-time payee)  
3. Amount + bill reference → Submit  
4. `paymentType=BILL_PAYMENT`  

## Kafka
Transfers and bill payments still go through the same payment strategies and publish `payment.completed` / `payment.failed` → notifications.

## Error codes
| Code | Meaning |
|------|---------|
| PAY_005 | Beneficiary not found |
| PAY_006 | Beneficiary inactive |
| PAY_007 | Duplicate account number for user |
| PAY_008 | Invalid transfer / bill-pay payload |
