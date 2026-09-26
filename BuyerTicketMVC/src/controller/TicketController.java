package controller;

import model.Order;
import model.Ticket;
import view.TicketView;

public class TicketController {
    private TicketView view;
    private long ticketId = 5000;
    private Ticket latestTicket;

    public TicketController(TicketView view) { this.view = view; }

    public Ticket createTicket(Order order) {
        if (order == null) return null;
        latestTicket = new Ticket(++ticketId, order);
        return latestTicket;
    }

    public void viewTicket() { view.displayTicket(latestTicket); }
    public Ticket getLatestTicket() { return latestTicket; }
}
