package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Seat {
    private final Long id;
    private final Long zoneId;
    private final String seatNumber;
    private SeatStatus status;
    public Long version;
    private final BigDecimal price;
    private boolean isActive;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Seat(Long id, Long zoneId, String seatNumber, BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Seat price must be non-negative");
        }
        this.id = id;
        this.zoneId = zoneId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
        this.version = 1L;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public void lock() {
        if (!isActive || status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat" + seatNumber + "is not available");
        }
        status = SeatStatus.LOCKED;
        version++;
        updatedAt = LocalDateTime.now();
    }

    public void unlock() {
        if (status != SeatStatus.LOCKED && status != SeatStatus.HOLD) {
            throw new IllegalStateException("Seat" + seatNumber + "cannot be unlocked from" + status);
        }
        status = SeatStatus.AVAILABLE;
        version++;
        updatedAt = LocalDateTime.now();
    }

    public void markAsSold() {
        if (status != SeatStatus.LOCKED && status != SeatStatus.HOLD) {
            throw new IllegalStateException("Seat" + seatNumber + "must be LOCKED/HOLD before SOLD");
        }
        status = SeatStatus.SOLD;
        version++;
        updatedAt = LocalDateTime.now();
    }

    public boolean isAvailable() {
        return isActive && status == SeatStatus.AVAILABLE;
    }
}
