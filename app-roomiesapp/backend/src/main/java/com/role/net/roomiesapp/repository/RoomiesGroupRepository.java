package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomiesGroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByInviteCode(String inviteCode);
    Optional<Group> findByExternalId(java.util.UUID externalId);

    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = :userId")
    List<Group> findGroupsByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Group g JOIN g.members m WHERE g.inviteCode = :inviteCode AND m.user.id = :userId")
    boolean isGroupMemberByInviteCode(@Param("inviteCode") String inviteCode, @Param("userId") Long userId);
}
