package org.kivislime.weatherapp.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weatherapp.config.JpaTestConfig;
import org.kivislime.weatherapp.user.dto.UserDto;
import org.kivislime.weatherapp.user.exception.UserAlreadyExistsException;
import org.kivislime.weatherapp.user.repository.UserRepository;
import org.kivislime.weatherapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaTestConfig.class)
@ActiveProfiles("test-jpa")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Registering a new user saves it in the database and hashes the password")
    void whenRegisterNewUser_thenPersistAndHashPassword() {
        String login = "ivan";
        String rawPwd = "secret";

        UserDto dto = userService.registerUser(login, rawPwd);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getLogin()).isEqualTo(login);

        var optUser = userRepository.findByLogin(login);
        assertThat(optUser).isPresent();
        var user = optUser.get();
        assertThat(user.getPassword()).startsWith("$2a$");  // BCrypt‑хэш
    }

    @Test
    @DisplayName("Повторная регистрация с тем же логином бросает UserAlreadyExistsException")
    void whenDuplicateLogin_thenThrow() {
        String login = "petr";
        userService.registerUser(login, "pwd");

        assertThatThrownBy(() -> userService.registerUser(login, "pwd2"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(login);
    }
}
