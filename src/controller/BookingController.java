package controller;

import command.CancelBookingCommand;
import command.ConfirmBookingCommand;
import command.CreateBookingCommand;
import command.CreateHoldCommand;
import command.LockBookingSeatCommand;
import command.LockSeatCommand;
import command.MarkBookingSeatAsSoldCommand;
import command.MarkSeatAsSoldCommand;
import command.ReleaseBookingSeatCommand;
import command.ReleaseHoldCommand;
import command.UnlockSeatCommand;
import command.ExtendHoldCommand;
import exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import model.Booking;
import model.BookingSeat;
import model.ETicket;
import model.Match;
import model.Payment;
import model.Seat;
import model.SeatHold;
import model.Ticket;
import model.User;
import model.enums.BookingSeatStatus;
import model.enums.BookingStatus;
import model.enums.PaymentMethod;
import model.enums.Role;
import model.enums.TicketStatus;
import repository.BookingRepository;
import repository.BookingSeatRepository;
import repository.SeatHoldRepository;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.UserRepository;
import service.AuditService;
import service.NotificationService;
import util.AppConfig;
import util.MoneyUtil;

/**
 * Luong dat ve tu dau den cuoi - hop nhat Main.java cua Trieu (booking, hold, payment, double booking)
 * va OrderController cua Khoa (createOrder, confirmOrder, viewPurchaseHistory).
 *
 * Chon ghe -> kiem tra backend -> HOLD (co han) -> thanh toan -> SOLD / tra ghe.
 */
public class BookingController {

    private final TicketRepository ticketRepo = new TicketRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final BookingSeatRepository bookingSeatRepo = new BookingSeatRepository();
    private final SeatHoldRepository holdRepo = new SeatHoldRepository();
    private final UserRepository userRepo = new UserRepository();
    private final MatchController matchController = new MatchController();
    private final PaymentController paymentController = new PaymentController();
    private final TicketController ticketController = new TicketController();
    private final NotificationService notificationService = new NotificationService();
    private final AuditService auditService = new AuditService();

    /** Ket qua thanh toan tra ve cho view. */
    public static class PaymentResult {
        private final Payment payment;
        private final List<ETicket> tickets;

        public PaymentResult(Payment payment, List<ETicket> tickets) {
            this.payment = payment;
            this.tickets = tickets;
        }

        public Payment getPayment() { return this.payment; }
        public List<ETicket> getTickets() { return this.tickets; }
        public boolean isSuccess() { return this.payment.verifyPayment(); }
    }

    // ===================================================================
    // 1. GIU GHE (HOLD)
    // ===================================================================

    /** Buyer giu mot ghe; ghe duoc gom vao booking PENDING cua tran do (Khoa: createOrder). */
    public BookingSeat holdSeat(User user, String ticketId) {
        releaseExpiredHolds();
        Ticket ticket = requireTicket(ticketId);
        requireOnSale(ticket.getMatchId());
        Booking booking = getOrCreatePendingBooking(user, ticket.getMatchId());
        return holdSeatInBooking(user, ticket, booking);
    }

    private BookingSeat holdSeatInBooking(User user, Ticket ticket, Booking booking) {
        if (!booking.canAddMoreSeats()) {
            throw new BusinessException("Moi booking toi da " + AppConfig.MAX_TICKETS_PER_BOOKING + " ve.");
        }
        if (!ticket.isAvailable()) {
            cancelIfEmpty(booking);
            throw new BusinessException("Ghe dang " + ticket.getStatus() + ", khong the chon.");
        }

        // Optimistic locking: chi thanh cong neu chua ai doi ve nay ke tu luc doc
        int expectedVersion = ticket.getVersion();
        new LockSeatCommand(ticket).execute();
        if (!this.ticketRepo.updateIfVersionMatches(ticket, expectedVersion)) {
            cancelIfEmpty(booking);
            this.auditService.log(user.getUsername(), "HOLD " + ticket.getId() + " (bi tranh cho)", AuditService.FAILED);
            throw new BusinessException("Ghe vua duoc nguoi khac giu truoc. Vui long chon ghe khac.");
        }

        Seat seat = this.seatRepo.findById(ticket.getSeatId());
        String label = seat == null ? ticket.getSeatId() : seat.getLabel();
        BookingSeat item = new BookingSeat(null, booking.getId(), ticket.getId(), label, ticket.getSectionId(), ticket.getPrice());
        new LockBookingSeatCommand(item).execute();
        this.bookingSeatRepo.insert(item);

        SeatHold hold = new SeatHold(null, ticket.getId(), user.getId(), booking.getId(),
                LocalDateTime.now().plusMinutes(AppConfig.HOLD_MINUTES));
        new CreateHoldCommand(hold).execute();
        this.holdRepo.insert(hold);

        booking.addItem(item);
        refreshExpiry(booking);
        this.bookingRepo.update(booking);
        this.auditService.log(user.getUsername(), "HOLD " + ticket.getId() + " -> " + booking.getId(), AuditService.SUCCESS);
        return item;
    }

