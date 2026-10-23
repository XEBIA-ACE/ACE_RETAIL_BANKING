package com.bank.core.notification.domain;

/** What an alert relates to, so customers can tell transaction, balance and bill alerts apart (FR-005). */
public enum NotificationCategory {
    TRANSACTION,
    BALANCE,
    BILL
}
