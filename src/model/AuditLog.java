package model;

import java.time.LocalDateTime;
import util.CsvUtil;

/** Nhat ky thao tac he thong (Minh: AuditLog). */
public class AuditLog extends BaseEntity {
    private String actorUsername;
    private String action;
    private LocalDateTime timestamp;
    private String statusResult;

    public AuditLog() {
        super();
    }

    public AuditLog(String logId, String actorUsername, String action, LocalDateTime timestamp, String statusResult) {
        super(logId);
        this.actorUsername = actorUsername;
        this.action = action;
        this.timestamp = timestamp;
        this.statusResult = statusResult;
    }

    /** Minh: logAction - ghi nhan thoi diem thuc hien thao tac. */
    public void logAction() {
        if (this.action == null || this.action.trim().isEmpty()) {
            throw new IllegalArgumentException("Audit action is required");
        }
        this.timestamp = LocalDateTime.now();
    }

    public String getActorUsername() { return this.actorUsername; }
    public String getAction() { return this.action; }
    public LocalDateTime getTimestamp() { return this.timestamp; }
    public String getStatusResult() { return this.statusResult; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.actorUsername, this.action, this.timestamp, this.statusResult);
    }

    public static AuditLog fromCsvLine(String line) {
        String[] p = CsvUtil.split(line);
        if (p.length < 5) {
            return null;
        }
        return new AuditLog(p[0], p[1], p[2], CsvUtil.toDateTime(p[3]), p[4]);
    }

    @Override
    public String toString() {
        return String.format("%-8s | %s | %-10s | %-7s | %s", getId(),
                this.timestamp == null ? "" : this.timestamp.withNano(0), this.actorUsername, this.statusResult, this.action);
    }
}
