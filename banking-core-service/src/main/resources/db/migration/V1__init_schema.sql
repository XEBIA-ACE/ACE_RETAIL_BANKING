-- V1__init_schema.sql
-- Initial schema for Banking Core Service

CREATE TABLE IF NOT EXISTS customers (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id   VARCHAR(36)  NOT NULL UNIQUE,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone         VARCHAR(30),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id    VARCHAR(36)    NOT NULL UNIQUE,
    customer_id    BIGINT         NOT NULL,
    account_number VARCHAR(30)    NOT NULL UNIQUE,
    account_type   VARCHAR(30)    NOT NULL,
    balance        DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    currency       VARCHAR(3)     NOT NULL DEFAULT 'USD',
    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id      VARCHAR(36)    NOT NULL UNIQUE,
    account_id       BIGINT         NOT NULL,
    transaction_type VARCHAR(30)    NOT NULL,
    amount           DECIMAL(19, 4) NOT NULL,
    currency         VARCHAR(3)     NOT NULL DEFAULT 'USD',
    description      VARCHAR(500),
    reference        VARCHAR(100),
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE TABLE IF NOT EXISTS loans (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id     VARCHAR(36)    NOT NULL UNIQUE,
    customer_id     BIGINT         NOT NULL,
    principal       DECIMAL(19, 4) NOT NULL,
    interest_rate   DECIMAL(5, 4)  NOT NULL,
    term_months     INT            NOT NULL,
    monthly_payment DECIMAL(19, 4) NOT NULL,
    outstanding     DECIMAL(19, 4) NOT NULL,
    currency        VARCHAR(3)     NOT NULL DEFAULT 'USD',
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    disbursed_at    DATETIME,
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE IF NOT EXISTS payments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id    VARCHAR(36)    NOT NULL UNIQUE,
    source_account BIGINT         NOT NULL,
    target_account BIGINT,
    amount         DECIMAL(19, 4) NOT NULL,
    currency       VARCHAR(3)     NOT NULL DEFAULT 'USD',
    payment_type   VARCHAR(30)    NOT NULL,
    description    VARCHAR(500),
    status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    processed_at   DATETIME,
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_source FOREIGN KEY (source_account) REFERENCES accounts (id),
    CONSTRAINT fk_payment_target FOREIGN KEY (target_account) REFERENCES accounts (id)
);
