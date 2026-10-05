package controller;

import dto.SalesReport;
import exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Booking;
import model.ETicket;
import model.Match;
import model.Seat;
import model.Section;
import model.Stadium;
import model.Team;
import model.Ticket;
import model.User;
import model.enums.MatchStatus;
import model.enums.TicketStatus;
import repository.MatchRepository;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.StadiumRepository;
import repository.TeamRepository;
import repository.TicketRepository;
import service.AuditService;
import service.ReportService;

/**
 * Nghiep vu cua Seller (Khanh: SellerController) - du lieu luu CSV thay vi List trong bo nho,
 * Seller chi thao tac tren tran do minh tao.
 */
public class SellerController {

    private final MatchRepository matchRepo = new MatchRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final StadiumRepository stadiumRepo = new StadiumRepository();
    private final SectionRepository sectionRepo = new SectionRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final MatchController matchController = new MatchController();
    private final BookingController bookingController = new BookingController();
    private final TicketController ticketController = new TicketController();
    private final ReportService reportService = new ReportService();
    private final AuditService auditService = new AuditService();

    // ============================ MATCH ============================

    /** Khanh: createMatch. */
    public Match createMatch(User seller, String name, String competition, String homeTeamId, String awayTeamId,
                             String stadiumId, LocalDate date, LocalTime time) {
        if (homeTeamId.equalsIgnoreCase(awayTeamId)) {
            throw new BusinessException("Doi nha va doi khach phai khac nhau.");
        }
        if (this.teamRepo.findById(homeTeamId) == null || this.teamRepo.findById(awayTeamId) == null) {
            throw new BusinessException("Ma doi bong khong ton tai.");
        }
        if (this.stadiumRepo.findById(stadiumId) == null) {
            throw new BusinessException("Ma san van dong khong ton tai.");
        }
        requireFuture(date, time);
        Match match = new Match(null, name, competition, homeTeamId.toUpperCase(), awayTeamId.toUpperCase(),
                stadiumId.toUpperCase(), seller.getId(), date, time);
        this.matchRepo.insert(match);
        this.auditService.log(seller.getUsername(), "CREATE_MATCH " + match.getId(), AuditService.SUCCESS);
        return this.matchController.enrich(match);
    }

    /** Khanh: updateMatch. */
    public Match updateMatch(User seller, String matchId, String name, String competition, LocalDate date, LocalTime time) {
        Match match = requireOwnMatch(seller, matchId);
        if (match.hasStarted()) {
            throw new BusinessException("Tran dau da dien ra, khong the sua.");
        }
        if (name != null && !name.trim().isEmpty()) {
            match.setName(name.trim());
        }
        if (competition != null && !competition.trim().isEmpty()) {
            match.setCompetition(competition.trim());
        }
        if (date != null || time != null) {
            LocalDate newDate = date != null ? date : match.getMatchDate();
            LocalTime newTime = time != null ? time : match.getStartTime();
            requireFuture(newDate, newTime);
            match.setMatchDate(newDate);
            match.setStartTime(newTime);
        }
        this.matchRepo.update(match);
        this.auditService.log(seller.getUsername(), "UPDATE_MATCH " + matchId, AuditService.SUCCESS);
        return this.matchController.enrich(match);
    }

