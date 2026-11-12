# Build from repo root: docker build -f payment-identity/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-identity/pom.xml payment-identity/pom.xml
COPY payment-identity/src payment-identity/src
# Le reactor parent liste tous les modules : on ne construit que payment-identity.
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-identity/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S identity && adduser -S identity -G identity
COPY --from=build /workspace/payment-identity/target/payment-identity.jar app.jar
USER identity
EXPOSE 8094
ENTRYPOINT ["java", "-jar", "app.jar"]
