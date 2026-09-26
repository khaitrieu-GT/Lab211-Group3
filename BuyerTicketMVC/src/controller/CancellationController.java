package controller;

import model.CancellationRequest;
import model.Ticket;
import view.TicketView;

public class CancellationController {
    private long requestId = 9000;
    private CancellationRequest latestRequest;
    private TicketView view;

    public CancellationController(TicketView view) { this.view = view; }

    public void createRequest(Ticket ticket, String reason) {
        if (ticket == null) {
            System.out.println("Ticket not found.");
            return;
        }
        latestRequest = new CancellationRequest(++requestId, ticket, reason);
        latestRequest.submitRequest();
        view.displayCancellation(String.valueOf(latestRequest.getId()), latestRequest.getReason(), latestRequest.getStatus());
    }

    public void viewStatus() {
        if (latestRequest == null) {
            System.out.println("No cancellation request.");
            return;
        }
        view.displayCancellation(String.valueOf(latestRequest.getId()), latestRequest.getReason(), latestRequest.getStatus());
    }
}
