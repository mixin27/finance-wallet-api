# Finance Wallet API - Complete Quick Start Guide

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- PostgreSQL database
- Gradle (included via wrapper)

---

## 📦 Installation & Setup

### 1. Clone and Configure

```bash
# Navigate to project directory
cd finance-wallet-api

# Create uploads directory
mkdir -p uploads/transactions
```

### 2. Configure Database

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_wallet
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your-super-secret-jwt-key-min-256-bits-long-change-this-in-production
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
app.upload.dir=uploads
```

### 3. Start the Application

```bash
# Build and run
./gradlew bootRun

# Or build JAR first
./gradlew build
java -jar build/libs/finance-wallet-api-0.0.1-SNAPSHOT.jar
```

Application starts at: `http://localhost:8080`

---

## 🧪 Complete API Testing Flow

### Step 1: Register User

```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "username": "johndoe",
    "password": "securePassword123",
    "fullName": "John Doe"
  }'
```

### Step 2: Login & Get Token

```bash
# Login
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePassword123"
  }'

# Save token for subsequent requests
export JWT="your-jwt-token-from-login-response"
```

### Step 3: Get Available Currencies

```bash
curl -X GET "http://localhost:8080/api/currencies" \
  -H "Authorization: Bearer $JWT"

# Save a currency ID for account creation
export CURRENCY_ID="currency-uuid-from-response"
```

### Step 4: Create Accounts

```bash
# Create Checking Account
curl -X POST "http://localhost:8080/api/accounts" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Main Checking",
    "accountTypeId": "'$ACCOUNT_TYPE_ID'",
    "currencyId": "'$CURRENCY_ID'",
    "initialBalance": 5000.00,
    "color": "#4CAF50",
    "icon": "💰"
  }'

# Create Savings Account
curl -X POST "http://localhost:8080/api/accounts" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emergency Savings",
    "accountTypeId": "'$ACCOUNT_TYPE_ID'",
    "currencyId": "'$CURRENCY_ID'",
    "initialBalance": 10000.00,
    "color": "#2196F3",
    "icon": "🏦"
  }'

# Save account ID for transactions
export CHECKING_ID="checking-account-uuid"
export SAVINGS_ID="savings-account-uuid"
```

### Step 5: Explore Categories

```bash
# Get all categories (system + custom)
curl -X GET "http://localhost:8080/api/categories" \
  -H "Authorization: Bearer $JWT"

# Get only expense categories
curl -X GET "http://localhost:8080/api/categories?type=EXPENSE" \
  -H "Authorization: Bearer $JWT"

# Save a category ID
export FOOD_CATEGORY_ID="food-category-uuid"
```

### Step 6: Create Transactions

```bash
# Add Income
curl -X POST "http://localhost:8080/api/transactions" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "'$CHECKING_ID'",
    "type": "INCOME",
    "amount": 4500.00,
    "transactionDate": "2024-01-05T09:00:00",
    "description": "Monthly Salary",
    "categoryId": "'$SALARY_CATEGORY_ID'"
  }'

# Add Expense
curl -X POST "http://localhost:8080/api/transactions" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "'$CHECKING_ID'",
    "type": "EXPENSE",
    "amount": 85.50,
    "transactionDate": "2024-01-15T19:30:00",
    "description": "Dinner at Italian Restaurant",
    "categoryId": "'$FOOD_CATEGORY_ID'",
    "payee": "La Trattoria",
    "tags": ["dining out", "date night"]
  }'

# Transfer Between Accounts
curl -X POST "http://localhost:8080/api/transactions/transfer" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "'$CHECKING_ID'",
    "toAccountId": "'$SAVINGS_ID'",
    "amount": 1000.00,
    "transactionDate": "2024-01-10T10:00:00",
    "description": "Monthly Savings"
  }'
```

### Step 7: Upload Receipt

```bash
# Save transaction ID from previous step
export TRANSACTION_ID="transaction-uuid"

# Upload receipt image
curl -X POST "http://localhost:8080/api/transactions/$TRANSACTION_ID/attachments" \
  -H "Authorization: Bearer $JWT" \
  -F "file=@/path/to/receipt.jpg"
```

### Step 8: Create Budget

