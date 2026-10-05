package command;

import model.BookingSeat;

/** Tra ghe trong booking (Trieu). */
public class ReleaseBookingSeatCommand implements Command {

    private final BookingSeat bookingSeat;

    public ReleaseBookingSeatCommand(BookingSeat bookingSeat) {
        this.bookingSeat = bookingSeat;
    }

    @Override
    public void execute() {
        this.bookingSeat.releaseSeat();
    }
}