package org.example.Task;

public class ConcludedTask extends Task{

    private TaskDecision taskDecision;

    public ConcludedTask(int id, double trust, TaskDecision taskDecision) {
        super(id, trust);
        this.taskDecision = taskDecision;
    }

    public TaskDecision getTaskDecision() {
        return taskDecision;
    }

    public void setTaskDecision(TaskDecision taskDecision) {
        this.taskDecision = taskDecision;
    }
}
