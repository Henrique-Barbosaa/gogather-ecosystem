package com.role.net.tripmaker.repository;

import com.role.net.tripmaker.entity.TripExpense;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripExpenseRepository extends JpaRepository<TripExpense, Long> {

    List<TripExpense> findByTripIdOrderByExpenseDateDesc(Long tripId);

    Optional<TripExpense> findByExternalId(UUID externalId);
}
