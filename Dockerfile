# Build backend
FROM maven:3.9-eclipse-temurin-21 AS backend-build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build and list the target directory for debugging
RUN mvn clean package -DskipTests
RUN ls -la target/

# Build frontend
FROM node:18-alpine AS frontend-build

WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci --omit=dev
COPY frontend/ .
RUN npm run build
RUN ls -la dist/

# Runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy any .war file from target directory
COPY --from=backend-build /app/target/*.war app.war

# Copy the built frontend
COPY --from=frontend-build /app/dist ./static

# Create non-root user
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 -G spring

# Set ownership and switch to non-root user
RUN chown -R spring:spring /app
USER spring

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.war --server.port=8080"]