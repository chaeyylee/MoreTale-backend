# 1. Build stage
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon -x test

# 2. Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN mkdir -p /app/uploads

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
