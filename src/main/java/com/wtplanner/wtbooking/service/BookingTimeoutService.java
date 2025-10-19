/*
 * © 2025 Danil Dunkovich. All rights reserved.
 * Provided for evaluation purposes only.
 * Any commercial use, distribution, or modification requires explicit permission.
 */

package com.wtplanner.wtbooking.service;

import com.wtplanner.wtbooking.model.dict.BookingStatus;
import com.wtplanner.wtbooking.model.entity.Booking;
import com.wtplanner.wtbooking.remote.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingTimeoutService {

    private final BookingRepository bookingRepo;
    private final BookingService bookingService;

    @Scheduled(fixedRate = 60_000) // every 1 minute
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Booking> pending = bookingRepo.findUnpaidBookings(BookingStatus.CREATED, threshold);
        for (Booking b : pending) {
            bookingService.cancel(b.getId());
        }
    }
}
