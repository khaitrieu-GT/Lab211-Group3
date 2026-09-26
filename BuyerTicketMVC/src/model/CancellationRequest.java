package model;

import java.time.LocalDateTime;

public class CancellationRequest {
    private Long id;
    private String reason;
    private LocalDateTime requestDate;
    private String status;
    private Ticket ticket;

    public CancellationRequest(Long id, Ticket ticket, String reason) {
        this.id = id;
        this.ticket = ticket;
        this.reason = reason;
        this.requestDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    public void submitRequest() { status = "PENDING"; }
    public void approveRequest() {
        status = "APPROVED";
        if (ticket != null) ticket.cancelTicket();
    }
    public void rejectRequest() { status = "REJECTED"; }
    public Long getId() { return id; }
    public String getReason() { return reason; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
    public Ticket getTicket() { return ticket; }
}
