# 🚀 Enterprise Microservices E-commerce Platform

## 📋 Project Overview
Enterprise-grade microservices system for **B2B/B2C E-commerce Platform** built with modern distributed architecture. Powered by Spring Boot 3.4.6, Spring Cloud 2024.0.0, and integrated with comprehensive cloud-native technologies.

### 🎯 **Business Model**
- **🏢 B2B Wholesale Platform** - Dealer wholesale ordering from manufacturers
- **🛍️ B2C Retail Platform** - Direct consumer retail purchases
- **📦 Product Management** - Catalog management with serial number tracking
- **🛒 Shopping Cart System** - Separate cart systems for dealers and customers
- **📋 Order Processing** - Multi-status order handling workflow
- **🛡️ Warranty Management** - Product warranty tracking and claims
- **📢 Notification System** - Multi-channel notifications (Email, SMS, Push)
- **📝 Content Management** - Blog and marketing content system
- **📊 Analytics & Reporting** - Business intelligence and analytics

### 🔒 **Security & Authorization**
Comprehensive security system with JWT authentication, role-based access control (RBAC), and gateway-based authorization:
- **👤 CUSTOMER** - End consumers (B2C operations)
- **🏢 DEALER** - Business partners (B2B operations) 
- **🛡️ ADMIN** - System administrators

### 🛠️ **Current Implementation Status**
- ✅ **Architecture**: Complete microservices implementation with 11 services
- ✅ **Database**: Database-per-service pattern with 9 isolated PostgreSQL databases
- ✅ **Authentication**: JWT/JWKS implementation with RSA256 signing
- ✅ **Gateway**: Centralized API Gateway with routing and security
- ✅ **Infrastructure**: Full Docker Compose orchestration with health checks
- ✅ **Documentation**: Centralized Swagger UI hub for all API documentation
- ⚠️ **Known Issue**: Role-based authentication working but needs endpoint-level authorization refinement

## 🏗️ System Architecture

### 🌐 Infrastructure Services
| Service | Port | Container | Description | Health Check |
|---------|------|-----------|-------------|--------------|
| **PostgreSQL 15** | 5432 | postgres-db | Primary database with auto-initialized schemas | `pg_isready -U postgres` |
| **Redis 7** | 6379 | redis-cache | Cache & session storage | `redis-cli ping` |
| **Redis Commander** | 8090 | redis-commander | Redis management UI (admin/admin123) | HTTP health |
| **Kafka Cluster** | 9092-9094 | kafka1-3 | Message streaming platform (3 brokers) | Kafka broker health |
| **Kafka UI** | 8091 | kafka-ui | Kafka management interface | HTTP health |
| **Zookeeper Cluster** | 2181-2183 | zookeeper1-3 | Kafka coordination service (3 nodes) | ZK health check |

### ⚙️ Core Platform Services
| Service | Port | Container | Description | Dependencies | Health Check |
|---------|------|-----------|-------------|--------------|--------------|
| **config-server** | 8888 | config-server | Centralized configuration management for all services | None | `/actuator/health` |
| **api-gateway** | 8080 | api-gateway | API Gateway with routing, security, and Swagger documentation hub | config-server, auth-service | `/actuator/health` |

### 📦 Shared Libraries
| Module | Version | Description | Included Components |
|--------|---------|-------------|---------------------|
| **common-service** | 0.0.1-SNAPSHOT | Shared utilities & base configurations for all services | BaseSecurityConfig, BaseOpenApiConfig, BaseResponse DTO, GlobalExceptionHandler, Custom exceptions |

### 🔐 Business Services
| Service | Port | Container | Database | Core Entities | Business Purpose | Status |
|---------|------|-----------|----------|---------------|------------------|--------|
| **auth-service** | 8081 | auth-service | auth_service_db | Account, Role | JWT authentication, JWKS, role management | ✅ Active |
| **user-service** | 8082 | user-service | user_service_db | Customer, Dealer, Admin | User profiles, B2B/B2C user management | ✅ Active |
| **product-service** | 8083 | product-service | product_service_db | Product, ProductSerial | Catalog management, inventory, pricing (retail/wholesale) | ✅ Active |
| **cart-service** | 8084 | cart-service | cart_service_db | ProductOfCart, DealerProductOfCart | Shopping cart, dealer-specific cart management | ✅ Active |
| **order-service** | 8085 | order-service | order_service_db | Order, OrderItem, DealerOrderItem | Order processing, B2B/B2C order handling | ✅ Active |
| **warranty-service** | 8086 | warranty-service | warranty_service_db | Warranty | Product warranty tracking and management | ✅ Active |
| **notification-service** | 8087 | notification-service | notification_service_db | Notification | Multi-channel notifications (Email, SMS, Push) | ✅ Active |
| **blog-service** | 8088 | blog-service | blog_service_db | Blog, CategoryBlog | Content management, marketing content | ✅ Active |
| **report-service** | 8089 | report-service | report_service_db | Report | Business analytics, sales reporting | ✅ Active |

## 🛠️ Technology Stack

### 🏢 Framework & Platform
- **Spring Boot 3.4.6** - Enterprise application framework
- **Spring Cloud 2024.0.0** - Cloud-native microservices platform  
- **Java 17** - LTS programming language
- **Maven 3.6+** - Dependency management & build tool

### 🔒 Security Stack
- **Spring Security 6.x** - Comprehensive security framework
- **JWT (RS256)** - Stateless token-based authentication
- **Spring Cloud Gateway Security** - Reactive security for gateway
- **BCrypt** - Password hashing algorithm
- **Gateway Header Validation** - Service-to-service security model

