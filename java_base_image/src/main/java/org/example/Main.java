package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (JedisPool pool = new JedisPool("localhost", 6379)) {
            try (Jedis jedis = pool.getResource()) {
                while (true) {
                    try {
                        List<String> message = jedis.blpop(60, "faas_input");
                        if (message == null) continue;

                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        command.add("D:\\Documents\\Facultate\\An 4\\licenta\\Coduri\\demo_start_process\\helloworld.jar");

                        ProcessBuilder builder = new ProcessBuilder(command);
                        Process p = builder.start();

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                            }
                        }

                        int exitCode = p.waitFor();
                        System.out.println("Process exited with code: " + exitCode);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Failed to get resource from Redis!");
            }
        }
        catch (Exception e) {
            System.out.println("Failed to connect to Redis!");
        }
    }
}