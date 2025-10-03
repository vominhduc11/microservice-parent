# E-Commerce Microservices Platform

Enterprise-grade e-commerce platform built with **microservices architecture**, featuring Spring Boot backend services and modern React/Next.js frontends.

## 📋 Quick Links

- **[📖 Complete Project Overview](./PROJECT_OVERVIEW.md)** - Detailed architecture, API reference, and implementation guide
- **[🗄️ Database Schema](./entity-relationship-diagram.drawio)** - Entity relationship diagram
- **[⚙️ Backend Services Documentation](./backend/README.md)** - All microservices info
- **[🎨 Frontend Applications Documentation](./frontend/README.md)** - All frontend apps info

## 🏗️ Project Structure

```
microservice-parent/
├── backend/                      # Backend microservices
│   ├── api-gateway/              # API Gateway (Port 8080)
│   ├── auth-service/             # Authentication (Port 8081)
│   ├── user-service/             # User Management (Port 8082)
│   ├── product-service/          # Product Catalog (Port 8083)
│   ├── cart-service/             # Shopping Cart (Port 8084)
│   ├── order-service/            # Order Processing (Port 8085)
│   ├── warranty-service/         # Warranty Management (Port 8086)
│   ├── notification-service/     # Notifications (Port 8087)
│   ├── blog-service/             # Blog & CMS (Port 8088)
│   ├── report-service/           # Analytics (Port 8089)
│   ├── media-service/            # Media Upload (Port 8095)
│   ├── config-server/            # Config Server (Port 8888)
│   ├── common-service/           # Shared utilities
│   └── scripts/                  # Database scripts
│
├── frontend/                     # Frontend applications
│   ├── main/                     # Customer Website (Next.js - Port 3000)
│   ├── admin/                    # Admin Dashboard (React - Port 9000)
│   └── dealer/                   # Dealer Portal (React - Port 5174)
│
├── docker-compose.yml            # Complete infrastructure setup
├── PROJECT_OVERVIEW.md           # Detailed documentation
├── entity-relationship-diagram.drawio  # Database ERD
└── README.md                     # This file
```

## 🚀 Quick Start

### Prerequisites

- **Docker** & Docker Compose
- **Java 21** (for local development)
- **Node.js 20+** (for frontend development)
- **Maven 3.9+** (for backend builds)

### 1. Start All Services

```bash
# Start entire platform (all microservices + frontends + infrastructure)
docker-compose up -d

# Or start specific services
docker-compose up api-gateway auth-service product-service main-frontend
```

### 2. Access Applications

| Application | URL | Credentials |
|------------|-----|-------------|
| **Customer Website** | http://localhost:3000 | - |
| **Admin Dashboard** | http://localhost:9000 | admin@example.com / admin123 |
| **Dealer Portal** | http://localhost:5174 | dealer@example.com / dealer123 |
| **API Gateway** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **Redis Commander** | http://localhost:8090 | admin / admin123 |

### 3. Verify Services

```bash
# Check all containers
docker-compose ps

# View logs
docker-compose logs -f [service-name]

# Check API Gateway health
curl http://localhost:8080/actuator/health
```

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.6, Spring Cloud 2024.0.0
- **Database**: PostgreSQL 15 (database per service)
- **Cache**: Redis 7
- **Message Queue**: Apache Kafka 7.4.0 (3-broker cluster)
- **Security**: JWT RS256 with JWKS
- **API Docs**: OpenAPI 3.0 / Swagger
- **Object Mapping**: MapStruct 1.5.5.Final

### Frontend
- **Main Website**: Next.js 14, TypeScript, Tailwind CSS
- **Admin/Dealer**: React 18, Vite, TypeScript, shadcn/ui
- **Web Server**: Nginx (reverse proxy + static serving)

### Infrastructure
- **Containerization**: Docker + Docker Compose
- **Config Management**: Spring Cloud Config Server
- **Service Discovery**: Spring Cloud Gateway
- **Load Balancing**: Nginx
- **Orchestration**: ZooKeeper (Kafka cluster)

## 📊 Architecture

### Microservices Pattern

```
┌─────────────┐
│   Clients   │
│ (Web/Mobile)│
└──────┬──────┘
       │
   ┌───▼────────────────────────────────────┐
   │      API Gateway (Port 8080)           │
   │  - Routing  - Security  - Rate Limit   │
   └───┬────────────────────────────────────┘
       │
   ┌───┴──────────────────────────────────────┐
   │                                           │
┌──▼──────┐  ┌────────┐  ┌─────────┐  ┌─────▼──────┐
│ Auth    │  │Product │  │ Order   │  │ Other      │
│ Service │  │Service │  │ Service │  │ Services   │
└─────────┘  └────────┘  └─────────┘  └────────────┘
   │              │            │              │
┌──▼──────────────▼────────────▼──────────────▼──┐
│          PostgreSQL (15 databases)             │
└────────────────────────────────────────────────┘

         Async Communication via Kafka
                      ↕
         ┌────────────────────────┐
         │  Notification Service  │
         └────────────────────────┘
```

