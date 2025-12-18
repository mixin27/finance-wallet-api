# Recurring Transactions API - Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token (from login)
- At least one account created
- At least one category available

---

## 1. Create Recurring Transaction

### Monthly Rent Payment
```bash
curl -X POST "http://localhost:8080/api/recurring-transactions" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "checking-account-uuid",
    "type": "EXPENSE",
    "amount": 1500.00,
    "description": "Monthly Rent",
    "categoryId": "housing-category-uuid",
    "frequency": "MONTHLY",
    "intervalValue": 1,
    "startDate": "2024-01-01"
  }'
```

### Weekly Groceries Budget
```bash
curl -X POST "http://localhost:8080/api/recurring-transactions" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "checking-account-uuid",
    "type": "EXPENSE",
    "amount": 150.00,
    "description": "Weekly Groceries",
    "categoryId": "food-category-uuid",
    "frequency": "WEEKLY",
    "intervalValue": 1,
    "startDate": "2024-01-01"
  }'
```

### Bi-Weekly Salary
```bash
curl -X POST "http://localhost:8080/api/recurring-transactions" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "checking-account-uuid",
    "type": "INCOME",
    "amount": 2500.00,
    "description": "Bi-Weekly Salary",
    "categoryId": "salary-category-uuid",
    "frequency": "WEEKLY",
    "intervalValue": 2,
    "startDate": "2024-01-05"
  }'
```

### Monthly Auto-Transfer to Savings
```bash
curl -X POST "http://localhost:8080/api/recurring-transactions" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "checking-account-uuid",
    "toAccountId": "savings-account-uuid",
    "type": "TRANSFER",
    "amount": 500.00,
    "description": "Monthly Auto-Save",
    "frequency": "MONTHLY",
    "intervalValue": 1,
    "startDate": "2024-01-10"
  }'
```

### Subscription with End Date
```bash
curl -X POST "http://localhost:8080/api/recurring-transactions" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "checking-account-uuid",
    "type": "EXPENSE",
    "amount": 9.99,
    "description": "Netflix Subscription",
    "categoryId": "entertainment-category-uuid",
    "frequency": "MONTHLY",
    "intervalValue": 1,
    "startDate": "2024-01-01",
    "endDate": "2024-12-31"
  }'
```

### Quarterly Bills
```bash
curl -X POST "http://localhost:8080/api/recurring-transactions" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "checking-account-uuid",
    "type": "EXPENSE",
    "amount": 250.00,
    "description": "Quarterly Insurance",
    "categoryId": "bills-category-uuid",
    "frequency": "MONTHLY",
    "intervalValue": 3,
    "startDate": "2024-01-01"
  }'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Recurring transaction created successfully",
  "data": {
    "id": "recurring-uuid",
    "accountId": "account-uuid",
    "accountName": "Main Checking",
    "toAccountId": null,
    "toAccountName": null,
    "categoryId": "category-uuid",
    "categoryName": "Housing",
    "type": "EXPENSE",
    "amount": 1500.00,
    "currencyCode": "USD",
    "currencySymbol": "$",
    "description": "Monthly Rent",
    "frequency": "MONTHLY",
    "intervalValue": 1,
    "startDate": "2024-01-01",
    "endDate": null,
    "nextOccurrenceDate": "2024-01-01",
    "lastGeneratedDate": null,
    "isActive": true,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  },
  "timestamp": "2024-01-15T10:00:00"
}
```

---

## 2. Get All Recurring Transactions

