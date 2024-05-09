package org.example.communication;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisCommunication {
    private final Jedis jedis;

    private RedisCommunication() {
        String redisHost = "localhost";
        String redisPort = "38226";

        String redisConnection = null;
        if (System.getenv("REDIS_PORT") != null)
            redisConnection = System.getenv("REDIS_PORT");

        if (redisConnection != null) {
            String[] connectionInfo = redisConnection.split(":");
            redisHost = connectionInfo[1].substring(2);
            redisPort = connectionInfo[2];
        }

        System.out.println("Host: " + redisHost);
        System.out.println("Port: " + redisPort);

        jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
    }

    public void sendMessage(String queueName, String message) {
        jedis.rpush(queueName, message);
    }

    public List<String> getMessage(String... queueName) {
        return jedis.blpop(60, queueName);
    }

    public static RedisCommunication getInstance() {
        return HelperHolder.instance;
    }

    private static class HelperHolder {
        private static final RedisCommunication instance = new RedisCommunication();
    }
}
