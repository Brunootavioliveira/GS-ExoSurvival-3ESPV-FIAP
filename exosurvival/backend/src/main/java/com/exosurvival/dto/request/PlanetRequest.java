package com.exosurvival.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlanetRequest {

    @NotBlank(message = "Planet name is required")
    @Size(max = 100)
    private String name;

    @NotNull
    @DecimalMin(value = "-273.15", message = "Temperature cannot be below absolute zero")
    @DecimalMax(value = "500.0", message = "Temperature above 500°C is not supported")
    private Double temperatureCelsius;

    @NotNull
    @DecimalMin(value = "0.1", message = "Gravity must be at least 0.1 m/s²")
    @DecimalMax(value = "50.0", message = "Gravity above 50 m/s² is not supported")
    private Double gravityMs2;

    @NotNull
    @DecimalMin(value = "0.0", message = "Pressure cannot be negative")
    @DecimalMax(value = "10.0", message = "Pressure above 10 atm is not supported")
    private Double atmospherePressureAtm;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private Double oxygenPercentage;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double waterAvailability;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double solarRadiationIndex;
}
