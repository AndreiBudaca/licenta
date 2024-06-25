FROM eclipse-temurin:17-jdk-alpine

COPY target/Redis_producer-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENV REDIS_HOST "10.106.20.49"
ENV OWN_REDIS_PORT "6379"

ENTRYPOINT ["java","-jar","/app.jar"]