package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {
    List<Chore> findByGroupId(Long groupId);
}
