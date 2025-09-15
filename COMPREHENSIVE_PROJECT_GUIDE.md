# 🏗️ Microservices E-commerce Platform - Comprehensive Developer Guide

## 📋 Executive Summary

This is an **Enterprise B2B/B2C E-commerce Platform** built with microservices architecture using Spring Boot 3.4.6, Spring Cloud 2024.0.0, and PostgreSQL. The platform supports both wholesale operations for dealers and retail sales for end customers, featuring advanced capabilities like multi-tier pricing, serial number tracking, event-driven notifications, and comprehensive media management.

### 🎯 Business Model
- **B2B Wholesale**: Dealers order wholesale products from manufacturers
- **B2C Retail**: Customers purchase directly at retail prices
- **Multi-tier Pricing**: Different wholesale price tiers based on quantity
- **Serial Number Tracking**: Individual product tracking with warranty management
- **Role-based Access**: Admin, Dealer, and Customer roles with different capabilities

---

## 🏗️ System Architecture Overview

### 🌐 Infrastructure Layer
| Component | Port | Description | Purpose |
|-----------|------|-------------|---------|
| **PostgreSQL 15** | 5432 | Primary database with 9 isolated schemas | Data persistence with database-per-service pattern |
| **Redis 7** | 6379 | Cache and session storage | JWT blacklisting, session management, caching |
| **Kafka Cluster** | 9092-9094 | Event streaming (3 brokers + 3 Zookeeper) | Asynchronous communication and event processing |
| **Redis Commander** | 8090 | Redis management UI | Database monitoring and debugging |
| **Kafka UI** | 8091 | Kafka cluster monitoring | Topic and message monitoring |

### ⚙️ Platform Services
| Service | Port | Description | Dependencies |
|---------|------|-------------|--------------|
| **config-server** | 8888 | Centralized configuration management | None |
| **api-gateway** | 8080 | Single entry point with routing and security | config-server, auth-service |

### 🔐 Business Services Overview
| Service | Port | Database | Key Entities | Primary Functions |
|---------|------|----------|--------------|------------------|
| **auth-service** | 8081 | auth_service_db | Account, Role | JWT authentication, JWKS endpoint, token management |
| **user-service** | 8082 | user_service_db | Customer, Dealer, Admin | User management, dealer registration |
| **product-service** | 8083 | product_service_db | Product, ProductSerial | Product catalog, serial number tracking |
| **cart-service** | 8084 | cart_service_db | ProductOfCart, DealerProductOfCart | Shopping cart management |
| **order-service** | 8085 | order_service_db | Order, OrderItem, DealerOrderItem | Order processing and management |
| **warranty-service** | 8086 | warranty_service_db | Warranty | Warranty tracking and claims |
| **notification-service** | 8087 | notification_service_db | Notification | Email, WebSocket notifications |
| **blog-service** | 8088 | blog_service_db | Blog, CategoryBlog | Content management system |
| **media-service** | 8095 | Stateless | None (Cloudinary hardcoded) | File upload, direct ObjectUtils.asMap() implementation |
| **report-service** | 8089 | report_service_db | Report | Analytics and reporting |

---

## 🔐 Security Architecture

### 🛡️ Authentication Flow
1. **User Login**: POST `/api/auth/login` → JWT token with RS256 signature
2. **Token Validation**: API Gateway validates JWT using JWKS endpoint
3. **Role Authorization**: Service-level access control based on user roles
4. **Token Refresh**: Expired tokens can be refreshed via `/api/auth/refresh`
5. **Logout**: Token blacklisting via Redis for immediate invalidation

### 🔑 Role-Based Access Control
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
  - Order management capabilities

CUSTOMER:
  - Product browsing (public)
  - Retail purchasing
  - Order tracking
  - Warranty management
```

### 🌐 API Gateway Security Rules
- **Public Endpoints**: Product browsing, dealer listing, login/registration
- **ADMIN Only**: Product/user/blog management, notifications, reports
- **Authenticated**: Cart operations, order management, warranty claims
- **Internal**: Service-to-service communication with `X-Gateway-Request` header

---

## 📚 Detailed API Documentation

### 🔐 Auth Service (`/api/auth`) - JWT & Security
```
POST   /api/auth/login                    # User authentication
POST   /api/auth/logout                   # Token invalidation
POST   /api/auth/refresh                  # Token refresh
GET    /api/auth/validate                 # Token validation
GET    /api/auth/.well-known/jwks.json    # JSON Web Key Set
POST   /api/auth/accounts                 # Internal: Account creation
DELETE /api/auth/accounts/{id}            # Internal: Account deletion
```

### 👥 User Service (`/api/user`) - User Management
```
GET    /api/user/dealers                  # List dealers (Public)
POST   /api/user/dealers                  # Dealer registration (Public)
PUT    /api/user/dealers/{id}             # Update dealer (ADMIN)
DELETE /api/user/dealers/{id}             # Delete dealer (ADMIN)
```

### 📦 Product Service (`/api/product`) - Catalog Management
```
# Public Endpoints
GET    /api/product/products/showhomepageandlimit4    # Homepage products
GET    /api/product/products/featuredandlimit1       # Featured products
GET    /api/product/{id}                             # Product details

