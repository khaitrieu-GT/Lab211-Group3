package model;

import java.util.ArrayList;
import java.util.List;

public class SeatSection {
    private Long id;
    private String name;
    private double basePrice;
    private int capacity;
    private List<Seat> seats;

    public SeatSection(Long id, String name, double basePrice, int capacity) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.capacity = capacity;
        this.seats = new ArrayList<>();
    }

    public void addSeat(Seat seat) {
        if (seat != null && seats.size() < capacity) seats.add(seat);
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> result = new ArrayList<>();
        for (Seat seat : seats) if (seat.isAvailable()) result.add(seat);
        return result;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public int getCapacity() { return capacity; }
    public List<Seat> getSeats() { return seats; }
}
