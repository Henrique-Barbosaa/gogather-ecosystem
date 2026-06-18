package com.role.net.gogather.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.role.net.gogather.entity.ExpenseDistribution;
import com.role.net.gogather.enums.SplitStatus;

public interface ExpenseDistributionRepository extends JpaRepository<ExpenseDistribution, Long> {
    Optional<ExpenseDistribution> findByExternalId(UUID externalId);

    boolean existsByParentExpense_IdAndStatus(Long id, SplitStatus status);
}
