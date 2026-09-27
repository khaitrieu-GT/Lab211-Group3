package com.tv4.booking.command;

import com.tv4.booking.model.Booking;

public class CancelBookingCommand implements Command {

    private final Booking booking;

    public CancelBookingCommand(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void execute() {
        booking.cancel();
    }
}