# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS deps
WORKDIR /workspace

# Copy only Maven metadata first to maximize layer caching for dependencies.
COPY pom.xml ./
COPY common-library/pom.xml common-library/pom.xml
COPY api-service/pom.xml api-service/pom.xml
COPY worker-service/pom.xml worker-service/pom.xml

# Pre-fetch dependencies for the whole reactor so later builds are faster.
RUN mvn -q -pl api-service -am dependency:go-offline

FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Reuse the dependency cache layer and then copy the full source tree.
COPY --from=deps /root/.m2 /root/.m2
COPY . .

# Build the API service JAR with tests skipped for a production image.
RUN mvn -q -pl api-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

ENV JAVA_OPTS=""
ENV SERVER_PORT=8080

COPY --from=builder /workspace/api-service/target/api-service-0.0.1-SNAPSHOT.jar /app/api-service.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/api-service.jar"]
