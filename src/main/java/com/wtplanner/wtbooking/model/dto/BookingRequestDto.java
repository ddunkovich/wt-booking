package com.wtplanner.wtbooking.model.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingRequestDto {
    private UUID userId;
    private UUID unitId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate start;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate end;
}