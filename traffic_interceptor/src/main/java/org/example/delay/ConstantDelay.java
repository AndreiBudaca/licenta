package org.example.delay;

import java.util.Random;

public class ConstantDelay extends Delay {

    private final int baseValue;

    public ConstantDelay(int baseValue, double noise) {
        super(noise);
        this.baseValue = baseValue;
    }

    @Override
    protected double getBaseValue(double requestPercent) {
        return baseValue;
    }
}
