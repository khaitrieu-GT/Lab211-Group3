package view;

import java.util.List;
import model.CancellationRequest;
import model.ETicket;
import model.Match;
import model.Ticket;

/** Hien thi ve (Khoa: TicketView, Khanh: SellerView.showTickets). */
public class TicketView extends BaseView {

    /** Khoa: displayTicket - ve dien tu. */
    public void displayTicket(ETicket ticket, Match match) {
        System.out.println();
        System.out.println("+------------------------- E-TICKET -------------------------+");
        System.out.println("  Ma ve      : " + ticket.getId() + "   (Booking " + ticket.getBookingId() + ")");
        if (match != null) {
            System.out.println("  Tran       : " + match.getName() + " - " + match.getCompetition());
            if (match.getHomeTeam() != null && match.getAwayTeam() != null) {
                System.out.println("  Doi dau    : " + match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName());
            }
            System.out.println("  Thoi gian  : " + match.getMatchDate() + " " + match.getStartTime());
            if (match.getStadium() != null) {
                System.out.println("  San        : " + match.getStadium().getName());
            }
        }
        System.out.println("  Cho ngoi   : " + ticket.getSeatInfo());
        System.out.println("  QR Code    : " + ticket.getQrCode());
        System.out.println("  Ngay phat  : " + ticket.getIssueDate().withNano(0));
        System.out.println("  Trang thai : " + ticket.getStatus());
        System.out.println("+------------------------------------------------------------+");
    }

    public void displayETickets(List<ETicket> tickets) {
        printSection("VE DIEN TU CUA BAN");
        printList(tickets, "Ban chua co ve nao.");
    }

    /** Khanh: SellerView.showTickets - ve mo ban. */
    public void showTickets(List<Ticket> tickets) {
        printSection("DANH SACH VE MO BAN");
        printList(tickets, "Chua co ve.");
    }

    /** Khoa: displayCancellation. */
    public void displayCancellation(CancellationRequest request) {
        printSection("YEU CAU HUY VE");
        System.out.println("Ma yeu cau : " + request.getId());
        System.out.println("Ve         : " + request.getETicketId());
        System.out.println("Ly do      : " + request.getReason());
        System.out.println("Ngay gui   : " + request.getRequestDate().withNano(0));
        System.out.println("Trang thai : " + request.getStatus());
        if (request.getNote() != null) {
            System.out.println("Ghi chu    : " + request.getNote());
        }
    }

    public void displayCancellations(List<CancellationRequest> requests) {
        printSection("DANH SACH YEU CAU HUY VE");
        printList(requests, "Khong co yeu cau nao.");
    }
}
