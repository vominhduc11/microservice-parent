# Kubernetes Deployment Guide

Complete guide for deploying the microservices architecture to Kubernetes using Kustomize.

## Table of Contents

- [Quick Start](#quick-start)
- [Directory Structure](#directory-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Resources Overview](#resources-overview)
- [Operations](#operations)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)
- [Advanced Topics](#advanced-topics)
- [Reference](#reference)

---

## Quick Start

Deploy to Kubernetes in 5 minutes:

### 1. Prerequisites Checklist

- [ ] Kubernetes cluster running (Minikube, Kind, or cloud provider)
- [ ] `kubectl` installed and configured
- [ ] Docker images built and pushed to registry
- [ ] NGINX Ingress Controller installed

### 2. Install NGINX Ingress Controller

```bash
# For most Kubernetes clusters
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

# For Minikube
minikube addons enable ingress

# For Docker Desktop
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml
```

### 3. Deploy Using Scripts

```bash
cd k8s
./deploy.sh           # Deploy everything
./status.sh           # Check status
```

### 4. Deploy Using Kustomize

```bash
# Development environment
kubectl apply -k overlays/dev

# Production environment
kubectl apply -k overlays/prod
```

### 5. Access Services

Add to `/etc/hosts`:
```
127.0.0.1  admin.microservices.local dealer.microservices.local www.microservices.local api.microservices.local
```

Open in browser:
- http://admin.microservices.local (Admin Dashboard)
- http://dealer.microservices.local (Dealer Portal)
- http://www.microservices.local (Main Website)
- http://api.microservices.local (API Gateway)

---

## Directory Structure

```
k8s/
├── base/                           # Base configurations (reusable)
│   ├── namespace/                  # Namespace definition
│   │   └── namespace.yaml
│   ├── configmaps/                 # ConfigMaps
│   │   ├── app-config.yaml
│   │   └── postgres-init-script.yaml
│   ├── secrets/                    # Secrets (development only)
│   │   └── app-secrets.yaml
│   ├── storage/                    # Storage classes
│   │   └── storage-class.yaml
│   ├── infrastructure/             # Stateful services
│   │   ├── postgres-statefulset.yaml
│   │   ├── redis-statefulset.yaml
│   │   ├── kafka-statefulset.yaml
│   │   └── zookeeper-statefulset.yaml
│   ├── services/                   # Backend microservices (12 services)
│   │   ├── config-server/          # [deployment, service, kustomization]
│   │   ├── api-gateway/
│   │   ├── auth-service/
│   │   ├── user-service/
│   │   ├── product-service/
│   │   ├── cart-service/
│   │   ├── order-service/
│   │   ├── warranty-service/
│   │   ├── notification-service/
│   │   ├── blog-service/
│   │   ├── report-service/
│   │   └── media-service/
│   ├── frontend/                   # Frontend applications (3 apps)
│   │   ├── admin/
│   │   ├── dealer/
│   │   └── main/
│   ├── autoscaling/                # HPA and resource limits
│   │   ├── hpa.yaml
│   │   ├── apply-autoscaling.sh
│   │   ├── test-load.sh
│   │   └── README.md
│   ├── ingress/                    # Ingress configurations
│   │   └── ingress.yaml
│   └── kustomization.yaml          # Base kustomization
│
├── overlays/                       # Environment-specific configs
│   ├── dev/                        # Development environment
│   │   └── kustomization.yaml      # 1 replica, reduced resources
│   └── prod/                       # Production environment
│       ├── kustomization.yaml      # Multiple replicas, full resources
│       └── loadbalancer-services.yaml
│
├── deploy.sh                       # Deployment script
├── cleanup.sh                      # Cleanup script
├── status.sh                       # Status check script
└── README.md                       # This file
```

### Resource Count

| Type | Count | Description |
|------|-------|-------------|
| Deployments | 17 | 12 backend + 3 frontend + 2 core services |
| Services (ClusterIP) | 21 | Internal service discovery |
| HorizontalPodAutoscalers | 14 | Auto-scaling configurations |
| StatefulSets | 4 | Postgres, Redis, Kafka, Zookeeper |
| ConfigMaps | 7 | Configuration data |
| Secrets | 4 | Sensitive credentials |
| Ingress | 1 | External routing |
| LoadBalancers (prod) | 4 | External access in production |

**Total:** 72 resources (base), 76 resources (prod)

### Structure Benefits

**Before (Old Structure):**
- ❌ All services in monolithic YAML files (500+ lines)
- ❌ Duplicate code everywhere
- ❌ Hard to find/edit specific service
- ❌ No environment separation

**After (New Structure with Kustomize):**
- ✅ Each service in own directory
- ✅ Clear separation of concerns
- ✅ No code duplication
- ✅ Easy to find/edit
- ✅ Environment-specific configs (dev/prod)
- ✅ Resource limits defined
- ✅ Health checks configured
- ✅ Auto-scaling ready

---

## Prerequisites

- **Kubernetes cluster**: v1.20+
- **kubectl**: CLI tool configured
- **kustomize**: Built into kubectl 1.14+
- **Docker registry**: With your images pushed
- **NGINX Ingress Controller**: For external access

---

## Configuration

### 1. Update Image Registry

Replace `your-registry` with your actual registry:

```bash
# Update dev overlay
sed -i 's/your-registry/your-actual-registry/g' overlays/dev/kustomization.yaml

# Update prod overlay
sed -i 's/your-registry/your-actual-registry/g' overlays/prod/kustomization.yaml
```

### 2. Create Secrets

**Important:** Never commit real secrets to git. For production, use external secret management.

```bash
# Postgres credentials
kubectl create secret generic postgres-secret \
  --from-literal=POSTGRES_USER=youruser \
  --from-literal=POSTGRES_PASSWORD=yourpassword \
  -n microservices --dry-run=client -o yaml | kubectl apply -f -

# Mail credentials
kubectl create secret generic mail-secret \
  --from-literal=MAIL_HOST=smtp.gmail.com \
  --from-literal=MAIL_PORT=587 \
  --from-literal=MAIL_USERNAME=your-email \
  --from-literal=MAIL_PASSWORD=your-password \
  -n microservices --dry-run=client -o yaml | kubectl apply -f -

# Cloudinary credentials
kubectl create secret generic cloudinary-secret \
  --from-literal=CLOUDINARY_CLOUD_NAME=yourname \
  --from-literal=CLOUDINARY_API_KEY=yourkey \
  --from-literal=CLOUDINARY_API_SECRET=yoursecret \
  -n microservices --dry-run=client -o yaml | kubectl apply -f -
```

---

## Deployment

### Development Environment

Single replicas with reduced resources:

```bash
# Preview what will be deployed
kubectl kustomize overlays/dev

# Apply to cluster
kubectl apply -k overlays/dev

# Check status
kubectl get pods -n microservices -l environment=dev

# Watch logs
kubectl logs -f -n microservices -l app=user-service,environment=dev
```

### Production Environment

Multiple replicas with full resources and LoadBalancers:

```bash
# Preview what will be deployed
kubectl kustomize overlays/prod

# Apply to cluster
kubectl apply -k overlays/prod

# Check status
kubectl get pods -n microservices -l environment=prod

# Get LoadBalancer IPs (cloud providers)
kubectl get svc -n microservices | grep LoadBalancer
```

### Individual Service Deployment

Deploy specific services independently:

```bash
# Deploy only user-service
kubectl apply -k base/services/user-service

# Deploy only admin frontend
kubectl apply -k base/frontend/admin

# Deploy only infrastructure
kubectl apply -f base/infrastructure/
```

### Environment Differences

| Feature | Dev | Prod |
|---------|-----|------|
| Replicas | 1 | 2-3 |
| Memory Request | 256Mi | 512Mi |
| Memory Limit | 512Mi | 1Gi |
| Image Tag | `dev` | `stable` |
| LoadBalancers | No | Yes (4) |
| Name Prefix | `dev-` | `prod-` |

---

## Resources Overview

### Infrastructure Components

| Name | Type | Replicas | Purpose | Port(s) |
|------|------|----------|---------|---------|
| `postgres` | StatefulSet | 1 | PostgreSQL database | 5432 |
| `redis` | StatefulSet | 1 | Cache & sessions | 6379 |
| `zookeeper` | StatefulSet | 3 | Kafka coordination | 2181, 2888, 3888 |
| `kafka` | StatefulSet | 3 | Message broker | 9092 |

### Backend Services

| Service | Port | Replicas | Database | Dependencies |
|---------|------|----------|----------|--------------|
| `config-server` | 8888 | 1 | - | - |
| `api-gateway` | 8080 | 2 | - | config-server, all services |
| `auth-service` | 8081 | 2 | auth_service_db | postgres, redis, config-server |
| `user-service` | 8082 | 2 | user_service_db | postgres, config-server |
| `product-service` | 8083 | 2 | product_service_db | postgres, config-server |
| `cart-service` | 8084 | 2 | cart_service_db | postgres, redis, config-server |
| `order-service` | 8085 | 2 | order_service_db | postgres, kafka, config-server |
| `warranty-service` | 8086 | 2 | warranty_service_db | postgres, config-server |
| `notification-service` | 8087 | 2 | notification_service_db | postgres, kafka, config-server |
| `blog-service` | 8088 | 2 | blog_service_db | postgres, config-server |
| `report-service` | 8089 | 2 | report_service_db | postgres, config-server |
| `media-service` | 8090 | 2 | - | config-server, Cloudinary |

### Frontend Services

| Service | Port | NodePort | Replicas | Purpose |
|---------|------|----------|----------|---------|
| `admin-frontend` | 80 | 30000 | 2 | Admin dashboard |
| `dealer-frontend` | 80 | 30174 | 2 | Dealer portal |
| `main-frontend` | 80 | 30080 | 2 | Customer website |

### ConfigMaps & Secrets

**ConfigMaps:**
- `common-config` - Common environment variables
- `postgres-config` - PostgreSQL connection config
- `redis-config` - Redis connection config
- `kafka-config` - Kafka broker config
- `zookeeper-config` - Zookeeper config
- `service-urls` - Internal service URLs
- `postgres-init-script` - Database initialization script

**Secrets:**
- `postgres-secret` - Database credentials
- `cloudinary-secret` - Cloudinary API credentials
- `mail-secret` - Email server credentials
- `redis-commander-secret` - Redis Commander login

### Ingress Routes

| Host | Backend Service | Port |
|------|----------------|------|
| `admin.microservices.local` | admin-frontend | 80 |
| `dealer.microservices.local` | dealer-frontend | 80 |
| `www.microservices.local` | main-frontend | 80 |
| `api.microservices.local` | api-gateway | 8080 |

### Startup Order

Deployment follows this order to ensure dependencies are ready:

1. Namespace & Secrets
2. ConfigMaps
3. Storage
4. Infrastructure (Postgres, Redis, Zookeeper, Kafka)
5. Config Server
6. API Gateway
7. Backend Services
8. Frontend Services
9. Ingress Rules

---

## Operations

### Scaling

#### Manual Scaling

```bash
# Scale a specific service
kubectl scale deployment user-service -n microservices --replicas=5

# Scale all services with label
kubectl scale deployment -n microservices -l tier=backend --replicas=3
```

#### Auto-scaling (HPA)

HPA is configured in `base/autoscaling/hpa.yaml`:

```bash
# Apply auto-scaling
cd base/autoscaling
./apply-autoscaling.sh

# Check HPA status
kubectl get hpa -n microservices

# Describe specific HPA
kubectl describe hpa user-service-hpa -n microservices

# Watch HPA in real-time
watch kubectl get hpa -n microservices

# Test auto-scaling
./test-load.sh
```

HPA Configuration:
- Min replicas: 2
- Max replicas: 10
- Target CPU: 70%

### Monitoring

#### Check Pod Status

```bash
# All pods
kubectl get pods -n microservices

# Specific service
kubectl get pods -n microservices -l app=user-service

# Wide output with node info
kubectl get pods -n microservices -o wide

# Watch pods in real-time
watch kubectl get pods -n microservices
```

#### View Logs

```bash
# Single pod
kubectl logs -f pod-name -n microservices

# All pods of a service
kubectl logs -f -n microservices -l app=user-service

# Previous container (if crashed)
kubectl logs --previous pod-name -n microservices

# Last 100 lines
kubectl logs --tail=100 pod-name -n microservices
```

#### Resource Usage

```bash
# Top pods
kubectl top pods -n microservices

# Top nodes
kubectl top nodes

# Sort by memory
kubectl top pods -n microservices --sort-by=memory

# Sort by CPU
kubectl top pods -n microservices --sort-by=cpu
```

#### Describe Resources

```bash
# Describe pod
kubectl describe pod pod-name -n microservices

# Describe deployment
kubectl describe deployment user-service -n microservices

# Describe service
kubectl describe svc user-service -n microservices

# Get events
kubectl get events -n microservices --sort-by='.lastTimestamp'
```

### Updates

#### Update Single Service

```bash
# Edit deployment
kubectl edit deployment user-service -n microservices

# Patch image
kubectl set image deployment/user-service \
  user-service=your-registry/user-service:v2 \
  -n microservices

# Watch rollout
kubectl rollout status deployment/user-service -n microservices
```

#### Update All Services

```bash
# Update overlay image tags
vim overlays/prod/kustomization.yaml

# Apply changes
kubectl apply -k overlays/prod

# Watch rollout of multiple deployments
kubectl rollout status -n microservices deployment/user-service
kubectl rollout status -n microservices deployment/product-service
```

### Rollback

```bash
# View rollout history
kubectl rollout history deployment/user-service -n microservices

# Rollback to previous version
kubectl rollout undo deployment/user-service -n microservices

# Rollback to specific revision
kubectl rollout undo deployment/user-service --to-revision=2 -n microservices

# Check rollout status
kubectl rollout status deployment/user-service -n microservices

# Pause rollout
kubectl rollout pause deployment/user-service -n microservices

# Resume rollout
kubectl rollout resume deployment/user-service -n microservices
```

### Cleanup

```bash
# Remove specific environment
kubectl delete -k overlays/dev
kubectl delete -k overlays/prod

# Or use cleanup script
./cleanup.sh

# Delete entire namespace
kubectl delete namespace microservices

# Delete PVs if needed
kubectl get pv | grep microservices | awk '{print $1}' | xargs kubectl delete pv
```

---

## Troubleshooting

### Pod Not Starting

```bash
# Check events
kubectl get events -n microservices --sort-by='.lastTimestamp'

# Check pod details
kubectl describe pod pod-name -n microservices

# Check logs
kubectl logs pod-name -n microservices

# Check previous logs if restarting
kubectl logs --previous pod-name -n microservices
```

Common issues:
- Image pull errors → Check registry credentials
- CrashLoopBackOff → Check application logs
- Pending → Check resource availability

### Service Not Accessible

```bash
# Check service endpoints
kubectl get endpoints -n microservices

# Check service details
kubectl describe svc user-service -n microservices

# Test service internally
kubectl run -it --rm debug --image=busybox --restart=Never -n microservices -- sh
# Inside pod: wget -O- http://user-service:8082/actuator/health

# Port forward for local testing
kubectl port-forward -n microservices svc/user-service 8082:8082
```

### Database Connection Issues

```bash
# Check postgres pod
kubectl get pods -n microservices | grep postgres

# Check logs
kubectl logs -f postgres-0 -n microservices

# Exec into postgres
kubectl exec -it postgres-0 -n microservices -- psql -U postgres

# List databases
kubectl exec -it postgres-0 -n microservices -- psql -U postgres -c "\l"

# Test connection from service
kubectl exec -it deployment/user-service -n microservices -- \
  sh -c 'nc -zv postgres 5432'
```

### Config Server Issues

```bash
# Check config-server pod
kubectl get pods -n microservices -l app=config-server

# Check logs
kubectl logs -f -n microservices -l app=config-server

# Test health endpoint
kubectl exec -it -n microservices <any-pod> -- \
  wget -O- http://config-server:8888/actuator/health
```

### Ingress Not Working

```bash
# Check if Ingress Controller is running
kubectl get pods -n ingress-nginx

# Check Ingress status
kubectl get ingress -n microservices
kubectl describe ingress -n microservices microservices-ingress

# Check Ingress logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

### High Memory/CPU Usage

```bash
# Check resource usage
kubectl top pods -n microservices --sort-by=memory

# Check HPA status
kubectl get hpa -n microservices

# Check resource limits
kubectl describe pod pod-name -n microservices | grep -A 5 "Limits"

# Adjust resources if needed
kubectl set resources deployment user-service -n microservices \
  --limits=cpu=2,memory=2Gi \
  --requests=cpu=500m,memory=1Gi
```

---

## Best Practices

1. **Always use overlays** - Never modify base directly
2. **Test in dev first** - Validate changes before prod
3. **Use version tags** - Avoid `latest` in production
4. **Monitor resources** - Regular checks with `kubectl top`
5. **Backup data** - PostgreSQL and Redis backups
6. **Use secrets properly** - Never commit real secrets
7. **Review before applying** - Preview with `kubectl kustomize`
8. **Set resource limits** - Prevent resource exhaustion
9. **Use health checks** - Liveness and readiness probes
10. **Label everything** - Consistent labels for filtering

### Production Checklist

- [ ] All secrets properly configured (use external secret manager)
- [ ] Image tags set to specific versions (not `latest`)
- [ ] Resource limits appropriate for workload
- [ ] Health checks configured and working
- [ ] HPA configured for critical services
- [ ] Monitoring and logging set up
- [ ] Backup strategy in place
- [ ] TLS certificates configured (HTTPS)
- [ ] Network policies defined (optional)
- [ ] RBAC configured properly

---

## Advanced Topics

### Kustomize Patches

Customize specific services in overlays:

```yaml
# overlays/prod/kustomization.yaml
patches:
  - patch: |-
      - op: replace
        path: /spec/replicas
        value: 5
    target:
      kind: Deployment
      name: user-service
```

### ConfigMap Generator

Generate ConfigMaps from files:

```yaml
configMapGenerator:
  - name: app-config
    files:
      - application.properties
```

### Multi-Environment Strategy

```bash
# Create staging overlay
mkdir -p overlays/staging
cp overlays/prod/kustomization.yaml overlays/staging/

# Customize for staging
vim overlays/staging/kustomization.yaml

# Deploy to staging
kubectl apply -k overlays/staging
```

### CI/CD Integration

#### GitHub Actions Example

```yaml
name: Deploy to K8s

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Setup kubectl
        uses: azure/setup-kubectl@v1

      - name: Deploy to Dev
        run: kubectl apply -k k8s/overlays/dev

      - name: Wait for rollout
        run: |
          kubectl rollout status deployment/user-service -n microservices
          kubectl rollout status deployment/api-gateway -n microservices
```

#### GitLab CI Example

```yaml
deploy:
  stage: deploy
  script:
    - kubectl apply -k k8s/overlays/prod
    - kubectl rollout status -n microservices deployment/api-gateway
  only:
    - main
```

### Accessing Services

#### Port Forward (Development)

```bash
# API Gateway
kubectl port-forward -n microservices svc/api-gateway 8080:8080

# Admin Frontend
kubectl port-forward -n microservices svc/admin-frontend 8081:80

# PostgreSQL (for debugging)
kubectl port-forward -n microservices svc/postgres 5432:5432
```

#### LoadBalancer (Production)

Get external IPs:

```bash
kubectl get svc -n microservices | grep LoadBalancer
```

### Security

#### Current State (Development)
- ⚠️ Secrets stored in plain YAML files
- ⚠️ No TLS/SSL encryption
- ⚠️ No network policies
- ⚠️ Default service accounts

#### Production Recommendations
1. **External Secret Management**: HashiCorp Vault, AWS Secrets Manager
2. **TLS/SSL**: Enable for all external endpoints
3. **Network Policies**: Implement pod-to-pod restrictions
4. **Service Accounts**: Dedicated accounts with RBAC
5. **Pod Security Standards**: Enforce security policies
6. **API Rate Limiting**: Protect against abuse
7. **Mutual TLS**: Service-to-service encryption

### Backup & Disaster Recovery

#### Backup Strategy

Critical data:
- PostgreSQL databases (9 databases)
- Kafka topics and messages
- Persistent volumes

Tools:
- **Velero**: Cluster and volume backups
- **pgBackRest**: PostgreSQL backups
- **Kafka MirrorMaker**: Kafka replication

```bash
# Example: Backup PostgreSQL with pg_dump
kubectl exec -it postgres-0 -n microservices -- \
  pg_dumpall -U postgres > backup.sql

# Example: Restore
cat backup.sql | kubectl exec -i postgres-0 -n microservices -- \
  psql -U postgres
```

#### Disaster Recovery

- **RTO (Recovery Time Objective)**: < 1 hour
- **RPO (Recovery Point Objective)**: < 15 minutes
- **Strategy**: Automated deployment + database backups

### Cost Optimization

#### Development Environment
- Use single replicas
- Reduce resource limits
- Use local storage
- Share database instances

#### Production Environment
- Use spot instances for stateless workloads
- Implement cluster autoscaling
- Use reserved instances for stateful services
- Monitor and optimize resource usage

---

## Reference

### Resource Requirements

#### Minimum Cluster Resources

**Development:**
- CPU: 8 cores minimum
- Memory: 16GB RAM minimum
- Storage: 50GB minimum

**Production:**
- CPU: 16+ cores recommended
- Memory: 32GB+ RAM recommended
- Storage: 200GB+ SSD recommended

#### Per-Service Estimates

| Component | CPU Request | Memory Request | CPU Limit | Memory Limit |
|-----------|-------------|----------------|-----------|--------------|
| PostgreSQL | 500m | 512Mi | 2000m | 2Gi |
| Redis | 100m | 128Mi | 500m | 512Mi |
| Zookeeper (each) | 250m | 512Mi | 1000m | 1Gi |
| Kafka (each) | 500m | 1Gi | 2000m | 2Gi |
| Backend Service | 200m | 512Mi | 1000m | 1Gi |
| Frontend App | 100m | 128Mi | 500m | 256Mi |

### Useful Commands

```bash
# View all resources
kubectl get all -n microservices

# Get resource usage
kubectl top pods -n microservices
kubectl top nodes

# Check pod logs
kubectl logs -n microservices <pod-name> -f

# Execute command in pod
kubectl exec -it -n microservices <pod-name> -- /bin/sh

# Scale deployment
kubectl scale deployment <name> -n microservices --replicas=3

# Port forward
kubectl port-forward -n microservices svc/<service> <local-port>:<service-port>

# Restart deployment
kubectl rollout restart deployment/<name> -n microservices

# Check rollout status
kubectl rollout status deployment/<name> -n microservices

# Get events
kubectl get events -n microservices --sort-by='.lastTimestamp'

# Describe resource
kubectl describe <resource-type> <name> -n microservices

# Apply kustomization
kubectl apply -k overlays/dev
kubectl apply -k overlays/prod

# Delete kustomization
kubectl delete -k overlays/dev
kubectl delete -k overlays/prod
```

### Health Check Endpoints

All Spring Boot services expose:
- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics (if configured)

### Scaling Recommendations

**Stateless Services** (Horizontal scaling):
- All backend microservices
- All frontend applications
- API Gateway

**Stateful Services** (Vertical scaling):
- PostgreSQL (or use read replicas)
- Redis (or use Redis Cluster)

**Already Clustered**:
- Kafka (3 brokers)
- Zookeeper (3 nodes)

---

## Support

For issues or questions:
- Create an issue in the repository
- Check logs: `kubectl logs -n microservices <pod-name>`
- Review troubleshooting section above

## Additional Scripts

- `deploy.sh` - Automated deployment with dependency ordering
- `cleanup.sh` - Clean up all resources
- `status.sh` - Check deployment status
- `base/autoscaling/apply-autoscaling.sh` - Enable auto-scaling
- `base/autoscaling/test-load.sh` - Test auto-scaling behavior
