package repository;

import java.util.ArrayList;
import java.util.List;
import model.Booking;

public class BookingRepository extends CsvRepository<Booking> {

    public BookingRepository() {
        super("bookings.csv", "id,userId,matchId,status,totalAmount,createdAt,updatedAt,expiresAt", "BK", 4);
    }

    @Override
    protected Booking parse(String line) {
        return Booking.fromCsvLine(line);
    }

    public List<Booking> findByUserId(String userId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : findAll()) {
            if (b.getUserId().equalsIgnoreCase(userId)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Booking> findByMatchId(String matchId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : findAll()) {
            if (b.getMatchId().equalsIgnoreCase(matchId)) {
                result.add(b);
            }
        }
        return result;
    }
}
