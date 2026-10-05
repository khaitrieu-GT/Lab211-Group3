package view;

import controller.AdminController;
import controller.CancellationController;
import controller.MainController;
import java.math.BigDecimal;
import java.util.List;
import model.CancellationRequest;
import model.Payment;
import model.Section;
import model.Stadium;
import model.User;
import model.enums.SectionStatus;
import model.enums.UserStatus;
import util.MoneyUtil;

/**
 * Giao dien quan tri (Nhat: AdminView) - mo rong day du quyen Admin.
 */
public class AdminView extends BaseView {
    private final User admin;
    private final MainController mainController;
    private final AdminController controller;
    private final CancellationController cancellationController;
    private final MatchView matchView = new MatchView();
    private final OrderView orderView = new OrderView();
    private final TicketView ticketView = new TicketView();
    private final ProfileView profileView;

    public AdminView(User admin, MainController mainController) {
        this.admin = admin;
        this.mainController = mainController;
        this.controller = new AdminController();
        this.cancellationController = new CancellationController();
        this.profileView = new ProfileView(admin, mainController);
    }

    /** Nhat: displayMenu. */
    public void displayMenu() {
        while (true) {
            int unread = this.mainController.countUnreadNotifications(this.admin.getId());
            printHeader("HE THONG QUAN LY (ADMIN) - " + this.admin.getFullName()
                    + (unread > 0 ? "   (" + unread + " thong bao moi)" : ""));
            System.out.println("1. Quan ly nguoi dung & Seller");
            System.out.println("2. Quan ly san van dong, khu vuc, ghe");
            System.out.println("3. Quan ly doi bong");
            System.out.println("4. Xem tat ca tran dau");
            System.out.println("5. Giam sat booking / thanh toan / giao dich");
            System.out.println("6. Xu ly yeu cau huy ve");
            System.out.println("7. Bao cao doanh thu");
            System.out.println("8. Nhat ky he thong (audit log)");
            System.out.println("9. Kiem thu chong Double Booking");
            System.out.println("10. Thong bao");
            System.out.println("11. Ho so ca nhan");
            System.out.println("0. Dang xuat");
            int choice = this.input.readInt("Lua chon cua ban: ");
            switch (choice) {
                case 1: userMenu(); break;
                case 2: stadiumMenu(); break;
                case 3: teamMenu(); break;
                case 4: safely(() -> this.matchView.displayMatches(this.controller.getAllMatches())); break;
                case 5: monitorMenu(); break;
                case 6: cancellationMenu(); break;
                case 7: safely(() -> SellerView.renderReport(this.controller.getRevenueReport())); break;
                case 8: safely(this::renderAuditLogs); break;
                case 9: safely(this::handleDoubleBookingTest); break;
                case 10: safely(this.profileView::showNotifications); break;
                case 11: this.profileView.showProfileMenu(); break;
                case 0:
                    System.out.println("Da thoat giao dien Admin.");
                    return;
                default:
                    printError("Lua chon khong hop le!");
            }
        }
    }

    // ============================ USER ============================

