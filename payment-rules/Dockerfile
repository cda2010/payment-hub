# Build from repo root: docker build -f payment-rules/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-rules/pom.xml payment-rules/pom.xml
COPY payment-rules/src payment-rules/src
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-rules/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S rules && adduser -S rules -G rules
COPY --from=build /workspace/payment-rules/target/payment-rules.jar app.jar
USER rules
EXPOSE 8099
ENTRYPOINT ["java", "-jar", "app.jar"]
