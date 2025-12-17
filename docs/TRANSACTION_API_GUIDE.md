# Transactions API - Testing Guide

## Prerequisites

1. User logged in with access token
2. At least 2 accounts created (for testing transfers)
3. Know your account IDs and category IDs

```bash
export TOKEN="your-access-token"
export ACCOUNT_ID="your-account-uuid"
export CATEGORY_ID="your-category-uuid"
```

---

## 1. Create Income Transaction

### Request
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "'$ACCOUNT_ID'",
    "type": "INCOME",
    "amount": 5000.00,
    "categoryId": "'$CATEGORY_ID'",
    "transactionDate": "2024-12-15T10:00:00",
    "description": "Monthly Salary",
    "note": "December 2024 salary",
    "payee": "ABC Company",
    "tags": ["salary", "monthly"]
  }'
```

### Expected Response (201 Created)
```json
{
  "success": true,
  "message": "Transaction created successfully",
  "data": {
    "id": "transaction-uuid",
    "account": {
      "id": "account-uuid",
      "name": "Main Bank Account",
      "accountType": {
        "id": "type-uuid",
        "name": "BANK",
        "icon": "bank"
      },
      "currency": {
        "id": "currency-uuid",
        "code": "USD",
        "symbol": "$",
        "name": "US Dollar"
      }
    },
    "toAccount": null,
    "category": {
      "id": "category-uuid",
      "name": "Salary",
      "type": "INCOME",
      "color": "#27AE60",
      "icon": "work"
    },
    "type": "INCOME",
    "amount": 5000.00,
    "currency": {
      "id": "currency-uuid",
      "code": "USD",
      "symbol": "$",
      "name": "US Dollar"
    },
    "exchangeRate": null,
    "convertedAmount": null,
    "transactionDate": "2024-12-15T10:00:00",
    "description": "Monthly Salary",
    "note": "December 2024 salary",
    "payee": "ABC Company",
    "location": null,
    "latitude": null,
    "longitude": null,
    "status": "COMPLETED",
    "tags": [
      {
        "id": "tag-uuid-1",
        "name": "salary",
        "color": null
      },
      {
        "id": "tag-uuid-2",
        "name": "monthly",
        "color": null
      }
    ],
    "createdAt": "2024-12-15T10:00:00",
    "updatedAt": "2024-12-15T10:00:00"
  }
}
```

**Note:** Account balance will be increased by $5000

---

## 2. Create Expense Transaction

### Request
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "'$ACCOUNT_ID'",
    "type": "EXPENSE",
    "amount": 150.50,
    "categoryId": "'$CATEGORY_ID'",
    "transactionDate": "2024-12-15T12:30:00",
    "description": "Grocery Shopping",
    "note": "Weekly groceries at Walmart",
    "payee": "Walmart",
    "location": "123 Main St",
    "tags": ["groceries", "food"]
  }'
```

### Expected Response (201 Created)
Similar structure to income, but:
- `type`: "EXPENSE"
- Account balance will be decreased by $150.50

### Error Response - Insufficient Balance (400)
```json
{
  "status": 400,
  "error": "Insufficient Balance",
  "message": "Insufficient balance in account 'Main Bank Account'. Available: $100.00, Required: $150.50",
  "path": "/transactions"
}
```

---

## 3. Transfer Between Accounts (Same Currency)

### Request
```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "from-account-uuid",
    "toAccountId": "to-account-uuid",
    "amount": 500.00,
    "transactionDate": "2024-12-15T14:00:00",
    "description": "Transfer to Savings",
    "note": "Moving emergency fund"
  }'
```

### Expected Response (201 Created)
```json
{
  "success": true,
  "message": "Transfer completed successfully",
  "data": {
    "id": "transaction-uuid",
    "account": {
      "id": "from-account-uuid",
      "name": "Main Bank Account",
      ...
    },
    "toAccount": {
      "id": "to-account-uuid",
      "name": "Savings Account",
      ...
    },
    "type": "TRANSFER",
    "amount": 500.00,
    "exchangeRate": 1,
    "convertedAmount": 500.00,
    ...
  }
}
```

