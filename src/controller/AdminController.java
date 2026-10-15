package controller;

import dto.SalesReport;
import exception.BusinessException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import model.AuditLog;
import model.Booking;
import model.CancellationRequest;
import model.Match;
import model.Payment;
import model.Seat;
import model.Section;
import model.Stadium;
import model.Team;
import model.Ticket;
import model.Transaction;
import model.User;
import model.enums.MatchStatus;
import model.enums.Role;
import model.enums.SeatStatus;
import model.enums.SectionStatus;
import model.enums.UserStatus;
import repository.MatchRepository;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.StadiumRepository;
import repository.TeamRepository;
import repository.TicketRepository;
import repository.UserRepository;
import service.AccountService;
import service.AuditService;
import service.NotificationService;
import service.ReportService;
import util.MoneyUtil;
import util.ValidationUtil;
import view.AdminView;
import view.MatchView;
import view.OrderView;
import view.ReportView;
import view.TicketView;

/**
 * Controller cua Admin (Nhat: AdminController + AdminView): dieu khien luong menu quan tri va
 * thuc hien nghiep vu CRUD cho nguoi dung, san, khu vuc, ghe, doi bong; giam sat, bao cao, xu ly huy ve.
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

    private final MatchRepository matchRepo;
    private final AccountService accountService;
    private final CancellationController cancellationController;
    private final ProfileController profileController;

    private final User admin;
    private final AdminView adminView = new AdminView();
    private final MatchView matchView = new MatchView();
    private final OrderView orderView = new OrderView();
    private final TicketView ticketView = new TicketView();
    private final ReportView reportView = new ReportView();

    public AdminController(User admin) {
        this.admin = admin;
        this.matchRepo = new MatchRepository();
        this.accountService = new AccountService();
        this.cancellationController = new CancellationController();
        this.profileController = new ProfileController(admin);
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

    // =====================================================================
    // LUONG (FLOW): controller lay du lieu tu View -> xu ly -> tra ket qua cho View
    // =====================================================================

    /** Nhat: displayMenu - vong lap menu Admin. */
    public void start() {
        while (true) {
            String title = this.adminView.withUnread("HE THONG QUAN LY (ADMIN) - " + this.admin.getFullName(),
                    this.profileController.countUnreadNotifications());
            int choice = this.adminView.showMainMenu(title);
            switch (choice) {
                case 1: this.runUserMenu(); break;
                case 2: this.runStadiumMenu(); break;
                case 3: this.runTeamMenu(); break;
                case 4: this.runSafely(() -> this.matchView.displayMatches(this.getAllMatches())); break;
                case 5: this.runMonitorMenu(); break;
                case 6: this.runCancellationMenu(); break;
                case 7: this.runSafely(() -> this.reportView.displayReport(this.getRevenueReport())); break;
                case 8: this.runSafely(() -> this.adminView.displayAuditLogs(this.getAuditLogs())); break;
                case 9: this.runSafely(this::handleDoubleBookingTest); break;
                case 10: this.runSafely(this.profileController::showNotifications); break;
                case 11: this.profileController.showProfileMenu(); break;
                case 0:
                    this.adminView.showMessage("Da thoat giao dien Admin.");
                    return;
                default:
                    this.adminView.printError("Lua chon khong hop le!");
            }
        }
    }

    // ---------------------------- USER ----------------------------

    private void runUserMenu() {
        while (true) {
            int choice = this.adminView.showUserMenu();
            switch (choice) {
                case 1: this.runSafely(() -> this.adminView.displayUsers(this.getAllUsers())); break;
                case 2: this.runSafely(this::handleUserLock); break;
                case 3: this.runSafely(this::handleCreateSeller); break;
                case 4: this.runSafely(this::handleSearchUser); break;
                case 5: this.runSafely(this::handleUpdateUser); break;
                case 6: this.runSafely(this::handleDeleteUser); break;
                case 0: return;
                default: this.adminView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleUserLock() {
        this.adminView.displayUsers(this.getAllUsers());
        String id = this.adminView.inputId("Nhap User ID can doi trang thai: ");
        int choice = this.adminView.inputIntInRange("1. ACTIVE (mo khoa)  2. LOCKED (khoa): ", 1, 2);
        UserStatus status = choice == 1 ? UserStatus.ACTIVE : UserStatus.LOCKED;
        if (this.changeUserStatus(id, status)) {
            this.adminView.printSuccess("Cap nhat trang thai User thanh cong!");
        } else {
            this.adminView.printError("That bai! Khong tim thay User ID.");
        }
    }

    private void handleCreateSeller() {
        String username = this.adminView.inputNonEmpty("Username: ");
        String password = this.adminView.inputNonEmpty("Mat khau (>= 6 ky tu): ");
        String fullName = this.adminView.inputNonEmpty("Ho ten: ");
        String email = this.adminView.inputNonEmpty("Email: ");
        String phone = this.adminView.inputString("Dien thoai: ");
        User seller = this.createSellerAccount(username, password, fullName, email, phone);
        this.adminView.printSuccess("Tao Seller thanh cong: " + seller.getId());
    }

    private void handleSearchUser() {
        String keyword = this.adminView.inputString("Tu khoa (ma/username/ho ten/email): ");
        this.adminView.displayUsers(this.searchUsers(keyword));
    }

    private void handleUpdateUser() {
        this.adminView.displayUsers(this.getAllUsers());
        String id = this.adminView.inputId("Nhap User ID can cap nhat: ");
        this.adminView.showMessage("(Bo trong de giu nguyen)");
        String fullName = this.adminView.inputString("Ho ten moi: ");
        String email = this.adminView.inputString("Email moi: ");
        String phone = this.adminView.inputString("Dien thoai moi: ");
        User user = this.updateUserInfo(id, fullName, email, phone);
        this.adminView.printSuccess("Da cap nhat: " + user.getUsername());
    }

    private void handleDeleteUser() {
        this.adminView.displayUsers(this.getAllUsers());
        String id = this.adminView.inputId("Nhap User ID can xoa: ");
        if (this.adminView.confirm("Xac nhan xoa tai khoan " + id)) {
            this.deleteUser(id);
            this.adminView.printSuccess("Da xoa tai khoan " + id + " (trang thai DELETED, giu lich su giao dich).");
        }
    }

    // ---------------------------- STADIUM ----------------------------

    private void runStadiumMenu() {
        while (true) {
            int choice = this.adminView.showStadiumMenu();
            switch (choice) {
                case 1: this.runSafely(() -> this.adminView.displayStadiums(this.getAllStadiums())); break;
                case 2: this.runSafely(this::handleAddStadium); break;
                case 3: this.runSafely(() -> this.adminView.displayStadiumLayout(this.getStadiumLayout(this.chooseStadium()))); break;
                case 4: this.runSafely(this::handleAddSection); break;
                case 5: this.runSafely(this::handleUpdateSection); break;
                case 6: this.runSafely(this::handleSectionStatus); break;
                case 7: this.runSafely(this::handleGenerateSeats); break;
                case 8: this.runSafely(this::handleSeatMaintenance); break;
                case 9: this.runSafely(this::handleSeatLock); break;
                case 10: this.runSafely(this::handleUpdateStadium); break;
                case 11: this.runSafely(this::handleDeleteStadium); break;
                case 12: this.runSafely(this::handleDeleteSection); break;
                case 13: this.runSafely(this::handleDeleteSeat); break;
                case 0: return;
                default: this.adminView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleAddStadium() {
        String name = this.adminView.inputNonEmpty("Nhap Ten san: ");
        String address = this.adminView.inputNonEmpty("Nhap Dia chi: ");
        String city = this.adminView.inputNonEmpty("Nhap Thanh pho: ");
        int capacity = this.adminView.inputInt("Nhap Suc chua: ");
        Stadium stadium = this.createStadium(name, address, city, capacity);
        this.adminView.printSuccess("Them San van dong thanh cong: " + stadium.getId());
    }

    private String chooseStadium() {
        this.adminView.displayStadiums(this.getAllStadiums());
        return this.adminView.inputId("Ma san: ");
    }

    private String chooseSection() {
        String stadiumId = this.chooseStadium();
        this.adminView.printSection("KHU VUC");
        this.adminView.printList(this.getSectionsByStadium(stadiumId), "San chua co khu vuc.");
        return this.adminView.inputId("Ma khu vuc: ");
    }

    private void handleUpdateStadium() {
        String stadiumId = this.chooseStadium();
        this.adminView.showMessage("(Bo trong de giu nguyen; suc chua nhap 0 de giu nguyen)");
        String name = this.adminView.inputString("Ten san moi: ");
        String address = this.adminView.inputString("Dia chi moi: ");
        String city = this.adminView.inputString("Thanh pho moi: ");
        int capacity = this.adminView.inputInt("Suc chua moi: ");
        Stadium stadium = this.updateStadium(stadiumId, name, address, city, capacity);
        this.adminView.printSuccess("Cap nhat san thanh cong: " + stadium.getName());
    }

    private void handleDeleteStadium() {
        String stadiumId = this.chooseStadium();
        if (this.adminView.confirm("Xoa san " + stadiumId + " cung toan bo khu vuc va ghe")) {
            this.deleteStadium(stadiumId);
            this.adminView.printSuccess("Da xoa san " + stadiumId);
        }
    }

    private void handleAddSection() {
        String stadiumId = this.chooseStadium();
        String name = this.adminView.inputNonEmpty("Ten khu vuc: ");
        String type = this.adminView.inputNonEmpty("Loai (VIP/STANDARD/...): ");
        int capacity = this.adminView.inputInt("Suc chua: ");
        BigDecimal price = this.adminView.inputMoney("Gia goc: ");
        Section section = this.createSection(stadiumId, name, type, capacity, price);
        this.adminView.printSuccess("Them khu vuc thanh cong: " + section.getId());
    }

    private void handleUpdateSection() {
        String sectionId = this.chooseSection();
        String name = this.adminView.inputString("Ten moi (Enter = giu nguyen): ");
        int capacity = this.adminView.inputInt("Suc chua moi: ");
        this.updateSection(sectionId, name, capacity);
        this.adminView.printSuccess("Cap nhat khu vuc thanh cong!");
    }

    private void handleSectionStatus() {
        String sectionId = this.chooseSection();
        int choice = this.adminView.inputIntInRange("1. ACTIVE  2. CLOSED: ", 1, 2);
        Section section = this.updateSectionStatus(sectionId, choice == 1 ? SectionStatus.ACTIVE : SectionStatus.CLOSED);
        this.adminView.printSuccess("Khu vuc " + section.getId() + " -> " + section.getStatus());
    }

    private void handleDeleteSection() {
        String sectionId = this.chooseSection();
        if (this.adminView.confirm("Xoa khu vuc " + sectionId + " cung toan bo ghe")) {
            this.deleteSection(sectionId);
            this.adminView.printSuccess("Da xoa khu vuc " + sectionId);
        }
    }

    private void handleGenerateSeats() {
        String sectionId = this.chooseSection();
        String row = this.adminView.inputNonEmpty("Ten hang (vd A): ");
        int count = this.adminView.inputInt("So ghe them vao hang: ");
        int created = this.generateSeats(sectionId, row, count);
        this.adminView.printSuccess("Da tao " + created + " ghe" + (created < count ? " (dat gioi han suc chua khu vuc)" : "") + ".");
    }

    private void handleSeatMaintenance() {
        String seatId = this.adminView.inputId("Nhap Seat ID: ");
        boolean isLock = this.adminView.confirm("Chuyen sang MAINTENANCE? (N = AVAILABLE)");
        int stopped = this.updateSeatMaintenance(seatId, isLock);
        this.adminView.printSuccess("Cap nhat trang thai bao tri ghe thanh cong!"
                + (stopped > 0 ? " Da dung ban " + stopped + " ve cua ghe nay." : ""));
    }

    private void handleSeatLock() {
        String seatId = this.adminView.inputId("Nhap Seat ID: ");
        int choice = this.adminView.inputIntInRange("1. Khoa ghe  2. Mo khoa ghe: ", 1, 2);
        if (choice == 1) {
            int stopped = this.lockSeat(seatId);
            this.adminView.printSuccess("Da khoa ghe." + (stopped > 0 ? " Da dung ban " + stopped + " ve cua ghe nay." : ""));
        } else {
            this.unlockSeat(seatId);
            this.adminView.printSuccess("Da mo khoa ghe.");
        }
    }

    private void handleDeleteSeat() {
        String sectionId = this.chooseSection();
        this.adminView.printSection("GHE CUA KHU " + sectionId);
        this.adminView.printList(this.getSeatsBySection(sectionId), "Khu vuc chua co ghe.");
        String seatId = this.adminView.inputId("Nhap Seat ID can xoa: ");
        if (this.adminView.confirm("Xac nhan xoa ghe " + seatId)) {
            this.deleteSeat(seatId);
            this.adminView.printSuccess("Da xoa ghe " + seatId);
        }
    }

    // ---------------------------- TEAM ----------------------------

    private void runTeamMenu() {
        while (true) {
            int choice = this.adminView.showTeamMenu();
            switch (choice) {
                case 1: this.runSafely(this::handleViewTeams); break;
                case 2: this.runSafely(this::handleAddTeam); break;
                case 3: this.runSafely(this::handleUpdateTeam); break;
                case 4: this.runSafely(this::handleDeleteTeam); break;
                case 0: return;
                default: this.adminView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleViewTeams() {
        this.adminView.printSection("DANH SACH DOI BONG");
        this.adminView.printList(this.getAllTeams(), "Chua co doi bong.");
    }

    private void handleAddTeam() {
        String name = this.adminView.inputNonEmpty("Ten doi: ");
        String logo = this.adminView.inputString("Logo: ");
        String country = this.adminView.inputNonEmpty("Quoc gia: ");
        this.adminView.printSuccess("Them doi thanh cong: " + this.createTeam(name, logo, country).getId());
    }

    private void handleUpdateTeam() {
        this.handleViewTeams();
        String teamId = this.adminView.inputId("Ma doi can sua: ");
        this.adminView.showMessage("(Bo trong de giu nguyen)");
        String name = this.adminView.inputString("Ten doi moi: ");
        String logo = this.adminView.inputString("Logo moi: ");
        String country = this.adminView.inputString("Quoc gia moi: ");
        Team team = this.updateTeam(teamId, name, logo, country);
        this.adminView.printSuccess("Da cap nhat: " + team);
    }

    private void handleDeleteTeam() {
        this.handleViewTeams();
        String teamId = this.adminView.inputId("Ma doi can xoa: ");
        if (this.adminView.confirm("Xac nhan xoa doi " + teamId)) {
            this.deleteTeam(teamId);
            this.adminView.printSuccess("Da xoa doi " + teamId);
        }
    }

    // ---------------------------- MONITOR / CANCELLATION ----------------------------

    private void runMonitorMenu() {
        while (true) {
            int choice = this.adminView.showMonitorMenu();
            switch (choice) {
                case 1: this.runSafely(() -> this.orderView.displayHistory(this.getAllBookings())); break;
                case 2:
                    this.runSafely(() -> {
                        this.adminView.printSection("THANH TOAN");
                        this.adminView.printList(this.getAllPayments(), "Chua co thanh toan.");
                    });
                    break;
                case 3:
                    this.runSafely(() -> {
                        this.adminView.printSection("GIAO DICH");
                        this.adminView.printList(this.getAllTransactions(), "Chua co giao dich.");
                    });
                    break;
                case 0: return;
                default: this.adminView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void runCancellationMenu() {
        while (true) {
            int choice = this.adminView.showCancellationMenu();
            switch (choice) {
                case 1:
                    this.runSafely(() -> this.ticketView.displayCancellations(this.cancellationController.getPendingRequests()));
                    break;
                case 2: this.runSafely(this::handleApprove); break;
                case 3: this.runSafely(this::handleReject); break;
                case 4:
                    this.runSafely(() -> this.ticketView.displayCancellations(this.cancellationController.getAllRequests()));
                    break;
                case 0: return;
                default: this.adminView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleApprove() {
        List<CancellationRequest> pending = this.cancellationController.getPendingRequests();
        this.ticketView.displayCancellations(pending);
        if (pending.isEmpty()) {
            return;
        }
        String id = this.adminView.inputId("Ma yeu cau can duyet: ");
        Payment payment = this.cancellationController.approveRequest(this.admin, id);
        this.adminView.printSuccess("Da duyet yeu cau " + id + "."
                + (payment == null ? "" : " Thanh toan " + payment.getId() + " da hoan "
                + MoneyUtil.format(payment.getRefundedAmount()) + " (" + payment.getStatus() + ")."));
    }

    private void handleReject() {
        List<CancellationRequest> pending = this.cancellationController.getPendingRequests();
        this.ticketView.displayCancellations(pending);
        if (pending.isEmpty()) {
            return;
        }
        String id = this.adminView.inputId("Ma yeu cau can tu choi: ");
        String note = this.adminView.inputNonEmpty("Ly do tu choi: ");
        this.cancellationController.rejectRequest(this.admin, id, note);
        this.adminView.printSuccess("Da tu choi yeu cau " + id + ".");
    }

    private void handleDoubleBookingTest() {
        this.matchView.displayMatches(this.getAllMatches());
        this.adminView.showMessage("Chon mot ve AVAILABLE cua tran dang mo ban (xem bang Seller > Xem ve, vd TK0001).");
        String ticketId = this.adminView.inputId("Ma ve: ");
        this.adminView.displayDoubleBookingResult(ticketId, this.runDoubleBookingTest(ticketId));
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (NoSuchElementException e) {
            throw e; // het du lieu vao: de MainController ket thuc chuong trinh
        } catch (RuntimeException e) {
            this.adminView.printError(e.getMessage());
        }
    }

    // =====================================================================
    // NGHIEP VU
    // =====================================================================


    // ============================ USER (CRUD) ============================

    /** Nhat: getAllUsers. */
    public List<User> getAllUsers() {
        return this.userRepo.findAll();
    }

    /** Tim theo ma, username, ho ten hoac email (khong phan biet hoa thuong). */
    public List<User> searchUsers(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        List<User> result = new ArrayList<>();
        for (User user : this.userRepo.findAll()) {
            String text = (user.getId() + " " + user.getUsername() + " " + user.getFullName() + " "
                    + user.getEmail()).toLowerCase();
            if (text.contains(key)) {
                result.add(user);
            }
        }
        return result;
    }

    /** Nhat: changeUserStatus - khoa/mo khoa tai khoan. */
    public boolean changeUserStatus(String userId, UserStatus status) {
        if (this.admin.getId().equalsIgnoreCase(userId)) {
            throw new BusinessException("Khong the tu doi trang thai tai khoan cua chinh minh.");
        }
        User user = this.userRepo.findById(userId);
        if (user != null && user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException("Tai khoan da bi xoa, khong the doi trang thai.");
        }
        boolean ok = this.userRepo.updateUserStatus(userId, status);
        this.auditService.log(this.admin.getUsername(), "USER_STATUS " + userId + " -> " + status,
                ok ? AuditService.SUCCESS : AuditService.FAILED);
        if (ok) {
            this.notificationService.send(userId, "Tai khoan cua ban da duoc chuyen sang trang thai " + status + ".");
        }
        return ok;
    }

    /** Admin cap tai khoan Seller. */
    public User createSellerAccount(String username, String password, String fullName, String email, String phone) {
        User seller = this.accountService.createAccount(Role.SELLER, username, password, fullName, email, phone, "seller.png");
        this.auditService.log(this.admin.getUsername(), "CREATE_SELLER " + seller.getId(), AuditService.SUCCESS);
        return seller;
    }

    /** Admin sua ho ten / email / dien thoai cua nguoi dung. */
    public User updateUserInfo(String userId, String fullName, String email, String phone) {
        User user = this.requireUser(userId);
        if (!ValidationUtil.isBlank(email)) {
            User owner = this.userRepo.findByEmail(email);
            if (owner != null && !owner.getId().equalsIgnoreCase(user.getId())) {
                throw new BusinessException("Email da duoc tai khoan khac su dung.");
            }
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new BusinessException("So dien thoai phai gom 10 chu so va bat dau bang 0.");
        }
        user.updateProfile(fullName, email, phone, null);
        this.userRepo.update(user);
        this.auditService.log(this.admin.getUsername(), "UPDATE_USER " + userId, AuditService.SUCCESS);
        return user;
    }

    /**
     * Xoa tai khoan (xoa mem): chuyen trang thai DELETED, khong dang nhap duoc nua
     * nhung van giu lich su booking/ve de doi soat. Khong xoa chinh minh va Admin cuoi cung.
     */
    public void deleteUser(String userId) {
        User user = this.requireUser(userId);
        if (this.admin.getId().equalsIgnoreCase(user.getId())) {
            throw new BusinessException("Khong the tu xoa tai khoan cua chinh minh.");
        }
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException("Tai khoan da bi xoa truoc do.");
        }
        if (user.getRole() == Role.SELLER) {
            for (Match match : this.matchRepo.findBySellerId(user.getId())) {
                if (match.getStatus() == MatchStatus.OPEN && !match.hasStarted()) {
                    throw new BusinessException("Seller dang co tran " + match.getId() + " mo ban, hay dong ban truoc.");
                }
            }
        }
        user.markDeleted();
        this.userRepo.update(user);
        this.auditService.log(this.admin.getUsername(), "DELETE_USER " + userId, AuditService.SUCCESS);
    }

    // ============================ STADIUM (CRUD) ============================

    /** Nhat: getAllStadiums. */
    public List<Stadium> getAllStadiums() {
        return this.stadiumRepo.findAll();
    }

    /** Nhat: createStadium (id duoc cap tu dong). */
    public Stadium createStadium(String name, String address, String city, int capacity) {
        if (ValidationUtil.isBlank(name) || capacity <= 0) {
            throw new BusinessException("Ten san khong duoc trong va suc chua phai > 0.");
        }
        Stadium stadium = new Stadium(null, name.trim(), address, city, capacity);
        this.stadiumRepo.insert(stadium);
        this.auditService.log(this.admin.getUsername(), "CREATE_STADIUM " + stadium.getId(), AuditService.SUCCESS);
        return stadium;
    }

    /** San kem khu vuc va ghe. */
    public Stadium getStadiumLayout(String stadiumId) {
        Stadium stadium = this.requireStadium(stadiumId);
        for (Section section : this.sectionRepo.findByStadiumId(stadium.getId())) {
            section.setSeats(this.seatRepo.findBySectionId(section.getId()));
            stadium.addSection(section);
        }
        return stadium;
    }

    /** Sua thong tin san; suc chua moi khong duoc nho hon tong suc chua cac khu vuc da phan bo. */
    public Stadium updateStadium(String stadiumId, String name, String address, String city, int capacity) {
        Stadium stadium = this.getStadiumLayout(stadiumId);
        if (!ValidationUtil.isBlank(name)) {
            stadium.setName(name.trim());
        }
        if (!ValidationUtil.isBlank(address)) {
            stadium.setAddress(address.trim());
        }
        if (!ValidationUtil.isBlank(city)) {
            stadium.setCity(city.trim());
        }
        if (capacity < 0) {
            throw new BusinessException("Suc chua khong duoc am.");
        }
        if (capacity > 0) {
            if (capacity < stadium.getAllocatedCapacity()) {
                throw new BusinessException("Suc chua moi nho hon tong suc chua cac khu vuc ("
                        + stadium.getAllocatedCapacity() + ").");
            }
            stadium.setCapacity(capacity);
        }
        this.stadiumRepo.update(stadium);
        this.auditService.log(this.admin.getUsername(), "UPDATE_STADIUM " + stadiumId, AuditService.SUCCESS);
        return stadium;
    }

    /** Xoa san (kem khu vuc va ghe) khi chua co tran dau nao to chuc tai san. */
    public void deleteStadium(String stadiumId) {
        Stadium stadium = this.requireStadium(stadiumId);
        if (!this.matchRepo.findByStadiumId(stadium.getId()).isEmpty()) {
            throw new BusinessException("San dang duoc dung cho tran dau, khong the xoa.");
        }
        for (Section section : this.sectionRepo.findByStadiumId(stadium.getId())) {
            this.removeSectionWithSeats(section.getId());
        }
        this.stadiumRepo.delete(stadium.getId());
        this.auditService.log(this.admin.getUsername(), "DELETE_STADIUM " + stadiumId, AuditService.SUCCESS);
    }

    // ============================ SECTION (CRUD) ============================

    public List<Section> getSectionsByStadium(String stadiumId) {
        return this.sectionRepo.findByStadiumId(stadiumId);
    }

    public Section createSection(String stadiumId, String name, String type, int capacity, BigDecimal basePrice) {
        Stadium stadium = this.getStadiumLayout(stadiumId);
        if (capacity <= 0) {
            throw new BusinessException("Suc chua khu vuc phai > 0.");
        }
        if (stadium.getAllocatedCapacity() + capacity > stadium.getCapacity()) {
            throw new BusinessException("Tong suc chua cac khu vuc vuot suc chua san (" + stadium.getCapacity() + ").");
        }
        Section section = new Section(null, stadium.getId(), name, type.toUpperCase(), capacity, basePrice);
        this.sectionRepo.insert(section);
        this.auditService.log(this.admin.getUsername(), "CREATE_SECTION " + section.getId(), AuditService.SUCCESS);
        return section;
    }

    /** Khanh: Section.updateStatus - mo/dong khu vuc. */
    public Section updateSectionStatus(String sectionId, SectionStatus status) {
        Section section = this.requireSection(sectionId);
        section.updateStatus(status);
        this.sectionRepo.update(section);
        this.auditService.log(this.admin.getUsername(), "SECTION_STATUS " + sectionId + " -> " + status, AuditService.SUCCESS);
        return section;
    }

    /**
     * Khanh: Section.updateSection - doi ten va suc chua.
     * Suc chua moi: khong nho hon so ghe hien co va tong cac khu vuc khong vuot suc chua san.
     */
    public Section updateSection(String sectionId, String name, int capacity) {
        Section section = this.requireSection(sectionId);
        section.setSeats(this.seatRepo.findBySectionId(sectionId));
        Stadium stadium = this.getStadiumLayout(section.getStadiumId());
        int otherSections = stadium.getAllocatedCapacity() - section.getCapacity();
        if (capacity <= 0) {
            throw new BusinessException("Suc chua khu vuc phai > 0.");
        }
        if (otherSections + capacity > stadium.getCapacity()) {
            throw new BusinessException("Tong suc chua cac khu vuc vuot suc chua san (" + stadium.getCapacity() + ").");
        }
        try {
            section.updateSection(ValidationUtil.isBlank(name) ? section.getName() : name.trim(), capacity);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Suc chua moi nho hon so ghe hien co (" + section.getSeats().size() + ").");
        }
        this.sectionRepo.update(section);
        this.auditService.log(this.admin.getUsername(), "UPDATE_SECTION " + sectionId, AuditService.SUCCESS);
        return section;
    }

    /** Xoa khu vuc (kem ghe) khi chua co ve nao duoc tao cho khu vuc. */
    public void deleteSection(String sectionId) {
        Section section = this.requireSection(sectionId);
        if (!this.ticketRepo.findBySectionId(section.getId()).isEmpty()) {
            throw new BusinessException("Khu vuc da co ve mo ban, chi co the dong khu vuc.");
        }
        this.removeSectionWithSeats(section.getId());
        this.auditService.log(this.admin.getUsername(), "DELETE_SECTION " + sectionId, AuditService.SUCCESS);
    }

    // ============================ SEAT (CRUD) ============================

    /** Sinh mot hang ghe cho khu vuc (Khoa: SeatSection.addSeat co kiem tra suc chua). */
    public int generateSeats(String sectionId, String row, int count) {
        if (count <= 0) {
            throw new BusinessException("So ghe phai > 0.");
        }
        String rowLabel = row.trim().toUpperCase();
        if (!rowLabel.matches("[A-Z]+")) {
            throw new BusinessException("Ten hang chi gom chu cai (vd A, B, AA).");
        }
        Section section = this.requireSection(sectionId);
        section.setSeats(this.seatRepo.findBySectionId(sectionId));
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
        this.auditService.log(this.admin.getUsername(), "GENERATE_SEATS " + sectionId + " " + rowLabel + " x" + created,
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
    public int updateSeatMaintenance(String seatId, boolean isMaintenance) {
        SeatStatus status = isMaintenance ? SeatStatus.MAINTENANCE : SeatStatus.AVAILABLE;
        if (!this.seatRepo.updateStatus(seatId, status)) {
            throw new BusinessException("Khong tim thay ghe " + seatId);
        }
        this.auditService.log(this.admin.getUsername(), "SEAT_MAINTENANCE " + seatId + " -> " + status, AuditService.SUCCESS);
        return isMaintenance ? this.stopAvailableTicketsOfSeat(seatId) : 0;
    }

    /** Trieu: Seat.lock - Admin khoa ghe. */
    public int lockSeat(String seatId) {
        Seat seat = this.requireSeat(seatId);
        seat.lock();
        this.seatRepo.update(seat);
        this.auditService.log(this.admin.getUsername(), "LOCK_SEAT " + seatId, AuditService.SUCCESS);
        return this.stopAvailableTicketsOfSeat(seatId);
    }

    /** Trieu: Seat.unlock - Admin mo khoa ghe. */
    public void unlockSeat(String seatId) {
        Seat seat = this.requireSeat(seatId);
        seat.unlock();
        this.seatRepo.update(seat);
        this.auditService.log(this.admin.getUsername(), "UNLOCK_SEAT " + seatId, AuditService.SUCCESS);
    }

    /** Xoa ghe khi ghe chua tung duoc mo ban cho tran nao. */
    public void deleteSeat(String seatId) {
        Seat seat = this.requireSeat(seatId);
        if (!this.ticketRepo.findBySeatId(seat.getId()).isEmpty()) {
            throw new BusinessException("Ghe da co ve mo ban, chi co the khoa ghe.");
        }
        this.seatRepo.delete(seat.getId());
        this.auditService.log(this.admin.getUsername(), "DELETE_SEAT " + seatId, AuditService.SUCCESS);
    }

    // ============================ TEAM (CRUD) ============================

    public List<Team> getAllTeams() {
        return this.teamRepo.findAll();
    }

    public Team createTeam(String name, String logo, String country) {
        if (ValidationUtil.isBlank(name)) {
            throw new BusinessException("Ten doi bong khong duoc trong.");
        }
        for (Team existing : this.teamRepo.findAll()) {
            if (existing.getName().equalsIgnoreCase(name.trim())) {
                throw new BusinessException("Doi bong " + name.trim() + " da ton tai (" + existing.getId() + ").");
            }
        }
        Team team = new Team(null, name.trim(), logo, country);
        this.teamRepo.insert(team);
        this.auditService.log(this.admin.getUsername(), "CREATE_TEAM " + team.getId(), AuditService.SUCCESS);
        return team;
    }

    public Team updateTeam(String teamId, String name, String logo, String country) {
        Team team = this.requireTeam(teamId);
        if (!ValidationUtil.isBlank(name)) {
            for (Team existing : this.teamRepo.findAll()) {
                if (!existing.getId().equalsIgnoreCase(team.getId()) && existing.getName().equalsIgnoreCase(name.trim())) {
                    throw new BusinessException("Ten doi bong da ton tai (" + existing.getId() + ").");
                }
            }
            team.setName(name.trim());
        }
        if (!ValidationUtil.isBlank(logo)) {
            team.setLogo(logo.trim());
        }
        if (!ValidationUtil.isBlank(country)) {
            team.setCountry(country.trim());
        }
        this.teamRepo.update(team);
        this.auditService.log(this.admin.getUsername(), "UPDATE_TEAM " + teamId, AuditService.SUCCESS);
        return team;
    }

    /** Xoa doi bong khi doi chua tham gia tran dau nao. */
    public void deleteTeam(String teamId) {
        Team team = this.requireTeam(teamId);
        if (!this.matchRepo.findByTeamId(team.getId()).isEmpty()) {
            throw new BusinessException("Doi bong dang co tran dau, khong the xoa.");
        }
        this.teamRepo.delete(team.getId());
        this.auditService.log(this.admin.getUsername(), "DELETE_TEAM " + teamId, AuditService.SUCCESS);
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
    public List<String> runDoubleBookingTest(String ticketId) {
        return this.bookingController.simulateDoubleBooking(this.admin, ticketId);
    }

    // ============================ HELPER ============================

    private int stopAvailableTicketsOfSeat(String seatId) {
        int stopped = 0;
        for (Ticket ticket : this.ticketRepo.findBySeatId(seatId)) {
            if (ticket.isAvailable()) {
                int expectedVersion = ticket.getVersion();
                ticket.stopSale();
                if (this.ticketRepo.updateIfVersionMatches(ticket, expectedVersion)) {
                    stopped++;
                }
            }
        }
        return stopped;
    }

    private void removeSectionWithSeats(String sectionId) {
        for (Seat seat : this.seatRepo.findBySectionId(sectionId)) {
            this.seatRepo.delete(seat.getId());
        }
        this.sectionRepo.delete(sectionId);
    }

    private User requireUser(String userId) {
        User user = this.userRepo.findById(userId);
        if (user == null) {
            throw new BusinessException("Khong tim thay nguoi dung " + userId);
        }
        return user;
    }

    private Stadium requireStadium(String stadiumId) {
        Stadium stadium = this.stadiumRepo.findById(stadiumId);
        if (stadium == null) {
            throw new BusinessException("Khong tim thay san " + stadiumId);
        }
        return stadium;
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

    private Team requireTeam(String teamId) {
        Team team = this.teamRepo.findById(teamId);
        if (team == null) {
            throw new BusinessException("Khong tim thay doi bong " + teamId);
        }
        return team;
    }
}
