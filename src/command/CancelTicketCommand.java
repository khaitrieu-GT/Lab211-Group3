package command;

import model.ETicket;

/** Huy ve dien tu (Trieu). */
public class CancelTicketCommand implements Command {

    private final ETicket ticket;

    public CancelTicketCommand(ETicket ticket) {
        this.ticket = ticket;
    }

    @Override
    public void execute() {
        this.ticket.cancel();
    }
}