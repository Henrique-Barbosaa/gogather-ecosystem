package com.role.net.tripmaker.service.provider;

import com.role.net.tripmaker.repository.UserRepository;
import gogather.framework.security.core.SecurityDataProvider;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppSecurityDataProvider implements SecurityDataProvider {

    private final UserRepository userRepository;

    public AppSecurityDataProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDetails> loadUserByUsername(String identifier) {
        return userRepository.findByUsername(identifier)
            .or(() -> userRepository.findByEmail(identifier))
            .map(user -> (UserDetails) user);
    }
}
