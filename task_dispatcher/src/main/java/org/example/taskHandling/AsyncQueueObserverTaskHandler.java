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

                String messageToSend = String.format("%s;%s", faasName, messageBody);
                System.out.println(messageToSend);
                redis.sendMessage(messageToSend);
                balancer.incrementReceivedMessages();
                try {
                    int taskId = Integer.parseInt(messageBody.split(";")[0]);
                    int replicas = kube.getActiveReplicaCount();
                    long outputQueueLength = redis.getOutputQueueLength();

                    redis.sendLog(String.format("%d task_dispatcher %d %s replicas %d", taskId, System.currentTimeMillis(), faasName, replicas));
                    redis.sendLog(String.format("%d task_dispatcher %d %s messages %d", taskId, System.currentTimeMillis(), faasName, outputQueueLength));
                } catch (ApiException e) {
                    System.err.println("An api error has occurred!");
                    System.err.println(e.getMessage());
                    System.err.println(e.getCode());
                    System.err.println(e.getResponseBody());
                    break;
                }
            }
        }

        balancer.stopBalancing();
    }
}
