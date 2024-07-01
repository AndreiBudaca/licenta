package org.example.Task;

import org.example.TTLQueue.TTLElementHandler;
import org.example.communication.RedisCommunication;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskExpiredHandler implements TTLElementHandler<Task> {
    private final TaskManager taskManager;
    private final ConcurrentLinkedQueue<ConcludedTask> conclusionedTasks;
    private final RedisCommunication redis = new RedisCommunication();

    public TaskExpiredHandler(TaskManager taskManager,
                              ConcurrentLinkedQueue<ConcludedTask> concludedTasks) {
        this.conclusionedTasks = concludedTasks;
        this.taskManager = taskManager;
    }

    @Override
    public void handleElement(Task task) {
        ConcludedTask partialVerdict = taskManager.givePartialVerdict(task);
        redis.sendTask(partialVerdict, null, true);
        conclusionedTasks.add(partialVerdict);
    }
}
