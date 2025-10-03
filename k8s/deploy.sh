#!/bin/bash

# Microservices Kubernetes Deployment Script
# This script deploys all microservices to a Kubernetes cluster

set -e

echo "=========================================="
echo "Deploying Microservices to Kubernetes"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Step 1: Create namespace
print_info "Creating namespace..."
kubectl apply -f namespace/namespace.yaml

# Step 2: Create secrets
print_info "Creating secrets..."
kubectl apply -f secrets/app-secrets.yaml

# Step 3: Create ConfigMaps
print_info "Creating ConfigMaps..."
kubectl apply -f configmaps/app-config.yaml
kubectl apply -f configmaps/postgres-init-script.yaml

# Step 4: Create storage
print_info "Creating storage..."
kubectl apply -f storage/storage-class.yaml

# Step 5: Deploy infrastructure (PostgreSQL, Redis, Zookeeper, Kafka)
print_info "Deploying infrastructure services..."
kubectl apply -f infrastructure/postgres-statefulset.yaml
kubectl apply -f infrastructure/redis-statefulset.yaml
kubectl apply -f infrastructure/zookeeper-statefulset.yaml
kubectl apply -f infrastructure/kafka-statefulset.yaml

print_info "Waiting for infrastructure to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n microservices --timeout=300s || print_warning "PostgreSQL pods not ready yet"
kubectl wait --for=condition=ready pod -l app=redis -n microservices --timeout=300s || print_warning "Redis pods not ready yet"

# Step 6: Deploy Config Server
print_info "Deploying Config Server..."
kubectl apply -f deployments/config-server.yaml

print_info "Waiting for Config Server to be ready..."
kubectl wait --for=condition=ready pod -l app=config-server -n microservices --timeout=300s || print_warning "Config Server not ready yet"

# Step 7: Deploy API Gateway
print_info "Deploying API Gateway..."
kubectl apply -f deployments/api-gateway.yaml

# Step 8: Deploy backend microservices
print_info "Deploying backend microservices..."
kubectl apply -f deployments/auth-service.yaml
kubectl apply -f deployments/backend-services.yaml

# Step 9: Deploy frontend services
print_info "Deploying frontend services..."
kubectl apply -f deployments/frontend-services.yaml

# Step 10: Create Ingress
print_info "Creating Ingress..."
kubectl apply -f ingress/ingress.yaml

# Display deployment status
echo ""
print_info "Deployment complete! Checking status..."
echo ""
kubectl get all -n microservices

echo ""
print_info "To check the status of your deployments, run:"
echo "  kubectl get pods -n microservices"
echo ""
print_info "To access the services, add these entries to your /etc/hosts file:"
echo "  127.0.0.1  admin.microservices.local"
echo "  127.0.0.1  dealer.microservices.local"
echo "  127.0.0.1  www.microservices.local"
echo "  127.0.0.1  api.microservices.local"
echo "  127.0.0.1  kafka-ui.microservices.local"
echo "  127.0.0.1  redis-commander.microservices.local"
echo ""
print_info "Make sure NGINX Ingress Controller is installed:"
echo "  kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml"
echo ""
