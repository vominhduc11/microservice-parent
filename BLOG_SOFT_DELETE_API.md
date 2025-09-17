# Blog Soft Delete API Documentation

This document describes the soft delete functionality implemented for the Blog Service, providing comprehensive blog lifecycle management.

## Overview

The Blog Service now supports soft delete functionality, allowing blogs to be temporarily removed (soft delete), permanently deleted (hard delete), or restored. This provides better data management and recovery options.

## Soft Delete API Endpoints

All endpoints require ADMIN authentication via JWT token through the API Gateway.

### 1. Get All Active Blogs
**GET** `/api/blog/blogs`

Retrieves all active blogs (isDeleted=false) for admin management.

#### Response
```json
{
  "success": true,
  "message": "All blogs retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "Gaming Setup Guide",
      "description": "Complete guide to gaming setup",
      "image": {
        "public_id": "gaming-setup",
        "imageUrl": "https://example.com/gaming.jpg"
      },
      "category": "Gaming",
      "showOnHomepage": true,
      "createdAt": "2024-01-15T10:30:00",
      "updateAt": "2024-01-15T10:30:00"
    }
  ]
}
```

#### Example
```bash
curl -X GET "http://localhost:8080/api/blog/blogs" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Get All Deleted Blogs
**GET** `/api/blog/blogs/deleted`

Retrieves all soft deleted blogs (isDeleted=true) for admin review and potential restoration.

#### Response
```json
{
  "success": true,
  "message": "Deleted blogs retrieved successfully",
  "data": [
    {
      "id": 2,
      "title": "Outdated Hardware Review",
      "description": "Review of old hardware",
      "image": {
        "public_id": "old-hardware",
        "imageUrl": "https://example.com/hardware.jpg"
      },
      "category": "Reviews",
      "showOnHomepage": false,
      "createdAt": "2024-01-10T08:00:00",
      "updateAt": "2024-01-20T14:30:00"
    }
  ]
}
```

#### Example
```bash
curl -X GET "http://localhost:8080/api/blog/blogs/deleted" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Soft Delete Blog
**DELETE** `/api/blog/{id}`

Soft deletes a blog by setting isDeleted=true. The blog is hidden from public and admin listings but remains in the database.

#### Response
```json
{
  "success": true,
  "message": "Blog soft deleted successfully",
  "data": null
}
```

#### Example
```bash
curl -X DELETE "http://localhost:8080/api/blog/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Hard Delete Blog
**DELETE** `/api/blog/{id}/hard`

Permanently deletes a blog from the database. This action cannot be undone.

#### Response
```json
{
  "success": true,
  "message": "Blog permanently deleted successfully",
  "data": null
}
```

#### Example
```bash
curl -X DELETE "http://localhost:8080/api/blog/1/hard" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Restore Deleted Blog
**PATCH** `/api/blog/{id}/restore`

Restores a soft deleted blog by setting isDeleted=false. The blog becomes visible again in listings.

#### Response
```json
{
  "success": true,
  "message": "Blog restored successfully",
  "data": {
    "id": 1,
    "title": "Gaming Setup Guide",
    "description": "Complete guide to gaming setup",
    "image": {
      "public_id": "gaming-setup",
      "imageUrl": "https://example.com/gaming.jpg"
    },
    "category": "Gaming",
    "showOnHomepage": true,
    "createdAt": "2024-01-15T10:30:00",
    "updateAt": "2024-01-20T15:45:00"
  }
}
```

#### Example
```bash
curl -X PATCH "http://localhost:8080/api/blog/1/restore" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Error Responses

### 400 Bad Request - Blog Not Deleted
```json
{
  "success": false,
  "message": "Blog with ID: 1 is not deleted",
  "data": null
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized - Invalid or missing JWT token",
  "data": null
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Forbidden - ADMIN role required",
  "data": null
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Blog not found with ID: 1",
  "data": null
}
```

## Implementation Details

### Database Changes
- Added `is_deleted` column to `blogs` table with default value `false`
- All queries now filter by `isDeleted=false` for active content
- Soft deleted blogs remain in database with `isDeleted=true`

### Security
- All endpoints require ADMIN role authentication
- Protected by API Gateway security configuration
- JWT token validation for all operations

### Public Endpoint Behavior
- `GET /api/blog/blogs/showhomepageandlimit6` - Only returns active blogs
- `GET /api/blog/{id}` - Only returns active blogs
- Soft deleted blogs are completely hidden from public access

## Best Practices

1. **Use Soft Delete First**: Always use soft delete unless permanent removal is specifically required
2. **Review Before Hard Delete**: Check deleted blogs list before permanent deletion
3. **Backup Strategy**: Consider database backups before hard delete operations
4. **Audit Trail**: Monitor soft delete operations for content management
5. **Recovery Planning**: Establish procedures for restoring accidentally deleted content

## Migration Notes

- Existing blogs automatically have `isDeleted=false`
- No breaking changes to existing public APIs
- Admin APIs now filter soft deleted content by default
- Homepage and public listings remain unchanged