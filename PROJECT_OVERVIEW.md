# E-Commerce Microservice Architecture - Complete Project Overview

## 📋 Table of Contents
- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Service Details](#service-details)
- [Database Design](#database-design)
- [API Documentation](#api-documentation)
- [Security Implementation](#security-implementation)
- [Configuration & Deployment](#configuration--deployment)
- [Recent Improvements](#recent-improvements)

## 🏗️ Project Structure

```
microservice-parent/
├── api-gateway/              # Gateway service (Port 9000)
├── auth-service/             # Authentication & Authorization (Port 8081)
├── cart-service/             # Shopping cart management (Port 8082)
├── product-service/          # Product catalog management (Port 8083)
├── blog-service/             # Blog & content management (Port 8084)
├── user-service/             # User & dealer management (Port 8085)
├── media-service/            # File upload & media handling (Port 8086)
├── notification-service/     # Real-time notifications (Port 8087)
├── order-service/            # Order processing (Port 8088)
├── common-service/           # Shared utilities & exceptions
├── config-server/            # Centralized configuration
└── discovery-server/         # Service discovery (Eureka)
```

## 🎯 Architecture Overview

### Microservice Pattern
- **API Gateway**: Single entry point, routing, authentication
- **Service Discovery**: Eureka server for service registration
- **Config Server**: Centralized configuration management
- **Event-Driven**: Kafka for asynchronous communication
- **Database Per Service**: Each service has its own database

### Technology Stack
- **Backend**: Spring Boot 3.x, Spring Cloud
- **Database**: PostgreSQL for each service
- **Cache**: Redis (for token blacklisting)
- **Message Queue**: Apache Kafka
- **File Storage**: Cloudinary
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Security**: JWT with RS256, Role-based access

## 🔧 Service Details

### 1. **API Gateway** (Port 9000)
**Purpose**: Single entry point for all client requests
- **Routes**: `/auth/**`, `/product/**`, `/cart/**`, `/blog/**`, `/user/**`, `/media/**`, `/notification/**`
- **Security**: JWT validation, role-based routing
- **Features**: Load balancing, rate limiting, CORS handling

### 2. **Auth Service** (Port 8081)
**Purpose**: Authentication and authorization management

#### Key Features:
- **JWT Token Management**: RS256 with JWKS endpoint
- **Token Types**: Access tokens (30 min), Refresh tokens (7 days)
- **Security**: Token blacklisting with Redis
- **Endpoints**:
  - `POST /auth/login` - User login
  - `POST /auth/logout` - User logout (blacklist token)
  - `POST /auth/refresh` - Refresh access token
  - `POST /auth/register` - Create new account
  - `GET /auth/.well-known/jwks.json` - Public keys for JWT validation

#### Database Tables:
- No persistent storage (stateless with Redis cache)

### 3. **Product Service** (Port 8083)
**Purpose**: Product catalog and inventory management

#### Key Features:
- **Product Management**: CRUD operations with soft delete
- **Field Filtering**: Dynamic response field selection
- **Product Categories**: Homepage, featured, all products
- **Serial Number Tracking**: Individual product serial management
- **Inventory Management**: Stock tracking and availability

#### API Endpoints:
```
GET    /product/products/showhomepageandlimit4     # Homepage products
GET    /product/{id}                              # Product details
GET    /product/products/featured                 # Featured products
GET    /product/related/{productId}               # Related products
GET    /product/products                          # All active products
GET    /product/deleted                           # Deleted products (ADMIN)
POST   /product                                   # Create product (ADMIN)
PUT    /product/{id}                              # Update product (ADMIN)
DELETE /product/{id}                              # Soft delete (ADMIN)
DELETE /product/{id}/hard                         # Hard delete (ADMIN)
PUT    /product/{id}/restore                      # Restore product (ADMIN)

# Product Serial Management
POST   /product/serial/bulk                       # Bulk create serials
GET    /product/serial/product/{productId}        # Get product serials
POST   /product/serial                            # Create single serial
DELETE /product/serial/{id}                       # Delete serial
PUT    /product/serial/{id}/status                # Update serial status
GET    /product/serial/inventory/{productId}      # Get inventory stats
GET    /product/serial/available-count/{productId} # Available count
```

#### Database Tables:
- **products**: Main product information
- **product_serials**: Individual product serial numbers

### 4. **Cart Service** (Port 8082)
**Purpose**: Shopping cart management with pricing tiers

#### Key Features:
- **Dealer-Product Relationship**: Direct mapping without intermediate entities
- **Pricing Tiers**: Bulk pricing support with unit_price
- **BigDecimal Precision**: Financial calculations
- **Quantity Management**: Add, update, remove cart items

#### API Endpoints:
```
POST   /cart/add                    # Add product to cart
GET    /cart/dealer/{dealerId}      # Get dealer cart
DELETE /cart/remove                 # Remove product from cart
PUT    /cart/update                 # Update product quantity
```

#### Database Tables:
- **dealer_carts**: Composite key (dealer_id, product_id) with quantity and unit_price

### 5. **User Service** (Port 8085)
**Purpose**: User and dealer account management

#### Key Features:
- **Account Generation**: Automatic username/password generation
- **Dealer Management**: Dealer-specific operations
- **Integration**: Auth service integration for account creation

#### API Endpoints:
```
GET    /user/dealers               # Get all dealers
POST   /user/dealers               # Create new dealer (ADMIN)
GET    /user/dealers/{id}          # Get dealer details
PUT    /user/dealers/{id}          # Update dealer (ADMIN)
DELETE /user/dealers/{id}          # Delete dealer (ADMIN)
```

#### Database Tables:
- **dealers**: Dealer information and contact details

### 6. **Blog Service** (Port 8084)
**Purpose**: Content management system

#### Key Features:
- **Blog Management**: CRUD operations with soft delete
- **Category System**: Blog categorization
- **Related Content**: Related blogs functionality
- **Field Filtering**: Dynamic response customization

#### API Endpoints:
```
# Blog Management
GET    /blog/blogs                 # All blogs (ADMIN)
GET    /blog/deleted               # Deleted blogs (ADMIN)
GET    /blog                       # Public blogs
GET    /blog/related/{blogId}      # Related blogs
GET    /blog/{id}                  # Blog details
POST   /blog                       # Create blog (ADMIN)
PUT    /blog/{id}                  # Update blog (ADMIN)
DELETE /blog/{id}                  # Soft delete (ADMIN)
DELETE /blog/{id}/hard             # Hard delete (ADMIN)
PUT    /blog/{id}/restore          # Restore blog (ADMIN)

# Category Management
GET    /blog/categories            # All categories
POST   /blog/categories            # Create category (ADMIN)
DELETE /blog/categories/{id}       # Delete category (ADMIN)
```

#### Database Tables:
- **blogs**: Blog content and metadata
- **category_blogs**: Blog categories

### 7. **Media Service** (Port 8086)
**Purpose**: File upload and media management

#### Key Features:
- **Cloudinary Integration**: Cloud-based file storage
- **Security**: File validation, magic byte checking
- **File Types**: Images only (JPG, PNG, GIF, WebP)
- **Validation**: Size limits, file signature verification

#### API Endpoints:
```
POST   /media/upload              # Upload image (ADMIN)
DELETE /media/delete              # Delete image (ADMIN)
```

#### Security Features:
- File signature validation (magic bytes)
- Path traversal prevention
- Size limitations (10MB default)
- Filename sanitization

### 8. **Notification Service** (Port 8087)
**Purpose**: Real-time notification system

#### Key Features:
- **Kafka Integration**: Event-driven notifications
- **WebSocket Support**: Real-time updates
- **Email Notifications**: SMTP integration
- **Notification Management**: Mark as read functionality

#### API Endpoints:
```
GET    /notification               # Get notifications
PUT    /notification/{id}/read     # Mark as read
```

#### Database Tables:
- **notifications**: Notification content and status

### 9. **Order Service** (Port 8088)
**Purpose**: Order processing and management

#### Key Features:
- **Order Management**: Order lifecycle management
- **Order Items**: Detailed item tracking with BigDecimal pricing
- **Financial Calculations**: VAT, shipping, totals

#### Database Tables:
- **orders**: Order header information
- **order_items**: Order line items with pricing details

## 🗄️ Database Design

### Common Patterns:
- **Soft Delete**: `is_deleted` boolean field across entities
- **Auditing**: Created/updated timestamps
- **BigDecimal**: Financial fields for precision
- **Composite Keys**: For relationship tables

### Key Relationships:
- **Product ↔ ProductSerial**: One-to-many
- **Dealer ↔ Cart**: One-to-many via dealer_carts
- **Product ↔ Cart**: Many-to-many via dealer_carts
- **Blog ↔ Category**: Many-to-one
- **Order ↔ OrderItem**: One-to-many

## 🔐 Security Implementation

### JWT Authentication:
- **Algorithm**: RS256 (asymmetric)
- **Key Management**: JWKS endpoint for public key distribution
- **Token Lifecycle**: Access (30min) + Refresh (7 days)
- **Blacklisting**: Redis-based token invalidation

### Authorization:
- **Roles**: ADMIN, USER
- **Gateway Level**: Route-based access control
- **Service Level**: Method-level security

### File Security:
- **Magic Byte Validation**: Prevents file type spoofing
- **Path Traversal Protection**: Filename sanitization
- **Size Limits**: Prevents DoS attacks

## ⚙️ Configuration & Deployment

### Service Ports:
- API Gateway: 9000
- Auth Service: 8081
- Cart Service: 8082
- Product Service: 8083
- Blog Service: 8084
- User Service: 8085
- Media Service: 8086
- Notification Service: 8087
- Order Service: 8088

### External Dependencies:
- **PostgreSQL**: Per-service databases
- **Redis**: Token blacklisting
- **Kafka**: Event streaming
- **Cloudinary**: File storage
- **SMTP**: Email notifications

### Configuration:
- **Config Server**: Centralized configuration management
- **Environment Variables**: Sensitive data (API keys, passwords)
- **Service Discovery**: Eureka for service registration

## 🚀 Recent Improvements

### Priority 1 - Security & Critical:
✅ **Removed hardcoded credentials** - Cloudinary config now uses environment variables
✅ **Implemented file validation** - Magic byte checking, size limits, security measures
✅ **Added input validation** - Custom annotations (@ValidId, @ValidEmail)
✅ **Exception standardization** - BaseException hierarchy across all services

### Priority 2 - Clean Code:
✅ **Extracted magic numbers** - Constants for file signatures, time calculations, RSA keys
✅ **Method decomposition** - Complex methods broken into focused, single-responsibility methods
✅ **Eliminated duplication** - Common utilities (ResponseUtil, RepositoryUtil, LoggingUtil)
✅ **Standardized error messages** - Centralized ErrorMessages constants

### Code Quality Improvements:
- **Utility Classes**: ResponseUtil, RepositoryUtil, LoggingUtil, ErrorMessages
- **Constants**: Magic numbers extracted to meaningful constants
- **Method Decomposition**: Large methods split into focused units
- **Error Handling**: Consistent error messages and response formats

## 📊 API Response Format

### Success Response:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### Error Response:
```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "ERROR_CODE",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

## 🔄 Event Flow Examples

### Product Purchase Flow:
1. **Client** → API Gateway → **Product Service** (check availability)
2. **Product Service** → **Cart Service** (add to cart)
3. **Cart Service** → **Order Service** (create order)
4. **Order Service** → **Notification Service** (via Kafka)
5. **Notification Service** → WebSocket/Email (notify user)

### Authentication Flow:
1. **Client** → API Gateway → **Auth Service** (login)
2. **Auth Service** → Redis (store session)
3. **Auth Service** → Client (JWT tokens)
4. **Subsequent requests** → API Gateway (JWT validation via JWKS)

## 📝 Development Guidelines

### Code Standards:
- **Clean Code**: Single responsibility, meaningful names
- **Error Handling**: Use BaseException hierarchy
- **Logging**: Use LoggingUtil for consistent patterns
- **Responses**: Use ResponseUtil for standardized responses
- **Validation**: Custom validation annotations

### Testing Strategy:
- **Unit Tests**: Service layer methods
- **Integration Tests**: API endpoints
- **Contract Tests**: Service-to-service communication

### Deployment:
- **Docker**: Containerized services
- **Environment Config**: External configuration files
- **Health Checks**: Actuator endpoints
- **Monitoring**: Centralized logging and metrics

---

**Last Updated**: January 2025
**Architecture Version**: 2.0
**Total Services**: 9 microservices + infrastructure components