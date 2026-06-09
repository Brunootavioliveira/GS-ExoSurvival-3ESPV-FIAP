package com.exosurvival.util;


public final class ExoplanetBaseline {

    private ExoplanetBaseline() {}

    public static final double MEAN_TEMPERATURE_CELSIUS = 15.0;
    public static final double MEAN_GRAVITY_MS2 = 9.8;
    public static final double MEAN_ATMOSPHERE_PRESSURE_ATM = 1.0;
    public static final double MEAN_OXYGEN_PERCENTAGE = 21.0;
    public static final double MEAN_WATER_AVAILABILITY = 0.7;
    public static final double MEAN_SOLAR_RADIATION_INDEX = 1.0;

    public static final double STD_TEMPERATURE = 20.0;
    public static final double STD_GRAVITY = 4.0;
    public static final double STD_PRESSURE = 0.5;
    public static final double STD_OXYGEN = 8.0;
    public static final double STD_WATER = 0.3;
    public static final double STD_RADIATION = 0.4;

    public static double deviation(double value, double mean, double std) {
        if (std == 0) return 0;
        double z = (value - mean) / std;
        return Math.max(-3.0, Math.min(3.0, z));
    }
}
