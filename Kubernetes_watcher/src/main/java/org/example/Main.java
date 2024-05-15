package org.example;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.util.ClientBuilder;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static boolean exit = false;
    public static String faasName = "echo-faas";

    public static void main(String[] args) throws IOException, ApiException {
        ApiClient client = ClientBuilder.defaultClient();

        Configuration.setDefaultApiClient(client);
        AppsV1Api api = new AppsV1Api();

        new Thread(() -> {
            try {
                System.in.read();
                exit = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        try (FileWriter logFile = new FileWriter("log_kube_watcher.txt")) {
            long startTime = System.currentTimeMillis();
            while (!exit) {
                long currentTime = System.currentTimeMillis();
                int replicaCount = api.listNamespacedDeployment("default",
                                null, null, null, null,
                                "app=" + faasName,
                                null, null, null, null, null)
                        .getItems().get(0).getStatus().getAvailableReplicas();

                logFile.append((currentTime - startTime) + " " + replicaCount + '\n');
            }
        }
    }
}