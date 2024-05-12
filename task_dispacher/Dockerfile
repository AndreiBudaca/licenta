FROM openjdk:21-jdk

COPY target/task_dispacher-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENV REDIS_INPUT "task_dispacher_input"
ENV REDIS_PORT "tcp://localhost:31381"

ENV FAAS_REDIS_OUTPUT "faas_output"

ENV FAAS_NAMESPACE "default"
ENV FAAS_IMAGE "andreibudaca/echo_faas:latest"
ENV MIN_FAAS_REPLICA "1"
ENV MAX_FAAS_REPLICA "5"

ENTRYPOINT ["java","-jar","/app.jar"]