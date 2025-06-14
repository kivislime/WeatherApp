package org.kivislime.weather.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weather.config.AppConfig;
import org.kivislime.weather.config.JpaConfig;
import org.kivislime.weather.config.TestConfig;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.exception.UserAlreadyExistsException;
import org.kivislime.weather.mapper.UserMapper;
import org.kivislime.weather.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AppConfig.class,
        TestConfig.class,
        JpaConfig.class,
        UserService.class
})
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("При регистрации нового пользователя он появляется в базе")
    void registrationCreatesNewUser() {
        String login = "ivan";
        String password = "secret";

        UserDto dto = userService.registrationUser(login, password);

        assertNotNull(dto.getId(), "DTO должен содержать сгенерированный ID");
        assertEquals(login, dto.getLogin());

        // Проверяем, что в БД лежит именно тот юзер
        User u = userRepository.findByLogin(login)
                .orElseThrow(() -> new AssertionError("User not found in DB"));
        assertEquals(dto.getId(), u.getId());
        assertTrue(u.getPassword().startsWith("$2a$"), "Пароль должен быть захеширован"); // если BCrypt
    }

    @Test
    @DisplayName("Повторная регистрация с тем же логином бросает UserAlreadyExistsException")
    void registrationDuplicateLoginThrows() {
        String login = "petr";
        String password = "pwd";

        userService.registrationUser(login, password);

        UserAlreadyExistsException ex = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registrationUser(login, "another")
        );
        assertTrue(ex.getMessage().contains(login));
    }
}
