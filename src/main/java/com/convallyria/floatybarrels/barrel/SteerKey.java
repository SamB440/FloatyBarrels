package com.convallyria.floatybarrels.barrel;

import org.jetbrains.annotations.Nullable;

public enum SteerKey {
    W(SteerType.FORWARD, 0.98),
    A(SteerType.SIDE, 0.98),
    S(SteerType.FORWARD, -0.98),
    D(SteerType.SIDE, -0.98);

    private final SteerType steerType;
    private final double value;

    SteerKey(final SteerType steerType, final double value) {
        this.steerType = steerType;
        this.value = value;
    }

    public SteerType getSteerType() {
        return steerType;
    }

    public double getValue() {
        return value;
    }

    @Nullable
    public static SteerKey getSteerKey(SteerType steerType, double value) {
        for (SteerKey steerKey : values()) {
            if (steerKey.getSteerType() == steerType && steerKey.getValue() == value) {
                return steerKey;
            }
        }
        return null;
    }
}
