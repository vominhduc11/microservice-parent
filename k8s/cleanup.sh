#!/bin/bash

# Microservices Kubernetes Cleanup Script
# This script removes all deployed microservices from the Kubernetes cluster

set -e

echo "=========================================="
echo "Cleaning up Microservices from Kubernetes"
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

# Ask for confirmation
echo ""
print_warning "This will delete all resources in the 'microservices' namespace."
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    print_info "Cleanup cancelled."
    exit 0
fi

# Delete in reverse order
print_info "Deleting Ingress..."
kubectl delete -f ingress/ingress.yaml --ignore-not-found=true

print_info "Deleting frontend services..."
kubectl delete -f deployments/frontend-services.yaml --ignore-not-found=true

print_info "Deleting backend microservices..."
kubectl delete -f deployments/backend-services.yaml --ignore-not-found=true
kubectl delete -f deployments/auth-service.yaml --ignore-not-found=true

print_info "Deleting API Gateway..."
kubectl delete -f deployments/api-gateway.yaml --ignore-not-found=true

print_info "Deleting Config Server..."
kubectl delete -f deployments/config-server.yaml --ignore-not-found=true

print_info "Deleting infrastructure services..."
kubectl delete -f infrastructure/kafka-statefulset.yaml --ignore-not-found=true
kubectl delete -f infrastructure/zookeeper-statefulset.yaml --ignore-not-found=true
kubectl delete -f infrastructure/redis-statefulset.yaml --ignore-not-found=true
kubectl delete -f infrastructure/postgres-statefulset.yaml --ignore-not-found=true

print_info "Deleting storage..."
kubectl delete -f storage/storage-class.yaml --ignore-not-found=true

print_info "Deleting ConfigMaps..."
kubectl delete -f configmaps/postgres-init-script.yaml --ignore-not-found=true
kubectl delete -f configmaps/app-config.yaml --ignore-not-found=true

print_info "Deleting secrets..."
kubectl delete -f secrets/app-secrets.yaml --ignore-not-found=true

print_info "Deleting namespace..."
kubectl delete -f namespace/namespace.yaml --ignore-not-found=true

echo ""
print_info "Cleanup complete!"
print_warning "Note: PersistentVolumes may need to be manually deleted."
echo ""
print_info "To check remaining PersistentVolumes, run:"
echo "  kubectl get pv"
echo ""
