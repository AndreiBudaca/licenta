package org.example;

import org.example.communication.Communication;
import org.example.communication.RedisCommunication;
import org.example.processing.DirectCodeInvocationProcessing;
import org.example.processing.Processing;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

public class Main {
    public static void main(String[] args)
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
                IllegalAccessException, InstantiationException, MalformedURLException {

        for (String arg: args) {
            System.out.println(arg);
        }

        Communication comm = new RedisCommunication();
        Processing processing = new DirectCodeInvocationProcessing();

        while (true) {
            try {
                String message = comm.getMessage();
                String result = processing.process(message);
                comm.sendMessage(result);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
