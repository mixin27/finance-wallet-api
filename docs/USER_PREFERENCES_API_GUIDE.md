# User Preferences API - Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token (from login)
- User account created

---

## 1. Get User Preferences

First time calling this endpoint will create default preferences automatically.

```bash
curl -X GET "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK) - First Time:**
```json
{
  "success": true,
  "message": "User preferences retrieved successfully",
  "data": {
    "userId": "user-uuid",
    "defaultCurrencyId": "usd-currency-uuid",
    "defaultCurrencyCode": "USD",
    "defaultCurrencySymbol": "$",
    "language": "en",
    "dateFormat": "DD/MM/YYYY",
    "firstDayOfWeek": 1,
    "theme": "SYSTEM",
    "enableNotifications": true,
    "enableBiometric": false,
    "autoBackup": false,
    "createdAt": "2024-01-20T10:00:00",
    "updatedAt": "2024-01-20T10:00:00"
  },
  "timestamp": "2024-01-20T10:00:00"
}
```

---

## 2. Update User Preferences

### Update Default Currency
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultCurrencyId": "eur-currency-uuid"
  }'
```

### Update Language
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "language": "es"
  }'
```

### Update Date Format
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateFormat": "MM/DD/YYYY"
  }'
```

### Update First Day of Week
```bash
# 0 = Sunday, 1 = Monday, 6 = Saturday
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstDayOfWeek": 0
  }'
```

### Update Theme
```bash
# Options: LIGHT, DARK, SYSTEM
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "DARK"
  }'
```

### Update Notification Settings
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "enableNotifications": false
  }'
```

### Update Biometric Setting
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "enableBiometric": true
  }'
```

### Update Auto-Backup Setting
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "autoBackup": true
  }'
```

### Update Multiple Settings at Once
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultCurrencyId": "eur-currency-uuid",
    "language": "fr",
    "dateFormat": "DD/MM/YYYY",
    "theme": "DARK",
    "enableNotifications": true,
    "enableBiometric": true
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "User preferences updated successfully",
  "data": {
    "userId": "user-uuid",
    "defaultCurrencyId": "eur-currency-uuid",
    "defaultCurrencyCode": "EUR",
    "defaultCurrencySymbol": "€",
    "language": "fr",
    "dateFormat": "DD/MM/YYYY",
    "firstDayOfWeek": 1,
    "theme": "DARK",
    "enableNotifications": true,
    "enableBiometric": true,
    "autoBackup": false,
    "createdAt": "2024-01-20T10:00:00",
    "updatedAt": "2024-01-20T15:30:00"
  },
  "timestamp": "2024-01-20T15:30:00"
}
```

---

## 3. Reset to Default Preferences

Resets all preferences back to default values.

```bash
curl -X POST "http://localhost:8080/api/user/preferences/reset" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "User preferences reset to default successfully",
  "data": {
    "userId": "user-uuid",
    "defaultCurrencyId": "usd-currency-uuid",
    "defaultCurrencyCode": "USD",
    "defaultCurrencySymbol": "$",
    "language": "en",
    "dateFormat": "DD/MM/YYYY",
    "firstDayOfWeek": 1,
    "theme": "SYSTEM",
    "enableNotifications": true,
    "enableBiometric": false,
    "autoBackup": false,
    "createdAt": "2024-01-20T16:00:00",
    "updatedAt": "2024-01-20T16:00:00"
  },
  "timestamp": "2024-01-20T16:00:00"
}
```

---

## Preference Options

### Language Codes
Common language codes (ISO 639-1):
- `en` - English
- `es` - Spanish
- `fr` - French
- `de` - German
- `it` - Italian
- `pt` - Portuguese
- `zh` - Chinese
- `ja` - Japanese
- `ko` - Korean
- `ar` - Arabic

With region (optional):
- `en-US` - English (United States)
- `en-GB` - English (United Kingdom)
- `es-ES` - Spanish (Spain)
- `es-MX` - Spanish (Mexico)
- `pt-BR` - Portuguese (Brazil)
- `zh-CN` - Chinese (Simplified)
- `zh-TW` - Chinese (Traditional)

### Date Format Options
- `DD/MM/YYYY` - Day first (Europe, most of world)
- `MM/DD/YYYY` - Month first (United States)
- `YYYY-MM-DD` - ISO format
- `DD.MM.YYYY` - Dot separator
- `DD-MM-YYYY` - Dash separator

### First Day of Week
- `0` - Sunday
- `1` - Monday (default, ISO standard)
- `2` - Tuesday
- `3` - Wednesday
- `4` - Thursday
- `5` - Friday
- `6` - Saturday

### Theme Options
- `LIGHT` - Light mode always
- `DARK` - Dark mode always
- `SYSTEM` - Follow system settings (default)

### Boolean Settings
- `enableNotifications` - Push/email notifications
- `enableBiometric` - Fingerprint/Face ID login
- `autoBackup` - Automatic data backup

---

## Use Cases

### 1. User Onboarding
```bash
# Set preferences during registration
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultCurrencyId": "'$CURRENCY_ID'",
    "language": "en-US",
    "dateFormat": "MM/DD/YYYY",
    "firstDayOfWeek": 0,
    "theme": "SYSTEM"
  }'
```

