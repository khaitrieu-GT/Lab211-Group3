package TV2Seller.view;

import java.util.List;

import TV2Seller.model.Match;
import TV2Seller.model.Ticket;

public class SellerView {

    public void showMenu() {

        System.out.println();
        System.out.println("   =====SELLER MANAGEMENT=====");
        System.out.println("1. Add Match");
        System.out.println("2. View Matches");
        System.out.println("3. Open Sale");
        System.out.println("4. Close Sale");
        System.out.println("5. Add Ticket");
        System.out.println("6. View Tickets");
        System.out.println("7. Sell Ticket");
        System.out.println("0. Exit");
        
    }

    public void showMatches(List<Match> matches) {

        System.out.println();
        System.out.println("========== MATCH LIST ==========");

        if (matches.isEmpty()) {
            System.out.println("No matches.");
            return;
        }

        for (Match match : matches) {
            System.out.println(match);
        }
    }

    public void showTickets(List<Ticket> tickets) {

        System.out.println();
        System.out.println("========== TICKET LIST ==========");

        if (tickets.isEmpty()) {
            System.out.println("No tickets.");
            return;
        }

        for (Ticket ticket : tickets) {
            System.out.println(ticket);
        }
    }

    public void showMessage(String message) {

        System.out.println(message);
    }
}