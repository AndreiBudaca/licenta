package org.example.taskHandling;

import io.kubernetes.client.openapi.ApiException;
import org.example.communication.KubernetesCommunication;
import org.example.communication.RedisCommunication;
import org.example.loadBalancing.AsyncQueueObserverLoadBalancer;

import java.util.List;
import java.util.Objects;

public class AsyncQueueObserverTaskHandler implements TaskHandler{
    private final AsyncQueueObserverLoadBalancer balancer;

    public AsyncQueueObserverTaskHandler(AsyncQueueObserverLoadBalancer balancer) {
        this.balancer = balancer;
    }

    @Override
    public void handleTasks(String faasName, RedisCommunication redis, KubernetesCommunication kube) {
        while (true) {
            List<String> message = redis.getMessage();

            if (message != null) {
                String messageBody = message.get(1);
                if (Objects.equals(messageBody, "quit")) break;

                redis.sendMessage(String.format("%s;%s", faasName, messageBody));
                balancer.incrementReceivedMessages();
                try {
                    int taskId = Integer.parseInt(messageBody.split(";")[0]);
                    int replicas = kube.getActiveReplicaCount();
                    long outputQueueLength = redis.getOutputQueueLength();

                    redis.sendLog(String.format("%d %s replicas %d", taskId, faasName, replicas));
                    redis.sendLog(String.format("%d %s messages %d", taskId, faasName, outputQueueLength));
                } catch (ApiException e) {
                    System.err.println("An api error has occured!");
                    System.err.println(e.getMessage());
                    System.err.println(e.getCode());
                    System.err.println(e.getResponseBody());
                }
            }
        }

        balancer.stopBalancing();
    }
}
