package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.TripDebt;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripDebtRepository extends JpaRepository<TripDebt, Long> {

    List<TripDebt> findByExpenseId(Long expenseId);

    List<TripDebt> findByExpenseTripId(Long tripId);

    Optional<TripDebt> findByExternalId(UUID externalId);

    void deleteByExpenseId(Long expenseId);
}
