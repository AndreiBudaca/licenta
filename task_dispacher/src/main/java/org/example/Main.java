package org.example;

import org.example.communication.KubernetesCommunication;
import org.example.communication.RedisCommunication;

public class Main {
    public static void main(String[] args) {
        //RedisCommunication redis = RedisCommunication.getInstance();
        KubernetesCommunication c = new KubernetesCommunication();
    }
}