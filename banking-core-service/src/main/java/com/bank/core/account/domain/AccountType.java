```java
package com.bank.core.account.domain;

public enum AccountType {

    CHECKING("Checking Account"),
    SAVINGS("Savings Account"),
    CREDIT_CARD("Credit Card");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```