### 🗄️ Data & Storage
- **PostgreSQL 15** - Primary relational database
- **Redis 7** - In-memory cache & session storage
- **Database per Service** - Microservices data isolation pattern
- **JPA/Hibernate 6.x** - Object-Relational Mapping
- **HikariCP** - High-performance connection pooling
- **Lombok** - Java boilerplate code reduction

### 📡 Communication & Messaging
- **Apache Kafka 7.4.0** - Event streaming platform (3-broker cluster)
- **Zookeeper 7.4.0** - Distributed coordination (3-node ensemble)
- **Spring Cloud OpenFeign** - Declarative REST client
- **Spring Kafka** - Kafka integration framework
- **RESTful APIs** - HTTP-based service communication

### 📚 API Documentation
- **SpringDoc OpenAPI 3 (v2.6.0)** - API documentation framework
- **Swagger UI** - Interactive API documentation
- **Centralized Documentation Hub** - Single point for all API docs

### 🔧 Operational Stack
- **Docker & Docker Compose** - Containerization platform
- **Spring Boot Actuator** - Production monitoring
- **Spring Cloud Config** - External configuration management
- **Health Check Dependencies** - Startup orchestration

### 📋 Data Modeling & Validation
- **Jakarta Persistence (JPA)** - Java persistence standard
- **Jakarta Validation** - Bean validation framework
- **Hibernate Annotations** - ORM configuration
- **Soft Delete Pattern** - Data retention strategy
- **Audit Timestamps** - Automatic created/updated tracking

## 🔒 Enterprise Security Architecture

### 🛡️ Multi-Layer Security Model
```
Internet → Load Balancer → API Gateway (JWT + CORS) → Services (Gateway Header) → Database
```

### 🔐 Authentication Flow
1. **Client Authentication** → Auth Service (JWT issuing)
2. **API Gateway Validation** → JWT verification with RS256
3. **Service Authorization** → Gateway header validation
4. **Endpoint Security** → Role/permission-based access

### 🏗️ Security Components
- **BaseSecurityConfig** (common-service) - Shared security foundation
- **Gateway Security Filter** - Centralized JWT validation
- **Service Security Configs** - Endpoint-specific authorization
- **CORS Configuration** - Cross-origin resource sharing
- **Rate Limiting** - DDoS protection at gateway level

### 🎯 Security Patterns
```java
// Gateway header validation in all services
public static final String GATEWAY_HEADER_EXPRESSION = 
    "request.getHeader('X-Gateway-Request') == 'true'";
```

### 🔐 JWT & JWKS Implementation
- **JWKS Endpoint**: `/auth/.well-known/jwks.json` - Public key distribution for JWT verification
- **RSA256 Signing**: 2048-bit RSA key pair for JWT signing/verification
- **OAuth2ResourceServer**: Compatible with Spring Security JWT validation
- **Token Expiration**: 24 hours (86400 seconds)
- **Role-based Claims**: JWT contains user roles for authorization
- **CORS Support**: Centralized CORS configuration for API Gateway integration

```http
GET /auth/.well-known/jwks.json
Response: {
  "keys": [{
    "kty": "RSA", "use": "sig", "kid": "key-id",
    "alg": "RS256", "n": "modulus", "e": "exponent"
  }]
}
```

### 🔐 Current Authentication System Status

#### ✅ **Working Components**
- **JWT Token Generation**: RS256 algorithm with proper claims
- **JWKS Endpoint**: Public key distribution for token verification
- **Role Management**: 3-tier role system (ADMIN, DEALER, CUSTOMER)
- **Password Encryption**: BCrypt password hashing
- **Data Initialization**: Automatic creation of default accounts and roles
- **Login Flow**: Complete authentication workflow with token response

#### ⚠️ **Current Issue: Role Authorization**
The authentication system is **functional** but has an identified issue with role-based authorization:

**Issue Description**: 
- JWT tokens are generated correctly with role claims
- Roles are properly stored in the database and retrieved during login
- However, roles may appear as empty array in some authorization contexts

**Default Test Accounts** (created automatically on startup):
```bash
Username: admin     | Password: password123 | Role: ADMIN
Username: dealer    | Password: password123 | Role: DEALER  
Username: customer  | Password: password123 | Role: CUSTOMER
```

**Debug Information Available**:
```java
// In AuthService - roles are logged during authentication
log.info("Account {} has {} roles: {}", account.getUsername(),
         account.getRoles().size(),
         account.getRoles().stream().map(Role::getName).toList());
```

#### 🔧 **Recommended Next Steps**
1. **Test Login Flow**: Use `/api/auth/login` with default accounts
2. **Verify JWT Claims**: Decode generated JWT to confirm role claims
3. **Service-level Authorization**: Implement endpoint-level authorization rules
4. **Gateway Security**: Configure role-based routing in API Gateway

## 🔄 Service Communication Patterns

### 🌐 Synchronous Communication
- **HTTP REST APIs** - Spring Boot RESTful services
- **OpenFeign Clients** - Declarative service-to-service calls
- **Circuit Breaker** - Resilience patterns (ready for implementation)
- **Load Balancing** - Client-side load balancing

### ⚡ Asynchronous Communication  
- **Apache Kafka** - Event-driven architecture
- **Event Sourcing** - Domain event publishing
- **Message Topics** - Service decoupling
- **Kafka Streams** - Real-time data processing (ready)

## 🗄️ Database Architecture

### 📊 Database Per Service Pattern
```sql
# 9 isolated databases for business services
auth_service_db      -- Authentication & user credentials
user_service_db      -- User profiles & preferences  
product_service_db   -- Product catalog & inventory
cart_service_db      -- Shopping cart sessions
order_service_db     -- Order management & history
warranty_service_db  -- Warranty claims & tracking
notification_service_db -- Notification logs & templates
blog_service_db      -- Blog content & CMS
report_service_db    -- Analytics & business intelligence
```

