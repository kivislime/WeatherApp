package org.kivislime.weatherapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kivislime.weatherapp.user.dto.UserDto;
import org.kivislime.weatherapp.user.exception.UserAlreadyExistsException;
import org.kivislime.weatherapp.security.util.PasswordUtil;
import org.kivislime.weatherapp.user.entity.User;
import org.kivislime.weatherapp.user.mapper.UserMapper;
import org.kivislime.weatherapp.user.repository.UserRepository;
import org.kivislime.weatherapp.user.service.UserService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

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
    void registerUser_UserAlreadyExists_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser("dummy", "anyPassword"));

        verify(userRepository).save(any(User.class));
    }


    @Test
    void registerUser_UserNonExists_ShouldSaveAndReturnDto() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        UserDto fakeDto = new UserDto(1L, "dummy");
        when(userMapper.toDto(any(User.class))).thenReturn(fakeDto);

        UserDto result = userService.registerUser("dummy", "dummy");

        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(argThat(u -> u.getLogin().equals("dummy") && u.getId() == 1L));

        assertThat(result).isSameAs(fakeDto);
    }

}
