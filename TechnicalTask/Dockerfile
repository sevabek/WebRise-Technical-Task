FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
# Копируем исходники и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/TechnicalTask-0.0.1-SNAPSHOT.jar app.jar
ENV POSTGRES_HOST=postgres
ENV TZ=Europe/Moscow
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
