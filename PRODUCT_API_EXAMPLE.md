# Product API JSON Structure

This document describes the JSON structure for creating/updating products via the Product Service API.

## POST /api/product/products

### Request Body Structure

```json
{
  "name": "string (required)",
  "sku": "string (required)",

  "descriptions": [
    {
      "type": "title",
      "text": "Gaming Headset Pro"
    },
    {
      "type": "description",
      "text": "<p>Rich text HTML content</p>"
    },
    {
      "type": "image",
      "link": "https://example.com/image.jpg"
    }
  ],

  "specifications": {
    "general": [
      {"label": "Model", "value": "GP-100"},
      {"label": "Brand", "value": "TechCorp"}
    ],
    "technical": [
      {"label": "Driver", "value": "40mm"},
      {"label": "Impedance", "value": "32 Ohm"}
    ]
  },

  "videos": [
    {
      "videoUrl": "https://youtube.com/watch?v=xyz",
      "title": "Product Review",
      "description": "Detailed review video"
    }
  ],

  "image": "string",
  "showOnHomepage": true,
  "isFeatured": false,
  "price": 220000,

  "wholesalePrice": [
    {"quantity": 10, "price": 180000},
    {"quantity": 50, "price": 160000}
  ]
}
```

## Field Descriptions

### Required Fields
- **`name`**: Product name (string, required)
- **`sku`**: Unique product identifier (string, required)
- **`price`**: Retail price (number, required)

### Media Fields (Base64 Support)
- **`images`**: Main product image (string) - supports base64: `"data:image/jpeg;base64,/9j/4AAQ..."`
- **`descriptions[].link`**: Image links when type="image" - supports base64
- **`videos[].videoUrl`**: Video URLs - supports base64: `"data:video/mp4;base64,GkXf..."`

### Structured Fields
- **`descriptions`**: Array of content blocks
  - `type`: "title" | "description" | "image"
  - `text`: Text content (for title/description types)
  - `link`: URL or base64 data (for image type)

- **`specifications`**: Product specifications object
  - `general`: Array of general specs `{label, value}`
  - `technical`: Array of technical specs `{label, value}`

- **`videos`**: Array of video objects
  - `videoUrl`: Video URL or base64 data
  - `title`: Video title
  - `description`: Video description

- **`wholesalePrice`**: Array of quantity-based pricing
  - `quantity`: Minimum quantity for this price tier
  - `price`: Price per unit at this tier

### Display Options
- **`showOnHomepage`**: Display on homepage (boolean, default: false)
- **`isFeatured`**: Mark as featured product (boolean, default: false)

## Base64 Media Processing

The system supports base64-encoded media data for async processing:

### Images
```json
{
  "images": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEA...",
  "descriptions": [
    {
      "type": "image",
      "link": "data:image/png;base64,iVBORw0KGgoAAAANSUhE..."
    }
  ]
}
```

### Videos
```json
{
  "videos": [
    {
      "videoUrl": "data:video/mp4;base64,GkXfo59ChoEBQveBAULu...",
      "title": "Product Demo",
      "description": "Product demonstration video"
    }
  ]
}
```

## Response

The API returns the created product immediately, while media processing happens asynchronously via Kafka:

```json
{
  "id": 1,
  "sku": "GP-100",
  "name": "Gaming Headset Pro",
  "images": "data:image/jpeg;base64,/9j/4AAQ...",
  "descriptions": [...],
  "videos": [...],
  "specifications": {...},
  "price": 220000,
  "wholesalePrice": [...],
  "showOnHomepage": true,
  "isFeatured": false,
  "createdAt": "2025-09-14T10:30:00Z",
  "updateAt": "2025-09-14T10:30:00Z"
}
```

> **Note**: Base64 data will be replaced with Cloudinary URLs after async processing completes.

## Media Processing Flow

1. **Immediate**: Product saved with original data
2. **Async**: Base64 data sent to Kafka topic `media-upload-request`
3. **Background**: Media Service uploads to Cloudinary
4. **Future**: Callback updates product with Cloudinary URLs

This approach ensures fast API responses while handling media uploads reliably in the background.