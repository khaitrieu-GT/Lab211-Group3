package controller;

import model.Match;
import view.MatchView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MatchController {
    private List<Match> matches;
    private MatchView view;

    public MatchController(List<Match> matches, MatchView view) {
        this.matches = matches;
        this.view = view;
    }

    public void searchByName(String keyword) {
        List<Match> result = new ArrayList<>();
        for (Match match : matches) {
            if (match.getName().toLowerCase().contains(keyword.toLowerCase())) result.add(match);
        }
        view.displayMatches(result);
    }

    public void searchByDate(LocalDate date) {
        List<Match> result = new ArrayList<>();
        for (Match match : matches) {
            if (match.getMatchDate().equals(date)) result.add(match);
        }
        view.displayMatches(result);
    }

    public Match findById(Long id) {
        for (Match match : matches) if (match.getId().equals(id)) return match;
        return null;
    }

    public void viewMatch(Long id) { view.displayMatch(findById(id)); }
}