### 🔧 Database Features
- **Automatic Schema Initialization** - Docker entrypoint script
- **Connection Pooling** - HikariCP (Spring Boot default)
- **JPA/Hibernate** - ORM framework
- **Database Migrations** - Ready for Liquibase/Flyway
- **Connection Health Checks** - Docker health monitoring

## 🏢 Domain Model & Entity Architecture

### 👤 **Auth Service Entities**
```java
📋 Account          // User authentication accounts
├── id: Long        // Primary key
├── username: String // Unique username
├── password: String // Encrypted password
├── roles: Set<Role> // User roles (Many-to-Many)
├── enabled: Boolean // Account status
└── timestamps      // Created/Updated/Deleted

🔐 Role             // User roles and permissions
├── id: Long        // Primary key
├── name: String    // Role name (ADMIN, DEALER, CUSTOMER)
└── permissions     // Role-based permissions
```

### 👥 **User Service Entities**
```java
🛡️ Admin            // System administrators
├── accountId: Long  // Links to Account in Auth Service
└── adminData       // Admin-specific information

🏢 Dealer           // Business dealers/partners
├── accountId: Long  // Links to Account in Auth Service
├── companyName     // Company information
├── address, phone  // Contact details
└── city, district  // Location data

👤 Customer         // End customers
├── accountId: Long  // Links to Account in Auth Service
├── name: String    // Customer name
└── email: String   // Contact email
```

### 📦 **Product Service Entities**
```java
🛍️ Product          // Product catalog
├── id: Long        // Primary key
├── sku: String     // Unique product code
├── name: String    // Product name
├── image: String   // Product image URL
├── features: Text  // Product features
├── description: Text // Detailed description
├── videos: Text    // Video links
├── specifications: Text // Technical specs
├── retailPrice: BigDecimal // Customer price
├── wholesalePrice: BigDecimal // Dealer price
├── status: ProductStatus // ACTIVE/INACTIVE/OUT_OF_STOCK
├── soldQuantity: Long // Sales tracking
├── showOnHomepage: Boolean // Featured product
└── productSerials: List<ProductSerial> // Serial numbers

🔢 ProductSerial    // Individual product items
├── id: Long        // Primary key
├── serialNumber    // Unique serial
├── product: Product // Parent product
└── status         // Serial status
```

### 🛒 **Cart Service Entities**
```java
🛍️ ProductOfCart    // Products in shopping cart
├── id: Long        // Primary key
├── idProduct: Long // Reference to Product Service
├── createdAt      // When added to cart
└── dealerProductOfCarts: List // Dealer-specific cart items

🏢 DealerProductOfCart // Dealer-specific cart data
├── id: Long        // Primary key
├── idDealer: Long  // Reference to Dealer
├── quantity: Integer // Requested quantity
└── productOfCart   // Parent cart item
```

### 📋 **Order Service Entities**
```java
📦 Order            // Customer orders
├── id: Long        // Primary key
├── subtotal: BigDecimal // Order subtotal
├── shippingFee: BigDecimal // Shipping cost
├── vat: BigDecimal // Tax amount
├── total: BigDecimal // Final total
├── status: OrderStatus // PENDING/CONFIRMED/PROCESSING/SHIPPED/DELIVERED/CANCELLED
├── createAt: LocalDateTime // Order date
├── idDealer: Long  // Dealer ID
├── orderItems: List<OrderItem> // Standard order items
└── dealerOrderItems: List<DealerOrderItem> // Dealer-specific items

📝 OrderItem        // Individual order items
├── id: Long        // Primary key
├── order: Order    // Parent order
├── productInfo     // Product details snapshot
├── quantity        // Ordered quantity
└── price          // Price at time of order

🏢 DealerOrderItem  // Dealer-specific order items
├── id: Long        // Primary key
├── order: Order    // Parent order
├── dealerInfo      // Dealer details
└── orderData      // Dealer-specific order data
```

### 🛡️ **Warranty Service Entities**
```java
📋 Warranty         // Product warranty records
├── id: Long        // Primary key
├── productInfo     // Product details
├── customerInfo    // Customer information
├── warrantyPeriod  // Warranty duration
├── startDate      // Warranty start
├── endDate        // Warranty expiry
└── status         // WARRANTY_STATUS
```

### 📢 **Notification Service Entities**
```java
📬 Notification     // System notifications
├── id: Long        // Primary key
├── recipientId     // Target user
├── title: String   // Notification title
├── message: Text   // Notification content
├── type: NotificationType // EMAIL/SMS/PUSH/IN_APP
├── status: NotificationStatus // PENDING/SENT/DELIVERED/FAILED
├── scheduledAt     // When to send
└── sentAt         // When actually sent
```

### 📝 **Blog Service Entities**
```java
📖 Blog             // Blog posts/articles
├── id: Long        // Primary key
├── title: String   // Blog title
├── content: Text   // Blog content
├── authorId: Long  // Author reference
├── category: CategoryBlog // Blog category
├── tags: String    // Blog tags
├── status: BlogStatus // DRAFT/PUBLISHED/ARCHIVED
├── publishedAt     // Publication date
└── viewCount      // Read statistics

📚 CategoryBlog     // Blog categories
├── id: Long        // Primary key
├── name: String    // Category name
├── description    // Category description
└── blogs: List<Blog> // Associated blogs
```

