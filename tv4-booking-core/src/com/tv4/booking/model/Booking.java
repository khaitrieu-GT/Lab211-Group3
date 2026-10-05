package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Booking {
    private final Long id;
    private final Long userId;
    private final Long matchId;
    private BookingStatus status;
    private final BigDecimal totalAmount;
    private final LocalDateTime createAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    public Booking(Long id, Long userId, Long matchId, BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.signum() < 0) {
            throw new IllegalArgumentException("Total anount must be non-negative");
        }

        this.id = id;
        this.userId = userId;
        this.matchId = matchId;
        this.totalAmount = totalAmount;
        this.status = BookingStatus.PENDING;
        this.createAt = LocalDateTime.now();
        this.updatedAt = createAt;
    }

    public void create() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not in PENDING state");
        }
        updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING booking can be confirmed");
        }
        status = BookingStatus.CONFIRMED;
        expiresAt = null;
        updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking cannot be cancelled from" + status);
        }
        status = BookingStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
