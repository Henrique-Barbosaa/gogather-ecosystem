package com.role.net.gogather.repository;

import com.role.net.gogather.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    @Modifying
    @Query("UPDATE PollOption p SET p.votes = p.votes + 1 WHERE p.id = :id")
    void incrementVote(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PollOption p SET p.votes = p.votes - 1 WHERE p.id = :id AND p.votes > 0")
    void decrementVote(@Param("id") Long id);
}
