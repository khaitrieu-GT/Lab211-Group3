package command;

import model.ETicket;

/** Sinh ma QR cho ve dien tu (Trieu). */
public class GenerateTicketQrCommand implements Command {

    private final ETicket ticket;

    public GenerateTicketQrCommand(ETicket ticket) {
        this.ticket = ticket;
    }

    @Override
    public void execute() {
        this.ticket.generateQr();
    }
}