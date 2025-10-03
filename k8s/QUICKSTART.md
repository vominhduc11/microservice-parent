# Kubernetes Quick Start Guide

## Prerequisites Checklist

- [ ] Kubernetes cluster running (Minikube, Kind, or cloud provider)
- [ ] `kubectl` installed and configured
- [ ] Docker images built and pushed to registry
- [ ] NGINX Ingress Controller installed

## 5-Minute Deployment

### Step 1: Install NGINX Ingress Controller

```bash
# For most Kubernetes clusters
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

# For Minikube
minikube addons enable ingress

# For Docker Desktop
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml
```

### Step 2: Update Image Registry

Edit deployment files and replace `your-registry` with your actual container registry:

```bash
cd k8s/deployments
# Example: Replace with your registry URL
find . -name "*.yaml" -exec sed -i 's|your-registry|docker.io/youruser|g' {} \;
```

### Step 3: Deploy Everything

```bash
cd k8s
./deploy.sh
```

### Step 4: Check Status

```bash
./status.sh
```

### Step 5: Access Services

Add to `/etc/hosts`:
```
127.0.0.1  admin.microservices.local dealer.microservices.local www.microservices.local api.microservices.local
```

Then open in browser:
- http://admin.microservices.local
- http://dealer.microservices.local
- http://www.microservices.local

## Troubleshooting

### Pods Not Starting?

Check logs:
```bash
kubectl get pods -n microservices
kubectl logs -n microservices <pod-name>
kubectl describe pod -n microservices <pod-name>
```

### Can't Access Services?

1. Check if Ingress Controller is running:
```bash
kubectl get pods -n ingress-nginx
```

2. Check Ingress status:
```bash
kubectl get ingress -n microservices
kubectl describe ingress -n microservices microservices-ingress
```

3. Try NodePort instead:
```bash
kubectl get svc -n microservices
# Access via http://localhost:<nodePort>
```

### Database Connection Issues?

1. Check if PostgreSQL is running:
```bash
kubectl get pods -n microservices -l app=postgres
```

2. Check PostgreSQL logs:
```bash
kubectl logs -n microservices postgres-0
```

3. Verify databases were created:
```bash
kubectl exec -it -n microservices postgres-0 -- psql -U postgres -c "\l"
```

## Cleanup

To remove everything:
```bash
./cleanup.sh
```

## Next Steps

- Review [README.md](README.md) for detailed documentation
- Set up monitoring (Prometheus/Grafana)
- Configure production-grade secrets management
- Implement CI/CD pipeline
- Set up backups for databases