### 2. European User Setup
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultCurrencyId": "'$EUR_ID'",
    "language": "de",
    "dateFormat": "DD.MM.YYYY",
    "firstDayOfWeek": 1
  }'
```

### 3. Mobile App Configuration
```bash
# Enable biometric and notifications
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "enableBiometric": true,
    "enableNotifications": true,
    "autoBackup": true
  }'
```

### 4. Privacy-Focused User
```bash
# Disable all optional features
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "enableNotifications": false,
    "enableBiometric": false,
    "autoBackup": false
  }'
```

---

## Testing Workflow

1. **Get initial preferences** (auto-creates default)
```bash
curl "http://localhost:8080/api/user/preferences" -H "Authorization: Bearer $JWT"
```

2. **Update currency** to user's preferred currency
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" -H "Authorization: Bearer $JWT" \
  -d '{"defaultCurrencyId": "'$CURRENCY_ID'"}'
```

3. **Set language and locale**
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" -H "Authorization: Bearer $JWT" \
  -d '{"language": "en-US", "dateFormat": "MM/DD/YYYY"}'
```

4. **Configure theme**
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" -H "Authorization: Bearer $JWT" \
  -d '{"theme": "DARK"}'
```

5. **Enable features**
```bash
curl -X PUT "http://localhost:8080/api/user/preferences" -H "Authorization: Bearer $JWT" \
  -d '{"enableNotifications": true, "enableBiometric": true}'
```

6. **Verify changes**
```bash
curl "http://localhost:8080/api/user/preferences" -H "Authorization: Bearer $JWT"
```

7. **Test reset**
```bash
curl -X POST "http://localhost:8080/api/user/preferences/reset" \
  -H "Authorization: Bearer $JWT"
```

---

## Error Responses

### 400 Bad Request - Invalid Language Code
```json
{
  "success": false,
  "message": "Invalid language code format (e.g., en, en-US)",
  "data": null,
  "timestamp": "2024-01-20T15:00:00"
}
```

### 400 Bad Request - Invalid First Day
```json
{
  "success": false,
  "message": "First day of week must be between 0 (Sunday) and 6 (Saturday)",
  "data": null,
  "timestamp": "2024-01-20T15:00:00"
}
```

### 404 Not Found - Currency
```json
{
  "success": false,
  "message": "Currency not found",
  "data": null,
  "timestamp": "2024-01-20T15:00:00"
}
```

---

## Integration with Frontend

### React Example
```javascript
// Get preferences on app load
const getPreferences = async () => {
  const response = await fetch('/api/user/preferences', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const data = await response.json();
  
  // Apply theme
  document.body.classList.add(data.data.theme.toLowerCase());
  
  // Set language
  i18n.changeLanguage(data.data.language);
  
  // Configure date format
  setDateFormat(data.data.dateFormat);
};

// Update preferences
const updatePreferences = async (updates) => {
  await fetch('/api/user/preferences', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(updates)
  });
};
```

### Flutter Example
```dart
// Get preferences
Future<UserPreferences> getPreferences() async {
  final response = await http.get(
    Uri.parse('$baseUrl/user/preferences'),
    headers: {'Authorization': 'Bearer $token'},
  );
  return UserPreferences.fromJson(jsonDecode(response.body)['data']);
}

// Apply theme
void applyTheme(String theme) {
  if (theme == 'DARK') {
    ThemeMode.dark;
  } else if (theme == 'LIGHT') {
    ThemeMode.light;
  } else {
    ThemeMode.system;
  }
}
```

---

## Default Values

When preferences are first created:
- **Currency**: USD (or first available currency)
- **Language**: `en`
- **Date Format**: `DD/MM/YYYY`
- **First Day**: `1` (Monday)
- **Theme**: `SYSTEM`
- **Notifications**: `true`
- **Biometric**: `false`
- **Auto-Backup**: `false`

---

## Pro Tips

1. **Auto-Create**: Preferences are created automatically on first GET
2. **Partial Updates**: Only send fields you want to change
3. **Theme Sync**: Use SYSTEM to follow device settings
4. **Currency Impact**: Changing currency affects new transactions/budgets
5. **Language Codes**: Use ISO 639-1 format (2 letters) or with region
6. **Date Format**: Frontend should format dates according to preference
7. **First Day**: Affects calendar views and "this week" calculations
8. **Biometric**: Mobile apps should respect this setting
9. **Notifications**: Backend should check before sending notifications
10. **Reset Carefully**: Reset will lose all custom preferences

---

## Example: Complete Setup Flow

```bash
# 1. Register user
curl -X POST "http://localhost:8080/api/auth/register" -d '{...}'

# 2. Login
JWT=$(curl -X POST "http://localhost:8080/api/auth/login" -d '{...}' | jq -r '.data.accessToken')

# 3. Get currencies
CURRENCY=$(curl "http://localhost:8080/api/currencies" -H "Authorization: Bearer $JWT" | jq -r '.data[0].id')

# 4. Set preferences
curl -X PUT "http://localhost:8080/api/user/preferences" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultCurrencyId": "'$CURRENCY'",
    "language": "en-US",
    "dateFormat": "MM/DD/YYYY",
    "firstDayOfWeek": 0,
    "theme": "DARK",
    "enableNotifications": true,
    "enableBiometric": true
  }'

# 5. Verify
curl "http://localhost:8080/api/user/preferences" -H "Authorization: Bearer $JWT"
```