### Get All (Active + Inactive)
```bash
curl -X GET "http://localhost:8080/api/recurring-transactions" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Active Only
```bash
curl -X GET "http://localhost:8080/api/recurring-transactions?activeOnly=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Recurring transactions retrieved successfully",
  "data": [
    {
      "id": "recurring-uuid-1",
      "accountId": "account-uuid",
      "accountName": "Main Checking",
      "toAccountId": null,
      "toAccountName": null,
      "categoryId": "category-uuid",
      "categoryName": "Housing",
      "type": "EXPENSE",
      "amount": 1500.00,
      "currencyCode": "USD",
      "currencySymbol": "$",
      "description": "Monthly Rent",
      "frequency": "MONTHLY",
      "intervalValue": 1,
      "startDate": "2024-01-01",
      "endDate": null,
      "nextOccurrenceDate": "2024-02-01",
      "lastGeneratedDate": "2024-01-01",
      "isActive": true,
      "createdAt": "2024-01-15T10:00:00",
      "updatedAt": "2024-01-15T10:00:00"
    },
    {
      "id": "recurring-uuid-2",
      "accountId": "checking-uuid",
      "accountName": "Main Checking",
      "toAccountId": "savings-uuid",
      "toAccountName": "Emergency Savings",
      "categoryId": null,
      "categoryName": null,
      "type": "TRANSFER",
      "amount": 500.00,
      "currencyCode": "USD",
      "currencySymbol": "$",
      "description": "Monthly Auto-Save",
      "frequency": "MONTHLY",
      "intervalValue": 1,
      "startDate": "2024-01-10",
      "endDate": null,
      "nextOccurrenceDate": "2024-02-10",
      "lastGeneratedDate": "2024-01-10",
      "isActive": true,
      "createdAt": "2024-01-15T11:00:00",
      "updatedAt": "2024-01-15T11:00:00"
    }
  ],
  "timestamp": "2024-01-20T14:00:00"
}
```

---

## 3. Get Recurring Transaction by ID

```bash
curl -X GET "http://localhost:8080/api/recurring-transactions/{id}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 4. Update Recurring Transaction

```bash
curl -X PUT "http://localhost:8080/api/recurring-transactions/{id}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1600.00,
    "description": "Monthly Rent (Updated)",
    "endDate": "2024-12-31"
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Recurring transaction updated successfully",
  "data": {
    "id": "recurring-uuid",
    "accountId": "account-uuid",
    "accountName": "Main Checking",
    "toAccountId": null,
    "toAccountName": null,
    "categoryId": "category-uuid",
    "categoryName": "Housing",
    "type": "EXPENSE",
    "amount": 1600.00,
    "currencyCode": "USD",
    "currencySymbol": "$",
    "description": "Monthly Rent (Updated)",
    "frequency": "MONTHLY",
    "intervalValue": 1,
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "nextOccurrenceDate": "2024-02-01",
    "lastGeneratedDate": "2024-01-01",
    "isActive": true,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-20T15:00:00"
  },
  "timestamp": "2024-01-20T15:00:00"
}
```

---

## 5. Pause/Resume Recurring Transaction

### Pause (Deactivate)
```bash
curl -X PUT "http://localhost:8080/api/recurring-transactions/{id}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isActive": false
  }'
```

### Resume (Activate)
```bash
curl -X PUT "http://localhost:8080/api/recurring-transactions/{id}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isActive": true
  }'
```

---

## 6. Delete Recurring Transaction

```bash
curl -X DELETE "http://localhost:8080/api/recurring-transactions/{id}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Recurring transaction deleted successfully",
  "data": null,
  "timestamp": "2024-01-20T16:00:00"
}
```

---

## Frequency Options

### Available Frequencies
- `DAILY`: Every N days
- `WEEKLY`: Every N weeks
- `MONTHLY`: Every N months
- `YEARLY`: Every N years

### Interval Value Examples
- `intervalValue: 1` with `WEEKLY` = Every week
- `intervalValue: 2` with `WEEKLY` = Every 2 weeks (bi-weekly)
- `intervalValue: 3` with `MONTHLY` = Every 3 months (quarterly)
- `intervalValue: 6` with `MONTHLY` = Every 6 months (semi-annually)

---

## Automatic Transaction Generation

### How It Works
- **Scheduled Job**: Runs daily at 1:00 AM
- **Checks**: All recurring transactions with `nextOccurrenceDate <= today`
- **Generates**: New transaction from recurring template
- **Updates**: Account balances automatically
- **Advances**: `nextOccurrenceDate` to next occurrence
- **Deactivates**: If `endDate` is reached

### What Gets Generated
1. A new transaction with:
    - Same amount, category, description
    - `transactionDate` = current date/time
    - `isRecurring: true`
    - `recurringTransactionId` = recurring template ID
    - `status: COMPLETED`

