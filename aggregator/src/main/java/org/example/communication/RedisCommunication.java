package org.example.communication;

import org.example.Task.ConcludedTask;
import redis.clients.jedis.Jedis;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

public class RedisCommunication {
    private final Jedis jedis;

    public RedisCommunication() {

        System.out.println("REDIS CONFIG");
        System.out.println("Redis host: " + EnvConfiguration.redisHost);
        System.out.println("Redis port: " + EnvConfiguration.redisPort);
        System.out.println();

        jedis = new Jedis(EnvConfiguration.redisHost, Integer.parseInt(EnvConfiguration.redisPort));
    }

    public void sendTask(ConcludedTask task, HashMap<String, Double> weights, boolean isPartial) {
        jedis.rpush(EnvConfiguration.outputQueue, String.format("%d %s", task.getIdentifier(), task.getTaskDecision().name()));

        if (isPartial) {
            jedis.rpush(EnvConfiguration.logsQueue, String.format("%d aggregator %d partial_response %s %d", task.getIdentifier(),
                   System.currentTimeMillis(), task.getTaskDecision().name(), System.currentTimeMillis() - task.getTimestamp()));
        }
        else {
            jedis.rpush(EnvConfiguration.logsQueue, String.format("%d aggregator %d final_response %s %d", task.getIdentifier(),
                    System.currentTimeMillis(), task.getTaskDecision().name(), System.currentTimeMillis() - task.getTimestamp()));

            StringBuilder builder = new StringBuilder(String.format("%d aggregator %d final_weights", task.getIdentifier(), System.currentTimeMillis()));
            for (String voter: weights.keySet()) {
                builder.append(String.format(" %s:%f", voter, weights.get(voter)));
            }
            jedis.rpush(EnvConfiguration.logsQueue, builder.toString());
        }
    }

    public void logNewTask(int taskId) {
        jedis.rpush(EnvConfiguration.logsQueue, String.format("%d aggregator %d new_time", taskId, System.currentTimeMillis()));
    }

    public List<String> getMessage(String... inputQueue) {
        return jedis.blpop(1, inputQueue);
    }


    private static class EnvConfiguration {
        public final static String redisHost = System.getenv("REDIS_PORT") == null ?
                "localhost" :
                System.getenv("REDIS_PORT").split(":")[1].substring(2);

        public final static String redisPort = System.getenv("REDIS_PORT") == null ?
                "31381" :
                System.getenv("REDIS_PORT").split(":")[2];

        public final static String outputQueue = System.getenv("REDIS_OUTPUT") == null ?
                "result" : System.getenv("REDIS_OUTPUT");

        public final static String logsQueue = System.getenv("REDIS_LOG") == null ?
                "logs" : System.getenv("REDIS_LOG");
    }
}
