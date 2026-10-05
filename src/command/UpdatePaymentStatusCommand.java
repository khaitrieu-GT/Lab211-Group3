package command;

import model.Payment;
import model.enums.PaymentStatus;

/** Cap nhat trang thai thanh toan (Trieu). */
public class UpdatePaymentStatusCommand implements Command {

    private final Payment payment;
    private final PaymentStatus newStatus;

    public UpdatePaymentStatusCommand(Payment payment, PaymentStatus newStatus) {
        this.payment = payment;
        this.newStatus = newStatus;
    }

    @Override
    public void execute() {
        this.payment.updateStatus(this.newStatus);
    }
}
