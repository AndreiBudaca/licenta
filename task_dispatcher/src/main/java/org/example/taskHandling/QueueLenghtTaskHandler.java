package org.example.taskHandling;

import io.kubernetes.client.openapi.ApiException;
import org.example.communication.KubernetesCommunication;
import org.example.communication.RedisCommunication;
import org.example.loadBalancing.AsyncQueueObserverLoadBalancer;
import org.example.loadBalancing.QueueLengthLoadBalancer;

import java.util.List;
import java.util.Objects;

public class QueueLenghtTaskHandler implements TaskHandler {
    private final QueueLengthLoadBalancer balancer;

    public QueueLenghtTaskHandler(QueueLengthLoadBalancer balancer) {
        this.balancer = balancer;
    }

    @Override
    public void handleTasks(String faasName, RedisCommunication redis, KubernetesCommunication kube) {
        while (true) {
            List<String> message = redis.getMessage();

            if (message != null) {
                String messageBody = message.get(1);
                if (Objects.equals(messageBody, "quit")) break;

                redis.sendMessage(messageBody);
            }

            long outputQueueLength = redis.getOutputQueueLength();
            System.out.println("Queue length: " + outputQueueLength);
            int balanceDifference = balancer.balance(outputQueueLength);

            try {
                int replicas;
                if (balanceDifference != 0) {
                    replicas = kube.updateDeploymentReplicas(balanceDifference);
                }
                else {
                    replicas = kube.getActiveReplicaCount();
                }

                if (message != null) {
                    String messageBody = message.get(1);
                    int taskId = Integer.parseInt(messageBody.split(";")[0]);

                    redis.sendLog(String.format("%d task_dispatcher %d %s replicas %d", taskId, System.currentTimeMillis(), faasName, replicas));
                    redis.sendLog(String.format("%d task_dispatcher %d %s messages %d", taskId, System.currentTimeMillis(), faasName, outputQueueLength));
                }
            } catch (ApiException e) {
                System.err.println("An api error has occured!");
                System.err.println(e.getMessage());
                System.err.println(e.getCode());
                System.err.println(e.getResponseBody());
            }
        }
    }
}
