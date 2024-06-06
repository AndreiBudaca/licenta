package org.example.communication;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisCommunication {
    private final Jedis jedis;

    public RedisCommunication() {

        System.out.println("REDIS CONFIG");
        System.out.println("Redis host: " + EnvConfiguration.redisHost);
        System.out.println("Redis port: " + EnvConfiguration.redisPort);
        System.out.println();

        jedis = new Jedis(EnvConfiguration.redisHost, Integer.parseInt(EnvConfiguration.redisPort));
    }

    public void sendMessage(String message, String outputQueue) {
        jedis.rpush(outputQueue, message);
    }

    public List<String> getMessage(String... inputQueue) {
        return jedis.blpop(1, inputQueue);
    }


    private static class EnvConfiguration {
        public final static String redisHost = System.getenv("REDIS_PORT") == null ?
                "localhost" :
                System.getenv("REDIS_PORT").split(":")[1].substring(2);

        public final static String redisPort = System.getenv("REDIS_PORT") == null ?
                "31381" :
                System.getenv("REDIS_PORT").split(":")[2];
    }
}
