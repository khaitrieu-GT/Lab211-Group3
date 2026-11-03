package controller;

import dto.SalesReport;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import model.AuditLog;
import model.Booking;
import model.CancellationRequest;
import model.Match;
import model.Payment;
import model.Section;
import model.Stadium;
import model.Team;
import model.Transaction;
import model.User;
import model.enums.SectionStatus;
import model.enums.UserStatus;
import service.AuditService;
import service.ReportService;
import util.MoneyUtil;
import view.AdminView;
import view.MatchView;
import view.OrderView;
import view.ReportView;
import view.StadiumView;
import view.TeamView;
import view.TicketView;
import view.UserView;

/**
 * Dieu khien luong menu quan tri (Nhat: AdminController + AdminView):
 * lay du lieu nhap tu View, goi cac controller nghiep vu, roi dua ket qua cho View hien thi.
 */
public class AdminController {

    private final User admin;
    private final AdminView adminView = new AdminView();
    private final UserView userView = new UserView();
    private final StadiumView stadiumView = new StadiumView();
    private final TeamView teamView = new TeamView();
    private final MatchView matchView = new MatchView();
    private final OrderView orderView = new OrderView();
    private final TicketView ticketView = new TicketView();
    private final ReportView reportView = new ReportView();
    private final UserController userController;
    private final StadiumController stadiumController;
    private final SeatManagementController seatManagementController;
    private final TeamController teamController;
    private final MatchController matchController = new MatchController();
    private final BookingController bookingController = new BookingController();
    private final PaymentController paymentController = new PaymentController();
    private final CancellationController cancellationController = new CancellationController();
    private final ProfileController profileController;
    private final ReportService reportService = new ReportService();
    private final AuditService auditService = new AuditService();

