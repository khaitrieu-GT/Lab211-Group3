package com.tv4.booking.command;

import com.tv4.booking.model.Ticket;

public class GenerateTicketQrCommand implements Command {

    private final Ticket ticket;

    public GenerateTicketQrCommand(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public void execute() {
        ticket.generateQr();
    }
}