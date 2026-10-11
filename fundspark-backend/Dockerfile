# ── Stage 1: Build the JAR ──────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom first (layer caching for dependencies)
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:resolve -DskipTests

# Copy source code
COPY src src

# Build the fat JAR
RUN ./mvnw package -DskipTests -Dspring-boot.build-image.enabled=false

# ── Stage 2: Run ────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Render injects $PORT at runtime
EXPOSE 8080

# JVM tuning for Render free-tier (512 MB RAM)
ENTRYPOINT ["java", \
  "-Xmx384m", \
  "-Xms128m", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
