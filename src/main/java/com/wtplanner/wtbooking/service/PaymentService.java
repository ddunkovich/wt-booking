package com.wtplanner.wtbooking.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    public boolean processPayment(String paymentReference, long amountCents) {
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        return true;
    }
}
