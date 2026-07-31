CREATE TABLE payments (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL,
  user_id UUID NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  payment_method VARCHAR(30) NOT NULL,
  status VARCHAR(20) NOT NULL,
  external_ref VARCHAR(100),
  failure_reason VARCHAR(500),
  correlation_id VARCHAR(64),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE ledger_entries (
  id UUID PRIMARY KEY,
  payment_id UUID NOT NULL REFERENCES payments(id),
  account_id UUID NOT NULL,
  entry_type VARCHAR(20) NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_account_id ON payments(account_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_ledger_entries_payment_id ON ledger_entries(payment_id);
