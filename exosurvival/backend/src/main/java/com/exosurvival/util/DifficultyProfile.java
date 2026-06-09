package com.exosurvival.util;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DifficultyProfile {

    private final double overallScore;

    private final double oxygenDecayMultiplier;
    private final double foodDecayMultiplier;
    private final double energyDecayMultiplier;
    private final double materialsDecayMultiplier;
    private final double temperatureRiseMultiplier;

    private final double oxygenProductionMultiplier;
    private final double energyProductionMultiplier;
    private final double foodProductionMultiplier;

    private final String difficultyLabel;

    private final String[] planetaryNotes;
}
