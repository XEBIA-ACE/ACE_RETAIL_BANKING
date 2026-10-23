-- V3__create_notification_log_table.sql
-- US-028: customer notification history (DS-001). Rows are append-only and are purged once they
-- fall outside notification.history.retention (baseline 90 days).

CREATE TABLE notification_log (
    notification_id   BIGINT       NOT NULL AUTO_INCREMENT,
    external_id       VARCHAR(36)  NOT NULL,
    customer_id       BIGINT       NOT NULL,
    notification_type VARCHAR(32)  NOT NULL,
    description       VARCHAR(255) NOT NULL,
    created_timestamp TIMESTAMP(6) NOT NULL,
    locale            VARCHAR(10)  NOT NULL,

    CONSTRAINT pk_notification_log PRIMARY KEY (notification_id),
    CONSTRAINT uq_notification_log_external_id UNIQUE (external_id),
    CONSTRAINT ck_notification_log_type
        CHECK (notification_type IN ('LARGE_TRANSACTION', 'LOW_BALANCE', 'BILL_REMINDER')),
    CONSTRAINT fk_notification_log_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers (id)
);

-- Customer-scoped history query ordered by recency (FR-002, FR-008)
CREATE INDEX idx_notification_log_customer_created ON notification_log (customer_id, created_timestamp);

-- Retention purge (FR-007)
CREATE INDEX idx_notification_log_created ON notification_log (created_timestamp);
