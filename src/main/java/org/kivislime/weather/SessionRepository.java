package org.kivislime.weather;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    int deleteByExpiresAtBefore(Instant now);
}
