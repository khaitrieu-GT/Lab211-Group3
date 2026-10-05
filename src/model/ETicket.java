package model;

import java.time.LocalDateTime;
import model.enums.ETicketStatus;
import util.CsvUtil;

/**
 * Ve dien tu phat hanh sau khi thanh toan - hop nhat Ticket cua Khoa va Trieu.
 */
public class ETicket extends BaseEntity {
    private String bookingId;
    private String bookingSeatId;
    private String ticketId;
    private String userId;
    private String matchId;
    private String seatInfo;
    private String qrCode;
    private ETicketStatus status;
    private LocalDateTime issueDate;

    public ETicket() {
        super();
    }

    public ETicket(String id, String bookingId, String bookingSeatId, String ticketId,
                   String userId, String matchId, String seatInfo) {
        super(id);
        this.bookingId = bookingId;
        this.bookingSeatId = bookingSeatId;
        this.ticketId = ticketId;
        this.userId = userId;
        this.matchId = matchId;
        this.seatInfo = seatInfo;
        this.status = ETicketStatus.ACTIVE;
        this.issueDate = LocalDateTime.now();
    }

    /** Trieu: Ticket.generateQr. */
    public String generateQr() {
        if (this.qrCode == null) {
            this.qrCode = "TICKET-" + getId() + "-" + this.bookingId + "-"
                    + Integer.toHexString((getId() + this.bookingId + this.issueDate).hashCode()).toUpperCase();
        }
        return this.qrCode;
    }

    /** Trieu: Ticket.viewTicket. */
    public String viewTicket() {
        return "ETicket{id=" + getId() + ", bookingId=" + this.bookingId + ", userId=" + this.userId
                + ", matchId=" + this.matchId + ", seatInfo=" + this.seatInfo + ", status=" + this.status
                + ", qrCode=" + this.qrCode + "}";
    }

    /** Khoa: Ticket.validateTicket. */
    public boolean validateTicket() {
        return this.status == ETicketStatus.ACTIVE;
    }

    /** Soat ve vao cong: ACTIVE -> USED. */
    public void checkIn() {
        if (!validateTicket()) {
            throw new IllegalStateException("Ticket is not valid (" + this.status + ")");
        }
        this.status = ETicketStatus.USED;
    }

    /** Trieu: Ticket.cancel / Khoa: Ticket.cancelTicket. */
    public void cancel() {
        if (this.status != ETicketStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE ticket can be cancelled");
        }
        this.status = ETicketStatus.CANCELLED;
    }

    public String getBookingId() { return this.bookingId; }
    public String getBookingSeatId() { return this.bookingSeatId; }
    public String getTicketId() { return this.ticketId; }
    public String getUserId() { return this.userId; }
    public String getMatchId() { return this.matchId; }
    public String getSeatInfo() { return this.seatInfo; }
    public String getQrCode() { return this.qrCode; }
    public ETicketStatus getStatus() { return this.status; }
    public LocalDateTime getIssueDate() { return this.issueDate; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.bookingId, this.bookingSeatId, this.ticketId, this.userId, this.matchId,
                this.seatInfo, this.qrCode, this.status, this.issueDate);
    }

    public static ETicket fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 10) {
            return null;
        }
        ETicket ticket = new ETicket(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
        ticket.qrCode = CsvUtil.text(p[7]);
        ticket.status = CsvUtil.toEnum(ETicketStatus.class, p[8]);
        ticket.issueDate = CsvUtil.toDateTime(p[9]);
        return ticket;
    }

    @Override
    public String toString() {
        return String.format("%-7s | Booking: %-7s | Tran: %-5s | %-26s | %-9s | %s",
                getId(), this.bookingId, this.matchId, this.seatInfo, this.status, this.qrCode);
    }
}
