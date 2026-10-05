package model;

import java.time.LocalDateTime;
import util.CsvUtil;

/** Thong bao gui toi nguoi dung (Minh: Notification). */
public class Notification extends BaseEntity {
    private String userId;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;

    public Notification() {
        super();
    }

    public Notification(String notificationId, String userId, String message, LocalDateTime createdAt, boolean isRead) {
        super(notificationId);
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    /** Minh: sendNotification - danh dau thoi diem gui, trang thai chua doc. */
    public void sendNotification() {
        if (this.message == null || this.message.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification message is required");
        }
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    /** Minh: markAsRead. */
    public void markAsRead() {
        this.isRead = true;
    }

    public String getUserId() { return this.userId; }
    public String getMessage() { return this.message; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public boolean isRead() { return this.isRead; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.userId, this.message, this.createdAt, this.isRead);
    }

    public static Notification fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 5) {
            return null;
        }
        return new Notification(p[0], p[1], p[2], CsvUtil.toDateTime(p[3]), Boolean.parseBoolean(p[4]));
    }

    @Override
    public String toString() {
        return (this.isRead ? "   " : "[*]") + " " + (this.createdAt == null ? "" : this.createdAt.withNano(0))
                + " | " + this.message;
    }
}
