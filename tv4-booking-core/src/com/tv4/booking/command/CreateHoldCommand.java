package com.tv4.booking.command;

import com.tv4.booking.model.SeatHold;

public class CreateHoldCommand implements Command {

    private final SeatHold hold;

    public CreateHoldCommand(SeatHold hold) {
        this.hold = hold;
    }

    @Override
    public void execute() {
        hold.crateHold();
    }
}