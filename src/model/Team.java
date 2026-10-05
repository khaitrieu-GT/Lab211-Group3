package model;

import util.CsvUtil;

/** Doi bong (Khoa: Team). */
public class Team extends BaseEntity {
    private String name;
    private String logo;
    private String country;

    public Team() {
        super();
    }

    public Team(String id, String name, String logo, String country) {
        super(id);
        this.name = name;
        this.logo = logo;
        this.country = country;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getLogo() { return this.logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getCountry() { return this.country; }
    public void setCountry(String country) { this.country = country; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.name, this.logo, this.country);
    }

    public static Team fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 4) {
            return null;
        }
        return new Team(p[0], p[1], p[2], p[3]);
    }

    @Override
    public String toString() {
        return this.name + " (" + this.country + ")";
    }
}
