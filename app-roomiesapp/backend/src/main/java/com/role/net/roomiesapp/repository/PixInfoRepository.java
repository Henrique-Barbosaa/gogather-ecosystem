package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.PixInfo;
import com.role.net.roomiesapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PixInfoRepository extends JpaRepository<PixInfo, Long> {
    Optional<PixInfo> findByUser(User user);
    Optional<PixInfo> findByUser_ExternalId(UUID userExternalId);
}
