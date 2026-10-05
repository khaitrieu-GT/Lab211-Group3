package view;

import model.Seat;
import model.SeatSection;
import model.Stadium;

public class SeatView {
    public void displaySeatMap(Stadium stadium) {
        System.out.println("\n========== STADIUM MAP ==========");
        System.out.println("Stadium: " + stadium.getName());
        System.out.println("Address: " + stadium.getAddress());
        for (SeatSection section : stadium.getSections()) {
            System.out.println("\n--- SECTION " + section.getName() + " ---");
            for (Seat seat : section.getSeats()) System.out.println(seat);
        }
    }

    public void displaySelectionResult(boolean success) {
        System.out.println(success ? "Seat selected successfully." : "Seat is not available.");
    }
}