# Admin Endpoints
GET    /api/product/products              # All products
POST   /api/product/products              # Create product
PATCH  /api/product/products/{id}         # Update product
```

### 📸 Media Service (`/api/media`) - File Management
```
POST   /api/media/upload/image            # Upload image (ADMIN)
       - Content-Type: multipart/form-data
       - Parameters: file (required), folder (optional, default: "images")

POST   /api/media/upload/video            # Upload video (ADMIN)
       - Content-Type: multipart/form-data
       - Parameters: file (required), folder (optional, default: "videos_short")

DELETE /api/media/delete/{publicId}       # Delete media (ADMIN)
       - Parameters: resourceType (query, default: "image")
```
**Implementation**: Direct Cloudinary ObjectUtils.asMap() pattern, hardcoded credentials

### 📢 Notification Service (`/api/notification`) - Messaging
```
GET    /api/notification/notifies         # List notifications (ADMIN)
PATCH  /api/notification/{id}/read        # Mark as read (ADMIN)
WS     /ws/topic/dealer-registrations     # WebSocket notifications
```

### 📝 Blog Service (`/api/blog`) - Content Management
```
GET    /api/blog/blogs/showhomepageandlimit6  # Homepage blogs (Public)
GET    /api/blog/{id}                         # Blog details (Public)
POST   /api/blog/blogs                        # Create blog (ADMIN)
PATCH  /api/blog/{id}                         # Update blog (ADMIN)
DELETE /api/blog/{id}                         # Delete blog (ADMIN)
```

### 🛒 Cart Service (`/api/cart`) - Shopping Cart
```
# TODO: Implementation pending
GET    /api/users/{userId}/cart           # User cart
POST   /api/users/{userId}/cart/items     # Add to cart
DELETE /api/users/{userId}/cart/items     # Remove from cart
```

### 📋 Order Service (`/api/order`) - Order Management
```
# TODO: Implementation pending
GET    /api/orders                        # List orders
POST   /api/orders                        # Create order
PATCH  /api/orders/{orderId}              # Update order
```

### 🛡️ Warranty Service (`/api/warranty`) - Warranty Management
```
# TODO: Implementation pending
GET    /api/warranties/check              # Check warranty status
GET    /api/warranties/{customerId}/purchases # Purchase history
POST   /api/warranties/warranty-requests  # Submit warranty claim
```

### 📊 Report Service (`/api/report`) - Analytics
```
# TODO: Implementation pending
GET    /api/reports/sales                 # Sales reports
GET    /api/reports/dashboard             # Dashboard analytics
```

---

## 💾 Database Architecture

### 🗄️ Database Schema Overview
Each service maintains its own isolated PostgreSQL database following the database-per-service pattern:

```sql
-- Infrastructure Database
auth_service_db:
  - accounts (id, username, password, create_at, update_at)
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
             price, wholesale_price, show_on_homepage, is_featured, created_at, update_at)
  - product_serials (id, product_id, serial_number, status, created_at)

-- Shopping & Orders Database
cart_service_db:
  - product_of_carts (id, id_product, created_at)
  - dealer_product_of_carts (id, product_of_cart_id, id_dealer, quantity)

order_service_db:
  - orders (id, subtotal, shipping_fee, vat, total, status, create_at, id_dealer)
  - order_items (id, order_id, product_id, quantity, unit_price)
  - dealer_order_items (id, order_id, dealer_id, product_id, quantity, unit_price)

-- Support Services Database
warranty_service_db:
  - warranties (id, product_serial_id, customer_id, start_date, end_date, status)

notification_service_db:
  - notifies (id, title, message, time, read, type, created_at)

blog_service_db:
  - blogs (id, title, content, image, category_id, created_at, updated_at)
  - category_blogs (id, name, description)

report_service_db:
  - reports (id, report_type, data, created_at)
```

### 🔗 Cross-Service Data Relationships
```
Account (auth-service) ←→ User Entities (user-service) [via accountId]
Product (product-service) → Cart/Order Items [via productId]
Dealer (user-service) → Orders/Cart [via dealerId]
Product + Serial → Warranty [via productSerialId]
```

---

## 🔄 Event-Driven Architecture

### 📡 Kafka Integration
The platform uses Apache Kafka for asynchronous communication and event processing:

**Active Kafka Topics:**
```
email-notifications:
  - Purpose: Dealer welcome emails
  - Producer: user-service
  - Consumer: notification-service

