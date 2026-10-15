package repository;

import model.Stadium;

/** Nhat: StadiumRepository. */
public class StadiumRepository extends CsvRepository<Stadium> {

    public StadiumRepository() {
        super("stadiums.csv", "id,name,address,city,capacity,createdAt", "STD", 2);
    }

    @Override
    public Stadium parse(String line) {
        return Stadium.fromCsvLine(line);
    }
}
