package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import model.enums.BookingSeatStatus;
import util.CsvUtil;
import util.MoneyUtil;

/**
 * Mot ghe trong booking - hop nhat BookingSeat (Trieu) va OrderItem (Khoa).
 */
public class BookingSeat extends BaseEntity {
    private String bookingId;
    private String ticketId;
    private String seatLabel;
    private String sectionId;
    private BigDecimal price;
    private BookingSeatStatus status;
    private LocalDateTime createdAt;

    public BookingSeat() {
        super();
    }

    public BookingSeat(String id, String bookingId, String ticketId, String seatLabel, String sectionId, BigDecimal price) {
        super(id);
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Seat price must be non-negative");
        }
        this.bookingId = bookingId;
        this.ticketId = ticketId;
        this.seatLabel = seatLabel;
        this.sectionId = sectionId;
        this.price = price;
        this.status = BookingSeatStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /** Trieu: BookingSeat.lockSeat. */
    public void lockSeat() {
        if (this.status != BookingSeatStatus.PENDING) {
            throw new IllegalStateException("Only PENDING seat can be locked");
        }
        this.status = BookingSeatStatus.HOLD;
    }

    /** Trieu: BookingSeat.releaseSeat. */
    public void releaseSeat() {
        if (this.status != BookingSeatStatus.PENDING && this.status != BookingSeatStatus.HOLD) {
            throw new IllegalStateException("Only PENDING/HOLD booking seat can be released");
        }
        this.status = BookingSeatStatus.CANCELLED;
    }

    /** Trieu: BookingSeat.markAsSold. */
    public void markAsSold() {
        if (this.status != BookingSeatStatus.HOLD) {
            throw new IllegalStateException("Only HOLD booking seat can be sold");
        }
        this.status = BookingSeatStatus.SOLD;
    }

    /** Huy ghe da ban khi yeu cau huy ve duoc duyet. */
    public void cancelSold() {
        if (this.status != BookingSeatStatus.SOLD) {
            throw new IllegalStateException("Only SOLD booking seat can be cancelled");
        }
        this.status = BookingSeatStatus.CANCELLED;
    }

    /** Khoa: OrderItem.calculateSubtotal (moi ghe so luong = 1). */
    public BigDecimal calculateSubtotal() {
        return this.status == BookingSeatStatus.CANCELLED ? BigDecimal.ZERO : this.price;
    }

    public boolean isActive() {
        return this.status != BookingSeatStatus.CANCELLED;
    }

    public String getBookingId() { return this.bookingId; }
    public String getTicketId() { return this.ticketId; }
    public String getSeatLabel() { return this.seatLabel; }
    public String getSectionId() { return this.sectionId; }
    public BigDecimal getPrice() { return this.price; }
    public BookingSeatStatus getStatus() { return this.status; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.bookingId, this.ticketId, this.seatLabel, this.sectionId, this.price,
                this.status, this.createdAt);
    }

    public static BookingSeat fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 8) {
            return null;
        }
        BookingSeat item = new BookingSeat(p[0], p[1], p[2], p[3], p[4], CsvUtil.toMoney(p[5]));
        item.status = CsvUtil.toEnum(BookingSeatStatus.class, p[6]);
        item.createdAt = CsvUtil.toDateTime(p[7]);
        return item;
    }

    @Override
    public String toString() {
        return String.format("%-7s | Khu %-6s | Ghe %-5s | %15s | %s",
                getId(), this.sectionId, this.seatLabel, MoneyUtil.format(this.price), this.status);
    }
}
