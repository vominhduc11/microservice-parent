# Auto-scaling and Load Balancing

This directory contains configurations for auto-scaling stateless services and load balancing.

## Overview

- **LoadBalancer Services**: Expose services externally via cloud load balancers
- **Horizontal Pod Autoscaler (HPA)**: Automatically scale pods based on CPU/Memory usage
- **Resource Limits**: Define CPU and memory constraints for optimal resource utilization

## Components

### 1. LoadBalancer Services (`../services/loadbalancer-services.yaml`)

Exposes the following services externally:
- API Gateway (port 80)
- Admin Frontend (port 80)
- Dealer Frontend (port 80)
- Main Frontend (port 80)

**Note**: LoadBalancer requires:
- Cloud provider (AWS, GCP, Azure) with load balancer support
- OR MetalLB for on-premise clusters
- OR use Ingress as an alternative (already configured in `../ingress/`)

### 2. Resource Limits (`resource-limits-patch.yaml`)

Defines CPU and memory requests/limits for all stateless services:

| Service Type | CPU Request | Memory Request | CPU Limit | Memory Limit |
|--------------|-------------|----------------|-----------|--------------|
| Backend Services | 200m | 512Mi | 1000m | 1Gi |
| Frontend Services | 100m | 128Mi | 500m | 256Mi |

### 3. Horizontal Pod Autoscaler (`hpa.yaml`)

Auto-scales pods based on metrics:

| Service | Min Replicas | Max Replicas | Target CPU | Target Memory |
|---------|--------------|--------------|------------|---------------|
| API Gateway | 2 | 10 | 70% | 80% |
| Product Service | 2 | 10 | 70% | 80% |
| Order Service | 2 | 10 | 70% | 80% |
| Media Service | 2 | 10 | 70% | 80% |
| Main Frontend | 2 | 10 | 70% | 80% |
| Dealer Frontend | 2 | 8 | 70% | 80% |
| Auth Service | 2 | 8 | 70% | 80% |
| User Service | 2 | 8 | 70% | 80% |
| Cart Service | 2 | 8 | 70% | 80% |
| Notification Service | 2 | 8 | 70% | 80% |
| Warranty Service | 2 | 6 | 70% | 80% |
| Blog Service | 2 | 6 | 70% | 80% |
| Report Service | 2 | 6 | 70% | 80% |
| Admin Frontend | 2 | 5 | 70% | 80% |

## Prerequisites

### 1. Metrics Server

HPA requires Metrics Server to be installed in your cluster.

**Installation:**

```bash
# Install Metrics Server
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# For local development (Minikube, Kind), patch for insecure TLS
kubectl patch deployment metrics-server -n kube-system --type='json' -p='[
  {
    "op": "add",
    "path": "/spec/template/spec/containers/0/args/-",
    "value": "--kubelet-insecure-tls"
  }
]'
```

**Verify:**

```bash
kubectl get deployment metrics-server -n kube-system
kubectl top nodes
kubectl top pods -n microservices
```

### 2. LoadBalancer Support (Optional)

**Cloud Providers:**
- AWS: Automatically provisioned
- GCP: Automatically provisioned
- Azure: Automatically provisioned

**On-Premise with MetalLB:**

```bash
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.13.12/config/manifests/metallb-native.yaml

# Configure IP pool (edit as needed)
cat <<EOF | kubectl apply -f -
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: default-pool
  namespace: metallb-system
spec:
  addresses:
  - 192.168.1.240-192.168.1.250
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: default
  namespace: metallb-system
spec:
  ipAddressPools:
  - default-pool
EOF
```

## Quick Start

### Automated Setup

```bash
cd k8s/autoscaling
chmod +x apply-autoscaling.sh
./apply-autoscaling.sh
```

This script will:
1. Check and install Metrics Server if needed
2. Apply resource limits to all deployments
3. Apply HPA configurations
4. Optionally apply LoadBalancer services

### Manual Setup

#### Step 1: Install Metrics Server

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

#### Step 2: Apply Resource Limits

```bash
# Apply to all deployments
kubectl apply -f resource-limits-patch.yaml
```

#### Step 3: Apply HPA

```bash
kubectl apply -f hpa.yaml
```

#### Step 4: Apply LoadBalancer Services (Optional)

```bash
kubectl apply -f ../services/loadbalancer-services.yaml
```

## Monitoring

### Check HPA Status

```bash
# List all HPAs
kubectl get hpa -n microservices

# Describe specific HPA
kubectl describe hpa api-gateway-hpa -n microservices

# Watch HPA in real-time
watch kubectl get hpa -n microservices
```

### Check Resource Usage

```bash
# Pod resource usage
kubectl top pods -n microservices

# Node resource usage
kubectl top nodes

# Sort by CPU usage
kubectl top pods -n microservices --sort-by=cpu

# Sort by Memory usage
kubectl top pods -n microservices --sort-by=memory
```

### Check LoadBalancer External IPs

