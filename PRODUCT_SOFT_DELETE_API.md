# Product Soft Delete API Documentation

This document describes the soft delete functionality implemented for the Product Service, providing comprehensive product lifecycle management and field filtering capabilities.

## Overview

The Product Service now supports soft delete functionality with enhanced field filtering, allowing products to be temporarily removed (soft delete), permanently deleted (hard delete), or restored. This provides better inventory management and recovery options.

## Field Filtering Enhancement

All product responses now support field filtering with `@JsonInclude(NON_NULL)`, meaning only requested fields are returned in the JSON response when using the `fields` parameter.

**Example**: `GET /api/product/products/showhomepageandlimit4?fields=name,image` returns only:
```json
{
  "name": "Gaming Headset",
  "image": "https://example.com/headset.jpg"
}
```

## Soft Delete API Endpoints

All endpoints require ADMIN authentication via JWT token through the API Gateway.

### 1. Get All Active Products
**GET** `/api/product/products`

Retrieves all active products (isDeleted=false) for admin management.

#### Response
```json
{
  "success": true,
  "message": "Active products retrieved successfully",
  "data": [
    {
      "id": 1,
      "sku": "GH-001",
      "name": "Gaming Headset Pro",
      "image": "https://example.com/headset.jpg",
      "descriptions": "High-quality gaming headset",
      "videos": "https://example.com/demo.mp4",
      "specifications": "7.1 Surround Sound, RGB Lighting",
      "price": 149.99,
      "wholesalePrice": "120.00",
      "showOnHomepage": true,
      "isFeatured": false,
      "createdAt": "2024-01-15T10:30:00",
      "updateAt": "2024-01-15T10:30:00"
    }
  ]
}
```

#### Example
```bash
curl -X GET "http://localhost:8080/api/product/products" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Get All Deleted Products
**GET** `/api/product/products/deleted`

Retrieves all soft deleted products (isDeleted=true) for admin review and potential restoration.

#### Response
```json
{
  "success": true,
  "message": "Deleted products retrieved successfully",
  "data": [
    {
      "id": 2,
      "sku": "KB-002",
      "name": "Mechanical Keyboard",
      "image": "https://example.com/keyboard.jpg",
      "descriptions": "RGB mechanical keyboard",
      "videos": "https://example.com/keyboard-demo.mp4",
      "specifications": "Cherry MX Blue switches",
      "price": 99.99,
      "wholesalePrice": "75.00",
      "showOnHomepage": false,
      "isFeatured": false,
      "createdAt": "2024-01-10T08:00:00",
      "updateAt": "2024-01-20T14:30:00"
    }
  ]
}
```

#### Example
```bash
curl -X GET "http://localhost:8080/api/product/products/deleted" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Soft Delete Product
**DELETE** `/api/product/{id}`

Soft deletes a product by setting isDeleted=true. The product is hidden from public and admin listings but remains in the database.

#### Response
```json
{
  "success": true,
  "message": "Product soft deleted successfully",
  "data": null
}
```

#### Example
```bash
curl -X DELETE "http://localhost:8080/api/product/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Hard Delete Product
**DELETE** `/api/product/{id}/hard`

Permanently deletes a product from the database. This action cannot be undone and will also delete related ProductSerial records.

#### Response
```json
{
  "success": true,
  "message": "Product permanently deleted successfully",
  "data": null
}
```

#### Example
```bash
curl -X DELETE "http://localhost:8080/api/product/1/hard" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Restore Deleted Product
**PATCH** `/api/product/{id}/restore`

Restores a soft deleted product by setting isDeleted=false. The product becomes visible again in listings.

