#!/bin/bash

echo "Loading images to Minikube..."

IMAGES=(
  "microservice-parent-config-server:latest"
  "microservice-parent-api-gateway:latest"
  "microservice-parent-auth-service:latest"
  "microservice-parent-user-service:latest"
  "microservice-parent-product-service:latest"
  "microservice-parent-cart-service:latest"
  "microservice-parent-order-service:latest"
  "microservice-parent-warranty-service:latest"
  "microservice-parent-notification-service:latest"
  "microservice-parent-blog-service:latest"
  "microservice-parent-report-service:latest"
  "microservice-parent-media-service:latest"
)

for image in "${IMAGES[@]}"; do
  echo "Loading $image..."
  minikube image load "$image"
done

echo "✅ All images loaded to Minikube!"
