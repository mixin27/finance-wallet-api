# Category Management API - Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token (from login)
- Set token in Authorization header: `Bearer YOUR_JWT_TOKEN`

---

## 1. Get All Categories

### Get All Categories (System + Custom)
```bash
curl -X GET "http://localhost:8080/api/categories" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Income Categories Only
```bash
curl -X GET "http://localhost:8080/api/categories?type=INCOME" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Expense Categories Only
```bash
curl -X GET "http://localhost:8080/api/categories?type=EXPENSE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Salary",
      "type": "INCOME",
      "color": "#4CAF50",
      "icon": "💼",
      "displayOrder": 1,
      "isSystem": true,
      "isActive": true,
      "parentCategoryId": null,
      "parentCategoryName": null,
      "subCategories": [],
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    },
    {
      "id": "uuid",
      "name": "Food & Dining",
      "type": "EXPENSE",
      "color": "#FF5722",
      "icon": "🍽️",
      "displayOrder": 1,
      "isSystem": true,
      "isActive": true,
      "parentCategoryId": null,
      "parentCategoryName": null,
      "subCategories": [],
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 2. Get Category by ID

```bash
curl -X GET "http://localhost:8080/api/categories/{categoryId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Category retrieved successfully",
  "data": {
    "id": "uuid",
    "name": "Food & Dining",
    "type": "EXPENSE",
    "color": "#FF5722",
    "icon": "🍽️",
    "displayOrder": 1,
    "isSystem": true,
    "isActive": true,
    "parentCategoryId": null,
    "parentCategoryName": null,
    "subCategories": [
      {
        "id": "uuid",
        "name": "Restaurant",
        "type": "EXPENSE",
        "color": "#FF6B6B",
        "icon": "🍴",
        "displayOrder": 1,
        "isSystem": false,
        "isActive": true,
        "parentCategoryId": "parent-uuid",
        "parentCategoryName": "Food & Dining",
        "subCategories": [],
        "createdAt": "2024-01-15T11:00:00",
        "updatedAt": "2024-01-15T11:00:00"
      }
    ],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T12:00:00"
}
```

---

## 3. Create Custom Category

### Create Top-Level Category
```bash
curl -X POST "http://localhost:8080/api/categories" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Freelance Projects",
    "type": "INCOME",
    "color": "#2196F3",
    "icon": "💼"
  }'
```

### Create Subcategory
```bash
curl -X POST "http://localhost:8080/api/categories" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fast Food",
    "type": "EXPENSE",
    "color": "#FF8A65",
    "icon": "🍔",
    "parentCategoryId": "parent-category-uuid"
  }'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Category created successfully",
  "data": {
    "id": "new-uuid",
    "name": "Freelance Projects",
    "type": "INCOME",
    "color": "#2196F3",
    "icon": "💼",
    "displayOrder": 6,
    "isSystem": false,
    "isActive": true,
    "parentCategoryId": null,
    "parentCategoryName": null,
    "subCategories": [],
    "createdAt": "2024-01-15T12:00:00",
    "updatedAt": "2024-01-15T12:00:00"
  },
  "timestamp": "2024-01-15T12:00:00"
}
```

---

## 4. Update Custom Category

```bash
curl -X PUT "http://localhost:8080/api/categories/{categoryId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Freelance & Consulting",
    "color": "#1976D2",
    "icon": "💻",
    "displayOrder": 2
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Category updated successfully",
  "data": {
    "id": "uuid",
    "name": "Freelance & Consulting",
    "type": "INCOME",
    "color": "#1976D2",
    "icon": "💻",
    "displayOrder": 2,
    "isSystem": false,
    "isActive": true,
    "parentCategoryId": null,
    "parentCategoryName": null,
    "subCategories": [],
    "createdAt": "2024-01-15T12:00:00",
    "updatedAt": "2024-01-15T12:30:00"
  },
  "timestamp": "2024-01-15T12:30:00"
}
```

---

## 5. Delete Custom Category (Soft Delete)

```bash
curl -X DELETE "http://localhost:8080/api/categories/{categoryId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Category deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T13:00:00"
}
```

---

## Error Responses

### 400 Bad Request - Invalid Data
```json
{
  "success": false,
  "message": "Invalid color format. Use hex color code (e.g., #FF5722)",
  "data": null,
  "timestamp": "2024-01-15T12:00:00"
}
```

### 400 Bad Request - Cannot Update System Category
```json
{
  "success": false,
  "message": "Cannot update system categories",
  "data": null,
  "timestamp": "2024-01-15T12:00:00"
}
```

### 400 Bad Request - Category Has Subcategories
```json
{
  "success": false,
  "message": "Cannot delete category with subcategories. Delete subcategories first.",
  "data": null,
  "timestamp": "2024-01-15T12:00:00"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Category not found with id: {categoryId}",
  "data": null,
  "timestamp": "2024-01-15T12:00:00"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null,
  "timestamp": "2024-01-15T12:00:00"
}
```

---

## Default System Categories

### Income Categories (5)
1. 💼 Salary - #4CAF50
2. 💻 Freelance - #2196F3
3. 📈 Investment - #FF9800
4. 🎁 Gift - #E91E63
5. 💰 Other Income - #9C27B0

### Expense Categories (10)
1. 🍽️ Food & Dining - #FF5722
2. 🚗 Transportation - #3F51B5
3. 🛍️ Shopping - #E91E63
4. 🎬 Entertainment - #9C27B0
5. 📄 Bills & Utilities - #FF9800
6. 🏥 Healthcare - #F44336
7. 📚 Education - #2196F3
8. 🏠 Housing - #795548
9. 💇 Personal Care - #00BCD4
10. 💸 Other Expense - #607D8B

---

## Testing Workflow

1. **Login** to get JWT token
2. **Get all categories** to see system categories
3. **Create custom category** (e.g., "Gym" under "Personal Care")
4. **Create subcategory** under existing category
5. **Update category** (change name, color, icon)
6. **Get category by ID** to verify changes
7. **Delete category** (soft delete)
8. **Verify deletion** by getting all categories (should not appear)

---

## Notes

- System categories **cannot be updated or deleted**
- Custom categories can be updated and deleted (soft delete)
- Parent and child categories must have the **same type** (INCOME/EXPENSE)
- Cannot delete a category that has subcategories
- Deleted categories are marked as `isActive: false` (soft delete)
- Categories are automatically initialized on application startup