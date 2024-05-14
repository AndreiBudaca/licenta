package org.example;

import redis.clients.jedis.Jedis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        String redisHost = "localhost";
        String redisPort = "31381";
        String redisQueue = "task_dispacher_input";

        int baseDelay = 10;
        int minNoise = -5;
        int maxNoise = 5;
        int messagesToSend = 10000;
        Random r = new Random();

        String payload = "5PD1ilZHEwetfz5Ob2v3RwZsiGYaSsZDwvlcWhP2WgCRsbiz4WWxIwD3riicShzGG41xqLCA18Kp4ZcidbfzF6AtBBBRYN6Gx8cxNUJTRTegANZsiWKaASSU0CndEaVGNJD6JncW5LVIyFDnS6pcGMUlrQePx6iyDqGOFG0QA3HemgVsJWBujVjyHZ9OVavPU54LeCm0MFVEA9rtJA";

        try (FileWriter logFile = new FileWriter("log_producer.txt")) {

            if (System.getenv("REDIS_HOST") != null)
                redisHost = System.getenv("REDIS_HOST");
            if (System.getenv("OWN_REDIS_PORT") != null)
                redisPort = System.getenv("OWN_REDIS_PORT");

            System.out.println("Host: " + redisHost);
            System.out.println("Port: " + redisPort);
            Jedis jedis = new Jedis(redisHost, Integer.parseInt(redisPort), 1000);

            int taskId = 0;
            long lastTimestamp = System.currentTimeMillis();
            for (int i = 0; i < messagesToSend; ++i){
                int sleepValue = (int) (baseDelay + minNoise + (maxNoise - minNoise) * r.nextDouble());
                long currentTimestamp = System.currentTimeMillis();
                String message = taskId + "." + currentTimestamp + "." + payload;
                jedis.rpush(redisQueue, message);

                long operationMillis = currentTimestamp - lastTimestamp;

                logFile.write(taskId + " " + operationMillis + '\n');

                ++taskId;
                lastTimestamp = currentTimestamp;
                Thread.sleep(sleepValue);
            }
        }

    }
}