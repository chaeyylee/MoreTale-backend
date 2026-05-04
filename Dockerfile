FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN mkdir -p /app/uploads

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
