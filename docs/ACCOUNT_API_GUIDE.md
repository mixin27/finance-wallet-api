# Accounts API - Testing Guide

## Prerequisites

1. User registered and logged in
2. Access token obtained from login
3. Set your token as environment variable:
```bash
export TOKEN="your-access-token-here"
```

---

## 1. Get Account Types (For Dropdown)

### Request
```bash
curl -X GET http://localhost:8080/api/accounts/types \
  -H "Authorization: Bearer $TOKEN"
```

### Expected Response
```json
{
  "success": true,
  "message": "Account types retrieved successfully",
  "data": [
    {
      "id": "uuid-here",
      "name": "BANK",
      "icon": "bank"
    },
    {
      "id": "uuid-here",
      "name": "CASH",
      "icon": "cash"
    },
    ...
  ]
}
```

**Save an account type ID for next step!**

---

## 2. Create Account

### Request
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Main Bank Account",
    "accountTypeId": "YOUR_ACCOUNT_TYPE_ID",
    "currencyId": "YOUR_CURRENCY_ID",
    "description": "My primary bank account",
    "initialBalance": 5000.00,
    "color": "#4CAF50",
    "icon": "account_balance",
    "isIncludedInTotal": true
  }'
```

### Expected Response (201 Created)
```json
{
  "success": true,
  "message": "Account created successfully",
  "data": {
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
    },
    "description": "My primary bank account",
    "initialBalance": 5000.00,
    "currentBalance": 5000.00,
    "color": "#4CAF50",
    "icon": "account_balance",
    "isIncludedInTotal": true,
    "isActive": true,
    "createdAt": "2024-12-15T10:30:00",
    "updatedAt": "2024-12-15T10:30:00"
  }
}
```

**Save the account ID for next steps!**

---

## 3. Create Multiple Accounts (For Testing)

### Cash Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cash Wallet",
    "accountTypeId": "CASH_TYPE_ID",
    "currencyId": "YOUR_CURRENCY_ID",
    "initialBalance": 500.00,
    "color": "#FF9800",
    "icon": "payments"
  }'
```

### Credit Card
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Visa Credit Card",
    "accountTypeId": "CREDIT_CARD_TYPE_ID",
    "currencyId": "YOUR_CURRENCY_ID",
    "initialBalance": -1000.00,
    "color": "#F44336",
    "icon": "credit_card"
  }'
```

### Savings Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emergency Fund",
    "accountTypeId": "SAVINGS_TYPE_ID",
    "currencyId": "YOUR_CURRENCY_ID",
    "description": "Emergency savings",
    "initialBalance": 10000.00,
    "color": "#2196F3",
    "icon": "savings",
    "isIncludedInTotal": true
  }'
```

---

## 4. Get All Accounts

### Request
```bash
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN"
```

### Expected Response
```json
{
  "success": true,
  "message": "Accounts retrieved successfully",
  "data": [
    {
      "id": "account-1-uuid",
      "name": "Main Bank Account",
      "accountType": { ... },
      "currency": { ... },
      "currentBalance": 5000.00,
      ...
    },
    {
      "id": "account-2-uuid",
      "name": "Cash Wallet",
      ...
    }
  ]
}
```

---

## 5. Get Account Summary

### Request
```bash
curl -X GET http://localhost:8080/api/accounts/summary \
  -H "Authorization: Bearer $TOKEN"
```

### Expected Response
```json
{
  "success": true,
  "message": "Account summary retrieved successfully",
  "data": {
    "totalAccounts": 4,
    "activeAccounts": 4,
    "totalBalance": 14500.00,
    "balanceByCurrency": [
      {
        "currencyCode": "USD",
        "currencySymbol": "$",
        "balance": 14500.00,
        "accountCount": 4
      }
    ],
    "accounts": [ ... ]
  }
}
```

---

## 6. Get Specific Account

### Request
```bash
curl -X GET http://localhost:8080/api/accounts/YOUR_ACCOUNT_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Expected Response
```json
{
  "success": true,
  "message": "Account retrieved successfully",
  "data": {
    "id": "account-uuid",
    "name": "Main Bank Account",
    ...
  }
}
```

---

## 7. Update Account

### Request
```bash
curl -X PUT http://localhost:8080/api/accounts/YOUR_ACCOUNT_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Bank Account",
    "description": "Updated description",
    "color": "#009688",
    "isIncludedInTotal": false
  }'
