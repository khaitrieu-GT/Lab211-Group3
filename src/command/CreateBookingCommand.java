package command;

import model.Booking;

/** Khoi tao booking (Trieu). */
public class CreateBookingCommand implements Command {

    private final Booking booking;

    public CreateBookingCommand(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void execute() {
        this.booking.create();
    }
}