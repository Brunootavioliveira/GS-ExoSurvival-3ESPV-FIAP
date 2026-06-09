package com.exosurvival.repository;

import com.exosurvival.entity.Planet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanetRepository extends JpaRepository<Planet, Long> {
    List<Planet> findByUserIdOrderByCreatedAtDesc(Long userId);
}
