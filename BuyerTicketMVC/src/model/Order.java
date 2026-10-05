package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status;
    private User buyer;
    private Match match;
    private List<OrderItem> items;

    public Order(Long id, User buyer, Match match) {
        this.id = id;
        this.buyer = buyer;
        this.match = match;
        this.orderDate = LocalDateTime.now();
        this.totalAmount = 0;
        this.status = "PENDING";
        this.items = new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (item != null) {
            items.add(item);
            calculateTotal();
        }
    }

    public double calculateTotal() {
        totalAmount = 0;
        for (OrderItem item : items) totalAmount += item.calculateSubtotal();
        return totalAmount;
    }

    public void confirmOrder() {
        if (items.isEmpty()) return;
        status = "CONFIRMED";
        for (OrderItem item : items) item.getSeat().sell();
    }

    public void cancelOrder() { status = "CANCELLED"; }
    public Long getId() { return id; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public User getBuyer() { return buyer; }
    public Match getMatch() { return match; }
    public List<OrderItem> getItems() { return items; }

    @Override
    public String toString() {
        return String.format("Order #%d | %s | Total: %,.0f VND | Status: %s",
                id, match.getName(), totalAmount, status);
    }
}