dealer-socket-notifications:
  - Purpose: Real-time dealer registration alerts
  - Producer: user-service
  - Consumer: notification-service

order-events:
  - Purpose: Order processing notifications
  - Producer: order-service
  - Consumer: notification-service
```

**Event Classes (common-service):**
```java
DealerEmailEvent:
  - accountId, username, password, companyName
  - email, phone, address, city, district
  - registrationTime

DealerSocketEvent:
  - dealerId, companyName, registrationTime
  - notificationType, message
```

### 🔔 Notification Channels
1. **📧 Email**: SMTP integration for dealer welcome emails
2. **🔌 WebSocket**: Real-time notifications via `/ws/topic/dealer-registrations`
3. **💾 Database**: Persistent notification storage for admin dashboard

---

## 🛠️ Technology Stack Deep Dive

### 🏢 Core Framework Stack
```
Spring Boot: 3.4.6 (Latest LTS)
Spring Cloud: 2024.0.0 (Latest Greenwich)
Java: 17 LTS (OpenJDK recommended)
Maven: 3.6+ (Dependency management)
```

### 🗄️ Data Layer Technologies
```
PostgreSQL: 15 (Primary database)
  - 9 isolated databases (microservice pattern)
  - JSONB support for flexible schemas
  - Connection pooling via HikariCP

Redis: 7 (Caching & Sessions)
  - JWT token blacklisting
  - Session storage
  - Application-level caching

JPA/Hibernate: 6.x
  - Entity mapping and relationship management
  - Automatic schema generation (dev)
  - Custom repository patterns
```

### 📡 Communication Layer
```
Apache Kafka: 7.4.0
  - 3-broker cluster for high availability
  - Event-driven architecture for notifications
  - Asynchronous processing capabilities

Spring Cloud OpenFeign:
  - Service-to-service HTTP communication
  - Declarative REST client
  - Load balancing integration

WebSocket (STOMP):
  - Real-time notifications
  - Admin dashboard updates
```

### 🔒 Security Technologies
```
JWT (JSON Web Tokens):
  - RS256 algorithm with public/private key pairs
  - JWKS endpoint for key distribution
  - Token refresh mechanism

Spring Security: 6.x
  - Reactive security for API Gateway
  - Method-level security
  - CORS configuration

Redis Token Blacklisting:
  - Immediate token invalidation
  - Logout functionality
  - Security breach response
```

### 📚 Documentation & Monitoring
```
SpringDoc OpenAPI: 3.x (Swagger v3)
  - Centralized documentation hub
  - Interactive API testing
  - Automatic schema generation

Spring Boot Actuator:
  - Health check endpoints (/actuator/health)
  - Metrics and monitoring
  - Application information

Docker & Docker Compose:
  - Containerized deployment
  - Multi-service orchestration
  - Development environment consistency
```

### ☁️ External Services
```
Cloudinary (Hardcoded Configuration):
  - Cloud Name: daohufjec
  - API Key: 872317127931114
  - Direct ObjectUtils.asMap() implementation
  - Default folders: "images" for images, "videos_short" for videos
  - Secure upload enabled
  - Base64 endpoint removed - direct file upload only

SMTP (Gmail):
  - Transactional email delivery
  - Dealer welcome emails
  - Notification system
```

---

## 🚀 Development Setup Guide

### 📋 Prerequisites
```bash
Java 17 LTS (OpenJDK recommended)
Maven 3.6+
Docker & Docker Compose
Git
IDE (IntelliJ IDEA, VS Code, Eclipse)
```

### 🐳 Quick Start Commands
```bash
# Clone repository
git clone <repository-url>
cd microservice-parent

# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f <service-name>

# Stop all services
docker-compose down

# Rebuild specific service
docker-compose up -d --build <service-name>
```

### 🔍 Health Check Endpoints
```bash
# Infrastructure
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8080/actuator/health  # API Gateway

# Business Services
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # User Service
curl http://localhost:8083/actuator/health  # Product Service
# ... etc for all services
```

### 🌐 Key Access Points
```bash
# Main Application
http://localhost:8080/swagger-ui/index.html   # Centralized API Documentation
http://localhost:8080/api/auth/login          # Login Endpoint

# Individual Service Documentation
http://localhost:8095/swagger-ui.html         # Media Service (hardcoded Cloudinary)

# Media Service Test
POST http://localhost:8080/api/media/upload/image
- Headers: Authorization: Bearer <JWT_TOKEN>
- Body: multipart/form-data with 'file' field
- Optional: folder parameter (default: "images")

