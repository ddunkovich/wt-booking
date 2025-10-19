package com.wtplanner.wtbooking.mapper;

import com.wtplanner.wtbooking.model.dto.UnitDto;
import com.wtplanner.wtbooking.model.entity.Unit;

import java.math.BigDecimal;

/** Manual mapper between Unit entity and UnitDto. */
public class UnitMapper {

    public static Unit toEntity(UnitDto dto) {
        if (dto == null) return null;

        return Unit.builder()
                .rooms(dto.getRooms())
                .accommodationType(dto.getAccommodationType())
                .floor(dto.getFloor())
                .isAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true)
                .cost(dto.getCost() != null ? dto.getCost() : BigDecimal.ZERO)
                .bookingMarkupPercent(dto.getBookingMarkupPercent() != null ? dto.getBookingMarkupPercent() : BigDecimal.valueOf(15))
                .description(dto.getDescription())
                .build();
    }

    public static UnitDto toDto(Unit entity) {
        if (entity == null) return null;

        return UnitDto.builder()
                .rooms(entity.getRooms())
                .accommodationType(entity.getAccommodationType())
                .floor(entity.getFloor())
                .isAvailable(entity.isAvailable())
                .cost(entity.getCost())
                .bookingMarkupPercent(entity.getBookingMarkupPercent())
                .description(entity.getDescription())
                .build();
    }
}
