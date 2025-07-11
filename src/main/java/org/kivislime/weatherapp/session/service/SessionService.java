package org.kivislime.weatherapp.session.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kivislime.weatherapp.session.dto.SessionDto;
import org.kivislime.weatherapp.session.entity.Session;
import org.kivislime.weatherapp.session.exception.InvalidCredentialsException;
import org.kivislime.weatherapp.session.exception.SessionNotFoundException;
import org.kivislime.weatherapp.session.mapper.SessionMapper;
import org.kivislime.weatherapp.session.repository.SessionRepository;
import org.kivislime.weatherapp.user.dto.UserDto;
import org.kivislime.weatherapp.user.entity.User;
import org.kivislime.weatherapp.weather.exception.BadRequestException;
import org.kivislime.weatherapp.user.exception.UserNotFoundException;
import org.kivislime.weatherapp.user.mapper.UserMapper;
import org.kivislime.weatherapp.user.repository.UserRepository;
import org.kivislime.weatherapp.security.CookieProperties;
import org.kivislime.weatherapp.security.util.PasswordUtil;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Clock clock;
    private final CookieProperties cookieProperties;

    @Transactional
    public SessionDto createSession(String login, String password) {
        User user = userRepository.findByLogin(login).orElseThrow(
                () -> new InvalidCredentialsException(String.format("Incorrect login: %s", login))
        );

        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException(String.format("Incorrect password for: %s", login));
        }

        Session session = new Session();
        session.setUserId(user.getId());
        session.setId(UUID.randomUUID());

        Instant now = Instant.now(clock);
        Instant expires = now.plusSeconds(cookieProperties.getCookieMaxAge());
        session.setExpiresAt(expires);

        log.info("Created new session for user: {}", login);
        sessionRepository.save(session);
        return sessionMapper.toDto(session);
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

    @Transactional(readOnly = true)
    public UserDto findUserDtoBySessionUuid(String sessionUuid) {
        UUID uuid;
        try {
            uuid = UUID.fromString(sessionUuid);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid session ID format: " + sessionUuid);
        }

        Session session = sessionRepository.findById(uuid)
                .orElseThrow(() -> new SessionNotFoundException(String.format("%s", sessionUuid)));

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new UserNotFoundException(String.format("user_id: %s, session_id: %s", session.getUserId(), sessionUuid)));

        return userMapper.toDto(user);
    }

    @Transactional(propagation = REQUIRES_NEW)
    public int removeAllExpired() {
        return sessionRepository.deleteByExpiresAtBefore(Instant.now(clock));
    }
}
