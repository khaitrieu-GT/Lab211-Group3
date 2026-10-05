package controller;

import command.CancelBookingCommand;
import command.CancelTicketCommand;
import exception.BusinessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Booking;
import model.BookingSeat;
import model.CancellationRequest;
import model.ETicket;
import model.Match;
import model.Payment;
import model.Ticket;
import model.User;
import model.enums.BookingStatus;
import model.enums.Role;
import repository.BookingRepository;
import repository.BookingSeatRepository;
import repository.CancellationRequestRepository;
import repository.ETicketRepository;
import repository.TicketRepository;
import service.AuditService;
import service.NotificationService;
import util.MoneyUtil;

/**
 * Yeu cau huy ve: Buyer gui, Admin duyet/tu choi (Khoa: CancellationController).
 */
public class CancellationController {

    private final CancellationRequestRepository requestRepo = new CancellationRequestRepository();
    private final ETicketRepository eTicketRepo = new ETicketRepository();
    private final BookingSeatRepository bookingSeatRepo = new BookingSeatRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final MatchController matchController = new MatchController();
    private final BookingController bookingController = new BookingController();
    private final PaymentController paymentController = new PaymentController();
    private final NotificationService notificationService = new NotificationService();
    private final AuditService auditService = new AuditService();

    /** Khoa: createRequest(ticket, reason). */
    public CancellationRequest createRequest(User user, String eTicketId, String reason) {
        ETicket ticket = this.eTicketRepo.findById(eTicketId);
        if (ticket == null || !ticket.getUserId().equalsIgnoreCase(user.getId())) {
            throw new BusinessException("Khong tim thay ve " + eTicketId);
        }
        if (!ticket.validateTicket()) {
            throw new BusinessException("Chi ve dang ACTIVE moi duoc yeu cau huy (hien tai: " + ticket.getStatus() + ").");
        }
        Match match = this.matchController.findById(ticket.getMatchId());
        if (match != null && match.hasStarted()) {
            throw new BusinessException("Tran dau da dien ra, khong the huy ve.");
        }
        if (this.requestRepo.findPendingByETicket(eTicketId) != null) {
            throw new BusinessException("Ve nay da co yeu cau huy dang cho xu ly.");
        }
        CancellationRequest request = new CancellationRequest(null, eTicketId, user.getId(), reason);
        request.submitRequest();
        this.requestRepo.insert(request);
        this.notificationService.send(user.getId(), "Da gui yeu cau huy ve " + eTicketId + " (ma " + request.getId() + ").");
        this.notificationService.sendToRole(Role.ADMIN, "Co yeu cau huy ve moi " + request.getId() + " tu " + user.getUsername());
        this.auditService.log(user.getUsername(), "CANCEL_REQUEST " + request.getId(), AuditService.SUCCESS);
        return request;
    }

    /** Khoa: viewStatus - cac yeu cau cua nguoi dung, moi nhat len dau. */
    public List<CancellationRequest> viewStatus(User user) {
        List<CancellationRequest> list = new ArrayList<>(this.requestRepo.findByUserId(user.getId()));
        Collections.reverse(list);
        return list;
    }

    public List<CancellationRequest> getPendingRequests() {
        return this.requestRepo.findPending();
    }

    public List<CancellationRequest> getAllRequests() {
        List<CancellationRequest> list = new ArrayList<>(this.requestRepo.findAll());
        Collections.reverse(list);
        return list;
    }

    /**
     * Khoa: approveRequest - huy ve dien tu, tra ghe ve kho ban, hoan tien.
     * Huy het moi ve trong booking thi booking chuyen CANCELLED va hoan toan bo.
     */
    public Payment approveRequest(User admin, String requestId) {
        CancellationRequest request = requirePending(requestId);
        ETicket eTicket = this.eTicketRepo.findById(request.getETicketId());
        if (eTicket == null) {
            throw new BusinessException("Ve dien tu cua yeu cau khong con ton tai.");
        }

        new CancelTicketCommand(eTicket).execute();
        this.eTicketRepo.update(eTicket);

        BookingSeat item = this.bookingSeatRepo.findById(eTicket.getBookingSeatId());
        item.cancelSold();
        this.bookingSeatRepo.update(item);

        Ticket ticket = this.ticketRepo.findById(eTicket.getTicketId());
        ticket.putOnSale();
        this.ticketRepo.update(ticket);

        Booking booking = this.bookingController.loadBooking(eTicket.getBookingId());
        Payment payment;
        if (booking.getActiveItems().isEmpty() && booking.getStatus() == BookingStatus.CONFIRMED) {
            new CancelBookingCommand(booking).execute();
            this.bookingRepo.update(booking);
            payment = this.paymentController.refund(booking.getId(), null);
        } else {
            payment = this.paymentController.refund(booking.getId(), item.getPrice());
        }

        request.approveRequest(admin.getId());
        this.requestRepo.update(request);
        this.notificationService.send(request.getUserId(), "Yeu cau huy ve " + eTicket.getId() + " da duoc duyet. Hoan tien "
                + MoneyUtil.format(item.getPrice()) + ".");
        this.auditService.log(admin.getUsername(), "APPROVE_CANCEL " + requestId, AuditService.SUCCESS);
        return payment;
    }

    /** Khoa: rejectRequest. */
    public void rejectRequest(User admin, String requestId, String note) {
        CancellationRequest request = requirePending(requestId);
        request.rejectRequest(admin.getId(), note);
        this.requestRepo.update(request);
        this.notificationService.send(request.getUserId(), "Yeu cau huy ve " + request.getETicketId()
                + " bi tu choi. Ly do: " + note);
        this.auditService.log(admin.getUsername(), "REJECT_CANCEL " + requestId, AuditService.SUCCESS);
    }

    private CancellationRequest requirePending(String requestId) {
        CancellationRequest request = this.requestRepo.findById(requestId);
        if (request == null) {
            throw new BusinessException("Khong tim thay yeu cau " + requestId);
        }
        if (!request.isPending()) {
            throw new BusinessException("Yeu cau da duoc xu ly (" + request.getStatus() + ").");
        }
        return request;
    }
}
