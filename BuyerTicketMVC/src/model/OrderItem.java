package model;

public class OrderItem {
    private Long id;
    private int quantity;
    private double price;
    private Seat seat;

    public OrderItem(Long id, Seat seat) {
        this.id = id;
        this.seat = seat;
        this.quantity = 1;
        this.price = seat.getPrice();
    }

    public double calculateSubtotal() { return quantity * price; }
    public Long getId() { return id; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public Seat getSeat() { return seat; }

    @Override
    public String toString() {
        return String.format("Seat %s%s | Price: %,.0f VND", seat.getRow(), seat.getNumber(), price);
    }
}
