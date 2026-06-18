package com.role.net.gogather.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import com.role.net.gogather.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<UserDetails> findUserByUsername(String username);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByExternalId(UUID externalId);

    List<User> findByUsernameContainingIgnoreCase(String search);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
