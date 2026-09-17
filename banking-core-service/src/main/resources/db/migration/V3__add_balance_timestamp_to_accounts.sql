ALTER TABLE accounts ADD COLUMN last_balance_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE INDEX idx_accounts_external_id_balance ON accounts (external_id, balance, last_balance_updated_at);
