package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingRepository extends JpaRepository<ShoppingItem, Long> {
    List<ShoppingItem> findByGroupIdOrderByBoughtAscCreatedAtItemDesc(Long groupId);
}
