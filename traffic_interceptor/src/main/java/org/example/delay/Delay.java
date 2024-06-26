package org.example.delay;

import java.util.Random;

public abstract class Delay {
    private final Random random = new Random();
    private final double noise;

    public Delay(double noise) {
        this.noise = noise;
    }

    public int getNextDelay(double requestPercent) {
        return addNoise(getBaseValue(requestPercent));
    }

    protected abstract double getBaseValue(double requestPercent);

    private int addNoise(double value) {
        return (int)(value * (1 + noise * (1 - 2 * random.nextDouble())));
    }
}