```

### Expected Response
```json
{
  "success": true,
  "message": "Account updated successfully",
  "data": {
    "id": "account-uuid",
    "name": "Updated Bank Account",
    "description": "Updated description",
    "color": "#009688",
    "isIncludedInTotal": false,
    ...
  }
}
```

---

## 8. Delete Account (Zero Balance Required)

### First, verify balance is zero
```bash
curl -X GET http://localhost:8080/api/accounts/YOUR_ACCOUNT_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Delete Request
```bash
curl -X DELETE http://localhost:8080/api/accounts/YOUR_ACCOUNT_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Expected Response (200 OK)
```json
{
  "success": true,
  "message": "Account deleted successfully"
}
```

### Error Response - Non-Zero Balance (400)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot delete account with non-zero balance. Current balance: $5000.00",
  "path": "/accounts/uuid"
}
```

---

## 9. Get Inactive Accounts

### Request
```bash
curl -X GET "http://localhost:8080/api/accounts?includeInactive=true" \
  -H "Authorization: Bearer $TOKEN"
```

This includes both active and inactive (deleted) accounts.

---

## Error Responses

### 401 Unauthorized - No Token
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 401 Unauthorized - Invalid Token
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "You need to login to access this resource"
}
```

### 404 Not Found - Account Doesn't Exist
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Account not found with id: uuid-here"
}
```

### 400 Bad Request - Validation Error
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "validationErrors": {
    "name": "Account name is required",
    "currencyId": "Currency ID is required"
  }
}
```

### 400 Bad Request - Invalid Color
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid color format. Use hex format: #RRGGBB"
}
```

---

## Complete Testing Workflow

### Step 1: Setup
```bash
# Login and get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"your@email.com","password":"yourpassword"}'

# Save token
export TOKEN="your-access-token"
```

### Step 2: Get Reference Data
```bash
# Get currencies
curl http://localhost:8080/api/currencies | jq

# Get account types
curl -X GET http://localhost:8080/api/accounts/types \
  -H "Authorization: Bearer $TOKEN" | jq

# Save IDs you want to use
export CURRENCY_ID="uuid-from-response"
export ACCOUNT_TYPE_ID="uuid-from-response"
```

### Step 3: Create Accounts
```bash
# Create bank account
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"My Bank\",
    \"accountTypeId\": \"$ACCOUNT_TYPE_ID\",
    \"currencyId\": \"$CURRENCY_ID\",
    \"initialBalance\": 1000.00,
    \"color\": \"#4CAF50\"
  }" | jq

# Save account ID
export ACCOUNT_ID="uuid-from-response"
```

### Step 4: Test Operations
```bash
# Get all accounts
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" | jq

# Get summary
curl -X GET http://localhost:8080/api/accounts/summary \
  -H "Authorization: Bearer $TOKEN" | jq

# Update account
curl -X PUT http://localhost:8080/api/accounts/$ACCOUNT_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Name"}' | jq
```

---

## Postman Collection

Import this into Postman for easier testing:

```json
{
  "info": {
    "name": "Accounts API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create Account",
      "request": {
        "method": "POST",
        "header": [
          {"key": "Authorization", "value": "Bearer {{token}}"},
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"My Account\",\n  \"accountTypeId\": \"{{accountTypeId}}\",\n  \"currencyId\": \"{{currencyId}}\",\n  \"initialBalance\": 1000.00,\n  \"color\": \"#4CAF50\"\n}"
        },
        "url": {"raw": "{{baseUrl}}/accounts"}
      }
    },
    {
      "name": "Get All Accounts",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
        "url": {"raw": "{{baseUrl}}/accounts"}
      }
    },
    {
      "name": "Get Account Summary",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
        "url": {"raw": "{{baseUrl}}/accounts/summary"}
      }
    }
  ],
  "variable": [
    {"key": "baseUrl", "value": "http://localhost:8080/api"}
  ]
}
```

---

## Next Steps

After accounts are working:
1. ✅ Test all CRUD operations
2. ✅ Create multiple accounts with different currencies
3. ✅ Verify summary calculations
4. 🎯 Move to Transactions API (income/expense/transfer)
5. 🎯 Start Flutter integration

---

## Common Issues

**Issue: Can't delete account**
- Check account balance is zero
- Use GET to verify balance first

**Issue: Invalid color format**
- Use hex format: #RRGGBB or #RGB
- Examples: #4CAF50, #F00

**Issue: Currency/AccountType not found**
- Get valid IDs from `/currencies` and `/accounts/types` first
- Save the UUIDs for use in requests