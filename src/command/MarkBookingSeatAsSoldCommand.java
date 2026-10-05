package command;

import model.BookingSeat;

/** Ghe trong booking HOLD -> SOLD (Trieu). */
public class MarkBookingSeatAsSoldCommand implements Command {

    private final BookingSeat bookingSeat;

    public MarkBookingSeatAsSoldCommand(BookingSeat bookingSeat) {
        this.bookingSeat = bookingSeat;
    }

    @Override
    public void execute() {
        this.bookingSeat.markAsSold();
    }
}