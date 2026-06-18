package com.role.net.gogather.repository;

import com.role.net.gogather.entity.Group;
import com.role.net.gogather.enums.GroupMemberStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

	@Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = :userId AND m.status = :status")
    List<Group> findGroupsByUserId(@Param("userId") Long userId, @Param("status") GroupMemberStatus status);

	Optional<Group> findByExternalId(UUID externalId);

	Optional<Group> findByInviteCode(String inviteCode);

	@Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM GroupMember m WHERE m.group.id = :groupId AND m.user.id = :userId AND m.status = :status) THEN true ELSE false END")
	boolean isGroupMember(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("status") GroupMemberStatus status);

	@Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM GroupMember m WHERE m.group.externalId = :externalId AND m.user.id = :userId AND m.status = :status) THEN true ELSE false END")
	boolean isGroupMemberByExternalId(@Param("externalId") UUID externalId, @Param("userId") Long userId, @Param("status") GroupMemberStatus status);
}
