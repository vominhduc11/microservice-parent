# Blog API JSON Structure

This document describes the JSON structure for creating/updating blogs and category blogs via the Blog Service API.

## Blog API Endpoints

### POST /api/blog/blogs (ADMIN Only)

#### Request Body Structure
hay 
```json
{
  "title": "string (required)",
  "content": "string (required, rich text HTML)",
  "categoryBlogId": 1,
  "showOnHomepage": true
}
```

#### Response

```json
{
  "success": true,
  "message": "Blog created successfully",
  "data": {
    "id": 1,
    "title": "How to Choose the Right Gaming Headset",
    "content": "<p>Rich text HTML content with formatting...</p>",
    "category": "Gaming",
    "showOnHomepage": true,
    "createdAt": "2025-09-16T10:30:00Z",
    "updatedAt": "2025-09-16T10:30:00Z"
  }
}
```

### GET /api/blog/blogs (ADMIN Only)

Get all blogs for administrative purposes with optional field filtering.

#### Query Parameters

- `fields` (optional): Comma-separated list of fields to return
  - Example: `?fields=id,title,category,createdAt`

#### Response

```json
{
  "success": true,
  "message": "All blogs retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "Gaming Tips and Tricks",
      "content": "<p>Content here...</p>",
      "category": "Gaming",
      "showOnHomepage": true,
      "createdAt": "2025-09-16T10:30:00Z",
      "updatedAt": "2025-09-16T10:30:00Z"
    }
  ]
}
```

### GET /api/blog/blogs/showhomepageandlimit6 (Public)

Get 6 blogs for homepage display with `showOnHomepage=true`.

#### Query Parameters

- `fields` (optional): Field filtering support

#### Response

```json
{
  "success": true,
  "message": "Blogs retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "Featured Blog Post",
      "content": "<p>Homepage blog content...</p>",
      "category": "Tech News",
      "showOnHomepage": true,
      "createdAt": "2025-09-16T10:30:00Z",
      "updatedAt": "2025-09-16T10:30:00Z"
    }
  ]
}
```

### GET /api/blog/{id} (Public)

Get detailed information about a specific blog by ID.

#### Response

```json
{
  "success": true,
  "message": "Blog details retrieved successfully",
  "data": {
    "id": 1,
    "title": "Complete Gaming Setup Guide",
    "content": "<h1>Gaming Setup</h1><p>Full detailed content with HTML formatting...</p>",
    "category": "Gaming",
    "showOnHomepage": true,
    "createdAt": "2025-09-16T10:30:00Z",
    "updatedAt": "2025-09-16T10:30:00Z"
  }
}
```

### PATCH /api/blog/{id} (ADMIN Only)

Update an existing blog by ID. Only provided fields will be updated (PATCH behavior).

#### Request Body Structure

```json
{
  "title": "Updated Blog Title",
  "content": "<p>Updated content...</p>",
  "categoryBlogId": 2,
  "showOnHomepage": false
}
```

#### Response

```json
{
  "success": true,
  "message": "Blog updated successfully",
  "data": {
    "id": 1,
    "title": "Updated Blog Title",
    "content": "<p>Updated content...</p>",
    "category": "Reviews",
    "showOnHomepage": false,
    "createdAt": "2025-09-16T10:30:00Z",
    "updatedAt": "2025-09-16T11:45:00Z"
  }
}
```

## Category Blog API Endpoints

### GET /api/blog/categories (Public)

Get all category blogs without authentication.

#### Response

```json
{
  "success": true,
  "message": "Category blogs retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Gaming"
    },
    {
      "id": 2,
      "name": "Reviews"
    },
    {
      "id": 3,
      "name": "Tech News"
    }
  ]
}
```

### POST /api/blog/categories (ADMIN Only)

Create a new category blog.

#### Request Body Structure

```json
{
  "name": "string (required, max 255 characters)"
}
```

#### Response

```json
{
  "success": true,
  "message": "Category blog created successfully",
  "data": {
    "id": 4,
    "name": "Tutorials"
  }
}
```

### DELETE /api/blog/categories/{id} (ADMIN Only)

Delete a category blog by ID.

#### Response

```json
{
  "success": true,
  "message": "Category blog deleted successfully",
  "data": null
}
```

## Field Descriptions

### Blog Fields

#### Required Fields
- **`title`**: Blog post title (string, required)
- **`content`**: Blog post content (string, required) - supports rich text HTML
- **`categoryBlogId`**: Category ID reference (number, required for creation)

#### Optional Fields
- **`showOnHomepage`**: Display on homepage (boolean, default: false)

### Category Blog Fields

#### Required Fields
- **`name`**: Category name (string, required, max 255 characters)

## Authentication & Authorization

### Public Endpoints (No Authentication Required)
- `GET /api/blog/categories` - Get all categories
- `GET /api/blog/blogs/showhomepageandlimit6` - Get homepage blogs
- `GET /api/blog/{id}` - Get specific blog details

### ADMIN Only Endpoints (Requires JWT Token with ADMIN Role)
- `GET /api/blog/blogs` - Get all blogs for admin
- `POST /api/blog/blogs` - Create new blog
- `PATCH /api/blog/{id}` - Update blog
- `POST /api/blog/categories` - Create category
- `DELETE /api/blog/categories/{id}` - Delete category

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Invalid blog data",
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
  "message": "Blog not found",
  "data": null
}
```

## Usage Examples

### Creating a Blog Post

```bash
curl -X POST "http://localhost:8080/api/blog/blogs" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Ultimate Gaming Setup Guide 2025",
    "content": "<h1>Gaming Setup</h1><p>Here is how to build the perfect gaming setup...</p>",
    "categoryBlogId": 1,
    "showOnHomepage": true
  }'
```

### Creating a Category

```bash
curl -X POST "http://localhost:8080/api/blog/categories" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Hardware Reviews"
  }'
```

### Getting Homepage Blogs

```bash
curl "http://localhost:8080/api/blog/blogs/showhomepageandlimit6"
```

### Getting All Categories

```bash
curl "http://localhost:8080/api/blog/categories"
```

## Notes

- All endpoints require the `X-Gateway-Request: true` header when accessed through the API Gateway
- Rich text HTML content is supported in blog content field
- Field filtering is available on GET endpoints using the `fields` query parameter
- Category blogs are referenced by ID in blog creation/updates
- Public endpoints are cached for better performance
- ADMIN endpoints require proper JWT authentication with ADMIN role