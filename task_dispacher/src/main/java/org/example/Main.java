package org.example;

import io.kubernetes.client.openapi.ApiException;
import org.example.communication.KubernetesCommunication;
import org.example.communication.RedisCommunication;
import org.example.loadBalancing.AsyncQueueObserverLoadBalancer;
import org.example.loadBalancing.QueueLengthLoadBalancer;
import org.example.taskHandling.AsyncQueueObserverTaskHandler;
import org.example.taskHandling.QueueLenghtTaskHandler;
import org.example.taskHandling.TaskHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException, ApiException {
        String faasName = generateFaasName();

        RedisCommunication redis = new RedisCommunication(faasName);
        KubernetesCommunication kube = new KubernetesCommunication(faasName);

        TaskHandler handler = Objects.equals(System.getenv("LOAD_BALANCER"), "rules") ?
                new QueueLenghtTaskHandler(new QueueLengthLoadBalancer()) :
                new AsyncQueueObserverTaskHandler(new AsyncQueueObserverLoadBalancer(faasName, kube));

        handler.handleTasks(faasName, redis, kube);
        kube.deleteDeployment();
    }

    private static String generateFaasName() {
        StringBuilder faasNameBuilder = new StringBuilder("faas-");
        Random r = new Random();
        for (int i = 0; i < 10; ++i) {
            faasNameBuilder.append((char) ('a' + r.nextInt(26)));
        }

        return faasNameBuilder.toString();
    }
}