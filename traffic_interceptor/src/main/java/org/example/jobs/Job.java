package org.example.jobs;

import org.example.delay.Delay;

public class Job {
    private final int requests;
    private final Delay delay;

    public Job(int requests, Delay delay) {
        this.requests = requests;
        this.delay = delay;
    }

    public int getRequests() {
        return requests;
    }

    public Delay getDelay() {
        return delay;
    }
}
