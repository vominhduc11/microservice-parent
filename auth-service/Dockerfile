# Multi-stage build for Auth Service
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# First, build common-service library
COPY common-service/pom.xml common-service/
COPY common-service/src common-service/src/
RUN cd common-service && mvn clean install -DskipTests

# Copy service pom.xml for dependency resolution
COPY auth-service/pom.xml .

# Download dependencies (better caching)
RUN mvn dependency:go-offline -B

# Copy source code
COPY auth-service/src ./src

# Build the application
RUN mvn clean package -DskipTests

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
COPY --from=build /app/target/auth-service-*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]