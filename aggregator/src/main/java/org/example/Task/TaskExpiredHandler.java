package org.example.Task;

import org.example.TTLQueue.TTLElementHandler;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskExpiredHandler implements TTLElementHandler<Task> {
    private TaskManager taskManager;
    private ConcurrentLinkedQueue<ConcludedTask> conclusionedTasks;

    public TaskExpiredHandler(TaskManager taskManager,
                               ConcurrentLinkedQueue<ConcludedTask> concludedTasks) {
        this.conclusionedTasks = concludedTasks;
        this.taskManager = taskManager;
    }

    @Override
    public void handleElement(Task task) {
        ConcludedTask decision = taskManager.giveFinalVerdict(task);
        // TODO send verdict
        conclusionedTasks.add(decision);
    }
}
