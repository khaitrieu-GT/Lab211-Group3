package repository;

import java.util.ArrayList;
import java.util.List;
import model.Match;

public class MatchRepository extends CsvRepository<Match> {

    public MatchRepository() {
        super("matches.csv", "id,name,competition,homeTeamId,awayTeamId,stadiumId,sellerId,matchDate,startTime,status,createdAt", "M", 3);
    }

    @Override
    protected Match parse(String line) {
        return Match.fromCsvLine(line);
    }

    public List<Match> findBySellerId(String sellerId) {
        List<Match> result = new ArrayList<>();
        for (Match m : findAll()) {
            if (m.getSellerId().equalsIgnoreCase(sellerId)) {
                result.add(m);
            }
        }
        return result;
    }
}