**Result:**
- From account: -$500
- To account: +$500

---

## 4. Transfer Between Accounts (Different Currencies)

### Request
```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "usd-account-uuid",
    "toAccountId": "eur-account-uuid",
    "amount": 1000.00,
    "exchangeRate": 0.85,
    "transactionDate": "2024-12-15T15:00:00",
    "description": "Transfer USD to EUR",
    "note": "1 USD = 0.85 EUR"
  }'
```

### Expected Response
```json
{
  "success": true,
  "message": "Transfer completed successfully",
  "data": {
    "amount": 1000.00,
    "exchangeRate": 0.85,
    "convertedAmount": 850.00,
    ...
  }
}
```

**Result:**
- USD account: -$1000
- EUR account: +€850

### Error Response - Missing Exchange Rate (400)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Exchange rate is required for transfers between different currencies"
}
```

---

## 5. Get All Transactions (Paginated)

### Request
```bash
curl -X GET "http://localhost:8080/api/transactions?page=0&size=10&sortBy=transactionDate&sortDirection=DESC" \
  -H "Authorization: Bearer $TOKEN"
```

### Expected Response
```json
{
  "success": true,
  "message": "Transactions retrieved successfully",
  "data": {
    "content": [
      {
        "id": "transaction-1-uuid",
        "type": "INCOME",
        "amount": 5000.00,
        ...
      },
      {
        "id": "transaction-2-uuid",
        "type": "EXPENSE",
        "amount": 150.50,
        ...
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 25,
    "totalPages": 3,
    "last": false,
    "first": true
  }
}
```

---

## 6. Filter Transactions by Account

### Request
```bash
curl -X GET "http://localhost:8080/api/transactions?accountId=$ACCOUNT_ID&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

Returns all transactions for specific account.

---

## 7. Filter Transactions by Category

### Request
```bash
curl -X GET "http://localhost:8080/api/transactions?categoryId=$CATEGORY_ID" \
  -H "Authorization: Bearer $TOKEN"
```

Returns all transactions in specific category.

---

## 8. Filter Transactions by Type

### Request - Get All Income
```bash
curl -X GET "http://localhost:8080/api/transactions?type=INCOME" \
  -H "Authorization: Bearer $TOKEN"
```

### Request - Get All Expenses
```bash
curl -X GET "http://localhost:8080/api/transactions?type=EXPENSE" \
  -H "Authorization: Bearer $TOKEN"
```

### Request - Get All Transfers
```bash
curl -X GET "http://localhost:8080/api/transactions?type=TRANSFER" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 9. Filter Transactions by Date Range

### Request - This Month
```bash
curl -X GET "http://localhost:8080/api/transactions?startDate=2024-12-01T00:00:00&endDate=2024-12-31T23:59:59" \
  -H "Authorization: Bearer $TOKEN"
```

### Request - Last 7 Days
```bash
curl -X GET "http://localhost:8080/api/transactions?startDate=2024-12-08T00:00:00&endDate=2024-12-15T23:59:59" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 10. Get Specific Transaction

### Request
```bash
curl -X GET http://localhost:8080/api/transactions/TRANSACTION_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Expected Response
```json
{
  "success": true,
  "message": "Transaction retrieved successfully",
  "data": {
    "id": "transaction-uuid",
    "type": "EXPENSE",
    "amount": 150.50,
    ...
  }
}
```

---

## 11. Update Transaction

### Request
```bash
curl -X PUT http://localhost:8080/api/transactions/TRANSACTION_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 175.00,
    "description": "Updated Grocery Shopping",
    "note": "Updated note",
    "tags": ["groceries", "food", "essentials"]
  }'
```

### Expected Response
```json
{
  "success": true,
  "message": "Transaction updated successfully",
  "data": {
    "id": "transaction-uuid",
    "amount": 175.00,
    "description": "Updated Grocery Shopping",
    ...
  }
}
```

**Note:** Account balance is automatically adjusted based on amount change.

### Error Response - Cannot Update Transfer (400)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot update transfer transactions. Delete and create a new one instead."
}
```

---

## 12. Delete Transaction

### Request
```bash
curl -X DELETE http://localhost:8080/api/transactions/TRANSACTION_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Expected Response
```json
{
  "success": true,
  "message": "Transaction deleted successfully"
}
```

**Note:** Account balance is automatically reverted.

---

## Complete Testing Workflow

### Setup
```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"your@email.com","password":"yourpassword"}' | jq

