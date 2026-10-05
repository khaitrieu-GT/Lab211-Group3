package model;

import java.time.LocalDateTime;
import model.enums.TransactionStatus;
import util.AppConfig;
import util.CsvUtil;

/** Giao dich voi cong thanh toan (Trieu: Transaction). */
public class Transaction extends BaseEntity {
    private String paymentId;
    private String gateway;
    private String transactionNo;
    private TransactionStatus status;
    private String responseCode;
    private LocalDateTime createdAt;

    public Transaction() {
        super();
    }

    public Transaction(String id, String paymentId, String gateway, String transactionNo, String responseCode) {
        super(id);
        this.paymentId = paymentId;
        this.gateway = gateway;
        this.transactionNo = transactionNo;
        this.responseCode = responseCode;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /** Trieu: Transaction.process - ma "00" la thanh cong. */
    public void process() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not PENDING");
        }
        this.status = AppConfig.GATEWAY_SUCCESS_CODE.equals(this.responseCode)
                ? TransactionStatus.SUCCESS : TransactionStatus.FAILED;
    }

    /** Trieu: Transaction.verify. */
    public void verify() {
        if (this.status != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Transaction verification failed: " + this.status
                    + " (code " + this.responseCode + ")");
        }
    }

    public void markRefunded() {
        this.status = TransactionStatus.REFUNDED;
    }

    public String getPaymentId() { return this.paymentId; }
    public String getGateway() { return this.gateway; }
    public String getTransactionNo() { return this.transactionNo; }
    public String getResponseCode() { return this.responseCode; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }

    /** Trieu: getsStatus (sua ten thanh getStatus). */
    public TransactionStatus getStatus() { return this.status; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.paymentId, this.gateway, this.transactionNo, this.status,
                this.responseCode, this.createdAt);
    }

    public static Transaction fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 7) {
            return null;
        }
        Transaction tx = new Transaction(p[0], p[1], p[2], p[3], p[5]);
        tx.status = CsvUtil.toEnum(TransactionStatus.class, p[4]);
        tx.createdAt = CsvUtil.toDateTime(p[6]);
        return tx;
    }

    @Override
    public String toString() {
        return String.format("%-7s | Payment: %-7s | %-13s | So GD: %-22s | Ma: %-3s | %s",
                getId(), this.paymentId, this.gateway, this.transactionNo, this.responseCode, this.status);
    }
}
