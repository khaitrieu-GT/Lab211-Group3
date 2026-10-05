package controller;

import command.CreatePaymentCommand;
import command.ProcessTransactionCommand;
import command.RefundPaymentCommand;
import command.UpdatePaymentStatusCommand;
import command.VerifyTransactionCommand;
import java.math.BigDecimal;
import java.util.List;
import model.Booking;
import model.Payment;
import model.Transaction;
import model.enums.PaymentMethod;
import model.enums.PaymentStatus;
import repository.PaymentRepository;
import repository.TransactionRepository;
import service.PaymentGateway;

/**
 * Thanh toan va hoan tien (Trieu: buoc 9-10 trong Main - Payment, Transaction).
 */
public class PaymentController {

    private final PaymentRepository paymentRepo = new PaymentRepository();
    private final TransactionRepository transactionRepo = new TransactionRepository();
    private final PaymentGateway gateway = new PaymentGateway();

    /**
     * Tao thanh toan, gui qua cong thanh toan, xac minh giao dich.
     * Tra ve Payment voi trang thai SUCCESS hoac FAILED.
     */
    public Payment processPayment(Booking booking, PaymentMethod method, boolean approvedByGateway) {
        Payment payment = new Payment(null, booking.getId(), booking.getTotalAmount(), method);
        new CreatePaymentCommand(payment).execute();
        this.paymentRepo.insert(payment);

        Transaction transaction = this.gateway.charge(payment, approvedByGateway);
        new ProcessTransactionCommand(transaction).execute();
        this.transactionRepo.insert(transaction);
        payment.setTransactionId(transaction.getTransactionNo());

        try {
            new VerifyTransactionCommand(transaction).execute();
            new UpdatePaymentStatusCommand(payment, PaymentStatus.SUCCESS).execute();
        } catch (IllegalStateException ex) {
            new UpdatePaymentStatusCommand(payment, PaymentStatus.FAILED).execute();
        }
        this.paymentRepo.update(payment);
        return payment;
    }

    /**
     * Hoan tien cho booking. amount = null nghia la hoan toan bo phan con lai.
     * Tra ve null neu booking khong co thanh toan thanh cong.
     */
    public Payment refund(String bookingId, BigDecimal amount) {
        Payment payment = this.paymentRepo.findPaidByBookingId(bookingId);
        if (payment == null || payment.getStatus() != PaymentStatus.SUCCESS) {
            return null;
        }
        new RefundPaymentCommand(payment, amount).execute();
        this.paymentRepo.update(payment);
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            for (Transaction tx : this.transactionRepo.findByPaymentId(payment.getId())) {
                if (tx.getTransactionNo().equals(payment.getTransactionId())) {
                    tx.markRefunded();
                    this.transactionRepo.update(tx);
                }
            }
        }
        return payment;
    }

    public List<Payment> getPaymentsByBooking(String bookingId) {
        return this.paymentRepo.findByBookingId(bookingId);
    }

    public List<Payment> getAllPayments() {
        return this.paymentRepo.findAll();
    }

    public List<Transaction> getAllTransactions() {
        return this.transactionRepo.findAll();
    }
}
