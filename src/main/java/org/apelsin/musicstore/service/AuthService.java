package org.apelsin.musicstore.service;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.Role;
import org.apelsin.musicstore.model.User;
import org.apelsin.musicstore.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public User register(String username, String password, String roleStr) {
        if (userRepository.existsByUserUsername(username)) {
            throw new RuntimeException("Этот логин уже занят!");
        }

        User user = new User();
        user.setUserUsername(username);
        user.setUserPassword(password);

        if ("ADMIN".equalsIgnoreCase(roleStr)) {
            user.setUserRole(Role.ADMIN);
        } else {
            user.setUserRole(Role.USER);
        }

        return userRepository.save(user);
    }

    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUserUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getUserPassword().equals(password)) {
                return user;
            }
        }

        throw new RuntimeException("Неверный логин или пароль");
    }
}