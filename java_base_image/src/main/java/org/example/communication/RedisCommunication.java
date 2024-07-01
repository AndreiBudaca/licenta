package org.example.communication;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisCommunication implements Communication {
    private final Jedis jedis;

    public RedisCommunication() {
        System.out.println("REDIS CONFIG");
        System.out.println("Redis host: " + EnvConfiguration.redisHost);
        System.out.println("Redis port: " + EnvConfiguration.redisPort);
        System.out.println("Input queue: " + EnvConfiguration.inputQueue);
        System.out.println("Output queue: " + EnvConfiguration.outputQueue);
        System.out.println();

        jedis = new Jedis(EnvConfiguration.redisHost, Integer.parseInt(EnvConfiguration.redisPort), 10);
    }

    @Override
    public String getMessage() {
        while (true) {
            List<String> message = jedis.blpop(0, EnvConfiguration.inputQueue);
            if (message != null) return message.get(1);
        }
    }

    @Override
    public void sendMessage(String message) {
        jedis.rpush(EnvConfiguration.outputQueue, message);
    }

    @Override
    public void sendLog(String message) {
        jedis.rpush(EnvConfiguration.logQueue, message);
    }

    private static class EnvConfiguration {
        public final static String redisHost = System.getenv("REDIS_SERVICE_HOST") == null ?
                "localhost" :
                System.getenv("REDIS_SERVICE_HOST");

        public final static String redisPort = System.getenv("REDIS_SERVICE_PORT") == null ?
                "31381" :
                System.getenv("REDIS_SERVICE_PORT");

        public final static String inputQueue = System.getenv("REDIS_INPUT") == null ?
                "good-analysis2" : System.getenv("REDIS_INPUT");

        public final static String outputQueue = System.getenv("REDIS_OUTPUT") == null ?
                "faas_output" : System.getenv("REDIS_OUTPUT");

        public final static String logQueue = System.getenv("REDIS_LOG") == null ?
                "logs" : System.getenv("REDIS_LOG");
    }
}
