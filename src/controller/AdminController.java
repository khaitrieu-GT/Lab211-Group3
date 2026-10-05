package controller;

import dto.SalesReport;
import exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import model.AuditLog;
import model.Booking;
import model.Match;
import model.Payment;
import model.Seat;
import model.Section;
import model.Stadium;
import model.Team;
import model.Ticket;
import model.Transaction;
import model.User;
import model.enums.Role;
import model.enums.SeatStatus;
import model.enums.SectionStatus;
import model.enums.UserStatus;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.StadiumRepository;
import repository.TeamRepository;
import repository.TicketRepository;
import repository.UserRepository;
import service.AuditService;
import service.NotificationService;
import service.ReportService;
import util.PasswordUtil;

/**
 * Nghiep vu quan tri (Nhat: AdminController) - mo rong theo quyen Admin trong README.
 */
public class AdminController {
    private final UserRepository userRepo;
    private final StadiumRepository stadiumRepo;
    private final SeatRepository seatRepo;
    private final SectionRepository sectionRepo;
    private final TeamRepository teamRepo;
    private final TicketRepository ticketRepo;
    private final MatchController matchController;
    private final BookingController bookingController;
    private final PaymentController paymentController;
    private final ReportService reportService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public AdminController() {
        this.userRepo = new UserRepository();
        this.stadiumRepo = new StadiumRepository();
        this.seatRepo = new SeatRepository();
        this.sectionRepo = new SectionRepository();
        this.teamRepo = new TeamRepository();
        this.ticketRepo = new TicketRepository();
        this.matchController = new MatchController();
        this.bookingController = new BookingController();
        this.paymentController = new PaymentController();
        this.reportService = new ReportService();
        this.notificationService = new NotificationService();
        this.auditService = new AuditService();
    }

    // ============================ USER ============================

    /** Nhat: getAllUsers. */
    public List<User> getAllUsers() {
        return this.userRepo.findAll();
    }

    /** Nhat: changeUserStatus - khoa/mo khoa tai khoan. */
    public boolean changeUserStatus(User admin, String userId, UserStatus status) {
        if (admin.getId().equalsIgnoreCase(userId)) {
            throw new BusinessException("Khong the tu doi trang thai tai khoan cua chinh minh.");
        }
        boolean ok = this.userRepo.updateUserStatus(userId, status);
        this.auditService.log(admin.getUsername(), "USER_STATUS " + userId + " -> " + status,
                ok ? AuditService.SUCCESS : AuditService.FAILED);
        if (ok) {
            this.notificationService.send(userId, "Tai khoan cua ban da duoc chuyen sang trang thai " + status + ".");
        }
        return ok;
    }

    /** Admin cap tai khoan Seller. */
    public User createSellerAccount(User admin, String username, String password, String fullName, String email, String phone) {
        if (this.userRepo.findByUsername(username) != null) {
            throw new BusinessException("Ten dang nhap da ton tai.");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("Mat khau phai co it nhat 6 ky tu.");
        }
        User seller = User.create(Role.SELLER, null, username, PasswordUtil.hash(password), fullName, email, phone,
                "seller.png", UserStatus.ACTIVE);
        if (!seller.register()) {
            throw new BusinessException("Thong tin tai khoan khong hop le.");
        }
        this.userRepo.insert(seller);
        this.auditService.log(admin.getUsername(), "CREATE_SELLER " + seller.getId(), AuditService.SUCCESS);
        return seller;
    }

    // ============================ STADIUM / SECTION / SEAT ============================

    /** Nhat: getAllStadiums. */
    public List<Stadium> getAllStadiums() {
        return this.stadiumRepo.findAll();
    }

    /** Nhat: createStadium (id duoc cap tu dong). */
    public Stadium createStadium(User admin, String name, String address, String city, int capacity) {
        if (name == null || name.trim().isEmpty() || capacity <= 0) {
            throw new BusinessException("Ten san khong duoc trong va suc chua phai > 0.");
        }
        Stadium stadium = new Stadium(null, name.trim(), address, city, capacity);
        this.stadiumRepo.insert(stadium);
        this.auditService.log(admin.getUsername(), "CREATE_STADIUM " + stadium.getId(), AuditService.SUCCESS);
        return stadium;
    }

