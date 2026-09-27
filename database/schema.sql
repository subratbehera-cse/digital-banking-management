-- =====================================================================
-- Digital Banking Management System - MySQL Schema
-- Note: Hibernate (ddl-auto: update) will create/update these tables
-- automatically on startup. This script is provided for reference,
-- manual provisioning, or for DBAs who prefer to manage DDL explicitly.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS banking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE banking_db;

-- ---------------------------------------------------------------------
-- roles
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    phone_number  VARCHAR(20),
    address       VARCHAR(255),
    enabled       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- user_roles (many-to-many)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- bank_accounts
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bank_accounts (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type   VARCHAR(20) NOT NULL,
    balance        DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id        BIGINT NOT NULL,
    version        BIGINT NOT NULL DEFAULT 0,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_account_number (account_number),
    INDEX idx_account_user (user_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- beneficiaries
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS beneficiaries (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id                    BIGINT NOT NULL,
    beneficiary_name            VARCHAR(100) NOT NULL,
    beneficiary_account_number  VARCHAR(20) NOT NULL,
    bank_name                   VARCHAR(100),
    nickname                    VARCHAR(50),
    created_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_beneficiary_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_beneficiary_owner (owner_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- transactions
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_ref  VARCHAR(40) NOT NULL UNIQUE,
    from_account_id  BIGINT NULL,
    to_account_id    BIGINT NULL,
    type             VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    amount           DECIMAL(19,2) NOT NULL,
    balance_after    DECIMAL(19,2),
    description      VARCHAR(255),
    failure_reason   VARCHAR(255),
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_from_account FOREIGN KEY (from_account_id) REFERENCES bank_accounts(id),
    CONSTRAINT fk_txn_to_account   FOREIGN KEY (to_account_id)   REFERENCES bank_accounts(id),
    INDEX idx_txn_reference (transaction_ref),
    INDEX idx_txn_from_account (from_account_id),
    INDEX idx_txn_to_account (to_account_id),
    INDEX idx_txn_created_at (created_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Seed roles
-- ---------------------------------------------------------------------
INSERT IGNORE INTO roles (name) VALUES ('ROLE_CUSTOMER');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
