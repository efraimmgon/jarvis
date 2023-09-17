FROM openjdk:8-alpine

COPY target/uberjar/jarvis.jar /jarvis/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/jarvis/app.jar"]
