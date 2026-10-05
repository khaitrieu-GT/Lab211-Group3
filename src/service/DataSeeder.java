package service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import model.Match;
import model.Seat;
import model.Section;
import model.Stadium;
import model.Team;
import model.Ticket;
import model.User;
import model.enums.MatchStatus;
import model.enums.Role;
import model.enums.SeatStatus;
import model.enums.UserStatus;
import repository.AuditLogRepository;
import repository.BookingRepository;
import repository.BookingSeatRepository;
import repository.CancellationRequestRepository;
import repository.ETicketRepository;
import repository.MatchRepository;
import repository.NotificationRepository;
import repository.PaymentRepository;
import repository.SeatHoldRepository;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.StadiumRepository;
import repository.TeamRepository;
import repository.TicketRepository;
import repository.TransactionRepository;
import repository.UserRepository;
import util.PasswordUtil;

/**
 * Tao du lieu mau khi file CSV chua ton tai (xoa thu muc data/ de khoi phuc du lieu ban dau).
 * Du lieu goc lay tu data/*.csv cua Nhat va Main.java cua Khoa.
 */
public class DataSeeder {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepo = new UserRepository();
    private final StadiumRepository stadiumRepo = new StadiumRepository();
    private final SectionRepository sectionRepo = new SectionRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final MatchRepository matchRepo = new MatchRepository();
    private final TicketRepository ticketRepo = new TicketRepository();

    public void seedIfMissing() {
        if (!this.userRepo.exists()) {
            seedUsers();
        }
        if (!this.stadiumRepo.exists()) {
            seedStadiums();
        }
        if (!this.sectionRepo.exists()) {
            seedSections();
        }
        if (!this.seatRepo.exists()) {
            seedSeats();
        }
        if (!this.teamRepo.exists()) {
            seedTeams();
        }
        if (!this.matchRepo.exists()) {
            seedMatches();
        }
        if (!this.ticketRepo.exists()) {
            seedTickets();
        }
        new BookingRepository().ensureFile();
        new BookingSeatRepository().ensureFile();
        new SeatHoldRepository().ensureFile();
        new PaymentRepository().ensureFile();
        new TransactionRepository().ensureFile();
        new ETicketRepository().ensureFile();
        new CancellationRequestRepository().ensureFile();
        new NotificationRepository().ensureFile();
        new AuditLogRepository().ensureFile();
    }

    private void seedUsers() {
        String pw = PasswordUtil.hash(DEFAULT_PASSWORD);
        this.userRepo.insert(User.create(Role.ADMIN, "U001", "admin", pw, "System Admin", "admin@stadium.com",
                "0900000001", "admin.png", UserStatus.ACTIVE));
        this.userRepo.insert(User.create(Role.SELLER, "U002", "seller1", pw, "Nguyen Van Seller", "seller@stadium.com",
                "0900000002", "seller.png", UserStatus.ACTIVE));
        this.userRepo.insert(User.create(Role.BUYER, "U003", "buyer1", pw, "Tran Van Buyer", "buyer@gmail.com",
                "0900000003", "buyer.png", UserStatus.ACTIVE));
        this.userRepo.insert(User.create(Role.BUYER, "U004", "khoa", pw, "Dang Dang Khoa", "khoa@gmail.com",
                "0901234567", "avatar.jpg", UserStatus.ACTIVE));
        this.userRepo.insert(User.create(Role.SELLER, "U005", "seller2", pw, "Le Thi Ban Ve", "seller2@stadium.com",
                "0900000005", "seller.png", UserStatus.ACTIVE));
    }

    private void seedStadiums() {
        this.stadiumRepo.insert(new Stadium("STD01", "San Van Dong My Dinh", "My Dinh - Nam Tu Liem", "Ha Noi", 40000));
        this.stadiumRepo.insert(new Stadium("STD02", "Old Trafford", "Sir Matt Busby Way", "Manchester", 74000));
    }

    private void seedSections() {
        this.sectionRepo.insert(new Section("SEC01", "STD01", "Khan dai A (VIP)", "VIP", 5000, new BigDecimal("500000")));
        this.sectionRepo.insert(new Section("SEC02", "STD01", "Khan dai B", "STANDARD", 10000, new BigDecimal("300000")));
        this.sectionRepo.insert(new Section("SEC03", "STD02", "A", "STANDARD", 20, new BigDecimal("700000")));
        this.sectionRepo.insert(new Section("SEC04", "STD02", "VIP", "VIP", 10, new BigDecimal("1000000")));
    }

    private void seedSeats() {
        addRow("SEC01", "A", 5);
        addRow("SEC01", "B", 5);
        addRow("SEC02", "C", 5);
        addRow("SEC02", "D", 5);
        addRow("SEC03", "A", 3);
        addRow("SEC04", "V", 2);
        // Ghe A3 khu VIP dang bao tri (Nhat: SEAT003 MAINTENANCE)
        this.seatRepo.updateStatus("SEAT003", SeatStatus.MAINTENANCE);
    }

    private void addRow(String sectionId, String row, int count) {
        for (int i = 1; i <= count; i++) {
            this.seatRepo.insert(new Seat(null, sectionId, row, i, SeatStatus.AVAILABLE, 1));
        }
    }

    private void seedTeams() {
        this.teamRepo.insert(new Team("T001", "Manchester United", "mu.png", "England"));
        this.teamRepo.insert(new Team("T002", "Liverpool", "liverpool.png", "England"));
        this.teamRepo.insert(new Team("T003", "Viet Nam", "vn.png", "Viet Nam"));
        this.teamRepo.insert(new Team("T004", "Thai Lan", "th.png", "Thailand"));
        this.teamRepo.insert(new Team("T005", "Indonesia", "id.png", "Indonesia"));
    }

    private void seedMatches() {
        Match m1 = new Match("M001", "MU vs Liverpool", "Premier League", "T001", "T002", "STD02", "U002",
                LocalDate.of(2026, 12, 12), LocalTime.of(19, 30));
        m1.setStatus(MatchStatus.OPEN);
        Match m2 = new Match("M002", "Viet Nam vs Thai Lan", "AFF Cup", "T003", "T004", "STD01", "U002",
                LocalDate.of(2027, 1, 20), LocalTime.of(19, 0));
        m2.setStatus(MatchStatus.OPEN);
        Match m3 = new Match("M003", "Viet Nam vs Indonesia", "AFF Cup", "T003", "T005", "STD01", "U005",
                LocalDate.of(2027, 3, 15), LocalTime.of(20, 0));
        this.matchRepo.insert(m1);
        this.matchRepo.insert(m2);
        this.matchRepo.insert(m3);
    }

    private void seedTickets() {
        createTicketsForMatch("M001", "STD02");
        createTicketsForMatch("M002", "STD01");
    }

    private void createTicketsForMatch(String matchId, String stadiumId) {
        for (Section section : this.sectionRepo.findByStadiumId(stadiumId)) {
            for (Seat seat : this.seatRepo.findBySectionId(section.getId())) {
                if (seat.isAvailable()) {
                    this.ticketRepo.insert(new Ticket(null, matchId, seat.getId(), section.getId(),
                            section.getType(), section.getBasePrice()));
                }
            }
        }
    }
}
