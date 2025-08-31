FROM openjdk:17-jdk-slim

VOLUME /tmp

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app.jar"]