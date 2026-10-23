-- V3__create_notification_log_table.sql
-- US-028 (DS-001): customer notification log backing the in-app notification history.
--   notification_id   : public identifier (UUID) exposed to clients
--   customer_id       : authenticated customer's external_id (JWT customerId claim)
--   notification_type : LARGE_TRANSACTION | LOW_BALANCE | BILL_REMINDER
--   description       : concise, high-level text (no account numbers or other sensitive detail)
--   created_timestamp : when the notification was sent; drives retention filtering (FR-006, FR-007)
--   locale            : language the description was written in (de, fr, it, en)

CREATE TABLE notification_log (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    notification_id   VARCHAR(36)  NOT NULL,
    customer_id       VARCHAR(36)  NOT NULL,
    notification_type VARCHAR(40)  NOT NULL,
    description       VARCHAR(255) NOT NULL,
    created_timestamp TIMESTAMP(6) NOT NULL,
    locale            VARCHAR(10)  NOT NULL,
    created_at        TIMESTAMP(6) NOT NULL,
    updated_at        TIMESTAMP(6) NOT NULL,

    CONSTRAINT pk_notification_log PRIMARY KEY (id),
    CONSTRAINT uq_notification_log_notification_id UNIQUE (notification_id),
    CONSTRAINT fk_notification_log_customer
        FOREIGN KEY (customer_id) REFERENCES customers (external_id)
);

-- Customer-scoped, retention-bounded, newest-first history query (FR-002, FR-006, FR-008)
CREATE INDEX idx_notification_log_customer_time
    ON notification_log (customer_id, created_timestamp, id);

-- Retention purge (FR-007)
CREATE INDEX idx_notification_log_created_timestamp
    ON notification_log (created_timestamp);
