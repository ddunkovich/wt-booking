package com.wtplanner.wtbooking.model.entity;

import com.wtplanner.wtbooking.model.dict.AccommodationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/** Entity representing an accommodation unit available for booking. */
@Entity
@Table(name = "unit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "rooms", nullable = false)
    private int rooms;

    @Enumerated(EnumType.STRING)
    @Column(name = "accommodation_type", nullable = false)
    private AccommodationType accommodationType;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @Column(name = "cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "booking_markup_percent", nullable = false, precision = 10, scale = 2)
    private BigDecimal bookingMarkupPercent;

    @Column(name = "description")
    private String description;
}
