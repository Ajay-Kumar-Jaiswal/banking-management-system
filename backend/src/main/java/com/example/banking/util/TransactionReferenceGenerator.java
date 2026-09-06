package com.example.banking.util;

import java.time.Instant;
import java.util.UUID;

/**
 * Generates a unique, human-shareable transaction reference such as
 * "TXN-20260115-9F3B2C1A". Combining a date stamp with a random suffix
 * keeps references sortable-by-day while remaining globally unique.
 */
public final class TransactionReferenceGenerator {

    private TransactionReferenceGenerator() {
    }

    public static String generate() {
        String datePart = Instant.now().toString().substring(0, 10).replace("-", "");
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + datePart + "-" + randomPart;
    }
}
