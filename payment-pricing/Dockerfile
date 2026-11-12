# Build from repo root: docker build -f payment-pricing/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-pricing/pom.xml payment-pricing/pom.xml
COPY payment-pricing/src payment-pricing/src
# Le reactor parent liste tous les modules : on ne construit que payment-pricing.
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-pricing/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S pricing && adduser -S pricing -G pricing
COPY --from=build /workspace/payment-pricing/target/payment-pricing.jar app.jar
USER pricing
EXPOSE 8095
ENTRYPOINT ["java", "-jar", "app.jar"]
