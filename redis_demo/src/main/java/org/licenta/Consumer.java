package org.licenta;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Consumer {
    public static void main(String[] args) {
        JedisPool pool = new JedisPool("redis", 6379);

        try (Jedis jedis = pool.getResource()) {
            String message = jedis.spop("list_test");
            while (message != null) {
                System.out.println(message);
                message = jedis.spop("list_test");
            }
        }
    }
}