    /** Tra mot ghe dang giu (Khoa: SeatController.unselectSeat). */
    public Booking releaseSeat(User user, String bookingSeatId) {
        BookingSeat item = this.bookingSeatRepo.findById(bookingSeatId);
        if (item == null) {
            throw new BusinessException("Khong tim thay ghe " + bookingSeatId + " trong booking.");
        }
        Booking booking = requireOwnPendingBooking(user, item.getBookingId());
        releaseItem(item);
        booking = loadBooking(booking.getId());
        booking.calculateTotal();
        if (booking.getActiveItems().isEmpty()) {
            new CancelBookingCommand(booking).execute();
        }
        refreshExpiry(booking);
        this.bookingRepo.update(booking);
        this.auditService.log(user.getUsername(), "RELEASE " + item.getTicketId(), AuditService.SUCCESS);
        return booking;
    }

    /** Gia han giu ghe them vai phut (Trieu: ExtendHoldCommand). */
    public Booking extendHold(User user, String bookingId) {
        releaseExpiredHolds();
        Booking booking = requireOwnPendingBooking(user, bookingId);
        List<SeatHold> holds = this.holdRepo.findHoldingByBooking(bookingId);
        if (holds.isEmpty()) {
            throw new BusinessException("Booking khong con ghe nao dang giu.");
        }
        for (SeatHold hold : holds) {
            new ExtendHoldCommand(hold).execute();
            this.holdRepo.update(hold);
        }
        refreshExpiry(booking);
        this.bookingRepo.update(booking);
        this.auditService.log(user.getUsername(), "EXTEND_HOLD " + bookingId, AuditService.SUCCESS);
        return booking;
    }

    /** Huy booking dang cho thanh toan, tra tat ca ghe. */
    public void cancelPendingBooking(User user, String bookingId) {
        Booking booking = requireOwnPendingBooking(user, bookingId);
        for (BookingSeat item : booking.getActiveItems()) {
            releaseItem(item);
        }
        booking = loadBooking(bookingId);
        booking.calculateTotal();
        new CancelBookingCommand(booking).execute();
        this.bookingRepo.update(booking);
        this.auditService.log(user.getUsername(), "CANCEL_BOOKING " + bookingId, AuditService.SUCCESS);
    }

    // ===================================================================
    // 2. THANH TOAN (Khoa: confirmOrder, Trieu: payment + confirm)
    // ===================================================================

    public PaymentResult payBooking(User user, String bookingId, PaymentMethod method, boolean approvedByGateway) {
        releaseExpiredHolds();
        Booking booking = loadBooking(bookingId);
        if (booking == null || !booking.getUserId().equalsIgnoreCase(user.getId())) {
            throw new BusinessException("Khong tim thay booking " + bookingId);
        }
        if (!booking.isPending()) {
            throw new BusinessException("Booking dang o trang thai " + booking.getStatus()
                    + " (co the da het thoi gian giu ghe), khong the thanh toan.");
        }
        return finalizePayment(user, booking, method, approvedByGateway);
    }

