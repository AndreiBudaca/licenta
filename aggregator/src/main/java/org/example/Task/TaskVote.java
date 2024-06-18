package org.example.Task;

public class TaskVote {
    private final int id;
    private final double trust;
    private final String voter;

    public TaskVote(int id, double trust, String voter) {
        this.id = id;
        this.trust = trust;
        this.voter = voter;
    }

    public int getId() {
        return id;
    }

    public double getTrust() {
        return trust;
    }

    public String getVoter() {
        return voter;
    }
}
