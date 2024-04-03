package org.example;

import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) {
        String redisHost = "redis";
        String redisPort = "6379";

        while (true) {
            try {
                Jedis jedis = new Jedis(redisHost, Integer.parseInt(redisPort), 10);
                for (int i = 0; i < 2147483647; ++i) {
                    jedis.rpush("faas_input", "Message " + i);
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}