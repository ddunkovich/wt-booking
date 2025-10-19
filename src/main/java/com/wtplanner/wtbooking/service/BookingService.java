package com.wtplanner.wtbooking.service;

import com.wtplanner.wtbooking.model.dict.BookingStatus;
import com.wtplanner.wtbooking.model.entity.Booking;
import com.wtplanner.wtbooking.model.entity.Unit;
import com.wtplanner.wtbooking.remote.repository.BookingRepository;
import com.wtplanner.wtbooking.remote.repository.UnitRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final UnitRepository unitRepo;
    private final PaymentService paymentService;
    private final StatsService statsService;

    public BookingService(BookingRepository bookingRepo, UnitRepository unitRepo,
                          PaymentService paymentService, StatsService statsService) {
        this.bookingRepo = bookingRepo;
        this.unitRepo = unitRepo;
        this.paymentService = paymentService;
        this.statsService = statsService;
    }

    @Transactional
    @CacheEvict(value = {"availableCount", "unitsSearch"}, allEntries = true)
    public Booking book(UUID unitId, LocalDate start, LocalDate end) {
        Unit unit = unitRepo.findById(unitId).orElseThrow();

        if (!unit.isAvailable()) {
            throw new IllegalStateException("Unit not available");
        }

        // Check if there is an overlapping active booking
        boolean busy = bookingRepo
                .findByUnitIdAndStatusAndEndDateGreaterThanEqual(unitId, BookingStatus.CREATED, LocalDate.now())
                .stream()
                .anyMatch(b -> !(end.isBefore(b.getStartDate()) || start.isAfter(b.getEndDate())));

        if (busy) {
            throw new IllegalStateException("Already booked");
        }

        Booking booking = new Booking();
        booking.setUnit(unit);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus(BookingStatus.CREATED);

        long nights = end.toEpochDay() - start.toEpochDay() + 1;

        // All calculations use BigDecimal for precision
        BigDecimal base = unit.getCost().multiply(BigDecimal.valueOf(nights));
        BigDecimal markup = base.multiply(unit.getBookingMarkupPercent().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        BigDecimal total = base.add(markup);

        booking.setTotalCost(total);

        Booking saved = bookingRepo.save(booking);

        // Simulate payment
        boolean paid = paymentService.processPayment("pay-" + saved.getId(),
                total.multiply(BigDecimal.valueOf(100)).longValue());

        if (paid) {
            saved.setStatus(BookingStatus.PAID);
            bookingRepo.save(saved);
            unit.setAvailable(false);
            unitRepo.save(unit);
            statsService.recordBooking(total.doubleValue());
        }

        return saved;
    }

    @Transactional
    @CacheEvict(value = {"availableCount", "unitsSearch"}, allEntries = true)
    public Booking cancel(UUID bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElseThrow();
        b.setStatus(BookingStatus.CANCELLED);

        // Mark the unit as available after cancellation
        Unit u = b.getUnit();
        u.setAvailable(true);
        unitRepo.save(u);

        return bookingRepo.save(b);
    }

}
