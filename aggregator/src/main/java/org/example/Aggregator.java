package org.example;

import org.example.TTLQueue.TTLList;
import org.example.Task.*;
import org.example.communication.RedisCommunication;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Aggregator {
    private final RedisCommunication redis;
    private final TaskManager taskManager;
    private final TTLList<Task, Integer> activeTasks;
    private final ConcurrentLinkedQueue<ConcludedTask> concludedTasks;

    public Aggregator() {

        redis = new RedisCommunication();
        taskManager = new TaskManager();
        concludedTasks = new ConcurrentLinkedQueue<>();

        TaskExpiredHandler taskExpiredHandler = new TaskExpiredHandler(taskManager, concludedTasks);
        activeTasks = new TTLList<>(EnvConfiguration.taskTTL, EnvConfiguration.maxActiveTasks, taskExpiredHandler);
    }

    public void HandleTasks() {
        while (true) {
            List<String> message = redis.getMessage(EnvConfiguration.configQueue, EnvConfiguration.taskAlertQueue,
                    EnvConfiguration.faasInputQueue);

            if (message == null) continue;

            if (Objects.equals(message.getFirst(), EnvConfiguration.faasInputQueue)) {
                TaskVote taskVote = TaskMapper.taskVoteFromFaasResult(message.get(1));
                Task task = activeTasks.findElement(taskVote.getId());

                // Task was not found in active queue => the task was already concluded.
                if (task == null) {
                    ConcludedTask concludedTask = concludedTasks.stream()
                            .filter(it -> it.hasIdentifier(taskVote.getId()))
                            .findFirst().orElse(null);

                    if (concludedTask == null) continue;

                    Task updatedTask = taskManager.updateTask(concludedTask, taskVote);
                    concludedTasks.remove(concludedTask);

                    // If a final verdict can be made, compute it and send it
                    if (taskManager.canGiveFinalVerdict(updatedTask)) {
                        ConcludedTask finalVerdict = taskManager.giveFinalVerdict(updatedTask);

                        redis.sendTask(finalVerdict, taskManager.getWeights(), false);
                    }
                    // Else, put the task back into the concluded  task queue
                    else {
                        concludedTasks.add(new ConcludedTask(updatedTask.getIdentifier(), updatedTask.getTrust(), updatedTask.getRequiredVotes(),
                                updatedTask.getVoters(), updatedTask.getVotes(), concludedTask.getTaskDecision()));
                    }
                }
                else {
                    Task updatedTask = taskManager.updateTask(task, taskVote);
                    if (taskManager.canGiveFinalVerdict(updatedTask)) {
                        activeTasks.deleteElement(updatedTask.getIdentifier());
                        ConcludedTask concludedTask = taskManager.giveFinalVerdict(updatedTask);

                        redis.sendTask(concludedTask, taskManager.getWeights(), false);
                    } else if (taskManager.canGivePartialVerdict(updatedTask)) {
                        activeTasks.deleteElement(updatedTask.getIdentifier());
                        ConcludedTask partialVerdict = taskManager.givPartialVerdict(updatedTask);

                        redis.sendTask(partialVerdict, null, true);

                        concludedTasks.add(new ConcludedTask(partialVerdict.getIdentifier(), partialVerdict.getTrust(), partialVerdict.getRequiredVotes(),
                                partialVerdict.getVoters(), partialVerdict.getVotes(), partialVerdict.getTaskDecision()));
                    }
                    else {
                        activeTasks.updateElement(updatedTask);
                    }
                }
            }
            else if (Objects.equals(message.getFirst(), EnvConfiguration.taskAlertQueue)) {
                Task newTask = TaskMapper.taskFromTaskAlert(message.get(1));
                activeTasks.addElement(newTask);
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
        activeTasks.stopHandling();
    }

    private static class EnvConfiguration {
        public final static String faasInputQueue = System.getenv("REDIS_FAAS_INPUT") == null ?
                "faas_output" : System.getenv("REDIS_FAAS_INPUT");

        public final static String taskAlertQueue = System.getenv("REDIS_TASK_ALERT") == null ?
                "task_alert" : System.getenv("REDIS_TASK_ALERT");

        public final static String configQueue = System.getenv("REDIS_CONFIG") == null ?
                "aggregator_config" : System.getenv("REDIS_CONFIG");

        public final static int taskTTL = System.getenv("TASK_TTL") == null ?
                1000 : Integer.parseInt(System.getenv("TASK_TTL"));

        public final static int maxActiveTasks = System.getenv("MAX_TASKS") == null ?
                200000 : Integer.parseInt(System.getenv("MAX_TASKS"));
    }
}
