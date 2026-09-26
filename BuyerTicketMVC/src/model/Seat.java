package model;

public class Seat {
    private Long id;
    private String row;
    private String number;
    private double price;
    private String status;

    public Seat(Long id, String row, String number, double price) {
        this.id = id;
        this.row = row;
        this.number = number;
        this.price = price;
        this.status = "AVAILABLE";
    }

    public boolean select() {
        if ("AVAILABLE".equals(status)) {
            status = "SELECTED";
            return true;
        }
        return false;
    }

    public void unselect() {
        if ("SELECTED".equals(status)) status = "AVAILABLE";
    }

    public void sell() { status = "SOLD"; }
    public boolean isAvailable() { return "AVAILABLE".equals(status); }
    public Long getId() { return id; }
    public String getRow() { return row; }
    public String getNumber() { return number; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("Seat %s%s | Price: %,.0f VND | Status: %s", row, number, price, status);
    }
}
