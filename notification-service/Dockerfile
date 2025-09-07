# Build stage
FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
# First, build common-service library
COPY common-service/pom.xml common-service/
COPY common-service/src common-service/src/
RUN cd common-service && mvn clean install -DskipTests

# Now build notification-service
COPY notification-service/pom.xml .


# Copy source code and build
COPY notification-service/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8087

CMD ["java", "-jar", "app.jar"]