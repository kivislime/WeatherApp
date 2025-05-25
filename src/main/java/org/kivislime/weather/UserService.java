package org.kivislime.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    //TODO: @Transactional? Here 2 sql requests
    public UserDto registrationUser(String login, String password) {
        Optional<User> userOptional = userRepository.findByLogin(login);
        userOptional.ifPresent(user -> {
            throw new IllegalArgumentException("Login already exists");
        });

        User user = new User();
        user.setLogin(login);
        user.setPassword(password);
        userRepository.save(user);

        return userMapper.toDto(user);
    }
}
