package dev.vivim.weatherviewer.repository;

import dev.vivim.weatherviewer.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    @Query("SELECT s FROM Session s JOIN FETCH s.user WHERE s.id = :id AND s.expiresAt > :now")
    Optional<Session> getValidSession(@Param("id") UUID id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);
}
