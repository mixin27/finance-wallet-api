-- Finance Wallet Database Schema
-- PostgreSQL 14+

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- USERS & AUTHENTICATION
-- ============================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255), -- Nullable for OAuth-only users
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    profile_image_url VARCHAR(500),
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    auth_provider VARCHAR(50) DEFAULT 'LOCAL', -- LOCAL, GOOGLE, APPLE
    provider_id VARCHAR(255), -- OAuth provider user ID
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE
);

-- ============================================
-- CURRENCIES
-- ============================================

CREATE TABLE currencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(3) UNIQUE NOT NULL, -- ISO 4217 (USD, EUR, IDR, etc.)
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    decimal_places SMALLINT DEFAULT 2,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exchange_rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_currency_id UUID NOT NULL REFERENCES currencies(id),
    to_currency_id UUID NOT NULL REFERENCES currencies(id),
    rate DECIMAL(20, 8) NOT NULL,
    effective_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(from_currency_id, to_currency_id, effective_date)
);

-- ============================================
-- ACCOUNTS
-- ============================================

CREATE TABLE account_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL, -- BANK, CASH, CREDIT_CARD, INVESTMENT, LOAN, etc.
    description VARCHAR(255),
    icon VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_type_id UUID NOT NULL REFERENCES account_types(id),
    currency_id UUID NOT NULL REFERENCES currencies(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    initial_balance DECIMAL(19, 4) DEFAULT 0,
    current_balance DECIMAL(19, 4) DEFAULT 0,
    color VARCHAR(7), -- Hex color for UI
    icon VARCHAR(100),
    is_included_in_total BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- CATEGORIES
-- ============================================

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE, -- NULL for system/default categories
    parent_category_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE, TRANSFER
    color VARCHAR(7),
    icon VARCHAR(100),
    display_order INTEGER DEFAULT 0,
    is_system BOOLEAN DEFAULT FALSE, -- System categories can't be deleted
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TRANSACTIONS
-- ============================================

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    to_account_id UUID REFERENCES accounts(id) ON DELETE CASCADE, -- For transfers
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE, TRANSFER
    amount DECIMAL(19, 4) NOT NULL,
    currency_id UUID NOT NULL REFERENCES currencies(id),
    exchange_rate DECIMAL(20, 8) DEFAULT 1, -- For multi-currency transfers
    converted_amount DECIMAL(19, 4), -- Amount in target account currency (for transfers)
    transaction_date TIMESTAMP NOT NULL,
    description TEXT,
    note TEXT,
    reference_number VARCHAR(100), -- Invoice number, check number, etc.
    payee VARCHAR(255), -- Who received/paid
    location VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_transaction_id UUID, -- Link to recurring template
    status VARCHAR(20) DEFAULT 'COMPLETED', -- PENDING, COMPLETED, CANCELLED
    synced_at TIMESTAMP, -- For offline sync
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TRANSACTION ATTACHMENTS
-- ============================================

CREATE TABLE transaction_attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    file_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TAGS
-- ============================================

CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);

CREATE TABLE transaction_tags (
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (transaction_id, tag_id)
);

-- ============================================
-- BUDGETS
-- ============================================

CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    currency_id UUID NOT NULL REFERENCES currencies(id),
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    period VARCHAR(20) NOT NULL, -- DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    start_date DATE NOT NULL,
    end_date DATE,
    alert_threshold DECIMAL(5, 2) DEFAULT 80, -- Alert when 80% spent
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- RECURRING TRANSACTIONS
-- ============================================

CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    to_account_id UUID REFERENCES accounts(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_id UUID NOT NULL REFERENCES currencies(id),
    description TEXT,
    frequency VARCHAR(20) NOT NULL, -- DAILY, WEEKLY, MONTHLY, YEARLY
    interval_value INTEGER DEFAULT 1, -- Every 1 month, every 2 weeks, etc.
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence_date DATE NOT NULL,
    last_generated_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- GOALS/SAVINGS
-- ============================================

CREATE TABLE goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID REFERENCES accounts(id) ON DELETE SET NULL,
    currency_id UUID NOT NULL REFERENCES currencies(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    target_amount DECIMAL(19, 4) NOT NULL,
    current_amount DECIMAL(19, 4) DEFAULT 0,
    target_date DATE,
    color VARCHAR(7),
    icon VARCHAR(100),
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- USER PREFERENCES
-- ============================================

CREATE TABLE user_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    default_currency_id UUID REFERENCES currencies(id),
    language VARCHAR(10) DEFAULT 'en',
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    first_day_of_week SMALLINT DEFAULT 1, -- 1=Monday, 0=Sunday
    theme VARCHAR(20) DEFAULT 'LIGHT', -- LIGHT, DARK, SYSTEM
    enable_notifications BOOLEAN DEFAULT TRUE,
    enable_biometric BOOLEAN DEFAULT FALSE,
    auto_backup BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- SYNC LOG (for offline-first support)
-- ============================================

CREATE TABLE sync_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL, -- TRANSACTION, ACCOUNT, etc.
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE
    synced BOOLEAN DEFAULT FALSE,
    sync_attempted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(auth_provider, provider_id);

-- Refresh Tokens
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Accounts
CREATE INDEX idx_accounts_user ON accounts(user_id);
CREATE INDEX idx_accounts_user_active ON accounts(user_id, is_active);

-- Transactions
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date DESC);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_type ON transactions(type);

-- Categories
CREATE INDEX idx_categories_user ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);

