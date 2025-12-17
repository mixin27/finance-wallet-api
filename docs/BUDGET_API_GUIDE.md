# Budget Management API - Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token (from login)
- At least one category created
- At least one currency available
- Set token in Authorization header: `Bearer YOUR_JWT_TOKEN`

---

## 1. Create Budget

### Create Monthly Budget for All Expenses
```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Monthly Expenses Budget",
    "amount": 3000.00,
    "period": "MONTHLY",
    "startDate": "2024-01-01",
    "currencyId": "your-currency-uuid",
    "alertThreshold": 80
  }'
```

### Create Budget for Specific Category (e.g., Food)
```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food Budget",
    "amount": 500.00,
    "period": "MONTHLY",
    "startDate": "2024-01-01",
    "currencyId": "your-currency-uuid",
    "categoryId": "food-category-uuid",
    "alertThreshold": 90
  }'
```

### Create Weekly Budget
```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Weekly Groceries",
    "amount": 150.00,
    "period": "WEEKLY",
    "startDate": "2024-01-01",
    "currencyId": "your-currency-uuid",
    "categoryId": "food-category-uuid"
  }'
```

### Create Custom Period Budget
```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vacation Budget",
    "amount": 2000.00,
    "period": "CUSTOM",
    "startDate": "2024-06-01",
    "endDate": "2024-06-30",
    "currencyId": "your-currency-uuid"
  }'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Budget created successfully",
  "data": {
    "id": "uuid",
    "name": "Monthly Expenses Budget",
    "amount": 3000.00,
    "period": "MONTHLY",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "alertThreshold": 80.00,
    "isActive": true,
    "categoryId": null,
    "categoryName": null,
    "currencyId": "uuid",
    "currencyCode": "USD",
    "currencySymbol": "$",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  },
  "timestamp": "2024-01-15T10:00:00"
}
```

---

## 2. Get All Budgets

