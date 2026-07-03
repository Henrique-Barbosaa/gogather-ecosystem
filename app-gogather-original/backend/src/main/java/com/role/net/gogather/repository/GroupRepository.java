package com.role.net.gogather.repository;

import com.role.net.gogather.entity.Group;
import gogather.framework.group.jpa.repository.BaseGroupRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends BaseGroupRepository<Group> {
}