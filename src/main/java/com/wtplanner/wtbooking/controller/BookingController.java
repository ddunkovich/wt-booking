/*
 * © 2025 Danil Dunkovich. All rights reserved.
 * Provided for evaluation purposes only.
 * Any commercial use, distribution, or modification requires explicit permission.
 */

package com.wtplanner.wtbooking.controller;

import com.wtplanner.wtbooking.model.dto.BookingRequestDto;
import com.wtplanner.wtbooking.model.dto.BookingResponseDto;
import com.wtplanner.wtbooking.model.entity.Booking;
import com.wtplanner.wtbooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wtbooking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/bookings")
    @Operation(summary = "Create a new booking")
    public ResponseEntity<?> book(@RequestBody BookingRequestDto request) {
        try {
            if (request.getStart() == null || request.getEnd() == null || !request.getEnd().isAfter(request.getStart())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid date range"));
            }
            if (request.getUserId() == null || request.getUnitId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId and unitId are required"));
            }

            Booking booking = bookingService.book(
                    request.getUserId(),
                    request.getUnitId(),
                    request.getStart(),
                    request.getEnd()
            );

            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create booking", "details", e.getMessage()));
        }
    }

    @Operation(summary = "Simulate booking payment")
    @PostMapping("/bookings/{id}/pay")
    public ResponseEntity<?> pay(@PathVariable UUID id) {
        try {
            Booking b = bookingService.markAsPaid(id);
            BookingResponseDto result = new BookingResponseDto(
                    b.getId(),
                    b.getUnit().getId(),
                    b.getUser().getId(),
                    b.getStartDate(),
                    b.getEndDate(),
                    b.getTotalCost(),
                    b.getStatus()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Booking not found", "details", e.getMessage()));
        }
    }

    @Operation(summary = "Cancel booking by ID")
    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID id) {
        try {
            Booking b = bookingService.cancel(id);
            BookingResponseDto result = new BookingResponseDto(
                    b.getId(),
                    b.getUnit().getId(),
                    b.getUser().getId(),
                    b.getStartDate(),
                    b.getEndDate(),
                    b.getTotalCost(),
                    b.getStatus()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Booking not found", "details", e.getMessage()));
        }
    }
}
