# Build from repo root: docker build -f payment-alias/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-alias/pom.xml payment-alias/pom.xml
COPY payment-alias/src payment-alias/src
# Le reactor parent liste tous les modules : on ne construit que payment-alias.
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-alias/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S alias && adduser -S alias -G alias
COPY --from=build /workspace/payment-alias/target/payment-alias.jar app.jar
USER alias
EXPOSE 8097
ENTRYPOINT ["java", "-jar", "app.jar"]