# Infrastructure Management
http://localhost:8090                         # Redis Commander (admin/admin123)
http://localhost:8091                         # Kafka UI
```

### 🔑 Default Test Accounts
```
Username: admin     | Password: password123 | Role: ADMIN
Username: dealer    | Password: password123 | Role: DEALER
Username: customer  | Password: password123 | Role: CUSTOMER
```

---

## 📊 Current Implementation Status

### ✅ Completed Features
- **🔐 Authentication System**: Complete JWT/JWKS implementation with RS256
- **👥 User Management**: Full dealer CRUD operations with account integration
- **📦 Product Management**: Complete catalog with serial number tracking
- **📸 Media Management**: Cloudinary direct integration with hardcoded credentials
  - Implementation: `Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(...))`
  - Default folders: images/, videos_short/
  - No environment variables - production-ready configuration
- **📝 Blog System**: Full CMS with category management
- **🌐 API Gateway**: Centralized routing, security, and documentation
- **📧 Event System**: Kafka-based notifications for dealer registration
- **🏗️ Infrastructure**: Complete Docker orchestration with 12 services
- **📚 Documentation**: Centralized Swagger UI with comprehensive API docs

### 📋 Pending Implementation (TODO)
- **🛒 Cart Service**: Shopping cart functionality
- **📋 Order Service**: Order processing and management
- **🛡️ Warranty Service**: Warranty tracking and claims
- **📊 Report Service**: Analytics and business intelligence
- **👤 Customer Registration**: Customer account creation endpoints
- **🔧 Admin Management**: Admin user creation and management

### 🎯 Architecture Highlights
1. **Database-per-Service**: Complete data isolation between services
2. **Event-Driven Design**: Kafka integration for asynchronous processing
3. **Hybrid Communication**: HTTP for CRUD operations, Kafka for events
4. **External Storage**: Cloudinary for scalable media management
5. **Centralized Security**: API Gateway with JWT validation
6. **Service Independence**: Each service can be deployed independently
7. **Comprehensive Monitoring**: Health checks and actuator endpoints
8. **Clean Code Practices**: MapStruct mapping, Lombok annotations, validation

---

## 🔧 Development Guidelines

### 📝 Code Conventions
```java
// Entity Mapping
@Entity
@Table(name = "table_name")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EntityName {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... fields
}

// Controller Pattern
@RestController
@RequestMapping("/service")
@Tag(name = "Service Name", description = "Service description")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService service;
    // ... endpoints with full Swagger documentation
}

// Service Layer
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceName {
    private final Repository repository;
    // ... business logic
}
```

### 🏗️ Architecture Patterns Used
- **Database-per-Service**: Isolated data storage
- **API Gateway Pattern**: Single entry point
- **Event Sourcing**: Kafka for state changes
- **CQRS**: Command-Query separation
- **Circuit Breaker**: Fault tolerance (via OpenFeign)
- **Service Discovery**: Config server integration

### 🔒 Security Best Practices
- **JWT with RS256**: Asymmetric key signing
- **Token Blacklisting**: Immediate invalidation capability
- **Role-based Access**: Granular permissions
- **Internal Headers**: Service-to-service authentication
- **CORS Configuration**: Multi-frontend support
- **Input Validation**: Jakarta validation annotations

---

## 📈 Future Roadmap

### 🎯 Short Term (1-2 months)
- Complete cart service implementation
- Implement order processing workflows
- Add warranty management features
- Build comprehensive reporting dashboard

### 🚀 Medium Term (3-6 months)
- Customer portal development
- Advanced analytics and insights
- Mobile API optimization
- Performance monitoring implementation

### 🌟 Long Term (6+ months)
- Machine learning for product recommendations
- Advanced inventory management
- Multi-tenant support
- Kubernetes deployment

---

## 📞 Support & Documentation

### 📚 Additional Resources
- **Swagger Documentation**: `http://localhost:8080/swagger-ui/index.html`
- **Database Schemas**: See individual service entity packages
- **Configuration Files**: `config-server/src/main/resources/configs/`
- **Docker Setup**: `docker-compose.yml` in project root

### 🐛 Troubleshooting
- **Service Won't Start**: Check dependencies in docker-compose.yml
- **Database Connection**: Verify PostgreSQL container is healthy
- **JWT Issues**: Check auth-service logs and JWKS endpoint
- **Kafka Problems**: Verify all 3 brokers are running

### 🔧 Development Tools
- **Redis Commander**: Monitor Redis data and cache
- **Kafka UI**: View topics, messages, and consumer groups
- **PostgreSQL Admin**: Use pgAdmin or DataGrip for database management
- **API Testing**: Use Swagger UI or Postman collections

---

**🏢 Enterprise-ready B2B/B2C microservices platform with complete authentication, media management, event-driven notifications, and comprehensive API documentation. Built for scalability, maintainability, and developer productivity.**

*Last Updated: September 15, 2025*
*Version: 1.0.0*
*Architecture: Microservices with Spring Cloud*