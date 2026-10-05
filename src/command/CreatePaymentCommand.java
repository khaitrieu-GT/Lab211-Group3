package command;

import model.Payment;

/** Tao thanh toan (Trieu). */
public class CreatePaymentCommand implements Command {

    private final Payment payment;

    public CreatePaymentCommand(Payment payment) {
        this.payment = payment;
    }

    @Override
    public void execute() {
        this.payment.createPayment();
    }
}