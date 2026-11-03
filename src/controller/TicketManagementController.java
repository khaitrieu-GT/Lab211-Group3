package controller;

import exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import model.ETicket;
import model.Match;
import model.Seat;
import model.Section;
import model.Ticket;
import model.User;
import model.enums.MatchStatus;
import model.enums.TicketStatus;
import repository.BookingSeatRepository;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.TicketRepository;
import service.AuditService;

/**
 * Quan ly ve mo ban cua Seller (Khanh: SellerController): tao, sua, dung/mo ban, xoa, ban tai quay.
 */
public class TicketManagementController {

    private final TicketRepository ticketRepo = new TicketRepository();
    private final SectionRepository sectionRepo = new SectionRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final BookingSeatRepository bookingSeatRepo = new BookingSeatRepository();
    private final MatchManagementController matchManagementController = new MatchManagementController();
    private final BookingController bookingController = new BookingController();
    private final AuditService auditService = new AuditService();

    /** Khanh: createTicket - mo ban mot ghe. price = null thi lay gia goc cua khu vuc. */
    public Ticket createTicket(User seller, String matchId, String seatId, String ticketType, BigDecimal price) {
        Match match = this.matchManagementController.requireOwnMatch(seller, matchId);
        this.requireNotFinished(match);
        Seat seat = this.seatRepo.findById(seatId);
        if (seat == null) {
            throw new BusinessException("Khong tim thay ghe " + seatId);
        }
        Section section = this.sectionRepo.findById(seat.getSectionId());
        if (section == null || !section.getStadiumId().equalsIgnoreCase(match.getStadiumId())) {
            throw new BusinessException("Ghe khong thuoc san cua tran dau.");
        }
        if (!seat.isAvailable() || !section.isActive()) {
            throw new BusinessException("Ghe/khu vuc dang bi khoa hoac bao tri.");
        }
        if (this.ticketRepo.findByMatchAndSeat(matchId, seat.getId()) != null) {
            throw new BusinessException("Ghe nay da co ve cho tran.");
        }
        String type = ticketType == null || ticketType.trim().isEmpty() ? section.getType() : ticketType.trim().toUpperCase();
        Ticket ticket = new Ticket(null, match.getId(), seat.getId(), section.getId(), type,
                price == null ? section.getBasePrice() : price);
        this.ticketRepo.insert(ticket);
        this.auditService.log(seller.getUsername(), "CREATE_TICKET " + ticket.getId(), AuditService.SUCCESS);
        return ticket;
    }

    /** Tao ve hang loat cho moi ghe hop le cua mot khu vuc. Tra ve so ve tao moi. */
    public int generateTicketsForSection(User seller, String matchId, String sectionId, BigDecimal price) {
        Match match = this.matchManagementController.requireOwnMatch(seller, matchId);
        this.requireNotFinished(match);
        Section section = this.sectionRepo.findById(sectionId);
        if (section == null || !section.getStadiumId().equalsIgnoreCase(match.getStadiumId())) {
            throw new BusinessException("Khu vuc khong thuoc san cua tran dau.");
        }
        if (!section.isActive()) {
            throw new BusinessException("Khu vuc dang dong.");
        }
        int created = 0;
        for (Seat seat : this.seatRepo.findBySectionId(section.getId())) {
            if (seat.isAvailable() && this.ticketRepo.findByMatchAndSeat(matchId, seat.getId()) == null) {
                this.ticketRepo.insert(new Ticket(null, match.getId(), seat.getId(), section.getId(), section.getType(),
                        price == null ? section.getBasePrice() : price));
                created++;
            }
        }
        this.auditService.log(seller.getUsername(), "GENERATE_TICKETS " + matchId + "/" + sectionId + " x" + created,
                AuditService.SUCCESS);
        return created;
    }

    /** Khanh: updateTicket - doi loai ve / gia khi ve chua co nguoi giu. */
    public Ticket updateTicket(User seller, String ticketId, String ticketType, BigDecimal price) {
        Ticket ticket = this.requireOwnTicket(seller, ticketId);
        if (ticket.getStatus() != TicketStatus.AVAILABLE && ticket.getStatus() != TicketStatus.CANCELLED) {
            throw new BusinessException("Chi sua duoc ve AVAILABLE/CANCELLED (hien tai: " + ticket.getStatus() + ").");
        }
        int expectedVersion = ticket.getVersion();
        if (ticketType != null && !ticketType.trim().isEmpty()) {
            ticket.setTicketType(ticketType.trim().toUpperCase());
        }
        if (price != null) {
            ticket.setPrice(price);
        }
        this.saveTicket(ticket, expectedVersion);
        this.auditService.log(seller.getUsername(), "UPDATE_TICKET " + ticketId, AuditService.SUCCESS);
        return ticket;
    }

