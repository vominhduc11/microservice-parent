# Rebuild Images in Minikube

Images hiện tại được build trong Docker Desktop, nhưng Minikube không thể access chúng.

## Giải pháp: Build lại trong Minikube's Docker

### Bước 1: Point shell đến Minikube Docker

```bash
# PowerShell
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# Hoặc CMD
@FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i
```

### Bước 2: Build lại tất cả services

```bash
cd backend

# Config Server
cd config-server && mvn clean package -DskipTests && docker build -t microservice-parent-config-server:latest . && cd ..

# API Gateway
cd api-gateway && mvn clean package -DskipTests && docker build -t microservice-parent-api-gateway:latest . && cd ..

# Auth Service
cd auth-service && mvn clean package -DskipTests && docker build -t microservice-parent-auth-service:latest . && cd ..

# User Service
cd user-service && mvn clean package -DskipTests && docker build -t microservice-parent-user-service:latest . && cd ..

# Product Service
cd product-service && mvn clean package -DskipTests && docker build -t microservice-parent-product-service:latest . && cd ..

# Cart Service
cd cart-service && mvn clean package -DskipTests && docker build -t microservice-parent-cart-service:latest . && cd ..

# Order Service
cd order-service && mvn clean package -DskipTests && docker build -t microservice-parent-order-service:latest . && cd ..

# Warranty Service
cd warranty-service && mvn clean package -DskipTests && docker build -t microservice-parent-warranty-service:latest . && cd ..

# Notification Service
cd notification-service && mvn clean package -DskipTests && docker build -t microservice-parent-notification-service:latest . && cd ..

# Blog Service
cd blog-service && mvn clean package -DskipTests && docker build -t microservice-parent-blog-service:latest . && cd ..

# Report Service
cd report-service && mvn clean package -DskipTests && docker build -t microservice-parent-report-service:latest . && cd ..

# Media Service
cd media-service && mvn clean package -DskipTests && docker build -t microservice-parent-media-service:latest . && cd ..

cd ..
```

### Bước 3: Verify images

```bash
docker images | findstr "microservice-parent"
```

Bạn sẽ thấy 12 images với tag `latest`.

### Bước 4: Redeploy K8s

```bash
kubectl delete -k k8s/overlays/dev
kubectl apply -k k8s/overlays/dev
```

### Bước 5: Watch deployment

```bash
kubectl get pods -n microservices -w
```

## HOẶC: Giải pháp nhanh hơn - Copy từ Docker Desktop

Nếu không muốn build lại, dùng cách này:

```bash
# Save images từ Docker Desktop
docker save microservice-parent-config-server:latest -o config-server.tar
docker save microservice-parent-api-gateway:latest -o api-gateway.tar
# ... tất cả services

# Load vào Minikube
minikube image load config-server.tar
minikube image load api-gateway.tar
# ... tất cả services
```

## Sau khi xong:

```bash
kubectl get pods -n microservices
```

Tất cả pods sẽ chạy!
