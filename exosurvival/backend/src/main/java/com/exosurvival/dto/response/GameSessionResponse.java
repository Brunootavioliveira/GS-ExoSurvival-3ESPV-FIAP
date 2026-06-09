package com.exosurvival.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GameSessionResponse {
    private Long id;
    private Long planetId;
    private String planetName;
    private Long durationSeconds;
    private String causeOfDeath;
    private Double finalOxygen;
    private Double finalFood;
    private Double finalEnergy;
    private Double finalMaterials;
    private Double finalTemperature;
    private Integer actionsPerformed;
    private Double difficultyScore;
    private String difficultyLabel;
    private LocalDateTime createdAt;
}
