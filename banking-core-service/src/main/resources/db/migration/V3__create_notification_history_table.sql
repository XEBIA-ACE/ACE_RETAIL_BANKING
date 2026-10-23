-- V3__create_notification_history_table.sql
-- US-009 (T001): persisted smart notification history records (FR-001, FR-003).
--   source_notification_id : idempotency key supplied by the Smart Notifications capability (T002)
--   retention_expires_at   : retention expiry marker; populated once retention rules are defined (T005, open gap)

CREATE TABLE notification_history (
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    external_id            VARCHAR(36)   NOT NULL,
    source_notification_id VARCHAR(100)  NOT NULL,
    customer_external_id   VARCHAR(36)   NOT NULL,
    notification_type      VARCHAR(40)   NOT NULL,
    message                VARCHAR(1000) NOT NULL,
    notification_timestamp TIMESTAMP(6)  NOT NULL,
    delivery_status        VARCHAR(20)   NOT NULL,
    delivered_at           TIMESTAMP(6)  NOT NULL,
    retention_expires_at   TIMESTAMP(6)  NULL,
    created_at             TIMESTAMP(6)  NOT NULL,
    updated_at             TIMESTAMP(6)  NOT NULL,

    CONSTRAINT pk_notification_history PRIMARY KEY (id),
    CONSTRAINT uq_notification_history_external_id UNIQUE (external_id),
    CONSTRAINT uq_notification_history_source_id UNIQUE (source_notification_id),
    CONSTRAINT fk_notification_history_customer
        FOREIGN KEY (customer_external_id) REFERENCES customers (external_id)
);

-- Supports the customer-scoped, reverse-chronological history query (FR-001, FR-004)
CREATE INDEX idx_notification_history_customer_time
    ON notification_history (customer_external_id, notification_timestamp, id);