-- Budgets
CREATE INDEX idx_budgets_user ON budgets(user_id);
CREATE INDEX idx_budgets_user_active ON budgets(user_id, is_active);

-- Recurring Transactions
CREATE INDEX idx_recurring_user ON recurring_transactions(user_id);
CREATE INDEX idx_recurring_next_date ON recurring_transactions(next_occurrence_date);

-- Goals
CREATE INDEX idx_goals_user ON goals(user_id);

-- Sync Log
CREATE INDEX idx_sync_log_user_synced ON sync_log(user_id, synced);

-- ============================================
-- INSERT DEFAULT DATA
-- ============================================

-- Default Currencies
INSERT INTO currencies (code, name, symbol, decimal_places) VALUES
('USD', 'US Dollar', '$', 2),
('EUR', 'Euro', '€', 2),
('GBP', 'British Pound', '£', 2),
('JPY', 'Japanese Yen', '¥', 0),
('CNY', 'Chinese Yuan', '¥', 2),
('INR', 'Indian Rupee', '₹', 2),
('IDR', 'Indonesian Rupiah', 'Rp', 0),
('SGD', 'Singapore Dollar', 'S$', 2),
('MYR', 'Malaysian Ringgit', 'RM', 2),
('THB', 'Thai Baht', '฿', 2);

-- Default Account Types
INSERT INTO account_types (name, description, icon) VALUES
('BANK', 'Bank Account', 'bank'),
('CASH', 'Cash', 'cash'),
('CREDIT_CARD', 'Credit Card', 'credit_card'),
('INVESTMENT', 'Investment Account', 'trending_up'),
('LOAN', 'Loan Account', 'account_balance'),
('SAVINGS', 'Savings Account', 'savings'),
('WALLET', 'Digital Wallet', 'account_balance_wallet');

-- Default System Categories for Expenses
INSERT INTO categories (name, type, color, icon, is_system, display_order) VALUES
('Food & Dining', 'EXPENSE', '#FF6B6B', 'restaurant', TRUE, 1),
('Transportation', 'EXPENSE', '#4ECDC4', 'directions_car', TRUE, 2),
('Shopping', 'EXPENSE', '#45B7D1', 'shopping_cart', TRUE, 3),
('Entertainment', 'EXPENSE', '#FFA07A', 'movie', TRUE, 4),
('Bills & Utilities', 'EXPENSE', '#98D8C8', 'receipt', TRUE, 5),
('Healthcare', 'EXPENSE', '#FF69B4', 'local_hospital', TRUE, 6),
('Education', 'EXPENSE', '#9370DB', 'school', TRUE, 7),
('Personal Care', 'EXPENSE', '#FFB6C1', 'spa', TRUE, 8),
('Housing', 'EXPENSE', '#DDA15E', 'home', TRUE, 9),
('Insurance', 'EXPENSE', '#BC6C25', 'security', TRUE, 10),
('Gifts & Donations', 'EXPENSE', '#F4A261', 'card_giftcard', TRUE, 11),
('Travel', 'EXPENSE', '#2A9D8F', 'flight', TRUE, 12),
('Other Expense', 'EXPENSE', '#95A5A6', 'more_horiz', TRUE, 13);

-- Default System Categories for Income
INSERT INTO categories (name, type, color, icon, is_system, display_order) VALUES
('Salary', 'INCOME', '#27AE60', 'work', TRUE, 1),
('Business', 'INCOME', '#16A085', 'business', TRUE, 2),
('Investment', 'INCOME', '#1ABC9C', 'trending_up', TRUE, 3),
('Gifts', 'INCOME', '#2ECC71', 'card_giftcard', TRUE, 4),
('Refund', 'INCOME', '#58D68D', 'money', TRUE, 5),
('Other Income', 'INCOME', '#82E0AA', 'more_horiz', TRUE, 6);