    private void userMenu() {
        while (true) {
            printHeader("QUAN LY NGUOI DUNG");
            System.out.println("1. Xem danh sach nguoi dung & Seller");
            System.out.println("2. Khoa / Mo khoa tai khoan");
            System.out.println("3. Tao tai khoan Seller");
            System.out.println("0. Quay lai");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1: safely(this::renderUserList); break;
                case 2: safely(this::handleUserLock); break;
                case 3: safely(this::handleCreateSeller); break;
                case 0: return;
                default: printError("Lua chon khong hop le!");
            }
        }
    }

    private void renderUserList() {
        printSection("DANH SACH NGUOI DUNG");
        System.out.println(String.format("%-6s | %-12s | %-22s | %-24s | %-11s | %-6s | %s",
                "ID", "Username", "Ho ten", "Email", "Dien thoai", "Role", "Status"));
        printList(this.controller.getAllUsers(), "Chua co nguoi dung.");
    }

    private void handleUserLock() {
        renderUserList();
        String id = this.input.readNonEmpty("Nhap User ID can doi trang thai: ").toUpperCase();
        int choice = this.input.readIntInRange("1. ACTIVE (mo khoa)  2. LOCKED (khoa): ", 1, 2);
        UserStatus status = choice == 1 ? UserStatus.ACTIVE : UserStatus.LOCKED;
        if (this.controller.changeUserStatus(this.admin, id, status)) {
            printSuccess("Cap nhat trang thai User thanh cong!");
        } else {
            printError("That bai! Khong tim thay User ID.");
        }
    }

    private void handleCreateSeller() {
        String username = this.input.readNonEmpty("Username: ");
        String password = this.input.readNonEmpty("Mat khau (>= 6 ky tu): ");
        String fullName = this.input.readNonEmpty("Ho ten: ");
        String email = this.input.readNonEmpty("Email: ");
        String phone = this.input.readString("Dien thoai: ");
        User seller = this.controller.createSellerAccount(this.admin, username, password, fullName, email, phone);
        printSuccess("Tao Seller thanh cong: " + seller.getId());
    }

    // ============================ STADIUM ============================

    private void stadiumMenu() {
        while (true) {
            printHeader("QUAN LY SAN VAN DONG");
            System.out.println("1. Xem danh sach san van dong");
            System.out.println("2. Them san van dong moi");
            System.out.println("3. Xem khu vuc & ghe cua san");
            System.out.println("4. Them khu vuc");
            System.out.println("5. Sua khu vuc (ten, suc chua)");
            System.out.println("6. Mo / Dong khu vuc");
            System.out.println("7. Sinh hang ghe cho khu vuc");
            System.out.println("8. Quan ly trang thai ghe (Khoa/Mo bao tri)");
            System.out.println("9. Khoa / Mo khoa ghe");
            System.out.println("0. Quay lai");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1: safely(this::renderStadiumList); break;
                case 2: safely(this::handleAddStadium); break;
                case 3: safely(this::renderStadiumLayout); break;
                case 4: safely(this::handleAddSection); break;
                case 5: safely(this::handleUpdateSection); break;
                case 6: safely(this::handleSectionStatus); break;
                case 7: safely(this::handleGenerateSeats); break;
                case 8: safely(this::handleSeatMaintenance); break;
                case 9: safely(this::handleSeatLock); break;
                case 0: return;
                default: printError("Lua chon khong hop le!");
            }
        }
    }

    private void renderStadiumList() {
        printSection("DANH SACH SAN VAN DONG");
        printList(this.controller.getAllStadiums(), "Chua co san van dong.");
    }

    private void handleAddStadium() {
        String name = this.input.readNonEmpty("Nhap Ten san: ");
        String address = this.input.readNonEmpty("Nhap Dia chi: ");
        String city = this.input.readNonEmpty("Nhap Thanh pho: ");
        int capacity = this.input.readInt("Nhap Suc chua: ");
        Stadium stadium = this.controller.createStadium(this.admin, name, address, city, capacity);
        printSuccess("Them San van dong thanh cong: " + stadium.getId());
    }

    private String chooseStadium() {
        renderStadiumList();
        return this.input.readNonEmpty("Ma san: ").toUpperCase();
    }

    private void renderStadiumLayout() {
        Stadium stadium = this.controller.getStadiumLayout(chooseStadium());
        printSection(stadium.getName() + " - suc chua " + stadium.getCapacity()
                + ", da phan bo " + stadium.getAllocatedCapacity());
        for (Section section : stadium.getSections()) {
            System.out.println(section);
            for (model.Seat seat : section.getSeats()) {
                System.out.println("    " + seat);
            }
        }
    }

    private String chooseSection() {
        String stadiumId = chooseStadium();
        List<Section> sections = this.controller.getSectionsByStadium(stadiumId);
        printSection("KHU VUC");
        printList(sections, "San chua co khu vuc.");
        return this.input.readNonEmpty("Ma khu vuc: ").toUpperCase();
    }

    private void handleAddSection() {
        String stadiumId = chooseStadium();
        String name = this.input.readNonEmpty("Ten khu vuc: ");
        String type = this.input.readNonEmpty("Loai (VIP/STANDARD/...): ");
        int capacity = this.input.readInt("Suc chua: ");
        BigDecimal price = this.input.readMoney("Gia goc: ");
        Section section = this.controller.createSection(this.admin, stadiumId, name, type, capacity, price);
        printSuccess("Them khu vuc thanh cong: " + section.getId());
    }

    private void handleUpdateSection() {
        String sectionId = chooseSection();
        String name = this.input.readString("Ten moi (Enter = giu nguyen): ");
        int capacity = this.input.readInt("Suc chua moi: ");
        this.controller.updateSection(this.admin, sectionId, name, capacity);
        printSuccess("Cap nhat khu vuc thanh cong!");
    }

    private void handleSectionStatus() {
        String sectionId = chooseSection();
        int choice = this.input.readIntInRange("1. ACTIVE  2. CLOSED: ", 1, 2);
        Section section = this.controller.updateSectionStatus(this.admin, sectionId,
                choice == 1 ? SectionStatus.ACTIVE : SectionStatus.CLOSED);
        printSuccess("Khu vuc " + section.getId() + " -> " + section.getStatus());
    }

    private void handleGenerateSeats() {
        String sectionId = chooseSection();
        String row = this.input.readNonEmpty("Ten hang (vd A): ");
        int count = this.input.readInt("So ghe them vao hang: ");
        int created = this.controller.generateSeats(this.admin, sectionId, row, count);
        printSuccess("Da tao " + created + " ghe" + (created < count ? " (dat gioi han suc chua khu vuc)" : "") + ".");
    }

    private void handleSeatMaintenance() {
        String seatId = this.input.readNonEmpty("Nhap Seat ID: ").toUpperCase();
        boolean isLock = this.input.readYesNo("Chuyen sang MAINTENANCE? (N = AVAILABLE)");
        int stopped = this.controller.updateSeatMaintenance(this.admin, seatId, isLock);
        printSuccess("Cap nhat trang thai bao tri ghe thanh cong!"
                + (stopped > 0 ? " Da dung ban " + stopped + " ve cua ghe nay." : ""));
    }

    private void handleSeatLock() {
        String seatId = this.input.readNonEmpty("Nhap Seat ID: ").toUpperCase();
        int choice = this.input.readIntInRange("1. Khoa ghe  2. Mo khoa ghe: ", 1, 2);
        if (choice == 1) {
            int stopped = this.controller.lockSeat(this.admin, seatId);
            printSuccess("Da khoa ghe." + (stopped > 0 ? " Da dung ban " + stopped + " ve cua ghe nay." : ""));
        } else {
            this.controller.unlockSeat(this.admin, seatId);
            printSuccess("Da mo khoa ghe.");
        }
    }

    // ============================ TEAM ============================

    private void teamMenu() {
        while (true) {
            printHeader("QUAN LY DOI BONG");
            System.out.println("1. Xem danh sach doi bong");
            System.out.println("2. Them doi bong");
            System.out.println("0. Quay lai");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    safely(() -> printList(this.controller.getAllTeams(), "Chua co doi bong."));
                    break;
                case 2:
                    safely(() -> {
                        String name = this.input.readNonEmpty("Ten doi: ");
                        String logo = this.input.readString("Logo: ");
                        String country = this.input.readNonEmpty("Quoc gia: ");
                        printSuccess("Them doi thanh cong: " + this.controller.createTeam(this.admin, name, logo, country).getId());
                    });
                    break;
                case 0:
                    return;
                default:
                    printError("Lua chon khong hop le!");
            }
        }
    }

    // ============================ MONITOR ============================

    private void monitorMenu() {
        while (true) {
            printHeader("GIAM SAT GIAO DICH");
            System.out.println("1. Tat ca booking");
            System.out.println("2. Tat ca thanh toan");
            System.out.println("3. Tat ca giao dich cong thanh toan");
            System.out.println("0. Quay lai");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1: safely(() -> this.orderView.displayHistory(this.controller.getAllBookings())); break;
                case 2:
                    safely(() -> {
                        printSection("THANH TOAN");
                        printList(this.controller.getAllPayments(), "Chua co thanh toan.");
                    });
                    break;
                case 3:
                    safely(() -> {
                        printSection("GIAO DICH");
                        printList(this.controller.getAllTransactions(), "Chua co giao dich.");
                    });
                    break;
                case 0: return;
                default: printError("Lua chon khong hop le!");
            }
        }
    }

    // ============================ CANCELLATION ============================

    private void cancellationMenu() {
        while (true) {
            printHeader("XU LY YEU CAU HUY VE");
            System.out.println("1. Yeu cau dang cho xu ly");
            System.out.println("2. Duyet yeu cau (huy ve + hoan tien)");
            System.out.println("3. Tu choi yeu cau");
            System.out.println("4. Tat ca yeu cau");
            System.out.println("0. Quay lai");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    safely(() -> this.ticketView.displayCancellations(this.cancellationController.getPendingRequests()));
                    break;
                case 2: safely(this::handleApprove); break;
                case 3: safely(this::handleReject); break;
                case 4:
                    safely(() -> this.ticketView.displayCancellations(this.cancellationController.getAllRequests()));
                    break;
                case 0: return;
                default: printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleApprove() {
        List<CancellationRequest> pending = this.cancellationController.getPendingRequests();
        this.ticketView.displayCancellations(pending);
        if (pending.isEmpty()) {
            return;
        }
        String id = this.input.readNonEmpty("Ma yeu cau can duyet: ").toUpperCase();
        Payment payment = this.cancellationController.approveRequest(this.admin, id);
        printSuccess("Da duyet yeu cau " + id + "."
                + (payment == null ? "" : " Thanh toan " + payment.getId() + " da hoan "
                + MoneyUtil.format(payment.getRefundedAmount()) + " (" + payment.getStatus() + ")."));
    }

    private void handleReject() {
        List<CancellationRequest> pending = this.cancellationController.getPendingRequests();
        this.ticketView.displayCancellations(pending);
        if (pending.isEmpty()) {
            return;
        }
        String id = this.input.readNonEmpty("Ma yeu cau can tu choi: ").toUpperCase();
        String note = this.input.readNonEmpty("Ly do tu choi: ");
        this.cancellationController.rejectRequest(this.admin, id, note);
        printSuccess("Da tu choi yeu cau " + id + ".");
    }

    // ============================ OTHER ============================

    private void renderAuditLogs() {
        printSection("NHAT KY HE THONG (moi nhat truoc, toi da 30 dong)");
        List<model.AuditLog> logs = this.controller.getAuditLogs();
        printList(logs.size() > 30 ? logs.subList(0, 30) : logs, "Chua co nhat ky.");
    }

    private void handleDoubleBookingTest() {
        this.matchView.displayMatches(this.controller.getAllMatches());
        System.out.println("Chon mot ve AVAILABLE cua tran dang mo ban (xem bang Seller > Xem ve, vd TK0001).");
        String ticketId = this.input.readNonEmpty("Ma ve: ").toUpperCase();
        printSection("2 BUYER CUNG LUC GIU GHE " + ticketId);
        for (String line : this.controller.runDoubleBookingTest(this.admin, ticketId)) {
            System.out.println(line);
        }
    }
}