    /** San kem khu vuc va ghe. */
    public Stadium getStadiumLayout(String stadiumId) {
        Stadium stadium = this.stadiumRepo.findById(stadiumId);
        if (stadium == null) {
            throw new BusinessException("Khong tim thay san " + stadiumId);
        }
        for (Section section : this.sectionRepo.findByStadiumId(stadium.getId())) {
            section.setSeats(this.seatRepo.findBySectionId(section.getId()));
            stadium.addSection(section);
        }
        return stadium;
    }

    public List<Section> getSectionsByStadium(String stadiumId) {
        return this.sectionRepo.findByStadiumId(stadiumId);
    }

    public Section createSection(User admin, String stadiumId, String name, String type, int capacity, BigDecimal basePrice) {
        Stadium stadium = getStadiumLayout(stadiumId);
        if (capacity <= 0) {
            throw new BusinessException("Suc chua khu vuc phai > 0.");
        }
        if (stadium.getAllocatedCapacity() + capacity > stadium.getCapacity()) {
            throw new BusinessException("Tong suc chua cac khu vuc vuot suc chua san (" + stadium.getCapacity() + ").");
        }
        Section section = new Section(null, stadium.getId(), name, type.toUpperCase(), capacity, basePrice);
        this.sectionRepo.insert(section);
        this.auditService.log(admin.getUsername(), "CREATE_SECTION " + section.getId(), AuditService.SUCCESS);
        return section;
    }

    /** Khanh: Section.updateStatus - mo/dong khu vuc. */
    public Section updateSectionStatus(User admin, String sectionId, SectionStatus status) {
        Section section = requireSection(sectionId);
        section.updateStatus(status);
        this.sectionRepo.update(section);
        this.auditService.log(admin.getUsername(), "SECTION_STATUS " + sectionId + " -> " + status, AuditService.SUCCESS);
        return section;
    }

    /** Khanh: Section.updateSection - doi ten va suc chua. */
    public Section updateSection(User admin, String sectionId, String name, int capacity) {
        Section section = requireSection(sectionId);
        section.setSeats(this.seatRepo.findBySectionId(sectionId));
        section.updateSection(name == null || name.trim().isEmpty() ? section.getName() : name.trim(), capacity);
        this.sectionRepo.update(section);
        this.auditService.log(admin.getUsername(), "UPDATE_SECTION " + sectionId, AuditService.SUCCESS);
        return section;
    }

    /** Sinh mot hang ghe cho khu vuc (Khoa: SeatSection.addSeat co kiem tra suc chua). */
    public int generateSeats(User admin, String sectionId, String row, int count) {
        Section section = requireSection(sectionId);
        section.setSeats(this.seatRepo.findBySectionId(sectionId));
        String rowLabel = row.trim().toUpperCase();
        int start = 0;
        for (Seat seat : section.getSeats()) {
            if (seat.getRowNumber().equalsIgnoreCase(rowLabel)) {
                start = Math.max(start, seat.getSeatNumber());
            }
        }
        int created = 0;
        for (int i = 1; i <= count; i++) {
            Seat seat = new Seat(null, sectionId, rowLabel, start + i, SeatStatus.AVAILABLE, 1);
            if (!section.addSeat(seat)) {
                break;
            }
            this.seatRepo.insert(seat);
            created++;
        }
        this.auditService.log(admin.getUsername(), "GENERATE_SEATS " + sectionId + " " + rowLabel + " x" + created,
                AuditService.SUCCESS);
        return created;
    }

    /** Nhat: getAllSeats. */
    public List<Seat> getAllSeats() {
        return this.seatRepo.findAll();
    }

