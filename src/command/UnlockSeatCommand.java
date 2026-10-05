package command;

import model.Ticket;

/** Tra ghe cua tran HOLD -> AVAILABLE (Trieu). */
public class UnlockSeatCommand implements Command {

    private final Ticket seatTicket;

    public UnlockSeatCommand(Ticket seatTicket) {
        this.seatTicket = seatTicket;
    }

    @Override
    public void execute() {
        this.seatTicket.unlock();
    }
}