    /** Khanh: deleteMatch - chi xoa khi chua co ve dang giu/da ban. */
    public void deleteMatch(User seller, String matchId) {
        requireOwnMatch(seller, matchId);
        List<Ticket> tickets = this.ticketRepo.findByMatchId(matchId);
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.HOLD || ticket.getStatus() == TicketStatus.SOLD) {
                throw new BusinessException("Tran da co ve dang giu/da ban, chi co the dong ban ve.");
            }
        }
        for (Ticket ticket : tickets) {
            this.ticketRepo.delete(ticket.getId());
        }
        this.matchRepo.delete(matchId);
        this.auditService.log(seller.getUsername(), "DELETE_MATCH " + matchId, AuditService.SUCCESS);
    }

    /** Khanh: getMatches - cac tran cua Seller. */
    public List<Match> getMatches(User seller) {
        List<Match> result = new ArrayList<>();
        for (Match match : this.matchRepo.findBySellerId(seller.getId())) {
            result.add(this.matchController.enrich(match));
        }
        return result;
    }

    /** Khanh: openSale. */
    public Match openSale(User seller, String matchId) {
        Match match = requireOwnMatch(seller, matchId);
        boolean hasAvailable = false;
        for (Ticket ticket : this.ticketRepo.findByMatchId(matchId)) {
            if (ticket.isAvailable()) {
                hasAvailable = true;
                break;
            }
        }
        if (!hasAvailable) {
            throw new BusinessException("Tran chua co ve AVAILABLE nao. Hay tao ve truoc khi mo ban.");
        }
        match.openSale();
        this.matchRepo.update(match);
        this.auditService.log(seller.getUsername(), "OPEN_SALE " + matchId, AuditService.SUCCESS);
        return match;
    }

    /** Khanh: closeSale. */
    public Match closeSale(User seller, String matchId) {
        Match match = requireOwnMatch(seller, matchId);
        match.closeSale();
        this.matchRepo.update(match);
        this.auditService.log(seller.getUsername(), "CLOSE_SALE " + matchId, AuditService.SUCCESS);
        return match;
    }

    // ============================ TICKET ============================

    /** Khanh: createTicket - mo ban mot ghe. price = null thi lay gia goc cua khu vuc. */
    public Ticket createTicket(User seller, String matchId, String seatId, String ticketType, BigDecimal price) {
        Match match = requireOwnMatch(seller, matchId);
        requireNotFinished(match);
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
        Match match = requireOwnMatch(seller, matchId);
        requireNotFinished(match);
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
        Ticket ticket = requireOwnTicket(seller, ticketId);
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
        saveTicket(ticket, expectedVersion);
        this.auditService.log(seller.getUsername(), "UPDATE_TICKET " + ticketId, AuditService.SUCCESS);
        return ticket;
    }

    /** Khanh: getTickets - ve cua mot tran. */
    public List<Ticket> getTickets(User seller, String matchId) {
        requireOwnMatch(seller, matchId);
        List<Ticket> tickets = this.ticketRepo.findByMatchId(matchId);
        for (Ticket ticket : tickets) {
            ticket.setSeat(this.seatRepo.findById(ticket.getSeatId()));
        }
        return tickets;
    }

    /** Khanh: Ticket.stopSale. */
    public Ticket stopTicketSale(User seller, String ticketId) {
        Ticket ticket = requireOwnTicket(seller, ticketId);
        int expectedVersion = ticket.getVersion();
        ticket.stopSale();
        saveTicket(ticket, expectedVersion);
        this.auditService.log(seller.getUsername(), "STOP_TICKET " + ticketId, AuditService.SUCCESS);
        return ticket;
    }

    /** Khanh: Ticket.putOnSale - mo ban lai ve da dung. */
    public Ticket resumeTicketSale(User seller, String ticketId) {
        Ticket ticket = requireOwnTicket(seller, ticketId);
        if (ticket.getStatus() != TicketStatus.CANCELLED) {
            throw new BusinessException("Chi mo ban lai ve dang CANCELLED.");
        }
        int expectedVersion = ticket.getVersion();
        ticket.putOnSale();
        saveTicket(ticket, expectedVersion);
        this.auditService.log(seller.getUsername(), "RESUME_TICKET " + ticketId, AuditService.SUCCESS);
        return ticket;
    }

    /** Khanh: sellTicket - ban tai quay cho mot tai khoan Buyer (thu tien mat). */
    public List<ETicket> sellTicket(User seller, String ticketId, String buyerUsername) {
        requireOwnTicket(seller, ticketId);
        BookingController.PaymentResult result = this.bookingController.sellAtCounter(seller, ticketId, buyerUsername);
        return result.getTickets();
    }

    /** Soat ve vao cong bang ma QR. */
    public ETicket checkInTicket(User seller, String qrCode) {
        return this.ticketController.checkIn(seller, qrCode);
    }

    // ============================ REPORT ============================

    public SalesReport getSalesReport(User seller) {
        return this.reportService.buildReport(getMatches(seller));
    }

    /** Lich su ban: cac booking da xac nhan/da huy thuoc tran cua Seller. */
    public List<Booking> getSalesHistory(User seller) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : this.bookingController.getBookingsForMatches(getMatches(seller))) {
            if (!booking.isPending()) {
                result.add(booking);
            }
        }
        return result;
    }

    // ============================ LOOKUP ============================

    public List<Team> getAllTeams() {
        return this.teamRepo.findAll();
    }

    public List<Stadium> getAllStadiums() {
        return this.stadiumRepo.findAll();
    }

    public List<Section> getSectionsOfMatch(User seller, String matchId) {
        Match match = requireOwnMatch(seller, matchId);
        return this.sectionRepo.findByStadiumId(match.getStadiumId());
    }

    public List<Seat> getSeatsOfSection(String sectionId) {
        return this.seatRepo.findBySectionId(sectionId);
    }

    // ============================ HELPER ============================

    private Match requireOwnMatch(User seller, String matchId) {
        Match match = this.matchRepo.findById(matchId);
        if (match == null || !match.getSellerId().equalsIgnoreCase(seller.getId())) {
            throw new BusinessException("Khong tim thay tran " + matchId + " trong danh sach tran cua ban.");
        }
        return match;
    }

    private Ticket requireOwnTicket(User seller, String ticketId) {
        Ticket ticket = this.ticketRepo.findById(ticketId);
        if (ticket == null) {
            throw new BusinessException("Khong tim thay ve " + ticketId);
        }
        requireOwnMatch(seller, ticket.getMatchId());
        return ticket;
    }

    private void saveTicket(Ticket ticket, int expectedVersion) {
        if (!this.ticketRepo.updateIfVersionMatches(ticket, expectedVersion)) {
            throw new BusinessException("Ve vua bi thay doi boi nguoi khac, vui long thu lai.");
        }
    }

    private void requireFuture(LocalDate date, LocalTime time) {
        if (!LocalDateTime.of(date, time).isAfter(LocalDateTime.now())) {
            throw new BusinessException("Thoi gian thi dau phai o tuong lai.");
        }
    }

    private void requireNotFinished(Match match) {
        if (match.hasStarted() || match.getStatus() == MatchStatus.CANCELLED) {
            throw new BusinessException("Tran dau da dien ra hoac da huy.");
        }
    }
}
