package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.enums.SectionStatus;
import util.CsvUtil;
import util.MoneyUtil;

/**
 * Khu vuc / khan dai trong san - hop nhat Section (Nhat, Khanh), SeatSection (Khoa), Zone (Trieu).
 */
public class Section extends BaseEntity {
    private String stadiumId;
    private String name;
    private String type;            // VIP, STANDARD... (Trieu: Zone.type)
    private int capacity;
    private BigDecimal basePrice;
    private SectionStatus status;
    private List<Seat> seats = new ArrayList<>();

    public Section() {
        super();
    }

    public Section(String id, String stadiumId, String name, String type, int capacity, BigDecimal basePrice) {
        super(id);
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }
        if (basePrice == null || basePrice.signum() < 0) {
            throw new IllegalArgumentException("Base price must be non-negative");
        }
        this.stadiumId = stadiumId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.status = SectionStatus.ACTIVE;
    }

    /** Khoa: SeatSection.addSeat - khong vuot suc chua. */
    public boolean addSeat(Seat seat) {
        if (seat != null && this.seats.size() < this.capacity) {
            this.seats.add(seat);
            return true;
        }
        return false;
    }

    /** Khoa + Trieu: getAvailableSeats. */
    public List<Seat> getAvailableSeats() {
        List<Seat> result = new ArrayList<>();
        for (Seat seat : this.seats) {
            if (seat.isAvailable()) {
                result.add(seat);
            }
        }
        return result;
    }

    /** Khanh: Section.updateSection. */
    public void updateSection(String name, int capacity) {
        if (capacity < this.seats.size()) {
            throw new IllegalArgumentException("Capacity is smaller than current seat count");
        }
        this.name = name;
        this.capacity = capacity;
    }

    /** Khanh: Section.updateStatus. */
    public void updateStatus(SectionStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return this.status == SectionStatus.ACTIVE;
    }

    public String getStadiumId() { return this.stadiumId; }
    public void setStadiumId(String stadiumId) { this.stadiumId = stadiumId; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }

    public int getCapacity() { return this.capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public BigDecimal getBasePrice() { return this.basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public SectionStatus getStatus() { return this.status; }
    public void setStatus(SectionStatus status) { this.status = status; }

    public List<Seat> getSeats() { return this.seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.stadiumId, this.name, this.type, this.capacity, this.basePrice, this.status);
    }

    public static Section fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 7) {
            return null;
        }
        Section section = new Section(p[0], p[1], p[2], p[3], CsvUtil.toInt(p[4]), CsvUtil.toMoney(p[5]));
        section.status = CsvUtil.toEnum(SectionStatus.class, p[6]);
        return section;
    }

    @Override
    public String toString() {
        return String.format("%-6s | San: %-6s | %-20s | %-8s | Suc chua: %,6d | Gia goc: %s | %s",
                getId(), this.stadiumId, this.name, this.type, this.capacity, MoneyUtil.format(this.basePrice), this.status);
    }
}
