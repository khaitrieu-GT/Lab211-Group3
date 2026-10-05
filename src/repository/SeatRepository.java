package repository;

import java.util.ArrayList;
import java.util.List;
import model.Seat;
import model.enums.SeatStatus;

/** Nhat: SeatRepository. */
public class SeatRepository extends CsvRepository<Seat> {

    public SeatRepository() {
        super("seats.csv", "id,sectionId,rowNumber,seatNumber,status,version", "SEAT", 3);
    }

    @Override
    protected Seat parse(String line) {
        return Seat.fromCsvLine(line);
    }

    public List<Seat> findBySectionId(String sectionId) {
        List<Seat> result = new ArrayList<>();
        for (Seat s : findAll()) {
            if (s.getSectionId().equalsIgnoreCase(sectionId)) {
                result.add(s);
            }
        }
        return result;
    }

    /** Nhat: updateStatus - doi trang thai va tang version trong cung mot khoa. */
    public boolean updateStatus(String seatId, SeatStatus newStatus) {
        synchronized (this.lock) {
            Seat seat = findById(seatId);
            if (seat == null) {
                return false;
            }
            seat.updateStatus(newStatus);
            return update(seat);
        }
    }
}
