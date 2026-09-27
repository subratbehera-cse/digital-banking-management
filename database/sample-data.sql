-- =====================================================================
-- Optional sample data for manual testing / demos.
-- Passwords below are BCrypt hashes of "Password123" (cost factor 12).
-- Run this AFTER the application has started at least once (so that
-- roles + the default admin have been created by DataInitializer),
-- or after running schema.sql manually.
-- =====================================================================

USE banking_db;

-- Sample customer: john_doe / Password123
INSERT IGNORE INTO users (username, email, password, first_name, last_name, phone_number, address, enabled)
VALUES ('john_doe', 'john.doe@example.com',
        '$2a$12$CwE1P9DhH6f9c1s0m0lF2eYQhZzX0m1yqk1nQe1pQveA1zR8m6bJa',
        'John', 'Doe', '9876543210', '221B Baker Street, London', true);

-- Sample customer: jane_smith / Password123
INSERT IGNORE INTO users (username, email, password, first_name, last_name, phone_number, address, enabled)
VALUES ('jane_smith', 'jane.smith@example.com',
        '$2a$12$CwE1P9DhH6f9c1s0m0lF2eYQhZzX0m1yqk1nQe1pQveA1zR8m6bJa',
        'Jane', 'Smith', '9123456780', '10 Downing Street, London', true);

-- Assign ROLE_CUSTOMER to sample users
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'john_doe' AND r.name = 'ROLE_CUSTOMER';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'jane_smith' AND r.name = 'ROLE_CUSTOMER';

-- Sample accounts
INSERT IGNORE INTO bank_accounts (account_number, account_type, balance, status, user_id)
SELECT '100000000001', 'SAVINGS', 5000.00, 'ACTIVE', id FROM users WHERE username = 'john_doe';

INSERT IGNORE INTO bank_accounts (account_number, account_type, balance, status, user_id)
SELECT '100000000002', 'CURRENT', 15000.00, 'ACTIVE', id FROM users WHERE username = 'jane_smith';

-- NOTE: Replace the password hash above with a freshly generated BCrypt
-- hash if you want to guarantee the literal password "Password123" works,
-- since hash generation is environment/salt dependent. The safest way to
-- create demo users is via POST /api/auth/register.
