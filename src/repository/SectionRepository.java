package repository;

import java.util.ArrayList;
import java.util.List;
import model.Section;

/** Nhat: SectionRepository (truoc day de trong). */
public class SectionRepository extends CsvRepository<Section> {

    public SectionRepository() {
        super("sections.csv", "id,stadiumId,name,type,capacity,basePrice,status", "SEC", 2);
    }

    @Override
    protected Section parse(String line) {
        return Section.fromCsvLine(line);
    }

    public List<Section> findByStadiumId(String stadiumId) {
        List<Section> result = new ArrayList<>();
        for (Section s : findAll()) {
            if (s.getStadiumId().equalsIgnoreCase(stadiumId)) {
                result.add(s);
            }
        }
        return result;
    }
}
