# Kubernetes Resources Overview

This document provides a complete overview of all Kubernetes resources in this deployment.

## Infrastructure Components

### StatefulSets

| Name | Replicas | Purpose | Port(s) |
|------|----------|---------|---------|
| `postgres` | 1 | PostgreSQL database for all microservices | 5432 |
| `redis` | 1 | Redis cache for session management | 6379 |
| `zookeeper` | 3 | Zookeeper cluster for Kafka coordination | 2181, 2888, 3888 |
| `kafka` | 3 | Kafka message broker cluster | 9092 |

### Support Tools

| Name | Type | Purpose | Port |
|------|------|---------|------|
| `redis-commander` | Deployment | Redis web UI | 8081 |
| `kafka-ui` | Deployment | Kafka web UI | 8080 |

## Backend Services

### Core Services

| Service | Port | Replicas | Database | Dependencies |
|---------|------|----------|----------|--------------|
| `config-server` | 8888 | 1 | - | - |
| `api-gateway` | 8080 | 2 | - | config-server, all backend services |

### Business Services

| Service | Port | Replicas | Database | Dependencies |
|---------|------|----------|----------|--------------|
| `auth-service` | 8081 | 2 | auth_service_db | postgres, redis, config-server |
| `user-service` | 8082 | 2 | user_service_db | postgres, config-server, auth-service |
| `product-service` | 8083 | 2 | product_service_db | postgres, config-server |
| `cart-service` | 8084 | 2 | cart_service_db | postgres, redis, config-server, product-service, user-service |
| `order-service` | 8085 | 2 | order_service_db | postgres, kafka, config-server, cart-service, product-service, user-service |
| `warranty-service` | 8086 | 2 | warranty_service_db | postgres, config-server, product-service, order-service |
| `notification-service` | 8087 | 2 | notification_service_db | postgres, redis, kafka, config-server |
| `blog-service` | 8088 | 2 | blog_service_db | postgres, config-server, user-service |
| `report-service` | 8089 | 2 | report_service_db | postgres, config-server, order-service, product-service, user-service |
| `media-service` | 8090 | 2 | - | config-server, Cloudinary (external) |

## Frontend Services

| Service | Port | NodePort | Replicas | Purpose |
|---------|------|----------|----------|---------|
| `admin-frontend` | 80 | 30000 | 2 | Admin dashboard |
| `dealer-frontend` | 80 | 30174 | 2 | Dealer portal |
| `main-frontend` | 80 | 30080 | 2 | Main customer-facing website |

## ConfigMaps

| Name | Purpose |
|------|---------|
| `common-config` | Common environment variables for all services |
| `postgres-config` | PostgreSQL connection configuration |
| `redis-config` | Redis connection configuration |
| `kafka-config` | Kafka broker connection configuration |
| `zookeeper-config` | Zookeeper connection configuration |
| `service-urls` | Internal service URLs for inter-service communication |
| `postgres-init-script` | Database initialization SQL script |

## Secrets

| Name | Contains |
|------|----------|
| `postgres-secret` | Database credentials |
| `cloudinary-secret` | Cloudinary API credentials |
| `mail-secret` | Email server credentials |
| `redis-commander-secret` | Redis Commander login credentials |

## Storage

### PersistentVolumeClaims

| Name | Size | Used By |
|------|------|---------|
| `postgres-storage-*` | 10Gi | PostgreSQL StatefulSet |
| `zookeeper-data-*` | 5Gi | Zookeeper StatefulSet |
| `zookeeper-logs-*` | 5Gi | Zookeeper StatefulSet |
| `kafka-data-*` | 10Gi | Kafka StatefulSet |

**Note**: PVCs are created dynamically via volumeClaimTemplates in StatefulSets.

## Networking

### Services (ClusterIP)

All backend services are exposed internally via ClusterIP services on their respective ports.

### Ingress Rules

| Host | Backend Service | Port |
|------|----------------|------|
| `admin.microservices.local` | admin-frontend | 80 |
| `dealer.microservices.local` | dealer-frontend | 80 |
| `www.microservices.local` | main-frontend | 80 |
| `api.microservices.local` | api-gateway | 8080 |
| `auth.microservices.local` | auth-service | 8081 |
| `kafka-ui.microservices.local` | kafka-ui | 8080 |
| `redis-commander.microservices.local` | redis-commander | 8081 |

## Resource Requirements

### Minimum Cluster Resources

For development/testing:
- **CPU**: 8 cores minimum
- **Memory**: 16GB RAM minimum
- **Storage**: 50GB minimum

For production:
- **CPU**: 16+ cores recommended
- **Memory**: 32GB+ RAM recommended
- **Storage**: 200GB+ recommended with fast SSD

