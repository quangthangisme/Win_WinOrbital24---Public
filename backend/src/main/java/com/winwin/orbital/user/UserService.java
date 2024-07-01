package com.winwin.orbital.user;

import com.winwin.orbital.exception.RegistrationException;
import com.winwin.orbital.exception.UserAlreadyExistException;
import com.winwin.orbital.manager.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public com.winwin.orbital.user.User registerNewUserAccount(UserDto userDto)
            throws UserAlreadyExistException, RegistrationException {

        if (emailExists(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + userDto.getEmail());
        }

        if (usernameExists(userDto.getUsername())) {
            throw new UserAlreadyExistException("There is an account with that username: "
                    + userDto.getUsername());
        }

        try {
            com.winwin.orbital.user.User user = new User();
            user.setUsername(userDto.getUsername());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setEmail(userDto.getEmail());
            user.setRole("USER");

            Manager manager = new Manager();
            manager.setUser(user);
            user.setManager(manager);

            return userRepository.save(user);
        } catch (Exception e) {
            throw new RegistrationException("An error occurs during registration.");
        }
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

}