    public AdminController(User admin) {
        this.admin = admin;
        this.userController = new UserController(admin);
        this.stadiumController = new StadiumController(admin);
        this.seatManagementController = new SeatManagementController(admin);
        this.teamController = new TeamController(admin);
        this.profileController = new ProfileController(admin);
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
                case 9: this.runSafely(this.profileController::showNotifications); break;
                case 10: this.profileController.showProfileMenu(); break;
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
            int choice = this.userView.showUserMenu();
            switch (choice) {
                case 1: this.runSafely(() -> this.userView.displayUsers(this.userController.getAllUsers())); break;
                case 2: this.runSafely(this::handleUserLock); break;
                case 3: this.runSafely(this::handleCreateSeller); break;
                case 4: this.runSafely(this::handleSearchUser); break;
                case 5: this.runSafely(this::handleUpdateUser); break;
                case 6: this.runSafely(this::handleDeleteUser); break;
                case 0: return;
                default: this.userView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleUserLock() {
        this.userView.displayUsers(this.userController.getAllUsers());
        String id = this.userView.inputId("Nhap User ID can doi trang thai: ");
        int choice = this.userView.inputIntInRange("1. ACTIVE (mo khoa)  2. LOCKED (khoa): ", 1, 2);
        UserStatus status = choice == 1 ? UserStatus.ACTIVE : UserStatus.LOCKED;
        if (this.userController.changeUserStatus(id, status)) {
            this.userView.printSuccess("Cap nhat trang thai User thanh cong!");
        } else {
            this.userView.printError("That bai! Khong tim thay User ID.");
        }
    }

    private void handleCreateSeller() {
        String username = this.userView.inputNonEmpty("Username: ");
        String password = this.userView.inputNonEmpty("Mat khau (>= 6 ky tu): ");
        String fullName = this.userView.inputNonEmpty("Ho ten: ");
        String email = this.userView.inputNonEmpty("Email: ");
        String phone = this.userView.inputString("Dien thoai: ");
        User seller = this.userController.createSellerAccount(username, password, fullName, email, phone);
        this.userView.printSuccess("Tao Seller thanh cong: " + seller.getId());
    }

    private void handleSearchUser() {
        String keyword = this.userView.inputString("Tu khoa (ma/username/ho ten/email): ");
        this.userView.displayUsers(this.userController.searchUsers(keyword));
    }

    private void handleUpdateUser() {
        this.userView.displayUsers(this.userController.getAllUsers());
        String id = this.userView.inputId("Nhap User ID can cap nhat: ");
        this.userView.showMessage("(Bo trong de giu nguyen)");
        String fullName = this.userView.inputString("Ho ten moi: ");
        String email = this.userView.inputString("Email moi: ");
        String phone = this.userView.inputString("Dien thoai moi: ");
        User user = this.userController.updateUserInfo(id, fullName, email, phone);
        this.userView.printSuccess("Da cap nhat: " + user.getUsername());
    }

    private void handleDeleteUser() {
        this.userView.displayUsers(this.userController.getAllUsers());
        String id = this.userView.inputId("Nhap User ID can xoa: ");
        if (this.userView.confirm("Xac nhan xoa tai khoan " + id)) {
            this.userController.deleteUser(id);
            this.userView.printSuccess("Da xoa tai khoan " + id + " (trang thai DELETED, giu lich su giao dich).");
        }
    }

    // ---------------------------- STADIUM ----------------------------

    private void runStadiumMenu() {
        while (true) {
            int choice = this.stadiumView.showStadiumMenu();
            switch (choice) {
                case 1: this.runSafely(() -> this.stadiumView.displayStadiums(this.stadiumController.getAllStadiums())); break;
                case 2: this.runSafely(this::handleAddStadium); break;
                case 3: this.runSafely(() -> this.stadiumView.displayStadiumLayout(this.stadiumController.getStadiumLayout(this.chooseStadium()))); break;
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
                default: this.stadiumView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleAddStadium() {
        String name = this.stadiumView.inputNonEmpty("Nhap Ten san: ");
        String address = this.stadiumView.inputNonEmpty("Nhap Dia chi: ");
        String city = this.stadiumView.inputNonEmpty("Nhap Thanh pho: ");
        int capacity = this.stadiumView.inputInt("Nhap Suc chua: ");
        Stadium stadium = this.stadiumController.createStadium(name, address, city, capacity);
        this.stadiumView.printSuccess("Them San van dong thanh cong: " + stadium.getId());
    }

    private String chooseStadium() {
        this.stadiumView.displayStadiums(this.stadiumController.getAllStadiums());
        return this.stadiumView.inputId("Ma san: ");
    }

    private String chooseSection() {
        String stadiumId = this.chooseStadium();
        this.stadiumView.printSection("KHU VUC");
        this.stadiumView.printList(this.stadiumController.getSectionsByStadium(stadiumId), "San chua co khu vuc.");
        return this.stadiumView.inputId("Ma khu vuc: ");
    }

    private void handleUpdateStadium() {
        String stadiumId = this.chooseStadium();
        this.stadiumView.showMessage("(Bo trong de giu nguyen; suc chua nhap 0 de giu nguyen)");
        String name = this.stadiumView.inputString("Ten san moi: ");
        String address = this.stadiumView.inputString("Dia chi moi: ");
        String city = this.stadiumView.inputString("Thanh pho moi: ");
        int capacity = this.stadiumView.inputInt("Suc chua moi: ");
        Stadium stadium = this.stadiumController.updateStadium(stadiumId, name, address, city, capacity);
        this.stadiumView.printSuccess("Cap nhat san thanh cong: " + stadium.getName());
    }

    private void handleDeleteStadium() {
        String stadiumId = this.chooseStadium();
        if (this.stadiumView.confirm("Xoa san " + stadiumId + " cung toan bo khu vuc va ghe")) {
            this.stadiumController.deleteStadium(stadiumId);
            this.stadiumView.printSuccess("Da xoa san " + stadiumId);
        }
    }

    private void handleAddSection() {
        String stadiumId = this.chooseStadium();
        String name = this.stadiumView.inputNonEmpty("Ten khu vuc: ");
        String type = this.stadiumView.inputNonEmpty("Loai (VIP/STANDARD/...): ");
        int capacity = this.stadiumView.inputInt("Suc chua: ");
        BigDecimal price = this.stadiumView.inputMoney("Gia goc: ");
        Section section = this.stadiumController.createSection(stadiumId, name, type, capacity, price);
        this.stadiumView.printSuccess("Them khu vuc thanh cong: " + section.getId());
    }

    private void handleUpdateSection() {
        String sectionId = this.chooseSection();
        String name = this.stadiumView.inputString("Ten moi (Enter = giu nguyen): ");
        int capacity = this.stadiumView.inputInt("Suc chua moi: ");
        this.stadiumController.updateSection(sectionId, name, capacity);
        this.stadiumView.printSuccess("Cap nhat khu vuc thanh cong!");
    }

    private void handleSectionStatus() {
        String sectionId = this.chooseSection();
        int choice = this.stadiumView.inputIntInRange("1. ACTIVE  2. CLOSED: ", 1, 2);
        Section section = this.stadiumController.updateSectionStatus(sectionId, choice == 1 ? SectionStatus.ACTIVE : SectionStatus.CLOSED);
        this.stadiumView.printSuccess("Khu vuc " + section.getId() + " -> " + section.getStatus());
    }

    private void handleDeleteSection() {
        String sectionId = this.chooseSection();
        if (this.stadiumView.confirm("Xoa khu vuc " + sectionId + " cung toan bo ghe")) {
            this.stadiumController.deleteSection(sectionId);
            this.stadiumView.printSuccess("Da xoa khu vuc " + sectionId);
        }
    }

    private void handleGenerateSeats() {
        String sectionId = this.chooseSection();
        String row = this.stadiumView.inputNonEmpty("Ten hang (vd A): ");
        int count = this.stadiumView.inputInt("So ghe them vao hang: ");
        int created = this.seatManagementController.generateSeats(sectionId, row, count);
        this.stadiumView.printSuccess("Da tao " + created + " ghe" + (created < count ? " (dat gioi han suc chua khu vuc)" : "") + ".");
    }

    private void handleSeatMaintenance() {
        String seatId = this.stadiumView.inputId("Nhap Seat ID: ");
        boolean isLock = this.stadiumView.confirm("Chuyen sang MAINTENANCE? (N = AVAILABLE)");
        int stopped = this.seatManagementController.updateSeatMaintenance(seatId, isLock);
        this.stadiumView.printSuccess("Cap nhat trang thai bao tri ghe thanh cong!"
                + (stopped > 0 ? " Da dung ban " + stopped + " ve cua ghe nay." : ""));
    }

    private void handleSeatLock() {
        String seatId = this.stadiumView.inputId("Nhap Seat ID: ");
        int choice = this.stadiumView.inputIntInRange("1. Khoa ghe  2. Mo khoa ghe: ", 1, 2);
        if (choice == 1) {
            int stopped = this.seatManagementController.lockSeat(seatId);
            this.stadiumView.printSuccess("Da khoa ghe." + (stopped > 0 ? " Da dung ban " + stopped + " ve cua ghe nay." : ""));
        } else {
            this.seatManagementController.unlockSeat(seatId);
            this.stadiumView.printSuccess("Da mo khoa ghe.");
        }
    }

    private void handleDeleteSeat() {
        String sectionId = this.chooseSection();
        this.stadiumView.printSection("GHE CUA KHU " + sectionId);
        this.stadiumView.printList(this.seatManagementController.getSeatsBySection(sectionId), "Khu vuc chua co ghe.");
        String seatId = this.stadiumView.inputId("Nhap Seat ID can xoa: ");
        if (this.stadiumView.confirm("Xac nhan xoa ghe " + seatId)) {
            this.seatManagementController.deleteSeat(seatId);
            this.stadiumView.printSuccess("Da xoa ghe " + seatId);
        }
    }

    // ---------------------------- TEAM ----------------------------

    private void runTeamMenu() {
        while (true) {
            int choice = this.teamView.showTeamMenu();
            switch (choice) {
                case 1: this.runSafely(this::handleViewTeams); break;
                case 2: this.runSafely(this::handleAddTeam); break;
                case 3: this.runSafely(this::handleUpdateTeam); break;
                case 4: this.runSafely(this::handleDeleteTeam); break;
                case 0: return;
                default: this.teamView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleViewTeams() {
        this.teamView.printSection("DANH SACH DOI BONG");
        this.teamView.printList(this.teamController.getAllTeams(), "Chua co doi bong.");
    }

    private void handleAddTeam() {
        String name = this.teamView.inputNonEmpty("Ten doi: ");
        String logo = this.teamView.inputString("Logo: ");
        String country = this.teamView.inputNonEmpty("Quoc gia: ");
        this.teamView.printSuccess("Them doi thanh cong: " + this.teamController.createTeam(name, logo, country).getId());
    }

    private void handleUpdateTeam() {
        this.handleViewTeams();
        String teamId = this.teamView.inputId("Ma doi can sua: ");
        this.teamView.showMessage("(Bo trong de giu nguyen)");
        String name = this.teamView.inputString("Ten doi moi: ");
        String logo = this.teamView.inputString("Logo moi: ");
        String country = this.teamView.inputString("Quoc gia moi: ");
        Team team = this.teamController.updateTeam(teamId, name, logo, country);
        this.teamView.printSuccess("Da cap nhat: " + team);
    }

    private void handleDeleteTeam() {
        this.handleViewTeams();
        String teamId = this.teamView.inputId("Ma doi can xoa: ");
        if (this.teamView.confirm("Xac nhan xoa doi " + teamId)) {
            this.teamController.deleteTeam(teamId);
            this.teamView.printSuccess("Da xoa doi " + teamId);
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
    // TRA CUU CHO MENU GIAM SAT / BAO CAO
    // =====================================================================

    public List<Match> getAllMatches() {
        return this.matchController.getAllMatches();
    }

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
}
