# Build from repo root: docker build -f payment-commission/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-commission/pom.xml payment-commission/pom.xml
COPY payment-commission/src payment-commission/src
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-commission/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S commission && adduser -S commission -G commission
COPY --from=build /workspace/payment-commission/target/payment-commission.jar app.jar
USER commission
EXPOSE 8098
ENTRYPOINT ["java", "-jar", "app.jar"]
