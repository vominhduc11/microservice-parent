#!/bin/bash

# Auto-scaling Setup Script
# This script sets up HPA and resource limits for all stateless services

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

echo "=========================================="
echo "Setting up Auto-scaling"
echo "=========================================="

# Check if Metrics Server is installed
print_info "Checking if Metrics Server is installed..."
if ! kubectl get deployment metrics-server -n kube-system &> /dev/null; then
    print_warning "Metrics Server is not installed!"
    echo ""
    print_info "Installing Metrics Server..."
    kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

    # For local development (Minikube, Kind, etc.), patch metrics-server
    print_info "Patching Metrics Server for local development..."
    kubectl patch deployment metrics-server -n kube-system --type='json' -p='[
      {
        "op": "add",
        "path": "/spec/template/spec/containers/0/args/-",
        "value": "--kubelet-insecure-tls"
      }
    ]'

    print_info "Waiting for Metrics Server to be ready..."
    kubectl wait --for=condition=available --timeout=120s deployment/metrics-server -n kube-system

    print_info "Metrics Server installed successfully!"
else
    print_info "Metrics Server is already installed"
fi

echo ""

# Apply resource limits to all deployments
print_info "Applying resource limits to deployments..."

# List of all stateless deployments
DEPLOYMENTS=(
    "config-server"
    "api-gateway"
    "auth-service"
    "user-service"
    "product-service"
    "cart-service"
    "order-service"
    "warranty-service"
    "notification-service"
    "blog-service"
    "report-service"
    "media-service"
    "admin-frontend"
    "dealer-frontend"
    "main-frontend"
)

for deployment in "${DEPLOYMENTS[@]}"; do
    if kubectl get deployment $deployment -n microservices &> /dev/null; then
        print_info "Applying resource limits to $deployment..."
        kubectl set resources deployment/$deployment -n microservices \
            --requests=cpu=200m,memory=512Mi \
            --limits=cpu=1000m,memory=1Gi 2>/dev/null || \
        kubectl set resources deployment/$deployment -n microservices \
            --requests=cpu=100m,memory=128Mi \
            --limits=cpu=500m,memory=256Mi
    else
        print_warning "Deployment $deployment not found, skipping..."
    fi
done

echo ""

# Apply HPA configurations
print_info "Applying Horizontal Pod Autoscaler configurations..."
kubectl apply -f hpa.yaml

echo ""

# Apply LoadBalancer services (optional)
if [ -f "../services/loadbalancer-services.yaml" ]; then
    print_info "Do you want to apply LoadBalancer services? (yes/no)"
    read -p "This will create cloud load balancers (may incur costs): " apply_lb

    if [ "$apply_lb" = "yes" ]; then
        print_info "Applying LoadBalancer services..."
        kubectl apply -f ../services/loadbalancer-services.yaml
    else
        print_info "Skipping LoadBalancer services"
    fi
fi

echo ""

# Show HPA status
print_info "Current HPA status:"
kubectl get hpa -n microservices

echo ""
print_info "Auto-scaling setup complete!"
echo ""
print_info "To check HPA status, run:"
echo "  kubectl get hpa -n microservices"
echo ""
print_info "To check resource usage, run:"
echo "  kubectl top pods -n microservices"
echo ""
print_info "To watch auto-scaling in action:"
echo "  watch kubectl get hpa -n microservices"
echo ""
