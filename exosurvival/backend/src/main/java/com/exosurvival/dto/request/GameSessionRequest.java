package com.exosurvival.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GameSessionRequest {

    @NotNull
    private Long planetId;

    @NotNull
    @Min(1)
    private Long durationSeconds;

    @NotBlank
    private String causeOfDeath;

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private Double finalOxygen;

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private Double finalFood;

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private Double finalEnergy;

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private Double finalMaterials;

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private Double finalTemperature;

    @NotNull @Min(0)
    private Integer actionsPerformed;
}
