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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pure Mockito JUnit5 tests for BookingService */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepo;
    @Mock
    private UnitRepository unitRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private PaymentService paymentService;
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private BookingService bookingService;

    /* ---------- helpers ---------- */
    private static Unit sampleUnit(UUID id, boolean available, BigDecimal cost, BigDecimal markup) {
        Unit u = new Unit();
        u.setId(id);
        u.setAvailable(available);
        u.setCost(cost);
        u.setBookingMarkupPercent(markup);
        return u;
    }

    private static User sampleUser(UUID id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user-" + id.toString().substring(0, 6));
        u.setEmail("u@example.com");
        return u;
    }

    private static Booking sampleBooking(UUID id, Unit unit, User user, LocalDate start, LocalDate end, BookingStatus status) {
        Booking b = new Booking();
        b.setId(id);
        b.setUnit(unit);
        b.setUser(user);
        b.setStartDate(start);
        b.setEndDate(end);
        b.setTotalCost(BigDecimal.TEN);
        b.setStatus(status);
        b.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        return b;
    }

    /* ---------- book() ---------- */

    @Nested
    @DisplayName("book() method tests")
    class BookTests {

        static Stream<Arguments> validBookingArgs() {
            UUID unitId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            LocalDate start = LocalDate.of(2025, 10, 20);
            LocalDate end = LocalDate.of(2025, 10, 22);
            return Stream.of(Arguments.of(userId, unitId, start, end));
        }

        @ParameterizedTest
        @MethodSource("validBookingArgs")
        @DisplayName("Should create booking, mark unit unavailable, and update cache")
        void book_success(UUID userId, UUID unitId, LocalDate start, LocalDate end) {
            Unit unit = sampleUnit(unitId, true, BigDecimal.valueOf(100), BigDecimal.valueOf(10));
            User user = sampleUser(userId);

            when(unitRepo.findById(unitId)).thenReturn(Optional.of(unit));
            when(userRepo.findById(userId)).thenReturn(Optional.of(user));
            when(bookingRepo.findByUnitIdAndStatusAndEndDateGreaterThanEqual(eq(unitId), eq(BookingStatus.CREATED), any()))
                    .thenReturn(Collections.emptyList());
            when(bookingRepo.save(any())).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                b.setId(UUID.randomUUID());
                return b;
            });

            Booking result = bookingService.book(userId, unitId, start, end);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.CREATED);
            verify(unitRepo).save(unit);
            verify(cacheService).adjustCount(-1);
            assertThat(unit.isAvailable()).isFalse();
        }

        static Stream<Arguments> failureCases() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();
            LocalDate start = LocalDate.of(2025, 10, 20);
            LocalDate end = start.plusDays(1);
            return Stream.of(
                    Arguments.of(null, u2, start, end, "User not found"),
                    Arguments.of(u1, null, start, end, "Unit not found"),
                    Arguments.of(u1, u2, start, end, "Already booked")
            );
        }

        @ParameterizedTest
        @MethodSource("failureCases")
        @DisplayName("Should throw exceptions for invalid booking cases")
        void book_failures(UUID userId, UUID unitId, LocalDate start, LocalDate end, String expected) {
            if (unitId == null) {
                when(unitRepo.findById(any())).thenReturn(Optional.empty());
            } else {
                when(unitRepo.findById(unitId))
                        .thenReturn(Optional.of(sampleUnit(unitId, true, BigDecimal.valueOf(100), BigDecimal.TEN)));
            }

            if (userId == null) {
                when(userRepo.findById(any())).thenReturn(Optional.empty());
            } else {
                lenient().when(userRepo.findById(userId))
                        .thenReturn(Optional.of(sampleUser(userId)));
            }

            // force overlap if both IDs non-null
            if (userId != null && unitId != null) {
                when(bookingRepo.findByUnitIdAndStatusAndEndDateGreaterThanEqual(eq(unitId),
                        eq(BookingStatus.CREATED), any()))
                        .thenReturn(List.of(sampleBooking(UUID.randomUUID(),
                                sampleUnit(unitId, true, BigDecimal.TEN, BigDecimal.ONE),
                                sampleUser(userId), start, end, BookingStatus.CREATED)));
            }

            Throwable thrown = catchThrowable(() -> bookingService.book(userId, unitId, start, end));
            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            assertThat(thrown).hasMessageContaining(expected);
        }
    }

    /* ---------- markAsPaid() ---------- */

    @Nested
    class MarkAsPaidTests {
        static Stream<Arguments> payCases() {
            return Stream.of(
                    Arguments.of(BookingStatus.CREATED, true, null),
                    Arguments.of(BookingStatus.PAID, true, "Booking cannot be paid again"),
                    Arguments.of(BookingStatus.CREATED, false, "Payment failed")
            );
        }

        @ParameterizedTest
        @MethodSource("payCases")
        void markAsPaid_cases(BookingStatus status, boolean paySuccess, String expectedMsg) {
            UUID bid = UUID.randomUUID();
            Unit unit = sampleUnit(UUID.randomUUID(), false, BigDecimal.valueOf(100), BigDecimal.TEN);
            User user = sampleUser(UUID.randomUUID());
            Booking booking = sampleBooking(bid, unit, user, LocalDate.now(), LocalDate.now().plusDays(1), status);
            booking.setTotalCost(BigDecimal.valueOf(200));

            when(bookingRepo.findById(bid)).thenReturn(Optional.of(booking));
            lenient().when(paymentService.processPayment(anyString(), anyLong())).thenReturn(paySuccess);
            lenient().when(bookingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            if (status == BookingStatus.CREATED && paySuccess) {
                Booking result = bookingService.markAsPaid(bid);
                assertThat(result.getStatus()).isEqualTo(BookingStatus.PAID);
                verify(bookingRepo).save(booking);
            } else {
                Throwable t = catchThrowable(() -> bookingService.markAsPaid(bid));
                assertThat(t).isInstanceOf(IllegalStateException.class);
                if (expectedMsg != null) assertThat(t).hasMessageContaining(expectedMsg);
            }
        }
    }

    /* ---------- cancel() ---------- */

    @Nested
    class CancelTests {
        static Stream<Arguments> cancelCases() {
            return Stream.of(
                    Arguments.of(BookingStatus.CREATED, true, null),
                    Arguments.of(BookingStatus.PAID, false, "Paid booking cannot be cancelled")
            );
        }

        @ParameterizedTest
        @MethodSource("cancelCases")
        void cancel_cases(BookingStatus status, boolean shouldCancel, String expected) {
            UUID bid = UUID.randomUUID();
            Unit unit = sampleUnit(UUID.randomUUID(), false, BigDecimal.valueOf(50), BigDecimal.valueOf(10));
            User user = sampleUser(UUID.randomUUID());
            Booking booking = sampleBooking(bid, unit, user, LocalDate.now(), LocalDate.now().plusDays(1), status);

            when(bookingRepo.findById(bid)).thenReturn(Optional.of(booking));
            lenient().when(bookingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(unitRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            if (shouldCancel) {
                Booking result = bookingService.cancel(bid);
                assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
                assertThat(unit.isAvailable()).isTrue();
                verify(cacheService).adjustCount(+1);
            } else {
                Throwable t = catchThrowable(() -> bookingService.cancel(bid));
                assertThat(t).isInstanceOf(IllegalStateException.class);
                if (expected != null) assertThat(t).hasMessageContaining(expected);
                verify(cacheService, never()).adjustCount(anyInt());
            }
        }
    }
}
