-- ====================================================================
-- PERSONAL FINANCE TRACKER - DATABASE SCHEMA & ENTERPRISE ADMIN SUITE
-- Target Engine: MySQL 8.0+ / MySQL 9.0+
-- Database: personal_finance_tracker
-- ====================================================================

-- --------------------------------------------------------------------
-- OPTIONAL: DEDICATED APPLICATION DATABASE USER (RECOMMENDED FOR PRODUCTION)
-- Execute the following in MySQL Workbench or mysql CLI as root:
--
-- CREATE USER IF NOT EXISTS 'pft_user'@'localhost' IDENTIFIED BY 'PftSecure2026!';
-- GRANT ALL PRIVILEGES ON personal_finance_tracker.* TO 'pft_user'@'localhost';
-- FLUSH PRIVILEGES;
-- --------------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS personal_finance_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE personal_finance_tracker;

-- --------------------------------------------------------------------
-- 1. USERS TABLE (Role-Based Access & Account Status)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(60) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    password_salt VARCHAR(32) NOT NULL,
    mobile_number VARCHAR(20) NULL,
    date_of_birth DATE NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',            -- 'USER' or 'ADMIN'
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE' or 'SUSPENDED'
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 2. TRANSACTIONS TABLE
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    transaction_type ENUM('INCOME', 'EXPENSE') NOT NULL,
    amount DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    category VARCHAR(60) NOT NULL,
    description VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_tx_user_date ON transactions (user_id, transaction_date);
CREATE INDEX idx_tx_user_type ON transactions (user_id, transaction_type);
CREATE INDEX idx_tx_user_category ON transactions (user_id, category);

-- --------------------------------------------------------------------
-- 3. BUDGETS TABLE
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(60) NOT NULL,
    budget_month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    budget_limit DECIMAL(12, 2) NOT NULL CHECK (budget_limit > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_user_cat_month UNIQUE (user_id, category, budget_month)
) ENGINE=InnoDB;

CREATE INDEX idx_budget_user_month ON budgets (user_id, budget_month);

-- --------------------------------------------------------------------
-- 4. USER SESSIONS TABLE (Session Management Center)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id VARCHAR(64) PRIMARY KEY,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'EXPIRED', 'REVOKED'
    client_info VARCHAR(255) NULL,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_session_user ON user_sessions (user_id);
CREATE INDEX idx_session_status ON user_sessions (status);
CREATE INDEX idx_session_expires ON user_sessions (expires_at);

-- --------------------------------------------------------------------
-- 5. PASSWORD RESET TOKENS TABLE (Cryptographic Recovery)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    client_ip VARCHAR(60) NULL,
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_reset_user ON password_reset_tokens (user_id);
CREATE INDEX idx_reset_hash ON password_reset_tokens (token_hash);
CREATE INDEX idx_reset_expires ON password_reset_tokens (expires_at);

-- --------------------------------------------------------------------
-- 6. ADMIN AUDIT LOGS TABLE (Append-Only Audit Trail)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id INT NOT NULL,
    actor_email VARCHAR(120) NOT NULL,
    action_type VARCHAR(60) NOT NULL,
    target_type VARCHAR(60) NOT NULL,
    target_id VARCHAR(60) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_audit_time ON admin_audit_logs (timestamp);
CREATE INDEX idx_audit_action ON admin_audit_logs (action_type);
CREATE INDEX idx_audit_actor ON admin_audit_logs (actor_email);

-- --------------------------------------------------------------------
-- 7. UI TEXT & LOCALIZATION SETTINGS TABLE
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ui_text_settings (
    text_key VARCHAR(100) PRIMARY KEY,
    text_value TEXT NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    description VARCHAR(255) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 8. THEME VERSIONS TABLE (Appearance Studio Persistence)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS theme_versions (
    version_id INT AUTO_INCREMENT PRIMARY KEY,
    theme_name VARCHAR(100) NOT NULL,
    is_dark BOOLEAN NOT NULL DEFAULT FALSE,
    color_primary VARCHAR(10) NOT NULL,
    color_primary_hover VARCHAR(10) NOT NULL,
    color_bg_main VARCHAR(10) NOT NULL,
    color_surface VARCHAR(10) NOT NULL,
    color_surface_hover VARCHAR(10) NOT NULL,
    color_sidebar_bg VARCHAR(10) NOT NULL,
    color_header_bg VARCHAR(10) NOT NULL,
    color_text_main VARCHAR(10) NOT NULL,
    color_text_muted VARCHAR(10) NOT NULL,
    color_border VARCHAR(10) NOT NULL,
    color_income VARCHAR(10) NOT NULL,
    color_expense VARCHAR(10) NOT NULL,
    color_balance VARCHAR(10) NOT NULL,
    color_warning VARCHAR(10) NOT NULL,
    font_family VARCHAR(50) NOT NULL DEFAULT 'Segoe UI',
    font_scale_percent INT NOT NULL DEFAULT 100,
    card_corner_radius INT NOT NULL DEFAULT 16,
    layout_density VARCHAR(20) NOT NULL DEFAULT 'COMFORTABLE',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 9. INITIAL ADMINISTRATOR CREATION GUIDELINES
-- --------------------------------------------------------------------
-- In compliance with enterprise security requirements, no default administrator
-- account is seeded with hardcoded credentials in this script.
--
-- To securely establish the initial administrator:
-- 1. Register a standard user account through the application UI.
-- 2. Set the ADMIN_BOOTSTRAP_TOKEN environment variable and execute the one-time
--    bootstrap promotion wizard on the login screen.
--    OR
-- 3. Execute an authorized SQL command:
--    UPDATE users SET role = 'ADMIN' WHERE email = 'your.registered.email@example.com';
