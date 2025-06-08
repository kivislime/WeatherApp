package org.kivislime.weather.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto registrationUser(String login, String password) {
        Optional<User> userOptional = userRepository.findByLogin(login);

        userOptional.ifPresent(user -> {
            throw new UserAlreadyExistsException(login);
        });

        User user = new User();
        try {
            user.setLogin(login);
            user.setPassword(PasswordUtil.hashPassword(password));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            user = userRepository.findByLogin(login)
                    .orElseThrow(() -> new UserAlreadyExistsException("Unique constraint failed but record not found"));
        }
        return userMapper.toDto(user);
    }
}
