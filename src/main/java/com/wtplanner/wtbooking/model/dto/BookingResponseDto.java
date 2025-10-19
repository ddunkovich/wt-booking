package com.wtplanner.wtbooking.model.dto;

import com.wtplanner.wtbooking.model.dict.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BookingResponseDto {
    private UUID id;
    private UUID unitId;
    private UUID userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalCost;
    private BookingStatus status;
}
