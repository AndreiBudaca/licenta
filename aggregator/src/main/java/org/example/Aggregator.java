package org.example;

import org.example.TTLQueue.TTLQueue;
import org.example.Task.*;
import org.example.communication.RedisCommunication;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Aggregator {
    private final RedisCommunication redis;
    private final TaskManager taskManager;
    private final TTLQueue<Task, Integer> activeQueue;
    private final ConcurrentLinkedQueue<ConcludedTask> conclusionedTasks;

    public Aggregator() {

        redis = new RedisCommunication();
        taskManager = new TaskManager();
        conclusionedTasks = new ConcurrentLinkedQueue<>();

        TaskExpiredHandler taskExpiredHandler = new TaskExpiredHandler(taskManager, redis, conclusionedTasks);
        activeQueue = new TTLQueue<>(EnvConfiguration.taskTTL, EnvConfiguration.maxActiveTasks, taskExpiredHandler);
    }

    public void HandleTasks() {
        while (true) {
            List<String> message = redis.getMessage(EnvConfiguration.faasInputQueue, EnvConfiguration.taskAlertQueue);

            if (message == null) continue;

            if (Objects.equals(message.getFirst(), EnvConfiguration.faasInputQueue)) {
                TaskVote taskVote = TaskMapper.taskVoteFromFaasResult(message.get(1));
                Task task = activeQueue.findElement(taskVote.getId());

                // Task was not found in active queue => the task was already concluded.
                if (task == null) {
                    ConcludedTask concludedTask = conclusionedTasks.stream()
                            .filter(it -> it.hasIdentifier(taskVote.getId()))
                            .findFirst().orElse(null);

                    if (concludedTask == null) continue;

                    Task updatedTask = taskManager.updateTask(concludedTask, taskVote);
                    conclusionedTasks.remove(concludedTask);

                    if (taskManager.canGiveFinalVerdict(updatedTask)) {
                        ConcludedTask finalVerdict = taskManager.giveFinalVerdict(updatedTask);

                        if (finalVerdict.getTaskDecision() != concludedTask.getTaskDecision()) {
                            // TODO: send task to output queue
                        }
                    }
                    else {
                        conclusionedTasks.add(new ConcludedTask(updatedTask.getIdentifier(), updatedTask.getTrust(), concludedTask.getTaskDecision()));
                    }
                }
                else {
                    Task updatedTask = taskManager.updateTask(task, taskVote);
                    if (taskManager.canGiveFinalVerdict(updatedTask)) {
                        activeQueue.deleteElement(updatedTask.getIdentifier());
                        ConcludedTask concludedTask = taskManager.giveFinalVerdict(updatedTask);
                        // TODO: send task to output queue
                    }
                    else {
                        activeQueue.updateElement(updatedTask);
                    }
                }
            }
            else if (Objects.equals(message.getFirst(), EnvConfiguration.taskAlertQueue)) {
                activeQueue.addElement(TaskMapper.taskFromTaskAlert(message.get(1)));
            }
        }
    }

    public void StopHandling() {
        activeQueue.stopHandling();
    }

    private static class EnvConfiguration {
        public final static String faasInputQueue = System.getenv("REDIS_FAAS_INPUT") == null ?
                "aggregator_input" : System.getenv("REDIS_FAAS_INPUT");

        public final static String taskAlertQueue = System.getenv("REDIS_TASK_ALERT") == null ?
                "task_alert" : System.getenv("REDIS_TASK_ALERT");

        public final static int taskTTL = System.getenv("TASK_TTL") == null ?
                2000 : Integer.parseInt(System.getenv("TASK_TTL"));

        public final static int maxActiveTasks = System.getenv("TASK_TTL") == null ?
                20000 : Integer.parseInt(System.getenv("TASK_TTL"));
    }
}
