package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.ItineraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryRepository extends JpaRepository<ItineraryEvent, Long> {
    List<ItineraryEvent> findByGroupIdOrderByStartTimeAsc(Long groupId);
}
