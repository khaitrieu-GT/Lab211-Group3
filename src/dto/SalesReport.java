package dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bao cao doanh thu theo tran (Admin: toan he thong, Seller: tran cua minh).
 */
public class SalesReport {

    /** Mot dong bao cao cho mot tran. */
    public static class Line {
        private final String matchId;
        private final String matchName;
        private final int totalTickets;
        private final int soldTickets;
        private final int heldTickets;
        private final BigDecimal revenue;

        public Line(String matchId, String matchName, int totalTickets, int soldTickets, int heldTickets, BigDecimal revenue) {
            this.matchId = matchId;
            this.matchName = matchName;
            this.totalTickets = totalTickets;
            this.soldTickets = soldTickets;
            this.heldTickets = heldTickets;
            this.revenue = revenue;
        }

        public String getMatchId() { return this.matchId; }
        public String getMatchName() { return this.matchName; }
        public int getTotalTickets() { return this.totalTickets; }
        public int getSoldTickets() { return this.soldTickets; }
        public int getHeldTickets() { return this.heldTickets; }
        public BigDecimal getRevenue() { return this.revenue; }
    }

    private final List<Line> lines = new ArrayList<>();
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalRefunded = BigDecimal.ZERO;
    private int totalSold;

    public void addLine(Line line) {
        this.lines.add(line);
        this.totalRevenue = this.totalRevenue.add(line.getRevenue());
        this.totalSold += line.getSoldTickets();
    }

    public void setTotalRefunded(BigDecimal totalRefunded) {
        this.totalRefunded = totalRefunded;
    }

    public List<Line> getLines() { return this.lines; }
    public BigDecimal getTotalRevenue() { return this.totalRevenue; }
    public BigDecimal getTotalRefunded() { return this.totalRefunded; }
    public int getTotalSold() { return this.totalSold; }
}
