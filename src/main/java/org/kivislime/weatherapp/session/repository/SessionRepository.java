package org.kivislime.weatherapp.session.repository;

import org.kivislime.weatherapp.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    int deleteByExpiresAtBefore(Instant now);
}
