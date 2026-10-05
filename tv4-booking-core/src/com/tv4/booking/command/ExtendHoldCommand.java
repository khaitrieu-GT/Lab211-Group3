package com.tv4.booking.command;

import com.tv4.booking.model.SeatHold;

public class ExtendHoldCommand implements Command {

    private final SeatHold hold;

    public ExtendHoldCommand(SeatHold hold) {
        this.hold = hold;
    }

    @Override
    public void execute() {
        hold.extendHold();
    }
}