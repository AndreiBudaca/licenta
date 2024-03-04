package org.example;

import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String redisHost = System.getenv("REDIS_HOST");
        if (redisHost == null) redisHost = "localhost";
        String redisPort = System.getenv("REDIS_PORT");
        if (redisPort == null) redisPort = "6379";
        String jarPath = System.getenv("FAAS_PATH");
        if (jarPath == null) jarPath = "/faas.jar";
        String inputQueue = System.getenv("REDIS_INPUT");
        if (inputQueue == null) inputQueue = "faas_input";
        String outputQueue = System.getenv("REDIS_OUTPUT");
        if (outputQueue == null) outputQueue = "faas_output";

        System.out.println("Configuration:");
        System.out.println("redis host: " + redisHost);
        System.out.println("redis port: " + redisPort);
        System.out.println("jarPath: " + jarPath);
        System.out.println("inputQueue: " + inputQueue);
        System.out.println("outputQueue: " + outputQueue);

        try (Jedis jedis = new Jedis(redisHost, Integer.parseInt(redisPort), 10)) {
            List<String> command = new ArrayList<>(4);

            command.add("java");
            command.add("-jar");
            command.add(jarPath);
            command.add("");

            while (true) {
                try {
                    System.out.println("Waiting for messages...");
                    List<String> message = jedis.blpop(60, inputQueue);
                    if (message == null) continue;

                    command.set(3, message.get(1));
                    ProcessBuilder builder = new ProcessBuilder(command);
                    builder.redirectErrorStream(true);
                    Process p = builder.start();

                    StringBuilder messageBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            messageBuilder.append(line);
                            messageBuilder.append('\n');
                        }
                    }

                    int exitCode = p.waitFor();
                    System.out.println("Process exited with code: " + exitCode);
                    if (exitCode != 0) {
                        messageBuilder.insert(0, "An error occurred while running the program\n");
                    }

                    jedis.rpush(outputQueue, messageBuilder.toString());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    break;
                }
            }
        }
        catch (Exception e) {
            System.out.println("Failed to get resource from Redis!");
            System.out.println(e.getMessage());
        }
    }
}