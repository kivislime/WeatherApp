package org.kivislime.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.entity.User;
import org.kivislime.weather.exception.UserAlreadyExistsException;
import org.kivislime.weather.mapper.UserMapper;
import org.kivislime.weather.repository.UserRepository;
import org.kivislime.weather.security.PasswordUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto registrationUser(String login, String password) {
        User user = new User();
        try {
            user.setLogin(login);
            user.setPassword(PasswordUtil.hashPassword(password));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Unique constraint failed but record not found");
        }
        log.info("User registration successful: {}", login);
        return userMapper.toDto(user);
    }
}