export TOKEN="access-token-from-response"

# 2. Get accounts
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" | jq

export ACCOUNT1="first-account-uuid"
export ACCOUNT2="second-account-uuid"

# 3. Get categories
curl -X GET http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" | jq

export INCOME_CAT="salary-category-uuid"
export EXPENSE_CAT="food-category-uuid"
```

### Test Income
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "'$ACCOUNT1'",
    "type": "INCOME",
    "amount": 3000.00,
    "categoryId": "'$INCOME_CAT'",
    "transactionDate": "2024-12-15T09:00:00",
    "description": "Freelance Payment",
    "payee": "Client XYZ"
  }' | jq

# Check account balance increased
curl -X GET http://localhost:8080/api/accounts/$ACCOUNT1 \
  -H "Authorization: Bearer $TOKEN" | jq '.data.currentBalance'
```

### Test Expense
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "'$ACCOUNT1'",
    "type": "EXPENSE",
    "amount": 75.00,
    "categoryId": "'$EXPENSE_CAT'",
    "transactionDate": "2024-12-15T13:00:00",
    "description": "Lunch at Restaurant"
  }' | jq

# Check account balance decreased
curl -X GET http://localhost:8080/api/accounts/$ACCOUNT1 \
  -H "Authorization: Bearer $TOKEN" | jq '.data.currentBalance'
```

### Test Transfer
```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "'$ACCOUNT1'",
    "toAccountId": "'$ACCOUNT2'",
    "amount": 500.00,
    "transactionDate": "2024-12-15T16:00:00",
    "description": "Transfer to Savings"
  }' | jq

# Check both account balances
curl -X GET http://localhost:8080/api/accounts/summary \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Test Filtering
```bash
# All transactions
curl -X GET "http://localhost:8080/api/transactions?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq

# Only income
curl -X GET "http://localhost:8080/api/transactions?type=INCOME" \
  -H "Authorization: Bearer $TOKEN" | jq

# This month only
curl -X GET "http://localhost:8080/api/transactions?startDate=2024-12-01T00:00:00&endDate=2024-12-31T23:59:59" \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## Common Error Responses

### 400 Bad Request - Insufficient Balance
```json
{
  "status": 400,
  "error": "Insufficient Balance",
  "message": "Insufficient balance in account 'Main Bank'. Available: $50.00, Required: $100.00"
}
```

### 400 Bad Request - Transfer to Same Account
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot transfer to the same account"
}
```

### 400 Bad Request - Wrong Category Type
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Category type (EXPENSE) does not match transaction type (INCOME)"
}
```

### 404 Not Found - Account Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Account not found with id: uuid-here"
}
```

---

## Success Criteria

✅ Can create income transaction (balance increases)  
✅ Can create expense transaction (balance decreases)  
✅ Can transfer between accounts (same currency)  
✅ Can transfer between accounts (different currencies)  
✅ Cannot create expense with insufficient balance  
✅ Can filter transactions by various criteria  
✅ Can update transaction (balance auto-adjusts)  
✅ Can delete transaction (balance reverts)  
✅ Account balances are always accurate

---

## Next Steps

After transactions are working:
1. ✅ Test all transaction types thoroughly
2. ✅ Verify account balances update correctly
3. ✅ Test edge cases (insufficient balance, invalid data)
4. 🎯 Add Categories API (manage custom categories)
5. 🎯 Add Dashboard/Reports API (statistics, charts)
6. 🎯 Start Flutter mobile app!