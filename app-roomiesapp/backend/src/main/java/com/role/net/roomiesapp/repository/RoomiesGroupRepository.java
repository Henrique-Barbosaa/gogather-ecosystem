package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.Group;
import gogather.framework.group.jpa.repository.BaseGroupRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomiesGroupRepository extends BaseGroupRepository<Group> {
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = :userId")
    List<Group> findGroupsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) > 0 FROM Group g JOIN g.members m WHERE g.inviteCode = :inviteCode AND m.user.id = :userId")
    boolean isGroupMemberByInviteCode(@Param("inviteCode") String inviteCode, @Param("userId") Long userId);
}
