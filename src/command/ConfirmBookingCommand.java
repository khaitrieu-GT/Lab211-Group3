package command;

import model.Booking;

/** Xac nhan booking sau khi thanh toan (Trieu). */
public class ConfirmBookingCommand implements Command {

    private final Booking booking;

    public ConfirmBookingCommand(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void execute() {
        this.booking.confirm();
    }
}