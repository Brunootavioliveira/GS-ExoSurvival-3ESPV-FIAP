package com.exosurvival.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "planets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Planet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "temperature_celsius", nullable = false)
    private Double temperatureCelsius;

    @Column(name = "gravity_ms2", nullable = false)
    private Double gravityMs2;

    @Column(name = "atmosphere_pressure_atm", nullable = false)
    private Double atmospherePressureAtm;

    @Column(name = "oxygen_percentage", nullable = false)
    private Double oxygenPercentage;

    @Column(name = "water_availability", nullable = false)
    private Double waterAvailability;

    @Column(name = "solar_radiation_index", nullable = false)
    private Double solarRadiationIndex;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
