package org.example.delay;

import java.util.Random;

public class SinDelay extends Delay {
    private final int median;
    private final int scale;

    public SinDelay(int minValue, int maxValue, double noise) {
        super(noise);

        this.median = minValue + (maxValue - minValue) / 2;
        this.scale = maxValue - median;
    }

    @Override
    protected double getBaseValue(double requestPercent) {
        double sinArg = requestPercent * 2 * Math.PI;
        return scale * Math.sin(sinArg) + median;
    }
}
