-- Flyway Migration V2: Add payees table
-- Consistent with V1__init_schema.sql naming conventions and SQL dialect

CREATE TABLE payees (
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    external_id    VARCHAR(255)    NOT NULL,
    customer_id    BIGINT          NOT NULL,
    payee_name     VARCHAR(255)    NOT NULL,
    account_number VARCHAR(255)    NOT NULL,
    bank_code      VARCHAR(255)    NOT NULL,
    bank_name      VARCHAR(255)    NULL,
    nickname       VARCHAR(255)    NULL,
    currency       VARCHAR(3)      NOT NULL,
    created_at     TIMESTAMP       NOT NULL,
    updated_at     TIMESTAMP       NOT NULL,

    CONSTRAINT pk_payees PRIMARY KEY (id),
    CONSTRAINT uq_payees_external_id UNIQUE (external_id),
    CONSTRAINT uq_payees_customer_account UNIQUE (customer_id, account_number),
    CONSTRAINT fk_payees_customer_id FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE INDEX idx_payees_customer_id ON payees (customer_id);
CREATE INDEX idx_payees_external_id ON payees (external_id);