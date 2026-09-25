package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingSeat {
    private final Long id;
    private final Long bookingId;
    private final Long seatId;
    private final BigDecimal price;
    private BookingSeatStatus status;
    private final LocalDateTime createdAt;

    public BookingSeat(Long id, Long bookingId, Long seatId, BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Seat price must be non-negative");
        }
        this.id = id;
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.price = price;
        this.status = BookingSeatStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void lockSeat() {
        if (status != BookingSeatStatus.PENDING) {
            throw new IllegalStateException("Only PENDING seat can be locked");
        }
        status = BookingSeatStatus.HOLD;
    }

    public void releaseSeat() {
        if (status != BookingSeatStatus.PENDING && status != BookingSeatStatus.HOLD) {
            throw new IllegalStateException("Only PENDING/HOLD booking seat can be released");
        }
        status = BookingSeatStatus.CANCELLED;
    }

    public void markAsSold() {
        if (status != BookingSeatStatus.HOLD) {
            throw new IllegalStateException("Only HOLD booking can be sold");
        }
        status = BookingSeatStatus.SOLD;
    }
}
