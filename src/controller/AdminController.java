package controller;

import model.*;
import repository.*;
import java.util.List;

public class AdminController {
    private final UserRepository userRepo;
    private final StadiumRepository stadiumRepo;
    private final SeatRepository seatRepo;

    public AdminController() {
        this.userRepo = new UserRepository();
        this.stadiumRepo = new StadiumRepository();
        this.seatRepo = new SeatRepository();
    }

    public List getAllUsers() {
        return this.userRepo.findAll();
    }

    public boolean changeUserStatus(String userId, String status) {
        return this.userRepo.updateUserStatus(userId, status);
    }

    public List getAllStadiums() {
        return this.stadiumRepo.findAll();
    }

    public boolean createStadium(String id, String name, String address, int capacity) {
        Stadium s = new Stadium(id, name, address, capacity);
        return this.stadiumRepo.add(s);
    }

    public List getAllSeats() {
        return this.seatRepo.findAll();
    }

    public boolean updateSeatMaintenance(String seatId, boolean isMaintenance) {
        SeatStatus status = isMaintenance ? SeatStatus.MAINTENANCE : SeatStatus.AVAILABLE;
        return this.seatRepo.updateStatus(seatId, status);
    }
}