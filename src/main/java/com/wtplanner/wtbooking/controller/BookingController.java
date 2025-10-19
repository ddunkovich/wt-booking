package com.wtplanner.wtbooking.controller;

import com.wtplanner.wtbooking.model.entity.Booking;
import com.wtplanner.wtbooking.service.BookingService;
import com.wtplanner.wtbooking.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/wtbooking")
public class BookingController {

    private final UnitService unitService;
    private final BookingService bookingService;

    public BookingController(UnitService unitService, BookingService bookingService) {
        this.unitService = unitService;
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create a new booking")
    @PostMapping("/bookings")
    public ResponseEntity<?> book(@RequestParam UUID unitId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            if (start == null || end == null || !end.isAfter(start)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid date range"));
            }
            Booking booking = bookingService.book(unitId, start, end);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create booking", "details", e.getMessage()));
        }
    }

    @Operation(summary = "Cancel booking by ID")
    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID id) {
        try {
            Booking result = bookingService.cancel(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Booking not found", "details", e.getMessage()));
        }
    }
}