```bash
# Create monthly food budget
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Monthly Food Budget",
    "amount": 600.00,
    "period": "MONTHLY",
    "startDate": "2024-01-01",
    "currencyId": "'$CURRENCY_ID'",
    "categoryId": "'$FOOD_CATEGORY_ID'",
    "alertThreshold": 80
  }'

# Get active budgets with progress
curl -X GET "http://localhost:8080/api/budgets/active" \
  -H "Authorization: Bearer $JWT"
```

### Step 9: Create Financial Goal

```bash
# Create vacation goal
curl -X POST "http://localhost:8080/api/goals" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Summer Vacation",
    "description": "Trip to Europe",
    "targetAmount": 5000.00,
    "initialAmount": 500.00,
    "targetDate": "2024-07-01",
    "currencyId": "'$CURRENCY_ID'",
    "accountId": "'$SAVINGS_ID'",
    "icon": "✈️",
    "color": "#FF9800"
  }'

# Update goal progress (add $200)
curl -X PATCH "http://localhost:8080/api/goals/$GOAL_ID/progress" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"amount": 200.00}'
```

### Step 10: View Dashboard

```bash
# Get dashboard overview
curl -X GET "http://localhost:8080/api/dashboard" \
  -H "Authorization: Bearer $JWT"

# Get this month's detailed statistics
curl -X GET "http://localhost:8080/api/dashboard/statistics/this-month" \
  -H "Authorization: Bearer $JWT"

# Get custom date range statistics
curl -X GET "http://localhost:8080/api/dashboard/statistics?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer $JWT"
```

---

## 📚 API Endpoints Summary

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token

### Accounts
- `GET /api/accounts` - Get all accounts
- `POST /api/accounts` - Create account
- `GET /api/accounts/{id}` - Get account
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete account

### Transactions
- `GET /api/transactions` - Get all transactions (with filters)
- `POST /api/transactions` - Create transaction
- `POST /api/transactions/transfer` - Transfer between accounts
- `GET /api/transactions/{id}` - Get transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction

### File Attachments
- `POST /api/transactions/{id}/attachments` - Upload receipt
- `GET /api/transactions/{id}/attachments` - Get attachments
- `DELETE /api/transactions/{id}/attachments/{attachmentId}` - Delete attachment
- `GET /api/uploads/{subDir}/{filename}` - View/download file

### Categories
- `GET /api/categories` - Get all categories
- `GET /api/categories?type=INCOME` - Get income categories
- `POST /api/categories` - Create custom category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Budgets
- `GET /api/budgets` - Get all budgets
- `GET /api/budgets/active` - Get active budgets with progress
- `POST /api/budgets` - Create budget
- `PUT /api/budgets/{id}` - Update budget
- `DELETE /api/budgets/{id}` - Delete budget

### Goals
- `GET /api/goals` - Get all goals
- `GET /api/goals?activeOnly=true` - Get active goals
- `POST /api/goals` - Create goal
- `PATCH /api/goals/{id}/progress` - Update progress
- `PATCH /api/goals/{id}/complete` - Mark as completed
- `PUT /api/goals/{id}` - Update goal
- `DELETE /api/goals/{id}` - Delete goal

### Dashboard
- `GET /api/dashboard` - Get dashboard overview
- `GET /api/dashboard/statistics` - Get statistics (custom range)
- `GET /api/dashboard/statistics/this-month` - This month stats
- `GET /api/dashboard/statistics/last-month` - Last month stats
- `GET /api/dashboard/statistics/this-year` - This year stats

### Currencies
- `GET /api/currencies` - Get all currencies

---

## 🎯 Common Use Cases

### 1. Monthly Budget Review
```bash
# Check dashboard
curl "http://localhost:8080/api/dashboard" -H "Authorization: Bearer $JWT"

# Check active budgets
curl "http://localhost:8080/api/budgets/active" -H "Authorization: Bearer $JWT"

# Get this month's transactions
curl "http://localhost:8080/api/transactions?startDate=$(date -d "1 day ago" +%Y-%m-01)&endDate=$(date +%Y-%m-%d)" \
  -H "Authorization: Bearer $JWT"
```

### 2. Add Daily Expense with Receipt
```bash
# 1. Create transaction
TRANS_ID=$(curl -X POST "http://localhost:8080/api/transactions" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "'$CHECKING_ID'",
    "type": "EXPENSE",
    "amount": 45.99,
    "transactionDate": "'$(date -Iseconds)'",
    "description": "Groceries",
    "categoryId": "'$FOOD_CATEGORY_ID'"
  }' | jq -r '.data.id')

# 2. Upload receipt
curl -X POST "http://localhost:8080/api/transactions/$TRANS_ID/attachments" \
  -H "Authorization: Bearer $JWT" \
  -F "file=@receipt.jpg"
```

