package model;

import java.time.LocalDateTime;

public class Notification {
    private String notificationId;
    private String userId;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;

    
    public Notification(String notificationId, String userId, String message, LocalDateTime createdAt, boolean isRead) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public void sendNotification() {}
    public void markAsRead() {this.isRead = true;}

    public String toCsvLine() {
        return notificationId +","+ userId +","+ message +","+ createdAt +","+ isRead;
    }
}