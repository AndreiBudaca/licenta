package org.example.Task;

import java.util.List;

public class ConcludedTask extends Task{
    private TaskDecision taskDecision;

    public ConcludedTask(int id, double trust, int requiredVotes, long timestamp, List<String> voters, List<Double> votes, TaskDecision taskDecision) {
        super(id, trust, requiredVotes, timestamp, voters, votes);
        this.taskDecision = taskDecision;
    }

    public TaskDecision getTaskDecision() {
        return taskDecision;
    }

    public void setTaskDecision(TaskDecision taskDecision) {
        this.taskDecision = taskDecision;
    }
}