### Per-Service Estimates

| Component | CPU Request | Memory Request | CPU Limit | Memory Limit |
|-----------|-------------|----------------|-----------|--------------|
| PostgreSQL | 500m | 512Mi | 2000m | 2Gi |
| Redis | 100m | 128Mi | 500m | 512Mi |
| Zookeeper (each) | 250m | 512Mi | 1000m | 1Gi |
| Kafka (each) | 500m | 1Gi | 2000m | 2Gi |
| Backend Service | 200m | 512Mi | 1000m | 1Gi |
| Frontend App | 100m | 128Mi | 500m | 256Mi |

**Note**: Resource limits are not currently set in the manifests. Add them for production use.

## Startup Order

The deployment script ensures the following startup order:

1. **Namespace** and **Secrets**
2. **ConfigMaps**
3. **Storage**
4. **Infrastructure** (PostgreSQL, Redis, Zookeeper, Kafka)
5. **Config Server** (waits for infrastructure)
6. **API Gateway** (waits for Config Server)
7. **Backend Services** (wait for their dependencies)
8. **Frontend Services**
9. **Ingress Rules**

## Health Checks

All services include:
- **Liveness Probes**: Detect if the pod needs to be restarted
- **Readiness Probes**: Detect if the pod is ready to receive traffic

### Probe Configurations

- **Initial Delay**: 30-90 seconds (depending on service)
- **Period**: 5-10 seconds
- **Timeout**: 3-5 seconds
- **Failure Threshold**: 3

## Database Schema

Each service has its own database:

1. `auth_service_db` - Authentication and authorization data
2. `user_service_db` - User profiles and dealer information
3. `product_service_db` - Product catalog and inventory
4. `cart_service_db` - Shopping cart data
5. `order_service_db` - Order management
6. `warranty_service_db` - Warranty registrations
7. `notification_service_db` - Notification history
8. `blog_service_db` - Blog posts and categories
9. `report_service_db` - Analytics and reporting data

## Monitoring Endpoints

All Java Spring Boot services expose:
- `/actuator/health` - Health check endpoint
- `/actuator/info` - Application information
- `/actuator/metrics` - Metrics endpoint (if enabled)
- `/actuator/prometheus` - Prometheus metrics (if configured)

## Scaling Recommendations

### Stateless Services (Can scale horizontally)
- All backend microservices
- All frontend applications
- API Gateway

### Stateful Services (Vertical scaling recommended)
- PostgreSQL (use read replicas for horizontal scaling)
- Redis (use Redis Cluster for horizontal scaling)

### Already Clustered
- Kafka (3 brokers)
- Zookeeper (3 nodes)

## Security Considerations

### Current State (Development)
- Secrets stored in plain YAML files
- No TLS/SSL encryption
- No network policies
- Default service accounts

### Production Recommendations
1. Use external secret management (HashiCorp Vault, AWS Secrets Manager)
2. Enable TLS/SSL for all external endpoints
3. Implement Kubernetes Network Policies
4. Use dedicated service accounts with RBAC
5. Enable Pod Security Standards
6. Implement API rate limiting
7. Use mutual TLS for service-to-service communication

## Backup Strategy

### Critical Data
- PostgreSQL databases (all 9 databases)
- Kafka topics and messages
- Persistent volumes

### Recommended Tools
- Velero for cluster backups
- pgBackRest for PostgreSQL backups
- Kafka MirrorMaker for Kafka replication

## Disaster Recovery

### RTO (Recovery Time Objective)
- Target: < 1 hour
- Strategy: Automated deployment scripts + database backups

### RPO (Recovery Point Objective)
- Target: < 15 minutes
- Strategy: Continuous database replication or frequent backups

## Cost Optimization

### Development Environment
- Use single replicas for most services
- Reduce resource limits
- Use local storage instead of cloud storage
- Share database instances (use schemas instead of separate databases)

### Production Environment
- Use spot instances for stateless workloads
- Implement cluster autoscaling
- Use reserved instances for stateful services
- Monitor and optimize resource usage

## Useful Commands

```bash
# View all resources
kubectl get all -n microservices

# Check pod logs
kubectl logs -n microservices <pod-name> -f

# Execute command in pod
kubectl exec -it -n microservices <pod-name> -- /bin/sh

# Scale deployment
kubectl scale deployment <name> -n microservices --replicas=3

# Port forward to local machine
kubectl port-forward -n microservices svc/<service-name> <local-port>:<service-port>

# Restart a deployment
kubectl rollout restart deployment/<name> -n microservices

# Check rollout status
kubectl rollout status deployment/<name> -n microservices
```
