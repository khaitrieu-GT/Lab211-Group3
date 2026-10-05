package repository;

import java.util.ArrayList;
import java.util.List;
import model.ETicket;

public class ETicketRepository extends CsvRepository<ETicket> {

    public ETicketRepository() {
        super("etickets.csv", "id,bookingId,bookingSeatId,ticketId,userId,matchId,seatInfo,qrCode,status,issueDate", "ET", 4);
    }

    @Override
    protected ETicket parse(String line) {
        return ETicket.fromCsvLine(line);
    }

    public List<ETicket> findByUserId(String userId) {
        List<ETicket> result = new ArrayList<>();
        for (ETicket t : findAll()) {
            if (t.getUserId().equalsIgnoreCase(userId)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<ETicket> findByBookingId(String bookingId) {
        List<ETicket> result = new ArrayList<>();
        for (ETicket t : findAll()) {
            if (t.getBookingId().equalsIgnoreCase(bookingId)) {
                result.add(t);
            }
        }
        return result;
    }

    public ETicket findByQrCode(String qrCode) {
        for (ETicket t : findAll()) {
            if (qrCode.trim().equalsIgnoreCase(t.getQrCode())) {
                return t;
            }
        }
        return null;
    }
}
