package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import util.CsvUtil;

/**
 * San van dong - hop nhat Stadium cua Nhat, Khoa, Trieu.
 * Danh sach khu vuc (sections) khong luu CSV, duoc repository/controller nap vao khi can.
 */
public class Stadium extends BaseEntity {
    private String name;
    private String address;
    private String city;
    private int capacity;
    private LocalDateTime createdAt;
    private final List<Section> sections = new ArrayList<>();

    public Stadium() {
        super();
    }

    public Stadium(String id, String name, String address, String city, int capacity) {
        super(id);
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }
        this.name = name;
        this.address = address;
        this.city = city;
        this.capacity = capacity;
        this.createdAt = LocalDateTime.now();
    }

    /** Khoa: Stadium.addSection. */
    public void addSection(Section section) {
        if (section != null) {
            this.sections.add(section);
        }
    }

    /** Khoa: getSections / Trieu: getZones. */
    public List<Section> getSections() {
        return this.sections;
    }

    /** Trieu: Stadium.getAllSeats. */
    public List<Seat> getAllSeats() {
        List<Seat> result = new ArrayList<>();
        for (Section section : this.sections) {
            result.addAll(section.getSeats());
        }
        return result;
    }

    /** Tong suc chua cua cac khu vuc khong duoc vuot suc chua san. */
    public int getAllocatedCapacity() {
        int total = 0;
        for (Section section : this.sections) {
            total += section.getCapacity();
        }
        return total;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return this.address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return this.city; }
    public void setCity(String city) { this.city = city; }

    public int getCapacity() { return this.capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.name, this.address, this.city, this.capacity, this.createdAt);
    }

    public static Stadium fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 6) {
            return null;
        }
        Stadium stadium = new Stadium(p[0], p[1], p[2], p[3], CsvUtil.toInt(p[4]));
        stadium.createdAt = CsvUtil.toDateTime(p[5]);
        return stadium;
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-28s | %-28s | %-12s | Suc chua: %,d",
                getId(), this.name, this.address, this.city, this.capacity);
    }
}
