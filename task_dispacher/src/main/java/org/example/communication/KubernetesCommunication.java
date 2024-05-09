package org.example.communication;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class KubernetesCommunication {

    public KubernetesCommunication() {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            Deployment deployment = new DeploymentBuilder()
                    .withNewMetadata()
                    .withName("nginx")
                    .endMetadata()
                    .withNewSpec()
                    .withReplicas(1)
                    .withNewTemplate()
                    .withNewMetadata()
                    .addToLabels("app", "nginx")
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("nginx")
                    .withImage("nginx")
                    .addNewPort()
                    .withContainerPort(80)
                    .endPort()
                    .endContainer()
                    .endSpec()
                    .endTemplate()
                    .withNewSelector()
                    .addToMatchLabels("app", "nginx")
                    .endSelector()
                    .endSpec()
                    .build();

            deployment = client.apps().deployments().resource(deployment).create();
            System.out.println("Created deployment: " + deployment);

            Thread.sleep(5000);

            System.out.println("Scaling up: " + deployment.getMetadata().getName());
            client.apps().deployments().withName("nginx").scale(2, true);
            System.out.println("Created replica sets: " + client.apps().replicaSets().list().getItems());

            Thread.sleep(5000);

            System.out.println("Deleting: " + deployment.getMetadata().getName());
            client.resource(deployment).delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
