package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private final Long id;
    private final Long bookingId;
    private BigDecimal amount;
    private final PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paidAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment(Long id, Long bookingId, BigDecimal amount, PaymentMethod method) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Payment amount must be non-negative");
        }

        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public void createPayment() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not PENDING");
        }
        updatedAt = LocalDateTime.now();
    }

    public boolean verifyPayment() {
        return status == PaymentStatus.SUCCESS;
    }

    public void updateStatus(PaymentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Payment status is required");
        }

        if (status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Refunded payment cannot change status");
        }

        status = newStatus;
        updatedAt = LocalDateTime.now();

        if (status == PaymentStatus.SUCCESS) {
            paidAt = updatedAt;
        }
    }

    public void refund() {
        if (status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only SUCCESS payment can be refunded");
        }

        status = PaymentStatus.REFUNDED;
        updatedAt = LocalDateTime.now();
    }
}
