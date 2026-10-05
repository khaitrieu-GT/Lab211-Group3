package controller;

import model.Match;
import model.Order;
import model.OrderItem;
import model.Seat;
import model.User;
import view.OrderView;

public class OrderController {
    private OrderView view;
    private long orderId = 1000;

    public OrderController(OrderView view) { this.view = view; }

    public Order createOrder(User buyer, Match match, Seat seat) {
        if (seat == null) return null;
        Order order = new Order(++orderId, buyer, match);
        order.addItem(new OrderItem(1L, seat));
        view.displayOrder(order);
        return order;
    }

    public void confirmOrder(Order order) {
        if (order == null) return;
        order.confirmOrder();
        order.getBuyer().addOrder(order);
        System.out.println("Order confirmed successfully.");
    }

    public void viewPurchaseHistory(User buyer) { view.displayHistory(buyer.getOrders()); }
}
