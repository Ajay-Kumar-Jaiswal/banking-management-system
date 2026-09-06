package com.example.banking.util;

import java.security.SecureRandom;

/**
 * Generates account numbers of the form "AC" + 10 random digits.
 * Uniqueness is still enforced by the DB unique constraint and re-checked
 * by AccountService in case of a (extremely unlikely) collision.
 */
public final class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "AC";
    private static final int DIGIT_COUNT = 10;

    private AccountNumberGenerator() {
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder(PREFIX);
        for (int i = 0; i < DIGIT_COUNT; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
