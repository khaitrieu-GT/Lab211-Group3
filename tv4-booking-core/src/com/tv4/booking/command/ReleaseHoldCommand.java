package com.tv4.booking.command;

import com.tv4.booking.model.SeatHold;

public class ReleaseHoldCommand implements Command {

    private final SeatHold hold;

    public ReleaseHoldCommand(SeatHold hold) {
        this.hold = hold;
    }

    @Override
    public void execute() {
        hold.releaseHold();
    }
}