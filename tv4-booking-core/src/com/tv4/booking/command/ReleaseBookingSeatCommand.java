package com.tv4.booking.command;

import com.tv4.booking.model.BookingSeat;

public class ReleaseBookingSeatCommand implements Command {

    private final BookingSeat bookingSeat;

    public ReleaseBookingSeatCommand(BookingSeat bookingSeat) {
        this.bookingSeat = bookingSeat;
    }

    @Override
    public void execute() {
        bookingSeat.releaseSeat();
    }
}