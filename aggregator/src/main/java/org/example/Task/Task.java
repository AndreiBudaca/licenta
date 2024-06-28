package org.example.Task;

import org.example.TTLQueue.TTLFindable;

import java.util.ArrayList;
import java.util.List;

public class Task implements TTLFindable<Integer> {
    private final int id;
    private final double trust;
    private final int requiredVotes;
    private final List<String> voters;
    private final List<Double> votes;

    public Task(int id, double trust, int requiredVotes) {
        this.id = id;
        this.trust = trust;
        this.requiredVotes = requiredVotes;
        this.voters = new ArrayList<>();
        this.votes = new ArrayList<>();
    }

    public Task(int id, double trust, int requiredVotes, List<String> voters, List<Double> votes) {
        this.id = id;
        this.trust = trust;
        this.requiredVotes = requiredVotes;
        this.voters = voters;
        this.votes = votes;
    }

    public double getTrust() {
        return trust;
    }

    public List<String> getVoters() {
        return voters;
    }

    public List<Double> getVotes() {
        return votes;
    }

    public int getRequiredVotes() {
        return requiredVotes;
    }

    @Override
    public Integer getIdentifier() {
        return id;
    }
    @Override
    public boolean hasIdentifier(Integer id) {
        return this.id == id;
    }
}
