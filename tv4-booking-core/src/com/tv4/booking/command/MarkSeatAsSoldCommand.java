package com.tv4.booking.command;

import com.tv4.booking.model.Seat;

public class MarkSeatAsSoldCommand implements Command {

    private final Seat seat;

    public MarkSeatAsSoldCommand(Seat seat) {
        this.seat = seat;
    }

    @Override
    public void execute() {
        seat.markAsSold();
    }
}