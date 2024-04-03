FROM eclipse-temurin:17-jdk-alpine

COPY target/Redis_producer-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]