package org.example;

import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        String redisHost = "localhost";
        String redisPort = "31381";

        if (System.getenv("REDIS_HOST") != null)
            redisHost = System.getenv("REDIS_HOST");
        if (System.getenv("OWN_REDIS_PORT") != null)
            redisPort = System.getenv("OWN_REDIS_PORT");

        System.out.println("Host: " + redisHost);
        System.out.println("Port: " + redisPort);
        Jedis jedis = new Jedis(redisHost, Integer.parseInt(redisPort), 1000);

        while (true) {
            jedis.rpush("task_dispacher_input", "Messaj de interes national!");
            Thread.sleep(70);
        }
    }
}