package org.kivislime.weather.service;

import lombok.RequiredArgsConstructor;
import org.kivislime.weather.exception.BadRequestException;
import org.kivislime.weather.exception.SessionNotFoundException;
import org.kivislime.weather.exception.UserNotFoundException;
import org.kivislime.weather.security.CookieProperties;
import org.kivislime.weather.dto.SessionDto;
import org.kivislime.weather.entity.Session;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.mapper.SessionMapper;
import org.kivislime.weather.mapper.UserMapper;
import org.kivislime.weather.repository.SessionRepository;
import org.kivislime.weather.repository.UserRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Clock clock;
    private final CookieProperties cookieProperties;

    //TODO: its okay if userRepository injected in session service ?
    //TODO: Transactional норм тема?
    @Transactional
    public SessionDto createSession(String login, String password) {
        User user = userRepository.findByLogin(login).orElseThrow(
                () -> new BadRequestException(String.format("Incorrect login: %s", login))
        );
        if (!user.getPassword().equals(password)) {
            throw new BadRequestException(String.format("Incorrect password for: %s", login));
        }

        Session session = new Session();
        session.setUserId(user.getId());
        session.setId(UUID.randomUUID());

        Instant now = Instant.now(clock);
        Instant expires = now.plusSeconds(cookieProperties.getCookieMaxAge());
        session.setExpiresAt(expires);

        sessionRepository.save(session);
        return sessionMapper.toSessionDto(session);
    }

    @Transactional
    public void deleteSession(String uuidCookie) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidCookie);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(String.format("Invalid session ID format: %s", uuidCookie));
        }

        try {
            sessionRepository.deleteById(uuid);
        } catch (EmptyResultDataAccessException ex) {
            throw new SessionNotFoundException(String.format("%s", uuid));
        }
    }

    @Transactional
    public UserDto findUserDtoBySessionUuid(String sessionUuid) {
        Session session = sessionRepository.findById(UUID.fromString(sessionUuid))
                .orElseThrow(() -> new SessionNotFoundException(String.format("%s", sessionUuid)));

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new UserNotFoundException(String.format("user_id: %s, session_id: %s", session.getUserId(), sessionUuid)));

        return userMapper.toDto(user);
    }

    @Transactional
    public int removeAllExpired() {
        return sessionRepository.deleteByExpiresAtBefore(Instant.now(clock).minus(
                Duration.ofSeconds(cookieProperties.getCookieMaxAge())));
    }
}