### 3. Track Savings Goal
```bash
# Create goal
GOAL_ID=$(curl -X POST "http://localhost:8080/api/goals" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emergency Fund",
    "targetAmount": 10000.00,
    "currencyId": "'$CURRENCY_ID'",
    "icon": "💰"
  }' | jq -r '.data.id')

# Add $500 weekly
curl -X PATCH "http://localhost:8080/api/goals/$GOAL_ID/progress" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}'

# Check progress
curl "http://localhost:8080/api/goals/$GOAL_ID" -H "Authorization: Bearer $JWT"
```

---

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Verify database exists
psql -U your_username -d postgres -c "\l"

# Create database if needed
psql -U your_username -d postgres -c "CREATE DATABASE finance_wallet;"
```

### JWT Token Issues
```bash
# Token expired - refresh it
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "your-refresh-token"}'

# Token invalid - login again
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "securePassword123"}'
```

### File Upload Issues
```bash
# Ensure uploads directory exists
mkdir -p uploads/transactions
chmod 755 uploads

# Check file size (max 5MB)
ls -lh receipt.jpg

# Verify file type (JPEG, PNG, GIF, PDF, WebP only)
file receipt.jpg
```

---

## 📖 Testing Guides Available

1. **Category Management API** - Complete guide with all endpoints
2. **Budget Management API** - Budget creation and tracking
3. **File Upload API** - Receipt and invoice attachments
4. **Dashboard & Statistics API** - Analytics and insights
5. **Goals Management API** - Financial goals tracking

All guides include:
- ✅ Complete curl examples
- ✅ Expected responses
- ✅ Error handling
- ✅ Common use cases
- ✅ Pro tips

---

## 🎨 Default System Categories

### Income (5 categories)
- 💼 Salary - #4CAF50
- 💻 Freelance - #2196F3
- 📈 Investment - #FF9800
- 🎁 Gift - #E91E63
- 💰 Other Income - #9C27B0

### Expense (10 categories)
- 🍽️ Food & Dining - #FF5722
- 🚗 Transportation - #3F51B5
- 🛍️ Shopping - #E91E63
- 🎬 Entertainment - #9C27B0
- 📄 Bills & Utilities - #FF9800
- 🏥 Healthcare - #F44336
- 📚 Education - #2196F3
- 🏠 Housing - #795548
- 💇 Personal Care - #00BCD4
- 💸 Other Expense - #607D8B

---

## 🚀 Next Steps

### For Development
1. Add unit tests for services
2. Add integration tests for controllers
3. Set up CI/CD pipeline
4. Add API documentation (Swagger/OpenAPI)
5. Implement remaining features (recurring transactions, OAuth2)

### For Production
1. Change JWT secret to strong random value
2. Set up proper database credentials
3. Configure CORS for your frontend domains
4. Set up SSL/TLS certificates
5. Configure cloud file storage (S3, etc.)
6. Set up monitoring and logging
7. Implement rate limiting
8. Add database backups

### For Mobile/Web App
1. Build authentication flow
2. Create account management UI
3. Implement transaction list with filters
4. Build budget tracking dashboard
5. Create goal progress visualization
6. Add receipt camera integration
7. Build analytics charts
8. Implement offline sync

---

## 💡 Pro Tips

1. **Always use HTTPS in production**
2. **Never commit JWT secrets to version control**
3. **Implement rate limiting for production**
4. **Use environment variables for sensitive config**
5. **Set up database connection pooling**
6. **Implement request/response logging**
7. **Add health check endpoints**
8. **Use CDN for file uploads in production**
9. **Implement database migrations (Flyway/Liquibase)**
10. **Add comprehensive API documentation**

---

## 🎉 You're Ready!

Your Finance Wallet API is now fully functional with:
- ✅ 8 major feature modules
- ✅ 40+ API endpoints
- ✅ Complete CRUD operations
- ✅ Real-time tracking and analytics
- ✅ File upload support
- ✅ Comprehensive error handling
- ✅ Production-ready architecture

**Start building your amazing finance app!** 🚀💪💰