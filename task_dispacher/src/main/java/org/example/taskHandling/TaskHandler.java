package org.example.taskHandling;

import org.example.communication.KubernetesCommunication;
import org.example.communication.RedisCommunication;

public interface TaskHandler {
    void handleTasks(String faasName, RedisCommunication redis, KubernetesCommunication kube);
}
