package org.example.communication;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisCommunication {
    private final Jedis jedis;

    public RedisCommunication() {

        System.out.println("REDIS CONFIG");
        System.out.println("Redis host: " + EnvConfiguration.redisHost);
        System.out.println("Redis port: " + EnvConfiguration.redisPort);
        System.out.println("Input queue: " + EnvConfiguration.inputQueue);
        System.out.println();

        jedis = new Jedis(EnvConfiguration.redisHost, Integer.parseInt(EnvConfiguration.redisPort));
    }

    public void sendMessage(String queue, String message) {
        jedis.rpush(queue, message);
    }

    public void sendLog(String message) {
        jedis.rpush(EnvConfiguration.logQueue, message);
    }

    public List<String> getMessage() {
        return jedis.blpop(1, EnvConfiguration.inputQueue);
    }


    private static class EnvConfiguration {
        public final static String redisHost = System.getenv("REDIS_SERVICE_HOST") == null ?
                "localhost" :
                System.getenv("REDIS_SERVICE_HOST");

        public final static String redisPort = System.getenv("REDIS_SERVICE_PORT") == null ?
                "31381" :
                System.getenv("REDIS_SERVICE_PORT");

        public final static String inputQueue = System.getenv("REDIS_INPUT") == null ?
                "traffic_interceptor_command" : System.getenv("REDIS_INPUT");

        public final static String logQueue = System.getenv("REDIS_LOG") == null ?
                "logs" : System.getenv("REDIS_LOG");
    }
}
