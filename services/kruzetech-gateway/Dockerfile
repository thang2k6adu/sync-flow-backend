# Stage 1: build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

# Stage 2: runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app && apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/build/libs/kruzetech-gateway.jar /app/app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
