package view;

import controller.BookingController;
import controller.CancellationController;
import controller.MainController;
import controller.MatchController;
import controller.SeatController;
import controller.TicketController;
import dto.SeatMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Booking;
import model.BookingSeat;
import model.CancellationRequest;
import model.ETicket;
import model.Match;
import model.User;
import model.enums.PaymentMethod;
import util.AppConfig;

/**
 * Giao dien Buyer (Khoa: MainView + Main.java cua BuyerTicketMVC).
 * Luong: tim tran -> xem so do -> chon/giu ghe -> thanh toan -> nhan e-ticket -> lich su / huy ve.
 */
public class BuyerView extends BaseView {

    private static final Pattern SEAT_PATTERN = Pattern.compile("([A-Za-z]+)\\s*(\\d+)");

    private final User buyer;
    private final MainController mainController;
    private final MatchController matchController = new MatchController();
    private final SeatController seatController = new SeatController();
    private final BookingController bookingController = new BookingController();
    private final TicketController ticketController = new TicketController();
    private final CancellationController cancellationController = new CancellationController();
    private final MatchView matchView = new MatchView();
    private final SeatView seatView = new SeatView();
    private final OrderView orderView = new OrderView();
    private final TicketView ticketView = new TicketView();
    private final ProfileView profileView;

    public BuyerView(User buyer, MainController mainController) {
        this.buyer = buyer;
        this.mainController = mainController;
        this.profileView = new ProfileView(buyer, mainController);
    }

