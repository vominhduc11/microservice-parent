# Multi-stage build for API Gateway
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Step 1: Download service dependencies (cache until pom.xml changes)
COPY api-gateway/pom.xml .
RUN mvn dependency:go-offline -B

# Step 2: Copy source and build (invalidates cache when code changes)
COPY api-gateway/src ./src
RUN mvn clean package -DskipTests -o

# Add build timestamp to force rebuild
ARG BUILD_TIME
ENV BUILD_TIME=${BUILD_TIME:-unknown}

# Production stage
FROM eclipse-temurin:17-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/api-gateway-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]