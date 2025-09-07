# 🚀 Microservices E-commerce Platform

## 📋 Tổng quan dự án
Hệ thống microservices hoàn chỉnh cho ứng dụng e-commerce với kiến trúc phân tán, sử dụng Spring Boot, Spring Cloud và các công nghệ hiện đại.

## 🏗️ Kiến trúc hệ thống

### 🌐 Infrastructure Services
| Service | Port | Mô tả |
|---------|------|-------|
| **PostgreSQL** | 5432 | Database chính |
| **Redis** | 6379 | Cache & Session storage |
| **Redis Commander** | 8090 | Redis management UI |
| **Kafka Cluster** | 9092-9094 | Message streaming (3 brokers) |
| **Kafka UI** | 8091 | Kafka management UI |
| **Zookeeper Cluster** | 2181-2183 | Kafka coordination (3 nodes) |

### ⚙️ Core Services
| Service | Port | Mô tả | Dependencies |
|---------|------|-------|--------------|
| **config-server** | 8888 | Centralized configuration | - |
| **api-gateway** | 8080 | API Gateway & Routing | config-server |

### 📦 Shared Libraries
| Module | Mô tả | Usage |
|--------|-------|-------|
| **common-service** | Shared utilities, DTOs, common entities & configurations | Used as Maven dependency by all business services |

### 🔐 Business Services
| Service | Port | Mô tả | Database | Dependencies |
|---------|------|-------|----------|--------------|
| **auth-service** | 8081 | Authentication & JWT | auth_service_db | config-server, postgres, redis |
| **user-service** | 8082 | User management | user_service_db | config-server, postgres, auth-service |
| **product-service** | 8083 | Product catalog | product_service_db | config-server, postgres |
| **cart-service** | 8084 | Shopping cart | cart_service_db | config-server, postgres, redis, product-service, user-service |
| **order-service** | 8085 | Order processing | order_service_db | config-server, postgres, cart-service, product-service, user-service |
| **warranty-service** | 8086 | Warranty management | warranty_service_db | config-server, postgres, product-service, order-service |
| **notification-service** | 8087 | Notifications | notification_service_db | config-server, redis |
| **blog-service** | 8088 | Blog & Content | blog_service_db | config-server, postgres, user-service |
| **report-service** | 8089 | Analytics & Reports | report_service_db | config-server, postgres, order-service, product-service, user-service |

## 🛠️ Tech Stack

### Backend Framework
- **Spring Boot 3.4.6** - Application framework
- **Spring Cloud 2024.0.0** - Microservices infrastructure
- **Java 17** - Programming language

### Database & Cache
- **PostgreSQL 15** - Primary database
- **Redis 7** - Caching & session storage

### Message Streaming
- **Apache Kafka 7.4.0** - Event streaming platform
- **Zookeeper 7.4.0** - Distributed coordination

### Key Libraries
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Config** - Configuration management
- **Spring Data JPA** - Database access
- **Spring Data Redis** - Redis integration
- **OpenFeign** - Service-to-service communication
- **Spring Kafka** - Kafka integration
- **Spring Security + JWT** - Authentication & authorization
- **Spring Boot Actuator** - Monitoring & health checks

## 🔄 Service Communication
- **HTTP REST APIs** - Synchronous communication via OpenFeign
- **Apache Kafka** - Asynchronous event-driven communication
- **API Gateway** - Single entry point for external clients
- **Load Balancer** - Client-side load balancing

## 🗄️ Database Schema
Each service has its own dedicated database:
```
auth_service_db      -> Authentication data
user_service_db      -> User profiles & info
product_service_db   -> Product catalog
cart_service_db      -> Shopping carts
order_service_db     -> Orders & transactions
warranty_service_db  -> Warranty records
notification_service_db -> Notification logs
blog_service_db      -> Blog posts & content
report_service_db    -> Analytics data
```

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17
- Maven 3.6+

### 0. Build Common Library
```bash
cd common-service
mvn clean install
cd ..
```

### 1. Start Infrastructure
```bash
docker-compose up postgres redis kafka1 kafka2 kafka3 zookeeper1 zookeeper2 zookeeper3
```

### 2. Start Core Services
```bash
docker-compose up config-server
docker-compose up api-gateway
```

### 3. Start Business Services
```bash
docker-compose up auth-service user-service product-service
docker-compose up cart-service order-service warranty-service
docker-compose up notification-service blog-service report-service
```

*Note: All services depend on common-service library which contains shared utilities, DTOs, and configurations*

### 4. Access Services
- **API Gateway**: http://localhost:8080
- **Config Server**: http://localhost:8888
- **Redis Commander**: http://localhost:8090 (admin/admin123)
- **Kafka UI**: http://localhost:8091

## 📊 Monitoring & Health
All services expose actuator endpoints:
- Health check: `http://localhost:{port}/actuator/health`
- Service info: `http://localhost:{port}/actuator/info`

## 🔧 Configuration
- **Centralized config** via config-server
- **Environment variables** for deployment flexibility
- **Optional config import** for graceful startup

## 📝 Development Notes
- **common-service** is a shared Maven library containing:
  - Common DTOs (Data Transfer Objects)
  - Shared entities and base classes
  - Utility functions and helpers
  - Common configurations and constants
  - Validation annotations and error handling
- All business services depend on common-service for code reuse and consistency
- Build common-service first before building other services
- All services use **optional config import** for independent startup
- **Database per service** pattern for data isolation
- **Event-driven architecture** with Kafka for loose coupling
- **Circuit breaker pattern** ready via OpenFeign
- **Distributed tracing** ready for implementation

## 🔐 Security
- **JWT-based authentication** via auth-service
- **Service-to-service security** configurable
- **API Gateway** as security enforcement point

---
*Generated on September 6, 2025*
