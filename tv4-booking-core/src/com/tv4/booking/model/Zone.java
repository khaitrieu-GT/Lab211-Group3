package com.tv4.booking.model;

import java.util.List;

public class Zone {
    private final Long id;
    private final String name;
    private final String type;
    private final Integer capacity;
    private final Long createdId;
    private final List<Seat> seats;

    public Zone(Long id, String name, String type, Integer capacity, Long createdId, List<Seat> seats) {
        if (capacity == null || capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }

        if (seats == null || seats.size() > capacity) {
            throw new IllegalArgumentException("Seat count exceeds zone capacity");
        }
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.createdId = createdId;
        this.seats = List.copyOf(seats);
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public List<Seat> getAvailableSeats() {
        return seats.stream().filter(Seat::isAvailable).toList();
    }

}
