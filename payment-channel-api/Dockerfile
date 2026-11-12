# Build from repo root: docker build -f payment-channel-api/Dockerfile .
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-domain/pom.xml payment-domain/pom.xml
COPY payment-orchestration/pom.xml payment-orchestration/pom.xml
COPY payment-application/pom.xml payment-application/pom.xml
COPY payment-channel-api/pom.xml payment-channel-api/pom.xml
COPY payment-domain/src payment-domain/src
COPY payment-orchestration/src payment-orchestration/src
COPY payment-application/src payment-application/src
COPY payment-channel-api/src payment-channel-api/src
RUN chmod +x mvnw && ./mvnw -B -pl payment-channel-api -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S gateway && adduser -S gateway -G gateway
COPY --from=build /workspace/payment-channel-api/target/payment-gateway.jar app.jar
USER gateway
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
