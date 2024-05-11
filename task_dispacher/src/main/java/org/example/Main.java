package org.example;

import io.kubernetes.client.openapi.ApiException;
import org.example.communication.KubernetesCommunication;
import org.example.communication.RedisCommunication;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        //RedisCommunication redis = RedisCommunication.getInstance();
        try {
            KubernetesCommunication c = new KubernetesCommunication();
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