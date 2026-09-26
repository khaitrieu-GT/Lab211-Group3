package view;

import model.Order;
import model.OrderItem;
import java.util.List;

public class OrderView {
    public void displayOrder(Order order) {
        System.out.println("\n========== ORDER ==========");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Buyer: " + order.getBuyer().getName());
        System.out.println("Match: " + order.getMatch().getName());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Seats:");
        for (OrderItem item : order.getItems()) System.out.println("  " + item);
        System.out.printf("Total: %,.0f VND%n", order.getTotalAmount());
    }

    public void displayHistory(List<Order> orders) {
        System.out.println("\n========== PURCHASE HISTORY ==========");
        if (orders.isEmpty()) {
            System.out.println("No purchase history.");
            return;
        }
        for (Order order : orders) System.out.println(order);
    }
}
