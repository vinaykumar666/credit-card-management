-- Banking: saved payees (beneficiaries)
CREATE TABLE beneficiaries (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    nickname            VARCHAR(100) NOT NULL,
    beneficiary_name    VARCHAR(255) NOT NULL,
    account_number      VARCHAR(34) NOT NULL,
    bank_name           VARCHAR(255) NOT NULL,
    ifsc_or_routing     VARCHAR(32) NOT NULL,
    beneficiary_type    VARCHAR(30) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_beneficiaries_user_id ON beneficiaries(user_id);
CREATE UNIQUE INDEX uq_beneficiaries_user_account
    ON beneficiaries(user_id, account_number);

-- Enrich payments for transfer / bill-pay context
ALTER TABLE payments ADD COLUMN payment_type VARCHAR(30) NOT NULL DEFAULT 'CARD_PAYMENT';
ALTER TABLE payments ADD COLUMN beneficiary_id UUID;
ALTER TABLE payments ADD COLUMN beneficiary_name VARCHAR(255);
ALTER TABLE payments ADD COLUMN beneficiary_account VARCHAR(34);
ALTER TABLE payments ADD COLUMN bank_name VARCHAR(255);
ALTER TABLE payments ADD COLUMN ifsc_or_routing VARCHAR(32);
ALTER TABLE payments ADD COLUMN remarks VARCHAR(500);
ALTER TABLE payments ADD COLUMN reference_number VARCHAR(64);

CREATE INDEX idx_payments_beneficiary_id ON payments(beneficiary_id);
