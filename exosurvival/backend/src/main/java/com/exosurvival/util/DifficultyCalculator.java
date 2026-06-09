package com.exosurvival.util;

import com.exosurvival.entity.Planet;

import java.util.ArrayList;
import java.util.List;

import static com.exosurvival.util.ExoplanetBaseline.*;


public final class DifficultyCalculator {

    private DifficultyCalculator() {}

    public static DifficultyProfile calculate(Planet planet) {
        double tempDev   = deviation(planet.getTemperatureCelsius(), MEAN_TEMPERATURE_CELSIUS, STD_TEMPERATURE);
        double gravDev   = deviation(planet.getGravityMs2(), MEAN_GRAVITY_MS2, STD_GRAVITY);
        double presDev   = deviation(planet.getAtmospherePressureAtm(), MEAN_ATMOSPHERE_PRESSURE_ATM, STD_PRESSURE);
        double o2Dev     = deviation(planet.getOxygenPercentage(), MEAN_OXYGEN_PERCENTAGE, STD_OXYGEN);
        double waterDev  = deviation(planet.getWaterAvailability(), MEAN_WATER_AVAILABILITY, STD_WATER);
        double radDev    = deviation(planet.getSolarRadiationIndex(), MEAN_SOLAR_RADIATION_INDEX, STD_RADIATION);

        List<String> notes = new ArrayList<>();

        double oxygenDecay = 1.0
                + clampPositive(-o2Dev) * 0.4
                + clampPositive(-presDev) * 0.3;
        if (o2Dev < -1.0) notes.add("Thin atmosphere: oxygen reserves deplete faster.");
        if (presDev < -1.0) notes.add("Low pressure worsens respiratory strain.");

        double oxygenProduction = 1.0 - clampPositive(-o2Dev) * 0.25;

        double foodDecay = 1.0
                + Math.abs(tempDev) * 0.35
                + clampPositive(gravDev) * 0.2
                + clampPositive(-waterDev) * 0.3;
        if (Math.abs(tempDev) > 1.0) notes.add("Extreme temperature accelerates food spoilage.");
        if (waterDev < -0.5) notes.add("Low water availability reduces food production.");

        double foodProduction = 1.0
                - clampPositive(-waterDev) * 0.3
                - Math.abs(tempDev) * 0.1;
        foodProduction = Math.max(0.3, foodProduction);

        double energyDecay = 1.0
                + clampPositive(gravDev) * 0.35
                + clampPositive(-radDev) * 0.2;

        double energyProduction = 1.0 + clampPositive(radDev) * 0.4;
        if (radDev > 1.0) notes.add("High solar radiation: energy generation is boosted.");

        double materialsDecay = 1.0 + Math.abs(tempDev) * 0.2 + clampPositive(gravDev) * 0.15;

        double temperatureRise = 1.0 + clampPositive(tempDev) * 0.5 + clampPositive(radDev) * 0.3;
        if (tempDev > 1.5) notes.add("Extreme heat: temperature management is critical.");

        double overallScore = (Math.abs(tempDev) + Math.abs(gravDev) + Math.abs(presDev)
                + Math.abs(o2Dev) + Math.abs(waterDev) + Math.abs(radDev)) / 6.0;

        String label = difficultyLabel(overallScore);

        if (notes.isEmpty()) notes.add("Conditions are close to Earth baseline. Manageable survival.");

        return DifficultyProfile.builder()
                .overallScore(round(overallScore))
                .oxygenDecayMultiplier(round(oxygenDecay))
                .foodDecayMultiplier(round(foodDecay))
                .energyDecayMultiplier(round(energyDecay))
                .materialsDecayMultiplier(round(materialsDecay))
                .temperatureRiseMultiplier(round(temperatureRise))
                .oxygenProductionMultiplier(round(oxygenProduction))
                .energyProductionMultiplier(round(energyProduction))
                .foodProductionMultiplier(round(foodProduction))
                .difficultyLabel(label)
                .planetaryNotes(notes.toArray(new String[0]))
                .build();
    }

    private static double clampPositive(double value) {
        return Math.max(0.0, value);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String difficultyLabel(double score) {
        if (score < 0.4) return "HABITABLE";
        if (score < 0.8) return "CHALLENGING";
        if (score < 1.3) return "HOSTILE";
        if (score < 2.0) return "EXTREME";
        return "UNSURVIVABLE";
    }
}
