package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.enums.BookingStatus;
import util.AppConfig;
import util.CsvUtil;
import util.MoneyUtil;

/**
 * Don dat ve - hop nhat Booking (Trieu), Order (Khoa) va BookingTransaction (Khanh).
 * Danh sach ghe (items) duoc repository/controller nap vao khi can.
 */
public class Booking extends BaseEntity {
    private String userId;
    private String matchId;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private List<BookingSeat> items = new ArrayList<>();

    public Booking() {
        super();
    }

    public Booking(String id, String userId, String matchId) {
        super(id);
        this.userId = userId;
        this.matchId = matchId;
        this.totalAmount = BigDecimal.ZERO;
        this.status = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /** Trieu: Booking.create. */
    public void create() {
        if (this.status != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not in PENDING state");
        }
        this.updatedAt = LocalDateTime.now();
    }

    /** Trieu: Booking.confirm / Khoa: Order.confirmOrder. */
    public void confirm() {
        if (this.status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING booking can be confirmed");
        }
        if (getActiveItems().isEmpty()) {
            throw new IllegalStateException("Booking has no seat");
        }
        this.status = BookingStatus.CONFIRMED;
        this.expiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    /** Trieu: Booking.cancel / Khoa: Order.cancelOrder. */
    public void cancel() {
        if (this.status != BookingStatus.PENDING && this.status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking cannot be cancelled from " + this.status);
        }
        this.status = BookingStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /** Het thoi gian giu ghe ma chua thanh toan. */
    public void expire() {
        if (this.status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING booking can expire");
        }
        this.status = BookingStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /** Khoa: Order.addItem + Khanh: toi da 4 ve moi giao dich. */
    public void addItem(BookingSeat item) {
        if (item == null) {
            return;
        }
        if (this.status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING booking can add seats");
        }
        if (getActiveItems().size() >= AppConfig.MAX_TICKETS_PER_BOOKING) {
            throw new IllegalStateException("A booking can contain maximum "
                    + AppConfig.MAX_TICKETS_PER_BOOKING + " tickets");
        }
        this.items.add(item);
        calculateTotal();
    }

    /** Khoa: Order.calculateTotal. */
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (BookingSeat item : this.items) {
            total = total.add(item.calculateSubtotal());
        }
        this.totalAmount = total;
        this.updatedAt = LocalDateTime.now();
        return total;
    }

    public List<BookingSeat> getActiveItems() {
        List<BookingSeat> result = new ArrayList<>();
        for (BookingSeat item : this.items) {
            if (item.isActive()) {
                result.add(item);
            }
        }
        return result;
    }

    public boolean canAddMoreSeats() {
        return getActiveItems().size() < AppConfig.MAX_TICKETS_PER_BOOKING;
    }

    public boolean isPending() {
        return this.status == BookingStatus.PENDING;
    }

    public String getUserId() { return this.userId; }
    public String getMatchId() { return this.matchId; }
    public BookingStatus getStatus() { return this.status; }
    public BigDecimal getTotalAmount() { return this.totalAmount; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public LocalDateTime getUpdatedAt() { return this.updatedAt; }

    public LocalDateTime getExpiresAt() { return this.expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public List<BookingSeat> getItems() { return this.items; }
    public void setItems(List<BookingSeat> items) { this.items = items; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.userId, this.matchId, this.status, this.totalAmount,
                this.createdAt, this.updatedAt, this.expiresAt);
    }

    public static Booking fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 8) {
            return null;
        }
        Booking booking = new Booking(p[0], p[1], p[2]);
        booking.status = CsvUtil.toEnum(BookingStatus.class, p[3]);
        booking.totalAmount = CsvUtil.toMoney(p[4]);
        booking.createdAt = CsvUtil.toDateTime(p[5]);
        booking.updatedAt = CsvUtil.toDateTime(p[6]);
        booking.expiresAt = CsvUtil.toDateTime(p[7]);
        return booking;
    }

    @Override
    public String toString() {
        return String.format("%-7s | User: %-5s | Tran: %-5s | %15s | %-9s | %s",
                getId(), this.userId, this.matchId, MoneyUtil.format(this.totalAmount), this.status,
                this.createdAt == null ? "" : this.createdAt.withNano(0));
    }
}
