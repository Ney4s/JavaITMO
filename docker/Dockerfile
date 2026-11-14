FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies --configuration runtimeClasspath

COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre

ENV LANG=C.UTF-8

RUN useradd --create-home --shell /bin/bash autosalon
USER autosalon
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar /app/autosalon.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/autosalon.jar"]
