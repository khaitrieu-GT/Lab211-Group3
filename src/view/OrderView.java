package view;

import java.util.List;
import model.Booking;
import model.BookingSeat;
import model.Payment;
import model.SeatHold;
import util.MoneyUtil;

/** Hien thi booking / don hang (Khoa: OrderView). */
public class OrderView extends BaseView {

    /** Khoa: displayOrder. */
    public void displayOrder(Booking booking, List<SeatHold> holds) {
        printSection("BOOKING " + booking.getId());
        System.out.println("Tran       : " + booking.getMatchId());
        System.out.println("Trang thai : " + booking.getStatus());
        System.out.println("Ghe:");
        for (BookingSeat item : booking.getItems()) {
            if (item.isActive()) {
                System.out.println("  " + item);
            }
        }
        System.out.println("Tong tien  : " + MoneyUtil.format(booking.getTotalAmount()));
        if (booking.isPending() && booking.getExpiresAt() != null) {
            long seconds = 0;
            for (SeatHold hold : holds) {
                seconds = seconds == 0 ? hold.getRemainingSeconds() : Math.min(seconds, hold.getRemainingSeconds());
            }
            System.out.println("Giu ghe den: " + booking.getExpiresAt().withNano(0)
                    + String.format(" (con %d phut %02d giay)", seconds / 60, seconds % 60));
        }
    }

    /** Khoa: displayHistory. */
    public void displayHistory(List<Booking> bookings) {
        printSection("LICH SU DAT VE");
        if (bookings.isEmpty()) {
            System.out.println("Chua co lich su dat ve.");
            return;
        }
        for (Booking booking : bookings) {
            System.out.println(booking);
            for (BookingSeat item : booking.getItems()) {
                System.out.println("      - " + item);
            }
        }
    }

    public void displayPayment(Payment payment) {
        printSection("KET QUA THANH TOAN");
        System.out.println("Ma thanh toan : " + payment.getId());
        System.out.println("Phuong thuc   : " + payment.getMethod());
        System.out.println("So tien       : " + MoneyUtil.format(payment.getAmount()));
        System.out.println("Ma giao dich  : " + payment.getTransactionId());
        System.out.println("Trang thai    : " + payment.getStatus());
    }
}
