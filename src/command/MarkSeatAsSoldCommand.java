package command;

import model.Ticket;

/** Ghe cua tran HOLD -> SOLD (Trieu). */
public class MarkSeatAsSoldCommand implements Command {

    private final Ticket seatTicket;

    public MarkSeatAsSoldCommand(Ticket seatTicket) {
        this.seatTicket = seatTicket;
    }

    @Override
    public void execute() {
        this.seatTicket.markAsSold();
    }
}