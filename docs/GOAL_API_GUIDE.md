# Goals Management API - Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token (from login)
- At least one currency available
- Optional: account to link goal to

---

## 1. Create Financial Goal

### Create Savings Goal
```bash
curl -X POST "http://localhost:8080/api/goals" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emergency Fund",
    "description": "Save 6 months of expenses",
    "targetAmount": 15000.00,
    "initialAmount": 2000.00,
    "targetDate": "2024-12-31",
    "currencyId": "your-currency-uuid",
    "color": "#4CAF50",
    "icon": "💰"
  }'
```

### Create Vacation Goal
```bash
curl -X POST "http://localhost:8080/api/goals" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Summer Vacation",
    "description": "Trip to Hawaii",
    "targetAmount": 5000.00,
    "targetDate": "2024-07-01",
    "currencyId": "your-currency-uuid",
    "color": "#FF9800",
    "icon": "✈️"
  }'
```

### Create Goal Linked to Account
```bash
curl -X POST "http://localhost:8080/api/goals" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Car",
    "description": "Down payment for Tesla Model 3",
    "targetAmount": 10000.00,
    "initialAmount": 1500.00,
    "targetDate": "2025-06-01",
    "currencyId": "your-currency-uuid",
    "accountId": "savings-account-uuid",
    "color": "#2196F3",
    "icon": "🚗"
  }'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Goal created successfully",
  "data": {
    "id": "goal-uuid",
    "name": "Emergency Fund",
    "description": "Save 6 months of expenses",
    "targetAmount": 15000.00,
    "currentAmount": 2000.00,
    "remaining": 13000.00,
    "percentageComplete": 13.33,
    "targetDate": "2024-12-31",
    "color": "#4CAF50",
    "icon": "💰",
    "isCompleted": false,
    "accountId": null,
    "accountName": null,
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

## 2. Get All Goals

### Get All Goals (Active + Completed)
```bash
curl -X GET "http://localhost:8080/api/goals" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Active Goals Only
```bash
curl -X GET "http://localhost:8080/api/goals?activeOnly=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Goals retrieved successfully",
  "data": [
    {
      "id": "goal-uuid-1",
      "name": "Emergency Fund",
      "description": "Save 6 months of expenses",
      "targetAmount": 15000.00,
      "currentAmount": 5000.00,
      "remaining": 10000.00,
      "percentageComplete": 33.33,
      "targetDate": "2024-12-31",
      "color": "#4CAF50",
      "icon": "💰",
      "isCompleted": false,
      "accountId": null,
      "accountName": null,
      "currencyId": "uuid",
      "currencyCode": "USD",
      "currencySymbol": "$",
      "createdAt": "2024-01-15T10:00:00",
      "updatedAt": "2024-01-20T14:30:00"
    },
    {
      "id": "goal-uuid-2",
      "name": "Summer Vacation",
      "description": "Trip to Hawaii",
      "targetAmount": 5000.00,
      "currentAmount": 5000.00,
      "remaining": 0.00,
      "percentageComplete": 100.00,
      "targetDate": "2024-07-01",
      "color": "#FF9800",
      "icon": "✈️",
      "isCompleted": true,
      "accountId": null,
      "accountName": null,
      "currencyId": "uuid",
      "currencyCode": "USD",
      "currencySymbol": "$",
      "createdAt": "2024-01-10T09:00:00",
      "updatedAt": "2024-05-01T16:00:00"
    }
  ],
  "timestamp": "2024-01-20T15:00:00"
}
```

---

## 3. Get Goal by ID

```bash
curl -X GET "http://localhost:8080/api/goals/{goalId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Goal retrieved successfully",
  "data": {
    "id": "goal-uuid",
    "name": "Emergency Fund",
    "description": "Save 6 months of expenses",
    "targetAmount": 15000.00,
    "currentAmount": 5000.00,
    "remaining": 10000.00,
    "percentageComplete": 33.33,
    "targetDate": "2024-12-31",
    "color": "#4CAF50",
    "icon": "💰",
    "isCompleted": false,
    "accountId": null,
    "accountName": null,
    "currencyId": "uuid",
    "currencyCode": "USD",
    "currencySymbol": "$",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-20T14:30:00"
  },
  "timestamp": "2024-01-20T15:30:00"
}
```

