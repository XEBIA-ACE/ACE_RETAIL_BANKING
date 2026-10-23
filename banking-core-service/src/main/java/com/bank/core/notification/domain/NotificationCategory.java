package com.bank.core.notification.domain;

/** High-level grouping that lets customers tell transaction, balance and bill alerts apart (FR-004, FR-005). */
public enum NotificationCategory {
    TRANSACTION,
    BALANCE,
    BILL
}
