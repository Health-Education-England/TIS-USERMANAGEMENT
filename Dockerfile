FROM openjdk:8-alpine

EXPOSE 8100

COPY target/app.jar app.jar

CMD ["java", "-jar", "app.jar"]