---

## 4. Update Goal Progress

Add money to goal progress (positive amount) or withdraw (negative amount).

### Add $500 to Goal
```bash
curl -X PATCH "http://localhost:8080/api/goals/{goalId}/progress" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00
  }'
```

### Withdraw $100 from Goal
```bash
curl -X PATCH "http://localhost:8080/api/goals/{goalId}/progress" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": -100.00
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Goal progress updated successfully",
  "data": {
    "id": "goal-uuid",
    "name": "Emergency Fund",
    "description": "Save 6 months of expenses",
    "targetAmount": 15000.00,
    "currentAmount": 5500.00,
    "remaining": 9500.00,
    "percentageComplete": 36.67,
    "targetDate": "2024-12-31",
    "color": "#4CAF50",
    "icon": "💰",
    "isCompleted": false,
    "accountId": null,
    "accountName": null,
    "currencyId": "uuid",
    "currencyCode": "USD",
    "currencySymbol": "$",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-20T16:00:00"
  },
  "timestamp": "2024-01-20T16:00:00"
}
```

---

## 5. Update Goal Details

```bash
curl -X PUT "http://localhost:8080/api/goals/{goalId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emergency Fund - 2024",
    "targetAmount": 20000.00,
    "targetDate": "2024-12-31",
    "color": "#2E7D32"
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Goal updated successfully",
  "data": {
    "id": "goal-uuid",
    "name": "Emergency Fund - 2024",
    "description": "Save 6 months of expenses",
    "targetAmount": 20000.00,
    "currentAmount": 5500.00,
    "remaining": 14500.00,
    "percentageComplete": 27.50,
    "targetDate": "2024-12-31",
    "color": "#2E7D32",
    "icon": "💰",
    "isCompleted": false,
    "accountId": null,
    "accountName": null,
    "currencyId": "uuid",
    "currencyCode": "USD",
    "currencySymbol": "$",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-20T16:30:00"
  },
  "timestamp": "2024-01-20T16:30:00"
}
```

---

## 6. Mark Goal as Completed

Manually mark a goal as completed (sets currentAmount = targetAmount).

```bash
curl -X PATCH "http://localhost:8080/api/goals/{goalId}/complete" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Goal marked as completed",
  "data": {
    "id": "goal-uuid",
    "name": "Summer Vacation",
    "description": "Trip to Hawaii",
    "targetAmount": 5000.00,
    "currentAmount": 5000.00,
    "remaining": 0.00,
    "percentageComplete": 100.00,
    "targetDate": "2024-07-01",
    "color": "#FF9800",
    "icon": "✈️",
    "isCompleted": true,
    "accountId": null,
    "accountName": null,
    "currencyId": "uuid",
    "currencyCode": "USD",
    "currencySymbol": "$",
    "createdAt": "2024-01-10T09:00:00",
    "updatedAt": "2024-05-01T16:00:00"
  },
  "timestamp": "2024-05-01T16:00:00"
}
```

---

## 7. Delete Goal

```bash
curl -X DELETE "http://localhost:8080/api/goals/{goalId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Goal deleted successfully",
  "data": null,
  "timestamp": "2024-01-20T17:00:00"
}
```

---

## Goal Completion

Goals are **automatically marked as completed** when:
- `currentAmount >= targetAmount`

This happens:
1. When creating a goal with `initialAmount >= targetAmount`
2. When updating progress and total reaches target
3. When manually marked as completed

---

## Error Responses

### 400 Bad Request - Negative Progress
```json
{
  "success": false,
  "message": "Goal progress cannot be negative",
  "data": null,
  "timestamp": "2024-01-20T15:00:00"
}
```

### 400 Bad Request - Update Completed Goal
```json
{
  "success": false,
  "message": "Cannot update progress for completed goal",
  "data": null,
  "timestamp": "2024-01-20T15:00:00"
}
```

