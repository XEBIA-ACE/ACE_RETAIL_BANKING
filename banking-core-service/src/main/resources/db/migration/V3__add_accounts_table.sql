-- V3__add_accounts_table.sql
-- Creates the accounts table for the Consolidated Account Dashboard feature (US-001).
-- Naming conventions and SQL dialect are consistent with V1__init_schema.sql and V2__add_payees_table.sql:
--   - snake_case column names
--   - BIGINT primary keys with GENERATED ALWAYS AS IDENTITY
--   - TIMESTAMP (without time zone) for audit columns
--   - Explicit named constraints and indexes for maintainability
--   - CHECK constraints for enum-like columns (MySQL 8.0+)

CREATE TABLE accounts (
    id             BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY,
    external_id    VARCHAR(255)  NOT NULL,
    customer_id    BIGINT        NOT NULL,
    account_type   VARCHAR(20)   NOT NULL,
    account_number VARCHAR(255)  NOT NULL,
    balance        DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    currency       VARCHAR(3)    NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    nickname       VARCHAR(255),
    created_at     TIMESTAMP     NOT NULL,
    updated_at     TIMESTAMP     NOT NULL,

    CONSTRAINT pk_accounts PRIMARY KEY (id),

    -- Enforce globally unique public identifier
    CONSTRAINT uq_accounts_external_id UNIQUE (external_id),

    -- Enforce valid account type values
    CONSTRAINT chk_accounts_type CHECK (account_type IN ('CHECKING', 'SAVINGS', 'CREDIT_CARD')),

    -- Enforce valid status values
    CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')),

    -- Referential integrity: account must belong to a known customer
    CONSTRAINT fk_accounts_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers (id)
);

-- Index to speed up list-by-customer queries (FR-01, FR-03)
CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);

-- Index to speed up lookup-by-external-id queries
CREATE INDEX idx_accounts_external_id ON accounts (external_id);