package view;

import model.Ticket;

public class TicketView {
    public void displayTicket(Ticket ticket) {
        if (ticket == null) {
            System.out.println("No ticket available.");
            return;
        }
        System.out.println("\n========== E-TICKET ==========");
        System.out.println("Ticket ID: " + ticket.getId());
        System.out.println("Buyer: " + ticket.getOrder().getBuyer().getName());
        System.out.println("Match: " + ticket.getOrder().getMatch().getName());
        System.out.println("Teams: " + ticket.getOrder().getMatch().getHomeTeam().getName()
                + " vs " + ticket.getOrder().getMatch().getAwayTeam().getName());
        System.out.println("Date: " + ticket.getOrder().getMatch().getMatchDate());
        System.out.println("Time: " + ticket.getOrder().getMatch().getStartTime());
        System.out.println("Stadium: " + ticket.getOrder().getMatch().getStadium().getName());
        System.out.println("QR Code: " + ticket.getQrCode());
        System.out.println("Issue Date: " + ticket.getIssueDate());
        System.out.println("Status: " + ticket.getStatus());
        System.out.println("================================");
    }

    public void displayCancellation(String id, String reason, String status) {
        System.out.println("\n========== CANCELLATION ==========");
        System.out.println("Request ID: " + id);
        System.out.println("Reason: " + reason);
        System.out.println("Status: " + status);
    }
}
