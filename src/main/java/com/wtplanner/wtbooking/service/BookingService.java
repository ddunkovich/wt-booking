/*
 * © 2025 Danil Dunkovich. All rights reserved.
 * Provided for evaluation purposes only.
 * Any commercial use, distribution, or modification requires explicit permission.
 */

package com.wtplanner.wtbooking.service;

import com.wtplanner.wtbooking.model.dict.BookingStatus;
import com.wtplanner.wtbooking.model.entity.Booking;
import com.wtplanner.wtbooking.model.entity.Unit;
import com.wtplanner.wtbooking.model.entity.User;
import com.wtplanner.wtbooking.remote.repository.BookingRepository;
import com.wtplanner.wtbooking.remote.repository.UnitRepository;
import com.wtplanner.wtbooking.remote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepo;
    private final UnitRepository unitRepo;
    private final UserRepository userRepo;
    private final PaymentService paymentService;
    private final CacheService cacheService;

    @Transactional
    public Booking book(UUID userId, UUID unitId, LocalDate start, LocalDate end) {
        log.info("Booking request: user={}, unit={}, start={}, end={}", userId, unitId, start, end);

        Unit unit = unitRepo.findById(unitId).orElseThrow(() -> new IllegalStateException("Unit not found"));
        if (!unit.isAvailable()) {
            log.warn("Unit {} not available", unitId);
            throw new IllegalStateException("Unit not available");
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));

        boolean busy = bookingRepo
                .findByUnitIdAndStatusAndEndDateGreaterThanEqual(unitId, BookingStatus.CREATED, LocalDate.now())
                .stream()
                .anyMatch(b -> !(end.isBefore(b.getStartDate()) || start.isAfter(b.getEndDate())));

        if (busy) {
            log.warn("Unit {} already booked for requested dates", unitId);
            throw new IllegalStateException("Already booked");
        }

        BigDecimal total = calcTotal(unit, start, end);
        Booking booking = Booking.builder()
                .unit(unit)
                .user(user)
                .startDate(start)
                .endDate(end)
                .totalCost(total)
                .status(BookingStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Booking saved = bookingRepo.save(booking);
        log.info("Booking created: {}", saved.getId());

        unit.setAvailable(false);
        unitRepo.save(unit);
        log.info("Unit {} marked unavailable", unitId);

        cacheService.adjustCount(-1);
        log.info("Cache updated: -1 unit available");

        return saved;
    }

    @Transactional
    public Booking markAsPaid(UUID bookingId) {
        log.info("Marking booking {} as paid", bookingId);

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CREATED) {
            log.warn("Booking {} cannot be paid again, status={}", bookingId, booking.getStatus());
            throw new IllegalStateException("Booking cannot be paid again");
        }

        boolean paid = paymentService.processPayment("pay-" + booking.getId(),
                booking.getTotalCost().multiply(BigDecimal.valueOf(100)).longValue());

        if (!paid) {
            log.error("Payment failed for booking {}", bookingId);
            throw new IllegalStateException("Payment failed");
        }

        booking.setStatus(BookingStatus.PAID);
        Booking updated = bookingRepo.save(booking);
        log.info("Booking {} marked as PAID", bookingId);

        return updated;
    }

    @Transactional
    public Booking cancel(UUID bookingId) {
        log.info("Cancelling booking {}", bookingId);

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (booking.getStatus() == BookingStatus.PAID) {
            log.warn("Paid booking {} cannot be cancelled", bookingId);
            throw new IllegalStateException("Paid booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Unit unit = booking.getUnit();
        unit.setAvailable(true);
        unitRepo.save(unit);

        cacheService.adjustCount(+1);
        log.info("Booking {} cancelled, unit {} available, cache updated +1", bookingId, unit.getId());

        return bookingRepo.save(booking);
    }

    private BigDecimal calcTotal(Unit unit, LocalDate start, LocalDate end) {
        long nights = end.toEpochDay() - start.toEpochDay() + 1;
        BigDecimal base = unit.getCost().multiply(BigDecimal.valueOf(nights));
        BigDecimal markup = base.multiply(
                unit.getBookingMarkupPercent().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
        return base.add(markup);
    }

}
