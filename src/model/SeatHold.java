package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import model.enums.SeatHoldStatus;
import util.AppConfig;
import util.CsvUtil;

/**
 * Phien giu ghe tam thoi co thoi han (Trieu: SeatHold).
 */
public class SeatHold extends BaseEntity {
    private String ticketId;
    private String userId;
    private String bookingId;
    private String holdToken;
    private SeatHoldStatus status;
    private LocalDateTime heldAt;
    private LocalDateTime expiresAt;
    private int extendCount;

    public SeatHold() {
        super();
    }

    public SeatHold(String id, String ticketId, String userId, String bookingId, LocalDateTime expiresAt) {
        super(id);
        if (expiresAt == null || !expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Hold expiry must be in the future");
        }
        this.ticketId = ticketId;
        this.userId = userId;
        this.bookingId = bookingId;
        this.holdToken = UUID.randomUUID().toString();
        this.status = SeatHoldStatus.HOLD;
        this.heldAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.extendCount = 0;
    }

    /** Trieu: SeatHold.crateHold (sua ten thanh createHold). */
    public void createHold() {
        if (isExpired()) {
            throw new IllegalStateException("Cannot create an expired hold");
        }
        this.status = SeatHoldStatus.HOLD;
    }

    /** Trieu: SeatHold.extendHold. */
    public void extendHold() {
        if (this.status != SeatHoldStatus.HOLD || isExpired()) {
            throw new IllegalStateException("Only active HOLD can be extended");
        }
        if (this.extendCount >= AppConfig.MAX_HOLD_EXTENSIONS) {
            throw new IllegalStateException("Hold can only be extended " + AppConfig.MAX_HOLD_EXTENSIONS + " time(s)");
        }
        this.expiresAt = this.expiresAt.plusMinutes(AppConfig.HOLD_EXTEND_MINUTES);
        this.extendCount++;
    }

    /** Trieu: SeatHold.releaseHold. */
    public void releaseHold() {
        if (this.status == SeatHoldStatus.HOLD || this.status == SeatHoldStatus.EXPIRED) {
            this.status = SeatHoldStatus.RELEASED;
        }
    }

    /** Thanh toan thanh cong - phien giu chuyen thanh CONFIRMED. */
    public void confirmHold() {
        if (this.status != SeatHoldStatus.HOLD || isExpired()) {
            throw new IllegalStateException("Hold has expired, cannot confirm");
        }
        this.status = SeatHoldStatus.CONFIRMED;
    }

    /** Trieu: SeatHold.isExpired. */
    public boolean isExpired() {
        if (this.status == SeatHoldStatus.EXPIRED) {
            return true;
        }
        if (this.status == SeatHoldStatus.HOLD && LocalDateTime.now().isAfter(this.expiresAt)) {
            this.status = SeatHoldStatus.EXPIRED;
            return true;
        }
        return false;
    }

    public boolean isActive() {
        return this.status == SeatHoldStatus.HOLD && !isExpired();
    }

    public long getRemainingSeconds() {
        long seconds = Duration.between(LocalDateTime.now(), this.expiresAt).getSeconds();
        return Math.max(0, seconds);
    }

    public String getTicketId() { return this.ticketId; }
    public String getUserId() { return this.userId; }
    public String getBookingId() { return this.bookingId; }
    public String getHoldToken() { return this.holdToken; }
    public SeatHoldStatus getStatus() { return this.status; }
    public LocalDateTime getHeldAt() { return this.heldAt; }
    public LocalDateTime getExpiresAt() { return this.expiresAt; }
    public int getExtendCount() { return this.extendCount; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.ticketId, this.userId, this.bookingId, this.holdToken, this.status,
                this.heldAt, this.expiresAt, this.extendCount);
    }

    public static SeatHold fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 9) {
            return null;
        }
        SeatHold hold = new SeatHold();
        hold.setId(p[0]);
        hold.ticketId = p[1];
        hold.userId = p[2];
        hold.bookingId = p[3];
        hold.holdToken = p[4];
        hold.status = CsvUtil.toEnum(SeatHoldStatus.class, p[5]);
        hold.heldAt = CsvUtil.toDateTime(p[6]);
        hold.expiresAt = CsvUtil.toDateTime(p[7]);
        hold.extendCount = CsvUtil.toInt(p[8]);
        return hold;
    }
}
