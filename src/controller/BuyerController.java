package controller;

import dto.SeatMap;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
import view.BuyerView;
import view.MatchView;
import view.OrderView;
import view.SeatView;
import view.TicketView;

/**
 * Dieu khien luong cua Buyer (Khoa: Main.java cua BuyerTicketMVC):
 * tim tran -> xem so do -> chon/giu ghe -> thanh toan -> nhan e-ticket -> lich su / huy ve.
 * Controller lay du lieu nhap tu View, goi cac controller nghiep vu, roi dua ket qua cho View hien thi.
 */
public class BuyerController {

    /** Ghe nhap dang "A1" hoac kem ma khu "SEC01-A1". */
    private static final Pattern SEAT_PATTERN = Pattern.compile("(?:([A-Za-z]+\\d+)\\s*[-:]\\s*)?([A-Za-z]+)\\s*(\\d+)");

    private final User buyer;
    private final BuyerView buyerView = new BuyerView();
    private final MatchView matchView = new MatchView();
    private final SeatView seatView = new SeatView();
    private final OrderView orderView = new OrderView();
    private final TicketView ticketView = new TicketView();
    private final MatchController matchController = new MatchController();
    private final SeatController seatController = new SeatController();
    private final BookingController bookingController = new BookingController();
    private final TicketController ticketController = new TicketController();
    private final CancellationController cancellationController = new CancellationController();
    private final ProfileController profileController;

    public BuyerController(User buyer) {
        this.buyer = buyer;
        this.profileController = new ProfileController(buyer);
    }

    /** Khoa: MainView.showMenu - vong lap menu Buyer. */
    public void start() {
        while (true) {
            String title = this.buyerView.withUnread("BUYER - " + this.buyer.getFullName(),
                    this.profileController.countUnreadNotifications());
            int choice = this.buyerView.showMenu(title);
            switch (choice) {
                case 1: this.runSafely(this::handleSearch); break;
                case 2: this.runSafely(this::handleViewMatch); break;
                case 3: this.runSafely(this::handleViewSeatMap); break;
                case 4: this.runSafely(this::handleSelectSeats); break;
                case 5: this.runSafely(this::handlePendingBookings); break;
                case 6: this.runSafely(this::handleHistory); break;
                case 7: this.runSafely(this::handleViewTickets); break;
                case 8: this.runSafely(this::handleCancelRequest); break;
                case 9: this.runSafely(this::handleCancelStatus); break;
                case 10: this.runSafely(this.profileController::showNotifications); break;
                case 11: this.profileController.showProfileMenu(); break;
                case 0: return;
                default: this.buyerView.printError("Lua chon khong hop le!");
            }
        }
    }

    // UC04 - Search Match
    private void handleSearch() {
        int type = this.buyerView.askSearchType();
        List<Match> result;
        if (type == 1) {
            result = this.matchController.searchByName(this.buyerView.inputString("Tu khoa (ten tran/doi/giai): "));
        } else if (type == 2) {
            result = this.matchController.searchByDate(this.buyerView.inputDate("Ngay (yyyy-MM-dd): "));
        } else {
            result = this.matchController.getVisibleMatches();
        }
        this.matchView.displayMatches(result);
    }

    // UC05 - View Match Information
    private void handleViewMatch() {
        this.matchView.displayMatches(this.matchController.getVisibleMatches());
        Match match = this.matchController.viewMatch(this.buyerView.inputNonEmpty("Nhap ma tran: "));
        this.matchView.displayMatch(match);
    }

    // UC06 - View Stadium Map
    private void handleViewSeatMap() {
        this.matchView.displayMatches(this.matchController.getVisibleMatches());
        SeatMap map = this.seatController.viewSeatMap(this.buyerView.inputNonEmpty("Nhap ma tran: "));
        this.seatView.displaySeatMap(map);
    }

