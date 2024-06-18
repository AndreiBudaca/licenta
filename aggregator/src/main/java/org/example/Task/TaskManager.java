package org.example.Task;

public class TaskManager {
    public Task updateTask(Task task, TaskVote taskVote) {
        return new Task(0, 0.0);
    }

    public ConcludedTask giveFinalVerdict(Task task) {
        return new ConcludedTask(0, 0.0, TaskDecision.Hostile);
    }

    public boolean canGiveFinalVerdict(Task task) {
        return false;
    }
}
