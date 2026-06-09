package com.exosurvival.dto.response;

import com.exosurvival.util.DifficultyProfile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlanetResponse {
    private Long id;
    private String name;
    private Double temperatureCelsius;
    private Double gravityMs2;
    private Double atmospherePressureAtm;
    private Double oxygenPercentage;
    private Double waterAvailability;
    private Double solarRadiationIndex;
    private LocalDateTime createdAt;
    private DifficultyProfile difficultyProfile;
}
