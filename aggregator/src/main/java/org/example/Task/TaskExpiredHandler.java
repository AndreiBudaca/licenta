package org.example.Task;

import org.example.TTLQueue.TTLElementHandler;

public class TaskExpiredHandler implements TTLElementHandler<Task> {
    @Override
    public void handleElement(Task element) {
        System.out.println("Element pulled of queue" + element.getId() + ": " + element.getText());
    }
}
