package org.kivislime.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.exception.UserAlreadyExistsException;
import org.kivislime.weather.mapper.UserMapper;
import org.kivislime.weather.repository.UserRepository;
import org.kivislime.weather.security.PasswordUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User dummyUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setLogin("dummy");
        dummyUser.setPassword(
                PasswordUtil.hashPassword("dummy")
        );
    }

    @Test
    void registrationUser_UserAlreadyExists_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.findByLogin("dummy")).thenReturn(Optional.of(dummyUser));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.registrationUser("dummy", "anyPassword"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registrationUser_UserNonExists_ShouldSaveAndReturnDto() {
        when(userRepository.findByLogin("dummy")).thenReturn(Optional.empty());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId(1L);
            return toSave;
        });

        UserDto fakeDto = new UserDto(1L, "dummy");
        when(userMapper.toDto(any(User.class))).thenReturn(fakeDto);

        UserDto result = userService.registrationUser("dummy", "dummy");

        verify(userRepository, times(1)).save(any(User.class));

        User savedUser = captor.getValue();
        assertThat(savedUser.getLogin()).isEqualTo("dummy");

        assertTrue(PasswordUtil.checkPassword("dummy", savedUser.getPassword()));

        verify(userMapper, times(1)).toDto(savedUser);

        assertThat(result).isSameAs(fakeDto);
    }
}
