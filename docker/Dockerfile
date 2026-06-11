# Этап 1: Сборка
FROM maven:3.8.1-openjdk-17 AS builder
WORKDIR /app
COPY ../my-shelf-server .
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]