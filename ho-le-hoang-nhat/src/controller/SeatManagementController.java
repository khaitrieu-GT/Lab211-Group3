package controller;

import exception.BusinessException;
import java.util.List;
import model.Seat;
import model.Section;
import model.Ticket;
import model.User;
import model.enums.SeatStatus;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.TicketRepository;
import service.AuditService;

/**
 * Quan ly ghe vat ly cua Admin (Nhat: updateSeatMaintenance, Trieu: Seat.lock/unlock, Khoa: SeatSection.addSeat).
 */
public class SeatManagementController {

    private final SeatRepository seatRepo = new SeatRepository();
    private final SectionRepository sectionRepo = new SectionRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final AuditService auditService = new AuditService();
    private final User admin;

    public SeatManagementController(User admin) {
        this.admin = admin;
    }

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
}
