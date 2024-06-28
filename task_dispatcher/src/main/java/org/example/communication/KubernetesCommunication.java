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

public class KubernetesCommunication {
    private final AppsV1Api api;
    private V1Deployment faasDeployment;

    public KubernetesCommunication(String faasName) throws IOException, ApiException {
        ApiClient client = System.getenv("REDIS_PORT") != null ?
                ClientBuilder.cluster().build() :
                ClientBuilder.defaultClient();

        Configuration.setDefaultApiClient(client);
        api = new AppsV1Api();

        try {
            faasDeployment = createDeployment(faasName);
        }
        catch (ApiException e) {
            faasDeployment = new V1Deployment();
            System.out.println(e.getResponseBody());

        }

        System.out.println("Min faas replica: " + EnvConfiguration.minFaasReplica);
        System.out.println("Max faas replica: " + EnvConfiguration.maxFaasReplica);
    }


    public void deleteDeployment() throws ApiException {
        api.deleteNamespacedDeployment(faasDeployment.getMetadata().getName(), EnvConfiguration.faasNamespace,
                null, null, null, null, null, null);

        System.out.println("Faas deployment deleted!");
    }

    public int updateDeploymentReplicas(int difference) throws ApiException {
        //if (difference < 2) return getFaasDeployment().getStatus().getAvailableReplicas();

        V1Deployment deployment = getFaasDeployment();
        if (deployment == null) return 0;

        int newReplica = deployment.getStatus().getAvailableReplicas() + difference;

        if (newReplica < EnvConfiguration.minFaasReplica) {
            if (deployment.getStatus().getAvailableReplicas() != EnvConfiguration.minFaasReplica) {
                newReplica = EnvConfiguration.minFaasReplica;
            } else {
                return EnvConfiguration.minFaasReplica;
            }
        }

        if (newReplica > EnvConfiguration.maxFaasReplica) {
            if (deployment.getStatus().getAvailableReplicas() != EnvConfiguration.maxFaasReplica) {
                newReplica = EnvConfiguration.maxFaasReplica;
            }
            else {
                return EnvConfiguration.maxFaasReplica;
            }
        }

        V1Patch body = new V1Patch("""
                [
                    {
                        "op": "replace",
                        "path": "/spec/replicas",
                        "value":""" + newReplica + """
                    }
                ]
                """);

        api.patchNamespacedDeployment(faasDeployment.getMetadata().getName(), EnvConfiguration.faasNamespace, body,
                null, null, null, null, null);

        deployment = getFaasDeployment();
        if (deployment == null) return 0;

        return deployment.getStatus().getAvailableReplicas();
    }

    public Integer getActiveReplicaCount() throws ApiException {
        V1Deployment deployment = getFaasDeployment();
        if (deployment == null) return 0;

        return deployment.getStatus().getAvailableReplicas();
    }

    private V1Deployment getFaasDeployment() throws ApiException {
        return api.listNamespacedDeployment(EnvConfiguration.faasNamespace,
                        null, null, null, null,
                        "app=" + faasDeployment.getMetadata().getLabels().get("app"),
                        null, null, null, null, null)
                .getItems().getFirst();
    }

    private V1Deployment createDeployment(String faasName) throws ApiException {
        V1Deployment d = new V1Deployment();
        {
            d.setApiVersion("apps/v1");
            d.setKind("Deployment");

            V1ObjectMeta dMeta = new V1ObjectMeta();
            {
                dMeta.setName(faasName);
                dMeta.setLabels(new HashMap<>() {{
                    put("app", faasName);
                }});
            }
            d.setMetadata(dMeta);

            V1DeploymentSpec dSpec = new V1DeploymentSpec();
            {
                dSpec.setReplicas(EnvConfiguration.minFaasReplica);

                V1LabelSelector dSpecSelector = new V1LabelSelector();
                {
                    dSpecSelector.setMatchLabels(new HashMap<>() {{
                        put("app", faasName);
                    }});
                }
                dSpec.setSelector(dSpecSelector);

                V1PodTemplateSpec dSpecTemplate = new V1PodTemplateSpec();
                {
                    V1ObjectMeta dSpecTemplateMeta = new V1ObjectMeta();
                    {
                        dSpecTemplateMeta.setLabels(new HashMap<>() {{
                            put("app", faasName);
                        }});
                    }
                    dSpecTemplate.setMetadata(dSpecTemplateMeta);

                    V1PodSpec dSpecTemplateSpec = new V1PodSpec();
                    {
                        V1Container dSpecTemplateSpecContainer = new V1Container();
                        {
                            dSpecTemplateSpecContainer.setName(faasName);
                            dSpecTemplateSpecContainer.setImage(EnvConfiguration.faasImage);

                            V1EnvVar redisInput = new V1EnvVar();
                            {
                                redisInput.setName("REDIS_INPUT");
                                redisInput.setValue(faasName);
                            }

                            V1EnvVar redisOutput = new V1EnvVar();
                            {
                                redisOutput.setName("REDIS_OUTPUT");
                                redisOutput.setValue(EnvConfiguration.faasRedisOutput);
                            }

                            V1EnvVar redisLog = new V1EnvVar();
                            {
                                redisLog.setName("REDIS_LOG");
                                redisLog.setValue(EnvConfiguration.faasRedisLog);
                            }

                            dSpecTemplateSpecContainer.setEnv(new ArrayList<>() {{
                                add(redisInput);
                                add(redisOutput);
                                add(redisLog);
                            }});
                        }
                        dSpecTemplateSpec.setContainers(new ArrayList<>() {{
                            add(dSpecTemplateSpecContainer);
                        }});
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

    private static class EnvConfiguration {
        public final static String faasNamespace = System.getenv("FAAS_NAMESPACE") == null ?
                "default" : System.getenv("FAAS_NAMESPACE");

        public final static String faasImage = System.getenv("FAAS_IMAGE") == null ?
                "andreibudaca/java_echo:1.0" : System.getenv("FAAS_IMAGE");

        public final static String faasRedisOutput = System.getenv("FAAS_REDIS_OUTPUT") == null ?
                "faas_output" : System.getenv("FAAS_REDIS_OUTPUT");

        public final static String faasRedisLog = System.getenv("FAAS_REDIS_LOG") == null ?
                "logs" : System.getenv("FAAS_REDIS_LOG");

        public final static int minFaasReplica = System.getenv("MIN_FASS_REPLICA") == null ?
                1 : Integer.parseInt(System.getenv("MIN_FASS_REPLICA"));

        public final static int maxFaasReplica = System.getenv("MAX_FASS_REPLICA") == null ?
                50 : Integer.parseInt(System.getenv("MAX_FASS_REPLICA"));
    }
}
