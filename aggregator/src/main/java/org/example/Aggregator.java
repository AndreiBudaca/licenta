package org.example;

import org.example.TTLQueue.TTLQueue;
import org.example.Task.Task;
import org.example.Task.TaskExpiredHandler;
import org.example.communication.RedisCommunication;

import java.util.List;

public class Aggregator {
    private final RedisCommunication redis;
    private final TaskExpiredHandler taskExpiredHandler;
    private final TTLQueue<Task, Integer> activeQueue;

    public Aggregator() {
        redis = new RedisCommunication();
        taskExpiredHandler = new TaskExpiredHandler();
        activeQueue = new TTLQueue<>(EnvConfiguration.taskTTL, EnvConfiguration.maxActiveTasks, taskExpiredHandler);
    }

    public void HandleTasks() {
        while (true) {
            List<String> message = redis.getMessage(EnvConfiguration.faasInputQueue, EnvConfiguration.taskAlertQueue);
        }
    }

    public void StopHandlig() {
        activeQueue.stopHandling();
    }

    private static class EnvConfiguration {
        public final static String faasInputQueue = System.getenv("REDIS_FAAS_INPUT") == null ?
                "aggregator_input" : System.getenv("REDIS_FAAS_INPUT");

        public final static String taskAlertQueue = System.getenv("REDIS_TASK_ALERT") == null ?
                "task_alert" : System.getenv("REDIS_TASK_ALERT");

        public final static String outputQueue = System.getenv("REDIS_INPUT") == null ?
                "task_dispacher_input" : System.getenv("REDIS_INPUT");

        public final static int taskTTL = System.getenv("TASK_TTL") == null ?
                2000 : Integer.parseInt(System.getenv("TASK_TTL"));

        public final static int maxActiveTasks = System.getenv("TASK_TTL") == null ?
                20000 : Integer.parseInt(System.getenv("TASK_TTL"));
    }
}
