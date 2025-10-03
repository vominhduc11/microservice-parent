# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the microservices application to a Kubernetes cluster.

## Prerequisites

1. **Kubernetes Cluster**: A running Kubernetes cluster (can be local with Minikube, kind, or cloud-based like EKS, GKE, AKS)
2. **kubectl**: Kubernetes command-line tool installed and configured
3. **NGINX Ingress Controller**: Required for ingress routing
4. **Docker Images**: All microservice Docker images must be built and pushed to a registry

## Directory Structure

```
k8s/
├── namespace/               # Namespace configuration
├── secrets/                 # Sensitive data (passwords, API keys)
├── configmaps/             # Configuration data
├── storage/                # PersistentVolume and StorageClass
├── infrastructure/         # StatefulSets for databases and messaging
├── deployments/            # Application deployments
├── ingress/                # Ingress rules for external access
├── deploy.sh              # Automated deployment script
├── cleanup.sh             # Automated cleanup script
└── README.md              # This file
```

## Quick Start

### 1. Build and Push Docker Images

Before deploying to Kubernetes, build all Docker images and push them to your container registry:

```bash
# From the project root directory
cd backend

# Build and tag images (replace 'your-registry' with your actual registry)
for service in api-gateway auth-service blog-service cart-service config-server \
               media-service notification-service order-service product-service \
               report-service user-service warranty-service; do
  docker build -t your-registry/$service:latest -f $service/Dockerfile .
  docker push your-registry/$service:latest
done

# Build frontend images
cd ../frontend
for app in admin dealer main; do
  cd $app
  docker build -t your-registry/${app}-frontend:latest .
  docker push your-registry/${app}-frontend:latest
  cd ..
done
```

### 2. Update Image References

Update the image names in the deployment YAML files to match your registry:

```bash
# Find and replace 'your-registry' with your actual registry URL
find k8s/deployments -name "*.yaml" -exec sed -i 's|your-registry|gcr.io/your-project|g' {} \;
```

### 3. Install NGINX Ingress Controller

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
```

For Minikube:
```bash
minikube addons enable ingress
```

### 4. Deploy Using the Automated Script

```bash
cd k8s
chmod +x deploy.sh
./deploy.sh
```

### 5. Manual Deployment (Alternative)

If you prefer to deploy manually:

```bash
# 1. Create namespace
kubectl apply -f namespace/namespace.yaml

# 2. Create secrets
kubectl apply -f secrets/app-secrets.yaml

# 3. Create ConfigMaps
kubectl apply -f configmaps/

# 4. Create storage
kubectl apply -f storage/storage-class.yaml

# 5. Deploy infrastructure
kubectl apply -f infrastructure/

# 6. Wait for infrastructure to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n microservices --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n microservices --timeout=300s

# 7. Deploy Config Server
kubectl apply -f deployments/config-server.yaml

# 8. Wait for Config Server
kubectl wait --for=condition=ready pod -l app=config-server -n microservices --timeout=300s

# 9. Deploy API Gateway and microservices
kubectl apply -f deployments/

