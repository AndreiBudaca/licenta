FROM andreibudaca/java_base_image:2.0

ENV FAAS_MAIN_CLASS "org.faas.Main"
ENV FAAS_MAIN_METHOD "echo"

ENV FAAS_PATH "faas.jar"

COPY target/java_echo-1.0-SNAPSHOT-jar-with-dependencies.jar faas.jar