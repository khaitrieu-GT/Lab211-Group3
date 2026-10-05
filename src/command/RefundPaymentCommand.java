package command;

import java.math.BigDecimal;
import model.Payment;

/** Hoan tien (Trieu - file goc thieu duoi .java). amount = null nghia la hoan toan bo. */
public class RefundPaymentCommand implements Command {

    private final Payment payment;
    private final BigDecimal amount;

    public RefundPaymentCommand(Payment payment) {
        this(payment, null);
    }

    public RefundPaymentCommand(Payment payment, BigDecimal amount) {
        this.payment = payment;
        this.amount = amount;
    }

    @Override
    public void execute() {
        if (this.amount == null) {
            this.payment.refund();
        } else {
            this.payment.refund(this.amount);
        }
    }
}
