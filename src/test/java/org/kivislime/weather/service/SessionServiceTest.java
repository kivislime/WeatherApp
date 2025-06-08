package org.kivislime.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weather.dto.SessionDto;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.entity.Session;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.exception.BadRequestException;
import org.kivislime.weather.exception.InvalidCredentialsException;
import org.kivislime.weather.exception.SessionNotFoundException;
import org.kivislime.weather.exception.UserNotFoundException;
import org.kivislime.weather.mapper.SessionMapper;
import org.kivislime.weather.mapper.UserMapper;
import org.kivislime.weather.repository.SessionRepository;
import org.kivislime.weather.repository.UserRepository;
import org.kivislime.weather.security.CookieProperties;
import org.kivislime.weather.security.PasswordUtil;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionMapper sessionMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Clock clock;

    @Mock
    private CookieProperties cookieProperties;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(
                sessionRepository,
                sessionMapper,
                userRepository,
                userMapper,
                clock,
                cookieProperties
        );
    }

    @Test
    void createSession_InvalidLogin_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.createSession("nonexistent", "anyPass"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Incorrect login: nonexistent");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_WrongPassword_ShouldThrowInvalidCredentialsException() {
        User user = new User();
        user.setId(10L);
        user.setLogin("john");
        user.setPassword(PasswordUtil.hashPassword("correctPass"));
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sessionService.createSession("john", "wrongPass"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Incorrect password for: john");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_ValidCredentials_ShouldSaveAndReturnDto() {
        Instant fixedInstant = Instant.parse("2025-06-01T12:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);
        int cookieMaxAge = 3600;
        when(cookieProperties.getCookieMaxAge()).thenReturn(cookieMaxAge);

        String rawPassword = "mypassword";
        String hashed = PasswordUtil.hashPassword(rawPassword);

        User user = new User();
        user.setId(20L);
        user.setLogin("alice");
        user.setPassword(hashed);
        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));

        SessionDto expectedDto = new SessionDto(UUID.randomUUID(), 20L, fixedInstant.plusSeconds(cookieMaxAge));
        doAnswer(invocation -> expectedDto)
                .when(sessionMapper)
                .toDto(any(Session.class));

        SessionDto result = sessionService.createSession("alice", rawPassword);

        assertThat(result).isEqualTo(expectedDto);

        verify(sessionRepository).save(argThat(session ->
                session.getUserId().equals(20L) &&
                        session.getExpiresAt().equals(fixedInstant.plusSeconds(cookieMaxAge)) &&
                        session.getId() != null
        ));
    }

    @Test
    void deleteSession_InvalidUuidFormat_ShouldThrowBadRequestException() {
        String badUuid = "not-a-uuid";
        assertThatThrownBy(() -> sessionService.deleteSession(badUuid))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid session ID format");

        verify(sessionRepository, never()).deleteById(any());
    }

    @Test
    void deleteSession_SessionNotFound_ShouldThrowSessionNotFoundException() {
        UUID fakeUuid = UUID.randomUUID();
        doThrow(new EmptyResultDataAccessException(1))
                .when(sessionRepository).deleteById(fakeUuid);

        assertThatThrownBy(() -> sessionService.deleteSession(fakeUuid.toString()))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining(fakeUuid.toString());

        verify(sessionRepository).deleteById(fakeUuid);
    }

    @Test
    void deleteSession_ValidUuid_ShouldCallRepository() {
        UUID uuid = UUID.randomUUID();
        sessionService.deleteSession(uuid.toString());
        verify(sessionRepository).deleteById(uuid);
    }

    @Test
    void findUserDtoBySessionUuid_InvalidUuidFormat_ShouldThrowBadRequestException() {
        assertThatThrownBy(() -> sessionService.findUserDtoBySessionUuid("invalid-uuid"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid session ID format");
    }

    @Test
    void findUserDtoBySessionUuid_SessionNotFound_ShouldThrowSessionNotFoundException() {
        UUID uuid = UUID.randomUUID();
        when(sessionRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.findUserDtoBySessionUuid(uuid.toString()))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining(uuid.toString());

        verify(sessionRepository).findById(uuid);
    }

    @Test
    void findUserDtoBySessionUuid_UserNotFound_ShouldThrowUserNotFoundException() {
        UUID uuid = UUID.randomUUID();
        Session session = new Session();
        session.setId(uuid);
        session.setUserId(55L);
        when(sessionRepository.findById(uuid)).thenReturn(Optional.of(session));
        when(userRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.findUserDtoBySessionUuid(uuid.toString()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("user_id: 55");

        verify(userRepository).findById(55L);
    }

    @Test
    void findUserDtoBySessionUuid_ValidSessionAndUser_ShouldReturnDto() {
        UUID uuid = UUID.randomUUID();
        Session session = new Session();
        session.setId(uuid);
        session.setUserId(77L);
        when(sessionRepository.findById(uuid)).thenReturn(Optional.of(session));

        User user = new User();
        user.setId(77L);
        user.setLogin("bob");
        user.setPassword("irrelevant");
        when(userRepository.findById(77L)).thenReturn(Optional.of(user));

        UserDto expectedDto = new UserDto(77L, "bob");
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserDto result = sessionService.findUserDtoBySessionUuid(uuid.toString());
        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void removeAllExpired_ShouldReturnDeletedCount() {
        // Настраиваем clock.instant() только в этом тесте
        Instant fixedInstant = Instant.parse("2025-06-01T12:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);

        when(sessionRepository.deleteByExpiresAtBefore(fixedInstant)).thenReturn(5);

        int deleted = sessionService.removeAllExpired();
        assertThat(deleted).isEqualTo(5);

        verify(sessionRepository).deleteByExpiresAtBefore(fixedInstant);
    }
}
