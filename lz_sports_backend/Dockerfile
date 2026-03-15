FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG JAR_FILE=target/lz_sports_backend-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
