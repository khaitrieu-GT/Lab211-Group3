package repository;

import model.Team;

public class TeamRepository extends CsvRepository<Team> {

    public TeamRepository() {
        super("teams.csv", "id,name,logo,country", "T", 3);
    }

    @Override
    protected Team parse(String line) {
        return Team.fromCsvLine(line);
    }
}
