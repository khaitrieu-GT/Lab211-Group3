package model;

import java.util.ArrayList;
import java.util.List;

public class Stadium {
    private Long id;
    private String name;
    private String address;
    private String city;
    private List<SeatSection> sections;

    public Stadium(Long id, String name, String address, String city) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.sections = new ArrayList<>();
    }

    public void addSection(SeatSection section) {
        if (section != null) sections.add(section);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public List<SeatSection> getSections() { return sections; }
}
