package view;

import controller.MainController;
import controller.SellerController;
import dto.SalesReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import model.ETicket;
import model.Match;
import model.Section;
import model.Ticket;
import model.User;
import util.MoneyUtil;

/**
 * Giao dien Seller (Khanh: SellerView) - quan ly tran, mo ban, tao ve, ban tai quay, soat ve, doanh thu.
 */
public class SellerView extends BaseView {

    private final User seller;
    private final MainController mainController;
    private final SellerController controller = new SellerController();
    private final MatchView matchView = new MatchView();
    private final TicketView ticketView = new TicketView();
    private final OrderView orderView = new OrderView();
    private final ProfileView profileView;

    public SellerView(User seller, MainController mainController) {
        this.seller = seller;
        this.mainController = mainController;
        this.profileView = new ProfileView(seller, mainController);
    }

    /** Khanh: showMenu. */
    public void showMenu() {
        while (true) {
            int unread = this.mainController.countUnreadNotifications(this.seller.getId());
            printHeader("SELLER MANAGEMENT - " + this.seller.getFullName() + (unread > 0 ? "   (" + unread + " thong bao moi)" : ""));
            System.out.println("1. Them tran dau            2. Xem tran cua toi        3. Sua tran dau");
            System.out.println("4. Xoa tran dau             5. Mo ban ve               6. Dong ban ve");
            System.out.println("7. Tao ve cho mot ghe       8. Tao ve ca khu vuc       9. Xem ve cua tran");
            System.out.println("10. Sua gia/loai ve         11. Dung/mo lai ban mot ve 12. Ban ve tai quay");
            System.out.println("13. Soat ve (check-in QR)   14. Bao cao doanh thu      15. Lich su ban");
            System.out.println("16. Thong bao               17. Ho so ca nhan          0. Dang xuat");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1: safely(this::handleAddMatch); break;
                case 2: safely(this::handleViewMatches); break;
                case 3: safely(this::handleUpdateMatch); break;
                case 4: safely(this::handleDeleteMatch); break;
                case 5: safely(this::handleOpenSale); break;
                case 6: safely(this::handleCloseSale); break;
                case 7: safely(this::handleAddTicket); break;
                case 8: safely(this::handleGenerateTickets); break;
                case 9: safely(this::handleViewTickets); break;
                case 10: safely(this::handleUpdateTicket); break;
                case 11: safely(this::handleToggleTicketSale); break;
                case 12: safely(this::handleSellTicket); break;
                case 13: safely(this::handleCheckIn); break;
                case 14: safely(this::handleReport); break;
                case 15: safely(this::handleHistory); break;
                case 16: safely(this.profileView::showNotifications); break;
                case 17: this.profileView.showProfileMenu(); break;
                case 0: return;
                default: printError("Lua chon khong hop le!");
            }
        }
    }

    /** Khanh: showMatches. */
    public void showMatches(List<Match> matches) {
        this.matchView.displayMatches(matches);
    }

    /** Khanh: showTickets. */
    public void showTickets(List<Ticket> tickets) {
        this.ticketView.showTickets(tickets);
    }

    private void handleAddMatch() {
        printSection("DOI BONG");
        printList(this.controller.getAllTeams(), "Chua co doi bong.");
        printSection("SAN VAN DONG");
        printList(this.controller.getAllStadiums(), "Chua co san.");
        String name = this.input.readNonEmpty("Ten tran: ");
        String competition = this.input.readNonEmpty("Giai dau: ");
        String home = this.input.readNonEmpty("Ma doi nha: ");
        String away = this.input.readNonEmpty("Ma doi khach: ");
        String stadium = this.input.readNonEmpty("Ma san: ");
        LocalDate date = this.input.readDate("Ngay thi dau (yyyy-MM-dd): ");
        LocalTime time = this.input.readTime("Gio thi dau (HH:mm): ");
        Match match = this.controller.createMatch(this.seller, name, competition, home, away, stadium, date, time);
        printSuccess("Tao tran thanh cong: " + match.getId() + ". Hay tao ve va mo ban.");
    }

    private void handleViewMatches() {
        showMatches(this.controller.getMatches(this.seller));
    }

    private String chooseMatch() {
        showMatches(this.controller.getMatches(this.seller));
        return this.input.readNonEmpty("Nhap ma tran: ").toUpperCase();
    }

    private void handleUpdateMatch() {
        String matchId = chooseMatch();
        System.out.println("(Bo trong de giu nguyen)");
        String name = this.input.readString("Ten tran moi: ");
        String competition = this.input.readString("Giai dau moi: ");
        LocalDate date = this.input.readOptionalDate("Ngay moi (yyyy-MM-dd): ");
        LocalTime time = this.input.readOptionalTime("Gio moi (HH:mm): ");
        this.controller.updateMatch(this.seller, matchId, name, competition, date, time);
        printSuccess("Cap nhat tran thanh cong!");
    }

    private void handleDeleteMatch() {
        String matchId = chooseMatch();
        if (this.input.readYesNo("Xac nhan xoa tran " + matchId)) {
            this.controller.deleteMatch(this.seller, matchId);
            printSuccess("Da xoa tran " + matchId);
        }
    }

    private void handleOpenSale() {
        Match match = this.controller.openSale(this.seller, chooseMatch());
        printSuccess("Tran " + match.getId() + " da MO BAN ve.");
    }

    private void handleCloseSale() {
        Match match = this.controller.closeSale(this.seller, chooseMatch());
        printSuccess("Tran " + match.getId() + " da DONG BAN ve.");
    }

    private void handleAddTicket() {
        String matchId = chooseMatch();
        String sectionId = chooseSection(matchId);
        printSection("GHE CUA KHU " + sectionId);
        printList(this.controller.getSeatsOfSection(sectionId), "Khu vuc chua co ghe.");
        String seatId = this.input.readNonEmpty("Ma ghe (SEATxxx): ").toUpperCase();
        String type = this.input.readString("Loai ve (Enter = theo khu vuc): ");
        BigDecimal price = this.input.readOptionalMoney("Gia ve (Enter = gia goc khu vuc): ");
        Ticket ticket = this.controller.createTicket(this.seller, matchId, seatId, type, price);
        printSuccess("Tao ve thanh cong: " + ticket.getId() + " - " + MoneyUtil.format(ticket.getPrice()));
    }

    private void handleGenerateTickets() {
        String matchId = chooseMatch();
        String sectionId = chooseSection(matchId);
        BigDecimal price = this.input.readOptionalMoney("Gia ve (Enter = gia goc khu vuc): ");
        int created = this.controller.generateTicketsForSection(this.seller, matchId, sectionId, price);
        printSuccess("Da tao " + created + " ve moi cho khu " + sectionId + ".");
    }

    private String chooseSection(String matchId) {
        List<Section> sections = this.controller.getSectionsOfMatch(this.seller, matchId);
        printSection("KHU VUC");
        printList(sections, "San chua co khu vuc.");
        return this.input.readNonEmpty("Ma khu vuc: ").toUpperCase();
    }

    private void handleViewTickets() {
        showTickets(this.controller.getTickets(this.seller, chooseMatch()));
    }

    private void handleUpdateTicket() {
        String matchId = chooseMatch();
        showTickets(this.controller.getTickets(this.seller, matchId));
        String ticketId = this.input.readNonEmpty("Ma ve: ").toUpperCase();
        String type = this.input.readString("Loai ve moi (Enter = giu nguyen): ");
        BigDecimal price = this.input.readOptionalMoney("Gia moi (Enter = giu nguyen): ");
        Ticket ticket = this.controller.updateTicket(this.seller, ticketId, type, price);
        printSuccess("Da cap nhat: " + ticket);
    }

    private void handleToggleTicketSale() {
        String matchId = chooseMatch();
        showTickets(this.controller.getTickets(this.seller, matchId));
        String ticketId = this.input.readNonEmpty("Ma ve: ").toUpperCase();
        int action = this.input.readIntInRange("1. Dung ban (stop sale)  2. Mo ban lai (put on sale): ", 1, 2);
        Ticket ticket = action == 1
                ? this.controller.stopTicketSale(this.seller, ticketId)
                : this.controller.resumeTicketSale(this.seller, ticketId);
        printSuccess("Ve " + ticket.getId() + " -> " + ticket.getStatus());
    }

    /** Khanh: sellTicket. */
    private void handleSellTicket() {
        String matchId = chooseMatch();
        showTickets(this.controller.getTickets(this.seller, matchId));
        String ticketId = this.input.readNonEmpty("Ma ve can ban: ").toUpperCase();
        String buyer = this.input.readNonEmpty("Username Buyer nhan ve: ");
        List<ETicket> tickets = this.controller.sellTicket(this.seller, ticketId, buyer);
        printSuccess("Ban ve thanh cong (tien mat).");
        for (ETicket ticket : tickets) {
            System.out.println("  " + ticket);
        }
    }

    private void handleCheckIn() {
        ETicket ticket = this.controller.checkInTicket(this.seller, this.input.readNonEmpty("Ma QR: "));
        printSuccess("Check-in thanh cong: " + ticket.getSeatInfo());
    }

    private void handleReport() {
        renderReport(this.controller.getSalesReport(this.seller));
    }

    private void handleHistory() {
        this.orderView.displayHistory(this.controller.getSalesHistory(this.seller));
    }

    /** Dung chung cho AdminView. */
    static void renderReport(SalesReport report) {
        System.out.println();
        System.out.println("---------- BAO CAO DOANH THU ----------");
        System.out.println(String.format("%-5s | %-26s | %8s | %6s | %8s | %18s", "Tran", "Ten", "Tong ve", "Da ban", "Dang giu", "Doanh thu"));
        for (SalesReport.Line line : report.getLines()) {
            System.out.println(String.format("%-5s | %-26s | %8d | %6d | %8d | %18s", line.getMatchId(), line.getMatchName(),
                    line.getTotalTickets(), line.getSoldTickets(), line.getHeldTickets(), MoneyUtil.format(line.getRevenue())));
        }
        System.out.println("Tong ve da ban : " + report.getTotalSold());
        System.out.println("Tong doanh thu : " + MoneyUtil.format(report.getTotalRevenue()));
        System.out.println("Da hoan tien   : " + MoneyUtil.format(report.getTotalRefunded()));
    }
}
