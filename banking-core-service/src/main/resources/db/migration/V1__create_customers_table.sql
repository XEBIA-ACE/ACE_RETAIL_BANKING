-- V1__create_customers_table.sql
-- Baseline customers table. V2__add_payees_table.sql and later migrations reference it.

CREATE TABLE customers (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    external_id VARCHAR(36)  NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_external_id UNIQUE (external_id),
    CONSTRAINT uq_customers_email UNIQUE (email)
);