### 📊 **Report Service Entities**
```java
📈 Report           // Business analytics reports
├── id: Long        // Primary key
├── reportType      // Report category
├── title: String   // Report title
├── data: JSON      // Report data
├── generatedBy     // Report creator
├── generatedAt     // Creation time
├── parameters     // Report parameters
└── status         // Report status
```

### 🔗 **Cross-Service Relationships**
```
🔄 Service Integration Patterns:
├── Auth Service ←→ User Service (Account ↔ Customer/Dealer/Admin)
├── Product Service → Cart Service (Product references)
├── Cart Service → Order Service (Cart to Order conversion)
├── User Service → Order Service (Dealer/Customer references)
├── Product Service → Warranty Service (Product warranty)
├── Order Service → Notification Service (Order notifications)
├── User Service → Blog Service (Author references)
└── All Services → Report Service (Analytics data)
```

## � Business Workflows & Use Cases

### 🛍️ **B2C Customer Journey**
```mermaid
Customer Registration → Browse Products → Add to Cart → Place Order → Payment → Order Fulfillment → Warranty Registration
```

**Key Workflows:**
1. **Customer Registration** (Auth + User Service)
   - Account creation with customer role
   - Email verification and profile setup
   - Customer-specific pricing access

2. **Product Discovery** (Product + Blog Service)
   - Browse product catalog with retail pricing
   - View product specifications, images, videos
   - Read blog posts and reviews

3. **Shopping Cart** (Cart Service)
   - Add/remove products with retail quantities
   - Calculate retail pricing and shipping
   - Session management and cart persistence

4. **Order Processing** (Order + Notification Service)
   - Order placement with customer details
   - Payment processing integration
   - Order status tracking and notifications

### 🏢 **B2B Dealer Journey**
```mermaid
Dealer Registration → Approval Process → Browse Wholesale → Bulk Orders → Dealer Management → Inventory Planning
```

**Key Workflows:**
1. **Dealer Onboarding** (User + Auth Service)
   - Dealer registration with company details
   - Admin approval workflow
   - Dealer-specific access and pricing

2. **Wholesale Operations** (Product + Cart Service)
   - Access to wholesale pricing tiers
   - Bulk quantity ordering capabilities
   - Dealer-specific product availability

3. **Order Management** (Order Service)
   - Large volume order processing
   - Dealer-specific order history
   - Credit terms and payment management

4. **Business Analytics** (Report Service)
   - Sales performance tracking
   - Inventory turnover reports
   - Commission and profit analysis

### 🛡️ **Admin Operations**
```mermaid
User Management → Product Management → Order Oversight → Analytics → System Monitoring
```

**Key Functions:**
1. **User Administration** (User Service)
   - Manage customer/dealer accounts
   - Role assignment and permissions
   - Account activation/deactivation

2. **Product Catalog Management** (Product Service)
   - Add/edit product information
   - Manage pricing (retail vs wholesale)
   - Inventory tracking and alerts

3. **Order Operations** (Order + Notification Service)
   - Order approval and processing
   - Status updates and tracking
   - Customer/dealer communication

4. **Business Intelligence** (Report Service)
   - Sales and revenue analytics
   - User behavior analysis
   - Performance dashboards

### 🔄 **Event-Driven Workflows**
```
📦 Order Placed → 🔔 Notification Sent → 📊 Analytics Updated
🛍️ Product Sold → 📈 Inventory Updated → 📊 Sales Recorded
👤 User Registered → ✉️ Welcome Email → 📋 Profile Created
🛡️ Warranty Claimed → 🔔 Admin Notified → 📋 Ticket Created
```

## 📚 Centralized API Documentation

### 🎯 Swagger Hub Architecture
- **Single Entry Point**: `http://localhost:8080/swagger-ui/index.html`
- **Service Aggregation**: All 9 services documented in one unified interface
- **Interactive Testing**: Direct API testing from documentation
- **OpenAPI 3.0 Specs**: Modern API specification standard with comprehensive examples

### 📋 API Documentation Endpoints
```bash
# Centralized Swagger UI Hub (Recommended)
GET http://localhost:8080/swagger-ui/index.html

# Individual Service API Documentation
GET http://localhost:8080/api/auth/v3/api-docs        # Auth Service API Spec
GET http://localhost:8080/api/user/v3/api-docs       # User Service API Spec
GET http://localhost:8080/api/product/v3/api-docs    # Product Service API Spec
GET http://localhost:8080/api/cart/v3/api-docs       # Cart Service API Spec
GET http://localhost:8080/api/order/v3/api-docs      # Order Service API Spec
GET http://localhost:8080/api/warranty/v3/api-docs   # Warranty Service API Spec
GET http://localhost:8080/api/notification/v3/api-docs # Notification Service API Spec
GET http://localhost:8080/api/blog/v3/api-docs       # Blog Service API Spec
GET http://localhost:8080/api/report/v3/api-docs     # Report Service API Spec
```

## 🚀 API Endpoints Overview

### 🔐 **Authentication Service** (`/api/auth`)
```bash
# Authentication
POST   /api/auth/login                    # User login with JWT response
GET    /api/auth/.well-known/jwks.json    # JWKS endpoint for JWT verification

# Health & Monitoring
GET    /api/auth/actuator/health          # Service health check
```

### 👥 **User Service** (`/api/user`)
```bash
# Dealer Management
GET    /api/users/dealers                 # List all dealers (public)
POST   /api/users/dealers/register        # Dealer registration (public)
PATCH  /api/users/dealers/{id}            # Update dealer information

# Customer Management  
POST   /api/users/customers               # Customer registration
GET    /api/users/customers/{id}          # Get customer profile
PATCH  /api/users/customers/{id}          # Update customer profile

# Admin Management
POST   /api/users/admins                  # Admin account creation
GET    /api/users/admins/{id}             # Get admin profile
```

