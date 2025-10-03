#!/bin/bash

# Microservices Kubernetes Status Check Script
# This script checks the status of all deployed microservices

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}=========================================="
    echo -e "$1"
    echo -e "==========================================${NC}"
}

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if namespace exists
if ! kubectl get namespace microservices &> /dev/null; then
    print_error "Namespace 'microservices' does not exist. Have you deployed the application yet?"
    exit 1
fi

# All Pods
print_header "Pod Status"
kubectl get pods -n microservices -o wide

echo ""

# All Services
print_header "Service Status"
kubectl get svc -n microservices

echo ""

# Deployments
print_header "Deployment Status"
kubectl get deployments -n microservices

echo ""

# StatefulSets
print_header "StatefulSet Status"
kubectl get statefulsets -n microservices

echo ""

# Ingress
print_header "Ingress Status"
kubectl get ingress -n microservices

echo ""

# ConfigMaps
print_header "ConfigMap Status"
kubectl get configmaps -n microservices

echo ""

# Secrets
print_header "Secret Status"
kubectl get secrets -n microservices

echo ""

# PersistentVolumeClaims
print_header "PersistentVolumeClaim Status"
kubectl get pvc -n microservices

echo ""

# Check pod health
print_header "Pod Health Summary"

total_pods=$(kubectl get pods -n microservices --no-headers | wc -l)
running_pods=$(kubectl get pods -n microservices --no-headers | grep -c "Running" || echo 0)
pending_pods=$(kubectl get pods -n microservices --no-headers | grep -c "Pending" || echo 0)
failed_pods=$(kubectl get pods -n microservices --no-headers | grep -c "Error\|CrashLoopBackOff\|Failed" || echo 0)

echo -e "Total Pods:   ${BLUE}$total_pods${NC}"
echo -e "Running:      ${GREEN}$running_pods${NC}"
echo -e "Pending:      ${YELLOW}$pending_pods${NC}"
echo -e "Failed:       ${RED}$failed_pods${NC}"

echo ""

# List not-running pods
if [ $failed_pods -gt 0 ] || [ $pending_pods -gt 0 ]; then
    print_warning "Pods with issues:"
    kubectl get pods -n microservices | grep -v "Running\|Completed" | tail -n +2

    echo ""
    print_info "To check logs of a problematic pod, run:"
    echo "  kubectl logs -n microservices <pod-name>"
    echo ""
    print_info "To describe a pod and see events, run:"
    echo "  kubectl describe pod -n microservices <pod-name>"
fi

echo ""

# Check if ingress controller is installed
print_header "Ingress Controller Check"
if kubectl get pods -n ingress-nginx &> /dev/null; then
    print_info "NGINX Ingress Controller is installed"
    kubectl get pods -n ingress-nginx
else
    print_warning "NGINX Ingress Controller not found in namespace 'ingress-nginx'"
    print_info "To install it, run:"
    echo "  kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml"
fi

echo ""

# Access URLs
print_header "Access URLs"
echo "Add these entries to your /etc/hosts file:"
echo "127.0.0.1  admin.microservices.local"
echo "127.0.0.1  dealer.microservices.local"
echo "127.0.0.1  www.microservices.local"
echo "127.0.0.1  api.microservices.local"
echo "127.0.0.1  kafka-ui.microservices.local"
echo "127.0.0.1  redis-commander.microservices.local"
echo ""
echo "Then access:"
echo "  Admin:     http://admin.microservices.local"
echo "  Dealer:    http://dealer.microservices.local"
echo "  Main:      http://www.microservices.local"
echo "  API:       http://api.microservices.local"
echo "  Kafka UI:  http://kafka-ui.microservices.local"
echo "  Redis:     http://redis-commander.microservices.local"
echo ""
echo "Or use NodePort (if configured):"
echo "  Admin:     http://localhost:30000"
echo "  Dealer:    http://localhost:30174"
echo "  Main:      http://localhost:30080"
echo "  Kafka UI:  http://localhost:30091"
echo "  Redis:     http://localhost:30081"
echo ""