2. Account balance updates:
    - **INCOME**: Account balance increases
    - **EXPENSE**: Account balance decreases
    - **TRANSFER**: Source decreases, destination increases

---

## Common Use Cases

### 1. Monthly Bills
```bash
# Rent
frequency: "MONTHLY", intervalValue: 1

# Utilities
frequency: "MONTHLY", intervalValue: 1

# Phone Bill
frequency: "MONTHLY", intervalValue: 1
```

### 2. Subscriptions
```bash
# Netflix, Spotify, etc.
frequency: "MONTHLY", intervalValue: 1

# Annual subscriptions (GitHub, etc.)
frequency: "YEARLY", intervalValue: 1
```

### 3. Income
```bash
# Monthly Salary
frequency: "MONTHLY", intervalValue: 1

# Bi-Weekly Paycheck
frequency: "WEEKLY", intervalValue: 2
```

### 4. Savings
```bash
# Monthly Auto-Save
frequency: "MONTHLY", intervalValue: 1
type: "TRANSFER"
```

### 5. Quarterly/Annual
```bash
# Quarterly Insurance
frequency: "MONTHLY", intervalValue: 3

# Annual Property Tax
frequency: "YEARLY", intervalValue: 1
```

---

## Testing Workflow

1. **Create recurring transactions** for common expenses
2. **Check nextOccurrenceDate** - should match start date initially
3. **Wait for scheduled job** (or trigger manually)
4. **Verify transaction generated** in transactions list
5. **Check account balance** updated correctly
6. **Verify nextOccurrenceDate** advanced to next period
7. **Check lastGeneratedDate** updated to today
8. **Test with end date** - verify auto-deactivation

---

## Error Responses

### 400 Bad Request - Missing Destination
```json
{
  "success": false,
  "message": "Destination account is required for transfers",
  "data": null,
  "timestamp": "2024-01-20T14:00:00"
}
```

### 400 Bad Request - Invalid Dates
```json
{
  "success": false,
  "message": "End date must be after start date",
  "data": null,
  "timestamp": "2024-01-20T14:00:00"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Recurring transaction not found",
  "data": null,
  "timestamp": "2024-01-20T14:00:00"
}
```

---

## Pro Tips

1. **Set Start Date Carefully**: First transaction generates on start date
2. **Use End Dates**: For limited subscriptions or temporary bills
3. **Pause Instead of Delete**: Use `isActive: false` to temporarily stop
4. **Check Next Occurrence**: Shows when next transaction will generate
5. **Review Generated Transactions**: Tagged with `isRecurring: true`
6. **Adjust Amounts**: Update amount if bill increases
7. **Use Intervals**: `intervalValue: 2` for bi-weekly, `3` for quarterly
8. **Monitor Last Generated**: Shows when it last created a transaction
9. **Auto-Deactivation**: Automatically stops when end date reached
10. **Bulk Setup**: Create all recurring expenses at once for complete automation

---

## Example: Complete Setup

```bash
# 1. Monthly salary (1st of month)
curl -X POST "http://localhost:8080/api/recurring-transactions" -H "Authorization: Bearer $JWT" -d '{
  "accountId": "'$CHECKING'", "type": "INCOME", "amount": 5000,
  "frequency": "MONTHLY", "intervalValue": 1, "startDate": "2024-01-01"
}'

# 2. Monthly rent (1st of month)
curl -X POST "http://localhost:8080/api/recurring-transactions" -H "Authorization: Bearer $JWT" -d '{
  "accountId": "'$CHECKING'", "type": "EXPENSE", "amount": 1500,
  "frequency": "MONTHLY", "intervalValue": 1, "startDate": "2024-01-01"
}'

# 3. Auto-save (10th of month)
curl -X POST "http://localhost:8080/api/recurring-transactions" -H "Authorization: Bearer $JWT" -d '{
  "accountId": "'$CHECKING'", "toAccountId": "'$SAVINGS'", "type": "TRANSFER",
  "amount": 500, "frequency": "MONTHLY", "intervalValue": 1, "startDate": "2024-01-10"
}'

# Get all recurring
curl "http://localhost:8080/api/recurring-transactions?activeOnly=true" -H "Authorization: Bearer $JWT"
```