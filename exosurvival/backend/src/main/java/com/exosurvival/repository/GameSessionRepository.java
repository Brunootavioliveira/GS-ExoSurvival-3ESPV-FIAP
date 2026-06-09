package com.exosurvival.repository;

import com.exosurvival.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT gs FROM GameSession gs WHERE gs.user.id = :userId ORDER BY gs.durationSeconds DESC LIMIT 1")
    Optional<GameSession> findBestSessionByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
