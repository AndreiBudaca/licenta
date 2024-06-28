package org.example.Task;

import org.example.TTLQueue.TTLElementHandler;
import org.example.communication.RedisCommunication;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskExpiredHandler implements TTLElementHandler<Task> {
    private final TaskManager taskManager;
    private final ConcurrentLinkedQueue<ConcludedTask> conclusionedTasks;

    private final RedisCommunication redis;

    public TaskExpiredHandler(TaskManager taskManager,
                              ConcurrentLinkedQueue<ConcludedTask> concludedTasks,
                              RedisCommunication redis) {
        this.conclusionedTasks = concludedTasks;
        this.taskManager = taskManager;
        this.redis = redis;
    }

    @Override
    public void handleElement(Task task) {
        ConcludedTask partialVerdict = taskManager.givPartialVerdict(task);
        redis.sendTask(partialVerdict, null, true);
        conclusionedTasks.add(partialVerdict);
    }
}
