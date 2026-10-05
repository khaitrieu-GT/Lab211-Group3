package command;

import model.BookingSeat;

/** Khoa ghe trong booking PENDING -> HOLD (Trieu). */
public class LockBookingSeatCommand implements Command {

    private final BookingSeat bookingSeat;

    public LockBookingSeatCommand(BookingSeat bookingSeat) {
        this.bookingSeat = bookingSeat;
    }

    @Override
    public void execute() {
        this.bookingSeat.lockSeat();
    }
}