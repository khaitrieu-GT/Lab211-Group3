package model;

import java.time.LocalDateTime;
import model.enums.SeatStatus;
import util.CsvUtil;

/**
 * Ghe vat ly trong mot khu vuc - hop nhat Seat cua Nhat, Khanh, Khoa, Trieu.
 * Trang thai ban ve theo tran (giu cho / da ban) nam o {@link Ticket}.
 */
public class Seat extends BaseEntity {
    private String sectionId;
    private String rowNumber;
    private int seatNumber;
    private SeatStatus status;
    private int version;            // Optimistic locking (Nhat, Khanh, Trieu)
    private LocalDateTime updatedAt;

    public Seat() {
        super();
    }

    public Seat(String id, String sectionId, String rowNumber, int seatNumber, SeatStatus status, int version) {
        super(id);
        this.sectionId = sectionId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.status = status == null ? SeatStatus.AVAILABLE : status;
        this.version = version;
        this.updatedAt = LocalDateTime.now();
    }

    /** Admin khoa ghe (Trieu: Seat.lock). */
    public void lock() {
        if (this.status == SeatStatus.LOCKED) {
            throw new IllegalStateException("Seat " + getLabel() + " is already locked");
        }
        updateStatus(SeatStatus.LOCKED);
    }

    /** Admin mo khoa ghe (Trieu: Seat.unlock). */
    public void unlock() {
        if (this.status == SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + getLabel() + " is already available");
        }
        updateStatus(SeatStatus.AVAILABLE);
    }

    /** Khanh: Seat.updateStatus - doi trang thai va tang version. */
    public void updateStatus(SeatStatus status) {
        this.status = status;
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }

    /** Khoa + Trieu: isAvailable. */
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    /** Nhan hien thi ghe, vi du "A5". */
    public String getLabel() {
        return this.rowNumber + this.seatNumber;
    }

    public String getSectionId() { return this.sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getRowNumber() { return this.rowNumber; }
    public void setRowNumber(String rowNumber) { this.rowNumber = rowNumber; }

    public int getSeatNumber() { return this.seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public SeatStatus getStatus() { return this.status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public int getVersion() { return this.version; }
    public void setVersion(int version) { this.version = version; }

    public LocalDateTime getUpdatedAt() { return this.updatedAt; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.sectionId, this.rowNumber, this.seatNumber, this.status, this.version);
    }

    public static Seat fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 6) {
            return null;
        }
        return new Seat(p[0], p[1], p[2], CsvUtil.toInt(p[3]), CsvUtil.toEnum(SeatStatus.class, p[4]),
                CsvUtil.toInt(p[5]));
    }

    @Override
    public String toString() {
        return String.format("%-8s | Khu: %-6s | Ghe %-4s | %-11s | v%d",
                getId(), this.sectionId, getLabel(), this.status, this.version);
    }
}
