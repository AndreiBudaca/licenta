FROM openjdk:21-jdk

COPY target/task_dispacher-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENV REDIS_HOST "redis"
ENV REDIS_PORT "test"

ENTRYPOINT ["java","-jar","/app.jar"]