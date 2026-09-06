package com.example.banking.enums;

/**
 * Application-level roles used for role-based authorization.
 * Kept as a plain enum (not a DB-driven table) since only two roles
 * are needed for this project - this is a deliberate simplicity choice.
 */
public enum Role {
    CUSTOMER,
    ADMIN
}
