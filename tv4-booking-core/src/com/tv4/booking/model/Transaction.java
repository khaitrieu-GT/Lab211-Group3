package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.time.LocalDateTime;

public class Transaction {
    private final Long id;
    private final Long paymentId;
    private final String gateway;
    private final String transactionNo;
    private TransactionStatus status;
    private final String responseCode;
    private final LocalDateTime createdAt;

    public Transaction(Long id, Long paymentId, String gateway, String transactionNo, String responseCode) {
        this.id = id;
        this.paymentId = paymentId;
        this.gateway = gateway;
        this.transactionNo = transactionNo;
        this.responseCode = responseCode;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void process() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not PENDING");
        }
        status = "00".equals(responseCode) ? TransactionStatus.SUCCESS : TransactionStatus.FAILED;
    }

    public void verify() {
        if (status != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Transaction verification failed: " + status);
        }
    }

    public TransactionStatus getsStatus() {
        return status;
    }
}
