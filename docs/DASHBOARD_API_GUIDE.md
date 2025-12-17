# Dashboard & Statistics API - Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token (from login)
- Some transactions created (income and expenses)
- At least one account with balance

---

## 1. Get Dashboard Overview

Shows current month's income, expenses, savings, and trends.

```bash
curl -X GET "http://localhost:8080/api/dashboard" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Dashboard data retrieved successfully",
  "data": {
    "totalBalance": 5420.50,
    "monthIncome": 4500.00,
    "monthExpenses": 2150.75,
    "savings": 2349.25,
    "incomeChange": 12.50,
    "expenseChange": -8.30,
    "categoryBreakdown": [
      {
        "categoryId": "uuid",
        "categoryName": "Food & Dining",
        "amount": 750.50,
        "color": "#FF5722",
        "icon": "🍽️"
      },
      {
        "categoryId": "uuid",
        "categoryName": "Transportation",
        "amount": 450.00,
        "color": "#3F51B5",
        "icon": "🚗"
      },
      {
        "categoryId": "uuid",
        "categoryName": "Shopping",
        "amount": 350.25,
        "color": "#E91E63",
        "icon": "🛍️"
      }
    ],
    "recentTransactionsCount": 10,
    "activeBudgetsCount": 3,
    "currentMonth": "2024-01"
  },
  "timestamp": "2024-01-15T16:00:00"
}
```

**Field Descriptions:**
- `totalBalance`: Sum of all account balances (only accounts with `isIncludedInTotal: true`)
- `monthIncome`: Total income for current month
- `monthExpenses`: Total expenses for current month
- `savings`: Income - Expenses
- `incomeChange`: Percentage change from last month (positive = increased)
- `expenseChange`: Percentage change from last month (negative = decreased)
- `categoryBreakdown`: Top spending categories sorted by amount

---

## 2. Get Statistics for Date Range

Get detailed statistics for any custom date range.

```bash
curl -X GET "http://localhost:8080/api/dashboard/statistics?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "data": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "totalIncome": 4500.00,
    "totalExpenses": 2150.75,
    "netIncome": 2349.25,
    "avgDailyIncome": 145.16,
    "avgDailyExpense": 69.38,
    "expensesByCategory": [
      {
        "categoryId": "uuid",
        "categoryName": "Food & Dining",
        "amount": 750.50,
        "color": "#FF5722",
        "icon": "🍽️"
      },
      {
        "categoryId": "uuid",
        "categoryName": "Transportation",
        "amount": 450.00,
        "color": "#3F51B5",
        "icon": "🚗"
      }
    ],
    "incomeByCategory": [
      {
        "categoryId": "uuid",
        "categoryName": "Salary",
        "amount": 4000.00,
        "color": "#4CAF50",
        "icon": "💼"
      },
      {
        "categoryId": "uuid",
        "categoryName": "Freelance",
        "amount": 500.00,
        "color": "#2196F3",
        "icon": "💻"
      }
    ],
    "dailyTrends": [
      {
        "date": "2024-01-01",
        "income": 0.00,
        "expenses": 50.00,
        "net": -50.00
      },
      {
        "date": "2024-01-02",
        "income": 0.00,
        "expenses": 125.50,
        "net": -125.50
      },
      {
        "date": "2024-01-05",
        "income": 4000.00,
        "expenses": 0.00,
        "net": 4000.00
      }
    ]
  },
  "timestamp": "2024-01-15T16:30:00"
}
```

**Field Descriptions:**
- `totalIncome`: Total income in date range
- `totalExpenses`: Total expenses in date range
- `netIncome`: Income - Expenses (can be negative)
- `avgDailyIncome`: Average income per day
- `avgDailyExpense`: Average expense per day
- `expensesByCategory`: All expense categories sorted by amount
- `incomeByCategory`: All income categories sorted by amount
- `dailyTrends`: Day-by-day breakdown (useful for charts)

---

## 3. Get This Month's Statistics

Quick shortcut for current month statistics.

```bash
curl -X GET "http://localhost:8080/api/dashboard/statistics/this-month" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** Same format as custom date range, but automatically uses current month dates.

---

## 4. Get Last Month's Statistics

Compare with previous month performance.

```bash
curl -X GET "http://localhost:8080/api/dashboard/statistics/last-month" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** Same format, but for last month.

---

## 5. Get This Year's Statistics

Annual overview of income and expenses.

