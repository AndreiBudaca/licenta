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

        TaskExpiredHandler taskExpiredHandler = new TaskExpiredHandler(taskManager, conclusionedTasks, redis);
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

                    // If a final verdict can be made, compute it and send it
                    if (taskManager.canGiveFinalVerdict(updatedTask)) {
                        ConcludedTask finalVerdict = taskManager.giveFinalVerdict(updatedTask);

                        redis.sendTask(finalVerdict, taskManager.getWeights(), false);
                    }
                    // Else, put the task back into the conclusioned task queue
                    else {
                        conclusionedTasks.add(new ConcludedTask(updatedTask.getIdentifier(), updatedTask.getTrust(), updatedTask.getVoters(),
                                updatedTask.getVotes(), concludedTask.getTaskDecision()));
                    }
                }
                else {
                    Task updatedTask = taskManager.updateTask(task, taskVote);
                    if (taskManager.canGiveFinalVerdict(updatedTask)) {
                        activeQueue.deleteElement(updatedTask.getIdentifier());
                        ConcludedTask concludedTask = taskManager.giveFinalVerdict(updatedTask);

                        redis.sendTask(concludedTask, taskManager.getWeights(), false);
                    } else if (taskManager.canGivePartialVerdict(updatedTask)) {
                        ConcludedTask partialVerdict = taskManager.givPartialVerdict(updatedTask);

                        redis.sendTask(partialVerdict, null, true);

                        conclusionedTasks.add(new ConcludedTask(partialVerdict.getIdentifier(), partialVerdict.getTrust(), partialVerdict.getVoters(),
                                partialVerdict.getVotes(), partialVerdict.getTaskDecision()));
                    }
                    else {
                        activeQueue.updateElement(updatedTask);
                    }
                }
            }
            else if (Objects.equals(message.getFirst(), EnvConfiguration.taskAlertQueue)) {
                Task newTask = TaskMapper.taskFromTaskAlert(message.get(1));
                activeQueue.addElement(newTask);
                redis.logNewTask(newTask.getIdentifier());
            }
            else if (Objects.equals(message.getFirst(), EnvConfiguration.configQueue)) {
                if (Objects.equals(message.get(1), "quit")) break;

                String[] messageBits = message.get(1).split(";");
                if (Objects.equals(messageBits[0], "add_module")) {
                    taskManager.addVoter(messageBits[1]);
                } else if (Objects.equals(messageBits[0], "delete_module")) {
                    taskManager.deleteVote(messageBits[1]);
                }
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

        public final static String configQueue = System.getenv("REDIS_CONFIG") == null ?
                "aggregator_config" : System.getenv("REDIS_CONFIG");

        public final static int taskTTL = System.getenv("TASK_TTL") == null ?
                2000 : Integer.parseInt(System.getenv("TASK_TTL"));

        public final static int maxActiveTasks = System.getenv("TASK_TTL") == null ?
                20000 : Integer.parseInt(System.getenv("TASK_TTL"));
    }
}
