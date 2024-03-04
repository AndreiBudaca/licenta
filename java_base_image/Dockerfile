FROM eclipse-temurin:17-jdk-alpine

ENV REDIS_HOST "redis"
ENV REIDS_PORT "6379"
ENV REDIS_INPUT "faas_input"
ENV REDIS_OUTPUT "faas_output"
ENV FAAS_PATH "/faas.jar"

COPY target/java_base_image-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]