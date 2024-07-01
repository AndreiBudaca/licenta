package org.example.configuration;

public class RunningModuleParser {
    public static RunningModule parse(String moduleString) {
        String[] moduleParts = moduleString.split(String.valueOf(ModuleConfiguration.separator));

        String name = moduleParts[0];
        String queue = moduleParts[1];

        String[] layersString = moduleParts[2].split(" ");
        int[] layers = new int[layersString.length];
        for (int i = 0; i < layersString.length; i++) {
            layers[i] = Integer.parseInt(layersString[i]);
        }

        return new RunningModule(name, queue, layers);
    }
}
