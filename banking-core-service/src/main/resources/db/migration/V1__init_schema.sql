-- V1__init_schema.sql
-- Baseline customers table referenced by V2__add_payees_table.sql and V3__create_notification_log_table.sql.

CREATE TABLE customers (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    external_id VARCHAR(36)  NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_external_id UNIQUE (external_id),
    CONSTRAINT uq_customers_email UNIQUE (email)
);
