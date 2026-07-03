package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.HouseBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HouseBillRepository extends JpaRepository<HouseBill, Long> {
    List<HouseBill> findByGroup(Group group);
    Optional<HouseBill> findByExternalId(UUID externalId);
    List<HouseBill> findByGroup_ExternalId(UUID groupExternalId);
}
