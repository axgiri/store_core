# ============================================
# Stage 1: Build Application
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application (skip tests in Docker build)
RUN ./mvnw clean package -DskipTests

# ============================================
# Stage 2: Runtime Image
# ============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# JVM options can be overridden via JAVA_OPTS env var
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
