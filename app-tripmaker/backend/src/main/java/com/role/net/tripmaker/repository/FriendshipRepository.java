package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByRequesterIdOrReceiverId(Long requesterId, Long receiverId);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :u1 AND f.receiver.id = :u2) OR (f.requester.id = :u2 AND f.receiver.id = :u1)")
    Optional<Friendship> findFriendshipBetween(@Param("u1") Long u1, @Param("u2") Long u2);
}