### 📦 **Product Service** (`/api/product`)
```bash
# Public Product Endpoints
GET    /api/products                      # Product catalog with filtering
       ?fields=id,name,image,description  # Field selection
       &show_on_homepage=true             # Homepage products
       &is_featured=true                  # Featured products
       &limit=4                           # Pagination
GET    /api/products/{id}                 # Product details (public)

# Admin Product Management
POST   /api/products                      # Create new product
PATCH  /api/products/{id}                 # Update product
DELETE /api/products/{id}                 # Delete product
```

### 🛒 **Cart Service** (`/api/cart`)
```bash
# Customer Cart Management
GET    /api/users/{userId}/cart           # Get user's cart
POST   /api/users/{userId}/cart/items     # Add item to cart
DELETE /api/users/{userId}/cart/items     # Remove item from cart
PATCH  /api/users/{userId}/cart/items/{itemId} # Update cart item quantity

# Dealer-Specific Cart Operations
GET    /api/dealers/{dealerId}/cart       # Get dealer cart
POST   /api/dealers/{dealerId}/cart/bulk  # Bulk add to dealer cart
```

### 📋 **Order Service** (`/api/order`)
```bash
# Order Management
GET    /api/orders                        # List user orders
POST   /api/orders                        # Create new order
GET    /api/orders/{orderId}              # Get order details
PATCH  /api/orders/{orderId}              # Update order status
DELETE /api/orders/{orderId}              # Cancel order

# Order Status Tracking
GET    /api/orders/{orderId}/status       # Get order status
PATCH  /api/orders/{orderId}/status       # Update order status (admin)
```

### 🛡️ **Warranty Service** (`/api/warranty`)
```bash
# Warranty Verification
GET    /api/warranties/check              # Check warranty status
       ?serial_number=ABC123XYZ           # By serial number

# Customer Warranty Management
GET    /api/warranties/{customerId}/purchases  # Customer purchase history
       ?fields=id,name,status,image,serial_number,purchase_date,warranty_until
       ?warranty_expired=false            # Filter active warranties

# Warranty Claims
GET    /api/warranties/warranty-requests  # List warranty requests
POST   /api/warranties/warranty-requests  # Submit warranty claim
```

### 📢 **Notification Service** (`/api/notification`)
```bash
# Notification Management
GET    /api/notifications                 # Get user notifications
POST   /api/notifications                 # Send notification
PATCH  /api/notifications/{id}/read       # Mark as read
DELETE /api/notifications/{id}            # Delete notification

# Admin Notification Operations
POST   /api/notifications/broadcast       # Broadcast notification
GET    /api/notifications/templates       # Notification templates
```

### 📝 **Blog Service** (`/api/blog`)
```bash
# Public Blog Endpoints
GET    /api/blogs                         # Blog post listing
       ?fields=id,title,description,image,category,created_at
       &show_on_homepage=true             # Homepage blogs
       &limit=6                           # Pagination
GET    /api/blogs/{id}                    # Blog post details (public)

# Admin Blog Management
POST   /api/blogs                         # Create blog post
PATCH  /api/blogs/{id}                    # Update blog post
DELETE /api/blogs/{id}                    # Delete blog post

# Category Management
GET    /api/blogs/categories              # List blog categories
POST   /api/blogs/categories              # Create category
```

### 📊 **Report Service** (`/api/report`)
```bash
# Business Analytics
GET    /api/reports/sales                 # Sales reports
GET    /api/reports/inventory             # Inventory reports
GET    /api/reports/customers             # Customer analytics
GET    /api/reports/dealers               # Dealer performance
GET    /api/reports/dashboard             # Dashboard analytics

# Custom Report Generation
POST   /api/reports/generate              # Generate custom report
GET    /api/reports/{reportId}/download   # Download report
```

## 🚀 Quick Start Guide

### 📋 Prerequisites
```bash
# Required Software
- Docker 20.10+ & Docker Compose V2
- Java 17 (OpenJDK recommended)  
- Maven 3.6+
- Git

# System Requirements
- RAM: 8GB minimum, 16GB recommended
- CPU: 4 cores minimum
- Disk: 10GB free space
```

### 🔨 Build Process
```bash
# 1. Clone repository
git clone <repository-url>
cd microservice-parent

# 2. Build shared library first
cd common-service
mvn clean install
cd ..

# 3. Build all services (optional - Docker handles this)
mvn clean package -DskipTests
```

### 🐳 Container Startup Sequence

#### Phase 1: Infrastructure
```bash
# Start core infrastructure
docker-compose up -d postgres redis 

# Start Kafka cluster
docker-compose up -d zookeeper1 zookeeper2 zookeeper3
docker-compose up -d kafka1 kafka2 kafka3

# Start management UIs
docker-compose up -d redis-commander kafka-ui
```

#### Phase 2: Platform Services  
```bash
# Start configuration server (dependency for all services)
docker-compose up -d config-server

# Wait for config-server health check, then start gateway
docker-compose up -d api-gateway
```

#### Phase 3: Business Services
```bash
# Authentication service (required by others)
docker-compose up -d auth-service

# Core business services
docker-compose up -d user-service product-service

# Extended business services  
docker-compose up -d cart-service order-service warranty-service
docker-compose up -d notification-service blog-service report-service
```

#### One-Command Startup (Recommended)
```bash
# Start everything with proper dependency order
docker-compose up -d
```

### 🔍 Service Health Verification
```bash
# Check all container status
docker-compose ps

# Check service health endpoints
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8080/actuator/health  # API Gateway  
curl http://localhost:8081/actuator/health  # Auth Service
# ... for other services
```

