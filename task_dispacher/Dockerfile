FROM openjdk:21-jdk

COPY target/task_dispacher-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENV REDIS_INPUT "task_dispacher_input"
ENV REDIS_PORT "test"

ENV FAAS_REDIS_OUTPUT "faas_output"

ENV FAAS_NAMESPACE "default"
ENV FAAS_IMAGE "andreibudaca/echo_faas:latest"

ENTRYPOINT ["java","-jar","/app.jar"]