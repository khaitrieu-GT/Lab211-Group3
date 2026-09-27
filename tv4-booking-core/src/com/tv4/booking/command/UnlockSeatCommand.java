package com.tv4.booking.command;

import com.tv4.booking.model.Seat;

public class UnlockSeatCommand implements Command {

    private final Seat seat;

    public UnlockSeatCommand(Seat seat) {
        this.seat = seat;
    }

    @Override
    public void execute() {
        seat.unlock();
    }
}