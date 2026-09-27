package com.tv4.booking.command;

import com.tv4.booking.enums.PaymentStatus;
import com.tv4.booking.model.Payment;

public class UpdatePaymentStatusCommand implements Command {

    private final Payment payment;
    private final PaymentStatus newStatus;

    public UpdatePaymentStatusCommand(
            Payment payment,
            PaymentStatus newStatus
    ) {
        this.payment = payment;
        this.newStatus = newStatus;
    }

    @Override
    public void execute() {
        payment.updateStatus(newStatus);
    }
}