## 🌐 Access Points

### 🔧 Management Interfaces
| Interface | URL | Credentials | Purpose |
|-----------|-----|-------------|---------|
| **Swagger Hub** | http://localhost:8080/swagger-ui/index.html | - | Centralized API documentation |
| **Config Server** | http://localhost:8888 | - | Configuration management |
| **Redis Commander** | http://localhost:8090 | admin/admin123 | Redis database management |
| **Kafka UI** | http://localhost:8091 | - | Kafka cluster management |

### 🔌 Service Endpoints
| Service | Base URL | Health Check | Documentation |
|---------|----------|--------------|---------------|
| **API Gateway** | http://localhost:8080 | /actuator/health | Centralized Swagger |
| **Auth Service** | http://localhost:8081 | /actuator/health | /api/auth/v3/api-docs |
| **User Service** | http://localhost:8082 | /actuator/health | /api/user/v3/api-docs |
| **Product Service** | http://localhost:8083 | /actuator/health | /api/product/v3/api-docs |
| **Cart Service** | http://localhost:8084 | /actuator/health | /api/cart/v3/api-docs |
| **Order Service** | http://localhost:8085 | /actuator/health | /api/order/v3/api-docs |
| **Warranty Service** | http://localhost:8086 | /actuator/health | /api/warranty/v3/api-docs |
| **Notification Service** | http://localhost:8087 | /actuator/health | /api/notification/v3/api-docs |
| **Blog Service** | http://localhost:8088 | /actuator/health | /api/blog/v3/api-docs |
| **Report Service** | http://localhost:8089 | /actuator/health | /api/report/v3/api-docs |

## 📊 Monitoring & Observability

### 🏥 Health Checks
```bash
# Infrastructure health  
docker-compose exec postgres pg_isready -U postgres
docker-compose exec redis redis-cli ping

# Service health (all expose actuator endpoints)
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
# ... for all services
```

### 📈 Metrics & Monitoring (Ready for Enhancement)
- **Spring Boot Actuator** - Built-in metrics
- **Prometheus Integration** - Ready for metrics collection
- **Grafana Dashboards** - Ready for visualization
- **Distributed Tracing** - Ready for Zipkin/Jaeger
- **Centralized Logging** - Ready for ELK stack

## 🔧 Configuration Management

### 🎛️ Configuration Hierarchy
```
1. Default application.yml (in each service)
2. Config Server centralized configs
3. Environment variables (Docker)
4. Command line arguments (if any)
```

### 📁 Configuration Structure
```
config-server/src/main/resources/configs/
├── api-gateway.yml      # Gateway routing & security
├── auth-service.yml     # Auth service configuration
├── user-service.yml     # User service configuration
├── product-service.yml  # Product service configuration
├── cart-service.yml     # Cart service configuration
├── order-service.yml    # Order service configuration
├── warranty-service.yml # Warranty service configuration
├── notification-service.yml # Notification configuration
├── blog-service.yml     # Blog service configuration
└── report-service.yml   # Report service configuration
```

### 🔀 Environment Variables
```bash
# Database configuration
DB_HOST=postgres
DB_PORT=5432  
DB_USER=postgres
DB_PASSWORD=postgres

# Service URLs (for inter-service communication)
AUTH_SERVICE_URI=http://auth-service:8081
USER_SERVICE_URI=http://user-service:8082
# ... for all services

# Feature flags
SWAGGER_UI_ENABLED=true
ACTUATOR_ENABLED=true
```

## 🔐 Security Implementation Details

### 🛡️ BaseSecurityConfig Pattern
```java
// Common security foundation for all services
@EnableWebSecurity  
public abstract class BaseSecurityConfig {
    public static final String GATEWAY_HEADER_EXPRESSION = 
        "request.getHeader('X-Gateway-Request') == 'true'";
    
    protected abstract void configureServiceEndpoints(
        AuthorizationManagerRequestMatcherRegistry auth);
}
```

### 🔒 Service-Level Security
```java
// Each service extends BaseSecurityConfig
@Configuration
public class UserServiceSecurityConfig extends BaseSecurityConfig {
    @Override
    protected void configureServiceEndpoints(
        AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/user/**")
            .access(WebExpressionAuthorizationManager
                .hasAuthority("USER").and(GATEWAY_HEADER_EXPRESSION));
    }
}
```

### 🌐 CORS Configuration
```yaml
# Support for multiple frontend frameworks
cors:
  allowed-origins: 
    - http://localhost:3000    # React
    - http://localhost:9000    # Vue.js  
    - http://localhost:5173    # Vite
    - http://localhost:5500    # Live Server
    - http://127.0.0.1:5500
```

## 🔄 Development Workflow

### 🏗️ Building & Testing
```bash
# Build shared library (required first)
cd common-service && mvn clean install

# Build individual service
cd user-service && mvn clean package

# Run tests
mvn test

# Build Docker images
docker-compose build user-service
```

### 🔧 Local Development
```bash
# Start infrastructure only
docker-compose up -d postgres redis config-server

# Run services locally (for debugging)
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run  
```

### 📝 Adding New Services
1. **Create service module** following existing pattern
2. **Add dependency** on common-service in pom.xml
3. **Extend BaseSecurityConfig** for security
4. **Extend BaseOpenApiConfig** for documentation
5. **Add service config** in config-server
6. **Update docker-compose.yml** with new service
7. **Add routes** in api-gateway configuration

## 🚧 Roadmap & Enhancement Opportunities

