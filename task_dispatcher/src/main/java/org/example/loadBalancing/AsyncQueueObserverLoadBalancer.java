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
        if (currentReplica == 0) return 0;
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
            averageRequestsPerInstance *= EnvConfiguration.penaltyPercentage;
        }

        int desiredReplica = averageRequestsPerInstance == 0 ?
                1 :
                (int) Math.round((messagesSinceLastBalance + waitingMessages) / (averageRequestsPerInstance));

        lastWaitingMessages = waitingMessages;
        if (measurements < EnvConfiguration.measurementsThreshold && averageRequestsPerInstance != 0)
            ++measurements;

        return getDiff(desiredReplica, currentReplica);
    }

    private int getDiff(int desiredReplica, int currentReplica) {
        int diff = desiredReplica - currentReplica;

        // Filter small noise
        if (Math.abs(diff) < EnvConfiguration.scalePercentage / currentReplica) diff = 0;

        return diff;
    }

    private static class EnvConfiguration {
        public final static int loadBalanceTime = System.getenv("LOAD_BALANCE_TIME") == null ?
                2000 : Integer.parseInt(System.getenv("LOAD_BALANCE_TIME"));

        public final static int measurementsThreshold = System.getenv("MEASUREMENTS_THRESHOLD") == null ?
                20 : Integer.parseInt(System.getenv("MEASUREMENTS_THRESHOLD"));

        public final static double scalePercentage = System.getenv("SCALE_PERCENTAGE") == null ?
                .1 : Double.parseDouble(System.getenv("SCALE_PERCENTAGE"));

        public final static double penaltyPercentage = System.getenv("PENALTY_PERCENTAGE") == null ?
                1.1 : Double.parseDouble(System.getenv("PENALTY_PERCENTAGE"));
    }
}
