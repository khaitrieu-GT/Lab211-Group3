package service;

import dto.SalesReport;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Booking;
import model.Match;
import model.Payment;
import model.Ticket;
import model.enums.TicketStatus;
import repository.BookingRepository;
import repository.PaymentRepository;
import repository.TicketRepository;

/** Thong ke doanh thu theo tran. */
public class ReportService {

    private final TicketRepository ticketRepo = new TicketRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final PaymentRepository paymentRepo = new PaymentRepository();

    public SalesReport buildReport(List<Match> matches) {
        SalesReport report = new SalesReport();
        Set<String> matchIds = new HashSet<>();
        for (Match match : matches) {
            matchIds.add(match.getId());
            int total = 0;
            int sold = 0;
            int held = 0;
            BigDecimal revenue = BigDecimal.ZERO;
            for (Ticket ticket : this.ticketRepo.findByMatchId(match.getId())) {
                total++;
                if (ticket.getStatus() == TicketStatus.SOLD) {
                    sold++;
                    revenue = revenue.add(ticket.getPrice());
                } else if (ticket.getStatus() == TicketStatus.HOLD) {
                    held++;
                }
            }
            report.addLine(new SalesReport.Line(match.getId(), match.getName(), total, sold, held, revenue));
        }

        BigDecimal refunded = BigDecimal.ZERO;
        for (Booking booking : this.bookingRepo.findAll()) {
            if (!matchIds.contains(booking.getMatchId())) {
                continue;
            }
            for (Payment payment : this.paymentRepo.findByBookingId(booking.getId())) {
                refunded = refunded.add(payment.getRefundedAmount());
            }
        }
        report.setTotalRefunded(refunded);
        return report;
    }
}
