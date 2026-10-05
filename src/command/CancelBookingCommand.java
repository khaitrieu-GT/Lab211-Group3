package command;

import model.Booking;

/** Huy booking (Trieu). */
public class CancelBookingCommand implements Command {

    private final Booking booking;

    public CancelBookingCommand(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void execute() {
        this.booking.cancel();
    }
}