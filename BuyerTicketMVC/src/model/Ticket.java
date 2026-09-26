package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {
    private Long id;
    private String qrCode;
    private LocalDateTime issueDate;
    private String status;
    private Order order;

    public Ticket(Long id, Order order) {
        this.id = id;
        this.order = order;
        this.qrCode = UUID.randomUUID().toString();
        this.issueDate = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public boolean validateTicket() { return "ACTIVE".equals(status); }
    public void cancelTicket() { status = "CANCELLED"; }
    public Long getId() { return id; }
    public String getQrCode() { return qrCode; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public String getStatus() { return status; }
    public Order getOrder() { return order; }
}
