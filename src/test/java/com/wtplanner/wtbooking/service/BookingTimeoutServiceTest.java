/*
 * © 2025 Danil Dunkovich. All rights reserved.
 * Provided for evaluation purposes only.
 * Any commercial use, distribution, or modification requires explicit permission.
 */

package com.wtplanner.wtbooking.service;

import com.wtplanner.wtbooking.model.dict.BookingStatus;
import com.wtplanner.wtbooking.model.entity.Booking;
import com.wtplanner.wtbooking.remote.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingTimeoutServiceTest {

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingTimeoutService bookingTimeoutService;

    @BeforeEach
    void init() {
        reset(bookingRepo, bookingService);
    }

    @Test
    @DisplayName("Should cancel expired bookings older than 15 minutes")
    void shouldCancelExpiredBookings() {
        UUID id = UUID.randomUUID();
        Booking expired = new Booking();
        expired.setId(id);
        expired.setStatus(BookingStatus.CREATED);

        when(bookingRepo.findUnpaidBookings(eq(BookingStatus.CREATED), any(LocalDateTime.class)))
                .thenReturn(List.of(expired));

        bookingTimeoutService.cancelExpiredBookings();

        verify(bookingRepo, times(1))
                .findUnpaidBookings(eq(BookingStatus.CREATED), any(LocalDateTime.class));
        verify(bookingService, times(1)).cancel(eq(id));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 10})
    @DisplayName("Should not cancel when there are no expired bookings")
    void shouldNotCancelWhenNoExpiredBookings(int minutesAgo) {
        when(bookingRepo.findUnpaidBookings(eq(BookingStatus.CREATED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        bookingTimeoutService.cancelExpiredBookings();

        verify(bookingRepo, times(1))
                .findUnpaidBookings(eq(BookingStatus.CREATED), any(LocalDateTime.class));
        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("Should cancel multiple expired bookings correctly")
    void shouldCancelMultipleExpiredBookings() {
        Booking b1 = new Booking();
        b1.setId(UUID.randomUUID());
        Booking b2 = new Booking();
        b2.setId(UUID.randomUUID());

        when(bookingRepo.findUnpaidBookings(eq(BookingStatus.CREATED), any(LocalDateTime.class)))
                .thenReturn(List.of(b1, b2));

        bookingTimeoutService.cancelExpiredBookings();

        verify(bookingRepo, times(1))
                .findUnpaidBookings(eq(BookingStatus.CREATED), any(LocalDateTime.class));
        verify(bookingService, times(1)).cancel(b1.getId());
        verify(bookingService, times(1)).cancel(b2.getId());
    }
}
