# Build from repo root: docker build -f payment-fidelite/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-fidelite/pom.xml payment-fidelite/pom.xml
COPY payment-fidelite/src payment-fidelite/src
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-fidelite/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fidelite && adduser -S fidelite -G fidelite
COPY --from=build /workspace/payment-fidelite/target/payment-fidelite.jar app.jar
USER fidelite
EXPOSE 8103
ENTRYPOINT ["java", "-jar", "app.jar"]
