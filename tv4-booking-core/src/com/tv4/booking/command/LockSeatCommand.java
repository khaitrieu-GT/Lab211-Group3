package com.tv4.booking.command;

import com.tv4.booking.model.Seat;

public class LockSeatCommand implements Command {

    private final Seat seat;

    public LockSeatCommand(Seat seat) {
        this.seat = seat;
    }

    @Override
    public void execute() {
        seat.lock();
    }
}