package model;

import java.time.LocalDateTime;
import model.enums.CancellationStatus;
import util.CsvUtil;

/** Yeu cau huy ve cua Buyer, Admin duyet/tu choi (Khoa: CancellationRequest). */
public class CancellationRequest extends BaseEntity {
    private String eTicketId;
    private String userId;
    private String reason;
    private LocalDateTime requestDate;
    private CancellationStatus status;
    private String handledBy;
    private String note;

    public CancellationRequest() {
        super();
    }

    public CancellationRequest(String id, String eTicketId, String userId, String reason) {
        super(id);
        this.eTicketId = eTicketId;
        this.userId = userId;
        this.reason = reason;
        this.requestDate = LocalDateTime.now();
        this.status = CancellationStatus.PENDING;
    }

    /** Khoa: submitRequest. */
    public void submitRequest() {
        if (this.reason == null || this.reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        this.status = CancellationStatus.PENDING;
        this.requestDate = LocalDateTime.now();
    }

    /** Khoa: approveRequest (viec huy ve/hoan tien do CancellationController dieu phoi). */
    public void approveRequest(String adminId) {
        requirePending();
        this.status = CancellationStatus.APPROVED;
        this.handledBy = adminId;
    }

    /** Khoa: rejectRequest. */
    public void rejectRequest(String adminId, String note) {
        requirePending();
        this.status = CancellationStatus.REJECTED;
        this.handledBy = adminId;
        this.note = note;
    }

    public boolean isPending() {
        return this.status == CancellationStatus.PENDING;
    }

    private void requirePending() {
        if (!isPending()) {
            throw new IllegalStateException("Request has already been handled (" + this.status + ")");
        }
    }

    public String getETicketId() { return this.eTicketId; }
    public String getUserId() { return this.userId; }
    public String getReason() { return this.reason; }
    public LocalDateTime getRequestDate() { return this.requestDate; }
    public CancellationStatus getStatus() { return this.status; }
    public String getHandledBy() { return this.handledBy; }
    public String getNote() { return this.note; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.eTicketId, this.userId, this.reason, this.requestDate, this.status,
                this.handledBy, this.note);
    }

    public static CancellationRequest fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 8) {
            return null;
        }
        CancellationRequest request = new CancellationRequest(p[0], p[1], p[2], p[3]);
        request.requestDate = CsvUtil.toDateTime(p[4]);
        request.status = CsvUtil.toEnum(CancellationStatus.class, p[5]);
        request.handledBy = CsvUtil.text(p[6]);
        request.note = CsvUtil.text(p[7]);
        return request;
    }

    @Override
    public String toString() {
        return String.format("%-7s | Ve: %-7s | User: %-5s | %-9s | Ly do: %s%s",
                getId(), this.eTicketId, this.userId, this.status, this.reason,
                this.note == null ? "" : " | Ghi chu: " + this.note);
    }
}