### 🎯 Phase 1: Core Platform (✅ Complete)
- [x] Microservices architecture
- [x] Security implementation  
- [x] API Gateway with routing
- [x] Centralized configuration
- [x] Database per service
- [x] Docker containerization
- [x] Health checks & monitoring

### 🎯 Phase 2: Advanced Features (🔄 In Progress)
- [x] Kafka event streaming
- [x] Redis caching
- [x] Swagger documentation hub
- [ ] Circuit breaker implementation
- [ ] Rate limiting enhancement
- [ ] API versioning strategy

### 🎯 Phase 3: Production Readiness (📋 Planned)
- [ ] Distributed tracing (Zipkin/Jaeger)
- [ ] Centralized logging (ELK stack)
- [ ] Metrics collection (Prometheus)
- [ ] Monitoring dashboards (Grafana)
- [ ] Database migrations (Liquibase)
- [ ] Integration testing suite
- [ ] Performance testing
- [ ] Security scanning

### 🎯 Phase 4: Cloud Native (🌟 Future)
- [ ] Kubernetes deployment
- [ ] Service mesh (Istio)
- [ ] GitOps pipeline
- [ ] Auto-scaling
- [ ] Backup strategies
- [ ] Disaster recovery

## 🤝 Contributing

### 📝 Development Guidelines
1. **Follow naming conventions** established in existing services
2. **Extend base configurations** (BaseSecurityConfig, BaseOpenApiConfig)
3. **Add comprehensive tests** for new features
4. **Update documentation** for any new endpoints
5. **Follow security patterns** established in the platform

### 🔍 Code Quality
- **SonarQube integration** ready for code quality analysis
- **Checkstyle configuration** for consistent formatting
- **PMD rules** for code standards
- **SpotBugs integration** for bug detection

---

## 📞 Contact & Support

**Project Team**: DevWonder Microservices Team
**Repository**: microservice-parent
**Last Updated**: September 7, 2025
**Version**: 1.0.0-ENTERPRISE
**Domain**: B2B/B2C E-commerce Platform

### 📊 **Project Statistics**
- **Services**: 11 total (2 platform + 9 business)
- **Entities**: 15+ domain entities across services
- **Databases**: 9 isolated databases
- **Infrastructure**: 6 supporting services (PostgreSQL, Redis, Kafka cluster, etc.)
- **Security**: JWT + Role-based access control
- **Documentation**: Centralized Swagger UI hub + Individual service HELP.md files

### 🛠️ **Repository Status**
- ✅ **Build Status**: Fully functional after critical .gitignore fixes
- ✅ **Essential Files**: All pom.xml files recovered and tracked
- ✅ **Documentation**: Complete with 12 service-specific HELP.md files
- ✅ **Git Configuration**: Optimized .gitignore for Maven projects
- ✅ **Dependencies**: All Maven dependencies properly declared
- ✅ **Collaboration Ready**: Project builds successfully from fresh clone
- ✅ **JWKS Implementation**: JWT verification endpoint for OAuth2ResourceServer
- ✅ **CORS Configuration**: Centralized CORS support for API Gateway integration

### � **Business Capabilities**
- ✅ **Multi-tenant B2B/B2C platform**
- ✅ **Product catalog with serial tracking**
- ✅ **Differential pricing (retail/wholesale)**
- ✅ **Shopping cart with dealer-specific features**
- ✅ **Order processing with multiple statuses**
- ✅ **Warranty management system**
- ✅ **Multi-channel notification system**
- ✅ **Content management for marketing**
- ✅ **Business analytics and reporting**

## 🔧 Troubleshooting Guide

### 🚨 **Common Issues & Solutions**

#### **Issue 1: Services failing to start**
```bash
# Problem: Service containers exit with errors
# Solution: Check dependencies and health checks

# 1. Verify infrastructure services are running
docker-compose ps postgres redis config-server

# 2. Check service logs
docker-compose logs auth-service
docker-compose logs api-gateway

# 3. Restart services in dependency order
docker-compose restart config-server
docker-compose restart auth-service
docker-compose restart api-gateway
```

#### **Issue 2: Database connection failures**
```bash
# Problem: Services cannot connect to PostgreSQL
# Solution: Verify database initialization

# 1. Check PostgreSQL status
docker-compose exec postgres pg_isready -U postgres

# 2. Verify databases were created
docker-compose exec postgres psql -U postgres -c "\l"

# 3. Check database logs
docker-compose logs postgres

# 4. Restart PostgreSQL if needed
docker-compose restart postgres
```

#### **Issue 3: JWT Authentication issues**
```bash
# Problem: JWT tokens not working or roles empty
# Solution: Debug authentication flow

# 1. Test login endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# 2. Verify JWKS endpoint
curl http://localhost:8080/api/auth/.well-known/jwks.json

# 3. Check auth-service logs for role information
docker-compose logs auth-service | grep "roles"

# 4. Verify default accounts were created
docker-compose logs auth-service | grep "Created account"
```

#### **Issue 4: API Gateway routing issues**
```bash
# Problem: API requests not routing to services
# Solution: Verify gateway configuration

# 1. Check API Gateway health
curl http://localhost:8080/actuator/health

# 2. Verify service endpoints through gateway
curl http://localhost:8080/api/auth/actuator/health
curl http://localhost:8080/api/user/actuator/health

# 3. Check gateway logs
docker-compose logs api-gateway

# 4. Verify config-server configurations
curl http://localhost:8888/api-gateway/default
```