    /** Khoa: MainView.showMenu. */
    public void showMenu() {
        while (true) {
            int unread = this.mainController.countUnreadNotifications(this.buyer.getId());
            printHeader("BUYER - " + this.buyer.getFullName() + (unread > 0 ? "   (" + unread + " thong bao moi)" : ""));
            System.out.println("1. Tim kiem tran dau");
            System.out.println("2. Xem thong tin tran dau");
            System.out.println("3. Xem so do san");
            System.out.println("4. Chon ghe & dat ve");
            System.out.println("5. Booking dang cho thanh toan");
            System.out.println("6. Lich su dat ve");
            System.out.println("7. Xem ve dien tu (E-Ticket)");
            System.out.println("8. Gui yeu cau huy ve");
            System.out.println("9. Trang thai yeu cau huy ve");
            System.out.println("10. Thong bao");
            System.out.println("11. Ho so ca nhan");
            System.out.println("0. Dang xuat");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1: safely(this::handleSearch); break;
                case 2: safely(this::handleViewMatch); break;
                case 3: safely(this::handleViewSeatMap); break;
                case 4: safely(this::handleSelectSeats); break;
                case 5: safely(this::handlePendingBookings); break;
                case 6: safely(this::handleHistory); break;
                case 7: safely(this::handleViewTickets); break;
                case 8: safely(this::handleCancelRequest); break;
                case 9: safely(this::handleCancelStatus); break;
                case 10: safely(this.profileView::showNotifications); break;
                case 11: this.profileView.showProfileMenu(); break;
                case 0: return;
                default: printError("Lua chon khong hop le!");
            }
        }
    }

    // UC04 - Search Match
    private void handleSearch() {
        int type = this.input.readIntInRange("1. Tim theo ten  2. Tim theo ngay  3. Tat ca tran\nLua chon: ", 1, 3);
        List<Match> result;
        if (type == 1) {
            result = this.matchController.searchByName(this.input.readString("Tu khoa (ten tran/doi/giai): "));
        } else if (type == 2) {
            result = this.matchController.searchByDate(this.input.readDate("Ngay (yyyy-MM-dd): "));
        } else {
            result = this.matchController.getVisibleMatches();
        }
        this.matchView.displayMatches(result);
    }

    // UC05 - View Match Information
    private void handleViewMatch() {
        this.matchView.displayMatches(this.matchController.getVisibleMatches());
        Match match = this.matchController.viewMatch(this.input.readNonEmpty("Nhap ma tran: "));
        this.matchView.displayMatch(match);
    }

    // UC06 - View Stadium Map
    private void handleViewSeatMap() {
        this.matchView.displayMatches(this.matchController.getVisibleMatches());
        SeatMap map = this.seatController.viewSeatMap(this.input.readNonEmpty("Nhap ma tran: "));
        this.seatView.displaySeatMap(map);
    }

    // UC07 - Select Seat & create booking
    private void handleSelectSeats() {
        this.matchView.displayMatches(this.matchController.getVisibleMatches());
        String matchId = this.input.readNonEmpty("Nhap ma tran: ").toUpperCase();
        Match match = this.matchController.viewMatch(matchId);
        if (!match.isOnSale()) {
            printError("Tran dau chua mo ban ve.");
            return;
        }
        this.seatView.displaySeatMap(this.seatController.viewSeatMap(matchId));
        System.out.println("\nToi da " + AppConfig.MAX_TICKETS_PER_BOOKING + " ve/booking. Ghe duoc giu "
                + AppConfig.HOLD_MINUTES + " phut.");

        while (true) {
            Booking pending = this.bookingController.getPendingBooking(this.buyer, matchId);
            if (pending != null && !pending.canAddMoreSeats()) {
                System.out.println("Da dat so ve toi da cho booking nay.");
                break;
            }
            String text = this.input.readString("Nhap ghe muon chon (vd A1), Enter de ket thuc: ");
            if (text.isEmpty()) {
                break;
            }
            Matcher m = SEAT_PATTERN.matcher(text);
            if (!m.matches()) {
                printError("Dinh dang ghe khong hop le.");
                continue;
            }
            try {
                BookingSeat item = this.seatController.selectSeat(this.buyer, matchId, m.group(1), Integer.parseInt(m.group(2)));
                this.seatView.displaySelectionResult(item);
            } catch (RuntimeException e) {
                printError(e.getMessage());
            }
        }

        Booking booking = this.bookingController.getPendingBooking(this.buyer, matchId);
        if (booking == null || booking.getActiveItems().isEmpty()) {
            System.out.println("Ban chua chon ghe nao.");
            return;
        }
        manageBooking(booking.getId());
    }

    private void handlePendingBookings() {
        List<Booking> pending = this.bookingController.getPendingBookings(this.buyer);
        printSection("BOOKING DANG CHO THANH TOAN");
        if (pending.isEmpty()) {
            System.out.println("Khong co booking nao dang cho thanh toan.");
            return;
        }
        printList(pending, "");
        manageBooking(this.input.readNonEmpty("Nhap ma booking: ").toUpperCase());
    }

    /** Thanh toan / gia han / bo chon ghe / huy booking. */
    private void manageBooking(String bookingId) {
        while (true) {
            Booking booking = this.bookingController.loadBooking(bookingId);
            if (booking == null || !booking.getUserId().equalsIgnoreCase(this.buyer.getId())) {
                printError("Khong tim thay booking " + bookingId);
                return;
            }
            if (!booking.isPending()) {
                System.out.println("Booking " + bookingId + " hien o trang thai " + booking.getStatus() + ".");
                return;
            }
            this.orderView.displayOrder(booking, this.bookingController.getActiveHolds(bookingId));
            System.out.println("\n1. Thanh toan");
            System.out.println("2. Gia han giu ghe them " + AppConfig.HOLD_EXTEND_MINUTES + " phut");
            System.out.println("3. Bo chon mot ghe");
            System.out.println("4. Huy booking");
            System.out.println("0. Quay lai (ghe van duoc giu den khi het han)");
            int choice = this.input.readInt("Lua chon: ");
            try {
                switch (choice) {
                    case 1:
                        if (handlePayment(bookingId)) {
                            return;
                        }
                        break;
                    case 2:
                        this.bookingController.extendHold(this.buyer, bookingId);
                        printSuccess("Da gia han giu ghe.");
                        break;
                    case 3:
                        String itemId = this.input.readNonEmpty("Nhap ma ghe trong booking (BSxxxx): ").toUpperCase();
                        this.seatController.unselectSeat(this.buyer, itemId);
                        printSuccess("Da bo chon ghe.");
                        break;
                    case 4:
                        if (this.input.readYesNo("Xac nhan huy booking")) {
                            this.bookingController.cancelPendingBooking(this.buyer, bookingId);
                            printSuccess("Da huy booking va tra ghe.");
                            return;
                        }
                        break;
                    case 0:
                        return;
                    default:
                        printError("Lua chon khong hop le!");
                }
            } catch (RuntimeException e) {
                printError(e.getMessage());
            }
        }
    }

    /** Tra ve true neu thanh toan thanh cong. */
    private boolean handlePayment(String bookingId) {
        List<PaymentMethod> methods = new ArrayList<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method != PaymentMethod.CASH) {
                methods.add(method);
            }
        }
        printSection("CHON PHUONG THUC THANH TOAN");
        for (int i = 0; i < methods.size(); i++) {
            System.out.println((i + 1) + ". " + methods.get(i));
        }
        PaymentMethod method = methods.get(this.input.readIntInRange("Lua chon: ", 1, methods.size()) - 1);
        boolean approved = this.input.readIntInRange(
                "Mo phong ket qua cong thanh toan (1 = thanh cong, 2 = that bai): ", 1, 2) == 1;

        BookingController.PaymentResult result = this.bookingController.payBooking(this.buyer, bookingId, method, approved);
        this.orderView.displayPayment(result.getPayment());
        if (!result.isSuccess()) {
            printError("Thanh toan that bai. Ghe van dang duoc giu, ban co the thu lai.");
            return false;
        }
        printSuccess("Dat ve thanh cong! Ve dien tu cua ban:");
        for (ETicket ticket : result.getTickets()) {
            this.ticketView.displayTicket(ticket, this.ticketController.getMatchOf(ticket));
        }
        return true;
    }

    // UC13 - Purchase History
    private void handleHistory() {
        this.orderView.displayHistory(this.bookingController.viewPurchaseHistory(this.buyer));
    }

    // UC12 - View E-Ticket
    private void handleViewTickets() {
        List<ETicket> tickets = this.ticketController.getTicketsOfUser(this.buyer);
        this.ticketView.displayETickets(tickets);
        if (tickets.isEmpty()) {
            return;
        }
        String id = this.input.readString("Nhap ma ve de xem chi tiet (Enter de bo qua): ");
        if (!id.isEmpty()) {
            ETicket ticket = this.ticketController.viewTicket(this.buyer, id);
            this.ticketView.displayTicket(ticket, this.ticketController.getMatchOf(ticket));
        }
    }

    // UC14 - Submit Cancellation Request
    private void handleCancelRequest() {
        List<ETicket> tickets = this.ticketController.getTicketsOfUser(this.buyer);
        this.ticketView.displayETickets(tickets);
        if (tickets.isEmpty()) {
            return;
        }
        String id = this.input.readNonEmpty("Nhap ma ve muon huy: ").toUpperCase();
        String reason = this.input.readNonEmpty("Ly do huy: ");
        CancellationRequest request = this.cancellationController.createRequest(this.buyer, id, reason);
        this.ticketView.displayCancellation(request);
        printSuccess("Da gui yeu cau, vui long cho Admin xu ly.");
    }

    private void handleCancelStatus() {
        this.ticketView.displayCancellations(this.cancellationController.viewStatus(this.buyer));
    }
}
