# 🚀 Microservices E-commerce Platform - Project Overview

## 📋 Project Summary
**Enterprise B2B/B2C E-commerce Platform** với kiến trúc microservices được xây dựng bằng Spring Boot 3.4.6, Spring Cloud 2024.0.0, và PostgreSQL.

### 🎯 Business Model
- **B2B Wholesale**: Dealers đặt hàng bán buôn từ nhà sản xuất
- **B2C Retail**: Khách hàng mua lẻ trực tiếp
- **Multi-tier Pricing**: Giá bán buôn vs bán lẻ khác nhau
- **Serial Number Tracking**: Theo dõi sản phẩm theo số serial

---

## 🏗️ System Architecture

### 🌐 Infrastructure Services
| Service | Port | Description | Status |
|---------|------|-------------|--------|
| **PostgreSQL 15** | 5432 | Primary database (9 isolated databases) | ✅ |
| **Redis 7** | 6379 | Cache & session storage | ✅ |
| **Kafka Cluster** | 9092-9094 | Event streaming (3 brokers) | ✅ |
| **Zookeeper Cluster** | 2181-2183 | Kafka coordination (3 nodes) | ✅ |

### ⚙️ Platform Services  
| Service | Port | Description | Status |
|---------|------|-------------|--------|
| **config-server** | 8888 | Centralized configuration | ✅ |
| **api-gateway** | 8080 | API Gateway + Swagger Hub | ✅ |

### 🔐 Business Services
| Service | Port | Database | Core Entities | Status |
|---------|------|----------|---------------|--------|
| **auth-service** | 8081 | auth_service_db | Account, Role | ✅ |
| **user-service** | 8082 | user_service_db | Customer, Dealer, Admin | ✅ |
| **product-service** | 8083 | product_service_db | Product, ProductSerial | ✅ |
| **cart-service** | 8084 | cart_service_db | ProductOfCart, DealerProductOfCart | ✅ |
| **order-service** | 8085 | order_service_db | Order, OrderItem, DealerOrderItem | ✅ |
| **warranty-service** | 8086 | warranty_service_db | Warranty | ✅ |
| **notification-service** | 8087 | notification_service_db | Notification | ✅ |
| **blog-service** | 8088 | blog_service_db | Blog, CategoryBlog | ✅ |
| **report-service** | 8089 | report_service_db | Report | ✅ |

---

## 🔐 Security Architecture

### 🛡️ Authentication & Authorization
- **JWT/JWKS**: RS256 signing với JWKS endpoint
- **3-tier Role System**: ADMIN, DEALER, CUSTOMER
- **Gateway-based Security**: Centralized JWT validation
- **Service-to-Service**: X-Gateway-Request header validation

### 🔑 Default Test Accounts
```
Username: admin     | Password: password123 | Role: ADMIN
Username: dealer    | Password: password123 | Role: DEALER  
Username: customer  | Password: password123 | Role: CUSTOMER
```

### 🌐 API Gateway Security Rules
```java
// Public endpoints
GET /api/product/* (homepage, featured products)
POST /api/auth/login, /api/auth/refresh
GET /api/user/dealers, POST /api/user/dealers

// ADMIN only endpoints
PUT /api/user/dealers/{id}
DELETE /api/user/dealers/{id}
GET /api/notification/notifies
POST /api/product/products
PATCH /api/blog/{id}
```

---

## 📚 API Endpoints Overview

### 🔐 Auth Service (`/api/auth`) - ✅ COMPLETE
```
POST   /api/auth/login                    # User login with JWT
POST   /api/auth/logout                   # Token invalidation
POST   /api/auth/refresh                  # JWT token refresh
GET    /api/auth/validate                 # Token validation
GET    /api/auth/.well-known/jwks.json    # JWKS endpoint
POST   /api/auth/accounts                 # Internal: Create account
DELETE /api/auth/accounts/{id}            # Internal: Delete account
```

