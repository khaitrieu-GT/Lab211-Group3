package com.tv4.booking.command;

import com.tv4.booking.model.Payment;

public class CreatePaymentCommand implements Command {

    private final Payment payment;

    public CreatePaymentCommand(Payment payment) {
        this.payment = payment;
    }

    @Override
    public void execute() {
        payment.createPayment();
    }
}