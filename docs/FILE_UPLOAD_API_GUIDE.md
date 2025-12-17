# File Upload API - Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token (from login)
- At least one transaction created
- Test image/PDF files ready for upload

---

## 1. Upload Attachment to Transaction

### Upload Receipt Image
```bash
curl -X POST "http://localhost:8080/api/transactions/{transactionId}/attachments" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/receipt.jpg"
```

### Upload PDF Receipt
```bash
curl -X POST "http://localhost:8080/api/transactions/{transactionId}/attachments" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/invoice.pdf"
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "id": "attachment-uuid",
    "fileName": "uuid_timestamp.jpg",
    "originalFileName": "receipt.jpg",
    "fileType": "image/jpeg",
    "fileSize": 245678,
    "fileUrl": "/uploads/transactions/uuid_timestamp.jpg",
    "thumbnailUrl": null,
    "createdAt": "2024-01-15T14:30:00"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

## 2. Get All Attachments for Transaction

```bash
curl -X GET "http://localhost:8080/api/transactions/{transactionId}/attachments" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Attachments retrieved successfully",
  "data": [
    {
      "id": "attachment-uuid-1",
      "fileName": "uuid_timestamp_1.jpg",
      "originalFileName": "receipt1.jpg",
      "fileType": "image/jpeg",
      "fileSize": 245678,
      "fileUrl": "/uploads/transactions/uuid_timestamp_1.jpg",
      "thumbnailUrl": null,
      "createdAt": "2024-01-15T14:30:00"
    },
    {
      "id": "attachment-uuid-2",
      "fileName": "uuid_timestamp_2.pdf",
      "originalFileName": "invoice.pdf",
      "fileType": "application/pdf",
      "fileSize": 567890,
      "fileUrl": "/uploads/transactions/uuid_timestamp_2.pdf",
      "thumbnailUrl": null,
      "createdAt": "2024-01-15T14:35:00"
    }
  ],
  "timestamp": "2024-01-15T15:00:00"
}
```

---

## 3. View/Download File

### View Image in Browser
```bash
curl -X GET "http://localhost:8080/api/uploads/transactions/uuid_timestamp.jpg" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Download PDF
```bash
curl -X GET "http://localhost:8080/api/uploads/transactions/uuid_timestamp.pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o downloaded_invoice.pdf
```

Or simply open in browser:
```
http://localhost:8080/api/uploads/transactions/uuid_timestamp.jpg
```

---

## 4. Delete Attachment

```bash
curl -X DELETE "http://localhost:8080/api/transactions/{transactionId}/attachments/{attachmentId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Attachment deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T15:30:00"
}
```

---

## Supported File Types

### Allowed Formats
- **Images**: JPEG, JPG, PNG, GIF, WebP
- **Documents**: PDF

### File Size Limits
- **Maximum file size**: 5MB per file
- **Maximum request size**: 10MB (for multiple files)

---

## Using Postman

### Upload File
1. Select **POST** method
2. URL: `http://localhost:8080/api/transactions/{transactionId}/attachments`
3. Headers:
    - `Authorization: Bearer YOUR_JWT_TOKEN`
4. Body:
    - Select **form-data**
    - Key: `file` (change type to **File**)
    - Value: Click "Select Files" and choose your file
5. Click **Send**

### View File
1. Copy the `fileUrl` from upload response
2. Open in browser: `http://localhost:8080/api{fileUrl}`
3. Example: `http://localhost:8080/api/uploads/transactions/abc123_1234567890.jpg`

---

## Error Responses

### 400 Bad Request - Empty File
```json
{
  "success": false,
  "message": "Cannot upload empty file",
  "data": null,
  "timestamp": "2024-01-15T14:00:00"
}
```

### 400 Bad Request - File Too Large
```json
{
  "success": false,
  "message": "File size exceeds maximum allowed size of 5MB",
  "data": null,
  "timestamp": "2024-01-15T14:00:00"
}
```

### 400 Bad Request - Invalid File Type
```json
{
  "success": false,
  "message": "File type not allowed. Allowed types: JPEG, PNG, GIF, PDF, WebP",
  "data": null,
  "timestamp": "2024-01-15T14:00:00"
}
```

### 400 Bad Request - Invalid Filename
```json
{
  "success": false,
  "message": "Filename contains invalid path sequence",
  "data": null,
  "timestamp": "2024-01-15T14:00:00"
}
```

### 404 Not Found - Transaction
```json
{
  "success": false,
  "message": "Transaction not found",
  "data": null,
  "timestamp": "2024-01-15T14:00:00"
}
```

### 404 Not Found - Attachment
```json
{
  "success": false,
  "message": "Attachment not found",
  "data": null,
  "timestamp": "2024-01-15T14:00:00"
}
```

### 404 Not Found - File
```json
{
  "success": false,
  "message": "File not found: filename.jpg",
  "data": null,
  "timestamp": "2024-01-15T14:00:00"
}
```

---

## Testing Workflow

1. **Create a transaction** (income/expense)
2. **Upload receipt image** for that transaction
3. **Upload PDF invoice** as another attachment
4. **Get all attachments** to verify both uploaded
5. **View/download files** using the fileUrl
6. **Delete one attachment**
7. **Verify deletion** by getting attachments again

---

## File Storage Location

Files are stored locally in the `uploads/transactions/` directory:

```
project-root/
├── uploads/
│   └── transactions/
│       ├── uuid1_timestamp1.jpg
│       ├── uuid2_timestamp2.pdf
│       └── uuid3_timestamp3.png
```

---

## Security Notes

1. **Authentication Required**: All file operations require valid JWT token
2. **User Verification**: Users can only access files from their own transactions
3. **Path Traversal Protection**: Filenames are validated to prevent directory traversal attacks
4. **File Type Validation**: Only allowed file types can be uploaded
5. **Size Limits**: Files over 5MB are rejected
6. **Unique Filenames**: Files are renamed with UUID to prevent conflicts

---

## Example: Complete File Upload Flow

```bash
# 1. Create a transaction
TRANSACTION_ID=$(curl -X POST "http://localhost:8080/api/transactions" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "account-uuid",
    "type": "EXPENSE",
    "amount": 50.00,
    "transactionDate": "2024-01-15T14:00:00",
    "description": "Grocery shopping"
  }' | jq -r '.data.id')

# 2. Upload receipt
curl -X POST "http://localhost:8080/api/transactions/$TRANSACTION_ID/attachments" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@receipt.jpg"

# 3. Get all attachments
curl -X GET "http://localhost:8080/api/transactions/$TRANSACTION_ID/attachments" \
  -H "Authorization: Bearer $JWT_TOKEN"

# 4. View receipt in browser
FILE_URL=$(curl -X GET "http://localhost:8080/api/transactions/$TRANSACTION_ID/attachments" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq -r '.data[0].fileUrl')

echo "Open in browser: http://localhost:8080/api$FILE_URL"
```

---

## Pro Tips

1. **Receipt Management**: Upload photos of receipts immediately after purchase
2. **Invoice Tracking**: Attach PDF invoices to expense transactions
3. **Multiple Attachments**: A transaction can have multiple attachments
4. **File Naming**: Original filenames are preserved in metadata
5. **Cleanup**: Deleting a transaction automatically deletes its attachments
6. **File Access**: Files are served directly - can be embedded in frontend
7. **Mobile Apps**: Perfect for camera integration - snap receipt and upload