    private PaymentResult finalizePayment(User actor, Booking booking, PaymentMethod method, boolean approved) {
        if (booking.getActiveItems().isEmpty()) {
            throw new BusinessException("Booking khong co ghe nao.");
        }
        booking.calculateTotal();
        Payment payment = this.paymentController.processPayment(booking, method, approved);
        if (!payment.verifyPayment()) {
            this.auditService.log(actor.getUsername(), "PAYMENT " + payment.getId() + " " + booking.getId(), AuditService.FAILED);
            String until = booking.getExpiresAt() == null ? "" : " den " + booking.getExpiresAt().withNano(0);
            this.notificationService.send(booking.getUserId(), "Thanh toan booking " + booking.getId()
                    + " that bai. Ghe van duoc giu" + until + ", ban co the thu lai.");
            return new PaymentResult(payment, new ArrayList<>());
        }

        for (BookingSeat item : booking.getActiveItems()) {
            Ticket ticket = this.ticketRepo.findById(item.getTicketId());
            new MarkSeatAsSoldCommand(ticket).execute();
            this.ticketRepo.update(ticket);
            new MarkBookingSeatAsSoldCommand(item).execute();
            this.bookingSeatRepo.update(item);
            SeatHold hold = this.holdRepo.findHoldingByTicket(ticket.getId());
            if (hold != null) {
                // Tien da thu: neu phien giu vua qua han trong tich tac thi van dong phien, khong lam hong giao dich
                if (hold.isActive()) {
                    hold.confirmHold();
                } else {
                    hold.releaseHold();
                }
                this.holdRepo.update(hold);
            }
        }
        new ConfirmBookingCommand(booking).execute();
        this.bookingRepo.update(booking);

        List<ETicket> tickets = this.ticketController.createTicket(booking);
        this.auditService.log(actor.getUsername(), "PAYMENT " + payment.getId() + " " + booking.getId(), AuditService.SUCCESS);
        this.notificationService.send(booking.getUserId(), "Thanh toan thanh cong booking " + booking.getId() + " ("
                + MoneyUtil.format(booking.getTotalAmount()) + "). Da phat hanh " + tickets.size() + " ve dien tu.");
        return new PaymentResult(payment, tickets);
    }

    // ===================================================================
    // 3. BAN TAI QUAY (Khanh: SellerController.sellTicket)
    // ===================================================================

    public PaymentResult sellAtCounter(User seller, String ticketId, String buyerUsername) {
        releaseExpiredHolds();
        User buyer = this.userRepo.findByUsername(buyerUsername);
        if (buyer == null || buyer.getRole() != Role.BUYER || !buyer.isActive()) {
            throw new BusinessException("Khong tim thay tai khoan Buyer dang hoat dong: " + buyerUsername);
        }
        Ticket ticket = requireTicket(ticketId);
        requireOnSale(ticket.getMatchId());
        Booking booking = createBooking(buyer, ticket.getMatchId());
        holdSeatInBooking(buyer, ticket, booking);
        PaymentResult result = finalizePayment(seller, loadBooking(booking.getId()), PaymentMethod.CASH, true);
        this.auditService.log(seller.getUsername(), "COUNTER_SALE " + ticketId + " -> " + buyerUsername, AuditService.SUCCESS);
        return result;
    }

    // ===================================================================
    // 4. TU DONG TRA GHE KHI HET HAN GIU
    // ===================================================================

    /** Quet cac phien giu da qua han: tra ghe, huy dong booking, het han booking neu khong con ghe. */
    public int releaseExpiredHolds() {
        int released = 0;
        Set<String> touchedBookings = new HashSet<>();
        for (SeatHold hold : this.holdRepo.findHolding()) {
            if (!hold.isExpired()) {
                continue;
            }
            this.holdRepo.update(hold);
            Ticket ticket = this.ticketRepo.findById(hold.getTicketId());
            if (ticket != null && ticket.getStatus() == TicketStatus.HOLD) {
                new UnlockSeatCommand(ticket).execute();
                this.ticketRepo.update(ticket);
            }
            for (BookingSeat item : this.bookingSeatRepo.findByBookingId(hold.getBookingId())) {
                if (item.getTicketId().equalsIgnoreCase(hold.getTicketId()) && item.getStatus() == BookingSeatStatus.HOLD) {
                    new ReleaseBookingSeatCommand(item).execute();
                    this.bookingSeatRepo.update(item);
                }
            }
            touchedBookings.add(hold.getBookingId());
            released++;
        }
        for (String bookingId : touchedBookings) {
            Booking booking = loadBooking(bookingId);
            if (booking == null || !booking.isPending()) {
                continue;
            }
            booking.calculateTotal();
            if (booking.getActiveItems().isEmpty()) {
                booking.expire();
                this.notificationService.send(booking.getUserId(), "Booking " + bookingId
                        + " da het thoi gian giu ghe, cac ghe da duoc tra lai.");
            }
            refreshExpiry(booking);
            this.bookingRepo.update(booking);
        }
        if (released > 0) {
            this.auditService.log("system", "AUTO_RELEASE " + released + " expired hold(s)", AuditService.SUCCESS);
        }
        return released;
    }

