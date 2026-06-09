package com.exosurvival.service;

import com.exosurvival.dto.request.PlanetRequest;
import com.exosurvival.dto.response.PlanetResponse;
import com.exosurvival.entity.Planet;
import com.exosurvival.entity.User;
import com.exosurvival.exception.ForbiddenException;
import com.exosurvival.exception.ResourceNotFoundException;
import com.exosurvival.repository.PlanetRepository;
import com.exosurvival.repository.UserRepository;
import com.exosurvival.util.DifficultyCalculator;
import com.exosurvival.util.DifficultyProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanetService {

    private final PlanetRepository planetRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlanetResponse create(PlanetRequest request, String email) {
        User user = findUserByEmail(email);

        Planet planet = Planet.builder()
                .name(request.getName())
                .temperatureCelsius(request.getTemperatureCelsius())
                .gravityMs2(request.getGravityMs2())
                .atmospherePressureAtm(request.getAtmospherePressureAtm())
                .oxygenPercentage(request.getOxygenPercentage())
                .waterAvailability(request.getWaterAvailability())
                .solarRadiationIndex(request.getSolarRadiationIndex())
                .user(user)
                .build();

        planetRepository.save(planet);

        return toResponse(planet);
    }

    @Transactional(readOnly = true)
    public PlanetResponse getById(Long planetId, String email) {
        Planet planet = findPlanetById(planetId);
        assertOwnership(planet, email);
        return toResponse(planet);
    }

    @Transactional(readOnly = true)
    public List<PlanetResponse> listByUser(String email) {
        User user = findUserByEmail(email);
        return planetRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long planetId, String email) {
        Planet planet = findPlanetById(planetId);
        assertOwnership(planet, email);
        planetRepository.delete(planet);
    }

    private PlanetResponse toResponse(Planet planet) {
        DifficultyProfile profile = DifficultyCalculator.calculate(planet);
        return PlanetResponse.builder()
                .id(planet.getId())
                .name(planet.getName())
                .temperatureCelsius(planet.getTemperatureCelsius())
                .gravityMs2(planet.getGravityMs2())
                .atmospherePressureAtm(planet.getAtmospherePressureAtm())
                .oxygenPercentage(planet.getOxygenPercentage())
                .waterAvailability(planet.getWaterAvailability())
                .solarRadiationIndex(planet.getSolarRadiationIndex())
                .createdAt(planet.getCreatedAt())
                .difficultyProfile(profile)
                .build();
    }

    private Planet findPlanetById(Long id) {
        return planetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planet not found with id: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void assertOwnership(Planet planet, String email) {
        if (!planet.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("You do not have access to this planet");
        }
    }
}
