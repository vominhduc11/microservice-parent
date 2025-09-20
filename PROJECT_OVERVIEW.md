# E-Commerce Microservice Architecture - Complete Project Overview

## 📋 Table of Contents
- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Service Details](#service-details)
- [Database Design](#database-design)
- [API Documentation](#api-documentation)
- [Security Implementation](#security-implementation)
- [Configuration & Deployment](#configuration--deployment)
- [Code Quality & Recent Improvements](#code-quality--recent-improvements)
- [Getting Started](#getting-started)

## 🏗️ Project Structure

```
microservice-parent/
├── api-gateway/              # Gateway service (Port 8080)
├── auth-service/             # Authentication & Authorization (Port 8081)
├── user-service/             # User & dealer management (Port 8082)
├── product-service/          # Product catalog management (Port 8083)
├── cart-service/             # Shopping cart management (Port 8084)
├── order-service/            # Order processing (Port 8085)
├── warranty-service/         # Warranty management (Port 8086)
├── notification-service/     # Real-time notifications (Port 8087)
├── blog-service/             # Blog & content management (Port 8088)
├── report-service/           # Analytics & reporting (Port 8089)
├── media-service/            # File upload & media handling (Port 8095)
├── common-service/           # Shared utilities & exceptions
├── config-server/            # Centralized configuration (Port 8888)
└── scripts/                  # Database optimization scripts
```

## 🎯 Architecture Overview

### Microservice Pattern
- **API Gateway**: Single entry point, routing, authentication
- **Config Server**: Centralized configuration management
- **Event-Driven**: Kafka for asynchronous communication
- **Database Per Service**: Each service has its own PostgreSQL database
- **Shared Libraries**: Common service for utilities and exceptions

### Technology Stack
- **Backend**: Spring Boot 3.4.6, Spring Cloud 2024.0.0
- **Database**: PostgreSQL 15 (isolated per service)
- **Cache**: Redis 7 (JWT blacklisting, session management)
- **Message Queue**: Apache Kafka 7.4.0 (3-broker cluster)
- **File Storage**: Cloudinary (cloud-based media management)
- **Documentation**: OpenAPI 3.0 (Swagger UI)
- **Security**: JWT with RS256, Role-based access control
- **Containerization**: Docker & Docker Compose

## 🔧 Service Details

### 1. **API Gateway** (Port 8080)
**Purpose**: Single entry point for all client requests
- **Routes**: `/api/auth/**`, `/api/product/**`, `/api/cart/**`, `/api/blog/**`, `/api/user/**`, `/api/media/**`, `/api/notification/**`
- **Security**: JWT validation, role-based routing
- **Features**: Load balancing, CORS handling
- **Health**: http://localhost:8080/actuator/health

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
GET    /product/products                          # All active products (ADMIN)
POST   /product                                   # Create product (ADMIN)
PUT    /product/{id}                              # Update product (ADMIN)
DELETE /product/{id}                              # Soft delete (ADMIN)
```

### 4. **Cart Service** (Port 8084)
**Purpose**: Shopping cart management for dealers

#### Key Features:
- **Dealer Cart Management**: CRUD operations for dealer carts
- **Pricing Tiers**: Multiple wholesale price levels
- **Unit Price Tracking**: Individual item pricing
- **Quantity Management**: Add, update, remove items

#### API Endpoints:
```
POST   /cart/add                                  # Add to cart
GET    /cart/{dealerId}                          # Get dealer cart
PUT    /cart/{dealerId}/products/{productId}     # Update quantity/price
DELETE /cart/{dealerId}/products/{productId}     # Remove from cart
```

### 5. **User Service** (Port 8082)
**Purpose**: User and dealer management

#### Key Features:
- **Dealer Registration**: Complete dealer onboarding process
- **User Management**: Customer and admin user handling
- **Event Integration**: Kafka events for dealer registration
- **Account Integration**: Links with auth-service for accounts

### 6. **Media Service** (Port 8095)
**Purpose**: File upload and media management

#### Key Features:
- **Cloudinary Integration**: Cloud-based file storage
- **Image Upload**: Support for various image formats
- **Video Upload**: Video file handling
- **File Deletion**: Secure file removal

#### API Endpoints:
```
POST   /media/upload/image                       # Upload image (ADMIN)
POST   /media/upload/video                       # Upload video (ADMIN)
DELETE /media/delete                             # Delete media (ADMIN)
```

### 7. **Notification Service** (Port 8087)
**Purpose**: Real-time notifications and messaging

#### Key Features:
- **Email Notifications**: SMTP integration
- **WebSocket Notifications**: Real-time updates
- **Kafka Integration**: Event-driven messaging
- **Notification Storage**: Persistent notification history

### 8. **Blog Service** (Port 8088)
**Purpose**: Content management system

#### Key Features:
- **Blog Management**: Create, read, update, delete blogs
- **Category Management**: Blog categorization
- **Public API**: Homepage blog display
- **Content Filtering**: Dynamic content selection

### 9. **Other Services**
- **Order Service** (Port 8085): Order processing and management
- **Warranty Service** (Port 8086): Warranty tracking and claims
- **Report Service** (Port 8089): Analytics and business intelligence
- **Config Server** (Port 8888): Centralized configuration

## 💾 Database Design

### Database Architecture
Each service maintains its own isolated PostgreSQL database following the database-per-service pattern:

```sql
-- Infrastructure Database
auth_service_db:
  - accounts (id, username, password, created_at, updated_at)
  - roles (id, name)
  - account_roles (account_id, role_id)

-- User Management Database
user_service_db:
  - customers (account_id, name, email, phone, address)
  - dealers (account_id, company_name, email, phone, address, city, district)
  - admins (account_id, name, email)

-- Product Catalog Database
product_service_db:
  - products (id, sku, name, image, descriptions, videos, specifications,
             price, wholesale_price, show_on_homepage, is_featured, created_at, updated_at)
  - product_serials (id, product_id, serial_number, status, created_at)

-- Shopping & Orders Database
cart_service_db:
  - dealer_carts (dealer_id, product_id, quantity, unit_price, created_at)

order_service_db:
  - orders (id, subtotal, shipping_fee, vat, total, status, created_at, dealer_id)
  - order_items (id, order_id, product_id, quantity, unit_price)

-- Support Services Database
warranty_service_db:
  - warranties (id, product_serial_id, customer_id, start_date, end_date, status)

notification_service_db:
  - notifications (id, title, message, time, read, type, created_at)

blog_service_db:
  - blogs (id, title, content, image, category_id, created_at, updated_at)
  - category_blogs (id, name, description)

report_service_db:
  - reports (id, report_type, data, created_at)
```

### Database Optimization
- **Performance Indexes**: Available in `scripts/database-indexes.sql`
- **Query Optimization**: Indexes for common queries
- **Soft Delete Support**: Logical deletion for products and blogs

## 📚 API Documentation

### Centralized Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Individual Service Docs**: Available on each service port
- **API Standards**: Consistent BaseResponse format across all services

### Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* actual response data */ }
}
```

### Authentication
- **JWT Bearer Token**: Required for protected endpoints
- **Role-based Access**: ADMIN, DEALER, CUSTOMER roles
- **Token Format**: `Authorization: Bearer <jwt_token>`

## 🔒 Security Implementation

### JWT Authentication
- **Algorithm**: RS256 (RSA with SHA-256)
- **Key Distribution**: JWKS endpoint for public key sharing
- **Token Lifecycle**: 30min access + 7day refresh tokens
- **Blacklisting**: Redis-based token invalidation

### Role-Based Access Control
```
ADMIN:
  - All product management operations
  - All user management operations
  - All system administration functions
  - Access to reports and analytics

DEALER:
  - View all dealers (public)
  - Register as dealer (public)
  - Access to wholesale pricing
  - Cart and order management

CUSTOMER:
  - Product browsing (public)
  - Retail purchasing
  - Order tracking
  - Warranty management
```

### API Gateway Security
- **Public Endpoints**: Product browsing, dealer listing, login/registration
- **Protected Endpoints**: Cart operations, admin functions
- **Internal Communication**: Service-to-service with special headers

## 🚀 Configuration & Deployment

### Docker Compose Setup
```bash
# Start all services
docker compose up -d

# Check service status
docker compose ps

# View logs
docker compose logs -f <service-name>

# Rebuild specific service
docker compose up -d --build <service-name>
```

### Infrastructure Services
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **Kafka Cluster**: localhost:9092-9094
- **Kafka UI**: http://localhost:8091
- **Redis Commander**: http://localhost:8090

### Health Checks
All services provide health check endpoints:
```bash
curl http://localhost:808X/actuator/health
```

## 🧹 Code Quality & Recent Improvements

### Latest Updates (September 2025)

#### ✅ **Major Code Cleanup:**
- **Unused Imports Removed**: Cleaned up ProductCreateRequest, ProductController
- **Type Safety Improved**: Fixed BigDecimal/Double mismatches in cart-service
- **Code Standardization**: All comments converted to English
- **Redundant Code Elimination**: Removed obvious comments and empty lines
- **Build Optimization**: All services compile without warnings

#### ✅ **Bug Fixes:**
- **Lombok Issues Resolved**: Added manual getters for exception classes
- **Generic Type Issues Fixed**: BaseResponse generic type corrections
- **Method Conflicts Resolved**: Fixed duplicate method signatures in RepositoryUtil
- **Response Consistency**: Standardized BaseResponse usage across all services

#### ✅ **File Organization:**
- **Documentation Cleanup**: Removed outdated API example files
- **Test File Removal**: Cleaned up temporary test files
- **Project Structure**: Optimized directory structure
- **Database Scripts**: Added performance optimization indexes

#### ✅ **Quality Metrics:**
- **Code Reduction**: -1,043 lines of redundant code removed
- **Build Status**: ✅ All services compile successfully
- **Container Health**: ✅ All Docker containers running healthy
- **Code Coverage**: Improved maintainability and readability

### Development Standards
- **Clean Code Principles**: Following SOLID principles
- **Consistent Formatting**: Standardized code style across services
- **Error Handling**: Comprehensive exception handling with BaseResponse
- **Logging**: Structured logging with SLF4J
- **Validation**: Jakarta validation for input validation

## 🚀 Getting Started

### Prerequisites
```bash
Java 17 LTS
Maven 3.6+
Docker & Docker Compose
Git
```

### Quick Start
```bash
# Clone repository
git clone https://github.com/vominhduc11/microservice-parent
cd microservice-parent

# Start all services
docker compose up -d

# Check service status
docker compose ps

# Access API documentation
open http://localhost:8080/swagger-ui/index.html
```

### Testing the System
```bash
# Test product API
curl "http://localhost:8080/api/product/products/showhomepageandlimit4"

# Test authentication
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

### Default Test Accounts
```
Username: admin     | Password: password123 | Role: ADMIN
Username: dealer    | Password: password123 | Role: DEALER
Username: customer  | Password: password123 | Role: CUSTOMER
```

## 📊 System Status

### ✅ **Current Implementation Status:**
- **✅ Auth Service**: Complete JWT implementation with RS256
- **✅ Product Service**: Full catalog with serial tracking and field filtering
- **✅ Cart Service**: Complete dealer cart functionality with pricing tiers
- **✅ User Service**: Dealer management with Kafka event integration
- **✅ Media Service**: Cloudinary integration for file management
- **✅ Blog Service**: Full CMS with category management
- **✅ Notification Service**: Email and WebSocket notifications
- **✅ API Gateway**: Centralized routing and security
- **✅ Infrastructure**: Complete Docker orchestration

### 🔧 **Services Ready for Extension:**
- **Order Service**: Basic structure, ready for business logic
- **Warranty Service**: Framework in place for warranty management
- **Report Service**: Analytics foundation established

### 📈 **System Metrics:**
- **Total Services**: 11 microservices + infrastructure
- **Database Isolation**: 9 separate PostgreSQL databases
- **API Endpoints**: 50+ documented endpoints
- **Security Coverage**: 100% JWT-protected admin operations
- **Documentation**: Complete Swagger documentation
- **Container Health**: All services running successfully

---

**🏢 Enterprise-ready B2B/B2C microservices platform with complete authentication, media management, event-driven notifications, and comprehensive API documentation. Built for scalability, maintainability, and developer productivity.**

*Last Updated: September 20, 2025*
*Version: 2.0.0*
*Architecture: Microservices with Spring Cloud*
*Repository: https://github.com/vominhduc11/microservice-parent*