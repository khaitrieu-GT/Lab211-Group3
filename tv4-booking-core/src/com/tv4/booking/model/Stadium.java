package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.time.LocalDateTime;
import java.util.List;

public class Stadium {
    private final Long id;
    private final String name;
    private final String address;
    private final Integer capacity;
    private final LocalDateTime createdAt;
    private final List<Zone> zones;

    public Stadium(Long id, String name, String address, Integer capacity, List<Zone> zones) {
        if (capacity == null || capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }

        if (zones == null) {
            throw new IllegalArgumentException("Zones cannot be null");
        }
        this.id = id;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.createdAt = LocalDateTime.now();
        this.zones = List.copyOf(zones);
    }

    public List<Zone> getZones() {
        return zones;
    }

    public List<Seat> getAllSeats() {
        return zones.stream().flatMap(zone -> zone.getSeats().stream()).toList();
    }
}