    // ===================================================================
    // 5. KIEM THU DOUBLE BOOKING (Trieu: DOUBLE BOOKING TEST)
    // ===================================================================

    /** Hai Buyer cung luc giu mot ghe: chi mot nguoi thanh cong. Sau do tra ghe ve trang thai cu. */
    public List<String> simulateDoubleBooking(User admin, String ticketId) {
        releaseExpiredHolds();
        final Ticket ticket = requireTicket(ticketId);
        if (!ticket.isAvailable()) {
            throw new BusinessException("Hay chon mot ve dang AVAILABLE de kiem thu (hien tai: " + ticket.getStatus() + ").");
        }
        requireOnSale(ticket.getMatchId());
        List<User> buyers = new ArrayList<>();
        for (User u : this.userRepo.findByRole(Role.BUYER)) {
            if (u.isActive() && buyers.size() < 2) {
                buyers.add(u);
            }
        }
        if (buyers.size() < 2) {
            throw new BusinessException("Can it nhat 2 tai khoan Buyer dang hoat dong.");
        }

        final List<String> log = Collections.synchronizedList(new ArrayList<>());
        final List<Booking> createdBookings = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch readySignal = new CountDownLatch(buyers.size());
        final CountDownLatch startSignal = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (final User buyer : buyers) {
            Thread thread = new Thread(() -> {
                BookingController worker = new BookingController();
                Booking booking = worker.createBooking(buyer, ticket.getMatchId());
                createdBookings.add(booking);
                // Ca hai cung doc ve (cung version) truoc khi xuat phat -> optimistic lock quyet dinh ai thang
                Ticket snapshot = worker.ticketRepo.findById(ticket.getId());
                readySignal.countDown();
                try {
                    startSignal.await();
                    worker.holdSeatInBooking(buyer, snapshot, booking);
                    log.add("[THANH CONG] " + buyer.getUsername() + " giu duoc ghe " + ticket.getId()
                            + " (booking " + booking.getId() + ")");
                } catch (RuntimeException e) {
                    log.add("[BI CHAN]    " + buyer.getUsername() + ": " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(thread);
            thread.start();
        }
        try {
            readySignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        startSignal.countDown();
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Don dep: tra ghe va xoa du lieu tam cua bai kiem thu (nhat ky audit van duoc giu)
        for (Booking booking : createdBookings) {
            purgeTestBooking(booking.getId());
        }
        Ticket after = this.ticketRepo.findById(ticketId);
        log.add("Ket qua: chi 1 nguoi giu duoc ghe. Da tra ghe, trang thai hien tai: " + after.getStatus());
        this.auditService.log(admin.getUsername(), "DOUBLE_BOOKING_TEST " + ticketId, AuditService.SUCCESS);
        return log;
    }

    // ===================================================================
    // 6. TRA CUU
    // ===================================================================

    /** Khoa: viewPurchaseHistory - moi nhat len dau. */
    public List<Booking> viewPurchaseHistory(User user) {
        releaseExpiredHolds();
        List<Booking> result = new ArrayList<>();
        for (Booking booking : this.bookingRepo.findByUserId(user.getId())) {
            result.add(attachItems(booking));
        }
        Collections.reverse(result);
        return result;
    }

    public List<Booking> getPendingBookings(User user) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : viewPurchaseHistory(user)) {
            if (booking.isPending() && !booking.getActiveItems().isEmpty()) {
                result.add(booking);
            }
        }
        return result;
    }

    public Booking getPendingBooking(User user, String matchId) {
        for (Booking booking : this.bookingRepo.findByUserId(user.getId())) {
            if (booking.isPending() && booking.getMatchId().equalsIgnoreCase(matchId)) {
                return attachItems(booking);
            }
        }
        return null;
    }

    public List<Booking> getBookingsForMatches(List<Match> matches) {
        Set<String> ids = new HashSet<>();
        for (Match match : matches) {
            ids.add(match.getId());
        }
        List<Booking> result = new ArrayList<>();
        for (Booking booking : this.bookingRepo.findAll()) {
            if (ids.contains(booking.getMatchId())) {
                result.add(attachItems(booking));
            }
        }
        Collections.reverse(result);
        return result;
    }

    public List<Booking> getAllBookings() {
        releaseExpiredHolds();
        List<Booking> result = new ArrayList<>();
        for (Booking booking : this.bookingRepo.findAll()) {
            result.add(attachItems(booking));
        }
        Collections.reverse(result);
        return result;
    }

    public List<SeatHold> getActiveHolds(String bookingId) {
        return this.holdRepo.findHoldingByBooking(bookingId);
    }

    public Booking loadBooking(String bookingId) {
        Booking booking = this.bookingRepo.findById(bookingId);
        return booking == null ? null : attachItems(booking);
    }

    // ===================================================================
    // Ham noi bo
    // ===================================================================

    private Booking attachItems(Booking booking) {
        booking.setItems(this.bookingSeatRepo.findByBookingId(booking.getId()));
        return booking;
    }

    private Booking getOrCreatePendingBooking(User user, String matchId) {
        Booking pending = getPendingBooking(user, matchId);
        return pending != null ? pending : createBooking(user, matchId);
    }

    private Booking createBooking(User user, String matchId) {
        Booking booking = new Booking(null, user.getId(), matchId);
        new CreateBookingCommand(booking).execute();
        this.bookingRepo.insert(booking);
        return booking;
    }

    private void releaseItem(BookingSeat item) {
        if (item.getStatus() != BookingSeatStatus.HOLD && item.getStatus() != BookingSeatStatus.PENDING) {
            return;
        }
        Ticket ticket = this.ticketRepo.findById(item.getTicketId());
        if (ticket != null && ticket.getStatus() == TicketStatus.HOLD) {
            new UnlockSeatCommand(ticket).execute();
            this.ticketRepo.update(ticket);
        }
        new ReleaseBookingSeatCommand(item).execute();
        this.bookingSeatRepo.update(item);
        SeatHold hold = this.holdRepo.findHoldingByTicket(item.getTicketId());
        if (hold != null && hold.getBookingId().equalsIgnoreCase(item.getBookingId())) {
            new ReleaseHoldCommand(hold).execute();
            this.holdRepo.update(hold);
        }
    }

    private void purgeTestBooking(String bookingId) {
        Booking booking = loadBooking(bookingId);
        if (booking == null) {
            return;
        }
        for (BookingSeat item : booking.getItems()) {
            releaseItem(item);
            this.bookingSeatRepo.delete(item.getId());
        }
        for (SeatHold hold : this.holdRepo.findAll()) {
            if (hold.getBookingId().equalsIgnoreCase(bookingId)) {
                this.holdRepo.delete(hold.getId());
            }
        }
        this.bookingRepo.delete(bookingId);
    }

    private void cancelIfEmpty(Booking booking) {
        if (booking.isPending() && booking.getActiveItems().isEmpty()) {
            new CancelBookingCommand(booking).execute();
            this.bookingRepo.update(booking);
        }
    }

    /** Han thanh toan cua booking = han som nhat trong cac ghe dang giu. */
    private void refreshExpiry(Booking booking) {
        LocalDateTime earliest = null;
        for (SeatHold hold : this.holdRepo.findHoldingByBooking(booking.getId())) {
            if (earliest == null || hold.getExpiresAt().isBefore(earliest)) {
                earliest = hold.getExpiresAt();
            }
        }
        booking.setExpiresAt(booking.getStatus() == BookingStatus.PENDING ? earliest : null);
    }

    private Booking requireOwnPendingBooking(User user, String bookingId) {
        Booking booking = loadBooking(bookingId);
        if (booking == null || !booking.getUserId().equalsIgnoreCase(user.getId())) {
            throw new BusinessException("Khong tim thay booking " + bookingId);
        }
        if (!booking.isPending()) {
            throw new BusinessException("Booking dang o trang thai " + booking.getStatus() + ".");
        }
        return booking;
    }

    private Ticket requireTicket(String ticketId) {
        Ticket ticket = this.ticketRepo.findById(ticketId);
        if (ticket == null) {
            throw new BusinessException("Khong tim thay ve " + ticketId);
        }
        return ticket;
    }

    private Match requireOnSale(String matchId) {
        Match match = this.matchController.findById(matchId);
        if (match == null || !match.isOnSale()) {
            throw new BusinessException("Tran dau chua mo ban, da dong ban hoac da dien ra.");
        }
        return match;
    }
}
