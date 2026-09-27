package com.tv4.booking.command;

import com.tv4.booking.model.BookingSeat;

public class LockBookingSeatCommand implements Command {

    private final BookingSeat bookingSeat;

    public LockBookingSeatCommand(BookingSeat bookingSeat) {
        this.bookingSeat = bookingSeat;
    }

    @Override
    public void execute() {
        bookingSeat.lockSeat();
    }
}