    public List<Seat> getSeatsBySection(String sectionId) {
        return this.seatRepo.findBySectionId(sectionId);
    }

    /** Nhat: updateSeatMaintenance. Ghe bao tri thi dung ban cac ve AVAILABLE cua ghe. */
    public int updateSeatMaintenance(User admin, String seatId, boolean isMaintenance) {
        SeatStatus status = isMaintenance ? SeatStatus.MAINTENANCE : SeatStatus.AVAILABLE;
        if (!this.seatRepo.updateStatus(seatId, status)) {
            throw new BusinessException("Khong tim thay ghe " + seatId);
        }
        this.auditService.log(admin.getUsername(), "SEAT_MAINTENANCE " + seatId + " -> " + status, AuditService.SUCCESS);
        return isMaintenance ? stopAvailableTicketsOfSeat(seatId) : 0;
    }

    /** Trieu: Seat.lock - Admin khoa ghe. */
    public int lockSeat(User admin, String seatId) {
        Seat seat = requireSeat(seatId);
        seat.lock();
        this.seatRepo.update(seat);
        this.auditService.log(admin.getUsername(), "LOCK_SEAT " + seatId, AuditService.SUCCESS);
        return stopAvailableTicketsOfSeat(seatId);
    }

    /** Trieu: Seat.unlock - Admin mo khoa ghe. */
    public void unlockSeat(User admin, String seatId) {
        Seat seat = requireSeat(seatId);
        seat.unlock();
        this.seatRepo.update(seat);
        this.auditService.log(admin.getUsername(), "UNLOCK_SEAT " + seatId, AuditService.SUCCESS);
    }

    // ============================ TEAM / MATCH ============================

    public List<Team> getAllTeams() {
        return this.teamRepo.findAll();
    }

    public Team createTeam(User admin, String name, String logo, String country) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("Ten doi bong khong duoc trong.");
        }
        Team team = new Team(null, name.trim(), logo, country);
        this.teamRepo.insert(team);
        this.auditService.log(admin.getUsername(), "CREATE_TEAM " + team.getId(), AuditService.SUCCESS);
        return team;
    }

    public List<Match> getAllMatches() {
        return this.matchController.getAllMatches();
    }

    // ============================ MONITOR / REPORT ============================

    public List<Booking> getAllBookings() {
        return this.bookingController.getAllBookings();
    }

    public List<Payment> getAllPayments() {
        return this.paymentController.getAllPayments();
    }

    public List<Transaction> getAllTransactions() {
        return this.paymentController.getAllTransactions();
    }

    public SalesReport getRevenueReport() {
        return this.reportService.buildReport(this.matchController.getAllMatches());
    }

    public List<AuditLog> getAuditLogs() {
        return this.auditService.findAll();
    }

    /** Kiem thu chong Double Booking (Trieu: DOUBLE BOOKING TEST). */
    public List<String> runDoubleBookingTest(User admin, String ticketId) {
        return this.bookingController.simulateDoubleBooking(admin, ticketId);
    }

    // ============================ HELPER ============================

    private int stopAvailableTicketsOfSeat(String seatId) {
        int stopped = 0;
        for (Ticket ticket : this.ticketRepo.findAll()) {
            if (ticket.getSeatId().equalsIgnoreCase(seatId) && ticket.isAvailable()) {
                int expectedVersion = ticket.getVersion();
                ticket.stopSale();
                if (this.ticketRepo.updateIfVersionMatches(ticket, expectedVersion)) {
                    stopped++;
                }
            }
        }
        return stopped;
    }

    private Section requireSection(String sectionId) {
        Section section = this.sectionRepo.findById(sectionId);
        if (section == null) {
            throw new BusinessException("Khong tim thay khu vuc " + sectionId);
        }
        return section;
    }

    private Seat requireSeat(String seatId) {
        Seat seat = this.seatRepo.findById(seatId);
        if (seat == null) {
            throw new BusinessException("Khong tim thay ghe " + seatId);
        }
        return seat;
    }
}
