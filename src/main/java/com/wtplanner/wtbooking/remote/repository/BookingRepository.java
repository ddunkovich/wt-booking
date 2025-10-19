package com.wtplanner.wtbooking.remote.repository;

import com.wtplanner.wtbooking.model.dict.BookingStatus;
import com.wtplanner.wtbooking.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUnitIdAndStatusAndEndDateGreaterThanEqual(UUID unitId, BookingStatus status, LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :threshold")
    List<Booking> findUnpaidBookings(@Param("status") BookingStatus status,
                                     @Param("threshold") LocalDateTime threshold);

}
