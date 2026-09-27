package com.tv4.booking.command;

import com.tv4.booking.model.Booking;

public class CreateBookingCommand implements Command {

    private final Booking booking;

    public CreateBookingCommand(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void execute() {
        booking.create();
    }
}