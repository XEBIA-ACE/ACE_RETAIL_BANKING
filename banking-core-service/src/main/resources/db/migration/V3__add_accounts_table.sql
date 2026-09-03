-- V3__add_accounts_table.sql
-- Creates the accounts table for the Consolidated Account Dashboard feature (US-001).
-- Naming conventions and SQL dialect are consistent with V1__init_schema.sql and V2__add_payees_table.sql:
--   - snake_case column names
--   - BIGINT primary keys with auto-increment (GENERATED ALWAYS AS IDENTITY)
--   - TIMESTAMP (without time zone) for audit columns
--   - Explicit named constraints and indexes for maintainability
--
-- Design notes:
--   - account_type stores the enum constant name (e.g. 'CHECKING', 'SAVINGS', 'CREDIT_CARD')
--     so that new account types require only a new enum constant — zero schema change (AC-7).
--   - account_number stores the full account number; masking is applied exclusively at the
--     application layer (AccountMapper) before any API response is transmitted.
--   - external_id is a UUID (VARCHAR(36)) safe to expose in API responses; internal id is
--     never surfaced externally.
--   - current_balance defaults to 0.0000 to allow account creation before any deposit.
--   - currency is the ISO 4217 three-letter code (e.g. 'USD', 'EUR', 'GBP').

CREATE TABLE accounts (
    id              BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY,
    external_id     VARCHAR(36)    NOT NULL,
    customer_id     BIGINT         NOT NULL,
    account_type    VARCHAR(50)    NOT NULL,
    account_number  VARCHAR(255)   NOT NULL,
    current_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    currency        VARCHAR(3)     NOT NULL,
    created_at      TIMESTAMP      NOT NULL,
    updated_at      TIMESTAMP      NOT NULL,

    CONSTRAINT pk_accounts PRIMARY KEY (id),

    -- Enforce globally unique public identifier (AC-3: external_id exposed in API responses)
    CONSTRAINT uq_accounts_external_id UNIQUE (external_id),

    -- Referential integrity: account must belong to a known customer
    CONSTRAINT fk_accounts_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers (id)
);

-- Index to speed up list-by-customer queries (AC-1, AC-5: single query per customer, no N+1)
CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);

-- Index to speed up lookup-by-external-id queries (AC-1: accountId field in dashboard response)
CREATE INDEX idx_accounts_external_id ON accounts (external_id);