package com.role.net.gogather.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.role.net.gogather.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByExternalId(UUID externalId);
    List<Expense> findByGroup_InviteCode(String inviteCode);
}
