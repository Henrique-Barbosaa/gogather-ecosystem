package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.Group;
import gogather.framework.group.jpa.repository.BaseGroupRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripGroupRepository extends BaseGroupRepository<Group> {
}
