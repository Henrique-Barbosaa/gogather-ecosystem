package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByExternalId(java.util.UUID externalId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
