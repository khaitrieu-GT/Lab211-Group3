package dto;

import java.util.HashMap;
import java.util.Map;
import model.Match;
import model.Seat;
import model.Stadium;
import model.Ticket;

/**
 * So do san cua mot tran: san -> khu vuc -> ghe, kem ve mo ban cua tung ghe.
 */
public class SeatMap {
    private final Match match;
    private final Stadium stadium;
    private final Map<String, Ticket> ticketBySeatId = new HashMap<>();

    public SeatMap(Match match, Stadium stadium) {
        this.match = match;
        this.stadium = stadium;
    }

    public void putTicket(Ticket ticket) {
        this.ticketBySeatId.put(ticket.getSeatId(), ticket);
    }

    /** Ve cua ghe trong tran, null neu Seller chua mo ban ghe nay. */
    public Ticket getTicket(Seat seat) {
        return this.ticketBySeatId.get(seat.getId());
    }

    public Match getMatch() { return this.match; }
    public Stadium getStadium() { return this.stadium; }
}