### Key Features

✅ **Database per Service** - Data sovereignty and isolation
✅ **Event-Driven Architecture** - Kafka for async communication
✅ **API Gateway Pattern** - Single entry point
✅ **Config Server** - Centralized configuration
✅ **JWT Security** - RS256 encryption with JWKS
✅ **Docker Deployment** - Production-ready containers
✅ **Multi-Frontend** - Separate apps for different user roles

## 📚 Development

### Backend Development

```bash
# Navigate to a service
cd backend/product-service

# Build
mvn clean install

# Run locally
mvn spring-boot:run

# Run tests
mvn test
```

See [Backend README](./backend/README.md) for details.

### Frontend Development

```bash
# Navigate to an app
cd frontend/main

# Install dependencies
npm install

# Development server
npm run dev

# Build for production
npm run build
```

See [Frontend README](./frontend/README.md) for details.

## 🐳 Docker

### Build Services

```bash
# Build all services
docker-compose build

# Build specific service
docker-compose build product-service

# Build with no cache
docker-compose build --no-cache
```

### Manage Containers

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Restart service
docker-compose restart [service-name]

# View logs
docker-compose logs -f [service-name]

# Remove volumes (⚠️ deletes data)
docker-compose down -v
```

## 🔧 Configuration

### Environment Variables

Key configurations in `docker-compose.yml`:

- **Database**: PostgreSQL credentials
- **Redis**: Cache configuration
- **Kafka**: Broker addresses
- **Services**: Port mappings, URLs

### Config Server

Centralized configs in `backend/config-server/src/main/resources/configs/`:

- `api-gateway.yml`
- `auth-service.yml`
- `product-service.yml`
- ... (one per service)

## 🔐 Security

- **JWT Authentication**: RS256 asymmetric encryption
- **JWKS Endpoint**: Public key distribution at `/auth/.well-known/jwks.json`
- **API Key**: Inter-service authentication
- **CORS**: Configured per service
- **Rate Limiting**: API Gateway level
- **HTTPS**: (Configure Nginx for production)

## 📈 Monitoring

### Health Checks

Each service exposes:
- `/actuator/health` - Service health
- `/actuator/metrics` - Prometheus metrics

### Redis Commander

Monitor Redis cache at: http://localhost:8090

### Database

PostgreSQL exposed on: `localhost:5432`

Connect with:
```bash
psql -h localhost -U postgres -d microservices_db
```

## 🧪 Testing

### Backend Tests
```bash
cd backend/[service-name]
mvn test
```

### Frontend Tests
```bash
cd frontend/[app-name]
npm test
```

### Integration Tests
```bash
# From root
docker-compose -f docker-compose.test.yml up
```

## 📦 Deployment

### Production Checklist

- [ ] Configure production database credentials
- [ ] Set up HTTPS/SSL certificates
- [ ] Configure environment-specific configs in config-server
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure log aggregation (ELK/Loki)
- [ ] Set up CI/CD pipeline
- [ ] Configure backup strategy
- [ ] Set up health check monitoring
- [ ] Configure auto-scaling (if using K8s)

### Kubernetes (Future)

For K8s deployment:
1. Create Kubernetes manifests or Helm charts
2. Configure Ingress for routing
3. Set up persistent volumes for databases
4. Configure secrets management
5. Deploy to cluster

## 🤝 Contributing

### Branch Strategy

- `main` - Production-ready code
- `develop` - Development branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches

### Commit Convention

```
<type>(<scope>): <subject>

Types: feat, fix, docs, style, refactor, test, chore
Example: feat(product-service): add product search API
```

## 📝 API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Detailed Reference**: See [PROJECT_OVERVIEW.md](./PROJECT_OVERVIEW.md)

## 🐛 Troubleshooting

### Common Issues

**Services won't start**
```bash
# Check logs
docker-compose logs [service-name]

# Rebuild
docker-compose up --build --force-recreate
```

**Database connection errors**
```bash
# Verify PostgreSQL is running
docker-compose ps postgres

# Check database logs
docker-compose logs postgres
```

**Port conflicts**
```bash
# Check which process is using port
netstat -ano | findstr :[PORT]  # Windows
lsof -i :[PORT]                 # Linux/Mac
```

## 📄 License

This project is proprietary and confidential.

## 👥 Team

Developed by DevWonder Team

## 📞 Support

For issues or questions:
- Create an issue in the project repository
- Contact: dev@devwonder.com
