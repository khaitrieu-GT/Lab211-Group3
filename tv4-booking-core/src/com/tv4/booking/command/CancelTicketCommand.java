package com.tv4.booking.command;

import com.tv4.booking.model.Ticket;

public class CancelTicketCommand implements Command {

    private final Ticket ticket;

    public CancelTicketCommand(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public void execute() {
        ticket.cancel();
    }
}