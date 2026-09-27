package com.bankapp.util;

import java.time.Instant;
import java.util.UUID;

/**
 * Generates a unique reference id for every transaction, combining a
 * timestamp component with a random UUID segment for traceability and uniqueness.
 */
public final class TransactionRefGenerator {

    private TransactionRefGenerator() {
    }

    public static String generate() {
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return "TXN" + Instant.now().toEpochMilli() + uuidPart;
    }
}
