package com.role.net.gogather.repository;

import com.role.net.gogather.entity.Group;
import gogather.framework.group.jpa.repository.BaseGroupRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends BaseGroupRepository<Group> {

    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = :userId")
    List<Group> findGroupsByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM GroupMember m WHERE m.group.id = :groupId AND m.user.id = :userId) THEN true ELSE false END")
    boolean isGroupMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM GroupMember m WHERE m.group.inviteCode = :inviteCode AND m.user.id = :userId) THEN true ELSE false END")
    boolean isGroupMemberByInviteCode(@Param("inviteCode") String inviteCode, @Param("userId") Long userId);
}