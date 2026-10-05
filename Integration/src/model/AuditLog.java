package model;

import java.time.LocalDateTime;

public class AuditLog {
    private String logId;
    private String actorUsername;
    private String action;
    private LocalDateTime timestamp;
    private String statusResult;

    
    public AuditLog(String logId, String actorUsername, String action, LocalDateTime timestamp, String statusResult) {
        this.logId = logId;
        this.actorUsername = actorUsername;
        this.action = action;
        this.timestamp = timestamp;
        this.statusResult = statusResult;
    }

    public void logAction() {}

    public String toCsvLine() {
        return logId +","+ actorUsername +","+ action +","+ timestamp +","+ statusResult;
    }

    public static AuditLog fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) return null;
        return new AuditLog(parts[0], parts[1], parts[2], LocalDateTime.parse(parts[3]), parts[4]);
    }
}