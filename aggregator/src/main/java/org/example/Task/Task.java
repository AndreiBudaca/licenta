package org.example.Task;

import org.example.TTLQueue.TTLFindable;

public class Task implements TTLFindable<Integer> {
    private final int id;
    private final String message;

    public Task(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return message;
    }

    @Override
    public boolean hasIdentifier(Integer id) {
        return this.id == id;
    }
}
