package controller;

import dto.SalesReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import model.Booking;
import model.ETicket;
import model.Match;
import model.Section;
import model.Ticket;
import model.User;
import service.ReportService;
import util.MoneyUtil;
import view.MatchView;
import view.OrderView;
import view.ReportView;
import view.SellerView;
import view.TicketView;

/**
 * Dieu khien luong menu Seller (Khanh: SellerController + SellerView):
 * lay du lieu nhap tu View, goi cac controller nghiep vu, roi dua ket qua cho View hien thi.
 */
public class SellerController {

    private final User seller;
    private final SellerView sellerView = new SellerView();
    private final MatchView matchView = new MatchView();
    private final TicketView ticketView = new TicketView();
    private final OrderView orderView = new OrderView();
    private final ReportView reportView = new ReportView();
    private final MatchManagementController matchManagementController = new MatchManagementController();
    private final TicketManagementController ticketManagementController = new TicketManagementController();
    private final BookingController bookingController = new BookingController();
    private final TicketController ticketController = new TicketController();
    private final ReportService reportService = new ReportService();
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
        this.sellerView.printList(this.matchManagementController.getAllTeams(), "Chua co doi bong.");
        this.sellerView.printSection("SAN VAN DONG");
        this.sellerView.printList(this.matchManagementController.getAllStadiums(), "Chua co san.");
        String name = this.sellerView.inputNonEmpty("Ten tran: ");
        String competition = this.sellerView.inputNonEmpty("Giai dau: ");
        String home = this.sellerView.inputNonEmpty("Ma doi nha: ");
        String away = this.sellerView.inputNonEmpty("Ma doi khach: ");
        String stadium = this.sellerView.inputNonEmpty("Ma san: ");
        LocalDate date = this.sellerView.inputDate("Ngay thi dau (yyyy-MM-dd): ");
        LocalTime time = this.sellerView.inputTime("Gio thi dau (HH:mm): ");
        Match match = this.matchManagementController.createMatch(this.seller, name, competition, home, away, stadium, date, time);
        this.sellerView.printSuccess("Tao tran thanh cong: " + match.getId() + ". Hay tao ve va mo ban.");
    }

    private void handleViewMatches() {
        this.matchView.displayMatches(this.matchManagementController.getMatches(this.seller));
    }

    private String chooseMatch() {
        this.matchView.displayMatches(this.matchManagementController.getMatches(this.seller));
        return this.sellerView.inputId("Nhap ma tran: ");
    }

    private void handleUpdateMatch() {
        String matchId = this.chooseMatch();
        this.sellerView.showMessage("(Bo trong de giu nguyen)");
        String name = this.sellerView.inputString("Ten tran moi: ");
        String competition = this.sellerView.inputString("Giai dau moi: ");
        LocalDate date = this.sellerView.inputOptionalDate("Ngay moi (yyyy-MM-dd): ");
        LocalTime time = this.sellerView.inputOptionalTime("Gio moi (HH:mm): ");
        this.matchManagementController.updateMatch(this.seller, matchId, name, competition, date, time);
        this.sellerView.printSuccess("Cap nhat tran thanh cong!");
    }

    private void handleDeleteMatch() {
        String matchId = this.chooseMatch();
        if (this.sellerView.confirm("Xac nhan xoa tran " + matchId)) {
            this.matchManagementController.deleteMatch(this.seller, matchId);
            this.sellerView.printSuccess("Da xoa tran " + matchId);
        }
    }

    private void handleOpenSale() {
        Match match = this.matchManagementController.openSale(this.seller, this.chooseMatch());
        this.sellerView.printSuccess("Tran " + match.getId() + " da MO BAN ve.");
    }

    private void handleCloseSale() {
        Match match = this.matchManagementController.closeSale(this.seller, this.chooseMatch());
        this.sellerView.printSuccess("Tran " + match.getId() + " da DONG BAN ve.");
    }

    private void handleAddTicket() {
        String matchId = this.chooseMatch();
        String sectionId = this.chooseSection(matchId);
        this.sellerView.printSection("GHE CUA KHU " + sectionId);
        this.sellerView.printList(this.matchManagementController.getSeatsOfSection(sectionId), "Khu vuc chua co ghe.");
        String seatId = this.sellerView.inputId("Ma ghe (SEATxxx): ");
        String type = this.sellerView.inputString("Loai ve (Enter = theo khu vuc): ");
        BigDecimal price = this.sellerView.inputOptionalMoney("Gia ve (Enter = gia goc khu vuc): ");
        Ticket ticket = this.ticketManagementController.createTicket(this.seller, matchId, seatId, type, price);
        this.sellerView.printSuccess("Tao ve thanh cong: " + ticket.getId() + " - " + MoneyUtil.format(ticket.getPrice()));
    }

    private void handleGenerateTickets() {
        String matchId = this.chooseMatch();
        String sectionId = this.chooseSection(matchId);
        BigDecimal price = this.sellerView.inputOptionalMoney("Gia ve (Enter = gia goc khu vuc): ");
        int created = this.ticketManagementController.generateTicketsForSection(this.seller, matchId, sectionId, price);
        this.sellerView.printSuccess("Da tao " + created + " ve moi cho khu " + sectionId + ".");
    }

    private String chooseSection(String matchId) {
        List<Section> sections = this.matchManagementController.getSectionsOfMatch(this.seller, matchId);
        this.sellerView.printSection("KHU VUC");
        this.sellerView.printList(sections, "San chua co khu vuc.");
        return this.sellerView.inputId("Ma khu vuc: ");
    }

    private void handleViewTickets() {
        this.ticketView.showTickets(this.ticketManagementController.getTickets(this.seller, this.chooseMatch()));
    }

    private String chooseTicket(String label) {
        String matchId = this.chooseMatch();
        this.ticketView.showTickets(this.ticketManagementController.getTickets(this.seller, matchId));
        return this.sellerView.inputId(label);
    }

    private void handleUpdateTicket() {
        String ticketId = this.chooseTicket("Ma ve: ");
        String type = this.sellerView.inputString("Loai ve moi (Enter = giu nguyen): ");
        BigDecimal price = this.sellerView.inputOptionalMoney("Gia moi (Enter = giu nguyen): ");
        Ticket ticket = this.ticketManagementController.updateTicket(this.seller, ticketId, type, price);
        this.sellerView.printSuccess("Da cap nhat: " + ticket);
    }

    private void handleToggleTicketSale() {
        String ticketId = this.chooseTicket("Ma ve: ");
        int action = this.sellerView.askToggleSaleAction();
        Ticket ticket = action == 1
                ? this.ticketManagementController.stopTicketSale(this.seller, ticketId)
                : this.ticketManagementController.resumeTicketSale(this.seller, ticketId);
        this.sellerView.printSuccess("Ve " + ticket.getId() + " -> " + ticket.getStatus());
    }

    private void handleDeleteTicket() {
        String ticketId = this.chooseTicket("Ma ve can xoa: ");
        if (this.sellerView.confirm("Xac nhan xoa ve " + ticketId)) {
            this.ticketManagementController.deleteTicket(this.seller, ticketId);
            this.sellerView.printSuccess("Da xoa ve " + ticketId);
        }
    }

    /** Khanh: sellTicket. */
    private void handleSellTicket() {
        String ticketId = this.chooseTicket("Ma ve can ban: ");
        String buyer = this.sellerView.inputNonEmpty("Username Buyer nhan ve: ");
        this.sellerView.displaySoldTickets(this.ticketManagementController.sellTicket(this.seller, ticketId, buyer));
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
    // BAO CAO / SOAT VE
    // =====================================================================

    /** Soat ve vao cong bang ma QR. */
    public ETicket checkInTicket(User seller, String qrCode) {
        return this.ticketController.checkIn(seller, qrCode);
    }

    public SalesReport getSalesReport(User seller) {
        return this.reportService.buildReport(this.matchManagementController.getMatches(seller));
    }

    /** Lich su ban: cac booking da xac nhan/da huy thuoc tran cua Seller. */
    public List<Booking> getSalesHistory(User seller) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : this.bookingController.getBookingsForMatches(this.matchManagementController.getMatches(seller))) {
            if (!booking.isPending()) {
                result.add(booking);
            }
        }
        return result;
    }
}