    /** Khanh: getTickets - ve cua mot tran. */
    public List<Ticket> getTickets(User seller, String matchId) {
        this.matchManagementController.requireOwnMatch(seller, matchId);
        List<Ticket> tickets = this.ticketRepo.findByMatchId(matchId);
        for (Ticket ticket : tickets) {
            ticket.setSeat(this.seatRepo.findById(ticket.getSeatId()));
        }
        return tickets;
    }

    /** Khanh: Ticket.stopSale. */
    public Ticket stopTicketSale(User seller, String ticketId) {
        Ticket ticket = this.requireOwnTicket(seller, ticketId);
        int expectedVersion = ticket.getVersion();
        ticket.stopSale();
        this.saveTicket(ticket, expectedVersion);
        this.auditService.log(seller.getUsername(), "STOP_TICKET " + ticketId, AuditService.SUCCESS);
        return ticket;
    }

    /** Khanh: Ticket.putOnSale - mo ban lai ve da dung. */
    public Ticket resumeTicketSale(User seller, String ticketId) {
        Ticket ticket = this.requireOwnTicket(seller, ticketId);
        if (ticket.getStatus() != TicketStatus.CANCELLED) {
            throw new BusinessException("Chi mo ban lai ve dang CANCELLED.");
        }
        int expectedVersion = ticket.getVersion();
        ticket.putOnSale();
        this.saveTicket(ticket, expectedVersion);
        this.auditService.log(seller.getUsername(), "RESUME_TICKET " + ticketId, AuditService.SUCCESS);
        return ticket;
    }

    /**
     * Xoa ve (Delete trong CRUD): chi xoa ve chua tung duoc dat (khong co trong booking nao)
     * va dang AVAILABLE/CANCELLED. Ve da co giao dich chi duoc dung ban.
     */
    public void deleteTicket(User seller, String ticketId) {
        Ticket ticket = this.requireOwnTicket(seller, ticketId);
        if (ticket.getStatus() == TicketStatus.HOLD || ticket.getStatus() == TicketStatus.SOLD) {
            throw new BusinessException("Ve dang " + ticket.getStatus() + ", khong the xoa.");
        }
        if (!this.bookingSeatRepo.findByTicketId(ticket.getId()).isEmpty()) {
            throw new BusinessException("Ve da tung duoc dat (co lich su giao dich), chi co the dung ban.");
        }
        this.ticketRepo.delete(ticket.getId());
        this.auditService.log(seller.getUsername(), "DELETE_TICKET " + ticketId, AuditService.SUCCESS);
    }

    /** Khanh: sellTicket - ban tai quay cho mot tai khoan Buyer (thu tien mat). */
    public List<ETicket> sellTicket(User seller, String ticketId, String buyerUsername) {
        this.requireOwnTicket(seller, ticketId);
        BookingController.PaymentResult result = this.bookingController.sellAtCounter(seller, ticketId, buyerUsername);
        return result.getTickets();
    }

    private Ticket requireOwnTicket(User seller, String ticketId) {
        Ticket ticket = this.ticketRepo.findById(ticketId);
        if (ticket == null) {
            throw new BusinessException("Khong tim thay ve " + ticketId);
        }
        this.matchManagementController.requireOwnMatch(seller, ticket.getMatchId());
        return ticket;
    }

    private void saveTicket(Ticket ticket, int expectedVersion) {
        if (!this.ticketRepo.updateIfVersionMatches(ticket, expectedVersion)) {
            throw new BusinessException("Ve vua bi thay doi boi nguoi khac, vui long thu lai.");
        }
    }

    private void requireNotFinished(Match match) {
        if (match.hasStarted() || match.getStatus() == MatchStatus.CANCELLED) {
            throw new BusinessException("Tran dau da dien ra hoac da huy.");
        }
    }
}