```bash
curl -X GET "http://localhost:8080/api/dashboard/statistics/this-year" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** Same format, but for entire current year (Jan 1 - Dec 31).

---

## Use Cases

### 1. Monthly Budget Review
```bash
# Compare this month vs last month
curl "http://localhost:8080/api/dashboard/statistics/this-month" -H "Authorization: Bearer $JWT"
curl "http://localhost:8080/api/dashboard/statistics/last-month" -H "Authorization: Bearer $JWT"
```

### 2. Quarterly Analysis
```bash
# Q1 2024
curl "http://localhost:8080/api/dashboard/statistics?startDate=2024-01-01&endDate=2024-03-31" \
  -H "Authorization: Bearer $JWT"
```

### 3. Year-over-Year Comparison
```bash
# 2024 vs 2023
curl "http://localhost:8080/api/dashboard/statistics?startDate=2024-01-01&endDate=2024-12-31" -H "Authorization: Bearer $JWT"
curl "http://localhost:8080/api/dashboard/statistics?startDate=2023-01-01&endDate=2023-12-31" -H "Authorization: Bearer $JWT"
```

### 4. Spending Patterns
```bash
# Last 7 days
WEEK_AGO=$(date -d "7 days ago" +%Y-%m-%d)
TODAY=$(date +%Y-%m-%d)

curl "http://localhost:8080/api/dashboard/statistics?startDate=$WEEK_AGO&endDate=$TODAY" \
  -H "Authorization: Bearer $JWT"
```

---

## Chart Data Usage

### Category Pie Chart
Use `expensesByCategory` or `incomeByCategory`:
```javascript
const pieChartData = response.data.expensesByCategory.map(cat => ({
  label: cat.categoryName,
  value: cat.amount,
  color: cat.color
}));
```

### Income vs Expense Bar Chart
Use `dailyTrends`:
```javascript
const barChartData = response.data.dailyTrends.map(day => ({
  date: day.date,
  income: day.income,
  expenses: day.expenses
}));
```

### Net Income Line Chart
Use `dailyTrends`:
```javascript
const lineChartData = response.data.dailyTrends.map(day => ({
  date: day.date,
  net: day.net
}));
```

---

## Testing Workflow

1. **Create test data:**
    - Create 2-3 accounts
    - Add various income transactions (salary, freelance)
    - Add expense transactions in different categories
    - Spread transactions across multiple days

2. **Test dashboard:**
   ```bash
   curl "http://localhost:8080/api/dashboard" -H "Authorization: Bearer $JWT"
   ```

3. **Check month comparison:**
    - Note the `incomeChange` and `expenseChange` percentages
    - These compare current month to previous month

4. **Analyze spending by category:**
    - Look at `categoryBreakdown` in dashboard
    - See which categories have highest spending

5. **Get detailed statistics:**
   ```bash
   curl "http://localhost:8080/api/dashboard/statistics/this-month" -H "Authorization: Bearer $JWT"
   ```

6. **Review daily trends:**
    - Check `dailyTrends` array
    - Useful for seeing spending patterns by day

7. **Custom date range:**
   ```bash
   curl "http://localhost:8080/api/dashboard/statistics?startDate=2024-01-01&endDate=2024-01-15" \
     -H "Authorization: Bearer $JWT"
   ```

---

## Insights You Can Get

### Dashboard Overview (`/dashboard`)
- ✅ Current total balance
- ✅ This month's income & expenses
- ✅ Savings (income - expenses)
- ✅ Percentage change from last month
- ✅ Top spending categories
- ✅ Recent activity count
- ✅ Active budget count

### Statistics (`/dashboard/statistics`)
- ✅ Total income & expenses for any period
- ✅ Net income (profit/loss)
- ✅ Average daily income & expense
- ✅ Complete category breakdowns
- ✅ Day-by-day trends for charts
- ✅ Both income and expense categories

---

## Error Responses

### 400 Bad Request - Invalid Date Format
```json
{
  "success": false,
  "message": "Invalid date format. Use YYYY-MM-DD",
  "data": null,
  "timestamp": "2024-01-15T16:00:00"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null,
  "timestamp": "2024-01-15T16:00:00"
}
```

---

## Pro Tips

1. **Dashboard First**: Always load `/dashboard` for overview before diving into details
2. **Date Ranges**: Use shortcuts (`this-month`, `last-month`, `this-year`) when possible
3. **Daily Trends**: Perfect for line/bar charts in your frontend
4. **Category Breakdown**: Use for pie/donut charts to show spending distribution
5. **Percentage Changes**: Green/red indicators based on positive/negative changes
6. **Zero Values**: Categories with $0 won't appear in breakdown
7. **Performance**: Statistics endpoint can be slow for large date ranges
8. **Caching**: Consider caching dashboard data for 5-10 minutes on frontend
9. **Comparison**: Fetch multiple periods and compare trends
10. **Mobile**: Dashboard endpoint perfect for mobile home screen widget