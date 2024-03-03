package org.licenta;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Producer {
    public static void main(String[] args) {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.rpush("list_test", "Test0", "asd");
        }

        System.out.println("Done producing messages...");
    }
}