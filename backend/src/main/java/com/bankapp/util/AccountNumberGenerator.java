package com.bankapp.util;

import java.security.SecureRandom;

/**
 * Generates unique, bank-style 12-digit account numbers.
 */
public final class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "10";

    private AccountNumberGenerator() {
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder(PREFIX);
        for (int i = 0; i < 10; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
