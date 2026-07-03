package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.dto.auth.ChangePasswordRequest;
import com.role.net.roomiesapp.dto.auth.RegisterUserRequest;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.exception.InvalidCredentialsException;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.exception.UniqueDataAlreadyInUseException;
import com.role.net.roomiesapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UniqueDataAlreadyInUseException("E-mail '" + request.email() + "' já está em uso!");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UniqueDataAlreadyInUseException("Username '" + request.username() + "' já está em uso!");
        }

        User newUser = new User();
        String displayName = request.displayName();
        newUser.setName(displayName == null || displayName.isBlank() ? request.username() : displayName);
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setPhoneNumber(request.phoneNumber());
        newUser.setBirthDate(request.birthDate());
        newUser.setActive(true);

        return userRepository.save(newUser);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Senha antiga incorreta!");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return userRepository.findByUsername(identifier)
            .or(() -> userRepository.findByEmail(identifier))
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o identificador: " + identifier));
    }

    @Transactional(readOnly = true)
    public User loadUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }
}
