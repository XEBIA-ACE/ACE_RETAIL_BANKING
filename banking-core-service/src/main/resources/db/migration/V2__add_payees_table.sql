-- V2__add_payees_table.sql
-- Creates the payees table for the Manage Payees feature (US-001).
-- Naming conventions and SQL dialect are consistent with V1__init_schema.sql:
--   - snake_case column names
--   - BIGINT primary keys with auto-increment (BIGSERIAL / GENERATED ALWAYS AS IDENTITY)
--   - TIMESTAMP (without time zone) for audit columns
--   - Explicit named constraints and indexes for maintainability

CREATE TABLE payees (
    id             BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    external_id    VARCHAR(255) NOT NULL,
    customer_id    BIGINT       NOT NULL,
    payee_name     VARCHAR(255) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    bank_code      VARCHAR(255) NOT NULL,
    bank_name      VARCHAR(255),
    nickname       VARCHAR(255),
    currency       VARCHAR(3)   NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,

    CONSTRAINT pk_payees PRIMARY KEY (id),

    -- Enforce globally unique public identifier
    CONSTRAINT uq_payees_external_id UNIQUE (external_id),

    -- Prevent a customer from registering the same account number twice (AC-5)
    CONSTRAINT uq_payees_customer_account UNIQUE (customer_id, account_number),

    -- Referential integrity: payee must belong to a known customer
    CONSTRAINT fk_payees_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers (id)
);

-- Index to speed up list-by-customer queries (AC-4, AC-7)
CREATE INDEX idx_payees_customer_id ON payees (customer_id);

-- Index to speed up lookup-by-external-id queries (AC-2, AC-3, AC-6)
CREATE INDEX idx_payees_external_id ON payees (external_id);
