package org.kivislime.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.kivislime.weather.security.PasswordUtil; // импорт для BCrypt
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    private CookieProperties cookieProperties;

    private SessionService sessionService;

    private User unsavedUserEntity;
    private User existingUserEntity;
    private Clock clock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        clock = Clock.fixed(Instant.parse("2025-05-30T12:00:00Z"), ZoneOffset.UTC);

        sessionService = new SessionService(
                sessionRepository,
                sessionMapper,
                userRepository,
                userMapper,
                clock,
                cookieProperties
        );

        unsavedUserEntity = new User();
        unsavedUserEntity.setId(1L);
        unsavedUserEntity.setLogin("john");

        existingUserEntity = new User();
        existingUserEntity.setId(2L);
        existingUserEntity.setLogin("serg");
        existingUserEntity.setPassword(PasswordUtil.hashPassword("psw"));
    }

    @Test
    void createSession_IncorrectLogin_InvalidCredentialsException() {
        when(userRepository.findByLogin(unsavedUserEntity.getLogin())).thenReturn(Optional.empty());
        assertThrows(
                InvalidCredentialsException.class,
                () -> sessionService.createSession(unsavedUserEntity.getLogin(), "anyPassword")
        );
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_IncorrectPassword_InvalidCredentialsException() {
        when(userRepository.findByLogin("serg")).thenReturn(Optional.of(existingUserEntity));

        assertThrows(
                InvalidCredentialsException.class,
                () -> sessionService.createSession("serg", "wrongPassword")
        );
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_CorrectLoginAndPassword_ShouldSaveAndReturnDto() {
        when(userRepository.findByLogin("serg")).thenReturn(Optional.of(existingUserEntity));
        when(cookieProperties.getCookieMaxAge()).thenReturn(3600);

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        when(sessionRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        SessionDto fakeDto = new SessionDto(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                existingUserEntity.getId(),
                Instant.parse("2025-05-30T13:00:00Z")
        );
        when(sessionMapper.toSessionDto(any(Session.class))).thenReturn(fakeDto);

        SessionDto result = sessionService.createSession("serg", "psw");

        verify(sessionRepository, times(1)).save(any(Session.class));

        Session savedSession = captor.getValue();
        assertThat(savedSession.getUserId()).isEqualTo(2L);
        assertThat(savedSession.getId()).isNotNull();
        Instant expectedExpires = Instant.parse("2025-05-30T12:00:00Z").plusSeconds(3600);
        assertThat(savedSession.getExpiresAt()).isEqualTo(expectedExpires);

        verify(sessionMapper, times(1)).toSessionDto(savedSession);

        assertThat(result.getUserId()).isEqualTo(existingUserEntity.getId());
        assertThat(result).isSameAs(fakeDto);
    }

    @Test
    void deleteSession_IncorrectCookie_ShouldThrowBadRequestException() {
        String incorrectUuid = "incorrectUuid";
        assertThrows(BadRequestException.class, () -> sessionService.deleteSession(incorrectUuid));
        verify(sessionRepository, never()).deleteById(any());
    }

    @Test
    void deleteSession_SessionNotFound_ShouldThrowSessionNotFoundException() {
        String validUuid = "00000000-0000-0000-0000-000000000001";
        UUID uuid = UUID.fromString(validUuid);

        doThrow(new EmptyResultDataAccessException(1))
                .when(sessionRepository).deleteById(uuid);

        assertThrows(
                SessionNotFoundException.class,
                () -> sessionService.deleteSession(validUuid)
        );

        verify(sessionRepository, times(1)).deleteById(uuid);
    }

    @Test
    void deleteSession_ValidUuidAndExists_ShouldCallDeleteOnce() {
        String validUuid = "00000000-0000-0000-0000-000000000001";
        UUID uuid = UUID.fromString(validUuid);

        sessionService.deleteSession(validUuid);

        verify(sessionRepository, times(1)).deleteById(uuid);
    }

    @Test
    void findUserDtoBySessionUuid_SessionNotFound_ShouldThrowSessionNotFoundException() {
        String validUuid = "00000000-0000-0000-0000-000000000002";
        UUID uuid = UUID.fromString(validUuid);

        when(sessionRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(
                SessionNotFoundException.class,
                () -> sessionService.findUserDtoBySessionUuid(validUuid)
        );

        verify(sessionRepository, times(1)).findById(uuid);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void findUserDtoBySessionUuid_UserNotFound_ShouldThrowUserNotFoundException() {
        String validUuid = "00000000-0000-0000-0000-000000000003";
        UUID uuid = UUID.fromString(validUuid);

        Session fakeSession = new Session();
        fakeSession.setId(uuid);
        fakeSession.setUserId(unsavedUserEntity.getId());

        when(sessionRepository.findById(uuid)).thenReturn(Optional.of(fakeSession));
        when(userRepository.findById(unsavedUserEntity.getId())).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> sessionService.findUserDtoBySessionUuid(validUuid)
        );

        verify(sessionRepository, times(1)).findById(uuid);
        verify(userRepository, times(1)).findById(unsavedUserEntity.getId());
    }

    @Test
    void findUserDtoBySessionUuid_Valid_ShouldReturnUserDto() {
        String validUuid = "00000000-0000-0000-0000-000000000004";
        UUID uuid = UUID.fromString(validUuid);

        Session fakeSession = new Session();
        fakeSession.setId(uuid);
        fakeSession.setUserId(existingUserEntity.getId());

        UserDto fakeUserDto = new UserDto(existingUserEntity.getId(), existingUserEntity.getLogin());

        when(sessionRepository.findById(uuid)).thenReturn(Optional.of(fakeSession));
        when(userRepository.findById(existingUserEntity.getId())).thenReturn(Optional.of(existingUserEntity));
        when(userMapper.toDto(existingUserEntity)).thenReturn(fakeUserDto);

        UserDto result = sessionService.findUserDtoBySessionUuid(validUuid);

        verify(sessionRepository, times(1)).findById(uuid);
        verify(userRepository, times(1)).findById(existingUserEntity.getId());
        verify(userMapper, times(1)).toDto(existingUserEntity);

        assertThat(result).isSameAs(fakeUserDto);
    }

    @Test
    void removeAllExpired_ShouldCallDeleteByExpiresAtBeforeAndReturnCount() {
        when(cookieProperties.getCookieMaxAge()).thenReturn(1800);

        Instant cutoff = Instant.parse("2025-05-30T12:00:00Z")
                .minus(Duration.ofSeconds(1800));

        when(sessionRepository.deleteByExpiresAtBefore(cutoff)).thenReturn(3);

        int deletedCount = sessionService.removeAllExpired();

        verify(sessionRepository, times(1)).deleteByExpiresAtBefore(cutoff);
        assertThat(deletedCount).isEqualTo(3);
    }
}
