FROM maven:3.9-openjdk-21-slim AS backend-build

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

FROM openjdk:21-jre-slim

WORKDIR /app
COPY --from=backend-build /app/target/*.war app.war
COPY --from=frontend-build /app/dist /app/static

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
CMD ["java", "-jar", "app.war", "--server.port=8080"]