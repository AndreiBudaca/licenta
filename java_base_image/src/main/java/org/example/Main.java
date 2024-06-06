package org.example;

import org.example.communication.Communication;
import org.example.communication.RedisCommunication;
import org.example.processing.DirectCodeInvocationProcessing;
import org.example.processing.Processing;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

public class Main {
    public static void main(String[] args)
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            IllegalAccessException, InstantiationException, IOException {

        long initialTime = System.currentTimeMillis();

        Communication comm = new RedisCommunication();
        Processing processing = new DirectCodeInvocationProcessing();

        long endTime = System.currentTimeMillis();

        try (FileWriter logFile = new FileWriter("log_consumer.txt")) {
            logFile.append(1 + " " + (endTime - initialTime));
        }

        return;

//        while (true) {
//            try {
//                String message = comm.getMessage();
//                String result = processing.process(message);
//                comm.sendMessage(result);
//            } catch (Exception e) {
//                System.err.println(e.getMessage());
//            }
//        }
    }
}
