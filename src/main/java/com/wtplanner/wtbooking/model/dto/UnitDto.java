package com.wtplanner.wtbooking.model.dto;

import com.wtplanner.wtbooking.model.dict.AccommodationType;
import lombok.*;

import java.math.BigDecimal;

/** DTO used for creating or returning accommodation units. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitDto {

    private int rooms;
    private AccommodationType accommodationType;
    private int floor;
    private Boolean isAvailable;           // nullable to allow default handling
    private BigDecimal cost;
    private BigDecimal bookingMarkupPercent;
    private String description;
}