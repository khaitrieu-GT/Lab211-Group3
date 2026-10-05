package model;

public class Stadium extends BaseEntity {
    private String name;
    private String address;
    private int capacity;

    public Stadium() {
        super();
    }

    public Stadium(String id, String name, String address, int capacity) {
        super(id);
        this.name = name;
        this.address = address;
        this.capacity = capacity;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return this.address; }
    public void setAddress(String address) { this.address = address; }

    public int getCapacity() { return this.capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toCsvLine() {
        return String.join(",", this.getId(), this.name, this.address, String.valueOf(this.capacity));
    }

    public static Stadium fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 4) return null;
        return new Stadium(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            Integer.parseInt(parts[3].trim())
        );
    }
}