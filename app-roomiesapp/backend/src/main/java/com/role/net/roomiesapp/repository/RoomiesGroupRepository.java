package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.Group;
import gogather.framework.group.jpa.repository.BaseGroupRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomiesGroupRepository extends BaseGroupRepository<Group> {
}
