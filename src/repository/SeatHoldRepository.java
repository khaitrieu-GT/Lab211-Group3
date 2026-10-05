package repository;

import java.util.ArrayList;
import java.util.List;
import model.SeatHold;
import model.enums.SeatHoldStatus;

public class SeatHoldRepository extends CsvRepository<SeatHold> {

    public SeatHoldRepository() {
        super("seat_holds.csv", "id,ticketId,userId,bookingId,holdToken,status,heldAt,expiresAt,extendCount", "H", 4);
    }

    @Override
    protected SeatHold parse(String line) {
        return SeatHold.fromCsvLine(line);
    }

    /** Cac phien dang o trang thai HOLD (co the da qua han nhung chua duoc quet). */
    public List<SeatHold> findHolding() {
        List<SeatHold> result = new ArrayList<>();
        for (SeatHold h : findAll()) {
            if (h.getStatus() == SeatHoldStatus.HOLD) {
                result.add(h);
            }
        }
        return result;
    }

    public List<SeatHold> findHoldingByBooking(String bookingId) {
        List<SeatHold> result = new ArrayList<>();
        for (SeatHold h : findHolding()) {
            if (h.getBookingId().equalsIgnoreCase(bookingId)) {
                result.add(h);
            }
        }
        return result;
    }

    public SeatHold findHoldingByTicket(String ticketId) {
        for (SeatHold h : findHolding()) {
            if (h.getTicketId().equalsIgnoreCase(ticketId)) {
                return h;
            }
        }
        return null;
    }
}
