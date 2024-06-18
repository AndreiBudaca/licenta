package org.example.Task;

import org.example.TTLQueue.TTLFindable;

import java.util.ArrayList;
import java.util.List;

public class Task implements TTLFindable<Integer> {
    private final int id;
    private double trust;
    private final List<String> voters;

    public Task(int id, double trust) {
        this.id = id;
        this.trust = trust;
        this.voters = new ArrayList<>();
    }

    public Task(int id, double trust, List<String> voters) {
        this.id = id;
        this.trust = trust;
        this.voters = voters;
    }

    public double getTrust() {
        return trust;
    }

    public List<String> getVoters() {
        return voters;
    }

    public void setTrust(double trust) {
        this.trust = trust;
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
