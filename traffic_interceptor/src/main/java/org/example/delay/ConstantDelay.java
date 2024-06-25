package org.example.delay;

import java.util.Random;

public class ConstantDelay implements Delay{

    private final int baseValue;
    private final double noisePercent;
    private final Random random;

    public ConstantDelay(int baseValue, double noisePercent) {
        this.baseValue = baseValue;
        this.noisePercent = noisePercent;
        random = new Random();
    }

    @Override
    public int getNextDelay(double requestPercent) {
        double noise = noisePercent * baseValue;

        return (int) (baseValue + noise * (1 - 2 * random.nextDouble()));
    }
}
