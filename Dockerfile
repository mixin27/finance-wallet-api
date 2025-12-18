FROM eclipse-temurin:17-jdk-focal AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN mkdir -p /app/uploads/transactions
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]