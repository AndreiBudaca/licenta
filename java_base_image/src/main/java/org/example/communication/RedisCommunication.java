package org.example.communication;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisCommunication implements Communication {
    private String inputQueue = "faas_input";
    private String outputQueue = "faas_output";
    private final Jedis jedis;

    public RedisCommunication() {
        String redisHost = "localhost";
        String redisPort = "6379";

        String redisConnection = null;
        if (System.getenv("REDIS_PORT") != null)
            redisConnection = System.getenv("REDIS_PORT");

        if (redisConnection != null) {
            String[] connectionInfo = redisConnection.split(":");
            redisHost = connectionInfo[1].substring(2);
            redisPort = connectionInfo[2];
        }

        if (System.getenv("REDIS_INPUT") != null)
            inputQueue = System.getenv("REDIS_INPUT");
        if (System.getenv("REDIS_OUTPUT") != null)
            outputQueue = System.getenv("REDIS_OUTPUT");

        System.out.println("REDIS CONFIG");
        System.out.println("Redis host: " + redisHost);
        System.out.println("Redis port: " + redisPort);
        System.out.println("Input queue: " + inputQueue);
        System.out.println("Output queue: " + outputQueue);
        System.out.println();

        jedis = new Jedis(redisHost, Integer.parseInt(redisPort), 10);
    }

    @Override
    public String getMessage() {
        while (true) {
            List<String> message = jedis.blpop(60, inputQueue);
            if (message != null) return message.get(1);
        }
    }

    @Override
    public void sendMessage(String message) {
        jedis.rpush(outputQueue, message);
    }
}
