package org.example.loadBalancing;

import io.kubernetes.client.openapi.ApiException;
import org.example.communication.KubernetesCommunication;
import org.example.communication.RedisCommunication;

public class AsyncQueueObserverLoadBalancer {

    private RedisCommunication redis;
    private KubernetesCommunication kube;

    private long receivedMessages;

    private double averageRequestsPerInstance;

    private long lastWaitingMessages;

    private double measurements = 0;

    private boolean running = true;

    public AsyncQueueObserverLoadBalancer(String faasName, KubernetesCommunication kube) {
        this.redis = new RedisCommunication(faasName);
        this.kube = kube;

        new Thread(this::balance).start();
    }

    public void incrementReceivedMessages() {
        ++receivedMessages;
    }

    public void stopBalancing() {
        running = false;
    }

    private void balance() {
        while (running) {
            try {
                int diff = getScaleDifference();
                //System.out.println("Difference: " + diff);
                kube.updateDeploymentReplicas(diff);
                Thread.sleep(EnvConfiguration.loadBalanceTime);
            } catch (ApiException e) {
                System.err.println("An api error has occured!");
                System.err.println(e.getMessage());
                System.err.println(e.getCode());
                System.err.println(e.getResponseBody());
            } catch (Exception e) {
                System.err.println("An exception has occured");
                System.err.println(e.getClass().getName());
                System.err.println(e.getMessage());
            }
        }
    }

    private int getScaleDifference() throws ApiException {
        long messagesSinceLastBalance = receivedMessages;
        long waitingMessages = redis.getOutputQueueLength();
        receivedMessages = 0;

        int currentReplica = kube.getActiveReplicaCount();
        long processedMessages = messagesSinceLastBalance + lastWaitingMessages - waitingMessages;
        double currentAverageRequestsPerInstance = (double) processedMessages / (double) currentReplica;

        // Recompute averageRequestsPerInstance for every measurement using mean value
        if (measurements == 0 || averageRequestsPerInstance == 0) {
            averageRequestsPerInstance = currentAverageRequestsPerInstance;
        } else if (currentAverageRequestsPerInstance != 0) {
            averageRequestsPerInstance = measurements / (measurements + 1) * averageRequestsPerInstance +
                    currentAverageRequestsPerInstance / (measurements + 1);
        }

        // Take a penalty when the system is running too rich
        if (waitingMessages == 0 && lastWaitingMessages == 0 && messagesSinceLastBalance != 0) {
            averageRequestsPerInstance *= 1.10;
        }

        int desiredReplica = averageRequestsPerInstance == 0 ?
                1 :
                (int) Math.round((messagesSinceLastBalance + waitingMessages) / (averageRequestsPerInstance));

        lastWaitingMessages = waitingMessages;
        if (measurements < 100 && averageRequestsPerInstance != 0)
            ++measurements;

        System.out.println("Waiting messages: " + waitingMessages);
        System.out.println("processedMessages: " + processedMessages);
        System.out.println("messagesSinceLastBalance: " + messagesSinceLastBalance);
        System.out.println("averageRequestsPerMilisecond: " + averageRequestsPerInstance);
        System.out.println("bestReplica: " + ((messagesSinceLastBalance + waitingMessages) / (averageRequestsPerInstance)));
        System.out.println("desiredReplica: " + desiredReplica);
        System.out.println();
        System.out.println();

        return getDiff(desiredReplica, currentReplica);
    }

    private int getDiff(int desiredReplica, int currentReplica) {
        int diff = desiredReplica - currentReplica;

        // Filter small noise
        if (Math.abs(diff) < .1 / currentReplica) diff = 0;

        return diff;
    }

    private static class EnvConfiguration {
        public final static int loadBalanceTime = System.getenv("LOAD_BALANCE_TIME") == null ?
                2000 : Integer.parseInt(System.getenv("LOAD_BALANCE_TIME"));
    }
}
