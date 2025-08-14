FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/pricewatcher-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]