package com.exosurvival.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    private Planet planet;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds;

    @Column(name = "cause_of_death", nullable = false, length = 50)
    private String causeOfDeath;

    @Column(name = "final_oxygen")
    private Double finalOxygen;

    @Column(name = "final_food")
    private Double finalFood;

    @Column(name = "final_energy")
    private Double finalEnergy;

    @Column(name = "final_materials")
    private Double finalMaterials;

    @Column(name = "final_temperature")
    private Double finalTemperature;

    @Column(name = "actions_performed", nullable = false)
    private Integer actionsPerformed;

    @Column(name = "difficulty_score", nullable = false)
    private Double difficultyScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
