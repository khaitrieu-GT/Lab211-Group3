package TV2Seller.controller;

import java.util.ArrayList;
import java.util.List;
import TV2Seller.model.Match;
import TV2Seller.model.Ticket;

public class SellerController {

    private final List<Match> matches = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();

   

    public void createMatch(Match match) {

        matches.add(match);

        System.out.println("Create match successfully!");
    }

    public void updateMatch(Match match) {

        for (int i = 0; i < matches.size(); i++) {

            if (matches.get(i).getMatchId() == match.getMatchId()) {

                matches.set(i, match);

                System.out.println("Update match successfully!");
                return;
            }
        }

        System.out.println("Match not found!");
    }

    public void deleteMatch(int matchId) {

        for (Match match : matches) {

            if (match.getMatchId() == matchId) {

                matches.remove(match);

                System.out.println("Delete match successfully!");
                return;
            }
        }

        System.out.println("Match not found!");
    }

    public List<Match> getMatches() {

        return matches;
    }

    public void openSale(int matchId) {

        for (Match match : matches) {

            if (match.getMatchId() == matchId) {

                match.openSale();

                return;
            }
        }

        System.out.println("Match not found!");
    }

    public void closeSale(int matchId) {

        for (Match match : matches) {

            if (match.getMatchId() == matchId) {

                match.closeSale();

                return;
            }
        }

        System.out.println("Match not found!");
    }


    public void createTicket(Ticket ticket) {

        tickets.add(ticket);

        System.out.println("Create ticket successfully!");
    }

    public void updateTicket(Ticket ticket) {

        for (int i = 0; i < tickets.size(); i++) {

            if (tickets.get(i).getTicketId() == ticket.getTicketId()) {

                tickets.set(i, ticket);

                System.out.println("Update ticket successfully!");
                return;
            }
        }

        System.out.println("Ticket not found!");
    }

    public List<Ticket> getTickets() {

        return tickets;
    }

    public void sellTicket(int ticketId) {

        for (Ticket ticket : tickets) {

            if (ticket.getTicketId() == ticketId) {

                ticket.sellTicket();

                return;
            }
        }

        System.out.println("Ticket not found!");
    }
}
