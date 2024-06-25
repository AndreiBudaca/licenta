package org.example.delay;

import java.util.Random;

public class StairDelay implements Delay {
    private final int[] baseValues;
    private final double noisePercent;
    private final Random random;

    public StairDelay(int[] baseValues, double noisePercent) {
        this.baseValues = baseValues;
        this.noisePercent = noisePercent;
        random = new Random();
    }

    @Override
    public int getNextDelay(double requestPercent) {
        int bdIndex = (int)(requestPercent * baseValues.length);
        double noise = noisePercent * baseValues[bdIndex];

        return (int) (baseValues[bdIndex] + noise * (1 - 2 * random.nextDouble()));
    }
}
