CREATE TABLE notification_log (
  id UUID PRIMARY KEY,
  user_id UUID,
  channel VARCHAR(20) NOT NULL,
  template VARCHAR(100) NOT NULL,
  recipient VARCHAR(255) NOT NULL,
  payload TEXT,
  status VARCHAR(20) NOT NULL,
  correlation_id VARCHAR(64),
  error_message VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  sent_at TIMESTAMPTZ
);
