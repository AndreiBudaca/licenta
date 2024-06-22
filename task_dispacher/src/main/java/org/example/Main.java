package org.example;

import io.kubernetes.client.openapi.ApiException;
import org.example.communication.KubernetesCommunication;
import org.example.communication.RedisCommunication;
import org.example.loadBalancing.AsyncQueueObserverLoadBalancer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException, ApiException {
        String faasName = generateFaasName();

        RedisCommunication redis = new RedisCommunication(faasName);
        KubernetesCommunication kube = new KubernetesCommunication(faasName);
        //QueueLengthLoadBalancer balancer = new QueueLengthLoadBalancer();
        AsyncQueueObserverLoadBalancer balancer = new AsyncQueueObserverLoadBalancer(faasName, kube);

        FileWriter logFile = new FileWriter("log_task_dispacher.txt");
        int replicas = 1;
        try {
            while(true) {
                List<String> message = redis.getMessage();

                if (message != null) {
                    String messageBody = message.get(1);
                    if (Objects.equals(messageBody, "quit")) break;

                    redis.sendMessage(messageBody);
                    balancer.incrementReceivedMessages();
                    replicas = kube.getActiveReplicaCount();
                    logData(logFile, message.get(1), replicas);
                }

//                 long outputQueueLength = redis.getOutputQueueLength();
//                 System.out.println("Queue length: " + outputQueueLength);
//                int balanceDifference = balancer.balance(outputQueueLength);
//                if (balanceDifference != 0) {
//                    replicas = kube.updateDeploymentReplicas(balanceDifference);
//                }
//
//                if (message != null) {
//                    logData(logFile, message.get(1), replicas);
//                }
            }
        }
        catch (ApiException e) {
            System.err.println("An api error has occured!");
            System.err.println(e.getMessage());
            System.err.println(e.getCode());
            System.err.println(e.getResponseBody());
        }
        catch (Exception e) {
            System.err.println("An exception has occured");
            System.err.println(e.getClass().getName());
            System.err.println(e.getMessage());
        }
        finally {
            kube.deleteDeployment();
            logFile.close();
            balancer.stopBalancing();
        }
    }

    private static String generateFaasName() {
        StringBuilder faasNameBuilder = new StringBuilder("faas-");
        Random r = new Random();
        for (int i = 0; i < 10; ++i) {
            faasNameBuilder.append((char) ('a' + r.nextInt(26)));
        }

        return faasNameBuilder.toString();
    }

    private static void logData(FileWriter file, String message, int replicaCount) throws IOException {
        int taskId = Integer.parseInt(message.split("\\.")[0]);
        file.write(taskId + " " + replicaCount + '\n');
    }
}