#### Response
```json
{
  "success": true,
  "message": "Product restored successfully",
  "data": {
    "id": 1,
    "sku": "GH-001",
    "name": "Gaming Headset Pro",
    "image": "https://example.com/headset.jpg",
    "descriptions": "High-quality gaming headset",
    "videos": "https://example.com/demo.mp4",
    "specifications": "7.1 Surround Sound, RGB Lighting",
    "price": 149.99,
    "wholesalePrice": "120.00",
    "showOnHomepage": true,
    "isFeatured": false,
    "createdAt": "2024-01-15T10:30:00",
    "updateAt": "2024-01-20T15:45:00"
  }
}
```

#### Example
```bash
curl -X PATCH "http://localhost:8080/api/product/1/restore" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Field Filtering Examples

### Homepage Products with Field Filtering
```bash
# Get only name and image fields
curl "http://localhost:8080/api/product/products/showhomepageandlimit4?fields=name,image"

# Response
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": [
    {
      "name": "Gaming Headset Pro",
      "image": "https://example.com/headset.jpg"
    }
  ]
}
```

### Featured Products with Price
```bash
# Get name, image, and price fields
curl "http://localhost:8080/api/product/products/featuredandlimit1?fields=name,image,price"

# Response
{
  "success": true,
  "message": "Featured products retrieved successfully",
  "data": [
    {
      "name": "Gaming Mouse Elite",
      "image": "https://example.com/mouse.jpg",
      "price": 79.99
    }
  ]
}
```

## Error Responses

### 400 Bad Request - Product Not Deleted
```json
{
  "success": false,
  "message": "Product with ID: 1 is not deleted",
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
  "message": "Product not found with ID: 1",
  "data": null
}
```

### 409 Conflict - SKU Exists
```json
{
  "success": false,
  "message": "Product with SKU 'GH-001' already exists",
  "data": null
}
```

## Implementation Details

### Database Changes
- Added `is_deleted` column to `products` table with default value `false`
- All queries now filter by `isDeleted=false` for active products
- Soft deleted products remain in database with `isDeleted=true`
- SKU uniqueness checks only apply to active products (isDeleted=false)

### Field Filtering
- `@JsonInclude(JsonInclude.Include.NON_NULL)` annotation on ProductResponse
- Only non-null fields are included in JSON response
- Supports fields: `id`, `name`, `image`, `descriptions`, `price`
- Available on public endpoints: `/showhomepageandlimit4` and `/featuredandlimit1`

### Security
- All CRUD endpoints require ADMIN role authentication
- Protected by API Gateway security configuration
- JWT token validation for all operations
- Public endpoints remain accessible without authentication

### Public Endpoint Behavior
- `GET /api/product/products/showhomepageandlimit4` - Only returns active products
- `GET /api/product/products/featuredandlimit1` - Only returns active featured products
- `GET /api/product/{id}` - Only returns active products
- Soft deleted products are completely hidden from public access

## ProductSerial Integration

### Cascade Delete Behavior
- Hard delete operations cascade to related ProductSerial records
- Soft delete preserves ProductSerial relationships
- Restore operations maintain ProductSerial associations

### Inventory Management
- Soft deleted products' serials remain in database
- Hard delete removes all associated serial numbers
- Inventory counts exclude soft deleted products

## Best Practices

1. **Use Soft Delete First**: Always use soft delete unless permanent removal is specifically required
2. **Review Before Hard Delete**: Check deleted products list and associated serials before permanent deletion
3. **SKU Management**: Remember that SKUs of soft deleted products can be reused for new products
4. **Field Filtering**: Use field filtering on public endpoints to optimize response size and improve performance
5. **Backup Strategy**: Consider database backups before hard delete operations
6. **Audit Trail**: Monitor soft delete operations for inventory management
7. **Recovery Planning**: Establish procedures for restoring accidentally deleted products

## Migration Notes

- Existing products automatically have `isDeleted=false`
- No breaking changes to existing public APIs
- Admin APIs now filter soft deleted products by default
- Homepage and featured product listings remain unchanged
- Field filtering is backward compatible (returns all fields if no `fields` parameter)
- SKU uniqueness validation updated to check only active products