package org.example.communication;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.Authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class KubernetesCommunication {

    private final AppsV1Api api;

    public KubernetesCommunication() throws IOException, ApiException {

        ApiClient client = System.getenv("REDIS_PORT") != null ?
                ClientBuilder.cluster().build() :
                ClientBuilder.defaultClient();

        Configuration.setDefaultApiClient(client);
        api = new AppsV1Api();

        System.out.println("Available authentications");
        for(String auth : client.getAuthentications().keySet()) {
            System.out.println(auth);
        }
    }

    public void CreateDeployment() throws InterruptedException, ApiException {
        V1Deployment d = new V1Deployment();
        {
            d.setApiVersion("apps/v1");
            d.setKind("Deployment");

            V1ObjectMeta dMeta = new V1ObjectMeta();
            {
                dMeta.setName("nginx-deployment");
                dMeta.setLabels(new HashMap<>() {{
                    put("app", "nginx");
                }});
            }
            d.setMetadata(dMeta);

            V1DeploymentSpec dSpec = new V1DeploymentSpec();
            {
                dSpec.setReplicas(1);

                V1LabelSelector dSpecSelector = new V1LabelSelector();
                {
                    dSpecSelector.setMatchLabels(new HashMap<>() {{
                        put("app", "nginx");
                    }});
                }
                dSpec.setSelector(dSpecSelector);

                V1PodTemplateSpec dSpecTemplate = new V1PodTemplateSpec();
                {
                    V1ObjectMeta dSpecTemplateMeta = new V1ObjectMeta();
                    {
                        dSpecTemplateMeta.setLabels(new HashMap<>() {{
                            put("app", "nginx");
                        }});
                    }
                    dSpecTemplate.setMetadata(dSpecTemplateMeta);

                    V1PodSpec dSpecTemplateSpec = new V1PodSpec();
                    {
                        V1Container dSpecTemplateSpecContainer = new V1Container();
                        {
                            dSpecTemplateSpecContainer.setName("nginx");
                            dSpecTemplateSpecContainer.setImage("nginx:1.14.2");

                            V1ContainerPort dSpecTemplateSpecContainerPort = new V1ContainerPort();
                            {
                                dSpecTemplateSpecContainerPort.setContainerPort(80);
                            }
                            dSpecTemplateSpecContainer.setPorts(new ArrayList<>() {{ add(dSpecTemplateSpecContainerPort); }});
                        }
                        dSpecTemplateSpec.setContainers(new ArrayList<>() {{ add(dSpecTemplateSpecContainer); }});
                    }
                    dSpecTemplate.setSpec(dSpecTemplateSpec);
                }
                dSpec.setTemplate(dSpecTemplate);
            }
            d.setSpec(dSpec);
        }

        api.createNamespacedDeployment("default", d, null, null, null, null);
        System.out.println("Deployment created!");

        V1DeploymentList deploymentList = api.listNamespacedDeployment("default", null,
                null, null, null, null, null, null, null, null, null);
        for (V1Deployment deployment : deploymentList.getItems()) {
            System.out.println(deployment.getMetadata().getName());
        }

        Thread.sleep(5000);

        api.deleteNamespacedDeployment("nginx-deployment", "default",
                null, null, null, null, null, null);
    }
}
