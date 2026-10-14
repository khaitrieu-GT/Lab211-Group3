package repository;

import java.util.ArrayList;
import java.util.List;
import model.Match;

public class MatchRepository extends CsvRepository<Match> {

    public MatchRepository() {
        super("matches.csv", "id,name,competition,homeTeamId,awayTeamId,stadiumId,sellerId,matchDate,startTime,status,createdAt", "M", 3);
    }

    @Override
    public Match parse(String line) {
        return Match.fromCsvLine(line);
    }

    public List<Match> findByStadiumId(String stadiumId) {
        List<Match> result = new ArrayList<>();
        for (Match m : this.findAll()) {
            if (m.getStadiumId().equalsIgnoreCase(stadiumId)) {
                result.add(m);
            }
        }
        return result;
    }

    /** Cac tran co doi bong tham gia (doi nha hoac doi khach). */
    public List<Match> findByTeamId(String teamId) {
        List<Match> result = new ArrayList<>();
        for (Match m : this.findAll()) {
            if (m.getHomeTeamId().equalsIgnoreCase(teamId) || m.getAwayTeamId().equalsIgnoreCase(teamId)) {
                result.add(m);
            }
        }
        return result;
    }

    public List<Match> findBySellerId(String sellerId) {
        List<Match> result = new ArrayList<>();
        for (Match m : this.findAll()) {
            if (m.getSellerId().equalsIgnoreCase(sellerId)) {
                result.add(m);
            }
        }
        return result;
    }
}
