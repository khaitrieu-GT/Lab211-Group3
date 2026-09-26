package model;

public class Team {
    private Long id;
    private String name;
    private String logo;
    private String country;

    public Team(Long id, String name, String logo, String country) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.country = country;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLogo() { return logo; }
    public String getCountry() { return country; }

    @Override
    public String toString() { return name + " (" + country + ")"; }
}
