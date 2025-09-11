# Use more reliable base images
FROM maven:3.9-eclipse-temurin-21 AS backend-build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM node:18-alpine AS frontend-build

WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

# Use Eclipse Temurin instead of openjdk
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR/WAR file
COPY --from=backend-build /app/target/MicroInvestApp-0.0.1-SNAPSHOT.war app.war

# Copy the built frontend
COPY --from=frontend-build /app/dist /app/static

# Create a non-root user for security
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Change ownership of the app directory
RUN chown -R spring:spring /app
USER spring

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.war --server.port=8080"]