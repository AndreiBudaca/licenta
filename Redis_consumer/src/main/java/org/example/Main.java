package org.example;

import redis.clients.jedis.Jedis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        String redisHost = "localhost";
        String redisPort = "31381";
        String redisQueue = "faas_output";

        try (FileWriter logFile = new FileWriter("log_consumer.txt")) {

            if (System.getenv("REDIS_HOST") != null)
                redisHost = System.getenv("REDIS_HOST");
            if (System.getenv("OWN_REDIS_PORT") != null)
                redisPort = System.getenv("OWN_REDIS_PORT");

            System.out.println("Host: " + redisHost);
            System.out.println("Port: " + redisPort);
            Jedis jedis = new Jedis(redisHost, Integer.parseInt(redisPort), 1000);

            while (true) {
                List<String> message = jedis.blpop(10, redisQueue);

                if (message == null) break;

                int taskId = Integer.parseInt(message.get(1).split("\\.")[0]);
                long startedTimestamp = Long.parseLong(message.get(1).split("\\.")[1]);
                long consumedTimestamp = System.currentTimeMillis();

                long operationMillis = startedTimestamp - consumedTimestamp;
                logFile.write(taskId + " " + operationMillis + '\n');
            }
        }
    }
}