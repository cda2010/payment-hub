# Build from repo root: docker build -f payment-network/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-network/pom.xml payment-network/pom.xml
COPY payment-network/src payment-network/src
# Le reactor parent liste tous les modules : on ne construit que payment-network.
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-network/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S network && adduser -S network -G network
COPY --from=build /workspace/payment-network/target/payment-network.jar app.jar
USER network
EXPOSE 8096
ENTRYPOINT ["java", "-jar", "app.jar"]
