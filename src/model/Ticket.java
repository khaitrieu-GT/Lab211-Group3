package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import model.enums.TicketStatus;
import util.CsvUtil;
import util.MoneyUtil;

/**
 * Ve mo ban cho mot ghe trong mot tran (Khanh: Ticket cua Seller).
 * Day la noi kiem soat Double Booking: trang thai AVAILABLE -> HOLD -> SOLD cung version
 * (gom cac ham lock/unlock/markAsSold tu Seat cua Trieu va select/unselect/sell cua Khoa).
 */
public class Ticket extends BaseEntity {
    private String matchId;
    private String seatId;
    private String sectionId;
    private String ticketType;
    private BigDecimal price;
    private TicketStatus status;
    private int version;
    private LocalDateTime updatedAt;

    private Seat seat;              // Tham chieu nap them de hien thi

    public Ticket() {
        super();
    }

    public Ticket(String id, String matchId, String seatId, String sectionId, String ticketType, BigDecimal price) {
        super(id);
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Ticket price must be non-negative");
        }
        this.matchId = matchId;
        this.seatId = seatId;
        this.sectionId = sectionId;
        this.ticketType = ticketType;
        this.price = price;
        this.status = TicketStatus.AVAILABLE;
        this.version = 1;
        this.updatedAt = LocalDateTime.now();
    }

    /** Khanh: Ticket.putOnSale - dua ve (da dung ban hoac vua duoc hoan) tro lai ban. */
    public void putOnSale() {
        if (this.status != TicketStatus.CANCELLED && this.status != TicketStatus.SOLD) {
            throw new IllegalStateException("Only CANCELLED/SOLD ticket can be put on sale again");
        }
        changeStatus(TicketStatus.AVAILABLE);
    }

    /** Khanh: Ticket.stopSale. */
    public void stopSale() {
        if (this.status != TicketStatus.AVAILABLE) {
            throw new IllegalStateException("Only AVAILABLE ticket can stop sale (current: " + this.status + ")");
        }
        changeStatus(TicketStatus.CANCELLED);
    }

    /** Trieu: Seat.lock - giu cho ghe cua tran. */
    public void lock() {
        if (this.status != TicketStatus.AVAILABLE) {
            throw new IllegalStateException("Seat ticket " + this.seatId + " is not available");
        }
        changeStatus(TicketStatus.HOLD);
    }

    /** Trieu: Seat.unlock - tra ghe khi bo chon / het han giu. */
    public void unlock() {
        if (this.status != TicketStatus.HOLD) {
            throw new IllegalStateException("Seat ticket " + this.seatId + " cannot be unlocked from " + this.status);
        }
        changeStatus(TicketStatus.AVAILABLE);
    }

    /** Trieu: Seat.markAsSold / Khoa: Seat.sell - thanh toan thanh cong HOLD -> SOLD. */
    public void markAsSold() {
        if (this.status != TicketStatus.HOLD) {
            throw new IllegalStateException("Seat ticket " + this.seatId + " must be HOLD before SOLD");
        }
        changeStatus(TicketStatus.SOLD);
    }

    /** Khoa + Trieu: isAvailable. */
    public boolean isAvailable() {
        return this.status == TicketStatus.AVAILABLE;
    }

    private void changeStatus(TicketStatus newStatus) {
        this.status = newStatus;
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }

    public String getMatchId() { return this.matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getSeatId() { return this.seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }

    public String getSectionId() { return this.sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getTicketType() { return this.ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public BigDecimal getPrice() { return this.price; }

    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Ticket price must be non-negative");
        }
        this.price = price;
    }

    public TicketStatus getStatus() { return this.status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public int getVersion() { return this.version; }
    public void setVersion(int version) { this.version = version; }

    public LocalDateTime getUpdatedAt() { return this.updatedAt; }

    public Seat getSeat() { return this.seat; }
    public void setSeat(Seat seat) { this.seat = seat; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.matchId, this.seatId, this.sectionId, this.ticketType, this.price,
                this.status, this.version, this.updatedAt);
    }

    public static Ticket fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 9) {
            return null;
        }
        Ticket ticket = new Ticket(p[0], p[1], p[2], p[3], p[4], CsvUtil.toMoney(p[5]));
        ticket.status = CsvUtil.toEnum(TicketStatus.class, p[6]);
        ticket.version = CsvUtil.toInt(p[7]);
        ticket.updatedAt = CsvUtil.toDateTime(p[8]);
        return ticket;
    }

    @Override
    public String toString() {
        String seatLabel = this.seat != null ? this.seat.getLabel() : this.seatId;
        return String.format("%-7s | Tran: %-5s | Khu: %-6s | Ghe %-5s | %-8s | %15s | %-9s",
                getId(), this.matchId, this.sectionId, seatLabel, this.ticketType, MoneyUtil.format(this.price), this.status);
    }
}
