package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.HouseNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<HouseNotice, Long> {
    List<HouseNotice> findByGroupIdOrderByCreatedAtNoticeDesc(Long groupId);
}
