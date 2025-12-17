# Finance Wallet API - Testing Guide

## Prerequisites

1. Spring Boot app running on `http://localhost:8080`
2. PostgreSQL database with schema created
3. Postman or any REST client (or use cURL)

---

## Base URL
```
http://localhost:8080/api
```

---

## 1. Health Check (No Auth Required)

### Request
```bash
curl -X GET http://localhost:8080/api/auth/health
```

### Expected Response
```json
{
  "status": "UP",
  "service": "Finance Wallet API",
  "timestamp": "1702847234567"
}
```

---

## 2. Get All Currencies (No Auth Required)

### Request
```bash
curl -X GET http://localhost:8080/api/currencies
```

### Expected Response
```json
{
  "success": true,
  "message": "Currencies retrieved successfully",
  "data": [
    {
      "id": "uuid-here",
      "code": "USD",
      "name": "US Dollar",
      "symbol": "$",
      "decimalPlaces": 2,
      "isActive": true
    },
    ...
  ],
  "timestamp": 1702847234567
}
```

---

## 3. Register New User

### Request
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "username": "johndoe",
    "password": "password123",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890"
  }'
```

### Expected Response (201 Created)
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "uuid-here",
      "email": "john.doe@example.com",
      "username": "johndoe",
      "fullName": "John Doe",
      "phoneNumber": "+1234567890",
      "profileImageUrl": null,
      "isEmailVerified": false,
      "authProvider": "LOCAL",
      "createdAt": "2024-12-15T10:30:00",
      "lastLoginAt": null
    }
  },
  "timestamp": 1702847234567
}
```

### Error Response - Email Already Exists (400)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Email is already taken",
  "path": "/api/auth/register",
  "timestamp": 1702847234567
}
```

### Error Response - Validation Failed (400)
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "path": "/api/auth/register",
  "validationErrors": {
    "email": "Email should be valid",
    "password": "Password must be between 6 and 100 characters"
  },
  "timestamp": 1702847234567
}
```

---

## 4. Login

### Request
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

### Expected Response (200 OK)
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "uuid-here",
      "email": "john.doe@example.com",
      "username": "johndoe",
      "fullName": "John Doe",
      "phoneNumber": "+1234567890",
      "profileImageUrl": null,
      "isEmailVerified": false,
      "authProvider": "LOCAL",
      "createdAt": "2024-12-15T10:30:00",
      "lastLoginAt": "2024-12-15T11:00:00"
    }
  },
  "timestamp": 1702847234567
}
```

### Error Response - Invalid Credentials (401)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/auth/login",
  "timestamp": 1702847234567
}
```

---

## 5. OAuth Login (Google/Apple)

### Request
```bash
curl -X POST http://localhost:8080/api/auth/oauth/login \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "GOOGLE",
    "providerId": "google-user-id-12345",
    "email": "jane.smith@gmail.com",
    "fullName": "Jane Smith",
    "profileImageUrl": "https://example.com/photo.jpg"
  }'
```

### Expected Response (200 OK)
Same structure as login response.

---

## 6. Get Current User (Requires Auth)

### Request
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### Expected Response (200 OK)
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": "uuid-here",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890",
    "profileImageUrl": null,
    "isEmailVerified": false,
    "authProvider": "LOCAL",
    "createdAt": "2024-12-15T10:30:00",
    "lastLoginAt": "2024-12-15T11:00:00"
  },
  "timestamp": 1702847234567
}
```

### Error Response - No Token (401)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "You need to login to access this resource",
  "path": "/api/auth/me"
}
```

---

## 7. Refresh Token

### Request
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

### Expected Response (200 OK)
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "NEW_ACCESS_TOKEN",
    "refreshToken": "NEW_REFRESH_TOKEN",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": { ... }
  },
  "timestamp": 1702847234567
}
```

---

## 8. Change Password (Requires Auth)

### Request
```bash
curl -X PUT http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "password123",
    "newPassword": "newpassword456"
  }'
```

### Expected Response (200 OK)
```json
{
  "success": true,
  "message": "Password changed successfully. Please login again.",
  "timestamp": 1702847234567
}
```

---

## 9. Logout

### Request
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

### Expected Response (200 OK)
```json
{
  "success": true,
  "message": "Logout successful",
  "timestamp": 1702847234567
}
```

---

## Postman Collection

### Import this collection into Postman:

```json
{
  "info": {
    "name": "Finance Wallet API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Register",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"test@example.com\",\n  \"username\": \"testuser\",\n  \"password\": \"password123\",\n  \"fullName\": \"Test User\"\n}"
            },
            "url": {"raw": "{{baseUrl}}/auth/register"}
          }
        },
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"test@example.com\",\n  \"password\": \"password123\"\n}"
            },
            "url": {"raw": "{{baseUrl}}/auth/login"}
          }
        },
        {
          "name": "Get Current User",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{accessToken}}"}],
            "url": {"raw": "{{baseUrl}}/auth/me"}
          }
        }
      ]
    }
  ],
  "variable": [
    {"key": "baseUrl", "value": "http://localhost:8080/api"}
  ]
}
```

---

## Swagger UI

Access interactive API documentation at:
```
http://localhost:8080/api/swagger-ui.html
```

You can test all endpoints directly from the browser!

---

## Testing Workflow

### 1. **First Time Setup**
```bash
# 1. Check health
curl http://localhost:8080/api/auth/health

# 2. Get currencies (for registration)
curl http://localhost:8080/api/currencies

# 3. Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","username":"testuser","password":"test123","fullName":"Test User"}'

# Save the accessToken from response
```

### 2. **Daily Testing**
```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'

# 2. Use accessToken for authenticated requests
export TOKEN="your-access-token-here"

curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Common Issues & Solutions

### Issue: 401 Unauthorized
**Solution**: Check if you included the Authorization header with Bearer token

### Issue: 403 Forbidden
**Solution**: Token might be expired, use refresh token endpoint

### Issue: 400 Bad Request
**Solution**: Check request body format and validation errors in response

### Issue: Database connection error
**Solution**: Check PostgreSQL is running and credentials in application.properties

---

## Next Steps

After authentication is working:
1. Test all endpoints in Postman
2. Verify tokens work correctly
3. Test OAuth flow (when ready)
4. Move to Accounts API implementation
5. Start Flutter integration