```bash
kubectl get svc -n microservices | grep LoadBalancer
```

## Load Testing

### Using the Test Script

```bash
cd k8s/autoscaling
chmod +x test-load.sh
./test-load.sh
```

The script provides options to:
1. Test individual services
2. Test all services sequentially
3. Custom load testing parameters

### Manual Load Testing

**Using Apache Bench (ab):**

```bash
# Install ab (if not installed)
# Ubuntu/Debian: apt-get install apache2-utils
# macOS: brew install httpd

# Generate load
ab -n 10000 -c 50 http://api.microservices.local/
```

**Using wrk:**

```bash
# Install wrk
# Ubuntu/Debian: apt-get install wrk
# macOS: brew install wrk

# Generate load
wrk -t10 -c100 -d60s http://api.microservices.local/
```

**Using kubectl run:**

```bash
# Create a load generator pod
kubectl run -it --rm load-generator --image=busybox --restart=Never -n microservices -- /bin/sh

# Inside the pod, run:
while true; do wget -q -O- http://api-gateway:8080; done
```

## Tuning Auto-scaling

### Adjust HPA Parameters

Edit `hpa.yaml` and modify:

```yaml
spec:
  minReplicas: 2          # Minimum number of pods
  maxReplicas: 10         # Maximum number of pods
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70  # Target CPU %
```

### Scaling Behavior

Control how fast pods scale up/down:

```yaml
behavior:
  scaleDown:
    stabilizationWindowSeconds: 300  # Wait 5 min before scaling down
    policies:
    - type: Percent
      value: 50           # Scale down max 50% of current pods
      periodSeconds: 60   # Per minute
  scaleUp:
    stabilizationWindowSeconds: 0    # Scale up immediately
    policies:
    - type: Percent
      value: 100          # Can double the number of pods
      periodSeconds: 30   # Every 30 seconds
    - type: Pods
      value: 2            # Or add 2 pods at a time
      periodSeconds: 30
    selectPolicy: Max     # Use the policy that adds more pods
```

### Resource Limits

Adjust per deployment:

```bash
kubectl set resources deployment/api-gateway -n microservices \
  --requests=cpu=500m,memory=1Gi \
  --limits=cpu=2000m,memory=2Gi
```

## Advanced HPA with Custom Metrics

### Using Custom Metrics (Prometheus)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: api-gateway-hpa-custom
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: api-gateway
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "1000"
```

**Requires:**
- Prometheus installed
- Prometheus Adapter configured
- Custom metrics exposed by applications

## Troubleshooting

### HPA Shows "Unknown" Metrics

**Check Metrics Server:**

```bash
kubectl get apiservice v1beta1.metrics.k8s.io -o yaml
kubectl logs -n kube-system deployment/metrics-server
```

**Verify metrics are available:**

```bash
kubectl top pods -n microservices
```

### Pods Not Scaling

**Check HPA events:**

```bash
kubectl describe hpa <hpa-name> -n microservices
```

**Common issues:**
- Resource requests not defined (required for percentage-based scaling)
- Metrics Server not installed or not working
- Target metrics never reached

### LoadBalancer Pending

**Check service:**

```bash
kubectl describe svc <service-name>-lb -n microservices
```

**Common causes:**
- No LoadBalancer provider (use MetalLB for on-premise)
- Cloud provider quota exceeded
- Insufficient IP addresses in pool

### Pods in Pending State Due to Resources

**Check node resources:**

```bash
kubectl describe nodes
```

**Solutions:**
- Scale down max replicas
- Reduce resource requests
- Add more nodes to cluster

## Cost Optimization

### Development Environment

```yaml
# Minimal auto-scaling
minReplicas: 1
maxReplicas: 3

# Lower resource requests
requests:
  cpu: 100m
  memory: 256Mi
```

### Production Environment

```yaml
# Higher availability
minReplicas: 3
maxReplicas: 20

# Appropriate resource requests
requests:
  cpu: 500m
  memory: 1Gi
```

### LoadBalancer Cost

**Alternatives to LoadBalancer:**
1. Use Ingress (already configured) - Single load balancer for all services
2. Use NodePort - Access via node IP and port
3. Use port-forwarding for development

## Best Practices

1. **Always set resource requests** - Required for HPA to work correctly
2. **Set resource limits** - Prevent pods from consuming all node resources
3. **Monitor regularly** - Use metrics to tune HPA parameters
4. **Test under load** - Verify auto-scaling works as expected
5. **Use Pod Disruption Budgets** - Ensure availability during scaling
6. **Configure appropriate min/max replicas** - Balance cost and availability
7. **Use appropriate stabilization windows** - Prevent flapping

## References

- [Kubernetes HPA Documentation](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
- [Kubernetes Metrics Server](https://github.com/kubernetes-sigs/metrics-server)
- [MetalLB Documentation](https://metallb.universe.tf/)
- [Kubernetes Resource Management](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/)
