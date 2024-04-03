FROM openjdk:21-jdk

ENV REDIS_HOST "redis"
ENV REIDS_PORT "6379"
ENV REDIS_INPUT "faas_input"
ENV REDIS_OUTPUT "faas_output"
ENV FAAS_PATH "/faas.jar"
ENV FAAS_MAIN_CLASS "org.faas.Main"
ENV FAAS_MAIN_METHOD "main"

COPY target/java_base_image-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]