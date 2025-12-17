# Database Setup Guide - DBngin + TablePlus

## Method 1: Create Database First, Then Run Schema

### Step 1: Create Database in TablePlus

1. Open **TablePlus**
2. Connect to your PostgreSQL server (from DBngin)
3. Right-click on the server → **New Database**
4. Database name: `finance_wallet_db`
5. Click **Create**

### Step 2: Enable UUID Extension

1. Select your `finance_wallet_db` database
2. Open **SQL Editor** (Cmd+E or Ctrl+E)
3. Run this first:

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### Step 3: Run Schema in Parts

**Don't run the entire schema at once.** Run in this order:

#### Part 1 - Users & Auth Tables
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    profile_image_url VARCHAR(500),
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    auth_provider VARCHAR(50) DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
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
```

#### Part 2 - Currency Tables
```sql
CREATE TABLE currencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(3) UNIQUE NOT NULL,
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
```

#### Part 3 - Account Tables
```sql
CREATE TABLE account_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL,
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
    color VARCHAR(7),
    icon VARCHAR(100),
    is_included_in_total BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Part 4 - Category Tables
```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    parent_category_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    color VARCHAR(7),
    icon VARCHAR(100),
    display_order INTEGER DEFAULT 0,
    is_system BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Part 5 - Transaction Tables
```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    to_account_id UUID REFERENCES accounts(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_id UUID NOT NULL REFERENCES currencies(id),
    exchange_rate DECIMAL(20, 8) DEFAULT 1,
    converted_amount DECIMAL(19, 4),
    transaction_date TIMESTAMP NOT NULL,
    description TEXT,
    note TEXT,
    reference_number VARCHAR(100),
    payee VARCHAR(255),
    location VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_transaction_id UUID,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
```

#### Part 6 - Tags
```sql
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
```

#### Part 7 - Budget, Goals, Recurring
```sql
CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    currency_id UUID NOT NULL REFERENCES currencies(id),
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    period VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    alert_threshold DECIMAL(5, 2) DEFAULT 80,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
    frequency VARCHAR(20) NOT NULL,
    interval_value INTEGER DEFAULT 1,
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence_date DATE NOT NULL,
    last_generated_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
```

#### Part 8 - User Preferences & Sync
```sql
CREATE TABLE user_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    default_currency_id UUID REFERENCES currencies(id),
    language VARCHAR(10) DEFAULT 'en',
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    first_day_of_week SMALLINT DEFAULT 1,
    theme VARCHAR(20) DEFAULT 'LIGHT',
    enable_notifications BOOLEAN DEFAULT TRUE,
    enable_biometric BOOLEAN DEFAULT FALSE,
    auto_backup BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sync_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL,
    synced BOOLEAN DEFAULT FALSE,
    sync_attempted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Part 9 - Create Indexes
```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(auth_provider, provider_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_accounts_user ON accounts(user_id);
CREATE INDEX idx_accounts_user_active ON accounts(user_id, is_active);
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date DESC);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_categories_user ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);
CREATE INDEX idx_budgets_user ON budgets(user_id);
CREATE INDEX idx_budgets_user_active ON budgets(user_id, is_active);
CREATE INDEX idx_recurring_user ON recurring_transactions(user_id);
CREATE INDEX idx_recurring_next_date ON recurring_transactions(next_occurrence_date);
CREATE INDEX idx_goals_user ON goals(user_id);
CREATE INDEX idx_sync_log_user_synced ON sync_log(user_id, synced);
```

#### Part 10 - Insert Default Data
```sql
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
```

---

## Method 2: Let Spring Boot Create Tables (Easier!)

If you're having trouble with the SQL script, just let Spring Boot create the tables automatically:

### Step 1: Create Empty Database
1. In TablePlus, create database: `finance_wallet_db`
2. Enable UUID extension:
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### Step 2: Update application.properties
```properties
spring.jpa.hibernate.ddl-auto=create
```

### Step 3: Run Spring Boot Application
```bash
./gradlew bootRun
```

Spring Boot will automatically create all tables from your entities!

### Step 4: Insert Default Data Manually
After tables are created, run the INSERT statements (Part 10 above) in TablePlus.

### Step 5: Change ddl-auto back
```properties
spring.jpa.hibernate.ddl-auto=update
```

---

## Troubleshooting Common Errors

### Error: "extension uuid-ossp does not exist"
**Solution:** Run this first:
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### Error: "relation does not exist"
**Solution:** Run tables in order (users first, then tables that reference users)

### Error: "syntax error near DEFAULT"
**Solution:** Some older PostgreSQL versions don't support `DEFAULT uuid_generate_v4()`. Use Spring Boot Method 2 instead.

### Error: Cannot connect to database
**Check:**
1. DBngin - Is PostgreSQL running? (should see green dot)
2. Port: Default is 5432
3. Username: Usually `postgres`
4. Password: Set in DBngin preferences

---

## Verify Setup

After running the schema, verify tables exist:

```sql
-- Check all tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Should return 20 tables
```

Check default data:
```sql
SELECT * FROM currencies;
SELECT * FROM account_types;
SELECT * FROM categories;
```

---

## Which Method Should You Use?

- **Method 1** (SQL Script): Best for production, full control
- **Method 2** (Spring Boot auto): Fastest for development, less error-prone

I recommend **Method 2** for now since you're in development phase!