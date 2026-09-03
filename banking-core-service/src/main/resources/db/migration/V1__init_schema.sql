CREATE TABLE customers (
    id             BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    external_id    VARCHAR(255) NOT NULL,
    first_name     VARCHAR(255) NOT NULL,
    last_name      VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    phone_number   VARCHAR(50),
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_external_id UNIQUE (external_id),
    CONSTRAINT uq_customers_email UNIQUE (email)
);

CREATE INDEX idx_customers_external_id ON customers (external_id);