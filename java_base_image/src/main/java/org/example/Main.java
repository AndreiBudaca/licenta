package org.example;

import org.example.communication.Communication;
import org.example.communication.RedisCommunication;
import org.example.processing.DirectCodeInvocationProcessing;
import org.example.processing.Processing;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Objects;

public class Main {
    public static void main(String[] args)
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            IllegalAccessException, InstantiationException, IOException {


        Communication comm = new RedisCommunication();
        Processing processing = new DirectCodeInvocationProcessing();


        while (true) {
            try {
                String message = comm.getMessage();
                if (Objects.equals(message, "quit")) break;

                long initialTime = System.currentTimeMillis();

                String[] messageBits = message.split(";", 3);

                String faasName = messageBits[0];
                int taskId = Integer.parseInt(messageBits[1]);
                String result = processing.process(messageBits[2]);

                long endTime = System.currentTimeMillis();

                comm.sendMessage(String.format("%d;%s;%s", taskId, faasName, result));
                comm.sendLog(String.format("%d %s result %s", taskId, faasName, result));
                comm.sendLog(String.format("%d %s processing_time %d", taskId, faasName, (endTime - initialTime)));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
