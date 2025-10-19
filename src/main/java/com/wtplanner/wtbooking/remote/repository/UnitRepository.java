package com.wtplanner.wtbooking.remote.repository;

import com.wtplanner.wtbooking.model.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {
    Page<Unit> findAllByCostBetweenAndIsAvailableTrue(BigDecimal minCost, BigDecimal maxCost, Pageable pageable);

    @Query("SELECT COUNT(u) FROM Unit u WHERE u.isAvailable = true")
    long countAvailable();
}
