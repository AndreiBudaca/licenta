package org.licenta;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Producer {
    public static void main(String[] args) {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.rpush("faas_input", "I can sleep");
        }

        System.out.println("Done producing messages...");
    }
}