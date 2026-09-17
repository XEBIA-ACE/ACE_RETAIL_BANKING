CREATE TABLE customers (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    external_id VARCHAR(36)  NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_external_id UNIQUE (external_id),
    CONSTRAINT uq_customers_email UNIQUE (email)
);
CREATE TABLE accounts (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    external_id    VARCHAR(36)    NOT NULL,
    customer_id    BIGINT         NOT NULL,
    account_number VARCHAR(30)    NOT NULL,
    account_type   VARCHAR(30)    NOT NULL,
    balance        DECIMAL(19,2)  NOT NULL DEFAULT 0.00,
    currency       VARCHAR(3)    NOT NULL DEFAULT 'USD',
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_external_id UNIQUE (external_id),
    CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_customer_id FOREIGN KEY (customer_id) REFERENCES customers (id)
);
CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);
