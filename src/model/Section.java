package model;

public class Section extends BaseEntity {
    private String stadiumId;
    private String name;
    private int capacity;
    private double basePrice;

    public Section() {
        super();
    }

    public Section(String id, String stadiumId, String name, int capacity, double basePrice) {
        super(id);
        this.stadiumId = stadiumId;
        this.name = name;
        this.capacity = capacity;
        this.basePrice = basePrice;
    }

    public String getStadiumId() { return this.stadiumId; }
    public void setStadiumId(String stadiumId) { this.stadiumId = stadiumId; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return this.capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getBasePrice() { return this.basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    @Override
    public String toCsvLine() {
        return String.join(",", this.getId(), this.stadiumId, this.name, String.valueOf(this.capacity), String.valueOf(this.basePrice));
    }

    public static Section fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 5) return null;
        return new Section(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            Integer.parseInt(parts[3].trim()),
            Double.parseDouble(parts[4].trim())
        );
    }
}