### 👥 User Service (`/api/user`) - ✅ COMPLETE  
```
GET    /api/user/dealers                  # List all dealers (Public)
POST   /api/user/dealers                  # Dealer registration (Public)
PUT    /api/user/dealers/{id}             # Update dealer (ADMIN)
DELETE /api/user/dealers/{id}             # Delete dealer + account (ADMIN)
POST   /api/users/customers               # Customer registration (TODO)
POST   /api/users/admins                  # Admin creation (TODO)
```

### 📦 Product Service (`/api/product`) - 🔄 PARTIAL
```
# ✅ Implemented
GET    /api/product/products/showhomepageandlimit4  # Homepage products (Public)
GET    /api/product/products/featuredandlimit1     # Featured products (Public)  
GET    /api/product/{id}                           # Product details (Public)

# ❌ In Trouble  
POST   /api/product/products              # Create product (ADMIN)
PATCH  /api/product/products/{id}         # Update product (ADMIN)
DELETE /api/product/products/{id}         # Delete product (ADMIN)
```

### 📢 Notification Service (`/api/notification`) - ✅ COMPLETE
```
GET    /api/notification/notifies         # List notifications (ADMIN)
PATCH  /api/notification/{id}/read        # Mark as read (ADMIN)
WS     /ws/topic/dealer-registrations     # WebSocket notifications
```

### 📝 Blog Service (`/api/blog`) - 🔄 PARTIAL
```
# ❌ In Trouble
GET    /api/blog/blogs/showhomepageandlimit6  # Homepage blogs (Public)
GET    /api/blog/{id}                         # Blog details (Public)
POST   /api/blog/blogs                        # Create blog (ADMIN)
PATCH  /api/blog/{id}                         # Update blog (ADMIN)
DELETE /api/blog/{id}                         # Delete blog (ADMIN)
```

### 🛒 Cart Service (`/api/cart`) - 📋 TODO
```
GET    /api/users/{userId}/cart           # User cart
POST   /api/users/{userId}/cart/items     # Add to cart
DELETE /api/users/{userId}/cart/items     # Remove from cart
```

### 📋 Order Service (`/api/order`) - 📋 TODO  
```
GET    /api/orders                        # List orders
POST   /api/orders                        # Create order
PATCH  /api/orders/{orderId}              # Update order
```

### 🛡️ Warranty Service (`/api/warranty`) - 📋 TODO
```
GET    /api/warranties/check              # Check warranty
GET    /api/warranties/{customerId}/purchases # Purchase history
POST   /api/warranties/warranty-requests  # Submit warranty claim
```

### 📊 Report Service (`/api/report`) - 📋 TODO
```
GET    /api/reports/sales                 # Sales reports
GET    /api/reports/dashboard             # Dashboard analytics
```

---

## 🔄 Event-Driven Architecture

### 📡 Kafka Topics & Events
```java
// Dealer Registration Flow
User Service → DealerEmailEvent → Notification Service (Email)
User Service → DealerSocketEvent → Notification Service (WebSocket)

// Event Classes (in common-service)
- DealerEmailEvent: Email notification data
- DealerSocketEvent: Real-time notification data
```

### 🔔 Notification Channels
- **📧 Email**: SMTP integration for dealer welcome emails
- **🔌 WebSocket**: Real-time notifications via `/ws/topic/dealer-registrations`
- **💾 Database**: Persistent notification storage

---

## 💾 Database Schema Overview

### 🗄️ Database-per-Service Pattern
```sql
auth_service_db:      accounts, roles, account_roles
user_service_db:      customers, dealers, admins  
product_service_db:   products, product_serials
cart_service_db:      product_of_carts, dealer_product_of_carts
order_service_db:     orders, order_items, dealer_order_items
warranty_service_db:  warranties
notification_service_db: notifies
blog_service_db:      blogs, category_blogs  
report_service_db:    reports
```

### 🔗 Key Relationships
```
Account (auth-service) ←→ Customer/Dealer/Admin (user-service) [accountId]
Product (product-service) → Cart/Order (cart/order-service) [productId]
Dealer (user-service) → Order/Cart (order/cart-service) [dealerId]
```

---

## 🛠️ Technology Stack

