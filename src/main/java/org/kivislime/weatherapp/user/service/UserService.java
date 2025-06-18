package org.kivislime.weatherapp.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kivislime.weatherapp.security.util.PasswordUtil;
import org.kivislime.weatherapp.user.dto.UserDto;
import org.kivislime.weatherapp.user.entity.User;
import org.kivislime.weatherapp.user.exception.UserAlreadyExistsException;
import org.kivislime.weatherapp.user.mapper.UserMapper;
import org.kivislime.weatherapp.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto registerUser(String login, String password) {
        User user = new User();
        try {
            user.setLogin(login);
            user.setPassword(PasswordUtil.hashPassword(password));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Unique constraint failed but record not found for login: " + login);
        }
        log.info("User registration successful: {}", login);
        return userMapper.toDto(user);
    }
}
