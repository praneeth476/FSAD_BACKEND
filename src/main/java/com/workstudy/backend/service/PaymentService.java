package com.workstudy.backend.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PaymentService {

    public String processPayment(Double amount, Long studentId) {
        // MOCK IMPLEMENTATION: Simulates talking to Stripe/Razorpay
        System.out.println("Initiating Mock Payment to Student ID: " + studentId + " for amount: $" + amount);
        
        try {
            Thread.sleep(1000); // Simulate network latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String transactionId = "txn_mock_" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("Payment Successful. Transaction ID: " + transactionId);
        
        return transactionId;
    }
}
