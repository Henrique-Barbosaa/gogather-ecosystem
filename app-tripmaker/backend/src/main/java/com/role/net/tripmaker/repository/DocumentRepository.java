package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.TripDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<TripDocument, Long> {
    List<TripDocument> findByGroupIdOrderByCreatedAtDocDesc(Long groupId);
}
