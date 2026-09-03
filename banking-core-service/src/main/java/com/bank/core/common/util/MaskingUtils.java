```java
package com.bank.core.common.util;

public final class MaskingUtils {

    private static final String MASK_PREFIX = "****";
    private static final int LAST_FOUR_LENGTH = 4;

    private MaskingUtils() {
        // Utility class — not instantiable
    }

    /**
     * Returns the account number masked as "****{last4}".
     * If the input is null, blank, or shorter than 4 characters, returns "****".
     */
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return MASK_PREFIX;
        }
        String trimmed = accountNumber.trim();
        if (trimmed.length() <= LAST_FOUR_LENGTH) {
            return MASK_PREFIX;
        }
        return MASK_PREFIX + trimmed.substring(trimmed.length() - LAST_FOUR_LENGTH);
    }

    /**
     * Returns the last 4 digits of the account number.
     * If the input is null, blank, or shorter than 4 characters, returns "0000".
     */
    public static String lastFour(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return "0000";
        }
        String trimmed = accountNumber.trim();
        if (trimmed.length() <= LAST_FOUR_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(trimmed.length() - LAST_FOUR_LENGTH);
    }
}
```