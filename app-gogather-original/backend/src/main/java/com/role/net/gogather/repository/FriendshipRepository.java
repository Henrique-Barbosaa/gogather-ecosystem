package com.role.net.gogather.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.role.net.gogather.entity.Friendship;
import com.role.net.gogather.enums.FriendshipStatus;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByExternalId(UUID id);
    Optional<Friendship> findByExternalIdAndRequesterId(UUID id, Long requesterId);
    Optional<Friendship> findByExternalIdAndReceiverId(UUID id, Long receiverId);

    @Query("""
        SELECT fs
        FROM Friendship fs
        WHERE fs.requester.username = :username
        OR fs.receiver.username = :username
    """)
    Optional<Friendship> findFriendshipByUsername(@Param("username") String username);

    @Query("""
        SELECT fs
        FROM Friendship fs
        WHERE
        (fs.requester.id = :userId1 AND fs.receiver.id = :userId2)
        OR
        (fs.requester.id = :userId2 AND fs.receiver.id = :userId1)
    """)
    Optional<Friendship> findFriendshipBetweenUsers(
        @Param("userId1") Long requesterId,
        @Param("userId2") Long receiverId
    );

    @Query("""
        SELECT fs
        FROM Friendship fs
        WHERE
        (fs.requester.externalId = :userId1 AND fs.receiver.externalId = :userId2)
        OR
        (fs.requester.externalId = :userId2 AND fs.receiver.externalId = :userId1)
    """)
    Optional<Friendship> findFriendshipBetweenUsers(
        @Param("userId1") UUID requesterId,
        @Param("userId2") UUID receiverId
    );

    List<Friendship> findByReceiverIdAndStatus(
        Long id,
        FriendshipStatus status
    );

    @Query("""
        SELECT fs
        FROM Friendship fs
        WHERE (fs.requester.id = :userId OR fs.receiver.id = :userId)
          AND fs.status = :status
    """)
    List<Friendship> findByUserIdAndStatus(
        @Param("userId") Long id,
        @Param("status") FriendshipStatus status
    );

    @Query("""
        SELECT COUNT(fs) > 0
        FROM Friendship fs
        WHERE fs.requester.id = :reqid
          AND fs.receiver.externalId = :recid
          AND fs.status = :status
    """)
    boolean checkRequest(
        @Param("reqid") Long requesterId,
        @Param("recid") Long receiverId,
        @Param("status") FriendshipStatus status
    );
}
