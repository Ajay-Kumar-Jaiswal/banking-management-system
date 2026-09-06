CREATE DATABASE IF NOT EXISTS banking_db;
USE banking_db;

-- ---------------- users ----------------
CREATE TABLE IF NOT EXISTS users (
    user_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    phone_number   VARCHAR(20)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    address        VARCHAR(255) NOT NULL,
    role           VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    created_at     DATETIME     NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

-- ---------------- accounts ----------------
CREATE TABLE IF NOT EXISTS accounts (
    account_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number  VARCHAR(20)   NOT NULL,
    user_id         BIGINT        NOT NULL,
    account_type    VARCHAR(20)   NOT NULL,
    balance         DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME      NOT NULL,
    version         BIGINT        DEFAULT 0,
    CONSTRAINT uk_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- ---------------- transactions ----------------
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(40)   NOT NULL,
    account_id            BIGINT        NOT NULL,
    amount                DECIMAL(19,2) NOT NULL,
    transaction_type      VARCHAR(20)   NOT NULL,
    description           VARCHAR(255),
    status                VARCHAR(20)   NOT NULL,
    balance_after         DECIMAL(19,2),
    created_at            DATETIME      NOT NULL,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(account_id)
) ENGINE=InnoDB;

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_reference ON transactions(transaction_reference);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- ---------------- beneficiaries ----------------
CREATE TABLE IF NOT EXISTS beneficiaries (
    beneficiary_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(150) NOT NULL,
    account_number  VARCHAR(20)  NOT NULL,
    bank_name       VARCHAR(150) NOT NULL,
    ifsc_code       VARCHAR(20)  NOT NULL,
    created_at      DATETIME     NOT NULL,
    CONSTRAINT fk_beneficiaries_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE INDEX idx_beneficiaries_user_id ON beneficiaries(user_id);


