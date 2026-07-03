package com.role.net.roomiesapp.repository;

import com.role.net.roomiesapp.entity.HouseBill;
import com.role.net.roomiesapp.entity.HouseDebt;
import com.role.net.roomiesapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HouseDebtRepository extends JpaRepository<HouseDebt, Long> {
    List<HouseDebt> findByBill(HouseBill bill);
    List<HouseDebt> findByDebtor(User debtor);
    List<HouseDebt> findByCreditor(User creditor);
    Optional<HouseDebt> findByExternalId(UUID externalId);
    void deleteByBill(HouseBill bill);
}