    // UC07 - Select Seat & create booking
    private void handleSelectSeats() {
        this.matchView.displayMatches(this.matchController.getVisibleMatches());
        String matchId = this.buyerView.inputId("Nhap ma tran: ");
        Match match = this.matchController.viewMatch(matchId);
        if (!match.isOnSale()) {
            this.buyerView.printError("Tran dau chua mo ban ve.");
            return;
        }
        this.seatView.displaySeatMap(this.seatController.viewSeatMap(matchId));
        this.buyerView.showSeatSelectionHint(AppConfig.MAX_TICKETS_PER_BOOKING, AppConfig.HOLD_MINUTES);

        while (true) {
            Booking pending = this.bookingController.getPendingBooking(this.buyer, matchId);
            if (pending != null && !pending.canAddMoreSeats()) {
                this.buyerView.showMessage("Da dat so ve toi da cho booking nay.");
                break;
            }
            String text = this.buyerView.askSeat();
            if (text.isEmpty()) {
                break;
            }
            Matcher m = SEAT_PATTERN.matcher(text);
            if (!m.matches()) {
                this.buyerView.printError("Dinh dang ghe khong hop le.");
                continue;
            }
            try {
                BookingSeat item = this.seatController.selectSeat(this.buyer, matchId, m.group(1), m.group(2),
                        Integer.parseInt(m.group(3)));
                this.seatView.displaySelectionResult(item);
            } catch (NoSuchElementException e) {
                throw e;
            } catch (RuntimeException e) {
                this.buyerView.printError(e.getMessage());
            }
        }

        Booking booking = this.bookingController.getPendingBooking(this.buyer, matchId);
        if (booking == null || booking.getActiveItems().isEmpty()) {
            this.buyerView.showMessage("Ban chua chon ghe nao.");
            return;
        }
        this.manageBooking(booking.getId());
    }

    private void handlePendingBookings() {
        List<Booking> pending = this.bookingController.getPendingBookings(this.buyer);
        this.orderView.printSection("BOOKING DANG CHO THANH TOAN");
        if (pending.isEmpty()) {
            this.orderView.showMessage("Khong co booking nao dang cho thanh toan.");
            return;
        }
        this.orderView.printList(pending, "");
        this.manageBooking(this.buyerView.inputId("Nhap ma booking: "));
    }

    /** Thanh toan / gia han / bo chon ghe / huy booking. */
    private void manageBooking(String bookingId) {
        while (true) {
            Booking booking = this.bookingController.loadBooking(bookingId);
            if (booking == null || !booking.getUserId().equalsIgnoreCase(this.buyer.getId())) {
                this.buyerView.printError("Khong tim thay booking " + bookingId);
                return;
            }
            if (!booking.isPending()) {
                this.buyerView.showMessage("Booking " + bookingId + " hien o trang thai " + booking.getStatus() + ".");
                return;
            }
            this.orderView.displayOrder(booking, this.bookingController.getActiveHolds(bookingId));
            int choice = this.buyerView.showBookingActions(AppConfig.HOLD_EXTEND_MINUTES);
            try {
                switch (choice) {
                    case 1:
                        if (this.handlePayment(bookingId)) {
                            return;
                        }
                        break;
                    case 2:
                        this.bookingController.extendHold(this.buyer, bookingId);
                        this.buyerView.printSuccess("Da gia han giu ghe.");
                        break;
                    case 3:
                        String itemId = this.buyerView.inputId("Nhap ma ghe trong booking (BSxxxx): ");
                        this.seatController.unselectSeat(this.buyer, itemId);
                        this.buyerView.printSuccess("Da bo chon ghe.");
                        break;
                    case 4:
                        if (this.buyerView.confirm("Xac nhan huy booking")) {
                            this.bookingController.cancelPendingBooking(this.buyer, bookingId);
                            this.buyerView.printSuccess("Da huy booking va tra ghe.");
                            return;
                        }
                        break;
                    case 0:
                        return;
                    default:
                        this.buyerView.printError("Lua chon khong hop le!");
                }
            } catch (NoSuchElementException e) {
                throw e;
            } catch (RuntimeException e) {
                this.buyerView.printError(e.getMessage());
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
        PaymentMethod method = this.buyerView.choosePaymentMethod(methods);
        boolean approved = this.buyerView.askGatewayApproved();

        BookingController.PaymentResult result = this.bookingController.payBooking(this.buyer, bookingId, method, approved);
        this.orderView.displayPayment(result.getPayment());
        if (!result.isSuccess()) {
            this.buyerView.printError("Thanh toan that bai. Ghe van dang duoc giu, ban co the thu lai.");
            return false;
        }
        this.buyerView.printSuccess("Dat ve thanh cong! Ve dien tu cua ban:");
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
        String id = this.buyerView.inputString("Nhap ma ve de xem chi tiet (Enter de bo qua): ");
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
        String id = this.buyerView.inputId("Nhap ma ve muon huy: ");
        String reason = this.buyerView.inputNonEmpty("Ly do huy: ");
        CancellationRequest request = this.cancellationController.createRequest(this.buyer, id, reason);
        this.ticketView.displayCancellation(request);
        this.buyerView.printSuccess("Da gui yeu cau, vui long cho Admin xu ly.");
    }

    private void handleCancelStatus() {
        this.ticketView.displayCancellations(this.cancellationController.viewStatus(this.buyer));
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (NoSuchElementException e) {
            throw e; // het du lieu vao: de MainController ket thuc chuong trinh
        } catch (RuntimeException e) {
            this.buyerView.printError(e.getMessage());
        }
    }
}