### 🏢 Core Framework
- **Spring Boot**: 3.4.6  
- **Spring Cloud**: 2024.0.0
- **Java**: 17 LTS
- **Maven**: 3.6+

### 🗄️ Data Layer
- **PostgreSQL**: 15 (9 isolated databases)
- **Redis**: 7 (caching & sessions)
- **JPA/Hibernate**: 6.x ORM
- **HikariCP**: Connection pooling

### 📡 Communication
- **Apache Kafka**: 7.4.0 (3-broker cluster)
- **Spring Cloud OpenFeign**: Service-to-service calls
- **WebSocket**: Real-time notifications
- **RESTful APIs**: HTTP communication

### 🔒 Security  
- **JWT/JWKS**: Token-based auth with RS256
- **Spring Security**: 6.x reactive security
- **Gateway Security**: Centralized authorization
- **CORS**: Multi-frontend support

### 📚 Documentation
- **SpringDoc OpenAPI**: 3 (v2.6.0)
- **Swagger UI**: Centralized documentation hub
- **API Gateway Integration**: Single point for all docs

---

## 🚀 Quick Start Commands

### 🐳 Docker Operations
```bash
# Start everything
docker-compose up -d

# Stop all services  
docker-compose down

# Rebuild and restart
docker-compose up -d --build

# Check service status
docker-compose ps
```

### 🔍 Health Checks
```bash
# Infrastructure
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8080/actuator/health  # API Gateway

# Business Services  
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # User Service
# ... all services expose /actuator/health
```

### 🌐 Access Points
```bash
# Main endpoints
http://localhost:8080/swagger-ui/index.html  # Swagger Hub
http://localhost:8080/api/auth/login         # Login API
http://localhost:8090                        # Redis Commander
http://localhost:8091                        # Kafka UI
```

---

## 📊 Project Status Summary

### ✅ **Completed Features**
- **Authentication System**: Full JWT/JWKS implementation
- **User Management**: Dealer CRUD with account integration  
- **API Gateway**: Centralized routing, security, documentation
- **Event System**: Kafka-based dealer registration notifications
- **Infrastructure**: Complete Docker orchestration
- **Security**: Gateway-based JWT validation + service authorization
- **Documentation**: Centralized Swagger UI hub

### 🔄 **Partially Implemented**
- **Product Service**: Public endpoints working, admin CRUD "in trouble"
- **Blog Service**: Endpoints defined but "in trouble"

### 📋 **TODO Features**
- Cart Service implementation
- Order Service implementation  
- Warranty Service implementation
- Report Service implementation
- Customer & Admin registration endpoints

### 🎯 **Business Logic Highlights**
1. **Dual Pricing System**: Retail vs Wholesale pricing implemented
2. **Account-Dealer Integration**: Seamless auth + user data relationship
3. **Event-Driven Notifications**: Email + WebSocket dealer alerts
4. **Serial Number Tracking**: Product-level inventory management
5. **RBAC Authorization**: Admin/Dealer/Customer access controls

---

## 🔧 Development Notes

### 🏗️ Architecture Patterns
- **Database-per-Service**: Each service has isolated database
- **API Gateway Pattern**: Centralized entry point with security
- **Event Sourcing**: Kafka-based asynchronous communication
- **Service-to-Service Auth**: Internal headers for security

### 📝 Code Conventions  
- **Common Service**: Shared utilities, exceptions, configs
- **MapStruct**: Entity-to-DTO mapping
- **Lombok**: Boilerplate reduction
- **Validation**: Jakarta validation annotations
- **Logging**: Comprehensive logging with SLF4J

### 🔒 Security Considerations
- **No Soft Delete**: Account entity uses hard delete only
- **Internal Endpoints**: X-Internal-Service header protection
- **CORS Configuration**: Multi-frontend support
- **Token Blacklisting**: Logout token invalidation

---

**🏢 Enterprise-ready B2B/B2C microservices platform with 11 services, complete authentication, event-driven notifications, and comprehensive API documentation.**

*Last Updated: September 12, 2025*