#!/bin/bash

# Load Testing Script for Auto-scaling
# This script generates load to test HPA functionality

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_header() {
    echo -e "${BLUE}=========================================="
    echo -e "$1"
    echo -e "==========================================${NC}"
}

# Function to generate load
generate_load() {
    local service_name=$1
    local duration=${2:-60}  # Default 60 seconds
    local concurrency=${3:-10}  # Default 10 concurrent requests

    print_header "Generating Load on $service_name"

    # Get service URL
    if [[ $service_name == *"-frontend" ]]; then
        local port=$(kubectl get svc ${service_name}-lb -n microservices -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
        if [ -z "$port" ]; then
            port="http://${service_name}.microservices.local"
        else
            port="http://${port}"
        fi
    else
        port="http://api.microservices.local"
    fi

    print_info "Target: $port"
    print_info "Duration: ${duration}s"
    print_info "Concurrency: $concurrency"

    # Use Apache Bench if available
    if command -v ab &> /dev/null; then
        print_info "Using Apache Bench (ab)..."
        ab -n 10000 -c $concurrency -t $duration $port/ 2>/dev/null &
        AB_PID=$!
    # Use wrk if available
    elif command -v wrk &> /dev/null; then
        print_info "Using wrk..."
        wrk -t$concurrency -c$concurrency -d${duration}s $port/ &
        AB_PID=$!
    # Use curl in a loop as fallback
    else
        print_warning "Neither ab nor wrk found, using curl in loop..."
        for i in $(seq 1 $concurrency); do
            (
                end=$((SECONDS + duration))
                while [ $SECONDS -lt $end ]; do
                    curl -s $port/ > /dev/null 2>&1
                done
            ) &
        done
        AB_PID=$!
    fi

    print_info "Load generation started (PID: $AB_PID)"
    print_info "Monitoring HPA status..."

    # Monitor HPA while load is running
    (
        for i in $(seq 1 $duration); do
            sleep 1
            if [ $((i % 5)) -eq 0 ]; then
                clear
                echo "Time: ${i}s / ${duration}s"
                echo ""
                kubectl get hpa -n microservices | grep "${service_name%-service}"
                echo ""
                kubectl top pods -n microservices | grep "${service_name%-service}"
            fi
        done
    )

    wait $AB_PID
    print_info "Load generation completed"
}

# Main menu
print_header "Auto-scaling Load Test Menu"

echo "Select a service to test:"
echo "1) API Gateway"
echo "2) Auth Service"
echo "3) Product Service"
echo "4) Order Service"
echo "5) All Backend Services (sequential)"
echo "6) Custom service"
echo "7) Exit"
echo ""

read -p "Enter your choice (1-7): " choice

case $choice in
    1)
        generate_load "api-gateway" 120 20
        ;;
    2)
        generate_load "auth-service" 120 15
        ;;
    3)
        generate_load "product-service" 120 15
        ;;
    4)
        generate_load "order-service" 120 15
        ;;
    5)
        SERVICES=("api-gateway" "auth-service" "user-service" "product-service" "cart-service" "order-service")
        for svc in "${SERVICES[@]}"; do
            generate_load "$svc" 60 10
            sleep 30  # Wait between services
        done
        ;;
    6)
        read -p "Enter service name (e.g., user-service): " custom_service
        read -p "Enter duration in seconds (default 60): " custom_duration
        read -p "Enter concurrency level (default 10): " custom_concurrency

        custom_duration=${custom_duration:-60}
        custom_concurrency=${custom_concurrency:-10}

        generate_load "$custom_service" $custom_duration $custom_concurrency
        ;;
    7)
        print_info "Exiting..."
        exit 0
        ;;
    *)
        print_warning "Invalid choice"
        exit 1
        ;;
esac

echo ""
print_header "Final HPA Status"
kubectl get hpa -n microservices

echo ""
print_header "Final Pod Status"
kubectl get pods -n microservices

echo ""
print_info "Load test completed!"
print_info "To watch scaling down, run:"
echo "  watch kubectl get hpa -n microservices"
