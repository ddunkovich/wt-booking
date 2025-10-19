package com.wtplanner.wtbooking.service;

import com.wtplanner.wtbooking.model.dto.UnitDto;
import com.wtplanner.wtbooking.model.entity.Unit;
import com.wtplanner.wtbooking.remote.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitService {
    private final UnitRepository repo;
    private final CacheService cacheService;

    public Page<Unit> search(BigDecimal minCost, BigDecimal maxCost, Pageable pageable) {
        return repo.findAllByCostBetweenAndIsAvailableTrue(minCost, maxCost, pageable);
    }

    /** Return number of available units using Redis cache */
    @Transactional(readOnly = true)
    public long countAvailable() {
        boolean valid = cacheService.isValid();
        Long cached = cacheService.getCachedCount();

        if (valid && cached != null) return cached;

        log.debug("Cache invalid → reading available units count from database");
        long count = repo.countAvailable();
        cacheService.updateCount(count);
        return count;
    }

    @Transactional
    public Unit save(UnitDto dto) {

        // Manual mapping from DTO → Entity
        Unit unit = Unit.builder()
                .rooms(dto.getRooms())
                .accommodationType(dto.getAccommodationType())
                .floor(dto.getFloor())
                .isAvailable(dto.getIsAvailable())
                .cost(dto.getCost())
                .bookingMarkupPercent(dto.getBookingMarkupPercent())
                .description(dto.getDescription())
                .build();

        log.debug("Saving Unit to database");
        Unit saved = repo.save(unit);

        if (Boolean.TRUE.equals(dto.getIsAvailable())) {
            cacheService.adjustCount(+1, repo.countAvailable());
        }
        return saved;
    }
}
