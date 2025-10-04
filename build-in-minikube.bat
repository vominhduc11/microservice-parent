@echo off
REM Point to Minikube's Docker daemon
FOR /f "tokens=*" %%i IN ('minikube -p minikube docker-env') DO %%i

echo Building images in Minikube Docker daemon...

REM Build backend services
cd backend

FOR %%s IN (config-server api-gateway auth-service user-service product-service cart-service order-service warranty-service notification-service blog-service report-service media-service) DO (
    echo Building %%s...
    cd %%s
    docker build -t microservice-parent-%%s:latest . 2>nul
    cd ..
)

cd ..

echo ✅ All images built in Minikube!
echo.
echo Now run: kubectl rollout restart deployment -n microservices
