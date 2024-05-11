package org.example.communication;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class KubernetesCommunication {
    private final AppsV1Api api;
    private V1Deployment faasDeployment;

    public KubernetesCommunication(String redisQueueName) throws IOException, ApiException {
        ApiClient client = System.getenv("REDIS_PORT") != null ?
                ClientBuilder.cluster().build() :
                ClientBuilder.defaultClient();

        Configuration.setDefaultApiClient(client);
        api = new AppsV1Api();
        faasDeployment = createDeployment(redisQueueName);
    }

    public V1Deployment createDeployment(String redisQueueName) throws ApiException {
        StringBuilder deploymentName = new StringBuilder("faas-");
        Random r = new Random();
        for (int i = 0; i < 10; ++i) {
            deploymentName.append((char) ('a' + r.nextInt(26)));
        }

        V1Deployment d = new V1Deployment();
        {
            d.setApiVersion("apps/v1");
            d.setKind("Deployment");

            V1ObjectMeta dMeta = new V1ObjectMeta();
            {
                dMeta.setName(deploymentName.toString());
                dMeta.setLabels(new HashMap<>() {{
                    put("app", deploymentName.toString());
                }});
            }
            d.setMetadata(dMeta);

            V1DeploymentSpec dSpec = new V1DeploymentSpec();
            {
                dSpec.setReplicas(1);

                V1LabelSelector dSpecSelector = new V1LabelSelector();
                {
                    dSpecSelector.setMatchLabels(new HashMap<>() {{
                        put("app", deploymentName.toString());
                    }});
                }
                dSpec.setSelector(dSpecSelector);

                V1PodTemplateSpec dSpecTemplate = new V1PodTemplateSpec();
                {
                    V1ObjectMeta dSpecTemplateMeta = new V1ObjectMeta();
                    {
                        dSpecTemplateMeta.setLabels(new HashMap<>() {{
                            put("app", deploymentName.toString());
                        }});
                    }
                    dSpecTemplate.setMetadata(dSpecTemplateMeta);

                    V1PodSpec dSpecTemplateSpec = new V1PodSpec();
                    {
                        V1Container dSpecTemplateSpecContainer = new V1Container();
                        {
                            dSpecTemplateSpecContainer.setName(deploymentName.toString());
                            dSpecTemplateSpecContainer.setImage(EnvConfiguration.faasImage);

                            V1ContainerPort dSpecTemplateSpecContainerPort = new V1ContainerPort();
                            {
                                dSpecTemplateSpecContainerPort.setContainerPort(80);
                            }
                            dSpecTemplateSpecContainer.setPorts(new ArrayList<>() {{ add(dSpecTemplateSpecContainerPort); }});

                            V1EnvVar redisInput = new V1EnvVar();
                            {
                                redisInput.setName("REDIS_INPUT");
                                redisInput.setValue(redisQueueName);
                            }

                            V1EnvVar redisOutput = new V1EnvVar();
                            {
                                redisOutput.setName("REDIS_OUTPUT");
                                redisOutput.setValue(EnvConfiguration.faasRedisOutput);
                            }

                            dSpecTemplateSpecContainer.setEnv(new ArrayList<>() {{ add(redisInput); add(redisOutput); }});
                        }
                        dSpecTemplateSpec.setContainers(new ArrayList<>() {{ add(dSpecTemplateSpecContainer); }});
                    }
                    dSpecTemplate.setSpec(dSpecTemplateSpec);
                }
                dSpec.setTemplate(dSpecTemplate);
            }
            d.setSpec(dSpec);
        }

        api.createNamespacedDeployment(EnvConfiguration.faasNamespace, d, null, null, null, null);
        System.out.println("Faas deployment created: " + d.getMetadata().getName());

        return d;
    }

    public void deleteDeployment() throws ApiException {
        api.deleteNamespacedDeployment(faasDeployment.getMetadata().getName(), EnvConfiguration.faasNamespace,
                null, null, null, null, null, null);

        System.out.println("Faas deployment deleted!");
    }

    public void UpdateDeploymentReplicas(int desired) throws ApiException {
        System.out.println("Replica count after: " + faasDeployment.getSpec().getReplicas());
        V1Patch body = new V1Patch("""
                [
                    {
                        "op": "replace",
                        "path": "/spec/replicas",
                        "value":""" + desired + """
                    }
                ]
                """);

        api.patchNamespacedDeployment(faasDeployment.getMetadata().getName(), EnvConfiguration.faasNamespace, body,
                null, null, null, null, null);
        faasDeployment = api.listNamespacedDeployment(EnvConfiguration.faasNamespace,
                null, null, null, null,
                "app=" + faasDeployment.getMetadata().getLabels().get("app"),
                null, null, null, null, null)
            .getItems().get(0);

        System.out.println("Replica count after: " + faasDeployment.getSpec().getReplicas());
    }

    private static class EnvConfiguration {
        public final static String faasNamespace = System.getenv("FAAS_NAMESPACE") == null ?
                "default" : System.getenv("FAAS_NAMESPACE");

        public final static String faasImage = System.getenv("FAAS_IMAGE") == null ?
                "andreibudaca/echo_faas:latest" : System.getenv("FAAS_IMAGE");

        public final static String faasRedisOutput = System.getenv("FAAS_REDIS_OUTPUT") == null ?
                "faas_output" : System.getenv("FAAS_REDIS_OUTPUT");
    }
}
