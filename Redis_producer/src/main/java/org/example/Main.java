package org.example;

import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) {
        String redisHost = "localhost";
        String redisPort = "6379";

        if (System.getenv("REDIS_HOST") != null)
            redisHost = System.getenv("REDIS_HOST");
        if (System.getenv("OWN_REDIS_PORT") != null)
            redisPort = System.getenv("OWN_REDIS_PORT");

        System.out.println("Host: " + redisHost);
        System.out.println("Port: " + redisPort);
        Jedis jedis = new Jedis(redisHost, Integer.parseInt(redisPort), 1000);

        while (true) {
            try {

                for (int i = 0; i < 2147483647; ++i) {
                    jedis.rpush("faas_input", "Message " + i);
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage() + ": " + System.currentTimeMillis());
            }
        }
    }
}