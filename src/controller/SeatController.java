package controller;

import dto.SeatMap;
import exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Khoa: selectSeat(match, row, number) - giu ghe theo hang + so.
     * sectionId co the null; khi cung ten hang (vd "A") xuat hien o nhieu khu thi bat buoc nhap ma khu,
     * tranh giu nham ghe cua khu khac.
     */
    public BookingSeat selectSeat(User user, String matchId, String sectionId, String row, int number) {
        Match match = this.matchController.viewMatch(matchId);
        String label = row.trim().toUpperCase() + number;
        List<Seat> candidates = new ArrayList<>();
        for (Section section : this.sectionRepo.findByStadiumId(match.getStadiumId())) {
            if (sectionId != null && !section.getId().equalsIgnoreCase(sectionId.trim())) {
                continue;
            }
            for (Seat seat : this.seatRepo.findBySectionId(section.getId())) {
                if (seat.getRowNumber().equalsIgnoreCase(row.trim()) && seat.getSeatNumber() == number) {
                    candidates.add(seat);
                }
            }
        }
        if (candidates.isEmpty()) {
            throw new BusinessException("Khong co ghe " + label
                    + (sectionId == null ? "" : " trong khu " + sectionId.toUpperCase()) + " cua san.");
        }
        if (candidates.size() > 1) {
            StringBuilder sections = new StringBuilder();
            for (Seat seat : candidates) {
                sections.append(sections.length() == 0 ? "" : ", ").append(seat.getSectionId());
            }
            throw new BusinessException("Ghe " + label + " co o nhieu khu (" + sections
                    + "). Vui long nhap kem ma khu, vd " + candidates.get(0).getSectionId() + "-" + label + ".");
        }
        Seat target = candidates.get(0);
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