# 10. Create Ingress
kubectl apply -f ingress/ingress.yaml
```

## Accessing the Services

### Add Hosts Entries

Add these entries to your `/etc/hosts` file (or `C:\Windows\System32\drivers\etc\hosts` on Windows):

```
127.0.0.1  admin.microservices.local
127.0.0.1  dealer.microservices.local
127.0.0.1  www.microservices.local
127.0.0.1  api.microservices.local
127.0.0.1  kafka-ui.microservices.local
127.0.0.1  redis-commander.microservices.local
```

### Service URLs

- **Admin Frontend**: http://admin.microservices.local
- **Dealer Frontend**: http://dealer.microservices.local
- **Main Frontend**: http://www.microservices.local
- **API Gateway**: http://api.microservices.local
- **Kafka UI**: http://kafka-ui.microservices.local
- **Redis Commander**: http://redis-commander.microservices.local

### NodePort Access (Alternative)

If Ingress is not configured, you can access services via NodePort:

- Admin Frontend: http://localhost:30000
- Dealer Frontend: http://localhost:30174
- Main Frontend: http://localhost:30080
- Kafka UI: http://localhost:30091
- Redis Commander: http://localhost:30081

## Monitoring and Debugging

### Check Pod Status

```bash
kubectl get pods -n microservices
```

### View Pod Logs

```bash
kubectl logs -n microservices <pod-name>
kubectl logs -n microservices <pod-name> -f  # Follow logs
```

### Describe Pod

```bash
kubectl describe pod -n microservices <pod-name>
```

### Access Pod Shell

```bash
kubectl exec -it -n microservices <pod-name> -- /bin/sh
```

### Check Services

```bash
kubectl get svc -n microservices
```

### Check Ingress

```bash
kubectl get ingress -n microservices
kubectl describe ingress -n microservices microservices-ingress
```

### View All Resources

```bash
kubectl get all -n microservices
```

## Scaling

### Scale a Deployment

```bash
kubectl scale deployment <deployment-name> -n microservices --replicas=3
```

### Horizontal Pod Autoscaling

```bash
kubectl autoscale deployment <deployment-name> -n microservices --min=2 --max=10 --cpu-percent=80
```

## Configuration Updates

### Update ConfigMap

1. Edit the ConfigMap YAML file
2. Apply the changes:
```bash
kubectl apply -f configmaps/app-config.yaml
```
3. Restart affected pods:
```bash
kubectl rollout restart deployment/<deployment-name> -n microservices
```

### Update Secrets

1. Edit the secret YAML file (values should be base64 encoded)
2. Apply the changes:
```bash
kubectl apply -f secrets/app-secrets.yaml
```
3. Restart affected pods

## Cleanup

### Using the Automated Script

```bash
cd k8s
chmod +x cleanup.sh
./cleanup.sh
```

### Manual Cleanup

```bash
kubectl delete namespace microservices
```

This will delete all resources in the namespace. To also remove PersistentVolumes:

```bash
kubectl get pv | grep microservices | awk '{print $1}' | xargs kubectl delete pv
```

## Troubleshooting

### Pods Not Starting

1. Check pod events:
```bash
kubectl describe pod -n microservices <pod-name>
```

2. Check logs:
```bash
kubectl logs -n microservices <pod-name>
```

3. Common issues:
   - Image pull errors: Check image name and registry credentials
   - Init container failures: Check dependencies (e.g., database, config-server)
   - Resource limits: Check if cluster has enough resources

### Database Connection Issues

1. Verify PostgreSQL is running:
```bash
kubectl get pods -n microservices -l app=postgres
```

2. Check PostgreSQL logs:
```bash
kubectl logs -n microservices postgres-0
```

3. Test connection from a pod:
```bash
kubectl exec -it -n microservices <app-pod> -- nc -zv postgres 5432
```

### Kafka Connection Issues

1. Check Kafka and Zookeeper pods:
```bash
kubectl get pods -n microservices -l app=kafka
kubectl get pods -n microservices -l app=zookeeper
```

2. Verify Kafka topics:
```bash
kubectl exec -it -n microservices kafka-0 -- kafka-topics --list --bootstrap-server localhost:9092
```

## Production Considerations

### Security

1. **Secrets Management**: Use external secret managers (e.g., HashiCorp Vault, AWS Secrets Manager)
2. **Network Policies**: Implement network policies to restrict pod-to-pod communication
3. **RBAC**: Configure Role-Based Access Control
4. **TLS/SSL**: Enable HTTPS for all external endpoints

### High Availability

1. **Multi-node Cluster**: Deploy to a multi-node cluster
2. **Pod Disruption Budgets**: Configure PDBs for critical services
3. **Resource Limits**: Set appropriate CPU and memory limits
4. **Health Checks**: Ensure all pods have proper liveness and readiness probes

### Storage

1. **Persistent Storage**: Use cloud-based persistent storage (e.g., AWS EBS, GCP Persistent Disk)
2. **Backup**: Implement regular database backups
3. **Storage Classes**: Configure appropriate storage classes for your cloud provider

### Monitoring

1. **Prometheus & Grafana**: Deploy monitoring stack
2. **Logging**: Implement centralized logging (e.g., ELK stack, Loki)
3. **Tracing**: Add distributed tracing (e.g., Jaeger, Zipkin)

### CI/CD Integration

1. **GitOps**: Use tools like ArgoCD or Flux for GitOps workflows
2. **Automated Testing**: Implement integration tests before deployment
3. **Blue-Green Deployments**: Use deployment strategies for zero-downtime updates

## Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
- [Kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)

## Support

For issues or questions, please refer to the main project documentation or contact the development team.
