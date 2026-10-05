package controller;

import exception.BusinessException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Match;
import model.enums.MatchStatus;
import repository.MatchRepository;
import repository.StadiumRepository;
import repository.TeamRepository;

/**
 * Tim kiem va xem thong tin tran dau (Khoa: MatchController).
 */
public class MatchController {

    private final MatchRepository matchRepo = new MatchRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final StadiumRepository stadiumRepo = new StadiumRepository();

    /** Khoa: searchByName - tim theo ten tran hoac ten doi. */
    public List<Match> searchByName(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Match> result = new ArrayList<>();
        for (Match match : getVisibleMatches()) {
            String text = (match.getName() + " " + match.getCompetition() + " "
                    + (match.getHomeTeam() == null ? "" : match.getHomeTeam().getName()) + " "
                    + (match.getAwayTeam() == null ? "" : match.getAwayTeam().getName())).toLowerCase();
            if (text.contains(key)) {
                result.add(match);
            }
        }
        return result;
    }

    /** Khoa: searchByDate. */
    public List<Match> searchByDate(LocalDate date) {
        List<Match> result = new ArrayList<>();
        for (Match match : getVisibleMatches()) {
            if (match.getMatchDate().equals(date)) {
                result.add(match);
            }
        }
        return result;
    }

    /** Khoa: findById - kem doi bong va san. */
    public Match findById(String id) {
        Match match = this.matchRepo.findById(id);
        return match == null ? null : enrich(match);
    }

    /** Khoa: viewMatch. */
    public Match viewMatch(String id) {
        Match match = findById(id);
        if (match == null) {
            throw new BusinessException("Khong tim thay tran dau " + id);
        }
        return match;
    }

    /** Cac tran Buyer nhin thay: dang mo ban hoac sap mo ban, chua dien ra. */
    public List<Match> getVisibleMatches() {
        List<Match> result = new ArrayList<>();
        for (Match match : this.matchRepo.findAll()) {
            if ((match.getStatus() == MatchStatus.OPEN || match.getStatus() == MatchStatus.UPCOMING) && !match.hasStarted()) {
                result.add(enrich(match));
            }
        }
        return result;
    }

    public List<Match> getAllMatches() {
        List<Match> result = new ArrayList<>();
        for (Match match : this.matchRepo.findAll()) {
            result.add(enrich(match));
        }
        return result;
    }

    /** Nap doi nha, doi khach, san van dong de hien thi. */
    public Match enrich(Match match) {
        match.setHomeTeam(this.teamRepo.findById(match.getHomeTeamId()));
        match.setAwayTeam(this.teamRepo.findById(match.getAwayTeamId()));
        match.setStadium(this.stadiumRepo.findById(match.getStadiumId()));
        return match;
    }
}
