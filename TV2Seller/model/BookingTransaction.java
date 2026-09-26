package TV2Seller.model;

import java.util.ArrayList;
import java.util.List;

public class BookingTransaction {

    private int transactionId;
    private int fanId;
    private int matchId;
    private double totalAmount;
    private String transactionDate;
    private String status;

    private List<Ticket> tickets = new ArrayList<>();

    public BookingTransaction() {
    }

    public BookingTransaction(int transactionId, int fanId,
                              int matchId, double totalAmount,
                              String transactionDate, String status) {

        this.transactionId = transactionId;
        this.fanId = fanId;
        this.matchId = matchId;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.status = status;
    }

    public String getOrderStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void addTicket(Ticket ticket) {

        if (tickets.size() >= 4) {
            System.out.println("A transaction can contain maximum 4 tickets.");
            return;
        }

        tickets.add(ticket);
        totalAmount += ticket.getPrice();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getFanId() {
        return fanId;
    }

    public void setFanId(int fanId) {
        this.fanId = fanId;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BookingTransaction{" +
                "transactionId=" + transactionId +
                ", fanId=" + fanId +
                ", matchId=" + matchId +
                ", totalAmount=" + totalAmount +
                ", transactionDate='" + transactionDate + '\'' +
                ", status='" + status + '\'' +
                ", ticketCount=" + tickets.size() +
                '}';
    }
}
