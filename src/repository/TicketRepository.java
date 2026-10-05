package repository;

import java.util.ArrayList;
import java.util.List;
import model.Ticket;

/**
 * Kho ve mo ban. updateIfVersionMatches la chot chan Double Booking (optimistic locking).
 */
public class TicketRepository extends CsvRepository<Ticket> {

    public TicketRepository() {
        super("tickets.csv", "id,matchId,seatId,sectionId,ticketType,price,status,version,updatedAt", "TK", 4);
    }

    @Override
    protected Ticket parse(String line) {
        return Ticket.fromCsvLine(line);
    }

    public List<Ticket> findByMatchId(String matchId) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : findAll()) {
            if (t.getMatchId().equalsIgnoreCase(matchId)) {
                result.add(t);
            }
        }
        return result;
    }

    public Ticket findByMatchAndSeat(String matchId, String seatId) {
        for (Ticket t : findAll()) {
            if (t.getMatchId().equalsIgnoreCase(matchId) && t.getSeatId().equalsIgnoreCase(seatId)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Chi ghi neu version trong file van bang expectedVersion (chua ai sua truoc).
     * Hai nguoi cung giu mot ghe: nguoi ghi sau se that bai.
     */
    public boolean updateIfVersionMatches(Ticket ticket, int expectedVersion) {
        synchronized (this.lock) {
            Ticket current = findById(ticket.getId());
            if (current == null || current.getVersion() != expectedVersion) {
                return false;
            }
            return update(ticket);
        }
    }
}
