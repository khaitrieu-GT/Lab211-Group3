package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.util.UUID;
import java.time.LocalDateTime;

public class SeatHold {
    private final Long id;
    private final Long seatId;
    private final Long userId;
    private final Long bookingId;
    private final String holdToken;
    private SeatHoldStatus status;
    private final LocalDateTime heldAt;
    private LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    public SeatHold(Long id, Long seatId, Long userId, Long bookingId, LocalDateTime expiresAt) {
        if (expiresAt == null || !expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Hold expiry must be in the future");
        }

        this.id = id;
        this.seatId = seatId;
        this.userId = userId;
        this.bookingId = bookingId;
        this.holdToken = UUID.randomUUID().toString();
        this.status = SeatHoldStatus.HOLD;
        this.heldAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.createdAt = heldAt;
    }

    public void crateHold() {
        if (isExpired()) {
            throw new IllegalStateException("Cannot create an expired hold");
        }
        status = SeatHoldStatus.HOLD;
    }

    public void extendHold() {
        if (status != SeatHoldStatus.HOLD || isExpired()) {
            throw new IllegalStateException("Only active HOLD can be extended");
        }
        expiresAt = expiresAt.plusMinutes(5);
    }

    public void releaseHold() {
        if (status == SeatHoldStatus.HOLD || status == SeatHoldStatus.EXPIRED) {
            status = SeatHoldStatus.RELEASED;
        }
    }

    public boolean isExpired() {
        if (status == SeatHoldStatus.EXPIRED) {
            return true;
        }

        if (LocalDateTime.now().isAfter(expiresAt)) {
            status = SeatHoldStatus.EXPIRED;
            return true;
        }

        return false;
    }
}
