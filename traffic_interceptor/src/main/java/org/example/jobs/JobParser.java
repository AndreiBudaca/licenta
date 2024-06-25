package org.example.jobs;

import org.example.configuration.ModuleConfiguration;
import org.example.delay.ConstantDelay;
import org.example.delay.SinDelay;
import org.example.delay.StairDelay;

import java.util.Arrays;

public class JobParser {
    public static Job parse(String jobString) {
        String[] jobParts = jobString.split(String.valueOf(ModuleConfiguration.separator));

        int requests = Integer.parseInt(jobParts[0]);
        int delayType = Integer.parseInt(jobParts[1]);

        double noisePercent;

        switch (delayType) {
            case 0:
                int baseValue = Integer.parseInt(jobParts[2]);
                noisePercent = Double.parseDouble(jobParts[3]);
                return new Job(requests, new ConstantDelay(baseValue, noisePercent));
            case 1:
                String[] stringBaseValues = jobParts[2].split(" ");
                int[] baseValues = new int[stringBaseValues.length];
                for (int i = 0; i < stringBaseValues.length; i++) {
                    baseValues[i] = Integer.parseInt(stringBaseValues[i]);
                }

                noisePercent = Double.parseDouble(jobParts[3]);
                return new Job(requests, new StairDelay(baseValues, noisePercent));
            case 2:
                int minValue = Integer.parseInt(jobParts[2]);
                int maxValue = Integer.parseInt(jobParts[3]);
                noisePercent = Double.parseDouble(jobParts[4]);
                return new Job(requests, new SinDelay(minValue, maxValue, noisePercent));
            default:
                return null;
        }
    }
}
