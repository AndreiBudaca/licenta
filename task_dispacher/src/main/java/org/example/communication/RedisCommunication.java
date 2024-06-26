package org.example.communication;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisCommunication {
    private final Jedis jedis;

    public RedisCommunication(String faasName) {
        EnvConfiguration.outputQueue = faasName;

        System.out.println("REDIS CONFIG");
        System.out.println("Redis host: " + EnvConfiguration.redisHost);
        System.out.println("Redis port: " + EnvConfiguration.redisPort);
        System.out.println("Input queue: " + EnvConfiguration.inputQueue);
        System.out.println("Output queue: " + EnvConfiguration.outputQueue);
        System.out.println();

        jedis = new Jedis(EnvConfiguration.redisHost, Integer.parseInt(EnvConfiguration.redisPort));
    }

    public void sendMessage(String message) {
        jedis.rpush(EnvConfiguration.outputQueue, message);
    }

    public List<String> getMessage() {
        return jedis.blpop(1, EnvConfiguration.inputQueue);
    }

    public void sendLog(String message) {
        jedis.rpush(EnvConfiguration.logQueue, message);
    }

    public long getOutputQueueLength() {
        return jedis.llen(EnvConfiguration.outputQueue);
    }

    private static class EnvConfiguration {
        public final static String redisHost = System.getenv("REDIS_PORT") == null ?
                "localhost" :
                System.getenv("REDIS_PORT").split(":")[1].substring(2);

        public final static String redisPort = System.getenv("REDIS_PORT") == null ?
                "31381" :
                System.getenv("REDIS_PORT").split(":")[2];

        public final static String inputQueue = System.getenv("REDIS_INPUT") == null ?
                "task_dispacher_input" : System.getenv("REDIS_INPUT");

        public final static String logQueue = System.getenv("REDIS_LOG") == null ?
                "logs" : System.getenv("REDIS_LOG");

        public static String outputQueue = "tunnel";
    }
}
