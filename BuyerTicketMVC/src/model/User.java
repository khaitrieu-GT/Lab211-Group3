package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String avatar;
    private String status;
    private List<Order> orders;

    public User(Long id, String name, String email, String password, String phone, String avatar) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.avatar = avatar;
        this.status = "ACTIVE";
        this.orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        if (order != null) orders.add(order);
    }

    public List<Order> getOrders() { return orders; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAvatar() { return avatar; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "User ID: " + id + " | Name: " + name + " | Email: " + email + " | Phone: " + phone + " | Status: " + status;
    }
}
