# 🚀 Enterprise Microservices E-commerce Platform

## 📋 Tổng quan dự án
Hệ thống microservices enterprise-grade cho ứng dụng e-commerce với kiến trúc phân tán hiện đại. Được xây dựng với Spring Boot 3.4.6, Spring Cloud 2024.0.0, và tích hợp đầy đủ các công nghệ cloud-native. Hệ thống bảo mật toàn diện với JWT authentication và gateway-based authorization.

## 🏗️ Kiến trúc hệ thống

### 🌐 Infrastructure Services
| Service | Port | Container | Mô tả | Health Check |
|---------|------|-----------|-------|--------------|
| **PostgreSQL** | 5432 | postgres-db | Primary database với auto-init schemas | `pg_isready` |
| **Redis** | 6379 | redis-cache | Cache & Session storage | `redis-cli ping` |
| **Redis Commander** | 8090 | redis-commander | Redis management UI | HTTP health |
| **Kafka Cluster** | 9092-9094 | kafka1-3 | Message streaming (3 brokers) | Kafka broker health |
| **Kafka UI** | 8091 | kafka-ui | Kafka management interface | HTTP health |
| **Zookeeper Cluster** | 2181-2183 | zookeeper1-3 | Kafka coordination (3 nodes) | ZK health |

### ⚙️ Core Platform Services
| Service | Port | Container | Mô tả | Dependencies | Health Check |
|---------|------|-----------|-------|--------------|--------------|
| **config-server** | 8888 | config-server | Centralized configuration management | - | Actuator health |
| **api-gateway** | 8080 | api-gateway | API Gateway, Routing, Security, Swagger Hub | config-server | Gateway health + curl |

### 📦 Shared Libraries
| Module | Version | Mô tả | Included Components |
|--------|---------|-------|---------------------|
| **common-service** | 0.0.1-SNAPSHOT | Shared utilities & base configurations | BaseSecurityConfig, BaseOpenApiConfig, Common DTOs, Utility classes |

### 🔐 Business Services
| Service | Port | Container | Database | Dependencies | Security Model |
|---------|------|-----------|----------|--------------|----------------|
| **auth-service** | 8081 | auth-service | auth_service_db | config-server, postgres, redis | JWT issuer, public auth endpoints |
| **user-service** | 8082 | user-service | user_service_db | config-server, postgres, auth-service | Gateway + JWT validation |
| **product-service** | 8083 | product-service | product_service_db | config-server, postgres | Public read, authenticated write |
| **cart-service** | 8084 | cart-service | cart_service_db | config-server, postgres, redis, product-service | User-specific access |
| **order-service** | 8085 | order-service | order_service_db | config-server, postgres, cart-service | User-specific + admin access |
| **warranty-service** | 8086 | warranty-service | warranty_service_db | config-server, postgres, product-service | Authenticated access |
| **notification-service** | 8087 | notification-service | notification_service_db | config-server, redis | Internal/admin access |
| **blog-service** | 8088 | blog-service | blog_service_db | config-server, postgres, user-service | Public read, auth write |
| **report-service** | 8089 | report-service | report_service_db | config-server, postgres | Admin/manager access |

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

### 📡 Communication & Messaging
- **Apache Kafka 7.4.0** - Event streaming platform (3-broker cluster)
- **Zookeeper 7.4.0** - Distributed coordination (3-node ensemble)
- **Spring Cloud OpenFeign** - Declarative REST client
- **Spring Kafka** - Kafka integration framework

### 📚 API Documentation
- **SpringDoc OpenAPI 3 (v2.6.0)** - API documentation framework
- **Swagger UI** - Interactive API documentation
- **Centralized Documentation Hub** - Single point for all API docs

### 🔧 Operational Stack
- **Docker & Docker Compose** - Containerization platform
- **Spring Boot Actuator** - Production monitoring
- **Spring Cloud Config** - External configuration management
- **Health Check Dependencies** - Startup orchestration

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

## 📚 Centralized API Documentation

### 🎯 Swagger Hub Architecture
- **Single Entry Point**: `http://localhost:8080/swagger-ui/index.html`
- **Service Aggregation**: All 9 services documented in one interface
- **Interactive Testing**: Direct API testing from documentation
- **OpenAPI 3.0 Specs**: Modern API specification standard

### 📋 Documentation Endpoints
```
# Centralized Swagger UI
GET /swagger-ui/index.html

# Individual Service API Docs  
GET /api/{service}/v3/api-docs
GET /api/auth/v3/api-docs
GET /api/user/v3/api-docs
# ... for all 9 services
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

*🏢 Enterprise-grade microservices platform with comprehensive security, monitoring, and cloud-native capabilities*
