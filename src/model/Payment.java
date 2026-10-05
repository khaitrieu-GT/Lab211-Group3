package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import model.enums.PaymentMethod;
import model.enums.PaymentStatus;
import util.CsvUtil;
import util.MoneyUtil;

/** Thanh toan cho mot booking (Trieu: Payment). */
public class Payment extends BaseEntity {
    private String bookingId;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
        super();
    }

    public Payment(String id, String bookingId, BigDecimal amount, PaymentMethod method) {
        super(id);
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Payment amount must be non-negative");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        this.bookingId = bookingId;
        this.amount = amount;
        this.refundedAmount = BigDecimal.ZERO;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /** Trieu: Payment.createPayment. */
    public void createPayment() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not PENDING");
        }
        this.updatedAt = LocalDateTime.now();
    }

    /** Trieu: Payment.verifyPayment. */
    public boolean verifyPayment() {
        return this.status == PaymentStatus.SUCCESS;
    }

    /** Trieu: Payment.updateStatus. */
    public void updateStatus(PaymentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Payment status is required");
        }
        if (this.status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Refunded payment cannot change status");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        if (this.status == PaymentStatus.SUCCESS) {
            this.paidAt = this.updatedAt;
        }
    }

    /** Trieu: Payment.refund - hoan toan bo so tien con lai. */
    public void refund() {
        refund(this.amount.subtract(this.refundedAmount));
    }

    /** Hoan mot phan (khi huy le tung ve trong booking). Hoan du thi chuyen REFUNDED. */
    public void refund(BigDecimal value) {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only SUCCESS payment can be refunded");
        }
        if (value == null || value.signum() <= 0 || this.refundedAmount.add(value).compareTo(this.amount) > 0) {
            throw new IllegalArgumentException("Invalid refund amount");
        }
        this.refundedAmount = this.refundedAmount.add(value);
        if (this.refundedAmount.compareTo(this.amount) == 0) {
            this.status = PaymentStatus.REFUNDED;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public String getBookingId() { return this.bookingId; }
    public BigDecimal getAmount() { return this.amount; }
    public BigDecimal getRefundedAmount() { return this.refundedAmount; }
    public PaymentMethod getMethod() { return this.method; }
    public PaymentStatus getStatus() { return this.status; }
    public LocalDateTime getPaidAt() { return this.paidAt; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }

    public String getTransactionId() { return this.transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.bookingId, this.amount, this.refundedAmount, this.method, this.status,
                this.transactionId, this.paidAt, this.createdAt, this.updatedAt);
    }

    public static Payment fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 10) {
            return null;
        }
        Payment payment = new Payment(p[0], p[1], CsvUtil.toMoney(p[2]), CsvUtil.toEnum(PaymentMethod.class, p[4]));
        payment.refundedAmount = CsvUtil.toMoney(p[3]);
        payment.status = CsvUtil.toEnum(PaymentStatus.class, p[5]);
        payment.transactionId = CsvUtil.text(p[6]);
        payment.paidAt = CsvUtil.toDateTime(p[7]);
        payment.createdAt = CsvUtil.toDateTime(p[8]);
        payment.updatedAt = CsvUtil.toDateTime(p[9]);
        return payment;
    }

    @Override
    public String toString() {
        return String.format("%-7s | Booking: %-7s | %15s | Hoan: %15s | %-13s | %-8s | GD: %s",
                getId(), this.bookingId, MoneyUtil.format(this.amount), MoneyUtil.format(this.refundedAmount),
                this.method, this.status, this.transactionId == null ? "-" : this.transactionId);
    }
}
