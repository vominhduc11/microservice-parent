# Build stage
FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /app

# First, build common-service library
COPY common-service/pom.xml common-service/
COPY common-service/src common-service/src/
RUN cd common-service && mvn clean install -DskipTests

# Now build blog-service
COPY blog-service/pom.xml .
COPY blog-service/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8088

CMD ["java", "-jar", "app.jar"]