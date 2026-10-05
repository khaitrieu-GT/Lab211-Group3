package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.time.LocalDateTime;

public class Ticket {
    private final Long id;
    private final Long bookingId;
    private final Long userId;
    private final Long matchId;
    private String qrCode;
    private final String seatInfo;
    private TicketStatus status;
    private final LocalDateTime createdAt;

    public Ticket(Long id, Long bookingId, Long userId, Long matchId, String seatInfo) {
        this.id = id;
        this.bookingId = bookingId;
        this.userId = userId;
        this.matchId = matchId;
        this.seatInfo = seatInfo;
        this.status = TicketStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public String generateQr() {
        if (qrCode == null) {
            qrCode = "TV4-TICKET-" + id + "-" + bookingId;
        }
        return qrCode;
    }

    public String viewTicket() {
        return "Ticket{id=" + id
                + ", bookingId=" + bookingId
                + ", userId=" + userId
                + ", matchId=" + matchId
                + ", seatInfo=" + seatInfo
                + ", status=" + status
                + ", qrCode=" + qrCode + "'}";
    }

    public void cancel() {
        if (status != TicketStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE ticket can be cancelled");
        }
        status = TicketStatus.CANCELLED;
    }
}
