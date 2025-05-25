package org.kivislime.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

//TODO: scheduler для проверки истечения сессий, если время истекло - удаляет
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
                () -> new RuntimeException("Wrong login or password")
        );
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Wrong login or password");
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
    public boolean deleteSession(String uuidCookie) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidCookie);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Wrong cookie");
        }

        //TODO: по идее это не ошибкa, норм формат?
        try {
            sessionRepository.deleteById(uuid);
            return true;
        } catch (EmptyResultDataAccessException ignore) {
            return false;
        }
    }

    @Transactional
    public UserDto findUserDtoBySessionUuid(String sessionUuid) {
        Session session = sessionRepository.findById(UUID.fromString(sessionUuid))
                .orElseThrow(() -> new RuntimeException("Session doesn't exist"));

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User doesn't found by id from session"));

        return userMapper.toDto(user);
    }

    public int removeAllExpired() {
        return sessionRepository.deleteByExpiresAtBefore(Instant.now(clock).minus(
                Duration.ofSeconds(cookieProperties.getCookieMaxAge())));
    }
}
