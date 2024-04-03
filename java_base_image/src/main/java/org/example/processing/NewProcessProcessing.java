package org.example.processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NewProcessProcessing implements Processing {
    private String jarPath = "/faas.jar";
    public NewProcessProcessing() {
        if (System.getenv("FAAS_PATH") != null)
            jarPath = System.getenv("FAAS_PATH");
    }

    @Override
    public String process(String data) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(4);

        command.add("java");
        command.add("-jar");
        command.add(jarPath);
        command.add("");

        command.set(3, data);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process p = builder.start();

        StringBuilder messageBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            messageBuilder.append(line);
            messageBuilder.append('\n');
        }

        int exitCode = p.waitFor();
        if (exitCode != 0) {
            messageBuilder.insert(0, "An error occurred while running the program\n");
        }

        return messageBuilder.toString();
    }
}
