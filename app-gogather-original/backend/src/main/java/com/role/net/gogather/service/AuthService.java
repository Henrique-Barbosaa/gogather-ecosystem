package com.role.net.gogather.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.role.net.gogather.dto.auth.RegisterUserRequest;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.exception.UniqueDataAlreadyInUseException;
import com.role.net.gogather.repository.UserRepository;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.encoder = passwordEncoder;
    }

    public User registerUser(RegisterUserRequest registerUserRequest) {

        if(userRepository.existsByEmail(registerUserRequest.email()))
            throw new UniqueDataAlreadyInUseException("E-mail '" + registerUserRequest.email() + "' já em uso!");
        if(userRepository.existsByUsername(registerUserRequest.username()))
            throw new UniqueDataAlreadyInUseException("Username '" + registerUserRequest.username() + "' já em uso!");

        User newUser = new User();
        String displayName = registerUserRequest.displayName();
        newUser.setDisplayName(displayName == null || displayName.isBlank()
                ? registerUserRequest.username()
                : registerUserRequest.displayName());
        newUser.setUsername(registerUserRequest.username());
        newUser.setEmail(registerUserRequest.email());
        newUser.setPassword(encoder.encode(registerUserRequest.password()));
        newUser.setBirthDate(registerUserRequest.birthDate());
        userRepository.save(newUser);

        return newUser;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

	public User loadUserById(Long id) throws UsernameNotFoundException {
		return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("ID " + id));
	}

}
