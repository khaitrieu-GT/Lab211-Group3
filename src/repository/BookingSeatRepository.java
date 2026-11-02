package repository;

import java.util.ArrayList;
import java.util.List;
import model.BookingSeat;

public class BookingSeatRepository extends CsvRepository<BookingSeat> {

    public BookingSeatRepository() {
        super("booking_seats.csv", "id,bookingId,ticketId,seatLabel,sectionId,price,status,createdAt", "BS", 4);
    }

    @Override
    public BookingSeat parse(String line) {
        return BookingSeat.fromCsvLine(line);
    }

    public List<BookingSeat> findByTicketId(String ticketId) {
        List<BookingSeat> result = new ArrayList<>();
        for (BookingSeat s : this.findAll()) {
            if (s.getTicketId().equalsIgnoreCase(ticketId)) {
                result.add(s);
            }
        }
        return result;
    }

    public List<BookingSeat> findByBookingId(String bookingId) {
        List<BookingSeat> result = new ArrayList<>();
        for (BookingSeat s : this.findAll()) {
            if (s.getBookingId().equalsIgnoreCase(bookingId)) {
                result.add(s);
            }
        }
        return result;
    }
}
