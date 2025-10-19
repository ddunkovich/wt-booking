package com.wtplanner.wtbooking.service;

import com.wtplanner.wtbooking.model.entity.Unit;
import com.wtplanner.wtbooking.remote.repository.UnitRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UnitService {
    private final UnitRepository repo;

    public UnitService(UnitRepository repo) { this.repo = repo; }

    public Page<Unit> search(BigDecimal minCost, BigDecimal maxCost, Pageable pageable) {
        return repo.findAllByCostBetweenAndIsAvailableTrue(minCost, maxCost, pageable);
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        return repo.count();
    }

    @Transactional
    public Unit save(Unit unit) {
        return repo.save(unit);
    }
}
