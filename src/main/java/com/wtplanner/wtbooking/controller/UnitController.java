package com.wtplanner.wtbooking.controller;

import com.wtplanner.wtbooking.model.dto.UnitDto;
import com.wtplanner.wtbooking.model.entity.Unit;
import com.wtplanner.wtbooking.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wtbooking")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @Operation(summary = "Search available units by cost range with pagination and sorting")
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) BigDecimal minCost,
            @RequestParam(required = false) BigDecimal maxCost,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id,asc") String sort) {

        try {
            if (minCost == null) minCost = BigDecimal.ZERO;
            if (maxCost == null) maxCost = BigDecimal.valueOf(Double.MAX_VALUE);
            if (page < 0) page = 0;
            if (pageSize <= 0) pageSize = 10;

            String[] sortParts = sort.split(",");
            String sortField = sortParts[0];
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortParts.length > 1) {
                try {
                    direction = Sort.Direction.fromString(sortParts[1]);
                } catch (IllegalArgumentException ignored) {
                    direction = Sort.Direction.ASC;
                }
            }

            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortField));
            Page<Unit> pageResult = unitService.search(minCost, maxCost, pageable);

            // Lightweight, stable JSON pagination structure
            Map<String, Object> response = Map.of(
                    "page", pageResult.getNumber(),
                    "pageSize", pageResult.getSize(),
                    "totalPages", pageResult.getTotalPages(),
                    "totalElements", pageResult.getTotalElements(),
                    "content", List.copyOf(pageResult.getContent())
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid query parameters", "details", e.getMessage()));
        }
    }

    @Operation(summary = "Get count of available units")
    @GetMapping("/units/available/count")
    public ResponseEntity<?> availableCount() {
        try {
            long count = unitService.countAvailable();
            return ResponseEntity.ok(Map.of("availableUnits", count));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch available count", "details", e.getMessage()));
        }
    }

    @Operation(summary = "Add a new unit")
    @PostMapping("/units")
    public ResponseEntity<?> addUnit(@RequestBody UnitDto dto) {
        try {
            // Default values
            if (dto.getBookingMarkupPercent() == null) {
                dto.setBookingMarkupPercent(BigDecimal.valueOf(15));
            }
            if (dto.getIsAvailable() == null) {
                dto.setIsAvailable(true);
            }

            Unit saved = unitService.save(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create unit", "details", e.getMessage()));
        }
    }
}
