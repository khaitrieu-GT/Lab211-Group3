package repository;

import java.util.ArrayList;
import java.util.List;
import model.Payment;
import model.enums.PaymentStatus;

public class PaymentRepository extends CsvRepository<Payment> {

    public PaymentRepository() {
        super("payments.csv", "id,bookingId,amount,refundedAmount,method,status,transactionId,paidAt,createdAt,updatedAt", "PM", 4);
    }

    @Override
    protected Payment parse(String line) {
        return Payment.fromCsvLine(line);
    }

    public List<Payment> findByBookingId(String bookingId) {
        List<Payment> result = new ArrayList<>();
        for (Payment p : findAll()) {
            if (p.getBookingId().equalsIgnoreCase(bookingId)) {
                result.add(p);
            }
        }
        return result;
    }

    /** Thanh toan thanh cong (hoac da hoan) cua booking. */
    public Payment findPaidByBookingId(String bookingId) {
        for (Payment p : findByBookingId(bookingId)) {
            if (p.getStatus() == PaymentStatus.SUCCESS || p.getStatus() == PaymentStatus.REFUNDED) {
                return p;
            }
        }
        return null;
    }
}
