# Build stage
FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY config-server/pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY config-server/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8888

CMD ["java", "-jar", "app.jar"]