package com.exosurvival.service;

import com.exosurvival.dto.request.GameSessionRequest;
import com.exosurvival.dto.response.GameSessionResponse;
import com.exosurvival.entity.GameSession;
import com.exosurvival.entity.Planet;
import com.exosurvival.entity.User;
import com.exosurvival.exception.ForbiddenException;
import com.exosurvival.exception.ResourceNotFoundException;
import com.exosurvival.repository.GameSessionRepository;
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
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final PlanetRepository planetRepository;
    private final UserRepository userRepository;

    @Transactional
    public GameSessionResponse save(GameSessionRequest request, String email) {
        User user = findUserByEmail(email);

        Planet planet = planetRepository.findById(request.getPlanetId())
                .orElseThrow(() -> new ResourceNotFoundException("Planet not found with id: " + request.getPlanetId()));

        if (!planet.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("You do not own this planet");
        }

        DifficultyProfile profile = DifficultyCalculator.calculate(planet);

        GameSession session = GameSession.builder()
                .user(user)
                .planet(planet)
                .durationSeconds(request.getDurationSeconds())
                .causeOfDeath(request.getCauseOfDeath())
                .finalOxygen(request.getFinalOxygen())
                .finalFood(request.getFinalFood())
                .finalEnergy(request.getFinalEnergy())
                .finalMaterials(request.getFinalMaterials())
                .finalTemperature(request.getFinalTemperature())
                .actionsPerformed(request.getActionsPerformed())
                .difficultyScore(profile.getOverallScore())
                .build();

        gameSessionRepository.save(session);

        return toResponse(session, profile.getDifficultyLabel());
    }

    @Transactional(readOnly = true)
    public List<GameSessionResponse> listByUser(String email) {
        User user = findUserByEmail(email);
        return gameSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(gs -> toResponse(gs, labelFromScore(gs.getDifficultyScore())))
                .toList();
    }

    @Transactional(readOnly = true)
    public GameSessionResponse getBestSession(String email) {
        User user = findUserByEmail(email);
        GameSession best = gameSessionRepository.findBestSessionByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No sessions found for user"));
        return toResponse(best, labelFromScore(best.getDifficultyScore()));
    }

    private GameSessionResponse toResponse(GameSession gs, String difficultyLabel) {
        return GameSessionResponse.builder()
                .id(gs.getId())
                .planetId(gs.getPlanet().getId())
                .planetName(gs.getPlanet().getName())
                .durationSeconds(gs.getDurationSeconds())
                .causeOfDeath(gs.getCauseOfDeath())
                .finalOxygen(gs.getFinalOxygen())
                .finalFood(gs.getFinalFood())
                .finalEnergy(gs.getFinalEnergy())
                .finalMaterials(gs.getFinalMaterials())
                .finalTemperature(gs.getFinalTemperature())
                .actionsPerformed(gs.getActionsPerformed())
                .difficultyScore(gs.getDifficultyScore())
                .difficultyLabel(difficultyLabel)
                .createdAt(gs.getCreatedAt())
                .build();
    }

    private String labelFromScore(double score) {
        if (score < 0.4) return "HABITABLE";
        if (score < 0.8) return "CHALLENGING";
        if (score < 1.3) return "HOSTILE";
        if (score < 2.0) return "EXTREME";
        return "UNSURVIVABLE";
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
