package controller;

import dto.SalesReport;
import exception.BusinessException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
import repository.BookingRepository;
import repository.BookingSeatRepository;
import repository.MatchRepository;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.StadiumRepository;
import repository.TeamRepository;
import repository.TicketRepository;
import service.AuditService;
import service.ReportService;
import util.AppConfig;
import util.MoneyUtil;
import view.MatchView;
import view.OrderView;
import view.ReportView;
import view.SellerView;
import view.TicketView;

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
    private final BookingSeatRepository bookingSeatRepo = new BookingSeatRepository();
    private final BookingRepository bookingRepo = new BookingRepository();

    private final User seller;
    private final SellerView sellerView = new SellerView();
    private final MatchView matchView = new MatchView();
    private final TicketView ticketView = new TicketView();
    private final OrderView orderView = new OrderView();
    private final ReportView reportView = new ReportView();
    private final ProfileController profileController;

    public SellerController(User seller) {
        this.seller = seller;
        this.profileController = new ProfileController(seller);
    }

    // =====================================================================
    // LUONG (FLOW): controller lay du lieu tu View -> xu ly -> tra ket qua cho View
    // =====================================================================

    /** Khanh: SellerView.showMenu - vong lap menu Seller. */
    public void start() {
        while (true) {
            String title = this.sellerView.withUnread("SELLER MANAGEMENT - " + this.seller.getFullName(),
                    this.profileController.countUnreadNotifications());
            int choice = this.sellerView.showMenu(title);
            switch (choice) {
                case 1: this.runSafely(this::handleAddMatch); break;
                case 2: this.runSafely(this::handleViewMatches); break;
                case 3: this.runSafely(this::handleUpdateMatch); break;
                case 4: this.runSafely(this::handleDeleteMatch); break;
                case 5: this.runSafely(this::handleOpenSale); break;
                case 6: this.runSafely(this::handleCloseSale); break;
                case 7: this.runSafely(this::handleAddTicket); break;
                case 8: this.runSafely(this::handleGenerateTickets); break;
                case 9: this.runSafely(this::handleViewTickets); break;
                case 10: this.runSafely(this::handleUpdateTicket); break;
                case 11: this.runSafely(this::handleToggleTicketSale); break;
                case 12: this.runSafely(this::handleSellTicket); break;
                case 13: this.runSafely(this::handleCheckIn); break;
                case 14: this.runSafely(this::handleReport); break;
                case 15: this.runSafely(this::handleHistory); break;
                case 16: this.runSafely(this.profileController::showNotifications); break;
                case 17: this.profileController.showProfileMenu(); break;
                case 18: this.runSafely(this::handleDeleteTicket); break;
                case 0: return;
                default: this.sellerView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleAddMatch() {
        this.sellerView.printSection("DOI BONG");
        this.sellerView.printList(this.getAllTeams(), "Chua co doi bong.");
        this.sellerView.printSection("SAN VAN DONG");
        this.sellerView.printList(this.getAllStadiums(), "Chua co san.");
        String name = this.sellerView.inputNonEmpty("Ten tran: ");
        String competition = this.sellerView.inputNonEmpty("Giai dau: ");
        String home = this.sellerView.inputNonEmpty("Ma doi nha: ");
        String away = this.sellerView.inputNonEmpty("Ma doi khach: ");
        String stadium = this.sellerView.inputNonEmpty("Ma san: ");
        LocalDate date = this.sellerView.inputDate("Ngay thi dau (yyyy-MM-dd): ");
        LocalTime time = this.sellerView.inputTime("Gio thi dau (HH:mm): ");
        Match match = this.createMatch(this.seller, name, competition, home, away, stadium, date, time);
        this.sellerView.printSuccess("Tao tran thanh cong: " + match.getId() + ". Hay tao ve va mo ban.");
    }

    private void handleViewMatches() {
        this.matchView.displayMatches(this.getMatches(this.seller));
    }

    private String chooseMatch() {
        this.matchView.displayMatches(this.getMatches(this.seller));
        return this.sellerView.inputId("Nhap ma tran: ");
    }

    private void handleUpdateMatch() {
        String matchId = this.chooseMatch();
        this.sellerView.showMessage("(Bo trong de giu nguyen)");
        String name = this.sellerView.inputString("Ten tran moi: ");
        String competition = this.sellerView.inputString("Giai dau moi: ");
        LocalDate date = this.sellerView.inputOptionalDate("Ngay moi (yyyy-MM-dd): ");
        LocalTime time = this.sellerView.inputOptionalTime("Gio moi (HH:mm): ");
        this.updateMatch(this.seller, matchId, name, competition, date, time);
        this.sellerView.printSuccess("Cap nhat tran thanh cong!");
    }

    private void handleDeleteMatch() {
        String matchId = this.chooseMatch();
        if (this.sellerView.confirm("Xac nhan xoa tran " + matchId)) {
            this.deleteMatch(this.seller, matchId);
            this.sellerView.printSuccess("Da xoa tran " + matchId);
        }
    }

    private void handleOpenSale() {
        Match match = this.openSale(this.seller, this.chooseMatch());
        this.sellerView.printSuccess("Tran " + match.getId() + " da MO BAN ve.");
    }

    private void handleCloseSale() {
        Match match = this.closeSale(this.seller, this.chooseMatch());
        this.sellerView.printSuccess("Tran " + match.getId() + " da DONG BAN ve.");
    }

    private void handleAddTicket() {
        String matchId = this.chooseMatch();
        String sectionId = this.chooseSection(matchId);
        this.sellerView.printSection("GHE CUA KHU " + sectionId);
        this.sellerView.printList(this.getSeatsOfSection(sectionId), "Khu vuc chua co ghe.");
        String seatId = this.sellerView.inputId("Ma ghe (SEATxxx): ");
        String type = this.sellerView.inputString("Loai ve (Enter = theo khu vuc): ");
        BigDecimal price = this.sellerView.inputOptionalMoney("Gia ve (Enter = gia goc khu vuc): ");
        Ticket ticket = this.createTicket(this.seller, matchId, seatId, type, price);
        this.sellerView.printSuccess("Tao ve thanh cong: " + ticket.getId() + " - " + MoneyUtil.format(ticket.getPrice()));
    }

    private void handleGenerateTickets() {
        String matchId = this.chooseMatch();
        String sectionId = this.chooseSection(matchId);
        BigDecimal price = this.sellerView.inputOptionalMoney("Gia ve (Enter = gia goc khu vuc): ");
        int created = this.generateTicketsForSection(this.seller, matchId, sectionId, price);
        this.sellerView.printSuccess("Da tao " + created + " ve moi cho khu " + sectionId + ".");
    }

    private String chooseSection(String matchId) {
        List<Section> sections = this.getSectionsOfMatch(this.seller, matchId);
        this.sellerView.printSection("KHU VUC");
        this.sellerView.printList(sections, "San chua co khu vuc.");
        return this.sellerView.inputId("Ma khu vuc: ");
    }

    private void handleViewTickets() {
        this.ticketView.showTickets(this.getTickets(this.seller, this.chooseMatch()));
    }

    private String chooseTicket(String label) {
        String matchId = this.chooseMatch();
        this.ticketView.showTickets(this.getTickets(this.seller, matchId));
        return this.sellerView.inputId(label);
    }

    private void handleUpdateTicket() {
        String ticketId = this.chooseTicket("Ma ve: ");
        String type = this.sellerView.inputString("Loai ve moi (Enter = giu nguyen): ");
        BigDecimal price = this.sellerView.inputOptionalMoney("Gia moi (Enter = giu nguyen): ");
        Ticket ticket = this.updateTicket(this.seller, ticketId, type, price);
        this.sellerView.printSuccess("Da cap nhat: " + ticket);
    }

    private void handleToggleTicketSale() {
        String ticketId = this.chooseTicket("Ma ve: ");
        int action = this.sellerView.askToggleSaleAction();
        Ticket ticket = action == 1
                ? this.stopTicketSale(this.seller, ticketId)
                : this.resumeTicketSale(this.seller, ticketId);
        this.sellerView.printSuccess("Ve " + ticket.getId() + " -> " + ticket.getStatus());
    }

    private void handleDeleteTicket() {
        String ticketId = this.chooseTicket("Ma ve can xoa: ");
        if (this.sellerView.confirm("Xac nhan xoa ve " + ticketId)) {
            this.deleteTicket(this.seller, ticketId);
            this.sellerView.printSuccess("Da xoa ve " + ticketId);
        }
    }

    /** Khanh: sellTicket. */
    private void handleSellTicket() {
        String ticketId = this.chooseTicket("Ma ve can ban: ");
        String buyer = this.sellerView.inputNonEmpty("Username Buyer nhan ve: ");
        this.sellerView.displaySoldTickets(this.sellTicket(this.seller, ticketId, buyer));
    }

    private void handleCheckIn() {
        ETicket ticket = this.checkInTicket(this.seller, this.sellerView.inputNonEmpty("Ma QR: "));
        this.sellerView.printSuccess("Check-in thanh cong: " + ticket.getSeatInfo());
    }

    private void handleReport() {
        this.reportView.displayReport(this.getSalesReport(this.seller));
    }

    private void handleHistory() {
        this.orderView.displayHistory(this.getSalesHistory(this.seller));
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (NoSuchElementException e) {
            throw e; // het du lieu vao: de MainController ket thuc chuong trinh
        } catch (RuntimeException e) {
            this.sellerView.printError(e.getMessage());
        }
    }

    // =====================================================================
    // NGHIEP VU
    // =====================================================================

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
        this.requireFuture(date, time);
        this.requireNoScheduleConflict(null, stadiumId, homeTeamId, awayTeamId, date, time);
        Match match = new Match(null, name, competition, homeTeamId.toUpperCase(), awayTeamId.toUpperCase(),
                stadiumId.toUpperCase(), seller.getId(), date, time);
        this.matchRepo.insert(match);
        this.auditService.log(seller.getUsername(), "CREATE_MATCH " + match.getId(), AuditService.SUCCESS);
        return this.matchController.enrich(match);
    }

    /** Khanh: updateMatch. */
    public Match updateMatch(User seller, String matchId, String name, String competition, LocalDate date, LocalTime time) {
        Match match = this.requireOwnMatch(seller, matchId);
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
            this.requireFuture(newDate, newTime);
            this.requireNoScheduleConflict(match.getId(), match.getStadiumId(), match.getHomeTeamId(), match.getAwayTeamId(),
                    newDate, newTime);
            match.setMatchDate(newDate);
            match.setStartTime(newTime);
        }
        this.matchRepo.update(match);
        this.auditService.log(seller.getUsername(), "UPDATE_MATCH " + matchId, AuditService.SUCCESS);
        return this.matchController.enrich(match);
    }

    /**
     * Khanh: deleteMatch - chi xoa khi tran chua phat sinh giao dich nao.
     * Neu da co booking (ke ca da huy/het han) thi phai giu lai de doi soat lich su -> chi dong ban.
     */
    public void deleteMatch(User seller, String matchId) {
        this.requireOwnMatch(seller, matchId);
        List<Ticket> tickets = this.ticketRepo.findByMatchId(matchId);
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.HOLD || ticket.getStatus() == TicketStatus.SOLD) {
                throw new BusinessException("Tran da co ve dang giu/da ban, chi co the dong ban ve.");
            }
        }
        if (!this.bookingRepo.findByMatchId(matchId).isEmpty()) {
            throw new BusinessException("Tran da phat sinh booking (lich su giao dich), chi co the dong ban ve.");
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
        Match match = this.requireOwnMatch(seller, matchId);
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
        Match match = this.requireOwnMatch(seller, matchId);
        match.closeSale();
        this.matchRepo.update(match);
        this.auditService.log(seller.getUsername(), "CLOSE_SALE " + matchId, AuditService.SUCCESS);
        return match;
    }

    // ============================ TICKET ============================

    /** Khanh: createTicket - mo ban mot ghe. price = null thi lay gia goc cua khu vuc. */
    public Ticket createTicket(User seller, String matchId, String seatId, String ticketType, BigDecimal price) {
        Match match = this.requireOwnMatch(seller, matchId);
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
        Match match = this.requireOwnMatch(seller, matchId);
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
        this.requireOwnMatch(seller, matchId);
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

    /** Soat ve vao cong bang ma QR. */
    public ETicket checkInTicket(User seller, String qrCode) {
        return this.ticketController.checkIn(seller, qrCode);
    }

    // ============================ REPORT ============================

    public SalesReport getSalesReport(User seller) {
        return this.reportService.buildReport(this.getMatches(seller));
    }

    /** Lich su ban: cac booking da xac nhan/da huy thuoc tran cua Seller. */
    public List<Booking> getSalesHistory(User seller) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : this.bookingController.getBookingsForMatches(this.getMatches(seller))) {
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
        Match match = this.requireOwnMatch(seller, matchId);
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
        this.requireOwnMatch(seller, ticket.getMatchId());
        return ticket;
    }

    private void saveTicket(Ticket ticket, int expectedVersion) {
        if (!this.ticketRepo.updateIfVersionMatches(ticket, expectedVersion)) {
            throw new BusinessException("Ve vua bi thay doi boi nguoi khac, vui long thu lai.");
        }
    }

    /**
     * Chong trung lich: mot san chi to chuc mot tran trong khoang MATCH_SLOT_HOURS gio,
     * va mot doi khong da hai tran trong cung mot ngay. Bo qua tran da huy va chinh tran dang sua.
     */
    private void requireNoScheduleConflict(String excludeMatchId, String stadiumId, String homeTeamId,
                                           String awayTeamId, LocalDate date, LocalTime time) {
        LocalDateTime start = LocalDateTime.of(date, time);
        for (Match other : this.matchRepo.findAll()) {
            if (other.getId().equalsIgnoreCase(excludeMatchId) || other.getStatus() == MatchStatus.CANCELLED) {
                continue;
            }
            long minutesApart = Math.abs(Duration.between(other.getStartDateTime(), start).toMinutes());
            if (other.getStadiumId().equalsIgnoreCase(stadiumId) && minutesApart < AppConfig.MATCH_SLOT_HOURS * 60L) {
                throw new BusinessException("San da co tran " + other.getId() + " luc " + other.getMatchDate() + " "
                        + other.getStartTime() + " (moi tran can cach nhau " + AppConfig.MATCH_SLOT_HOURS + " gio).");
            }
            boolean sameTeam = other.getHomeTeamId().equalsIgnoreCase(homeTeamId) || other.getHomeTeamId().equalsIgnoreCase(awayTeamId)
                    || other.getAwayTeamId().equalsIgnoreCase(homeTeamId) || other.getAwayTeamId().equalsIgnoreCase(awayTeamId);
            if (sameTeam && other.getMatchDate().equals(date)) {
                throw new BusinessException("Mot doi bong da co tran " + other.getId() + " trong ngay " + date + ".");
            }
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
