package org.example;

import io.kubernetes.client.openapi.ApiException;
import org.example.communication.KubernetesCommunication;

public class Main {
    public static void main(String[] args) {
        try {
            KubernetesCommunication c = new KubernetesCommunication("tunnel");

            Thread.sleep(5000);

            c.UpdateDeploymentReplicas(3);

            Thread.sleep(5000);

            c.deleteDeployment();
        }
        catch (ApiException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
            System.out.println(e.getResponseBody());
        }
        catch (Exception e) {
            System.out.println("An exception has occured");
            System.out.println(e.getMessage());
        }
    }
}