### Get All Budgets
```bash
curl -X GET "http://localhost:8080/api/budgets" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Active Budgets Only
```bash
curl -X GET "http://localhost:8080/api/budgets?activeOnly=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Budgets retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Monthly Expenses Budget",
      "amount": 3000.00,
      "period": "MONTHLY",
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "alertThreshold": 80.00,
      "isActive": true,
      "categoryId": null,
      "categoryName": null,
      "currencyId": "uuid",
      "currencyCode": "USD",
      "currencySymbol": "$",
      "createdAt": "2024-01-15T10:00:00",
      "updatedAt": "2024-01-15T10:00:00"
    },
    {
      "id": "uuid",
      "name": "Food Budget",
      "amount": 500.00,
      "period": "MONTHLY",
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "alertThreshold": 90.00,
      "isActive": true,
      "categoryId": "uuid",
      "categoryName": "Food & Dining",
      "currencyId": "uuid",
      "currencyCode": "USD",
      "currencySymbol": "$",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "timestamp": "2024-01-15T11:00:00"
}
```

---

## 3. Get Active Budgets with Progress

This endpoint shows real-time budget tracking with spent amounts and percentages.

```bash
curl -X GET "http://localhost:8080/api/budgets/active" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Active budgets with progress retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Monthly Expenses Budget",
      "amount": 3000.00,
      "spent": 2100.50,
      "remaining": 899.50,
      "percentageUsed": 70.02,
      "period": "MONTHLY",
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "alertThreshold": 80.00,
      "isOverBudget": false,
      "isNearLimit": false,
      "categoryName": null,
      "currencyCode": "USD",
      "currencySymbol": "$"
    },
    {
      "id": "uuid",
      "name": "Food Budget",
      "amount": 500.00,
      "spent": 425.75,
      "remaining": 74.25,
      "percentageUsed": 85.15,
      "period": "MONTHLY",
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "alertThreshold": 90.00,
      "isOverBudget": false,
      "isNearLimit": false,
      "categoryName": "Food & Dining",
      "currencyCode": "USD",
      "currencySymbol": "$"
    },
    {
      "id": "uuid",
      "name": "Shopping Budget",
      "amount": 300.00,
      "spent": 350.00,
      "remaining": -50.00,
      "percentageUsed": 116.67,
      "period": "MONTHLY",
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "alertThreshold": 80.00,
      "isOverBudget": true,
      "isNearLimit": true,
      "categoryName": "Shopping",
      "currencyCode": "USD",
      "currencySymbol": "$"
    }
  ],
  "timestamp": "2024-01-15T11:30:00"
}
```

**Field Descriptions:**
- `spent`: Total amount spent in this budget period
- `remaining`: Amount left (can be negative if over budget)
- `percentageUsed`: Percentage of budget used
- `isOverBudget`: True if spent > amount
- `isNearLimit`: True if percentageUsed >= alertThreshold

---

## 4. Get Budget by ID

```bash
curl -X GET "http://localhost:8080/api/budgets/{budgetId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Budget retrieved successfully",
  "data": {
    "id": "uuid",
    "name": "Food Budget",
    "amount": 500.00,
    "period": "MONTHLY",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "alertThreshold": 90.00,
    "isActive": true,
    "categoryId": "uuid",
    "categoryName": "Food & Dining",
    "currencyId": "uuid",
    "currencyCode": "USD",
    "currencySymbol": "$",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T12:00:00"
}
```

---

## 5. Update Budget

```bash
curl -X PUT "http://localhost:8080/api/budgets/{budgetId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food & Groceries Budget",
    "amount": 600.00,
    "alertThreshold": 85
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Budget updated successfully",
  "data": {
    "id": "uuid",
    "name": "Food & Groceries Budget",
    "amount": 600.00,
    "period": "MONTHLY",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "alertThreshold": 85.00,
    "isActive": true,
    "categoryId": "uuid",
    "categoryName": "Food & Dining",
    "currencyId": "uuid",
    "currencyCode": "USD",
    "currencySymbol": "$",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T12:30:00"
  },
  "timestamp": "2024-01-15T12:30:00"
}
```

---

## 6. Delete Budget (Soft Delete)

```bash
curl -X DELETE "http://localhost:8080/api/budgets/{budgetId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Budget deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T13:00:00"
}
```

---

## Budget Periods

### Available Periods
- `DAILY`: Budget for one day
- `WEEKLY`: Budget for one week (7 days)
- `MONTHLY`: Budget for the entire month
- `YEARLY`: Budget for the entire year
- `CUSTOM`: Custom date range (must provide endDate)

### End Date Auto-Calculation
- **DAILY**: Same as start date
- **WEEKLY**: Start date + 6 days
- **MONTHLY**: Last day of the month
- **YEARLY**: Last day of the year (Dec 31)
- **CUSTOM**: Must be provided manually

---

## Testing Workflow

1. **Login** to get JWT token
2. **Get all categories** to get category IDs
3. **Get all currencies** to get currency IDs
4. **Create monthly budget** for all expenses
5. **Create category-specific budgets** (Food, Transport, etc.)
6. **Create some expense transactions** in those categories
7. **Get active budgets with progress** to see real-time tracking
8. **Check if near limit** (percentageUsed >= alertThreshold)
9. **Check if over budget** (spent > amount)
10. **Update budget** to increase amount
11. **Delete budget** when no longer needed

---

## Error Responses

### 400 Bad Request - Invalid Amount
```json
{
  "success": false,
  "message": "Budget amount must be greater than zero",
  "data": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

### 400 Bad Request - Invalid Dates
```json
{
  "success": false,
  "message": "End date must be after start date",
  "data": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

### 404 Not Found - Category
```json
{
  "success": false,
  "message": "Category not found",
  "data": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

### 404 Not Found - Budget
```json
{
  "success": false,
  "message": "Budget not found with id: {budgetId}",
  "data": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

---

## Pro Tips

1. **Alert Threshold**: Set to 80-90% to get warnings before overspending
2. **Category Budgets**: Create specific budgets for problem categories (dining out, shopping)
3. **Global Budget**: Create one budget without category to track total expenses
4. **Multiple Periods**: You can have daily, weekly, and monthly budgets simultaneously
5. **Budget Progress**: Check `/budgets/active` regularly to stay on track
6. **Over Budget**: `isOverBudget: true` means you've exceeded your limit
7. **Near Limit**: `isNearLimit: true` means you're approaching your alert threshold