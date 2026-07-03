package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.PackingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackingRepository extends JpaRepository<PackingItem, Long> {
    List<PackingItem> findByGroupIdOrderByIdDesc(Long groupId);
}
