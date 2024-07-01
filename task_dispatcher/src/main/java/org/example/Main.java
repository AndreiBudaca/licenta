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
        String faasName = EnvConfiguration.faasName;

        RedisCommunication redis = new RedisCommunication(faasName);
        KubernetesCommunication kube = new KubernetesCommunication(faasName);

        TaskHandler handler = Objects.equals(EnvConfiguration.loadBalancer, "rules") ?
                new QueueLenghtTaskHandler(new QueueLengthLoadBalancer()) :
                new AsyncQueueObserverTaskHandler(new AsyncQueueObserverLoadBalancer(faasName, kube));

        handler.handleTasks(faasName, redis, kube);
        kube.deleteDeployment();
    }

    private static class EnvConfiguration {
        public final static String faasName = System.getenv("FAAS_NAME") == null ?
                "optimal-instances" : System.getenv("FAAS_NAME");

        public final static String loadBalancer = System.getenv("LOAD_BALANCER") == null ?
                "as" : System.getenv("LOAD_BALANCER");
    }
}