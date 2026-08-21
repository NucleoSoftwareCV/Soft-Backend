FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring spring
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

USER spring:spring
EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -fsS http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
