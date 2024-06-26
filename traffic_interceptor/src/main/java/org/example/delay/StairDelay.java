package org.example.delay;

import java.util.Random;

public class StairDelay extends Delay {
    private final int[] baseValues;

    public StairDelay(int[] baseValues, double noise) {
        super(noise);
        this.baseValues = baseValues;
    }

    @Override
    protected double getBaseValue(double requestPercent) {
        int index = Math.max((int)(requestPercent * baseValues.length), baseValues.length - 1);
        return baseValues[index];
    }
}
