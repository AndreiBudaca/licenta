package org.licenta;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

public class Consumer {
    public static void main(String[] args) {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            List<String> message = jedis.blpop(1, "list_test");
            while (message != null) {
                System.out.println(message);
                message = jedis.blpop(1, "list_test");
            }
        }
    }
}
