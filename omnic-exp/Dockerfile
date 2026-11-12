# Image d'exécution. Le jar est produit avant : mvn -DskipTests package
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S omnicanal && adduser -S omnicanal -G omnicanal
COPY target/omnicanal.jar app.jar
USER omnicanal
EXPOSE 5981
ENTRYPOINT ["java", "-jar", "app.jar"]
