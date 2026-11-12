# Build from repo root: docker build -f payment-divers/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-divers/pom.xml payment-divers/pom.xml
COPY payment-divers/src payment-divers/src
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-divers/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S divers && adduser -S divers -G divers
COPY --from=build /workspace/payment-divers/target/payment-divers.jar app.jar
USER divers
EXPOSE 8102
ENTRYPOINT ["java", "-jar", "app.jar"]
