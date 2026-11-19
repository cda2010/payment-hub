# Build from repo root: docker build -f payment-api-gateway/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-api-gateway/pom.xml payment-api-gateway/pom.xml
COPY payment-api-gateway/src payment-api-gateway/src
# Le reactor parent liste tous les modules : on ne construit que cette porte.
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-api-gateway/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S edge && adduser -S edge -G edge
COPY --from=build /workspace/payment-api-gateway/target/payment-api-gateway.jar app.jar
USER edge
EXPOSE 8070
ENTRYPOINT ["java", "-jar", "app.jar"]
