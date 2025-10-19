package com.wtplanner.wtbooking.remote.repository;

import com.wtplanner.wtbooking.model.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {
    Page<Unit> findAllByCostBetweenAndIsAvailableTrue(BigDecimal minCost, BigDecimal maxCost, Pageable pageable);
}
