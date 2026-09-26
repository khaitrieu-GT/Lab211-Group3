package view;

import model.Match;
import java.util.List;

public class MatchView {
    public void displayMatches(List<Match> matches) {
        System.out.println("\n========== MATCH LIST ==========");
        if (matches.isEmpty()) {
            System.out.println("No match found.");
            return;
        }
        for (Match match : matches) System.out.println(match);
    }

    public void displayMatch(Match match) {
        if (match == null) {
            System.out.println("Match not found.");
            return;
        }
        System.out.println("\n========== MATCH INFORMATION ==========");
        System.out.println("Match ID: " + match.getId());
        System.out.println("Name: " + match.getName());
        System.out.println("Competition: " + match.getTitle());
        System.out.println("Teams: " + match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName());
        System.out.println("Date: " + match.getMatchDate());
        System.out.println("Time: " + match.getStartTime());
        System.out.println("Stadium: " + match.getStadium().getName());
        System.out.println("Address: " + match.getStadium().getAddress());
        System.out.println("Status: " + match.getStatus());
    }
}
