# Build from repo root: docker build -f payment-content/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-content/pom.xml payment-content/pom.xml
COPY payment-content/src payment-content/src
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-content/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S content && adduser -S content -G content
COPY --from=build /workspace/payment-content/target/payment-content.jar app.jar
USER content
EXPOSE 8098
ENTRYPOINT ["java", "-jar", "app.jar"]
