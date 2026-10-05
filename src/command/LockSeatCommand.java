package command;

import model.Ticket;

/** Giu ghe cua tran AVAILABLE -> HOLD (Trieu). */
public class LockSeatCommand implements Command {

    private final Ticket seatTicket;

    public LockSeatCommand(Ticket seatTicket) {
        this.seatTicket = seatTicket;
    }

    @Override
    public void execute() {
        this.seatTicket.lock();
    }
}