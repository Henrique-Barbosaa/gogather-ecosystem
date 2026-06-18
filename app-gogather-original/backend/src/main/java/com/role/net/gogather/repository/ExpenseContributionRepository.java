package com.role.net.gogather.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.role.net.gogather.entity.ExpenseContribution;

public interface ExpenseContributionRepository extends JpaRepository<ExpenseContribution, Long> {
    Optional<ExpenseContribution> findByExternalId(UUID externalId);
}
