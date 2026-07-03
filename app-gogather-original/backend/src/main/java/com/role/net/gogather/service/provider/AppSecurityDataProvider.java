package com.role.net.gogather.service.provider;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.role.net.gogather.repository.UserRepository;

import gogather.framework.security.core.SecurityDataProvider;

@Service
public class AppSecurityDataProvider implements SecurityDataProvider {

    private final UserRepository userRepository;

    public AppSecurityDataProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDetails> loadUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .map(user -> (UserDetails) user);
    }
}
