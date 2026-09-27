package com.tv4.booking.command;

import com.tv4.booking.model.BookingSeat;

public class MarkBookingSeatAsSoldCommand implements Command {

    private final BookingSeat bookingSeat;

    public MarkBookingSeatAsSoldCommand(BookingSeat bookingSeat) {
        this.bookingSeat = bookingSeat;
    }

    @Override
    public void execute() {
        bookingSeat.markAsSold();
    }
}