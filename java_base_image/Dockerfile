FROM openjdk:21-jdk

ENV REDIS_INPUT "faas_input"
ENV REDIS_OUTPUT "faas_output"

COPY target/java_base_image-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]