### 400 Bad Request - Invalid Amount
```json
{
  "success": false,
  "message": "Target amount must be greater than zero",
  "data": null,
  "timestamp": "2024-01-20T15:00:00"
}
```

### 404 Not Found - Goal
```json
{
  "success": false,
  "message": "Goal not found with id: {goalId}",
  "data": null,
  "timestamp": "2024-01-20T15:00:00"
}
```

---

## Testing Workflow

1. **Create multiple goals** with different targets:
   ```bash
   # Emergency fund - long term
   # Vacation - short term
   # New gadget - small amount
   ```

2. **Add initial amounts** to some goals:
   ```bash
   # Start with some progress already made
   ```

3. **Update progress regularly**:
   ```bash
   # Add $100 to emergency fund
   # Add $500 to vacation
   ```

4. **Track progress**:
   ```bash
   # Get all goals
   # Check percentageComplete
   # Check remaining amount
   ```

5. **Complete a goal**:
   ```bash
   # Keep adding until currentAmount >= targetAmount
   # Goal automatically marked as completed
   ```

6. **Filter active goals**:
   ```bash
   # Use ?activeOnly=true to see only in-progress goals
   ```

---

## Common Goal Types

### 1. Emergency Fund
```json
{
  "name": "Emergency Fund",
  "targetAmount": 15000.00,
  "targetDate": "2024-12-31",
  "icon": "💰",
  "color": "#4CAF50"
}
```

### 2. Vacation/Travel
```json
{
  "name": "Europe Trip",
  "targetAmount": 5000.00,
  "targetDate": "2024-08-15",
  "icon": "✈️",
  "color": "#FF9800"
}
```

### 3. Major Purchase
```json
{
  "name": "New Laptop",
  "targetAmount": 2000.00,
  "targetDate": "2024-06-01",
  "icon": "💻",
  "color": "#2196F3"
}
```

### 4. Home Down Payment
```json
{
  "name": "House Down Payment",
  "targetAmount": 50000.00,
  "targetDate": "2025-12-31",
  "icon": "🏠",
  "color": "#795548"
}
```

### 5. Education Fund
```json
{
  "name": "Master's Degree",
  "targetAmount": 30000.00,
  "targetDate": "2026-09-01",
  "icon": "🎓",
  "color": "#673AB7"
}
```

---

## Pro Tips

1. **Link to Savings Account**: Create goals linked to specific savings accounts
2. **Regular Updates**: Update progress monthly or per paycheck
3. **Realistic Targets**: Set achievable targets with reasonable timelines
4. **Multiple Goals**: Have 3-5 active goals at once (short + long term)
5. **Progress Tracking**: Use percentageComplete for progress bars in UI
6. **Visual Indicators**: Use different colors/icons for different goal types
7. **Target Dates**: Optional but helps with motivation
8. **Initial Amount**: Start with current savings if transferring to goal
9. **Auto-Complete**: Goals complete automatically when target reached
10. **Celebrate**: When goal completes, create new goal to maintain momentum!

---

## Example: Complete Goal Flow

```bash
# 1. Create goal
GOAL_ID=$(curl -X POST "http://localhost:8080/api/goals" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nintendo Switch",
    "targetAmount": 350.00,
    "currencyId": "'$CURRENCY_ID'",
    "icon": "🎮",
    "color": "#E91E63"
  }' | jq -r '.data.id')

# 2. Add weekly progress ($50/week)
curl -X PATCH "http://localhost:8080/api/goals/$GOAL_ID/progress" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"amount": 50.00}'

# 3. Check progress (repeat weekly)
curl "http://localhost:8080/api/goals/$GOAL_ID" \
  -H "Authorization: Bearer $JWT"

# 4. Goal auto-completes after 7 weeks!
# 5. Get all active goals (Switch will be filtered out)
curl "http://localhost:8080/api/goals?activeOnly=true" \
  -H "Authorization: Bearer $JWT"
```