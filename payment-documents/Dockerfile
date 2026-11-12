# Build from repo root: docker build -f payment-documents/Dockerfile .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY payment-documents/pom.xml payment-documents/pom.xml
COPY payment-documents/src payment-documents/src
RUN chmod +x mvnw && ./mvnw -B -N install -DskipTests && ./mvnw -B -f payment-documents/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S documents && adduser -S documents -G documents \
    && mkdir -p /var/lib/payment-documents && chown -R documents:documents /var/lib/payment-documents
COPY --from=build /workspace/payment-documents/target/payment-documents.jar app.jar
USER documents
EXPOSE 8101
ENTRYPOINT ["java", "-jar", "app.jar"]
