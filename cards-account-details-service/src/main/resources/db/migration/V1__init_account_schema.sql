CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  account_number VARCHAR(34) NOT NULL UNIQUE,
  card_last_four VARCHAR(4) NOT NULL,
  card_brand VARCHAR(20) NOT NULL,
  credit_limit NUMERIC(19,2) NOT NULL,
  available_credit NUMERIC(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'USD',
  status VARCHAR(20) NOT NULL,
  holder_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(32),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES accounts(id),
  type VARCHAR(30) NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  merchant VARCHAR(255),
  description VARCHAR(500),
  status VARCHAR(20) NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
