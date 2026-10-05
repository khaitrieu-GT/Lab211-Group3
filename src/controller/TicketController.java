package controller;

import command.GenerateTicketQrCommand;
import exception.BusinessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Booking;
import model.BookingSeat;
import model.ETicket;
import model.Match;
import model.User;
import model.enums.BookingSeatStatus;
import model.enums.Role;
import repository.ETicketRepository;
import service.AuditService;

/**
 * Phat hanh, xem va soat ve dien tu (Khoa: TicketController + Trieu: GenerateTicketQrCommand).
 */
public class TicketController {

    private final ETicketRepository eTicketRepo = new ETicketRepository();
    private final MatchController matchController = new MatchController();
    private final AuditService auditService = new AuditService();

    /** Khoa: createTicket(order) - phat hanh ve cho tung ghe da ban trong booking. */
    public List<ETicket> createTicket(Booking booking) {
        Match match = this.matchController.findById(booking.getMatchId());
        String matchName = match == null ? booking.getMatchId() : match.getName();
        List<ETicket> issued = new ArrayList<>();
        for (BookingSeat item : booking.getItems()) {
            if (item.getStatus() != BookingSeatStatus.SOLD) {
                continue;
            }
            String seatInfo = matchName + " - Khu " + item.getSectionId() + " - Ghe " + item.getSeatLabel();
            ETicket ticket = new ETicket(null, booking.getId(), item.getId(), item.getTicketId(),
                    booking.getUserId(), booking.getMatchId(), seatInfo);
            this.eTicketRepo.insert(ticket);              // cap id truoc de sinh QR
            new GenerateTicketQrCommand(ticket).execute();
            this.eTicketRepo.update(ticket);
            issued.add(ticket);
        }
        return issued;
    }

    /** Khoa: viewTicket - chi xem duoc ve cua chinh minh. */
    public ETicket viewTicket(User user, String eTicketId) {
        ETicket ticket = this.eTicketRepo.findById(eTicketId);
        if (ticket == null || (user.getRole() == Role.BUYER && !ticket.getUserId().equalsIgnoreCase(user.getId()))) {
            throw new BusinessException("Khong tim thay ve " + eTicketId);
        }
        return ticket;
    }

    /** Ve cua nguoi dung, moi nhat len dau. */
    public List<ETicket> getTicketsOfUser(User user) {
        List<ETicket> list = new ArrayList<>(this.eTicketRepo.findByUserId(user.getId()));
        Collections.reverse(list);
        return list;
    }

    /** Khoa: getLatestTicket. */
    public ETicket getLatestTicket(User user) {
        List<ETicket> list = getTicketsOfUser(user);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<ETicket> getTicketsOfBooking(String bookingId) {
        return this.eTicketRepo.findByBookingId(bookingId);
    }

    /** Soat ve tai cong: Seller chi soat ve tran cua minh. */
    public ETicket checkIn(User staff, String qrCode) {
        ETicket ticket = this.eTicketRepo.findByQrCode(qrCode);
        if (ticket == null) {
            throw new BusinessException("Ma QR khong ton tai.");
        }
        Match match = this.matchController.findById(ticket.getMatchId());
        if (staff.getRole() == Role.SELLER && (match == null || !match.getSellerId().equalsIgnoreCase(staff.getId()))) {
            throw new BusinessException("Ve nay khong thuoc tran dau cua ban.");
        }
        if (!ticket.validateTicket()) {
            this.auditService.log(staff.getUsername(), "CHECK_IN " + ticket.getId(), AuditService.FAILED);
            throw new BusinessException("Ve khong hop le (trang thai " + ticket.getStatus() + ").");
        }
        ticket.checkIn();
        this.eTicketRepo.update(ticket);
        this.auditService.log(staff.getUsername(), "CHECK_IN " + ticket.getId(), AuditService.SUCCESS);
        return ticket;
    }

    public Match getMatchOf(ETicket ticket) {
        return this.matchController.findById(ticket.getMatchId());
    }
}