#### **Issue 5: Swagger UI not accessible**
```bash
# Problem: Cannot access centralized Swagger documentation
# Solution: Verify API Gateway and service configurations

# 1. Access Swagger UI directly
curl -I http://localhost:8080/swagger-ui/index.html

# 2. Check API docs endpoints
curl http://localhost:8080/api/auth/v3/api-docs
curl http://localhost:8080/api/user/v3/api-docs

# 3. Verify service OpenAPI configurations
docker-compose logs auth-service | grep -i swagger
docker-compose logs api-gateway | grep -i openapi
```

### 🔍 **Debugging Commands**

#### **System Health Check**
```bash
# Check all container status
docker-compose ps

# Check system resource usage
docker stats

# View all service logs
docker-compose logs --tail=50 -f

# Check specific service health endpoints
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8080/actuator/health  # API Gateway  
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # User Service
# ... continue for other services
```

#### **Database Debugging**
```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U postgres

# List all databases
\l

# Connect to specific service database
\c auth_service_db

# List tables
\dt

# Check account and role data
SELECT a.username, r.name as role 
FROM accounts a 
JOIN account_roles ar ON a.id = ar.account_id 
JOIN roles r ON ar.role_id = r.id;
```

#### **Network Debugging**
```bash
# Test service-to-service communication
docker-compose exec api-gateway curl http://auth-service:8081/actuator/health
docker-compose exec auth-service curl http://config-server:8888/actuator/health

# Check network configuration
docker network ls
docker network inspect microservice-parent_microservices-network
```

### 🚨 **Emergency Reset Procedures**

#### **Complete System Reset**
```bash
# WARNING: This will delete all data and containers
docker-compose down -v
docker system prune -f
docker volume prune -f

# Restart from scratch
docker-compose up -d
```

#### **Database Reset Only**
```bash
# Reset PostgreSQL data only
docker-compose down
docker volume rm microservice-parent_postgres_data
docker-compose up -d postgres

# Wait for database initialization
docker-compose logs -f postgres
```

#### **Selective Service Restart**
```bash
# Restart specific service group
docker-compose restart config-server api-gateway auth-service

# Rebuild and restart specific service
docker-compose build auth-service
docker-compose up -d auth-service
```

### 📊 **Monitoring & Health Checks**

#### **Service Health Monitoring**
```bash
# Create health check script
#!/bin/bash
services=("config-server:8888" "api-gateway:8080" "auth-service:8081" "user-service:8082")

for service in "${services[@]}"; do
  name=$(echo $service | cut -d: -f1)
  port=$(echo $service | cut -d: -f2)
  
  if curl -f -s "http://localhost:$port/actuator/health" > /dev/null; then
    echo "✅ $name is healthy"
  else
    echo "❌ $name is unhealthy"
  fi
done
```

#### **Performance Monitoring**
```bash
# Monitor resource usage
watch -n 2 'docker stats --no-stream'

# Monitor log output
docker-compose logs -f --tail=10

# Check memory usage by service
docker-compose exec postgres free -h
docker-compose exec redis redis-cli info memory
```

### 📞 **Getting Help**

#### **Log Collection for Support**
```bash
# Collect all logs for analysis
mkdir -p logs/$(date +%Y%m%d_%H%M%S)
cd logs/$(date +%Y%m%d_%H%M%S)

# Export service logs
docker-compose logs config-server > config-server.log
docker-compose logs api-gateway > api-gateway.log
docker-compose logs auth-service > auth-service.log
docker-compose logs postgres > postgres.log
docker-compose logs redis > redis.log

# Export system information
docker-compose ps > containers.txt
docker system df > docker-usage.txt
```

#### **Configuration Validation**
```bash
# Validate Docker Compose file
docker-compose config

# Check service configurations
curl http://localhost:8888/application/default
curl http://localhost:8888/auth-service/default
curl http://localhost:8888/api-gateway/default
```

---

## 📞 Contact & Support

**Project**: Enterprise Microservices E-commerce Platform  
**Repository**: microservice-parent  
**Last Updated**: September 9, 2024  
**Version**: 1.0.0-ENTERPRISE  
**Domain**: B2B/B2C E-commerce Platform

### 📊 **Project Statistics**
- **Services**: 11 total (2 platform + 9 business)
- **Entities**: 17 domain entities across services
- **Databases**: 9 isolated databases with auto-initialization
- **Infrastructure**: 6 supporting services (PostgreSQL, Redis, 3-node Kafka cluster, etc.)
- **Security**: JWT/JWKS with RSA256 + Role-based access control
- **Documentation**: Centralized Swagger UI hub with comprehensive API specs

### 🛠️ **Implementation Status**
- ✅ **Architecture**: Complete microservices implementation
- ✅ **Database**: Database-per-service pattern with health checks
- ✅ **Authentication**: JWT/JWKS with role-based authentication
- ✅ **Gateway**: Centralized routing, security, and documentation hub
- ✅ **Infrastructure**: Full Docker orchestration with dependency management
- ✅ **Documentation**: Comprehensive API documentation and troubleshooting guides
- ⚠️ **Authorization**: Role-based authentication implemented, endpoint-level authorization ready for enhancement

### 🏢 **Business Capabilities**
- ✅ **Multi-tenant B2B/B2C platform** with role separation
- ✅ **Product catalog** with serial number tracking and dual pricing
- ✅ **Shopping cart system** with dealer-specific features
- ✅ **Order processing** with multi-status workflow
- ✅ **Warranty management** with claims tracking
- ✅ **Multi-channel notifications** (Email, SMS, Push ready)
- ✅ **Content management** for marketing and blogs
- ✅ **Business analytics** and reporting system
- ✅ **Event-driven architecture** with Kafka messaging

*🏢 Production-ready enterprise microservices platform with comprehensive B2B/B2C e-commerce capabilities, advanced security, monitoring, and cloud-native architecture*