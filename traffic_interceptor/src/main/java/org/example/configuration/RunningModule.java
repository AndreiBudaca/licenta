package org.example.configuration;

public class RunningModule {
    private final String name;

    private final String queueName;

    private final int[] dataLayers;

    public RunningModule(String name, String queueName, int[] dataLayers) {
        this.name = name;
        this.queueName = queueName;
        this.dataLayers = dataLayers;
    }

    public String getName() {
        return name;
    }

    public String getQueueName() {
        return queueName;
    }

    public int[] getDataLayers() {
        return dataLayers;
    }

}
