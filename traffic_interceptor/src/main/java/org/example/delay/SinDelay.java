package org.example.delay;

import java.util.Random;

public class SinDelay implements Delay {
    private final int median;
    private final int scale;
    private final double noisePercent;
    private final Random random;

    public SinDelay(int minValue, int maxValue, double noisePercent) {
        this.median = minValue + (maxValue - minValue) / 2;
        this.scale = maxValue - median;
        this.noisePercent = noisePercent;

        random = new Random();
    }

    @Override
    public int getNextDelay(double requestPercent) {
        double sinArg = requestPercent * 2 * Math.PI;

        double noise = noisePercent * median;
        return (int) (scale * Math.sin(sinArg) + median + noise * (1 - 2 * random.nextDouble()));
    }
}
