package com.wtplanner.wtbooking.remote.repository;

import com.wtplanner.wtbooking.model.dict.BookingStatus;
import com.wtplanner.wtbooking.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUnitIdAndStatusAndEndDateGreaterThanEqual(UUID unitId, BookingStatus status, LocalDate date);
}
