package com.tv4.booking.command;

import com.tv4.booking.model.Booking;

public class ConfirmBookingCommand implements Command {

    private final Booking booking;

    public ConfirmBookingCommand(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void execute() {
        booking.confirm();
    }
}