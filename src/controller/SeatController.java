package controller;

import dto.SeatMap;
import exception.BusinessException;
import model.Booking;
import model.BookingSeat;
import model.Match;
import model.Seat;
import model.Section;
import model.Stadium;
import model.Ticket;
import model.User;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.TicketRepository;

/**
 * So do san va chon ghe (Khoa: SeatController).
 */
public class SeatController {

    private final SectionRepository sectionRepo = new SectionRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final MatchController matchController = new MatchController();
    private final BookingController bookingController = new BookingController();

    /** Khoa: viewSeatMap - san -> khu vuc -> ghe kem trang thai ve cua tran. */
    public SeatMap viewSeatMap(String matchId) {
        this.bookingController.releaseExpiredHolds();
        Match match = this.matchController.viewMatch(matchId);
        Stadium stadium = match.getStadium();
        if (stadium == null) {
            throw new BusinessException("Tran dau chua gan san van dong.");
        }
        for (Section section : this.sectionRepo.findByStadiumId(stadium.getId())) {
            section.setSeats(this.seatRepo.findBySectionId(section.getId()));
            stadium.addSection(section);
        }
        SeatMap map = new SeatMap(match, stadium);
        for (Ticket ticket : this.ticketRepo.findByMatchId(matchId)) {
            map.putTicket(ticket);
        }
        return map;
    }

    /** Khoa: selectSeat(match, row, number) - giu ghe theo hang + so. */
    public BookingSeat selectSeat(User user, String matchId, String row, int number) {
        Match match = this.matchController.viewMatch(matchId);
        Seat target = null;
        for (Section section : this.sectionRepo.findByStadiumId(match.getStadiumId())) {
            for (Seat seat : this.seatRepo.findBySectionId(section.getId())) {
                if (seat.getRowNumber().equalsIgnoreCase(row.trim()) && seat.getSeatNumber() == number) {
                    target = seat;
                }
            }
        }
        if (target == null) {
            throw new BusinessException("Khong co ghe " + row + number + " trong san.");
        }
        if (!target.isAvailable()) {
            throw new BusinessException("Ghe " + target.getLabel() + " dang " + target.getStatus() + ".");
        }
        Ticket ticket = this.ticketRepo.findByMatchAndSeat(matchId, target.getId());
        if (ticket == null) {
            throw new BusinessException("Ghe " + target.getLabel() + " chua duoc mo ban cho tran nay.");
        }
        return this.bookingController.holdSeat(user, ticket.getId());
    }

    /** Khoa: unselectSeat. */
    public Booking unselectSeat(User user, String bookingSeatId) {
        return this.bookingController.releaseSeat(user, bookingSeatId);
    }
}
